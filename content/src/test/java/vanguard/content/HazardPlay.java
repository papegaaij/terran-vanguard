package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.FurnaceVents;
import vanguard.sim.Hitbox;
import vanguard.sim.PlayField;
import vanguard.sim.RelayFlash;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * The autopilot's hazards that filter or bend its move (see {@link Autopilot}): a gust's lean (M5 part F), the
 * canyon's rock (M7 part C), the furnace vents' hot grates (M7 part F) and the mass relay's flash (M9 part H).
 */
final class HazardPlay {
    private HazardPlay() {}

    /** M5 part F: in a gust (from its telegraph on) it aims this far upwind of its target, px. */
    private static final double LEAN = 12;

    /** M5 part F: ... and keeps at least this far from the downwind edge, px. */
    private static final double DOWNWIND_EDGE = 70;

    /** M7 part C: the half width of the ship's hull kept off rock, px, and the margin beyond it. */
    private static final double HULL_HALF = 18;

    /** M7 part C: the margin beyond the hull it keeps from rock, px (an anchor at a wall's foot stays in reach). */
    static final double ROCK_MARGIN = 8;

    /** M7 part C: how far ahead it looks at the rock coming down at its height, s (walls move ≤ 115 px/s). */
    static final double ROCK_AHEAD = 0.6;

    /** M7 part C: the half height of the hull it checks the rock over, px. */
    private static final double HULL_HALF_HEIGHT = 22;

    /**
     * M5 part F: a target's x in a gust, from its telegraph on (Level 12's wind): a little upwind, and
     * clear of the downwind edge, so the push does not pin the ship there; the target as it is in calm air
     * and in a level without weather.
     */
    static double leanX(Sortie sortie, double targetX) {
        if (!sortie.wind().present()) {
            return targetX;
        }
        double push = sortie.windPush();
        int telegraph = sortie.wind().telegraph(SimStep.ticks(sortie.levelSeconds()));
        int from = telegraph != 0 ? telegraph : (int) Math.signum(push);
        if (from == 0) {
            return targetX;
        }
        double leaned = targetX - from * LEAN;
        return from > 0 ? Math.min(leaned, PlayField.WIDTH - DOWNWIND_EDGE) : Math.max(leaned, DOWNWIND_EDGE);
    }

