package vanguard.sim;

/**
 * M7 part C: a level's {@link Terrain} and the ground's scroll now, which the sortie sets every step; shared
 * by what rock stops, clamps or steers (the player's fire, the enemies, the wingman). Derived from the script
 * and the scroll, so nothing of it is hashed.
 */
final class Canyon {
    /** M8 AW: {@link #inSpan}'s most push off the rock in a step, px, and its centre's least gap to the rock, px. */
    static final double PUSH = 2;

    static final double EDGE = 2;

    final Terrain terrain;
    private double scroll;

    Canyon(Terrain terrain) {
        this.terrain = terrain;
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
        return terrain.rock(scroll, x, y, halfW, halfH);
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
        double left = terrain.spanLeft(scroll, y, fromX);
        double right = terrain.spanRight(scroll, y, fromX);
        double out = x;
        if (!Double.isNaN(left) && !Double.isNaN(right) && right - left >= 2 * half) {
            if (left > 0 && out < left + half) {
                out = Math.min(left + half, Math.max(out, fromX + PUSH));
            }
            if (right < PlayField.WIDTH && out > right - half) {
                out = Math.max(right - half, Math.min(out, fromX - PUSH));
            }
        }
        return terrain.flyableX(scroll, y, out, EDGE);
    }

    /** {@link Terrain#flyableX} now. */
    double flyableX(double y, double x, double margin) {
        return terrain.flyableX(scroll, y, x, margin);
    }
}
