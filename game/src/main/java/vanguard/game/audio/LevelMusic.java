package vanguard.game.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;

/**
 * A level's music cues (design/audio/music): the setting's ambience from the start, the level
 * theme from its start section (at the level's start level through that section, rising to full in
 * two seconds at the next one), a cut when the ship is destroyed and a one-second fade when the
 * level is won. The theme plays as two sample-aligned stems: the base stem alone until the level's
 * full section, where it crossfades in a second to the full mix (Intensity layers); a section can
 * override that either way (Level 03 drops to the base stem under the Leviathan's first pass and
 * forces the full mix for its second), crossfading the same way. While a radio
 * message is shown the music ducks by 4 dB (design/audio, Mix groups). A level may end on its
 * ambience alone: from its {@code ambience_from} time the theme fades out as at a won level
 * (Level 06), and loop a radio line faintly under one section (Level 06's perimeter beacon). M5 part
 * C: a run-time hook ({@code full_on}) plays the full mix while the sortie asks for it whatever the
 * section (Level 09: while a hold zone runs and from the collapse on), fading in over a second as it
 * starts and out over {@value #RUN_TIME_OUT_SECONDS} s after it ends.
 *
 * <p>M5 part D (Level 10): a run-time event may duck the theme ({@link #duck}: the scripted loss,
 * −6 dB for 3 s, no sting), the lower of it and the radio's duck applying; and the ambience may
 * crossfade to another at a section's start ({@link #ambienceChanges}: the megacity to the ocean
 * at section 4 over 4 s).
 *
 * <p>M5 part F (Level 12): the theme's level by section ({@link #sectionLevels}: the base stem at
 * −6 dB in the eye), moving to a section's level over {@value #SECTION_RAMP_SECONDS} s as it starts.
 *
 * <p>M5 part G (Level 13): the mission-complete jingle inside the level ({@link #jingle}: at its time the
 * theme fades out over its fade, then the jingle plays once and the ambience goes on alone; the debrief
 * then starts without it), and the ambience's level by the level time ({@link #ambienceLevels}: the
 * arctic wind 3 dB louder through the whiteout).
 *
 * <p>M5 part H (Level 14): an ambience that dies with the boss ({@link Sfx#afterBoss}: the canopy's
 * hum) fades out over {@value #BOSS_AMBIENCE_OUT_SECONDS} s at the kill while the megacity's own
 * ambience rises over {@value #BOSS_AMBIENCE_IN_SECONDS} s.
 *
 * <p>M7 part E (Level 18): a theme's motif stem ({@link #motif}: track 9's Vorne motif, a third sample-aligned stream
 * added on top of the stems' crossfade) plays while its cue asks for it (the level's {@code music.motif}: from the
 * Ghost Drones' first sight, off in the eye), in over 2 s, out over 4 s ({@link StemMix}).
 *
 * <p>M7 part G (Level 20): ambience layers over the setting's loop ({@link #ambienceLayers}: the basin rumble under
 * the travelling ridge), each looping at its level by the level time (the level's {@code music.ambience_layers}: in
 * and out over their fades), silent outside it. M7 part H (Level 21): while an act boss fights they swell and hold, and
 * the kill cuts them ({@link #layers}); the wind's calm over the settling dust is {@link #calmDb}.
 *
 * <p>M8 part F (Level 26): the theme's timed stem changes ({@link #stemTimes}: the level's {@code music.stem_times}):
 * from a change's time on, eased over its fade, its {@code stems} play (the theme's stems; an extra stem of the theme
 * alone, the stems out, on the same grid through {@link StemMix}'s extras; or none, the ambience alone) at its {@code
 * db}. A change that brings in an extra stem without a {@code db} plays it at its file's level (0 dB): each stem is
 * levelled in its file (the warm pad after the dark stem's −6 dB thinning). And a radio line with its own duck ({@link
 * #radioDuck}: Level 26's Choir, −6 dB) ducks the theme by that much instead of the radio's −4 dB.
 *
 * <p>An act boss (Level 07) has its own music: at its arrival the theme crossfades out over 0.5 s
 * while the boss warning (track 22) starts, and the boss track (track 18) comes in on the downbeat
 * after the warning's bars ({@link BossCue}); at the kill it fades out in a second, leaving the
 * ambience, and the theme does not come back. A Retry from boss holds the theme until the boss
 * arrives again. A level without boss music keeps the mid-boss sting ({@link #sting}).
 *
 * <p>M8 part H (Level 28, Music &amp; ambience): a motif-alone section plays the motif as its cue says (the stems silent
 * from the theme's first sample: the hatchery's bed alone before t=16); the boss music drops away while a coiled boss
 * dives ({@link #bossDrop}: out over {@value #BOSS_DROP_OUT_SECONDS} s with the coil's slide, back over {@value
 * #BOSS_DROP_IN_SECONDS} s after the lunge); after the kill the theme comes back as its motif stem alone for the run
 * ({@link #motifAfterBoss}, the level's {@code music.motif_after_boss}); the reveal sting (track 30) plays once over the
 * ambience at the cruisers' rise ({@link #reveal}).
 */
public final class LevelMusic implements Disposable {
    private static final float MUSIC_VOLUME = 0.6f;
    /** The ambience beds' level: round 59 Q.1 a, +6 dB over the old 0.35 (design/audio/sfx). */
    static final float AMBIENCE_VOLUME = 0.70f;

    private static final float FADE_SECONDS = 1;
    /** The intensity layer fades in over a second (design/audio/music, Intensity layers). */
    private static final double CROSSFADE_SECONDS = 1;
    /** M5 part C: the run-time hook's full mix fades back to the base stem over this long (design/audio/music). */
    static final double RUN_TIME_OUT_SECONDS = 4;
    /** The start level rises to full over this long. */
    private static final float RISE_SECONDS = 2;
    /** −4 dB. */
    public static final float DUCKED = (float) Math.pow(10, -4 / 20.0);
    /** The duck moves this much of the way per second, so it does not click. */
    public static final float DUCK_RATE = 4;

