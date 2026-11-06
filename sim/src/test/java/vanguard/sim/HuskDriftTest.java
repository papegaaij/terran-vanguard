package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part D, step CD1 (design/enemies/space/husk, {@code drift}; m11d-gaps.md §1 #1, §4): a drifting unit flies its
 * wave's line straight, at a speed from its range, out through the far edge (it left, never a kill); an unwarned wave
 * comes in from its edge without the edge warning; its spin (presentation) comes from its serial, at a rate in its
 * range.
 */
class HuskDriftTest {
    private static Sortie sortie(WaveSpec wave) {
        return Act7CDSpecs.sortie(Act7CDSpecs.level(60, 90, List.of(wave), List.of(), VrellLevel.NONE));
    }

    /** Left to right and down: from (-40, depth 150) through (520, depth 350). */
    private static final WaveSpec.Line ACROSS = Act7CDSpecs.line(-40, 150, 520, 350);

    @Test
    void itDriftsStraightAlongItsLineAtASpeedFromItsRangeAndLeavesByTheFarEdge() {
        Sortie sortie = sortie(Act7CDSpecs.drift(1, Act7CDSpecs.husk(150), 1, true, List.of(ACROSS)));
        while (Act7CDSpecs.find(sortie, "husk") == null) {
            sortie.step(Command.NONE);
        }
        Enemy husk = Act7CDSpecs.find(sortie, "husk");
        double x0 = husk.x();
        double y0 = husk.y();
        assertEquals(-40, x0, 2);
        assertEquals(PlayField.HEIGHT - 150, y0, 2);
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
        }
        double dx = husk.x() - x0;
        double dy = husk.y() - y0;
        double lineDx = 560;
        double lineDy = -200;
        assertEquals(
                0,
                dx * lineDy - dy * lineDx,
                1e-6 * Math.hypot(lineDx, lineDy) * Math.hypot(dx, dy) + 1e-6,
                "on its line");
        double speed = Math.hypot(dx, dy) / 4;
        assertTrue(speed >= 20 - 0.5 && speed <= 40 + 0.5, "a speed from its range: " + speed);

        int kills = sortie.kills();
        int steps = 0;
        while (Act7CDSpecs.find(sortie, "husk") != null && steps++ < SimStep.ticks(40)) {
            sortie.step(Command.NONE);
        }
        assertNull(Act7CDSpecs.find(sortie, "husk"), "gone");
        assertEquals(kills, sortie.kills(), "it left: no kill");
    }

    @Test
    void anUnwarnedWaveComesInWithoutItsEdgeWarning() {
        Sortie unwarned = sortie(Act7CDSpecs.drift(4, Act7CDSpecs.husk(150), 1, true, List.of(ACROSS)));
        Sortie warned = sortie(Act7CDSpecs.drift(4, Act7CDSpecs.husk(150), 1, false, List.of(ACROSS)));
        int warnings = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            unwarned.step(Command.NONE);
            warned.step(Command.NONE);
            assertEquals(0, unwarned.edgeWarnings());
            warnings += warned.edgeWarnings() != 0 ? 1 : 0;
        }
        assertTrue(warnings > 0, "the same wave warned without the flag");
    }

    @Test
    void itsSpinComesFromItsSerialAtARateInItsRange() {
        WaveSpec wave = Act7CDSpecs.drift(1, Act7CDSpecs.husk(150), 1, true, List.of(ACROSS));
        Sortie one = sortie(wave);
        Sortie two = sortie(wave);
        for (int i = 0; i < SimStep.ticks(3); i++) {
            one.step(Command.NONE);
            two.step(Command.NONE);
        }
        Enemy husk = Act7CDSpecs.find(one, "husk");
        assertNotNull(husk);
        assertEquals(husk.driftSpin(1), Act7CDSpecs.find(two, "husk").driftSpin(1), 0, "a replay spins alike");
        double before = husk.driftSpin(1);
        for (int i = 0; i < SimStep.ticks(1); i++) {
            one.step(Command.NONE);
        }
        double rate = ((husk.driftSpin(1) - before) % 360 + 360) % 360;
        assertTrue(rate >= 10 - 0.5 && rate <= 25 + 0.5, "degrees a second: " + rate);
    }

    @Test
    void onlyADriftingUnitsWaveFliesLinesAndItAlwaysNamesThem() {
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        1, WaveSpec.Formation.SINGLE, TestSpecs.SKITTER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .lines(List.of(ACROSS))
                .build());
        assertThrows(
                IllegalArgumentException.class, () -> Act7CDSpecs.drift(1, Act7CDSpecs.husk(150), 1, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> Act7CDSpecs.husk(150).withTerrain(true));
        assertTrue(Act7CDSpecs.CASKET.lineDrift().isPresent() && Act7CDSpecs.CASKET.seekersIgnore());
        assertEquals(Optional.empty(), Act7CDSpecs.CASKET.crack());
    }
}
