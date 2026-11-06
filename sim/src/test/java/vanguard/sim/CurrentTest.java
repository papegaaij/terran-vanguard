package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1a (design/enemies, Distorted space): the current's sideways drift (the scroll times sin θ, eased
 * between its profile's points) carries a unit flying on its own but not one holding its station, the enemy bullets
 * (an aimed volley led so it still crosses the ship's point), the debris and a chain's history; the ship never drifts;
 * a unit carried out through a side edge has left (no bounty, no kill); a reversal is told 2 s ahead and the current is
 * not calm from the tell to its run's end; the drift flips with a reversed scroll; a reversing level with a current
 * may hold chains and debris.
 */
class CurrentTest {
    /** A plain flyer coming straight down at {@code speed} px/s. */
    private static EnemySpec flyer(double speed) {
        return EnemySpec.of("flyer", 100, new Hitbox(20, 20), Layer.AIR, 0, false, 5, speed);
    }

    /** A plain hover holding 120 px below the top edge for 20 s. */
    private static final EnemySpec HOVER = EnemySpec.of("hover", 100, new Hitbox(30, 30), Layer.AIR, 0, false, 5, 120)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(20, 20), new Range(120, 120))));

    private static Sortie sortie(Optional<HazardSpec.Current> current, WaveSpec... waves) {
        return Act7ASpecs.sortie(Act7ASpecs.level(40, List.of(waves), current));
    }

    @Test
    void theAngleEasesBetweenItsPoints() {
        Current current = new Current(
                new HazardSpec.Current(List.of(new HazardSpec.Bend(10, 0), new HazardSpec.Bend(14, 20)), List.of(), 2));
        assertEquals(0, current.degreesAt(0));
        assertEquals(0, current.degreesAt(SimStep.ticks(10)));
        assertEquals(10, current.degreesAt(SimStep.ticks(12)), 1e-9, "smoothstep's middle");
        assertTrue(current.degreesAt(SimStep.ticks(11)) < 5, "eased in");
        assertEquals(20, current.degreesAt(SimStep.ticks(30)), "held after the last point");
    }

    @Test
    void aFlyingUnitDriftsAStationHolderAndTheShipDoNot() {
        Optional<HazardSpec.Current> twenty = Optional.of(Act7ASpecs.steady(20, List.of()));
        Sortie drifting = sortie(twenty, Act7ASpecs.single(0, flyer(40)), Act7ASpecs.single(0, HOVER));
        Sortie still = sortie(Optional.empty(), Act7ASpecs.single(0, flyer(40)), Act7ASpecs.single(0, HOVER));
        double perStep = 130 * SimStep.SECONDS * Math.sin(Math.toRadians(20));
        int steps = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            drifting.step(Command.NONE);
            still.step(Command.NONE);
            if (Act7ASpecs.find(drifting, "flyer") != null) {
                steps++;
            }
        }
        assertEquals(perStep * SimStep.PER_SECOND, drifting.current().drift(), 1e-4);
        double shifted = Act7ASpecs.find(drifting, "flyer").x()
                - Act7ASpecs.find(still, "flyer").x();
        assertEquals(perStep * (steps - 1), shifted, perStep * 1.01, "the flyer took the drift every step");
        Enemy hover = Act7ASpecs.find(drifting, "hover");
        assertEquals(Enemy.Phase.HOLD, hover.phase);
        double x = hover.x();
        for (int i = 0; i < SimStep.ticks(1); i++) {
            drifting.step(Command.NONE);
        }
        assertEquals(x, hover.x(), 1e-9, "a station holds on the screen against the drift");
        assertEquals(still.ship().x(), drifting.ship().x(), 1e-9, "the ship never drifts");
    }

    @Test
    void aUnitTheDriftCarriesOutThroughASideEdgeHasLeftUnpaid() {
        Sortie sortie = sortie(Optional.of(Act7ASpecs.steady(25, List.of())), Act7ASpecs.single(0, flyer(20)));
        double lastX = 0;
        boolean seen = false;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            Enemy flyer = Act7ASpecs.find(sortie, "flyer");
            if (flyer != null) {
                lastX = flyer.x();
                seen = true;
            }
        }
        assertTrue(seen);
        assertNull(Act7ASpecs.find(sortie, "flyer"), "gone");
        assertTrue(lastX > PlayField.WIDTH - 20, "through the right edge, downwind: " + lastX);
        assertEquals(0, sortie.kills(), "not a kill");
        assertEquals(0, sortie.credits(), "no bounty");
    }

    @Test
    void anAimedVolleyIsLedSoThatDriftingItCrossesTheShipsPoint() {
        EnemySpec gunner = HOVER.withGun(Optional.of(EnemyGun.aimed(1.0, 0.3, 1, 160, 4, false)));
        Sortie sortie = sortie(Optional.of(Act7ASpecs.steady(25, List.of())), Act7ASpecs.single(0, gunner));
        Map<EnemyBullet, Double> above = new IdentityHashMap<>();
        int crossed = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            Pool<EnemyBullet> bullets = sortie.force.bullets();
            double shipY = sortie.ship().y();
            for (int k = 0; k < bullets.size(); k++) {
                EnemyBullet bullet = bullets.get(k);
                Double was = above.put(bullet, bullet.y());
                if (was != null && was > shipY && bullet.y() <= shipY) {
                    crossed++;
                    assertEquals(sortie.ship().x(), bullet.x(), 6, "it crosses the ship's point despite the drift");
                }
            }
        }
        assertTrue(crossed >= 3, "volleys crossed: " + crossed);
    }

    @Test
    void theBulletsAndTheDebrisRideTheDrift() {
        Current current = new Current(Act7ASpecs.steady(20, List.of()));
        current.reset();
        current.step(0, 130 * SimStep.SECONDS, new SimEvents(8));
        Pool<EnemyBullet> bullets = new Pool<>(2, EnemyBullet::new, EnemyBullet[]::new);
        bullets.obtain().fire(100, 200, 0, -100, 4);
        Pool<Debris> debris = new Pool<>(2, Debris::new, Debris[]::new);
        Pool<Pickup> pickups = new Pool<>(2, Pickup::new, Pickup[]::new);
        current.carry(bullets, pickups, debris);
        assertEquals(100 + current.driftStep(), bullets.get(0).x(), 1e-12);
    }

    @Test
    void aReversalIsToldTwoSecondsAheadAndTheCurrentIsNotCalmTillItsRunsEnd() {
        HazardSpec.Current spec = Act7ASpecs.steady(15, List.of(new HazardSpec.Reversal(10, 4)));
        LevelScript level = Act7ASpecs.level(40, List.of(), Optional.of(spec)).toBuilder()
                .sections(List.of(new Section(10, 130), new Section(16, -130, false, 2, 1), new Section(40, 130)))
                .build();
        Sortie sortie = Act7ASpecs.sortie(level);
        int told = -1;
        int run = -1;
        boolean flipped = false;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.REVERSAL_TOLD) > 0) {
                told = sortie.levelTick;
            }
            if (sortie.events().count(SimEvents.Type.REVERSAL_RUN) > 0) {
                run = sortie.levelTick;
            }
            if (sortie.reversed()) {
                flipped = true;
                assertTrue(sortie.current().driftStep() <= 0, "the drift flips with the scroll");
            }
            int t = sortie.levelTick;
            assertEquals(
                    t >= SimStep.ticks(8) && t < SimStep.ticks(16),
                    !sortie.current().calm(t),
                    "not calm at " + t);
        }
        assertEquals(SimStep.ticks(8), told, 1);
        assertEquals(SimStep.ticks(10), run, 1);
        assertTrue(flipped);
    }

    @Test
    void aChainSwirlsWithTheCurrent() {
        WaveSpec chain = WaveSpec.builder(
                        1, WaveSpec.Formation.SNAKE, FarsideTest.COILWYRM, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(
                        List.of(new WaveSpec.At(200, -60), new WaveSpec.At(200, 300), new WaveSpec.At(200, 700))))
                .build();
        Sortie drifting = sortie(Optional.of(Act7ASpecs.steady(15, List.of())), chain);
        Sortie still = sortie(Optional.empty(), chain);
        for (int i = 0; i < SimStep.ticks(4); i++) {
            drifting.step(Command.NONE);
            still.step(Command.NONE);
        }
        assertTrue(drifting.enemyCount() > 2);
        double head = drifting.enemy(0).x() - still.enemy(0).x();
        double tail = drifting.enemy(drifting.enemyCount() - 1).x()
                - still.enemy(still.enemyCount() - 1).x();
        assertTrue(head > 20, "the head point's path drifted: " + head);
        assertTrue(tail > 20, "the body follows its drifted history: " + tail);
    }

    @Test
    void aReversingLevelWithACurrentMayHoldDebrisAndChains() {
        LevelScript base = Act7ASpecs.level(40, List.of(), Optional.empty()).toBuilder()
                .sections(List.of(new Section(10, 130), new Section(16, -130, false, 2, 1), new Section(40, 130)))
                .build();
        HazardSpec debris = base.hazards().withDebris(List.of(Act6DSpecs.pod(20, 200)));
        assertThrows(
                IllegalArgumentException.class,
                () -> base.toBuilder().hazards(debris).build());
        assertDoesNotThrow(() -> base.toBuilder()
                .hazards(debris.withCurrent(Optional.of(Act7ASpecs.steady(10, List.of(new HazardSpec.Reversal(9, 4))))))
                .build());
        assertFalse(base.hazards().current().isPresent());
    }
}
