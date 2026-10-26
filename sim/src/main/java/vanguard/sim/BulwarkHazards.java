package vanguard.sim;

/**
 * M8 part G (design/player/wingmen, Heavy drone "Bulwark"; design/campaign Level 27, Escort; m8g-gaps.md §1.3, §5 G7):
 * where the Bulwark's goal moves to keep out of the heat plumes and the torpedoes, as Rook's {@link RookHazards} keep
 * him out of the vents: out of a plume's disc while it tells or burns (radially, to its rim plus its half size and a
 * gap), and off a homing missile's line (its path over the next {@link #LOOK_SECONDS}, {@link #LINE} px either side
 * plus its half size; across it, to the side the goal is on, the other side when the play field's margins leave no
 * room). M8 part H, step H2: then clear of the Maw's coil, bights and lunge column and of the cruisers' told bands
 * ({@link MawHazards}). Made once; allocates nothing per step. After {@link #clear} the goal is ({@link #x()}, {@link
 * #y()}).
 */
final class BulwarkHazards {
    /** It keeps this far outside a plume's rim (its hull's edge to it), px. */
    static final double GAP = 8;
    /** A torpedo's line: it keeps this far off the path, its hull's edge to it, px. */
    static final double LINE = 24;
    /** How far ahead a torpedo's path is looked at, s. */
    static final double LOOK_SECONDS = 1;
    /** Passes over the hazards (a move clear of one may land in another). */
    private static final int PASSES = 3;

    private final double half;
    private final double limit;
    /** M8 part H: the coiled boss's and the cruisers' hazards. */
    private final MawHazards maw;

    private double gx;
    private double gy;

    /**
     * @param hitbox its hit box (its half size is the larger half)
     * @param fieldLimit its centre's margin inside the play field's edges
     */
    BulwarkHazards(Hitbox hitbox, double fieldLimit) {
        half = Math.max(hitbox.width(), hitbox.height()) / 2;
        limit = fieldLimit;
        maw = new MawHazards(half, fieldLimit);
    }

    double x() {
        return gx;
    }

    double y() {
        return gy;
    }

    /** M8 part H: the Maw's and the cruisers' hazards (and its target in the Maw's fight, {@link MawHazards#aim}). */
    MawHazards maw() {
        return maw;
    }

    /**
     * Moves the goal (goalX, goalY) out of every telling or burning plume disc of {@code vents} (null: none) and off
     * every line of {@code missiles} (null: none); M8 part H: then clear of the coiled boss {@code coil} and the told
     * bands of {@code cruisers} (null: none; {@link MawHazards}). Returns whether it moved.
     */
    boolean clear(double goalX, double goalY, FurnaceVents vents, Missiles missiles) {
        return clear(goalX, goalY, vents, missiles, null, null);
    }

    /** As {@link #clear(double, double, FurnaceVents, Missiles)}, then clear of {@code coil} and {@code cruisers}. */
    boolean clear(
            double goalX, double goalY, FurnaceVents vents, Missiles missiles, CoilArena coil, CruiserRun cruisers) {
        gx = goalX;
        gy = goalY;
        boolean moved = false;
        for (int pass = 0; pass < PASSES; pass++) {
            boolean shifted = false;
            if (vents != null) {
                shifted |= clearOfPlumes(vents);
            }
            if (missiles != null) {
                shifted |= clearOfLines(missiles);
            }
            moved |= shifted;
            if (!shifted) {
                break;
            }
        }
        if ((coil != null || cruisers != null) && maw.clear(gx, gy, coil, cruisers)) {
            gx = maw.x();
            gy = maw.y();
            moved = true;
        }
        return moved;
    }

    /** Out of every hot plume disc: straight away from its centre to its rim plus its half size and the gap. */
    private boolean clearOfPlumes(FurnaceVents vents) {
        boolean moved = false;
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.disc(g) || !vents.present(g) || !vents.hot(g)) {
                continue;
            }
            double reach = vents.radius(g) + half + GAP;
            double dx = gx - vents.x(g);
            double dy = gy - vents.y(g);
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance >= reach) {
                continue;
            }
            // Straight out; from the very centre, down the screen (toward the ship's usual lane).
            double ux = distance > 0 ? dx / distance : 0;
            double uy = distance > 0 ? dy / distance : -1;
            double toX = vents.x(g) + ux * reach;
            double toY = vents.y(g) + uy * reach;
            if (!inField(toX, toY)) {
                toX = vents.x(g) - ux * reach;
                toY = vents.y(g) - uy * reach;
            }
            gx = clampX(toX);
            gy = clampY(toY);
            moved = true;
        }
        return moved;
    }

    /** Off every missile's line over its next {@link #LOOK_SECONDS}: across it, to the goal's side of it. */
    private boolean clearOfLines(Missiles missiles) {
        boolean moved = false;
        double reach = LINE + half;
        for (int i = 0; i < missiles.size(); i++) {
            Missile missile = missiles.get(i);
            double vx = missile.vx();
            double vy = missile.vy();
            double speed = Math.sqrt(vx * vx + vy * vy);
            if (!(speed > 0)) {
                continue;
            }
            double ux = vx / speed;
            double uy = vy / speed;
            double length = speed * LOOK_SECONDS;
            double dx = gx - missile.x();
            double dy = gy - missile.y();
            double along = dx * ux + dy * uy;
            if (along < -reach || along > length + reach) {
                continue;
            }
            // Its side of the line: positive to the left of the missile's flight.
            double across = ux * dy - uy * dx;
            if (Math.abs(across) >= reach) {
                continue;
            }
            double side = across >= 0 ? 1 : -1;
            double toX = gx - uy * side * (reach - Math.abs(across));
            double toY = gy + ux * side * (reach - Math.abs(across));
            if (!inField(toX, toY)) {
                double other = reach + Math.abs(across);
                toX = gx + uy * side * other;
                toY = gy - ux * side * other;
            }
            gx = clampX(toX);
            gy = clampY(toY);
            moved = true;
        }
        return moved;
    }

    private boolean inField(double x, double y) {
        return x >= limit && x <= PlayField.WIDTH - limit && y >= limit && y <= PlayField.HEIGHT - limit;
    }

    private double clampX(double x) {
        return Math.clamp(x, limit, PlayField.WIDTH - limit);
    }

    private double clampY(double y) {
        return Math.clamp(y, limit, PlayField.HEIGHT - limit);
    }
}