    /**
     * M7 part C: {@code commands} kept off the rock (Level 16): the span of the channel it flies (the {@code channel}
     * side where rock splits the canyon) at its hull's height now and over the next {@link #ROCK_AHEAD} s, less the
     * hull and {@link #ROCK_MARGIN}; a move out of it is dropped, and outside it it moves back in.
     */
    static int canyon(Sortie sortie, int commands, Autopilot.Channel channel) {
        Terrain terrain = sortie.terrain();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double scroll = sortie.groundScroll();
        double speed = sortie.groundSpeed();
        // M9 part E: a forked level's route picks the channel a fork's island ahead splits (RoutePlay).
        channel = RoutePlay.channel(sortie, channel, Terrain.row(scroll, shipY));
        double lo = Double.NEGATIVE_INFINITY;
        double hi = Double.POSITIVE_INFINITY;
        // M9 part E: into or out of a tower's notch (Level 32's gallery, TowerPlay) the notches are open and the pilot
        // moves with the scroll: its hull's rows now, padded, instead of the look-ahead.
        boolean notched = TowerPlay.notched(sortie);
        double rockAhead = notched ? 0 : SirenPlay.rockAhead(sortie, terrain);
        double hullRows = notched ? HULL_HALF_HEIGHT + TowerPlay.NOTCH_PAD : HULL_HALF_HEIGHT;
        for (double ahead = 0; ahead <= rockAhead + 1e-9; ahead += 0.1) {
            double at = scroll + speed * ahead;
            for (double dy = -hullRows; dy <= hullRows + 1e-9; dy += notched ? hullRows / 2 : HULL_HALF_HEIGHT) {
                lo = Math.max(lo, channelLeft(terrain, at, shipY + dy, channel, notched));
                hi = Math.min(hi, channelRight(terrain, at, shipY + dy, channel, notched));
            }
        }
        double margin = HULL_HALF + ROCK_MARGIN;
        if (hi - lo < 2 * margin) {
            // The rock moves faster than the look-ahead allows: the span at its height now.
            lo = channelLeft(terrain, scroll, shipY, channel, notched);
            hi = channelRight(terrain, scroll, shipY, channel, notched);
            margin = Math.min(margin, (hi - lo) / 2);
        }
        // M8 part E: a nest on a wall's foot (Level 25's Sirens) is rock to its hull while it scrolls past its height.
        double nestLo = nestLeft(sortie, terrain, lo);
        double nestHi = nestRight(sortie, terrain, hi);
        if (nestHi - nestLo >= 2 * margin) {
            lo = nestLo;
            hi = nestHi;
        }
        double left = lo + margin;
        double right = hi - margin;
        // M8 part C: under water it brakes before its slide carries it onto the rock (WaterPlay; none in the air).
        commands = WaterPlay.keepInSpan(sortie, commands, left, right);
        int horizontal = Command.LEFT.bit() | Command.RIGHT.bit();
        if (shipX < left - MovePlanner.DEAD_ZONE) {
            return (commands & ~horizontal) | Command.RIGHT.bit();
        }
        if (shipX > right + MovePlanner.DEAD_ZONE) {
            return (commands & ~horizontal) | Command.LEFT.bit();
        }
        if (Command.LEFT.in(commands) && shipX <= left) {
            // M9 part E: in the rock a dodge pinned against a wall goes the other way (dodged).
            return dodged(sortie, commands)
                    ? (commands & ~Command.LEFT.bit()) | Command.RIGHT.bit()
                    : commands & ~Command.LEFT.bit();
        }
        if (Command.RIGHT.in(commands) && shipX >= right) {
            return dodged(sortie, commands)
                    ? (commands & ~Command.RIGHT.bit()) | Command.LEFT.bit()
                    : commands & ~Command.RIGHT.bit();
        }
        return commands;
    }

    /**
     * M9 part E (Level 32): whether {@code commands} are a dodge ({@link DodgePlay#threat}) in a level with towers
     * ({@link TowerPlay#towers}), where {@link DodgePlay}'s field edges are walls it can be pinned against; no other
     * level's moves change.
     */
    private static boolean dodged(Sortie sortie, int commands) {
        return TowerPlay.towers(sortie)
                && DodgePlay.threat(sortie, sortie.ship().x(), sortie.ship().y()) != 0;
    }

