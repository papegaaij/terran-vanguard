package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part E, the cave entries (design/campaign Level 25, The caves; m8e-gaps.md §1.1, §5 E5): a cave wave's units
 * spawn at its notch's back, each at its own time, swim out of the mouth without touching the notch's rock while the
 * scroll carries it down, and fly on to their formation's point; a hover held above the ship stops only once out of
 * the notch; the entry is warned 3 s ahead at the notch's side edge. A cave wave enters from its notch's side; a unit
 * too slow to swim out is refused at the level's start.
 */
class CaveEntryTest {
    /** Walls at 110 and 370, a cave in the left wall at row 410 (in the middle of the screen at t=5 s, 130 px/s). */
    static final Terrain TRENCH = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {110, 110})),
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {370, 370})),
            List.of(),
            List.of(new Terrain.Notch(Terrain.Side.LEFT, 410, 90, 70)));

    static LevelScript trench(Terrain terrain, List<WaveSpec> waves) {
        return TestSpecs.withGround(
                TestSpecs.level(40, waves),
                g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(terrain, 10, 28, 0.12, 0.5))));
    }

    static WaveSpec caveColumn(double t, EnemySpec enemy, int count, WaveSpec.Edge edge) {
        return TestSpecs.wave(t, WaveSpec.Formation.COLUMN, enemy, count, WaveSpec.Entry.SIDES, edge)
                .withCave(0);
    }

    private static Sortie sortie(List<WaveSpec> waves) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                trench(TRENCH, waves),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void theUnitsSwimOutOfTheNotchsBackWithoutTouchingItsRock() {
        Sortie sortie = sortie(List.of(caveColumn(5, BarbfinTest.BARBFIN, 3, WaveSpec.Edge.LEFT)));
        Map<Integer, double[]> first = new HashMap<>();
        Map<Integer, Integer> out = new HashMap<>();
        Map<Integer, Integer> stopped = new HashMap<>();
        Map<Integer, Double> lastY = new HashMap<>();
        boolean warned = false;
        for (int step = 0; step < SimStep.ticks(9); step++) {
            sortie.step(Command.NONE);
            if (step < SimStep.ticks(4.95) && step >= SimStep.ticks(2.05)) {
                assertTrue(WarningEdge.LEFT.in(sortie.edgeWarnings()), "warned at the left edge at step " + step);
                warned = true;
            }
            for (Enemy unit : AbyssRayTest.all(sortie, "barbfin")) {
                int serial = unit.serial();
                if (!first.containsKey(serial)) {
                    first.put(serial, new double[] {unit.x(), unit.y(), sortie.groundScroll()});
                }
                double half = unit.hitbox().width() / 2;
                if (!out.containsKey(serial)) {
                    assertFalse(
                            TRENCH.rock(
                                    sortie.groundScroll(),
                                    unit.x(),
                                    unit.y(),
                                    half,
                                    unit.hitbox().height() / 2),
                            "unit " + serial + " clear of rock at step " + step + ": " + unit.x() + ", " + unit.y());
                    if (unit.x() - half > 110) {
                        out.put(serial, step);
                    }
                }
                Double was = lastY.put(serial, unit.y());
                if (was != null && was == unit.y() && !stopped.containsKey(serial)) {
                    stopped.put(serial, step);
                    assertTrue(out.containsKey(serial), "unit " + serial + " stops only out of the notch");
                }
            }
        }
        assertTrue(warned);
        assertEquals(3, first.size(), "three units");
        assertEquals(3, out.size(), "all three swam out");
        for (double[] at : first.values()) {
            assertTrue(at[0] < 110 - 13, "it starts inside the notch: " + at[0]);
            double row = Terrain.row(at[2], at[1]);
            assertTrue(row > 365 && row < 455, "at the notch's rows: " + row);
        }
        assertEquals(3, stopped.size(), "each holds its hover");
    }

    @Test
    void aCaveWaveEntersFromItsNotchsSideAndSwimsFastEnough() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sortie(List.of(caveColumn(5, BarbfinTest.BARBFIN, 3, WaveSpec.Edge.RIGHT))));
        EnemySpecCopy copy = new EnemySpecCopy(BarbfinTest.BARBFIN);
        copy.speed = 30;
        EnemySpec slow = copy.build();
        assertThrows(IllegalArgumentException.class, () -> sortie(List.of(caveColumn(5, slow, 3, WaveSpec.Edge.LEFT))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Sortie(
                        1,
                        TestSpecs.LOADOUT,
                        TestSpecs.level(40, List.of(caveColumn(5, BarbfinTest.BARBFIN, 3, WaveSpec.Edge.LEFT))),
                        TestSpecs.RULES,
                        TestSpecs.FULL_ARMOUR),
                "a level without walls");
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.wave(
                        5, WaveSpec.Formation.COLUMN, BarbfinTest.BARBFIN, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .withCave(0));
    }

    @Test
    void theSameSeedSwimsTheSameWithoutAllocating() {
        Sortie a = sortie(List.of(caveColumn(5, BarbfinTest.BARBFIN, 3, WaveSpec.Edge.LEFT)));
        Sortie b = sortie(List.of(caveColumn(5, BarbfinTest.BARBFIN, 3, WaveSpec.Edge.LEFT)));
        for (int step = 0; step < SimStep.ticks(9); step++) {
            a.step(Command.NONE);
            b.step(Command.NONE);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        assertEquals(
                0,
                Allocations.least(
                        () -> sortie(List.of(caveColumn(5, BarbfinTest.BARBFIN, 3, WaveSpec.Edge.LEFT))), sortie -> {
                            for (int step = 0; step < SimStep.ticks(9); step++) {
                                sortie.step(step % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit());
                            }
                        }));
    }
}
