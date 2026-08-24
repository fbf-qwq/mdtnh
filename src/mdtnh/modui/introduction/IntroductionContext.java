package mdtnh.modui.introduction;

import arc.Core;
import mdtnh.modui.recipeui.ItemIntroductionPage;
import mdtnh.modui.recipeui.RecipeQueryUI;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.Liquid;

/**
 * 页面构建时使用的受控上下文。
 */
public final class IntroductionContext {

    private final MdtIntroductionUI owner;

    IntroductionContext(
            MdtIntroductionUI owner) {

        this.owner = owner;
    }

    public String pageId() {
        return owner.currentPageId();
    }

    public String sectionId() {
        return owner.currentSectionId();
    }

    public void rebuild() {
        Core.app.post(owner::rebuild);
    }

    public void openPage(String pageId) {
        owner.navigateToPage(pageId);
    }

    public void openContent(
            UnlockableContent content) {

        owner.navigateToContent(content);
    }

    public void openRecipeIntroduction(
            UnlockableContent content) {

        if (!(content instanceof Item) &&
                !(content instanceof Liquid)) {
            return;
        }

        RecipeQueryUI.showPage(
                content,
                ItemIntroductionPage.ID
        );
    }

    public void putState(
            String key,
            Object value) {

        owner.putPageState(key, value);
    }

    public Object getState(String key) {
        return owner.getPageState(key);
    }

    public int getInt(
            String key,
            int fallback) {

        Object value = getState(key);

        return value instanceof Number
                ? ((Number) value).intValue()
                : fallback;
    }

    public boolean getBoolean(
            String key,
            boolean fallback) {

        Object value = getState(key);

        return value instanceof Boolean
                ? (Boolean) value
                : fallback;
    }

    public String getString(
            String key,
            String fallback) {

        Object value = getState(key);

        return value instanceof String
                ? (String) value
                : fallback;
    }

    public String text(String key) {
        return Core.bundle.get(key);
    }

    public String format(
            String key,
            Object... args) {

        return Core.bundle.format(
                key,
                args
        );
    }
}
