package vanguard.sim;

/**
 * M9 part B (design/enemies, Rock riding; design/enemies/space/asteroid-mite; m9b-gaps.md §1.5): one rider's state on
 * its rock. Each {@link Enemy} owns one, idle for any other unit. {@link RockRides} places a riding unit ({@link
 * Enemy.Phase#RIDE}) each step after the rocks moved: on its rock's rim while it clings, tumbling off a broken rock,
 * holding still in its tell, then it leaps (a straight dive at the ship, {@link Enemy.Phase#LEAVE}). Hashed only for a
 * rider.
 */
final class Riding {
    /** It clings to its rock's rim. */
    static final int CLING = 0;
    /** Its rock broke: it tumbles off. */
    static final int TUMBLE = 1;
    /** Its tell before the leap. */
    static final int TELL = 2;

    private final Enemy unit;
    /** Whether it entered as a rider. */
    private boolean rider;
    /** Its rock's number in the level's plan ({@link RockRides}). */
    int rock;
    /** Its place on the rim, radians (y up, from the right). */
    double angle;
    /** Its turn in its rock's leap order (from 0). */
    int order;
    /** {@link #CLING}, {@link #TUMBLE} or {@link #TELL}. */
    int state;
    /** Steps in its state so far. */
    int ticks;
    /** Steps it waits (clinging or tumbled off) before its tell starts; -1 while no leap is due. */
    int waitTicks = -1;
    /** Its rock left the screen with it: it is gone (it pays nothing). */
    boolean carriedOff;

    Riding(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit that rides nothing. */
    void clear() {
        rider = false;
        rock = -1;
        angle = 0;
        order = 0;
        state = CLING;
        ticks = 0;
        waitTicks = -1;
        carriedOff = false;
    }

    /** It boards rock {@code number} at {@code onRim} radians, {@code turn}-th in its leap order. */
    void board(int number, double onRim, int turn) {
        clear();
        rider = true;
        rock = number;
        angle = onRim;
        order = turn;
        unit.phase = Enemy.Phase.RIDE;
    }

    /** Whether it entered as a rider (on its rock, or leaping off it). */
    boolean rider() {
        return rider;
    }

    /** Whether it still rides (clings, tumbles or tells): not leaping yet. */
    boolean riding() {
        return rider && unit.phase == Enemy.Phase.RIDE;
    }

    /** Hashed only for a rider, so other units hash as before. */
    void addTo(StateHash hash) {
        if (rider) {
            hash.add(rock)
                    .add(angle)
                    .add(order)
                    .add(state)
                    .add(ticks)
                    .add(waitTicks)
                    .add(carriedOff ? 1 : 0);
        }
    }
}