    /**
     * A bed's loop volume at {@code scale} of the ambience's own level, clamped at 1: since the +6 dB of round 59 a
     * layer at full under an act boss's swell (+4 dB, Level 21's basin rumble) would otherwise ask for more than full.
     */
    static float bed(float scale) {
        return Math.min(1, AMBIENCE_VOLUME * scale);
    }

    private final Audio audio;
    private final Mixer mixer;
    private final FileHandle base;
    private final FileHandle full;
    private final SfxBank sfx;
    private final Sfx ambience;
    private final int startSection;
    private final float startLevel;
    private final IntPredicate fullMix;
    private Optional<MusicStreamer> music = Optional.empty();
    private Optional<StemMix> stems = Optional.empty();
    /** The full mix's share the stems were last sent to. */
    private float stemTarget;
    /** Whether the run-time hook asked for the full mix at the last update. */
    private boolean intense;

    private boolean cut;
    private float fade = -1;
    private float duck = 1;
    private float rise;
    /** Seconds left of a sting over the theme; negative without one. */
    private float sting = -1;
    /** A sting's length, its 0.5 s crossfades included (track 21 is 4.6 s). */
    private static final float STING_SECONDS = 4.6f;

    private static final float STING_FADE = 0.5f;

    /** A radio line looping under a section: its file, the section and its gain below full. */
    public record VoiceLoop(FileHandle file, int section, float gain) {}

    /**
     * An act boss's music: the warning played once at its arrival (track 22), the boss track looping
     * from {@code trackFromSeconds} into it (track 18); either may be missing.
     */
    public record Boss(Optional<FileHandle> warning, Optional<FileHandle> track, double trackFromSeconds) {
        public Boss {
            if (warning.isEmpty() && track.isEmpty()) {
                throw new IllegalArgumentException("boss music has a warning or a track");
            }
        }
    }

    /** Where the boss music stands. */
    enum BossState {
        /** Before the boss: the theme plays as its sections ask. */
        WAITING,
        /** A Retry from boss restarted the level just before the boss: the theme waits for it. */
        HELD,
        /** The boss's warning and track play; the theme is out. */
        FIGHT,
        /** The boss is down: its track has faded, only the ambience plays. */
        DOWN
    }

    /** The theme crossfades out under the boss warning over this long (design/audio/music). */
    private static final float BOSS_CROSSFADE = 0.5f;

    private final Optional<Boss> boss;
    private BossState bossState = BossState.WAITING;
    private Optional<MusicStreamer> bossMusic = Optional.empty();
    /** Seconds left of the theme's crossfade out under the warning; negative when not fading. */
    private float themeOut = -1;
    /** Seconds left of the boss music's fade at the kill; negative when not fading. */
    private float bossOut = -1;

    private final double ambienceFrom;
    private final Optional<VoiceLoop> voiceLoop;

    /** M5 part D: at section {@code section}'s start (1-based) the ambience crossfades to {@code ambience} over {@code seconds}. */
    public record AmbienceChange(int section, Sfx ambience, double seconds) {}

    private List<AmbienceChange> ambienceChanges = List.of();
    /** The changes made so far, in order. */
    private int changesMade;
    /** The ambience looping at full now (or fading in). */
    private Sfx currentAmbience;
    /** The ambience fading out under a crossfade; null when none. */
    private Sfx fadingAmbience;
    /** The running crossfade's length and the seconds left of it. */
    private double crossfade;

    private double crossfadeLeft;
    /**
     * M5 part H: the fading ambience's own fade-out, s, when it is shorter than the crossfade (Level 14's
     * canopy hum at the Spire's death); NaN for the plain crossfade.
     */
    private double fadeOutSeconds = Double.NaN;
    /** M5 part H: the canopy's hum dies over this long at the boss's death (design/audio/sfx, Level 14). */
    static final double BOSS_AMBIENCE_OUT_SECONDS = 2;
    /** M5 part H: the megacity's own ambience rises over this long after it, under the canopy's withering. */
    static final double BOSS_AMBIENCE_IN_SECONDS = 7;
    /** M5 part D: a run-time duck's level (linear) and the seconds left of it; 0 left for none. */
    private float eventDuck = 1;

    private double eventDuckLeft;
    /** M5 part F: a section's level ramps in over this long (design/audio/music, {@code section_db}). */
    static final double SECTION_RAMP_SECONDS = 1;
    /** M5 part F: the theme's level by section (1-based), dB; 0 (full) everywhere by default. */
    private IntToDoubleFunction sectionDb = section -> 0;
    /** M5 part F: the section level now (linear), where its ramp started, where it goes, the ramp's seconds left. */
    private float sectionGain = 1;

    private float sectionFrom = 1;
    private float sectionTarget = 1;
    private double sectionLeft;
    /** M5 part G: the ambience's level by the level time, dB; 0 everywhere by default. */
    private DoubleUnaryOperator ambienceDb = t -> 0;
    /** M5 part G: the ambience's level (linear) at the last update. */
    private float ambienceGain = 1;
    /** M5 part G: the mission-complete jingle's level, the debrief's. */
    static final float JINGLE_VOLUME = 0.8f;
    /** M5 part G: the level time the jingle's fade starts, NaN for none; the theme's fade before it, s. */
    private double jingleAt = Double.NaN;

    private float jingleFade = FADE_SECONDS;
    /** M5 part G: whether the jingle has played in this attempt. */
    private boolean jinglePlayed;
    /** The running fade's length, s: {@link #FADE_SECONDS}, or the jingle's fade. */
    private float fadeLength = FADE_SECONDS;

