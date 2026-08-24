package mdtnh.modui.introduction;

import arc.Core;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品、流体、建筑、单位和状态效果共用的内容浏览页。
 */
public final class ContentCatalogPage
        implements IntroductionPage {

    public enum Kind {
        item,
        liquid,
        block,
        unit,
        status
    }

    private static final int CONTENTS_PER_PAGE = 40;
    private static final int GRID_COLUMNS = 8;

    private static final float CONTENT_BUTTON_SIZE = 58f;
    private static final float DETAIL_WIDTH = 790f;
    private static final float NAV_BUTTON_WIDTH = 160f;
    private static final float NAV_BUTTON_HEIGHT = 46f;

    private final Kind kind;
    private final String id;
    private final String titleKey;
    private final int order;

    public ContentCatalogPage(
            Kind kind,
            String id,
            String titleKey,
            int order) {

        this.kind = kind;
        this.id = id;
        this.titleKey = titleKey;
        this.order = order;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String sectionId() {
        return MdtIntroductionContent
                .SECTION_CONTENT;
    }

    @Override
    public String titleKey() {
        return titleKey;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public void build(
            IntroductionContext context,
            Table root) {

        List<UnlockableContent> contents =
                collectContents();

        root.top().left();

        if (contents.isEmpty()) {
            root.add(
                    Core.bundle.get(
                            "mdtnh.intro.content.empty"
                    )
            ).width(DETAIL_WIDTH)
                    .pad(20f)
                    .left();

            return;
        }

        int pageCount = Math.max(
                1,
                (contents.size() +
                        CONTENTS_PER_PAGE - 1) /
                        CONTENTS_PER_PAGE
        );

        int page = Math.max(
                0,
                Math.min(
                        context.getInt(
                                "catalog.page",
                                0
                        ),
                        pageCount - 1
                )
        );

        UnlockableContent selected =
                findSelected(
                        contents,
                        context.getString(
                                "catalog.selected",
                                null
                        )
                );

        if (selected == null) {
            int firstIndex =
                    Math.min(
                            page *
                                    CONTENTS_PER_PAGE,
                            contents.size() - 1
                    );

            selected =
                    contents.get(firstIndex);
        }

        buildDetail(
                context,
                root,
                selected
        );

        root.row();

        root.add(
                Core.bundle.get(
                        "mdtnh.intro.content.choose"
                )
        ).left()
                .padLeft(8f)
                .padTop(10f)
                .padBottom(5f);

        root.row();

        Table grid = new Table();
        grid.left();

        int start =
                page *
                        CONTENTS_PER_PAGE;

        int end = Math.min(
                start + CONTENTS_PER_PAGE,
                contents.size()
        );

        int index = 0;

        for (int i = start;
             i < end;
             i++) {

            UnlockableContent content =
                    contents.get(i);

            if (index > 0 &&
                    index % GRID_COLUMNS == 0) {
                grid.row();
            }

            final UnlockableContent target =
                    content;

            ImageButton button =
                    grid.button(
                            new TextureRegionDrawable(
                                    content.uiIcon
                            ),
                            Styles.clearTogglei,
                            () -> {
                                context.putState(
                                        "catalog.selected",
                                        target.name
                                );

                                context.rebuild();
                            }
                    ).size(CONTENT_BUTTON_SIZE)
                            .get();

            button.resizeImage(40f);
            button.setChecked(
                    content == selected
            );

            Vars.ui.addDescTooltip(
                    button,
                    content.localizedName
            );

            index++;
        }

        int rest =
                index % GRID_COLUMNS;

        if (rest != 0) {
            for (int i = rest;
                 i < GRID_COLUMNS;
                 i++) {

                grid.add()
                        .size(
                                CONTENT_BUTTON_SIZE
                        );
            }
        }

        root.add(grid)
                .left()
                .pad(8f);

        root.row();

        final int currentPage = page;

        root.table(nav -> {
            nav.button(
                    Core.bundle.get(
                            "mdtnh.intro.content.previous"
                    ),
                    Icon.left,
                    Styles.flatt,
                    () -> {
                        if (currentPage <= 0) {
                            return;
                        }

                        context.putState(
                                "catalog.page",
                                currentPage - 1
                        );

                        context.putState(
                                "catalog.selected",
                                null
                        );

                        context.rebuild();
                    }
            ).width(NAV_BUTTON_WIDTH)
                    .height(NAV_BUTTON_HEIGHT)
                    .disabled(button ->
                            currentPage <= 0);

            nav.add(
                    Core.bundle.format(
                            "mdtnh.intro.content.page-counter",
                            currentPage + 1,
                            pageCount
                    )
            ).width(150f)
                    .center();

            nav.button(
                    Core.bundle.get(
                            "mdtnh.intro.content.next"
                    ),
                    Icon.right,
                    Styles.flatt,
                    () -> {
                        if (currentPage + 1 >=
                                pageCount) {
                            return;
                        }

                        context.putState(
                                "catalog.page",
                                currentPage + 1
                        );

                        context.putState(
                                "catalog.selected",
                                null
                        );

                        context.rebuild();
                    }
            ).width(NAV_BUTTON_WIDTH)
                    .height(NAV_BUTTON_HEIGHT)
                    .disabled(button ->
                            currentPage + 1 >=
                                    pageCount);
        }).left()
                .pad(8f);
    }

    private void buildDetail(
            IntroductionContext context,
            Table root,
            UnlockableContent content) {

        Table detail = new Table();
        detail.top().left();
        detail.background(Styles.black6);

        detail.table(header -> {
            header.left();

            header.image(content.uiIcon)
                    .size(64f)
                    .pad(10f);

            header.table(text -> {
                text.left();

                Label name =
                        new Label(
                                content.localizedName
                        );

                name.setWrap(true);

                text.add(name)
                        .width(620f)
                        .left();

                text.row();

                text.add(
                        Core.bundle.format(
                                "mdtnh.intro.content.internal-name",
                                content.name
                        )
                ).left()
                        .padTop(3f);
            }).left();
        }).growX()
                .left();

        addTextSection(
                detail,
                "mdtnh.intro.content.description",
                usable(content.description)
                        ? content.description
                        : Core.bundle.get(
                                "mdtnh.intro.content.no-description"
                        )
        );

        if (usable(content.details)) {
            addTextSection(
                    detail,
                    "mdtnh.intro.content.details",
                    content.details
            );
        }

        if (usable(content.credit)) {
            addTextSection(
                    detail,
                    "mdtnh.intro.content.credit",
                    content.credit
            );
        }

        if (content instanceof Item ||
                content instanceof Liquid) {

            detail.row();

            detail.button(
                    Core.bundle.get(
                            "mdtnh.intro.content.open-recipe-intro"
                    ),
                    Icon.list,
                    Styles.flatt,
                    () -> context
                            .openRecipeIntroduction(
                                    content
                            )
            ).width(280f)
                    .height(50f)
                    .left()
                    .pad(10f);
        }

        root.add(detail)
                .width(DETAIL_WIDTH)
                .left()
                .pad(8f);
    }

    private void addTextSection(
            Table detail,
            String titleKey,
            String bodyText) {

        detail.row();

        detail.add(
                Core.bundle.get(
                        titleKey
                )
        ).left()
                .padLeft(10f)
                .padTop(8f)
                .padBottom(4f);

        detail.row();

        Label body =
                new Label(bodyText);

        body.setWrap(true);

        detail.add(body)
                .width(
                        DETAIL_WIDTH - 30f
                )
                .left()
                .padLeft(10f)
                .padRight(10f)
                .padBottom(8f);
    }

    private boolean usable(String value) {
        return value != null &&
                !value.trim().isEmpty();
    }

    private UnlockableContent findSelected(
            List<UnlockableContent> contents,
            String name) {

        if (name == null) return null;

        for (UnlockableContent content :
                contents) {

            if (name.equals(content.name)) {
                return content;
            }
        }

        return null;
    }

    private List<UnlockableContent> collectContents() {
        List<UnlockableContent> result =
                new ArrayList<>();

        switch (kind) {
            case item:
                for (Item value :
                        Vars.content.items()) {
                    addIfVisible(
                            result,
                            value
                    );
                }
                break;

            case liquid:
                for (Liquid value :
                        Vars.content.liquids()) {
                    addIfVisible(
                            result,
                            value
                    );
                }
                break;

            case block:
                for (Block value :
                        Vars.content.blocks()) {
                    addIfVisible(
                            result,
                            value
                    );
                }
                break;

            case unit:
                for (UnitType value :
                        Vars.content.units()) {
                    addIfVisible(
                            result,
                            value
                    );
                }
                break;

            case status:
                for (StatusEffect value :
                        Vars.content.statusEffects()) {
                    addIfVisible(
                            result,
                            value
                    );
                }
                break;
        }

        return result;
    }

    private void addIfVisible(
            List<UnlockableContent> result,
            UnlockableContent content) {

        if (content == null ||
                content.isHidden() ||
                content.uiIcon == null) {
            return;
        }

        result.add(content);
    }
}
