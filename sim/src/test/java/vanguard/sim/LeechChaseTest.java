package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M9 part F, step F1 (design/enemies/space/void-leech, Movement): a leech drifts in from a side or the rear edge at 50
 * px/s toward the ship, its chase starting only within 220 px of the ship: 4 s at 190 px/s turning at most 160°/s, then
 * straight on and out. The Lamprey's chase from its entry is unchanged (no drift).
 */
class LeechChaseTest {
    private static Sortie invulnerable(WaveSpec wave) {
        return Act5FSpecs.sortie(TestSpecs.LOADOUT, TestSpecs.RULES.withInvulnerableShip(), 40, wave);
    }

    private static double distance(Enemy unit, Ship ship) {
        return Math.hypot(unit.x() - ship.x(), unit.y() - ship.y());
    }

    @Test
    void fromTheSideItDriftsAtFiftyThenChasesWithinTwoHundredTwentyPixels() {
        Sortie sortie = invulnerable(Act5FSpecs.sideStreamAt(0, Act5FSpecs.LEECH, 1, WaveSpec.Edge.LEFT, 120));
        sortie.step(Command.NONE);
        Enemy leech = sortie.enemy(0);
        int drifting = 0;
        while (leech.latchPhase() != Enemy.LatchPhase.CHASE) {
            double x = leech.x();
            double y = leech.y();
            sortie.step(Command.NONE);
            if (leech.latchPhase() == Enemy.LatchPhase.DRIFT) {
                drifting++;
                assertTrue(distance(leech, sortie.ship()) > 220 - 1, "no chase farther than 220 px");
                assertEquals(50 * SimStep.SECONDS, Math.hypot(leech.x() - x, leech.y() - y), 1e-6, "50 px/s");
            }
            assertTrue(drifting < SimStep.ticks(20), "it reaches the ship's reach");
        }
        assertTrue(drifting > SimStep.ticks(3), "the side entry far from the ship drifts seconds: " + drifting);
        assertTrue(distance(leech, sortie.ship()) <= 220 + 190 * SimStep.SECONDS, "within 220 px");
        // The step that came within 220 px chased already.
        int chasing = 1;
        double last = leech.facing();
        double most = Math.toRadians(160) * SimStep.SECONDS + 1e-9;
        while (leech.latchPhase() == Enemy.LatchPhase.CHASE) {
            double x = leech.x();
            double y = leech.y();
            sortie.step(Command.NONE);
            chasing++;
            assertTrue(Math.abs(Math.IEEEremainder(leech.facing() - last, 2 * Math.PI)) <= most, "160°/s");
            last = leech.facing();
            if (leech.latchPhase() == Enemy.LatchPhase.CHASE) {
                assertEquals(190 * SimStep.SECONDS, Math.hypot(leech.x() - x, leech.y() - y), 1e-6, "190 px/s");
            }
        }
        assertEquals(SimStep.ticks(4), chasing, "it chases for its 4 s");
        assertEquals(Enemy.LatchPhase.ON, leech.latchPhase(), "then straight on");
        int steps = 0;
        while (sortie.enemyCount() > 0 && steps++ < SimStep.ticks(10)) {
            sortie.step(Command.NONE);
        }
        assertEquals(0, sortie.enemyCount(), "and out");
    }

    @Test
    void fromTheRearItIsWithinReachAtOnce() {
        Sortie sortie =
                invulnerable(Act5FSpecs.stream(0, Act5FSpecs.LEECH, 1, WaveSpec.Entry.REAR, WaveSpec.Edge.LEFT));
        sortie.step(Command.NONE);
        Enemy leech = sortie.enemy(0);
        int drifting = 0;
        for (int i = 0; i < SimStep.ticks(6) && leech.latchPhase() != Enemy.LatchPhase.CHASE; i++) {
            sortie.step(Command.NONE);
            drifting += leech.latchPhase() == Enemy.LatchPhase.DRIFT ? 1 : 0;
        }
        assertEquals(Enemy.LatchPhase.CHASE, leech.latchPhase());
        assertTrue(drifting < SimStep.ticks(0.5), "a rear entry near the ship hardly drifts: " + drifting);
    }

    @Test
    void theLampreyStillChasesFromItsEntry() {
        Sortie sortie = invulnerable(TestSpecs.wave(
                0, WaveSpec.Formation.SINGLE, LampreyTest.LAMPREY, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.LEFT));
        boolean drifted = false;
        boolean chased = false;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(Command.NONE);
            if (sortie.enemyCount() > 0) {
                drifted |= sortie.enemy(0).latchPhase() == Enemy.LatchPhase.DRIFT;
                chased |= sortie.enemy(0).latchPhase() == Enemy.LatchPhase.CHASE;
            }
        }
        assertTrue(chased);
        assertFalse(drifted, "no drift without one");
    }

    @Test
    void aDriftComesWithItsDistance() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Chase(190, 1, 4, 220, 0));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Chase(190, 1, 4, 0, 50));
        assertFalse(new EnemySpec.Chase(190, 1, 4).drifts());
    }
}
