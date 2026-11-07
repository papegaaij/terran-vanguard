package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.content.voice.VoiceLines;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 47 (M11 part E, step E2), with and without the escort (every run is played
 * with each fit; RadioRuns#level47Runs): the timed lines, Rook's barks (the first Seraph killed, the t=58 Wraith line)
 * only with him, his t=12 and t=250 lines with every fit, the pearl's line, the Seraphs' secondary met or failed. Its
 * lines are voiced at the radio's noise step 5 (step VOX-EF); the Choir's two lines are clean; Vorne is silent.
 */
class RadioTimelineLevel47Test extends RadioTimeline {
    RadioTimelineLevel47Test() {
        super(LEVEL_47);
    }

    /** Rook's barks only with him, his two scripted lines with every fit; the pearl's line in every run that has it. */
    @Test
    void theBarksNeedRookAndThePearlIsAlwaysTold() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        if (!rook) {
                            assertTrue(!opened(played, CueTrigger.FIRST_KIND_KILL), run + ": no Seraph bark");
                        }
                        for (double t : new double[] {12, 250}) {
                            assertTrue(
                                    played.stream()
                                            .anyMatch(p -> p.cue().speaker().equals("Rook")
                                                    && p.cue().trigger() == CueTrigger.TIME
                                                    && p.cue().t() == t
                                                    && p.opened().isPresent()),
                                    run + ": Rook's t=" + t + " line with every fit");
                        }
                        if (events.stream().anyMatch(e -> e.trigger() == CueTrigger.SECRET)) {
                            assertTrue(opened(played, CueTrigger.SECRET), run + ": the pearl's line");
                        }
                    }
                }
            }
        }
    }

    /** The Choir's lines are clean on the enemy channel; Vorne is silent; the friendly lines are at noise step 5. */
    @Test
    void theChoirIsCleanAndTheNoiseIsStepFive() {
        LevelData data = CONTENT.level(LEVEL_47);
        assertEquals(5, data.radioNoiseStep(), "the act's fifth noise step");
        int choir = 0;
        for (VoiceLines.VoiceLine line : VoiceLines.radio(CONTENT.voices(), data, level)) {
            if (line.speaker().equals("The Choir")) {
                choir++;
                assertEquals(VoiceLines.Filter.CLEAN, line.filter(), "the Choir's channel is clean");
            } else if (line.filter() == VoiceLines.Filter.RADIO) {
                assertEquals(5, line.noise(), "noise step 5: " + line.text());
            }
        }
        assertEquals(2, choir, "the Choir twice");
        for (var cue : data.radio()) {
            assertTrue(!cue.speaker().equals(Expression.VORNE), "Vorne is silent");
        }
    }

    private static boolean opened(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .anyMatch(p -> p.cue().trigger() == trigger && p.opened().isPresent());
    }
}
