package mdtnh.combat.api.resource;

import mdtnh.combat.api.visual.HeatStateProvider;
import mindustry.gen.Unit;

/** Mutable heat contract used by support units without depending on one concrete HeatAbility class. */
public interface HeatResource extends HeatStateProvider {
    /** Adds normalized heat. Negative values cool the unit. */
    void addHeat(Unit unit, float amount);
}
