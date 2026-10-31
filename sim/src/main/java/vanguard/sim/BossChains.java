package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * A boss's chains (design/enemies/bosses: the necks its parts hang on, M5 part E the slam arms), kept by its {@link
 * SetPiece} in the style of the {@link BreachArena}: per chain its pieces' angles (0 at the anchor; its segments,
 * then its tip; radians counter-clockwise), its segments' and its tip's offsets from the boss's centre, and which
 * part hangs on which chain. A neck bends toward the ship; a slam arm lies along its lane while its arena lays it
 * out. Only the angles are state (hashed); the offsets follow from them.
 */
final class BossChains {
    private final SetPiece piece;
    /** The boss; null for a set piece flying passes (it then has no chains). */
    private final BossSpec boss;

    private final List<PartSpec> parts;
    /** Per part, the chain it hangs on; -1 for none. */
    private final int[] partChain;
    /** Per chain, its first piece's index in {@link #angles} and its first segment's in the segment arrays. */
    private final int[] chainStart;

    private final int[] segmentStart;
    private final double[] angles;
    private final double[] segmentX;
    private final double[] segmentY;
    private final double[] tipX;
    private final double[] tipY;

    BossChains(SetPiece piece, BossSpec boss, List<PartSpec> parts) {
        this.piece = piece;
        this.boss = boss;
        this.parts = parts;
        partChain = new int[parts.size()];
        Arrays.fill(partChain, -1);
        int chains = boss == null ? 0 : boss.chains().size();
        chainStart = new int[chains];
        segmentStart = new int[chains];
        int angleCount = 0;
        int segmentCount = 0;
        for (int c = 0; c < chains; c++) {
            BossSpec.Chain chain = boss.chains().get(c);
            partChain[chain.part()] = c;
            chainStart[c] = angleCount;
            segmentStart[c] = segmentCount;
            angleCount += chain.segments() + 1;
            segmentCount += chain.segments();
        }
        angles = new double[angleCount];
        segmentX = new double[segmentCount];
        segmentY = new double[segmentCount];
        tipX = new double[chains];
        tipY = new double[chains];
    }

    /** Back to the level start: every chain along its rest line. */
    void reset() {
        Arrays.fill(angles, 0);
    }

    /**
     * The necks turn toward the ship at (shipX, shipY): each chain's anchor piece eases toward the angle to the ship
     * (at most its bend, {@code phaseBend} in an enraged phase; NaN for the chain's own), every later piece toward
     * the one before it, {@code lag} s late; a chain whose part is destroyed goes limp toward its rest.
     */
    void bend(double shipX, double shipY, double phaseBend) {
        double x = piece.x();
        double y = piece.y();
        for (int c = 0; c < chainStart.length; c++) {
            BossSpec.Chain chain = boss.chains().get(c);
            if (chain.slam()) {
                continue;
            }
            PartSpec end = parts.get(chain.part());
            double restX = end.dx() - chain.fromDx();
            double restY = end.dy() - chain.fromDy();
            double target = 0;
            if (piece.partHp(chain.part()) > 0) {
                double toX = shipX - (x + chain.fromDx());
                double toY = shipY - (y + chain.fromDy());
                double bend = Double.isNaN(phaseBend) ? chain.bendRadians() : phaseBend;
                target =
                        Math.clamp(StrictMath.atan2(restX * toY - restY * toX, restX * toX + restY * toY), -bend, bend);
            }
            double ease = Math.min(1, SimStep.SECONDS / chain.lagSeconds());
            int first = chainStart[c];
            angles[first] += (target - angles[first]) * ease;
            for (int k = 1; k <= chain.segments(); k++) {
                angles[first + k] += (angles[first + k - 1] - angles[first + k]) * ease;
            }
        }
        place();
    }

