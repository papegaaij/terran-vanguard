package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Layer;
import vanguard.sim.Missile;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Uplinks;

/**
 * M10 part G, step G3 (design/campaign Level 41 Callisto Fortress, the halts and the clock, the secondary {@code FILES};
 * m10g-gaps.md §1 #6, #14; see {@link Autopilot}): the autopilot's play at the pylon gates. A gate's halt stands while a
 * pylon stands, its domes cover the guns round it and stop the beam, and its halt cycle sends waves in meanwhile; so
 * every move is planned ({@link #move}), as {@link VentPlay} does: of the eight ways and standing still, the one whose
 * look-ahead keeps the hull off the bullets, the SAM Nests' missiles, the air bodies and the Rail Bunkers' lines (told
 * or live; {@link LeechPlay#line}), and nearest its line. Its line in a halt, in this order: the gate's archive core
 * (Varga's upload: over the open core, inside its radius until the file is in), the core's hatch on the screen (under
 * it, the beam's column clear), a hurt Chimera ({@link DivePlay#focus}: finished before it heals), a cheap gun a fallen
 * dome left bare ({@link #bareGun}), then the awake pylons (the one whose column the domes' covered units and the
 * hardened bunkers leave clear, the nearer across; one whose every column is blocked from below, from a height between
 * the highest blocker and the pylon). Outside the halts, and in the Halo Platform's (no pylon there: {@link RingPlay}'s),
 * the autopilot flies as everywhere. With the EMP Burst fitted and ready it goes off in a halt when the pylon's every
 * column is blocked (the stun drops the domes). Only a level of several held gates takes this play (Level 41: {@link
 * Sortie#gates()} above 1), so nothing here moves another level's flight.
 * Allocation-free.
 */
final class GatePlay {
    private GatePlay() {}

    /** How far ahead each move is followed, in samples of {@link #SAMPLE} s. */
    static final int SAMPLES = 14;

    static final double SAMPLE = 0.05;
    /** The cost of the hull meeting a bullet, a missile, a body or a live line in the look-ahead, the earlier the dearer. */
    static final double HIT = 1000;
    /** Below this clearance to a bullet, a missile or a body a move costs more, px. */
    static final double CLEAR = 14;
    /** Its height under a target whose column is clear, px. */
    static final double CRUISE_Y = 100;
    /** The least it keeps under a target it lines up under, px. */
    static final double UNDER = 60;
    /** The gap it keeps above a blocker it climbs over to reach a pylon, px. */
    static final double OVER = 30;
    /** A bare gun of at most this many HP is worth a detour in a halt (a SAM Nest's 20, a Graft Turret's 24). */
    static final double CHEAP = 40;
    /** A pylon it has to climb under counts this much further across, px. */
    static final double CLIMB = PlayField.WIDTH;
    /** The gap kept between the beam's column and a blocker's box, px. */
    static final double MARGIN = 3;
    /** The front beam's half width (the plan's Ion Beam L5: 14 px), px. */
    static final double BEAM_HALF = 7;
    /** The step between the columns tried across a target's box, px. */
    static final double COLUMN_STEP = 4;
    /** The cost of each px off its line across, and up or down, at the look-ahead's end. */
    static final double ALONG_X = 1.2;

    static final double ALONG_Y = 0.15;
    /** The cost of each px off an open core's centre beyond {@link #INSIDE} (the upload). */
    static final double HOVER = 2.5;
    /** How far from an open core's centre it may roam while uploading, px (the radius is 40). */
    static final double INSIDE = 24;
    /** It keeps off the side edges, px. */
    static final double EDGE = 40;
    /** How near its line (across, or the core's centre) it closes in on before the planner alone moves it, px. */
    static final double NEAR = 8;
    /** The extra hazard cost it accepts to close in on its line. */
    static final double TIE = 20;

    /** Whether the level has pylon gates: several held gates (Level 41). */
    static boolean gated(Sortie sortie) {
        return sortie.gates() > 1;
    }

