package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part G (design/campaign Level 20, Reverse scroll): signed section speeds, the direction of travel, the flipped
 * units' travel frame ({@link TravelFrame}), the ground's and the pickups' two edges, the TOP edge warnings and the
 * BOTTOM lessons, the level's cross-checks and the wingman's formation by travel.
 */
class ReverseScrollTest {
    private static final double EPSILON = 1e-9;

    /**
     * A Level 04-like mix of Act 1–3 units on both scrolls: a V of Skitters, hovering Needlers, a diving Stinger,
     * swooping Dust Kites, Burrowers, a Dust Devil with its pull and spiral, a Wraith ambush, a looping Mote Swarm, a
     * rear wave and a side stream, a Spine Turret and a mortar nest on the ground.
     */
    private static LevelScript mixed(double sign) {
        return mixed(sign, true);
    }

    /** As {@link #mixed(double)}, the diving Stinger left out unless {@code diver} (a diver is never flipped). */
    private static LevelScript mixed(double sign, boolean diver) {
        EnemySpec skitter = TestSpecs.SKITTER;
        List<WaveSpec> all = List.of(
                TestSpecs.wave(3, WaveSpec.Formation.V_WING, skitter, 5, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                TestSpecs.wave(
                        4,
                        WaveSpec.Formation.LINE_ABREAST,
                        TestSpecs.NEEDLER,
                        2,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE),
                TestSpecs.wave(
                        5,
                        WaveSpec.Formation.SINGLE,
                        GroundAndDiveTest.STINGER,
                        1,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE),
                TestSpecs.wave(
                        6,
                        WaveSpec.Formation.LINE_ABREAST,
                        KiteSwoopTest.kite(KiteSwoopTest.SWOOP),
                        3,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE),
                BurrowTest.field(7, BurrowTest.burrower(), 2),
                VortexTest.wave(8, VortexTest.devil(), 1),
                PartDSpecs.ambush(9, PartDSpecs.wraith(), 2),
                PartDSpecs.swarm(10, 8, 1),
                TestSpecs.wave(
                        11, WaveSpec.Formation.LINE_ABREAST, skitter, 3, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE),
                stream(12, skitter, WaveSpec.Edge.LEFT));
        List<WaveSpec> waves = all.stream()
                .filter(wave -> diver || wave.enemy().dive().isEmpty())
                .toList();
        return LevelScript.builder(4, 1)
                .sections(List.of(new Section(14, sign * 130), new Section(40, sign * 160, false, 2, 1.5)))
                .waves(waves)
                .groundUnits(List.of(
                        new GroundUnit(5, 120, GroundAndDiveTest.TURRET, -1),
                        new GroundUnit(9, 330, CraterNestTest.MORTAR, -1)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    private static Sortie untouchable(LevelScript level) {
        return new Sortie(7, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    @Test
    void aReversedLevelFliesEveryUnitMirrored() {
        // Every speed negated and the ship's input mirrored (it holds the bottom edge going forward, the top one going
        // back), guns cold: every unit flies the same course top to bottom mirrored. The Stinger is left out: a diver
        // is never flipped (aDiverEntersFromTheTopInAReversedScroll).
        Sortie forward = untouchable(mixed(1, false));
        Sortie back = untouchable(mixed(-1, false));
        int seen = 0;
        int flippedSeen = 0;
        java.util.Set<String> kinds = new java.util.HashSet<>();
        for (int tick = 0; tick < SimStep.ticks(36); tick++) {
            forward.step(Command.DOWN.bit());
            back.step(Command.UP.bit());
            assertEquals(1, forward.travel());
            assertEquals(-1, back.travel());
            assertEquals(forward.enemyCount(), back.enemyCount(), "units at step " + tick);
            // The units fire alike (their volleys mirrored; a bullet on an edge may leave a step apart by rounding).
            assertEquals(
                    forward.events().count(SimEvents.Type.ENEMY_FIRED),
                    back.events().count(SimEvents.Type.ENEMY_FIRED),
                    "volleys at step " + tick);
            assertEquals(forward.flockCount(), back.flockCount(), "flocks at step " + tick);
            assertEquals(forward.edgeWarnings() == 0, back.edgeWarnings() == 0, "warnings at step " + tick);
            for (int i = 0; i < forward.enemyCount(); i++) {
                Enemy a = forward.enemy(i);
                Enemy b = back.enemy(i);
                String what = a.spec().slug() + " #" + a.serial() + " at step " + tick;
                assertEquals(a.serial(), b.serial(), what);
                assertFalse(a.flipped(), what);
                assertTrue(b.flipped(), what);
                assertEquals(a.x(), b.x(), EPSILON, what);
                assertEquals(PlayField.HEIGHT - a.y(), b.y(), EPSILON, what);
                assertEquals(PlayField.HEIGHT - a.renderY(0.5), b.renderY(0.5), EPSILON, what);
                assertEquals(
                        0,
                        Math.IEEEremainder(StrictMath.PI - a.facing() - b.facing(), 2 * StrictMath.PI),
                        EPSILON,
                        what);
                assertEquals(a.hp(), b.hp(), what);
                assertEquals(a.layer(), b.layer(), what);
                seen++;
                kinds.add(a.spec().slug());
            }
            flippedSeen += back.enemyCount();
        }
        assertTrue(seen > 2000, "the units flew: " + seen);
        assertEquals(seen, flippedSeen);
        assertEquals(
                java.util.Set.of(
                        "skitter",
                        "needler",
                        "dust-kite",
                        BurrowTest.burrower().slug(),
                        "dust-devil",
                        "wraith",
                        "mote-swarm",
                        "spine-turret",
                        CraterNestTest.MORTAR.slug()),
                kinds);
    }

    @Test
    void theTurnaboutReversesTheTravelThroughZero() {
        // Level 20's sections: +140, then +140 → −120 over 4 s from t = 20 (zero at ≈ 22.2), then −120.
        LevelScript level = turnabout(List.of(), List.of(new GroundUnit(19, 240, GroundAndDiveTest.TURRET, -1)));
        Sortie sortie = untouchable(level);
        assertTrue(level.reverses());
        int turnedAt = -1;
        boolean slowedThroughZero = false;
        List<Double> turretYs = new ArrayList<>();
        int goneAt = -1;
        for (int tick = 0; tick < SimStep.ticks(30); tick++) {
            sortie.step(Command.NONE);
            assertEquals(
                    Sortie.travelAt(level, sortie.levelSeconds() - SimStep.SECONDS), sortie.travel(), "at " + tick);
            if (sortie.travel() < 0 && turnedAt < 0) {
                turnedAt = tick;
                assertTrue(sortie.reversed());
            }
            slowedThroughZero |= Math.abs(sortie.groundSpeed()) < 2;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy unit = sortie.enemy(i);
                if (unit.spec().slug().equals("spine-turret")) {
                    assertFalse(unit.flipped(), "it entered before the turnabout");
                    turretYs.add(unit.y());
                }
            }
            if (!turretYs.isEmpty() && sortie.enemyCount() == 0 && goneAt < 0) {
                goneAt = tick;
            }
        }
        assertTrue(slowedThroughZero);
        double turnSeconds = turnedAt * SimStep.SECONDS;
        assertTrue(turnSeconds > 22.0 && turnSeconds < 22.4, "the scroll turns at ≈ 22.2 s: " + turnSeconds);
        assertTrue(sortie.groundSpeed() == -120, "then −120: " + sortie.groundSpeed());
        double lowest = turretYs.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        assertTrue(lowest < 400, "the turret came down with the scroll: " + lowest);
        assertTrue(goneAt > turnedAt, "and left after the turn");
        assertTrue(turretYs.getLast() >= PlayField.HEIGHT, "by the top edge: " + turretYs.getLast());
        assertEquals(1, Sortie.travelAt(level, 10));
        assertEquals(1, Sortie.travelAt(level, 22.0));
        assertEquals(-1, Sortie.travelAt(level, 22.4));
        assertEquals(-1, Sortie.travelAt(level, 29));
        assertEquals(1, Sortie.travelAt(mixed(1), 20), "a level without a reversed section");
    }

    /** Level 20's first sections: 20 s at +140, the turnabout's 4 s ease to −120, then −120 to t = 40. */
    private static LevelScript turnabout(List<WaveSpec> waves, List<GroundUnit> units) {
        return LevelScript.builder(20, 3)
                .sections(List.of(new Section(20, 140), new Section(40, -120, false, 2, 4)))
                .waves(waves)
                .groundUnits(units)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    @Test
    void aWaveAfterTheTurnComesUpFromTheBottomAndOneBeforeItFromTheTop() {
        Sortie sortie = untouchable(turnabout(
                List.of(
                        TestSpecs.wave(
                                18,
                                WaveSpec.Formation.SINGLE,
                                TestSpecs.SKITTER,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE),
                        TestSpecs.wave(
                                24,
                                WaveSpec.Formation.SINGLE,
                                TestSpecs.SKITTER,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)),
                List.of(new GroundUnit(25, 200, GroundAndDiveTest.TURRET, -1))));
        double firstY = Double.NaN;
        double secondY = Double.NaN;
        double turretY = Double.NaN;
        double turretLast = Double.NaN;
        for (int tick = 0; tick < SimStep.ticks(30); tick++) {
            sortie.step(Command.NONE);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy unit = sortie.enemy(i);
                if (unit.serial() == 0 && Double.isNaN(firstY)) {
                    firstY = unit.y();
                    assertFalse(unit.flipped());
                } else if (unit.serial() == 1 && Double.isNaN(secondY)) {
                    secondY = unit.y();
                    assertTrue(unit.flipped());
                    assertEquals(WaveSpec.Entry.FRONT, unit.entry(), "front by travel");
                } else if (unit.spec().slug().equals("spine-turret")) {
                    assertTrue(unit.flipped());
                    if (Double.isNaN(turretY)) {
                        turretY = unit.y();
                    }
                    turretLast = unit.y();
                }
            }
        }
        assertTrue(firstY > PlayField.HEIGHT, "before the turn: in at the top: " + firstY);
        assertTrue(secondY < 0, "after it: in at the bottom: " + secondY);
        assertEquals(-GroundAndDiveTest.TURRET.hitbox().height() / 2 + 120 * SimStep.SECONDS, turretY, 1e-6);
        assertTrue(turretLast > turretY + 100, "the turret rides the ground up the screen: " + turretLast);
    }

    @Test
    void aDiverEntersFromTheTopInAReversedScroll() {
        // 2026-10-21 (user): "when flying backwards, the stingers should still enter from the top." A diver is never
        // flipped: after the turn it comes in at the top edge and dives down the screen at the ship, as going forward.
        WaveSpec stinger = TestSpecs.wave(
                25, WaveSpec.Formation.SINGLE, GroundAndDiveTest.STINGER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        Sortie sortie = untouchable(turnabout(List.of(stinger), List.of()));
        double firstY = Double.NaN;
        double lowest = Double.POSITIVE_INFINITY;
        boolean fired = false;
        for (int tick = 0; tick < SimStep.ticks(32); tick++) {
            // The ship sidesteps once the Stinger has paused (as in GroundAndDiveTest), so its fan has room.
            sortie.step(sortie.levelSeconds() > 27.2 ? Command.RIGHT.bit() : Command.NONE);
            assertTrue(sortie.reversed() || sortie.levelSeconds() < 23, "the scroll runs reversed from ≈ 22.2 s");
            fired |= sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy unit = sortie.enemy(i);
                assertEquals("stinger", unit.spec().slug());
                assertFalse(unit.flipped(), "a diver is never flipped");
                if (Double.isNaN(firstY)) {
                    firstY = unit.y();
                }
                lowest = Math.min(lowest, unit.y());
            }
        }
        assertTrue(firstY > PlayField.HEIGHT, "in at the top: " + firstY);
        assertTrue(lowest < PlayField.HEIGHT / 2.0, "it dived down the screen: " + lowest);
        assertTrue(fired, "its fan in the dive");
        // Its warning is a forward level's: a front diver warns no edge, its warning time or not (no BOTTOM lesson).
        assertEquals(0, warningsAt(turnabout(List.of(stinger), List.of()), 24.5));
        assertEquals(0, warningsAt(turnabout(List.of(withWarning(stinger, 4)), List.of()), 23));
    }

    @Test
    void theEdgesWarnByTheScreenInAReversedScroll() {
        WaveSpec rear = TestSpecs.wave(
                30, WaveSpec.Formation.LINE_ABREAST, TestSpecs.SKITTER, 3, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE);
        WaveSpec side = stream(30, TestSpecs.SKITTER, WaveSpec.Edge.RIGHT);
        WaveSpec front = TestSpecs.wave(
                30, WaveSpec.Formation.V_WING, TestSpecs.SKITTER, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        WaveSpec lesson = withWarning(front, 4);
        assertEquals(WarningEdge.TOP.bit(), warningsAt(turnabout(List.of(rear), List.of()), 28));
        assertEquals(WarningEdge.RIGHT.bit(), warningsAt(turnabout(List.of(side), List.of()), 28));
        assertEquals(0, warningsAt(turnabout(List.of(front), List.of()), 28), "the front is the bottom: no warning");
        assertEquals(WarningEdge.BOTTOM.bit(), warningsAt(turnabout(List.of(lesson), List.of()), 26.5), "a lesson");
        assertEquals(0, warningsAt(turnabout(List.of(lesson), List.of()), 25.5), "4 s ahead");
        // Going forward a front wave warns nothing, its warning time or not, and a rear one the bottom edge.
        assertEquals(0, warningsAt(turnabout(List.of(withWarning(rearAt(front, 12), 4)), List.of()), 10));
        assertEquals(WarningEdge.BOTTOM.bit(), warningsAt(turnabout(List.of(rearAt(rear, 12)), List.of()), 10));
        assertEquals(8, WarningEdge.TOP.bit(), "TOP is bit 3, after the others");
    }

    @Test
    void aFlippedReentryWarnsNoEdge() {
        // A Wraith ambush swoops off the frame's bottom (the screen's top) and comes back from there; a swarm's
        // loop-back too. 2026-10-21 (user): a re-entry is the unit's own flight pattern: no edge warning.
        LevelScript ambush = turnabout(List.of(PartDSpecs.ambush(25, PartDSpecs.wraith(), 2)), List.of());
        LevelScript swarm = turnabout(List.of(PartDSpecs.swarm(25, 6, 1)), List.of());
        for (LevelScript level : List.of(ambush, swarm)) {
            Sortie sortie = untouchable(level);
            int warned = 0;
            boolean flippedRearSeen = false;
            for (int tick = 0; tick < SimStep.ticks(39); tick++) {
                sortie.step(Command.NONE);
                warned |= sortie.edgeWarnings();
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    flippedRearSeen |= sortie.enemy(i).flipped();
                }
            }
            assertEquals(0, warned, level.waves().getFirst().enemy().slug());
            assertTrue(flippedRearSeen);
        }
    }

    @Test
    void aFlippedRearUnitIsAnActiveTopEdgeAndRookStaysInWing() {
        Sortie sortie = untouchable(turnabout(
                List.of(TestSpecs.wave(
                        25,
                        WaveSpec.Formation.LINE_ABREAST,
                        TestSpecs.SKITTER,
                        3,
                        WaveSpec.Entry.REAR,
                        WaveSpec.Edge.NONE)),
                List.of()));
        int active = 0;
        for (int tick = 0; tick < SimStep.ticks(27); tick++) {
            sortie.step(Command.NONE);
            active |= sortie.force.activeEdges();
        }
        assertEquals(WarningEdge.TOP.bit(), active);
    }

    @Test
    void rookStaysInWingForAWaveFromBehindAndTrailsOneBeforeTheTurn() {
        // Behind by travel is the top in a reversed scroll: no Trail (he would fly into the bottom's waves ahead).
        for (double t : new double[] {10, 25}) {
            LevelScript level = turnabout(
                    List.of(TestSpecs.wave(
                            t,
                            WaveSpec.Formation.LINE_ABREAST,
                            TestSpecs.SKITTER,
                            3,
                            WaveSpec.Entry.REAR,
                            WaveSpec.Edge.NONE)),
                    List.of());
            Sortie sortie = new Sortie(
                    1,
                    TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                    level,
                    TestSpecs.RULES.withInvulnerableShip(),
                    TestSpecs.FULL_ARMOUR);
            boolean trail = false;
            while (sortie.levelSeconds() < t + 3) {
                sortie.step(Command.NONE);
                trail |= sortie.wingman().orElseThrow().formation() == Wingman.Formation.TRAIL;
            }
            assertEquals(t < 20, trail, "a rear wave at t=" + t);
        }
    }

    @Test
    void groundObjectsAndPickupsFollowTheTravel() {
        GroundObjectSpec early =
                new GroundObjectSpec(19.5, 120, new Hitbox(32, 24), 50, 5, Optional.empty(), 0, 0, "", false);
        GroundObjectSpec late =
                new GroundObjectSpec(25, 360, new Hitbox(32, 24), 50, 5, Optional.empty(), 0, 0, "", false);
        LevelScript level = turnabout(List.of(), List.of()).toBuilder()
                .groundObjects(List.of(early, late))
                .drops(DropSpec.NONE.withPlacedPickups(List.of(
                        new DropSpec.PlacedPickup(10, 80, 300, PickupType.SHIELD_CELL),
                        new DropSpec.PlacedPickup(26, 400, 300, PickupType.SHIELD_CELL))))
                .build();
        Sortie sortie = untouchable(level);
        List<Double> earlyYs = new ArrayList<>();
        List<Double> lateYs = new ArrayList<>();
        List<Double> pickupYs = new ArrayList<>();
        for (int tick = 0; tick < SimStep.ticks(40); tick++) {
            sortie.step(Command.NONE);
            for (int i = 0; i < sortie.groundObjectCount(); i++) {
                GroundObject object = sortie.groundObject(i);
                (object.x() == 120 ? earlyYs : lateYs).add(object.y());
            }
            for (int i = 0; i < sortie.pickupCount(); i++) {
                if (sortie.pickup(i).x() == 400) {
                    pickupYs.add(sortie.pickup(i).y());
                }
            }
        }
        assertTrue(earlyYs.getFirst() > PlayField.HEIGHT, "placed at the top going forward");
        double lowest = earlyYs.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        assertTrue(lowest < PlayField.HEIGHT && earlyYs.getLast() > PlayField.HEIGHT, "back up and off the top");
        assertTrue(lateYs.getFirst() < 0, "placed at the bottom going back: " + lateYs.getFirst());
        assertTrue(lateYs.getLast() > PlayField.HEIGHT, "and off the top");
        assertTrue(pickupYs.getLast() > pickupYs.getFirst() + 100, "a pickup drifts up the screen");
    }

    @Test
    void aReversedLevelRefusesWhatEntersWithTheGround() {
        LevelScript level = turnabout(List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> new Section(30, -100, true));
        assertThrows(
                IllegalArgumentException.class,
                () -> level.toBuilder().ground(GroundSpec.NONE.withWater(true)).build());
        assertThrows(IllegalArgumentException.class, () -> level.toBuilder()
                .sections(List.of(new Section(10, 140), new Section(20, 0), new Section(40, -120)))
                .build());
        assertThrows(IllegalArgumentException.class, () -> level.toBuilder()
                .waves(List.of(TestSpecs.wave(
                        5,
                        WaveSpec.Formation.SNAKE,
                        FarsideTest.COILWYRM,
                        1,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)))
                .build());
        // Going forward it changes nothing: water, a halt and chains as before.
        LevelScript forward = level.toBuilder()
                .sections(List.of(new Section(10, 140), new Section(20, 0), new Section(40, 120)))
                .ground(GroundSpec.NONE.withWater(true))
                .build();
        assertFalse(forward.reverses());
    }

    @Test
    void aReversedSortieIsDeterministicAndDoesNotAllocate() {
        Sortie a = untouchable(mixed(-1));
        Sortie b = untouchable(mixed(-1));
        for (int tick = 0; tick < SimStep.ticks(20); tick++) {
            a.step(Command.UP.bit());
            b.step(Command.UP.bit());
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(() -> untouchable(mixed(-1)), sortie -> {
            for (int i = 0; i < SimStep.ticks(30); i++) {
                sortie.step(i % 120 < 60 ? Command.UP.bit() : Command.LEFT.bit() | Command.FIRE.bit());
            }
        });
        assertEquals(0, allocated, "a reversed sortie steps without allocating");
    }

    private static int warningsAt(LevelScript level, double seconds) {
        Sortie sortie = untouchable(level);
        while (sortie.levelSeconds() < seconds) {
            sortie.step(Command.NONE);
        }
        return sortie.edgeWarnings();
    }

    private static WaveSpec withWarning(WaveSpec wave, double seconds) {
        return WaveSpec.builder(wave.t(), wave.formation(), wave.enemy(), wave.count(), wave.entry(), wave.edge())
                .holdSeconds(wave.holdSeconds())
                .warningSeconds(Optional.of(seconds))
                .breakGroup(wave.breakGroup())
                .speed(wave.speed())
                .intervalSeconds(wave.intervalSeconds())
                .carried(wave.carried())
                .at(wave.at())
                .paths(wave.paths())
                .loopBack(wave.loopBack())
                .tag(wave.tag())
                .field(wave.field())
                .clockwise(wave.clockwise())
                .build();
    }

    /** A stream of 3 {@code enemy} from the side {@code edge} at {@code t}, 0.5 s apart. */
    private static WaveSpec stream(double t, EnemySpec enemy, WaveSpec.Edge edge) {
        return WaveSpec.builder(t, WaveSpec.Formation.STREAM, enemy, 3, WaveSpec.Entry.SIDES, edge)
                .intervalSeconds(Optional.of(0.5))
                .build();
    }

    private static WaveSpec rearAt(WaveSpec wave, double t) {
        return TestSpecs.wave(t, wave.formation(), wave.enemy(), wave.count(), wave.entry(), wave.edge());
    }
}
