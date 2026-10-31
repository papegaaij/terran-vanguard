package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/enemies/ground/graft-turret, Regrowth): the turret's first death pays its 15 and counts
 * once, and exposes its root (16 HP in 24×24) for 5 s; the root's death ends the unit for good and pays its bonus of 5;
 * the window closed, the turret knits back over 0.8 s (not hittable), then stands with its full HP and fires after a
 * further 0.5 s. A regrown turret's death pays and counts nothing and opens the window again; a blast that killed the
 * turret does not reach the root (hittable from the next step); its scrolling off counts nowhere.
 */
class RegrowthTest {
    /** A Graft Turret on a slow deck (20 px/s), so it stays on the screen through its cycle. */
    private static Sortie sortie() {
        return Act6ASpecs.sortie(Act6ASpecs.level(
                60, 20, List.of(new GroundUnit(0, Ship.START_X + 60, Act6ASpecs.GRAFT, -1)), List.of()));
    }

    @Test
    void theFirstDeathPaysAndCountsOnceAndExposesTheRoot() {
        Sortie sortie = sortie();
        Enemy turret = Act6ASpecs.until(sortie, "graft-turret", 40);
        int serial = turret.serial();
        sortie.destroyEnemy(0);
        assertEquals(15, sortie.credits(), "its 15");
        assertEquals(1, sortie.kills());
        assertEquals(1, sortie.events().count(SimEvents.Type.ROOT_EXPOSED));
        assertEquals(1, sortie.events().count(SimEvents.Type.ENEMY_DESTROYED), "the kill, once");
        Enemy root = Act6ASpecs.bySerial(sortie, serial);
        assertTrue(root == turret, "the unit stays through the cycle");
        assertEquals(Regrowing.Phase.ROOT, root.regrowth().phase());
        assertEquals(16, root.hp(), 1e-9);
        assertEquals(24, root.hitbox().width());
        assertTrue(root.invulnerable(), "the step it was exposed: a blast that killed the turret misses it");
        assertFalse(root.damage(5));
        sortie.step(Command.NONE);
        assertFalse(root.invulnerable());
        assertTrue(root.damage(16), "from the next step every weapon hits it");
        sortie.destroyEnemy(Act6ASpecs.indexOf(sortie, serial));
        assertEquals(1, sortie.events().count(SimEvents.Type.ROOT_KILLED));
        assertEquals(15 + 5, sortie.credits(), "the root's bonus");
        assertEquals(1, sortie.kills(), "no second kill");
        assertNull(Act6ASpecs.bySerial(sortie, serial), "gone for good");
    }

    @Test
    void theWindowClosedTheTurretKnitsBackRegrowsAndAgainPaysNothing() {
        Sortie sortie = sortie();
        Enemy turret = Act6ASpecs.until(sortie, "graft-turret", 40);
        sortie.destroyEnemy(0);
        int fired = 0;
        int knits = 0;
        for (int i = 0; i < SimStep.ticks(5.0); i++) {
            sortie.step(Command.NONE);
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            knits += sortie.events().count(SimEvents.Type.KNITTING);
        }
        assertEquals(0, fired, "silent while its root is exposed");
        assertEquals(1, knits, "the window closed after 5 s");
        assertEquals(Regrowing.Phase.KNIT, turret.regrowth().phase());
        assertTrue(turret.invulnerable(), "not hittable while it knits");
        int regrown = 0;
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(Command.NONE);
            regrown += sortie.events().count(SimEvents.Type.REGROWN);
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertEquals(1, regrown);
        assertEquals(0, fired);
        assertEquals(Regrowing.Phase.TURRET, turret.regrowth().phase());
        assertEquals(24, turret.hp(), 1e-9, "its full HP");
        assertEquals(32, turret.hitbox().width());
        int credits = sortie.credits();
        sortie.destroyEnemy(Act6ASpecs.indexOf(sortie, turret.serial()));
        assertEquals(credits, sortie.credits(), "a regrown turret pays nothing");
        assertEquals(1, sortie.kills(), "and counts nothing");
        assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_DESTROYED));
        assertEquals(Regrowing.Phase.ROOT, turret.regrowth().phase(), "the window opens again");
    }

    @Test
    void aRegrownTurretFiresItsWakeAfterItStands() {
        Sortie sortie = sortie();
        Act6ASpecs.until(sortie, "graft-turret", 40);
        sortie.destroyEnemy(0);
        Act6ASpecs.run(sortie, SimStep.ticks(5.0) + SimStep.ticks(0.8), Command.NONE);
        int firstShot = -1;
        for (int i = 0; i < SimStep.ticks(1.0) && firstShot < 0; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                firstShot = i + 1;
            }
        }
        assertEquals(SimStep.ticks(0.5), firstShot, "its first pair 0.5 s after it stands");
    }

    @Test
    void scrollingOffAfterItsFirstDeathCountsNowhere() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(40, List.of(new GroundUnit(0, 240, Act6ASpecs.GRAFT, -1)), List.of()));
        Act6ASpecs.until(sortie, "graft-turret", 40);
        sortie.destroyEnemy(0);
        Act6ASpecs.run(sortie, SimStep.ticks(8), Command.NONE);
        assertEquals(0, sortie.enemyCount(), "off the bottom");
        assertEquals(1, sortie.kills());
        assertEquals(1, sortie.enemyTotal(), "counted once among the level's enemies");
    }

    @Test
    void aRegrowthIsAFixedGroundUnits() {
        assertThrows(IllegalArgumentException.class, () -> Act6ASpecs.air(10)
                .withRegrowth(Optional.of(Act6ASpecs.REGROWTH)));
        assertThrows(IllegalArgumentException.class, () -> Act6ASpecs.GRAFT.withDome(Optional.of(Act6ASpecs.dome(0))));
    }
}
