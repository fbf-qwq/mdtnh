package mdtnh;

import arc.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GTNH Steam/LV/MV/HV ore-material catalog used by this Mindustry port.
 *
 * <p>The catalog is data-only: player-visible names stay in bundle files.</p>
 *
 * <p>veinEligible=false does not mean the material is unavailable; it means
 * it is poor-ore-only / loose / otherwise not intended to be automatically
 * inserted into a normal MineralVein.</p>
 */
public final class GtOreCatalog {

    public static final class OreSpec {
        public final String id;
        public final String colorHex;
        public final VoltageTier processingTier;
        public final boolean veinEligible;

        private OreSpec(
                String id,
                String colorHex,
                VoltageTier processingTier,
                boolean veinEligible) {

            this.id = id;
            this.colorHex = colorHex;
            this.processingTier = processingTier;
            this.veinEligible = veinEligible;
        }

        public Color fallbackColor() {
            return Color.valueOf(colorHex);
        }
    }

    public static final class LooseDepositSpec {
        public final String id;
        public final String materialId;
        public final String colorHex;

        private LooseDepositSpec(
                String id,
                String materialId,
                String colorHex) {

            this.id = id;
            this.materialId = materialId;
            this.colorHex = colorHex;
        }

        public Color fallbackColor() {
            return Color.valueOf(colorHex);
        }
    }

