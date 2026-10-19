package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.game.render.TowerProjection.Tower;

/**
 * The perspective towers' projection (design/art-direction, Perspective towers are scenery): a
 * roof h camera units high is drawn at k = 6 ÷ (6 − h) round the play field's (240, 243), 297 px
 * below its top edge.
 */
class TowerProjectionTest {
    private static final double K_135 = 6 / 4.65;

    @Test
    void theProjectionCentreLies297PxBelowTheTopEdge() {
        assertEquals(240, BackdropData.Tower.CENTRE_X);
        assertEquals(540 - 297, BackdropData.Tower.CENTRE_Y);
    }

    /**
     * The walls drawn are the ones facing the projection centre (the camera sees those): a tower left
     * of the centre shows its east wall, right of it its west wall, above it its south wall, below it
     * its north wall; one over the centre shows none. (The east and west walls were swapped before
     * 2026-10-07: a tower left of the centre drew its hidden west wall under its roof and left a gap
     * where its east wall belongs.)
     */
    @Test
    void theWallsFacingTheCentreAreTheOnesDrawn() {
        assertEquals(TowerProjection.EAST, sides(60, 243), "left of the centre");
        assertEquals(TowerProjection.WEST, sides(420, 243), "right of the centre");
        assertEquals(TowerProjection.SOUTH, sides(240, 480), "above the centre");
        assertEquals(TowerProjection.NORTH, sides(240, 40), "below the centre");
        assertEquals(TowerProjection.EAST | TowerProjection.SOUTH, sides(60, 480), "up and left: two walls");
        assertEquals(0, sides(240, 243), "over the centre: only the roof");
    }

    /** The visible sides of a 60 x 50 tower 1.35 units high whose footprint is centred at (x, y). */
    private static int sides(double x, double y) {
        double k = K_135;
        float fx0 = (float) (x - 30);
        float fy0 = (float) (y - 25);
        float rx0 = TowerProjection.roofLeft(x, (int) Math.round(60 * k), k);
        float ry0 = TowerProjection.roofBottom(y, (int) Math.round(50 * k), k);
        return TowerProjection.visibleSides(
                fx0, fy0, fx0 + 60, fy0 + 50, rx0, ry0, rx0 + Math.round(60 * k), ry0 + Math.round(50 * k));
    }

    @Test
    void aRoofOverTheCentreDoesNotLean() {
        // A 60 x 60 footprint at k = 1.29 has a 77 x 77 roof, centred over it.
        assertEquals(240 - 38, TowerProjection.roofLeft(240, 77, K_135));
        assertEquals(243 - 38, TowerProjection.roofBottom(243, 77, K_135));
    }

    @Test
    void aRoofLeansAwayFromTheCentre() {
        // At the field's side edges the parallax B concept's tallest roofs (h = 1.35) lean 70 px outward:
        // 240 x 0.29 = 69.7, less half the roof's 77 px.
        assertEquals(-108, TowerProjection.roofLeft(0, 77, K_135));
        assertEquals(511, TowerProjection.roofLeft(480, 77, K_135));
        // The bottom edge is 243 px below the centre (70.5 px of lean), the top edge 297 px above it (86.2 px).
        assertEquals(-109, TowerProjection.roofBottom(0, 77, K_135));
        assertEquals(588, TowerProjection.roofBottom(540, 77, K_135));
    }

    @Test
    void aLowerRoofLeansLess() {
        double k = BackdropData.Tower.scaleAt(0.5); // 1.09
        assertEquals(12.0 / 11, k, 1e-12);
        // A 20 px footprint has a 22 px roof: 21.8 px of lean at the left edge, 9.1 px at x = 340.
        assertEquals(-33, TowerProjection.roofLeft(0, 22, k));
        assertEquals(338, TowerProjection.roofLeft(340, 22, k));
    }

    @Test
    void aRoofMovesAtKTimesTheScroll() {
        // While the ground scrolls 100 px, the roof moves 129 px: its own speed is (k - 1) x the scroll.
        int before = TowerProjection.roofBottom(400, 77, K_135);
        int after = TowerProjection.roofBottom(300, 77, K_135);
        assertEquals(Math.round(100 * K_135), before - after);
        assertEquals(29, (before - after) - 100);
        // At a scroll of 140 px/s the tallest roof (k = 1.33): 47 px/s, inside the 120 px/s budget.
        assertEquals(140 / 3.0, new BackdropData.Tower(1.5, "wall", Optional.empty()).ownSpeed(140), 1e-9);
    }

