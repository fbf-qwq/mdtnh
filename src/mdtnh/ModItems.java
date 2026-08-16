package mdtnh;

import arc.graphics.Color;
import arc.util.Log;
import mdtnh.material.MaterialDefinition;
import mindustry.type.Item;

import java.util.*;

public class ModItems {

    public static Map<String, Item> items = new HashMap<>();
    public static Item tinyPileOfDarkAsh;

    // ==================== 材料数据定义（完整列出） ====================

    /** 金属数据：[ID, 颜色十六进制字符串, 形态模式] */
    private static final Object[][] METAL_DATA = {
            // 特殊颜色（前四种）
            {"iron",        "#C0C0C0", "mechanical"},
            {"copper",      "#B87333", "full"},
            {"lead",        "#6B6B6B", "basic"},
            {"tin",         "#C0D0D0", "pipe"},

            // 其余金属（按字母顺序）
            {"actinium",    "#C0C0C0", "mechanical"},
            {"aluminum",    "#D9DAD9", "wire"},
            {"americium",   "#C0C0C0", "mechanical"},
            {"antimony",    "#A0B0C8", "mechanical"},
            {"rodium",      "#C0C0C0", "mechanical"},
            {"berkelium",   "#C0C0C0", "mechanical"},
            {"beryllium",   "#A0A0A0", "mechanical"},
            {"bismuth",     "#C8C0C0", "mechanical"},
            {"bohrium",     "#C0C0C0", "mechanical"},
            {"cadmium",     "#C8D0D8", "mechanical"},
            {"calcium",     "#D0D0C8", "mechanical"},
            {"californium", "#C0C0C0", "mechanical"},
            {"cerium",      "#C8C8C8", "mechanical"},
            {"cesium",      "#C8C0B0", "mechanical"},
            {"chromium",    "#C0C8D0", "mechanical"},
            {"cobalt",      "#C8D0D8", "mechanical"},
            {"copernicium", "#C0C0C0", "mechanical"},
            {"curium",      "#C0C0C0", "mechanical"},
            {"darmstadtium","#C0C0C0", "mechanical"},
            {"dubnium",     "#C0C0C0", "mechanical"},
            {"dysprosium",  "#C8C8C8", "mechanical"},
            {"einsteinium", "#C0C0C0", "mechanical"},
            {"erbium",      "#C8C0C0", "mechanical"},
            {"europium",    "#C8C0B0", "mechanical"},
            {"fermium",     "#C0C0C0", "mechanical"},
            {"flerovium",   "#C0C0C0", "mechanical"},
            {"francium",    "#C8C8C8", "mechanical"},
            {"gadolinium",  "#C0C0C0", "mechanical"},
            {"gallium",     "#C8C8D0", "mechanical"},
            {"gold",        "#FFD700", "wire"},
            {"hafnium",     "#C8C8C8", "mechanical"},
            {"hassium",     "#C0C0C0", "mechanical"},
            {"holmium",     "#C8C8C8", "mechanical"},
            {"indium",      "#C0C8C8", "mechanical"},
            {"iridium",     "#D0D0D0", "mechanical"},
            {"lanthanum",   "#C0C0C0", "mechanical"},
            {"lawrencium",  "#C0C0C0", "mechanical"},
            {"lithium",     "#C8C8C8", "mechanical"},
            {"livermorium", "#C0C0C0", "mechanical"},
            {"lutetium",    "#C0C0C0", "mechanical"},
            {"magnesium",   "#D0D0D0", "mechanical"},
            {"mendelevium", "#C0C0C0", "mechanical"},
            {"manganese",   "#C8C0C0", "mechanical"},
            {"meitnerium",  "#C0C0C0", "mechanical"},
            {"mercury",     "#D0D0D0", "mechanical"},
            {"molybdenum",  "#C8C8C8", "mechanical"},
            {"moscovium",   "#C8C8C8", "mechanical"},
            {"neodymium",   "#C8C0C0", "mechanical"},
            {"neptunium",   "#C0C0C0", "mechanical"},
            {"nickel",      "#C8C8C0", "mechanical"},
            {"niobium",     "#C8C8C8", "mechanical"},
            {"nobelium",    "#C0C0C0", "mechanical"},
            {"osmium",      "#B0C8D8", "mechanical"},
            {"palladium",   "#C8C8C8", "mechanical"},
            {"polonium",    "#C8C8C8", "mechanical"},
            {"platinum",    "#D0D0D0", "wire"},
            {"plutonium",   "#C8C8C8", "mechanical"},
            {"potassium",   "#C8C8C8", "mechanical"},
            {"praseodymium","#C8C8A0", "mechanical"},
            {"promethium",  "#C0C0C0", "mechanical"},
            {"protactinium","#C8C8C8", "mechanical"},
            {"radium",      "#D0D0D0", "mechanical"},
            {"rhodium",     "#C8C8C8", "mechanical"},
            {"roentgenium", "#C0C0C0", "mechanical"},
            {"rubidium",    "#C8C8C8", "mechanical"},
            {"ruthenium",   "#C8C8C8", "mechanical"},
            {"rutherfordium","#C0C0C0","mechanical"},
            {"samarium",    "#C8C8C8", "mechanical"},
            {"scandium",    "#C8C8C8", "mechanical"},
            {"seaborgium",  "#C0C0C0", "mechanical"},
            {"silver",      "#C0C0C0", "wire"},
            {"sodium",      "#C8C8C8", "mechanical"},
            {"strontium",   "#C8C8C0", "mechanical"},
            {"tantalum",    "#B0C0D0", "mechanical"},
            {"technetium",  "#C8C8C8", "mechanical"},
            {"terbium",     "#C8C8C8", "mechanical"},
            {"thallium",    "#B0C8D0", "mechanical"},
            {"thorium",     "#C0C0C0", "mechanical"},
            {"thulium",     "#C8C8C8", "mechanical"},
            {"titanium",    "#C8C8C8", "pipe"},
            {"tungsten",    "#C8C8C8", "pipe"},
            {"uranium",     "#C8C8C8", "mechanical"},
            {"vanadium",    "#C8C8C8", "mechanical"},
            {"ytterbium",   "#C8C8C8", "mechanical"},
            {"yttrium",     "#C8C8C8", "mechanical"},
            {"zinc",        "#C8D0D8", "mechanical"},
            {"zirconium",   "#C8C8C8", "mechanical"}
    };

