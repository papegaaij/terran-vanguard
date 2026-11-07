package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, phase 2: the drift; m11h-gaps.md §1 #4): the drift inside a
 * boss phase while the scroll is halted: a level without a current gets one for it; it drifts from the closing on its
 * profile (|θ| ≤ 15°) at the nominal arena speed (QH-2 a: 60 px/s), its one reversal told 2 s ahead at the second
 * node's death or 25 s into the phase, none on easy; the bullets slide with it; it eases out after the phase; the
 * reversal breaks the calm and shows as the level's reversals do; deterministic.
 */
class PhaseDriftTest {
    private static final double EPS = 1e-9;

    private static Sortie arena(Optional<PhaseDriftSpec> drift) {
        Sortie sortie =
                Act7HSpecs.invulnerable(Act7HSpecs.heart(Act7HSpecs.boss(Optional.of(Act7HSpecs.WALLS), drift, true)));
        Act7HSpecs.intoArena(sortie);
        return sortie;
    }

    @Test
    void aBossDriftMakesTheCurrentAndNothingElseDoes() {
        Sortie plain = Act7HSpecs.sortie(Act7HSpecs.heart(Act7HSpecs.boss(Optional.empty(), Optional.empty(), false)));
        assertNull(plain.current());
        Sortie drifting = Act7HSpecs.sortie(Act7HSpecs.HEART);
        assertNotNull(drifting.current());
        assertNotNull(drifting.current().phaseDrift());
        Act7HSpecs.untilEngaged(drifting);
        Act7HSpecs.steps(drifting, 3);
        assertEquals(0, drifting.current().driftStep(), "no drift before its phase");
        assertTrue(drifting.current().calm(drifting.levelTick));
    }

    @Test
    void itDriftsOnItsProfileAtTheArenaSpeed() {
        Sortie sortie = arena(Optional.of(Act7HSpecs.DRIFT.withReversal(Optional.empty())));
        Current current = sortie.current();
        PhaseDrift drift = current.phaseDrift();
        double most = 0;
        for (int t = 0; t < SimStep.ticks(40); t++) {
            sortie.step(0);
            assertTrue(Math.abs(current.degrees()) <= 15 + EPS, "|θ| ≤ 15°");
            double expected = 60 * SimStep.SECONDS * Trig.sin(StrictMath.toRadians(drift.degreesAt(t)));
            assertEquals(expected, current.driftStep(), 1e-9, "60 px/s × sin θ on the phase's clock");
            most = Math.max(most, Math.abs(current.drift()));
        }
        assertTrue(most > 60 * Math.sin(Math.toRadians(14)), "it reaches its profile's angles: " + most);
        assertTrue(most <= 60 * Math.sin(Math.toRadians(15)) + EPS);
    }

