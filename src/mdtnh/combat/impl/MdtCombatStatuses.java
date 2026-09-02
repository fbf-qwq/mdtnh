package mdtnh.combat.impl;

import mindustry.content.StatusEffects;
import mindustry.type.StatusEffect;

/**
 * MDTNH mechanic status aliases.
 *
 * Phase 2 intentionally stops creating content here.  Wherever a vanilla status
 * already expresses the gameplay effect we re-use it directly.  This also avoids
 * duplicate-content crashes when the project already owns similarly-named status
 * effects in ModStatusEffects.
 */
public final class MdtCombatStatuses {
    public static final StatusEffect
        overheated = StatusEffects.disarmed,
        suppressed = StatusEffects.electrified,
        jammed = StatusEffects.electrified,
        corroded = StatusEffects.corroded,
        vectorAssist = StatusEffects.fast,
        stabilized = StatusEffects.overclock,
        phaseCorridor = StatusEffects.fast,
        marked = StatusEffects.shocked,
        buildAssist = StatusEffects.overclock,
        formation = StatusEffects.overclock,
        deployedBoost = StatusEffects.overclock,
        heatBoost = StatusEffects.overclock,
        momentumBoost = StatusEffects.fast,
        lastStandBoost = StatusEffects.overclock;

    /** Kept for source compatibility. No content is registered here. */
    public static void load() {
    }

    private MdtCombatStatuses() {
    }
}
