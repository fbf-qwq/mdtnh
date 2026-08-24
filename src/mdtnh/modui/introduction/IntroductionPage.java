package mdtnh.modui.introduction;

import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;

/**
 * 介绍菜单的页面扩展接口。
 *
 * <p>所有玩家可见文本都应通过 bundle key 获取。</p>
 */
public interface IntroductionPage {

    String id();

    String sectionId();

    String titleKey();

    default Drawable icon() {
        return null;
    }

    default int order() {
        return 1000;
    }

    default boolean visible() {
        return true;
    }

    void build(IntroductionContext context, Table root);
}