    private static final OreSpec[] ores = {
            new OreSpec("coal", "303030", VoltageTier.LV, true),
            new OreSpec("lignite", "6b4b2a", VoltageTier.LV, true),
            new OreSpec("brown_limonite", "8b5a2b", VoltageTier.LV, true),
            new OreSpec("yellow_limonite", "d9a923", VoltageTier.LV, true),
            new OreSpec("banded_iron", "8f6f63", VoltageTier.LV, true),
            new OreSpec("malachite", "35a85b", VoltageTier.LV, true),
            new OreSpec("magnetite", "4b4b50", VoltageTier.LV, true),
            new OreSpec("vanadium_magnetite", "45504f", VoltageTier.LV, true),
            new OreSpec("gold", "ffd700", VoltageTier.LV, true),
            new OreSpec("chalcopyrite", "b58b2a", VoltageTier.LV, true),
            new OreSpec("iron", "c0c0c0", VoltageTier.LV, true),
            new OreSpec("pyrite", "c6aa3c", VoltageTier.LV, true),
            new OreSpec("copper", "b87333", VoltageTier.LV, true),
            new OreSpec("tin", "c0d0d0", VoltageTier.LV, true),
            new OreSpec("cassiterite", "8a6b5a", VoltageTier.LV, true),
            new OreSpec("rock_salt", "f0c7c7", VoltageTier.LV, true),
            new OreSpec("salt", "eeeeee", VoltageTier.LV, true),
            new OreSpec("lepidolite", "c08ab8", VoltageTier.LV, true),
            new OreSpec("spodumene", "d6b6d8", VoltageTier.LV, true),
            new OreSpec("redstone", "c7352f", VoltageTier.LV, true),
            new OreSpec("ruby", "d51d48", VoltageTier.LV, true),
            new OreSpec("cinnabar", "b5333b", VoltageTier.LV, true),
            new OreSpec("soapstone", "8d9b87", VoltageTier.LV, true),
            new OreSpec("talc", "d2d9c9", VoltageTier.LV, true),
            new OreSpec("glauconite", "5f8057", VoltageTier.LV, true),
            new OreSpec("pentlandite", "9c8451", VoltageTier.LV, true),
            new OreSpec("grossular", "c7844a", VoltageTier.LV, true),
            new OreSpec("spessartine", "b45a36", VoltageTier.LV, true),
            new OreSpec("pyrolusite", "53535a", VoltageTier.LV, true),
            new OreSpec("tantalite", "4c445a", VoltageTier.LV, true),
            new OreSpec("graphite", "414147", VoltageTier.LV, true),
            new OreSpec("diamond", "79e6e6", VoltageTier.LV, true),
            new OreSpec("apatite", "62b8d9", VoltageTier.LV, true),
            new OreSpec("phosphorite", "c5ba73", VoltageTier.LV, true),
            new OreSpec("phosphate", "d4ca8d", VoltageTier.LV, true),
            new OreSpec("tricalcium_phosphate", "e7e7d4", VoltageTier.LV, true),
            new OreSpec("pyrochlore", "b7a26d", VoltageTier.LV, true),
            new OreSpec("lapis", "3454c6", VoltageTier.LV, true),
            new OreSpec("lazurite", "4168d5", VoltageTier.LV, true),
            new OreSpec("sodalite", "5474c5", VoltageTier.LV, true),
            new OreSpec("calcite", "e8e1ca", VoltageTier.LV, true),
            new OreSpec("oilsands", "453a2c", VoltageTier.LV, true),
            new OreSpec("vermiculite", "8d6f46", VoltageTier.LV, true),
            new OreSpec("alunite", "b9a797", VoltageTier.LV, true),
            new OreSpec("basaltic_mineral_sand", "47464c", VoltageTier.LV, true),
            new OreSpec("granitic_mineral_sand", "8d7e79", VoltageTier.LV, true),
            new OreSpec("fullers_earth", "c9b790", VoltageTier.LV, true),
            new OreSpec("gypsum", "ded8cc", VoltageTier.LV, true),
            new OreSpec("cassiterite_sand", "8a7768", VoltageTier.LV, true),
            new OreSpec("garnet_sand", "8f443d", VoltageTier.LV, true),
            new OreSpec("asbestos", "c8c8b8", VoltageTier.LV, true),
            new OreSpec("diatomite", "d4c7ae", VoltageTier.LV, true),
            new OreSpec("kaolinite", "e4d6c7", VoltageTier.LV, true),
            new OreSpec("zeolite", "cfbca5", VoltageTier.LV, true),
            new OreSpec("glauconite_sand", "6d8c5e", VoltageTier.LV, true),
            new OreSpec("kyanite", "5377b5", VoltageTier.LV, true),
            new OreSpec("mica", "b9a89a", VoltageTier.LV, true),
            new OreSpec("pollucite", "d4c6ae", VoltageTier.LV, true),
            new OreSpec("dolomite", "dad0c2", VoltageTier.LV, true),
            new OreSpec("wollastonite", "d8d3c4", VoltageTier.LV, true),
            new OreSpec("trona", "d5e5dd", VoltageTier.LV, true),
            new OreSpec("andradite", "8f6a3d", VoltageTier.LV, true),
            new OreSpec("silver", "d7d7d7", VoltageTier.LV, true),
            new OreSpec("lead", "6b6b6b", VoltageTier.LV, true),
            new OreSpec("nickel", "c8c8c0", VoltageTier.LV, true),
            new OreSpec("zinc", "a9a9b0", VoltageTier.LV, false),
            new OreSpec("sulfur", "e4d84d", VoltageTier.LV, true),
            new OreSpec("sphalerite", "8a6f4d", VoltageTier.LV, true),
            new OreSpec("tetrahedrite", "6f5e58", VoltageTier.LV, true),
            new OreSpec("stibnite", "74747d", VoltageTier.LV, true),
            new OreSpec("nether_quartz", "eee6dc", VoltageTier.LV, true),
            new OreSpec("quartzite", "e5ded5", VoltageTier.LV, true),
            new OreSpec("barite", "dedad2", VoltageTier.LV, true),
            new OreSpec("certus_quartz", "c5e8ef", VoltageTier.LV, true),
            new OreSpec("saltpeter", "e5e4d5", VoltageTier.LV, false),
            new OreSpec("realgar", "d75b2c", VoltageTier.LV, false),
            new OreSpec("bismuthinite", "777985", VoltageTier.LV, false),
            new OreSpec("blue_stone", "5b6aa0", VoltageTier.LV, true),
            new OreSpec("amber", "f1a52b", VoltageTier.LV, false),
            new OreSpec("amethyst", "a85ad4", VoltageTier.LV, false),
            new OreSpec("blue_topaz", "58a8e6", VoltageTier.LV, false),
            new OreSpec("emerald", "42c878", VoltageTier.LV, false),
            new OreSpec("green_sapphire", "4eaa8d", VoltageTier.LV, false),
            new OreSpec("jade", "71ad86", VoltageTier.LV, false),
            new OreSpec("jasper", "a5473f", VoltageTier.LV, false),
            new OreSpec("olivine", "a4b74e", VoltageTier.LV, true),
            new OreSpec("opal", "d8e3df", VoltageTier.LV, false),
            new OreSpec("red_garnet", "9f2f3f", VoltageTier.LV, false),
            new OreSpec("spinel", "cb697b", VoltageTier.LV, false),
            new OreSpec("tanzanite", "615fc8", VoltageTier.LV, false),
            new OreSpec("topaz", "e8c04d", VoltageTier.LV, false),
            new OreSpec("sapphire", "355ec9", VoltageTier.LV, false),
            new OreSpec("yellow_garnet", "d9b542", VoltageTier.LV, false),
            new OreSpec("garnierite", "6eaa75", VoltageTier.LV, true),
            new OreSpec("cobaltite", "657a92", VoltageTier.LV, true),
            new OreSpec("cryolite", "d6e8ee", VoltageTier.LV, true),
            new OreSpec("bentonite", "c7a98c", VoltageTier.LV, true),
            new OreSpec("magnesite", "d3c8b3", VoltageTier.LV, true),
            new OreSpec("infused_air", "e6e6a8", VoltageTier.LV, true),
            new OreSpec("infused_earth", "75934f", VoltageTier.LV, true),
            new OreSpec("infused_fire", "c65d38", VoltageTier.LV, true),
            new OreSpec("infused_water", "5689c7", VoltageTier.LV, true),
            new OreSpec("infused_order", "d5d1c1", VoltageTier.LV, true),
            new OreSpec("infused_entropy", "6d5b79", VoltageTier.LV, true),
            new OreSpec("bauxite", "aa6e55", VoltageTier.HV, true),
            new OreSpec("ilmenite", "4c4f58", VoltageTier.HV, true),
            new OreSpec("aluminum", "d9dad9", VoltageTier.HV, true),
            new OreSpec("galena", "77738a", VoltageTier.HV, true),
            new OreSpec("bastnasite", "b89a66", VoltageTier.HV, true),
            new OreSpec("monazite", "b17c62", VoltageTier.HV, true),
            new OreSpec("neodymium", "c8c0c0", VoltageTier.HV, true),
            new OreSpec("wulfenite", "d8903d", VoltageTier.HV, true),
            new OreSpec("molybdenite", "63666e", VoltageTier.HV, true),
            new OreSpec("molybdenum", "c8c8c8", VoltageTier.HV, true),
            new OreSpec("powellite", "ded29b", VoltageTier.HV, true),
            new OreSpec("chromite", "4f5357", VoltageTier.HV, true),
            new OreSpec("uvarovite", "4c9a50", VoltageTier.HV, true),
            new OreSpec("perlite", "b5aca6", VoltageTier.HV, true),
            new OreSpec("meteoric_iron", "8b8b84", VoltageTier.HV, false),
            new OreSpec("beryllium", "a0a0a0", VoltageTier.HV, true),
            new OreSpec("thorium", "808a72", VoltageTier.HV, true)
    };

