package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;

/**
 * {@link RadioTimeline}'s checks for Level 50 (M11 part H, step H2), with and without the escort (every run is played
 * with each fit; RadioRuns#level50Runs): the timed lines, Rook's barks only with him (and Okafor's K + 32 line in its two
 * rows), the seed vault's line, the secondary met or failed by the warning, the Heart's subjects (the closing, the first
 * node, the reversal's tell, the opening, the core's half, K); the clock standing at the arena while the Heart lives
 * and 12 s after K, then the closing gate's lines on the clock, the last before the level's end (K + 58). Its lines are
 * voiced (step VOX-GH), so every timed line has its file; the radio's noise is step 5, then 0 on the near side;
 * the Choir's five lines ride the warning and the Heart's subjects on the clean enemy channel.
 */
class RadioTimelineLevel50Test extends RadioTimeline {
    RadioTimelineLevel50Test() {
        super(LEVEL_50);
    }

    /** The noise step 5, off on the near side; the Choir five times on the enemy channel; Okafor's K + 32 in two rows. */
    @Test
    void theChoirSpeaksFiveTimesAndTheNoiseEndsOnTheNearSide() {
        LevelData data = CONTENT.level(LEVEL_50);
        assertEquals(5, data.radioNoiseStep(), "the act's fifth noise step");
        assertEquals(
                List.of(new LevelData.NoiseChange("near-side", 0)), data.noiseChanges(), "no noise on the near side");
        List<LevelData.RadioCue> choir = data.radio().stream()
                .filter(cue -> cue.speaker().equals("The Choir"))
                .toList();
        assertEquals(5, choir.size(), "the Heart's five lines");
        assertTrue(choir.stream().allMatch(cue -> cue.channel().isPresent()), "the enemy channel");
        assertEquals(
                2,
                data.radio().stream().filter(cue -> cue.t().orElse(0.0) == 208).count(),
                "K + 32: with Rook and without");
    }
}
