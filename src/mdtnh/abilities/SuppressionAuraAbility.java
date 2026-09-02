package mdtnh.abilities;

import mdtnh.status.ModStatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/**
 * Local fire-control disruption.
 *
 * Nearby enemies are slowed and lose reload/damage efficiency. This gives support and
 * brawler units a battlefield-control identity that is not based on shields or healing.
 */
public class SuppressionAuraAbility extends MdtAbility {

    public float range = 85f;
    public float duration = 15f;

    public SuppressionAuraAbility() {
        super("suppressionaura");
    }

    @Override
    public void update(Unit unit) {
        Units.nearbyEnemies(unit.team, unit.x, unit.y, range, other ->
                other.apply(ModStatusEffects.suppressed, duration)
        );
    }
}
