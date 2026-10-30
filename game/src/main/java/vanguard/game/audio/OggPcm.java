package vanguard.game.audio;

import com.badlogic.gdx.files.FileHandle;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

/**
 * Startup step: decodes an Ogg Vorbis effect to 16-bit PCM in a WAV file in memory, on any thread, with the same
 * stb_vorbis decoder libGDX's OpenAL audio uses for an {@code .ogg} sound; the render thread then makes the sound from
 * the WAV ({@code Audio.newSound} of {@link #file}), which only copies the samples into an OpenAL buffer. So the
 * decoding, the slow part, runs off the render thread, and every OpenAL call stays on it.
 */
final class OggPcm {
    private static final int HEADER = 44;

    private OggPcm() {}

    /** The effect's samples as a 16-bit PCM WAV file. */
    static byte[] wav(byte[] ogg, String name) {
        ByteBuffer encoded = MemoryUtil.memAlloc(ogg.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            encoded.put(ogg).flip();
            IntBuffer channels = stack.mallocInt(1);
            IntBuffer rate = stack.mallocInt(1);
            ShortBuffer samples = STBVorbis.stb_vorbis_decode_memory(encoded, channels, rate);
            if (samples == null) {
                throw new IllegalStateException("cannot decode the Ogg Vorbis effect " + name);
            }
            try {
                return wav(samples, channels.get(0), rate.get(0));
            } finally {
                LibCStdlib.free(samples);
            }
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    /** A WAV file of 16-bit samples (interleaved, {@code channels} of them a frame) at {@code rate}. */
    static byte[] wav(ShortBuffer samples, int channels, int rate) {
        int data = samples.remaining() * 2;
        ByteBuffer wav = ByteBuffer.allocate(HEADER + data).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        wav.put(new byte[] {'R', 'I', 'F', 'F'}).putInt(HEADER - 8 + data);
        wav.put(new byte[] {'W', 'A', 'V', 'E', 'f', 'm', 't', ' '}).putInt(16);
        wav.putShort((short) 1).putShort((short) channels).putInt(rate);
        wav.putInt(rate * channels * 2).putShort((short) (channels * 2)).putShort((short) 16);
        wav.put(new byte[] {'d', 'a', 't', 'a'}).putInt(data);
        wav.asShortBuffer().put(samples.duplicate());
        return wav.array();
    }

    /** A WAV file in memory as a file handle named {@code <name>.wav}, for {@code Audio.newSound}. */
    static FileHandle file(String name, byte[] wav) {
        return new FileHandle(name + ".wav") {
            @Override
            public InputStream read() {
                return new ByteArrayInputStream(wav);
            }

            @Override
            public byte[] readBytes() {
                return wav.clone();
            }

            @Override
            public long length() {
                return wav.length;
            }

            @Override
            public boolean exists() {
                return true;
            }
        };
    }
}
