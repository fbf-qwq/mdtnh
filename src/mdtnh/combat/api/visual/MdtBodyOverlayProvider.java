package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

/**
 * Draws body-attached sprite parts immediately after the body and before weapons.
 * Use this for independent armor plates or other structural overlays that should not
 * cover turret/weapon sprites.
 */
public interface MdtBodyOverlayProvider {
    void drawBodyOverlay(Unit unit);
}
