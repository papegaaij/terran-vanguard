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
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.game.render.FoundryFixture;
import vanguard.game.render.RetreatFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WarningEdge;

/**
 * M7 part G's Level 20 sounds and music (design/campaign Level 20, Music &amp; ambience; design/audio/sfx): round 43's
 * proposals (a each) until the pick: the basin rumble as an ambience layer over the {@code mars-dust} wind from t=12, in
 * over 2 s, out over its last 12 s to t=40; the pursuit swarm's band hum while it shows and its lunge's rising hum
 * through each tell, the whoosh on the dive; the TOP warning the edge warning's tone; the rim flak the frigate's (its
 * sound in {@code AllyCues}); track 8's stems: the base stem in the strike, the full mix from the turnabout, the base
 * stem in the lull (section 5), the full mix again from section 6. The flight needs Level 20 mapped (step G4).
 */
class Level20SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everySoundHasItsFileAndTheRumbleIsALayerOverTheWind() throws IOException {
        for (Sfx sfx : List.of(Sfx.SWARM_HUM, Sfx.SWARM_LUNGE, Sfx.AMBIENCE_BASIN_RUMBLE)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r43-a.ogg"), sfx + ": round 43's proposal a");
        }
        LevelData.Music music = RetreatFixture.level().music();
        assertEquals(Sfx.AMBIENCE_MARS_DUST, Sfx.ambience(music.ambience()), "the dust wind");
        assertEquals(1, music.ambienceLayers().orElseThrow().size());
        assertEquals(
                Sfx.AMBIENCE_BASIN_RUMBLE,
                Sfx.ambience(music.ambienceLayers().orElseThrow().getFirst().ambience()));
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_BASIN_RUMBLE.priority());
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.AMBIENCE_BASIN_RUMBLE.path())))) {
            assertEquals(16, file.frameCount() / (double) file.sampleRate(), 0.05, "a seamless 16 s loop");
        }
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.SWARM_HUM.path())))) {
            assertEquals(6, file.frameCount() / (double) file.sampleRate(), 0.05, "a seamless 6 s loop");
        }
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.SWARM_LUNGE.path())))) {
            assertEquals(
                    PursuitCues.LUNGE_SECONDS,
                    file.frameCount() / (double) file.sampleRate(),
                    0.05,
                    "the lunge as long as the tell");
        }
        assertEquals(1f, PursuitCues.PURSUIT_LEVEL, "levelled in their files");
    }

    @Test
    void theLungesWhooshLandsOnTheDive() {
        assertEquals(0, PursuitCues.lungeDelay(0.8), "medium: the 0.8 s tell, at once");
        assertEquals(SimStep.ticks(0.4), PursuitCues.lungeDelay(1.2), "easy's 1.2 s tell: 0.4 s later");
        assertEquals(0, PursuitCues.lungeDelay(0.5), "a shorter tell: at once");
        assertEquals(0, PursuitCues.hum(true, false), "no hum without the swarm on the screen");
        assertEquals(0, PursuitCues.hum(false, true), "nor once the flight is over");
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL, PursuitCues.hum(true, true));
    }

    @Test
    void theBasinRumbleFadesInAtTwelveAndOutByForty() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelData.Music data = RetreatFixture.level().music();
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("red-dust-run")),
                        files.internal(Tracks.path(8)),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .ambienceLayers(data.ambienceLayers().orElseThrow().stream()
                        .map(layer -> new LevelMusic.AmbienceLayer(Sfx.ambience(layer.ambience()), layer::level))
                        .toList());
        music.update(1, 11, 0.1f, false);
        assertFalse(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE), "not before the ridge");
        music.update(1, 13, 0.1f, false);
        assertTrue(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE));
        assertEquals(0.5, music.layerLevel(0), 1e-6, "half way in its 2 s fade-in");
        music.update(2, 20, 0.1f, false);
        assertEquals(1, music.layerLevel(0), 1e-6);
        music.update(3, 34, 0.1f, false);
        assertEquals(0.5, music.layerLevel(0), 1e-6, "half way out over its last 12 s");
        music.update(3, 40, 0.1f, false);
        assertFalse(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE), "gone by t=40");
        assertTrue(sfx.looping(Sfx.AMBIENCE_MARS_DUST), "the wind plays on");
        music.update(1, 14, 0.1f, false);
        assertTrue(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE), "a restart rewinds the clock: the rumble again");
        music.dispose();
        assertFalse(sfx.looping(Sfx.AMBIENCE_BASIN_RUMBLE), "stopped with the level");
        sfx.dispose();
    }

    @Test
    void track8sStemsFollowTheRetreat() {
        LevelData.Music music = RetreatFixture.level().music();
        assertEquals(8, music.track());
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.basePath("red-dust-run"))), "its base stem");
        assertFalse(music.full(1), "the strike: the base stem");
        for (int section = 2; section <= 4; section++) {
            assertTrue(music.full(section), "the full mix from the turnabout: section " + section);
        }
        assertFalse(music.full(5), "the lull: down to the base stem under Rook's line");
        assertTrue(music.full(6) && music.full(7), "the full mix again from section 6, over the rim");
    }

    @Test
    void theTopWarningIsTheEdgeWarningsTone() {
        Sortie sortie = FoundryFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        sounds.edgeWarnings(WarningEdge.TOP.bit());
        assertEquals(1, bank.playing(Sfx.EDGE_WARNING));
    }

    /**
     * Flown at medium without firing: the band hum loops once the first pursuit shows (t=24 on), the lunge sound plays
     * at its first tell, and the hum stops with the flight (needs step G4).
     */
    @Test
    void thePursuitHumsAndLunges() {
        Sortie sortie = RetreatFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean hum = false;
        boolean lunge = false;
        while (sortie.levelSeconds() < 40 && !(hum && lunge)) {
            step(sortie, sounds);
            hum |= bank.looping(Sfx.SWARM_HUM);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.PURSUIT_TELL) {
                    assertTrue(bank.playing(Sfx.SWARM_LUNGE) > 0, "the rising hum at the tell");
                    lunge = true;
                }
            }
        }
        assertTrue(hum, "the band hum while the swarm shows");
        assertTrue(lunge, "a tell by t=40");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.SWARM_HUM));
    }

    private void step(Sortie sortie, FlightSounds sounds) {
        now[0] += NANOS_PER_STEP;
        sortie.step(Command.NONE);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
