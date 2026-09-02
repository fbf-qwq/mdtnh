package mdtnh.combat.api.support;

import mdtnh.combat.api.MechanicId;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Common contract for S01-S20 support mechanics.
 *
 * This deliberately does NOT prescribe target selection. A support ability can use
 * vanilla target search, SmartAI, the player's current target or a custom selector.
 */
public interface MdtSupportAbility {
    MechanicId mechanicId();

    SupportMode supportMode();

    /** World-unit radius used by AI/debug/UI. Return <= 0 if not range-based. */
    float supportRange(Unit source);

    /** Whether this support mechanic is allowed to affect this target. */
    boolean canSupport(Unit source, Teamc target);

    /**
     * Relative target value. Higher wins.
     * Keep this deterministic; do not use random values here.
     */
    default float supportScore(Unit source, Teamc target) {
        return 1f;
    }

    /**
     * Performs one support update.
     * Implementations define whether delta is applied continuously or only after their own cooldown.
     */
    void applySupport(Unit source, Teamc target, float delta);
}
