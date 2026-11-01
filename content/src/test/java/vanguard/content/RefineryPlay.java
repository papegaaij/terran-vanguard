package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.Eruptions;
import vanguard.sim.HazardSpec;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M10 part E, step E3 (design/campaign Level 39 Io Flyby; m10e-gaps.md §1 #17; see {@link Autopilot}): the refinery's
 * plays, for a destroy-targets level with holds over its works and eruptions. The level's Hornets make it a hunted level,
 * so its moves are {@link VentPlay}'s (every move planned among the bullets, the missiles and the bodies), its line this
 * play's: the lowest cracking tower on the screen first (a tower leaving the screen alive fails the mission at once),
 * lined up from the side away from a telling vent beside it; the pump stations (the secondary's escapes units) only when
 * no tower is on the screen. On top of {@link VentPlay#cost}'s look-ahead ({@link #cost}): the flying rocks are bodies
 * (each flown on along its line out of its vent, so the guns meet the rocks in its lane and it steps round the rest), a
 * vent in its tell (or its rocks still rising) costs every sample above it inside its 100° fan within the rocks' reach,
 * and a Sentinel Tower's told or sweeping arc within its beam's length costs every sample inside it. Only a level with
 * eruptions takes these plays (Level 39), so nothing here moves another level's flight. Allocation-free.
 */
final class RefineryPlay {
    private RefineryPlay() {}

    /** The cost of each sample with the hull in a hot fan (late in its tell, or its rocks rising). */
    static final double FAN = 300;

    /** The cost of each sample with the hull in a fan early in its tell: leave it while it is cheap. */
    static final double FAN_EARLY = 60;

    /** A tell this far through counts as hot. */
    static final double TELL_HOT = 0.35;

    /** The margin kept round a fan's edges and its reach, px. */
    static final double FAN_MARGIN = 14;

    /** The cost of each sample with the hull inside a Sentinel's told or sweeping arc. */
    static final double ARC = 200;

    /** The margin kept round a Sentinel's arc, radians (about 8°), and beyond its length, px. */
    static final double ARC_MARGIN = 0.14;

    static final double ARC_REACH = 24;

    /** How far beside a tower it lines up, away from a telling vent near it, px (the tower's box is 72 px wide). */
    static final double ASIDE = 18;

    /** A telling vent this near a tower (across, and up or down) moves the line aside, px. */
    static final double VENT_NEAR = 160;

    /** How far under a tower it keeps, px, and the cost of each px less a sample. */
    static final double BELOW = 80;

    static final double ABOVE = 0.5;

    /** How near a tower's line it closes in on before it holds still under it, px. */
    static final double COLUMN = 8;

    /** The extra hazard cost it accepts to close in on a tower's line. */
    static final double TIE = 20;

    /** Whether the level has eruptions: the plays below apply (Level 39). */
    static boolean refinery(Sortie sortie) {
        return sortie.eruptions().isPresent();
    }

    /** Whether {@code enemy} is a live unit of a destroy-targets group (a cracking tower). */
    static boolean target(Sortie sortie, Enemy enemy) {
        return enemy.groupIndex() >= 0
                && !sortie.script().objectives().targets().isEmpty()
                && enemy.hp() > 0
                && enemy.layer().touchable();
    }

    /**
     * The tower to take now: the lowest live destroy-targets unit on the screen (its box inside the top edge; one below
     * the ship too: {@link #cost} brings it back under it); null for none, or on a level without eruptions.
     */
    static Enemy tower(Sortie sortie) {
        if (!refinery(sortie)) {
            return null;
        }
        Enemy best = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!target(sortie, enemy)) {
                continue;
            }
            double y = enemy.renderY(1);
            if (y + enemy.hitbox().height() / 2 <= 0 || y - enemy.hitbox().height() / 2 >= PlayField.HEIGHT) {
                continue;
            }
            if (best == null || y < best.renderY(1)) {
                best = enemy;
            }
        }
        return best;
    }

    /**
     * The weight of each px off the tower's line: {@link VentPlay#BEACON_X}, {@link #URGENT}× once the tower's time
     * left on the screen (its top edge to the bottom edge at the scroll now) is short of what its HP take at {@link
     * #DPS} with {@link #SPARE} s to spare.
     */
    static double weight(Sortie sortie, Enemy tower) {
        double left = (tower.renderY(1) + tower.hitbox().height() / 2) / Math.max(1, sortie.groundSpeed());
        return left < tower.hp() / DPS + SPARE ? VentPlay.BEACON_X * URGENT : VentPlay.BEACON_X;
    }

    /** The guns' rate on a tower it lines up under, about (the beam and the pods), HP/s. */
    static final double DPS = 150;

    /** The time to spare before a tower counts as urgent, s, and how much harder it pulls then. */
    static final double SPARE = 2;

    static final double URGENT = 3;

    /** In a hold the EMP Burst goes off with this many air units on the screen (a wing between it and the towers). */
    static final int EMP_CROWD = 4;

    /** ... or this many with its shield under {@link #EMP_SHIELD} of its capacity. */
    static final int EMP_FEW = 2;

    static final double EMP_SHIELD = 0.4;

    /**
     * Whether the EMP Burst (fitted, ready, charged) goes off now: in a hold over a works with a tower still standing,
     * with {@link #EMP_CROWD} air units on the screen, or {@link #EMP_FEW} with its shield low (it stuns the machines
     * and strips the Act 6 shields: the guns reach the towers again).
     */
    static boolean empDue(Sortie sortie) {
        vanguard.sim.SpecialSlot special = sortie.special();
        if (!refinery(sortie)
                || !sortie.holdActive()
                || !special.fitted()
                || !special.name().equals("EMP Burst")
                || !special.ready()
                || special.charges() <= 0
                || tower(sortie) == null) {
            return false;
        }
        int air = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.layer() == Layer.GROUND || !enemy.layer().touchable() || enemy.hp() <= 0) {
                continue;
            }
            double y = enemy.renderY(1);
            double x = enemy.renderX(1);
            if (x > 0 && x < PlayField.WIDTH && y > 0 && y < PlayField.HEIGHT) {
                air++;
            }
        }
        boolean low = sortie.ship().defences().shield()
                < EMP_SHIELD * sortie.ship().defences().shieldCapacity();
        return air >= EMP_CROWD || (low && air >= EMP_FEW);
    }

    /** Its line under {@code tower}: the tower's x, moved {@link #ASIDE} px away from a telling vent beside it. */
    static double lineX(Sortie sortie, Enemy tower) {
        double x = tower.renderX(1);
        Eruptions eruptions = sortie.eruptions().orElseThrow();
        double nearest = Double.POSITIVE_INFINITY;
        double side = 0;
        for (int v = 0; v < eruptions.vents(); v++) {
            if (!eruptions.ventPresent(v) || !hot(eruptions, v)) {
                continue;
            }
            double dx = eruptions.ventX(v) - x;
            double dy = eruptions.ventY(v) - tower.renderY(1);
            if (Math.abs(dx) < VENT_NEAR && Math.abs(dy) < VENT_NEAR && Math.abs(dx) < nearest) {
                nearest = Math.abs(dx);
                side = dx >= 0 ? -1 : 1;
            }
        }
        return x + side * ASIDE;
    }

    /** Whether vent {@code v} tells now or its rocks still rise over it (they fly out next). */
    private static boolean hot(Eruptions eruptions, int v) {
        if (eruptions.telling(v)) {
            return true;
        }
        for (int r = 0; r < eruptions.rocks(); r++) {
            if (eruptions.rockUsed(r)
                    && eruptions.rockPhase(r) == Eruptions.Phase.RISE
                    && Math.abs(eruptions.rockX(r) - eruptions.ventX(v)) < 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * The gap from the hull at ({@code x}, {@code y}) to the nearest flying rock {@code t} s on (each flown on along its
     * line out of the vent beneath it at its eased speed); infinite without one.
     */
    static double rockGap(Sortie sortie, double x, double y, double t) {
        Eruptions eruptions = sortie.eruptions().orElseThrow();
        HazardSpec.EruptionSpec spec = eruptions.spec();
        double nearest = Double.POSITIVE_INFINITY;
        for (int r = 0; r < eruptions.rocks(); r++) {
            if (!eruptions.shootable(r)) {
                continue;
            }
            double rx = eruptions.rockX(r);
            double ry = eruptions.rockY(r);
            // its line: out of the nearest vent below it (the fan's spread), straight up without one
            double dirX = 0;
            double dirY = 1;
            double best = Double.POSITIVE_INFINITY;
            for (int v = 0; v < eruptions.vents(); v++) {
                if (!eruptions.ventPresent(v)) {
                    continue;
                }
                double dx = rx - eruptions.ventX(v);
                double dy = ry - eruptions.ventY(v);
                double d = Math.hypot(dx, dy);
                if (dy > 0 && d > 1 && d < best && Math.abs(dx) <= dy * 1.4) {
                    best = d;
                    dirX = dx / d;
                    dirY = dy / d;
                }
            }
            double share = eruptions.rockProgress(r);
            double speed = spec.speed() + (spec.endSpeed() - spec.speed()) * share;
            double left = (1 - share) * spec.flySeconds();
            double flown = Math.min(t, left);
            double fx = rx + dirX * speed * flown;
            double fy = ry + dirY * speed * flown;
            nearest = Math.min(
                    nearest,
                    MovePlanner.gap(
                            fx - x,
                            fy - y,
                            DarkPlay.HULL_X + spec.rock().width() / 2,
                            DarkPlay.HULL_Y + spec.rock().height() / 2));
        }
        return nearest;
    }

    /**
     * The extra cost of the hull at ({@code x}, {@code y}) {@code t} s on (the ground scrolled {@code groundSpeed} px/s
     * meanwhile): inside a hot vent's fan within the rocks' reach, inside a Sentinel's told or sweeping arc.
     */
    static double cost(Sortie sortie, double x, double y, double t) {
        Eruptions eruptions = sortie.eruptions().orElseThrow();
        HazardSpec.EruptionSpec spec = eruptions.spec();
        double groundSpeed = sortie.groundSpeed();
        double cost = 0;
        double tan = Math.tan(spec.fanRadians() / 2);
        double reach = spec.reach() + FAN_MARGIN + DarkPlay.HULL_Y;
        double rockHalf = spec.rock().width() / 2.0;
        for (int v = 0; v < eruptions.vents(); v++) {
            if (!eruptions.ventPresent(v) || !hot(eruptions, v)) {
                continue;
            }
            double dx = Math.abs(x - eruptions.ventX(v));
            double dy = y - (eruptions.ventY(v) - groundSpeed * t);
            double up = dy + DarkPlay.HULL_Y + rockHalf;
            if (up <= 0 || Math.hypot(dx, dy) > reach) {
                continue;
            }
            if (dx - DarkPlay.HULL_X - rockHalf - FAN_MARGIN <= tan * up) {
                boolean late = !eruptions.telling(v) || eruptions.tellProgress(v) >= TELL_HOT;
                cost += late ? FAN : FAN_EARLY;
            }
        }
        Enemy tower = tower(sortie);
        if (tower != null) {
            // under the tower, in its guns' line: each px less than BELOW under it costs (a dodge up past it lost the
            // tower: the guns only fire up)
            double towerY = tower.renderY(1) - groundSpeed * t;
            cost += ABOVE * Math.max(0, y + BELOW - towerY);
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().groundSweep().isEmpty() || !(enemy.telegraphing() || enemy.sweeping())) {
                continue;
            }
            double ex = enemy.renderX(1);
            double ey = enemy.renderY(1) - (enemy.layer() == Layer.GROUND ? groundSpeed * t : 0);
            double dx = x - ex;
            double dy = y - ey;
            double length = enemy.spec().sweep().orElseThrow().length();
            if (Math.hypot(dx, dy) > length + ARC_REACH + DarkPlay.HULL_Y) {
                continue;
            }
            if (enemy.sweepFullTurn() || inArc(enemy, StrictMath.atan2(-dx, -dy))) {
                cost += ARC;
            }
        }
        return cost;
    }

    /** Whether {@code heading} (radians clockwise from straight down) lies in the sweep's arc, its margin round it. */
    private static boolean inArc(Enemy enemy, double heading) {
        double from = enemy.sweepFrom();
        double span = Math.IEEEremainder(enemy.sweepTo() - from, 2 * Math.PI);
        double off = Math.IEEEremainder(heading - from, 2 * Math.PI);
        if (span < 0) {
            span = -span;
            off = -off;
        }
        return off >= -ARC_MARGIN && off <= span + ARC_MARGIN;
    }
}
