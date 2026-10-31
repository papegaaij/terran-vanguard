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
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 36 (M10 part B, step B4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot, {@code --escort none} without him; RadioRuns#level36Runs): the timed
 * lines (Rook's t=80 bark only with him; step B4 retimed them to the typed pages), the harvesters' secondary met or failed (Okafor's or Varga's closing line,
 * {@code secondary-failed} queued as a closing line, as {@code secondary-objective} is), the first strike kill's bark
 * (Rook's) and the pathfinder pod's line (Varga's). Its lines are voiced (step VOX-B); Vorne is silent here.
 */
class RadioTimelineLevel36Test extends RadioTimeline {
    RadioTimelineLevel36Test() {
        super(LEVEL_36);
    }

    /**
     * The secondary's outcome is always told: met, Okafor's line; failed, Varga's; never both, never left out, voiced or
     * not, with Rook or without him; the failed line queues as a closing line.
     */
    @Test
    void theHarvestersOutcomeIsAlwaysTold() {
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

    /**
     * The first strike kill's bark and the t=80 homing bark are Rook's: they play only while he flies; the bark plays
     * after the first two strikes' kills (one at t=170 waits for Okafor's t=172 line and goes stale, as an event line does).
     */
    @Test
    void rooksBarksNeedRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                boolean strike =
                        events.stream().anyMatch(e -> e.trigger() == CueTrigger.FIRST_STRIKE_KILL && e.t() < 165);
                boolean late =
                        events.stream().anyMatch(e -> e.trigger() == CueTrigger.FIRST_STRIKE_KILL && e.t() >= 165);
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        boolean bark = opened(played, CueTrigger.FIRST_STRIKE_KILL);
                        if (!late) {
                            assertEquals(strike && rook, bark, run + ": the strike bark");
                        } else if (!rook) {
                            assertTrue(!bark, run + ": the strike bark waits for Rook");
                        }
                        boolean homing = played.stream()
                                .anyMatch(p -> p.cue().trigger() == CueTrigger.TIME
                                        && p.cue().t() == 80
                                        && p.opened().isPresent());
                        assertEquals(rook, homing, run + ": Rook's t=80 bark");
                    }
                }
            }
        }
    }

    /** The pathfinder pod's line (Varga's) plays whenever the pod is broken, with Rook or without him. */
    @Test
    void thePodsLineIsVargas() {
        for (List<Event> events : RUNS.get(level)) {
            if (events.stream().noneMatch(e -> e.trigger() == CueTrigger.SECRET)) {
                continue;
            }
            LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
            for (int fitted : new int[] {0, ESCORT}) {
                List<Played> played =
                        play(script, events, fitted, true, Double.NaN, new double[1], SIDES.getFirst(), 6);
                assertTrue(
                        played.stream()
                                .anyMatch(p -> p.cue().trigger() == CueTrigger.SECRET
                                        && p.cue().speaker().equals("Varga")
                                        && p.opened().isPresent()),
                        events + " fit " + fitted + ": Varga's pod line");
            }
        }
    }

    /** Vorne is silent in Level 36 (the act intro's page 3 carries him); every line is voiced. */
    @Test
    void vorneIsSilentAndEveryLineIsVoiced() {
        LevelData data = CONTENT.level(LEVEL_36);
        assertTrue(data.radio().stream().noneMatch(cue -> cue.speaker().equals(Expression.VORNE)), "Vorne is silent");
        for (var cue : data.radio()) {
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
    }

    private static boolean opened(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .anyMatch(p -> p.cue().trigger() == trigger && p.opened().isPresent());
    }
}
