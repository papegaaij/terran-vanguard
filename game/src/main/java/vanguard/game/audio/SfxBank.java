package vanguard.game.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.LongSupplier;

/**
 * All {@link Sfx} loaded as OpenAL buffers, carried over from the tech spike. Every play goes
 * through the {@link VoiceLimit}: at most 32 effects at once, each within its instance limit, the
 * lowest-priority oldest voice stolen first (design/audio/sfx, Mixing rules). Every play is scaled
 * by the {@link Mixer}'s gain for the effect's bus; loops follow a change. The desktop launcher
 * raises the OpenAL source count to 64, so the music stream and many effects play at once.
 *
 * <p>Play only from the render thread: libGDX's OpenAL source pool, which the music streamer
 * shares, is not thread-safe (spike gate 5).
 *
 * <p>Startup step: the effects are no longer decoded before the title screen (387 of them, about 1.7 s). With OpenAL
 * a background thread decodes them to PCM ({@link OggPcm}), the menu sounds first; the render thread makes each sound
 * from its PCM in {@link #update} (a few a frame) or at once when it is played first, decoding it itself if the
 * background thread has not got to it, so no effect is ever dropped or late by more than its own decoding. A level
 * start calls {@link #finish}: every effect is ready before the level's first tick. Every OpenAL call stays on the
 * render thread. Without OpenAL (a test's mock audio) every effect is loaded in the constructor, as before.
 */
public final class SfxBank implements Disposable, Mixer.Listener {
    /** A file's length when it cannot be read (a test's mock files): long enough to count as playing. */
    static final long UNKNOWN_NANOS = 1_000_000_000L;

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    /** How long {@link #update} may spend making sounds in a frame. */
    private static final long UPDATE_NANOS = 2_000_000L;

    /** Each effect's sound once made (render thread only); null until then. */
    private final Sound[] sounds = new Sound[Sfx.values().length];
    /** Each effect's decoding: its PCM as a WAV file (null to load the file itself) and its length. */
    private final List<FutureTask<Clip>> clips = new ArrayList<>();
    /** How many effects have their sound, so {@link #update} stops looking once all have. */
    private int made;
    /** The background thread decoding the effects; null when they were loaded in the constructor. */
    private final Thread decoder;

    private volatile boolean closed;
    private final Audio audio;
    private final Files files;
    /** Each effect's length at its recorded pitch. */
    private final long[] lengths = new long[Sfx.values().length];
    /** Each running loop's instance. */
    private final long[] loopIds = new long[Sfx.values().length];
    /** The volume of each running loop before the mixer's gain; negative when it is not looping. */
    private final float[] loopVolumes = new float[Sfx.values().length];

    private final Mixer mixer;
    private final LongSupplier clock;
    private final VoiceLimit voices;

    public SfxBank(Audio audio, Files files, Mixer mixer) {
        this(audio, files, mixer, System::nanoTime, audio instanceof OpenALLwjgl3Audio);
    }

    /** @param clock the time in nanoseconds, for the voices' ends */
    SfxBank(Audio audio, Files files, Mixer mixer, LongSupplier clock) {
        this(audio, files, mixer, clock, false);
    }

    /**
     * @param clock the time in nanoseconds, for the voices' ends
     * @param background decode the effects on a background thread rather than in the constructor
     */
    SfxBank(Audio audio, Files files, Mixer mixer, LongSupplier clock, boolean background) {
        this.audio = audio;
        this.files = files;
        this.mixer = mixer;
        this.clock = clock;
        voices = new VoiceLimit(VoiceLimit.MAX_VOICES, (sfx, id) -> sound(sfx).stop(id));
        Arrays.fill(loopVolumes, -1);
        mixer.listen(this);
        boolean pcm = audio instanceof OpenALLwjgl3Audio;
        for (Sfx sfx : Sfx.values()) {
            clips.add(new FutureTask<>(() -> decode(sfx, pcm)));
        }
        if (background) {
            // The menu's sounds first: the title screen plays them.
            List<FutureTask<Clip>> order = Arrays.stream(Sfx.values())
                    .sorted(Comparator.comparing(sfx -> !sfx.name().startsWith("MENU_")))
                    .map(sfx -> clips.get(sfx.ordinal()))
                    .toList();
            decoder = new Thread(
                    () -> {
                        for (FutureTask<Clip> clip : order) {
                            if (closed) {
                                return;
                            }
                            clip.run();
                        }
                    },
                    "sfx-decoder");
            decoder.setDaemon(true);
            decoder.start();
        } else {
            decoder = null;
            finish();
        }
    }

