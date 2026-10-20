package vanguard.content;

import vanguard.sim.BossSpec;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;

/**
 * The autopilot against the Revenant Walker (M7 part F, Level 19, see {@link Autopilot}): behind it but out of its
 * cone.
 */
final class WalkerPlay {
    private WalkerPlay() {}

    /** M7 part F: the present walking boss (Level 19's Revenant Walker), alive; null without one. */
    static SetPiece walkingBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && !piece.destroyed() && piece.stride().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** M7 part F: how far outside the flame cone's length it keeps behind the walker, px (inside its trigger). */
    private static final double CONE_CLEAR = 35;
    /** M7 part F: inside the cone widened by this, px, it leaves at once (the hull's half size and some). */
    static final double CONE_INNER = 32;
    /** M7 part F: while the cone tells or burns its goal stays outside the cone widened by this, px. */
    private static final double CONE_OUTER = 50;
    /** M7 part F: how far below a horizontal pass's hull it flies to shoot its flank, px. */
    private static final double FLANK_BELOW = 150;
    /** M7 part F: how far outside a pod's centre it lines up on a vertical pass, px (the pods sit at the plate's ends). */
    private static final double POD_OUTSIDE = 14;
    /** M7 part F: how far below a pod it flies while the walker strides down at it, px. */
    private static final double POD_BELOW = 110;

    /**
     * M7 part F, the Revenant Walker (design/enemies/bosses/revenant-walker, Passes: "get behind it, but not in the
     * cone"): the boss planner (the homing missiles dodged as bullets) toward where its guns reach what is open: in
     * phase 1 a living pod (the one nearer the ship; outside the plate's cover), then the hull's back. Striding down at
     * the ship the walker shows its plate: from below only the pods in phase 1, else from behind it (above, the rear
     * gun); striding up it shows its back and vents to the front guns from below; on a horizontal pass its flank from
     * below. Behind it the ship keeps just outside the cone's length (inside its trigger, so a burn that misses opens
     * the vents). While the cone tells or burns (the tell's sector widened by its swivel from phase 2 on), or sweeps
     * through an overheating turn (every way round its flamer), it leaves the cone at once if it is in it, and its goal
     * keeps clear of it. Off the screen it waits where the next pass shows it something: low for a bottom entry, at its
     * cruise otherwise.
     */
    static int walkerMove(Sortie sortie, SetPiece boss) {
        StrideArena walker = boss.stride().orElseThrow();
        BossSpec.Cone cone = boss.boss().orElseThrow().cone().orElse(null);
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        double margin = sortie.ship().spec().edgeLimit();
        double targetX;
        double targetY;
        if (walker.offScreen()) {
            targetX = Math.clamp(shipX, MovePlanner.BAND_LEFT, MovePlanner.BAND_RIGHT);
            targetY = walker.nextEdge() == vanguard.sim.WaveSpec.Edge.BOTTOM ? 50 : Autopilot.CRUISE_Y;
        } else {
            int part = walkerTarget(boss, shipX);
            double partX = boss.partX(part);
            double partY = boss.partY(part);
            double hy = -Math.cos(walker.headingRadians());
            targetX = partX;
            int hull = Autopilot.vitalPart(boss);
            if (part != hull && Math.abs(hy) >= 0.5) {
                // A pod on a vertical pass: its outer edge, clear of the plate's and the hull's width.
                targetX += Math.signum(partX - boss.partX(hull)) * POD_OUTSIDE;
            }
            if (Math.abs(hy) < 0.5) {
                // A horizontal pass: its flank from below.
                targetY = partY - FLANK_BELOW;
            } else if (hy < 0 && boss.phase() == 0 && part != hull) {
                // Striding down in phase 1: a pod from below, outside the plate.
                targetY = Math.min(Autopilot.CRUISE_Y, partY - POD_BELOW);
            } else {
                // Behind it, just outside the cone, lined up on the part: above it (rear gun) or below it (front guns).
                double ay = cone == null ? boss.renderY(1) : walker.coneY();
                targetY = ay - hy * ((cone == null ? 150 : cone.length()) + CONE_CLEAR);
            }
        }
        targetX = Math.clamp(targetX, margin, PlayField.WIDTH - margin);
        targetY = Math.clamp(targetY, margin, PlayField.HEIGHT - margin);
        if (coneDanger(boss, shipX, shipY, CONE_INNER)) {
            return coneEscape(sortie, boss);
        }
        return MovePlanner.bossMove(sortie, targetX, targetY);
    }

