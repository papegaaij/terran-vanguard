package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 round 80 fixes, R80a (design/enemies/bosses/ascendant, the phase-1 playbook; round 80's B.1, the user: "more and
 * different firing sequences, the rail gun more often"): the lance sweep, the twin lances, the battery barrage and a
 * told line at least every second draw.
 */
class AscendantLancesTest {
    private static final double EPS = 1e-6;

    private static boolean lance(int move) {
        return move == Act6HSpecs.MOVE_LANCE || move == Act6HSpecs.MOVE_SWEEP || move == Act6HSpecs.MOVE_TWIN;
    }

    private static List<Integer> plays(Act6HSpecs.Rig rig, double seconds) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            rig.step();
            moves.addAll(rig.values(SimEvents.Type.BOSS_PLAY_TELL));
        }
        return moves;
    }

    /** Steps until the cannon starts running {@code move} (its tell over); false if it never comes. */
    private static boolean untilPlayed(Act6HSpecs.Rig rig, int move, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            rig.step();
            if (rig.values(SimEvents.Type.BOSS_PLAY).contains(move)) {
                return true;
            }
        }
        return false;
    }

    /** R81a: steps until {@code move}'s tell starts. */
    private static boolean untilTold(Act6HSpecs.Rig rig, int move, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            rig.step();
            if (rig.values(SimEvents.Type.BOSS_PLAY_TELL).contains(move)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void aToldLineAtLeastEverySecondDrawNeverTheSameTwiceEveryMoveDrawn() {
        List<Integer> moves = plays(new Act6HSpecs.Rig(Act6HSpecs.round80()).settled(), 240);
        assertTrue(moves.size() >= 40, "draws: " + moves.size());
        int lances = 0;
        for (int i = 0; i < moves.size(); i++) {
            lances += lance(moves.get(i)) ? 1 : 0;
            if (i > 0) {
                assertNotEquals(moves.get(i - 1), moves.get(i), "never twice running: " + moves);
                assertTrue(lance(moves.get(i - 1)) || lance(moves.get(i)), "a lance every second draw: " + moves);
            }
        }
        for (int m = 0; m < 7; m++) {
            assertTrue(moves.contains(m), "every move drawn: " + m + " " + moves);
        }
        assertTrue(lances * 2 >= moves.size(), "at least half the draws a lance: " + lances + " of " + moves.size());
        assertEquals(moves, plays(new Act6HSpecs.Rig(Act6HSpecs.round80()).settled(), 240), "seeded: the same again");
    }

    @Test
    void theSweepFiresAtTheShipThenTurnsFiftyDegreesAwayFromTheShipsSide() {
        for (double side : new double[] {100, 380}) {
            Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.round80()).settled();
            rig.shipX = side;
            assertTrue(untilPlayed(rig, Act6HSpecs.MOVE_SWEEP, 240), "the sweep drawn");
            BossPlaybook book = rig.piece.playbook().orElseThrow();
            assertEquals(side < Act6HSpecs.X ? 1 : -1, book.side(), "away from the ship's side");
            BossLines lines = rig.piece.lines().orElseThrow();
            rig.step();
            assertTrue(lines.telegraphing());
            assertEquals(Math.toRadians(50) * book.side(), lines.sweep(), EPS);
            rig.steps(1.2);
            assertTrue(lines.live(), "live after its 1.2 s line");
            double startX = lines.dirX();
            double startY = lines.dirY();
            double fromX = lines.fromX();
            assertEquals(
                    side,
                    fromX + startX * (rig.shipY - lines.fromY()) / startY,
                    5,
                    "it fires at the ship's spot (its first live step turned)");
            rig.steps(1.3);
            assertTrue(lines.live(), "a 1.4 s beam");
            double turned = Math.atan2(
                    startX * lines.dirY() - startY * lines.dirX(), startX * lines.dirX() + startY * lines.dirY());
            assertEquals(Math.toRadians(50) * 1.3 / 1.4 * book.side(), turned, Math.toRadians(2), "turned: " + turned);
            rig.steps(0.2);
            assertFalse(lines.running(), "over after 1.4 s");
        }
    }

    @Test
    void theTwinLancesFireFromBothFlanksSixTenthsApartTheSecondReAimedAndAFanBetween() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.round80()).settled();
        rig.shipX = 300;
        assertTrue(untilPlayed(rig, Act6HSpecs.MOVE_TWIN, 240), "the twins drawn");
        BossLines lines = rig.piece.lines().orElseThrow();
        double cannonX = rig.piece.partX(Act6HSpecs.CANNON);
        rig.step();
        assertEquals(cannonX + 24, lines.fromX(), EPS, "the ship's flank first");
        assertTrue(lines.twinBusy() && !lines.twinRunning(), "the second waits");
        List<Integer> fired = new ArrayList<>();
        int fans = 0;
        for (int i = 0; i < SimStep.ticks(2.5); i++) {
            if (i == SimStep.ticks(1.0)) {
                rig.shipX = 150;
            }
            rig.step();
            if (rig.values(SimEvents.Type.BOSS_LINE_FIRED).size() > 0) {
                fired.add(i);
                if (fired.size() == 1) {
                    assertTrue(lines.twinRunning(), "the second telegraphs as the first fires");
                    assertEquals(cannonX - 24, lines.twinFromX(), EPS, "from the other flank");
                }
            }
            for (double[] shot : rig.shots) {
                fans += Math.abs(shot[0] - cannonX) < EPS ? 1 : 0;
            }
        }
        assertEquals(2, fired.size(), "two lances: " + fired);
        assertEquals(SimStep.ticks(0.6), fired.get(1) - fired.get(0), 1, "0.6 s apart");
        assertEquals(1, fans, "one gold fan between them");
        assertTrue(lines.twinDirX() < 0, "the second re-aimed at the ship's new spot (left)");
        assertFalse(lines.twinBusy(), "both over");
    }

    @Test
    void theBarrageCuesTheLivingBatteriesSideToSideEachItsOwnToldBurst() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.round80()).settled();
        rig.kill(Act6HSpecs.BAT_2);
        // R81a: from its tell on (the first battery tells under the barrage's tell, firing as it ends)
        assertTrue(untilTold(rig, Act6HSpecs.MOVE_BARRAGE, 240), "the barrage drawn");
        List<Integer> told = new ArrayList<>(rig.values(SimEvents.Type.BATTERY_TELL));
        List<Integer> at = new ArrayList<>();
        for (int k = 0; k < told.size(); k++) {
            at.add(0);
        }
        for (int i = 1; i < SimStep.ticks(3); i++) {
            rig.step();
            for (int p : rig.values(SimEvents.Type.BATTERY_TELL)) {
                told.add(p);
                at.add(i);
            }
        }
        assertEquals(
                List.of(Act6HSpecs.BAT_1, Act6HSpecs.BAT_4, Act6HSpecs.BAT_3), told, "side to side, the dead skipped");
        assertEquals(SimStep.ticks(0.4), at.get(1) - at.get(0), 1);
    }
}
