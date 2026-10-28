package vanguard.sim;

/**
 * M9 part B (design/player/wingmen, Rook's rescue and return; design/campaign Level 29; m9b-gaps.md §1.7): the level's
 * mid-level escort swap. When the last {@link GroundUnit#holder() holder} of the level's arena falls the swap
 * starts: the craft in the escort slot's hold launches from ({@code x}, {@code y}) (px from the left, px up from the
 * bottom edge: the prison tender's hold on the halted screen); a rescued Rook ({@link Loadout#rescue()}) flies from
 * there into his slot over {@code joinSeconds}, the Bulwark in the slot (the loaner or an owned one) peels away to its
 * side's edge at its speed and is gone; with Rook flying from the start the hold opens empty, and with neither the
 * Ember launches and leaves upward (the look's; nobody joins). A retry replays it.
 */
public record EscortSwapSpec(double x, double y, double joinSeconds) {
    public EscortSwapSpec {
        if (!(x >= 0 && x <= PlayField.WIDTH) || !(y >= 0 && y <= PlayField.HEIGHT) || !(joinSeconds > 0)) {
            throw new IllegalArgumentException("an escort swap launches on the screen and joins over a time: (" + x
                    + ", " + y + "), " + joinSeconds + " s");
        }
    }
}