    private static final LooseDepositSpec[] looseDeposits = {
            new LooseDepositSpec("zinc_gravel", "zinc", "9b967d"),
            new LooseDepositSpec("clay", "clay", "a78b78"),
            new LooseDepositSpec("gravel", "gravel", "827b73"),
            new LooseDepositSpec("sand", "sand", "d7c58c")
    };

    private static final Map<String, OreSpec> byId =
            new LinkedHashMap<>();

    static {
        for (OreSpec spec : ores) {
            byId.put(spec.id, spec);
        }
    }

    private GtOreCatalog() {
    }

    public static OreSpec spec(String id) {
        return byId.get(id);
    }

    public static List<OreSpec> ores() {
        List<OreSpec> result =
                new ArrayList<>();

        Collections.addAll(result, ores);
        return Collections.unmodifiableList(result);
    }

    public static List<LooseDepositSpec> looseDeposits() {
        List<LooseDepositSpec> result =
                new ArrayList<>();

        Collections.addAll(
                result,
                looseDeposits
        );

        return Collections.unmodifiableList(result);
    }

    public static String[] processingIds() {
        Set<String> ids =
                new LinkedHashSet<>();

        for (OreSpec spec : ores) {
            ids.add(spec.id);
        }

        for (LooseDepositSpec spec :
                looseDeposits) {

            ids.add(spec.materialId);
        }

        return ids.toArray(new String[0]);
    }

    public static Color fallbackColor(
            String id) {

        OreSpec ore =
                byId.get(id);

        if (ore != null) {
            return ore.fallbackColor();
        }

        for (LooseDepositSpec loose :
                looseDeposits) {

            if (loose.materialId.equals(id)) {
                return loose.fallbackColor();
            }
        }

        return Color.gray.cpy();
    }

    public static VoltageTier processingTier(
            String id) {

        OreSpec spec =
                byId.get(id);

        return spec == null
                ? VoltageTier.LV
                : spec.processingTier;
    }
}