    /**
     * An effect decoded: its PCM as a WAV file (null: the sound is made from the file; dropped once the sound is made,
     * so the bank does not keep the samples on the heap) and its length.
     */
    private static final class Clip {
        byte[] wav;
        final long nanos;

        Clip(byte[] wav, long nanos) {
            this.wav = wav;
            this.nanos = nanos;
        }
    }

    /** Decodes {@code sfx} (any thread): to PCM when {@code pcm}, and its length. */
    private Clip decode(Sfx sfx, boolean pcm) {
        FileHandle file = files.internal(sfx.path());
        if (!file.exists()) {
            return new Clip(null, UNKNOWN_NANOS);
        }
        byte[] ogg = file.readBytes();
        return new Clip(pcm ? OggPcm.wav(ogg, sfx.path()) : null, oggNanos(ogg));
    }

    /**
     * Makes the sounds of the effects decoded so far, for a little time each frame (render thread); the effects not
     * yet decoded follow in later frames, or when they are played first.
     */
    public void update() {
        if (made == sounds.length) {
            return;
        }
        long until = System.nanoTime() + UPDATE_NANOS;
        for (int i = 0; i < sounds.length && System.nanoTime() < until; i++) {
            if (sounds[i] == null && clips.get(i).isDone()) {
                sound(Sfx.values()[i]);
            }
        }
    }

    /** Makes every effect's sound now, decoding those the background thread has not (render thread): a level start. */
    public void finish() {
        for (Sfx sfx : Sfx.values()) {
            sound(sfx);
        }
    }

    /** Whether every effect has its sound. */
    public boolean finished() {
        return made == sounds.length;
    }

    /** The sound of {@code sfx}, made now if it is not yet (render thread). */
    private Sound sound(Sfx sfx) {
        Sound sound = sounds[sfx.ordinal()];
        if (sound != null) {
            return sound;
        }
        FutureTask<Clip> task = clips.get(sfx.ordinal());
        // Decodes it here unless the background thread has started it; then this waits for it.
        task.run();
        Clip clip;
        try {
            clip = task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while decoding " + sfx.path(), e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("cannot load the effect " + sfx.path(), e.getCause());
        }
        sound = audio.newSound(clip.wav != null ? OggPcm.file(sfx.path(), clip.wav) : files.internal(sfx.path()));
        clip.wav = null;
        lengths[sfx.ordinal()] = clip.nanos;
        sounds[sfx.ordinal()] = sound;
        made++;
        return sound;
    }

    /**
     * Plays {@code sfx} once, unless the voice limit drops it.
     *
     * @param volume 0..1
     * @param pitch 1 for the recorded pitch
     * @param pan -1 (left) to 1 (right)
     */
    public void play(Sfx sfx, float volume, float pitch, float pan) {
        long now = clock.getAsLong();
        if (!voices.admit(sfx, now, false)) {
            return;
        }
        long id = sound(sfx).play(volume * mixer.gain(sfx.bus()), pitch, pan);
        if (id != -1) {
            voices.started(sfx, id, now + (long) (lengths[sfx.ordinal()] / Math.max(0.01f, pitch)));
        }
    }

    /** Loops {@code sfx} until {@link #stop(Sfx)}; a loop always gets a voice and keeps it. */
    public void loop(Sfx sfx, float volume) {
        stop(sfx);
        voices.admit(sfx, clock.getAsLong(), true);
        long id = sound(sfx).loop(volume * mixer.gain(sfx.bus()));
        if (id != -1) {
            voices.started(sfx, id, VoiceLimit.LOOPING);
        }
        loopIds[sfx.ordinal()] = id;
        loopVolumes[sfx.ordinal()] = volume;
    }

    /**
     * M9 part C: loops {@code sfx} at {@code pitch} (1 for the recorded pitch) until {@link #stop(Sfx)}: one file
     * looped at a hull's own pitch (the Ascendancy turbine for the Gunship and the Minelayer).
     */
    public void loop(Sfx sfx, float volume, float pitch) {
        stop(sfx);
        voices.admit(sfx, clock.getAsLong(), true);
        long id = sound(sfx).loop(volume * mixer.gain(sfx.bus()), pitch, 0);
        if (id != -1) {
            voices.started(sfx, id, VoiceLimit.LOOPING);
        }
        loopIds[sfx.ordinal()] = id;
        loopVolumes[sfx.ordinal()] = volume;
    }

