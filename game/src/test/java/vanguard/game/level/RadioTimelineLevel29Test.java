package vanguard.game.level;

/**
 * {@link RadioTimeline}'s checks for Level 29 (M9 part B, steps B4 and B4b), with and without the escort (every run is
 * played with each fit; Rook's lines are scripted, the Bulwark has no radio): the timed lines through the turn, Harker
 * on the enemy channel, the wavering pairs' lines, the rescue halt on its held clock (the last clamp's line for the
 * swap's outcome, Rook's rescue line) and the closing line. The runs stand for the loaned Bulwark, an owned one, the
 * loaner lost before the swap and {@code --escort none} (RadioRuns#act5). Step B4b trimmed the lines to one HUD page
 * each and retimed four (pending round 62), so the timing checks run.
 */
class RadioTimelineLevel29Test extends RadioTimeline {
    RadioTimelineLevel29Test() {
        super(LEVEL_29);
    }
}
