package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part C, Level 30's sounds (step C4; design/audio/sfx, Act 5 sounds; round 60's picks and step AUD-C's round 63
 * proposals, their a variants until round 63 picks; design/audio/music track 12): every key plays its file; the level's
 * theme and the belt's open bed; and flown through the first lines, the first Gunship and the first train, invulnerable
 * and firing: the mines dropped, arming, blinking and bursting, the Gunship's ring, doors and Darts with its engine's
 * loop, the Minelayer's drone, the Rail Serpent's rumble and turret shots.
 */
class Level30SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(30).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    /**
     * Step FIX1 (design/audio/music, Cues per level (Act 5): Vorne's motif "on"): track 12's motif stem on top of
     * Level 30's theme from the base stem's start (section 1) to the end.
     */
    @Test
    void theMotifPlaysOnTopFromTheBaseStemsStartToTheEnd() {
        LevelData.Music data = CONTENT.level(KEY).music();
        String theme = Tracks.name(data.track()).orElseThrow();
        assertEquals("hollow-rock", theme);
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.motifPath(theme))), "track 12's motif stem");
        assertTrue(data.motif().isPresent(), "the level's motif cue");
        assertEquals(1, data.startSection());
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath(theme)),
                        files.internal(Tracks.path(theme)),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(files.internal(Tracks.motifPath(theme)), data::motifOn);
        music.update(1, 0.05, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the base stem with the motif from the start");
        music.update(2, 40, 0.1f, false);
        assertTrue(data.full(2));
        assertTrue(music.motifOn(), "over the full mix from the first column");
        music.update(4, 175, 0.1f, false);
        assertTrue(music.motifOn(), "to the end");
        music.dispose();
        sfx.dispose();
    }

    @Test
    void everyLevel30SoundPlaysItsFile() {
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.AMBIENCE_BELT_OPEN, "sfx/belt-open-r60-a.ogg"),
                Map.entry(Sfx.MINE_BLINK, "sfx/mine-blink-r60-a.ogg"),
                Map.entry(Sfx.MINE_BURST, "sfx/mine-burst-r60-a.ogg"),
                Map.entry(Sfx.GUNSHIP_ENGINE, "sfx/ascendancy-turbine-r60-a.ogg"),
                Map.entry(Sfx.MINELAYER_DRONE, "sfx/ascendancy-turbine-r60-a.ogg"),
                Map.entry(Sfx.LINE_MINE_ARM, "sfx/mine-arm-r63-b.ogg"),
                Map.entry(Sfx.LINE_MINE_DROP, "sfx/mine-drop-r63-a.ogg"),
                Map.entry(Sfx.MINE_CHAIN, "sfx/mine-chain-r63-a.ogg"),
                Map.entry(Sfx.SERPENT_SHOT, "sfx/serpent-shot-r63-a.ogg"),
                Map.entry(Sfx.SERPENT_RUMBLE, "sfx/serpent-rumble-r64-a.ogg"),
                Map.entry(Sfx.SERPENT_SPLIT, "sfx/serpent-split-r63-b.ogg"),
                Map.entry(Sfx.GUNSHIP_RING, "sfx/gunship-ring-r63-b.ogg"),
                Map.entry(Sfx.GUNSHIP_DOORS, "sfx/gunship-doors-r63-b.ogg"),
                Map.entry(Sfx.DART_LAUNCH, "sfx/dart-launch-r63-a.ogg"),
                Map.entry(Sfx.BUOY_TICK, "sfx/buoy-tick-r63-a.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals(file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        });
        LevelData.Music music = CONTENT.level(KEY).music();
        assertEquals(Sfx.AMBIENCE_BELT_OPEN, Sfx.ambience(music.ambience()), "the belt's open bed");
        String theme = Tracks.name(music.track()).orElseThrow();
        assertEquals("hollow-rock", theme, "track 12");
        for (String path : List.of(Tracks.path(theme), Tracks.basePath(theme))) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(path)), path);
        }
    }

    /** Flown to t=80, invulnerable, firing from the middle of the lane. */
    @Test
    void theMinesTheGunshipAndTheTrainSound() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        Set<Sfx> wanted = EnumSet.of(
                Sfx.LINE_MINE_DROP,
                Sfx.LINE_MINE_ARM,
                Sfx.MINE_BURST,
                Sfx.GUNSHIP_RING,
                Sfx.GUNSHIP_DOORS,
                Sfx.DART_LAUNCH,
                Sfx.SERPENT_SHOT);
        Set<Sfx> loops = EnumSet.of(Sfx.GUNSHIP_ENGINE, Sfx.MINELAYER_DRONE, Sfx.SERPENT_RUMBLE);
        Set<Sfx> heard = EnumSet.noneOf(Sfx.class);
        while (sortie.levelSeconds() < 80) {
            now[0] += NANOS_PER_STEP;
            sortie.step(IceShelfFixture.toward(sortie, 240, 100));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (Sfx sfx : wanted) {
                if (bank.playing(sfx) > 0) {
                    heard.add(sfx);
                }
            }
            for (Sfx sfx : loops) {
                if (bank.looping(sfx)) {
                    heard.add(sfx);
                }
            }
        }
        Set<Sfx> all = EnumSet.copyOf(wanted);
        all.addAll(loops);
        assertEquals(all, heard);
        sounds.stopLoops();
        for (Sfx loop : loops) {
            assertTrue(!bank.looping(loop), loop + " stops with the level");
        }
        bank.dispose();
    }
}
