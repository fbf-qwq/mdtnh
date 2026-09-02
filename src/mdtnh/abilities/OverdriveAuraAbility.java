package mdtnh.abilities;

import mdtnh.status.ModStatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/**
 * Fire-control / propulsion coordination aura.
 *
 * This is intentionally not a shield or heal field. Nearby allies receive a short
 * reload/speed coordination status while they remain near the carrier.
 */
public class OverdriveAuraAbility extends MdtAbility {

    public float range = 90f;
    public float duration = 18f;
    public boolean affectSelf = true;

    public OverdriveAuraAbility() {
        super("overdriveaura");
    }

    @Override
    public void update(Unit unit) {
        if (affectSelf) unit.apply(ModStatusEffects.overdriveAura, duration);

        Units.nearby(unit.team, unit.x, unit.y, range, other -> {
            if (other != unit) other.apply(ModStatusEffects.overdriveAura, duration);
        });
    }
}
