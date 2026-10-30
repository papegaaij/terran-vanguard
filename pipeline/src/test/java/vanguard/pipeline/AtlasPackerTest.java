package vanguard.pipeline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AtlasPackerTest {
    @TempDir
    Path dir;

    @Test
    void aliasesOfOneRectangleAreSortedByNameAndIndex() {
        String atlas = """

                sprites.png
                size: 64, 64
                format: RGBA8888
                filter: Nearest, Nearest
                repeat: none
                ship
                  rotate: false
                  xy: 1, 1
                  size: 8, 8
                  orig: 8, 8
                  offset: 0, 0
                  index: 2
                ship
                  rotate: false
                  xy: 1, 1
                  size: 8, 8
                  orig: 8, 8
                  offset: 0, 0
                  index: 0
                orb
                  rotate: false
                  xy: 11, 1
                  size: 4, 4
                  orig: 4, 4
                  offset: 0, 0
                  index: -1
                ship
                  rotate: false
                  xy: 1, 11
                  size: 8, 8
                  orig: 8, 8
                  offset: 0, 0
                  index: 1
                """;
        String sorted = AtlasPacker.sortAliases(atlas);
        List<String> names = sorted.lines()
                .filter(line -> line.startsWith("  index:") || line.startsWith("  xy:"))
                .toList();
        // Index 0 and 2 share one rectangle and swap; the orb and the other ship frame stay in place.
        assertEquals(
                List.of(
                        "  xy: 1, 1",
                        "  index: 0",
                        "  xy: 1, 1",
                        "  index: 2",
                        "  xy: 11, 1",
                        "  index: -1",
                        "  xy: 1, 11",
                        "  index: 1"),
                names);
        assertEquals(atlas.length(), sorted.length());
        assertEquals(sorted, AtlasPacker.sortAliases(sorted));
    }

    @Test
    void packsTheAtlasesAndOneBackdropAtlasPerLevel() throws IOException, InterruptedException {
        Path sprites = dir.resolve("sprites");
        Path backdrop = dir.resolve("backdrop");
        image(sprites.resolve("ship_0.png"), 0xff00ff00);
        image(sprites.resolve("ship_1.png"), 0xff00ff00);
        image(sprites.resolve("skitter.png"), 0xffff0000);
        image(sprites.resolve("hud/well.png"), 0xff0000ff);
        image(backdrop.resolve("level-02/ice.png"), 0xff808080);
        image(backdrop.resolve("level-01/earth.png"), 0xff404040);
        Path claims = dir.resolve("claims.txt");
        Files.write(claims, List.of("ship sprites", "skitter level-01,level-02"));
        Path output = dir.resolve("build/atlases");

        AtlasPacker.main(new String[] {sprites.toString(), backdrop.toString(), claims.toString(), output.toString()});

        assertEquals(
                List.of(
                        "backdrop-level-01.atlas",
                        "backdrop-level-01.png",
                        "backdrop-level-02.atlas",
                        "backdrop-level-02.png",
                        "level-01.atlas",
                        "level-01.png",
                        "level-02.atlas",
                        "level-02.png",
                        "sprites.atlas",
                        "sprites.png"),
                list(output));
        assertTrue(Files.notExists(dir.resolve("build/atlases-work")));
        String first = Files.readString(output.resolve("backdrop-level-01.atlas"));
        assertTrue(first.indexOf("backdrop-level-01.png") < first.indexOf("level-01/earth"), first);
        assertFalse(first.contains("level-02/ice"), first);
        String second = Files.readString(output.resolve("backdrop-level-02.atlas"));
        assertTrue(second.indexOf("backdrop-level-02.png") < second.indexOf("level-02/ice"), second);
        assertFalse(second.contains("level-01/earth"), second);
        String shared = Files.readString(output.resolve("sprites.atlas"));
        assertTrue(shared.contains("hud/well"), shared);
        assertTrue(shared.indexOf("index: 0") < shared.indexOf("index: 1"), shared);

        byte[] packed = Files.readAllBytes(output.resolve("backdrop-level-02.atlas"));
        AtlasPacker.main(new String[] {sprites.toString(), backdrop.toString(), claims.toString(), output.toString()});
        assertArrayEquals(packed, Files.readAllBytes(output.resolve("backdrop-level-02.atlas")));
    }

    /**
     * M7 part F: a large sprite drawn only centred (the Revenant Walker's legs, topple and wreck) goes into its atlas
     * cropped by the same margin on both sides down to its visible pixels, so its centre stays its centre.
     */
    @Test
    void aCentredCropKeepsTheFramesCentre() {
        BufferedImage frame = new BufferedImage(12, 10, BufferedImage.TYPE_INT_ARGB);
        frame.setRGB(4, 3, 0xff112233);
        frame.setRGB(6, 5, 0xff445566);
        BufferedImage cropped = AtlasPacker.centredCrop(frame);
        // x: 4 empty columns on the left, 5 on the right: 4 off each side; y: 3 above, 4 below: 3 off each.
        assertEquals(12 - 2 * 4, cropped.getWidth());
        assertEquals(10 - 2 * 3, cropped.getHeight());
        assertEquals(0xff112233, cropped.getRGB(0, 0));
        assertEquals(0xff445566, cropped.getRGB(2, 2));
        BufferedImage empty = AtlasPacker.centredCrop(new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB));
        assertEquals(2, empty.getWidth(), "an empty frame keeps its centre");
        assertEquals(2, empty.getHeight());
        assertTrue(AtlasPacker.CENTRED_CROP.containsAll(
                List.of("revenant-walker-legs", "revenant-walker-topple", "revenant-walker-wreck")));
    }

    private static void image(Path file, int argb) throws IOException {
        Files.createDirectories(file.getParent());
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                image.setRGB(x, y, argb);
            }
        }
        ImageIO.write(image, "png", file.toFile());
    }

    private static List<String> list(Path dir) throws IOException {
        try (var files = Files.list(dir)) {
            return files.map(file -> file.getFileName().toString()).sorted().toList();
        }
    }
}
