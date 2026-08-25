package mdtnh;

import arc.Core;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;

/**
 * GTNH 风格资源机器注册。
 *
 * <p>当前实现 LV/MV/HV 三档矿机：17x17 / 33x33 / 49x49，
 * 单个矿格基准耗时 8s / 4s / 2s。矿石会被实际移除。</p>
 */
public final class GtResourceMachines {

    public static ModDrill minerLV;
    public static ModDrill minerMV;
    public static ModDrill minerHV;

    private GtResourceMachines() {
    }

    public static void load() {
        minerLV = createMiner("gt-miner-lv", VoltageTier.LV, 8, 8f);
        minerMV = createMiner("gt-miner-mv", VoltageTier.MV, 16, 4f);
        minerHV = createMiner("gt-miner-hv", VoltageTier.HV, 24, 2f);
    }

    private static ModDrill createMiner(
            String name,
            VoltageTier tier,
            int radius,
            float secondsPerOre
    ) {
        ModDrill miner = new ModDrill(name);
        miner.localizedName = Core.bundle.get(
                "block." + name + ".name",
                tier.name() + " 矿机"
        );
        miner.description = Core.bundle.get(
                "block." + name + ".description",
                "扫描 " + (radius * 2 + 1) + "x" + (radius * 2 + 1)
                        + " 范围，每次开采一个矿格。"
        );

        miner.top = radius;
        miner.button = radius;
        miner.left = radius;
        miner.right = radius;
        miner.drillTime = secondsPerOre * 60f;
        miner.itemCapacity = 80;
        miner.mineOneOrePerCycle = true;
        miner.consumeOreOverlay = true;
        miner.useHardnessSpeedPenalty = false;

        miner.usesMdtEnergy = true;
        miner.energyPerMineJ = tier.maxVoltageV * secondsPerOre;
        miner.energySpec.role = mdtnh.energy.EnergySpec.Role.consumer;
        miner.energySpec.voltageV = tier.maxVoltageV;
        miner.energySpec.minInputVoltageV = tier.minVoltageV;
        miner.energySpec.maxInputVoltageV = tier.maxVoltageV;
        miner.energySpec.capacityJ = Math.max(
                tier.capacityJ,
                miner.energyPerMineJ
        );
        miner.energySpec.maxInputA = 1;
        miner.energySpec.maxOutputA = 0;

        miner.health = 320;
        miner.size = 2;
        miner.alwaysUnlocked = true;
        miner.buildVisibility = BuildVisibility.shown;
        miner.requirements(Category.production, ItemStack.with(
                Items.copper, 80,
                Items.lead, 50,
                Items.silicon, 25
        ));
        return miner;
    }
}
