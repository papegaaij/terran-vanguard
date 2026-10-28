package vanguard.game.level;

/**
 * {@link RadioTimeline}'s checks for Level 31 (M9 part D, step D4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot): the timed lines, the tug's first section hit and a section lost (their
 * {@code {section}} the engines), Dacosta's claim (RadioRuns#level31Runs), and the closing line at t=176.5.
 */
class RadioTimelineLevel31Test extends RadioTimeline {
    RadioTimelineLevel31Test() {
        super(LEVEL_31);
    }
}
