package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;

/**
 * M11 part G, step G4 (design/campaign Level 49, the memory and the echoes; ART-G's reference sheet {@code
 * echo-tint-final-r94-a.png}): the spectral tint ({@link EchoTint}) is the level's alone, never on a bullet, a step
 * brighter than the tinted worlds, its edge and shimmer as the sheet's constants; the memories ({@link MemoryLooks})
 * form and melt with no flash.
 */
class Level49LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 49's remembered units (Acts 1, 3 and 5). */
    private static final List<String> UNITS = List.of(
            "skitter",
            "needler",
            "stinger",
            "spore-bomber",
            "dust-kite",
            "ghost-drone",
            "mote-swarm",
            "choir-herald",
            "dart-drone",
            "talon",
            "gilded-gunship",
            "hornet");
    /** Level 49's memories: their tile sets and deep pieces. */
    private static final List<String> WORLDS = List.of(
            "luna-regolith",
            "l1-debris",
            "valles-veils",
            "belt-rocks",
            "hellas-floor",
            "luna-earth",
            "mars-disc",
            "belt-ceres",
            "l1-point",
            "ceres-orbit");

    @Test
    void onlyALevelWithEchoesIsTinted() {
        for (int number = 43; number <= 49; number++) {
            String key = CONTENT.levelKey(number).orElseThrow();
            assertEquals(
                    number == 49, EchoTint.wanted(SimSpecs.level(CONTENT, key, Difficulty.MEDIUM)), "Level " + number);
        }
    }

    /**
     * Bullets, beams and their glows are never tinted: no draw of theirs lies between a {@code tintOn} and its {@code
     * tintOff} in {@link DrawPasses} (the shots' and overlay passes stay outside every tinted span).
     */
    @Test
    void noBulletIsDrawnUnderTheTint() throws IOException {
        String source = Files.readString(Path.of("src/main/java/vanguard/game/render/DrawPasses.java"));
        Matcher span = Pattern.compile("tintOn\\(batch\\);(.*?)tintOff\\(batch\\);", Pattern.DOTALL)
                .matcher(source);
        int spans = 0;
        while (span.find()) {
            spans++;
            String drawn = span.group(1);
            for (String bullet : List.of(
                    "drawShots", "bullets", "drawMines", "drawLines", "drawMuzzles", "overlayPass", "shotPass")) {
                assertFalse(drawn.contains(bullet), "a tinted span draws " + bullet + ":\n" + drawn);
            }
        }
        assertTrue(spans >= 5, "the units', set pieces' and debris' spans: " + spans);
    }

    /**
     * Each remembered unit, tinted at the shimmer's trough, is brighter on average than the brightest tenth of every
     * memory's pixels (the reference sheet's measured step: the units a step brighter than the tinted worlds).
     */
    @Test
    void tintedUnitsStandAboveTheWorldsBrightestTenth() throws IOException {
        double worlds = 0;
        for (String world : WORLDS) {
            worlds = Math.max(
                    worlds,
                    brightestTenth(ImageIO.read(ASSETS.resolve("backdrop/level-49/" + world + ".png")
                            .toFile())));
        }
        float trough = 1 - EchoTint.SHIMMER_AMP;
        float[] rgb = new float[3];
        float[] out = new float[3];
        for (String unit : UNITS) {
            BufferedImage image =
                    ImageIO.read(ASSETS.resolve("sprites/" + unit + "_0.png").toFile());
            double sum = 0;
            int count = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    if ((argb >>> 24) < 128) {
                        continue;
                    }
                    rgb[0] = ((argb >> 16) & 255) / 255f;
                    rgb[1] = ((argb >> 8) & 255) / 255f;
                    rgb[2] = (argb & 255) / 255f;
                    EchoTint.shade(rgb, trough, out);
                    sum += luminance(out[0], out[1], out[2]);
                    count++;
                }
            }
            double mean = sum / count;
            assertTrue(mean >= worlds, unit + ": tinted " + mean + " under the worlds' brightest tenth " + worlds);
        }
    }

    @Test
    void theTintsConstantsAreTheReferenceSheets() {
        // Desaturated 40 % toward violet-white, lifted 0.26: a grey stays grey-violet, a pure red keeps a red cast.
        float[] out = new float[3];
        EchoTint.shade(new float[] {1, 0, 0}, 1, out);
        assertTrue(out[0] > out[1], "the original's hue stays: " + Arrays.toString(out));
        EchoTint.shade(new float[] {0, 0, 0}, 1, out);
        assertEquals(EchoTint.VIOLET_W[2] * EchoTint.LIFT, out[2], 1e-6, "black lifted a step toward violet-white");
        assertEquals(10, EchoTint.edgePx(400, 300), 1e-6, "edge at most 10 px");
        assertEquals(1.5, EchoTint.edgePx(12, 12), 1e-6, "edge at least 1.5 px");
        assertEquals(4, EchoTint.edgePx(50, 80), 1e-6, "8 % of the smaller side");
        // The shimmer: ±6 % at 0.35 Hz, at most 0.5 Hz.
        assertTrue(EchoTint.SHIMMER_HZ <= 0.5f);
        double low = 2;
        double high = 0;
        for (double t = 0; t < 1 / EchoTint.SHIMMER_HZ; t += 0.01) {
            low = Math.min(low, EchoTint.shimmer(t));
            high = Math.max(high, EchoTint.shimmer(t));
        }
        assertEquals(0.94, low, 1e-3);
        assertEquals(1.06, high, 1e-3);
    }

    /** A memory forms and melts one way at every pixel: no flash, the luminance one-way. */
    @Test
    void aMemoryFormsAndMeltsWithNoFlash() {
        List<float[]> thresholds = new ArrayList<>();
        for (float r = 0; r <= 1.0001f; r += 0.05f) {
            for (float g = 0; g <= 1.0001f; g += 0.05f) {
                thresholds.add(new float[] {r, g});
            }
        }
        for (float[] at : thresholds) {
            assertEquals(0, MemoryLooks.visibility(at[0], at[1], 0, 0), 1e-6, "unformed");
            assertEquals(1, MemoryLooks.visibility(at[0], at[1], 1, 0), 1e-6, "formed");
            assertEquals(0, MemoryLooks.visibility(at[0], at[1], 1, 1), 1e-6, "melted");
            float last = 0;
            for (float p = 0; p <= 1.0001f; p += 0.02f) {
                float v = MemoryLooks.visibility(at[0], at[1], p, 0);
                assertTrue(v >= last - 1e-6, "forming one way");
                last = v;
            }
            for (float q = 0; q <= 1.0001f; q += 0.02f) {
                float v = MemoryLooks.visibility(at[0], at[1], 1, q);
                assertTrue(v <= last + 1e-6, "melting one way");
                last = v;
            }
        }
        assertEquals(0.5f, MemoryLooks.share(1), 1e-6, "over 2 s");
        assertEquals(1f, MemoryLooks.share(5), 1e-6);
    }

    private static double brightestTenth(BufferedImage image) {
        List<Double> lit = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                double a = (argb >>> 24) / 255.0;
                if (a > 0.05) {
                    lit.add(luminance(((argb >> 16) & 255) / 255f, ((argb >> 8) & 255) / 255f, (argb & 255) / 255f)
                            * a);
                }
            }
        }
        double[] sorted = lit.stream().mapToDouble(Double::doubleValue).sorted().toArray();
        return sorted[(int) Math.floor(sorted.length * 0.9)];
    }

    private static double luminance(float r, float g, float b) {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }
}
