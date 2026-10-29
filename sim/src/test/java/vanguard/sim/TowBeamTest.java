package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.PartASpecs.beamMount;
import static vanguard.sim.PartASpecs.sortie;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 (round 64 C.1 a, the Act 5 tows: Level 30's dead buoy, Level 31's claim): a beam touching an opted-in tow's
 * housing (its cable box) counts one hit at once and one per 0.25 s while the touch lasts; a break starts the count
 * again; Level 07's lifeboat (not opted in) counts shots only.
 */
class TowBeamTest {
    private static final int FIRE = Command.FIRE.bit();

    /** A buoy-like tow straight over the ship's start, drifting down, its housing taking {@code hits}. */
    private static ObjectiveSpec.TowSpec buoy(int hits, boolean beamTouch) {
        return new ObjectiveSpec.TowSpec(
                0.5,
                Ship.START_X,
                0,
                -200,
                new Hitbox(28, 28),
                new Hitbox(20, 20),
                0,
                0,
                new Hitbox(24, 24),
                hits,
                400,
                "dead beacon buoy",
                beamTouch);
    }

    private static Sortie sortieWith(ObjectiveSpec.TowSpec tow) {
        LevelScript level =
                TestSpecs.level(30, List.of()).toBuilder().secrets(1).build();
        return sortie(TestSpecs.withObjectives(level, o -> o.withTows(List.of(tow))), beamMount(1));
    }

    /** Steps without fire until the tow's housing is down in the middle of the screen (at most 4 s). */
    private static void untilPresent(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(4) && !(sortie.tow(0).cableY() < PlayField.HEIGHT / 2.0); i++) {
            sortie.step(Command.NONE);
        }
        assertTrue(sortie.tow(0).present());
        assertTrue(sortie.tow(0).cableY() < PlayField.HEIGHT / 2.0);
    }

    @Test
    void aBeamsTouchCountsAHitAtOnceAndEveryQuarterSecond() {
        assertEquals(15, Tow.BEAM_HIT_TICKS);
        Sortie sortie = sortieWith(buoy(4, true));
        untilPresent(sortie);
        int first = -1;
        int hits = 0;
        int cut = -1;
        for (int i = 0; i < SimStep.ticks(3) && cut < 0; i++) {
            sortie.step(FIRE);
            int clamp = sortie.events().count(SimEvents.Type.CLAMP_HIT);
            if (clamp > 0 && first < 0) {
                first = i;
            }
            hits += clamp;
            if (!sortie.tow(0).holding()) {
                cut = i;
            }
        }
        assertEquals(4, hits, "one counted hit each, the last cuts it");
        assertEquals(3 * Tow.BEAM_HIT_TICKS, cut - first, "at once, then every 0.25 s");
        assertEquals(1, sortie.events().count(SimEvents.Type.SECRET_FOUND));
    }

    @Test
    void aBreakInTheTouchStartsTheCountAgain() {
        Sortie sortie = sortieWith(buoy(4, true));
        untilPresent(sortie);
        int hits = 0;
        // Touch, let go, touch again: each touch counts at once.
        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < Beam.GROW_TICKS + 1; i++) {
                sortie.step(FIRE);
                hits += sortie.events().count(SimEvents.Type.CLAMP_HIT);
            }
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.CLAMP_HIT);
        }
        assertEquals(3, hits, "a fresh touch counts at once");
        assertEquals(1, sortie.tow(0).hitsLeft());
        assertTrue(sortie.tow(0).holding());
    }

    @Test
    void aTowNotOptedInIgnoresTheBeam() {
        Sortie sortie = sortieWith(buoy(4, false));
        untilPresent(sortie);
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(FIRE);
            hits += sortie.events().count(SimEvents.Type.CLAMP_HIT);
        }
        assertEquals(0, hits, "Level 07's rule: the player's shots only");
        assertTrue(sortie.tow(0).holding());
        assertFalse(sortie.tow(0).crateDue());
    }
}
