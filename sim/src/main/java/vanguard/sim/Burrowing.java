package vanguard.sim;

/**
 * M7 part B: one Burrower's {@code burrow} cycle (design/enemies/ground/burrower; user decision Q1 = a:
 * fixed on the screen while it swims, bulges and is up; see {@link EnemySpec.Burrow}). Each {@link Enemy}
 * owns one, used only while its stat block has a burrow; the {@link EnemyForce} runs the wave's stagger
 * ({@link #waiting()}, {@link #bulge()}) and fires the rings ({@link #ringDue()}). Its random draws come
 * from its own seed (a SplitMix64 state), so the cycle replays.
 */
final class Burrowing {
    private EnemySpec.Burrow burrow;
    private SurfaceZones zones = SurfaceZones.NONE;
    private Enemy.BurrowPhase phase = Enemy.BurrowPhase.NONE;
    /** Steps into the current phase. */
    private int ticks;
    /** The current swim's seeded length, steps. */
    private int travelTicks;
    /** Steps since it appeared. */
    private int life;

    private double x;
    private double y;
    private double spotX;
    private double spotY;
    /** Whether the current swim's spot is allowed; a swim without one ends in a skipped surfacing. */
    private boolean spotValid;
    /** Whether it is at its spot. */
    private boolean arrived;
    /** Its surfacings so far, skipped ones included. */
    private int surfacings;
    /** Whether it is up (on {@code ground}): from its surfacing to its dive's middle. */
    private boolean up;

    private long seed;
    /** What happened in this step (not hashed: set and read within the step), as the {@code NOW_} bits. */
    private int now;
    /** The ring due in this step; null for none (not hashed: set and read within the step). */
    private EnemySpec.BurrowRing due;

    static final int NOW_BULGE = 1;
    static final int NOW_SURFACED = 2;
    static final int NOW_DIVE = 4;

    /** Starts the cycle of a unit appearing buried at (atX, atY): it scrolls in with the ground first. */
    void start(EnemySpec.Burrow spec, double atX, double atY, long unitSeed, SurfaceZones surfaceZones) {
        burrow = spec;
        zones = surfaceZones == null ? SurfaceZones.NONE : surfaceZones;
        phase = Enemy.BurrowPhase.ENTER;
        ticks = 0;
        travelTicks = 0;
        life = 0;
        x = atX;
        y = atY;
        spotX = atX;
        spotY = atY;
        spotValid = false;
        arrived = false;
        surfacings = 0;
        up = false;
        seed = unitSeed;
        now = 0;
        due = null;
    }

    /** The level's no-surface zones (set as the unit enters). */
    void zones(SurfaceZones surfaceZones) {
        zones = surfaceZones == null ? SurfaceZones.NONE : surfaceZones;
    }

    /** Back to a unit without a burrow. */
    void clear() {
        burrow = null;
        zones = SurfaceZones.NONE;
        phase = Enemy.BurrowPhase.NONE;
        ticks = 0;
        travelTicks = 0;
        life = 0;
        x = y = spotX = spotY = 0;
        spotValid = false;
        arrived = false;
        surfacings = 0;
        up = false;
        seed = 0;
        now = 0;
        due = null;
    }

    /**
     * One step; returns false once it swam off the bottom edge (its escape). The ground scrolled
     * {@code groundScroll} px in this step; the ship is at (shipX, shipY).
     */
    boolean step(double shipX, double shipY, double groundScroll, double halfHeight) {
        now = 0;
        due = null;
        life++;
        ticks++;
        double swim = burrow.swimSpeed() * SimStep.SECONDS;
        switch (phase) {
            case ENTER -> {
                y -= groundScroll;
                EnemySpec.SurfaceSpot spot = burrow.spot();
                if (y <= Math.min(PlayField.HEIGHT - spot.edgeMargin(), shipY + spot.aheadMax())) {
                    travel(shipX, shipY);
                }
            }
            case TRAVEL -> {
                swimToSpot(swim);
                if (ticks >= travelTicks && arrived) {
                    if (!spotValid) {
                        // A surfacing with no allowed spot is skipped; it still counts.
                        surfacings++;
                        afterSurfacing(shipX, shipY);
                    } else {
                        phase = Enemy.BurrowPhase.WAIT;
                        ticks = 0;
                    }
                } else if (lifeOver()) {
                    escape();
                }
            }
            case WAIT -> {
                if (lifeOver()) {
                    escape();
                }
            }
            case BULGE -> {
                if (ticks >= SimStep.ticks(burrow.bulgeSeconds())) {
                    phase = Enemy.BurrowPhase.SURFACED;
                    ticks = 0;
                    up = true;
                    surfacings++;
                    now |= NOW_SURFACED;
                }
            }
            case SURFACED -> {
                if (ticks >= surfacedTicks()) {
                    phase = Enemy.BurrowPhase.DIVE;
                    ticks = 0;
                    now |= NOW_DIVE;
                }
            }
            case DIVE -> {
                int total = diveTicks();
                if (ticks >= total / 2) {
                    up = false;
                }
                if (ticks >= total) {
                    afterSurfacing(shipX, shipY);
                }
            }
            case ESCAPE -> {
                y -= groundScroll + swim;
                return y + halfHeight > 0;
            }
            case NONE -> {
                return true;
            }
        }
        if (phase == Enemy.BurrowPhase.SURFACED) {
            ring(burrow.ring());
            if (burrow.secondRing().isPresent()) {
                ring(burrow.secondRing().get());
            }
        }
        return true;
    }

