package vanguard.sim;

/**
 * M8 part D: a hover held above the ship (design/enemies/naval/barbfin; {@link EnemySpec.Hover#aboveShip()}): the
 * height above the ship it drew by its own seed, where it stops coming down its path, and while it holds its drift
 * sideways (turning back at the side edges) and its facing turned toward the ship. Each {@link Enemy} owns one, active
 * only for a unit whose hover has a height above the ship (its fields are hashed only then). Allocation-free.
 */
final class HoverHold {
    /** The most its facing turns toward the ship in one step: one step of a 32-angle set. */
    private static final double MAX_TURN = StrictMath.PI / 16;

    private boolean active;
    /**
     * M8 part G (design/enemies/naval/depth-hunter): a hover at its depth with a drift but no height above the ship: it
     * stops at its path's end, drifts sideways while it holds (turning back at the side edges) and turns its facing up
     * the screen. Not {@link #active()}.
     */
    private boolean driftOnly;
    /** How far above the ship's centre it holds, px (its own draw). */
    private double above;
    /** Its drift's direction: 1 to the right, -1 to the left. */
    private int drift = 1;
    /**
     * M8 part E: how far along its path it must be before it may stop (a cave's unit, out of its notch); 0 for any
     * other. Not hashed: it follows from its wave's plan, as its path does.
     */
    private double from;
    /**
     * M8 part E: the level's rock and scroll its drift keeps off (null without rock). Not hashed: the level's, set by
     * its force each step.
     */
    private Canyon canyon;

    /** Back to a unit without a hover above the ship. */
    void clear() {
        active = false;
        driftOnly = false;
        above = 0;
        drift = 1;
        from = 0;
    }

    /** M8 part E: its drift keeps off {@code rock} (null: the level has none). */
    void canyon(Canyon rock) {
        canyon = rock;
    }

    /** M8 part E: it may stop only {@code distance} px along its path and beyond (a cave's unit, out of its notch). */
    void notBefore(double distance) {
        from = distance;
    }

    /** It starts with {@code spec}'s hover, drawing its height and its drift's side by its own {@code serial}. */
    void start(EnemySpec spec, int serial) {
        clear();
        if (spec.hover().isPresent()
                && spec.hover().get().aboveShip().isEmpty()
                && spec.hover().get().drift() > 0) {
            // M8 part G: the Depth Hunter's drift at its depth (its side by its own serial, as a Barbfin's).
            driftOnly = true;
            drift = Ramming.draw(0xBB67AE8584CAA73BL ^ serial) < 0.5 ? -1 : 1;
            return;
        }
        if (spec.hover().isEmpty() || spec.hover().get().aboveShip().isEmpty()) {
            return;
        }
        active = true;
        above = spec.hover().get().aboveShip().get().at(Ramming.draw(0x6A09E667F3BCC909L ^ serial));
        drift = Ramming.draw(0xBB67AE8584CAA73BL ^ serial) < 0.5 ? -1 : 1;
    }

    boolean active() {
        return active;
    }

    /** M8 part G: whether it drifts at its depth while it holds (a hover with a drift, not above the ship). */
    boolean driftsOnly() {
        return driftOnly;
    }

    /** M8 part G: its drift's side from now, -1 left or 1 right (a column's units fan out). */
    void driftSide(int side) {
        drift = side < 0 ? -1 : 1;
    }

    /** Its drift's side now, -1 left or 1 right. */
    int driftSide() {
        return drift;
    }

    /**
     * M8 part G: one step of {@code u}'s hold at its depth: it drifts sideways at the hover's drift (turning back at the
     * side edges, as {@link #hold}) and turns its facing up the screen (its torpedoes leave along it).
     */
    void driftUp(Enemy u) {
        driftStep(u);
        double delta = Math.IEEEremainder(StrictMath.PI - u.facing, 2 * StrictMath.PI);
        u.facing = Math.IEEEremainder(u.facing + Math.clamp(delta, -MAX_TURN, MAX_TURN), 2 * StrictMath.PI);
    }

