package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;

/** {@link BackdropSeams}' terrain checks: the terrain levels' wraps and tile set changes, Level 08's pieces and ground. */
class BackdropSeamsTest extends BackdropSeams {
    @Test
    void terrainTileSetsWrapTopToBottom() throws IOException {
        List<String> seams = new ArrayList<>();
        for (Map.Entry<String, LevelData> entry : terrainLevels().entrySet()) {
            Path folder = BACKDROP.resolve(Backdrop.folder(entry.getKey(), entry.getValue()));
            for (Map.Entry<String, BackdropData.TileSet> tileSet :
                    entry.getValue().backdrop().tileSets().entrySet()) {
                if (!terrain(tileSet.getValue().layer())) {
                    continue;
                }
                double score = wrapScore(image(folder.resolve(tileSet.getKey() + ".png")));
                if (score > WRAP_LIMIT) {
                    seams.add(String.format(Locale.ROOT, "%s/%s: %.2f", folder.getFileName(), tileSet.getKey(), score));
                }
            }
        }
        assertEquals(
                List.of(), seams, "tile sets with a seam where they repeat (rows changing x times their neighbours)");
    }

    @Test
    void theTerrainRunsOnWhereASectionsTileSetChanges() throws IOException {
        List<String> seams = new ArrayList<>();
        for (Map.Entry<String, LevelData> entry : terrainLevels().entrySet()) {
            LevelData level = entry.getValue();
            Path folder = BACKDROP.resolve(Backdrop.folder(entry.getKey(), level));
            for (BackdropLayer layer : TERRAIN) {
                for (int s = 1; s < level.sections().size(); s++) {
                    String below = tileOn(level, s - 1, layer);
                    String above = tileOn(level, s, layer);
                    if (below == null || above == null || below.equals(above)) {
                        continue;
                    }
                    long seam = Math.round(level.seam(layer, s));
                    // Both tile sets over the rows next to the border, the set pieces on top: they
                    // match, or a feature crosses the border without a piece cut from one render
                    // over it.
                    int[][] lower = composite(level, layer, folder, below, seam - 2, seam + 2);
                    int[][] upper = composite(level, layer, folder, above, seam - 2, seam + 2);
                    double share = differing(lower, upper);
                    if (share > 0.002) {
                        seams.add(String.format(
                                Locale.ROOT,
                                "%s%s -> %s at %d: %.1f %% of the pixels",
                                folder,
                                below,
                                above,
                                seam,
                                share * 100));
                    }
                }
            }
        }
        assertEquals(List.of(), seams, "set borders where the terrain does not run on");
    }

    @Test
    void levelEightsSetPiecesShowNoEdgeOnAnyLayer() throws IOException {
        assertEquals(
                List.of(), cutPieces(LEVEL_08, "level-08/"), "pieces cut at an image border that crosses the screen");
    }

    @Test
    void levelEightsGroundRunsOnWhereItsTileSetChanges() throws IOException {
        LevelData level = CONTENT.level(LEVEL_08);
        Path folder = BACKDROP.resolve(Backdrop.folder(LEVEL_08, level));
        assumeTrue(folder.endsWith("level-08"), "Level 08 still takes another level's backdrop images");
        List<String> seams = new ArrayList<>();
        for (int s = 1; s < level.sections().size(); s++) {
            String below = tileOn(level, s - 1, BackdropLayer.GROUND);
            String above = tileOn(level, s, BackdropLayer.GROUND);
            if (below == null || above == null || below.equals(above)) {
                continue;
            }
            long seam = Math.round(level.seam(BackdropLayer.GROUND, s));
            double share = differing(
                    composite(level, BackdropLayer.GROUND, folder, below, seam - 2, seam + 2),
                    composite(level, BackdropLayer.GROUND, folder, above, seam - 2, seam + 2));
            if (share > 0.002) {
                seams.add(String.format(
                        Locale.ROOT, "%s -> %s at %d: %.1f %% of the pixels", below, above, seam, share * 100));
            }
        }
        assertEquals(List.of(), seams, "ground tile set changes that no piece covers");
    }

    @Test
    void aLineWhereTheTilesRepeatIsFound() {
        BufferedImage tile = new BufferedImage(64, 120, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < tile.getHeight(); y++) {
            for (int x = 0; x < tile.getWidth(); x++) {
                int grey = 96
                        + (int) Math.round(16 * Math.sin(2 * Math.PI * y / tile.getHeight()) + ((x * 7 + y * 13) % 5));
                tile.setRGB(x, y, 0xff000000 | grey << 16 | grey << 8 | grey);
            }
        }
        assertTrue(wrapScore(tile) < WRAP_LIMIT, "a periodic tile passes");
        for (int x = 0; x < tile.getWidth(); x++) {
            tile.setRGB(x, tile.getHeight() - 1, 0xff808898);
        }
        assertTrue(wrapScore(tile) > WRAP_LIMIT, "a line in its bottom row fails");
        assertTrue(translucentWrapScore(tile, false) > WRAP_LIMIT, "and fails the translucent score");
    }
}
