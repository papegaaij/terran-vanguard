package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;

/**
 * Every level's backdrop finds its images in assets/backdrop, at the sizes its data file gives (a
 * tower's roof at its scale, and its wall texture), and on the layers that cover the screen no set
 * piece shows an edge until the debrief.
 */
class BackdropAssetsTest {
    private static final Path BACKDROP = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "backdrop");

    @Test
    void everyTileSetAndSetPieceHasItsImagesAtTheirSize() throws IOException {
        Content content = ContentLoader.fromClasspath();
        for (Map.Entry<String, LevelData> level : content.levels().entrySet()) {
            Path folder = BACKDROP.resolve(Backdrop.folder(level.getKey(), level.getValue()));
            BackdropData backdrop = level.getValue().backdrop();
            for (Map.Entry<String, BackdropData.TileSet> tileSet :
                    backdrop.tileSets().entrySet()) {
                assertSize(
                        folder.resolve(tileSet.getKey() + ".png"),
                        PlayField.WIDTH,
                        tileSet.getValue().height());
            }
            for (Map.Entry<String, BackdropData.Piece> piece : backdrop.pieces().entrySet()) {
                BackdropData.Piece spec = piece.getValue();
                // A tower's image is its roof as drawn: the footprint at the roof's scale.
                int width = spec.imageWidth();
                int height = spec.imageHeight();
                int count = spec.imageCount();
                for (int i = 0; i < count; i++) {
                    String file = count == 1 ? piece.getKey() : piece.getKey() + "_" + i;
                    assertSize(folder.resolve(file + ".png"), width, height);
                }
                if (spec.tower().isPresent()) {
                    Path wall = folder.resolve(spec.tower().get().wall() + ".png");
                    assertTrue(Files.isRegularFile(wall), wall + " exists (" + piece.getKey() + "'s wall)");
                }
            }
        }
    }

    /**
     * A set piece on an opaque layer lies over the layer's tile set, so wherever one of its edges
     * crosses the screen the piece has to be transparent along it; otherwise the tiles show
     * through as a gap beyond the edge (Level 01's Earth limb in the outro). M7 part F: unless another
     * piece on the layer covers the pixels just beyond the edge with its own (Level 19's conveyor belts:
     * the copies of a belt's plates join edge to edge, and a drum housing covers each run's ends).
     */
    @Test
    void opaqueLayersShowNoSetPieceEdgeUntilTheDebrief() throws IOException {
        Content content = ContentLoader.fromClasspath();
        for (Map.Entry<String, LevelData> entry : content.levels().entrySet()) {
            LevelData level = entry.getValue();
            BackdropData backdrop = level.backdrop();
            Path folder = BACKDROP.resolve(Backdrop.folder(entry.getKey(), entry.getValue()));
            List<Laid> laid = new ArrayList<>();
            for (BackdropData.PlacedPiece placed : backdrop.placements()) {
                BackdropData.Piece spec = backdrop.pieces().get(placed.piece());
                // A tower's roof lies over its own walls, not over the tiles; a face patch (M7 part C) is
                // drawn on a wall's face.
                if (spec.layer() == backdrop.base()
                        && !spec.isTower()
                        && !WallProjection.FACE_PATCHES.contains(placed.piece())) {
                    laid.add(new Laid(placed, spec, images(folder, placed.piece(), spec)));
                }
            }
            for (Laid piece : laid) {
                for (BufferedImage image : piece.images()) {
                    assertNoEdgeOnScreen(level, piece.placed(), piece.spec(), image, laid);
                }
            }
        }
    }

    /** M7 part F: a piece laid on the opaque layer, with its images (an animated piece's frames). */
    private record Laid(BackdropData.PlacedPiece placed, BackdropData.Piece spec, List<BufferedImage> images) {
        /** Its left edge on the layer at {@code t}, px, as Backdrop draws it. */
        long left(double t) {
            return Math.round(placed.x() + placed.offsetX(t) - spec.imageWidth() / 2.0);
        }

        /** Its bottom edge on the layer (without the scroll) at {@code t}, px, as Backdrop draws it. */
        long bottom(LevelData level, double t) {
            return Math.round(level.pieceCentre(placed) - spec.imageHeight() / 2.0) + Math.round(placed.offsetY(t));
        }

        /** Whether every one of its images is opaque at the layer's point (x, y) at {@code t}. */
        boolean covers(LevelData level, double t, long x, long y) {
            long column = x - left(t);
            long row = bottom(level, t) + spec.imageHeight() - 1 - y;
            if (column < 0 || column >= spec.imageWidth() || row < 0 || row >= spec.imageHeight()) {
                return false;
            }
            int c = (int) (placed.mirrored() ? spec.imageWidth() - 1 - column : column);
            for (BufferedImage image : images) {
                if (alpha(image, c, (int) row) == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * M7 part F: whether another laid piece covers the layer's pixels just beyond an edge of {@code image} (placed
     * at {@code left}, {@code bottom} on the layer) wherever the edge is opaque: {@code dx}, {@code dy} step from the
     * edge's row or column out of the piece (bottom 0, -1; top 0, 1; left -1, 0; right 1, 0); a pixel along the edge
     * beside it counts too (dithered sides).
     */
    private static boolean coveredBeyond(
            LevelData level,
            double t,
            BackdropData.PlacedPiece self,
            BufferedImage image,
            long left,
            long bottom,
            int dx,
            int dy,
            List<Laid> laid) {
        int width = image.getWidth();
        int height = image.getHeight();
        int count = dy != 0 ? width : height;
        for (int i = 0; i < count; i++) {
            int column = dy != 0 ? i : dx < 0 ? 0 : width - 1;
            int row = dy != 0 ? (dy < 0 ? height - 1 : 0) : i;
            int c = self.mirrored() ? width - 1 - column : column;
            if (alpha(image, c, row) == 0) {
                continue;
            }
            long x = left + column + dx;
            long y = bottom + height - 1 - row + dy;
            // The silhouettes' sides are dithered a pixel in and out from row to row (the drum housing's and the
            // plates' rails): a pixel beside the one beyond the edge, along the edge, covers it as well.
            boolean covered = false;
            for (Laid other : laid) {
                if (other.placed() != self
                        && (other.covers(level, t, x, y)
                                || other.covers(level, t, x + dy, y + dx)
                                || other.covers(level, t, x - dy, y - dx))) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                return false;
            }
        }
        return true;
    }

    /**
     * A level without a deep layer (Level 04's Luna, a top-down surface) has nothing behind its
     * ground: each section's ground tile set is opaque, or where it is not (the rille's channel) the
     * section's far tile set, opaque everywhere, shows through. The set pieces on the ground then
     * follow the edge rule above, as the deep layer's do.
     */
    @Test
    void withoutADeepLayerTheGroundTilesCoverTheScreen() throws IOException {
        Content content = ContentLoader.fromClasspath();
        for (Map.Entry<String, LevelData> entry : content.levels().entrySet()) {
            BackdropData backdrop = entry.getValue().backdrop();
            if (backdrop.hasDeep()) {
                continue;
            }
            Path folder = BACKDROP.resolve(Backdrop.folder(entry.getKey(), entry.getValue()));
            // M7 part G: a strip backdrop (Level 20) names its tile sets per zone of the strip, not per section.
            Map<String, List<String>> stretches = new java.util.LinkedHashMap<>();
            if (backdrop.stripped()) {
                List<BackdropData.Zone> zones = backdrop.strip().orElseThrow().zones();
                for (int z = 0; z < zones.size(); z++) {
                    stretches.put(
                            entry.getKey() + " strip zone " + z, zones.get(z).tiles());
                }
            } else {
                for (LevelData.Section section : entry.getValue().sections()) {
                    stretches.put(entry.getKey() + " " + section.name(), section.tiles());
                }
            }
            for (Map.Entry<String, List<String>> stretch : stretches.entrySet()) {
                String ground = tileOn(backdrop, stretch.getValue(), BackdropLayer.GROUND);
                String far = tileOn(backdrop, stretch.getValue(), BackdropLayer.FAR);
                assertTrue(ground != null, stretch.getKey() + ": no ground tile set");
                if (opaque(ImageIO.read(folder.resolve(ground + ".png").toFile()))) {
                    continue;
                }
                assertTrue(
                        far != null
                                && opaque(ImageIO.read(
                                        folder.resolve(far + ".png").toFile())),
                        stretch.getKey() + ": " + ground + " has openings but no opaque far tile set lies under them");
            }
        }
    }

    private static String tileOn(BackdropData backdrop, List<String> tiles, BackdropLayer layer) {
        for (String id : tiles) {
            if (backdrop.tileSets().get(id).layer() == layer) {
                return id;
            }
        }
        return null;
    }

    private static boolean opaque(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) >>> 24 != 0xff) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void assertNoEdgeOnScreen(
            LevelData level,
            BackdropData.PlacedPiece placed,
            BackdropData.Piece spec,
            BufferedImage image,
            List<Laid> laid) {
        int width = image.getWidth();
        int height = image.getHeight();
        long bottom = Math.round(level.pieceCentre(placed) - height / 2.0);
        double factor = level.backdrop().factor(spec.layer());
        for (int step = 0; step <= SimStep.ticks(level.outroEnd()); step++) {
            double t = step * SimStep.SECONDS;
            // As Backdrop draws it: whole pixels, y up from the play field's bottom edge.
            long y = bottom - Math.round(level.scrollAt(t) * factor) + Math.round(placed.offsetY(t));
            long x = Math.round(placed.x() + placed.offsetX(t) - width / 2.0);
            List<String> edges = new ArrayList<>();
            // The image columns on screen, reflected for a mirrored piece.
            long from = Math.max(0, x) - x;
            long to = Math.min(PlayField.WIDTH, x + width) - x;
            if (placed.mirrored()) {
                long reflected = width - to;
                to = width - from;
                from = reflected;
            }
            // M7 part F: the layer's place of the piece (without the scroll), for the pieces covering an edge.
            long layerBottom = y + Math.round(level.scrollAt(t) * factor);
            if (inside(y, PlayField.HEIGHT)
                    && opaqueRow(image, height - 1, from, to)
                    && !coveredBeyond(level, t, placed, image, x, layerBottom, 0, -1, laid)) {
                edges.add("bottom");
            }
            if (inside(y + height, PlayField.HEIGHT)
                    && opaqueRow(image, 0, from, to)
                    && !coveredBeyond(level, t, placed, image, x, layerBottom, 0, 1, laid)) {
                edges.add("top");
            }
            int low = (int) Math.max(0, y);
            int high = (int) Math.min(PlayField.HEIGHT, y + height);
            int first = placed.mirrored() ? width - 1 : 0;
            if (inside(x, PlayField.WIDTH)
                    && opaqueColumn(image, first, y + height - high, y + height - low)
                    && !coveredBeyond(level, t, placed, image, x, layerBottom, -1, 0, laid)) {
                edges.add("left");
            }
            if (inside(x + width, PlayField.WIDTH)
                    && opaqueColumn(image, width - 1 - first, y + height - high, y + height - low)
                    && !coveredBeyond(level, t, placed, image, x, layerBottom, 1, 0, laid)) {
                edges.add("right");
            }
            if (!edges.isEmpty()) {
                fail(String.format(
                        Locale.ROOT,
                        "%s (t=%s): its %s edge is not transparent but on screen at t=%.2f",
                        placed.piece(),
                        placed.t(),
                        String.join(" and ", edges),
                        t));
            }
        }
    }

    private static boolean inside(long edge, int size) {
        return edge > 0 && edge < size;
    }

    private static boolean opaqueRow(BufferedImage image, int row, long from, long to) {
        for (long column = from; column < to; column++) {
            if (alpha(image, (int) column, row) > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean opaqueColumn(BufferedImage image, int column, long from, long to) {
        for (long row = from; row < to; row++) {
            if (alpha(image, column, (int) row) > 0) {
                return true;
            }
        }
        return false;
    }

    private static int alpha(BufferedImage image, int column, int row) {
        return image.getRGB(column, row) >>> 24;
    }

    private static List<BufferedImage> images(Path folder, String id, BackdropData.Piece spec) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        int count = spec.imageCount();
        for (int i = 0; i < count; i++) {
            images.add(ImageIO.read(
                    folder.resolve((count == 1 ? id : id + "_" + i) + ".png").toFile()));
        }
        return images;
    }

    private static void assertSize(Path file, int width, int height) throws IOException {
        assertTrue(Files.isRegularFile(file), file + " exists");
        BufferedImage image = ImageIO.read(file.toFile());
        assertEquals(width + "x" + height, image.getWidth() + "x" + image.getHeight(), file.toString());
    }
}
