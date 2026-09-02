package mdtnh.test;

import mdtnh.abilities.BarrierPulseAbility;
import mdtnh.abilities.BurstDriveAbility;
import mdtnh.abilities.CapacitorAbility;
import mdtnh.abilities.DeployAbility;
import mdtnh.abilities.DroneBayAbility;
import mdtnh.abilities.EmergencyRepairAbility;
import mdtnh.abilities.ExecutionAbility;
import mdtnh.abilities.FormationAbility;
import mdtnh.abilities.GravityFieldAbility;
import mdtnh.abilities.HeatAbility;
import mdtnh.abilities.InterceptMatrixAbility;
import mdtnh.abilities.LockOnAbility;
import mdtnh.abilities.PhaseBlinkAbility;
import mdtnh.abilities.ShieldLinkAbility;
import mdtnh.abilities.TacticalLinkAbility;
import mdtnh.abilities.TargetMarkAbility;
import mdtnh.ai.AIProfile;
import mdtnh.ai.AIProfiles;
import mdtnh.ai.AIRole;
import mdtnh.ai.SmartAI;
import mdtnh.ai.SmartCommandAI;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * Development-only test content for the MDTNH AI/Ability system.
 *
 * These units intentionally have short cooldowns and exaggerated values so that
 * each mechanic can be verified in seconds instead of minutes.
 *
 * Remove ModTestUnits.load() from release builds.
 */
public final class ModTestUnits {

    public static UnitType
            testSiege,
            testRaider,
            testHunter,
            testCapacitor,
            testNetwork,
            testInterceptor,
            testCarrier,
            testFlanker,
            testBomber,
            testDrone;

    public static void load() {
        testDrone = base("ai-test-drone", true, AIProfiles.swarm(), 450f, 3.2f, 18f);
        testDrone.useUnitCap = false;
        testDrone.weapons.add(weapon(95f, 14f, 14f));
testSiege = base("ai-test-siege", false, AIProfiles.siege(), 3600f, 0.72f, 30f);
        testSiege.weapons.add(weapon(300f, 90f, 42f));
        testSiege.abilities.add(
                new DeployAbility(90f),
                heat(180f, 120f)
        );
testRaider = base("ai-test-raider", true, AIProfiles.skirmish(), 1900f, 2.65f, 24f);
        testRaider.weapons.add(weapon(185f, 32f, 18f));
        testRaider.abilities.add(
                new BurstDriveAbility(120f, 4.5f),
                blink(60f, 0.45f, 82f),
                emergencyRepair(0.18f, 0.25f, 300f)
        );
testHunter = base("ai-test-hunter", true, AIProfiles.hunter(), 2300f, 2.25f, 25f);
        testHunter.weapons.add(weapon(220f, 48f, 22f));
        testHunter.abilities.add(
                targetMark(90f, 300f, 240f),
                lockOn(60f, 180f),
                execution(0.25f)
        );
testCapacitor = base("ai-test-capacitor", false, AIProfiles.assault(), 4200f, 1.05f, 32f);
        testCapacitor.weapons.add(weapon(165f, 45f, 18f));
        testCapacitor.abilities.add(
                capacitor(180f, 4f, 500f, 1000f),
                gravity(105f, 0.20f)
        );
testNetwork = base("ai-test-network", true, AIProfiles.escort(), 2500f, 1.85f, 26f);
        testNetwork.weapons.add(weapon(185f, 26f, 20f));
        testNetwork.abilities.add(
                shieldLink(125f, 1.5f, 1000f, 4, 10f),
                new FormationAbility(),
                tacticalLink(170f, 10f)
        );
AIProfile sentinel = AIProfiles.escort().role(AIRole.SENTINEL);
        testInterceptor = base("ai-test-interceptor", true, sentinel, 2800f, 1.75f, 27f);
        testInterceptor.weapons.add(weapon(175f, 24f, 22f));
        testInterceptor.abilities.add(
                intercept(150f, 5f, 60f, 2),
                barrier(180f, 95f, 2.5f, 20f)
        );
AIProfile commander = AIProfiles.assault().role(AIRole.COMMANDER).preferredRange(0.82f).retreat(0.22f);
        commander.tacticalLinkRange = 210f;
        testCarrier = base("ai-test-carrier", true, commander, 5200f, 1.35f, 36f);
        testCarrier.weapons.add(weapon(210f, 38f, 28f));
        testCarrier.abilities.add(
                droneBay(testDrone, 120f, 3, 260f),
                tacticalLink(210f, 10f)
        );
testFlanker = base("ai-test-flanker", true, AIProfiles.flank(), 1700f, 2.45f, 23f);
        testFlanker.weapons.add(weapon(175f, 28f, 18f));
testBomber = base("ai-test-bomber", true, AIProfiles.bomber(), 2400f, 2.15f, 28f);
        testBomber.weapons.add(weapon(125f, 65f, 45f));
        testBomber.abilities.add(new BurstDriveAbility(150f, 4f));
}

