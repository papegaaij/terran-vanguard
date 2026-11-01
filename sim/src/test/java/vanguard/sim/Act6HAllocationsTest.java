package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1a: the Ascendant's first phase steps without allocating and replays: its descent, its wedge's
 * contact, its fed shield, its panels under bolts, a beam and a Tesla arc (bounces, absorbs, heat, shatters), its
 * batteries' told bursts, the cannon's playbook (lines, fans, salvos, Darts) and its phase cues.
 */
class Act6HAllocationsTest {
    static Sortie sortie() {
        return Act6HSpecs.sortie(
                Act6HSpecs.ASCENDANT,
                TestSpecs.loadout(
                        PartASpecs.beamMount(5),
                        new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA),
                        new Armament.Mount(Armament.Slot.RIGHT_WING, TestSpecs.PULSE, TestSpecs.PULSE_L2)),
                TestSpecs.RULES.withInvulnerableShip());
    }

    /** 30 s: the descent, then firing in bursts, weaving and climbing into the bow now and then. */
    static int command(int i) {
        int fire = i % 90 < 60 ? Command.FIRE.bit() : 0;
        int weave = i % 240 < 120 ? Command.LEFT.bit() : Command.RIGHT.bit();
        int climb = i % 600 < 200 ? Command.UP.bit() : i % 600 < 400 ? Command.DOWN.bit() : 0;
        return fire | weave | climb;
    }

    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(command(i));
        }
    }

    @Test
    void theAscendantsFirstPhaseStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act6HAllocationsTest::sortie, Act6HAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(30); i++) {
            one.step(command(i));
            two.step(command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        assertTrue(seen[SimEvents.Type.BOSS_PLAY_TELL.ordinal()] > 0, "the playbook drew");
        assertTrue(seen[SimEvents.Type.BATTERY_TELL.ordinal()] > 0, "the batteries burst");
        assertTrue(seen[SimEvents.Type.MIRROR_ABSORBED.ordinal()] + seen[SimEvents.Type.MIRROR_BOUNCED.ordinal()] > 0);
    }
}
