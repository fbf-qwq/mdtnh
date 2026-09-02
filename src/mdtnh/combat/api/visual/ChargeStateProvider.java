package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface ChargeStateProvider {
    /** Normalized stored energy/capacitor charge. */
    float chargeProgress(Unit unit);
}
