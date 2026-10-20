package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.SiegeSpecs.count;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part D's {@code untouched} secondary (design/campaign Level 17, Objective; Q.25c): failed at once at the
 * first hit on any ground ally, met when the last of them (Eden) leaves the bottom edge with none hit; the
 * debrief's count of the domes never hit; a retry starts it over.
 */
class UntouchedTest {
    private static final Secondary UNTOUCHED = Secondary.untouched(100, "DOMES");
    /** Eden's footprint leaves the bottom edge: its centre 72 px below it. */
    private static final double EDEN_GONE = SiegeSpecs.EDEN_T + (PlayField.HEIGHT + 144) / SiegeSpecs.SPEED;

    private static LevelScript level(List<WaveSpec> waves) {
        return SiegeSpecs.level(waves, List.of(), UNTOUCHED, Optional.of(SiegeSpecs.hookedDomes()));
    }

    /** One hooked bomber on the first dome. */
    private static List<WaveSpec> oneBomber() {
        return List.of(
                SiegeSpecs.bombers(0, WaveSpec.Formation.LINE_ABREAST, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE));
    }

    @Test
    void metWhenTheLastDomeLeavesTheBottomEdgeWithNoneHit() {
        Sortie sortie = SiegeSpecs.sortie(level(List.of()));
        double metAt = Double.NaN;
        int credits = 0;
        while (sortie.levelSeconds() < SiegeSpecs.LENGTH - 1) {
            int before = sortie.credits();
            sortie.step(Command.NONE);
            if (count(sortie, SimEvents.Type.OBJECTIVE_MET) > 0) {
                assertTrue(Double.isNaN(metAt), "once");
                metAt = sortie.levelSeconds();
                credits = sortie.credits() - before;
            }
        }
        assertEquals(EDEN_GONE, metAt, 2 * SimStep.SECONDS, "as Eden's footprint leaves the bottom edge");
        assertEquals(100, credits);
        assertTrue(sortie.secondaryMet());
        assertFalse(sortie.untouchedFailed());
        assertEquals(3, sortie.alliesUntouched());
    }

    @Test
    void failedAtOnceAtTheFirstHit() {
        Sortie sortie = SiegeSpecs.sortie(level(oneBomber()));
        boolean failed = false;
        while (sortie.levelSeconds() < 10 && !failed) {
            sortie.step(Command.NONE);
            failed = count(sortie, SimEvents.Type.OBJECTIVE_FAILED) > 0;
            if (failed) {
                assertEquals(1, count(sortie, SimEvents.Type.ALLY_HIT), "in the step of the hit");
                assertEquals(1, count(sortie, SimEvents.Type.BOMB_DROPPED));
            }
        }
        assertTrue(failed);
        assertTrue(sortie.untouchedFailed());
        assertTrue(sortie.ally(0).alive(), "one hit of three");
        while (sortie.levelSeconds() < SiegeSpecs.LENGTH - 1) {
            sortie.step(Command.NONE);
            assertEquals(0, count(sortie, SimEvents.Type.OBJECTIVE_MET));
            assertEquals(0, count(sortie, SimEvents.Type.OBJECTIVE_FAILED), "failed once");
        }
        assertFalse(sortie.secondaryMet());
        assertEquals(2, sortie.alliesUntouched(), "DOMES UNTOUCHED 2 / 3");
    }

    @Test
    void aRetryStartsItOver() {
        Sortie sortie = SiegeSpecs.sortie(level(oneBomber()));
        SiegeSpecs.runTo(sortie, 10);
        assertTrue(sortie.untouchedFailed());
        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(Command.NONE);
        assertFalse(sortie.untouchedFailed());
        assertEquals(3, sortie.alliesUntouched());
        assertEquals(0, sortie.ally(0).hitsTaken());
    }

    @Test
    void anUntouchedObjectiveJudgesTheGroundAlliesAndNothingElse() {
        assertFalse(UNTOUCHED.none());
        assertTrue(UNTOUCHED.untouched());
        assertFalse(UNTOUCHED.byEscapes());
        assertThrows(
                IllegalArgumentException.class,
                () -> new Secondary(
                        0.5, 100, List.of(), "", List.of(), "DOMES", "", List.of(), -1, "", false, false, 0, false,
                        true));
        LevelScript without = SiegeSpecs.level(List.of(), List.of(), UNTOUCHED, Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> SiegeSpecs.sortie(without));
        assertFalse(new Secondary(0.5, 100).untouched(), "the shorter constructors are not untouched");
    }
}
