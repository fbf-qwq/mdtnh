package mdtnh.test;

import arc.graphics.Color;
import mdtnh.combat.api.projectile.BehaviorArtilleryBulletType;
import mdtnh.combat.api.projectile.BehaviorBasicBulletType;
import mdtnh.combat.api.visual.MdtVisualUnitType;
import mdtnh.combat.api.visual.StateVisualAbility;
import mdtnh.combat.impl.CombatStateVisualAbility;
import mdtnh.combat.impl.MdtCombatRuntime;
import mdtnh.combat.impl.abilities.MechanicAbilities.*;
import mdtnh.combat.impl.abilities.DroneBayStatusAbility;
import mdtnh.combat.impl.abilities.DroneBayWeapon;
import mdtnh.combat.impl.abilities.ModeWeapon;
import mdtnh.combat.impl.abilities.TimedDecoyAbility;
import mdtnh.combat.impl.abilities.WeaponModeAbility;
import mdtnh.combat.impl.projectile.ProjectileBehaviors.*;
import mdtnh.combat.impl.projectile.TriangleBulletType;
import mdtnh.combat.impl.support.SupportAbilities.*;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Healthc;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * Development-only test content for the active A/B/S mechanics; A08/A09/A25/B12 are retired.
 *
 * Multi-mechanic units use ONE ModeWeapon, so only one weapon mode fires at a time.
 * When directly controlling one of these units, press V to cycle its weapon mode.
 */
public final class ModMechanicTestUnits {
    public static UnitType
        ballistic,
        reaction,
        geometry,
        artillery,
        missile,
        energy,
        control,
        phase,
        supportTech,
        supportControl,
        supportNetwork,
        supportLogistics,
        drone,
        decoy;

    public static void load() {
        createInternalHelpers();

        createBallistic();
        createReaction();
        createGeometry();
        createArtillery();
        createMissile();
        createEnergy();
        createControl();
        createPhase();

        createSupportTech();
        createSupportControl();
        createSupportNetwork();
        createSupportLogistics();
    }

    private static void createBallistic() {
        ballistic = base("mechanic-test-ballistic", false, 5200f, 0.72f, 34f);
        ballistic.armor = 13f;

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.backspray",
            "mdt.weaponmode.corrosion",
            "mdt.weaponmode.ap",
            "mdt.weaponmode.he",
            "mdt.weaponmode.aa"
        );
        modes.autoCycleAI = false;

        ModeWeapon weapon = modeWeapon(
            "mechanic-test-ballistic-weapon",
            38f, 245f,
            backSprayMass(),
            corrosionRound(),
            apRound(),
            heRound(),
            aaRound()
        );

        // AI tests B05 automatically. Direct player control can still press V and select A01/A21 manually.
        weapon.aiSelector = (unit, mount) -> {
            if (mount.target instanceof Unit target) {
                if (target.isFlying()) return 4;
                if (target.hitSize >= 30f || target.type.armor >= 10f) return 2;
                return 3;
            }

            if (mount.target instanceof Building build) {
                return build.block.size >= 3 || build.block.armor >= 5f ? 2 : 3;
            }

            return 3;
        };

