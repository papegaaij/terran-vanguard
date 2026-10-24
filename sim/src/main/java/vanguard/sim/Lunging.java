package vanguard.sim;

/**
 * M8 part F: one unit's lunge from its lure (design/enemies/naval/glow-angler; {@link EnemySpec.Lunge}; m8f-gaps.md
 * §1.1, §5 F2), modelled on C's {@link Ramming}. It hangs drifting with the water (scrolling with the ground, its drift
 * on top); armed and on the play field with the ship within its trigger it lunges ({@link SimEvents.Type#LURE_LUNGE})
 * straight at where the ship was then, stopping short of that point or at its time; its ring is then due (fired by
 * {@link EnemyFire#lure}, {@link SimEvents.Type#LURE_RING}) and it retreats away from the ship, then drifts again. It
 * re-arms its re-arm time after its ring, or after its retreat when the ring never found room. The lunge and the
 * retreat run in the screen's frame (no scroll). Each {@link Enemy} owns one, used only while its stat block has a lunge
 * (hashed only then). Allocation-free. Not for a flipped scroll (no level reverses with one).
 */
final class Lunging {
    private EnemySpec.Lunge lunge;
    private LurePhase phase = LurePhase.NONE;
    /** Steps into its lunge or its retreat. */
    private int ticks;
    /** Steps until it may lunge again; 0 armed. */
    private int rearm;
    /** Whether its ring is due (from its lunge's end until it fires or its retreat ends). */
    private boolean ringDue;
    /** The point its lunge aims at: where the ship was when it triggered. */
    private double toX;

    private double toY;
    /** Its retreat's velocity, px/s (y up). */
    private double vx;

    private double vy;

    /** Starts a unit with {@code spec}: drifting, armed. */
    void start(EnemySpec.Lunge spec) {
        clear();
        lunge = spec;
        phase = LurePhase.DRIFT;
    }

    /** Back to a unit without a lunge. */
    void clear() {
        lunge = null;
        phase = LurePhase.NONE;
        ticks = 0;
        rearm = 0;
        ringDue = false;
        toX = toY = 0;
        vx = vy = 0;
    }

    /**
     * One step of {@code u}, the ship at (shipX, shipY), the ground scrolling by {@code groundScroll}: its re-arm clock,
     * then its drift (and its trigger), its lunge or its retreat. Returns false once it left the play field by a side
     * or the bottom edge.
     */
    boolean step(Enemy u, double shipX, double shipY, double groundScroll) {
        if (rearm > 0) {
            rearm--;
        }
        switch (phase) {
            case LUNGE -> lungeStep(u, shipX, shipY);
            case RETREAT -> {
                u.x += vx * SimStep.SECONDS;
                u.y += vy * SimStep.SECONDS;
                u.facing = Enemy.heading(shipX - u.x, shipY - u.y);
                if (++ticks >= SimStep.ticks(lunge.retreatSeconds())) {
                    phase = LurePhase.DRIFT;
                    ticks = 0;
                    if (ringDue) {
                        // The ship never left it room for its ring: none, the re-arm runs from now.
                        ringDue = false;
                        rearm = SimStep.ticks(lunge.rearmSeconds());
                    }
                }
            }
            default -> {
                u.y -= groundScroll;
                u.x += u.driftX * SimStep.SECONDS;
                u.y += u.driftY * SimStep.SECONDS;
                if (rearm == 0 && !ringDue && PlayField.overlaps(u.x, u.y, u.box)) {
                    double dx = shipX - u.x;
                    double dy = shipY - u.y;
                    if (dx * dx + dy * dy <= lunge.trigger() * lunge.trigger()) {
                        phase = LurePhase.LUNGE;
                        ticks = 0;
                        toX = shipX;
                        toY = shipY;
                        u.facing = Enemy.heading(dx, dy);
                        u.events.add(SimEvents.Type.LURE_LUNGE, u.x, u.y, u.kind);
                    }
                }
            }
        }
        double halfW = u.box.width() / 2;
        return u.x + halfW > 0 && u.x - halfW < PlayField.WIDTH && u.y + u.box.height() / 2 > 0;
    }

    /** One step of its dive toward its point, ending {@link EnemySpec.Lunge#stopShort()} short of it or at its time. */
    private void lungeStep(Enemy u, double shipX, double shipY) {
        double dx = toX - u.x;
        double dy = toY - u.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double left = distance - lunge.stopShort();
        double step = lunge.speed() * SimStep.SECONDS;
        if (left > 0 && distance > 0) {
            double move = Math.min(step, left);
            u.x += dx / distance * move;
            u.y += dy / distance * move;
            u.facing = Enemy.heading(dx, dy);
        }
        if (left <= step || ++ticks >= SimStep.ticks(lunge.seconds())) {
            // Its dive is over: its ring is due and it backs away from where the ship is now.
            phase = LurePhase.RETREAT;
            ticks = 0;
            ringDue = true;
            double ax = u.x - shipX;
            double ay = u.y - shipY;
            double away = Math.sqrt(ax * ax + ay * ay);
            if (away == 0) {
                ax = 0;
                ay = 1;
                away = 1;
            }
            vx = ax / away * lunge.retreatSpeed();
            vy = ay / away * lunge.retreatSpeed();
        }
    }

    /** Whether its ring is due now (it waits for room while the ship is too close, within its retreat). */
    boolean ringDue() {
        return ringDue;
    }

    /** Its ring fired: its re-arm starts. */
    void ringFired() {
        ringDue = false;
        rearm = SimStep.ticks(lunge.rearmSeconds());
    }

    LurePhase phase() {
        return phase;
    }

    /** Hashed only for a unit whose stat block has a lunge. */
    void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(ticks)
                .add(rearm)
                .add(ringDue ? 1 : 0)
                .add(toX)
                .add(toY)
                .add(vx)
                .add(vy);
    }
}
