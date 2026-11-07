package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import vanguard.sim.SimStep;

/** {@link BackdropSeams}' open-space checks: the deep-layer levels' wraps and arenas, Level 07's pieces. */
class BackdropSeamsSpaceTest extends BackdropSeams {
    @Test
    void tileSetsOverSpaceWrapWithoutALine() throws IOException {
        List<String> seams = new ArrayList<>();
        for (Map.Entry<String, LevelData> entry : CONTENT.levels().entrySet()) {
            LevelData level = entry.getValue();
            if (!level.backdrop().hasDeep()) {
                continue;
            }
            Path folder = BACKDROP.resolve(Backdrop.folder(entry.getKey(), level));
            for (Map.Entry<String, BackdropData.TileSet> tileSet :
                    level.backdrop().tileSets().entrySet()) {
                BufferedImage image = image(folder.resolve(tileSet.getKey() + ".png"));
                double rows = translucentWrapScore(image, false);
                double columns = tileSet.getValue().drifts() ? translucentWrapScore(image, true) : 0;
                if (Math.max(rows, columns) > WRAP_LIMIT) {
                    seams.add(String.format(
                            Locale.ROOT,
                            "%s/%s: rows %.2f, columns %.2f",
                            folder.getFileName(),
                            tileSet.getKey(),
                            rows,
                            columns));
                }
            }
        }
        assertEquals(List.of(), seams, "tile sets with a line where they repeat (a drifting one also sideways)");
    }

    @Test
    void levelSevensSetPiecesShowNoEdgeOnAnyLayer() throws IOException {
        assertEquals(
                List.of(), cutPieces(LEVEL_07, "level-07/"), "pieces cut at an image border that crosses the screen");
    }

    @Test
    void overOpenSpaceOnlyTheDeepLayerShowsAnythingInTheArena() {
        List<String> seen = new ArrayList<>();
        for (Map.Entry<String, LevelData> entry : CONTENT.levels().entrySet()) {
            LevelData level = entry.getValue();
            BackdropData backdrop = level.backdrop();
            if (!backdrop.hasDeep()) {
                continue;
            }
            for (int s = 0; s < level.sections().size(); s++) {
                if (!level.sections().get(s).isArena()) {
                    continue;
                }
                if (level.sections().get(s).speed().filter(speed -> speed == 0).isPresent()) {
                    // M5 part E: an arena of speed 0 (Level 11's) does not scroll, so the jump to its
                    // end moves nothing; its anchored boss's platform lies there on purpose.
                    continue;
                }
                double start = level.sectionStart(s);
                double end = level.sections().get(s).end();
                if (risesPastArena(level, end - start)) {
                    // M11 part F: a boss rising longer than its arena lasts (Level 48's Chimera: 4 s out of the
                    // spire well, untouchable, in a 1 s arena) cannot die before the clock reaches the arena's end, so
                    // no kill jumps it; its citadel's ground lies there on purpose.
                    continue;
                }
                for (BackdropData.PlacedPiece placed : backdrop.placements()) {
                    BackdropData.Piece spec = backdrop.pieces().get(placed.piece());
                    if (spec.layer() == BackdropLayer.DEEP) {
                        continue;
                    }
                    for (double t = start; t <= end; t += SimStep.SECONDS) {
                        if (onScreen(level, placed, spec, t)) {
                            seen.add(String.format(Locale.ROOT, "%s: %s at t=%.2f", entry.getKey(), placed.piece(), t));
                            break;
                        }
                    }
                }
            }
        }
        assertEquals(List.of(), seen, "pieces that an early kill's jump to the arena's end would pop in or out");
    }

    /** M11 part F: whether the level's boss rises (untouchable) for longer than its arena of {@code seconds} lasts. */
    private static boolean risesPastArena(LevelData level, double seconds) {
        return level.boss()
                .map(boss -> CONTENT.enemies().get(boss.enemy()))
                .flatMap(enemy -> enemy.boss())
                .flatMap(boss -> boss.rise())
                .filter(rise -> rise.seconds() >= seconds)
                .isPresent();
    }
}
