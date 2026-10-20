package vanguard.content;

import vanguard.sim.BossSpec;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;
import vanguard.sim.StrideStrikes;

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
    /** Round 44: striding down at the ship from up to this far above it, px, ... */
    static final double SIDESTEP_AHEAD = 320;
    /** Round 44: ... it steps aside to this far outside the stomp's ring, px, ... */
    static final double SIDESTEP_CLEAR = 50;
    /** Round 44: ... and keeps there while within this far outside it. */
    static final double SIDESTEP_KEEP = 80;
    /** Round 44: on a horizontal pass it lines up this far behind the hull's centre, px. */
    static final double FLANK_BACK = 36;
    /** Round 44: it keeps this far off the rail lance's line while it charges or fires (its hull and some), px. */
    static final double LANCE_CLEAR = 36;
    /** Round 44: ... and this far outside the stomp's ring while it braces or expands, px. */
    static final double STOMP_CLEAR = 26;

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
            if (walker.nextEdge() == vanguard.sim.WaveSpec.Edge.BOTTOM
                    && walker.strikes().hasStomp()
                    && boss.phase() > 0) {
                // Round 44: low for a bottom entry, but between two lanes from phase 2 on: it would step up into the
                // ship's lane and
                // stomp it from below, driving it ahead of its plate.
                targetX = laneGap(boss, shipX);
            }
        } else {
            double hy = -Math.cos(walker.headingRadians());
            int part = walkerTarget(boss, shipX, hy <= -0.5 && shipY < boss.partY(Autopilot.vitalPart(boss)));
            double partX = boss.partX(part);
            double partY = boss.partY(part);
            targetX = partX;
            int hull = Autopilot.vitalPart(boss);
            if (part != hull && Math.abs(hy) >= 0.5) {
                // A pod on a vertical pass: its outer edge, clear of the plate's and the hull's width.
                targetX += Math.signum(partX - boss.partX(hull)) * POD_OUTSIDE;
            }
            if (Math.abs(hy) < 0.5) {
                // A horizontal pass: its flank from below.
                targetY = partY - FLANK_BELOW;
                if (part == hull) {
                    // Round 44: on its back half, past the lower pod (armoured while shut, it stops the shots) and
                    // the plate.
                    targetX -= -Math.sin(walker.headingRadians()) * FLANK_BACK;
                }
            } else if (hy < 0 && part != hull) {
                // Striding down in phase 1 (round 44: or at the ship below it later): a pod from below, outside the
                // plate.
                targetY = Math.min(Autopilot.CRUISE_Y, partY - POD_BELOW);
            } else {
                // Behind it, just outside the cone, lined up on the part: above it (rear gun) or below it (front guns).
                double ay = cone == null ? boss.renderY(1) : walker.coneY();
                targetY = ay - hy * ((cone == null ? 150 : cone.length()) + CONE_CLEAR);
                double below = boss.partY(hull) - shipY;
                double lateral = shipX - boss.partX(hull);
                double reach = walker.strikes().hasStomp() ? walker.strikes().stompRadius() : 0;
                if (part == hull
                        && reach > 0
                        && hy < -0.5
                        && below > 0
                        && below < SIDESTEP_AHEAD
                        && Math.abs(lateral) < reach + SIDESTEP_KEEP) {
                    // Round 44: striding down at the ship below it, it steps aside out of the stomp's ring and lets
                    // it pass (no more flying over it to get behind it), holding its height; behind it once past.
                    double side = lateral >= 0 ? 1 : -1;
                    double aside = boss.partX(hull) + side * (reach + SIDESTEP_CLEAR);
                    if (aside < margin || aside > PlayField.WIDTH - margin) {
                        side = -side;
                        aside = boss.partX(hull) + side * (reach + SIDESTEP_CLEAR);
                    }
                    targetX = aside;
                    targetY = shipY;
                }
            }
        }
        targetX = Math.clamp(targetX, margin, PlayField.WIDTH - margin);
        targetY = Math.clamp(targetY, margin, PlayField.HEIGHT - margin);
        if (coneDanger(boss, shipX, shipY, CONE_INNER)) {
            return coneEscape(sortie, boss);
        }
        StrideStrikes strikes = walker.strikes();
        boolean stomp = inStomp(strikes, shipX, shipY);
        boolean lance = onLance(strikes, shipX, shipY);
        if (stomp || lance) {
            // Round 44: out of the stomp's ring (straight away from its centre) and off the lance's line (across it).
            double awayX =
                    (stomp ? stompOutX(strikes, shipX, shipY) : 0) + (lance ? lanceOutX(strikes, shipX, shipY) : 0);
            double awayY =
                    (stomp ? stompOutY(strikes, shipX, shipY) : 0) + (lance ? lanceOutY(strikes, shipX, shipY) : 0);
            return MovePlanner.escape(sortie, awayX, awayY);
        }
        if (strikes.lancePhase() != StrideStrikes.LancePhase.IDLE) {
            // Round 44: its goal off the lance's line, on the ship's side of it.
            double ux = strikes.lanceDirX();
            double uy = strikes.lanceDirY();
            double lateral = (targetX - strikes.lanceX()) * uy - (targetY - strikes.lanceY()) * ux;
            double side = (shipX - strikes.lanceX()) * uy - (shipY - strikes.lanceY()) * ux >= 0 ? 1 : -1;
            double reach = strikes.lanceWidth() / 2 + LANCE_CLEAR + 4;
            if (lateral * side < reach) {
                double shift = reach - lateral * side;
                targetX = Math.clamp(targetX + uy * side * shift, margin, PlayField.WIDTH - margin);
                targetY = Math.clamp(targetY - ux * side * shift, margin, PlayField.HEIGHT - margin);
            }
        }
        return MovePlanner.bossMove(sortie, targetX, targetY);
    }

    /** Round 44: the midpoint between two of its vertical lanes nearest {@code x} ({@code x} with one lane). */
    static double laneGap(SetPiece boss, double x) {
        double[] lanes = boss.boss().orElseThrow().stride().orElseThrow().laneXs();
        double best = x;
        double off = Double.POSITIVE_INFINITY;
        for (int i = 1; i < lanes.length; i++) {
            double gap = (lanes[i - 1] + lanes[i]) / 2;
            if (Math.abs(gap - x) < off) {
                off = Math.abs(gap - x);
                best = gap;
            }
        }
        return best;
    }

    /** Round 44: whether the stomp braces or expands with (x, y) inside its ring and {@link #STOMP_CLEAR}. */
    static boolean inStomp(StrideStrikes strikes, double x, double y) {
        if (strikes.stompPhase() == StrideStrikes.StompPhase.IDLE) {
            return false;
        }
        double clear = strikes.stompRadius() + STOMP_CLEAR;
        double dx = x - strikes.ringX();
        double dy = y - strikes.ringY();
        return dx * dx + dy * dy < clear * clear;
    }

    /** Round 44: the way out of the stomp's ring from (x, y), x: straight away from its centre. */
    private static double stompOutX(StrideStrikes strikes, double x, double y) {
        double d = Math.hypot(x - strikes.ringX(), y - strikes.ringY());
        return d > 1e-6 ? (x - strikes.ringX()) / d : 0;
    }

    /** ... y. */
    private static double stompOutY(StrideStrikes strikes, double x, double y) {
        double d = Math.hypot(x - strikes.ringX(), y - strikes.ringY());
        return d > 1e-6 ? (y - strikes.ringY()) / d : -1;
    }

    /** Round 44: whether the rail lance charges or fires with (x, y) within {@link #LANCE_CLEAR} of its line. */
    static boolean onLance(StrideStrikes strikes, double x, double y) {
        if (strikes.lancePhase() == StrideStrikes.LancePhase.IDLE) {
            return false;
        }
        double ux = strikes.lanceDirX();
        double uy = strikes.lanceDirY();
        double dx = x - strikes.lanceX();
        double dy = y - strikes.lanceY();
        double clear = strikes.lanceWidth() / 2 + LANCE_CLEAR;
        return dx * ux + dy * uy >= -clear && Math.abs(dx * uy - dy * ux) < clear;
    }

    /**
     * Round 44: the side of the lance's line the ship leaves to: its own (+1 left of the line's way, −1 right), the other
     * at the field's edge.
     */
    private static double lanceSide(StrideStrikes strikes, double x, double y) {
        double ux = strikes.lanceDirX();
        double uy = strikes.lanceDirY();
        double lateral = (x - strikes.lanceX()) * uy - (y - strikes.lanceY()) * ux;
        double side = lateral >= 0 ? 1 : -1;
        if (x < 60 && uy * side < 0 || x > PlayField.WIDTH - 60 && uy * side > 0) {
            side = -side;
        }
        return side;
    }

    /** Round 44: the way off the lance's line, x. */
    private static double lanceOutX(StrideStrikes strikes, double x, double y) {
        return strikes.lanceDirY() * lanceSide(strikes, x, y);
    }

    /** ... y. */
    private static double lanceOutY(StrideStrikes strikes, double x, double y) {
        return -strikes.lanceDirX() * lanceSide(strikes, x, y);
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

    /**
     * M7 part F: the part to shoot: in phase 1 (round 44: or later while it strides down at the ship below it, {@code
     * atShip}: its plate turns the hull's shots) a living pod whose hatch is open (round 44: a shut pod is armoured),
     * else the living pod nearer {@code shipX}; else the vital hull.
     */
    private static int walkerTarget(SetPiece boss, double shipX, boolean atShip) {
        int hull = Autopilot.vitalPart(boss);
        int best = hull;
        double hy = -Math.cos(boss.stride().orElseThrow().headingRadians());
        if (Math.abs(hy) < 0.5) {
            // Round 44: a horizontal pass: the pod below the hull, in the line of its shots from below, while open.
            for (int p = 0; p < boss.partCount(); p++) {
                if (p != hull && !boss.partWrecked(p) && boss.partHatchOpen(p) && boss.partY(p) < boss.partY(hull)) {
                    return p;
                }
            }
        }
        if (boss.phase() == 0 || atShip) {
            for (int p = 0; p < boss.partCount(); p++) {
                if (p != hull && !boss.partWrecked(p) && boss.partHatchOpen(p)) {
                    return p;
                }
            }
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