    /** M8 part H: the boss music drops away over this long as a coiled boss dives (its 2 s slide into the dark)... */
    static final double BOSS_DROP_OUT_SECONDS = 2;
    /** ... and comes back over this long after the lunge. */
    static final double BOSS_DROP_IN_SECONDS = 2;
    /** M8 part H: whether the boss music is asked to drop away now, and its level under the drop (linear, 1 none). */
    private boolean bossDropped;

    private float bossDropGain = 1;
    /** M8 part H: the theme's motif stem alone after the act boss's kill (the level's {@code music.motif_after_boss}). */
    private vanguard.content.LevelData.Music.MotifAfterBoss afterBoss;
    /** M8 part H: whether the theme plays as the motif after the boss now, and its level by the level time. */
    private boolean afterMode;

    private float afterLevel;
    /** M8 part H: whether the reveal sting has played in this attempt. */
    private boolean revealed;

    private Sound loopSound;
    private long loopId = -1;

    /** M7 part E: whether the motif layer plays at a section (1-based) and level time (the level's {@code music.motif}). */
    @FunctionalInterface
    public interface MotifCue {
        boolean on(int section, double levelSeconds);
    }

    /** M7 part E: the theme's motif stem, null without one, and its cue. */
    private FileHandle motifFile;

    private MotifCue motifCue = (section, t) -> false;
    /** M7 part E: whether the cue asked for the motif at the last update. */
    private boolean motifOn;
    /**
     * M7 part F: whether a section (1-based) plays the motif layer alone, the stems silent (the level's {@code stems:
     * motif}, Level 19's crawl).
     */
    private IntPredicate motifAlone = section -> false;

    /**
     * @param base the theme's base stem
     * @param full the theme's full mix, sample-aligned with the base stem
     * @param startSection the section the theme starts in
     * @param startDb the theme's level through its start section, dB (0 for full)
     * @param fullMix whether a section (1-based) plays the full mix rather than the base stem
     */
    public LevelMusic(
            Audio audio,
            Mixer mixer,
            FileHandle base,
            FileHandle full,
            SfxBank sfx,
            Sfx ambience,
            int startSection,
            double startDb,
            IntPredicate fullMix,
            double ambienceFrom,
            Optional<VoiceLoop> voiceLoop) {
        this(
                audio,
                mixer,
                base,
                full,
                sfx,
                ambience,
                startSection,
                startDb,
                fullMix,
                ambienceFrom,
                voiceLoop,
                Optional.empty());
    }

    /** @param boss an act boss's music, empty for none (a mid-boss's sting plays through {@link #sting}) */
    public LevelMusic(
            Audio audio,
            Mixer mixer,
            FileHandle base,
            FileHandle full,
            SfxBank sfx,
            Sfx ambience,
            int startSection,
            double startDb,
            IntPredicate fullMix,
            double ambienceFrom,
            Optional<VoiceLoop> voiceLoop,
            Optional<Boss> boss) {
        this.boss = boss;
        this.audio = audio;
        this.mixer = mixer;
        this.base = base;
        this.full = full;
        this.sfx = sfx;
        this.ambience = ambience;
        this.startSection = startSection;
        startLevel = (float) Math.pow(10, startDb / 20);
        rise = startLevel;
        this.fullMix = fullMix;
        this.ambienceFrom = ambienceFrom;
        this.voiceLoop = voiceLoop;
        currentAmbience = ambience;
        sfx.loop(ambience, bed(1));
    }

    /** M5 part D: the level's ambience changes by section, in section order (Level 10's ocean). */
    public LevelMusic ambienceChanges(List<AmbienceChange> changes) {
        ambienceChanges = List.copyOf(changes);
        return this;
    }

    /** M5 part F: the theme's level by section (1-based), dB (the level's {@code music.section_db}). */
    public LevelMusic sectionLevels(IntToDoubleFunction db) {
        sectionDb = db;
        return this;
    }

    /**
     * M5 part G: the mission-complete jingle inside the level (the level's {@code music.jingle}): at level
     * time {@code t} the theme fades out over {@code fade} s, then the jingle plays once and only the
     * ambience goes on; the theme does not come back in this attempt.
     */
    public LevelMusic jingle(double t, double fade) {
        jingleAt = t;
        jingleFade = (float) fade;
        return this;
    }

    /**
     * M7 part E: the theme's motif stem ({@code file}, sample-aligned with the base stem and the full mix; ignored when
     * it does not exist) and when it plays.
     */
    public LevelMusic motif(FileHandle file, MotifCue cue) {
        motifFile = file;
        motifCue = cue;
        return this;
    }

    /**
     * M7 part F: the sections that play the motif layer alone over the ambience (the stems faded out under it, the
     * motif on whatever its cue).
     */
    public LevelMusic motifAlone(IntPredicate alone) {
        motifAlone = alone;
        return this;
    }

    /**
     * M8 part H: after the act boss's kill the theme comes back as its motif stem alone at {@code motif}'s level by the
     * level time (null for none: the theme stays out after its boss).
     */
    public LevelMusic motifAfterBoss(vanguard.content.LevelData.Music.MotifAfterBoss motif) {
        afterBoss = motif;
        return this;
    }

    /**
     * M8 part H (design/enemies/bosses/abyssal-maw, Music: "silence under the dive's darkness except the ambience
     * bed"): while {@code dropped} the boss music falls away over {@value #BOSS_DROP_OUT_SECONDS} s, after it comes back
     * over {@value #BOSS_DROP_IN_SECONDS} s.
     */
    public void bossDrop(boolean dropped) {
        bossDropped = dropped;
    }

    /** M8 part H: the boss music's level under the dive's drop now, linear (1 none), for the tests. */
    float bossDropGain() {
        return bossDropGain;
    }

    /** M8 part H: whether the theme plays as the motif after the boss now, for the tests. */
    boolean afterMode() {
        return afterMode;
    }

