package mdtnh.combat.impl;

/** One-line integration entry point for content/runtime setup. */
public final class MdtCombatMechanics {
    private static boolean loaded;

    public static void load() {
        if (loaded) return;
        loaded = true;

        MdtCombatStatuses.load();
        MdtCombatModifiers.install();
        MdtCombatRuntime.install();
        MdtCombatHud.install();
    }

    private MdtCombatMechanics() {}
}
