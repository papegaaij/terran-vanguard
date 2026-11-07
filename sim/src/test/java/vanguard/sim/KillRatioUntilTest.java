package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/campaign Level 49, {@code KILLS n / 104} by the first warning; m11g-gaps.md §1 #8): a
 * kill ratio judged by the first boss warning ({@link Secondary#byFirstWarning()}) counts the units sent before it,
 * is met at once on reaching its target, and fails as soon as it can no longer be reached, at the latest at the
 * warning.
 */
class KillRatioUntilTest {
    /** Eight Skitters before the boss at {@code arrive} s (four at 1 s, four at 2 s), four more at 30 s. */
    static Sortie sortie(double arrive) {
        Secondary secondary = new Secondary(0.5, 50).withBossesCounted(false).byFirstWarning();
        LevelScript script = LevelScript.builder(49, 7)
                .sections(List.of(new Section(60, 100)))
                .waves(List.of(
                        Act6GSpecs.front(1, TestSpecs.SKITTER, 4),
                        Act6GSpecs.front(2, TestSpecs.SKITTER, 4),
                        Act6GSpecs.front(30, TestSpecs.SKITTER, 4)))
                .setPieces(List.of(Act6GSpecs.piece(arrive)))
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .build();
        return new Sortie(1, TestSpecs.LOADOUT, script, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    @Test
    void itCountsTheUnitsBeforeTheWarningAndIsMetAtOnce() {
        Sortie sortie = sortie(20);
        assertEquals(4, sortie.requiredKills(), "half of the eight before the warning");
        while (sortie.enemyCount() < 4) {
            sortie.step(0);
        }
        int credits = sortie.credits();
        for (int k = 0; k < 4; k++) {
            assertFalse(sortie.secondaryMet());
            sortie.objectivePasses.destroy(0);
        }
        sortie.step(0);
        assertTrue(sortie.secondaryMet(), "met at its target, long before the warning");
        assertEquals(1, sortie.events().count(SimEvents.Type.OBJECTIVE_MET));
        assertTrue(sortie.credits() >= credits + 50);
        assertFalse(sortie.secondaryFailed());
    }

    @Test
    void itFailsAsSoonAsTooManyGotAway() {
        Sortie sortie = sortie(40);
        int failedAt = -1;
        for (int i = 0; i < SimStep.ticks(39) && failedAt < 0; i++) {
            sortie.step(0);
            failedAt = sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED) > 0 ? i : -1;
        }
        assertTrue(failedAt > 0 && failedAt < SimStep.ticks(30), "failed once five got away: " + failedAt);
        assertTrue(sortie.secondaryFailed());
        assertFalse(sortie.secondaryMet());
    }

    @Test
    void itFailsAtTheWarningUnmet() {
        Sortie sortie = sortie(3);
        int failedAt = -1;
        for (int i = 0; i < SimStep.ticks(4) && failedAt < 0; i++) {
            sortie.step(0);
            failedAt = sortie.events().count(SimEvents.Type.OBJECTIVE_FAILED) > 0 ? i : -1;
        }
        assertEquals(SimStep.ticks(3), failedAt + 1, 1, "at the warning");
        assertTrue(sortie.secondaryFailed());
    }

    @Test
    void onlyAPlainKillRatioIsJudgedByTheWarning() {
        assertThrows(IllegalArgumentException.class, () -> new Secondary(0, 50).byFirstWarning());
        assertThrows(IllegalArgumentException.class, () -> Secondary.killRatioOf("skitter", 0.5, 50, "S")
                .byFirstWarning());
        assertFalse(new Secondary(0.5, 50).untilWarning());
    }
}
