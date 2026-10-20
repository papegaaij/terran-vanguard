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
 * Round 41, Level 17's domes after the user's decision of 2026-10-19 ("Both", pt3-findings item 4): the {@code
 * standing} secondary ("No dome lost"), met when the last dome leaves the bottom edge with every one still
 * standing, hits allowed, failed at the first one broken; and the bomb run's run-in, the time a hooked bomber
 * flies on the play field before it may drop its load.
 */
class DomesStandingTest {
    private static final Secondary STANDING = Secondary.standing(100, "DOMES");
    /** Eden's footprint leaves the bottom edge: its centre 72 px below it. */
    private static final double EDEN_GONE = SiegeSpecs.EDEN_T + (PlayField.HEIGHT + 144) / SiegeSpecs.SPEED;

    private static LevelScript level(List<WaveSpec> waves, ObjectiveSpec.GroundAllies allies) {
        return SiegeSpecs.level(waves, List.of(), STANDING, Optional.of(allies));
    }

    /** {@code count} hooked bombers on the first dome (3 hits break it). */
    private static List<WaveSpec> bombers(int count) {
        return List.of(SiegeSpecs.bombers(
                0, WaveSpec.Formation.LINE_ABREAST, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE));
    }

    @Test
    void metWhenTheLastDomeLeavesTheBottomEdgeStandingThoughHit() {
        Sortie sortie = SiegeSpecs.sortie(level(bombers(2), SiegeSpecs.hookedDomes()));
        double metAt = Double.NaN;
        int credits = 0;
        while (sortie.levelSeconds() < SiegeSpecs.LENGTH - 1) {
            int before = sortie.credits();
            sortie.step(Command.NONE);
            assertEquals(0, count(sortie, SimEvents.Type.OBJECTIVE_FAILED), "hits are allowed");
            if (count(sortie, SimEvents.Type.OBJECTIVE_MET) > 0) {
                assertTrue(Double.isNaN(metAt), "once");
                metAt = sortie.levelSeconds();
                credits = sortie.credits() - before;
            }
        }
        assertEquals(2, sortie.ally(0).hitsTaken(), 1e-9, "the first dome took two of its three hits");
        assertTrue(sortie.ally(0).alive());
        assertEquals(EDEN_GONE, metAt, 2 * SimStep.SECONDS, "as Eden's footprint leaves the bottom edge");
        assertEquals(100, credits);
        assertTrue(sortie.secondaryMet());
        assertFalse(sortie.untouchedFailed());
        assertEquals(3, sortie.alliesStanding(), "DOMES STANDING 3 / 3");
        assertEquals(2, sortie.alliesUntouched());
    }

    @Test
    void failedAtOnceWhenTheFirstDomeBreaks() {
        Sortie sortie = SiegeSpecs.sortie(level(bombers(3), SiegeSpecs.hookedDomes()));
        boolean failed = false;
        while (sortie.levelSeconds() < 12 && !failed) {
            sortie.step(Command.NONE);
            failed = count(sortie, SimEvents.Type.OBJECTIVE_FAILED) > 0;
            if (failed) {
                assertEquals(1, count(sortie, SimEvents.Type.ALLY_LOST), "in the step it breaks");
            } else {
                assertTrue(sortie.ally(0).alive());
            }
        }
        assertTrue(failed);
        assertTrue(sortie.untouchedFailed(), "the ground allies' objective failed");
        while (sortie.levelSeconds() < SiegeSpecs.LENGTH - 1) {
            sortie.step(Command.NONE);
            assertEquals(0, count(sortie, SimEvents.Type.OBJECTIVE_MET));
            assertEquals(0, count(sortie, SimEvents.Type.OBJECTIVE_FAILED), "failed once");
        }
        assertFalse(sortie.secondaryMet());
        assertEquals(2, sortie.alliesStanding());
    }

