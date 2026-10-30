package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part F, step F1 (design/enemies/ground/rail-bunker, Level 33): never more than 2 lines telegraphed at once: a
 * due line waits while 2 others telegraph (in the pool's order, deterministic) and starts as soon as one goes live;
 * a line without the gate is unchanged (A1's {@link LaserLineTest}: lock, band, once, the hold within 100 px, the
 * EMP's cancel). The line runs any way, up the screen too. Round 66's C.1 b: the line's direction is locked at the
 * telegraph's start, its origin rides with the bunker as the ground scrolls (telegraph, live band and hit test). Round
 * 69's B.2: the locked direction leads the scroll, so the live line passes through the ship's locked point.
 */
class RailBunkerTest {
    private final SimEvents events = new SimEvents(64);
    private final Pool<Enemy> pool = new Pool<>(4, () -> new Enemy(events), Enemy[]::new);

    private static final EnemySpec GATED = Act5Specs.RAIL_BUNKER.withLaserLine(Optional.of(Act5FSpecs.RAIL));

    private Enemy bunker(EnemySpec spec, double x, double down) {
        Enemy enemy = pool.obtain();
        enemy.root(spec, 0, x, 0, -1);
        enemy.move(Ship.START_X, Ship.START_Y, down);
        return enemy;
    }

    /** Level 33's ground scroll, px a step (≈130 px over the 1.0 s telegraph). */
    private static final double SCROLL = 130 * SimStep.SECONDS;

    /** Steps every unit and its line; returns the most lines telegraphing at once after a step. */
    private int steps(int count, double shipX, double shipY) {
        return steps(count, shipX, shipY, 0);
    }

    /** As {@link #steps(int, double, double)}, the ground scrolling {@code scroll} px a step. */
    private int steps(int count, double shipX, double shipY, double scroll) {
        int most = 0;
        for (int i = 0; i < count; i++) {
            events.clear();
            for (int j = 0; j < pool.size(); j++) {
                pool.get(j).move(shipX, shipY, scroll);
            }
            LineBeam.stepAll(pool, shipX, shipY, events);
            int telegraphing = 0;
            for (int j = 0; j < pool.size(); j++) {
                telegraphing += pool.get(j).lineBeam().telegraphing() ? 1 : 0;
            }
            most = Math.max(most, telegraphing);
        }
        return most;
    }

    @Test
    void neverMoreThanTwoLinesTelegraphAtOnce() {
        Enemy a = bunker(GATED, 100, 150);
        Enemy b = bunker(GATED, 240, 150);
        Enemy c = bunker(GATED, 380, 150);
        steps(SimStep.ticks(0.5), 240, 96);
        assertTrue(a.lineBeam().telegraphing(), "the first two in the pool's order");
        assertTrue(b.lineBeam().telegraphing());
        assertFalse(c.lineBeam().telegraphing(), "the third waits while two telegraph");
        steps(SimStep.ticks(1.0) - 1, 240, 96);
        assertFalse(c.lineBeam().telegraphing(), "until one of them goes live");
        steps(1, 240, 96);
        assertTrue(a.lineBeam().live());
        assertTrue(c.lineBeam().telegraphing(), "then at once");
        assertTrue(steps(SimStep.ticks(30), 240, 96) <= 2, "never more than 2 in 30 s");
    }

    @Test
    void withoutTheGateThreeTelegraphTogetherAsBefore() {
        bunker(Act5Specs.RAIL_BUNKER, 100, 150);
        bunker(Act5Specs.RAIL_BUNKER, 240, 150);
        bunker(Act5Specs.RAIL_BUNKER, 380, 150);
        assertEquals(3, steps(SimStep.ticks(0.5), 240, 96));
    }

    @Test
    void aLineRunsUpTheScreenToo() {
        Enemy low = bunker(GATED, 240, 500);
        steps(SimStep.ticks(0.6), 240, 300);
        assertTrue(low.lineBeam().telegraphing());
        assertTrue(low.y() < 300, "the bunker below the ship");
        assertTrue(low.lineBeam().dirY() > 0.99, "its line up the screen");
    }

    /** The distance from (x, y) to the line from (fromX, fromY) along the unit vector (dirX, dirY), px. */
    static double across(double fromX, double fromY, double dirX, double dirY, double x, double y) {
        return Math.abs((x - fromX) * dirY - (y - fromY) * dirX);
    }

    @Test
    void aScrollingBunkersLineRidesOnItsMuzzleWithItsDirectionLocked() {
        Enemy unit = bunker(GATED, 100, 150);
        LineBeam line = unit.lineBeam();
        double shipX = 300;
        double shipY = 96;
        int step = 0;
        while (!line.telegraphing() && step++ < SimStep.ticks(2)) {
            steps(1, shipX, shipY, SCROLL);
        }
        assertTrue(line.telegraphing());
        double dirX = line.dirX();
        double dirY = line.dirY();
        double startY = unit.y();
        assertTrue(
                across(unit.x(), unit.y(), dirX, dirY, shipX, shipY) > 10,
                "led: the telegraph from the muzzle now passes beside the ship");
        while (line.running()) {
            assertEquals(unit.x(), line.fromX(), 1e-9, "on the bunker");
            assertEquals(unit.y(), line.fromY(), 1e-9, "scrolling with it");
            assertEquals(dirX, line.dirX(), 1e-12, "the direction locked");
            assertEquals(dirY, line.dirY(), 1e-12);
            if (line.live()) {
                break;
            }
            steps(1, shipX, shipY, SCROLL);
        }
        assertTrue(line.live());
        assertTrue(startY - unit.y() > 100, "the ground scrolled the bunker on over the telegraph");
        Hull hull = TestSpecs.HULL;
        EnemySpec.LaserLine rail = GATED.laserLine().orElseThrow();
        assertTrue(
                across(line.fromX(), line.fromY(), dirX, dirY, shipX, shipY) < 1,
                "round 69's B.2: the live line passes through the ship's locked point: "
                        + across(line.fromX(), line.fromY(), dirX, dirY, shipX, shipY));
        assertTrue(line.strikes(rail, hull, shipX, shipY), "a still ship is hit");
        assertFalse(line.strikes(rail, hull, shipX + 60, shipY), "a sidestep in the telegraph dodges");
        double along = 200;
        assertTrue(
                line.strikes(rail, hull, unit.x() + dirX * along, unit.y() + dirY * along),
                "the band runs from the bunker where it is now");
    }

    @Test
    void aNearVerticalRidingLineHitsAStillShipAndASidestepDodgesIt() {
        Enemy unit = bunker(GATED, 240, 150);
        LineBeam line = unit.lineBeam();
        int step = 0;
        while (!line.live() && step++ < SimStep.ticks(3)) {
            steps(1, 240, 96, SCROLL);
        }
        assertTrue(line.live());
        assertTrue(Math.abs(line.dirY()) > 0.99, "straight down the screen");
        Hull hull = TestSpecs.HULL;
        EnemySpec.LaserLine rail = GATED.laserLine().orElseThrow();
        assertTrue(line.strikes(rail, hull, 240, 96), "the band slides along itself: a still ship is hit");
        assertFalse(line.strikes(rail, hull, 240 + 60, 96), "a sidestep out of the 8 px band");
    }

    @Test
    void scrollingNeverMoreThanTwoLinesTelegraphAtOnce() {
        bunker(GATED, 100, 150);
        bunker(GATED, 240, 150);
        bunker(GATED, 380, 150);
        assertEquals(2, steps(SimStep.ticks(1.0), 240, 96, SCROLL / 4), "the gate as before");
    }
}
