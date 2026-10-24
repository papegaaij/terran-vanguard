package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.game.render.BlackwaterFixture;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part F's Level 26 sounds (design/audio/sfx and design/audio/music, Level 26's rows; round 55's picks, closed
 * 2026-10-24: the spin, the click and the static b, the rest a; the warm pad b): the Glow Angler's lure hum and lunge, the Nautilus's crown hum in the dark, Rook's loss (the trails, the
 * hits, the spin), the headlights' clicks, the Choir's static; the black water's bed; track 10 with its timed stems
 * (the dark stem alone from t=8, −6 dB from 150, out at 166, the warm pad from 174) and the Choir's 6 dB duck.
 */
class Level26SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    private static final List<Sfx> SOUNDS = List.of(
            Sfx.LURE_HUM,
            Sfx.ANGLER_LUNGE,
            Sfx.CROWN_HUM,
            Sfx.TORPEDO_TRAILS,
            Sfx.ROOK_HIT,
            Sfx.ROOK_SPIN,
            Sfx.HEADLIGHT_CLICK,
            Sfx.CHOIR_STATIC,
            Sfx.AMBIENCE_EUROPA_BLACK);

    @Test
    void everySoundHasItsFileRoundFiftyFivesPick() {
        List<Sfx> b = List.of(Sfx.ROOK_SPIN, Sfx.HEADLIGHT_CLICK, Sfx.CHOIR_STATIC);
        for (Sfx sfx : SOUNDS) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(
                    sfx.path().endsWith(b.contains(sfx) ? "-r55-b.ogg" : "-r55-a.ogg"),
                    "round 55's pick: " + sfx.path());
        }
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_EUROPA_BLACK.priority());
        assertTrue(Sfx.LURE_HUM.priority() != Sfx.Priority.AMBIENCE, "a unit's loop is stolen like its peers");
        assertTrue(Sfx.CROWN_HUM.priority() != Sfx.Priority.AMBIENCE);
    }

    @Test
    void theBedIsTheBlackWaterAndTheTrenchOverTheScarp() {
        LevelData.Music music = BlackwaterFixture.level().music();
        assertEquals(10, music.track(), "track 10 only (gap check F12)");
        assertEquals(Sfx.AMBIENCE_EUROPA_BLACK, Sfx.ambience(music.ambience()));
        assertEquals(
                List.of(Sfx.AMBIENCE_EUROPA_TRENCH),
                music.ambienceLayers().orElseThrow().stream()
                        .map(layer -> Sfx.ambience(layer.ambience()))
                        .toList(),
                "the trench's rumble over the scarp");
    }

    /** The dark stem and the warm pad are on track 10's grid: the stems' length and loop points. */
    @Test
    void theExtraStemsAreOnTheThemesGrid() throws IOException {
        VorbisFile full = vorbis(Tracks.path("thera-deep"));
        for (String stem : List.of("dark", "warm")) {
            VorbisFile extra = vorbis(Tracks.stemPath("thera-deep", stem));
            assertEquals(full.frameCount(), extra.frameCount(), stem);
            assertEquals(full.loopPoints(), extra.loopPoints(), stem);
            assertEquals(full.sampleRate(), extra.sampleRate(), stem);
            assertEquals(full.channels(), extra.channels(), stem);
        }
    }

    private static VorbisFile vorbis(String path) throws IOException {
        return new VorbisFile(Files.readAllBytes(ASSETS.resolve(path)));
    }

    /**
     * The level's stem changes through the music: the stems until t=8, the dark stem alone from 8, 6 dB down over 2 s
     * from 150, silent from 164 (out at 166), the warm pad from 174 at its file's level; a restart starts over.
     */
    @Test
    void theStemsStripBackThinGoSilentAndTheWarmPadComesIn() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("thera-deep")),
                        files.internal(Tracks.path("thera-deep")),
                        sfx,
                        Sfx.AMBIENCE_EUROPA_BLACK,
                        1,
                        0,
                        section -> true,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .stemTimes(
                        BlackwaterFixture.level().music().stemTimeList(),
                        stem -> files.absolute(ASSETS.resolve(Tracks.stemPath("thera-deep", stem))
                                .toAbsolutePath()
                                .toString()));
        music.update(1, 5, 0.1f, false);
        assertTrue(music.timedStemsOn(), "the full mix with the lamps");
        assertEquals(-1, music.timedExtra());
        music.update(1, 8.05, 0.1f, false);
        assertFalse(music.timedStemsOn(), "the dark stem alone from t=8");
        assertEquals(0, music.timedExtra(), "the dark stem");
        assertEquals(1, music.timedGain(), 1e-6);
        double t = 150;
        while (t < 152.5) {
            music.update(1, t, 0.1f, false);
            t += 0.1;
        }
        assertEquals(Math.pow(10, -6 / 20.0), music.timedGain(), 1e-3, "-6 dB from t=150 over 2 s");
        music.update(1, 158.05, 0.1f, false);
        assertEquals(0, music.timedExtra(), "the dark stem through Rook's cut line");
        music.update(1, 164.05, 0.1f, false);
        assertEquals(-1, music.timedExtra(), "out from 164, silent at 166");
        assertFalse(music.timedStemsOn());
        music.update(1, 174.05, 0.1f, false);
        assertEquals(1, music.timedExtra(), "the warm pad from 174");
        t = 174.15;
        while (t < 185) {
            music.update(1, t, 0.1f, false);
            t += 0.1;
        }
        assertEquals(1, music.timedGain(), 1e-3, "the pad at its file's level");
        music.restart();
        music.update(1, 1, 0.1f, false);
        assertTrue(music.timedStemsOn(), "a retry starts over with the full mix");
        assertEquals(-1, music.timedExtra());
        assertEquals(1, music.timedGain(), 1e-6);
        music.dispose();
        sfx.dispose();
    }

    /** The Choir's line ducks the music 6 dB (its row's {@code duck_db}), other lines the radio's 4 dB. */
    @Test
    void theChoirsLineDucksTheMusicSixDecibels() {
        LevelData level = BlackwaterFixture.level();
        assertTrue(
                level.radio().stream()
                        .anyMatch(cue -> cue.speaker().equals("The Choir")
                                && cue.musicDuckDb() == 6
                                && cue.staticLeadSeconds() == 2),
                "the Choir's row: static_lead 2, duck_db 6");
        assertEquals(
                (float) Math.pow(10, -6 / 20.0), LevelMusic.duckTarget(true, (float) Math.pow(10, -6 / 20.0), 1), 1e-6);
        assertEquals(LevelMusic.DUCKED, LevelMusic.duckTarget(true, 1), 1e-6);
    }

    /** The lure hum is louder close to the ship. */
    @Test
    void theLureHumRisesAsTheShipComesClose() {
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL, BlackwaterCues.lureLevel(40), 1e-6);
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL * BlackwaterCues.FAR_LEVEL, BlackwaterCues.lureLevel(600), 1e-6);
        assertTrue(BlackwaterCues.lureLevel(200) < BlackwaterCues.lureLevel(100));
    }

    /**
     * Flown through the level with Rook along, invulnerable, firing, held low in the middle: the headlights click on
     * at the start and as the lamps go out (t=18); an Angler's lure hums while one is on the screen and its lunge
     * sounds; the crown hums with a Nautilus on the screen; Rook's trails at 162.5, his hits and spin at 163.5.
     */
    @Test
    void theLuresHumTheCrownsSingAndRooksLossIsHeard() {
        Sortie sortie = BlackwaterFixture.sortie(true);
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                List.of(),
                Optional.of(sortie.wingman().orElseThrow().gun()));
        sounds.dark(18);
        int clicks = 0;
        boolean lure = false;
        boolean lunge = false;
        boolean crown = false;
        boolean trails = false;
        boolean lost = false;
        int hushing = 0;
        while (sortie.levelSeconds() < 164 && !sortie.complete()) {
            int before = bank.playing(Sfx.HEADLIGHT_CLICK);
            // M8 AW: holding fire: the reworked harpoon and the charges tossed ahead kill the Anglers before they
            // lunge.
            step(sortie, sounds, IceShelfFixture.toward(sortie, 240, 110) & ~Command.FIRE.bit());
            clicks += Math.max(0, bank.playing(Sfx.HEADLIGHT_CLICK) - before);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case LURE_LUNGE -> {
                        lunge = true;
                        assertTrue(bank.playing(Sfx.ANGLER_LUNGE) > 0, "the rush at the lunge");
                    }
                    case ROOK_TRAILS -> {
                        trails = true;
                        assertTrue(bank.playing(Sfx.TORPEDO_TRAILS) > 0, "the trails rising");
                    }
                    case ROOK_LOST -> {
                        lost = true;
                        assertTrue(bank.playing(Sfx.ROOK_HIT) > 0, "the three hits");
                        assertTrue(bank.playing(Sfx.ROOK_SPIN) > 0, "the lamp's flicker and spin-down");
                    }
                    default -> {}
                }
            }
            lure |= onScreen(sortie, "glow-angler") && bank.looping(Sfx.LURE_HUM);
            crown |= onScreen(sortie, "spiral-nautilus") && bank.looping(Sfx.CROWN_HUM);
            if (!onScreen(sortie, "glow-angler") && bank.looping(Sfx.LURE_HUM)) {
                hushing++;
            }
        }
        assertEquals(2, clicks, "the headlights on, the lamps out");
        assertTrue(hushing < SimStep.PER_SECOND * 30, "the hum only fades out after the last Angler: " + hushing);
        assertTrue(lure, "the lure's hum");
        assertTrue(lunge, "a lunge");
        assertTrue(crown, "the crown's hum");
        assertTrue(trails, "Rook's trails");
        assertTrue(lost, "Rook's loss");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.LURE_HUM), "the hums stop with the flight");
        assertFalse(bank.looping(Sfx.CROWN_HUM));
    }

    private static boolean onScreen(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (enemy.spec().slug().equals(slug) && y > 0 && y < PlayField.HEIGHT && x > 0 && x < PlayField.WIDTH) {
                return true;
            }
        }
        return false;
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
