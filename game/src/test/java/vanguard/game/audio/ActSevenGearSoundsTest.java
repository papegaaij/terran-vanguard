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

/**
 * M11 part A, step A3b: the Act 7 gear's and hazards' sounds (design/audio/sfx, rounds 84, 86 and 87): the Choir
 * Resonator's whoosh per wave and its clang per hull (−6 % a hull), the Swarm Call's swell, thorns and burst, the spore
 * cloud's rustle loop and crackle, the boss rush's refill chime; each event one family, the music's tracks 27 and 28.
 */
class ActSevenGearSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theGearAndTheCloudsPlayTheRoundsPicks() {
        Map<Sfx, String> picks = Map.of(
                Sfx.RESONATOR_FIRE, "resonator-fire-r87-a.ogg",
                Sfx.RESONATOR_HIT, "resonator-hit-r86-a.ogg",
                Sfx.SWARM_CALL, "swarm-call-r84-b.ogg",
                Sfx.SWARM_DRONE_BURST, "swarm-drone-burst-r84-a.ogg",
                Sfx.SPORE_INSIDE, "spore-inside-r84-b.ogg",
                Sfx.SPORE_BURN, "spore-burn-r84-a.ogg");
        picks.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertEquals("music/ending.ogg", Tracks.path(Tracks.ENDING));
        assertEquals("music/credits.ogg", Tracks.path(Tracks.CREDITS));
        bank.dispose();
    }

    @Test
    void theClangDropsSixPercentAHull() {
        assertEquals(1f, PlayerCues.clangPitch(0));
        assertEquals(0.94f, PlayerCues.clangPitch(1), 1e-6);
        assertEquals(0.94 * 0.94, PlayerCues.clangPitch(2), 1e-6);
        assertEquals(1f, PlayerCues.clangPitch(-1), "never above the first");
    }

    @Test
    void everyEventHasOneFamily() {
        // FlightSounds throws when two families claim one event.
        new FlightSounds(bank, new EnemyLooks[0], JovianFixture.arcFlight().armament(), List.of());
        SoundMix mix = new SoundMix(bank);
        assertTrue(new SwarmCues(mix)
                .types()
                .containsAll(List.of(
                        SimEvents.Type.SWARM_CALLED, SimEvents.Type.SWARM_THORN, SimEvents.Type.SWARM_DRONE_LOST)));
        assertFalse(new SwarmCues(mix).types().contains(SimEvents.Type.SWARM_WITHER), "the wither is a fade");
        assertTrue(new VrellCues(mix)
                .types()
                .containsAll(List.of(
                        SimEvents.Type.SPORE_INSIDE,
                        SimEvents.Type.SPORE_LEFT,
                        SimEvents.Type.SPORE_BURN,
                        SimEvents.Type.BOSS_REFILL)));
        assertTrue(SwarmCues.THORN_LEVEL < FlightSounds.ENEMY_FIRE, "the thorns under the enemies' fire");
        bank.dispose();
    }
}
