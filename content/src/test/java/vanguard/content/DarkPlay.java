package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.GroundObject;
import vanguard.sim.LurePhase;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.WaveSpec;

/**
 * M8 part F (Level 26 Blackwater): the autopilot's play in a level with lunging units (see {@link Autopilot}). It plays
 * from the simulation's state (the darkness is a look only, design/world/europa *Darkness*). The level is a flood of
 * small threats that each punish standing close: the Glow Angler lunges at a ship inside its trigger and rings where its
 * dive ends (design/enemies/naval/glow-angler), a Driftjelly rings at a ship within its reach, a Spiral Nautilus pair
 * orbits it with its spirals open. So every move is planned ({@link #move}): of the eight ways and standing still, the
 * one whose look-ahead keeps the hull off the bullets (flown straight on) and the bodies, out of a drifting Angler's
 * trigger and a jelly's reach and away from where a diving Angler rings, and nearest its line under the lowest unit
 * above it (or a pickup, or the farm barge's lamp). A trigger it cannot keep out of is entered at its edge: the dive aims
 * at where the ship was, so the plan's next moves take it away from the dive's end before the ring. The rock (the
 * scarp) stays {@link HazardPlay#canyon}'s. No level before it has a lunging unit, so nothing here changes another
 * level's flight. Allocation-free.
 */
final class DarkPlay {
    private DarkPlay() {}

    /** How far ahead each move is followed, in samples of {@link #SAMPLE} s. */
    static final int SAMPLES = 14;

    static final double SAMPLE = 0.05;
    /** The hull's half width and height with a small bullet's radius, px (design/player/ship, hull). */
    static final double HULL_X = 21;

    static final double HULL_Y = 23;
    /** A hit: the cost of the hull meeting a bullet or a body in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** The margin kept outside a drifting Angler's trigger and a jelly's reach, px. */
    static final double ZONE_MARGIN = 10;
    /** The cost of standing inside a drifting Angler's trigger (per sample, at its centre). */
    static final double TRIGGER = 25;
    /** The cost of standing inside a jelly's reach (per sample, at its centre). */
    static final double REACH = 40;
    /** How far it keeps from an Angler diving or backing away with its ring due, px. */
    static final double RING_KEEP = 150;

    static final double RING = 50;
    /** Its cruise height in the dark, px. */
    static final double CRUISE_Y = 90;
    /** The cost of each px off its line, across and up or down, at the look-ahead's end. */
    static final double ALONG_X = 0.5;

    static final double ALONG_Y = 0.1;
    /** It keeps off the side edges, where a ring traps it, px. */
    static final double EDGE = 50;

    /** Whether the level has a lunging unit (Level 26's Glow Angler). */
    static boolean dark(Sortie sortie) {
        var waves = sortie.script().waves();
        for (int i = 0; i < waves.size(); i++) {
            WaveSpec wave = waves.get(i);
            if (wave.enemy().lunge().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = shipX;
        double targetY = CRUISE_Y;
        // The lowest unit above it it can line up under from outside its zone (a drifting Angler's trigger, a jelly's
        // reach); without one the lowest unit above it.
        Enemy lowest = null;
        Enemy safe = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (!enemy.cloaked()
                    && enemy.layer().touchable()
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 40
                    && enemy.renderX(1) > 0
                    && enemy.renderX(1) < PlayField.WIDTH) {
                if (lowest == null || y < lowest.renderY(1)) {
                    lowest = enemy;
                }
                if (y - shipY > zone(enemy) && (safe == null || y < safe.renderY(1))) {
                    safe = enemy;
                }
            }
        }
        if (safe != null) {
            lowest = safe;
        }
        Pickup pickup = sortie.pickupCount() > 0 ? sortie.pickup(0) : null;
        if (pickup != null && (lowest == null || pickup.renderY(1) < 300)) {
            targetX = pickup.renderX();
            targetY = Math.max(40, pickup.renderY(1) - 10);
        } else if (lowest != null) {
            targetX = lowest.renderX(1);
        } else {
            GroundObject cache = sortie.terrain() != null ? TargetPlay.secretTarget(sortie, shipY) : null;
            GroundObject ground = cache != null ? cache : TargetPlay.lowestGround(sortie, shipY);
            if (ground != null) {
                targetX = ground.renderX();
            }
        }
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

    /** The zone it keeps out of round {@code enemy}: a drifting Angler's trigger, a jelly's reach, with the margin; 0. */
    static double zone(Enemy enemy) {
        if (enemy.lurePhase() == LurePhase.DRIFT) {
            return enemy.spec().lunge().orElseThrow().trigger() + ZONE_MARGIN;
        }
        if (enemy.spec().ring().isPresent()) {
            return enemy.spec().ring().get().within() + ZONE_MARGIN;
        }
        return 0;
    }

    /** What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the zones, the line. */
    static double cost(Sortie sortie, double dx, double dy, double targetX, double targetY) {
        double speed = sortie.ship().spec().speed() * (sortie.ship().slow().slowed() ? SirenPlay.SLOW : 1);
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        Terrain terrain = sortie.terrain();
        double scroll = sortie.groundScroll();
        double groundSpeed = sortie.groundSpeed();
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / SAMPLES;
            double nearest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < sortie.bulletCount(); i++) {
                EnemyBullet bullet = sortie.bullet(i);
                double bx = bullet.renderX(1) + (bullet.renderX(1) - bullet.renderX(0)) * steps;
                double by = bullet.renderY(1) + (bullet.renderY(1) - bullet.renderY(0)) * steps;
                nearest = Math.min(nearest, MovePlanner.gap(bx - x, by - y, HULL_X, HULL_Y));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.layer().touchable()) {
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                double ux = ex - x;
                double uy = ey - y;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ux,
                                uy,
                                HULL_X + enemy.hitbox().width() / 2,
                                HULL_Y + enemy.hitbox().height() / 2));
                double d = Math.sqrt(ux * ux + uy * uy);
                LurePhase phase = enemy.lurePhase();
                if (phase == LurePhase.DRIFT) {
                    double zone = enemy.spec().lunge().orElseThrow().trigger() + ZONE_MARGIN;
                    if (d < zone && PlayField.overlaps(ex, ey, enemy.hitbox())) {
                        cost += weight * TRIGGER * (zone - d) / zone;
                    }
                } else if (phase == LurePhase.LUNGE || phase == LurePhase.RETREAT) {
                    if (d < RING_KEEP) {
                        cost += weight * RING * (RING_KEEP - d) / RING_KEEP;
                    }
                } else if (enemy.spec().ring().isPresent()) {
                    double zone = enemy.spec().ring().get().within() + ZONE_MARGIN;
                    if (d < zone) {
                        cost += weight * REACH * (zone - d) / zone;
                    }
                }
            }
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
            if (terrain != null) {
                double row = Terrain.row(scroll + groundSpeed * t, y);
                double left = terrain.leftAt(row);
                double right = terrain.rightAt(row);
                double keep = HULL_X + HazardPlay.ROCK_MARGIN;
                if (!Double.isNaN(left) && x < left + keep) {
                    cost += weight * 20 * (left + keep - x);
                }
                if (!Double.isNaN(right) && x > right - keep) {
                    cost += weight * 20 * (x - right + keep);
                }
            }
        }
        cost += ALONG_X * Math.abs(x - targetX) + ALONG_Y * Math.abs(y - targetY);
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        return cost;
    }
}
