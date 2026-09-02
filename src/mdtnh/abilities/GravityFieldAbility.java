package mdtnh.abilities;

import arc.math.geom.Vec2;
import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/** Local slow field with a light pull toward the carrier. */
public class GravityFieldAbility extends MdtAbility {

    public float range = 110f;
    public float pull = 0.12f;
    public float statusDuration = 12f;

    private final Vec2 pullVec = new Vec2();

    public GravityFieldAbility() {
        super("gravityfield");
    }

    @Override
    public void update(Unit unit) {
        Units.nearbyEnemies(unit.team, unit.x, unit.y, range, other -> {
            if (other.dead()) return;

            pullVec.set(unit.x - other.x, unit.y - other.y);
            if (pullVec.len2() > 0.001f) {
                pullVec.setLength(pull * Time.delta);
                other.vel().add(pullVec);
            }
            other.apply(ModStatusEffects.gravitySlow, statusDuration);
        });
    }
}
