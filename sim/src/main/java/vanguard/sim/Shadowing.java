package vanguard.sim;

/**
 * M11 part A (the movement vocabulary's {@code mirror} and its fairness rules; design/enemies/air/choir-seraph; named
 * apart from Act 6's {@link Mirroring}, the reflecting plate): one unit's {@link EnemySpec.Shadow}. While it mirrors
 * (it holds its hover's station, or glides after a teleport's landing) its x follows the field's width minus the ship's
 * x as it was the lag ago, at most its speed a second (never a jump), and its row keeps its distance from the ship's y
 * (pushed up the screen, away from the ship, at the same cap, inside the top edge); {@link #keepApart} keeps two
 * mirroring units' rows a hull's height apart. Its spiral gun (if any; unaimed) fires only while it mirrors: a bullet
 * per arm every the spiral's interval, the arms turning at the spiral's rate from a start angle drawn by its serial
 * ({@link EnemyFire#shadowSpiral}). The lag is a ring of the ship's past x (fixed size: {@link #MAX_LAG_SECONDS}).
 * Each {@link Enemy} owns one, used and hashed only for a unit with a {@code mirror}. Allocates nothing in a step.
 */
public final class Shadowing {
    /** The longest lag a mirror may follow with, s. */
    static final double MAX_LAG_SECONDS = 0.5;

    private final double[] past = new double[SimStep.ticks(MAX_LAG_SECONDS) + 1];
    /** The ring's next write. */
    private int next;
    /** The ship's x values recorded so far (up to the ring's size). */
    private int recorded;
    /** Whether it mirrors in this step (set by {@link #follow}). */
    private boolean active;
    /** Whether it mirrored in its last step (a new spell starts its lag's ring afresh). */
    private boolean last;
    /** Its spiral's first arm now, radians counter-clockwise from the +x axis (y up). */
    private double spiralAngle;
    /** Steps until its spiral's next volley. */
    private int spiralWait;

    /** Back to a unit that has not mirrored yet (a new life). */
    void clear() {
        next = 0;
        recorded = 0;
        active = false;
        last = false;
        spiralAngle = 0;
        spiralWait = 0;
    }

    /** Starts a new life's spiral at its start angle, drawn by its {@code serial}. */
    void start(int serial) {
        clear();
        spiralAngle = 2 * StrictMath.PI * Ramming.draw(0x1F83D9ABFB41BD6BL ^ serial);
    }

    /** Not mirroring this step (until {@link #follow} says otherwise). */
    void idle() {
        last = active;
        active = false;
    }

    /** Whether it mirrors now (only then does its spiral fire). */
    public boolean mirroring() {
        return active;
    }

    /**
     * One step of {@code u} mirroring the ship at (shipX, shipY) on the screen: its x toward the mirrored lagged point,
     * its row kept off the ship's, each at most its speed's step.
     */
    void follow(Enemy u, EnemySpec.Shadow spec, double shipX, double shipY) {
        if (!last) {
            // It starts mirroring: the ring starts from the ship's x now (no stale point from an earlier spell).
            recorded = 0;
            next = 0;
        }
        active = true;
        past[next] = shipX;
        next = (next + 1) % past.length;
        recorded = Math.min(past.length, recorded + 1);
        int lag = Math.min(recorded - 1, SimStep.ticks(spec.lagSeconds()));
        double lagged = past[Math.floorMod(next - 1 - lag, past.length)];
        double step = spec.speed() * SimStep.SECONDS;
        double half = u.box.width() / 2;
        double target = Math.clamp(PlayField.WIDTH - lagged, half, PlayField.WIDTH - half);
        u.prevX = u.x;
        u.prevY = u.y;
        u.x += Math.clamp(target - u.x, -step, step);
        double y = u.y();
        double top = PlayField.HEIGHT - u.box.height() / 2;
        if (y - shipY < spec.minDy() && y < top) {
            // Its row keeps its distance at the ship's own pace (the speed cap is the mirror's, sideways).
            u.y = u.frame.y(Math.min(top, shipY + spec.minDy()));
        }
    }

    /**
     * Its spiral's step while it mirrors: the arms turn; returns whether a volley is due now (one bullet per arm from
     * {@link #spiralAngle()}).
     */
    boolean spiralDue(EnemyGun.SpiralSpec spiral) {
        if (!active) {
            return false;
        }
        spiralAngle =
                Math.IEEEremainder(spiralAngle + spiral.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        if (spiralWait > 0) {
            spiralWait--;
            return false;
        }
        spiralWait = Math.max(1, SimStep.ticks(spiral.intervalSeconds())) - 1;
        return true;
    }

    /** Its spiral's first arm now, radians counter-clockwise from the +x axis (y up). */
    double spiralAngle() {
        return spiralAngle;
    }

    /**
     * Every two mirroring units of {@code enemies} whose rows are less than the taller hull's height apart: the upper
     * one moves up (inside the top edge), the lower one down by what is left, each at most its speed's step. Runs after
     * the moves; nothing without mirroring units.
     */
    static void keepApart(Pool<Enemy> enemies) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy a = enemies.get(i);
            if (a.spec().shadow().isEmpty() || !a.shadowing.active) {
                continue;
            }
            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy b = enemies.get(j);
                if (b.spec().shadow().isEmpty() || !b.shadowing.active) {
                    continue;
                }
                Enemy upper = a.y() >= b.y() ? a : b;
                Enemy lower = upper == a ? b : a;
                double gap = Math.max(a.box.height(), b.box.height());
                double missing = gap - (upper.y() - lower.y());
                if (missing <= 0) {
                    continue;
                }
                double top = PlayField.HEIGHT - upper.box.height() / 2;
                double up = Math.min(
                        Math.min(missing, Math.max(0, top - upper.y())),
                        upper.spec().shadow().get().speed() * SimStep.SECONDS);
                upper.y = upper.frame.y(upper.y() + up);
                double down = Math.min(missing - up, lower.spec().shadow().get().speed() * SimStep.SECONDS);
                lower.y = lower.frame.y(lower.y() - down);
            }
        }
    }

    void addTo(StateHash hash) {
        hash.add(next)
                .add(recorded)
                .add(active ? 1 : 0)
                .add(last ? 1 : 0)
                .add(spiralAngle)
                .add(spiralWait);
        for (int k = 0; k < recorded; k++) {
            hash.add(past[k]);
        }
    }
}
