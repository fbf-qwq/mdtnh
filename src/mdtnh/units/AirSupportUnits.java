package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;

public final class AirSupportUnits {
        public static void load() {
        loadWorker();
        loadFinch();
        loadTinker();
        loadMender();
        loadLifter();
        loadWarden();
        loadBeacon();
        loadHive();
        loadBeaconUV();
        loadShepherd();
        loadMirror();
        loadRelay();
        loadSwarm();
        loadNetwork();
        loadSeraph();
    }

    private static void loadWorker() {
        final String id = "air-support-worker";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.standard(u, id + "-defense", t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportWorker = u;
    }
    private static void loadFinch() {
        final String id = "air-support-finch";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.standard(u, id + "-defense", t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportFinch = u;
    }
    private static void loadTinker() {
        final String id = "air-support-tinker";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 1);
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S17");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportTinker = u;
    }
    private static void loadMender() {
        final String id = "air-support-mender";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.standard(u, id + "-flak", t, ModUnitFactory.Standard.FLAK, 1);
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S07");
        ModUnitFactory.addVanilla(u, t, "REPAIR_FIELD");
        ModUnitFactory.standard(u, id + "-vanilla-heal", t, ModUnitFactory.Standard.HEAL, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportMender = u;
    }
    private static void loadLifter() {
        final String id = "air-support-lifter";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 1);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S08");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportLifter = u;
    }
    private static void loadWarden() {
        final String id = "air-support-warden";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point-intercept", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S10", "S11");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportWarden = u;
    }
    private static void loadBeacon() {
        final String id = "air-support-beacon";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A23, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S01", "S06", "S15");
        ModUnitFactory.addVanilla(u, t, "OVERCLOCK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportBeacon = u;
    }
    private static void loadHive() {
        final String id = "air-support-hive";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.standard(u, id + "-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.droneBay(u, id + "-dronebay", ModUnits.repairDrone, t, 3, 0f, -u.hitSize * 0.22f);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S09", "S10", "S16", "S20");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportHive = u;
    }
    private static void loadBeaconUV() {
        final String id = "air-support-beacon-uv";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser_bolt", t, ModUnitFactory.Standard.LASER_BOLT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S02", "S10", "S15", "S20");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportBeaconUV = u;
    }
    private static void loadShepherd() {
        final String id = "air-support-shepherd";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 2);
        ModUnitFactory.standard(u, id + "-vanilla-flak", t, ModUnitFactory.Standard.FLAK, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S08", "S10", "S19");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportShepherd = u;
    }
    private static void loadMirror() {
        final String id = "air-support-mirror";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S07", "S10", "S11");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportMirror = u;
    }
    private static void loadRelay() {
        final String id = "air-support-relay";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09", "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S04", "S08", "S10", "S14");
        ModUnitFactory.addAdvanced(u, t, "BARRIER");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportRelay = u;
    }
    private static void loadSwarm() {
        final String id = "air-support-swarm";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.droneBay(u, id + "-dronebay-engineering-a", ModUnits.engineeringDrone, t, 4, u.hitSize * 0.22f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-engineering-b", ModUnits.engineeringDrone, t, 4, -u.hitSize * 0.22f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-repair", ModUnits.repairDrone, t, 3, 0f, -u.hitSize * 0.28f);
        ModUnitFactory.standard(u, id + "-defense-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S06", "S16", "S17", "S19");
        ModUnitFactory.addVanilla(u, t, "REPAIR_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportSwarm = u;
    }
    private static void loadNetwork() {
        final String id = "air-support-network";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.standard(u, id + "-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 2);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S01", "S02", "S09", "S15", "S19", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT", "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportNetwork = u;
    }
    private static void loadSeraph() {
        final String id = "air-support-seraph";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 2);
        ModUnitFactory.droneBay(u, id + "-dronebay-repair", ModUnits.repairDrone, t, 4, u.hitSize * 0.25f, -u.hitSize * 0.16f);
        ModUnitFactory.droneBay(u, id + "-dronebay-intercept", ModUnits.interceptorDrone, t, 4, -u.hitSize * 0.25f, -u.hitSize * 0.16f);
        ModUnitFactory.droneBay(u, id + "-dronebay-engineering", ModUnits.engineeringDrone, t, 4, u.hitSize * 0.14f, -u.hitSize * 0.29f);
        ModUnitFactory.droneBay(u, id + "-dronebay-decoy", ModUnits.decoyDrone, t, 4, -u.hitSize * 0.14f, -u.hitSize * 0.29f);
        ModUnitFactory.standard(u, id + "-vanilla-continuous", t, ModUnitFactory.Standard.CONTINUOUS, 1);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B06", "B07", "B09", "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S06", "S14", "S15", "S16", "S19", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_REGEN", "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airSupportSeraph = u;
    }

    private AirSupportUnits() {}
}
