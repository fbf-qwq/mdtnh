package mdtnh.ai;

import mdtnh.debug.AIDebugState;
import mindustry.gen.Teamc;

/**
 * Common view of MDTNH autonomous and command-compatible controllers.
 *
 * The important distinction is:
 * - SmartAI: autonomous movement.
 * - SmartCommandAI: vanilla CommandAI movement while an RTS command exists,
 *   then SmartAI fallback when the command finishes.
 */
public interface MdtSmartController {

    AIProfile profile();

    Teamc currentTarget();

    AIDebugState debugState();

    /** True only while a direct RTS command currently has movement/command priority. */
    default boolean commandMode() {
        return false;
    }
}
