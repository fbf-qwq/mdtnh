package mdtnh.abilities;

import arc.util.Time;
import mdtnh.status.ModStatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;

/** Capped same-type formation bonuses, refreshed at a low frequency. */
public class FormationAbility extends MdtAbility {

    public float range = 100f;
    public float checkInterval = 12f;

    protected float timer;

    public FormationAbility() {
        super("formation");
    }

    @Override
    public void update(Unit unit) {
        timer += Time.delta;
        if (timer < checkInterval) return;
        timer = 0f;

        final int[] allies = {0};
        Units.nearby(unit.team, unit.x, unit.y, range, other -> {
            if (other != unit && other.type == unit.type) {
                allies[0]++;
            }
        });

        if (allies[0] >= 4) {
            unit.apply(ModStatusEffects.formation4, checkInterval + 3f);
        } else if (allies[0] >= 3) {
            unit.apply(ModStatusEffects.formation3, checkInterval + 3f);
        } else if (allies[0] >= 2) {
            unit.apply(ModStatusEffects.formation2, checkInterval + 3f);
        }
    }
}
