package mdtnh.debug;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Strings;
import mdtnh.ai.BattleCommander;
import mdtnh.ai.MdtSmartController;
import mdtnh.ai.SmartAI;
import mdtnh.ai.SmartCommandAI;
import mdtnh.ai.ThreatManager;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

/**
 * Development-only AI telemetry overlay.
 *
 * Hotkeys:
 * F8  enable/disable
 * F9  scope: SELECTED -> NEARBY -> SCREEN
 * F10 threat overlay
 * F11 verbose labels
 * F12 print current debug snapshot(s) to console/log
 *
 * IMPORTANT:
 * Mindustry AIController updates server-side. In multiplayer client mode this overlay cannot
 * reliably inspect unsynchronized private SmartAI telemetry. Use singleplayer or the host.
 */
public final class MdtAIDebug {

    public enum Scope {
        SELECTED,
        NEARBY,
        SCREEN;

        public Scope next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public static boolean enabled;
    public static boolean showThreat = true;
    public static boolean verbose = true;
    public static Scope scope = Scope.SELECTED;

    public static float nearbyRange = 900f;
    public static int maxUnits = 32;

    private static boolean installed;
    private static final Seq<Unit> units = new Seq<>();

    private static final Color roleColor = Color.valueOf("84f3ff");
    private static final Color targetColor = Color.valueOf("ff6b6b");
    private static final Color routeColor = Color.valueOf("66d9ff");
    private static final Color route2Color = Color.valueOf("ffcf66");
    private static final Color velocityColor = Color.valueOf("7dff8a");
    private static final Color rangeColor = Color.valueOf("ffe66d");
    private static final Color leftColor = Color.valueOf("a4ff7a");
    private static final Color rightColor = Color.valueOf("ff9f7a");
    private static final Color threatAirColor = Color.valueOf("ff5577");
    private static final Color threatGroundColor = Color.valueOf("ffb45a");

    public static void install() {
        if (installed || Vars.headless) return;
        installed = true;

        Events.run(Trigger.update, MdtAIDebug::update);
        Events.run(Trigger.drawOver, MdtAIDebug::draw);
    }

    private static void update() {
        if (Core.input.keyTap(KeyCode.f8)) {
            enabled = !enabled;
            Log.info("[MDTNH-AI-DEBUG] enabled=@ scope=@ threat=@ verbose=@",
                    enabled, scope, showThreat, verbose);
        }

        if (!enabled) return;

        if (Core.input.keyTap(KeyCode.f9)) {
            scope = scope.next();
            Log.info("[MDTNH-AI-DEBUG] scope=@", scope);
        }

        if (Core.input.keyTap(KeyCode.f10)) {
            showThreat = !showThreat;
            Log.info("[MDTNH-AI-DEBUG] threat=@", showThreat);
        }

        if (Core.input.keyTap(KeyCode.f11)) {
            verbose = !verbose;
            Log.info("[MDTNH-AI-DEBUG] verbose=@", verbose);
        }

        if (Core.input.keyTap(KeyCode.f12)) {
            collectUnits();
            printSnapshots();
        }
    }

    private static void draw() {
        if (!enabled || Vars.state == null || !Vars.state.isGame()) return;

        collectUnits();

        float z = Draw.z();
        Draw.z(Layer.overlayUI);

        for (Unit unit : units) {
            drawUnit(unit);
        }

        if (showThreat && !units.isEmpty()) {
            // Threat overlay uses the team of the first inspected unit.
            drawThreat(units.first());
        }

        Draw.z(z);
        Draw.reset();
    }

