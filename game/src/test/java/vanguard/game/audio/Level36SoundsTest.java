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
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part B (step B4), Level 36's sounds (design/campaign Level 36, Music &amp; ambience; design/audio/sfx, Act 6
 * sounds; step AUD-B's round 74 proposals, their a variants until the picks): track 14 "Eye of the Storm" by section
 * (its intro over the dive, the full mix from the harvester fields), the {@code jovian-sky} bed with the dive's wind as
 * a layer over section 1; flown, the fleet's drop craft diving past, the Chimeras' and Talons' whine and twin crack (the
 * Talon's until round 74's QB-2 pick), the harvesters' scoop for the nearest, a harvester's break and a Chimera's death
 * over their rungs, the loops stopped with the flight.
 */
class Level36SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(36).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBedAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(14, data.track(), "Act 6 A, Eye of the Storm");
        assertEquals("eye-of-the-storm", Tracks.name(14).orElseThrow());
        for (String file :
                new String[] {Tracks.path(14), Tracks.basePath("eye-of-the-storm"), Tracks.motifPath("eye-of-the-storm")
                }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(1, data.startSection(), "the theme's intro over the dive");
        assertEquals(3, data.fullSection(), "the full mix from the harvester fields");
        assertEquals(Sfx.AMBIENCE_JOVIAN_SKY, Sfx.ambience(data.ambience()));
        LevelData.Music.AmbienceLayer wind = data.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AMBIENCE_DIVE_WIND, Sfx.ambience(wind.ambience()));
        assertEquals(1, wind.level(10), 1e-9, "the dive's wind over section 1");
        assertTrue(wind.level(24) > 0 && wind.level(24) < 1, "falling away as the dive levels out");
        assertEquals(0, wind.level(30), 1e-9, "gone in the bands");
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_JOVIAN_SKY, "jovian-sky-r72-b.ogg",
                Sfx.AMBIENCE_DIVE_WIND, "dive-wind-r74-a.ogg",
                Sfx.DROP_CRAFT_PASS, "drop-craft-pass-r74-a.ogg",
                Sfx.HARVESTER_SCOOP, "harvester-scoop-r74-a.ogg",
                Sfx.HARVESTER_BREAK, "harvester-break-r74-a.ogg",
                Sfx.CHIMERA_DEATH, "chimera-death-r74-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable low in the middle, the Ion Beam firing: the drop craft's passes (t=4,
     * t=10), the Talons' and Chimeras' whine, a Chimera's death, the harvesters' scoop loop from t=72 and a harvester's
     * break; the loop stopped with the flight.
     */
    @Test
    void theDiveSoundsItsOwn() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
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
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        int passes = 0;
        boolean wasPass = false;
        boolean whine = false;
        boolean scoop = false;
        boolean chimeraDeath = false;
        boolean harvesterBreak = false;
        int last = SimStep.ticks(125);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            boolean pass = bank.playing(Sfx.DROP_CRAFT_PASS) > 0;
            if (pass && !wasPass) {
                passes++;
            }
            wasPass = pass;
            whine |= bank.playing(Sfx.TALON_WHINE) > 0;
            scoop |= bank.looping(Sfx.HARVESTER_SCOOP);
            chimeraDeath |= bank.playing(Sfx.CHIMERA_DEATH) > 0;
            harvesterBreak |= bank.playing(Sfx.HARVESTER_BREAK) > 0;
            if (sortie.levelSeconds() < 70) {
                assertFalse(bank.looping(Sfx.HARVESTER_SCOOP), "no scoop before the harvesters");
            }
        }
        sounds.stopLoops();
        assertEquals(2, passes, "one pass a drop craft pair");
        assertTrue(whine, "the Talons and Chimeras swooping in");
        assertTrue(chimeraDeath, "a Chimera's death");
        assertTrue(scoop, "the scoop for the nearest harvester");
        assertTrue(harvesterBreak, "a harvester's break");
        assertFalse(bank.looping(Sfx.HARVESTER_SCOOP), "the scoop stops with the flight");
        bank.dispose();
    }

    /** Low on the field, under the nearest harvester on the screen (the beam picks it), else in the middle. */
    private static int steer(Sortie sortie) {
        double target = 240;
        double best = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            var enemy = sortie.enemy(e);
            if (enemy.spec().slug().equals(CloudDiverCues.HARVESTER)
                    && enemy.renderY(1) < 600
                    && Math.abs(enemy.renderX(1) - sortie.ship().x()) < best) {
                best = Math.abs(enemy.renderX(1) - sortie.ship().x());
                target = enemy.renderX(1);
            }
        }
        int commands = 0;
        if (sortie.ship().x() < target - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > target + 3) {
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
