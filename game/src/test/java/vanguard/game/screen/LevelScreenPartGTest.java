package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.voice.VoiceLines;
import vanguard.game.level.RadioQueue;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;

/**
 * M5 part G's wiring in the level screen (Level 13): the debrief's row reads {@code RELAY n %}, the
 * relay's integrity left rounded down so a relay under half never reads 50 % (the secondary's
 * threshold); with the integrity secondary met it pays, without it nothing. The relay's loss (round 35's
 * capture): the mission failed screen speaks Okafor's line and the HUD's radio box closes under it.
 */
class LevelScreenPartGTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(13).orElseThrow();

    @Test
    void theDebriefsRelayRowShowsTheIntegrityLeft() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                60);
        DebriefScreen.Hulls count = LevelScreen.debriefCount(sortie);
        assertEquals(DebriefScreen.RELAY, count.label());
        assertTrue(count.percent());
        assertEquals(100, count.afloat(), "untouched at the start");
        DebriefScreen.Row met = DebriefScreen.hullsRow(DebriefScreen.Hulls.share(DebriefScreen.RELAY, 53), true, 152);
        assertEquals("RELAY", met.label());
        assertEquals("53 %", met.middle());
        assertEquals("+ 152 CR", met.right(1));
        DebriefScreen.Row missed = DebriefScreen.hullsRow(DebriefScreen.Hulls.share(DebriefScreen.RELAY, 49), false, 0);
        assertEquals("49 %", missed.middle());
        assertEquals("", missed.right(1), "nothing paid");
        assertEquals(
                "3 / 3",
                DebriefScreen.hullsRow(new DebriefScreen.Hulls(3, 3), true, 0).middle(),
                "counts as before");
    }

    @Test
    void aShareUnderHalfNeverReadsFiftyPercent() {
        assertEquals(50, LevelScreen.percent(0.5));
        assertEquals(49, LevelScreen.percent(0.4999));
        assertEquals(100, LevelScreen.percent(1));
        assertEquals(0, LevelScreen.percent(0));
        assertEquals(29, LevelScreen.percent(70 / 240.0), "70 of 240");
    }

    /**
     * Every level's {@code mission-failed} line, as the mission failed screen looks its voice up (the
     * lost group's name filled in), has its voice file: the screen speaks it (design/systems/retry,
     * design/audio/voice). Level 13's "Nansen is gone. Pull back, Lancer." among them.
     */
    @Test
    void theFailedScreensLineIsVoicedInEveryLevel() {
        Path assets = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
        Map<String, VoiceLines.VoiceLine> index = VoiceLines.radioIndex(CONTENT);
        List<String> voiced = new ArrayList<>();
        for (int number = 1; number <= 50; number++) {
            Optional<String> key = CONTENT.levelKey(number);
            if (key.isEmpty()) {
                continue;
            }
            LevelScript script = SimSpecs.level(CONTENT, key.get(), Difficulty.MEDIUM);
            for (LevelScript.RadioCue cue : script.radio()) {
                if (cue.trigger() != LevelScript.CueTrigger.MISSION_FAILED) {
                    continue;
                }
                List<String> groups = cue.line().contains("{group}") ? script.groups() : List.of("");
                for (String group : groups) {
                    String line = VoiceLines.groupLine(cue.line(), group);
                    Optional<String> voice =
                            VoiceLines.radioVoice(index, CONTENT.voices(), cue.speaker(), line, cue.expression());
                    assertTrue(voice.isPresent(), "level " + number + ": no voice for " + line);
                    assertTrue(Files.isRegularFile(assets.resolve(voice.get())), voice.get());
                    voiced.add(number + ": " + line);
                }
            }
        }
        assertTrue(voiced.contains("13: Nansen is gone. Pull back, Lancer."), String.valueOf(voiced));
    }

    /**
     * The failed screen takes the radio over: the playing voice is cut and the HUD's radio box closes,
     * so the line on the radio at the failure (Varga's in the capture) does not stand frozen under it.
     */
    @Test
    void theFailedScreenClosesTheHudsRadio() {
        RadioQueue radio = new RadioQueue();
        radio.add("Varga", "neutral", "Walkers on the floes. Their claws are armoured from the front.", false);
        radio.add("Rook", "neutral", "Contacts, all sides!", false);
        assertEquals(RadioQueue.Change.OPENED, radio.update(0.01f));
        assertTrue(radio.current().isPresent());
        int[] stopped = {0};

        LevelScreen.failedScreenOpens(radio, () -> stopped[0]++);

        assertEquals(1, stopped[0], "the voice is cut");
        assertFalse(radio.current().isPresent(), "the radio box is closed");
        assertEquals(RadioQueue.Change.NONE, radio.update(5), "nothing waits to open behind the screen");
        assertTrue(radio.visibleLines().isEmpty());
    }
}
