package mdtnh.units;

import arc.graphics.Color;
import arc.math.Mathf;
import mdtnh.ModUnits;
import mdtnh.combat.api.projectile.BehaviorArtilleryBulletType;
import mdtnh.combat.api.projectile.BehaviorBasicBulletType;
import mdtnh.combat.api.visual.MdtVisualUnitType;
import mdtnh.combat.api.visual.StateVisualAbility;
import mdtnh.combat.impl.CombatStateVisualAbility;
import mdtnh.combat.impl.abilities.AdvancedAbilities;
import mdtnh.combat.impl.abilities.DroneBayStatusAbility;
import mdtnh.combat.impl.abilities.DroneBayWeapon;
import mdtnh.combat.impl.abilities.MechanicAbilities;
import mdtnh.combat.impl.abilities.ModeWeapon;
import mdtnh.combat.impl.abilities.TimedDecoyAbility;
import mdtnh.combat.impl.abilities.WeaponModeAbility;
import mdtnh.combat.impl.projectile.ProjectileBehaviors;
import mdtnh.combat.impl.projectile.TriangleBulletType;
import mdtnh.combat.impl.support.SupportAbilities;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.SuicideAI;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.EnergyFieldAbility;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.abilities.StatusFieldAbility;
import mindustry.entities.abilities.SuppressionFieldAbility;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BombBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.entities.bullet.EmpBulletType;
import mindustry.entities.bullet.LaserBoltBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.entities.bullet.LiquidBulletType;
import mindustry.entities.bullet.MissileBulletType;
import mindustry.entities.bullet.PointLaserBulletType;
import mindustry.entities.bullet.RailBulletType;
import mindustry.entities.bullet.SapBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.gen.LegsUnit;
import mindustry.gen.UnitEntity;
import mindustry.gen.UnitWaterMove;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.weapons.PointDefenseWeapon;
import mindustry.type.weapons.RepairBeamWeapon;

public final class ModUnitFactory {
    public enum Line {
        NAVAL_ATTACK, NAVAL_SUPPORT, GROUND_ATTACK, GROUND_SUPPORT, AIR_ATTACK, AIR_SUPPORT, CRAWLER
    }

    public enum Standard {
        RAPID, CANNON, HEAVY,
        ARTILLERY, MISSILE, BOMB, RAIL,
        LASER_BOLT, LASER, CONTINUOUS, POINT,
        LIGHTNING, EMP, SAP, LIQUID, SHRAPNEL,
        FLAK, FLAME, HEAL, MELEE
    }

    public enum Special {
        A01, A02, A03, A04, A05, A06, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22, A23, A24, A26
    }

    private static final float[] healthScale = {
        1f, 1.5f, 2.2f, 3.2f, 4.8f, 7f, 10f, 14.5f, 20f, 28f, 39f, 54f, 75f, 104f, 145f
    };
    private static final float[] damageScale = {
        1f, 1.22f, 1.48f, 1.8f, 2.18f, 2.65f, 3.2f, 3.9f, 4.75f, 5.8f, 7.1f, 8.7f, 10.7f, 13.2f, 16.2f
    };
    private static final float[] armorScale = {
        0f, 1f, 2f, 4f, 6f, 8f, 11f, 14f, 18f, 22f, 27f, 32f, 38f, 45f, 54f
    };

    private static final int[][] bodyPixels = {
        {96,128,160,192,224,256,320,384,416,448,480,512,544,592,640},
        {96,128,160,192,224,256,320,384,416,448,480,512,544,592,640},
        {80,96,128,160,192,224,272,320,352,384,416,448,480,512,560},
        {80,96,128,160,192,224,272,320,352,384,416,448,480,512,560},
        {80,96,112,144,176,208,256,288,320,352,384,416,448,480,512},
        {80,96,112,144,176,208,256,288,320,352,384,416,448,480,512},
        {80,96,128,160,192,224,272,336,368,400,432,464,496,536,576}
    };

    public static int bodyPixels(Line line, int tier) {
        return bodyPixels[line.ordinal()][tier];
    }

    public static float hitSize(Line line, int tier) {
        // Final sprites are 4x the old visual canvas; hitbox is roughly 2x the old hitbox.
        return bodyPixels(line, tier) / 8f;
    }

    public static MdtVisualUnitType base(String id, Line line, int tier) {
        MdtVisualUnitType unit = new MdtVisualUnitType(id);
        float hit = hitSize(line, tier);

        float baseHealth = switch (line) {
            case NAVAL_ATTACK -> 620f;
            case NAVAL_SUPPORT -> 520f;
            case GROUND_ATTACK -> 560f;
            case GROUND_SUPPORT -> 470f;
            case AIR_ATTACK -> 390f;
            case AIR_SUPPORT -> 350f;
            case CRAWLER -> 500f;
        };

        unit.health = baseHealth * healthScale[tier];
        unit.hitSize = hit;
        unit.armor = armorScale[tier] + switch (line) {
            case NAVAL_ATTACK, GROUND_ATTACK, CRAWLER -> 2f;
            case NAVAL_SUPPORT, GROUND_SUPPORT -> 1f;
            default -> 0f;
        };

        unit.drawCell = false;
        unit.createWreck = false;
        unit.outlineRadius = 3;
        unit.alwaysCreateOutline = true;
        unit.targetAir = true;
        unit.targetGround = true;
        unit.singleTarget = false;

        switch (line) {
            case NAVAL_ATTACK, NAVAL_SUPPORT -> {
                unit.constructor = UnitWaterMove::create;
                unit.speed = Math.max(0.48f, (line == Line.NAVAL_ATTACK ? 1.10f : 1.00f) - tier * 0.035f);
                unit.drag = 0.13f + tier * 0.002f;
                unit.accel = Math.max(0.12f, 0.34f - tier * 0.012f);
                unit.rotateSpeed = Math.max(0.75f, 3.5f - tier * 0.16f);
                unit.faceTarget = false;
                unit.trailLength = 18 + tier * 4;
                unit.waveTrailX = hit * 0.34f;
                unit.waveTrailY = -hit * 0.40f;
                unit.trailScl = Math.max(1f, hit / 12f);
            }
            case AIR_ATTACK, AIR_SUPPORT -> {
                unit.constructor = UnitEntity::create;
                unit.flying = true;
                unit.lowAltitude = tier < 7;
                unit.speed = Math.max(1.05f, (line == Line.AIR_ATTACK ? 2.45f : 2.20f) - tier * 0.075f);
                unit.drag = 0.035f;
                unit.accel = 0.09f;
                unit.rotateSpeed = Math.max(2.0f, 8.0f - tier * 0.32f);
                unit.engineOffset = hit * 0.42f;
                unit.engineSize = Math.max(2.4f, hit * 0.13f);
                unit.trailLength = 10 + tier * 2;
            }
            case CRAWLER -> {
                unit.constructor = LegsUnit::create;
                unit.speed = Math.max(0.48f, 1.00f - tier * 0.032f);
                unit.drag = 0.28f;
                unit.accel = 0.16f;
                unit.rotateSpeed = Math.max(1.2f, 4.0f - tier * 0.15f);
                unit.legCount = tier < 3 ? 4 : tier < 8 ? 6 : 8;
                unit.legGroupSize = 2;
                unit.legLength = hit * 0.92f;
                unit.legBaseOffset = hit * 0.18f;
                unit.legForwardScl = 0.72f;
                unit.legMoveSpace = 1.3f;
                unit.hovering = true;
                unit.shadowElevation = 0.22f;
                unit.groundLayer = Layer.legUnit;
            }
            default -> {
                unit.constructor = UnitEntity::create;
                unit.speed = Math.max(0.38f, (line == Line.GROUND_ATTACK ? 0.76f : 0.70f) - tier * 0.022f);
                unit.drag = 0.10f;
                unit.accel = 0.13f;
                unit.rotateSpeed = Math.max(1.2f, 4.0f - tier * 0.14f);
            }
        }

        unit.abilities.add(new StateVisualAbility(), new CombatStateVisualAbility());
        return unit;
    }

