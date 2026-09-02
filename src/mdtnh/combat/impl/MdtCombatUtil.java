package mdtnh.combat.impl;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mdtnh.combat.api.resource.ChargeResource;
import mdtnh.combat.api.resource.HeatResource;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.Team;
import mindustry.gen.Unit;

public final class MdtCombatUtil {
    public static HeatResource heatResource(Unit unit) {
        for (Ability ability : unit.abilities) {
            if (ability instanceof HeatResource resource) return resource;
        }
        return null;
    }

    public static ChargeResource chargeResource(Unit unit) {
        for (Ability ability : unit.abilities) {
            if (ability instanceof ChargeResource resource) return resource;
        }
        return null;
    }

    public static void impulse(Unit unit, float angle, float amount) {
        unit.vel.add(Tmp.v1.trns(angle, amount));
    }

    public static void pull(Unit unit, float x, float y, float amount) {
        Tmp.v1.set(x - unit.x, y - unit.y);
        if (!Tmp.v1.isZero()) unit.vel.add(Tmp.v1.setLength(amount));
    }

    public static void push(Unit unit, float x, float y, float amount) {
        Tmp.v1.set(unit.x - x, unit.y - y);
        if (!Tmp.v1.isZero()) unit.vel.add(Tmp.v1.setLength(amount));
    }

    public static void damageLine(Team team, float x1, float y1, float x2, float y2,
                                  float radius, float totalDamage) {
        float length = Mathf.dst(x1, y1, x2, y2);
        int steps = Math.max(1, (int)(length / Math.max(8f, radius * 1.5f)));
        float per = totalDamage / steps;
        for (int i = 0; i <= steps; i++) {
            float t = i / (float)steps;
            float x = Mathf.lerp(x1, x2, t);
            float y = Mathf.lerp(y1, y2, t);
            Damage.damage(team, x, y, radius, per, false, true, true);
        }
    }

    public static Unit nearestDamagedAlly(Unit source, float range, float healthFraction) {
        return Units.closest(source.team, source.x, source.y, range,
            u -> u != source && u.healthf() < healthFraction && !u.dead);
    }

    private MdtCombatUtil() {}
}
