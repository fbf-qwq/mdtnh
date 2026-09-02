package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface ArmorStateProvider {
    int armorPlatesRemaining(Unit unit);
    int armorPlatesMax(Unit unit);
}
