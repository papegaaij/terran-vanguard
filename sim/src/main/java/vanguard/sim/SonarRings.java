package vanguard.sim;

/**
 * M8 part E, the Sirens' rings in flight (design/enemies/naval/siren; m8e-gaps.md §1.2, §5 E2, E4): each leaves its
 * sac's centre and expands at its {@link EnemySpec.SlowRing#speed()} (sound: not the water's x 0.7) to its radius, its
 * centre scrolling with the ground (the sac's spot on the wall). It deals no damage. A hull (the ship, Rook) is tested
 * once per ring, in the step its band (its last {@link EnemySpec.SlowRing#band()} px behind its edge) first touches the
 * hull's boxes: with a clear line from the sac to the hull's centre ({@link Terrain#rock} sampled every {@link
 * #LINE_STEP} px: a wall, a notch's lip or an island stops it) the hull takes the ring's slow ({@link ShipSlow}); in a
 * dash the ring passes the ship, and after the level's end it slows nothing. Shooting a sac ends its song: its ring
 * in flight ends with it. Pooled, allocation-free; hashed only in a level with a ring unit.
 */
public final class SonarRings {
    /** The most rings in flight (four nests on the screen, a ring each, with room to spare). */
    static final int CAPACITY = 16;
    /** The rock samples along a ring's line to a hull lie this far apart, px. */
    static final double LINE_STEP = 4;

    private final double[] x = new double[CAPACITY];
    private final double[] y = new double[CAPACITY];
    private final double[] prevY = new double[CAPACITY];
    private final double[] radius = new double[CAPACITY];
    private final double[] prevRadius = new double[CAPACITY];
    /** Per ring the serial of the unit that sang it. */
    private final int[] source = new int[CAPACITY];
    /** Per ring whether the ship's and Rook's test is done. */
    private final boolean[] shipDone = new boolean[CAPACITY];

    private final boolean[] rookDone = new boolean[CAPACITY];
    /** Per ring its numbers (its singer's; not hashed: they follow from the singer's kind). */
    private final EnemySpec.SlowRing[] spec = new EnemySpec.SlowRing[CAPACITY];

    private int count;

    /** A new attempt: no ring in flight. */
    void reset() {
        for (int i = 0; i < count; i++) {
            spec[i] = null;
        }
        count = 0;
    }

    /**
     * After {@code unit}'s step: its sac's tell began ({@link SimEvents.Type#RING_TELL}), or its ring leaves the sac
     * ({@link SimEvents.Type#RING}); nothing for a unit without a song.
     */
    void sing(Enemy unit, SimEvents events) {
        if (!unit.song.active()) {
            return;
        }
        if (unit.song.tellNow()) {
            events.add(SimEvents.Type.RING_TELL, unit.x(), unit.y(), unit.kind());
        }
        if (unit.song.ringNow() && count < CAPACITY) {
            int i = count++;
            x[i] = unit.x();
            y[i] = prevY[i] = unit.y();
            radius[i] = prevRadius[i] = 0;
            source[i] = unit.serial();
            shipDone[i] = false;
            rookDone[i] = false;
            spec[i] = unit.spec().slowRing().orElseThrow();
            events.add(SimEvents.Type.RING, unit.x(), unit.y(), unit.kind());
        }
    }

    /** One step: each ring scrolls with the ground and expands; a ring at its radius for a step ends. */
    void step(double groundScroll) {
        for (int i = count - 1; i >= 0; i--) {
            prevY[i] = y[i];
            prevRadius[i] = radius[i];
            if (radius[i] >= spec[i].radius()) {
                free(i);
                continue;
            }
            y[i] -= groundScroll;
            radius[i] = Math.min(spec[i].radius(), radius[i] + spec[i].speed() * SimStep.SECONDS);
        }
    }

    /** The unit {@code serial} was destroyed: its rings end (shooting the sac ends the song). */
    void silence(int serial) {
        for (int i = count - 1; i >= 0; i--) {
            if (source[i] == serial) {
                free(i);
            }
        }
    }

