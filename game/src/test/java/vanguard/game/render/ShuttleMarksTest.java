package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Ally;
import vanguard.sim.Command;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part C, step C4b (design/campaign Level 37, The shuttles; design/ui/hud, the friendly marker): Aurelia's Halcyon
 * shuttles (air allies outside the objectives, not {@code neutral}) are marked friendly: the "FRIENDLIES INBOUND — HOLD
 * FIRE" banner opens 2 s before each pair lifts (t=42, 70 and 125), and the IFF brackets sit on each shuttle from its
 * pad to its climb out, none on a shuttle home or lost. Level 37 at medium, flown invulnerable low in the middle.
 */
class ShuttleMarksTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(37).orElseThrow();

    @Test
    void theShuttlesWearTheBracketsUnderTheBanner() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FriendlyMarks marks = new FriendlyMarks(null, null);
        FriendlyBanner banner = new FriendlyBanner(null, null, sortie.airEscort());
        List<Double> openings = new ArrayList<>();
        List<String> misses = new ArrayList<>();
        int most = 0;
        long limit = SimStep.ticks(170);
        while (!sortie.complete() && sortie.tick() < limit) {
            sortie.step(Command.FIRE.bit());
            boolean was = banner.showing();
            banner.step(sortie.friendliesInbound(), sortie.tick());
            if (banner.showing() && !was) {
                openings.add(sortie.levelSeconds());
            }
            marks.plan(sortie, 1);
            int expected = 0;
            for (int k = 0; k < sortie.allyCount(); k++) {
                Ally ally = sortie.ally(k);
                var box = sortie.allySpec(k).hitbox();
                boolean marked =
                        FriendlyMarks.markedAlly(ally, ally.renderX(1), ally.renderY(1), box.width(), box.height());
                if (marked) {
                    expected++;
                }
                if (ally.state() == Ally.State.HOME || !ally.alive()) {
                    assertFalse(marked, "no brackets on a shuttle home or lost");
                }
            }
            if (marks.count() != expected) {
                misses.add(String.format(
                        "t=%.2f: %d marks for %d shuttles", sortie.levelSeconds(), marks.count(), expected));
            }
            most = Math.max(most, marks.count());
        }
        assertTrue(misses.isEmpty(), "the marks follow the shuttles: " + misses);
        assertEquals(3, openings.size(), "a banner for each pair: " + openings);
        double[] lifts = {44, 72, 127};
        for (int g = 0; g < lifts.length; g++) {
            assertEquals(lifts[g] - 2, openings.get(g), 0.05, "2 s before pair " + (g + 1) + " lifts");
        }
        assertTrue(most >= 2, "a pair marked at once, was " + most);
    }
}
