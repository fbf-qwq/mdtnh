package mdtnh.abilities;

import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.gen.Unit;

/**
 * Deploy after remaining almost stationary for a while.
 *
 * V1 intentionally uses a status multiplier instead of rewriting weapon ranges at runtime.
 * This keeps the mechanic robust and reusable; range-changing can be added later per weapon.
 */
public class DeployAbility extends MdtAbility {

    public float deployTime = 60f * 2f;
    public float maxMoveSpeed = 0.06f;

    protected boolean active;

    public DeployAbility() {
        super("deploy");
    }

    public DeployAbility(float deployTime) {
        this();
        this.deployTime = deployTime;
    }

    @Override
    public void update(Unit unit) {
        boolean still = unit.vel().len2() <= maxMoveSpeed * maxMoveSpeed;

        if (still) {
            data += Time.delta;
        } else {
            data = 0f;
        }

        active = data >= deployTime;
        if (active) {
            unit.apply(ModStatusEffects.deployed, 3f);
        }
    }
}
