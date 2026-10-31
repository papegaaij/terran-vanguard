package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (design/player/shields, Reflex Shield (Act 6)): while the shield holds at least 75 % of its
 * capacity now (the Overcharge's raised capacity too) an enemy bullet costs it half its damage and comes back as the
 * ship's bolt, straight up at 640 px/s, dealing twice the bullet's damage and paying as the ship's; below the window,
 * and for a mirror's bounced bullet, it is an ordinary shield.
 */
class ReflexShieldTest {
    private static Sortie sortie(List<GroundUnit> units) {
        Loadout loadout = Loadout.builder(
                        TestSpecs.SHIP, TestSpecs.LOADOUT.armament(), Act6BSpecs.REFLEX, TestSpecs.PLATING)
                .build();
        return Act6BSpecs.sortie(Act6BSpecs.level(20, HazardSpec.NONE, units, List.of()), loadout);
    }

    /** An enemy bullet of {@code damage} just above the ship, flying down at it. */
    private static EnemyBullet bulletAtShip(Sortie sortie, double damage) {
        EnemyBullet bullet = sortie.force.bullets().obtain();
        bullet.fire(sortie.ship.x(), sortie.ship.y() + 20, 0, -200, damage);
        return bullet;
    }

    /** Steps until the bullets in flight are gone (at most a second); returns the {@code REFLEX} events. */
    private static int land(Sortie sortie) {
        int reflexes = 0;
        for (int i = 0; i < SimStep.ticks(1) && sortie.bulletCount() > 0; i++) {
            sortie.step(0);
            reflexes += Act6BSpecs.count(sortie, SimEvents.Type.REFLEX);
        }
        return reflexes;
    }

    private static Shot bolt(Sortie sortie) {
        Pool<Shot> shots = sortie.fire.shots();
        for (int i = 0; i < shots.size(); i++) {
            if (shots.get(i).bounced()) {
                return shots.get(i);
            }
        }
        return null;
    }

    @Test
    void aBulletInsideTheWindowCostsHalfAndComesBackAsABolt() {
        Sortie sortie = sortie(List.of());
        bulletAtShip(sortie, 4);
        assertEquals(1, land(sortie));
        assertEquals(38, sortie.ship.defences().shield(), 1e-9, "half of the bullet's 4");
        Shot bolt = bolt(sortie);
        assertNotNull(bolt);
        assertEquals(8, bolt.damage(), 1e-9, "twice the bullet's damage");
        assertEquals(sortie.fire.boltMount(), bolt.mount());
        double y = bolt.y();
        sortie.step(0);
        assertEquals(640 * SimStep.SECONDS, bolt.y() - y, 1e-6, "straight up at 640 px/s");
    }

    @Test
    void belowTheWindowAndForABouncedBulletItIsAnOrdinaryShield() {
        Sortie sortie = sortie(List.of());
        sortie.ship.defences().restore(29, TestSpecs.FULL_ARMOUR, 0);
        bulletAtShip(sortie, 4);
        assertEquals(0, land(sortie), "29 of 40 is below 75 %");
        assertEquals(25, sortie.ship.defences().shield(), 0.5, "the full 4 (from 29 and its regen)");
        assertNull(bolt(sortie));
        Sortie bounced = sortie(List.of());
        bulletAtShip(bounced, 4).markBounced();
        assertEquals(0, land(bounced), "a mirror's bounced bullet is never reflected");
        assertEquals(36, bounced.ship.defences().shield(), 1e-9);
        assertNull(bolt(bounced));
    }

    @Test
    void theWindowFollowsTheCapacityNowTheOverchargesToo() {
        Defences reflex = new Defences(Act6BSpecs.REFLEX, TestSpecs.PLATING, 0.25);
        SimEvents events = new SimEvents(16);
        assertTrue(reflex.reflects());
        reflex.takeShot(10, events, 0, 0);
        assertTrue(reflex.reflects(), "30 of 40");
        reflex.takeShot(0.5, events, 0, 0);
        assertFalse(reflex.reflects(), "29.5 of 40");
        reflex.overcharge(2, SimStep.ticks(8), SimStep.ticks(1));
        assertEquals(80, reflex.shield(), 1e-9);
        reflex.takeShot(20, events, 0, 0);
        assertTrue(reflex.reflects(), "60 of the overcharged 80");
        reflex.takeShot(0.5, events, 0, 0);
        assertFalse(reflex.reflects(), "59.5 of 80");
        Defences plain = new Defences(TestSpecs.SHIELD, TestSpecs.PLATING, 0.25);
        assertFalse(plain.reflects(), "only the Reflex reflects");
    }

    @Test
    void theBoltPaysAsTheShipsKill() {
        EnemySpec above = Act6BSpecs.target("above", Layer.AIR, 6);
        // It rides the ground down to the ship's row's column; the bolt meets it on its way up.
        Sortie sortie = sortie(List.of(new GroundUnit(0, Ship.START_X, above, -1)));
        while (Act6BSpecs.find(sortie, "above") == null
                || Act6BSpecs.find(sortie, "above").y() > sortie.ship.y() + 200) {
            sortie.step(0);
        }
        bulletAtShip(sortie, 4);
        assertEquals(1, land(sortie));
        for (int i = 0; i < SimStep.ticks(1) && Act6BSpecs.find(sortie, "above") != null; i++) {
            sortie.step(0);
        }
        assertNull(Act6BSpecs.find(sortie, "above"), "the 8-damage bolt kills the 6-HP unit");
        assertEquals(1, sortie.kills());
        assertTrue(sortie.credits() > 0, "and pays");
    }
}
