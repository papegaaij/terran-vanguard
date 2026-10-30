package vanguard.content;

import vanguard.sim.BossLines;
import vanguard.sim.BossSpec;
import vanguard.sim.CoreVolley;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.EnemySpec;
import vanguard.sim.Layer;
import vanguard.sim.Mine;
import vanguard.sim.Missile;
import vanguard.sim.PlayField;
import vanguard.sim.RingArena;
import vanguard.sim.RingCore;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part H (Level 35's Iron Sovereign, design/enemies/bosses/iron-sovereign): the autopilot's play against the ringed
 * station (see {@link Autopilot}). Every move is planned ({@link #move}, as {@link MawPlay} does): of the eight ways and
 * standing still, the one whose look-ahead keeps the hull off the bullets, the station's missiles and the bay Darts, off a
 * rail line that will be live (a telegraphed line is locked through the ship: it steps off it in the telegraph, {@link
 * BossLines}), out of a laser sweep's wedge still to be swept (its telegraph's whole arc, then the part the beam has not
 * crossed yet), at least {@link #BAY_CLEAR} px from the nearest rim bay once the core opens, and nearest its line. Its line:
 * the armed batteries first (inside the fire arc: they shoot at it), the one low on the screen in a column where other
 * batteries line up above it (the beam burns the lowest and goes on to the next), led by its ring's turn; once the core is
 * open the core, a bay Dart diving at it in reach first. It holds {@link #STAND_Y} below the station. Step BOSS70 (round
 * 70's playbook): it also keeps off the core volley's lines (as a rail line's), its centre out of a thrown line mine's
 * proximity (and its hull out of a blinking one's blast), and its hull off the thrown wrecks (as missiles, by their
 * box). Allocation-free and stateless.
 */
final class RingPlay {
    private RingPlay() {}

    static final int SAMPLES = 16;
    static final double SAMPLE = 0.05;
    /** The cost of the hull meeting a bullet, a missile, a body or a live line in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** The cost of each sample with the hull in a sweep's wedge still to be swept. */
    static final double STRUCK = 400;
    /** The cost of each sample standing on a telegraphed rail line (it steps off it). */
    static final double ON_LINE = 60;
    /** The margin kept round a sweep's beam and a rail line's band, px. */
    static final double MARGIN = 8;
    /** Its height under the station (y up; the outer ring's lowest battery at 180), px. */
    static final double STAND_Y = 100;
    /** It keeps this far from the nearest rim bay once the core opens, px (a bay within 72 px skips its turn). */
    static final double BAY_CLEAR = 76;
    /** The cost of each px off its line, across and up or down, at the look-ahead's end. */
    static final double ALONG_X = 0.6;
    /** ... and of each px across outside its target's column (the beam on it), px. */
    static final double COLUMN_X = 3;
    /** The column's half width over a battery and over the open core (its 70 px box less a margin), px. */
    static final double BATTERY_COLUMN = 10;

    static final double CORE_COLUMN = 26;

    static final double ALONG_Y = 0.15;
    /** The cost of turning against its way now (a tie between two ways is kept, not flipped every step). */
    static final double TURN = 6;
    /** It keeps off the side edges, where an aimed fan traps it, px. */
    static final double EDGE = 50;
    /** A battery inside the fire arc counts this much lower when it picks its target, px. */
    static final double ARMED_BONUS = 80;
    /** Each other living battery in the target's column counts this much, px. */
    static final double COLUMN_BONUS = 25;
    /** The column's half width: batteries this close across line up, px. */
    static final double COLUMN = 18;
    /** How far it leads a battery by its ring's turn, s. */
    static final double LEAD = 0.25;
    /** A bay Dart this close above it and across is in reach: it lines up on it first, px. */
    static final double DART_REACH_Y = 170;

    static final double DART_REACH_X = 90;
    /** Step BOSS70: the cost of each sample with its centre inside a live mine's proximity (and margin). */
    static final double IN_PROXIMITY = 300;
    /** Step BOSS70: the margin kept round a mine's proximity and a blinking mine's blast, px. */
    static final double MINE_MARGIN = 10;

    /** The ringed boss present, engaged and alive (Level 35's Iron Sovereign); null without one. */
    static SetPiece ringedBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present()
                    && !piece.destroyed()
                    && piece.engaged()
                    && piece.ring().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie, SetPiece piece) {
        RingArena ring = piece.ring().orElseThrow();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = piece.renderX(1);
        double targetY = STAND_Y;
        double column = BATTERY_COLUMN;
        RingCore core = ring.core().orElse(null);
        boolean coreOut = core != null && (core.open() || core.opening());
        if (coreOut) {
            Enemy dart = dartInReach(sortie, shipX, shipY);
            targetX = dart != null ? dart.renderX(1) : piece.partX(core.spec().part());
            column = dart != null ? BATTERY_COLUMN : CORE_COLUMN;
        } else if (!ring.spinningUp()) {
            int battery = target(piece, ring, shipX);
            if (battery >= 0) {
                targetX = leadX(piece, ring, battery);
            }
        }
        targetX = Math.clamp(targetX, EDGE, PlayField.WIDTH - EDGE);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost = cost(
                    sortie,
                    piece,
                    coreOut ? core : null,
                    MovePlanner.MOVE_X[m],
                    MovePlanner.MOVE_Y[m],
                    targetX,
                    targetY,
                    column);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /**
     * The battery it lines up under: of the living batteries on the screen, the lowest, an armed one {@link
     * #ARMED_BONUS} lower, each other living battery in its column {@link #COLUMN_BONUS} lower, and the nearer across
     * the better; -1 for none.
     */
    static int target(SetPiece piece, RingArena ring, double shipX) {
        int best = -1;
        double least = Double.POSITIVE_INFINITY;
        for (int p = 0; p < piece.partCount(); p++) {
            if (!living(piece, ring, p)) {
                continue;
            }
            double x = piece.partX(p);
            double y = piece.partY(p);
            if (x < EDGE - 20 || x > PlayField.WIDTH - EDGE + 20 || y > PlayField.HEIGHT - 20) {
                continue;
            }
            int column = 0;
            for (int q = 0; q < piece.partCount(); q++) {
                if (q != p && living(piece, ring, q) && Math.abs(piece.partX(q) - x) < COLUMN && piece.partY(q) > y) {
                    column++;
                }
            }
            double score = y + 0.6 * Math.abs(x - shipX) - (ring.armed(p) ? ARMED_BONUS : 0) - COLUMN_BONUS * column;
            if (score < least) {
                least = score;
                best = p;
            }
        }
        return best;
    }

    /** Whether part {@code p} is a living battery on a turning (not locked) ring. */
    static boolean living(SetPiece piece, RingArena ring, int p) {
        int i = ring.battery(p);
        return i >= 0 && !ring.locked(i) && piece.partHp(p) > 0;
    }

    /** Battery {@code p}'s x {@link #LEAD} s ahead by its ring's turn. */
    static double leadX(SetPiece piece, RingArena ring, int p) {
        double a = ring.mountRadians(p);
        double r = Math.hypot(piece.partOffsetX(p), piece.partOffsetY(p));
        // x = cx - r sin a: dx/dt = -r cos a × da/dt
        return piece.partX(p) - r * Math.cos(a) * ring.velocity(ring.battery(p)) * LEAD;
    }

    /** A bay Dart (or any air unit) diving at it within reach above it, the nearest across; null for none. */
    static Enemy dartInReach(Sortie sortie, double shipX, double shipY) {
        Enemy best = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!enemy.layer().touchable() || enemy.layer() == Layer.GROUND || enemy.cloaked()) {
                continue;
            }
            double dx = Math.abs(enemy.renderX(1) - shipX);
            double dy = enemy.renderY(1) - shipY;
            if (dy > 30
                    && dy < DART_REACH_Y
                    && dx < DART_REACH_X
                    && (best == null || dx < Math.abs(best.renderX(1) - shipX))) {
                best = enemy;
            }
        }
        return best;
    }

    /**
     * What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the rail line, the sweeps' wedges,
     * the bays, the line where the move passes nearest it (more outside its target's {@code column}, px either side).
     */
    static double cost(
            Sortie sortie,
            SetPiece piece,
            RingCore core,
            double dx,
            double dy,
            double targetX,
            double targetY,
            double column) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        double along = Double.POSITIVE_INFINITY;
        BossLines lines = piece.lines().orElse(null);
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / SAMPLES;
            // its line where the move passes nearest it (it stops there): no overshoot counted
            along = Math.min(
                    along,
                    ALONG_X * Math.abs(x - targetX)
                            + COLUMN_X * Math.max(0, Math.abs(x - targetX) - column)
                            + ALONG_Y * Math.abs(y - targetY));
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
                                DarkPlay.HULL_X + missile.box().width() / 2,
                                DarkPlay.HULL_Y + missile.box().height() / 2));
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.layer().touchable() || enemy.layer() == Layer.GROUND || enemy.cloaked()) {
                    continue;
                }
                double ex = enemy.renderX(1) + (enemy.renderX(1) - enemy.renderX(0)) * steps;
                double ey = enemy.renderY(1) + (enemy.renderY(1) - enemy.renderY(0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ex - x,
                                ey - y,
                                DarkPlay.HULL_X + enemy.hitbox().width() / 2,
                                DarkPlay.HULL_Y + enemy.hitbox().height() / 2));
            }
            if (core != null) {
                CoreVolley volley = core.volley().orElse(null);
                double line = volley == null ? 0 : volley(volley, x, y, t);
                if (line >= HIT && !hit) {
                    cost += HIT * weight;
                    hit = true;
                } else if (line < HIT) {
                    cost += weight * line;
                }
                cost += weight * mines(sortie, core, x, y);
            }
            if (lines != null) {
                double line = line(piece, lines, x, y, t);
                if (line >= HIT && !hit) {
                    cost += HIT * weight;
                    hit = true;
                } else if (line < HIT) {
                    cost += weight * line;
                }
            }
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
            if (inWedge(piece, x, y, t)) {
                cost += weight * STRUCK;
            }
            if (core != null) {
                double bay = nearestBay(core, x, y);
                if (bay < BAY_CLEAR) {
                    cost += weight * 4 * (BAY_CLEAR - bay);
                }
            }
        }
        cost += along;
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        // its way now, as a share of its full speed: a way against it costs a little more
        cost += TURN
                * (Math.abs(dx - sortie.ship().vx() / speed)
                        + Math.abs(dy - sortie.ship().vy() / speed));
        return cost;
    }

    /**
     * Step BOSS70: what standing at (x, y) {@code t} s ahead costs against the core's running volley: a hit when its lines
     * are live then and the hull touches a band (with {@link #MARGIN}), {@link #ON_LINE} while one only telegraphs over it.
     */
    static double volley(CoreVolley volley, double x, double y, double t) {
        if (!volley.running()) {
            return 0;
        }
        double cost = 0;
        for (int k = 0; k < volley.lines(); k++) {
            double ux = x - volley.fromX(k);
            double uy = y - volley.fromY(k);
            double along = ux * volley.dirX(k) + uy * volley.dirY(k);
            double across = Math.abs(ux * volley.dirY(k) - uy * volley.dirX(k));
            if (along < -DarkPlay.HULL_Y || across > volley.bandPx() / 2 + DarkPlay.HULL_X + MARGIN) {
                continue;
            }
            double live = volley.secondsToLive();
            if (t >= live - SAMPLE && t <= live + volley.liveSeconds() + SAMPLE) {
                return HIT;
            }
            cost = ON_LINE;
        }
        return cost;
    }

    /**
     * Step BOSS70: what standing at (x, y) costs against the core's thrown mines: {@link #IN_PROXIMITY} with its centre
     * inside a live mine's proximity (and {@link #MINE_MARGIN}) or its hull in a blinking one's blast; a hit for its hull
     * on an armed mine.
     */
    static double mines(Sortie sortie, RingCore core, double x, double y) {
        if (sortie.mineCount() == 0) {
            return 0;
        }
        RingSpec.MineRing ring =
                core.spec().pool().flatMap(RingSpec.Pool::mines).orElse(null);
        if (ring == null) {
            return 0;
        }
        var line = ring.gun().mine().orElseThrow().line().orElseThrow();
        double cost = 0;
        for (int i = 0; i < sortie.mineCount(); i++) {
            Mine mine = sortie.mine(i);
            if (!mine.line() || mine.setOff()) {
                continue;
            }
            double d = Math.hypot(mine.renderX(1) - x, mine.renderY(1) - y);
            if (mine.armed() && d < DarkPlay.HULL_X + 5) {
                cost += HIT / 4;
            }
            if (d < line.proximity() + MINE_MARGIN
                    || (mine.blinking() && d < line.blastRadius() + DarkPlay.HULL_X + MINE_MARGIN)) {
                cost += IN_PROXIMITY;
            }
        }
        return cost;
    }

    /** The distance from (x, y) to the nearest rim bay, px. */
    static double nearestBay(RingCore core, double x, double y) {
        double least = Double.POSITIVE_INFINITY;
        for (int k = 0; k < core.bayCount(); k++) {
            least = Math.min(least, Math.hypot(core.bayX(k) - x, core.bayY(k) - y));
        }
        return least;
    }

    /**
     * What standing at (x, y) {@code t} s ahead costs against the station's running rail line (riding with its battery,
     * its direction locked): a hit when the line is live then and the hull touches its band (with {@link #MARGIN}), {@link #ON_LINE} while it only telegraphs
     * over it; 0 off it or without a line.
     */
    static double line(SetPiece piece, BossLines lines, double x, double y, double t) {
        if (!lines.running()) {
            return 0;
        }
        EnemySpec.LaserLine spec =
                piece.boss().orElseThrow().attacks().get(lines.attack()).line().orElseThrow();
        // Round 68's row 9 (pending): the line rides with its battery on the turning ring, its direction locked.
        double steps = t / SimStep.SECONDS;
        double ux = x - (lines.fromX() + lines.stepX() * steps);
        double uy = y - (lines.fromY() + lines.stepY() * steps);
        double along = ux * lines.dirX() + uy * lines.dirY();
        double across = Math.abs(ux * lines.dirY() - uy * lines.dirX());
        double reach = lines.bandPx() / 2 + DarkPlay.HULL_X + MARGIN;
        if (along < -DarkPlay.HULL_Y || across > reach) {
            return 0;
        }
        double live = lines.live() ? 0 : (1 - lines.charge()) * spec.telegraphSeconds();
        if (t >= live - SAMPLE && t <= live + spec.liveSeconds() + SAMPLE) {
            return HIT;
        }
        return ON_LINE;
    }

    /**
     * Whether a hull at (x, y) {@code t} s ahead lies in a laser sweep's wedge still to be swept: within its beam's
     * length (and the hull) of its origin, between the beam's heading then (its first edge while it telegraphs) and its
     * last edge, with the beam's half width, the hull and {@link #MARGIN} round it.
     */
    static boolean inWedge(SetPiece piece, double x, double y, double t) {
        BossSpec boss = piece.boss().orElseThrow();
        for (int a = 0; a < boss.attacks().size(); a++) {
            BossSpec.Attack attack = boss.attacks().get(a);
            if (attack.sweep().isEmpty()) {
                continue;
            }
            boolean telling = piece.sweepTelegraphing(a);
            if (!telling && !piece.sweepBeaming(a)) {
                continue;
            }
            EnemySpec.Sweep sweep = attack.sweep().get();
            double ox = piece.sweepOriginX(a);
            double oy = piece.sweepOriginY(a);
            double d = Math.hypot(x - ox, y - oy);
            if (d > sweep.length() + DarkPlay.HULL_Y + MARGIN) {
                continue;
            }
            double from = piece.sweepFrom(a);
            double to = piece.sweepTo(a);
            double direction = Math.signum(to - from);
            double rate = (to - from) / Math.max(SimStep.SECONDS, sweep.sweepSeconds());
            double beam = telling
                    ? from
                    : Math.min(Math.abs(to - from), Math.abs(piece.sweepBeam(a, 1) - from + rate * t)) * direction
                            + from;
            double heading = Math.atan2(-(x - ox), -(y - oy));
            double u = wrap(heading - beam) * direction;
            double span = Math.abs(to - beam);
            double slack = Math.asin(Math.min(1, (sweep.width() / 2 + DarkPlay.HULL_X + MARGIN) / Math.max(1, d)));
            if (u > -slack && u < span + slack) {
                return true;
            }
        }
        return false;
    }

    /** {@code angle} wrapped into (-π, π]. */
    static double wrap(double angle) {
        double a = angle % (2 * Math.PI);
        if (a > Math.PI) {
            a -= 2 * Math.PI;
        } else if (a <= -Math.PI) {
            a += 2 * Math.PI;
        }
        return a;
    }
}
