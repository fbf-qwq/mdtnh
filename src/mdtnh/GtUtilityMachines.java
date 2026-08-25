package mdtnh;

import mdtnh.energy.EnergySpec;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

import java.util.EnumMap;

/**
 * 资源机、锅炉、发电机与基础电网元件。
 * 二极管在没有上一版“方向寻路钩子”补丁时只能作为缓冲限流器；真正单向行为应继续使用 MdtDiodeBlock。
 */
public final class GtUtilityMachines {
    private GtUtilityMachines() {}

    public static GtAutoRecipeCrafter pumpLV;
    public static GtAutoRecipeCrafter pumpMV;
    public static GtAutoRecipeCrafter pumpHV;

    public static GtMinerBlock minerLV;
    public static GtMinerBlock minerMV;
    public static GtMinerBlock minerHV;

    public static GtAutoRecipeCrafter coalBoiler;
    public static GtAutoRecipeCrafter slagBoiler;
    public static GtAutoRecipeCrafter advancedBoiler;
    public static GtAutoRecipeCrafter solarBoiler;

    public static GtGeneratorCrafter steamTurbineLV;
    public static GtGeneratorCrafter steamTurbineMV;
    public static GtGeneratorCrafter steamTurbineHV;
    public static GtGeneratorCrafter combustionGenerator;
    public static GtGeneratorCrafter gasTurbine;
    public static GtGeneratorCrafter solarPanel;
    public static GtGeneratorCrafter lightningRod;

    public static final EnumMap<VoltageTier, RecipeCrafter> batteries = new EnumMap<>(VoltageTier.class);
    public static final EnumMap<VoltageTier, RecipeCrafter> diodes = new EnumMap<>(VoltageTier.class);
    public static final EnumMap<VoltageTier, RecipeCrafter> stepUpTransformers = new EnumMap<>(VoltageTier.class);
    public static final EnumMap<VoltageTier, RecipeCrafter> stepDownTransformers = new EnumMap<>(VoltageTier.class);

    private static boolean loaded;

    public static void load() {
        if (loaded) return;
        loaded = true;

        loadPumps();
        loadMiners();
        loadBoilers();
        loadGenerators();
        loadEnergyBuffers();
    }

    private static void loadPumps() {
        pumpLV = makePump("resource-pump-lv", VoltageTier.LV, 120f, 4f);
        pumpMV = makePump("resource-pump-mv", VoltageTier.MV, 60f, 8f);
        pumpHV = makePump("resource-pump-hv", VoltageTier.HV, 30f, 16f);
    }