    /** M8 part H: the motif's level after the boss now, linear, for the tests. */
    float afterLevel() {
        return afterLevel;
    }

    /**
     * M8 part H (design/campaign Level 28: the reveal sting, track 30, at the cruisers' rise): the sting plays once over
     * the ambience (an attempt's first rise; the music cut after a lost ship plays none).
     */
    public void reveal() {
        if (revealed || cut) {
            return;
        }
        revealed = true;
        sfx.play(Sfx.REVEAL_STING, JINGLE_VOLUME, 1, 0);
    }

    /** M8 part H: whether the reveal sting has played in this attempt, for the tests. */
    boolean revealed() {
        return revealed;
    }

    /** M7 part F: whether the stems were asked to play at the last update (not the motif alone), for the tests. */
    boolean stemsOn() {
        return stemsOn;
    }

    private boolean stemsOn = true;

    /** M8 part F: the level's timed stem changes, in time order; none by default. */
    private List<vanguard.content.LevelData.Music.StemTime> stemTimes = List.of();
    /** M8 part F: the theme's extra stems the changes name, in order of first use, and their files. */
    private List<String> extraNames = List.of();

    private List<FileHandle> extraFiles = List.of();
    /** M8 part F: the last change applied to the stream (-1: none yet, the sections' stems). */
    private int stemTime = -1;
    /** M8 part F: what the changes ask for now: the stems on, a full-mix share (-1: the section's), the extra (-1). */
    private boolean timedStemsOn = true;

    private float timedShare = -1;
    private int timedExtra = -1;
    /** M8 part F: the stems' fade of the last change, s (the default 2 s before any). */
    private double timedFade = StemMix.STEMS_FADE_SECONDS;
    /** M8 part F: the changes' level now (linear), where its ramp started, where it goes, its length and what is left. */
    private float timedGain = 1;

    private float timedFrom = 1;
    private float timedTarget = 1;
    private double timedLength;
    private double timedLeft;
    /** M8 part F: the radio line's own duck now, dB (0: the radio's usual {@link #DUCKED}). */
    private double radioDuckDb;

    /**
     * M8 part F: the theme's timed stem changes (the level's {@code music.stem_times}); {@code extraFile} gives an extra
     * stem's file by its name ({@code <theme>-<name>.ogg}); a stem whose file does not exist stays silent.
     */
    public LevelMusic stemTimes(
            List<vanguard.content.LevelData.Music.StemTime> times,
            java.util.function.Function<String, FileHandle> extraFile) {
        stemTimes = List.copyOf(times);
        List<String> names = new java.util.ArrayList<>();
        List<FileHandle> files = new java.util.ArrayList<>();
        for (var time : stemTimes) {
            String name = time.stems().orElse("");
            if (time.extraStem()
                    && !names.contains(name)
                    && extraFile.apply(name).exists()) {
                names.add(name);
                files.add(extraFile.apply(name));
            }
        }
        extraNames = List.copyOf(names);
        extraFiles = List.copyOf(files);
        return this;
    }

    /** M8 part F: the extra stems' files, decoded, in {@link #extraNames}' order. */
    private VorbisFile[] extraStems() {
        VorbisFile[] files = new VorbisFile[extraFiles.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = new VorbisFile(extraFiles.get(i).readBytes());
        }
        return files;
    }

    /**
     * M8 part F: the radio line on the air now asks the music to duck by {@code db} (positive; 0: the radio's usual
     * duck), Level 26's Choir 6 dB.
     */
    public void radioDuck(double db) {
        radioDuckDb = db;
    }

    /** M8 part F: the timed changes' level now, linear, for the tests. */
    float timedGain() {
        return timedGain;
    }

    /** M8 part F: the extra stem the changes play now (-1: none), for the tests. */
    int timedExtra() {
        return timedExtra;
    }

    /** M8 part F: whether the changes have the theme's stems play now, for the tests. */
    boolean timedStemsOn() {
        return timedStemsOn;
    }

    /**
     * M8 part F: the last change due by {@code levelSeconds}; when it is a new one, the state it asks for (a jump over
     * several, or a new stream, at once) goes to the stems.
     */
    private void stemTimes(double levelSeconds, float seconds) {
        int due = -1;
        while (due + 1 < stemTimes.size() && stemTimes.get(due + 1).t() <= levelSeconds) {
            due++;
        }
        if (due != stemTime) {
            boolean step = due == stemTime + 1;
            stemTime = due;
            applyStemTime(due, step);
        }
        timedLeft = Math.max(0, timedLeft - seconds);
        timedGain = timedLength <= 0
                ? timedTarget
                : (float) (timedTarget + (timedFrom - timedTarget) * (timedLeft / timedLength));
    }

    /** Applies the state change {@code due} asks for (-1: back to the sections' stems), eased when {@code eased}. */
    private void applyStemTime(int due, boolean eased) {
        String stemsName = null;
        double db = 0;
        for (int i = 0; i <= due; i++) {
            var time = stemTimes.get(i);
            if (time.stems().isPresent()) {
                stemsName = time.stems().get();
                if (time.extraStem()) {
                    db = 0;
                }
            }
            if (time.db().isPresent()) {
                db = time.db().get();
            }
        }
        double fade = due >= 0 && eased ? stemTimes.get(due).fade() : 0;
        timedFade = due >= 0 ? fade : StemMix.STEMS_FADE_SECONDS;
        timedStemsOn = stemsName == null || stemsName.equals("full") || stemsName.equals("base");
        timedShare = "full".equals(stemsName) ? 1 : "base".equals(stemsName) ? 0 : -1;
        timedExtra = stemsName == null ? -1 : extraNames.indexOf(stemsName);
        float target = (float) Math.pow(10, db / 20);
        timedFrom = fade > 0 ? timedGain : target;
        timedTarget = target;
        timedLength = fade;
        timedLeft = fade;
        if (stems.isPresent()) {
            StemMix mix = stems.get();
            for (int e = 0; e < mix.extras(); e++) {
                mix.extra(e, e == timedExtra ? 1 : 0, fade);
            }
        }
    }

