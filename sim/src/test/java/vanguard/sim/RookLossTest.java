package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.LevelClockSpecs.cue;
import static vanguard.sim.LevelClockSpecs.radioed;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part F, step F1: Rook's scripted loss (design/player/wingmen, Rook's loss; design/campaign Level 26's {@code
 * rook_loss}; m8f-gaps.md §1.2, §5 F6, F7). At the trails' time his trails start and his barks end ({@code requires:
 * escort} lines no longer play); at the loss's time he stops firing and taking hits and spirals down off the bottom
 * edge in the spin's time, faster than the scroll, then is gone for the attempt; not an eject (no pod, his armour as it
 * was). Ejected before, without an escort or with the Bulwark the loss is the radio's only: the scripted lines play in
 * every case. A retry replays it. Deterministic and allocation-free; a level without one hashes as before.
 */
class RookLossTest {
    /** The trails at 3 s, the loss at 3.5 s, 2.5 s of spin (Level 26: 162.5, 163.5, 2.5). */
    static final RookLossSpec LOSS = new RookLossSpec(3.5, 3, 2.5);

    private static final double SCROLL = 40;

    /** Cue 0: a bark before the trails; 1: a bark after them; 2 and 3: the scripted lines (no escort required). */
    private static final List<RadioCue> RADIO = List.of(
            cue(CueTrigger.TIME, 2, RadioCue.FITTED_ESCORT),
            cue(CueTrigger.TIME, 3.2, RadioCue.FITTED_ESCORT),
            cue(CueTrigger.TIME, 3.3, 0),
            cue(CueTrigger.TIME, 6.5, 0));

    static LevelScript level(Optional<RookLossSpec> loss) {
        return LevelScript.builder(26, 4)
                .sections(List.of(new Section(20, SCROLL)))
                .radio(RADIO)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .rookLoss(loss)
                .build();
    }

    static final LevelScript LEVEL = level(Optional.of(LOSS));

    private static Sortie withRook() {
        return WingmanTest.sortie(LEVEL, WingmanSpec.Side.LEFT);
    }

    /** What happened over a run. */
    private static final class Run {
        int trails = -1;
        int lost = -1;
        int trailsEvents;
        int lostEvents;
        int ejectEvents;
        int lostValue;
        final boolean[] radioed = new boolean[RADIO.size()];

