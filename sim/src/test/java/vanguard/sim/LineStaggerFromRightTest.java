package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part B, step B1 (design/campaign Level 43, the t=188 Warden line: "straight down, opening right to left"): a line
 * stagger {@code fromRight} opens the windows in turn from the right.
 */
class LineStaggerFromRightTest {
    @Test
    void aLineAbreastOpensItsWindowsInTurnRightToLeft() {
        WaveSpec wave = WaveSpec.builder(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act7ASpecs.warden(14, true),
                        3,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .lineStagger(Optional.of(new WaveSpec.LineStagger(1.3, false, true)))
                .build();
        Sortie sortie = Act7ASpecs.sortie(Act7ASpecs.level(30, List.of(wave), Optional.empty()));
        List<double[]> lines = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(12) && lines.size() < 3; i++) {
            sortie.step(Command.NONE);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.LINE_TELEGRAPH) {
                    lines.add(new double[] {i, sortie.events().x(e)});
                }
            }
        }
        assertEquals(3, lines.size());
        for (int k = 1; k < 3; k++) {
            assertEquals(SimStep.ticks(1.3), lines.get(k)[0] - lines.get(k - 1)[0], 1e-9, "1.3 s apart");
            assertTrue(lines.get(k)[1] < lines.get(k - 1)[1], "each one left of the one before");
        }
    }

    @Test
    void theTwoArgumentStaggerIsLeftToRightAsBuilt() {
        assertEquals(new WaveSpec.LineStagger(1.3, true, false), new WaveSpec.LineStagger(1.3, true));
    }
}
