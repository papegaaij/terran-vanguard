package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/enemies/bosses/brood-carrier, Echo; m11g-gaps.md §1 #5): the Brood Carrier echo's keys on
 * a small fixture ({@link Act7GSpecs#carrierEcho}): it forms in place by its rise (untouchable until formed); a sac
 * pair opens as each playbook move ends ({@link BossSpec.Windows#onMove}), in hull order; the mandible sweep's volleys
 * step their centre away from the ship's side ({@link PhasePlaybook.Move#sweptVolley}); the brood alternates its
 * launchers and splits from the open pair ({@link PhasePlaybook.Bays#alternating}); each move ends with its cover.
 */
class CarrierEchoTest {
    private static Act7GSpecs.Rig formed() {
        Act7GSpecs.Rig rig = new Act7GSpecs.Rig(Act7GSpecs.carrierEcho(1));
        for (int i = 0; i < SimStep.ticks(4) && !rig.piece.engaged(); i++) {
            rig.step();
        }
        assertTrue(rig.piece.engaged());
        return rig;
    }

    @Test
    void itFormsInPlaceUntouchableUntilFormed() {
        Act7GSpecs.Rig rig = new Act7GSpecs.Rig(Act7GSpecs.carrierEcho(1));
        rig.step();
        assertTrue(rig.piece.present());
        int formed = -1;
        for (int i = 1; i < SimStep.ticks(4) && formed < 0; i++) {
            assertEquals(Act7GSpecs.STATION_X, rig.piece.x(), 1e-6, "in place: no movement");
            assertTrue(rig.piece.partShielded(Act7GSpecs.CORE) && rig.piece.partShielded(Act7GSpecs.MANDIBLE));
            rig.step();
            formed = rig.piece.engaged() ? i : -1;
        }
        assertEquals(SimStep.ticks(3), formed, 2, "formed after its 3 s rise");
        assertTrue(rig.piece.echo().isPresent());
    }

    @Test
    void aSacPairOpensAsEachMoveEndsInHullOrder() {
        Act7GSpecs.Rig rig = formed();
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        List<Integer> opened = new ArrayList<>();
        int moves = 0;
        int before = book.move();
        for (int i = 0; i < SimStep.ticks(30) && moves < 6; i++) {
            boolean shutBefore = !rig.piece.partOpen(1) && !rig.piece.partOpen(3);
            int open1 = rig.piece.windows.openTicks(1);
            int open3 = rig.piece.windows.openTicks(3);
            rig.step();
            boolean ended = before >= 0 && book.move() < 0;
            before = book.move();
            if (ended) {
                moves++;
                int pair = rig.piece.windows.openTicks(1) > open1 && rig.piece.partOpen(2)
                        ? 0
                        : rig.piece.windows.openTicks(3) > open3 && rig.piece.partOpen(4) ? 1 : -1;
                assertTrue(pair >= 0, "a pair opens as the move ends");
                opened.add(pair);
                assertFalse(rig.aimed.isEmpty(), "the move's cover from the mandibles");
            } else if (shutBefore) {
                assertFalse(rig.piece.partOpen(1) || rig.piece.partOpen(3), "no window on a timer");
            }
        }
        assertEquals(6, moves);
        assertEquals(List.of(0, 1, 0, 1, 0, 1), opened, "the pairs in hull order");
        assertTrue(rig.piece.partShielded(Act7GSpecs.CORE), "the core sealed in the sacs' phase");
    }

    @Test
    void theSweptVolleysStepAwayFromTheShipsSide() {
        Act7GSpecs.Rig rig = formed();
        rig.shipX = 60;
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        List<Double> turns = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(60) && turns.size() < 3; i++) {
            boolean sweeping = book.move() == Act7GSpecs.MOVE_SWEEP;
            rig.step();
            if (sweeping || book.move() == Act7GSpecs.MOVE_SWEEP) {
                for (double[] shot : rig.aimedAt) {
                    double toShip = StrictMath.atan2(rig.shipY - shot[1], rig.shipX - shot[0]);
                    double aim = StrictMath.atan2(shot[3] - shot[1], shot[2] - shot[0]);
                    turns.add(Math.IEEEremainder(aim - toShip, 2 * StrictMath.PI));
                }
            }
        }
        assertEquals(3, turns.size(), "three volleys");
        assertEquals(0, turns.get(0), 1e-9, "the first at the ship");
        assertEquals(Act7GSpecs.SWEEP / 2, turns.get(1), 1e-9);
        assertEquals(Act7GSpecs.SWEEP, turns.get(2), 1e-9, "turning toward the right: the ship is on the left");
    }

    @Test
    void theBroodAlternatesItsLaunchersFromTheOpenPair() {
        Act7GSpecs.Rig rig = formed();
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        List<EnemySpec.Launcher> broods = new ArrayList<>();
        int last = -1;
        int first = -1;
        for (int i = 0; i < SimStep.ticks(120) && broods.size() < 3; i++) {
            rig.step();
            if (!rig.launches.isEmpty() && book.move() == Act7GSpecs.MOVE_BROOD) {
                if (book.draws() != last) {
                    broods.add(rig.launchers.getFirst());
                    last = book.draws();
                }
                boolean anyOpen = false;
                for (int p : Act7GSpecs.SACS) {
                    anyOpen |= rig.piece.partOpen(p);
                }
                for (double[] at : rig.launches) {
                    boolean fromSac = false;
                    for (int p : Act7GSpecs.SACS) {
                        boolean here = Math.abs(at[0] - rig.piece.partX(p)) < 1e-6
                                && Math.abs(at[1] - rig.piece.partY(p)) < 1e-6;
                        boolean starting = book.draws() != first;
                        fromSac |= here && (!starting || (anyOpen ? rig.piece.partOpen(p) : p == 1));
                    }
                    assertTrue(fromSac, "a brood splits from the pair open as it begins (else the first sac)");
                }
                first = book.draws();
            }
        }
        assertEquals(3, broods.size());
        assertSame(Act6HSpecs.BROOD, broods.get(0));
        assertSame(Act6HSpecs.DARTS, broods.get(1));
        assertSame(Act6HSpecs.BROOD, broods.get(2));
    }

    @Test
    void theCoreJoinsOnceTwoSacsAreDead() {
        Act7GSpecs.Rig rig = formed();
        rig.piece.wreckPart(1);
        rig.piece.wreckPart(2);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            rig.step();
        }
        assertEquals(1, rig.piece.phase(), "the core's phase");
        assertFalse(rig.piece.partShielded(Act7GSpecs.CORE), "the core exposed after its 1 s");
    }
}
