package mdtnh.abilities;

import arc.util.Time;
import mindustry.gen.Unit;

/**
 * Out-of-combat self maintenance.
 *
 * Health regeneration begins only after the unit has avoided health damage for a while,
 * so this rewards disengagement and logistics instead of replacing dedicated support.
 */
public class CombatRepairAbility extends MdtAbility {

    public float delay = 60f * 5f;
    /** Fraction of max health repaired per second once maintenance is active. */
    public float healPerSecond = 0.006f;

    protected float lastHealth = -1f;

    public CombatRepairAbility() {
        super("combatrepair");
    }

    @Override
    public void created(Unit unit) {
        lastHealth = unit.health;
        data = 0f;
    }

    @Override
    public void update(Unit unit) {
        if (lastHealth < 0f) lastHealth = unit.health;

        if (unit.health < lastHealth - 0.001f) {
            data = 0f;
        } else {
            data += Time.delta;
        }

        lastHealth = unit.health;

        if (data >= delay && unit.health < unit.maxHealth()) {
            unit.heal(unit.maxHealth() * healPerSecond / 60f * Time.delta);
        }
    }
}
