package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M7 part E, step E3: the Ghost Drone's lane strafe and pass trigger (design/enemies/air/ghost-drone), on
 * hand-built specs at the stat block's medium numbers: down at 180 px/s to its lane (120–220 px below the top, a
 * wave's lanes 40 px apart), a 90° bank, across at 150 px/s toward the far side; one 3-shot burst per pass when it
 * is within 60 px of the ship's x, 0.3 s after its eye glint, the shots 0.15 s apart and each re-aimed; hard's
 * second pass; the EMP Burst stuns the machine 3 s.
 */
class GhostDroneTest {
    static final EnemySpec.LaneStrafe STRAFE = new EnemySpec.LaneStrafe(180, 150, new Range(120, 220), 40, false);
    static final EnemySpec.LaneStrafe HARD = new EnemySpec.LaneStrafe(180, 150, new Range(120, 220), 40, true);

    static EnemyGun gun(boolean reaim) {
        return EnemyGun.aimed(TestSpecs.INFINITE, 0, 3, 210, 4, false).withPassTrigger(60, 0.3, reaim);
    }

    /** A Ghost Drone: 14 HP, a 28×24 hit box, a machine, its lane strafe and its gun. */
    static EnemySpec drone(EnemySpec.LaneStrafe strafe, Optional<EnemyGun> gun) {
        return new EnemySpec(
                        "ghost-drone",
                        14,
                        new Hitbox(28, 24),
                        Layer.AIR,
                        0,
                        false,
                        14,
                        180,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        gun,
                        Optional.empty(),
                        Optional.empty(),
                        false)
                .withMachine(true)
                .withLaneStrafe(Optional.of(strafe));
    }

    static EnemySpec drone() {
        return drone(STRAFE, Optional.of(gun(true)));
    }

    static WaveSpec wave(WaveSpec.Formation formation, EnemySpec enemy, int count, WaveSpec.Edge edge) {
        WaveSpec.Entry entry = formation == WaveSpec.Formation.PINCER ? WaveSpec.Entry.SIDES : WaveSpec.Entry.FRONT;
        return TestSpecs.wave(0, formation, enemy, count, entry, edge);
    }

    static Sortie sortie(Loadout loadout, WaveSpec... waves) {
        return new Sortie(
                1,
                loadout,
                TestSpecs.level(40, List.of(waves)),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    static Sortie sortie(WaveSpec... waves) {
        return sortie(TestSpecs.LOADOUT, waves);
    }

    /** One step's look at a unit. */
    record Seen(int step, double x, double y, Enemy.StrafePhase phase, int side) {}

    /** The units in play after the first step, by their start x. */
    static List<Enemy> units(Sortie sortie) {
        sortie.step(Command.NONE);
        List<Enemy> units = new ArrayList<>();
        for (int k = 0; k < sortie.enemyCount(); k++) {
            units.add(sortie.enemy(k));
        }
        units.sort((a, b) -> Double.compare(a.x(), b.x()));
        return units;
    }

    static boolean inPlay(Sortie sortie, Enemy unit) {
        for (int k = 0; k < sortie.enemyCount(); k++) {
            if (sortie.enemy(k) == unit) {
                return true;
            }
        }
        return false;
    }

    @Test
    void itsNumbersHoldAndOnlyAPlainAirUnitWithAPassTriggerStrafesLanes() {
        assertThrows(IllegalArgumentException.class, () -> PartASpecs.dummy(Layer.GROUND, 14)
                .withLaneStrafe(Optional.of(STRAFE)));
        assertThrows(
                IllegalArgumentException.class,
                () -> drone(STRAFE, Optional.of(EnemyGun.aimed(1, 0, 3, 210, 4, false))),
                "a lane strafer's gun fires by its pass trigger");
        assertThrows(IllegalArgumentException.class, () -> gun(true).withPassTrigger(-1, 0.3, true));
        assertThrows(IllegalArgumentException.class, () -> gun(true).withPassTrigger(60, -0.1, true));
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.LaneStrafe(0, 150, new Range(120, 220), 40, false));
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.LaneStrafe(180, 150, new Range(0, 220), 40, false));
        EnemyGun plain = EnemyGun.aimed(1, 0, 3, 210, 4, false);
        assertTrue(Double.isNaN(plain.passWithinX()) && !plain.passTrigger() && !plain.reaim());
        assertEquals(0, plain.tellSeconds());
        EnemyGun pass = gun(true);
        assertTrue(pass.passTrigger() && pass.reaim());
        assertEquals(60, pass.passWithinX());
        assertEquals(0.3, pass.tellSeconds());
        assertEquals(pass, pass.withSpiral(Optional.empty()), "the spiral's copy keeps the trigger");
        assertEquals(Optional.of(STRAFE), drone().withVortex(Optional.empty()).laneStrafe(), "every copy keeps it");
        assertEquals(Enemy.StrafePhase.DESCENT, new Enemy().strafePhase(), "any other unit reads its descent");
        assertFalse(new Enemy().laneStrafer());
        // The wave's lanes: 40 px apart from the band's top, closer when the band does not hold them.
        assertEquals(120, STRAFE.depth(0, 3), 1e-9);
        assertEquals(160, STRAFE.depth(1, 3), 1e-9);
        assertEquals(200, STRAFE.depth(2, 3), 1e-9);
        assertEquals(220, STRAFE.depth(4, 5), 1e-9);
        assertEquals(145, STRAFE.depth(1, 5), 1e-9);
        assertEquals(120, STRAFE.depth(0, 1), 1e-9);
    }

