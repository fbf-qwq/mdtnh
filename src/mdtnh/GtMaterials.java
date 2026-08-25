package mdtnh;

import arc.graphics.Color;
import mindustry.type.Item;

import java.util.Locale;

/**
 * GT 主流程缺失物品与矿物处理中间态。
 * 已经由 ModItems/MaterialDefinition 生成的锭、板、杆、线、齿轮等不会重复创建。
 */
public final class GtMaterials {
    private GtMaterials() {}

    public static final String[] ORE_METALS = {
            "iron", "copper", "tin", "lead", "gold", "silver", "nickel", "zinc", "aluminum"
    };

    public static final String[] EARLY_SMELTABLE_METALS = {
            "iron", "copper", "tin", "lead", "gold", "silver", "nickel", "zinc"
    };

    public static void load() {
        for (String id : ORE_METALS) {
            Color base = ModItems.materialColors.get(id);
            if (base == null) base = Color.gray.cpy();
            add(id + "_raw-ore", id + "-raw-ore", base.cpy().mul(0.82f), 1.1f);
            add(id + "_crushed-ore", id + "-crushed-ore", base.cpy().mul(0.90f), 0.9f);
            add(id + "_purified-crushed-ore", id + "-purified-crushed-ore", base.cpy().mul(1.06f), 1.0f);
            add(id + "_centrifuged-crushed-ore", id + "-centrifuged-crushed-ore", base.cpy().mul(0.96f), 1.0f);
            add(id + "_impure-powder", id + "-impure-powder", base.cpy().mul(0.78f), 0.55f);
            add(id + "_pure-powder", id + "-pure-powder", base.cpy().mul(1.12f), 0.60f);
        }

        add("sticky_resin", "gt-sticky-resin", Color.valueOf("D79A46"), 0.7f);
        add("raw_rubber", "gt-raw-rubber", Color.valueOf("4C4C4C"), 0.8f);
        add("rubber_sheet", "gt-rubber-sheet", Color.valueOf("303030"), 1.0f);
        add("rubber_ring", "gt-rubber-ring", Color.valueOf("262626"), 0.35f);
        add("plastic_sheet", "gt-plastic-sheet", Color.valueOf("E8E8E8"), 1.2f);
        add("silicon_wafer", "gt-silicon-wafer", Color.valueOf("A8A8B0"), 1.2f);
        add("etched_silicon_wafer", "gt-etched-silicon-wafer", Color.valueOf("8A8AA0"), 1.6f);
        add("printed_circuit_board", "gt-printed-circuit-board", Color.valueOf("3E8B55"), 1.6f);
        add("basic_electronic_circuit", "gt-basic-electronic-circuit", Color.valueOf("62C26F"), 2.2f);
        add("advanced_electronic_circuit", "gt-advanced-electronic-circuit", Color.valueOf("4EA8C9"), 3.0f);
        add("processor", "gt-processor", Color.valueOf("78BBD8"), 4.0f);
        add("empty_cell", "gt-empty-cell", Color.valueOf("D0D0D0"), 0.5f);
        add("hydrogen_cell", "gt-hydrogen-cell", Color.valueOf("EDF9FF"), 0.8f);
        add("oxygen_cell", "gt-oxygen-cell", Color.valueOf("9BC7FF"), 0.8f);
        add("diesel_cell", "gt-diesel-cell", Color.valueOf("D8B43D"), 0.9f);
        add("bio_culture", "gt-bio-culture", Color.valueOf("8BCB62"), 2.5f);
        add("bio_sample", "gt-bio-sample", Color.valueOf("A8D676"), 1.3f);
        add("sulfur_dust", "gt-sulfur-dust", Color.valueOf("E4D84D"), 0.6f);
        add("salt_dust", "gt-salt-dust", Color.valueOf("EEEEEE"), 0.5f);
        add("machine_hull_lv", "gt-machine-hull-lv", Color.valueOf("8A8A8A"), 3.0f);
        add("electric_motor_lv", "gt-electric-motor-lv", Color.valueOf("B0B0B0"), 2.0f);
        add("electric_pump_lv", "gt-electric-pump-lv", Color.valueOf("7FAFC6"), 2.4f);
        add("conveyor_module_lv", "gt-conveyor-module-lv", Color.valueOf("5C5C5C"), 2.4f);
        add("electric_piston_lv", "gt-electric-piston-lv", Color.valueOf("9B9B9B"), 2.4f);
        add("robot_arm_lv", "gt-robot-arm-lv", Color.valueOf("A8A8A8"), 3.2f);
        add("workbench_machine_kit", "gt-workbench-machine-kit", Color.valueOf("8E98A0"), 3.0f);
    }

    public static Item get(String key) {
        return ModItems.items.get(key);
    }

    public static Item require(String key) {
        Item item = get(key);
        if (item == null) throw new IllegalStateException("Missing GT item: " + key);
        return item;
    }

    public static Item rawOreForDrop(Item drop) {
        if (drop == null || drop.name == null) return null;
        String name = drop.name.toLowerCase(Locale.ROOT);
        for (String id : ORE_METALS) {
            String normalized = id.toLowerCase(Locale.ROOT);
            boolean matches = name.contains(normalized);
            if (id.equals("aluminum")) matches |= name.contains("aluminium");
            if (matches) return get(id + "_raw-ore");
        }
        return null;
    }

    private static void add(String key, String internalName, Color color, float cost) {
        if (ModItems.items.containsKey(key)) return;
        Item item = new Item(internalName, color);
        item.cost = cost;
        ModItems.items.put(key, item);
    }
}
