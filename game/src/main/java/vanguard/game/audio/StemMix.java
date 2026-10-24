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
    private volatile float stemsPace;
    /** M7 part F's default pace of the stems' fade per frame. */
    private final float stemsDefaultPace;

    /**
     * M8 part F (Level 26's {@code stem_times}): the theme's extra stems on the same grid (the dark stem, the warm pad),
     * each played alone at its level, fading to its target at its own pace; written from the render thread before
     * {@link #extrasVersion}, read on the music thread after it.
     */
    private final PcmStream[] extras;

    private final float[] extraLevel;
    private final float[] extraTarget;
    private final float[] extraPace;
    private short[][] extraChunks;
    private volatile int extrasVersion;

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
        this(base, full, motif, new PcmStream[0], fadeSeconds);
    }

    /** M8 part F: with the theme's extra stems ({@link #extra}), silent until asked for. */
    StemMix(PcmStream base, PcmStream full, PcmStream motif, PcmStream[] extras, double fadeSeconds) {
        for (PcmStream extra : extras) {
            if (extra.channels() != base.channels() || extra.sampleRate() != base.sampleRate()) {
                throw new IllegalArgumentException("an extra stem differs in format");
            }
        }
        this.extras = extras.clone();
        extraLevel = new float[extras.length];
        extraTarget = new float[extras.length];
        extraPace = new float[extras.length];
        extraChunks = new short[extras.length][0];
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
        stemsDefaultPace = (float) (1 / (STEMS_FADE_SECONDS * base.sampleRate()));
        stemsPace = stemsDefaultPace;
        java.util.Arrays.fill(extraPace, stemsDefaultPace);
    }

    /** A fade of {@code seconds} as a pace per frame (at once for none). */
    private float pace(double seconds) {
        return seconds > 0 ? (float) (1 / (seconds * base.sampleRate())) : 1;
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
        return of(base, full, motif, new VorbisFile[0], fadeSeconds, fullShare, motifOn);
    }

    /** M8 part F: with the theme's extra stems, which must be sample-aligned with the others too. */
    static StemMix of(
            VorbisFile base,
            VorbisFile full,
            VorbisFile motif,
            VorbisFile[] extras,
            double fadeSeconds,
            float fullShare,
            boolean motifOn) {
        PcmStream[] streams = new PcmStream[extras.length];
        for (int i = 0; i < extras.length; i++) {
            if (extras[i].frameCount() != base.frameCount()
                    || !extras[i].loopPoints().equals(base.loopPoints())) {
                throw new IllegalArgumentException("an extra stem is not sample-aligned");
            }
            streams[i] = LoopingStream.of(extras[i]);
        }
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
                streams,
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
        stems(on, STEMS_FADE_SECONDS);
    }

    /** M8 part F: as {@link #stems(boolean)}, the fade taking {@code seconds} (0: at once). */
    public void stems(boolean on, double seconds) {
        stemsPace = seconds == STEMS_FADE_SECONDS ? stemsDefaultPace : pace(seconds);
        stemsTarget = on ? 1 : 0;
    }

    /** M8 part F: how many extra stems the theme has. */
    public int extras() {
        return extras.length;
    }

    /** M8 part F: extra stem {@code i} fades to {@code level} (0..1) over {@code seconds} (0: at once). */
    public void extra(int i, float level, double seconds) {
        extraTarget[i] = Math.clamp(level, 0, 1);
        extraPace[i] = pace(seconds);
        extrasVersion++;
    }

    /** M8 part F: extra stem {@code i}'s level now, 0..1, for the tests. */
    float extraLevel(int i) {
        return extraLevel[i];
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
        int version = extrasVersion;
        for (int e = 0; e < extras.length; e++) {
            if (extraChunks[e].length != out.length) {
                extraChunks[e] = new short[out.length];
            }
            // Read even while silent: each extra stem stays sample-aligned with the others.
            extras[e].read(extraChunks[e]);
        }
        int channels = channels();
        float goal = target;
        float pace = step;
        float motifGoal = motif == null ? 0 : motifTarget;
        float stemsGoal = stemsTarget;
        float stemsStep = stemsPace;
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
                stemsLevel = Math.min(stemsGoal, stemsLevel + stemsStep);
            } else if (stemsLevel > stemsGoal) {
                stemsLevel = Math.max(stemsGoal, stemsLevel - stemsStep);
            }
            for (int e = 0; e < extras.length && version >= 0; e++) {
                float extraGoal = extraTarget[e];
                if (extraLevel[e] < extraGoal) {
                    extraLevel[e] = Math.min(extraGoal, extraLevel[e] + extraPace[e]);
                } else if (extraLevel[e] > extraGoal) {
                    extraLevel[e] = Math.max(extraGoal, extraLevel[e] - extraPace[e]);
                }
            }
            for (int c = 0; c < channels; c++) {
                int i = frame * channels + c;
                float sample = (b[i] + (f[i] - b[i]) * mix) * stemsLevel;
                if (motifLevel > 0) {
                    sample += m[i] * motifLevel;
                }
                for (int e = 0; e < extras.length; e++) {
                    if (extraLevel[e] > 0) {
                        sample += extraChunks[e][i] * extraLevel[e];
                    }
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
        for (PcmStream extra : extras) {
            extra.close();
        }
    }
}
