package mdtnh.combat.impl.abilities;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mdtnh.combat.api.visual.LockStateProvider;
import mdtnh.combat.impl.MdtCombatFx;
import mdtnh.combat.impl.MdtCombatModifiers;
import mdtnh.combat.impl.MdtCombatUtil;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Small set of UV+ active mechanics referenced by the 105-unit design.
 * These are intentionally independent from movement AI so RTS CommandAI remains usable.
 */
public final class AdvancedAbilities {

    public static class EmergencyRepair extends Ability {
        public float threshold = 0.22f;
        public float healFraction = 0.18f;
        public float cooldown = 60f * 12f;
        private float timer;

        @Override
        public void update(Unit unit) {
            timer = Math.max(0f, timer - Time.delta);
            if (timer > 0f || unit.healthf() > threshold) return;
            if (!Vars.net.client()) unit.heal(unit.maxHealth * healFraction);
            Fx.healWaveMend.at(unit.x, unit.y, unit.hitSize * 1.3f, unit.team.color);
            timer = cooldown;
        }
    }

    public static class InterceptMatrix extends Ability {
        public float range = 150f;
        public float reload = 9f;
        public int maxPerScan = 2;
        public float maxDamage = 450f;
        private float timer;

        @Override
        public void update(Unit unit) {
            timer += Time.delta;
            if (timer < reload) return;
            timer = 0f;

            Seq<Bullet> candidates = new Seq<>();
            for (Bullet b : Groups.bullet) {
                if (b.team == unit.team || !b.within(unit, range) || !b.type.absorbable) continue;
                if (b.damage > maxDamage) continue;
                candidates.add(b);
            }
            int count = Math.min(maxPerScan, candidates.size);
            for (int i = 0; i < count; i++) {
                Bullet best = null;
                float bestScore = -1f;
                for (Bullet b : candidates) {
                    float score = b.damage + b.vel.len() * 8f;
                    if (score > bestScore) {
                        bestScore = score;
                        best = b;
                    }
                }
                if (best == null) break;
                candidates.remove(best);
                Fx.pointHit.at(best.x, best.y, best.rotation(), unit.team.color);
                best.absorb();
            }
        }
    }

    public static class BarrierPulse extends Ability {
        public float range = 92f;
        public float reload = 180f;
        public float push = 3.2f;
        public float weakBulletDamage = 28f;
        private float timer;

        @Override
        public void update(Unit unit) {
            timer += Time.delta;
            if (timer < reload) return;

            boolean triggered = false;
            for (Bullet b : Groups.bullet) {
                if (b.team != unit.team && b.within(unit, range) && b.damage <= weakBulletDamage) {
                    triggered = true;
                    break;
                }
            }
            if (!triggered && Units.closestTarget(unit.team, unit.x, unit.y, range) == null) return;

            timer = 0f;
            if (!Vars.net.client()) {
                Units.nearbyEnemies(unit.team, unit.x, unit.y, range, enemy -> {
                    MdtCombatUtil.push(enemy, unit.x, unit.y, push);
                    enemy.apply(StatusEffects.slow, 35f);
                });
                for (Bullet b : Groups.bullet) {
                    if (b.team != unit.team && b.within(unit, range) && b.damage <= weakBulletDamage) b.absorb();
                }
            }
            Fx.shockwave.at(unit.x, unit.y, range, unit.team.color);
        }
    }

    public static class GravityField extends Ability {
        public float range = 110f;
        public float pull = 0.16f;

        @Override
        public void update(Unit unit) {
            Units.nearbyEnemies(unit.team, unit.x, unit.y, range, enemy -> {
                float falloff = 1f - Mathf.clamp(enemy.dst(unit) / range);
                MdtCombatUtil.pull(enemy, unit.x, unit.y, pull * (0.35f + falloff) * Time.delta);
                enemy.apply(StatusEffects.slow, 8f);
            });
        }
    }

    public static class TargetMark extends Ability {
        public float range = 260f;
        public float bonus = 0.10f;
        public float duration = 20f;
        private Teamc target;

        @Override
        public void update(Unit unit) {
            target = Units.closestTarget(unit.team, unit.x, unit.y, range);
            if (target != null) MdtCombatModifiers.mark(target, bonus, duration);
        }

        @Override
        public void draw(Unit unit) {
            if (target == null) return;
            Draw.color(unit.team.color);
            Draw.alpha(0.58f);
            Lines.stroke(1.2f);
            Lines.square(target.x(), target.y(), 10f + Mathf.absin(Time.time, 5f, 3f), 45f);
            Draw.reset();
        }
    }

    public static class LockOn extends Ability implements LockStateProvider {
        public float fullLockTime = 150f;
        public float maxBonus = 0.20f;
        private Teamc target;
        private float progress;

        @Override
        public void update(Unit unit) {
            Teamc current = null;
            for (WeaponMount mount : unit.mounts) {
                if (mount.target != null) {
                    current = mount.target;
                    break;
                }
            }

            if (current == null) {
                progress = Math.max(0f, progress - Time.delta / 45f);
                if (progress <= 0f) target = null;
                return;
            }

            if (current != target) {
                target = current;
                progress = 0f;
            } else {
                progress = Mathf.clamp(progress + Time.delta / fullLockTime);
            }

            MdtCombatModifiers.mark(target, maxBonus * progress, 6f);
        }

        @Override public Teamc lockTarget(Unit unit) { return target; }
        @Override public float lockProgress(Unit unit) { return progress; }
    }

    public static class Execution extends Ability {
        public float threshold = 0.25f;
        public float bonus = 0.28f;

        @Override
        public void update(Unit unit) {
            for (WeaponMount mount : unit.mounts) {
                if (mount.target instanceof Unit target && target.healthf() <= threshold) {
                    MdtCombatModifiers.mark(target, bonus, 6f);
                    return;
                }
            }
        }
    }

    private AdvancedAbilities() {}
}
