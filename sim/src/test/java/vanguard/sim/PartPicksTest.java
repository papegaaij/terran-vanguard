package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1 ({@link PartPicks}, {@link BossSpec.RookOrder}; design/enemies/bosses/vornes-chimera, The escort
 * (Rook)): on the test Chimera ({@link Act7FSpecs}) phase 1's order goes first: the Dart bay, then the rack, then a
 * battery from its open side (a panel facing him never his target); a phase without an order keeps the built rules (the
 * sac with the least HP).
 */
class PartPicksTest {
    private static final double ROOK_X = 240;
    private static final double ROOK_Y = 100;

    @Test
    void phaseOnesOrderTheBayThenTheRackThenABatteryFromItsOpenSide() {
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(Act7FSpecs.chimera(java.util.List.of())).settled();
        SetPiece boss = rig.piece;
        assertTrue(PartPicks.applies(boss));
        assertEquals(PartPicks.FIRST, PartPicks.rank(boss, Act7FSpecs.BAY, ROOK_X, ROOK_Y));
        assertEquals(PartPicks.OPEN, PartPicks.rank(boss, Act7FSpecs.RACK, ROOK_X, ROOK_Y));
        assertEquals(PartPicks.REST, PartPicks.rank(boss, Act7FSpecs.CAPSULE, ROOK_X, ROOK_Y), "not in the order");
        rig.kill(Act7FSpecs.BAY);
        rig.step();
        assertEquals(PartPicks.FIRST, PartPicks.rank(boss, Act7FSpecs.RACK, ROOK_X, ROOK_Y), "the rack next");
        rig.kill(Act7FSpecs.RACK);
        rig.step();
        for (double rx : new double[] {20, 460}) {
            int first = -1;
            for (int p : new int[] {Act7FSpecs.BAT_L, Act7FSpecs.BAT_R}) {
                boolean meets = boss.mirrors.meets(p, boss.partX(p) - rx, boss.partY(p) - ROOK_Y);
                int rank = PartPicks.rank(boss, p, rx, ROOK_Y);
                if (meets) {
                    assertEquals(PartPicks.SKIP, rank, "a panel facing him is never his target");
                } else if (first < 0) {
                    first = p;
                    assertEquals(PartPicks.FIRST, rank, "the first open battery from " + rx);
                } else {
                    assertEquals(PartPicks.OPEN, rank);
                }
            }
            assertTrue(first >= 0, "a battery is open from " + rx);
        }
    }

    @Test
    void aPhaseWithoutAnOrderKeepsTheWeakestSacFirst() {
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(Act7FSpecs.chimera(java.util.List.of()))
                .settled()
                .toPhase2();
        SetPiece boss = rig.piece;
        boss.damagePartFlat(Act7FSpecs.SAC_R, 200);
        assertEquals(PartPicks.FIRST, PartPicks.rank(boss, Act7FSpecs.SAC_R, ROOK_X, ROOK_Y));
        assertEquals(PartPicks.REST, PartPicks.rank(boss, Act7FSpecs.SAC_L, ROOK_X, ROOK_Y));
    }
}
