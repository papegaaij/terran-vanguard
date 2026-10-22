package vanguard.sim;

/**
 * M8 part A, the Decoy Flares in flight (design/player/specials, Decoy Flares (L27)): each launch puts its {@link
 * FlaresSpec#count()} flares out of the rear muzzle in a fan backwards (down the screen), each slowing from its launch
 * speed to a stop over the drift time and burning for the burn time. While any burns, every enemy homing missile
 * ({@link Missiles}) steers at the nearest burning flare instead of the ship and bursts on it (the flare keeps
 * burning). They do not move with the ground scroll: they hang in the water or the air. Pooled; nothing here
 * allocates once made. Hashed (by the {@link SpecialSlot}) only with the flares fitted.
 */
public final class Flares {
    /** How far behind the ship's centre the rear muzzle the flares leave from lies, px. */
    public static final double REAR_OFFSET = 20;
    /** A flare's box: a homing missile overlapping it bursts on it. */
    public static final Hitbox BOX = new Hitbox(10, 10);

    private final FlaresSpec spec;
    private final Pool<Flare> pool;
    private final int driftTicks;
    private final int burnTicks;
    /** The flares launched in this attempt: the next one's serial. */
    private int launched;

    /** @param spec the flares' numbers */
    Flares(FlaresSpec spec) {
        this.spec = spec;
        driftTicks = Math.max(1, SimStep.ticks(spec.driftSeconds()));
        burnTicks = Math.max(1, SimStep.ticks(spec.burnSeconds()));
        // As many launches as can burn at once, the repeat apart.
        int launches = (int) Math.ceil(spec.burnSeconds() / spec.repeatSeconds()) + 1;
        pool = new Pool<>(spec.count() * launches, Flare::new, Flare[]::new);
    }

    /** Back to the level start: none burning. */
    void reset() {
        Pools.clear(pool);
        launched = 0;
    }

    /**
     * A launch from the rear muzzle of the ship at (x, y): the fan's flares evenly over the fan centred on straight
     * down the screen, at the launch speed × {@code speedFactor} (the water's bullet speed; 1 in the air).
     */
    void launch(double x, double y, double speedFactor) {
        double fan = Math.toRadians(spec.fanDegrees());
        int count = spec.count();
        for (int k = 0; k < count; k++) {
            Flare flare = pool.obtain();
            if (flare == null) {
                return;
            }
            // Radians clockwise from straight down the screen (as a missile's heading).
            double angle = count == 1 ? 0 : -fan / 2 + k * fan / (count - 1);
            double speed = spec.speed() * speedFactor;
            flare.launch(
                    x,
                    y - REAR_OFFSET,
                    -Trig.sin(angle) * speed,
                    -Trig.cos(angle) * speed,
                    driftTicks,
                    burnTicks,
                    launched++);
        }
    }

    /** One step: the flares drift and slow; one that burnt out is gone. */
    void step() {
        for (int i = pool.size() - 1; i >= 0; i--) {
            if (!pool.get(i).step()) {
                pool.free(i);
            }
        }
    }

    /** Whether any flare burns now: then the homing missiles go for the flares. */
    public boolean burning() {
        return pool.size() > 0;
    }

    /** The index of the burning flare nearest to (x, y); -1 when none burns. */
    int nearest(double x, double y) {
        int best = -1;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < pool.size(); i++) {
            Flare flare = pool.get(i);
            double dx = flare.x - x;
            double dy = flare.y - y;
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    /** How many burn now. */
    public int size() {
        return pool.size();
    }

    /** Flare {@code index} (0 ≤ index &lt; {@link #size()}). */
    public Flare get(int index) {
        return pool.get(index);
    }

    void addTo(StateHash hash) {
        hash.add(launched);
        Pools.addAll(hash, pool);
    }

    /** One burning decoy flare. */
    public static final class Flare implements Hashed {
        private double x;
        private double y;
        private double prevX;
        private double prevY;
        private double vx;
        private double vy;
        /** The speed taken off per step until it stops. */
        private double slowX;

        private double slowY;
        private int age;
        private int driftTicks;
        private int burnTicks;
        private int serial;

        void launch(double atX, double atY, double velocityX, double velocityY, int drift, int burn, int id) {
            x = prevX = atX;
            y = prevY = atY;
            vx = velocityX;
            vy = velocityY;
            slowX = velocityX / drift;
            slowY = velocityY / drift;
            age = 0;
            driftTicks = drift;
            burnTicks = burn;
            serial = id;
        }

        /** One step: it drifts, slowing to a stop over its drift time; returns false once it has burnt out. */
        boolean step() {
            prevX = x;
            prevY = y;
            if (age < driftTicks) {
                // The mean of the step's start and end speeds: it comes to rest at its drift's end.
                x += (vx - slowX / 2) * SimStep.SECONDS;
                y += (vy - slowY / 2) * SimStep.SECONDS;
                vx -= slowX;
                vy -= slowY;
            }
            age++;
            return age < burnTicks;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double renderX(double alpha) {
            return prevX + (x - prevX) * alpha;
        }

        public double renderY(double alpha) {
            return prevY + (y - prevY) * alpha;
        }

        /** Its share of its burn, 0 just launched to 1 burnt out (the glow fades at the end). */
        public double age() {
            return Math.min(1, (double) age / burnTicks);
        }

        /** Its number in the attempt (the game keeps a glow per flare). */
        public int serial() {
            return serial;
        }

        @Override
        public void addTo(StateHash hash) {
            hash.add(x).add(y).add(vx).add(vy).add(age).add(serial);
        }
    }
}
