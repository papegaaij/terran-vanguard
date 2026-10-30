package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import org.junit.jupiter.api.Test;
import vanguard.game.settings.AudioSettings;

/**
 * Startup step: the sound effects decode on a background thread, so the title screen does not wait for them; an
 * effect played before the thread got to it is made at once, and a level start makes every effect.
 */
class SfxBankLoadingTest {
    @Test
    void anEffectPlayedBeforeItsTurnPlaysAtOnceAndALevelStartMakesEveryEffect() {
        long[] now = {0};
        var bank = new SfxBank(
                new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0], true);
        try {
            Sfx last = Sfx.values()[Sfx.values().length - 1];
            bank.play(last, 1, 1, 0);
            assertEquals(1, bank.playing(last), "played at once, not dropped");

            bank.finish();

            assertTrue(bank.finished(), "every effect has its sound");
        } finally {
            bank.dispose();
        }
    }

    @Test
    void theBankLoadedInTheConstructorHasEveryEffect() {
        var bank = new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> 0);
        try {
            assertTrue(bank.finished());
        } finally {
            bank.dispose();
        }
    }

    @Test
    void thePcmIsAWavFileOfSixteenBitSamples() {
        ShortBuffer samples = ShortBuffer.wrap(new short[] {1, -2, 3, -4});

        byte[] wav = OggPcm.wav(samples, 2, 44_100);

        ByteBuffer file = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals("RIFF", new String(wav, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        assertEquals(wav.length - 8, file.getInt(4));
        assertEquals(1, file.getShort(20), "PCM");
        assertEquals(2, file.getShort(22), "channels");
        assertEquals(44_100, file.getInt(24));
        assertEquals(16, file.getShort(34), "bits");
        assertEquals(8, file.getInt(40), "data bytes");
        assertEquals(-2, file.getShort(46));
        assertEquals(-4, file.getShort(50));
    }
}
