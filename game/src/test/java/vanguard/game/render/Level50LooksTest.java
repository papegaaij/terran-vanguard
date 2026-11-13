package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H4: Level 50 on the play field (design/campaign Level 50; design/enemies/bosses/choir-heart;
 * tools/art/choir_heart.py, tools/art/act7_core.py): the art the looks draw (the hive core's lights and the race's strip
 * at half size); the pulse smooth, slow, peaking on the beat and below a bullet's core; the membranes' frames; the dark's
 * ambient a quarter back per node cut (the HUD and the bullets never under it: they are drawn after it); the Heart's
 * flares only from living parts in its fight; the closing gate's warp home one way; the neutral gate banner; the
 * Choir's face-like frame once, then its loop.
 */
class Level50LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-50");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(50).orElseThrow();
    private static final String H = HeartLooks.SLUG;

    private static long frames(Path dir, String name) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static JsonValue pivots() throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + H + ".json")));
    }

    private static SetPieceSpec heart() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        return script.setPieces().stream()
                .filter(spec -> HeartLooks.drawn(spec))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        assertTrue(Files.isRegularFile(SPRITES.resolve(H + ".png")));
        assertTrue(Files.isRegularFile(SPRITES.resolve(H + "-halo.png")));
        assertEquals(4, frames(SPRITES, H + "-closing"));
        assertEquals(8, frames(SPRITES, H + "-opening"));
        assertEquals(12, frames(SPRITES, H + "-collapse"));
        assertEquals(3, frames(SPRITES, H + "-pulse"), "sealed, contracted, open");
        for (int m = 1; m <= 6; m++) {
            assertEquals(13, frames(SPRITES, H + "-membrane-" + m), "closed, iris, open, damaged, dead");
            assertEquals(8, frames(SPRITES, H + "-membrane-" + m + "-singer"));
        }
        assertEquals(3, frames(SPRITES, H + "-node-l"));
        assertEquals(3, frames(SPRITES, H + "-node-r"));
        assertEquals(2, frames(SPRITES, H + "-core"));
        for (String set :
                new String[] {"-wall-l", "-wall-r", "-wall-l-edge", "-wall-r-edge", "-wall-l-veins", "-wall-r-veins"}) {
            assertEquals(8, frames(SPRITES, H + set), set);
        }
        assertEquals(10, frames(SPRITES, H + "-chain"));
        assertEquals(6, frames(SPRITES, H + "-centre"));
        assertTrue(frames(SPRITES, H + "-core-flare") > 0);
        assertEquals(48, frames(SPRITES.resolve("portraits"), "radio-the-choir-face"), "the face-like frame");
        // The closing gate's kit, the race's strip and the tile sets' lights at half size (the atlas budget).
        for (String name : new String[] {
            "arena-grey",
            "grey-mask",
            "plane-disc",
            "plane-rim",
            "near-space",
            "neptune",
            "ring-arc",
            "ring-arc-glyphs",
            "glyph-order",
            "steadfast-group",
            "pulse-light"
        }) {
            assertTrue(Files.isRegularFile(BACKDROP.resolve(name + ".png")), name);
        }
        assertEquals(8, frames(BACKDROP, "fold"));
        for (int k = 1; k <= 5; k++) {
            BufferedImage race =
                    ImageIO.read(BACKDROP.resolve("race-" + k + ".png").toFile());
            assertEquals(ClosingGateLooks.RACE_HEIGHT / 2, race.getHeight(), "race-" + k + " at half size");
            assertEquals(240, race.getWidth());
        }
        LevelData level = CONTENT.level(KEY);
        for (String id : level.backdrop().tileSets().keySet()) {
            Path veins = BACKDROP.resolve(id + VeinPulse.VEINS + ".png");
            if (Files.isRegularFile(veins)) {
                BufferedImage image = ImageIO.read(veins.toFile());
                // At half size, but the near side's sparse deep veins (1:1: halved they read as a checker).
                int scale = id.equals("deep-8") ? 1 : 2;
                assertEquals(480 / scale, image.getWidth(), id + "'s veins");
                assertEquals(level.backdrop().tileSets().get(id).height(), scale * image.getHeight(), id);
            }
        }
        assertTrue(level.backdrop().pulse().isPresent(), "the pulse light per section");
    }

    @Test
    void thePulseIsSmoothSlowAndPeaksOnTheBeat() throws IOException {
        for (double period : new double[] {2, 3}) {
            assertEquals(1, HeartLooks.pulse(0, period), 1e-9, "the peak on the beat");
            assertEquals(0, HeartLooks.pulse(period / 2, period), 1e-9);
            assertEquals(1, HeartLooks.pulse(period, period), 1e-9, "back to the peak at the next beat");
            for (double t = 0; t < period; t += 1 / 60.0) {
                double step = Math.abs(HeartLooks.pulse(t + 1 / 60.0, period) - HeartLooks.pulse(t, period));
                assertTrue(step <= Math.PI / period / 60 + 1e-9, "smooth, at most 1 / period Hz: " + step);
            }
        }
        // The pulse's brightest stays below a bullet's white core (ART-H: luminance 231 at its peak).
        double brightest = 0;
        for (int f = 0; f < 3; f++) {
            brightest = Math.max(
                    brightest,
                    maxLuminance(ImageIO.read(
                            SPRITES.resolve(H + "-pulse_" + f + ".png").toFile())));
        }
        assertTrue(brightest < 240, "the pulse under the bullets' cores: " + brightest);
    }

    private static double maxLuminance(BufferedImage image) {
        double most = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                double l = 0.2126 * (argb >> 16 & 255) + 0.7152 * (argb >> 8 & 255) + 0.0722 * (argb & 255);
                most = Math.max(most, l);
            }
        }
        return most;
    }

    @Test
    void theMembranesFramesFollowTheirWindows() {
        assertEquals(0, HeartLooks.membraneFrame(0, false, false), "closed");
        assertEquals(1, HeartLooks.membraneFrame(0.01, false, false), "the iris starting");
        assertEquals(4, HeartLooks.membraneFrame(0.99, false, false));
        assertEquals(5, HeartLooks.membraneFrame(1, false, false), "open, singing");
        assertEquals(11, HeartLooks.membraneFrame(1, true, false), "below half its HP");
        assertEquals(12, HeartLooks.membraneFrame(1, true, true), "dead");
    }

    @Test
    void theDarkGivesAQuarterOfTheLightBackPerNodeCut() {
        var dark = CONTENT.enemies().get(H).boss().orElseThrow().dark().orElseThrow();
        assertEquals(0.08, dark.ambient(), 1e-9);
        assertEquals(1, HeartDark.target(false, dark.ambient(), dark.perNode(), 0, 0), "full outside its phase");
        float before = HeartDark.target(true, dark.ambient(), dark.perNode(), 0, 0);
        assertEquals(0.08, before, 1e-6);
        float last = before;
        for (int cut = 1; cut <= 4; cut++) {
            float now = HeartDark.target(true, dark.ambient(), dark.perNode(), cut, 0);
            assertTrue(now > last, "lighter per node cut");
            last = now;
        }
        assertEquals(1, last, 1e-6, "every node cut: the light back");
        double easy = CONTENT.enemies()
                .get(H)
                .heartChange(Difficulty.EASY)
                .orElseThrow()
                .ambient()
                .orElseThrow();
        assertEquals(0.2, HeartDark.target(true, easy, dark.perNode(), 0, 0), 1e-6, "easy's 20 %");
        assertEquals(HeartDark.ASSIST_FLOOR, HeartDark.target(true, 0.08, 0.25, 0, HeartDark.ASSIST_FLOOR), 1e-6);
    }

    @Test
    void theHeartsFlaresComeFromLivingPartsAndItsBeatsDriveThePulse() throws IOException {
        Sortie sortie = sortie();
        HeartLooks looks = HeartLooks.following(heart(), pivots());
        int parts = heart().parts().size();
        int[] flares = new int[parts];
        int beats = 0;
        int limit = SimStep.ticks(245);
        for (int step = 0; step < limit; step++) {
            sortie.step(hold(sortie));
            looks.stepped(sortie);
            SetPiece piece = HeartLooks.heart(sortie);
            if (piece == null || !piece.present()) {
                continue;
            }
            for (int i = 0; i < sortie.events().size(); i++) {
                if (sortie.events().type(i) == SimEvents.Type.HEARTBEAT) {
                    beats++;
                    // Step PT2: a lull dims the pulse (the beat's peak × (1 − LULL_DIM × its share)).
                    double lull =
                            piece.lulls().isPresent() ? piece.lulls().get().share() : 0;
                    assertEquals(
                            1 - HeartLooks.LULL_DIM * lull,
                            looks.pulseAt(sortie.tick(), 0),
                            1e-9,
                            "the pulse's peak on the beat");
                }
            }
            for (int p = 0; p < parts; p++) {
                if (looks.flareTicks(p, sortie.tick()) == 0) {
                    assertTrue(piece.partHp(p) > 0, "only a living part fires");
                    flares[p]++;
                }
            }
        }
        assertTrue(beats >= 5, "the heartbeat rings: " + beats);
        int membranes = 0;
        for (int p = 0; p < parts; p++) {
            if (heart().parts().get(p).name().startsWith("m") && flares[p] > 0) {
                membranes++;
            }
        }
        assertTrue(membranes >= 3, "the membranes' songs at their ports: " + membranes);
        assertTrue(looks.seen() && looks.beating());
    }

    /** Low in the middle, guns held. */
    private static int hold(Sortie sortie) {
        return sortie.ship().y() > 80 ? Command.DOWN.bit() : 0;
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
                50);
    }

    @Test
    void theWarpHomeUnbendsOneWayWithoutAFlash() {
        assertEquals(0, ClosingGateLooks.warpStrength(28.9), 1e-9);
        assertEquals(ClosingGateLooks.WARP_STRENGTH, ClosingGateLooks.warpStrength(29), 1e-9);
        assertEquals(0, ClosingGateLooks.warpStrength(30.5), 1e-9);
        double last = Double.POSITIVE_INFINITY;
        for (double t = 29; t < 30.5; t += 1 / 60.0) {
            double w = ClosingGateLooks.warpStrength(t);
            assertTrue(w <= last + 1e-12, "unbending one way");
            last = w;
        }
        // A grey field through the warp at those strengths: its luminance moves one way (no flash, no inversion).
        int width = 8;
        int height = 8;
        float[] field = new float[width * height * 3];
        java.util.Arrays.fill(field, 0.1f);
        float[] out = new float[field.length];
        double previous = Double.NaN;
        int direction = 0;
        for (double t = 29; t < 30.5; t += 0.05) {
            TransitWarp.shade(field, width, height, ClosingGateLooks.warpStrength(t), out);
            double mean = 0;
            for (int i = 0; i < width * height; i++) {
                mean += TransitWarp.luminance(out, i * 3);
            }
            mean /= width * height;
            if (!Double.isNaN(previous) && Math.abs(mean - previous) > 1e-6) {
                int now = mean > previous ? 1 : -1;
                assertTrue(direction == 0 || direction == now, "one way");
                direction = now;
            }
            previous = mean;
        }
        assertEquals(0, ClosingGateLooks.greying(10.5), 1e-9);
        assertEquals(1, ClosingGateLooks.greying(13.5), 1e-9);
    }

    /**
     * Step FIXGH (round 95: no Steadfast group seen on the near side): the group ahead stays wholly in view and above the
     * ship's formation from K+30.5 to the level's end at K+62 (the scroll easing from 300 to 60 px/s, a deep layer at
     * 0.12), closing on the wing (moving down) as Neptune and the ring do.
     */
    @Test
    void theSteadfastsGroupStaysInViewOnTheNearSide() {
        double scroll = 300 * 2 + 60 * (62 - 30.5);
        double half = 96 / 2.0;
        double last = Double.NEGATIVE_INFINITY;
        for (double dy = 0; dy <= scroll * ClosingGateLooks.DEEP; dy += 1) {
            double y = ClosingGateLooks.groupY(dy);
            assertTrue(y - half >= 0, "the group's top in view");
            assertTrue(y + half < 420 - 24, "the group above the ship's formation");
            assertTrue(y > last, "closing on the wing");
            last = y;
        }
    }

    @Test
    void theGateBannerIsNeutralAndShowsForTheWarningsTime() {
        GateBanner banner = new GateBanner(null, null, false);
        assertFalse(banner.showing(0));
        banner.open(100);
        assertTrue(banner.showing(100));
        assertTrue(banner.showing(100 + SimStep.ticks(WaveBanners.SECONDS) - 1));
        assertFalse(banner.showing(100 + SimStep.ticks(WaveBanners.SECONDS)));
        assertEquals("THE GATE IS CLOSING", GateBanner.TEXT);
        Color edge = GateBanner.VIOLET.edge();
        assertNotEquals(WaveBanners.AMBER_BAND.edge(), edge, "never the hostile amber");
        assertTrue(edge.b > edge.g && edge.r > edge.g, "the event plane's violet-white");
    }

    @Test
    void theChoirsLastLineShowsTheFaceOnceThenItsLoop() {
        Array<String> face = new Array<>();
        Array<String> loop = new Array<>();
        for (int i = 0; i < 48; i++) {
            face.add("face " + i);
        }
        for (int i = 0; i < 32; i++) {
            loop.add("loop " + i);
        }
        assertEquals("face 0", MissionPanel.faceFrame(face, loop, 0));
        assertEquals("face 47", MissionPanel.faceFrame(face, loop, 47 / Portraits.FPS + 0.01f));
        assertEquals(
                "loop " + MissionPanel.FACE_RESUME, MissionPanel.faceFrame(face, loop, 48 / Portraits.FPS + 0.01f));
        String line = vanguard.game.screen.LevelScreen.faceLine(CONTENT.level(KEY));
        assertTrue(line != null && line.startsWith("…it is behind us"), "the Heart's line 5: " + line);
    }
}
