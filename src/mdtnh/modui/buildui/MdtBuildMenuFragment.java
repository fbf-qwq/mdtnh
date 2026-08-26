package mdtnh.modui.buildui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.Seq;
import mdtnh.modui.introduction.MdtIntroductionUI;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.Category;
import mindustry.ui.Styles;
import mindustry.ui.fragments.PlacementFragment;
import mindustry.world.Block;

import java.lang.reflect.Field;

/**
 * MDT 多级建造菜单的 HUD 控制器。
 *
 * <p>该类读取 {@link BuildMenuRegistry} 中的树形数据，将入口按钮注入原版建造分类栏，
 * 并负责分类导航、方块选择、悬浮信息和弹窗定位。它不创建任何游戏内容。</p>
 *
 * <p>界面由三部分组成：原版分类栏中的 MDT 入口按钮、分类/方块弹窗、
 * 以及鼠标悬停方块时显示的建造花费面板。</p>
 */
public final class MdtBuildMenuFragment {

    /** 注入场景树时使用的唯一名称，用于检测并复用已有入口按钮。 */
    private static final String entryName = "mdtnh-build-menu-entry";

    /** 收藏分类按钮在场景树中的唯一名称。 */
    private static final String favoriteCategoryName =
            "mdtnh-favorite-category";

    /** 右侧 MDT 辅助入口栏的唯一名称。 */
    private static final String sideRailName =
            "mdtnh-build-menu-side-rail";

    /** 介绍/教程入口在场景树中的唯一名称。 */
    private static final String introductionEntryName =
            "mdtnh-introduction-menu-entry";

    /** 菜单数据来源。 */
    private final BuildMenuRegistry registry;

    /** 当前正在浏览的分类节点。 */
    private BuildMenuNode current;

    /**
     * MDT 自定义建造树入口。
     *
     * <p>不再占用原版分类栏左下角，而是放到原版建造菜单右侧辅助栏。</p>
     */
    private ImageButton entryButton;

    /** 原版分类栏中的收藏分类按钮。 */
    private ImageButton favoriteCategoryButton;

    /** 原版建造菜单右侧的介绍/教程入口。 */
    private ImageButton introductionEntryButton;

    /** 原版建造菜单右侧的 MDT 辅助入口栏。 */
    private Table sideRail;

    /** 原版方块选择 ScrollPane。收藏分类直接复用这一块区域。 */
    private ScrollPane vanillaBlockPane;

    /** 原版 ScrollPane 原先承载的方块 Table。 */
    private Element vanillaBlockWidget;

    /** 收藏分类使用的方块 Table。 */
    private final Table favoriteBlockTable =
            new Table();

    /** 当前是否正在浏览收藏伪分类。 */
    private boolean favoriteCategoryActive;

    /** 打开收藏分类时原版记录的分类，用于检测键盘切换分类。 */
    private Category favoriteBaseCategory;

    /** 收藏分类中当前悬浮的方块。 */
    private Block favoriteHoveredBlock;

    /** 收藏分类自己的滚动位置。 */
    private float favoriteScrollY;

    /**
     * PlacementFragment.menuHoverBlock 的兼容访问。
     *
     * <p>它在 Mindustry 中是包可见字段，模组不在 mindustry 包下，
     * 因此这里只在收藏页悬浮时使用一次反射桥接。反射失败不会影响
     * 收藏、选择和 O 键，只会缺少原版顶部悬浮信息同步。</p>
     */
    private Field placementMenuHoverField;

    /** 分类和方块主体弹窗。 */
    private Table popup;

    /** 当前悬停方块的详情面板。 */
    private Table hoverInfo;

    /** 当前鼠标悬停方块；没有悬停对象时为 {@code null}。 */
    private Block hoveredBlock;

    /** 菜单是否处于打开状态。 */
    private boolean opened;

    /** 防止重复安装 HUD 元素和 update 回调。 */
    private boolean installed;

    /** 上一帧原版建造系统选中的方块，用于检测选择变化。 */
    private Block lastSelected;

    /** 坐标换算复用向量，避免位置更新时频繁创建临时对象。 */
    private final Vec2 tmp = new Vec2();

    /**
     * 创建菜单控制器，并从注册表根节点开始浏览。
     *
     * @param registry 已完成内容注册的菜单注册表
     */
    public MdtBuildMenuFragment(BuildMenuRegistry registry) {
        this.registry = registry;
        this.current = registry.root;
        BuildMenuFavorites.load();
    }

