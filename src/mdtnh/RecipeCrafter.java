package mdtnh;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mdtnh.energy.EnergySpec;
import mdtnh.energy.EnergyState;
import mdtnh.energy.MdtEnergyNode;
import mdtnh.energy.SteamEnergyConverter;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;
import mindustry.world.draw.*;

import java.util.*;

/**
 * 支持配方分组、物品/液体输入输出和 MDT 能源消耗的单方块工厂。
 *
 * <p>方块定义保存可选配方组与能源规格；每个已放置建筑保存当前选中的配方组、
 * 正在执行的配方、生产进度和独立能源缓存。建筑通过 {@link MdtEnergyNode}
 * 接入离散能源网络，不使用 Mindustry 原生电力模块。</p>
 */
public class RecipeCrafter extends GenericCrafter {
    /** 预留的编程电路图标数组；当前配方组界面主要通过 Texture_name 动态读取图标。 */
    public TextureRegion[] Programming_circuit = new TextureRegion[24];

    /**
     * 未使用分组时的默认配方数组。
     *
     * <p>{@link #load()} 会在 groups 为空时把该数组包装成名为 default 的配方组。</p>
     */
    public Recipe[] recipes = new Recipe[]{};

    /** 配方分组；配置界面通过组索引切换当前可执行配方集合。 */
    public RecipeGroup[] groups = new RecipeGroup[]{};

    /** 工厂内部缓存的充能来源。 */
    public enum EnergySource {
        electricity,
        steam,
        manual
    }

    /** 默认由 MDT 导线网络充电；蒸汽机器会改为 {@link EnergySource#steam}。 */
    public EnergySource energySource = EnergySource.electricity;

    /** 蒸汽模式接受并转换的液体。 */
    public Liquid steamLiquid;

    /** 每消耗一单位蒸汽可写入内部缓存的能量，单位为焦耳。 */
    public float joulesPerSteamUnit = 10f;

    /** 每个模拟秒最多转换的蒸汽量。 */
    public float maxSteamUsePerSecond = 1f;

    /** 该工厂类型共享的输出电压、输入电压区间、内部容量和电流上限。 */
    public final EnergySpec energySpec = new EnergySpec();

    /** 新放置工厂的初始能源缓存比例；通常为 0，由外部网络充电。 */
    public float initialEnergyFraction = 0f;

    public RecipeCrafter(String name) {
        super(name);
        energySpec.role = EnergySpec.Role.consumer;
        energySpec.voltageV = 12f;
        energySpec.minInputVoltageV = 10f;
        energySpec.maxInputVoltageV = 14f;
        energySpec.capacityJ = 360f;
        energySpec.maxInputA = 6;
        energySpec.maxOutputA = 0;
        steamLiquid = ModLiquids.steam;

        update = true;
        solid = true;
        hasItems = true;
        hasLiquids = true;
        buildType = MDTFactoryBuild::new;
        drawer = new DrawDefault();
        buildVisibility = BuildVisibility.shown;
        requirements(Category.crafting, ItemStack.with(Items.copper, 50));
        itemCapacity = 20;
        liquidCapacity = 20f;

        configurable = true;
        saveConfig = true;
        copyConfig = true;

        config(Integer.class, (MDTFactoryBuild build, Integer groupIdx) -> {
            build.selectedGroup = groupIdx;
            build.currentRecipe = -1;
            build.progress = 0f;
        });
    }

    @Override
    public void load() {
        super.load();
        if (groups.length == 0 && recipes.length > 0) {
            groups = new RecipeGroup[]{ new RecipeGroup("default", recipes) };
        }
        for (int i = 0; i < groups.length; i++) {
            groups[i].Texture_name = "programming-circuit" + (i + 1);
        }
    }

    public RecipeGroup[] getEffectiveGroups() {
        return groups;
    }

    public boolean usesSteamEnergy() {
        return energySource == EnergySource.steam;
    }

    public boolean usesElectricEnergy() {
        return energySource == EnergySource.electricity;
    }

    public boolean usesManualEnergy() {
        return energySource == EnergySource.manual;
    }

    public static class Recipe {
        public ItemStack[] inputItems;
        public LiquidStack[] inputLiquids;
        public ItemStack[] outputItems;
        public LiquidStack[] outputLiquids;
        public float craftTime;
        public float energyPerCraftJ;
        public VoltageTier minimumVoltageTier;
        public VoltageTier executionVoltageTier;

