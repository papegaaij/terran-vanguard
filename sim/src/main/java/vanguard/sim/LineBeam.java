package vanguard.sim;

/**
 * M9 part A, one unit's {@code laser-line} (design/enemies/ground/rail-bunker; {@link EnemySpec.LaserLine}): the wait
 * for its next telegraph, the telegraph that locks a line's direction from the unit through the ship's position to the
 * screen's edge (lock, not track), the live line and whether it struck the ship. Round 66's C.1 b: the line's origin
 * rides with its unit (a bunker scrolls on with the ground under its telegraph and rail), only its direction is locked
 * ({@link #ride}). Round 69's B.2: the direction is led ({@link LineAim}): it runs from where the unit will be when the
 * line goes live (its move of the last step times the telegraph) through the ship, so the riding line passes through
 * the locked ship point at its live moment. Each {@link Enemy} owns one, used and hashed
 * only while its stat block has a line; {@link #stepAll} runs every unit's line after the moves and {@link
 * #strikes} tests the ship. Any unit may carry one (not only a hovering one, as the Mantis's sweep). Allocates nothing.
 */
public final class LineBeam {
    /** How far a line runs from its origin: past every edge of the play field from anywhere on it, px. */
    static final double LENGTH = 2 * (PlayField.WIDTH + PlayField.HEIGHT);

    /** Steps to the next telegraph; -1 before the unit is on the play field. */
    private int wait = -1;
    /** Steps into the current telegraph and live line; -1 while none runs. */
    private int ticks = -1;
    /** The running line: its origin on the screen (its unit's centre, riding with it) and its locked, led direction. */
    private double fromX;

    private double fromY;
    private final LineAim aim = new LineAim();
    /** Whether the running line is live (past its telegraph). */
    private boolean live;
    /** Whether the current line struck the ship. */
    private boolean hit;

    /** Back to a unit without a line (a new life). */
    void clear() {
        wait = -1;
        ticks = -1;
        fromX = 0;
        fromY = 0;
        aim.clear();
        live = false;
        hit = false;
    }

    /** Hashed only for a unit with a line. */
    void addTo(StateHash hash) {
        hash.add(wait)
                .add(ticks)
                .add(fromX)
                .add(fromY)
                .add(aim.dirX())
                .add(aim.dirY())
                .add(live ? 1 : 0)
                .add(hit ? 1 : 0);
    }

    /**
     * Round 66's C.1 b (design/enemies/ground/rail-bunker): a running line's origin follows its unit, the direction
     * stays locked; called each step after the moves, before the line's own step (a stunned unit's too: the ground
     * scrolls it on).
     */
    void ride(Enemy unit) {
        if (ticks >= 0) {
            fromX = unit.x();
            fromY = unit.y();
        }
    }

    /** The EMP Burst's stun: a running telegraph is cancelled (no shot); a live line runs out. */
    void cancel() {
        if (ticks >= 0 && !live) {
            ticks = -1;
        }
    }

    /**
     * One step of {@code unit}'s line with the ship at (shipX, shipY) (not while the EMP Burst stuns it): a running
     * telegraph or line goes on or ends; the wait runs while the unit is on the play field, and at its end a telegraph
     * starts unless the ship is within the hold distance (then it waits for the next step). Round 69's B.2: the line
     * locks led ({@link LineAim}): from the unit where its last step's move carries it over the telegraph.
     */
    void step(Enemy unit, EnemySpec.LaserLine line, double shipX, double shipY, SimEvents events) {
        step(unit, line, shipX, shipY, events, true);
    }

