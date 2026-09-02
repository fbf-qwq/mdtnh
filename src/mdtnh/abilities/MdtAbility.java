package mdtnh.abilities;

import mindustry.entities.abilities.Ability;

/**
 * Common base for MDTNH abilities.
 * getBundle() is namespaced so all visible text stays in bundle files.
 */
public abstract class MdtAbility extends Ability {

    private final String bundleKey;

    protected MdtAbility(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    @Override
    public String getBundle() {
        return "ability.mdtnh-" + bundleKey;
    }
}
