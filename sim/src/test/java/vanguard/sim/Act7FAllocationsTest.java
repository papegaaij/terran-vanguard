package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1: Level 48's new mechanics together step without allocating and replay: the test Chimera
 * ({@link Act7FSpecs}) rising out of its well, its three phases with the rear sweep (every second draw in phase 2), the
 * capsule's own shield, its death on its last words (the bullets' fade, the burst, the hold from K), and a rear wave in
 * the lane away from the ship, under the Pulse Cannon with the ship weaving.
 */
class Act7FAllocationsTest {
    private static final int[] PHASE_1 = {Act7FSpecs.BAT_L, Act7FSpecs.BAT_R, Act7FSpecs.RACK, Act7FSpecs.BAY};
    private static final int[] SACS = {Act7FSpecs.SAC_L, Act7FSpecs.SAC_R, Act7FSpecs.SAC_S};
    private static final int[] CAPSULE = {Act7FSpecs.CAPSULE};
    private static final double SECONDS = 42;

    static LevelScript level() {
        WaveSpec rear = WaveSpec.builder(
                        2,
                        WaveSpec.Formation.LINE_ABREAST,
                        TestSpecs.SKITTER,
                        1,
                        WaveSpec.Entry.REAR,
                        WaveSpec.Edge.NONE)
                .warningSeconds(Optional.of(3.0))
                .lane(WaveSpec.Lane.AWAY)
                .build();
        return Act7FSpecs.level(Act7FSpecs.SWEEPING).toBuilder()
                .waves(List.of(rear))
                .build();
    }

    static Sortie sortie() {
        return new Sortie(3, TestSpecs.LOADOUT, level(), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** 42 s: phase 1 killed at 9 s, phase 2 at 27 s, the capsule at 32 s; firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(SECONDS); i++) {
            if (i == SimStep.ticks(9)) {
                kill(sortie, PHASE_1);
            } else if (i == SimStep.ticks(27)) {
                kill(sortie, SACS);
            } else if (i == SimStep.ticks(32)) {
                kill(sortie, CAPSULE);
            }
            sortie.step(command(i));
        }
    }

    private static void kill(Sortie sortie, int[] parts) {
        for (int p : parts) {
            Act7FSpecs.kill(sortie, p);
        }
    }

    private static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
        return fire | (i % 240 < 120 ? 0 : weave);
    }

    @Test
    void levelFortyEightsMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7FAllocationsTest::sortie, Act7FAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(SECONDS); i++) {
            if (i == SimStep.ticks(9)) {
                kill(one, PHASE_1);
                kill(two, PHASE_1);
            } else if (i == SimStep.ticks(27)) {
                kill(one, SACS);
                kill(two, SACS);
            } else if (i == SimStep.ticks(32)) {
                kill(one, CAPSULE);
                kill(two, CAPSULE);
                for (int e = 0; e < one.events().size(); e++) {
                    seen[one.events().type(e).ordinal()]++;
                }
            }
            one.step(command(i));
            two.step(command(i));
            assertEquals(one.stateHash(), two.stateHash(), "step " + i);
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        for (SimEvents.Type type : List.of(
                SimEvents.Type.BOSS_ARRIVED,
                SimEvents.Type.SWEEP_TELL,
                SimEvents.Type.SWEEP_OUT,
                SimEvents.Type.SWEEP_MARK,
                SimEvents.Type.SWEEP_CLIMB,
                SimEvents.Type.SWEEP_DONE,
                SimEvents.Type.LAST_WORDS,
                SimEvents.Type.BULLETS_FADE,
                SimEvents.Type.LAST_BURST,
                SimEvents.Type.BOSS_DESTROYED)) {
            assertTrue(seen[type.ordinal()] > 0, type.name());
        }
    }
}
