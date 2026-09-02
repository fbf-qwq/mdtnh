package mdtnh;

import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GT 常用单方块加工机器与主流配方。
 * 编程电路不作为物品存在，直接映射为 RecipeGroup 语义分组。
 */
public final class GtProductionMachines {
    private GtProductionMachines() {}

    public static VoltageRecipeRegistry nasaWorkbench;
    public static VoltageRecipeRegistry electricFurnace;
    public static VoltageRecipeRegistry alloySmelter;
    public static VoltageRecipeRegistry compressor;
    public static VoltageRecipeRegistry macerator;
    public static VoltageRecipeRegistry forgeHammer;
    public static VoltageRecipeRegistry extractor;
    public static VoltageRecipeRegistry bender;
    public static VoltageRecipeRegistry wiremill;
    public static VoltageRecipeRegistry lathe;
    public static VoltageRecipeRegistry arcFurnace;
    public static VoltageRecipeRegistry cuttingMachine;
    public static VoltageRecipeRegistry electrolyzer;
    public static VoltageRecipeRegistry formingPress;
    public static VoltageRecipeRegistry assembler;
    public static VoltageRecipeRegistry circuitAssembler;
    public static VoltageRecipeRegistry chemicalReactor;
    public static VoltageRecipeRegistry mixer;
    public static VoltageRecipeRegistry centrifuge;
    public static VoltageRecipeRegistry polarizer;
    public static VoltageRecipeRegistry fluidSolidifier;
    public static VoltageRecipeRegistry thermalCentrifuge;
    public static VoltageRecipeRegistry sifter;
    public static VoltageRecipeRegistry distillery;
    public static VoltageRecipeRegistry laserEngraver;
    public static VoltageRecipeRegistry canner;
    public static VoltageRecipeRegistry fluidExtractor;
    public static VoltageRecipeRegistry chemicalBath;
    public static VoltageRecipeRegistry oreWasher;
    public static VoltageRecipeRegistry fluidHeater;
    public static VoltageRecipeRegistry autoWorkbench;
    public static VoltageRecipeRegistry ultimateWorkbench;
    public static VoltageRecipeRegistry biologicalLab;

    private static boolean loaded;

    public static void load() {
        if (loaded) return;
        loaded = true;

        nasaWorkbench = reg("nasa-workbench");
        electricFurnace = reg("electric-furnace");
        alloySmelter = reg("alloy-smelter");
        compressor = reg("compressor");
        macerator = reg("macerator");
        forgeHammer = reg("forge-hammer");
        extractor = reg("extractor");
        bender = reg("bender");
        wiremill = reg("wiremill");
        lathe = reg("lathe");
        arcFurnace = reg("arc-furnace");
        cuttingMachine = reg("cutting-machine");
        electrolyzer = reg("electrolyzer");
        formingPress = reg("forming-press");
        assembler = reg("assembler");
        circuitAssembler = reg("circuit-assembler");
        chemicalReactor = reg("chemical-reactor");
        mixer = reg("mixer");
        centrifuge = reg("centrifuge");
        polarizer = reg("polarizer");
        fluidSolidifier = reg("fluid-solidifier");
        thermalCentrifuge = reg("thermal-centrifuge");
        sifter = reg("sifter");
        distillery = reg("distillery");
        laserEngraver = reg("laser-engraver");
        canner = reg("canner");
        fluidExtractor = reg("fluid-extractor");
        chemicalBath = reg("chemical-bath");
        oreWasher = reg("ore-washer");
        fluidHeater = reg("fluid-heater");
        autoWorkbench = reg("auto-workbench");
        ultimateWorkbench = reg("ultimate-workbench");
        biologicalLab = reg("biological-lab");

        registerMaterialFormRecipes();
        registerOreProcessingRecipes();
        registerAlloyRecipes();
        registerChemicalRecipes();
        registerMachineComponentRecipes();
        registerWorkbenchRecipes(autoWorkbench, VoltageTier.LV, 100f, power(VoltageTier.LV, 100f, 1f));
        registerWorkbenchRecipes(nasaWorkbench, VoltageTier.LV, 100f, power(VoltageTier.LV, 100f, 1f));
        registerWorkbenchRecipes(ultimateWorkbench, VoltageTier.MV, 40f, power(VoltageTier.MV, 40f, 1f));
        registerBiologyRecipes();
    }

    private static VoltageRecipeRegistry reg(String name) {
        return new VoltageRecipeRegistry(name);
    }

