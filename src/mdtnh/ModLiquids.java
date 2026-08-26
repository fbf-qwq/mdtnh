package mdtnh;

import arc.graphics.Color;
import arc.util.Log;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.type.Liquid;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MDT/NH 扩展流体注册表。
 *
 * 重要原则：
 * 1. Mindustry 原版已经存在的流体直接复用，不重复 new Liquid。
 * 2. GtLiquids 已经存在的流体直接复用，不重复注册同义流体。
 * 3. steam 保留原 ModLiquids 的既有内容 ID。
 * 4. 只有清单中真正缺失的 189 项才在本类中新建。
 *
 * get(id) 使用“清单语义 ID”，因此即使底层实际对象来自 gt-* 或原版，
 * 配方侧仍可统一通过 ModLiquids.get(...) 获取。
 */
public class ModLiquids {

    /** 204 个清单项的统一访问表，其中包含复用对象和新建对象。 */
    public static final Map<String, Liquid> liquids = new LinkedHashMap<>();

    // 常用直接字段，保留旧代码兼容。
    public static Liquid water;
    public static Liquid steam;
    public static Liquid distilledWater;
    public static Liquid ozone;
    public static Liquid petroleum;

    /** 所有水类流体，用于锅炉等设备。 */
    public static Liquid[] AllWater;

    private enum Profile {
        ACID,
        BIO,
        CHEMICAL,
        CRYO,
        FUEL,
        GAS,
        GAS_FUEL,
        HEAVY,
        MOLTEN,
        NUCLEAR_FUEL,
        ORGANIC,
        OXIDIZER,
        PLASMA,
        PURIFIED_WATER,
        SPECIAL,
        SUPERHEATED_STEAM,
        TOXIC_GAS,
        WASTE,
        STEAM
    }

    private static final class FluidDef {
        final String id;
        final String color;
        final Profile profile;

        FluidDef(String id, String color, Profile profile) {
            this.id = id;
            this.color = color;
            this.profile = profile;
        }
    }

