package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.LevelData.Visibility.Ramp;

/**
 * M7 part E: Level 18's dust storm (design/campaign Level 18, Visibility in the storm; MA.2 = b). Level 18 is not
 * mapped yet (step E4), so a hand-built visibility block with the approved numbers stands in: the look-b edge
 * around the radius, the storm's strength by the ramps and difficulty, and the dust bed's swell with it.
 */
class StormLooksTest {
    /** The approved block: 170 px in the deep storm, 150 in the back wall (easy 200 / 180, hard 150 / 130). */
    private static final LevelData.Visibility STORM = new LevelData.Visibility(
            Map.of("deep", 170.0, "wall", 150.0),
            List.of(
                    new Ramp(28, 36, "deep"),
                    new Ramp(110, 114, LevelData.Visibility.FULL),
                    new Ramp(130, 134, "wall"),
                    new Ramp(170, 176, LevelData.Visibility.FULL)),
            120,
            Optional.of(Map.of("deep", 200.0, "wall", 180.0)),
            Optional.of(Map.of("deep", 150.0, "wall", 130.0)));

    @Test
    void theEdgeIsClearInsideAndDustOutsideLookB() {
        // Look b at its 120 px falloff: clear to 65 px inside the radius, gone 55 px outside it (noise at its middle).
        assertEquals(1, StormLooks.visible(0, 0.5, 0.5, 170, 120), 1e-12);
        assertEquals(1, StormLooks.visible(105, 0.5, 0.5, 170, 120), 1e-12);
        assertEquals(0, StormLooks.visible(225, 0.5, 0.5, 170, 120), 1e-12);
        double half = StormLooks.visible(165, 0.5, 0.5, 170, 120);
        assertEquals(0.5, half, 1e-12, "half way between R - 65 and R + 55");
        // Soft: it falls the whole way, never a step.
        double last = 1;
        for (int d = 100; d <= 230; d++) {
            double v = StormLooks.visible(d, 0.5, 0.5, 170, 120);
            assertTrue(v <= last && last - v < 0.03, "a step of " + (last - v) + " at " + d);
            last = v;
        }
    }

    @Test
    void theDriftingNoiseBreaksTheEdgeBothWays() {
        // A tongue of dust (both fields high) reaches in by up to 55 px; a gap (both low) lets the view out as far.
        double plain = StormLooks.visible(140, 0.5, 0.5, 170, 120);
        double tongue = StormLooks.visible(140, 1, 1, 170, 120);
        double gap = StormLooks.visible(140, 0, 0, 170, 120);
        assertTrue(tongue < plain && plain < gap, tongue + " < " + plain + " < " + gap);
        assertEquals(StormLooks.visible(140 + 55, 0.5, 0.5, 170, 120), tongue, 1e-12);
        assertEquals(StormLooks.visible(140 - 55, 0.5, 0.5, 170, 120), gap, 1e-12);
    }

    @Test
    void theStormClosesInAtTheFrontOpensInTheEyeAndClearsAtTheEnd() {
        assertEquals(0, StormLooks.strength(STORM, Difficulty.MEDIUM, 10), "the open plains: the full view");
        assertEquals(0, StormLooks.strength(STORM, Difficulty.MEDIUM, 28));
        double deep = StormLooks.strength(STORM, Difficulty.MEDIUM, 60);
        assertTrue(deep > 0.95 && deep < 1, "the deep storm, 170 px: " + deep);
        assertEquals(0, StormLooks.strength(STORM, Difficulty.MEDIUM, 120), "the eye: the full view");
        assertEquals(1, StormLooks.strength(STORM, Difficulty.MEDIUM, 150), 1e-12, "the back wall at its smallest");
        assertEquals(0, StormLooks.strength(STORM, Difficulty.MEDIUM, 180), "the clearing");
        double ramp = StormLooks.strength(STORM, Difficulty.MEDIUM, 32);
        assertTrue(ramp > 0.3 && ramp < 0.7, "half way into the front: " + ramp);
        // Hard's back wall is its own smallest: full strength there too.
        assertEquals(1, StormLooks.strength(STORM, Difficulty.HARD, 150), 1e-12);
        assertEquals(130, STORM.radiusAt(150, Difficulty.HARD), 1e-12);
        assertEquals(200, STORM.radiusAt(60, Difficulty.EASY), 1e-12);
    }

    @Test
    void theDustBedSwellsThreeDecibelsWithTheView() {
        assertEquals(0, StormLooks.swellDb(STORM, Difficulty.MEDIUM, 10), 1e-12);
        assertEquals(3, StormLooks.swellDb(STORM, Difficulty.MEDIUM, 150), 1e-12, "+3 dB in the back wall");
        assertTrue(StormLooks.swellDb(STORM, Difficulty.MEDIUM, 60) > 2.8, "nearly +3 in the deep storm");
        assertEquals(0, StormLooks.swellDb(STORM, Difficulty.MEDIUM, 120), 1e-12, "the eye: a light wind");
        assertEquals(3, StormLooks.swellDb(STORM, Difficulty.EASY, 150), 1e-12, "easy's wall is its smallest");
    }

    @Test
    void outsideTheRadiusAnAirUnitTakesTheDustsColourWithOnlyItsLightsPokingThrough() {
        // Round 41 R.b: seen through the dust a ship shares the dust's colour, a little denser so its outline reads,
        // a trace of its shading but no detail; never the dark umber of a "dark version" of the ship.
        for (int i = 0; i <= 10; i++) {
            double shade = StormLooks.silhouetteShade(i / 10.0);
            assertTrue(shade > 0.7 && shade < 0.95, "the veil's colour, a little denser: " + shade);
        }
        double form = StormLooks.silhouetteShade(1) - StormLooks.silhouetteShade(0);
        assertTrue(form > 0.05 && form < 0.2, "a trace of the unit's shading, not its detail: " + form);
        assertTrue(StormLooks.SILHOUETTE_COVER > 0.9, "the dust covers the unit drawn under it");
        // Its lights poke through: the Vrell's lime, teal and crimson, the Ghost Drone's red eye.
        assertTrue(StormLooks.glows(0xA8 / 255.0, 1, 0x3A / 255.0), "lime");
        assertTrue(StormLooks.glows(40 / 255.0, 220 / 255.0, 180 / 255.0), "teal");
        assertTrue(StormLooks.glows(220 / 255.0, 50 / 255.0, 40 / 255.0), "crimson");
        assertTrue(StormLooks.glows(1, 0.2, 0.16), "the red eye");
        // Its hull, a pale part (a Needler's legs) and a white highlight go into the dust.
        assertTrue(!StormLooks.glows(0.45, 0.45, 0.5), "a grey hull");
        assertTrue(!StormLooks.glows(0.85, 0.85, 0.7), "pale legs");
        assertTrue(!StormLooks.glows(0.95, 0.95, 0.95), "a white highlight");
    }
}
