package mdtnh.ai;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Dedicated controller for DroneBayWeapon children.
 *
 * The drone never leaves the carrier formation to chase a target.
 * Targeting is still handled by the vanilla AIController weapon update,
 * so weapons can fire at enemies while movement remains carrier-relative.
 */
public class CarrierOrbitAI extends AIController {
    private Unit carrier;
    private final int slot;
    private final int slots;
    private final float orbitRadius;
    private final float phase;
    private final float angularSpeed;

    private final Vec2 desired = new Vec2();
    private final Vec2 move = new Vec2();
    private float orphanTime;

    public CarrierOrbitAI(Unit carrier, int slot, int slots, float orbitRadius, float phase) {
        this.carrier = carrier;
        this.slot = Math.max(0, slot);
        this.slots = Math.max(1, slots);
        this.orbitRadius = Math.max(18f, orbitRadius);
        this.phase = phase;
        this.angularSpeed = 0.45f + (slot % 3) * 0.035f;
    }

    @Override
    public void updateMovement() {
        if (carrier == null || !carrier.isValid() || carrier.dead || carrier.team != unit.team) {
            orphanTime += Time.delta;
            unit.vel.scl(0.82f);

            // Drones are carrier subsystems rather than independent units.
            // Give a short grace period for network/despawn order, then remove them.
            if (orphanTime >= 120f && !Vars.net.client()) {
                unit.remove();
            }
            return;
        }

        orphanTime = 0f;

        float slotAngle = slot * (360f / slots);
        float angle = phase + slotAngle + Time.time * angularSpeed;

        // The carrier's own size participates in the radius so large MAX carriers
        // do not have their drones clipped inside the hull.
        float radius = Math.max(orbitRadius, carrier.hitSize * 0.72f + unit.hitSize * 0.7f);

        desired.set(
            carrier.x + Angles.trnsx(angle, radius),
            carrier.y + Angles.trnsy(angle, radius)
        );

        move.set(desired).sub(unit);

        // When the carrier accelerates, bias the drone movement with carrier velocity.
        // This keeps the formation attached without teleporting.
        if (!carrier.vel.isZero()) {
            move.add(carrier.vel.x * 8f, carrier.vel.y * 8f);
        }

        float distance = unit.dst(desired);
        float speed = prefSpeed();

        if (distance > radius * 2.5f) {
            move.setLength(speed);
        } else if (distance > 3f) {
            move.limit(speed);
        } else {
            // Tangential motion keeps a clean visible orbit even when the drone
            // is already almost exactly on its desired slot.
            move.trns(angle + 90f, speed * 0.55f);
        }

        if (!move.isZero() && !move.isNaN() && !move.isInfinite()) {
            unit.movePref(move);
        }

        if (!unit.vel.isZero()) {
            unit.lookAt(unit.vel.angle());
        }
    }

    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground) {
        if (carrier == null || !carrier.isValid()) {
            return super.findMainTarget(x, y, range, air, ground);
        }

        // Drones defend the carrier's local battlespace instead of selecting
        // a far-away strategic target and trying to chase it.
        float localRange = Math.max(range, orbitRadius + unit.range());
        return Units.closestTarget(
            unit.team,
            carrier.x,
            carrier.y,
            localRange,
            other -> other.checkTarget(air, ground),
            build -> ground && build.block.targetable
        );
    }

    public Unit carrier() {
        return carrier;
    }

    public int slot() {
        return slot;
    }
}
