package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;

/**
 * M11 part D (step D4), Level 46's sounds (design/campaign Level 46, Music &amp; ambience; design/audio/sfx, Act 7
 * sounds; round 84's picks): track 17 "Hollow Light" by its base stem alone at −6 dB (its {@code sparse} stem until the
 * user's Act 7 playthrough, 2026-11-12: inaudible on laptop speakers), out over 1 s at t=120
 * for the quiet, back at t=147, thinning over the dead world to the bed alone; the {@code dead-fleet} bed (round 91's a
 * until the user's pick); flown, the nearest Husk's drift creak, the cue's cracks and shards; the warm hull's core taken
 * by its own sound; the dying Leviathan's cry at 0.75 pitch.
 */
class Level46SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(46).orElseThrow();
    private static final String THEME = "hollow-light";
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBedAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(17, data.track(), "Act 7 B, Hollow Light");
        assertEquals(THEME, Tracks.name(17).orElseThrow());
        for (String file : new String[] {Tracks.path(17), Tracks.basePath(THEME), Tracks.stemPath(THEME, "sparse")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(Sfx.AMBIENCE_DEAD_FLEET, Sfx.ambience(data.ambience()));
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_DEAD_FLEET.priority());
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_DEAD_FLEET, "dead-fleet-r91-a.ogg",
                Sfx.HUSK_DRIFT, "husk-drift-r84-b.ogg",
                Sfx.HUSK_CRACK, "husk-crack-r84-a.ogg",
                Sfx.HUSK_SHARDS, "husk-shards-r84-a.ogg",
                Sfx.HUSK_DEATH, "husk-death-r84-a.ogg",
                Sfx.CASKET_CRACK, "casket-crack-r84-b.ogg",
                Sfx.WARM_HULL_TAKEN, "warm-hull-taken-r84-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /**
     * The base stem alone at −6 dB from the start (never the full mix, no extra stem), out over 1 s at t=120 (the bed
     * alone through the quiet and the cue), back at t=147, −12 dB over the dead world, gone by t=262: the level ends in
     * the bed alone.
     */
    @Test
    void theBaseStemDropsForTheQuietAndComesBack() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank beds = new SfxBank(audio, files, mixer);
        LevelData.Music data = CONTENT.level(KEY).music();
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath(THEME)),
                        files.internal(Tracks.path(17)),
                        beds,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .stemTimes(
                        data.stemTimeList(),
                        stem -> files.absolute(ASSETS.resolve(Tracks.stemPath(THEME, stem))
                                .toAbsolutePath()
                                .toString()));
        double down6 = Math.pow(10, -6 / 20.0);
        music.update(1, 5, 0.1f, false);
        assertTrue(music.timedStemsOn(), "the theme's stems");
        assertEquals(0, music.timedShare(), 1e-6, "the base stem, never the full mix");
        assertEquals(-1, music.timedExtra(), "no extra stem (the sparse stem was inaudible on laptop speakers)");
        assertEquals(down6, music.timedGain(), 1e-3, "at -6 dB");
        double t = 119.9;
        music.update(3, t, 0.1f, false);
        music.update(3, 120.05, 0.1f, false);
        assertEquals(-1, music.timedExtra(), "out at t=120");
        assertFalse(music.timedStemsOn(), "the bed alone through the quiet");
        music.update(4, 147.05, 0.1f, false);
        assertTrue(music.timedStemsOn(), "back at t=147");
        assertEquals(0, music.timedShare(), 1e-6, "the base stem again");
        for (t = 147.15; t < 149; t += 0.1) {
            music.update(4, t, 0.1f, false);
        }
        assertEquals(down6, music.timedGain(), 1e-3, "at -6 dB again");
        for (t = 214; t < 239; t += 0.1) {
            music.update(5, t, 0.1f, false);
        }
        assertEquals(Math.pow(10, -12 / 20.0), music.timedGain(), 2e-3, "thinned over the dead world");
        music.update(5, 250.05, 0.1f, false);
        assertFalse(music.timedStemsOn(), "gone at the end: the bed alone");
        music.restart();
        music.update(1, 1, 0.1f, false);
        assertTrue(music.timedStemsOn(), "a retry starts with the base stem");
        assertEquals(0, music.timedShare(), 1e-6, "a retry starts with the base stem");
        music.dispose();
        beds.dispose();
        bank.dispose();
    }

    /** The warm hull's core is taken by its own soft hum; the dying Leviathan cries at 0.75 pitch, Level 03's at 1. */
    @Test
    void theCoreAndTheDyingLeviathanSoundTheirOwn() {
        assertEquals(
                List.of("vrell-core"),
                CONTENT.level(KEY).pickups().stream()
                        .flatMap(pickup -> pickup.story().stream())
                        .toList());
        assertEquals(
                Sfx.WARM_HULL_TAKEN, ReefCues.taken(new StoryPickups.Story("vrell-core", 2.0, new Hitbox(48, 48))));
        assertEquals(0.75f, BossCues.cryPitch("dying-leviathan"), 1e-6);
        assertEquals(1, BossCues.cryPitch("leviathan"), 1e-6);
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        assertEquals(
                List.of("dying-leviathan"),
                script.setPieces().stream().map(FlightSounds::setPieceSound).toList());
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable and holding fire to t=147: the nearest Husk's drift creak loops from
     * the first Husk on, and the quiet's cue at t=144 cracks the Husks on the screen in turn with their shards.
     */
    @Test
    void theHusksCreakCrackAndSplinter() {
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
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                script.setPieces().stream().map(FlightSounds::setPieceSound).toList());
        Set<Sfx> heard = EnumSet.noneOf(Sfx.class);
        Set<Sfx> wanted = EnumSet.of(Sfx.HUSK_DRIFT, Sfx.HUSK_CRACK, Sfx.HUSK_SHARDS);
        double firstDrift = Double.NaN;
        int last = SimStep.ticks(147);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(0);
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (Sfx sfx : wanted) {
                if (bank.playing(sfx) > 0 || bank.looping(sfx)) {
                    heard.add(sfx);
                }
            }
            if (Double.isNaN(firstDrift) && bank.looping(Sfx.HUSK_DRIFT)) {
                firstDrift = sortie.levelSeconds();
            }
            if (sortie.levelSeconds() < 143.9) {
                assertEquals(0, bank.playing(Sfx.HUSK_CRACK), "no crack while the guns are held");
            }
        }
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.HUSK_DRIFT), "stopped with the flight");
        assertEquals(wanted, heard);
        assertTrue(firstDrift >= 12 && firstDrift < 20, "the first Husk's creak (t=14): " + firstDrift);
        bank.dispose();
    }
}
