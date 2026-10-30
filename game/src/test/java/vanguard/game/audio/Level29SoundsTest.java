package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part B, Level 29's sounds (step B4; design/audio/sfx, Act 5 sounds; step AUD-B's round 62 proposals, their a
 * variants until round 62 picks; design/audio/music track 12 and 31): every key plays its file; the level's theme, its
 * stems, Rook's return sting and the station bed; and flown to the turn, invulnerable and firing: the Talons' whine and
 * twin crack, the SAM Nests' hatch and lock beep, Block C's relays and klaxon and the nests' wake at t=60 (the first
 * wake: the clamp guns' own wake at the halt is not the station's).
 */
class Level29SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(29).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    /**
     * Step FIX1 (design/audio/music, Cues per level (Act 5): Vorne's motif "on"): track 12's motif stem on top of
     * Level 29's theme from the base stem's start (section 2) to the end.
     */
    @Test
    void theMotifPlaysOnTopFromTheBaseStemsStartToTheEnd() {
        LevelData.Music data = CONTENT.level(KEY).music();
        String theme = Tracks.name(data.track()).orElseThrow();
        assertEquals("hollow-rock", theme);
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.motifPath(theme))), "track 12's motif stem");
        assertTrue(data.motif().isPresent(), "the level's motif cue");
        assertEquals(2, data.startSection());
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath(theme)),
                        files.internal(Tracks.path(theme)),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(files.internal(Tracks.motifPath(theme)), data::motifOn);
        music.update(1, 10, 0.1f, false);
        assertFalse(music.motifOn(), "no motif under the intro (the theme's own statement)");
        music.update(2, 28.05, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the base stem with the motif from t=28");
        music.update(4, 70, 0.1f, false);
        assertTrue(music.motifOn(), "over the full mix from the turn");
        music.update(6, 160, 0.1f, false);
        assertTrue(music.motifOn(), "to the end");
        music.dispose();
        sfx.dispose();
    }

    @Test
    void everyLevel29SoundPlaysItsProposal() {
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.STATION_KLAXON, "sfx/station-klaxon-r62-a.ogg"),
                Map.entry(Sfx.STATION_RELAYS, "sfx/station-relays-r62-a.ogg"),
                Map.entry(Sfx.NEST_HATCH, "sfx/nest-hatch-r62-b.ogg"),
                Map.entry(Sfx.NEST_LOCK, "sfx/nest-lock-r62-b.ogg"),
                Map.entry(Sfx.NEST_WAKE, "sfx/nest-wake-r62-ab.ogg"),
                Map.entry(Sfx.CLAMP_GUN_SHOT, "sfx/clamp-gun-shot-r62-b.ogg"),
                Map.entry(Sfx.CLAMP_RELEASE, "sfx/clamp-release-r62-b.ogg"),
                Map.entry(Sfx.EMBER_LAUNCH, "sfx/ember-launch-r62-a.ogg"),
                Map.entry(Sfx.BULWARK_DEPART, "sfx/bulwark-depart-r62-a.ogg"),
                Map.entry(Sfx.TALON_WHINE, "sfx/talon-whine-r62-a.ogg"),
                Map.entry(Sfx.TALON_SHOT, "sfx/talon-shot-r62-b.ogg"),
                Map.entry(Sfx.LEADER_SQUELCH, "sfx/leader-squelch-r62-b.ogg"),
                Map.entry(Sfx.MITE_TELL, "sfx/mite-tell-r62-a.ogg"),
                Map.entry(Sfx.MITE_LEAP, "sfx/mite-leap-r62-a.ogg"),
                Map.entry(Sfx.AMBIENCE_BELT_STATION, "sfx/belt-station-r60-b.ogg"),
                Map.entry(Sfx.ROOK_RETURN_STING, "music/rook-return.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals(file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        });
        LevelData.Music music = CONTENT.level(KEY).music();
        assertEquals(Sfx.AMBIENCE_BELT_STATION, Sfx.ambience(music.ambience()), "the belt's station bed");
        String theme = Tracks.name(music.track()).orElseThrow();
        assertEquals("hollow-rock", theme, "track 12");
        for (String path : List.of(Tracks.path(theme), Tracks.basePath(theme), Tracks.motifPath(theme))) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(path)), path);
        }
        assertEquals("music/rook-return.ogg", Tracks.path(Tracks.ROOK_RETURN));
        assertEquals(Bus.MUSIC, Sfx.ROOK_RETURN_STING.bus(), "the sting is music");
    }

    /** Flown to the turn and past it, invulnerable, firing from the middle of the canyon. */
    @Test
    void theTalonsTheNestsAndTheTurningStationSound() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean whine = false;
        boolean shot = false;
        boolean hatch = false;
        double relays = Double.NaN;
        double klaxon = Double.NaN;
        boolean wake = false;
        while (sortie.levelSeconds() < 66) {
            now[0] += NANOS_PER_STEP;
            sortie.step(IceShelfFixture.toward(sortie, 240, 100));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            whine |= bank.playing(Sfx.TALON_WHINE) > 0;
            shot |= bank.playing(Sfx.TALON_SHOT) > 0;
            hatch |= bank.playing(Sfx.NEST_HATCH) > 0 && bank.playing(Sfx.NEST_LOCK) > 0;
            wake |= bank.playing(Sfx.NEST_WAKE) > 0;
            if (Double.isNaN(relays) && bank.playing(Sfx.STATION_RELAYS) > 0) {
                relays = sortie.levelSeconds();
            }
            if (Double.isNaN(klaxon) && bank.playing(Sfx.STATION_KLAXON) > 0) {
                klaxon = sortie.levelSeconds();
            }
        }
        assertTrue(whine, "a Talon swooping in");
        assertTrue(shot, "a Talon's twin crack");
        assertTrue(hatch, "a SAM Nest's hatch with its lock beep");
        assertTrue(wake, "Block C's nests waking");
        assertEquals(60, relays, 0.1, "the relays as Block C declares");
        assertTrue(klaxon > relays && klaxon < relays + 1, "then the klaxon: " + klaxon);
        sounds.stopLoops();
        bank.dispose();
    }

    /**
     * Step FIX6 (the user's playthrough 2026-10-30: the wavering Stormhawks vanished when shot): a destroyed neutral
     * craft ({@code NEUTRAL_DOWN}, no kill) bursts with its kind's explosion. Flown to the first wavering pair,
     * invulnerable, under the first craft and firing until it falls.
     */
    @Test
    void aDestroyedWaveringStormhawkBurstsWithItsExplosion() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean burst = false;
        boolean down = false;
        while (!down && sortie.levelSeconds() < 140) {
            now[0] += NANOS_PER_STEP;
            double x = 240;
            for (int e = 0; e < sortie.enemyCount(); e++) {
                if (sortie.enemy(e).spec().neutral()) {
                    x = sortie.enemy(e).renderX(1);
                    break;
                }
            }
            sortie.step(IceShelfFixture.toward(sortie, x, 100));
            sounds.step();
            down = has(sortie, SimEvents.Type.NEUTRAL_DOWN);
            boolean killed = has(sortie, SimEvents.Type.ENEMY_DESTROYED);
            int before = bank.playing(Sfx.EXPLOSION_SMALL_A) + bank.playing(Sfx.EXPLOSION_SMALL_B);
            sounds.play(sortie.events());
            int after = bank.playing(Sfx.EXPLOSION_SMALL_A) + bank.playing(Sfx.EXPLOSION_SMALL_B);
            burst = down && !killed && after > before;
        }
        assertTrue(down, "a wavering craft shot down by t=140 (60 HP at medium since step FIX7)");
        assertTrue(burst, "its explosion plays");
        sounds.stopLoops();
        bank.dispose();
    }

    private static boolean has(Sortie sortie, SimEvents.Type type) {
        for (int i = 0; i < sortie.events().size(); i++) {
            if (sortie.events().type(i) == type) {
                return true;
            }
        }
        return false;
    }
}