    /**
     * M8 part E: the left edge {@code lo} moved past the nests of the left wall (Level 25's Sirens: a body on the wall's
     * foot that a hull touching it takes contact from) that come down to its hull's rows within {@link #NEST_AHEAD} s
     * (the slow's share longer while slowed); {@code lo} unchanged without one.
     */
    private static double nestLeft(Sortie sortie, Terrain terrain, double lo) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy nest = sortie.enemy(i);
            if (nest.wallMounted() && leftWall(sortie, terrain, nest) && nearing(sortie, nest)) {
                lo = Math.max(lo, nest.renderX(1) + nest.hitbox().width() / 2);
            }
        }
        return lo;
    }

    /** M8 part E: the right edge {@code hi} moved past the right wall's nests coming down (see {@link #nestLeft}). */
    private static double nestRight(Sortie sortie, Terrain terrain, double hi) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy nest = sortie.enemy(i);
            if (nest.wallMounted() && !leftWall(sortie, terrain, nest) && nearing(sortie, nest)) {
                hi = Math.min(hi, nest.renderX(1) - nest.hitbox().width() / 2);
            }
        }
        return hi;
    }

    /** M8 part E: whether a wall's nest stands on the left wall (left of the trench's middle at its row). */
    private static boolean leftWall(Sortie sortie, Terrain terrain, Enemy nest) {
        double scroll = sortie.groundScroll();
        double y = nest.renderY(1);
        return nest.renderX(1) < (wallLeft(terrain, scroll, y) + wallRight(terrain, scroll, y)) / 2;
    }

    /** M8 part E: whether a wall's nest reaches the hull's rows (with {@link #NEST_GAP}) within the look-ahead. */
    static boolean nearing(Sortie sortie, Enemy nest) {
        double ahead = NEST_AHEAD / (sortie.ship().slow().slowed() ? SirenPlay.SLOW : 1);
        double reach = nest.hitbox().height() / 2 + HULL_HALF_HEIGHT + NEST_GAP;
        double dy = nest.renderY(1) - sortie.ship().y();
        return dy > -reach && dy - sortie.groundSpeed() * ahead < reach;
    }

    /** M8 part E: how far ahead it keeps clear of a wall's nest coming down at its height, s. */
    static final double NEST_AHEAD = 0.9;

    /** M8 part E: the gap it keeps between its hull's rows and a nest's box, px. */
    private static final double NEST_GAP = 8;

    /**
     * M7 part C: the left edge of the flyable span at play-field height {@code y} of the channel the pilot takes (see
     * {@link #channelX}); the wall's line, or the field's edge, where there is no rock split. Each edge on its own
     * rather than a {@code double[]} pair: the allocation tests measure the autopilot's steps too, and such an array
     * only vanishes once C2 has inlined this; the 3-core macOS runner measured C1 code, 21 arrays a step (CI,
     * 2026-10-20).
     */
    private static double channelLeft(
            Terrain terrain, double scroll, double y, Autopilot.Channel channel, boolean notches) {
        double from = terrain.spanLeft(scroll, y, channelX(terrain, scroll, y, channel), notches);
        return Double.isNaN(from) ? wallLeft(terrain, scroll, y) : from;
    }

    /** M7 part C: the right edge of the span {@link #channelLeft} gives the left edge of. */
    private static double channelRight(
            Terrain terrain, double scroll, double y, Autopilot.Channel channel, boolean notches) {
        double to = terrain.spanRight(scroll, y, channelX(terrain, scroll, y, channel), notches);
        return Double.isNaN(to) ? wallRight(terrain, scroll, y) : to;
    }

    /**
     * M7 part C: an x in the channel the pilot takes at play-field height {@code y}: the leftmost or the rightmost
     * one where rock splits the canyon.
     */
    private static double channelX(Terrain terrain, double scroll, double y, Autopilot.Channel channel) {
        return channel == Autopilot.Channel.LEFT
                ? wallLeft(terrain, scroll, y) + 0.5
                : wallRight(terrain, scroll, y) - 0.5;
    }

    /** M7 part C: the left wall's line at play-field height {@code y}; the field's left edge where there is none. */
    private static double wallLeft(Terrain terrain, double scroll, double y) {
        double left = terrain.leftAt(Terrain.row(scroll, y));
        return Double.isNaN(left) ? 0 : left;
    }

    /** M7 part C: the right wall's line at play-field height {@code y}; the field's right edge where there is none. */
    private static double wallRight(Terrain terrain, double scroll, double y) {
        double right = terrain.rightAt(Terrain.row(scroll, y));
        return Double.isNaN(right) ? PlayField.WIDTH : right;
    }

    /** M9 part H: the margin it keeps beyond the relay's band and its hull, px. */
    private static final double RELAY_MARGIN = 8;

    /**
     * M9 part H (Level 35's mass relay, design/campaign Level 35): while the relay's line telegraphs or is live (it locks
     * on the ship's height at its start), a ship whose hull touches its band (with {@link #RELAY_MARGIN}) steps off it
     * up or down, the side with more room, its horizontal move kept; {@code commands} unchanged off the band, before and
     * after the flash and in a level without a relay.
     */
    static int relay(Sortie sortie, int commands) {
        if (sortie.relay().isEmpty()) {
            return commands;
        }
        RelayFlash relay = sortie.relay().get();
        if (!relay.telegraphing() && !relay.live()) {
            return commands;
        }
        double shipY = sortie.ship().y();
        double clear = relay.spec().bandPx() / 2 + MovePlanner.HULL_Y + RELAY_MARGIN;
        if (Math.abs(shipY - relay.lineY()) >= clear) {
            return commands;
        }
        double margin = sortie.ship().spec().edgeLimit();
        boolean up = relay.lineY() + clear < PlayField.HEIGHT - margin
                && (relay.lineY() - clear < margin || shipY >= relay.lineY());
        int vertical = Command.UP.bit() | Command.DOWN.bit();
        return (commands & ~vertical) | (up ? Command.UP.bit() : Command.DOWN.bit());
    }

    /** M7 part F: a furnace vent's grate counts as hot from this share of its tell on (it burns within ≈ 0.6 s). */
    private static final double VENT_HOT = 0.4;
    /** M7 part F: how far ahead it checks the ship against the hot grates, s. */
    private static final double VENT_AHEAD = 0.3;

    /**
     * M7 part F (Level 19's furnace vents): the commands unchanged unless they take the ship into a hot grate (late
     * in its tell or burning) within {@link #VENT_AHEAD} s (the grates scroll with the ground); then the move of the
     * eight, or none, that keeps clear and nearest the wanted one, the fire kept.
     */
    static int ventFilter(Sortie sortie, int commands) {
        int wanted = commands & (Command.LEFT.bit() | Command.RIGHT.bit() | Command.UP.bit() | Command.DOWN.bit());
        if (!ventHit(sortie, wanted)) {
            return commands;
        }
        double wx = ((wanted & Command.RIGHT.bit()) != 0 ? 1 : 0) - ((wanted & Command.LEFT.bit()) != 0 ? 1 : 0);
        double wy = ((wanted & Command.UP.bit()) != 0 ? 1 : 0) - ((wanted & Command.DOWN.bit()) != 0 ? 1 : 0);
        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            if (ventHit(sortie, MovePlanner.MOVES[m])) {
                continue;
            }
            double score = MovePlanner.MOVE_X[m] * wx + MovePlanner.MOVE_Y[m] * wy;
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best < 0 ? commands : (commands & ~wanted) | MovePlanner.MOVES[best];
    }

    /** Whether the move {@code move} takes the ship's hull into a hot grate within {@link #VENT_AHEAD} s. */
    private static boolean ventHit(Sortie sortie, int move) {
        FurnaceVents vents = sortie.vents();
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double mx = ((move & Command.RIGHT.bit()) != 0 ? 1 : 0) - ((move & Command.LEFT.bit()) != 0 ? 1 : 0);
        double my = ((move & Command.UP.bit()) != 0 ? 1 : 0) - ((move & Command.DOWN.bit()) != 0 ? 1 : 0);
        if (mx != 0 && my != 0) {
            mx *= MovePlanner.DIAGONAL;
            my *= MovePlanner.DIAGONAL;
        }
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.present(g)) {
                continue;
            }
            FurnaceVents.Phase phase = vents.phase(g);
            if (phase == FurnaceVents.Phase.IDLE
                    || (phase == FurnaceVents.Phase.TELL && vents.progress(g) < VENT_HOT)) {
                continue;
            }
            Hitbox box = vents.box(g);
            for (int k = 0; k <= 3; k++) {
                double t = VENT_AHEAD * k / 3;
                double x = Math.clamp(sortie.ship().x() + mx * speed * t, margin, PlayField.WIDTH - margin);
                double y = Math.clamp(sortie.ship().y() + my * speed * t, margin, PlayField.HEIGHT - margin);
                double gy = vents.y(g) - sortie.groundSpeed() * t;
                if (MovePlanner.gap(
                                vents.x(g) - x,
                                gy - y,
                                MovePlanner.HULL_X + box.width() / 2 - 4,
                                MovePlanner.HULL_Y + box.height() / 2 - 4)
                        <= 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