    private static UnitType base(
            String name,
            boolean flying,
            AIProfile profile,
            float health,
            float speed,
            float hitSize
    ) {
        UnitType unit = new UnitType(name);

        unit.constructor = UnitEntity::create;
        unit.flying = flying;
        unit.health = health;
        unit.speed = speed;
        unit.hitSize = hitSize;
        unit.armor = Math.max(0f, hitSize / 8f - 1f);

        unit.rotateSpeed = flying ? 6.5f : 3.5f;
        unit.accel = flying ? 0.12f : 0.12f;
        unit.drag = flying ? 0.035f : 0.09f;

        unit.targetAir = true;
        unit.targetGround = true;
        unit.faceTarget = true;
        unit.omniMovement = true;

        unit.drawCell = false;
        unit.createWreck = false;
        unit.outlineRadius = 2;

        unit.engineOffset = flying ? hitSize * 0.42f : 0f;
        unit.engineSize = flying ? Math.max(2.4f, hitSize * 0.12f) : 0f;

        // Enemy/AI-team autonomous controller.
        unit.aiController = () -> new SmartAI(profile);

        // Player RTS teams receive a CommandAI-compatible wrapper that keeps the same profile.
        // Direct commands own movement; when the command ends, SmartAI resumes automatically.
        unit.controller = u ->
                !unit.playerControllable || (u.team.isAI() && !u.team.rules().rtsAi)
                        ? unit.aiController.get()
                        : new SmartCommandAI(profile);

        return unit;
    }

    private static Weapon weapon(float range, float damage, float reload) {
        BasicBulletType bullet = new BasicBulletType(5.5f, damage);
        bullet.lifetime = range / bullet.speed;
        bullet.width = 7f;
        bullet.height = 11f;
        bullet.hitSize = 5f;
        bullet.collidesAir = true;
        bullet.collidesGround = true;

        Weapon weapon = new Weapon();
        weapon.mirror = false;
        weapon.rotate = true;
        weapon.rotateSpeed = 9f;
        weapon.reload = reload;
        weapon.shootY = 8f;
        weapon.recoil = 1.5f;
        weapon.bullet = bullet;
        return weapon;
    }

    private static HeatAbility heat(float heatTime, float coolTime) {
        HeatAbility ability = new HeatAbility();
        ability.heatTime = heatTime;
        ability.coolTime = coolTime;
        ability.warmThreshold = 0.45f;
        ability.overheatThreshold = 0.98f;
        ability.recoverThreshold = 0.55f;
        return ability;
    }

    private static PhaseBlinkAbility blink(float reload, float threshold, float distance) {
        PhaseBlinkAbility ability = new PhaseBlinkAbility();
        ability.reload = reload;
        ability.healthThreshold = threshold;
        ability.distance = distance;
        ability.threatRange = 220f;
        return ability;
    }

    private static EmergencyRepairAbility emergencyRepair(float threshold, float heal, float reload) {
        EmergencyRepairAbility ability = new EmergencyRepairAbility();
        ability.threshold = threshold;
        ability.healFraction = heal;
        ability.reload = reload;
        return ability;
    }

    private static TargetMarkAbility targetMark(float reload, float duration, float range) {
        TargetMarkAbility ability = new TargetMarkAbility();
        ability.reload = reload;
        ability.duration = duration;
        ability.range = range;
        return ability;
    }

    private static LockOnAbility lockOn(float stage1, float stage2) {
        LockOnAbility ability = new LockOnAbility();
        ability.stage1Time = stage1;
        ability.stage2Time = stage2;
        return ability;
    }

    private static ExecutionAbility execution(float threshold) {
        ExecutionAbility ability = new ExecutionAbility();
        ability.threshold = threshold;
        return ability;
    }

    private static CapacitorAbility capacitor(
            float chargeTime,
            float damageChargeScale,
            float shieldGain,
            float maxShield
    ) {
        CapacitorAbility ability = new CapacitorAbility();
        ability.chargeTime = chargeTime;
        ability.damageChargeScale = damageChargeScale;
        ability.shieldGain = shieldGain;
        ability.maxShield = maxShield;
        return ability;
    }

    private static GravityFieldAbility gravity(float range, float pull) {
        GravityFieldAbility ability = new GravityFieldAbility();
        ability.range = range;
        ability.pull = pull;
        ability.statusDuration = 15f;
        return ability;
    }

    private static ShieldLinkAbility shieldLink(
            float range,
            float regenPerAlly,
            float maxShield,
            int maxLinks,
            float checkInterval
    ) {
        ShieldLinkAbility ability = new ShieldLinkAbility();
        ability.range = range;
        ability.regenPerAlly = regenPerAlly;
        ability.maxShield = maxShield;
        ability.maxLinks = maxLinks;
        ability.checkInterval = checkInterval;
        return ability;
    }

    private static TacticalLinkAbility tacticalLink(float range, float refresh) {
        TacticalLinkAbility ability = new TacticalLinkAbility();
        ability.range = range;
        ability.refreshInterval = refresh;
        return ability;
    }

    private static InterceptMatrixAbility intercept(
            float range,
            float scanInterval,
            float threatThreshold,
            int maxIntercepts
    ) {
        InterceptMatrixAbility ability = new InterceptMatrixAbility();
        ability.range = range;
        ability.scanInterval = scanInterval;
        ability.threatThreshold = threatThreshold;
        ability.maxInterceptsPerScan = maxIntercepts;
        return ability;
    }

    private static BarrierPulseAbility barrier(
            float reload,
            float range,
            float knockback,
            float maxBulletDamage
    ) {
        BarrierPulseAbility ability = new BarrierPulseAbility();
        ability.reload = reload;
        ability.range = range;
        ability.knockback = knockback;
        ability.maxBulletDamage = maxBulletDamage;
        return ability;
    }

    private static DroneBayAbility droneBay(UnitType drone, float spawnTime, int maxDrones, float countRange) {
        DroneBayAbility ability = new DroneBayAbility(drone);
        ability.spawnTime = spawnTime;
        ability.maxDrones = maxDrones;
        ability.countRange = countRange;
        ability.spawnDistance = 24f;
        return ability;
    }

    private ModTestUnits() {
    }
}
