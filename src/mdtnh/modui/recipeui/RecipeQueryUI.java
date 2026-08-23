package mdtnh.modui.recipeui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Strings;
import mdtnh.RecipeCrafter;
import mdtnh.VoltageRecipeRegistry;
import mdtnh.VoltageTier;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MDTNH 配方查询界面。
 *
 * <p>R 查询制造配方，U 查询用途。查询目标支持 Item 和 Liquid。</p>
 *
 * <p>分类按钮按固定数量分页显示；在配方页中的任意物品/流体上再次按 R/U
 * 会递归进入该内容的查询页，并把当前页面保存到历史栈。</p>
 */
public final class RecipeQueryUI {

    public static final KeyBind recipeQueryKey =
            KeyBind.add("mdtnh_recipe_query", KeyCode.r, "mdtnh");

    public static final KeyBind usageQueryKey =
            KeyBind.add("mdtnh_usage_query", KeyCode.u, "mdtnh");

    /** 内置页面 ID；扩展页面不得占用。 */
    public static final String PAGE_RECIPE = "recipe";
    public static final String PAGE_USAGE = "usage";

    /*
     * ==============================
     * UI 尺寸集中配置
     * ==============================
     *
     * CATEGORY_BUTTONS_PER_PAGE 就是一屏显示几个分类按钮。
     * 后续只改这一个值即可改变分类分页密度。
     */
    private static final int CATEGORY_BUTTONS_PER_PAGE = 4;

    /**
     * 顶部“页面种类”一屏显示几个。
     * 配方 / 用途 / 物品介绍 / 第三方扩展页都使用这一分页。
     */
    private static final int PAGE_TYPE_BUTTONS_PER_PAGE = 4;

    private static final float DIALOG_CONTENT_WIDTH = 900f;

    private static final float PAGE_TYPE_BUTTON_WIDTH = 185f;
    private static final float PAGE_TYPE_BUTTON_HEIGHT = 46f;
    private static final float PAGE_TYPE_ARROW_SIZE = 54f;
    private static final float CATEGORY_BUTTON_WIDTH = 185f;
    private static final float CATEGORY_BUTTON_HEIGHT = 46f;
    private static final float CATEGORY_ARROW_SIZE = 54f;

    private static final float SUBPAGE_BUTTON_WIDTH = 180f;
    private static final float SUBPAGE_BUTTON_HEIGHT = 46f;

    private static final float DIALOG_ACTION_BUTTON_WIDTH = 220f;
    private static final float DIALOG_ACTION_BUTTON_HEIGHT = 56f;

    private static final float IO_COLUMN_WIDTH = 395f;
    private static final float IO_ENTRY_MIN_HEIGHT = 46f;

    /** 第三方/本模组注册的自定义页面类型。 */
    private static final List<RecipeQueryPage> customPages = new ArrayList<>();

    private static final RecipeQueryUI instance = new RecipeQueryUI();

    /** 当前由 UI 声明为鼠标悬浮的物品或流体。 */
    private static UnlockableContent hoveredContent;

    /** 声明 hoveredContent 的 UI 元素。 */
    private static Element hoverOwner;

    private BaseDialog dialog;
    private boolean installed;

    private UnlockableContent targetContent;

    /** 当前顶部页面。内置值为 PAGE_RECIPE / PAGE_USAGE，也可以是扩展页 ID。 */
    private String selectedPageId = PAGE_RECIPE;

    /** 仅在配方/用途内置页面中使用。 */
    private QueryMode queryMode = QueryMode.recipe;

    private VoltageRecipeRegistry selectedRegistry;
    private int selectedRecipeIndex;

    /** 当前配方分类按钮处于第几屏，从 0 开始。 */
    private int categoryPageIndex;

    /** 当前顶部页面种类按钮处于第几屏，从 0 开始。 */
    private int pageTypePageIndex;

    /**
     * 当前扩展页的轻量状态。
     * 扩展页可通过 PageContext 保存 Integer/String/Boolean 等简单值，
     * 历史回溯时会连同这些值一起恢复。
     */
    private final Map<String, Object> customPageState = new HashMap<>();

    /** 递归查询历史。 */
    private final Deque<NavigationState> history = new ArrayDeque<>();

    private enum QueryMode {
        recipe,
        usage
    }

    private static class CategoryPage {
        final VoltageRecipeRegistry registry;
        final List<VoltageRecipeRegistry.RegisteredRecipe> recipes = new ArrayList<>();

        CategoryPage(VoltageRecipeRegistry registry) {
            this.registry = registry;
        }
    }

    private static class NavigationState {
        final UnlockableContent targetContent;
        final String selectedPageId;
        final QueryMode queryMode;
        final VoltageRecipeRegistry selectedRegistry;
        final int selectedRecipeIndex;
        final int categoryPageIndex;
        final int pageTypePageIndex;
        final Map<String, Object> customPageState;

        NavigationState(UnlockableContent targetContent,
                        String selectedPageId,
                        QueryMode queryMode,
                        VoltageRecipeRegistry selectedRegistry,
                        int selectedRecipeIndex,
                        int categoryPageIndex,
                        int pageTypePageIndex,
                        Map<String, Object> customPageState) {
            this.targetContent = targetContent;
            this.selectedPageId = selectedPageId;
            this.queryMode = queryMode;
            this.selectedRegistry = selectedRegistry;
            this.selectedRecipeIndex = selectedRecipeIndex;
            this.categoryPageIndex = categoryPageIndex;
            this.pageTypePageIndex = pageTypePageIndex;
            this.customPageState = customPageState;
        }
    }

