package mdtnh.abilities;

import mdtnh.ai.MdtSmartController;
import mdtnh.status.ModStatusEffects;
import mindustry.gen.Healthc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Limited low-health execution bonus.
 * Intended for single-target hunter units; it is deliberately not an instant kill.
 */
public class ExecutionAbility extends MdtAbility {

    public float threshold = 0.20f;

    public ExecutionAbility() {
        super("execution");
    }

    @Override
    public void update(Unit unit) {
        if (!(unit.controller() instanceof MdtSmartController ai)) return;

        Teamc target = ai.currentTarget();
        if (target instanceof Healthc health && health.isValid() && health.healthf() <= threshold) {
            unit.apply(ModStatusEffects.execution, 3f);
        }
    }
}
