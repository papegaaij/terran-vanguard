package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M7 part A, the Evasive thrusters' dash (design/player/systems, Evasive thrusters (Act 3)): the Dash
 * button and the double-tap detected from the direction commands, the 72 px dash over 0.12 s in 8
 * directions with the own speed kept for afterwards, the 0.25 s invulnerability in which bullets pass
 * through, the 3 s cooldown, and the hash rule.
 */
class DashTest {
    /** The numbers of design/player/systems/data.yaml's {@code dash} block. */
    static final ThrusterSpec DASH = new ThrusterSpec(72, 0.12, 0.25, 3, 0.25);

    private static final int UP = Command.UP.bit();
    private static final int DOWN = Command.DOWN.bit();
    private static final int LEFT = Command.LEFT.bit();
    private static final int RIGHT = Command.RIGHT.bit();
    private static final int BUTTON = Command.DASH.bit();

    private final Ship ship = new Ship(
            TestSpecs.SHIP,
            new Defences(TestSpecs.LOADOUT.shield(), TestSpecs.LOADOUT.plating(), TestSpecs.SHIP.mercySeconds()),
            DASH);

    private void fly(int commands, int steps) {
        for (int i = 0; i < steps; i++) {
            ship.fly(commands);
        }
    }

    @Test
    void theButtonDashesStraightAheadWithNothingHeld72PxIn7Steps() {
        double y = ship.y();
        ship.fly(BUTTON);
        assertTrue(ship.dashStarted());
        assertTrue(ship.dashing());
        assertEquals(0, ship.dashEighth());
        fly(Command.NONE, 6);
        assertFalse(ship.dashing());
        assertEquals(y + 72, ship.y(), 1e-9);
        assertEquals(Ship.START_X, ship.x(), 1e-9);
    }

    @Test
    void theButtonDashesInTheHeldDirectionAndADiagonalMoves72PxAlongIt() {
        double x = ship.x();
        double y = ship.y();
        ship.fly(BUTTON | RIGHT | UP);
        assertEquals(1, ship.dashEighth());
        fly(RIGHT | UP, 6);
        double moved = Math.hypot(ship.x() - x, ship.y() - y);
        // The dash's 72 px; the own speed is set aside, so nothing else moved it.
        assertEquals(72, moved, 1e-9);
        assertEquals(ship.x() - x, ship.y() - y, 1e-9);
    }

    @Test
    void theOwnSpeedIsSetAsideAndKeptForAfterwards() {
        fly(LEFT, 10);
        assertEquals(-270, ship.vx(), 1e-9);
        double x = ship.x();
        ship.fly(BUTTON | UP);
        fly(LEFT | UP, 6);
        assertEquals(x, ship.x(), 1e-9, "straight up: the own leftward speed set aside");
        assertEquals(-270, ship.vx(), 1e-9, "and kept");
        ship.fly(LEFT);
        assertEquals(x - 270 * SimStep.SECONDS, ship.x(), 1e-9);
    }

    @Test
    void aDoubleTapOfADirectionDashesThatWay() {
        double x = ship.x();
        fly(RIGHT, 3);
        double before = ship.x();
        fly(Command.NONE, 10);
        double coasted = ship.x();
        ship.fly(RIGHT);
        assertTrue(ship.dashStarted(), "the second press within 0.25 s of the release");
        assertEquals(2, ship.dashEighth());
        fly(RIGHT, 6);
        assertEquals(coasted + 72, ship.x(), 1e-9);
        assertTrue(before > x);
    }

    @Test
    void aSlowSecondPressADifferentDirectionOrAnotherPressBetweenIsNoDoubleTap() {
        fly(RIGHT, 3);
        fly(Command.NONE, 16);
        ship.fly(RIGHT);
        assertFalse(ship.dashStarted(), "more than 0.25 s after the release");

        Ship other = new Ship(TestSpecs.SHIP, new Defences(TestSpecs.SHIELD, TestSpecs.PLATING, 0.25), DASH);
        other.fly(RIGHT);
        other.fly(Command.NONE);
        other.fly(LEFT);
        assertFalse(other.dashStarted(), "another direction");
        other.fly(Command.NONE);
        other.fly(RIGHT);
        assertFalse(other.dashStarted(), "the left press came between");

        Ship diagonal = new Ship(TestSpecs.SHIP, new Defences(TestSpecs.SHIELD, TestSpecs.PLATING, 0.25), DASH);
        diagonal.fly(RIGHT | UP);
        diagonal.fly(Command.NONE);
        diagonal.fly(RIGHT);
        assertFalse(diagonal.dashStarted(), "a direction released with another is no tap");
        diagonal.fly(Command.NONE);
        diagonal.fly(RIGHT | DOWN);
        assertFalse(diagonal.dashStarted(), "the second press with another direction");
    }

    @Test
    void theCooldownRunsThreeSecondsFromTheStartAndADashWhileItCoolsDoesNothing() {
        ship.fly(BUTTON);
        assertEquals(0, ship.dashReadiness(), 1e-9);
        fly(Command.NONE, 100);
        ship.fly(BUTTON);
        assertFalse(ship.dashStarted());
        assertFalse(ship.dashReady());
        fly(Command.NONE, 78);
        ship.fly(BUTTON);
        assertTrue(ship.dashStarted(), "180 steps after the first");
    }

