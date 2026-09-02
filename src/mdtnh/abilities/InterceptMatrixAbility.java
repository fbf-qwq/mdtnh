package mdtnh.abilities;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;

/**
 * Higher-level point defense. Scans periodically and spends a limited number of
 * interceptions on sufficiently dangerous absorbable projectiles.
 */
public class InterceptMatrixAbility extends MdtAbility {

    public float range = 150f;
    public float scanInterval = 10f;
    public float threatThreshold = 45f;
    public int maxInterceptsPerScan = 3;

    protected float timer;

    public InterceptMatrixAbility() {
        super("interceptmatrix");
    }

    @Override
    public void update(Unit unit) {
        timer += Time.delta;
        if (timer < scanInterval) return;
        timer = 0f;

        final int[] intercepted = {0};

        Groups.bullet.intersect(
                unit.x - range,
                unit.y - range,
                range * 2f,
                range * 2f,
                bullet -> {
                    if (intercepted[0] >= maxInterceptsPerScan) return;
                    if (!valid(unit, bullet)) return;

                    float threat =
                            bullet.type().damage
                                    + bullet.type().splashDamage
                                    + bullet.vel.len() * 4f
                                    + Math.max(0f, bullet.type().homingPower) * 100f;

                    if (threat >= threatThreshold) {
                        Fx.absorb.at(bullet);
                        bullet.absorb();
                        intercepted[0]++;
                    }
                }
        );
    }

    private boolean valid(Unit unit, Bullet bullet) {
        return bullet.team != unit.team
                && bullet.type().absorbable
                && bullet.within(unit.x, unit.y, range);
    }
}
