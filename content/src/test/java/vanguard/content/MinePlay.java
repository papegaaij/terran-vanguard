package vanguard.content;

import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.EnemyGun;
import vanguard.sim.GroundObject;
import vanguard.sim.Layer;
import vanguard.sim.Mine;
import vanguard.sim.Missile;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M9 part C (Level 30 Minefield): the autopilot's play in a level with line mines (see {@link Autopilot}). The lane
 * crowds things that each want their own move: the Minelayers' lines coming down with the scroll (an armed mine whose
 * proximity holds the ship's centre blinks, then bursts with a blast; design/enemies, Mine lines), the Minelayers
 * themselves rising up a side lane from behind (design/enemies/space/minelayer), the Darts' streams, the Rail Serpents'
 * trains and the large rocks. So every move is planned ({@link #move}), as {@link VentPlay} does among the plumes: of the
 * eight ways and standing still, the one whose look-ahead keeps the hull off the bullets, the bodies (the rocks
 * included) and an armed mine, its centre out of a live mine's proximity disc and its hull out of a blinking mine's
 * blast, out of a rising layer's lane, and nearest its line. A mine in the beam's column (the front gun fires all the
 * time) is as good as gone, and so are the mines its burst sets off (its cluster: the chain hops {@code chain} px), so
 * the plan disregards them: where the line ahead blocks its line, it lines up under the nearest mine in the way, burns
 * it and flies through the hole the cluster leaves (one shot opens 208 px, where the gap leaves the ship's centre a few
 * px). Its line: a Gilded Gunship on the screen first (the convoy is the secondary, {@code escapes}); else the lowest
 * unit above it; else a pickup. It never lines up over a unit below it: the rear gun (the plan's Swarm Tail, homing)
 * takes the layers rising behind it, and a rising layer's lane is a wall. No level before Level 30 has a line mine, so
 * nothing here changes another level's flight. Allocation-free (the doomed mines are two bit masks over the pool).
 */
final class MinePlay {
    private MinePlay() {}

    /** How far ahead each move is followed, in samples of {@link #SAMPLE} s. */
    static final int SAMPLES = 14;

    static final double SAMPLE = 0.05;
    /** The cost of the hull meeting a bullet, a body, a rock or an armed mine in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** The cost of the ship's centre entering a live mine's proximity disc (it blinks and blasts). */
    static final double PROXIMITY = 600;
    /** The margin kept round a live mine's proximity disc, px. */
    static final double PROXIMITY_MARGIN = 4;
    /** The cost of each sample with the hull in a blinking mine's blast disc once it bursts. */
    static final double BLAST = 400;
    /** The margin kept round a blinking mine's blast disc, px. */
    static final double BLAST_MARGIN = 8;
    /** A mine this close to the ship's x (and above it) is in the beam's column: burned before it comes near, px. */
    static final double COLUMN = 7;
    /** The line ahead it plans its passage through: the mines this far above it, px. */
    static final double AHEAD = 230;
    /** The cost of each sample with the hull in a rising layer's lane. */
    static final double LANE = 120;
    /** The margin kept round a rising layer's lane, px. */
    static final double LANE_MARGIN = 10;
    /** The cost of each sample with Rook (in his slot) in a rising layer's lane. */
    static final double ROOK_LANE = 40;
    /** Rook's half width, px. */
    static final double ROOK_HALF = 16;
    /** Its cruise height, px. */
    static final double CRUISE_Y = 100;
    /** The cost of each px off its line, across and up or down, at the look-ahead's end. */
    static final double ALONG_X = 0.5;

    static final double ALONG_Y = 0.1;
    /** The cost of each px off a Gunship's line: it pushes harder for the convoy. */
    static final double GUNSHIP_X = 1.2;
    /** It keeps off the side edges, px. */
    static final double EDGE = 40;
    /** The doomed masks' size: the mine pool's (96) fits in two longs. */
    static final int MASK = 128;

    /** Whether the level has line mines: a wave's unit lays them (Level 30's Minelayers). */
    static boolean mined(Sortie sortie) {
        return lineMine(sortie) != null;
    }

    /** The level's line mine (the first layer's), or null. */
    static EnemyGun.LineMine lineMine(Sortie sortie) {
        var waves = sortie.script().waves();
        for (int i = 0; i < waves.size(); i++) {
            var spec = waves.get(i).enemy();
            if (spec.mineLayer().isPresent() && spec.gun().isPresent()) {
                var mine = spec.gun().get().mine();
                if (mine.isPresent() && mine.get().line().isPresent()) {
                    return mine.get().line().get();
                }
            }
        }
        return null;
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie) {
        EnemyGun.LineMine line = lineMine(sortie);
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        long doomedLow = doomed(sortie, line, shipX, shipY, true);
        long doomedHigh = doomed(sortie, line, shipX, shipY, false);
        Enemy gunship = null;
        Enemy above = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (x < 0
                    || x > PlayField.WIDTH
                    || y > PlayField.HEIGHT - 10
                    || y < shipY + 40
                    || enemy.cloaked()
                    || !enemy.layer().touchable()
                    || enemy.layer() == Layer.GROUND) {
                continue;
            }
            if (enemy.spec().facingArmour().isPresent()) {
                if (gunship == null || y < gunship.renderY(1)) {
                    gunship = enemy;
                }
            } else if (above == null || y < above.renderY(1)) {
                above = enemy;
            }
        }
        double targetX = shipX;
        double targetY = CRUISE_Y;
        Pickup pickup = sortie.pickupCount() > 0 ? sortie.pickup(0) : null;
        if (gunship != null) {
            targetX = gunship.renderX(1);
        } else if (pickup != null && (above == null || pickup.renderY(1) < 300)) {
            targetX = pickup.renderX();
            targetY = Math.max(40, pickup.renderY(1) - 10);
        } else if (above != null) {
            targetX = above.renderX(1);
        } else {
            GroundObject secret = TargetPlay.lowestGround(sortie, shipY);
            if (secret != null) {
                targetX = secret.renderX();
            }
        }
        targetX = passage(sortie, line, targetX, shipY, doomedLow, doomedHigh);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost = cost(
                    sortie,
                    line,
                    doomedLow,
                    doomedHigh,
                    MovePlanner.MOVE_X[m],
                    MovePlanner.MOVE_Y[m],
                    targetX,
                    targetY,
                    gunship != null ? GUNSHIP_X : ALONG_X);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /** Whether mine {@code index} is in the doomed masks. */
    static boolean doomed(int index, long low, long high) {
        return index < 64 ? (low & (1L << index)) != 0 : index < MASK && (high & (1L << (index - 64))) != 0;
    }

    /** Whether {@code mine} is a live line mine: a line's, not yet set off. */
    static boolean live(Mine mine) {
        return mine.line() && !mine.setOff();
    }

    /**
     * The mines as good as gone (the {@code low} half of the mask: pool indices 0-63, else 64-127): each armed live
     * line mine in the beam's column above the ship, and every live line mine its burst sets off, hop by hop.
     */
    static long doomed(Sortie sortie, EnemyGun.LineMine line, double shipX, double shipY, boolean low) {
        long maskLow = 0;
        long maskHigh = 0;
        int count = Math.min(MASK, sortie.mineCount());
        for (int i = 0; i < count; i++) {
            Mine mine = sortie.mine(i);
            if (live(mine)
                    && mine.armed()
                    && mine.renderY(1) > shipY + 10
                    && Math.abs(mine.renderX(1) - shipX) < COLUMN) {
                if (i < 64) {
                    maskLow |= 1L << i;
                } else {
                    maskHigh |= 1L << (i - 64);
                }
            }
        }
        double reach = line == null ? 0 : line.chainRadius();
        boolean grew = maskLow != 0 || maskHigh != 0;
        while (grew) {
            grew = false;
            for (int i = 0; i < count; i++) {
                Mine mine = sortie.mine(i);
                if (!live(mine) || doomed(i, maskLow, maskHigh)) {
                    continue;
                }
                for (int j = 0; j < count; j++) {
                    if (!doomed(j, maskLow, maskHigh)) {
                        continue;
                    }
                    Mine from = sortie.mine(j);
                    if (Math.hypot(mine.renderX(1) - from.renderX(1), mine.renderY(1) - from.renderY(1)) <= reach) {
                        if (i < 64) {
                            maskLow |= 1L << i;
                        } else {
                            maskHigh |= 1L << (i - 64);
                        }
                        grew = true;
                        break;
                    }
                }
            }
        }
        return low ? maskLow : maskHigh;
    }

    /**
     * Its line through the mines ahead: {@code targetX} if no live mine of the lines within {@link #AHEAD} px above it
     * is within the proximity (and its margin) of it, else the x of the nearest mine in the way: under it, the beam
     * burns it and its cluster goes.
     */
    static double passage(
            Sortie sortie, EnemyGun.LineMine line, double targetX, double shipY, long doomedLow, long doomedHigh) {
        if (line == null) {
            return targetX;
        }
        double reach = line.proximity() + PROXIMITY_MARGIN + DarkPlay.HULL_X;
        Mine nearest = null;
        double least = Double.POSITIVE_INFINITY;
        int count = Math.min(MASK, sortie.mineCount());
        for (int i = 0; i < count; i++) {
            Mine mine = sortie.mine(i);
            double dy = mine.renderY(1) - shipY;
            if (!live(mine) || doomed(i, doomedLow, doomedHigh) || dy < -10 || dy > AHEAD) {
                continue;
            }
            double dx = Math.abs(mine.renderX(1) - targetX);
            if (dx < reach && dx < least) {
                least = dx;
                nearest = mine;
            }
        }
        return nearest == null ? targetX : nearest.renderX(1);
    }

    /**
     * What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the live mines' proximity and the
     * blinking ones' blasts, a rising layer's lane, the line.
     */
    static double cost(
            Sortie sortie,
            EnemyGun.LineMine line,
            long doomedLow,
            long doomedHigh,
            double dx,
            double dy,
            double targetX,
            double targetY,
            double alongX) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        boolean proximity = false;
        int mines = Math.min(MASK, sortie.mineCount());
        // Rook in his slot beside it: a rising layer's lane is a wall for him too (his slot follows the ship).
        double rookDx = Double.NaN;
        if (sortie.wingman().isPresent()) {
            Wingman rook = sortie.wingman().get();
            if (!rook.ejected() && !rook.waiting() && !rook.joining()) {
                rookDx = rook.x() - x0;
            }
        }
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
                nearest = Math.min(nearest, MovePlanner.gap(bx - x, by - y, DarkPlay.HULL_X, DarkPlay.HULL_Y));
            }
            for (int i = 0; i < sortie.missileCount(); i++) {
                Missile missile = sortie.missile(i);
                double mx = missile.renderX(1) + (missile.renderX(1) - missile.renderX(0)) * steps;
                double my = missile.renderY(1) + (missile.renderY(1) - missile.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                mx - x,
                                my - y,
                                DarkPlay.HULL_X + Missile.BOX.width() / 2,
                                DarkPlay.HULL_Y + Missile.BOX.height() / 2));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.layer().touchable() || enemy.layer() == Layer.GROUND || enemy.cloaked()) {
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                double halfW = enemy.hitbox().width() / 2;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ex - x,
                                ey - y,
                                DarkPlay.HULL_X + halfW,
                                DarkPlay.HULL_Y + enemy.hitbox().height() / 2));
                if (enemy.mineLayer() && !enemy.laying() && ey < y0 + DarkPlay.HULL_Y) {
                    // A layer rising up its lane from behind: the lane above it is a wall until it has passed.
                    if (Math.abs(enemy.renderX(1) - x) < halfW + DarkPlay.HULL_X + LANE_MARGIN) {
                        cost += weight * LANE;
                    }
                    if (!Double.isNaN(rookDx)
                            && Math.abs(enemy.renderX(1) - x - rookDx) < halfW + ROOK_HALF + LANE_MARGIN) {
                        cost += weight * ROOK_LANE;
                    }
                }
            }
            for (int i = 0; i < sortie.debrisCount(); i++) {
                Debris chunk = sortie.debris(i);
                double cx = chunk.renderX(1) + (chunk.renderX(1) - chunk.renderX(0)) * steps;
                double cy = chunk.renderY(1) + (chunk.renderY(1) - chunk.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                cx - x,
                                cy - y,
                                DarkPlay.HULL_X + chunk.spec().size().width() / 2,
                                DarkPlay.HULL_Y + chunk.spec().size().height() / 2));
            }
            for (int i = 0; i < mines; i++) {
                Mine mine = sortie.mine(i);
                if (!live(mine) || doomed(i, doomedLow, doomedHigh)) {
                    continue;
                }
                double mx = mine.renderX(1) + (mine.renderX(1) - mine.renderX(0)) * steps;
                double my = mine.renderY(1) + (mine.renderY(1) - mine.renderY(0)) * steps;
                if (mine.blinking()) {
                    double burst = (1 - mine.blinkShare()) * line.blinkSeconds();
                    double reach = line.blastRadius() + BLAST_MARGIN;
                    if (t >= burst - SAMPLE
                            && Math.hypot(
                                            Math.max(0, Math.abs(mx - x) - DarkPlay.HULL_X),
                                            Math.max(0, Math.abs(my - y) - DarkPlay.HULL_Y))
                                    < reach) {
                        cost += weight * BLAST;
                    }
                    continue;
                }
                if (!mine.armed() && mine.rising() < 0.5) {
                    continue;
                }
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                mx - x,
                                my - y,
                                DarkPlay.HULL_X + EnemyGun.MineSpec.BOX.width() / 2,
                                DarkPlay.HULL_Y + EnemyGun.MineSpec.BOX.height() / 2));
                if (!proximity && Math.hypot(mx - x, my - y) < line.proximity() + PROXIMITY_MARGIN) {
                    cost += weight * PROXIMITY;
                    proximity = true;
                }
            }
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
        }
        cost += alongX * Math.abs(x - targetX) + ALONG_Y * Math.abs(y - targetY);
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        return cost;
    }
}
