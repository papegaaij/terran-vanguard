package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.HazardsBlock;
import vanguard.content.LevelData;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.render.VentFieldFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Enemy;
import vanguard.sim.Missiles;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part G's Level 27 sounds (design/audio/sfx and design/audio/music, Level 27's rows; round 57's a variants as
 * stand-ins until the user's pick): the vent field's bed, the plumes' tell and eruption, the bend's swish, the Depth
 * Hunter's torpedo launch, run, impact and hull break, the relay beacons' hum and break, the Marlin's crack, Varga's scan
 * ping; track 11 with the motif from t=56, alone over the bed 68–82, the full mix back, the base stem at the gate, out
 * over the last 6 s.
 */
class Level27SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    private static final List<Sfx> SOUNDS = List.of(
            Sfx.AMBIENCE_EUROPA_VENTS,
            Sfx.PLUME_TELL,
            Sfx.PLUME_ERUPTION,
            Sfx.BEND_SWISH,
            Sfx.TORPEDO_LAUNCH,
            Sfx.TORPEDO_RUN,
            Sfx.TORPEDO_IMPACT,
            Sfx.HUNTER_BREAK,
            Sfx.BEACON_HUM,
            Sfx.BEACON_BREAK,
            Sfx.MARLIN_CRACK,
            Sfx.SCAN_PING);

    @Test
    void everySoundHasItsFileRoundFiftySevensStandIn() {
        for (Sfx sfx : SOUNDS) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r57-a.ogg"), "round 57's a until the pick: " + sfx.path());
        }
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_EUROPA_VENTS.priority());
        assertTrue(Sfx.TORPEDO_RUN.priority() != Sfx.Priority.AMBIENCE, "a unit's loop is stolen like its peers");
        assertTrue(Sfx.BEACON_HUM.priority() != Sfx.Priority.AMBIENCE);
    }

    @Test
    void theBedIsTheVentFieldOnTrackEleven() {
        LevelData.Music music = VentFieldFixture.level().music();
        assertEquals(11, music.track(), "Act 4 B, Pressure Line");
        assertEquals(Sfx.AMBIENCE_EUROPA_VENTS, Sfx.ambience(music.ambience()));
        assertEquals(2, music.startSection(), "section 1 the bed alone");
        assertFalse(music.full(2));
        assertTrue(music.full(3), "the full mix from the chimney garden");
        assertFalse(music.full(5), "the base stem from the gate");
        assertFalse(music.motifOn(2, 50));
        assertTrue(music.motifOn(2, 57), "the motif from the Hunters' first sight");
        assertTrue(music.motifOn(5, 140));
        assertEquals(
                HazardsBlock.Vents.HEAT_PLUME,
                VentFieldFixture.level().vents().orElseThrow().lookOrDefault());
    }

    /**
     * The timed stems through the music: the sections' stems until t=68, then the stems out and the motif layer alone
     * over the bed (no extra stem: the motif is its own layer) until 82, the full mix back, the base stem at 124, the
     * whole theme out over 144–150.
     */
    @Test
    void theMotifPlaysAloneWhileVargaReadsTheHullCode() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelData.Music data = VentFieldFixture.level().music();
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("pressure-line")),
                        files.internal(Tracks.path("pressure-line")),
                        sfx,
                        Sfx.AMBIENCE_EUROPA_VENTS,
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(files.internal(Tracks.motifPath("pressure-line")), data::motifOn)
                .motifAlone(data::motifAlone)
                .stemTimes(
                        data.stemTimeList(),
                        stem -> files.absolute(ASSETS.resolve(Tracks.stemPath("pressure-line", stem))
                                .toAbsolutePath()
                                .toString()));
        music.update(2, 60, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the stems with the motif from t=56");
        music.update(3, 68.05, 0.1f, false);
        assertFalse(music.stemsOn(), "the stems out at 68");
        assertTrue(music.motifOn(), "the motif alone over the bed");
        assertEquals(-1, music.timedExtra(), "the motif layer, not a second copy as an extra stem");
        music.update(3, 82.05, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the full mix and the motif back at 82");
        music.update(4, 100, 0.1f, false);
        assertTrue(music.stemsOn());
        double t = 144;
        while (t < 150.5) {
            music.update(5, t, 0.1f, false);
            t += 0.1;
        }
        assertTrue(music.timedGain() < 0.02f, "the theme out over the last 6 s: " + music.timedGain());
        music.restart();
        music.update(3, 70, 0.1f, false);
        music.update(3, 70.1, 0.1f, false);
        assertFalse(music.stemsOn(), "a retry's jump lands on the change due");
        music.dispose();
        sfx.dispose();
    }

    /** The torpedoes' run is louder close to the ship; the scan pings 2 s before the wreck passes mid-screen. */
    @Test
    void theRunRisesAsATorpedoClosesAndTheScanLeadsTheWreck() {
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL, VentFieldCues.runLevel(30), 1e-6);
        assertEquals(FlightSounds.UNIT_LOOP_LEVEL * VentFieldCues.FAR_LEVEL, VentFieldCues.runLevel(600), 1e-6);
        assertTrue(VentFieldCues.runLevel(250) < VentFieldCues.runLevel(120));
        assertEquals(68, VentFieldCues.scanSeconds(VentFieldFixture.level()), 1e-9, "Varga's line at t=70");
        assertTrue(VentFieldCues.scanDue(67.99, 68.01, 68));
        assertFalse(VentFieldCues.scanDue(68.01, 68.03, 68));
        assertTrue(VentFieldCues.bendDue(0, VentFieldCues.BEND_GAP_STEPS));
        assertFalse(VentFieldCues.bendDue(0, VentFieldCues.BEND_GAP_STEPS - 1));
    }

    /**
     * Flown through the level with the Bulwark along, invulnerable, firing, over the Marlin and the beacons: the plumes
     * rumble and roar, a bent shot swishes, the Hunters' torpedoes launch and run (the loop only while one runs) and the
     * Hunters break up, a torpedo shot down bursts deep; the beacons hum while one is on the screen and break; the Marlin
     * cracks open; the scan pings once.
     */
    @Test
    void thePlumesRoarTheTorpedoesRunAndTheBeaconsHum() {
        Sortie sortie = VentFieldFixture.sortie(true);
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                List.of(),
                Optional.of(sortie.bulwark().orElseThrow().gun()));
        sounds.plumes(true);
        sounds.scan(VentFieldFixture.level());
        boolean tell = false;
        boolean eruption = false;
        boolean swish = false;
        boolean launch = false;
        boolean run = false;
        boolean hunterBreak = false;
        boolean shotDown = false;
        boolean hum = false;
        boolean beaconBreak = false;
        int scans = 0;
        int strayRun = 0;
        while (!sortie.complete() && sortie.levelSeconds() < 150) {
            int pings = bank.playing(Sfx.SCAN_PING);
            step(sortie, sounds, IceShelfFixture.toward(sortie, aim(sortie.levelSeconds()), 110));
            scans += Math.max(0, bank.playing(Sfx.SCAN_PING) - pings);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case VENT_TELL -> tell |= bank.playing(Sfx.PLUME_TELL) > 0;
                    case VENT_BURST -> eruption |= bank.playing(Sfx.PLUME_ERUPTION) > 0;
                    case PLUME_BEND -> swish |= bank.playing(Sfx.BEND_SWISH) > 0;
                    case MISSILE_LAUNCHED -> launch |= bank.playing(Sfx.TORPEDO_LAUNCH) > 0;
                    case MISSILE_DESTROYED -> {
                        if (events.value(e) == Missiles.SHOT_DOWN) {
                            shotDown |= bank.playing(Sfx.EXPLOSION_DEEP_TINY) > 0;
                        }
                    }
                    case ENEMY_DESTROYED -> {
                        String slug = sortie.enemyKinds().get(events.value(e)).slug();
                        if (slug.equals(VentFieldCues.HUNTER)) {
                            hunterBreak |= bank.playing(Sfx.HUNTER_BREAK) > 0;
                        } else if (slug.equals(VentFieldCues.BEACON)) {
                            beaconBreak |= bank.playing(Sfx.BEACON_BREAK) > 0;
                        }
                    }
                    default -> {}
                }
            }
            run |= sortie.missileCount() > 0 && bank.looping(Sfx.TORPEDO_RUN);
            if (sortie.missileCount() == 0 && bank.looping(Sfx.TORPEDO_RUN)) {
                strayRun++;
            }
            hum |= onScreen(sortie, VentFieldCues.BEACON) && bank.looping(Sfx.BEACON_HUM);
        }
        assertTrue(tell, "a plume's tell");
        assertTrue(eruption, "a plume's eruption");
        assertTrue(swish, "a bent shot's swish");
        assertTrue(launch, "a torpedo's launch");
        assertTrue(run, "the torpedoes' run");
        assertTrue(strayRun < SimStep.PER_SECOND * 20, "the run only fades out after the last torpedo: " + strayRun);
        assertTrue(hunterBreak, "a Hunter's hull break");
        assertTrue(shotDown, "a torpedo shot down bursts deep");
        assertTrue(hum, "the beacons' hum");
        assertTrue(beaconBreak, "a beacon's break");
        assertEquals(1, scans, "one scan ping");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.TORPEDO_RUN), "the loops stop with the flight");
        assertFalse(bank.looping(Sfx.BEACON_HUM));
    }

    /** The Marlin's crack plays as she is destroyed (an HP target revealing the secret). */
    @Test
    void theMarlinCracksOpen() {
        Sortie sortie = VentFieldFixture.sortie(false);
        FlightSounds sounds = new FlightSounds(
                bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of(), Optional.empty());
        boolean spent = false;
        while (!sortie.complete() && sortie.levelSeconds() < 60) {
            step(sortie, sounds, IceShelfFixture.toward(sortie, aim(sortie.levelSeconds()), 110));
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if ((events.type(e) == SimEvents.Type.GROUND_DESTROYED
                                || events.type(e) == SimEvents.Type.TRIGGER_SPENT)
                        && sortie.script()
                                .groundObjects()
                                .get(events.value(e))
                                .look()
                                .equals(VentFieldCues.MARLIN)) {
                    spent = true;
                    assertTrue(bank.playing(Sfx.MARLIN_CRACK) > 0, "her resin cracks");
                }
            }
        }
        assertTrue(spent, "the Marlin is shot open by the ship flying over her stack");
    }

    /** Where the ship flies at level time {@code t}: over the Marlin's stack, then over each beacon in turn. */
    private static double aim(double t) {
        if (t >= 30 && t < 46) {
            return 90;
        }
        if (t >= 90 && t < 99) {
            return 80;
        }
        if (t >= 99 && t < 106) {
            return 400;
        }
        if (t >= 106 && t < 113) {
            return 240;
        }
        if (t >= 113 && t < 121) {
            return 90;
        }
        return 240;
    }

    private static boolean onScreen(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (enemy.spec().slug().equals(slug) && y > 0 && y < PlayField.HEIGHT && x > 0 && x < PlayField.WIDTH) {
                return true;
            }
        }
        return false;
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