    /** M7 part E: whether the motif layer was asked for at the last update, for the tests. */
    boolean motifOn() {
        return motifOn;
    }

    /** M5 part G: the ambience's level by the level time, dB (Level 13's whiteout peak, +3). */
    public LevelMusic ambienceLevels(DoubleUnaryOperator db) {
        ambienceDb = db;
        return this;
    }

    /** M7 part G: an ambience layer over the setting's loop: its sound and its level (0–1) by the level time. */
    public record AmbienceLayer(Sfx sound, DoubleUnaryOperator level) {}

    /** M7 part G: the level's ambience layers ({@code music.ambience_layers}), and their levels at the last update. */
    private List<AmbienceLayer> layers = List.of();

    private float[] layerLevels = new float[0];

    /** M7 part G: the level's ambience layers (Level 20's basin rumble), each looping at its level by the level time. */
    public LevelMusic ambienceLayers(List<AmbienceLayer> layers) {
        this.layers = List.copyOf(layers);
        layerLevels = new float[layers.size()];
        return this;
    }

    /** M7 part G: layer {@code i}'s level now (0–1), for the tests. */
    float layerLevel(int i) {
        return layerLevels[i];
    }

    /**
     * M7 part H (Level 21): while an act boss fights, the ambience layers swell by {@value #BOSS_LAYER_SWELL_DB} dB over
     * {@value #BOSS_LAYER_SWELL_SECONDS} s (the basin rumble at the arena) and do not fall with the level time (a
     * crawling arena holds its clock at its end, where the rumble's fade-out lies); at the kill they follow the level
     * time again at once: the kill's jump to the arena's end cuts the rumble with the death chain.
     */
    static final double BOSS_LAYER_SWELL_DB = 4;

    static final double BOSS_LAYER_SWELL_SECONDS = 3;
    private static final float BOSS_LAYER_SWELL = (float) Math.pow(10, BOSS_LAYER_SWELL_DB / 20);
    /** M7 part H: the layers' swell now, linear (1 outside an act boss's fight). */
    private float layerSwell = 1;

    /** M7 part H: the layers' swell now, linear, for the tests. */
    float layerSwell() {
        return layerSwell;
    }

    /**
     * M7 part H (Level 21): the wind's calm over the settling dust: from level time {@code from} (a crawling arena's
     * end, where the kill's jump puts the clock) the setting's ambience falls by {@value #CALM_DB} dB over {@value
     * #CALM_SECONDS} s (smoothstep) and stays down; 0 before it and for a level without one ({@code from} NaN).
     */
    public static double calmDb(double levelSeconds, double from) {
        if (!(levelSeconds >= from)) {
            return 0;
        }
        double s = Math.min(1, (levelSeconds - from) / CALM_SECONDS);
        return CALM_DB * s * s * (3 - 2 * s);
    }

    static final double CALM_DB = -9;
    static final double CALM_SECONDS = 4;

    /**
     * M7 part G: each ambience layer follows its level by the level time: looping while above 0, stopped at 0; M7 part
     * H: held and swelled while an act boss fights.
     */
    private void layers(double levelSeconds, float seconds) {
        boolean fight = bossState == BossState.FIGHT;
        float swellBefore = layerSwell;
        layerSwell = fight
                ? (float) Math.min(
                        BOSS_LAYER_SWELL, layerSwell + seconds * (BOSS_LAYER_SWELL - 1) / BOSS_LAYER_SWELL_SECONDS)
                : 1;
        for (int i = 0; i < layers.size(); i++) {
            AmbienceLayer layer = layers.get(i);
            float level = (float) Math.clamp(layer.level().applyAsDouble(levelSeconds), 0, 1);
            if (fight) {
                level = Math.max(level, layerLevels[i]);
            }
            if (level == layerLevels[i] && layerSwell == swellBefore) {
                continue;
            }
            layerLevels[i] = level;
            if (level <= 0) {
                if (sfx.looping(layer.sound())) {
                    sfx.stop(layer.sound());
                }
            } else if (!sfx.looping(layer.sound())) {
                sfx.loop(layer.sound(), bed(level * layerSwell));
            } else {
                sfx.loopVolume(layer.sound(), bed(level * layerSwell));
            }
        }
    }

    /** M5 part G: the ambience's level now, linear (1 its own), for the tests. */
    float ambienceGain() {
        return ambienceGain;
    }

    /** M5 part G: whether the jingle has played in this attempt, for the tests. */
    boolean jinglePlayed() {
        return jinglePlayed;
    }

    /** M5 part F: the section level now, linear (1 full), for the tests. */
    float sectionGain() {
        return sectionGain;
    }

    /** M5 part F: the section level moves toward {@code section}'s over {@value #SECTION_RAMP_SECONDS} s. */
    private void sectionLevel(int section, float seconds) {
        float target = (float) Math.pow(10, sectionDb.applyAsDouble(section) / 20);
        if (target != sectionTarget) {
            sectionFrom = sectionGain;
            sectionTarget = target;
            sectionLeft = SECTION_RAMP_SECONDS;
        }
        sectionLeft = Math.max(0, sectionLeft - seconds);
        sectionGain = (float) (sectionTarget + (sectionFrom - sectionTarget) * (sectionLeft / SECTION_RAMP_SECONDS));
    }

    /**
     * M5 part D: a run-time event ducks the theme by {@code db} (below 0) for {@code seconds} (Level
     * 10's scripted loss: −6 dB for 3 s, no sting); with the radio's duck the lower applies.
     */
    public void duck(double db, double seconds) {
        eventDuck = (float) Math.pow(10, db / 20);
        eventDuckLeft = seconds;
    }

