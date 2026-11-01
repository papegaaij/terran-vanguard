package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part G, step G1a (design/campaign Level 41, the secondary {@code FILES}; {@link Uplinks}): an archive core opens
 * where its hatch broke; the ship's centre within its radius uploads its file over its time in total (paused outside,
 * never reset); a core off the bottom edge, or under a hatch that left unbroken, is lost; a retry starts every core
 * over; a {@code files} secondary is judged at the level's end; no cores, no pass.
 */
class UplinkTest {
    private static int run(Sortie sortie, double seconds, int commands, SimEvents.Type type) {
        return Act6BSpecs.run(sortie, SimStep.ticks(seconds), commands, type);
    }

    /** Up to the core's row at 10 px/s, the hatch broken just above the ship. */
    private static Sortie atTheCore(int files) {
        Sortie sortie = TestSpecs.sortie(Act6GSpecs.uplinkLevel(10, files, Ship.START_X));
        run(sortie, 1.6, Command.UP.bit(), SimEvents.Type.UPLINK_OPEN);
        assertTrue(sortie.ship().y() > 480, "the ship flew up to the top");
        assertTrue(Act6GSpecs.breakHatch(sortie, 0));
        assertEquals(1, sortie.events().count(SimEvents.Type.UPLINK_OPEN));
        assertEquals(Uplinks.OPEN, sortie.uplinks().state(0));
        return sortie;
    }

    @Test
    void theShipUploadsOverItsTimeInTotalPausedOutside() {
        Sortie sortie = atTheCore(0);
        assertEquals(1, run(sortie, 1, 0, SimEvents.Type.UPLINK_PROGRESS), "the upload starts");
        assertTrue(sortie.uplinks().uploading(0));
        double half = sortie.uplinks().share(0);
        assertEquals(0.5, half, 0.05);
        run(sortie, 0.5, Command.RIGHT.bit(), SimEvents.Type.UPLINK_PROGRESS);
        double left = sortie.uplinks().share(0);
        assertTrue(!sortie.uplinks().uploading(0) && left < 0.75, "out of the radius");
        assertEquals(0, run(sortie, 1, 0, SimEvents.Type.FILE_IN));
        assertEquals(left, sortie.uplinks().share(0), "paused outside, never reset");
        int resumed = 0;
        int in = 0;
        for (int i = 0; i < SimStep.ticks(4) && in == 0; i++) {
            sortie.step(sortie.ship().x() > Ship.START_X + 2 ? Command.LEFT.bit() : 0);
            resumed += sortie.events().count(SimEvents.Type.UPLINK_PROGRESS);
            in += sortie.events().count(SimEvents.Type.FILE_IN);
            if (in > 0) {
                assertEquals(1, Act6GSpecs.radioed(sortie, 0), "Varga's first file line");
                assertEquals(1, sortie.events().value(index(sortie, SimEvents.Type.FILE_IN)), "the first file");
            }
        }
        assertEquals(1, resumed, "it resumes where it paused");
        assertEquals(1, in);
        assertEquals(Uplinks.IN, sortie.uplinks().state(0));
        assertEquals(1, sortie.uplinks().filesIn());
        assertEquals(1, sortie.uplinks().share(0));
    }

    private static int index(Sortie sortie, SimEvents.Type type) {
        for (int e = 0; e < sortie.events().size(); e++) {
            if (sortie.events().type(e) == type) {
                return e;
            }
        }
        throw new AssertionError("no " + type);
    }

    @Test
    void aCoreOffTheBottomEdgeOrUnderAHatchGoneIsLost() {
        Sortie sortie = TestSpecs.sortie(Act6GSpecs.uplinkLevel(200, 0, 60, 420));
        sortie.step(0);
        assertTrue(Act6GSpecs.breakHatch(sortie, 0));
        assertEquals(2, run(sortie, 4, 0, SimEvents.Type.FILE_LOST), "the open core and the unbroken hatch");
        assertEquals(Uplinks.LOST, sortie.uplinks().state(0));
        assertEquals(Uplinks.LOST, sortie.uplinks().state(1));
        assertEquals(0, sortie.uplinks().filesIn());
        assertEquals(2, sortie.uplinks().lost());
    }

    @Test
    void aRetryRepeatsItAndTheSecondaryIsJudgedAtTheEnd() {
        Sortie sortie = atTheCore(1);
        assertEquals(1, run(sortie, 3, 0, SimEvents.Type.FILE_IN));
        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(0);
        assertEquals(0, sortie.uplinks().filesIn(), "a retry starts every core over");
        assertEquals(Uplinks.SHUT, sortie.uplinks().state(0));
        assertEquals(0, run(sortie, 31, 0, SimEvents.Type.OBJECTIVE_MET), "no file in: not met");
        assertTrue(sortie.complete());

        Sortie met = atTheCore(1);
        assertEquals(0, run(met, 3, 0, SimEvents.Type.OBJECTIVE_MET), "not at the file: at the end");
        assertEquals(1, met.uplinks().filesIn());
        assertEquals(1, run(met, 29, 0, SimEvents.Type.OBJECTIVE_MET), "met at the end with its file in");
        assertTrue(met.complete());
    }

    @Test
    void noCoresNoPassAndTheHatchesAreChecked() {
        assertNull(TestSpecs.sortie(TestSpecs.level(10, List.of())).uplinks());
        LevelScript level = Act6GSpecs.uplinkLevel(10, 1, Ship.START_X);
        assertThrows(
                IllegalArgumentException.class,
                () -> level.toBuilder()
                        .uplinks(List.of(new UplinkSpec(1, 2, 40)))
                        .build(),
                "a hatch among the ground objects");
        assertThrows(
                IllegalArgumentException.class,
                () -> level.toBuilder()
                        .groundObjects(List.of(Act6GSpecs.hatch(0, 240, 0)))
                        .build(),
                "a destructible with HP");
        assertThrows(
                IllegalArgumentException.class,
                () -> Act6GSpecs.uplinkLevel(10, 2, Ship.START_X),
                "no more files than cores");
        assertThrows(IllegalArgumentException.class, () -> new UplinkSpec(0, 0, 40));
        assertThrows(
                IllegalArgumentException.class,
                () -> Secondary.builder().files(3).killRatio(0.5).build());
    }
}