    /** 合金数据：[ID, 颜色十六进制字符串, 形态模式] */
    private static final Object[][] ALLOY_DATA = {
            {"annealedCopper",                            "#B87333", "mechanical"},
            {"batteryAlloy",                              "#A0A0A0", "mechanical"},
            {"brass",                                     "#C8A850", "wire"},
            {"bronze",                                    "#B88040", "pipe"},
            {"cupronickel",                               "#C8C8B0", "wire"},
            {"electrum",                                  "#D8C850", "wire"},
            {"invar",                                     "#B8B8B8", "pipe"},
            {"kanthal",                                   "#A0A0A0", "pipe"},
            {"magnesiumAluminumAlloy",                    "#C8C8C8", "mechanical"},
            {"nichrome",                                  "#B8B8B8", "mechanical"},
            {"niobiumTitaniumAlloy",                      "#B8B8C0", "mechanical"},
            {"crudePlatinum",                             "#C0C0C0", "mechanical"},
            {"sterlingSilver",                            "#C8C8C8", "mechanical"},
            {"roseGold",                                  "#D0A080", "mechanical"},
            {"blackBronze",                               "#5A4A3A", "mechanical"},
            {"bismuthBronze",                             "#A89888", "mechanical"},
            {"rutheniumTungstenMolybdenumAlloy",          "#A8A8A8", "mechanical"},
            {"rutheniumIridiumAlloy",                     "#B8B8C0", "mechanical"},
            {"solder",                                    "#A8A8A8", "mechanical"},
            {"stainlessSteel",                            "#C8C8C8", "pipe"},
            {"steel",                                     "#A0A0A0", "pipe"},
            {"tinIronAlloy",                              "#B0B8B8", "mechanical"},
            {"hastelloy",                                 "#A8A8A8", "pipe"},
            {"vanadiumGalliumAlloy",                      "#B0B0B0", "mechanical"},
            {"wroughtIron",                               "#787878", "mechanical"},
            {"iridiumOsmiumAlloy",                        "#B0B8C0", "mechanical"},
            {"sodiumPotassiumAlloy",                      "#B8B8B8", "mechanical"},
            {"magnetizedIron",                            "#686868", "mechanical"},
            {"magnetizedNeodymium",                       "#686868", "mechanical"},
            {"magnetizedSamarium",                        "#686868", "mechanical"},
            {"indiumTinBariumTitaniumCopperOxideAlloy",   "#808080", "mechanical"},
            {"uraniumRhodiumNaquadahAlloy",               "#808080", "mechanical"},
            {"enrichedNaquadahKaijinEuropiumKenguraniumAlloy","#808080","mechanical"},
            {"inertMetalMixture",                         "#888888", "mechanical"},
            {"metalMixture",                              "#888888", "mechanical"},
            {"blackSteel",                                "#505050", "mechanical"},
            {"damascusSteel",                             "#707070", "mechanical"},
            {"tungstenSteel",                             "#808080", "mechanical"},
            {"cobaltBrass",                               "#B09850", "mechanical"},
            {"magnetizedSteel",                           "#585858", "mechanical"},
            {"vanadiumSteel",                             "#888888", "mechanical"},
            {"crudeBronzeAlloy",                          "#A07848", "mechanical"},
            {"naquadahAlloy",                             "#808080", "mechanical"},
            {"crudePalladium",                            "#A8A8A8", "mechanical"},
            {"rareMetalMixture",                          "#888888", "mechanical"},
            {"rhodiumPlatedPalladium",                    "#B8B8B8", "mechanical"},
            {"redSteel",                                  "#8A4040", "mechanical"},
            {"blueSteel",                                 "#404080", "mechanical"},
            {"highSpeedSteelG",                           "#909090", "mechanical"},
            {"redAlloy",                                  "#A04040", "mechanical"},
            {"highSpeedSteelE",                           "#909090", "mechanical"},
            {"highSpeedSteelS",                           "#909090", "mechanical"},
            {"iridiumSlag",                               "#707070", "mechanical"},
            {"blueAlloy",                                 "#4060A0", "mechanical"},
            {"hslaSteel",                                 "#888888", "mechanical"},
            {"waterproofSteel",                           "#808080", "mechanical"},
            {"heatResistantChromiumIronAlloyMa956",       "#888888", "mechanical"},
            {"maragingSteel300",                          "#888888", "mechanical"},
            {"hastelloyX",                                "#888888", "mechanical"},
            {"stellite100",                               "#888888", "mechanical"},
            {"hastelloyC276",                             "#888888", "mechanical"}
    };

