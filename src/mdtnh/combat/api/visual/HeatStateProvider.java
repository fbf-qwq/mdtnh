package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface HeatStateProvider {
    /** Normalized heat: 0 = cold, 1 = overheat threshold. */
    float heatProgress(Unit unit);

    default boolean overheated(Unit unit) {
        return heatProgress(unit) >= 1f;
    }
}
