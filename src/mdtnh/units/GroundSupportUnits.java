package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;

public final class GroundSupportUnits {
        public static void load() {
        loadGlint();
        loadMend();
        loadPulse();
        loadGuard();
        loadMercy();
        loadBulwark();
        loadLumen();
        loadSanctum();
        loadRampart();
        loadRegen();
        loadRefract();
        loadStasis();
        loadCommand();
        loadAegis();
        loadProvidence();
    }

    private static void loadGlint() {
        final String id = "ground-support-glint";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.standard(u, id + "-dual", t, ModUnitFactory.Standard.HEAL, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportGlint = u;
    }
    private static void loadMend() {
        final String id = "ground-support-mend";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S07");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportMend = u;
    }
    private static void loadPulse() {
        final String id = "ground-support-pulse";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S13");
        ModUnitFactory.addVanilla(u, t, "OVERCLOCK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportPulse = u;
    }
    private static void loadGuard() {
        final String id = "ground-support-guard";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S11");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportGuard = u;
    }
    private static void loadMercy() {
        final String id = "ground-support-mercy";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.standard(u, id + "-vanilla-sap", t, ModUnitFactory.Standard.SAP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S04", "S07");
        ModUnitFactory.addVanilla(u, t, "REPAIR_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportMercy = u;
    }
    private static void loadBulwark() {
        final String id = "ground-support-bulwark";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.special(u, id + "-impact", t, ModUnitFactory.Special.A06, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S12", "S13");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportBulwark = u;
    }
    private static void loadLumen() {
        final String id = "ground-support-lumen";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A23, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser", t, ModUnitFactory.Standard.LASER, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S03", "S05", "S06");
        ModUnitFactory.addVanilla(u, t, "REPAIR_FIELD", "OVERCLOCK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportLumen = u;
    }
    private static void loadSanctum() {
        final String id = "ground-support-sanctum";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.standard(u, id + "-flak", t, ModUnitFactory.Standard.FLAK, 1);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S06", "S09", "S12", "S13");
        ModUnitFactory.addVanilla(u, t, "FORCE", "REPAIR_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportSanctum = u;
    }
    private static void loadRampart() {
        final String id = "ground-support-rampart";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 2);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B03", "B06");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S11", "S13");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportRampart = u;
    }
    private static void loadRegen() {
        final String id = "ground-support-regen";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.repairBeam(u, id + "-repair", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A05, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B06");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S06", "S07", "S13");
        ModUnitFactory.addVanilla(u, t, "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "EMERGENCY_REPAIR");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportRegen = u;
    }
    private static void loadRefract() {
        final String id = "ground-support-refract";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.standard(u, id + "-flak", t, ModUnitFactory.Standard.FLAK, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 2);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B08");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S10", "S11");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportRefract = u;
    }
    private static void loadStasis() {
        final String id = "ground-support-stasis";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.standard(u, id + "-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S09", "S13", "S14");
        ModUnitFactory.addVanilla(u, t, "SUPPRESSION");
        ModUnitFactory.addAdvanced(u, t, "BARRIER");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportStasis = u;
    }
    private static void loadCommand() {
        final String id = "ground-support-command";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 2);
        ModUnitFactory.standard(u, id + "-vanilla-laser-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S01", "S02", "S05", "S15", "S19", "S20");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportCommand = u;
    }
    private static void loadAegis() {
        final String id = "ground-support-aegis";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.tractor", "mdt.weaponmode.displace", "mdt.weaponmode.emp"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A05, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A06, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.EMP, t));
        ModUnitFactory.repairBeam(u, id + "-repair", t, 2);
        ModUnitFactory.standard(u, id + "-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S03", "S04", "S06", "S12", "S13", "S14");
        ModUnitFactory.addVanilla(u, t, "FORCE", "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "BARRIER");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportAegis = u;
    }
    private static void loadProvidence() {
        final String id = "ground-support-providence";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_SUPPORT, t);
        ModUnitFactory.reactionPair(u, id + "-reaction", t, 4, 2);
        ModUnitFactory.droneBay(u, id + "-dronebay-repair", ModUnits.repairDrone, t, 5, u.hitSize * 0.25f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-rescue", ModUnits.repairDrone, t, 4, -u.hitSize * 0.25f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-intercept", ModUnits.interceptorDrone, t, 4, 0f, -u.hitSize * 0.30f);
        ModUnitFactory.healWeapon(u, id + "-dual-heal", t, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B07", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.repairDrone, "S05", "S06", "S07", "S12", "S15", "S16", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE", "REPAIR_FIELD");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT", "EMERGENCY_REPAIR");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_SUPPORT, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundSupportProvidence = u;
    }

    private GroundSupportUnits() {}
}
