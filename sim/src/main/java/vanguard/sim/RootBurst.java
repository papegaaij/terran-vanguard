package vanguard.sim;

/**
 * M5 part H, a root burst (design/campaign Level 14, Hazards; design/enemies, Boss attacks and root
 * bursts; decision H2 = a): at {@code t} the paving cracks and dust rises for {@code
 * telegraphSeconds} at the ground point ({@code x}, {@code depth} px below the top edge at {@code
 * t}), which scrolls with the ground from then on; then its growths ({@link GroundUnit#fromBurst})
 * stand there and scroll with the ground. A growth takes no damage and fires nothing for {@code
 * firstShotSeconds} after it appeared, then fires as soon as its gun can (its own first-shot delay is
 * not read). A burst whose centre lies within {@link #SHIP_CLEARANCE} px of the ship's x at its
 * telegraph moves sideways, away from the ship, to that distance (to the other side when the field
 * has no room there): its growths with it. No hit and no collision. The units of one burst share its
 * {@code index}, which its events carry.
 *
 * @param index the burst's number in its level, from 0
 */
public record RootBurst(int index, double t, double x, double depth, double telegraphSeconds, double firstShotSeconds) {
    /** A burst lies at least this far below the top edge at its telegraph, px. */
    public static final double MIN_DEPTH = 100;
    /** ... and at most this far, px. */
    public static final double MAX_DEPTH = 400;
    /** Its centre lies at least this far from the ship's x at its telegraph, px. */
    public static final double SHIP_CLEARANCE = 60;

    public RootBurst {
        if (index < 0
                || !(t >= 0)
                || !(x >= 0 && x <= PlayField.WIDTH)
                || !(depth >= MIN_DEPTH && depth <= MAX_DEPTH)
                || !(telegraphSeconds >= 0)
                || !(firstShotSeconds >= 0)) {
            throw new IllegalArgumentException("a root burst lies on the field "
                    + MIN_DEPTH
                    + "–"
                    + MAX_DEPTH
                    + " px below the top edge with a telegraph and a growth time: burst "
                    + index
                    + " at depth "
                    + depth);
        }
    }
}
