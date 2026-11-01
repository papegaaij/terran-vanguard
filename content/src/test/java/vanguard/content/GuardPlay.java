package vanguard.content;

import vanguard.sim.AcePlaybook;
import vanguard.sim.BossLines;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.Layer;
import vanguard.sim.Missile;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;

/**
 * M10 part F (design/enemies/bosses/honour-guard, The escort (Rook); m10f-gaps.md §1 row 8): the autopilot against a
 * squadron mid-boss (Level 40's Honour Guard). Step F2's first cut lined up under the nearest ace; step F3 plans every
 * move ({@link #move}, as {@link RingPlay} does): of the eight ways and standing still, the one whose look-ahead keeps
 * the hull off the bullets and the aces' bodies (the way to its line when nothing threatens on it, {@link
 * MovePlanner#bossMove}'s rule), out of the leader's sniper line once it would be live (it tracks the
 * ship in its telegraph and locks 0.2 s before it fires: the planner steps off it from just before the lock, {@link
 * #line}),
 * {@link AcePlaybook#DIVE_KEEP_OFF} px off a told or running dive's line ("turn for Kestrel": the side-step off it), and
 * nearest its line. Its line: the nearest ace (Rook's pick); the rear mount's homing swarm takes Kestrel
 * on its station below it. It holds the cruise height. While no ace is on the field it holds the middle.
 * Allocation-free and stateless.
 */
final class GuardPlay {
    private GuardPlay() {}

    /** The look-ahead: samples of {@link #SAMPLE} s. */
    static final int SAMPLES = 16;

    static final double SAMPLE = 0.05;
    /** The margin kept round an ace's hull beyond the planner's clearance, px (its contact pushes the ship, 15). */
    static final double BODY_MARGIN = 0;
    /** The cost of each sample with the hull within a told dive's keep-off of its line. */
    static final double ON_DIVE = 60;
    /** It keeps off the side edges, where an aimed fan traps it, px. */
    static final double EDGE = 50;