    private void ring(EnemySpec.BurrowRing ring) {
        if (ticks == SimStep.ticks(ring.atSeconds())) {
            due = ring;
        }
    }

    /** After a surfacing (or a skipped one): off the bottom edge after its last or once its life is over, else on to the next. */
    private void afterSurfacing(double shipX, double shipY) {
        if (surfacings >= burrow.surfacings() || lifeOver()) {
            escape();
        } else {
            travel(shipX, shipY);
        }
    }

    private boolean lifeOver() {
        return life >= SimStep.ticks(burrow.lifeSeconds());
    }

    private void escape() {
        phase = Enemy.BurrowPhase.ESCAPE;
        ticks = 0;
        up = false;
    }

    /** A new swim: a spot picked now and a seeded swim time. */
    private void travel(double shipX, double shipY) {
        phase = Enemy.BurrowPhase.TRAVEL;
        ticks = 0;
        arrived = false;
        double min = burrow.travelSeconds().min();
        double max = burrow.travelSeconds().max();
        travelTicks = Math.max(1, SimStep.ticks(min + next() * (max - min)));
        pick(shipX, shipY);
    }

    /**
     * Picks the spot (design/enemies/ground/burrower, Choosing a spot): {@code aheadMin}–{@code aheadMax}
     * px above the ship and within ±{@code across} of its x (seeded), inside the edge margins; mirrored
     * below the ship when the top edge leaves it too close; in a no-surface zone the nearest allowed point
     * beside or beyond that zone (M7 part C: on rock, or within the edge margin of it, the nearest point
     * beside it at that height, else beside a zone); without one the swim goes toward the drawn point and
     * skips its surfacing.
     */
    private void pick(double shipX, double shipY) {
        EnemySpec.SurfaceSpot spot = burrow.spot();
        double dx = spot.across() * (2 * next() - 1);
        double dy = spot.aheadMin() + next() * (spot.aheadMax() - spot.aheadMin());
        double m = spot.edgeMargin();
        double tx = Math.clamp(shipX + dx, m, PlayField.WIDTH - m);
        double ty = Math.clamp(shipY + dy, m, PlayField.HEIGHT - m);
        if (!clear(tx, ty, shipX, shipY)) {
            ty = Math.clamp(shipY - dy, m, PlayField.HEIGHT - m);
        }
        spotX = tx;
        spotY = ty;
        spotValid = allowed(tx, ty, shipX, shipY) || nearestAllowed(tx, ty, shipX, shipY);
    }

    /** Whether (px, py) is at least the clearance from the ship's centre. */
    private boolean clear(double px, double py, double shipX, double shipY) {
        double c = burrow.spot().clearance();
        double ddx = px - shipX;
        double ddy = py - shipY;
        return ddx * ddx + ddy * ddy >= c * c;
    }

    private boolean allowed(double px, double py, double shipX, double shipY) {
        double m = burrow.spot().edgeMargin();
        return px >= m
                && px <= PlayField.WIDTH - m
                && py >= m
                && py <= PlayField.HEIGHT - m
                && clear(px, py, shipX, shipY)
                && !zones.blocked(px, py)
                && !zones.rock(px, py, m);
    }

