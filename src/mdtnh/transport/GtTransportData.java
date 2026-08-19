package mdtnh.transport;

/**
 * GTNH 2.8.x transport data used by MDT.
 *
 * <p>Only materials that already exist in ModItems are listed here. Website materials without
 * a matching MDT material are intentionally omitted and summarized in README_MDT_GT_TRANSPORT.md.</p>
 */
public final class GtTransportData {

    private GtTransportData() {}

    /** 1x/2x/4x/8x/12x/16x wire and cable variants. */
    public static final int[] WIRE_COUNTS = {1, 2, 4, 8, 12, 16};

    /** Item pipe sizes in GT order: tiny, small, normal, large, huge. */
    public static final String[] ITEM_PIPE_SIZE_IDS = {
            "micro", "small", "medium", "large", "giant"
    };
    public static final String[] ITEM_PIPE_SIZE_NAMES = {
            "微型", "小型", "普通", "大型", "巨型"
    };

    /** Fluid pipe sizes in GT order: tiny, small, normal, large, huge, quadruple, nonuple. */
    public static final String[] FLUID_PIPE_SIZE_IDS = {
            "micro", "small", "medium", "large", "giant", "quad", "nine"
    };
    public static final String[] FLUID_PIPE_SIZE_NAMES = {
            "微型", "小型", "普通", "大型", "巨型", "四联", "九联"
    };

    /**
     * MDT fluid conversion chosen for this patch.
     *
     * <p>1000 L = 1 MDT liquid unit, so a GTNH flow in L/s is divided by 1000.</p>
     */
    public static final float LITERS_PER_MDT_FLUID_UNIT = 1000f;

    public static final class WireMaterial {
        public final String id;
        public final String displayName;
        public final int maxVoltageV;
        public final int baseCurrentA;
        public final float wireLossV;
        public final float cableLossV;

        public WireMaterial(
                String id,
                String displayName,
                int maxVoltageV,
                int baseCurrentA,
                float wireLossV,
                float cableLossV
        ) {
            this.id = id;
            this.displayName = displayName;
            this.maxVoltageV = maxVoltageV;
            this.baseCurrentA = baseCurrentA;
            this.wireLossV = wireLossV;
            this.cableLossV = cableLossV;
        }
    }

    public static final class ItemPipeMaterial {
        public final String id;
        public final String displayName;
        /** GTNH values in stacks/s. NaN means that size does not exist. */
        public final float[] stacksPerSecond;

        public ItemPipeMaterial(String id, String displayName, float... stacksPerSecond) {
            this.id = id;
            this.displayName = displayName;
            this.stacksPerSecond = stacksPerSecond;
        }
    }

    public static final class FluidPipeMaterial {
        public final String id;
        public final String displayName;
        /** GTNH values in L/s. NaN means that size does not exist. */
        public final float[] litersPerSecond;
        public final int maxTemperatureK;

        public FluidPipeMaterial(
                String id,
                String displayName,
                int maxTemperatureK,
                float... litersPerSecond
        ) {
            this.id = id;
            this.displayName = displayName;
            this.maxTemperatureK = maxTemperatureK;
            this.litersPerSecond = litersPerSecond;
        }
    }

    private static float n() {
        return Float.NaN;
    }

    /**
     * Wire/cable rows with an exact material counterpart in current ModItems.
     *
     * <p>Maximum current of an Nx variant = baseCurrentA * N.
     * Voltage loss is per wire block for each transmitted 1 A packet.</p>
     */
    public static final WireMaterial[] WIRES = {
            new WireMaterial("redAlloy", "红色合金", 8, 1, 1f, 0f),
            new WireMaterial("cobalt", "钴", 32, 2, 2f, 1f),
            new WireMaterial("lead", "铅", 32, 2, 4f, 2f),
            new WireMaterial("tin", "锡", 32, 1, 2f, 1f),
            new WireMaterial("zinc", "锌", 32, 1, 2f, 1f),
            new WireMaterial("solder", "焊锡", 32, 1, 2f, 1f),

            new WireMaterial("iron", "铁", 128, 2, 6f, 3f),
            new WireMaterial("nickel", "镍", 128, 3, 6f, 3f),
            new WireMaterial("cupronickel", "白铜", 128, 4, 6f, 3f),
            new WireMaterial("copper", "铜", 128, 1, 4f, 2f),
            new WireMaterial("annealedCopper", "退火铜", 128, 1, 2f, 1f),

            new WireMaterial("kanthal", "坎塔尔合金", 512, 5, 6f, 3f),
            new WireMaterial("gold", "金", 512, 3, 4f, 2f),
            new WireMaterial("electrum", "琥珀金", 512, 2, 2f, 1f),
            new WireMaterial("silver", "银", 512, 1, 2f, 1f),
            new WireMaterial("blueAlloy", "蓝色合金", 512, 2, 2f, 1f),

            new WireMaterial("nichrome", "镍铬合金", 2048, 6, 8f, 4f),
            new WireMaterial("steel", "钢", 2048, 2, 6f, 3f),
            new WireMaterial("blackSteel", "黑钢", 2048, 4, 2f, 1f),
            new WireMaterial("titanium", "钛", 2048, 4, 4f, 2f),
            new WireMaterial("aluminum", "铝", 2048, 1, 2f, 1f),

            new WireMaterial("platinum", "铂", 8192, 2, 2f, 1f),
            new WireMaterial("tungstenSteel", "钨钢", 8192, 4, 8f, 4f),
            new WireMaterial("tungsten", "钨", 8192, 6, 4f, 2f),

            new WireMaterial("osmium", "锇", 32768, 4, 4f, 2f),
            new WireMaterial("highSpeedSteelG", "高速钢-G", 32768, 4, 4f, 2f),
            new WireMaterial("niobiumTitaniumAlloy", "铌钛合金", 32768, 4, 4f, 2f),
            new WireMaterial("vanadiumGalliumAlloy", "钒镓合金", 32768, 4, 8f, 4f),
            new WireMaterial("highSpeedSteelE", "高速钢-E", 32768, 6, 8f, 4f),

            new WireMaterial("iridiumOsmiumAlloy", "铱锇合金", 131072, 16, 2f, 1f),
            new WireMaterial("naquadahAlloy", "硅岩合金", 524288, 6, 8f, 4f),
            new WireMaterial("highSpeedSteelS", "高速钢-S", 2097152, 8, 8f, 4f)
    };

