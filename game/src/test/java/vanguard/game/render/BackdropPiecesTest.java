package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;

/**
 * Round 80 (Level 42's Callisto moving up and down a pixel all the time): a piece on a path that rides its layer's
 * scroll is placed by one rounding of the path's offset and the scroll together, so it only ever steps the way it
 * drifts; a piece without a path moves with its layer's tiles.
 */
class BackdropPiecesTest {
    /** Level 42's deep layer: 0.06 of the 160 px/s scroll. */
    private static final double DEEP_SPEED = 0.06 * 160;

    private static BackdropPieces.Piece piece(Optional<List<BackdropData.Waypoint>> path, long bottom) {
        BackdropData.PlacedPiece placed = new BackdropData.PlacedPiece(
                "callisto-curve",
                -21.875,
                240,
                Optional.empty(),
                path,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        double start = path.map(points -> points.getFirst().t()).orElse(Double.NaN);
        return new BackdropPieces.Piece(placed, null, null, bottom, start, false);
    }

    @Test
    void aPathRidingTheScrollOnlyStepsTheWayItDrifts() {
        // Level 42's Callisto over its first section: 201.6 px up in 22 s against the deep layer's 9.6 px/s, a net
        // 0.44 px/s down the screen.
        BackdropPieces.Piece callisto = piece(
                Optional.of(List.of(new BackdropData.Waypoint(0, 0, 0), new BackdropData.Waypoint(22, 0, 201.6))), 40);
        float last = BackdropPieces.pieceY(callisto, 0, 0, 0);
        int steps = 0;
        for (int frame = 1; frame <= 22 * 60; frame++) {
            double t = frame / 60.0;
            float y = BackdropPieces.pieceY(callisto, 0, DEEP_SPEED * t, t);
            assertTrue(y <= last, "it never steps back up, frame " + frame);
            if (y != last) {
                steps++;
            }
            last = y;
        }
        assertTrue(steps <= 11, "one step per 1 px of its 9.7 px drift, not a flicker: " + steps);
    }

    @Test
    void aPieceWithoutAPathMovesWithItsLayersTiles() {
        BackdropPieces.Piece still = piece(Optional.empty(), 300);
        for (double scroll = 0; scroll < 50; scroll += 0.37) {
            assertEquals(300 + 5 - Math.round(scroll), BackdropPieces.pieceY(still, 5, scroll, 0), 0);
        }
    }
}