    /** 清单中扣除 15 个已存在/复用项后，真正需要新注册的 189 项。 */
    private static final FluidDef[] NEW_FLUID_DATA = {
        new FluidDef("filtered-clear-water", "4D82B8", Profile.PURIFIED_WATER),
        new FluidDef("ozone-treated-water", "51B2C1", Profile.PURIFIED_WATER),
        new FluidDef("flocculated-settled-water", "55A1C9", Profile.PURIFIED_WATER),
        new FluidDef("acid-base-neutralized-water", "466FA6", Profile.PURIFIED_WATER),
        new FluidDef("extreme-temperature-purified-water", "499BAF", Profile.PURIFIED_WATER),
        new FluidDef("uv-irradiated-water", "4D8CB8", Profile.PURIFIED_WATER),
        new FluidDef("deionized-degassed-water", "517AC1", Profile.PURIFIED_WATER),
        new FluidDef("ideal-particulate-water", "55ABC9", Profile.PURIFIED_WATER),
        new FluidDef("ice", "378FAB", Profile.CRYO),
        new FluidDef("superheated-steam", "EBC1A0", Profile.SUPERHEATED_STEAM),
        new FluidDef("supercritical-dense-steam", "F4D9A6", Profile.SUPERHEATED_STEAM),
        new FluidDef("hydrogen-peroxide", "B55B99", Profile.OXIDIZER),
        new FluidDef("carbon-monoxide", "458337", Profile.TOXIC_GAS),
        new FluidDef("carbon-dioxide", "A399CC", Profile.GAS),
        new FluidDef("sulfur-dioxide", "5C953F", Profile.TOXIC_GAS),
        new FluidDef("sulfur-trioxide", "859E42", Profile.TOXIC_GAS),
        new FluidDef("ammonia", "577A33", Profile.TOXIC_GAS),
        new FluidDef("nitric-oxide", "AA92C3", Profile.GAS),
        new FluidDef("nitrous-oxide", "9E99CC", Profile.GAS),
        new FluidDef("nitrogen-dioxide", "64953F", Profile.TOXIC_GAS),
        new FluidDef("dinitrogen-tetroxide", "D96CA2", Profile.OXIDIZER),
        new FluidDef("hydrazine", "724896", Profile.SPECIAL),
        new FluidDef("silane", "B6A35B", Profile.GAS_FUEL),
        new FluidDef("hydrofluoric-acid", "679E2F", Profile.ACID),
        new FluidDef("industrial-hydrofluoric-acid", "99A732", Profile.ACID),
        new FluidDef("hydrogen-sulfide", "5E9E42", Profile.TOXIC_GAS),
        new FluidDef("dilute-hydrochloric-acid", "82952D", Profile.ACID),
        new FluidDef("ammonium-chloride", "689EA3", Profile.CHEMICAL),
        new FluidDef("dilute-sulfuric-acid", "91B035", Profile.ACID),
        new FluidDef("aqua-regia", "5E8C2A", Profile.ACID),
        new FluidDef("boric-acid", "8C952D", Profile.ACID),
        new FluidDef("nitric-acid", "C763A0", Profile.OXIDIZER),
        new FluidDef("hypochlorous-acid", "D068CE", Profile.OXIDIZER),
        new FluidDef("phosphoric-acid", "9CB035", Profile.ACID),
        new FluidDef("fluorosilicic-acid", "678C2A", Profile.ACID),
        new FluidDef("fluoroantimonic-acid", "95952D", Profile.ACID),
        new FluidDef("phosgene", "5D8C3B", Profile.TOXIC_GAS),
        new FluidDef("boron-trichloride", "4F953F", Profile.TOXIC_GAS),
        new FluidDef("titanium-tetrachloride", "8E59BA", Profile.SPECIAL),
        new FluidDef("ferric-chloride", "5D9190", Profile.CHEMICAL),
        new FluidDef("polyaluminum-chloride", "68A398", Profile.CHEMICAL),
        new FluidDef("ethane", "D19D69", Profile.GAS_FUEL),
        new FluidDef("acetylene", "B6B15B", Profile.GAS_FUEL),
        new FluidDef("propane", "BF9660", Profile.GAS_FUEL),
        new FluidDef("propylene", "C8B364", Profile.GAS_FUEL),
        new FluidDef("butane", "D0D169", Profile.GAS_FUEL),
        new FluidDef("butene", "AD8D57", Profile.GAS_FUEL),
        new FluidDef("butadiene", "B6A95B", Profile.GAS_FUEL),
        new FluidDef("cyclopentadiene", "996243", Profile.ORGANIC),
        new FluidDef("isoprene", "A27B47", Profile.ORGANIC),
        new FluidDef("cyclohexane", "AB984B", Profile.ORGANIC),
        new FluidDef("octane", "875B3B", Profile.ORGANIC),
        new FluidDef("benzene", "90733F", Profile.ORGANIC),
        new FluidDef("naphthalene", "998D43", Profile.ORGANIC),
        new FluidDef("anthracene", "A27347", Profile.ORGANIC),
        new FluidDef("toluene", "AB8F4B", Profile.ORGANIC),
        new FluidDef("chlorobenzene", "87543B", Profile.ORGANIC),
        new FluidDef("o-xylene", "906C3F", Profile.ORGANIC),
        new FluidDef("p-xylene", "998643", Profile.ORGANIC),
        new FluidDef("m-xylene", "A26B47", Profile.ORGANIC),
        new FluidDef("ethylbenzene", "AB864B", Profile.ORGANIC),
        new FluidDef("chloromethane", "A68CBA", Profile.GAS),
        new FluidDef("chloroform", "90653F", Profile.ORGANIC),
        new FluidDef("fluoroform", "AE99CC", Profile.GAS),
        new FluidDef("methanol", "A26347", Profile.ORGANIC),
        new FluidDef("ethanol", "AB7E4B", Profile.ORGANIC),
        new FluidDef("butanol", "87743B", Profile.ORGANIC),
        new FluidDef("ethylene-glycol", "905D3F", Profile.ORGANIC),
        new FluidDef("1-4-butanediol", "997643", Profile.ORGANIC),
        new FluidDef("glycerol", "A29147", Profile.ORGANIC),
        new FluidDef("phenol", "AB754B", Profile.ORGANIC),
        new FluidDef("diethyl-ether", "876D3B", Profile.ORGANIC),
        new FluidDef("tetrahydrofuran", "90873F", Profile.ORGANIC),
        new FluidDef("formaldehyde", "A499CC", Profile.GAS),
        new FluidDef("acetaldehyde", "A28947", Profile.ORGANIC),
        new FluidDef("butyraldehyde", "AB6C4B", Profile.ORGANIC),
        new FluidDef("isobutyraldehyde", "87673B", Profile.ORGANIC),
        new FluidDef("acetone", "907F3F", Profile.ORGANIC),
        new FluidDef("cyclohexanone", "996743", Profile.ORGANIC),
        new FluidDef("formic-acid", "8BA732", Profile.ACID),
        new FluidDef("acetic-acid", "78B035", Profile.ACID),
        new FluidDef("propionic-acid", "848C2A", Profile.ACID),
        new FluidDef("naphthenic-acid", "76952D", Profile.ACID),
        new FluidDef("oxalic-acid", "9E962F", Profile.ACID),
        new FluidDef("phthalic-acid", "96A732", Profile.ACID),
        new FluidDef("methyl-acetate", "AB954B", Profile.ORGANIC),
        new FluidDef("methylamine", "918CBA", Profile.GAS),
        new FluidDef("dimethylamine", "A292C3", Profile.GAS),
        new FluidDef("trimethylamine", "B599CC", Profile.GAS),
        new FluidDef("diethylamine", "A27147", Profile.ORGANIC),
        new FluidDef("putrescine", "AB8C4B", Profile.ORGANIC),
        new FluidDef("cadaverine", "87803B", Profile.ORGANIC),
        new FluidDef("aniline", "906A3F", Profile.ORGANIC),
        new FluidDef("p-507", "998343", Profile.ORGANIC),
        new FluidDef("air", "A4A0D5", Profile.GAS),
        new FluidDef("liquid-air", "42BFCF", Profile.CRYO),
        new FluidDef("nether-air", "A48CBA", Profile.GAS),
        new FluidDef("toxic-air", "658337", Profile.TOXIC_GAS),
        new FluidDef("magma", "8F2C17", Profile.MOLTEN),
        new FluidDef("lava-magma", "984B18", Profile.MOLTEN),
        new FluidDef("crude-oil", "693E17", Profile.FUEL),
        new FluidDef("light-oil", "712219", Profile.FUEL),
        new FluidDef("heavy-oil", "473A2E", Profile.HEAVY),
        new FluidDef("extremely-heavy-oil", "504734", Profile.HEAVY),
        new FluidDef("brine", "74B5A1", Profile.CHEMICAL),
        new FluidDef("bacterial-slime", "362C23", Profile.HEAVY),
        new FluidDef("uu-matter", "9A4C9F", Profile.SPECIAL),
        new FluidDef("uu-amplification-liquid", "8151A8", Profile.SPECIAL),
        new FluidDef("unknown-liquid", "9D55B1", Profile.SPECIAL),
        new FluidDef("pine-oil", "692F17", Profile.FUEL),
        new FluidDef("enriched-fertilizer", "337138", Profile.BIO),
        new FluidDef("coolant", "3CBDB0", Profile.CRYO),
        new FluidDef("super-coolant", "3FB5C6", Profile.CRYO),
        new FluidDef("extreme-cold-ice", "429ECF", Profile.CRYO),
        new FluidDef("blazing-fire", "7D1417", Profile.MOLTEN),
        new FluidDef("waste-liquid", "303E22", Profile.WASTE),
        new FluidDef("light-silicon-rock-fuel", "A651A8", Profile.SPECIAL),
        new FluidDef("heavy-silicon-rock-fuel", "8C55B1", Profile.SPECIAL),
        new FluidDef("silicon-rock-asphalt", "594D3A", Profile.HEAVY),
        new FluidDef("extraction-nano-resin", "362723", Profile.HEAVY),
        new FluidDef("culture-medium-stock", "397133", Profile.BIO),
        new FluidDef("sterile-culture-medium", "377A3F", Profile.BIO),
        new FluidDef("biological-culture-medium-stock", "4D833B", Profile.BIO),
        new FluidDef("sterilized-biological-culture-medium", "428C3F", Profile.BIO),
        new FluidDef("mutagen", "2F693A", Profile.BIO),
        new FluidDef("isomeric-xenon", "7C4C9F", Profile.SPECIAL),
        new FluidDef("superheavy-lado-x", "9651A8", Profile.SPECIAL),
        new FluidDef("heavy-lado-x", "7B55B1", Profile.SPECIAL),
        new FluidDef("mutagenic-active-solder", "A1241A", Profile.MOLTEN),
        new FluidDef("sea-crystal-acid", "678C2A", Profile.ACID),
        new FluidDef("excited-crude-hyperdimensional-catalyst", "744C9F", Profile.SPECIAL),
        new FluidDef("excited-ordinary-hyperdimensional-catalyst", "8E51A8", Profile.SPECIAL),
        new FluidDef("excited-glorious-hyperdimensional-catalyst", "AA55B1", Profile.SPECIAL),
        new FluidDef("excited-alien-hyperdimensional-catalyst", "8E59BA", Profile.SPECIAL),
        new FluidDef("excited-stellar-hyperdimensional-catalyst", "844896", Profile.SPECIAL),
        new FluidDef("hyperdimensional-residue", "6D4C9F", Profile.SPECIAL),
        new FluidDef("dimensional-shifted-superfluid", "3CB6BD", Profile.CRYO),
        new FluidDef("tachyon-rich-time-fluid", "A255B1", Profile.SPECIAL),
        new FluidDef("expanded-spatial-fluid", "8659BA", Profile.SPECIAL),
        new FluidDef("boundless-universal-solder", "7D2114", Profile.MOLTEN),
        new FluidDef("fluid-primordial-substance", "974C9F", Profile.SPECIAL),
        new FluidDef("stable-baryonic-matter", "7F51A8", Profile.SPECIAL),
        new FluidDef("saturated-phononic-crystal-solution", "9A55B1", Profile.SPECIAL),
        new FluidDef("lossless-phonon-transmission-medium", "B759BA", Profile.SPECIAL),
        new FluidDef("primordial-substance", "764896", Profile.SPECIAL),
        new FluidDef("semi-stable-antimatter", "904C9F", Profile.SPECIAL),
        new FluidDef("concentrated-primordial-star-plasma-mixture", "812FA8", Profile.PLASMA),
        new FluidDef("degenerate-quark-gluon-plasma", "A432B1", Profile.PLASMA),
        new FluidDef("stargate-crystal-slurry", "594F3A", Profile.HEAVY),
        new FluidDef("biodiesel", "713D19", Profile.FUEL),
        new FluidDef("gasoline", "83361D", Profile.FUEL),
        new FluidDef("high-cetane-diesel", "8C521F", Profile.FUEL),
        new FluidDef("ethanol-gasoline", "691E17", Profile.FUEL),
        new FluidDef("aviation-kerosene-3", "713519", Profile.FUEL),
        new FluidDef("aviation-kerosene-a", "7A4E1B", Profile.FUEL),
        new FluidDef("high-octane-gasoline", "832D1D", Profile.FUEL),
        new FluidDef("natural-gas", "D1B669", Profile.GAS_FUEL),
        new FluidDef("charcoal-gas", "ADAA57", Profile.GAS_FUEL),
        new FluidDef("biogas", "B6905B", Profile.GAS_FUEL),
        new FluidDef("coal-gas", "BFAD60", Profile.GAS_FUEL),
        new FluidDef("refinery-gas", "C88F64", Profile.GAS_FUEL),
        new FluidDef("naphtha", "8C3E1F", Profile.FUEL),
        new FluidDef("liquefied-petroleum-gas", "694117", Profile.FUEL),
        new FluidDef("silicon-rock-gas", "B6885B", Profile.GAS_FUEL),
        new FluidDef("evil-gas", "BFA460", Profile.GAS_FUEL),
        new FluidDef("nitrobenzene", "A855B1", Profile.SPECIAL),
        new FluidDef("fish-oil", "8C351F", Profile.FUEL),
        new FluidDef("seed-oil", "693917", Profile.FUEL),
        new FluidDef("coal-coke", "9C4C9F", Profile.SPECIAL),
        new FluidDef("coal-tar", "504534", Profile.HEAVY),
        new FluidDef("sulfurous-coal-tar", "59413A", Profile.HEAVY),
        new FluidDef("creosote", "362B23", Profile.HEAVY),
        new FluidDef("heavy-fuel-oil", "3E3729", Profile.HEAVY),
        new FluidDef("evil-crude-oil", "7A2C1B", Profile.FUEL),
        new FluidDef("rp-1-rocket-fuel", "83451D", Profile.FUEL),
        new FluidDef("dense-hydrazine-mixed-fuel", "8C621F", Profile.FUEL),
        new FluidDef("cn3h7o3-rocket-fuel", "692B17", Profile.FUEL),
        new FluidDef("h8n4c2o4-rocket-fuel", "714219", Profile.FUEL),
        new FluidDef("uranium-based-fluid-fuel", "4A7A27", Profile.NUCLEAR_FUEL),
        new FluidDef("thorium-based-fluid-fuel", "3B832A", Profile.NUCLEAR_FUEL),
        new FluidDef("plutonium-based-fluid-fuel", "2D8C30", Profile.NUCLEAR_FUEL),
        new FluidDef("silicon-rock-based-fluid-fuel-mk-i", "3A6921", Profile.NUCLEAR_FUEL),
        new FluidDef("silicon-rock-based-fluid-fuel-mk-ii", "2E7124", Profile.NUCLEAR_FUEL),
        new FluidDef("silicon-rock-based-fluid-fuel-mk-iii", "277A2F", Profile.NUCLEAR_FUEL),
        new FluidDef("silicon-rock-based-fluid-fuel-mk-iv", "43832A", Profile.NUCLEAR_FUEL),
        new FluidDef("silicon-rock-based-fluid-fuel-mk-v", "338C2D", Profile.NUCLEAR_FUEL),
        new FluidDef("silicon-rock-based-fluid-fuel-mk-vi", "21692D", Profile.NUCLEAR_FUEL),
        new FluidDef("industrial-hydrogen-chloride", "538C3B", Profile.TOXIC_GAS),
        new FluidDef("chlorosulfonic-acid", "A7A332", Profile.ACID)
    };