    @Test
    void windowRowsFollowTheTruePerspectiveUpAWall() {
        assertEquals(0, TowerProjection.rise(0, 1.35), 1e-12);
        assertEquals(1, TowerProjection.rise(1, 1.35), 1e-12);
        // Half way up is drawn a little below half way to the roof's edge: rows grow towards the top.
        double half = TowerProjection.rise(0.5, 1.35);
        assertTrue(half < 0.5 && half > 0.4, "half way up: " + half);
        // s(0.675) = 6 / 5.325
        assertEquals((6 / 5.325 - 1) / (K_135 - 1), half, 1e-12);
    }

    @Test
    void aWallIsCutIntoBandsOfAtMostACell() {
        // Even a wall that hardly leans gets enough bands that each widens by at most a tenth.
        assertEquals(4, TowerProjection.bands(0, 4 / 3.0));
        assertEquals(3, TowerProjection.bands(10, K_135));
        assertEquals(2, TowerProjection.bands(10, BackdropData.Tower.scaleAt(0.9)));
        assertEquals(7, TowerProjection.bands(100, K_135));
        assertEquals(TowerProjection.MAX_BANDS, TowerProjection.bands(400, K_135));
    }

    /**
     * The user's pair in Level 08 (playtest 2026-10-18, t = 171.9 on the right, near the end): a
     * 56 x 56 tower 1.10 high at x 353 and a 68 x 64 one 1.35 high at x 435 in one row. The inner
     * roof leans ~12 px over the outer footprint, so the outer (taller) tower is drawn first and the
     * inner roof covers its west wall; drawn lowest first, the inner roof passed under the outer tower.
     */
    @Test
    void ofTwoTowersInARowTheOuterIsDrawnFirstWhateverItsHeight() {
        assertEquals(List.of(435.0, 353.0), drawnX(0, tower(353, CY, 56, 56, 1.10), tower(435, CY, 68, 64, 1.35)));
        assertEquals(List.of(45.0, 127.0), drawnX(0, tower(127, CY, 56, 56, 1.10), tower(45, CY, 68, 64, 1.35)));
    }

    /** Level 09's case (t = 170.04 and 180.28): a 68 x 64 tower 1.10 high inside an 84 x 72 one 1.35 high. */
    @Test
    void level09sCreepTowersAreDrawnOutermostFirst() {
        assertEquals(List.of(429.0, 328.0), drawnX(0, tower(328, CY, 68, 64, 1.10), tower(429, CY, 84, 72, 1.35)));
        assertEquals(List.of(51.0, 152.0), drawnX(0, tower(152, CY, 68, 64, 1.10), tower(51, CY, 84, 72, 1.35)));
    }

    /**
     * Two towers in one column, rows 96 px apart: above the centre the upper one is outer and drawn
     * first; once the scroll has carried both below the centre the lower one is, and the order flips.
     */
    @Test
    void inAColumnTheOrderFlipsAsTheTowersPassTheCentre() {
        Tower lower = tower(131, 0, 56, 56, 1.40);
        Tower upper = tower(131, 96, 56, 56, 1.35);
        // The lower tower 140 px above the centre, the upper one 236 px.
        assertEquals(List.of(96.0, 0.0), drawnCentres(-(CY + 140), lower, upper));
        // The upper tower 140 px below the centre, the lower one 236 px (both partly on screen).
        assertEquals(List.of(0.0, 96.0), drawnCentres(96 - (CY - 140), lower, upper));
    }

    /** Towers either side of the centre's line never cover each other: drawn lowest first. */
    @Test
    void towersEitherSideOfTheCentreKeepTheLowestFirstOrder() {
        assertEquals(
                List.of(280.0, 200.0),
                drawnX(0, tower(200, CY + 150, 40, 40, 1.35), tower(280, CY + 150, 40, 40, 1.1)));
        assertEquals(0, TowerProjection.outerFirst(180, 220, 260, 300, 240), "the gap holds the centre");
        assertEquals(-1, TowerProjection.outerFirst(240, 280, 300, 340, 240), "both right of it: the right one first");
        assertEquals(1, TowerProjection.outerFirst(100, 140, 160, 240, 240), "both left of it: the left one first");
    }

