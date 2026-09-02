package mdtnh.abilities;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

/**
 * Simplified shield network: nearby same-type allies increase shield regeneration.
 * There is no shared HP pool.
 */
public class ShieldLinkAbility extends MdtAbility {

    public float range = 120f;
    public float regenPerAlly = 0.18f;
    public float maxShield = 1600f;
    public int maxLinks = 4;
    public float checkInterval = 10f;

    protected float timer;
    protected int links;

    public ShieldLinkAbility() {
        super("shieldlink");
    }

    @Override
    public void update(Unit unit) {
        timer += Time.delta;
        if (timer < checkInterval) return;
        timer = 0f;

        final int[] count = {0};
        Units.nearby(unit.team, unit.x, unit.y, range, other -> {
            if (other != unit && other.type == unit.type && count[0] < maxLinks) {
                count[0]++;
            }
        });

        links = count[0];
        if (links > 0 && unit.shield < maxShield) {
            unit.shield = Math.min(maxShield, unit.shield + regenPerAlly * links * checkInterval);
        }
    }

    @Override
    public void draw(Unit unit) {
        if (links <= 0) return;

        Draw.z(Layer.shields);
        Draw.color(Pal.accent);
        Lines.stroke(0.8f);

        final int[] drawn = {0};
        Units.nearby(unit.team, unit.x, unit.y, range, other -> {
            if (other != unit && other.type == unit.type && drawn[0] < maxLinks) {
                Lines.line(unit.x, unit.y, other.x, other.y);
                drawn[0]++;
            }
        });

        Draw.reset();
    }
}
