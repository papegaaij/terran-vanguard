package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M10 part C, step C1 (design/enemies/air/mirror-interceptor, Movement; design/campaign Level 37, Spacing): the Mirror
 * Interceptor's stations. A pincer flies in from both side edges to 90–130 px in, 160–300 px below the top edge, the
 * hit boxes of one edge's units at least 80 px apart; a rear ambush rises from the bottom edge to 90–120 px above it in
 * its lanes (x = (i + 1) × 480 ÷ (n + 1), wider when the hulls would crowd). Each holds 7 s, strafing along its
 * station toward the ship (up and down at a side, across at the rear) at 40 px/s, at most 30 px from its station
 * point, never closing on a neighbour; then a side unit leaves through its own edge, a rear one up through the nearer
 * side edge. Deterministic, allocation-free.
 */
class MirrorStationTest {
    private static WaveSpec pincer(double t, int count) {
        return WaveSpec.builder(
                        t,
                        WaveSpec.Formation.PINCER,
                        Act6CSpecs.MIRROR,
                        count,
                        WaveSpec.Entry.SIDES,
                        WaveSpec.Edge.NONE)
                .build();
    }

    private static WaveSpec rear(double t, int count) {
        return WaveSpec.builder(
                        t,
                        WaveSpec.Formation.REAR_AMBUSH,
                        Act6CSpecs.MIRROR,
                        count,
                        WaveSpec.Entry.REAR,
                        WaveSpec.Edge.NONE)
                .build();
    }

    private static Sortie sortie(WaveSpec... waves) {
        return Act6CSpecs.sortie(Act6CSpecs.plain(30, List.of(waves)));
    }

