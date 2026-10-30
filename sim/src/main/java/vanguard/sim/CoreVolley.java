package vanguard.sim;

/**
 * Step BOSS70 (design/enemies/bosses/iron-sovereign, The round 70 rework; {@link RingSpec.Volley}): the open core's rail
 * volley, kept by its {@link RingCore}. At its start it locks its lines from the core's centre: the middle one through
 * the ship's spot, the others its fan apart either side (each from {@link RingSpec.Volley#fromPx()} out, the petals'
 * rim); they telegraph together for the line attack's telegraph (the thin red lines, brightening), then are live for its
 * live time, each a band that deals the attack's damage to the ship once (A1's {@link LineBeam} geometry, as {@link
 * BossLines}). They hurt nothing else; the core does not move, so they do not ride. Allocates nothing once made.
 */
public final class CoreVolley {
    /** The most lines in a volley. */
    public static final int MOST = 5;

    private final RingSpec.Volley spec;
    private final EnemySpec.LaserLine line;
    private final int telegraphTicks;
    private final int liveTicks;
    private final double[] fromX = new double[MOST];
    private final double[] fromY = new double[MOST];
    private final double[] dirX = new double[MOST];
    private final double[] dirY = new double[MOST];
    private final boolean[] hit = new boolean[MOST];
    /** Steps into its telegraph and live time; -1 while none runs. */
    private int ticks = -1;

    CoreVolley(RingSpec.Volley spec, BossSpec boss) {
        this.spec = spec;
        line = boss.attacks().get(spec.attack()).line().orElseThrow();
        telegraphTicks = Math.max(1, SimStep.ticks(line.telegraphSeconds()));
        liveTicks = Math.max(1, SimStep.ticks(line.liveSeconds()));
    }

    /** Back to the level start: none running. */
    void reset() {
        ticks = -1;
        java.util.Arrays.fill(hit, false);
    }

    /** Its telegraph and live time, steps (the play's run). */
    int ticks() {
        return telegraphTicks + liveTicks;
    }

    /** Starts it from the core at (cx, cy), the middle line through the ship at (shipX, shipY). */
    void start(double cx, double cy, double shipX, double shipY) {
        double aim = StrictMath.atan2(shipY - cy, shipX - cx);
        if (shipX == cx && shipY == cy) {
            aim = -StrictMath.PI / 2;
        }
        int n = spec.lines();
        for (int k = 0; k < n; k++) {
            double angle = aim + (k - (n - 1) / 2.0) * spec.fanRadians();
            dirX[k] = Trig.cos(angle);
            dirY[k] = Trig.sin(angle);
            fromX[k] = cx + dirX[k] * spec.fromPx();
            fromY[k] = cy + dirY[k] * spec.fromPx();
            hit[k] = false;
        }
        ticks = 0;
    }

    /** One step: returns true at the step its lines go live. */
    boolean step() {
        if (ticks < 0) {
            return false;
        }
        ticks++;
        if (ticks >= telegraphTicks + liveTicks) {
            ticks = -1;
            return false;
        }
        return ticks == telegraphTicks;
    }

    /** It stops (the core died): none runs. */
    void stop() {
        ticks = -1;
    }

    /** The first line whose live band {@code hull} at (x, y) touches and that has not struck yet; -1 for none. */
    int strikes(Hull hull, double x, double y) {
        if (!live()) {
            return -1;
        }
        for (int k = 0; k < spec.lines(); k++) {
            if (!hit[k]
                    && hull.touchesSegment(
                            x,
                            y,
                            fromX[k],
                            fromY[k],
                            fromX[k] + dirX[k] * LineBeam.LENGTH,
                            fromY[k] + dirY[k] * LineBeam.LENGTH,
                            line.bandPx() / 2)) {
                return k;
            }
        }
        return -1;
    }

    /** Line {@code k} struck the ship: once per line. */
    void markHit(int k) {
        hit[k] = true;
    }

    /** A line's damage. */
    double damage() {
        return line.damage();
    }

    /** Whether it runs (telegraph or live). */
    public boolean running() {
        return ticks >= 0;
    }

    /** Whether it telegraphs now. */
    public boolean telegraphing() {
        return ticks >= 0 && ticks < telegraphTicks;
    }

    /** Whether its lines are live now. */
    public boolean live() {
        return ticks >= telegraphTicks;
    }

    /** The telegraph's share run, 0 to 1 (1 once live, 0 while none runs). */
    public double charge() {
        if (ticks < 0) {
            return 0;
        }
        return live() ? 1 : Math.min(1, (double) ticks / telegraphTicks);
    }

    /** Seconds until its lines go live (0 once live; the autopilot's look-ahead). */
    public double secondsToLive() {
        return live() || ticks < 0 ? 0 : (telegraphTicks - ticks) * SimStep.SECONDS;
    }

    /** Its live time, s. */
    public double liveSeconds() {
        return line.liveSeconds();
    }

    /** Its lines' count. */
    public int lines() {
        return spec.lines();
    }

    /** Its band's width, px. */
    public double bandPx() {
        return line.bandPx();
    }

    /** Line {@code k}'s origin, px from the left. */
    public double fromX(int k) {
        return fromX[k];
    }

    /** Line {@code k}'s origin, px up. */
    public double fromY(int k) {
        return fromY[k];
    }

    /** Line {@code k}'s direction, a unit vector (y up); it runs to the screen's edge. */
    public double dirX(int k) {
        return dirX[k];
    }

    public double dirY(int k) {
        return dirY[k];
    }

    void addTo(StateHash hash) {
        hash.add(ticks);
        if (ticks >= 0) {
            for (int k = 0; k < spec.lines(); k++) {
                hash.add(fromX[k]).add(fromY[k]).add(dirX[k]).add(dirY[k]).add(hit[k] ? 1 : 0);
            }
        }
    }
}