    /**
     * The level the duck moves to: the radio's {@link #DUCKED} while a message is shown, a run-time
     * duck's {@code event} while it lasts (1 for none); the lower of the two, they do not add up.
     */
    static float duckTarget(boolean radio, float event) {
        return duckTarget(radio, DUCKED, event);
    }

    /** M8 part F: as above with the radio line's own duck level {@code radioLevel} (linear). */
    static float duckTarget(boolean radio, float radioLevel, float event) {
        return Math.min(radio ? radioLevel : 1, event);
    }

    /** The ambience looping at full now (or fading in), for the tests. */
    Sfx ambience() {
        return currentAmbience;
    }

    /**
     * The ambience changes due by {@code section} start their crossfade (a section skipped starts the
     * last one's at once), and the running crossfade moves on by {@code seconds}: the new loop rises
     * from silence as the old one falls, the old one stopped at its end.
     */
    private void ambience(int section, float seconds) {
        while (changesMade < ambienceChanges.size()
                && section >= ambienceChanges.get(changesMade).section()) {
            AmbienceChange change = ambienceChanges.get(changesMade++);
            if (change.ambience() == currentAmbience) {
                continue;
            }
            swapAmbience(change.ambience(), change.seconds(), Double.NaN);
        }
        if (fadingAmbience == null) {
            return;
        }
        crossfadeLeft = Math.max(0, crossfadeLeft - seconds);
        float in = (float) (1 - crossfadeLeft / crossfade);
        float out = Double.isNaN(fadeOutSeconds)
                ? 1 - in
                : (float) Math.max(0, 1 - (crossfade - crossfadeLeft) / fadeOutSeconds);
        sfx.loopVolume(currentAmbience, bed(ambienceGain * in));
        sfx.loopVolume(fadingAmbience, bed(ambienceGain * out));
        if (crossfadeLeft == 0) {
            sfx.stop(fadingAmbience);
            fadingAmbience = null;
            fadeOutSeconds = Double.NaN;
        }
    }

    /**
     * The ambience starts its crossfade to {@code next}: the new loop rises from silence over {@code
     * seconds} as the old one falls, over the same time or (M5 part H) over its own {@code outSeconds}.
     */
    private void swapAmbience(Sfx next, double seconds, double outSeconds) {
        if (fadingAmbience != null) {
            sfx.stop(fadingAmbience);
        }
        fadingAmbience = currentAmbience;
        currentAmbience = next;
        sfx.loop(currentAmbience, 0);
        crossfade = Math.max(1e-3, seconds);
        crossfadeLeft = crossfade;
        fadeOutSeconds = outSeconds;
    }

    /** M5 part H: the ambience fading out under a crossfade, null for none, for the tests. */
    Sfx fadingAmbience() {
        return fadingAmbience;
    }

    /**
     * Starts the theme once the scroll reaches its section, raises it to full after that section,
     * crossfades between the base stem and the full mix as the sections ask, and runs the fade and
     * the duck.
     *
     * @param levelSeconds the level time: from {@code ambience_from} on only the ambience plays
     * @param radio whether a radio message is shown
     */
    public void update(int section, double levelSeconds, float seconds, boolean radio) {
        update(section, levelSeconds, seconds, radio, false);
    }