        ballistic.weapons.add(weapon);
        ballistic.abilities.add(new Ability[]{
            modes,
            new FacingArmor(),
            ablative(5, 0.065f),
            new DamageGate(),
            new AdaptiveArmor(),
            new CombatStateVisualAbility()
        });
    }

    private static void createReaction() {
        reaction = base("mechanic-test-reaction", false, 3900f, 0.95f, 30f);

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.primer",
            "mdt.weaponmode.detonator",
            "mdt.weaponmode.fracture",
            "mdt.weaponmode.sticky"
        );
        modes.autoCycleAI = false;

        ModeWeapon weapon = modeWeapon(
            "mechanic-test-reaction-weapon",
            32f, 225f,
            reactionPrimer(),
            reactionDetonator(),
            fractureRound(),
            stickyRound()
        );

        // Autonomous test sequence: build primer stacks, then detonate.
        weapon.aiSelector = (unit, mount) -> {
            if (mount.target instanceof Healthc health) {
                return MdtCombatRuntime.reactionStacks(health) >= 3 ? 1 : 0;
            }
            return 0;
        };

        reaction.weapons.add(weapon);
        reaction.abilities.add(new Ability[]{
            modes,
            new RecoilAnchor(),
            new LastStand(),
            new CombatStateVisualAbility()
        });
    }

    private static void createGeometry() {
        geometry = base("mechanic-test-geometry", true, 3200f, 1.75f, 28f);

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.anchor",
            "mdt.weaponmode.ring"
        );
        modes.aiCycleTime = 210f;

        geometry.weapons.add(modeWeapon(
            "mechanic-test-geometry-weapon",
            46f, 215f,
            anchorRicochet(),
            collapsingRingRound()
        ));

        geometry.abilities.add(new Ability[]{
            modes,
            new Momentum(),
            new CombatStateVisualAbility()
        });
    }

    private static void createArtillery() {
        artillery = base("mechanic-test-artillery", false, 5600f, 0.62f, 38f);
        artillery.range = 340f;

        DeployMechanic deploy = new DeployMechanic();
        deploy.deployTime = 70f;

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.delayedfuse",
            "mdt.weaponmode.seismic"
        );
        modes.aiCycleTime = 250f;

        artillery.weapons.add(modeWeapon(
            "mechanic-test-artillery-weapon",
            92f, 330f,
            delayedFuseArtillery(),
            shockwaveArtillery()
        ));

        artillery.abilities.add(new Ability[]{
            modes,
            deploy,
            new RecoilAnchor(),
            new CounterBattery(),
            new StateVisualAbility(),
            new CombatStateVisualAbility()
        });
    }

    private static void createMissile() {
        missile = base("mechanic-test-missile", true, 3500f, 1.45f, 30f);

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.split",
            "mdt.weaponmode.flak"
        );
        modes.autoCycleAI = false;

        ModeWeapon weapon = modeWeapon(
            "mechanic-test-missile-weapon",
            48f, 285f,
            splitDecoyMissile(),
            velocityFlak()
        );

        weapon.aiSelector = (unit, mount) -> {
            if (mount.target instanceof Unit target && target.isFlying() && target.vel.len() >= 1.4f) {
                return 1;
            }
            return 0;
        };

        missile.weapons.add(weapon);
        missile.abilities.add(new Ability[]{
            modes,
            new Momentum(),
            new BurstDrive(),
            new CombatStateVisualAbility()
        });
    }

    private static void createEnergy() {
        energy = base("mechanic-test-energy", false, 4700f, 0.86f, 34f);

        HeatMechanic heat = new HeatMechanic();
        heat.heatPerShot = 0.050f;
        heat.coolPerTick = 0.0035f;

        Capacitor capacitor = new Capacitor();
        capacitor.chargePerShot = 0.11f;

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.chain",
            "mdt.weaponmode.shielddrain",
            "mdt.weaponmode.ablation"
        );
        modes.aiCycleTime = 220f;

        energy.weapons.add(modeWeapon(
            "mechanic-test-energy-weapon",
            30f, 220f,
            chainRound(),
            shieldDrainRound(),
            ablationRound()
        ));

        energy.abilities.add(new Ability[]{
            modes,
            heat,
            capacitor,
            new StateVisualAbility(),
            new CombatStateVisualAbility()
        });
    }

    private static void createControl() {
        control = base("mechanic-test-control", false, 5000f, 0.88f, 36f);

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.tractor",
            "mdt.weaponmode.displace",
            "mdt.weaponmode.gravity"
        );
        modes.aiCycleTime = 220f;

        control.weapons.add(modeWeapon(
            "mechanic-test-control-weapon",
            48f, 235f,
            tractorRound(),
            displacementRound(),
            gravityCoreRound()
        ));

        control.abilities.add(new Ability[]{
            modes,
            new Capacitor(),
            new CombatStateVisualAbility()
        });
    }

    private static void createPhase() {
        phase = base("mechanic-test-phase", true, 3100f, 2.15f, 27f);

        WeaponModeAbility modes = modes(
            "mdt.weaponmode.inertiaphase",
            "mdt.weaponmode.prism"
        );
        modes.aiCycleTime = 210f;

        phase.weapons.add(modeWeapon(
            "mechanic-test-phase-weapon",
            46f, 230f,
            inertiaPhaseRound(),
            movingPrismRound()
        ));

        PhaseBlink blink = new PhaseBlink();
        blink.cooldown = 120f;

        phase.abilities.add(new Ability[]{
            modes,
            new Momentum(),
            blink,
            new BurstDrive(),
            new StateVisualAbility(),
            new CombatStateVisualAbility()
        });
    }

    private static void createSupportTech() {
        supportTech = base("mechanic-test-support-tech", true, 3900f, 1.25f, 30f);
        supportTech.weapons.add(weapon(simpleBullet(5.0f, 24f, Color.valueOf("a8e8ff")), 26f, 180f));

        supportTech.abilities.add(new Ability[]{
            new HeatMechanic(),
            new Capacitor(),
            new HeatTransfer(),
            new CapacitorTransfer(),
            new ReloadService(),
            new StatusCleanse(),
            new StateVisualAbility(),
            new CombatStateVisualAbility()
        });
    }

    private static void createSupportControl() {
        supportControl = base("mechanic-test-support-control", true, 4400f, 1.10f, 33f);
        supportControl.weapons.add(weapon(simpleBullet(4.5f, 18f, Color.valueOf("b9d6ff")), 28f, 175f));

        supportControl.abilities.add(new Ability[]{
            new RescueTractor(),
            new VectorAssist(),
            new ElectronicSuppression(),
            new DecoyChaff(decoy),
            new DeflectionWedge(),
            new DamageRedirect(),
            new StabilizationField(),
            new PhaseCorridor(),
            new CombatStateVisualAbility()
        });
    }

    private static void createSupportNetwork() {
        supportNetwork = base("mechanic-test-support-network", true, 4100f, 1.18f, 31f);
        supportNetwork.weapons.add(weapon(simpleBullet(5.4f, 20f, Color.valueOf("ffd37f")), 30f, 200f));

        supportNetwork.abilities.add(new Ability[]{
            new FireControlLink(),
            new CounterBatteryMark(),
            new TargetDesignation(),
            new FormationCoordination(),
            new ThreatWarning(),
            new CombatStateVisualAbility()
        });
    }

    private static void createSupportLogistics() {
        supportLogistics = base("mechanic-test-support-logistics", true, 4600f, 1.05f, 34f);
        supportLogistics.buildSpeed = 2.5f;

        DroneBayWeapon bay = new DroneBayWeapon(
            Vars.content.transformName("mechanic-test-support-logistics-dronebay"),
            drone
        );
        bay.spawnTime = 90f;
        bay.maxDrones = 3;
        bay.x = 0f;
        bay.y = -4f;
        bay.spawnDistance = 18f;

        supportLogistics.abilities.add(new Ability[]{
            new DroneBayStatusAbility(),
            new DroneMaintenance(drone),
            new BuildAssist(),
            new StateVisualAbility(),
            new CombatStateVisualAbility()
        });

        supportLogistics.weapons.add(
            bay,
            weapon(simpleBullet(4.2f, 16f, Color.valueOf("92f0ce")), 36f, 165f)
        );
    }

    private static void createInternalHelpers() {
        decoy = base("mechanic-test-decoy", true, 90f, 2.8f, 11f);
        decoy.useUnitCap = false;
        decoy.targetPriority = 3f;
        decoy.abilities.add(new TimedDecoyAbility(120f));

        drone = base("mechanic-test-drone", true, 520f, 2.4f, 15f);
        drone.useUnitCap = false;
        drone.weapons.add(weapon(simpleBullet(6.2f, 17f, Color.valueOf("8fe9ff")), 18f, 110f));
    }

    private static MdtVisualUnitType base(
        String name, boolean flying, float health, float speed, float hitSize
    ) {
        MdtVisualUnitType unit = new MdtVisualUnitType(name);
        unit.constructor = UnitEntity::create;
        unit.flying = flying;
        unit.health = health;
        unit.speed = speed;
        unit.hitSize = hitSize;
        unit.armor = Math.max(0f, hitSize / 5f - 2f);

        unit.rotateSpeed = flying ? 5.8f : 3.2f;
        unit.accel = flying ? 0.10f : 0.08f;
        unit.drag = flying ? 0.035f : 0.09f;

        unit.targetAir = true;
        unit.targetGround = true;
        unit.faceTarget = true;
        unit.omniMovement = flying;

        unit.drawCell = false;
        unit.createWreck = false;
        unit.outlineRadius = 2;

        if (flying) {
            unit.engineOffset = hitSize * 0.38f;
            unit.engineSize = Math.max(2.8f, hitSize * 0.10f);
        }

        return unit;
    }

    private static WeaponModeAbility modes(String... keys) {
        WeaponModeAbility ability = new WeaponModeAbility(keys);
        ability.aiCycleTime = 180f;
        return ability;
    }

    private static ModeWeapon modeWeapon(
        String spriteBase,
        float reload,
        float range,
        BulletType... bullets
    ) {
        ModeWeapon weapon = new ModeWeapon(Vars.content.transformName(spriteBase));
        weapon.mirror = false;
        weapon.rotate = true;
        weapon.rotateSpeed = 8f;
        weapon.reload = reload;
        weapon.shootY = 7f;
        weapon.recoil = 2f;
        weapon.modes(bullets);

        for (BulletType bullet : bullets) {
            bullet.lifetime = range / Math.max(0.01f, bullet.speed);
        }

        return weapon;
    }

    private static Weapon weapon(BulletType bullet, float reload, float range) {
        Weapon weapon = new Weapon();
        weapon.mirror = false;
        weapon.rotate = true;
        weapon.rotateSpeed = 8f;
        weapon.reload = reload;
        weapon.shootY = 7f;
        weapon.recoil = 2f;
        weapon.bullet = bullet;

        bullet.lifetime = range / Math.max(0.01f, bullet.speed);
        return weapon;
    }

    private static BasicBulletType simpleBullet(float speed, float damage, Color color) {
        BasicBulletType bullet = new BasicBulletType(speed, damage);
        bullet.width = 7f;
        bullet.height = 12f;
        bullet.hitSize = 5f;
        bullet.backColor = color;
        bullet.frontColor = Color.white;
        bullet.collidesAir = true;
        bullet.collidesGround = true;
        return bullet;
    }

    private static BehaviorBasicBulletType behaviorBullet(float speed, float damage, Color color) {
        BehaviorBasicBulletType bullet = new BehaviorBasicBulletType(speed, damage);
        bullet.width = 8f;
        bullet.height = 14f;
        bullet.hitSize = 6f;
        bullet.backColor = color;
        bullet.frontColor = Color.white;
        bullet.collidesAir = true;
        bullet.collidesGround = true;
        return bullet;
    }

    private static BehaviorArtilleryBulletType artilleryBullet(float speed, float damage, Color color) {
        BehaviorArtilleryBulletType bullet = new BehaviorArtilleryBulletType(speed, damage);
        bullet.width = 13f;
        bullet.height = 18f;
        bullet.hitSize = 8f;
        bullet.backColor = color;
        bullet.frontColor = Color.white;
        bullet.collidesAir = true;
        bullet.collidesGround = true;
        bullet.despawnHit = true;
        return bullet;
    }

    // A01 + A04
    private static BehaviorBasicBulletType backSprayMass() {
        TriangleBulletType fragment = new TriangleBulletType(7.4f, 30f, Color.valueOf("ffd38c"));
        fragment.triangleWidth = 5.2f;
        fragment.triangleLength = 10f;
        fragment.tailLength = 4.5f;
        fragment.lifetime = 28f;
        fragment.pierce = true;
        fragment.pierceCap = 2;
        fragment.collidesAir = true;
        fragment.collidesGround = true;

        BackSprayFragment back = new BackSprayFragment(fragment);
        back.fragments = 12;
        back.cone = 62f;

        MassImpact mass = new MassImpact();
        mass.extraPerSize = 1.6f;
        mass.impulse = 10f;

        return behaviorBullet(7.5f, 110f, Color.valueOf("d9d0bd"))
            .behavior(back)
            .behavior(mass);
    }

    // A02
    private static BehaviorBasicBulletType stickyRound() {
        StickyExplosive sticky = new StickyExplosive();
        sticky.delay = 75f;
        sticky.radius = 44f;
        sticky.damage = 190f;

        return behaviorBullet(5.0f, 18f, Color.valueOf("ff9d66"))
            .behavior(sticky);
    }

    // B05 AP
    private static BasicBulletType apRound() {
        BasicBulletType bullet = simpleBullet(8.6f, 82f, Color.valueOf("eeeeee"));
        bullet.width = 5f;
        bullet.height = 16f;
        bullet.pierce = true;
        bullet.pierceCap = 3;
        return bullet;
    }

    // B05 HE
    private static BasicBulletType heRound() {
        BasicBulletType bullet = simpleBullet(5.7f, 38f, Color.valueOf("ffae64"));
        bullet.width = 10f;
        bullet.height = 13f;
        bullet.splashDamage = 70f;
        bullet.splashDamageRadius = 35f;
        return bullet;
    }

    // B05 AA
    private static BasicBulletType aaRound() {
        BasicBulletType bullet = simpleBullet(7.3f, 30f, Color.valueOf("8ed7ff"));
        bullet.collidesGround = false;
        bullet.collidesAir = true;
        bullet.splashDamage = 42f;
        bullet.splashDamageRadius = 28f;
        return bullet;
    }

    // A21
    private static BehaviorBasicBulletType corrosionRound() {
        AdhesiveCorrosion corrosion = new AdhesiveCorrosion();
        corrosion.duration = 240f;

        return behaviorBullet(5.2f, 32f, Color.valueOf("9dd35f"))
            .behavior(corrosion);
    }

    // A07 primer
    private static BehaviorBasicBulletType reactionPrimer() {
        ReactionPrimer primer = new ReactionPrimer();
        primer.max = 3;

        return behaviorBullet(5.4f, 18f, Color.valueOf("ffb06b"))
            .behavior(primer);
    }

    // A07 detonator
    private static BehaviorBasicBulletType reactionDetonator() {
        ReactionDetonator detonator = new ReactionDetonator();
        detonator.damagePerStack = 120f;

        return behaviorBullet(6.5f, 42f, Color.valueOf("fff1cf"))
            .behavior(detonator);
    }

    // A13
    private static BehaviorBasicBulletType fractureRound() {
        BasicBulletType fragment = simpleBullet(6.5f, 20f, Color.valueOf("ff8277"));
        fragment.lifetime = 18f;

        FractureStack fracture = new FractureStack();
        fracture.threshold = 4;
        fracture.fragment = fragment;

        return behaviorBullet(6.2f, 55f, Color.valueOf("ff6b6b"))
            .behavior(fracture);
    }

    // A09 + A10 + A22
    private static BehaviorBasicBulletType anchorRicochet() {
        CuttingAnchor anchor = new CuttingAnchor();
        anchor.lineDamage = 100f;
        anchor.lineWidth = 8f;

        LimitedRicochet bounce = new LimitedRicochet();
        bounce.maxBounces = 2;

        BehaviorBasicBulletType bullet = behaviorBullet(5.2f, 44f, Color.valueOf("db91ff"))
            .behavior(anchor)
            .behavior(bounce);

        bullet.collidesTiles = false;
        return bullet;
    }

    // A12
    private static BehaviorBasicBulletType collapsingRingRound() {
        BasicBulletType child = simpleBullet(5.6f, 22f, Color.valueOf("9de1ff"));
        child.lifetime = 18f;

        CollapsingRing ring = new CollapsingRing(child);
        ring.count = 12;
        ring.radius = 58f;

        return behaviorBullet(4.8f, 38f, Color.valueOf("7fcfff"))
            .behavior(ring);
    }

    // A14 - delayed impact fuse
    private static BehaviorArtilleryBulletType delayedFuseArtillery() {
        DelayedFuse fuse = new DelayedFuse();
        fuse.delay = 42f;
        fuse.blastRadius = 58f;
        fuse.blastDamage = 190f;

        return artilleryBullet(3.15f, 72f, Color.valueOf("d8b27a"))
            .behavior(fuse);
    }

    // A15
    private static BehaviorArtilleryBulletType shockwaveArtillery() {
        SubsurfaceShockwave wave = new SubsurfaceShockwave();
        wave.arms = 6;
        wave.eruptionsPerArm = 4;
        wave.damage = 62f;
        wave.knockback = 3.0f;

        return artilleryBullet(2.7f, 76f, Color.valueOf("c49366"))
            .behavior(wave);
    }

    // A16 + A17
    private static BehaviorBasicBulletType splitDecoyMissile() {
        TriangleBulletType child = new TriangleBulletType(6.7f, 34f, Color.valueOf("b6d8ff"));
        child.triangleWidth = 5.4f;
        child.triangleLength = 11f;
        child.tailLength = 4.2f;
        child.homingPower = 0.06f;
        child.homingRange = 120f;
        child.lifetime = 30f;
        child.collidesAir = true;
        child.collidesGround = true;

        BasicBulletType decoyBullet = simpleBullet(5.3f, 0f, Color.valueOf("eeeeff"));
        decoyBullet.hittable = true;
        decoyBullet.absorbable = true;
        decoyBullet.lifetime = 45f;

        TopAttackSplit split = new TopAttackSplit(child);
        split.count = 5;
        split.splitAt = 0.58f;

        DecoySalvo decoys = new DecoySalvo(decoyBullet);
        decoys.count = 5;

        BehaviorBasicBulletType bullet = behaviorBullet(4.0f, 28f, Color.valueOf("91bcff"));
        bullet.homingPower = 0.035f;
        bullet.homingRange = 180f;
        bullet.behavior(split).behavior(decoys);
        return bullet;
    }

    // A26
    private static BehaviorBasicBulletType velocityFlak() {
        VelocityFuseFlak flak = new VelocityFuseFlak();
        flak.blastDamage = 140f;
        flak.speedFactor = 20f;
        flak.searchRange = 120f;
        flak.blastRadius = 46f;

        return behaviorBullet(6.5f, 16f, Color.valueOf("ffd7a6"))
            .behavior(flak);
    }

    // A18
    private static BehaviorBasicBulletType chainRound() {
        ChargedChainLightning chain = new ChargedChainLightning();
        chain.jumps = 5;
        chain.firstDamage = 30f;
        chain.growth = 1.30f;

        return behaviorBullet(6.0f, 42f, Color.valueOf("85cfff"))
            .behavior(chain);
    }

    // A19
    private static BehaviorBasicBulletType shieldDrainRound() {
        ShieldDrain drain = new ShieldDrain();
        drain.drain = 220f;

        return behaviorBullet(5.8f, 22f, Color.valueOf("8fe9ff"))
            .behavior(drain);
    }

    // A20
    private static BehaviorBasicBulletType ablationRound() {
        AblationBeamHit ablation = new AblationBeamHit();
        ablation.armorPerStack = 2.5f;
        ablation.maxStacks = 5;

        return behaviorBullet(6.3f, 24f, Color.valueOf("ffb36e"))
            .behavior(ablation);
    }

    // A05
    private static BehaviorBasicBulletType tractorRound() {
        TractorLance tractor = new TractorLance();
        tractor.duration = 70f;
        tractor.pull = 0.31f;

        return behaviorBullet(5.0f, 16f, Color.valueOf("bbc6ff"))
            .behavior(tractor);
    }

    // A06
    private static BehaviorBasicBulletType displacementRound() {
        DisplacementShot push = new DisplacementShot();
        push.impulse = 14f;

        return behaviorBullet(6.0f, 24f, Color.valueOf("a8d8ff"))
            .behavior(push);
    }

    // A24
    private static BehaviorBasicBulletType gravityCoreRound() {
        GravityCore gravity = new GravityCore();
        gravity.pullRange = 118f;
        gravity.pull = 0.66f;
        gravity.collapseDamage = 220f;

        BehaviorBasicBulletType bullet = behaviorBullet(2.0f, 12f, Color.valueOf("9a82ff"));
        bullet.width = 16f;
        bullet.height = 16f;
        bullet.collides = false;
        bullet.collidesTiles = false;
        bullet.behavior(gravity);
        return bullet;
    }

    // A03 + A08 + A11; deliberately slow enough for A11 to be visually readable.
    private static BehaviorBasicBulletType inertiaPhaseRound() {
        InertiaShot inertia = new InertiaShot();

        PhaseProjectile phaseProjectile = new PhaseProjectile();
        phaseProjectile.phaseDistance = 70f;
        phaseProjectile.triggerDistance = 105f;
        phaseProjectile.landingClearance = 30f;

        return behaviorBullet(3.35f, 56f, Color.valueOf("c2c3ff"))
            .behavior(inertia)
            .behavior(phaseProjectile);
    }

    // A23
    private static BehaviorBasicBulletType movingPrismRound() {
        BasicBulletType side = simpleBullet(7.5f, 18f, Color.valueOf("e0b2ff"));
        side.lifetime = 14f;

        MovingPrism prism = new MovingPrism(side);
        prism.interval = 8f;

        BehaviorBasicBulletType bullet = behaviorBullet(3.7f, 34f, Color.valueOf("d39cff"));
        bullet.width = 12f;
        bullet.height = 12f;
        bullet.behavior(prism);
        return bullet;
    }

    private static AblativeArmor ablative(int plates, float triggerFraction) {
        AblativeArmor armor = new AblativeArmor();
        armor.maxPlates = plates;
        armor.triggerDamageFraction = triggerFraction;
        return armor;
    }

    private ModMechanicTestUnits() {
    }
}