    private static List<Enemy> mirrors(Sortie sortie) {
        List<Enemy> found = new ArrayList<>();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec() == Act6CSpecs.MIRROR) {
                found.add(sortie.enemy(i));
            }
        }
        return found;
    }

    private static void run(Sortie sortie, double seconds) {
        Act6ASpecs.run(sortie, SimStep.ticks(seconds), Command.NONE);
    }

    @Test
    void aPincerHoldsSideStationsItsHullsApartThenLeavesThroughItsEdges() {
        Sortie sortie = sortie(pincer(1, 4));
        run(sortie, 2);
        List<Enemy> units = mirrors(sortie);
        assertEquals(4, units.size());
        for (Enemy unit : units) {
            assertEquals(Enemy.Phase.HOLD, unit.phase, "on its station");
            double in = unit.x() < PlayField.WIDTH / 2.0 ? unit.x() : PlayField.WIDTH - unit.x();
            assertTrue(in >= 90 && in <= 130, "90–130 px in: " + in);
            double below = PlayField.HEIGHT - unit.stationHold.centre();
            assertTrue(below >= 160 - 1e-9 && below <= 300 + 1e-9, "its station 160–300 px below the top: " + below);
        }
        assertApart(units);
        // Strafing toward the ship (far below): down at 40 px/s, at most 30 px from the station point.
        double[] at = new double[4];
        for (int k = 0; k < 4; k++) {
            at[k] = units.get(k).y();
        }
        sortie.step(Command.NONE);
        for (int k = 0; k < 4; k++) {
            Enemy unit = units.get(k);
            assertEquals(
                    Math.max(at[k] - 40 * SimStep.SECONDS, unit.stationHold.centre() - 30),
                    unit.y(),
                    1e-9,
                    "down toward the ship");
        }
        run(sortie, 2);
        for (int k = 0; k < 4; k++) {
            assertEquals(units.get(k).stationHold.centre() - 30, units.get(k).y(), 1e-9, "30 px down");
        }
        assertApart(units);
        // The hold is 7 s from its stop (by 1.94 s); then each leaves sideways through the edge it came in by.
        run(sortie, 5);
        for (Enemy unit : units) {
            assertEquals(Enemy.Phase.LEAVE, unit.phase);
            assertEquals(unit.x() < PlayField.WIDTH / 2.0 ? -1 : 1, Math.signum(unit.vx));
            assertEquals(0, unit.vy, 1e-12);
        }
    }

    /** Two units holding on one edge keep their hit boxes the station's gap (80 px) apart. */
    private static void assertApart(List<Enemy> units) {
        for (int a = 0; a < units.size(); a++) {
            for (int b = a + 1; b < units.size(); b++) {
                Enemy one = units.get(a);
                Enemy two = units.get(b);
                if ((one.x() < PlayField.WIDTH / 2.0) == (two.x() < PlayField.WIDTH / 2.0)) {
                    double gap = Math.abs(one.y() - two.y()) - 48;
                    assertTrue(gap >= 80 - 1e-9, "hulls 80 px apart: " + gap);
                }
            }
        }
    }

    @Test
    void aRearAmbushHoldsAboveTheBottomEdgeInItsLanesAndLeavesUpThroughTheNearerSide() {
        Sortie sortie = sortie(rear(1, 2));
        run(sortie, 3);
        List<Enemy> units = mirrors(sortie);
        assertEquals(2, units.size());
        units.sort(java.util.Comparator.comparingDouble(Enemy::x));
        for (int k = 0; k < 2; k++) {
            Enemy unit = units.get(k);
            assertEquals(Enemy.Phase.HOLD, unit.phase);
            assertTrue(unit.y() >= 90 - 1e-9 && unit.y() <= 120 + 1e-9, "90–120 px above the bottom: " + unit.y());
            assertTrue(unit.stationHold.rear());
            assertEquals(160 * (k + 1), unit.stationHold.centre(), 1e-9, "its lane");
        }
        // Strafing across toward the ship's x (the middle), at most 30 px, never closer than 80 px between hulls.
        run(sortie, 3);
        assertTrue(units.get(1).x() - units.get(0).x() - 52 >= 80 - 1e-9, "apart");
        assertTrue(
                units.get(0).x() > 160 && units.get(0).x() <= 190 + 1e-9,
                "toward the ship: " + units.get(0).x());
        run(sortie, 3);
        for (Enemy unit : units) {
            assertEquals(Enemy.Phase.LEAVE, unit.phase);
            assertTrue(unit.vy > 0, "up");
            assertEquals(unit.x() <= PlayField.WIDTH / 2.0 ? -1 : 1, Math.signum(unit.vx), "the nearer side");
        }
    }

    @Test
    void threeRearLanesWidenToKeepTheGap() {
        Sortie sortie = sortie(rear(1, 3));
        run(sortie, 2.5);
        List<Enemy> units = mirrors(sortie);
        units.sort(java.util.Comparator.comparingDouble(Enemy::x));
        assertEquals(3, units.size());
        assertEquals(240 - 132, units.get(0).x(), 1e-9, "a hit box (52) and the gap (80) apart about the middle");
        assertEquals(240, units.get(1).x(), 1e-9);
        assertEquals(240 + 132, units.get(2).x(), 1e-9);
    }

    @Test
    void aStackTallerThanTheRangeIsCentredOnIt() {
        double[] depth = {200, 210, 220};
        boolean[] left = {true, true, false};
        Formations.spaced(depth, left, true, 128, new Range(160, 300));
        assertEquals(200 - 28, depth[0], 1e-9, "160–300 holds 128 apart: pushed down, then shifted up into it");
        assertEquals(300, depth[1], 1e-9);
        assertEquals(220, depth[2], 1e-9, "the other edge untouched");
        double[] tall = {160, 170, 180};
        Formations.spaced(tall, new boolean[] {true, true, true}, true, 128, new Range(160, 300));
        assertEquals(230 - 128, tall[0], 1e-9, "centred on 230");
        assertEquals(230, tall[1], 1e-9);
        assertEquals(230 + 128, tall[2], 1e-9);
    }

    @Test
    void aStationIsAFlyingUnitsHover() {
        assertThrows(IllegalArgumentException.class, () -> EnemySpec.of(
                        "x", 1, new Hitbox(10, 10), Layer.AIR, 1, false, 1, 100)
                .withStation(Optional.of(Act6CSpecs.STATION)));
    }

    @Test
    void itReplaysAndAllocatesNothing() {
        Supplier<Sortie> make = () -> sortie(pincer(1, 4), rear(4, 2), pincer(12, 2));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(20); i++) {
            a.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            b.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            assertEquals(a.stateHash(), b.stateHash());
        }
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(20); i++) {
                sortie.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            }
        }));
    }
}