    /**
     * As {@link #step(Enemy, EnemySpec.LaserLine, double, double, SimEvents)}; M9 part F: a due telegraph starts only
     * when {@code mayStart} (the gate of {@link EnemySpec.LaserLine#mostTelegraphing()}), else it waits for the next
     * step. Returns whether a telegraph started.
     */
    boolean step(Enemy unit, EnemySpec.LaserLine line, double shipX, double shipY, SimEvents events, boolean mayStart) {
        int telegraph = SimStep.ticks(line.telegraphSeconds());
        int total = telegraph + Math.max(1, SimStep.ticks(line.liveSeconds()));
        if (ticks >= 0) {
            ticks++;
            if (ticks == telegraph) {
                live = true;
                events.add(SimEvents.Type.LINE_FIRED, unit.x(), unit.y(), unit.kind());
            }
            if (ticks >= total) {
                ticks = -1;
                live = false;
            }
        }
        boolean onField = PlayField.overlaps(unit.x(), unit.y(), unit.hitbox());
        if (wait < 0) {
            if (!onField) {
                return false;
            }
            // Its first delay counts from the step it is on the play field.
            wait = SimStep.ticks(line.firstDelaySeconds());
        }
        if (wait > 0) {
            wait--;
        }
        if (wait > 0 || ticks >= 0 || !onField || !mayStart) {
            return false;
        }
        double dx = shipX - unit.x();
        double dy = shipY - unit.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance <= line.holdWithinPx() || distance == 0) {
            return false;
        }
        fromX = unit.x();
        fromY = unit.y();
        // Round 69's B.2: it rides on over the telegraph (a bunker scrolls with the ground): aim from where it fires.
        aim.lock(
                fromX + (unit.renderX(1) - unit.renderX(0)) * telegraph,
                fromY + (unit.renderY(1) - unit.renderY(0)) * telegraph,
                fromX,
                fromY,
                shipX,
                shipY);
        ticks = 0;
        live = false;
        hit = false;
        wait = SimStep.ticks(line.intervalSeconds());
        events.add(SimEvents.Type.LINE_TELEGRAPH, unit.x(), unit.y(), unit.kind());
        return true;
    }

    /**
     * One step of every unit's line in {@code enemies} with the ship at (shipX, shipY), after their moves: a running
     * line's origin rides with its unit ({@link #ride}); a stunned unit's clock stops. M9 part F (design/enemies/ground/rail-bunker, never more than 2 lines telegraphed at once): a
     * line with a {@link EnemySpec.LaserLine#mostTelegraphing()} gate starts its due telegraph only while fewer lines
     * than that telegraph (counted after this step's running lines went on, in the pool's order); else it waits.
     */
    static void stepAll(Pool<Enemy> enemies, double shipX, double shipY, SimEvents events) {
        int telegraphing = -1;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.spec().laserLine().isPresent()
                    && unit.spec().laserLine().get().mostTelegraphing() > 0) {
                telegraphing = telegraphingAfterStep(enemies);
                break;
            }
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.spec().laserLine().isPresent()) {
                unit.lineBeam.ride(unit);
            }
            if (unit.spec().laserLine().isPresent() && !unit.empStunned()) {
                EnemySpec.LaserLine line = unit.spec().laserLine().get();
                boolean mayStart = line.mostTelegraphing() <= 0 || telegraphing < line.mostTelegraphing();
                if (unit.lineBeam.step(unit, line, shipX, shipY, events, mayStart)) {
                    telegraphing++;
                }
            }
        }
    }

    /** M9 part F: the lines that telegraph after this step's running ones went on (a stunned unit's stands still). */
    private static int telegraphingAfterStep(Pool<Enemy> enemies) {
        int count = 0;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.spec().laserLine().isEmpty() || !unit.lineBeam.telegraphing()) {
                continue;
            }
            int next = unit.lineBeam.ticks + (unit.empStunned() ? 0 : 1);
            if (next < SimStep.ticks(unit.spec().laserLine().get().telegraphSeconds())) {
                count++;
            }
        }
        return count;
    }

    /** Whether the line is live and has not struck yet, and {@code hull} at (x, y) touches its band. */
    boolean strikes(EnemySpec.LaserLine line, Hull hull, double x, double y) {
        return live && !hit && touches(line, hull, x, y);
    }

    /** Whether the line is live and {@code hull} at (x, y) touches its band (a drone's laser test). */
    boolean touches(EnemySpec.LaserLine line, Hull hull, double x, double y) {
        return live
                && hull.touchesSegment(
                        x,
                        y,
                        fromX,
                        fromY,
                        fromX + aim.dirX() * LENGTH,
                        fromY + aim.dirY() * LENGTH,
                        line.bandPx() / 2);
    }

    /** The line struck the ship: once per line. */
    void markHit() {
        hit = true;
    }

    /** Whether a telegraph or a live line runs now. */
    public boolean running() {
        return ticks >= 0;
    }

    /** Whether the line is in its telegraph now (the thin red line). */
    public boolean telegraphing() {
        return ticks >= 0 && !live;
    }

    /** Whether the line is live now (the rail's flash). */
    public boolean live() {
        return live;
    }

    /** How far the telegraph has run, 0 to 1 (its brightening); 1 once live, 0 while none runs. */
    public double charge(EnemySpec.LaserLine line) {
        if (ticks < 0) {
            return 0;
        }
        return live ? 1 : Math.min(1, (double) ticks / Math.max(1, SimStep.ticks(line.telegraphSeconds())));
    }

    /** The running line's origin, px on the screen: its unit's centre (it rides with the unit). */
    public double fromX() {
        return fromX;
    }

    public double fromY() {
        return fromY;
    }

    /**
     * The locked line's direction (a unit vector, y up the screen), led for the unit's move over the telegraph (round
     * 69's B.2); it runs to the screen's edge.
     */
    public double dirX() {
        return aim.dirX();
    }

    public double dirY() {
        return aim.dirY();
    }
}
