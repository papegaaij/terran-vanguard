package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part E, step E1: the Dust Devil's pull (design/enemies/ground/dust-devil, Pull) on hand-built specs: within
 * 130 px it draws the ship toward its centre, 35 px/s at the centre fading to 0 at the edge, added to the ship's
 * step (never a trap); loose pickups within 170 px fly at it at 90 px/s (a fitted Pickup magnet wins where it is
 * the stronger) and those it reaches are held, circling it, out of the ship's reach; they drop at its death
 * ({@code PICKUPS_RELEASED}) and go with it when it escapes.
 */
class PullTest {
    static final EnemySpec.Pull PULL = new EnemySpec.Pull(130, 35, 170, 90);

    /** A devil without a wander, its centre line at {@code x}. */
    private static WaveSpec still(double x) {
        return VortexTest.wave(0, VortexTest.devil(VortexTest.STILL, Optional.of(VortexTest.spiralGun(2))), 1)
                .withAt(new WaveSpec.At(x, 0));
    }

    @Test
    void aPullsNumbersHold() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Pull(-1, 35, 170, 90));
        assertEquals(35, PULL.shipSpeedAt(0), 1e-12);
        assertEquals(17.5, PULL.shipSpeedAt(65), 1e-12);
        assertEquals(0, PULL.shipSpeedAt(130), 1e-12);
        assertEquals(0, PULL.shipSpeedAt(200), 1e-12);
    }

    @Test
    void withinItsRadiusItDrawsTheShipTowardItsCentreFadingToTheEdge() {
        Sortie sortie = VortexTest.sortie(still(Ship.START_X + 100));
        Enemy unit = VortexTest.firstIn(sortie);
        Ship ship = sortie.ship();
        double most = 0;
        for (int i = 0; i < 6 * SimStep.PER_SECOND && sortie.enemyCount() > 0; i++) {
            double ex = unit.x();
            double ey = unit.y();
            double sx = ship.x();
            double sy = ship.y();
            boolean on = PlayField.overlaps(ex, ey, unit.hitbox());
            sortie.step(Command.NONE);
            double dx = ex - sx;
            double dy = ey - sy;
            double d = Math.hypot(dx, dy);
            double speed = on ? PULL.shipSpeedAt(d) : 0;
            assertEquals(dx / d * speed, sortie.pullX(), 1e-9, "step " + i);
            assertEquals(dy / d * speed, sortie.pullY(), 1e-9);
            assertEquals(sx + sortie.pullX() * SimStep.SECONDS, ship.x(), 1e-9, "added to the ship's step");
            assertEquals(sy + sortie.pullY() * SimStep.SECONDS, ship.y(), 1e-9);
            most = Math.max(most, Math.hypot(sortie.pullX(), sortie.pullY()));
        }
        assertTrue(most > 7 && most < 35 * (1 - 90 / 130.0) + 1e-9, "it passed about 100 px off: " + most);
        assertTrue(ship.x() > Ship.START_X, "drawn toward its path: " + ship.x());
    }

    @Test
    void withoutAUnitWithAPullNothingPullsTheShip() {
        Sortie sortie = VortexTest.sortie(TestSpecs.wave(
                0, WaveSpec.Formation.SINGLE, TestSpecs.NEEDLER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE));
        for (int i = 0; i < 3 * SimStep.PER_SECOND; i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.pullX());
            assertEquals(0, sortie.pullY());
        }
        assertEquals(Ship.START_X, sortie.ship().x());
    }

    /** Steps until the devil holds a pickup dropped 120 px below it; returns the pickup. */
    private static Pickup heldPickup(Sortie sortie, Enemy unit) {
        while (unit.y() > PlayField.HEIGHT - 60) {
            sortie.step(Command.NONE);
        }
        sortie.drop(PickupType.SMALL_SALVAGE, unit.x(), unit.y() - 120);
        Pickup pickup = sortie.pickup(0);
        double before = Math.hypot(pickup.x() - unit.x(), pickup.y() - unit.y());
        sortie.step(Command.NONE);
        double after = Math.hypot(pickup.x() - unit.x(), pickup.y() - unit.y());
        // It flies at the devil at 90 px/s while the devil comes down with the ground.
        assertEquals(before - (90 + 40) * SimStep.SECONDS, after, 1e-6, "90 px/s at it, with the 40 px/s scroll");
        for (int i = 0; i < SimStep.PER_SECOND && pickup.heldBy() < 0; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(unit.serial(), pickup.heldBy(), "held once it reached it");
        return pickup;
    }

    @Test
    void loosePickupsWithinItsReachFlyAtItAndAreHeldCirclingIt() {
        Sortie sortie = VortexTest.slow(still(Ship.START_X + 150));
        Enemy unit = VortexTest.firstIn(sortie);
        Pickup pickup = heldPickup(sortie, unit);
        int life = pickup.ticksLeft();
        for (int i = 0; i < 2 * SimStep.PER_SECOND; i++) {
            sortie.step(Command.NONE);
            assertEquals(Pickup.HOLD_RADIUS, Math.hypot(pickup.x() - unit.x(), pickup.y() - unit.y()), 1e-3);
            assertEquals(life, pickup.ticksLeft(), "its time stands still while held");
        }
        assertEquals(1, sortie.pickupCount());
        assertEquals(0, sortie.credits(), "out of the ship's reach");
    }

    @Test
    void itsDeathReleasesThePickupsItHeldWithAWholeLife() {
        Sortie sortie = VortexTest.slow(still(Ship.START_X + 150));
        Enemy unit = VortexTest.firstIn(sortie);
        Pickup pickup = heldPickup(sortie, unit);
        double x = pickup.x();
        double y = pickup.y();
        sortie.objectivePasses.destroy(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.PICKUPS_RELEASED));
        assertEquals(-1, pickup.heldBy());
        assertEquals(SimStep.ticks(TestSpecs.RULES.pickups().seconds()), pickup.ticksLeft());
        sortie.step(Command.NONE);
        assertEquals(x, pickup.x(), 1e-12, "it drifts on from where it circled");
        assertEquals(y - 40 * SimStep.SECONDS, pickup.y(), 1e-9);
    }

    @Test
    void aDevilThatEscapesCarriesOffThePickupsItHeld() {
        Sortie sortie = VortexTest.sortie(still(Ship.START_X + 150));
        Enemy unit = VortexTest.firstIn(sortie);
        while (unit.y() > PlayField.HEIGHT - 60) {
            sortie.step(Command.NONE);
        }
        sortie.drop(PickupType.SHIELD_CELL, unit.x(), unit.y() - 100);
        Pickup pickup = sortie.pickup(0);
        for (int i = 0; i < SimStep.PER_SECOND && pickup.heldBy() < 0; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(unit.serial(), pickup.heldBy());
        int released = 0;
        for (int i = 0; i < 6 * SimStep.PER_SECOND && sortie.enemyCount() > 0; i++) {
            sortie.step(Command.NONE);
            released += sortie.events().count(SimEvents.Type.PICKUPS_RELEASED);
        }
        assertEquals(0, sortie.enemyCount(), "it left at the bottom");
        assertEquals(0, sortie.pickupCount(), "with the shield cell");
        assertEquals(0, released);
    }

    @Test
    void aFittedMagnetWinsWhereItIsTheStrongerElseThePull() {
        for (Magnet magnet : new Magnet[] {new Magnet(72, 240), new Magnet(72, 30)}) {
            Sortie sortie = VortexTest.sortie(40, TestSpecs.LOADOUT.withMagnet(magnet), still(Ship.START_X + 160));
            Enemy unit = VortexTest.firstIn(sortie);
            Ship ship = sortie.ship();
            while (unit.y() - ship.y() > 100) {
                sortie.step(Command.NONE);
            }
            double px = ship.x() + 60;
            double py = ship.y();
            sortie.drop(PickupType.SMALL_SALVAGE, px, py);
            Pickup pickup = sortie.pickup(0);
            assertTrue(Math.hypot(unit.x() - px, unit.y() - py) < 170, "within both reaches");
            // Without a wander it comes down with the ground (40 px/s); the pickup flies at where it is then.
            double ux = unit.x();
            double uy = unit.y() - 40 * SimStep.SECONDS;
            sortie.step(Command.NONE);
            if (magnet.pullSpeed() > 90) {
                assertEquals(px - 240 * SimStep.SECONDS, pickup.x(), 1e-9, "the magnet is the stronger");
                assertEquals(py, pickup.y(), 1e-9);
            } else {
                double d = Math.hypot(ux - px, uy - py);
                assertEquals(px + (ux - px) / d * 90 * SimStep.SECONDS, pickup.x(), 1e-9, "the pull is the stronger");
                assertEquals(py + (uy - py) / d * 90 * SimStep.SECONDS, pickup.y(), 1e-9);
            }
        }
    }
}
