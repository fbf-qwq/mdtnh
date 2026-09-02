package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;

public final class AirAttackUnits {
        public static void load() {
        loadDart();
        loadFalcon();
        loadDive();
        loadBomber();
        loadRaptor();
        loadJavelin();
        loadRay();
        loadEclipse();
        loadPhantom();
        loadTalon();
        loadBlink();
        loadInterdictor();
        loadSquadron();
        loadNemesis();
        loadApocalypse();
    }

    private static void loadDart() {
        final String id = "air-attack-dart";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.CANNON, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A03, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackDart = u;
    }
    private static void loadFalcon() {
        final String id = "air-attack-falcon";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.CANNON, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.standard(u, id + "-nose", t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackFalcon = u;
    }
    private static void loadDive() {
        final String id = "air-attack-dive";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.CANNON, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A02, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.standard(u, id + "-nose", t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.standard(u, id + "-vanilla-bomb", t, ModUnitFactory.Standard.BOMB, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackDive = u;
    }
    private static void loadBomber() {
        final String id = "air-attack-bomber";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.delayed", "mdt.weaponmode.cluster"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A14, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.BOMB, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B05", "B10");
        ModUnitFactory.standard(u, id + "-vanilla-artillery", t, ModUnitFactory.Standard.ARTILLERY, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackBomber = u;
    }
    private static void loadRaptor() {
        final String id = "air-attack-raptor";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A16, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackRaptor = u;
    }
    private static void loadJavelin() {
        final String id = "air-attack-javelin";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.ap", "mdt.weaponmode.aa"}, ModUnitFactory.standardBullet(ModUnitFactory.Standard.HEAVY, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A26, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B10");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackJavelin = u;
    }
    private static void loadRay() {
        final String id = "air-attack-ray";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A23, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser_bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09", "B10");
        ModUnitFactory.addVanilla(u, t, "ENERGY_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackRay = u;
    }
    private static void loadEclipse() {
        final String id = "air-attack-eclipse";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.standard(u, id + "-flak", t, ModUnitFactory.Standard.FLAK, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A12, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 1);
        ModUnitFactory.standard(u, id + "-vanilla-bomb", t, ModUnitFactory.Standard.BOMB, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackEclipse = u;
    }
    private static void loadPhantom() {
        final String id = "air-attack-phantom";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A03, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A11, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10", "B11", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackPhantom = u;
    }
    private static void loadTalon() {
        final String id = "air-attack-talon";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser_bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B02", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10", "S15");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackTalon = u;
    }
    private static void loadBlink() {
        final String id = "air-attack-blink";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A11, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A16, 1);
        ModUnitFactory.standard(u, id + "-phase-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09", "B10", "B11", "B15");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackBlink = u;
    }
    private static void loadInterdictor() {
        final String id = "air-attack-interdictor";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A26, 2);
        ModUnitFactory.standard(u, id + "-vanilla-emp", t, ModUnitFactory.Standard.EMP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.addAdvanced(u, t, "GRAVITY");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackInterdictor = u;
    }
    private static void loadSquadron() {
        final String id = "air-attack-squadron";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A16, 4);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 2);
        ModUnitFactory.standard(u, id + "-escort-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.standard(u, id + "-escort-flak", t, ModUnitFactory.Standard.FLAK, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B09", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S10", "S19");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackSquadron = u;
    }
    private static void loadNemesis() {
        final String id = "air-attack-nemesis";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.ablation", "mdt.weaponmode.fracture"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A20, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A13, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A16, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 2);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B09", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10", "S15");
        ModUnitFactory.addAdvanced(u, t, "LOCK", "EXECUTION");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackNemesis = u;
    }
    private static void loadApocalypse() {
        final String id = "air-attack-apocalypse";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.AIR_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A16, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.droneBay(u, id + "-dronebay-a", ModUnits.attackDrone, t, 4, u.hitSize * 0.23f, -u.hitSize * 0.16f);
        ModUnitFactory.droneBay(u, id + "-dronebay-b", ModUnits.attackDrone, t, 4, -u.hitSize * 0.23f, -u.hitSize * 0.16f);
        ModUnitFactory.droneBay(u, id + "-dronebay-intercept", ModUnits.interceptorDrone, t, 3, 0f, -u.hitSize * 0.28f);
        ModUnitFactory.standard(u, id + "-vanilla-bomb", t, ModUnitFactory.Standard.BOMB, 2);
        ModUnitFactory.standard(u, id + "-heavy", t, ModUnitFactory.Standard.HEAVY, 2);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B06", "B07", "B08", "B09", "B10", "B15");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S16", "S19", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE");
        ModUnitFactory.addAdvanced(u, t, "INTERCEPT", "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.AIR_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.airAttackApocalypse = u;
    }

    private AirAttackUnits() {}
}
