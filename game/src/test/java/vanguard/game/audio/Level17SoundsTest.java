package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part D's dome sounds (design/audio/sfx, Level 17 Dome Siege): round 40's proposals (a each) until the pick, a
 * dome's hit and break, the {@code mars-colony} ambience from the start; a hooked bomber's drop reuses the Bomb
 * Rack's falling-bomb whistle and the militia's flak the frigate's. Step DG2: the Choir Herald's song for its ring,
 * the aura loop while any ring is up, a ring's absorb (spaced out) and the rings' collapse; the Shellback's walk
 * loop, its lob, curl, roll loop and uncurl, on hand-built sorties ({@link SiegeUnitsFixture}).
 */
class Level17SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(17).orElseThrow();
    private static final LevelData LEVEL = CONTENT.level(KEY);

    @Test
    void everySoundHasItsFile() {
        for (Sfx sfx : List.of(Sfx.DOME_HIT, Sfx.DOME_BREAK, Sfx.AMBIENCE_MARS_COLONY)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r40-a.ogg"), sfx + ": round 40's proposal a");
        }
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_MARS_COLONY.priority());
        assertTrue(Files.isRegularFile(ASSETS.resolve(Sfx.BOMB_SHOT.path())), "the reused whistle");
        assertTrue(Files.isRegularFile(ASSETS.resolve(Sfx.FRIGATE_FLAK.path())), "the reused flak");
    }

    @Test
    void theColonysAmbienceFromTheStart() {
        assertEquals(Sfx.AMBIENCE_MARS_COLONY, Sfx.ambience(LEVEL.music().ambience()));
        assertEquals(null, Sfx.afterBoss(Sfx.AMBIENCE_MARS_COLONY), "it plays on to the end");
    }

    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everyUnitSoundHasItsFile() {
        for (Sfx sfx : List.of(
                Sfx.HERALD_SONG,
                Sfx.HERALD_AURA,
                Sfx.AURA_ABSORB,
                Sfx.AURA_COLLAPSE,
                Sfx.SHELLBACK_WALK,
                Sfx.SHELLBACK_LOB,
                Sfx.SHELLBACK_CURL,
                Sfx.SHELLBACK_ROLL,
                Sfx.SHELLBACK_UNCURL)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r40-a.ogg"), sfx + ": round 40's proposal a");
        }
        assertEquals(Sfx.AURA_COLLAPSE, FlightSounds.siegeSound(SimEvents.Type.RINGS_COLLAPSED));
        assertEquals(Sfx.AURA_COLLAPSE, FlightSounds.siegeSound(SimEvents.Type.RINGS_STRIPPED), "the EMP's strip too");
        assertEquals(Sfx.SHELLBACK_CURL, FlightSounds.siegeSound(SimEvents.Type.CURLED));
        assertEquals(Sfx.SHELLBACK_UNCURL, FlightSounds.siegeSound(SimEvents.Type.UNCURLED));
        assertNull(FlightSounds.siegeSound(SimEvents.Type.ROLL_STARTED), "the roll is a loop");
        assertEquals(1f, FlightSounds.UNIT_LOOP_LEVEL, "loops at 1.0 (round 39 B.4d)");
        assertEquals(SimStep.ticks(0.2), FlightSounds.UNIT_FADE_STEPS, "with 0.2 s fades");
    }

    @Test
    void anAbsorbPingsAtMostOnceInAFewSteps() {
        assertTrue(FlightSounds.absorbDue(Long.MIN_VALUE / 2, 0), "the first");
        assertFalse(FlightSounds.absorbDue(100, 100 + FlightSounds.ABSORB_GAP_STEPS - 1));
        assertTrue(FlightSounds.absorbDue(100, 100 + FlightSounds.ABSORB_GAP_STEPS));
        assertTrue(FlightSounds.absorbDue(100, 5), "after a restart the clock starts again");
    }

    @Test
    void theHeraldSingsItsAuraHumsWhileARingIsUpAndItsRingsCollapseAtItsDeath() {
        Sortie sortie = SiegeUnitsFixture.herald();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        assertTrue(until(sortie, sounds, Command.NONE, 10, type -> type == SimEvents.Type.ENEMY_FIRED), "it sang");
        assertTrue(bank.playing(Sfx.HERALD_SONG) > 0, "its song, not a gun's report");
        assertEquals(0, bank.playing(Sfx.ENEMY_SHOT_A) + bank.playing(Sfx.ENEMY_SHOT_B));
        Enemy target = null;
        while (sortie.levelSeconds() < 12 && (target == null || target.ringShare() < 1)) {
            step(sortie, sounds, Command.NONE);
            target = SiegeUnitsFixture.find(sortie, SiegeUnitsFixture.TARGET);
        }
        assertTrue(target != null && target.ringShare() >= 1, "the target is ringed");
        assertTrue(bank.looping(Sfx.HERALD_AURA), "the aura's shimmer while a ring is up");
        // Under the target, the ship's shots spend its ring.
        int right = Command.of(Command.RIGHT);
        while (sortie.ship().renderX(1) < SiegeUnitsFixture.TARGET_X) {
            step(sortie, sounds, right);
        }
        assertTrue(until(sortie, sounds, Command.of(Command.FIRE), 3, type -> type == SimEvents.Type.RING_ABSORBED));
        assertTrue(bank.playing(Sfx.AURA_ABSORB) > 0, "the glassy ping");
        // Back under the Herald, the ship's fire kills it: its rings collapse.
        int left = Command.of(Command.LEFT);
        while (sortie.ship().renderX(1) > SiegeUnitsFixture.TARGET_X - 60) {
            step(sortie, sounds, left);
        }
        assertTrue(until(sortie, sounds, Command.of(Command.FIRE), 8, type -> type == SimEvents.Type.RINGS_COLLAPSED));
        assertTrue(bank.playing(Sfx.AURA_COLLAPSE) > 0, "the falling chord");
        run(sortie, sounds, Command.NONE, SimStep.ticks(0.3) + FlightSounds.UNIT_FADE_STEPS + 2);
        assertFalse(bank.looping(Sfx.HERALD_AURA), "no ring is up: faded out");
        sounds.stopLoops();
    }

    @Test
    void theShellbackWalksLobsCurlsRollsAndUncurls() {
        Sortie sortie = SiegeUnitsFixture.shellback();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        assertTrue(until(sortie, sounds, Command.NONE, 10, type -> type == SimEvents.Type.MORTAR_LOBBED), "it lobbed");
        assertTrue(bank.playing(Sfx.SHELLBACK_LOB) > 0, "its own lob");
        assertEquals(0, bank.playing(Sfx.MORTAR_LOB), "not the Polyp Mortar's");
        assertTrue(bank.looping(Sfx.SHELLBACK_WALK), "its walk while it walks on the screen");
        int fire = Command.of(Command.FIRE);
        assertTrue(until(sortie, sounds, fire, 20, type -> type == SimEvents.Type.CURLED), "it curled");
        assertTrue(bank.playing(Sfx.SHELLBACK_CURL) > 0, "the plates grinding shut");
        assertTrue(until(sortie, sounds, fire, 2, type -> type == SimEvents.Type.ROLL_STARTED));
        run(sortie, sounds, fire, FlightSounds.UNIT_FADE_STEPS + 2);
        assertTrue(bank.looping(Sfx.SHELLBACK_ROLL), "the rolling grind");
        assertFalse(bank.looping(Sfx.SHELLBACK_WALK), "and no walk: it is the nearest Shellback, rolling");
        assertTrue(until(sortie, sounds, fire, 4, type -> type == SimEvents.Type.UNCURLED), "it uncurled");
        assertTrue(bank.playing(Sfx.SHELLBACK_UNCURL) > 0);
        run(sortie, sounds, Command.NONE, FlightSounds.UNIT_FADE_STEPS + 2);
        assertFalse(bank.looping(Sfx.SHELLBACK_ROLL), "the roll faded out");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.SHELLBACK_WALK));
    }

    private void run(Sortie sortie, FlightSounds sounds, int commands, int steps) {
        for (int t = 0; t < steps; t++) {
            step(sortie, sounds, commands);
        }
    }

    /** Steps with {@code commands} for at most {@code seconds} until an event of the kind shows; whether it did. */
    private boolean until(
            Sortie sortie, FlightSounds sounds, int commands, double seconds, Predicate<SimEvents.Type> wanted) {
        for (int t = 0; t < SimStep.ticks(seconds); t++) {
            SimEvents events = step(sortie, sounds, commands);
            for (int i = 0; i < events.size(); i++) {
                if (wanted.test(events.type(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private SimEvents step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
        return sortie.events();
    }
}
