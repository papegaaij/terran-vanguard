package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_FLASH_TICKS;
import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part E: Level 32's units and ground targets (tools/art/belt_units_e.py, whose docstring gives the sprites;
 * design/enemies/ground/sentinel-tower, ground/strider, design/campaign Level 32's blast doors, mining charges and repair
 * crate), drawn whole instead of by {@link EnemyLooks}' heading set:
 *
 * <ul>
 *   <li>the Sentinel Tower: its collar ({@code sentinel-tower}) under the emitter head at 32 headings ({@code -head}),
 *       the head turning toward the ship, onto the sweep's first edge over the telegraph and with the beam; the
 *       telegraph (the crimson arc wedge per difficulty, {@code -telegraph-<arc>}, or for a full turn the reach ring
 *       {@code -ring} and the dim start ray), the eye on the lens ({@code -eye}: its first glimmer to live), the beam
 *       ({@code -beam}, tiled from the lens) and its tip ({@code -beam-tip}); at its death the head toppling off over the
 *       burst ({@code -topple}), its stump ({@link EnemyLooks}' remains);
 *   <li>the Strider: its legs (16 headings × 8 walk frames, {@code strider-legs}) under the torso at 32 headings (the
 *       twin cannons turning toward the ship), the shoulder pod ({@code strider-pod}, its hatch open over the salvo's
 *       0.4 s tell; gone with its part), a hit knee's red flash ({@code strider-knee}), the muzzles' glow over the fan's
 *       0.3 s tell; a topple's stagger and fall ({@code strider-fall}, by the lost leg's side), its fall line marked
 *       over the stagger, the toppled wreck along the line ({@code strider-wreck}); a hull kill's standing burnt hulk
 *       ({@code strider-hulk});
 *   <li>the blast doors' locks (the amber lamp's pulse; the sealed door's dark lock), the doors sliding open when the
 *       lock is destroyed ({@code -open}, the last frame left on the ground); the mining charges' blinking lamp and
 *       their burst ({@code mining-charge-blast}, on the {@link SimEvents.Type#CHARGE_BLAST}); the repair crate and its
 *       opening; a fork's unchosen branch's objects drawn dark.
 * </ul>
 *
 * <p>The simulation has no read-out of the Strider's guns' clocks: the fan's glow and the pod's hatch are timed from the
 * walker's last fan ({@code ENEMY_FIRED} at its centre) or salvo ({@code MISSILE_LAUNCHED} at its pod) and the gun's
 * interval, the first from when it came into view, as {@link CeresLooks#untilShot} does.
 */
final class DeepCoreLooks {
    static final String TOWER = "sentinel-tower";
    static final String STRIDER = "strider";
    static final String DOOR_LEFT = "blast-door-left";
    static final String DOOR_RIGHT = "blast-door-right";
    static final String DOOR_SEALED = "blast-door-sealed-right";
    static final String CHARGE = "mining-charge";
    static final String CRATE = "repair-crate";
    /** The ground targets these looks draw (by their {@code sprite:}). */
    static final Set<String> OBJECTS = Set.of(DOOR_LEFT, DOOR_RIGHT, DOOR_SEALED, CHARGE, CRATE);

    /** The tower head's headings: k turns k × 11.25 degrees clockwise from straight down. */
    static final int HEAD_HEADINGS = 32;
    /** The Strider's legs' headings and walk frames, its torso's headings. */
    static final int LEG_HEADINGS = 16;

    static final int WALK_FRAMES = 8;
    static final int TORSO_HEADINGS = 32;
    /** The fall's directions (the walker's heading rounded to 45 degrees) and steps (2 of stagger, 4 of fall). */
    static final int FALL_DIRS = 8;

    static final int FALL_STEPS = 6;
    static final int STAGGER_STEPS = 2;
    /** The head turns toward its aim at most this fast, radians a step (360 degrees/s). */
    static final double HEAD_TURN = 2 * Math.PI / SimStep.PER_SECOND;
    /** The torso turns toward the ship at most this fast, radians a step (120 degrees/s). */
    static final double TORSO_TURN = Math.toRadians(120) / SimStep.PER_SECOND;
    /** The fan's tell: the torso holds still and the muzzles glow (design/enemies/ground/strider). */
    static final double FAN_TELL_SECONDS = 0.3;
    /** A knee's hit flash: swell, full, fade, 3 steps a frame. */
    static final int KNEE_FRAME_TICKS = 3;
    /** The door's slide and the crate's opening: 3 steps a frame, the last frame stays. */
    static final int OPEN_FRAME_TICKS = 3;
    /** The charge's burst: 3 steps a frame. */
    static final int BLAST_FRAME_TICKS = 3;
    /** The tower's head toppling off: 4 steps a frame. */
    static final int TOPPLE_FRAME_TICKS = 4;
    /** The lock's lamp pulse and the charge's blink: 8 steps a frame. */
    static final int LAMP_FRAME_TICKS = 8;
    /** An event lies on its unit within this, px (a fan at the walker's centre, a salvo at its pod). */
    static final double REACH = 6;

    static final double POD_REACH = 12;
    /** An unchosen branch's object is drawn this dark. */
    static final float DORMANT = 0.4f;

    private static final Color CRIMSON = Color.valueOf("D0203A");
    private static final Color FALL_LINE = new Color(1f, 0.35f, 0.15f, 1f);

    private static final int SLOTS = 16;
    private static final int PIECES = 16;
    private static final long NEVER = Long.MIN_VALUE / 2;

    private static final int NONE = 0;
    private static final int ROLE_TOWER = 1;
    private static final int ROLE_STRIDER = 2;

    private final Sprites sprites;
    private final TextureRegion pixel;
    private final int[] role;

    private final AtlasRegion collar;
    private final Array<AtlasRegion> head;
    private final Array<AtlasRegion> eye;
    private final Array<AtlasRegion> beam;
    private final Array<AtlasRegion> beamTip;
    private final AtlasRegion ring;
    private final Array<AtlasRegion> towerTopple;
    /** Per head heading the lens's front, px from the tower's centre, y up. */
    private final double[][] lens;

    private final Array<AtlasRegion> legs;
    private final Array<AtlasRegion> torso;
    private final Array<AtlasRegion> pod;
    private final Array<AtlasRegion> knee;
    private final Array<AtlasRegion> fall;
    private final Array<AtlasRegion> wreck;
    private final Array<AtlasRegion> hulk;
    /** Per torso heading the pod's centre, px from the walker's centre, y up. */
    private final double[][] podAt;
    /** Per torso heading the twin muzzles, px from the walker's centre, y up. */
    private final double[][][] muzzles;
    /** Per legs frame the left and right knee, px from the walker's centre, y up. */
    private final double[][][] knees;
    /** Per fall direction × side the walker's centre in a fall or wreck frame, px from the frame's top left. */
    private final double[][] fallAnchor;

    private final Array<AtlasRegion> charge;
    private final Array<AtlasRegion> blast;

    /** The units followed (serial, -1 free) and what the looks keep of them between steps. */
    private final int[] serial = new int[SLOTS];

    private final boolean[] seen = new boolean[SLOTS];
    /** The step a unit was last missed (it died or left), for its death's remains. */
    private final long[] goneAt = new long[SLOTS];
    /** The step a tower's telegraph started, {@code NEVER} outside one. */
    private final long[] tellStart = new long[SLOTS];

    private final long[] firstSeen = new long[SLOTS];
    private final double[] heading = new double[SLOTS];
    private final long[] lastFan = new long[SLOTS];
    private final long[] lastSalvo = new long[SLOTS];
    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];
    private final double[] lastFacing = new double[SLOTS];
    /** A Strider's topple: whether it was toppling at the last step, its fall direction and side. */
    private final boolean[] toppled = new boolean[SLOTS];

    private final int[] fallDir = new int[SLOTS];
    private final int[] fallSide = new int[SLOTS];
    /** Per Strider its legs' HP shares at the last step and the step each knee was last hit. */
    private final double[][] legShare = new double[SLOTS][2];

    private final long[][] kneeHit = new long[SLOTS][2];

    /** What stays on the ground: wrecks, hulks, opened doors and crates (frames, start step, place, ground row). */
    private final List<Array<AtlasRegion>> remainFrames = new java.util.ArrayList<>(PIECES);

    private final long[] remainTick = new long[PIECES];
    private final double[] remainX = new double[PIECES];
    private final double[] remainGroundY = new double[PIECES];
    private final int[] remainFrame = new int[PIECES];
    /** A remain's anchor in its frame (px from the top left), NaN for its centre. */
    private final double[] remainAnchorX = new double[PIECES];

    private final double[] remainAnchorY = new double[PIECES];
    private int nextRemain;

    /** The towers' heads toppling off and the charges' bursts (start step, place, ground row). */
    private final long[] toppleTick = new long[PIECES];

    private final double[] toppleX = new double[PIECES];
    private final double[] toppleGroundY = new double[PIECES];
    private int nextTopple;

    private final long[] blastTick = new long[PIECES];
    private final double[] blastX = new double[PIECES];
    private final double[] blastGroundY = new double[PIECES];
    private int nextBlast;

    DeepCoreLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        this.sprites = sprites;
        this.pixel = sprites.pixel;
        role = new int[kinds.size()];
        for (int k = 0; k < role.length; k++) {
            String slug = kinds.get(k).slug();
            role[k] = switch (slug) {
                case TOWER -> sprites.has(TOWER + "-head") ? ROLE_TOWER : NONE;
                case STRIDER -> sprites.has(STRIDER + "-legs") && sprites.has(STRIDER) ? ROLE_STRIDER : NONE;
                default -> NONE;
            };
        }
        Array<AtlasRegion> none = new Array<>();
        collar = single(TOWER);
        head = optional(TOWER + "-head", none);
        eye = optional(TOWER + "-eye", none);
        beam = optional(TOWER + "-beam", none);
        beamTip = optional(TOWER + "-beam-tip", none);
        ring = single(TOWER + "-ring");
        towerTopple = optional(TOWER + "-topple", none);
        lens = lens(pivots.apply(TOWER));
        legs = optional(STRIDER + "-legs", none);
        torso = sprites.has(STRIDER + "-legs") ? optional(STRIDER, none) : none;
        pod = optional(STRIDER + "-pod", none);
        knee = optional(STRIDER + "-knee", none);
        fall = optional(STRIDER + "-fall", none);
        wreck = optional(STRIDER + "-wreck", none);
        hulk = optional(STRIDER + "-hulk", none);
        JsonValue strider = pivots.apply(STRIDER);
        podAt = torsoPoints(strider, "pod");
        muzzles = pairs(strider, "muzzles", "torso", TORSO_HEADINGS);
        knees = pairs(strider, "knees", "size", LEG_HEADINGS * WALK_FRAMES);
        fallAnchor = fallAnchors(strider);
        charge = optional(CHARGE, none);
        blast = optional(CHARGE + "-blast", none);
        for (int k = 0; k < PIECES; k++) {
            remainFrames.add(null);
        }
        restart();
    }

    private Array<AtlasRegion> optional(String name, Array<AtlasRegion> none) {
        return sprites.has(name) ? sprites.frames(name) : none;
    }

    private AtlasRegion single(String name) {
        return sprites.has(name) ? sprites.region(name) : null;
    }

    /** The lens per head heading from {@code lens[k]} ([x, y] px from the head frame's top left). */
    static double[][] lens(JsonValue pivots) {
        double[][] at = new double[HEAD_HEADINGS][2];
        JsonValue lens = pivots == null ? null : pivots.get("lens");
        if (lens == null) {
            return at;
        }
        double half = pivots.get("head").getDouble(0) / 2;
        for (int k = 0; k < HEAD_HEADINGS && k < lens.size; k++) {
            at[k][0] = lens.get(k).getDouble(0) - half;
            at[k][1] = half - lens.get(k).getDouble(1);
        }
        return at;
    }

    /** One point per torso heading ({@code name[k]}: [x, y] px from the torso frame's top left), from its centre. */
    static double[][] torsoPoints(JsonValue pivots, String name) {
        double[][] at = new double[TORSO_HEADINGS][2];
        JsonValue points = pivots == null ? null : pivots.get(name);
        if (points == null) {
            return at;
        }
        double half = pivots.get("torso").getDouble(0) / 2;
        for (int k = 0; k < TORSO_HEADINGS && k < points.size; k++) {
            at[k][0] = points.get(k).getDouble(0) - half;
            at[k][1] = half - points.get(k).getDouble(1);
        }
        return at;
    }

    /** Two points per frame ({@code name[k]}: [[x, y], [x, y]] px from the frame's top left), from its centre. */
    static double[][][] pairs(JsonValue pivots, String name, String size, int count) {
        double[][][] at = new double[count][2][2];
        JsonValue points = pivots == null ? null : pivots.get(name);
        if (points == null) {
            return at;
        }
        double half = pivots.get(size).getDouble(0) / 2;
        for (int k = 0; k < count && k < points.size; k++) {
            for (int m = 0; m < 2; m++) {
                at[k][m][0] = points.get(k).get(m).getDouble(0) - half;
                at[k][m][1] = half - points.get(k).get(m).getDouble(1);
            }
        }
        return at;
    }

    /** The fall frames' anchors ({@code fall.anchor[dir * 2 + side]}, px from the frame's top left). */
    static double[][] fallAnchors(JsonValue pivots) {
        double[][] at = new double[FALL_DIRS * 2][2];
        JsonValue fall = pivots == null ? null : pivots.get("fall");
        JsonValue anchors = fall == null ? null : fall.get("anchor");
        double half = fall == null ? 0 : fall.get("size").getDouble(0) / 2;
        for (int g = 0; g < at.length; g++) {
            at[g][0] = anchors != null && g < anchors.size ? anchors.get(g).getDouble(0) : half;
            at[g][1] = anchors != null && g < anchors.size ? anchors.get(g).getDouble(1) : half;
        }
        return at;
    }

    private int roleOf(int kind) {
        return kind >= 0 && kind < role.length ? role[kind] : NONE;
    }

    /** Whether these looks draw the unit (a Sentinel Tower or a Strider with its art). */
    boolean draws(Enemy enemy) {
        return roleOf(enemy.kind()) != NONE;
    }

    /** Whether these looks draw a ground object of that look ({@link #OBJECTS} with its art). */
    boolean drawsObject(GroundObject object) {
        String look = object.spec().look();
        return OBJECTS.contains(look) && sprites.has(look);
    }

    /** The level restarts: no unit followed, nothing left on the ground, no burst. */
    void restart() {
        Arrays.fill(serial, -1);
        Arrays.fill(goneAt, NEVER);
        Arrays.fill(tellStart, NEVER);
        Arrays.fill(remainTick, NEVER);
        Arrays.fill(toppleTick, NEVER);
        Arrays.fill(blastTick, NEVER);
        java.util.Collections.fill(remainFrames, null);
        nextRemain = 0;
        nextTopple = 0;
        nextBlast = 0;
    }

    // --- the frames ------------------------------------------------------------------------------

    /** The legs' frame at {@code heading} (16) after walking {@code walked} px with strides of {@code stride} px. */
    static int legFrame(int heading, double walked, double stride) {
        int frame = (int) Math.floorMod((long) Math.floor(walked / stride * WALK_FRAMES), (long) WALK_FRAMES);
        return heading * WALK_FRAMES + frame;
    }

    /**
     * The fall's step at {@code share} of the topple (0 at the loss, 1 at the crush): its first two over the stagger's
     * share of it, the other four over the fall.
     */
    static int fallStep(double share, double staggerShare) {
        if (share < staggerShare) {
            return Math.min(STAGGER_STEPS - 1, (int) (share / staggerShare * STAGGER_STEPS));
        }
        double into = (share - staggerShare) / Math.max(1e-9, 1 - staggerShare);
        return Math.min(FALL_STEPS - 1, STAGGER_STEPS + (int) (into * (FALL_STEPS - STAGGER_STEPS)));
    }

    /** The fall frame's index: per direction and lost side its 6 steps. */
    static int fallFrame(int dir, int side, int step) {
        return (dir * 2 + side) * FALL_STEPS + step;
    }

    /** The lost leg's side: 0 its own left ({@code leg-left}, screen right facing down), 1 its right. */
    static int side(String part) {
        return part.endsWith("left") ? 0 : 1;
    }

    /** The eye's frame over a telegraph {@code share} through (its first glimmer to nearly live), live while sweeping. */
    static int eyeFrame(boolean sweeping, double share, int frames) {
        if (sweeping) {
            return frames - 1;
        }
        return Math.clamp((int) (share * (frames - 1)), 0, frames - 2);
    }

    /** {@code from} turned toward {@code to} by at most {@code step} radians, the short way round. */
    static double turn(double from, double to, double step) {
        double delta = Math.IEEEremainder(to - from, 2 * Math.PI);
        return Math.abs(delta) <= step ? to : from + Math.signum(delta) * step;
    }

    /** The bearing from (x, y) to (tx, ty), radians clockwise from straight down (y up). */
    static double bearing(double x, double y, double tx, double ty) {
        return Math.atan2(-(tx - x), -(ty - y));
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each step: the heads' and torsos' turns, the guns' clocks, the knees' hits, deaths, doors and bursts. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            if (r == NONE) {
                continue;
            }
            int s = slotOf(enemy.serial());
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (s < 0) {
                s = slotOf(-1);
                if (s < 0) {
                    continue;
                }
                serial[s] = enemy.serial();
                firstSeen[s] = tick;
                tellStart[s] = NEVER;
                lastFan[s] = NEVER;
                lastSalvo[s] = NEVER;
                heading[s] = r == ROLE_TOWER ? bearing(x, y, shipX, shipY) : enemy.facing();
                toppled[s] = false;
                for (int leg = 0; leg < 2; leg++) {
                    legShare[s][leg] = 1;
                    kneeHit[s][leg] = NEVER;
                }
            }
            seen[s] = true;
            lastX[s] = x;
            lastY[s] = y;
            lastFacing[s] = enemy.facing();
            if (r == ROLE_TOWER) {
                if (enemy.sweeping()) {
                    heading[s] = enemy.beam(1);
                } else if (!enemy.empStunned()) {
                    double aim = enemy.telegraphing() ? enemy.sweepFrom() : bearing(x, y, shipX, shipY);
                    heading[s] = turn(heading[s], aim, HEAD_TURN);
                }
            } else {
                followStrider(enemy, s, x, y, shipX, shipY, tick);
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s] && serial[s] >= 0) {
                // Gone: a death leaves its remains (the destroyed event comes in the same step), a unit that walked
                // off leaves nothing.
                serial[s] = -1;
                goneAt[s] = tick;
            }
        }
        tells(sortie);
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            switch (events.type(i)) {
                case ENEMY_FIRED -> fired(sortie, x, y, tick);
                case MISSILE_LAUNCHED -> launched(sortie, x, y, tick);
                case ENEMY_DESTROYED -> destroyed(sortie, events.value(i), x, y, tick);
                case CHARGE_BLAST -> {
                    blastTick[nextBlast] = tick;
                    blastX[nextBlast] = x;
                    blastGroundY[nextBlast] = y + sortie.groundScroll();
                    nextBlast = (nextBlast + 1) % PIECES;
                }
                case GROUND_DESTROYED -> opened(sortie, events.value(i), x, y, tick);
                default -> {}
            }
        }
    }

    private void followStrider(Enemy enemy, int s, double x, double y, double shipX, double shipY, long tick) {
        if (enemy.toppling()) {
            if (!toppled[s]) {
                toppled[s] = true;
                fallDir[s] = EnemyLooks.heading(enemy.facing(), FALL_DIRS);
                fallSide[s] = lostSide(enemy);
            }
        } else if (!enemy.empStunned() && !fanTelling(enemy, s, tick)) {
            heading[s] = turn(heading[s], bearing(x, y, shipX, shipY), TORSO_TURN);
        }
        for (int p = 0; p < enemy.partCount(); p++) {
            EnemySpec.UnitPart part = enemy.spec().parts().get(p);
            if (!part.topples()) {
                continue;
            }
            int leg = side(part.name());
            double share = enemy.partShare(p);
            if (share < legShare[s][leg]) {
                kneeHit[s][leg] = tick;
            }
            legShare[s][leg] = share;
        }
    }

    /** The side of a toppling walker's lost leg (the first topple part down). */
    private static int lostSide(Enemy enemy) {
        for (int p = 0; p < enemy.partCount(); p++) {
            EnemySpec.UnitPart part = enemy.spec().parts().get(p);
            if (part.topples() && !enemy.partAlive(p)) {
                return side(part.name());
            }
        }
        return 0;
    }

    /** A shot at (x, y): a Strider's fan if it lies on one's centre (the burst's later shots keep its start). */
    private void fired(Sortie sortie, double x, double y, long tick) {
        Enemy strider = nearest(sortie, x, y, REACH, false);
        if (strider == null) {
            return;
        }
        int s = slotOf(strider.serial());
        EnemyGun gun = strider.spec().gun().orElse(null);
        if (s >= 0 && gun != null && tick - lastFan[s] > SimStep.ticks(gun.intervalSeconds() / 2)) {
            lastFan[s] = tick;
        }
    }

    /** A missile launched at (x, y): a Strider's salvo if it lies on one's pod. */
    private void launched(Sortie sortie, double x, double y, long tick) {
        Enemy strider = nearest(sortie, x, y, POD_REACH, true);
        if (strider == null) {
            return;
        }
        int s = slotOf(strider.serial());
        if (s >= 0) {
            lastSalvo[s] = tick;
        }
    }

    /** The Strider whose centre (or pod) lies nearest (x, y) within {@code reach}, null for none. */
    private Enemy nearest(Sortie sortie, double x, double y, double reach, boolean atPod) {
        Enemy found = null;
        double best = reach;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (roleOf(enemy.kind()) != ROLE_STRIDER) {
                continue;
            }
            int p = atPod ? podPart(enemy) : -1;
            double ex = p >= 0 ? enemy.partX(p) : enemy.renderX(1);
            double ey = p >= 0 ? enemy.partY(p) : enemy.renderY(1);
            double distance = Math.hypot(ex - x, ey - y);
            if (distance <= best) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    /** The walker's pod (its part that carries the salvo), -1 for none. */
    static int podPart(Enemy enemy) {
        for (int p = 0; p < enemy.partCount(); p++) {
            if (enemy.spec().parts().get(p).silencesSpit()) {
                return p;
            }
        }
        return -1;
    }

    /** A unit died at (x, y): a tower's head topples off; a Strider leaves its wreck (toppled) or its hulk. */
    private void destroyed(Sortie sortie, int kind, double x, double y, long tick) {
        int r = roleOf(kind);
        double groundY = y + sortie.groundScroll();
        if (r == ROLE_TOWER) {
            if (towerTopple.size > 0) {
                toppleTick[nextTopple] = tick;
                toppleX[nextTopple] = x;
                toppleGroundY[nextTopple] = groundY;
                nextTopple = (nextTopple + 1) % PIECES;
            }
            return;
        }
        if (r != ROLE_STRIDER) {
            return;
        }
        int s = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int k = 0; k < SLOTS; k++) {
            double distance = Math.hypot(lastX[k] - x, lastY[k] - y);
            if (goneAt[k] == tick && distance < best && distance < 48) {
                best = distance;
                s = k;
            }
        }
        if (s >= 0 && toppled[s] && wreck.size == FALL_DIRS * 2) {
            int g = fallDir[s] * 2 + fallSide[s];
            remain(wreck, g, lastX[s], lastY[s] + sortie.groundScroll(), tick, fallAnchor[g]);
        } else if (hulk.size == LEG_HEADINGS) {
            double facing = s >= 0 ? lastFacing[s] : 0;
            remain(hulk, EnemyLooks.heading(facing, LEG_HEADINGS), x, groundY, tick, null);
        }
    }

    /** A ground object was destroyed: a blast door slides open, the repair crate opens (their last frame stays). */
    private void opened(Sortie sortie, int index, double x, double y, long tick) {
        List<GroundObjectSpec> objects = sortie.script().groundObjects();
        if (index < 0 || index >= objects.size()) {
            return;
        }
        String look = objects.get(index).look();
        if (!OBJECTS.contains(look) || look.equals(CHARGE) || !sprites.has(look + "-open")) {
            return;
        }
        remain(sprites.frames(look + "-open"), -1, x, y + sortie.groundScroll(), tick, null);
    }

    /** Something left on the ground: one frame of {@code frames} (-1: the set played once, its last frame staying). */
    private void remain(Array<AtlasRegion> frames, int frame, double x, double groundY, long tick, double[] anchor) {
        remainFrames.set(nextRemain, frames);
        remainFrame[nextRemain] = frame;
        remainTick[nextRemain] = tick;
        remainX[nextRemain] = x;
        remainGroundY[nextRemain] = groundY;
        remainAnchorX[nextRemain] = anchor == null ? Double.NaN : anchor[0];
        remainAnchorY[nextRemain] = anchor == null ? Double.NaN : anchor[1];
        nextRemain = (nextRemain + 1) % PIECES;
    }

    private int slotOf(int unit) {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] == unit) {
                return s;
            }
        }
        return -1;
    }

    /** Steps until a gun's next shot on its clock: its interval after the last, or its first delay after it came on. */
    static long until(long last, long first, double interval, double firstDelay, long tick) {
        long due = last > NEVER ? last + SimStep.ticks(interval) : first + SimStep.ticks(firstDelay);
        return due - tick;
    }

    /** Whether the Strider is in its fan's 0.3 s tell (the torso holds still, the muzzles glow). */
    private boolean fanTelling(Enemy enemy, int s, long tick) {
        return fanShare(enemy, s, tick) >= 0;
    }

    private double fanShare(Enemy enemy, int s, long tick) {
        EnemyGun gun = enemy.spec().gun().orElse(null);
        if (gun == null || enemy.toppling() || enemy.empStunned()) {
            return -1;
        }
        long until = until(lastFan[s], firstSeen[s], gun.intervalSeconds(), gun.firstShotDelay(), tick);
        return CeresLooks.tellShare(until, FAN_TELL_SECONDS);
    }

    /** Whether the Strider's pod has its hatch open: over the salvo's tell before its predicted launch. */
    private boolean hatchOpen(Enemy enemy, int s, long tick) {
        EnemyGun gun = enemy.spec().walker().flatMap(EnemySpec.Walker::spit).orElse(null);
        if (gun == null || gun.homing().isEmpty() || enemy.toppling() || enemy.empStunned()) {
            return false;
        }
        double interval = gun.intervalSeconds();
        long until = until(lastSalvo[s], firstSeen[s], interval, interval / 2, tick);
        return CeresLooks.tellShare(until, gun.homing().get().hatchSeconds()) >= 0;
    }

    // --- drawing -----------------------------------------------------------------------------------

    /** On the ground under the ground units: the Striders' wrecks and hulks, the opened doors and crates. */
    void drawGround(SpriteBatch batch, Sortie sortie, double scroll) {
        long tick = sortie.tick();
        for (int k = 0; k < PIECES; k++) {
            Array<AtlasRegion> frames = remainFrames.get(k);
            if (frames == null || frames.size == 0) {
                continue;
            }
            double y = remainGroundY[k] - scroll;
            if (y < -100 || y > vanguard.sim.PlayField.HEIGHT + 100) {
                continue;
            }
            int frame = remainFrame[k] >= 0
                    ? remainFrame[k]
                    : (int) Math.min(frames.size - 1, Math.max(0, (tick - remainTick[k]) / OPEN_FRAME_TICKS));
            AtlasRegion region = frames.get(Math.min(frame, frames.size - 1));
            if (Double.isNaN(remainAnchorX[k])) {
                drawCentred(batch, region, remainX[k], y);
            } else {
                anchored(batch, region, remainX[k], y, remainAnchorX[k], remainAnchorY[k]);
            }
        }
    }

    /** A frame drawn with its anchor (px from its top left) on (x, y) of the play field. */
    private static void anchored(SpriteBatch batch, AtlasRegion region, double x, double y, double ax, double ay) {
        batch.draw(region, Math.round(PlayFrame.X0 + x - ax), Math.round(y - (region.getRegionHeight() - ay)));
    }

    /** The unit, whole: a tower's collar and head, a Strider's legs, torso and pod (or its fall), hit flashes on each. */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        int s = slotOf(enemy.serial());
        if (roleOf(enemy.kind()) == ROLE_TOWER) {
            if (collar != null) {
                flasher.draw(batch, enemy, collar, x, y);
            }
            double aim = enemy.sweeping() ? enemy.beam(alpha) : s >= 0 ? heading[s] : 0;
            if (head.size == HEAD_HEADINGS) {
                flasher.draw(batch, enemy, head.get(EnemyLooks.heading(aim, HEAD_HEADINGS)), x, y);
            }
            return;
        }
        if (enemy.toppling() && s >= 0 && fall.size == FALL_DIRS * 2 * FALL_STEPS) {
            var topple = enemy.spec().topple().orElseThrow();
            double stagger = topple.staggerSeconds() / (topple.staggerSeconds() + topple.fallSeconds());
            int step = fallStep(enemy.toppleShare(alpha), stagger);
            int g = fallDir[s] * 2 + fallSide[s];
            anchored(
                    batch,
                    fall.get(fallFrame(fallDir[s], fallSide[s], step)),
                    x,
                    y,
                    fallAnchor[g][0],
                    fallAnchor[g][1]);
            return;
        }
        int legHeading = EnemyLooks.heading(enemy.facing(), LEG_HEADINGS);
        double stride = enemy.spec().walker().map(EnemySpec.Walker::stride).orElse(32.0);
        if (legs.size == LEG_HEADINGS * WALK_FRAMES) {
            flasher.draw(batch, enemy, legs.get(legFrame(legHeading, enemy.walked(), stride)), x, y);
        }
        int aim = EnemyLooks.heading(s >= 0 ? heading[s] : enemy.facing(), TORSO_HEADINGS);
        if (torso.size == TORSO_HEADINGS) {
            flasher.draw(batch, enemy, torso.get(aim), x, y);
        }
        int p = podPart(enemy);
        if (p >= 0 && enemy.partAlive(p) && pod.size == TORSO_HEADINGS * 2) {
            boolean open = s >= 0 && hatchOpen(enemy, s, sortie.tick());
            flasher.draw(batch, enemy, pod.get(aim * 2 + (open ? 1 : 0)), x + podAt[aim][0], y + podAt[aim][1]);
        }
    }

    /**
     * Over the ground units, additive (the batch is): the towers' telegraphs (the arc's wedge, or a full turn's reach
     * ring and dim start ray, pulsing crimson) and the toppling Striders' fall lines over the stagger.
     */
    void drawTells(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (r == ROLE_TOWER && enemy.telegraphing()) {
                float pulse = 0.45f + 0.35f * (float) Math.abs(Math.sin(tick * 0.4));
                var sweep = enemy.spec().sweep().orElseThrow();
                if (enemy.sweepFullTurn()) {
                    if (ring != null) {
                        batch.setColor(1, 1, 1, Math.min(1, 0.6f + 0.5f * pulse));
                        drawCentred(batch, ring, x, y);
                    }
                    if (beam.size > 0) {
                        batch.setColor(1, 1, 1, 0.35f + 0.3f * pulse);
                        FarsideLooks.drawBeam(batch, beam.first(), x, y, enemy.sweepFrom(), sweep.length());
                    }
                } else {
                    int degrees = (int) Math.round(Math.toDegrees(sweep.arcRadians()));
                    String name = TOWER + "-telegraph-" + degrees;
                    if (sprites.has(name)) {
                        AtlasRegion wedge = sprites.region(name);
                        float h = wedge.getRegionHeight();
                        batch.setColor(1, 1, 1, Math.min(1, 0.6f + 0.5f * pulse));
                        batch.draw(
                                wedge,
                                (float) (PlayFrame.X0 + x),
                                (float) (y - h / 2),
                                0,
                                h / 2,
                                wedge.getRegionWidth(),
                                h,
                                1,
                                1,
                                FarsideLooks.degrees(enemy.sweepCentre()));
                    } else {
                        batch.setColor(CRIMSON.r, CRIMSON.g, CRIMSON.b, pulse);
                        stroke(batch, x, y, enemy.sweepFrom(), sweep.length(), 2);
                        stroke(batch, x, y, enemy.sweepTo(), sweep.length(), 2);
                    }
                }
            } else if (r == ROLE_STRIDER && enemy.toppling()) {
                var topple = enemy.spec().topple().orElseThrow();
                double dx = enemy.fallDirX();
                double dy = enemy.fallDirY();
                if (dx == 0 && dy == 0) {
                    continue;
                }
                // The strip from its centre toward the lost leg: bright over the stagger, fading over the fall.
                float strength = enemy.staggering()
                        ? 0.35f + 0.25f * (float) Math.abs(Math.sin(tick * 0.5))
                        : 0.3f * (float) (1 - enemy.toppleShare(alpha));
                double heading = Math.atan2(-dx, -dy);
                batch.setColor(FALL_LINE.r, FALL_LINE.g, FALL_LINE.b, strength);
                stroke(batch, x, y, heading, topple.lineLength(), (float) topple.lineWidth());
                batch.setColor(FALL_LINE.r, FALL_LINE.g, FALL_LINE.b, strength * 1.5f);
                stroke(batch, x, y, heading, topple.lineLength(), 2);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** A stroke from (x, y) along {@code heading} (radians clockwise from straight down), {@code length} long. */
    private void stroke(SpriteBatch batch, double x, double y, double heading, double length, float width) {
        batch.draw(
                pixel,
                (float) (PlayFrame.X0 + x),
                (float) (y - width / 2),
                0,
                width / 2,
                (float) length,
                width,
                1,
                1,
                FarsideLooks.degrees(heading));
    }

    /**
     * The lights, additive (the batch is): the towers' eyes, beams and tips; the Striders' hit knees and muzzles over the
     * fan's tell; the charges' bursts.
     */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            int s = slotOf(enemy.serial());
            if (r == NONE || s < 0 || enemy.empStunned()) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (r == ROLE_TOWER) {
                drawEye(batch, enemy, s, x, y, alpha, tick);
                continue;
            }
            if (enemy.toppling()) {
                continue;
            }
            int legFrame = legFrame(
                    EnemyLooks.heading(enemy.facing(), LEG_HEADINGS),
                    enemy.walked(),
                    enemy.spec().walker().map(EnemySpec.Walker::stride).orElse(32.0));
            for (int leg = 0; leg < 2; leg++) {
                long age = tick - kneeHit[s][leg];
                if (knee.size > 0 && age >= 0 && age < (long) knee.size * KNEE_FRAME_TICKS) {
                    double[] at = knees[legFrame][leg];
                    drawCentred(batch, knee.get((int) (age / KNEE_FRAME_TICKS)), x + at[0], y + at[1]);
                }
            }
            double share = fanShare(enemy, s, tick);
            if (share >= 0 && knee.size > 0) {
                int aim = EnemyLooks.heading(heading[s], TORSO_HEADINGS);
                AtlasRegion glow = knee.get(Math.min(knee.size - 1, (int) (share * knee.size)));
                batch.setColor(1, 0.75f, 0.4f, 1);
                for (double[] muzzle : muzzles[aim]) {
                    drawCentred(batch, glow, x + muzzle[0], y + muzzle[1]);
                }
                batch.setColor(Color.WHITE);
            }
        }
        double scroll = sortie.groundScroll();
        for (int b = 0; b < PIECES; b++) {
            long age = tick - blastTick[b];
            if (blastTick[b] == NEVER || blast.size == 0 || age < 0 || age >= (long) blast.size * BLAST_FRAME_TICKS) {
                continue;
            }
            drawCentred(batch, blast.get((int) (age / BLAST_FRAME_TICKS)), blastX[b], blastGroundY[b] - scroll);
        }
    }

    /** A tower's eye on its lens through the telegraph and the sweep, the beam from the lens and its tip. */
    private void drawEye(SpriteBatch batch, Enemy enemy, int s, double x, double y, float alpha, long tick) {
        boolean sweeping = enemy.sweeping();
        if (!sweeping && !enemy.telegraphing()) {
            return;
        }
        var sweep = enemy.spec().sweep().orElseThrow();
        double aim = sweeping ? enemy.beam(alpha) : heading[s];
        double[] at = lens[EnemyLooks.heading(aim, HEAD_HEADINGS)];
        double lx = x + at[0];
        double ly = y + at[1];
        if (sweeping && beam.size > 0) {
            int frame = (int) (tick / 2 % beam.size);
            double reach = sweep.length() - Math.hypot(at[0], at[1]);
            FarsideLooks.drawBeam(batch, beam.get(frame), lx, ly, aim, reach);
            if (beamTip.size > 0) {
                drawCentred(
                        batch,
                        beamTip.get(frame % beamTip.size),
                        x - Math.sin(aim) * sweep.length(),
                        y - Math.cos(aim) * sweep.length());
            }
        }
        if (eye.size > 0) {
            double share = 0;
            if (!sweeping) {
                double tell = enemy.sweepFullTurn()
                        ? enemy.spec().groundSweep().orElseThrow().fullTurnTelegraphSeconds()
                        : sweep.telegraphSeconds();
                share = Math.min(1, (tick - telegraphStart(s, tick)) / (double) SimStep.ticks(tell));
            }
            drawCentred(batch, eye.get(eyeFrame(sweeping, share, eye.size)), lx, ly);
        }
    }

    /** The step a tower's telegraph started (followed per slot), for its eye's glimmer. */
    private long telegraphStart(int s, long tick) {
        return tellStart[s] == NEVER ? tick : tellStart[s];
    }

    /** Marks the towers' telegraph starts, each step. */
    private void tells(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (roleOf(enemy.kind()) != ROLE_TOWER) {
                continue;
            }
            int s = slotOf(enemy.serial());
            if (s < 0) {
                continue;
            }
            if (enemy.telegraphing()) {
                if (tellStart[s] == NEVER) {
                    tellStart[s] = sortie.tick();
                }
            } else {
                tellStart[s] = NEVER;
            }
        }
    }

    /** The towers' heads toppling off over their bursts (solid). */
    void drawPieces(SpriteBatch batch, Sortie sortie, double scroll) {
        long tick = sortie.tick();
        for (int t = 0; t < PIECES; t++) {
            long age = tick - toppleTick[t];
            if (toppleTick[t] == NEVER
                    || towerTopple.size == 0
                    || age < 0
                    || age >= (long) towerTopple.size * TOPPLE_FRAME_TICKS) {
                continue;
            }
            drawCentred(
                    batch, towerTopple.get((int) (age / TOPPLE_FRAME_TICKS)), toppleX[t], toppleGroundY[t] - scroll);
        }
    }

    /** Level 32's ground targets these looks draw ({@link #drawObject}), after {@link GroundPasses}' own. */
    void drawObjects(SpriteBatch batch, Sortie sortie, float alpha, FlashShader flash, float whiteFlash) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (drawsObject(object)) {
                drawObject(batch, sortie, object, alpha, flash, whiteFlash);
            }
        }
    }

    /**
     * Level 32's ground targets (in place of {@link GroundPasses}' frames): a door's lock with its lamp pulsing (the
     * sealed door's dark), a charge's lamp blinking, the repair crate; an unchosen branch's dark, a hit flashing white.
     */
    void drawObject(
            SpriteBatch batch, Sortie sortie, GroundObject object, float alpha, FlashShader flash, float whiteFlash) {
        String look = object.spec().look();
        Array<AtlasRegion> frames = sprites.frames(look);
        int frame = frames.size <= 1 ? 0 : (int) (sortie.tick() / LAMP_FRAME_TICKS % frames.size);
        if (look.startsWith("blast-door") && frames.size == 4) {
            // The amber lamp's pulse: up and down its four steps.
            int phase = (int) (sortie.tick() / LAMP_FRAME_TICKS % 6);
            frame = phase < 4 ? phase : 6 - phase;
        }
        AtlasRegion region = frames.get(frame);
        double x = object.renderX(alpha);
        double y = object.renderY(alpha);
        if (object.dormant()) {
            batch.setColor(DORMANT, DORMANT, DORMANT + 0.05f, 1);
            drawCentred(batch, region, x, y);
            batch.setColor(Color.WHITE);
        } else if (object.ticksSinceHit() < HIT_FLASH_TICKS) {
            flash.draw(batch, region, Math.round(PlayFrame.X0 + x), Math.round(y), HIT_WHITE, whiteFlash);
        } else {
            drawCentred(batch, region, x, y);
        }
    }
}