    /** How far above the ship's centre it holds, px. */
    double above() {
        return above;
    }

    /**
     * The height (px from the bottom edge) it holds at, the ship's centre at {@code shipY}: its height above the ship,
     * never higher than {@code hover}'s depth's min below the top edge.
     */
    double holdY(EnemySpec.Hover hover, double shipY) {
        return Math.min(shipY + above, PlayField.HEIGHT - hover.depth().min());
    }

    /**
     * Whether {@code u}, coming down its path on the play field, stops now: its centre came down to its hold height
     * above the ship at (shipX, shipY). M8 part G (design/campaign Level 27, the rear Barbfin pairs): a unit that entered
     * from the rear stops coming up instead, its centre risen to its hold height.
     */
    boolean stopsNow(Enemy u, double shipY) {
        if (!active || u.distance < from || !PlayField.overlaps(u.x, u.y, u.box)) {
            return false;
        }
        double holdY = holdY(u.spec.hover().orElseThrow(), shipY);
        if (u.entry() == WaveSpec.Entry.REAR) {
            return u.y > u.prevY && u.y >= holdY;
        }
        return u.y < u.prevY && u.y <= holdY;
    }

    /**
     * One step of {@code u}'s hold: it drifts sideways at the hover's drift (turning back at the side edges, its hit
     * box kept on the play field) and turns its facing toward the ship at (shipX, shipY). M8 part E: in a level with
     * rock it turns back at the flyable span's edges too ({@link Terrain#spanLeft}, {@link Terrain#spanRight}: its hit
     * box kept off the walls and islands where the span holds it), and as the walls scroll in on it, it is pushed off
     * them ({@link Canyon#inSpan}; laid out at a span's edge, it eases off to its half width).
     */
    void hold(Enemy u, double shipX, double shipY) {
        driftStep(u);
        double dx = shipX - u.x;
        double dy = shipY - u.y;
        if (dx != 0 || dy != 0) {
            double delta = Math.IEEEremainder(Enemy.heading(dx, dy) - u.facing, 2 * StrictMath.PI);
            u.facing = Math.IEEEremainder(u.facing + Math.clamp(delta, -MAX_TURN, MAX_TURN), 2 * StrictMath.PI);
        }
    }

    /** One step of {@code u}'s drift sideways while it holds (see {@link #hold}). */
    private void driftStep(Enemy u) {
        double speed = u.spec.hover().orElseThrow().drift();
        if (speed > 0) {
            double half = u.box.width() / 2;
            double lo = half;
            double hi = PlayField.WIDTH - half;
            if (canyon != null) {
                double left = canyon.terrain.spanLeft(canyon.scroll(), u.y, u.x);
                double right = canyon.terrain.spanRight(canyon.scroll(), u.y, u.x);
                if (!Double.isNaN(left)) {
                    lo = Math.max(lo, left + half);
                }
                if (!Double.isNaN(right)) {
                    hi = Math.min(hi, right - half);
                }
            }
            double x = u.x + drift * speed * SimStep.SECONDS;
            if (x < lo && drift < 0 || x > hi && drift > 0) {
                drift = -drift;
                x = u.x;
            }
            // M8 AW: pushed off the walls and islands as they scroll in on it (Level 25's narrows).
            u.x = canyon == null ? x : canyon.inSpan(u.y, x, u.x, half);
        }
    }

    /**
     * M8 AW: one step of {@code u} leaving after its hold (down and out to its side): in a level with rock its path
     * is kept inside the flyable span it leaves from ({@link Canyon#inSpan}), so it slides down along a wall instead
     * of flying into it. Without rock nothing changes.
     */
    void leaving(Enemy u) {
        if (active && canyon != null) {
            u.x = canyon.inSpan(u.y, u.x, u.prevX, u.box.width() / 2);
        }
    }

    /** Hashed only while active (M8 part G: or drifting at its depth). */
    void addTo(StateHash hash) {
        hash.add(above).add(drift);
    }
}
