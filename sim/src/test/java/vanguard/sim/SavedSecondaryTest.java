package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part C, step C1 (design/campaign Level 37, Objective: "met when 5 of the 6 have climbed out, fails at once when a
 * second shuttle is lost"; easy 4 of 6, hard 6 of 6): the {@code saved} secondary counts the air allies home. It is met
 * at the target's home (its credits, {@link SimEvents.Type#OBJECTIVE_MET} and the {@code secondary-objective} line) and
 * fails at once by the loss after which fewer than the target can come home ({@link SimEvents.Type#OBJECTIVE_FAILED},
 * the {@code secondary-failed} line): at medium the second, at easy the third, at hard the first.
 */
class SavedSecondaryTest {
    private static Sortie sortie(Secondary secondary) {
        return Act6CSpecs.sortie(Act6CSpecs.level(secondary, List.of(), List.of()));
    }

    /** Runs to {@code seconds}; returns the steps' count of {@code type} events. */
    private static int runTo(Sortie sortie, double seconds, SimEvents.Type type) {
        int seen = 0;
        while (sortie.levelSeconds() < seconds - 1e-9) {
            sortie.step(Command.NONE);
            seen += sortie.events().count(type);
        }
        return seen;
    }

    /** Shoots down the pair-{@code pair} shuttles {@code units} (0 or 1 of the pair) once that pair holds its stations. */
    private static void lose(Sortie sortie, int pair, int... units) {
        runTo(sortie, Act6CSpecs.LIFTS[pair] + Act6CSpecs.LIFT_SECONDS + 1, SimEvents.Type.ALLY_LOST);
        for (int unit : units) {
            Act6CSpecs.fromBelow(sortie, 2 * pair + unit, 500);
        }
    }

    @Test
    void metAtTheFifthHomeWithOneLost() {
        Sortie sortie = sortie(Act6CSpecs.SAVED);
        lose(sortie, 0, 1);
        assertEquals(0, runTo(sortie, 31, SimEvents.Type.OBJECTIVE_FAILED), "one loss is the spare");
        assertFalse(sortie.secondaryMet());
        assertEquals(3, sortie.alliesHome());
        // Pair 3 climbs out at 24 + 5 + 8 = 37, home after 3 s: the fifth home meets it at once.
        int met = 0;
        boolean cued = false;
        while (sortie.levelSeconds() < 44.9 && !sortie.secondaryMet()) {
            sortie.step(Command.NONE);
            met += sortie.events().count(SimEvents.Type.OBJECTIVE_MET);
            cued |= Act6CSpecs.cued(sortie, CueTrigger.SECONDARY_OBJECTIVE);
        }
        assertTrue(sortie.secondaryMet());
        assertEquals(1, met);
        assertTrue(cued, "its line");
        assertEquals(5, sortie.alliesHome());
        assertEquals(100, sortie.credits());
        assertFalse(sortie.savedFailed());
    }

    @Test
    void failedAtOnceByTheSecondLoss() {
        Sortie sortie = sortie(Act6CSpecs.SAVED);
        lose(sortie, 0, 0);
        assertEquals(0, runTo(sortie, 12, SimEvents.Type.OBJECTIVE_FAILED));
        lose(sortie, 1, 1);
        int failed = 0;
        boolean cued = false;
        while (sortie.levelSeconds() < 21 && failed == 0) {
            sortie.step(Command.NONE);
            failed += sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED);
            cued |= Act6CSpecs.cued(sortie, CueTrigger.SECONDARY_FAILED);
        }
        assertEquals(1, failed, "at the second loss");
        assertTrue(cued, "the secondary-failed line");
        assertTrue(sortie.savedFailed());
        assertEquals(0, runTo(sortie, Act6CSpecs.END + 1, SimEvents.Type.OBJECTIVE_MET), "never met after");
        assertFalse(sortie.secondaryMet());
        assertEquals(0, sortie.credits());
        assertFalse(sortie.primaryFailed());
        assertTrue(sortie.complete());
    }

    @Test
    void easyAllowsTwoLossesAndHardNone() {
        Sortie easy = sortie(Secondary.saved(4, 100, "SHUTTLES"));
        lose(easy, 0, 0, 1);
        assertEquals(0, runTo(easy, 12, SimEvents.Type.OBJECTIVE_FAILED), "easy: two lost, 4 of 4 still to come");
        lose(easy, 1, 0);
        assertEquals(1, runTo(easy, 21, SimEvents.Type.OBJECTIVE_FAILED), "the third loss");

        Sortie hard = sortie(Secondary.saved(6, 100, "SHUTTLES"));
        lose(hard, 0, 1);
        assertEquals(1, runTo(hard, 11, SimEvents.Type.OBJECTIVE_FAILED), "hard: the first loss");

        Sortie whole = sortie(Secondary.saved(6, 100, "SHUTTLES"));
        assertEquals(1, runTo(whole, Act6CSpecs.END, SimEvents.Type.OBJECTIVE_MET), "all six home");
        assertEquals(6, whole.alliesHome());
    }

    @Test
    void aSavedObjectiveCountsTheAirAlliesAndNothingElse() {
        assertTrue(Act6CSpecs.SAVED.bySaved());
        assertFalse(Act6CSpecs.SAVED.none());
        assertFalse(Act6CSpecs.SAVED.byEscapes());
        assertThrows(IllegalArgumentException.class, () -> Secondary.saved(0, 100, "SHUTTLES"));
        assertThrows(
                IllegalArgumentException.class,
                () -> Secondary.builder().saved(5).killRatio(0.5).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> sortie(Secondary.saved(7, 100, "SHUTTLES")),
                "no more than the shuttles");
    }
}
