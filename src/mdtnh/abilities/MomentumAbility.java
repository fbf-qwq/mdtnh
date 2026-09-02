package mdtnh.abilities;

import mdtnh.status.ModStatusEffects;
import mindustry.gen.Unit;

/**
 * Passive high-speed combat state.
 *
 * A unit moving near its normal maximum speed receives a small offensive/mobility bonus.
 * This is deliberately weaker and more passive than UV BurstDrive.
 */
public class MomentumAbility extends MdtAbility {

    public float speedFraction = 0.92f;
    public float duration = 8f;

    public MomentumAbility() {
        super("momentum");
    }

    @Override
    public void update(Unit unit) {
        float max = Math.max(unit.speed(), 0.01f);

        if (unit.moving() && unit.vel().len() >= max * speedFraction) {
            unit.apply(ModStatusEffects.momentum, duration);
        }
    }
}
