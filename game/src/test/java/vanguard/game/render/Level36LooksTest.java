package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;

/**
 * M10 part B, Level 36's looks (step B4; tools/art/jovian_units.py, backdrop_l36.py): the units' art as the looks count
 * it (the Chimera's headings, seams, healing pulse and flecks; the harvester's two hulls, plumes, breaks and flares; the
 * drop craft and its trail; the pathfinder pod's tumble and break); the Chimera's twin muzzles from its pivots; the
 * deep pieces' lights (the carrier group's and the storm wall's own frame clocks); the banks drawn as their pieces, each
 * image exactly its bank's rectangle with the veil at most {@link ForegroundBanks#PEAK}; the soot puff's curve and mask;
 * the Act 6 soot only from Level 36 on.
 */
class Level36LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final LevelData LEVEL = CONTENT.level(CONTENT.levelKey(36).orElseThrow());

    private static long frames(Path folder, String name) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        int headings = CloudDiverLooks.HEADINGS;
        String chimera = CloudDiverLooks.CHIMERA;
        assertEquals(headings, frames(SPRITES, chimera));
        assertEquals(headings, frames(SPRITES, chimera + "-seams"));
        assertEquals(headings * CloudDiverLooks.HEAL_PHASES, frames(SPRITES, chimera + "-heal"));
        assertEquals(8, frames(SPRITES, chimera + "-flecks"));
        String harvester = CloudDiverLooks.HARVESTER;
        for (String side : new String[] {"", "-left"}) {
            assertTrue(Files.isRegularFile(SPRITES.resolve(harvester + side + ".png")), "the hull" + side);
            assertEquals(4, frames(SPRITES, harvester + side + "-plume"), side);
            assertEquals(12, frames(SPRITES, harvester + side + "-break"), side);
            assertEquals(12, frames(SPRITES, harvester + side + "-flare"), side);
        }
        assertEquals(headings, frames(SPRITES, CloudDiverLooks.FLEET));
        assertEquals(2, frames(SPRITES, CloudDiverLooks.FLEET + "-trail"));
        for (String chunk : LEVEL.debris().orElseThrow().chunks().keySet()) {
            assertEquals(PropPasses.SPIN_FRAMES, frames(SPRITES, "debris-" + chunk), chunk);
            assertEquals(8, frames(SPRITES, "debris-" + chunk + "-break"), chunk);
        }
    }

    @Test
    void theChimerasMuzzlesComeFromItsPivots() {
        JsonValue pivots = new JsonReader().parse(new FileHandle(new File(ASSETS.toFile(), "pivots/chimera.json")));
        double[][][] muzzles = CeresLooks.muzzles(pivots);
        assertEquals(CeresLooks.HEADINGS, muzzles.length);
        assertEquals(-muzzles[0][0][0], muzzles[0][1][0], 1e-9, "heading 0: side by side, mirrored");
        assertTrue(muzzles[0][0][1] < 0, "below the centre (y up)");
    }

    /** The deep pieces' lights: declared beside their pieces on {@code deep}, unplaced, on their own frame clocks. */
    @Test
    void theDeepPiecesHaveTheirLights() throws IOException {
        BackdropData backdrop = LEVEL.backdrop();
        Path folder = ASSETS.resolve("backdrop/level-36");
        int lights = 0;
        for (BackdropData.PlacedPiece placed : backdrop.placements()) {
            BackdropData.Piece spec = backdrop.pieces().get(placed.piece());
            BackdropData.Piece light = backdrop.pieces().get(placed.piece() + BackdropPieces.GLOW);
            if (spec.layer() != BackdropLayer.DEEP || light == null) {
                continue;
            }
            lights++;
            assertEquals(BackdropLayer.DEEP, light.layer(), placed.piece());
            assertTrue(light.animated(), placed.piece() + ": its own frames");
            assertEquals(
                    light.imageCount(),
                    frames(folder, placed.piece() + BackdropPieces.GLOW),
                    placed.piece() + "'s frames");
            assertTrue(backdrop.placements().stream().noneMatch(p -> p.piece().equals(placed.piece() + "-glow")));
        }
        assertEquals(2, lights, "the carrier group's running lights and the storm wall's lightning");
    }

    /** Every bank names its piece; each image is exactly its bank's rectangle, its veil at most PEAK. */
    @Test
    void theBanksAreTheirPieces() throws IOException {
        List<BackdropData.Bank> banks = LEVEL.backdrop().banksOn(Difficulty.MEDIUM);
        assertFalse(banks.isEmpty());
        Path folder = ASSETS.resolve("backdrop/level-36");
        for (BackdropData.Bank bank : banks) {
            String piece = bank.piece().orElseThrow();
            BufferedImage image = ImageIO.read(folder.resolve(piece + ".png").toFile());
            assertEquals((int) bank.w(), image.getWidth(), piece);
            assertEquals((int) bank.h(), image.getHeight(), piece);
            int most = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    most = Math.max(most, image.getRGB(x, y) >>> 24);
                }
            }
            assertTrue(most / 255.0 <= ForegroundBanks.PEAK + 0.01, piece + ": its veil at most PEAK");
        }
        ForegroundBanks layer = new ForegroundBanks(LEVEL);
        java.util.List<String> asked = new java.util.ArrayList<>();
        layer.pieces(name -> {
            asked.add(name);
            return null;
        });
        assertEquals(banks.stream().map(bank -> bank.piece().orElseThrow()).toList(), asked, "each bank its piece");
        assertFalse(layer.drawnAsPiece(0), "without the image in the atlas: the code veil");
    }

    /** The HARVESTERS secondary's HUD line counts to its target: 0 / 6 at medium (8 sent, 2 may get away). */
    @Test
    void theHarvestersTrackerCountsToItsTarget() {
        var script =
                vanguard.content.SimSpecs.level(CONTENT, CONTENT.levelKey(36).orElseThrow(), Difficulty.MEDIUM);
        var secondary = script.objectives().secondary();
        assertEquals("HARVESTERS", secondary.label());
        assertEquals("0 / 6", MissionPanel.escapesCount(0, 8, secondary.escapesAllowed()));
        var easy = vanguard.content.SimSpecs.level(CONTENT, CONTENT.levelKey(36).orElseThrow(), Difficulty.EASY);
        assertEquals(
                "0 / 5",
                MissionPanel.escapesCount(0, 8, easy.objectives().secondary().escapesAllowed()));
    }

    /** A harvester's hull by its drift's side; the flecks' and breaks' frames run once. */
    @Test
    void thePiecesRunOnce() {
        assertEquals(-1, CloudDiverLooks.frame(-1, 12, 4), "before it");
        assertEquals(0, CloudDiverLooks.frame(0, 12, 4));
        assertEquals(11, CloudDiverLooks.frame(47, 12, 4));
        assertEquals(-1, CloudDiverLooks.frame(48, 12, 4), "after its last");
        assertEquals(-1, CloudDiverLooks.frame(0, 0, 4), "without art");
    }

    /**
     * The soot (round 73 A.3 b): in under the flash's peak, at most 0.42 opaque, out by 1.3 x the flash's life; its mask
     * full in the middle, nothing at its rim, no edge; only from Level 36 on.
     */
    @Test
    void theSootPuffsCurveAndMask() {
        assertEquals(0, SootPuffs.opacity(0), 1e-6);
        float peak = 0;
        float last = 0;
        for (int i = 0; i <= 100; i++) {
            float o = SootPuffs.opacity(i / 100.0);
            peak = Math.max(peak, o);
            last = o;
        }
        assertEquals(SootPuffs.PEAK, peak, 1e-6);
        assertEquals(0, last, 1e-6, "gone at its end");
        assertEquals(
                SootPuffs.PEAK, SootPuffs.opacity(SootPuffs.IN / SootPuffs.LIFE), 1e-5, "in under the flash's peak");
        assertTrue(SootPuffs.PEAK <= 0.42f);
        assertEquals(1, SootPuffs.maskAlpha(0.5, 0.5), 1e-6);
        for (double a = 0; a < 2 * Math.PI; a += 0.1) {
            assertEquals(0, SootPuffs.maskAlpha(0.5 + 0.5 * Math.cos(a), 0.5 + 0.5 * Math.sin(a)), 1e-6, "no edge");
        }
        // smooth: no step between neighbouring texels larger than a soft falloff's
        for (int x = 0; x < SootPuffs.SIZE - 1; x++) {
            double u = (x + 0.5) / SootPuffs.SIZE;
            double next = (x + 1.5) / SootPuffs.SIZE;
            assertTrue(Math.abs(SootPuffs.maskAlpha(u, 0.5) - SootPuffs.maskAlpha(next, 0.5)) < 0.15, "smooth at " + x);
        }
        assertFalse(SootPuffs.in(35), "Acts 1-5 keep their flashes as built");
        assertTrue(SootPuffs.in(36));
    }
}
