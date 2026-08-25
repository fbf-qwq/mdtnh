package mdtnh.energy;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;

/**
 * GT 风格相邻电压等级变压器。
 *
 * <p>默认降压：1A 高压 -> 4A 低压；切换为升压后：4A 低压 -> 1A 高压。
 * 正面（rotation 指向）始终为输出侧，其余相邻侧为输入侧。</p>
 */
public class MdtTransformerBlock extends Block {

    public float lowMinVoltageV = 0f;
    public float lowVoltageV = 8f;
    public float highMinVoltageV = 8.0001f;
    public float highVoltageV = 32f;

    public int lowSideAmperage = 4;
    public int highSideAmperage = 1;
    public float capacityJ = 256f;
    public String fallbackRegion = "battery";

    public MdtTransformerBlock(String name) {
        super(name);
        update = true;
        solid = true;
        rotate = true;
        configurable = true;
        saveConfig = true;
        copyConfig = true;
        hasPower = false;
        outputsPower = false;
        consumesPower = false;

        config(Boolean.class, (MdtTransformerBuild build, Boolean value) -> {
            build.stepUp = value != null && value;
            build.refreshSpec();
        });
        configClear((MdtTransformerBuild build) -> {
            build.stepUp = false;
            build.refreshSpec();
        });

        buildType = MdtTransformerBuild::new;
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(fallbackRegion);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("mdt-transformer-energy", (MdtTransformerBuild build) -> new Bar(
                () -> Core.bundle.format("mdt.bar.energy",
                        Math.round(build.nodeState.energyJ),
                        Math.round(build.runtimeSpec.capacityJ)),
                () -> arc.graphics.Color.valueOf("ffd37f"),
                () -> build.runtimeSpec.capacityJ <= 0f
                        ? 0f
                        : Math.min(1f, build.nodeState.energyJ / build.runtimeSpec.capacityJ)
        ));
    }

    public class MdtTransformerBuild extends Building implements MdtEnergyNode {
        public final EnergyState nodeState = new EnergyState();
        private final EnergySpec runtimeSpec = new EnergySpec();
        public boolean stepUp;

        public MdtTransformerBuild() {
            refreshSpec();
        }

        private void refreshSpec() {
            runtimeSpec.role = EnergySpec.Role.battery;
            runtimeSpec.capacityJ = Math.max(
                    capacityJ,
                    Math.max(highVoltageV * highSideAmperage,
                            lowVoltageV * lowSideAmperage) * 2f
            );

            if (stepUp) {
                runtimeSpec.voltageV = highVoltageV;
                runtimeSpec.minInputVoltageV = lowMinVoltageV;
                runtimeSpec.maxInputVoltageV = lowVoltageV;
                runtimeSpec.maxInputA = Math.max(0, lowSideAmperage);
                runtimeSpec.maxOutputA = Math.max(0, highSideAmperage);
            } else {
                runtimeSpec.voltageV = lowVoltageV;
                runtimeSpec.minInputVoltageV = highMinVoltageV;
                runtimeSpec.maxInputVoltageV = highVoltageV;
                runtimeSpec.maxInputA = Math.max(0, highSideAmperage);
                runtimeSpec.maxOutputA = Math.max(0, lowSideAmperage);
            }

            runtimeSpec.maxWireVoltageV = Float.MAX_VALUE;
            runtimeSpec.maxWireCurrentA = 0;
            runtimeSpec.wireLossV = 0f;
            nodeState.energyJ = Math.min(nodeState.energyJ, runtimeSpec.capacityJ);
        }

        @Override
        public Building energyBuilding() {
            return this;
        }

        @Override
        public EnergySpec energySpec() {
            refreshSpec();
            return runtimeSpec;
        }

        @Override
        public EnergyState energyState() {
            return nodeState;
        }

        @Override
        public boolean allowsBatteryBridge(MdtEnergyNode other) {
            return true;
        }

        @Override
        public boolean canSendEnergyTo(MdtEnergyNode neighbor) {
            return sideOf(neighbor) == (rotation & 3);
        }

        @Override
        public boolean canReceiveEnergyFrom(MdtEnergyNode neighbor) {
            return sideOf(neighbor) != (rotation & 3);
        }

        private int sideOf(MdtEnergyNode neighbor) {
            if (neighbor == null || neighbor.energyBuilding() == null) return -1;
            Building other = neighbor.energyBuilding();
            float dx = other.x - x;
            float dy = other.y - y;
            if (Math.abs(dx) >= Math.abs(dy)) {
                return dx >= 0f ? 0 : 2;
            }
            return dy >= 0f ? 1 : 3;
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button(
                    stepUp
                            ? Core.bundle.get("mdtnh.transformer.step-up", "模式：升压（4A低压 → 1A高压）")
                            : Core.bundle.get("mdtnh.transformer.step-down", "模式：降压（1A高压 → 4A低压）"),
                    () -> configure(!stepUp)
            ).width(260f);
        }

        @Override
        public Object config() {
            return stepUp;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(stepUp);
            nodeState.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            stepUp = revision >= 1 && read.bool();
            refreshSpec();
            nodeState.read(read, runtimeSpec);
        }

        @Override
        public byte version() {
            return 1;
        }
    }
}