    public static void load() {
        if (!liquids.isEmpty()) return;

        /*
         * MainMod 当前顺序是 ModLiquids.load() -> GtLiquids.load()。
         * 因此这里先确保 GtLiquids 已加载；GtLiquids.load() 本身带幂等保护，
         * MainMod 后面再次调用不会重复注册。
         */
        GtLiquids.load();

        // ---- 复用原版内容 ----
        water = Liquids.water;
        ozone = Liquids.ozone;
        petroleum = Liquids.oil;

        // ---- 保留原 ModLiquids 已有 steam ----
        steam = new Liquid("steam", Color.lightGray) {
            gas = true;
            temperature = 0.9f;
            viscosity = 0.2f;
        };

        // ---- 原 ModLiquids 的 distilled_water 不再重复注册，改用现有 GT 蒸馏水 ----
        distilledWater = GtLiquids.distilledWater;

        // ---- 将 15 个已有内容绑定到 204 项统一访问表 ----
        bind("water", Liquids.water);
        bind("distilled_water", GtLiquids.distilledWater);
        bind("steam", steam);
        bind("ozone", Liquids.ozone);
        bind("hydrochloric-acid", GtLiquids.hydrochloricAcid);
        bind("sulfuric-acid", GtLiquids.sulfuricAcid);
        bind("sodium-persulfate", GtLiquids.sodiumPersulfate);
        bind("methane", GtLiquids.methane);
        bind("ethylene", GtLiquids.ethylene);
        bind("petroleum", Liquids.oil);
        bind("lubricant", GtLiquids.lubricant);
        bind("light-fuel-oil", GtLiquids.lightFuel);
        bind("diesel", GtLiquids.diesel);
        bind("biomass", GtLiquids.biomass);
        bind("mercury", GtLiquids.mercury);

        // ---- 只注册真正缺失的 189 项 ----
        for (FluidDef def : NEW_FLUID_DATA) {
            Liquid liquid = new Liquid(def.id, Color.valueOf(def.color));
            applyProfile(liquid, def.profile);
            bind(def.id, liquid);
        }

        AllWater = new Liquid[]{
            Liquids.water,
            distilledWater,
            get("filtered-clear-water"),
            get("ozone-treated-water"),
            get("flocculated-settled-water"),
            get("acid-base-neutralized-water"),
            get("extreme-temperature-purified-water"),
            get("uv-irradiated-water"),
            get("deionized-degassed-water"),
            get("ideal-particulate-water")
        };

        if (liquids.size() != 204) {
            throw new IllegalStateException("Expected 204 liquid aliases, got " + liquids.size());
        }

        Log.info("ModLiquids loaded: 204 aliases, 189 newly registered, 15 reused.");
    }