    /** 顶部页面按钮的统一描述；内置页面和扩展页面都会转换成它。 */
    private static class PageEntry {
        final String id;
        final RecipeQueryPage extension;

        PageEntry(String id, RecipeQueryPage extension) {
            this.id = id;
            this.extension = extension;
        }

        boolean isBuiltIn() {
            return extension == null;
        }
    }

    /**
     * 扩展页面在 build(...) 时拿到的上下文。
     *
     * <p>扩展页不需要访问 RecipeQueryUI 私有字段。需要重新绘制、递归进入
     * 其他内容、绑定 R/U 悬浮或保存页面状态时，都通过这里完成。</p>
     */
    public static final class PageContext {
        private final RecipeQueryUI owner;

        private PageContext(RecipeQueryUI owner) {
            this.owner = owner;
        }

        public UnlockableContent content() {
            return owner.targetContent;
        }

        public String pageId() {
            return owner.selectedPageId;
        }

        /** 请求在下一帧重新构建当前查询页。 */
        public void rebuild() {
            Core.app.post(owner::rebuild);
        }

        /**
         * 保存扩展页状态。建议只保存不可变或简单对象。
         * value == null 时删除该键。
         */
        public void putState(String key, Object value) {
            if (key == null) return;

            if (value == null) {
                owner.customPageState.remove(key);
            } else {
                owner.customPageState.put(key, value);
            }
        }

        public Object getState(String key) {
            return key == null ? null : owner.customPageState.get(key);
        }

        public int getInt(String key, int fallback) {
            Object value = getState(key);
            return value instanceof Number
                    ? ((Number) value).intValue()
                    : fallback;
        }

        public boolean getBoolean(String key, boolean fallback) {
            Object value = getState(key);
            return value instanceof Boolean
                    ? (Boolean) value
                    : fallback;
        }

        public String getString(String key, String fallback) {
            Object value = getState(key);
            return value instanceof String
                    ? (String) value
                    : fallback;
        }

        /** 让某个元素上的 Item/Liquid 支持 R/U 递归查询。 */
        public void bindHover(Element element, UnlockableContent content) {
            RecipeQueryUI.bindHover(element, content);
        }

        /** 递归进入另一个内容的制造配方页，并写入历史栈。 */
        public void openRecipe(UnlockableContent content) {
            owner.navigateTo(content, PAGE_RECIPE);
        }

        /** 递归进入另一个内容的用途页，并写入历史栈。 */
        public void openUsage(UnlockableContent content) {
            owner.navigateTo(content, PAGE_USAGE);
        }

        /**
         * 递归进入另一个内容的指定扩展页，并写入历史栈。
         * 如果该页面不支持目标内容，则不会跳转。
         */
        public void openPage(UnlockableContent content, String pageId) {
            owner.navigateTo(content, pageId);
        }

        public String text(String key) {
            return Core.bundle.get(key);
        }

        public String format(String key, Object... args) {
            return Core.bundle.format(key, args);
        }
    }

    private RecipeQueryUI() {
        // “物品介绍”本身也通过公开扩展接口注册，用作可直接参考的示例。
        registerPage(new ItemIntroductionPage());
    }

    public static void registerKeybind() {
    }

    public static void install() {
        instance.installInternal();
    }

    /**
     * 注册一个自定义页面种类。
     *
     * <p>相同 ID 再次注册会替换旧实现，方便模组热重载/覆盖默认扩展。
     * PAGE_RECIPE 与 PAGE_USAGE 为保留 ID，不能注册。</p>
     */
    public static synchronized void registerPage(RecipeQueryPage page) {
        if (page == null ||
                page.id() == null ||
                page.id().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid recipe query page.");
        }

        if (PAGE_RECIPE.equals(page.id()) ||
                PAGE_USAGE.equals(page.id())) {
            throw new IllegalArgumentException("Reserved recipe query page id.");
        }

        customPages.removeIf(existing ->
                existing.id().equals(page.id()));
        customPages.add(page);
        customPages.sort(
                Comparator.comparingInt(RecipeQueryPage::order)
        );
    }

    /** 注销一个扩展页面。内置配方/用途页面不能注销。 */
    public static synchronized boolean unregisterPage(String pageId) {
        if (pageId == null) return false;

        return customPages.removeIf(page ->
                page.id().equals(pageId));
    }

    /** 返回当前扩展页面注册表的只读快照。 */
    public static synchronized List<RecipeQueryPage> registeredPages() {
        return Collections.unmodifiableList(
                new ArrayList<>(customPages)
        );
    }

    /** 从外部作为新的根查询打开。 */
    public static void showRecipe(UnlockableContent content) {
        instance.showRoot(content, QueryMode.recipe);
    }

    /** 从外部作为新的根查询打开。 */
    public static void showUsage(UnlockableContent content) {
        instance.showRoot(content, QueryMode.usage);
    }

