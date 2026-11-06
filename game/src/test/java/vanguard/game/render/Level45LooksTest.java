package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.Pits;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part C, step C4: Level 45 on the play field (design/campaign Level 45; design/enemies/ground/spawning-organ;
 * design/world/vrell-space, Living terrain; tools/art/act7_reef.py): the art the looks draw, the organ's pods swelling
 * and bursting in place of its iris before a cluster, the pits' frames by their state on the placed pit pieces (which
 * lie where the simulation's pits are), a unit's rise out of a pit from the ground's scale, the veins' slow smooth
 * pulse, the tracker's {@code ORGANS n / 8} and {@code CRAWLERS n / 2}, the radio's portrait static at step 3, and
 * Vrell bullets read over the reef's brightest veins at 1:1. A small review sheet (not a check) of the organ's states
 * and a pit's four over the brood fields, the call organ and the cyst is written to {@code build/act7-reef.png}.
 */
class Level45LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-45");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(45).orElseThrow();
    private static final String ORGAN = ReefLooks.ORGAN;

    private static long frames(Path dir, String name) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static long frames(String name) throws IOException {
        return frames(SPRITES, name);
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        assertEquals(EnemyLooks.IRIS_STATES * EnemyLooks.PULSE_FRAMES, frames(ORGAN), "the Hive Node's layout");
        assertEquals(frames(ORGAN), frames(ORGAN + "-glow"));
        assertEquals(ReefLooks.SWELL_FRAMES, frames(ORGAN + "-swell"));
        assertEquals(ReefLooks.SWELL_FRAMES, frames(ORGAN + "-swell-glow"));
        assertEquals(ReefLooks.RELEASE_FRAMES, frames(ORGAN + "-release"));
        assertEquals(ReefLooks.RELEASE_FRAMES, frames(ORGAN + "-release-glow"));
        assertEquals(16, frames(ORGAN + "-tatters"), "the tear, its last frame the husk");
        assertEquals(16, frames(ORGAN + "-death"), "the flash and the ichor ring");
        assertEquals(9, frames(ORGAN + "-creep"), "alive, then going dark");
        assertTrue(Files.isRegularFile(SPRITES.resolve(ORGAN + "-stump.png")), "the torn husk");
        assertEquals(8, frames("call-organ"), "the horn breathing on the plates");
        assertEquals(StoryLooks.RISE_FRAMES, frames("call-organ-rise"));
        assertEquals(8, frames("call-organ-drift"));
        assertEquals(StoryLooks.RISE_FRAMES + 6, frames("call-organ-glow"), "the rise's ten, the drift's loop");
        assertEquals(3, frames("call-organ-root"), "whole, hit, cut");
        assertEquals(8, frames("call-organ-root-break"));
        assertEquals(3, frames("courier-cyst"), "whole, cracked, broken open");
        assertEquals(8, frames("courier-cyst-break"));
        assertEquals(PitFrames.FRAMES, frames(BACKDROP, BackdropPieces.PIT));
        assertEquals(PitFrames.FRAMES, frames(BACKDROP, BackdropPieces.PIT + BackdropPieces.GLOW));
        assertEquals(PitFrames.SHUT_FRAMES, frames(BACKDROP, "pit-shut"));
        for (String tile : List.of("rim-plates", "brood-fields", "spine", "brood-heart")) {
            assertTrue(Files.isRegularFile(BACKDROP.resolve(tile + VeinPulse.VEINS + ".png")), tile + "'s veins");
        }
    }

    @Test
    void theOrgansPodsSwellAndBurstBeforeACluster() {
        assertEquals(0, ReefLooks.swellFrame(0));
        assertEquals(3, ReefLooks.swellFrame(0.5));
        assertEquals(5, ReefLooks.swellFrame(0.99));
        assertEquals(5, ReefLooks.swellFrame(1), "held swollen");
        assertEquals(0, ReefLooks.releaseFrame(0), "the burst at the release");
        assertEquals(5, ReefLooks.releaseFrame(SimStep.ticks(0.3) - 1));
        assertEquals(-1, ReefLooks.releaseFrame(SimStep.ticks(0.3)), "then the breath again");
        assertEquals(-1, ReefLooks.releaseFrame(-1));
    }

    @Test
    void aPitsFrameFollowsItsState() {
        for (double real = 0; real < 4; real += 0.1) {
            int shut = PitFrames.frame(Pits.State.SHUT, 0, 0, real);
            assertTrue(shut >= 0 && shut < PitFrames.SHUT_FRAMES, "breathing shut");
        }
        assertEquals(8, PitFrames.frame(Pits.State.TELL, 0, 0, 0), "the tell starts");
        assertEquals(12, PitFrames.frame(Pits.State.TELL, 0.5, 60, 0));
        assertEquals(15, PitFrames.frame(Pits.State.TELL, 1, 120, 0), "its lips parted");
        assertEquals(15, PitFrames.frame(Pits.State.HELD, 1, 400, 0), "held at the last of the tell");
        assertEquals(16, PitFrames.frame(Pits.State.OPEN, 1, 0, 0));
        assertEquals(17, PitFrames.frame(Pits.State.OPEN, 1, SimStep.ticks(0.25), 0), "4 fps");
        assertEquals(16, PitFrames.frame(Pits.State.OPEN, 1, SimStep.ticks(1), 0), "a loop");
        assertEquals(20, PitFrames.frame(Pits.State.CLOSED, 0, 0, 0), "closing");
        assertEquals(23, PitFrames.frame(Pits.State.CLOSED, 0, SimStep.ticks(0.5) - 1, 0));
        assertEquals(PitFrames.shut(7.3), PitFrames.frame(Pits.State.CLOSED, 0, SimStep.ticks(0.5), 7.3), "shut again");
    }

    /**
     * The placed {@code pit} pieces are the simulation's pits in their order, at their places; flown, the first tells
     * from t=10 and opens at t=12, its frames following.
     */
    @Test
    void thePlacedPitsAreTheLevelsPits() {
        LevelData level = CONTENT.level(KEY);
        List<BackdropData.PlacedPiece> placed = level.backdrop().placements().stream()
                .filter(piece -> piece.piece().equals(BackdropPieces.PIT))
                .toList();
        Sortie sortie = sortie();
        Pits pits = sortie.pits();
        assertEquals(14, pits.count());
        assertEquals(pits.count(), placed.size(), "a placed pit a pit");
        for (int p = 0; p < pits.count(); p++) {
            assertEquals(pits.x(p), placed.get(p).x(), 1e-9, "pit " + p + "'s x");
            assertEquals(pits.renderY(p, 1), level.pieceCentre(placed.get(p)) - sortie.groundScroll(), 0.5, "pit " + p);
        }
        PitFrames frames = new PitFrames(pits.count());
        int told = -1;
        int opened = -1;
        for (int step = 0; step < SimStep.ticks(13); step++) {
            sortie.step(Command.NONE);
            frames.stepped(pits, sortie.tick());
            int frame = frames.frame(0, sortie.realSeconds());
            if (frame >= PitFrames.TELL_FIRST && frame < PitFrames.OPEN_FIRST && told < 0) {
                told = step;
            }
            if (frame >= PitFrames.OPEN_FIRST && frame < PitFrames.CLOSE_FIRST && opened < 0) {
                opened = step;
            }
            for (int p = 1; p < pits.count(); p++) {
                assertTrue(frames.frame(p, sortie.realSeconds()) < PitFrames.SHUT_FRAMES, "the others shut");
            }
        }
        assertEquals(SimStep.ticks(10), told, SimStep.ticks(0.1), "the tell from t=10");
        assertEquals(SimStep.ticks(12), opened, SimStep.ticks(0.1), "open at t=12");
        frames.restart();
        assertTrue(frames.frame(0, 0) < PitFrames.SHUT_FRAMES, "a restart shuts it");
    }

    @Test
    void aUnitRisesFromTheGroundsScale() {
        assertEquals(1 / 1.43, ReefLooks.GROUND_SCALE, 1e-3);
        assertEquals(ReefLooks.GROUND_SCALE, ReefLooks.riseScale(0), 1e-6);
        assertEquals(1, ReefLooks.riseScale(1), 1e-6);
        assertEquals(1, ReefLooks.riseShade(1), 1e-6);
        assertEquals(0, ReefLooks.shadowSlide(0), 1e-6, "its shadow under it at the mouth");
        float last = 0;
        for (double s = 0; s <= 1; s += 0.05) {
            assertTrue(ReefLooks.riseScale(s) >= last, "growing");
            last = ReefLooks.riseScale(s);
        }
    }

    /** One slow, smooth breath: none to full and back over 4 s (0.25 Hz), small steps a frame. */
    @Test
    void theVeinsPulseSlowlyAndSmoothly() {
        assertEquals(0, VeinPulse.share(0), 1e-6);
        assertEquals(1, VeinPulse.share(VeinPulse.PERIOD_SECONDS / 2), 1e-6);
        assertEquals(0, VeinPulse.share(VeinPulse.PERIOD_SECONDS), 1e-6);
        assertTrue(1 / VeinPulse.PERIOD_SECONDS <= 0.5, "at most 0.5 Hz");
        for (double t = 0; t < 8; t += 1 / 60.0) {
            assertTrue(Math.abs(VeinPulse.share(t + 1 / 60.0) - VeinPulse.share(t)) < 0.015, "smooth at t=" + t);
        }
    }

    @Test
    void theTrackerCountsTheOrgansAndTheCrawlers() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        assertEquals("ORGANS", MissionPanel.countedLabel(script.groups(), script.groundUnits()));
        assertEquals("0 / 8", MissionPanel.countedTargets(0, 8, false));
        assertEquals("FAILED", MissionPanel.countedTargets(3, 8, true), "one organ away fails at once");
        assertEquals("CRAWLERS", script.objectives().secondary().label());
        assertEquals("0 / 2", MissionPanel.escapesCount(0, 3, 1), "2 of the 3 crawlers");
        assertEquals(3, CONTENT.level(KEY).radioNoiseStep());
        assertEquals(0.3f, MissionPanel.portraitNoise(0, 3, true), 1e-6, "the friendly portraits' steady static");
        assertEquals(0, MissionPanel.portraitNoise(0, 3, false), 1e-6, "the Choir's clean");
    }

    /**
     * At 1:1 over the brightest 15 px window of each ground image with its veins at the pulse's full: an enemy orb's
     * white core stands far above the brightest vein, its dark rim below the window's mean, its magenta ring apart from
     * the mint veins (the bullets are never lost in the reef).
     */
    @Test
    void vrellBulletsReadOverTheBrightestVeins() throws IOException {
        List<String> grounds = List.of(
                "rim-plates",
                "brood-fields",
                "spine",
                "brood-heart",
                "seam-2",
                "seam-3",
                "reef-edge",
                "second-reef-1",
                "second-reef-2",
                "second-reef-3",
                "second-reef-4",
                "second-reef-5",
                "far-rim");
        for (String orbName : List.of("orb_0", "orb-medium_0")) {
            BufferedImage orb = ImageIO.read(SPRITES.resolve(orbName + ".png").toFile());
            for (String ground : grounds) {
                float[][] field = lit(ground);
                BufferedImage tile =
                        ImageIO.read(BACKDROP.resolve(ground + ".png").toFile());
                int w = tile.getWidth();
                int h = tile.getHeight();
                int size = 15;
                int bestX = 0;
                int bestY = 0;
                double best = -1;
                for (int y = 0; y + size <= h; y += 3) {
                    for (int x = 0; x + size <= w; x += 3) {
                        double sum = 0;
                        for (int dy = 0; dy < size; dy++) {
                            for (int dx = 0; dx < size; dx++) {
                                sum += lum(field[(y + dy) * w + x + dx]);
                            }
                        }
                        if (sum > best) {
                            best = sum;
                            bestX = x;
                            bestY = y;
                        }
                    }
                }
                double mean = best / (size * size);
                double brightest = 0;
                float[] mint = null;
                for (int dy = 0; dy < size; dy++) {
                    for (int dx = 0; dx < size; dx++) {
                        float[] p = field[(bestY + dy) * w + bestX + dx];
                        if (lum(p) > brightest) {
                            brightest = lum(p);
                            mint = p;
                        }
                    }
                }
                assertTrue(mint[1] > mint[0], ground + ": the brightest vein is mint (green over red)");
                double core = 0;
                double rim = 255;
                int ox = bestX + size / 2 - orb.getWidth() / 2;
                int oy = bestY + size / 2 - orb.getHeight() / 2;
                for (int y = 0; y < orb.getHeight(); y++) {
                    for (int x = 0; x < orb.getWidth(); x++) {
                        int argb = orb.getRGB(x, y);
                        float a = (argb >>> 24) / 255f;
                        if (a < 0.8f || ox + x < 0 || oy + y < 0 || ox + x >= w || oy + y >= h) {
                            continue;
                        }
                        float[] under = field[(oy + y) * w + ox + x];
                        float[] seen = new float[3];
                        for (int c = 0; c < 3; c++) {
                            seen[c] = under[c] * (1 - a) + (argb >> (16 - 8 * c) & 255) * a;
                        }
                        core = Math.max(core, lum(seen));
                        rim = Math.min(rim, lum(seen));
                        if ((argb >> 8 & 255) < 64 && (argb >> 16 & 255) > 150) {
                            assertTrue(seen[0] > seen[1] + 100, ground + ": the ring stays magenta");
                        }
                    }
                }
                assertTrue(core - brightest >= 80, orbName + " over " + ground + ": core " + core + " vs " + brightest);
                assertTrue(mean - rim >= 30, orbName + " over " + ground + ": rim " + rim + " vs mean " + mean);
            }
        }
    }

    /** A ground image with its veins added at the pulse's full, RGB 0-255 per pixel (alpha-weighted). */
    private static float[][] lit(String ground) throws IOException {
        BufferedImage tile = ImageIO.read(BACKDROP.resolve(ground + ".png").toFile());
        BufferedImage veins =
                ImageIO.read(BACKDROP.resolve(ground + VeinPulse.VEINS + ".png").toFile());
        int w = tile.getWidth();
        int h = tile.getHeight();
        float[][] out = new float[w * h][3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int t = tile.getRGB(x, y);
                int v = veins.getRGB(x, y);
                float ta = (t >>> 24) / 255f;
                float va = (v >>> 24) / 255f;
                for (int c = 0; c < 3; c++) {
                    int shift = 16 - 8 * c;
                    out[y * w + x][c] = Math.min(255, (t >> shift & 255) * ta + (v >> shift & 255) * va);
                }
            }
        }
        return out;
    }

    private static double lum(float[] rgb) {
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }

    private static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    // A small review sheet (not a check).

    /**
     * {@code build/act7-reef.png}: over the brood fields with their veins at full, the organ idle, its iris open, swollen,
     * bursting, its torn husk on its dark creep; a pit shut, telling, open and closing with its glow; the call organ on
     * the plates and lifted, a root, the cyst whole and broken.
     */
    @Test
    void writesASmallReviewSheet() throws IOException {
        float[][] field = lit("brood-fields");
        int fw = 480;
        BufferedImage sheet = new BufferedImage(6 * 100, 3 * 110, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < sheet.getHeight(); y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                float[] p = field[(y % 960) * fw + x % fw];
                sheet.setRGB(x, y, (int) p[0] << 16 | (int) p[1] << 8 | (int) p[2]);
            }
        }
        String[][] organ = {
            {ORGAN + "_0", ORGAN + "-glow_0"},
            {ORGAN + "_22", ORGAN + "-glow_22"},
            {ORGAN + "-swell_5", ORGAN + "-swell-glow_5"},
            {ORGAN + "-release_1", ORGAN + "-release-glow_1"},
            {ORGAN + "-creep_8", null},
            {ORGAN + "-stump", null}
        };
        for (int k = 0; k < organ.length; k++) {
            place(sheet, organ[k][0], 50 + k * 100, 55, false);
            if (organ[k][1] != null) {
                place(sheet, organ[k][1], 50 + k * 100, 55, true);
            }
        }
        place(sheet, ORGAN + "-stump", 550, 55, false);
        int[] pit = {0, 12, 17, 21};
        for (int k = 0; k < pit.length; k++) {
            placeFrom(sheet, BACKDROP.resolve("pit_" + pit[k] + ".png"), 50 + k * 100, 165, false);
            placeFrom(sheet, BACKDROP.resolve("pit-glow_" + pit[k] + ".png"), 50 + k * 100, 165, true);
        }
        place(sheet, "call-organ_0", 450, 165, false);
        place(sheet, "call-organ-drift_0", 550, 165, false);
        place(sheet, "call-organ-glow_12", 550, 165, true);
        place(sheet, "call-organ-root_0", 50, 275, false);
        place(sheet, "call-organ-root_2", 150, 275, false);
        place(sheet, "courier-cyst_0", 250, 275, false);
        place(sheet, "courier-cyst_2", 350, 275, false);
        place(sheet, "orb_0", 450, 275, false);
        place(sheet, "orb-medium_0", 550, 275, false);
        Path out = Path.of("build", "act7-reef.png");
        Files.createDirectories(out.getParent());
        ImageIO.write(sheet, "png", out.toFile());
        assertTrue(Files.size(out) > 0);
    }

    private static void place(BufferedImage sheet, String sprite, int cx, int cy, boolean additive) throws IOException {
        placeFrom(sheet, SPRITES.resolve(sprite + ".png"), cx, cy, additive);
    }

    private static void placeFrom(BufferedImage sheet, Path file, int cx, int cy, boolean additive) throws IOException {
        BufferedImage sprite = ImageIO.read(file.toFile());
        int left = cx - sprite.getWidth() / 2;
        int top = cy - sprite.getHeight() / 2;
        for (int y = 0; y < sprite.getHeight(); y++) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                int sx = left + x;
                int sy = top + y;
                if (sx < 0 || sy < 0 || sx >= sheet.getWidth() || sy >= sheet.getHeight()) {
                    continue;
                }
                int argb = sprite.getRGB(x, y);
                int base = sheet.getRGB(sx, sy);
                float a = (argb >>> 24) / 255f;
                int rgb = 0;
                for (int shift = 16; shift >= 0; shift -= 8) {
                    float b = base >> shift & 255;
                    float s = argb >> shift & 255;
                    int v = Math.round(additive ? Math.min(255, b + s * a) : b * (1 - a) + s * a);
                    rgb |= v << shift;
                }
                sheet.setRGB(sx, sy, rgb);
            }
        }
    }
}
