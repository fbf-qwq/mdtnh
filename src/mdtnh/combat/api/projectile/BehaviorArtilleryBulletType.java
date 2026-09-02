package mdtnh.combat.api.projectile;

import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;

/**
 * Artillery counterpart of BehaviorBasicBulletType.
 * Use it for airburst, subsurface shockwave, delayed bombardment, etc.
 */
public class BehaviorArtilleryBulletType extends ArtilleryBulletType {
    public final BulletBehaviorHost behaviorHost = new BulletBehaviorHost();

    public BehaviorArtilleryBulletType() {
        super();
    }

    public BehaviorArtilleryBulletType(float speed, float damage) {
        super(speed, damage);
    }

    public BehaviorArtilleryBulletType(float speed, float damage, String sprite) {
        super(speed, damage, sprite);
    }

    public BehaviorArtilleryBulletType behavior(MdtProjectileBehavior behavior) {
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