    private static void registerMaterialFormRecipes() {
        for (String id : GtMaterials.EARLY_SMELTABLE_METALS) {
            registerIf(electricFurnace, "smelting", VoltageTier.LV,
                    new String[]{id + "_powder"}, null,
                    new String[]{id + "_ingot"}, null,
                    120f, power(VoltageTier.LV, 120f, 1f));
            registerIf(electricFurnace, "smelting", VoltageTier.LV,
                    new String[]{id + "_pure-powder"}, null,
                    new String[]{id + "_ingot"}, null,
                    100f, power(VoltageTier.LV, 100f, 1f));

            registerIf(compressor, "blocks", VoltageTier.LV,
                    repeat(id + "_ingot", 9), null,
                    new String[]{id + "_block"}, null,
                    180f, power(VoltageTier.LV, 180f, 1f));

            registerIf(bender, "plate", VoltageTier.LV,
                    new String[]{id + "_ingot"}, null,
                    new String[]{id + "_plate"}, null,
                    80f, power(VoltageTier.LV, 80f, 1f));
            registerIf(bender, "foil", VoltageTier.LV,
                    new String[]{id + "_plate"}, null,
                    repeat(id + "_foil", 4), null,
                    70f, power(VoltageTier.LV, 70f, 1f));
            registerIf(bender, "dense", VoltageTier.MV,
                    repeat(id + "_plate", 9), null,
                    new String[]{id + "_dense_plate"}, null,
                    160f, power(VoltageTier.MV, 160f, 1f));

            registerIf(forgeHammer, "plate", VoltageTier.LV,
                    repeat(id + "_ingot", 3), null,
                    repeat(id + "_plate", 2), null,
                    100f, power(VoltageTier.LV, 100f, 1f));

            registerIf(wiremill, "wire", VoltageTier.LV,
                    new String[]{id + "_ingot"}, null,
                    repeat(id + "_wire-1", 2), null,
                    100f, power(VoltageTier.LV, 100f, 1f));
            registerIf(wiremill, "fine-wire", VoltageTier.LV,
                    new String[]{id + "_wire-1"}, null,
                    repeat(id + "_fine-wire", 4), null,
                    60f, power(VoltageTier.LV, 60f, 1f));

            registerIf(lathe, "rod", VoltageTier.LV,
                    new String[]{id + "_ingot"}, null,
                    new String[]{id + "_rod", id + "_small-pile-powder"}, null,
                    120f, power(VoltageTier.LV, 120f, 1f));
            registerIf(cuttingMachine, "rod", VoltageTier.LV,
                    new String[]{id + "_long-rod"}, null,
                    repeat(id + "_rod", 2), null,
                    80f, power(VoltageTier.LV, 80f, 1f));
            registerIf(cuttingMachine, "bolt", VoltageTier.LV,
                    new String[]{id + "_rod"}, null,
                    repeat(id + "_bolt", 2), null,
                    70f, power(VoltageTier.LV, 70f, 1f));
        }

        registerIf(extractor, "rubber", VoltageTier.LV,
                new String[]{"sticky_resin"}, null,
                repeat("raw_rubber", 3), null,
                100f, power(VoltageTier.LV, 100f, 1f));

        registerIf(formingPress, "rubber", VoltageTier.LV,
                new String[]{"raw_rubber"}, null,
                new String[]{"rubber_sheet"}, null,
                80f, power(VoltageTier.LV, 80f, 1f));
        registerIf(formingPress, "rings", VoltageTier.LV,
                new String[]{"rubber_sheet"}, null,
                repeat("rubber_ring", 4), null,
                60f, power(VoltageTier.LV, 60f, 1f));

        registerIf(formingPress, "gear", VoltageTier.LV,
                repeat("steel_plate", 4), null,
                new String[]{"steel_gear"}, null,
                120f, power(VoltageTier.LV, 120f, 1f));
        registerIf(formingPress, "casing", VoltageTier.LV,
                repeat("steel_plate", 2), null,
                new String[]{"steel_casing"}, null,
                100f, power(VoltageTier.LV, 100f, 1f));

        registerIf(polarizer, "iron", VoltageTier.LV,
                new String[]{"iron_rod"}, null,
                new String[]{"magnetizedIron_rod"}, null,
                100f, power(VoltageTier.LV, 100f, 1f));
        registerIf(polarizer, "neodymium", VoltageTier.MV,
                new String[]{"neodymium_rod"}, null,
                new String[]{"magnetizedNeodymium_rod"}, null,
                120f, power(VoltageTier.MV, 120f, 1f));
        registerIf(polarizer, "samarium", VoltageTier.HV,
                new String[]{"samarium_rod"}, null,
                new String[]{"magnetizedSamarium_rod"}, null,
                140f, power(VoltageTier.HV, 140f, 1f));
    }

