package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.LevelClockSpecs.cue;
import static vanguard.sim.LevelClockSpecs.radioed;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1 (design/player/wingmen, Rook's rescue and return; design/campaign Level 29; m9b-gaps.md §1.7,
 * §5): the mid-level escort swap. The rescued Rook waits in the tender's hold (out of the fight, off the radio) while
 * the Bulwark flies the slot; at the last clamp's fall he launches and flies into his slot over 1 s ({@code
 * escort-joined}), the Bulwark peels away to its side's edge and is gone. With Rook flying from the start the hold opens
 * empty; with no Rook the Ember leaves (the Bulwark stays). A retry replays it; a level without one hashes as before.
 */
class EscortSwapTest {
    /** Cue 0: a bark that needs the escort flying at 4 s (Rook waits then); cue 1: his first line as he joins. */
    private static final List<RadioCue> RADIO =
            List.of(cue(CueTrigger.TIME, 4, RadioCue.FITTED_ESCORT), cue(CueTrigger.ESCORT_JOINED, 0, 0));

    private static final LevelScript LEVEL =
            Act5BSpecs.heldLevel(Optional.empty(), Optional.of(Act5BSpecs.SWAP), RADIO);

    private static final WingmanSpec ROOK = WingmanTest.rook(WingmanSpec.Side.LEFT);

    private static Sortie sortie(Loadout loadout) {
        return new Sortie(1, loadout, LEVEL, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    private static Loadout rescue() {
        return TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK).withRescue(ROOK);
    }

    private static void untilHalt(Sortie sortie, boolean[] heard) {
        while (!sortie.halting()) {
            sortie.step(0);
            for (int c = 0; c < heard.length; c++) {
                heard[c] |= radioed(sortie, c);
            }
        }
    }

    @Test
    void theRescuedRookJoinsAtTheLastClampAndTheBulwarkPeelsAway() {
        Sortie sortie = sortie(rescue());
        boolean[] heard = new boolean[RADIO.size()];
        untilHalt(sortie, heard);
        Wingman rook = sortie.wingman().orElseThrow();
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        assertTrue(rook.waiting());
        assertFalse(heard[0], "no Rook on the radio before the rescue");
        assertFalse(bulwark.departing());
        HeldArenaTest.killHolders(sortie);
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.ESCORT_SWAP));
        assertEquals(EscortSwap.JOINS, sortie.escortSwap().kind());
        int joined = -1;
        int departed = -1;
        for (int step = 1; step <= SimStep.ticks(4); step++) {
            sortie.step(0);
            for (int c = 0; c < heard.length; c++) {
                heard[c] |= radioed(sortie, c);
            }
            if (PartCSpecs.count(sortie, SimEvents.Type.ESCORT_JOINED) > 0) {
                joined = step;
            }
            if (PartCSpecs.count(sortie, SimEvents.Type.BULWARK_DEPARTED) > 0) {
                departed = step;
            }
        }
        assertEquals(SimStep.ticks(1), joined, 1, "he flies into his slot over 1 s");
        assertTrue(departed > 0, "the Bulwark is gone past its edge");
        assertTrue(heard[1], "his first line as he joins");
        assertFalse(rook.waiting() || rook.joining());
        assertTrue(bulwark.departing());
        assertTrue(sortie.escortSwap().bulwarkDeparted());
    }

    @Test
    void aRetryPlaysTheRescueAgain() {
        Sortie sortie = sortie(rescue());
        untilHalt(sortie, new boolean[RADIO.size()]);
        HeldArenaTest.killHolders(sortie);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(0);
        }
        sortie.retry(TestSpecs.FULL_ARMOUR, ROOK.armour());
        sortie.step(0);
        assertTrue(sortie.wingman().orElseThrow().waiting());
        assertFalse(sortie.bulwark().orElseThrow().departing());
        assertFalse(sortie.escortSwap().released());
    }

    @Test
    void withRookFlyingTheHoldOpensEmptyAndWithoutHimTheEmberLeaves() {
        Sortie flying = sortie(TestSpecs.LOADOUT.withWingman(ROOK));
        untilHalt(flying, new boolean[RADIO.size()]);
        HeldArenaTest.killHolders(flying);
        assertEquals(EscortSwap.EMPTY, flying.escortSwap().kind());
        Sortie none = sortie(TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK));
        untilHalt(none, new boolean[RADIO.size()]);
        HeldArenaTest.killHolders(none);
        assertEquals(EscortSwap.LEAVES, none.escortSwap().kind());
        for (int i = 0; i < SimStep.ticks(3); i++) {
            none.step(0);
        }
        assertFalse(none.bulwark().orElseThrow().departing(), "nobody replaces it: it stays");
    }

    @Test
    void theRescueReplaysAndNeedsTheLevelsSwap() {
        Sortie one = sortie(rescue());
        Sortie two = sortie(rescue());
        untilHalt(one, new boolean[RADIO.size()]);
        untilHalt(two, new boolean[RADIO.size()]);
        HeldArenaTest.killHolders(one);
        HeldArenaTest.killHolders(two);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            one.step(0);
            two.step(0);
        }
        assertEquals(one.stateHash(), two.stateHash());
        assertThrows(
                IllegalArgumentException.class,
                () -> TestSpecs.LOADOUT.withWingman(ROOK).withRescue(ROOK));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Sortie(1, rescue(), TestSpecs.level(10, List.of()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR));
    }
}