    /** The chains' segment and tip offsets from their angles: each piece is the rest line's share, turned. */
    void place() {
        SlamArena arena = piece.slamArena();
        for (int c = 0; c < chainStart.length; c++) {
            BossSpec.Chain chain = boss.chains().get(c);
            PartSpec end = parts.get(chain.part());
            if (chain.slam() && arena != null && arena.chainArm(c) >= 0 && arena.armLane(arena.chainArm(c)) > 0) {
                laySlamChain(arena, c, chain, arena.armLane(arena.chainArm(c)));
                continue;
            }
            int pieces = chain.segments() + 1;
            double stepX = (end.dx() - chain.fromDx()) / pieces;
            double stepY = (end.dy() - chain.fromDy()) / pieces;
            double px = chain.fromDx();
            double py = chain.fromDy();
            for (int k = 0; k < pieces; k++) {
                double angle = angles[chainStart[c] + k];
                double cosA = Trig.cos(angle);
                double sinA = Trig.sin(angle);
                px += stepX * cosA - stepY * sinA;
                py += stepX * sinA + stepY * cosA;
                if (k < chain.segments()) {
                    segmentX[segmentStart[c] + k] = px;
                    segmentY[segmentStart[c] + k] = py;
                }
            }
            tipX[c] = px;
            tipY[c] = py;
        }
    }

    /**
     * M5 part E: a slam chain laid along lane {@code lane}: its base just below the lanes' top edge, its tip {@link
     * SlamArena#TIP_MARGIN} above the bottom edge, its segments evenly between them from the base.
     */
    private void laySlamChain(SlamArena arena, int c, BossSpec.Chain chain, int lane) {
        double x = piece.x();
        double y = piece.y();
        double laneX = arena.laneWidth() * (lane - 0.5) - x;
        // Round 40: a rooted arm lies from its root's lower edge down.
        double top = (arena.rooted() ? arena.armTop(arena.chainArm(c)) : arena.laneTop()) - y;
        double bottom = SlamArena.TIP_MARGIN - y;
        int n = chain.segments();
        for (int k = 0; k < n; k++) {
            segmentX[segmentStart[c] + k] = laneX;
            segmentY[segmentStart[c] + k] = top + (bottom - top) * k / n;
        }
        tipX[c] = laneX;
        tipY[c] = bottom;
    }

