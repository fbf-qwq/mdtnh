package mdtnh;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.*;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.struct.Seq;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mdtnh.draw.DrawerManager;
import mdtnh.energy.EnergySpec;
import mdtnh.energy.EnergyState;
import mdtnh.energy.MdtEnergyNode;
import mdtnh.hatch.EnergyInputHatch;
import mdtnh.hatch.Hatch;
import mdtnh.hatch.ItemInputHatch;
import mdtnh.hatch.ItemOutputHatch;
import mdtnh.hatch.LiquidInputHatch;
import mdtnh.hatch.LiquidOutputHatch;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;

import java.util.*;

/**
 * 由核心方块、物品舱室和能源舱室共同组成的多方块生产结构。
 *
 * <p>核心定期检查周围方块是否匹配某个 {@link LevelStruct}。结构成形后，
 * 核心从指定输入仓汇总原料、从指定能源仓按 tick 消耗能量，并把完成的产物
 * 写入指定输出仓。核心自身不保存物品或能量。</p>
 */
public class MultiblockStructer extends Block {

    /**
     * 打开或关闭多方块结构预览的可重绑定按键。
     *
     * <p>该按键会自动出现在游戏的“设置 → 控制”列表中，默认值为 K。
     * 绑定名称和分类名称可通过语言包中的
     * {@code keybind.mdtnh_multiblock_preview.name} 与
     * {@code category.mdtnh.name} 本地化。</p>
     */
    public static final KeyBind structurePreviewKey =
            KeyBind.add("mdtnh_multiblock_preview", KeyCode.k, "mdtnh");

    /** 新建筑首次显示结构预览时使用的等级。 */
    public int defaultPreviewLevel = 1;

    /** 结构幽灵方块的不透明度。 */
    public float previewAlpha = 0.55f;

    /**
     * 预览只绘制缺失/不匹配的槽位，还是绘制整个结构的全部槽位。
     *
     * <p>结构已经完全成形时，所有槽位都已满足，若只绘制缺失槽位则幽灵方块
     * 列表为空，预览将什么都看不见；此时应设为 false 以显示整个结构轮廓。</p>
     */
    public boolean showMissingOnly = false;

    /** 是否输出结构幽灵方块预览相关的调试日志。 */
    public boolean debugPreview = true;

    /** 核心方块使用的图集区域。 */
    public TextureRegion region;

    /** 并行数 */
    public int parallel=8;

    /**
     * GTNH 旧式多方块输入电压聚合：true 时把所有能源仓标称输入电压相加后判级，
     * 因而会出现“双仓升压”；false 时使用能源仓平均电压。
     */
    public boolean gtNhLegacyVoltageAggregation = true;



    // ====== 绘图管理器 ======
    private DrawerManager drawerManager = new DrawerManager();

    public void setDrawer(DrawBlock drawer) {
        drawerManager.setDrawer(drawer);
    }

    public DrawBlock getDrawer() {
        return drawerManager.getDrawer();
    }

    public static class Recipe {
        public ItemStack[] inputItems;
        public LiquidStack[] inputLiquids;
        public ItemStack[] outputItems;
        public LiquidStack[] outputLiquids;
        public float craftTime;
        public float energyPerCraftJ;
        /** 该配方允许启动的最低电压等级。 */
        public VoltageTier minimumVoltageTier = VoltageTier.ULV;

        public Recipe(ItemStack[] inputItems, LiquidStack[] inputLiquids,
                      ItemStack[] outputItems, LiquidStack[] outputLiquids, float craftTime) {
            this(inputItems, inputLiquids, outputItems, outputLiquids, craftTime, 0f);
        }

        public Recipe(ItemStack[] inputItems, LiquidStack[] inputLiquids,
                      ItemStack[] outputItems, LiquidStack[] outputLiquids,
                      float craftTime, float energyPerCraftJ) {
            this.inputItems = inputItems;
            this.inputLiquids = inputLiquids;
            this.outputItems = outputItems;
            this.outputLiquids = outputLiquids;
            this.craftTime = craftTime;
            this.energyPerCraftJ = energyPerCraftJ;
        }

        public Recipe energy(float joules) {
            this.energyPerCraftJ = Math.max(0f, joules);
            return this;
        }

        public Recipe voltage(VoltageTier tier) {
            this.minimumVoltageTier = tier == null ? VoltageTier.ULV : tier;
            return this;
        }

        public static Recipe items(ItemStack[] in, ItemStack out, float time) {
            return new Recipe(in, null, out == null ? null : new ItemStack[]{out}, null, time);
        }

        public static Recipe items(ItemStack[] in, ItemStack[] out, float time) {
            return new Recipe(in, null, out, null, time);
        }

        public static Recipe withLiquid(ItemStack[] in, LiquidStack[] liqIn,
                                        ItemStack[] out, LiquidStack[] liqOut, float time) {
            return new Recipe(in, liqIn, out, liqOut, time);
        }

