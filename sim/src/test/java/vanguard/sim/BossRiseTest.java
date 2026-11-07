package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, Arena: the intro; {@link BossSpec.Rise}): the test Chimera
 * ({@link Act7FSpecs}) arrives at the spire well's point and rises to its station over 4 s, eased, every part
 * untouchable until it is in place; then it engages and sways.
 */
class BossRiseTest {
    private static final double EPS = 1e-9;

    @Test
    void itRisesOutOfTheWellOverFourSecondsUntouchableThenEngages() {
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(Act7FSpecs.CHIMERA);
        SetPiece boss = rig.piece;
        assertEquals(0, boss.riseShare(), EPS, "not arrived");
        rig.step();
        assertTrue(boss.present());
        assertEquals(Act7FSpecs.X, boss.x(), EPS);
        assertEquals(Act7FSpecs.WELL_Y, boss.y(), 0.1, "at the well's point");
        int arrived = rig.tick;
        double last = boss.y();
        while (!boss.engaged()) {
            assertTrue(boss.partShielded(Act7FSpecs.BAY), "untouchable while it rises");
            assertTrue(boss.riseShare() < 1);
            rig.step();
            assertTrue(boss.y() <= last + EPS, "down the screen to its station");
            last = boss.y();
            assertTrue(rig.tick - arrived <= SimStep.ticks(4) + 1, "within 4 s");
        }
        assertEquals(SimStep.ticks(4), rig.tick - arrived, 1, "4 s");
        assertEquals(Act7FSpecs.Y, boss.y(), EPS, "at its station");
        assertEquals(1, boss.riseShare(), EPS);
        assertFalse(boss.partShielded(Act7FSpecs.BAY), "touchable in place");
        rig.steps(2);
        assertTrue(Math.abs(boss.x() - Act7FSpecs.X) > 1, "it sways");
    }
}
