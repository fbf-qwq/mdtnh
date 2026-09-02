package mdtnh.combat.api.projectile;

import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;

/**
 * BasicBulletType that delegates all useful lifecycle points to a behavior host.
 * This is the default base for A01/A02/A03/... style custom projectiles.
 */
public class BehaviorBasicBulletType extends BasicBulletType {
    public final BulletBehaviorHost behaviorHost = new BulletBehaviorHost();

    public BehaviorBasicBulletType() {
        super();
    }

    public BehaviorBasicBulletType(float speed, float damage) {
        super(speed, damage);
    }

    public BehaviorBasicBulletType(float speed, float damage, String sprite) {
        super(speed, damage, sprite);
    }

    public BehaviorBasicBulletType behavior(MdtProjectileBehavior behavior) {
        behaviorHost.add(behavior);
        return this;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        behaviorHost.init(b);
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        behaviorHost.update(b);
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);
        behaviorHost.draw(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        behaviorHost.beforeHitEntity(b, entity, health);
        super.hitEntity(b, entity, health);
        behaviorHost.afterHitEntity(b, entity, health);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        behaviorHost.beforeHitTile(b, build, x, y, initialHealth, direct);
        super.hitTile(b, build, x, y, initialHealth, direct);
        behaviorHost.afterHitTile(b, build, x, y, initialHealth, direct);
    }

    @Override
    public void hit(Bullet b, float x, float y, boolean createFrags) {
        behaviorHost.beforeHit(b, x, y, createFrags);
        super.hit(b, x, y, createFrags);
        behaviorHost.afterHit(b, x, y, createFrags);
    }

    @Override
    public void despawned(Bullet b) {
        super.despawned(b);
        behaviorHost.despawned(b);
    }

    @Override
    public void removed(Bullet b) {
        super.removed(b);
        behaviorHost.removed(b);
    }
}
