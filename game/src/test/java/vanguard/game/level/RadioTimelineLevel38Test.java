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
 * {@link RadioTimeline}'s checks for Level 38 (M10 part D, step D4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot, {@code --escort none} without him; RadioRuns#level38Runs): the timed
 * lines, the first strike kill's bark (Rook's), the cargo pod's line (Varga's, the secret's own), the Harrows' secondary
 * met at the level's end (Varga's closing line before Okafor's); the Choir and Vorne are quiet. Its lines are voiced
 * (step VOX-D).
 */
class RadioTimelineLevel38Test extends RadioTimeline {
    RadioTimelineLevel38Test() {
        super(LEVEL_38);
    }

    /** The first strike kill's bark is Rook's: it plays only while he flies; his t=186 line needs him too. */
    @Test
    void rooksLinesNeedRook() {
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
                            assertTrue(
                                    played.stream()
                                            .noneMatch(p -> p.cue().speaker().equals("Rook")
                                                    && p.opened().isPresent()),
                                    run + ": no Rook line without him");
                        }
                    }
                }
            }
        }
    }

    /** The secondary met at the end is told (a closing line), voiced or not, with Rook or without him. */
    @Test
    void theHarrowsSecondaryIsToldAtTheEnd() {
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
                        assertEquals(
                                met,
                                played.stream()
                                        .anyMatch(p -> p.cue().trigger() == CueTrigger.SECONDARY_OBJECTIVE
                                                && p.opened().isPresent()),
                                run + ": the met line");
                    }
                }
            }
        }
    }

    /** The pod's line is Varga's (the secret's {@code radio}); the Choir and Vorne are quiet; every line is voiced. */
    @Test
    void thePodsLineIsVargasNoChoirOrVorneAndEveryLineVoiced() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        assertTrue(
                script.radio().stream()
                        .anyMatch(cue -> cue.trigger() == CueTrigger.SECRET
                                && cue.speaker().equals("Varga")),
                "Varga's pod line");
        LevelData data = CONTENT.level(LEVEL_38);
        for (var cue : data.radio()) {
            assertTrue(!cue.speaker().equals("The Choir"), "the Choir is quiet");
            assertTrue(!cue.speaker().equals(Expression.VORNE), "Vorne is quiet");
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
    }
}
