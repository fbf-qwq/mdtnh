package mdtnh.ai;

import mdtnh.status.ModStatusEffects;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import mindustry.type.weapons.RepairBeamWeapon;
import mindustry.world.meta.BlockFlag;

/**
 * Target-value scoring. Higher score is better.
 *
 * This is intentionally a local scan rather than a full-map search. Retargeting is already
 * throttled by AIController, so the cost stays bounded.
 */
public final class TargetScorer {

    public static Teamc findBest(Unit seeker, AIProfile profile, boolean air, boolean ground) {
        float range = Math.max(360f, seeker.range() * profile.scanRangeMultiplier);

        final Teamc[] best = {null};
        final float[] bestScore = {-Float.MAX_VALUE};

        Units.nearbyEnemies(seeker.team, seeker.x, seeker.y, range, enemy -> {
            if (enemy.dead() || !enemy.targetable(seeker.team) || !enemy.checkTarget(air, ground)) return;
            float score = score(seeker, enemy, profile);
            if (score > bestScore[0]) {
                best[0] = enemy;
                bestScore[0] = score;
            }
        });

        if (ground) {
            Units.nearbyBuildings(seeker.x, seeker.y, range, build -> {
                if (!build.isValid() || build.team == seeker.team || !build.block.targetable) return;
                float score = score(seeker, build, profile);
                if (score > bestScore[0]) {
                    best[0] = build;
                    bestScore[0] = score;
                }
            });
        }

        return best[0];
    }

    public static float score(Unit seeker, Teamc target, AIProfile p) {
        float value = 0f;

        if (target instanceof Unit unit) {
            value += p.unitWeight;

            boolean support = unit.type.buildSpeed > 0.001f;

            if (!support) {
                for (Weapon weapon : unit.type.weapons) {
                    if (weapon instanceof RepairBeamWeapon) {
                        support = true;
                        break;
                    }
                }
            }

            if (!support) {
                for (Ability ability : unit.type.abilities) {
                    if (ability instanceof RepairFieldAbility
                            || ability instanceof ShieldRegenFieldAbility) {
                        support = true;
                        break;
                    }
                }
            }

            if (support) value += p.supportUnitWeight;
            if (unit.isBoss()) value += p.commanderUnitWeight;
            if (unit.healthf() < 0.25f) value += p.lowHealthWeight;
            if (unit.hasEffect(ModStatusEffects.marked)) value += p.markedWeight;

            value += Math.min(unit.maxHealth() / 120f, 70f);
        }

        if (target instanceof Building build) {
            var flags = build.block.flags;

            if (flags.contains(BlockFlag.core)) value += p.coreWeight;
            if (flags.contains(BlockFlag.turret)) value += p.turretWeight;
            if (flags.contains(BlockFlag.repair)) value += p.repairWeight;
            if (flags.contains(BlockFlag.factory)) value += p.factoryWeight;
            if (flags.contains(BlockFlag.generator)) value += p.generatorWeight;
            if (flags.contains(BlockFlag.storage)) value += p.storageWeight;
            if (flags.contains(BlockFlag.battery)) value += p.batteryWeight;
            if (flags.contains(BlockFlag.reactor)) value += p.reactorWeight;
            if (flags.contains(BlockFlag.drill)) value += p.drillWeight;

            value += Math.min(build.maxHealth() / 180f, 60f);
            if (build.healthf() < 0.25f) value += p.lowHealthWeight * 0.7f;
        }

        value -= seeker.dst(target) * p.distanceWeight;
        return value;
    }

    private TargetScorer() {
    }
}
