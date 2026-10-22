package vanguard.sim;

/**
 * M8 part C (design/enemies/naval/eel-swarm): a water chain's own motion, one per {@link Chain} (kept out of its
 * body): the head point's {@code snake} sway about its route and a cut-off rear part's scatter clock.
 *
 * <p>The sway is its head's {@link EnemySpec.Sine}: the head point swings {@code amplitude} px across its direction
 * of travel (to the right of it for a positive swing, as a lane walker's weave), one full swing per {@code
 * periodSeconds} of its own flight from its entry on; the swing shrinks with the chain's speed while a dead chain
 * drifts to a halt, so the body does not writhe in place. A scattering part ({@link EnemySpec.Scatter}) counts down
 * its swim; when it runs out its members die. Hashed only when it has a sway or a scatter, so the Coilwyrm and the
 * Threadcrawler hash as before.
 */
final class ChainSwim {
    /** The head point's sway; null for none. */
    private EnemySpec.Sine sway;
    /** The speed at which it swings its whole amplitude: the head point's at its entry. */
    private double swaySpeed;
    /** Steps it swayed. */
    private int swayTicks;
    /** Whether it is a scattering rear part. */
    private boolean scattering;
    /** Steps of its swim left; 0 once it ran out. */
    private int scatterTicks;
    /** The sway's offset of this step (not hashed: worked out in the step from the hashed clock). */
    private double offsetX;

    private double offsetY;

    /** Back to a chain without a sway or a scatter. */
    void clear() {
        sway = null;
        swaySpeed = 0;
        swayTicks = 0;
        scattering = false;
        scatterTicks = 0;
        offsetX = 0;
        offsetY = 0;
    }

    /** A wave's chain whose head sways by {@code headSway} (null for none) at {@code speed} px/s. */
    void sway(EnemySpec.Sine headSway, double speed) {
        sway = headSway;
        swaySpeed = speed;
    }

    /** A cut-off rear part that swims on for {@code seconds}. */
    void scatter(double seconds) {
        scattering = true;
        scatterTicks = Math.max(1, SimStep.ticks(seconds));
    }

    /**
     * One step of the sway while the head point moves along (dx, dy) at {@code speed}: its offset of this step
     * ({@link #offsetX()}, {@link #offsetY()}); none without a sway.
     */
    void step(double dx, double dy, double speed) {
        if (sway == null) {
            return;
        }
        swayTicks++;
        double norm = Math.sqrt(dx * dx + dy * dy);
        double ux = norm > 0 ? dx / norm : 0;
        double uy = norm > 0 ? dy / norm : -1;
        double scale = swaySpeed > 0 ? Math.clamp(speed / swaySpeed, 0, 1) : 1;
        double swing = sway.amplitude()
                * scale
                * Trig.sin(2 * StrictMath.PI * swayTicks * SimStep.SECONDS / sway.periodSeconds());
        offsetX = uy * swing;
        offsetY = -ux * swing;
    }

    /** The sway's offset across the route in this step, px (0 without a sway). */
    double offsetX() {
        return offsetX;
    }

    double offsetY() {
        return offsetY;
    }

    /** Whether the head point sways. */
    boolean swaying() {
        return sway != null;
    }

    /** Whether it is a scattering rear part. */
    boolean scattering() {
        return scattering;
    }

    /** Counts its swim down; true in the step it runs out (once). */
    boolean scatterDue() {
        return scattering && scatterTicks > 0 && --scatterTicks == 0;
    }

    /** Steps of its swim left; 0 once it ran out (and for a chain that does not scatter). */
    int scatterLeft() {
        return scatterTicks;
    }

    /** Hashed only for a chain with a sway or a scatter. */
    void addTo(StateHash hash) {
        if (sway != null || scattering) {
            hash.add(swayTicks).add(swaySpeed).add(scattering ? 1 : 0).add(scatterTicks);
        }
    }
}
