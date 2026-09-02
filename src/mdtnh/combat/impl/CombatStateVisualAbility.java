package mdtnh.combat.impl;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mdtnh.combat.api.visual.*;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Lightweight non-text visual overlay for gameplay state.
 * It deliberately reads provider interfaces instead of concrete ability classes.
 */
public class CombatStateVisualAbility extends Ability {
    public Color lockColor = Color.valueOf("ffd37f");
    public Color linkColor = Color.valueOf("8fa9ff");

    public CombatStateVisualAbility() {
        display = false;
    }

    @Override
    public void draw(Unit unit) {
        drawLock(unit);
        drawLinks(unit);
        Draw.reset();
    }

    private void drawLock(Unit unit) {
        for (Ability a : unit.abilities) {
            if (a instanceof LockStateProvider p) {
                Teamc target = p.lockTarget(unit);
                float progress = Mathf.clamp(p.lockProgress(unit));
                if (target == null || progress <= 0f) continue;

                float r = (target instanceof Unit u ? u.hitSize : 16f) * (0.9f + 0.35f * (1f - progress));
                float pulse = 1f + Mathf.absin(Time.time, 5f, 0.08f);
                Draw.color(lockColor);
                Lines.stroke(0.8f + progress * 1.2f);
                for (int i = 0; i < 4; i++) {
                    float ang = i * 90f;
                    float x1 = target.x() + Angles.trnsx(ang, r * pulse);
                    float y1 = target.y() + Angles.trnsy(ang, r * pulse);
                    Lines.lineAngle(x1, y1, ang + 45f, 4f + 6f * progress);
                }
            }
        }
    }





    private void drawLinks(Unit unit) {
        for (Ability a : unit.abilities) {
            if (a instanceof LinkStateProvider p) {
                Draw.color(linkColor);
                Draw.alpha(0.42f);
                Lines.stroke(0.7f);
                p.eachLink(unit, other -> {
                    if (other != null) Lines.line(unit.x, unit.y, other.x(), other.y());
                });
            }
        }
    }


}