    /** Whether a shot of {@code size} at (sx, sy) touches one of its armoured neck segments (not a slam arm's). */
    boolean neckTouches(Hitbox size, double sx, double sy) {
        double x = piece.x();
        double y = piece.y();
        for (int c = 0; c < chainStart.length; c++) {
            if (boss.chains().get(c).slam()) {
                continue;
            }
            Hitbox box = boss.chains().get(c).box();
            for (int k = 0; k < boss.chains().get(c).segments(); k++) {
                int i = segmentStart[c] + k;
                if (size.overlaps(sx, sy, box, x + segmentX[i], y + segmentY[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * M7 part A: where a beam's column (centred on {@code cx}, {@code halfWidth} either side, running up from {@code
     * fromY}) first touches one of its armoured neck segments (not a slam arm's); {@link Double#POSITIVE_INFINITY}
     * for none.
     */
    double neckColumn(double cx, double halfWidth, double fromY) {
        double nearest = Double.POSITIVE_INFINITY;
        for (int c = 0; c < chainStart.length; c++) {
            if (boss.chains().get(c).slam()) {
                continue;
            }
            nearest = segmentsColumn(c, cx, halfWidth, fromY, nearest);
        }
        return nearest;
    }

    /** The nearer of {@code nearest} and where a beam's column first touches one of chain {@code c}'s segments. */
    double segmentsColumn(int c, double cx, double halfWidth, double fromY, double nearest) {
        double x = piece.x();
        double y = piece.y();
        Hitbox box = boss.chains().get(c).box();
        for (int k = 0; k < boss.chains().get(c).segments(); k++) {
            int i = segmentStart[c] + k;
            nearest = Math.min(
                    nearest, ShotGeometry.columnContact(cx, halfWidth, fromY, x + segmentX[i], y + segmentY[i], box));
        }
        return nearest;
    }

    /** Whether a shot of {@code size} at (sx, sy) touches one of chain {@code c}'s segments. */
    boolean segmentsTouch(int c, Hitbox size, double sx, double sy) {
        double x = piece.x();
        double y = piece.y();
        Hitbox box = boss.chains().get(c).box();
        for (int k = 0; k < boss.chains().get(c).segments(); k++) {
            int i = segmentStart[c] + k;
            if (size.overlaps(sx, sy, box, x + segmentX[i], y + segmentY[i])) {
                return true;
            }
        }
        return false;
    }

    /** Whether a blast of {@code radius} at (bx, by) reaches one of chain {@code c}'s segments. */
    boolean segmentsInBlast(int c, double bx, double by, double radius) {
        double x = piece.x();
        double y = piece.y();
        Hitbox box = boss.chains().get(c).box();
        for (int k = 0; k < boss.chains().get(c).segments(); k++) {
            int i = segmentStart[c] + k;
            if (ShotGeometry.inBlast(bx, by, radius, x + segmentX[i], y + segmentY[i], box)) {
                return true;
            }
        }
        return false;
    }

    /** Whether one of chain {@code c}'s segments overlaps the play field. */
    boolean segmentsOnField(int c) {
        double x = piece.x();
        double y = piece.y();
        Hitbox box = boss.chains().get(c).box();
        for (int k = 0; k < boss.chains().get(c).segments(); k++) {
            int i = segmentStart[c] + k;
            if (PlayField.overlaps(x + segmentX[i], y + segmentY[i], box)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The slam chain whose arm part {@code p} is, for its segments' hit boxes; -1 for any other part (round 40: and
     * for a rooted arm's root while its arm is not out of the water along its lane).
     */
    int slamChain(int p) {
        int c = partChain[p];
        if (c < 0 || !boss.chains().get(c).slam()) {
            return -1;
        }
        SlamArena arena = piece.slamArena();
        if (arena != null && arena.rooted() && (arena.chainArm(c) < 0 || !arena.armOut(arena.chainArm(c)))) {
            return -1;
        }
        return c;
    }

    /** Whether part {@code p} hangs at its chain's tip (not a rooted arm's root, which stays at its offset). */
    boolean atTip(int p) {
        return partChain[p] >= 0 && !rootPart(p);
    }

    /** Round 40: whether part {@code p} is a rooted arm's root: it stays at its offset, not at its chain's tip. */
    private boolean rootPart(int p) {
        SlamArena arena = piece.slamArena();
        return arena != null
                && arena.rooted()
                && partChain[p] >= 0
                && boss.chains().get(partChain[p]).slam();
    }

    /** Part {@code p}'s chain's tip offset from the boss's centre, px to the right (as {@link #atTip}). */
    double tipX(int p) {
        return tipX[partChain[p]];
    }

    /** Part {@code p}'s chain's tip offset from the boss's centre, px up. */
    double tipY(int p) {
        return tipY[partChain[p]];
    }

    /** Its neck segments, all chains in order. */
    int segmentCount() {
        return segmentX.length;
    }

    /** Neck segment {@code i}'s centre offset from the boss's centre, px to the right. */
    double segmentOffsetX(int i) {
        return segmentX[i];
    }

    /** Neck segment {@code i}'s centre offset from the boss's centre, px up. */
    double segmentOffsetY(int i) {
        return segmentY[i];
    }

    /** The chain neck segment {@code i} belongs to. */
    int segmentChain(int i) {
        int c = 0;
        while (c + 1 < segmentStart.length && segmentStart[c + 1] <= i) {
            c++;
        }
        return c;
    }

    /** Chain {@code c}'s angle at its piece {@code k}. */
    double angle(int c, int k) {
        return angles[chainStart[c] + k];
    }

    /** Its state (the angles), as the boss's hash has always added it. */
    void addTo(StateHash hash) {
        for (double angle : angles) {
            hash.add(angle);
        }
    }
}
