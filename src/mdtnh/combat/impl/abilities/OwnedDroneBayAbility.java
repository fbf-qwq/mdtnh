package mdtnh.combat.impl.abilities;

import arc.math.Angles;
import arc.struct.Seq;
import arc.util.Time;
import mdtnh.combat.api.visual.DroneBayStateProvider;
import mdtnh.combat.impl.MdtCombatFx;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import arc.graphics.Color;

/**
 * Simple owner-local DroneBay used by the playtest units and suitable as the base for MAX units.
 *
 * It counts only Unit references spawned by THIS copied Ability instance.
 * Manually spawned units and another carrier's drones do not consume its cap.
 *
 * Save/load ownership serialization is intentionally not implemented in this Phase 1 playtest.
 */
public class OwnedDroneBayAbility extends Ability implements DroneBayStateProvider {
    public UnitType droneType;
    public float spawnTime = 150f;
    public int maxDrones = 3;
    public float spawnDistance = 20f;

    private float timer;
    private Seq<Unit> owned = new Seq<>();

    public OwnedDroneBayAbility(UnitType droneType) {
        this.droneType = droneType;
    }

    @Override
    public void update(Unit unit) {
        for (int i = owned.size - 1; i >= 0; i--) {
            Unit drone = owned.get(i);
            if (drone == null || !drone.isValid() || drone.dead || drone.team != unit.team) {
                owned.remove(i);
            }
        }

        if (droneType == null || owned.size >= maxDrones) {
            timer = Math.min(timer, spawnTime);
            return;
        }

        timer += Time.delta;
        if (timer < spawnTime) return;
        timer = 0f;

        if (!Vars.net.client()) {
            Unit drone = droneType.create(unit.team);
            float angle = unit.rotation + 180f;
            drone.set(
                unit.x + Angles.trnsx(angle, spawnDistance),
                unit.y + Angles.trnsy(angle, spawnDistance)
            );
            drone.rotation = unit.rotation;
            drone.add();
            owned.add(drone);
            MdtCombatFx.phase.at(drone.x, drone.y, angle, Color.valueOf("8fe9ff"));
        }
    }

    @Override
    public int activeDrones(Unit unit) {
        return owned.size;
    }

    @Override
    public int maxDrones(Unit unit) {
        return maxDrones;
    }

    @Override
    public Ability copy() {
        OwnedDroneBayAbility out = (OwnedDroneBayAbility)super.copy();
        out.owned = new Seq<>();
        out.timer = 0f;
        return out;
    }
}