        public String primaryOutputName() {
            if (outputItems != null && outputItems.length > 0) return outputItems[0].item.localizedName;
            if (outputLiquids != null && outputLiquids.length > 0) return outputLiquids[0].liquid.localizedName;
            return null;
        }

        public Recipe times(int count) {
            int multiplier = Math.max(0, count);
            Recipe scaled = new Recipe(
                    scaleItems(inputItems, multiplier),
                    scaleLiquids(inputLiquids, multiplier),
                    scaleItems(outputItems, multiplier),
                    scaleLiquids(outputLiquids, multiplier),
                    craftTime,
                    energyPerCraftJ * multiplier
            );
            scaled.minimumVoltageTier = minimumVoltageTier;
            return scaled;
        }

        private static ItemStack[] scaleItems(ItemStack[] stacks, int multiplier) {
            if (stacks == null) return null;
            ItemStack[] scaled = new ItemStack[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                scaled[i] = new ItemStack(stacks[i].item, safeMultiply(stacks[i].amount, multiplier));
            }
            return scaled;
        }

        private static LiquidStack[] scaleLiquids(LiquidStack[] stacks, int multiplier) {
            if (stacks == null) return null;
            LiquidStack[] scaled = new LiquidStack[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                scaled[i] = new LiquidStack(stacks[i].liquid, stacks[i].amount * multiplier);
            }
            return scaled;
        }

        private static int safeMultiply(int amount, int multiplier) {
            long result = (long) amount * multiplier;
            return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
        }
    }

    public static class RecipeGroup {
        public String name;
        public TextureRegion icon;
        public String Texture_name;
        public Recipe[] recipes;

        public RecipeGroup(String name, Recipe[] recipes) {
            this.name = name;
            this.recipes = recipes;
        }
    }

    public static class LevelStruct {
        public Map<pos, Integer> struct;
        public List<List<Block>> Mapping;
        public Recipe recipe;
    }

    public List<LevelStruct> levels = new ArrayList<>();
    public RecipeGroup[] groups = new RecipeGroup[]{};

    public MultiblockStructer(String name) {
        super(name);
        rotate = true;
        update = true;
        solid = true;
        buildType = MultiblockStructerBuilding::new;

        configurable = true;
        saveConfig = true;
        copyConfig = true;

        config(Integer.class, (MultiblockStructerBuilding build, Integer groupIdx) -> {
            build.selectedGroup = groupIdx;
            build.currentRecipe = -1;
            build.currentParallel = 0;
            build.progress = 0f;
        });

        ensurePreviewDrawHook();
    }

    private static boolean previewDrawHookRegistered;

    private void ensurePreviewDrawHook() {
        if (previewDrawHookRegistered) return;
        previewDrawHookRegistered = true;

        Events.run(EventType.Trigger.postDraw, () -> {
            if (Vars.state.isMenu()) return;
            for (Building b : Groups.build) {
                if (b instanceof MultiblockStructerBuilding mb
                        && mb.structurePreviewVisible
                        && mb.tile != null) {
                    mb.drawStructurePreview();
                }
            }
        });
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(name);
    }

    @Override
    public void drawBase(Tile tile) {
        Draw.rect(region, tile.worldx(), tile.worldy());
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Seq<BuildPlan> plans = new Seq<>();
        list.each(plans::add);
        drawerManager.drawPlan(this, plan, plans);
    }

    public static class pos {
        public int x, y;
        public pos() {}
        public pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            pos pos = (pos) o;
            return x == pos.x && y == pos.y;
        }
        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("level", (MultiblockStructerBuilding build) -> new Bar(
                () -> build.Molded ? Core.bundle.format("mdt.level.bar", build.level) : Core.bundle.get("mdt.level.unformed", "未成形"),
                () -> Pal.accent,
                () -> 1f
        ));