    /** Sets a running loop's volume (before the mixer's gain); nothing when it is not looping. */
    public void loopVolume(Sfx sfx, float volume) {
        if (loopVolumes[sfx.ordinal()] < 0) {
            return;
        }
        loopVolumes[sfx.ordinal()] = volume;
        sound(sfx).setVolume(loopIds[sfx.ordinal()], volume * mixer.gain(sfx.bus()));
    }

    /** M7 part C: pans a running loop ({@code pan} -1 left to 1 right) at its volume; nothing when it is not looping. */
    public void loopPan(Sfx sfx, float pan) {
        if (loopVolumes[sfx.ordinal()] < 0) {
            return;
        }
        sound(sfx).setPan(loopIds[sfx.ordinal()], pan, loopVolumes[sfx.ordinal()] * mixer.gain(sfx.bus()));
    }

    /** M9 part D: sets a running loop's pitch (the Buzzsaw's whine with its spin); nothing when it is not looping. */
    public void loopPitch(Sfx sfx, float pitch) {
        if (loopVolumes[sfx.ordinal()] < 0) {
            return;
        }
        sound(sfx).setPitch(loopIds[sfx.ordinal()], pitch);
    }

    /** A running loop's volume before the mixer's gain; negative when it is not looping. */
    float loopVolume(Sfx sfx) {
        return loopVolumes[sfx.ordinal()];
    }

    /** Whether {@code sfx} is looping. */
    public boolean looping(Sfx sfx) {
        return loopVolumes[sfx.ordinal()] >= 0;
    }

    /** Stops every playing instance of {@code sfx}. */
    public void stop(Sfx sfx) {
        Sound sound = sounds[sfx.ordinal()];
        if (sound != null) {
            sound.stop();
        }
        voices.stopped(sfx);
        loopVolumes[sfx.ordinal()] = -1;
    }

    /** How many effects play now (for tests and the voice limit's checks). */
    int playing() {
        return voices.playing(clock.getAsLong());
    }

    /** How many instances of {@code sfx} play now (for tests). */
    int playing(Sfx sfx) {
        return voices.playing(sfx, clock.getAsLong());
    }

    @Override
    public void gainsChanged() {
        for (Sfx sfx : Sfx.values()) {
            float volume = loopVolumes[sfx.ordinal()];
            if (volume >= 0) {
                sound(sfx).setVolume(loopIds[sfx.ordinal()], volume * mixer.gain(sfx.bus()));
            }
        }
    }

    @Override
    public void dispose() {
        mixer.forget(this);
        closed = true;
        if (decoder != null) {
            try {
                // At most the effect it decodes now.
                decoder.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (Sound sound : sounds) {
            if (sound != null) {
                sound.dispose();
            }
        }
    }

    /**
     * An Ogg Vorbis file's length in nanoseconds: the last page's granule position (its last
     * sample) over the sample rate of the identification header; {@link #UNKNOWN_NANOS} when the
     * bytes are not Ogg Vorbis.
     */
    static long oggNanos(byte[] ogg) {
        if (ogg.length < 28 || !page(ogg, 0)) {
            return UNKNOWN_NANOS;
        }
        int packet = 27 + (ogg[26] & 0xFF);
        if (packet + 16 > ogg.length || ogg[packet] != 1 || ogg[packet + 1] != 'v') {
            return UNKNOWN_NANOS;
        }
        long rate = littleEndian(ogg, packet + 12, 4);
        if (rate <= 0) {
            return UNKNOWN_NANOS;
        }
        for (int i = ogg.length - 27; i >= 0; i--) {
            if (page(ogg, i)) {
                long granule = littleEndian(ogg, i + 6, 8);
                if (granule > 0) {
                    return granule * NANOS_PER_SECOND / rate;
                }
            }
        }
        return UNKNOWN_NANOS;
    }

    private static boolean page(byte[] bytes, int at) {
        return bytes[at] == 'O' && bytes[at + 1] == 'g' && bytes[at + 2] == 'g' && bytes[at + 3] == 'S';
    }

    private static long littleEndian(byte[] bytes, int at, int length) {
        long value = 0;
        for (int k = length - 1; k >= 0; k--) {
            value = value << 8 | bytes[at + k] & 0xFF;
        }
        return value;
    }
}
