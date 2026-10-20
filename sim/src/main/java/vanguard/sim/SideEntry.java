package vanguard.sim;

/**
 * M5 part G (design/campaign Level 13, Ground targets): a ground unit's entry at a side edge: it
 * appears just outside the left ({@code fromLeft}) or the right edge at {@code y} px from the bottom
 * edge (y up), drifts across toward the other edge at {@code drift} px/s on top of the scroll, and is
 * gone once it has drifted off the other edge or scrolled off the bottom one. Its edge is warned
 * {@link WaveSchedule#EDGE_WARNING_SECONDS} ahead, as a side wave's.
 */
public record SideEntry(boolean fromLeft, double y, double drift) {
    public SideEntry {
        if (!(drift > 0) || !(y >= 0 && y <= PlayField.HEIGHT)) {
            throw new IllegalArgumentException("a side entry drifts across the play field: y " + y + ", " + drift);
        }
    }
}
