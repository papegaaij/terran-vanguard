package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.content.TerrainBlock;

/**
 * M9 part E, Level 32's looks (step E4; tools/art/belt_units_e.py, backdrop_l32.py): the art's frames as the looks count
 * them (the Sentinel Tower, the Strider, the blast doors, the mining charges, the repair crate), the pivots' lens, pod,
 * knees and fall anchors, the Strider's walk, stagger and fall frames by the lost leg's side, and a gallery mouth for
 * every notch of Level 32's terrain (by its side and depth, Level 25's cave mouths unchanged).
 */
class Level32LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static long frames(Path folder, String name) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static boolean single(Path folder, String name) {
        return Files.isRegularFile(folder.resolve(name + ".png"));
    }

    private static JsonValue pivots(String name) {
        return new JsonReader().parse(new FileHandle(new File(ASSETS.toFile(), "pivots/" + name + ".json")));
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        String tower = DeepCoreLooks.TOWER;
        assertTrue(single(SPRITES, tower), "the collar");
        assertEquals(DeepCoreLooks.HEAD_HEADINGS, frames(SPRITES, tower + "-head"));
        for (String set : new String[] {"-eye", "-beam", "-beam-tip"}) {
            assertEquals(4, frames(SPRITES, tower + set), set);
        }
        // The arc's wedge per difficulty (easy 100°, medium 120°, hard 160°), the full turn's ring, the wreck.
        for (int arc : new int[] {100, 120, 160}) {
            assertTrue(single(SPRITES, tower + "-telegraph-" + arc), "the " + arc + "° wedge");
        }
        assertTrue(single(SPRITES, tower + "-ring"));
        assertTrue(single(SPRITES, tower + "-stump"));
        assertEquals(6, frames(SPRITES, tower + "-topple"));
        String strider = DeepCoreLooks.STRIDER;
        assertEquals(
                DeepCoreLooks.LEG_HEADINGS * DeepCoreLooks.WALK_FRAMES, frames(SPRITES, strider + "-legs"), "legs");
        assertEquals(DeepCoreLooks.TORSO_HEADINGS, frames(SPRITES, strider), "torso");
        assertEquals(DeepCoreLooks.TORSO_HEADINGS * 2, frames(SPRITES, strider + "-pod"));
        assertEquals(3, frames(SPRITES, strider + "-knee"));
        assertEquals(
                DeepCoreLooks.FALL_DIRS * 2 * DeepCoreLooks.FALL_STEPS, frames(SPRITES, strider + "-fall"), "fall");
        assertEquals(DeepCoreLooks.FALL_DIRS * 2, frames(SPRITES, strider + "-wreck"));
        assertEquals(DeepCoreLooks.LEG_HEADINGS, frames(SPRITES, strider + "-hulk"));
        for (String door : new String[] {DeepCoreLooks.DOOR_LEFT, DeepCoreLooks.DOOR_RIGHT}) {
            assertEquals(4, frames(SPRITES, door), door);
            assertEquals(6, frames(SPRITES, door + "-open"), door);
        }
        assertTrue(single(SPRITES, DeepCoreLooks.DOOR_SEALED));
        assertEquals(6, frames(SPRITES, DeepCoreLooks.DOOR_SEALED + "-open"));
        assertEquals(4, frames(SPRITES, DeepCoreLooks.CHARGE));
        assertEquals(10, frames(SPRITES, DeepCoreLooks.CHARGE + "-blast"));
        assertTrue(single(SPRITES, DeepCoreLooks.CRATE));
        assertEquals(4, frames(SPRITES, DeepCoreLooks.CRATE + "-open"));
        // Every ground target of Level 32 these looks draw names one of their sprites.
        LevelData level = CONTENT.level(CONTENT.levelKey(32).orElseThrow());
        for (LevelData.GroundTarget target : level.groundTargets()) {
            target.sprite()
                    .filter(sprite -> sprite.startsWith("blast-door") || sprite.equals(DeepCoreLooks.CHARGE))
                    .ifPresent(sprite -> assertTrue(DeepCoreLooks.OBJECTS.contains(sprite), sprite));
        }
    }

    @Test
    void theLensPodKneesAndFallAnchorsComeFromThePivots() {
        double[][] lens = DeepCoreLooks.lens(pivots(DeepCoreLooks.TOWER));
        // Heading 0 looks down the screen: the lens below the centre; heading 8 (90° clockwise) to the left.
        assertEquals(0, lens[0][0], 0.5);
        assertTrue(lens[0][1] < -20, "below the centre (y up)");
        assertTrue(lens[8][0] < -20, "a quarter turn clockwise from down looks left");
        JsonValue strider = pivots(DeepCoreLooks.STRIDER);
        double[][] pod = DeepCoreLooks.torsoPoints(strider, "pod");
        // Facing down the pod is on the left shoulder: screen right (the data's +19 across), toward the tail (up).
        assertTrue(pod[0][0] > 10 && pod[0][1] > 5, "pod at (" + pod[0][0] + ", " + pod[0][1] + ")");
        double[][][] knees = DeepCoreLooks.pairs(strider, "knees", "size", 128);
        // Facing down the left leg (leg-left, +42 across) is at screen right.
        assertTrue(knees[0][0][0] > 30 && knees[0][1][0] < -30, "the left knee right, the right knee left");
        double[][] anchors = DeepCoreLooks.fallAnchors(strider);
        assertEquals(DeepCoreLooks.FALL_DIRS * 2, anchors.length);
        // Falling down the screen's right (heading 0, the left leg lost) the walker's centre lies left in the frame.
        assertTrue(anchors[0][0] < anchors[1][0], "side 0 falls to screen right");
    }

    @Test
    void theStriderWalksStaggersAndFallsByTheLostLegsSide() {
        assertEquals(0, DeepCoreLooks.legFrame(0, 0, 32));
        assertEquals(4, DeepCoreLooks.legFrame(0, 16, 32), "half a stride: frame 4");
        assertEquals(3 * 8 + 1, DeepCoreLooks.legFrame(3, 36, 32), "a stride on: frame 1 again");
        double stagger = 0.6 / 1.4;
        assertEquals(0, DeepCoreLooks.fallStep(0, stagger));
        assertEquals(1, DeepCoreLooks.fallStep(stagger * 0.6, stagger));
        assertEquals(2, DeepCoreLooks.fallStep(stagger + 0.01, stagger), "the fall's first step");
        assertEquals(5, DeepCoreLooks.fallStep(1, stagger), "on the crush");
        assertEquals(0, DeepCoreLooks.side("leg-left"));
        assertEquals(1, DeepCoreLooks.side("leg-right"));
        assertEquals((3 * 2 + 1) * 6 + 4, DeepCoreLooks.fallFrame(3, 1, 4));
        assertEquals(Math.PI / 2, DeepCoreLooks.turn(0, Math.PI / 2, 4), 1e-9, "within reach: on the aim");
        assertEquals(-0.1, DeepCoreLooks.turn(0, -3, 0.1), 1e-9, "the short way round");
        assertEquals(0, DeepCoreLooks.bearing(100, 100, 100, 0), 1e-9, "straight down");
        assertEquals(3, DeepCoreLooks.eyeFrame(true, 0, 4), "live while sweeping");
        assertEquals(0, DeepCoreLooks.eyeFrame(false, 0, 4), "the first glimmer");
        assertEquals(2, DeepCoreLooks.eyeFrame(false, 0.99, 4));
    }

    @Test
    void everyNotchOfLevel32HasItsGalleryMouth() throws IOException {
        LevelData level = CONTENT.level(CONTENT.levelKey(32).orElseThrow());
        Path backdrop = ASSETS.resolve("backdrop/level-32");
        assertTrue(!level.terrain().orElseThrow().notchList().isEmpty());
        for (TerrainBlock.Notch notch : level.terrain().orElseThrow().notchList()) {
            String side = notch.side().name().toLowerCase(java.util.Locale.ROOT);
            int depth = (int) Math.round(notch.depth());
            Path file = backdrop.resolve(TrenchWalls.GALLERY + side + "-" + depth + ".png");
            assertTrue(Files.isRegularFile(file), file.toString());
            BufferedImage image = ImageIO.read(file.toFile());
            // The foot line at column depth + 16 (left) or 28 (right): the image is depth + 16 + 28 wide.
            assertEquals(
                    depth + TrenchWalls.GALLERY_RIM + TrenchWalls.GALLERY_SPILL, image.getWidth(), file.toString());
            assertEquals(Math.round(notch.width()) + 32, image.getHeight(), file.toString());
        }
        assertTrue(!single(backdrop, "cave-mouth-left") && !single(backdrop, "cave-mouth-right"));
    }
}
