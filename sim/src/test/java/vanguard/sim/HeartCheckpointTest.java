package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, the checkpoint at each phase's start; m11h-gaps.md §1 #5): with
 * phase checkpoints (easy and medium) the boss checkpoint is recorded again as each later phase starts, and a Retry
 * from boss restarts that phase from its start: the earlier phases' parts dead, the others as they were, the walls and
 * the drift as that phase found them, its opening and its armour patch again, the boss time on from its arrival; the
 * restored state is the state the run had one step into the phase; none on hard (the warning's checkpoint only);
 * deterministic.
 */
class HeartCheckpointTest {
    private static int command(int i) {
        return (i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit()) | Command.FIRE.bit();
    }

    /** The boss's state that a restore puts back, as a hash: its phase and clocks, place, parts, walls and drift. */
    private static long bossState(Sortie sortie) {
        SetPiece heart = sortie.setPiece(0);
        StateHash hash = new StateHash();
        hash.add(heart.phase())
                .add(heart.engaged() ? 1 : 0)
                .add(heart.opening() ? 1 : 0)
                .add(heart.phaseSeconds())
                .add(heart.bossTicks())
                .add(heart.renderX(1))
                .add(heart.renderY(1));
        for (int p = 0; p < heart.partCount(); p++) {
            hash.add(heart.partHp(p));
        }
        sortie.arenaWalls().addTo(hash);
        sortie.current().addTo(hash);
        hash.add(sortie.levelTick).add(sortie.groundScroll);
        return hash.value();
    }

    /** Steps (the ship weaving and firing) until the boss's phase is {@code phase}; the step that entered it. */
    private static void untilPhase(Sortie sortie, int phase) {
        for (int i = 0; i < SimStep.ticks(15) && sortie.setPiece(0).phase() != phase; i++) {
            sortie.step(command(i));
        }
        assertEquals(phase, sortie.setPiece(0).phase());
    }

    private static int patches(Sortie sortie) {
        int n = 0;
        for (int i = 0; i < sortie.pickups.size(); i++) {
            n += sortie.pickups.get(i).type() == PickupType.ARMOUR_PATCH ? 1 : 0;
        }
        return n;
    }

    @Test
    void aRetryRestartsTheArenaFromItsStart() {
        Sortie sortie = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Act7HSpecs.untilEngaged(sortie);
        assertTrue(sortie.bossCheckpoint(), "the warning's checkpoint");
        Act7HSpecs.kill(sortie, Act7HSpecs.M1);
        Act7HSpecs.kill(sortie, Act7HSpecs.M2);
        untilPhase(sortie, 1);
        assertEquals(1, sortie.events().count(SimEvents.Type.PHASE_CHECKPOINT));
        int at = -1;
        for (int e = 0; e < sortie.events().size(); e++) {
            at = sortie.events().type(e) == SimEvents.Type.PHASE_CHECKPOINT
                    ? sortie.events().value(e)
                    : at;
        }
        assertEquals(1, at, "value: the phase");
        int levelTick = sortie.levelTick;
        sortie.step(0);
        long oneStepIn = bossState(sortie);
        // Play on: the walls grow and breathe, the drift runs, a node falls, the ship takes hits.
        for (int i = 0; i < SimStep.ticks(9); i++) {
            if (i == SimStep.ticks(6)) {
                assertTrue(Act7HSpecs.kill(sortie, Act7HSpecs.N_LU));
            }
            sortie.step(command(i));
        }
        assertTrue(sortie.arenaWalls().inset(0) < 80, "the cut half is going back");
        sortie.retryFromBoss();
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_RETRY));
        SetPiece heart = sortie.setPiece(0);
        assertEquals(1, heart.phase(), "back in the arena");
        assertTrue(heart.opening(), "its opening (the closing) runs again");
        assertTrue(heart.partWrecked(Act7HSpecs.M1) && heart.partWrecked(Act7HSpecs.M2), "phase 1's parts stay dead");
        for (int p : Act7HSpecs.NODES) {
            assertEquals(1750, heart.partHp(p), "the nodes whole again");
        }
        assertEquals(6600, heart.partHp(Act7HSpecs.CORE));
        assertEquals(1, patches(sortie), "its armour patch dropped again");
        assertEquals(levelTick, sortie.levelTick, "the level clock stands where it stood");
        assertTrue(sortie.arenaWalls().standing() && sortie.arenaWalls().told(0), "the walls told afresh");
        assertEquals(0, sortie.arenaWalls().inset(0));
        assertEquals(oneStepIn, bossState(sortie), "the boss, its walls and drift as one step into the phase");
        assertTrue(sortie.bossCheckpoint());
    }

    @Test
    void aRetryRestartsTheCoreFromItsStart() {
        Sortie sortie = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Act7HSpecs.intoArena(sortie);
        Act7HSpecs.steps(sortie, 12);
        for (int p : Act7HSpecs.NODES) {
            assertTrue(Act7HSpecs.kill(sortie, p));
        }
        untilPhase(sortie, 2);
        assertEquals(1, sortie.events().count(SimEvents.Type.PHASE_CHECKPOINT));
        double bossSeconds = sortie.setPiece(0).bossSeconds();
        sortie.step(0);
        long oneStepIn = bossState(sortie);
        Act7HSpecs.steps(sortie, 6);
        assertTrue(Act7HSpecs.kill(sortie, Act7HSpecs.CORE), "the core is open after the opening");
        Act7HSpecs.steps(sortie, 1);
        sortie.retryFromBoss();
        sortie.step(0);
        SetPiece heart = sortie.setPiece(0);
        assertFalse(heart.destroyed(), "the core's fight again");
        assertEquals(2, heart.phase());
        assertTrue(heart.opening());
        for (int p : Act7HSpecs.NODES) {
            assertTrue(heart.partWrecked(p), "phase 2's nodes stay dead");
        }
        assertEquals(6600, heart.partHp(Act7HSpecs.CORE));
        assertEquals(
                bossSeconds + SimStep.SECONDS, heart.bossSeconds(), 1e-9, "the boss time runs on from its arrival");
        assertEquals(oneStepIn, bossState(sortie));
    }

    @Test
    void hardHasNoPhaseCheckpoint() {
        Sortie sortie = Act7HSpecs.sortie(
                Act7HSpecs.heart(Act7HSpecs.boss(Optional.of(Act7HSpecs.WALLS), Optional.of(Act7HSpecs.DRIFT), false)));
        Act7HSpecs.untilEngaged(sortie);
        Act7HSpecs.kill(sortie, Act7HSpecs.M1);
        Act7HSpecs.kill(sortie, Act7HSpecs.M2);
        int checkpoints = 0;
        for (int i = 0; i < SimStep.ticks(15) && sortie.setPiece(0).phase() != 1; i++) {
            sortie.step(0);
            checkpoints += sortie.events().count(SimEvents.Type.PHASE_CHECKPOINT);
        }
        Act7HSpecs.steps(sortie, 3);
        assertEquals(0, checkpoints);
        sortie.retryFromBoss();
        sortie.step(0);
        Act7HSpecs.untilEngaged(sortie);
        SetPiece heart = sortie.setPiece(0);
        assertEquals(0, heart.phase(), "the warning's checkpoint: the membranes again");
        assertFalse(heart.partWrecked(Act7HSpecs.M1));
        assertFalse(sortie.arenaWalls().standing());
        assertEquals(-1, sortie.setPiece(0).markedPhase());
    }

    @Test
    void aWarningRetryForgetsNothingItShouldNot() {
        // A retry before any phase's start restores the warning's checkpoint (the boss arrives again).
        Sortie sortie = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Act7HSpecs.untilEngaged(sortie);
        Act7HSpecs.steps(sortie, 2);
        sortie.retryFromBoss();
        sortie.step(0);
        assertFalse(sortie.setPiece(0).present() && sortie.setPiece(0).engaged(), "the boss arrives again");
        Act7HSpecs.untilEngaged(sortie);
        assertEquals(0, sortie.setPiece(0).phase());
    }

    @Test
    void aPhaseRetryReplays() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = Act7HSpecs.sortie(Act7HSpecs.HEART);
            Act7HSpecs.intoArena(sortie);
            for (int i = 0; i < SimStep.ticks(10); i++) {
                sortie.step(command(i));
            }
            sortie.retryFromBoss();
            for (int i = 0; i < SimStep.ticks(12); i++) {
                if (i == SimStep.ticks(6)) {
                    Act7HSpecs.kill(sortie, Act7HSpecs.N_LL);
                    Act7HSpecs.kill(sortie, Act7HSpecs.N_RU);
                }
                sortie.step(command(i));
            }
            hashes[run] = sortie.stateHash();
        }
        assertArrayEquals(new long[] {hashes[0], hashes[0]}, hashes);
    }
}
