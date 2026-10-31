package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part D, step D1 (design/campaign Level 38, {@code HARROWS n / 23}; gap check m10d §1 #5, §4): a kill ratio scoped
 * to one enemy: its target the share of that enemy's units the level sends (rounded up), its count that enemy's kills
 * only however they die (a strike's included), judged at the end as any kill ratio.
 */
class ScopedKillRatioTest {
    private static final Secondary HARROWS = Secondary.killRatioOf("harrow", 0.65, 100, "HARROWS");

    /** 3 Harrows down from the top and 4 drones, nothing hitting the ship. */
    private static Sortie sortie() {
        WaveSpec drones = WaveSpec.builder(
                        1,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act6DSpecs.DRONE,
                        4,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .build();
        return Act6DSpecs.untouchable(Act6DSpecs.level(
                20,
                HazardSpec.NONE,
                List.of(Act6DSpecs.stream(1, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.ALTERNATING), drones),
                HARROWS));
    }

    /** Flies to the end, destroying every drone and up to {@code harrows} Harrows once they are on the play field. */
    private static int fly(Sortie sortie, int harrows) {
        int killed = 0;
        int met = 0;
        for (int i = 0; i < SimStep.ticks(40) && !sortie.complete(); i++) {
            sortie.step(Command.NONE);
            met += sortie.events().count(SimEvents.Type.OBJECTIVE_MET);
            for (int j = sortie.enemyCount() - 1; j >= 0; j--) {
                Enemy u = sortie.enemy(j);
                boolean harrow = u.spec().slug().equals("harrow");
                if (u.y() < PlayField.HEIGHT - 20 && (!harrow || killed < harrows)) {
                    killed += harrow ? 1 : 0;
                    sortie.destroyEnemy(j);
                    assertFalse(sortie.secondaryMet(), "judged at the end only");
                }
            }
        }
        assertTrue(sortie.complete());
        return met;
    }

    @Test
    void itsTargetIsTheShareOfItsEnemysUnitsRoundedUpAndItsCountThatEnemysKills() {
        Sortie sortie = sortie();
        assertEquals(2, sortie.requiredKills(), "65 % of 3, rounded up");
        assertEquals(0, sortie.secondaryKills());
        assertEquals(1, fly(sortie, 2), "met at the end");
        assertEquals(2, sortie.secondaryKills(), "the Harrows only");
        assertEquals(6, sortie.kills());
        assertTrue(sortie.secondaryMet());
    }

    @Test
    void otherKillsDoNotCount() {
        Sortie sortie = sortie();
        // 5 kills of 7 units would meet 65 % of every enemy; of the Harrows it is 1 of 2 needed.
        assertEquals(0, fly(sortie, 1));
        assertEquals(1, sortie.secondaryKills());
        assertEquals(5, sortie.kills());
        assertFalse(sortie.secondaryMet());
    }

    @Test
    void aStrikesKillCounts() {
        HazardSpec.Strikes strikes = Act6BSpecs.strikes(0, new HazardSpec.Strike(1, 120, false));
        LevelScript level = Act6DSpecs.level(
                        10, HazardSpec.NONE.withWeather(Optional.of(Act6BSpecs.storm(strikes))), List.of(), HARROWS)
                .toBuilder()
                .groundUnits(List.of(new GroundUnit(0, 120, Act6ASpecs.HARROW, -1)))
                .build();
        Sortie sortie = Act6DSpecs.untouchable(level);
        assertEquals(1, sortie.requiredKills(), "its ground-riding units count too");
        Act6BSpecs.run(sortie, SimStep.ticks(2.2), 0, SimEvents.Type.STRIKE);
        assertEquals(1, sortie.secondaryKills(), "a lightning kill counts");
        assertEquals(8, sortie.bulletCount(), "and bursts its ring");
    }

    @Test
    void onlyAKillRatioIsScopedToAnEnemy() {
        assertTrue(HARROWS.scoped());
        assertEquals("harrow", HARROWS.enemy());
        assertEquals("HARROWS", HARROWS.label());
        assertEquals(HARROWS, HARROWS.toBuilder().build());
        assertFalse(new Secondary(0.8, 50).scoped());
        assertThrows(IllegalArgumentException.class, () -> Secondary.killRatioOf("", 0.65, 100, "HARROWS"));
        assertThrows(IllegalArgumentException.class, () -> Secondary.killRatioOf("harrow", 0, 100, "HARROWS"));
        assertThrows(
                IllegalArgumentException.class,
                () -> Secondary.builder().escapes("harrow").enemy("harrow").build());
    }
}