    /**
     * As {@link #update(int, double, float, boolean)}, with the run-time hook (M5 part C, {@code
     * full_on}).
     *
     * @param runTimeFull whether the sortie's state asks for the full mix now (a hold zone runs, the
     *     collapse has started), whatever the section
     */
    public void update(int section, double levelSeconds, float seconds, boolean radio, boolean runTimeFull) {
        boolean wasIntense = intense;
        intense = runTimeFull;
        // M8 part H: after the act boss's kill the theme is its motif stem alone, the timed changes no longer applied.
        afterMode = afterBoss != null && bossState == BossState.DOWN;
        afterLevel = afterMode ? (float) Math.clamp(afterBoss.level(levelSeconds), 0, 1) : 0;
        if (!afterMode) {
            stemTimes(levelSeconds, seconds);
        }
        boolean wanted = timedShare >= 0 ? timedShare > 0 : fullMix.test(section) || runTimeFull;
        // M8 part H: a motif-alone section plays the motif as its cue says (Level 28's section 1 silent before t=16).
        stemsOn = !afterMode && !motifAlone.test(section) && timedStemsOn;
        motifOn = afterMode || motifCue.on(section, levelSeconds);
        if (afterMode && afterLevel <= 0 && levelSeconds >= afterBoss.to() && music.isPresent()) {
            stopTheme();
        }
        if (levelSeconds >= ambienceFrom && fade < 0 && music.isPresent()) {
            fadeOut();
        }
        playJingle(levelSeconds);
        ambienceLevel(levelSeconds);
        layers(levelSeconds, seconds);
        loop(section);
        if (music.isEmpty()
                && !cut
                && fade < 0
                && (bossState == BossState.WAITING || (afterMode && afterLevel > 0))
                && levelSeconds < ambienceFrom
                && !(levelSeconds >= jingleAt)
                && section >= startSection
                && MusicStreamer.available(audio)) {
            StemMix mix = StemMix.of(
                    new VorbisFile(base.readBytes()),
                    new VorbisFile(full.readBytes()),
                    motifFile != null && motifFile.exists() ? new VorbisFile(motifFile.readBytes()) : null,
                    extraStems(),
                    CROSSFADE_SECONDS,
                    wanted ? 1 : 0,
                    motifOn);
            stemTarget = wanted ? 1 : 0;
            stems = Optional.of(mix);
            // M8 part H: the stems start where they are asked to be (silent under a motif-alone section's start).
            mix.stems(stemsOn, 0);
            if (!afterMode) {
                // M8 part F: the timed changes due now apply to the new stream at once.
                stemTime = -2;
                stemTimes(levelSeconds, 0);
            }
            float startLevel = afterMode ? MUSIC_VOLUME * afterLevel : MUSIC_VOLUME * rise;
            music = Optional.of(MusicStreamer.play(audio, mix, startLevel, mixer));
        }
        if (stems.isPresent()) {
            stems.get().motif(motifOn);
            stems.get().stems(stemsOn, timedFade);
        }
        float share = wanted ? 1 : 0;
        if (stems.isPresent() && share != stemTarget) {
            stems.get().fadeTo(share, fadeSeconds(share, wasIntense && !runTimeFull));
            stemTarget = share;
        }
        if (section > startSection) {
            rise = Math.min(1, rise + seconds * (1 - startLevel) / RISE_SECONDS);
        }
        ambience(section, seconds);
        sectionLevel(section, seconds);
        float radioLevel = radioDuckDb > 0 ? (float) Math.pow(10, -radioDuckDb / 20) : DUCKED;
        float target = duckTarget(radio, radioLevel, eventDuckLeft > 0 ? eventDuck : 1);
        eventDuckLeft = Math.max(0, eventDuckLeft - seconds);
        duck += (target - duck) * Math.min(1, DUCK_RATE * seconds);
        float faded = fade >= 0 ? fade / fadeLength : 1;
        float level = afterMode
                ? afterLevel * faded
                : rise * sectionGain * timedGain * faded * stingDip() * (themeOut >= 0 ? themeOut / BOSS_CROSSFADE : 1);
        if (sting >= 0) {
            sting -= seconds;
        }
        music.ifPresent(streamer -> streamer.setVolume(MUSIC_VOLUME * duck * level));
        float dropTarget = bossDropped && bossState == BossState.FIGHT ? 0 : 1;
        double dropSeconds = dropTarget < bossDropGain ? BOSS_DROP_OUT_SECONDS : BOSS_DROP_IN_SECONDS;
        bossDropGain = dropTarget < bossDropGain
                ? (float) Math.max(dropTarget, bossDropGain - seconds / dropSeconds)
                : (float) Math.min(dropTarget, bossDropGain + seconds / dropSeconds);
        float bossLevel = faded * bossDropGain * (bossOut >= 0 ? bossOut / FADE_SECONDS : 1);
        bossMusic.ifPresent(streamer -> streamer.setVolume(MUSIC_VOLUME * duck * bossLevel));
        if (themeOut >= 0) {
            themeOut = Math.max(0, themeOut - seconds);
            if (themeOut == 0) {
                themeOut = -1;
                stopTheme();
            }
        }
        if (bossOut >= 0) {
            bossOut = Math.max(0, bossOut - seconds);
            if (bossOut == 0) {
                bossOut = -1;
                stopBoss();
            }
        }
        if (fade >= 0) {
            fade = Math.max(0, fade - seconds);
            if (fade == 0) {
                stopTheme();
                stopBoss();
            }
        }
    }

    /**
     * M5 part G: from the jingle's time the theme fades out over the jingle's fade (once, unless a fade
     * already runs); after it the jingle plays once, unless the music was cut (a lost sortie).
     */
    private void playJingle(double levelSeconds) {
        if (!(levelSeconds >= jingleAt) || jinglePlayed || cut) {
            return;
        }
        if (fade < 0 && music.isPresent()) {
            fade = jingleFade;
            fadeLength = jingleFade;
        }
        if (levelSeconds >= jingleAt + jingleFade) {
            jinglePlayed = true;
            sfx.play(Sfx.MISSION_COMPLETE, JINGLE_VOLUME, 1, 0);
        }
    }

    /** M5 part G: the ambience follows its level by the level time (only when that level changes). */
    private void ambienceLevel(double levelSeconds) {
        float gain = (float) Math.pow(10, ambienceDb.applyAsDouble(levelSeconds) / 20);
        if (gain == ambienceGain) {
            return;
        }
        ambienceGain = gain;
        if (fadingAmbience == null) {
            sfx.loopVolume(currentAmbience, bed(gain));
        }
    }

    /**
     * How long a stem change takes (design/audio/music, Intensity layers): a second, but the run-time
     * hook's full mix fades back to the base stem over {@value #RUN_TIME_OUT_SECONDS} s.
     *
     * @param target the full mix's share it fades to
     * @param runTimeEnded whether the run-time hook just stopped asking for the full mix
     */
    static double fadeSeconds(float target, boolean runTimeEnded) {
        return target < 1 && runTimeEnded ? RUN_TIME_OUT_SECONDS : CROSSFADE_SECONDS;
    }

    /**
     * An act boss arrived: the theme crossfades out over 0.5 s while the boss warning starts, the
     * boss track coming in after the warning's bars. Returns false, doing nothing, for a level without
     * boss music (its mid-boss plays the sting instead).
     */
    public boolean bossArrived() {
        if (boss.isEmpty()) {
            return false;
        }
        if (bossState == BossState.FIGHT || cut) {
            return true;
        }
        bossState = BossState.FIGHT;
        bossDropped = false;
        bossDropGain = 1;
        if (music.isPresent()) {
            themeOut = BOSS_CROSSFADE;
        }
        sting = -1;
        stopBoss();
        bossOut = -1;
        if (MusicStreamer.available(audio)) {
            BossCue cue = cue(boss.get());
            if (cue != null) {
                bossMusic = Optional.of(MusicStreamer.play(audio, cue, MUSIC_VOLUME * duck, mixer));
            }
        }
        return true;
    }

