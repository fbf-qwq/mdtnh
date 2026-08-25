package mdtnh.energy;

import arc.Core;
import arc.graphics.Color;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;

/**
 * 以蒸汽为燃料的 MDT 离散电网发电机。
 *
 * <p>具体 steam Liquid 由内容注册层注入；本类只负责“蒸汽 -> 内部 EnergyState ->
 * 额定电压电流包”的通用行为。</p>
 */
public class MdtSteamTurbineBlock extends Block {

    public Liquid steam;
    public float joulesPerSteamUnit = 0.5f;
    public float maxSteamUsePerSecond = 16f;

    public final EnergySpec energySpec = new EnergySpec();
    public String fallbackRegion = "steam-generator";

    public MdtSteamTurbineBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasLiquids = true;
        liquidCapacity = 60f;
        hasPower = false;
        outputsPower = false;
        consumesPower = false;

        energySpec.role = EnergySpec.Role.generator;
        energySpec.voltageV = 8f;
        energySpec.minInputVoltageV = 0f;
        energySpec.maxInputVoltageV = 8f;
        energySpec.capacityJ = 256f;
        energySpec.maxInputA = 0;
        energySpec.maxOutputA = 1;

        buildType = MdtSteamTurbineBuild::new;
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(fallbackRegion);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("mdt-energy", (MdtSteamTurbineBuild build) -> new Bar(
                () -> Core.bundle.format("mdt.bar.energy",
                        Math.round(build.nodeState.energyJ),
                        Math.round(energySpec.capacityJ)),
                () -> Color.valueOf("ffd37f"),
                () -> energySpec.capacityJ <= 0f
                        ? 0f
                        : Math.min(1f, build.nodeState.energyJ / energySpec.capacityJ)
        ));
    }

    public class MdtSteamTurbineBuild extends Building implements MdtEnergyNode {
        public final EnergyState nodeState = new EnergyState();

        @Override
        public Building energyBuilding() {
            return this;
        }

        @Override
        public EnergySpec energySpec() {
            return MdtSteamTurbineBlock.this.energySpec;
        }

        @Override
        public EnergyState energyState() {
            return nodeState;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (steam == null) return;

            SteamEnergyConverter.convert(
                    this,
                    nodeState,
                    energySpec,
                    steam,
                    joulesPerSteamUnit,
                    maxSteamUsePerSecond,
                    Time.delta
            );
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            nodeState.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            nodeState.read(read, energySpec);
        }
    }
}
