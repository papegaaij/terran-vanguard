package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.sim.Ally;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part D's dome looks (design/allies/ares-landing-dome; design/campaign Level 17; tools/art/siege_units.py's
 * sprites) and the DOMES tracker (design/ui/hud, Level 17), on hand-built sorties ({@link DomesFixture}): a crack
 * overlay per hit, the flicker at the last hit before breaking on every difficulty's hits, the cave-in to the wreck
 * and the dust rising once; Eden in its own larger set; the tracker's status once every dome has left.
 */
class DomeLooksTest {
    @Test
    void aCrackOverlayPerHitTakenCumulativeAtMostTheThree() {
        assertEquals(0, DomeLooks.cracks(0, 3), "untouched");
        assertEquals(1, DomeLooks.cracks(1, 3));
        assertEquals(2, DomeLooks.cracks(2, 3));
        assertEquals(3, DomeLooks.cracks(3, 3), "easy's third hit");
        assertEquals(3, DomeLooks.cracks(4, 3), "never more than the overlays");
        assertEquals(1, DomeLooks.cracks(2, 1), "a set with one overlay");
        assertEquals(2, DomeLooks.cracks(2 - 1e-12, 3), "hits as HP: rounding does not drop a crack");
    }

    @Test
    void theLightFlickersAtTheLastHitBeforeBreakingOnEveryDifficulty() {
        // easy 4 hits, medium 3, hard 2 (design/allies, Ares Landing dome)
        for (int hits = 2; hits <= 4; hits++) {
            for (int taken = 0; taken < hits; taken++) {
                assertEquals(taken == hits - 1, DomeLooks.flickers(taken, hits), hits + " hits, " + taken + " taken");
            }
        }
        assertFalse(DomeLooks.flickers(0, 1), "an untouched dome never flickers");
    }

    @Test
    void theFlickerStuttersThroughAllFourFramesNotAPlainLoop() {
        Set<Integer> seen = new HashSet<>();
        int changes = 0;
        int last = -1;
        for (int step = 0; step < DomeLooks.FLICKER.length; step++) {
            int frame = DomeLooks.flickerFrame(step / DomeLooks.FLICKER_FPS + 1e-6, 0, 4);
            seen.add(frame);
            changes += frame != last ? 1 : 0;
            last = frame;
        }
        assertEquals(Set.of(0, 1, 2, 3), seen);
        assertTrue(changes < DomeLooks.FLICKER.length, "some frames hold");
        assertTrue(
                DomeLooks.flickerFrame(0.05, 0, 4) != DomeLooks.flickerFrame(0.05, 1, 4)
                        || DomeLooks.flickerFrame(0.15, 0, 4) != DomeLooks.flickerFrame(0.15, 1, 4),
                "two domes out of step");
        assertEquals(1, DomeLooks.flickerFrame(0.25, 0, 2), "a shorter set stays inside it");
    }

    @Test
    void theCaveInEndsOnTheWreckAndTheDustRisesOnce() {
        assertEquals(0, DomeLooks.brokenFrame(0, 6));
        assertEquals(1, DomeLooks.brokenFrame(SimStep.PER_SECOND / 10, 6), "10 fps");
        assertEquals(5, DomeLooks.brokenFrame(SimStep.PER_SECOND / 2, 6), "the wreck at 0.5 s");
        assertEquals(5, DomeLooks.brokenFrame(SimStep.PER_SECOND * 60, 6), "and it stays");
        assertEquals(0, DomeLooks.dustFrame(0, 10));
        assertEquals(9, DomeLooks.dustFrame((int) (SimStep.PER_SECOND * 1.24), 10), "8 fps");
        assertEquals(-1, DomeLooks.dustFrame((int) (SimStep.PER_SECOND * 1.25), 10), "gone after 1.25 s");
    }

    @Test
    void theBreathingLoopRunsAtFourFramesASecondEachDomeOutOfStep() {
        assertEquals(0, DomeLooks.breatheFrame(0.1, 0, 4));
        assertEquals(1, DomeLooks.breatheFrame(0.3, 0, 4));
        assertEquals(0, DomeLooks.breatheFrame(1.1, 0, 4), "a 1 s loop");
        assertEquals(1, DomeLooks.breatheFrame(0.1, 1, 4));
    }

    @Test
    void edenIsDrawnInItsOwnLargerSetAsTheLevelNamesIt() {
        String key = DomesFixture.CONTENT.levelKey(17).orElseThrow();
        List<String> names = DomeLooks.names(DomesFixture.CONTENT.level(key), DomesFixture.CONTENT.allies());
        assertEquals(List.of("ares-dome", "ares-dome", "ares-dome", "ares-dome", "ares-dome-eden"), names);
        // In the sortie Eden's footprint is its own 144 px: it is decided (left the screen) by its own size.
        Sortie sortie = DomesFixture.sortie(0);
        assertEquals(144, sortie.allySpec(2).size().height());
        DomesFixture.runTo(sortie, 23);
        Ally eden = sortie.ally(2);
        assertEquals(PlayField.HEIGHT + 72, eden.y(), 1.5, "its lower edge at the top edge as it enters");
        assertFalse(DomeLooks.onScreen(eden.y() + 1, 144));
        assertTrue(DomeLooks.onScreen(eden.y() - 2, 144));
    }

    @Test
    void theTrackersDomesAreDecidedOnceEveryDomeHasLeft() {
        Sortie sortie = DomesFixture.sortie(0);
        DomesFixture.runTo(sortie, 30);
        assertFalse(MissionPanel.allPassed(sortie), "Eden is still on the screen");
        DomesFixture.runTo(sortie, 33);
        assertTrue(MissionPanel.allPassed(sortie), "Eden's footprint left at t≈31.6");
        assertTrue(sortie.secondaryMet());
        assertEquals("DONE", MissionPanel.convoyStatus(true, false, true));
        assertNull(MissionPanel.convoyStatus(false, true, false), "failed: the pips show while domes remain");
        assertEquals("FAILED", MissionPanel.convoyStatus(false, true, true));
    }

    @Test
    void aBombedDomeShowsItsCrackFlashesAndFailsTheSecondary() {
        // One hooked bomber: one load, one hit on Bradbury.
        Sortie sortie = DomesFixture.sortie(1);
        int drops = 0;
        int hits = 0;
        boolean flashed = false;
        while (sortie.levelSeconds() < DomesFixture.LENGTH && !sortie.complete()) {
            sortie.step(vanguard.sim.Command.NONE);
            drops += sortie.events().count(SimEvents.Type.BOMB_DROPPED);
            hits += sortie.events().count(SimEvents.Type.ALLY_HIT);
            flashed |= sortie.ally(0).ticksSinceHit() < DomeLooks.HIT_FLASH_TICKS;
        }
        assertEquals(1, drops, "the bomber dropped its load on the dome");
        assertEquals(1, hits);
        assertTrue(flashed, "its white flash");
        Ally bradbury = sortie.ally(0);
        assertFalse(bradbury.lost());
        assertEquals(1, DomeLooks.cracks(bradbury.hitsTaken(), 3), "one crack");
        assertFalse(DomeLooks.flickers(bradbury.hitsTaken(), DomesFixture.HITS), "two hits left");
        assertEquals(0, DomeLooks.cracks(sortie.ally(1).hitsTaken(), 3), "Lowell untouched");
        assertTrue(sortie.untouchedFailed(), "the secondary failed at the first hit");
    }
}
