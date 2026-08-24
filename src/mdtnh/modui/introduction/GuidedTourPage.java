package mdtnh.modui.introduction;

import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

/**
 * 介绍菜单自身的交互式导览页。
 */
public final class GuidedTourPage
        implements IntroductionPage {

    private static final String[] STEP_TITLES = {
            "mdtnh.intro.guide.step.welcome.title",
            "mdtnh.intro.guide.step.content.title",
            "mdtnh.intro.guide.step.production.title",
            "mdtnh.intro.guide.step.voltage.title",
            "mdtnh.intro.guide.step.ui.title",
            "mdtnh.intro.guide.step.extension.title"
    };

    private static final String[] STEP_BODIES = {
            "mdtnh.intro.guide.step.welcome.body",
            "mdtnh.intro.guide.step.content.body",
            "mdtnh.intro.guide.step.production.body",
            "mdtnh.intro.guide.step.voltage.body",
            "mdtnh.intro.guide.step.ui.body",
            "mdtnh.intro.guide.step.extension.body"
    };

    private static final String[] TARGET_PAGES = {
            null,
            MdtIntroductionContent.PAGE_CONTENT_ITEMS,
            MdtIntroductionContent.PAGE_PRODUCTION_OVERVIEW,
            "mdtnh:voltage:ulv",
            MdtIntroductionContent.PAGE_UI_INTRO_MENU,
            MdtIntroductionContent.PAGE_GUIDE_EXTENSION
    };

    @Override
    public String id() {
        return MdtIntroductionContent
                .PAGE_GUIDE_TOUR;
    }

    @Override
    public String sectionId() {
        return MdtIntroductionContent
                .SECTION_GUIDE;
    }

    @Override
    public String titleKey() {
        return "mdtnh.intro.guide.tour.title";
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public void build(
            IntroductionContext context,
            Table root) {

        int step = Math.max(
                0,
                Math.min(
                        context.getInt(
                                "guide.step",
                                0
                        ),
                        STEP_TITLES.length - 1
                )
        );

        Table card = new Table();
        card.top().left();
        card.background(Styles.black6);

        card.add(
                Core.bundle.format(
                        "mdtnh.intro.guide.counter",
                        step + 1,
                        STEP_TITLES.length
                )
        ).left()
                .pad(12f);

        card.row();

        card.add(
                Core.bundle.get(
                        STEP_TITLES[step]
                )
        ).left()
                .padLeft(12f)
                .padRight(12f)
                .padBottom(8f);

        card.row();

        Label body =
                new Label(
                        Core.bundle.get(
                                STEP_BODIES[step]
                        )
                );

        body.setWrap(true);

        card.add(body)
                .width(770f)
                .left()
                .padLeft(12f)
                .padRight(12f)
                .padBottom(12f);

        if (TARGET_PAGES[step] != null) {
            final String target =
                    TARGET_PAGES[step];

            card.row();

            card.button(
                    Core.bundle.get(
                            "mdtnh.intro.guide.open-related"
                    ),
                    Icon.right,
                    Styles.flatt,
                    () -> context.openPage(
                            target
                    )
            ).width(230f)
                    .height(48f)
                    .left()
                    .pad(12f);
        }

        root.add(card)
                .width(810f)
                .left()
                .top()
                .pad(8f);

        root.row();

        final int currentStep = step;

        root.table(nav -> {
            nav.button(
                    Core.bundle.get(
                            "mdtnh.intro.guide.previous"
                    ),
                    Icon.left,
                    Styles.flatt,
                    () -> {
                        if (currentStep <= 0) {
                            return;
                        }

                        context.putState(
                                "guide.step",
                                currentStep - 1
                        );

                        context.rebuild();
                    }
            ).width(180f)
                    .height(48f)
                    .disabled(button ->
                            currentStep <= 0);

            nav.add(
                    Core.bundle.format(
                            "mdtnh.intro.guide.counter",
                            currentStep + 1,
                            STEP_TITLES.length
                    )
            ).width(160f)
                    .center();

            nav.button(
                    Core.bundle.get(
                            "mdtnh.intro.guide.next"
                    ),
                    Icon.right,
                    Styles.flatt,
                    () -> {
                        if (currentStep + 1 >=
                                STEP_TITLES.length) {
                            return;
                        }

                        context.putState(
                                "guide.step",
                                currentStep + 1
                        );

                        context.rebuild();
                    }
            ).width(180f)
                    .height(48f)
                    .disabled(button ->
                            currentStep + 1 >=
                                    STEP_TITLES.length);
        }).left()
                .pad(8f);
    }
}
