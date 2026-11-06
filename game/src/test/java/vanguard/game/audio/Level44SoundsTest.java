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
 * M11 part B (step B4), Level 44's sounds (design/campaign Level 44, Music &amp; ambience; design/audio/sfx, Act 7
 * sounds; round 84's picks): track 17 "Hollow Light" (its intro and base over the far mouth, the full mix from the
 * current, the base again out of the gate), the {@code vrell-gate} bed with the {@code event-plane} layer over the whole
 * level, the theme ducked 3 dB under each reversal; flown, the Rift Skaters' bursts once a burst, and each reversal's
 * groan over its own run (4 s, then 5 s), cut at its end.
 */
class Level44SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(44).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBedsAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(17, data.track(), "Act 7 B, Hollow Light");
        assertEquals("hollow-light", Tracks.name(17).orElseThrow());
        for (String file : new String[] {Tracks.path(17), Tracks.basePath("hollow-light")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(1, data.startSection(), "the intro and the base over the far mouth");
        assertEquals(2, data.fullSection(), "the full mix from the current");
        assertEquals(Sfx.AMBIENCE_VRELL_GATE, Sfx.ambience(data.ambience()));
        LevelData.Music.AmbienceLayer plane =
                data.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AMBIENCE_EVENT_PLANE, Sfx.ambience(plane.ambience()));
        assertEquals(1, plane.level(100), 1e-9, "the event plane's layer through the level");
        LevelData.Music.Duck duck = data.duck().orElseThrow();
        assertEquals(LevelData.Music.DuckOn.REVERSAL, duck.on());
        assertEquals(-3, duck.db(), 1e-9, "3 dB under the groan");
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_VRELL_GATE, "vrell-gate-r89-a.ogg",
                Sfx.AMBIENCE_EVENT_PLANE, "event-plane-r84-b.ogg",
                Sfx.SKATER_BURST, "skater-burst-r84-a.ogg",
                Sfx.REVERSAL_GROAN, "reversal-groan-r84-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /** The groan's level over a 4 s run: full, then down over the last 0.5 s, none from the run's end. */
    @Test
    void theGroanIsCutToItsRunWithAHalfSecondFade() {
        int run = SimStep.ticks(4);
        assertEquals(1, GateCues.groanLevel(0, run), 1e-6);
        assertEquals(1, GateCues.groanLevel(SimStep.ticks(3.5), run), 1e-6);
        assertEquals(0.5, GateCues.groanLevel(SimStep.ticks(3.75), run), 1e-3);
        assertEquals(0, GateCues.groanLevel(run, run), 1e-6);
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable low in the middle, the beam firing: the Skaters' bursts, and each
     * reversal's groan looping only through its run (the first from t=100 for 4 s, the second from t=170 for 5 s).
     */
    @Test
    void theCrossingSoundsItsOwn() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean burst = false;
        int groanSteps = 0;
        boolean[] groaned = new boolean[2];
        int last = SimStep.ticks(185);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            burst |= bank.playing(Sfx.SKATER_BURST) > 0;
            if (bank.looping(Sfx.REVERSAL_GROAN)) {
                groanSteps++;
                double t = sortie.levelSeconds();
                assertTrue((t >= 99.9 && t < 104.2) || (t >= 169.9 && t < 175.2), "a groan only in a run: t=" + t);
                groaned[t < 140 ? 0 : 1] = true;
            }
        }
        sounds.stopLoops();
        assertTrue(burst, "a Skater's burst");
        assertTrue(groaned[0] && groaned[1], "both reversals groan");
        assertEquals(SimStep.ticks(9), groanSteps, SimStep.ticks(0.2), "4 s and 5 s of groan");
        assertFalse(bank.looping(Sfx.REVERSAL_GROAN), "the groan stops");
        bank.dispose();
    }

    /** Low in the middle of the field. */
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
