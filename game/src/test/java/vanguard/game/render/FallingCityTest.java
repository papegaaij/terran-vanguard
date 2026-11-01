package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.FallingCityData;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M10 part C, step C4a (design/campaign Level 37, The Vrell turn and The falling city): the fall's schedule hands the
 * ground over to the two halves exactly where the city's pieces lay, on the level's own clock; the halves follow their
 * curves (shrinking and darkening, never back), the sinking city crossfades in as they fade out, the one-shots hold
 * their last frame, the growth's reveal runs over its seconds, the lift rings' lights are additive, and nothing of it
 * allocates per frame.
 */
class FallingCityTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final LevelData LEVEL_37 = CONTENT.level(CONTENT.levelKey(37).orElseThrow());

    private static FallingCity.Schedule schedule() {
        return new FallingCity.Schedule(LEVEL_37);
    }

    /**
     * At the fall's t each half lies on its own ground rows, unscaled and unturned, on whole pixels: where the city's
     * pieces were drawn (the hand-over is pixel-exact). Its first key is that place on the level's clock, so the
     * generator's rows agree with the data's sections (a retimed section would move the city under the halves).
     */
    @Test
    void theHalvesTakeOverExactlyWhereTheCityLay() {
        FallingCity.Schedule schedule = schedule();
        FallingCityData.Fall fall = LEVEL_37.backdrop().fall().orElseThrow();
        double ground = LEVEL_37.scrollAt(fall.t());
        double[] out = new double[FallingCity.Schedule.SLOTS];
        for (int h = 0; h < schedule.halves(); h++) {
            schedule.handOver(h, out);
            assertEquals(schedule.x0(h), out[FallingCity.Schedule.LEFT], "half " + h + ": its left column");
            assertEquals(
                    schedule.bottom(h) - Math.round(ground),
                    out[FallingCity.Schedule.BOTTOM],
                    "half " + h + ": its bottom on the screen, the ground's");
            assertEquals(1, out[FallingCity.Schedule.SCALE]);
            assertEquals(0, out[FallingCity.Schedule.ROTATION], 0);
            assertEquals(0, out[FallingCity.Schedule.SHADE]);
            assertEquals(0, out[FallingCity.Schedule.FADE_IN], "the rims fade in as the gap opens");
            List<Double> first = fall.halves().get(h).keys().getFirst();
            assertEquals(schedule.x0(h) + schedule.width(h) / 2.0, first.get(1), 0.5, "half " + h + ": first key x");
            double centreFromTop = PlayField.HEIGHT - (schedule.bottom(h) + schedule.height(h) / 2.0 - ground);
            assertEquals(centreFromTop, first.get(2), 0.5, "half " + h + ": first key y on the level's clock");
        }
    }

    /**
     * Frame to frame across the swap the halves move with the ground: at the swap they are on its rows, and over the
     * next 0.2 s they move as it does within a pixel a frame (then their keys slow them further; never a jump).
     */
    @Test
    void acrossTheSwapTheHalvesMoveWithTheGround() {
        FallingCity.Schedule schedule = schedule();
        double t = LEVEL_37.backdrop().fall().orElseThrow().t();
        double[] out = new double[FallingCity.Schedule.SLOTS];
        double[] last = new double[schedule.halves()];
        for (int frame = 0; frame <= 12; frame++) {
            double u = frame / 60.0;
            double ground = LEVEL_37.scrollAt(t + u);
            double groundBefore = LEVEL_37.scrollAt(t + u - 1 / 60.0);
            for (int h = 0; h < schedule.halves(); h++) {
                schedule.half(h, t + u, ground, out);
                double bottom = out[FallingCity.Schedule.BOTTOM];
                if (frame == 0) {
                    assertEquals(schedule.bottom(h) - Math.round(ground), bottom, "half " + h + " at the swap");
                    assertEquals(schedule.x0(h), out[FallingCity.Schedule.LEFT], "half " + h + " at the swap");
                } else {
                    double groundStep = Math.round(groundBefore) - Math.round(ground);
                    assertEquals(groundStep, bottom - last[h], 1.0, "half " + h + " at u=" + u);
                }
                last[h] = bottom;
            }
        }
    }

    @Test
    void theHalvesShrinkAndDarkenOverTheirSecondsAndNeverBack() {
        FallingCityData.Fall fall = LEVEL_37.backdrop().fall().orElseThrow();
        assertEquals(2, fall.halves().size(), "two layers (A.13 b)");
        for (FallingCityData.Half half : fall.halves()) {
            List<List<Double>> keys = half.keys();
            for (int i = 1; i < keys.size(); i++) {
                assertTrue(keys.get(i).get(3) <= keys.get(i - 1).get(3), half.piece() + ": scale at key " + i);
                assertTrue(keys.get(i).get(5) >= keys.get(i - 1).get(5), half.piece() + ": shade at key " + i);
            }
            assertTrue(keys.getLast().get(3) < 0.6, half.piece() + ": shrinks toward the centre");
            assertTrue(keys.getLast().get(5) > 0.4, half.piece() + ": darkens");
        }
        assertTrue(
                fall.halves().get(0).keys().getLast().get(3)
                        != fall.halves().get(1).keys().getLast().get(3),
                "each at its own rate");
    }

    /** The sinking city comes in as the halves go: their opacities sum to one, and the halves are gone after it. */
    @Test
    void theSinkingCityCrossfadesWithTheHalves() {
        FallingCity.Schedule schedule = schedule();
        FallingCityData.Fall fall = LEVEL_37.backdrop().fall().orElseThrow();
        FallingCityData.Sinking sinking = fall.sinking().orElseThrow();
        double[] out = new double[FallingCity.Schedule.SLOTS];
        assertEquals(0, schedule.sinkingAlpha(fall.t() + sinking.from() - 0.01));
        for (double u = sinking.from(); u <= sinking.from() + sinking.fade() + 1; u += 0.1) {
            schedule.half(0, fall.t() + u, LEVEL_37.scrollAt(fall.t() + u), out);
            assertEquals(1, out[FallingCity.Schedule.ALPHA] + schedule.sinkingAlpha(fall.t() + u), 1e-9, "u=" + u);
        }
        schedule.half(1, fall.t() + sinking.from() + sinking.fade(), 0, out);
        assertEquals(0, out[FallingCity.Schedule.ALPHA], "the halves are gone");
        assertEquals(
                179.0, fall.t() + sinking.from(), 1e-9, "the README's open cloud from t=180: the crossfade 179-181");
    }

    @Test
    void theShardsTumbleBetweenTheCutEdgesOnlyInTheirSpan() {
        FallingCity.Schedule schedule = schedule();
        FallingCityData.Fall fall = LEVEL_37.backdrop().fall().orElseThrow();
        FallingCityData.Shards shards = fall.shards().orElseThrow();
        double[] out = new double[FallingCity.Schedule.SLOTS];
        double[] scratch = new double[FallingCity.Schedule.SLOTS];
        assertFalse(schedule.shards(fall.t() + shards.from() - 0.05, 0, out, scratch));
        assertFalse(schedule.shards(fall.t() + shards.to() + 0.05, 0, out, scratch));
        double t = fall.t() + (shards.from() + shards.to()) / 2;
        double ground = LEVEL_37.scrollAt(t);
        assertTrue(schedule.shards(t, ground, out, scratch));
        double x = out[FallingCity.Schedule.LEFT];
        schedule.half(0, t, ground, scratch);
        double westRight = scratch[FallingCity.Schedule.LEFT]
                + schedule.width(0) / 2.0 * (1 + scratch[FallingCity.Schedule.SCALE]);
        schedule.half(1, t, ground, scratch);
        double eastLeft = scratch[FallingCity.Schedule.LEFT]
                + schedule.width(1) / 2.0 * (1 - scratch[FallingCity.Schedule.SCALE]);
        assertTrue(x > Math.min(westRight, eastLeft) && x < Math.max(westRight, eastLeft), "between the edges: " + x);
    }

    @Test
    void theOneShotsHoldTheirLastFrame() {
        assertEquals(0, FallingCity.Schedule.oneShot(-1, 5, 10), "upright before");
        assertEquals(0, FallingCity.Schedule.oneShot(0.1, 5, 10));
        assertEquals(4, FallingCity.Schedule.oneShot(0.9, 5, 10));
        assertEquals(9, FallingCity.Schedule.oneShot(30, 5, 10), "the last frame held");
        assertEquals(3, FallingCity.frameAt(1.5, 2, 4));
        assertEquals(1, FallingCity.frameAt(2.5, 2, 4), "a loop");
    }

    /** The turn's burst: nothing before its t, the map's zeros at t, every value by its seconds. */
    @Test
    void theGrowthRevealsOverItsSeconds() {
        FallingCityData.Growth growth = LEVEL_37.backdrop().growth().orElseThrow();
        assertEquals(100.0, growth.t(), "the turn");
        assertTrue(GrowthOverlay.threshold(growth, growth.t() - 0.01) < 0);
        assertTrue(GrowthOverlay.threshold(growth, growth.t()) * 255 < 1);
        assertTrue(GrowthOverlay.threshold(growth, growth.t() + growth.seconds() / 2) * 255 > 127);
        assertTrue(GrowthOverlay.threshold(growth, growth.t() + growth.seconds()) >= 1);
        // It lies over the screen at the turn: its rows cover the ground on the screen at t.
        double ground = LEVEL_37.scrollAt(growth.t());
        BackdropData.Piece piece = LEVEL_37.backdrop().pieces().get(growth.piece());
        assertTrue(growth.bottom() <= Math.round(ground), "from the bottom edge");
        assertTrue(growth.bottom() + piece.size().height() >= Math.round(ground) + PlayField.HEIGHT, "to the top edge");
    }

    /** A placed {@code <id>-glow} on its piece's place is its light (Level 37's lift rings); alone it is not (Level 01). */
    @Test
    void aGlowPlacedOnItsPieceIsItsLight() {
        List<BackdropData.PlacedPiece> lights = new ArrayList<>();
        for (BackdropData.PlacedPiece placed : LEVEL_37.backdrop().placements()) {
            if (BackdropPieces.light(placed, LEVEL_37.backdrop().placements())) {
                lights.add(placed);
            }
        }
        assertEquals(
                List.of("lift-ring-glow", "lift-ring-glow"),
                lights.stream().map(p -> p.piece()).toList());
        assertTrue(lights.stream()
                .allMatch(p -> LEVEL_37.backdrop().pieces().get(p.piece()).layer() == BackdropLayer.LOW_AIR));
        LevelData level01 = CONTENT.level(CONTENT.levelKey(1).orElseThrow());
        assertTrue(level01.backdrop().placements().stream()
                .noneMatch(placed ->
                        BackdropPieces.light(placed, level01.backdrop().placements())));
    }

    @Test
    void theScheduleAllocatesNothing() {
        var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        FallingCity.Schedule schedule = schedule();
        FallingCityData.Growth growth = LEVEL_37.backdrop().growth().orElseThrow();
        double[] out = new double[FallingCity.Schedule.SLOTS];
        double[] scratch = new double[FallingCity.Schedule.SLOTS];
        int frames = 45 * 60;
        double[] grounds = new double[frames];
        for (int f = 0; f < frames; f++) {
            grounds[f] = LEVEL_37.scrollAt(155 + f / 60.0);
        }
        long least = Long.MAX_VALUE;
        double sum = 0;
        for (int run = 0; run < 10 && least > 0; run++) {
            long before = threads.getCurrentThreadAllocatedBytes();
            for (int f = 0; f < frames; f++) {
                double t = 155 + f / 60.0;
                double ground = grounds[f];
                for (int h = 0; h < schedule.halves(); h++) {
                    schedule.half(h, t, ground, out);
                    sum += out[FallingCity.Schedule.BOTTOM];
                }
                if (schedule.shards(t, ground, out, scratch)) {
                    sum += out[FallingCity.Schedule.LEFT];
                }
                sum += schedule.sinkingAlpha(t) + GrowthOverlay.threshold(growth, t - 60);
                sum += FallingCity.Schedule.oneShot(t - 160, 5, 10);
            }
            least = Math.min(least, threads.getCurrentThreadAllocatedBytes() - before);
        }
        assertEquals(0, least, "the fall's schedule allocated " + least + " bytes (" + sum + ")");
    }
}
