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
import org.junit.jupiter.api.Test;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.JovianFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;

/**
 * M11 part A, step A3a: the sounds of Act 7's unit mechanics and Vrell space (design/audio/sfx, round 84's picks): the
 * window shell's grind and tick, the jump's swoosh and its reversed landing, the reversal's two-tone tell play their
 * files; one family per event, the shells' ticks throttled over all units.
 */
class ActSevenSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theShellsTheJumpsAndTheReversalPlayRoundEightyFoursPicks() {
        Map<Sfx, String> picks = Map.of(
                Sfx.SHELL_GRIND, "shell-grind-r84-a.ogg",
                Sfx.SHELL_TICK, "shell-tick-r84-b.ogg",
                Sfx.JUMP_OUT, "jump-out-r84-b.ogg",
                Sfx.ARRIVE, "arrive-r84-b.ogg",
                Sfx.REVERSAL_TELL, "reversal-tell-r84-a.ogg");
        picks.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertTrue(Sfx.JUMP_OUT.instanceLimit() >= 4, "at most four rings on the screen");
        bank.dispose();
    }

    @Test
    void everyActSevenEventHasOneFamily() {
        // FlightSounds throws when two families claim one event.
        new FlightSounds(bank, new EnemyLooks[0], JovianFixture.arcFlight().armament(), List.of());
        GateCues gate = new GateCues(new SoundMix(bank));
        for (SimEvents.Type type : List.of(
                SimEvents.Type.SHELL_OPEN,
                SimEvents.Type.SHELL_CLOSE,
                SimEvents.Type.SHELL_TICK,
                SimEvents.Type.JUMP_OUT,
                SimEvents.Type.ARRIVE)) {
            assertTrue(gate.types().contains(type), type.name());
        }
        assertFalse(gate.types().contains(SimEvents.Type.SHELL_FLAKE), "a flake is a look; its sound is the tick");
        assertTrue(new VrellCues(new SoundMix(bank)).types().contains(SimEvents.Type.REVERSAL_TOLD));
        bank.dispose();
    }

    @Test
    void theShellsTicksAreThrottledOverAllUnits() {
        long gap = SimStep.ticks(GateCues.TICK_GAP_SECONDS);
        assertTrue(gap >= 1 && gap < SimStep.ticks(0.15), "under the per-unit 0.15 s: " + gap);
        assertTrue(JovianCues.due(-gap, 0, GateCues.TICK_GAP_SECONDS));
        assertFalse(JovianCues.due(-gap + 1, 0, GateCues.TICK_GAP_SECONDS));
        bank.dispose();
    }
}
