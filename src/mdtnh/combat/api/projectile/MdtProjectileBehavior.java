package mdtnh.combat.api.projectile;

import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;

/**
 * Composable projectile behavior contract.
 *
 * Rules:
 * - configuration fields may live on the behavior object;
 * - mutable per-projectile state MUST be returned from createState(...);
 * - do not store a current Bullet/target in the behavior instance because BulletType is shared.
 */
public interface MdtProjectileBehavior {
    default Object createState(Bullet bullet, Object originalPayload) {
        return null;
    }

    default void init(Bullet bullet, Object state) {
    }

    default void update(Bullet bullet, Object state) {
    }

    default void draw(Bullet bullet, Object state) {
    }

    default void beforeHitEntity(Bullet bullet, Hitboxc entity, float initialHealth, Object state) {
    }

    default void afterHitEntity(Bullet bullet, Hitboxc entity, float initialHealth, Object state) {
    }

    default void beforeHitTile(Bullet bullet, Building build, float x, float y,
                               float initialHealth, boolean direct, Object state) {
    }

    default void afterHitTile(Bullet bullet, Building build, float x, float y,
                              float initialHealth, boolean direct, Object state) {
    }

    default void beforeHit(Bullet bullet, float x, float y, boolean createFrags, Object state) {
    }

    default void afterHit(Bullet bullet, float x, float y, boolean createFrags, Object state) {
    }

    default void despawned(Bullet bullet, Object state) {
    }

    default void removed(Bullet bullet, Object state) {
    }
}
