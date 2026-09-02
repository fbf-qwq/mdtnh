package mdtnh.units;

import arc.Core;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BombBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;

/**
 * Extra attack families introduced in Phase 4.7.
 *
 * One direct Weapon still owns exactly one direct BulletType:
 *
 * airDropWeapon:
 *   Weapon -> AirstrikeBombBulletType
 *
 * bombardmentWeapon:
 *   Weapon -> BombardmentMarkerBulletType
 *             -> later spawns repeated instances of ONE AirstrikeBombBulletType
 *
 * No MultiBulletType is used.
 */
public final class MdtAttackProjectiles {

    private MdtAttackProjectiles(){}

    public static Weapon airDropWeapon(
            String name,
            int tier,
            float hitSize,
            float damage
    ){
        AirstrikeBombBulletType bomb = airstrikeBomb(tier, damage);

        Weapon weapon = new Weapon(name);
        weapon.mirror = false;
        weapon.rotate = false;
        weapon.x = 0f;
        weapon.y = -Math.max(2f, hitSize * 0.16f);
        weapon.shootY = -Math.max(1f, hitSize * 0.05f);
        weapon.reload = Math.max(34f, 82f - tier * 2.8f);
        weapon.recoil = 0f;
        weapon.inaccuracy = 7f + Math.max(0, tier - 8) * 0.5f;
        weapon.shootCone = 180f;
        weapon.ejectEffect = Fx.none;
        weapon.bullet = bomb;
        return weapon;
    }

    public static Weapon bombardmentWeapon(
            String name,
            int tier,
            float hitSize,
            float damage,
            float range
    ){
        AirstrikeBombBulletType bomb = airstrikeBomb(tier, damage);

        BombardmentMarkerBulletType marker = new BombardmentMarkerBulletType();
        marker.speed = 7.5f + tier * 0.12f;
        marker.damage = 0f;
        marker.lifetime = range / marker.speed;
        marker.maxRange = range;
        marker.scaleLife = true;

        marker.width = 7f + tier * 0.45f;
        marker.height = 12f + tier * 0.55f;
        marker.shrinkX = 0.25f;
        marker.shrinkY = 0.15f;
        marker.backColor = tier >= MdtProjectileStyling.UEV
                ? Color.valueOf("985cff")
                : Color.valueOf("5ecbff");
        marker.frontColor = Color.white;

        marker.collides = false;
        marker.collidesTiles = false;
        marker.collidesAir = false;
        marker.collidesGround = false;
        marker.hittable = false;
        marker.reflectable = false;
        marker.absorbable = false;
        marker.keepVelocity = false;
        marker.despawnHit = false;

        marker.trailLength = 12 + Math.max(0, tier - MdtProjectileStyling.ZPM);
        marker.trailWidth = 2.4f + tier * 0.08f;
        marker.trailColor = marker.backColor.cpy();
        marker.lightRadius = 28f + tier * 2f;
        marker.lightOpacity = 0.55f;
        marker.lightColor = marker.backColor.cpy();
        marker.hitEffect = Fx.none;
        marker.despawnEffect = Fx.none;

        marker.bomb = bomb;
        marker.bombCount = bombardmentCount(tier);
        marker.strikeRadius = 28f + tier * 3.2f;
        marker.bombDelay = Math.max(2.5f, 6.5f - tier * 0.18f);

        Weapon weapon = new Weapon(name);
        weapon.mirror = false;
        weapon.rotate = true;
        weapon.rotateSpeed = 2.2f + tier * 0.12f;
        weapon.x = 0f;
        weapon.y = Math.max(3f, hitSize * 0.25f);
        weapon.shootY = Math.max(5f, hitSize * 0.24f);
        weapon.reload = Math.max(150f, 330f - tier * 7f);
        weapon.recoil = Math.max(1f, hitSize * 0.025f);
        weapon.shake = Math.min(6f, 1.5f + tier * 0.25f);
        weapon.ejectEffect = Fx.none;
        weapon.bullet = marker;
        return weapon;
    }

    private static int bombardmentCount(int tier){
        if(tier < MdtProjectileStyling.IV) return 3;
        if(tier <= MdtProjectileStyling.ZPM) return 5;
        if(tier <= MdtProjectileStyling.UEV) return 7;
        return Math.min(11, 8 + (tier - MdtProjectileStyling.UIV));
    }

