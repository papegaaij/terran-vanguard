package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1b (design/campaign Level 42, The home fleet's cruisers; m10h-gaps.md §1 #15): the cruisers as a
 * list of runs ({@link CruiserRunSpec#runs()}), Level 42's one flank at a time: A on the left (rise 24, run 28–38), B on
 * the right (rise 40, run 44–54), each falling 4 s; broadsides every 3.5 s after a 1.2 s tell from the running flank
 * only, the band on the ship's y and then a random one, never two bands; point defence from the running hull only; no
 * flak; the CDF frigates beside A (presentation only). Level 28's data reads as one run of both hulls (its golden
 * flights pin it unchanged).
 */
class CruiserRunsTest {
    static final CruiserRunSpec.Broadside BROADSIDE =
            new CruiserRunSpec.Broadside(70, 1.2, 7, 260, 6, 3.5, Double.POSITIVE_INFINITY, 100);
    static final List<CruiserRunSpec.Run> RUNS = List.of(
            new CruiserRunSpec.Run(CruiserRunSpec.Flank.LEFT, 24, 28, 38, 4, true),
            new CruiserRunSpec.Run(CruiserRunSpec.Flank.RIGHT, 40, 44, 54, 4, false));
    static final CruiserRunSpec SPEC = CruiserRunSpec.ofRuns(
            RUNS, new Hitbox(220, 640), 110, 370, BROADSIDE, CruiserRunSpec.Flak.none(), CruiserRunTest.DEFENCE);

    private static Sortie sortie() {
        LevelScript level = LevelScript.builder(42, 6)
                .sections(List.of(new Section(70, 200)))
                .hazards(HazardSpec.NONE.withCruisers(Optional.of(SPEC)))
                .build();
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static final SimEvents.Type[] TYPES = {
        SimEvents.Type.CRUISER_RISE,
        SimEvents.Type.BROADSIDE_TELL,
        SimEvents.Type.BROADSIDE,
        SimEvents.Type.FLAK,
        SimEvents.Type.POINT_DEFENCE
    };

    @Test
    void theSpecReadsTheRunsTimesAndLevel28sDataIsOneRunOfBoth() {
        assertEquals(24, SPEC.riseAt());
        assertEquals(28, SPEC.runAt());
        assertEquals(54, SPEC.endAt());
        assertEquals(4, SPEC.fallSeconds());
        List<CruiserRunSpec.Run> l28 = CruiserRunTest.SPEC.runs();
        assertEquals(1, l28.size());
        assertEquals(CruiserRunSpec.Flank.BOTH, l28.getFirst().flank());
        assertEquals(CruiserRunTest.RISE, l28.getFirst().riseAt());
        assertEquals(CruiserRunTest.END, l28.getFirst().endAt());
    }

    @Test
    void aHullFliesOneRunAtATimeInTimeOrder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CruiserRunSpec.ofRuns(
                        List.of(
                                RUNS.getFirst(),
                                new CruiserRunSpec.Run(CruiserRunSpec.Flank.LEFT, 30, 32, 40, 4, false)),
                        new Hitbox(220, 640),
                        110,
                        370,
                        BROADSIDE,
                        CruiserRunSpec.Flak.none(),
                        CruiserRunTest.DEFENCE),
                "the left hull twice at once");
        assertThrows(
                IllegalArgumentException.class,
                () -> CruiserRunSpec.ofRuns(
                        List.of(RUNS.get(1), RUNS.getFirst()),
                        new Hitbox(220, 640),
                        110,
                        370,
                        BROADSIDE,
                        CruiserRunSpec.Flak.none(),
                        CruiserRunTest.DEFENCE),
                "in time order");
    }

    @Test
    void oneFlankAtATimeItsBroadsidesNeverTwoBandsNoFlak() {
        Sortie sortie = sortie();
        CruiserRun run = sortie.cruisers().orElseThrow();
        List<CruiserRunTest.Event> events = CruiserRunTest.record(sortie, 62, 0, TYPES);
        List<CruiserRunTest.Event> rises = of(events, SimEvents.Type.CRUISER_RISE);
        assertEquals(2, rises.size());
        assertEquals(SimStep.ticks(24), rises.get(0).tick());
        assertEquals(CruiserRun.LEFT, rises.get(0).value());
        assertEquals(SimStep.ticks(40), rises.get(1).tick());
        assertEquals(CruiserRun.RIGHT, rises.get(1).value());
        List<CruiserRunTest.Event> tells = of(events, SimEvents.Type.BROADSIDE_TELL);
        assertTrue(tells.size() >= 4, "broadsides: " + tells.size());
        int left = 0;
        int right = 0;
        for (CruiserRunTest.Event tell : tells) {
            boolean inA = tell.tick() >= SimStep.ticks(28) && tell.tick() <= SimStep.ticks(38);
            boolean inB = tell.tick() >= SimStep.ticks(44) && tell.tick() <= SimStep.ticks(54);
            assertTrue(inA || inB, "only while a run runs: " + tell);
            assertEquals(inA ? CruiserRun.LEFT : CruiserRun.RIGHT, tell.value(), "from the running flank: " + tell);
            left += inA ? 1 : 0;
            right += inB ? 1 : 0;
        }
        assertEquals(3, left, "every 3.5 s over A's 10 s (the last one's volley by its end)");
        assertEquals(3, right);
        assertEquals(SimStep.ticks(28), tells.get(0).tick(), "the first tell at the run's start");
        assertEquals(SimStep.ticks(44), tells.get(left).tick(), "B starts its own clock");
        for (int i = 1; i < tells.size(); i++) {
            assertTrue(tells.get(i).tick() - tells.get(i - 1).tick() >= SimStep.ticks(1.2), "never two bands");
        }
        assertTrue(of(events, SimEvents.Type.FLAK).isEmpty(), "no flak");
        for (CruiserRunTest.Event burst : of(events, SimEvents.Type.POINT_DEFENCE)) {
            boolean inA = burst.tick() <= SimStep.ticks(38) + 1;
            assertEquals(inA ? CruiserRun.LEFT : CruiserRun.RIGHT, burst.value(), "the running hull's turrets");
        }
        assertFalse(of(events, SimEvents.Type.POINT_DEFENCE).isEmpty());
        assertFalse(run.bandTelling(1), "the double band never");
    }

    @Test
    void theFirstBandOfEachRunLiesOnTheShipThenARandomOne() {
        Sortie sortie = sortie();
        List<CruiserRunTest.Event> tells =
                of(CruiserRunTest.record(sortie, 62, 0, TYPES), SimEvents.Type.BROADSIDE_TELL);
        double shipY = sortie.ship().y();
        assertEquals(shipY, tells.get(0).y(), 1e-6, "A's first on the ship");
        assertEquals(shipY, tells.get(2).y(), 1e-6, "its third too");
        assertEquals(shipY, tells.get(3).y(), 1e-6, "B's first on the ship");
    }

    @Test
    void onlyTheRunningHullIsSolidOnTheScreenAndBesideTheFrigates() {
        Sortie sortie = sortie();
        CruiserRun run = sortie.cruisers().orElseThrow();
        CruiserRunTest.record(sortie, 30, 0);
        int at = sortie.levelTick;
        assertTrue(run.solid(CruiserRun.LEFT, at));
        assertFalse(run.solid(CruiserRun.RIGHT, at));
        assertTrue(run.present(CruiserRun.LEFT, at));
        assertFalse(run.present(CruiserRun.RIGHT, at));
        assertTrue(run.frigates(CruiserRun.LEFT, at), "the CDF frigates beside A");
        assertFalse(run.frigates(CruiserRun.RIGHT, at));
        double half = 4;
        assertTrue(run.column(run.hullX(CruiserRun.LEFT), half, 0) < Double.POSITIVE_INFINITY, "A stops a beam");
        assertEquals(Double.POSITIVE_INFINITY, run.column(run.hullX(CruiserRun.RIGHT), half, 0), "B is not there");
        CruiserRunTest.record(sortie, 18, 0);
        at = sortie.levelTick;
        assertFalse(run.present(CruiserRun.LEFT, at), "A fell away");
        assertTrue(run.solid(CruiserRun.RIGHT, at));
        assertEquals(1, run.fallShare(CruiserRun.LEFT, at), 1e-9);
        assertEquals(0, run.fallShare(CruiserRun.RIGHT, at), 1e-9);
        assertEquals(1, run.riseShare(CruiserRun.RIGHT, at), 1e-9);
    }

    @Test
    void aRetryRunsThemTheSameAgain() {
        Sortie first = sortie();
        Sortie second = sortie();
        assertEquals(CruiserRunTest.record(first, 60, 0, TYPES), CruiserRunTest.record(second, 60, 0, TYPES), "seeded");
        first.cruisers().orElseThrow().reset();
    }

    private static List<CruiserRunTest.Event> of(List<CruiserRunTest.Event> events, SimEvents.Type type) {
        return events.stream().filter(e -> e.type() == type).toList();
    }
}