    /**
     * Moves the spot from (px, py), which lies in a zone, to the nearest allowed point just outside a zone's
     * side, near or far row; false (the spot unchanged) when there is none.
     */
    private boolean nearestAllowed(double px, double py, double shipX, double shipY) {
        double best = Double.POSITIVE_INFINITY;
        // M7 part C: off rock, beside the wall or island it lies in, at the same height.
        double fx = zones.flyableX(py, px, burrow.spot().edgeMargin());
        if (fx != px && allowed(fx, py, shipX, shipY)) {
            best = (fx - px) * (fx - px);
            spotX = fx;
            spotY = py;
        }
        for (int i = 0; i < zones.count(); i++) {
            for (int c = 0; c < 4; c++) {
                double cx = c == 0 ? zones.left(i) - 1 : c == 1 ? zones.right(i) + 1 : px;
                double cy = c == 2 ? zones.bottom(i) - 1 : c == 3 ? zones.top(i) + 1 : py;
                double d = (cx - px) * (cx - px) + (cy - py) * (cy - py);
                if (d < best && allowed(cx, cy, shipX, shipY)) {
                    best = d;
                    spotX = cx;
                    spotY = cy;
                }
            }
        }
        return best < Double.POSITIVE_INFINITY;
    }

    private void swimToSpot(double swim) {
        double dx = spotX - x;
        double dy = spotY - y;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d <= swim) {
            x = spotX;
            y = spotY;
            arrived = true;
        } else {
            x += dx / d * swim;
            y += dy / d * swim;
        }
    }

    /** Whether it waits buried at its spot for its wave's stagger to let it bulge. */
    boolean waiting() {
        return phase == Enemy.BurrowPhase.WAIT;
    }

    /**
     * Whether its spot is still allowed now (the ship may have come within the clearance, a zone may have
     * scrolled under it); checked as the bulge starts.
     */
    boolean spotAllowed(double shipX, double shipY) {
        return allowed(spotX, spotY, shipX, shipY);
    }

    /** Its spot is no longer allowed: it swims to a new one (no surfacing counted). */
    void repick(double shipX, double shipY) {
        travel(shipX, shipY);
    }

    /** The stagger lets it bulge: the tell starts at its spot. */
    void bulge() {
        phase = Enemy.BurrowPhase.BULGE;
        ticks = 0;
        now |= NOW_BULGE;
    }

    /**
     * Steps from now until it is buried again if it is up or about to be (bulging, up, or diving before
     * the dive's middle); -1 otherwise. The stagger's cap counts the units still up when a new one would
     * surface.
     */
    int upFor() {
        int half = diveTicks() / 2;
        return switch (phase) {
            case BULGE -> SimStep.ticks(burrow.bulgeSeconds()) - ticks + surfacedTicks() + half;
            case SURFACED -> surfacedTicks() - ticks + half;
            case DIVE -> up ? half - ticks : -1;
            default -> -1;
        };
    }

    /** Its bulge's length, steps. */
    int bulgeTicks() {
        return SimStep.ticks(burrow.bulgeSeconds());
    }

    private int surfacedTicks() {
        return SimStep.ticks(burrow.surfacedSeconds());
    }

    private int diveTicks() {
        return Math.max(2, SimStep.ticks(burrow.diveSeconds()));
    }

    /** The next draw of its timer, uniform in [0, 1): SplitMix64 on its own state. */
    private double next() {
        long z = (seed += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) >>> 11) * 0x1.0p-53;
    }

    Enemy.BurrowPhase phase() {
        return phase;
    }

    /** Whether it is up: on {@code ground}, hittable. */
    boolean up() {
        return up;
    }

    int now() {
        return now;
    }

    EnemySpec.BurrowRing ringDue() {
        return due;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    double spotX() {
        return spotX;
    }

    double spotY() {
        return spotY;
    }

    int surfacings() {
        return surfacings;
    }

    /** How far into the current phase it is, 0 to 1, between the previous and the current step; for the looks. */
    double progress(double alpha) {
        int total =
                switch (phase) {
                    case TRAVEL -> travelTicks;
                    case BULGE -> bulgeTicks();
                    case SURFACED -> surfacedTicks();
                    case DIVE -> diveTicks();
                    default -> 0;
                };
        return total <= 0 ? 0 : Math.min(1, Math.max(0, (ticks - 1 + alpha) / total));
    }

    void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(ticks)
                .add(travelTicks)
                .add(life)
                .add(spotX)
                .add(spotY)
                .add(spotValid ? 1 : 0)
                .add(arrived ? 1 : 0)
                .add(surfacings)
                .add(up ? 1 : 0)
                .add(seed);
    }
}
