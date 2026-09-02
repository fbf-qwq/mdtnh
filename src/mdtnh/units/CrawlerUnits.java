package mdtnh.units;

import mdtnh.ModUnits;
import mindustry.content.StatusEffects;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public final class CrawlerUnits {
        public static void load() {
        loadScarab();
        loadMite();
        loadEmber();
        loadVenom();
        loadPincer();
        loadLeech();
        loadFacet();
        loadPlague();
        loadRipper();
        loadPack();
        loadPhase();
        loadGraviton();
        loadBrood();
        loadDevourer();
        loadCalamity();
    }

    private static void loadScarab() {
        final String id = "crawler-scarab";
        final int t = 0;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        Weapon suicide = new Weapon();
        suicide.shootOnDeath = true;
        suicide.mirror = false;
        suicide.reload = 1f;
        suicide.bullet = new mindustry.entities.bullet.ShrapnelBulletType(){ { damage = ModUnitFactory.damage(t, 90f); length = 70f; width = 34f; serrationLenScl = 4f; } };
        u.weapons.add(suicide);
        ModUnitFactory.addBaseAbilities(u, t, "B10");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerScarab = u;
    }
    private static void loadMite() {
        final String id = "crawler-mite";
        final int t = 1;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A21, 1);
        Weapon suicide = new Weapon();
        suicide.shootOnDeath = true;
        suicide.mirror = false;
        suicide.reload = 1f;
        suicide.bullet = ModUnitFactory.specialBullet(ModUnitFactory.Special.A21, t);
        suicide.bullet.splashDamage = ModUnitFactory.damage(t, 65f);
        suicide.bullet.splashDamageRadius = 42f;
        u.weapons.add(suicide);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerMite = u;
    }
    private static void loadEmber() {
        final String id = "crawler-ember";
        final int t = 2;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.FLAME, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B13");
        ModUnitFactory.standard(u, id + "-bite", t, ModUnitFactory.Standard.FLAME, 1);
        ModUnitFactory.standard(u, id + "-vanilla-liquid", t, ModUnitFactory.Standard.LIQUID, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerEmber = u;
    }
    private static void loadVenom() {
        final String id = "crawler-venom";
        final int t = 3;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.standard(u, id, t, ModUnitFactory.Standard.CANNON, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A21, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03");
        u.immunities.add(StatusEffects.corroded);
        ModUnitFactory.standard(u, id + "-vanilla-liquid", t, ModUnitFactory.Standard.LIQUID, 1);
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerVenom = u;
    }
    private static void loadPincer() {
        final String id = "crawler-pincer";
        final int t = 4;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.reactionPair(u, id + "-reaction", t, 1, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B13");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerPincer = u;
    }
    private static void loadLeech() {
        final String id = "crawler-leech";
        final int t = 5;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.standard(u, id + "-sap", t, ModUnitFactory.Standard.SAP, 1);
        ModUnitFactory.standard(u, id + "-vanilla-sap", t, ModUnitFactory.Standard.SAP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B06");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerLeech = u;
    }
    private static void loadFacet() {
        final String id = "crawler-facet";
        final int t = 6;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A10, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B07");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerFacet = u;
    }
    private static void loadPlague() {
        final String id = "crawler-plague";
        final int t = 7;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A13, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A21, 1);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B07", "B08", "B13");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerPlague = u;
    }
    private static void loadRipper() {
        final String id = "crawler-ripper";
        final int t = 8;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A03, 1);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A05, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B10", "B13", "B15");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerRipper = u;
    }
    private static void loadPack() {
        final String id = "crawler-pack";
        final int t = 9;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A18, 1);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B10", "B13");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.broodlingDrone, "S01", "S19");
        ModUnitFactory.addVanilla(u, t, "ENERGY_FIELD");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerPack = u;
    }
    private static void loadPhase() {
        final String id = "crawler-phase";
        final int t = 10;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A11, 1);
        ModUnitFactory.standard(u, id + "-vanilla-point", t, ModUnitFactory.Standard.POINT, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B06", "B11");
        ModUnitFactory.addAdvanced(u, t, "EMERGENCY_REPAIR");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerPhase = u;
    }
    private static void loadGraviton() {
        final String id = "crawler-graviton";
        final int t = 11;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A05, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B13");
        ModUnitFactory.addAdvanced(u, t, "GRAVITY");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerGraviton = u;
    }
    private static void loadBrood() {
        final String id = "crawler-brood";
        final int t = 12;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A10, 2);
        ModUnitFactory.droneBay(u, id + "-dronebay-a", ModUnits.broodlingDrone, t, 4, u.hitSize * 0.22f, -u.hitSize * 0.14f);
        ModUnitFactory.droneBay(u, id + "-dronebay-b", ModUnits.broodlingDrone, t, 4, -u.hitSize * 0.22f, -u.hitSize * 0.14f);
        ModUnitFactory.standard(u, id + "-brood-spike", t, ModUnitFactory.Standard.SHRAPNEL, 2);
        ModUnitFactory.addBaseAbilities(u, t, "B07", "B13");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.broodlingDrone, "S16", "S19");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerBrood = u;
    }
    private static void loadDevourer() {
        final String id = "crawler-devourer";
        final int t = 13;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.reactionPair(u, id + "-reaction", t, 4, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A19, 1);
        ModUnitFactory.standard(u, id + "-vanilla-sap", t, ModUnitFactory.Standard.SAP, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B08", "B13");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.broodlingDrone, "S01", "S19");
        ModUnitFactory.addAdvanced(u, t, "EXECUTION", "MARK");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerDevourer = u;
    }
    private static void loadCalamity() {
        final String id = "crawler-calamity";
        final int t = 14;
        UnitType u = ModUnitFactory.base(id, ModUnitFactory.Line.CRAWLER, t);
        ModUnitFactory.reactionPair(u, id + "-reaction", t, 3, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A05, 2);
        ModUnitFactory.special(u, id, t, ModUnitFactory.Special.A24, 1);
        ModUnitFactory.droneBay(u, id + "-dronebay-a", ModUnits.broodlingDrone, t, 5, u.hitSize * 0.24f, -u.hitSize * 0.12f);
        ModUnitFactory.droneBay(u, id + "-dronebay-b", ModUnits.broodlingDrone, t, 5, -u.hitSize * 0.24f, -u.hitSize * 0.12f);
        ModUnitFactory.droneBay(u, id + "-dronebay-c", ModUnits.broodlingDrone, t, 5, 0f, -u.hitSize * 0.28f);
        ModUnitFactory.standard(u, id + "-calamity-spike", t, ModUnitFactory.Standard.SHRAPNEL, 4);
        ModUnitFactory.standard(u, id + "-vanilla-shrapnel", t, ModUnitFactory.Standard.SHRAPNEL, 1);
        ModUnitFactory.addBaseAbilities(u, t, "B03", "B06", "B07", "B08", "B13");
        ModUnitFactory.addSupportAbilities(u, t, ModUnits.decoyDrone, ModUnits.broodlingDrone, "S01", "S16", "S19");
        ModUnitFactory.addVanilla(u, t, "FORCE");
        ModUnitFactory.addAdvanced(u, t, "GRAVITY", "EMERGENCY_REPAIR");
        ModUnitFactory.configureUtility(u, id, ModUnitFactory.Line.CRAWLER, t, "");
        ModUnitFactory.finish(u);
        ModUnits.crawlerCalamity = u;
    }

    private CrawlerUnits() {}
}