    /**
     * 将 MDT 菜单安装到客户端 HUD。
     *
     * <p>安装后每帧确认入口按钮仍在场景中；如果原版 HUD 重建导致按钮消失，
     * 会自动重新注入。同时同步原版当前选中方块与 MDT 菜单分类。</p>
     */
    public void install() {
        if (installed) return;
        installed = true;
        buildPopup();
        Events.run(Trigger.update, () -> {
            if (entryButton == null ||
                    entryButton.getScene() == null ||
                    favoriteCategoryButton == null ||
                    favoriteCategoryButton.getScene() == null ||
                    introductionEntryButton == null ||
                    introductionEntryButton.getScene() == null) {

                favoriteCategoryActive = false;
                favoriteHoveredBlock = null;
                injectEntryButton();
            }

            syncSelectedBlock();
            updateFavoriteCategoryState();
            updateFavoriteHotkey();
        });
        Core.app.post(this::injectEntryButton);
    }

    /**
     * 创建主弹窗和悬浮信息面板，并挂到 HUD 根容器。
     *
     * <p>这里只建立容器与可见性更新逻辑，具体分类内容在打开菜单时生成。</p>
     */
    private void buildPopup() {
        popup = new Table(Tex.pane);
        popup.touchable = Touchable.enabled;
        popup.visible = false;
        Vars.ui.hudGroup.addChild(popup);

        hoverInfo = new Table(Tex.buttonEdge2);
        hoverInfo.touchable = Touchable.disabled;
        hoverInfo.visible = false;
        Vars.ui.hudGroup.addChild(hoverInfo);

        popup.update(() -> {
            boolean visible = opened && Vars.ui.hudfrag.shown
                    && entryButton != null && entryButton.getScene() != null;

            popup.visible = visible;

            if (visible) {
                updatePopupPosition();
                updateHoverInfoPosition();
            }

            hoverInfo.visible = visible && hoveredBlock != null;
        });
    }