    private static void collectUnits() {
        units.clear();

        if (scope == Scope.SELECTED && Vars.control != null && Vars.control.input != null) {
            for (Unit unit : Vars.control.input.selectedUnits) {
                if (unit != null && unit.isValid()) {
                    units.add(unit);
                    if (units.size >= maxUnits) return;
                }
            }

            // When nothing is RTS-selected, SELECTED falls back to the locally controlled unit.
            if (units.isEmpty() && Vars.player != null && Vars.player.unit() != null) {
                units.add(Vars.player.unit());
            }

            return;
        }

        float cx, cy, range;

        if (scope == Scope.NEARBY && Vars.player != null && Vars.player.unit() != null) {
            cx = Vars.player.unit().x;
            cy = Vars.player.unit().y;
            range = nearbyRange;
        } else {
            cx = Core.camera.position.x;
            cy = Core.camera.position.y;
            range = Math.max(Core.camera.width, Core.camera.height) * 0.75f;
        }

        Groups.unit.each(unit -> {
            if (units.size >= maxUnits || !unit.isValid()) return;

            if (Mathf.within(cx, cy, unit.x, unit.y, range)) {
                // In automatic scopes, avoid cluttering the screen with ordinary vanilla AI.
                if (unit.controller() instanceof MdtSmartController) {
                    units.add(unit);
                }
            }
        });
    }

    private static void drawUnit(Unit unit) {
        boolean smart = unit.controller() instanceof MdtSmartController;

        Drawf.select(unit.x, unit.y, Math.max(unit.hitSize * 0.65f, 9f),
                smart ? roleColor : Color.scarlet);

        if (!smart) {
            String controller = unit.controller() == null
                    ? "null"
                    : unit.controller().getClass().getSimpleName();

            Drawf.text(
                    "NOT SmartAI | controller=" + controller,
                    unit.x,
                    unit.y + unit.hitSize * 0.75f + 13f,
                    Color.scarlet,
                    0.75f,
                    Align.center
            );
            return;
        }

        MdtSmartController ai = (MdtSmartController)unit.controller();
        AIDebugState state = ai.debugState();

        // Actual velocity: what physics is currently doing.
        if (unit.vel().len2() > 0.001f) {
            float len = Mathf.clamp(unit.vel().len() * 45f, 24f, 130f);
            float vx = unit.x + Angles.trnsx(unit.vel().angle(), len);
            float vy = unit.y + Angles.trnsy(unit.vel().angle(), len);
            Drawf.arrow(unit.x, unit.y, vx, vy, len, 4f, velocityColor);
        }

        // Current target.
        if (state.target != null) {
            Drawf.dashLine(targetColor, unit.x, unit.y, state.target.getX(), state.target.getY());
            Drawf.target(state.target.getX(), state.target.getY(), 10f, targetColor);
        }

        // Current planned route: unit -> waypoint -> waypoint2.
        if (state.waypointValid) {
            Drawf.line(routeColor, unit.x, unit.y, state.waypoint.x, state.waypoint.y);
            Drawf.square(state.waypoint.x, state.waypoint.y, 7f, 45f, routeColor);

            if (state.waypoint2Valid) {
                Drawf.dashLine(route2Color,
                        state.waypoint.x, state.waypoint.y,
                        state.waypoint2.x, state.waypoint2.y);
                Drawf.square(state.waypoint2.x, state.waypoint2.y, 6f, 45f, route2Color);
            }
        }

        // Preferred combat range.
        if (state.preferredRange > 1f && state.target != null) {
            Drawf.dashCircle(
                    state.target.getX(),
                    state.target.getY(),
                    state.preferredRange,
                    rangeColor
            );
        }

        // FLANK candidates and actual danger scores.
        if (state.flankCandidatesValid) {
            Drawf.dashLine(leftColor, unit.x, unit.y,
                    state.leftCandidate.x, state.leftCandidate.y);
            Drawf.dashLine(rightColor, unit.x, unit.y,
                    state.rightCandidate.x, state.rightCandidate.y);

            Drawf.text(
                    "L=" + Strings.fixed(state.leftDanger, 2),
                    state.leftCandidate.x,
                    state.leftCandidate.y + 9f,
                    leftColor,
                    0.65f,
                    Align.center
            );

            Drawf.text(
                    "R=" + Strings.fixed(state.rightDanger, 2),
                    state.rightCandidate.x,
                    state.rightCandidate.y + 9f,
                    rightColor,
                    0.65f,
                    Align.center
            );
        }

        String line1 =
                "id=" + unit.id
                        + " role=" + state.role
                        + " mode=" + (ai.commandMode() ? "COMMAND" : "AUTO")
                        + " branch=" + state.branch;

        String line2 =
                "strategy=" + state.strategy
                        + " retreat=" + state.retreating
                        + " target=" + targetName(state.target);

        Drawf.text(
                line1,
                unit.x,
                unit.y + unit.hitSize * 0.75f + 17f,
                roleColor,
                0.72f,
                Align.center
        );

        if (verbose) {
            Drawf.text(
                    line2,
                    unit.x,
                    unit.y + unit.hitSize * 0.75f + 8f,
                    Color.white,
                    0.64f,
                    Align.center
            );

            String line3 =
                    "hp=" + Strings.fixed(unit.healthf() * 100f, 0) + "%"
                            + " speed=" + Strings.fixed(unit.vel().len(), 2)
                            + "/" + Strings.fixed(unit.speed(), 2)
                            + " macroActive=" + BattleCommander.active(unit.team);

            Drawf.text(
                    line3,
                    unit.x,
                    unit.y + unit.hitSize * 0.75f - 1f,
                    Color.lightGray,
                    0.58f,
                    Align.center
            );

            if (state.bomberPassTimer > 0f) {
                String bomb =
                        "pass=" + Strings.fixed(state.bomberPassTimer, 1)
                                + " angle=" + Strings.fixed(state.bomberPassAngle, 0);

                Drawf.text(
                        bomb,
                        unit.x,
                        unit.y - unit.hitSize * 0.75f - 6f,
                        route2Color,
                        0.58f,
                        Align.center
                );
            }
        }
    }

