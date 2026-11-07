package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.game.settings.AudioSettings;

/** The act boss's music cues in a level (design/audio/music, Loops and transitions), without a device. */
class BossMusicTest {
    private final MockAudio audio = new MockAudio();
    private final Lwjgl3Files files = new Lwjgl3Files();
    private final Mixer mixer = new Mixer(AudioSettings.defaults());
    private final SfxBank bank = new SfxBank(audio, files, mixer);

    private LevelMusic music(Optional<LevelMusic.Boss> boss) {
        return new LevelMusic(
                audio,
                mixer,
                files.internal(Tracks.basePath("coalition-rising")),
                files.internal(Tracks.path(5)),
                bank,
                Sfx.AMBIENCE_ORBIT,
                1,
                0,
                section -> true,
                Double.POSITIVE_INFINITY,
                Optional.empty(),
                boss);
    }

    private LevelMusic withBoss() {
        return music(Optional.of(new LevelMusic.Boss(
                Optional.of(files.internal(Tracks.path(Tracks.BOSS_WARNING))),
                Optional.of(files.internal(Tracks.path(Tracks.BOSS_VRELL))),
                Tracks.BOSS_WARNING_BARS_SECONDS)));
    }

    @Test
    void aLevelWithoutBossMusicLeavesTheArrivalToTheSting() {
        LevelMusic music = music(Optional.empty());

        assertFalse(music.bossArrived(), "Level 05's frigate plays its sting instead");
        assertEquals(LevelMusic.BossState.WAITING, music.bossState());
        music.dispose();
    }

    @Test
    void theBossMusicRunsFromTheArrivalToTheKillAndTheThemeStaysOut() {
        LevelMusic music = withBoss();
        music.update(4, 99, 0.1f, false);

        assertTrue(music.bossArrived());
        assertEquals(LevelMusic.BossState.FIGHT, music.bossState());
        music.update(4, 101, 0.1f, false);
        music.bossDown();
        assertEquals(LevelMusic.BossState.DOWN, music.bossState());
        music.update(4, 140, 2, false);
        assertEquals(LevelMusic.BossState.DOWN, music.bossState(), "only the ambience after the kill");
        music.dispose();
    }

    @Test
    void aRetryFromBossHoldsTheThemeUntilTheBossArrivesAndARestartForgetsIt() {
        LevelMusic music = withBoss();
        music.bossArrived();

        music.restart();
        music.bossRetry();
        assertEquals(LevelMusic.BossState.HELD, music.bossState());
        assertTrue(music.bossArrived());
        assertEquals(LevelMusic.BossState.FIGHT, music.bossState());

        music.restart();
        assertEquals(LevelMusic.BossState.WAITING, music.bossState());
        music.dispose();
    }

    /**
     * M11 part F, step F4 (design/campaign Level 48, Music &amp; ambience): Vorne's last words (K) cut track 34 at once
     * (a few ms, no sting over it); the burst's kill changes nothing more; the theme (its full mix) comes back only when
     * the level clock moves on past K (the hold's end), not during the calm.
     */
    @Test
    void theLastWordsCutTheMidBossTrackAndTheThemeWaitsForTheClock() {
        LevelMusic music = music(Optional.of(new LevelMusic.Boss(
                        Optional.of(files.internal(Tracks.path(21))),
                        Optional.of(files.internal(Tracks.path(Tracks.VORNE_LAST_STAND))),
                        Tracks.MINIBOSS_STING_SECONDS)))
                .midBoss("full");
        assertEquals("vorne-last-stand", Tracks.name(Tracks.VORNE_LAST_STAND).orElseThrow());
        music.update(6, 186, 0.1f, false);
        assertTrue(music.bossArrived());
        music.update(6, 187, 0.1f, false);
        music.bossCut(187);
        assertEquals(LevelMusic.BossState.DOWN, music.bossState(), "cut at K");
        assertTrue(LevelMusic.CUT_SECONDS < 0.05, "a hard cut, a few ms so it does not click");
        assertTrue(music.waitingAfterCut());
        music.update(6, 187, 4.5f, false);
        music.bossDown();
        assertFalse(music.midBossBack(), "the burst: the bed alone still");
        music.update(6, 187, 3.5f, false);
        assertFalse(music.midBossBack(), "the hold: the bed alone");
        music.update(7, 187.02, 0.02f, false);
        assertTrue(music.midBossBack(), "the clock moves on: track 16's full mix comes back");
        assertFalse(music.waitingAfterCut());

        music.restart();
        assertFalse(music.waitingAfterCut());
        assertEquals(LevelMusic.BossState.WAITING, music.bossState());
        music.dispose();
    }

    @Test
    void bossMusicHasAWarningOrATrack() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> new LevelMusic.Boss(Optional.empty(), Optional.empty(), 0));
    }
}
