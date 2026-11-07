package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, The rear sweep; m11f-gaps.md §1 #2, §4), on the test
 * Chimera ({@link Act7FSpecs}): the sweep is never phase 2's first draw and counts as a told line; its tell flares the
 * far flank and Rook's barks alternate; it drops out of the top edge, is out of sight 1.0 s with its rising point marked
 * on the bottom edge of the half the ship is not on (≥ 160 px off its x), rises there and climbs at 200 px/s with its
 * body box ≥ 100 px off the ship every step (a ship chasing it sideways, a ship over its lane), never crossing the ship's
 * x, its parts hittable on the screen, firing every 0.4 s; then it glides back into its sway, which goes on from there.
 */
class RearSweepTest {
    private static final double EPS = 1e-6;

    /** The test Chimera in phase 2, the ship at (100, 100), stepped until the sweep is told. */
    private static Act7FSpecs.Rig told(SetPieceSpec spec) {
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(spec).settled().toPhase2();
        for (int i = 0; i < SimStep.ticks(20); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.SWEEP_TELL).isEmpty()) {
                return rig;
            }
        }
        throw new AssertionError("no sweep told");
    }

    /** Whether the ship's centre is clear of the body box grown by 100 px. */
    private static boolean clear(SetPiece piece, double shipX, double shipY) {
        return Math.abs(shipX - piece.x()) - Act7FSpecs.BODY.width() / 2 >= 100 - EPS
                || Math.abs(shipY - piece.y()) - Act7FSpecs.BODY.height() / 2 >= 100 - EPS;
    }

    @Test
    void itRisesOnTheFarSideAfterASecondOutOfSightAndClimbsClearOfTheShip() {
        Act7FSpecs.Rig rig = told(Act7FSpecs.CHIMERA);
        SetPiece boss = rig.piece;
        RearSweep sweep = boss.rearSweep().orElseThrow();
        assertEquals(List.of(1), rig.values(SimEvents.Type.SWEEP_TELL), "the far flank: away from the ship");
        assertEquals(List.of("1"), rig.sweepCues, "Rook's first bark");
        assertEquals(
                PhasePlaybook.Kind.REAR_SWEEP, boss.playbook().orElseThrow().kind());
        int tell = rig.tick;
        int out = -1;
        int mark = -1;
        int climb = -1;
        int done = -1;
        List<Integer> shotTicks = new ArrayList<>();
        boolean hitOnScreen = false;
        while (done < 0 && rig.tick - tell < SimStep.ticks(15)) {
            rig.step();
            if (!rig.values(SimEvents.Type.SWEEP_OUT).isEmpty()) {
                out = rig.tick;
            }
            if (!rig.values(SimEvents.Type.SWEEP_MARK).isEmpty()) {
                mark = rig.tick;
                assertEquals(List.of(1), rig.values(SimEvents.Type.SWEEP_MARK), "the right: the ship is on the left");
                double[] at = rig.place(SimEvents.Type.SWEEP_MARK);
                assertEquals(PlayField.WIDTH - 60, at[0], EPS, "along the side, its body at the edge");
                assertEquals(0, at[1], EPS, "on the bottom edge");
                assertTrue(at[0] - rig.shipX >= 160, "at least 160 px off the ship's x");
                assertTrue(boss.y() - Act7FSpecs.SIZE.height() / 2 >= PlayField.HEIGHT - EPS, "out of sight");
            }
            if (sweep.away()) {
                assertTrue(boss.y() - Act7FSpecs.SIZE.height() / 2 >= PlayField.HEIGHT - EPS, "out of sight");
            }
            if (!rig.values(SimEvents.Type.SWEEP_CLIMB).isEmpty()) {
                climb = rig.tick;
            }
            if (sweep.climbing()) {
                assertTrue(clear(boss, rig.shipX, rig.shipY), "≥ 100 px off the ship at " + boss.x() + ", " + boss.y());
                assertTrue(boss.x() > rig.shipX, "never across the ship's path");
                if (!rig.shots.isEmpty()) {
                    shotTicks.add(rig.tick);
                }
                if (boss.partY(Act7FSpecs.SAC_L) > 0 && boss.partY(Act7FSpecs.SAC_L) < PlayField.HEIGHT) {
                    assertFalse(boss.partShielded(Act7FSpecs.SAC_L), "a sac on the screen takes hits");
                    hitOnScreen = true;
                }
            }
            if (!rig.values(SimEvents.Type.SWEEP_DONE).isEmpty()) {
                done = rig.tick;
            }
        }
        assertEquals(SimStep.ticks(0.8), out - tell, "the drop after the 0.8 s tell");
        assertTrue(mark > out, "marked once out of sight");
        assertEquals(SimStep.ticks(1.0), climb - mark, "1.0 s out of sight");
        assertTrue(done > climb, "back after the climb");
        assertTrue(hitOnScreen, "its sacs came on the screen");
        assertTrue(shotTicks.size() >= 4, "the climb fires: " + shotTicks);
        for (int i = 1; i < shotTicks.size(); i++) {
            assertEquals(SimStep.ticks(0.4), shotTicks.get(i) - shotTicks.get(i - 1), "every 0.4 s");
        }
        // The climb's time at 200 px/s from below the bottom edge to the station's height.
        double climbPx = Act7FSpecs.Y + Act7FSpecs.SIZE.height() / 2;
        assertEquals(climbPx / 200, (done - climb) * SimStep.SECONDS, 0.4, "200 px/s up, then the glide back");
        assertTrue(boss.x() >= 120 - EPS && boss.x() <= 360 + EPS, "in its sway's reach: " + boss.x());
        assertEquals(Act7FSpecs.Y, boss.y(), EPS, "at its station's height");
        assertFalse(sweep.busy());
        double last = boss.x();
        for (int i = 0; i < SimStep.ticks(2); i++) {
            rig.step();
            assertTrue(
                    Math.abs(boss.x() - last) <= 120 * 2 * Math.PI / 8.4 * SimStep.SECONDS + 0.5, "the sway goes on");
            last = boss.x();
        }
    }

    @Test
    void aShipChasingItSidewaysPushesItAwayNeverAcross() {
        Act7FSpecs.Rig rig = told(Act7FSpecs.CHIMERA);
        SetPiece boss = rig.piece;
        RearSweep sweep = boss.rearSweep().orElseThrow();
        double widest = 0;
        boolean pushed = false;
        while (!rig.values(SimEvents.Type.SWEEP_DONE).equals(List.of(1)) && rig.tick < SimStep.ticks(120)) {
            if (sweep.climbing()) {
                // 300 px/s to the right, up to the field's edge.
                rig.shipX = Math.min(PlayField.WIDTH - 15, rig.shipX + 300 * SimStep.SECONDS);
            }
            rig.step();
            if (sweep.climbing()) {
                assertTrue(clear(boss, rig.shipX, rig.shipY), "≥ 100 px off at " + boss.x() + " / " + rig.shipX);
                assertTrue(boss.x() > rig.shipX, "never across the ship's path");
                pushed |= boss.x() > PlayField.WIDTH - 60 + EPS;
                widest = Math.max(widest, boss.x());
            }
        }
        assertTrue(pushed, "the chase pushed it out: " + widest);
        assertEquals(List.of(1), rig.values(SimEvents.Type.SWEEP_DONE));
        assertTrue(boss.x() <= 360 + EPS, "back in its sway's reach");
    }

    @Test
    void aShipOverItsLaneMakesItHoldAndSlideOutFromUnder() {
        Act7FSpecs.Rig rig = told(Act7FSpecs.CHIMERA);
        SetPiece boss = rig.piece;
        RearSweep sweep = boss.rearSweep().orElseThrow();
        boolean held = false;
        double lastY = -1e9;
        while (!sweep.climbing()) {
            rig.step();
        }
        rig.shipX = sweep.markX();
        rig.shipY = 300;
        while (sweep.climbing()) {
            rig.step();
            assertTrue(clear(boss, rig.shipX, rig.shipY), "≥ 100 px off at " + boss.x() + ", " + boss.y());
            assertTrue(boss.x() >= rig.shipX, "it slides out to its own side");
            held |= boss.y() == lastY;
            lastY = boss.y();
        }
        assertTrue(held, "it held its height under the ship");
    }

    @Test
    void itIsNeverPhaseTwosFirstDrawAndCountsAsAToldLine() {
        for (long seed = 1; seed <= 6; seed++) {
            Act7FSpecs.Rig rig =
                    new Act7FSpecs.Rig(Act7FSpecs.CHIMERA, seed).settled().toPhase2();
            List<PhasePlaybook.Kind> drawn = new ArrayList<>();
            for (int i = 0; i < SimStep.ticks(25); i++) {
                rig.step();
                if (!rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty()) {
                    drawn.add(rig.piece.playbook().orElseThrow().kind());
                }
            }
            assertNotEquals(PhasePlaybook.Kind.REAR_SWEEP, drawn.getFirst(), "seed " + seed);
            assertTrue(drawn.contains(PhasePlaybook.Kind.REAR_SWEEP), "seed " + seed + ": " + drawn);
            // Phase 3: a lance or the sweep at least every third draw, the sweep never first.
            for (int p : Act7FSpecs.SACS) {
                rig.kill(p);
            }
            rig.steps(2.05);
            List<PhasePlaybook.Kind> late = new ArrayList<>();
            for (int i = 0; i < SimStep.ticks(60); i++) {
                rig.step();
                if (!rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty()) {
                    late.add(rig.piece.playbook().orElseThrow().kind());
                }
            }
            assertNotEquals(PhasePlaybook.Kind.REAR_SWEEP, late.getFirst(), "seed " + seed);
            assertTrue(late.contains(PhasePlaybook.Kind.REAR_SWEEP), "seed " + seed + ": " + late);
            int since = 0;
            for (PhasePlaybook.Kind kind : late) {
                since = PhasePlaybook.countsAsLance(kind) ? 0 : since + 1;
                assertTrue(since < 3, "seed " + seed + ": " + late);
            }
        }
    }

    @Test
    void rooksBarksAlternateAndATwoDrawSpacingSweepsEverySecondDraw() {
        Act7FSpecs.Rig rig = new Act7FSpecs.Rig(Act7FSpecs.SWEEPING).settled().toPhase2();
        List<PhasePlaybook.Kind> drawn = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(40); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty()) {
                drawn.add(rig.piece.playbook().orElseThrow().kind());
            }
        }
        assertTrue(rig.sweepCues.size() >= 3, "sweeps: " + rig.sweepCues);
        for (int i = 0; i < rig.sweepCues.size(); i++) {
            assertEquals(i % 2 == 0 ? "1" : "2", rig.sweepCues.get(i));
        }
        for (int i = 1; i < drawn.size(); i += 2) {
            assertEquals(PhasePlaybook.Kind.REAR_SWEEP, drawn.get(i), drawn.toString());
        }
    }

    @Test
    void aPhaseEndingMidSweepLetsItFinishBeforeTheNextDraw() {
        Act7FSpecs.Rig rig = told(Act7FSpecs.CHIMERA);
        RearSweep sweep = rig.piece.rearSweep().orElseThrow();
        while (!sweep.climbing()) {
            rig.step();
        }
        for (int p : Act7FSpecs.SACS) {
            rig.kill(p);
        }
        boolean toldDuring = false;
        while (sweep.busy()) {
            rig.step();
            toldDuring |= !rig.values(SimEvents.Type.BOSS_PLAY_TELL).isEmpty();
        }
        assertEquals(2, rig.piece.phase(), "phase 3 began mid-climb");
        assertFalse(toldDuring, "no draw until it is back");
        assertEquals(Act7FSpecs.Y, rig.piece.y(), EPS);
    }

    @Test
    void itsStateIsHashedAndRestartsFresh() {
        Act7FSpecs.Rig rig = told(Act7FSpecs.CHIMERA);
        StateHash a = new StateHash();
        rig.piece.addTo(a);
        rig.step();
        StateHash b = new StateHash();
        rig.piece.addTo(b);
        assertNotEquals(a.value(), b.value());
        rig.piece.reset();
        assertEquals(RearSweep.Mode.IDLE, rig.piece.rearSweep().orElseThrow().mode());
        assertEquals(0, rig.piece.rearSweep().orElseThrow().sweeps());
    }

    @Test
    void rookFliesTrailWhileItIsToldOrFlyingAndWingAfter() {
        Sortie sortie = Act7FSpecs.sortie(
                Act7FSpecs.SWEEPING,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.RIGHT)),
                TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = sortie.setPiece(0);
        for (int i = 0; i < SimStep.ticks(15) && !boss.engaged(); i++) {
            sortie.step(0);
        }
        for (int p : Act7FSpecs.PHASE_1) {
            Act7FSpecs.kill(sortie, p);
        }
        RearSweep sweep = boss.rearSweep().orElseThrow();
        Wingman rook = sortie.wingman().orElseThrow();
        boolean trailed = false;
        boolean done = false;
        for (int i = 0; i < SimStep.ticks(30) && !done; i++) {
            sortie.step(0);
            if (sweep.climbing()) {
                trailed |= rook.formation() == Wingman.Formation.TRAIL;
            }
            done = trailed && !sweep.busy();
        }
        assertTrue(trailed, "Trail during the sweep");
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(0);
        }
        assertNotEquals(Wingman.Formation.TRAIL, rook.formation(), "back out of Trail after it");
    }
}
