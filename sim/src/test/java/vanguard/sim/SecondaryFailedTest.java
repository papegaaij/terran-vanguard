package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B3 (design/campaign Level 36, radio: Varga's "Too many got through" when the harvesters' secondary
 * fails): the {@code secondary-failed} cue ({@link CueTrigger#SECONDARY_FAILED}) plays once, in the step the secondary
 * objective fails (an escapes objective's unit past the allowed ones gets away), not at the first escape (the {@code
 * enemy-escaped} cue's); a level without the cue plays nothing more.
 */
class SecondaryFailedTest {
    private static final List<RadioCue> RADIO = List.of(
            CloudDiversSpecs.cue(CueTrigger.ENEMY_ESCAPED, "harvester"),
            CloudDiversSpecs.cue(CueTrigger.SECONDARY_FAILED, ""));

    /** Three harvesters 2 s apart down the screen untouched; one may get away. */
    private static Sortie sortie(List<RadioCue> radio) {
        List<WaveSpec> waves = new ArrayList<>();
        for (int k = 0; k < 3; k++) {
            waves.add(TestSpecs.wave(
                    2 * k,
                    WaveSpec.Formation.SINGLE,
                    CloudDiversSpecs.harvester(new EnemySpec.CarrierDrift(0, false)),
                    1,
                    WaveSpec.Entry.FRONT,
                    WaveSpec.Edge.NONE));
        }
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(40, 160)))
                .waves(waves)
                .radio(radio)
                .objectives(ObjectiveSpec.NONE.withSecondary(
                        new Secondary(0, 100, List.of(), "harvester").withEscapesAllowed(1)))
                .build();
        return CloudDiversSpecs.sortie(level);
    }

    @Test
    void theLineComesWhenTheSecondaryFailsOnce() {
        Sortie sortie = sortie(RADIO);
        List<Integer> escapes = new ArrayList<>();
        List<Integer> escapedLines = new ArrayList<>();
        List<Integer> failedLines = new ArrayList<>();
        int failed = -1;
        for (int i = 0; i < SimStep.ticks(36); i++) {
            sortie.step(Command.NONE);
            SimEvents seen = sortie.events();
            for (int e = 0; e < seen.size(); e++) {
                switch (seen.type(e)) {
                    case OBJECTIVE_FAILED -> failed = i;
                    case RADIO -> {
                        CueTrigger trigger =
                                sortie.script().radio().get(seen.value(e)).trigger();
                        (trigger == CueTrigger.SECONDARY_FAILED ? failedLines : escapedLines).add(i);
                    }
                    default -> {}
                }
            }
            if (sortie.escapesLost() > escapes.size()) {
                escapes.add(i);
            }
        }
        // the objective counts no more once failed: the third's escape is not counted, nor called again
        assertEquals(2, escapes.size(), "the allowed one and the one past it: " + escapes);
        assertEquals(List.of(escapes.get(0)), escapedLines, "the enemy-escaped line at the first escape");
        assertEquals(escapes.get(1), failed, "the second escape fails it (one allowed)");
        assertEquals(List.of(failed), failedLines, "the secondary-failed line once, as it fails");
    }

    @Test
    void withoutTheCueNothingMorePlays() {
        Sortie sortie = sortie(List.of(RADIO.getFirst()));
        int lines = 0;
        boolean failed = false;
        for (int i = 0; i < SimStep.ticks(36); i++) {
            sortie.step(Command.NONE);
            lines += sortie.events().count(SimEvents.Type.RADIO);
            failed |= sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED) > 0;
        }
        assertTrue(failed, "the secondary failed");
        assertEquals(1, lines, "only the enemy-escaped line");
    }
}
