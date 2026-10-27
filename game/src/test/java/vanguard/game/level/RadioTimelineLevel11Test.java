package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;

/** {@link RadioTimeline}'s checks for Level 11. */
class RadioTimelineLevel11Test extends RadioTimeline {
    RadioTimelineLevel11Test() {
        super(LEVEL_11);
    }

    /**
     * Step FXL11 (round 44's capture): the first telegraph falls on a cargo ship's lane, so the first-telegraph and
     * the ship-lane cues come in the same step, the ship hit just after; Okafor's teaching line ({@code
     * first-ship-telegraph}) waits out his voiced first-telegraph line and still plays, with and without Rook.
     */
    @Test
    void theShipLaneTeachingLinePlaysAfterTheFirstTelegraphsLineInTheSameStep() {
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_11, Difficulty.MEDIUM);
        List<Event> events = List.of(
                new Event(162, CueTrigger.FIRST_TELEGRAPH, ""),
                new Event(162, CueTrigger.FIRST_SHIP_TELEGRAPH, ""),
                Event.named(164, CueTrigger.ALLY_HIT, "Saint-Laurent"));
        for (int fitted : FITS) {
            List<Played> played = play(script, events, fitted, true);
            Played first = byTrigger(played, CueTrigger.FIRST_TELEGRAPH);
            Played teaching = byTrigger(played, CueTrigger.FIRST_SHIP_TELEGRAPH);
            assertTrue(first.opened().isPresent(), "fit " + fitted + ": the first-telegraph line plays");
            assertTrue(teaching.opened().isPresent(), "fit " + fitted + ": the teaching line plays");
            assertTrue(
                    teaching.opened().orElseThrow() > first.opened().orElseThrow() + SimStep.SECONDS,
                    "fit " + fitted + ": the teaching line follows the first-telegraph line");
        }
    }

    private static Played byTrigger(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .filter(line -> line.cue().trigger() == trigger)
                .findFirst()
                .orElseThrow();
    }
}
