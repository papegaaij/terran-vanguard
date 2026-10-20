package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.RadioBlock;

/**
 * {@link RadioTimeline}'s checks for Level 20 (M7 part G), with and without Rook (every run is played with each fit).
 * The runs need the level mapped to a script (its {@code pursuit} waves, step G4); until then they fail with the
 * formation not implemented.
 */
class RadioTimelineLevel20Test extends RadioTimeline {
    RadioTimelineLevel20Test() {
        super(LEVEL_20);
    }

    /**
     * Rook's quiet beat in the lull (t=113) plays without the escort flying (round 36, Q.20d: no {@code requires:
     * escort}), his other timed lines only while he flies.
     */
    @Test
    void rooksQuietBeatPlaysWithoutTheEscort() {
        List<RadioBlock.RadioCue> rook = CONTENT.level(LEVEL_20).radio().stream()
                .filter(cue -> cue.speaker().equals("Rook") && cue.t().isPresent())
                .toList();
        List<RadioBlock.RadioCue> quiet =
                rook.stream().filter(cue -> cue.t().orElseThrow() == 113).toList();
        assertEquals(1, quiet.size(), "Rook's line at t=113");
        assertTrue(quiet.getFirst().requires().isEmpty(), "the quiet beat requires nothing");
        assertTrue(
                rook.stream()
                        .filter(cue -> cue.t().orElseThrow() != 113)
                        .allMatch(cue -> cue.requires().orElse("").equals("escort")),
                "his other timed lines require the escort");
    }
}
