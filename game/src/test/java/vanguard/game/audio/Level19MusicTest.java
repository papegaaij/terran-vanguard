package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.game.settings.AudioSettings;

/**
 * M7 part F's music for Level 19 (design/campaign Level 19, Music & ambience): track 9 "Grey Signal", the motif layer
 * from the walker's arena on, and in the crawl after it (section 6, {@code stems: motif}) the motif alone over the
 * ambience: the stems faded out under it with a stems-only gain (risk 6: the section's level would dim the motif too),
 * back for the run-out's base stem, which plays without the motif.
 */
class Level19MusicTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL = "act-3-red-dust/level-19-foundry-of-tharsis";

    /** A mono stream of one constant sample value. */
    private record Constant(short value, int sampleRate) implements PcmStream {
        @Override
        public void read(short[] out) {
            java.util.Arrays.fill(out, value);
        }

        @Override
        public int channels() {
            return 1;
        }

        @Override
        public void close() {}
    }

    @Test
    void theCrawlPlaysTheMotifAloneAndTheRunOutTheBaseStem() {
        LevelData.Music music = CONTENT.level(LEVEL).music();
        assertEquals(9, music.track());
        assertFalse(music.motifAlone(5), "the arena: the stems with the motif");
        assertTrue(music.motifAlone(6), "the crawl: the motif alone");
        assertFalse(music.motifAlone(7));
        assertFalse(music.full(6) || music.full(7), "the base stem under the crawl's fade and in the run-out");
        assertTrue(music.motifOn(5, 133), "the motif from the walker's arena");
        assertTrue(music.motifOn(6, 150));
        assertFalse(music.motifOn(7, 165), "not in the run-out");
    }

    @Test
    void theStemsGoSilentUnderTheMotifOverTwoSecondsAndComeBack() {
        var mix = new StemMix(
                new Constant((short) 1000, 100), new Constant((short) 3000, 100), new Constant((short) 400, 100), 1);
        mix.motif(true);
        short[] out = new short[200];
        mix.read(out);
        assertEquals(1400, out[199], "the base stem with the motif on top");
        mix.stems(false);
        short[] fade = new short[200];
        mix.read(fade);
        assertEquals(900, fade[99], 3, "half way after a second");
        assertEquals(400, fade[199], "the motif alone after 2 s: its level untouched");
        for (int i = 1; i < fade.length; i++) {
            assertTrue(fade[i - 1] - fade[i] <= 6, "no click at " + i);
        }
        mix.stems(true);
        mix.motif(false);
        short[] back = new short[400];
        mix.read(back);
        assertEquals(1000, back[399], "the base stem alone again");
    }

    @Test
    void aMotifAloneSectionSilencesTheStemsAndPlaysTheMotif() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank bank = new SfxBank(audio, files, mixer);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("grey-signal")),
                        files.internal(Tracks.path(9)),
                        bank,
                        Sfx.ambience("mars-dust"),
                        2,
                        0,
                        section -> section >= 3 && section <= 5,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(files.internal(Tracks.motifPath("grey-signal")), (section, t) -> t >= 132 && section != 7)
                .motifAlone(section -> section == 6);
        music.update(5, 133, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the arena: the stems and the motif");
        music.update(6, 140, 0.1f, false);
        assertFalse(music.stemsOn(), "the crawl: the stems silent");
        assertTrue(music.motifOn(), "the motif alone");
        music.update(7, 160, 0.1f, false);
        assertTrue(music.stemsOn(), "the run-out: the stems back");
        assertFalse(music.motifOn());
        music.dispose();
        bank.dispose();
    }
}
