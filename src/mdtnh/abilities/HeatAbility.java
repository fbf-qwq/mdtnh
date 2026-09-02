package mdtnh.abilities;

import arc.math.Mathf;
import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.gen.Unit;

/**
 * Sustained fire builds heat:
 * warm -> higher output; extreme heat -> temporary disarm until cooled.
 */
public class HeatAbility extends MdtAbility {

    public float heatTime = 60f * 6f;
    public float coolTime = 60f * 5f;
    public float warmThreshold = 0.5f;
    public float overheatThreshold = 0.98f;
    public float recoverThreshold = 0.72f;

    protected boolean overheated;

    public HeatAbility() {
        super("heat");
    }

    @Override
    public void update(Unit unit) {
        if (unit.isShooting() && !overheated) {
            data += Time.delta / heatTime;
        } else {
            data -= Time.delta / coolTime;
        }

        data = Mathf.clamp(data);

        if (!overheated && data >= overheatThreshold) {
            overheated = true;
        } else if (overheated && data <= recoverThreshold) {
            overheated = false;
        }

        if (overheated) {
            unit.apply(ModStatusEffects.overheated, 3f);
        } else if (data >= warmThreshold) {
            unit.apply(ModStatusEffects.heated, 3f);
        }
    }
}
