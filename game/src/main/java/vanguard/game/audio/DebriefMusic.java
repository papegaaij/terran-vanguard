package vanguard.game.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import java.util.Optional;

/**
 * Track 29 "Stand Down" under the debrief (design/audio/music, round 40 M.a = b): after the mission
 * complete jingle it comes in on the jingle's downbeat, {@link #HANDOVER_SECONDS} after the jingle
 * starts (step MUS-D's handover, as the round's listening aid has it); where the debrief plays no
 * jingle (Level 13, whose jingle played inside the level) it starts at once. It loops with its
 * {@code LOOPSTART} / {@code LOOPLENGTH} under the debrief and its act summary page, and fades out
 * over {@link #FADE_SECONDS} when the player leaves the debrief, before the next screen's music. It
 * streams on its own thread like the themes; nothing plays without an audio device or the file.
 */
public final class DebriefMusic implements AutoCloseable {
    /** Three bars of the jingle at 126 BPM: the downbeat after its final chord, while it rings out. */
    public static final double HANDOVER_SECONDS = 3 * 4 * 60 / 126.0;
    /** The jingle's level, so the handover sounds as the listening aid (both at their mastered levels). */
    static final float VOLUME = 0.8f;
    /** The fade out on leaving the debrief. */
    public static final float FADE_SECONDS = 0.5f;

    private final Optional<MusicStreamer> streamer;
    private boolean leaving;
    private float fade = 1;

    DebriefMusic(Optional<MusicStreamer> streamer) {
        this.streamer = streamer;
    }

    /**
     * Starts the theme.
     *
     * @param afterJingle whether the debrief plays the mission complete jingle, which the theme follows
     */
    public static DebriefMusic start(Audio audio, Files files, Mixer mixer, boolean afterJingle) {
        FileHandle file = files.internal(Tracks.path(Tracks.DEBRIEF));
        if (!MusicStreamer.available(audio) || !file.exists()) {
            return new DebriefMusic(Optional.empty());
        }
        PcmStream stream = stream(new VorbisFile(file.readBytes()), afterJingle);
        return new DebriefMusic(Optional.of(MusicStreamer.play(audio, stream, VOLUME, mixer)));
    }

    /** The theme from its start after the lead-in, looping with its loop points. */
    static LoopingStream stream(VorbisFile file, boolean afterJingle) {
        LeadIn lead = new LeadIn(file, leadFrames(file.sampleRate(), afterJingle));
        return new LoopingStream(lead, lead.shifted(file.loopPoints()));
    }

    /** The silence before the theme: the handover after the jingle, none without it. */
    static long leadFrames(int sampleRate, boolean afterJingle) {
        return afterJingle ? Math.round(HANDOVER_SECONDS * sampleRate) : 0;
    }

    /** Starts the fade out: the player leaves the debrief. */
    public void leave() {
        leaving = true;
    }

    /** Whether the player is leaving (the fade has started). */
    public boolean leaving() {
        return leaving;
    }

    /** Moves the fade out on. */
    public void update(float seconds) {
        if (!leaving || fade <= 0) {
            return;
        }
        fade = Math.max(0, fade - seconds / FADE_SECONDS);
        streamer.ifPresent(music -> music.setVolume(level()));
    }

    /** The theme's level, 0..1 before the mixer's music gain. */
    float level() {
        return VOLUME * fade;
    }

    /** Whether the debrief can go: it is leaving and the theme has faded out (at once when none plays). */
    public boolean gone() {
        return leaving && (fade <= 0 || streamer.isEmpty());
    }

    @Override
    public void close() {
        streamer.ifPresent(MusicStreamer::close);
    }
}
