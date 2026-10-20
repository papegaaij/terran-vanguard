package vanguard.sim;

/**
 * M7 part H, step H3 (design/enemies/bosses/dust-colossus, Suction and debris): the rocks a breaching boss tears loose
 * toward its open mouth ({@link BossSpec.DebrisFlow}). Its {@link BreachArena} times them; the level tears each one
 * loose ({@link SetPiece.BossActions#tearLoose}) from its {@link Debris} pool with the simulation's random numbers.
 * Its rock sizes are made once; tearing a rock loose allocates nothing.
 */
public final class PulledDebris {
    /** A rock is swallowed this close to the mouth's centre (the gullet's half), px. */
    public static final double SWALLOW = 30;
    /** A rock's {@link HazardSpec.DebrisSpec#chunk()}: its sprite {@code debris-colossus-rock}. */
    public static final String CHUNK = "colossus-rock";
    /** Its serials start here (unique in an attempt, apart from the thrown rocks'). */
    static final int SERIAL = 1 << 21;
    /** Draws for a point in the far half before it takes the point straight across from the mouth. */
    private static final int TRIES = 8;

    private final BossSpec.DebrisFlow flow;
    private final HazardSpec.DebrisSpec[] sizes;
    /** Rocks torn loose in this attempt. */
    private int torn;

    PulledDebris(BossSpec.DebrisFlow flow) {
        this.flow = flow;
        sizes = new HazardSpec.DebrisSpec[BossSpec.DebrisFlow.SIZES];
        for (int i = 0; i < sizes.length; i++) {
            double size = flow.minSize() + (flow.maxSize() - flow.minSize()) * i / (sizes.length - 1);
            sizes[i] =
                    new HazardSpec.DebrisSpec(0, 0, CHUNK, new Hitbox(size, size), 0, -1, flow.hp(), flow.contact(), 0);
        }
    }

    void reset() {
        torn = 0;
    }

    /** Rocks torn loose in this attempt. */
    int torn() {
        return torn;
    }

    /**
     * Tears one rock loose from {@code pool} (none when it is full) at a random point of the play field's edges in
     * its far half from the mouth at (mouthX, mouthY) (the side of the line through the field's centre square to the
     * mouth's direction), toward the mouth; its size from {@code rng}. Returns it, or null.
     */
    Debris tear(Pool<Debris> pool, SplitMix64 rng, double mouthX, double mouthY) {
        double cx = PlayField.WIDTH / 2.0;
        double cy = PlayField.HEIGHT / 2.0;
        double ax = mouthX - cx;
        double ay = mouthY - cy;
        double perimeter = 2 * (PlayField.WIDTH + PlayField.HEIGHT);
        double px = Double.NaN;
        double py = Double.NaN;
        for (int i = 0; i < TRIES; i++) {
            double u = rng.range(0, perimeter);
            double ex = edgeX(u);
            double ey = edgeY(u);
            if ((ex - cx) * ax + (ey - cy) * ay <= 0) {
                px = ex;
                py = ey;
                break;
            }
        }
        if (Double.isNaN(px)) {
            // Straight across from the mouth, on the edge.
            double length = Math.sqrt(ax * ax + ay * ay);
            double scale = Math.min(cx / Math.max(1e-9, Math.abs(ax)), cy / Math.max(1e-9, Math.abs(ay)));
            px = length > 0 ? cx - ax * scale : cx;
            py = length > 0 ? cy - ay * scale : 0;
        }
        int size = rng.nextInt(sizes.length);
        Debris rock = pool.obtain();
        if (rock != null) {
            rock.pull(sizes[size], SERIAL + torn, px, py, mouthX, mouthY, flow.startSpeed(), flow.acceleration());
        }
        torn++;
        return rock;
    }

    /** The point {@code u} px along the play field's edges (clockwise from the bottom-left corner up the left edge), x. */
    private static double edgeX(double u) {
        double w = PlayField.WIDTH;
        double h = PlayField.HEIGHT;
        if (u < h) {
            return 0;
        }
        if (u < h + w) {
            return u - h;
        }
        if (u < 2 * h + w) {
            return w;
        }
        return w - (u - 2 * h - w);
    }

    /** ... y. */
    private static double edgeY(double u) {
        double w = PlayField.WIDTH;
        double h = PlayField.HEIGHT;
        if (u < h) {
            return u;
        }
        if (u < h + w) {
            return h;
        }
        if (u < 2 * h + w) {
            return h - (u - h - w);
        }
        return 0;
    }
}
