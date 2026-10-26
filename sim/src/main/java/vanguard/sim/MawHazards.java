package vanguard.sim;

/**
 * M8 part H, step H2 (design/enemies/bosses/abyssal-maw, The escort; design/campaign Level 28, Rook and the escort;
 * m8h-gaps.md §1.3): where the escort's goal moves in Level 28's arena and run, shared by the Bulwark's {@link
 * BulwarkHazards} and Rook's {@link RookHazards}: inside the coil's walls (off the coil), out of a told or swinging
 * sweep's bight (its full reach, until it has swung), out of the lunge's column (its shadow, the lunge and the body
 * standing in it) and, in the run, out of a broadside's band while it is told. It also picks the Bulwark's target in
 * the fight ({@link #aim}): a gill sac while the Maw is coiled, an open implant, else the head (Level 27's aimed shell
 * generalised). Made once per escort; allocates nothing per step. After {@link #clear} the goal is ({@link #x()},
 * {@link #y()}).
 */
final class MawHazards {
    /** It keeps this far outside a hazard's reach (its hull's edge to it), px. */
    static final double GAP = 8;
    /** Passes over the hazards (a move clear of one may land in another). */
    private static final int PASSES = 3;

    private final double half;
    private final double limit;
    private final double[] band = new double[2];
    private double gx;
    private double gy;
    private double aimX;
    private double aimY;

    /**
     * @param half its hull's larger half size, px
     * @param fieldLimit its centre's margin inside the play field's edges
     */
    MawHazards(double half, double fieldLimit) {
        this.half = half;
        limit = fieldLimit;
    }

    double x() {
        return gx;
    }

    double y() {
        return gy;
    }

    /** The target {@link #aim} found. */
    double aimX() {
        return aimX;
    }

    double aimY() {
        return aimY;
    }

    /**
     * Moves the goal (goalX, goalY) clear of the coiled boss {@code coil} (null: none; nothing once it is destroyed)
     * and of the told broadside bands of {@code cruisers} (null: none); returns whether it moved.
     */
    boolean clear(double goalX, double goalY, CoilArena coil, CruiserRun cruisers) {
        gx = goalX;
        gy = goalY;
        boolean moved = false;
        for (int pass = 0; pass < PASSES; pass++) {
            boolean shifted = false;
            if (coil != null) {
                shifted |= offTheCoil(coil);
                shifted |= outOfBight(coil);
                shifted |= outOfColumn(coil);
            }
            if (cruisers != null) {
                shifted |= outOfBand(cruisers);
            }
            moved |= shifted;
            if (!shifted) {
                break;
            }
        }
        return moved;
    }

    /** Inside the coil's inner flanks (with the gap) while its walls stand. */
    private boolean offTheCoil(CoilArena coil) {
        if (!coil.walled()) {
            return false;
        }
        double x = Math.clamp(gx, coil.innerLeft() + GAP, coil.innerRight() - GAP);
        double y = Math.clamp(gy, coil.innerBottom() + GAP, coil.innerTop() - GAP);
        return moveTo(x, y);
    }

    /** Out of a told or swinging bight's full reach: straight in from its run, as far as the bight reaches there. */
    private boolean outOfBight(CoilArena coil) {
        if (!coil.sweepThreat()) {
            return false;
        }
        CoilSpec.Side side = coil.sweepSide();
        boolean bottom = side == CoilSpec.Side.BOTTOM;
        double reach = coil.bightReach(bottom ? gx : gy, half);
        if (!(reach > 0)) {
            return false;
        }
        double clear = reach + half + GAP;
        return switch (side) {
            case LEFT -> gx < coil.innerLeft() + clear && moveTo(coil.innerLeft() + clear, gy);
            case RIGHT -> gx > coil.innerRight() - clear && moveTo(coil.innerRight() - clear, gy);
            case BOTTOM -> gy < coil.innerBottom() + clear && moveTo(gx, coil.innerBottom() + clear);
        };
    }

    /** Out of the lunge's column: to the side the goal is on (the other side when the field's margins leave no room). */
    private boolean outOfColumn(CoilArena coil) {
        if (!coil.columnThreat()) {
            return false;
        }
        double clear = coil.columnWidth() / 2 + coil.clearance() + half + GAP;
        double dx = gx - coil.columnX();
        if (Math.abs(dx) >= clear) {
            return false;
        }
        double side = dx >= 0 ? 1 : -1;
        double x = coil.columnX() + side * (clear + 1);
        if (x < limit || x > PlayField.WIDTH - limit) {
            x = coil.columnX() - side * (clear + 1);
        }
        return moveTo(x, gy);
    }

    /** Out of a told broadside band: to its nearer edge (the other one when the field's margins leave no room). */
    private boolean outOfBand(CruiserRun cruisers) {
        if (!cruisers.inToldBand(gy, half, GAP, band)) {
            return false;
        }
        double y = band[0] >= limit && band[0] <= PlayField.HEIGHT - limit ? band[0] : band[1];
        return moveTo(gx, y);
    }

    /** The goal to (x, y), clamped into the field's margins; whether it moved. */
    private boolean moveTo(double x, double y) {
        double nx = Math.clamp(x, limit, PlayField.WIDTH - limit);
        double ny = Math.clamp(y, limit, PlayField.HEIGHT - limit);
        if (nx == gx && ny == gy) {
            return false;
        }
        gx = nx;
        gy = ny;
        return true;
    }

    /**
     * The escort's target in the coiled boss's fight ({@code piece} with its {@code coil}): while it is coiled (its
     * first phase) the living gill sac nearest (fromX, fromY); else an open implant; else the head while it is on the
     * play plane. Returns whether there is one (then at ({@link #aimX()}, {@link #aimY()})).
     */
    boolean aim(SetPiece piece, CoilArena coil, double fromX, double fromY) {
        if (!piece.present() || piece.destroyed() || !coil.up()) {
            return false;
        }
        if (piece.phase() == 0) {
            double best = Double.POSITIVE_INFINITY;
            for (int k = 0; k < coil.sacCount(); k++) {
                int p = coil.sacPart(k);
                if (piece.partWrecked(p)) {
                    continue;
                }
                double dx = piece.partX(p) - fromX;
                double dy = piece.partY(p) - fromY;
                double distance = dx * dx + dy * dy;
                if (distance < best) {
                    best = distance;
                    aimX = piece.partX(p);
                    aimY = piece.partY(p);
                }
            }
            if (best < Double.POSITIVE_INFINITY) {
                return true;
            }
        }
        for (int i = 0; i < coil.implantCount(); i++) {
            if (coil.implantOpen(i)) {
                aimX = coil.implantX(i);
                aimY = coil.implantY(i);
                return true;
            }
        }
        if (coil.headOnPlane()) {
            aimX = coil.headX();
            aimY = coil.headY();
            return true;
        }
        return false;
    }
}
