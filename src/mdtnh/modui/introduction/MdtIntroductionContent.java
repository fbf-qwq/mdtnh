package mdtnh.modui.introduction;

import mdtnh.VoltageTier;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;

/**
 * MDT 介绍菜单的内置板块与页面注册。
 */
public final class MdtIntroductionContent {

    public static final String SECTION_CONTENT =
            "mdtnh:content";
    public static final String SECTION_PRODUCTION =
            "mdtnh:production";
    public static final String SECTION_VOLTAGE =
            "mdtnh:voltage";
    public static final String SECTION_UI =
            "mdtnh:ui";
    public static final String SECTION_GUIDE =
            "mdtnh:guide";
    public static final String SECTION_ABOUT =
            "mdtnh:about";

    public static final String PAGE_CONTENT_ITEMS =
            "mdtnh:content:items";
    public static final String PAGE_CONTENT_LIQUIDS =
            "mdtnh:content:liquids";
    public static final String PAGE_CONTENT_BLOCKS =
            "mdtnh:content:blocks";
    public static final String PAGE_CONTENT_UNITS =
            "mdtnh:content:units";
    public static final String PAGE_CONTENT_STATUS =
            "mdtnh:content:status";

    public static final String PAGE_PRODUCTION_OVERVIEW =
            "mdtnh:production:overview";

    public static final String PAGE_UI_BUILD_MENU =
            "mdtnh:ui:build-menu";
    public static final String PAGE_UI_QUICK_BAR =
            "mdtnh:ui:quick-bar";
    public static final String PAGE_UI_RECIPE_QUERY =
            "mdtnh:ui:recipe-query";
    public static final String PAGE_UI_INTRO_MENU =
            "mdtnh:ui:introduction-menu";

    public static final String PAGE_GUIDE_TOUR =
            "mdtnh:guide:tour";
    public static final String PAGE_GUIDE_EXTENSION =
            "mdtnh:guide:extension";
    public static final String PAGE_CREDITS =
            "mdtnh:about:credits";
    public static final String PAGE_SUPPORT =
            "mdtnh:about:support";

    private static boolean loaded;

    private MdtIntroductionContent() {
    }

    public static synchronized void load(
            IntroductionRegistry registry) {

        if (loaded) return;
        loaded = true;

        registry.section(
                SECTION_CONTENT,
                "mdtnh.intro.section.content",
                null,
                0
        );

        registry.section(
                SECTION_PRODUCTION,
                "mdtnh.intro.section.production",
                null,
                100
        );

        registry.section(
                SECTION_VOLTAGE,
                "mdtnh.intro.section.voltage",
                null,
                200
        );

        registry.section(
                SECTION_UI,
                "mdtnh.intro.section.ui",
                null,
                300
        );

        registry.section(
                SECTION_GUIDE,
                "mdtnh.intro.section.guide",
                null,
                400
        );

        registry.section(
                SECTION_ABOUT,
                "mdtnh.intro.section.about",
                null,
                500
        );

        registry.registerPage(
                new ContentCatalogPage(
                        ContentCatalogPage.Kind.item,
                        PAGE_CONTENT_ITEMS,
                        "mdtnh.intro.content.items",
                        0
                )
        );

        registry.registerPage(
                new ContentCatalogPage(
                        ContentCatalogPage.Kind.liquid,
                        PAGE_CONTENT_LIQUIDS,
                        "mdtnh.intro.content.liquids",
                        100
                )
        );

        registry.registerPage(
                new ContentCatalogPage(
                        ContentCatalogPage.Kind.block,
                        PAGE_CONTENT_BLOCKS,
                        "mdtnh.intro.content.blocks",
                        200
                )
        );

        registry.registerPage(
                new ContentCatalogPage(
                        ContentCatalogPage.Kind.unit,
                        PAGE_CONTENT_UNITS,
                        "mdtnh.intro.content.units",
                        300
                )
        );

        registry.registerPage(
                new ContentCatalogPage(
                        ContentCatalogPage.Kind.status,
                        PAGE_CONTENT_STATUS,
                        "mdtnh.intro.content.status",
                        400
                )
        );

        registry.registerPage(
                new LocalizedArticlePage(
                        PAGE_PRODUCTION_OVERVIEW,
                        SECTION_PRODUCTION,
                        "mdtnh.intro.production.overview.title",
                        "mdtnh.intro.production.overview.body",
                        null,
                        0
                )
        );

        for (VoltageTier tier :
                VoltageTier.values()) {

            registry.registerPage(
                    new VoltageTierIntroductionPage(
                            tier
                    )
            );
        }

        registry.registerPage(
                new LocalizedArticlePage(
                        PAGE_UI_BUILD_MENU,
                        SECTION_UI,
                        "mdtnh.intro.ui.build-menu.title",
                        "mdtnh.intro.ui.build-menu.body",
                        null,
                        0
                )
        );

        registry.registerPage(
                new LocalizedArticlePage(
                        PAGE_UI_QUICK_BAR,
                        SECTION_UI,
                        "mdtnh.intro.ui.quick-bar.title",
                        "mdtnh.intro.ui.quick-bar.body",
                        null,
                        100
                )
        );

        registry.registerPage(
                new LocalizedArticlePage(
                        PAGE_UI_RECIPE_QUERY,
                        SECTION_UI,
                        "mdtnh.intro.ui.recipe-query.title",
                        "mdtnh.intro.ui.recipe-query.body",
                        null,
                        200
                )
        );

        registry.registerPage(
                new LocalizedArticlePage(
                        PAGE_UI_INTRO_MENU,
                        SECTION_UI,
                        "mdtnh.intro.ui.introduction-menu.title",
                        "mdtnh.intro.ui.introduction-menu.body",
                        null,
                        300
                )
        );

        registry.registerPage(
                new GuidedTourPage()
        );

        registry.registerPage(
                new LocalizedArticlePage(
                        PAGE_GUIDE_EXTENSION,
                        SECTION_GUIDE,
                        "mdtnh.intro.guide.extension.title",
                        "mdtnh.intro.guide.extension.body",
                        null,
                        100
                )
        );
        registry.registerPage(
                new CreditIntroductionPage(
                        PAGE_CREDITS,
                        SECTION_ABOUT,
                        "mdtnh.intro.credit.title",
                        0
                )
        );
        registry.registerPage(
                new SupportPage(
                        PAGE_SUPPORT,
                        SECTION_ABOUT,
                        "mdtnh.intro.support.title",
                        100
                )
        );
    }

    public static String pageIdForContent(
            UnlockableContent content) {

        if (content instanceof Item) {
            return PAGE_CONTENT_ITEMS;
        }

        if (content instanceof Liquid) {
            return PAGE_CONTENT_LIQUIDS;
        }

        if (content instanceof Block) {
            return PAGE_CONTENT_BLOCKS;
        }

        if (content instanceof UnitType) {
            return PAGE_CONTENT_UNITS;
        }

        if (content instanceof StatusEffect) {
            return PAGE_CONTENT_STATUS;
        }

        return null;
    }
}
