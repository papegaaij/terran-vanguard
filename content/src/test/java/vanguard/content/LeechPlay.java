package vanguard.content;

import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObject;
import vanguard.sim.Layer;
import vanguard.sim.LineBeam;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part F (Level 33 Leech Field): the autopilot's play in a level with a generator-draining latch (see {@link
 * Autopilot}; design/enemies/space/void-leech, design/enemies/ground/rail-bunker). A latched Void Leech is ridden while
 * the fit's rear or side guns can kill it ({@link #ride}: one leech, the shield at least half full), else shaken off by
 * reversals (left and right in turn, {@link #SHAKE_SECONDS} each, the Lamprey's play); the EMP Burst throws them all off
 * when a third latches (the fire slows); the drain itself is ridden out (nothing else changes with it). Every other move is
 * planned ({@link #move}, as {@link DarkPlay}'s): of the eight ways and standing still, the one whose look-ahead keeps
 * the hull off the bullets (flown straight on), the air bodies but the leeches (it lets them latch and answers them) and
 * the large rocks, off a rail line that will be live
 * (a telegraphed line's direction is locked and it rides with its bunker: it steps off it in the telegraph), away from the side edges and corners where an aimed
 * burst traps it, and nearest its line under the lowest unit above it (a pickup, <em>Claim 114</em>). No level before it
 * has a power-draining latch, so nothing here changes another level's flight. Allocation-free.
 */
final class LeechPlay {
    private LeechPlay() {}

    /** How long each reversal of a shake-off lasts, s (four within the latch's 1.2 s). */
    static final double SHAKE_SECONDS = 0.1;
    /** It rides a latched leech for its rear guns only with the shield at least this full. */
    static final double RIDE_SHIELD = 0.5;
    /** How far ahead each move is followed, in samples of {@link #SAMPLE} s. */
    static final int SAMPLES = 14;

    static final double SAMPLE = 0.05;
    /** The hull's half width and height with a small bullet's radius, px (design/player/ship, hull). */
    static final double HULL_X = 21;

    static final double HULL_Y = 23;
    /** A hit: the cost of the hull meeting a bullet, a body or a live line in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** The margin kept off a rail line's band, px, and the cost a sample of standing on a telegraphed line. */
    static final double LINE_MARGIN = 6;

    static final double ON_LINE = 30;
    /** Its cruise height, px. */
    static final double CRUISE_Y = 100;
    /** The cost of each px off its line, across and up or down, at the look-ahead's end. */
    static final double ALONG_X = 0.5;

    static final double ALONG_Y = 0.1;
    /** It keeps off the side edges and the bottom edge, where an aimed burst traps it, px. */
    static final double EDGE = 60;

    static final double BOTTOM = 50;

    /** Whether the level sends a unit whose latch drains the generator (Level 33's Void Leech). */
    static boolean field(Sortie sortie) {
        var kinds = sortie.enemyKinds();
        for (int i = 0; i < kinds.size(); i++) {
            EnemySpec kind = kinds.get(i);
            if (kind.latch().isPresent() && kind.latch().get().drainsPower()) {
                return true;
            }
        }
        return false;
    }

    /** One step's move (the fire bit is the caller's): the shake-off or the EMP when due, else the planned move. */
    static int move(Sortie sortie) {
        int answer = answer(sortie);
        return answer >= 0 ? answer : plan(sortie);
    }

    /**
     * The answer to the leeches latched on the ship: the EMP Burst when a third latches (the fire slows), the shake-off
     * unless it rides them ({@link #ride}); -1 when none is due (nothing latched, or riding one).
     */
    static int answer(Sortie sortie) {
        int latched = sortie.latched();
        if (latched >= 3 && emp(sortie)) {
            return Command.SPECIAL.bit();
        }
        if (latched > 0 && !ride(sortie, latched)) {
            boolean left = ((long) Math.floor(sortie.realSeconds() / SHAKE_SECONDS)) % 2 == 0;
            return left ? Command.LEFT.bit() : Command.RIGHT.bit();
        }
        return -1;
    }

    /** Whether {@code enemy} latches on to drain the generator (a Void Leech). */
    static boolean drainer(Enemy enemy) {
        return enemy.spec().latch().isPresent() && enemy.spec().latch().get().drainsPower();
    }

    /** Whether the fitted special is an EMP Burst ready to call. */
    static boolean emp(Sortie sortie) {
        return sortie.special().ready() && sortie.special().name().equals("EMP Burst");
    }

    /**
     * Whether it rides the latched leeches instead of shaking them off: one leech, a rear or side gun that hits it, the
     * shield at least {@link #RIDE_SHIELD} full.
     */
    static boolean ride(Sortie sortie, int latched) {
        if (latched > 1) {
            return false;
        }
        double shield = sortie.ship().defences().shield();
        double most = sortie.ship().defences().maxShield();
        return most > 0 && shield >= RIDE_SHIELD * most && flanking(sortie);
    }

    /** Whether a fitted gun hits a latched unit: the rear mount's, or one with the {@code side} or {@code rear} trait. */
    static boolean flanking(Sortie sortie) {
        var mounts = sortie.armament().mounts();
        for (int m = 0; m < mounts.size(); m++) { // indexed: no iterator on C1 (the allocation tests)
            Armament.Mount mount = mounts.get(m);
            if (mount.fires()
                    && (mount.slot() == Armament.Slot.REAR || mount.weapon().flanking())) {
                return true;
            }
        }
        return false;
    }

    /** The planned move: the cheapest of the nine toward its line (the lowest unit above it, a pickup, the claim). */
    static int plan(Sortie sortie) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = shipX;
        double targetY = CRUISE_Y;
        boolean antiGround = TargetPlay.antiGround(sortie);
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            // a hardened bunker takes nothing from the plan's guns: not a target without an anti-ground gun
            if (!enemy.cloaked()
                    && enemy.layer().touchable()
                    && (antiGround || !enemy.spec().hardened())
                    && !enemy.latched()
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 40
                    && enemy.renderX(1) > 0
                    && enemy.renderX(1) < PlayField.WIDTH
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        Pickup pickup = sortie.pickupCount() > 0 ? sortie.pickup(0) : null;
        GroundObject claim = TargetPlay.secretTarget(sortie, shipY);
        if (pickup != null && (lowest == null || pickup.renderY(1) < 300)) {
            targetX = pickup.renderX();
            targetY = Math.max(40, pickup.renderY(1) - 10);
        } else if (claim != null) {
            targetX = claim.renderX();
        } else if (lowest != null) {
            targetX = lowest.renderX(1);
        } else {
            GroundObject ground = TargetPlay.lowestGround(sortie, shipY);
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

    /** What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the lines, the edges, the line. */
    static double cost(Sortie sortie, double dx, double dy, double targetX, double targetY) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
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
                                HULL_X + chunk.spec().size().width() / 2,
                                HULL_Y + chunk.spec().size().height() / 2));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().laserLine().isPresent()) {
                    cost += weight * line(enemy, x, y, t);
                }
                // a leech is no body to dodge: latched, it is ridden for the rear guns or shaken off (and drains)
                if (enemy.layer() == Layer.GROUND || !enemy.layer().touchable() || drainer(enemy)) {
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ex - x,
                                ey - y,
                                HULL_X + enemy.hitbox().width() / 2,
                                HULL_Y + enemy.hitbox().height() / 2));
            }
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
        }
        cost += ALONG_X * Math.abs(x - targetX) + ALONG_Y * Math.abs(y - targetY);
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        cost += 2 * Math.max(0, BOTTOM - y);
        return cost;
    }

    /**
     * What standing at (x, y) {@code t} s ahead costs against {@code unit}'s rail line (riding with the unit, its
     * direction locked): a hit when its line is
     * live then and the hull touches its band (with {@link #LINE_MARGIN}), a little while it only telegraphs over it.
     */
    static double line(Enemy unit, double x, double y, double t) {
        LineBeam beam = unit.lineBeam();
        if (!beam.running()) {
            return 0;
        }
        EnemySpec.LaserLine spec = unit.spec().laserLine().get();
        // Round 66's C.1 b: the line rides with its unit (the ground scrolls it on), its direction locked.
        double steps = t / SimStep.SECONDS;
        double ux = x - (beam.fromX() + (unit.renderX(1) - unit.renderX(0)) * steps);
        double uy = y - (beam.fromY() + (unit.renderY(1) - unit.renderY(0)) * steps);
        double along = ux * beam.dirX() + uy * beam.dirY();
        double across = Math.abs(ux * beam.dirY() - uy * beam.dirX());
        double reach = spec.bandPx() / 2 + HULL_X + LINE_MARGIN;
        if (along < -HULL_Y || across > reach) {
            return 0;
        }
        double live = beam.live() ? 0 : (1 - beam.charge(spec)) * spec.telegraphSeconds();
        if (t >= live - SAMPLE && t <= live + spec.liveSeconds() + SAMPLE) {
            return HIT;
        }
        return ON_LINE;
    }
}
