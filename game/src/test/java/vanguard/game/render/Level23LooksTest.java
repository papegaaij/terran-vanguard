package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.sim.SimStep;

/**
 * M8 part C's Level 23 looks (design/campaign Level 23; tools/art/eel_swarm.py, glassfin.py, siphon_pump.py,
 * l23_props.py, backdrop_l23.py, act4_gear.py hydrofx, explosions.py --deep): the art's frames as the looks count
 * them; the under-water grade's waterline row, its looks per section eased between them and its wipe; the refit flash
 * on the waterline; the eel's stripe running from the tail to the head; the Glassfin's sparkle; the rules strip.
 */
class Level23LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-23");

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static JsonValue pivots(String name) throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + name + ".json")));
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        assertEquals(EelLooks.HEADINGS * EelLooks.HEAD_STATES, frames(EelLooks.SLUG), "the head: rest, tell, shot");
        assertEquals(EelLooks.HEADINGS * 2, frames(EelLooks.SLUG + "-segment"), "a body eel: rest, lit");
        assertEquals(EelLooks.HEADINGS * 2, frames(EelLooks.SLUG + "-tail"));
        assertEquals(EelLooks.HEADINGS * 3, frames(EelLooks.SLUG + "-stripe"), "head, segment, tail");
        assertEquals(4, frames(EelLooks.SLUG + "-muzzle"));
        assertEquals(EelLooks.HEADINGS * EelLooks.HUSK_STEPS, frames(EelLooks.SLUG + "-husk"));
        assertEquals(EelLooks.HEADINGS * EelLooks.HUSK_STEPS, frames(EelLooks.SLUG + "-head-husk"));
        assertEquals(EelLooks.HEADINGS, pivots(EelLooks.SLUG).get("mouth").size, "a mouth per heading");
        assertEquals(GlassfinLooks.HEADINGS * GlassfinLooks.STATES, frames(GlassfinLooks.SLUG));
        assertEquals(4, frames(GlassfinLooks.SLUG + "-glint"));
        assertEquals(8, frames(GlassfinLooks.SLUG + "-death"));
        assertEquals(8, frames(GlassfinLooks.SHARDS));
        assertEquals(4, frames(SiphonPumpLooks.SLUG));
        assertEquals(frames(SiphonPumpLooks.SLUG), frames(SiphonPumpLooks.SLUG + "-glow"), "the glow per frame");
        assertEquals(8, frames(SiphonPumpLooks.SLUG + "-break"));
        for (String single : List.of(
                SiphonPumpLooks.SLUG + "-dead",
                SiphonPumpLooks.SLUG + "-roots",
                SiphonPumpLooks.SLUG + "-roots-dead",
                "reef-spitter-floor")) {
            assertTrue(Files.isRegularFile(SPRITES.resolve(single + ".png")), single);
        }
        assertEquals(4, frames("driftjelly-deep"), "the jelly's four pulse frames");
        assertEquals(3, frames("survey-probe"), "snagged, hit, freed");
        assertEquals(8, frames("survey-probe-glow"), "the strobe's halves");
        assertEquals(6, frames("thera-tender"), "two headings of three jet frames");
        assertEquals(4, frames("thera-tender-lights"));
        assertEquals(2, pivots("thera-tender").get("cradle").size, "the cradle per heading");
        for (String prefix : List.of("ship", "rook")) {
            assertEquals(8, frames(prefix + "-hydro-wake"), prefix);
            assertEquals(3, frames(prefix + "-hydro-jet"), prefix);
            assertEquals(10, frames(prefix + "-refit"), prefix);
        }
        for (String rung : DeepDeaths.RUNGS) {
            assertTrue(frames("explosion-deep-" + rung) > 0, rung);
            assertEquals(frames("explosion-deep-" + rung), frames("explosion-deep-cavity-" + rung), rung);
            assertEquals(frames("explosion-" + rung), frames("explosion-deep-" + rung), "the air ladder's: " + rung);
        }
        for (String wall : List.of("ice-wall", "ice-wall-wet", "ice-wall-glow", "ice-wall-foot", "ice-wall-foot-wet")) {
            assertTrue(Files.isRegularFile(BACKDROP.resolve(wall + ".png")), wall);
        }
    }

    /** The waterline's row is the backdrop's band's: the ground row the ship's start height passes at 25.5 s. */
    @Test
    void theWaterlineLiesOnTheRowTheBandIsPaintedOn() {
        LevelData level = ThroughIceFixture.level();
        assertEquals(List.of(25.5), level.waterlines());
        assertEquals(2646, UnderwaterGrade.waterlineRow(level, 25.5), 1e-6, "scroll(25.5) + 96 (backdrop_l23.py)");
        assertEquals(96, UnderwaterGrade.SHIP_ROW, "the sim's Ship.START_Y");
        assertTrue(UnderwaterGrade.hasLook(level));
        assertFalse(UnderwaterGrade.hasLook(IceShelfFixture.level()), "Level 22 stays in the air");
    }

    /**
     * The look per section: the dry shaft unlit, the wet shaft the level's look, Thera Deep's lamp-lit farms and the
     * plain's darker water their own, each eased in over 2 s from the section before.
     */
    @Test
    void theLookFollowsTheSectionsEasedInOverTwoSeconds() {
        LevelData level = ThroughIceFixture.level();
        List<Optional<UnderwaterLook>> looks = new ArrayList<>();
        double[] starts = new double[level.sections().size()];
        for (int i = 0; i < starts.length; i++) {
            looks.add(level.underwaterLookOf(i));
            starts[i] = level.sectionStart(i);
        }
        float[] out = new float[UnderwaterGrade.LOOK_SIZE];
        UnderwaterGrade.lookAt(looks, starts, 10, out);
        assertEquals(1, out[0], 1e-6, "the dry shaft: unlit");
        assertEquals(0, out[4], 1e-6, "no caustics in the air");
        UnderwaterGrade.lookAt(looks, starts, 40, out);
        assertEquals(0.5, out[0], 1e-6, "the wet shaft: the level's look");
        assertEquals(60, out[5], 1e-4, "its snow");
        UnderwaterGrade.lookAt(looks, starts, 72, out);
        assertEquals(0.5, out[0], 1e-6, "Thera Deep's look eases in from the shaft's");
        UnderwaterGrade.lookAt(looks, starts, 73, out);
        assertEquals(0.54, out[0], 1e-6, "halfway");
        UnderwaterGrade.lookAt(looks, starts, 80, out);
        assertEquals(0.58, out[0], 1e-6, "the farms' lamp-lit 0.58 (the backdrop's lamp pools baked for it)");
        assertEquals(0xcc / 255f, out[1], 1e-6, "its tint");
        UnderwaterGrade.lookAt(looks, starts, 150, out);
        assertEquals(0.45, out[0], 1e-6, "the plain's edge darker");
        assertEquals(80, out[5], 1e-4);
    }

    /**
     * The wipe: dry before the line enters, then graded above it ("the water ahead") and dry below it until the
     * crossing, the blend after it; then the whole field by the medium's share.
     */
    @Test
    void theWipeGradesTheWaterAheadOfTheLine() {
        double[] rows = {2646};
        boolean[] into = {true};
        double[] out = new double[4];
        UnderwaterGrade.wipe(rows, into, 1000, 0, out);
        assertTrue(Double.isNaN(out[0]), "the line not on the screen yet");
        assertEquals(0, out[2]);
        UnderwaterGrade.wipe(rows, into, 2400, 0, out);
        assertEquals(246, out[0], 1e-9, "the line on the screen");
        assertEquals(1, out[1], "graded above it");
        assertEquals(0, out[3], "the first waterline (its look above it)");
        assertEquals(0, out[2], "dry below it before the crossing");
        UnderwaterGrade.wipe(rows, into, 2580, 0.4, out);
        assertEquals(0.4, out[2], 1e-9, "below it the medium's share as the water eases in");
        UnderwaterGrade.wipe(rows, into, 2700, 1, out);
        assertTrue(Double.isNaN(out[0]), "the line below the bottom edge");
        assertEquals(1, out[1]);
        assertEquals(1, out[2], "the whole field");
    }

    /** The refit flash's frame 4 (the hull's swap) falls on the waterline; nothing outside its 10 frames. */
    @Test
    void theRefitFlashSwapsTheHullOnTheWaterline() {
        double[] waterlines = {25.5};
        double frame = HydroLooks.REFIT_FRAME_TICKS / (double) SimStep.PER_SECOND;
        assertEquals(-1, HydroLooks.refitFrame(waterlines, 25.5 - 4 * frame - 0.01, 10));
        assertEquals(0, HydroLooks.refitFrame(waterlines, 25.5 - 4 * frame + 1e-9, 10));
        assertEquals(HydroLooks.SWAP_FRAME, HydroLooks.refitFrame(waterlines, 25.5 + 1e-9, 10));
        assertEquals(9, HydroLooks.refitFrame(waterlines, 25.5 + 5.5 * frame, 10));
        assertEquals(-1, HydroLooks.refitFrame(waterlines, 25.5 + 6.5 * frame, 10));
        assertEquals(-1, HydroLooks.refitFrame(new double[0], 25.5, 10), "a level without a waterline");
    }

    /** The stripe runs from the tail to the head over the 0.3 s tell; the head tells, then shoots. */
    @Test
    void theEelsStripeRunsFromTheTailToTheHead() {
        int n = 13;
        double start = EelLooks.wave(100, 100, 0);
        assertEquals(0, start, 1e-9);
        assertTrue(EelLooks.lit(n, n, start), "the tail first");
        assertFalse(EelLooks.lit(0, n, start), "not the head");
        double end = EelLooks.wave(118, 100, 0);
        assertEquals(1, end, 1e-9);
        assertTrue(EelLooks.lit(0, n, end), "the head last");
        assertFalse(EelLooks.lit(n, n, end));
        assertTrue(Double.isNaN(EelLooks.wave(119, 100, 0)), "after the tell");
        assertFalse(EelLooks.lit(5, n, Double.NaN));
        assertEquals(EelLooks.TELL, EelLooks.headState(110, 100, Long.MIN_VALUE / 2, 0));
        assertEquals(EelLooks.SHOT, EelLooks.headState(120, 100, 118, 0));
        assertEquals(0, EelLooks.headState(130, 100, 118, 0));
    }

    @Test
    void theGlassfinsSparkleRunsThroughItsFramesAndHolds() {
        assertEquals(0, GlassfinLooks.glintFrame(0, 4));
        assertEquals(1, GlassfinLooks.glintFrame(GlassfinLooks.GLINT_FRAME_TICKS, 4));
        assertEquals(3, GlassfinLooks.glintFrame(100, 4), "held at its last");
    }

    /** The rules strip from the water's factors, shown on the step the sortie goes under water (not a start under it). */
    @Test
    void theRulesStripReadsTheWatersFactors() {
        assertEquals(
                "BULLETS ×0.7 · DRAG · TORPEDOES +50 %",
                WaterlineTell.rules(ThroughIceFixture.CONTENT.europa().underwater(), "×"));
        WaterlineTell tell = new WaterlineTell();
        assertFalse(tell.step(true), "an attempt starting under water");
        tell.reset();
        assertFalse(tell.step(false));
        assertFalse(tell.step(false));
        assertTrue(tell.step(true), "the crossing");
        assertFalse(tell.step(true), "once");
    }

    @Test
    void theCausticsTileSeamlessly() {
        for (double v = 0; v < 1; v += 0.0625) {
            assertEquals(UnderwaterGrade.caustic(0, v), UnderwaterGrade.caustic(1, v), 1e-9);
            assertEquals(UnderwaterGrade.caustic(v, 0), UnderwaterGrade.caustic(v, 1), 1e-9);
        }
    }
}