    /**
     * 从外部直接打开某个扩展页。
     * pageId 可以是 PAGE_RECIPE / PAGE_USAGE，也可以是已注册扩展页 ID。
     */
    public static void showPage(UnlockableContent content, String pageId) {
        instance.showRoot(content, pageId);
    }

    /**
     * 把一个 UI 元素声明为“代表某个可查询内容”。
     *
     * <p>当前只接受 Item / Liquid；这样同一套 hover 逻辑可以同时用于背包物品
     * 和配方页中的流体。</p>
     */
    public static void bindHover(Element element, UnlockableContent content) {
        if (element == null || !isQueryable(content)) return;

        element.update(() -> {
            if (Core.scene == null) return;

            Element hover = Core.scene.getHoverElement();
            boolean inside = hover == element ||
                    (hover != null && hover.isDescendantOf(element));

            if (inside) {
                hoverOwner = element;
                hoveredContent = content;
            } else if (hoverOwner == element) {
                hoverOwner = null;
                hoveredContent = null;
            }
        });
    }

    private static boolean isQueryable(UnlockableContent content) {
        return content instanceof Item || content instanceof Liquid;
    }

    private void installInternal() {
        if (installed) return;

        installed = true;
        Events.run(Trigger.update, this::updateHotkeys);
        Log.info("[MDTNH] RecipeQueryUI hotkey listener installed.");
    }

    private void updateHotkeys() {
        if (!installed ||
                Core.input == null ||
                Core.scene == null ||
                Vars.state == null ||
                Vars.state.isMenu()) {
            return;
        }

        UnlockableContent content = currentHoveredContent();
        if (content == null) return;

        boolean recipeTap = Core.input.keyTap(recipeQueryKey);
        boolean usageTap = Core.input.keyTap(usageQueryKey);
        if (!recipeTap && !usageTap) return;

        QueryMode requestedMode =
                recipeTap ? QueryMode.recipe : QueryMode.usage;

        /*
         * 查询 Dialog 正在显示时，说明 R/U 来自配方查询页内部：
         * 先保存当前页面，再递归进入新目标。
         *
         * Dialog 未显示时，则把它视为来自背包/快捷栏的新根查询。
         */
        navigateTo(
                content,
                requestedMode == QueryMode.recipe
                        ? PAGE_RECIPE
                        : PAGE_USAGE
        );
    }

    private static UnlockableContent currentHoveredContent() {
        if (Core.scene == null ||
                hoverOwner == null ||
                hoveredContent == null) {
            return null;
        }

        Element hover = Core.scene.getHoverElement();
        boolean inside = hover == hoverOwner ||
                (hover != null && hover.isDescendantOf(hoverOwner));

        return inside ? hoveredContent : null;
    }

    private void showRoot(UnlockableContent content, QueryMode mode) {
        if (!isQueryable(content)) return;

        history.clear();
        open(content, mode);
    }

    private void showRoot(UnlockableContent content, String pageId) {
        if (!isQueryable(content)) return;

        history.clear();
        open(content, pageId);
    }

    private void open(UnlockableContent content, QueryMode mode) {
        open(
                content,
                mode == QueryMode.recipe
                        ? PAGE_RECIPE
                        : PAGE_USAGE
        );
    }

    private void open(UnlockableContent content, String pageId) {
        if (!isQueryable(content)) return;

        PageEntry entry = findAvailablePage(content, pageId);
        if (entry == null) return;

        targetContent = content;
        selectedPageId = entry.id;

        if (PAGE_RECIPE.equals(selectedPageId)) {
            queryMode = QueryMode.recipe;
        } else if (PAGE_USAGE.equals(selectedPageId)) {
            queryMode = QueryMode.usage;
        }

        selectedRegistry = null;
        selectedRecipeIndex = 0;
        categoryPageIndex = 0;
        customPageState.clear();

        pageTypePageIndex = pageIndexForSelection(
                availablePages(content),
                selectedPageId,
                PAGE_TYPE_BUTTONS_PER_PAGE
        );

        ensureDialog();
        rebuild();

        if (!dialog.isShown()) {
            dialog.show();
        }
    }

    /**
     * 从当前查询页递归进入新页面；会保存完整导航状态。
     */
    private void navigateTo(UnlockableContent content, String pageId) {
        if (!isQueryable(content)) return;
        if (findAvailablePage(content, pageId) == null) return;

        if (dialog != null &&
                dialog.isShown() &&
                targetContent != null) {
            history.push(snapshot());
        } else {
            history.clear();
        }

        open(content, pageId);
    }

    private void ensureDialog() {
        if (dialog != null) return;

        dialog = new BaseDialog(
                Core.bundle.get("mdtnh.recipe-query.title")
        );

        /*
         * 不使用 addCloseButton()：
         * 默认 close button 会直接 hide，无法执行历史回溯。
         */
        dialog.buttons.defaults()
                .height(DIALOG_ACTION_BUTTON_HEIGHT);

        dialog.buttons.button(
                Core.bundle.get("mdtnh.recipe-query.back"),
                Icon.left,
                this::navigateBack
        ).width(DIALOG_ACTION_BUTTON_WIDTH)
                .disabled(button -> history.isEmpty());

        dialog.buttons.button(
                Core.bundle.get("mdtnh.recipe-query.return-inventory"),
                Icon.list,
                this::returnToInventory
        ).width(DIALOG_ACTION_BUTTON_WIDTH)
                .padLeft(8f);

        /*
         * 键盘 Esc / Android Back 与“返回”按钮走同一回溯逻辑。
         * 历史为空时则退出查询层，回到原先的背包/物品界面。
         */
        dialog.keyDown(KeyCode.escape, this::navigateBackOrInventory);
        dialog.keyDown(KeyCode.back, this::navigateBackOrInventory);

        dialog.hidden(() -> {
            hoverOwner = null;
            hoveredContent = null;
        });
    }

