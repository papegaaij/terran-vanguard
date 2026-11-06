package vanguard.game.audio;

/**
 * Turns a track with an intro and a loop section into an endless PCM stream: the intro plays
 * once, then the loop section repeats. At the loop end it seeks back to the loop start inside
 * the same {@link #read} call, so the boundary is sample-exact regardless of buffer sizes.
 */
public final class LoopingStream implements PcmStream {
    private final PcmSource source;
    private final LoopPoints loop;
    private long position;

    public LoopingStream(PcmSource source, LoopPoints loop) {
        this.source = source;
        this.loop = loop;
    }

    /** A track with the loop points of its comments. */
    static LoopingStream of(VorbisFile file) {
        return new LoopingStream(file, file.loopPoints());
    }

    /**
     * M11 part B: a track started at {@code frame} (0 = its start; a frame at or past the loop end starts at the loop
     * start), then on with its loop points.
     */
    static LoopingStream from(VorbisFile file, long frame) {
        LoopingStream stream = of(file);
        long start = frame >= stream.loop.end() ? stream.loop.start() : Math.max(0, frame);
        if (start > 0) {
            file.seek(start);
            stream.position = start;
        }
        return stream;
    }

    @Override
    public void read(short[] out) {
        int channels = source.channels();
        int wanted = out.length / channels;
        int filled = 0;
        while (filled < wanted) {
            int frames = (int) Math.min(wanted - filled, loop.end() - position);
            int decoded = source.read(out, filled * channels, frames);
            if (decoded <= 0) {
                throw new IllegalStateException(
                        "track ended at frame " + position + " before the loop end " + loop.end());
            }
            filled += decoded;
            position += decoded;
            if (position == loop.end()) {
                source.seek(loop.start());
                position = loop.start();
            }
        }
    }

    @Override
    public int channels() {
        return source.channels();
    }

    @Override
    public int sampleRate() {
        return source.sampleRate();
    }

    @Override
    public void close() {
        source.close();
    }
}
