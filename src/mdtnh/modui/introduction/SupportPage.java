package mdtnh.modui.introduction;

import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;

import java.util.Objects;

public final class SupportPage implements IntroductionPage {
    private final String id;
    private final String sectionId;
    private final String titleKey;
    private final int order;

    // 定义段落标题和内容的 bundle key 对
    private static final String[][] SECTIONS = {
            {"mdtnh.intro.support.star.title", "mdtnh.intro.support.star.body"},
            {"mdtnh.intro.support.donate.title", "mdtnh.intro.support.donate.body"}
    };

    public SupportPage(String id, String sectionId, String titleKey, int order) {
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
            if(Objects.equals(pair[1], "mdtnh.intro.support.star.body"))
               root.button(
                                Core.bundle.get("mdtnh.intro.support.star.url"),
                                Styles.flatTogglet,
                                () -> Core.app.openURI("https://github.com/fbf-qwq/mdtnh")
                        ).width(200f)
                        .height(48f)
                        .pad(8f)
                        .left();
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
