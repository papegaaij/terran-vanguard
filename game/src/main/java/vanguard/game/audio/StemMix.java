package vanguard.game.audio;

/**
 * Two sample-aligned stems of a level theme played in lockstep (design/audio/music, Intensity
 * layers): the base stem and the full mix, which is the intensity layer's "on" state. Both loop
 * at the same points, so they stay aligned forever; the output crossfades linearly between them
 * (the base is part of the full mix, so a linear fade keeps it at a constant level). The mix moves
 * towards its target by a fixed step per frame, so a change never clicks. The target is set from
 * the render thread, the samples are read on the music thread.
 */
public final class StemMix implements PcmStream {
    private final PcmStream base;
    private final PcmStream full;
    /** The mix moves by this much per frame: a whole crossfade takes the fade time. */
    private final float stepPerFrame;
    /** The step of the change under way: {@link #stepPerFrame}, or a fade time of its own ({@link #fadeTo(float, double)}). */
    private volatile float step;

    private volatile float target;
    private float mix;
    /** The stems' samples of the last read; reallocated only when the chunk size changes. */
    private short[] baseChunk = new short[0];

    private short[] fullChunk = new short[0];

    /** M7 part E: the motif layer comes in over this long (design/audio/music, Vorne's motif layer). */
    static final double MOTIF_IN_SECONDS = 2;
    /** M7 part E: the motif layer goes out over this long. */
    static final double MOTIF_OUT_SECONDS = 4;
    /** M7 part E: the motif stem, null for a theme without one. */
    private final PcmStream motif;

    private short[] motifChunk = new short[0];
    /** M7 part E: the motif's level, 0..1, where it goes and its pace per frame in and out. */
    private float motifLevel;

    private volatile float motifTarget;
    private final float motifIn;
    private final float motifOut;

    /**
     * M7 part F: the stems (the base stem and the full mix together) go out over this long for the motif layer alone
     * (design/campaign Level 19's crawl, {@code stems: motif}) and come back as long.
     */
    static final double STEMS_FADE_SECONDS = 2;
    /** M7 part F: the stems' level under the motif, 0..1, where it goes and its pace per frame. */
    private float stemsLevel = 1;

    private volatile float stemsTarget = 1;
    private final float stemsPace;

    /** @param fadeSeconds how long a whole crossfade takes */
    StemMix(PcmStream base, PcmStream full, double fadeSeconds) {
        this(base, full, null, fadeSeconds);
    }

    /**
     * M7 part E: with a motif stem (null for none) added on top of the crossfade, silent until {@link #motif}.
     *
     * @param fadeSeconds how long a whole crossfade takes
     */
    StemMix(PcmStream base, PcmStream full, PcmStream motif, double fadeSeconds) {
        if (base.channels() != full.channels() || base.sampleRate() != full.sampleRate()) {
            throw new IllegalArgumentException("the stems differ in format");
        }
        if (motif != null && (motif.channels() != base.channels() || motif.sampleRate() != base.sampleRate())) {
            throw new IllegalArgumentException("the motif stem differs in format");
        }
        this.base = base;
        this.full = full;
        this.motif = motif;
        stepPerFrame = (float) (1 / (fadeSeconds * base.sampleRate()));
        step = stepPerFrame;
        motifIn = (float) (1 / (MOTIF_IN_SECONDS * base.sampleRate()));
        motifOut = (float) (1 / (MOTIF_OUT_SECONDS * base.sampleRate()));
        stemsPace = (float) (1 / (STEMS_FADE_SECONDS * base.sampleRate()));
    }

    /**
     * The stems of a theme; they must have the same length and loop points, so they stay sample-aligned.
     *
     * @param fullShare where the mix starts: 0 = the base stem alone, 1 = the full mix
     */
    static StemMix of(VorbisFile base, VorbisFile full, double fadeSeconds, float fullShare) {
        return of(base, full, null, fadeSeconds, fullShare, false);
    }