    @Test
    void aLineAbreastDropsToItsLanesBanksAndStrafesAcrossSplittingHalfAndHalf() {
        Sortie sortie =
                sortie(wave(WaveSpec.Formation.LINE_ABREAST, drone(STRAFE, Optional.empty()), 4, WaveSpec.Edge.NONE));
        List<Enemy> units = units(sortie);
        assertEquals(4, units.size());
        List<List<Seen>> seen = new ArrayList<>();
        for (int k = 0; k < 4; k++) {
            seen.add(new ArrayList<>());
        }
        for (int i = 1; i <= SimStep.ticks(6) && sortie.enemyCount() > 0; i++) {
            for (int k = 0; k < 4; k++) {
                Enemy unit = units.get(k);
                if (inPlay(sortie, unit)) {
                    seen.get(k).add(new Seen(i, unit.x(), unit.y(), unit.strafePhase(), unit.strafeSide()));
                }
            }
            sortie.step(Command.NONE);
        }
        assertEquals(0, sortie.enemyCount(), "all four left through their far edges");
        double descent = 180 * SimStep.SECONDS;
        double across = 150 * SimStep.SECONDS;
        for (int k = 0; k < 4; k++) {
            List<Seen> track = seen.get(k);
            double startX = (k + 1) * PlayField.WIDTH / 5.0;
            int side = k < 2 ? 1 : -1;
            double laneY = PlayField.HEIGHT - STRAFE.depth(k, 4);
            assertEquals(startX, track.getFirst().x(), 1e-9, "unit " + k + " enters evenly across");
            List<Enemy.StrafePhase> order = new ArrayList<>();
            for (int s = 1; s < track.size(); s++) {
                Seen before = track.get(s - 1);
                Seen now = track.get(s);
                if (order.isEmpty() || order.getLast() != now.phase()) {
                    order.add(now.phase());
                }
                if (before.phase() == Enemy.StrafePhase.DESCENT && now.phase() == Enemy.StrafePhase.DESCENT) {
                    assertEquals(startX, now.x(), 1e-9, "straight down");
                    assertEquals(-descent, now.y() - before.y(), 1e-9, "at 180 px/s");
                }
                if (before.phase() == Enemy.StrafePhase.STRAFE && now.phase() == Enemy.StrafePhase.STRAFE) {
                    assertEquals(laneY, now.y(), 1e-9, "unit " + k + " strafes on its lane");
                    assertEquals(side * across, now.x() - before.x(), 1e-9, "at 150 px/s toward the far side");
                    assertEquals(side, now.side());
                }
            }
            assertEquals(
                    List.of(Enemy.StrafePhase.DESCENT, Enemy.StrafePhase.BANK, Enemy.StrafePhase.STRAFE),
                    order,
                    "unit " + k);
            Seen last = track.getLast();
            assertTrue(side > 0 ? last.x() > PlayField.WIDTH : last.x() < 0, "unit " + k + " leaves at " + last.x());
        }
    }

    @Test
    void aPincerStrafesInFromBothSidesAtItsLanesAndNeverTurnsBack() {
        Sortie sortie = sortie(wave(WaveSpec.Formation.PINCER, drone(HARD, Optional.empty()), 4, WaveSpec.Edge.NONE));
        List<Enemy> units = units(sortie);
        assertEquals(4, units.size());
        List<Double> lanes = new ArrayList<>();
        for (Enemy unit : units) {
            assertEquals(Enemy.StrafePhase.STRAFE, unit.strafePhase(), "straight in at its lane");
            assertEquals(unit.x() < 0 ? 1 : -1, unit.strafeSide(), "in from its edge");
            lanes.add(unit.y());
        }
        assertTrue(units.get(0).x() < 0 && units.get(1).x() < 0, "2 from the left");
        assertTrue(units.get(2).x() > PlayField.WIDTH && units.get(3).x() > PlayField.WIDTH, "2 from the right");
        Collections.sort(lanes);
        for (int k = 0; k < 4; k++) {
            assertEquals(PlayField.HEIGHT - STRAFE.depth(3 - k, 4), lanes.get(k), 1e-9, "four lanes in the band");
        }
        for (int i = 0; i < SimStep.ticks(5) && sortie.enemyCount() > 0; i++) {
            sortie.step(Command.NONE);
            for (int k = 0; k < sortie.enemyCount(); k++) {
                assertEquals(
                        Enemy.StrafePhase.STRAFE, sortie.enemy(k).strafePhase(), "a side entry has no second pass");
            }
        }
        assertEquals(0, sortie.enemyCount(), "gone through the opposite edges");
    }