    /**
     * 在原版建造分类栏中注入 MDT 入口按钮。
     *
     * <p>先按唯一名称寻找已有元素以避免重复创建；若不存在，
     * 则定位原版分类按钮的父表格并在末尾追加 MDT 按钮。</p>
     */
    private void injectEntryButton() {
        if (Vars.ui == null ||
                Vars.ui.hudfrag == null ||
                registry.root.icon == null) {
            return;
        }

        Element existingBuild =
                Core.scene.find(entryName);

        Element existingFavorite =
                Core.scene.find(
                        favoriteCategoryName
                );

        Element existingIntroduction =
                Core.scene.find(
                        introductionEntryName
                );

        Element existingSideRail =
                Core.scene.find(
                        sideRailName
                );

        if (existingBuild instanceof ImageButton) {
            entryButton =
                    (ImageButton) existingBuild;
        }

        if (existingFavorite instanceof ImageButton) {
            favoriteCategoryButton =
                    (ImageButton) existingFavorite;
        }

        if (existingIntroduction instanceof ImageButton) {
            introductionEntryButton =
                    (ImageButton) existingIntroduction;
        }

        if (existingSideRail instanceof Table) {
            sideRail =
                    (Table) existingSideRail;
        }

        Table categoryTable =
                findVanillaCategoryTable();

        if (categoryTable == null ||
                !(categoryTable.parent instanceof Table)) {
            return;
        }

        Table blockCatTable =
                (Table) categoryTable.parent;

        ScrollPane foundPane =
                findFirstScrollPane(
                        blockCatTable
                );

        if (foundPane != null) {
            vanillaBlockPane =
                    foundPane;

            if (!favoriteCategoryActive &&
                    vanillaBlockPane.getWidget() !=
                            favoriteBlockTable) {

                vanillaBlockWidget =
                        vanillaBlockPane.getWidget();
            }
        }

        /*
         * 原本 MDT 自定义建造树占用原版分类栏最后一行左格，
         * 右格为空。
         *
         * 现在收藏、MDT 建造树、介绍菜单全部移动到右侧辅助栏，
         * 因此这里不再放任何可交互入口，而是保留两个
         * 50 x 50 的灰色半透明占位格，让原版分类网格保持整齐。
         */
        Element oldFavorite =
                Core.scene.find(
                        favoriteCategoryName
                );

        if (oldFavorite != null &&
                oldFavorite.parent ==
                        categoryTable) {

            oldFavorite.remove();
            favoriteCategoryButton = null;
        }

        Element oldBuild =
                Core.scene.find(entryName);

        if (oldBuild != null &&
                oldBuild.parent ==
                        categoryTable) {

            oldBuild.remove();
            entryButton = null;
        }

        /*
         * 只有在最后一行还没有我们的灰色占位时才追加，
         * 避免 HUD 重建时重复插入。
         */
        Element placeholder =
                Core.scene.find(
                        "mdtnh-build-menu-placeholder-left"
                );

        if (placeholder == null) {
            categoryTable.row();

            arc.scene.ui.Image left =
                    categoryTable.image(
                            Styles.black6
                    ).size(50f)
                            .get();

            left.name =
                    "mdtnh-build-menu-placeholder-left";

            arc.scene.ui.Image right =
                    categoryTable.image(
                            Styles.black6
                    ).size(50f)
                            .get();

            right.name =
                    "mdtnh-build-menu-placeholder-right";
        }

        /*
         * 右侧辅助栏：
         *
         * [ 收藏       ]
         * [ MDT 建造树 ]
         * [ 介绍/教程  ]
         *
         * 收藏按钮不加入原版 Category ButtonGroup，
         * 从而避免点击后被原版分类监听器立即改回、只显示一帧。
         */
        if (sideRail == null ||
                sideRail.getScene() == null) {

            sideRail = new Table();
            sideRail.name = sideRailName;
            sideRail.bottom();
            sideRail.defaults().size(50f);
            sideRail.setBackground(Tex.pane);

            favoriteCategoryButton =
                    sideRail.button(
                            Icon.list,
                            Styles.clearTogglei,
                            this::openFavoriteCategory
                    ).get();

            favoriteCategoryButton.name =
                    favoriteCategoryName;

            sideRail.row();

            entryButton =
                    sideRail.button(
                            registry.root.icon,
                            Styles.clearTogglei,
                            this::toggle
                    ).get();

            entryButton.name =
                    entryName;

            sideRail.row();

            introductionEntryButton =
                    sideRail.button(
                            Icon.list,
                            Styles.clearTogglei,
                            this::openIntroductionMenu
                    ).get();

            introductionEntryButton.name =
                    introductionEntryName;

            blockCatTable.add(sideRail)
                    .fillY()
                    .bottom()
                    .padLeft(4f);

            Vars.ui.addDescTooltip(
                    favoriteCategoryButton,
                    Core.bundle.get(
                            "mdtnh.favorite.category.tooltip"
                    )
            );

            Vars.ui.addDescTooltip(
                    entryButton,
                    Core.bundle.get(
                            "mdtnh.menu.entry.tooltip"
                    )
            );

            Vars.ui.addDescTooltip(
                    introductionEntryButton,
                    Core.bundle.get(
                            "mdtnh.intro.entry.tooltip"
                    )
            );
        }

        if (favoriteCategoryButton != null) {
            favoriteCategoryButton.update(
                    () -> favoriteCategoryButton
                            .setChecked(
                                    favoriteCategoryActive
                            )
            );
        }

        if (entryButton != null) {
            entryButton.update(
                    () -> entryButton.setChecked(
                            opened
                    )
            );
        }

        if (introductionEntryButton != null) {
            introductionEntryButton.update(
                    () -> introductionEntryButton
                            .setChecked(
                                    MdtIntroductionUI
                                            .isShown()
                            )
            );
        }

        categoryTable.invalidateHierarchy();
        blockCatTable.invalidateHierarchy();
    }

    /**
     * 切换到收藏伪分类。
     *
     * <p>收藏按钮已经加入原版分类按钮 ButtonGroup，所以选中收藏时
     * 原版分类按钮会自动取消勾选；点击任意原版分类又会自动取消收藏勾选。</p>
     */
    private void openFavoriteCategory() {
        if (vanillaBlockPane == null) {
            return;
        }

        if (vanillaBlockPane.getWidget() !=
                favoriteBlockTable) {

            vanillaBlockWidget =
                    vanillaBlockPane.getWidget();
        }

        favoriteCategoryActive = true;

        if (favoriteCategoryButton != null) {
            favoriteCategoryButton.setChecked(
                    true
            );
        }

        PlacementFragment placement =
                Vars.ui.hudfrag.blockfrag;

        favoriteBaseCategory =
                placement.currentCategory;

        favoriteHoveredBlock = null;
        setPlacementMenuHoverBlock(null);

        rebuildFavoriteCategory();

        if (favoriteCategoryButton != null) {
            favoriteCategoryButton.setChecked(
                    true
            );
        }
    }

