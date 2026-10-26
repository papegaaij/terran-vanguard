package vanguard.content;

import vanguard.sim.CoilArena;
import vanguard.sim.CoilSpec;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H (Level 28's Abyssal Maw, design/enemies/bosses/abyssal-maw): the autopilot's play in the coiled boss's arena
 * (see {@link Autopilot}). Every move is planned ({@link #move}), as {@link VentPlay} does: of the eight ways and
 * standing still, the one whose look-ahead keeps the hull off the bullets and the shed Eel Swarms, out of a told or
 * swinging sweep's bight (its full reach round the bight's segments), out of the lunge's column (its shadow, the lunge
 * and the body standing in it), inside the coil's walls (a move into a wall is cut short there), and nearest its line.
 * Its line: an open implant's (phase 3; the side ones from the coil's inner flank below them, for the pods and the
 * harpoon's edge), else the throat while the jaw is open, else the nearest living gill sac (phase 1), else the head; a
 * fan wall's gap while a wall glows or its orbs are still above the hull (the orbs found as the bullets on one circle
 * round the throat); the column's nearer side from the shadow on. It holds {@link #STAND_Y} below the head, high enough
 * over the bottom sweep's reach, low enough for the walls' orbs to give it time. Allocation-free and stateless.
 */
final class MawPlay {
    private MawPlay() {}

    static final int SAMPLES = 16;
    static final double SAMPLE = 0.05;
    /** The cost of the hull meeting a bullet or a body in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** The cost of each sample with the hull in a told or swinging bight's reach, or in the lunge's column. */
    static final double STRUCK = 400;
    /** The margin kept round a bight's or the column's reach, px. */
    static final double MARGIN = 12;
    /** Its height in the arena (y up), px: the bottom sweep reaches 90 px over the coil's bottom flank (47). */
    static final double STAND_Y = 190;
    /** Its height while a side implant is open: below it (240), px. */
    static final double SIDE_IMPLANT_Y = 170;
    /** The cost of each px off its line, across and up or down, at the look-ahead's end. */
    static final double ALONG_X = 0.6;

    static final double ALONG_Y = 0.15;
    /** ... across while it makes for a wall's gap or out of the column. */
    static final double GAP_X = 3;
    /** Bullets this close to one circle round the throat are a fan wall's orbs, px. */
    static final double ORB_TOLERANCE = 2.5;
    /** At least this many on one circle make a wall. */
    static final int WALL_ORBS = 5;

