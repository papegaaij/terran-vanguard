package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import vanguard.game.settings.Scaling;

/** The mouse pointer's mapping from the window into the pixel screen, the inverse of the letterbox. */
class ViewportTest {
    private static final float EPSILON = 1e-3f;

    @Test
    void anExactIntegerScaleFillsTheWindow() {
        for (int scale = 1; scale <= 4; scale++) {
            int width = PixelScreen.WIDTH * scale;
            int height = PixelScreen.HEIGHT * scale;
            var viewport = PixelScreen.Viewport.of(width, height, Scaling.INTEGER);

            assertEquals(0, viewport.x());
            assertEquals(0, viewport.y());
            assertEquals(0, viewport.screenX(0, width, width), EPSILON);
            assertEquals(0, viewport.screenY(0, height, height), EPSILON);
            assertEquals(480, viewport.screenX(480 * scale, width, width), EPSILON);
            assertEquals(100, viewport.screenY(100 * scale, height, height), EPSILON, "y runs down");
            assertEquals(959.5f, viewport.screenX(959.5f * scale, width, width), EPSILON);
        }
    }

    @Test
    void theLetterboxBarsLieOutsideTheScreen() {
        // 1920x1200 at integer scale 2: 1920x1080, bars of 60 px above and below.
        var viewport = PixelScreen.Viewport.of(1920, 1200, Scaling.INTEGER);
        assertEquals(60, viewport.y());

        assertEquals(0, viewport.screenY(60, 1200, 1200), EPSILON);
        assertEquals(540, viewport.screenY(1140, 1200, 1200), EPSILON);
        assertEquals(-10, viewport.screenY(40, 1200, 1200), EPSILON, "the top bar");
        // 2560x1440 at integer scale 2: bars of 320 px left and right, 180 above and below.
        var wide = PixelScreen.Viewport.of(2560, 1440, Scaling.INTEGER);

        assertEquals(0, wide.screenX(320, 2560, 2560), EPSILON);
        assertEquals(0, wide.screenY(180, 1440, 1440), EPSILON);
        assertEquals(960, wide.screenX(2240, 2560, 2560), EPSILON);
        for (int scale = 1; scale <= 4; scale++) {
            int width = PixelScreen.WIDTH * scale + 2 * 37;
            int height = PixelScreen.HEIGHT * scale + 2 * 23;
            var boxed = PixelScreen.Viewport.of(width, height, Scaling.INTEGER);
            assertEquals(scale, boxed.scale(), EPSILON);
            assertEquals(0, boxed.screenX(37, width, width), EPSILON);
            assertEquals(0, boxed.screenY(23, height, height), EPSILON);
            assertEquals(10, boxed.screenX(37 + 10 * scale, width, width), EPSILON);
        }
    }

    @Test
    void aHiDpiWindowMapsFromLogicalUnits() {
        // A Retina window: 1440x810 logical units, a 2880x1620 back buffer at integer scale 3.
        var viewport = PixelScreen.Viewport.of(2880, 1620, Scaling.INTEGER);

        assertEquals(0, viewport.screenX(0, 1440, 2880), EPSILON);
        assertEquals(480, viewport.screenX(720, 1440, 2880), EPSILON);
        assertEquals(270, viewport.screenY(405, 810, 1620), EPSILON);
    }

    @Test
    void sharpBilinearFillsAtAnyScale() {
        var viewport = PixelScreen.Viewport.of(1600, 900, Scaling.SHARP_BILINEAR);

        assertEquals(0, viewport.x());
        assertEquals(480, viewport.screenX(800, 1600, 1600), EPSILON);
        assertEquals(540, viewport.screenY(900, 900, 900), EPSILON);
    }
}
