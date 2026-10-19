package vanguard.game.audio;

import java.util.Arrays;

/**
 * A source that starts after some frames of silence: the debrief theme, which comes in on the mission
 * complete jingle's downbeat ({@link DebriefMusic}). The silence is part of the source, so the theme's
 * entry is sample-exact on the stream and its loop points move by the lead-in ({@link #shifted}).
 */
final class LeadIn implements PcmSource {
    private final PcmSource source;
    private final long lead;
    private long position;

    LeadIn(PcmSource source, long lead) {
        if (lead < 0) {
            throw new IllegalArgumentException("negative lead-in " + lead);
        }
        this.source = source;
        this.lead = lead;
    }

    /** The source's loop points on this source's frames. */
    LoopPoints shifted(LoopPoints loop) {
        return new LoopPoints(loop.start() + lead, loop.end() + lead);
    }

    @Override
    public int read(short[] out, int offset, int frames) {
        int channels = source.channels();
        if (position < lead) {
            int quiet = (int) Math.min(frames, lead - position);
            Arrays.fill(out, offset, offset + quiet * channels, (short) 0);
            position += quiet;
            return quiet;
        }
        int decoded = source.read(out, offset, frames);
        position += Math.max(0, decoded);
        return decoded;
    }

    @Override
    public void seek(long frame) {
        position = frame;
        source.seek(Math.max(0, frame - lead));
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
    public long frameCount() {
        return source.frameCount() + lead;
    }

    @Override
    public void close() {
        source.close();
    }
}
