package mdtnh.abilities;

import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.core.World;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Emergency short-range blink.
 *
 * Fixes:
 * - requires a real nearby threat (enemy unit or turret);
 * - cannot repeatedly blink forever while remaining at low health;
 * - rearms only after health recovers above the critical threshold + margin.
 *
 * Ability.data state:
 *   data < 0  : disarmed; waiting for health recovery
 *   0..reload : normal cooldown
 */
public class PhaseBlinkAbility extends MdtAbility {

    public float reload = 60f * 12f;
    public float healthThreshold = 0.28f;
    public float rearmHealthMargin = 0.12f;
    public float distance = 72f;
    public float threatRange = 180f;

    private final Vec2 direction = new Vec2();

    public PhaseBlinkAbility() {
        super("phaseblink");
    }

    @Override
    public void created(Unit unit) {
        // Ready immediately on spawn, useful both in gameplay and sandbox tests.
        data = reload;
    }

    @Override
    public void update(Unit unit) {
        // After a blink, a unit must recover before this emergency tool can arm again.
        if (data < 0f) {
            if (unit.healthf() >= Math.min(1f, healthThreshold + rearmHealthMargin)) {
                data = 0f;
            }
            return;
        }

        data = Math.min(reload, data + Time.delta);
        if (data < reload || unit.healthf() > healthThreshold) return;

        Teamc threat = Units.closestTarget(
                unit.team,
                unit.x,
                unit.y,
                threatRange,
                other -> other.targetable(unit.team),
                build -> build.block instanceof Turret
        );

        // No nearby combat threat = no blink.
        if (threat == null) return;

        direction.set(unit.x - threat.getX(), unit.y - threat.getY());
        if (direction.len2() < 0.001f) {
            direction.trns(unit.rotation + 180f, 1f);
        } else {
            direction.nor();
        }

        float oldX = unit.x, oldY = unit.y;

        for (int attempt = 0; attempt < 6; attempt++) {
            float dst = distance * (1f - attempt * 0.13f);
            float nx = unit.x + direction.x * dst;
            float ny = unit.y + direction.y * dst;
            int tx = World.toTile(nx), ty = World.toTile(ny);

            if (Vars.world.tile(tx, ty) != null && unit.canPass(tx, ty)) {
                Fx.spawn.at(oldX, oldY);
                unit.set(nx, ny);
                unit.vel().setZero();
                Fx.spawn.at(nx, ny);

                // Negative means "used during this low-health episode".
                data = -1f;
                return;
            }
        }
    }
}
