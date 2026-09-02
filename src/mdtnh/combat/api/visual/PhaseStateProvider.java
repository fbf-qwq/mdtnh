package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface PhaseStateProvider {
    boolean phased(Unit unit);

    /** 0..1, used by optional ghost/phase rendering. */
    default float phaseStrength(Unit unit) {
        return phased(unit) ? 1f : 0f;
    }
}