    private static void bind(String id, Liquid liquid) {
        if (liquid == null) {
            throw new IllegalStateException("Cannot bind null liquid: " + id);
        }
        if (liquids.put(id, liquid) != null) {
            throw new IllegalArgumentException("Duplicate liquid alias: " + id);
        }
    }

    /**
     * 以下字段均来自 Mindustry Liquid 官方 API。
     * 数值用于游戏表现/平衡，不宣称等同于现实物性。
     */
    private static void applyProfile(Liquid liquid, Profile profile) {
        switch (profile) {
            case PURIFIED_WATER:
                liquid.heatCapacity = 0.45f;
                liquid.temperature = 0.48f;
                liquid.viscosity = 0.45f;
                liquid.boilPoint = 0.50f;
                liquid.effect = StatusEffects.wet;
                liquid.gasColor = Color.grays(0.93f);
                break;

            case STEAM:
                liquid.gas = true;
                liquid.temperature = 0.90f;
                liquid.heatCapacity = 0.35f;
                liquid.viscosity = 0.20f;
                liquid.coolant = false;
                break;

            case SUPERHEATED_STEAM:
                liquid.gas = true;
                liquid.temperature = 1.00f;
                liquid.heatCapacity = 0.28f;
                liquid.viscosity = 0.12f;
                liquid.coolant = false;
                break;

            case GAS:
                liquid.gas = true;
                liquid.temperature = 0.52f;
                liquid.heatCapacity = 0.28f;
                liquid.viscosity = 0.10f;
                liquid.coolant = false;
                break;

            case TOXIC_GAS:
                liquid.gas = true;
                liquid.temperature = 0.55f;
                liquid.heatCapacity = 0.22f;
                liquid.viscosity = 0.10f;
                liquid.explosiveness = 0.15f;
                liquid.coolant = false;
                break;

            case GAS_FUEL:
                liquid.gas = true;
                liquid.temperature = 0.55f;
                liquid.heatCapacity = 0.22f;
                liquid.viscosity = 0.08f;
                liquid.flammability = 1.10f;
                liquid.explosiveness = 0.85f;
                liquid.coolant = false;
                break;

            case OXIDIZER:
                liquid.temperature = 0.52f;
                liquid.heatCapacity = 0.30f;
                liquid.viscosity = 0.42f;
                liquid.explosiveness = 0.35f;
                liquid.coolant = false;
                break;

            case ACID:
                liquid.temperature = 0.55f;
                liquid.heatCapacity = 0.32f;
                liquid.viscosity = 0.58f;
                liquid.explosiveness = 0.08f;
                liquid.coolant = false;
                break;

            case CHEMICAL:
                liquid.temperature = 0.52f;
                liquid.heatCapacity = 0.34f;
                liquid.viscosity = 0.55f;
                liquid.coolant = false;
                break;

            case ORGANIC:
                liquid.temperature = 0.52f;
                liquid.heatCapacity = 0.30f;
                liquid.viscosity = 0.38f;
                liquid.flammability = 0.65f;
                liquid.explosiveness = 0.28f;
                liquid.coolant = false;
                break;

            case FUEL:
                liquid.temperature = 0.55f;
                liquid.heatCapacity = 0.35f;
                liquid.viscosity = 0.62f;
                liquid.flammability = 1.10f;
                liquid.explosiveness = 0.75f;
                liquid.effect = StatusEffects.tarred;
                liquid.coolant = false;
                break;

            case HEAVY:
                liquid.temperature = 0.56f;
                liquid.heatCapacity = 0.40f;
                liquid.viscosity = 0.88f;
                liquid.flammability = 0.75f;
                liquid.explosiveness = 0.40f;
                liquid.effect = StatusEffects.tarred;
                liquid.coolant = false;
                break;

            case CRYO:
                liquid.temperature = 0.10f;
                liquid.heatCapacity = 0.95f;
                liquid.viscosity = 0.48f;
                liquid.boilPoint = 0.55f;
                liquid.effect = StatusEffects.freezing;
                break;

            case MOLTEN:
                liquid.temperature = 1.00f;
                liquid.heatCapacity = 0.30f;
                liquid.viscosity = 0.72f;
                liquid.flammability = 0.30f;
                liquid.explosiveness = 0.30f;
                liquid.effect = StatusEffects.melting;
                liquid.coolant = false;
                liquid.lightColor = liquid.color.cpy().a(0.25f);
                break;

            case BIO:
                liquid.temperature = 0.52f;
                liquid.heatCapacity = 0.45f;
                liquid.viscosity = 0.68f;
                liquid.coolant = false;
                break;

            case SPECIAL:
                liquid.temperature = 0.50f;
                liquid.heatCapacity = 0.55f;
                liquid.viscosity = 0.50f;
                break;

            case PLASMA:
                liquid.gas = true;
                liquid.temperature = 1.00f;
                liquid.heatCapacity = 0.18f;
                liquid.viscosity = 0.05f;
                liquid.explosiveness = 1.30f;
                liquid.flammability = 0.60f;
                liquid.coolant = false;
                liquid.lightColor = liquid.color.cpy().a(0.35f);
                break;

            case WASTE:
                liquid.temperature = 0.58f;
                liquid.heatCapacity = 0.25f;
                liquid.viscosity = 0.72f;
                liquid.coolant = false;
                break;

            case NUCLEAR_FUEL:
                liquid.temperature = 0.66f;
                liquid.heatCapacity = 0.38f;
                liquid.viscosity = 0.70f;
                liquid.flammability = 0.40f;
                liquid.explosiveness = 0.85f;
                liquid.coolant = false;
                break;
        }
    }

    /** 通过清单语义 ID 取得流体。 */
    public static Liquid get(String id) {
        return liquids.get(id);
    }
}