    private static void registerOreProcessingRecipes() {
        for (String id : GtMaterials.ORE_MATERIALS) {
            VoltageTier tier =
                    GtOreCatalog.processingTier(id);

            registerIf(
                    macerator,
                    "ore",
                    tier,
                    new String[]{id + "_raw-ore"},
                    null,
                    repeat(id + "_crushed-ore", 2),
                    null,
                    160f,
                    power(tier, 160f, 1f)
            );

            registerIf(
                    macerator,
                    "crushed",
                    tier,
                    new String[]{id + "_crushed-ore"},
                    null,
                    new String[]{id + "_impure-powder"},
                    null,
                    120f,
                    power(tier, 120f, 1f)
            );

            registerIf(
                    macerator,
                    "purified",
                    tier,
                    new String[]{id + "_purified-crushed-ore"},
                    null,
                    new String[]{id + "_pure-powder"},
                    null,
                    120f,
                    power(tier, 120f, 1f)
            );

            registerIf(
                    macerator,
                    "centrifuged",
                    tier,
                    new String[]{id + "_centrifuged-crushed-ore"},
                    null,
                    new String[]{id + "_powder"},
                    null,
                    110f,
                    power(tier, 110f, 1f)
            );

            registerIf(
                    forgeHammer,
                    "ore",
                    tier,
                    new String[]{id + "_raw-ore"},
                    null,
                    new String[]{id + "_crushed-ore"},
                    null,
                    120f,
                    power(tier, 120f, 1f)
            );

            registerIf(
                    oreWasher,
                    "water",
                    tier,
                    new String[]{id + "_crushed-ore"},
                    new LiquidStack[]{
                            new LiquidStack(
                                    Liquids.water,
                                    1.0f
                            )
                    },
                    new String[]{id + "_purified-crushed-ore"},
                    null,
                    120f,
                    power(tier, 120f, 1f)
            );

            registerIf(
                    chemicalBath,
                    "persulfate",
                    tier,
                    new String[]{id + "_crushed-ore"},
                    new LiquidStack[]{
                            new LiquidStack(
                                    GtLiquids.sodiumPersulfate,
                                    0.25f
                            )
                    },
                    new String[]{id + "_purified-crushed-ore"},
                    null,
                    100f,
                    power(tier, 100f, 1f)
            );

            registerIf(
                    thermalCentrifuge,
                    "ore",
                    tier,
                    new String[]{id + "_purified-crushed-ore"},
                    null,
                    new String[]{id + "_centrifuged-crushed-ore"},
                    null,
                    180f,
                    power(tier, 180f, 1f)
            );

            registerIf(
                    sifter,
                    "ore",
                    tier,
                    new String[]{id + "_purified-crushed-ore"},
                    null,
                    new String[]{
                            id + "_pure-powder",
                            id + "_small-pile-powder"
                    },
                    null,
                    180f,
                    power(tier, 180f, 1f)
            );

            registerIf(
                    centrifuge,
                    "ore-cleaning",
                    tier,
                    new String[]{id + "_impure-powder"},
                    null,
                    new String[]{
                            id + "_powder",
                            id + "_small-pile-powder"
                    },
                    null,
                    140f,
                    power(tier, 140f, 1f)
            );
        }
    }