    /**
     * GT item-pipe rows with an exact ModItems material.
     *
     * <p>MDT speed = GT stacks/s * 4 items/s, exactly as requested. Route values are omitted.</p>
     */
    public static final ItemPipeMaterial[] ITEM_PIPES = {
            new ItemPipeMaterial("aluminum", "铝",
                    n(), n(), 2f, 4f, 8f),
            new ItemPipeMaterial("tin", "锡",
                    1f / 8f, 1f / 4f, 1f / 2f, 1f, 2f),
            new ItemPipeMaterial("brass", "黄铜",
                    1f / 4f, 1f / 2f, 1f, 2f, 4f),
            new ItemPipeMaterial("electrum", "琥珀金",
                    1f / 2f, 1f, 2f, 4f, 8f),
            new ItemPipeMaterial("platinum", "铂",
                    1f, 2f, 4f, 8f, 16f),
            new ItemPipeMaterial("osmium", "锇",
                    2f, 4f, 8f, 16f, 32f),
            new ItemPipeMaterial("nickel", "镍",
                    n(), n(), 1f, 2f, 4f),
            new ItemPipeMaterial("cobalt", "钴",
                    n(), n(), 2f, 4f, 8f)
    };

    /** GT fluid-pipe rows with an exact ModItems material. */
    public static final FluidPipeMaterial[] FLUID_PIPES = {
            new FluidPipeMaterial("copper", "铜", 1000,
                    60f, 120f, 400f, 800f, 1600f, 400f, 120f),
            new FluidPipeMaterial("bronze", "青铜", 2000,
                    400f, 800f, 2400f, 4800f, 9600f, 2400f, 800f),
            new FluidPipeMaterial("steel", "钢", 2500,
                    800f, 1600f, 4800f, 9600f, 19200f, 4800f, 1600f),
            new FluidPipeMaterial("stainlessSteel", "不锈钢", 3000,
                    1200f, 2400f, 7200f, 14400f, 28800f, 7200f, 2400f),
            new FluidPipeMaterial("titanium", "钛", 5000,
                    1600f, 3200f, 9600f, 19200f, 38400f, 9600f, 3200f),
            new FluidPipeMaterial("tungstenSteel", "钨钢", 7500,
                    2000f, 4000f, 12000f, 24000f, 48000f, 12000f, 4000f),
            new FluidPipeMaterial("niobiumTitaniumAlloy", "铌钛合金", 2900,
                    3000f, 6000f, 18000f, 36000f, 72000f, 18000f, 6000f),
            new FluidPipeMaterial("wroughtIron", "锻铁", 2250,
                    600f, 1200f, 3600f, 7200f, 14400f, 3600f, 1200f),

            new FluidPipeMaterial("europium", "铕", 7500,
                    24000f, 48000f, 144000f, 288000f, 576000f, n(), n()),
            new FluidPipeMaterial("crudeBronzeAlloy", "粗青铜合金", 2000,
                    1000f, 2000f, 6000f, 12000f, 24000f, n(), n()),
            new FluidPipeMaterial("maragingSteel300", "马氏体时效钢300", 2500,
                    28000f, 56000f, 168000f, 336000f, 672000f, n(), n()),
            new FluidPipeMaterial("hastelloyX", "哈斯特洛依合金-X", 4200,
                    40000f, 80000f, 240000f, 480000f, 960000f, n(), n()),
            new FluidPipeMaterial("tungsten", "钨", 7200,
                    8640f, 17280f, 51840f, 103680f, 207360f, n(), n()),
            new FluidPipeMaterial("lead", "铅", 1200,
                    680f, 1360f, 4080f, 8160f, 16320f, n(), n())
    };
}
