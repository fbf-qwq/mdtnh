package mdtnh.ai;

import mdtnh.debug.AIDebugBranch;
import mdtnh.debug.AIDebugState;
import mdtnh.status.ModStatusEffects;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

/**
 * Player-command-compatible wrapper for SmartAI.
 *
 * Priority model:
 *
 * 1. An explicit RTS command has movement priority and is executed by vanilla CommandAI.
 * 2. While commanded, MDTNH target scoring and all Unit abilities remain active.
 * 3. When the command path/queue finishes, the embedded SmartAI immediately resumes.
 *
 * Two movement controllers are intentionally NOT updated in the same tick. Doing that would
 * make CommandAI and SmartAI overwrite each other's movement orders.
 *
 * Because this extends CommandAI, Mindustry's RTS selection, command queue, stances,
 * serialization and command UI continue to recognize the unit as commandable.
 */
public class SmartCommandAI extends CommandAI implements MdtSmartController {

    private final AIProfile profile;
    private final SmartAI autonomous;
    private final AIDebugState commandDebug = new AIDebugState();

    private boolean commandMode;

    public SmartCommandAI(AIProfile profile) {
        this.profile = profile;
        this.autonomous = new SmartAI(profile);
    }

    @Override
    public AIProfile profile() {
        return profile;
    }

    public SmartAI autonomousAI() {
        bindAutonomous();
        return autonomous;
    }

    @Override
    public boolean commandMode() {
        return commandMode;
    }

    @Override
    public Teamc currentTarget() {
        if (commandMode) {
            if (attackTarget != null) return attackTarget;
            if (target != null) return target;
        }

        return autonomous.currentTarget();
    }

    @Override
    public AIDebugState debugState() {
        return commandMode ? commandDebug : autonomous.debugState();
    }

    @Override
    public void init() {
        super.init();
        bindAutonomous();
    }

    @Override
    public void updateUnit() {
        bindAutonomous();

        commandMode = hasExplicitCommand();

        if (commandMode) {
            updateCommandDebug();
            // Vanilla command movement/pathfinding/stances get absolute movement priority.
            super.updateUnit();
        } else {
            // No active command: preserve the unit's original SmartAI behavior.
            autonomous.updateUnit();
        }
    }

    /**
     * `CommandAI.hasCommand()` only checks targetPos. For a hybrid controller we also need
     * attack targets, queued destinations and non-move command modes.
     */
    public boolean hasExplicitCommand() {
        return targetPos != null
                || attackTarget != null
                || commandQueue.any()
                || (command != null && command != UnitCommand.moveCommand);
    }

    /**
     * Keep MDTNH target-value logic while CommandAI owns movement.
     * An explicitly commanded attack target still wins through CommandAI's normal handling.
     */
    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground) {
        if (attackTarget != null) {
            return super.findMainTarget(x, y, range, air, ground);
        }

        Teamc shared = sharedTarget(x, y, range);
        if (shared != null) return shared;

        Teamc scored = TargetScorer.findBest(unit, profile, air, ground);
        if (inRange(scored, x, y, range)) return scored;

        return super.findMainTarget(x, y, range, air, ground);
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        // Direct player attack command always has priority.
        if (nearAttackTarget(x, y, range)) {
            return super.findTarget(x, y, range, air, ground);
        }

        Teamc shared = sharedTarget(x, y, range);
        if (shared != null) return shared;

        Teamc scored = TargetScorer.findBest(unit, profile, air, ground);
        if (inRange(scored, x, y, range)) return scored;

        return super.findTarget(x, y, range, air, ground);
    }

    private Teamc sharedTarget(float x, float y, float range) {
        if (!unit.hasEffect(ModStatusEffects.tacticalLinked)) return null;

        final Teamc[] best = {null};
        final float[] bestScore = {-Float.MAX_VALUE};

        Units.nearby(unit.team, unit.x, unit.y, profile.tacticalLinkRange, ally -> {
            if (ally == unit || !(ally.controller() instanceof MdtSmartController smart)) return;

            Teamc allyTarget = smart.currentTarget();
            if (!inRange(allyTarget, x, y, range)) return;
            if (Units.invalidateTarget(allyTarget, unit.team, x, y)) return;

            float score = TargetScorer.score(unit, allyTarget, profile);

            if (score > bestScore[0]) {
                best[0] = allyTarget;
                bestScore[0] = score;
            }
        });

        return best[0];
    }

    private boolean inRange(Teamc value, float x, float y, float range) {
        if (value == null) return false;

        float extra = targetHitSize(value) / 2f;
        return value.within(x, y, range + extra + 3f);
    }


    private float targetHitSize(Teamc value) {
        if (value instanceof Unit other) {
            return other.hitSize;
        }

        if (value instanceof Building build) {
            return build.block.size * Vars.tilesize;
        }

        return 0f;
    }

    private void bindAutonomous() {
        if (autonomous.unit() != unit) {
            autonomous.unit(unit);
        }
    }

    private void updateCommandDebug() {
        commandDebug.begin(
                profile.role,
                BattleCommander.strategy(unit.team),
                attackTarget != null ? attackTarget : target,
                false,
                mindustry.Vars.state == null ? 0f : arc.util.Time.time
        );

        commandDebug.safeTarget = null;

        if (attackTarget != null) {
            commandDebug.branch = AIDebugBranch.COMMAND_ATTACK;
            commandDebug.route(attackTarget.getX(), attackTarget.getY());
            commandDebug.preferredRange = Math.max(0f, unit.range());
        } else if (targetPos != null) {
            commandDebug.branch = AIDebugBranch.COMMAND_MOVE;
            commandDebug.route(targetPos.x, targetPos.y);
        } else {
            commandDebug.branch = AIDebugBranch.COMMAND_SPECIAL;
        }
    }
}
