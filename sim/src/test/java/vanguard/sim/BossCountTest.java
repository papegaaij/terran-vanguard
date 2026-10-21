package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Step SIM-K (design/campaign Level 21, round 44 K.h: "count as the data says"): a kill ratio whose data leaves the
 * bosses out ({@link Secondary#countsBosses()}) counts neither the boss in the level's enemy total nor its kill; by
 * default the bosses count, as before. On {@link BreachArenaTest}'s colossus and level (no other enemies).
 */
class BossCountTest {
    private static Sortie sortie(Secondary secondary) {
        LevelScript level = LevelScript.builder(21, 3)
                .sections(BreachArenaTest.level(false, true).sections())
                .setPieces(List.of(BreachArenaTest.colossus(false)))
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary))
                .build();
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Kills the boss once it has arrived and returns the kills counted. */
    private static int killBoss(Sortie sortie) {
        BreachArenaTest.until(sortie, SimEvents.Type.BREACH_RISE, 12, 0);
        BreachArenaTest.steps(sortie, 1, 0);
        SetPiece boss = BreachArenaTest.boss(sortie);
        boss.damagePart(BreachArenaTest.MOUTH, 10 * BreachArenaTest.MOUTH_HP);
        sortie.objectivePasses.wreck(0, BreachArenaTest.MOUTH);
        assertTrue(boss.destroyed());
        sortie.step(0);
        return sortie.kills();
    }

    @Test
    void aBossTheSecondaryDoesNotCountIsNeitherInTheTotalNorAKill() {
        Secondary secondary = new Secondary(0.65, 100).withBossesCounted(false);
        assertFalse(secondary.countsBosses());
        Sortie sortie = sortie(secondary);
        assertEquals(0, sortie.enemyTotal(), "the boss left out");
        assertEquals(0, killBoss(sortie), "its kill not counted");
        assertEquals(0, sortie.enemyTotal());
    }

    @Test
    void byDefaultTheBossCounts() {
        Secondary secondary = new Secondary(0.65, 100);
        assertTrue(secondary.countsBosses());
        Sortie sortie = sortie(secondary);
        assertEquals(1, sortie.enemyTotal(), "the set piece counts, as before");
        assertEquals(1, killBoss(sortie));
    }

    @Test
    void onlyAKillRatioLeavesTheBossesOut() {
        assertThrows(IllegalArgumentException.class, () -> Secondary.sondes(75, "SONDES")
                .withBossesCounted(false));
        assertTrue(Secondary.sondes(75, "SONDES").withBossesCounted(true).countsBosses());
    }
}
