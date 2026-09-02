package mdtnh.debug;

/**
 * The exact high-level movement branch SmartAI used during its latest server-side update.
 * This is deliberately more specific than AIRole: AIRole is intent, branch is what actually ran.
 */
public enum AIDebugBranch {
    NONE,
    NO_TARGET,
    PATH_CORE,

    COMMAND_MOVE,
    COMMAND_ATTACK,
    COMMAND_SPECIAL,

    ASSAULT,
    RANGED,

    SKIRMISH_APPROACH,
    SKIRMISH_STRAFE,
    SKIRMISH_BACKOFF,

    RETREAT_REPAIR,
    RETREAT_CORE,
    RETREAT_AWAY,
    RETREAT_NO_SAFE_TARGET,

    REGROUP_COMMANDER,
    REGROUP_CORE,

    FLANK_REPATH,
    FLANK_WAYPOINT,
    FLANK_STRAFE,
    FLANK_GROUND_FALLBACK,

    BOMBER_APPROACH,
    BOMBER_PASS,

    SWARM_APPROACH,
    SWARM_CONTACT,

    COMMANDER_CENTROID,
    COMMANDER_RANGE,
    COMMANDER_IDLE_GROUP,

    MACRO_FLANK
}
