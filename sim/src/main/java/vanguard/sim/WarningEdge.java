package vanguard.sim;

/** A play-field edge that shows an edge warning before a wave enters there. */
public enum WarningEdge {
    LEFT,
    RIGHT,
    BOTTOM,
    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): the top edge, for an entry from the top while the scroll
     * runs reversed (behind the ship by travel: a rear wave, a re-entry, the pursuit swarms); bit 3, after the others,
     * so their bits do not move.
     */
    TOP;

    /** The bit of this edge in {@link Sortie#edgeWarnings()}. */
    public int bit() {
        return 1 << ordinal();
    }

    /** Whether this edge is in a set of warning bits. */
    public boolean in(int edges) {
        return (edges & bit()) != 0;
    }
}
