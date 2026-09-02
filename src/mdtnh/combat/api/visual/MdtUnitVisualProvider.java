package mdtnh.combat.api.visual;

import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Unit;

/**
 * Optional visual contract implemented by an Ability.
 * MdtVisualUnitType asks every unit Ability for a body-region override.
 *
 * Returning null means "use the normal unit body".
 */
public interface MdtUnitVisualProvider {
    default TextureRegion bodyRegion(Unit unit) {
        return null;
    }

    /** Higher priority wins when multiple abilities want to replace the body sprite. */
    default float bodyPriority(Unit unit) {
        return 0f;
    }
}
