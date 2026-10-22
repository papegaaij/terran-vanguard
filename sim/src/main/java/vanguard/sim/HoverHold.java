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
    /** How far above the ship's centre it holds, px (its own draw). */
    private double above;
    /** Its drift's direction: 1 to the right, -1 to the left. */
    private int drift = 1;

    /** Back to a unit without a hover above the ship. */
    void clear() {
        active = false;
        above = 0;
        drift = 1;
    }

    /** It starts with {@code spec}'s hover, drawing its height and its drift's side by its own {@code serial}. */
    void start(EnemySpec spec, int serial) {
        clear();
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
     * above the ship at (shipX, shipY).
     */
    boolean stopsNow(Enemy u, double shipY) {
        return active
                && u.y < u.prevY
                && PlayField.overlaps(u.x, u.y, u.box)
                && u.y <= holdY(u.spec.hover().orElseThrow(), shipY);
    }

    /**
     * One step of {@code u}'s hold: it drifts sideways at the hover's drift (turning back at the side edges, its hit
     * box kept on the play field) and turns its facing toward the ship at (shipX, shipY).
     */
    void hold(Enemy u, double shipX, double shipY) {
        double speed = u.spec.hover().orElseThrow().drift();
        if (speed > 0) {
            double half = u.box.width() / 2;
            double x = u.x + drift * speed * SimStep.SECONDS;
            if (x < half && drift < 0 || x > PlayField.WIDTH - half && drift > 0) {
                drift = -drift;
                x = u.x;
            }
            u.x = x;
        }
        double dx = shipX - u.x;
        double dy = shipY - u.y;
        if (dx != 0 || dy != 0) {
            double delta = Math.IEEEremainder(Enemy.heading(dx, dy) - u.facing, 2 * StrictMath.PI);
            u.facing = Math.IEEEremainder(u.facing + Math.clamp(delta, -MAX_TURN, MAX_TURN), 2 * StrictMath.PI);
        }
    }

    /** Hashed only while active. */
    void addTo(StateHash hash) {
        hash.add(above).add(drift);
    }
}
