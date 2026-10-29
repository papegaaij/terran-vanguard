package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part H (step H4), Level 35's sounds (design/campaign Level 35, Music &amp; ambience; design/audio/sfx, Act 5
 * sounds; step AUD-H2's round 68 proposals, their a variants until the picks, and round 60's pick for the rings' bed):
 * track 13 "Gilded Cage" on the approach, the boss warning and track 19 "Iron Sovereign" (imported as {@code
 * iron-sovereign}) at the arena, track 13's motif alone after the kill; the Sovereign's and the mass relay's sounds by
 * their events; flown, the relay's charge and flash, the rings' spin-up, the klaxon and the grind bed under the fight.
 */
class Level35SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(35).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBossTrackTheMotifAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(13, data.track(), "Act 5 B, Gilded Cage");
        assertEquals(Optional.of("boss-warning"), data.bossWarning());
        assertEquals(Optional.of("iron-sovereign"), data.bossTrack(), "track 19 from the warning");
        assertEquals(Optional.of("iron-sovereign"), Tracks.name(19));
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.path("iron-sovereign"))), "track 19 imported");
        LevelData.Music.MotifAfterBoss motif = data.motifAfterBoss().orElseThrow();
        assertEquals(93, motif.from(), 1e-9, "the motif alone from K+6");
        assertEquals(131.5, motif.to(), 1e-9, "to the level's end");
        for (Sfx sfx : new Sfx[] {
            Sfx.SOVEREIGN_SPINUP,
            Sfx.SOVEREIGN_RING_TURN,
            Sfx.SOVEREIGN_STEP,
            Sfx.SOVEREIGN_HOLD,
            Sfx.SOVEREIGN_LOCK,
            Sfx.SOVEREIGN_KLAXON,
            Sfx.SOVEREIGN_SHUTTERS,
            Sfx.SOVEREIGN_FAN,
            Sfx.SOVEREIGN_SPIRAL,
            Sfx.SOVEREIGN_COOKOFF,
            Sfx.SOVEREIGN_IMPLOSION,
            Sfx.RELAY_CHARGE,
            Sfx.RELAY_FLASH
        }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.name());
        }
        assertEquals("sfx/sovereign-ring-turn-r60-a.ogg", Sfx.SOVEREIGN_RING_TURN.path(), "round 60's pick S.16 a");
    }

    @Test
    void theEventsPlayTheirSounds() {
        assertEquals(Sfx.SOVEREIGN_SPINUP, SovereignCues.sound(SimEvents.Type.RING_LIT));
        assertEquals(Sfx.SOVEREIGN_STEP, SovereignCues.sound(SimEvents.Type.RING_STEP_TELL));
        assertEquals(Sfx.SOVEREIGN_HOLD, SovereignCues.sound(SimEvents.Type.RING_HOLD));
        assertEquals(Sfx.SOVEREIGN_LOCK, SovereignCues.sound(SimEvents.Type.RING_LOCKED));
        assertEquals(Sfx.SOVEREIGN_FAN, SovereignCues.sound(SimEvents.Type.CORE_GLOW));
        assertEquals(Sfx.SOVEREIGN_SHUTTERS, SovereignCues.sound(SimEvents.Type.CORE_OPENING));
        assertEquals(Sfx.RAIL_CHARGE, SovereignCues.sound(SimEvents.Type.BOSS_LINE_TELEGRAPH));
        assertEquals(Sfx.RAIL_SHOT, SovereignCues.sound(SimEvents.Type.BOSS_LINE_FIRED));
        assertEquals(Sfx.ENEMY_SHIELD_BREAK, SovereignCues.sound(SimEvents.Type.PART_SHIELD_BROKEN));
        assertEquals(Sfx.RELAY_FLASH, SovereignCues.sound(SimEvents.Type.RELAY_FIRED));
        assertEquals(0, SovereignCues.relayChargeDelay(1.2), "medium: the charge fills the telegraph");
        assertEquals(
                SimStep.ticks(0.3), SovereignCues.relayChargeDelay(1.5), "easy: 0.3 s in, so it ends at the flash");
    }

    /**
     * Flown on medium with the ship invulnerable near the bottom edge's middle, not firing: the relay's charge and flash
     * (t=59.8, 61); at the arena the rings' spin-up and the klaxon, then the grind bed under the fight (the rings up), the
     * loop stopped with the flight.
     */
    @Test
    void theRelayAndTheRingsSoundTheirOwn() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        List<String> pieces =
                script.setPieces().stream().map(SetPieceSpec::slug).toList();
        FlightSounds sounds = new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), pieces);
        boolean charge = false;
        boolean flash = false;
        boolean spinup = false;
        boolean klaxon = false;
        boolean bed = false;
        int last = SimStep.ticks(92 + 20);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            charge |= bank.playing(Sfx.RELAY_CHARGE) > 0;
            flash |= bank.playing(Sfx.RELAY_FLASH) > 0;
            spinup |= bank.playing(Sfx.SOVEREIGN_SPINUP) > 0;
            klaxon |= bank.playing(Sfx.SOVEREIGN_KLAXON) > 0;
            bed |= bank.looping(Sfx.SOVEREIGN_RING_TURN);
        }
        sounds.stopLoops();
        assertTrue(charge && flash, "the relay's charge and flash");
        assertTrue(spinup && klaxon, "the rings' spin-up and the klaxon");
        assertTrue(bed, "the rings' grind bed");
        assertFalse(bank.looping(Sfx.SOVEREIGN_RING_TURN), "the bed stops with the flight");
    }

    private static int steer(Sortie sortie) {
        int commands = 0;
        if (sortie.ship().x() < 237) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > 243) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < 57) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > 63) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
