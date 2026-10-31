package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.SiegeSpecs.A_X;
import static vanguard.sim.SiegeSpecs.BOMB_ROW;
import static vanguard.sim.SiegeSpecs.B_X;
import static vanguard.sim.SiegeSpecs.count;
import static vanguard.sim.SiegeSpecs.runTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part D's bomb run (design/campaign Level 17, What goes for the domes; defaults pending round 40): a
 * Spore Bomber hooked {@code always} descends to the bomb row (a side entry keeps its entry row), steers at
 * its 45 px/s to the next dome's x plus its slot (wave place − middle) × 40 px, drops its load as its centre
 * comes over the dome's hit box ({@link SimEvents.Type#BOMB_DROPPED}, one hit) and flies on straight down,
 * laying its spores all along; with no dome left it flies straight down.
 */
class BombRunTest {
    private static final double ROW_Y = PlayField.HEIGHT - BOMB_ROW;

    private static List<Enemy> bombers(Sortie sortie) {
        List<Enemy> found = new ArrayList<>();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec() == SiegeSpecs.BOMBER) {
                found.add(sortie.enemy(i));
            }
        }
        return found;
    }

    @Test
    void threeBombersTakeTheirSlotsOverTheDomeAndEachDropsOneHit() {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(List.of(
                SiegeSpecs.bombers(0, WaveSpec.Formation.LINE_ABREAST, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE))));
        runTo(sortie, 7);
        List<Enemy> wave = bombers(sortie);
        assertEquals(3, wave.size());
        double[] xs = new double[3];
        for (int i = 0; i < 3; i++) {
            Enemy bomber = wave.get(i);
            assertTrue(bomber.bombing());
            assertEquals(0, bomber.bombRun(), "every one steers to the first dome coming down");
            assertEquals(ROW_Y, bomber.y(), 1e-9, "at the bomb row, 160 px below the top edge");
            xs[i] = bomber.x();
        }
        java.util.Arrays.sort(xs);
        assertEquals(A_X - 40, xs[0], 1e-9, "the slots: (place - middle) × 40 px");
        assertEquals(A_X, xs[1], 1e-9);
        assertEquals(A_X + 40, xs[2], 1e-9);
        int dropped = 0;
        boolean mines = false;
        while (sortie.levelSeconds() < 9) {
            sortie.step(Command.NONE);
            for (int i = 0; i < sortie.events().size(); i++) {
                if (sortie.events().type(i) == SimEvents.Type.BOMB_DROPPED) {
                    dropped++;
                    assertEquals(0, sortie.events().value(i), "on the first dome");
                    assertEquals(PlayField.HEIGHT - BOMB_ROW, sortie.events().y(i), 1e-9);
                }
            }
            mines |= sortie.mineCount() > 0;
        }
        assertEquals(3, dropped, "one load each");
        assertEquals(3, sortie.ally(0).hitsTaken(), 1e-9, "one hit per load");
        assertFalse(sortie.ally(0).alive(), "its third hit breaks it");
        assertTrue(mines, "the spores fall all along");
        for (Enemy bomber : bombers(sortie)) {
            assertTrue(bomber.bombSpent());
            assertEquals(-1, bomber.bombRun());
            assertTrue(bomber.y() < ROW_Y, "it flies on straight down");
        }
    }

    @Test
    void aBomberGoesForTheNextDomeOnceTheFirstHasPassedItsRow() {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(List.of(
                SiegeSpecs.bombers(8, WaveSpec.Formation.LINE_ABREAST, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE))));
        runTo(sortie, 14);
        Enemy bomber = bombers(sortie).getFirst();
        assertEquals(1, bomber.bombRun(), "the first dome is below its row: the second is next");
        assertEquals(B_X, bomber.x(), 1e-9, "a wave of one has its slot in the middle");
        runTo(sortie, 17);
        assertEquals(1, sortie.ally(1).hitsTaken(), 1e-9);
        assertEquals(0, sortie.ally(0).hitsTaken(), 1e-9);
    }

    @Test
    void aSideEntryKeepsItsEntryRow() {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(List.of(WaveSpec.of(
                1,
                WaveSpec.Formation.STREAM,
                SiegeSpecs.BOMBER,
                1,
                WaveSpec.Entry.SIDES,
                WaveSpec.Edge.RIGHT,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.of(1.0),
                List.of()))));
        runTo(sortie, 1 + SimStep.SECONDS);
        Enemy bomber = bombers(sortie).getFirst();
        double row = bomber.y();
        double x = bomber.x();
        boolean dropped = false;
        while (sortie.levelSeconds() < 25 && !dropped) {
            sortie.step(Command.NONE);
            dropped = count(sortie, SimEvents.Type.BOMB_DROPPED) > 0;
            if (!dropped) {
                assertEquals(row, bomber.y(), 1e-9, "across at its entry row");
            }
        }
        assertTrue(bomber.x() < x, "it steered in from the right edge");
        assertTrue(dropped, "and bombed the dome passing under its row");
    }

    @Test
    void withNoDomeLeftItFliesStraightDown() {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(List.of(
                SiegeSpecs.bombers(30, WaveSpec.Formation.LINE_ABREAST, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE))));
        runTo(sortie, 30 + SimStep.SECONDS);
        Enemy bomber = bombers(sortie).getFirst();
        double x = bomber.x();
        double y = bomber.y();
        runTo(sortie, 33);
        assertEquals(-1, bomber.bombRun());
        assertFalse(bomber.bombSpent());
        assertEquals(x, bomber.x(), 1e-9);
        assertEquals(y - 45 * (3 - SimStep.SECONDS), bomber.y(), 1e-6, "straight down at its 45 px/s");
    }

    @Test
    void anUnhookedBomberFliesItsPathAndHurtsNoDome() {
        LevelScript level = SiegeSpecs.level(
                List.of(SiegeSpecs.bombers(
                        0, WaveSpec.Formation.LINE_ABREAST, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)),
                List.of(),
                Secondary.NONE,
                Optional.of(SiegeSpecs.domes(Map.of())));
        Sortie sortie = SiegeSpecs.sortie(level);
        while (sortie.levelSeconds() < 15) {
            sortie.step(Command.NONE);
            assertEquals(0, count(sortie, SimEvents.Type.BOMB_DROPPED));
            for (Enemy bomber : bombers(sortie)) {
                assertFalse(bomber.bombing());
                assertEquals(-1, bomber.bombRun());
            }
        }
        assertEquals(0, sortie.ally(0).hitsTaken());
    }
}
