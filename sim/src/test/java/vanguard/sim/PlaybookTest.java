package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1a ({@link Playbook}, the draw F's {@link AcePlaybook} and H's {@link BossPlaybook} share): the
 * candidates offered in order, the disallowed at weight 0; a draw times the total falls on one by weight, the last
 * allowed taking the rounding's rest; none allowed, none picked; the spacing "at most every n-th draw".
 */
class PlaybookTest {
    @Test
    void aDrawFallsOnTheAllowedCandidatesByWeight() {
        Playbook draw = new Playbook(4);
        draw.clear();
        assertEquals(0, draw.offer(1, true));
        assertEquals(1, draw.offer(5, false));
        assertEquals(2, draw.offer(2, true));
        assertEquals(3, draw.offer(1, true));
        assertEquals(4, draw.total(), 1e-12, "the disallowed one counts nothing");
        assertEquals(0, draw.pick(0.0));
        assertEquals(0, draw.pick(0.999));
        assertEquals(2, draw.pick(1.0));
        assertEquals(2, draw.pick(2.999));
        assertEquals(3, draw.pick(3.5));
        assertEquals(3, draw.pick(4.0), "past the total (rounding): the last allowed");
        assertEquals(2, draw.pick(3.5, 3), "among the first three only");
        draw.clear();
        draw.offer(3, false);
        assertEquals(0, draw.total(), 1e-12);
        assertEquals(Playbook.NONE, draw.pick(0));
    }

    @Test
    void aSpacedCandidateComesAtMostEveryNthDraw() {
        assertTrue(Playbook.spaced(0, Playbook.NEVER, 3), "never drawn: free");
        // Drawn as draw 4: draws 5 and 6 are too soon, 7 is the third after it.
        assertFalse(Playbook.spaced(4, 4, 3));
        assertFalse(Playbook.spaced(5, 4, 3));
        assertTrue(Playbook.spaced(6, 4, 3));
        assertTrue(Playbook.spaced(4, 4, 1), "every draw: no limit");
    }

    @Test
    void theSameSeedDrawsTheSameAndEveryAllowedCandidateComesUp() {
        int[] counts = new int[3];
        SplitMix64 one = new SplitMix64(7);
        SplitMix64 two = new SplitMix64(7);
        Playbook a = new Playbook(3);
        Playbook b = new Playbook(3);
        for (int i = 0; i < 300; i++) {
            a.clear();
            b.clear();
            for (int k = 0; k < 3; k++) {
                a.offer(k + 1, true);
                b.offer(k + 1, true);
            }
            int picked = a.pick(one.nextDouble() * a.total());
            assertEquals(picked, b.pick(two.nextDouble() * b.total()));
            counts[picked]++;
        }
        assertTrue(counts[0] > 20 && counts[1] > counts[0] && counts[2] > counts[1], java.util.Arrays.toString(counts));
    }
}
