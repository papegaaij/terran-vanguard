package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1 (design/campaign Level 48, rear rules: "a rear Chimera V's tip ≥ 120 px off the ship's x at the
 * warning"; {@link RearLanes}): a rear wave with {@code lane: away} comes up in the middle of the field's half the ship
 * was not in as its 3 s warning started (the tip at least 120 px off its x), whatever the ship does after; the warning's
 * arrow shows there while it warns; the same every time; a front wave cannot have it.
 */
class RearLaneTest {
    private static final double WAVE_T = 4;

    private static LevelScript level() {
        WaveSpec wave = WaveSpec.builder(
                        WAVE_T,
                        WaveSpec.Formation.LINE_ABREAST,
                        TestSpecs.SKITTER,
                        1,
                        WaveSpec.Entry.REAR,
                        WaveSpec.Edge.NONE)
                .warningSeconds(Optional.of(3.0))
                .lane(WaveSpec.Lane.AWAY)
                .build();
        return Act7ASpecs.level(12, List.of(wave), Optional.empty());
    }

    /** Flies {@code first} until the warning starts, then {@code after}; returns {ship x at the warning, lane, x at entry}. */
    private static double[] fly(int first, int after) {
        Sortie sortie = Act7ASpecs.sortie(level());
        RearLanes lanes = sortie.rearLanes().orElseThrow();
        double shipAtWarning = Double.NaN;
        double lane = Double.NaN;
        for (int i = 0; i < SimStep.ticks(WAVE_T + 0.5); i++) {
            boolean warned = !Double.isNaN(lanes.laneX());
            sortie.step(warned ? after : first);
            if (!warned && !Double.isNaN(lanes.laneX())) {
                shipAtWarning = sortie.ship().x();
                lane = lanes.laneX();
                assertEquals(SimStep.ticks(WAVE_T - 3), sortie.levelTick, "drawn as the warning starts");
            }
            Enemy unit = Act7ASpecs.find(sortie, "skitter");
            if (unit != null) {
                assertTrue(Double.isNaN(lanes.laneX()), "no arrow once it is in");
                return new double[] {shipAtWarning, lane, unit.x()};
            }
        }
        throw new AssertionError("the wave never came");
    }

    @Test
    void itComesUpInTheHalfTheShipWasNotInAsItsWarningStarted() {
        double[] left = fly(Command.LEFT.bit(), Command.RIGHT.bit());
        assertTrue(left[0] < PlayField.WIDTH / 2.0, "the ship on the left");
        assertEquals(360, left[1], 1e-9, "the right half's middle");
        assertEquals(360, left[2], 1e-6, "its tip at the lane, though the ship flew right after");
        assertTrue(left[2] - left[0] >= RearLanes.LEAST_OFF, "≥ 120 px off");
        double[] right = fly(Command.RIGHT.bit(), Command.LEFT.bit());
        assertTrue(right[0] >= PlayField.WIDTH / 2.0, "the ship on the right");
        assertEquals(120, right[2], 1e-6, "the left half's middle");
        assertTrue(right[0] - right[2] >= RearLanes.LEAST_OFF, "≥ 120 px off");
    }

    @Test
    void theLaneIsAtLeastOneTwentyOffForAnyShipX() {
        for (double x = 0; x <= PlayField.WIDTH; x += 0.5) {
            assertTrue(Math.abs(RearLanes.lane(x) - x) >= RearLanes.LEAST_OFF, "at " + x);
        }
    }

    @Test
    void itRunsTheSameEveryTimeAndOnlyARearWaveHasIt() {
        Sortie one = Act7ASpecs.sortie(level());
        Sortie two = Act7ASpecs.sortie(level());
        for (int i = 0; i < SimStep.ticks(6); i++) {
            int command = i < 30 ? Command.LEFT.bit() : 0;
            one.step(command);
            two.step(command);
            assertEquals(one.stateHash(), two.stateHash());
        }
        assertFalse(Act7ASpecs.sortie(Act7ASpecs.level(5, List.of(), Optional.empty()))
                .rearLanes()
                .isPresent());
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        1, WaveSpec.Formation.SINGLE, TestSpecs.SKITTER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .lane(WaveSpec.Lane.AWAY)
                .build());
    }
}
