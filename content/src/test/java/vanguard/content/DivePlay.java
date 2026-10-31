package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.Lightning;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M10 part B, step B3 (design/campaign Level 36 Cloud Divers; see {@link Autopilot}): the dive's plays. It holds the beam
 * on one hurt regenerating unit (a Chimera) until it dies, so its healing never undoes the work; it takes the escapes
 * secondary's craft that are not counted as enemies (the helium-3 harvesters) before anything else, from below them,
 * before they reach the bottom edge; and it keeps its hull out of a told or striking lightning column. The drop craft
 * are neutral ({@link HoldFirePlay#neutral}: never a target). Only Act 6 has regenerating units, uncounted escapes
 * craft and strikes, so nothing here moves an Act 1–5 flight.
 */
final class DivePlay {
    private DivePlay() {}

    /** How far below a harvester it holds, px (it must stay above the ship for the beam to reach it). */
    static final double UNDER = 70;

    /** The lowest it flies under a harvester, px from the bottom edge. */
    static final double LOWEST = 40;

    /** The margin it keeps beyond a told column's half width and its hull, px. */
    static final double STRIKE_MARGIN = 8;

    /** Beyond that, the band in which it makes no move toward a told column, px. */
    static final double APPROACH = 6;

    /**
     * The escapes-secondary craft not counted as an enemy (Level 36's harvesters) above the ship to take now, while the
     * secondary is still open: the lowest, its HP left weighed in ({@link #HP_WEIGHT}); null without one.
     */
    static Enemy harvester(Sortie sortie, double shipY) {
        String escapes = sortie.script().objectives().secondary().escapes();
        if (escapes.isEmpty() || sortie.secondaryFailed()) {
            return null;
        }
        Enemy best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.spec().countsAsEnemy()
                    || enemy.spec().neutral()
                    || !enemy.spec().slug().equals(escapes)
                    || y >= PlayField.HEIGHT - 10
                    || y <= shipY + 20) {
                continue;
            }
            // the lower the sooner it gets away; a pair flies side by side, so the one the beam is on (less HP left)
            // is finished first instead of crossing between the two
            double score = y + HP_WEIGHT * enemy.hp() / enemy.spec().hp();
            if (score < bestScore) {
                best = enemy;
                bestScore = score;
            }
        }
        return best;
    }

    /** How much a harvester's full HP weighs against its height in picking one, px. */
    static final double HP_WEIGHT = 200;

    /** Its height under {@code harvester}: below it by {@link #UNDER}, never above the cruise. */
    static double underY(Enemy harvester, double cruise) {
        return Math.clamp(harvester.renderY(1) - UNDER, LOWEST, Math.max(LOWEST, cruise));
    }

    /**
     * The hurt regenerating unit (a Chimera) above the ship with the least share of its HP left (the one the beam was
     * on), so it is finished before it heals; null when none is hurt.
     */
    static Enemy focus(Sortie sortie, double shipY) {
        Enemy best = null;
        double bestShare = 1;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().regeneration().isEmpty()
                    || enemy.cloaked()
                    || !enemy.layer().touchable()
                    || enemy.hp() <= 0) {
                continue;
            }
            double y = enemy.renderY(1);
            double share = enemy.hp() / enemy.spec().hp();
            if (y > shipY + 40 && y < PlayField.HEIGHT - 10 && share < bestShare) {
                best = enemy;
                bestShare = share;
            }
        }
        return best;
    }

    /**
     * The commands with the ship out of every told or striking lightning column (its half width, the hull and {@link
     * #STRIKE_MARGIN}): inside one it moves out to the side with room, its vertical move and fire kept; just outside it
     * makes no move toward it. Unchanged without strikes.
     */
    static int strikes(Sortie sortie, int commands) {
        if (sortie.lightning().isEmpty()) {
            return commands;
        }
        Lightning lightning = sortie.lightning().get();
        double shipX = sortie.ship().x();
        double clear = lightning.width() / 2 + MovePlanner.HULL_X + STRIKE_MARGIN;
        double edge = sortie.ship().spec().edgeLimit();
        int horizontal = Command.LEFT.bit() | Command.RIGHT.bit();
        int out = commands;
        for (int i = 0; i < lightning.count(); i++) {
            if (!lightning.threatens(i)) {
                continue;
            }
            double column = lightning.x(i);
            double dx = shipX - column;
            if (Math.abs(dx) < clear) {
                boolean right = dx >= 0 ? column + clear < PlayField.WIDTH - edge : column - clear < edge;
                return (commands & ~horizontal) | (right ? Command.RIGHT.bit() : Command.LEFT.bit());
            }
            if (Math.abs(dx) < clear + APPROACH + (rookBetween(sortie, column) ? ROOK_ROOM : 0)) {
                out &= ~(dx > 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
            }
        }
        return out;
    }

    /** Whether Rook flies between the ship and the column at {@code column} (on its side of it, nearer it). */
    private static boolean rookBetween(Sortie sortie, double column) {
        if (sortie.wingman().isEmpty()) {
            return false;
        }
        double rook = sortie.wingman().get().x() - column;
        double ship = sortie.ship().x() - column;
        return rook * ship > 0 && Math.abs(rook) < Math.abs(ship);
    }

    /** Whether the level is a dive with told strikes (Level 36): its moves are planned ({@link #move}). */
    static boolean diving(Sortie sortie) {
        return sortie.lightning().isPresent();
    }

    /** The band along each side edge it keeps out of while it plans, px (an aimed burst traps it there). */
    static final double EDGE_BAND = 40;

    /**
     * A planned move toward ({@code targetX}, {@code targetY}) among the bullets and bodies ({@link
     * MovePlanner#clearance}) and out of the told columns: the move toward the target when nothing comes too close to
     * the hull while it flies it, otherwise the safe move that keeps nearest the target and off the side edges; with
     * no safe move, the one that keeps farthest from them.
     */
    static int move(Sortie sortie, double targetX, double targetY) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int wanted = MovePlanner.steer(shipX, shipY, targetX, targetY);
        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        int clearest = 0;
        double clearestDistance = Double.NEGATIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double x = MovePlanner.ahead(shipX, MovePlanner.MOVE_X[m], sortie, true);
            double y = MovePlanner.ahead(shipY, MovePlanner.MOVE_Y[m], sortie, false);
            double clearance = inColumn(sortie, x)
                    ? 0
                    : MovePlanner.clearance(sortie, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], null);
            if (clearance > clearestDistance) {
                clearestDistance = clearance;
                clearest = m;
            }
            if (clearance < MovePlanner.SAFE) {
                continue;
            }
            if (MovePlanner.MOVES[m] == wanted) {
                return wanted;
            }
            double score = -Math.abs(x - targetX)
                    - 0.5 * Math.abs(y - targetY)
                    - 2 * Math.max(0, Math.max(EDGE_BAND - x, x - (PlayField.WIDTH - EDGE_BAND)));
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return MovePlanner.MOVES[best >= 0 ? best : clearest];
    }

    /** Whether the hull at {@code x} touches a told or striking column (with {@link #STRIKE_MARGIN}). */
    private static boolean inColumn(Sortie sortie, double x) {
        if (sortie.lightning().isEmpty()) {
            return false;
        }
        Lightning lightning = sortie.lightning().get();
        double clear = lightning.width() / 2 + MovePlanner.HULL_X + STRIKE_MARGIN;
        for (int i = 0; i < lightning.count(); i++) {
            if (lightning.threatens(i) && Math.abs(x - lightning.x(i)) < clear) {
                return true;
            }
        }
        return false;
    }

    /**
     * The room it leaves Rook between itself and a told column when he flies between them, px: his hull's way out of
     * the column and his distance from the ship's centre (40 px), so it never pushes him into the column.
     */
    static final double ROOK_ROOM = 40;

    /**
     * {@code targetX} moved out of every told or striking column to its nearer side with room; {@link #ROOK_ROOM}
     * farther when Rook flies between the ship and the column.
     */
    static double clearX(Sortie sortie, double targetX) {
        if (sortie.lightning().isEmpty()) {
            return targetX;
        }
        Lightning lightning = sortie.lightning().get();
        double x = targetX;
        for (int i = 0; i < lightning.count(); i++) {
            double column = lightning.x(i);
            double clear = lightning.width() / 2
                    + MovePlanner.HULL_X
                    + STRIKE_MARGIN
                    + APPROACH
                    + (rookBetween(sortie, column) ? ROOK_ROOM : 0);
            if (lightning.threatens(i) && Math.abs(x - column) < clear) {
                boolean left = x < column ? column - clear > 20 : column + clear > PlayField.WIDTH - 20;
                x = left ? column - clear : column + clear;
            }
        }
        return x;
    }
}
