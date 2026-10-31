package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/enemies/air/harrow; design/enemies, the {@code death-burst} on contact): a Harrow's ring
 * of 8 slow {@code small} bullets comes on every death on the screen, a ram on the ship and a kill at point-blank range
 * included (an older unit's ring still waits until it is clear of the ship); it is turned so that the ship's bearing
 * falls between two bullets; after a ram its bullets cannot hit the ship for their first 0.25 s.
 */
class DeathBurstContactTest {
    /** A Harrow riding the ground straight down onto the ship (the ship can be hit). */
    private static Sortie rammed() {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X, Act6ASpecs.HARROW, -1)), List.of()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    /** The smallest angle between {@code bearing} and a bullet's heading from (x, y). */
    private static double nearestBullet(Sortie sortie, double bearing) {
        double nearest = Math.PI;
        for (int b = 0; b < sortie.bulletCount(); b++) {
            EnemyBullet bullet = sortie.bullet(b);
            double angle = Math.atan2(bullet.vy(), bullet.vx());
            nearest = Math.min(nearest, Math.abs(Math.IEEEremainder(angle - bearing, 2 * Math.PI)));
        }
        return nearest;
    }

    @Test
    void aRamBurstsItsRingTurnedBetweenTheShipWhichItCannotHitForAQuarterSecond() {
        Sortie sortie = rammed();
        Act6ASpecs.until(sortie, "harrow", 20);
        int steps = 0;
        while (sortie.enemyCount() > 0 && steps++ < SimStep.ticks(10)) {
            sortie.step(Command.NONE);
        }
        assertEquals(0, sortie.enemyCount(), "it died on the ship");
        assertEquals(1, sortie.kills(), "a ram pays as a kill");
        assertEquals(8, sortie.bulletCount(), "its ring, however close the ship is");
        double shield = sortie.ship().defences().shield();
        double armour = sortie.ship().defences().armour();
        EnemyBullet first = sortie.bullet(0);
        assertEquals(90, Math.hypot(first.vx(), first.vy()), 1e-9);
        // Between two bullets: the ship's bearing from the burst lies half a gap (22.5°) from the nearest.
        double bearing = Math.atan2(sortie.ship().y() - first.y(), sortie.ship().x() - first.x());
        assertEquals(Math.PI / 8, nearestBullet(sortie, bearing), 1e-9);
        for (int i = 0; i < SimStep.ticks(0.25) - 1; i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.SHIELD_HIT), "its grace");
        }
        assertEquals(shield, sortie.ship().defences().shield(), 1e-9);
        assertEquals(armour, sortie.ship().defences().armour(), 1e-9);
    }

    @Test
    void killedAtPointBlankRangeItStillBurstsButAnOlderRingWaits() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X + 40, Act6ASpecs.HARROW, -1)), List.of()));
        Act6ASpecs.until(sortie, "harrow", PlayField.HEIGHT - Ship.START_Y - 20);
        sortie.destroyEnemy(0);
        assertEquals(8, sortie.bulletCount(), "its ring within 72 px of the ship");

        EnemySpec old = Act6ASpecs.HARROW.withDeathBurst(Optional.of(new EnemySpec.DeathBurst(8, 90, 4)));
        Sortie before = Act6ASpecs.sortie(
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X + 40, old, -1)), List.of()));
        Act6ASpecs.until(before, "harrow", PlayField.HEIGHT - Ship.START_Y - 20);
        before.destroyEnemy(0);
        assertEquals(0, before.bulletCount(), "an Act 1–5 ring never spawns this close");
    }

    @Test
    void aShotKillFarOffTurnsItsRingBetweenTheShipWithoutAGrace() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X + 100, Act6ASpecs.HARROW, -1)), List.of()));
        Enemy harrow = Act6ASpecs.until(sortie, "harrow", 100);
        double bearing = Math.atan2(Ship.START_Y - harrow.y(), Ship.START_X - harrow.x());
        sortie.destroyEnemy(0);
        assertEquals(8, sortie.bulletCount());
        assertEquals(Math.PI / 8, nearestBullet(sortie, bearing), 1e-9);
        assertTrue(sortie.bullet(0).hitsShip(), "no grace off a ram");
    }

    @Test
    void aGraceIsABurstOnContacts() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.DeathBurst(8, 90, 4, false, false, 0.25));
    }
}
