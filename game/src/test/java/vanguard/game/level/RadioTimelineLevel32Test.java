package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 32 (M9 part E, step E4), per route and with and without the escort (every
 * run is played with each fit: Rook flies in the escort slot): the timed lines (step E2 trimmed each to one HUD page),
 * fork one's side line (RadioRuns#level32Runs: the left and the right channel), the first topple crush, the secondary
 * met at the fourth tower, and the closing line at t=175. Its lines are voiced (step VOX-E).
 */
class RadioTimelineLevel32Test extends RadioTimeline {
    RadioTimelineLevel32Test() {
        super(LEVEL_32);
    }

    /** Fork one's choice plays its own side's line, never the other's, voiced or not, on every route and difficulty. */
    @Test
    void theForksLinePlaysForTheSideTaken() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                List<String> taken = events.stream()
                        .filter(e -> e.trigger() == CueTrigger.BRANCH)
                        .map(Event::subject)
                        .toList();
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        List<String> spoken = played.stream()
                                .filter(p -> p.cue().trigger() == CueTrigger.BRANCH
                                        && p.opened().isPresent())
                                .map(p -> p.cue().subject())
                                .toList();
                        assertEquals(
                                taken,
                                spoken,
                                difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted
                                        + ": the side taken has its line");
                        assertTrue(
                                played.stream()
                                        .filter(p -> p.cue().trigger() == CueTrigger.BRANCH)
                                        .allMatch(p -> taken.contains(p.cue().subject())
                                                || p.opened().isEmpty()),
                                "no other side's line");
                    }
                }
            }
        }
    }
}
