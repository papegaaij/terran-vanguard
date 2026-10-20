package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Hitbox;

/** Only a trigger whose spent frame is a wreck (Level 08's billboard) breaks as it is spent. */
class TriggerBreakTest {
    private static GroundObjectSpec object(int hits, String look) {
        return new GroundObjectSpec(
                0,
                240,
                new Hitbox(72, 40),
                hits > 0 ? 0 : 20,
                0,
                Optional.empty(),
                hits,
                hits > 0 ? 160 : 0,
                hits > 0 ? "cache" : "",
                false,
                hits > 0 ? 0 : -1,
                1,
                Optional.empty(),
                look);
    }

    @Test
    void theBillboardToppleBreaksTheOpenedAndSwitchedOffTriggersDoNot() {
        assertTrue(TriggerBreak.breaks(object(3, "billboard")));
        assertFalse(TriggerBreak.breaks(object(3, "survey-cache")), "shot open");
        assertFalse(TriggerBreak.breaks(object(2, "data-core-terminal")), "its core released");
        assertFalse(TriggerBreak.breaks(object(3, GroundObjectSpec.CARGO_CONTAINER)), "the beacon or a trigger light");
        assertFalse(TriggerBreak.breaks(object(0, "billboard")), "a destructible breaks by its own event");
    }

    @Test
    void inLevels01To08OnlyTheBillboardBreaks() {
        Content content = ContentLoader.fromClasspath();
        Set<String> breaking = new HashSet<>();
        for (int number = 1; number <= 8; number++) {
            Optional<String> key = content.levelKey(number);
            if (key.isEmpty()) {
                continue;
            }
            for (Difficulty difficulty : Difficulty.values()) {
                for (GroundObjectSpec spec :
                        SimSpecs.level(content, key.get(), difficulty).groundObjects()) {
                    if (TriggerBreak.breaks(spec)) {
                        breaking.add(spec.look());
                    }
                }
            }
        }
        // Level 08's billboard once its data names its sprite; Level 06's survey cache and terminal never.
        assertTrue(Set.of("billboard").containsAll(breaking), breaking.toString());
    }

    @Test
    void theRelayHutsDoorShowsItsHitsAndBurstsAsItIsSpent() {
        assertTrue(TriggerBreak.doorBursts(object(3, "relay-hut")));
        assertFalse(TriggerBreak.breaks(object(3, "relay-hut")), "no wreck: its burst frame stays");
        assertFalse(TriggerBreak.doorBursts(object(0, "relay-hut")), "a destructible breaks by its own event");
        // medium's 3 hits: sealed, one hit, two hits, burst; hard's 5: 0, 1, 1, 2, 2, burst
        assertEquals(List.of(0, 1, 2, 3), doorFrames(3));
        assertEquals(List.of(0, 1, 1, 2, 2, 3), doorFrames(5));
    }

    private static List<Integer> doorFrames(int hits) {
        List<Integer> frames = new ArrayList<>();
        for (int left = hits; left >= 0; left--) {
            frames.add(TriggerBreak.doorFrame(hits, left, left == 0));
        }
        return frames;
    }
}
