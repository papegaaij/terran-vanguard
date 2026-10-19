package vanguard.sim;

/**
 * M5 part C: one walker's pounce (design/enemies/ground/ravager): a leap at the ship whose middle (the apex
 * and the air window, on {@link Layer#AIR}) passes over the ship's position at take-off and lands as far
 * beyond (user decision 2026-10-07, the overshoot), then a wait before the next. Each {@link Enemy} owns
 * one, used only while its stat block has a pounce (its fields are hashed only then); in a leap the unit
 * takes its place from {@link #x()} and {@link #y()} after a {@link #step}.
 */
final class Pouncing {
    /** Steps since take-off; -1 while it is not leaping. */
    private int leapTicks = -1;
    /** Steps before it may leap again (from landing). */
    private int pounceWait;
    /** The leap's steps, and the steps of its air window [{@link #airFrom}, {@link #airTo}). */
    private int leapTotal;

    private int airFrom;
    private int airTo;
    /**
     * Where the leap started, where it lands and the point its middle (the apex and the air window)
     * passes over: the ship's position at take-off.
     */
    private double leapFromX;

    private double leapFromY;
    private double leapToX;
    private double leapToY;
    private double leapOverX;
    private double leapOverY;
    /** What this leap touched already, as {@link Enemy#TOUCHED_SHIP} and {@link Enemy#TOUCHED_WINGMAN} bits: once each per leap. */
    private int pounceTouched;
    /** Whether it landed in this step (not hashed: set and read within the step). */
    private boolean landed;
    /** Its place after the last {@link #step} (not hashed: the unit's own position is). */
    private double x;

    private double y;

    /** Back to a unit without a pounce. */
    void clear() {
        leapTicks = -1;
        pounceWait = 0;
        leapTotal = 0;
        airFrom = 0;
        airTo = 0;
        leapFromX = 0;
        leapFromY = 0;
        leapToX = 0;
        leapToY = 0;
        leapOverX = 0;
        leapOverY = 0;
        pounceTouched = 0;
        landed = false;
    }

    /** A walker with {@code pounce} enters: its leap's steps and its air window, the leap's middle. */
    void start(EnemySpec.Pounce pounce) {
        leapTotal = Math.max(1, SimStep.ticks(pounce.leapSeconds()));
        int air = Math.min(leapTotal, SimStep.ticks(pounce.airSeconds()));
        // The middle of the leap: its steps run 1 .. leapTotal.
        airFrom = 1 + (leapTotal - air) / 2;
        airTo = airFrom + air;
    }

    /** Hashed only for a unit with a pounce. */
    void addTo(StateHash hash) {
        hash.add(leapTicks)
                .add(pounceWait)
                .add(leapFromX)
                .add(leapFromY)
                .add(leapToX)
                .add(leapToY)
                .add(leapOverX)
                .add(leapOverY)
                .add(pounceTouched);
    }

    /** A step starts: it has not landed in it yet. */
    void scratch() {
        landed = false;
    }

    /**
     * One step of its leap with {@code pounce}: its first half to the point it passes over, its second half on
     * to its landing point; at its end it lands and waits its interval.
     */
    void step(EnemySpec.Pounce pounce) {
        leapTicks++;
        double p = (double) leapTicks / leapTotal;
        if (2 * leapTicks <= leapTotal) {
            x = leapFromX + (leapOverX - leapFromX) * 2 * p;
            y = leapFromY + (leapOverY - leapFromY) * 2 * p;
        } else {
            x = leapOverX + (leapToX - leapOverX) * (2 * p - 1);
            y = leapOverY + (leapToY - leapOverY) * (2 * p - 1);
        }
        if (leapTicks >= leapTotal) {
            leapTicks = -1;
            pounceWait = SimStep.ticks(pounce.intervalSeconds());
            landed = true;
        }
    }

    /** One step on the ground: the wait before the next leap counts down. */
    void waitStep() {
        if (pounceWait > 0) {
            pounceWait--;
        }
    }

    /** Whether it may leap now: not leaping, its interval since the last landing over. */
    boolean ready() {
        return leapTicks < 0 && pounceWait <= 0;
    }

    /**
     * It takes off from (fromX, fromY) at the ship at (shipX, shipY): its middle passes over the ship's
     * position and it lands as far beyond, the landing point's centre kept {@code halfWidth} and {@code
     * halfHeight} inside every edge of the play field.
     */
    void takeOff(double fromX, double fromY, double shipX, double shipY, double halfWidth, double halfHeight) {
        leapTicks = 0;
        leapFromX = fromX;
        leapFromY = fromY;
        leapOverX = shipX;
        leapOverY = shipY;
        leapToX = Math.clamp(2 * shipX - fromX, halfWidth, PlayField.WIDTH - halfWidth);
        leapToY = Math.clamp(2 * shipY - fromY, halfHeight, PlayField.HEIGHT - halfHeight);
        pounceTouched = 0;
    }

    /** Whether it is in a leap. */
    boolean leaping() {
        return leapTicks >= 0;
    }

    /** Whether it is in its leap's air window now (on {@link Layer#AIR}). */
    boolean inAir() {
        return leapTicks >= airFrom && leapTicks < airTo;
    }

    /** Whether it landed in this step. */
    boolean landedNow() {
        return landed;
    }

    /**
     * How far its leap is, from 0 at take-off to 1 at landing, between the previous and the current step
     * ({@code alpha} in [0, 1]); 0 while it is not leaping.
     */
    double progress(double alpha) {
        if (leapTicks < 0) {
            return 0;
        }
        return Math.clamp((leapTicks - 1 + alpha) / leapTotal, 0.0, 1.0);
    }

    /** Whether the leap touches {@code what} for the first time: once each per leap. */
    boolean touch(int what) {
        if ((pounceTouched & what) != 0) {
            return false;
        }
        pounceTouched |= what;
        return true;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }
}
