package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;

public final class NavalSupportUnits {
        public static void load() {
        loadSpring();
        loadEscort();
        loadConvoy();
        loadVortex();
        loadSolace();
        loadReflux();
        loadHaven();
        loadAegis();
        loadBulwark();
        loadReversal();
        loadSilence();
        loadTidal();
        loadChorus();
        loadSustain();
        loadArk();
    }

    private static void loadSpring() {
        final String id = "naval-support-spring";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.standard(u, id + "-dual", t, ModUnitFactory.Standard.HEAL, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportSpring = u;
    }
    private static void loadEscort() {
        final String id = "naval-support-escort";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S07");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportEscort = u;
    }
    private static void loadConvoy() {
        final String id = "naval-support-convoy";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 1);
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S13");
        ModUnitFactory.addVanilla(u, t, "OVERCLOCK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportConvoy = u;
    }
    private static void loadVortex() {
        final String id = "naval-support-vortex";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S11", "S13");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportVortex = u;
    }
    private static void loadSolace() {
        final String id = "naval-support-solace";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.standard(u, id + "-vanilla-sap", t, ModUnitFactory.Standard.SAP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S04");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_REGEN");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportSolace = u;
    }
    private static void loadReflux() {
        final String id = "naval-support-reflux";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S03", "S05", "S06");
        ModUnitFactory.addVanilla(u, t, "OVERCLOCK", "ENERGY_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportReflux = u;
    }
    private static void loadHaven() {
        final String id = "naval-support-haven";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A23, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser", t, ModUnitFactory.Standard.LASER, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S06", "S10", "S11");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportHaven = u;
    }
    private static void loadAegis() {
        final String id = "naval-support-aegis";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 2);
        ModUnitFactory.standard(u, id + "-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 1);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S09", "S10", "S12");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_REGEN", "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportAegis = u;
    }
    private static void loadBulwark() {
        final String id = "naval-support-bulwark";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.tractor", "mdt.weaponmode.displace"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A05, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A06, t));
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S07", "S11", "S13");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportBulwark = u;
    }
    private static void loadReversal() {
        final String id = "naval-support-reversal";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.special(u, id + "-siphon", t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A05, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S04", "S05", "S06", "S07");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN", "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "EMERGENCY_REPAIR");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportReversal = u;
    }
    private static void loadSilence() {
        final String id = "naval-support-silence";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.standard(u, id + "-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 1);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B08");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S06", "S09", "S10", "S11");
        ModUnitFactory.addVanilla(u, t, "SUPPRESSION");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportSilence = u;
    }
    private static void loadTidal() {
        final String id = "naval-support-tidal";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.tractor", "mdt.weaponmode.displace"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A05, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A06, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S08", "S13", "S14");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.addAdvanced(u, t, "BARRIER");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportTidal = u;
    }
    private static void loadChorus() {
        final String id = "naval-support-chorus";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 2);
        ModUnitFactory.standard(u, id + "-vanilla-laser-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S01", "S04", "S05", "S15", "S19");
        ModUnitFactory.addVanilla(u, t, "FORCE");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportChorus = u;
    }
    private static void loadSustain() {
        final String id = "naval-support-sustain";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A20, 2);
        ModUnitFactory.standard(u, id + "-vanilla-laser", t, ModUnitFactory.Standard.LASER, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S03", "S04", "S05", "S06", "S12", "S13");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN", "REPAIR_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportSustain = u;
    }
    private static void loadArk() {
        final String id = "naval-support-ark";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A10, 4);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 2);
        ModUnitFactory.droneBay(u, id + "-dronebay-intercept", ModUnits.interceptorDrone, t, 5, u.hitSize * 0.24f, -u.hitSize * 0.20f);
        ModUnitFactory.droneBay(u, id + "-dronebay-repair", ModUnits.repairDrone, t, 5, -u.hitSize * 0.24f, -u.hitSize * 0.20f);
        ModUnitFactory.droneBay(u, id + "-dronebay-decoy", ModUnits.decoyDrone, t, 4, 0f, -u.hitSize * 0.30f);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.standard(u, id + "-defense-emp", t, ModUnitFactory.Standard.EMP, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B07", "B08", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S06", "S09", "S11", "S12", "S16", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_REGEN", "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalSupportArk = u;
    }

    private NavalSupportUnits() {}
}
