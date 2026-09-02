package mdtnh.combat.api.support;

import mdtnh.combat.api.MechanicId;
import mindustry.gen.Unit;

/**
 * Contract for an area-support mechanic that does not need one selected Teamc.
 */
public interface MdtAreaSupport {
    MechanicId mechanicId();

    float supportRange(Unit source);

    void applyArea(Unit source, float delta);
}
