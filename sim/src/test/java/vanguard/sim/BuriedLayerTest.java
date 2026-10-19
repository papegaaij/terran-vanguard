package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.muzzle;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M7 part B, step B1: the {@code buried} layer (design/enemies/ground/burrower, M7 = a): appended after
 * {@code sub}, hit by nothing — bolts, homing missiles, turrets, torpedoes, bombs, mines, the beam, the
 * drones, the Smart Bomb (which does hit {@code sub}), the Airstrike, the EMP Burst, the Orbital Lance —
 * never picked by Rook, and nothing on it touches the ship. Each case runs a unit on the buried layer
 * beside the same unit on {@code ground} (where the weapon reaches it) under the ship.
 */
class BuriedLayerTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final int SPECIAL = Command.SPECIAL.bit();

    private static EnemySpec unit(Layer layer, double hp) {
        return PartASpecs.dummy(layer, hp, new Hitbox(40, 40), false);
    }

    /** A buried unit and a ground one coming down with the scroll (130 px/s), {@code apart} px across. */
    private static LevelScript pair(double hp, double apart) {
        return PartASpecs.units(
                PartASpecs.unit(0, Ship.START_X, unit(Layer.BURIED, hp)),
                PartASpecs.unit(0, Ship.START_X + apart, unit(Layer.GROUND, hp)));
    }

    private static Enemy find(Sortie sortie, Layer layer) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().layer() == layer) {
                return sortie.enemy(i);
            }
        }
        throw new AssertionError("no unit on " + layer);
    }

    private static Sortie sortie(LevelScript level, Loadout loadout) {
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /**
     * Runs {@code seconds} with {@code commands}, asserting the buried unit is never struck: its HP stays
     * while it is on the field, and it leaves only off the bottom edge.
     */
    private static void runUntouched(Sortie sortie, double seconds, int commands, double hp) {
        double lastY = Double.NaN;
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            Enemy buried = null;
            for (int j = 0; j < sortie.enemyCount(); j++) {
                if (sortie.enemy(j).spec().layer() == Layer.BURIED) {
                    buried = sortie.enemy(j);
                }
            }
            if (buried == null) {
                assertTrue(Double.isNaN(lastY) || lastY < 40, "gone only off the bottom edge: " + lastY);
                continue;
            }
            assertEquals(hp, buried.hp(), "the buried unit untouched at step " + i);
            assertFalse(buried.empStunned());
            lastY = buried.y();
        }
    }

    @Test
    void buriedIsAppendedAndNoDeliveryReachesIt() {
        assertEquals(Layer.values().length - 1, Layer.BURIED.ordinal());
        assertEquals(5, Layer.BURIED.ordinal());
        assertFalse(Layer.BURIED.touchable());
        assertFalse(Layer.BURIED.hitByStandardShots());
        assertFalse(Layer.BURIED.collidesWithPlayer());
        for (Layer layer : Layer.values()) {
            assertEquals(layer != Layer.BURIED, layer.touchable(), layer.name());
        }
        for (WeaponSpec.Delivery delivery : WeaponSpec.Delivery.values()) {
            assertFalse(delivery.reaches(Layer.BURIED), delivery.name());
        }
    }

    @Test
    void boltsPassOverItAndItPassesUnderTheShipWithoutContact() {
        var sortie = new Sortie(
                1,
                TestSpecs.LOADOUT,
                PartASpecs.units(PartASpecs.unit(0, Ship.START_X, unit(Layer.BURIED, 10))),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(FIRE);
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_HIT));
        }
        assertEquals(TestSpecs.SHIELD.capacity(), sortie.ship().defences().shield(), "no contact");
        assertEquals(TestSpecs.FULL_ARMOUR, sortie.ship().defences().armour(), "no contact");
    }

    @Test
    void homingMissilesNeverLockOnIt() {
        var sortie = sortie(
                PartASpecs.units(PartASpecs.unit(0, Ship.START_X, unit(Layer.BURIED, 10))),
                TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, TestSpecs.HOMING, TestSpecs.HOMING)));
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(FIRE);
            for (int s = 0; s < sortie.shotCount(); s++) {
                assertEquals(-1, sortie.shot(s).target(), "no lock on a buried unit");
            }
        }
        assertEquals(10, find(sortie, Layer.BURIED).hp());
    }

    @Test
    void bombsAndMinesBlastTheGroundUnitButNotTheBuriedOne() {
        WeaponSpec bombs = new WeaponSpec(
                "bomb-rack",
                "bomb",
                "bomb",
                WeaponSpec.Delivery.DROPPED,
                false,
                3,
                10,
                0,
                new Hitbox(8, 8),
                0,
                INFINITE,
                1,
                60,
                0,
                Math.PI,
                0.3,
                0,
                List.of(muzzle(0, 0, 0)));
        var bombed =
                sortie(pair(1000, 30), TestSpecs.loadout(new Armament.Mount(Armament.Slot.RIGHT_WING, bombs, bombs)));
        runUntouched(bombed, 4.4, FIRE, 1000);
        assertTrue(find(bombed, Layer.GROUND).hp() < 1000, "the bombs reach the ground unit beside it");
    }

    @Test
    void theBeamBurnsTheGroundUnitBehindItButNeverStopsAtTheBuriedOne() {
        var sortie = sortie(
                PartASpecs.units(
                        PartASpecs.unit(0, Ship.START_X, unit(Layer.BURIED, 1000)),
                        PartASpecs.unit(0.5, Ship.START_X, unit(Layer.GROUND, 1000))),
                TestSpecs.loadout(PartASpecs.beamMount(1)));
        runUntouched(sortie, 4.4, FIRE, 1000);
        assertTrue(find(sortie, Layer.GROUND).hp() < 1000, "the column runs on to the ground unit");
    }

    @Test
    void theDronesNeitherShootNorTouchIt() {
        var sortie = sortie(
                PartASpecs.units(PartASpecs.unit(0, Ship.START_X, unit(Layer.BURIED, 1000))),
                TestSpecs.loadout(
                        PartASpecs.droneBay(Armament.Slot.LEFT_WING, 1),
                        PartASpecs.droneBay(Armament.Slot.RIGHT_WING, 1)));
        for (int i = 0; i < SimStep.ticks(4.4); i++) {
            sortie.step(FIRE);
            assertEquals(0, sortie.events().count(SimEvents.Type.DRONE_HIT));
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_HIT));
        }
        assertEquals(1000, find(sortie, Layer.BURIED).hp());
    }

    @Test
    void theSmartBombHitsTheGroundUnitButNotTheBuriedOne() {
        SmartBombSpec bomb = new SmartBombSpec(0.35, 120, 60, 1.0, 1.5, 0.1, 0.8, 0.25);
        var sortie =
                sortie(pair(100, 60), TestSpecs.LOADOUT.withSpecial(new SpecialSpec("Smart Bomb", 1, 3, 0.1, bomb)));
        runUntouched(sortie, 2.5, Command.NONE, 100);
        sortie.step(SPECIAL);
        runUntouched(sortie, 1, Command.NONE, 100);
        assertEquals(1, sortie.kills(), "the ground unit only");
    }

    @Test
    void theAirstrikeDoesNotReachUnderTheSand() {
        // SubLayerTest's set-up: the units as wide as the two carpets, the ground one 100 px behind.
        AirstrikeSpec spec = new AirstrikeSpec(0.6, 64, 600, new Hitbox(56, 64), 36, 0.25, 32, 100, 300, 20, 60, 150);
        var sortie = sortie(
                PartASpecs.units(
                        PartASpecs.unit(
                                0, Ship.START_X, PartASpecs.dummy(Layer.BURIED, 1000, new Hitbox(120, 60), false)),
                        PartASpecs.unit(
                                100 / 130.0,
                                Ship.START_X,
                                PartASpecs.dummy(Layer.GROUND, 1000, new Hitbox(120, 60), false))),
                TestSpecs.LOADOUT.withSpecial(new SpecialSpec("Airstrike", 1, 4, 0.1, spec)));
        while (sortie.levelSeconds() < 2.4) {
            sortie.step(Command.NONE);
        }
        sortie.step(SPECIAL);
        runUntouched(sortie, 1.9, Command.NONE, 1000);
        assertEquals(700, find(sortie, Layer.GROUND).hp(), "the ground unit takes its cap");
    }

    @Test
    void theEmpBurstStunsTheGroundUnitButNotTheBuriedOne() {
        var sortie = sortie(pair(1000, 60), TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.emp(1)));
        runUntouched(sortie, 2.5, Command.NONE, 1000);
        sortie.step(SPECIAL);
        boolean stunned = false;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
            stunned |= find(sortie, Layer.GROUND).empStunned();
            assertFalse(find(sortie, Layer.BURIED).empStunned());
        }
        assertTrue(stunned, "the ground unit beside it is stunned");
    }

    @Test
    void theOrbitalLanceBurnsTheGroundUnitButNotTheBuriedOne() {
        var sortie = sortie(pair(10000, 0), TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.lance(1)));
        runUntouched(sortie, 1.5, Command.NONE, 10000);
        sortie.step(SPECIAL);
        runUntouched(sortie, 2.5, Command.NONE, 10000);
        assertTrue(find(sortie, Layer.GROUND).hp() < 10000, "the ground unit burns");
    }

    @Test
    void rookNeverPicksIt() {
        var sortie = WingmanTest.sortie(
                PartASpecs.units(PartASpecs.unit(0, Ship.START_X - 64, unit(Layer.BURIED, 1000))),
                WingmanSpec.Side.LEFT);
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
            assertEquals(-1, sortie.wingman().orElseThrow().target());
        }
        assertEquals(1000, find(sortie, Layer.BURIED).hp());
    }

    @Test
    void aBurrowerIsBuriedButWhileItIsUp() {
        Sortie sortie =
                BurrowTest.sortie(5, BurrowTest.level(30, List.of(), BurrowTest.field(0, BurrowTest.burrower(), 1)));
        boolean wasUp = false;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            Enemy unit = sortie.enemy(0);
            Enemy.BurrowPhase phase = unit.burrowPhase();
            boolean up = phase == Enemy.BurrowPhase.SURFACED
                    || (phase == Enemy.BurrowPhase.DIVE && unit.burrowProgress(1) < 0.5);
            assertEquals(up ? Layer.GROUND : Layer.BURIED, unit.layer(), phase + " at step " + i);
            wasUp |= up;
        }
        assertTrue(wasUp);
    }
}
