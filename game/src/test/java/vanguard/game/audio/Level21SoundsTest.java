package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.ColossusFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.screen.LevelScreen;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Allocations;
import vanguard.sim.BreachArena;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WalkerHooks;

/**
 * M7 part H, step HB: Level 21's sounds and music (design/campaign Level 21, Music &amp; ambience; design/audio/sfx):
 * round 44's proposals (a each until the pick, the ridge pass b, whose pan runs the ridge's way): the bulge at a
 * breach's tell, the eruption as the mouth breaches, the suction's draw as a loop while the mouth draws (louder the
 * nearer the ship), the bite's snap, the Colossus's own death, the ridge pass's rumble once at its time; the basin
 * rumble bed from t=64 over 30 s, swelling and held through the fight, cut at the kill (the clock's jump to t=245), the
 * wind's calm over the settling dust; the boss warning and "The Choir Descends" at the arena. Zero allocation per step.
 */
class Level21SoundsTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_21 = ColossusFixture.LEVEL;
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everySoundHasItsRound44File() throws IOException {
        List<Sfx> sounds = List.of(
                Sfx.COLOSSUS_BULGE,
                Sfx.COLOSSUS_ERUPTION,
                Sfx.COLOSSUS_SUCTION,
                Sfx.COLOSSUS_BITE,
                Sfx.COLOSSUS_DEATH,
                Sfx.COLOSSUS_RIDGE);
        for (Sfx sfx : sounds) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r44-a.ogg"), sfx + ": round 44's pick");
        }
        assertEquals(Sfx.Priority.WARNING, Sfx.COLOSSUS_BULGE.priority(), "the tell is a warning");
        assertEquals(Sfx.Priority.BOSS, Sfx.COLOSSUS_DEATH.priority());
        assertEquals(1f, ColossusCues.COLOSSUS_LEVEL, "levelled in their files");
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.COLOSSUS_SUCTION.path())))) {
            assertEquals(4, file.frameCount() / (double) file.sampleRate(), 0.05, "a seamless 4 s loop");
        }
        LevelData.Ridge ridge = CONTENT.level(LEVEL_21).ridge().orElseThrow();
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.COLOSSUS_RIDGE.path())))) {
            double seconds = file.frameCount() / (double) file.sampleRate();
            assertTrue(seconds <= ridge.seconds(), "the rumble within the " + ridge.seconds() + " s pass: " + seconds);
        }
        assertTrue(ridge.leftward(), "Level 21's ridge runs right to left, as variant b's pan");
    }

    @Test
    void theDrawIsLouderTheNearerTheShipAndTheRidgeRumblesOnceAtItsTime() {
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL, ColossusCues.draw(60, 120, 360), 1e-6, "inside the inner radius");
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL, ColossusCues.draw(120, 120, 360), 1e-6);
        assertEquals(ColossusCues.FAR_SHARE, ColossusCues.draw(360, 120, 360), 1e-6, "its share at the outer radius");
        assertEquals(ColossusCues.FAR_SHARE, ColossusCues.draw(800, 120, 360), 1e-6, "and beyond");
        assertEquals((1 + ColossusCues.FAR_SHARE) / 2, ColossusCues.draw(240, 120, 360), 1e-6, "half way");
        assertTrue(ColossusCues.drawing(BreachArena.Phase.OPEN) && ColossusCues.drawing(BreachArena.Phase.BITE));
        assertFalse(ColossusCues.drawing(BreachArena.Phase.RISE) || ColossusCues.drawing(BreachArena.Phase.DIVE));
        assertTrue(ColossusCues.ridgeDue(63.99, 64.01, 64));
        assertFalse(ColossusCues.ridgeDue(64.01, 64.03, 64), "once");
        assertFalse(ColossusCues.ridgeDue(Double.NaN, 64.01, 64), "not at the first watch");
        assertFalse(ColossusCues.ridgeDue(150, 105, 64), "nor when a boss checkpoint rewinds the clock past it");
        assertTrue(ColossusCues.ridgeDue(0, 64, 64), "again after a restart, as the clock reaches it");
        assertFalse(ColossusCues.ridgeDue(63, 64.5, Double.NaN), "none without a ridge");
    }

    @Test
    void theMusicCuesAreTheBossWarningTheChoirDescendsAndTheBedOverTheWind() {
        LevelData.Music music = CONTENT.level(LEVEL_21).music();
        assertEquals(9, music.track(), "Grey Signal");
        assertEquals(Optional.of("boss-warning"), music.bossWarning());
        assertEquals(Optional.of("choir-descends"), music.bossTrack());
        for (String track : List.of("boss-warning", "choir-descends", "act-complete")) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.path(track))), track);
        }
        assertEquals(Sfx.AMBIENCE_MARS_DUST, Sfx.ambience(music.ambience()), "the dust wind");
        LevelData.Music.AmbienceLayer bed = music.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AMBIENCE_BASIN_RUMBLE, Sfx.ambience(bed.ambience()));
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_21, Difficulty.MEDIUM);
        assertEquals(245, LevelScreen.crawlEnd(script), 1e-9, "the wind calms from the arena's end");
        assertEquals(bed.to(), LevelScreen.crawlEnd(script), 1e-9, "the bed's end is the arena's: cut by the kill");
        assertTrue(LevelScreen.breachingBoss(script), "its ridge pass rumbles");
        LevelScript level20 = SimSpecs.level(CONTENT, CONTENT.levelKey(20).orElseThrow(), Difficulty.MEDIUM);
        assertFalse(LevelScreen.breachingBoss(level20), "Level 20's ridge keeps its own bed");
        assertTrue(Double.isNaN(LevelScreen.crawlEnd(level20)));
        assertEquals(0, LevelMusic.calmDb(244.99, 245), 1e-9, "the wind at full while the boss lives");
        assertEquals(LevelMusic.CALM_DB / 2, LevelMusic.calmDb(245 + LevelMusic.CALM_SECONDS / 2, 245), 1e-9);
        assertEquals(LevelMusic.CALM_DB, LevelMusic.calmDb(270, 245), 1e-9, "calm over the settling dust");
        assertEquals(0, LevelMusic.calmDb(270, Double.NaN), 1e-9, "no calm without a crawling arena");
    }

    /**
     * The bed fades in from t=64 over 30 s, swells at the boss's arrival and holds while the clock is held at the
     * arena's end (a long fight), the kill cuts it; the wind calms after the kill.
     */
    @Test
    void theBasinRumbleSwellsAtTheArenaHoldsThroughTheFightAndCutsAtTheKill() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelData.Music data = CONTENT.level(LEVEL_21).music();
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("grey-signal")),
                        files.internal(Tracks.path(9)),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty(),
                        Optional.of(new LevelMusic.Boss(
                                Optional.of(files.internal(Tracks.path("boss-warning"))),
                                Optional.of(files.internal(Tracks.path("choir-descends"))),
                                Tracks.BOSS_WARNING_BARS_SECONDS)))
                .ambienceLayers(data.ambienceLayers().orElseThrow().stream()
                        .map(layer -> new LevelMusic.AmbienceLayer(Sfx.ambience(layer.ambience()), layer::level))
                        .toList());
        music.ambienceLevels(t -> LevelMusic.calmDb(t, 245));
        music.update(3, 63, 0.1f, false);
        assertFalse(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE), "not before the ridge pass");
        music.update(3, 79, 0.1f, false);
        assertEquals(0.5, music.layerLevel(0), 1e-6, "half way in its 30 s fade-in");
        music.update(4, 100, 0.1f, false);
        assertEquals(1, music.layerLevel(0), 1e-6, "full through the hollow");
        assertTrue(music.bossArrived(), "the boss music");
        for (int i = 0; i < 40; i++) {
            music.update(5, 105 + i * 0.1, 0.1f, false);
        }
        assertEquals(LevelMusic.BOSS_LAYER_SWELL_DB, 20 * Math.log10(music.layerSwell()), 1e-3, "swelled at the arena");
        assertEquals(1, sfx.loopVolume(Sfx.AMBIENCE_BASIN_RUMBLE), 1e-6, "the swell clamped at full (round 59: +6 dB)");
        assertEquals(
                LevelMusic.AMBIENCE_VOLUME, sfx.loopVolume(Sfx.AMBIENCE_MARS_DUST), 1e-6, "the wind at its bed level");
        music.update(5, 245 - 1.0 / SimStep.PER_SECOND, 0.1f, false);
        assertEquals(1, music.layerLevel(0), 1e-6, "held while the clock waits at the arena's end");
        assertTrue(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE));
        assertEquals(1, music.ambienceGain(), 1e-6, "the wind at full in the fight");
        music.bossDown();
        music.update(6, 245, 0.1f, false);
        assertFalse(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE), "cut at the kill");
        assertEquals(1, music.layerSwell(), 1e-6);
        music.update(6, 250, 0.1f, false);
        assertEquals(Math.pow(10, LevelMusic.CALM_DB / 20), music.ambienceGain(), 1e-5, "the wind calmed");
        assertTrue(sfx.looping(Sfx.AMBIENCE_MARS_DUST), "the wind plays on");
        music.restart();
        music.update(1, 1, 0.1f, false);
        assertEquals(1, music.ambienceGain(), 1e-6, "a restart brings the wind back");
        music.dispose();
        sfx.dispose();
    }

    /**
     * Flown in the arena at medium (the ship holding still, invulnerable): the bulge at the first tell, the eruption at
     * the rise, the draw looping while the mouth is open, the snap at the bite; the kill plays its own death and the
     * draw stops; the ridge pass's rumble at its time.
     */
    @Test
    void itBulgesBreachesDrawsBitesAndDiesWithItsOwnSounds() {
        Sortie sortie = ColossusFixture.sortie();
        FlightSounds sounds = new FlightSounds(
                bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of("dust-colossus"));
        sounds.ridgePass(1);
        boolean ridge = false;
        while (sortie.levelSeconds() < 2) {
            step(sortie, sounds);
            ridge |= bank.playing(Sfx.COLOSSUS_RIDGE) > 0;
        }
        assertTrue(ridge, "the ridge pass's rumble at its time");
        assertTrue(until(sortie, sounds, 20, type -> type == SimEvents.Type.BREACH_TELL), "the first tell");
        assertTrue(bank.playing(Sfx.COLOSSUS_BULGE) > 0, "the bulge");
        assertTrue(until(sortie, sounds, 5, type -> type == SimEvents.Type.BREACH_RISE), "the rise");
        assertTrue(bank.playing(Sfx.COLOSSUS_ERUPTION) > 0, "the eruption");
        BreachArena breach = ColossusFixture.colossus(sortie).breach().orElseThrow();
        for (int t = 0; t < SimStep.ticks(4) && breach.phase() != BreachArena.Phase.OPEN; t++) {
            step(sortie, sounds);
        }
        for (int t = 0; t < 30; t++) {
            step(sortie, sounds);
        }
        assertEquals(BreachArena.Phase.OPEN, breach.phase());
        assertTrue(bank.looping(Sfx.COLOSSUS_SUCTION), "the draw while it is open");
        assertTrue(until(sortie, sounds, 12, type -> type == SimEvents.Type.BITE_SNAP), "the bite");
        assertTrue(bank.playing(Sfx.COLOSSUS_BITE) > 0, "the snap");
        WalkerHooks.destroy(sortie, 0, ColossusFixture.MOUTH);
        sounds.play(sortie.events());
        boolean died = false;
        for (int t = 0; t < 30 && !died; t++) {
            step(sortie, sounds);
            died = bank.playing(Sfx.COLOSSUS_DEATH) > 0;
        }
        assertTrue(died, "its own death");
        for (int t = 0; t < 30; t++) {
            step(sortie, sounds);
        }
        assertFalse(bank.looping(Sfx.COLOSSUS_SUCTION), "the draw ends with it");
        sounds.stopLoops();
    }

    /** Following the suction step by step allocates nothing (watched on an open mouth, the loop running). */
    @Test
    void watchingTheColossusAllocatesNothing() {
        Sortie sortie = ColossusFixture.sortie();
        ColossusCues cues = new ColossusCues(new SoundMix(bank));
        cues.ridgePass(1e9);
        BreachArena breach = ColossusFixture.colossus(sortie).breach().orElseThrow();
        for (int t = 0; t < SimStep.ticks(30) && breach.phase() != BreachArena.Phase.OPEN; t++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(Command.NONE);
            cues.watch(sortie);
        }
        assertEquals(BreachArena.Phase.OPEN, breach.phase(), "the mouth open");
        long least = Allocations.least(() -> cues, watching -> {
            for (int i = 0; i < 600; i++) {
                watching.watch(sortie);
            }
        });
        assertEquals(0, least, "bytes allocated watching it");
        assertTrue(bank.looping(Sfx.COLOSSUS_SUCTION));
    }

    private boolean until(Sortie sortie, FlightSounds sounds, double seconds, Predicate<SimEvents.Type> type) {
        for (int t = 0; t < SimStep.ticks(seconds); t++) {
            step(sortie, sounds);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (type.test(events.type(e))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void step(Sortie sortie, FlightSounds sounds) {
        now[0] += NANOS_PER_STEP;
        sortie.step(Command.NONE);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