    /** The coiled boss present and alive (Level 28's Abyssal Maw); null without one. */
    static SetPiece coiledBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && !piece.destroyed() && piece.coil().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move (the fire bit is the caller's). */
    static int move(Sortie sortie, SetPiece piece) {
        CoilArena coil = piece.coil().orElseThrow();
        CoilSpec spec = piece.boss().orElseThrow().coil().orElseThrow();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double targetX = coil.headX();
        double targetY = STAND_Y;
        double alongX = ALONG_X;
        int implant = openImplant(coil);
        if (implant >= 0) {
            double ix = coil.implantX(implant);
            double iy = coil.implantY(implant);
            targetX = Math.clamp(ix, innerLeft(spec) + 2, innerRight(spec) - 2);
            if (iy < STAND_Y + 120) {
                targetY = SIDE_IMPLANT_Y;
            }
        } else if (coil.jawOpen() && coil.up()) {
            targetX = coil.throatX();
        } else if (piece.phase() == 0) {
            int sac = nearestSac(piece, spec, shipX);
            if (sac >= 0) {
                targetX = piece.partX(sac);
            }
        }
        double gap = wallGap(sortie, coil, shipY);
        if (!Double.isNaN(gap)) {
            targetX = gap;
            alongX = GAP_X;
        }
        CoilArena.DiveState dive = coil.diveState();
        if (dive == CoilArena.DiveState.SHADOW
                || dive == CoilArena.DiveState.LUNGE
                || dive == CoilArena.DiveState.BODY) {
            double clear = coil.columnWidth() / 2 + spec.body().clearance() + DarkPlay.HULL_X + MARGIN + 6;
            double left = coil.columnX() - clear;
            double right = coil.columnX() + clear;
            boolean leftRoom = left >= innerLeft(spec);
            boolean rightRoom = right <= innerRight(spec);
            boolean goLeft = leftRoom && (!rightRoom || shipX < coil.columnX());
            targetX = goLeft ? left : right;
            alongX = GAP_X;
        }
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost =
                    cost(sortie, coil, spec, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], targetX, targetY, alongX);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /** The open implant (phase 3), or -1. */
    static int openImplant(CoilArena coil) {
        for (int i = 0; i < coil.implantCount(); i++) {
            if (coil.implantOpen(i)) {
                return i;
            }
        }
        return -1;
    }

    /** The living gill sac nearest {@code x} across, or -1. */
    static int nearestSac(SetPiece piece, CoilSpec spec, double x) {
        int best = -1;
        for (int k = 0; k < spec.sacs().size(); k++) {
            int p = spec.sacs().get(k);
            if (piece.partWrecked(p) || piece.partLayer(p) != Layer.AIR) {
                continue;
            }
            if (best < 0 || Math.abs(piece.partX(p) - x) < Math.abs(piece.partX(best) - x)) {
                best = p;
            }
        }
        return best;
    }

    static double innerLeft(CoilSpec spec) {
        return spec.body().left() + spec.body().clearance();
    }

    static double innerRight(CoilSpec spec) {
        return spec.body().right() - spec.body().clearance();
    }

    static double innerBottom(CoilSpec spec) {
        return spec.body().bottom() + spec.body().clearance();
    }

    /**
     * The gap of the fan wall coming at the hull, at its height: while the throat glows for a wall, the last wall's
     * gap moved by the chain (unknown yet: NaN, the hull holds), and while a wall's orbs (at least {@link #WALL_ORBS}
     * bullets on one circle round the throat, not yet past the hull's height) are above it, the widest angular gap
     * among them, at the hull's height; NaN for none.
     */
    static double wallGap(Sortie sortie, CoilArena coil, double shipY) {
        double tx = coil.throatX();
        double ty = coil.throatY();
        double drop = ty - shipY;
        if (!(drop > 0)) {
            return Double.NaN;
        }
        double radius = Double.NaN;
        for (int i = 0; i < sortie.bulletCount() && Double.isNaN(radius); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            double r = Math.hypot(bullet.renderX(1) - tx, bullet.renderY(1) - ty);
            if (r > drop + DarkPlay.HULL_Y + 10 || bullet.renderY(1) > ty) {
                continue;
            }
            int on = 0;
            for (int j = 0; j < sortie.bulletCount(); j++) {
                EnemyBullet other = sortie.bullet(j);
                double q = Math.hypot(other.renderX(1) - tx, other.renderY(1) - ty);
                if (Math.abs(q - r) < ORB_TOLERANCE && other.renderY(1) <= ty) {
                    on++;
                }
            }
            if (on >= WALL_ORBS) {
                radius = r;
            }
        }
        if (Double.isNaN(radius)) {
            return Double.NaN;
        }
        // The orbs' angles from straight down; the widest gap between neighbours (the missing orb) inside the walls.
        double bestGap = 0;
        double bestAngle = Double.NaN;
        double previous = Double.NaN;
        // Sorted by angle without allocating: walk the angles in rising order by repeated minimum search.
        double last = -Math.PI;
        while (true) {
            double next = Double.POSITIVE_INFINITY;
            for (int j = 0; j < sortie.bulletCount(); j++) {
                EnemyBullet other = sortie.bullet(j);
                double ox = other.renderX(1) - tx;
                double oy = other.renderY(1) - ty;
                if (Math.abs(Math.hypot(ox, oy) - radius) >= ORB_TOLERANCE || oy > 0) {
                    continue;
                }
                double angle = Math.atan2(ox, -oy);
                if (angle > last + 1e-9 && angle < next) {
                    next = angle;
                }
            }
            if (next == Double.POSITIVE_INFINITY) {
                break;
            }
            if (!Double.isNaN(previous) && next - previous > bestGap) {
                bestGap = next - previous;
                bestAngle = (next + previous) / 2;
            }
            previous = next;
            last = next;
        }
        if (Double.isNaN(bestAngle)) {
            return Double.NaN;
        }
        return tx + drop * Math.tan(bestAngle);
    }

    /** What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the bight, the column, the line. */
    static double cost(
            Sortie sortie,
            CoilArena coil,
            CoilSpec spec,
            double dx,
            double dy,
            double targetX,
            double targetY,
            double alongX) {
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        boolean walled = coil.walled();
        double minX = walled ? innerLeft(spec) : margin;
        double maxX = walled ? innerRight(spec) : PlayField.WIDTH - margin;
        double minY = walled ? innerBottom(spec) : margin;
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            x = Math.clamp(x0 + dx * speed * t, minX, maxX);
            y = Math.clamp(y0 + dy * speed * t, minY, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / SAMPLES;
            double nearest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < sortie.bulletCount(); i++) {
                EnemyBullet bullet = sortie.bullet(i);
                double bx = bullet.renderX(1) + (bullet.renderX(1) - bullet.renderX(0)) * steps;
                double by = bullet.renderY(1) + (bullet.renderY(1) - bullet.renderY(0)) * steps;
                nearest = Math.min(nearest, MovePlanner.gap(bx - x, by - y, DarkPlay.HULL_X, DarkPlay.HULL_Y));
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
            if (!hit && nearest < 1) {
                cost += HIT * weight;
                hit = true;
            }
            if (nearest < CLEAR) {
                cost += weight * 4 * (CLEAR - nearest);
            }
            if (inBight(coil, spec, x, y) || inColumn(coil, spec, x)) {
                cost += weight * STRUCK;
            }
        }
        cost += alongX * Math.abs(x - targetX) + ALONG_Y * Math.abs(y - targetY);
        return cost;
    }

    /** Whether a hull at (x, y) is inside a told or swinging (or holding) bight's full reach, with the margin. */
    static boolean inBight(CoilArena coil, CoilSpec spec, double x, double y) {
        CoilArena.SweepState state = coil.sweepState();
        if (state == CoilArena.SweepState.IDLE || state == CoilArena.SweepState.BACK) {
            return false;
        }
        CoilSpec.Sweeps sweeps = spec.sweeps();
        double half = (sweeps.segments() / 2.0 + 0.5) * spec.body().spacing();
        CoilSpec.Side side = coil.sweepSide();
        boolean bottom = side == CoilSpec.Side.BOTTOM;
        // The bight's centre along its run: the mean of its segments' rest places along it.
        double sum = 0;
        int n = 0;
        for (int i = 0; i < coil.segmentCount(); i++) {
            if (coil.inBight(i)) {
                sum += bottom ? coil.segmentX(i) : coil.segmentY(i);
                n++;
            }
        }
        if (n == 0) {
            return false;
        }
        double centre = sum / n;
        double u = bottom ? x : y;
        double hull = bottom ? DarkPlay.HULL_X : DarkPlay.HULL_Y;
        double t = (Math.abs(u - centre) - hull) / half;
        if (t >= 1) {
            return false;
        }
        double reach =
                (bottom ? sweeps.bottomReach() : sweeps.reach()) * (t <= 0 ? 1 : 0.5 + 0.5 * Math.cos(Math.PI * t));
        return switch (side) {
            case LEFT -> x - DarkPlay.HULL_X < innerLeft(spec) + reach + MARGIN;
            case RIGHT -> x + DarkPlay.HULL_X > innerRight(spec) - reach - MARGIN;
            case BOTTOM -> y - DarkPlay.HULL_Y < innerBottom(spec) + reach + MARGIN;
        };
    }

    /** Whether a hull at x is in the lunge's column while it threatens (its shadow, the lunge, its body), with the margin. */
    static boolean inColumn(CoilArena coil, CoilSpec spec, double x) {
        CoilArena.DiveState dive = coil.diveState();
        if (dive != CoilArena.DiveState.SHADOW
                && dive != CoilArena.DiveState.LUNGE
                && dive != CoilArena.DiveState.BODY) {
            return false;
        }
        return Math.abs(x - coil.columnX())
                < coil.columnWidth() / 2 + spec.body().clearance() + DarkPlay.HULL_X + MARGIN;
    }
}
