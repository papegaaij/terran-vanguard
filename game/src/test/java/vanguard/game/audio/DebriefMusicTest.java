package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vanguard.game.settings.AudioSettings;

/**
 * Track 29 "Stand Down" under the debrief (round 40 M.a = b): it comes in on the mission complete
 * jingle's downbeat 5.714 s after the jingle starts, at once where the debrief has no jingle, loops
 * with its loop points and fades out when the player leaves the debrief.
 */
class DebriefMusicTest {
    private static final Path TRACK =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve(Tracks.path(Tracks.DEBRIEF));

    private static byte[] ogg;
    private static short[] straight;
    private static LoopPoints loop;

    @BeforeAll
    static void decodeTheWholeTrack() throws IOException {
        ogg = Files.readAllBytes(TRACK);
        try (VorbisFile file = new VorbisFile(ogg)) {
            loop = file.loopPoints();
            straight = new short[Math.toIntExact(file.frameCount() * file.channels())];
            int frame = 0;
            for (int read; (read = file.read(straight, frame * 2, 4096)) > 0; ) {
                frame += read;
            }
            assertEquals(file.frameCount(), frame, "straight decode length");
        }
    }

    @Test
    void theTrackIsStandDownWithItsLoopPoints() {
        assertEquals("music/debrief-theme.ogg", Tracks.path(Tracks.DEBRIEF));
        // 6.43 s of intro, then 68.57 s of loop at 44.1 kHz (design/audio/music, round 40 b).
        assertEquals(new LoopPoints(283_500, 283_500 + 3_024_000), loop);
    }

    @Test
    void itComesInOnTheJinglesDownbeatThreeBarsAt126Bpm() {
        assertEquals(5.714, DebriefMusic.HANDOVER_SECONDS, 0.001);
        assertEquals(252_000, DebriefMusic.leadFrames(44_100, true));
        assertEquals(0, DebriefMusic.leadFrames(44_100, false), "no jingle: at once");
    }

    @Test
    void afterTheJingleItIsSilentToTheDownbeatThenLoopsSampleExact() {
        try (VorbisFile file = new VorbisFile(ogg)) {
            long lead = DebriefMusic.leadFrames(file.sampleRate(), true);
            compare(DebriefMusic.stream(file, true), lead);
        }
    }

    @Test
    void withoutTheJingleItStartsAtOnce() {
        try (VorbisFile file = new VorbisFile(ogg)) {
            compare(DebriefMusic.stream(file, false), 0);
        }
    }

    /** The stream against the straight decode: {@code lead} frames of silence, the intro, then the loop twice. */
    private static void compare(LoopingStream stream, long lead) {
        short[] chunk = new short[2 * 4093]; // deliberately not a divisor of anything
        long total = lead + loop.end() + loop.length() + 50_000;
        for (long frame = 0; frame < total; ) {
            stream.read(chunk);
            for (int i = 0; i < chunk.length / 2; i++, frame++) {
                short left;
                short right;
                if (frame < lead) {
                    left = 0;
                    right = 0;
                } else {
                    int source = Math.toIntExact(LoopingStreamTest.expectedSourceFrame(frame - lead, loop));
                    left = straight[2 * source];
                    right = straight[2 * source + 1];
                }
                if (chunk[2 * i] != left || chunk[2 * i + 1] != right) {
                    throw new AssertionError("output frame " + frame + " differs (lead-in " + lead + ")");
                }
            }
        }
    }

    @Test
    void itFadesOutOverHalfASecondWhenThePlayerLeaves() {
        DebriefMusic music = new DebriefMusic(Optional.empty());
        music.update(1);
        assertFalse(music.leaving());
        assertFalse(music.gone(), "it plays on under the debrief and the act summary");
        assertEquals(DebriefMusic.VOLUME, music.level(), 1e-6);
        music.leave();
        assertTrue(music.leaving());
        music.update(DebriefMusic.FADE_SECONDS / 2);
        assertEquals(DebriefMusic.VOLUME / 2, music.level(), 1e-6);
        music.update(DebriefMusic.FADE_SECONDS / 2);
        assertEquals(0, music.level(), 1e-6);
        assertTrue(music.gone());
        music.close();
    }

    @Test
    void withoutAnAudioDeviceTheDebriefLeavesAtOnce() {
        Mixer mixer = new Mixer(AudioSettings.defaults());
        DebriefMusic music = DebriefMusic.start(new MockAudio(), new Lwjgl3Files(), mixer, true);
        music.leave();
        assertTrue(music.gone(), "no theme plays, so nothing to wait for");
        music.close();
    }
}
