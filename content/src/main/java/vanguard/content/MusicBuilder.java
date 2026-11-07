package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M10 part C, step C2 (RF5's open add-on, RF9b's pattern): builds {@link LevelData.Music} in code (the data file builds
 * it by its keys). Every key starts left out: track 0, the music from section 1 with every stem, no ambience or end
 * jingle; set what the cues need and {@link #build()} checks them as a whole.
 */
public final class MusicBuilder {
    private int track = 0;
    private int startSection = 1;
    private Optional<Double> startDb = Optional.empty();
    private int fullSection = 1;
    private Optional<Map<Integer, LevelData.Music.Stems>> stems = Optional.empty();
    private String ambience = "";
    private String endJingle = "";
    private Optional<String> bossSting = Optional.empty();
    private Optional<Double> ambienceFrom = Optional.empty();
    private Optional<LevelData.Music.VoiceLoop> voiceLoop = Optional.empty();
    private Optional<String> bossWarning = Optional.empty();
    private Optional<String> bossTrack = Optional.empty();
    private Optional<List<LevelData.Music.FullOn>> fullOn = Optional.empty();
    private Optional<LevelData.Music.Duck> duck = Optional.empty();
    private Optional<List<LevelData.Music.AmbienceChange>> ambienceChanges = Optional.empty();
    private Optional<Map<Integer, Double>> sectionDb = Optional.empty();
    private Optional<LevelData.Music.Jingle> jingle = Optional.empty();
    private Optional<LevelData.Music.Motif> motif = Optional.empty();
    private Optional<List<LevelData.Music.AmbienceLayer>> ambienceLayers = Optional.empty();
    private Optional<List<LevelData.Music.StemTime>> stemTimes = Optional.empty();
    private Optional<LevelData.Music.MotifAfterBoss> motifAfterBoss = Optional.empty();
    private Optional<List<LevelData.Music.ThemeChange>> themes = Optional.empty();
    private Optional<Map<Integer, Double>> ambienceDb = Optional.empty();
    private Optional<String> trackAfterBoss = Optional.empty();
    private Optional<List<LevelData.Music.BossTrackChange>> bossTrackChanges = Optional.empty();
    private Optional<String> bossEndSting = Optional.empty();
    private Optional<List<String>> bossTracks = Optional.empty();

    MusicBuilder() {}

    MusicBuilder(LevelData.Music music) {
        track = music.track();
        startSection = music.startSection();
        startDb = music.startDb();
        fullSection = music.fullSection();
        stems = music.stems();
        ambience = music.ambience();
        endJingle = music.endJingle();
        bossSting = music.bossSting();
        ambienceFrom = music.ambienceFrom();
        voiceLoop = music.voiceLoop();
        bossWarning = music.bossWarning();
        bossTrack = music.bossTrack();
        fullOn = music.fullOn();
        duck = music.duck();
        ambienceChanges = music.ambienceChanges();
        sectionDb = music.sectionDb();
        jingle = music.jingle();
        motif = music.motif();
        ambienceLayers = music.ambienceLayers();
        stemTimes = music.stemTimes();
        motifAfterBoss = music.motifAfterBoss();
        themes = music.themes();
        ambienceDb = music.ambienceDb();
        trackAfterBoss = music.trackAfterBoss();
        bossTrackChanges = music.bossTrackChanges();
        bossEndSting = music.bossEndSting();
        bossTracks = music.bossTracks();
    }

    /** Its {@link LevelData.Music#track()}. */
    public MusicBuilder track(int track) {
        this.track = track;
        return this;
    }

    /** Its {@link LevelData.Music#startSection()}. */
    public MusicBuilder startSection(int startSection) {
        this.startSection = startSection;
        return this;
    }

    /** Its {@link LevelData.Music#startDb()}. */
    public MusicBuilder startDb(Double startDb) {
        this.startDb = Optional.of(startDb);
        return this;
    }

    /** Its {@link LevelData.Music#fullSection()}. */
    public MusicBuilder fullSection(int fullSection) {
        this.fullSection = fullSection;
        return this;
    }

    /** Its {@link LevelData.Music#stems()}. */
    public MusicBuilder stems(Map<Integer, LevelData.Music.Stems> stems) {
        this.stems = Optional.of(stems);
        return this;
    }

    /** Its {@link LevelData.Music#ambience()}. */
    public MusicBuilder ambience(String ambience) {
        this.ambience = ambience;
        return this;
    }

    /** Its {@link LevelData.Music#endJingle()}. */
    public MusicBuilder endJingle(String endJingle) {
        this.endJingle = endJingle;
        return this;
    }

    /** Its {@link LevelData.Music#bossSting()}. */
    public MusicBuilder bossSting(String bossSting) {
        this.bossSting = Optional.of(bossSting);
        return this;
    }

    /** Its {@link LevelData.Music#ambienceFrom()}. */
    public MusicBuilder ambienceFrom(Double ambienceFrom) {
        this.ambienceFrom = Optional.of(ambienceFrom);
        return this;
    }

    /** Its {@link LevelData.Music#voiceLoop()}. */
    public MusicBuilder voiceLoop(LevelData.Music.VoiceLoop voiceLoop) {
        this.voiceLoop = Optional.of(voiceLoop);
        return this;
    }

    /** Its {@link LevelData.Music#bossWarning()}. */
    public MusicBuilder bossWarning(String bossWarning) {
        this.bossWarning = Optional.of(bossWarning);
        return this;
    }

    /** Its {@link LevelData.Music#bossTrack()}. */
    public MusicBuilder bossTrack(String bossTrack) {
        this.bossTrack = Optional.of(bossTrack);
        return this;
    }

    /** Its {@link LevelData.Music#fullOn()}. */
    public MusicBuilder fullOn(List<LevelData.Music.FullOn> fullOn) {
        this.fullOn = Optional.of(fullOn);
        return this;
    }

    /** Its {@link LevelData.Music#duck()}. */
    public MusicBuilder duck(LevelData.Music.Duck duck) {
        this.duck = Optional.of(duck);
        return this;
    }

    /** Its {@link LevelData.Music#ambienceChanges()}. */
    public MusicBuilder ambienceChanges(List<LevelData.Music.AmbienceChange> ambienceChanges) {
        this.ambienceChanges = Optional.of(ambienceChanges);
        return this;
    }

    /** Its {@link LevelData.Music#sectionDb()}. */
    public MusicBuilder sectionDb(Map<Integer, Double> sectionDb) {
        this.sectionDb = Optional.of(sectionDb);
        return this;
    }

    /** Its {@link LevelData.Music#jingle()}. */
    public MusicBuilder jingle(LevelData.Music.Jingle jingle) {
        this.jingle = Optional.of(jingle);
        return this;
    }

    /** Its {@link LevelData.Music#motif()}. */
    public MusicBuilder motif(LevelData.Music.Motif motif) {
        this.motif = Optional.of(motif);
        return this;
    }

    /** Its {@link LevelData.Music#ambienceLayers()}. */
    public MusicBuilder ambienceLayers(List<LevelData.Music.AmbienceLayer> ambienceLayers) {
        this.ambienceLayers = Optional.of(ambienceLayers);
        return this;
    }

    /** Its {@link LevelData.Music#stemTimes()}. */
    public MusicBuilder stemTimes(List<LevelData.Music.StemTime> stemTimes) {
        this.stemTimes = Optional.of(stemTimes);
        return this;
    }

    /** Its {@link LevelData.Music#motifAfterBoss()}. */
    public MusicBuilder motifAfterBoss(LevelData.Music.MotifAfterBoss motifAfterBoss) {
        this.motifAfterBoss = Optional.of(motifAfterBoss);
        return this;
    }

    /** Its {@link LevelData.Music#themes()}. */
    public MusicBuilder themes(List<LevelData.Music.ThemeChange> themes) {
        this.themes = Optional.of(themes);
        return this;
    }

    /** M10 part D: its {@link LevelData.Music#ambienceDb()}. */
    public MusicBuilder ambienceDb(Map<Integer, Double> ambienceDb) {
        this.ambienceDb = Optional.of(ambienceDb);
        return this;
    }

    /** M10 part F: its {@link LevelData.Music#trackAfterBoss()}. */
    public MusicBuilder trackAfterBoss(String trackAfterBoss) {
        this.trackAfterBoss = Optional.of(trackAfterBoss);
        return this;
    }

    /** M10 part H: its {@link LevelData.Music#bossTrackChanges()}. */
    public MusicBuilder bossTrackChanges(List<LevelData.Music.BossTrackChange> bossTrackChanges) {
        this.bossTrackChanges = Optional.of(bossTrackChanges);
        return this;
    }

    /** M10 part H: its {@link LevelData.Music#bossEndSting()}. */
    public MusicBuilder bossEndSting(String bossEndSting) {
        this.bossEndSting = Optional.of(bossEndSting);
        return this;
    }

    /** M11 part G, step G2: its {@link LevelData.Music#bossTracks()}. */
    public MusicBuilder bossTracks(List<String> bossTracks) {
        this.bossTracks = Optional.of(bossTracks);
        return this;
    }

    /** The music cues, checked as a whole. */
    public LevelData.Music build() {
        return new LevelData.Music(
                track,
                startSection,
                startDb,
                fullSection,
                stems,
                ambience,
                endJingle,
                bossSting,
                ambienceFrom,
                voiceLoop,
                bossWarning,
                bossTrack,
                fullOn,
                duck,
                ambienceChanges,
                sectionDb,
                jingle,
                motif,
                ambienceLayers,
                stemTimes,
                motifAfterBoss,
                themes,
                ambienceDb,
                trackAfterBoss,
                bossTrackChanges,
                bossEndSting,
                bossTracks);
    }
}
