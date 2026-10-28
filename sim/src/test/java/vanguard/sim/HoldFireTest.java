package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1 (design/campaign Level 29, Hold fire; m9b-gaps.md §1.6): a neutral craft is no enemy: not in the
 * level's total or the kills, no bounty, no contact, no seeker's target, the escort's shots pass through it; the
 * player's shots hit it. The {@code spared} secondary is met when the last has left, failed at the first destroyed.
 */
class HoldFireTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final String SLUG = "wavering-stormhawk";

    private static LevelScript level() {
        return LevelScript.builder(29, 5)
                .sections(List.of(new Section(20, 100)))
                .waves(List.of(TestSpecs.wave(
                        0.5,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act5BSpecs.STORMHAWK,
                        2,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)))
                .objectives(ObjectiveSpec.NONE.withSecondary(Secondary.spared(SLUG, 100, "HOLD FIRE")))
                .build();
    }

    @Test
    void sparedCraftMeetTheObjectiveWhenTheLastHasLeft() {
        Sortie sortie = new Sortie(1, TestSpecs.loadout(), level(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        assertEquals(0, sortie.enemyTotal(), "never counted as enemies");
        assertEquals(2, sortie.sparedTotal());
        int left = 0;
        boolean met = false;
        for (int i = 0; i < SimStep.ticks(15); i++) {
            sortie.step(0);
            left += PartCSpecs.count(sortie, SimEvents.Type.NEUTRAL_LEFT);
            met |= PartCSpecs.count(sortie, SimEvents.Type.OBJECTIVE_MET) > 0;
        }
        assertEquals(2, left);
        assertEquals(2, sortie.sparedLeft());
        assertTrue(met && sortie.secondaryMet());
        assertFalse(sortie.secondaryFailed());
    }

    @Test
    void theFirstDestroyedFailsItAndIsNoKill() {
        Sortie sortie = new Sortie(1, TestSpecs.loadout(), level(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        while (sortie.enemyCount() < 2) {
            sortie.step(0);
        }
        sortie.destroyEnemy(0);
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.NEUTRAL_DOWN));
        assertEquals(0, PartCSpecs.count(sortie, SimEvents.Type.ENEMY_DESTROYED));
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.OBJECTIVE_FAILED));
        assertTrue(sortie.secondaryFailed());
        assertEquals(0, sortie.kills());
        assertEquals(0, sortie.credits());
        for (int i = 0; i < SimStep.ticks(15); i++) {
            sortie.step(0);
        }
        assertFalse(sortie.secondaryMet());
    }

    @Test
    void theEscortsShotsPassThroughThePlayersHit() {
        Sortie rook = new Sortie(
                1,
                TestSpecs.loadout().withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level(),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            rook.step(FIRE);
            hits += PartCSpecs.count(rook, SimEvents.Type.ENEMY_HIT);
        }
        assertEquals(0, hits, "Rook's shots pass through, he picks no neutral target");
        Sortie player = new Sortie(1, TestSpecs.LOADOUT, level(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int playerHits = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            // Under the first craft.
            int steer = 0;
            if (player.enemyCount() > 0) {
                double dx = player.enemy(0).x() - player.ship().x();
                steer = dx > 4 ? Command.RIGHT.bit() : dx < -4 ? Command.LEFT.bit() : 0;
            }
            player.step(FIRE | steer);
            playerHits += PartCSpecs.count(player, SimEvents.Type.ENEMY_HIT);
        }
        assertTrue(playerHits > 0, "the player's shots hit them");
    }

    @Test
    void aNeutralCraftHasNoGunContactOrBountyAndTheSeekersIgnoreIt() {
        assertTrue(Act5BSpecs.STORMHAWK.seekersIgnore());
        assertFalse(Act5BSpecs.STORMHAWK.countsAsEnemy());
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.NEEDLER.withNeutral(true));
        assertThrows(IllegalArgumentException.class, () -> Secondary.spared(SLUG, 100, "HOLD FIRE")
                .withEscapesAllowed(1));
        assertTrue(Optional.of(Secondary.spared(SLUG, 1, "")).get().bySpared());
    }
}
