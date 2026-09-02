package mdtnh.combat.impl;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.EventType.Trigger;
import mindustry.game.Team;
import mindustry.gen.Healthc;
import mindustry.gen.Posc;
import mindustry.gen.Unit;

/**
 * Small shared runtime for mechanics that must outlive the projectile that created them.
 * No AI decisions are made here.
 */
public final class MdtCombatRuntime {
    private static boolean installed;

    private static final Seq<StickyCharge> stickies = new Seq<>();
    private static final Seq<Tether> tethers = new Seq<>();
    private static final Seq<AnchorLink> anchorLinks = new Seq<>();
    private static final Seq<DelayedBlast> blasts = new Seq<>();
    private static final Seq<StackMark> reactions = new Seq<>();
    private static final Seq<StackMark> fractures = new Seq<>();
    private static final Seq<AblationMark> ablations = new Seq<>();

    public static void install() {
        if (installed) return;
        installed = true;
        MdtDamageBus.install();

        Events.run(Trigger.update, MdtCombatRuntime::update);
        Events.run(Trigger.drawOver, MdtCombatRuntime::draw);
    }

    public static void addSticky(Team team, Posc target, float fallbackX, float fallbackY,
                                 float delay, float radius, float damage) {
        StickyCharge c = new StickyCharge();
        c.team = team;
        c.target = target;
        c.x = fallbackX;
        c.y = fallbackY;
        c.time = delay;
        c.radius = radius;
        c.damage = damage;
        stickies.add(c);
    }

    public static void addTether(Posc source, Unit target, float duration, float pull) {
        Tether t = new Tether();
        t.source = source;
        t.target = target;
        t.time = duration;
        t.pull = pull;
        tethers.add(t);
    }

    public static void addAnchor(int ownerKey, Team team, float x, float y,
                                 float duration, float damage, float width) {
        AnchorLink pending = null;
        for (int i = anchorLinks.size - 1; i >= 0; i--) {
            AnchorLink link = anchorLinks.get(i);
            if (link.ownerKey == ownerKey && !link.complete && link.time > 0f) {
                pending = link;
                break;
            }
        }

        if (pending == null) {
            pending = new AnchorLink();
            pending.ownerKey = ownerKey;
            pending.team = team;
            pending.x1 = x;
            pending.y1 = y;
            pending.time = duration;
            pending.damage = damage;
            pending.width = width;
            pending.complete = false;
            anchorLinks.add(pending);
        } else {
            pending.x2 = x;
            pending.y2 = y;
            pending.complete = true;
            pending.time = duration;
        }
    }

    public static void addDelayedBlast(Team team, float x, float y, float delay,
                                       float radius, float damage, Color color) {
        DelayedBlast b = new DelayedBlast();
        b.team = team;
        b.x = x;
        b.y = y;
        b.time = delay;
        b.radius = radius;
        b.damage = damage;
        b.color = color;
        blasts.add(b);
    }



    public static int addReaction(Healthc target, Team team, int add, int max, float duration) {
        StackMark mark = find(reactions, target);
        if (mark == null) {
            mark = new StackMark();
            mark.target = target;
            mark.team = team;
            reactions.add(mark);
        }
        mark.stacks = Math.min(max, mark.stacks + add);
        mark.max = max;
        mark.time = duration;
        return mark.stacks;
    }

    public static int reactionStacks(Healthc target) {
        StackMark mark = find(reactions, target);
        return mark == null ? 0 : mark.stacks;
    }

    public static int consumeReaction(Healthc target) {
        StackMark mark = find(reactions, target);
        if (mark == null) return 0;
        int value = mark.stacks;
        reactions.remove(mark);
        return value;
    }

    public static int addFracture(Healthc target, Team team, int add, int max, float duration) {
        StackMark mark = find(fractures, target);
        if (mark == null) {
            mark = new StackMark();
            mark.target = target;
            mark.team = team;
            fractures.add(mark);
        }
        mark.stacks = Math.min(max, mark.stacks + add);
        mark.max = max;
        mark.time = duration;
        return mark.stacks;
    }

    public static void clearFracture(Healthc target) {
        StackMark mark = find(fractures, target);
        if (mark != null) fractures.remove(mark);
    }

    public static int addAblation(Unit unit, float duration, float armorPerStack, int maxStacks) {
        AblationMark mark = null;
        for (AblationMark a : ablations) {
            if (a.unit == unit) {
                mark = a;
                break;
            }
        }
        if (mark == null) {
            mark = new AblationMark();
            mark.unit = unit;
            ablations.add(mark);
        }
        mark.stacks = Math.min(maxStacks, mark.stacks + 1);
        mark.time = duration;
        mark.armorPerStack = armorPerStack;
        return mark.stacks;
    }

