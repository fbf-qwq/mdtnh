package mdtnh.units;

import arc.Core;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * MDTNH projectile post-processor.
 *
 * Goals:
 * 1) projectile Z is selected by attack semantics, not globally forced to one layer;
 * 2) tier changes visuals without turning every weapon into the same projectile;
 * 3) all reachable child bullets are processed recursively:
 *      fragBullet / intervalBullet / spawnBullets
 *    and MDTNH custom behavior-owned BulletType fields are found reflectively;
 * 4) existing "one Weapon -> one direct BulletType" rule is preserved;
 * 5) new signature attacks are injected only into units whose role fits them.
 *
 * Call {@link #applyAll()} once, after ModUnits.load()/ModAdvancedUnits.load()
 * and after all regular unit weapons have been constructed, but still inside loadContent().
 */
public final class MdtProjectileStyling {

    private MdtProjectileStyling(){}

    /** Bloom/effect layer. Moving glow/trail effects stay here. */
    public static final float glowLayer = Layer.effect - 0.05f;
    /** Air-dropped bombs are deliberately below flying units. */
    public static final float airDropLayer = Layer.flyingUnit - 0.50f;
    /** Ordinary projectile bodies that must remain visible over aircraft. */
    public static final float projectileBodyLayer = Layer.flyingUnit + 0.50f;
    /** Beam bodies are slightly above aircraft, but below shield/UI layers. */
    public static final float beamBodyLayer = Layer.flyingUnit + 0.35f;

    public static final int ULV = 0, LV = 1, MV = 2, HV = 3, EV = 4, IV = 5,
            LuV = 6, ZPM = 7, UV = 8, UHV = 9, UEV = 10, UIV = 11,
            UXV = 12, OpV = 13, MAX = 14;

    public enum Semantic {
        physical,
        artillery,
        missile,
        airDrop,
        energy,
        beam,
        healing,
        gravity,
        fragment,
        interceptor
    }

    private static final ObjectMap<String, Integer> unitTiers = new ObjectMap<>();
    private static final ObjectSet<BulletType> styledBullets = new ObjectSet<>();
    private static final ObjectSet<Object> reflectedObjects = new ObjectSet<>();

    static {
        registerLine("naval-attack-", new String[]{
                "skiff","wake","pike","salvo","torrent","trench","prism","leviathan",
                "hunter","bastion","dynamo","maelstrom","marshal","nemesis","sovereign"
        });

        registerLine("naval-support-", new String[]{
                "spring","escort","convoy","vortex","solace","reflux","haven","aegis",
                "bulwark","reversal","silence","tidal","chorus","sustain","ark"
        });

        registerLine("ground-attack-", new String[]{
                "bayonet","hammer","flame","mortar","thunder","cannon","lancer","dominion",
                "fortress","furnace","verdict","tremor","legion","judgment","terminus"
        });

        registerLine("ground-support-", new String[]{
                "glint","mend","pulse","guard","mercy","bulwark","lumen","sanctum",
                "rampart","regen","refract","stasis","command","aegis","providence"
        });

        registerLine("air-attack-", new String[]{
                "dart","falcon","dive","bomber","raptor","javelin","ray","eclipse",
                "phantom","talon","blink","interdictor","squadron","nemesis","apocalypse"
        });

        // The design contains two displayed "Beacon" units in the same line.
        // Existing implementation keeps unique content IDs by using beacon-uv for UV.
        registerLine("air-support-", new String[]{
                "worker","finch","tinker","mender","lifter","warden","beacon","hive",
                "beacon-uv","shepherd","mirror","relay","swarm","network","seraph"
        });

        registerLine("crawler-", new String[]{
                "scarab","mite","ember","venom","pincer","leech","facet","plague",
                "ripper","pack","phase","graviton","brood","devourer","calamity"
        });
    }

    private static void registerLine(String prefix, String[] names){
        if(names.length != 15){
            throw new IllegalArgumentException("Expected 15 tiers for " + prefix);
        }
        for(int tier = 0; tier < names.length; tier++){
            unitTiers.put(prefix + names[tier], tier);
        }
    }

    /**
     * Applies the full rule set to every MDTNH regular unit found in content,
     * then adds a small number of role-appropriate new attacks and styles those too.
     */
    public static void applyAll(){
        styledBullets.clear();
        reflectedObjects.clear();

        // First inject new role-specific attacks so they are covered by the same styling pass.
        injectSignatureAttacks();

        int units = 0;
        int weapons = 0;
        for(UnitType unit : Vars.content.units()){
            int tier = tierOf(unit);
            if(tier < 0) continue;

            units++;
            weapons += styleUnit(unit, tier);
        }

        Log.info("MDTNH projectile styling applied: @ regular units, @ direct weapons.", units, weapons);
    }

    /** Styles one unit and every bullet reachable from each direct Weapon bullet. */
    public static int styleUnit(UnitType unit, int tier){
        if(unit == null) return 0;

        int count = 0;
        for(Weapon weapon : unit.weapons){
            if(weapon == null || weapon.bullet == null) continue;
            count++;

            String weaponName = weapon.name == null ? "" : weapon.name.toLowerCase();
            Semantic semantic = classify(weapon.bullet, weaponName);

            // Continuous weapons must be parented to their current mount.
            if(weapon.bullet instanceof ContinuousLaserBulletType ||
                    weapon.bullet instanceof PointLaserBulletType){
                weapon.continuous = true;
                weapon.parentizeEffects = true;
            }

            styleRecursive(weapon.bullet, tier, semantic, 0);

            // Covers MDTNH custom BulletBehavior objects that hold their own BulletType fields
            // and are not connected through fragBullet/intervalBullet/spawnBullets.
            scanMdtOwnedBullets(weapon.bullet, tier, 0);
        }

        return count;
    }

    private static void styleRecursive(BulletType bullet, int tier, Semantic semantic, int depth){
        if(bullet == null || depth > 12 || !styledBullets.add(bullet)) return;

        applyLayer(bullet, semantic);
        applyTierVisuals(bullet, tier, semantic);

        if(bullet.fragBullet != null && bullet.fragBullet != bullet){
            Semantic child = classify(bullet.fragBullet, "frag");
            styleRecursive(bullet.fragBullet, tier, child == Semantic.physical ? Semantic.fragment : child, depth + 1);
        }

        if(bullet.intervalBullet != null && bullet.intervalBullet != bullet){
            styleRecursive(bullet.intervalBullet, tier, classify(bullet.intervalBullet, "interval"), depth + 1);
        }

        if(bullet.spawnBullets != null){
            for(BulletType spawned : bullet.spawnBullets){
                if(spawned != null && spawned != bullet){
                    styleRecursive(spawned, tier, classify(spawned, "spawn"), depth + 1);
                }
            }
        }
    }

    private static void applyLayer(BulletType bullet, Semantic semantic){
        switch(semantic){
            case airDrop -> bullet.layer = airDropLayer;
            case beam -> bullet.layer = beamBodyLayer;
            default -> bullet.layer = projectileBodyLayer;
        }
    }

    /**
     * Tier visuals deliberately avoid overwriting every custom projectile.
     * They strengthen minimum size, trail, light and default colors;
     * A-series/custom colors survive unless the projectile is explicitly a gravity core.
     */
    private static void applyTierVisuals(BulletType bullet, int tier, Semantic semantic){
        tier = Math.max(ULV, Math.min(MAX, tier));

        // Real dynamic light is independent from BulletType.layer.
        // This keeps high-tier shots luminous even when their body is above flying units.
        if(semantic != Semantic.airDrop){
            float minLight = tier < EV ? 0f : 12f + (tier - EV) * 3.2f;
            if(minLight > 0f){
                bullet.lightRadius = Math.max(bullet.lightRadius, minLight);
                bullet.lightOpacity = Math.max(bullet.lightOpacity, Math.min(0.72f, 0.22f + tier * 0.028f));
                bullet.lightColor = lightColorFor(semantic, tier);
            }
        }

        // Tiered trail: effects are rendered on the effect/Bloom side,
        // while the actual body can stay above aircraft.
        if(tier >= HV && semantic != Semantic.airDrop && semantic != Semantic.beam){
            bullet.trailLength = Math.max(bullet.trailLength, 4 + (tier - HV));
            bullet.trailWidth = Math.max(bullet.trailWidth, 1.4f + (tier - HV) * 0.18f);
            bullet.trailParam = Math.max(bullet.trailParam, 1.5f + tier * 0.10f);
            bullet.trailColor = trailColorFor(semantic, tier);

            if(bullet.trailEffect == null || bullet.trailEffect == Fx.missileTrail){
                bullet.trailEffect = MdtProjectileEffects.tierGlowTrail;
                bullet.trailInterval = bullet.trailInterval <= 0f ? 1.5f : Math.min(bullet.trailInterval, 1.5f);
            }
        }

        if(bullet instanceof BasicBulletType basic){
            applyBasicVisuals(basic, tier, semantic);
        }

        // High-tier gravity core:
        // UIV+ gets a black-hole body when the sprite exists and releases lightning on impact.
        if(semantic == Semantic.gravity){
            applyGravityCoreVisuals(bullet, tier);
        }

        // Default hit visuals get a stronger tiered effect, bespoke effects are left intact.
        if(tier >= UV && (bullet.hitEffect == Fx.hitBulletSmall || bullet.hitEffect == Fx.hitBulletBig)){
            bullet.hitEffect = MdtProjectileEffects.highTierImpact;
            bullet.hitColor = hitColorFor(semantic, tier);
        }

        if(tier >= UEV && (bullet.despawnEffect == Fx.hitBulletSmall || bullet.despawnEffect == Fx.hitBulletBig)){
            bullet.despawnEffect = MdtProjectileEffects.highTierImpact;
            bullet.hitColor = hitColorFor(semantic, tier);
        }
    }

    private static void applyBasicVisuals(BasicBulletType basic, int tier, Semantic semantic){
        float minWidth;
        float minHeight;

        switch(semantic){
            case missile -> {
                minWidth = 5f + tier * 0.42f;
                minHeight = 10f + tier * 0.80f;
            }
            case artillery -> {
                minWidth = 7f + tier * 0.50f;
                minHeight = 9f + tier * 0.62f;
            }
            case airDrop -> {
                minWidth = 8f + tier * 0.45f;
                minHeight = 11f + tier * 0.70f;
            }
            case energy, gravity -> {
                minWidth = 6f + tier * 0.52f;
                minHeight = 8f + tier * 0.60f;
            }
            case fragment -> {
                minWidth = 3.5f + tier * 0.25f;
                minHeight = 5f + tier * 0.32f;
            }
            default -> {
                minWidth = 5f + tier * 0.38f;
                minHeight = 7f + tier * 0.55f;
            }
        }

        basic.width = Math.max(basic.width, minWidth);
        basic.height = Math.max(basic.height, minHeight);

        // Healing and special vanilla families retain their identity.
        if(semantic == Semantic.healing){
            basic.backColor = Pal.heal.cpy().lerp(Color.white, Math.min(0.45f, tier / 32f));
            basic.frontColor = Color.white.cpy();
            return;
        }

        if(semantic == Semantic.gravity){
            basic.backColor = Color.valueOf("3b1b66");
            basic.frontColor = tier >= UIV ? Color.valueOf("f0dcff") : Color.valueOf("b67cff");
            return;
        }

        // Recolor only bullets that still look like generic vanilla physical rounds,
        // instead of destroying carefully assigned A-series colors.
        if(isGenericVanillaBulletColor(basic)){
            basic.backColor = backColorFor(semantic, tier);
            basic.frontColor = frontColorFor(semantic, tier);
        }
    }

    private static boolean isGenericVanillaBulletColor(BasicBulletType basic){
        return close(basic.frontColor, Pal.bulletYellow) ||
                close(basic.frontColor, Color.white) ||
                close(basic.backColor, Pal.bulletYellowBack);
    }

    private static boolean close(Color a, Color b){
        if(a == null || b == null) return false;
        return Math.abs(a.r - b.r) < 0.035f &&
                Math.abs(a.g - b.g) < 0.035f &&
                Math.abs(a.b - b.b) < 0.035f;
    }

    private static void applyGravityCoreVisuals(BulletType bullet, int tier){
        Color gravity = tier >= UIV ? Color.valueOf("b689ff") : Color.valueOf("8357d6");
        bullet.lightColor = gravity;
        bullet.trailColor = tier >= UIV ? Color.valueOf("6c3fb7") : Color.valueOf("7145aa");

        if(bullet instanceof BasicBulletType basic){
            basic.width = Math.max(basic.width, tier >= UIV ? 18f + (tier - UIV) * 3.5f : 11f + tier * 0.5f);
            basic.height = Math.max(basic.height, basic.width);

            // Only switch to the custom black-hole sprite if it is actually present.
            // Otherwise the existing projectile sprite remains valid.
            if(tier >= UIV && Core.atlas != null && Core.atlas.has("gravity-core-blackhole")){
                basic.sprite = "gravity-core-blackhole";
                basic.backSprite = null;
                basic.spin = Math.max(Math.abs(basic.spin), 1.2f + (tier - UIV) * 0.25f);
            }
        }

        if(tier >= UIV){
            int arcs = 2 + (tier - UIV);
            bullet.lightning = Math.max(bullet.lightning, arcs);
            bullet.lightningLength = Math.max(bullet.lightningLength, 10 + (tier - UIV) * 3);
            bullet.lightningLengthRand = Math.max(bullet.lightningLengthRand, 4 + (tier - UIV) * 2);
            bullet.lightningDamage = Math.max(bullet.lightningDamage, Math.max(8f, bullet.damage * (0.10f + (tier - UIV) * 0.025f)));
            bullet.lightningColor = Color.valueOf("c9a7ff");
            bullet.hitEffect = MdtProjectileEffects.gravityCollapse;
            bullet.despawnEffect = MdtProjectileEffects.gravityCollapse;
            bullet.hitColor = Color.valueOf("b689ff");
        }
    }

    private static Semantic classify(BulletType bullet, String weaponName){
        String name = weaponName == null ? "" : weaponName.toLowerCase();

        if(name.contains("a24") || name.contains("gravity") || name.contains("graviton") || name.contains("blackhole")){
            return Semantic.gravity;
        }
        if(name.contains("bomb") || name.contains("airdrop") || name.contains("air-drop")){
            return Semantic.airDrop;
        }
        if(name.contains("intercept") || name.contains("point-defense") || name.contains("-pd")){
            return Semantic.interceptor;
        }

        if(bullet.heals()) return Semantic.healing;
        if(bullet instanceof BombBulletType) return Semantic.airDrop;
        if(bullet instanceof ContinuousLaserBulletType ||
                bullet instanceof PointLaserBulletType ||
                bullet instanceof LaserBulletType){
            return Semantic.beam;
        }
        if(bullet instanceof ArtilleryBulletType) return Semantic.artillery;
        if(bullet instanceof MissileBulletType) return Semantic.missile;
        if(bullet instanceof EmpBulletType ||
                bullet instanceof SapBulletType ||
                bullet instanceof LightningBulletType ||
                bullet instanceof LaserBoltBulletType){
            return Semantic.energy;
        }
        return Semantic.physical;
    }

    private static Color backColorFor(Semantic semantic, int tier){
        float high = Math.max(0f, (tier - EV) / 10f);
        return switch(semantic){
            case missile -> Color.valueOf("ff8a4c").lerp(Color.valueOf("ff4f8f"), high);
            case artillery -> Color.valueOf("e6a14a").lerp(Color.valueOf("ffd58a"), high);
            case energy -> Color.valueOf("53b7ff").lerp(Color.valueOf("9d6cff"), high);
            case interceptor -> Color.valueOf("7ce8ff").lerp(Color.white, high * 0.6f);
            default -> Color.valueOf("f0ad4e").lerp(Color.valueOf("d9f4ff"), high);
        };
    }

    private static Color frontColorFor(Semantic semantic, int tier){
        float high = Math.max(0f, (tier - EV) / 10f);
        return switch(semantic){
            case missile -> Color.valueOf("ffd0a1").lerp(Color.valueOf("ffd6f2"), high);
            case artillery -> Color.valueOf("fff0c7").lerp(Color.white, high);
            case energy -> Color.valueOf("d8f3ff").lerp(Color.white, high);
            default -> Color.valueOf("fff1cc").lerp(Color.white, high);
        };
    }

    private static Color trailColorFor(Semantic semantic, int tier){
        return switch(semantic){
            case gravity -> Color.valueOf("7541c8");
            case energy, beam, interceptor -> tier >= UEV ? Color.valueOf("a682ff") : Color.valueOf("68c8ff");
            case missile -> tier >= UEV ? Color.valueOf("ff6b9c") : Color.valueOf("ff9b62");
            case artillery -> tier >= UEV ? Color.valueOf("ffd98a") : Color.valueOf("e9a650");
            case healing -> Pal.heal.cpy();
            default -> tier >= UEV ? Color.valueOf("bfeeff") : Pal.bulletYellowBack.cpy();
        };
    }

    private static Color lightColorFor(Semantic semantic, int tier){
        return switch(semantic){
            case gravity -> Color.valueOf("b689ff");
            case energy, beam, interceptor -> tier >= UEV ? Color.valueOf("bda7ff") : Color.valueOf("8ddcff");
            case healing -> Pal.heal.cpy();
            case missile -> Color.valueOf("ff9a71");
            default -> tier >= UEV ? Color.valueOf("d9f4ff") : Color.valueOf("ffd38a");
        };
    }

    private static Color hitColorFor(Semantic semantic, int tier){
        return switch(semantic){
            case gravity -> Color.valueOf("b689ff");
            case energy, beam, interceptor -> Color.valueOf("9cdcff");
            case healing -> Pal.heal.cpy();
            case missile -> Color.valueOf("ff7a82");
            default -> tier >= UEV ? Color.valueOf("dff7ff") : Color.valueOf("ffc96b");
        };
    }

    /**
     * Reflectively walks MDTNH-owned fields so behavior objects that hold a BulletType
     * are not skipped. Only mdtnh.* classes are opened; Mindustry/Arc/JDK internals
     * are never reflectively traversed.
     */
    private static void scanMdtOwnedBullets(Object root, int tier, int depth){
        if(root == null || depth > 8 || !reflectedObjects.add(root)) return;

        Class<?> type = root.getClass();
        while(type != null && type != Object.class){
            String className = type.getName();
            if(!className.startsWith("mdtnh.")){
                type = type.getSuperclass();
                continue;
            }

            for(Field field : type.getDeclaredFields()){
                if(Modifier.isStatic(field.getModifiers())) continue;

                try{
                    if(!field.canAccess(root)) field.setAccessible(true);
                    Object value = field.get(root);
                    if(value == null) continue;

                    if(value instanceof BulletType nested){
                        styleRecursive(nested, tier, classify(nested, field.getName()), 0);
                    }else if(value instanceof Seq<?> seq){
                        for(Object child : seq){
                            if(child instanceof BulletType nested){
                                styleRecursive(nested, tier, classify(nested, field.getName()), 0);
                            }else if(child != null && child.getClass().getName().startsWith("mdtnh.")){
                                scanMdtOwnedBullets(child, tier, depth + 1);
                            }
                        }
                    }else if(value.getClass().getName().startsWith("mdtnh.")){
                        scanMdtOwnedBullets(value, tier, depth + 1);
                    }
                }catch(Throwable ignored){
                    // A visual post-processor must never make content loading fail
                    // because one private implementation field could not be inspected.
                }
            }

            type = type.getSuperclass();
        }
    }

    private static int tierOf(UnitType unit){
        if(unit == null || unit.name == null) return -1;
        String id = canonicalId(unit.name);
        Integer tier = unitTiers.get(id);
        return tier == null ? -1 : tier;
    }

    /** Handles names both with and without Mindustry's mod-name prefix. */
    public static String canonicalId(String raw){
        if(raw == null) return "";
        String name = raw.toLowerCase();

        String[] prefixes = {
                "naval-attack-", "naval-support-", "ground-attack-", "ground-support-",
                "air-attack-", "air-support-", "crawler-"
        };

        for(String prefix : prefixes){
            int index = name.indexOf(prefix);
            if(index >= 0) return name.substring(index);
        }
        return name;
    }

    private static UnitType findUnit(String canonical){
        for(UnitType unit : Vars.content.units()){
            if(canonicalId(unit.name).equals(canonical)) return unit;
        }
        return null;
    }

    private static boolean hasWeapon(UnitType unit, String prefix){
        if(unit == null) return false;
        for(Weapon weapon : unit.weapons){
            if(weapon != null && weapon.name != null && weapon.name.startsWith(prefix)) return true;
        }
        return false;
    }

    /**
     * Adds new attacks only where they reinforce the unit's role.
     *
     * - Bomber/Eclipse: actual under-aircraft bomb drops.
     * - Apocalypse/Judgment/Terminus/Sovereign: target-marker -> delayed bombardment.
     *
     * No Weapon directly fires multiple BulletTypes.
     */
    private static void injectSignatureAttacks(){
        addAirDrop("air-attack-bomber", HV, 72f);
        addAirDrop("air-attack-eclipse", ZPM, 165f);

        addBombardment("air-attack-apocalypse", MAX, 350f, 560f);
        addBombardment("ground-attack-judgment", OpV, 420f, 610f);
        addBombardment("ground-attack-terminus", MAX, 520f, 660f);
        addBombardment("naval-attack-sovereign", MAX, 440f, 640f);
    }

    private static void addAirDrop(String unitId, int tier, float damage){
        UnitType unit = findUnit(unitId);
        if(unit == null || hasWeapon(unit, "mdt-airdrop-")) return;

        unit.weapons.add(MdtAttackProjectiles.airDropWeapon(
                "mdt-airdrop-" + unitId,
                tier,
                unit.hitSize,
                damage
        ));
    }

    private static void addBombardment(String unitId, int tier, float damage, float range){
        UnitType unit = findUnit(unitId);
        if(unit == null || hasWeapon(unit, "mdt-bombardment-")) return;

        unit.weapons.add(MdtAttackProjectiles.bombardmentWeapon(
                "mdt-bombardment-" + unitId,
                tier,
                unit.hitSize,
                damage,
                range
        ));
    }
}


/** Package-private effects used by all projectile families. */
final class MdtProjectileEffects {

    private MdtProjectileEffects(){}

    static final Effect tierGlowTrail = new Effect(16f, e -> {
        float oldZ = arc.graphics.g2d.Draw.z();
        arc.graphics.g2d.Draw.z(MdtProjectileStyling.glowLayer);

        arc.graphics.g2d.Draw.color(e.color);
        arc.graphics.g2d.Draw.alpha(0.18f + e.fout() * 0.34f);
        float radius = Math.max(0.8f, e.rotation) * (0.35f + e.fout() * 0.65f);
        arc.graphics.g2d.Fill.circle(e.x, e.y, radius);

        arc.graphics.g2d.Draw.reset();
        arc.graphics.g2d.Draw.z(oldZ);
    });

    static final Effect highTierImpact = new Effect(26f, 90f, e -> {
        float oldZ = arc.graphics.g2d.Draw.z();
        arc.graphics.g2d.Draw.z(MdtProjectileStyling.glowLayer);

        arc.graphics.g2d.Draw.color(e.color, Color.white, e.fin() * 0.35f);
        arc.graphics.g2d.Lines.stroke(3.2f * e.fout());
        arc.graphics.g2d.Lines.circle(e.x, e.y, 5f + e.finpow() * 24f);

        arc.graphics.g2d.Draw.alpha(0.42f * e.fout());
        arc.graphics.g2d.Fill.circle(e.x, e.y, 9f * e.fout());

        arc.graphics.g2d.Draw.reset();
        arc.graphics.g2d.Draw.z(oldZ);
    });

    static final Effect gravityCollapse = new Effect(42f, 130f, e -> {
        float oldZ = arc.graphics.g2d.Draw.z();
        arc.graphics.g2d.Draw.z(MdtProjectileStyling.glowLayer);

        Color purple = Color.valueOf("9d6cff");
        arc.graphics.g2d.Draw.color(purple, Color.white, e.fin() * 0.45f);

        arc.graphics.g2d.Lines.stroke(4.2f * e.fout());
        arc.graphics.g2d.Lines.circle(e.x, e.y, 8f + e.finpow() * 38f);
        arc.graphics.g2d.Lines.stroke(2.2f * e.fout());
        arc.graphics.g2d.Lines.circle(e.x, e.y, 28f * e.fout());

        arc.graphics.g2d.Draw.color(Color.black, purple, 0.25f);
        arc.graphics.g2d.Draw.alpha(0.72f * e.fout());
        arc.graphics.g2d.Fill.circle(e.x, e.y, 13f * (0.4f + e.fout() * 0.6f));

        arc.graphics.g2d.Draw.reset();
        arc.graphics.g2d.Draw.z(oldZ);
    });
}
