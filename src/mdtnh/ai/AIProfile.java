package mdtnh.ai;

/**
 * Data-driven combat personality. Values are intentionally public for concise content setup.
 */
public class AIProfile {

    public AIRole role = AIRole.ASSAULT;

    /** Preferred distance as a fraction of unit weapon range. */
    public float preferredRange = 0.78f;

    /** Health fraction below which the unit seeks repair/core safety. */
    public float retreatThreshold = 0.0f;

    /** Target search radius relative to weapon range, with a floor applied by TargetScorer. */
    public float scanRangeMultiplier = 1.8f;

    /** How aggressively flying units avoid cached turret danger. 0 disables it. */
    public float threatAvoidance = 0.0f;

    /** Tactical-link range used by SmartAI target sharing. */
    public float tacticalLinkRange = 180f;

    // Target-score weights.
    public float unitWeight = 20f;
    public float supportUnitWeight = 95f;
    public float commanderUnitWeight = 100f;
    public float lowHealthWeight = 40f;
    public float markedWeight = 85f;

    public float coreWeight = 90f;
    public float turretWeight = 100f;
    public float repairWeight = 95f;
    public float factoryWeight = 75f;
    public float generatorWeight = 70f;
    public float storageWeight = 55f;
    public float batteryWeight = 45f;
    public float reactorWeight = 80f;
    public float drillWeight = 35f;

    /** Score subtracted per world-unit of distance. */
    public float distanceWeight = 0.12f;

    public AIProfile role(AIRole value) {
        role = value;
        return this;
    }

    public AIProfile preferredRange(float value) {
        preferredRange = value;
        return this;
    }

    public AIProfile retreat(float value) {
        retreatThreshold = value;
        return this;
    }

    public AIProfile threatAvoidance(float value) {
        threatAvoidance = value;
        return this;
    }

    public AIProfile scan(float value) {
        scanRangeMultiplier = value;
        return this;
    }
}
