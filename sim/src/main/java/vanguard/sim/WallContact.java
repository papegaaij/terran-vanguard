package vanguard.sim;

/**
 * M7 part C (design/campaign Level 16, Wall contact; M1 = a): one hull's touches of rock, the ship's or
 * Rook's: a touch pushes it back along the rock's normal over the push time and starts the cooldown, in
 * which further contact only clamps it. Hashed by the sortie only in a level with terrain.
 */
final class WallContact {
    private final int pushTicks;
    private final double pushStep;
    private final int cooldownTicks;
    private int pushLeft;
    private int cooling;
    private double nx;
    private double ny;

    WallContact(LevelScript.TerrainSpec spec) {
        pushTicks = Math.max(1, SimStep.ticks(spec.pushSeconds()));
        pushStep = spec.push() / pushTicks;
        cooldownTicks = SimStep.ticks(spec.cooldownSeconds());
    }

    /** A new attempt: no push, no cooldown. */
    void reset() {
        pushLeft = 0;
        cooling = 0;
        nx = ny = 0;
    }

    /** One step: the cooldown runs down; returns whether a push moves the hull in it ({@link #pushStep} along the normal). */
    boolean step() {
        if (cooling > 0) {
            cooling--;
        }
        if (pushLeft > 0) {
            pushLeft--;
            return true;
        }
        return false;
    }

    /** The push's direction (the touch's normal) and its distance per step. */
    double nx() {
        return nx;
    }

    double ny() {
        return ny;
    }

    double pushStep() {
        return pushStep;
    }

    /** Whether a contact now is a new touch (damage and push), not one that only clamps. */
    boolean ready() {
        return cooling == 0;
    }

    /** A touch with the rock's normal (nx, ny): the push and the cooldown start. */
    void touch(double normalX, double normalY) {
        nx = normalX;
        ny = normalY;
        pushLeft = pushTicks;
        cooling = cooldownTicks;
    }

    /** Whether its cooldown runs (the presentation's flash may follow it). */
    boolean cooling() {
        return cooling > 0;
    }

    void addTo(StateHash hash) {
        hash.add(pushLeft).add(cooling).add(nx).add(ny);
    }
}
