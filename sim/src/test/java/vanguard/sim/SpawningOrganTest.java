package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part C, step CD1 (design/enemies/ground/spawning-organ; m11c-gaps.md §1 #1, §4): the spawning organ is a periodic
 * spawner whose first release comes 1.5 s after its centre crosses the top edge, then every 3 s, alternating a whirl
 * cluster of 6 Whirl Seeds after the pod's 0.5 s swell ({@code ORGAN_SWELL}, {@code ORGAN_RELEASE}) and a Skitter pair
 * after the 0.5 s iris; a release due while the ship is within its shut distance waits (never skipped); the EMP leaves
 * its clock running.
 */
class SpawningOrganTest {
    /** The organ at {@code x}, entering at 0 s, the ground scrolling {@code speed} px/s. */
    private static Sortie sortie(double x, double speed, double shutWithin) {
        return Act7CDSpecs.sortie(Act7CDSpecs.level(
                30,
                speed,
                List.of(),
                List.of(new GroundUnit(0, x, Act7CDSpecs.organ(shutWithin), -1)),
                VrellLevel.NONE));
    }

    /** The steps of the four spawner events over {@code steps} steps flown with {@code commands}. */
    private record Run(List<Integer> swells, List<Integer> clusters, List<Integer> irises, List<Integer> pairs) {}

    private static Run fly(Sortie sortie, int steps, java.util.function.IntUnaryOperator commands) {
        Run run = new Run(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (int tick = 0; tick < steps; tick++) {
            sortie.step(commands.applyAsInt(tick));
            SimEvents events = sortie.events();
            if (events.count(SimEvents.Type.ORGAN_SWELL) > 0) {
                run.swells().add(tick);
            }
            if (events.count(SimEvents.Type.ORGAN_RELEASE) > 0) {
                run.clusters().add(tick);
            }
            if (events.count(SimEvents.Type.SPAWN_TELEGRAPH) > 0) {
                run.irises().add(tick);
            }
            if (events.count(SimEvents.Type.SPAWN_RELEASED) > 0) {
                run.pairs().add(tick);
            }
        }
        return run;
    }

    @Test
    void itsFirstReleaseComesOneAndAHalfSecondsInThenItAlternatesEveryThreeSeconds() {
        // At 100 px/s its centre (30 px above the field) crosses the top edge at 0.3 s; it is gone off the bottom at
        // about 6.3 s: two releases while it is on the screen, never more.
        Run run = fly(sortie(60, 100, 120), SimStep.ticks(8), tick -> Command.NONE);
        int crossing = SimStep.ticks(0.3);
        int telegraph = SimStep.ticks(0.5);
        assertEquals(1, run.clusters().size(), "the cluster first");
        assertEquals(1, run.pairs().size(), "then the pair");
        assertEquals(crossing + SimStep.ticks(1.5), run.clusters().getFirst(), 2);
        assertEquals(run.clusters().getFirst() + SimStep.ticks(3), run.pairs().getFirst());
        assertEquals(List.of(run.clusters().getFirst() - telegraph), run.swells(), "the pod swells 0.5 s ahead");
        assertEquals(List.of(run.pairs().getFirst() - telegraph), run.irises(), "the iris opens 0.5 s ahead");
    }

    @Test
    void itsClusterSpiralsOutOfItsPointAndItsPairFansOut() {
        Sortie sortie = sortie(60, 100, 120);
        while (sortie.events().count(SimEvents.Type.ORGAN_RELEASE) == 0) {
            sortie.step(Command.NONE);
        }
        Enemy organ = Act7CDSpecs.find(sortie, "spawning-organ");
        assertNotNull(organ);
        assertTrue(organ.spec().uncounted() && !organ.spec().hardened(), "uncounted, not hardened");
        int seeds = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            if (unit.spec().slug().equals("whirl-seed")) {
                seeds++;
                assertEquals(Enemy.Phase.SPIRAL, unit.phase, "spiralling out");
                assertEquals(organ.x(), unit.x(), 1e-9);
                assertEquals(organ.y(), unit.y(), 1e-9);
            }
        }
        assertEquals(6, seeds, "a cluster of 6");
        while (sortie.events().count(SimEvents.Type.SPAWN_RELEASED) == 0) {
            sortie.step(Command.NONE);
        }
        assertEquals(2, Act7CDSpecs.count(sortie, "skitter"), "then a Skitter pair");
    }

    @Test
    void aReleaseDueWithTheShipNearWaitsAndTheNextComesACycleAfterIt() {
        // At 30 px/s it crosses at 1 s; its first release is due at 2.5 s, its centre 45 px into the field, 423 px
        // from the ship: inside a 450 px shut distance. The ship then flies to the bottom right, out of it.
        int due = SimStep.ticks(1.0) + SimStep.ticks(1.5);
        int leave = due + SimStep.ticks(1);
        Sortie sortie = sortie(100, 30, 450);
        Run run = fly(
                sortie,
                SimStep.ticks(12),
                tick -> tick < leave ? Command.NONE : Command.RIGHT.bit() | Command.DOWN.bit());
        assertTrue(run.clusters().getFirst() > due + SimStep.ticks(1), "held past its time, never skipped");
        assertEquals(
                run.swells().getFirst() + SimStep.ticks(0.5), run.clusters().getFirst());
        assertEquals(run.clusters().getFirst() + SimStep.ticks(3), run.pairs().getFirst(), "the next a cycle later");
    }

    @Test
    void theEmpLeavesItsReleaseClockRunning() {
        Run free = fly(sortie(60, 100, 120), SimStep.ticks(3), tick -> Command.NONE);
        Sortie stunned = sortie(60, 100, 120);
        List<Integer> clusters = new ArrayList<>();
        for (int tick = 0; tick < SimStep.ticks(3); tick++) {
            Enemy organ = Act7CDSpecs.find(stunned, "spawning-organ");
            if (organ != null) {
                // Held stunned throughout, as an EMP Burst would.
                organ.empStun = SimStep.ticks(5);
            }
            stunned.step(Command.NONE);
            if (stunned.events().count(SimEvents.Type.ORGAN_RELEASE) > 0) {
                clusters.add(tick);
            }
        }
        assertEquals(free.clusters(), clusters);
    }

    @Test
    void theLeavingBlinkCountsAPrimaryTargetInTheFieldsBottomThird() {
        for (boolean bottomThird : new boolean[] {true, false}) {
            LevelScript level = Act7CDSpecs.level(
                            30,
                            100,
                            List.of(),
                            List.of(new GroundUnit(0, 60, Act7CDSpecs.organ(120), 0)),
                            VrellLevel.NONE.withBottomThird(bottomThird))
                    .toBuilder()
                    .objectives(ObjectiveSpec.NONE.withTargets(List.of("O1")))
                    .build();
            Sortie sortie = Act7CDSpecs.sortie(level);
            boolean blinked = false;
            for (int tick = 0; tick < SimStep.ticks(5.9); tick++) {
                sortie.step(Command.NONE);
                Enemy organ = Act7CDSpecs.find(sortie, "spawning-organ");
                boolean low = organ != null && organ.y() < PlayField.HEIGHT / 3.0;
                assertEquals(bottomThird && low ? 1 : 0, sortie.leaving(), "at " + tick);
                blinked |= low;
            }
            assertTrue(blinked);
        }
    }
}
