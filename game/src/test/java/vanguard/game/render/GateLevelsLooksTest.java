package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part B, step B4: Levels 43 and 44 on the play field (design/campaign Levels 43 and 44; design/enemies/space,
 * the Gate Warden and the Rift Skater; design/world/vrell-space, the gate transit and the event plane): the Warden's
 * frames by its shell's state, the Skater's glint, the gate ring's pieces cross-fading at the section changes, the
 * event plane's Vrell space staying under its 800 px whatever the drift, the transit's warp (each pixel's brightness
 * one way only, toward the violet, smooth, no flash), and a small review sheet of the Warden and the Skater over their
 * levels' tiles with the warp's frame strip and luminance plot ({@code build/act7-gate-levels.png}).
 */
class GateLevelsLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();

    // The Gate Warden.

    @Test
    void theWardensFramesFollowItsShell() {
        assertEquals(0, WardenLooks.irisFrame(0, 6), "closed");
        assertEquals(3, WardenLooks.irisFrame(0.55, 6));
        assertEquals(5, WardenLooks.irisFrame(1, 6), "open");
        assertEquals(-1, WardenLooks.tiltFrame(0, 4), "upright outside the tell");
        assertEquals(0, WardenLooks.tiltFrame(0.1, 4));
        assertEquals(3, WardenLooks.tiltFrame(1, 4), "16 degrees once eased in");
        assertEquals(0, WardenLooks.eyeFrame(0, 8));
        assertEquals(7, WardenLooks.eyeFrame(1, 8), "held through the beam");
        int regrow = SimStep.ticks(0.4);
        assertEquals(0, WardenLooks.regrowFrame(0, regrow, 8));
        assertEquals(7, WardenLooks.regrowFrame(regrow, regrow, 8));
        // The sweep: 12 frames over 2.5 s.
        int first = WardenLooks.sweepFrame(0, 0, 12);
        assertEquals(first, WardenLooks.sweepFrame(SimStep.ticks(2.5), 0, 12), "one pass in 2.5 s");
        assertTrue(WardenLooks.sweepFrame(SimStep.ticks(1.25), 0, 12) != first, "half way");
    }

    @Test
    void theSkatersGlintRunsOverTheQuarterSecondBeforeItsBurst() {
        assertEquals(0, SkaterLooks.glintFrame(0, 0.25, 6));
        assertEquals(5, SkaterLooks.glintFrame(SimStep.ticks(0.25) - 1, 0.25, 6));
        assertEquals(-1, SkaterLooks.glintFrame(SimStep.ticks(0.25), 0.25, 6), "gone at the first shot");
        assertEquals(-1, SkaterLooks.glintFrame(-1, 0.25, 6));
    }

    @Test
    void theWardenAndTheSkaterBreakUpWithoutAWreck() {
        assertTrue(Files.isRegularFile(SPRITES.resolve("gate-warden-slabs_15.png")), "the slabs");
        assertTrue(Files.isRegularFile(SPRITES.resolve("rift-skater-tatters_11.png")), "the tatters");
    }

    // The gate ring's pieces.

    @Test
    void theRingsPiecesCrossFadeAtTheSectionChanges() {
        Map<String, BackdropData.PlacedPiece> ring =
                CONTENT.level(CONTENT.levelKey(43).orElseThrow()).backdrop().placements().stream()
                        .filter(placed -> BackdropPieces.CROSS_FADED.contains(placed.piece()))
                        .collect(Collectors.toMap(BackdropData.PlacedPiece::piece, placed -> placed));
        assertEquals(5, ring.size(), "far, third, top, rim and the mouth");
        String[][] swaps = {{"ring-far", "ring-third"}, {"ring-third", "ring-top"}, {"ring-top", "ring-rim"}};
        double[] at = {48, 124, 200};
        for (int s = 0; s < swaps.length; s++) {
            BackdropData.PlacedPiece out = ring.get(swaps[s][0]);
            BackdropData.PlacedPiece in = ring.get(swaps[s][1]);
            assertEquals(at[s], BackdropPieces.parkedFrom(out), 1e-9, swaps[s][0] + " parks at " + at[s]);
            assertEquals(at[s], BackdropPieces.unparkedAt(in), 1e-9, swaps[s][1] + " comes on at " + at[s]);
            for (double t = at[s] - 1.5; t <= at[s] + 1.5; t += 0.05) {
                float sum = BackdropPieces.fadeAlpha(out, t) + BackdropPieces.fadeAlpha(in, t);
                assertEquals(1, sum, 1e-5, "they cross at t=" + t);
                if (BackdropPieces.fadeAlpha(out, t) > 0) {
                    assertTrue(
                            Math.abs(out.offsetX(BackdropPieces.fadeTime(out, t))) < BackdropPieces.PARKED,
                            "held while it shows: t=" + t);
                }
                if (BackdropPieces.fadeAlpha(in, t) > 0) {
                    assertTrue(
                            Math.abs(in.offsetX(BackdropPieces.fadeTime(in, t))) < BackdropPieces.PARKED,
                            "held while it shows: t=" + t);
                }
            }
            assertEquals(1, BackdropPieces.fadeAlpha(out, at[s] - 1.01), 1e-6, "whole before the fade");
            assertEquals(0, BackdropPieces.fadeAlpha(out, at[s] + 1.01), 1e-6, "gone after it");
        }
        BackdropData.PlacedPiece mouth = ring.get("mouth");
        assertEquals(256, BackdropPieces.unparkedAt(mouth), 1e-9, "the mouth comes on at t=256");
        assertTrue(Double.isNaN(BackdropPieces.parkedFrom(mouth)), "and stays to the end");
    }

    /**
     * Level 44: from t=252 Vrell space behind the parting curtain (800 px wide, centred) slides only by the drift since
     * it came on; the layer's whole slide by then would leave it short.
     */
    @Test
    void theSpaceBehindTheCurtainStaysUnderItsWidth() {
        String key = CONTENT.levelKey(44).orElseThrow();
        BackdropData backdrop = CONTENT.level(key).backdrop();
        double factor = backdrop.factor(vanguard.content.BackdropLayer.DEEP);
        for (BackdropData.PlacedPiece placed : backdrop.placements()) {
            if (List.of("beyond", "curtain-left", "curtain-right").contains(placed.piece())) {
                assertTrue(BackdropPieces.parks(placed), placed.piece() + " parks before t=252");
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, key, difficulty);
            Sortie sortie = new Sortie(
                    1,
                    SimSpecs.loadout(
                            CONTENT,
                            CONTENT.systems().engines().getFirst().name(),
                            List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                            CONTENT.shields().models().getFirst().name(),
                            CONTENT.armour().plating().getFirst().name(),
                            30,
                            difficulty),
                    script,
                    SimSpecs.rules(CONTENT, key, difficulty).withInvulnerableShip(),
                    60);
            double slide = 0;
            double on = Double.NaN;
            double most = 0;
            for (int step = 0; step < SimStep.ticks(270) && !sortie.complete(); step++) {
                sortie.step(0);
                slide += sortie.current().driftStep();
                if (sortie.levelSeconds() >= 252) {
                    if (Double.isNaN(on)) {
                        on = slide;
                    }
                    most = Math.max(
                            most, Math.abs(BackdropTiles.lateral(slide, factor) - BackdropTiles.lateral(on, factor)));
                }
            }
            assertTrue(most <= 160, difficulty + ": " + most + " px of slide under the 800 px piece");
        }
    }

    /**
     * Level 44's tile sets wrap across the field (the current slides them sideways): the seam between a tile's last and
     * first column is no bigger a step than its columns' own.
     */
    @Test
    void theEventPlanesTilesWrapAcross() throws IOException {
        BackdropData backdrop =
                CONTENT.level(CONTENT.levelKey(44).orElseThrow()).backdrop();
        for (String tileSet : backdrop.tileSets().keySet()) {
            BufferedImage tile = ImageIO.read(
                    ASSETS.resolve("backdrop/level-44/" + tileSet + ".png").toFile());
            int w = tile.getWidth();
            double inner = 0;
            double seam = 0;
            for (int y = 0; y < tile.getHeight(); y++) {
                for (int x = 0; x + 1 < w; x++) {
                    inner += step(tile.getRGB(x, y), tile.getRGB(x + 1, y));
                }
                seam += step(tile.getRGB(w - 1, y), tile.getRGB(0, y));
            }
            inner /= (double) (w - 1) * tile.getHeight();
            seam /= tile.getHeight();
            assertTrue(seam <= 3 * inner + 2, tileSet + ": the x seam " + seam + " against " + inner + " a column");
        }
    }

    /** The biggest channel step between two ARGB pixels, alpha weighted, 0-255. */
    private static double step(int a, int b) {
        double most = Math.abs((a >>> 24) - (b >>> 24));
        for (int shift = 0; shift < 24; shift += 8) {
            most = Math.max(most, Math.abs((a >> shift & 255) - (b >> shift & 255)));
        }
        return most;
    }

    // The transit's warp.

    /**
     * Over the warp's 1.5 s at 60 frames a second on Level 43's last field (the mouth over the transit's deep tile):
     * every pixel's luminance moves one way only, toward the violet's (never past it: no flash, no inversion), the
     * field's mean in small steps, the field the violet alone at the end; Level 44's opening runs the same back.
     */
    @Test
    void theWarpGradesEveryPixelOneWayToTheViolet() throws IOException {
        int width = 120;
        int height = 135;
        float[] field = fieldOf43(width, height);
        float[] out = new float[field.length];
        float[] last = new float[width * height];
        float violet = TransitWarp.luminance(TransitWarp.VIOLET, 0);
        double lastMean = mean(field, width * height);
        double biggestStep = 0;
        int frames = SimStep.ticks(1.5);
        for (int i = 0; i < width * height; i++) {
            last[i] = TransitWarp.luminance(field, i * 3);
        }
        for (int f = 1; f <= frames; f++) {
            TransitWarp.shade(field, width, height, (double) f / frames, out);
            for (int i = 0; i < width * height; i++) {
                float own = TransitWarp.luminance(field, i * 3);
                float now = TransitWarp.luminance(out, i * 3);
                float low = Math.min(own, violet) - 1e-4f;
                float high = Math.max(own, violet) + 1e-4f;
                assertTrue(now >= low && now <= high, "pixel " + i + " stays between its own and the violet");
                if (own < violet) {
                    assertTrue(now >= last[i] - 1e-4f, "pixel " + i + " only brightens, frame " + f);
                } else {
                    assertTrue(now <= last[i] + 1e-4f, "pixel " + i + " only darkens, frame " + f);
                }
                last[i] = now;
            }
            double m = mean(out, width * height);
            biggestStep = Math.max(biggestStep, Math.abs(m - lastMean));
            lastMean = m;
        }
        assertTrue(biggestStep < 0.02, "smooth: the mean's biggest step a frame " + biggestStep);
        for (int i = 0; i < width * height; i++) {
            assertEquals(violet, TransitWarp.luminance(out, i * 3), 1e-3, "the violet alone at the end");
        }
        writeSheet(field, width, height);
    }

    private static double mean(float[] rgb, int pixels) {
        double sum = 0;
        for (int i = 0; i < pixels; i++) {
            sum += TransitWarp.luminance(rgb, i * 3);
        }
        return sum / pixels;
    }

    /** Level 43's field near its end, quartered: the transit's deep tile with the mouth over its top (rows from the bottom). */
    private static float[] fieldOf43(int width, int height) throws IOException {
        BufferedImage deep =
                ImageIO.read(ASSETS.resolve("backdrop/level-43/deep-5.png").toFile());
        BufferedImage mouth =
                ImageIO.read(ASSETS.resolve("backdrop/level-43/mouth.png").toFile());
        float[] rgb = new float[width * height * 3];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int x = col * 4;
                int yTop = (height - 1 - row) * 4;
                int base = deep.getRGB(x % deep.getWidth(), yTop % deep.getHeight());
                int over = yTop < mouth.getHeight() ? mouth.getRGB(x, yTop) : 0;
                float a = (over >>> 24) / 255f;
                int i = (row * width + col) * 3;
                for (int c = 0; c < 3; c++) {
                    int shift = 16 - 8 * c;
                    rgb[i + c] = ((base >> shift & 255) * (1 - a) + (over >> shift & 255) * a) / 255f;
                }
            }
        }
        return rgb;
    }

    /**
     * The review sheet: the Warden (closed, half open, open, bare, tilted, with its glyph light) over Level 43's pickets
     * tile, the Skater (four headings, its glow and glint) over Level 44's current, and the warp's strip at 0, 0.25,
     * 0.5, 0.75 and 1 with the field's mean luminance plotted under it.
     */
    private static void writeSheet(float[] field, int width, int height) throws IOException {
        BufferedImage sheet = new BufferedImage(5 * width, 2 * 140 + height + 60, BufferedImage.TYPE_INT_RGB);
        BufferedImage pickets =
                ImageIO.read(ASSETS.resolve("backdrop/level-43/deep-2.png").toFile());
        BufferedImage plane =
                ImageIO.read(ASSETS.resolve("backdrop/level-44/plane-2.png").toFile());
        for (int y = 0; y < 140; y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                sheet.setRGB(x, y, pickets.getRGB(x % pickets.getWidth(), (y + 100) % pickets.getHeight()));
                sheet.setRGB(x, 140 + y, plane.getRGB(x % plane.getWidth(), (y + 300) % plane.getHeight()));
            }
        }
        String[] warden = {"gate-warden_0", "gate-warden_3", "gate-warden_5", "gate-warden-bare", "gate-warden-tilt_3"};
        float[] glyphs = {0.18f, 0.5f, 0.8f, 0.8f, 0.1f};
        for (int k = 0; k < warden.length; k++) {
            int left = k * width + (width - 120) / 2;
            paint(sheet, sprite(warden[k]), left, 4, false, 1);
            paint(sheet, sprite(k == 4 ? "gate-warden-tilt-glyphs_3" : "gate-warden-glyphs"), left, 4, true, glyphs[k]);
            if (k == 0) {
                paint(sheet, sprite("gate-warden-sweep_5"), left, 4, true, 1);
            }
            if (k == 2) {
                paint(sheet, sprite("gate-warden-eye_7"), left + 44, 4 + 50, true, 1);
            }
        }
        int[] headings = {0, 4, 8, 12, 2};
        for (int k = 0; k < headings.length; k++) {
            int left = k * width + (width - 48) / 2;
            paint(sheet, sprite("rift-skater_" + headings[k]), left, 140 + 46, false, 1);
            paint(sheet, sprite("rift-skater-glow"), left + 12, 140 + 58, true, 1);
            if (k == 4) {
                paint(sheet, sprite("rift-skater-glint_3"), left + 12, 140 + 58, true, 1);
            }
        }
        float[] out = new float[field.length];
        double[] means = new double[91];
        for (int f = 0; f <= 90; f++) {
            TransitWarp.shade(field, width, height, f / 90.0, out);
            means[f] = mean(out, width * height);
            if (f % 22 == 0 || f == 90) {
                int strip = Math.min(4, f / 22);
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        int i = (row * width + col) * 3;
                        int r = Math.round(out[i] * 255);
                        int g = Math.round(out[i + 1] * 255);
                        int b = Math.round(out[i + 2] * 255);
                        sheet.setRGB(strip * width + col, 280 + height - 1 - row, r << 16 | g << 8 | b);
                    }
                }
            }
        }
        int plotTop = 280 + height;
        for (int x = 0; x < sheet.getWidth(); x++) {
            int f = Math.min(90, x * 90 / (sheet.getWidth() - 1));
            int y = plotTop + 59 - (int) Math.round(means[f] * 59);
            sheet.setRGB(x, y, 0xffffff);
        }
        Path file = Path.of("build", "act7-gate-levels.png");
        Files.createDirectories(file.getParent());
        ImageIO.write(sheet, "png", file.toFile());
    }

    private static BufferedImage sprite(String name) throws IOException {
        return ImageIO.read(SPRITES.resolve(name + ".png").toFile());
    }

    private static void paint(
            BufferedImage sheet, BufferedImage sprite, int left, int top, boolean additive, float strength) {
        for (int y = 0; y < sprite.getHeight(); y++) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                int sx = left + x;
                int sy = top + y;
                if (sx < 0 || sy < 0 || sx >= sheet.getWidth() || sy >= sheet.getHeight()) {
                    continue;
                }
                int argb = sprite.getRGB(x, y);
                int base = sheet.getRGB(sx, sy);
                float a = additive ? strength : (argb >>> 24) / 255f;
                int rgb = 0;
                for (int c = 0; c < 3; c++) {
                    int shift = 16 - 8 * c;
                    int was = base >> shift & 255;
                    int own = argb >> shift & 255;
                    int now = additive ? Math.min(255, Math.round(was + own * a)) : Math.round(was * (1 - a) + own * a);
                    rgb |= now << shift;
                }
                sheet.setRGB(sx, sy, rgb);
            }
        }
    }
}
