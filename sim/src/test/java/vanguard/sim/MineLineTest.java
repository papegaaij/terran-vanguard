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
 * M9 part C, step C1 (design/enemies, Mine lines; design/enemies/space/minelayer; design/campaign Level 30, C1 a and
 * C2 a): the Minelayer's lay path from behind, its line of three clusters of 4 with one gap, the mines following the
 * scroll (the line's slope), the proximity blink and the blast, the cluster chain, the 40-mine limit, and Rook keeping
 * out of the proximity discs through the gap. Opt-in: spores fly as before (their tests and the golden flights).
 */
class MineLineTest {
    /** Level 30's minefield scroll, px/s. */
    private static final double SCROLL = 80;

    private static LevelScript level(List<WaveSpec> waves) {
        return LevelScript.builder(30, 5)
                .sections(List.of(new Section(60, SCROLL)))
                .waves(waves)
                .ground(GroundSpec.SPACE)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    private static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** A laid line mine: where and on which step. */
    private record Laid(double x, double y, int step) {}

    /** Flies {@code seconds}, collecting the line mines laid (MINE_DROPPED value 1). */
    private static List<Laid> fly(Sortie sortie, double seconds, int command) {
        List<Laid> laid = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(command);
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                if (events.type(k) == SimEvents.Type.MINE_DROPPED && events.value(k) == 1) {
                    laid.add(new Laid(events.x(k), events.y(k), i));
                }
            }
        }
        return laid;
    }

    private static double[] xs(List<Laid> laid) {
        return laid.stream().mapToDouble(Laid::x).sorted().toArray();
    }

    // --- the line -------------------------------------------------------------------------------------------------

    @Test
    void theLineIsThreeClustersOfFourWithOneGapAtTheBoundaryNearestTheGapPoint() {
        EnemySpec.MineLayer lay = Act5CSpecs.LAY;
        assertEquals(12, lay.mines());
        assertEquals(456, lay.width(), 1e-9, "3 × 96 + 56 + 112");
        assertEquals(164, lay.gapMiddle(0), 1e-9, "left-centre");
        assertEquals(316, lay.gapMiddle(1), 1e-9, "right-centre");
        assertEquals(0, lay.gapBoundary(Act5CSpecs.GAP_LEFT));
        assertEquals(1, lay.gapBoundary(Act5CSpecs.GAP_RIGHT));
        double[] xs = new double[EnemySpec.MineLayer.MOST_MINES];
        assertEquals(12, lay.positions(0, xs));
        double[] left = {12, 44, 76, 108, 220, 252, 284, 316, 372, 404, 436, 468};
        for (int k = 0; k < 12; k++) {
            assertEquals(left[k], xs[k], 1e-9);
        }
        // The gap's middle is 56 px from both edge mines: outside the 48 px proximity.
        assertEquals(56, lay.gapMiddle(0) - xs[3], 1e-9);
        assertEquals(56, xs[4] - lay.gapMiddle(0), 1e-9);
        // Easy: the middle cluster 3 mines beside a 144 px gap, the same width.
        EnemySpec.MineLayer easy = Act5CSpecs.EASY_LAY;
        assertEquals(11, easy.mines());
        assertEquals(456, easy.width(), 1e-9);
        assertEquals(11, easy.positions(1, xs));
        assertEquals(144, xs[7] - xs[6], 1e-9, "the gap after the middle cluster's three");
    }

    @Test
    void aLayerRisesFromBehindInItsLaneWarnedAtTheBottomThenLaysItsLineAcrossItsRow() {
        Sortie sortie = sortie(level(List.of(Act5CSpecs.layers(
                4,
                Act5CSpecs.MINELAYER,
                1,
                WaveSpec.Edge.LEFT,
                new WaveSpec.MineLines(List.of(100.0), List.of(Act5CSpecs.GAP_LEFT))))));
        boolean warned = false;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
            warned |= WarningEdge.BOTTOM.in(sortie.edgeWarnings());
        }
        assertTrue(warned, "the bottom edge warns before the layer rises");
        List<Laid> laid = new ArrayList<>();
        double laneX = Double.NaN;
        double lowest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < SimStep.ticks(9); i++) {
            sortie.step(Command.NONE);
            for (int k = 0; k < sortie.enemyCount(); k++) {
                Enemy u = sortie.enemy(k);
                if (u.mineLayer() && !u.laying()) {
                    laneX = u.x();
                    lowest = Math.min(lowest, u.y());
                }
            }
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                if (events.type(k) == SimEvents.Type.MINE_DROPPED && events.value(k) == 1) {
                    laid.add(new Laid(events.x(k), events.y(k), i));
                }
            }
        }
        assertEquals(70, laneX, 1e-9, "up the left lane, 70 px in");
        assertTrue(lowest < 0, "from below the bottom edge");
        assertEquals(12, laid.size());
        double[] xs = xs(laid);
        double[] left = {12, 44, 76, 108, 220, 252, 284, 316, 372, 404, 436, 468};
        for (int k = 0; k < 12; k++) {
            assertEquals(left[k], xs[k], 1e-9);
        }
        for (Laid mine : laid) {
            assertEquals(PlayField.HEIGHT - 100, mine.y(), 1e-9, "laid on its lay row");
        }
        // Laid left to right as it strafes; the two between its lane and the left edge at once as it turns.
        assertEquals(laid.get(0).step(), laid.get(1).step());
        for (int k = 2; k < 12; k++) {
            assertTrue(laid.get(k).x() > laid.get(k - 1).x());
            assertTrue(laid.get(k).step() > laid.get(k - 1).step());
        }
        // 150 px/s across: the last mine (468) 398 px after the lane, 2.65 s later.
        assertEquals((468 - 70) / 150.0, (laid.get(11).step() - laid.get(0).step()) * SimStep.SECONDS, 0.05);
    }

    @Test
    void theMinesHoldTheirPlaceInTheFieldSoTheLineSlopesAbout28DegreesAt80() {
        Sortie sortie = sortie(level(List.of(Act5CSpecs.layers(
                0.5,
                Act5CSpecs.MINELAYER,
                1,
                WaveSpec.Edge.RIGHT,
                new WaveSpec.MineLines(List.of(100.0), List.of(Act5CSpecs.GAP_RIGHT))))));
        List<Laid> laid = fly(sortie, 8.5, Command.NONE);
        assertEquals(12, laid.size());
        double[] xs = xs(laid);
        double[] right = {12, 44, 76, 108, 164, 196, 228, 260, 372, 404, 436, 468};
        for (int k = 0; k < 12; k++) {
            assertEquals(right[k], xs[k], 1e-9, "the gap at the right-centre boundary");
        }
        assertTrue(laid.get(0).x() > laid.get(11).x(), "from the right lane it lays right to left");
        // Every mine has come down with the scroll since it was laid: the line slopes down toward the layer's side
        // (the two between its lane and the right edge, laid at once as it turned, aside).
        assertEquals(12, sortie.mineCount());
        Mine early = sortie.mine(mineAt(sortie, 404));
        Mine late = sortie.mine(mineAt(sortie, 12));
        double slope = (early.y() - late.y()) / (404 - 12);
        assertEquals(-SCROLL / 150, slope, 1e-3, "tan 28° = 80 / 150: the right-hand mines laid first, lower");
        // One step later every mine is 80 / 60 px lower, none sideways.
        double[] ys = new double[sortie.mineCount()];
        double[] mxs = new double[sortie.mineCount()];
        for (int k = 0; k < ys.length; k++) {
            ys[k] = sortie.mine(k).y();
            mxs[k] = sortie.mine(k).x();
        }
        sortie.step(Command.NONE);
        for (int k = 0; k < ys.length; k++) {
            assertEquals(ys[k] - SCROLL * SimStep.SECONDS, sortie.mine(k).y(), 1e-6);
            assertEquals(mxs[k], sortie.mine(k).x(), 1e-9);
        }
    }

    @Test
    void aColumnEntersStaggeredAndAtMostFortyLineMinesLive() {
        // Hard's column of three lines (rows 100, 180, 260), then two more layers: the limit holds at 40.
        Sortie sortie = sortie(level(List.of(
                Act5CSpecs.layers(
                        0.5,
                        Act5CSpecs.MINELAYER,
                        3,
                        WaveSpec.Edge.ALTERNATING,
                        new WaveSpec.MineLines(
                                List.of(100.0, 180.0, 260.0), List.of(Act5CSpecs.GAP_LEFT, Act5CSpecs.GAP_RIGHT))),
                Act5CSpecs.layers(
                        3,
                        Act5CSpecs.MINELAYER,
                        2,
                        WaveSpec.Edge.LEFT,
                        new WaveSpec.MineLines(List.of(140.0), List.of(Act5CSpecs.GAP_LEFT))))));
        int most = 0;
        List<Integer> firstSteps = new ArrayList<>();
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            most = Math.max(most, sortie.mineCount());
            for (int k = 0; k < sortie.enemyCount(); k++) {
                Enemy u = sortie.enemy(k);
                if (u.mineLayer() && seen.add(u.serial())) {
                    firstSteps.add(i);
                }
            }
        }
        assertEquals(5, firstSteps.size());
        assertEquals(2.5, (firstSteps.get(1) - firstSteps.get(0)) * SimStep.SECONDS, 0.02, "2.5 s apart");
        assertEquals(40, most, "at most 40 line mines alive: the fifth line is not topped up");
        assertTrue(most < 96, "the pool never fills");
    }

    // --- proximity, blast, chain ------------------------------------------------------------------------------

    @Test
    void anArmedMineNearTheShipBlinksThenBurstsWithItsBlastOnce() {
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level(List.of()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Ship ship = sortie.ship();
        sortie.dropMine(Act5CSpecs.MINE_GUN, ship.x() + 40, ship.y() + 150, 0);
        double before = ship.defences().shield() + ship.defences().armour();
        int blinkStep = -1;
        int burstStep = -1;
        int blasts = 0;
        int blastDamage = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                if (events.type(k) == SimEvents.Type.MINE_BLINK) {
                    assertEquals(-1, blinkStep, "it blinks once");
                    blinkStep = i;
                } else if (events.type(k) == SimEvents.Type.MINE_BURST) {
                    assertEquals(1, events.value(k), "a proximity burst");
                    burstStep = i;
                } else if (events.type(k) == SimEvents.Type.MINE_BLAST_HIT) {
                    blasts++;
                    blastDamage = events.value(k);
                }
            }
        }
        assertTrue(blinkStep > SimStep.ticks(1), "armed first");
        assertEquals(SimStep.ticks(0.6), burstStep - blinkStep, "a 0.6 s blink");
        assertEquals(1, blasts, "the blast hits once");
        assertEquals(10, blastDamage, "heavy 10");
        assertEquals(10, before - ship.defences().shield() - ship.defences().armour(), 1e-6);
        assertEquals(0, sortie.mineCount());
        assertEquals(0, sortie.bulletCount(), "its ring stays out of the 72 px no-fire distance");
    }

    @Test
    void aMineFarFromTheShipNeverBlinksAndAnUnarmedOneNeverBursts() {
        Sortie sortie = sortie(level(List.of()));
        Ship ship = sortie.ship();
        // 60 px to the side: outside the 48 px proximity all the way down.
        sortie.dropMine(Act5CSpecs.MINE_GUN, ship.x() + 60, ship.y() + 100, 0);
        // Within 48 px only once it armed (its first second it comes down unarmed).
        sortie.dropMine(Act5CSpecs.MINE_GUN, ship.x() + 20, ship.y() + 120, 0);
        int blinks = 0;
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(Command.NONE);
            blinks += sortie.events().count(SimEvents.Type.MINE_BLINK);
        }
        assertEquals(0, blinks);
        assertEquals(2, sortie.mineCount());
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(Command.NONE);
            blinks += sortie.events().count(SimEvents.Type.MINE_BLINK);
        }
        assertEquals(1, blinks, "only the second, once armed and still within 48 px");
    }

    @Test
    void rookTriggersAMineToo() {
        LevelScript level = level(List.of());
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        Wingman rook = sortie.wingman().orElseThrow();
        // Armed at once, 40 px above him: within his 48 px, more than 48 from the ship.
        EnemyGun armed = new EnemyGun(
                1,
                0,
                1,
                100,
                6,
                false,
                1,
                0,
                TestSpecs.INFINITE,
                TestSpecs.INFINITE,
                Optional.of(new EnemyGun.MineSpec(0, 30, 0, 1, 8, 4, false, 0).withLine(Act5CSpecs.LINE_MINE.line())));
        double x = rook.x();
        double y = rook.y() + 40;
        assertTrue(Math.hypot(x - sortie.ship().x(), y - sortie.ship().y()) > 48, "the ship does not trigger it");
        sortie.dropMine(armed, x, y, 0);
        int blinks = 0;
        int bursts = 0;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            blinks += sortie.events().count(SimEvents.Type.MINE_BLINK);
            bursts += sortie.events().count(SimEvents.Type.MINE_BURST);
        }
        assertEquals(1, blinks, "his centre within 48 px blinks it");
        assertEquals(1, bursts);
    }

    /** Drops line mines at (x, y) for each x; returns nothing (the pool keeps them in order). */
    private static void lay(Sortie sortie, double y, double... xs) {
        for (double x : xs) {
            sortie.dropMine(Act5CSpecs.MINE_GUN, x, y, 0);
        }
    }

    private static int mineAt(Sortie sortie, double x) {
        for (int k = 0; k < sortie.mineCount(); k++) {
            if (Math.abs(sortie.mine(k).x() - x) < 1e-6) {
                return k;
            }
        }
        return -1;
    }

    @Test
    void aShotMineSetsOffItsClusterHopByHopAndTheNextClusterStays() {
        Sortie sortie = sortie(level(List.of()));
        // Two clusters of 4, 32 px apart within, 56 between: high above the ship.
        lay(sortie, 480, 100, 132, 164, 196, 252, 284, 316, 348);
        for (int i = 0; i < SimStep.ticks(1.2); i++) {
            sortie.step(Command.NONE);
        }
        int credits = sortie.credits();
        sortie.objectivePasses.shootMine(mineAt(sortie, 132));
        assertEquals(7, sortie.mineCount());
        List<Integer> chained = new ArrayList<>();
        for (int i = 1; i <= SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
            for (int k = 0; k < sortie.events().count(SimEvents.Type.MINE_CHAINED); k++) {
                chained.add(i);
            }
        }
        assertEquals(3, chained.size(), "its three neighbours in the cluster");
        int hop = SimStep.ticks(0.08);
        assertEquals(List.of(hop, hop, 2 * hop), chained, "100 and 164 a hop later, 196 a hop after 164");
        assertEquals(4, sortie.mineCount(), "the next cluster, 56 px on, stays");
        for (int k = 0; k < sortie.mineCount(); k++) {
            assertTrue(sortie.mine(k).x() >= 252);
            assertFalse(sortie.mine(k).setOff());
        }
        assertEquals(credits, sortie.credits(), "mines pay nothing");
    }

    @Test
    void aMineShotByTheGunBurstsAndTheChainReplaysTheSame() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = sortie(level(List.of()));
            Ship ship = sortie.ship();
            // A cluster straight ahead of the guns, beyond the no-fire distance.
            lay(sortie, ship.y() + 300, ship.x() - 32, ship.x(), ship.x() + 32, ship.x() + 64);
            int chained = 0;
            int destroyed = 0;
            for (int i = 0; i < SimStep.ticks(3); i++) {
                sortie.step(i > SimStep.ticks(1.1) ? Command.FIRE.bit() : Command.NONE);
                chained += sortie.events().count(SimEvents.Type.MINE_CHAINED);
                destroyed += sortie.events().count(SimEvents.Type.MINE_DESTROYED);
            }
            assertEquals(1, destroyed, "the shot one");
            assertEquals(3, chained, "the rest of its cluster");
            assertEquals(0, sortie.mineCount());
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1]);
    }

    // --- Rook in the minefield ------------------------------------------------------------------------------------

    /** Two clusters of 4 with a 112 px gap centred on the ship's start x (240). */
    private static final EnemySpec.MineLayer CENTRE_GAP =
            new EnemySpec.MineLayer(120, 150, 70, 2, 4, 32, 56, 112, 0, 40);

    @Test
    void rookFliesTheGapWithTheShipAndSetsNoMineOff() {
        assertEquals(240, CENTRE_GAP.gapMiddle(0), 1e-9);
        // A layer he cannot shoot down before it lays (his gun fires at it).
        EnemySpecCopy tanky = new EnemySpecCopy(Act5CSpecs.minelayer(CENTRE_GAP));
        tanky.hp = 1e6;
        LevelScript level = level(List.of(Act5CSpecs.layers(
                0.5, tanky.build(), 1, WaveSpec.Edge.LEFT, new WaveSpec.MineLines(List.of(60.0), List.of(240.0)))));
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        Wingman rook = sortie.wingman().orElseThrow();
        int blinks = 0;
        boolean passed = false;
        double rookXAtLine = Double.NaN;
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(Command.NONE);
            blinks += sortie.events().count(SimEvents.Type.MINE_BLINK);
            for (int k = 0; k < sortie.mineCount(); k++) {
                Mine mine = sortie.mine(k);
                // The left cluster's gap-edge mine (184, on his side; laid first, lowest) passing his height.
                if (Math.abs(mine.x() - 184) < 1 && Math.abs(mine.y() - rook.y()) < 2) {
                    passed = true;
                    rookXAtLine = rook.x();
                }
            }
        }
        assertTrue(passed, "the line came down past him");
        assertEquals(0, blinks, "neither the ship in the gap nor Rook set a mine off");
        assertTrue(rookXAtLine > 184 + 48, "he flew the gap, clear of its proximity: " + rookXAtLine);
    }

    // --- checks ---------------------------------------------------------------------------------------------------

    @Test
    void aLineMineDoesNotDriftAndALayerIsAPlainAirUnitFromTheRear() {
        assertThrows(IllegalArgumentException.class, () -> new EnemyGun.MineSpec(1, 30, 20, 1, 8, 4, false, 0)
                .withLine(Optional.of(new EnemyGun.LineMine(48, 0.6, 40, 10, 40, 0.08))));
        assertThrows(
                IllegalArgumentException.class,
                () -> TestSpecs.SKITTER.withMineLayer(Optional.of(Act5CSpecs.LAY)),
                "a layer's gun lays line mines");
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.wave(
                        1, WaveSpec.Formation.COLUMN, Act5CSpecs.MINELAYER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .withMineLines(new WaveSpec.MineLines(List.of(100.0), List.of(160.0))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.MineLayer(120, 150, 70, 1, 4, 32, 56, 112, 0, 40),
                "a gap needs two clusters");
    }
}
