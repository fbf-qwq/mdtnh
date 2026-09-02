package mdtnh.abilities;

import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/**
 * Marks an enemy unit for coordinated targeting.
 *
 * In Phase 1, the mark is consumed by TargetScorer as a high-priority target signal.
 * A future bullet hook can add the design document's exact "+damage taken" rule.
 */
public class TargetMarkAbility extends MdtAbility {

    public float reload = 60f * 4f;
    public float duration = 60f * 6f;
    public float range = 220f;
    public boolean requireShooting = true;

    public TargetMarkAbility() {
        super("targetmark");
    }

    @Override
    public void update(Unit unit) {
        data += Time.delta;
        if (data < reload) return;
        if (requireShooting && !unit.isShooting()) return;

        Unit target = Units.closestEnemy(
                unit.team,
                unit.x,
                unit.y,
                range,
                other -> other.targetable(unit.team)
        );

        if (target != null) {
            target.apply(ModStatusEffects.marked, duration);
            Fx.spawn.at(target.x, target.y);
            data = 0f;
        }
    }
}
