package vanguard.game.audio;

/** An endless stream of interleaved 16-bit PCM samples, read by the {@link MusicStreamer}'s thread. */
public interface PcmStream extends AutoCloseable {
    /** Fills {@code out} completely with interleaved samples; never allocates. */
    void read(short[] out);

    int channels();

    int sampleRate();

    /**
     * M11 part H, step H4: the frame of its track it reads next (a looping track's position in its file, so a stem of
     * the same track can start sample-aligned with it); -1 when it has none (yet).
     */
    default long frame() {
        return -1;
    }

    @Override
    void close();
}
