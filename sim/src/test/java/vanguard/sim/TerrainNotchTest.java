package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part E, the trench's notches (design/campaign Level 25, The caves; m8e-gaps.md §1.1, §5 E5): a notch 70 px deep
 * and 90 px wide is open water with rock on three sides for the rock test, the wall contact, the pushes out of rock,
 * the beams and the spans asked with notches; the collision lines, the plain spans and the channels are the walls'
 * lines without them; a terrain without notches answers as before.
 */
class TerrainNotchTest {
    private static final double SCROLL = 1_240;

    private static final Terrain.Wall LEFT = new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {110, 110});
    private static final Terrain.Wall RIGHT = new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {370, 370});

    /** A cave in the left wall at row 1,000 (rows 955–1,045, back at x 40) and one in the right at row 2,000. */
    static final Terrain TRENCH = new Terrain(
            Optional.of(LEFT),
            Optional.of(RIGHT),
            List.of(),
            List.of(
                    new Terrain.Notch(Terrain.Side.LEFT, 1_000, 90, 70),
                    new Terrain.Notch(Terrain.Side.RIGHT, 2_000, 90, 70)));

    private static final Terrain PLAIN = new Terrain(Optional.of(LEFT), Optional.of(RIGHT), List.of());

    private static final Hull BOX = new Hull(List.of(new Hull.Part(0, 0, new Hitbox(20, 20))));

    private static double y(double row) {
        return Terrain.y(SCROLL, row);
    }

    @Test
    void aNotchIsOpenWaterWithRockOnThreeSides() {
        assertFalse(TRENCH.rock(SCROLL, 75, y(1_000), 0, 0), "inside the notch");
        assertTrue(PLAIN.rock(SCROLL, 75, y(1_000), 0, 0), "rock without it");
        assertTrue(TRENCH.rock(SCROLL, 30, y(1_000), 0, 0), "behind its back");
        assertTrue(TRENCH.rock(SCROLL, 75, y(950), 0, 0), "below its floor");
        assertTrue(TRENCH.rock(SCROLL, 75, y(1_050), 0, 0), "above its ceiling");
        assertFalse(TRENCH.rock(SCROLL, 75, y(1_000), 10, 10), "a box inside it");
        assertTrue(TRENCH.rock(SCROLL, 75, y(950), 10, 10), "a box across its floor");
        assertFalse(TRENCH.rock(SCROLL, 405, y(2_000), 10, 10), "inside the right wall's notch");
        assertTrue(TRENCH.rock(SCROLL, 405, y(1_000), 10, 10), "the right wall without a notch there");
        assertEquals(110, TRENCH.leftAt(1_000), "the collision line is the wall's");
        assertEquals(40, TRENCH.leftRockAt(1_000));
        assertEquals(440, TRENCH.rightRockAt(2_000));
        assertEquals(370, TRENCH.rightRockAt(1_000));
        assertEquals(110, TRENCH.notchMouth(0));
        assertEquals(370, TRENCH.notchMouth(1));
    }

    @Test
    void theSpansTreatANotchAsRockUnlessAskedWithNotches() {
        assertEquals(110, TRENCH.spanLeft(SCROLL, y(1_000), 200));
        assertEquals(40, TRENCH.spanLeft(SCROLL, y(1_000), 200, true));
        assertTrue(Double.isNaN(TRENCH.spanLeft(SCROLL, y(1_000), 75)), "in rock without notches");
        assertEquals(40, TRENCH.spanLeft(SCROLL, y(1_000), 75, true));
        assertEquals(370, TRENCH.spanRight(SCROLL, y(1_000), 75, true));
        assertEquals(440, TRENCH.spanRight(SCROLL, y(2_000), 200, true));
        assertEquals(370, TRENCH.spanRight(SCROLL, y(2_000), 200));
        assertEquals(1, TRENCH.channels(SCROLL, y(1_000)), "a notch is no channel");
    }

    @Test
    void rockPushesOutToTheNotchsBackOrTheLine() {
        assertEquals(75, TRENCH.flyableX(SCROLL, y(1_000), 75, 10));
        assertEquals(50, TRENCH.flyableX(SCROLL, y(1_000), 20, 10), "out of its back");
        assertEquals(120, TRENCH.flyableX(SCROLL, y(900), 75, 10), "out of the wall beside it");
    }

    @Test
    void aBeamInTheNotchStopsAtItsCeiling() {
        assertEquals(y(1_045), TRENCH.ceiling(SCROLL, 75, 5, y(980)), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, TRENCH.ceiling(SCROLL, 200, 5, y(900)));
        assertEquals(y(900), TRENCH.ceiling(SCROLL, 75, 5, y(900)), 1e-9, "in the wall below it");
    }

    @Test
    void theContactPushesOffTheFloorTheBackAndTheCeiling() {
        double[] touch = new double[3];
        assertTrue(TRENCH.contact(SCROLL, BOX, 75, y(1_040), touch), "against the ceiling");
        assertArrayEquals(new double[] {0, -1, 5}, touch);
        assertTrue(TRENCH.contact(SCROLL, BOX, 75, y(958), touch), "across the floor");
        assertArrayEquals(new double[] {0, 1, 7}, touch);
        assertTrue(TRENCH.contact(SCROLL, BOX, 45, y(1_000), touch), "against the back");
        assertArrayEquals(new double[] {1, 0, 5}, touch);
        assertFalse(TRENCH.contact(SCROLL, BOX, 55, y(1_000), touch), "clear inside");
        assertTrue(TRENCH.contact(SCROLL, BOX, 115, y(950), touch), "from the channel into the floor's lip");
        assertArrayEquals(new double[] {1, 0, 5}, touch);
        assertTrue(TRENCH.contact(SCROLL, BOX, 395, y(2_040), touch), "the right wall's notch's ceiling");
        assertArrayEquals(new double[] {0, -1, 5}, touch);
        // Away from the notches the walls touch as before.
        double[] plain = new double[3];
        assertTrue(TRENCH.contact(SCROLL, BOX, 115, y(3_000), touch));
        assertTrue(PLAIN.contact(SCROLL, BOX, 115, y(3_000), plain));
        assertArrayEquals(plain, touch);
    }

    @Test
    void aNotchLiesOnItsWallAndNotOnAnother() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Terrain(
                        Optional.of(LEFT),
                        Optional.empty(),
                        List.of(),
                        List.of(new Terrain.Notch(Terrain.Side.RIGHT, 1_000, 90, 70))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Terrain(
                        Optional.of(LEFT),
                        Optional.of(RIGHT),
                        List.of(),
                        List.of(new Terrain.Notch(Terrain.Side.LEFT, -990, 90, 70))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Terrain(
                        Optional.of(LEFT),
                        Optional.of(RIGHT),
                        List.of(),
                        List.of(
                                new Terrain.Notch(Terrain.Side.LEFT, 1_000, 90, 70),
                                new Terrain.Notch(Terrain.Side.LEFT, 1_050, 90, 70))));
        assertThrows(IllegalArgumentException.class, () -> new Terrain.Notch(Terrain.Side.LEFT, 1_000, 0, 70));
    }

    private static void assertArrayEquals(double[] expected, double[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], 1e-9, "component " + i + " of " + java.util.Arrays.toString(actual));
        }
    }
}
