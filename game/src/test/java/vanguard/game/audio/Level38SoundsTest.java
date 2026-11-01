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
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.AtmosphereRamp;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part D (step D4), Level 38's sounds (design/campaign Level 38, Music &amp; ambience; design/enemies/air/harrow;
 * design/audio/sfx, Act 6 sounds): track 15 "Storm Wall", its intro over the drop, the base from section 2 and the full
 * mix from section 3; the {@code jovian-sky} bed 3 dB louder in the storm sections (2-4, a 1 s ramp at each section's
 * start); flown, the Harrow's hunting whistle (the nearest hunting one, pitched up over its chase, gone at its tell), its
 * lock tell (round 72 S.5 b), its ram (round 76 a) and its burst on every death (round 72 S.6 a, in its rung's place).
 */
class Level38SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(38).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBedAndTheHarrowsSoundsAreThere() {
        LevelData level = CONTENT.level(KEY);
        LevelData.Music data = level.music();
        assertEquals(15, data.track(), "Act 6 B, Storm Wall");
        assertEquals(1, data.startSection(), "its intro over the drop");
        assertEquals(3, data.fullSection(), "the full mix from the cells");
        assertEquals(Sfx.AMBIENCE_JOVIAN_SKY, Sfx.ambience(data.ambience()));
        for (String file : new String[] {Tracks.path(15), Tracks.basePath("storm-wall")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        Map<Sfx, String> files = Map.of(
                Sfx.HARROW_LOCK, "harrow-lock-r72-b.ogg",
                Sfx.HARROW_BURST, "harrow-burst-r72-a.ogg",
                Sfx.HARROW_WHISTLE, "harrow-whistle-r76-a.ogg",
                Sfx.HARROW_RAM, "harrow-ram-r76-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /** The bed's rise by section: 0 under Aurelia, +3 dB from the storm wall (over 1 s) to the breakout, back over 1 s. */
    @Test
    void theBedRisesThreeDecibelsInTheStorm() {
        LevelData level = CONTENT.level(KEY);
        assertEquals(0, AtmosphereRamp.sectionRiseDb(level, 10), 1e-9, "under Aurelia");
        assertEquals(1.5, AtmosphereRamp.sectionRiseDb(level, 22.5), 1e-9, "half way up the ramp");
        assertEquals(3, AtmosphereRamp.sectionRiseDb(level, 30), 1e-9, "the storm wall");
        assertEquals(3, AtmosphereRamp.sectionRiseDb(level, 120), 1e-9, "the Red Spot's rim");
        assertEquals(1.5, AtmosphereRamp.sectionRiseDb(level, 150.5), 1e-9, "half way down at the breakout");
        assertEquals(0, AtmosphereRamp.sectionRiseDb(level, 170), 1e-9, "the breakout");
        LevelData other = CONTENT.level(CONTENT.levelKey(37).orElseThrow());
        assertEquals(0, AtmosphereRamp.sectionRiseDb(other, 120), 1e-9, "no rise without ambience_db");
    }

    /** The whistle rises from 0.85 to 1.25 over a chase's middle length (2 s for the Harrow's 1.5-2.5 s). */
    @Test
    void theWhistleRisesOverTheChase() {
        EnemySpec harrow = sortie().enemyKinds().stream()
                .filter(kind -> kind.slug().equals("harrow"))
                .findFirst()
                .orElseThrow();
        assertTrue(HarrowCues.rams(harrow), "the Harrow rams");
        assertEquals(0, HarrowCues.chaseShare(harrow, 0), 1e-9);
        assertEquals(0.5, HarrowCues.chaseShare(harrow, SimStep.ticks(1)), 1e-6);
        assertEquals(HarrowCues.WHISTLE_FROM, HarrowCues.whistlePitch(0), 1e-6);
        assertEquals(HarrowCues.WHISTLE_TO, HarrowCues.whistlePitch(1), 1e-6);
        assertEquals(HarrowCues.WHISTLE_TO, HarrowCues.whistlePitch(1.4), 1e-6, "held at the top");
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable low on the field, the Ion Beam firing: the whistle loops while a Harrow
     * hunts, a Harrow's lock and its ram sound (its burst is its death sound: Level38LooksTest); the Buzzsaws keep their spin-up (not the lock); the whistle
     * stops with the flight.
     */
    @Test
    void theHarrowsSoundTheirOwn() {
        Sortie sortie = sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean whistle = false;
        boolean lock = false;
        boolean ram = false;
        boolean spinUp = false;
        int last = SimStep.ticks(100);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            whistle |= bank.looping(Sfx.HARROW_WHISTLE);
            lock |= bank.playing(Sfx.HARROW_LOCK) > 0;
            ram |= bank.playing(Sfx.HARROW_RAM) > 0;
            spinUp |= bank.playing(Sfx.SAW_SPINUP) > 0;
            if (sortie.levelSeconds() < 30) {
                assertFalse(bank.looping(Sfx.HARROW_WHISTLE), "no Harrow before t=32");
            }
        }
        sounds.stopLoops();
        assertTrue(whistle, "the nearest hunting Harrow's whistle");
        assertTrue(lock, "a Harrow's lock tell");
        assertTrue(ram, "a Harrow's ram");
        assertTrue(spinUp, "the Buzzsaws' spin-up (t=62 on)");
        assertFalse(bank.looping(Sfx.HARROW_WHISTLE), "the whistle stops with the flight");
        bank.dispose();
    }

    /** Medium, the ship invulnerable with the Ion Beam. */
    private static Sortie sortie() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** Low on the field, under the middle. */
    private static int steer(Sortie sortie) {
        int commands = 0;
        if (sortie.ship().x() < 237) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > 243) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < 77) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > 83) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
