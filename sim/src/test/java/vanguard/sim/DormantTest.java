package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1 (design/enemies/ground/sam-nest, Dormant until t; Level 29's Block C; m9b-gaps.md §1.3): a
 * dormant unit is dark scenery until its time on the level clock: not hittable (shots pass), on no layer anything
 * touches, not firing; then it wakes ({@link SimEvents.Type#WOKE}) with a 1 s tell and fires as its stat block says.
 */
class DormantTest {
    private static final int FIRE = Command.FIRE.bit();

    private static Sortie sortie() {
        GroundUnit nest = new GroundUnit(0.5, PlayField.WIDTH / 2.0, Act5BSpecs.NEST, -1).dormantUntil(4);
        return TestSpecs.sortie(LevelScript.builder(29, 5)
                .sections(List.of(new Section(20, 60)))
                .groundUnits(List.of(nest))
                .objectives(ObjectiveSpec.NONE)
                .build());
    }

    private static Enemy nest(Sortie sortie) {
        return PartCSpecs.find(sortie, "sam-nest");
    }

    @Test
    void itIsScenery_untilItsTime_thenWakesAndFiresAfterItsTell() {
        Sortie sortie = sortie();
        int woke = -1;
        int firstBullet = -1;
        for (int step = 1; step <= SimStep.ticks(8); step++) {
            sortie.step(0);
            Enemy nest = nest(sortie);
            if (nest != null && sortie.levelSeconds() < 4 - SimStep.SECONDS) {
                assertTrue(nest.dormant());
                assertEquals(Layer.BURIED, nest.layer());
            }
            if (PartCSpecs.count(sortie, SimEvents.Type.WOKE) > 0) {
                woke = step;
                assertTrue(nest.waking());
            }
            if (firstBullet < 0 && sortie.bulletCount() > 0) {
                firstBullet = step;
            }
        }
        assertEquals(SimStep.ticks(4), woke, "it wakes at its time");
        assertEquals(woke + SimStep.ticks(GroundUnit.WAKE_SECONDS), firstBullet, "its 1 s tell, then it fires");
    }

    @Test
    void shotsPassItWhileItIsDormant() {
        Sortie sortie = sortie();
        while (sortie.levelSeconds() < 3.9) {
            sortie.step(FIRE);
        }
        Enemy nest = nest(sortie);
        assertEquals(Act5BSpecs.NEST.hp(), nest.hp(), "untouched while dormant");
        assertEquals(0, sortie.kills());
        boolean destroyed = false;
        for (int i = 0; i < SimStep.ticks(4) && !destroyed; i++) {
            sortie.step(FIRE);
            destroyed = PartCSpecs.count(sortie, SimEvents.Type.ENEMY_DESTROYED) > 0;
        }
        assertTrue(destroyed, "awake, it is shot down");
    }

    @Test
    void aDormantUnitWakesAfterItEnteredAtTheTopEdge() {
        assertThrows(IllegalArgumentException.class, () -> new GroundUnit(5, 100, Act5BSpecs.NEST, -1).dormantUntil(4));
        assertFalse(new GroundUnit(5, 100, Act5BSpecs.NEST, -1).dormant());
    }
}
