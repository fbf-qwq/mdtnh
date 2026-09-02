package mdtnh.ai;

/** Reusable baseline profiles. Individual UV+ units can copy/tune these values inline. */
public final class AIProfiles {

    public static AIProfile assault() {
        return new AIProfile()
                .role(AIRole.ASSAULT)
                .preferredRange(0.65f)
                .retreat(0.18f)
                .scan(1.7f);
    }

    public static AIProfile skirmish() {
        AIProfile p = new AIProfile()
                .role(AIRole.SKIRMISH)
                .preferredRange(0.88f)
                .retreat(0.28f)
                .threatAvoidance(0.65f)
                .scan(2.0f);
        p.turretWeight = 75f;
        return p;
    }

    public static AIProfile artillery() {
        AIProfile p = new AIProfile()
                .role(AIRole.ARTILLERY)
                .preferredRange(0.94f)
                .retreat(0.32f)
                .threatAvoidance(0.85f)
                .scan(2.4f);
        p.turretWeight = 125f;
        p.repairWeight = 105f;
        return p;
    }

    public static AIProfile hunter() {
        AIProfile p = new AIProfile()
                .role(AIRole.HUNTER)
                .preferredRange(0.82f)
                .retreat(0.25f)
                .threatAvoidance(0.75f)
                .scan(2.4f);
        p.supportUnitWeight = 145f;
        p.commanderUnitWeight = 155f;
        p.lowHealthWeight = 70f;
        p.repairWeight = 125f;
        return p;
    }

    public static AIProfile flank() {
        AIProfile p = new AIProfile()
                .role(AIRole.FLANK)
                .preferredRange(0.75f)
                .retreat(0.24f)
                .threatAvoidance(1.0f)
                .scan(2.2f);
        p.generatorWeight = 120f;
        p.factoryWeight = 110f;
        p.storageWeight = 85f;
        p.turretWeight = 40f;
        return p;
    }

    public static AIProfile bomber() {
        AIProfile p = flank();
        p.role = AIRole.BOMBER;
        p.generatorWeight = 135f;
        p.factoryWeight = 125f;
        p.repairWeight = 115f;
        return p;
    }

    public static AIProfile escort() {
        AIProfile p = new AIProfile()
                .role(AIRole.ESCORT)
                .preferredRange(0.70f)
                .retreat(0.38f)
                .threatAvoidance(0.85f)
                .scan(1.6f);
        p.unitWeight = 10f;
        p.turretWeight = 65f;
        return p;
    }

    public static AIProfile swarm() {
        AIProfile p = new AIProfile()
                .role(AIRole.SWARM)
                .preferredRange(0.15f)
                .retreat(0f)
                .scan(1.6f);
        p.turretWeight = 120f;
        p.repairWeight = 130f;
        p.distanceWeight = 0.20f;
        return p;
    }

    public static AIProfile siege() {
        AIProfile p = artillery();
        p.role = AIRole.SIEGE;
        p.preferredRange = 0.97f;
        p.retreatThreshold = 0.36f;
        return p;
    }

    private AIProfiles() {
    }
}
