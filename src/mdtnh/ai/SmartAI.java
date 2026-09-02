package mdtnh.ai;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mdtnh.debug.AIDebugBranch;
import mdtnh.debug.AIDebugState;
import mdtnh.status.ModStatusEffects;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.meta.BlockFlag;

/**
 * Reusable data-driven MDTNH combat controller.
 *
 * Phase 1.2 makes every test role behaviorally distinct and prevents the team macro layer
 * from masking individual-role behavior when no Commander is present.
 */
public class SmartAI extends AIController implements MdtSmartController {

    private final AIProfile profile;
    private final AIDebugState debug = new AIDebugState();

    private boolean retreating;

    private float bomberPassTimer;
    private float bomberPassAngle;

    private float flankRepathTimer;
    private int flankTargetId = -1;
    private int flankSide = 1;

    private final Vec2 flankPoint = new Vec2();
    private final Vec2 commanderPoint = new Vec2();

    public SmartAI(AIProfile profile) {
        this.profile = profile;
    }

    @Override
    public AIProfile profile() {
        return profile;
    }

    @Override
    public Teamc currentTarget() {
        return target;
    }

    @Override
    public AIDebugState debugState() {
        return debug;
    }

    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground) {
        Teamc shared = sharedTarget();
        if (shared != null) return shared;

        Teamc scored = TargetScorer.findBest(unit, profile, air, ground);
        return scored != null ? scored : super.findMainTarget(x, y, range, air, ground);
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        Teamc scored = TargetScorer.findBest(unit, profile, air, ground);
        return scored != null ? scored : super.findTarget(x, y, range, air, ground);
    }

    @Override
    public void updateMovement() {
        unloadPayloads();

        BattleStrategy strategy = BattleCommander.strategy(unit.team);
        updateRetreatState(strategy);

        debug.begin(profile.role, strategy, target, retreating, Time.time);

        if (retreating) {
            retreat();
            faceMovement();
            return;
        }

        // A Commander is the rally point; it should not try to regroup toward itself.
        if (strategy == BattleStrategy.REGROUP && profile.role != AIRole.COMMANDER) {
            regroup();
            faceMovement();
            return;
        }

        // Continue a bombing pass even if the original target dies during the pass.
        if (profile.role == AIRole.BOMBER && bomberPassTimer > 0f) {
            continueBomberPass();
            return;
        }

        if (target != null) {
            // Team-level FLANK is a strong bias for eligible flying units.
            if (strategy == BattleStrategy.FLANK
                    && unit.isFlying()
                    && profile.role != AIRole.ARTILLERY
                    && profile.role != AIRole.SIEGE
                    && profile.role != AIRole.COMMANDER) {
                debug.branch = AIDebugBranch.MACRO_FLANK;
                flankMovement(target);
                return;
            }

            switch (profile.role) {
                case ARTILLERY, SIEGE -> {
                    rangedMovement(target, strategy == BattleStrategy.SIEGE ? 0.97f : 0.92f);
                    faceTarget();
                }
                case SKIRMISH -> {
                    skirmishMovement(target);
                    faceTarget();
                }
                case HUNTER -> {
                    rangedMovement(target, profile.preferredRange);
                    faceTarget();
                }
                case FLANK -> flankMovement(target);
                case BOMBER -> bomberMovement(target);
                case SWARM -> {
                    swarmMovement(target);
                    faceTarget();
                }
                case COMMANDER -> {
                    commanderMovement(target);
                    faceTarget();
                }
                case ESCORT, SENTINEL -> {
                    rangedMovement(target, 0.82f);
                    faceTarget();
                }
                case ASSAULT -> {
                    assaultMovement(target);
                    faceTarget();
                }
                case RETREAT -> {
                    retreating = true;
                    retreat();
                    faceMovement();
                }
            }
        } else {
            debug.branch = AIDebugBranch.NO_TARGET;
            if (profile.role == AIRole.COMMANDER) {
                commanderIdle();
            } else {
                idleAdvance();
            }
        }
    }

    private void updateRetreatState(BattleStrategy strategy) {
        if (strategy == BattleStrategy.RETREAT || profile.role == AIRole.RETREAT) {
            retreating = true;
            return;
        }

        if (profile.retreatThreshold <= 0f) {
            retreating = false;
            return;
        }

        if (!retreating && unit.healthf() <= profile.retreatThreshold) {
            retreating = true;
        } else if (retreating && unit.healthf() >= Math.min(1f, profile.retreatThreshold + 0.14f)) {
            retreating = false;
        }
    }

    private void assaultMovement(Teamc target) {
        float keep = unit.range() * Math.max(0.05f, profile.preferredRange);
        debug.branch = AIDebugBranch.ASSAULT;
        debug.preferredRange = keep;
        debug.route(target.getX(), target.getY());

        if (unit.isFlying() && profile.threatAvoidance > 0f) {
            steerFlying(target, keep, false);
        } else {
            moveTo(target, keep, 65f, true, null);
        }
    }

    private void skirmishMovement(Teamc target) {
        float keep = unit.range() * Math.max(0.72f, profile.preferredRange);
        debug.preferredRange = keep;
        debug.route(target.getX(), target.getY());

        if (unit.isFlying()) {
            // Distinct behavior: hold an outer range band and strafe rather than simply approach.
            steerFlying(target, keep, true);
        } else {
            moveTo(target, keep, 55f, true, null);
        }
    }

    private void rangedMovement(Teamc target, float rangeFactor) {
        float keep = unit.range() * rangeFactor;
        debug.branch = AIDebugBranch.RANGED;
        debug.preferredRange = keep;
        debug.route(target.getX(), target.getY());

        if (unit.isFlying() && profile.threatAvoidance > 0f) {
            steerFlying(target, keep, false);
        } else {
            moveTo(target, keep, 75f, true, null);
        }
    }

    /**
     * Threat-aware local steering with an actual distance band.
     * "strafe" makes the within-range behavior tangential instead of stationary.
     */
    private void steerFlying(Teamc target, float desiredRange, boolean strafe) {
        Vec2 toTarget = Tmp.v1.set(target).sub(unit);
        float distance = toTarget.len();

        if (distance < 0.001f) return;

        float targetAngle = toTarget.angle();
        float baseAngle;

        debug.preferredRange = desiredRange;

        if (desiredRange <= 1f) {
            if (profile.role == AIRole.BOMBER) {
                debug.branch = AIDebugBranch.BOMBER_APPROACH;
            }
            baseAngle = targetAngle;
        } else if (distance > desiredRange * 1.08f) {
            if (profile.role == AIRole.SKIRMISH) debug.branch = AIDebugBranch.SKIRMISH_APPROACH;
            baseAngle = targetAngle;
        } else if (distance < desiredRange * 0.82f) {
            if (profile.role == AIRole.SKIRMISH) debug.branch = AIDebugBranch.SKIRMISH_BACKOFF;
            baseAngle = targetAngle + 180f;
        } else if (strafe) {
            if (profile.role == AIRole.SKIRMISH) debug.branch = AIDebugBranch.SKIRMISH_STRAFE;
            if (profile.role == AIRole.FLANK) debug.branch = AIDebugBranch.FLANK_STRAFE;
            baseAngle = targetAngle + (((unit.id & 1) == 0) ? 90f : -90f);
        } else {
            baseAngle = targetAngle + (((unit.id & 1) == 0) ? 75f : -75f);
        }

        float lookAhead = Mathf.clamp(unit.speed() * 70f, 120f, 260f);
        float bestAngle = baseAngle;
        float bestCost = Float.MAX_VALUE;

        float[] offsets = {0f, 24f, -24f, 48f, -48f, 72f, -72f};

        for (float offset : offsets) {
            float angle = baseAngle + offset;
            float px = unit.x + Angles.trnsx(angle, lookAhead);
            float py = unit.y + Angles.trnsy(angle, lookAhead);

            float danger = ThreatManager.pathDanger(
                    unit.team,
                    unit.x,
                    unit.y,
                    px,
                    py,
                    true,
                    5
            );

            float turnPenalty = Math.abs(offset) / 90f;
            float cost = danger * Math.max(0f, profile.threatAvoidance) + turnPenalty * 0.35f;

            if (cost < bestCost) {
                bestCost = cost;
                bestAngle = angle;
            }
        }

        float debugX = unit.x + Angles.trnsx(bestAngle, lookAhead);
        float debugY = unit.y + Angles.trnsy(bestAngle, lookAhead);
        debug.route(debugX, debugY);
        debug.route2(target.getX(), target.getY());

        unit.movePref(Tmp.v2.trns(bestAngle, prefSpeed()));
    }

    /**
     * Uses a persistent side waypoint around the target. The side is selected by comparing
     * the cached danger of left/right two-segment routes.
     */
    private void flankMovement(Teamc target) {
        if (!unit.isFlying()) {
            rangedMovement(target, profile.preferredRange);
            debug.branch = AIDebugBranch.FLANK_GROUND_FALLBACK;
            faceTarget();
            return;
        }

        flankRepathTimer -= Time.delta;

        if (target.id() != flankTargetId || flankRepathTimer <= 0f) {
            flankTargetId = target.id();
            flankRepathTimer = 90f;

            float tx = target.getX(), ty = target.getY();
            float dx = tx - unit.x, dy = ty - unit.y;
            float length = Mathf.sqrt(dx * dx + dy * dy);

            if (length < 0.001f) return;

            dx /= length;
            dy /= length;

            float flankDistance = Mathf.clamp(length * 0.45f, 140f, 300f);
            float backoff = Math.max(unit.range() * profile.preferredRange * 0.35f, 35f);

            float leftX = tx - dy * flankDistance - dx * backoff;
            float leftY = ty + dx * flankDistance - dy * backoff;

            float rightX = tx + dy * flankDistance - dx * backoff;
            float rightY = ty - dx * flankDistance - dy * backoff;

            float leftDanger =
                    ThreatManager.pathDanger(unit.team, unit.x, unit.y, leftX, leftY, true, 7)
                            + ThreatManager.pathDanger(unit.team, leftX, leftY, tx, ty, true, 5);

            float rightDanger =
                    ThreatManager.pathDanger(unit.team, unit.x, unit.y, rightX, rightY, true, 7)
                            + ThreatManager.pathDanger(unit.team, rightX, rightY, tx, ty, true, 5);

            debug.branch = AIDebugBranch.FLANK_REPATH;
            debug.flankCandidatesValid = true;
            debug.leftCandidate.set(leftX, leftY);
            debug.rightCandidate.set(rightX, rightY);
            debug.leftDanger = leftDanger;
            debug.rightDanger = rightDanger;

            if (Math.abs(leftDanger - rightDanger) < 0.05f) {
                flankSide = ((unit.id & 1) == 0) ? 1 : -1;
            } else {
                flankSide = leftDanger < rightDanger ? 1 : -1;
            }

            flankPoint.set(
                    flankSide > 0 ? leftX : rightX,
                    flankSide > 0 ? leftY : rightY
            );

            debug.flankSide = flankSide;
        }

        float desired = unit.range() * profile.preferredRange;
        debug.preferredRange = desired;
        debug.route(flankPoint.x, flankPoint.y);
        debug.route2(target.getX(), target.getY());

        if (unit.dst(flankPoint) > 45f && unit.dst(target) > desired * 0.85f) {
            debug.branch = AIDebugBranch.FLANK_WAYPOINT;
            float angle = unit.angleTo(flankPoint);
            unit.movePref(Tmp.v1.trns(angle, prefSpeed()));
            unit.lookAt(angle);
        } else {
            steerFlying(target, desired, true);
            faceTarget();
        }
    }

    private void bomberMovement(Teamc target) {
        float trigger = Math.max(65f, unit.range() * 0.75f);
        debug.branch = AIDebugBranch.BOMBER_APPROACH;
        debug.route(target.getX(), target.getY());

        if (unit.within(target, trigger)) {
            bomberPassAngle = unit.vel().len2() > 0.01f
                    ? unit.vel().angle()
                    : unit.angleTo(target);

            // If current motion is mostly sideways/backwards, establish a clean pass through target.
            if (Angles.angleDist(bomberPassAngle, unit.angleTo(target)) > 55f) {
                bomberPassAngle = unit.angleTo(target);
            }

            bomberPassTimer = 55f;
            continueBomberPass();
            return;
        }

        if (profile.threatAvoidance > 0f) {
            steerFlying(target, 0f, false);
            unit.lookAt(unit.vel().angle());
        } else {
            float angle = unit.angleTo(target);
            unit.movePref(Tmp.v1.trns(angle, prefSpeed()));
            unit.lookAt(angle);
        }
    }

    private void continueBomberPass() {
        bomberPassTimer = Math.max(0f, bomberPassTimer - Time.delta);
        debug.branch = AIDebugBranch.BOMBER_PASS;
        debug.bomberPassAngle = bomberPassAngle;
        debug.bomberPassTimer = bomberPassTimer;
        debug.route(
                unit.x + Angles.trnsx(bomberPassAngle, 180f),
                unit.y + Angles.trnsy(bomberPassAngle, 180f)
        );

        unit.movePref(Tmp.v1.trns(bomberPassAngle, prefSpeed()));
        unit.lookAt(bomberPassAngle);
    }

    private void swarmMovement(Teamc target) {
        float targetSize = targetHitSize(target);
        float contact = targetSize / 2f + unit.hitSize / 2f + 3f;

        debug.preferredRange = contact;
        debug.route(target.getX(), target.getY());
        debug.branch = unit.within(target, contact)
                ? AIDebugBranch.SWARM_CONTACT
                : AIDebugBranch.SWARM_APPROACH;

        if (unit.isFlying()) {
            float angle = unit.angleTo(target);
            unit.movePref(Tmp.v1.trns(angle, prefSpeed()));

            if (unit.within(target, contact)) {
                // Tiny per-unit tangent prevents every drone from occupying one exact line.
                float tangent = ((unit.id & 1) == 0) ? 82f : -82f;
                unit.movePref(Tmp.v2.trns(angle + tangent, prefSpeed() * 0.55f));
            }
        } else {
            moveTo(target, contact, 25f, false, null);
        }
    }

    /**
     * Commander stays behind the local SmartAI group relative to the current target.
     * This makes the COMMANDER role visually different even before the full MAX commander
     * feature set exists.
     */

    private float targetHitSize(Teamc target) {
        if (target instanceof Unit other) {
            return other.hitSize;
        }

        if (target instanceof Building build) {
            return build.block.size * Vars.tilesize;
        }

        return 0f;
    }

    private void commanderMovement(Teamc target) {
        final float[] sum = {0f, 0f};
        final int[] count = {0};

        Units.nearby(unit.team, unit.x, unit.y, 340f, ally -> {
            if (ally == unit || !(ally.controller() instanceof MdtSmartController ai)) return;
            if (ai.profile().role == AIRole.COMMANDER) return;

            sum[0] += ally.x;
            sum[1] += ally.y;
            count[0]++;
        });

        if (count[0] >= 2) {
            commanderPoint.set(sum[0] / count[0], sum[1] / count[0]);
            debug.branch = AIDebugBranch.COMMANDER_CENTROID;

            Tmp.v1.set(commanderPoint).sub(target);

            if (Tmp.v1.len2() > 0.001f) {
                Tmp.v1.setLength(85f);
                commanderPoint.add(Tmp.v1);
            }

            debug.route(commanderPoint.x, commanderPoint.y);
            debug.route2(target.getX(), target.getY());

            if (unit.dst(commanderPoint) > 55f) {
                moveTo(commanderPoint, 20f, 70f, true, null);
                return;
            }
        }

        rangedMovement(target, 0.94f);
        debug.branch = AIDebugBranch.COMMANDER_RANGE;
    }

    private void commanderIdle() {
        final float[] sum = {0f, 0f};
        final int[] count = {0};

        Units.nearby(unit.team, unit.x, unit.y, 420f, ally -> {
            if (ally == unit || !(ally.controller() instanceof MdtSmartController ai)) return;
            if (ai.profile().role == AIRole.COMMANDER) return;

            sum[0] += ally.x;
            sum[1] += ally.y;
            count[0]++;
        });

        if (count[0] > 0) {
            commanderPoint.set(sum[0] / count[0], sum[1] / count[0]);
            debug.branch = AIDebugBranch.COMMANDER_IDLE_GROUP;
            debug.route(commanderPoint.x, commanderPoint.y);
            moveTo(commanderPoint, 90f, 80f, true, null);
        } else {
            idleAdvance();
        }
    }

    private void retreat() {
        stopShooting();

        Teamc repair = targetFlag(unit.x, unit.y, BlockFlag.repair, false);
        Teamc safe = repair != null ? repair : unit.closestCore();

        if (safe != null) {
            debug.safeTarget = safe;
            debug.route(safe.getX(), safe.getY());
            debug.branch = repair != null
                    ? AIDebugBranch.RETREAT_REPAIR
                    : AIDebugBranch.RETREAT_CORE;
            moveTo(safe, 18f, 55f, false, null);
            return;
        }

        Teamc threat = target;

        if (threat == null || Units.invalidateTarget(threat, unit.team, unit.x, unit.y)) {
            threat = Units.closestTarget(
                    unit.team,
                    unit.x,
                    unit.y,
                    320f,
                    other -> other.targetable(unit.team),
                    build -> true
            );
        }

        if (threat != null) {
            debug.branch = AIDebugBranch.RETREAT_AWAY;
            Vec2 away = Tmp.v1.set(unit.x - threat.getX(), unit.y - threat.getY());

            if (away.len2() > 0.001f) {
                away.setLength(Math.max(120f, unit.speed() * 80f));
                debug.route(unit.x + away.x, unit.y + away.y);
                unit.movePref(away.setLength(prefSpeed()));
            }
        } else {
            debug.branch = AIDebugBranch.RETREAT_NO_SAFE_TARGET;
        }
    }

    private void regroup() {
        Unit commander = BattleCommander.commander(unit.team);

        if (commander != null && commander != unit) {
            debug.branch = AIDebugBranch.REGROUP_COMMANDER;
            debug.route(commander.x, commander.y);
            moveTo(commander, 95f + unit.hitSize, 70f, true, null);
            return;
        }

        Teamc core = unit.closestCore();

        if (core != null) {
            debug.branch = AIDebugBranch.REGROUP_CORE;
            debug.route(core.getX(), core.getY());
            moveTo(core, 80f + unit.hitSize, 80f, false, null);
        } else {
            idleAdvance();
        }
    }

    private void idleAdvance() {
        Teamc core = unit.closestEnemyCore();

        if (unit.isFlying()) {
            if (core != null) {
                debug.branch = AIDebugBranch.NO_TARGET;
                debug.route(core.getX(), core.getY());
                moveTo(core, unit.range() * 0.8f);
            }
        } else {
            debug.branch = AIDebugBranch.PATH_CORE;
            if (core != null) debug.route(core.getX(), core.getY());
            pathfind(Pathfinder.fieldCore, true);
        }
    }

    private Teamc sharedTarget() {
        if (!unit.hasEffect(ModStatusEffects.tacticalLinked)) return null;

        final Teamc[] best = {null};
        final float[] bestScore = {-Float.MAX_VALUE};

        Units.nearby(unit.team, unit.x, unit.y, profile.tacticalLinkRange, ally -> {
            if (ally == unit || !(ally.controller() instanceof MdtSmartController ai)) return;

            Teamc allyTarget = ai.currentTarget();

            if (allyTarget == null
                    || Units.invalidateTarget(allyTarget, unit.team, unit.x, unit.y)) return;

            float score = TargetScorer.score(unit, allyTarget, profile);

            if (score > bestScore[0]) {
                best[0] = allyTarget;
                bestScore[0] = score;
            }
        });

        return best[0];
    }
}
