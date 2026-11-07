package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M11 part E, step E1 (design/enemies/air/choir-seraph, Formations: pairs; m11e §1 #3, §4): a pair's two Seraphs run
 * one cycle clock (they enter, land, fold and leave together), arrive on two rows at least 90 px apart (new rows every
 * cycle from the wave's seed, or the wave's fixed rows), mirror the same point, never overlap, and their spirals'
 * arms start 90° apart at each landing (four arms interleaved, never stacked).
 */
class SeraphPairTest {
    private static Sortie sortie(WaveSpec... waves) {
        return Act7ASpecs.sortie(Act7ASpecs.level(40, List.of(waves), Optional.empty()));
    }

    @Test
    void bothRunOneCycleClockOnRowsAtLeastNinetyApartNewEachCycleArmsInterleaved() {
        Sortie sortie = sortie(Act7ESpecs.pair(0, Act7ESpecs.SERAPH, Optional.empty()));
        sortie.step(Command.NONE);
        Enemy[] pair = Act7ESpecs.all(sortie, "choir-seraph");
        assertEquals(2, pair.length);
        assertEquals(0, pair[0].teleport().place());
        assertEquals(1, pair[1].teleport().place());
        assertEquals(2, sortie.events().count(SimEvents.Type.JUMP_RING), "both rings at once");
        long seed = PairRows.seed(pair[0].wave);
        assertEquals(pair[0].wave, pair[1].wave, "one wave, one seed");
        Set<Double> uppers = new HashSet<>();
        int landings = 0;
        int mirroring = 0;
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(i % 200 < 100 ? Command.LEFT.bit() : Command.RIGHT.bit());
            if (Act7ASpecs.find(sortie, "choir-seraph") == null) {
                break;
            }
            for (SimEvents.Type type : List.of(
                    SimEvents.Type.ARRIVE,
                    SimEvents.Type.JUMP_FOLD,
                    SimEvents.Type.JUMP_RING,
                    SimEvents.Type.LEAVING_TELL)) {
                int count = sortie.events().count(type);
                assertTrue(count == 0 || count == 2, "together: " + type + " " + count);
            }
            assertEquals(pair[0].teleport().phase(), pair[1].teleport().phase(), "one cycle clock");
            if (sortie.events().count(SimEvents.Type.ARRIVE) == 2) {
                int landing = pair[0].teleport().jumps() - 1;
                double upper = PairRows.row(seed, landing, 0, null);
                double lower = PairRows.row(seed, landing, 1, null);
                assertTrue(upper >= 110 && upper <= 230, "the upper row 110–230 px below the top: " + upper);
                assertTrue(lower - upper >= 90 && lower - upper <= 120, "the rows 90–120 px apart: " + (lower - upper));
                uppers.add(upper);
                assertEquals(pair[0].x(), pair[1].x(), 1e-9, "the same mirrored point");
                landings++;
            }
            if (pair[0].shadow().mirroring() && pair[1].shadow().mirroring()) {
                mirroring++;
                double apart = Math.IEEEremainder(
                        pair[1].shadowing.spiralAngle() - pair[0].shadowing.spiralAngle(), 2 * Math.PI);
                assertEquals(Math.PI / 2, apart, 1e-9, "the arms interleaved");
                assertTrue(Math.abs(pair[0].y() - pair[1].y()) >= 52 - 1e-9, "never overlapping");
            }
        }
        assertEquals(4, landings, "4 cycles together");
        assertTrue(uppers.size() >= 3, "new rows each cycle: " + uppers);
        assertTrue(mirroring > SimStep.ticks(8));
    }

    @Test
    void whenTheLowerRowIsLiftedAboveTheShipTheUpperOneIsLiftedAsFar() {
        // M11 part G, step G1 (E2's finding: on hard the two hulls touched for 0.3 s): the ship high on the field.
        Sortie sortie = sortie(Act7ESpecs.pair(0, Act7ESpecs.SERAPH, Optional.empty()));
        sortie.step(Command.NONE);
        Enemy[] pair = Act7ESpecs.all(sortie, "choir-seraph");
        long seed = PairRows.seed(pair[0].wave);
        int lifted = 0;
        int landings = 0;
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(Command.UP.bit() | (i % 200 < 100 ? Command.LEFT.bit() : Command.RIGHT.bit()));
            if (Act7ASpecs.find(sortie, "choir-seraph") == null) {
                break;
            }
            if (sortie.events().count(SimEvents.Type.ARRIVE) == 2) {
                landings++;
                int landing = pair[0].teleport().jumps() - 1;
                double lower = PlayField.HEIGHT - PairRows.row(seed, landing, 1, null);
                lifted += pair[1].y() > lower + 1e-6 ? 1 : 0;
                assertTrue(pair[0].y() - pair[1].y() >= PairRows.LEAST_GAP - 1e-6, "the rows keep their gap");
            }
            if (pair[0].shadow().mirroring() && pair[1].shadow().mirroring()) {
                assertTrue(Math.abs(pair[0].y() - pair[1].y()) >= 52 - 1e-9, "never overlapping");
            }
        }
        assertTrue(landings >= 3, "landings: " + landings);
        assertTrue(lifted > 0, "the lower row was lifted above the ship");
    }

    @Test
    void fixedRowsHoldEveryCycle() {
        Sortie sortie = sortie(Act7ESpecs.pair(0, Act7ESpecs.SERAPH, Optional.of(new WaveSpec.Rows(150, 245))));
        sortie.step(Command.NONE);
        Enemy[] pair = Act7ESpecs.all(sortie, "choir-seraph");
        int landings = 0;
        for (int i = 0; i < SimStep.ticks(16) && landings < 4; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ARRIVE) == 2) {
                landings++;
                assertEquals(PlayField.HEIGHT - 150, pair[0].y(), 1e-9, "the upper row");
                assertEquals(PlayField.HEIGHT - 245, pair[1].y(), 1e-9, "the lower row");
            }
        }
        assertEquals(4, landings);
    }

    @Test
    void aPairIsTwoMirroringRingEntrantsOnRowsNinetyApart() {
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        0, WaveSpec.Formation.PAIR, Act7ESpecs.SERAPH, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .build());
        assertThrows(
                IllegalArgumentException.class,
                () -> Act7ESpecs.pair(0, Act7ASpecs.SERAPH, Optional.empty()),
                "a mirroring unit without a cycle");
        assertThrows(
                IllegalArgumentException.class,
                () -> Act7ESpecs.pair(0, Act7ESpecs.SERAPH, Optional.of(new WaveSpec.Rows(150, 220))),
                "fixed rows under 90 px apart");
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveSpec.builder(
                                0,
                                WaveSpec.Formation.SINGLE,
                                Act7ASpecs.SKATER,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)
                        .rows(Optional.of(new WaveSpec.Rows(150, 245)))
                        .build(),
                "rows only for mirror-row units");
    }
}
