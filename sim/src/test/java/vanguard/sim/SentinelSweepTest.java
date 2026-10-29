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
 * M9 part E, step E1 (design/enemies/ground/sentinel-tower, {@code laser-sweep}; m9e-gaps.md §1 #4): a ground unit's
 * sweep is due while it is on the field, centred on the ship's bearing in any direction (up the screen too), every third
 * sweep a full turn (hard every second, easy none) with its 1.0 s ring telegraph, 360° over 3.0 s, the full turns'
 * direction alternating; one hit per sweep; the EMP Burst cuts a live beam.
 */
class SentinelSweepTest {
    private static final EnemySpec.Sweep SWEEP = Act5ESpecs.SWEEP;

    /** A sweep stepped on the field for {@code seconds} at (240, 300), the ship at (shipX, shipY); its telegraphs. */
    private static List<double[]> telegraphs(EnemySpec.GroundSweep turns, double seconds, double shipX, double shipY) {
        GroundSweepBeam beam = new GroundSweepBeam();
        beam.clear();
        List<double[]> out = new ArrayList<>();
        for (int step = 1; step <= SimStep.ticks(seconds); step++) {
            beam.step(SWEEP, turns, true, 240, 300, shipX, shipY);
            if (beam.telegraphStarted()) {
                out.add(new double[] {step, beam.fullTurn() ? 1 : 0, beam.from(), beam.to(), beam.centre()});
            }
        }
        return out;
    }

    @Test
    void itAimsAllRoundOnTheShipsBearing() {
        double[] down = telegraphs(Act5ESpecs.FULL_TURNS, 1.5, 240, 100).getFirst();
        assertEquals(0, down[4], 1e-9, "straight down");
        double[] up = telegraphs(Act5ESpecs.FULL_TURNS, 1.5, 240, 500).getFirst();
        assertEquals(Math.PI, Math.abs(up[4]), 1e-9, "a tower the ship passed sweeps up the screen");
        double[] right = telegraphs(Act5ESpecs.FULL_TURNS, 1.5, 400, 300).getFirst();
        assertEquals(Enemy.heading(160, 0), right[4], 1e-9);
        assertEquals(SWEEP.arcRadians(), Math.abs(right[3] - right[2]), 1e-9, "a 120° arc round it");
        assertEquals(SimStep.ticks(SWEEP.firstDelaySeconds()), (int) down[0], "its first delay after it came on");
    }

    @Test
    void everyThirdSweepIsAFullTurn_theirDirectionAlternating() {
        List<double[]> sweeps = telegraphs(Act5ESpecs.FULL_TURNS, 40, 240, 100);
        assertTrue(sweeps.size() >= 7, "" + sweeps.size());
        List<Double> fullSpans = new ArrayList<>();
        for (int n = 1; n <= sweeps.size(); n++) {
            double[] sweep = sweeps.get(n - 1);
            assertEquals(n % 3 == 0, sweep[1] == 1, "sweep " + n);
            if (sweep[1] == 1) {
                fullSpans.add(sweep[3] - sweep[2]);
                assertEquals(0, sweep[2], 1e-9, "a full turn starts on the ship's bearing");
            }
            if (n > 1) {
                double gap = (sweep[0] - sweeps.get(n - 2)[0]) * SimStep.SECONDS;
                double slot = sweeps.get(n - 2)[1] == 1 ? 6.0 : 4.5;
                assertEquals(slot, gap, SimStep.SECONDS, "telegraph to telegraph after sweep " + (n - 1));
            }
        }
        assertEquals(2 * Math.PI, Math.abs(fullSpans.get(0)), 1e-9);
        assertEquals(-fullSpans.get(0), fullSpans.get(1), 1e-9, "the full turns alternate");
        // The arcs sweep one way and the next the other.
        assertEquals(-(sweeps.get(0)[3] - sweeps.get(0)[2]), sweeps.get(1)[3] - sweeps.get(1)[2], 1e-9);
    }

    @Test
    void hardTurnsEverySecond_easyNever() {
        List<double[]> hard = telegraphs(new EnemySpec.GroundSweep(2, 3.0, 1.0, 6.0), 30, 240, 100);
        for (int n = 1; n <= hard.size(); n++) {
            assertEquals(n % 2 == 0, hard.get(n - 1)[1] == 1, "hard sweep " + n);
        }
        assertTrue(telegraphs(EnemySpec.GroundSweep.ARCS_ONLY, 30, 240, 100).stream()
                .noneMatch(s -> s[1] == 1));
    }

    /**
     * M9 part E, step E3b (QE-3 a, pending round 65): a placement's first full turn (Level 32's towers) makes the first
     * sweep a full turn, after its 1.0 s first delay, then every third after it (hard every second), the full turns'
     * direction alternating from the first; without full turns (easy) it changes nothing.
     */
    @Test
    void aFirstFullTurnComesFirst_thenEveryNthAfterIt() {
        EnemySpec.GroundSweep first = Act5ESpecs.FULL_TURNS.withFirstFullTurn();
        List<double[]> sweeps = telegraphs(first, 40, 240, 100);
        assertTrue(sweeps.size() >= 7, "" + sweeps.size());
        assertEquals(SimStep.ticks(SWEEP.firstDelaySeconds()), (int) sweeps.getFirst()[0], "its first delay");
        List<Double> fullSpans = new ArrayList<>();
        for (int n = 1; n <= sweeps.size(); n++) {
            double[] sweep = sweeps.get(n - 1);
            assertEquals((n - 1) % 3 == 0, sweep[1] == 1, "sweep " + n);
            if (sweep[1] == 1) {
                fullSpans.add(sweep[3] - sweep[2]);
                assertEquals(0, sweep[2], 1e-9, "the start ray on the ship's bearing");
            }
            if (n > 1) {
                double gap = (sweep[0] - sweeps.get(n - 2)[0]) * SimStep.SECONDS;
                assertEquals(sweeps.get(n - 2)[1] == 1 ? 6.0 : 4.5, gap, SimStep.SECONDS, "after sweep " + (n - 1));
            }
        }
        assertEquals(2 * Math.PI, fullSpans.get(0), 1e-9, "the first full turn's sense as without it");
        assertEquals(-fullSpans.get(0), fullSpans.get(1), 1e-9, "the full turns alternate");
        List<double[]> hard = telegraphs(new EnemySpec.GroundSweep(2, 3.0, 1.0, 6.0).withFirstFullTurn(), 30, 240, 100);
        for (int n = 1; n <= hard.size(); n++) {
            assertEquals(n % 2 == 1, hard.get(n - 1)[1] == 1, "hard sweep " + n);
        }
        assertTrue(telegraphs(EnemySpec.GroundSweep.ARCS_ONLY.withFirstFullTurn(), 30, 240, 100).stream()
                .noneMatch(s -> s[1] == 1));
    }

    @Test
    void aFullTurnsRingTelegraphLasts1sAndItsBeamTurns360In3s() {
        GroundSweepBeam beam = new GroundSweepBeam();
        beam.clear();
        int telegraph = 0;
        int live = 0;
        double first = Double.NaN;
        double last = Double.NaN;
        for (int step = 1; step <= SimStep.ticks(20); step++) {
            beam.step(SWEEP, Act5ESpecs.FULL_TURNS, true, 240, 300, 240, 100);
            if (beam.count() == 3 && beam.fullTurn()) {
                telegraph += beam.telegraphing() ? 1 : 0;
                if (beam.sweeping()) {
                    live++;
                    first = Double.isNaN(first) ? beam.beam(0) : first;
                    last = beam.beam(0);
                }
            }
        }
        assertEquals(SimStep.ticks(1.0), telegraph);
        assertEquals(SimStep.ticks(3.0), live);
        assertEquals(2 * Math.PI, Math.abs(last - first), 0.05, "the whole circle");
    }

    @Test
    void itWaitsOffTheFieldAndTheEmpCutsALiveBeam() {
        GroundSweepBeam beam = new GroundSweepBeam();
        beam.clear();
        for (int step = 0; step < SimStep.ticks(5); step++) {
            beam.step(SWEEP, Act5ESpecs.FULL_TURNS, false, 240, 600, 240, 100);
        }
        assertEquals(0, beam.count(), "nothing before it comes onto the field");
        while (!beam.sweeping()) {
            beam.step(SWEEP, Act5ESpecs.FULL_TURNS, true, 240, 300, 240, 100);
        }
        beam.cut();
        assertFalse(beam.running(), "cut mid-sweep");
        beam.step(SWEEP, Act5ESpecs.FULL_TURNS, true, 240, 300, 240, 100);
        assertTrue(beam.telegraphing(), "the next telegraph as soon as the stun is over");
        assertEquals(2, beam.count());
    }

    @Test
    void onTheFieldItHitsTheShipOncePerSweep() {
        LevelScript level =
                Act5ESpecs.slow(List.of(new GroundUnit(0, 240, Act5ESpecs.SENTINEL, -1)), List.of(), List.of());
        Sortie sortie = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int fired = 0;
        int hits = 0;
        boolean ring = false;
        for (int step = 0; step < SimStep.ticks(24); step++) {
            // Up near the tower (it comes down at 10 px/s), then hold still.
            sortie.step(sortie.ship().y() < 380 ? Command.UP.bit() : 0);
            fired += sortie.events().count(SimEvents.Type.SWEEP_FIRED);
            hits += sortie.events().count(SimEvents.Type.SWEEP_HIT);
            Enemy tower = PartCSpecs.find(sortie, "sentinel-tower");
            ring |= tower != null && tower.telegraphing() && tower.sweepFullTurn();
        }
        assertTrue(fired >= 4, "" + fired);
        assertEquals(fired, hits, "each sweep across the still ship hits it once");
        assertTrue(ring, "a full turn's ring telegraph showed");
    }

    @Test
    void aGroundSweepIsAFixedGroundUnitsSweep() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5ESpecs.COLLAR.withGroundSweep(Optional.of(Act5ESpecs.FULL_TURNS)));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.GroundSweep(3, 3, 1, 3.5));
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.NEEDLER
                .withSweep(Optional.of(SWEEP))
                .withGroundSweep(Optional.of(Act5ESpecs.FULL_TURNS)));
    }
}
