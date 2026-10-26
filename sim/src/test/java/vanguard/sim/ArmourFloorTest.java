package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M8 part H, step H2 (design/campaign Level 28, The run cannot be lost; the boss spec's J2): a level's armour floor
 * ({@link LevelScript#armourFloor()}) holds the ship's armour at its points from the boss's kill to the level's end,
 * on every difficulty; hits still cost the shield and the armour above it. And the finale's crossing from the water into
 * the air at its line on the resumed level clock (Level 28's breakout).
 */
class ArmourFloorTest {
    /** The halted arena (3 s, the arena to 4 s), the climb after it; under water to 6 s, then the air. */
    private static LevelScript level(double floor) {
        return MawSpecs.level(MawSpecs.Fit.QUIET, List.of()).toBuilder()
                .medium(new MediumSpec(
                        UnderwaterTest.EUROPA,
                        List.of(new MediumSpec.Switch(0, true), new MediumSpec.Switch(6, false))))
                .armourFloor(floor)
                .build();
    }

    private static Sortie sortie(double floor) {
        return new Sortie(1, TestSpecs.LOADOUT, level(floor), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** Kills the Maw once it is up. */
    private static void kill(Sortie sortie) {
        MawSpecs.untilUp(sortie);
        sortie.destroyPart(0, MawSpecs.HEAD);
        assertTrue(MawSpecs.boss(sortie).destroyed());
    }

    /** A heavy bullet on the ship every step for {@code seconds}. */
    private static void pound(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds) && !sortie.wrecked; i++) {
            Ship ship = sortie.ship();
            sortie.fireEnemyBullet(ship.x(), ship.y(), 0, 0, 40);
            sortie.step(0);
        }
    }

    @Test
    void fromTheKillTheArmourStopsAtItsFloorUntilTheLevelsEnd() {
        Sortie sortie = sortie(1);
        Defences defences = sortie.ship().defences();
        MawSpecs.untilUp(sortie);
        assertEquals(0, defences.floor(), "no floor before the kill");
        sortie.destroyPart(0, MawSpecs.HEAD);
        assertEquals(1, defences.floor(), "the floor from the kill");
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            Ship ship = sortie.ship();
            sortie.fireEnemyBullet(ship.x(), ship.y(), 0, 0, 40);
            sortie.step(0);
            hits += sortie.events().count(SimEvents.Type.ARMOUR_HIT);
        }
        assertFalse(sortie.wrecked, "the run cannot be lost");
        assertEquals(1, defences.armour(), 1e-9, "the armour stops at 1");
        assertTrue(hits > 3, "the hits still land: " + hits);
        assertEquals(TestSpecs.FULL_ARMOUR - 1, defences.armourLost(), 1e-9, "only what it lost above the floor");
        // To the level's end.
        while (!sortie.complete() && !sortie.wrecked) {
            Ship ship = sortie.ship();
            sortie.fireEnemyBullet(ship.x(), ship.y(), 0, 0, 40);
            sortie.step(0);
        }
        assertFalse(sortie.wrecked);
        assertEquals(1, defences.armour(), 1e-9);
    }

    @Test
    void aRetryOrALevelWithoutTheKeyHasNoFloor() {
        Sortie sortie = sortie(1);
        kill(sortie);
        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(0);
        assertEquals(0, sortie.ship().defences().floor(), "a new attempt starts without it");
        pound(sortie, 8);
        assertTrue(sortie.wrecked, "before the kill the ship can be lost");
        Sortie without = sortie(0);
        kill(without);
        assertEquals(0, without.ship().defences().floor());
        pound(without, 8);
        assertTrue(without.wrecked, "without the key the run can be lost");
    }

    @Test
    void theFloorNeedsABossAndIsNeverNegative() {
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(28, 4)
                .sections(List.of(new Section(30, 120)))
                .armourFloor(1)
                .build());
        assertThrows(IllegalArgumentException.class, () -> level(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> sortie(0).ship().defences().floor(-1));
    }

    @Test
    void theWaterTurnsToAirAtItsLineOnTheResumedClock() {
        Sortie sortie = sortie(1);
        MawSpecs.untilUp(sortie);
        MawSpecs.steps(sortie, 5, 0);
        assertTrue(sortie.underwater(), "under water in the halted arena");
        assertEquals(MawSpecs.ARRIVE + 1, sortie.levelSeconds(), SimStep.SECONDS + 1e-9, "the clock halted");
        sortie.destroyPart(0, MawSpecs.HEAD);
        while (sortie.levelSeconds() < 6 - 1e-9) {
            assertTrue(sortie.underwater(), "under water until the line: " + sortie.levelSeconds());
            sortie.step(0);
        }
        assertFalse(sortie.underwater(), "the air from the line");
        assertTrue(sortie.waterBlend() > 0, "the factors ease out");
        MawSpecs.steps(sortie, 1, 0);
        assertEquals(0, sortie.waterBlend(), 1e-9, "out over the rules' 1 s");
    }
}