    private static void registerAlloyRecipes() {
        alloy("bronze", new String[]{"copper_ingot", "copper_ingot", "copper_ingot", "tin_ingot"}, repeat("bronze_ingot", 4));
        alloy("brass", new String[]{"copper_ingot", "copper_ingot", "copper_ingot", "zinc_ingot"}, repeat("brass_ingot", 4));
        alloy("invar", new String[]{"iron_ingot", "iron_ingot", "nickel_ingot"}, repeat("invar_ingot", 3));
        alloy("electrum", new String[]{"gold_ingot", "silver_ingot"}, repeat("electrum_ingot", 2));
        alloy("cupronickel", new String[]{"copper_ingot", "nickel_ingot"}, repeat("cupronickel_ingot", 2));
        alloy("solder", new String[]{"tin_ingot", "lead_ingot"}, repeat("solder_ingot", 2));

        mixAlloy("bronze", new String[]{"copper_powder", "copper_powder", "copper_powder", "tin_powder"}, repeat("bronze_powder", 4));
        mixAlloy("brass", new String[]{"copper_powder", "copper_powder", "copper_powder", "zinc_powder"}, repeat("brass_powder", 4));
        mixAlloy("invar", new String[]{"iron_powder", "iron_powder", "nickel_powder"}, repeat("invar_powder", 3));
        mixAlloy("electrum", new String[]{"gold_powder", "silver_powder"}, repeat("electrum_powder", 2));
        mixAlloy("cupronickel", new String[]{"copper_powder", "nickel_powder"}, repeat("cupronickel_powder", 2));

        registerIf(arcFurnace, "refining", VoltageTier.MV,
                new String[]{"iron_ingot"}, new LiquidStack[]{new LiquidStack(GtLiquids.oxygen, 0.5f)},
                new String[]{"wroughtIron_ingot"}, null,
                120f, power(VoltageTier.MV, 120f, 1f));
    }

