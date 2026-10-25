package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * M8 part G, step G1 (design/campaign Level 27, the Sirens on a chimney stack; m8g-gaps.md §1.4, §5 G8): a turret
 * nest's units standing on the ground at fixed xs in a level without walls: they enter together at the top edge at the
 * wave's time, scroll with the ground, face down the screen and sing a stagger apart in turn.
 */
class GroundNestTest {
    private static WaveSpec stack(double t, int count, List<Double> xs) {
        return TestSpecs.wave(
                        t, WaveSpec.Formation.SINGLE, SirenTest.SIREN, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .withGroundNests(xs);
    }

    @Test
    void groundNestsStandAtTheirXsScrollWithTheGroundAndSingAStaggerApart() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT,
                TestSpecs.level(30, List.of(stack(2, 2, List.of(180.0, 300.0)))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        Map<Integer, double[]> seen = new HashMap<>();
        List<double[]> rings = new ArrayList<>();
        for (int step = 0; step < SimStep.ticks(6); step++) {
            sortie.step(Command.NONE);
            for (Enemy unit : AbyssRayTest.all(sortie, "siren")) {
                assertTrue(unit.wallMounted(), "a fixed nest");
                assertEquals(0, unit.facing(), 1e-12, "facing down the screen");
                double[] first = seen.get(unit.serial());
                if (first == null) {
                    first = new double[] {unit.x(), unit.y() + sortie.groundScroll(), step};
                    seen.put(unit.serial(), first);
                }
                assertEquals(first[0], unit.x(), 1e-12, "it stands at its x");
                assertEquals(first[1], unit.y() + sortie.groundScroll(), 1e-9, "it scrolls with the ground");
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.RING) {
                    rings.add(new double[] {step, events.x(i)});
                }
            }
        }
        assertEquals(2, seen.size());
        List<Double> xs = seen.values().stream().map(v -> v[0]).sorted().toList();
        assertEquals(List.of(180.0, 300.0), xs);
        List<Double> entered = seen.values().stream().map(v -> v[2]).distinct().toList();
        assertEquals(1, entered.size(), "together, at one row");
        assertTrue(rings.size() >= 2, "both sang: " + rings.size());
        double firstAt180 = rings.stream()
                .filter(r -> r[1] == 180)
                .mapToDouble(r -> r[0])
                .min()
                .orElseThrow();
        double firstAt300 = rings.stream()
                .filter(r -> r[1] == 300)
                .mapToDouble(r -> r[0])
                .min()
                .orElseThrow();
        assertEquals(
                SimStep.ticks(WallMounts.STAGGER_SECONDS),
                firstAt300 - firstAt180,
                1,
                "the second's song a stagger behind the first's");
    }

    @Test
    void aMountIsACaveTheWallsOrTheGround() {
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.Mount.ground(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new WaveSpec.Mount(2, false, List.of(100.0)));
        assertTrue(WaveSpec.Mount.ground(List.of(100.0)).ground());
        assertTrue(WaveSpec.Mount.wall(false).wall());
    }
}