    public static AirstrikeBombBulletType airstrikeBomb(int tier, float damage){
        AirstrikeBombBulletType bomb = new AirstrikeBombBulletType();

        bomb.speed = 0f;
        bomb.damage = 0f;
        bomb.lifetime = Math.max(20f, 34f - tier * 0.55f);
        bomb.keepVelocity = false;

        bomb.collides = false;
        bomb.collidesTiles = false;
        bomb.collidesAir = false;
        bomb.collidesGround = false;
        bomb.hittable = false;
        bomb.reflectable = false;
        bomb.absorbable = false;

        bomb.despawnHit = true;
        bomb.splashDamage = damage;
        bomb.splashDamageRadius = 24f + tier * 2.8f;
        bomb.scaledSplashDamage = true;

        bomb.width = 8f + tier * 0.45f;
        bomb.height = 12f + tier * 0.72f;
        bomb.shrinkX = 0f;
        bomb.shrinkY = 0f;
        bomb.backColor = tier >= MdtProjectileStyling.UEV
                ? Color.valueOf("ff5f86")
                : Pal.missileYellowBack.cpy();
        bomb.frontColor = tier >= MdtProjectileStyling.UEV
                ? Color.valueOf("ffe4ef")
                : Pal.missileYellow.cpy();

        bomb.layer = MdtProjectileStyling.airDropLayer;

        bomb.visualHeight = 56f + tier * 4.5f;
        bomb.visualSideDrift = 8f + tier * 0.8f;

        bomb.hitEffect = Fx.none;
        bomb.despawnEffect = tier >= MdtProjectileStyling.UV
                ? MdtProjectileEffects.highTierImpact
                : Fx.blastExplosion;
        bomb.hitColor = bomb.backColor.cpy();

        if(tier >= MdtProjectileStyling.UIV){
            bomb.lightning = 1 + (tier - MdtProjectileStyling.UIV);
            bomb.lightningLength = 7 + (tier - MdtProjectileStyling.UIV) * 2;
            bomb.lightningLengthRand = 3 + (tier - MdtProjectileStyling.UIV);
            bomb.lightningDamage = Math.max(8f, damage * 0.07f);
            bomb.lightningColor = Color.valueOf("f0a9ff");
        }

        return bomb;
    }

    /**
     * Bomb body is physically at the target point, but its sprite starts "high"
     * in screen-space and falls toward that point.
     *
     * Since layer = flyingUnit - 0.5, the bomb is always drawn under aircraft.
     */
    public static class AirstrikeBombBulletType extends BombBulletType {
        public float visualHeight = 72f;
        public float visualSideDrift = 10f;

        @Override
        public void load(){
            if(Core.atlas != null && Core.atlas.has("bombardment-bomb")){
                sprite = "bombardment-bomb";
            }else if(Core.atlas != null && Core.atlas.has("shell")){
                sprite = "shell";
            }
            super.load();
        }

        @Override
        public void draw(Bullet b){
            float oldX = b.x;
            float oldY = b.y;

            float fall = 1f - Interp.pow3In.apply(b.fin());
            float side = Mathf.sin(b.id * 0.73f) * visualSideDrift * fall;

            b.x = oldX + side;
            b.y = oldY + visualHeight * fall;
            super.draw(b);

            b.x = oldX;
            b.y = oldY;
        }
    }

    /**
     * A visible target marker. It does not directly deal damage.
     * When its flight ends at the aimed point, it calls in repeated instances
     * of one AirstrikeBombBulletType.
     */
    public static class BombardmentMarkerBulletType extends BasicBulletType {
        public AirstrikeBombBulletType bomb;
        public int bombCount = 5;
        public float strikeRadius = 45f;
        public float bombDelay = 4f;

        @Override
        public void despawned(Bullet b){
            super.despawned(b);

            if(bomb == null || b.absorbed) return;

            final float cx = b.x;
            final float cy = b.y;
            final var owner = b.owner;
            final var team = b.team;
            final int seed = b.id;

            for(int i = 0; i < bombCount; i++){
                final int index = i;
                Time.run(index * bombDelay, () -> {
                    // Deterministic-looking spiral + small random-looking phase based on bullet id.
                    float angle = index * 137.50776f + seed * 17.31f;
                    float radius = strikeRadius * Mathf.sqrt((index + 0.65f) / Math.max(1f, bombCount));
                    float px = cx + Mathf.cosDeg(angle) * radius;
                    float py = cy + Mathf.sinDeg(angle) * radius;

                    bomb.create(owner, team, px, py, 270f);
                });
            }
        }
    }
}
