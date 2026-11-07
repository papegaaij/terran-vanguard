package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part E, step E1: Level 47's enemy side together steps without allocating and replays: a lone Seraph's cycle (in
 * by its ring, folds, mirror-row arrivals, its leaving tell and last jump), a pair on drawn rows with its interleaved
 * spirals, a Herald at its station over a pair on fixed rows (its aura on the upper one), and a Rift Skater stream
 * sharing the ring cap, under a beam, a Tesla pod and a Pulse Cannon.
 */
class Act7EAllocationsTest {
    /** Tough Seraphs, so their whole cycles run under the guns. */
    private static final EnemySpec TOUGH = Act7ESpecs.seraph(100_000, 2.5, 4, 4);

    static LevelScript level() {
        return Act7ASpecs.level(
                40,
                List.of(
                        Act7ASpecs.single(0, TOUGH),
                        Act7ESpecs.pair(2, TOUGH, Optional.empty()),
                        Act7ESpecs.station(4, Act7ESpecs.herald(90), 240, 70, 10),
                        Act7ESpecs.pair(5.5, TOUGH, Optional.of(new WaveSpec.Rows(150, 245))),
                        Act7ASpecs.stream(1, Act7ASpecs.SKATER, 4, 0.5)),
                Optional.empty());
    }

    static Sortie sortie() {
        return new Sortie(
                3,
                TestSpecs.loadout(
                        PartASpecs.beamMount(5),
                        new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA),
                        new Armament.Mount(Armament.Slot.RIGHT_WING, TestSpecs.PULSE, TestSpecs.PULSE_L2)),
                level(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** 18 s, firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(18); i++) {
            sortie.step(command(i));
        }
    }

    private static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
        return fire | (i % 240 < 120 ? 0 : weave);
    }

    @Test
    void levelFortySevensEnemySideStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7EAllocationsTest::sortie, Act7EAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(18); i++) {
            one.step(command(i));
            two.step(command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        for (SimEvents.Type type : List.of(
                SimEvents.Type.JUMP_FOLD,
                SimEvents.Type.JUMP_OUT,
                SimEvents.Type.JUMP_RING,
                SimEvents.Type.ARRIVE,
                SimEvents.Type.LEAVING_TELL,
                SimEvents.Type.RING_SUNG)) {
            assertTrue(seen[type.ordinal()] > 0, type.name());
        }
    }
}
