package mdtnh.modui.recipeui;

import arc.scene.ui.layout.Table;
import mindustry.ctype.UnlockableContent;

/**
 * RecipeQueryUI 的自定义页面扩展接口。
 *
 * <p>一个实现对应顶部的一个“页面种类”。页面标题必须通过 titleKey()
 * 指向 bundle 本地化键，不应在 Java 中硬编码可见文本。</p>
 *
 * <p>注册：</p>
 * <pre>
 * RecipeQueryUI.registerPage(new MyQueryPage());
 * </pre>
 */
public interface RecipeQueryPage {

    /**
     * 页面唯一 ID。
     * 建议使用带模组命名空间的稳定 ID，例如 "mdtnh:item-introduction"。
     */
    String id();

    /** 顶部页面按钮使用的 bundle key。 */
    String titleKey();

    /**
     * 排序值越小越靠前。
     * 内置“配方/用途”始终位于扩展页之前。
     */
    default int order() {
        return 1000;
    }

    /** 当前内容是否支持显示此页面。 */
    default boolean supports(UnlockableContent content) {
        return content != null;
    }

    /**
     * 构建页面主体。
     *
     * @param context 查询上下文，可用于递归导航、R/U 悬浮绑定和状态保存
     * @param root    页面根 Table
     */
    void build(RecipeQueryUI.PageContext context, Table root);
}
