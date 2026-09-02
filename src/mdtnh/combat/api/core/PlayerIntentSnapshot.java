package mdtnh.combat.api.core;

import mindustry.entities.units.BuildPlan;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;

/**
 * Read-only snapshot of what the player is already doing.
 *
 * Core assistants are expected to REACT to this object.
 * They must not write movement/aim/build commands into playerUnit.
 */
public final class PlayerIntentSnapshot {
    public PlayerIntentKind kind = PlayerIntentKind.IDLE;

    public Unit playerUnit;
    public Teamc combatTarget;
    public Tile miningTile;
    public BuildPlan buildPlan;

    public float moveX;
    public float moveY;
    public boolean shooting;
    public boolean boosting;

    public void clearTransient() {
        combatTarget = null;
        miningTile = null;
        buildPlan = null;
        moveX = 0f;
        moveY = 0f;
        shooting = false;
        boosting = false;
        kind = PlayerIntentKind.IDLE;
    }
}