    private static StackMark find(Seq<StackMark> list, Healthc target) {
        for (StackMark m : list) if (m.target == target) return m;
        return null;
    }

    private static void update() {
        if (!Vars.net.client()) {
            updateStickies();
            updateTethers();
            updateAnchors();
            updateBlasts();
            updateStacks(reactions);
            updateStacks(fractures);
            updateAblation();
        }
    }

    private static void updateStickies() {
        for (int i = stickies.size - 1; i >= 0; i--) {
            StickyCharge c = stickies.get(i);
            boolean dead = false;
            if (c.target != null) {
                c.x = c.target.x();
                c.y = c.target.y();
                if (c.target instanceof Healthc h) dead = h.dead();
            }
            c.time -= Time.delta;
            if (c.time <= 0f || dead) {
                Damage.damage(c.team, c.x, c.y, c.radius, c.damage, true, true, true);
                MdtCombatFx.pulse.at(c.x, c.y, c.radius, Color.valueOf("ff9d66"));
                stickies.remove(i);
            }
        }
    }

    private static void updateTethers() {
        for (int i = tethers.size - 1; i >= 0; i--) {
            Tether t = tethers.get(i);
            t.time -= Time.delta;
            if (t.time <= 0f || t.source == null || t.target == null || t.target.dead) {
                tethers.remove(i);
                continue;
            }
            float scale = Mathf.clamp(26f / Math.max(10f, t.target.hitSize), 0.25f, 1.25f);
            MdtCombatUtil.pull(t.target, t.source.x(), t.source.y(), t.pull * scale * Time.delta);
            if (t.source instanceof Unit sourceUnit && t.target.hitSize > sourceUnit.hitSize * 1.5f) {
                MdtCombatUtil.pull(sourceUnit, t.target.x, t.target.y, t.pull * 0.18f * Time.delta);
            }
        }
    }

    private static void updateAnchors() {
        for (int i = anchorLinks.size - 1; i >= 0; i--) {
            AnchorLink l = anchorLinks.get(i);
            l.time -= Time.delta;
            if (l.time <= 0f) {
                anchorLinks.remove(i);
                continue;
            }
            if (l.complete) {
                l.damageTimer -= Time.delta;
                if (l.damageTimer <= 0f) {
                    l.damageTimer = 8f;
                    MdtCombatUtil.damageLine(l.team, l.x1, l.y1, l.x2, l.y2, l.width, l.damage);
                }
            }
        }
    }

    private static void updateBlasts() {
        for (int i = blasts.size - 1; i >= 0; i--) {
            DelayedBlast b = blasts.get(i);
            b.time -= Time.delta;
            if (b.time <= 0f) {
                Damage.damage(b.team, b.x, b.y, b.radius, b.damage, true, true, true);
                MdtCombatFx.pulse.at(b.x, b.y, b.radius, b.color);
                blasts.remove(i);
            }
        }
    }





    private static void updateStacks(Seq<StackMark> list) {
        for (int i = list.size - 1; i >= 0; i--) {
            StackMark m = list.get(i);
            m.time -= Time.delta;
            if (m.time <= 0f || m.target == null || m.target.dead()) list.remove(i);
        }
    }

    private static void updateAblation() {
        for (int i = ablations.size - 1; i >= 0; i--) {
            AblationMark a = ablations.get(i);
            a.time -= Time.delta;
            if (a.time <= 0f || a.unit == null || a.unit.dead) {
                ablations.remove(i);
                continue;
            }
        }
    }

    private static void draw() {
        drawStickies();
        drawTethers();
        drawAnchors();
        drawBlasts();
        drawStacks(reactions, Color.valueOf("ffb15c"));
        drawAblation();
    }

    private static void drawStickies() {
        Draw.color(Color.valueOf("ff9d66"));
        Lines.stroke(1.1f);
        for (StickyCharge c : stickies) {
            Lines.circle(c.x, c.y, 4f + Mathf.absin(Time.time, 3f, 2f));
            Fill.circle(c.x, c.y, 1.5f);
        }
        Draw.reset();
    }

