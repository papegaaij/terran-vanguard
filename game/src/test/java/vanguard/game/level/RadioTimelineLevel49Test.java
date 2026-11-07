package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;

/**
 * {@link RadioTimeline}'s checks for Level 49 (M11 part G, step G2), with and without the escort (every run is played
 * with each fit; RadioRuns#level49Runs): the timed lines, Rook's barks (t=23.5, t=97.5, the second echo's death, t=126.5)
 * only with him, the supply pod's line, the secondary met or failed by the first warning, the first two echoes' deaths;
 * the clock standing at each of the rush's three gates while its echo lives. Its lines wait for their voices (step
 * VOX-GH: {@link RadioTimeline#UNVOICED}); the radio's noise is step 5; the Choir's two lines ride the first and the
 * third warnings on the clean enemy channel.
 */
class RadioTimelineLevel49Test extends RadioTimeline {
    RadioTimelineLevel49Test() {
        super(LEVEL_49);
    }

    /** The noise step 5; the Choir twice on the enemy channel; the deaths' lines name the first two echoes. */
    @Test
    void theChoirSpeaksAtTheWarningsAndTheEchoesDeathsAreNamed() {
        LevelData data = CONTENT.level(LEVEL_49);
        assertEquals(5, data.radioNoiseStep(), "the act's fifth noise step");
        List<LevelData.RadioCue> choir = data.radio().stream()
                .filter(cue -> cue.speaker().equals("The Choir"))
                .toList();
        assertEquals(2, choir.size(), "the Choir twice");
        assertEquals(
                List.of(91.5, 110.5),
                choir.stream().map(cue -> cue.t().orElseThrow()).toList());
        assertTrue(choir.stream().allMatch(cue -> cue.channel().isPresent()), "the enemy channel");
        assertEquals(
                List.of("brood-carrier", "dust-colossus"),
                data.radio().stream()
                        .filter(cue -> cue.event().orElse(null) == LevelData.CueEvent.BOSS_DESTROYED)
                        .map(cue -> cue.enemy().orElseThrow())
                        .toList());
        assertEquals(
                4,
                data.radio().stream()
                        .filter(cue -> cue.speaker().equals("Rook"))
                        .filter(cue -> cue.requires().orElse("").equals("escort"))
                        .count(),
                "Rook's four barks need him flying");
    }
}
