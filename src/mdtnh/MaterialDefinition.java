package mdtnh.material;

import arc.graphics.Color;
import java.util.*;

/**
 * 材料定义：存储材料的 ID、颜色以及可生成的所有物品形态。
 * 提供预设形态组合（BASIC、MECHANICAL、WIRE、PIPE、SPECIAL）和工厂方法。
 */
public class MaterialDefinition {

    private final String id;
    private final Color color;
    private final Set<String> forms = new HashSet<>();

    // ==================== 预设形态组合 ====================

    /** 基础形态（所有材料都应具备） */
    public static final List<String> BASIC_FORMS = List.of(
            "ingot", "block", "nugget",
            "powder", "small-pile-powder", "pinch-powder",
            "plate", "foil", "rod",
            "dense_plate", "super_dense_plate",
            "2x_plate", "3x_plate", "4x_plate", "5x_plate"
    );

    /** 机械零件形态 */
    public static final List<String> MECHANICAL_FORMS = List.of(
            "long-rod", "bolt", "screw", "ring", "round",
            "spring", "small-spring",
            "gear", "small-gear",
            "casing", "rotor", "railing", "frame"
    );

    /** 导线/线缆形态（含细导线、多芯导线、线缆） */
    public static final List<String> WIRE_FORMS = List.of(
            "fine-wire",
            "wire-1", "wire-2", "wire-4", "wire-8", "wire-12", "wire-16",
            "cable-1", "cable-2", "cable-4", "cable-8", "cable-12", "cable-16"
    );

    /** 流体管道形态（7种尺寸） */
    public static final List<String> PIPE_FORMS = List.of(
            "fluid-pipe-micro", "fluid-pipe-small", "fluid-pipe-medium",
            "fluid-pipe-large", "fluid-pipe-giant",
            "fluid-pipe-quad", "fluid-pipe-nine"
    );

    /** 特殊形态（纳米蜂群、热锭） */
    public static final List<String> SPECIAL_FORMS = List.of(
            "nano_swarm", "hot_ingot"
    );

    // ==================== 构造函数 ====================

    public MaterialDefinition(String id, Color color) {
        this.id = id;
        this.color = color;
    }

    // ==================== 形态管理 ====================

    public MaterialDefinition addForms(String... formNames) {
        forms.addAll(Arrays.asList(formNames));
        return this;
    }

    public MaterialDefinition addForms(Collection<String> formNames) {
        forms.addAll(formNames);
        return this;
    }

    public MaterialDefinition removeForm(String formName) {
        forms.remove(formName);
        return this;
    }

    // ==================== 预设工厂方法 ====================

    /** 仅基础形态 */
    public static MaterialDefinition basic(String id, Color color) {
        return new MaterialDefinition(id, color).addForms(BASIC_FORMS);
    }

    /** 基础 + 机械零件 */
    public static MaterialDefinition mechanical(String id, Color color) {
        return new MaterialDefinition(id, color)
                .addForms(BASIC_FORMS)
                .addForms(MECHANICAL_FORMS);
    }

    /** 基础 + 机械 + 导线 */
    public static MaterialDefinition wire(String id, Color color) {
        return new MaterialDefinition(id, color)
                .addForms(BASIC_FORMS)
                .addForms(MECHANICAL_FORMS)
                .addForms(WIRE_FORMS);
    }

    /** 基础 + 机械 + 管道 */
    public static MaterialDefinition pipe(String id, Color color) {
        return new MaterialDefinition(id, color)
                .addForms(BASIC_FORMS)
                .addForms(MECHANICAL_FORMS)
                .addForms(PIPE_FORMS);
    }

    /** 全形态（基础 + 机械 + 导线 + 管道 + 特殊） */
    public static MaterialDefinition full(String id, Color color) {
        return new MaterialDefinition(id, color)
                .addForms(BASIC_FORMS)
                .addForms(MECHANICAL_FORMS)
                .addForms(WIRE_FORMS)
                .addForms(PIPE_FORMS)
                .addForms(SPECIAL_FORMS);
    }

    /** 自定义组合（传入多个预设列表） */
    @SafeVarargs
    public static MaterialDefinition custom(String id, Color color, List<String>... formGroups) {
        MaterialDefinition def = new MaterialDefinition(id, color);
        for (List<String> group : formGroups) {
            def.addForms(group);
        }
        return def;
    }

    // ==================== Getter ====================

    public String getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public Set<String> getForms() {
        return Collections.unmodifiableSet(forms);
    }

    @Override
    public String toString() {
        return String.format("MaterialDefinition{id='%s', forms=%d}", id, forms.size());
    }
}