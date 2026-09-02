package mdtnh.combat.api.projectile;

import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;

/**
 * Lifecycle dispatcher used by modular BulletType subclasses.
 */
public final class BulletBehaviorHost {
    public final Seq<MdtProjectileBehavior> behaviors = new Seq<>();

    public BulletBehaviorHost add(MdtProjectileBehavior behavior) {
        behaviors.add(behavior);
        return this;
    }

    public void init(Bullet bullet) {
        Object original = bullet.data;
        MdtBulletRuntime runtime = new MdtBulletRuntime(original, behaviors.size);
        bullet.data = runtime;

        for (int i = 0; i < behaviors.size; i++) {
            MdtProjectileBehavior behavior = behaviors.get(i);
            Object state = behavior.createState(bullet, original);
            runtime.state(i, state);
            behavior.init(bullet, state);
        }
    }

    public void update(Bullet bullet) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;

        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).update(bullet, runtime.states[i]);
        }
    }

    public void draw(Bullet bullet) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;

        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).draw(bullet, runtime.states[i]);
        }
    }

    public void beforeHitEntity(Bullet bullet, Hitboxc entity, float health) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).beforeHitEntity(bullet, entity, health, runtime.states[i]);
        }
    }

    public void afterHitEntity(Bullet bullet, Hitboxc entity, float health) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).afterHitEntity(bullet, entity, health, runtime.states[i]);
        }
    }

    public void beforeHitTile(Bullet bullet, Building build, float x, float y,
                              float health, boolean direct) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).beforeHitTile(bullet, build, x, y, health, direct, runtime.states[i]);
        }
    }

    public void afterHitTile(Bullet bullet, Building build, float x, float y,
                             float health, boolean direct) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).afterHitTile(bullet, build, x, y, health, direct, runtime.states[i]);
        }
    }

    public void beforeHit(Bullet bullet, float x, float y, boolean createFrags) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).beforeHit(bullet, x, y, createFrags, runtime.states[i]);
        }
    }

    public void afterHit(Bullet bullet, float x, float y, boolean createFrags) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).afterHit(bullet, x, y, createFrags, runtime.states[i]);
        }
    }

    public void despawned(Bullet bullet) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).despawned(bullet, runtime.states[i]);
        }
    }

    public void removed(Bullet bullet) {
        MdtBulletRuntime runtime = runtime(bullet);
        if (runtime == null) return;
        for (int i = 0; i < behaviors.size; i++) {
            behaviors.get(i).removed(bullet, runtime.states[i]);
        }
    }

    public MdtBulletRuntime runtime(Bullet bullet) {
        return bullet.data instanceof MdtBulletRuntime runtime ? runtime : null;
    }
}
