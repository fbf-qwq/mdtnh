package mdtnh.ai;

import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Team-level cached turret threat map.
 *
 * Threat is intentionally cached and coarse. SmartAI should query this map, not scan every
 * enemy turret itself.
 */
public final class ThreatManager {

    private static final ObjectMap<Team, Snapshot> snapshots = new ObjectMap<>();

    /** Rebuild period for the cached turret list. */
    public static float refreshInterval = 30f;

    /**
     * Extra buffer outside nominal turret range.
     * Without this, fast aircraft start avoiding only after they are already inside the kill zone.
     */
    public static float safetyMargin = 70f;

    public static float dangerAt(Team viewer, float x, float y, boolean air) {
        Snapshot snapshot = snapshot(viewer);
        float danger = 0f;

        for (ThreatNode node : snapshot.nodes) {
            if (air ? !node.targetsAir : !node.targetsGround) continue;

            float effectiveRange = node.range + safetyMargin;
            float dst = Mathf.dst(x, y, node.x, node.y);

            if (dst <= effectiveRange) {
                float proximity = 1f - dst / Math.max(effectiveRange, 1f);
                danger += node.danger * (0.20f + proximity * 0.80f);
            }
        }

        return danger;
    }

    /**
     * Samples a route segment instead of only one look-ahead point.
     * This is the important part for FLANK and threat-aware flying movement.
     */
    public static float pathDanger(
            Team viewer,
            float x1,
            float y1,
            float x2,
            float y2,
            boolean air,
            int samples
    ) {
        int count = Math.max(2, samples);
        float danger = 0f;

        for (int i = 1; i <= count; i++) {
            float t = (float)i / count;
            float x = x1 + (x2 - x1) * t;
            float y = y1 + (y2 - y1) * t;
            danger += dangerAt(viewer, x, y, air);
        }

        return danger / count;
    }

    /**
     * Debug-only traversal of the currently cached threat nodes.
     * Calling this does not rescan the map; it reads the same cache SmartAI uses.
     */
    public static void eachThreat(Team viewer, Cons<ThreatNode> consumer) {
        Snapshot snapshot = snapshot(viewer);
        for (ThreatNode node : snapshot.nodes) {
            consumer.get(node);
        }
    }

    public static void clear() {
        snapshots.clear();
    }

    private static Snapshot snapshot(Team viewer) {
        Snapshot snapshot = snapshots.get(viewer, Snapshot::new);

        if (Time.time - snapshot.lastRefresh >= refreshInterval || snapshot.lastRefresh < 0f) {
            rebuild(viewer, snapshot);
        }

        return snapshot;
    }

    private static void rebuild(Team viewer, Snapshot snapshot) {
        snapshot.nodes.clear();

        for (var data : Vars.state.teams.present) {
            if (data.team == viewer) continue;

            for (Building build : data.buildings) {
                if (!(build.block instanceof Turret turret) || !build.isValid()) continue;

                ThreatNode node = new ThreatNode();
                node.x = build.x;
                node.y = build.y;
                node.range = Math.max(turret.range, 1f);
                node.targetsAir = turret.targetAir;
                node.targetsGround = turret.targetGround;

                node.danger =
                        1f
                                + turret.size * 1.6f
                                + turret.range / 80f
                                + Math.min(build.maxHealth() / 2500f, 4f);

                snapshot.nodes.add(node);
            }
        }

        snapshot.lastRefresh = Time.time;
    }

    private static final class Snapshot {
        final Seq<ThreatNode> nodes = new Seq<>();
        float lastRefresh = -1f;
    }

    public static final class ThreatNode {
        public float x, y, range, danger;
        public boolean targetsAir, targetsGround;
    }

    private ThreatManager() {
    }
}