    /** M7 part F: whether the walker's cone tells or burns, or it sweeps its cone through an overheating turn. */
    static boolean coneActive(SetPiece boss) {
        StrideArena walker = boss.stride().orElseThrow();
        return boss.boss().orElseThrow().cone().isPresent()
                && (sweeping(walker) || walker.conePhase() != StrideArena.ConePhase.IDLE);
    }

    private static boolean sweeping(StrideArena walker) {
        return walker.gait() == StrideArena.Gait.HOLD || walker.gait() == StrideArena.Gait.TURN;
    }

    /**
     * M7 part F: the cone's half angle now: its own, widened by its swivel during a tell from phase 2 on (round 43: and
     * through a burn that tracks the ship).
     */
    private static double coneHalf(SetPiece boss) {
        BossSpec.Cone cone = boss.boss().orElseThrow().cone().orElseThrow();
        StrideArena.ConePhase phase = boss.stride().orElseThrow().conePhase();
        boolean turning = phase == StrideArena.ConePhase.TELL
                || (phase == StrideArena.ConePhase.BURN && cone.trackRadiansPerSecond() > 0);
        return Math.min(1.4, cone.halfAngleRadians() + (boss.phase() > 0 && turning ? cone.swivelRadians() : 0));
    }

    /** M7 part F: the part to shoot: in phase 1 the living pod nearer {@code shipX}, else the vital hull. */
    private static int walkerTarget(SetPiece boss, double shipX) {
        int hull = Autopilot.vitalPart(boss);
        int best = hull;
        if (boss.phase() == 0) {
            for (int p = 0; p < boss.partCount(); p++) {
                if (p != hull
                        && !boss.partWrecked(p)
                        && (best == hull || Math.abs(boss.partX(p) - shipX) < Math.abs(boss.partX(best) - shipX))) {
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * M7 part F: whether (px, py) lies within {@code margin} of the walker's cone ({@link #coneHalf} each side of its
     * axis) while it is {@link #coneActive}; while it sweeps through a turn, within its length and {@code margin} of
     * its flamer.
     */
    static boolean coneDanger(SetPiece boss, double px, double py, double margin) {
        if (!coneActive(boss)) {
            return false;
        }
        StrideArena walker = boss.stride().orElseThrow();
        BossSpec.Cone cone = boss.boss().orElseThrow().cone().orElseThrow();
        double dx = px - walker.coneX();
        double dy = py - walker.coneY();
        double reach = cone.length() + margin;
        if (dx * dx + dy * dy >= reach * reach) {
            return false;
        }
        if (sweeping(walker)) {
            return true;
        }
        double ux = -Math.sin(walker.coneHeading());
        double uy = -Math.cos(walker.coneHeading());
        double along = dx * ux + dy * uy;
        double lateral = dx * uy - dy * ux;
        return along >= -margin && Math.abs(lateral) <= Math.max(0, along) * Math.tan(coneHalf(boss)) + margin;
    }

    /** M7 part F: the way of the eight out of the cone soonest: past its tip or across its edge, whichever is nearer. */
    private static int coneEscape(Sortie sortie, SetPiece boss) {
        StrideArena walker = boss.stride().orElseThrow();
        BossSpec.Cone cone = boss.boss().orElseThrow().cone().orElseThrow();
        double dx = sortie.ship().x() - walker.coneX();
        double dy = sortie.ship().y() - walker.coneY();
        double distance = Math.hypot(dx, dy);
        double rx = distance > 1e-6 ? dx / distance : 0;
        double ry = distance > 1e-6 ? dy / distance : 1;
        if (sweeping(walker)) {
            return MovePlanner.escape(sortie, rx, ry);
        }
        double ux = -Math.sin(walker.coneHeading());
        double uy = -Math.cos(walker.coneHeading());
        double along = dx * ux + dy * uy;
        double lateral = dx * uy - dy * ux;
        double out = cone.length() + CONE_OUTER - distance;
        double across = Math.max(0, along) * Math.tan(coneHalf(boss)) + CONE_OUTER - Math.abs(lateral);
        if (out <= across) {
            return MovePlanner.escape(sortie, rx, ry);
        }
        double side = lateral >= 0 ? 1 : -1;
        return MovePlanner.escape(sortie, uy * side, -ux * side);
    }
}
