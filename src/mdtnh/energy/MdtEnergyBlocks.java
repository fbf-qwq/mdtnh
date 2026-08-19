package mdtnh.energy;

import arc.Core;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;

/** 注册用于测试离散能源系统的四种示例方块。 */
public final class MdtEnergyBlocks {
    public static MdtEnergyBlock exampleGenerator;
    public static MdtEnergyBlock exampleWire;
    public static MdtEnergyBlock exampleConsumer;
    public static MdtEnergyBlock exampleBattery;

    private MdtEnergyBlocks() {
    }

    public static void load() {
        exampleGenerator = new MdtEnergyBlock("example-generator") {{
            localizedName = Core.bundle.get("block.example-generator.name", "示例发电机");
            description = Core.bundle.get("block.example-generator.description",
                    "点击后可分别设置启停、输出电压、每秒增能速度和最大输出电流，"
                            + "用于测试欠压、正常供电、过压与限流行为。");
            fallbackRegion = "combustion-generator";
            role = EnergyRole.generator;

            /*
             * 电压和最大输出电流仍作为方块级后备默认值。
             * 固定 generationJPerSecond 必须为 0，避免旧能源系统的
             * 自动发电与建筑实例的可配置发电重复叠加。
             */
            voltageV = 12f;
            capacityJ = 2147483647f;
            initialEnergyFraction = 0f;
            generationJPerSecond = 0f;
            maxInputA = 0;
            maxOutputA = 8;

            configurableGenerator = true;
            defaultGeneratorEnabled = true;
            defaultConfiguredVoltageV = 12f;
            defaultConfiguredGenerationJPerSecond = 96f;
            defaultConfiguredMaxOutputA = 8;

            /*
             * 调试输入上限。可按项目需要继续提高，但不建议完全取消，
             * 否则一次误输入可能让寻路循环或能量数值失去控制。
             */
            maxConfigVoltageV = 536_870_912f;
            maxConfigGenerationJPerSecond = 1_000_000_000f;
            maxConfigOutputA = 1_000_000;

            health = 260;
            size = 1;
            alwaysUnlocked = true;
            buildVisibility = BuildVisibility.shown;
            requirements(Category.power, ItemStack.with(
                    Items.copper, 40,
                    Items.lead, 25
            ));
        }};

        exampleWire = new MdtEnergyBlock("example-wire") {{
            localizedName = Core.bundle.get("block.example-wire.name", "示例导线");
            description = Core.bundle.get("block.example-wire.description",
                    "自动连接四周同队的示例电力方块。最高承受128V，"
                            + "每格最多通过16A，每个1A包损失0.05V；超限会烧毁。");
            fallbackRegion = "power-node";
            role = EnergyRole.wire;

            capacityJ = 0f;
            voltageV = 0f;
            maxInputA = 0;
            maxOutputA = 0;
            maxWireVoltageV = 128f;
            maxWireCurrentA = 16;
            wireLossV = 0.05f;

            health = 90;
            size = 1;
            alwaysUnlocked = true;
            buildVisibility = BuildVisibility.shown;
            requirements(Category.power, ItemStack.with(
                    Items.copper, 4,
                    Items.lead, 2
            ));
        }};

        exampleConsumer = new MdtEnergyBlock("example-consumer") {{
            localizedName = Core.bundle.get("block.example-consumer.name", "示例用电器");
            description = Core.bundle.get("block.example-consumer.description",
                    "相当于每秒自动减少48J的12V电池；每秒最多接收6A。");
            fallbackRegion = "arc";
            role = EnergyRole.consumer;

            voltageV = 12f;
            capacityJ = 600f;
            initialEnergyFraction = 1f;
            consumptionJPerSecond = 48f;
            maxInputA = 6;
            maxOutputA = 0;

            health = 220;
            size = 1;
            alwaysUnlocked = true;
            buildVisibility = BuildVisibility.shown;
            requirements(Category.power, ItemStack.with(
                    Items.copper, 30,
                    Items.lead, 20,
                    Items.silicon, 10
            ));
        }};

        exampleBattery = new MdtEnergyBlock("example-battery") {{
            localizedName = Core.bundle.get("block.example-battery.name", "示例电池");
            description = Core.bundle.get("block.example-battery.description",
                    "储存12000J；每秒最多输入10A、输出10A。"
                            + "只向用电器放电，避免电池互相来回传输。");
            fallbackRegion = "battery";
            role = EnergyRole.battery;

            voltageV = 12f;
            capacityJ = 12000f;
            initialEnergyFraction = 0.25f;
            maxInputA = 10;
            maxOutputA = 10;

            health = 320;
            size = 1;
            alwaysUnlocked = true;
            buildVisibility = BuildVisibility.shown;
            requirements(Category.power, ItemStack.with(
                    Items.copper, 50,
                    Items.lead, 60,
                    Items.silicon, 20
            ));
        }};
    }
}