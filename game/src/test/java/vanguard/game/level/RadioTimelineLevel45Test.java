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
 * {@link RadioTimeline}'s checks for Level 45 (M11 part C, step C2), with and without the escort (every run is played
 * with each fit; RadioRuns#level45Runs): the timed lines, Rook's two barks (the first organ down, the secret) only with
 * him, the call organ's lines (taken or tugged), the crawlers' secondary met or failed. Its lines are voiced at the
 * radio's noise step 3 (step VOX-CD); the Choir's line is clean.
 */
class RadioTimelineLevel45Test extends RadioTimeline {
    RadioTimelineLevel45Test() {
        super(LEVEL_45);
    }

    /** Rook's barks only with him; the call organ's line, taken or tugged, in every run that has it. */
    @Test
    void theBarksNeedRookAndTheHornIsAlwaysTold() {
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
                            assertTrue(!opened(played, CueTrigger.FIRST_TARGET), run + ": no organ bark");
                            assertTrue(
                                    played.stream()
                                            .noneMatch(p -> p.cue().speaker().equals("Rook")
                                                    && p.opened().isPresent()),
                                    run + ": Rook silent without him");
                        }
                        for (CueTrigger story :
                                new CueTrigger[] {CueTrigger.STORY_COLLECTED, CueTrigger.STORY_MISSED}) {
                            if (events.stream().anyMatch(e -> e.trigger() == story)) {
                                assertTrue(opened(played, story), run + ": the call organ's " + story);
                            }
                        }
                    }
                }
            }
        }
    }

    /** The Choir's line is clean on the enemy channel; Vorne is silent; the friendly lines are at noise step 3. */
    @Test
    void theChoirIsCleanAndTheNoiseIsStepThree() {
        LevelData data = CONTENT.level(LEVEL_45);
        assertEquals(3, data.radioNoiseStep(), "the act's third noise step");
        for (VoiceLines.VoiceLine line : VoiceLines.radio(CONTENT.voices(), data, level)) {
            if (line.speaker().equals("The Choir")) {
                assertEquals(VoiceLines.Filter.CLEAN, line.filter(), "the Choir's channel is clean");
            } else if (line.filter() == VoiceLines.Filter.RADIO) {
                assertEquals(3, line.noise(), "noise step 3: " + line.text());
            }
        }
        for (var cue : data.radio()) {
            assertTrue(!cue.speaker().equals(Expression.VORNE), "Vorne is silent");
        }
    }

    private static boolean opened(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .anyMatch(p -> p.cue().trigger() == trigger && p.opened().isPresent());
    }
}
