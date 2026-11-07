package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import com.badlogic.gdx.files.FileHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
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
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part G (step G4), Level 49's sounds (design/campaign Level 49, Music &amp; ambience; design/audio/music, the memory
 * copies; design/audio/sfx, Act 7 sounds; round 84's picks): the memory copies of tracks 4, 8 and 12 over Luna, Mars and
 * Ceres, a boss track per echo (the memory copies of 18, 18 and 19) after the unfiltered warning, the remembered beds
 * then the hive's; an echo forming and dissolving, and its launched units dissolving with it.
 */
class Level49SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(49).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theMemoryCopiesAndTheBedsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(Tracks.MEMORY + 4, data.track(), "track 4's memory copy over Luna");
        assertEquals("afterburner-memory", Tracks.name(data.track()).orElseThrow());
        assertEquals(
                List.of("red-dust-run-memory", "hollow-rock-memory"),
                data.themeList().stream()
                        .map(theme -> Tracks.name(theme.track()).orElseThrow())
                        .toList());
        assertEquals(
                List.of("choir-descends-memory", "choir-descends-memory", "iron-sovereign-memory"),
                data.bossTracks().orElseThrow());
        assertEquals("boss-warning", data.bossWarning().orElseThrow(), "the warning unfiltered (track 22)");
        for (String name : List.of(
                "afterburner-memory",
                "red-dust-run-memory",
                "hollow-rock-memory",
                "choir-descends-memory",
                "iron-sovereign-memory")) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.path(name))), name);
            // One full mix: no stems (its base stem is the copy itself).
            assertTrue(!Files.exists(ASSETS.resolve(Tracks.basePath(name))), name + " has no base stem");
        }
        assertTrue(Tracks.name(Tracks.MEMORY + 5).isEmpty(), "only the copied tracks have a memory copy");
        assertEquals(Sfx.AMBIENCE_LUNA_MEMORY, Sfx.ambience(data.ambience()));
        assertEquals(
                List.of(Sfx.AMBIENCE_MARS_DUST_MEMORY, Sfx.AMBIENCE_BELT_OPEN_MEMORY, Sfx.AMBIENCE_VRELL_HIVE),
                data.ambienceChangeList().stream()
                        .map(change -> Sfx.ambience(change.ambience()))
                        .toList());
        Map<Sfx, String> files = Map.of(
                Sfx.ECHO_FORM, "echo-form-r84-a.ogg",
                Sfx.ECHO_DISSOLVE, "echo-dissolve-r84-a.ogg",
                Sfx.AMBIENCE_LUNA_MEMORY, "luna-memory-r84-a.ogg",
                Sfx.AMBIENCE_MARS_DUST_MEMORY, "mars-dust-memory-r84-a.ogg",
                Sfx.AMBIENCE_BELT_OPEN_MEMORY, "belt-open-memory-r84-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /** Each fight plays its own boss track after the warning; its track falls away over 2 s at its death. */
    @Test
    void eachEchoHasItsOwnBossTrack() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank beds = new SfxBank(audio, files, mixer);
        FileHandle warning = files.internal(Tracks.path("boss-warning"));
        List<FileHandle> tracks = List.of(
                files.internal(Tracks.path("choir-descends-memory")),
                files.internal(Tracks.path("choir-descends-memory")),
                files.internal(Tracks.path("iron-sovereign-memory")));
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.path("afterburner-memory")),
                        files.internal(Tracks.path("afterburner-memory")),
                        beds,
                        Sfx.AMBIENCE_LUNA_MEMORY,
                        1,
                        0,
                        section -> true,
                        Double.POSITIVE_INFINITY,
                        Optional.empty(),
                        Optional.of(new LevelMusic.Boss(
                                Optional.of(warning), Optional.of(tracks.get(0)), Tracks.BOSS_WARNING_BARS_SECONDS)))
                .fightTracks(tracks);
        for (int fight = 0; fight < 3; fight++) {
            music.fight(fight);
            assertEquals(
                    tracks.get(fight).path(), music.fightTrack().orElseThrow().path(), "fight " + fight);
        }
        assertEquals(2f, LevelMusic.FIGHT_FADE_SECONDS, "falling away over 2 s at its death");
        music.dispose();
        beds.dispose();
        bank.dispose();
    }

    @Test
    void theEchoesFormAndDissolveWithTheirSounds() {
        EchoCues echoes = new EchoCues(new SoundMix(bank));
        assertEquals(
                EnumSet.of(SimEvents.Type.ECHO_FORM, SimEvents.Type.ECHO_DISSOLVE, SimEvents.Type.ECHO_UNIT_DISSOLVE),
                echoes.types());
        assertTrue(EchoCues.UNIT_LEVEL < 1, "a launched unit's dissolve quieter than its echo's");
        // Flown: the first echo forms at its station (t=95-98) with the rising shimmer.
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 5)),
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
        boolean formed = false;
        boolean heard = false;
        for (int step = 0; step < SimStep.ticks(110) && !heard; step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(0);
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (int e = 0; e < sortie.events().size(); e++) {
                formed |= sortie.events().type(e) == SimEvents.Type.ECHO_FORM;
            }
            heard = bank.playing(Sfx.ECHO_FORM) > 0;
        }
        assertTrue(formed, "the Brood Carrier's echo formed");
        assertTrue(heard, "with its shimmer");
        bank.dispose();
    }
}
