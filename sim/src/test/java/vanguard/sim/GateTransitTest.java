package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part B, step B1 (design/world/vrell-space, gate transit; design/campaign Level 43, Layout; design/player/wingmen,
 * Rook in Act 7): from the transit's formation the input is set aside, the ship and Rook ease into formation and hold
 * it, nothing hits them (bullets in flight pass through), the warp starts at its time and the level ends inside it.
 */
class GateTransitTest {
    private static final int HELD = Command.LEFT.bit() | Command.UP.bit() | Command.FIRE.bit();

    private static Sortie sortie() {
        return Act7BSpecs.withRook(Act7BSpecs.transitLevel(List.of(), Optional.of(Act7BSpecs.TRANSIT)));
    }

    @Test
    void theShipAndRookFormUpWhateverTheInputThenTheWarpAndTheEnd() {
        Sortie sortie = sortie();
        int form = SimStep.ticks(10);
        int warp = SimStep.ticks(14.5);
        int forms = 0;
        int warps = 0;
        int shotsAfter = 0;
        double before = Double.NaN;
        for (int tick = 1; !sortie.complete(); tick++) {
            sortie.step(HELD);
            forms += sortie.events().count(SimEvents.Type.TRANSIT_FORM);
            warps += sortie.events().count(SimEvents.Type.TRANSIT_WARP);
            if (sortie.events().count(SimEvents.Type.TRANSIT_FORM) > 0) {
                assertEquals(form, tick, "the formation at its time");
                before = sortie.ship().x();
            }
            if (sortie.events().count(SimEvents.Type.TRANSIT_WARP) > 0) {
                assertEquals(warp, tick, "the warp at its time");
            }
            if (tick >= form) {
                shotsAfter += sortie.events().count(SimEvents.Type.SHOT_FIRED);
                assertTrue(sortie.transit().active());
            } else {
                assertFalse(sortie.transit().active());
            }
            if (tick < warp) {
                assertEquals(0, sortie.transit().warp(), 1e-12);
            }
        }
        assertEquals(1, forms);
        assertEquals(1, warps);
        assertEquals(0, shotsAfter, "the guns are silent, Rook's too");
        assertTrue(before < 100, "the input had flown it to the left");
        assertEquals(SimStep.ticks(16), sortie.levelTick, "the level ends inside the warp, at its reach-end");
        assertEquals(1, sortie.transit().warp(), 1e-12, "the warp has run its length at the end");
        assertEquals(GateTransit.FORM_X, sortie.ship().x(), 1e-9, "in formation whatever the input");
        assertEquals(GateTransit.FORM_Y, sortie.ship().y(), 1e-9);
        Wingman rook = sortie.wingman().orElseThrow();
        // The input held left pinned the ship to the left edge, so he had swapped to its right: he keeps that side.
        assertEquals(WingmanTest.AI.wing().x(), Math.abs(rook.x() - GateTransit.FORM_X), 1e-9, "Rook in his wing slot");
        assertEquals(GateTransit.FORM_Y - WingmanTest.AI.wing().y(), rook.y(), 1e-9);
        assertEquals(1, sortie.transit().formation(), 1e-12);
    }

    @Test
    void theFormationEasesInWithoutAJump() {
        Sortie sortie = sortie();
        double lastX = Double.NaN;
        double most = 0;
        for (int tick = 1; tick <= SimStep.ticks(13); tick++) {
            sortie.step(HELD);
            if (tick > SimStep.ticks(10)) {
                most = Math.max(most, Math.abs(sortie.ship().x() - lastX));
            }
            lastX = sortie.ship().x();
        }
        assertTrue(most > 0 && most < 4, "smooth steps: " + most);
    }

    @Test
    void nothingHitsTheShipOrRookInTheTransitTheBulletsPassThrough() {
        Sortie with = sortie();
        Sortie without = Act7BSpecs.withRook(Act7BSpecs.transitLevel(List.of(), Optional.empty()));
        double[] lost = new double[4];
        Sortie[] both = {with, without};
        for (int k = 0; k < 2; k++) {
            Sortie sortie = both[k];
            Act6ASpecs.run(sortie, SimStep.ticks(12), Command.NONE);
            Defences defences = sortie.ship().defences();
            Wingman rook = sortie.wingman().orElseThrow();
            double ship = defences.shield() + defences.armour();
            double his = rook.armour();
            // Straight down onto each from 100 px above, at 300 px/s: there in a third of a second.
            sortie.fireBullet(sortie.ship().x(), sortie.ship().y() + 100, -Math.PI / 2, 300, 20);
            sortie.fireBullet(rook.x(), rook.y() + 100, -Math.PI / 2, 300, 20);
            assertEquals(2, sortie.bulletCount());
            Act6ASpecs.run(sortie, SimStep.ticks(0.4), Command.NONE);
            if (k == 0) {
                assertEquals(2, sortie.bulletCount(), "they pass through");
            }
            Act6ASpecs.run(sortie, SimStep.ticks(0.2), Command.NONE);
            lost[2 * k] = ship - (defences.shield() + defences.armour());
            lost[2 * k + 1] = his - rook.armour();
            assertTrue(lost[2 * k + 1] >= 0);
        }
        assertEquals(0, lost[0], 1e-9, "the ship untouched in the transit");
        assertEquals(0, lost[1], 1e-9, "Rook untouched in the transit");
        assertTrue(lost[2] > 0, "the same bullet hurts the ship without a transit (Rook dodges as ever there)");
    }

    @Test
    void aRetryFormsItAfreshAndItIsHashed() {
        Sortie sortie = sortie();
        Sortie other = sortie();
        Act6ASpecs.run(sortie, SimStep.ticks(11), HELD);
        Act6ASpecs.run(other, SimStep.ticks(11), HELD);
        assertEquals(sortie.stateHash(), other.stateHash());
        assertTrue(sortie.transit().active());

        sortie.retry(TestSpecs.FULL_ARMOUR);
        sortie.step(Command.NONE);
        assertFalse(sortie.transit().active());
        assertEquals(0, sortie.transit().formation(), 1e-12);
    }

    @Test
    void withoutATransitTheInputFliesTheShipToTheEnd() {
        Sortie sortie = Act7BSpecs.withRook(Act7BSpecs.transitLevel(List.of(), Optional.empty()));
        Act6ASpecs.run(sortie, SimStep.ticks(15), Command.RIGHT.bit());
        assertNull(sortie.transit());
        assertTrue(sortie.ship().x() > 400, "it flies by its input");
    }
}
