package vanguard.game.screen;

import java.util.Optional;
import vanguard.game.GameServices;
import vanguard.game.audio.Fanfare;
import vanguard.game.audio.Tracks;

/**
 * M11 part A, step A3b (design/ui/debrief, The campaign end; design/audio/music, track 27): the ending's track, played
 * once from the ending pages on, under the end card and the name entry (silence if they outlast it), and faded out as
 * the credits roll starts ({@link #fadeOut}). The screens it runs under hand it on; the end card's screen closes it. It
 * ducks under the pages' voices as the briefing theme does.
 */
final class EndingMusic implements AutoCloseable {
    static final float VOLUME = 0.8f;

    private final Optional<Fanfare> track;
    private float duck = 1;
    private float fade = 1;
    /** The fade-out's length, s; 0 while it plays on. */
    private float fadeSeconds;

    private boolean closed;

    private EndingMusic(Optional<Fanfare> track) {
        this.track = track;
    }

    /** Starts the ending's track {@code name} ({@code music/<name>.ogg}); silent without a device or the file. */
    static EndingMusic play(GameServices services, String name) {
        return new EndingMusic(
                Fanfare.play(services.audio, services.files.internal(Tracks.path(name)), VOLUME, services.mixer));
    }

    /** No music (a test, a missing file). */
    static EndingMusic none() {
        return new EndingMusic(Optional.empty());
    }

    /** The voice's duck now, 1 for none. */
    void duck(float duck) {
        this.duck = duck;
        apply();
    }

    /** Starts the fade-out over {@code seconds} (the credits roll's start). */
    void fadeOut(float seconds) {
        fadeSeconds = Math.max(seconds, 1e-3f);
    }

    /** Advances the fade by a frame. */
    void update(float seconds) {
        if (fadeSeconds > 0 && fade > 0) {
            fade = Math.max(0, fade - seconds / fadeSeconds);
            apply();
        }
    }

    /** The share of its volume it plays at now (1 until it fades). */
    float fade() {
        return fade;
    }

    private void apply() {
        if (closed) {
            return;
        }
        track.ifPresent(cue -> cue.setVolume(VOLUME * duck * fade));
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            track.ifPresent(Fanfare::close);
        }
    }
}
