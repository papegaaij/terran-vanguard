package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (design/world/jovian, Hazards & set pieces: eruptions): a vent erupts a period after its entry
 * and every period while on the screen: a 1.2 s tell, then its rocks rise 0.5 s (harmless), fly out along a 100° fan
 * at 120 easing to 60 px/s for 2.5 s, fall 0.4 s (harmless) and are gone. A flying rock is shootable (6 HP, paying
 * nothing, no kill), breaks on the ship's or Rook's hull; Rook keeps out of a telling vent's fan. M10 part E, step E1
 * (design/campaign Level 39; m10e-gaps.md §1 #1, #2): a vent enters on the level clock but its cycles count the real
 * steps (a hold zone's slowed clock does not slow them, a boss checkpoint's restore included), and its first tell may
 * come at its own delay after the entry ({@code first}).
 */
class EruptionTest {
    /** One vent at x 240 entering at 0 s, erupting every 2 s, three rocks each time. */
    private static final HazardSpec.EruptionSpec SPEC = Act6BSpecs.eruptions(new HazardSpec.Vent(0, 240, 2, 2, 3, 3));

    /** The vents' steps with the ground at 60 px/s; returns the events of the last step. */
    private static SimEvents step(Eruptions eruptions, int tick, SimEvents events) {
        events.clear();
        eruptions.step(tick, tick * 1.0, events);
        return events;
    }

    @Test
    void aVentTellsAPeriodAfterItsEntryThenThrowsItsRocksAlongTheFan() {
        Eruptions eruptions = new Eruptions(SPEC, new SplitMix64(1));
        SimEvents events = new SimEvents(32);
        int tell = -1;
        int thrown = -1;
        int tick = 0;
        for (; thrown < 0; tick++) {
            step(eruptions, tick, events);
            if (events.count(SimEvents.Type.ERUPTION_TELL) > 0) {
                tell = tick;
            }
            if (events.count(SimEvents.Type.VENT_ERUPTED) > 0) {
                thrown = tick;
                assertEquals(3, events.value(0));
            }
        }
        assertEquals(SimStep.ticks(2), tell, "a period after its entry");
        assertEquals(tell + SimStep.ticks(1.2), thrown, "after its tell");
        int[] rocks = new int[3];
        int found = 0;
        for (int r = 0; r < eruptions.rocks() && found < 3; r++) {
            if (eruptions.rockUsed(r)) {
                rocks[found++] = r;
            }
        }
        assertEquals(3, found);
        assertEquals(Eruptions.Phase.RISE, eruptions.rockPhase(rocks[0]));
        assertFalse(eruptions.shootable(rocks[0]), "harmless while rising");
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            step(eruptions, tick++, events);
        }
        assertTrue(eruptions.shootable(rocks[1]), "flying");
        double fromY = eruptions.rockY(rocks[1]);
        double fromX = eruptions.rockX(rocks[1]);
        for (int i = 0; i < SimStep.ticks(2.5) - 1; i++) {
            step(eruptions, tick++, events);
        }
        assertEquals(SPEC.reach(), eruptions.rockY(rocks[1]) - fromY, 3, "straight up, 120 easing to 60 px/s");
        assertEquals(fromX, eruptions.rockX(rocks[1]), 1e-9);
        assertTrue(eruptions.rockX(rocks[0]) < fromX - 100, "the fan's left edge");
        assertTrue(eruptions.rockX(rocks[2]) > fromX + 100, "its right edge");
        step(eruptions, tick++, events);
        assertEquals(Eruptions.Phase.FALL, eruptions.rockPhase(rocks[1]));
        for (int i = 0; i < SimStep.ticks(0.4); i++) {
            step(eruptions, tick++, events);
        }
        assertFalse(eruptions.rockUsed(rocks[1]), "gone after its fall");
    }

    /** A vent stepped to its rocks' flight; returns a flying rock's slot. */
    private static int flying(Eruptions eruptions, SimEvents events) {
        for (int tick = 0; tick < SimStep.ticks(5); tick++) {
            step(eruptions, tick, events);
            for (int r = 0; r < eruptions.rocks(); r++) {
                if (eruptions.shootable(r)) {
                    return r;
                }
            }
        }
        throw new AssertionError("no rock flies");
    }

    @Test
    void aFlyingRockTakesShotsAndBreaksOnAHull() {
        Eruptions eruptions = new Eruptions(SPEC, new SplitMix64(1));
        SimEvents events = new SimEvents(32);
        int r = flying(eruptions, events);
        Pool<Shot> shots = new Pool<>(4, Shot::new, Shot[]::new);
        int hits = 0;
        int broken = 0;
        for (int k = 0; k < 3; k++) {
            shots.obtain().fire(TestSpecs.PULSE, 0, eruptions.rockX(r), eruptions.rockY(r), 0);
            events.clear();
            eruptions.hitBy(shots, events);
            hits += events.count(SimEvents.Type.ROCK_HIT);
            broken += events.count(SimEvents.Type.ROCK_DESTROYED);
        }
        assertEquals(3, hits);
        assertEquals(0, shots.size(), "each shot spent on it");
        assertEquals(1, broken, "6 HP: three 2-damage shots");
        int other = flying(eruptions, events);
        events.clear();
        assertEquals(other, eruptions.strike(TestSpecs.HULL, eruptions.rockX(other), eruptions.rockY(other), events));
        assertEquals(1, events.count(SimEvents.Type.ROCK_STRUCK));
        assertFalse(eruptions.rockUsed(other), "broken on the impact");
        assertEquals(10, eruptions.damage(), 1e-9);
    }

    @Test
    void shotDownRocksPayNothing() {
        LevelScript level = Act6BSpecs.level(
                12,
                HazardSpec.NONE.withEruptions(
                        Optional.of(Act6BSpecs.eruptions(new HazardSpec.Vent(0, Ship.START_X, 2, 2, 3, 3)))),
                List.of(),
                List.of());
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        int broken = Act6BSpecs.run(sortie, SimStep.ticks(10), Command.FIRE.bit(), SimEvents.Type.ROCK_DESTROYED);
        assertTrue(broken > 0, "the straight-up rocks fall to the guns");
        assertEquals(0, sortie.kills(), "no kill");
        assertEquals(0, sortie.credits(), "no pay");
        assertTrue(sortie.eruptions().isPresent());
    }

    @Test
    void rookKeepsOutOfATellingVentsFan() {
        Eruptions eruptions = new Eruptions(SPEC, new SplitMix64(1));
        SimEvents events = new SimEvents(32);
        int tick = 0;
        while (!eruptions.telling(0)) {
            step(eruptions, tick++, events);
        }
        RookHazards hazards = new RookHazards(new Hitbox(30, 30), 20);
        double ventY = eruptions.ventY(0);
        assertTrue(hazards.clearOfStorm(250, ventY + 100, null, eruptions));
        double reach = 100 * Math.tan(Math.toRadians(50)) + 15 + RookHazards.GAP;
        assertEquals(240 + reach, hazards.x(), 1e-9, "sideways out of the fan, to the nearer side");
        assertFalse(hazards.clearOfStorm(250, ventY - 60, null, eruptions), "nothing flies below the vent");
    }

    /** The steps of each tell's start over {@code steps} real steps, the level clock ticking every {@code every}th. */
    private static List<Integer> tells(Eruptions eruptions, int steps, int every) {
        SimEvents events = new SimEvents(32);
        List<Integer> tells = new java.util.ArrayList<>();
        int levelTick = 0;
        double scroll = 0;
        for (int step = 0; step < steps; step++) {
            events.clear();
            eruptions.step(levelTick, scroll, events);
            if (events.count(SimEvents.Type.ERUPTION_TELL) > 0) {
                tells.add(step);
            }
            if (step % every == every - 1) {
                levelTick++;
                scroll += 0.1;
            }
        }
        return tells;
    }

    @Test
    void theCyclesCountTheRealStepsWhileTheLevelClockIsHeld_theEntryWaitsForIt() {
        HazardSpec.EruptionSpec later = Act6BSpecs.eruptions(new HazardSpec.Vent(1, 240, 2, 2, 3, 3));
        List<Integer> free = tells(new Eruptions(later, new SplitMix64(1)), SimStep.ticks(10), 1);
        List<Integer> held = tells(new Eruptions(later, new SplitMix64(1)), SimStep.ticks(10), 3);
        int cycle = SimStep.ticks(1.2) + SimStep.ticks(2);
        assertEquals(List.of(SimStep.ticks(3), SimStep.ticks(3) + cycle, SimStep.ticks(3) + 2 * cycle), free);
        // A clock at a third: the vent enters at the third real second, its cycles then on the real steps.
        int entry = 3 * SimStep.ticks(1);
        assertEquals(List.of(entry + SimStep.ticks(2), entry + SimStep.ticks(2) + cycle), held);
    }

    @Test
    void aVentsFirstTellComesAtItsOwnDelay_laterOnesByThePeriod() {
        HazardSpec.Vent quick = new HazardSpec.Vent(0, 240, 5, 5, 3, 3, 0.8);
        assertTrue(quick.firstSet());
        assertFalse(new HazardSpec.Vent(0, 240, 5, 5, 3, 3).firstSet(), "round 73 C.8: a period after its entry");
        List<Integer> tells = tells(new Eruptions(Act6BSpecs.eruptions(quick), new SplitMix64(1)), SimStep.ticks(9), 1);
        assertEquals(
                List.of(SimStep.ticks(0.8), SimStep.ticks(0.8) + SimStep.ticks(1.2) + SimStep.ticks(5)),
                tells,
                "0.8 s after its entry, then its tell and a period");
        // Level 39's plume field: a row of three rippling 0.4 s a vent.
        List<Integer> ripple = tells(
                new Eruptions(
                        Act6BSpecs.eruptions(
                                new HazardSpec.Vent(0, 160, 5, 7, 3, 5, 0.5),
                                new HazardSpec.Vent(0, 240, 5, 7, 3, 5, 0.9),
                                new HazardSpec.Vent(0, 320, 5, 7, 3, 5, 1.3)),
                        new SplitMix64(1)),
                SimStep.ticks(2),
                1);
        assertEquals(List.of(SimStep.ticks(0.5), SimStep.ticks(0.9), SimStep.ticks(1.3)), ripple);
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> new HazardSpec.Vent(0, 240, 5, 5, 3, 3, -0.1));
    }

    @Test
    void aBossCheckpointsRestoreEruptsAnEnteredVentAPeriodOfRealStepsLater() {
        Eruptions eruptions = new Eruptions(SPEC, new SplitMix64(1));
        SimEvents events = new SimEvents(32);
        int tick = 0;
        for (; tick < SimStep.ticks(3); tick++) {
            step(eruptions, tick, events);
        }
        eruptions.restore(SimStep.ticks(1), 20);
        assertTrue(eruptions.ventPresent(0), "entered before the checkpoint: it stays");
        int told = -1;
        for (int k = 1; k <= SimStep.ticks(3) && told < 0; k++) {
            events.clear();
            // The level clock held at the checkpoint: the period still runs.
            eruptions.step(SimStep.ticks(1), 20, events);
            if (events.count(SimEvents.Type.ERUPTION_TELL) > 0) {
                told = k;
            }
        }
        assertEquals(SimStep.ticks(2), told, "a period after the restore");
    }
}
