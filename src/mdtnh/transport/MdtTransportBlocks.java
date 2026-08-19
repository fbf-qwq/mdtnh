package mdtnh.transport;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mdtnh.ModItems;
import mdtnh.energy.MdtEnergyBlock;
import mdtnh.modui.buildui.BuildMenuRegistry;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

/**
 * Registers all GTNH-derived transport blocks for materials that exist in ModItems.
 *
 * <p>Call {@link #load()} after ModItems.load().</p>
 */
public final class MdtTransportBlocks {

    public static final ObjectMap<String, Block> blocks = new ObjectMap<>();
    public static final Seq<Block> itemPipes = new Seq<>();
    public static final Seq<Block> fluidPipes = new Seq<>();
    public static final Seq<Block> wiresAndCables = new Seq<>();

    private MdtTransportBlocks() {}

    public static void load() {
        blocks.clear();
        itemPipes.clear();
        fluidPipes.clear();
        wiresAndCables.clear();

        loadItemPipes();
        loadFluidPipes();
        loadWiresAndCables();
    }

    /**
     * Optional integration with MDT's custom multilevel build menu.
     *
     * <p>The three category paths must already exist in BuildMenuRegistry. This method does not
     * guess project-specific paths; it only registers blocks into caller-selected categories.</p>
     */
    public static void addToBuildMenu(
            BuildMenuRegistry menu,
            String itemPipePath,
            String fluidPipePath,
            String wirePath
    ) {
        if (menu == null) return;

        for (Block block : itemPipes) {
            menu.add(itemPipePath, block);
        }
        for (Block block : fluidPipes) {
            menu.add(fluidPipePath, block);
        }
        for (Block block : wiresAndCables) {
            menu.add(wirePath, block);
        }
    }

    private static void loadItemPipes() {
        for (GtTransportData.ItemPipeMaterial material : GtTransportData.ITEM_PIPES) {
            for (int i = 0; i < GtTransportData.ITEM_PIPE_SIZE_IDS.length; i++) {
                float stacksPerSecond = material.stacksPerSecond[i];
                if (Float.isNaN(stacksPerSecond)) continue;

                String sizeId = GtTransportData.ITEM_PIPE_SIZE_IDS[i];
                String form = "item-pipe-" + sizeId;
                Item requirement = ModItems.get(material.id, form);
                if (requirement == null) continue;

                float itemsPerSecond = stacksPerSecond * 4f;
                String name = "gt-" + material.id + "-" + form;

                MdtItemPipeBlock block = new MdtItemPipeBlock(name, itemsPerSecond);
                applyPipeVisualScale(block, sizeId);
                block.localizedName =
                        GtTransportData.ITEM_PIPE_SIZE_NAMES[i]
                                + material.displayName + "物品管道";
                block.description =
                        "GTNH 标定 " + format(stacksPerSecond) + " 组/s；"
                                + "MDT 实际速度 " + format(itemsPerSecond) + " 物品/s。"
                                + (itemsPerSecond > 32f
                                ? " 使用塑钢带式打包，额定单包 "
                                + block.packageSize + " 个物品、满负载 4 包/s。"
                                : "");
                block.health = 90 + i * 15;
                block.alwaysUnlocked = true;
                block.buildVisibility = BuildVisibility.shown;
                block.requirements(Category.distribution, ItemStack.with(requirement, 1));

                register(name, block, itemPipes);
            }
        }
    }

    private static void loadFluidPipes() {
        for (GtTransportData.FluidPipeMaterial material : GtTransportData.FLUID_PIPES) {
            for (int i = 0; i < GtTransportData.FLUID_PIPE_SIZE_IDS.length; i++) {
                float litersPerSecond = material.litersPerSecond[i];
                if (Float.isNaN(litersPerSecond)) continue;

                String sizeId = GtTransportData.FLUID_PIPE_SIZE_IDS[i];
                String form = "fluid-pipe-" + sizeId;
                Item requirement = ModItems.get(material.id, form);
                if (requirement == null) continue;

                int channels = i == 5 ? 4 : (i == 6 ? 9 : 1);
                String name = "gt-" + material.id + "-" + form;

                MdtFluidPipeBlock block = new MdtFluidPipeBlock(
                        name,
                        litersPerSecond,
                        material.maxTemperatureK,
                        channels
                );
                applyFluidPipeVisualScale(block, sizeId);
                block.localizedName =
                        GtTransportData.FLUID_PIPE_SIZE_NAMES[i]
                                + material.displayName + "流体管道";
                block.description =
                        "GTNH 基准流量 " + format(litersPerSecond) + " L/s；"
                                + "MDT 实际速度 x"
                                + format(MdtFluidPipeBlock.transportSpeedMultiplier)
                                + " = " + format(block.litersPerSecond) + " L/s = "
                                + format(block.mdtUnitsPerSecond) + " MDT流体单位/s；"
                                + "输入与输出均受该速度限制；"
                                + "GTNH 温度上限 " + material.maxTemperatureK + " K。"
                                + (channels > 1
                                ? " 独立流量通道：" + channels + "，每种流体分别限速。"
                                : "");
                block.health = 100 + i * 18;
                block.alwaysUnlocked = true;
                block.buildVisibility = BuildVisibility.shown;
                block.requirements(Category.liquid, ItemStack.with(requirement, 1));

                register(name, block, fluidPipes);
            }
        }
    }

