package mdtnh.abilities;

import mdtnh.status.ModStatusEffects;
import mindustry.gen.Unit;

/**
 * Passive low-health combat mode.
 *
 * This does not heal the unit. It trades survival margin for a temporary offensive boost,
 * producing a very different identity from force fields and repair abilities.
 */
public class LastStandAbility extends MdtAbility {

    public float threshold = 0.28f;
    public float duration = 10f;

    public LastStandAbility() {
        super("laststand");
    }

    @Override
    public void update(Unit unit) {
        if (unit.healthf() <= threshold) {
            unit.apply(ModStatusEffects.lastStand, duration);
        }
    }
}