    /**
     * M7 part E: the stems of a theme with its motif stem (null for none), which must be sample-aligned with them
     * too.
     *
     * @param motifOn whether the motif starts at its full level (else silent)
     */
    static StemMix of(
            VorbisFile base, VorbisFile full, VorbisFile motif, double fadeSeconds, float fullShare, boolean motifOn) {
        if (base.frameCount() != full.frameCount() || !base.loopPoints().equals(full.loopPoints())) {
            throw new IllegalArgumentException("the stems are not sample-aligned");
        }
        if (motif != null
                && (motif.frameCount() != base.frameCount()
                        || !motif.loopPoints().equals(base.loopPoints()))) {
            throw new IllegalArgumentException("the motif stem is not sample-aligned");
        }
        StemMix stems = new StemMix(
                LoopingStream.of(base),
                LoopingStream.of(full),
                motif == null ? null : LoopingStream.of(motif),
                fadeSeconds);
        stems.target = fullShare;
        stems.mix = fullShare;
        stems.motifTarget = motifOn ? 1 : 0;
        stems.motifLevel = stems.motifTarget;
        return stems;
    }

    /** M7 part E: whether the theme has a motif stem. */
    public boolean hasMotif() {
        return motif != null;
    }

    /**
     * M7 part E: the motif layer comes in (over {@value #MOTIF_IN_SECONDS} s) or goes out (over {@value
     * #MOTIF_OUT_SECONDS} s); nothing without a motif stem.
     */
    public void motif(boolean on) {
        motifTarget = on ? 1 : 0;
    }

    /**
     * M7 part F: the stems play ({@code on}) or go silent under the motif layer alone (over {@value
     * #STEMS_FADE_SECONDS} s either way); the motif layer is not touched (a stems-only gain, risk 6).
     */
    public void stems(boolean on) {
        stemsTarget = on ? 1 : 0;
    }

    /** M7 part F: the stems' level now, 0..1, for the tests. */
    float stemsLevel() {
        return stemsLevel;
    }

    /** Crossfades towards {@code share} of the full mix: 0 = the base stem alone, 1 = the full mix. */
    public void fadeTo(float share) {
        step = stepPerFrame;
        target = Math.clamp(share, 0, 1);
    }

    /**
     * Crossfades towards {@code share} with a whole crossfade taking {@code fadeSeconds} (M5 part C:
     * a hold zone's full mix fades out over 4 s, design/audio/music); it keeps that pace until the
     * next change.
     */
    public void fadeTo(float share, double fadeSeconds) {
        step = (float) (1 / (fadeSeconds * base.sampleRate()));
        target = Math.clamp(share, 0, 1);
    }

    @Override
    public void read(short[] out) {
        if (baseChunk.length != out.length) {
            baseChunk = new short[out.length];
            fullChunk = new short[out.length];
        }
        short[] b = baseChunk;
        short[] f = fullChunk;
        base.read(b);
        full.read(f);
        short[] m = motifChunk;
        if (motif != null) {
            if (m.length != out.length) {
                m = new short[out.length];
                motifChunk = m;
            }
            // Read even while silent: the motif stays sample-aligned with the other two.
            motif.read(m);
        }
        int channels = channels();
        float goal = target;
        float pace = step;
        float motifGoal = motif == null ? 0 : motifTarget;
        float stemsGoal = stemsTarget;
        for (int frame = 0; frame < out.length / channels; frame++) {
            if (mix < goal) {
                mix = Math.min(goal, mix + pace);
            } else if (mix > goal) {
                mix = Math.max(goal, mix - pace);
            }
            if (motifLevel < motifGoal) {
                motifLevel = Math.min(motifGoal, motifLevel + motifIn);
            } else if (motifLevel > motifGoal) {
                motifLevel = Math.max(motifGoal, motifLevel - motifOut);
            }
            if (stemsLevel < stemsGoal) {
                stemsLevel = Math.min(stemsGoal, stemsLevel + stemsPace);
            } else if (stemsLevel > stemsGoal) {
                stemsLevel = Math.max(stemsGoal, stemsLevel - stemsPace);
            }
            for (int c = 0; c < channels; c++) {
                int i = frame * channels + c;
                float sample = (b[i] + (f[i] - b[i]) * mix) * stemsLevel;
                if (motifLevel > 0) {
                    sample += m[i] * motifLevel;
                }
                out[i] = (short) Math.clamp(Math.round(sample), Short.MIN_VALUE, Short.MAX_VALUE);
            }
        }
    }

    @Override
    public int channels() {
        return base.channels();
    }

    @Override
    public int sampleRate() {
        return base.sampleRate();
    }

    @Override
    public void close() {
        base.close();
        full.close();
        if (motif != null) {
            motif.close();
        }
    }
}