    private static void loadWiresAndCables() {
        for (GtTransportData.WireMaterial material : GtTransportData.WIRES) {
            for (int count : GtTransportData.WIRE_COUNTS) {
                createWireBlock(material, count, false);
                createWireBlock(material, count, true);
            }
        }
    }

    private static void createWireBlock(
            GtTransportData.WireMaterial material,
            int count,
            boolean cable
    ) {
        String form = (cable ? "cable-" : "wire-") + count;
        Item requirement = ModItems.get(material.id, form);
        if (requirement == null) return;

        int maxCurrentA = material.baseCurrentA * count;
        float lossV = cable ? material.cableLossV : material.wireLossV;
        String name = "gt-" + material.id + "-" + form;

        MdtEnergyBlock block = new MdtEnergyBlock(name);
        block.localizedName =
                count + "x" + material.displayName + (cable ? "线缆" : "导线");
        block.description =
                "最大电压 " + material.maxVoltageV + " V；最大电流 "
                        + maxCurrentA + " A；线损 " + format(lossV)
                        + " V/格。电压或电流超限时烧毁。";
        block.fallbackRegion = "power-node";
        block.role = MdtEnergyBlock.EnergyRole.wire;

        block.connectedWireSprites = true;
        block.cableWireSprites = cable;

        /*
         * 不在这里写死 atlas 名称。
         * MdtEnergyBlock.load() 会基于 Mindustry 最终的 content name 自动推导，
         * 因而能正确保留 Java 模组的命名空间前缀。
         *
         * 资源逻辑：
         *   导线底层：<最终导线方块名>-center / -edge
         *   线缆底层：复用对应 <...-wire-count>-center / -edge
         *   线缆覆盖：<模组前缀>gt-cable-<count>-center / -edge
         */
        block.capacityJ = 0f;
        block.voltageV = 0f;
        block.maxInputA = 0;
        block.maxOutputA = 0;
        block.maxWireVoltageV = material.maxVoltageV;
        block.maxWireCurrentA = maxCurrentA;
        block.wireLossV = lossV;
        block.health = 70 + count * 3;
        block.size = 1;
        block.solid = false;
        block.underBullets = true;
        block.alwaysUnlocked = true;
        block.buildVisibility = BuildVisibility.shown;
        block.requirements(Category.power, ItemStack.with(requirement, 1));

        register(name, block, wiresAndCables);
    }

    private static void register(String key, Block block, Seq<Block> bucket) {
        blocks.put(key, block);
        bucket.add(block);
    }

    private static void applyPipeVisualScale(
            MdtItemPipeBlock block,
            String sizeId
    ) {
        switch (sizeId) {
            case "micro":
                block.movingItemScale = 0.28f;
                block.packageItemScale = 0.34f;
                block.maxRenderedItemSize = 5.0f;
                block.maxRenderedPackageSize = 5.6f;
                break;
            case "small":
                block.movingItemScale = 0.38f;
                block.packageItemScale = 0.44f;
                block.maxRenderedItemSize = 6.4f;
                block.maxRenderedPackageSize = 7.0f;
                break;
            case "medium":
                block.movingItemScale = 0.52f;
                block.packageItemScale = 0.60f;
                block.maxRenderedItemSize = 8.2f;
                block.maxRenderedPackageSize = 9.0f;
                break;
            case "large":
                block.movingItemScale = 0.68f;
                block.packageItemScale = 0.76f;
                block.maxRenderedItemSize = 10.8f;
                block.maxRenderedPackageSize = 12.0f;
                break;
            case "giant":
            default:
                block.movingItemScale = 0.82f;
                block.packageItemScale = 0.90f;
                block.maxRenderedItemSize = 13.2f;
                block.maxRenderedPackageSize = 14.0f;
                break;
        }
    }

    /**
     * 控制内部流体的可见宽度，避免 renderer.fluidFrames 铺满整个 tile。
     * 数值单位为 Mindustry 世界单位；标准一格为 8。
     */
    private static void applyFluidPipeVisualScale(
            MdtFluidPipeBlock block,
            String sizeId
    ) {
        switch (sizeId) {
            case "micro":
                block.liquidInnerWidth = 1.8f;
                break;
            case "small":
                block.liquidInnerWidth = 2.6f;
                break;
            case "medium":
                block.liquidInnerWidth = 3.6f;
                break;
            case "large":
                block.liquidInnerWidth = 4.8f;
                break;
            case "giant":
                block.liquidInnerWidth = 6.0f;
                break;
            case "quad":
                block.liquidInnerWidth = 5.4f;
                break;
            case "nine":
                block.liquidInnerWidth = 6.2f;
                break;
            default:
                block.liquidInnerWidth = 3.6f;
                break;
        }
    }

    private static String format(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001f) {
            return Integer.toString(Math.round(value));
        }
        return Float.toString(value);
    }
}
