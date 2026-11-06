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
import java.util.Map;
import java.util.Optional;
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
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;

/**
 * M11 part C (step C4), Level 45's sounds (design/campaign Level 45, Music &amp; ambience; design/audio/sfx, Act 7
 * sounds; round 84's picks): track 16 "Far Shore" (its base over the rim and the brood fields, the full mix from the
 * living bridge, its {@code choir} stem over the brood heart), the base 6 dB down under the call organ's capture
 * (t=126–138, a timed duck window), the {@code vrell-hive} bed (round 90's a until the user's pick); flown, the pits'
 * tell and opening and the organs' swell, release and tearing on their events; the call organ taken by its own sound
 * in place of the Helix core's hum.
 */
class Level45SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(45).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBedAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(16, data.track(), "Act 7 A, Far Shore");
        assertEquals("far-shore", Tracks.name(16).orElseThrow());
        for (String file :
                new String[] {Tracks.path(16), Tracks.basePath("far-shore"), Tracks.motifPath("far-shore")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(1, data.startSection(), "the base over the rim and the brood fields");
        assertEquals(4, data.fullSection(), "the full mix from the living bridge");
        assertFalse(data.motifOn(3, 200), "no choir before the brood heart");
        assertTrue(data.motifOn(5, 230), "the choir stem over the brood heart");
        assertEquals(Sfx.AMBIENCE_VRELL_HIVE, Sfx.ambience(data.ambience()));
        LevelData.Music.Duck duck = data.duck().orElseThrow();
        assertEquals(LevelData.Music.DuckOn.TIME, duck.on());
        assertEquals(126, duck.at().orElseThrow(), 1e-9);
        assertEquals(-6, duck.db(), 1e-9);
        assertEquals(12, duck.seconds(), 1e-9);
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_VRELL_HIVE, "vrell-hive-r90-a.ogg",
                Sfx.ORGAN_SWELL, "organ-swell-r84-a.ogg",
                Sfx.ORGAN_RELEASE, "organ-release-r84-a.ogg",
                Sfx.ORGAN_DEATH, "organ-death-r84-b.ogg",
                Sfx.PIT_TELL, "pit-tell-r84-a.ogg",
                Sfx.PIT_OPEN, "pit-open-r84-b.ogg",
                Sfx.CALL_ORGAN_TAKEN, "call-organ-taken-r84-b.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /** The theme goes 6 dB down from t=126 for 12 s and back up after, eased at the duck's rate. */
    @Test
    void theThemeDucksUnderTheCapture() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank beds = new SfxBank(audio, files, mixer);
        LevelData.Music.Duck duck = CONTENT.level(KEY).music().duck().orElseThrow();
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("far-shore")),
                        files.internal(Tracks.path(16)),
                        beds,
                        Sfx.AMBIENCE_VRELL_HIVE,
                        1,
                        0,
                        section -> section >= 4,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .timedDuck(duck.at().orElseThrow(), duck.db(), duck.seconds());
        float down = (float) Math.pow(10, -6 / 20.0);
        assertEquals(1, music.timedDuckLevel(125.9), 1e-6);
        assertEquals(down, music.timedDuckLevel(126), 1e-6, "from t=126");
        assertEquals(down, music.timedDuckLevel(137.9), 1e-6);
        assertEquals(1, music.timedDuckLevel(138), 1e-6, "back at t=138");
        double t = 120;
        for (int frame = 0; t < 126; frame++, t += 1 / 60.0) {
            music.update(3, t, 1 / 60f, false);
        }
        assertEquals(1, music.duckLevel(), 1e-3, "full before the window");
        for (; t < 130; t += 1 / 60.0) {
            music.update(3, t, 1 / 60f, false);
        }
        assertEquals(down, music.duckLevel(), 1e-3, "6 dB down inside it");
        for (; t < 137.9; t += 1 / 60.0) {
            music.update(3, t, 1 / 60f, false);
        }
        assertEquals(down, music.duckLevel(), 1e-3, "held to its end");
        for (; t < 142; t += 1 / 60.0) {
            music.update(4, t, 1 / 60f, false);
        }
        assertEquals(1, music.duckLevel(), 1e-3, "up again after it");
        music.dispose();
        beds.dispose();
        bank.dispose();
    }

    /** The call organ is taken by its own sound (not the Helix core's hum over the large salvage); the core keeps it. */
    @Test
    void theCallOrganIsTakenByItsOwnSound() {
        assertEquals(
                List.of("call-organ"),
                CONTENT.level(KEY).pickups().stream()
                        .flatMap(pickup -> pickup.story().stream())
                        .toList());
        assertEquals(
                Sfx.CALL_ORGAN_TAKEN, ReefCues.taken(new StoryPickups.Story("call-organ", 2.0, new Hitbox(64, 40))));
        assertNull(ReefCues.taken(StoryPickups.Story.HELIX_CORE), "Level 34's core keeps its hum");
        assertNull(ReefCues.taken(null));
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable low in the middle to t=40, the coil pod and the beam firing from t=31:
     * the first pit's tell and opening (t=10–12), the first organ's swell and release, and its tearing as it dies.
     */
    @Test
    void theReefSoundsItsOwn() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean tell = false;
        boolean open = false;
        boolean swell = false;
        boolean release = false;
        boolean organDied = false;
        boolean tore = false;
        int last = SimStep.ticks(40);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            // Held fire until the first organ's first cluster is out (t≈30), then the coil burns it down.
            sortie.step(steer(sortie) | (sortie.levelSeconds() >= 31 ? Command.FIRE.bit() : 0));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            tell |= bank.playing(Sfx.PIT_TELL) > 0;
            open |= bank.playing(Sfx.PIT_OPEN) > 0;
            swell |= bank.playing(Sfx.ORGAN_SWELL) > 0;
            release |= bank.playing(Sfx.ORGAN_RELEASE) > 0;
            tore |= bank.playing(Sfx.ORGAN_DEATH) > 0;
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ENEMY_DESTROYED
                        && sortie.enemyKinds().get(events.value(e)).slug().equals(ReefCues.ORGAN)) {
                    organDied = true;
                }
            }
        }
        sounds.stopLoops();
        assertTrue(tell, "the first pit's tell");
        assertTrue(open, "it opens");
        assertTrue(swell, "an organ's pod swells");
        assertTrue(release, "and bursts its cluster");
        assertTrue(organDied, "the first organ burnt down");
        assertTrue(tore, "it tears open as it dies");
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
