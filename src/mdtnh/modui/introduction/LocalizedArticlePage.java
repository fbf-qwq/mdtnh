package mdtnh.modui.introduction;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;

/**
 * 仅由本地化文本构成的通用文章页。
 */
public class LocalizedArticlePage
        implements IntroductionPage {

    private final String id;
    private final String sectionId;
    private final String titleKey;
    private final String bodyKey;
    private final Drawable icon;
    private final int order;

    public LocalizedArticlePage(
            String id,
            String sectionId,
            String titleKey,
            String bodyKey,
            Drawable icon,
            int order) {

        this.id = id;
        this.sectionId = sectionId;
        this.titleKey = titleKey;
        this.bodyKey = bodyKey;
        this.icon = icon;
        this.order = order;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String sectionId() {
        return sectionId;
    }

    @Override
    public String titleKey() {
        return titleKey;
    }

    @Override
    public Drawable icon() {
        return icon;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public void build(
            IntroductionContext context,
            Table root) {

        Table article = new Table();
        article.top().left();
        article.background(Styles.black6);

        Label body =
                new Label(
                        Core.bundle.get(
                                bodyKey
                        )
                );

        body.setWrap(true);

        article.add(body)
                .width(780f)
                .left()
                .top()
                .pad(16f);

        root.add(article)
                .width(810f)
                .left()
                .top()
                .pad(8f);
    }
}
