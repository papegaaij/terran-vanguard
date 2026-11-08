package vanguard.game.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The sound test of the Options Audio tab (design/ui/options): the music tracks and the sound
 * effects the game has, picked with left and right and played on confirm through their buses, so
 * the volume sliders apply. A track plays solo, so the music of the screen below is silent while
 * it plays: a theme loops until confirm stops it or another track starts, a jingle plays once.
 */
public final class SoundTest implements AutoCloseable {
    /** The test's two lists. */
    public enum Kind {
        MUSIC,
        EFFECTS
    }

    /**
     * A music track.
     *
     * @param where where the game plays it
     * @param volume its level before the music volume, as the screen that plays it has it
     * @param layer a sample-aligned stream played with it at full level (null for none): M11 part E, track 17's {@code
     *     choir} stem, whose statement step MUS-EF lifted out of the full mix ({@code hollow-light-motif.ogg})
     */
    record Track(String title, String where, String path, boolean loops, float volume, String layer) {
        Track(String title, String where, String path, boolean loops, float volume) {
            this(title, where, path, loops, volume, null);
        }
    }

    /**
     * The screens' music level (the level's boss cue too), and the debrief's, game over's and the
     * act outro's for their cues.
     */
    private static final float THEME_VOLUME = 0.6f;

    private static final float CUE_VOLUME = 0.8f;
    /** The menu sounds' level. */
    private static final float EFFECT_VOLUME = 0.5f;

