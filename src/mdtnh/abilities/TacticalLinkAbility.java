package mdtnh.abilities;

import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/**
 * Enables target sharing for SmartAI units in a local network.
 * The actual shared-target decision is implemented inside SmartAI.
 */
public class TacticalLinkAbility extends MdtAbility {

    public float range = 160f;
    public float refreshInterval = 15f;

    protected float timer;

    public TacticalLinkAbility() {
        super("tacticallink");
    }

    @Override
    public void update(Unit unit) {
        timer += Time.delta;
        if (timer < refreshInterval) return;
        timer = 0f;

        unit.apply(ModStatusEffects.tacticalLinked, refreshInterval + 5f);
        Units.nearby(unit.team, unit.x, unit.y, range, other -> {
            other.apply(ModStatusEffects.tacticalLinked, refreshInterval + 5f);
        });
    }
}
