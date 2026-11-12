package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.content.LevelData.Music.FullOn;

/** M5 part C: the full mix plays while the sortie's state asks for it, as the level's {@code full_on} names it. */
class RunTimeMusicTest {
    private static LevelData.Music music(Optional<List<FullOn>> fullOn) {
        LevelData.Music music = LevelData.Music.builder()
                .track(7)
                .fullSection(99)
                .ambience("earth-megacity")
                .endJingle("mission-complete")
                .build();
        return fullOn.map(events -> music.toBuilder().fullOn(events).build()).orElse(music);
    }

    @Test
    void holdsAndTheCollapsePlayTheFullMixOnlyWhenTheLevelNamesThem() {
        LevelData.Music level09 = music(Optional.of(List.of(FullOn.HOLD, FullOn.COLLAPSE)));
        assertFalse(LevelScreen.runTimeFull(level09, false, false));
        assertTrue(LevelScreen.runTimeFull(level09, true, false));
        assertTrue(LevelScreen.runTimeFull(level09, false, true));

        LevelData.Music holdsOnly = music(Optional.of(List.of(FullOn.HOLD)));
        assertTrue(LevelScreen.runTimeFull(holdsOnly, true, false));
        assertFalse(LevelScreen.runTimeFull(holdsOnly, false, true));

        LevelData.Music none = music(Optional.empty());
        assertFalse(LevelScreen.runTimeFull(none, true, true));
    }

    /**
     * M11 part C, step C4: Level 45's full mix goes by its sections alone (no run-time state asks for it); its capture's
     * −6 dB is a timed window (t=126 for 12 s), not a run-time duck.
     */
    @Test
    void level45sMusicGoesByItsSectionsAndItsTimedDuck() {
        vanguard.content.Content content = vanguard.content.ContentLoader.fromClasspath();
        LevelData.Music level45 =
                content.level(content.levelKey(45).orElseThrow()).music();
        assertTrue(level45.fullOn().isEmpty(), "no run-time full mix");
        assertFalse(LevelScreen.runTimeFull(level45, true, true));
        LevelData.Music.Duck duck = level45.duck().orElseThrow();
        assertTrue(duck.on() == LevelData.Music.DuckOn.TIME && duck.at().orElseThrow() == 126 && duck.seconds() == 12);
        assertTrue(LevelScreen.storyVerb("call-organ").equals("TAKEN"), "CALL ORGAN TAKEN on the event line");
        assertTrue(LevelScreen.storyVerb("helix-core").equals("SALVAGED"), "Level 34's core salvaged");
    }

    /**
     * M11 part D, step D4: Level 46's music goes by its timed stems alone (the base stem's drop at t=120 and return at
     * t=147, the sparse stem's until the user's Act 7 playthrough, 2026-11-12; no run-time full mix, no timed duck); the
     * warm hull's core reads {@code VRELL CORE SALVAGED}.
     */
    @Test
    void level46sMusicGoesByItsTimedStems() {
        vanguard.content.Content content = vanguard.content.ContentLoader.fromClasspath();
        LevelData.Music level46 =
                content.level(content.levelKey(46).orElseThrow()).music();
        assertTrue(level46.fullOn().isEmpty(), "no run-time full mix");
        assertFalse(LevelScreen.runTimeFull(level46, true, true));
        assertTrue(level46.duck().isEmpty(), "the quiet is a stem change, not a duck");
        assertTrue(level46.stemTimeList().stream().anyMatch(time -> time.t() == 120 && time.fade() == 1));
        assertTrue(level46.stemTimeList().stream().anyMatch(time -> time.t() == 147));
        assertTrue(LevelScreen.storyName("vrell-core").equals("VRELL CORE"));
        assertTrue(LevelScreen.storyVerb("vrell-core").equals("SALVAGED"), "VRELL CORE SALVAGED on the event line");
    }

    /**
     * M11 part E, step E4: Level 47's music goes by its timed stems (the full mix from t=62, the base from t=243; no
     * run-time full mix, no duck) with the Choir's statement, its motif stream, over the Choir hall (t=120–200).
     */
    @Test
    void level47sMusicGoesByItsTimedStemsAndTheHallsStatement() {
        vanguard.content.Content content = vanguard.content.ContentLoader.fromClasspath();
        LevelData.Music level47 =
                content.level(content.levelKey(47).orElseThrow()).music();
        assertTrue(level47.fullOn().isEmpty(), "no run-time full mix");
        assertFalse(LevelScreen.runTimeFull(level47, true, true));
        assertTrue(level47.duck().isEmpty());
        assertTrue(level47.stemTimeList().stream().anyMatch(time -> time.t() == 62));
        assertTrue(level47.stemTimeList().stream().anyMatch(time -> time.t() == 243));
        assertFalse(level47.motifOn(3, 119));
        assertTrue(level47.motifOn(3, 121));
        assertTrue(level47.motifOn(4, 199));
        assertFalse(level47.motifOn(4, 201));
    }