    private static GtAutoRecipeCrafter makePump(String name, VoltageTier tier, float ticks, float waterAmount) {
        GtAutoRecipeCrafter block = new GtAutoRecipeCrafter(name);
        block.size = 2;
        block.itemCapacity = 0;
        block.liquidCapacity = 120f;
        block.energySpec.role = EnergySpec.Role.consumer;
        block.energySpec.voltageV = tier.maxVoltageV;
        block.energySpec.minInputVoltageV = tier.minVoltageV;
        block.energySpec.maxInputVoltageV = tier.maxVoltageV;
        block.energySpec.capacityJ = Math.max(tier.capacityJ, tier.maxVoltageV * 4f);
        block.energySpec.maxInputA = 1;
        block.energySpec.maxOutputA = 0;
        block.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("pumping", new RecipeCrafter.Recipe[]{
                        new RecipeCrafter.Recipe(null, null, null,
                                new LiquidStack[]{new LiquidStack(Liquids.water, waterAmount)},
                                ticks, tier.maxVoltageV * ticks / 60f)
                })
        };
        return block;
    }

    private static void loadMiners() {
        // GTNH: 17x17/8s, 33x33/4s, 49x49/2s.
        minerLV = new GtMinerBlock("basic-miner-lv", VoltageTier.LV, 17, 8f * 60f);
        minerMV = new GtMinerBlock("good-miner-mv", VoltageTier.MV, 33, 4f * 60f);
        minerHV = new GtMinerBlock("advanced-miner-hv", VoltageTier.HV, 49, 2f * 60f);
    }

    private static void loadBoilers() {
        coalBoiler = boiler("coal-boiler", 240f,
                new ItemStack[]{new ItemStack(Items.coal, 1)},
                new LiquidStack[]{new LiquidStack(Liquids.water, 8f)}, 32f);

        slagBoiler = boiler("slag-boiler", 180f,
                null,
                new LiquidStack[]{new LiquidStack(Liquids.water, 8f), new LiquidStack(Liquids.slag, 1f)}, 48f);

        advancedBoiler = new GtAutoRecipeCrafter("advanced-boiler");
        configureBoiler(advancedBoiler);
        advancedBoiler.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("fuel", new RecipeCrafter.Recipe[]{
                        new RecipeCrafter.Recipe(
                                new ItemStack[]{new ItemStack(Items.coal, 1)},
                                new LiquidStack[]{new LiquidStack(Liquids.water, 16f)},
                                null, new LiquidStack[]{new LiquidStack(ModLiquids.steam, 96f)}, 120f, 0f),
                        new RecipeCrafter.Recipe(
                                null,
                                new LiquidStack[]{new LiquidStack(Liquids.water, 16f), new LiquidStack(GtLiquids.diesel, 0.5f)},
                                null, new LiquidStack[]{new LiquidStack(ModLiquids.steam, 128f)}, 120f, 0f)
                })
        };

        solarBoiler = boiler("solar-boiler", 240f,
                null,
                new LiquidStack[]{new LiquidStack(Liquids.water, 2f)}, 8f);
    }

    private static GtAutoRecipeCrafter boiler(String name, float ticks,
                                               ItemStack[] itemIn, LiquidStack[] liquidIn,
                                               float steamOut) {
        GtAutoRecipeCrafter block = new GtAutoRecipeCrafter(name);
        configureBoiler(block);
        block.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("boiling", new RecipeCrafter.Recipe[]{
                        new RecipeCrafter.Recipe(itemIn, liquidIn, null,
                                new LiquidStack[]{new LiquidStack(ModLiquids.steam, steamOut)}, ticks, 0f)
                })
        };
        return block;
    }

    private static void configureBoiler(GtAutoRecipeCrafter block) {
        block.size = 2;
        block.energySource = RecipeCrafter.EnergySource.manual;
        block.energySpec.role = EnergySpec.Role.consumer;
        block.energySpec.capacityJ = 0f;
        block.energySpec.maxInputA = 0;
        block.energySpec.maxOutputA = 0;
        block.itemCapacity = 40;
        block.liquidCapacity = 400f;
    }

    private static void loadGenerators() {
        steamTurbineLV = steamTurbine("steam-turbine-lv", VoltageTier.LV, 0.85f);
        steamTurbineMV = steamTurbine("steam-turbine-mv", VoltageTier.MV, 0.80f);
        steamTurbineHV = steamTurbine("steam-turbine-hv", VoltageTier.HV, 0.66f);

        combustionGenerator = generator("combustion-generator-lv", VoltageTier.LV, 1, VoltageTier.LV.maxVoltageV * 16f);
        combustionGenerator.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("fuel", new RecipeCrafter.Recipe[]{
                        generation(null, new LiquidStack[]{new LiquidStack(GtLiquids.diesel, 1f)}, 240f, VoltageTier.LV.maxVoltageV * 4f),
                        generation(null, new LiquidStack[]{new LiquidStack(GtLiquids.lightFuel, 1f)}, 180f, VoltageTier.LV.maxVoltageV * 3f)
                })
        };

        gasTurbine = generator("gas-turbine-lv", VoltageTier.LV, 1, VoltageTier.LV.maxVoltageV * 16f);
        gasTurbine.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("gas", new RecipeCrafter.Recipe[]{
                        generation(null, new LiquidStack[]{new LiquidStack(GtLiquids.methane, 1f)}, 180f, VoltageTier.LV.maxVoltageV * 3f),
                        generation(null, new LiquidStack[]{new LiquidStack(GtLiquids.hydrogen, 1f)}, 240f, VoltageTier.LV.maxVoltageV * 2f)
                })
        };

        solarPanel = generator("solar-panel", VoltageTier.ULV, 1, VoltageTier.ULV.maxVoltageV * 8f);
        solarPanel.itemCapacity = 0;
        solarPanel.liquidCapacity = 0f;
        solarPanel.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("solar", new RecipeCrafter.Recipe[]{
                        generation(null, null, 60f, VoltageTier.ULV.maxVoltageV)
                })
        };

        lightningRod = generator("lightning-rod", VoltageTier.HV, 4, VoltageTier.HV.maxVoltageV * 8f);
        lightningRod.itemCapacity = 0;
        lightningRod.liquidCapacity = 0f;
        lightningRod.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("storm-charge", new RecipeCrafter.Recipe[]{
                        generation(null, null, 30f * 60f, VoltageTier.HV.maxVoltageV * 4f)
                })
        };
    }

    private static GtGeneratorCrafter steamTurbine(String name, VoltageTier tier, float efficiency) {
        GtGeneratorCrafter block = generator(name, tier, 1, tier.maxVoltageV * 16f);
        block.liquidCapacity = Math.max(200f, tier.maxVoltageV * 8f);
        // GTNH 单方块蒸汽轮机基础关系约 2L 蒸汽 -> 1EU，再乘机器燃料效率。
        float steamPerSecond = tier.maxVoltageV * 2f / Math.max(0.01f, efficiency);
        block.groups = new RecipeCrafter.RecipeGroup[]{
                new RecipeCrafter.RecipeGroup("steam", new RecipeCrafter.Recipe[]{
                        generation(null, new LiquidStack[]{new LiquidStack(ModLiquids.steam, steamPerSecond)},
                                60f, tier.maxVoltageV)
                })
        };
        return block;
    }

    private static GtGeneratorCrafter generator(String name, VoltageTier tier, int outputA, float capacity) {
        GtGeneratorCrafter block = new GtGeneratorCrafter(name);
        block.size = 2;
        block.energySpec.role = EnergySpec.Role.generator;
        block.energySpec.voltageV = tier.maxVoltageV;
        block.energySpec.minInputVoltageV = 0f;
        block.energySpec.maxInputVoltageV = Float.MAX_VALUE;
        block.energySpec.capacityJ = Math.max(capacity, tier.maxVoltageV * outputA);
        block.energySpec.maxInputA = 0;
        block.energySpec.maxOutputA = Math.max(1, outputA);
        block.itemCapacity = 40;
        block.liquidCapacity = 400f;
        return block;
    }

    private static RecipeCrafter.Recipe generation(ItemStack[] items, LiquidStack[] liquids, float ticks, float joules) {
        return new RecipeCrafter.Recipe(items, liquids, null, null, ticks, joules);
    }

    private static void loadEnergyBuffers() {
        VoltageTier[] tiers = VoltageTier.values();
        for (VoltageTier tier : tiers) {
            RecipeCrafter battery = passiveEnergyBlock("battery-" + tier.contentName);
            battery.energySpec.role = EnergySpec.Role.battery;
            battery.energySpec.voltageV = tier.maxVoltageV;
            battery.energySpec.minInputVoltageV = tier.minVoltageV;
            battery.energySpec.maxInputVoltageV = tier.maxVoltageV;
            battery.energySpec.capacityJ = Math.max(tier.capacityJ * 16f, tier.maxVoltageV * 32f);
            battery.energySpec.maxInputA = 4;
            battery.energySpec.maxOutputA = 4;
            batteries.put(tier, battery);

            // 兼容回退：它能限制安培并缓冲，但真正单向寻路请使用上一能源补丁的 MdtDiodeBlock。
            RecipeCrafter diode = passiveEnergyBlock("buffered-diode-" + tier.contentName);
            diode.rotate = true;
            diode.energySpec.role = EnergySpec.Role.battery;
            diode.energySpec.voltageV = tier.maxVoltageV;
            diode.energySpec.minInputVoltageV = tier.minVoltageV;
            diode.energySpec.maxInputVoltageV = tier.maxVoltageV;
            diode.energySpec.capacityJ = Math.max(tier.maxVoltageV * 2f, 2f);
            diode.energySpec.maxInputA = 1;
            diode.energySpec.maxOutputA = 1;
            diodes.put(tier, diode);
        }

        for (int i = 0; i < tiers.length - 1; i++) {
            VoltageTier low = tiers[i];
            VoltageTier high = tiers[i + 1];

            RecipeCrafter up = passiveEnergyBlock("transformer-" + low.contentName + "-" + high.contentName + "-up");
            up.energySpec.role = EnergySpec.Role.battery;
            up.energySpec.voltageV = high.maxVoltageV;
            up.energySpec.minInputVoltageV = low.minVoltageV;
            up.energySpec.maxInputVoltageV = low.maxVoltageV;
            up.energySpec.capacityJ = Math.max(high.maxVoltageV * 4f, high.capacityJ);
            up.energySpec.maxInputA = 4;
            up.energySpec.maxOutputA = 1;
            stepUpTransformers.put(low, up);

            RecipeCrafter down = passiveEnergyBlock("transformer-" + high.contentName + "-" + low.contentName + "-down");
            down.energySpec.role = EnergySpec.Role.battery;
            down.energySpec.voltageV = low.maxVoltageV;
            down.energySpec.minInputVoltageV = high.minVoltageV;
            down.energySpec.maxInputVoltageV = high.maxVoltageV;
            down.energySpec.capacityJ = Math.max(high.maxVoltageV * 4f, high.capacityJ);
            down.energySpec.maxInputA = 1;
            down.energySpec.maxOutputA = 4;
            stepDownTransformers.put(low, down);
        }
    }

    private static RecipeCrafter passiveEnergyBlock(String name) {
        RecipeCrafter block = new RecipeCrafter(name);
        block.size = 2;
        block.hasItems = false;
        block.hasLiquids = false;
        block.itemCapacity = 0;
        block.liquidCapacity = 0f;
        block.groups = new RecipeCrafter.RecipeGroup[]{};
        return block;
    }
}
