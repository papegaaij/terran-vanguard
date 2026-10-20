package vanguard.sim;

import java.util.List;

/**
 * M7 part B: the level's no-surface zones ({@link GroundSpec.NoSurface}; design/campaign Level 15) in
 * ground space, placed by the scroll at their times ({@link Sortie#scrollAt}), and the ground's scroll now,
 * which the sortie sets every step: where a Burrower may not surface. Derived from the script and the
 * scroll, so nothing of it is hashed.
 */
final class SurfaceZones {
    /** No zones (a level without them). */
    static final SurfaceZones NONE = new SurfaceZones(new double[0], new double[0], new double[0], new double[0], null);

    private final double[] left;
    private final double[] right;
    /** Per zone, the ground's scroll when its near (bottom) and far (top) rows cross the top edge, px. */
    private final double[] near;

    private final double[] far;
    /** M7 part C: the level's rock, where a Burrower never surfaces either; null without terrain. */
    private final Terrain terrain;

    private double scroll;

    private SurfaceZones(double[] left, double[] right, double[] near, double[] far, Terrain terrain) {
        this.left = left;
        this.right = right;
        this.near = near;
        this.far = far;
        this.terrain = terrain;
    }

    /** The zones of {@code script} (M7 part C: and its rock); {@link #NONE} without any. */
    static SurfaceZones of(LevelScript script) {
        List<GroundSpec.NoSurface> zones = script.ground().noSurface();
        Terrain rock =
                script.ground().terrain().map(GroundSpec.TerrainSpec::terrain).orElse(null);
        if (zones.isEmpty() && rock == null) {
            return NONE;
        }
        int n = zones.size();
        double[] left = new double[n];
        double[] right = new double[n];
        double[] near = new double[n];
        double[] far = new double[n];
        for (int i = 0; i < n; i++) {
            GroundSpec.NoSurface zone = zones.get(i);
            left[i] = zone.left();
            right[i] = zone.right();
            near[i] = Sortie.scrollAt(script, zone.from());
            far[i] = Sortie.scrollAt(script, zone.to());
        }
        return new SurfaceZones(left, right, near, far, rock);
    }

    /** Whether it places anything (zones or rock), so the sortie sets its scroll. */
    boolean active() {
        return left.length > 0 || terrain != null;
    }

    /**
     * M7 part C (design/campaign Level 16, the outflow's Burrower field): whether a spot at (x, y) lies on
     * rock now, or within {@code margin} of it across; false without terrain.
     */
    boolean rock(double x, double y, double margin) {
        return terrain != null && terrain.rock(scroll, x, y, margin, 0);
    }

    /** M7 part C: the nearest x to {@code x} at y clear of rock by {@code margin} ({@link Terrain#flyableX}); x without terrain. */
    double flyableX(double y, double x, double margin) {
        return terrain == null ? x : terrain.flyableX(scroll, y, x, margin);
    }

    /** The ground has scrolled {@code groundScroll} px in this attempt (the sortie's, every step). */
    void scroll(double groundScroll) {
        scroll = groundScroll;
    }

    int count() {
        return left.length;
    }

    /** Whether the play-field point (x, y) lies on a zone's ground now. */
    boolean blocked(double x, double y) {
        return zoneAt(x, y) >= 0;
    }

    /** The first zone whose ground the play-field point (x, y) lies on now; -1 for none. */
    int zoneAt(double x, double y) {
        double ground = groundY(y);
        for (int i = 0; i < left.length; i++) {
            if (x >= left[i] && x <= right[i] && ground >= near[i] && ground <= far[i]) {
                return i;
            }
        }
        return -1;
    }

    /** Zone {@code i}'s left edge, px. */
    double left(int i) {
        return left[i];
    }

    /** Zone {@code i}'s right edge, px. */
    double right(int i) {
        return right[i];
    }

    /** Zone {@code i}'s near (bottom) row on the play field now, px from the bottom edge. */
    double bottom(int i) {
        return near[i] - scroll + PlayField.HEIGHT;
    }

    /** Zone {@code i}'s far (top) row on the play field now, px from the bottom edge. */
    double top(int i) {
        return far[i] - scroll + PlayField.HEIGHT;
    }

    /** The ground row at play-field height {@code y} now: the scroll at which it crossed (or crosses) the top edge. */
    private double groundY(double y) {
        return scroll + y - PlayField.HEIGHT;
    }
}
