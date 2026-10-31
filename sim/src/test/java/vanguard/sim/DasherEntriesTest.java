package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.PartGSpecs.runTo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M10 part D, step D1 (design/campaign Level 38, all-direction waves; gap check m10d §1 #2): dashers stream in from the
 * rear (the bottom edge, warned) as chasers do, and fly a {@code cross} from all four edges (every edge warned), each
 * starting its chase as it comes onto the play field; any other unit still streams in from the rear only as before.
 */
class DasherEntriesTest {
    /** Steps until {@code count} units chased (phase DASH) or 10 s; returns the serials that did. */
    private static Set<Integer> chasing(Sortie sortie, int count) {
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < SimStep.ticks(10) && seen.size() < count; i++) {
            sortie.step(Command.NONE);
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy u = sortie.enemy(j);
                if (u.phase == Enemy.Phase.DASH) {
                    assertTrue(PlayField.overlaps(u.x(), u.y(), u.hitbox()), "chasing on the play field");
                    seen.add(u.serial());
                }
            }
        }
        return seen;
    }

    @Test
    void dashersStreamInFromTheBottomEdgeWarned() {
        Sortie sortie = Act6DSpecs.untouchable(
                Act6DSpecs.level(30, List.of(Act6DSpecs.stream(5, 3, WaveSpec.Entry.REAR, WaveSpec.Edge.ALTERNATING))));
        runTo(sortie, 3);
        assertTrue(WarningEdge.BOTTOM.in(sortie.edgeWarnings()), "the bottom edge warned");
        runTo(sortie, 5);
        assertEquals(1, sortie.enemyCount());
        assertTrue(sortie.enemy(0).y() < 0, "from below");
        assertEquals(WaveSpec.Entry.REAR, sortie.enemy(0).entry());
        assertEquals(3, chasing(sortie, 3).size(), "each chases once on the play field");
    }

    @Test
    void onlyChasersAndDashersStreamInFromTheRear() {
        WaveSpec drones = WaveSpec.builder(
                        1, WaveSpec.Formation.STREAM, Act6DSpecs.DRONE, 3, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE)
                .intervalSeconds(java.util.Optional.of(0.6))
                .build();
        assertThrows(
                IllegalArgumentException.class, () -> Act6DSpecs.untouchable(Act6DSpecs.level(10, List.of(drones))));
    }

    @Test
    void aCrossOfDashersComesFromAllFourEdgesWarnedAndEachChases() {
        Sortie sortie = Act6DSpecs.untouchable(Act6DSpecs.level(30, List.of(Act6DSpecs.cross(5, 4))));
        runTo(sortie, 4);
        assertEquals(
                WarningEdge.LEFT.bit() | WarningEdge.RIGHT.bit() | WarningEdge.BOTTOM.bit(),
                sortie.edgeWarnings(),
                "every edge with a unit warned (the top as the front)");
        runTo(sortie, 5);
        assertEquals(4, sortie.enemyCount(), "together");
        int above = 0;
        int below = 0;
        int left = 0;
        int right = 0;
        for (int i = 0; i < 4; i++) {
            Enemy unit = sortie.enemy(i);
            above += unit.y() > PlayField.HEIGHT ? 1 : 0;
            below += unit.y() < 0 ? 1 : 0;
            left += unit.x() < 0 ? 1 : 0;
            right += unit.x() > PlayField.WIDTH ? 1 : 0;
        }
        assertEquals(List.of(1, 1, 1, 1), List.of(above, right, below, left), "one per edge");
        assertEquals(4, chasing(sortie, 4).size(), "each starts its chase inside the play field");
    }
}