        addBar("progress", (MultiblockStructerBuilding build) -> new Bar(
                () -> {
                    if (!build.Molded || build.selectedGroup < 0 || build.selectedGroup >= groups.length)
                        return Core.bundle.get("mdt.progress.idle", "空闲");
                    RecipeGroup group = groups[build.selectedGroup];
                    String groupName = Core.bundle.get("group." + group.name, group.name);
                    if (build.currentRecipe >= 0 && build.currentRecipe < group.recipes.length) {
                        Recipe r = group.recipes[build.currentRecipe];
                        String itemName = r.primaryOutputName();
                        if (itemName == null) itemName = "???";
                        return Core.bundle.format("mdt.progress.multiblock",
                                groupName, itemName, build.currentParallel, (int)(build.progress * 100));
                    }
                    return groupName + " - " + Core.bundle.get("mdt.progress.idle", "空闲");
                },
                () -> Pal.accent,
                () -> build.Molded ? build.progress : 0f
        ));
    }

    public class MultiblockStructerBuilding extends Building {
        public boolean Molded;
        public int level;
        public float progress;
        public int selectedGroup = -1;
        public int currentRecipe = -1;
        public int currentParallel = 0;
        public pos[] currentInputs;
        public pos[] currentOutputs;
        public pos[] currentEnergyInputs;
        public pos[] currentLiquidInputs;
        public pos[] currentLiquidOutputs;

        public boolean structurePreviewVisible;
        public int structurePreviewLevel = defaultPreviewLevel;
        private boolean previewLevelAdjustedDuringHold;
        private boolean previewKeyPressActive;
        private boolean previewKeyDownLastFrame;
        private boolean previewWasVisibleBeforePress;
        private int lastLoggedPreviewSignature = -1;
        private int lastPreviewEntrySignature = -1;
        private int lastCheckedRotation = -1;

        private final Seq<BuildPlan> structurePreviewPlans = new Seq<>();

        private pos rotateOffset(pos offset) {
            int dx = offset.x;
            int dy = offset.y;
            switch (rotation & 3) {
                case 1:  return new pos(dy, -dx);
                case 2:  return new pos(-dx, -dy);
                case 3:  return new pos(-dy, dx);
                default: return new pos(dx, dy);
            }
        }

        public void CheckStruct() {
            level = 0;
            Molded = false;

            for (int i = 1; i <= levels.size(); i++) {
                LevelStruct now = levels.get(i - 1);
                boolean accept = true;

                List<pos> foundInputs = new ArrayList<>();
                List<pos> foundOutputs = new ArrayList<>();
                List<pos> foundEnergyInputs = new ArrayList<>();
                List<pos> foundLiquidInputs = new ArrayList<>();
                List<pos> foundLiquidOutputs = new ArrayList<>();

                for (Map.Entry<pos, Integer> ps : now.struct.entrySet()) {
                    pos worldOffset = rotateOffset(ps.getKey());
                    int dx = worldOffset.x;
                    int dy = worldOffset.y;
                    Tile checkTile = Vars.world.tile(tile.x + dx, tile.y + dy);

                    if (checkTile == null) {
                        accept = false;
                        break;
                    }

                    Block blockThere = checkTile.block();
                    int typeIndex = ps.getValue();
                    List<Block> allowed = now.Mapping.get(typeIndex);

                    if (allowed == null || !allowed.contains(blockThere)) {
                        accept = false;
                        break;
                    }

                    if (blockThere instanceof Hatch) {
                        if (blockThere instanceof ItemInputHatch) {
                            foundInputs.add(new pos(dx, dy));
                        } else if (blockThere instanceof ItemOutputHatch) {
                            foundOutputs.add(new pos(dx, dy));
                        } else if (blockThere instanceof EnergyInputHatch) {
                            foundEnergyInputs.add(new pos(dx, dy));
                        } else if (blockThere instanceof LiquidInputHatch) {
                            foundLiquidInputs.add(new pos(dx, dy));
                        } else if (blockThere instanceof LiquidOutputHatch) {
                            foundLiquidOutputs.add(new pos(dx, dy));
                        }
                    }
                }

                if (accept) {
                    level = i;
                    Molded = true;

                    currentInputs = foundInputs.toArray(new pos[0]);
                    currentOutputs = foundOutputs.toArray(new pos[0]);
                    currentEnergyInputs = foundEnergyInputs.toArray(new pos[0]);
                    currentLiquidInputs = foundLiquidInputs.toArray(new pos[0]);
                    currentLiquidOutputs = foundLiquidOutputs.toArray(new pos[0]);
                    break;
                }
            }
            if (!Molded) {
                currentInputs = null;
                currentOutputs = null;
                currentEnergyInputs = null;
                currentLiquidInputs = null;
                currentLiquidOutputs = null;
            }
        }

        private LevelStruct currentLevel() {
            return (level > 0 && level <= levels.size()) ? levels.get(level - 1) : null;
        }

        private int takeFromInputs(Item item, int amount) {
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentInputs == null) return 0;

            int remaining = amount;
            for (pos offset : currentInputs) {
                if (remaining <= 0) break;
                Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (t != null && t.build != null && t.build.block.hasItems) {
                    int canTake = Math.min(t.build.items.get(item), remaining);
                    t.build.items.remove(item, canTake);
                    remaining -= canTake;
                }
            }
            return amount - remaining;
        }

        private boolean inputsHave(ItemStack[] items) {
            if (items == null || items.length == 0) return true;
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentInputs == null) return false;

            Map<Item, Integer> needed = new HashMap<>();
            for (ItemStack stack : items) {
                needed.merge(stack.item, stack.amount, Integer::sum);
            }

            Map<Item, Integer> available = new HashMap<>();
            for (pos offset : currentInputs) {
                Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (t != null && t.build != null && t.build.block.hasItems) {
                    for (Item item : needed.keySet()) {
                        available.merge(item, t.build.items.get(item), Integer::sum);
                    }
                }
            }

            for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
                if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        private int putToOutputs(Item item, int amount) {
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentOutputs == null) return 0;

            int remaining = amount;
            for (pos offset : currentOutputs) {
                if (remaining <= 0) break;
                Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (t != null && t.build != null && t.build.block.hasItems) {
                    int space = t.build.block.itemCapacity - t.build.items.get(item);
                    int canPut = Math.min(space, remaining);
                    if (canPut > 0) {
                        t.build.items.add(item, canPut);
                        remaining -= canPut;
                    }
                }
            }
            return amount - remaining;
        }

        private boolean outputsFullFor(Item item, int amount) {
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentOutputs == null) return true;

            int totalSpace = 0;
            for (pos offset : currentOutputs) {
                Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (t != null && t.build != null && t.build.block.hasItems) {
                    totalSpace += Math.max(0, t.build.block.itemCapacity - t.build.items.get(item));
                }
            }
            return totalSpace < amount;
        }

        private float takeLiquidFromInputs(Liquid liquid, float amount) {
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentLiquidInputs == null) return 0f;

            float remaining = amount;
            for (pos offset : currentLiquidInputs) {
                if (remaining <= 0.001f) break;
                Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (t != null && t.build != null && t.build.block.hasLiquids) {
                    float canTake = Math.min(t.build.liquids.get(liquid), remaining);
                    if (canTake > 0.001f) {
                        t.build.liquids.remove(liquid, canTake);
                        remaining -= canTake;
                    }
                }
            }
            return amount - remaining;
        }

        private float putLiquidToOutputs(Liquid liquid, float amount) {
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentLiquidOutputs == null) return 0f;

            float remaining = amount;
            for (pos offset : currentLiquidOutputs) {
                if (remaining <= 0.001f) break;
                Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (t != null && t.build != null && t.build.block.hasLiquids) {
                    float space = t.build.block.liquidCapacity - t.build.liquids.get(liquid);
                    float canPut = Math.min(space, remaining);
                    if (canPut > 0.001f) {
                        t.build.liquids.add(liquid, canPut);
                        remaining -= canPut;
                    }
                }
            }
            return amount - remaining;
        }

        private boolean inputsHaveForParallel(Recipe recipe, int parallelCount) {
            if (parallelCount <= 0) return false;
            if (currentLevel() == null) return false;

            boolean needItems = recipe.inputItems != null && recipe.inputItems.length > 0;
            boolean needLiquids = recipe.inputLiquids != null && recipe.inputLiquids.length > 0;
            if (!needItems && !needLiquids) return true;
            if (needItems && currentInputs == null) return false;
            if (needLiquids && currentLiquidInputs == null) return false;

            if (needItems) {
                Map<Item, Long> needed = new HashMap<>();
                for (ItemStack stack : recipe.inputItems) {
                    long required = (long) stack.amount * parallelCount;
                    needed.merge(stack.item, required, Long::sum);
                }

                Map<Item, Long> available = new HashMap<>();
                for (pos offset : currentInputs) {
                    Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                    if (t == null || t.build == null || !t.build.block.hasItems) continue;

                    for (Item item : needed.keySet()) {
                        available.merge(item, (long) t.build.items.get(item), Long::sum);
                    }
                }

                for (Map.Entry<Item, Long> entry : needed.entrySet()) {
                    if (available.getOrDefault(entry.getKey(), 0L) < entry.getValue()) {
                        return false;
                    }
                }
            }

            if (needLiquids) {
                Map<Liquid, Double> needed = new HashMap<>();
                for (LiquidStack stack : recipe.inputLiquids) {
                    double required = (double) stack.amount * parallelCount;
                    needed.merge(stack.liquid, required, Double::sum);
                }

                Map<Liquid, Double> available = new HashMap<>();
                for (pos offset : currentLiquidInputs) {
                    Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                    if (t == null || t.build == null || !t.build.block.hasLiquids) continue;

                    for (Liquid liquid : needed.keySet()) {
                        available.merge(liquid, (double) t.build.liquids.get(liquid), Double::sum);
                    }
                }

                for (Map.Entry<Liquid, Double> entry : needed.entrySet()) {
                    if (available.getOrDefault(entry.getKey(), 0d) + 0.0001 < entry.getValue()) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean outputsHaveSpaceForParallel(Recipe recipe, int parallelCount) {
            if (parallelCount <= 0) return false;
            if (currentLevel() == null) return false;

            boolean needItems = recipe.outputItems != null && recipe.outputItems.length > 0;
            boolean needLiquids = recipe.outputLiquids != null && recipe.outputLiquids.length > 0;
            if (!needItems && !needLiquids) return true;
            if (needItems && currentOutputs == null) return false;
            if (needLiquids && currentLiquidOutputs == null) return false;

            if (needItems) {
                for (ItemStack out : recipe.outputItems) {
                    long requiredSpace = (long) out.amount * parallelCount;
                    long totalSpace = 0L;

                    for (pos offset : currentOutputs) {
                        Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                        if (t != null && t.build != null && t.build.block.hasItems) {
                            totalSpace += Math.max(0, t.build.block.itemCapacity - t.build.items.get(out.item));
                        }
                    }
                    if (totalSpace < requiredSpace) return false;
                }
            }

            if (needLiquids) {
                for (LiquidStack out : recipe.outputLiquids) {
                    double requiredSpace = (double) out.amount * parallelCount;
                    double totalSpace = 0d;

                    for (pos offset : currentLiquidOutputs) {
                        Tile t = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                        if (t != null && t.build != null && t.build.block.hasLiquids) {
                            totalSpace += Math.max(
                                    0,
                                    t.build.block.liquidCapacity - t.build.liquids.get(out.liquid)
                            );
                        }
                    }
                    if (totalSpace + 0.0001 < requiredSpace) return false;
                }
            }
            return true;
        }

        private boolean canRunParallel(Recipe recipe, int parallelCount) {
            return inputsHaveForParallel(recipe, parallelCount)
                    && outputsHaveSpaceForParallel(recipe, parallelCount);
        }

        private int findMaximumParallel(Recipe recipe) {
            int low = 1;
            int high = Math.max(0, parallel);
            int best = 0;

            while (low <= high) {
                int middle = low + ((high - low) >> 1);
                if (canRunParallel(recipe, middle)) {
                    best = middle;
                    low = middle + 1;
                } else {
                    high = middle - 1;
                }
            }
            return best;
        }

        private int parallelAmount(int amount, int parallelCount) {
            long result = (long) amount * parallelCount;
            return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
        }

        private float availableEnergyJ() {
            LevelStruct lvl = currentLevel();
            if (lvl == null || currentEnergyInputs == null) return 0f;

            float total = 0f;
            for (pos offset : currentEnergyInputs) {
                Tile target = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (target == null || target.build == null) continue;
                if (target.build.team != team) continue;
                if (target.build instanceof MdtEnergyNode) {
                    MdtEnergyNode node = (MdtEnergyNode) target.build;
                    total += node.energyState().energyJ;
                }
            }
            return total;
        }

        private boolean consumeEnergyJ(float amountJ) {
            if (amountJ <= 0f) return true;
            if (availableEnergyJ() + 0.0001f < amountJ) return false;

            LevelStruct lvl = currentLevel();
            float remaining = amountJ;

            for (pos offset : currentEnergyInputs) {
                if (remaining <= 0.0001f) break;
                Tile target = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (target == null || target.build == null) continue;
                if (target.build.team != team) continue;
                if (!(target.build instanceof MdtEnergyNode)) continue;

                MdtEnergyNode node = (MdtEnergyNode) target.build;
                EnergyState state = node.energyState();
                float taken = Math.min(state.energyJ, remaining);
                state.energyJ -= taken;
                remaining -= taken;
            }
            return remaining <= 0.0001f;
        }

        /**
         * 返回当前结构全部能源仓聚合后的有效输入电压。
         * 旧式 GTNH 模式使用求和；兼容模式使用平均值。
         */
        public float getEffectiveInputVoltageV() {
            if (currentLevel() == null || currentEnergyInputs == null
                    || currentEnergyInputs.length == 0) return 0f;

            float sum = 0f;
            int count = 0;
            for (pos offset : currentEnergyInputs) {
                Tile target = Vars.world.tile(tile.x + offset.x, tile.y + offset.y);
                if (target == null || target.build == null) continue;
                if (target.build.team != team) continue;
                if (!(target.build instanceof MdtEnergyNode)) continue;

                MdtEnergyNode node = (MdtEnergyNode) target.build;
                EnergySpec spec = node.energySpec();
                if (spec == null || spec.voltageV <= 0f) continue;
                sum += spec.voltageV;
                count++;
            }

            if (count == 0) return 0f;
            return gtNhLegacyVoltageAggregation ? sum : sum / count;
        }

        public VoltageTier getEffectiveInputVoltageTier() {
            float voltage = getEffectiveInputVoltageV();
            VoltageTier[] tiers = VoltageTier.values();
            if (tiers.length == 0) return null;

            for (VoltageTier tier : tiers) {
                if (voltage <= tier.maxVoltageV + 0.0001f) {
                    return tier;
                }
            }
            return tiers[tiers.length - 1];
        }

        private boolean voltageAllows(Recipe recipe) {
            if (recipe == null) return false;

            // 旧的零能耗配方保持无电运行兼容性。
            if (recipe.energyPerCraftJ <= 0f
                    && (recipe.minimumVoltageTier == null
                    || recipe.minimumVoltageTier == VoltageTier.ULV)) {
                return true;
            }

            VoltageTier effective = getEffectiveInputVoltageTier();
            VoltageTier minimum = recipe.minimumVoltageTier == null
                    ? VoltageTier.ULV
                    : recipe.minimumVoltageTier;
            return effective != null
                    && getEffectiveInputVoltageV() > 0f
                    && effective.canProcess(minimum);
        }

        private float effectiveCraftTime(Recipe recipe) {
            VoltageTier minimum = recipe.minimumVoltageTier == null
                    ? VoltageTier.ULV
                    : recipe.minimumVoltageTier;
            VoltageTier effective = getEffectiveInputVoltageTier();
            if (effective == null || !effective.canProcess(minimum)) {
                return recipe.craftTime;
            }
            return recipe.craftTime / effective.speedMultiplierFrom(minimum);
        }

        private float effectiveEnergyPerCraft(Recipe recipe) {
            VoltageTier minimum = recipe.minimumVoltageTier == null
                    ? VoltageTier.ULV
                    : recipe.minimumVoltageTier;
            VoltageTier effective = getEffectiveInputVoltageTier();
            if (effective == null || !effective.canProcess(minimum)) {
                return recipe.energyPerCraftJ;
            }
            return recipe.energyPerCraftJ * effective.energyMultiplierFrom(minimum);
        }

        @Override
        public void updateTile() {
            super.updateTile();

            handleStructurePreviewInput();

            if (timer(0, 60f) || rotation != lastCheckedRotation) {
                lastCheckedRotation = rotation;
                boolean wasMolded = Molded;
                int oldLevel = level;
                CheckStruct();
                if (!Molded || level != oldLevel) {
                    progress = 0f;
                    currentRecipe = -1;
                    currentParallel = 0;
                }
            }

            if (!Molded) return;
            if (groups.length == 0) return;
            if (selectedGroup < 0 || selectedGroup >= groups.length) return;

            Recipe[] activeRecipes = groups[selectedGroup].recipes;
            if (activeRecipes.length == 0) return;

            if (currentRecipe >= 0 && currentRecipe < activeRecipes.length) {
                Recipe active = activeRecipes[currentRecipe];
                if (!voltageAllows(active) || !canRunParallel(active, 1)) {
                    currentRecipe = -1;
                    currentParallel = 0;
                }
            }

            if (currentRecipe == -1) {
                for (int i = 0; i < activeRecipes.length; i++) {
                    Recipe recipe = activeRecipes[i];
                    if (!voltageAllows(recipe)) continue;
                    int maximum = findMaximumParallel(recipe);
                    if (maximum > 0) {
                        currentRecipe = i;
                        currentParallel = maximum;
                        progress = 0f;
                        break;
                    }
                }
            }

            if (currentRecipe >= 0 && currentRecipe < activeRecipes.length) {
                Recipe active = activeRecipes[currentRecipe];

                if (currentParallel <= 0 || !canRunParallel(active, currentParallel)) {
                    int maximum = findMaximumParallel(active);
                    if (maximum <= 0) {
                        currentRecipe = -1;
                        currentParallel = 0;
                        progress = 0f;
                        return;
                    }

                    currentParallel = maximum;
                    progress = 0f;
                }

                if (!voltageAllows(active)) {
                    currentRecipe = -1;
                    currentParallel = 0;
                    progress = 0f;
                    return;
                }

                float workTicks = delta();
                float runCraftTime = Math.max(0.0001f, effectiveCraftTime(active));
                float runEnergyPerCraft = Math.max(0f, effectiveEnergyPerCraft(active));
                float requiredEnergyJ = runEnergyPerCraft
                        * workTicks
                        / runCraftTime
                        * currentParallel;

                if (consumeEnergyJ(requiredEnergyJ)) {
                    progress += workTicks / runCraftTime;
                }

                if (progress >= 1f) {
                    if (!canRunParallel(active, currentParallel)) {
                        progress = 0f;
                        currentParallel = 0;
                        return;
                    }

                    if (active.inputItems != null) {
                        for (ItemStack stack : active.inputItems) {
                            takeFromInputs(
                                    stack.item,
                                    parallelAmount(stack.amount, currentParallel)
                            );
                        }
                    }

                    if (active.inputLiquids != null) {
                        for (LiquidStack stack : active.inputLiquids) {
                            takeLiquidFromInputs(stack.liquid, stack.amount * currentParallel);
                        }
                    }

                    if (active.outputItems != null) {
                        for (ItemStack stack : active.outputItems) {
                            putToOutputs(
                                    stack.item,
                                    parallelAmount(stack.amount, currentParallel)
                            );
                        }
                    }

                    if (active.outputLiquids != null) {
                        for (LiquidStack stack : active.outputLiquids) {
                            putLiquidToOutputs(stack.liquid, stack.amount * currentParallel);
                        }
                    }

                    progress = 0f;
                    currentParallel = 0;
                }
            } else {
                currentParallel = 0;
                progress = 0f;
            }
        }

        private boolean mouseHoveredOverCore() {
            if (Vars.headless || Core.input == null || tile == null) return false;

            Building hovered = Vars.world.buildWorld(
                    Core.input.mouseWorldX(),
                    Core.input.mouseWorldY()
            );
            return hovered == this;
        }

        private int effectiveStructurePreviewLevel() {
            int maximum = levels == null ? 0 : levels.size();
            if (maximum <= 0) return 0;

            structurePreviewLevel = Math.max(
                    1,
                    Math.min(structurePreviewLevel, maximum)
            );
            return structurePreviewLevel;
        }

        private void handleStructurePreviewInput() {
            if (Vars.headless || Core.input == null
                    || !Vars.state.isGame()
                    || Core.scene != null && (Core.scene.hasField() || Core.scene.hasDialog())) {
                previewKeyDownLastFrame = false;
                return;
            }

            boolean keyDown = Core.input.keyDown(structurePreviewKey);
            boolean justPressed = keyDown && !previewKeyDownLastFrame;
            boolean justReleased = !keyDown && previewKeyDownLastFrame;
            previewKeyDownLastFrame = keyDown;

            if (debugPreview && (justPressed || justReleased)) {
                Log.info("[mdtnh-preview] 按键边沿 @:@ 按下=@ 按住=@", tile.x, tile.y, justPressed, keyDown);
            }

            if (justPressed && mouseHoveredOverCore()) {
                previewKeyPressActive = true;
                previewLevelAdjustedDuringHold = false;
                previewWasVisibleBeforePress = structurePreviewVisible;
                structurePreviewVisible = true;

                if (debugPreview) {
                    Log.info("[mdtnh-preview] 开始按压 @:@ 按压前可见=@", tile.x, tile.y, previewWasVisibleBeforePress);
                }
            }

            if (previewKeyPressActive && keyDown) {
                boolean increase = Core.input.keyTap(KeyCode.plus)
                        || (Core.input.shift() && Core.input.keyTap(KeyCode.equals));
                boolean decrease = Core.input.keyTap(KeyCode.minus);

                if (increase || decrease) {
                    int maximum = levels == null ? 0 : levels.size();
                    if (maximum > 0) {
                        int delta = increase ? 1 : -1;
                        structurePreviewLevel = Math.max(
                                1,
                                Math.min(structurePreviewLevel + delta, maximum)
                        );
                        structurePreviewVisible = true;

                        if (debugPreview) {
                            Log.info("[mdtnh-preview] 调整等级 @:@ @ -> @/@",
                                    tile.x, tile.y,
                                    delta > 0 ? "+1" : "-1",
                                    structurePreviewLevel, maximum);
                        }
                    } else if (debugPreview) {
                        Log.warn("[mdtnh-preview] 尝试调整等级但未定义任何结构等级 @:@", tile.x, tile.y);
                    }

                    previewLevelAdjustedDuringHold = true;
                }
            }

            if (previewKeyPressActive && justReleased) {
                if (!previewLevelAdjustedDuringHold) {
                    structurePreviewVisible = !previewWasVisibleBeforePress;
                }

                if (debugPreview) {
                    Log.info("[mdtnh-preview] 结束按压 @:@ 可见=@ 曾调级=@",
                            tile.x, tile.y, structurePreviewVisible, previewLevelAdjustedDuringHold);
                }

                previewKeyPressActive = false;
                previewLevelAdjustedDuringHold = false;
            }
        }

        private void rebuildStructurePreviewPlans(int previewLevel) {
            structurePreviewPlans.clear();
            if (previewLevel <= 0 || previewLevel > levels.size()) {
                logPreviewDiagnostic("等级无效", previewLevel, 0);
                return;
            }

            LevelStruct definition = levels.get(previewLevel - 1);
            if (definition == null || definition.struct == null || definition.Mapping == null) {
                logPreviewDiagnostic("等级缺少结构定义", previewLevel, 0);
                return;
            }

            for (Map.Entry<pos, Integer> entry : definition.struct.entrySet()) {
                pos offset = entry.getKey();
                Integer mappingIndex = entry.getValue();

                if (offset == null || mappingIndex == null
                        || mappingIndex < 0
                        || mappingIndex >= definition.Mapping.size()) {
                    continue;
                }

                List<Block> candidates = definition.Mapping.get(mappingIndex);
                if (candidates == null || candidates.isEmpty()) continue;

                Block previewBlock = candidates.get(0);
                if (previewBlock == null || previewBlock.isAir()) continue;

                if (offset.x == 0 && offset.y == 0
                        && previewBlock == MultiblockStructer.this) {
                    continue;
                }

                pos worldOffset = rotateOffset(offset);
                int planX = tile.x + worldOffset.x;
                int planY = tile.y + worldOffset.y;
                Tile existing = Vars.world.tile(planX, planY);

                if (showMissingOnly && slotSatisfied(existing, candidates)) continue;

                BuildPlan plan = new BuildPlan(planX, planY, 0, previewBlock, null);
                plan.worldContext = true;
                plan.animScale = 1f;
                structurePreviewPlans.add(plan);
            }

            logPreviewDiagnostic("生成幽灵方块", previewLevel, structurePreviewPlans.size);
        }

        private boolean slotSatisfied(Tile existing, List<Block> candidates) {
            if (existing == null || existing.block() == null) return false;
            for (Block allowed : candidates) {
                if (existing.block() == allowed) return true;
            }
            return false;
        }

        private void logPreviewDiagnostic(String message, int level, int count) {
            if (!debugPreview) return;

            int rot = rotation & 3;
            int signature = (structurePreviewVisible ? 1 : 0) * 100000 + level * 1000 + rot * 100 + count;
            if (signature == lastLoggedPreviewSignature) return;
            lastLoggedPreviewSignature = signature;

            Log.info("[mdtnh-preview] @ @:@ 可见=@ 等级=@ 旋转=@ 计划数=@",
                    message,
                    tile == null ? -1 : tile.x,
                    tile == null ? -1 : tile.y,
                    structurePreviewVisible,
                    level,
                    rot,
                    count);
        }

        private void drawStructurePreview() {
            logPreviewEntry();

            if (!structurePreviewVisible || tile == null) return;

            int previewLevel = effectiveStructurePreviewLevel();
            if (previewLevel <= 0) {
                logPreviewDiagnostic("预览已开启但有效等级为 0", previewLevel, 0);
                return;
            }

            rebuildStructurePreviewPlans(previewLevel);
            logPreviewDiagnostic("绘制幽灵方块", previewLevel, structurePreviewPlans.size);

            float alpha = Math.max(0f, Math.min(1f, previewAlpha));

            try {
                float previousZ = Draw.z();
                Draw.z(Layer.plans);
                for (BuildPlan plan : structurePreviewPlans) {
                    if (plan == null || plan.block == null) continue;
                    plan.block.drawPlan(plan, structurePreviewPlans, true, alpha);
                }
                Draw.z(previousZ);
            } catch (Throwable t) {
                Log.err("[mdtnh-preview] 绘制幽灵方块时发生异常 @:@", tile.x, tile.y);
                Log.err(t);
            }

            Draw.reset();

            if (mouseHoveredOverCore()) {
                Drawf.text(
                        Core.bundle.format("mdtnh.multiblock.preview.level",
                                previewLevel, levels.size()),
                        x,
                        y + block.size * Vars.tilesize / 2f + 10f,
                        Pal.accent,
                        0.75f
                );
            }
        }

        private void logPreviewEntry() {
            if (!debugPreview || tile == null) return;

            int level = effectiveStructurePreviewLevel();
            int rot = rotation & 3;
            int signature = (structurePreviewVisible ? 1 : 0) * 10000 + level * 100 + rot;
            if (signature == lastPreviewEntrySignature) return;
            lastPreviewEntrySignature = signature;

            Log.info("[mdtnh-preview] 进入绘制 @:@ 可见=@ 等级=@ 旋转=@ 计划数=@",
                    tile.x, tile.y, structurePreviewVisible, level, rot, structurePreviewPlans.size);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.clear();
            if (groups.length == 0) return;

            TextureRegion errorRegion = Core.atlas.find("error");
            String modName = Vars.mods.getMod(MainMod.class).name;

            for (int i = 0; i < groups.length; i++) {
                final int idx = i;
                RecipeGroup group = groups[i];

                TextureRegion icon = null;
                if (group.Texture_name != null && !group.Texture_name.isEmpty()) {
                    String atlasName = modName + "-" + group.Texture_name;
                    TextureRegion loaded = Core.atlas.find(atlasName);
                    if (loaded != null && loaded != errorRegion) {
                        icon = loaded;
                        Log.info("Group @ loaded custom icon: @", i, atlasName);
                    } else {
                        Log.warn("Group @ failed to load icon '@'; atlas has region: @", i, atlasName, Core.atlas.has(atlasName));
                    }
                }

                if (icon == null && group.recipes != null && group.recipes.length > 0) {
                    Recipe first = group.recipes[0];
                    if (first.outputItems != null && first.outputItems.length > 0) {
                        icon = first.outputItems[0].item.uiIcon;
                    } else if (first.outputLiquids != null && first.outputLiquids.length > 0) {
                        icon = first.outputLiquids[0].liquid.uiIcon;
                    }
                }

                if (icon == null || icon == errorRegion) icon = errorRegion;
                group.icon = icon;

                TextureRegionDrawable drawable = new TextureRegionDrawable(icon);
                ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(Styles.defaulti);
                style.imageUp = drawable;
                style.imageChecked = drawable;
                style.imageDisabled = drawable;

                ImageButton button = new ImageButton(style);
                button.clicked(() -> configure(idx));
                button.setChecked(idx == selectedGroup);

                table.add(button).size(50f).pad(4f);
                table.add(Core.bundle.get("group." + group.name, group.name)).pad(4f);
                table.row();
            }
        }

        @Override
        public Object config() {
            return selectedGroup;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(selectedGroup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) selectedGroup = read.i();
        }

        @Override
        public void draw() {
            drawerManager.drawBuilding(this);
        }

        @Override
        public void drawLight() {
            drawerManager.drawLight(this);
        }
    }
}