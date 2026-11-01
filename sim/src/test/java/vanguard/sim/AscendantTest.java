package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant; m10h-gaps.md §1 rows 1, 2, 5, 6 and 13's phase-1 subjects), on
 * the test Ascendant ({@link Act6HSpecs}): the bow slides in bow-first over 6 s, untouchable; it is a wedge, not a box
 * (contact on its faces and under its tip, none in the side pockets; shots spark on it; a beam stops on its edge); its
 * contact deals 25 once a second and the ship cannot fly into it; the bar counts both phases from the arrival; phase 1
 * ends on its seven parts in any order, the cannon not vital; the batteries burst on their clocks only while their
 * panels face the ship, never more than two at once and 0.6 s apart; the cannon's playbook draws with the seeded random,
 * never the same move twice running, the Darts at most every third draw, rests 1.5 s (0.8 s below half), its lance one
 * told line locked 0.2 s before it fires, its salvo from the living racks only; the phase-1 radio subjects come once.
 */
class AscendantTest {
    private static final double EPS = 1e-9;
    private static final int FIRE = Command.FIRE.bit();
    private static final Hull HULL = TestSpecs.SHIP.hull();

    @Test
    void theBowSlidesInOverSixSecondsUntouchableUntilInPlace() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.QUIET);
        SetPiece boss = rig.piece;
        while (!boss.present()) {
            rig.step();
        }
        int arrived = rig.tick;
        double top = boss.partY(Act6HSpecs.CANNON);
        assertTrue(top > PlayField.HEIGHT, "the bow tip still above the top edge: " + top);
        while (!boss.engaged()) {
            for (int p = 0; p < boss.partCount(); p++) {
                assertTrue(boss.partShielded(p), "untouchable until in place: part " + p);
            }
            rig.step();
            assertTrue(rig.tick - arrived <= SimStep.ticks(6) + 1, "in place within 6 s");
        }
        assertEquals(SimStep.ticks(6), rig.tick - arrived, 1, "6 s");
        assertEquals(Act6HSpecs.Y, boss.y(), EPS);
        for (int p : Act6HSpecs.PHASE_1) {
            assertFalse(boss.partShielded(p), "phase 1's parts take hits: " + p);
        }
        for (int p = Act6HSpecs.SAC_1; p <= Act6HSpecs.HEART; p++) {
            assertTrue(boss.partShielded(p), "phase 2's parts not yet: " + p);
        }
    }

    @Test
    void theBowIsAWedgeWithOpenSidePockets() {
        SetPiece boss = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled().piece;
        assertTrue(boss.wedged());
        // On the left face (the screen's (120, 100)) and under the tip's edge (240, 255).
        assertTrue(boss.bodyTouchesHull(HULL, 120, PlayField.HEIGHT - 100));
        assertTrue(boss.bodyTouchesHull(HULL, 240, PlayField.HEIGHT - 260));
        // In the side pockets below y 150: the screen's (60, 200) and (420, 230), and well under the tip.
        assertFalse(boss.bodyTouchesHull(HULL, 60, PlayField.HEIGHT - 200));
        assertFalse(boss.bodyTouchesHull(HULL, 420, PlayField.HEIGHT - 230));
        assertFalse(boss.bodyTouchesHull(HULL, 240, PlayField.HEIGHT - 320));
        Hitbox bolt = new Hitbox(4, 12);
        assertTrue(boss.bodyTouches(bolt, 200, PlayField.HEIGHT - 150), "a shot on the face sparks");
        assertFalse(boss.bodyTouches(bolt, 100, PlayField.HEIGHT - 150), "a shot in the pocket flies on");
        // A beam stops on the wedge's lowest point in its column: the tip; at x 60 near the top edge.
        assertEquals(Act6HSpecs.Y - 125, boss.bodyColumn(240, 4, 0), EPS);
        assertEquals(PlayField.HEIGHT - 40, boss.bodyColumn(60, 4, 0), EPS, "x 60: by the top edge");
        assertEquals(Double.POSITIVE_INFINITY, boss.bodyColumn(20, 4, 0), "x 20: past the hull");
    }

    @Test
    void itsContactDealsTwentyFiveOnceASecondAndTheShipCannotFlyIntoIt() {
        Sortie sortie =
                Act6HSpecs.sortie(Act6HSpecs.QUIET, Act5ESpecs.loadout(Act5ESpecs.bolt(1, false)), TestSpecs.RULES);
        SetPiece boss = Act6HSpecs.settle(sortie, 0);
        double tip = Act6HSpecs.Y - 125;
        double highest = 0;
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(4.5); i++) {
            // Up into the bow under its tip, then up along the left face (out to the field's edge, past the hull).
            boolean tipOnly = i < SimStep.ticks(3);
            sortie.step(Command.UP.bit() | (tipOnly ? 0 : Command.LEFT.bit()));
            assertFalse(
                    boss.bodyTouchesHull(HULL, sortie.ship().x(), sortie.ship().y()), "never inside the hull");
            if (tipOnly) {
                highest = Math.max(highest, sortie.ship().y());
            }
            if (sortie.events().count(SimEvents.Type.ARMOUR_HIT) > 0) {
                hits.add(i);
            }
        }
        assertTrue(hits.size() >= 3 && hits.size() <= 5, "once a second: " + hits);
        for (int h = 1; h < hits.size(); h++) {
            assertTrue(hits.get(h) - hits.get(h - 1) >= SimStep.ticks(1), "at most once a second: " + hits);
        }
        assertTrue(highest < tip, "kept under the tip: " + highest);
    }

    @Test
    void theBarCountsBothPhasesFromTheArrival() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
        SetPiece boss = rig.piece;
        assertEquals(1, boss.barShare(), EPS);
        rig.kill(Act6HSpecs.BAT_1);
        assertEquals(1 - 1100 / Act6HSpecs.TOTAL_HP, boss.barShare(), EPS, "15,200 in all");
        for (int p : Act6HSpecs.PHASE_1) {
            rig.kill(p);
        }
        rig.step();
        assertEquals(1, boss.phase());
        assertEquals(6400 / Act6HSpecs.TOTAL_HP, boss.barShare(), EPS, "no jump at the eruption");
    }

    @Test
    void phaseOneEndsOnItsSevenPartsInAnyOrderTheCannonNotVital() {
        List<List<Integer>> orders = List.of(
                Act6HSpecs.PHASE_1,
                List.of(6, 5, 4, 3, 2, 1, 0),
                List.of(Act6HSpecs.GEN_L, Act6HSpecs.BAT_3, Act6HSpecs.GEN_R, 0, 4, 3, 6));
        for (List<Integer> order : orders) {
            Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
            SetPiece boss = rig.piece;
            for (int i = 0; i < order.size(); i++) {
                rig.kill(order.get(i));
                rig.step();
                assertFalse(boss.destroyed(), "no part of phase 1 ends the boss: " + order);
                assertEquals(i < order.size() - 1 ? 0 : 1, boss.phase(), order + " after " + (i + 1));
            }
        }
    }

    @Test
    void withTheCannonDeadFirstNoPlaybookRunsAndTheBatteriesFireOn() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        // On the left: the left face's panels can turn to it (under the bow's centre none faces the ship within 30°).
        rig.shipX = 120;
        rig.kill(Act6HSpecs.CANNON);
        int plays = 0;
        int tells = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            rig.step();
            plays += rig.values(SimEvents.Type.BOSS_PLAY_TELL).size();
            tells += rig.values(SimEvents.Type.BATTERY_TELL).size();
        }
        assertEquals(0, plays);
        assertEquals(-1, rig.piece.playbook().orElseThrow().source());
        assertTrue(tells >= 4, "the batteries burst on: " + tells);
    }

    @Test
    void theBatteriesBurstOnlyFacingTheShipNeverThreeAtOnceTheirTellsApart() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        SetPiece boss = rig.piece;
        BatteryBursts bursts = boss.bursts().orElseThrow();
        PartMirrors mirrors = boss.mirrors().orElseThrow();
        int last = Integer.MIN_VALUE / 2;
        int[] told = new int[boss.partCount()];
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            // The ship roams the bottom of the field.
            rig.shipX = 240 + 200 * Math.sin(i * 2 * Math.PI / SimStep.ticks(13));
            rig.step();
            assertTrue(bursts.busy() <= 2, "never three at once");
            for (int p : rig.values(SimEvents.Type.BATTERY_TELL)) {
                assertTrue(rig.tick - last >= SimStep.ticks(0.6), "0.6 s apart");
                assertTrue(mirrors.aimed(p, rig.shipX, rig.shipY), "its panel faces the ship");
                last = rig.tick;
                told[p]++;
            }
            shots += rig.shots.size();
        }
        for (int p : Act6HSpecs.BATTERIES) {
            assertTrue(told[p] >= 3, "each battery bursts: " + p + " " + told[p]);
        }
        assertTrue(shots >= 3 * 12, "bursts of 3: " + shots);

        // The ship far right and low: the left face's panels cannot turn to it (45° ± 20°): only the right face bursts.
        Act6HSpecs.Rig right = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        right.shipX = 470;
        right.shipY = 20;
        List<Integer> tells = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(15); i++) {
            right.step();
            tells.addAll(right.values(SimEvents.Type.BATTERY_TELL));
        }
        assertFalse(tells.contains(Act6HSpecs.BAT_1) || tells.contains(Act6HSpecs.BAT_2), "left face silent: " + tells);
        assertTrue(tells.contains(Act6HSpecs.BAT_3) && tells.contains(Act6HSpecs.BAT_4), "right face bursts: " + tells);
    }

    @Test
    void aBurstIsToldHalfASecondThenThreeShots() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        rig.shipX = 120;
        rig.kill(Act6HSpecs.CANNON);
        int tellAt = -1;
        List<Integer> shotTicks = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(5) && shotTicks.size() < 3; i++) {
            rig.step();
            if (tellAt < 0 && !rig.values(SimEvents.Type.BATTERY_TELL).isEmpty()) {
                tellAt = rig.tick;
            }
            if (tellAt >= 0) {
                for (int s = 0; s < rig.shots.size(); s++) {
                    shotTicks.add(rig.tick);
                }
            }
        }
        assertTrue(tellAt >= 0);
        assertEquals(3, shotTicks.size(), "3 shots");
        assertEquals(SimStep.ticks(0.5), shotTicks.getFirst() - tellAt, 1, "after the 0.5 s tell");
        assertEquals(SimStep.ticks(0.3), shotTicks.getLast() - shotTicks.getFirst(), 1, "0.15 s apart");
    }

    /** The playbook's tells over {@code seconds} with the ship at the bottom centre. */
    private static List<Integer> plays(Act6HSpecs.Rig rig, double seconds) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            rig.step();
            moves.addAll(rig.values(SimEvents.Type.BOSS_PLAY_TELL));
        }
        return moves;
    }

    @Test
    void theCannonDrawsSeededNeverTheSameTwiceTheDartsAtMostEveryThirdDraw() {
        List<Integer> moves = plays(new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled(), 150);
        assertTrue(moves.size() >= 30, "draws: " + moves.size());
        int darts = Integer.MIN_VALUE / 2;
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) {
                assertNotEquals(moves.get(i - 1), moves.get(i), "never twice running: " + moves);
            }
            if (moves.get(i) == Act6HSpecs.MOVE_DARTS) {
                assertTrue(i - darts >= 3, "the Darts at most every third draw: " + moves);
                darts = i;
            }
        }
        for (int m = 0; m < 4; m++) {
            assertTrue(moves.contains(m), "every move drawn: " + m + " " + moves);
        }
        assertEquals(moves, plays(new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled(), 150), "seeded: the same again");
    }

    @Test
    void itRestsOneAndAHalfSecondsThenPointEightBelowHalfTheCannonsHp() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        assertEquals(SimStep.ticks(1.5), restBeforeNextTell(rig, book), 1, "1.5 s, the first rest too");
        assertEquals(SimStep.ticks(1.5), restBeforeNextTell(rig, book), 1);
        rig.kill(Act6HSpecs.GEN_L);
        rig.kill(Act6HSpecs.GEN_R);
        rig.step();
        rig.piece.damagePartFlat(Act6HSpecs.CANNON, 1500);
        assertTrue(rig.piece.partHp(Act6HSpecs.CANNON) < 1400);
        restBeforeNextTell(rig, book);
        assertEquals(SimStep.ticks(0.8), restBeforeNextTell(rig, book), 1, "0.8 s below half");
    }

    /** Runs to the end of the current move, then counts the steps of the rest up to the next tell. */
    private static int restBeforeNextTell(Act6HSpecs.Rig rig, BossPlaybook book) {
        for (int i = 0; i < SimStep.ticks(20) && book.mode() != BossPlaybook.Mode.REST; i++) {
            rig.step();
        }
        int rest = 0;
        while (rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty()) {
            rig.step();
            rest++;
            assertTrue(rest < SimStep.ticks(5), "a tell comes");
        }
        return rest;
    }

    @Test
    void theSpinalLanceIsOneToldLineLockedBeforeItFiresItsBandTwentyFourPx() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        SetPiece boss = rig.piece;
        BossLines lines = boss.lines().orElseThrow();
        int lances = 0;
        for (int i = 0; i < SimStep.ticks(120) && lances < 4; i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_LANCE)) {
                continue;
            }
            lances++;
            // The line telegraphs 1.2 s, tracking the ship, locked its last 0.2 s; then a 24 px band 0.5 s.
            rig.step();
            assertTrue(lines.telegraphing());
            assertEquals(Act6HSpecs.CANNON, lines.part());
            assertEquals(24, lines.bandPx(), EPS);
            rig.shipX = 100;
            rig.steps(0.5);
            double tracked = lines.dirX();
            assertTrue(tracked < 0, "it tracks the ship: " + tracked);
            rig.steps(0.55);
            rig.shipX = 400;
            rig.steps(0.1);
            assertEquals(tracked < 0, lines.dirX() < 0, "locked 0.2 s before it fires");
            rig.steps(0.1);
            assertTrue(lines.live(), "live after 1.2 s");
            rig.shipX = Act6HSpecs.X;
        }
        assertTrue(lances >= 2, "the lance drawn: " + lances);
    }

    @Test
    void theSalvoFiresFromTheLivingRacksOnlyThreeEach() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        SetPiece boss = rig.piece;
        rig.kill(Act6HSpecs.GEN_L);
        int salvos = 0;
        for (int i = 0; i < SimStep.ticks(150) && salvos < 2; i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_SALVO)) {
                continue;
            }
            salvos++;
            List<double[]> missiles = new ArrayList<>(rig.missiles);
            for (int s = 0; s < SimStep.ticks(1); s++) {
                rig.step();
                missiles.addAll(rig.missiles);
            }
            assertEquals(3, missiles.size(), "3 from the living rack");
            for (double[] m : missiles) {
                assertEquals(boss.partX(Act6HSpecs.GEN_R), m[0], EPS, "from the right rack");
            }
        }
        assertEquals(2, salvos);
        // With both generators dead the salvo is never drawn.
        rig.kill(Act6HSpecs.GEN_R);
        assertFalse(plays(rig, 60).contains(Act6HSpecs.MOVE_SALVO));
    }

    @Test
    void theDartStreamLaunchesFourFromEachBayDownTheScreen() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        for (int i = 0; i < SimStep.ticks(150); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_DARTS)) {
                continue;
            }
            List<double[]> launches = new ArrayList<>(rig.launches);
            for (int s = 0; s < SimStep.ticks(2); s++) {
                rig.step();
                launches.addAll(rig.launches);
            }
            assertEquals(8, launches.size(), "4 a bay");
            for (double[] l : launches) {
                assertTrue(Math.abs(l[2] + Math.PI / 2) <= Math.toRadians(15) + EPS, "down the screen ±15°");
            }
            return;
        }
        throw new AssertionError("the Darts drawn");
    }

    @Test
    void thePhaseOneSubjectsComeOnceTheFirstGeneratorOrTwentyFiveSecondsAndTheCannonBelowHalf() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
        rig.steps(2);
        rig.kill(Act6HSpecs.GEN_R);
        rig.step();
        assertEquals(List.of("first-generator"), rig.cues);
        rig.kill(Act6HSpecs.GEN_L);
        rig.steps(30);
        assertEquals(List.of("first-generator"), rig.cues, "once");
        rig.piece.damagePartFlat(Act6HSpecs.CANNON, 1500);
        rig.step();
        assertEquals(List.of("first-generator", "cannon-half"), rig.cues);

        Act6HSpecs.Rig timed = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
        int start = timed.tick;
        while (timed.cues.isEmpty()) {
            timed.step();
            assertTrue(timed.tick - start <= SimStep.ticks(26), "within 25 s");
        }
        assertEquals(SimStep.ticks(25), timed.tick - start, 2, "25 s into phase 1");
        assertEquals(List.of("first-generator"), timed.cues);
    }

    // --- step H1b: the eruption and phase 2 (m10h-gaps.md §1 rows 7–9 and 13) ---------------------------------------

    @Test
    void theEruptionIsFourSecondsUntouchableItsPlaybookWaitsAndAnArmourPatchDrops() {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled();
        SetPiece boss = rig.piece;
        rig.toEruption();
        assertEquals(1, boss.phase());
        assertEquals(1, rig.drops.size(), "one drop");
        assertEquals(PickupType.ARMOUR_PATCH.ordinal(), (int) rig.drops.getFirst()[0]);
        assertEquals(Act6HSpecs.X, rig.drops.getFirst()[1], EPS);
        assertTrue(rig.drops.getFirst()[2] < Act6HSpecs.Y - 125, "under the bow's tip");
        int untouchable = 1;
        while (boss.opening()) {
            for (int p = 0; p < boss.partCount(); p++) {
                assertTrue(boss.partShielded(p), "untouchable through the eruption: part " + p);
            }
            assertTrue(rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty(), "its playbook waits");
            rig.step();
            untouchable++;
            assertTrue(untouchable <= SimStep.ticks(4) + 1, "4 s");
        }
        assertEquals(SimStep.ticks(4), untouchable, 1, "4 s");
        for (int p : Act6HSpecs.SACS) {
            assertFalse(boss.partShielded(p), "the sacs take hits: " + p);
        }
        assertTrue(boss.partShielded(Act6HSpecs.HEART), "the heart sealed");
        assertEquals(1, rig.drops.size(), "one drop only");
    }

    @Test
    void theHeartIsSealedUntilTwoSacsAreDeadThenItsPlaybookDrawsFromIt() {
        Act6HSpecs.Rig rig =
                new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled().toEruption().pastEruption();
        SetPiece boss = rig.piece;
        BossPlaybook book = boss.playbook().orElseThrow();
        rig.steps(2);
        assertEquals(Act6HSpecs.SAC_1, book.source(), "from the sacs while the heart is sealed");
        rig.kill(Act6HSpecs.SAC_1);
        rig.step();
        assertEquals(1, boss.phase());
        assertTrue(boss.partShielded(Act6HSpecs.HEART), "one sac down: still sealed");
        assertEquals(Act6HSpecs.SAC_2, book.source());
        rig.kill(Act6HSpecs.SAC_3);
        rig.step();
        assertEquals(2, boss.phase(), "two sacs down: the heart opens");
        assertFalse(boss.partShielded(Act6HSpecs.HEART));
        assertFalse(boss.partShielded(Act6HSpecs.SAC_2), "the last sac still takes hits");
        rig.steps(1.5);
        assertEquals(Act6HSpecs.HEART, book.source(), "from the heart");
    }

    @Test
    void theVrellPlaybookDrawsSeededNeverTwiceTheBroodAtMostEveryThirdDraw() {
        List<Integer> moves = plays(
                new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled().toEruption().pastEruption(), 150);
        assertTrue(moves.size() >= 25, "draws: " + moves.size());
        int brood = Integer.MIN_VALUE / 2;
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) {
                assertNotEquals(moves.get(i - 1), moves.get(i), "never twice running: " + moves);
            }
            if (moves.get(i) == Act6HSpecs.MOVE_BROOD) {
                assertTrue(i - brood >= 3, "the brood at most every third draw: " + moves);
                brood = i;
            }
        }
        for (int m = 0; m < 4; m++) {
            assertTrue(moves.contains(m), "every move drawn: " + m + " " + moves);
        }
        assertEquals(
                moves,
                plays(
                        new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT)
                                .settled()
                                .toEruption()
                                .pastEruption(),
                        150),
                "seeded");
    }

    @Test
    void theBroodSplitsFourChimerasFromALivingSacElseFromTheHeart() {
        Act6HSpecs.Rig rig =
                new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled().toEruption().pastEruption();
        SetPiece boss = rig.piece;
        List<double[]> broods = broodLaunches(rig);
        assertEquals(4, broods.size(), "a wing of 4");
        for (double[] launch : broods) {
            assertEquals(boss.partX(Act6HSpecs.SAC_1), launch[0], EPS, "from the swelling sac");
            assertEquals(boss.partY(Act6HSpecs.SAC_1), launch[1], EPS);
        }
        for (int p : Act6HSpecs.SACS) {
            rig.kill(p);
        }
        rig.step();
        assertEquals(2, boss.phase());
        broods = broodLaunches(rig);
        assertEquals(4, broods.size());
        for (double[] launch : broods) {
            assertEquals(boss.partX(Act6HSpecs.HEART), launch[0], EPS, "no sac left: the heart broods");
            assertEquals(boss.partY(Act6HSpecs.HEART), launch[1], EPS);
        }
    }

    /** Steps until a brood has run, at most 120 s; its launches. */
    private static List<double[]> broodLaunches(Act6HSpecs.Rig rig) {
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        List<double[]> launches = new ArrayList<>();
        boolean running = false;
        for (int i = 0; i < SimStep.ticks(120); i++) {
            rig.step();
            if (rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_BROOD)) {
                running = true;
            }
            if (running) {
                launches.addAll(rig.launches);
                if (book.mode() == BossPlaybook.Mode.REST) {
                    return launches;
                }
            }
        }
        throw new AssertionError("a brood drawn");
    }

    @Test
    void theBileLobsMarkThreePointsNeverWithinSixtyPxOfTheShipsX() {
        for (double shipX : new double[] {240, 30, 450, 130}) {
            Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT)
                    .settled()
                    .toEruption()
                    .pastEruption();
            rig.shipX = shipX;
            int volleys = 0;
            for (int i = 0; i < SimStep.ticks(150) && volleys < 4; i++) {
                rig.step();
                if (rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_LOBS)) {
                    volleys++;
                    List<double[]> marks = new ArrayList<>();
                    for (int k = 0; k < 10; k++) {
                        marks.addAll(rig.lobs);
                        rig.step();
                    }
                    assertEquals(3, marks.size(), "3 marks");
                    for (double[] lob : marks) {
                        assertTrue(Math.abs(lob[2] - shipX) >= 60 - EPS, "never within 60 px: " + lob[2] + " " + shipX);
                        assertTrue(lob[2] >= 0 && lob[2] <= PlayField.WIDTH);
                        assertTrue(lob[3] < Act6HSpecs.Y - 125, "under the bow: " + lob[3]);
                    }
                }
            }
            assertTrue(volleys >= 2, "lobs drawn: " + volleys);
        }
    }

    @Test
    void theSporeSpiralTurnsThreeArmsFourBulletsASecondForFourSeconds() {
        Act6HSpecs.Rig rig =
                new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled().toEruption().pastEruption();
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        for (int i = 0; i < SimStep.ticks(150); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_SPIRAL)) {
                continue;
            }
            int bullets = rig.bullets.size();
            int steps = 1;
            while (book.mode() == BossPlaybook.Mode.RUN) {
                rig.step();
                bullets += rig.bullets.size();
                steps++;
                assertTrue(rig.bullets.isEmpty() || rig.bullets.size() == 3, "three arms at once");
            }
            assertEquals(SimStep.ticks(4), steps, 2, "4 s");
            assertEquals(3 * 16, bullets, 3, "4 a second an arm");
            return;
        }
        throw new AssertionError("a spiral drawn");
    }

    @Test
    void theGraftedGunsFireThreeTimesFromBothDeadShoulders() {
        Act6HSpecs.Rig rig =
                new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled().toEruption().pastEruption();
        SetPiece boss = rig.piece;
        BossPlaybook book = boss.playbook().orElseThrow();
        for (int i = 0; i < SimStep.ticks(150); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY).contains(Act6HSpecs.MOVE_GRAFTED)) {
                continue;
            }
            List<double[]> shots = new ArrayList<>(rig.shots);
            List<Integer> at = new ArrayList<>();
            at.add(0);
            int step = 0;
            while (book.mode() == BossPlaybook.Mode.RUN) {
                rig.step();
                step++;
                if (!rig.shots.isEmpty()) {
                    at.add(step);
                }
                shots.addAll(rig.shots);
            }
            assertEquals(6, shots.size(), "three volleys from two shoulders");
            for (double[] shot : shots) {
                boolean left = Math.abs(shot[0] - boss.partX(Act6HSpecs.GEN_L)) < EPS;
                boolean right = Math.abs(shot[0] - boss.partX(Act6HSpecs.GEN_R)) < EPS;
                assertTrue(left || right, "from a dead shoulder: " + shot[0]);
            }
            assertEquals(List.of(0, SimStep.ticks(0.4), 2 * SimStep.ticks(0.4)), at, "0.4 s apart");
            return;
        }
        throw new AssertionError("the grafted guns drawn");
    }

    @Test
    void itRestsOnePointTwoSecondsThenPointSevenBelowHalfOfPhaseTwosHp() {
        Act6HSpecs.Rig rig =
                new Act6HSpecs.Rig(Act6HSpecs.ASCENDANT).settled().toEruption().pastEruption();
        BossPlaybook book = rig.piece.playbook().orElseThrow();
        restBeforeNextTell(rig, book);
        assertEquals(SimStep.ticks(1.2), restBeforeNextTell(rig, book), 1);
        // Phase 2's HP 6,400: the heart and two sacs dead leave 1,300 (no healing past its whole).
        rig.kill(Act6HSpecs.SAC_1);
        rig.kill(Act6HSpecs.SAC_2);
        rig.piece.damagePartFlat(Act6HSpecs.HEART, 2000);
        rig.step();
        restBeforeNextTell(rig, book);
        assertEquals(SimStep.ticks(0.7), restBeforeNextTell(rig, book), 1, "0.7 s below half");
    }

    @Test
    void atMostSixBossLaunchedChimerasAlive() {
        Sortie sortie =
                Act6HSpecs.sortie(Act6HSpecs.ASCENDANT, Act5ESpecs.loadout(Act5ESpecs.bolt(1, false)), TestSpecs.RULES);
        Act6HSpecs.settle(sortie, 0);
        for (int p : Act6HSpecs.PHASE_1) {
            sortie.destroyPart(0, p);
        }
        int most = 0;
        int launched = 0;
        for (int i = 0; i < SimStep.ticks(90); i++) {
            sortie.step(0);
            int alive = 0;
            for (int j = 0; j < sortie.enemyCount(); j++) {
                alive += sortie.enemy(j).launchedBy() == Launching.BOSS ? 1 : 0;
            }
            most = Math.max(most, alive);
            launched += sortie.events().count(SimEvents.Type.BOSS_LAUNCHED);
        }
        assertTrue(launched > 6, "broods launched: " + launched);
        assertTrue(most <= 6, "at most 6 alive: " + most);
    }
}
