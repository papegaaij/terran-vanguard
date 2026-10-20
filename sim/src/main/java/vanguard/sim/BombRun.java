package vanguard.sim;

/**
 * M7 part D: one unit's bomb run on the ground allies (design/campaign Level 17, What goes for the domes;
 * defaults pending the user's review): it flies to its row and steers to its dome's x plus its slot's
 * offset, drops its load over it and flies on straight down. Each {@link Enemy} owns one, used only once it
 * flies a run (its fields are hashed only then); the unit takes its place from {@link #x()} and {@link
 * #y()} after a {@link #step}.
 */
final class BombRun {
    /** Whether it flies one. */
    private boolean bombing;
    /** The ground ally it steers to; -1 for none. */
    private int bombDome = -1;
    /** Whether its load is spent. */
    private boolean bombSpent;
    /** Its row (y up), its slot's offset across from the dome's centre and the x it steers to. */
    private double bombRowY;

    private double bombOffset;
    private double bombX;
    /**
     * Round 41: the steps it must fly on the play field before it may drop (its run-in; 0 for none) and the
     * steps it has flown there (hashed only with a run-in, so a level without one hashes as before).
     */
    private int runInTicks;

    private int runTicks;
    /** Its place after the last {@link #step} (not hashed: the unit's own position is). */
    private double x;

    private double y;

    /** Back to a unit without a bomb run. */
    void clear() {
        bombing = false;
        bombDome = -1;
        bombSpent = false;
        bombRowY = 0;
        bombOffset = 0;
        bombX = 0;
        runInTicks = 0;
        runTicks = 0;
    }

    /** Hashed once it flies a run, with the unit's {@code entered}. */
    void addTo(StateHash hash, boolean entered) {
        if (bombing) {
            hash.add(bombDome)
                    .add(bombSpent ? 1 : 0)
                    .add(bombRowY)
                    .add(bombOffset)
                    .add(bombX)
                    .add(entered ? 1 : 0);
            if (runInTicks > 0) {
                hash.add(runTicks);
            }
        }
    }

    /** Starts the run of a unit at {@code unitX}: to its row at {@code rowY} (y up), its slot {@code offset} across. */
    void start(double rowY, double offset, double unitX) {
        start(rowY, offset, unitX, 0);
    }

    /** Round 41: as {@link #start(double, double, double)}, dropping only after {@code runIn} steps on the play field. */
    void start(double rowY, double offset, double unitX, int runIn) {
        runInTicks = runIn;
        runTicks = 0;
        bombing = true;
        bombDome = -1;
        bombSpent = false;
        bombRowY = rowY;
        bombOffset = offset;
        bombX = unitX;
    }

    /** The ground ally it steers to this step ({@code dome}, -1 for none), standing at {@code domeX}; {@code half} its half width. */
    void target(int dome, double domeX, double half) {
        bombDome = dome;
        if (dome >= 0) {
            bombX = Math.clamp(domeX + bombOffset, half, PlayField.WIDTH - half);
        }
    }

    /** Round 41: one step of its run flown on the play field ({@code onField}), toward its run-in. */
    void flown(boolean onField) {
        if (onField && runTicks < runInTicks) {
            runTicks++;
        }
    }

    /** Round 41: whether its run-in is over, so it may drop its load (always without one). */
    boolean armed() {
        return runTicks >= runInTicks;
    }

    /** It dropped its load over its dome: it flies on straight down. */
    void drop() {
        bombSpent = true;
        bombDome = -1;
    }

    /** One step of the run of a unit at (unitX, unitY) by {@code step} px: toward its row and its dome's slot, or straight down. */
    void step(double unitX, double unitY, double step) {
        x = unitX;
        y = unitY;
        if (bombSpent || bombDome < 0) {
            y -= step;
            return;
        }
        double dx = bombX - x;
        double dy = bombRowY - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length <= step) {
            x = bombX;
            y = bombRowY;
        } else {
            x += dx / length * step;
            y += dy / length * step;
        }
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    /** The ground ally it steers to now; -1 when it flies none, its load is spent or no dome is left. */
    int dome() {
        return bombing && !bombSpent ? bombDome : -1;
    }

    boolean spent() {
        return bombSpent;
    }

    boolean bombing() {
        return bombing;
    }
}