    public static float damage(int tier, float base) {
        return base * damageScale[tier];
    }

    public static float range(int tier, float base) {
        return base + tier * 11f;
    }

    public static String region(String id) {
        return Vars.content.transformName(id);
    }

    private static Weapon weapon(UnitType unit, String name, BulletType bullet, float x, float y, float reload, boolean mirror) {
        Weapon w = new Weapon(region(name));
        w.bullet = bullet;

        // x/y are the actual turret hardpoint on the hull. shootY is the muzzle offset
        // from the center of the weapon sprite. Large 4x unit sprites need both values;
        // otherwise projectiles visually look like they come from the unit center.
        w.x = x;
        w.y = y;
        w.shootX = 0f;
        w.shootY = Math.max(4f, unit.hitSize * muzzleFraction(bullet));

        w.reload = reload;
        w.recoil = Math.max(0.8f, reload / 22f);
        w.rotate = true;
        w.rotateSpeed = 4.5f;
        w.mirror = mirror;
        w.top = true;
        w.layerOffset = 0.02f;

        raiseBulletLayer(bullet);

        // Mindustry's continuous weapon path repositions mount.bullet every tick.
        // This keeps continuous/point beams attached to the moving/rotating muzzle.
        if (bullet instanceof ContinuousLaserBulletType || bullet instanceof PointLaserBulletType) {
            w.continuous = true;
            w.parentizeEffects = true;
            w.cooldownTime = Math.max(80f, bullet.lifetime + 20f);
            w.recoil = 0f;
        }

        return w;
    }

    private static float muzzleFraction(BulletType bullet) {
        if (bullet instanceof RailBulletType || bullet instanceof ArtilleryBulletType) return 0.28f;
        if (bullet instanceof MissileBulletType || bullet instanceof BombBulletType) return 0.18f;
        if (bullet instanceof ContinuousLaserBulletType || bullet instanceof PointLaserBulletType || bullet instanceof LaserBulletType) return 0.22f;
        if (bullet instanceof ShrapnelBulletType || bullet instanceof LiquidBulletType) return 0.16f;
        return 0.20f;
    }

    /** Unit projectiles must render over normal and flying units, not under the aircraft that fired them. */
    private static void raiseBulletLayer(BulletType bullet) {
        raiseBulletLayer(bullet, 0);
    }

    private static void raiseBulletLayer(BulletType bullet, int depth) {
        if (bullet == null || depth > 4) return;
        bullet.layer = Math.max(bullet.layer, Layer.flyingUnit + 0.5f);

        if (bullet.fragBullet != null && bullet.fragBullet != bullet) {
            raiseBulletLayer(bullet.fragBullet, depth + 1);
        }
        if (bullet.intervalBullet != null && bullet.intervalBullet != bullet) {
            raiseBulletLayer(bullet.intervalBullet, depth + 1);
        }
    }

    private static void addRepeated(UnitType unit, String id, BulletType bullet, int count, float reload) {
        count = Math.max(1, count);
        int pairs = count / 2;
        float hit = unit.hitSize;

        for (int i = 0; i < pairs; i++) {
            // Spread mirrored batteries over the actual hull, not around its pivot.
            float x = hit * Math.min(0.58f, 0.36f + i * 0.11f);
            float y = hit * (0.20f - i * 0.16f);
            unit.weapons.add(weapon(unit, id + "-" + (i + 1), bullet, x, y, reload, true));
        }

        if ((count & 1) == 1) {
            // A single main gun sits forward on the centerline.
            unit.weapons.add(weapon(unit, id + "-center", bullet, 0f, hit * 0.34f, reload, false));
        }
    }

    public static void standard(UnitType unit, String id, int tier, Standard style, int count) {
        if (style == Standard.HEAL) {
            healWeapon(unit, id + "-heal", tier, Math.max(1, count));
            return;
        }
        BulletType bullet = standardBullet(style, tier);
        float reload = switch (style) {
            case RAPID, LIGHTNING -> Math.max(5f, 11f - tier * 0.18f);
            case HEAVY, ARTILLERY, RAIL, BOMB -> Math.max(32f, 70f - tier * 1.6f);
            case MISSILE -> Math.max(20f, 42f - tier * 0.8f);
            case FLAK, SHRAPNEL, LIQUID -> Math.max(9f, 24f - tier * 0.45f);
            case CONTINUOUS, POINT -> Math.max(55f, 115f - tier * 2.4f);
            case LASER -> Math.max(28f, 58f - tier * 1.0f);
            default -> Math.max(9f, 25f - tier * 0.45f);
        };
        addRepeated(unit, id + "-" + style.name().toLowerCase(), bullet, count, reload);
    }

