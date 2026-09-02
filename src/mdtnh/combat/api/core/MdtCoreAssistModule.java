package mdtnh.combat.api.core;

import mdtnh.combat.api.MechanicId;
import mindustry.gen.Unit;

/**
 * Interface for C01-C15 core-machine assistance.
 *
 * Design invariant:
 * - may control assistantUnit;
 * - may inspect playerUnit and PlayerIntentSnapshot;
 * - must NOT move, rotate, aim, shoot or enqueue builds for playerUnit.
 */
public interface MdtCoreAssistModule {
    MechanicId mechanicId();

    /** Whether this module currently has something useful to do for the player's existing intent. */
    boolean accepts(CoreAssistContext context);

    /** Runs one simulation update for the assistant. */
    void update(CoreAssistContext context);

    /** Optional debug/visual overlay. Must not change simulation state. */
    default void draw(CoreAssistContext context) {
    }

    /** Optional cleanup when the helper unit is removed/reassigned. */
    default void reset(Unit assistantUnit) {
    }
}
