package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M9 part A, step A1 (design/enemies/ground/rail-bunker, {@code laser-line}): any unit's line, every 4.5 s telegraph to
 * telegraph: a 1.0 s telegraph locks a line from the unit through the ship's position to the screen's edge (lock, not
 * track), then it is live 0.2 s, an 8 px band dealing its damage to the ship once; it holds fire while the ship is
 * within 100 px; the EMP Burst's stun cancels a running telegraph. Round 66's C.1 b: the origin rides with the unit
 * ({@link RailBunkerTest}). Also the {@code IN ROCK} limit word of Level 32.
 */
class LaserLineTest {
    private final SimEvents events = new SimEvents(64);
    private final Pool<Enemy> pool = new Pool<>(4, () -> new Enemy(events), Enemy[]::new);

    private Enemy bunker(double x, double down) {
        Enemy enemy = pool.obtain();
        enemy.root(Act5Specs.RAIL_BUNKER, 0, x, 0, -1);
        enemy.move(Ship.START_X, Ship.START_Y, down);
        return enemy;
    }

    private int steps(int count, double shipX, double shipY, SimEvents.Type type) {
        int counted = 0;
        for (int i = 0; i < count; i++) {
            events.clear();
            for (int j = 0; j < pool.size(); j++) {
                pool.get(j).move(shipX, shipY, 0);
            }
            LineBeam.stepAll(pool, shipX, shipY, events);
            counted += events.count(type);
        }
        return counted;
    }

    @Test
    void aTelegraphLocksTheLineThroughTheShipThenItIsLiveForAFifthOfASecond() {
        Enemy unit = bunker(100, 200);
        LineBeam line = unit.lineBeam();
        assertEquals(0, steps(SimStep.ticks(0.5) - 1, 300, 96, SimEvents.Type.LINE_TELEGRAPH), "its first delay");
        assertEquals(1, steps(1, 300, 96, SimEvents.Type.LINE_TELEGRAPH));
        assertTrue(line.telegraphing());
        double dx = 300 - unit.x();
        double dy = 96 - unit.y();
        double length = Math.hypot(dx, dy);
        assertEquals(dx / length, line.dirX(), 1e-9);
        assertEquals(dy / length, line.dirY(), 1e-9);
        // The ship moves: the line stays locked.
        steps(SimStep.ticks(0.5), 200, 96, SimEvents.Type.LINE_FIRED);
        assertEquals(dx / length, line.dirX(), 1e-9, "lock, not track");
        assertEquals(1, steps(SimStep.ticks(0.5), 200, 96, SimEvents.Type.LINE_FIRED), "after the 1.0 s telegraph");
        assertTrue(line.live());
        Hull hull = TestSpecs.HULL;
        EnemySpec.LaserLine rail = Act5Specs.RAIL;
        assertTrue(line.strikes(rail, hull, 300, 96), "on the locked line");
        assertFalse(line.strikes(rail, hull, 300 + 60, 96), "a sidestep out of the 8 px band");
        steps(SimStep.ticks(0.2), 200, 96, SimEvents.Type.LINE_FIRED);
        assertFalse(line.running(), "live 0.2 s");
        assertEquals(
                1,
                steps(SimStep.ticks(4.5) - SimStep.ticks(1.2), 300, 96, SimEvents.Type.LINE_TELEGRAPH),
                "every 4.5 s, telegraph to telegraph");
    }

    @Test
    void itHoldsFireWhileTheShipIsWithinAHundredPixels() {
        Enemy unit = bunker(240, 300);
        double near = unit.y() - 90;
        assertEquals(0, steps(SimStep.ticks(3), unit.x(), near, SimEvents.Type.LINE_TELEGRAPH));
        assertEquals(1, steps(1, unit.x(), unit.y() - 150, SimEvents.Type.LINE_TELEGRAPH), "out of 100 px: at once");
    }

    @Test
    void theEmpCancelsARunningTelegraph() {
        Enemy unit = bunker(100, 200);
        steps(SimStep.ticks(0.5), 300, 96, SimEvents.Type.LINE_TELEGRAPH);
        assertTrue(unit.lineBeam().telegraphing());
        unit.empStun(SimStep.ticks(3));
        assertFalse(unit.lineBeam().running(), "cancelled: no shot");
        assertEquals(0, steps(SimStep.ticks(1.5), 300, 96, SimEvents.Type.LINE_FIRED));
    }

    @Test
    void aLiveLineStrikesTheShipOnce() {
        // Straight above the ship: the line rides with the scrolling bunker along itself (round 66's C.1 b), so it
        // still finds a still ship (a diagonal line would slide off it).
        LevelScript level = TorpedoTest.units(false, new GroundUnit(0, Ship.START_X, Act5Specs.RAIL_BUNKER, -1));
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int hits = TorpedoTest.count(sortie, SimStep.ticks(4), Command.NONE, SimEvents.Type.LINE_HIT);
        assertEquals(1, hits, "the ship stood still on the line");
        assertTrue(sortie.ship().defences().shield() <= 20 - 10 + 1.5, "heavy 10 once (and some regrowth since)");
    }

    @Test
    void anUndergroundListReadsInRock() {
        SpecialSpec airstrike = Act4SpecialsTest.airstrike(2);
        SpecialLimits rock = new SpecialLimits(java.util.List.of(
                new SpecialLimits.Switch(0, Optional.of(Set.of(airstrike.kind())), SpecialLimits.Reason.IN_ROCK)));
        LevelScript level =
                TorpedoTest.units(false).toBuilder().specialLimits(rock).build();
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT.withSpecial(airstrike), level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        sortie.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.IN_ROCK, sortie.special().limit());
        assertEquals("IN ROCK", sortie.special().limit().word());

        SpecialLimits plain = new SpecialLimits(
                java.util.List.of(new SpecialLimits.Switch(0, Optional.of(Set.of(airstrike.kind())))));
        Sortie ice = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(airstrike),
                TorpedoTest.units(false).toBuilder().specialLimits(plain).build(),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        ice.step(Command.NONE);
        assertEquals(SpecialLimits.Reason.UNDER_ICE, ice.special().limit(), "an Act 4 list: the setting's word");
    }
}
