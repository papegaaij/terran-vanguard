package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 31 (M9 part D, step D4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot): the timed lines, the tug's first section hit and a section lost (their
 * {@code {section}} the engines), Dacosta's claim (RadioRuns#level31Runs), and the closing line at t=176.5.
 */
class RadioTimelineLevel31Test extends RadioTimeline {
    RadioTimelineLevel31Test() {
        super(LEVEL_31);
    }

    /**
     * Step FIX1: Dacosta's claim breaks at t≈101–107 (the plan's beam frees it at t≈101, a bolt gun at t≈100.5), inside
     * the story pair's Varga line; the Bellwether's secret line still plays (it is not dropped as stale) in every claim
     * run, voiced or not, with every fit and side.
     */
    @Test
    void theClaimsLinePlaysInEveryClaimRun() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                if (events.stream().noneMatch(e -> e.trigger() == CueTrigger.SECRET)) {
                    continue;
                }
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        for (String side : SIDES) {
                            List<Played> played =
                                    play(script, events, fitted, voiced, Double.NaN, new double[1], side, 6);
                            boolean opened = played.stream()
                                    .filter(p -> p.cue().trigger() == CueTrigger.SECRET)
                                    .anyMatch(p -> p.opened().isPresent());
                            StringBuilder around = new StringBuilder();
                            for (Played p : played) {
                                if (p.opened().isPresent()
                                        && p.opened().get() > 80
                                        && p.opened().get() < 125) {
                                    around.append(String.format(
                                            " [%s %s opened %.1f]",
                                            p.cue().trigger(),
                                            p.cue().t(),
                                            p.opened().get()));
                                }
                            }
                            assertTrue(
                                    opened,
                                    difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted
                                            + ": the claim's line plays;" + around);
                        }
                    }
                }
            }
        }
    }
}