    static final List<Track> TRACKS = List.of(
            new Track(
                    "TERRAN VANGUARD",
                    "TITLE THEME: TITLE SCREEN AND MAIN MENU",
                    "music/title-theme.ogg",
                    true,
                    THEME_VOLUME),
            new Track("SITUATION ROOM", "BRIEFING THEME", "music/briefing-theme.ogg", true, THEME_VOLUME),
            new Track("DRY DOCK", "HANGAR THEME", "music/hangar-theme.ogg", true, THEME_VOLUME),
            // The level themes by act (track list 4-17), each as its full mix: the file a level plays at full
            // intensity (LevelScreen's full stream; the -base, -motif and other stems are only layers of it).
            new Track("AFTERBURNER", "LEVEL THEME: ACT 1 A", "music/afterburner.ogg", true, THEME_VOLUME),
            new Track("COALITION RISING", "LEVEL THEME: ACT 1 B", "music/coalition-rising.ogg", true, THEME_VOLUME),
            new Track("HOMEFRONT", "LEVEL THEME: ACT 2 A", "music/homefront.ogg", true, THEME_VOLUME),
            new Track("FIRESTORM", "LEVEL THEME: ACT 2 B", "music/firestorm.ogg", true, THEME_VOLUME),
            new Track("RED DUST RUN", "LEVEL THEME: ACT 3 A", "music/red-dust-run.ogg", true, THEME_VOLUME),
            new Track("GREY SIGNAL", "LEVEL THEME: ACT 3 B", "music/grey-signal.ogg", true, THEME_VOLUME),
            new Track("THERA DEEP", "LEVEL THEME: ACT 4 A", "music/thera-deep.ogg", true, THEME_VOLUME),
            new Track("PRESSURE LINE", "LEVEL THEME: ACT 4 B", "music/pressure-line.ogg", true, THEME_VOLUME),
            new Track("HOLLOW ROCK", "LEVEL THEME: ACT 5 A", "music/hollow-rock.ogg", true, THEME_VOLUME),
            new Track("GILDED CAGE", "LEVEL THEME: ACT 5 B", "music/gilded-cage.ogg", true, THEME_VOLUME),
            new Track("EYE OF THE STORM", "LEVEL THEME: ACT 6 A", "music/eye-of-the-storm.ogg", true, THEME_VOLUME),
            // M10 part D: track 15, Act 6 B (Level 38's theme, Level 37's from the turn).
            new Track("STORM WALL", "LEVEL THEME: ACT 6 B", "music/storm-wall.ogg", true, THEME_VOLUME),
            // M11 part B: tracks 16 and 17, Act 7 A (Level 43) and Act 7 B (Level 44).
            new Track("FAR SHORE", "LEVEL THEME: ACT 7 A", "music/far-shore.ogg", true, THEME_VOLUME),
            // M11 part E: Hollow Light as the user chose it, its full mix with the Choir's statement (its motif
            // stream).
            new Track(
                    "HOLLOW LIGHT",
                    "LEVEL THEME: ACT 7 B",
                    "music/hollow-light.ogg",
                    true,
                    THEME_VOLUME,
                    Tracks.motifPath("hollow-light")),
            new Track("RED ALERT", "CUE: BOSS WARNING", "music/boss-warning.ogg", false, THEME_VOLUME),
            new Track(
                    "THE CHOIR DESCENDS",
                    "BOSS THEME: THE VRELL ACT BOSSES",
                    "music/choir-descends.ogg",
                    true,
                    THEME_VOLUME),
            // M9 part H: track 19, the Ascendancy's boss theme (round 68 QH-3 a), Level 35's boss.
            new Track("IRON SOVEREIGN", "BOSS THEME: THE ASCENDANCY", "music/iron-sovereign.ogg", true, THEME_VOLUME),
            // M11 part F: track 34, Vorne's mid-boss theme at Level 48 (round 86 M.3 a).
            new Track(
                    "VORNE'S LAST STAND",
                    "BOSS THEME: VORNE'S CHIMERA",
                    "music/vorne-last-stand.ogg",
                    true,
                    THEME_VOLUME),
            // M11 part H, step H4: track 20, the Choir Heart's theme at Level 50 (QH-1 a, MUS-H), and its coda from the
            // control's taking into the warp home (the music plan's hook 3).
            new Track("CHOIR HEART", "BOSS THEME: THE CHOIR HEART", "music/final-boss.ogg", true, THEME_VOLUME),
            new Track("CHOIR HEART CODA", "CODA: THE GATE CLOSING", "music/final-boss-coda.ogg", false, CUE_VOLUME),
            new Track("MISSION COMPLETE", "JINGLE: DEBRIEF", "music/mission-complete.ogg", false, CUE_VOLUME),
            new Track("STAND DOWN", "DEBRIEF THEME", "music/debrief-theme.ogg", true, CUE_VOLUME),
            new Track("MISSION FAILED", "STING: MISSION FAILED", "music/mission-failed.ogg", false, CUE_VOLUME),
            new Track("CONTACT HEAVY", "STING: MINI-BOSS", "music/miniboss-sting.ogg", false, CUE_VOLUME),
            new Track("ACT COMPLETE", "FANFARE: ACT OUTRO", "music/act-complete.ogg", false, CUE_VOLUME),
            // M8 part H: track 30, the reveal sting at Level 28's cruisers' rise (round 58 a as a stand-in).
            new Track("BLACK AND GOLD", "STING: THE REVEAL", "music/reveal-sting.ogg", false, CUE_VOLUME),
            // M9 part B: track 31, Rook's return sting at Level 29's rescue (round 60 M.2 a).
            new Track("HORN CALL", "STING: ROOK'S RETURN", "music/rook-return.ogg", false, CUE_VOLUME),
            // M10 part H: track 33, the escape sting at Level 42's ESCAPED (round 72 M.4 a).
            new Track("COLLAPSE", "STING: THE ESCAPE", "music/escape-sting.ogg", false, CUE_VOLUME),
            new Track("GAME OVER", "CUE: GAME OVER", "music/game-over.ogg", false, CUE_VOLUME));

    /** Every sound effect but the music cues, which are tracks here. */
    static final List<Sfx> EFFECTS =
            Arrays.stream(Sfx.values()).filter(sfx -> sfx.bus() != Bus.MUSIC).toList();

    private static final List<String> TITLES = TRACKS.stream().map(Track::title).toList();
    private static final List<String> EFFECT_NAMES =
            EFFECTS.stream().map(sfx -> sfx.name().replace('_', ' ')).toList();

