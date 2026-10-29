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
 * M9 part F (step F4), Level 33's sounds (design/campaign Level 33, Music &amp; ambience; design/audio/sfx, Act 5
 * sounds; step AUD-F's round 66 proposals, their a variants until the picks, and round 60's rail picks): track 13
 * "Gilded Cage" without its motif, the open-space bed; the latched leeches' hum by their count; flown, the Rail
 * Bunker's charge and shot, a leech's own latch (never the Lamprey's) and its hum while it drains.
 */
class Level33SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(33).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theOpenBedTheThemesStemsAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(13, data.track(), "Act 5 B, Gilded Cage");
        String theme = Tracks.name(data.track()).orElseThrow();
        assertEquals("gilded-cage", theme);
        for (String file : new String[] {Tracks.path(theme), Tracks.basePath(theme)}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(Sfx.AMBIENCE_BELT_OPEN, Sfx.ambience(data.ambience()));
        for (Sfx sfx : new Sfx[] {
            Sfx.RAIL_CHARGE,
            Sfx.RAIL_SHOT,
            Sfx.LEECH_LATCH,
            Sfx.LEECH_DRAIN,
            Sfx.LEECH_HUM,
            Sfx.LEECH_SHAKE,
            Sfx.LEECH_DEATH,
            Sfx.BROWNOUT_WARN,
            Sfx.BUNKER_DEATH
        }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.name());
        }
    }

    @Test
    void theHumStepsWithTheLatchedCount() {
        assertEquals(0f, LeechFieldCues.humLevel(0));
        assertEquals(0.5f, LeechFieldCues.humLevel(1), "−6 dB");
        assertEquals(0.71f, LeechFieldCues.humLevel(2), "−3 dB");
        assertEquals(1f, LeechFieldCues.humLevel(3));
        assertEquals(1f, LeechFieldCues.humLevel(4), "a fourth drains nothing more");
    }

    /**
     * Flown on medium to t=50 near the bottom edge's middle, not firing (the plan's front gun only): the first bunker
     * (t=33.5) charges and fires its rail; the rear ambushes latch with the leech's own sounds and hum, never the
     * Lamprey's.
     */
    @Test
    void theRailsAndTheLeechesSoundTheirOwn() {
        Loadout loadout = SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 1)),
                        CONTENT.shields().models().get(2).name(),
                        CONTENT.armour().plating().get(4).name(),
                        0,
                        Difficulty.MEDIUM)
                // The plan's generator at its load (20 of 20 MW): every latched leech drains.
                .withPower(SimSpecs.power(
                        CONTENT, 20, 20, CONTENT.shields().models().get(2).name(), Difficulty.MEDIUM));
        Sortie sortie = new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                loadout.plating().maxArmour());
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean charge = false;
        boolean shot = false;
        boolean latched = false;
        boolean latch = false;
        boolean hum = false;
        boolean lamprey = false;
        while (!sortie.complete() && sortie.levelSeconds() < 50) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie, 240));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            charge |= bank.playing(Sfx.RAIL_CHARGE) > 0;
            shot |= bank.playing(Sfx.RAIL_SHOT) > 0;
            latched |= sortie.latched() > 0;
            latch |= bank.playing(Sfx.LEECH_LATCH) > 0;
            hum |= bank.looping(Sfx.LEECH_HUM);
            lamprey |= bank.playing(Sfx.LAMPREY_LATCH) > 0 || bank.looping(Sfx.LAMPREY_DRAIN);
        }
        sounds.stopLoops();
        assertTrue(charge && shot, "the rail's charge and shot");
        assertTrue(latched, "a leech latched");
        assertTrue(latch, "the leech's latch");
        assertTrue(hum, "the leech's hum");
        assertFalse(lamprey, "never the Lamprey's");
        assertFalse(bank.looping(Sfx.LEECH_HUM), "the hum stops with the flight");
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