    /** The steps of a sortie's eye glints and shots, with where the unit was at each glint. */
    record Fire(List<Integer> glints, List<Integer> shots, List<Double> glintX, List<double[]> aims) {}

    /**
     * Steps the sortie until it is empty (at most 8 s), holding {@code commands} from the first glint on,
     * recording the glints and shots.
     */
    static Fire fire(Sortie sortie, int commands) {
        Fire fire = new Fire(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Set<EnemyBullet> old = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 1; i <= SimStep.ticks(8) && (i == 1 || sortie.enemyCount() > 0); i++) {
            sortie.step(fire.glints().isEmpty() ? Command.NONE : commands);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.EYE_GLINT) {
                    fire.glints().add(i);
                    fire.glintX().add(events.x(e));
                } else if (events.type(e) == SimEvents.Type.ENEMY_FIRED) {
                    fire.shots().add(i);
                    for (int b = 0; b < sortie.bulletCount(); b++) {
                        EnemyBullet bullet = sortie.bullet(b);
                        if (!old.contains(bullet)) {
                            // Its heading and the heading from where it was fired to the ship.
                            double toShip = StrictMath.atan2(
                                    sortie.ship().y() - events.y(e),
                                    sortie.ship().x() - events.x(e));
                            fire.aims().add(new double[] {StrictMath.atan2(bullet.vy(), bullet.vx()), toShip});
                        }
                    }
                }
            }
            old.clear();
            for (int b = 0; b < sortie.bulletCount(); b++) {
                old.add(sortie.bullet(b));
            }
        }
        return fire;
    }

    @Test
    void oneBurstAPassWithinSixtyPxOfTheShipsXAfterAGlintOf03Seconds() {
        // From the left third it strafes to the right, across the ship at x = 240.
        Sortie sortie = sortie(wave(WaveSpec.Formation.SINGLE, drone(), 1, WaveSpec.Edge.LEFT));
        Fire fire = fire(sortie, Command.NONE);
        assertEquals(1, fire.glints().size(), "one glint per pass: " + fire.glints());
        double x = fire.glintX().getFirst();
        assertTrue(Math.abs(x - 240) <= 60 && Math.abs(x - 2.5 - 240) > 60, "the first step within 60 px: " + x);
        int glint = fire.glints().getFirst();
        int tell = SimStep.ticks(0.3);
        int gap = SimStep.ticks(0.15);
        assertEquals(List.of(glint + tell, glint + tell + gap, glint + tell + 2 * gap), fire.shots());
        assertEquals(3, fire.aims().size(), "one bullet a shot");
    }

    @Test
    void eachShotIsReaimedAtTheShipAndANotReaimedBurstKeepsItsFirstLine() {
        // The ship flies left while the drone strafes right: the line to it turns between the shots.
        Fire reaimed =
                fire(sortie(wave(WaveSpec.Formation.SINGLE, drone(), 1, WaveSpec.Edge.LEFT)), Command.LEFT.bit());
        assertEquals(3, reaimed.aims().size());
        for (double[] aim : reaimed.aims()) {
            assertEquals(aim[1], aim[0], 0.03, "at the ship");
        }
        assertTrue(Math.abs(reaimed.aims().get(2)[0] - reaimed.aims().get(0)[0]) > 0.2, "the shots turn with it");

        EnemySpec locked = drone(STRAFE, Optional.of(gun(false)));
        Fire kept = fire(sortie(wave(WaveSpec.Formation.SINGLE, locked, 1, WaveSpec.Edge.LEFT)), Command.LEFT.bit());
        assertEquals(3, kept.aims().size());
        assertEquals(kept.aims().get(0)[1], kept.aims().get(0)[0], 0.03, "the first at the ship");
        assertEquals(kept.aims().get(0)[0], kept.aims().get(2)[0], 1e-9, "the others along its line");
    }

    @Test
    void onHardItTurnsAtTheFarEdgeAndStrafesBackOnceOnTheLaneBelowFiringAgain() {
        Sortie sortie =
                sortie(wave(WaveSpec.Formation.SINGLE, drone(HARD, Optional.of(gun(true))), 1, WaveSpec.Edge.LEFT));
        Enemy unit = units(sortie).getFirst();
        List<Enemy.StrafePhase> order = new ArrayList<>();
        int glints = 0;
        int shots = 0;
        double lastX = unit.x();
        double prevX = unit.x();
        int returnSide = 0;
        double returnY = Double.NaN;
        for (int i = 0; i < SimStep.ticks(10) && sortie.enemyCount() > 0; i++) {
            prevX = unit.x();
            sortie.step(Command.NONE);
            glints += sortie.events().count(SimEvents.Type.EYE_GLINT);
            shots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            if (sortie.enemyCount() == 0) {
                break;
            }
            lastX = unit.x();
            Enemy.StrafePhase phase = unit.strafePhase();
            if (order.isEmpty() || order.getLast() != phase) {
                order.add(phase);
            }
            if (phase == Enemy.StrafePhase.RETURN) {
                assertTrue(
                        unit.x() <= PlayField.WIDTH + 150 * SimStep.SECONDS, "it turns at the far edge: " + unit.x());
                returnSide = unit.strafeSide();
                if (unit.x() - prevX < 0 && unit.x() < 300) {
                    returnY = unit.y();
                }
            }
        }
        assertEquals(
                List.of(
                        Enemy.StrafePhase.DESCENT,
                        Enemy.StrafePhase.BANK,
                        Enemy.StrafePhase.STRAFE,
                        Enemy.StrafePhase.RETURN),
                order);
        assertEquals(-1, returnSide, "back to the left");
        assertEquals(PlayField.HEIGHT - 120 - 2 * EnemySpec.LaneStrafe.RETURN_RADIUS, returnY, 1e-9, "the lane below");
        assertEquals(2, glints, "a glint each pass");
        assertEquals(6, shots, "a burst each pass");
        assertTrue(lastX < 30, "it leaves through the edge it came from: " + lastX);
    }

    @Test
    void theEmpStunsTheMachine3SItDriftsOnItsLineAndFiresNothing() {
        Loadout loadout = TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.emp(1));
        Sortie sortie = sortie(loadout, wave(WaveSpec.Formation.SINGLE, drone(), 1, WaveSpec.Edge.LEFT));
        Enemy unit = units(sortie).getFirst();
        // The ship off to the right, so the drone has not reached it yet when the burst stuns it.
        while (unit.strafePhase() != Enemy.StrafePhase.STRAFE) {
            sortie.step(Command.RIGHT.bit());
        }
        sortie.step(Command.SPECIAL.bit());
        int steps = 0;
        while (!unit.empStunned() && steps++ < 60) {
            assertEquals(0, sortie.events().count(SimEvents.Type.EYE_GLINT));
            sortie.step(Command.NONE);
        }
        assertTrue(unit.empStunned(), "stunned");
        assertEquals(SimStep.ticks(3), unit.empStunTicks(), "a machine: 3 s");
        double y = unit.y();
        double x = unit.x();
        while (unit.empStunned() && inPlay(sortie, unit)) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.EYE_GLINT), "no glint");
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_FIRED), "no shot");
            if (inPlay(sortie, unit)) {
                assertEquals(y, unit.y(), 1e-9, "on its line");
                assertEquals(150 * SimStep.SECONDS, unit.x() - x, 1e-9, "with its momentum");
                x = unit.x();
            }
        }
    }

    @Test
    void aStunInItsTellHoldsTheBurstWhichFiresWhenTheStunEnds() {
        Sortie sortie = sortie(wave(WaveSpec.Formation.SINGLE, drone(), 1, WaveSpec.Edge.LEFT));
        Enemy unit = units(sortie).getFirst();
        int glint = 0;
        for (int i = 1; i < SimStep.ticks(5) && glint == 0; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.EYE_GLINT) > 0) {
                glint = i;
            }
        }
        assertTrue(glint > 0);
        assertTrue(unit.eyeGlinting(), "its eye glints through its tell");
        sortie.step(Command.NONE);
        unit.empStun(30);
        int shot = 0;
        for (int i = 1; i <= 60 && shot == 0; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                shot = i;
            }
        }
        // 1 tell step ran before the stun: the other 17 run after its 30 steps.
        assertEquals(30 + SimStep.ticks(0.3) - 1, shot, "the tell waits out the stun");
    }

    @Test
    void lineAbreastsPincersAndSecondPassesDoNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(
                        wave(
                                WaveSpec.Formation.LINE_ABREAST,
                                drone(HARD, Optional.of(gun(true))),
                                4,
                                WaveSpec.Edge.NONE),
                        wave(WaveSpec.Formation.PINCER, drone(STRAFE, Optional.of(gun(false))), 4, WaveSpec.Edge.NONE)),
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(6); i++) {
                        sortie.step(SortieTest.Pilot.commands(i));
                    }
                });
        assertEquals(0, allocated, "6 s allocated " + allocated + " bytes");
    }
}
