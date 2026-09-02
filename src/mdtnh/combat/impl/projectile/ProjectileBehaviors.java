package mdtnh.combat.impl.projectile;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectSet;
import arc.util.Time;
import arc.util.Tmp;
import mdtnh.combat.api.projectile.MdtProjectileBehavior;
import mdtnh.combat.impl.MdtCombatFx;
import mdtnh.combat.impl.MdtCombatRuntime;
import mdtnh.combat.impl.MdtCombatStatuses;
import mdtnh.combat.impl.MdtCombatUtil;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Hitboxc;
import mindustry.gen.Posc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Tile;

/**
 * Concrete, reusable attack implementations. A08/A09/A25 are retired in Phase 3.
 * All mutable per-projectile state is kept in behavior state objects, never in shared BulletType fields.
 */
public final class ProjectileBehaviors {

    // A01
    public static class BackSprayFragment implements MdtProjectileBehavior {
    public BulletType fragment;
    public int fragments = 12;
    public float cone = 62f;
    public float offset = 8f;

    public BackSprayFragment(BulletType fragment) {
        this.fragment = fragment;
    }

    private void spawn(Bullet b, float x, float y, float targetSize) {
        if (fragment == null) return;

        float forward = Math.max(offset, targetSize * 0.58f + offset);
        float ox = x + Angles.trnsx(b.rotation(), forward);
        float oy = y + Angles.trnsy(b.rotation(), forward);

        for (int i = 0; i < fragments; i++) {
            float t = fragments <= 1 ? 0.5f : i / (float)(fragments - 1);
            float angle = b.rotation() + Mathf.lerp(-cone / 2f, cone / 2f, t);
            fragment.create(b, ox, oy, angle, 1f, 1f);
        }

        // No parent-child guide lines. The split children themselves are compact triangles.
        Fx.flakExplosion.at(ox, oy);
    }

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        float size = entity instanceof Unit u ? u.hitSize : 16f;
        spawn(b, entity.x(), entity.y(), size);
    }