        void record(Sortie sortie, int step) {
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                switch (events.type(i)) {
                    case ROOK_TRAILS -> {
                        trails = step;
                        trailsEvents++;
                    }
                    case ROOK_LOST -> {
                        lost = step;
                        lostEvents++;
                        lostValue = events.value(i);
                    }
                    case WINGMAN_EJECTED -> ejectEvents++;
                    default -> {}
                }
            }
            for (int c = 0; c < radioed.length; c++) {
                radioed[c] |= radioed(sortie, c);
            }
        }
    }

    /** Steps {@code seconds} (the ship idle), running {@code at} before step {@code atStep} (-1: never). */
    private static Run run(Sortie sortie, double seconds, int atStep, Runnable at) {
        Run run = new Run();
        for (int step = 0; step < SimStep.ticks(seconds); step++) {
            if (step == atStep) {
                at.run();
            }
            sortie.step(Command.NONE);
            run.record(sortie, step);
        }
        return run;
    }

    private static Run run(Sortie sortie, double seconds) {
        return run(sortie, seconds, -1, () -> {});
    }

    @Test
    void theLossHasItsTrailsFirstAndFallsInsideTheLevel() {
        assertThrows(IllegalArgumentException.class, () -> new RookLossSpec(3, 3.5, 2.5), "the trails come first");
        assertThrows(IllegalArgumentException.class, () -> new RookLossSpec(3.5, 3, 0), "a spin takes time");
        assertThrows(
                IllegalArgumentException.class,
                () -> level(Optional.of(new RookLossSpec(20, 19, 2.5))),
                "before the level's end");
        assertEquals(Optional.empty(), level(Optional.empty()).rookLoss());
        assertEquals(Optional.of(LOSS), LEVEL.toBuilder().build().rookLoss(), "the builder keeps it");
        assertFalse(WingmanTest.sortie(level(Optional.empty()), WingmanSpec.Side.LEFT)
                .wingman()
                .orElseThrow()
                .loss()
                .planned());
    }

    @Test
    void withRookHisTrailsStartThenHeIsLostAndSpinsOnTheScreenOffTheBottomEdgeFasterThanTheScroll() {
        Sortie sortie = withRook();
        Wingman rook = sortie.wingman().orElseThrow();
        assertTrue(rook.loss().planned());
        // The level clock reaches 3 s in the step of index ticks(3) - 1.
        Run before = run(sortie, (SimStep.ticks(3) - 1) * SimStep.SECONDS);
        assertEquals(-1, before.trails, "nothing before the trails' time");
        assertFalse(rook.loss().trailing());
        assertTrue(rook.onRadio());

        Run run = run(sortie, 0.5);
        assertEquals(0, run.trails, "his trails start at 3 s");
        assertEquals(1, run.trailsEvents, "once");
        assertTrue(rook.loss().trailing());
        assertFalse(rook.onRadio(), "his barks end with the trails");
        assertFalse(rook.loss().lost(), "he still flies until the loss");
        double armour = rook.armour();

        double fromX = rook.x();
        double fromY = rook.y();
        double shipX = sortie.ship().x();
        Run loss = run(sortie, 1.0 / SimStep.PER_SECOND);
        assertEquals(1, loss.lostEvents, "lost at 3.5 s");
        assertTrue(rook.loss().lost());
        assertEquals(fromX < shipX ? -1 : 1, loss.lostValue, "he spins to the side away from the ship");
        assertFalse(rook.ejected(), "not an eject");
        assertFalse(rook.fires(true), "his gun is silent");
        assertFalse(rook.hit(1000, shipX, sortie.ship().y(), sortie.events()), "he takes no hits");
        assertEquals(armour, rook.armour(), "his armour as at the loss");

        // Round 55 C.2: the whole spin plays on the screen. From low beside the ship the hits toss him up a little,
        // then he sinks back faster and faster and crosses the bottom edge as the spin ends, faster than the scroll.
        double half = rook.spec().craft().hitbox().height() / 2;
        double below = fromY + half + RookLoss.BELOW;
        double faster = RookLoss.FASTER * SCROLL;
        assertTrue(below < faster * 2.5, "low: " + fromY);
        assertEquals(faster - 2 * below / 2.5, rook.loss().tossSpeed(), 1e-9, "the hits' toss");
        double lastY = rook.y();
        double highest = rook.y();
        int steps = 0;
        int onScreen = 0;
        while (!rook.loss().gone()) {
            sortie.step(Command.FIRE.bit());
            steps++;
            highest = Math.max(highest, rook.y());
            if (rook.y() > 0) {
                onScreen++;
            }
            if (steps > SimStep.ticks(1.5)) {
                assertTrue(rook.y() < lastY, "sinking");
            }
            lastY = rook.y();
            assertEquals(0, sortie.events().count(SimEvents.Type.WINGMAN_EJECTED), "no pod");
        }
        assertEquals(SimStep.ticks(2.5), steps, "off the bottom edge in 2.5 s");
        assertTrue(rook.loss().tossSpeed() <= 0 || highest > fromY, "tossed up first");
        assertTrue(onScreen >= SimStep.ticks(1.8), "his centre on the screen for most of the spin: " + onScreen);
        assertTrue(rook.y() + half < 0, "below the bottom edge: " + rook.y());
        assertEquals(faster, rook.loss().fallSpeed(), 1e-9, "off the edge at 1.25 × the scroll");
        assertTrue(loss.lostValue < 0 ? rook.x() < fromX : rook.x() > fromX, "toward his side");
        assertEquals(Integer.MAX_VALUE, rook.ticksSinceShot(), "never fired: the ship held its fire before, he after");

        double goneY = rook.y();
        Run after = run(sortie, 3);
        assertEquals(0, after.lostEvents + after.trailsEvents + after.ejectEvents, "gone for the attempt");
        assertEquals(goneY, rook.y(), 1e-9, "he stays gone");
        assertTrue(rook.loss().lost());
    }

    @Test
    void hisBarksEndAtTheTrailsAndTheScriptedLinesPlayInEveryCase() {
        Sortie rook = withRook();
        Run flying = run(rook, 8);
        assertTrue(flying.radioed[0], "a bark while he flies");
        assertFalse(flying.radioed[1], "no bark after the trails");
        assertTrue(flying.radioed[2] && flying.radioed[3], "the scripted lines");

        Sortie ejecting = withRook();
        Wingman ejected = ejecting.wingman().orElseThrow();
        Run pod = run(
                ejecting,
                8,
                SimStep.ticks(1),
                () -> ejected.hit(1000, ejecting.ship().x(), ejecting.ship().y(), ejecting.events()));
        assertTrue(ejected.ejected());
        assertFalse(pod.radioed[0] || pod.radioed[1], "no bark after the eject");
        assertTrue(pod.radioed[2] && pod.radioed[3], "the scripted lines");

        Sortie none = new Sortie(1, TestSpecs.LOADOUT, LEVEL, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Run alone = run(none, 8);
        assertTrue(alone.radioed[2] && alone.radioed[3], "the scripted lines without an escort");

        Sortie bulwark = new Sortie(
                1, TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK), LEVEL, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Run drone = run(bulwark, 8);
        assertTrue(drone.radioed[2] && drone.radioed[3], "the scripted lines with the Bulwark");
    }

    @Test
    void ejectedBeforeWithoutAnEscortOrWithTheBulwarkTheLossIsTheRadiosOnly() {
        Sortie ejecting = withRook();
        Wingman rook = ejecting.wingman().orElseThrow();
        Run pod = run(
                ejecting,
                8,
                SimStep.ticks(1),
                () -> rook.hit(1000, ejecting.ship().x(), ejecting.ship().y(), ejecting.events()));
        assertEquals(-1, pod.trails, "no trails");
        assertEquals(-1, pod.lost, "no loss");
        assertFalse(rook.loss().lost());
        assertTrue(rook.ejected(), "his pod as before");

        Sortie none = new Sortie(1, TestSpecs.LOADOUT, LEVEL, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Run alone = run(none, 8);
        assertEquals(-1, alone.trails);
        assertEquals(-1, alone.lost);

        Sortie bulwark = new Sortie(
                1, TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK), LEVEL, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Bulwark drone = bulwark.bulwark().orElseThrow();
        double armour = drone.armour();
        Run slot = run(bulwark, 8);
        assertEquals(-1, slot.trails);
        assertEquals(-1, slot.lost);
        assertEquals(armour, drone.armour(), "the Bulwark is untouched");
        assertEquals(0, bulwark.events().count(SimEvents.Type.BULWARK_DESTROYED));
    }

    @Test
    void aRetryReplaysTheLoss() {
        Sortie sortie = withRook();
        Wingman rook = sortie.wingman().orElseThrow();
        Run first = run(sortie, 7);
        assertEquals(1, first.lostEvents);
        assertTrue(rook.loss().gone());

        sortie.retry(TestSpecs.FULL_ARMOUR, 40);
        Run second = run(sortie, 7);
        assertEquals(2, sortie.attempt());
        assertEquals(1, second.trailsEvents, "the trails again");
        assertEquals(1, second.lostEvents, "the loss again");
        assertEquals(first.lost, second.lost, "at the same time on the level clock");
        assertTrue(second.radioed[0], "his bark before the trails again");
        assertTrue(rook.loss().gone());
        assertEquals(40, rook.armour(), "he started the attempt with the retry's armour");
    }

    @Test
    void theLossReplaysWithoutAllocatingAndALevelWithoutOneHashesAsBefore() {
        Supplier<Sortie> make = () -> new Sortie(
                3,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.RIGHT)),
                LEVEL,
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Sortie a = make.get();
        Sortie b = make.get();
        for (int step = 0; step < SimStep.ticks(8); step++) {
            int commands = SortieTest.Pilot.commands(step);
            a.step(commands);
            b.step(commands);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        assertTrue(a.wingman().orElseThrow().loss().gone());
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int step = 0; step < SimStep.ticks(8); step++) {
                sortie.step(SortieTest.Pilot.commands(step));
            }
        }));

        // Before the trails a level with the loss and one without differ only by the loss's hashed state.
        Sortie with = WingmanTest.sortie(LEVEL, WingmanSpec.Side.LEFT);
        Sortie without = WingmanTest.sortie(level(Optional.empty()), WingmanSpec.Side.LEFT);
        for (int step = 0; step < SimStep.ticks(2); step++) {
            with.step(Command.NONE);
            without.step(Command.NONE);
            assertEquals(
                    with.wingman().orElseThrow().x(),
                    without.wingman().orElseThrow().x(),
                    1e-12);
        }
    }
}