    /**
     * 每帧检测是否通过原版分类按钮或分类快捷键离开收藏页。
     */
    private void updateFavoriteCategoryState() {
        if (!favoriteCategoryActive) {
            return;
        }

        if (favoriteCategoryButton == null ||
                favoriteCategoryButton.getScene() == null ||
                vanillaBlockPane == null) {

            leaveFavoriteCategory();
            return;
        }

        PlacementFragment placement =
                Vars.ui.hudfrag.blockfrag;

        /*
         * 若玩家使用原版分类快捷键切换到了别的 Category，
         * currentCategory 会发生变化，此时退出收藏页。
         */
        if (favoriteBaseCategory != null &&
                placement.currentCategory !=
                        favoriteBaseCategory) {

            leaveFavoriteCategory();
            return;
        }

        /*
         * 原版 PlacementFragment 有时会在自身 update / rebuild 中
         * 重新把 blockPane 指回原版列表。
         *
         * 收藏页激活期间，每帧确认一次 widget；
         * 如果被原版覆盖，就立即重新接管。
         * 这样不会再出现“显示一帧后消失”。
         */
        if (vanillaBlockPane.getWidget() !=
                favoriteBlockTable) {

            vanillaBlockPane.setWidget(
                    favoriteBlockTable
            );

            favoriteBlockTable.act(0f);

            vanillaBlockPane
                    .setScrollYForce(
                            favoriteScrollY
                    );

            vanillaBlockPane.act(0f);
            vanillaBlockPane.layout();
        }
    }

    /** 恢复原版方块列表。 */
    private void leaveFavoriteCategory() {
        favoriteCategoryActive = false;
        favoriteBaseCategory = null;
        favoriteHoveredBlock = null;
        setPlacementMenuHoverBlock(null);

        if (vanillaBlockPane != null &&
                vanillaBlockWidget != null &&
                vanillaBlockPane.getWidget() ==
                        favoriteBlockTable) {

            vanillaBlockPane.setWidget(
                    vanillaBlockWidget
            );

            vanillaBlockPane.act(0f);
            vanillaBlockPane.layout();
        }
    }

    /**
     * 使用原版 blockPane 尺寸和方块按钮样式构建收藏内容。
     */
    private void rebuildFavoriteCategory() {
        if (vanillaBlockPane == null) {
            return;
        }

        if (vanillaBlockPane.getWidget() ==
                favoriteBlockTable) {

            favoriteScrollY =
                    vanillaBlockPane.getScrollY();
        }

        favoriteBlockTable.clearChildren();
        favoriteBlockTable.top();
        favoriteBlockTable.margin(5f);

        Seq<Block> favorites =
                BuildMenuFavorites.all();

        int index = 0;

        for (Block block :
                favorites) {

            if (!available(block)) {
                continue;
            }

            if (index > 0 &&
                    index % 4 == 0) {

                favoriteBlockTable.row();
            }

            ImageButton button =
                    favoriteBlockTable.button(
                            new TextureRegionDrawable(
                                    block.uiIcon
                            ),
                            Styles.selecti,
                            () -> selectFavoriteBlock(
                                    block
                            )
                    ).size(46f)
                            .get();

            button.resizeImage(40f);

            button.update(
                    () -> updateFavoriteBlockButton(
                            button,
                            block
                    )
            );

            button.hovered(
                    () -> {
                        favoriteHoveredBlock =
                                block;

                        setPlacementMenuHoverBlock(
                                block
                        );
                    }
            );

            button.exited(
                    () -> {
                        if (favoriteHoveredBlock ==
                                block) {

                            favoriteHoveredBlock =
                                    null;

                            setPlacementMenuHoverBlock(
                                    null
                            );
                        }
                    }
            );

            index++;
        }

        if (index == 0) {
            favoriteBlockTable.add(
                    Core.bundle.get(
                            "mdtnh.favorite.empty"
                    )
            ).width(170f)
                    .pad(12f)
                    .left();
        } else {
            int rest = index % 4;

            if (rest != 0) {
                for (int i = rest;
                     i < 4;
                     i++) {

                    favoriteBlockTable.add()
                            .size(46f);
                }
            }
        }

        vanillaBlockPane.setWidget(
                favoriteBlockTable
        );

        favoriteBlockTable.act(0f);

        vanillaBlockPane.setScrollYForce(
                favoriteScrollY
        );

        Core.app.post(
                () -> {
                    if (!favoriteCategoryActive ||
                            vanillaBlockPane == null) {
                        return;
                    }

                    vanillaBlockPane
                            .setScrollYForce(
                                    favoriteScrollY
                            );

                    vanillaBlockPane.act(0f);
                    vanillaBlockPane.layout();
                }
        );
    }

