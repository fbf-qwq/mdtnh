package mdtnh.combat.impl;

import arc.Events;
import arc.struct.IntMap;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.game.EventType.UnitDamageEvent;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;

/**
 * Shared lightweight combat modifier registry for fire-control and designation support.
 */
public final class MdtCombatModifiers {
    private static final class Link {
        int targetId;
        float bonus;
        float expire;
    }

    private static final class Mark {
        float bonus;
        float expire;
    }

    private static final IntMap<Link> linkedAttackers = new IntMap<>();
    private static final IntMap<Mark> markedTargets = new IntMap<>();
    private static boolean installed;
    private static boolean applyingExtra;

    public static void install() {
        if (installed) return;
        installed = true;

        Events.on(UnitDamageEvent.class, event -> {
            if (applyingExtra || event == null || event.unit == null || event.bullet == null) return;

            Bullet bullet = event.bullet;
            if (!(bullet.owner instanceof Unit attacker)) return;

            float bonus = 0f;

            Link link = linkedAttackers.get(attacker.id);
            if (link != null) {
                if (link.expire >= Time.time && event.unit.id == link.targetId) {
                    bonus += link.bonus;
                } else if (link.expire < Time.time) {
                    linkedAttackers.remove(attacker.id);
                }
            }

            Mark mark = markedTargets.get(event.unit.id);
            if (mark != null) {
                if (mark.expire >= Time.time) {
                    bonus += mark.bonus;
                } else {
                    markedTargets.remove(event.unit.id);
                }
            }

            if (bonus <= 0f) return;

            applyingExtra = true;
            event.unit.damagePierce(Math.max(0f, bullet.damage) * bonus);
            applyingExtra = false;

            Fx.chainLightning.at(attacker.x, attacker.y, 0f, Pal.accent, event.unit);
        });
    }

    public static void link(Unit attacker, Teamc target, float bonus, float duration) {
        if (attacker == null || target == null) return;

        Link link = linkedAttackers.get(attacker.id);
        if (link == null) {
            link = new Link();
            linkedAttackers.put(attacker.id, link);
        }

        link.targetId = target.id();
        link.bonus = bonus;
        link.expire = Time.time + duration;
    }

    public static void mark(Teamc target, float bonus, float duration) {
        Mark mark = markedTargets.get(target.id());
        if (mark == null) {
            mark = new Mark();
            markedTargets.put(target.id(), mark);
        }

        mark.bonus = Math.max(mark.bonus, bonus);
        mark.expire = Math.max(mark.expire, Time.time + duration);
    }

    private MdtCombatModifiers() {
    }
}
