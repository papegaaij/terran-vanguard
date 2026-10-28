package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1 (design/campaign Level 29, the rescue halt; m9b-gaps.md §1.1, §1.2): a held arena. The scroll
 * eases to a halt over the arena section, the level clock stands still at its end while a clamp gun (a holder) stands,
 * the halt cycle's waves come on the halt's own clock (repeating, cut at the last holder's fall), and the clock resumes
 * with the section after easing up; holders all down before the halt jump the clock to the arena's end. No boss.
 */
class HeldArenaTest {
    private static Sortie sortie(Optional<HaltCycle> cycle) {
        return TestSpecs.sortie(Act5BSpecs.heldLevel(cycle, Optional.empty(), List.of()));
    }

    private static void run(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }

    /** Destroys every holder on the field; returns how many. */
    static int killHolders(Sortie sortie) {
        int killed = 0;
        for (int i = sortie.enemyCount() - 1; i >= 0; i--) {
            if (sortie.enemy(i).holder()) {
                sortie.destroyEnemy(i);
                killed++;
            }
        }
        return killed;
    }

    private static int stepsUntilHalt(Sortie sortie) {
        for (int step = 1; step < SimStep.ticks(Act5BSpecs.HALT + 2); step++) {
            sortie.step(0);
            if (sortie.halting()) {
                return step;
            }
        }
        throw new AssertionError("no halt");
    }

    @Test
    void theScrollEasesToAHaltAndTheClockWaitsForTheClamps() {
        Sortie sortie = sortie(Optional.empty());
        double last = Double.POSITIVE_INFINITY;
        while (sortie.levelSeconds() < Act5BSpecs.ARENA_START) {
            sortie.step(0);
        }
        // Into the arena the scroll eases down (smoothstep) from 100 px/s.
        while (!sortie.halting()) {
            double speed = sortie.groundSpeed();
            assertTrue(speed <= last + 1e-9 && speed >= 0, "eases down: " + speed);
            last = speed;
            sortie.step(0);
        }
        assertEquals(Act5BSpecs.HALT, sortie.levelSeconds(), 2 * SimStep.SECONDS);
        assertEquals(4, sortie.holders());
        assertEquals(4, sortie.holdersStanding());
        double clock = sortie.levelSeconds();
        double scroll = sortie.groundScroll();
        run(sortie, 5);
        assertEquals(clock, sortie.levelSeconds(), "the clock stands still while a clamp stands");
        assertEquals(scroll, sortie.groundScroll(), 1e-9, "the ground stands still");
        assertTrue(sortie.halting());
        assertEquals(5, sortie.haltSeconds(), 2 * SimStep.SECONDS);
        assertEquals(4, killHolders(sortie));
        assertEquals(0, sortie.holdersStanding());
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.HOLDERS_DOWN));
        sortie.step(0);
        assertFalse(sortie.halting());
        run(sortie, 1);
        assertTrue(sortie.levelSeconds() > clock + 0.9, "the clock resumes");
        double easing = sortie.groundSpeed();
        assertTrue(easing > 0 && easing < 120, "the section after eases up: " + easing);
        run(sortie, 1.5);
        assertEquals(120, sortie.groundSpeed(), 1e-9);
    }

    @Test
    void clampsDownBeforeTheHaltJumpTheClockToTheArenasEnd() {
        Sortie sortie = sortie(Optional.empty());
        while (sortie.levelSeconds() < 9.6) {
            sortie.step(0);
        }
        assertEquals(4, killHolders(sortie));
        double speed = Double.NaN;
        boolean halted = false;
        while (sortie.levelSeconds() < Act5BSpecs.HALT + 1) {
            halted |= sortie.halting();
            speed = sortie.groundSpeed();
            assertTrue(speed > 50, "no halt: the scroll ramps on from its speed: " + speed);
            sortie.step(0);
        }
        assertFalse(halted);
    }

    @Test
    void theHaltCycleRepeatsOnTheHaltsClockAndIsCutAtTheLastClamp() {
        Sortie sortie = sortie(Optional.of(Act5BSpecs.CYCLE));
        int total = sortie.enemyTotal();
        stepsUntilHalt(sortie);
        // H+1: three Skitters (a snake enters one by one); H+3: three more; the cycle again from H+4.
        run(sortie, 0.9);
        assertEquals(total, sortie.enemyTotal(), "nothing before H+1");
        run(sortie, 1.5);
        assertTrue(sortie.enemyTotal() > total, "the first wave came");
        run(sortie, 3.5);
        int cycled = sortie.enemyTotal() - total;
        assertTrue(cycled > 6, "the cycle repeated: " + cycled);
        killHolders(sortie);
        sortie.step(0);
        int after = sortie.enemyTotal();
        run(sortie, 8);
        assertEquals(after, sortie.enemyTotal(), "the cycle is cut at the last clamp");
    }

    @Test
    void aRetryHaltsAgainAndARunReplays() {
        Sortie one = sortie(Optional.of(Act5BSpecs.CYCLE));
        Sortie two = sortie(Optional.of(Act5BSpecs.CYCLE));
        int first = stepsUntilHalt(one);
        assertEquals(first, stepsUntilHalt(two));
        run(one, 3);
        run(two, 3);
        assertEquals(one.stateHash(), two.stateHash());
        killHolders(one);
        run(one, 2);
        one.retry(TestSpecs.FULL_ARMOUR);
        assertEquals(first, stepsUntilHalt(one), "the retry's first step restarts the level: the same halt");
        assertEquals(4, one.holdersStanding());
    }

    @Test
    void aHeldArenaIsOneArenaOfSpeedZeroWithoutABoss() {
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(29, 5)
                .sections(List.of(new Section(10, 100), new Section(20, 100)))
                .groundUnits(Act5BSpecs.clamps())
                .build());
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(29, 5)
                .sections(List.of(new Section(10, 100)))
                .haltCycle(Optional.of(Act5BSpecs.CYCLE))
                .build());
    }
}
