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
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.JovianFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3a: the sounds of A1a's Act 6 mechanics (design/audio/sfx, Act 6 sounds): round 72's picks play
 * their files; round 73's proposals (the mirror's heat, break and regrowth, the dome's hit, the absorbed tick) play the
 * nearest built sound until the user's pick; the war's kills play the prey's rung; one family per event.
 */
class ActSixSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everyActSixSoundPlaysRoundSeventyTwosPick() {
        Map<Sfx, String> picks = Map.of(
                Sfx.REGROWTH_CREAK, "regrowth-creak-r72-a.ogg",
                Sfx.MIRROR_PING, "mirror-ping-r72-a.ogg",
                Sfx.PYLON_DOME_UP, "pylon-dome-up-r72-a.ogg",
                Sfx.PYLON_DOME_DOWN, "pylon-dome-down-r72-b.ogg",
                Sfx.GRAFT_REGROWTH, "graft-regrowth-r72-a.ogg");
        picks.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        // Round 73's proposals wait for the pick: the nearest built sounds stand in.
        for (Sfx standIn : List.of(
                JovianCues.HEAT_STAND_IN,
                JovianCues.BREAK_STAND_IN,
                JovianCues.REGROW_STAND_IN,
                JovianCues.DOME_HIT_STAND_IN,
                JovianCues.ABSORBED_STAND_IN)) {
            assertFalse(standIn.path().contains("-r73-"), standIn.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(standIn.path())), standIn.path());
        }
        assertTrue(!JovianCues.DOME_HIT_STAND_IN.equals(Sfx.DOME_HIT), "never the Level 17 colony dome's glass boom");
        bank.dispose();
    }

    @Test
    void everyActSixEventHasOneFamily() {
        // FlightSounds throws when two families claim one event.
        new FlightSounds(bank, new EnemyLooks[0], JovianFixture.mirror().armament(), List.of());
        JovianCues cues = new JovianCues(new SoundMix(bank));
        for (SimEvents.Type type : List.of(
                SimEvents.Type.MIRROR_BOUNCED,
                SimEvents.Type.MIRROR_ABSORBED,
                SimEvents.Type.MIRROR_BROKEN,
                SimEvents.Type.MIRROR_REGROWING,
                SimEvents.Type.ROOT_EXPOSED,
                SimEvents.Type.ROOT_KILLED,
                SimEvents.Type.KNITTING,
                SimEvents.Type.DOME_HIT,
                SimEvents.Type.DOME_DOWN,
                SimEvents.Type.DOME_UP,
                SimEvents.Type.DOME_COLLAPSED)) {
            assertTrue(cues.types().contains(type), type.name());
        }
        bank.dispose();
    }

    @Test
    void theThrottlesAndTheHeatsPitch() {
        assertTrue(JovianCues.due(0, SimStep.ticks(0.08), 0.08));
        assertFalse(JovianCues.due(0, SimStep.ticks(0.08) - 1, 0.08));
        assertTrue(JovianCues.due(100, 5, 0.15), "a restarted level");
        assertEquals(JovianCues.HEAT_PITCH_LOW, JovianCues.heatPitch(0), 1e-6);
        assertEquals(JovianCues.HEAT_PITCH_HIGH, JovianCues.heatPitch(1), 1e-6);
        assertEquals(0.85f, JovianCues.HEAT_PITCH_LOW, "0.85 cold (round 73's brief)");
        assertEquals(1.35f, JovianCues.HEAT_PITCH_HIGH, "1.35 at the break");
        bank.dispose();
    }

    @Test
    void aBounceRingsTheGlassPing() {
        Sortie sortie = JovianFixture.mirror();
        JovianCues cues = new JovianCues(new SoundMix(bank));
        boolean bounced = run(sortie, cues, Command.FIRE.bit(), 15, () -> bank.playing(Sfx.MIRROR_PING) > 0);
        assertTrue(bounced, "the ship's shots meet the mirror's face");
        bank.dispose();
    }

    @Test
    void aKilledGraftTurretKnitsBackWithItsCreakAndKnock() {
        Sortie sortie = JovianFixture.graft();
        JovianCues cues = new JovianCues(new SoundMix(bank));
        boolean[] firstBurst = {false};
        assertTrue(run(sortie, cues, Command.FIRE.bit(), 15, () -> {
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ROOT_EXPOSED) {
                    firstBurst[0] = JovianCues.killedHere(events, i);
                    return true;
                }
            }
            return false;
        }));
        assertTrue(firstBurst[0], "its first death: the kill's rung is the shell's burst (UnitCues)");
        assertEquals(0, bank.playing(Sfx.EXPLOSION_SMALL_A) + bank.playing(Sfx.EXPLOSION_SMALL_B));
        assertTrue(run(sortie, cues, Command.NONE, 6, () -> bank.playing(Sfx.GRAFT_REGROWTH) > 0), "the knit");
        bank.dispose();
    }

    @Test
    void aHealingChimeraCreaks() {
        Sortie sortie = JovianFixture.chimera();
        JovianCues cues = new JovianCues(new SoundMix(bank));
        assertTrue(run(sortie, cues, Command.FIRE.bit(), 10, () -> {
            Enemy c = SiegeUnitsFixture.find(sortie, JovianFixture.CHIMERA);
            return c != null && c.hp() < c.spec().hp();
        }));
        assertEquals(0, bank.playing(Sfx.REGROWTH_CREAK));
        assertTrue(run(sortie, cues, Command.NONE, 2, () -> bank.playing(Sfx.REGROWTH_CREAK) > 0), "it heals");
        bank.dispose();
    }

    /** Steps with {@code commands}, the cues hearing every step, until {@code done} or {@code seconds} ran. */
    private boolean run(
            Sortie sortie, JovianCues cues, int commands, double seconds, java.util.function.BooleanSupplier done) {
        for (int t = 0; t < SimStep.ticks(seconds); t++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(commands);
            cues.begin(sortie.tick());
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (cues.types().contains(events.type(i))) {
                    cues.play(events, i, 0);
                }
            }
            cues.end();
            cues.watch(sortie);
            if (done.getAsBoolean()) {
                return true;
            }
        }
        return false;
    }
}
