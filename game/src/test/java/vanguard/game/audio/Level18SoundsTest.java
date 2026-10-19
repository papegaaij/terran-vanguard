package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.render.StormUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part E's unit sounds (design/audio/sfx, Dust Devil and Ghost Drone): round 41's proposals (a each) until the
 * pick, on hand-built sorties ({@link StormUnitsFixture}): the Dust Devil's spin loop while one is on the screen
 * (faded in and out over 0.2 s), its opening from its rise, its grit spiral's hiss instead of the shots' reports;
 * the Ghost Drone's whine as it drops into its lane, its lock tick with the eye glint and its burst once for the
 * burst's shots. Its death is its explosion's ({@code GhostDroneLooksTest}).
 */
class Level18SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everyUnitSoundHasItsFile() {
        for (Sfx sfx : List.of(
                Sfx.DEVIL_SPIN,
                Sfx.DEVIL_OPEN,
                Sfx.DEVIL_SPIRAL,
                Sfx.GHOST_DRONE_DROP,
                Sfx.GHOST_DRONE_GLINT,
                Sfx.GHOST_DRONE_BURST,
                Sfx.GHOST_DRONE_DEATH)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r41-a.ogg"), sfx + ": round 41's proposal a");
        }
        assertEquals(Sfx.Priority.EXPLOSION, Sfx.GHOST_DRONE_DEATH.priority(), "on the explosion rung");
        assertEquals(1f, StormCues.STORM_LEVEL, "levelled in their files");
    }

    @Test
    void aSpiralHissesOncePerVolley() {
        assertTrue(StormCues.spiralDue(Long.MIN_VALUE / 2, 0), "the first");
        assertFalse(StormCues.spiralDue(100, 100), "the volley's other arms");
        assertTrue(StormCues.spiralDue(100, 100 + SimStep.ticks(0.16)), "the next volley");
        assertTrue(StormCues.spiralDue(100, 5), "after a restart the clock starts again");
    }

    @Test
    void theDevilSpinsOpensWithItsTellAndHissesItsSpiral() {
        Sortie sortie = StormUnitsFixture.devil();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        run(sortie, sounds, Command.NONE, SimStep.ticks(4));
        assertTrue(bank.looping(Sfx.DEVIL_SPIN), "its spin while it is on the screen");
        assertTrue(until(sortie, sounds, 6, type -> type == SimEvents.Type.VORTEX_RISING), "it rose");
        assertTrue(bank.playing(Sfx.DEVIL_OPEN) > 0, "the gust and hollow tone from the tell");
        assertTrue(until(sortie, sounds, 3, type -> type == SimEvents.Type.ENEMY_FIRED), "its spiral fired");
        assertTrue(bank.playing(Sfx.DEVIL_SPIRAL) > 0, "the grit hiss");
        assertEquals(0, bank.playing(Sfx.ENEMY_SHOT_A) + bank.playing(Sfx.ENEMY_SHOT_B), "not a gun's report");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.DEVIL_SPIN));
    }

    @Test
    void theDroneWhinesInTicksWithItsGlintAndFiresOneBurst() {
        Sortie sortie = StormUnitsFixture.drone();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        run(sortie, sounds, Command.NONE, SimStep.ticks(0.5));
        assertTrue(bank.playing(Sfx.GHOST_DRONE_DROP) > 0, "the whine of its drop");
        assertTrue(until(sortie, sounds, 6, type -> type == SimEvents.Type.EYE_GLINT), "its eye glinted");
        assertTrue(bank.playing(Sfx.GHOST_DRONE_GLINT) > 0, "the lock tick");
        assertTrue(until(sortie, sounds, 1, type -> type == SimEvents.Type.ENEMY_FIRED), "its burst's first shot");
        assertEquals(1, bank.playing(Sfx.GHOST_DRONE_BURST), "the burst's sound");
        int shots = 1;
        for (int i = 0; i < SimStep.ticks(0.3); i++) {
            shots += step(sortie, sounds).count(SimEvents.Type.ENEMY_FIRED);
        }
        assertTrue(shots >= 3, "a burst of three shots: " + shots);
        assertEquals(1, bank.playing(Sfx.GHOST_DRONE_BURST), "one burst sound for them all");
        assertEquals(0, bank.playing(Sfx.ENEMY_SHOT_A) + bank.playing(Sfx.ENEMY_SHOT_B), "not the Vrell shots");
    }

    private void run(Sortie sortie, FlightSounds sounds, int commands, int steps) {
        for (int t = 0; t < steps; t++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(commands);
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
        }
    }

    /** Steps for at most {@code seconds} until an event of the kind shows; whether it did. */
    private boolean until(Sortie sortie, FlightSounds sounds, double seconds, Predicate<SimEvents.Type> wanted) {
        for (int t = 0; t < SimStep.ticks(seconds); t++) {
            SimEvents events = step(sortie, sounds);
            for (int i = 0; i < events.size(); i++) {
                if (wanted.test(events.type(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private SimEvents step(Sortie sortie, FlightSounds sounds) {
        run(sortie, sounds, Command.NONE, 1);
        return sortie.events();
    }
}
