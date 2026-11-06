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
 * {@link RadioTimeline}'s checks for Level 43 (M11 part B, step B4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot, {@code --escort none} without him; RadioRuns#level43Runs): the timed
 * lines, Rook's first Warden kill's bark and his t=130 bark only with him (his t=10 and t=257 lines are scripted), the
 * Wardens' secondary met or failed (Okafor's or Varga's closing line), the Helix pod's line (Varga's). Its lines are
 * voiced at the radio's noise step 1 (step VOX-B); the Choir's line comes over the clean channel; Vorne is silent.
 */
class RadioTimelineLevel43Test extends RadioTimeline {
    RadioTimelineLevel43Test() {
        super(LEVEL_43);
    }

    /** The secondary's outcome is always told: met, Okafor's line; failed, Varga's; never both; both closing lines. */
    @Test
    void theWardensOutcomeIsAlwaysTold() {
        LevelScript medium = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        RadioSchedule schedule = new RadioSchedule(medium);
        for (int c = 0; c < medium.radio().size(); c++) {
            CueTrigger trigger = medium.radio().get(c).trigger();
            if (trigger == CueTrigger.SECONDARY_FAILED || trigger == CueTrigger.SECONDARY_OBJECTIVE) {
                assertEquals(RadioQueue.Priority.CLOSING, schedule.priority(c), trigger.name());
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                boolean met = events.stream().anyMatch(e -> e.trigger() == CueTrigger.SECONDARY_OBJECTIVE);
                boolean failed = events.stream().anyMatch(e -> e.trigger() == CueTrigger.SECONDARY_FAILED);
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        assertEquals(met, opened(played, CueTrigger.SECONDARY_OBJECTIVE), run + ": the met line");
                        assertEquals(failed, opened(played, CueTrigger.SECONDARY_FAILED), run + ": the failed line");
                    }
                }
            }
        }
    }

    /** Rook's first Warden kill and his t=130 bark play only while he flies; his scripted lines play without him. */
    @Test
    void rooksBarksNeedRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                boolean kill = events.stream().anyMatch(e -> e.trigger() == CueTrigger.ESCORT_FIRST_KILL);
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        boolean bark = opened(played, CueTrigger.ESCORT_FIRST_KILL);
                        if (!rook || !kill) {
                            assertTrue(!bark, run + ": no first-Warden bark");
                        }
                        boolean scripted = played.stream()
                                .anyMatch(p -> p.cue().speaker().equals("Rook")
                                        && p.cue().trigger() == CueTrigger.TIME
                                        && p.cue().line().startsWith("Here we go")
                                        && p.opened().isPresent());
                        assertTrue(scripted, run + ": Rook's scripted line at the transit");
                    }
                }
            }
        }
    }

    /**
     * The Helix pod's line is Varga's (the secret's {@code radio}); the Choir speaks once, over the clean channel (no
     * noise); Vorne is silent; every line is voiced, the friendly ones at the level's noise step 1.
     */
    @Test
    void thePodIsVargasTheChoirCleanAndEveryLineVoiced() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        assertTrue(
                script.radio().stream()
                        .anyMatch(cue -> cue.trigger() == CueTrigger.SECRET
                                && cue.speaker().equals("Varga")),
                "Varga's pod line");
        LevelData data = CONTENT.level(LEVEL_43);
        assertEquals(1, data.radioNoiseStep(), "the act's first noise step");
        assertEquals(
                1,
                data.radio().stream()
                        .filter(cue -> cue.speaker().equals("The Choir"))
                        .count(),
                "one Choir line");
        List<VoiceLines.VoiceLine> lines = VoiceLines.radio(CONTENT.voices(), data, level);
        for (VoiceLines.VoiceLine line : lines) {
            if (line.speaker().equals("The Choir")) {
                assertEquals(VoiceLines.Filter.CLEAN, line.filter(), "the Choir's channel is clean");
                assertEquals(0, line.noise(), "no noise on the Choir's line");
            } else if (line.filter() == VoiceLines.Filter.RADIO) {
                assertEquals(1, line.noise(), "noise step 1: " + line.text());
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
