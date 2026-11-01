package vanguard.sim;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, Arena: the bow; m10h-gaps.md §1 #1): a boss's armoured body as
 * a <b>wedge</b> instead of its one box: two slanted faces from its top (the hull running on off the top edge) down to
 * a narrower bow tip, so the side pockets beside the bow stay open to the ship and its shots. Shots spark on it (they
 * glance, as on a body box), a beam stops on it, and it touches the ship as the body does (its contact damage at most
 * once per {@link SetPieceSpec#HIT_INTERVAL_SECONDS}), the ship pushed {@code push} px out of it, away from the face it
 * met (straight down under the tip). In the boss's own frame: px from its centre, to the right and up.
 *
 * @param topHalfWidth half its width at its top, px (the Ascendant's bow 400 px wide at the top edge: 200)
 * @param topDy its top above the centre, px up
 * @param tipHalfWidth half the bow tip's width, px (100 px wide: 50)
 * @param tipDy the tip's height from the centre, px up (below the centre: negative)
 * @param push how far a touch pushes the ship out, px
 */
public record WedgeBody(double topHalfWidth, double topDy, double tipHalfWidth, double tipDy, double push) {
    public WedgeBody {
        if (!(tipHalfWidth > 0) || !(topHalfWidth >= tipHalfWidth) || !(topDy > tipDy) || !(push >= 0)) {
            throw new IllegalArgumentException("a wedge widens from its tip up to its top and pushes 0 px or more");
        }
    }

    /** Its half width at {@code dy} px up from the centre (the tip's below it, the top's above it). */
    double halfWidthAt(double dy) {
        double share = Math.clamp((dy - tipDy) / (topDy - tipDy), 0, 1);
        return tipHalfWidth + (topHalfWidth - tipHalfWidth) * share;
    }

    /**
     * Whether a box {@code halfW} × {@code halfH} (half sizes) centred (dx, dy) from the centre overlaps it (touching
     * edges do not count, as for {@link Hitbox#overlaps}). It widens upward, so the box's highest row inside it is where
     * it is widest under the box.
     */
    boolean touches(double halfW, double halfH, double dx, double dy) {
        if (!(dy + halfH > tipDy) || !(dy - halfH < topDy)) {
            return false;
        }
        return Math.abs(dx) < halfWidthAt(Math.min(dy + halfH, topDy)) + halfW;
    }

    /**
     * Where a beam's column centred {@code dx} from the centre, {@code half} either side and running up from {@code
     * fromDy}, first touches it (px up from the centre): the lowest point of the wedge inside the column, or the column's
     * start when the wedge reaches below it; {@link Double#POSITIVE_INFINITY} when the column misses it.
     */
    double column(double dx, double half, double fromDy) {
        double nearest = Math.max(0, Math.abs(dx) - half);
        if (!(nearest < topHalfWidth) || topDy <= fromDy) {
            return Double.POSITIVE_INFINITY;
        }
        double low = nearest <= tipHalfWidth
                ? tipDy
                : tipDy + (nearest - tipHalfWidth) / (topHalfWidth - tipHalfWidth) * (topDy - tipDy);
        return Math.max(fromDy, low);
    }

    /**
     * The x of the unit push out of it for the ship {@code dx} from the centre: straight down under the tip, else away
     * from the face on its side (outward and down along the face's normal).
     */
    double pushX(double dx) {
        if (Math.abs(dx) <= tipHalfWidth) {
            return 0;
        }
        double across = topHalfWidth - tipHalfWidth;
        double up = topDy - tipDy;
        return Math.signum(dx) * up / Math.hypot(across, up);
    }

    /** As {@link #pushX}, its y (y up). */
    double pushY(double dx) {
        if (Math.abs(dx) <= tipHalfWidth) {
            return -1;
        }
        double across = topHalfWidth - tipHalfWidth;
        double up = topDy - tipDy;
        return -across / Math.hypot(across, up);
    }
}
