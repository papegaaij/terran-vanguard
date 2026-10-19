package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.PlayField;
import vanguard.sim.Terrain;

/**
 * M7 part C: the canyon's projection (design/campaign Level 16, Rendering; camera model b) on a hand-built
 * terrain: the strips' foot is the collision line, the strips are continuous, the walls lean outward round
 * the projection centre to the plateau's lip at 1.25, and the plateau shows only beyond the lip.
 */
class WallProjectionTest {
    private static final double EPSILON = 1e-6;
    /** A left wall with two bends (rows 1,000 and 1,200) and a right wall starting at row 500. */
    private static final Terrain TERRAIN = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {0, 1000, 1200, 3000}, new double[] {60, 60, 140, 100})),
            Optional.of(new Terrain.Wall(new double[] {500, 3000}, new double[] {420, 380})),
            List.of(new Terrain.Island(new double[] {200, 280, 240}, new double[] {1500, 1500, 1700})));
    /** The right wall rising from the floor at row 500 to full height at row 900 (the canyon mouth's ramp). */
    private static final WallProjection.Looks LOOKS = new WallProjection.Looks(
            WallProjection.Heights.FULL,
            new WallProjection.Heights(new double[] {500, 900}, new double[] {0, 1}),
            List.of(),
            List.of(),
            List.of());

    private static WallProjection prepared(long scroll) {
        WallProjection walls = new WallProjection(TERRAIN, LOOKS, null, null, null, List.of(), List.of());
        walls.prepare(scroll);
        return walls;
    }

    private static double line(int side, double row) {
        return side == WallProjection.LEFT ? TERRAIN.leftAt(row) : TERRAIN.rightAt(row);
    }

    @Test
    void theStripsFootIsTheCollisionLineOnEveryRow() {
        long scroll = 1400;
        WallProjection walls = prepared(scroll);
        for (int side = WallProjection.LEFT; side <= WallProjection.RIGHT; side++) {
            for (int e = 0; e < walls.edgeCount(side); e++) {
                double row = walls.edgeRow(side, e);
                assertEquals(line(side, row), walls.footX(side, e), EPSILON, "edge " + e + "'s foot");
                assertEquals(row - scroll + 540, walls.footY(side, e), EPSILON);
                assertEquals(walls.footX(side, e), walls.faceX(side, e, 0), EPSILON, "the face starts at the foot");
                assertEquals(walls.footY(side, e), walls.faceY(side, e, 0), EPSILON);
            }
            // Between two edges the drawn foot is straight: it follows the line at every screen row, bends included.
            for (int y = 0; y <= 540; y++) {
                double row = Terrain.row(scroll, y);
                int e = 1;
                while (walls.edgeRow(side, e) < row) {
                    e++;
                }
                double r0 = walls.edgeRow(side, e - 1);
                double r1 = walls.edgeRow(side, e);
                double t = (row - r0) / (r1 - r0);
                double drawn = walls.footX(side, e - 1) + (walls.footX(side, e) - walls.footX(side, e - 1)) * t;
                assertEquals(line(side, row), drawn, EPSILON, "side " + side + " at screen row " + y);
            }
        }
    }

    @Test
    void theStripsAreContinuousFromTheBottomEdgeToTheTopEdge() {
        long scroll = 900;
        WallProjection walls = prepared(scroll);
        for (int side = WallProjection.LEFT; side <= WallProjection.RIGHT; side++) {
            int n = walls.edgeCount(side);
            assertTrue(walls.edgeRow(side, 0) <= Terrain.row(scroll, 0));
            assertTrue(walls.edgeRow(side, n - 1) >= Terrain.row(scroll, 540));
            for (int e = 1; e < n; e++) {
                double gap = walls.edgeRow(side, e) - walls.edgeRow(side, e - 1);
                assertTrue(gap > 0 && gap <= WallProjection.CELL + EPSILON, "rows increase by at most a cell");
                boolean wall = !Double.isNaN(line(side, walls.edgeRow(side, e - 1)))
                        && !Double.isNaN(line(side, walls.edgeRow(side, e)));
                assertEquals(wall, walls.cell(side, e), "every cell where the wall is is drawn");
            }
        }
        // The right wall starts at row 500 on screen: an edge exactly there, no cell before it.
        boolean found = false;
        for (int e = 0; e < walls.edgeCount(WallProjection.RIGHT); e++) {
            if (Math.abs(walls.edgeRow(WallProjection.RIGHT, e) - 500) < 1e-6) {
                found = true;
                assertFalse(e > 0 && walls.cell(WallProjection.RIGHT, e), "nothing below the wall's start");
                assertTrue(walls.cell(WallProjection.RIGHT, e + 1), "the strip starts at the wall's first row");
            }
        }
        assertTrue(found, "an edge at the wall's first row");
    }

    @Test
    void theWallsLeanOutwardToThePlateausRimAt125() {
        assertEquals(1.25, WallProjection.scale(1, 1), EPSILON);
        assertEquals(6 / (6 - 0.6), WallProjection.scale(1, 0.5), EPSILON, "half height: half the plateau's 1.2");
        assertEquals(1, WallProjection.scale(1, 0), EPSILON, "a flat wall: no lean");
        long scroll = 1400;
        WallProjection walls = prepared(scroll);
        int rim = CanyonProfile.NODES - 1;
        for (int e = 0; e < walls.edgeCount(WallProjection.LEFT); e++) {
            double foot = walls.footX(WallProjection.LEFT, e);
            double x = walls.nodeX(WallProjection.LEFT, e, rim);
            assertTrue(x < foot, "the rim is set back toward the rock");
            assertEquals(240 + (x - 240) * 1.25, walls.rimX(WallProjection.LEFT, e), EPSILON);
            assertTrue(walls.rimX(WallProjection.LEFT, e) < 240 + (foot - 240) * 1.25, "the left wall leans left");
            double y = walls.footY(WallProjection.LEFT, e);
            assertEquals(
                    BackdropData.Tower.CENTRE_Y + (y - BackdropData.Tower.CENTRE_Y) * 1.25,
                    walls.rimY(WallProjection.LEFT, e),
                    EPSILON,
                    "and away from the centre's row");
        }
        for (int e = 0; e < walls.edgeCount(WallProjection.RIGHT); e++) {
            double foot = walls.footX(WallProjection.RIGHT, e);
            assertTrue(walls.nodeX(WallProjection.RIGHT, e, rim) > foot, "the right rim is set back to the right");
            assertTrue(walls.rimX(WallProjection.RIGHT, e) > 240 + (foot - 240) * 1.25, "the right wall leans right");
        }
        // The canyon mouth's ramp: flat at row 500 (the rim on the foot), half height at row 700.
        WallProjection mouth = prepared(900);
        for (int e = 0; e < mouth.edgeCount(WallProjection.RIGHT); e++) {
            double row = mouth.edgeRow(WallProjection.RIGHT, e);
            if (Math.abs(row - 500) < EPSILON) {
                assertEquals(mouth.footX(WallProjection.RIGHT, e), mouth.rimX(WallProjection.RIGHT, e), EPSILON);
            }
            if (Math.abs(row - 704) < EPSILON) {
                double k = WallProjection.scale(1, 204 / 400.0);
                assertEquals(
                        240 + (mouth.nodeX(WallProjection.RIGHT, e, rim) - 240) * k,
                        mouth.rimX(WallProjection.RIGHT, e),
                        EPSILON);
            }
        }
    }

    @Test
    void theStepsDescendFromTheRimToTheFootOnTheRocksSideOnly() {
        WallProjection walls = prepared(1400);
        for (int side = WallProjection.LEFT; side <= WallProjection.RIGHT; side++) {
            CanyonProfile.Steps steps = walls.steps(side);
            for (double row = steps.start; row <= steps.end; row += 7) {
                assertEquals(0, steps.setback(row, 0), EPSILON, "node 0 is the foot");
                for (int j = 1; j < CanyonProfile.NODES; j++) {
                    assertTrue(
                            steps.setback(row, j) >= steps.setback(row, j - 1) - 1e-4,
                            "side " + side + " row " + row + ": each step edge further into the rock");
                }
            }
        }
    }

    @Test
    void theRimIsRaggedSideCanyonsBiteBackIntoThePlateau() {
        WallProjection walls = prepared(1400);
        CanyonProfile.Steps left = walls.steps(WallProjection.LEFT);
        double low = Double.POSITIVE_INFINITY;
        double high = Double.NEGATIVE_INFINITY;
        // rows 1,200-3,000: the wall at 140 → 100 px from the edge, never a smooth strip
        for (double row = 1300; row < 2900; row += 1) {
            double s = left.setback(row, CanyonProfile.NODES - 1);
            low = Math.min(low, s);
            high = Math.max(high, s);
        }
        assertTrue(high - low > 40, "side canyons: the rim's setback varies " + low + " .. " + high);
        assertTrue(low > 10, "a spur still has its steps: " + low);
    }

    @Test
    void theModelIsTheArtScriptsTwin() {
        // tools/art/backdrop_l16.py's values (hash01, big_notches, notches, fbm1, setbacks)
        assertEquals(0.8073484136257321, CanyonProfile.hash01(0, 41), 1e-15);
        assertEquals(0.6604457460343838, CanyonProfile.hash01(12345, 7), 1e-15);
        assertEquals(4.611870133897816, CanyonProfile.notches(1, 20000), 1e-9);
        assertEquals(0.5476815649988923, CanyonProfile.fbm(1234.5, 48, 300, 3), 1e-12);
        double[] s = new double[CanyonProfile.NODES];
        CanyonProfile.Steps.setbacks(WallProjection.LEFT, 28000, 150, 1, Double.POSITIVE_INFINITY, 0, s);
        double[] python = {0, 5.02227918, 27.17861156, 35.08143635, 67.1461326, 72.74939015, 88.27469815};
        for (int j = 0; j < s.length; j++) {
            assertEquals(python[j], s[j], 1e-6, "node " + j);
        }
        // the plateau tile's gullies drain into the big side canyons: they repeat with the tile (960 / 1.25)
        assertEquals(CanyonProfile.notches(0, 1000), CanyonProfile.notches(0, 1000 + 768 * 7), 30);
    }

    @Test
    void thePlateauShowsOnlyBeyondTheRim() {
        long scroll = 900;
        WallProjection walls = prepared(scroll);
        int left = 0;
        int right = 0;
        for (int y = 0; y < 540; y++) {
            float l = walls.leftClip(y);
            if (!Float.isNaN(l)) {
                left++;
                // The rim of the row whose rim crosses this screen row, never over the floor.
                double footRow = Terrain.row(scroll, 243 + (y + 0.5 - 243) / 1.25);
                double rim = walls.steps(WallProjection.LEFT).x(footRow, CanyonProfile.NODES - 1);
                // the drawn rim is the polyline through the edges' rims (CELL rows apart): within a px of the curve
                assertEquals(240 + (rim - 240) * 1.25, l, 1.0, "row " + y);
                assertTrue(l < TERRAIN.leftAt(Terrain.row(scroll, y + 0.5)), "the plateau stops at the rim");
            }
            float r = walls.rightClip(y);
            if (!Float.isNaN(r)) {
                right++;
                assertTrue(r > TERRAIN.rightAt(Terrain.row(scroll, y + 0.5)) - EPSILON, "row " + y);
            }
        }
        assertEquals(540, left, "the left wall runs the whole screen");
        assertTrue(right > 0 && right < 540, "the right wall starts on screen: " + right);
        assertTrue(Float.isNaN(walls.rightClip(0)), "no plateau where there is no wall");
    }

    @Test
    void lightFromTheUpperLeftShadesTheLeftWallsCliffsAndLightsTheRightWalls() {
        assertEquals(CanyonProfile.FLAT, CanyonProfile.shade(0, 0, 1), 1e-6, "a flat bench");
        assertEquals(CanyonProfile.SHADOWED, CanyonProfile.shade(1, 0, 0), 1e-6, "a cliff facing right: ambient only");
        assertEquals(1, CanyonProfile.shade(-1, 0.5, 0.3), 1e-6, "a cliff facing the key light: lit");
        WallProjection walls = prepared(1400);
        int e = walls.edgeCount(WallProjection.LEFT) / 2;
        int f = walls.edgeCount(WallProjection.RIGHT) / 2;
        for (int g = 0; g < CanyonProfile.SEGMENTS; g += 2) {
            assertTrue(
                    walls.shade(WallProjection.LEFT, e, g) < walls.shade(WallProjection.RIGHT, f, g) - 0.2,
                    "cliff " + g + ": the left wall's in shadow, the right wall's lit");
        }
        // the left wall's benches lie partly in the cast shadow of the cliffs above; the right wall's do not
        double left = 0;
        double right = 0;
        for (int i = 1; i < walls.edgeCount(WallProjection.LEFT) - 1; i++) {
            left += walls.cast(WallProjection.LEFT, i, 1) + walls.cast(WallProjection.LEFT, i, 3);
        }
        for (int i = 1; i < walls.edgeCount(WallProjection.RIGHT) - 1; i++) {
            right += walls.cast(WallProjection.RIGHT, i, 1) + walls.cast(WallProjection.RIGHT, i, 3);
        }
        assertTrue(left > 10 * Math.max(right, 0.01), "cast shadows: left " + left + ", right " + right);
    }

    @Test
    void theLeftWallsShadowFallsOnTheFloorTheRightWallsBehindIt() {
        WallProjection walls = prepared(1400);
        CanyonProfile.Steps left = walls.steps(WallProjection.LEFT);
        CanyonProfile.Steps right = walls.steps(WallProjection.RIGHT);
        for (double row = 1300; row < 2900; row += 50) {
            assertTrue(left.floorShadow(row) > left.line(row) + 5, "row " + row + ": a shadow on the floor");
            assertEquals(right.line(row), right.floorShadow(row), EPSILON);
        }
    }

    @Test
    void aButtesStepsAreRingsInsideItsFootRoundAFlatCap() {
        Terrain.Island foot = new Terrain.Island(new double[] {200, 280, 240}, new double[] {1500, 1500, 1700});
        CanyonProfile.Butte butte = new CanyonProfile.Butte(foot, "pillar");
        double total = 0;
        for (double s : CanyonProfile.SETBACK) {
            total += s * CanyonProfile.islandScale("pillar");
        }
        for (int j = 1; j < CanyonProfile.NODES; j++) {
            for (int i = 0; i < butte.points; i++) {
                double x = butte.x[j][i];
                double row = butte.row[j][i];
                assertTrue(
                        TERRAIN.rock(row, x, PlayField.HEIGHT, 0.01, 0.01),
                        "ring " + j + " point " + i + " inside the foot: " + x + ", " + row);
            }
        }
        // the cap's edge on the base (row 1500) lies the whole setback inside it
        double lowest = Arrays.stream(butte.row[CanyonProfile.NODES - 1]).min().orElseThrow();
        assertEquals(1500 + total, lowest, 1e-6);
    }

    @Test
    void aWallsHeightRampsLinearlyBetweenItsPoints() {
        WallProjection.Heights ramp = new WallProjection.Heights(new double[] {2850, 4180}, new double[] {0, 1});
        assertEquals(0, ramp.at(0), EPSILON);
        assertEquals(0.5, ramp.at(3515), EPSILON);
        assertEquals(1, ramp.at(9000), EPSILON);
        assertEquals(1, WallProjection.Heights.FULL.at(123), EPSILON);
    }

    @Test
    void aSparkFansAwayFromTheRock() {
        // A touch on the left wall's line: rock to its left; on the right wall's: rock to its right.
        double scroll = 1400;
        assertFalse(CanyonLooks.rockRight(TERRAIN, scroll, TERRAIN.leftAt(Terrain.row(scroll, 270)), 270, 0));
        assertTrue(CanyonLooks.rockRight(TERRAIN, scroll, TERRAIN.rightAt(Terrain.row(scroll, 270)), 270, 0));
    }

    @Test
    void theRockArchesAreOverheadPasses() {
        assertTrue(Backdrop.OVERHEAD_PASSES.containsAll(List.of("arch-a", "arch-b", "transport")));
    }

    @Test
    void levelSixteensLooksComeFromTheTerrainTheSimulationGets() {
        String key = "act-3-red-dust/level-16-valles-canyon-run";
        LevelData level = ContentLoader.fromClasspath().level(key);
        WallProjection.Looks looks = WallProjection.Looks.of(level);
        Terrain terrain = SimSpecs.level(ContentLoader.fromClasspath(), key, Difficulty.MEDIUM)
                .terrain()
                .orElseThrow()
                .terrain();
        // the mouth's and the outflow's ramps: flat at 2,850 and 34,850, full from 4,180 to 31,250
        assertEquals(0, looks.left().at(2850), EPSILON);
        assertEquals(1, looks.left().at(4180), EPSILON);
        assertEquals(1, looks.right().at(20000), EPSILON);
        assertEquals(0, looks.right().at(34850), EPSILON);
        assertEquals(terrain.islands(), looks.islands().size());
        for (WallProjection.IslandLook island : looks.islands()) {
            double[] xs = island.foot().xs();
            double[] rows = island.foot().scrolls();
            double x = Arrays.stream(xs).average().orElseThrow();
            double row = Arrays.stream(rows).average().orElseThrow();
            // the polygon's middle is rock in the simulation's terrain (row = scroll at the field's top edge)
            assertTrue(terrain.rock(row, x, PlayField.HEIGHT, 1, 1), island.look() + " at " + x + ", " + row);
        }
        assertEquals(
                List.of(WallProjection.LEFT, WallProjection.RIGHT, WallProjection.LEFT, WallProjection.RIGHT),
                looks.patches().stream().map(WallProjection.FacePatch::side).toList());
        assertEquals(6600, looks.patches().getFirst().row(), 1);
        assertEquals(20180, looks.patches().getLast().row(), 1);
        // the Helix station on the north plateau: the right rim keeps clear of it
        assertEquals(1, looks.caps().size());
        CanyonProfile.Cap cap = looks.caps().getFirst();
        assertEquals(WallProjection.RIGHT, cap.side());
        WallProjection walls = new WallProjection(terrain, looks, null, null, null, List.of(), List.of());
        CanyonProfile.Steps right = walls.steps(WallProjection.RIGHT);
        for (double row = cap.from(); row <= cap.to(); row += 4) {
            double rim = 240 + (right.x(row, CanyonProfile.NODES - 1) - 240) * 1.25;
            assertTrue(rim <= cap.edge(), "row " + row + ": the rim " + rim + " short of the station " + cap.edge());
        }
    }

    @Test
    void aLevelWithoutTerrainHasPlainLooks() {
        LevelData level = ContentLoader.fromClasspath().level("act-1-first-contact/level-01-break-at-dawn");
        assertEquals(WallProjection.Looks.PLAIN, WallProjection.Looks.of(level));
    }
}