    private NavigationState snapshot() {
        return new NavigationState(
                targetContent,
                selectedPageId,
                queryMode,
                selectedRegistry,
                selectedRecipeIndex,
                categoryPageIndex,
                pageTypePageIndex,
                new HashMap<>(customPageState)
        );
    }

    private void navigateBack() {
        if (history.isEmpty()) return;

        NavigationState state = history.pop();
        targetContent = state.targetContent;
        selectedPageId = state.selectedPageId;
        queryMode = state.queryMode;
        selectedRegistry = state.selectedRegistry;
        selectedRecipeIndex = state.selectedRecipeIndex;
        categoryPageIndex = state.categoryPageIndex;
        pageTypePageIndex = state.pageTypePageIndex;

        customPageState.clear();
        customPageState.putAll(state.customPageState);

        rebuild();
    }

    private void navigateBackOrInventory() {
        if (history.isEmpty()) {
            returnToInventory();
        } else {
            navigateBack();
        }
    }

    /**
     * 一键关闭查询层。
     *
     * <p>查询 Dialog 打开时不会主动关闭原背包/物品 Dialog，
     * 因此 hide 后会直接露出原界面。</p>
     */
    private void returnToInventory() {
        history.clear();
        targetContent = null;
        selectedRegistry = null;
        selectedRecipeIndex = 0;
        categoryPageIndex = 0;
        pageTypePageIndex = 0;
        customPageState.clear();

        if (dialog != null) {
            dialog.hide();
        }
    }

    private void rebuild() {
        if (dialog == null || targetContent == null) return;

        dialog.cont.clear();

        buildHeader(dialog.cont);
        dialog.cont.row();

        List<PageEntry> pageEntries = availablePages(targetContent);
        if (findPageEntry(pageEntries, selectedPageId) == null) {
            selectedPageId = PAGE_RECIPE;
            queryMode = QueryMode.recipe;
            customPageState.clear();
        }

        clampPageTypePage(pageEntries);
        buildPageTypeButtons(dialog.cont, pageEntries);
        dialog.cont.row();

        if (PAGE_RECIPE.equals(selectedPageId) ||
                PAGE_USAGE.equals(selectedPageId)) {
            buildRecipeUsageBody(dialog.cont);
        } else {
            buildExtensionBody(dialog.cont);
        }
    }

    private void buildRecipeUsageBody(Table root) {
        List<CategoryPage> pages = collectPages();

        if (pages.isEmpty()) {
            selectedRegistry = null;
            selectedRecipeIndex = 0;
            categoryPageIndex = 0;
            buildEmptyState(root);
            return;
        }

        CategoryPage selectedPage = resolveSelectedPage(pages);
        clampCategoryPage(pages);

        buildCategoryTabs(root, pages, selectedPage);
        root.row();

        selectedRecipeIndex = Math.max(
                0,
                Math.min(selectedRecipeIndex, selectedPage.recipes.size() - 1)
        );

        VoltageRecipeRegistry.RegisteredRecipe recipe =
                selectedPage.recipes.get(selectedRecipeIndex);

        Table detail = new Table();
        detail.top().left();
        buildRecipePage(detail, recipe);

        addDetailPane(root, detail);

        root.row();
        buildSubPageNavigation(root, selectedPage);
    }

    private void buildExtensionBody(Table root) {
        RecipeQueryPage page = findCustomPage(selectedPageId);

        if (page == null || !page.supports(targetContent)) {
            selectedPageId = PAGE_RECIPE;
            queryMode = QueryMode.recipe;
            customPageState.clear();
            buildRecipeUsageBody(root);
            return;
        }

        Table detail = new Table();
        detail.top().left();

        try {
            page.build(new PageContext(this), detail);
        } catch (Throwable error) {
            Log.err(
                    "[MDTNH] Recipe query extension page failed: @",
                    page.id()
            );
            Log.err(error);

            detail.clear();
            detail.add(
                    Core.bundle.get(
                            "mdtnh.recipe-query.extension.error"
                    )
            ).width(DIALOG_CONTENT_WIDTH - 80f)
                    .pad(30f)
                    .left();
        }

        addDetailPane(root, detail);
    }

    private void addDetailPane(Table root, Table detail) {
        ScrollPane pane = new ScrollPane(detail, Styles.smallPane);
        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(true, false);
        pane.setOverscroll(false, false);

        root.add(pane)
                .width(DIALOG_CONTENT_WIDTH)
                .maxHeight(610f)
                .growY()
                .padTop(6f);
    }

