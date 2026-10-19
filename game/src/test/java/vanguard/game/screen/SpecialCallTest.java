package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.SpecialsData;
import vanguard.content.voice.VoiceLines;
import vanguard.sim.SpecialSpec;

/**
 * M7 part A: a special's press answers on the radio by its kind (step A3's finding: the level screen
 * took every special but the Smart Bomb for the Airstrike): Hammer Lead's call for the Airstrike, Lance
 * Control's "Lance uplink locked. Firing!" for the Orbital Lance (design/player/specials), nothing for
 * the Smart Bomb, the EMP Burst and the Shield Overcharge. Both calls are spoken (Lance Control cast in
 * round 37, b): the radio finds each one's voice file as the level screen does.
 */
class SpecialCallTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    @Test
    void eachSpecialAnswersWithItsOwnCallOrNone() {
        SpecialsData specials = CONTENT.specials();
        SpecialsData.Radio hammer = LevelScreen.specialCall(SpecialSpec.Kind.AIRSTRIKE, specials);
        assertEquals("Hammer Lead", hammer.speaker());
        SpecialsData.Radio lance = LevelScreen.specialCall(SpecialSpec.Kind.LANCE, specials);
        assertEquals("Lance Control", lance.speaker());
        assertEquals("generic-cdf", lance.portrait());
        assertEquals("Lance uplink locked. Firing!", lance.line());
        assertNull(LevelScreen.specialCall(SpecialSpec.Kind.SMART_BOMB, specials));
        assertNull(LevelScreen.specialCall(SpecialSpec.Kind.EMP, specials));
        assertNull(LevelScreen.specialCall(SpecialSpec.Kind.OVERCHARGE, specials));
        assertNull(LevelScreen.specialCall(null, specials), "no special fitted");
    }

    @Test
    void bothCallsAreSpoken() {
        var index = VoiceLines.radioIndex(CONTENT);
        SpecialsData specials = CONTENT.specials();
        for (SpecialSpec.Kind kind : new SpecialSpec.Kind[] {SpecialSpec.Kind.AIRSTRIKE, SpecialSpec.Kind.LANCE}) {
            SpecialsData.Radio call = LevelScreen.specialCall(kind, specials);
            String voice = VoiceLines.radioVoice(index, CONTENT.voices(), call.speaker(), call.line(), "neutral")
                    .orElseThrow(() -> new AssertionError(call.speaker() + " has no voice"));
            assertTrue(Files.isRegularFile(ASSETS.resolve(voice)), voice);
        }
        assertEquals(
                "lance-control", CONTENT.voices().voiceOf("Lance Control").orElse(null), "Lance Control's own voice");
    }
}
