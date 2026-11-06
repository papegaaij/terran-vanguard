package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part B, step B3b (design/campaign Level 43, the Warden lines of three "≥ 80 px between hull edges"): a hovering
 * line abreast with a {@code lineSpacing} stands that far apart, centred on the field's middle; without one the even
 * spread as built.
 */
class LineSpacingTest {
    @Test
    void aSpacedLineOfThreeStandsAt80_240_400() {
        assertArrayEquals(new double[] {80, 240, 400}, telegraphXs(Optional.of(160.0)), 1e-6);
    }

    @Test
    void anUnspacedLineKeepsTheEvenSpread() {
        assertArrayEquals(new double[] {120, 240, 360}, telegraphXs(Optional.empty()), 1e-6);
    }

    @Test
    void onlyAHoveringLineAbreastIsSpaced() {
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        0,
                        WaveSpec.Formation.V_WING,
                        Act7ASpecs.warden(14, true),
                        3,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .lineSpacing(Optional.of(160.0))
                .build());
    }

    private static double[] telegraphXs(Optional<Double> spacing) {
        WaveSpec wave = WaveSpec.builder(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act7ASpecs.warden(14, true),
                        3,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .lineStagger(Optional.of(new WaveSpec.LineStagger(1.3, false)))
                .lineSpacing(spacing)
                .build();
        Sortie sortie = Act7ASpecs.sortie(Act7ASpecs.level(30, List.of(wave), Optional.empty()));
        List<Double> xs = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(12) && xs.size() < 3; i++) {
            sortie.step(Command.NONE);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.LINE_TELEGRAPH) {
                    xs.add(sortie.events().x(e));
                }
            }
        }
        return xs.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