    private static void drawThreat(Unit viewer) {
        ThreatManager.eachThreat(viewer.team, node -> {
            boolean relevant = viewer.isFlying() ? node.targetsAir : node.targetsGround;
            if (!relevant) return;

            Color color = viewer.isFlying() ? threatAirColor : threatGroundColor;
            float radius = node.range + ThreatManager.safetyMargin;

            Draw.color(color, 0.35f);
            Lines.stroke(1f);
            Lines.circle(node.x, node.y, radius);
            Draw.reset();

            if (verbose) {
                Drawf.text(
                        "THREAT " + Strings.fixed(node.danger, 2),
                        node.x,
                        node.y + radius + 7f,
                        color,
                        0.55f,
                        Align.center
                );
            }
        });
    }

    private static void printSnapshots() {
        if (units.isEmpty()) {
            Log.info("[MDTNH-AI-DEBUG] no inspected units");
            return;
        }

        Log.info("[MDTNH-AI-DEBUG] --- snapshot count=@ scope=@ ---", units.size, scope);

        for (Unit unit : units) {
            String controller = unit.controller() == null
                    ? "null"
                    : unit.controller().getClass().getName();

            if (!(unit.controller() instanceof MdtSmartController ai)) {
                Log.info("[MDTNH-AI-DEBUG] id=@ type=@ controller=@ NOT-SMART",
                        unit.id, unit.type.name, controller);
                continue;
            }

            AIDebugState s = ai.debugState();

            Log.info(
                    "[MDTNH-AI-DEBUG] id=@ type=@ controller=@ role=@ strategy=@ branch=@ retreat=@ hp=@ vel=@/@ target=@ waypoint=@ waypoint2=@ Ldanger=@ Rdanger=@ flankSide=@",
                    unit.id,
                    unit.type.name,
                    controller,
                    s.role,
                    s.strategy,
                    s.branch,
                    s.retreating,
                    Strings.fixed(unit.healthf(), 3),
                    Strings.fixed(unit.vel().len(), 3),
                    Strings.fixed(unit.speed(), 3),
                    targetName(s.target),
                    s.waypointValid ? vec(s.waypoint.x, s.waypoint.y) : "-",
                    s.waypoint2Valid ? vec(s.waypoint2.x, s.waypoint2.y) : "-",
                    Strings.fixed(s.leftDanger, 3),
                    Strings.fixed(s.rightDanger, 3),
                    s.flankSide
            );
        }
    }

    private static String targetName(Teamc target) {
        if (target == null) return "null";

        if (target instanceof Unit unit) {
            return "U#" + unit.id + ":" + unit.type.name;
        }

        return target.getClass().getSimpleName() + "#" + target.id();
    }

    private static String vec(float x, float y) {
        return "(" + Strings.fixed(x, 1) + "," + Strings.fixed(y, 1) + ")";
    }

    private MdtAIDebug() {
    }
}
