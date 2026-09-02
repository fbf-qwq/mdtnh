package mdtnh.ai;

import mdtnh.debug.MdtAIDebug;
import arc.Events;
import mindustry.game.EventType.WorldLoadEvent;

/** Installs lifecycle hooks for the cached combat-AI services. */
public final class MdtAISystem {

    private static boolean installed;

    public static void install() {
        if (installed) return;
        installed = true;

        MdtAIDebug.install();

        Events.on(WorldLoadEvent.class, event -> {
            ThreatManager.clear();
            BattleCommander.clear();
        });
    }

    private MdtAISystem() {
    }
}