    /**
     * 与原版 PlacementFragment 的方块按钮保持相同的
     * 资源不足/不可放置变灰逻辑。
     */
    private void updateFavoriteBlockButton(
            ImageButton button,
            Block block) {

        Building core =
                Vars.player == null
                        ? null
                        : Vars.player.core();

        boolean canAfford =
                Vars.state.rules.infiniteResources ||
                        (core != null &&
                                core.items.has(
                                        block.requirements,
                                        Vars.state.rules
                                                .buildCostMultiplier
                                ));

        boolean canBuild =
                Vars.player != null &&
                        Vars.player.isBuilder();

        Color color =
                canAfford && canBuild
                        ? Color.white
                        : Color.gray;

        button.forEach(
                element ->
                        element.setColor(color)
        );

        if (!block.isPlaceable()) {
            button.forEach(
                    element ->
                            element.setColor(
                                    Color.darkGray
                            )
            );
        }

        button.setChecked(
                Vars.control.input.block ==
                        block
        );
    }

    /**
     * 收藏页中的点击行为直接复用原版放置系统，不切换 currentCategory，
     * 因而选中不同原版分类的方块时收藏页仍保持打开。
     */
    private void selectFavoriteBlock(
            Block block) {

        if (!available(block)) {
            return;
        }

        Vars.control.input.block =
                Vars.control.input.block ==
                        block
                        ? null
                        : block;
    }

    /**
     * O 键：
     * - MDT 自定义建造树中悬浮：收藏/取消收藏；
     * - 原版收藏分类中悬浮：取消收藏（再次按也遵循 toggle 语义）。
     */
    private void updateFavoriteHotkey() {
        if (Core.input == null ||
                Core.scene == null ||
                Core.scene.hasKeyboard()) {
            return;
        }

        Block target = null;

        if (opened &&
                hoveredBlock != null) {

            target = hoveredBlock;

        } else if (favoriteCategoryActive &&
                favoriteHoveredBlock != null) {

            target = favoriteHoveredBlock;
        }

        if (target == null ||
                !Core.input.keyTap(
                        KeyCode.o
                )) {
            return;
        }

        BuildMenuFavorites.toggle(
                target
        );

        /*
         * 自定义建造树保持当前 hover，同时刷新收藏标记。
         */
        if (opened &&
                hoveredBlock == target) {

            rebuildHoverInfo(
                    target
            );
        }

        /*
         * 收藏页里按 O 后立即从当前列表移除该项。
         * 若以后允许从收藏页重新添加，toggle 语义仍然兼容。
         */
        if (favoriteCategoryActive) {
            favoriteHoveredBlock = null;
            setPlacementMenuHoverBlock(
                    null
            );

            rebuildFavoriteCategory();
        }
    }

    /**
     * 把收藏页的悬浮块同步给原版 PlacementFragment 顶部详情框。
     *
     * <p>Mindustry 当前实现中的 menuHoverBlock 为包可见字段；
     * 模组无法直接访问，因此使用反射桥接。字段不存在时静默降级。</p>
     */
    private void setPlacementMenuHoverBlock(
            Block block) {

        try {
            if (Vars.ui == null ||
                    Vars.ui.hudfrag == null) {
                return;
            }

            PlacementFragment placement =
                    Vars.ui.hudfrag.blockfrag;

            if (placementMenuHoverField == null) {
                placementMenuHoverField =
                        PlacementFragment.class
                                .getDeclaredField(
                                        "menuHoverBlock"
                                );

                placementMenuHoverField
                        .setAccessible(true);
            }

            placementMenuHoverField.set(
                    placement,
                    block
            );
        } catch (Throwable ignored) {
            /*
             * 兼容未来 Mindustry 字段改名：
             * 失败只影响原版顶部 hover 同步，
             * 收藏列表、O 键和放置选择仍然正常。
             */
        }
    }

