package vanguard.sim;

/**
 * M7 part E: one Dust Devil's vortex (design/enemies/ground/dust-devil; see {@link EnemySpec.Vortex}): its cycle
 * (closed on {@code ground}, rising, open on {@code low-air}, closing and sinking), its seeded swirl round its
 * centre line, which scrolls with the ground, kept off the ship, and its spiral gun's arms while it is open. Each
 * {@link Enemy} owns one, used only while its stat block has a vortex; the {@link EnemyForce} reports its events
 * and fires its spiral. Its random draws come from its own seed, so it replays.
 */
final class VortexCycle {
    /** The sinking after an opening lasts this share of the rise (the gap check's default, pending round 41). */
    static final double SINK_SHARE = 0.5;

    /** {@link #now()}: it started to rise in this step. */
    static final int NOW_RISING = 1;
    /** {@link #now()}: it opened in this step. */
    static final int NOW_OPENED = 2;
    /** {@link #now()}: it closed in this step. */
    static final int NOW_CLOSED = 4;
    /** {@link #now()}: its spiral fires a volley in this step (while open). */
    static final int NOW_SPIRAL = 8;

    private EnemySpec.Vortex vortex;
    private EnemyGun.SpiralSpec spiral;
    private int closedTicks;
    private int riseTicks;
    private int openTicks;
    private int sinkTicks;
    /** Steps into the cycle; negative while its wave's offset still holds it closed. */
    private int cycle;
    /** Whether it opened at least once (only then does a cycle start with its sinking). */
    private boolean opened;
    /** Steps since it appeared (the swirl's clock). */
    private int life;

    private double centreX;
    private double centreY;
    private double x;
    private double y;
    /** The swirl's seeded phases (radians) and its second axis's frequency ratio. */
    private double phaseX;

    private double phaseY;
    private double ratio;
    /** The spiral's arm angle (radians, 0 = right, y up) and its steps to the next volley. */
    private double spiralAngle;

    private int spiralWait;
    /** What happened in this step, as the {@code NOW_} bits (not hashed: set and read within the step). */
    private int now;

