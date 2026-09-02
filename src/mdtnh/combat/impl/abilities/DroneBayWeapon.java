package mdtnh.combat.impl.abilities;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mdtnh.ai.CarrierOrbitAI;
import mindustry.Vars;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * Independent drone-bay weapon.
 *
 * - The bay is a real Weapon with its own sprite.
 * - Count 0..N selects <weapon-name>-0 .. <weapon-name>-N.
 * - Spawned drones are normal Unit entities, but CarrierOrbitAI keeps them in a local orbit.
 * - They may fire/repair/build within that local battlespace, but never leave the carrier to chase.
 * - The mount remembers only its own children for cap/slot bookkeeping.
 */
public class DroneBayWeapon extends Weapon {
    public UnitType droneType;
    public int maxDrones = 3;
    public float spawnTime = 150f;
    public float spawnDistance = 18f;
    /** <= 0 means derive the orbit radius from carrier hitSize + spawnDistance. */
    public float orbitRadius = -1f;

    private TextureRegion[] countRegions = {};

    public DroneBayWeapon(String name, UnitType droneType) {
        super(name);
        this.droneType = droneType;

        mirror = false;
        rotate = false;
        controllable = false;
        aiControllable = false;
        noAttack = true;
        useAttackRange = false;
        showStatSprite = false;

        mountType = DroneBayMount::new;
    }

    public static class DroneBayMount extends WeaponMount {
        public Seq<Unit> owned = new Seq<>();
        public float spawnTimer;

        public DroneBayMount(Weapon weapon) {
            super(weapon);
        }
    }

    @Override
    public float dps() {
        return 0f;
    }

    @Override
    public float shotsPerSec() {
        return 0f;
    }

    @Override
    public boolean hasStats(UnitType type) {
        return display;
    }

    @Override
    public void addStats(UnitType type, Table table) {
        table.row();
        table.add(Core.bundle.format(
            "stat.mdt-dronebay",
            maxDrones,
            String.format(java.util.Locale.ROOT, "%.1f", spawnTime / 60f)
        ));
    }

    @Override
    public void load() {
        super.load();

        countRegions = new TextureRegion[Math.max(1, maxDrones + 1)];
        for (int i = 0; i < countRegions.length; i++) {
            countRegions[i] = Core.atlas.find(name + "-" + i);
        }
    }

    @Override
    public void update(Unit unit, WeaponMount base) {
        DroneBayMount mount = (DroneBayMount)base;

        for (int i = mount.owned.size - 1; i >= 0; i--) {
            Unit drone = mount.owned.get(i);
            if (drone == null || !drone.isValid() || drone.dead || drone.team != unit.team) {
                mount.owned.remove(i);
            }
        }

        if (droneType == null || mount.owned.size >= maxDrones) {
            mount.spawnTimer = Math.min(mount.spawnTimer, spawnTime);
            return;
        }

        mount.spawnTimer += Time.delta;
        if (mount.spawnTimer < spawnTime) return;
        mount.spawnTimer = 0f;

        if (!Vars.net.client()) {
            float bodyRotation = unit.rotation - 90f;
            float wx = unit.x + Angles.trnsx(bodyRotation, x, y);
            float wy = unit.y + Angles.trnsy(bodyRotation, x, y);

            Unit drone = droneType.create(unit.team);
            float angle = unit.rotation + 180f + Mathf.range(24f);

            drone.set(
                wx + Angles.trnsx(angle, spawnDistance),
                wy + Angles.trnsy(angle, spawnDistance)
            );
            drone.rotation = unit.rotation;

            // Reuse the first free slot. This avoids a replacement drone occupying
            // the same orbit slot as an older survivor after another drone is destroyed.
            boolean[] used = new boolean[Math.max(1, maxDrones)];
            for (Unit owned : mount.owned) {
                if (owned != null && owned.controller() instanceof CarrierOrbitAI orbit) {
                    int slot = orbit.slot();
                    if (slot >= 0 && slot < used.length) used[slot] = true;
                }
            }

            int slot = 0;
            while (slot < used.length && used[slot]) slot++;
            if (slot >= used.length) slot = mount.owned.size % used.length;

            int ring = Math.abs(name.hashCode() % 3);
            float baseRadius = Math.max(
                unit.hitSize * 0.78f + drone.hitSize * 0.65f,
                spawnDistance * 2.2f
            );
            float radius = orbitRadius > 0f
                ? orbitRadius
                : baseRadius + ring * (drone.hitSize * 0.75f + 6f);

            // Different bays on the same carrier get different phase offsets and,
            // by default, one of three concentric rings. This keeps mixed drone
            // types readable around large carriers.
            float phase = Math.abs(name.hashCode() % 360);

            drone.controller(new CarrierOrbitAI(
                unit,
                slot,
                maxDrones,
                radius,
                phase
            ));

            drone.add();
            mount.owned.add(drone);
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount base) {
        DroneBayMount mount = (DroneBayMount)base;

        int count = Math.max(0, Math.min(maxDrones, mount.owned.size));
        TextureRegion selected =
            count < countRegions.length && countRegions[count] != null && countRegions[count].found()
                ? countRegions[count]
                : region;

        if (selected == null || !selected.found()) return;

        float rotation = unit.rotation - 90f;
        float weaponRotation = rotation + baseRotation;
        float wx = unit.x + Angles.trnsx(rotation, x, y);
        float wy = unit.y + Angles.trnsy(rotation, x, y);

        unit.type.applyColor(unit);
        Draw.rect(selected, wx, wy, weaponRotation);
        Draw.reset();
    }

    public int activeDrones(WeaponMount mount) {
        return mount instanceof DroneBayMount bay ? bay.owned.size : 0;
    }
}
