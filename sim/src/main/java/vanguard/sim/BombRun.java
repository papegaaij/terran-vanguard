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
        }
    }

    /** Starts the run of a unit at {@code unitX}: to its row at {@code rowY} (y up), its slot {@code offset} across. */
    void start(double rowY, double offset, double unitX) {
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
