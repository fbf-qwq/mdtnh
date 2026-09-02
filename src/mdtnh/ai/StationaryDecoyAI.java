package mdtnh.ai;

import mindustry.entities.units.AIController;

/**
 * Chaff/decoy controller: intentionally never moves, never searches for targets,
 * and does not run FlyingAI wobble/movement behavior.
 */
public class StationaryDecoyAI extends AIController {
    @Override
    public void updateUnit() {
        unit.vel.setZero();
        stopShooting();
    }

    @Override
    public boolean isLogicControllable() {
        return false;
    }
}
