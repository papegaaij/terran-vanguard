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
 * M7 part H, step H1: the breaching arena boss ({@link BreachArena}; design/enemies/bosses/dust-colossus) on a test
 * colossus shaped like the design's medium numbers: a mouth of 1,000 (a 120 px box, the lips ×0.5) with its gullet ×2
 * while open; the cycle tell 2 / rise 1.5 / open 7 / bite tell 0.8 / dive 1.5 / buried 2, the edge warnings 3 s ahead,
 * the bite 70 px for 10, the intro 6 s; the points front (240, 30), left (30, 260), right (450, 260) and rear (240,
 * 515) from the top, phase 2 following the ship within 100–380 across and 160–420 down; phase 1 front → left → right →
 * rear, phase 2 rear → left → rear → right → front, phase 3 under the ship; the suction 45 px/s within 120 px, to 0
 * at 360. The arena crawls at 20 px/s from 3 s to 23 s, eased in from 140 over its first 2 s.
 */
class BreachArenaTest {
    static final int MOUTH = 0;
    static final double ARRIVE = 3;
    static final double ARENA_END = 23;
    static final double SPEED = 140;
    static final double CRAWL = 20;
    static final double MOUTH_HP = 1000;

    static final double FRONT_X = 240;
    static final double FRONT_Y = PlayField.HEIGHT - 30;
    static final double SIDE_Y = PlayField.HEIGHT - 260;
    static final double REAR_Y = PlayField.HEIGHT - 515;

