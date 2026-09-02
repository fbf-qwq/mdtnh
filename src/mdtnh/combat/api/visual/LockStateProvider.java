package mdtnh.combat.api.visual;

import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public interface LockStateProvider {
    Teamc lockTarget(Unit unit);

    /** 0 = just acquired, 1 = fully locked. */
    float lockProgress(Unit unit);
}
