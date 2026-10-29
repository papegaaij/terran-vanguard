package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M9 part F, step F1 (design/enemies/space/void-leech, The uneasy alliance; design/campaign Level 33, QF-1 a pending
 * round 66): a prey Talon wing flies a refuelling hold (5 s at 140–200 px below the top, then straight up); a hunting
 * leech stream entering beside it drifts and chases the nearest free prey and latches on it at a spot. The prey's
 * shield is emptied for good, it no longer fires and it drifts down at 60 px/s until it leaves; the leech rides it, is
 * not latched on the ship (any shot hits it) and never moves on to the ship. Both pay their credits; a prey destroyed
 * under its leech throws it off (stunned 1 s, then it flees); one carried off counts as away. Deterministic, and it
 * allocates nothing per step.
 */
class PreyTest {
    /** A prey wing of {@code talons} at t=0 and a hunting stream of {@code leeches} from the left at 170 px, t=1.5. */
    static LevelScript level(int talons, int leeches) {
        LevelScript level = TestSpecs.level(
                40,
                List.of(
                        Act5FSpecs.preyWing(0, talons),
                        Act5FSpecs.sideStreamAt(1.5, Act5FSpecs.HUNTER, leeches, WaveSpec.Edge.LEFT, 170)));
        return TestSpecs.withObjectives(
                level, o -> o.withSecondary(new Secondary(0, 100, List.of(), "void-leech").withEscapesAllowed(15)));
    }

    static Sortie sortie(int talons, int leeches) {
        return new Sortie(3, TestSpecs.LOADOUT, level(talons, leeches), TestSpecs.RULES.withInvulnerableShip(), 60);
    }

