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
import java.util.Deque;
import java.util.List;

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

    /*
     * ==============================
     * UI 尺寸集中配置
     * ==============================
     *
     * CATEGORY_BUTTONS_PER_PAGE 就是一屏显示几个分类按钮。
     * 后续只改这一个值即可改变分类分页密度。
     */
    private static final int CATEGORY_BUTTONS_PER_PAGE = 4;

    private static final float DIALOG_CONTENT_WIDTH = 900f;
    private static final float CATEGORY_BUTTON_WIDTH = 185f;
    private static final float CATEGORY_BUTTON_HEIGHT = 46f;
    private static final float CATEGORY_ARROW_SIZE = 54f;

    private static final float MODE_BUTTON_WIDTH = 210f;
    private static final float MODE_BUTTON_HEIGHT = 46f;

    private static final float SUBPAGE_BUTTON_WIDTH = 180f;
    private static final float SUBPAGE_BUTTON_HEIGHT = 46f;

    private static final float DIALOG_ACTION_BUTTON_WIDTH = 220f;
    private static final float DIALOG_ACTION_BUTTON_HEIGHT = 56f;

    private static final float IO_COLUMN_WIDTH = 395f;
    private static final float IO_ENTRY_MIN_HEIGHT = 46f;

    private static final RecipeQueryUI instance = new RecipeQueryUI();

    /** 当前由 UI 声明为鼠标悬浮的物品或流体。 */
    private static UnlockableContent hoveredContent;

    /** 声明 hoveredContent 的 UI 元素。 */
    private static Element hoverOwner;

    private BaseDialog dialog;
    private boolean installed;

    private UnlockableContent targetContent;
    private QueryMode queryMode = QueryMode.recipe;
    private VoltageRecipeRegistry selectedRegistry;
    private int selectedRecipeIndex;

    /** 当前分类按钮处于第几屏，从 0 开始。 */
    private int categoryPageIndex;

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
        final QueryMode queryMode;
        final VoltageRecipeRegistry selectedRegistry;
        final int selectedRecipeIndex;
        final int categoryPageIndex;

        NavigationState(UnlockableContent targetContent,
                        QueryMode queryMode,
                        VoltageRecipeRegistry selectedRegistry,
                        int selectedRecipeIndex,
                        int categoryPageIndex) {
            this.targetContent = targetContent;
            this.queryMode = queryMode;
            this.selectedRegistry = selectedRegistry;
            this.selectedRecipeIndex = selectedRecipeIndex;
            this.categoryPageIndex = categoryPageIndex;
        }
    }

    private RecipeQueryUI() {
    }

    public static void registerKeybind() {
    }

    public static void install() {
        instance.installInternal();
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
        if (dialog != null && dialog.isShown() && targetContent != null) {
            history.push(snapshot());
            open(content, requestedMode);
        } else {
            history.clear();
            open(content, requestedMode);
        }
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

    private void open(UnlockableContent content, QueryMode mode) {
        if (!isQueryable(content)) return;

        targetContent = content;
        queryMode = mode;
        selectedRegistry = null;
        selectedRecipeIndex = 0;
        categoryPageIndex = 0;

        ensureDialog();
        rebuild();

        if (!dialog.isShown()) {
            dialog.show();
        }
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
                queryMode,
                selectedRegistry,
                selectedRecipeIndex,
                categoryPageIndex
        );
    }

    private void navigateBack() {
        if (history.isEmpty()) return;

        NavigationState state = history.pop();
        targetContent = state.targetContent;
        queryMode = state.queryMode;
        selectedRegistry = state.selectedRegistry;
        selectedRecipeIndex = state.selectedRecipeIndex;
        categoryPageIndex = state.categoryPageIndex;

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

        if (dialog != null) {
            dialog.hide();
        }
    }

    private void rebuild() {
        if (dialog == null || targetContent == null) return;

        dialog.cont.clear();

        buildHeader(dialog.cont);
        dialog.cont.row();

        buildModeButtons(dialog.cont);
        dialog.cont.row();

        List<CategoryPage> pages = collectPages();

        if (pages.isEmpty()) {
            selectedRegistry = null;
            selectedRecipeIndex = 0;
            categoryPageIndex = 0;
            buildEmptyState(dialog.cont);
            return;
        }

        CategoryPage selectedPage = resolveSelectedPage(pages);
        clampCategoryPage(pages);

        buildCategoryTabs(dialog.cont, pages, selectedPage);
        dialog.cont.row();

        selectedRecipeIndex = Math.max(
                0,
                Math.min(selectedRecipeIndex, selectedPage.recipes.size() - 1)
        );

        VoltageRecipeRegistry.RegisteredRecipe recipe =
                selectedPage.recipes.get(selectedRecipeIndex);

        Table detail = new Table();
        detail.top().left();
        buildRecipePage(detail, recipe);

        ScrollPane pane = new ScrollPane(detail, Styles.smallPane);
        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(true, false);
        pane.setOverscroll(false, false);

        dialog.cont.add(pane)
                .width(DIALOG_CONTENT_WIDTH)
                .maxHeight(610f)
                .growY()
                .padTop(6f);

        dialog.cont.row();
        buildSubPageNavigation(dialog.cont, selectedPage);
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

    private void buildModeButtons(Table root) {
        root.table(t -> {
            t.button(
                    Core.bundle.format(
                            "mdtnh.recipe-query.mode.recipe",
                            keyName(recipeQueryKey)
                    ),
                    Styles.flatTogglet,
                    () -> switchMode(QueryMode.recipe)
            ).checked(queryMode == QueryMode.recipe)
                    .width(MODE_BUTTON_WIDTH)
                    .height(MODE_BUTTON_HEIGHT);

            t.button(
                    Core.bundle.format(
                            "mdtnh.recipe-query.mode.usage",
                            keyName(usageQueryKey)
                    ),
                    Styles.flatTogglet,
                    () -> switchMode(QueryMode.usage)
            ).checked(queryMode == QueryMode.usage)
                    .width(MODE_BUTTON_WIDTH)
                    .height(MODE_BUTTON_HEIGHT)
                    .padLeft(8f);
        }).padBottom(6f);
    }

    private String keyName(KeyBind bind) {
        if (bind == null ||
                bind.value == null ||
                bind.value.key == null) {
            return Core.bundle.get("mdtnh.recipe-query.key.unset");
        }

        return bind.value.key.getName();
    }

    private void switchMode(QueryMode mode) {
        if (queryMode == mode) return;

        queryMode = mode;
        selectedRegistry = null;
        selectedRecipeIndex = 0;
        categoryPageIndex = 0;

        Core.app.post(this::rebuild);
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
