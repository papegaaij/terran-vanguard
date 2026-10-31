package vanguard.pipeline;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The dither audit's fix (step DITH8, the user 2026-10-30, design/art-direction/production#dither-audit-2026-10-30):
 * every set listed in {@code smooth-sets.txt} (an asset path without the frame suffix) was re-rendered in smooth steps
 * instead of a 4x4 Bayer ordered dither, and stays under 5 % checker: the share of its visible pixels that are a 1 px
 * checker cell, over all its frames (the measure of {@code SovereignLooksTest.checkerShare}, steps PIX7, DITH7).
 */
class DitherGuardTest {
    private static final Path ASSETS =
            Path.of(System.getProperty("vanguard.rootDir", "..")).resolve("assets");
    private static final double LIMIT = 0.05;

    static List<String> sets() throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(DitherGuardTest.class.getResourceAsStream("smooth-sets.txt")),
                StandardCharsets.UTF_8))) {
            return in.lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        }
    }

    @ParameterizedTest
    @MethodSource("sets")
    void staysUnderFivePercentChecker(String set) throws IOException {
        List<Path> frames = frames(set);
        assertFalse(frames.isEmpty(), set + ": no frames in assets/");
        long visible = 0;
        long checker = 0;
        for (Path frame : frames) {
            long[] counts = checkerCells(ImageIO.read(frame.toFile()));
            visible += counts[0];
            checker += counts[1];
        }
        double share = visible == 0 ? 0 : checker / (double) visible;
        assertTrue(share < LIMIT, set + ": a 1 px checker on " + Math.round(share * 1000) / 10.0 + " % of its pixels");
    }

    private static List<Path> frames(String set) throws IOException {
        Path dir = ASSETS.resolve(set).getParent();
        String name = ASSETS.resolve(set).getFileName().toString();
        Pattern pattern = Pattern.compile(Pattern.quote(name) + "(_\\d+)?\\.png");
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(path ->
                            pattern.matcher(path.getFileName().toString()).matches())
                    .sorted()
                    .toList();
        }
    }

    /** {visible pixels, checker cells}: a visible pixel whose neighbours left and right are alike, and up and down too,
     * both pairs unlike the pixel (an ordered dither's cell). */
    static long[] checkerCells(BufferedImage image) {
        long visible = 0;
        long checker = 0;
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                int c = image.getRGB(x, y);
                if (c >>> 24 == 0) {
                    continue;
                }
                visible++;
                int left = image.getRGB(x - 1, y);
                int up = image.getRGB(x, y - 1);
                if (left == image.getRGB(x + 1, y) && left != c && up == image.getRGB(x, y + 1) && up != c) {
                    checker++;
                }
            }
        }
        return new long[] {visible, checker};
    }
}
