package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface DeployStateProvider {
    /** 0 = fully packed, 1 = fully deployed. */
    float deployProgress(Unit unit);

    default boolean deployed(Unit unit) {
        return deployProgress(unit) >= 0.999f;
    }
}