    /** The level's squadron mid-boss while it fights (present, not yet destroyed); null otherwise. */
    static SetPiece squadronBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.squadron().isPresent() && piece.present() && !piece.destroyed()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move against {@code guard} (the fire bit is the caller's). */
    static int move(Sortie sortie, SetPiece guard) {
        Squadron squadron = guard.squadron().orElseThrow();
        int ace = target(sortie, squadron);
        double targetX = Math.clamp(ace < 0 ? PlayField.WIDTH / 2.0 : squadron.x(ace), EDGE, PlayField.WIDTH - EDGE);
        // the way to its line when nothing threatens on it (step F2's first cut, MovePlanner#bossMove's rule) ...
        int wanted = MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), targetX, Autopilot.CRUISE_Y);
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            if (MovePlanner.MOVES[m] == wanted
                    && cost(sortie, guard, squadron, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], Double.NaN, 0)
                            == 0) {
                return wanted;
            }
        }
        // ... else the planned way
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost = cost(
                    sortie, guard, squadron, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], targetX, Autopilot.CRUISE_Y);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /**
     * The ace it lines up on: the nearest touchable ace (Rook's pick, the spec's escort rule; the rear mount's homing
     * swarm takes Kestrel below it); -1 while none is on the field.
     */
    static int target(Sortie sortie, Squadron squadron) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int a = 0; a < squadron.aceCount(); a++) {
            if (!squadron.touchable(a)) {
                continue;
            }
            double distance = Math.hypot(squadron.x(a) - shipX, squadron.y(a) - shipY);
            if (distance < best) {
                best = distance;
                nearest = a;
            }
        }
        return nearest;
    }

    /**
     * What standing at (x, y) {@code t} s ahead costs against the leader's sniper line: {@link RingPlay#HIT} when it is
     * live then over the hull ({@link RingPlay#line}); nothing while it still tracks the ship (stepping off a line that
     * follows is no use: it steps off once the line locks, {@link #LOCK_MARGIN} s early) or only telegraphs locked.
     */
    static double line(SetPiece guard, BossLines lines, double x, double y, double t) {
        if (!lines.running()) {
            return 0;
        }
        vanguard.sim.EnemySpec.LaserLine spec =
                guard.boss().orElseThrow().attacks().get(lines.attack()).line().orElseThrow();
        double untilLive = lines.live() ? 0 : (1 - lines.charge()) * spec.telegraphSeconds();
        if (spec.tracks() && untilLive > spec.lockBeforeSeconds() + LOCK_MARGIN) {
            return 0;
        }
        double cost = RingPlay.line(guard, lines, x, y, t);
        return cost >= RingPlay.HIT ? cost : 0;
    }

    /** It starts stepping off a tracking line this long before it locks, s (the line re-aims at most this far). */
    static final double LOCK_MARGIN = 0.1;

    /**
     * What flying (dx, dy) at full speed for the look-ahead costs: a hit (the bullets, the missiles, the air units, the
     * aces' bodies, the live sniper line) {@link RingPlay#HIT}, the earlier the dearer; less clearance than {@link
     * RingPlay#CLEAR} to a bullet or a body; standing on the telegraphed line or in a told dive's keep-off; its distance
     * from its line at the look-ahead's nearest, the edges and a turn against its way now. With {@code targetX} NaN the
     * danger alone: a hit (or less than {@link MovePlanner#SAFE} to a bullet or a body) and a running dive's line.
     */
    static double cost(
            Sortie sortie, SetPiece guard, Squadron squadron, double dx, double dy, double targetX, double targetY) {
        // NaN: the danger only (a hit, the safe clearance, a running dive's line), 0 for none
        boolean danger = Double.isNaN(targetX);
        double speed = sortie.ship().spec().speed();
        double margin = sortie.ship().spec().edgeLimit();
        double x0 = sortie.ship().x();
        double y0 = sortie.ship().y();
        double halfW = squadron.spec().hull().width() / 2;
        double halfH = squadron.spec().hull().height() / 2;
        double cost = 0;
        double x = x0;
        double y = y0;
        boolean hit = false;
        double along = Double.POSITIVE_INFINITY;
        BossLines lines = guard.lines().orElse(null);
        AcePlaybook book = squadron.playbook().orElse(null);
        for (int k = 1; k <= SAMPLES; k++) {
            double t = k * SAMPLE;
            x = Math.clamp(x0 + dx * speed * t, margin, PlayField.WIDTH - margin);
            y = Math.clamp(y0 + dy * speed * t, margin, PlayField.HEIGHT - margin);
            double steps = t / SimStep.SECONDS;
            double weight = 1 - 0.5 * (k - 1) / SAMPLES;
            along = Math.min(
                    along, RingPlay.ALONG_X * Math.abs(x - targetX) + RingPlay.ALONG_Y * Math.abs(y - targetY));
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
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (!squadron.joined(a) || squadron.state(a) == Squadron.State.DOWN) {
                    continue;
                }
                double ax = squadron.renderX(a, 1) + (squadron.renderX(a, 1) - squadron.renderX(a, 0)) * steps;
                double ay = squadron.renderY(a, 1) + (squadron.renderY(a, 1) - squadron.renderY(a, 0)) * steps;
                nearest = Math.min(
                        nearest,
                        MovePlanner.gap(
                                ax - x,
                                ay - y,
                                DarkPlay.HULL_X + halfW + BODY_MARGIN,
                                DarkPlay.HULL_Y + halfH + BODY_MARGIN));
            }
            if (lines != null) {
                double line = line(guard, lines, x, y, t);
                if (line >= RingPlay.HIT && !hit) {
                    cost += RingPlay.HIT * weight;
                    hit = true;
                } else if (line < RingPlay.HIT) {
                    cost += weight * line;
                }
            }
            if (book != null && onDive(squadron, book, x, y)) {
                cost += weight * ON_DIVE;
            }
            if (!hit && nearest < (danger ? MovePlanner.SAFE : 1)) {
                cost += RingPlay.HIT * weight;
                hit = true;
            }
            if (nearest < RingPlay.CLEAR && !danger) {
                cost += weight * 4 * (RingPlay.CLEAR - nearest);
            }
        }
        if (danger) {
            return cost;
        }
        cost += along;
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        cost += RingPlay.TURN
                * (Math.abs(dx - sortie.ship().vx() / speed)
                        + Math.abs(dy - sortie.ship().vy() / speed));
        return cost;
    }

    /**
     * Whether a hull at (x, y) lies within {@link AcePlaybook#DIVE_KEEP_OFF} px of a told or running dive's line (from
     * the ace to the spot it dives at, the keep-off beyond it), Rook's rule.
     */
    static boolean onDive(Squadron squadron, AcePlaybook book, double x, double y) {
        for (int a = 0; a < squadron.aceCount(); a++) {
            // in its tell the dive's aim follows the ship (no side-step helps); once it runs its line is locked
            if (!book.diveTold(a) || book.mode(a) != AcePlaybook.Mode.RUN) {
                continue;
            }
            double ox = book.diveFromX(a);
            double oy = book.diveFromY(a);
            double lx = book.diveToX(a) - ox;
            double ly = book.diveToY(a) - oy;
            double length = Math.hypot(lx, ly);
            if (length <= 0) {
                continue;
            }
            double ux = lx / length;
            double uy = ly / length;
            double clear = AcePlaybook.DIVE_KEEP_OFF + Math.max(DarkPlay.HULL_X, DarkPlay.HULL_Y);
            double px = x - ox;
            double py = y - oy;
            double along = px * ux + py * uy;
            double across = Math.abs(px * uy - py * ux);
            if (along >= -clear && along <= length + clear && across < clear) {
                return true;
            }
        }
        return false;
    }
}