    static final EnemyGun RING12 = EnemyGun.aimed(2.5, 0, 1, 100, 4, false);
    static final EnemyGun FAN5 = new EnemyGun(
            2.5, 0, 1, 130, 4, false, 5, Math.toRadians(50), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    static final BossSpec.Breach BREACH = new BossSpec.Breach(
            2.0,
            1.5,
            7,
            0.8,
            1.5,
            2,
            3,
            List.of(
                    new BossSpec.Point(FRONT_X, FRONT_Y),
                    new BossSpec.Point(30, SIDE_Y),
                    new BossSpec.Point(450, SIDE_Y),
                    new BossSpec.Point(240, REAR_Y)),
            List.of(
                    new BossSpec.Clamp(100, 380),
                    new BossSpec.Clamp(PlayField.HEIGHT - 420, PlayField.HEIGHT - 160),
                    new BossSpec.Clamp(PlayField.HEIGHT - 420, PlayField.HEIGHT - 160),
                    new BossSpec.Clamp(100, 380)),
            List.of(
                    new BossSpec.Order(
                            List.of(
                                    BreachArena.Pose.FRONT,
                                    BreachArena.Pose.LEFT,
                                    BreachArena.Pose.RIGHT,
                                    BreachArena.Pose.REAR),
                            false),
                    new BossSpec.Order(
                            List.of(
                                    BreachArena.Pose.REAR,
                                    BreachArena.Pose.LEFT,
                                    BreachArena.Pose.REAR,
                                    BreachArena.Pose.RIGHT,
                                    BreachArena.Pose.FRONT),
                            true),
                    new BossSpec.Order(List.of(BreachArena.Pose.UNDER), true)),
            70,
            10,
            6);

    static final BossSpec.Suction SUCTION = new BossSpec.Suction(45, 120, 360);

    private static BossSpec.Phase phase(String name, List<Integer> attacks, double below) {
        return new BossSpec.Phase(
                name,
                List.of(),
                0,
                attacks,
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        Double.isNaN(below) ? List.of() : List.of(MOUTH),
                        below,
                        BossSpec.Slamming.NONE,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    /** The test colossus; {@code attacks}: its rings (and phase 2's fan). */
    static SetPieceSpec colossus(boolean attacks) {
        var mouth = new PartSpec("mouth", 0, 0, new Hitbox(120, 120), MOUTH_HP, true, 264, Optional.empty(), 0, 0.5);
        var ring12 = new BossSpec.Attack("ring", BossSpec.Pattern.RING, RING12, 0, false, 12, 0, 0, 0, List.of(MOUTH));
        var ring14 =
                new BossSpec.Attack("ring-14", BossSpec.Pattern.RING, RING12, 0, false, 14, 0, 0, 0, List.of(MOUTH));
        var fan = new BossSpec.Attack("fan", BossSpec.Pattern.FAN, FAN5, 0, false, 5, 0, 0, 0, List.of(MOUTH));
        var arena = new BossSpec.Arena(
                BossSpec.Lanes.NONE,
                null,
                List.of(Layer.BURIED),
                List.of(new BossSpec.Spot("gullet", MOUTH, 0, 0, new Hitbox(60, 60), 2, true)));
        BossSpec boss = new BossSpec(
                        ARRIVE,
                        PlayField.WIDTH / 2.0,
                        PlayField.HEIGHT / 2.0,
                        0,
                        0,
                        1,
                        Layer.BURIED,
                        false,
                        "TEST COLOSSUS",
                        130,
                        List.of(),
                        List.of(ring12, ring14, fan),
                        List.of(
                                phase("Circling", attacks ? List.of(0) : List.of(), 0.6),
                                phase("Hunting", attacks ? List.of(1, 2) : List.of(), 0.25),
                                phase("Breaching anywhere", attacks ? List.of(1) : List.of(), Double.NaN)),
                        false,
                        List.of(),
                        List.of(),
                        3,
                        Optional.of(arena))
                .withBreach(Optional.of(BREACH))
                .withSuction(Optional.of(SUCTION));
        return new SetPieceSpec(
                "test-colossus",
                new Hitbox(150, 150),
                new Hitbox(120, 120),
                0,
                List.of(mouth),
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    /** The crawling arena: 3 s at 140 px/s, the arena to 23 s at 20 px/s (eased in over 2 s), 20 s at 140 after it. */
    static LevelScript level(boolean attacks, boolean crawl) {
        return LevelScript.builder(21, 3)
                .sections(List.of(
                        new Section(ARRIVE, SPEED),
                        new Section(ARENA_END, CRAWL, true, 0, crawl ? 2 : 0, 0, crawl),
                        new Section(ARENA_END + 20, SPEED)))
                .setPieces(List.of(colossus(attacks)))
                .build();
    }

    static Sortie sortie(boolean attacks, Rules rules) {
        return new Sortie(1, TestSpecs.LOADOUT, level(attacks, true), rules, TestSpecs.FULL_ARMOUR);
    }

    static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    static BreachArena breach(Sortie sortie) {
        return boss(sortie).breach().orElseThrow();
    }

    /** Steps until {@code type} happens (returns its values in that step), at most {@code seconds}. */
    static List<Integer> until(Sortie sortie, SimEvents.Type type, double seconds, int commands) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            List<Integer> values = values(sortie, type);
            if (!values.isEmpty()) {
                return values;
            }
        }
        throw new AssertionError(type + " within " + seconds + " s");
    }

    static List<Integer> values(Sortie sortie, SimEvents.Type type) {
        List<Integer> values = new ArrayList<>();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                values.add(events.value(i));
            }
        }
        return values;
    }

    static void steps(Sortie sortie, double seconds, int commands) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
        }
    }

    /** Steps until the breach's phase is {@code phase}, at most {@code seconds}. */
    static void untilPhase(Sortie sortie, BreachArena.Phase phase, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            if (breach(sortie).phase() == phase) {
                return;
            }
            sortie.step(0);
        }
        throw new AssertionError(phase + " within " + seconds + " s");
    }

    /** Flies the ship to (x, y). */
    static void steerTo(Sortie sortie, double x, double y) {
        for (int i = 0;
                i < 900
                        && (Math.abs(sortie.ship().x() - x) > 2
                                || Math.abs(sortie.ship().y() - y) > 2);
                i++) {
            int commands = 0;
            if (Math.abs(sortie.ship().x() - x) > 2) {
                commands |= (sortie.ship().x() < x ? Command.RIGHT : Command.LEFT).bit();
            }
            if (Math.abs(sortie.ship().y() - y) > 2) {
                commands |= (sortie.ship().y() < y ? Command.UP : Command.DOWN).bit();
            }
            sortie.step(commands);
        }
    }

    private static double health(Sortie sortie) {
        Defences defences = sortie.ship().defences();
        return defences.shield() + defences.armour();
    }

    private static final SimEvents.Type[] CYCLE = {
        SimEvents.Type.BOSS_ARRIVED,
        SimEvents.Type.BREACH_TELL,
        SimEvents.Type.BREACH_RISE,
        SimEvents.Type.BITE_TELL,
        SimEvents.Type.BITE_SNAP,
        SimEvents.Type.BREACH_DIVE
    };

    @Test
    void itArrivesBuriedAndBreachesFrontLeftRightRearOnTheCyclesTimes() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        List<SimEvents.Type> types = new ArrayList<>();
        List<Integer> ticks = new ArrayList<>();
        List<double[]> places = new ArrayList<>();
        List<Integer> poses = new ArrayList<>();
        for (int tick = 0; poses.size() < 6; tick++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                for (SimEvents.Type type : CYCLE) {
                    if (events.type(e) == type) {
                        types.add(type);
                        ticks.add(tick);
                        if (type == SimEvents.Type.BREACH_RISE) {
                            poses.add(events.value(e));
                            places.add(new double[] {events.x(e), events.y(e)});
                        }
                    }
                }
            }
            if (tick < SimStep.ticks(ARRIVE) - 1) {
                assertFalse(boss.present(), "no bar before it arrives");
            }
            assertTrue(tick < SimStep.ticks(120), "six breaches within two minutes");
        }
        assertEquals(SimEvents.Type.BOSS_ARRIVED, types.getFirst());
        assertEquals(
                List.of(
                        BreachArena.Pose.FRONT.ordinal(),
                        BreachArena.Pose.LEFT.ordinal(),
                        BreachArena.Pose.RIGHT.ordinal(),
                        BreachArena.Pose.REAR.ordinal(),
                        BreachArena.Pose.FRONT.ordinal(),
                        BreachArena.Pose.LEFT.ordinal()),
                poses,
                "the fixed order in phase 1, repeating");
        assertEquals(FRONT_X, places.get(0)[0], 1e-9);
        assertEquals(FRONT_Y, places.get(0)[1], 1e-9);
        assertEquals(30, places.get(1)[0], 1e-9);
        assertEquals(SIDE_Y, places.get(1)[1], 1e-9);
        assertEquals(450, places.get(2)[0], 1e-9);
        assertEquals(240, places.get(3)[0], 1e-9);
        assertEquals(REAR_Y, places.get(3)[1], 1e-9);
        // The cycle: arrival, then per breach tell, rise, bite tell, snap + dive (the next tell at the dive's end).
        int arrived = ticks.getFirst();
        assertEquals(SimEvents.Type.BREACH_TELL, types.get(1));
        assertEquals(SimStep.ticks(6), ticks.get(1) - arrived, "the intro: 6 s from the bar to the first tell");
        for (int k = 1; k + 5 < types.size(); k += 5) {
            assertEquals(SimEvents.Type.BREACH_TELL, types.get(k));
            assertEquals(SimEvents.Type.BREACH_RISE, types.get(k + 1));
            assertEquals(SimEvents.Type.BITE_TELL, types.get(k + 2));
            assertEquals(SimEvents.Type.BITE_SNAP, types.get(k + 3));
            assertEquals(SimEvents.Type.BREACH_DIVE, types.get(k + 4));
            assertEquals(SimStep.ticks(2), ticks.get(k + 1) - ticks.get(k), "the tell: 2 s");
            assertEquals(SimStep.ticks(1.5) + SimStep.ticks(7), ticks.get(k + 2) - ticks.get(k + 1), "rise + open");
            assertEquals(SimStep.ticks(0.8), ticks.get(k + 3) - ticks.get(k + 2), "the bite's tell: 0.8 s");
            assertEquals(ticks.get(k + 3), ticks.get(k + 4), "the dive starts at the snap");
            assertEquals(SimStep.ticks(1.5), ticks.get(k + 5) - ticks.get(k + 4), "the next tell at the dive's end");
        }
        assertEquals(6, breach.breaches());
        assertTrue(sortie.arenaHalted(), "the level clock is held at the arena's end while it lives");
    }

    @Test
    void theMouthIsBuriedUntilTheRisesMiddleAndAgainFromTheDivesMiddle() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = boss(sortie);
        until(sortie, SimEvents.Type.BREACH_TELL, 10, 0);
        assertEquals(Layer.BURIED, boss.partLayer(MOUTH));
        assertTrue(boss.partShielded(MOUTH), "nothing touches a buried mouth");
        until(sortie, SimEvents.Type.BREACH_RISE, 3, 0);
        int buried = 0;
        while (boss.partLayer(MOUTH) == Layer.BURIED) {
            sortie.step(0);
            buried++;
        }
        assertEquals((SimStep.ticks(1.5) + 1) / 2, buried, "ground from the rise's middle");
        assertFalse(boss.partShielded(MOUTH));
        assertEquals(Layer.GROUND, boss.partLayer(MOUTH));
        until(sortie, SimEvents.Type.BREACH_DIVE, 10, 0);
        int up = 0;
        while (boss.partLayer(MOUTH) == Layer.GROUND) {
            sortie.step(0);
            up++;
        }
        assertEquals((SimStep.ticks(1.5) + 1) / 2, up, "buried from the dive's middle");
        assertTrue(boss.partShielded(MOUTH));
    }

    @Test
    void theGulletDoublesWhileOpenAndTheLipsHalve() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = boss(sortie);
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        double x = boss.partX(MOUTH);
        double y = boss.partY(MOUTH);
        double hp = boss.partHp(MOUTH);
        boss.damagePartAt(MOUTH, 10, x, y - 100, 0, 1);
        assertEquals(hp - 20, boss.partHp(MOUTH), 1e-9, "a shot through the gullet: ×2");
        boss.damagePartAt(MOUTH, 10, x + 50, y - 100, 0, 1);
        assertEquals(hp - 25, boss.partHp(MOUTH), 1e-9, "a shot on the lips: ×0.5");
        untilPhase(sortie, BreachArena.Phase.BITE, 10);
        boss.damagePartAt(MOUTH, 10, x, y - 100, 0, 1);
        assertEquals(hp - 30, boss.partHp(MOUTH), 1e-9, "the gullet closes for the bite: the lips' ×0.5");
    }

    @Test
    void sideAndRearBreachesAreWarnedThreeSecondsBeforeTheirRiseTheFrontNot() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        int[] warned = new int[BreachArena.Pose.values().length];
        int[] beforeTell = new int[warned.length];
        int rises = 0;
        while (rises < 5) {
            sortie.step(0);
            int bit = sortie.edgeWarnings();
            if (bit != 0) {
                BreachArena.Pose next = breach.nextPose();
                assertTrue(breach.nextFixed(), "the point is fixed while it is warned");
                int expected = next == BreachArena.Pose.LEFT
                        ? WarningEdge.LEFT.bit()
                        : next == BreachArena.Pose.RIGHT
                                ? WarningEdge.RIGHT.bit()
                                : next == BreachArena.Pose.REAR ? WarningEdge.BOTTOM.bit() : 0;
                assertEquals(expected, bit, "the warning for " + next);
                warned[next.ordinal()]++;
                beforeTell[next.ordinal()] += breach.phase() == BreachArena.Phase.TELL ? 0 : 1;
            }
            rises += values(sortie, SimEvents.Type.BREACH_RISE).size();
        }
        assertEquals(0, warned[BreachArena.Pose.FRONT.ordinal()], "a front breach needs no edge warning");
        assertEquals(SimStep.ticks(3), warned[BreachArena.Pose.LEFT.ordinal()], "3 s before the rise");
        assertEquals(SimStep.ticks(3), warned[BreachArena.Pose.RIGHT.ordinal()]);
        assertEquals(SimStep.ticks(3), warned[BreachArena.Pose.REAR.ordinal()]);
        assertEquals(SimStep.ticks(1), beforeTell[BreachArena.Pose.LEFT.ordinal()], "1 s before the bulge");
    }

    @Test
    void aRearBreachIsARearWaveForRook() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        boolean rear = false;
        while (breach.breaches() < 4 || breach.phase() != BreachArena.Phase.OPEN) {
            sortie.step(0);
            boolean expected = breach.pose() == BreachArena.Pose.REAR && breach.phase() != BreachArena.Phase.BURIED;
            assertEquals(expected ? WarningEdge.BOTTOM.bit() : 0, breach.rear());
            rear |= expected;
        }
        assertTrue(rear, "the fourth breach is the rear one");
    }

    @Test
    void phaseTwoRunsRearLeftRearRightFrontFollowingTheShipWithinTheClampsFixedAtTheWarning() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        boss.damagePartFlat(MOUTH, MOUTH_HP * 0.5);
        assertEquals(List.of(1), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0), "below 60 %: phase 2");
        // The ship holds the bottom-left corner against the suction.
        int corner = Command.LEFT.bit() | Command.DOWN.bit();
        List<Integer> poses = new ArrayList<>();
        List<double[]> places = new ArrayList<>();
        boolean moved = false;
        while (poses.size() < 6) {
            if (breach.warning() != 0 && breach.nextPose() == BreachArena.Pose.REAR && !moved) {
                // The rear point is fixed when its warning starts: the ship flying off does not move it.
                double fixed = breach.nextX();
                for (int i = 0; i < SimStep.ticks(0.5); i++) {
                    sortie.step(Command.RIGHT.bit());
                    assertEquals(fixed, breach.nextX(), 1e-9);
                }
                moved = true;
                continue;
            }
            sortie.step(corner);
            List<Integer> rises = values(sortie, SimEvents.Type.BREACH_RISE);
            if (!rises.isEmpty()) {
                poses.add(rises.getFirst());
                places.add(new double[] {breach.mouthX(), breach.mouthY()});
            }
        }
        // The front breach under way when phase 2 began runs out; the next pick starts phase 2's order.
        assertEquals(
                List.of(
                        BreachArena.Pose.REAR.ordinal(),
                        BreachArena.Pose.LEFT.ordinal(),
                        BreachArena.Pose.REAR.ordinal(),
                        BreachArena.Pose.RIGHT.ordinal(),
                        BreachArena.Pose.FRONT.ordinal(),
                        BreachArena.Pose.REAR.ordinal()),
                poses);
        // The ship in the corner: the rear and front points clamp its x to 100, the sides its y to 120 (420 from the
        // top).
        assertEquals(100, places.get(0)[0], 1e-9, "the rear point where the ship was at its warning");
        assertEquals(REAR_Y, places.get(0)[1], 1e-9);
        assertEquals(30, places.get(1)[0], 1e-9);
        assertEquals(PlayField.HEIGHT - 420, places.get(1)[1], 1e-9, "the left point clamps the ship's y");
        assertEquals(100, places.get(2)[0], 1e-9);
        assertEquals(450, places.get(3)[0], 1e-9);
        assertEquals(PlayField.HEIGHT - 420, places.get(3)[1], 1e-9);
        assertEquals(100, places.get(4)[0], 1e-9, "the front point clamps the ship's x");
        assertEquals(FRONT_Y, places.get(4)[1], 1e-9);
        assertTrue(moved);
    }

    @Test
    void phaseThreeBreachesUnderTheShipInsideTheEdges() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = boss(sortie);
        BreachArena breach = breach(sortie);
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        boss.damagePartFlat(MOUTH, MOUTH_HP * 0.8);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        steerTo(sortie, 20, 100);
        int rises = 0;
        while (rises < 2) {
            sortie.step(0);
            if (!values(sortie, SimEvents.Type.BREACH_RISE).isEmpty()) {
                rises++;
            }
        }
        // The first rise ran the breach under way (or phase 3's first pick); the second is under the ship for sure.
        assertEquals(BreachArena.Pose.UNDER, breach.pose());
        assertEquals(BreachArena.UNDER_INSET, breach.mouthX(), 1e-9, "80 px inside the left edge");
        assertEquals(Math.max(BreachArena.UNDER_INSET, sortie.ship().y()), breach.mouthY(), 3);
    }

    @Test
    void theSuctionPullsTheShipTowardTheOpenMouthOnItsProfile() {
        assertEquals(45, SUCTION.speedAt(0), 1e-12);
        assertEquals(45, SUCTION.speedAt(120), 1e-12);
        assertEquals(22.5, SUCTION.speedAt(240), 1e-12);
        assertEquals(0, SUCTION.speedAt(360), 1e-12);
        assertEquals(0, SUCTION.speedAt(500), 1e-12);
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Suction(45, 360, 120));
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        until(sortie, SimEvents.Type.BREACH_TELL, 10, 0);
        // The front mouth at (240, 510): the ship 210 px below it, idle.
        steerTo(sortie, FRONT_X, FRONT_Y - 210);
        steps(sortie, 0.5, 0);
        assertEquals(0, breach.pullX(), 1e-12);
        assertEquals(0, breach.pullY(), 1e-12, "no suction while the mouth is not open");
        untilPhase(sortie, BreachArena.Phase.OPEN, 5);
        sortie.step(0);
        double y = sortie.ship().y();
        double x = sortie.ship().x();
        double distance = Math.hypot(FRONT_X - x, FRONT_Y - y);
        sortie.step(0);
        double expected = SUCTION.speedAt(distance);
        assertEquals(expected * (FRONT_Y - y) / distance, breach.pullY(), 1e-9);
        assertEquals(expected * (FRONT_X - x) / distance, breach.pullX(), 1e-9);
        assertTrue(expected > 20 && expected < 40, "about 28 px/s at 210 px: " + expected);
        double drift = sortie.ship().y() - y;
        assertEquals(expected * (FRONT_Y - y) / distance * SimStep.SECONDS, drift, 1e-6, "the ship drifts toward it");
        // Never past the centre.
        assertEquals(0, breach.pullXAt(FRONT_X, FRONT_Y), 1e-12);
        assertEquals(0.5 / SimStep.SECONDS, breach.pullYAt(FRONT_X, FRONT_Y - 0.5), 1e-9);
        untilPhase(sortie, BreachArena.Phase.DIVE, 10);
        sortie.step(0);
        assertEquals(0, breach.pullY(), 1e-12, "none once it dives");
    }

    @Test
    void theSnapBitesTheShipInsideItsCircleOnce() {
        Sortie sortie = sortie(false, TestSpecs.RULES);
        until(sortie, SimEvents.Type.BREACH_RISE, 12, 0);
        steerTo(sortie, FRONT_X, FRONT_Y - 50);
        untilPhase(sortie, BreachArena.Phase.BITE, 10);
        double before = health(sortie);
        List<Integer> snap = until(sortie, SimEvents.Type.BITE_SNAP, 1, 0);
        assertEquals(List.of(BreachArena.BITE_SHIP), snap, "the ship 50 px from the mouth");
        assertEquals(before - 10, health(sortie), 0.2, "heavy = 10, once");
        double after = health(sortie);
        steps(sortie, 1, 0);
        assertTrue(health(sortie) >= after - 1e-9, "no second bite in the dive");
        // Out of the circle at the next snap: nothing.
        assertEquals(
                List.of(0),
                until(sortie, SimEvents.Type.BITE_SNAP, 15, Command.RIGHT.bit()),
                "the ship far off the left mouth");
    }

    @Test
    void theRingFiresAfterTheGlowAndEveryIntervalTheFanHalfAnIntervalLater() {
        Sortie sortie = sortie(true, TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        List<Integer> rings = new ArrayList<>();
        List<Integer> others = new ArrayList<>();
        int glowing = 0;
        for (int tick = 1; breach.phase() == BreachArena.Phase.OPEN; tick++) {
            sortie.step(0);
            if ((breach.fires() & BreachArena.RING) != 0) {
                rings.add(tick);
            }
            if ((breach.fires() & BreachArena.OTHERS) != 0) {
                others.add(tick);
            }
            glowing += breach.glowing() ? 1 : 0;
        }
        int glow = SimStep.ticks(0.5);
        int interval = SimStep.ticks(2.5);
        assertEquals(List.of(glow, glow + interval, glow + 2 * interval), rings, "three rings in 7 s");
        assertEquals(
                List.of(glow + interval / 2, glow + interval + interval / 2, glow + 2 * interval + interval / 2),
                others);
        assertEquals(3 * glow - 1, glowing, "the glow before each ring");
    }

    @Test
    void theRidgeRunsFromTheLastPointToTheNextAndArrivesAtTheRise() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        BreachArena breach = breach(sortie);
        until(sortie, SimEvents.Type.BREACH_DIVE, 25, 0);
        // The front breach dove; the left point is fixed 3 s before its rise (in the dive) and the ridge runs to it.
        untilPhase(sortie, BreachArena.Phase.TELL, 3);
        int last = breach.ridgeCount() - 1;
        assertEquals(FRONT_X, breach.ridgeX(0), 1e-9);
        assertEquals(FRONT_Y, breach.ridgeY(0), 1e-9);
        assertEquals(30, breach.ridgeX(last), 1e-9);
        assertEquals(SIDE_Y, breach.ridgeY(last), 1e-9);
        double head = breach.ridgeHead();
        assertTrue(head >= 0 && head < 0.1, "the head starts at the last point: " + head);
        while (breach.phase() == BreachArena.Phase.TELL) {
            sortie.step(0);
            assertTrue(breach.ridgeHead() >= head, "the head runs on");
            head = breach.ridgeHead();
        }
        assertEquals(1, breach.ridgeHead(), 1e-12, "at the point at the rise");
        // Its middle bends toward the field's centre.
        double midX = breach.ridgeX(last / 2);
        assertTrue(midX > (FRONT_X + 30) / 2, "bent toward the centre: " + midX);
    }

    @Test
    void aRetryFromTheBossStartsTheBreachesOver() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BREACH_RISE, 12, 0);
        untilPhase(sortie, BreachArena.Phase.OPEN, 3);
        assertTrue(sortie.bossCheckpoint());
        sortie.retryFromBoss();
        sortie.step(0);
        BreachArena breach = breach(sortie);
        assertEquals(0, breach.breaches());
        assertEquals(BreachArena.Phase.BURIED, breach.phase());
        assertEquals(Layer.BURIED, boss(sortie).partLayer(MOUTH));
        int steps = 0;
        while (values(sortie, SimEvents.Type.BREACH_TELL).isEmpty()) {
            sortie.step(0);
            steps++;
        }
        assertTrue(steps > SimStep.ticks(6) - 3 && steps <= SimStep.ticks(6) + 1, "the intro again: " + steps);
        assertEquals(BreachArena.Pose.FRONT, breach.pose(), "from the order's start");
    }

    /** With Rook (on the left) in the escort slot. */
    private static Sortie withRook(boolean attacks) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level(attacks, true),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void theSuctionPullsRookTowardTheOpenMouth() {
        Sortie sortie = withRook(false);
        Wingman rook = sortie.wingman().orElseThrow();
        BreachArena breach = breach(sortie);
        until(sortie, SimEvents.Type.BREACH_TELL, 10, 0);
        steerTo(sortie, FRONT_X, FRONT_Y - 240);
        untilPhase(sortie, BreachArena.Phase.OPEN, 5);
        steps(sortie, 1, 0);
        double expectedX = breach.pullXAt(rook.x(), rook.y());
        double expectedY = breach.pullYAt(rook.x(), rook.y());
        double distance = Math.hypot(breach.mouthX() - rook.x(), breach.mouthY() - rook.y());
        assertEquals(SUCTION.speedAt(distance), Math.hypot(expectedX, expectedY), 1e-9);
        assertTrue(expectedY > 5, "toward the front mouth above him: " + expectedY);
    }

    @Test
    void aBreachingFightRunsTheSameTwiceAndAllocatesNothingPerStep() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = withRook(true);
            for (int i = 0; i < SimStep.ticks(60); i++) {
                sortie.step(Command.FIRE.bit() | ((i / 90) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                if (i == SimStep.ticks(20)) {
                    boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.45);
                }
            }
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1], "deterministic");
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = withRook(true);
                    steps(sortie, 12, 0);
                    boss(sortie).damagePartFlat(MOUTH, MOUTH_HP * 0.45);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(40); i++) {
                        sortie.step(
                                Command.FIRE.bit() | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                    }
                });
        assertEquals(0, allocated, "the breaches, the bite, the suction and the crawl allocate nothing per step");
    }

    @Test
    void aBreachNeedsAnArenaWithoutLanesAndOnlyABreachingBossSucks() {
        BossSpec boss = colossus(false).boss().orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> boss.withBreach(Optional.empty()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpec.Breach(
                        2, 1.5, 7, 0.8, 1.5, 2, 3, List.of(), BREACH.clamps(), BREACH.orders(), 70, 10, 6));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Clamp(380, 100));
        assertThrows(IllegalArgumentException.class, () -> new BossSpec.Order(List.of(), false));
    }
}
