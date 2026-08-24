package mdtnh.modui.introduction;

import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;

/**
 * 特别鸣谢页面，显示开发者、贡献者及第三方资源致谢。
 */
public final class CreditIntroductionPage implements IntroductionPage {

    private final String id;
    private final String sectionId;
    private final String titleKey;
    private final int order;

    // 定义段落标题和内容的 bundle key 对
    private static final String[][] SECTIONS = {
            {"mdtnh.intro.credit.developers.title", "mdtnh.intro.credit.developers.body"},
            {"mdtnh.intro.credit.contributors.title", "mdtnh.intro.credit.contributors.body"},
            {"mdtnh.intro.credit.thanks.title", "mdtnh.intro.credit.thanks.body"}
    };

    public CreditIntroductionPage(String id, String sectionId, String titleKey, int order) {
        this.id = id;
        this.sectionId = sectionId;
        this.titleKey = titleKey;
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
    public int order() {
        return order;
    }

    @Override
    public void build(IntroductionContext context, Table root) {
        root.top().left();

        for (String[] pair : SECTIONS) {
            addSection(root, pair[0], pair[1]);
            root.row();
        }
    }

    private void addSection(Table root, String titleKey, String bodyKey) {
        Table section = new Table();
        section.top().left();
        section.background(Styles.black6);

        section.add(Core.bundle.get(titleKey))
                .left()
                .pad(12f)
                .padBottom(4f);

        section.row();

        Label body = new Label(Core.bundle.get(bodyKey));
        body.setWrap(true);

        section.add(body)
                .width(780f)
                .left()
                .padLeft(12f)
                .padRight(12f)
                .padBottom(12f);

        root.add(section)
                .width(810f)
                .left()
                .pad(8f);
    }
}