    /** Steps until a leech latched on a prey; returns that leech. */
    private static Enemy untilPreyed(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.PREY_LATCHED) > 0) {
                for (int j = 0; j < sortie.enemyCount(); j++) {
                    if (sortie.enemy(j).preySerial() >= 0) {
                        return sortie.enemy(j);
                    }
                }
            }
        }
        throw new AssertionError("no prey latch");
    }

    private static Enemy bySerial(Sortie sortie, int serial) {
        for (int j = 0; j < sortie.enemyCount(); j++) {
            if (sortie.enemy(j).serial() == serial) {
                return sortie.enemy(j);
            }
        }
        return null;
    }

    @Test
    void aPreyWingHoldsThenLeavesUpWhenNoLeechComes() {
        Sortie sortie = sortie(3, 0);
        double lowest = PlayField.HEIGHT;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
        }
        for (int j = 0; j < sortie.enemyCount(); j++) {
            double below = PlayField.HEIGHT - sortie.enemy(j).y();
            assertTrue(below >= 140 - 1e-6 && below <= 200 + 1e-6, "its hold 140–200 px below the top: " + below);
            lowest = Math.min(lowest, sortie.enemy(j).y());
        }
        Enemy talon = sortie.enemy(0);
        double y = talon.y();
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
        }
        assertTrue(sortie.enemyCount() == 0 || talon.y() > y, "after its 5 s it leaves straight up");
        assertTrue(lowest > 300);
    }

    @Test
    void theHuntersLatchOnTheNearestFreePrey() {
        Sortie sortie = sortie(3, 3);
        Enemy leech = untilPreyed(sortie);
        Enemy talon = bySerial(sortie, leech.preySerial());
        assertNotNull(talon);
        assertTrue(talon.preyed());
        assertEquals(Enemy.LatchPhase.PREY, leech.latchPhase());
        assertFalse(leech.latched(), "not on the ship's hull: any shot hits it");
        assertFalse(leech.loose());
        assertEquals(0, sortie.latched());
        assertEquals(1, sortie.events().count(SimEvents.Type.PREY_DRIFT));
        // The other leeches go for the other, free Talons; none for the ship while one is free.
        int preyed = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.latched(), "never the ship while prey is free (invulnerable: nothing touches it)");
        }
        for (int j = 0; j < sortie.enemyCount(); j++) {
            preyed += sortie.enemy(j).preyed() ? 1 : 0;
        }
        assertEquals(3, preyed, "one leech a Talon");
    }

    @Test
    void thePreysShieldEmptiesForGoodItHoldsFireAndDriftsDownWithItsLeech() {
        Sortie sortie = sortie(1, 1);
        Enemy leech = untilPreyed(sortie);
        Enemy talon = bySerial(sortie, leech.preySerial());
        assertEquals(0, talon.shield(), "its shield emptied");
        double x = talon.x();
        double dx = leech.x() - talon.x();
        double dy = leech.y() - talon.y();
        int fired = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            double y = talon.y();
            sortie.step(Command.NONE);
            assertEquals(y - 60 * SimStep.SECONDS, talon.y(), 1e-9, "down at 60 px/s");
            assertEquals(x, talon.x(), 1e-9, "off its path, straight down");
            assertEquals(talon.x() + dx, leech.x(), 1e-9, "the leech rides its spot");
            assertEquals(talon.y() + dy, leech.y(), 1e-9);
            assertEquals(0, talon.shield(), "no refill");
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertEquals(0, fired, "a preyed Talon no longer fires");
    }

    @Test
    void bothPayAndADestroyedPreyThrowsItsLeechOff() {
        Sortie sortie = sortie(1, 1);
        Enemy leech = untilPreyed(sortie);
        int credits = sortie.credits();
        for (int j = 0; j < sortie.enemyCount(); j++) {
            if (sortie.enemy(j).serial() == leech.preySerial()) {
                sortie.destroyEnemy(j);
                break;
            }
        }
        assertTrue(sortie.credits() > credits, "the Talon pays");
        sortie.step(Command.NONE);
        assertEquals(Enemy.LatchPhase.STUNNED, leech.latchPhase(), "thrown off, stunned 1 s");
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(Enemy.LatchPhase.FLEE, leech.latchPhase(), "then it flees");
        int before = sortie.credits();
        sortie.destroyEnemy(0);
        assertTrue(sortie.credits() > before, "the leech pays");
    }

    @Test
    void aLeechCarriedOffOnItsPreyCountsAsAway() {
        Sortie sortie = sortie(1, 1);
        untilPreyed(sortie);
        int steps = 0;
        while (sortie.enemyCount() > 0 && steps++ < SimStep.ticks(20)) {
            sortie.step(Command.NONE);
        }
        assertEquals(0, sortie.enemyCount(), "both drifted off the bottom");
        assertEquals(1, sortie.escapesLost(), "the leech counts as away");
    }

    @Test
    void withNoFreePreyAHunterGoesForTheShip() {
        Sortie sortie = new Sortie(
                3,
                TestSpecs.LOADOUT,
                TestSpecs.level(
                        30,
                        List.of(
                                Act5FSpecs.preyWing(0, 1),
                                Act5FSpecs.stream(4, Act5FSpecs.HUNTER, 2, WaveSpec.Entry.REAR, WaveSpec.Edge.LEFT))),
                TestSpecs.RULES,
                60);
        for (int j = 0; j < SimStep.ticks(1); j++) {
            sortie.step(Command.NONE);
        }
        sortie.destroyEnemy(0);
        for (int i = 0; i < SimStep.ticks(10) && sortie.latched() == 0; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(1, sortie.latched() > 0 ? 1 : 0, "the ship, once no prey is left");
    }

    @Test
    void preyAndHuntersAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.SKITTER.withPreyRole(EnemySpec.PreyRole.HUNTER));
        assertThrows(IllegalArgumentException.class, () -> LampreyTest.LAMPREY.withPreyRole(EnemySpec.PreyRole.HUNTER));
        assertThrows(IllegalArgumentException.class, () -> Act5FSpecs.LEECH.withPreyRole(EnemySpec.PreyRole.PREY));
        assertEquals(Optional.of(60.0), Act5FSpecs.LEECH.latch().map(EnemySpec.Latch::preyDrift));
    }

    @Test
    void itReplaysAndAllocatesNothing() {
        Supplier<Sortie> make = () -> sortie(3, 3);
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(14); i++) {
            int command = i % 9 < 4 ? Command.FIRE.bit() : 0;
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(14); i++) {
                sortie.step(i % 9 < 4 ? Command.FIRE.bit() : 0);
            }
        }));
    }
}
