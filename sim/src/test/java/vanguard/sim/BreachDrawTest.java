package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/enemies/bosses/dust-colossus, Echo; m11g-gaps.md §1 #6): the seeded draw of a breaching
 * echo's next point ({@link BreachDraw}): never twice running, the rear at least every third, the eruption at most every
 * third and never first; every point drawn; the same seed draws the same.
 */
class BreachDrawTest {
    static final BreachDraw.Spec SPEC = new BreachDraw.Spec(
            List.of(BreachArena.Pose.FRONT, BreachArena.Pose.LEFT, BreachArena.Pose.RIGHT, BreachArena.Pose.REAR),
            true,
            3,
            3,
            true);

    static List<BreachArena.Pose> draws(long seed, int n) {
        BreachDraw draw = new BreachDraw(SPEC, true);
        draw.draws(new SplitMix64(seed));
        List<BreachArena.Pose> poses = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            BreachArena.Pose peeked = draw.peek();
            assertEquals(peeked, draw.peek(), "a peek draws once");
            poses.add(draw.take());
        }
        return poses;
    }

    @Test
    void theRulesHoldOver200Draws() {
        for (long seed = 1; seed <= 5; seed++) {
            List<BreachArena.Pose> poses = draws(seed, 200);
            Map<BreachArena.Pose, Integer> counts = new EnumMap<>(BreachArena.Pose.class);
            assertNotEquals(BreachArena.Pose.UNDER, poses.getFirst(), "never the eruption first");
            for (int i = 0; i < poses.size(); i++) {
                counts.merge(poses.get(i), 1, Integer::sum);
                if (i > 0) {
                    assertNotEquals(poses.get(i - 1), poses.get(i), "never twice running at " + i);
                }
                if (i >= 2) {
                    assertTrue(
                            poses.subList(i - 2, i + 1).contains(BreachArena.Pose.REAR),
                            "the rear at least every third: " + poses.subList(i - 2, i + 1));
                    long eruptions = poses.subList(i - 2, i + 1).stream()
                            .filter(p -> p == BreachArena.Pose.UNDER)
                            .count();
                    assertTrue(eruptions <= 1, "the eruption at most every third");
                }
            }
            assertEquals(5, counts.size(), "every point and the eruption: " + counts);
        }
    }

    /**
     * Step G5b: the front at least every second draw (before the rear's rule), and each boss phase drawing from its
     * order's points (stage 1 the front and the sides, stage 2 the front and the rear, the eruption from stage 2 on);
     * a point drawn in the old stage that the new one leaves out is drawn again.
     */
    @Test
    void theFrontEverySecondAndEachStageItsPoints() {
        BreachDraw.Spec spec = new BreachDraw.Spec(
                List.of(BreachArena.Pose.FRONT, BreachArena.Pose.LEFT, BreachArena.Pose.RIGHT, BreachArena.Pose.REAR),
                true,
                4,
                3,
                true,
                1,
                true,
                2);
        List<BossSpec.Order> stages = List.of(
                new BossSpec.Order(
                        List.of(BreachArena.Pose.FRONT, BreachArena.Pose.LEFT, BreachArena.Pose.RIGHT), true),
                new BossSpec.Order(List.of(BreachArena.Pose.FRONT, BreachArena.Pose.REAR), true));
        for (long seed = 1; seed <= 5; seed++) {
            BreachDraw draw = new BreachDraw(spec, true, stages);
            draw.draws(new SplitMix64(seed));
            List<BreachArena.Pose> poses = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                poses.add(draw.take(0));
            }
            BreachArena.Pose stale = draw.peek(0);
            assertTrue(stale == BreachArena.Pose.LEFT || stale == BreachArena.Pose.RIGHT, "a side after the front");
            assertTrue(
                    List.of(BreachArena.Pose.REAR, BreachArena.Pose.UNDER).contains(draw.peek(1)),
                    "drawn again in stage 2");
            for (int i = 0; i < 40; i++) {
                poses.add(draw.take(1));
            }
            assertEquals(BreachArena.Pose.FRONT, poses.getFirst(), "the front first");
            for (int i = 0; i < poses.size(); i++) {
                BreachArena.Pose pose = poses.get(i);
                assertTrue(i >= 3 || (pose != BreachArena.Pose.REAR && pose != BreachArena.Pose.UNDER), "stage 1");
                assertTrue(
                        i < 3 || (pose != BreachArena.Pose.LEFT && pose != BreachArena.Pose.RIGHT),
                        "stage 2 hunts from ahead and behind: " + poses);
                if (i > 0) {
                    assertTrue(
                            pose == BreachArena.Pose.FRONT || poses.get(i - 1) == BreachArena.Pose.FRONT,
                            "the front at least every second: " + poses);
                }
            }
            assertTrue(poses.subList(3, poses.size()).contains(BreachArena.Pose.UNDER), "the eruption in stage 2");
        }
    }

    @Test
    void theSameSeedDrawsTheSame() {
        assertEquals(draws(7, 50), draws(7, 50));
        assertNotEquals(draws(7, 50), draws(8, 50));
    }

    @Test
    void aDrawIsCheckedAgainstItsBoss() {
        assertThrows(IllegalArgumentException.class, () -> new BreachDraw(SPEC, false), "an eruption needs one");
        assertThrows(
                IllegalArgumentException.class,
                () -> new BreachDraw.Spec(List.of(BreachArena.Pose.FRONT, BreachArena.Pose.LEFT), true, 3, 0, true),
                "the rear rule needs the rear");
        assertThrows(
                IllegalArgumentException.class,
                () -> new BreachDraw.Spec(List.of(BreachArena.Pose.FRONT, BreachArena.Pose.UNDER), true, 0, 0, true));
    }
}