    private static void drawTethers() {
        Draw.color(Color.valueOf("aab9ff"));
        Lines.stroke(3.1f);

        for (Tether t : tethers) {
            if (t.source == null || t.target == null) continue;

            float x1 = t.source.x(), y1 = t.source.y();
            float x2 = t.target.x, y2 = t.target.y;
            float dst = Mathf.dst(x1, y1, x2, y2);

            // Near targets -> more, shorter dashes. Far targets -> fewer, wider gaps.
            int dashCount = Math.max(5, Math.min(18, (int)(18f - dst / 18f)));
            float duty = Mathf.clamp(0.45f + (1f - Mathf.clamp(dst / 180f)) * 0.35f, 0.45f, 0.80f);

            for (int i = 0; i < dashCount; i++) {
                float a = i / (float)dashCount;
                float b = (i + duty) / dashCount;
                if (b > 1f) b = 1f;

                float sx = Mathf.lerp(x1, x2, a);
                float sy = Mathf.lerp(y1, y2, a);
                float ex = Mathf.lerp(x1, x2, b);
                float ey = Mathf.lerp(y1, y2, b);

                Lines.line(sx, sy, ex, ey);
            }
        }

        Draw.reset();
    }

    private static void drawAnchors() {
        Draw.color(Color.valueOf("ff8ac8"));
        Lines.stroke(1.2f);
        for (AnchorLink l : anchorLinks) {
            Fill.circle(l.x1, l.y1, 2f);
            if (l.complete) {
                Fill.circle(l.x2, l.y2, 2f);
                Lines.line(l.x1, l.y1, l.x2, l.y2);
            }
        }
        Draw.reset();
    }

    private static void drawBlasts() {
        for (DelayedBlast b : blasts) {
            Draw.color(b.color);
            Draw.alpha(0.22f + Mathf.absin(Time.time, 5f, 0.12f));
            Lines.stroke(1.15f);
            Lines.circle(b.x, b.y, 6f + Mathf.absin(Time.time + b.x + b.y, 4f, 3f));

            for (int i = 0; i < 4; i++) {
                float a = i * 90f + Time.time * 0.5f;
                float r = 9f + Mathf.absin(Time.time + i * 8f, 5f, 2f);
                Fill.square(b.x + Angles.trnsx(a, r), b.y + Angles.trnsy(a, r), 1.2f, 45f);
            }
        }
        Draw.reset();
    }



    private static void drawStacks(Seq<StackMark> list, Color color) {
        Draw.color(color);
        for (StackMark m : list) {
            if (m.stacks <= 0) continue;

            Draw.alpha(0.20f + 0.08f * m.stacks);
            int particles = 2 + m.stacks * 2;

            for (int i = 0; i < particles; i++) {
                float angle = i * (360f / particles) + Time.time * (0.35f + 0.04f * m.stacks);
                float radius = 7f + (i % 3) * 4f + Mathf.absin(Time.time + i * 9f, 6f, 2f);
                float x = m.target.x() + Angles.trnsx(angle, radius);
                float y = m.target.y() + Angles.trnsy(angle, radius) + 3f * Mathf.sinDeg(Time.time * 2f + i * 45f);
                Fill.square(x, y, 1.2f + 0.2f * m.stacks, 45f + angle);
            }
        }
        Draw.reset();
    }


    private static void drawAblation() {
        for (AblationMark a : ablations) {
            if (a.unit == null || a.unit.dead || a.stacks <= 0) continue;

            Draw.color(Color.valueOf("ffb36e"));
            Draw.alpha(0.72f);
            Lines.stroke(1.4f + a.stacks * 0.35f);

            float base = a.unit.hitSize * 0.68f;
            for (int i = 0; i < a.stacks; i++) {
                float angle = i * 360f / Math.max(1, a.stacks) + Time.time * 0.28f;
                float r = base + i * 2.5f;
                float x = a.unit.x + Angles.trnsx(angle, r);
                float y = a.unit.y + Angles.trnsy(angle, r);
                Lines.lineAngle(x, y, angle + 115f, 7f + a.stacks * 1.6f);
                Lines.lineAngle(x, y, angle - 115f, 5f + a.stacks * 1.1f);
            }

            Draw.reset();
        }
    }

    private static final class StickyCharge {
        Team team; Posc target; float x, y, time, radius, damage;
    }
    private static final class Tether {
        Posc source; Unit target; float time, pull;
    }
    private static final class AnchorLink {
        int ownerKey; Team team; float x1, y1, x2, y2, time, damage, width, damageTimer; boolean complete;
    }
    private static final class DelayedBlast {
        Team team; float x, y, time, radius, damage; Color color;
    }
    private static final class StackMark {
        Healthc target; Team team; int stacks, max; float time;
    }
    private static final class AblationMark {
        Unit unit; int stacks; float time, armorPerStack;
    }

    private MdtCombatRuntime() {}
}
