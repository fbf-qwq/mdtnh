package mdtnh;

import arc.Core;
import mdtnh.energy.EnergySpec;
import mdtnh.hatch.EnergyInputHatch;
import mdtnh.hatch.ItemInputHatch;
import mdtnh.hatch.ItemOutputHatch;
import mdtnh.hatch.LiquidInputHatch;
import mdtnh.hatch.LiquidOutputHatch;
import mdtnh.hatch.SteamInputHatch;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItemFlammable;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BuildVisibility;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * 注册项目中的生产建筑、舱室和多方块结构示例。
 *
 * <p>该类只负责内容定义与参数装配，不保存运行时状态。每个已放置建筑的生产进度、
 * 物品、液体和能量由相应的 Building 子类独立维护。</p>
 */
public class ModCrafters {

    /** 使用 Mindustry 原生生产逻辑的蒸汽锅炉。 */
    public static Boiler Small_Coal_Fired_Boiler;

    /** 供其他内容注册代码访问的多配方工厂引用。 */
    public static RecipeCrafter multiFactory;
    @Deprecated
    public static RecipeCrafter test;

    /** 直接消耗蒸汽、无法连接电线的示例多配方工厂。 */
    public static SteamRecipeCrafter steamFactory;

    // ---------- 多方块舱室 ----------
    public static ItemInputHatch copperInputHatch;
    public static ItemOutputHatch productOutputHatch;

    public static LiquidInputHatch liquidInputHatch;
    public static LiquidOutputHatch liquidOutputHatch;

    public static EnergyInputHatch energyInputHatch;
    public static SteamInputHatch steamInputHatch;

    // ---------- 多方块核心 ----------
    public static MultiblockStructer poweredAltar;

    public static ModDrill testDrill;

