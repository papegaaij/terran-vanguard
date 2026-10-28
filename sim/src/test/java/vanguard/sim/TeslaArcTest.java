package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M9 part A, step A1 (design/player/weapons/tesla-coil-pod): the {@link WeaponSpec.Delivery#ARC}: on its fire step a
 * pod strikes the nearest valid target within its reach in a 240° forward spread at once, a shielded unit first, a
 * seekable missile only with no unit in reach; no target, no arc (and no cooldown spent). Its hit is {@code
 * shield-breaker}; its sparks deal half its damage to the other units within 24 px of the struck one. A hardened unit
 * glances it off (never picked). The two pods pick on their own.
 */
class TeslaArcTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double POD_X = Ship.START_X - 20;

    private static Armament.Mount left(WeaponSpec weapon) {
        return new Armament.Mount(Armament.Slot.LEFT_WING, weapon, weapon);
    }

    private static Armament.Mount right(WeaponSpec weapon) {
        return new Armament.Mount(Armament.Slot.RIGHT_WING, weapon, weapon);
    }

    private static Sortie sortie(LevelScript level, Armament.Mount... mounts) {
        return new Sortie(
                1, TestSpecs.loadout(mounts), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static GroundUnit unit(double x, EnemySpec spec) {
        return new GroundUnit(0, x, spec, -1);
    }

    private static EnemySpec dummy(double hp) {
        return TorpedoTest.dummy(Layer.AIR, hp);
    }

    /** Runs until the first unit's centre is within {@code px} of the pod (they all scroll down together). */
    private static void approach(Sortie sortie, double px) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            if (sortie.enemyCount() > 0 && sortie.enemy(0).y() - (Ship.START_Y + 4) <= px) {
                return;
            }
            sortie.step(Command.NONE);
        }
        throw new AssertionError("no unit came in reach");
    }

    private static Enemy at(Sortie sortie, double x) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (Math.abs(sortie.enemy(i).x() - x) < 1) {
                return sortie.enemy(i);
            }
        }
        throw new AssertionError("no unit at " + x);
    }

    static List<Integer> values(SimEvents events, SimEvents.Type type) {
        List<Integer> values = new java.util.ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                values.add(events.value(i));
            }
        }
        return values;
    }

    private static int count(Sortie sortie, int steps, int commands, SimEvents.Type type) {
        return TorpedoTest.count(sortie, steps, commands, type);
    }

    @Test
    void withoutATargetInReachThePodDoesNotFire() {
        Sortie sortie = sortie(TorpedoTest.units(false), left(Act5Specs.TESLA));
        assertEquals(0, count(sortie, SimStep.ticks(1), FIRE, SimEvents.Type.SHOT_FIRED));
        assertEquals(Integer.MAX_VALUE, sortie.ticksSinceShot(0), "no flash, no arc into empty space");

        Sortie far = sortie(TorpedoTest.units(false, unit(POD_X, dummy(100))), left(Act5Specs.TESLA));
        approach(far, 160);
        assertEquals(0, count(far, 1, FIRE, SimEvents.Type.ARC), "160 px: out of its 140 px reach");
    }

    @Test
    void itStrikesTheNearestAtOnceFourTimesASecond() {
        Sortie sortie = sortie(TorpedoTest.units(false, unit(POD_X, dummy(100))), left(Act5Specs.TESLA));
        approach(sortie, 130);
        sortie.step(FIRE);
        assertEquals(1, sortie.events().count(SimEvents.Type.ARC), "on the fire step: no projectile");
        assertEquals(0, sortie.shotCount());
        Enemy target = sortie.enemy(0);
        assertEquals(97, target.hp(), 1e-9);
        assertEquals(target.x(), sortie.fire.arcX(0), 1e-9, "the arc's end for the look");
        assertEquals(1, count(sortie, SimStep.ticks(0.25), FIRE, SimEvents.Type.ARC), "4 arcs a second");
        assertEquals(94, target.hp(), 1e-9);
    }

    @Test
    void aShieldedUnitInReachIsPickedFirstAndItsShieldBrokenWithTheWholeHitOnTheHull() {
        Sortie sortie = sortie(
                TorpedoTest.units(false, unit(POD_X, dummy(100)), unit(POD_X + 90, Act5Specs.TALON)),
                left(Act5Specs.TESLA));
        approach(sortie, 100);
        sortie.step(FIRE);
        Enemy talon = at(sortie, POD_X + 90);
        assertEquals(0, talon.shield(), "shield-breaker: emptied at once");
        assertEquals(17, talon.hp(), 1e-9, "the arc's whole 3 on the hull");
        assertTrue(talon.shieldHeld(), "its refill held 3 s");
        assertEquals(1, sortie.events().count(SimEvents.Type.ENEMY_SHIELD_BROKEN));
        assertEquals(100, at(sortie, POD_X).hp(), "the nearer plain unit waits");
        // The next arc: the Talon's shield is down, so the nearest of all.
        TorpedoTest.run(sortie, SimStep.ticks(0.25), FIRE);
        assertEquals(97, at(sortie, POD_X).hp(), 1e-9);
    }

    @Test
    void itsSparksHitTheOtherUnitsWithinTwentyFourPixelsForHalf() {
        Sortie sortie = sortie(
                TorpedoTest.units(
                        false, unit(POD_X, dummy(100)), unit(POD_X + 30, dummy(100)), unit(POD_X - 70, dummy(100))),
                left(Act5Specs.TESLA));
        approach(sortie, 60);
        sortie.step(FIRE);
        assertEquals(97, at(sortie, POD_X).hp(), 1e-9, "struck");
        assertEquals(98.5, at(sortie, POD_X + 30).hp(), 1e-9, "a spark: 10 px between the boxes");
        assertEquals(100, at(sortie, POD_X - 70).hp(), "out of the sparks");
    }

    @Test
    void aHardenedUnitIsNeverPickedAndOneBehindTheShipIsOutOfItsSpread() {
        EnemySpec hardened = TorpedoTest.dummy(Layer.GROUND, 100).withHardened(true);
        Sortie only = sortie(TorpedoTest.units(false, unit(POD_X, hardened)), left(Act5Specs.TESLA));
        approach(only, 60);
        assertEquals(0, count(only, SimStep.ticks(0.5), FIRE, SimEvents.Type.ARC), "the arc glances off: no pick");

        Sortie behind = sortie(TorpedoTest.units(false, unit(POD_X, dummy(100))), left(Act5Specs.TESLA));
        approach(behind, -60);
        assertEquals(0, count(behind, 1, FIRE, SimEvents.Type.ARC), "straight behind: the 120° wedge");
        Sortie beside = sortie(TorpedoTest.units(false, unit(POD_X + 120, dummy(100))), left(Act5Specs.TESLA));
        approach(beside, -30);
        assertEquals(1, count(beside, 1, FIRE, SimEvents.Type.ARC), "beside and a little behind: in the 240°");
    }

    @Test
    void twoPodsPickOnTheirOwnAndMayStrikeTheSameTarget() {
        Sortie sortie = sortie(
                TorpedoTest.units(false, unit(Ship.START_X, dummy(100))),
                left(Act5Specs.tesla(-20)),
                right(Act5Specs.tesla(20)));
        approach(sortie, 100);
        sortie.step(FIRE);
        assertEquals(2, sortie.events().count(SimEvents.Type.ARC));
        assertEquals(94, sortie.enemy(0).hp(), 1e-9);
    }

    @Test
    void aSeekableMissileIsStruckOnlyWithNoUnitInReach() {
        Sortie sortie = sortie(TorpedoTest.units(false), left(Act5Specs.TESLA));
        sortie.step(Command.NONE);
        sortie.missiles().launch(POD_X, Ship.START_Y + 120, Act5Specs.DIAMOND, POD_X, Ship.START_Y);
        sortie.step(FIRE);
        assertEquals(1, sortie.events().count(SimEvents.Type.ARC));
        assertEquals(
                List.of(Missiles.SHOT_DOWN),
                values(sortie.events(), SimEvents.Type.MISSILE_DESTROYED),
                "1 HP, 3 damage");

        Sortie withUnit = sortie(TorpedoTest.units(false, unit(POD_X + 60, dummy(100))), left(Act5Specs.TESLA));
        approach(withUnit, 110);
        withUnit.missiles().launch(POD_X, Ship.START_Y + 60, Act5Specs.DIAMOND, POD_X, Ship.START_Y);
        withUnit.step(FIRE);
        assertEquals(97, withUnit.enemy(0).hp(), 1e-9, "the unit first (QA-1)");
        assertEquals(1, withUnit.missiles().size());

        EnemyGun plain = Act5Specs.DIAMOND.withHoming(Act5Specs.DIAMOND.homing().map(spec -> spec.withSeekable(false)));
        Sortie walker = sortie(TorpedoTest.units(false), left(Act5Specs.TESLA));
        walker.step(Command.NONE);
        walker.missiles().launch(POD_X, Ship.START_Y + 120, plain, POD_X, Ship.START_Y);
        assertEquals(0, count(walker, 1, FIRE, SimEvents.Type.ARC), "an Act 4 missile is never picked");
    }

    @Test
    void itReplaysToTheSameHashAndAllocatesNothing() {
        Supplier<Sortie> make = () -> sortie(
                TorpedoTest.units(
                        false,
                        unit(POD_X, dummy(400)),
                        unit(POD_X + 90, Act5Specs.talon(400, 20)),
                        unit(POD_X + 30, dummy(400))),
                left(Act5Specs.tesla(-20)),
                right(Act5Specs.tesla(20)));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(8); i++) {
            a.step(FIRE);
            b.step(FIRE);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(8); i++) {
                sortie.step(FIRE);
            }
        });
        assertEquals(0, allocated);
    }
}