    @Test
    void theReversalComesAtTheSecondNodesDeathToldTwoSecondsAhead() {
        Sortie sortie = arena(Optional.of(Act7HSpecs.DRIFT));
        Current current = sortie.current();
        Act7HSpecs.steps(sortie, 8);
        assertTrue(Act7HSpecs.kill(sortie, Act7HSpecs.N_LU));
        sortie.step(0);
        assertEquals(0, sortie.events().count(SimEvents.Type.REVERSAL_TOLD), "one node: not yet");
        assertTrue(Act7HSpecs.kill(sortie, Act7HSpecs.N_RU));
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.REVERSAL_TOLD), "the second node tells it");
        int told = current.phaseDrift().index();
        assertEquals(told, current.told(sortie.levelTick), "the chevron shows from its tell");
        assertFalse(current.calm(sortie.levelTick));
        int toRun = 0;
        while (sortie.events().count(SimEvents.Type.REVERSAL_RUN) == 0 && toRun < SimStep.ticks(5)) {
            sortie.step(0);
            toRun++;
        }
        assertEquals(SimStep.ticks(2), toRun, "it runs 2 s after its tell");
        assertEquals(told, current.running(sortie.levelTick));
        Act7HSpecs.steps(sortie, 1.5);
        assertTrue(current.driftReversed(), "the drift runs reversed");
        double reversed = current.driftStep();
        double forward = 60 * SimStep.SECONDS * Trig.sin(StrictMath.toRadians(current.degrees()));
        assertEquals(-forward, reversed, 1e-9, "its sign flipped");
        int toldAgain = 0;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(0);
            toldAgain += sortie.events().count(SimEvents.Type.REVERSAL_TOLD);
        }
        assertEquals(0, toldAgain, "one reversal a run");
        assertTrue(current.calm(sortie.levelTick));
        assertFalse(current.driftReversed());
    }

    @Test
    void withoutKillsTheReversalRunsAt25Seconds() {
        Sortie sortie = arena(Optional.of(Act7HSpecs.DRIFT));
        int toldAt = -1;
        int ranAt = -1;
        for (int t = 0; t < SimStep.ticks(40); t++) {
            sortie.step(0);
            if (sortie.events().count(SimEvents.Type.REVERSAL_TOLD) > 0) {
                assertEquals(-1, toldAt);
                toldAt = t;
            }
            if (sortie.events().count(SimEvents.Type.REVERSAL_RUN) > 0) {
                assertEquals(-1, ranAt);
                ranAt = t;
            }
        }
        assertEquals(SimStep.ticks(23), toldAt, "told 2 s ahead");
        assertEquals(SimStep.ticks(25), ranAt, "runs 25 s into the phase");
    }

    @Test
    void easyHasNoReversal() {
        Sortie sortie = arena(Optional.of(Act7HSpecs.DRIFT.withReversal(Optional.empty())));
        Act7HSpecs.steps(sortie, 8);
        Act7HSpecs.kill(sortie, Act7HSpecs.N_LU);
        Act7HSpecs.kill(sortie, Act7HSpecs.N_RU);
        int told = 0;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            sortie.step(0);
            told += sortie.events().count(SimEvents.Type.REVERSAL_TOLD)
                    + sortie.events().count(SimEvents.Type.REVERSAL_RUN);
            assertTrue(sortie.current().calm(sortie.levelTick));
        }
        assertEquals(0, told);
    }

    @Test
    void theBulletsSlideWithIt() {
        Sortie sortie = arena(Optional.of(Act7HSpecs.DRIFT.withReversal(Optional.empty())));
        Act7HSpecs.steps(sortie, 8);
        EnemyBullet bullet = sortie.force.bullets().obtain();
        bullet.fire(240, 300, 0, -60, 4);
        double x = bullet.x();
        sortie.step(0);
        double drift = sortie.current().driftStep();
        assertTrue(Math.abs(drift) > 0.05, "it drifts at 8 s: " + drift);
        assertEquals(x + drift, bullet.x(), 1e-9, "the bullet slid with the drift");
    }

    @Test
    void itEasesOutAfterThePhase() {
        Sortie sortie = arena(Optional.of(Act7HSpecs.DRIFT.withReversal(Optional.empty())));
        Act7HSpecs.steps(sortie, 8);
        for (int p : Act7HSpecs.NODES) {
            assertTrue(Act7HSpecs.kill(sortie, p));
        }
        double before = Math.abs(sortie.current().degrees());
        assertTrue(before > 5);
        int easing = 0;
        double last = before;
        while (sortie.current().phaseDrift().active() && easing < SimStep.ticks(3)) {
            sortie.step(0);
            assertTrue(Math.abs(sortie.current().degrees()) <= last + 0.2, "easing toward 0");
            last = Math.abs(sortie.current().degrees());
            easing++;
        }
        assertEquals(2, sortie.setPiece(0).phase());
        assertTrue(easing <= SimStep.ticks(1) + 2, "out within its 1 s (after the phase's end): " + easing);
        Act7HSpecs.steps(sortie, 2);
        assertEquals(0, sortie.current().driftStep());
        assertEquals(0, sortie.current().degrees());
    }

    @Test
    void theDriftReplays() {
        Sortie one = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Sortie two = Act7HSpecs.sortie(Act7HSpecs.HEART);
        for (Sortie sortie : new Sortie[] {one, two}) {
            Act7HSpecs.intoArena(sortie);
            for (int i = 0; i < SimStep.ticks(30); i++) {
                if (i == SimStep.ticks(9)) {
                    Act7HSpecs.kill(sortie, Act7HSpecs.N_LL);
                    Act7HSpecs.kill(sortie, Act7HSpecs.N_RL);
                }
                sortie.step(i % 100 < 50 ? Command.LEFT.bit() | Command.FIRE.bit() : Command.RIGHT.bit());
            }
        }
        assertEquals(one.stateHash(), two.stateHash());
    }
}