    private final Audio audio;
    private final Files files;
    private final Mixer mixer;
    private final SfxBank sfx;
    private final int[] picked = new int[Kind.values().length];
    /** The track playing, or -1. */
    private int playing = -1;

    private Optional<MusicStreamer> streamer = Optional.empty();
    private Optional<OnceStream> once = Optional.empty();

    public SoundTest(Audio audio, Files files, Mixer mixer, SfxBank sfx) {
        this.audio = audio;
        this.files = files;
        this.mixer = mixer;
        this.sfx = sfx;
    }

    /** The names in a list. */
    public List<String> names(Kind kind) {
        return switch (kind) {
            case MUSIC -> TITLES;
            case EFFECTS -> EFFECT_NAMES;
        };
    }

    /** The index of the entry picked in a list. */
    public int picked(Kind kind) {
        return picked[kind.ordinal()];
    }

    /** What the picked entry is: where the game plays the track, or the volume the effect follows. */
    public String note(Kind kind) {
        return switch (kind) {
            case MUSIC -> TRACKS.get(picked(kind)).where();
            case EFFECTS ->
                EFFECTS.get(picked(kind)).bus() == Bus.RADIO
                        ? "FOLLOWS THE RADIO BLIPS VOLUME"
                        : "FOLLOWS THE EFFECTS VOLUME";
        };
    }

    /** Whether the picked track is the one playing. */
    public boolean playing() {
        return playing == picked(Kind.MUSIC);
    }

    /** Picks the next (+1) or previous (-1) entry of a list, wrapping round; returns whether it moved. */
    public boolean pick(Kind kind, int direction) {
        if (direction == 0) {
            return false;
        }
        picked[kind.ordinal()] =
                Math.floorMod(picked(kind) + direction, names(kind).size());
        return true;
    }

    /**
     * Plays the picked entry: an effect once, a track solo (stopping the one playing), or stops the
     * picked track when it is the one playing. An effect that loops already (the ambience under a
     * paused level) is heard as it is.
     */
    public void play(Kind kind) {
        switch (kind) {
            case EFFECTS -> {
                Sfx effect = EFFECTS.get(picked(kind));
                if (!sfx.looping(effect)) {
                    sfx.play(effect, EFFECT_VOLUME, 1, 0);
                }
            }
            case MUSIC -> {
                boolean again = playing();
                stop();
                if (!again) {
                    start(picked(kind));
                }
            }
        }
    }

    /** Ends a jingle's turn once it has played out; call it every frame. */
    public void update() {
        if (once.filter(OnceStream::ended).isPresent()) {
            stop();
        }
    }

    private void start(int track) {
        playing = track;
        if (!MusicStreamer.available(audio)) {
            return;
        }
        Track chosen = TRACKS.get(track);
        VorbisFile file = new VorbisFile(files.internal(chosen.path()).readBytes());
        PcmStream stream;
        if (chosen.loops() && chosen.layer() != null) {
            // M11 part E: the full mix and its layer in lockstep, both at full level (StemMix at its full share).
            // The base slot gets its own decoder (a file decodes from one cursor); silent at the full share.
            VorbisFile base = new VorbisFile(files.internal(chosen.path()).readBytes());
            VorbisFile layer = new VorbisFile(files.internal(chosen.layer()).readBytes());
            stream = StemMix.of(base, file, layer, 0, 1, true);
        } else if (chosen.loops()) {
            stream = LoopingStream.of(file);
        } else {
            OnceStream cue = new OnceStream(file);
            once = Optional.of(cue);
            stream = cue;
        }
        streamer = Optional.of(MusicStreamer.solo(audio, stream, chosen.volume(), mixer));
    }

    private void stop() {
        streamer.ifPresent(MusicStreamer::close);
        streamer = Optional.empty();
        once = Optional.empty();
        playing = -1;
    }

    @Override
    public void close() {
        stop();
    }
}
