package vanguard.content;

import vanguard.sim.BreachArena;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * The autopilot against the Dust Colossus (M7 part H, Level 21, see {@link Autopilot}): a stand-off from the open
 * gullet on the side its guns reach, out of the bite circle at the bite's tell and out of the eruption's marker, the
 * suction and the pulled rocks in the boss planner's look-ahead ({@link MovePlanner#clearance}).
 */
final class ColossusPlay {
    private ColossusPlay() {}

    /** How far from the mouth's centre it lines up to shoot the gullet, px: out of the bite circle (70) and the inner suction (120). */
    static final double STANDOFF = 170;
    /** Inside the bite circle widened by this, px, it leaves at once from the bite's tell to the snap (its hull and some). */
    static final double BITE_CLEAR = 30;
    /** ... and in the last this many s of the open, before the tell. */
    static final double BITE_EARLY = 0.6;
    /** Inside the marker widened by this, px, it leaves at once until the eruption (its hull and some). */
    static final double MARKER_CLEAR = 34;
    /** Phase 3: its height while the mouth is buried, px: the marker falls on it, and it drops below it to shoot. */
    static final double UNDER_WAIT_Y = 330;
    /** Phase 3: below an erupted mouth at least this far above the bottom edge, else above it (the rear gun), px. */
    static final double UNDER_FLOOR = 60;

    /** M7 part H: the present breaching boss (Level 21's Dust Colossus), alive; null without one. */
    static SetPiece breachingBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && !piece.destroyed() && piece.breach().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** M7 part H: the suction on the ship this step, px/s to the right (0 without a breaching boss). */
    static double pullX(Sortie sortie) {
        SetPiece boss = breachingBoss(sortie);
        return boss == null ? 0 : boss.breach().orElseThrow().pullX();
    }

    /** ... px/s up. */
    static double pullY(Sortie sortie) {
        SetPiece boss = breachingBoss(sortie);
        return boss == null ? 0 : boss.breach().orElseThrow().pullY();
    }

    /**
     * M7 part H, the Dust Colossus (design/enemies/bosses/dust-colossus, Breaches): the boss planner toward the stand-off
     * point of the mouth that is up or coming (the next point once fixed): {@link #STANDOFF} below a front or side mouth
     * (the front guns up its gullet, lined up on its x), above a rear one (the rear gun); in phase 3 it waits high so the
     * marker falls where it can drop below the eruption. From the bite's tell (and the last {@link #BITE_EARLY} s of the
     * open) it leaves the bite circle at once if it is inside it, and it leaves an eruption's marker at once, toward its
     * stand-off point. The rings, the fan, the spiral, the vents' bursts and the pulled rocks are dodged by the
     * planner, the suction added to its look-ahead.
     */
    static int move(Sortie sortie, SetPiece boss) {
        BreachArena breach = boss.breach().orElseThrow();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double margin = sortie.ship().spec().edgeLimit();
        boolean up = breach.phase() != BreachArena.Phase.BURIED && breach.phase() != BreachArena.Phase.TELL;
        boolean marker = breach.marker();
        double mouthX;
        double mouthY;
        BreachArena.Pose pose;
        if (up || marker || (breach.phase() == BreachArena.Phase.TELL && !breach.nextFixed())) {
            mouthX = breach.mouthX();
            mouthY = breach.mouthY();
            pose = breach.pose();
        } else if (breach.nextFixed()) {
            mouthX = breach.nextX();
            mouthY = breach.nextY();
            pose = breach.nextPose();
        } else {
            mouthX = Double.NaN;
            mouthY = Double.NaN;
            pose = null;
        }
        double targetX;
        double targetY;
        if (pose == null || (pose == BreachArena.Pose.UNDER && !up && !marker)) {
            // Nothing coming yet: the field's centre at its cruise; in phase 3 high, where the marker will fall.
            targetX = PlayField.WIDTH / 2.0;
            targetY = boss.phase() >= 2 ? UNDER_WAIT_Y : Autopilot.CRUISE_Y;
        } else {
            targetX = mouthX;
            boolean above = pose == BreachArena.Pose.REAR
                    || (pose == BreachArena.Pose.UNDER && mouthY - STANDOFF < UNDER_FLOOR);
            targetY = above ? mouthY + STANDOFF : mouthY - STANDOFF;
        }
        targetX = Math.clamp(targetX, margin, PlayField.WIDTH - margin);
        targetY = Math.clamp(targetY, margin, PlayField.HEIGHT - margin);
        if (marker) {
            double reach = breach.markerRadius() + MARKER_CLEAR;
            double dx = shipX - breach.markerX();
            double dy = shipY - breach.markerY();
            if (dx * dx + dy * dy < reach * reach) {
                return escapeToward(sortie, targetX, targetY, dx, dy);
            }
        }
        if (biting(boss, breach)) {
            double reach = breach.biteRadius() + BITE_CLEAR;
            double dx = shipX - breach.mouthX();
            double dy = shipY - breach.mouthY();
            if (dx * dx + dy * dy < reach * reach) {
                double d = Math.hypot(dx, dy);
                return MovePlanner.escape(sortie, d > 1e-6 ? dx / d : 0, d > 1e-6 ? dy / d : -1);
            }
        }
        return MovePlanner.bossMove(sortie, targetX, targetY);
    }

    /** Whether the bite is telling, or the open has at most {@link #BITE_EARLY} s left. */
    static boolean biting(SetPiece boss, BreachArena breach) {
        if (breach.phase() == BreachArena.Phase.BITE) {
            return true;
        }
        return breach.phase() == BreachArena.Phase.OPEN && openLeft(boss, breach) <= BITE_EARLY;
    }

    /** The open's seconds left, from its progress (its length: the breach's, an eruption's under the ship). */
    private static double openLeft(SetPiece boss, BreachArena breach) {
        var spec = boss.boss().orElseThrow();
        double length =
                breach.pose() == BreachArena.Pose.UNDER && spec.eruption().isPresent()
                        ? spec.eruption().orElseThrow().openSeconds()
                        : spec.breach().orElseThrow().openSeconds();
        return (1 - breach.progress()) * length;
    }

    /** Out of a marker: toward the stand-off point if that leads out, else straight away from the marker's centre. */
    private static int escapeToward(Sortie sortie, double targetX, double targetY, double dx, double dy) {
        double tx = targetX - sortie.ship().x();
        double ty = targetY - sortie.ship().y();
        double t = Math.hypot(tx, ty);
        if (t > 1e-6 && tx * dx + ty * dy >= 0) {
            return MovePlanner.escape(sortie, tx / t, ty / t);
        }
        double d = Math.hypot(dx, dy);
        if (d > 1e-6) {
            return MovePlanner.escape(sortie, dx / d, dy / d);
        }
        return t > 1e-6 ? MovePlanner.escape(sortie, tx / t, ty / t) : MovePlanner.escape(sortie, 0, -1);
    }
}