    /**
     * The rings reach the hulls: the ship ({@code shipOpen}: it may be slowed now; else a ring touching it passes, as
     * in a dash) and Rook ({@code rook}, null without him or ejected); rock on the line from the sac stops a ring
     * ({@code canyon} null without terrain). A slowed hull raises {@link SimEvents.Type#SLOWED} (value 0 the ship, 1
     * Rook).
     */
    void reach(Ship ship, boolean shipOpen, Wingman rook, Canyon canyon, SimEvents events) {
        for (int i = 0; i < count; i++) {
            if (!shipDone[i] && touches(i, ship.spec().hull(), ship.x(), ship.y())) {
                shipDone[i] = true;
                if (shipOpen && clear(i, ship.x(), ship.y(), canyon) && ship.slow.slow(spec[i])) {
                    events.add(SimEvents.Type.SLOWED, ship.x(), ship.y(), 0);
                }
            }
            if (rook != null && !rookDone[i] && touches(i, rook.hull(), rook.x(), rook.y())) {
                rookDone[i] = true;
                if (clear(i, rook.x(), rook.y(), canyon) && rook.slow.slow(spec[i])) {
                    events.add(SimEvents.Type.SLOWED, rook.x(), rook.y(), 1);
                }
            }
        }
    }

    /** Whether ring i's band, between its edge and its band's width behind it, touches a box of {@code hull} at (hx, hy). */
    private boolean touches(int i, Hull hull, double hx, double hy) {
        double outer = radius[i];
        double inner = outer - spec[i].band();
        double cx = x[i];
        double cy = y[i];
        for (int p = 0; p < hull.parts().size(); p++) {
            Hull.Part part = hull.parts().get(p);
            double bx = hx + part.dx() - cx;
            double by = hy + part.dy() - cy;
            double hw = part.box().width() / 2;
            double hh = part.box().height() / 2;
            double nearX = Math.max(0, Math.abs(bx) - hw);
            double nearY = Math.max(0, Math.abs(by) - hh);
            double farX = Math.abs(bx) + hw;
            double farY = Math.abs(by) + hh;
            double near = Math.sqrt(nearX * nearX + nearY * nearY);
            double far = Math.sqrt(farX * farX + farY * farY);
            if (near <= outer && far >= inner) {
                return true;
            }
        }
        return false;
    }

    /** Whether the line from ring i's centre to (tx, ty) is clear of rock (always without terrain). */
    private boolean clear(int i, double tx, double ty, Canyon canyon) {
        if (canyon == null) {
            return true;
        }
        double dx = tx - x[i];
        double dy = ty - y[i];
        double length = Math.sqrt(dx * dx + dy * dy);
        int samples = (int) Math.ceil(length / LINE_STEP);
        for (int k = 1; k < samples; k++) {
            double s = (double) k / samples;
            if (canyon.rock(x[i] + dx * s, y[i] + dy * s, 0, 0)) {
                return false;
            }
        }
        return true;
    }

    private void free(int i) {
        int last = --count;
        x[i] = x[last];
        y[i] = y[last];
        prevY[i] = prevY[last];
        radius[i] = radius[last];
        prevRadius[i] = prevRadius[last];
        source[i] = source[last];
        shipDone[i] = shipDone[last];
        rookDone[i] = rookDone[last];
        spec[i] = spec[last];
        spec[last] = null;
    }

    /** The rings in flight. */
    public int count() {
        return count;
    }

    /** Ring i's centre (the sac's spot when it sang, scrolled with the ground), px. */
    public double x(int i) {
        return x[i];
    }

    /** Ring i's centre's height between the previous and the current step; {@code alpha} in [0, 1]. */
    public double y(int i, double alpha) {
        return prevY[i] + (y[i] - prevY[i]) * alpha;
    }

    /** Ring i's radius (its edge) between the previous and the current step, px. */
    public double radius(int i, double alpha) {
        return prevRadius[i] + (radius[i] - prevRadius[i]) * alpha;
    }

    /** Ring i's band behind its edge, px. */
    public double band(int i) {
        return spec[i].band();
    }

    /** The radius ring i ends at, px. */
    public double maxRadius(int i) {
        return spec[i].radius();
    }

    /** The serial of the unit that sang ring i. */
    public int source(int i) {
        return source[i];
    }

    void addTo(StateHash hash) {
        hash.add(count);
        for (int i = 0; i < count; i++) {
            hash.add(x[i])
                    .add(y[i])
                    .add(radius[i])
                    .add(source[i])
                    .add(shipDone[i] ? 1 : 0)
                    .add(rookDone[i] ? 1 : 0);
        }
    }
}
