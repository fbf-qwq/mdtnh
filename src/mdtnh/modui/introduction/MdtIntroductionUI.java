package mdtnh.modui.introduction;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MDT 介绍/教程中心。
 *
 * <p>界面由顶部标题、左侧一级板块、右侧正文以及右下方内容选择区组成。</p>
 */
public final class MdtIntroductionUI {

    /**
     * 底部页面按钮一屏显示几个。
     * 后续只改这个值即可调整分页密度。
     */
    public static final int PAGE_BUTTONS_PER_SCREEN = 4;

    private static final float LEFT_WIDTH = 210f;
    private static final float CONTENT_WIDTH = 830f;
    private static final float CONTENT_HEIGHT = 560f;

    private static final float SECTION_BUTTON_WIDTH = 190f;
    private static final float SECTION_BUTTON_HEIGHT = 50f;

    private static final float PAGE_BUTTON_WIDTH = 175f;
    private static final float PAGE_BUTTON_HEIGHT = 48f;
    private static final float PAGE_ARROW_SIZE = 50f;

    private static final IntroductionRegistry registry =
            new IntroductionRegistry();

    private static final MdtIntroductionUI instance =
            new MdtIntroductionUI();

    private final Deque<NavigationState> history =
            new ArrayDeque<>();

    private final Map<String, Object> pageState =
            new HashMap<>();

    private BaseDialog dialog;

    private String currentSectionId;
    private String currentPageId;
    private int pageButtonScreen;

    private boolean builtInsLoaded;

    private static final class NavigationState {

        final String sectionId;
        final String pageId;
        final int pageButtonScreen;
        final Map<String, Object> pageState;

        NavigationState(
                String sectionId,
                String pageId,
                int pageButtonScreen,
                Map<String, Object> pageState) {

            this.sectionId = sectionId;
            this.pageId = pageId;
            this.pageButtonScreen = pageButtonScreen;
            this.pageState = pageState;
        }
    }

    private MdtIntroductionUI() {
    }

    public static IntroductionRegistry registry() {
        instance.ensureBuiltIns();
        return registry;
    }

    public static void registerSection(
            String id,
            String titleKey,
            arc.scene.style.Drawable icon,
            int order) {

        instance.ensureBuiltIns();

        registry.section(
                id,
                titleKey,
                icon,
                order
        );

        instance.rebuildIfShown();
    }

    public static void registerPage(
            IntroductionPage page) {

        instance.ensureBuiltIns();
        registry.registerPage(page);
        instance.rebuildIfShown();
    }

    public static boolean unregisterPage(
            String pageId) {

        instance.ensureBuiltIns();

        boolean changed =
                registry.unregisterPage(
                        pageId
                );

        if (changed) {
            instance.rebuildIfShown();
        }

        return changed;
    }

    /**
     * 注册一个交互式产线流程图。
     */
    public static void registerFlowChart(
            FlowChartDefinition definition) {

        registerPage(
                new FlowChartIntroductionPage(
                        definition
                )
        );
    }

    public static void show() {
        instance.showInternal(
                null,
                null
        );
    }

    public static void showPage(
            String pageId) {

        instance.showInternal(
                pageId,
                null
        );
    }

    public static void showContent(
            UnlockableContent content) {

        String pageId =
                MdtIntroductionContent
                        .pageIdForContent(
                                content
                        );

        instance.showInternal(
                pageId,
                content
        );
    }

    public static boolean isShown() {
        return instance.dialog != null &&
                instance.dialog.isShown();
    }

    String currentPageId() {
        return currentPageId;
    }

    String currentSectionId() {
        return currentSectionId;
    }

    Object getPageState(String key) {
        return key == null
                ? null
                : pageState.get(key);
    }

    void putPageState(
            String key,
            Object value) {

        if (key == null) return;

        if (value == null) {
            pageState.remove(key);
        } else {
            pageState.put(
                    key,
                    value
            );
        }
    }

    private void ensureBuiltIns() {
        if (builtInsLoaded) return;

        builtInsLoaded = true;

        MdtIntroductionContent.load(
                registry
        );
    }

