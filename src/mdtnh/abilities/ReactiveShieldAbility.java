package mdtnh.abilities;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Unit;

/**
 * Passive damage-spike protection.
 *
 * A sufficiently large health loss triggers a temporary shield reserve. This is not a
 * permanent force field and therefore gives attack/support units a defensive response
 * without turning every chassis into a shield carrier.
 */
public class ReactiveShieldAbility extends MdtAbility {

    public float triggerDamage = 80f;
    public float shieldGain = 160f;
    public float maxShield = 420f;
    public float reload = 60f * 8f;

    protected float lastHealth = -1f;
    protected float cooldown;

    public ReactiveShieldAbility() {
        super("reactiveshield");
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

        if (lost >= triggerDamage && cooldown <= 0f) {
            unit.shield = Math.min(maxShield, unit.shield + shieldGain);
            Fx.healWave.at(unit.x, unit.y, unit.hitSize);
            cooldown = reload;
        }
    }
}