    // ==================== 加载核心 ====================

    public static void load() {
        // 1. 注册所有金属
        for (Object[] entry : METAL_DATA) {
            String id = (String) entry[0];
            Color color = Color.valueOf((String) entry[1]);
            String mode = (String) entry[2];
            MaterialDefinition def = createDefinition(id, color, mode);
            registerMaterial(def);
        }

        // 2. 注册所有合金
        for (Object[] entry : ALLOY_DATA) {
            String id = (String) entry[0];
            Color color = Color.valueOf((String) entry[1]);
            String mode = (String) entry[2];
            MaterialDefinition def = createDefinition(id, color, mode);
            registerMaterial(def);
        }

        // 3. 特殊物品（独立于材料）
        items.put("nano_swarm", new Item("nano-swarm", Color.valueOf("00FFAA")) {{
            radioactivity = 0.7f;
            cost = 5.0f;
        }});
        items.put("hot_ingot", new Item("hot-ingot", Color.valueOf("FF4500")) {{
            flammability = 0.3f;
            cost = 2.0f;
        }});
        tinyPileOfDarkAsh = new Item("tiny-pile-of-dark-ash", Color.valueOf("000000"));

        Log.info("ModItems loaded, total items: " + items.size());
    }

    // ==================== 辅助方法 ====================

    private static MaterialDefinition createDefinition(String id, Color color, String mode) {
        switch (mode) {
            case "basic":       return MaterialDefinition.basic(id, color);
            case "mechanical":  return MaterialDefinition.mechanical(id, color);
            case "wire":        return MaterialDefinition.wire(id, color);
            case "pipe":        return MaterialDefinition.pipe(id, color);
            case "full":        return MaterialDefinition.full(id, color);
            default:            return MaterialDefinition.basic(id, color);
        }
    }

