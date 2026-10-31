package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B1 (design/campaign Level 36, the fleet's drop craft; gap check m10b §1 #5, §4): a pair of neutral
 * CDF Stormhawks rises from the bottom edge to the top at 400 px/s; the friendly banner opens 2 s ahead and is their
 * only warning (no edge warning); they are never counted as enemies and leave the screen as neutral craft.
 */
class DropCraftTest {
    @Test
    void aPairRisesFromTheBottomEdgeAt400AndLeavesAtTheTop() {
        Sortie sortie = CloudDiversSpecs.sortie(CloudDiversSpecs.level(20, List.of(CloudDiversSpecs.dropCraft(3))));
        int left = 0;
        for (int i = 0; i < SimStep.ticks(3.5); i++) {
            sortie.step(Command.NONE);
            left += sortie.events().count(SimEvents.Type.NEUTRAL_LEFT);
        }
        List<Enemy> pair = CloudDiversSpecs.all(sortie, "fleet-stormhawk");
        assertEquals(2, pair.size());
        double[] ys = {pair.get(0).y(), pair.get(1).y()};
        assertEquals(
                PlayField.WIDTH / 4.0, Math.min(pair.get(0).x(), pair.get(1).x()), 1e-6);
        assertEquals(
                PlayField.WIDTH * 3 / 4.0, Math.max(pair.get(0).x(), pair.get(1).x()), 1e-6);
        for (int i = 0; i < SimStep.ticks(0.25); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(100, pair.get(0).y() - ys[0], 1, "up the screen at 400 px/s");
        assertEquals(100, pair.get(1).y() - ys[1], 1);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            left += sortie.events().count(SimEvents.Type.NEUTRAL_LEFT);
        }
        assertEquals(2, left, "both out at the top, as neutral craft");
        assertEquals(0, sortie.kills());
    }

    @Test
    void theFriendlyBannerOpensTwoSecondsAheadAndNoEdgeWarns() {
        Sortie sortie = CloudDiversSpecs.sortie(CloudDiversSpecs.level(20, List.of(CloudDiversSpecs.dropCraft(4))));
        assertEquals(0, sortie.enemyTotal(), "never counted");
        int firstBanner = -1;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(Command.NONE);
            if (firstBanner < 0 && sortie.friendliesInbound()) {
                firstBanner = sortie.levelTick;
            }
            assertEquals(0, sortie.edgeWarnings(), "the banner replaces the edge warning");
        }
        assertEquals(SimStep.ticks(2), firstBanner, 1);
        Sortie hostile = CloudDiversSpecs.sortie(CloudDiversSpecs.level(
                20,
                List.of(TestSpecs.wave(
                        4,
                        WaveSpec.Formation.LINE_ABREAST,
                        TestSpecs.SKITTER,
                        2,
                        WaveSpec.Entry.REAR,
                        WaveSpec.Edge.NONE))));
        boolean warned = false;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            hostile.step(Command.NONE);
            warned |= hostile.edgeWarnings() != 0;
            assertFalse(hostile.friendliesInbound());
        }
        assertTrue(warned, "a hostile rear wave keeps its edge warning");
    }
}
