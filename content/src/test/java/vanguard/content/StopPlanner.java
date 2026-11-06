package vanguard.content;

import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part B, step B3 (Act 7: {@link WardenPlay}): the autopilot's planned move toward a spot. Of the
 * nine moves (eight ways and standing still) the cheapest over a short look-ahead ({@link LeechPlay#SAMPLES} samples of
 * {@link LeechPlay#SAMPLE} s, as {@link LeechPlay#cost}): the bullets and the air bodies (flown on as they move now, the
 * current's drift included), the large debris, a running laser line ({@link LeechPlay#line}: off its band before it is
 * live), a teleporting unit's arrival ring (its landing point, before the unit is there), the side and bottom edges, and
 * the distance to the spot at the look-ahead's end. Unlike {@link LeechPlay#cost} a move toward the spot stops on it (as
 * the pilot's steer does), so a spot beside a body or a line's band is reached and held rather than overshot in the
 * look-ahead. Allocation-free.
 */
final class StopPlanner {
    private StopPlanner() {}

    /** A landing ring is weighed as a body this wide around its point, px (a Rift Skater's hitbox and its burst). */
    static final double RING = 40;

    /** The cheapest of the nine moves toward (targetX, targetY). */
    static int move(Sortie sortie, double targetX, double targetY) {
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost = cost(sortie, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], targetX, targetY);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /** Where a move from {@code from} at {@code velocity} px/s is after {@code t} s, stopping on {@code target}. */
    private static double along(double from, double velocity, double t, double target) {
        double to = from + velocity * t;
        if (velocity > 0 && target >= from) {
            return Math.min(to, target);
        }
        if (velocity < 0 && target <= from) {
            return Math.max(to, target);
        }
        return to;
    }

    /** What flying (dx, dy) at full speed (stopping on the spot) for the look-ahead costs. */
    static double cost(Sortie sortie, double dx, double dy, double targetX, double targetY) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        for (int k = 1; k <= LeechPlay.SAMPLES; k++) {
            double t = k * LeechPlay.SAMPLE;
            x = Math.clamp(along(x0, dx * speed, t, targetX), margin, PlayField.WIDTH - margin);
            y = Math.clamp(along(y0, dy * speed, t, targetY), margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / LeechPlay.SAMPLES;
            double nearest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < sortie.bulletCount(); i++) {
                EnemyBullet bullet = sortie.bullet(i);
                double bx = bullet.renderX(1) + (bullet.renderX(1) - bullet.renderX(0)) * steps;
                double by = bullet.renderY(1) + (bullet.renderY(1) - bullet.renderY(0)) * steps;
                nearest = Math.min(nearest, MovePlanner.gap(bx - x, by - y, LeechPlay.HULL_X, LeechPlay.HULL_Y));
            }
            for (int i = 0; i < sortie.debrisCount(); i++) {
                Debris chunk = sortie.debris(i);
                if (!chunk.large() && !chunk.thrown()) {
                    continue;
                }
                double cx = chunk.renderX(1) + (chunk.renderX(1) - chunk.renderX(0)) * steps;
                double cy = chunk.renderY(1) + (chunk.renderY(1) - chunk.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                cx - x,
                                cy - y,
                                LeechPlay.HULL_X + chunk.spec().size().width() / 2,
                                LeechPlay.HULL_Y + chunk.spec().size().height() / 2));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().laserLine().isPresent()) {
                    cost += weight * LeechPlay.line(enemy, x, y, t);
                }
                if (enemy.layer() == Layer.GROUND || !enemy.layer().touchable() || enemy.hp() <= 0) {
                    continue;
                }
                if (enemy.spec().teleport().isPresent() && enemy.teleport().phase() == Teleporting.Phase.GONE) {
                    // a gone unit lands on its ring's point: weigh the point as its body
                    nearest = Math.min(
                            nearest,
                            MovePlanner.gap(
                                    enemy.teleport().ringX() - x,
                                    enemy.teleport().ringY() - y,
                                    LeechPlay.HULL_X + RING / 2,
                                    LeechPlay.HULL_Y + RING / 2));
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ex - x,
                                ey - y,
                                LeechPlay.HULL_X + enemy.hitbox().width() / 2,
                                LeechPlay.HULL_Y + enemy.hitbox().height() / 2));
            }
            if (!hit && nearest < 1) {
                cost += LeechPlay.HIT * weight;
                hit = true;
            }
            if (nearest < LeechPlay.CLEAR) {
                cost += weight * 4 * (LeechPlay.CLEAR - nearest);
            }
        }
        cost += LeechPlay.ALONG_X * Math.abs(x - targetX) + LeechPlay.ALONG_Y * Math.abs(y - targetY);
        cost += 2 * Math.max(0, Math.max(LeechPlay.EDGE - x, x - (PlayField.WIDTH - LeechPlay.EDGE)));
        cost += 2 * Math.max(0, LeechPlay.BOTTOM - y);
        return cost;
    }
}
