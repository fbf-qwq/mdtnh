package mdtnh.abilities;

import arc.math.geom.Vec2;
import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

/** Defensive pulse: knockback + slow nearby enemies and delete weak absorbable bullets. */
public class BarrierPulseAbility extends MdtAbility {

    public float reload = 60f * 5f;
    public float range = 95f;
    public float knockback = 2.2f;
    public float maxBulletDamage = 45f;

    private final Vec2 knockVec = new Vec2();

    public BarrierPulseAbility() {
        super("barrierpulse");
    }

    @Override
    public void update(Unit unit) {
        data += Time.delta;
        if (data < reload) return;
        data = 0f;

        Units.nearbyEnemies(unit.team, unit.x, unit.y, range, other -> {
            knockVec.set(other.x - unit.x, other.y - unit.y);
            if (knockVec.len2() > 0.001f) {
                other.vel().add(knockVec.setLength(knockback));
            }
            other.apply(ModStatusEffects.gravitySlow, 60f);
        });

        Groups.bullet.intersect(
                unit.x - range,
                unit.y - range,
                range * 2f,
                range * 2f,
                bullet -> {
                    if (bullet.team != unit.team
                            && bullet.type().absorbable
                            && bullet.type().damage <= maxBulletDamage
                            && bullet.within(unit.x, unit.y, range)) {
                        Fx.absorb.at(bullet);
                        bullet.absorb();
                    }
                }
        );

        Fx.spawn.at(unit.x, unit.y, range);
    }
}
