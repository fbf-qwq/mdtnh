package mdtnh.abilities;

import arc.util.Time;
import mdtnh.ai.MdtSmartController;
import mdtnh.status.ModStatusEffects;
import mindustry.gen.Healthc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Rewards keeping the same target.
 * Phase 1 uses self damage/reload statuses rather than mutating individual projectile guidance.
 */
public class LockOnAbility extends MdtAbility {

    public float stage1Time = 60f * 2f;
    public float stage2Time = 60f * 5f;

    protected int lastTargetId = -1;

    public LockOnAbility() {
        super("lockon");
    }

    @Override
    public void update(Unit unit) {
        if (!(unit.controller() instanceof MdtSmartController ai)) {
            data = 0f;
            lastTargetId = -1;
            return;
        }

        Teamc target = ai.currentTarget();
        if (target == null || !(target instanceof Healthc health) || !health.isValid()) {
            data = 0f;
            lastTargetId = -1;
            return;
        }

        int id = target.id();
        if (id != lastTargetId) {
            lastTargetId = id;
            data = 0f;
        } else {
            data += Time.delta;
        }

        if (data >= stage2Time) {
            unit.apply(ModStatusEffects.lock2, 3f);
        } else if (data >= stage1Time) {
            unit.apply(ModStatusEffects.lock1, 3f);
        }
    }
}
