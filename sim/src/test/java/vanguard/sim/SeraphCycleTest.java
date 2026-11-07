package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part E, step E1 (design/enemies/air/choir-seraph, the cycle; m11e §1 #1, #2, #4): a Seraph enters by its ring
 * (1.0 s, later 0.5 s), never flying a path; mirrors 2.5 s at each arrival (the mirrored point on its row), folds 0.3 s
 * before each jump (still, silent, hittable; {@code JUMP_FOLD}), fades 0.2 s, jumps; after 4 cycles its leaving tell
 * (0.8 s, rising 40 px, the HUD's count) and a last jump out: an escape, counted once; a kill in the tell is a kill.
 * Easy 3 cycles of 2.0 s, hard 5 of 3.0 s. The EMP misses it mid-jump; the ring cap makes a jump wait, mirroring.
 */
class SeraphCycleTest {
    private static Sortie sortie(Loadout loadout, WaveSpec... waves) {
        LevelScript level = TestSpecs.withObjectives(
                Act7ASpecs.level(40, List.of(waves), Optional.empty()),
                objectives -> objectives.withSecondary(Secondary.builder()
                        .credits(50)
                        .escapes("choir-seraph")
                        .escapesAllowed(3)
                        .build()));
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static WaveSpec single(EnemySpec spec) {
        return Act7ASpecs.single(0, spec);
    }

    /** The counts of one Seraph's life, flown with the ship idle (no fire). */
    private record Life(
            int steps,
            int firstRing,
            int laterGone,
            int folds,
            int foldSteps,
            int tellSteps,
            int arrivals,
            int rings,
            int jumpOuts,
            int tells,
            int silentShots,
            double rise) {}

    private static Life life(EnemySpec spec) {
        Sortie sortie = sortie(TestSpecs.LOADOUT, single(spec));
        sortie.step(Command.NONE);
        Enemy seraph = Act7ASpecs.find(sortie, "choir-seraph");
        assertTrue(seraph.teleport().gone(), "it enters gone, by its ring");
        assertEquals(1, sortie.events().count(SimEvents.Type.JUMP_RING), "its first ring at once");
        int steps = 1;
        int firstRing = 1;
        int laterGone = 0;
        int folds = 0;
        int foldSteps = 0;
        int tellSteps = 0;
        int arrivals = 0;
        int rings = 1;
        int jumpOuts = 0;
        int tells = 0;
        int silent = 0;
        double tellStart = Double.NaN;
        double tellTop = Double.NaN;
        while (Act7ASpecs.find(sortie, "choir-seraph") != null && steps < SimStep.ticks(40)) {
            Teleporting jumps = seraph.teleport();
            boolean folding = jumps.folding();
            boolean telling = jumps.leavingTell();
            double x = seraph.x();
            double y = seraph.y();
            assertEquals(telling ? 1 : 0, sortie.leaving(), "the HUD's count follows the tell");
            if (jumps.gone()) {
                assertFalse(ShotGeometry.onField(seraph), "gone: nothing reaches it");
                if (arrivals == 0) {
                    firstRing++;
                } else {
                    laterGone++;
                }
            }
            sortie.step(Command.NONE);
            steps++;
            int fired = sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            if (folding) {
                foldSteps++;
                assertTrue(ShotGeometry.onField(seraph), "the fold is hittable");
                silent += fired;
                if (seraph.teleport().folding()) {
                    assertEquals(x, seraph.x(), "it holds still");
                    assertEquals(y, seraph.y(), "it holds still");
                }
            }
            if (telling) {
                tellSteps++;
                silent += fired;
                assertTrue(ShotGeometry.onField(seraph), "the tell is hittable");
                if (Double.isNaN(tellStart)) {
                    tellStart = y;
                }
                tellTop = seraph.y();
            }
            folds += sortie.events().count(SimEvents.Type.JUMP_FOLD);
            arrivals += sortie.events().count(SimEvents.Type.ARRIVE);
            rings += sortie.events().count(SimEvents.Type.JUMP_RING);
            jumpOuts += sortie.events().count(SimEvents.Type.JUMP_OUT);
            tells += sortie.events().count(SimEvents.Type.LEAVING_TELL);
        }
        assertNull(Act7ASpecs.find(sortie, "choir-seraph"), "it jumped out for good");
        assertEquals(0, sortie.kills());
        assertEquals(1, sortie.escapesLost(), "one escape, counted once");
        return new Life(
                steps,
                firstRing,
                laterGone,
                folds,
                foldSteps,
                tellSteps,
                arrivals,
                rings,
                jumpOuts,
                tells,
                silent,
                tellTop - tellStart);
    }

    @Test
    void itEntersByItsRingCyclesFourTimesAndJumpsOutForGood() {
        Life life = life(Act7ESpecs.SERAPH);
        assertEquals(SimStep.ticks(1.0), life.firstRing(), 1, "its first ring 1.0 s");
        assertEquals(4, life.arrivals(), "4 cycles");
        assertEquals(4, life.rings(), "its entry's ring and 3 jumps'");
        assertEquals(3, life.folds(), "a fold before each jump in play");
        assertEquals(3 * SimStep.ticks(0.3), life.foldSteps(), 3, "0.3 s folds");
        assertEquals(3 * SimStep.ticks(0.5), life.laterGone(), 3, "later rings 0.5 s");
        assertEquals(4, life.jumpOuts(), "3 jumps and the last one out");
        assertEquals(1, life.tells(), "one leaving tell");
        assertEquals(SimStep.ticks(0.8), life.tellSteps(), 1, "the tell 0.8 s");
        assertEquals(40, life.rise(), 1, "it rises 40 px in its tell");
        assertEquals(0, life.silentShots(), "silent in its folds and its tell");
        // 1.0 + 4 × 2.5 + 3 × (0.3 + 0.2 + 0.5) + 0.8 + 0.2 = 15 s on the screen (m11e: 4 × 3.5 + 1.0).
        assertEquals(SimStep.ticks(15.0), life.steps(), 6, "its hold ≈ 15 s");
    }

    @Test
    void easyFliesThreeCyclesOfTwoSecondsAndHardFiveOfThree() {
        Life easy = life(Act7ESpecs.SERAPH_EASY);
        assertEquals(3, easy.arrivals());
        assertEquals(2, easy.folds());
        assertEquals(SimStep.ticks(1.0 + 3 * 2.0 + 2 * 1.0 + 1.0), easy.steps(), 6);
        Life hard = life(Act7ESpecs.SERAPH_HARD);
        assertEquals(5, hard.arrivals());
        assertEquals(4, hard.folds());
        assertEquals(SimStep.ticks(1.0 + 5 * 3.0 + 4 * 1.0 + 1.0), hard.steps(), 6);
    }

    @Test
    void itArrivesOnTheMirroredPointOnItsRowOutOfReach() {
        Sortie sortie = sortie(TestSpecs.LOADOUT, single(Act7ESpecs.SERAPH));
        Enemy seraph = null;
        int arrivals = 0;
        for (int i = 0; i < SimStep.ticks(16) && arrivals < 4; i++) {
            // The ship weaves: each ring forms where the mirror would put the unit then.
            int command = (i / SimStep.ticks(1.7)) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit();
            double shipX = sortie.ship().x();
            sortie.step(command);
            if (seraph == null) {
                seraph = Act7ASpecs.find(sortie, "choir-seraph");
            }
            if (sortie.events().count(SimEvents.Type.JUMP_RING) > 0) {
                double expected = Math.clamp(PlayField.WIDTH - sortie.ship().x(), 24, PlayField.WIDTH - 24);
                assertEquals(expected, seraph.teleport().ringX(), 1e-9, "the mirrored point as the ring starts");
                assertTrue(Math.abs(shipX - sortie.ship().x()) < 10);
                double row = PairRows.row(
                        PairRows.seed(seraph.wave), seraph.teleport().jumps(), 0, null);
                double ringY = seraph.teleport().ringY();
                assertTrue(
                        ringY == PlayField.HEIGHT - row
                                || ringY == sortie.ship().y() + 140,
                        "on its row, or pushed 140 px above the ship: " + ringY);
            }
            if (sortie.events().count(SimEvents.Type.ARRIVE) > 0) {
                arrivals++;
                assertTrue(Math.hypot(
                                seraph.x() - sortie.ship().x(),
                                seraph.y() - sortie.ship().y())
                        >= 140 - 1e-9);
                assertTrue(seraph.y() >= 80 && seraph.y() - sortie.ship().y() >= 140 - 1e-9, "its row off the ship's");
                assertTrue(seraph.y() >= PlayField.HEIGHT - 230 - 1e-9
                        || seraph.y() >= sortie.ship().y() + 140 - 1e-9);
            }
        }
        assertEquals(4, arrivals);
    }

    @Test
    void aKillInItsLeavingTellIsAKill() {
        Sortie sortie = sortie(TestSpecs.LOADOUT, single(Act7ESpecs.seraph(2, 2.5, 4, 4)));
        Enemy seraph = null;
        while (seraph == null || !seraph.teleport().leavingTell()) {
            sortie.step(Command.NONE);
            seraph = Act7ASpecs.find(sortie, "choir-seraph");
        }
        // The ship idles at the centre: the mirrored unit is straight above it.
        for (int i = 0; i < SimStep.ticks(1.0) && Act7ASpecs.find(sortie, "choir-seraph") != null; i++) {
            sortie.step(Command.FIRE.bit());
        }
        assertNull(Act7ASpecs.find(sortie, "choir-seraph"));
        assertEquals(1, sortie.kills(), "killed in its tell");
        assertEquals(0, sortie.escapesLost(), "no escape");
        assertEquals(1, sortie.escapesDestroyed());
    }

    @Test
    void theEmpMissesItMidJump() {
        Sortie sortie = sortie(TestSpecs.LOADOUT.withSpecial(Act3SpecialsTest.emp(1)), single(Act7ESpecs.SERAPH));
        Enemy seraph = null;
        while (seraph == null
                || seraph.teleport().jumps() == 0
                || !seraph.teleport().gone()) {
            sortie.step(Command.NONE);
            seraph = Act7ASpecs.find(sortie, "choir-seraph");
        }
        int stunned = 0;
        int command = Command.SPECIAL.bit();
        while (seraph.teleport().gone()) {
            sortie.step(command);
            command = Command.NONE;
            stunned += sortie.events().count(SimEvents.Type.EMP_STUNNED);
            assertFalse(seraph.empStunned());
        }
        assertEquals(0, stunned, "not on the screen for the EMP");
    }

    @Test
    void aFullRingCapHoldsAJumpMirroringAndCountsTheWait() {
        // A ring cap of 1: a pair's second unit waits gone to enter; a later unit's jump waits, mirroring, while the
        // first one's ring runs (A holds its ring 3.5–4.5 s; B, in at 1.2 s, mirroring 2.0 s, wants one at 4.2 s).
        Sortie pairSortie =
                sortie(TestSpecs.LOADOUT, Act7ESpecs.pair(0, Act7ESpecs.seraph(90, 2.5, 4, 1), Optional.empty()));
        pairSortie.step(Command.NONE);
        Enemy[] pair = Act7ESpecs.all(pairSortie, "choir-seraph");
        assertEquals(2, pair.length);
        Enemy first = pair[0].teleport().phase() == Teleporting.Phase.GONE ? pair[0] : pair[1];
        Enemy second = first == pair[0] ? pair[1] : pair[0];
        assertEquals(Teleporting.Phase.GONE, first.teleport().phase(), "one takes the one ring");
        assertEquals(Teleporting.Phase.ENTER, second.teleport().phase(), "the other waits for it, gone");
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            pairSortie.step(Command.NONE);
        }
        assertEquals(SimStep.ticks(1.0), second.teleport().waited(), 2, "its entry waited the first ring out");

        Sortie sortie = sortie(
                TestSpecs.LOADOUT,
                single(Act7ESpecs.seraph(90, 2.5, 4, 1)),
                Act7ASpecs.single(1.2, Act7ESpecs.seraph(91, 2.0, 4, 1)));
        Enemy late = null;
        int waitedMirroring = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            int before = late == null ? 0 : late.teleport().waited();
            sortie.step(Command.NONE);
            for (Enemy unit : Act7ESpecs.all(sortie, "choir-seraph")) {
                if (unit.spec().hp() == 91) {
                    late = unit;
                }
            }
            if (late != null
                    && late.teleport().phase() == Teleporting.Phase.GLIDE
                    && late.teleport().waited() > before) {
                assertTrue(late.shadow().mirroring(), "a starved jump waits, mirroring");
                waitedMirroring++;
            }
        }
        assertEquals(SimStep.ticks(0.3), late.teleport().waited(), 2, "it waited 0.3 s for the ring");
        assertTrue(waitedMirroring > 0);
    }
}