        public Recipe(ItemStack[] inputItems, LiquidStack[] inputLiquids,
                      ItemStack[] outputItems, LiquidStack[] outputLiquids, float craftTime) {
            this(inputItems, inputLiquids, outputItems, outputLiquids, craftTime, 0f);
        }

        public Recipe(ItemStack[] inputItems, LiquidStack[] inputLiquids,
                      ItemStack[] outputItems, LiquidStack[] outputLiquids, float craftTime, float energyPerCraftJ) {
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

        public Recipe copyWith(float newCraftTime, float newEnergyPerCraftJ) {
            Recipe result = new Recipe(
                    copyItems(inputItems),
                    copyLiquids(inputLiquids),
                    copyItems(outputItems),
                    copyLiquids(outputLiquids),
                    Math.max(0.0001f, newCraftTime),
                    Math.max(0f, newEnergyPerCraftJ)
            );
            result.minimumVoltageTier = minimumVoltageTier;
            result.executionVoltageTier = executionVoltageTier;
            return result;
        }

        private static ItemStack[] copyItems(ItemStack[] source) {
            if (source == null) return null;
            ItemStack[] result = new ItemStack[source.length];
            for (int i = 0; i < source.length; i++) {
                ItemStack stack = source[i];
                result[i] = stack == null ? null : new ItemStack(stack.item, stack.amount);
            }
            return result;
        }

        private static LiquidStack[] copyLiquids(LiquidStack[] source) {
            if (source == null) return null;
            LiquidStack[] result = new LiquidStack[source.length];
            for (int i = 0; i < source.length; i++) {
                LiquidStack stack = source[i];
                result[i] = stack == null ? null : new LiquidStack(stack.liquid, stack.amount);
            }
            return result;
        }

        public static Recipe items(ItemStack[] in, ItemStack out, float time) {
            return new Recipe(in, new LiquidStack[]{},
                    out == null ? null : new ItemStack[]{out}, null, time);
        }

        public static Recipe items(ItemStack[] in, ItemStack[] out, float time) {
            return new Recipe(in, new LiquidStack[]{}, out, null, time);
        }

        public static Recipe withLiquid(ItemStack[] in, LiquidStack[] liqIn,
                                        ItemStack[] out, LiquidStack[] liqOut, float time) {
            return new Recipe(in, liqIn, out, liqOut, time);
        }

        public static Recipe withLiquid(ItemStack[] in, LiquidStack[] liqIn,
                                        ItemStack out, LiquidStack liqOut, float time) {
            return new Recipe(in, liqIn,
                    out == null ? null : new ItemStack[]{out},
                    liqOut == null ? null : new LiquidStack[]{liqOut},
                    time);
        }

        public String primaryOutputName() {
            if (outputItems != null && outputItems.length > 0) return outputItems[0].item.localizedName;
            if (outputLiquids != null && outputLiquids.length > 0) return outputLiquids[0].liquid.localizedName;
            return null;
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
            this.icon = null;
        }

        public void addRecipe(Recipe recipe){
            List<Recipe> x = new ArrayList<>(Arrays.asList(recipes));
            x.add(recipe);
            recipes=x.toArray(new Recipe[0]);
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        if (!usesManualEnergy()) {
            addBar("mdt-energy", (MDTFactoryBuild build) -> new Bar(
                    () -> Core.bundle.format("mdt.energy.bar",
                            Math.round(build.energyState.energyJ),
                            Math.round(energySpec.capacityJ)),
                    () -> Color.valueOf("ffd37f"),
                    () -> energySpec.capacityJ <= 0f ? 0f : Math.min(1f, build.energyState.energyJ / energySpec.capacityJ)
            ));
        }

        if (usesSteamEnergy()) {
            addBar("mdt-steam", (MDTFactoryBuild build) -> new Bar(
                    () -> Core.bundle.format("mdt.steam.bar",
                            Math.round(build.liquids.get(steamLiquid) * 10f) / 10f,
                            Math.round(liquidCapacity * 10f) / 10f),
                    () -> Color.lightGray,
                    () -> steamLiquid == null || liquidCapacity <= 0f ? 0f : Math.min(1f, build.liquids.get(steamLiquid) / liquidCapacity)
            ));
        } else if (usesElectricEnergy()) {
            addBar("mdt-energy-io", (MDTFactoryBuild build) -> {
                int maximum = Math.max(1, Math.max(energySpec.maxInputA, energySpec.maxOutputA));
                String ignored = build.energyState.ignoredInputA > 0
                        ? " | " + Core.bundle.format("mdt.io.ignored", build.energyState.ignoredInputA)
                        : "";
                return new Bar(
                        () -> Core.bundle.format("mdt.io.withvoltage",
                                build.energyState.inputA,
                                build.energyState.outputA,
                                Math.round(build.energyState.lastInputVoltageV * 10f) / 10f,
                                energySpec.minInputVoltageV,
                                energySpec.maxInputVoltageV,
                                ignored),
                        () -> Color.valueOf("84f491"),
                        () -> Math.min(1f, Math.max(build.energyState.inputA, build.energyState.outputA) / (float) maximum)
                );
            });
        }

        addBar("progress", (MDTFactoryBuild build) -> new Bar(
                () -> {
                    RecipeGroup[] groups = getEffectiveGroups();
                    if (build.selectedGroup >= 0 && build.selectedGroup < groups.length) {
                        RecipeGroup group = groups[build.selectedGroup];
                        String groupName = Core.bundle.get("group." + group.name, group.name);
                        if (build.currentRecipe >= 0 && build.currentRecipe < group.recipes.length) {
                            Recipe r = group.recipes[build.currentRecipe];
                            String itemName = r.primaryOutputName();
                            if (itemName == null) itemName = "???";
                            return Core.bundle.format("mdt.progress.bar", groupName, itemName, (int)(build.progress * 100));
                        }
                        return groupName + " - " + Core.bundle.get("mdt.progress.idle", "空闲");
                    }
                    return Core.bundle.get("mdt.progress.norecipe", "无配方");
                },
                () -> Pal.accent,
                () -> build.progress
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        RecipeGroup[] groups = getEffectiveGroups();
        if (groups.length == 0) return;

        stats.add(Stat.output, table -> {
            for (RecipeGroup group : groups) {
                String groupName = Core.bundle.get("group." + group.name, group.name);
                table.add("[accent]" + groupName + "[]").padTop(8).colspan(2).left().row();
                for (Recipe r : group.recipes) {
                    if (r.outputItems != null) {
                        for (ItemStack out : r.outputItems) {
                            table.image(out.item.uiIcon).size(24);
                            table.add(out.item.localizedName + " x" + out.amount).left().padLeft(4).row();
                        }
                    }
                    if (r.outputLiquids != null) {
                        for (LiquidStack out : r.outputLiquids) {
                            table.image(out.liquid.uiIcon).size(24);
                            table.add(out.liquid.localizedName + " " + out.amount + "单位").left().padLeft(4).row();
                        }
                    }
                }
            }
        });
    }

    public class MDTFactoryBuild extends GenericCrafterBuild implements MdtEnergyNode {
        public final EnergyState energyState = new EnergyState();
        public int selectedGroup = -1;
        public int currentRecipe = -1;

        @Override
        public Building energyBuilding() {
            return this;
        }

        @Override
        public EnergySpec energySpec() {
            return RecipeCrafter.this.energySpec;
        }

        @Override
        public EnergyState energyState() {
            return energyState;
        }

        @Override
        public boolean canConnectToElectricGrid() {
            return usesElectricEnergy();
        }

        protected void convertSteamToEnergy() {
            if (!usesSteamEnergy()) return;
            SteamEnergyConverter.convert(
                    this,
                    energyState,
                    energySpec,
                    steamLiquid,
                    joulesPerSteamUnit,
                    maxSteamUsePerSecond,
                    delta()
            );
        }

        @Override
        public void created() {
            super.created();
            float fraction = Math.max(0f, Math.min(1f, initialEnergyFraction));
            energyState.energyJ = energySpec.capacityJ * fraction;
        }

        @Override
        public void updateTile() {
            convertSteamToEnergy();
            RecipeGroup[] groups = getEffectiveGroups();

            for (RecipeGroup group : groups) {
                for (Recipe r : group.recipes) {
                    if (r.outputItems != null) {
                        for (ItemStack out : r.outputItems) {
                            if (items.has(out.item)) dump(out.item);
                        }
                    }
                    if (r.outputLiquids != null) {
                        for (LiquidStack out : r.outputLiquids) {
                            if (liquids.get(out.liquid) > 0.001f) dumpLiquid(out.liquid);
                        }
                    }
                }
            }

            if (selectedGroup < 0 || selectedGroup >= groups.length) {
                progress = 0f;
                currentRecipe = -1;
                return;
            }

            Recipe[] activeRecipes = groups[selectedGroup].recipes;

            if (currentRecipe >= 0 && currentRecipe < activeRecipes.length) {
                if (!hasAllMaterials(activeRecipes[currentRecipe]) || outputFull(activeRecipes[currentRecipe])) {
                    currentRecipe = -1;
                }
            }

            if (currentRecipe == -1) {
                for (int i = 0; i < activeRecipes.length; i++) {
                    if (hasAllMaterials(activeRecipes[i]) && !outputFull(activeRecipes[i])) {
                        currentRecipe = i;
                        progress = 0f;
                        break;
                    }
                }
            }

            if (currentRecipe >= 0 && currentRecipe < activeRecipes.length) {
                Recipe active = activeRecipes[currentRecipe];
                craftTime = Math.max(0.0001f, active.craftTime);

                if (shouldConsume()) {
                    float remainingWorkTicks = Math.max(0f, delta() * efficiency);
                    int safety = 0;
                    while (remainingWorkTicks > 0.000001f && safety++ < 10000) {
                        if (!hasAllMaterials(active) || outputFull(active)) {
                            currentRecipe = -1;
                            break;
                        }

                        float ticksToFinish = Math.max(0.000001f, (1f - progress) * craftTime);
                        float segmentTicks = Math.min(remainingWorkTicks, ticksToFinish);
                        float requiredEnergyJ = active.energyPerCraftJ * segmentTicks / craftTime;

                        if (!energyState.consume(requiredEnergyJ)) {
                            break;
                        }

                        progress += segmentTicks / craftTime;
                        remainingWorkTicks -= segmentTicks;

                        if (progress >= 0.999999f) {
                            craft(active);
                            progress = Math.max(0f, progress - 1f);

                            if (active.outputItems != null) {
                                for (ItemStack out : active.outputItems) dump(out.item);
                            }
                            if (active.outputLiquids != null) {
                                for (LiquidStack out : active.outputLiquids) dumpLiquid(out.liquid);
                            }
                        }
                    }
                }

                if (currentRecipe >= 0 && (!hasAllMaterials(active) || outputFull(active))) {
                    currentRecipe = -1;
                }
            } else {
                progress = 0f;
            }
        }

        protected void craft(Recipe recipe) {
            if (recipe.inputItems != null) {
                for (ItemStack stack : recipe.inputItems) items.remove(stack.item, stack.amount);
            }
            if (recipe.inputLiquids != null) {
                for (LiquidStack stack : recipe.inputLiquids) liquids.remove(stack.liquid, stack.amount);
            }
            if (recipe.outputItems != null) {
                for (ItemStack stack : recipe.outputItems) {
                    for (int i = 0; i < stack.amount; i++) offload(stack.item);
                }
            }
            if (recipe.outputLiquids != null) {
                for (LiquidStack stack : recipe.outputLiquids) {
                    liquids.add(stack.liquid, stack.amount);
                }
            }
        }

        private boolean hasAllMaterials(Recipe r) {
            if (r.inputItems != null) {
                for (ItemStack stack : r.inputItems) if (items.get(stack.item) < stack.amount) return false;
            }
            if (r.inputLiquids != null) {
                for (LiquidStack stack : r.inputLiquids) if (liquids.get(stack.liquid) < stack.amount) return false;
            }
            return true;
        }

        private boolean outputFull(Recipe r) {
            if (r.outputItems != null) {
                for (ItemStack stack : r.outputItems) {
                    if (items.get(stack.item) + stack.amount > itemCapacity) return true;
                }
            }
            if (r.outputLiquids != null) {
                for (LiquidStack stack : r.outputLiquids) {
                    if (liquids.get(stack.liquid) + stack.amount > liquidCapacity) return true;
                }
            }
            return false;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            for (RecipeGroup group : getEffectiveGroups()) {
                for (Recipe r : group.recipes) {
                    if (r.inputItems != null) {
                        for (ItemStack stack : r.inputItems) {
                            if (stack.item == item && items.get(item) < itemCapacity) return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (usesSteamEnergy() && liquid == steamLiquid) {
                return liquids.get(liquid) < liquidCapacity;
            }

            for (RecipeGroup group : getEffectiveGroups()) {
                for (Recipe r : group.recipes) {
                    if (r.inputLiquids != null) {
                        for (LiquidStack stack : r.inputLiquids) {
                            if (stack.liquid == liquid && liquids.get(liquid) < liquidCapacity) return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void buildConfiguration(Table table) {
            table.clear();
            RecipeGroup[] groups = getEffectiveGroups();
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
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(selectedGroup);
            energyState.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                selectedGroup = read.i();
            }
            if (revision >= 2) {
                energyState.read(read, energySpec());
            } else {
                float fraction = Math.max(0f, Math.min(1f, initialEnergyFraction));
                energyState.energyJ = energySpec.capacityJ * fraction;
            }
        }
    }
}