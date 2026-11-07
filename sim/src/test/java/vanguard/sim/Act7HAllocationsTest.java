package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * M11 part H (design/enemies/bosses/choir-heart): Level 50's new mechanics step without allocating. Step H1a: the arena
 * walls (the closing's grow, the breath, the ship's clearance, a cut node's retreat, the walls as rock: contact, shots,
 * bullets), the drift inside the phase with its reversal and its ease out, and a Retry from the phase checkpoint. Step
 * H1b: the three playbooks (the membranes' songs from their part windows, two at once from the half, the spores, the
 * seeds and motes, the wall spit and pits, the storm's pairs), the EMP's hold, the heartbeat, K on the last words at 0 s
 * with its fade and flash, and the closing gate's scene timed from K.
 */
class Act7HAllocationsTest {
    private static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        return fire | (i % 240 < 120 ? Command.LEFT.bit() : Command.RIGHT.bit());
    }

    /** The nodes as an array (a list's iterator would allocate in the measured work). */
    private static final int[] NODES = {Act7HSpecs.N_LU, Act7HSpecs.N_LL, Act7HSpecs.N_RU, Act7HSpecs.N_RL};

    static Sortie heart() {
        Sortie sortie = Act7HSpecs.invulnerable(Act7HSpecs.HEART);
        Act7HSpecs.intoArena(sortie);
        return sortie;
    }

    static void flyArena(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(40); i++) {
            if (i == SimStep.ticks(8)) {
                Act7HSpecs.kill(sortie, Act7HSpecs.N_LU);
                Act7HSpecs.kill(sortie, Act7HSpecs.N_RL);
            } else if (i == SimStep.ticks(16)) {
                sortie.retryFromBoss();
            } else if (i == SimStep.ticks(30)) {
                for (int p : NODES) {
                    Act7HSpecs.kill(sortie, p);
                }
            }
            sortie.step(command(i));
        }
    }

    private static final int[] FIRST_HALF = {Act7HPlaySpecs.M1, Act7HPlaySpecs.M2, Act7HPlaySpecs.M3};
    private static final int[] SECOND_HALF = {Act7HPlaySpecs.M4, Act7HPlaySpecs.M5, Act7HPlaySpecs.M6};
    private static final int[] CORE = {Act7HPlaySpecs.CORE};

    static Sortie playHeart() {
        Sortie sortie = Act7HPlaySpecs.invulnerable(1, Act7HPlaySpecs.HEART);
        Act7HSpecs.untilEngaged(sortie);
        return sortie;
    }

    static void flyPlaybooks(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(80); i++) {
            if (i == SimStep.ticks(8)) {
                Act7HPlaySpecs.kill(sortie, FIRST_HALF);
                sortie.setPiece(0).stripShields(SimStep.ticks(1));
            } else if (i == SimStep.ticks(18)) {
                Act7HPlaySpecs.kill(sortie, SECOND_HALF);
            } else if (i == SimStep.ticks(36)) {
                Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.NODES);
            } else if (i == SimStep.ticks(50)) {
                Act7HPlaySpecs.kill(sortie, CORE);
            }
            sortie.step(command(i));
        }
    }

    @Test
    void thePlaybooksTheWindowsAndTheClosingGateStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7HAllocationsTest::playHeart, Act7HAllocationsTest::flyPlaybooks));
    }

    @Test
    void theWallsTheDriftAndThePhaseCheckpointStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7HAllocationsTest::heart, Act7HAllocationsTest::flyArena));
    }

    @Test
    void theArenaReplays() {
        Sortie one = heart();
        Sortie two = heart();
        flyArena(one);
        flyArena(two);
        assertEquals(one.stateHash(), two.stateHash());
    }
}
