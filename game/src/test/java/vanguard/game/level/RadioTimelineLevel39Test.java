package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;
import static vanguard.game.level.RadioRuns.holdRuns;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.level.RadioRuns.HoldRun;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 39 (M10 part E, step E4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot, {@code --escort none} without him; RadioRuns#level39Runs) and through
 * its three holds (the level clock slows over the works while the queue runs on real time, so a hold stretches the gaps
 * between the timed lines): the timed lines, the first tower's bark (Rook's), the topple onto a tower (Okafor's), the
 * survey hut's line (Varga's, the secret's own), the pumps' secondary met (Varga's); Vorne once on the enemy channel.
 * Its lines are voiced (step VOX-E).
 */
class RadioTimelineLevel39Test extends RadioTimeline {
    RadioTimelineLevel39Test() {
        super(LEVEL_39);
    }

    /** The three holds are flown where the works' first towers reach their depth, in order, at 40 of 130 px/s. */
    @Test
    void theHoldsAreFlownOverTheThreeWorks() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        List<HoldRun> holds = holdRuns(script, 6);
        assertEquals(3, holds.size(), "Loki, Pele, Prometheus: " + holds);
        double[] near = {44.8, 126.8, 176.8};
        for (int h = 0; h < holds.size(); h++) {
            HoldRun hold = holds.get(h);
            assertEquals(40.0 / 130, hold.ratio(), 1e-9, "hold " + h);
            assertEquals(near[h], hold.start(), 1.0, "hold " + h + " starts where its first tower is 200 px down");
        }
        // A line inside a hold is due in real time: the timed lines still play at most MAX_LATE_SECONDS late.
        for (double seconds : HOLD_SECONDS) {
            for (Played line :
                    play(script, List.of(), ESCORT, true, Double.NaN, new double[1], SIDES.getFirst(), seconds)) {
                if (line.cue().trigger() == CueTrigger.TIME) {
                    assertTrue(line.opened().isPresent() && line.late() <= MAX_LATE_SECONDS, line.toString());
                }
            }
        }
    }

    /**
     * The first tower's bark is Rook's: it plays only while he flies; Okafor's topple line plays, Rook or not, unless it
     * comes with the first tower (both at once).
     */
    @Test
    void rooksBarkNeedsRookTheToppleLineDoesNot() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                // A topple onto the first tower raises both events at once: Rook's bark may take the radio then.
                boolean topple = events.stream()
                        .anyMatch(e -> e.trigger() == CueTrigger.TOPPLE_TARGET
                                && events.stream()
                                        .noneMatch(o -> o != e && o.hold() == e.hold() && o.into() == e.into()));
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
                        if (topple) {
                            assertTrue(
                                    played.stream()
                                            .anyMatch(p -> p.cue().trigger() == CueTrigger.TOPPLE_TARGET
                                                    && p.opened().isPresent()),
                                    run + ": Okafor's topple line");
                        }
                    }
                }
            }
        }
    }

    /** The hut's line is Varga's (the secret's {@code radio}); Vorne speaks once, on the enemy channel; all voiced. */
    @Test
    void theHutsLineIsVargasVorneOnceAndEveryLineVoiced() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        assertTrue(
                script.radio().stream()
                        .anyMatch(cue -> cue.trigger() == CueTrigger.SECRET
                                && cue.speaker().equals("Varga")),
                "Varga's hut line");
        LevelData data = CONTENT.level(LEVEL_39);
        assertEquals(
                1,
                data.radio().stream()
                        .filter(cue -> cue.speaker().equals(Expression.VORNE))
                        .count(),
                "Vorne once");
        for (var cue : data.radio()) {
            assertTrue(!cue.speaker().equals("The Choir"), "the Choir is quiet");
            if (cue.line().contains("{group}")) {
                // The failed screen's line, voiced per tower (VoiceFilesTest).
                continue;
            }
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
    }
}
