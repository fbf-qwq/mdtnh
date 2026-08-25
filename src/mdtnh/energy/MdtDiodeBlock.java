package mdtnh.energy;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;

/**
 * GT 风格二极管：从除正面以外的侧面接收能量，只从正面输出，并限制最大安培数。
 */
public class MdtDiodeBlock extends Block {

    public float minInputVoltageV = 0f;
    public float voltageV = 8f;
    public int maxConfigAmperage = 16;
    public float capacityJ = 256f;
    public String fallbackRegion = "battery";

    public MdtDiodeBlock(String name) {
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

        config(Integer.class, (MdtDiodeBuild build, Integer value) -> {
            build.allowedA = build.sanitizeAmperage(value == null ? 1 : value);
            build.refreshSpec();
        });
        configClear((MdtDiodeBuild build) -> {
            build.allowedA = 1;
            build.refreshSpec();
        });

        buildType = MdtDiodeBuild::new;
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(fallbackRegion);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("mdt-diode-energy", (MdtDiodeBuild build) -> new Bar(
                () -> Core.bundle.format("mdt.bar.io",
                        build.nodeState.inputA,
                        build.nodeState.outputA),
                () -> arc.graphics.Color.valueOf("84f491"),
                () -> Math.min(1f,
                        Math.max(build.nodeState.inputA, build.nodeState.outputA)
                                / (float)Math.max(1, build.allowedA))
        ));
    }

    public class MdtDiodeBuild extends Building implements MdtEnergyNode {
        public final EnergyState nodeState = new EnergyState();
        private final EnergySpec runtimeSpec = new EnergySpec();
        public int allowedA = 1;

        public MdtDiodeBuild() {
            refreshSpec();
        }

        private int sanitizeAmperage(int value) {
            int maximum = Math.max(1, maxConfigAmperage);
            int target = Math.max(1, Math.min(value, maximum));
            int result = 1;
            while (result <= maximum / 2 && result * 2 <= target) {
                result *= 2;
            }
            return result;
        }

        private int nextAmperage() {
            int maximum = Math.max(1, maxConfigAmperage);
            return allowedA >= maximum ? 1 : Math.min(maximum, allowedA * 2);
        }

        private void refreshSpec() {
            allowedA = sanitizeAmperage(allowedA);
            runtimeSpec.role = EnergySpec.Role.battery;
            runtimeSpec.voltageV = voltageV;
            runtimeSpec.minInputVoltageV = minInputVoltageV;
            runtimeSpec.maxInputVoltageV = voltageV;
            runtimeSpec.capacityJ = Math.max(capacityJ, voltageV * allowedA * 2f);
            runtimeSpec.maxInputA = allowedA;
            runtimeSpec.maxOutputA = allowedA;
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
                    Core.bundle.get("mdtnh.diode.current", "限流") + ": " + allowedA + "A",
                    () -> configure(nextAmperage())
            ).width(220f);
        }

        @Override
        public Object config() {
            return allowedA;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(allowedA);
            nodeState.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            allowedA = revision >= 1 ? sanitizeAmperage(read.i()) : 1;
            refreshSpec();
            nodeState.read(read, runtimeSpec);
        }

        @Override
        public byte version() {
            return 1;
        }
    }
}
