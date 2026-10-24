package vanguard.sim;

/**
 * M8 part F: a level's scripted loss of Rook (design/player/wingmen, Rook's loss; design/campaign Level 26, the level's
 * {@code rook_loss} key; m8f-gaps.md §1.2, §5 F6, F7): at {@code trails} s on the level clock his trails start and his
 * barks end (he no longer counts as flying for the radio); at {@code t} s he stops firing and taking hits and spirals
 * down off the bottom edge in {@code spin} s, then is gone for the attempt. Not an eject (no pod, no repair). It acts
 * only on Rook flying in the escort slot (not ejected): ejected, without an escort or with the Bulwark the loss is the
 * radio's only. A retry replays it.
 */
public record RookLossSpec(double t, double trails, double spin) {
    public RookLossSpec {
        if (!(trails >= 0) || !(t >= trails) || !(spin > 0)) {
            throw new IllegalArgumentException("Rook's loss starts its trails before it: trails " + trails + " s, t "
                    + t + " s, spin " + spin + " s");
        }
    }
}
