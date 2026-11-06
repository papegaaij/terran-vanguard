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
 * {@link RadioTimeline}'s checks for Level 46 (M11 part D, step D2), with and without the escort (every run is played
 * with each fit; RadioRuns#level46Runs): the timed lines, Rook's barks (the first Husk that cracks, Vorne's courier) and
 * his casket line only with him, the core's lines (taken or tugged), the caskets' secondary met or failed, and nothing
 * but Varga's one line in the quiet (t=120-144). Its lines are voiced at the radio's noise step 4 (step VOX-CD); the
 * Choir's line is clean.
 */
class RadioTimelineLevel46Test extends RadioTimeline {
    /** The quiet's window on the level clock, s (Level 46's README). */
    private static final double QUIET_FROM = 120;

    private static final double QUIET_TO = 144;

    RadioTimelineLevel46Test() {
        super(LEVEL_46);
    }

    /** Rook's lines only with him; the core's line, taken or tugged, in every run that has it; the quiet Varga's alone. */
    @Test
    void theBarksNeedRookTheCoreIsAlwaysToldAndTheQuietIsVargas() {
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
                            assertTrue(!opened(played, CueTrigger.FIRST_CRACK), run + ": no crack bark");
                            assertTrue(
                                    played.stream()
                                            .noneMatch(p -> p.cue().speaker().equals("Rook")
                                                    && p.opened().isPresent()),
                                    run + ": Rook silent without him");
                        }
                        for (CueTrigger story :
                                new CueTrigger[] {CueTrigger.STORY_COLLECTED, CueTrigger.STORY_MISSED}) {
                            if (events.stream().anyMatch(e -> e.trigger() == story)) {
                                assertTrue(opened(played, story), run + ": the core's " + story);
                            }
                        }
                        for (Played p : played) {
                            double at = p.opened().orElse(Double.NaN);
                            if (at >= QUIET_FROM && at < QUIET_TO) {
                                assertEquals(
                                        "Varga", p.cue().speaker(), run + ": only Varga in the quiet: " + p.text());
                            }
                        }
                    }
                }
            }
        }
    }

    /** The Choir's line is clean on the enemy channel; Vorne is silent; the friendly lines are at noise step 4. */
    @Test
    void theChoirIsCleanAndTheNoiseIsStepFour() {
        LevelData data = CONTENT.level(LEVEL_46);
        assertEquals(4, data.radioNoiseStep(), "the act's fourth noise step");
        for (VoiceLines.VoiceLine line : VoiceLines.radio(CONTENT.voices(), data, level)) {
            if (line.speaker().equals("The Choir")) {
                assertEquals(VoiceLines.Filter.CLEAN, line.filter(), "the Choir's channel is clean");
            } else if (line.filter() == VoiceLines.Filter.RADIO) {
                assertEquals(4, line.noise(), "noise step 4: " + line.text());
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
