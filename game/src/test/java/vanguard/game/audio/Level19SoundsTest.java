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
import vanguard.game.render.FoundryFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.GroundSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part F's Level 19 sounds (design/audio/sfx, Warden Tank, Furnace vent, Conveyor belts; the {@code mars-foundry}
 * ambience): round 42's proposals (a each) until the pick, flown in the level at medium ({@link FoundryFixture}): the
 * Warden Tank's tracks loop while one is on the screen, its turret's whine as it stops on the ship, its burst once for
 * the burst's shots instead of their reports; the belts' quiet rumble while a belt runs on the screen; each grate's
 * rising roar with its tell and its fire blast with its flame; the loops stop with the flight. Its death is its
 * explosion's, the medium rung's Warden Tank entry.
 */
class Level19SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everySoundHasItsFileAndTheBedIsTheFoundrys() throws IOException {
        for (Sfx sfx : List.of(
                Sfx.TANK_TRACKS,
                Sfx.TANK_TURRET,
                Sfx.TANK_BURST,
                Sfx.TANK_DEATH,
                Sfx.VENT_TELL,
                Sfx.VENT_BURST,
                Sfx.BELT_RUMBLE,
                Sfx.AMBIENCE_MARS_FOUNDRY)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r42-a.ogg"), sfx + ": round 42's proposal a");
        }
        assertEquals(Sfx.AMBIENCE_MARS_FOUNDRY, Sfx.ambience("mars-foundry"));
        assertEquals("mars-foundry", FoundryFixture.level().music().ambience(), "Level 19's bed");
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_MARS_FOUNDRY.priority());
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.AMBIENCE_MARS_FOUNDRY.path())))) {
            assertEquals(16, file.frameCount() / (double) file.sampleRate(), 0.05, "a seamless 16 s loop");
        }
        assertEquals(Sfx.Priority.EXPLOSION, Sfx.TANK_DEATH.priority(), "on the explosion rung");
        assertEquals(1f, FoundryCues.FOUNDRY_LEVEL, "levelled in their files");
    }

    @Test
    void aTurretWhinesAtMostOnceForTurretsLockingTogether() {
        assertTrue(FoundryCues.slewDue(Long.MIN_VALUE / 2, 0), "the first");
        assertFalse(FoundryCues.slewDue(100, 100 + FoundryCues.SLEW_GAP_STEPS - 1), "a convoy locking together");
        assertTrue(FoundryCues.slewDue(100, 100 + FoundryCues.SLEW_GAP_STEPS));
        assertTrue(FoundryCues.slewDue(100, 5), "after a restart the clock starts again");
    }

    @Test
    void aBeltIsOnTheScreenWhileSomeOfItsRunIs() {
        GroundSpec.Belt belt = new GroundSpec.Belt(140, 48, 1000, 2000, 40);
        assertFalse(FoundryCues.onScreen(belt, 700), "its start still above the top edge");
        assertTrue(FoundryCues.onScreen(belt, 800), "entering at the top edge");
        assertTrue(FoundryCues.onScreen(belt, 2200), "leaving at the bottom edge");
        assertFalse(FoundryCues.onScreen(belt, 2300), "gone below");
    }

    /**
     * Flown from t=28 under the halls' first belt: the tracks loop with the first tanks, the belts' rumble from the
     * belts' start, a turret's whine and one burst sound for the burst's shots; then on to the furnace row, where the
     * first row's grates roar with their tells and blast with their flames; the loops stop with the flight.
     */
    @Test
    void theTanksTheBeltsAndTheVentsSound() {
        Sortie sortie = FoundryFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        while (sortie.levelSeconds() < 28) {
            step(sortie, sounds, FoundryFixture.toward(sortie, 240));
        }
        assertTrue(bank.looping(Sfx.BELT_RUMBLE), "the belts' rumble once a belt runs on the screen");
        boolean tracks = false;
        boolean whine = false;
        boolean burst = false;
        while (sortie.levelSeconds() < 40 && !(tracks && whine && burst)) {
            step(sortie, sounds, FoundryFixture.toward(sortie, 240));
            tracks |= bank.looping(Sfx.TANK_TRACKS);
            whine |= bank.playing(Sfx.TANK_TURRET) > 0;
            if (tankFired(sortie)) {
                burst = true;
                assertTrue(bank.playing(Sfx.TANK_BURST) > 0, "the burst's sound at its shot");
            }
        }
        assertTrue(tracks, "the tracks while a tank is on the screen");
        assertTrue(whine, "the turret's whine as it stops on the ship");
        assertTrue(burst, "a tank's burst");
        boolean tell = false;
        boolean blast = false;
        while (sortie.levelSeconds() < 72 && !(tell && blast)) {
            step(sortie, sounds, Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.VENT_TELL) {
                    tell = true;
                    assertTrue(bank.playing(Sfx.VENT_TELL) > 0, "the grate's rising roar with its glow");
                } else if (events.type(e) == SimEvents.Type.VENT_BURST) {
                    blast = true;
                    assertTrue(bank.playing(Sfx.VENT_BURST) > 0, "the fire blast with its flame");
                }
            }
        }
        assertTrue(tell && blast, "the furnace row's first vents fired");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.TANK_TRACKS));
        assertFalse(bank.looping(Sfx.BELT_RUMBLE));
    }

    /** A tank's burst sounds once, at its first shot; its later shots (0.15 s apart) are in that sound. */
    @Test
    void aTanksBurstSoundsOnceForItsShots() {
        Sortie sortie = FoundryFixture.sortie();
        Enemy tank = null;
        while (tank == null && sortie.levelSeconds() < 40) {
            sortie.step(Command.NONE);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                if (sortie.enemy(i).spec().turret().isPresent()) {
                    tank = sortie.enemy(i);
                }
            }
        }
        assertTrue(tank != null, "a Warden Tank came");
        FoundryCues cues = new FoundryCues(new SoundMix(bank));
        cues.begin(1000);
        assertTrue(cues.fired(tank, 0), "a tank's shot is the foundry's");
        assertEquals(1, bank.playing(Sfx.TANK_BURST));
        cues.begin(1000 + SimStep.ticks(0.3));
        assertTrue(cues.fired(tank, 0));
        assertEquals(1, bank.playing(Sfx.TANK_BURST), "its third shot: still the one sound");
        cues.begin(1000 + FoundryCues.BURST_STEPS);
        cues.fired(tank, 0);
        assertEquals(2, bank.playing(Sfx.TANK_BURST), "the next burst");
        assertFalse(cues.fired(null, 0), "no unit found: the shot plays as ever");
    }

    /** Whether one of this step's shots left a turret unit. */
    private static boolean tankFired(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) != SimEvents.Type.ENEMY_FIRED) {
                continue;
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().turret().isPresent()
                        && Math.hypot(enemy.renderX(1) - events.x(e), enemy.renderY(1) - events.y(e)) < 30) {
                    return true;
                }
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
