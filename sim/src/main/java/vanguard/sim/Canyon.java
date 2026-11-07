package vanguard.sim;

/**
 * M7 part C: a level's {@link Terrain} and the ground's scroll now, which the sortie sets every step; shared
 * by what rock stops, clamps or steers (the player's fire, the enemies, the wingman). Derived from the script
 * and the scroll, so nothing of it is hashed.
 *
 * <p>M11 part H, step H1a (design/enemies/bosses/choir-heart, Arena; m11h-gaps.md §4): a boss's {@link ArenaWalls} are a
 * second source of rock (screen space, moving): every query asks the terrain (when the level has one) and the walls
 * (when a boss has them), so whatever rock stops, clamps or steers meets the walls unchanged. Without walls each query
 * is the terrain's own.
 */
final class Canyon {
    /** M8 AW: {@link #inSpan}'s most push off the rock in a step, px, and its centre's least gap to the rock, px. */
    static final double PUSH = 2;

    static final double EDGE = 2;

    /** The level's terrain; null without (a boss's walls only). */
    private final Terrain terrain;
    /** M11 part H, step H1a: a boss's arena walls; null without. */
    private final ArenaWalls walls;
    /** A walls' contact's scratch (normal, depth). */
    private final double[] touch = new double[3];

    private double scroll;

    Canyon(Terrain terrain) {
        this(terrain, null);
    }

    /** The level's {@code terrain} (null: none) and a boss's {@code walls} (null: none); one at least. */
    Canyon(Terrain terrain, ArenaWalls walls) {
        if (terrain == null && walls == null) {
            throw new IllegalArgumentException("a canyon has terrain or arena walls");
        }
        this.terrain = terrain;
        this.walls = walls;
    }

    /** M11 part H, step H1a: the boss's arena walls; null without. */
    ArenaWalls walls() {
        return walls;
    }

    /** The ground has scrolled {@code groundScroll} px in this attempt. */
    void scroll(double groundScroll) {
        scroll = groundScroll;
    }

    double scroll() {
        return scroll;
    }

    /** Whether the box of half size halfW × halfH around (x, y) overlaps rock now. */
    boolean rock(double x, double y, double halfW, double halfH) {
        return (terrain != null && terrain.rock(scroll, x, y, halfW, halfH))
                || (walls != null && walls.rock(x, y, halfW, halfH));
    }

    /** {@link Terrain#ceiling} now, the walls' too. */
    double ceiling(double x, double halfW, double fromY) {
        double best = terrain == null ? Double.POSITIVE_INFINITY : terrain.ceiling(scroll, x, halfW, fromY);
        return walls == null ? best : Math.min(best, walls.ceiling(x, halfW, fromY));
    }

    /** {@link Terrain#spanLeft(double, double, double)} now, inside the walls too. */
    double spanLeft(double y, double x) {
        return spanLeft(y, x, false);
    }

    /** {@link Terrain#spanLeft(double, double, double, boolean)} now, inside the walls too. */
    double spanLeft(double y, double x, boolean withNotches) {
        double lo = terrain == null
                ? (x > 0 && x < PlayField.WIDTH ? 0 : Double.NaN)
                : terrain.spanLeft(scroll, y, x, withNotches);
        if (walls != null && !Double.isNaN(lo)) {
            double wall = walls.spanLeft(y, x);
            lo = Double.isNaN(wall) ? Double.NaN : Math.max(lo, wall);
        }
        return lo;
    }

    /** {@link Terrain#spanRight(double, double, double)} now, inside the walls too. */
    double spanRight(double y, double x) {
        return spanRight(y, x, false);
    }

    /** {@link Terrain#spanRight(double, double, double, boolean)} now, inside the walls too. */
    double spanRight(double y, double x, boolean withNotches) {
        double hi = terrain == null
                ? (x > 0 && x < PlayField.WIDTH ? PlayField.WIDTH : Double.NaN)
                : terrain.spanRight(scroll, y, x, withNotches);
        if (walls != null && !Double.isNaN(hi)) {
            double wall = walls.spanRight(y, x);
            hi = Double.isNaN(wall) ? Double.NaN : Math.min(hi, wall);
        }
        return hi;
    }

    /** {@link Terrain#channels} now (the walls split nothing). */
    int channels(double y) {
        return terrain == null ? 1 : terrain.channels(scroll, y);
    }

    /** {@link Terrain#contact} now, the walls' too: the deeper touch's normal and depth in {@code out}. */
    boolean contact(Hull hull, double x, double y, double[] out) {
        boolean any = terrain != null && terrain.contact(scroll, hull, x, y, out);
        if (!any) {
            out[0] = 0;
            out[1] = 0;
            out[2] = 0;
        }
        if (walls != null && walls.contact(hull, x, y, touch)) {
            if (!any || touch[2] > out[2]) {
                out[0] = touch[0];
                out[1] = touch[1];
                out[2] = touch[2];
            }
            any = true;
        }
        return any;
    }

    /**
     * M8 AW: where a unit flying in the water at play-field height y, wanting to go to {@code x} from {@code fromX}
     * (its last x), stays inside the flyable span it is in: its hit box of half width {@code half} is moved off the
     * walls and islands where the span holds it (the field's side edges are not walls: it may still leave through
     * them), by at most {@value #PUSH} px a step beyond {@code fromX} so a unit laid out closer than its half width
     * eases off; and its centre is kept {@value #EDGE} px clear of the rock in any case (on the rock's very edge the
     * span holding it would be lost).
     */
    double inSpan(double y, double x, double fromX, double half) {
        double left = spanLeft(y, fromX);
        double right = spanRight(y, fromX);
        double out = x;
        if (!Double.isNaN(left) && !Double.isNaN(right) && right - left >= 2 * half) {
            if (left > 0 && out < left + half) {
                out = Math.min(left + half, Math.max(out, fromX + PUSH));
            }
            if (right < PlayField.WIDTH && out > right - half) {
                out = Math.max(right - half, Math.min(out, fromX - PUSH));
            }
        }
        return flyableX(y, out, EDGE);
    }

    /** {@link Terrain#flyableX} now, clear of the walls too. */
    double flyableX(double y, double x, double margin) {
        double out = terrain == null ? x : terrain.flyableX(scroll, y, x, margin);
        return walls == null ? out : walls.flyableX(y, out, margin);
    }
}
