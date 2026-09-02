package mdtnh.ai;

import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Low-frequency team-level strategy selector.
 *
 * Important Phase-1.2 rule:
 * BattleCommander is neutral (ATTACK) unless the team actually has a COMMANDER-role
 * SmartAI unit. This prevents macro strategy from silently overriding normal unit-role
 * tests and matches the intended high-tier commander concept.
 */
public final class BattleCommander {

    private static final ObjectMap<Team, State> states = new ObjectMap<>();
    public static float refreshInterval = 120f;

    public static BattleStrategy strategy(Team team) {
        State state = states.get(team, State::new);

        if (Time.time - state.lastRefresh >= refreshInterval || state.lastRefresh < 0f) {
            rebuild(team, state);
        }

        return state.strategy;
    }

    public static Unit commander(Team team) {
        State state = states.get(team, State::new);

        if (Time.time - state.lastRefresh >= refreshInterval || state.lastRefresh < 0f) {
            rebuild(team, state);
        }

        return state.commanderId < 0 ? null : Groups.unit.getByID(state.commanderId);
    }

    public static boolean active(Team team) {
        return commander(team) != null;
    }

    public static void clear() {
        states.clear();
    }

    private static void rebuild(Team team, State state) {
        int total = 0, air = 0, artillery = 0;
        float health = 0f;

        Unit commander = null;

        for (Unit unit : team.data().units) {
            if (!unit.isValid() || !(unit.controller() instanceof MdtSmartController ai)) continue;

            total++;
            health += unit.healthf();
            if (unit.isFlying()) air++;

            AIRole role = ai.profile().role;

            if (role == AIRole.COMMANDER && commander == null) {
                commander = unit;
            }

            if (role == AIRole.ARTILLERY || role == AIRole.SIEGE) {
                artillery++;
            }
        }

        // No commander: macro layer stays neutral and individual roles are fully visible.
        if (commander == null) {
            state.commanderId = -1;
            state.strategy = BattleStrategy.ATTACK;
            state.lastRefresh = Time.time;
            return;
        }

        state.commanderId = commander.id;

        int enemyAA = 0;

        for (var data : Vars.state.teams.present) {
            if (data.team == team) continue;

            for (var build : data.buildings) {
                if (build.isValid() && build.block instanceof Turret turret && turret.targetAir) {
                    enemyAA++;
                }
            }
        }

        float avgHealth = total == 0 ? 1f : health / total;
        float airRatio = total == 0 ? 0f : (float)air / total;
        float artilleryRatio = total == 0 ? 0f : (float)artillery / total;

        float spread = 0f;
        int spreadCount = 0;

        for (Unit unit : team.data().units) {
            if (!unit.isValid()
                    || unit == commander
                    || !(unit.controller() instanceof MdtSmartController)) continue;

            spread += unit.dst(commander);
            spreadCount++;
        }

        float averageSpread = spreadCount == 0 ? 0f : spread / spreadCount;

        if (total >= 4 && avgHealth < 0.32f) {
            state.strategy = BattleStrategy.RETREAT;
        } else if (total >= 5 && averageSpread > 300f) {
            state.strategy = BattleStrategy.REGROUP;
        } else if (total >= 4 && airRatio > 0.45f && enemyAA >= 3) {
            state.strategy = BattleStrategy.FLANK;
        } else if (total >= 4 && artilleryRatio > 0.28f) {
            state.strategy = BattleStrategy.SIEGE;
        } else {
            state.strategy = BattleStrategy.ATTACK;
        }

        state.lastRefresh = Time.time;
    }

    private static final class State {
        BattleStrategy strategy = BattleStrategy.ATTACK;
        int commanderId = -1;
        float lastRefresh = -1f;
    }

    private BattleCommander() {
    }
}
