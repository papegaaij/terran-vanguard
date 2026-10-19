package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part C (design/campaign Level 16, Terrain): the rock's geometry on hand-built walls and islands after
 * Level 16's wall table: the row helper, the walls' lines linear between their points (none beyond them),
 * the rock test, the beam's ceiling, the channels and their spans (the fork and the rock pillar), the way
 * off rock and the contact normal.
 */
class TerrainTest {
    private static final double EPS = 1e-9;

    /** Level 16's rapids with the pillar's explicit points (risk 4: 130 + 150 px beside it, not 120). */
    static final Terrain RAPIDS = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {18_050, 19_200, 19_476, 20_350}, new double[] {70, 70, 70, 90})),
            Optional.of(
                    new Terrain.Wall(new double[] {18_050, 19_200, 19_476, 20_350}, new double[] {400, 400, 400, 370})),
            List.of(new Terrain.Island(
                    new double[] {200, 250, 250, 200}, new double[] {19_200, 19_200, 19_476, 19_476})));

    /** The canyon around the fork: walls at 40 and 440, the fork island's diamond (channels 40–190, 290–440). */
    static final Terrain FORK = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {12_000, 16_000}, new double[] {40, 40})),
            Optional.of(new Terrain.Wall(new double[] {12_000, 16_000}, new double[] {440, 440})),
            List.of(new Terrain.Island(
                    new double[] {240, 190, 190, 240, 290, 290},
                    new double[] {12_830, 13_370, 14_810, 15_350, 14_810, 13_370})));

    /** A left wall leaning in: x 40 at row 0 to 140 at row 200 (0.5 px per px), no right wall. */
    static final Terrain LEAN = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {0, 200}, new double[] {40, 140})), Optional.empty(), List.of());

    @Test
    void aScreenRowIsTheScrollAtWhichItReachesTheTopEdge() {
        assertEquals(1_000, Terrain.row(1_000, PlayField.HEIGHT), EPS);
        assertEquals(460, Terrain.row(1_000, 0), EPS);
        assertEquals(300, Terrain.y(1_000, Terrain.row(1_000, 300)), EPS);
    }

    @Test
    void wallsAreLinearBetweenTheirPointsAndAbsentBeyondThem() {
        assertEquals(70, RAPIDS.leftAt(19_300), EPS, "explicit points: no curve through the pillar's rows");
        assertEquals(80, RAPIDS.leftAt((19_476 + 20_350) / 2.0), EPS);
        assertEquals(385, RAPIDS.rightAt((19_476 + 20_350) / 2.0), EPS);
        assertTrue(Double.isNaN(RAPIDS.leftAt(18_049)));
        assertTrue(Double.isNaN(RAPIDS.rightAt(20_351)));
        assertThrows(IllegalArgumentException.class, () -> new Terrain.Wall(new double[] {5, 5}, new double[] {1, 2}));
        assertThrows(
                IllegalArgumentException.class, () -> new Terrain.Island(new double[] {1, 2}, new double[] {1, 2}));
    }

    @Test
    void thePillarSplitsTheRapidsIntoAChannelOf130AndOneOf150() {
        // Scroll so that row 19,300 is at mid-screen (y 270).
        double scroll = 19_300 + PlayField.HEIGHT - 270;
        assertEquals(2, RAPIDS.channels(scroll, 270));
        assertEquals(70, RAPIDS.spanLeft(scroll, 270, 100), EPS);
        assertEquals(200, RAPIDS.spanRight(scroll, 270, 100), EPS);
        assertEquals(250, RAPIDS.spanLeft(scroll, 270, 300), EPS);
        assertEquals(400, RAPIDS.spanRight(scroll, 270, 300), EPS);
        assertTrue(Double.isNaN(RAPIDS.spanLeft(scroll, 270, 225)), "in the pillar");
        assertTrue(Double.isNaN(RAPIDS.spanRight(scroll, 270, 50)), "in the left wall");
        // Below the pillar: one channel again.
        assertEquals(1, RAPIDS.channels(scroll, 270 - 200));
    }

    @Test
    void theForkSplitsTheCanyonWhereTheIslandIsAndOnlyThere() {
        double scroll = 14_000 + PlayField.HEIGHT - 270;
        assertEquals(2, FORK.channels(scroll, 270));
        assertEquals(40, FORK.spanLeft(scroll, 270, 100), EPS);
        assertEquals(190, FORK.spanRight(scroll, 270, 100), EPS);
        assertEquals(290, FORK.spanLeft(scroll, 270, 400), EPS);
        assertEquals(440, FORK.spanRight(scroll, 270, 400), EPS);
        // Halfway along the tip's edge (240, 12,830) → (190, 13,370): x 215.
        double tipScroll = 13_100 + PlayField.HEIGHT - 270;
        assertEquals(215, FORK.spanRight(tipScroll, 270, 100), EPS);
        assertEquals(265, FORK.spanLeft(tipScroll, 270, 400), EPS);
        assertEquals(1, FORK.channels(12_000 + PlayField.HEIGHT - 270, 270));
        // No walls past the data: the field's edges bound the span.
        assertEquals(0, FORK.spanLeft(20_000, 270, 240), EPS);
        assertEquals(PlayField.WIDTH, FORK.spanRight(20_000, 270, 240), EPS);
    }

    @Test
    void theRockTestCoversWallsAcrossTheBoxRowsAndIslands() {
        // The leaning wall at rows 100–120 is at x 90–100: a box from x 95 overlaps it at its top only.
        double scroll = 110 + PlayField.HEIGHT - 300;
        assertTrue(LEAN.rock(scroll, 105, 300, 10, 10), "the wall reaches 100 at the box's top row");
        assertFalse(LEAN.rock(scroll, 111, 300, 10, 10));
        assertFalse(LEAN.rock(scroll, 300, 300, 10, 10), "no right wall");
        double pillar = 19_300 + PlayField.HEIGHT - 270;
        assertTrue(RAPIDS.rock(pillar, 195, 270, 6, 6), "the pillar's side");
        assertFalse(RAPIDS.rock(pillar, 190, 270, 6, 6));
        assertTrue(RAPIDS.rock(pillar, 225, 270 - 100 + 2, 4, 4), "the pillar's foot (its bottom row at y 170)");
        assertFalse(RAPIDS.rock(pillar, 225, 270 - 100 - 5, 4, 4));
    }

    @Test
    void theCeilingIsTheFirstRockAboveInTheColumn() {
        // The pillar's bottom row 19,200 is 100 px below row 19,300 at y 270: y 170.
        double pillar = 19_300 + PlayField.HEIGHT - 270;
        assertEquals(170, RAPIDS.ceiling(pillar, 225, 3, 50), EPS);
        assertEquals(170, RAPIDS.ceiling(pillar, 199, 3, 50), EPS, "its edge reaches the column's side");
        assertEquals(Double.POSITIVE_INFINITY, RAPIDS.ceiling(pillar, 190, 3, 50));
        assertEquals(200, RAPIDS.ceiling(pillar, 225, 3, 200), EPS, "from inside the rock: there");
        // The leaning wall reaches a column at x 90 (half width 5: from 85) at row 90.
        assertEquals(Terrain.y(0, 90), LEAN.ceiling(0, 90, 5, Terrain.y(0, 0)), EPS);
        // The fork's tip reaches x 240 first.
        double fork = 12_000;
        assertEquals(Terrain.y(fork, 12_830), FORK.ceiling(fork, 240, 1, Terrain.y(fork, 12_500)), EPS);
    }

    @Test
    void theWayOffRockIsBesideTheWallOrTheNearerSideOfTheIsland() {
        double pillar = 19_300 + PlayField.HEIGHT - 270;
        assertEquals(78, RAPIDS.flyableX(pillar, 270, 60, 8), EPS);
        assertEquals(392, RAPIDS.flyableX(pillar, 270, 420, 8), EPS);
        assertEquals(192, RAPIDS.flyableX(pillar, 270, 210, 8), EPS);
        assertEquals(258, RAPIDS.flyableX(pillar, 270, 245, 8), EPS);
        assertEquals(150, RAPIDS.flyableX(pillar, 270, 150, 8), EPS, "clear: unchanged");
        // Off the field without a wall there: unchanged.
        assertEquals(-30, LEAN.flyableX(5_000, 270, -30, 8), EPS);
    }

    @Test
    void contactGivesTheNormalOutOfTheRockAndTheDepth() {
        Hull box = new Hull(List.of(new Hull.Part(0, 0, new Hitbox(20, 20))));
        double[] out = new double[3];
        double pillar = 19_300 + PlayField.HEIGHT - 270;
        assertFalse(RAPIDS.contact(pillar, box, 150, 270, out));
        // 4 px into the left wall (its line at 70): pushed right by 4.
        assertTrue(RAPIDS.contact(pillar, box, 76, 270, out));
        assertEquals(1, out[0], EPS);
        assertEquals(0, out[1], EPS);
        assertEquals(4, out[2], EPS);
        // 3 px into the pillar's left side: pushed left by 3.
        assertTrue(RAPIDS.contact(pillar, box, 193, 270, out));
        assertEquals(-1, out[0], EPS);
        assertEquals(3, out[2], EPS);
        // Under the pillar's foot (y 170), 2 px in: pushed down.
        assertTrue(RAPIDS.contact(pillar, box, 225, 162, out));
        assertEquals(0, out[0], EPS);
        assertEquals(-1, out[1], EPS);
        assertEquals(2, out[2], EPS);
        // The leaning wall (slope 0.5): the normal leans down, the depth is across the line.
        double scroll = 110 + PlayField.HEIGHT - 300;
        assertTrue(LEAN.contact(scroll, box, 105, 300, out));
        double norm = Math.sqrt(1.25);
        assertEquals(1 / norm, out[0], EPS);
        assertEquals(-0.5 / norm, out[1], EPS);
        assertEquals(5 / norm, out[2], EPS, "the wall reaches 100 at the top row, the box from 95");
    }
}
