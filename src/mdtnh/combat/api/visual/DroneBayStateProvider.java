package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface DroneBayStateProvider {
    int activeDrones(Unit unit);
    int maxDrones(Unit unit);
}