    private void showInternal(
            String requestedPageId,
            UnlockableContent content) {

        ensureBuiltIns();
        ensureDialog();

        history.clear();
        pageState.clear();

        if (requestedPageId != null &&
                registry.page(
                        requestedPageId
                ) != null) {

            selectPage(
                    requestedPageId,
                    false
            );
        } else {
            selectFirstAvailable();
        }

        if (content != null) {
            pageState.put(
                    "catalog.selected",
                    content.name
            );
        }

        rebuild();

        if (!dialog.isShown()) {
            dialog.show();
        }
    }

    private void ensureDialog() {
        if (dialog != null) return;

        dialog = new BaseDialog(
                Core.bundle.get(
                        "mdtnh.intro.title"
                )
        );

        dialog.keyDown(
                KeyCode.escape,
                this::backOrClose
        );

        dialog.keyDown(
                KeyCode.back,
                this::backOrClose
        );
    }

    /**
     * 按“左侧板块 + 右侧正文 + 底部内容选择”的结构重建页面。
     */
    public void rebuild() {
        if (dialog == null) return;

        ensureCurrentPage();

        if (currentPageId == null) {
            return;
        }

        dialog.cont.clear();

        Table frame = new Table();
        frame.top().left();

        buildPageHeader(frame);
        frame.row();

        frame.table(body -> {
            body.top().left();

            Table sections = new Table();
            sections.top().left();
            buildSections(sections);

            ScrollPane sectionPane =
                    new ScrollPane(
                            sections,
                            Styles.smallPane
                    );

            sectionPane.setFadeScrollBars(
                    false
            );

            sectionPane.setScrollingDisabled(
                    true,
                    false
            );

            sectionPane.setOverscroll(
                    false,
                    false
            );

            body.add(sectionPane)
                    .width(LEFT_WIDTH)
                    .growY()
                    .top();

            body.table(right -> {
                right.top().left();

                Table content = new Table();
                content.top().left();

                IntroductionPage page =
                        registry.page(
                                currentPageId
                        );

                try {
                    page.build(
                            new IntroductionContext(
                                    this
                            ),
                            content
                    );
                } catch (Throwable error) {
                    Log.err(
                            "[MDTNH] Introduction page failed: @",
                            currentPageId
                    );

                    Log.err(error);

                    content.clearChildren();

                    content.add(
                            Core.bundle.get(
                                    "mdtnh.intro.page.error"
                            )
                    ).width(
                            CONTENT_WIDTH - 60f
                    ).left()
                            .pad(20f);
                }

                ScrollPane contentPane =
                        new ScrollPane(
                                content,
                                Styles.smallPane
                        );

                contentPane.setFadeScrollBars(
                        false
                );

                contentPane.setOverscroll(
                        false,
                        false
                );

                right.add(contentPane)
                        .width(CONTENT_WIDTH)
                        .height(CONTENT_HEIGHT)
                        .grow();

                right.row();

                buildPageSelector(right);

            }).width(CONTENT_WIDTH)
                    .growY()
                    .top()
                    .padLeft(8f);

        }).grow();

        dialog.cont.add(frame)
                .grow()
                .maxWidth(
                        LEFT_WIDTH +
                                CONTENT_WIDTH +
                                24f
                )
                .maxHeight(
                        CONTENT_HEIGHT +
                                150f
                )
                .pad(6f);
    }