    private static void registerMaterial(MaterialDefinition def) {
        String id = def.getId();
        Color base = def.getColor();

        for (String form : def.getForms()) {
            // 根据形态名称决定颜色乘数和 cost
            float colorMul = 1.0f;
            float cost = 1.0f;

            // 通过形态名称识别
            switch (form) {
                case "ingot":               colorMul = 1.0f; cost = 1.2f; break;
                case "block":               colorMul = 0.7f; cost = 3.0f; break;
                case "nugget":              colorMul = 1.2f; cost = 0.2f; break;
                case "powder":              colorMul = 0.9f; cost = 0.5f; break;
                case "small-pile-powder":   colorMul = 0.8f; cost = 0.15f; break;
                case "pinch-powder":        colorMul = 0.7f; cost = 0.08f; break;
                case "plate":               colorMul = 1.1f; cost = 1.8f; break;
                case "foil":                colorMul = 1.3f; cost = 0.8f; break;
                case "rod":                 colorMul = 1.1f; cost = 1.8f; break;
                case "dense_plate":         colorMul = 0.6f; cost = 4.5f; break;
                case "super_dense_plate":   colorMul = 0.4f; cost = 8.0f; break;
                case "2x_plate":            colorMul = 0.9f; cost = 3.6f; break;
                case "3x_plate":            colorMul = 0.85f; cost = 5.4f; break;
                case "4x_plate":            colorMul = 0.8f; cost = 7.2f; break;
                case "5x_plate":            colorMul = 0.75f; cost = 9.0f; break;
                case "long-rod":            colorMul = 0.85f; cost = 1.2f; break;
                case "bolt":                colorMul = 1.0f; cost = 0.3f; break;
                case "screw":               colorMul = 0.95f; cost = 0.25f; break;
                case "ring":                colorMul = 0.9f; cost = 0.4f; break;
                case "round":               colorMul = 0.8f; cost = 0.5f; break;
                case "spring":              colorMul = 1.1f; cost = 0.7f; break;
                case "small-spring":        colorMul = 1.2f; cost = 0.35f; break;
                case "gear":                colorMul = 0.8f; cost = 1.5f; break;
                case "small-gear":          colorMul = 0.9f; cost = 0.75f; break;
                case "casing":              colorMul = 0.7f; cost = 2.0f; break;
                case "rotor":               colorMul = 0.8f; cost = 2.5f; break;
                case "railing":             colorMul = 0.6f; cost = 1.2f; break;
                case "frame":               colorMul = 0.5f; cost = 3.0f; break;
                case "nano_swarm":          colorMul = 1.5f; cost = 12.0f; break;
                case "hot_ingot":           colorMul = 1.6f; cost = 2.4f; break;
                case "fine-wire":           colorMul = 1.3f; cost = 0.2f; break;
                // 导线/线缆/管道：形态名以 "wire-", "cable-", "fluid-pipe-" 开头，需要解析数字
                default:
                    if (form.startsWith("wire-")) {
                        int count = Integer.parseInt(form.substring(5));
                        colorMul = 1.0f;
                        cost = 0.3f * count;
                    } else if (form.startsWith("cable-")) {
                        int count = Integer.parseInt(form.substring(6));
                        colorMul = 0.9f;
                        cost = 0.5f * count;
                    } else if (form.startsWith("fluid-pipe-")) {
                        String size = form.substring(11);
                        float pipeCost;
                        switch (size) {
                            case "micro":  pipeCost = 0.5f; break;
                            case "small":  pipeCost = 1.0f; break;
                            case "medium": pipeCost = 2.0f; break;
                            case "large":  pipeCost = 4.0f; break;
                            case "giant":  pipeCost = 8.0f; break;
                            case "quad":   pipeCost = 16.0f; break;
                            case "nine":   pipeCost = 32.0f; break;
                            default:       pipeCost = 1.0f; break;
                        }
                        colorMul = 0.8f;
                        cost = pipeCost;
                    } else {
                        // 未识别的形态，使用默认值
                        colorMul = 1.0f;
                        cost = 1.0f;
                    }
                    break;
            }

            Color finalColor = base.cpy().mul(colorMul);
            float finalCost = cost;

            String key = id + "_" + form;
            if (!items.containsKey(key)) {
                Item item = new Item(id + "-" + form, finalColor) {{
                    this.cost = finalCost;
                }};
                items.put(key, item);
            } else {
                Log.warn("Duplicate item key: " + key);
            }
        }
    }

    // ==================== 便捷获取 ====================

    public static Item get(String metal, String form) {
        return items.get(metal + "_" + form);
    }
}