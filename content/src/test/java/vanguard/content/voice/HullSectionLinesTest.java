package vanguard.content.voice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.content.VoiceData;

/**
 * M9 part D: a hull escort's {@code {section}} lines (Level 31's mining tug) get one take per section name (bow, cargo,
 * engines), as a convoy's {@code {ally}} lines get one per unit.
 */
class HullSectionLinesTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_31 = "act-5-the-belt/level-31-mining-rig-escort";
    private static final String BELLWETHER = "Bellwether";

    @Test
    void theTugsSectionsNameThePlaceholder() {
        assertEquals(List.of("bow", "cargo", "engines"), VoiceLines.sections(CONTENT, level()));
        assertEquals(
                List.of(),
                VoiceLines.sections(CONTENT, CONTENT.levels().get("act-2-homefront/level-11-atlantic-convoy")));
    }

    @Test
    void everySectionLineGetsOneTakePerSection() {
        List<String> texts = VoiceLines.radio(cast(), level(), VoiceLines.sections(CONTENT, level()), LEVEL_31).stream()
                .filter(line -> line.speaker().equals(BELLWETHER))
                .map(VoiceLines.VoiceLine::text)
                .toList();
        for (String section : List.of("bow", "cargo", "engines")) {
            assertTrue(texts.contains("We're hit in the " + section + "! Get them off us!"), section + " hit");
            assertTrue(texts.contains("We've lost the " + section + "! We're still flying."), section + " lost");
        }
        assertEquals(
                0,
                texts.stream().filter(text -> text.contains(VoiceLines.SECTION)).count());
    }

    /** The game's whole list expands them too once the speaker is cast (VoiceFilesTest reads it). */
    @Test
    void theGamesLineListUsesTheSections() {
        List<String> placeholders = VoiceLines.all(CONTENT).stream()
                .map(VoiceLines.VoiceLine::text)
                .filter(text -> text.contains(VoiceLines.SECTION))
                .toList();
        assertEquals(List.of(), placeholders);
    }

    private static LevelData level() {
        return CONTENT.levels().get(LEVEL_31);
    }

    /** The voices with the Bellwether cast (a stand-in reference clip) if she is not cast yet. */
    private static VoiceData cast() {
        VoiceData voices = CONTENT.voices();
        if (voices.voiceOf(BELLWETHER).isPresent()) {
            return voices;
        }
        Map<String, VoiceData.Speaker> speakers = new HashMap<>(voices.speakers());
        speakers.replaceAll((slug, speaker) -> speaker.names().contains(BELLWETHER)
                ? new VoiceData.Speaker(
                        speaker.names(),
                        Optional.of("ref-" + slug),
                        speaker.shift(),
                        speaker.fixed(),
                        speaker.layering(),
                        speaker.pins(),
                        Optional.empty(),
                        speaker.filter(),
                        speaker.stage())
                : speaker);
        return new VoiceData(voices.expressions(), speakers, voices.respell());
    }
}
