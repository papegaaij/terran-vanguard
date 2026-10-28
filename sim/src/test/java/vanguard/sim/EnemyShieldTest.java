package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part A, step A1 (design/enemies, Enemy shields): a hex pool over the hull that every hit spends first at its full
 * damage, the overflow to the hull; after 2 s without a hit it refills at 50 % of its maximum a second; a {@code
 * shield-breaker} hit empties it at once (its whole damage to the hull) and holds the refill 3 s; the EMP Burst empties
 * every shield on the screen and holds the refill for the machine stun. A unit without a shield hashes as before.
 */
class EnemyShieldTest {
    private final SimEvents events = new SimEvents(64);
    private final Pool<Enemy> pool = new Pool<>(8, () -> new Enemy(events), Enemy[]::new);
    private int serials;

    private Enemy add(EnemySpec spec, double x) {
        Enemy enemy = pool.obtain();
        enemy.root(spec, 0, x, serials++, -1);
        return enemy;
    }

    private void steps(int count) {
        for (int i = 0; i < count; i++) {
            events.clear();
            for (int j = 0; j < pool.size(); j++) {
                pool.get(j).move(Ship.START_X, Ship.START_Y, 0);
            }
        }
    }

    private Enemy talon(double shield) {
        Enemy unit = add(Act5Specs.talon(20, shield), 200);
        pool.get(0).move(Ship.START_X, Ship.START_Y, 300);
        return unit;
    }

    @Test
    void aHitSpendsTheShieldFirstAndTheOverflowGoesToTheHull() {
        Enemy unit = talon(4);
        assertEquals(4, unit.shield());
        assertEquals(4, unit.shieldMax());
        events.clear();
        assertFalse(unit.damage(3));
        assertEquals(1, unit.shield(), 1e-9);
        assertEquals(20, unit.hp(), "the shield took it all");
        assertEquals(1, events.count(SimEvents.Type.ENEMY_SHIELD_HIT));
        assertFalse(unit.damage(5, true), "a blast from above too (every weapon hits it)");
        assertEquals(0, unit.shield());
        assertEquals(16, unit.hp(), 1e-9, "the overflow");
        assertEquals(1, events.count(SimEvents.Type.ENEMY_SHIELD_BROKEN));
        assertFalse(unit.damage(5));
        assertEquals(11, unit.hp(), 1e-9, "a shield that is down absorbs nothing");
    }

    @Test
    void itRefillsAtHalfItsMaximumASecondAfterTwoSecondsWithoutAHit() {
        Enemy unit = talon(4);
        unit.damage(4);
        steps(SimStep.ticks(2.0));
        assertEquals(0, unit.shield(), "the delay");
        steps(1);
        assertEquals(4 * 0.5 / SimStep.PER_SECOND, unit.shield(), 1e-9, "refilling at 2 points a second");
        unit.damage(1);
        steps(SimStep.ticks(2.0));
        assertEquals(0, unit.shield(), "a hit on a shield that is down restarts the delay");
        events.clear();
        int refilled = 0;
        for (int i = 0; i < SimStep.ticks(2.1); i++) {
            for (int j = 0; j < pool.size(); j++) {
                pool.get(j).move(Ship.START_X, Ship.START_Y, 0);
            }
            refilled += events.count(SimEvents.Type.ENEMY_SHIELD_REFILLED);
            events.clear();
        }
        assertEquals(4, unit.shield(), 1e-9, "full after 2 s of refill");
        assertEquals(1, refilled, "the shimmer once, as it is full");
    }

    @Test
    void aBreakerHitEmptiesItAtOnceSendsTheWholeHitToTheHullAndHoldsTheRefillThreeSeconds() {
        Enemy unit = talon(4);
        events.clear();
        assertFalse(unit.damageBreaking(3));
        assertEquals(0, unit.shield());
        assertEquals(17, unit.hp(), 1e-9, "the whole hit to the hull");
        assertEquals(1, events.count(SimEvents.Type.ENEMY_SHIELD_BROKEN));
        assertTrue(unit.shieldHeld());
        steps(SimStep.ticks(3.0));
        assertEquals(0, unit.shield(), "held 3 s");
        steps(1);
        assertTrue(unit.shield() > 0, "it refills from the hold's end");
        assertFalse(unit.shieldHeld());

        Enemy plain = add(PartASpecs.dummy(Layer.AIR, 20), 300);
        assertFalse(plain.damageBreaking(3));
        assertEquals(17, plain.hp(), 1e-9, "a unit without a shield takes it as any hit");
    }

    @Test
    void theEmpEmptiesItAndHoldsItsRefillForTheStun() {
        Enemy unit = talon(4);
        int stun = SimStep.ticks(3.0);
        assertTrue(unit.stripShield(stun));
        unit.empStun(stun);
        assertEquals(0, unit.shield());
        steps(stun);
        assertEquals(0, unit.shield(), "empty for the stun");
        steps(1);
        assertTrue(unit.shield() > 0);
        assertFalse(add(PartASpecs.dummy(Layer.AIR, 20), 300).stripShield(stun), "nothing to strip");
    }

    @Test
    void aUnitPartTakesTheShieldFirstToo() {
        EnemySpec spec = DrillRigTest.RIG.withShield(Optional.of(Act5Specs.shield(5)));
        Enemy rig = add(spec, 240);
        rig.move(Ship.START_X, Ship.START_Y, 200);
        rig.damagePart(0, 3, false);
        assertEquals(1, rig.partShare(0), 1e-9, "the shield took it");
        assertEquals(2, rig.shield(), 1e-9);
        rig.damagePart(0, 3, false);
        assertTrue(rig.partShare(0) < 1, "the overflow reaches the part");
    }
}
