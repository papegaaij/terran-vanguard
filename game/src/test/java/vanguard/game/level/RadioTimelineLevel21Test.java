package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Expression;
import vanguard.content.RadioBlock;

/**
 * {@link RadioTimeline}'s checks for Level 21 (M7 part H), with and without Rook (every run is played with each fit).
 * Its lines are voiced (step VOX-H), the Choir's dying words with its layering on speech.
 */
class RadioTimelineLevel21Test extends RadioTimeline {
    RadioTimelineLevel21Test() {
        super(LEVEL_21);
    }

    /** The death's four lines come in order: the Choir's speech, then Varga, Okafor and Ares Control. */
    @Test
    void theDeathsLinesComeInOrder() {
        List<String> speakers = CONTENT.level(LEVEL_21).radio().stream()
                .filter(cue -> cue.event().orElse(null) == RadioBlock.CueEvent.BOSS_DESTROYED)
                .map(RadioBlock.RadioCue::speaker)
                .toList();
        assertEquals(List.of("The Choir", "Varga", "Okafor", "Ares Control"), speakers);
    }

    /**
     * Step HB: the death's lines are voiced, the Choir's words too (its layering on speech), and the Choir speaks from
     * its glyph in the portrait frame (its looping radio portrait).
     */
    @Test
    void theDeathsLinesAreVoicedAndTheChoirSpeaksFromItsGlyph() {
        for (RadioBlock.RadioCue cue : CONTENT.level(LEVEL_21).radio()) {
            if (cue.event().orElse(null) != RadioBlock.CueEvent.BOSS_DESTROYED) {
                continue;
            }
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, cue.speaker() + ": " + cue.line());
        }
        assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/portraits/radio-the-choir-neutral_0.png")), "the glyph");
        assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/portraits/radio-the-choir-neutral_1.png")), "a loop");
    }

    /** Rook's phase line only while he flies. */
    @Test
    void rooksPhaseLineRequiresTheEscort() {
        assertTrue(CONTENT.level(LEVEL_21).radio().stream()
                .filter(cue -> cue.speaker().equals("Rook"))
                .allMatch(cue -> cue.requires().orElse("").equals("escort")));
    }
}