    @Test
    void theShipCannotBeHurtFor15StepsFromTheStart() {
        ship.fly(BUTTON);
        for (int i = 1; i < 15; i++) {
            assertTrue(ship.evading(), "step " + i);
            ship.fly(Command.NONE);
        }
        assertTrue(ship.evading());
        ship.fly(Command.NONE);
        assertFalse(ship.evading());
    }

    @Test
    void theEdgesClampADash() {
        fly(LEFT, 120);
        double x = ship.x();
        ship.fly(BUTTON | LEFT);
        fly(LEFT, 6);
        assertEquals(x, ship.x(), 1e-9);
    }

    @Test
    void withoutTheThrustersNothingDashes() {
        Ship plain = new Ship(TestSpecs.SHIP, new Defences(TestSpecs.SHIELD, TestSpecs.PLATING, 0.25));
        plain.fly(BUTTON);
        assertFalse(plain.dashing());
        plain.fly(RIGHT);
        plain.fly(Command.NONE);
        plain.fly(RIGHT);
        assertFalse(plain.dashing());
        assertFalse(plain.thrustersFitted());
        assertEquals(1, plain.dashReadiness());
    }

    /** A Needler right above the start, firing a thorn every 0.1 s straight at the ship. */
    private static Sortie needlerSortie(Loadout loadout) {
        var needler = TestSpecs.needler(EnemyGun.aimed(0.1, 0, 1, 300, 1, false));
        return new Sortie(
                1,
                loadout,
                TestSpecs.level(
                        20,
                        List.of(TestSpecs.wave(
                                0,
                                WaveSpec.Formation.LINE_ABREAST,
                                needler,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE))),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void inTheInvulnerabilityBulletsPassThroughTheShipWithoutBeingAbsorbed() {
        Sortie sortie = needlerSortie(TestSpecs.LOADOUT.withThrusters(DASH));
        // Until a thorn is about to reach the hull.
        int steps = 0;
        while (!closeBullet(sortie) && steps++ < 600) {
            sortie.step(Command.NONE);
        }
        assertTrue(steps < 600, "a thorn comes");
        double shield = sortie.ship().defences().shield();
        sortie.step(BUTTON);
        assertEquals(1, sortie.events().count(SimEvents.Type.DASH));
        int passed = 0;
        for (int i = 1; i < 15; i++) {
            int before = sortie.bulletCount();
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.SHIELD_HIT), "step " + i);
            passed += overlapping(sortie);
            assertTrue(sortie.bulletCount() >= before - 1, "no thorn absorbed");
        }
        assertEquals(shield, sortie.ship().defences().shield(), 1e-9);
        assertTrue(passed > 0, "thorns overlapped the hull and flew on");
    }

    private static boolean closeBullet(Sortie sortie) {
        Ship ship = sortie.ship();
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            if (Math.abs(bullet.x() - ship.x()) < 4 && bullet.y() - ship.y() < 40 && bullet.y() > ship.y()) {
                return true;
            }
        }
        return false;
    }

    private static int overlapping(Sortie sortie) {
        Ship ship = sortie.ship();
        int count = 0;
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            if (ship.spec().hull().overlaps(ship.x(), ship.y(), EnemyGun.BULLET, bullet.x(), bullet.y())) {
                count++;
            }
        }
        return count;
    }

    @Test
    void aRunWithoutTheThrustersHashesAsBeforeAndTheDashReplays() {
        int[] commands = new int[600];
        for (int i = 0; i < commands.length; i++) {
            commands[i] = i % 40 < 3 ? RIGHT : i % 40 == 6 ? RIGHT : i % 97 == 50 ? BUTTON | LEFT : Command.NONE;
        }
        Sortie plain = needlerSortie(TestSpecs.LOADOUT);
        Sortie fitted = needlerSortie(TestSpecs.LOADOUT.withThrusters(DASH));
        Sortie again = needlerSortie(TestSpecs.LOADOUT.withThrusters(DASH));
        Sortie unfitted = needlerSortie(
                new Loadout(TestSpecs.SHIP, TestSpecs.LOADOUT.armament(), TestSpecs.SHIELD, TestSpecs.PLATING));
        int dashes = 0;
        for (int command : commands) {
            plain.step(command);
            fitted.step(command);
            again.step(command);
            unfitted.step(command);
            dashes += fitted.events().count(SimEvents.Type.DASH);
            assertEquals(plain.stateHash(), unfitted.stateHash());
            assertEquals(fitted.stateHash(), again.stateHash());
        }
        assertTrue(dashes > 1);
        assertNotEquals(plain.stateHash(), fitted.stateHash());
    }

    @Test
    void dashingDoesNotAllocate() {
        long allocated = Allocations.least(() -> needlerSortie(TestSpecs.LOADOUT.withThrusters(DASH)), sortie -> {
            for (int i = 0; i < 900; i++) {
                sortie.step(i % 30 < 2 ? RIGHT : i % 30 == 4 ? RIGHT : i % 200 == 100 ? BUTTON : Command.NONE);
            }
        });
        assertEquals(0, allocated);
    }
}
