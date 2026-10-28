package vanguard.game.level;

/**
 * {@link RadioTimeline}'s checks for Level 30 (M9 part C, step C4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot; the Bulwark and {@code --escort none} have no radio, as the fits
 * without him): the timed lines (step C2 trimmed each to one HUD page), Rook's t=123.5 bark only with him flying, the
 * convoy secondary met or failed (RadioRuns#level30Runs) beside it, and the closing line. Its lines are voiced (step C2).
 */
class RadioTimelineLevel30Test extends RadioTimeline {
    RadioTimelineLevel30Test() {
        super(LEVEL_30);
    }
}
