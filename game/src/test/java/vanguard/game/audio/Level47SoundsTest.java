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
import vanguard.sim.Command;
import vanguard.sim.Debris;
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part E (step E4), Level 47's sounds (design/campaign Level 47, Music &amp; ambience; design/audio/sfx, Act 7
 * sounds; round 84's picks): track 17 "Hollow Light", its base stem over the shell's edge, the full mix from the first
 * pair (t=62), the Choir's statement (its motif stream) over the Choir hall (t=120–200), thinning to the base under the
 * Choir's t=243 line; the {@code vrell-inner-sphere} bed (round 92's a until the user's pick); flown, the nearest
 * mirroring Seraph's tone pitched by its sideways speed, its arrival shimmer, its spiral's tick, its leaving chime and
 * the memory pearl's ring; the sound test's Hollow Light with its statement.
 */
class Level47SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(47).orElseThrow();
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
        for (String file : new String[] {Tracks.path(17), Tracks.basePath(THEME), Tracks.motifPath(THEME)}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(Sfx.AMBIENCE_VRELL_INNER_SPHERE, Sfx.ambience(data.ambience()));
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_VRELL_INNER_SPHERE.priority());
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_VRELL_INNER_SPHERE, "vrell-inner-sphere-r92-a.ogg",
                Sfx.SERAPH_MIRROR, "seraph-mirror-r84-a.ogg",
                Sfx.SERAPH_ARRIVE, "seraph-arrive-r84-b.ogg",
                Sfx.SERAPH_SPIRAL, "seraph-spiral-r84-a.ogg",
                Sfx.SERAPH_LEAVE, "seraph-leave-r84-b.ogg",
                Sfx.POD_PEARL, "pod-pearl-r84-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /**
     * The base stem over the shell's edge, the full mix from t=62, the Choir's statement over the Choir hall only
     * (t=120–200), the base again from t=243 to the end; a retry starts on the base.
     */
    @Test
    void theStemsAndTheChoirsStatementFollowTheLevel() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank beds = new SfxBank(audio, files, mixer);
        LevelData level = CONTENT.level(KEY);
        LevelData.Music data = level.music();
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
                                .toString()))
                .motif(files.internal(Tracks.motifPath(THEME)), data::motifOn);
        music.update(1, 5, 0.1f, false);
        assertFalse(music.motifOn(), "no statement over the shell's edge");
        assertTrue(music.timedShare() < 1, "the base stem at the start");
        music.update(2, 62.05, 0.1f, false);
        assertEquals(1, music.timedShare(), "the full mix from the first pair");
        music.update(3, 119.9, 0.1f, false);
        assertFalse(music.motifOn());
        music.update(3, 120.05, 0.1f, false);
        assertTrue(music.motifOn(), "the Choir's statement in the Choir hall");
        music.update(3, 199.9, 0.1f, false);
        assertTrue(music.motifOn());
        music.update(4, 200.05, 0.1f, false);
        assertFalse(music.motifOn(), "gone after the hall");
        assertEquals(1, music.timedShare());
        music.update(4, 243.05, 0.1f, false);
        assertEquals(0, music.timedShare(), "thinning to the base under the Choir's line");
        assertTrue(music.timedStemsOn());
        music.restart();
        music.update(1, 1, 0.1f, false);
        assertTrue(music.timedShare() < 1, "a retry starts on the base");
        music.dispose();
        beds.dispose();
        bank.dispose();
    }

    /** The mirror loop's pitch rises with the Seraph's sideways speed, either way, to its cap. */
    @Test
    void theMirrorTonesPitchFollowsTheSidewaysSpeed() {
        assertEquals(SphereCues.PITCH_FROM, SphereCues.mirrorPitch(0), 1e-6);
        assertEquals(SphereCues.mirrorPitch(130), SphereCues.mirrorPitch(-130), 1e-6);
        assertTrue(SphereCues.mirrorPitch(130) > SphereCues.mirrorPitch(40));
        assertEquals(SphereCues.PITCH_TO, SphereCues.mirrorPitch(260), 1e-6);
        assertEquals(SphereCues.PITCH_TO, SphereCues.mirrorPitch(900), 1e-6);
        bank.dispose();
    }

    /** The sound test's Hollow Light is the chosen mix: its full mix with the Choir's statement layered over it. */
    @Test
    void theSoundTestPlaysHollowLightWithItsStatement() {
        SoundTest.Track hollow = SoundTest.TRACKS.stream()
                .filter(track -> track.title().equals("HOLLOW LIGHT"))
                .findFirst()
                .orElseThrow();
        assertEquals(Tracks.path(17), hollow.path());
        assertEquals(Tracks.motifPath(THEME), hollow.layer());
        assertTrue(Files.isRegularFile(ASSETS.resolve(hollow.layer())));
        assertTrue(SoundTest.TRACKS.stream().filter(track -> track != hollow).allMatch(track -> track.layer() == null));
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable, holding fire to t=165, then under the memory pearl firing: the
     * Seraphs' arrival shimmers, their spirals' ticks, the nearest one's mirror tone
     * while one mirrors, a leaving chime as an unhurt Seraph leaves, and the pearl's ring once when it breaks.
     */
    @Test
    void theSeraphsSingArriveTickLeaveAndThePearlRings() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        // A gun (a beam does not break debris).
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
        Set<Sfx> heard = EnumSet.noneOf(Sfx.class);
        Set<Sfx> wanted =
                EnumSet.of(Sfx.SERAPH_MIRROR, Sfx.SERAPH_ARRIVE, Sfx.SERAPH_SPIRAL, Sfx.SERAPH_LEAVE, Sfx.POD_PEARL);
        int pearls = 0;
        boolean secret = false;
        int last = SimStep.ticks(190);
        for (int step = 0; step < last && !sortie.complete() && !(secret && step > SimStep.ticks(166)); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (int e = 0; e < sortie.events().size(); e++) {
                secret |= sortie.events().type(e) == SimEvents.Type.SECRET_FOUND;
            }
            for (Sfx sfx : wanted) {
                if (bank.playing(sfx) > 0 || bank.looping(sfx)) {
                    heard.add(sfx);
                }
            }
            if (bank.playing(Sfx.POD_PEARL) > 0 && pearls == 0) {
                pearls = 1;
            }
            if (sortie.levelSeconds() < 30) {
                assertFalse(bank.looping(Sfx.SERAPH_MIRROR), "no Seraph before the first: " + sortie.levelSeconds());
            }
        }
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.SERAPH_MIRROR), "stopped with the flight");
        assertTrue(secret, "the pearl was broken");
        assertEquals(wanted, heard);
        assertEquals(1, pearls);
        bank.dispose();
    }

    /** Guns held to t=165; then under the memory pearl, firing, until it breaks; else low in the middle. */
    private static int steer(Sortie sortie) {
        double tx = 240;
        double ty = 80;
        boolean fire = false;
        if (sortie.levelSeconds() >= 165) {
            for (int d = 0; d < sortie.debrisCount(); d++) {
                Debris chunk = sortie.debris(d);
                if (chunk.sprite().equals(SphereCues.PEARL)) {
                    tx = chunk.renderX(1);
                    fire = true;
                }
            }
        }
        int commands = fire ? Command.FIRE.bit() : 0;
        double x = sortie.ship().x();
        double y = sortie.ship().y();
        if (x < tx - 3) {
            commands |= Command.RIGHT.bit();
        } else if (x > tx + 3) {
            commands |= Command.LEFT.bit();
        }
        if (y < ty - 3) {
            commands |= Command.UP.bit();
        } else if (y > ty + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
