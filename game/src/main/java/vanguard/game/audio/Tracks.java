package vanguard.game.audio;

import java.util.Map;
import java.util.Optional;

/**
 * The music files the game plays, by their number in the track list (design/audio/music): each
 * one's file in {@code assets/music/} (imported by {@code :pipeline:importPlaceholders} until its
 * production render by tools/art/themes.py replaces it). A level names its theme by number and its
 * boss cues by file name ({@code boss_sting: miniboss-sting}); both resolve here.
 */
public final class Tracks {
    /** Track 18, the Vrell boss theme "The Choir Descends": loops under an act boss fight (Level 07). */
    public static final int BOSS_VRELL = 18;
    /** Track 22, the boss warning "Red Alert": three bars at 150 BPM that bridge into the boss track. */
    public static final int BOSS_WARNING = 22;
    /** Track 24, "Act Complete": the fanfare under the first page of an act outro (Level 07's Act 1 outro). */
    public static final int ACT_COMPLETE = 24;
    /** M9 part B: track 31, Rook's return sting "Horn Call" (round 60 M.2 a), a one-shot at Level 29's rescue. */
    public static final int ROOK_RETURN = 31;
    /** Track 29, "Stand Down": loops under the debrief after the mission complete jingle ({@link DebriefMusic}). */
    public static final int DEBRIEF = 29;
    /**
     * M11 part A, step A3b: track 27, "Ending" (round 86 M.3), once under the campaign's ending pages, end card and name
     * entry (design/ui/debrief, The campaign end).
     */
    public static final int ENDING = 27;
    /** M11 part A, step A3b: track 28, "Credits" (round 87 M.2), once under the credits roll, again after 2 s of silence. */
    public static final int CREDITS = 28;
    /** M10 part H: track 33, the escape sting "Collapse" (round 72 M.4 a): cuts the boss music at Level 42's escape. */
    public static final int ESCAPE_STING = 33;
    /** M11 part F: track 34, "Vorne's Last Stand" (round 86 M.3 a): Level 48's mid-boss track, cut at its last words. */
    public static final int VORNE_LAST_STAND = 34;

    /**
     * Where the boss track starts under the warning: track 22 is three bars at 150 BPM (4.8 s); its
     * last beat is silent, so the boss track comes in on the downbeat after it, the warning's tail
     * ringing out under it (design/audio/music, Loops and transitions).
     */
    public static final double BOSS_WARNING_BARS_SECONDS = 4.8;

    /**
     * M10 part F: where a mid-boss's own track starts under its sting (Level 40's duel theme after track 21, the
     * music plan's hook (2)): on the sting's end, 4.6 s.
     */
    public static final double MINIBOSS_STING_SECONDS = 4.6;

    /** The file names (without {@code .ogg}) of the tracks that have one. */
    private static final Map<Integer, String> FILES = Map.ofEntries(
            Map.entry(1, "title-theme"),
            Map.entry(2, "hangar-theme"),
            Map.entry(3, "briefing-theme"),
            Map.entry(4, "afterburner"),
            Map.entry(5, "coalition-rising"),
            Map.entry(6, "homefront"),
            Map.entry(7, "firestorm"),
            Map.entry(8, "red-dust-run"),
            Map.entry(9, "grey-signal"),
            Map.entry(10, "thera-deep"),
            Map.entry(11, "pressure-line"),
            // M9 part B: Act 5 A "Hollow Rock" (Level 29), its base and motif stems beside it.
            Map.entry(12, "hollow-rock"),
            // M9 part D: Act 5 B "Gilded Cage" (Level 31, round 60 M.1 a), its base and motif stems beside it.
            Map.entry(13, "gilded-cage"),
            // M10 part B: Act 6 A "Eye of the Storm" (Level 36), its base and motif stems beside it.
            Map.entry(14, "eye-of-the-storm"),
            // M10 part C: Act 6 B "Storm Wall" (Level 37 from the turn), its base, motif and fall stems beside it.
            Map.entry(15, "storm-wall"),
            // M11 part B: Act 7 A "Far Shore" (Level 43), its base and motif (the Choir's voice) stems beside it.
            Map.entry(16, "far-shore"),
            // M11 part B: Act 7 B "Hollow Light" (Level 44), its base and sparse stems beside it.
            Map.entry(17, "hollow-light"),
            Map.entry(BOSS_VRELL, "choir-descends"),
            // M9 part H: track 19, the Ascendancy's boss theme (round 68 QH-3 a), Level 35's boss track.
            Map.entry(19, "iron-sovereign"),
            Map.entry(21, "miniboss-sting"),
            Map.entry(BOSS_WARNING, "boss-warning"),
            Map.entry(23, "mission-complete"),
            Map.entry(ACT_COMPLETE, "act-complete"),
            // M11 part A, step A3b: the campaign end's tracks, both played once (the ending, the credits roll's).
            Map.entry(ENDING, "ending"),
            Map.entry(CREDITS, "credits"),
            Map.entry(25, "mission-failed"),
            Map.entry(26, "game-over"),
            Map.entry(DEBRIEF, "debrief-theme"),
            // M9 part B: track 31, Rook's return sting (Level 29's Ember launch, the theme ducked -6 dB under it).
            Map.entry(ROOK_RETURN, "rook-return"),
            // M10 part F: track 32, the Honour Guard's duel theme "Three Aces" (round 72 M.2 b), Level 40's mid-boss
            // track after the mini-boss sting.
            Map.entry(32, "duel-theme"),
            // M10 part H: track 33, the escape sting "Collapse" (round 72 M.4 a), cutting the boss music at Level 42's
            // ESCAPED (Sfx.ESCAPE_STING).
            Map.entry(ESCAPE_STING, "escape-sting"),
            // M11 part F: track 34, "Vorne's Last Stand" (round 86 M.3 a), Level 48's mid-boss track after the
            // mini-boss
            // sting, cut at Vorne's last words.
            Map.entry(VORNE_LAST_STAND, "vorne-last-stand"));

    private Tracks() {}

    /** A track's file name without the extension ({@code coalition-rising}), or empty while it has none. */
    public static Optional<String> name(int track) {
        return Optional.ofNullable(FILES.get(track));
    }

    /** A music file's path in the assets by its name: {@code music/<name>.ogg}. */
    public static String path(String name) {
        return "music/" + name + ".ogg";
    }

    /** A track's path in the assets ({@code music/act-complete.ogg}); it must have a file. */
    public static String path(int track) {
        return path(name(track).orElseThrow(() -> new IllegalArgumentException("no file for track " + track + " yet")));
    }

    /** A level theme's base stem ({@code music/coalition-rising-base.ogg}): the theme's own file when it has no stems. */
    public static String basePath(String name) {
        return "music/" + name + "-base.ogg";
    }

    /**
     * M7 part E: a level theme's motif stem ({@code music/grey-signal-motif.ogg}, track 9's Vorne motif), a third
     * stream sample-aligned with its base stem and full mix; only a theme with a motif layer has one.
     */
    public static String motifPath(String name) {
        return "music/" + name + "-motif.ogg";
    }

    /**
     * M8 part F: a level theme's extra stem by its name ({@code music/thera-deep-dark.ogg}, track 10's dark stem; the
     * level's {@code music.stem_times}), sample-aligned with its base stem and full mix.
     */
    public static String stemPath(String name, String stem) {
        return "music/" + name + "-" + stem + ".ogg";
    }
}
