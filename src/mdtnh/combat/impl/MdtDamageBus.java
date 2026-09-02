package mdtnh.combat.impl;

import arc.Events;
import arc.struct.IntMap;
import arc.util.Time;
import mindustry.game.EventType.UnitDamageEvent;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public final class MdtDamageBus {
    public static final class HitInfo {
        public float time;
        public float incomingFromAngle;
        public int bulletTypeId;
        public Teamc attacker;
        public float rawDamage;
    }

    private static final IntMap<HitInfo> hits = new IntMap<>();
    private static boolean installed;

    public static void install() {
        if (installed) return;
        installed = true;

        Events.on(UnitDamageEvent.class, event -> {
            Unit unit = event.unit;
            Bullet bullet = event.bullet;
            if (unit == null || bullet == null) return;

            HitInfo info = hits.get(unit.id);
            if (info == null) {
                info = new HitInfo();
                hits.put(unit.id, info);
            }

            info.time = Time.time;
            info.incomingFromAngle = bullet.rotation() + 180f;
            info.bulletTypeId = bullet.type == null ? -1 : bullet.type.id;
            info.rawDamage = bullet.damage;
            info.attacker = bullet.owner instanceof Teamc t ? t : null;
        });
    }

    public static HitInfo recent(Unit unit, float maxAge) {
        HitInfo info = hits.get(unit.id);
        return info != null && Time.time - info.time <= maxAge ? info : null;
    }

    private MdtDamageBus() {}
}
