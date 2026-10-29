package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 34 (M9 part G, step G4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot): the timed lines (step G2 moved Rook's t=128 to 130 and trimmed three
 * to one HUD page), the three secrets' lines, the Helix core collected or missed, the secondary met
 * (RadioRuns#level34Runs). Its 16 lines are voiced (step G2).
 */
class RadioTimelineLevel34Test extends RadioTimeline {
    private static final Set<CueTrigger> CORE = Set.of(CueTrigger.STORY_COLLECTED, CueTrigger.STORY_MISSED);

    RadioTimelineLevel34Test() {
        super(LEVEL_34);
    }

    /** *Old Marta*'s line is Rook's: it plays only while he flies (requires: escort); Okafor's and Varga's always. */
    @Test
    void oldMartasLineNeedsRookTheOthersAlwaysPlay() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                if (events.stream().noneMatch(e -> e.trigger() == CueTrigger.SECRET)) {
                    continue;
                }
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        boolean rook = (fitted & ESCORT) != 0;
                        for (Played line : played) {
                            if (line.cue().trigger() != CueTrigger.SECRET
                                    || events.stream()
                                            .noneMatch(e -> e.trigger() == CueTrigger.SECRET
                                                    && e.subject()
                                                            .equals(line.cue().subject()))) {
                                continue;
                            }
                            boolean expected = !line.cue().speaker().equals("Rook") || rook;
                            assertTrue(
                                    line.opened().isPresent() == expected,
                                    difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted + ": "
                                            + line.cue().speaker() + "'s secret line "
                                            + (expected ? "plays" : "waits for Rook"));
                        }
                    }
                }
            }
        }
    }

    /**
     * The core's line (Okafor's, collected or missed) finds its gap whenever it falls: from its first chance (t≈110) to
     * the last that fits before Rook's t=130 (t≈122.8), text and voiced, with and without him.
     */
    @Test
    void theCoresLinePlaysCollectedOrMissed() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                if (events.stream().noneMatch(e -> CORE.contains(e.trigger()))) {
                    continue;
                }
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        assertTrue(
                                played.stream()
                                        .anyMatch(p -> CORE.contains(p.cue().trigger())
                                                && p.opened().isPresent()),
                                difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted
                                        + ": the core's line plays");
                    }
                }
            }
        }
    }
}
