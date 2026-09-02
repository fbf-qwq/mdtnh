package mdtnh;

import mdtnh.combat.api.visual.MdtVisualUnitType;
import mdtnh.combat.impl.abilities.TimedDecoyAbility;
import mdtnh.ai.StationaryDecoyAI;
import mdtnh.units.*;
import mindustry.ai.UnitCommand;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;

public final class ModUnits {
    // 海军攻击 / Naval Attack
    public static UnitType navalAttackSkiff, navalAttackWake, navalAttackPike, navalAttackSalvo, navalAttackTorrent, navalAttackTrench, navalAttackPrism, navalAttackLeviathan, navalAttackHunter, navalAttackBastion, navalAttackDynamo, navalAttackMaelstrom, navalAttackMarshal, navalAttackNemesis, navalAttackSovereign;
    // 海军辅助 / Naval Support
    public static UnitType navalSupportSpring, navalSupportEscort, navalSupportConvoy, navalSupportVortex, navalSupportSolace, navalSupportReflux, navalSupportHaven, navalSupportAegis, navalSupportBulwark, navalSupportReversal, navalSupportSilence, navalSupportTidal, navalSupportChorus, navalSupportSustain, navalSupportArk;
    // 陆军攻击 / Ground Attack
    public static UnitType groundAttackBayonet, groundAttackHammer, groundAttackFlame, groundAttackMortar, groundAttackThunder, groundAttackCannon, groundAttackLancer, groundAttackDominion, groundAttackFortress, groundAttackFurnace, groundAttackVerdict, groundAttackTremor, groundAttackLegion, groundAttackJudgment, groundAttackTerminus;
    // 陆军辅助 / Ground Support
    public static UnitType groundSupportGlint, groundSupportMend, groundSupportPulse, groundSupportGuard, groundSupportMercy, groundSupportBulwark, groundSupportLumen, groundSupportSanctum, groundSupportRampart, groundSupportRegen, groundSupportRefract, groundSupportStasis, groundSupportCommand, groundSupportAegis, groundSupportProvidence;
    // 空军攻击 / Air Attack
    public static UnitType airAttackDart, airAttackFalcon, airAttackDive, airAttackBomber, airAttackRaptor, airAttackJavelin, airAttackRay, airAttackEclipse, airAttackPhantom, airAttackTalon, airAttackBlink, airAttackInterdictor, airAttackSquadron, airAttackNemesis, airAttackApocalypse;
    // 空军辅助 / Air Support
    public static UnitType airSupportWorker, airSupportFinch, airSupportTinker, airSupportMender, airSupportLifter, airSupportWarden, airSupportBeacon, airSupportHive, airSupportBeaconUV, airSupportShepherd, airSupportMirror, airSupportRelay, airSupportSwarm, airSupportNetwork, airSupportSeraph;
    // 爬行 / Crawler
    public static UnitType crawlerScarab, crawlerMite, crawlerEmber, crawlerVenom, crawlerPincer, crawlerLeech, crawlerFacet, crawlerPlague, crawlerRipper, crawlerPack, crawlerPhase, crawlerGraviton, crawlerBrood, crawlerDevourer, crawlerCalamity;

    // Internal helper units used by DroneBay/S10. They are not part of the 105 regular-unit count.
    public static UnitType attackDrone, repairDrone, interceptorDrone, engineeringDrone, broodlingDrone, siegeDrone, decoyDrone;

    public static void load() {
        loadInternalUnits();
        NavalAttackUnits.load();
        NavalSupportUnits.load();
        GroundAttackUnits.load();
        GroundSupportUnits.load();
        AirAttackUnits.load();
        AirSupportUnits.load();
        CrawlerUnits.load();
    }

    private static void loadInternalUnits() {
        attackDrone = internalDrone("mdt-attack-drone", 620f, 2.7f, 12f);
        ModUnitFactory.standard(attackDrone, "mdt-attack-drone", 5, ModUnitFactory.Standard.RAPID, 1);

        repairDrone = internalDrone("mdt-repair-drone", 700f, 2.5f, 13f);
        ModUnitFactory.repairBeam(repairDrone, "mdt-repair-drone-beam", 6, 1);

        interceptorDrone = internalDrone("mdt-interceptor-drone", 760f, 2.8f, 13f);
        ModUnitFactory.pointDefense(interceptorDrone, "mdt-interceptor-drone-pd", 7, 1);

        engineeringDrone = internalDrone("mdt-engineering-drone", 820f, 2.4f, 14f);
        engineeringDrone.buildSpeed = 2.2f;
        engineeringDrone.defaultCommand = UnitCommand.rebuildCommand;

        broodlingDrone = internalDrone("mdt-broodling", 900f, 2.2f, 15f);
        ModUnitFactory.standard(broodlingDrone, "mdt-broodling", 7, ModUnitFactory.Standard.MELEE, 1);

        siegeDrone = internalDrone("mdt-siege-drone", 1200f, 1.7f, 16f);
        ModUnitFactory.standard(siegeDrone, "mdt-siege-drone", 9, ModUnitFactory.Standard.HEAVY, 1);

        decoyDrone = internalDrone("mdt-decoy-drone", 220f, 0f, 10f);
        decoyDrone.targetAir = decoyDrone.targetGround = false;
        decoyDrone.canAttack = false;
        decoyDrone.accel = 0f;
        decoyDrone.drag = 1f;
        decoyDrone.engineSize = 0f;
        decoyDrone.wobble = false;
        decoyDrone.aiController = StationaryDecoyAI::new;
        decoyDrone.abilities.add(new TimedDecoyAbility(120f));
    }

    private static UnitType internalDrone(String id, float health, float speed, float hitSize) {
        MdtVisualUnitType unit = new MdtVisualUnitType(id);
        unit.constructor = UnitEntity::create;
        unit.flying = true;
        unit.health = health;
        unit.speed = speed;
        unit.hitSize = hitSize;
        unit.drag = 0.04f;
        unit.accel = 0.12f;
        unit.rotateSpeed = 7f;
        unit.engineOffset = hitSize * 0.38f;
        unit.engineSize = hitSize * 0.12f;
        unit.useUnitCap = false;
        unit.playerControllable = false;
        unit.logicControllable = false;
        unit.hidden = true;
        unit.internal = false;
        unit.drawCell = false;
        unit.createWreck = false;
        return unit;
    }

    private ModUnits() {}
}
