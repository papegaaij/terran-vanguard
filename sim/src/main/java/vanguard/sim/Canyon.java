package vanguard.sim;

/**
 * M7 part C: a level's {@link Terrain} and the ground's scroll now, which the sortie sets every step; shared
 * by what rock stops, clamps or steers (the player's fire, the enemies, the wingman). Derived from the script
 * and the scroll, so nothing of it is hashed.
 */
final class Canyon {
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

    /** {@link Terrain#flyableX} now. */
    double flyableX(double y, double x, double margin) {
        return terrain.flyableX(scroll, y, x, margin);
    }
}
