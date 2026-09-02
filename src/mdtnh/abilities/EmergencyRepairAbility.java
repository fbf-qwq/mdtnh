package mdtnh.abilities;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Unit;

/** One emergency heal below a health threshold, followed by a long cooldown. */
public class EmergencyRepairAbility extends MdtAbility {

    public float threshold = 0.25f;
    public float healFraction = 0.16f;
    public float reload = 60f * 30f;

    public EmergencyRepairAbility() {
        super("emergencyrepair");
    }

    @Override
    public void update(Unit unit) {
        if (data > 0f) {
            data = Math.max(0f, data - Time.delta);
        }

        if (data <= 0f && unit.healthf() <= threshold && unit.health > 0f) {
            unit.heal(unit.maxHealth() * healFraction);
            Fx.healWave.at(unit.x, unit.y, unit.hitSize);
            data = reload;
        }
    }
}