    /**
     * Starts the cycle of a unit appearing at (atX, atY), its centre line at {@code atX}, closed, its random
     * draws from {@code seed}.
     */
    void start(EnemySpec.Vortex spec, EnemyGun.SpiralSpec spiralSpec, double atX, double atY, long seed) {
        vortex = spec;
        spiral = spiralSpec;
        closedTicks = Math.max(1, SimStep.ticks(spec.closedSeconds()));
        riseTicks = SimStep.ticks(spec.riseSeconds());
        openTicks = Math.max(1, SimStep.ticks(spec.openSeconds()));
        sinkTicks = Math.min(closedTicks - 1, (int) Math.round(riseTicks * SINK_SHARE));
        cycle = 0;
        opened = false;
        life = 0;
        centreX = x = atX;
        centreY = y = atY;
        long state = seed;
        // Three draws of a SplitMix64 sequence on its own state (no allocation per spawn).
        state += 0x9E3779B97F4A7C15L;
        phaseX = 2 * StrictMath.PI * unit(mix(state));
        state += 0x9E3779B97F4A7C15L;
        phaseY = 2 * StrictMath.PI * unit(mix(state));
        state += 0x9E3779B97F4A7C15L;
        ratio = 1.3 + 0.4 * unit(mix(state));
        spiralAngle = 0;
        spiralWait = 0;
        now = 0;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static double unit(long bits) {
        return (bits >>> 11) * 0x1.0p-53;
    }

    /** Its place in its wave (from 0): its cycle starts that many offsets later. */
    void wavePlace(int place) {
        if (vortex != null) {
            cycle = -SimStep.ticks(place * vortex.offsetSeconds());
        }
    }

    /** Back to a unit without a vortex. */
    void clear() {
        vortex = null;
        spiral = null;
        closedTicks = riseTicks = openTicks = sinkTicks = 0;
        cycle = 0;
        opened = false;
        life = 0;
        centreX = centreY = x = y = 0;
        phaseX = phaseY = ratio = 0;
        spiralAngle = 0;
        spiralWait = 0;
        now = 0;
    }

    /**
     * One step: the centre line scrolls by {@code groundScroll}, the unit follows its swirl and is kept off the
     * ship at (shipX, shipY); then the cycle runs on (an opening aims the spiral's first volley at the ship).
     * Returns false once it left the bottom edge ({@code halfHeight} its hit box's half height).
     */
    boolean step(double shipX, double shipY, double groundScroll, double halfHeight) {
        now = 0;
        life++;
        swirl(shipX, shipY, groundScroll);
        cycle++;
        int cycleTicks = closedTicks + riseTicks + openTicks;
        if (cycle >= cycleTicks) {
            cycle = 0;
        }
        if (cycle == 0 && opened) {
            now |= NOW_CLOSED;
        } else if (cycle == closedTicks && riseTicks > 0) {
            now |= NOW_RISING;
        }
        if (cycle == closedTicks + riseTicks) {
            if (riseTicks == 0) {
                now |= NOW_RISING;
            }
            now |= NOW_OPENED;
            opened = true;
            spiralAngle = StrictMath.atan2(shipY - y, shipX - x);
            spiralWait = 0;
        } else if (phase() == Enemy.VortexPhase.OPEN && spiral != null) {
            spiralAngle = Math.IEEEremainder(
                    spiralAngle + spiral.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        }
        if (phase() == Enemy.VortexPhase.OPEN && spiral != null && --spiralWait <= 0) {
            now |= NOW_SPIRAL;
            spiralWait = Math.max(1, SimStep.ticks(spiral.intervalSeconds()));
        }
        return y + halfHeight > 0;
    }

    /** As {@link #step} while the EMP Burst stuns it: its clocks wait, it scrolls with the ground. */
    boolean stunned(double groundScroll, double halfHeight) {
        now = 0;
        centreY -= groundScroll;
        y -= groundScroll;
        return y + halfHeight > 0;
    }

    /**
     * The swirl: two sines round the centre line (the second axis at half the reach and a seeded frequency ratio),
     * the fastest at the wander speed; the unit follows the swirl's point at up to twice that speed over the ground
     * and is pushed out to the keep-away distance from the ship's centre.
     */
    private void swirl(double shipX, double shipY, double groundScroll) {
        centreY -= groundScroll;
        y -= groundScroll;
        double amplitude = vortex.wanderAmplitude();
        double toX = centreX;
        double toY = centreY;
        if (amplitude > 0 && vortex.wanderSpeed() > 0) {
            double rate = vortex.wanderSpeed() / amplitude;
            double t = life * SimStep.SECONDS;
            toX += amplitude * Trig.sin(phaseX + rate * t);
            toY += amplitude * 0.5 * Trig.sin(phaseY + rate * ratio * t);
        }
        double dx = toX - x;
        double dy = toY - y;
        double step = 2 * vortex.wanderSpeed() * SimStep.SECONDS;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance <= step || distance == 0) {
            x = toX;
            y = toY;
        } else {
            x += dx * step / distance;
            y += dy * step / distance;
        }
        double away = vortex.keepAway();
        double sx = x - shipX;
        double sy = y - shipY;
        double d2 = sx * sx + sy * sy;
        if (away > 0 && d2 < away * away) {
            double d = Math.sqrt(d2);
            if (d == 0) {
                // Right on the ship: out toward its centre line's side (up the screen on the line itself).
                double side = centreX < x ? -1 : centreX > x ? 1 : 0;
                x = shipX + side * away;
                y = side == 0 ? shipY + away : shipY;
            } else {
                x = shipX + sx * away / d;
                y = shipY + sy * away / d;
            }
        }
    }

    /** Where it is in its cycle now. */
    Enemy.VortexPhase phase() {
        if (cycle < 0) {
            return Enemy.VortexPhase.CLOSED;
        }
        if (cycle < closedTicks) {
            return opened && cycle < sinkTicks ? Enemy.VortexPhase.SINKING : Enemy.VortexPhase.CLOSED;
        }
        return cycle < closedTicks + riseTicks ? Enemy.VortexPhase.RISING : Enemy.VortexPhase.OPEN;
    }

    /** How far into its current phase it is, 0 to 1. */
    double progress() {
        return switch (phase()) {
            case SINKING -> (double) cycle / Math.max(1, sinkTicks);
            case CLOSED -> {
                if (cycle < 0) {
                    yield 0;
                }
                int from = opened ? sinkTicks : 0;
                yield (double) (cycle - from) / Math.max(1, closedTicks - from);
            }
            case RISING -> (double) (cycle - closedTicks) / Math.max(1, riseTicks);
            case OPEN -> (double) (cycle - closedTicks - riseTicks) / openTicks;
        };
    }

    /** Whether it is open now (on {@code low-air}, its core hittable). */
    boolean open() {
        return vortex != null && phase() == Enemy.VortexPhase.OPEN;
    }

    /** What happened in this step, as the {@code NOW_} bits. */
    int now() {
        return now;
    }

    /** The spiral's arm angle now (radians, 0 = right, y up). */
    double spiralAngle() {
        return spiralAngle;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    /** Its centre line's x (the wave's authored x). */
    double centreX() {
        return centreX;
    }

    void addTo(StateHash hash) {
        hash.add(cycle)
                .add(opened ? 1 : 0)
                .add(life)
                .add(centreX)
                .add(centreY)
                .add(phaseX)
                .add(phaseY)
                .add(ratio)
                .add(spiralAngle)
                .add(spiralWait);
    }
}
