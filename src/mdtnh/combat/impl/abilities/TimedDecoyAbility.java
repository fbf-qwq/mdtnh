package mdtnh.combat.impl.abilities;

import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

/** Short-lived targetable helper unit used by the S10 chaff test. */
public class TimedDecoyAbility extends Ability {
    public float lifetime = 120f;
    private float time;

    public TimedDecoyAbility(float lifetime) {
        this.lifetime = lifetime;
        display = false;
    }

    @Override
    public void update(Unit unit) {
        // Chaff is a stationary false target, not a small fighter.
        // Zero velocity every tick so collision/knockback cannot make it drift.
        unit.vel.setZero();

        time += Time.delta;
        if (time >= lifetime && !Vars.net.client()) {
            unit.remove();
        }
    }

    @Override
    public Ability copy() {
        TimedDecoyAbility out = (TimedDecoyAbility)super.copy();
        out.time = 0f;
        return out;
    }
}