    /**
     * 递归寻找原版 blockPane。
     */
    private ScrollPane findFirstScrollPane(
            Element root) {

        if (root instanceof ScrollPane) {
            return (ScrollPane) root;
        }

        if (!(root instanceof Group)) {
            return null;
        }

        Group group =
                (Group) root;

        for (Element child :
                group.getChildren()) {

            ScrollPane result =
                    findFirstScrollPane(
                            child
                    );

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /** 关闭建造菜单弹窗，并打开介绍/教程中心。 */
    private void openIntroductionMenu() {
        if (favoriteCategoryActive) {
            leaveFavoriteCategory();
        }

        opened = false;
        clearHoveredBlock();

        if (popup != null) {
            popup.visible = false;
        }

        if (hoverInfo != null) {
            hoverInfo.visible = false;
        }

        MdtIntroductionUI.show();
    }

    /**
     * 定位 Mindustry 原版建造分类按钮所在表格。
     *
     * @return 分类表格；HUD 尚未构建完成时返回 {@code null}
     */
    private Table findVanillaCategoryTable() {
        for (Category category : Category.values()) {
            Element element = Core.scene.find("category-" + category.name());
            if (element == null) continue;
            if (element.parent instanceof Table)
                return (Table) element.parent;
        }
        return null;
    }

    /**
     * 切换菜单打开状态。
     *
     * <p>打开时重建当前分类并把弹窗置于前景；关闭时同时隐藏悬浮信息。</p>
     */
    private void toggle() {
        if (favoriteCategoryActive) {
            leaveFavoriteCategory();
        }

        opened = !opened;
        clearHoveredBlock();

        if (opened) {
            rebuildPopup();
            popup.visible = true;
            popup.toFront();
            hoverInfo.toFront();
            updatePopupPosition();
        } else {
            popup.visible = false;
            hoverInfo.visible = false;
        }
    }

    /**
     * 按当前分类重新生成整个菜单弹窗。
     *
     * <p>标题区提供返回上级、面包屑路径和关闭按钮；主体使用滚动面板，
     * 避免内容过多时超出屏幕。</p>
     */
    private void rebuildPopup() {
        clearHoveredBlock();

        popup.clearChildren();
        popup.margin(6f);
        buildHeader();
        popup.row();
        popup.pane(this::buildContent).width(300f).maxHeight(380f);
        popup.pack();
    }

    /** 构建返回按钮、面包屑路径和关闭按钮组成的标题栏。 */
    private void buildHeader() {
        popup.table(header -> {
            if (current != registry.root) {
                header.button(Core.bundle.get("mdtnh.menu.back"), Styles.cleart, () -> {
                    if (current.parent != null) {
                        current = current.parent;
                        rebuildPopup();
                    }
                }).size(42f);
            } else {
                header.add().size(42f);
            }
            header.add(getBreadcrumb()).left().growX().padLeft(6f).padRight(6f);
            header.button(Core.bundle.get("mdtnh.menu.close"), Styles.cleart, () -> opened = false).size(42f);
        }).growX();
    }

    /**
     * 从当前节点向根节点回溯，生成“MDT > 分类 > 子分类”形式的路径文本。
     */
    private String getBreadcrumb() {
        if (current == registry.root) return registry.root.title;
        Seq<BuildMenuNode> path = new Seq<>();
        BuildMenuNode node = current;
        while (node != null && node != registry.root) {
            path.insert(0, node);
            node = node.parent;
        }
        StringBuilder builder = new StringBuilder(registry.root.title);
        for (BuildMenuNode item : path) {
            builder.append(Core.bundle.get("mdtnh.menu.breadcrumb.separator")).append(item.title);
        }
        return builder.toString();
    }

    /**
     * 构建当前分类主体内容。
     *
     * <p>先显示仍有可用内容的子分类，再显示当前分类直接包含的方块。
     * 如果两者都为空，则显示空分类提示。</p>
     */
    private void buildContent(Table table) {
        table.top().left();
        buildChildren(table);
        if (hasAvailableBlocks(current) && hasAvailableChildren(current)) {
            table.add().height(5f);
            table.row();
        }
        buildBlocks(table);
        if (table.getChildren().isEmpty()) {
            table.add(Core.bundle.get("mdtnh.menu.empty")).pad(10f);
        }
    }

    /** 将当前节点中包含可用内容的子分类生成为纵向导航按钮。 */
    private void buildChildren(Table table) {
        for (BuildMenuNode child : current.children) {
            if (!hasAvailableContent(child)) continue;
            table.button(Core.bundle.format("mdtnh.menu.child", child.title), Styles.cleart, () -> {
                current = child;
                rebuildPopup();
            }).height(44f).growX();
            table.row();
        }
    }

    /**
     * 将当前分类中的可用方块按每行四个排列。
     *
     * <p>按钮勾选状态与原版放置系统同步；悬停按钮时刷新建造花费详情。</p>
     */
    private void buildBlocks(Table table) {
        Table grid = new Table();
        int index = 0;
        for (Block block : current.blocks) {
            if (!available(block)) continue;
            if (index > 0 && index % 4 == 0) grid.row();
            ImageButton button = grid.button(
                    new TextureRegionDrawable(block.uiIcon), Styles.selecti, () -> select(block)
            ).size(56f).get();
            button.resizeImage(40f);
            button.update(() -> button.setChecked(Vars.control.input.block == block));

            button.hovered(() -> setHoveredBlock(block));
            button.exited(() -> {
                if (hoveredBlock == block) clearHoveredBlock();
            });

            index++;
        }
        int rest = index % 4;
        if (rest != 0) {
            for (int i = rest; i < 4; i++) grid.add().size(56f);
        }
        table.add(grid).left();
    }

    /** 设置当前悬停方块，并根据该方块刷新详情面板。 */
    private void setHoveredBlock(Block block) {
        if (block == null || hoveredBlock == block) return;
        hoveredBlock = block;
        rebuildHoverInfo(block);
        hoverInfo.visible = opened && popup.visible;
        hoverInfo.toFront();
        updateHoverInfoPosition();
    }

    /** 清空悬停状态，同时隐藏并清空详情面板。 */
    private void clearHoveredBlock() {
        hoveredBlock = null;
        if (hoverInfo != null) {
            hoverInfo.visible = false;
            hoverInfo.clearChildren();
        }
    }

    /**
     * 根据方块重建悬浮信息。
     *
     * <p>顶部显示方块图标和名称，下方逐项列出受当前建造成本倍率影响后的资源需求。</p>
     */
    private void rebuildHoverInfo(Block block) {
        hoverInfo.clearChildren();
        hoverInfo.top().left();
        hoverInfo.margin(6f);

        hoverInfo.table(header -> {
            header.left();
            header.image(block.uiIcon).size(40f).padRight(8f);
            header.add(
                    block.localizedName +
                            (BuildMenuFavorites
                                    .contains(block)
                                    ? Core.bundle.get(
                                            "mdtnh.favorite.mark"
                                      )
                                    : "")
            ).left().growX();
        }).growX().left();

        hoverInfo.row();

        hoverInfo.table(costs -> {
            costs.top().left();
            costs.add(Core.bundle.get("mdtnh.menu.build-cost")).left().padBottom(3f);
            costs.row();

            if (block.requirements == null || block.requirements.length == 0) {
                costs.add(Core.bundle.get("mdtnh.menu.none")).left().padLeft(2f);
                return;
            }

            for (ItemStack stack : block.requirements) {
                int required = Math.round(stack.amount * Vars.state.rules.buildCostMultiplier);
                costs.table(line -> {
                    line.left();
                    line.image(stack.item.uiIcon).size(20f).padRight(4f);
                    line.add(stack.item.localizedName).left().width(150f).get().setEllipsis(true);
                    line.label(() -> formatRequirement(stack, required)).right().padLeft(6f);
                }).growX().left();
                costs.row();
            }
        }).growX().left().padTop(4f);

        hoverInfo.row();

        hoverInfo.add(
                Core.bundle.get(
                        "mdtnh.favorite.hotkey-hint"
                )
        ).left()
                .padTop(5f);

        hoverInfo.pack();
    }

    /**
     * 格式化单项建造材料的库存与需求量。
     *
     * <p>库存不足一半使用红色；不足需求但达到一半使用强调色；
     * 数量充足使用白色。无限资源规则下只显示需求量。</p>
     */
    private String formatRequirement(ItemStack stack, int required) {
        Building core = Vars.player == null ? null : Vars.player.core();
        if (core == null || Vars.state.rules.infiniteResources) {
            return "[white]" + required;
        }
        int amount = core.items.get(stack.item);
        String color = amount < required / 2f ? "[scarlet]"
                : amount < required ? "[accent]"
                : "[white]";
        return color + amount + "[white]/" + required;
    }

    /** 将悬浮详情放在主弹窗上方，并限制在 HUD 可见边界内。 */
    private void updateHoverInfoPosition() {
        if (hoverInfo == null || popup == null || hoveredBlock == null) return;
        float desiredWidth = Math.max(popup.getWidth(), hoverInfo.getPrefWidth());
        hoverInfo.setWidth(desiredWidth);
        hoverInfo.invalidate();

        float x = popup.x;
        float y = popup.y + popup.getHeight() + 6f;
        float maxX = Math.max(0f, Vars.ui.hudGroup.getWidth() - hoverInfo.getWidth());
        float maxY = Math.max(0f, Vars.ui.hudGroup.getHeight() - hoverInfo.getHeight());

        x = Mathf.clamp(x, 0f, maxX);
        y = Mathf.clamp(y, 0f, maxY);
        hoverInfo.setPosition(x, y);
    }

    /**
     * 将方块交给 Mindustry 原版放置系统。
     *
     * <p>再次点击已选方块会取消选择。选择新方块时关闭 MDT 菜单，
     * 再在下一帧同步原版建造分类与输入状态。</p>
     */
    private void select(Block block) {
        if (!available(block)) return;
        if (Vars.control.input.block == block) {
            Vars.control.input.block = null;
            opened = false;
            clearHoveredBlock();
            return;
        }
        opened = false;
        clearHoveredBlock();
        Core.app.post(() -> {
            if (Vars.ui == null || Vars.ui.hudfrag == null) return;
            PlacementFragment placement = Vars.ui.hudfrag.blockfrag;
            Vars.control.input.block = block;
            if (placement.currentCategory != block.category) {
                placement.currentCategory = block.category;
                placement.rebuild();
            }
        });
    }

    /**
     * 根据原版当前选中方块同步 MDT 菜单所在分类。
     *
     * <p>通过注册表反向索引查找方块的主要分类；菜单打开时会立即刷新内容。</p>
     */
    private void syncSelectedBlock() {
        Block selected = Vars.control.input.block;
        if (selected == lastSelected) return;
        lastSelected = selected;
        if (selected == null) return;
        BuildMenuNode node = registry.primaryNode(selected);
        if (node == null || node == current) return;
        current = node;
        if (opened) rebuildPopup();
    }

    /**
     * 判断方块在当前游戏状态下是否应显示。
     *
     * <p>同时检查内容可见性、科技解锁、玩家可建造标记、环境限制和当前规则环境。</p>
     */
    private boolean available(Block block) {
        if (block == null) return false;
        return block.isVisible()
                && block.unlockedNowHost()
                && block.placeablePlayer
                && block.environmentBuildable()
                && block.supportsEnv(Vars.state.rules.env);
    }

    /** @return 当前节点是否至少包含一个可显示方块。 */
    private boolean hasAvailableBlocks(BuildMenuNode node) {
        for (Block block : node.blocks) if (available(block)) return true;
        return false;
    }

    /** @return 当前节点是否至少包含一个有可显示内容的子分类。 */
    private boolean hasAvailableChildren(BuildMenuNode node) {
        for (BuildMenuNode child : node.children) if (hasAvailableContent(child)) return true;
        return false;
    }

    /**
     * 递归判断节点本身或任意后代是否存在可显示内容。
     *
     * <p>用于隐藏完全空的分类，避免玩家进入没有任何可建造内容的层级。</p>
     */
    private boolean hasAvailableContent(BuildMenuNode node) {
        if (hasAvailableBlocks(node)) return true;
        for (BuildMenuNode child : node.children) if (hasAvailableContent(child)) return true;
        return false;
    }

    /** 根据入口按钮位置计算主弹窗坐标，并将结果限制在 HUD 边界内。 */
    private void updatePopupPosition() {
        if (entryButton == null || entryButton.getScene() == null) return;
        tmp.set(0f, 0f);
        entryButton.localToStageCoordinates(tmp);
        Vars.ui.hudGroup.stageToLocalCoordinates(tmp);
        float width = popup.getWidth();
        float height = popup.getHeight();

        float rightX =
                tmp.x +
                        entryButton.getWidth() +
                        8f;

        float leftX =
                tmp.x -
                        width -
                        8f;

        float availableWidth =
                Vars.ui.hudGroup.getWidth();

        float x =
                rightX + width <= availableWidth
                        ? rightX
                        : leftX;

        float y = tmp.y;

        x = Mathf.clamp(
                x,
                0f,
                Math.max(
                        0f,
                        availableWidth - width
                )
        );

        y = Mathf.clamp(
                y,
                0f,
                Math.max(
                        0f,
                        Vars.ui.hudGroup.getHeight() -
                                height
                )
        );

        popup.setPosition(x, y);
    }
}