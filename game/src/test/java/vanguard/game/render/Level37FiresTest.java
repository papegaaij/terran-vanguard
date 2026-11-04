package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;

/**
 * Level 37's deck fires burn wholly on a deck (PT2, the user's Act 6 playthrough, 2026-11-03: a fire at t≈133 burned in
 * open air, where the city's pieces are transparent and the cloud deck shows; PT3: PT2's fire at t=118.25 x=120 still
 * hung half off its deck's corner, the rule having held only a few points round the centre): under every placed
 * {@code deck-fire} the city piece covering each row is opaque over the fire's whole drawn footprint, every pixel of
 * it: the flame sprite's opaque extent (alpha &ge; 128 in any frame: the flames and their base, not the faint smoke)
 * plus {@link #MARGIN} px ({@code tools/art/backdrop_l37.py} {@code fire_foot}, which also holds the deck kinds). Both
 * are ground pieces placed by their centre at the screen's middle, so a fire's ground row is the level's scroll at its
 * t plus half the screen.
 */
class Level37FiresTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(37).orElseThrow();
    private static final LevelData LEVEL_37 = CONTENT.level(KEY);
    private static final Path FOLDER = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "backdrop")
            .resolve(Backdrop.folder(KEY, LEVEL_37));
    private static final int MARGIN = 6;
    private static final int EDGE_ALPHA = 8;
    private static final int FRAMES = 8;
    private static final int HALF_SCREEN = 270;

    @Test
    void everyDeckFireLiesOnADeck() throws IOException {
        BackdropData backdrop = LEVEL_37.backdrop();
        List<BackdropData.PlacedPiece> city = backdrop.placements().stream()
                .filter(placed -> placed.piece().startsWith("city-"))
                .toList();
        List<BackdropData.PlacedPiece> fires = backdrop.placements().stream()
                .filter(placed -> placed.piece().equals("deck-fire"))
                .toList();
        assertEquals(4, fires.size(), "the four deck fires");
        int[] foot = footprint();
        for (BackdropData.PlacedPiece fire : fires) {
            double centre = LEVEL_37.scrollAt(fire.t()) + HALF_SCREEN;
            for (int dy = foot[2]; dy <= foot[3]; dy++) {
                double row = centre - dy;
                for (int dx = foot[0]; dx <= foot[1]; dx++) {
                    int x = (int) Math.round(fire.x()) + dx;
                    assertTrue(
                            opaque(city, row, x),
                            String.format(
                                    "deck-fire at (%s, %s): open air at dy %d dx %d", fire.t(), fire.x(), dy, dx));
                }
            }
        }
    }

    /**
     * The fire fades out inside its canvas (PT4, 2026-11-03: the flames ran solid to the sprite's bottom edge, so every
     * fire on the screen ended in a flat cut): every frame has alpha &le; {@link #EDGE_ALPHA} on all four canvas edges.
     */
    @Test
    void deckFireFadesOutInsideItsCanvas() throws IOException {
        for (int k = 0; k < FRAMES; k++) {
            BufferedImage frame = image("deck-fire_" + k);
            int width = frame.getWidth();
            int height = frame.getHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                        int alpha = frame.getRGB(x, y) >>> 24;
                        assertTrue(
                                alpha <= EDGE_ALPHA,
                                String.format("deck-fire_%d: alpha %d on its canvas edge at (%d, %d)", k, alpha, x, y));
                    }
                }
            }
        }
    }

    /**
     * The fire's footprint {dx0, dx1, dy0, dy1}: inclusive pixel offsets from the sprite's centre (down positive) of
     * its frames' opaque extent plus the margin.
     */
    private int[] footprint() throws IOException {
        int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE;
        int width = 0, height = 0;
        for (int k = 0; k < FRAMES; k++) {
            BufferedImage frame = image("deck-fire_" + k);
            width = frame.getWidth();
            height = frame.getHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((frame.getRGB(x, y) >>> 24) >= 128) {
                        x0 = Math.min(x0, x);
                        x1 = Math.max(x1, x);
                        y0 = Math.min(y0, y);
                        y1 = Math.max(y1, y);
                    }
                }
            }
        }
        assertTrue(x0 <= x1, "the deck fire's frames have opaque pixels");
        return new int[] {
            x0 - width / 2 - MARGIN, x1 - width / 2 + MARGIN, y0 - height / 2 - MARGIN, y1 - height / 2 + MARGIN
        };
    }

    /** Whether the city piece covering ground {@code row} is opaque at {@code x}. */
    private boolean opaque(List<BackdropData.PlacedPiece> city, double row, int x) throws IOException {
        for (BackdropData.PlacedPiece chunk : city) {
            double height =
                    LEVEL_37.backdrop().pieces().get(chunk.piece()).size().height();
            long bottom = Math.round(LEVEL_37.scrollAt(chunk.t()) + HALF_SCREEN - height / 2);
            long r = Math.round(row);
            if (r >= bottom && r < bottom + height) {
                BufferedImage image = image(chunk.piece());
                int left = (int) Math.round(chunk.x() - image.getWidth() / 2.0);
                int y = (int) (bottom + image.getHeight() - 1 - r);
                return (image.getRGB(x - left, y) >>> 24) > 230;
            }
        }
        return false;
    }

    private final java.util.Map<String, BufferedImage> images = new java.util.HashMap<>();

    private BufferedImage image(String piece) throws IOException {
        BufferedImage image = images.get(piece);
        if (image == null) {
            image = ImageIO.read(FOLDER.resolve(piece + ".png").toFile());
            images.put(piece, image);
        }
        return image;
    }
}