    /** Whether the play flies now: a gate's halt with a file to take, a hatch to open or an awake pylon. */
    static boolean active(Sortie sortie) {
        if (!gated(sortie) || !sortie.halting()) {
            return false;
        }
        return openCore(sortie) >= 0 || hatch(sortie) != null || pylon(sortie) != null;
    }

    /**
     * A cheap gun a fallen dome left bare (a SAM Nest, a Graft Turret: at most {@link #CHEAP} HP, not hardened, hittable
     * now, its column clear), the nearest across; null for none. A short detour in a halt: bare, it fires on.
     */
    static Enemy bareGun(Sortie sortie) {
        double shipX = sortie.ship().x();
        Enemy best = null;
        double bestX = Double.NaN;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.layer() != Layer.GROUND
                    || enemy.holder()
                    || enemy.dormant()
                    || enemy.covered()
                    || enemy.invulnerable()
                    || enemy.spec().hardened()
                    || enemy.hp() <= 0
                    || enemy.hp() > CHEAP
                    || y < CRUISE_Y + UNDER
                    || y > PlayField.HEIGHT - 10) {
                continue;
            }
            double x = clearColumn(sortie, enemy.renderX(1), enemy.hitbox().width() / 2, CRUISE_Y, y, shipX);
            if (!Double.isNaN(x) && (best == null || Math.abs(x - shipX) < Math.abs(bestX - shipX))) {
                best = enemy;
                bestX = x;
            }
        }
        return best;
    }

    /** The open core on the screen whose file is still to come (the lowest), -1 for none. */
    static int openCore(Sortie sortie) {
        Uplinks uplinks = sortie.uplinks();
        if (uplinks == null) {
            return -1;
        }
        int best = -1;
        for (int i = 0; i < uplinks.count(); i++) {
            if (uplinks.state(i) == Uplinks.OPEN
                    && uplinks.y(i) > 0
                    && uplinks.y(i) < PlayField.HEIGHT
                    && (best < 0 || uplinks.y(i) < uplinks.y(best))) {
                best = i;
            }
        }
        return best;
    }

    /** A shut core's hatch on the screen (the lowest), null for none. */
    static GroundObject hatch(Sortie sortie) {
        Uplinks uplinks = sortie.uplinks();
        if (uplinks == null) {
            return null;
        }
        GroundObject best = null;
        for (int i = 0; i < uplinks.count(); i++) {
            if (uplinks.state(i) != Uplinks.SHUT) {
                continue;
            }
            GroundObjectSpec spec = sortie.script().groundObjects().get(uplinks.hatch(i));
            for (int k = 0; k < sortie.groundObjectCount(); k++) {
                GroundObject object = sortie.groundObject(k);
                double y = object.renderY(1);
                if (object.spec() == spec
                        && y > 10
                        && y < PlayField.HEIGHT - 10
                        && (best == null || y < best.renderY(1))) {
                    best = object;
                }
            }
        }
        return best;
    }

    /** Whether {@code enemy} is an awake pylon still standing on the screen (a gate's holder). */
    static boolean awakePylon(Enemy enemy) {
        double y = enemy.renderY(1);
        return enemy.holder()
                && !enemy.dormant()
                && enemy.hp() > 0
                && y > 0
                && y < PlayField.HEIGHT
                && enemy.renderX(1) > 0
                && enemy.renderX(1) < PlayField.WIDTH;
    }

    /**
     * The pylon to take: of the awake pylons, one with a clear column from {@link #CRUISE_Y} first, then one it can
     * climb under (a gap over its blockers), the nearer across the better; null for none.
     */
    static Enemy pylon(Sortie sortie) {
        double shipX = sortie.ship().x();
        Enemy best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!awakePylon(enemy)) {
                continue;
            }
            double half = enemy.hitbox().width() / 2;
            double x = clearColumn(sortie, enemy.renderX(1), half, CRUISE_Y, enemy.renderY(1), shipX);
            double score;
            if (!Double.isNaN(x)) {
                score = Math.abs(x - shipX);
            } else {
                double y = climbY(sortie, enemy);
                if (Double.isNaN(y)) {
                    continue;
                }
                score = CLIMB + Math.abs(enemy.renderX(1) - shipX);
            }
            if (score < bestScore) {
                bestScore = score;
                best = enemy;
            }
        }
        return best;
    }

    /**
     * The column under a target at {@code x} (half width {@code half}) that the beam still touches and no blocker crosses
     * between {@code fromY} and {@code toY} (with {@link #MARGIN} px to spare), the nearest the target's centre; NaN when
     * every one is blocked.
     */
    static double clearColumn(Sortie sortie, double x, double half, double fromY, double toY, double shipX) {
        double reach = Math.max(0, half + BEAM_HALF - MARGIN);
        double edge = sortie.ship().spec().edgeLimit();
        double best = Double.NaN;
        for (double d = -reach; d <= reach + 1e-9; d += COLUMN_STEP) {
            double cx = x + d;
            if (cx < edge || cx > PlayField.WIDTH - edge) {
                continue;
            }
            // the one nearest the target's centre (the beam's whole width on it), the nearer the ship on a tie
            if (blocker(sortie, cx, fromY, toY) == null
                    && (Double.isNaN(best)
                            || Math.abs(cx - x) < Math.abs(best - x)
                            || (Math.abs(cx - x) == Math.abs(best - x)
                                    && Math.abs(cx - shipX) < Math.abs(best - shipX)))) {
                best = cx;
            }
        }
        return best;
    }

    /**
     * The highest unit that stops the beam in its column at {@code x} between {@code fromY} and {@code toY} (a ground
     * unit a dome covers, or a hardened one: the beam is no anti-ground gun), {@link #MARGIN} px round it; null for none.
     */
    static Enemy blocker(Sortie sortie, double x, double fromY, double toY) {
        Enemy highest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.layer() != Layer.GROUND
                    || enemy.dormant()
                    || enemy.holder()
                    || !(enemy.covered() || enemy.spec().hardened())) {
                continue;
            }
            double ex = enemy.renderX(1);
            double ey = enemy.renderY(1);
            if (Math.abs(ex - x) < enemy.hitbox().width() / 2 + BEAM_HALF + MARGIN
                    && ey > fromY
                    && ey < toY
                    && (highest == null || ey > highest.renderY(1))) {
                highest = enemy;
            }
        }
        return highest;
    }

    /**
     * The height it climbs to under a pylon whose columns are all blocked from {@link #CRUISE_Y}: {@link #OVER} px above
     * the highest blocker under its centre, if that leaves {@link #UNDER} px under the pylon; NaN otherwise.
     */
    static double climbY(Sortie sortie, Enemy pylon) {
        double px = pylon.renderX(1);
        double py = pylon.renderY(1);
        Enemy blocking = blocker(sortie, px, CRUISE_Y, py);
        if (blocking == null) {
            return CRUISE_Y;
        }
        double y = blocking.renderY(1) + blocking.hitbox().height() / 2 + OVER;
        return y <= py - UNDER ? y : Double.NaN;
    }

    /** One step's move and, when due, the special (the fire bit is the caller's). */
    static int move(Sortie sortie) {
        double shipX = sortie.ship().x();
        double targetX = shipX;
        double targetY = CRUISE_Y;
        double alongX = ALONG_X;
        double alongY = ALONG_Y;
        boolean blocked = false;
        boolean hover = false;
        int core = openCore(sortie);
        GroundObject hatch = core < 0 ? hatch(sortie) : null;
        // a hurt Chimera held until it dies, before it heals (DivePlay's focus)
        Enemy hurt =
                core < 0 && hatch == null ? DivePlay.focus(sortie, sortie.ship().y()) : null;
        Enemy bare = core < 0 && hatch == null && hurt == null ? bareGun(sortie) : null;
        Enemy pylon = core < 0 && hatch == null && hurt == null && bare == null ? pylon(sortie) : null;
        if (core >= 0) {
            Uplinks uplinks = sortie.uplinks();
            targetX = uplinks.x(core);
            targetY = uplinks.y(core);
            hover = true;
        } else if (hatch != null) {
            double hy = hatch.renderY(1);
            double half = hatch.spec().size().width() / 2;
            double fromY = Math.max(40, Math.min(CRUISE_Y, hy - UNDER));
            double x = clearColumn(sortie, hatch.renderX(), half, fromY, hy, shipX);
            targetX = Double.isNaN(x) ? hatch.renderX() : x;
            targetY = fromY;
        } else if (hurt != null) {
            targetX = hurt.renderX(1);
        } else if (bare != null) {
            targetX = clearColumn(sortie, bare.renderX(1), bare.hitbox().width() / 2, CRUISE_Y, bare.renderY(1), shipX);
        } else if (pylon != null) {
            double py = pylon.renderY(1);
            double x = clearColumn(sortie, pylon.renderX(1), pylon.hitbox().width() / 2, CRUISE_Y, py, shipX);
            if (!Double.isNaN(x)) {
                targetX = x;
            } else {
                blocked = true;
                targetX = pylon.renderX(1);
                targetY = climbY(sortie, pylon);
            }
        }
        targetX = Math.clamp(targetX, EDGE, PlayField.WIDTH - EDGE);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double cost =
                    cost(sortie, MovePlanner.MOVE_X[m], MovePlanner.MOVE_Y[m], targetX, targetY, alongX, alongY, hover);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        // As VentPlay's lifeboat: the look-ahead's full-speed end overshoots a near line as far as it falls short of it
        // (it stood beside the core, out of its radius): the way straight at it when its hazards cost no more than the
        // best move's.
        double offX = targetX - shipX;
        double offY = targetY - sortie.ship().y();
        boolean far = hover ? Math.hypot(offX, offY) > INSIDE : Math.abs(offX) > NEAR;
        if (far) {
            int toward = toward(offX, Math.abs(offY) > NEAR ? offY : 0);
            double hazards =
                    cost(sortie, MovePlanner.MOVE_X[toward], MovePlanner.MOVE_Y[toward], targetX, targetY, 0, 0, false);
            if (hazards
                    <= cost(sortie, MovePlanner.MOVE_X[best], MovePlanner.MOVE_Y[best], targetX, targetY, 0, 0, false)
                            + TIE) {
                best = toward;
            }
        }
        boolean emp = blocked && sortie.halting() && empReady(sortie);
        return MovePlanner.MOVES[best] | (emp ? vanguard.sim.Command.SPECIAL.bit() : 0);
    }

    /** The move of the nine ({@link MovePlanner#MOVES}) along (dx, dy): a side, a diagonal or none. */
    static int toward(double dx, double dy) {
        int sx = dx > NEAR ? 1 : dx < -NEAR ? -1 : 0;
        int sy = dy > NEAR ? 1 : dy < -NEAR ? -1 : 0;
        if (sy == 0) {
            return sx < 0 ? 1 : sx > 0 ? 2 : 0;
        }
        if (sx == 0) {
            return sy > 0 ? 3 : 4;
        }
        return sy > 0 ? (sx < 0 ? 5 : 6) : (sx < 0 ? 7 : 8);
    }

    /** Whether the EMP Burst is fitted, ready and charged. */
    static boolean empReady(Sortie sortie) {
        vanguard.sim.SpecialSlot special = sortie.special();
        return special.fitted() && special.name().equals("EMP Burst") && special.ready() && special.charges() > 0;
    }

    /** What flying (dx, dy) at full speed for the look-ahead costs: hits, close calls, the rail lines, the line. */
    static double cost(
            Sortie sortie,
            double dx,
            double dy,
            double targetX,
            double targetY,
            double alongX,
            double alongY,
            boolean hover) {
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
                if (enemy.spec().laserLine().isPresent()) {
                    cost += weight * LeechPlay.line(enemy, x, y, t);
                }
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
        }
        if (hover) {
            // over an open core: anywhere well inside its radius uploads; out of it costs
            cost += HOVER * Math.max(0, Math.hypot(x - targetX, y - targetY) - INSIDE);
        } else {
            cost += alongX * Math.abs(x - targetX) + alongY * Math.abs(y - targetY);
        }
        cost += 2 * Math.max(0, Math.max(EDGE - x, x - (PlayField.WIDTH - EDGE)));
        return cost;
    }
}
