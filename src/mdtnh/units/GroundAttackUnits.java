package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;

public final class GroundAttackUnits {
        public static void load() {
        loadBayonet();
        loadHammer();
        loadFlame();
        loadMortar();
        loadThunder();
        loadCannon();
        loadLancer();
        loadDominion();
        loadFortress();
        loadFurnace();
        loadVerdict();
        loadTremor();
        loadLegion();
        loadJudgment();
        loadTerminus();
    }

    private static void loadBayonet() {
        final String id = "ground-attack-bayonet";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.CANNON, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B04");
        ModUnitFactory.standard(u, id + "-bayonet", t, ModUnitFactory.Standard.MELEE, 1);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackBayonet = u;
    }
    private static void loadHammer() {
        final String id = "ground-attack-hammer";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B02", "B04");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackHammer = u;
    }
    private static void loadFlame() {
        final String id = "ground-attack-flame";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.FLAME, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A21, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B13");
        ModUnitFactory.standard(u, id + "-flame", t, ModUnitFactory.Standard.FLAME, 1);
        ModUnitFactory.standard(u, id + "-vanilla-liquid", t, ModUnitFactory.Standard.LIQUID, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackFlame = u;
    }
    private static void loadMortar() {
        final String id = "ground-attack-mortar";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.delayed", "mdt.weaponmode.ricochet"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A14, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A22, t));
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B03", "B04", "B05");
        ModUnitFactory.standard(u, id + "-vanilla-artillery", t, ModUnitFactory.Standard.ARTILLERY, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackMortar = u;
    }
    private static void loadThunder() {
        final String id = "ground-attack-thunder";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.standard(u, id + "-secondary", t, ModUnitFactory.Standard.RAPID, 1);
        ModUnitFactory.standard(u, id + "-vanilla-lightning", t, ModUnitFactory.Standard.LIGHTNING, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B06", "B09");
        ModUnitFactory.addVanilla(u, t, "ENERGY_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackThunder = u;
    }
    private static void loadCannon() {
        final String id = "ground-attack-cannon";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.ap", "mdt.weaponmode.he", "mdt.weaponmode.aa"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A01, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.CANNON, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A26, t));
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B04", "B07");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackCannon = u;
    }
    private static void loadLancer() {
        final String id = "ground-attack-lancer";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.RAIL, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A20, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser_bolt", t, ModUnitFactory.Standard.LASER_BOLT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B04", "B06");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackLancer = u;
    }
    private static void loadDominion() {
        final String id = "ground-attack-dominion";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.mass", "mdt.weaponmode.fracture", "mdt.weaponmode.he"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A04, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A13, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.HEAVY, t));
        ModUnitFactory.standard(u, id + "-side-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B07", "B08");
        ModUnitFactory.addVanilla(u, t, "SHIELD_ARC");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackDominion = u;
    }
    private static void loadFortress() {
        final String id = "ground-attack-fortress";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A13, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A15, 2);
        ModUnitFactory.standard(u, id + "-vanilla-artillery", t, ModUnitFactory.Standard.ARTILLERY, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B03", "B06", "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S02", "S10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackFortress = u;
    }
    private static void loadFurnace() {
        final String id = "ground-attack-furnace";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.flame", "mdt.weaponmode.corrosion", "mdt.weaponmode.heavy"}, ModUnitFactory.standardBullet(ModUnitFactory.Standard.FLAME, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A21, t), ModUnitFactory.standardBullet(ModUnitFactory.Standard.HEAVY, t));
        ModUnitFactory.standard(u, id + "-vanilla-liquid", t, ModUnitFactory.Standard.LIQUID, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B02", "B03", "B13");
        ModUnitFactory.addAdvanced(u, t, "EMERGENCY_REPAIR");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackFurnace = u;
    }
    private static void loadVerdict() {
        final String id = "ground-attack-verdict";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A13, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A14, 1);
        ModUnitFactory.standard(u, id + "-vanilla-laser_bolt", t, ModUnitFactory.Standard.LASER_BOLT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B06", "B08");
        ModUnitFactory.addAdvanced(u, t, "EMERGENCY_REPAIR", "LOCK", "EXECUTION");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackVerdict = u;
    }
    private static void loadTremor() {
        final String id = "ground-attack-tremor";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A06, 4);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B09");
        ModUnitFactory.addVanilla(u, t, "FORCE");
        ModUnitFactory.addAdvanced(u, t, "BARRIER", "GRAVITY");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackTremor = u;
    }
    private static void loadLegion() {
        final String id = "ground-attack-legion";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.ap", "mdt.weaponmode.aa"}, ModUnitFactory.standardBullet(ModUnitFactory.Standard.HEAVY, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A26, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A10, 4);
        ModUnitFactory.standard(u, id + "-line-bolt", t, ModUnitFactory.Standard.LASER_BOLT, 2);
        ModUnitFactory.standard(u, id + "-line-flak", t, ModUnitFactory.Standard.FLAK, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B07");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S15", "S19");
        ModUnitFactory.addVanilla(u, t, "SHIELD_REGEN");
        ModUnitFactory.addAdvanced(u, t, "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackLegion = u;
    }
    private static void loadJudgment() {
        final String id = "ground-attack-judgment";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.modeWeapon(u, id, t, new String[]{"mdt.weaponmode.ablation", "mdt.weaponmode.fracture", "mdt.weaponmode.topattack"}, ModUnitFactory.specialBullet(ModUnitFactory.Special.A20, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A13, t), ModUnitFactory.specialBullet(ModUnitFactory.Special.A16, t));
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A17, 2);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B08", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S10", "S15");
        ModUnitFactory.addAdvanced(u, t, "LOCK", "EXECUTION");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackJudgment = u;
    }
    private static void loadTerminus() {
        final String id = "ground-attack-terminus";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.GROUND_ATTACK, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A04, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A13, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A15, 1);
        ModUnitFactory.droneBay(u, id + "-dronebay-a", ModUnits.siegeDrone, t, 3, u.hitSize * 0.25f, -u.hitSize * 0.18f);
        ModUnitFactory.droneBay(u, id + "-dronebay-b", ModUnits.siegeDrone, t, 3, -u.hitSize * 0.25f, -u.hitSize * 0.18f);
        ModUnitFactory.standard(u, id + "-vanilla-artillery", t, ModUnitFactory.Standard.ARTILLERY, 2);
        ModUnitFactory.standard(u, id + "-vanilla-rail", t, ModUnitFactory.Standard.RAIL, 1);
        ModUnitFactory.pointDefense(u, id + "-pd", t, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B01", "B03", "B06", "B07", "B08", "B09");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.attackDrone, "S01", "S02", "S16", "S19", "S20");
        ModUnitFactory.addVanilla(u, t, "FORCE", "SHIELD_REGEN");
        ModUnitFactory.addAdvanced(u, t, "LOCK", "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.GROUND_ATTACK, t, "");
        ModUnitFactory.finish(u);
        ModUnits.groundAttackTerminus = u;
    }

    private GroundAttackUnits() {}
}