    public static void load() {

        // 小型锅炉消耗可燃物和水，每 60 tick 生产一单位蒸汽。
        Small_Coal_Fired_Boiler = new Boiler("small-coal-fired-boiler") {{
            localizedName = Core.bundle.get("block.small-coal-fired-boiler.name", "小型燃煤锅炉");
            health = 100;
            size = 2;
            requirements(Category.crafting, ItemStack.with(Items.copper, 50));
            fuelList = new FuelList(
                    Items.coal,new FuelList.FuelProp(new ItemStack(ModItems.tinyPileOfDarkAsh,1),10800f,1F/3F)
            );
            maxHeat = 500;
            maxSteamAmount = 3200;
            maxWaterAmount = 1600;
            productSpeed = 0.005f;
            heatLoseSpeed = 0.01f;
            heatSpeed = 0.06944f;
        }};

        /*
         * 物品输入仓由传送设备写入原料，但不会主动把内容 dump 到外部。
         * 多方块核心会根据当前配方从指定输入仓中统一取料。
         */
        copperInputHatch = new ItemInputHatch("copper-input-hatch") {{
            localizedName = Core.bundle.get("hatch.copper-input-hatch.name", "通用输入仓");
            itemCapacity = 20;
            requirements(Category.distribution, ItemStack.with(Items.copper, 30, Items.lead, 15));
        }};

        productOutputHatch = new ItemOutputHatch("product-output-hatch") {{
            localizedName = Core.bundle.get("hatch.product-output-hatch.name", "通用输出仓");
            itemCapacity = 20;
            requirements(Category.distribution, ItemStack.with(Items.copper, 30, Items.lead, 15));
        }};

        liquidInputHatch = new LiquidInputHatch("liquid-input-hatch") {{
            localizedName = Core.bundle.get("hatch.liquid-input-hatch.name", "液体输入仓");
            liquidCapacity = 60f;
            requirements(Category.distribution, ItemStack.with(Items.copper, 30, Items.lead, 15));
        }};

        liquidOutputHatch = new LiquidOutputHatch("liquid-output-hatch") {{
            localizedName = Core.bundle.get("hatch.liquid-output-hatch.name", "液体输出仓");
            liquidCapacity = 60f;
            requirements(Category.distribution, ItemStack.with(Items.copper, 30, Items.lead, 15));
        }};

        energyInputHatch = new EnergyInputHatch("energy-input-hatch") {{
            localizedName = Core.bundle.get("hatch.energy-input-hatch.name", "能源输入仓");
            requirements(Category.power, ItemStack.with(Items.copper, 50, Items.silicon, 20));

            energySpec.voltageV = 12f;
            energySpec.minInputVoltageV = 10f;
            energySpec.maxInputVoltageV = 14f;
            energySpec.capacityJ = 4800f;
            energySpec.maxInputA = 32;
        }};

        steamInputHatch = new SteamInputHatch("steam-input-hatch") {{
            localizedName = Core.bundle.get("hatch.steam-input-hatch.name", "蒸汽能源仓");
            description = Core.bundle.get("hatch.steam-input-hatch.description", "接收蒸汽并转换为内部能源缓存，不能连接MDT电线。");
            requirements(Category.power, ItemStack.with(Items.copper, 45, Items.lead, 30));

            liquidCapacity = 40f;
            energySpec.capacityJ = 4800f;
            joulesPerSteamUnit = 120f;
            maxSteamUsePerSecond = 2f;
        }};

        // 单方块多配方工厂
        multiFactory = new RecipeCrafter("multi-factory") {{
            localizedName = Core.bundle.get("block.multi-factory.name", "多配方工厂");
            size = 2;
            health = 300;
            requirements(Category.crafting, ItemStack.with(Items.copper, 80, Items.silicon, 40));

            energySpec.role = EnergySpec.Role.consumer;
            energySpec.voltageV = 12f;
            energySpec.minInputVoltageV = 10f;
            energySpec.maxInputVoltageV = 14f;
            energySpec.capacityJ = 720f;
            energySpec.maxInputA = 12;
            energySpec.maxOutputA = 0;

            RecipeGroup groupMetals = new RecipeGroup(
                    "metals",
                    new RecipeCrafter.Recipe[]{
                            RecipeCrafter.Recipe.items(
                                    new ItemStack[]{new ItemStack(Items.copper, 3), new ItemStack(Items.lead, 2)},
                                    new ItemStack(Items.graphite, 1), 60f
                            ).energy(144f),
                            RecipeCrafter.Recipe.items(
                                    new ItemStack[]{new ItemStack(Items.titanium, 2)},
                                    new ItemStack(Items.silicon, 2), 50f
                            ).energy(200f)
                    }
            );

            RecipeGroup groupElectronics = new RecipeGroup(
                    "electronics",
                    new RecipeCrafter.Recipe[]{
                            RecipeCrafter.Recipe.items(
                                    new ItemStack[]{new ItemStack(Items.copper, 1), new ItemStack(Items.silicon, 2)},
                                    new ItemStack(Items.metaglass, 2), 90f
                            ).energy(360f),
                            RecipeCrafter.Recipe.withLiquid(
                                    new ItemStack[]{new ItemStack(Items.silicon, 3)},
                                    new LiquidStack[]{new LiquidStack(Liquids.water, 0.1f)},
                                    new ItemStack[]{new ItemStack(Items.surgeAlloy, 1), new ItemStack(Items.metaglass, 1)},
                                    new LiquidStack[]{new LiquidStack(Liquids.oil, 0.05f)},
                                    120f
                            ).energy(500f)
                    }
            );

            groups = new RecipeCrafter.RecipeGroup[]{groupMetals, groupElectronics};
        }};

        test = multiFactory;

        // 蒸汽多配方工厂
        steamFactory = new SteamRecipeCrafter("steam-multi-factory") {{
            localizedName = Core.bundle.get("block.steam-multi-factory.name", "蒸汽多配方工厂");
            description = Core.bundle.get("block.steam-multi-factory.description", "消耗蒸汽为内部缓存充能，不接受MDT电线供电。");
            size = 2;
            health = 320;
            requirements(Category.crafting, ItemStack.with(
                    Items.copper, 70,
                    Items.lead, 50,
                    Items.graphite, 25
            ));

            liquidCapacity = 30f;
            energySpec.capacityJ = 720f;
            joulesPerSteamUnit = 120f;
            maxSteamUsePerSecond = 1f;

            groups = new RecipeCrafter.RecipeGroup[]{
                    new RecipeCrafter.RecipeGroup("steam-processing", new RecipeCrafter.Recipe[]{
                            RecipeCrafter.Recipe.items(
                                    new ItemStack[]{
                                            new ItemStack(Items.copper, 2),
                                            new ItemStack(Items.lead, 1)
                                    },
                                    new ItemStack(Items.graphite, 1),
                                    60f
                            ).energy(96f)
                    })
            };
        }};

        // 多方块核心
        poweredAltar = new MultiblockStructer("powered-altar") {{
            localizedName = Core.bundle.get("block.powered-altar.name", "多方块核心");
            size = 1;
            requirements(Category.crafting, ItemStack.with(Items.copper, 100, Items.silicon, 50));
            buildVisibility = BuildVisibility.shown;

            Vector<Block> core = new Vector<>(); core.add(this);
            Vector<Block> in = new Vector<>(); in.add(copperInputHatch);
            Vector<Block> out = new Vector<>(); out.add(productOutputHatch);
            Vector<Block> liqIn = new Vector<>(); liqIn.add(liquidInputHatch);
            Vector<Block> liqOut = new Vector<>(); liqOut.add(liquidOutputHatch);
            Vector<Block> energy = new Vector<>();
            energy.add(energyInputHatch);
            energy.add(steamInputHatch);
            Vector<Block> air = new Vector<>(); air.add(Blocks.air);

            List<List<Block>> mapping = new Vector<>();
            mapping.add(core);   // 类型 0：核心方块
            mapping.add(in);     // 类型 1：物品输入仓
            mapping.add(out);    // 类型 2：物品输出仓
            mapping.add(energy); // 类型 3：能源输入仓
            mapping.add(air);    // 类型 4：必须为空的结构槽位
            mapping.add(liqIn);  // 类型 5：液体输入仓
            mapping.add(liqOut); // 类型 6：液体输出仓

            LevelStruct level1 = new LevelStruct();
            level1.struct = new HashMap<>();
            level1.struct.put(new pos(0, 0), 0);
            level1.struct.put(new pos(1, 0), 1);
            level1.struct.put(new pos(-1, 0), 1);
            level1.struct.put(new pos(0, 1), 2);
            level1.struct.put(new pos(0, -1), 4);
            level1.struct.put(new pos(2, 0), 3);
            level1.struct.put(new pos(1, 1), 5);
            level1.struct.put(new pos(-1, 1), 6);
            /*
               IO LI
            II C  II EI
               _  LO
              */

            level1.Mapping = mapping;
            levels = new Vector<>();
            levels.add(level1);

            groups = new MultiblockStructer.RecipeGroup[]{
                    new MultiblockStructer.RecipeGroup("smelting", new MultiblockStructer.Recipe[]{
                            MultiblockStructer.Recipe.items(
                                    new ItemStack[]{new ItemStack(Items.copper, 4), new ItemStack(Items.lead, 2)},
                                    new ItemStack[]{new ItemStack(Items.silicon, 2)},
                                    180f
                            ).energy(360f),
                            MultiblockStructer.Recipe.withLiquid(
                                    new ItemStack[]{new ItemStack(Items.titanium, 3), new ItemStack(Items.silicon, 2)},
                                    new LiquidStack[]{new LiquidStack(Liquids.water, 30f)},
                                    new ItemStack[]{new ItemStack(Items.surgeAlloy, 1), new ItemStack(Items.metaglass, 1)},
                                    new LiquidStack[]{new LiquidStack(ModLiquids.steam, 10f)},
                                    240f
                            ).energy(720f)
                    }),
                    new MultiblockStructer.RecipeGroup("advanced", new MultiblockStructer.Recipe[]{
                            MultiblockStructer.Recipe.items(
                                    new ItemStack[]{new ItemStack(Items.surgeAlloy, 1), new ItemStack(Items.phaseFabric, 1)},
                                    new ItemStack[]{new ItemStack(Items.plastanium, 2)},
                                    300f
                            ).energy(1200f)
                    })
            };

            groups[0].Texture_name = "programming-circuit1";
            groups[1].Texture_name = "programming-circuit2";
        }};

        testDrill = new ModDrill("testdrill");
        testDrill.localizedName = Core.bundle.get("block.testdrill.name", "测试钻机");
    }
}