package mdtnh.abilities;

import arc.math.Angles;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/**
 * Damage-triggered defensive counter-pulse.
 *
 * It damages and pushes nearby enemy units after a sufficiently large incoming hit.
 * The cooldown prevents rapid multi-hit weapons from turning it into continuous AOE DPS.
 */
public class RetaliationPulseAbility extends MdtAbility {

    public float triggerDamage = 120f;
    public float range = 85f;
    public float damage = 45f;
    public float knockback = 1.3f;
    public float reload = 60f * 8f;

    protected float lastHealth = -1f;
    protected float cooldown;

    public RetaliationPulseAbility() {
        super("retaliationpulse");
    }

    @Override
    public void created(Unit unit) {
        lastHealth = unit.health;
        cooldown = 0f;
    }

    @Override
    public void update(Unit unit) {
        if (lastHealth < 0f) lastHealth = unit.health;
        if (cooldown > 0f) cooldown = Math.max(0f, cooldown - Time.delta);

        float lost = Math.max(0f, lastHealth - unit.health);
        lastHealth = unit.health;

        if (lost < triggerDamage || cooldown > 0f) return;

        Units.nearbyEnemies(unit.team, unit.x, unit.y, range, other -> {
            other.damage(damage);

            float angle = unit.angleTo(other);
            other.vel().add(
                    Angles.trnsx(angle, knockback),
                    Angles.trnsy(angle, knockback)
            );
        });

        Fx.spawn.at(unit.x, unit.y);
        cooldown = reload;
    }
}
