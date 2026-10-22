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
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.render.ThroughIceFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part C's Level 23 sounds (design/audio/sfx, Level 23's rows; round 52's a variants until the user's pick): the
 * waterline's crossing and the hydro-kit's refit once at the crossing; below the waterline every kill on the
 * under-water rung, a Glassfin's glass shatter over it, an eel head's pulse instead of a gun's report; the siphon pumps'
 * hum while one is on the screen; the Europa ice bed in the dry shaft crossfading to the bed under the ice at the
 * waterline; track 10 "Thera Deep" from the waterline, its full mix from the farms.
 */
class Level23SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    private static final List<Sfx> SOUNDS = List.of(
            Sfx.WATERLINE_CROSS,
            Sfx.HYDRO_REFIT,
            Sfx.SCRAPE_UNDER,
            Sfx.EEL_PULSE,
            Sfx.EEL_SEGMENT,
            Sfx.GLASSFIN_SHATTER,
            Sfx.PUMP_HUM,
            Sfx.PUMP_BREAK,
            Sfx.AMBIENCE_EUROPA_UNDER,
            Sfx.EXPLOSION_DEEP_TINY,
            Sfx.EXPLOSION_DEEP_SMALL,
            Sfx.EXPLOSION_DEEP_MEDIUM,
            Sfx.EXPLOSION_DEEP_LARGE);

    @Test
    void everySoundHasItsFileRoundFiftyTwosAAndTheLoopsLoop() throws IOException {
        for (Sfx sfx : SOUNDS) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r52-a.ogg"), "round 52's a until the pick: " + sfx.path());
        }
        assertEquals(Sfx.AMBIENCE_EUROPA_UNDER, Sfx.ambience("europa-under"));
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_EUROPA_UNDER.priority());
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.AMBIENCE_EUROPA_UNDER.path())))) {
            assertEquals(16, file.frameCount() / (double) file.sampleRate(), 0.05, "the bed's seamless 16 s loop");
        }
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.PUMP_HUM.path())))) {
            assertEquals(4, file.frameCount() / (double) file.sampleRate(), 0.05, "the hum's 4 s loop");
        }
    }

    /** Below the waterline a kill sounds on the under-water rung of its air rung; a unit's own death sound stays. */
    @Test
    void theUnderWaterLadderTakesTheAirRungsPlace() {
        assertEquals(Sfx.EXPLOSION_DEEP_TINY, Sfx.underwater(Sfx.EXPLOSION_TINY_A));
        assertEquals(Sfx.EXPLOSION_DEEP_TINY, Sfx.underwater(Sfx.EXPLOSION_TINY_B));
        assertEquals(Sfx.EXPLOSION_DEEP_SMALL, Sfx.underwater(Sfx.EXPLOSION_SMALL_B));
        assertEquals(Sfx.EXPLOSION_DEEP_MEDIUM, Sfx.underwater(Sfx.EXPLOSION_MEDIUM_A));
        assertEquals(Sfx.EXPLOSION_DEEP_LARGE, Sfx.underwater(Sfx.EXPLOSION_LARGE_B));
        assertEquals(Sfx.EXPLOSION_DEEP_TINY, Sfx.underwater(Sfx.COILWYRM_BURST), "an eel's segment");
        assertEquals(Sfx.EXPLOSION_DEEP_MEDIUM, Sfx.underwater(Sfx.COILWYRM_HEAD_BURST), "an eel's head");
        assertEquals(Sfx.EXPLOSION_HUGE_A, Sfx.underwater(Sfx.EXPLOSION_HUGE_A), "huge waits for the Maw");
        assertEquals(Sfx.RIG_DEATH, Sfx.underwater(Sfx.RIG_DEATH));
        for (Sfx deep : List.of(
                Sfx.EXPLOSION_DEEP_TINY,
                Sfx.EXPLOSION_DEEP_SMALL,
                Sfx.EXPLOSION_DEEP_MEDIUM,
                Sfx.EXPLOSION_DEEP_LARGE)) {
            assertEquals(Sfx.Priority.EXPLOSION, deep.priority());
        }
    }

    @Test
    void theMusicIsTheraDeepFromTheWaterlineOverTheBedUnderTheIce() {
        LevelData.Music music = ThroughIceFixture.level().music();
        assertEquals(10, music.track());
        assertEquals(2, music.startSection(), "the ice bed alone in the dry shaft, the base stem from the waterline");
        assertFalse(music.full(3));
        assertTrue(music.full(4), "the full mix from the farms");
        assertEquals("europa-ice", music.ambience());
        List<LevelData.Music.AmbienceChange> changes = music.ambienceChangeList();
        assertEquals(1, changes.size());
        assertEquals(2, changes.getFirst().section(), "at the waterline");
        assertEquals(Sfx.AMBIENCE_EUROPA_UNDER, Sfx.ambience(changes.getFirst().ambience()));
        assertEquals(
                Sfx.AMBIENCE_EUROPA_ICE,
                Sfx.ambience(IceShelfFixture.level().music().ambience()));
    }

    /**
     * Flown through the shaft to the farms with the starter loadout, over the pumps: the crossing and the refit play
     * once as the ship drops into the water; before it a kill sounds on the air rung, after it on the under-water rung
     * (never the air's); a Glassfin's kill shatters; an eel head's volley pulses; the pumps hum while one is on the
     * screen; the loop stops with the flight.
     */
    @Test
    void theWaterlineTheKillsTheEelsAndThePumpsSound() {
        Sortie sortie = ThroughIceFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        int crossings = 0;
        boolean dryKill = false;
        boolean deepKill = false;
        boolean shatter = false;
        boolean pulse = false;
        boolean hum = false;
        double crossed = Double.POSITIVE_INFINITY;
        while (sortie.levelSeconds() < 120 && !(deepKill && shatter && pulse && hum && dryKill && crossings > 0)) {
            Enemy pump = ThroughIceFixture.first(sortie, "siphon-pump");
            boolean wasUnder = sortie.underwater();
            step(
                    sortie,
                    sounds,
                    pump == null
                            ? IceShelfFixture.toward(sortie, 240, 100)
                            : IceShelfFixture.toward(sortie, pump.renderX(1), 100));
            if (!wasUnder && sortie.underwater()) {
                crossings++;
                crossed = sortie.levelSeconds();
                assertTrue(bank.playing(Sfx.WATERLINE_CROSS) > 0, "the crossing's swell");
                assertTrue(bank.playing(Sfx.HYDRO_REFIT) > 0, "the refit's clamps");
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ENEMY_DESTROYED) {
                    String slug = sortie.enemyKinds().get(events.value(e)).slug();
                    if (sortie.underwater()) {
                        deepKill = true;
                        assertTrue(bank.playing(Sfx.EXPLOSION_DEEP_SMALL) > 0, "the under-water rung: " + slug);
                        if (slug.equals("glassfin")) {
                            shatter = true;
                            assertTrue(bank.playing(Sfx.GLASSFIN_SHATTER) > 0, "the glass shatter");
                        }
                    } else {
                        dryKill = true;
                        assertTrue(bank.playing(Sfx.EXPLOSION_SMALL_A) + bank.playing(Sfx.EXPLOSION_SMALL_B) > 0, slug);
                    }
                }
            }
            pulse |= bank.playing(Sfx.EEL_PULSE) > 0;
            if (sortie.levelSeconds() > crossed + 3) {
                assertEquals(
                        0,
                        bank.playing(Sfx.EXPLOSION_SMALL_A) + bank.playing(Sfx.EXPLOSION_SMALL_B),
                        "no air rung under water");
            }
            if (pump != null && pump.renderY(1) < 500) {
                hum |= bank.looping(Sfx.PUMP_HUM);
            }
        }
        assertEquals(1, crossings, "one crossing");
        assertTrue(dryKill, "a kill in the dry shaft");
        assertTrue(deepKill, "a kill under water");
        assertTrue(shatter, "a Glassfin shattered");
        assertTrue(pulse, "an eel head's pulse");
        assertTrue(hum, "the pumps' hum");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.PUMP_HUM));
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
