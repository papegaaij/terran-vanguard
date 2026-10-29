package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Loadout;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part E (step E4), Level 32's sounds (design/campaign Level 32, Music &amp; ambience; design/audio/sfx, Act 5
 * sounds; step AUD-E's round 65 proposals, their a variants until the picks): track 12 "Hollow Rock" with its motif from
 * t=18, the tunnel bed (round 60 S.19 a) with the foundry's roar under the refinery; flown, the Sentinel Towers' charge,
 * beam and servo (never the Mantis's), the Striders' footfalls and cannons.
 */
class Level32SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(32).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theTunnelBedTheFoundrysRoarAndTheThemesStemsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(12, data.track(), "Act 5 A, Hollow Rock");
        String theme = Tracks.name(data.track()).orElseThrow();
        assertEquals("hollow-rock", theme);
        for (String file : new String[] {Tracks.path(theme), Tracks.basePath(theme), Tracks.motifPath(theme)}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(Sfx.AMBIENCE_BELT_TUNNEL, Sfx.ambience(data.ambience()));
        assertEquals(
                Sfx.AMBIENCE_MARS_FOUNDRY,
                Sfx.ambience(data.ambienceLayers().orElseThrow().getFirst().ambience()),
                "the refinery's furnace roar");
        for (Sfx sfx : new Sfx[] {
            Sfx.AMBIENCE_BELT_TUNNEL,
            Sfx.SENTINEL_CHARGE,
            Sfx.SENTINEL_BEAM,
            Sfx.SENTINEL_SERVO,
            Sfx.SENTINEL_DEATH,
            Sfx.STRIDER_STEP,
            Sfx.STRIDER_SERVO,
            Sfx.STRIDER_CANNON,
            Sfx.STRIDER_SNAP,
            Sfx.STRIDER_FALL,
            Sfx.DOOR_GRIND,
            Sfx.CHARGE_BLAST
        }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.name());
        }
    }

    @Test
    void aStriderStepsTwiceAStrideAndTheServoDropsOnAFullTurn() {
        assertEquals(0, DeepCoreCues.footfall(15.9, 32));
        assertEquals(1, DeepCoreCues.footfall(16, 32));
        assertEquals(2, DeepCoreCues.footfall(32, 32));
        assertEquals(DeepCoreCues.FULL_TURN_SERVO_PITCH, DeepCoreCues.servoPitch(true));
        assertEquals(1f, DeepCoreCues.servoPitch(false));
    }

    /**
     * Flown on medium to t=100 holding the left channel (x 140), invulnerable and not firing: the gallery tower (t=24) and the
     * hall pair (t≈79) charge and sweep with the Sentinel's sounds, never the Mantis's; the Striders (t=36) step.
     */
    @Test
    void theSentinelsAndStridersSoundTheirOwn() {
        Loadout loadout = SimSpecs.loadout(
                CONTENT,
                CONTENT.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 1)),
                CONTENT.shields().models().get(2).name(),
                CONTENT.armour().plating().get(4).name(),
                0,
                Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                loadout.plating().maxArmour());
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean charge = false;
        boolean beam = false;
        boolean servo = false;
        boolean step = false;
        boolean mantis = false;
        while (!sortie.complete() && sortie.levelSeconds() < 100) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie, 140));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            charge |= bank.playing(Sfx.SENTINEL_CHARGE) > 0;
            beam |= bank.looping(Sfx.SENTINEL_BEAM);
            servo |= bank.looping(Sfx.SENTINEL_SERVO);
            step |= bank.playing(Sfx.STRIDER_STEP) > 0;
            mantis |= bank.playing(Sfx.MANTIS_TELEGRAPH) > 0 || bank.playing(Sfx.MANTIS_SWEEP) > 0;
        }
        sounds.stopLoops();
        assertTrue(charge, "a Sentinel's charge");
        assertTrue(beam && servo, "its beam and servo loops");
        assertTrue(step, "a Strider's footfalls");
        assertFalse(mantis, "never the Mantis's");
        assertFalse(bank.looping(Sfx.SENTINEL_BEAM), "the loops stop with the flight");
    }

    private static int steer(Sortie sortie, double targetX) {
        int commands = 0;
        if (sortie.ship().x() < targetX - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > targetX + 3) {
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
