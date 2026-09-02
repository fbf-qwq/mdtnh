package mdtnh.combat.api.support;

import mdtnh.combat.api.MechanicId;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;

/**
 * Shared contract for interception, deflection and decoy systems.
 */
public interface MdtInterceptSupport {
    MechanicId mechanicId();

    float interceptRange(Unit source);

    /** Higher values should be handled first by the interception system. */
    float projectileThreat(Unit source, Bullet bullet);

    boolean canIntercept(Unit source, Bullet bullet);

    void intercept(Unit source, Bullet bullet);
}