    /**
     * M11 part F, step F4: Level 48's music goes by its timed stems (track 16's base over the outer growth, the full mix
     * over the decks, the base from t=160, out from t=176 under Varga's warning; no run-time full mix, no duck); its
     * mid-boss plays the sting (track 21) as its warning, then track 34 "Vorne's Last Stand" (cut at the last words), and
     * track 16's full mix comes back after it; the living growth's bed.
     */
    @Test
    void level48sMusicGoesByItsTimedStemsAndVornesTrack() {
        vanguard.content.Content content = vanguard.content.ContentLoader.fromClasspath();
        LevelData.Music level48 =
                content.level(content.levelKey(48).orElseThrow()).music();
        assertTrue(level48.fullOn().isEmpty(), "no run-time full mix");
        assertFalse(LevelScreen.runTimeFull(level48, true, true));
        assertTrue(level48.duck().isEmpty());
        assertTrue(level48.stemTimeList().stream().anyMatch(time -> time.t() == 160));
        assertTrue(level48.stemTimeList().stream().anyMatch(time -> time.t() == 176));
        assertTrue(level48.ambience().equals("vrell-hive"));
        LevelScreen.BossMusic boss = LevelScreen.bossMusic(level48).orElseThrow();
        assertTrue(boss.midBoss(), "the sting as its warning");
        assertTrue(boss.track().orElseThrow().equals("vorne-last-stand"));
        assertTrue(vanguard.game.audio.Tracks.path(vanguard.game.audio.Tracks.VORNE_LAST_STAND)
                .equals(vanguard.game.audio.Tracks.path(boss.track().orElseThrow())));
        assertTrue(level48.trackAfterBoss().orElseThrow().equals("full"));
    }

    /**
     * M11 part G, step G4: Level 49's music goes by the level clock (the memory copies' theme changes at t=31 and 61,
     * out under t=84's line; no run-time full mix) and a boss track per echo ({@code boss_tracks}), each after the
     * unfiltered warning.
     */
    @Test
    void level49sMusicGoesByTheClockAndABossTrackPerEcho() {
        vanguard.content.Content content = vanguard.content.ContentLoader.fromClasspath();
        LevelData.Music level49 =
                content.level(content.levelKey(49).orElseThrow()).music();
        assertTrue(level49.fullOn().isEmpty(), "no run-time full mix");
        assertFalse(LevelScreen.runTimeFull(level49, true, true));
        assertTrue(LevelScreen.theme(level49.track()).equals("afterburner-memory"));
        assertTrue(level49.themeList().stream().anyMatch(theme -> theme.t() == 31));
        assertTrue(level49.themeList().stream().anyMatch(theme -> theme.t() == 61));
        assertTrue(level49.stemTimeList().stream().anyMatch(time -> time.t() == 84));
        LevelScreen.BossMusic boss = LevelScreen.bossMusic(level49).orElseThrow();
        assertFalse(boss.midBoss(), "the warning (track 22) before each echo");
        assertTrue(boss.warning().orElseThrow().equals("boss-warning"));
        List<String> tracks =
                LevelScreen.fightTracks(level49, new com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files()).stream()
                        .map(file -> file.path())
                        .toList();
        assertTrue(
                tracks.equals(List.of(
                        "music/choir-descends-memory.ogg",
                        "music/choir-descends-memory.ogg",
                        "music/iron-sovereign-memory.ogg")),
                tracks.toString());
        LevelData.Music level48 =
                content.level(content.levelKey(48).orElseThrow()).music();
        assertTrue(LevelScreen.fightTracks(level48, new com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files())
                .isEmpty());
    }
    /**
     * M11 part H, step H4: Level 50's music goes by the clock (track 17's full mix with its choir stem over the approach,
     * the base from t=160, out from t=171 under Varga's line; no run-time full mix), then the warning (track 22) and
     * track 20 through the fight; at K its stem alone ({@code final-boss-motif}), and the coda from the control's taking:
     * the final boss's track only.
     */
    @Test
    void level50sMusicGoesByTheClockThenTrack20AndItsStemAfterK() {
        vanguard.content.Content content = vanguard.content.ContentLoader.fromClasspath();
        LevelData.Music level50 =
                content.level(content.levelKey(50).orElseThrow()).music();
        assertTrue(level50.fullOn().isEmpty(), "no run-time full mix");
        assertFalse(LevelScreen.runTimeFull(level50, true, true));
        assertTrue(level50.stemTimeList().stream().anyMatch(time -> time.t() == 160));
        assertTrue(level50.stemTimeList().stream().anyMatch(time -> time.t() == 171));
        LevelScreen.BossMusic boss = LevelScreen.bossMusic(level50).orElseThrow();
        assertFalse(boss.midBoss(), "the warning (track 22) before the Heart shows");
        assertTrue(boss.warning().orElseThrow().equals("boss-warning"));
        assertTrue(boss.track().orElseThrow().equals(LevelScreen.FINAL_BOSS));
        assertTrue(LevelScreen.killStem(level50).orElseThrow().equals("music/final-boss-motif.ogg"));
        LevelData.Music level48 =
                content.level(content.levelKey(48).orElseThrow()).music();
        assertTrue(LevelScreen.killStem(level48).isEmpty(), "Vorne's track is cut at his K");
        assertTrue(LevelScreen.noiseEvent("near-side") == vanguard.sim.SimEvents.Type.NEAR_SIDE);
    }
}
