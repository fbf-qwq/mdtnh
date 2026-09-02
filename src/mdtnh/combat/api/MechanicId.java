package mdtnh.combat.api;

/**
 * Stable mechanic IDs used by unit loadouts, debug UI and future serialization.
 * Keep the code field stable even if the Java class name changes later.
 */
public enum MechanicId {
    // Attack mechanics: stable IDs; A08/A09/A25 are intentionally retired.
    BACKSPRAY_FRAGMENT("A01", MechanicGroup.ATTACK),
    STICKY_EXPLOSIVE("A02", MechanicGroup.ATTACK),
    INERTIA_SHOT("A03", MechanicGroup.ATTACK),
    MASS_IMPACT("A04", MechanicGroup.ATTACK),
    TRACTOR_LANCE("A05", MechanicGroup.ATTACK),
    DISPLACEMENT_SHOT("A06", MechanicGroup.ATTACK),
    REACTION_PRIMER("A07", MechanicGroup.ATTACK),
    CUTTING_ANCHOR("A10", MechanicGroup.ATTACK),
    PHASE_PROJECTILE("A11", MechanicGroup.ATTACK),
    COLLAPSING_RING("A12", MechanicGroup.ATTACK),
    FRACTURE_STACK("A13", MechanicGroup.ATTACK),
    DELAYED_FUSE("A14", MechanicGroup.ATTACK),
    SUBSURFACE_SHOCKWAVE("A15", MechanicGroup.ATTACK),
    TOP_ATTACK_SPLIT("A16", MechanicGroup.ATTACK),
    DECOY_SALVO("A17", MechanicGroup.ATTACK),
    CHARGE_CHAIN_LIGHTNING("A18", MechanicGroup.ATTACK),
    SHIELD_DRAIN("A19", MechanicGroup.ATTACK),
    ABLATION_BEAM("A20", MechanicGroup.ATTACK),
    ADHESIVE_CORROSION("A21", MechanicGroup.ATTACK),
    RICOCHET("A22", MechanicGroup.ATTACK),
    MOVING_PRISM("A23", MechanicGroup.ATTACK),
    GRAVITY_CORE("A24", MechanicGroup.ATTACK),
    VELOCITY_FUSE_FLAK("A26", MechanicGroup.ATTACK),

    // Base/unit mechanics: stable IDs; B12 is intentionally retired.
    DEPLOY("B01", MechanicGroup.BASE_ABILITY),
    HEAT("B02", MechanicGroup.BASE_ABILITY),
    FACING_ARMOR("B03", MechanicGroup.BASE_ABILITY),
    RECOIL_ANCHOR("B04", MechanicGroup.BASE_ABILITY),
    AMMO_CYCLE("B05", MechanicGroup.BASE_ABILITY),
    DAMAGE_GATE("B06", MechanicGroup.BASE_ABILITY),
    ABLATIVE_ARMOR("B07", MechanicGroup.BASE_ABILITY),
    ADAPTIVE_ARMOR("B08", MechanicGroup.BASE_ABILITY),
    CAPACITOR("B09", MechanicGroup.BASE_ABILITY),
    MOMENTUM("B10", MechanicGroup.BASE_ABILITY),
    PHASE_BLINK("B11", MechanicGroup.BASE_ABILITY),
    LAST_STAND("B13", MechanicGroup.BASE_ABILITY),
    COUNTER_BATTERY("B14", MechanicGroup.BASE_ABILITY),
    BURST_DRIVE("B15", MechanicGroup.BASE_ABILITY),

    // Support mechanics: S01-S20
    FIRE_CONTROL_LINK("S01", MechanicGroup.SUPPORT),
    COUNTER_BATTERY_MARK("S02", MechanicGroup.SUPPORT),
    HEAT_TRANSFER("S03", MechanicGroup.SUPPORT),
    CAPACITOR_TRANSFER("S04", MechanicGroup.SUPPORT),
    RELOAD_SERVICE("S05", MechanicGroup.SUPPORT),
    STATUS_CLEANSE("S06", MechanicGroup.SUPPORT),
    RESCUE_TRACTOR("S07", MechanicGroup.SUPPORT),
    VECTOR_ASSIST("S08", MechanicGroup.SUPPORT),
    ELECTRONIC_SUPPRESSION("S09", MechanicGroup.SUPPORT),
    DECOY_CHAFF("S10", MechanicGroup.SUPPORT),
    DEFLECTION_WEDGE("S11", MechanicGroup.SUPPORT),
    DAMAGE_REDIRECT("S12", MechanicGroup.SUPPORT),
    STABILIZATION_FIELD("S13", MechanicGroup.SUPPORT),
    PHASE_CORRIDOR("S14", MechanicGroup.SUPPORT),
    TARGET_DESIGNATION("S15", MechanicGroup.SUPPORT),
    DRONE_MAINTENANCE("S16", MechanicGroup.SUPPORT),
    BUILD_ASSIST("S17", MechanicGroup.SUPPORT),
    FORMATION_COORDINATION("S19", MechanicGroup.SUPPORT),
    THREAT_WARNING("S20", MechanicGroup.SUPPORT),

    // Core-assistant contracts: C01-C15
    FOLLOW_MINING("C01", MechanicGroup.CORE_ASSIST),
    ORE_SCAN_AND_HAUL("C02", MechanicGroup.CORE_ASSIST),
    BUILD_SHADOW("C03", MechanicGroup.CORE_ASSIST),
    MAINTENANCE_COMPANION("C04", MechanicGroup.CORE_ASSIST),
    CONSTRUCTION_LOGISTICS("C05", MechanicGroup.CORE_ASSIST),
    BALLISTIC_GUARD("C06", MechanicGroup.CORE_ASSIST),
    MULTITOOL_MIRROR("C07", MechanicGroup.CORE_ASSIST),
    DEPLOYED_SERVICE_STATION("C08", MechanicGroup.CORE_ASSIST),
    THREAT_ANALYSIS_OVERLAY("C09", MechanicGroup.CORE_ASSIST),
    VECTOR_CORRIDOR_ASSIST("C10", MechanicGroup.CORE_ASSIST),
    PHASE_SAFETY_ANCHOR("C11", MechanicGroup.CORE_ASSIST),
    PERSONAL_SPACE_CONTROL("C12", MechanicGroup.CORE_ASSIST),
    COMBAT_ANALYZER("C13", MechanicGroup.CORE_ASSIST),
    BUILD_DRONE_SWARM("C14", MechanicGroup.CORE_ASSIST),
    GENESIS_ASSIST_SUITE("C15", MechanicGroup.CORE_ASSIST);

    public final String code;
    public final MechanicGroup group;

    MechanicId(String code, MechanicGroup group) {
        this.code = code;
        this.group = group;
    }
}
