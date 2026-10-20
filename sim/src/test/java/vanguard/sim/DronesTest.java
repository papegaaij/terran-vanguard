package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.PartASpecs.droneBay;
import static vanguard.sim.PartASpecs.dummy;
import static vanguard.sim.PartASpecs.sortie;
import static vanguard.sim.PartASpecs.unit;
import static vanguard.sim.PartASpecs.units;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The light drones of the Light Drone Bay (design/player/wingmen, Light drone; M7 part A): the
 * clockwise orbit from 9 and 3 o'clock, fire at the convergence point only while the player fires and
 * the drone is whole, bullets and contacts absorbed, the break, rebuild and fade-in, whole drones on a
 * retry. M7 part G: the rear-guard drone's trail station (behind, to its side, its lag, firing straight back,
 * blocking, breaking and the rebuild).
 */
class DronesTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private static Sortie pair(LevelScript level, int level1) {
        return sortie(level, droneBay(Armament.Slot.LEFT_WING, level1), droneBay(Armament.Slot.RIGHT_WING, level1));
    }

    private static void run(Sortie sortie, int steps, int commands) {
        for (int i = 0; i < steps; i++) {
            sortie.step(commands);
        }
    }

    private static int count(Sortie sortie, int steps, int commands, SimEvents.Type type, int mount) {
        int counted = 0;
        for (int i = 0; i < steps; i++) {
            sortie.step(commands);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == type && sortie.events().value(e) == mount) {
                    counted++;
                }
            }
        }
        return counted;
    }

    @Test
    void aPairOrbitsClockwiseOppositeEachOtherAndFollowsTheShip() {
        var sortie = pair(TestSpecs.level(10, List.of()), 1);
        Drones drones = sortie.drones();
        assertTrue(drones.any());
        assertEquals(Ship.START_X - 36, drones.x(LEFT), 1e-9, "the left drone starts at 9 o'clock");
        assertEquals(Ship.START_Y, drones.y(LEFT), 1e-9);
        assertEquals(Ship.START_X + 36, drones.x(RIGHT), 1e-9, "the right one at 3 o'clock");

        // A quarter turn (0.6 s) clockwise: the left drone above the ship, the right one below.
        run(sortie, SimStep.ticks(0.6), Command.RIGHT.bit());
        Ship ship = sortie.ship();
        assertTrue(ship.x() > Ship.START_X + 50, "the ship moved");
        assertEquals(0, drones.offsetX(LEFT), 0.02);
        assertEquals(36, drones.offsetY(LEFT), 0.02);
        assertEquals(ship.x(), drones.x(LEFT), 0.02, "the orbit centre follows the ship at once");
        assertEquals(-36, drones.offsetY(RIGHT), 0.02);
    }

    @Test
    void itFiresOnlyWithThePlayerAtItsRateAimedAtTheConvergencePoint() {
        var sortie = sortie(TestSpecs.level(10, List.of()), droneBay(Armament.Slot.LEFT_WING, 1));
        assertEquals(0, count(sortie, SimStep.PER_SECOND, Command.NONE, SimEvents.Type.SHOT_FIRED, 0));
        assertEquals(6, count(sortie, SimStep.PER_SECOND, FIRE, SimEvents.Type.SHOT_FIRED, 0), "6 a second");

        var aimed = sortie(TestSpecs.level(10, List.of()), droneBay(Armament.Slot.LEFT_WING, 1));
        aimed.step(FIRE);
        Shot shot = aimed.shot(0);
        double toX = aimed.ship().x() - shot.x();
        double toY = aimed.ship().y() + 140 - shot.y();
        assertEquals(0, toX * shot.vy() - toY * shot.vx(), 1e-6, "on the line to the convergence point");
        assertTrue(toX * shot.vx() + toY * shot.vy() > 0);
        assertEquals(900, Math.hypot(shot.vx(), shot.vy()), 1e-3);
    }

    @Test
    void aBrokenDroneStopsFiringRebuildsAndFadesInFromItsBay() {
        var sortie = sortie(TestSpecs.level(30, List.of()), droneBay(Armament.Slot.LEFT_WING, 1));
        sortie.step(Command.NONE);
        Drones drones = sortie.drones();
        assertEquals(8, drones.hitsLeft(0));
        assertFalse(drones.hit(0, 7, sortie.events()));
        assertEquals(1, drones.hitsLeft(0));
        assertTrue(drones.hit(0, 1, sortie.events()), "the eighth hit breaks it");
        assertTrue(drones.broken(0));
        assertEquals(0, drones.hitsLeft(0));

        // 6 s to rebuild at L1: no shots meanwhile, then the fade-in starts at the bay.
        assertEquals(0, count(sortie, SimStep.ticks(6) - 1, FIRE, SimEvents.Type.SHOT_FIRED, 0));
        assertTrue(drones.broken(0));
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.DRONE_REBUILT));
        assertTrue(drones.fadingIn(0));
        assertEquals(-16, drones.offsetX(0), 1e-9, "it appears at its bay");
        assertEquals(-3, drones.offsetY(0), 1e-9);
        assertEquals(0, count(sortie, SimStep.ticks(0.3) - 1, FIRE, SimEvents.Type.SHOT_FIRED, 0));
        sortie.step(FIRE);
        assertTrue(drones.whole(0));
        assertEquals(1, sortie.events().count(SimEvents.Type.SHOT_FIRED));
        assertEquals(8, drones.hitsLeft(0));
    }

    @Test
    void enemyBulletsThatTouchItAreAbsorbedOneHitEach() {
        var sortie = pair(SortieTest.mixedLevel(), 5);
        int absorbed = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(Command.NONE);
            absorbed += sortie.events().count(SimEvents.Type.DRONE_HIT);
        }
        assertTrue(absorbed > 0, "the drones absorbed bullets");
    }

    @Test
    void anAirContactCostsFourHitsAndTakesTenDamageOnce() {
        // A tall pillar beside the ship, in the left drone's path round 9 o'clock but clear of the hull.
        EnemySpec pillar = dummy(Layer.AIR, 1000, new Hitbox(10, 400), false);
        var sortie = sortie(units(unit(0, Ship.START_X - 36, pillar)), droneBay(Armament.Slot.LEFT_WING, 1));
        Drones drones = sortie.drones();
        for (int i = 0; i < SimStep.ticks(8) && drones.hitsLeft(0) == 8; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(4, drones.hitsLeft(0));
        assertEquals(990, sortie.enemy(0).hp(), 1e-9);
        sortie.step(Command.NONE);
        assertEquals(4, drones.hitsLeft(0), "a lasting contact costs once");
    }

    @Test
    void aSmallRammerCostsTwoHitsAndIsDestroyed() {
        EnemySpec rammer = dummy(Layer.AIR, 1000, new Hitbox(10, 400), true);
        var sortie = sortie(units(unit(0, Ship.START_X - 36, rammer)), droneBay(Armament.Slot.LEFT_WING, 1));
        Drones drones = sortie.drones();
        int destroyed = 0;
        for (int i = 0; i < SimStep.ticks(8) && drones.hitsLeft(0) == 8; i++) {
            sortie.step(Command.NONE);
            destroyed += sortie.events().count(SimEvents.Type.ENEMY_DESTROYED);
        }
        assertEquals(6, drones.hitsLeft(0));
        assertEquals(1, destroyed);
    }

    @Test
    void aRetryStartsWithWholeDrones() {
        var sortie = sortie(TestSpecs.level(30, List.of()), droneBay(Armament.Slot.RIGHT_WING, 2));
        sortie.step(Command.NONE);
        sortie.drones().hit(0, 9, sortie.events());
        assertTrue(sortie.drones().broken(0));
        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(Command.NONE);
        assertTrue(sortie.drones().whole(0));
        assertEquals(9, sortie.drones().hitsLeft(0));
    }

    @Test
    void anOverdriveFiresAtTheOverdriveRate() {
        var sortie = sortie(TestSpecs.level(10, List.of()), droneBay(Armament.Slot.LEFT_WING, 5));
        sortie.overdrive(5);
        // 7 a second: an interval of round(60 / 7) = 9 steps, so 7 shots in the first 55 steps.
        assertEquals(7, count(sortie, 55, FIRE, SimEvents.Type.SHOT_FIRED, 0));
    }

    @Test
    void aDroneBayAndADeflectorSitOnAWingOnly() {
        assertThrows(() -> droneBay(Armament.Slot.FRONT, 1));
        assertThrows(() -> PartASpecs.deflectorPod(Armament.Slot.REAR, 1));
        assertThrows(() ->
                new Armament.Mount(Armament.Slot.LEFT_WING, null, null, null, null, PartASpecs.deflector(1), null));
        Armament.Mount pod = PartASpecs.deflectorPod(Armament.Slot.LEFT_WING, 1);
        assertFalse(pod.fires());
        assertTrue(pod.deflects());
        assertTrue(droneBay(Armament.Slot.RIGHT_WING, 1).bay());
    }

    // M7 part G (design/player/wingmen, Rear-guard drone): the trail station.

    /** The rear-guard drone at L1: 40 px behind, {@code side} px out, a 0.15 s lag, firing back; 8 hits, 6 s rebuild. */
    private static DroneSpec rearGuard(double side) {
        return DroneSpec.trail(40, side, 0.15, true, 8, 6, 0.3, 4, 2, 2, 10, DroneSpec.BOX);
    }

    private static Armament.Mount rearGuardBay(Armament.Slot slot, double side) {
        WeaponSpec gun = PartASpecs.droneGun(slot, 1);
        return Armament.Mount.droneBay(slot, gun, gun, rearGuard(side), rearGuard(side));
    }

    private static Sortie rearGuardPair(LevelScript level) {
        return sortie(level, rearGuardBay(Armament.Slot.LEFT_WING, 12), rearGuardBay(Armament.Slot.RIGHT_WING, 12));
    }

    @Test
    void aRearGuardPairTrailsBehindTheShipSideBySide() {
        var sortie = rearGuardPair(TestSpecs.level(10, List.of()));
        Drones drones = sortie.drones();
        assertEquals(DroneSpec.Station.TRAIL, drones.station(LEFT));
        assertEquals(Ship.START_X - 12, drones.x(LEFT), 1e-9, "12 px to the left bay's side");
        assertEquals(Ship.START_Y - 40, drones.y(LEFT), 1e-9, "40 px behind the ship's centre");
        assertEquals(Ship.START_X + 12, drones.x(RIGHT), 1e-9, "24 px apart");
        run(sortie, SimStep.ticks(1), Command.NONE);
        assertEquals(-12, drones.offsetX(LEFT), 1e-9);
        assertEquals(-40, drones.offsetY(LEFT), 1e-9);
        assertEquals(12, drones.offsetX(RIGHT), 1e-9);
    }

    @Test
    void itFollowsWithItsLagSoItSwingsOutInATurn() {
        var sortie = rearGuardPair(TestSpecs.level(10, List.of()));
        Drones drones = sortie.drones();
        Ship ship = sortie.ship();
        double before = ship.x();
        run(sortie, SimStep.ticks(0.6) - 1, Command.RIGHT.bit());
        double last = ship.x();
        sortie.step(Command.RIGHT.bit());
        double speed = (ship.x() - last) * SimStep.PER_SECOND;
        assertTrue(ship.x() > before + 50, "the ship moved");
        // A first-order lag of 0.15 s behind a steady move: about the move's speed × the lag, less one step.
        double behindBy = 12 - drones.offsetX(RIGHT);
        assertTrue(behindBy > 0.1 * speed && behindBy < 0.16 * speed, "it swings out: " + behindBy + " at " + speed);
        run(sortie, SimStep.ticks(1.5), Command.NONE);
        assertEquals(12, drones.offsetX(RIGHT), 0.01, "and settles on its station");
        assertEquals(-40, drones.offsetY(RIGHT), 0.01);
    }

    @Test
    void itFiresStraightBackOnlyWithThePlayerAtItsRate() {
        var sortie = sortie(TestSpecs.level(10, List.of()), rearGuardBay(Armament.Slot.LEFT_WING, 12));
        assertEquals(0, count(sortie, SimStep.PER_SECOND, Command.NONE, SimEvents.Type.SHOT_FIRED, 0));
        assertEquals(6, count(sortie, SimStep.PER_SECOND, FIRE, SimEvents.Type.SHOT_FIRED, 0), "6 a second");

        var back = sortie(TestSpecs.level(10, List.of()), rearGuardBay(Armament.Slot.LEFT_WING, 12));
        back.step(FIRE);
        Shot shot = back.shot(0);
        assertEquals(0, shot.vx(), 1e-9, "straight back");
        assertEquals(-900, shot.vy(), 1e-6, "down the screen");
        assertEquals(back.drones().y(0), shot.y(), 20, "from the drone behind the ship");
    }

    @Test
    void behindStaysTheBottomOfTheScreenInAReversedScroll() {
        var sortie = rearGuardPair(PartCSpecs.level(10, -120, List.of(), List.of()));
        run(sortie, SimStep.ticks(2), Command.NONE);
        assertEquals(sortie.ship().y() - 40, sortie.drones().y(LEFT), 0.01);
    }

    @Test
    void aTrailingDroneTakesContactsAndBreaksAndIsRebuiltAtItsStation() {
        // A tall pillar coming down 40 px left of the ship: the drone 40 px out takes the contact, the hull does not.
        EnemySpec pillar = dummy(Layer.AIR, 1000, new Hitbox(10, 400), false);
        var sortie = sortie(units(unit(0, Ship.START_X - 40, pillar)), rearGuardBay(Armament.Slot.LEFT_WING, 40));
        Drones drones = sortie.drones();
        for (int i = 0; i < SimStep.ticks(8) && drones.hitsLeft(0) == 8; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(4, drones.hitsLeft(0), "an air contact costs 4 hits");
        assertEquals(990, sortie.enemy(0).hp(), 1e-9, "and takes 10 damage");

        assertTrue(drones.hit(0, 4, sortie.events()), "the eighth hit breaks it");
        assertTrue(drones.broken(0));
        assertEquals(0, count(sortie, SimStep.ticks(6) - 1, FIRE, SimEvents.Type.SHOT_FIRED, 0), "no shots");
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.DRONE_REBUILT));
        assertEquals(-16, drones.offsetX(0), 1e-9, "it appears at its bay");
        run(sortie, SimStep.ticks(0.3), Command.NONE);
        assertTrue(drones.whole(0));
        assertEquals(-40, drones.offsetX(0), 1e-6, "back on its station");
        assertEquals(-40, drones.offsetY(0), 1e-6);
    }

    @Test
    void aTrailingDroneBlocksBulletsFromBehind() {
        // Reversed (slowly): a turret rises from the bottom under the ship and fires up at it; the drone right behind
        // the ship (no side offset) is in the way.
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(30, -30)))
                .groundUnits(List.of(new GroundUnit(1, Ship.START_X, GroundAndDiveTest.TURRET, -1)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
        var sortie = sortie(level, rearGuardBay(Armament.Slot.LEFT_WING, 0));
        int absorbed = 0;
        int fired = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            absorbed += sortie.events().count(SimEvents.Type.DRONE_HIT);
            fired += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertTrue(fired > 0, "the turret fired up at the ship");
        assertTrue(absorbed > 0, "the drone absorbed bullets from behind");
    }

    @Test
    void aRetryStartsWithWholeDronesOnTheirStations() {
        var sortie = rearGuardPair(TestSpecs.level(30, List.of()));
        run(sortie, SimStep.ticks(1), Command.RIGHT.bit());
        sortie.drones().hit(0, 9, sortie.events());
        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(Command.NONE);
        assertTrue(sortie.drones().whole(0));
        assertEquals(-12, sortie.drones().offsetX(LEFT), 1e-9);
        assertEquals(-40, sortie.drones().offsetY(LEFT), 1e-9);
    }

    @Test
    void anOrbitingDroneHasNoTrailAndATrailingOneNoOrbit() {
        assertThrows(() -> new DroneSpec(
                36, 2.4, 140, 8, 6, 0.3, 4, 2, 2, 10, DroneSpec.BOX, DroneSpec.Station.ORBIT, 40, 12, 0.15, true));
        assertThrows(() -> DroneSpec.trail(-1, 12, 0.15, true, 8, 6, 0.3, 4, 2, 2, 10, DroneSpec.BOX));
        assertEquals(DroneSpec.Station.ORBIT, PartASpecs.drone(1).station());
    }

    @Test
    void aTrailingPairDoesNotAllocate() {
        long allocated = Allocations.least(() -> rearGuardPair(TestSpecs.level(20, List.of())), sortie -> {
            for (int step = 0; step < 600; step++) {
                sortie.step(step % 120 < 60 ? FIRE | Command.LEFT.bit() : FIRE | Command.RIGHT.bit());
            }
        });
        assertEquals(0, allocated);
    }

    private static void assertThrows(Runnable action) {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, action::run);
    }
}
