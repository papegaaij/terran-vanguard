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
 * {@link RadioTimeline}'s checks for Level 44 (M11 part B, step B4), with and without the escort (every run is played
 * with each fit; RadioRuns#level44Runs): the timed lines, Rook's reversal calls (t=98, t=168) and his t=28 line with or
 * without him (scripted), his landing-kill and chain barks only with him, the kill ratio met at the end (a closing line
 * before Okafor's level-end line). Its lines are voiced at the radio's noise step 2; the Choir's line is clean.
 */
class RadioTimelineLevel44Test extends RadioTimeline {
    RadioTimelineLevel44Test() {
        super(LEVEL_44);
    }

    /** The reversal calls play in every run, with Rook or without him; his two barks only with him. */
    @Test
    void theReversalCallsAlwaysPlayAndTheBarksNeedRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        long calls = played.stream()
                                .filter(p -> p.cue().speaker().equals("Rook")
                                        && p.cue().trigger() == CueTrigger.TIME
                                        && p.cue().line().contains("lipping")
                                        && p.opened().isPresent())
                                .count();
                        assertEquals(2, calls, run + ": both reversal calls");
                        for (CueTrigger bark :
                                new CueTrigger[] {CueTrigger.FIRST_LANDING_KILL, CueTrigger.FIRST_KIND_KILL}) {
                            if (!rook || events.stream().noneMatch(e -> e.trigger() == bark)) {
                                assertTrue(!opened(played, bark), run + ": no " + bark);
                            }
                        }
                    }
                }
            }
        }
    }

    /** The kill ratio met at the end is told (a closing line), voiced or not, with Rook or without him. */
    @Test
    void theKillRatioIsToldAtTheEnd() {
        LevelScript medium = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        RadioSchedule schedule = new RadioSchedule(medium);
        for (int c = 0; c < medium.radio().size(); c++) {
            if (medium.radio().get(c).trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
                assertEquals(RadioQueue.Priority.CLOSING, schedule.priority(c), "the met line closes");
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                boolean met = events.stream().anyMatch(e -> e.trigger() == CueTrigger.SECONDARY_OBJECTIVE);
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        assertEquals(met, opened(played, CueTrigger.SECONDARY_OBJECTIVE), run + ": the met line");
                    }
                }
            }
        }
    }

    /** The Choir's line is clean; Vorne is silent; every line is voiced, the friendly ones at noise step 2. */
    @Test
    void theChoirIsCleanAndEveryLineVoiced() {
        LevelData data = CONTENT.level(LEVEL_44);
        assertEquals(2, data.radioNoiseStep(), "the act's second noise step");
        for (VoiceLines.VoiceLine line : VoiceLines.radio(CONTENT.voices(), data, level)) {
            if (line.speaker().equals("The Choir")) {
                assertEquals(VoiceLines.Filter.CLEAN, line.filter(), "the Choir's channel is clean");
            } else if (line.filter() == VoiceLines.Filter.RADIO) {
                assertEquals(2, line.noise(), "noise step 2: " + line.text());
            }
        }
        for (var cue : data.radio()) {
            assertTrue(!cue.speaker().equals(Expression.VORNE), "Vorne is silent");
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
    }

    private static boolean opened(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .anyMatch(p -> p.cue().trigger() == trigger && p.opened().isPresent());
    }
}
