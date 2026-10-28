package vanguard.sim;

/**
 * M9 part C (design/enemies, Mine lines; design/enemies/space/minelayer; design/campaign Level 30, C1 a and C2 a): the
 * line mines' rules over {@link EnemyFire}'s mine pool. An armed line mine whose proximity holds the ship's centre or
 * Rook's blinks, then bursts with its ring and its blast; a shot one bursts with its ring and sets off the line mines
 * within its chain reach, which burst a hop later without a ring and set off theirs (pool order, deterministic). The
 * blasts of a step wait here until the hit passes ({@link HitPasses}) apply them. Spores are never touched. Allocated
 * once; it allocates nothing per step.
 */
final class MineLines {
    /** {@link #step}: nothing happens to the mine. */
    static final int NONE = 0;
    /** {@link #step}: its blink is over: it bursts with its ring and its blast. */
    static final int BLAST = 1;
    /** {@link #step}: a neighbour's burst set it off: it bursts without a ring and sets off its own neighbours. */
    static final int CHAINED = 2;

    private final double[] blastX;
    private final double[] blastY;
    private final double[] blastRadius;
    private final double[] blastDamage;
    private int blasts;

    MineLines(int capacity) {
        blastX = new double[capacity];
        blastY = new double[capacity];
        blastRadius = new double[capacity];
        blastDamage = new double[capacity];
    }

    /** The step's blasts are spent: none wait. */
    void clearBlasts() {
        blasts = 0;
    }

    /**
     * One step of line mine {@code mine} after its move: a set-off counts down to its burst; an armed mine not yet
     * blinking starts to blink when the ship's centre (shipX, shipY) or Rook's (rookX, rookY) is within its proximity
     * (NaN: that craft does not trigger it now); a blink counts down. Returns {@link #NONE}, {@link #BLAST} or {@link
     * #CHAINED}; a blink's start adds its {@link SimEvents.Type#MINE_BLINK}.
     */
    int step(Mine mine, double shipX, double shipY, double rookX, double rookY, SimEvents events) {
        if (mine.setOff()) {
            return mine.chainDue() ? CHAINED : NONE;
        }
        if (mine.blinking()) {
            return mine.blinkOver() ? BLAST : NONE;
        }
        if (!mine.armed()) {
            return NONE;
        }
        double reach = mine.spec().line().orElseThrow().proximity();
        if (within(mine, shipX, shipY, reach) || within(mine, rookX, rookY, reach)) {
            mine.blink();
            events.add(SimEvents.Type.MINE_BLINK, mine.x(), mine.y());
            return mine.blinkOver() ? BLAST : NONE;
        }
        return NONE;
    }

    private static boolean within(Mine mine, double x, double y, double reach) {
        if (Double.isNaN(x)) {
            return false;
        }
        double dx = x - mine.x();
        double dy = y - mine.y();
        return dx * dx + dy * dy <= reach * reach;
    }

    /** Line mine {@code mine} blasts: its blast waits for the hit passes (none for a radius or damage of 0). */
    void blast(Mine mine) {
        EnemyGun.LineMine line = mine.spec().line().orElseThrow();
        if (!(line.blastRadius() > 0) || !(line.blastDamage() > 0) || blasts == blastX.length) {
            return;
        }
        blastX[blasts] = mine.x();
        blastY[blasts] = mine.y();
        blastRadius[blasts] = line.blastRadius();
        blastDamage[blasts] = line.blastDamage();
        blasts++;
    }

    /**
     * Line mine {@code from} burst: the other line mines within its chain reach that are not set off yet are set off,
     * bursting one hop from now; returns how many.
     */
    static int setOffNeighbours(Pool<Mine> mines, Mine from) {
        EnemyGun.LineMine line = from.spec().line().orElseThrow();
        double reach = line.chainRadius();
        if (!(reach > 0)) {
            return 0;
        }
        int hop = SimStep.ticks(line.hopSeconds());
        int set = 0;
        for (int j = 0; j < mines.size(); j++) {
            Mine other = mines.get(j);
            if (other == from || !other.line() || other.setOff()) {
                continue;
            }
            double dx = other.x() - from.x();
            double dy = other.y() - from.y();
            if (dx * dx + dy * dy <= reach * reach) {
                other.setOff(hop);
                set++;
            }
        }
        return set;
    }

    /** The blasts of this step waiting for the hit passes. */
    int blasts() {
        return blasts;
    }

    /**
     * The damage of this step's blasts on a hull at (x, y): each blast whose disc {@code hull} touches deals its
     * damage once.
     */
    double blastDamage(Hull hull, double x, double y) {
        double damage = 0;
        for (int k = 0; k < blasts; k++) {
            if (hull.touchesDisc(x, y, blastX[k], blastY[k], blastRadius[k])) {
                damage += blastDamage[k];
            }
        }
        return damage;
    }
}
