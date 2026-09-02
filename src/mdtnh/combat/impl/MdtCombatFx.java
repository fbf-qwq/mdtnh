package mdtnh.combat.impl;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Tmp;
import mindustry.entities.Effect;

/** Shared visual effects. Phase 3 intentionally keeps most support effects dim. */
public final class MdtCombatFx {
    public static final Effect pulse = new Effect(28f, 120f, e -> {
        Draw.color(e.color);
        Lines.stroke(2.2f * e.fout());
        Lines.circle(e.x, e.y, 7f + 34f * e.fin());
        Draw.reset();
    });
    public static final Effect supportPulse=pulse;
    public static final Effect cross = new Effect(30f, 120f, e -> {
        Draw.color(e.color);
        Lines.stroke(1.8f * e.fout());
        float r = 7f + 20f * e.fin();
        for (int i = 0; i < 4; i++) {
            float a = e.rotation + i * 90f;
            Lines.lineAngle(
                e.x + Angles.trnsx(a, r),
                e.y + Angles.trnsy(a, r),
                a,
                9f * e.fout()
            );
        }
        Draw.reset();
    });

    public static final Effect spark = new Effect(20f, 90f, e -> {
        Draw.color(e.color);
        Draw.alpha(0.75f * e.fout());
        for (int i = 0; i < 7; i++) {
            float a = e.rotation + i * (360f / 7f);
            float r = 3f + 18f * e.fin();
            Fill.circle(e.x + Angles.trnsx(a, r), e.y + Angles.trnsy(a, r), 1.4f * e.fout());
        }
        Draw.reset();
    });

    public static final Effect phase = new Effect(24f, 120f, e -> {
        Draw.color(e.color);
        Draw.alpha(e.fout() * 0.65f);
        Lines.stroke(1.7f);
        Lines.poly(e.x, e.y, 4, 7f + e.fin() * 18f, 45f + e.rotation);
        Draw.reset();
    });

    /** Reaction primer: subtle overclock-like squares rising from the target. */
    public static final Effect reactionParticles = new Effect(42f, 90f, e -> {
        Draw.color(e.color);
        Draw.alpha(0.22f + 0.42f * e.fout());
        for (int i = 0; i < 6; i++) {
            float seed = i * 57f + e.id * 0.17f;
            float angle = seed + e.fin() * 75f;
            float radius = 5f + 18f * e.finpow();
            float x = e.x + Angles.trnsx(angle, radius);
            float y = e.y + Angles.trnsy(angle, radius) + 10f * e.fin();
            Fill.square(x, y, 1.2f + 1.7f * e.fout(), 45f + angle);
        }
        Draw.reset();
    });

    /** S03-S05: deliberately dim, compact support pulse. */
    public static final Effect softPulse = new Effect(30f, 80f, e -> {
        Draw.color(e.color);
        Draw.alpha(0.16f * e.fout());
        Lines.stroke(1.05f);
        Lines.circle(e.x, e.y, 4f + 15f * e.fin());
        for (int i = 0; i < 4; i++) {
            float a = i * 90f + e.fin() * 20f;
            float r = 4f + 13f * e.fin();
            Fill.square(e.x + Angles.trnsx(a, r), e.y + Angles.trnsy(a, r), 1.1f * e.fout(), 45f);
        }
        Draw.reset();
    });

    /** Data for A19 shield-energy orbs. */
    public static final class ShieldTransferData {
        public Position to;
        public Color fromColor;
        public Color toColor;

        public ShieldTransferData(Position to, Color fromColor, Color toColor) {
            this.to = to;
            this.fromColor = fromColor;
            this.toColor = toColor;
        }
    }

    /**
     * A19: several small energy balls travel from target to owner.
     * Their color continuously blends from enemy-team color to friendly-team color.
     */
    public static final Effect shieldTransfer = new Effect(34f, 320f, e -> {
        if (!(e.data instanceof ShieldTransferData data) || data.to == null) return;

        for (int i = 0; i < 4; i++) {
            float offset = i * 0.14f;
            float f = Mathf.clamp((e.fin() - offset) / (1f - offset));
            float curved = Interp.pow2Out.apply(f);

            Tmp.v1.set(
                Mathf.lerp(e.x, data.to.getX(), curved),
                Mathf.lerp(e.y, data.to.getY(), curved)
            );
            float side = Mathf.sin((f * 180f) + i * 70f) * 5f * e.fslope();
            Tmp.v2.set(data.to.getX() - e.x, data.to.getY() - e.y).nor().rotate90(1).scl(side);
            Tmp.v1.add(Tmp.v2);

            Tmp.c1.set(data.fromColor).lerp(data.toColor, f);
            Draw.color(Tmp.c1);
            Draw.alpha(0.35f + 0.65f * e.fout());
            Fill.circle(Tmp.v1.x, Tmp.v1.y, 1.5f + 1.2f * e.fslope());
        }

        Draw.reset();
    });

    private MdtCombatFx() {}
}
