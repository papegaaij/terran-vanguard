package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part G, step G1a (design/campaign Level 41): the gates' own halt cycles, a gate held by a set piece (its halt's
 * cue, its fall) and the archive cores' uploads step without allocating and replay.
 */
class Act6GAllocationsTest {
    static Sortie gateSortie() {
        return TestSpecs.sortie(
                Act6GSpecs.pieceLevel(List.of(Optional.of(Act6GSpecs.SKITTERS), Optional.of(Act6GSpecs.NEEDLERS))));
    }

    /** Through both gates: the clamps destroyed 2 s into gate 0's halt, the piece 2 s into its own. */
    static void flyGates(Sortie sortie) {
        int halted = 0;
        for (int i = 0; i < SimStep.ticks(32); i++) {
            sortie.step(Act6BAllocationsTest.command(i));
            halted = sortie.halting() ? halted + 1 : 0;
            if (halted == SimStep.ticks(2)) {
                if (sortie.gate() == 0) {
                    HeldArenaTest.killHolders(sortie);
                } else {
                    sortie.objectivePasses.wreck(0, 0);
                }
            }
        }
    }

    static Sortie uplinkSortie() {
        return TestSpecs.sortie(Act6GSpecs.uplinkLevel(10, 1, Ship.START_X, 60));
    }

    /** Up to the first core, its hatch broken, uploading, out and back in; to the end (met). */
    static void flyUplinks(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(32); i++) {
            int command = i < SimStep.ticks(1.6) ? Command.UP.bit() : 0;
            if (i >= SimStep.ticks(2.5) && i < SimStep.ticks(2.8)) {
                command = Command.RIGHT.bit();
            } else if (i >= SimStep.ticks(3.2) && i < SimStep.ticks(3.5)) {
                command = Command.LEFT.bit();
            }
            sortie.step(command);
            if (i == SimStep.ticks(1.6)) {
                Act6GSpecs.breakHatch(sortie, 0);
            }
        }
    }

    @Test
    void theGatesAndTheUplinksStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act6GAllocationsTest::gateSortie, Act6GAllocationsTest::flyGates));
        assertEquals(0, Allocations.least(Act6GAllocationsTest::uplinkSortie, Act6GAllocationsTest::flyUplinks));
    }

    @Test
    void itReplays() {
        Sortie one = gateSortie();
        Sortie two = gateSortie();
        int opened = 0;
        int halted = 0;
        for (int i = 0; i < SimStep.ticks(32); i++) {
            one.step(Act6BAllocationsTest.command(i));
            two.step(Act6BAllocationsTest.command(i));
            halted = one.halting() ? halted + 1 : 0;
            if (halted == SimStep.ticks(2)) {
                if (one.gate() == 0) {
                    HeldArenaTest.killHolders(one);
                    HeldArenaTest.killHolders(two);
                } else {
                    one.objectivePasses.wreck(0, 0);
                    two.objectivePasses.wreck(0, 0);
                }
            }
            assertEquals(one.stateHash(), two.stateHash());
            opened += one.events().count(SimEvents.Type.GATE_OPENED);
        }
        assertEquals(2, opened);
        Sortie a = uplinkSortie();
        Sortie b = uplinkSortie();
        flyUplinks(a);
        flyUplinks(b);
        assertEquals(a.stateHash(), b.stateHash());
        assertEquals(1, a.uplinks().filesIn());
        assertEquals(Uplinks.SHUT, a.uplinks().state(1), "its hatch still on its way down");
    }
}