    private void buildPageHeader(
            Table frame) {

        frame.table(header -> {
            header.left();

            header.button(
                    Icon.left,
                    Styles.clearNonei,
                    this::navigateBack
            ).size(48f)
                    .disabled(button ->
                            history.isEmpty())
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.intro.back"
                            )
                    );

            IntroductionPage page =
                    registry.page(
                            currentPageId
                    );

            Label title =
                    new Label(
                            Core.bundle.get(
                                    page.titleKey()
                            )
                    );

            title.setWrap(true);

            header.add(title)
                    .width(
                            LEFT_WIDTH +
                                    CONTENT_WIDTH -
                                    140f
                    )
                    .left()
                    .padLeft(8f)
                    .padRight(8f);

            header.button(
                    Icon.cancel,
                    Styles.clearNonei,
                    this::close
            ).size(48f)
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.intro.close"
                            )
                    );

        }).growX()
                .height(54f)
                .padBottom(4f);
    }

    private void buildSections(
            Table table) {

        List<IntroductionSection> sections =
                visibleSections();

        for (IntroductionSection section :
                sections) {

            table.button(
                    button -> {
                        button.left();

                        if (section.icon != null) {
                            button.image(
                                    section.icon
                            ).size(28f)
                                    .padRight(7f);
                        }

                        Label label =
                                new Label(
                                        Core.bundle.get(
                                                section.titleKey
                                        )
                                );

                        label.setEllipsis(
                                true
                        );

                        button.add(label)
                                .width(
                                        SECTION_BUTTON_WIDTH -
                                                45f
                                )
                                .left();
                    },
                    Styles.flatTogglet,
                    () -> switchSection(
                            section.id
                    )
            ).checked(
                    section.id.equals(
                            currentSectionId
                    )
            ).width(SECTION_BUTTON_WIDTH)
                    .height(SECTION_BUTTON_HEIGHT)
                    .padBottom(4f);

            table.row();
        }
    }

    private void buildPageSelector(
            Table right) {

        List<IntroductionPage> pages =
                registry.pagesForSection(
                        currentSectionId
                );

        if (pages.isEmpty()) return;

        int screens = Math.max(
                1,
                (pages.size() +
                        PAGE_BUTTONS_PER_SCREEN - 1) /
                        PAGE_BUTTONS_PER_SCREEN
        );

        pageButtonScreen =
                Math.max(
                        0,
                        Math.min(
                                pageButtonScreen,
                                screens - 1
                        )
                );

        int start =
                pageButtonScreen *
                        PAGE_BUTTONS_PER_SCREEN;

        int end = Math.min(
                start +
                        PAGE_BUTTONS_PER_SCREEN,
                pages.size()
        );

        final int screenCount = screens;

        right.table(selector -> {
            selector.left();

            selector.button(
                    Icon.left,
                    Styles.clearNonei,
                    () -> {
                        if (pageButtonScreen <= 0) {
                            return;
                        }

                        pageButtonScreen--;
                        rebuild();
                    }
            ).size(PAGE_ARROW_SIZE)
                    .disabled(button ->
                            pageButtonScreen <= 0)
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.intro.page-selector.previous"
                            )
                    );

            Table tabs = new Table();
            tabs.left();

            for (int i = start;
                 i < end;
                 i++) {

                IntroductionPage page =
                        pages.get(i);

                tabs.button(
                        button -> {
                            button.left();

                            if (page.icon() != null) {
                                button.image(
                                        page.icon()
                                ).size(26f)
                                        .padRight(6f);
                            }

                            Label label =
                                    new Label(
                                            Core.bundle.get(
                                                    page.titleKey()
                                            )
                                    );

                            label.setEllipsis(
                                    true
                            );

                            button.add(label)
                                    .width(
                                            PAGE_BUTTON_WIDTH -
                                                    35f
                                    )
                                    .center();
                        },
                        Styles.flatTogglet,
                        () -> switchPage(
                                page.id()
                        )
                ).checked(
                        page.id().equals(
                                currentPageId
                        )
                ).width(PAGE_BUTTON_WIDTH)
                        .height(PAGE_BUTTON_HEIGHT)
                        .padLeft(2f)
                        .padRight(2f);
            }

            selector.add(tabs)
                    .width(
                            PAGE_BUTTON_WIDTH *
                                    PAGE_BUTTONS_PER_SCREEN
                    )
                    .height(
                            PAGE_BUTTON_HEIGHT +
                                    4f
                    )
                    .left();

            selector.button(
                    Icon.right,
                    Styles.clearNonei,
                    () -> {
                        if (pageButtonScreen + 1 >=
                                screenCount) {
                            return;
                        }

                        pageButtonScreen++;
                        rebuild();
                    }
            ).size(PAGE_ARROW_SIZE)
                    .disabled(button ->
                            pageButtonScreen + 1 >=
                                    screenCount)
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.intro.page-selector.next"
                            )
                    );

        }).width(CONTENT_WIDTH)
                .height(
                        PAGE_BUTTON_HEIGHT +
                                8f
                )
                .left()
                .padTop(6f);

        if (screens > 1) {
            right.row();

            right.add(
                    Core.bundle.format(
                            "mdtnh.intro.page-selector.counter",
                            pageButtonScreen + 1,
                            screens
                    )
            ).center()
                    .padTop(2f);
        }
    }

    private List<IntroductionSection> visibleSections() {
        List<IntroductionSection> result =
                new ArrayList<>();

        for (IntroductionSection section :
                registry.sections()) {

            if (!registry
                    .pagesForSection(
                            section.id
                    )
                    .isEmpty()) {

                result.add(section);
            }
        }

        return result;
    }

    private void switchSection(
            String sectionId) {

        if (sectionId == null ||
                sectionId.equals(
                        currentSectionId
                )) {
            return;
        }

        List<IntroductionPage> pages =
                registry.pagesForSection(
                        sectionId
                );

        if (pages.isEmpty()) return;

        currentSectionId = sectionId;
        currentPageId =
                pages.get(0).id();

        pageButtonScreen = 0;
        pageState.clear();

        rebuild();
    }

    private void switchPage(
            String pageId) {

        if (pageId == null ||
                pageId.equals(
                        currentPageId
                )) {
            return;
        }

        selectPage(
                pageId,
                false
        );

        rebuild();
    }

    private void selectPage(
            String pageId,
            boolean preserveState) {

        IntroductionPage page =
                registry.page(pageId);

        if (page == null ||
                !page.visible()) {
            return;
        }

        currentPageId = page.id();
        currentSectionId =
                page.sectionId();

        List<IntroductionPage> pages =
                registry.pagesForSection(
                        currentSectionId
                );

        pageButtonScreen =
                screenForPage(
                        pages,
                        currentPageId
                );

        if (!preserveState) {
            pageState.clear();
        }
    }

    private int screenForPage(
            List<IntroductionPage> pages,
            String pageId) {

        for (int i = 0;
             i < pages.size();
             i++) {

            if (pages.get(i)
                    .id()
                    .equals(pageId)) {

                return i /
                        PAGE_BUTTONS_PER_SCREEN;
            }
        }

        return 0;
    }

    private void selectFirstAvailable() {
        List<IntroductionSection> sections =
                visibleSections();

        if (sections.isEmpty()) {
            currentSectionId = null;
            currentPageId = null;
            return;
        }

        IntroductionSection section =
                sections.get(0);

        List<IntroductionPage> pages =
                registry.pagesForSection(
                        section.id
                );

        currentSectionId =
                section.id;

        currentPageId =
                pages.get(0).id();

        pageButtonScreen = 0;
    }

    private void ensureCurrentPage() {
        IntroductionPage page =
                currentPageId == null
                        ? null
                        : registry.page(
                                currentPageId
                        );

        if (page != null &&
                page.visible()) {
            return;
        }

        selectFirstAvailable();
    }

    void navigateToPage(
            String pageId) {

        IntroductionPage page =
                registry.page(pageId);

        if (page == null ||
                !page.visible()) {
            return;
        }

        history.push(
                snapshot()
        );

        selectPage(
                pageId,
                false
        );

        rebuild();
    }

    void navigateToContent(
            UnlockableContent content) {

        if (content == null) return;

        String pageId =
                MdtIntroductionContent
                        .pageIdForContent(
                                content
                        );

        if (pageId == null) return;

        history.push(
                snapshot()
        );

        selectPage(
                pageId,
                false
        );

        pageState.put(
                "catalog.selected",
                content.name
        );

        rebuild();
    }

    private NavigationState snapshot() {
        return new NavigationState(
                currentSectionId,
                currentPageId,
                pageButtonScreen,
                new HashMap<>(
                        pageState
                )
        );
    }

    private void navigateBack() {
        if (history.isEmpty()) return;

        NavigationState state =
                history.pop();

        currentSectionId =
                state.sectionId;

        currentPageId =
                state.pageId;

        pageButtonScreen =
                state.pageButtonScreen;

        pageState.clear();

        pageState.putAll(
                state.pageState
        );

        rebuild();
    }

    private void backOrClose() {
        if (history.isEmpty()) {
            close();
        } else {
            navigateBack();
        }
    }

    private void close() {
        history.clear();
        pageState.clear();

        if (dialog != null) {
            dialog.hide();
        }
    }

    private void rebuildIfShown() {
        if (dialog != null &&
                dialog.isShown()) {

            rebuild();
        }
    }
}