    private void buildHeader(Table root) {
        root.table(header -> {
            header.left();
            header.image(targetContent.uiIcon)
                    .size(52f)
                    .padRight(10f);

            header.table(text -> {
                text.left();

                Label name = new Label(targetContent.localizedName);
                name.setColor(Color.white);
                name.setWrap(true);

                text.add(name)
                        .width(DIALOG_CONTENT_WIDTH - 90f)
                        .left();

                text.row();

                text.add(
                        Core.bundle.format(
                                "mdtnh.recipe-query.internal-name",
                                targetContent.name
                        )
                ).left();
            }).left();
        }).left()
                .growX()
                .pad(8f);
    }

    private void buildPageTypeButtons(
            Table root,
            List<PageEntry> entries) {

        int totalScreens = pageCount(
                entries.size(),
                PAGE_TYPE_BUTTONS_PER_PAGE
        );

        int start = pageTypePageIndex *
                PAGE_TYPE_BUTTONS_PER_PAGE;

        int end = Math.min(
                start + PAGE_TYPE_BUTTONS_PER_PAGE,
                entries.size()
        );

        root.table(nav -> {
            nav.left();

            nav.button(
                    Icon.left,
                    Styles.clearNonei,
                    () -> {
                        if (pageTypePageIndex <= 0) return;
                        pageTypePageIndex--;
                        Core.app.post(this::rebuild);
                    }
            ).size(PAGE_TYPE_ARROW_SIZE)
                    .disabled(button -> pageTypePageIndex <= 0)
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.recipe-query.page-type.previous-screen"
                            )
                    );

            Table tabs = new Table();
            tabs.left();

            for (int i = start; i < end; i++) {
                PageEntry entry = entries.get(i);
                String text = pageButtonText(entry);

                tabs.button(
                        button -> {
                            Label label = new Label(text);
                            label.setEllipsis(true);
                            label.setAlignment(Align.center);

                            button.add(label)
                                    .width(PAGE_TYPE_BUTTON_WIDTH - 18f)
                                    .center();
                        },
                        Styles.flatTogglet,
                        () -> switchPage(entry.id)
                ).checked(entry.id.equals(selectedPageId))
                        .width(PAGE_TYPE_BUTTON_WIDTH)
                        .height(PAGE_TYPE_BUTTON_HEIGHT)
                        .padLeft(3f)
                        .padRight(3f);
            }

            nav.add(tabs)
                    .width(
                            PAGE_TYPE_BUTTON_WIDTH *
                                    PAGE_TYPE_BUTTONS_PER_PAGE
                    )
                    .height(PAGE_TYPE_BUTTON_HEIGHT + 4f)
                    .left();

            nav.button(
                    Icon.right,
                    Styles.clearNonei,
                    () -> {
                        if (pageTypePageIndex + 1 >=
                                totalScreens) {
                            return;
                        }

                        pageTypePageIndex++;
                        Core.app.post(this::rebuild);
                    }
            ).size(PAGE_TYPE_ARROW_SIZE)
                    .disabled(
                            button ->
                                    pageTypePageIndex + 1 >=
                                            totalScreens
                    )
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.recipe-query.page-type.next-screen"
                            )
                    );
        }).width(DIALOG_CONTENT_WIDTH)
                .height(PAGE_TYPE_BUTTON_HEIGHT + 8f);

        if (totalScreens > 1) {
            root.row();
            root.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.page-type.screen-counter",
                            pageTypePageIndex + 1,
                            totalScreens
                    )
            ).center()
                    .padTop(2f)
                    .padBottom(2f);
        }
    }

    private String pageButtonText(PageEntry entry) {
        if (PAGE_RECIPE.equals(entry.id)) {
            return Core.bundle.format(
                    "mdtnh.recipe-query.mode.recipe",
                    keyName(recipeQueryKey)
            );
        }

        if (PAGE_USAGE.equals(entry.id)) {
            return Core.bundle.format(
                    "mdtnh.recipe-query.mode.usage",
                    keyName(usageQueryKey)
            );
        }

        return Core.bundle.get(entry.extension.titleKey());
    }

    private String keyName(KeyBind bind) {
        if (bind == null ||
                bind.value == null ||
                bind.value.key == null) {
            return Core.bundle.get("mdtnh.recipe-query.key.unset");
        }

        return bind.value.key.getName();
    }

    private void switchPage(String pageId) {
        if (pageId == null || pageId.equals(selectedPageId)) return;
        if (findAvailablePage(targetContent, pageId) == null) return;

        selectedPageId = pageId;

        if (PAGE_RECIPE.equals(pageId)) {
            queryMode = QueryMode.recipe;
        } else if (PAGE_USAGE.equals(pageId)) {
            queryMode = QueryMode.usage;
        }

        selectedRegistry = null;
        selectedRecipeIndex = 0;
        categoryPageIndex = 0;
        customPageState.clear();

        Core.app.post(this::rebuild);
    }

    private List<PageEntry> availablePages(UnlockableContent content) {
        List<PageEntry> result = new ArrayList<>();

        result.add(new PageEntry(PAGE_RECIPE, null));
        result.add(new PageEntry(PAGE_USAGE, null));

        List<RecipeQueryPage> snapshot;
        synchronized (RecipeQueryUI.class) {
            snapshot = new ArrayList<>(customPages);
        }

        for (RecipeQueryPage page : snapshot) {
            if (page.supports(content)) {
                result.add(new PageEntry(page.id(), page));
            }
        }

        return result;
    }

    private PageEntry findAvailablePage(
            UnlockableContent content,
            String pageId) {

        return findPageEntry(
                availablePages(content),
                pageId
        );
    }

    private PageEntry findPageEntry(
            List<PageEntry> entries,
            String pageId) {

        if (pageId == null) return null;

        for (PageEntry entry : entries) {
            if (pageId.equals(entry.id)) {
                return entry;
            }
        }

        return null;
    }

    private RecipeQueryPage findCustomPage(String pageId) {
        if (pageId == null) return null;

        synchronized (RecipeQueryUI.class) {
            for (RecipeQueryPage page : customPages) {
                if (pageId.equals(page.id())) {
                    return page;
                }
            }
        }

        return null;
    }

    private int pageCount(int itemCount, int perPage) {
        return Math.max(
                1,
                (itemCount + perPage - 1) / perPage
        );
    }

    private int pageIndexForSelection(
            List<PageEntry> entries,
            String pageId,
            int perPage) {

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id.equals(pageId)) {
                return i / perPage;
            }
        }

        return 0;
    }

    private void clampPageTypePage(List<PageEntry> entries) {
        int totalScreens = pageCount(
                entries.size(),
                PAGE_TYPE_BUTTONS_PER_PAGE
        );

        pageTypePageIndex = Math.max(
                0,
                Math.min(
                        pageTypePageIndex,
                        totalScreens - 1
                )
        );
    }

    private List<CategoryPage> collectPages() {
        List<CategoryPage> pages = new ArrayList<>();

        for (VoltageRecipeRegistry registry :
                VoltageRecipeRegistry.allRegistries()) {

            CategoryPage page = new CategoryPage(registry);

            for (VoltageRecipeRegistry.RegisteredRecipe recipe :
                    registry.registeredRecipes()) {

                boolean matches =
                        queryMode == QueryMode.recipe
                                ? recipe.produces(targetContent)
                                : recipe.consumes(targetContent);

                if (matches) {
                    page.recipes.add(recipe);
                }
            }

            if (!page.recipes.isEmpty()) {
                pages.add(page);
            }
        }

        return pages;
    }

    private CategoryPage resolveSelectedPage(List<CategoryPage> pages) {
        if (selectedRegistry != null) {
            for (CategoryPage page : pages) {
                if (page.registry == selectedRegistry) {
                    return page;
                }
            }
        }

        CategoryPage first = pages.get(0);
        selectedRegistry = first.registry;
        selectedRecipeIndex = 0;
        return first;
    }

    private void clampCategoryPage(List<CategoryPage> pages) {
        int totalScreens = categoryScreenCount(pages.size());

        categoryPageIndex = Math.max(
                0,
                Math.min(categoryPageIndex, totalScreens - 1)
        );
    }

    private int categoryScreenCount(int categoryCount) {
        return Math.max(
                1,
                (categoryCount + CATEGORY_BUTTONS_PER_PAGE - 1) /
                        CATEGORY_BUTTONS_PER_PAGE
        );
    }

    /**
     * 固定长度分类按钮 + 左右翻屏。
     *
     * <p>不使用横向 ScrollPane，避免分类数量多时按钮被无限压缩。
     * 每屏数量只由 CATEGORY_BUTTONS_PER_PAGE 控制。</p>
     */
    private void buildCategoryTabs(Table root,
                                   List<CategoryPage> pages,
                                   CategoryPage selectedPage) {

        int totalScreens = categoryScreenCount(pages.size());
        int start = categoryPageIndex * CATEGORY_BUTTONS_PER_PAGE;
        int end = Math.min(
                start + CATEGORY_BUTTONS_PER_PAGE,
                pages.size()
        );

        root.table(nav -> {
            nav.left();

            nav.button(
                    Icon.left,
                    Styles.clearNonei,
                    () -> {
                        if (categoryPageIndex <= 0) return;
                        categoryPageIndex--;
                        Core.app.post(this::rebuild);
                    }
            ).size(CATEGORY_ARROW_SIZE)
                    .disabled(button -> categoryPageIndex <= 0)
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.recipe-query.category.previous-screen"
                            )
                    );

            Table tabs = new Table();
            tabs.left();

            for (int i = start; i < end; i++) {
                CategoryPage page = pages.get(i);

                String text = Core.bundle.format(
                        "mdtnh.recipe-query.category.button",
                        categoryName(page.registry),
                        page.recipes.size()
                );

                tabs.button(
                        button -> {
                            Label label = new Label(text);
                            label.setEllipsis(true);
                            label.setAlignment(Align.center);

                            button.add(label)
                                    .width(CATEGORY_BUTTON_WIDTH - 18f)
                                    .center();
                        },
                        Styles.flatTogglet,
                        () -> {
                            selectedRegistry = page.registry;
                            selectedRecipeIndex = 0;
                            Core.app.post(this::rebuild);
                        }
                ).checked(page == selectedPage)
                        .width(CATEGORY_BUTTON_WIDTH)
                        .height(CATEGORY_BUTTON_HEIGHT)
                        .padLeft(3f)
                        .padRight(3f);
            }

            nav.add(tabs)
                    .width(
                            CATEGORY_BUTTON_WIDTH *
                                    CATEGORY_BUTTONS_PER_PAGE
                    )
                    .height(CATEGORY_BUTTON_HEIGHT + 4f)
                    .left();

            nav.button(
                    Icon.right,
                    Styles.clearNonei,
                    () -> {
                        if (categoryPageIndex + 1 >= totalScreens) return;
                        categoryPageIndex++;
                        Core.app.post(this::rebuild);
                    }
            ).size(CATEGORY_ARROW_SIZE)
                    .disabled(
                            button ->
                                    categoryPageIndex + 1 >= totalScreens
                    )
                    .tooltip(
                            Core.bundle.get(
                                    "mdtnh.recipe-query.category.next-screen"
                            )
                    );
        }).width(DIALOG_CONTENT_WIDTH)
                .height(CATEGORY_BUTTON_HEIGHT + 8f);

        if (totalScreens > 1) {
            root.row();
            root.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.category.screen-counter",
                            categoryPageIndex + 1,
                            totalScreens
                    )
            ).center()
                    .padTop(2f)
                    .padBottom(2f);
        }
    }

    /** 一个子页面：一条 register(...) 逻辑配方。 */
    private void buildRecipePage(
            Table root,
            VoltageRecipeRegistry.RegisteredRecipe indexed) {

        root.defaults().left();

        root.table(meta -> {
            meta.left();

            meta.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.meta.category",
                            categoryName(indexed.registry)
                    )
            ).left();

            meta.row();

            meta.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.meta.group",
                            groupName(indexed.groupName)
                    )
            ).left();

            meta.row();

            String voltageRange = Core.bundle.format(
                    "mdtnh.recipe-query.voltage.range",
                    format(indexed.minimumTier.minVoltageV),
                    format(indexed.minimumTier.maxVoltageV)
            );

            meta.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.meta.minimum-tier",
                            tierName(indexed.minimumTier),
                            voltageRange
                    )
            ).left();
        }).growX().pad(8f);

        root.row();

        Table io = new Table();
        io.top();

        Table inputs = new Table();
        inputs.top().left();
        inputs.add(
                Core.bundle.get("mdtnh.recipe-query.inputs")
        ).left().padBottom(5f).row();

        addItemStacks(inputs, indexed.definition.inputItems);
        addLiquidStacks(inputs, indexed.definition.inputLiquids);

        Table outputs = new Table();
        outputs.top().left();
        outputs.add(
                Core.bundle.get("mdtnh.recipe-query.outputs")
        ).left().padBottom(5f).row();

        addItemStacks(outputs, indexed.definition.outputItems);
        addLiquidStacks(outputs, indexed.definition.outputLiquids);

        io.add(inputs)
                .width(IO_COLUMN_WIDTH)
                .top()
                .pad(6f);

        io.add(
                Core.bundle.get("mdtnh.recipe-query.io.arrow")
        ).pad(12f);

        io.add(outputs)
                .width(IO_COLUMN_WIDTH)
                .top()
                .pad(6f);

        root.add(io).growX();
        root.row();

        root.add(
                Core.bundle.get("mdtnh.recipe-query.runtime.title")
        ).left()
                .padLeft(8f)
                .padTop(10f)
                .padBottom(4f);

        root.row();
        buildVoltageTable(root, indexed);
    }

    private void addItemStacks(Table table, ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) return;

        for (ItemStack stack : stacks) {
            if (stack == null || stack.item == null) continue;

            Table entry = new Table();
            entry.left();
            entry.background(Styles.black6);

            entry.image(stack.item.uiIcon)
                    .size(34f)
                    .pad(5f);

            Label name = new Label(stack.item.localizedName);
            name.setEllipsis(true);

            entry.add(name)
                    .width(IO_COLUMN_WIDTH - 105f)
                    .left();

            entry.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.stack.amount",
                            stack.amount
                    )
            ).right().pad(6f);

            bindHover(entry, stack.item);

            table.add(entry)
                    .growX()
                    .minHeight(IO_ENTRY_MIN_HEIGHT)
                    .padBottom(3f)
                    .row();
        }
    }

    private void addLiquidStacks(Table table, LiquidStack[] stacks) {
        if (stacks == null || stacks.length == 0) return;

        for (LiquidStack stack : stacks) {
            if (stack == null || stack.liquid == null) continue;

            Table entry = new Table();
            entry.left();
            entry.background(Styles.black6);

            entry.image(stack.liquid.uiIcon)
                    .size(34f)
                    .pad(5f);

            Label name = new Label(stack.liquid.localizedName);
            name.setEllipsis(true);

            entry.add(name)
                    .width(IO_COLUMN_WIDTH - 105f)
                    .left();

            entry.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.stack.amount",
                            format(stack.amount)
                    )
            ).right().pad(6f);

            /*
             * 流体也绑定 hover，因此可以继续按 R/U 递归查询。
             */
            bindHover(entry, stack.liquid);

            table.add(entry)
                    .growX()
                    .minHeight(IO_ENTRY_MIN_HEIGHT)
                    .padBottom(3f)
                    .row();
        }
    }

    private void buildVoltageTable(
            Table root,
            VoltageRecipeRegistry.RegisteredRecipe indexed) {

        Table table = new Table();
        table.left();
        table.defaults().pad(4f);

        table.add(
                Core.bundle.get("mdtnh.recipe-query.table.method")
        ).width(145f).left();

        table.add(
                Core.bundle.get("mdtnh.recipe-query.table.voltage")
        ).width(180f).left();

        table.add(
                Core.bundle.get("mdtnh.recipe-query.table.time")
        ).width(185f).left();

        table.add(
                Core.bundle.get("mdtnh.recipe-query.table.energy")
        ).width(155f).left();

        table.add(
                Core.bundle.get("mdtnh.recipe-query.table.power")
        ).width(155f).left();

        table.row();

        for (VoltageTier tier : VoltageTier.values()) {
            RecipeCrafter.Recipe variant =
                    indexed.electricVariants.get(tier);

            if (variant == null) continue;

            String voltage = Core.bundle.format(
                    "mdtnh.recipe-query.voltage.range",
                    format(tier.minVoltageV),
                    format(tier.maxVoltageV)
            );

            addVariantRow(
                    table,
                    Core.bundle.format(
                            "mdtnh.recipe-query.variant.electric",
                            tierName(tier)
                    ),
                    voltage,
                    variant
            );
        }

        if (indexed.steamVariant != null) {
            addVariantRow(
                    table,
                    Core.bundle.format(
                            "mdtnh.recipe-query.variant.steam",
                            tierName(VoltageTier.ULV)
                    ),
                    Core.bundle.get(
                            "mdtnh.recipe-query.voltage.steam"
                    ),
                    indexed.steamVariant
            );
        }

        if (indexed.manualVariant != null) {
            addVariantRow(
                    table,
                    Core.bundle.format(
                            "mdtnh.recipe-query.variant.manual",
                            tierName(VoltageTier.ULV)
                    ),
                    Core.bundle.get(
                            "mdtnh.recipe-query.voltage.none"
                    ),
                    indexed.manualVariant
            );
        }

        root.add(table)
                .growX()
                .pad(8f);
    }

    private void addVariantRow(Table table,
                               String method,
                               String voltage,
                               RecipeCrafter.Recipe recipe) {

        float seconds = recipe.craftTime / 60f;
        float power =
                seconds <= 0f
                        ? 0f
                        : recipe.energyPerCraftJ / seconds;

        table.add(method).left();
        table.add(voltage).left();

        table.add(
                Core.bundle.format(
                        "mdtnh.recipe-query.time.value",
                        format(seconds),
                        format(recipe.craftTime)
                )
        ).left();

        table.add(
                Core.bundle.format(
                        "mdtnh.recipe-query.energy.value",
                        format(recipe.energyPerCraftJ)
                )
        ).left();

        table.add(
                Core.bundle.format(
                        "mdtnh.recipe-query.power.value",
                        format(power)
                )
        ).left();

        table.row();
    }

    private void buildSubPageNavigation(
            Table root,
            CategoryPage page) {

        root.table(nav -> {
            nav.button(
                    Core.bundle.get(
                            "mdtnh.recipe-query.recipe.previous"
                    ),
                    Styles.flatt,
                    () -> {
                        if (selectedRecipeIndex > 0) {
                            selectedRecipeIndex--;
                        }
                        Core.app.post(this::rebuild);
                    }
            ).width(SUBPAGE_BUTTON_WIDTH)
                    .height(SUBPAGE_BUTTON_HEIGHT)
                    .disabled(button -> selectedRecipeIndex <= 0);

            nav.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.recipe.counter",
                            selectedRecipeIndex + 1,
                            page.recipes.size()
                    )
            ).width(130f)
                    .center();

            nav.button(
                    Core.bundle.get(
                            "mdtnh.recipe-query.recipe.next"
                    ),
                    Styles.flatt,
                    () -> {
                        if (selectedRecipeIndex + 1 <
                                page.recipes.size()) {
                            selectedRecipeIndex++;
                        }
                        Core.app.post(this::rebuild);
                    }
            ).width(SUBPAGE_BUTTON_WIDTH)
                    .height(SUBPAGE_BUTTON_HEIGHT)
                    .disabled(
                            button ->
                                    selectedRecipeIndex + 1 >=
                                            page.recipes.size()
                    );
        }).padTop(6f)
                .padBottom(4f);
    }

    private void buildEmptyState(Table root) {
        String key =
                queryMode == QueryMode.recipe
                        ? "mdtnh.recipe-query.empty.recipe"
                        : "mdtnh.recipe-query.empty.usage";

        root.add(
                Core.bundle.get(key)
        ).width(760f)
                .pad(35f);
    }

    private String categoryName(
            VoltageRecipeRegistry registry) {

        String key =
                "recipe-category." +
                        registry.contentPrefix;

        return Core.bundle.get(
                key,
                Core.bundle.format(
                        "mdtnh.recipe-query.category.fallback",
                        registry.contentPrefix
                )
        );
    }

    private String groupName(String group) {
        String key = "group." + group;

        return Core.bundle.get(
                key,
                Core.bundle.format(
                        "mdtnh.recipe-query.group.fallback",
                        group
                )
        );
    }

    private String tierName(VoltageTier tier) {
        String key =
                "voltage-tier." +
                        tier.name().toLowerCase();

        return Core.bundle.get(
                key,
                Core.bundle.format(
                        "mdtnh.recipe-query.tier.fallback",
                        tier.name()
                )
        );
    }

    private String format(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001f) {
            return Long.toString(Math.round(value));
        }

        return Strings.autoFixed(value, 2);
    }
}
