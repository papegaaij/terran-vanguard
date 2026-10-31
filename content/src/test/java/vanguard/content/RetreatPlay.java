package vanguard.content;

import java.util.Optional;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.GroundObject;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.Pursuit;
import vanguard.sim.Sortie;

/**
 * M7 part G (step G4): the autopilot's play while the scroll runs reversed (design/campaign Level 20 Retreat from
 * Hellas, see {@link Autopilot}): the waves ahead come up from the bottom edge into the rear guns, the pursuit swarm
 * holds its band behind the ship at the top and lunges down the ship's lane. It holds the middle band ({@link #HOLD_Y},
 * between {@link #BAND_LOW} and {@link #BAND_HIGH}), lines up on the unit it kills soonest, below it for the rear guns
 * or above it for the beam (else under the swarm's leader), steps out of a lunge's path during its tell and
 * dive and out of the ball's way while it climbs back, and picks up what drops when nothing is below it. It reads the
 * sortie's state only and allocates nothing.
 */
final class RetreatPlay {
    private RetreatPlay() {}

    /** The middle band it holds in reversed travel, px from the bottom edge. */
    static final double BAND_LOW = 220;

    static final double BAND_HIGH = 320;

    /** Its height in the band when nothing calls it elsewhere. */
    static final double HOLD_Y = 260;

    /** How far beyond the ball's half width and its hull it keeps from a lunge's path, px. */
    private static final double LANE_MARGIN = 14;

    /** How close a climbing ball below it may come before it steps aside, px. */
    private static final double CLIMB_REACH = 220;

    /** A unit counts as below it (in the rear guns' reach) this far under its centre, px. */
    private static final double BELOW = 24;

    /**
     * What a unit's hit point below it counts as against its distance across when it picks a target, px: about the
     * ship's speed over the rear guns' damage a second (the Tail Gun L3's 20).
     */
    private static final double BELOW_HP = 12;

    /** The same above it, in the beam's reach (the Ion Beam L3's 80 a second). */
    private static final double ABOVE_HP = 3;

    /** How much its planner weighs the band's height against the target's x. */
    private static final double Y_WEIGHT = 1.5;

    /** Its planner keeps out of the side strips this wide, px (an aimed burst traps it at the edge). */
    private static final double EDGE_BAND = 60;

    /** It keeps this far off the side edges when it steps out of a lunge, px. */
    private static final double EDGE = 24;

    /**
     * The side to step to out of the pursuit's lunge (negative = left, positive = right), 0 when the ball's path does
     * not cross its place: during the tell and the dive the line from the leader point to the lane's end at its height,
     * while the ball climbs back the ball's x when it is below it and close.
     */
    static double lungeThreat(Sortie sortie, double shipX, double shipY) {
        Optional<Pursuit> chase = sortie.pursuit();
        if (chase.isEmpty()) {
            return 0;
        }
        Pursuit pursuit = chase.get();
        double clear = pursuit.spec().ballSize() / 2 + MovePlanner.HULL_X + LANE_MARGIN;
        double crossX;
        if (pursuit.lunging()) {
            double fromX = pursuit.leaderX();
            double fromY = pursuit.leaderY();
            double toX = pursuit.laneX();
            double toY = pursuit.laneEndY();
            if (shipY >= fromY) {
                crossX = fromX;
            } else if (shipY <= toY || fromY - toY < 1) {
                crossX = toX;
            } else {
                crossX = fromX + (toX - fromX) * (fromY - shipY) / (fromY - toY);
            }
        } else if (pursuit.phase() == Pursuit.Phase.CLIMB
                && pursuit.leaderY() < shipY + clear
                && pursuit.leaderY() > shipY - CLIMB_REACH) {
            crossX = pursuit.leaderX();
            clear += 20; // it follows the ship's x while it climbs
        } else {
            return 0;
        }
        double dx = crossX - shipX;
        if (Math.abs(dx) >= clear) {
            return 0;
        }
        double left = crossX - clear;
        double right = crossX + clear;
        boolean leftOk = left >= EDGE;
        boolean rightOk = right <= PlayField.WIDTH - EDGE;
        if (leftOk && rightOk) {
            return shipX - left <= right - shipX ? -1 : 1;
        }
        return leftOk ? -1 : 1;
    }

