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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用液体燃料发电机。内燃机和燃气轮机可共享该实现，只需登记不同燃料热值。
 */
public class MdtLiquidFuelGeneratorBlock extends Block {

    /** 每单位液体可转化的焦耳；LinkedHashMap 保证燃料选择顺序稳定。 */
    public final Map<Liquid, Float> fuelJoulesPerUnit = new LinkedHashMap<>();
    public float maxFuelUsePerSecond = 4f;

    public final EnergySpec energySpec = new EnergySpec();
    public String fallbackRegion = "combustion-generator";

    public MdtLiquidFuelGeneratorBlock(String name) {
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

        buildType = MdtLiquidFuelGeneratorBuild::new;
    }

    public MdtLiquidFuelGeneratorBlock fuel(Liquid liquid, float joulesPerUnit) {
        if (liquid != null && joulesPerUnit > 0f) {
            fuelJoulesPerUnit.put(liquid, joulesPerUnit);
        }
        return this;
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(fallbackRegion);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("mdt-energy", (MdtLiquidFuelGeneratorBuild build) -> new Bar(
                () -> Core.bundle.format("mdt.bar.energy",
                        Math.round(build.nodeState.energyJ),
                        Math.round(energySpec.capacityJ)),
                () -> Color.valueOf("ffd37f"),
                () -> energySpec.capacityJ <= 0f
                        ? 0f
                        : Math.min(1f, build.nodeState.energyJ / energySpec.capacityJ)
        ));
    }

    public class MdtLiquidFuelGeneratorBuild extends Building implements MdtEnergyNode {
        public final EnergyState nodeState = new EnergyState();

        @Override
        public Building energyBuilding() {
            return this;
        }

        @Override
        public EnergySpec energySpec() {
            return MdtLiquidFuelGeneratorBlock.this.energySpec;
        }

        @Override
        public EnergyState energyState() {
            return nodeState;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (liquids == null || fuelJoulesPerUnit.isEmpty()) return;

            float freeJ = Math.max(0f, energySpec.capacityJ - nodeState.energyJ);
            if (freeJ <= 0.0001f) return;

            for (Map.Entry<Liquid, Float> entry : fuelJoulesPerUnit.entrySet()) {
                Liquid fuel = entry.getKey();
                float joulesPerUnit = entry.getValue();
                float available = liquids.get(fuel);
                if (available <= 0.000001f || joulesPerUnit <= 0f) continue;

                float tickLimit = maxFuelUsePerSecond * Time.delta / 60f;
                float used = Math.min(
                        available,
                        Math.min(tickLimit, freeJ / joulesPerUnit)
                );
                if (used <= 0.000001f) return;

                liquids.remove(fuel, used);
                nodeState.add(used * joulesPerUnit, energySpec);
                return;
            }
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