    @Test
    void aStandingObjectiveJudgesTheGroundAlliesAndNothingElse() {
        assertFalse(STANDING.none());
        assertTrue(STANDING.standing());
        assertFalse(STANDING.untouched());
        assertTrue(STANDING.byGroundAllies());
        assertThrows(
                IllegalArgumentException.class,
                () -> new Secondary(
                        0, 100, List.of(), "", List.of(), "DOMES", "", List.of(), -1, "", false, false, 0, false, true,
                        true),
                "not untouched and standing at once");
        assertThrows(
                IllegalArgumentException.class,
                () -> new Secondary(
                        0.5, 100, List.of(), "", List.of(), "DOMES", "", List.of(), -1, "", false, false, 0, false,
                        false, true));
        LevelScript without = SiegeSpecs.level(List.of(), List.of(), STANDING, Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> SiegeSpecs.sortie(without));
        assertFalse(Secondary.untouched(100, "DOMES").standing(), "untouched is not standing");
    }

    // --- the run-in ------------------------------------------------------------------------------------

    @Test
    void aBomberHoldsItsLoadThroughItsRunInOnThePlayField() {
        // A side entry steers across its entry row at once: without a run-in it bombs as soon as it is over a dome.
        double[] entered = new double[1];
        double without = firstDrop(SiegeSpecs.hookedDomes(), entered);
        double flown = without - entered[0];
        assertTrue(flown > 0, "it drops once over the dome: " + flown);
        double runIn = flown + 0.5;
        double with = firstDrop(SiegeSpecs.hookedDomes().withBombRunIn(runIn), entered);
        assertTrue(
                with - entered[0] >= runIn - SimStep.SECONDS,
                "not before its run-in of " + runIn + " s on the field: " + (with - entered[0]));
    }

    @Test
    void theRunInCountsOnlyOnThePlayFieldAndDefaultsToNone() {
        BombRun run = new BombRun();
        run.start(380, 0, 100, 3);
        assertFalse(run.armed());
        run.flown(false);
        run.flown(true);
        run.flown(true);
        assertFalse(run.armed(), "two steps on the field of three");
        run.flown(true);
        assertTrue(run.armed());
        run.start(380, 0, 100);
        assertTrue(run.armed(), "without a run-in at once");
        assertEquals(0, SiegeSpecs.hookedDomes().bombRunIn(), 1e-9);
        assertThrows(
                IllegalArgumentException.class, () -> SiegeSpecs.hookedDomes().withBombRunIn(-1));
    }

    /** The level seconds of the first bomb dropped by a bomber entering from the right at 1 s; {@code entered[0]} when it came on the field. */
    private static double firstDrop(ObjectiveSpec.GroundAllies allies, double[] entered) {
        Sortie sortie = SiegeSpecs.sortie(SiegeSpecs.level(
                List.of(new WaveSpec(
                        1,
                        WaveSpec.Formation.STREAM,
                        SiegeSpecs.BOMBER,
                        1,
                        WaveSpec.Entry.SIDES,
                        WaveSpec.Edge.RIGHT,
                        Optional.empty(),
                        Optional.empty(),
                        1,
                        Optional.empty(),
                        Optional.of(1.0),
                        List.of())),
                List.of(),
                Secondary.NONE,
                Optional.of(allies)));
        entered[0] = Double.NaN;
        while (sortie.levelSeconds() < 30) {
            sortie.step(Command.NONE);
            Enemy bomber = SiegeSpecs.find(sortie, SiegeSpecs.BOMBER);
            if (Double.isNaN(entered[0])
                    && bomber != null
                    && PlayField.overlaps(bomber.x(), bomber.y(), bomber.hitbox())) {
                entered[0] = sortie.levelSeconds();
            }
            if (count(sortie, SimEvents.Type.BOMB_DROPPED) > 0) {
                return sortie.levelSeconds();
            }
        }
        throw new AssertionError("no bomb dropped");
    }
}
