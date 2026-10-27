package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/** {@link RadioTimeline}'s checks for Level 03 and its own tests. */
class RadioTimelineLevel03Test extends RadioTimeline {
    RadioTimelineLevel03Test() {
        super(LEVEL_03);
    }

    @Test
    void theLeviathansLinesFindTheirGapsAndALateReactionIsDropped() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);

        assertTrue(opened(play(script, List.of(new Event(160.7, CueTrigger.ENEMY_ESCAPED, "leviathan"))), "leviathan"));
        assertTrue(opened(play(script, List.of(new Event(150, CueTrigger.FIRST_KILL, "leviathan"))), "leviathan"));
        // killed while Okafor's long line plays: Ring Control would only find a gap after it
        assertTrue(!opened(play(script, List.of(new Event(169, CueTrigger.FIRST_KILL, "leviathan"))), "leviathan"));
    }
}