    @Override
    public void afterHitTile(Bullet b, Building build, float x, float y,
                             float initialHealth, boolean direct, Object state) {
        spawn(b, x, y, build.hitSize());
    }
}

    // A02
    public static class StickyExplosive implements MdtProjectileBehavior {
        public float delay = 90f;
        public float radius = 45f;
        public float damage = 180f;

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
                MdtCombatRuntime.addSticky(b.team, entity, entity.x(), entity.y(), delay, radius, damage);

        }

        @Override
        public void afterHitTile(Bullet b, Building build, float x, float y,
                                 float initialHealth, boolean direct, Object state) {
            MdtCombatRuntime.addSticky(b.team, build, x, y, delay, radius, damage);
        }
    }

    // A03
    public static class InertiaShot implements MdtProjectileBehavior {
    public float minMultiplier = 0.65f;
    public float maxMultiplier = 1.70f;

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new float[]{1f};
    }

    @Override
    public void init(Bullet b, Object state) {
        float ratio = 0f;
        if (b.owner instanceof Unit u) {
            ratio = Mathf.clamp(u.vel.len() / Math.max(0.001f, u.speed()));
        }

        float mult = Mathf.lerp(minMultiplier, maxMultiplier, ratio);
        b.damage *= mult;
        ((float[])state)[0] = mult;
    }

    @Override
    public void draw(Bullet b, Object state) {
        float mult = ((float[])state)[0];
        float power = Mathf.clamp((mult - minMultiplier) / Math.max(0.01f, maxMultiplier - minMultiplier));

        // Only recolor the projectile and add a compact tail; no screen flash or long streaks.
        Tmp.c1.set(Color.valueOf("c9d7df")).lerp(Color.valueOf("ffd27f"), power);
        Draw.color(Tmp.c1);
        Draw.alpha(0.94f);
        Drawf.tri(
            b.x, b.y,
            4.6f + 1.2f * power,
            8.5f + 2.5f * power,
            b.rotation()
        );

        Draw.alpha(0.16f + 0.24f * power);
        Drawf.tri(
            b.x + Angles.trnsx(b.rotation() + 180f, 3.2f),
            b.y + Angles.trnsy(b.rotation() + 180f, 3.2f),
            2.8f + 1.0f * power,
            5.5f + 4f * power,
            b.rotation() + 180f
        );
        Draw.reset();
    }
}

    // A04
    public static class MassImpact implements MdtProjectileBehavior {
        public float extraPerSize = 1.8f;
        public float referenceSize = 30f;
        public float impulse = 9f;

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
            if (entity instanceof Unit u) {
                float sizeScale = Mathf.clamp(u.hitSize / referenceSize, 0.45f, 3f);
                u.damagePierce(extraPerSize * u.hitSize);
                float push = impulse / Math.max(0.55f, sizeScale);
                MdtCombatUtil.impulse(u, b.rotation(), push);
                MdtCombatFx.pulse.at(u.x, u.y, u.hitSize, Color.valueOf("f1d09a"));
            }
        }

        @Override
        public void afterHitTile(Bullet b, Building build, float x, float y,
                                 float initialHealth, boolean direct, Object state) {
            build.damage(extraPerSize * build.hitSize());
            MdtCombatFx.pulse.at(x, y, build.hitSize(), Color.valueOf("f1d09a"));
        }
    }

    // A05
    public static class TractorLance implements MdtProjectileBehavior {
        public float duration = 60f;
        public float pull = 0.24f;

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
            if (entity instanceof Unit u && b.owner instanceof Posc source) {
                MdtCombatRuntime.addTether(source, u, duration, pull);
            }
        }
    }

    // A06
    public static class DisplacementShot implements MdtProjectileBehavior {
        public float impulse = 13f;

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
            if (entity instanceof Unit u) {
                float scale = Mathf.clamp(28f / Math.max(10f, u.hitSize), 0.25f, 1.35f);
                MdtCombatUtil.impulse(u, b.rotation(), impulse * scale);
                MdtCombatFx.cross.at(u.x, u.y, b.rotation(), Color.valueOf("a8d8ff"));
            }
        }
    }

    // A07 primer
    public static class ReactionPrimer implements MdtProjectileBehavior {
    public int add = 1;
    public int max = 3;
    public float duration = 240f;
    public float debuffDuration = 120f;

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        if (!(entity instanceof Healthc health)) return;

        int stacks = MdtCombatRuntime.addReaction(health, b.team, add, max, duration);

        if (entity instanceof Unit unit) {
            unit.apply(StatusEffects.sapped, debuffDuration);
        }

        Color c = Color.valueOf("ffc06e");
        MdtCombatFx.reactionParticles.at(entity.x(), entity.y(), stacks, c);
    }
}

    // A07 detonator
    public static class ReactionDetonator implements MdtProjectileBehavior {
    public float damagePerStack = 100f;
    public float triggerRadius = 76f;
    public float baseDamage = 45f;

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new boolean[]{false};
    }

    private void detonateArea(Bullet b, float x, float y, Object state) {
        boolean[] fired = (boolean[])state;
        if (fired[0]) return;
        fired[0] = true;

        final int[] totalStacks = {0};

        Units.nearbyEnemies(b.team, x, y, triggerRadius, unit -> {
            int stacks = MdtCombatRuntime.consumeReaction(unit);
            if (stacks <= 0) return;

            totalStacks[0] += stacks;
            unit.unapply(StatusEffects.sapped);
            MdtCombatFx.reactionParticles.at(unit.x, unit.y, stacks, Color.valueOf("ff9c61"));
        });

        // Spatial build-group intersection is unsafe here in Mindustry 159.7:
        // this EntityGroup may not have a QuadTree, so intersect() can dereference
        // a null tree. Detonation is infrequent, so a direct group scan is safer.
        Groups.build.each(build -> {
            if (build == null || !build.isValid() || build.team == b.team) return;
            if (!build.within(x, y, triggerRadius)) return;

            int stacks = MdtCombatRuntime.consumeReaction(build);
            if (stacks <= 0) return;

            totalStacks[0] += stacks;
            MdtCombatFx.reactionParticles.at(build.x, build.y, stacks, Color.valueOf("ff9c61"));
        });

        if (totalStacks[0] <= 0) return;

        float damage = baseDamage + totalStacks[0] * damagePerStack;
        float radius = triggerRadius * (0.72f + Math.min(totalStacks[0], 8) * 0.035f);

        Damage.damage(b.team, x, y, radius, damage, true, true, true);
        Fx.massiveExplosion.at(x, y);
        Fx.bigShockwave.at(x, y);
        Effect.shake(3.2f, 6f, x, y);
    }

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        detonateArea(b, entity.x(), entity.y(), state);
    }

    @Override
    public void afterHitTile(Bullet b, Building build, float x, float y,
                             float initialHealth, boolean direct, Object state) {
        detonateArea(b, x, y, state);
    }

    @Override
    public void afterHit(Bullet b, float x, float y, boolean createFrags, Object state) {
        detonateArea(b, x, y, state);
    }
}





    // A10
    public static class CuttingAnchor implements MdtProjectileBehavior {
        public float duration = 180f;
        public float lineDamage = 120f;
        public float lineWidth = 8f;

        private void add(Bullet b, float x, float y) {
            int key = b.owner != null ? b.owner.id() : b.id;
            MdtCombatRuntime.addAnchor(key, b.team, x, y, duration, lineDamage, lineWidth);
        }

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
            add(b, entity.x(), entity.y());
        }

        @Override
        public void afterHitTile(Bullet b, Building build, float x, float y,
                                 float initialHealth, boolean direct, Object state) {
            add(b, x, y);
        }
    }

    // A11
    public static class PhaseProjectile implements MdtProjectileBehavior {
    /** Distance from aim point at which the projectile attempts a phase jump. */
    public float triggerDistance = 105f;
    /** Maximum jump length. */
    public float phaseDistance = 70f;
    /** Leave this much distance before the aim point after jumping. */
    public float landingClearance = 26f;
    public float fallbackLifeFraction = 0.58f;

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new boolean[]{false};
    }

    @Override
    public void update(Bullet b, Object state) {
        boolean[] done = (boolean[])state;
        if (done[0]) return;

        boolean hasAim = b.aimX >= 0f && b.aimY >= 0f;
        float remaining = hasAim ? Mathf.dst(b.x, b.y, b.aimX, b.aimY) : Float.POSITIVE_INFINITY;

        boolean shouldJump = hasAim
            ? remaining <= triggerDistance && remaining > landingClearance + 6f
            : b.fin() >= fallbackLifeFraction;

        if (!shouldJump) return;

        done[0] = true;

        float angle = hasAim ? Angles.angle(b.x, b.y, b.aimX, b.aimY) : b.rotation();
        float jump = hasAim
            ? Math.min(phaseDistance, Math.max(0f, remaining - landingClearance))
            : phaseDistance;

        if (jump <= 1f) return;

        Fx.teleportOut.at(b.x, b.y, angle, Color.valueOf("b7b4ff"));
        MdtCombatFx.phase.at(b.x, b.y, angle, Color.valueOf("b7b4ff"));

        b.x += Angles.trnsx(angle, jump);
        b.y += Angles.trnsy(angle, jump);
        b.rotation(angle);

        Fx.teleport.at(b.x, b.y, angle, Color.valueOf("d9d7ff"));
        MdtCombatFx.phase.at(b.x, b.y, angle, Color.valueOf("d9d7ff"));
        Effect.shake(1.6f, 3f, b.x, b.y);
    }
}

    // A12
    public static class CollapsingRing implements MdtProjectileBehavior {
        public BulletType child;
        public int count = 10;
        public float radius = 50f;

        public CollapsingRing(BulletType child) {
            this.child = child;
        }

        private void spawn(Bullet b, float x, float y) {
            if (child == null) return;
            for (int i = 0; i < count; i++) {
                float around = i * 360f / count;
                float sx = x + Angles.trnsx(around, radius);
                float sy = y + Angles.trnsy(around, radius);
                child.create(b, sx, sy, around + 180f, 1f, 1f);
            }
            MdtCombatFx.pulse.at(x, y, radius, Color.valueOf("9de1ff"));
        }

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
            spawn(b, entity.x(), entity.y());
        }

        @Override
        public void afterHitTile(Bullet b, Building build, float x, float y,
                                 float initialHealth, boolean direct, Object state) {
            spawn(b, x, y);
        }
    }

    // A13
    public static class FractureStack implements MdtProjectileBehavior {
    public int threshold = 4;
    public float duration = 240f;
    public float burstRadius = 46f;
    public float burstDamage = 240f;
    public BulletType fragment;
    public int fragments = 10;

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        if (!(entity instanceof Healthc health)) return;

        int stacks = MdtCombatRuntime.addFracture(health, b.team, 1, threshold, duration);
        if (stacks < threshold) return;

        MdtCombatRuntime.clearFracture(health);
        Damage.damage(b.team, entity.x(), entity.y(), burstRadius, burstDamage, true, true, true);

        if (fragment != null) {
            for (int i = 0; i < fragments; i++) {
                float angle = i * 360f / fragments;
                fragment.create(b, entity.x(), entity.y(), angle, 1f, 1f);
            }
        }

        // No pre-trigger red fracture scribbles; only the full-stack rupture is visible.
        Fx.massiveExplosion.at(entity.x(), entity.y());
        Fx.flakExplosion.at(entity.x(), entity.y());
        Effect.shake(3.6f, 7f, entity.x(), entity.y());
    }
}

    // A14
    public static class DelayedFuse implements MdtProjectileBehavior {
    public float delay = 42f;
    public float blastRadius = 54f;
    public float blastDamage = 185f;

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new boolean[]{false};
    }

    private void arm(Bullet b, float x, float y, Object state) {
        boolean[] armed = (boolean[])state;
        if (armed[0]) return;
        armed[0] = true;

        MdtCombatRuntime.addDelayedBlast(
            b.team, x, y, delay,
            blastRadius, blastDamage,
            Color.valueOf("ffbc73")
        );

        MdtCombatFx.softPulse.at(x, y, 0f, Color.valueOf("ffbc73"));
    }

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        arm(b, entity.x(), entity.y(), state);
    }

    @Override
    public void afterHitTile(Bullet b, Building build, float x, float y,
                             float initialHealth, boolean direct, Object state) {
        arm(b, x, y, state);
    }

    @Override
    public void afterHit(Bullet b, float x, float y, boolean createFrags, Object state) {
        arm(b, x, y, state);
    }
}

    // A15
    public static class SubsurfaceShockwave implements MdtProjectileBehavior {
    public int arms = 6;
    public int eruptionsPerArm = 4;
    public float spacing = 28f;
    public float interval = 7f;
    public float radius = 25f;
    public float damage = 58f;
    public float knockback = 2.6f;

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new boolean[]{false};
    }

    private boolean validGround(float x, float y) {
        Tile tile = Vars.world.tileWorld(x, y);
        if (tile == null) return false;
        return tile.floor() != Blocks.space && tile.floor() != Blocks.empty;
    }

    private void scheduleOnce(Bullet b, float x, float y, Object state) {
        boolean[] fired = (boolean[])state;
        if (fired[0]) return;
        fired[0] = true;

        if (!validGround(x, y)) return;

        Team team = b.team;
        float base = b.rotation();

        for (int arm = 0; arm < arms; arm++) {
            float angle = base + arm * 360f / arms;

            for (int step = 1; step <= eruptionsPerArm; step++) {
                final int fstep = step;
                final float fangle = angle;

                Time.run(step * interval, () -> {
                    float ex = x + Angles.trnsx(fangle, spacing * fstep);
                    float ey = y + Angles.trnsy(fangle, spacing * fstep);

                    Tile tile = Vars.world.tileWorld(ex, ey);
                    if (tile == null || tile.floor() == Blocks.space || tile.floor() == Blocks.empty) return;

                    if (!Vars.net.client()) {
                        Damage.damage(team, ex, ey, radius, damage, false, false, true);
                        Units.nearbyEnemies(team, ex, ey, radius * 1.25f, unit -> {
                            float scale = Mathf.clamp(28f / Math.max(10f, unit.hitSize), 0.35f, 1.4f);
                            MdtCombatUtil.impulse(unit, fangle, knockback * scale);
                        });
                    }

                    // Ground dust rather than glowing yellow rings.
                    Fx.unitLandSmall.at(ex, ey, Math.max(0.9f, radius / 18f), tile.floor().mapColor);
                });
            }
        }

        Tile center = Vars.world.tileWorld(x, y);
        if (center != null) {
            Fx.unitLand.at(x, y, 1.6f, center.floor().mapColor);
        }
        Effect.shake(3f, 6f, x, y);
    }

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        scheduleOnce(b, entity.x(), entity.y(), state);
    }

    @Override
    public void afterHitTile(Bullet b, Building build, float x, float y,
                             float initialHealth, boolean direct, Object state) {
        scheduleOnce(b, x, y, state);
    }

    @Override
    public void afterHit(Bullet b, float x, float y, boolean createFrags, Object state) {
        scheduleOnce(b, x, y, state);
    }
}

    // A16
    public static class TopAttackSplit implements MdtProjectileBehavior {
    public BulletType child;
    public int count = 5;
    public float splitAt = 0.62f;
    public float spread = 28f;

    public TopAttackSplit(BulletType child) {
        this.child = child;
    }

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        float initial = bullet.rotation();
        if (bullet.aimX >= 0f && bullet.aimY >= 0f) {
            float aim = Angles.angle(bullet.x, bullet.y, bullet.aimX, bullet.aimY);
            if (Angles.angleDist(initial, aim) <= 75f) initial = aim;
        }
        return new float[]{0f, initial};
    }

    @Override
    public void update(Bullet b, Object state) {
        float[] data = (float[])state;
        if (data[0] > 0f || b.fin() < splitAt || child == null) return;
        data[0] = 1f;

        float base = data[1];

        for (int i = 0; i < count; i++) {
            float off = count <= 1 ? 0f : Mathf.lerp(-spread / 2f, spread / 2f, i / (float)(count - 1));
            Bullet spawned = child.create(b, b.x, b.y, base + off, 1f, 1f);
            if (spawned != null && b.aimX >= 0f && b.aimY >= 0f) {
                spawned.aimX = b.aimX;
                spawned.aimY = b.aimY;
            }
        }

        // No child-parent guide line; each split child is rendered as a small directional triangle.
        Fx.flakExplosion.at(b.x, b.y);
        b.remove();
    }
}

    // A17
    public static class DecoySalvo implements MdtProjectileBehavior {
        public BulletType decoy;
        public int count = 4;
        public float spread = 34f;

        public DecoySalvo(BulletType decoy) {
            this.decoy = decoy;
        }

        @Override
        public void init(Bullet b, Object state) {
            if (decoy == null) return;
            for (int i = 0; i < count; i++) {
                float off = count <= 1 ? 0f : Mathf.lerp(-spread / 2f, spread / 2f, i / (float)(count - 1));
                decoy.create(b, b.x, b.y, b.rotation() + off, 1f, 1f);
            }
        }
    }

    // A18
    public static class ChargedChainLightning implements MdtProjectileBehavior {
    public int jumps = 4;
    public float range = 90f;
    public float firstDamage = 34f;
    public float growth = 1.28f;

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        if (!(entity instanceof Unit first)) return;

        ObjectSet<Unit> used = new ObjectSet<>();
        used.add(first);
        Unit current = first;

        for (int i = 0; i < jumps; i++) {
            final ObjectSet<Unit> currentUsed = used;
            Unit next = Units.closestEnemy(
                b.team, current.x, current.y, range,
                u -> !currentUsed.contains(u)
            );
            if (next == null) break;

            float damage = firstDamage * Mathf.pow(growth, i);
            float angle = current.angleTo(next);
            int length = Math.max(1, (int)(current.dst(next) / Vars.tilesize));

            // Actual lightning damage + the much more visible vanilla chain-lightning renderer.
            Lightning.create(b, Pal.surge, damage * 0.55f, current.x, current.y, angle, length);
            Fx.chainLightning.at(current.x, current.y, 0f, Pal.surge, next);
            Fx.hitLancer.at(next.x, next.y, angle, Pal.surge);
            next.damagePierce(damage * 0.45f);

            used.add(next);
            current = next;
        }
    }
}

    // A19
    public static class ShieldDrain implements MdtProjectileBehavior {
    public float drain = 160f;
    public float transferEfficiency = 0.65f;
    public float ownerShieldCap = 1400f;

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        if (!(entity instanceof Unit target)) return;

        float taken = Math.min(Math.max(0f, target.shield), drain);
        if (taken <= 0f) return;

        target.shield = Math.max(0f, target.shield - taken);

        if (b.owner instanceof Unit owner) {
            owner.shield = Math.min(ownerShieldCap, owner.shield + taken * transferEfficiency);

            MdtCombatFx.shieldTransfer.at(
                target.x, target.y, 0f, Color.white,
                new MdtCombatFx.ShieldTransferData(
                    owner,
                    target.team.color,
                    owner.team.color
                )
            );
        }
    }
}

    // A20
    public static class AblationBeamHit implements MdtProjectileBehavior {
        public float duration = 180f;
        public float armorPerStack = 2f;
        public int maxStacks = 5;

        @Override
        public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
            if (entity instanceof Unit u) {
                int stacks = MdtCombatRuntime.addAblation(u, duration, armorPerStack, maxStacks);
                u.damagePierce(stacks * armorPerStack * 2.5f);
                MdtCombatFx.spark.at(u.x, u.y, b.rotation(), Color.valueOf("ffb36e"));
            }
        }
    }

    // A21
    public static class AdhesiveCorrosion implements MdtProjectileBehavior {
    public float duration = 240f;

    @Override
    public void afterHitEntity(Bullet b, Hitboxc entity, float initialHealth, Object state) {
        if (entity instanceof Unit u) {
            u.apply(StatusEffects.corroded, duration);

            for (int i = 0; i < 5; i++) {
                Fx.corrosionVapor.at(
                    u.x + Mathf.range(u.hitSize * 0.35f),
                    u.y + Mathf.range(u.hitSize * 0.35f),
                    0f,
                    StatusEffects.corroded.color
                );
            }

            MdtCombatFx.pulse.at(u.x, u.y, 0f, StatusEffects.corroded.color);
        }
    }
}

    // A22
    public static class LimitedRicochet implements MdtProjectileBehavior {
        public int maxBounces = 2;
        public float velocityRetain = 0.78f;

        @Override
        public Object createState(Bullet bullet, Object originalPayload) {
            return new int[]{0};
        }

        @Override
        public void update(Bullet b, Object state) {
            int[] count = (int[])state;

            float nx = b.x + b.vel.x * Math.max(1f, Time.delta * 2f);
            float ny = b.y + b.vel.y * Math.max(1f, Time.delta * 2f);
            Tile tile = Vars.world.tileWorld(nx, ny);
            if (tile == null || !tile.solid()) return;

            if (count[0] >= maxBounces) {
                b.remove();
                return;
            }

            float dx = b.x - tile.worldx();
            float dy = b.y - tile.worldy();

            if (Math.abs(dx) > Math.abs(dy)) b.vel.x = -b.vel.x;
            else b.vel.y = -b.vel.y;

            b.vel.scl(velocityRetain);
            count[0]++;
            MdtCombatFx.cross.at(b.x, b.y, b.rotation(), Color.valueOf("e6e6e6"));
        }
    }

    // A23
    public static class MovingPrism implements MdtProjectileBehavior {
        public BulletType sideBullet;
        public float interval = 10f;

        public MovingPrism(BulletType sideBullet) {
            this.sideBullet = sideBullet;
        }

        @Override
        public Object createState(Bullet bullet, Object originalPayload) {
            return new float[]{0f};
        }

        @Override
        public void update(Bullet b, Object state) {
            if (sideBullet == null) return;
            float[] timer = (float[])state;
            timer[0] -= Time.delta;
            if (timer[0] <= 0f) {
                timer[0] = interval;
                sideBullet.create(b, b.x, b.y, b.rotation() + 90f, 1f, 0.65f);
                sideBullet.create(b, b.x, b.y, b.rotation() - 90f, 1f, 0.65f);
                MdtCombatFx.spark.at(b.x, b.y, b.rotation(), Color.valueOf("d1a6ff"));
            }
        }
    }

    // A24
    public static class GravityCore implements MdtProjectileBehavior {
    public float armAt = 0.28f;
    public float collapseAt = 0.92f;
    public float pullRange = 115f;
    public float pull = 0.62f;
    public float collapseRadius = 62f;
    public float collapseDamage = 205f;

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new boolean[]{false};
    }

    @Override
    public void update(Bullet b, Object state) {
        boolean[] done = (boolean[])state;
        if (done[0]) return;

        if (b.fin() >= armAt) {
            b.vel.scl(Mathf.clamp(1f - 0.20f * Time.delta));

            Units.nearbyEnemies(b.team, b.x, b.y, pullRange, u -> {
                float falloff = 1f - Mathf.clamp(u.dst(b) / pullRange);
                float sizeScale = Mathf.clamp(30f / Math.max(10f, u.hitSize), 0.35f, 1.25f);
                MdtCombatUtil.pull(u, b.x, b.y, pull * (0.35f + falloff) * sizeScale * Time.delta);
            });
        }

        if (b.fin() >= collapseAt) {
            done[0] = true;

            if (!Vars.net.client()) {
                Damage.damage(b.team, b.x, b.y, collapseRadius, collapseDamage, true, true, true);
            }

            Fx.massiveExplosion.at(b.x, b.y);
            Fx.bigShockwave.at(b.x, b.y);
            MdtCombatFx.pulse.at(b.x, b.y, 0f, Color.valueOf("a88cff"));
            Effect.shake(5f, 10f, b.x, b.y);
            b.remove();
        }
    }

    @Override
    public void draw(Bullet b, Object state) {
        if (b.fin() < armAt) return;

        float t = Time.time;
        Draw.color(Color.valueOf("9b82ff"));
        Draw.alpha(0.75f);
        Lines.stroke(1.8f + Mathf.absin(t, 5f, 0.8f));

        for (int ring = 0; ring < 3; ring++) {
            float radius = 18f + ring * 14f + Mathf.absin(t + ring * 11f, 6f, 5f);
            Lines.arc(b.x, b.y, radius, 0.55f, t * (1.4f + ring * 0.25f) + ring * 90f);
        }

        for (int i = 0; i < 8; i++) {
            float angle = i * 45f - t * 1.8f;
            float radius = pullRange * 0.45f;
            float px = b.x + Angles.trnsx(angle, radius);
            float py = b.y + Angles.trnsy(angle, radius);
            Lines.line(px, py, b.x + Angles.trnsx(angle, 12f), b.y + Angles.trnsy(angle, 12f));
        }

        Fill.circle(b.x, b.y, 5f + Mathf.absin(t, 4f, 2f));
        Draw.reset();
    }
}



    // A26
    public static class VelocityFuseFlak implements MdtProjectileBehavior {
    public float baseFuseRadius = 18f;
    public float speedFactor = 18f;
    public float searchRange = 110f;
    public float blastRadius = 42f;
    public float blastDamage = 135f;

    public static class FuseState {
        Unit target;
        float fuse;
        boolean done;
    }

    @Override
    public Object createState(Bullet bullet, Object originalPayload) {
        return new FuseState();
    }

    @Override
    public void update(Bullet b, Object state) {
        FuseState data = (FuseState)state;
        if (data.done) return;

        data.target = Units.closestEnemy(
            b.team, b.x, b.y, searchRange,
            u -> u.isFlying() && u.hittable()
        );

        if (data.target == null) {
            data.fuse = baseFuseRadius;
            return;
        }

        data.fuse = baseFuseRadius + data.target.vel.len() * speedFactor;

        if (data.target.within(b.x, b.y, data.fuse)) {
            data.done = true;

            if (!Vars.net.client()) {
                Damage.damage(b.team, b.x, b.y, blastRadius, blastDamage, false, true, false);
                Units.nearbyEnemies(b.team, b.x, b.y, blastRadius, u -> {
                    if (u.isFlying()) MdtCombatUtil.push(u, b.x, b.y, 2.2f);
                });
            }

            Fx.flakExplosion.at(b.x, b.y);
            Fx.bigShockwave.at(b.x, b.y);
            Effect.shake(2.5f, 5f, b.x, b.y);
            b.remove();
        }
    }

    @Override
    public void draw(Bullet b, Object state) {
        FuseState data = (FuseState)state;

        Draw.color(Color.valueOf("ffd39a"));
        Draw.alpha(data.target == null ? 0.18f : 0.78f);
        Lines.stroke(data.target == null ? 0.8f : 2f);
        Lines.circle(b.x, b.y, Math.max(baseFuseRadius, data.fuse));

        if (data.target != null) {
            Lines.line(b.x, b.y, data.target.x, data.target.y);
            Lines.circle(data.target.x, data.target.y, 4f + Mathf.absin(Time.time, 4f, 2f));
        }

        Draw.reset();
    }
}

    private ProjectileBehaviors() {}
}
