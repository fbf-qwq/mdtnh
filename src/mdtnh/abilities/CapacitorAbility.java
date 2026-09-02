package mdtnh.abilities;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Unit;

/**
 * Charges while attacking or taking damage. At full charge, discharges into a shield reserve.
 * This is the conservative V1 mode from the design's possible capacitor outputs.
 */
public class CapacitorAbility extends MdtAbility {

    public float chargeTime = 60f * 8f;
    public float damageChargeScale = 2.5f;
    public float shieldGain = 450f;
    public float maxShield = 1200f;

    protected float lastHealth = -1f;

    public CapacitorAbility() {
        super("capacitor");
    }

    @Override
    public void created(Unit unit) {
        lastHealth = unit.health;
    }

    @Override
    public void update(Unit unit) {
        if (lastHealth < 0f) lastHealth = unit.health;

        if (unit.isShooting()) {
            data += Time.delta / chargeTime;
        }

        if (unit.health < lastHealth) {
            float lost = lastHealth - unit.health;
            data += lost / Math.max(unit.maxHealth(), 1f) * damageChargeScale;
        }

        lastHealth = unit.health;
        data = Mathf.clamp(data);

        if (data >= 0.999f) {
            unit.shield = Math.min(maxShield, unit.shield + shieldGain);
            Fx.healWave.at(unit.x, unit.y, unit.hitSize);
            data = 0f;
        }
    }
}
