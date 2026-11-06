package vanguard.sim;

/**
 * M11 part A (design/enemies, Window shields; design/enemies/space/gate-warden): one unit's {@link
 * EnemySpec.WindowShield}. The shell is <b>closed</b> ({@link #closed()}: {@link Enemy#invulnerable()}) except while
 * its unit's {@link LineBeam} runs (its telegraph's first step to its beam's last: read from the line, so the window
 * never drifts from it), while a {@code shield-breaker} hit's or the EMP Burst's hold lasts ({@link #hold}; the hold
 * runs on the real steps, not the stunned unit's clock; another hit restarts it) and while a broken shell regrows (the
 * shared regrowth shimmer; the shell counts as closed again only once it has regrown). A hit on the closed shell is a
 * flake and, at most once a tick gap, the stone tick ({@link #spark}); it pays nothing. The iris (the look) draws back
 * and closes over its iris time. Each {@link Enemy} owns one, used and hashed only while its stat block has a window
 * shield; {@link #stepAll} runs after the laser lines. Allocates nothing.
 */
public final class WindowShell {
    private final Enemy u;
    /** Steps left of a breaker's or the EMP's hold. */
    private int hold;
    /** Steps left of its regrowth after a break (open until it ends). */
    private int regrow;
    /** Whether a hold broke it since it was last closed (it regrows when the window ends). */
    private boolean broken;
    /** Whether it was open at the end of its last step (its open and close events). */
    private boolean open;
    /** The iris's steps toward open, 0 (closed) to the iris time. */
    private int iris;
    /** Steps until its next tick may sound. */
    private int tickWait;
    /** Steps until a beam on it raises its next flake. */
    private int beamWait;

    WindowShell(Enemy unit) {
        this.u = unit;
    }

    /** Back to a closed shell (a new life). */
    void clear() {
        hold = 0;
        regrow = 0;
        broken = false;
        open = false;
        iris = 0;
        tickWait = 0;
        beamWait = 0;
    }

    private boolean fitted() {
        return u.spec().windowShield().isPresent();
    }

    /** Whether its unit has a window shield and it is closed now: no damage from any path. */
    public boolean closed() {
        return fitted() && !u.lineBeam.running() && hold == 0 && regrow == 0;
    }

    /** Whether its unit has a window shield and it is open now (its window, a hold or its regrowth). */
    public boolean open() {
        return fitted() && !closed();
    }

    /**
     * A {@code shield-breaker} hit or the EMP Burst: it opens at once for its hold ({@link
     * EnemySpec.WindowShield#holdSeconds()}), restarting a running one ({@link SimEvents.Type#SHELL_BROKEN}; {@link
     * SimEvents.Type#SHELL_OPEN} if it was closed). Nothing for a unit without a window shield.
     */
    void hold() {
        if (!fitted()) {
            return;
        }
        boolean wasClosed = closed();
        EnemySpec.WindowShield spec = u.spec().windowShield().get();
        hold = Math.max(1, SimStep.ticks(spec.holdSeconds()));
        regrow = 0;
        broken = true;
        u.events.add(SimEvents.Type.SHELL_BROKEN, u.x(), u.y(), u.kind());
        if (wasClosed) {
            open = true;
            u.events.add(SimEvents.Type.SHELL_OPEN, u.x(), u.y(), u.kind());
        }
    }

    /**
     * A hit on the closed shell at (x, y): the flake ({@link SimEvents.Type#SHELL_FLAKE}) and, at most once a tick gap,
     * the tick ({@link SimEvents.Type#SHELL_TICK}).
     */
    void spark(double x, double y) {
        u.events.add(SimEvents.Type.SHELL_FLAKE, x, y, u.kind());
        if (tickWait == 0) {
            tickWait = SimStep.ticks(u.spec().windowShield().orElseThrow().tickGapSeconds());
            u.events.add(SimEvents.Type.SHELL_TICK, x, y, u.kind());
        }
    }

    /** A beam on the closed shell at (x, y) this step: a {@link #spark} at most once a tick gap. */
    void beamSpark(double x, double y) {
        if (beamWait == 0) {
            beamWait = Math.max(
                    1, SimStep.ticks(u.spec().windowShield().orElseThrow().tickGapSeconds()));
            spark(x, y);
        }
    }

    /**
     * One step after its line's: the hold and the regrowth count down; a broken shell whose window and hold are over
     * starts to regrow; {@link SimEvents.Type#SHELL_OPEN} or {@link SimEvents.Type#SHELL_CLOSE} as it changes; the
     * iris follows.
     */
    void step() {
        EnemySpec.WindowShield spec = u.spec().windowShield().orElseThrow();
        if (hold > 0) {
            hold--;
        }
        if (regrow > 0) {
            regrow--;
        }
        if (tickWait > 0) {
            tickWait--;
        }
        if (beamWait > 0) {
            beamWait--;
        }
        boolean running = u.lineBeam.running();
        if (broken && !running && hold == 0 && regrow == 0) {
            broken = false;
            regrow = SimStep.ticks(spec.regrowSeconds());
        }
        boolean now = running || hold > 0 || regrow > 0;
        if (now != open) {
            open = now;
            u.events.add(now ? SimEvents.Type.SHELL_OPEN : SimEvents.Type.SHELL_CLOSE, u.x(), u.y(), u.kind());
        }
        int irisTicks = SimStep.ticks(spec.irisSeconds());
        iris = now ? Math.min(irisTicks, iris + 1) : Math.max(0, iris - 1);
    }

    /**
     * Every shelled unit's step in {@code enemies}, after the laser lines; then the seekers' skip: while an open target
     * is on the screen the homing shots and Rook skip the closed shells ({@link Enemy#shunned}).
     */
    static void stepAll(Pool<Enemy> enemies) {
        boolean openTarget = false;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.spec().windowShield().isPresent()) {
                unit.window.step();
            }
            if (!unit.invulnerable()
                    && !unit.spec().neutral()
                    && !unit.spec().seekersIgnore()
                    && ShotGeometry.onField(unit)) {
                openTarget = true;
            }
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.spec().windowShield().isPresent()) {
                unit.shunned = unit.window.closed() && openTarget;
            }
        }
    }

    /** How far the iris is drawn back, 0 (closed) to 1 (open). */
    public double iris() {
        if (!fitted()) {
            return 0;
        }
        int irisTicks = SimStep.ticks(u.spec().windowShield().get().irisSeconds());
        return irisTicks == 0 ? (open ? 1 : 0) : (double) iris / irisTicks;
    }

    /** Whether a breaker's or the EMP's hold keeps it open now. */
    public boolean held() {
        return hold > 0;
    }

    /** Whether it regrows now after a break (the silver-gold shimmer; still open). */
    public boolean regrowing() {
        return regrow > 0;
    }

    /** Steps left of its hold (0 without one). */
    public int holdTicks() {
        return hold;
    }

    void addTo(StateHash hash) {
        hash.add(hold)
                .add(regrow)
                .add(broken ? 1 : 0)
                .add(open ? 1 : 0)
                .add(iris)
                .add(tickWait)
                .add(beamWait);
    }
}