    private static void registerChemicalRecipes() {
        electrolyzer.register("water", VoltageTier.LV,
                mixed(null, new LiquidStack[]{new LiquidStack(Liquids.water, 3f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.hydrogen, 2f), new LiquidStack(GtLiquids.oxygen, 1f)},
                        160f, power(VoltageTier.LV, 160f, 1f)));

        chemicalReactor.register("acid", VoltageTier.LV,
                mixed(null, new LiquidStack[]{new LiquidStack(GtLiquids.hydrogen, 1f), new LiquidStack(GtLiquids.chlorine, 1f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.hydrochloricAcid, 1f)},
                        120f, power(VoltageTier.LV, 120f, 1f)));
        chemicalReactor.register("polymer", VoltageTier.MV,
                mixed(null, new LiquidStack[]{new LiquidStack(GtLiquids.ethylene, 2f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.polyethylene, 1f)},
                        180f, power(VoltageTier.MV, 180f, 1f)));
        chemicalReactor.register("ore-chemistry", VoltageTier.MV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("sulfur_dust"), 1)},
                        new LiquidStack[]{new LiquidStack(GtLiquids.distilledWater, 2f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.sodiumPersulfate, 1f)},
                        200f, power(VoltageTier.MV, 200f, 1f)));

        distillery.register("water", VoltageTier.LV,
                mixed(null, new LiquidStack[]{new LiquidStack(Liquids.water, 2f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.distilledWater, 1.8f)},
                        120f, power(VoltageTier.LV, 120f, 1f)));
        distillery.register("fuel", VoltageTier.MV,
                mixed(null, new LiquidStack[]{new LiquidStack(Liquids.oil, 2f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.diesel, 1f), new LiquidStack(GtLiquids.lightFuel, 0.5f)},
                        220f, power(VoltageTier.MV, 220f, 1f)));

        fluidHeater.register("steam", VoltageTier.LV,
                mixed(null, new LiquidStack[]{new LiquidStack(GtLiquids.distilledWater, 1f)}, null,
                        new LiquidStack[]{new LiquidStack(ModLiquids.steam, 1f)},
                        60f, power(VoltageTier.LV, 60f, 1f)));

        fluidSolidifier.register("polymer", VoltageTier.MV,
                mixed(null, new LiquidStack[]{new LiquidStack(GtLiquids.polyethylene, 1f)},
                        new ItemStack[]{new ItemStack(GtMaterials.require("plastic_sheet"), 1)}, null,
                        100f, power(VoltageTier.MV, 100f, 1f)));
        fluidSolidifier.register("rubber", VoltageTier.LV,
                mixed(null, new LiquidStack[]{new LiquidStack(GtLiquids.moltenRubber, 1f)},
                        new ItemStack[]{new ItemStack(GtMaterials.require("rubber_sheet"), 1)}, null,
                        100f, power(VoltageTier.LV, 100f, 1f)));

        fluidExtractor.register("rubber", VoltageTier.LV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("raw_rubber"), 1)}, null, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.moltenRubber, 1f)},
                        100f, power(VoltageTier.LV, 100f, 1f)));

        laserEngraver.register("wafer", VoltageTier.MV,
                items(new String[]{"silicon_wafer", "gold_fine-wire"}, new String[]{"etched_silicon_wafer"},
                        160f, power(VoltageTier.MV, 160f, 1f)));

        circuitAssembler.register("basic", VoltageTier.MV,
                items(new String[]{"etched_silicon_wafer", "printed_circuit_board", "copper_fine-wire", "plastic_sheet"},
                        new String[]{"basic_electronic_circuit"}, 180f, power(VoltageTier.MV, 180f, 1f)));
        circuitAssembler.register("advanced", VoltageTier.HV,
                items(new String[]{"etched_silicon_wafer", "printed_circuit_board", "gold_fine-wire", "basic_electronic_circuit"},
                        new String[]{"advanced_electronic_circuit"}, 220f, power(VoltageTier.HV, 220f, 1f)));
        circuitAssembler.register("processor", VoltageTier.HV,
                items(new String[]{"etched_silicon_wafer", "advanced_electronic_circuit", "gold_fine-wire", "plastic_sheet"},
                        new String[]{"processor"}, 240f, power(VoltageTier.HV, 240f, 1f)));

        canner.register("gas-cell", VoltageTier.LV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("empty_cell"), 1)},
                        new LiquidStack[]{new LiquidStack(GtLiquids.hydrogen, 1f)},
                        new ItemStack[]{new ItemStack(GtMaterials.require("hydrogen_cell"), 1)}, null,
                        80f, power(VoltageTier.LV, 80f, 1f)));
        canner.register("gas-cell", VoltageTier.LV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("empty_cell"), 1)},
                        new LiquidStack[]{new LiquidStack(GtLiquids.oxygen, 1f)},
                        new ItemStack[]{new ItemStack(GtMaterials.require("oxygen_cell"), 1)}, null,
                        80f, power(VoltageTier.LV, 80f, 1f)));
        canner.register("fuel-cell", VoltageTier.LV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("empty_cell"), 1)},
                        new LiquidStack[]{new LiquidStack(GtLiquids.diesel, 1f)},
                        new ItemStack[]{new ItemStack(GtMaterials.require("diesel_cell"), 1)}, null,
                        80f, power(VoltageTier.LV, 80f, 1f)));
    }

    private static void registerMachineComponentRecipes() {
        registerIf(assembler, "motor", VoltageTier.LV,
                new String[]{"iron_rod", "iron_rod", "copper_wire-1", "copper_wire-1", "tin_cable-1"}, null,
                new String[]{"electric_motor_lv"}, null,
                160f, power(VoltageTier.LV, 160f, 1f));
        registerIf(assembler, "pump", VoltageTier.LV,
                new String[]{"electric_motor_lv", "tin_rotor", "bronze_fluid-pipe-small", "rubber_ring"}, null,
                new String[]{"electric_pump_lv"}, null,
                180f, power(VoltageTier.LV, 180f, 1f));
        registerIf(assembler, "conveyor", VoltageTier.LV,
                new String[]{"electric_motor_lv", "rubber_sheet", "rubber_sheet", "steel_plate"}, null,
                new String[]{"conveyor_module_lv"}, null,
                180f, power(VoltageTier.LV, 180f, 1f));
        registerIf(assembler, "piston", VoltageTier.LV,
                new String[]{"electric_motor_lv", "steel_rod", "steel_gear", "steel_plate"}, null,
                new String[]{"electric_piston_lv"}, null,
                180f, power(VoltageTier.LV, 180f, 1f));
        registerIf(assembler, "robot-arm", VoltageTier.LV,
                new String[]{"electric_motor_lv", "electric_motor_lv", "electric_piston_lv", "basic_electronic_circuit"}, null,
                new String[]{"robot_arm_lv"}, null,
                220f, power(VoltageTier.LV, 220f, 1f));
        registerIf(assembler, "hull", VoltageTier.LV,
                new String[]{"steel_plate", "steel_plate", "tin_cable-1", "basic_electronic_circuit"}, null,
                new String[]{"machine_hull_lv"}, null,
                180f, power(VoltageTier.LV, 180f, 1f));

        formingPress.register("pcb", VoltageTier.MV,
                items(new String[]{"plastic_sheet", "copper_foil", "copper_foil"},
                        new String[]{"printed_circuit_board"}, 140f, power(VoltageTier.MV, 140f, 1f)));

        cuttingMachine.register("wafer", VoltageTier.MV,
                new RecipeCrafter.Recipe(
                        new ItemStack[]{new ItemStack(Items.silicon, 1)}, null,
                        new ItemStack[]{new ItemStack(GtMaterials.require("silicon_wafer"), 4)}, null,
                        160f, power(VoltageTier.MV, 160f, 1f)));
    }

    private static void registerWorkbenchRecipes(VoltageRecipeRegistry registry, VoltageTier tier, float time, float energy) {
        registerIf(registry, "machine-kit", tier,
                new String[]{"machine_hull_lv", "electric_motor_lv", "electric_pump_lv", "basic_electronic_circuit"}, null,
                new String[]{"workbench_machine_kit"}, null, time, energy);
        registerIf(registry, "casing", tier,
                new String[]{"steel_plate", "steel_plate", "steel_plate", "steel_plate"}, null,
                new String[]{"steel_casing", "steel_casing"}, null, time, energy);
        registerIf(registry, "cable", tier,
                new String[]{"copper_wire-1", "rubber_sheet"}, null,
                new String[]{"copper_cable-1"}, null, time, energy);
    }

    private static void registerBiologyRecipes() {
        biologicalLab.register("culture", VoltageTier.MV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("bio_sample"), 1)},
                        new LiquidStack[]{new LiquidStack(GtLiquids.biomass, 2f)},
                        new ItemStack[]{new ItemStack(GtMaterials.require("bio_culture"), 1)}, null,
                        240f, power(VoltageTier.MV, 240f, 1f)));
        biologicalLab.register("biomass", VoltageTier.MV,
                mixed(new ItemStack[]{new ItemStack(GtMaterials.require("bio_culture"), 1)},
                        new LiquidStack[]{new LiquidStack(Liquids.water, 2f)}, null,
                        new LiquidStack[]{new LiquidStack(GtLiquids.biomass, 4f)},
                        220f, power(VoltageTier.MV, 220f, 1f)));
    }

    private static void alloy(String group, String[] in, String[] out) {
        registerIf(alloySmelter, group, VoltageTier.LV, in, null, out, null,
                160f, power(VoltageTier.LV, 160f, 1f));
    }

    private static void mixAlloy(String group, String[] in, String[] out) {
        registerIf(mixer, group, VoltageTier.LV, in, null, out, null,
                140f, power(VoltageTier.LV, 140f, 1f));
    }

    private static void registerIf(VoltageRecipeRegistry registry, String group, VoltageTier tier,
                                   String[] inputKeys, LiquidStack[] liquidIn,
                                   String[] outputKeys, LiquidStack[] liquidOut,
                                   float time, float energy) {
        if (!allPresent(inputKeys) || !allPresent(outputKeys)) return;
        registry.register(group, tier, mixed(stacks(inputKeys), liquidIn, stacks(outputKeys), liquidOut, time, energy));
    }

    private static RecipeCrafter.Recipe items(String[] inputKeys, String[] outputKeys, float time, float energy) {
        if (!allPresent(inputKeys) || !allPresent(outputKeys)) {
            throw new IllegalStateException("GT recipe references missing item");
        }
        return mixed(stacks(inputKeys), null, stacks(outputKeys), null, time, energy);
    }

    private static RecipeCrafter.Recipe mixed(ItemStack[] itemIn, LiquidStack[] liquidIn,
                                               ItemStack[] itemOut, LiquidStack[] liquidOut,
                                               float time, float energy) {
        return new RecipeCrafter.Recipe(itemIn, liquidIn, itemOut, liquidOut, time, energy);
    }

    private static ItemStack[] stacks(String[] keys) {
        if (keys == null || keys.length == 0) return null;
        List<ItemStack> result = new ArrayList<>();
        String current = null;
        int amount = 0;
        for (String key : keys) {
            if (key == null) continue;
            if (current == null) {
                current = key;
                amount = 1;
            } else if (current.equals(key)) {
                amount++;
            } else {
                result.add(new ItemStack(GtMaterials.require(current), amount));
                current = key;
                amount = 1;
            }
        }
        if (current != null) result.add(new ItemStack(GtMaterials.require(current), amount));
        return result.toArray(new ItemStack[0]);
    }

    private static boolean allPresent(String[] keys) {
        if (keys == null) return true;
        for (String key : keys) {
            if (key != null && GtMaterials.get(key) == null) return false;
        }
        return true;
    }

    private static String[] repeat(String key, int count) {
        String[] result = new String[Math.max(0, count)];
        for (int i = 0; i < result.length; i++) result[i] = key;
        return result;
    }

    private static float power(VoltageTier tier, float ticks, float amps) {
        return tier.maxVoltageV * (ticks / 60f) * Math.max(0f, amps);
    }
}
