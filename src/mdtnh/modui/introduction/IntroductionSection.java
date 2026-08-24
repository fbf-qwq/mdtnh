package mdtnh.modui.introduction;

import arc.scene.style.Drawable;

/**
 * 介绍菜单左侧的一级板块。
 */
public final class IntroductionSection {

    public final String id;
    public final String titleKey;
    public final Drawable icon;
    public final int order;

    public IntroductionSection(
            String id,
            String titleKey,
            Drawable icon,
            int order) {

        this.id = id;
        this.titleKey = titleKey;
        this.icon = icon;
        this.order = order;
    }
}
