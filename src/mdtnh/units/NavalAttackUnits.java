package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;

public final class NavalAttackUnits {
        public static void load() {
        loadSkiff();
        loadWake();
        loadPike();
        loadSalvo();
        loadTorrent();
        loadTrench();
        loadPrism();
        loadLeviathan();
        loadHunter();
        loadBastion();
        loadDynamo();
        loadMaelstrom();
        loadMarshal();
        loadNemesis();
        loadSovereign();
    }

    private static void loadSkiff() {
        final String id = "naval-attack-skiff";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B02", "B10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackSkiff = u;
    }
    private static void loadWake() {
        final String id = "naval-attack-wake";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.HEAVY, 1);
        ModUnitFactory.standard(u, id + "-missile", t, ModUnitFactory.Standard.MISSILE, 4);
        ModUnitFactory.addBaseAbilities(u, t, "B04");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackWake = u;
    }
    private static void loadPike() {
        final String id = "naval-attack-pike";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.CANNON, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A01, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B10");
        ModUnitFactory.standard(u, id + "-shotgun", t, ModUnitFactory.Standard.RAPID, 2);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackPike = u;
    }
    private static void loadSalvo() {
        final String id = "naval-attack-salvo";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.rapid", "mdt.weaponmode.heavy", "mdt.weaponmode.split"}, ModUnitFactory.standardBullet(ModUnitFactory.Standard.RAPID, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.HEAVY, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A16, t));
        ModUnitFactory.addBaseAbilities(u, t, "B02", "B03", "B04", "B05");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackSalvo = u;
    }
    private static void loadTorrent() {
        final String id = "naval-attack-torrent";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.FLAME, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A21, 1);
        ModUnitFactory.standard(u, id + "-flame", t, ModUnitFactory.Standard.FLAME, 1);
        ModUnitFactory.standard(u, id + "-vanilla-liquid", t, ModUnitFactory.Standard.LIQUID, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B02", "B03");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackTorrent = u;
    }
    private static void loadTrench() {
        final String id = "naval-attack-trench";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAIL, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A15, 2);
        ModUnitFactory.standard(u, id + "-vanilla-artillery", t, ModUnitFactory.Standard.ARTILLERY, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B04", "B06");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S02");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackTrench = u;
    }
    private static void loadPrism() {
        final String id = "naval-attack-prism";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAIL, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser_bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.standard(u, id + "-vanilla-lightning", t, ModUnitFactory.Standard.LIGHTNING, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackPrism = u;
    }
    private static void loadLeviathan() {
        final String id = "naval-attack-leviathan";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.ap", "mdt.weaponmode.he"}, ModUnitFactory.standardBullet(ModUnitFactory.Standard.HEAVY, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.CANNON, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A10, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackLeviathan = u;
    }
    private static void loadHunter() {
        final String id = "naval-attack-hunter";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A01, 8);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A02, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10", "S15");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackHunter = u;
    }
    private static void loadBastion() {
        final String id = "naval-attack-bastion";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A13, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A16, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 4);
        ModUnitFactory.standard(u, id + "-battery-heavy", t, ModUnitFactory.Standard.HEAVY, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B03", "B06", "B07", "B08");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_ARC");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackBastion = u;
    }
    private static void loadDynamo() {
        final String id = "naval-attack-dynamo";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 4);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S09", "S10");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN", "ENERGY_FIELD");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackDynamo = u;
    }
    private static void loadMaelstrom() {
        final String id = "naval-attack-maelstrom";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 4);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S11");
        ModUnitFactory.addVanilla(u, t, "FORCE");
        ModUnitFactory.addAdvanced(u, t, "BARRIER", "GRAVITY");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackMaelstrom = u;
    }
    private static void loadMarshal() {
        final String id = "naval-attack-marshal";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.standard(u, id + "-beam", t, ModUnitFactory.Standard.CONTINUOUS, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A12, 4);
        ModUnitFactory.standard(u, id + "-main-beam", t, ModUnitFactory.Standard.CONTINUOUS, 1);
        ModUnitFactory.standard(u, id + "-vanilla-artillery", t, ModUnitFactory.Standard.ARTILLERY, 2);
        ModUnitFactory.standard(u, id + "-firecontrol-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S15", "S19");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackMarshal = u;
    }
    private static void loadNemesis() {
        final String id = "naval-attack-nemesis";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A01, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A02, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A13, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 4);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A23, 1);
        ModUnitFactory.droneBay(u, id + "-dronebay-a", ModUnits.attackDrone, t, 4, u.hitSize * 0.24f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-b", ModUnits.attackDrone, t, 4, -u.hitSize * 0.24f, -u.hitSize * 0.18f);
        ModUnitFactory.standard(u, id + "-hunter-point", t, ModUnitFactory.Standard.POINT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S10", "S16");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.addAdvanced(u, t, "LOCK", "EXECUTION", "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackNemesis = u;
    }
    private static void loadSovereign() {
        final String id = "naval-attack-sovereign";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.NAVAL_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A10, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.droneBay(u, id + "-dronebay-attack", ModUnits.attackDrone, t, 5, u.hitSize * 0.25f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-intercept", ModUnits.interceptorDrone, t, 4, -u.hitSize * 0.25f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-repair", ModUnits.repairDrone, t, 4, 0f, -u.hitSize * 0.28f);
        ModUnitFactory.standard(u, id + "-main-rail", t, ModUnitFactory.Standard.RAIL, 1);
        ModUnitFactory.standard(u, id + "-broadside-heavy", t, ModUnitFactory.Standard.HEAVY, 4);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B07", "B08", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S16", "S19", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_REGEN");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT", "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.NAVAL_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.navalAttackSovereign = u;
    }

    private NavalAttackUnits() {}
}
