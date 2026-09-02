package mdtnh.abilities;

import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.gen.Unit;

/**
 * Passive firing brace.
 *
 * Unlike DeployAbility, this does not unlock special weapons or represent an active
 * tactical deployment. It is a low-complexity baseline mechanic: staying nearly still
 * stabilizes the platform and grants the hidden "braced" status.
 */
public class BraceAbility extends MdtAbility {

    public float settleTime = 45f;
    public float maxMoveSpeed = 0.08f;

    public BraceAbility() {
        super("brace");
    }

    @Override
    public void update(Unit unit) {
        if (unit.vel().len2() <= maxMoveSpeed * maxMoveSpeed) {
            data += Time.delta;
        } else {
            data = 0f;
        }

        if (data >= settleTime) {
            unit.apply(ModStatusEffects.braced, 3f);
        }
    }
}