    /**
     * A Level 08-like block (four columns, rows 96 px apart, heights mixed) at many scrolls: every
     * tower on screen is drawn once, and of two whose drawn boxes overlap the outer one comes first.
     */
    @Test
    void aBlockOfTowersIsAlwaysDrawnOutermostFirst() {
        double[] heights = {0.35, 1.35, 0.85, 1.1, 0.6, 1.35, 1.1, 0.85};
        double[] columns = {45, 127, 353, 435};
        List<Tower> towers = new ArrayList<>();
        int h = 0;
        for (int row = 0; row < 12; row++) {
            for (double x : columns) {
                int size = (row + (int) x) % 2 == 0 ? 56 : 68;
                towers.add(tower(x, row * 96, size, size, heights[h++ % heights.length]));
            }
        }
        TowerProjection projection = new TowerProjection(towers, null);
        for (int scroll = -540; scroll <= 1100; scroll += 7) {
            int n = projection.order(scroll);
            boolean[] seen = new boolean[towers.size()];
            for (int i = 0; i < n; i++) {
                int t = projection.drawn(i);
                assertTrue(!seen[t], "drawn twice at scroll " + scroll);
                seen[t] = true;
                for (int j = i + 1; j < n; j++) {
                    Tower a = projection.tower(t);
                    Tower b = projection.tower(projection.drawn(j));
                    if (overlap(a, b, scroll)) {
                        assertTrue(
                                outer(a, b, scroll) >= 0,
                                "scroll " + scroll + ": " + a.x() + "," + a.centre() + " drawn before the outer "
                                        + b.x() + "," + b.centre());
                    }
                }
            }
        }
    }

    private static final double CY = BackdropData.Tower.CENTRE_Y;

    /** A tower centred at (x, centre) on the ground, its roof footprint x k on whole px. */
    private static Tower tower(double x, double centre, int width, int height, double h) {
        double k = BackdropData.Tower.scaleAt(h);
        return new Tower(
                x,
                centre,
                Math.round(centre - height / 2.0),
                width,
                height,
                k,
                h,
                1,
                region((int) Math.round(width * k), (int) Math.round(height * k)),
                null);
    }

    /** A region of that size without a texture (the order needs only the roof's size). */
    private static TextureRegion region(int width, int height) {
        return new TextureRegion() {
            @Override
            public int getRegionWidth() {
                return width;
            }

            @Override
            public int getRegionHeight() {
                return height;
            }
        };
    }

    private static List<Double> drawnX(double scroll, Tower... towers) {
        TowerProjection projection = new TowerProjection(List.of(towers), null);
        List<Double> xs = new ArrayList<>();
        for (int i = 0, n = projection.order(scroll); i < n; i++) {
            xs.add(projection.tower(projection.drawn(i)).x());
        }
        return xs;
    }

    private static List<Double> drawnCentres(double scroll, Tower... towers) {
        TowerProjection projection = new TowerProjection(List.of(towers), null);
        List<Double> centres = new ArrayList<>();
        for (int i = 0, n = projection.order(scroll); i < n; i++) {
            centres.add(projection.tower(projection.drawn(i)).centre());
        }
        return centres;
    }

    /** The drawn box of a tower at that scroll: {left, right, low, high}. */
    private static int[] box(Tower t, double scroll) {
        int fx = (int) Math.round(t.x() - t.footWidth() / 2.0);
        int fy = (int) (t.bottom() - Math.round(scroll));
        int w = t.roof().getRegionWidth();
        int hh = t.roof().getRegionHeight();
        int rx = TowerProjection.roofLeft(t.x(), w, t.scale());
        int ry = TowerProjection.roofBottom(t.centre() - scroll, hh, t.scale());
        return new int[] {
            Math.min(fx, rx),
            Math.max(fx + t.footWidth(), rx + w),
            Math.min(fy, ry),
            Math.max(fy + t.footHeight(), ry + hh)
        };
    }

    private static boolean overlap(Tower a, Tower b, double scroll) {
        int[] p = box(a, scroll);
        int[] q = box(b, scroll);
        return p[1] > q[0] && q[1] > p[0] && p[3] > q[2] && q[3] > p[2];
    }

    /** +1 if a is the outer of the two (drawn first), -1 if b is, 0 for either (as the projection decides it). */
    private static int outer(Tower a, Tower b, double scroll) {
        float ax = (float) Math.round(a.x() - a.footWidth() / 2.0);
        float bx = (float) Math.round(b.x() - b.footWidth() / 2.0);
        float ay = a.bottom() - Math.round(scroll);
        float by = b.bottom() - Math.round(scroll);
        int x = TowerProjection.outerFirst(ax, ax + a.footWidth(), bx, bx + b.footWidth(), 240);
        int y = TowerProjection.outerFirst(ay, ay + a.footHeight(), by, by + b.footHeight(), (float) CY);
        return x != 0 && y != 0 && x != y ? 0 : x != 0 ? x : y;
    }
}
