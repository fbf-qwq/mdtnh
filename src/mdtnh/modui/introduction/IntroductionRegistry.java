package mdtnh.modui.introduction;

import arc.scene.style.Drawable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 介绍菜单注册表。
 */
public final class IntroductionRegistry {

    private final Map<String, IntroductionSection> sections =
            new LinkedHashMap<>();

    private final Map<String, IntroductionPage> pages =
            new LinkedHashMap<>();

    public synchronized IntroductionSection section(
            String id,
            String titleKey,
            Drawable icon,
            int order) {

        validateId(id);

        if (titleKey == null || titleKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "Introduction section titleKey is empty."
            );
        }

        IntroductionSection section =
                new IntroductionSection(
                        id,
                        titleKey,
                        icon,
                        order
                );

        sections.put(id, section);
        return section;
    }

    public synchronized void registerPage(
            IntroductionPage page) {

        if (page == null) {
            throw new IllegalArgumentException(
                    "Introduction page is null."
            );
        }

        validateId(page.id());
        validateId(page.sectionId());

        if (page.titleKey() == null ||
                page.titleKey().isEmpty()) {
            throw new IllegalArgumentException(
                    "Introduction page titleKey is empty."
            );
        }

        if (!sections.containsKey(
                page.sectionId()
        )) {
            throw new IllegalArgumentException(
                    "Unknown introduction section: " +
                            page.sectionId()
            );
        }

        pages.put(page.id(), page);
    }

    public synchronized boolean unregisterPage(
            String id) {

        return id != null &&
                pages.remove(id) != null;
    }

    public synchronized IntroductionSection section(
            String id) {

        return sections.get(id);
    }

    public synchronized IntroductionPage page(
            String id) {

        return pages.get(id);
    }

    public synchronized List<IntroductionSection> sections() {
        List<IntroductionSection> result =
                new ArrayList<>(
                        sections.values()
                );

        result.sort(
                Comparator
                        .comparingInt(
                                (IntroductionSection s) ->
                                        s.order
                        )
                        .thenComparing(
                                s -> s.id
                        )
        );

        return result;
    }

    public synchronized List<IntroductionPage> pagesForSection(
            String sectionId) {

        List<IntroductionPage> result =
                new ArrayList<>();

        for (IntroductionPage page :
                pages.values()) {

            if (page.visible() &&
                    page.sectionId().equals(
                            sectionId
                    )) {

                result.add(page);
            }
        }

        result.sort(
                Comparator
                        .comparingInt(
                                IntroductionPage::order
                        )
                        .thenComparing(
                                IntroductionPage::id
                        )
        );

        return result;
    }

    private void validateId(String id) {
        if (id == null ||
                id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Introduction id is empty."
            );
        }
    }
}
