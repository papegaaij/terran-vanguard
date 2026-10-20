package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.Layer;
import vanguard.sim.Missile;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * The autopilot's moves (see {@link Autopilot}): the steer toward a target and the boss-fight planner, a short
 * look-ahead over the bullets, missiles and air enemies (and a walker's cone or a slam lane), and the escape along a
 * direction.
 */
final class MovePlanner {
    private MovePlanner() {}

    static final double DEAD_ZONE = 4;

    static int steer(double shipX, double shipY, double targetX, double targetY) {
        int commands = 0;
        if (targetX < shipX - DEAD_ZONE) {
            commands |= Command.LEFT.bit();
        } else if (targetX > shipX + DEAD_ZONE) {
            commands |= Command.RIGHT.bit();
        }
        if (targetY < shipY - DEAD_ZONE) {
            commands |= Command.DOWN.bit();
        } else if (targetY > shipY + DEAD_ZONE) {
            commands |= Command.UP.bit();
        }
        return commands;
    }

    /** Round 41: the command of the eight ways that moves the ship furthest along (nx, ny), none into a wall it is at. */
    static int escape(Sortie sortie, double nx, double ny) {
        double x = sortie.ship().x();
        double y = sortie.ship().y();
        double margin = sortie.ship().spec().edgeLimit();
        int best = 0;
        double most = Double.NEGATIVE_INFINITY;
        for (int sx = -1; sx <= 1; sx++) {
            for (int sy = -1; sy <= 1; sy++) {
                if (sx == 0 && sy == 0) {
                    continue;
                }
                double scale = sx != 0 && sy != 0 ? Math.sqrt(0.5) : 1;
                double vx =
                        (sx < 0 && x <= margin + 1) || (sx > 0 && x >= PlayField.WIDTH - margin - 1) ? 0 : sx * scale;
                double vy =
                        (sy < 0 && y <= margin + 1) || (sy > 0 && y >= PlayField.HEIGHT - margin - 1) ? 0 : sy * scale;
                double along = vx * nx + vy * ny;
                if (along > most) {
                    most = along;
                    best = (sx > 0 ? Command.RIGHT.bit() : sx < 0 ? Command.LEFT.bit() : 0)
                            | (sy > 0 ? Command.UP.bit() : sy < 0 ? Command.DOWN.bit() : 0);
                }
            }
        }
        return best;
    }

    /** The moves the boss-fight planner weighs: stay, the four sides, the four diagonals. */
    static final int[] MOVES = {
        0,
        Command.LEFT.bit(),
        Command.RIGHT.bit(),
        Command.UP.bit(),
        Command.DOWN.bit(),
        Command.LEFT.bit() | Command.UP.bit(),
        Command.RIGHT.bit() | Command.UP.bit(),
        Command.LEFT.bit() | Command.DOWN.bit(),
        Command.RIGHT.bit() | Command.DOWN.bit()
    };

    static final double DIAGONAL = Math.sqrt(0.5);
    static final double[] MOVE_X = {0, -1, 1, 0, 0, -DIAGONAL, DIAGONAL, -DIAGONAL, DIAGONAL};
    static final double[] MOVE_Y = {0, 0, 0, 1, -1, DIAGONAL, DIAGONAL, -DIAGONAL, -DIAGONAL};
    /** How far ahead the planner looks, s, in samples of {@link #SAMPLE} s. */
    private static final int SAMPLES = 12;

    private static final double SAMPLE = 0.05;
    /** The ship's hull from its centre, px, with a small bullet's radius added. */
    static final double HULL_X = 22;

    static final double HULL_Y = 25;
    /** A move is safe when nothing comes closer to the hull than this in the look-ahead, px. */
    private static final double SAFE = 6;
    /** The band of the field it prefers while it dodges a boss, px from the left. */
    static final double BAND_LEFT = 130;

    static final double BAND_RIGHT = PlayField.WIDTH - 130.0;

    /**
     * In a boss fight: the move toward the boss's vital part ({@code targetX}, {@code targetY}) if
     * no bullet or air enemy comes too close to the hull while it flies it for the next 0.6 s (each
     * flying straight on, the ship at full speed); otherwise the safe move that keeps nearest the
     * target and the field's centre band (it keeps away from the edges, where an aimed burst traps
     * it); with no safe move, the one that keeps farthest from them.
     */
    static int bossMove(Sortie sortie, double targetX, double targetY) {
        return bossMove(sortie, targetX, targetY, null);
    }

    /** As {@link #bossMove(Sortie, double, double)}; with {@code lanes} (M5 part E) a move into a lane about to be slammed is a hit. */
    static int bossMove(Sortie sortie, double targetX, double targetY, vanguard.sim.SlamArena lanes) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int wanted = steer(shipX, shipY, targetX, targetY);
        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        int clearest = 0;
        double clearestDistance = Double.NEGATIVE_INFINITY;
        for (int m = 0; m < MOVES.length; m++) {
            double clearance = clearance(sortie, MOVE_X[m], MOVE_Y[m], lanes);
            if (clearance > clearestDistance) {
                clearestDistance = clearance;
                clearest = m;
            }
            if (clearance < SAFE) {
                continue;
            }
            if (MOVES[m] == wanted) {
                return wanted;
            }
            double x = ahead(shipX, MOVE_X[m], sortie, true);
            double y = ahead(shipY, MOVE_Y[m], sortie, false);
            double score = -Math.abs(x - targetX)
                    - 0.5 * Math.abs(y - targetY)
                    - 2 * Math.max(0, Math.max(BAND_LEFT - x, x - BAND_RIGHT));
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return MOVES[best >= 0 ? best : clearest];
    }

    /** Where a move takes the ship in two samples, inside its bounds. */
    private static double ahead(double from, double direction, Sortie sortie, boolean horizontal) {
        double margin = sortie.ship().spec().edgeLimit();
        double to = from + direction * sortie.ship().spec().speed() * 2 * SAMPLE;
        return Math.clamp(to, margin, (horizontal ? PlayField.WIDTH : PlayField.HEIGHT) - margin);
    }

    /**
     * The closest any enemy bullet or air enemy comes to the hull's box over the look-ahead while
     * the ship flies (dx, dy) at full speed, px (0 = a hit).
     */
    private static double clearance(Sortie sortie, double dx, double dy, vanguard.sim.SlamArena lanes) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double nearest = Double.POSITIVE_INFINITY;
        SetPiece walker = WalkerPlay.walkingBoss(sortie);
        boolean cone = walker != null && WalkerPlay.coneActive(walker);
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            double x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            double y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            if (cone && WalkerPlay.coneDanger(walker, x, y, WalkerPlay.CONE_INNER)) {
                // M7 part F: a move into the walker's telling or burning cone is a hit.
                return 0;
            }
            if (lanes != null && y - HULL_Y < lanes.laneTop()) {
                for (int lane = 1; lane <= lanes.laneCount(); lane++) {
                    double left = (lane - 1) * lanes.laneWidth();
                    if (lanes.untilImpact(lane) <= t + KrakenPlay.LANE_WARNING
                            && x + HULL_X + KrakenPlay.LANE_GAP > left
                            && x - HULL_X - KrakenPlay.LANE_GAP < left + lanes.laneWidth()) {
                        return 0;
                    }
                }
            }
            for (int i = 0; i < sortie.bulletCount(); i++) {
                EnemyBullet bullet = sortie.bullet(i);
                double bx = bullet.renderX(1) + (bullet.renderX(1) - bullet.renderX(0)) * steps;
                double by = bullet.renderY(1) + (bullet.renderY(1) - bullet.renderY(0)) * steps;
                nearest = Math.min(nearest, gap(bx - x, by - y, HULL_X, HULL_Y));
            }
            for (int i = 0; i < sortie.missileCount(); i++) {
                // M7 part F: the walker's homing missiles, flown straight on (they turn slower than the ship moves).
                Missile missile = sortie.missile(i);
                double mx = missile.renderX(1) + (missile.renderX(1) - missile.renderX(0)) * steps;
                double my = missile.renderY(1) + (missile.renderY(1) - missile.renderY(0)) * steps;
                nearest = Math.min(nearest, gap(mx - x, my - y, HULL_X, HULL_Y));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                // M7 part B: nor a buried one (it touches nothing).
                if (enemy.layer() == Layer.GROUND || !enemy.layer().touchable()) {
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        gap(
                                ex - x,
                                ey - y,
                                HULL_X + enemy.hitbox().width() / 2,
                                HULL_Y + enemy.hitbox().height() / 2));
            }
        }
        return nearest;
    }

    /** The distance between a point (dx, dy) from a box's centre and the box (halfW, halfH); 0 inside. */
    static double gap(double dx, double dy, double halfW, double halfH) {
        return Math.hypot(Math.max(0, Math.abs(dx) - halfW), Math.max(0, Math.abs(dy) - halfH));
    }
}