    /** The boss's warning and track as one stream, from the files that exist; null when none does. */
    private static BossCue cue(Boss boss) {
        VorbisFile warning = boss.warning()
                .filter(FileHandle::exists)
                .map(file -> new VorbisFile(file.readBytes()))
                .orElse(null);
        LoopingStream track = boss.track()
                .filter(FileHandle::exists)
                .map(file -> LoopingStream.of(new VorbisFile(file.readBytes())))
                .orElse(null);
        if (warning == null && track == null) {
            return null;
        }
        int rate = warning != null ? warning.sampleRate() : track.sampleRate();
        return new BossCue(warning, track, Math.round(boss.trackFromSeconds() * rate));
    }

    /** The act boss is down: its music fades out in a second; the ambience plays on and the theme stays out. */
    public void bossDown() {
        Sfx after = Sfx.afterBoss(currentAmbience);
        if (after != null) {
            // M5 part H: the canopy's hum dies with the Siege Spire, the megacity's ambience comes back.
            swapAmbience(after, BOSS_AMBIENCE_IN_SECONDS, BOSS_AMBIENCE_OUT_SECONDS);
        }
        if (boss.isEmpty() || bossState != BossState.FIGHT) {
            return;
        }
        bossState = BossState.DOWN;
        if (bossMusic.isPresent()) {
            bossOut = FADE_SECONDS;
        }
    }

    /** A Retry from boss restarts the level at the boss checkpoint: the theme waits for the boss's arrival. */
    public void bossRetry() {
        if (boss.isPresent()) {
            bossState = BossState.HELD;
            bossDropped = false;
            bossDropGain = 1;
            stopTheme();
        }
    }

    /** Where the boss music stands, for the tests. */
    BossState bossState() {
        return bossState;
    }

    /**
     * A boss arrived (design/audio/music, Level 05's mini-boss): {@code sting} cuts in over a 0.5 s
     * crossfade from the theme, which comes back after it.
     */
    public void sting(Sfx stingSound) {
        sfx.play(stingSound, 1, 1, 0);
        sting = STING_SECONDS;
    }

    /** The theme's level under a sting: down over the first half second, back over the last. */
    private float stingDip() {
        if (sting < 0) {
            return 1;
        }
        float in = STING_SECONDS - sting;
        return Math.clamp(Math.max(1 - in / STING_FADE, 1 - sting / STING_FADE), 0, 1);
    }

    /** The voice loop plays while its section does (and the music has not been cut). */
    private void loop(int section) {
        if (voiceLoop.isEmpty()) {
            return;
        }
        VoiceLoop line = voiceLoop.get();
        boolean wanted = section == line.section() && !cut && line.file().exists();
        if (wanted && loopSound == null) {
            try {
                loopSound = audio.newSound(line.file());
                loopId = loopSound.loop(line.gain() * mixer.gain(Bus.VOICE));
            } catch (RuntimeException e) {
                loopSound = null;
            }
        } else if (!wanted && loopSound != null) {
            stopLoop();
        }
    }

    private void stopLoop() {
        if (loopSound != null) {
            loopSound.stop(loopId);
            loopSound.dispose();
            loopSound = null;
            loopId = -1;
        }
    }

    /** The ship was destroyed: the music cuts (the failure sting plays as a sound effect). */
    public void cut() {
        cut = true;
        stopTheme();
        stopBoss();
        stopLoop();
        if (revealed) {
            sfx.stop(Sfx.REVEAL_STING);
        }
    }

    /** The level restarts: the theme waits for its section again, at its start level. */
    public void restart() {
        cut = false;
        stemTime = -1;
        applyStemTime(-1, false);
        timedGain = 1;
        sting = -1;
        eventDuckLeft = 0;
        restartAmbience();
        sectionGain = sectionFrom = sectionTarget = (float) Math.pow(10, sectionDb.applyAsDouble(1) / 20);
        sectionLeft = 0;
        rise = startLevel;
        fade = -1;
        fadeLength = FADE_SECONDS;
        jinglePlayed = false;
        themeOut = -1;
        bossOut = -1;
        bossState = BossState.WAITING;
        bossDropped = false;
        bossDropGain = 1;
        afterMode = false;
        afterLevel = 0;
        if (revealed) {
            sfx.stop(Sfx.REVEAL_STING);
            revealed = false;
        }
        stopTheme();
        stopBoss();
        stopLoop();
    }

    /** The level restarts at its start: its first ambience again, at full. */
    private void restartAmbience() {
        changesMade = 0;
        fadeOutSeconds = Double.NaN;
        if (fadingAmbience != null) {
            sfx.stop(fadingAmbience);
            fadingAmbience = null;
        }
        if (currentAmbience != ambience) {
            sfx.stop(currentAmbience);
            currentAmbience = ambience;
            sfx.loop(ambience, bed(1));
        } else if (ambienceGain != 1) {
            sfx.loopVolume(ambience, bed(1));
        }
        ambienceGain = 1;
    }

    /** The level is won: the theme (or the boss music) fades out. */
    public void fadeOut() {
        fade = FADE_SECONDS;
        fadeLength = FADE_SECONDS;
    }

    private void stopTheme() {
        music.ifPresent(MusicStreamer::close);
        music = Optional.empty();
        stems = Optional.empty();
        intense = false;
    }

    private void stopBoss() {
        bossMusic.ifPresent(MusicStreamer::close);
        bossMusic = Optional.empty();
    }

    @Override
    public void dispose() {
        stopTheme();
        stopBoss();
        stopLoop();
        if (jinglePlayed) {
            // M5 part G: leaving the level mid-jingle (the pause menu's quit) must not leave it playing.
            sfx.stop(Sfx.MISSION_COMPLETE);
        }
        if (revealed) {
            sfx.stop(Sfx.REVEAL_STING);
        }
        sfx.stop(currentAmbience);
        if (fadingAmbience != null) {
            sfx.stop(fadingAmbience);
        }
        for (int i = 0; i < layers.size(); i++) {
            if (layerLevels[i] > 0) {
                sfx.stop(layers.get(i).sound());
                layerLevels[i] = 0;
            }
        }
    }
}
