package mdtnh.debug;

import arc.math.geom.Vec2;
import mdtnh.ai.AIRole;
import mdtnh.ai.BattleStrategy;
import mindustry.gen.Teamc;

/**
 * Reused, allocation-free runtime telemetry for one SmartAI controller.
 *
 * This is not synchronized over the network. It is intended for singleplayer or host-side
 * development debugging, because Mindustry AI controllers only update server-side.
 */
public final class AIDebugState {

    public AIRole role;
    public BattleStrategy strategy;
    public AIDebugBranch branch = AIDebugBranch.NONE;

    public Teamc target;
    public Teamc safeTarget;

    public boolean retreating;

    /** Main planned waypoint/goal. */
    public final Vec2 waypoint = new Vec2();
    public boolean waypointValid;

    /** Secondary route point, usually target after a flank waypoint. */
    public final Vec2 waypoint2 = new Vec2();
    public boolean waypoint2Valid;

    /** Left/right FLANK candidate points. */
    public final Vec2 leftCandidate = new Vec2();
    public final Vec2 rightCandidate = new Vec2();
    public boolean flankCandidatesValid;

    public float leftDanger;
    public float rightDanger;
    public int flankSide;

    public float preferredRange;
    public float bomberPassAngle;
    public float bomberPassTimer;

    public float lastUpdateTime;

    public void begin(AIRole role, BattleStrategy strategy, Teamc target, boolean retreating, float time) {
        this.role = role;
        this.strategy = strategy;
        this.target = target;
        this.retreating = retreating;
        this.lastUpdateTime = time;

        branch = AIDebugBranch.NONE;
        safeTarget = null;

        waypointValid = false;
        waypoint2Valid = false;

        // FLANK route candidates are recalculated at low frequency. Keep the last pair visible
        // between repaths so the overlay does not flash for a single tick.
        if (role != AIRole.FLANK) {
            flankCandidatesValid = false;
        }

        preferredRange = 0f;
        bomberPassTimer = 0f;
    }

    public void route(float x, float y) {
        waypoint.set(x, y);
        waypointValid = true;
    }

    public void route2(float x, float y) {
        waypoint2.set(x, y);
        waypoint2Valid = true;
    }

    public void clearRoute() {
        waypointValid = false;
        waypoint2Valid = false;
    }
}
