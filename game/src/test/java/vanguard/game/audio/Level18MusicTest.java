package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.game.settings.AudioSettings;

/**
 * M7 part E's music and ambience for Level 18 (design/campaign Level 18, Music & ambience; design/audio/music, Vorne's
 * motif layer): track 9 "Grey Signal" with its base and motif stems, sample-aligned; the motif as a third stream on
 * top of the stems' crossfade, in over 2 s and out over 4 s, by its cue (from the Ghost Drones' first sight, off in
 * the eye); the {@code mars-storm} bed. Level 18 is not mapped yet (step E4): the cue is a hand-built {@code motif}.
 */
class Level18MusicTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

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
    void trackNineHasItsThreeStemsSampleAligned() throws IOException {
        assertEquals(Optional.of("grey-signal"), Tracks.name(9));
        assertEquals("music/grey-signal-motif.ogg", Tracks.motifPath("grey-signal"));
        var base = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Tracks.basePath("grey-signal"))));
        var full = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Tracks.path(9))));
        var motif = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Tracks.motifPath("grey-signal"))));
        assertEquals(new LoopPoints(349272, 349272 + 7451136), full.loopPoints(), "the stems' loop (MUS-E)");
        assertEquals(full.loopPoints(), motif.loopPoints());
        assertEquals(full.frameCount(), motif.frameCount());
        // The mix owns the three files and closes them; it refuses stems that are not aligned.
        StemMix mix = StemMix.of(base, full, motif, 1, 0, false);
        assertTrue(mix.hasMotif());
        mix.close();
    }

    @Test
    void theMotifComesInOverTwoSecondsOnTopAndGoesOutOverFour() {
        var mix = new StemMix(
                new Constant((short) 1000, 100), new Constant((short) 3000, 100), new Constant((short) 400, 100), 1);
        short[] out = new short[200];
        mix.read(out);
        assertEquals(1000, out[199], "silent until its cue: the base stem alone");

        mix.motif(true);
        mix.read(out);
        assertEquals(1002, out[0], 1);
        assertEquals(1200, out[99], 2, "half way after a second");
        assertEquals(1400, out[199], "on top of the base stem after 2 s");

        mix.fadeTo(1);
        short[] second = new short[100];
        mix.read(second);
        assertEquals(3400, second[99], "on top of the full mix as well");

        mix.motif(false);
        short[] four = new short[400];
        mix.read(four);
        assertEquals(3200, four[199], 2, "half way out after 2 s");
        assertEquals(3000, four[399], "out after 4 s");
        for (int i = 1; i < four.length; i++) {
            assertTrue(four[i - 1] - four[i] <= 2, "no click at " + i);
        }
    }

    @Test
    void theMotifPlaysFromTheGhostDronesFirstSightAndIsOffInTheEye() {
        LevelData.Music.Motif motif = new LevelData.Music.Motif(69.5, Map.of(4, false));
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
                        section -> section == 5,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(files.internal(Tracks.motifPath("grey-signal")), motif::on);
        music.update(2, 40, 0.1f, false);
        assertFalse(music.motifOn(), "the front: the base stem alone");
        music.update(3, 69, 0.1f, false);
        assertFalse(music.motifOn());
        music.update(3, 69.5, 0.1f, false);
        assertTrue(music.motifOn(), "hint 1: the Ghost Drones' first sight");
        music.update(4, 115, 0.1f, false);
        assertFalse(music.motifOn(), "off in the eye");
        music.update(5, 140, 0.1f, false);
        assertTrue(music.motifOn(), "the back wall: the full mix with the motif");
        music.update(6, 175, 0.1f, false);
        assertTrue(music.motifOn(), "out over the clearing");
        music.dispose();
        bank.dispose();
    }

    @Test
    void theStormBedIsTheRoundThirtySixPick() throws IOException {
        assertEquals(Sfx.AMBIENCE_MARS_STORM, Sfx.ambience("mars-storm"));
        assertEquals("sfx/dust-storm-bed-r36-a.ogg", Sfx.AMBIENCE_MARS_STORM.path(), "round 36, S.5 = a");
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_MARS_STORM.priority());
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.AMBIENCE_MARS_STORM.path())))) {
            assertEquals(16, file.frameCount() / (double) file.sampleRate(), 0.05, "a seamless 16 s loop");
        }
    }
}
