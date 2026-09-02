package mdtnh.abilities;

import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.gen.Unit;

/**
 * Periodic dash that can only trigger while the unit is already moving at (nearly) full speed.
 *
 * This prevents BurstDrive from acting as a start-from-rest accelerator or firing while
 * the unit is merely rotating/aiming.
 */
public class BurstDriveAbility extends MdtAbility {

    public float reload = 60f * 5f;
    public float impulse = 5f;
    public float maxVelocityMultiplier = 2.2f;

    /** Fraction of normal max speed required before the burst can trigger. */
    public float minSpeedFraction = 0.95f;

    private final Vec2 impulseVec = new Vec2();

    public BurstDriveAbility() {
        super("burstdrive");
    }

    public BurstDriveAbility(float reload, float impulse) {
        this();
        this.reload = reload;
        this.impulse = impulse;
    }

    @Override
    public void update(Unit unit) {
        data += Time.delta;
        if (data < reload) return;

        float normalSpeed = Math.max(unit.speed(), 0.01f);
        float currentSpeed = unit.vel().len();

        // "Full-speed forward" is defined by actual movement velocity.
        // The burst continues in the current movement direction.
        if (!unit.moving() || currentSpeed < normalSpeed * minSpeedFraction) return;

        float angle = unit.vel().angle();
        impulseVec.trns(angle, impulse);

        unit.vel().add(impulseVec);
        unit.vel().limit(normalSpeed * maxVelocityMultiplier);

        data = 0f;
    }
}