    public static BulletType standardBullet(Standard style, int tier) {
        float d = damage(tier, 12f);
        float r = range(tier, 120f);

        BulletType result = switch (style) {
            case RAPID -> {
                BasicBulletType b = new BasicBulletType(6.5f, d * 0.58f);
                b.lifetime = r / b.speed;
                b.width = 5f; b.height = 9f;
                b.pierceArmor = false;
                yield b;
            }
            case CANNON -> {
                BasicBulletType b = new BasicBulletType(5.2f, d);
                b.lifetime = r / b.speed;
                b.width = 8f; b.height = 13f;
                b.splashDamage = d * 0.25f;
                b.splashDamageRadius = 14f + tier;
                yield b;
            }
            case HEAVY -> {
                BasicBulletType b = new BasicBulletType(4.2f, d * 2.2f);
                b.lifetime = range(tier, 170f) / b.speed;
                b.width = 12f; b.height = 20f;
                b.pierce = true; b.pierceCap = 2 + tier / 4;
                b.splashDamage = d;
                b.splashDamageRadius = 22f + tier * 1.4f;
                yield b;
            }
            case MISSILE -> {
                MissileBulletType b = new MissileBulletType(3.3f + tier * 0.05f, d * 1.2f);
                b.lifetime = range(tier, 165f) / b.speed;
                b.homingPower = 0.06f + tier * 0.003f;
                b.homingRange = 80f + tier * 6f;
                b.splashDamage = d * 0.9f;
                b.splashDamageRadius = 22f + tier;
                b.trailColor = Pal.missileYellowBack;
                yield b;
            }
            case RAIL -> {
                RailBulletType b = new RailBulletType();
                b.damage = d * 4.0f;
                b.length = range(tier, 260f);
                b.pierceDamageFactor = 0.72f;
                b.shootEffect = Fx.railShoot;
                b.hitEffect = Fx.railHit;
                b.pierceEffect = Fx.railHit;
                b.pointEffect = Fx.railTrail;
                yield b;
            }
            case FLAME -> {
                BulletType b = new BulletType(4.0f, d * 0.72f);
                b.lifetime = 22f + tier;
                b.pierce = true;
                b.pierceBuilding = true;
                b.status = StatusEffects.burning;
                b.statusDuration = 180f;
                b.hitEffect = Fx.hitFlameSmall;
                b.shootEffect = Fx.shootSmallFlame;
                b.despawnEffect = Fx.none;
                b.hittable = false;
                yield b;
            }
            case FLAK -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.8f, d * 0.45f);
                b.lifetime = range(tier, 150f) / b.speed;
                ProjectileBehaviors.VelocityFuseFlak vf = new ProjectileBehaviors.VelocityFuseFlak();
                vf.blastDamage = d * 1.4f;
                vf.blastRadius = 34f + tier * 1.2f;
                b.behavior(vf);
                b.collides = false;
                b.collidesTiles = false;
                yield b;
            }
            case EMP -> {
                EmpBulletType b = new EmpBulletType();
                b.speed = 4.6f + tier * 0.045f;
                b.damage = d * 0.68f;
                b.lifetime = range(tier, 165f) / b.speed;
                b.radius = 72f + tier * 5f;
                b.powerDamageScl = 1.1f + tier * 0.05f;
                b.powerSclDecrease = Math.max(0.08f, 0.30f - tier * 0.01f);
                b.status = StatusEffects.electrified;
                b.statusDuration = 100f + tier * 8f;
                b.unitDamageScl = 0.65f;
                yield b;
            }
            case LASER -> {
                LaserBulletType b = new LaserBulletType();
                b.damage = d * 2.0f;
                b.length = range(tier, 180f);
                b.width = 12f + tier * 0.8f;
                b.colors = new Color[]{Pal.surge.cpy().a(0.25f), Pal.surge, Color.white};
                yield b;
            }
            case CONTINUOUS -> {
                ContinuousLaserBulletType b = new ContinuousLaserBulletType();
                b.damage = d * 0.55f;
                b.length = range(tier, 200f);
                b.width = 5f + tier * 0.25f;
                b.lifetime = 80f + tier * 4f;
                b.colors = new Color[]{Pal.surge.cpy().a(0.2f), Pal.surge, Color.white};
                yield b;
            }
            case BOMB -> {
                BombBulletType b = new BombBulletType(
                    d * 2.4f,
                    36f + tier * 1.8f,
                    "shell"
                );
                b.lifetime = 34f + tier * 1.2f;
                b.hitEffect = tier >= 8 ? Fx.massiveExplosion : Fx.blastExplosion;
                yield b;
            }
            case SAP -> {
                SapBulletType b = new SapBulletType();
                b.damage = d;
                b.length = range(tier, 80f);
                b.sapStrength = 0.65f;
                b.color = b.hitColor = Pal.sapBullet;
                yield b;
            }
            case MELEE -> {
                BasicBulletType b = new BasicBulletType(5.5f, d * 1.25f);
                b.lifetime = 8f + tier * 0.15f;
                b.width = 10f; b.height = 16f;
                b.knockback = 1.2f;
                yield b;
            }
            case HEAL -> {
                LaserBoltBulletType b = new LaserBoltBulletType(5.2f, d * 0.55f);
                b.lifetime = r / b.speed;
                b.healPercent = 4f + tier * 0.18f;
                b.collidesTeam = true;
                b.backColor = Pal.heal;
                b.frontColor = Color.white;
                yield b;
            }
            case ARTILLERY -> {
                ArtilleryBulletType b = new ArtilleryBulletType(
                    2.6f + tier * 0.035f,
                    d * 1.35f,
                    "shell"
                );
                b.lifetime = range(tier, 210f) / b.speed;
                b.splashDamage = d * 1.65f;
                b.splashDamageRadius = 30f + tier * 1.8f;
                b.knockback = 0.7f + tier * 0.04f;
                yield b;
            }
            case LASER_BOLT -> {
                LaserBoltBulletType b = new LaserBoltBulletType(5.4f + tier * 0.06f, d);
                b.lifetime = range(tier, 155f) / b.speed;
                b.width = 7f + tier * 0.7f;
                b.height = 18f + tier * 1.4f;
                yield b;
            }
            case POINT -> {
                PointLaserBulletType b = new PointLaserBulletType();
                b.damage = d * 0.48f;
                b.lifetime = 70f + tier * 4f;
                b.damageInterval = Math.max(3f, 6f - tier * 0.12f);
                b.beamEffectInterval = 4f;
                b.beamEffectSize = 3.5f + tier * 0.20f;
                yield b;
            }
            case LIGHTNING -> {
                LightningBulletType b = new LightningBulletType();
                b.damage = d * 0.85f;
                b.lightningLength = 10 + tier;
                b.lightningLengthRand = 4 + tier / 2;
                yield b;
            }
            case LIQUID -> {
                LiquidBulletType b = new LiquidBulletType(Liquids.slag);
                b.damage = d * 0.52f;
                b.speed = 3.1f + tier * 0.035f;
                b.lifetime = range(tier, 92f) / b.speed;
                b.puddleSize = 7f + tier * 0.35f;
                b.orbSize = 3.5f + tier * 0.22f;
                b.statusDuration = 150f + tier * 10f;
                yield b;
            }
            case SHRAPNEL -> {
                ShrapnelBulletType b = new ShrapnelBulletType();
                b.damage = d * 1.65f;
                b.length = range(tier, 70f);
                b.width = 18f + tier * 1.6f;
                b.serrations = 6 + tier / 2;
                b.serrationWidth = 4f + tier * 0.18f;
                b.pierceCap = 2 + tier / 4;
                yield b;
            }
        };

        return styleStandard(result, tier, style);
    }

    private static Color tierColor(int tier) {
        return switch (Math.max(0, Math.min(14, tier))) {
            case 0 -> Color.valueOf("d4c7a1");
            case 1 -> Color.valueOf("b8d5ff");
            case 2 -> Color.valueOf("ffbf6f");
            case 3 -> Color.valueOf("ff8b66");
            case 4 -> Color.valueOf("77e69b");
            case 5 -> Color.valueOf("69ddff");
            case 6 -> Color.valueOf("d696ff");
            case 7 -> Color.valueOf("60f0dd");
            case 8 -> Color.valueOf("8f7cff");
            case 9 -> Color.valueOf("ff5e7f");
            case 10 -> Color.valueOf("6ff5ff");
            case 11 -> Color.valueOf("8ba7ff");
            case 12 -> Color.valueOf("ffd86d");
            case 13 -> Color.valueOf("f2d8ff");
            default -> Color.valueOf("ffffff");
        };
    }

    private static Color darkColor(Color color) {
        return color.cpy().mul(0.48f, 0.52f, 0.58f, 1f);
    }

    private static Color specialColor(Special special, int tier) {
        Color base = switch (special) {
            case A01 -> Color.valueOf("ffd08a");
            case A02 -> Color.valueOf("ff6d7a");
            case A03 -> Color.valueOf("72e6ff");
            case A04 -> Color.valueOf("ffc857");
            case A05 -> Color.valueOf("f29cff");
            case A06 -> Color.valueOf("84a9ff");
            case A10 -> Color.valueOf("c78cff");
            case A11 -> Color.valueOf("a5a0ff");
            case A12 -> Color.valueOf("6fefff");
            case A13 -> Color.valueOf("ff79cb");
            case A14 -> Color.valueOf("ff9b62");
            case A15 -> Color.valueOf("d6a06c");
            case A16 -> Color.valueOf("8ee8ff");
            case A17 -> Color.valueOf("a8f08a");
            case A18 -> Color.valueOf("7ec8ff");
            case A19 -> Color.valueOf("b98cff");
            case A20 -> Color.valueOf("fff0a6");
            case A21 -> Color.valueOf("76e676");
            case A22 -> Color.valueOf("7ff0ce");
            case A23 -> Color.valueOf("d9b3ff");
            case A24 -> Color.valueOf("8b70ff");
            case A26 -> Color.valueOf("a9eeff");
        };
        if (tier >= 12) base.lerp(Color.white, 0.18f + (tier - 12) * 0.10f);
        return base;
    }

    private static BulletType applyVisual(BulletType bullet, int tier, Color accent, float sizeScale) {
        raiseBulletLayer(bullet);
        bullet.hitColor = accent;
        bullet.trailColor = accent;
        bullet.lightColor = accent;
        bullet.lightOpacity = tier >= 5 ? 0.72f : 0.35f;
        bullet.lightRadius = 12f + tier * 2.5f;

        if (tier >= 3 && !(bullet instanceof LightningBulletType) && !(bullet instanceof ShrapnelBulletType)) {
            bullet.trailLength = Math.max(bullet.trailLength, 4 + tier);
            bullet.trailWidth = Math.max(bullet.trailWidth, (1.2f + tier * 0.16f) * sizeScale);
            bullet.trailEffect = tier >= 8 ? Fx.colorTrail : bullet.trailEffect;
        }

        if (bullet instanceof BasicBulletType b) {
            b.backColor = darkColor(accent);
            b.frontColor = tier >= 8 ? Color.white : accent.cpy().lerp(Color.white, 0.35f);
            float minW = (5f + tier * 0.85f) * sizeScale;
            float minH = (10f + tier * 1.35f) * sizeScale;
            b.width = Math.max(b.width, minW);
            b.height = Math.max(b.height, minH);
            b.hitSize = Math.max(b.hitSize, 4f + tier * 0.42f);
            if (tier >= 8) {
                b.shrinkX = Math.min(b.shrinkX, 0.3f);
                b.shrinkY = 0f;
            }
        }

        if (bullet instanceof LaserBulletType b) {
            b.colors = new Color[]{
                darkColor(accent).a(0.28f),
                accent.cpy().a(0.72f),
                accent,
                Color.white
            };
            b.width = Math.max(b.width, (10f + tier * 1.4f) * sizeScale);
            b.sideWidth = Math.max(b.sideWidth, 1.1f + tier * 0.08f);
            b.sideLength = Math.max(b.sideLength, 18f + tier * 3f);
        }

        if (bullet instanceof ContinuousLaserBulletType b) {
            b.colors = new Color[]{
                darkColor(accent).a(0.22f),
                accent.cpy().a(0.52f),
                accent,
                Color.white
            };
            b.width = Math.max(b.width, (5f + tier * 0.75f) * sizeScale);
            b.drawSize = Math.max(b.drawSize, b.length * 2.4f);
        }

        if (bullet instanceof PointLaserBulletType b) {
            b.color = accent;
            b.hitColor = accent;
            b.trailColor = accent;
            b.beamEffect = Fx.colorTrail;
            b.beamEffectSize = Math.max(b.beamEffectSize, 3.5f + tier * 0.3f);
        }

        if (bullet instanceof LightningBulletType b) {
            b.lightningColor = accent;
            b.hitColor = accent;
        }

        if (bullet instanceof ShrapnelBulletType b) {
            b.fromColor = accent.cpy().lerp(Color.white, 0.28f);
            b.toColor = darkColor(accent);
            b.lightColor = accent;
            b.lightOpacity = 0.55f;
        }

        if (bullet instanceof SapBulletType b) {
            b.color = b.hitColor = accent;
        }

        if (bullet instanceof RailBulletType b) {
            b.trailColor = accent;
            b.hitColor = accent;
            b.pointEffect = Fx.railTrail;
            b.pointEffectSpace = Math.max(8f, 22f - tier * 0.7f);
            b.lineEffect = tier >= 8 ? Fx.railTrail : b.lineEffect;
        }

        return bullet;
    }

    private static BulletType styleStandard(BulletType bullet, int tier, Standard style) {
        Color accent = tierColor(tier);
        float scale = tier < 4 ? 1f : tier < 8 ? 1.25f : tier < 12 ? 1.55f : 1.9f;

        if (style == Standard.HEAL) accent = Pal.heal.cpy();
        if (style == Standard.SAP) accent = Pal.sapBullet.cpy();
        if (style == Standard.EMP || style == Standard.LIGHTNING) accent = Color.valueOf("72d9ff");
        if (style == Standard.FLAME || style == Standard.LIQUID) accent = Color.valueOf("ff8a4e");

        return applyVisual(bullet, tier, accent, scale);
    }

    private static BulletType styleSpecial(BulletType bullet, int tier, Special special) {
        float scale = tier < 5 ? 1.05f : tier < 8 ? 1.30f : tier < 12 ? 1.65f : 2.0f;
        return applyVisual(bullet, tier, specialColor(special, tier), scale);
    }

    public static BulletType specialBullet(Special special, int tier) {
        float d = damage(tier, 15f);
        float r = range(tier, 150f);

        switch (special) {
            case A01 -> {
                TriangleBulletType frag = new TriangleBulletType(6.5f, d * 0.34f, specialColor(special, tier));
                frag.lifetime = 18f + tier * 0.3f;
                raiseBulletLayer(frag);
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(6.0f, d * 1.15f);
                b.lifetime = r / b.speed;
                b.pierce = true;
                b.pierceCap = 2 + tier / 4;
                ProjectileBehaviors.BackSprayFragment fx = new ProjectileBehaviors.BackSprayFragment(frag);
                fx.fragments = 5 + tier / 3;
                fx.cone = 54f;
                b.behavior(fx);
                return styleSpecial(b, tier, special);
            }
            case A02 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(4.4f, d * 0.55f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.StickyExplosive sticky = new ProjectileBehaviors.StickyExplosive();
                sticky.delay = 75f;
                sticky.radius = 32f + tier * 1.4f;
                sticky.damage = d * 2.3f;
                b.behavior(sticky);
                return styleSpecial(b, tier, special);
            }
            case A03 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(7.0f, d);
                b.lifetime = r / b.speed;
                b.behavior(new ProjectileBehaviors.InertiaShot());
                return styleSpecial(b, tier, special);
            }
            case A04 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(4.6f, d * 1.25f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.MassImpact mass = new ProjectileBehaviors.MassImpact();
                mass.extraPerSize = 1.2f + tier * 0.10f;
                mass.impulse = 7f + tier * 0.4f;
                b.behavior(mass);
                return styleSpecial(b, tier, special);
            }
            case A05 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.0f, d * 0.32f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.TractorLance t = new ProjectileBehaviors.TractorLance();
                t.duration = 55f + tier * 2f;
                t.pull = 0.15f + tier * 0.008f;
                b.behavior(t);
                return styleSpecial(b, tier, special);
            }
            case A06 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.4f, d * 0.48f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.DisplacementShot ds = new ProjectileBehaviors.DisplacementShot();
                ds.impulse = 9f + tier * 0.45f;
                b.behavior(ds);
                return styleSpecial(b, tier, special);
            }
            case A10 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.2f, d * 0.50f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.CuttingAnchor a = new ProjectileBehaviors.CuttingAnchor();
                a.duration = 150f + tier * 6f;
                a.lineDamage = d * 0.70f;
                a.lineWidth = 6f + tier * 0.25f;
                b.behavior(a);
                return styleSpecial(b, tier, special);
            }
            case A11 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(3.4f, d * 1.15f);
                b.lifetime = range(tier, 210f) / b.speed;
                ProjectileBehaviors.PhaseProjectile p = new ProjectileBehaviors.PhaseProjectile();
                p.triggerDistance = 95f + tier * 4f;
                p.phaseDistance = 60f + tier * 3f;
                b.behavior(p);
                return styleSpecial(b, tier, special);
            }
            case A12 -> {
                BasicBulletType child = new BasicBulletType(6.5f, d * 0.42f);
                child.lifetime = 22f;
                child.pierce = true;
                if (tier >= 8) {
                    child.lightning = 2 + tier / 6;
                    child.lightningDamage = d * 0.16f;
                    child.lightningLength = 5 + tier / 2;
                    child.lightningLengthRand = 3 + tier / 3;
                    child.lightningColor = specialColor(special, tier);
                }
                applyVisual(child, tier, specialColor(special, tier), tier >= 8 ? 1.35f : 1f);

                BehaviorBasicBulletType b = new BehaviorBasicBulletType(4.2f, d * 0.35f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.CollapsingRing ring = new ProjectileBehaviors.CollapsingRing(child);
                ring.count = 8 + tier / 2;
                ring.radius = 38f + tier * 1.7f;
                b.behavior(ring);
                return styleSpecial(b, tier, special);
            }
            case A13 -> {
                TriangleBulletType frag = new TriangleBulletType(6.2f, d * 0.28f, specialColor(special, tier));
                frag.lifetime = 22f;
                raiseBulletLayer(frag);
                if (tier >= 10) {
                    frag.lightning = 1 + tier / 7;
                    frag.lightningDamage = d * 0.14f;
                    frag.lightningLength = 5 + tier / 2;
                    frag.lightningLengthRand = 3;
                    frag.lightningColor = specialColor(special, tier);
                }

                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.4f, d * 0.75f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.FractureStack f = new ProjectileBehaviors.FractureStack();
                f.threshold = 4;
                f.burstDamage = d * 2.2f;
                f.burstRadius = 38f + tier * 1.2f;
                f.fragment = frag;
                f.fragments = 8 + tier / 3;
                b.behavior(f);
                return styleSpecial(b, tier, special);
            }
            case A14 -> {
                BehaviorArtilleryBulletType b = new BehaviorArtilleryBulletType(3.0f, d * 0.65f);
                b.lifetime = range(tier, 190f) / b.speed;
                b.splashDamage = d * 0.55f;
                b.splashDamageRadius = 22f + tier;
                ProjectileBehaviors.DelayedFuse f = new ProjectileBehaviors.DelayedFuse();
                f.delay = 36f + tier;
                f.blastDamage = d * 2.0f;
                f.blastRadius = 40f + tier * 1.4f;
                b.behavior(f);
                return styleSpecial(b, tier, special);
            }
            case A15 -> {
                BehaviorArtilleryBulletType b = new BehaviorArtilleryBulletType(2.8f, d * 0.65f);
                b.lifetime = range(tier, 200f) / b.speed;
                ProjectileBehaviors.SubsurfaceShockwave s = new ProjectileBehaviors.SubsurfaceShockwave();
                s.arms = 4 + tier / 4;
                s.eruptionsPerArm = 3 + tier / 5;
                s.damage = d * 0.48f;
                s.radius = 22f + tier;
                s.knockback = 2.0f + tier * 0.12f;
                b.behavior(s);
                return styleSpecial(b, tier, special);
            }
            case A16 -> {
                TriangleBulletType child = new TriangleBulletType(6.2f, d * 0.42f, specialColor(special, tier));
                child.lifetime = 32f + tier;
                child.homingPower = 0.05f;
                child.homingRange = 90f + tier * 4f;
                raiseBulletLayer(child);

                if (tier >= 7) {
                    child.lightning = 1 + tier / 6;
                    child.lightningDamage = d * 0.20f;
                    child.lightningLength = 6 + tier / 2;
                    child.lightningLengthRand = 4;
                    child.lightningColor = specialColor(special, tier);
                }

                if (tier >= 13) {
                    LaserBoltBulletType terminalBolt = new LaserBoltBulletType(5.8f, d * 0.20f);
                    terminalBolt.lifetime = 18f + tier;
                    terminalBolt.backColor = darkColor(specialColor(special, tier));
                    terminalBolt.frontColor = Color.white;
                    raiseBulletLayer(terminalBolt);
                    child.fragBullet = terminalBolt;
                    child.fragBullets = 2;
                    child.fragSpread = 24f;
                    child.fragRandomSpread = 8f;
                }

                BehaviorBasicBulletType b = new BehaviorBasicBulletType(3.4f, d * 0.50f);
                b.lifetime = range(tier, 220f) / b.speed;
                b.homingPower = 0.035f;
                b.homingRange = 100f;
                ProjectileBehaviors.TopAttackSplit split = new ProjectileBehaviors.TopAttackSplit(child);
                split.count = 4 + tier / 4;
                split.spread = 24f;
                b.behavior(split);
                return styleSpecial(b, tier, special);
            }
            case A17 -> {
                BasicBulletType decoy = new BasicBulletType(3.6f, 0f);
                decoy.lifetime = 55f;
                decoy.hittable = true;
                decoy.absorbable = true;
                decoy.collides = false;
                decoy.collidesTiles = false;
                raiseBulletLayer(decoy);

                BehaviorBasicBulletType b = new BehaviorBasicBulletType(3.7f, d * 0.85f);
                b.lifetime = range(tier, 200f) / b.speed;
                b.homingPower = 0.06f;
                b.homingRange = 110f;
                ProjectileBehaviors.DecoySalvo decoys = new ProjectileBehaviors.DecoySalvo(decoy);
                decoys.count = 3 + tier / 5;
                b.behavior(decoys);
                return styleSpecial(b, tier, special);
            }
            case A18 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.8f, d * 0.55f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.ChargedChainLightning c = new ProjectileBehaviors.ChargedChainLightning();
                c.jumps = 3 + tier / 4;
                c.range = 70f + tier * 4f;
                c.firstDamage = d * 0.45f;
                c.growth = 1.18f + tier * 0.006f;
                b.behavior(c);
                return styleSpecial(b, tier, special);
            }
            case A19 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.0f, d * 0.55f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.ShieldDrain s = new ProjectileBehaviors.ShieldDrain();
                s.drain = 90f + tier * 22f;
                s.ownerShieldCap = 600f + tier * 350f;
                b.behavior(s);
                return styleSpecial(b, tier, special);
            }
            case A20 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(7.0f, d * 0.85f);
                b.lifetime = range(tier, 210f) / b.speed;
                ProjectileBehaviors.AblationBeamHit a = new ProjectileBehaviors.AblationBeamHit();
                a.maxStacks = 4 + tier / 5;
                a.armorPerStack = 1.5f + tier * 0.18f;
                b.behavior(a);
                return styleSpecial(b, tier, special);
            }
            case A21 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(4.5f, d * 0.60f);
                b.lifetime = r / b.speed;
                ProjectileBehaviors.AdhesiveCorrosion a = new ProjectileBehaviors.AdhesiveCorrosion();
                a.duration = 180f + tier * 12f;
                b.status = StatusEffects.corroded;
                b.statusDuration = a.duration;
                b.behavior(a);
                return styleSpecial(b, tier, special);
            }
            case A22 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(5.5f, d * 0.95f);
                b.lifetime = range(tier, 220f) / b.speed;
                b.collidesTiles = false;
                ProjectileBehaviors.LimitedRicochet rch = new ProjectileBehaviors.LimitedRicochet();
                rch.maxBounces = 2;
                rch.velocityRetain = 0.80f;
                b.behavior(rch);
                return styleSpecial(b, tier, special);
            }
            case A23 -> {
                LaserBoltBulletType side = new LaserBoltBulletType(6.5f, d * 0.35f);
                side.lifetime = 28f;
                side.pierce = true;
                if (tier >= 10) {
                    side.lightning = 1 + tier / 8;
                    side.lightningDamage = d * 0.12f;
                    side.lightningLength = 4 + tier / 3;
                    side.lightningColor = specialColor(special, tier);
                }
                applyVisual(side, tier, specialColor(special, tier), tier >= 8 ? 1.45f : 1.1f);

                BehaviorBasicBulletType b = new BehaviorBasicBulletType(3.0f, d * 0.35f);
                b.lifetime = range(tier, 230f) / b.speed;
                b.pierce = true;
                b.pierceCap = 20;
                ProjectileBehaviors.MovingPrism p = new ProjectileBehaviors.MovingPrism(side);
                p.interval = Math.max(6f, 12f - tier * 0.25f);
                b.behavior(p);
                return styleSpecial(b, tier, special);
            }
            case A24 -> {
                BehaviorBasicBulletType b = new BehaviorBasicBulletType(2.4f, d * 0.25f);
                b.lifetime = 95f + tier * 2f;
                b.collides = false;
                b.collidesTiles = false;
                ProjectileBehaviors.GravityCore g = new ProjectileBehaviors.GravityCore();
                g.pullRange = 90f + tier * 5f;
                g.pull = 0.48f + tier * 0.025f;
                g.collapseDamage = d * 2.0f;
                g.collapseRadius = 48f + tier * 1.6f;
                b.behavior(g);
                return styleSpecial(b, tier, special);
            }
            case A26 -> {
                return styleSpecial(standardBullet(Standard.FLAK, tier), tier, special);
            }
        }
        return standardBullet(Standard.CANNON, tier);
    }

    public static void special(UnitType unit, String id, int tier, Special special, int count) {
        BulletType bullet = specialBullet(special, tier);
        float reload = switch (special) {
            case A02, A12, A14, A15, A24 -> Math.max(35f, 72f - tier * 1.8f);
            case A16, A17 -> Math.max(20f, 46f - tier);
            case A05, A06, A18, A19, A21, A26 -> Math.max(10f, 28f - tier * 0.55f);
            default -> Math.max(12f, 34f - tier * 0.65f);
        };
        addRepeated(unit, id + "-" + special.name().toLowerCase(), bullet, count, reload);
    }

    public static void reactionPair(UnitType unit, String id, int tier, int primerCount, int detonatorCount) {
        float d = damage(tier, 15f);

        BehaviorBasicBulletType primer = new BehaviorBasicBulletType(5.0f, d * 0.28f);
        primer.lifetime = range(tier, 135f) / primer.speed;
        ProjectileBehaviors.ReactionPrimer rp = new ProjectileBehaviors.ReactionPrimer();
        rp.max = 4;
        rp.duration = 260f;
        rp.debuffDuration = 130f;
        primer.behavior(rp);
        raiseBulletLayer(primer);

        BehaviorBasicBulletType detonator = new BehaviorBasicBulletType(4.3f, d * 0.45f);
        detonator.lifetime = range(tier, 150f) / detonator.speed;
        ProjectileBehaviors.ReactionDetonator rd = new ProjectileBehaviors.ReactionDetonator();
        rd.baseDamage = d * 0.7f;
        rd.damagePerStack = d * 0.85f;
        rd.triggerRadius = 58f + tier * 2f;
        detonator.behavior(rd);
        raiseBulletLayer(detonator);

        addRepeated(unit, id + "-primer", primer, primerCount, Math.max(9f, 25f - tier * 0.45f));
        addRepeated(unit, id + "-detonator", detonator, detonatorCount, Math.max(28f, 60f - tier * 1.2f));
    }

    public static void modeWeapon(UnitType unit, String id, int tier, String[] keys, BulletType... bullets) {
        ModeWeapon w = new ModeWeapon(region(id + "-weapon"));
        w.modes(bullets);
        for (BulletType modeBullet : bullets) {
            raiseBulletLayer(modeBullet);
        }
        w.x = unit.hitSize * 0.38f;
        w.y = unit.hitSize * 0.18f;
        w.shootX = 0f;
        w.shootY = unit.hitSize * 0.24f;
        w.reload = Math.max(12f, 34f - tier * 0.55f);
        w.rotate = true;
        w.rotateSpeed = 3.8f;
        w.mirror = true;

        // If a flying target exists, prefer the last mode for AA-oriented sets.
        w.aiSelector = (u, mount) -> {
            if (mount.target instanceof mindustry.gen.Unit target && target.isFlying() && bullets.length >= 3) {
                return bullets.length - 1;
            }
            return 0;
        };

        unit.weapons.add(w);
        unit.abilities.add(new WeaponModeAbility(keys));
    }

    /**
     * A mixed battery is deliberately implemented as multiple independent Weapon objects.
     * No Weapon in this method fires more than one BulletType.
     */
    public static void mixedBattery(UnitType unit, String id, int tier, int scale) {
        float hit = unit.hitSize;
        int pairs = Math.max(1, scale);

        // Laser-bolt battery.
        for (int i = 0; i < pairs; i++) {
            Weapon bolt = weapon(
                unit,
                id + "-bolt-" + (i + 1),
                standardBullet(Standard.LASER_BOLT, tier),
                hit * (0.34f + i * 0.075f),
                hit * (0.22f - i * 0.10f),
                Math.max(12f, 28f - tier * 0.45f),
                true
            );
            unit.weapons.add(bolt);
        }

        // Arc battery on a separate mount and a separate reload cycle.
        for (int i = 0; i < pairs; i++) {
            Weapon arc = weapon(
                unit,
                id + "-arc-" + (i + 1),
                standardBullet(Standard.LIGHTNING, tier),
                hit * (0.46f + i * 0.060f),
                -hit * (0.02f + i * 0.09f),
                Math.max(18f, 38f - tier * 0.55f),
                true
            );
            unit.weapons.add(arc);
        }

        // UXV+ batteries also gain a single central kinetic/rail-style gun.
        if (tier >= 12) {
            Weapon kinetic = weapon(
                unit,
                id + "-kinetic-center",
                standardBullet(Standard.HEAVY, tier),
                0f,
                hit * 0.36f,
                Math.max(34f, 62f - tier * 1.0f),
                false
            );
            unit.weapons.add(kinetic);
        }
    }

    public static void healWeapon(UnitType unit, String id, int tier, int count) {
        LaserBoltBulletType b = (LaserBoltBulletType)standardBullet(Standard.HEAL, tier);
        addRepeated(unit, id, b, count, Math.max(13f, 28f - tier * 0.5f));
    }

    public static void repairBeam(UnitType unit, String id, int tier, int count) {
        int pairs = count / 2;
        for (int i = 0; i < pairs; i++) {
            RepairBeamWeapon w = new RepairBeamWeapon(region(id + "-" + (i + 1)));
            w.x = unit.hitSize * (0.36f + i * 0.08f);
            w.y = unit.hitSize * (0.08f - i * 0.10f);
            w.shootY = Math.max(4f, unit.hitSize * 0.18f);
            w.mirror = true;
            w.repairSpeed = 0.55f + tier * 0.09f;
            w.beamWidth = 0.7f + tier * 0.02f;
            w.bullet = new BulletType();
            w.bullet.maxRange = range(tier, 95f);
            unit.weapons.add(w);
        }
        if ((count & 1) == 1) {
            RepairBeamWeapon w = new RepairBeamWeapon(region(id + "-center"));
            w.x = 0f;
            w.y = unit.hitSize * 0.22f;
            w.mirror = false;
            w.repairSpeed = 0.55f + tier * 0.09f;
            w.beamWidth = 0.7f + tier * 0.02f;
            w.bullet = new BulletType();
            w.bullet.maxRange = range(tier, 95f);
            unit.weapons.add(w);
        }
    }

    public static void pointDefense(UnitType unit, String id, int tier, int count) {
        int pairs = count / 2;
        for (int i = 0; i < pairs; i++) {
            PointDefenseWeapon w = new PointDefenseWeapon(region(id + "-" + (i + 1)));
            w.x = unit.hitSize * (0.40f + i * 0.07f);
            w.y = unit.hitSize * (0.10f - i * 0.10f);
            w.mirror = true;
            w.reload = Math.max(4f, 10f - tier * 0.25f);
            w.targetInterval = 8f;
            w.targetSwitchInterval = 10f;
            w.bullet = new BulletType();
            w.bullet.maxRange = range(tier, 85f);
            w.bullet.damage = damage(tier, 8f);
            w.bullet.shootEffect = Fx.sparkShoot;
            w.bullet.hitEffect = Fx.pointHit;
            unit.weapons.add(w);
        }
        if ((count & 1) == 1) {
            PointDefenseWeapon w = new PointDefenseWeapon(region(id + "-center"));
            w.x = 0f;
            w.y = unit.hitSize * 0.08f;
            w.mirror = false;
            w.reload = Math.max(4f, 10f - tier * 0.25f);
            w.targetInterval = 8f;
            w.targetSwitchInterval = 10f;
            w.bullet = new BulletType();
            w.bullet.maxRange = range(tier, 85f);
            w.bullet.damage = damage(tier, 8f);
            w.bullet.shootEffect = Fx.sparkShoot;
            w.bullet.hitEffect = Fx.pointHit;
            unit.weapons.add(w);
        }
    }

    public static void droneBay(UnitType unit, String id, UnitType drone, int tier, int max, float x, float y) {
        DroneBayWeapon bay = new DroneBayWeapon(region(id), drone);
        bay.maxDrones = max;
        bay.spawnTime = Math.max(80f, 190f - tier * 6f);
        bay.spawnDistance = Math.max(14f, unit.hitSize * 0.32f);
        bay.x = x;
        bay.y = y;
        unit.weapons.add(bay);

        boolean hasCounter = false;
        for (var ability : unit.abilities) {
            if (ability instanceof DroneBayStatusAbility) {
                hasCounter = true;
                break;
            }
        }
        if (!hasCounter) unit.abilities.add(new DroneBayStatusAbility());
    }

    public static void addBaseAbilities(UnitType unit, int tier, String... codes) {
        for (String code : codes) {
            switch (code) {
                case "B01" -> {
                    MechanicAbilities.DeployMechanic a = new MechanicAbilities.DeployMechanic();
                    a.deployTime = Math.max(50f, 90f - tier * 2f);
                    unit.abilities.add(a);
                }
                case "B02" -> {
                    MechanicAbilities.HeatMechanic a = new MechanicAbilities.HeatMechanic();
                    a.heatPerShot = Math.max(0.020f, 0.045f - tier * 0.0012f);
                    a.coolPerTick = 0.0030f + tier * 0.00008f;
                    unit.abilities.add(a);
                }
                case "B03" -> {
                    MechanicAbilities.FacingArmor a = new MechanicAbilities.FacingArmor();
                    a.frontMultiplier = Math.max(0.48f, 0.72f - tier * 0.014f);
                    a.rearMultiplier = 1.18f + tier * 0.008f;
                    unit.abilities.add(a);
                }
                case "B04" -> {
                    MechanicAbilities.RecoilAnchor a = new MechanicAbilities.RecoilAnchor();
                    a.velocityRetain = 0.55f;
                    a.lockTime = 7f + tier * 0.4f;
                    unit.abilities.add(a);
                }
                case "B06" -> {
                    MechanicAbilities.DamageGate a = new MechanicAbilities.DamageGate();
                    a.thresholdFraction = Math.max(0.07f, 0.14f - tier * 0.004f);
                    a.excessMultiplier = 0.48f;
                    unit.abilities.add(a);
                }
                case "B07" -> {
                    MechanicAbilities.AblativeArmor a = new MechanicAbilities.AblativeArmor();
                    a.maxPlates = 3 + tier / 2;
                    a.triggerDamageFraction = Math.max(0.035f, 0.08f - tier * 0.0025f);
                    a.absorbedFraction = 0.52f + Math.min(0.18f, tier * 0.01f);
                    unit.abilities.add(a);
                }
                case "B08" -> {
                    MechanicAbilities.AdaptiveArmor a = new MechanicAbilities.AdaptiveArmor();
                    a.maxStacks = 2 + tier / 5;
                    a.reductionPerStack = 0.06f + tier * 0.002f;
                    unit.abilities.add(a);
                }
                case "B09" -> {
                    MechanicAbilities.Capacitor a = new MechanicAbilities.Capacitor();
                    a.chargePerShot = Math.max(0.025f, 0.06f - tier * 0.0015f);
                    a.shieldGain = 220f + tier * 95f;
                    a.shieldCap = 550f + tier * 380f;
                    unit.abilities.add(a);
                }
                case "B10" -> unit.abilities.add(new MechanicAbilities.Momentum());
                case "B11" -> {
                    MechanicAbilities.PhaseBlink a = new MechanicAbilities.PhaseBlink();
                    a.distance = 55f + tier * 3f;
                    a.threatRange = 130f + tier * 5f;
                    unit.abilities.add(a);
                }
                case "B13" -> unit.abilities.add(new MechanicAbilities.LastStand());
                case "B14" -> unit.abilities.add(new MechanicAbilities.CounterBattery());
                case "B15" -> {
                    MechanicAbilities.BurstDrive a = new MechanicAbilities.BurstDrive();
                    a.impulse = 3.0f + tier * 0.16f;
                    a.reload = Math.max(80f, 145f - tier * 4f);
                    unit.abilities.add(a);
                }
            }
        }
    }

    public static void addSupportAbilities(UnitType unit, int tier, UnitType decoy, UnitType maintenanceDrone, String... codes) {
        for (String code : codes) {
            switch (code) {
                case "S01" -> {
                    SupportAbilities.FireControlLink a = new SupportAbilities.FireControlLink();
                    a.linkRange = range(tier, 120f);
                    a.targetRange = range(tier, 210f);
                    unit.abilities.add(a);
                }
                case "S02" -> {
                    SupportAbilities.CounterBatteryMark a = new SupportAbilities.CounterBatteryMark();
                    a.range = range(tier, 170f);
                    unit.abilities.add(a);
                }
                case "S03" -> {
                    SupportAbilities.HeatTransfer a = new SupportAbilities.HeatTransfer();
                    a.range = range(tier, 100f);
                    unit.abilities.add(a);
                }
                case "S04" -> {
                    SupportAbilities.CapacitorTransfer a = new SupportAbilities.CapacitorTransfer();
                    a.range = range(tier, 100f);
                    unit.abilities.add(a);
                }
                case "S05" -> {
                    SupportAbilities.ReloadService a = new SupportAbilities.ReloadService();
                    a.range = range(tier, 95f);
                    unit.abilities.add(a);
                }
                case "S06" -> {
                    SupportAbilities.StatusCleanse a = new SupportAbilities.StatusCleanse();
                    a.range = range(tier, 95f);
                    unit.abilities.add(a);
                }
                case "S07" -> {
                    SupportAbilities.RescueTractor a = new SupportAbilities.RescueTractor();
                    a.range = range(tier, 100f);
                    a.pull = 0.055f;
                    a.healFractionPerTick = 0.00028f + tier * 0.000006f;
                    unit.abilities.add(a);
                }
                case "S08" -> {
                    SupportAbilities.VectorAssist a = new SupportAbilities.VectorAssist();
                    a.range = range(tier, 85f);
                    unit.abilities.add(a);
                }
                case "S09" -> {
                    SupportAbilities.ElectronicSuppression a = new SupportAbilities.ElectronicSuppression();
                    a.range = range(tier, 105f);
                    unit.abilities.add(a);
                }
                case "S10" -> {
                    if (decoy != null) {
                        SupportAbilities.DecoyChaff a = new SupportAbilities.DecoyChaff(decoy);
                        a.count = 2 + tier / 5;
                        a.detectRange = range(tier, 70f);
                        unit.abilities.add(a);
                    }
                }
                case "S11" -> {
                    SupportAbilities.DeflectionWedge a = new SupportAbilities.DeflectionWedge();
                    a.range = range(tier, 58f);
                    a.maxBulletDamage = 120f + tier * 28f;
                    unit.abilities.add(a);
                }
                case "S12" -> {
                    SupportAbilities.DamageRedirect a = new SupportAbilities.DamageRedirect();
                    a.range = range(tier, 78f);
                    a.maxPerSecond = 180f + tier * 70f;
                    unit.abilities.add(a);
                }
                case "S13" -> {
                    SupportAbilities.StabilizationField a = new SupportAbilities.StabilizationField();
                    a.range = range(tier, 78f);
                    unit.abilities.add(a);
                }
                case "S14" -> {
                    SupportAbilities.PhaseCorridor a = new SupportAbilities.PhaseCorridor();
                    a.length = range(tier, 125f);
                    a.halfWidth = 24f + tier * 1.2f;
                    unit.abilities.add(a);
                }
                case "S15" -> {
                    SupportAbilities.TargetDesignation a = new SupportAbilities.TargetDesignation();
                    a.range = range(tier, 170f);
                    unit.abilities.add(a);
                }
                case "S16" -> {
                    if (maintenanceDrone != null) {
                        SupportAbilities.DroneMaintenance a = new SupportAbilities.DroneMaintenance(maintenanceDrone);
                        a.range = range(tier, 70f);
                        a.healPerTick = 2.5f + tier * 0.35f;
                        unit.abilities.add(a);
                    }
                }
                case "S17" -> {
                    SupportAbilities.BuildAssist a = new SupportAbilities.BuildAssist();
                    a.range = range(tier, 90f);
                    unit.abilities.add(a);
                }
                case "S19" -> {
                    SupportAbilities.FormationCoordination a = new SupportAbilities.FormationCoordination();
                    a.range = range(tier, 70f);
                    unit.abilities.add(a);
                }
                case "S20" -> {
                    SupportAbilities.ThreatWarning a = new SupportAbilities.ThreatWarning();
                    a.range = range(tier, 95f);
                    unit.abilities.add(a);
                }
            }
        }
    }

    public static void addVanilla(UnitType unit, int tier, String... tags) {
        for (String tag : tags) {
            switch (tag) {
                case "ENERGY_FIELD" -> {
                    EnergyFieldAbility a = new EnergyFieldAbility(
                        damage(tier, 2.2f),
                        Math.max(32f, 72f - tier * 1.6f),
                        range(tier, 58f)
                    );
                    a.healPercent = tier >= 9 ? 0.8f + tier * 0.08f : 0f;
                    a.displayHeal = a.healPercent > 0f;
                    a.maxTargets = 3 + tier / 3;
                    a.status = StatusEffects.electrified;
                    a.statusDuration = 50f + tier * 4f;
                    a.color = tierColor(tier);
                    unit.abilities.add(a);
                }
                case "FORCE" -> unit.abilities.add(new ForceFieldAbility(
                    48f + tier * 4f,
                    0.35f + tier * 0.08f,
                    350f + tier * 360f,
                    60f * 5f
                ));
                case "SHIELD_ARC" -> {
                    ShieldArcAbility a = new ShieldArcAbility();
                    a.radius = 42f + tier * 3.5f;
                    a.max = 280f + tier * 300f;
                    a.regen = 0.30f + tier * 0.06f;
                    a.cooldown = 60f * 5f;
                    a.angle = 105f;
                    a.whenShooting = false;
                    unit.abilities.add(a);
                }
                case "SHIELD_REGEN" -> unit.abilities.add(new ShieldRegenFieldAbility(
                    8f + tier * 1.8f,
                    80f + tier * 25f,
                    60f * 4f,
                    60f + tier * 4f
                ));
                case "REPAIR_FIELD" -> unit.abilities.add(new RepairFieldAbility(
                    6f + tier * 2.2f,
                    60f * 4f,
                    55f + tier * 5f
                ));
                case "OVERCLOCK" -> unit.abilities.add(new StatusFieldAbility(
                    StatusEffects.overclock,
                    90f,
                    60f * 6f,
                    60f + tier * 4f
                ));
                case "SUPPRESSION" -> {
                    SuppressionFieldAbility a = new SuppressionFieldAbility();
                    a.range = 120f + tier * 8f;
                    a.reload = 60f * 9f;
                    unit.abilities.add(a);
                }
            }
        }
    }

    public static void addAdvanced(UnitType unit, int tier, String... tags) {
        for (String tag : tags) {
            switch (tag) {
                case "EMERGENCY_REPAIR" -> {
                    AdvancedAbilities.EmergencyRepair a = new AdvancedAbilities.EmergencyRepair();
                    a.healFraction = 0.14f + tier * 0.006f;
                    unit.abilities.add(a);
                }
                case "INTERCEPT" -> {
                    AdvancedAbilities.InterceptMatrix a = new AdvancedAbilities.InterceptMatrix();
                    a.range = range(tier, 95f);
                    a.maxPerScan = 1 + tier / 5;
                    a.maxDamage = 220f + tier * 55f;
                    unit.abilities.add(a);
                }
                case "BARRIER" -> {
                    AdvancedAbilities.BarrierPulse a = new AdvancedAbilities.BarrierPulse();
                    a.range = range(tier, 60f);
                    unit.abilities.add(a);
                }
                case "GRAVITY" -> {
                    AdvancedAbilities.GravityField a = new AdvancedAbilities.GravityField();
                    a.range = range(tier, 75f);
                    a.pull = 0.10f + tier * 0.006f;
                    unit.abilities.add(a);
                }
                case "MARK" -> {
                    AdvancedAbilities.TargetMark a = new AdvancedAbilities.TargetMark();
                    a.range = range(tier, 180f);
                    unit.abilities.add(a);
                }
                case "LOCK" -> {
                    AdvancedAbilities.LockOn a = new AdvancedAbilities.LockOn();
                    a.fullLockTime = Math.max(70f, 170f - tier * 5f);
                    unit.abilities.add(a);
                }
                case "EXECUTION" -> unit.abilities.add(new AdvancedAbilities.Execution());
            }
        }
    }

    public static void configureUtility(UnitType unit, String id, Line line, int tier, String description) {
        if (line == Line.AIR_SUPPORT) {
            if (id.endsWith("worker") || id.endsWith("finch")) {
                unit.mineTier = Math.min(4, 1 + tier / 2);
                unit.mineSpeed = 2.5f + tier * 0.55f;
                unit.itemCapacity = 30 + tier * 10;
                unit.defaultCommand = UnitCommand.mineCommand;
            }
            if (id.endsWith("tinker") || id.endsWith("swarm")) {
                unit.buildSpeed = 0.6f + tier * 0.25f;
                unit.defaultCommand = UnitCommand.rebuildCommand;
            }
            if (id.endsWith("mender") || id.endsWith("mirror") || id.endsWith("seraph")) {
                unit.defaultCommand = UnitCommand.repairCommand;
            }
        }

        if (line == Line.CRAWLER && (id.endsWith("scarab") || id.endsWith("mite"))) {
            unit.aiController = SuicideAI::new;
        }

        if (line == Line.AIR_ATTACK && (
            id.endsWith("dive") || id.endsWith("bomber") || id.endsWith("eclipse") || id.endsWith("apocalypse")
        )) {
            unit.circleTarget = true;
            unit.circleTargetRadius = range(tier, 70f);
        }
    }

    public static void finish(UnitType unit) {
        // Intentionally keep UnitType.controller untouched.
        // Player-controlled units therefore use vanilla CommandAI; autonomous units use UnitType.aiController.
    }

    private ModUnitFactory() {}
}
