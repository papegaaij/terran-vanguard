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
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part B's Level 22 sounds (design/audio/sfx, Level 22's rows; round 51's picks: the geyser tell and the rig's collar, ring and death b, the rest a): the ice
 * geysers' crack and steam roar instead of the furnace vent's, the Drill Rig's collar whine (one quiet loop while a rig
 * is on the screen), its ring of shards once a volley instead of the shots' report and its collapse with its
 * explosion; the Europa ice bed, crossfading to Conamara's at the run-in; track 10 "Thera Deep": the ambience alone on
 * the shelf, the base stem from the rig field, the full mix from the run-in.
 */
class Level22SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everySoundHasItsFileTheUsersPickAndTheBedsLoop() throws IOException {
        for (Sfx sfx : List.of(
                Sfx.GEYSER_TELL,
                Sfx.GEYSER_BURST,
                Sfx.RIG_COLLAR,
                Sfx.RIG_RING,
                Sfx.RIG_DEATH,
                Sfx.AMBIENCE_EUROPA_ICE,
                Sfx.AMBIENCE_CONAMARA)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        }
        for (Sfx sfx : List.of(Sfx.GEYSER_TELL, Sfx.RIG_COLLAR, Sfx.RIG_RING, Sfx.RIG_DEATH)) {
            assertTrue(sfx.path().endsWith("-r51-b.ogg"), "round 51's pick b: " + sfx.path());
        }
        for (Sfx sfx : List.of(Sfx.GEYSER_BURST, Sfx.AMBIENCE_EUROPA_ICE, Sfx.AMBIENCE_CONAMARA)) {
            assertTrue(sfx.path().endsWith("-r51-a.ogg"), "round 51's pick a: " + sfx.path());
        }
        assertEquals(Sfx.AMBIENCE_EUROPA_ICE, Sfx.ambience("europa-ice"));
        assertEquals(Sfx.AMBIENCE_CONAMARA, Sfx.ambience("conamara-station"));
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_EUROPA_ICE.priority());
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_CONAMARA.priority());
        assertEquals(Sfx.Priority.EXPLOSION, Sfx.RIG_DEATH.priority(), "on the explosion rung");
        for (Sfx bed : List.of(Sfx.AMBIENCE_EUROPA_ICE, Sfx.AMBIENCE_CONAMARA)) {
            try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(bed.path())))) {
                assertEquals(16, file.frameCount() / (double) file.sampleRate(), 0.05, bed + ": a seamless 16 s loop");
            }
        }
        try (var file = new VorbisFile(Files.readAllBytes(ASSETS.resolve(Sfx.RIG_COLLAR.path())))) {
            assertEquals(4, file.frameCount() / (double) file.sampleRate(), 0.05, "the collar's 4 s loop");
        }
    }

    @Test
    void theMusicIsTheraDeepOverTheIceBedThenTheStationsBed() {
        LevelData.Music music = IceShelfFixture.level().music();
        assertEquals(10, music.track());
        assertEquals("thera-deep", Tracks.name(10).orElseThrow());
        assertEquals("pressure-line", Tracks.name(11).orElseThrow(), "track 11 ships for Level 25 on");
        for (String file : List.of(
                Tracks.path(10), Tracks.basePath("thera-deep"), Tracks.path(11), Tracks.basePath("pressure-line"))) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.motifPath("pressure-line"))), "track 11's motif stem");
        assertEquals(2, music.startSection(), "the ambience alone on the shelf, the base stem from the rig field");
        assertFalse(music.full(2));
        assertTrue(music.full(3), "the full mix from the run-in");
        assertEquals("europa-ice", music.ambience());
        List<LevelData.Music.AmbienceChange> changes = music.ambienceChangeList();
        assertEquals(1, changes.size());
        assertEquals(3, changes.getFirst().section());
        assertEquals("conamara-station", changes.getFirst().ambience());
        assertEquals(Sfx.AMBIENCE_CONAMARA, Sfx.ambience(changes.getFirst().ambience()));
    }

    /**
     * Flown with bomb racks under the first rig until it rings, then over the rigs: the first geyser row cracks and roars with the geyser's sounds (never the
     * furnace's); the collar's whine loops while a rig is on the screen; a rig's ring plays the rig's ring; a rig's
     * core destroyed plays its collapse; the loop stops with the flight.
     */
    @Test
    void theGeysersAndTheRigsSound() {
        Sortie sortie = IceShelfFixture.bomber();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        sounds.geysers(true);
        boolean tell = false;
        boolean burst = false;
        boolean collar = false;
        boolean ring = false;
        boolean death = false;
        while (sortie.levelSeconds() < 75 && !(tell && burst && collar && ring && death)) {
            Enemy rig = IceShelfFixture.rig(sortie);
            // Under the rig until it has rung, then over it: the bombs drop on the ground under the ship.
            step(
                    sortie,
                    sounds,
                    rig == null
                            ? IceShelfFixture.toward(sortie, 240, 100)
                            : IceShelfFixture.toward(sortie, rig.renderX(1), ring ? rig.renderY(1) : 100));
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case VENT_TELL -> {
                        tell = true;
                        assertTrue(bank.playing(Sfx.GEYSER_TELL) > 0, "the ice crack with the glow");
                    }
                    case VENT_BURST -> {
                        burst = true;
                        assertTrue(bank.playing(Sfx.GEYSER_BURST) > 0, "the steam roar with the column");
                    }
                    case ENEMY_DESTROYED -> {
                        if (sortie.enemyKinds().get(events.value(e)).slug().equals("drill-rig")) {
                            death = true;
                            assertTrue(bank.playing(Sfx.RIG_DEATH) > 0, "the rig's collapse");
                        }
                    }
                    default -> {}
                }
            }
            assertEquals(0, bank.playing(Sfx.VENT_TELL) + bank.playing(Sfx.VENT_BURST), "never the furnace's");
            Enemy onScreen = IceShelfFixture.rig(sortie);
            if (onScreen != null && onScreen.renderY(1) < 500) {
                collar |= bank.looping(Sfx.RIG_COLLAR);
            }
            ring |= bank.playing(Sfx.RIG_RING) > 0;
        }
        assertTrue(tell && burst, "the first geyser rows fired");
        assertTrue(collar, "the collar's whine while a rig is on the screen");
        assertTrue(ring, "a rig's ring");
        assertTrue(death, "a rig's core destroyed");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.RIG_COLLAR));
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
