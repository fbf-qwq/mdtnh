package mdtnh.modui.recipeui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.ui.Styles;

/**
 * “物品介绍”示例扩展页。
 *
 * <p>注意：这个页面没有被 RecipeQueryUI 特判；它和第三方页面一样，
 * 完全通过 RecipeQueryPage + registerPage(...) 接入。</p>
 */
public final class ItemIntroductionPage implements RecipeQueryPage {

    public static final String ID = "mdtnh:item-introduction";

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
    public boolean supports(UnlockableContent content) {
        return content instanceof Item;
    }

    @Override
    public void build(
            RecipeQueryUI.PageContext context,
            Table root) {

        Item item = (Item) context.content();

        root.defaults().left();
        root.top().left();

        Table card = new Table();
        card.left();
        card.background(Styles.black6);

        card.image(item.uiIcon)
                .size(72f)
                .pad(10f);

        card.table(text -> {
            text.left();

            Label name = new Label(item.localizedName);
            name.setColor(Color.white);
            name.setWrap(true);

            text.add(name)
                    .width(650f)
                    .left();

            text.row();

            text.add(
                    Core.bundle.format(
                            "mdtnh.recipe-query.introduction.internal-name",
                            item.name
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
                usableText(item.description)
                        ? item.description
                        : Core.bundle.get(
                                "mdtnh.recipe-query.introduction.no-description"
                        )
        );

        if (usableText(item.details)) {
            root.row();
            addSection(
                    root,
                    "mdtnh.recipe-query.introduction.details-title",
                    item.details
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

        Label body = new Label(bodyText);
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
        return value != null && !value.trim().isEmpty();
    }
}
