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
 * Level 37's deck fires burn on a deck (PT2, the user's Act 6 playthrough, 2026-11-03: a fire at t≈133 burned in open
 * air, where the city's pieces are transparent and the cloud deck shows): under every placed {@code deck-fire} the city
 * piece covering its rows is opaque over the fire's footprint ({@code tools/art/backdrop_l37.py}, {@code FIRE_FOOT_DY}
 * and {@code _DX}, from the fire's centre, down positive). Both are ground pieces placed by their centre at the screen's
 * middle, so a fire's ground row is the level's scroll at its t plus half the screen.
 */
class Level37FiresTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(37).orElseThrow();
    private static final LevelData LEVEL_37 = CONTENT.level(KEY);
    private static final Path FOLDER = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "backdrop")
            .resolve(Backdrop.folder(KEY, LEVEL_37));
    private static final int[] FOOT_DY = {-16, 0, 16, 24};
    private static final int[] FOOT_DX = {-12, 0, 12};
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
        for (BackdropData.PlacedPiece fire : fires) {
            double centre = LEVEL_37.scrollAt(fire.t()) + HALF_SCREEN;
            for (int dy : FOOT_DY) {
                double row = centre - dy;
                for (int dx : FOOT_DX) {
                    int x = (int) Math.round(fire.x()) + dx;
                    assertTrue(
                            opaque(city, row, x),
                            String.format(
                                    "deck-fire at (%s, %s): open air at dy %d dx %d", fire.t(), fire.x(), dy, dx));
                }
            }
        }
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
