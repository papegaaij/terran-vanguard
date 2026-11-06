package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.sim.PlayField;

/**
 * M11 part D, step D4 (design/campaign Level 46, Layout: the starless patch; m11d-gaps.md §1 #7): only Level 46 has it;
 * it is fixed on the screen (its place a constant, whatever the scroll), about 120 px across at (360, 150 below the
 * top), in the deep's base colour, and its edge is smooth: far more than 32 alpha steps, no jump between neighbouring
 * pixels, fading to nothing inside its canvas (DitherGuardTest's rule for a glow, held by a code-drawn mask).
 */
class StarlessPatchTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    @Test
    void onlyLevel46HasItAtItsFixedPlace() {
        assertNotNull(StarlessPatch.of(CONTENT.levelKey(46).orElseThrow()));
        for (int n : new int[] {1, 34, 44, 45}) {
            assertNull(StarlessPatch.of(CONTENT.levelKey(n).orElseThrow()), "Level " + n);
        }
        assertEquals(360, StarlessPatch.X);
        assertEquals(PlayField.HEIGHT - 150, StarlessPatch.centreY(), 1e-6, "150 px below the top");
        assertEquals(6, Math.round(StarlessPatch.BASE.r * 255));
        assertEquals(5, Math.round(StarlessPatch.BASE.g * 255));
        assertEquals(10, Math.round(StarlessPatch.BASE.b * 255), "the deep's near-black violet");
    }

    @Test
    void itIsAbout120PxAcrossAndOpaqueInItsMiddle() {
        assertEquals(1, StarlessPatch.alpha(0), 1e-6);
        assertEquals(1, StarlessPatch.alpha(StarlessPatch.INNER), 1e-6);
        assertEquals(0, StarlessPatch.alpha(StarlessPatch.OUTER), 1e-6);
        double half = 0;
        for (double r = 0; r < StarlessPatch.OUTER; r += 0.1) {
            if (StarlessPatch.alpha(r) >= 0.5) {
                half = r;
            }
        }
        assertEquals(StarlessPatch.DIAMETER / 2.0, half, 4, "half alpha about 60 px from its centre");
    }

    @Test
    void itsEdgeIsSmoothAndFadesInsideItsCanvas() {
        int[] mask = StarlessPatch.mask();
        int size = StarlessPatch.SIZE;
        Set<Integer> levels = new HashSet<>();
        int jump = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int a = mask[y * size + x];
                levels.add(a);
                if (x == 0 || y == 0 || x == size - 1 || y == size - 1) {
                    assertEquals(0, a, "nothing on the canvas's border");
                }
                if (x > 0) {
                    jump = Math.max(jump, Math.abs(a - mask[y * size + x - 1]));
                }
                if (y > 0) {
                    jump = Math.max(jump, Math.abs(a - mask[(y - 1) * size + x]));
                }
            }
        }
        assertEquals(255, mask[size / 2 * size + size / 2]);
        assertTrue(levels.size() >= 32, "at least 32 steps: " + levels.size());
        assertTrue(jump <= 12, "no ring between neighbours: " + jump);
        double previous = 2;
        for (double r = 0; r <= StarlessPatch.OUTER + 1; r += 0.25) {
            double a = StarlessPatch.alpha(r);
            assertTrue(a <= previous + 1e-9, "never brighter outward");
            previous = a;
        }
    }
}