    /**
     * Its move in reversed travel out of a lunge's way: toward the unit it lines up on ({@link #target}, else a ground
     * object below it, else the swarm's leader) or a pickup, in the middle band, by the planner's look-ahead over the bullets and units from
     * both edges ({@link #plan}).
     */
    static int move(Sortie sortie, double shipX, double shipY) {
        double targetX = shipX;
        double targetY = HOLD_Y;
        Enemy target = target(sortie, shipX, shipY);
        boolean below = target != null && target.renderY(1) < shipY - BELOW;
        if (target != null) {
            targetX = target.renderX(1);
        } else {
            Optional<Pursuit> chase = sortie.pursuit();
            GroundObject ground = groundBelow(sortie, shipY);
            if (ground != null) {
                targetX = ground.renderX();
            } else if (chase.isPresent()) {
                targetX = chase.get().leaderX();
            }
        }
        if (!below && sortie.pickupCount() > 0) {
            Pickup pickup = sortie.pickup(0);
            if (pickup.renderY(1) > 30 && pickup.renderY(1) < PlayField.HEIGHT - 60) {
                targetX = pickup.renderX();
                targetY = Math.clamp(pickup.renderY(1), BAND_LOW - 60, BAND_HIGH + 60);
            }
        }
        return plan(sortie, Math.clamp(targetX, EDGE, PlayField.WIDTH - EDGE), targetY) | Command.FIRE.bit();
    }

    /**
     * The boss planner's look-ahead ({@link MovePlanner#bossMove}) with the band's height weighed more: the move toward
     * the target if it is safe, else the safe move nearest the target and the band, else the clearest. In reversed
     * travel the bullets come up from below as well as down from above, and the boss planner, which weighs the height
     * at half the x, drifts up out of the band into the swarm's.
     */
    private static int plan(Sortie sortie, double targetX, double targetY) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int wanted = MovePlanner.steer(shipX, shipY, targetX, targetY);
        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        int clearest = 0;
        double clearestDistance = Double.NEGATIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double clearance = MovePlanner.clearance(sortie, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], null);
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
            double x = MovePlanner.ahead(shipX, MovePlanner.MOVE_X[m], sortie, true);
            double y = MovePlanner.ahead(shipY, MovePlanner.MOVE_Y[m], sortie, false);
            double score = -Math.abs(x - targetX)
                    - Y_WEIGHT * Math.abs(y - targetY)
                    - 2 * Math.max(0, Math.max(EDGE_BAND - x, x - (PlayField.WIDTH - EDGE_BAND)));
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return MovePlanner.MOVES[best >= 0 ? best : clearest];
    }

    /**
     * The unit on the screen it lines up on, or null: the one it reaches and kills soonest, its distance across counted
     * with the hits it still takes, a hit point as {@link #BELOW_HP} px below it (the rear guns) and as {@link
     * #ABOVE_HP} px above it (the beam burns a unit in its column in a few steps); so it finishes what it started rather
     * than swapping between the units of a line, and it swings the beam onto a unit that rose past it. Not the
     * pursuit's motes: it lines up under the swarm's leader when nothing else is on the screen.
     */
    static Enemy target(Sortie sortie, double shipX, double shipY) {
        Enemy best = null;
        double bestCost = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (shootable(enemy)
                    && (enemy.flock() == null || !enemy.flock().pursuing())
                    && y > 8
                    && y < PlayField.HEIGHT - 8) {
                double cost =
                        Math.abs(enemy.renderX(1) - shipX) + (y < shipY - BELOW ? BELOW_HP : ABOVE_HP) * enemy.hp();
                if (cost < bestCost) {
                    bestCost = cost;
                    best = enemy;
                }
            }
        }
        return best;
    }

    /** A ground object (the emergency cache) below it on the screen, or null. */
    private static GroundObject groundBelow(Sortie sortie, double shipY) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (!object.spent() && object.renderY(1) > 8 && object.renderY(1) < shipY - BELOW) {
                return object;
            }
        }
        return null;
    }

    private static boolean shootable(Enemy enemy) {
        return !enemy.cloaked()
                && !enemy.invulnerable()
                && enemy.layer().touchable()
                && !TargetPlay.closedVortex(enemy);
    }
}
