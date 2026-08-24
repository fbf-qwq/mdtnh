package mdtnh.modui.introduction;

import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mdtnh.VoltageTier;
import mindustry.ui.Styles;

import java.util.Locale;

/**
 * 单个电压等级的介绍/教程页。
 */
public final class VoltageTierIntroductionPage
        implements IntroductionPage {

    private final VoltageTier tier;
    private final String suffix;

    public VoltageTierIntroductionPage(
            VoltageTier tier) {

        this.tier = tier;

        this.suffix =
                tier.name().toLowerCase(
                        Locale.ROOT
                );
    }

    @Override
    public String id() {
        return "mdtnh:voltage:" +
                suffix;
    }

    @Override
    public String sectionId() {
        return MdtIntroductionContent
                .SECTION_VOLTAGE;
    }

    @Override
    public String titleKey() {
        return "voltage-tier." +
                suffix;
    }

    @Override
    public int order() {
        return tier.ordinal();
    }

    @Override
    public void build(
            IntroductionContext context,
            Table root) {

        root.top().left();

        Table summary = new Table();
        summary.top().left();
        summary.background(Styles.black6);

        summary.add(
                Core.bundle.format(
                        "mdtnh.intro.voltage.heading",
                        Core.bundle.get(
                                titleKey()
                        )
                )
        ).left()
                .pad(12f);

        summary.row();

        summary.add(
                Core.bundle.format(
                        "mdtnh.intro.voltage.range",
                        number(
                                tier.minVoltageV
                        ),
                        number(
                                tier.maxVoltageV
                        )
                )
        ).left()
                .padLeft(12f)
                .padBottom(5f);

        summary.row();

        summary.add(
                Core.bundle.format(
                        "mdtnh.intro.voltage.capacity",
                        number(
                                tier.capacityJ
                        )
                )
        ).left()
                .padLeft(12f)
                .padBottom(12f);

        root.add(summary)
                .width(810f)
                .left()
                .pad(8f);

        root.row();

        addArticle(
                root,
                "mdtnh.intro.voltage.summary-title",
                "mdtnh.intro.voltage." +
                        suffix +
                        ".summary"
        );

        root.row();

        addArticle(
                root,
                "mdtnh.intro.voltage.tutorial-title",
                "mdtnh.intro.voltage." +
                        suffix +
                        ".tutorial"
        );
    }

    private void addArticle(
            Table root,
            String titleKey,
            String bodyKey) {

        Table section = new Table();
        section.top().left();
        section.background(Styles.black6);

        section.add(
                Core.bundle.get(
                        titleKey
                )
        ).left()
                .pad(12f)
                .padBottom(4f);

        section.row();

        Label body =
                new Label(
                        Core.bundle.get(
                                bodyKey
                        )
                );

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

    private String number(float value) {
        if (Math.abs(
                value - Math.round(value)
        ) < 0.0001f) {

            return Long.toString(
                    Math.round(value)
            );
        }

        return Strings.autoFixed(
                value,
                2
        );
    }
}
