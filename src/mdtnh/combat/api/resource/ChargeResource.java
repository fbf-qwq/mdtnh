package mdtnh.combat.api.resource;

import mdtnh.combat.api.visual.ChargeStateProvider;
import mindustry.gen.Unit;

/** Mutable charge/capacitor contract used by transfer and dump mechanics. */
public interface ChargeResource extends ChargeStateProvider {
    /** Adds normalized charge. Negative values drain the capacitor. */
    void addCharge(Unit unit, float amount);
}
