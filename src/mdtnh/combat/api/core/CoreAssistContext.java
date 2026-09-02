package mdtnh.combat.api.core;

import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.gen.Unit;

/**
 * Runtime context for a core-assistant module.
 * "assistantUnit" is the helper/core machine itself.
 * "playerUnit" is observational input and must not be commandeered by modules.
 */
public final class CoreAssistContext {
    public Player player;
    public Unit playerUnit;
    public Unit assistantUnit;
    public Team team;

    public PlayerIntentSnapshot intent;

    public boolean valid() {
        return playerUnit != null && assistantUnit != null && team != null && intent != null;
    }
}
