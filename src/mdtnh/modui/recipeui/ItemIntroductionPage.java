package mdtnh.modui.recipeui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Styles;

/**
 * 配方查看器中的通用“介绍”扩展页。
 *
 * <p>保留原类名与页面 ID 以兼容已有调用，但现在同时支持 Item 与 Liquid。</p>
 */
public final class ItemIntroductionPage
        implements RecipeQueryPage {

    public static final String ID =
            "mdtnh:item-introduction";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String titleKey() {
        return "mdtnh.recipe-query.page.item-introduction";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean supports(
            UnlockableContent content) {

        return content instanceof Item ||
                content instanceof Liquid;
    }

    @Override
    public void build(
            RecipeQueryUI.PageContext context,
            Table root) {

        UnlockableContent content =
                context.content();

        root.defaults().left();
        root.top().left();

        Table card = new Table();
        card.left();
        card.background(Styles.black6);

        card.image(content.uiIcon)
                .size(72f)
                .pad(10f);

        card.table(text -> {
            text.left();

            Label name =
                    new Label(
                            content.localizedName
                    );

            name.setColor(Color.white);
            name.setWrap(true);

            text.add(name)
                    .width(650f)
                    .left();

            text.row();

            text.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.introduction.internal-name",
                            content.name
                    )
            ).left()
                    .padTop(4f);
        }).left();

        root.add(card)
                .growX()
                .pad(8f);

        root.row();

        addSection(
                root,
                "mdtnh.recipe-query.introduction.description-title",
                usableText(content.description)
                        ? content.description
                        : Core.bundle.get(
                                "mdtnh.recipe-query.introduction.no-description"
                        )
        );

        if (usableText(content.details)) {
            root.row();

            addSection(
                    root,
                    "mdtnh.recipe-query.introduction.details-title",
                    content.details
            );
        }

        if (usableText(content.credit)) {
            root.row();

            addSection(
                    root,
                    "mdtnh.recipe-query.introduction.credit-title",
                    content.credit
            );
        }
    }

    private void addSection(
            Table root,
            String titleKey,
            String bodyText) {

        Table section = new Table();
        section.left();
        section.top();
        section.background(Styles.black6);

        section.add(
                Core.bundle.get(titleKey)
        ).left()
                .pad(10f)
                .padBottom(5f);

        section.row();

        Label body =
                new Label(bodyText);

        body.setWrap(true);

        section.add(body)
                .width(820f)
                .left()
                .padLeft(10f)
                .padRight(10f)
                .padBottom(12f);

        root.add(section)
                .growX()
                .padLeft(8f)
                .padRight(8f)
                .padBottom(8f);
    }

    private boolean usableText(String value) {
        return value != null &&
                !value.trim().isEmpty();
    }
}
