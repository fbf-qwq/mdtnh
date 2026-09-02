package mdtnh.combat.api.visual;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Tmp;
import mindustry.entities.abilities.Ability;
import mindustry.gen.UnderwaterMovec;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

/**
 * UnitType with a tiny but important extension:
 * an Ability may replace the body region at draw time.
 *
 * This is the hook used by Deploy -> "-deployed.png" and any future
 * damaged/phase/alternate-body state.
 */
public class MdtVisualUnitType extends UnitType {
    public MdtVisualUnitType(String name) {
        super(name);
    }

    @Override
    public void drawBody(Unit unit) {
        TextureRegion selected = region;
        float bestPriority = -Float.MAX_VALUE;

        for (Ability ability : unit.abilities) {
            if (ability instanceof MdtUnitVisualProvider provider) {
                TextureRegion candidate = provider.bodyRegion(unit);
                float priority = provider.bodyPriority(unit);

                if (candidate != null && candidate.found() && priority >= bestPriority) {
                    selected = candidate;
                    bestPriority = priority;
                }
            }
        }

        applyColor(unit);

        // Preserve vanilla underwater body tint behavior.
        if (unit instanceof UnderwaterMovec) {
            Draw.alpha(1f);
            Draw.mixcol(unit.floorOn().mapColor.write(Tmp.c1).mul(0.9f), 1f);
        }

        Draw.rect(selected, unit.x, unit.y, unit.rotation - 90f);
        Draw.reset();

        // Structural overlays belong with the body, underneath weapons.
        for (Ability ability : unit.abilities) {
            if (ability instanceof MdtBodyOverlayProvider provider) {
                provider.drawBodyOverlay(unit);
                Draw.reset();
            }
        }
    }
}
