package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.render.EnemyLooks;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.EmpSpec;
import vanguard.sim.LanceSpec;
import vanguard.sim.Loadout;
import vanguard.sim.OverchargeSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSpec;
import vanguard.sim.ThrusterSpec;
import vanguard.sim.WeaponSpec;

/**
 * M7 part A's sounds in flight (design/audio/sfx, round 37's picks of 2026-10-13): the Ion Beam's
 * start with its held loop and its stop on release; the EMP Burst's ring; the Orbital Lance's lock
 * beep and its loop c with the rumble while it burns, both stopped as it ends; the Shield Overcharge's
 * whoosh and the ready chime as its cooldown ends; the dash's burst. Flown on Level 01 with test specs
 * of the approved numbers (the content's mapping of the Act 3 items is step A4's).
 */
class ArsenalSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(1).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private static final int FIRE = Command.FIRE.bit();
    private static final int SPECIAL = Command.SPECIAL.bit();

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everyArsenalSoundHasItsFile() {
        for (Sfx sfx : List.of(
                Sfx.ION_BEAM_START,
                Sfx.ION_BEAM_LOOP,
                Sfx.ION_BEAM_STOP,
                Sfx.BEAM_IMPACT,
                Sfx.BEAM_IMPACT_ROCK,
                Sfx.DEFLECTOR_TICK,
                Sfx.DEFLECTOR_RETURN,
                Sfx.DRONE_BREAK,
                Sfx.DRONE_REBUILD,
                Sfx.EMP_RING,
                Sfx.EMP_POWERDOWN,
                Sfx.LANCE_LOCK,
                Sfx.LANCE_LOOP,
                Sfx.LANCE_RUMBLE,
                Sfx.OVERCHARGE_WHOOSH,
                Sfx.OVERCHARGE_END,
                Sfx.COOLDOWN_READY,
                Sfx.DASH)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        }
        assertTrue(Sfx.ION_BEAM_LOOP.path().endsWith("ion-beam-loop-r36-a.ogg"), "round 36's chosen loop (S.4 = a)");
        assertTrue(Sfx.LANCE_LOOP.path().endsWith("shot-beam-r04-c.ogg"), "the beam family's loop c");
        // Round 37's picks (user, 2026-10-13): S.2, S.4, S.6-S.10 b, the other sounds a; round 46's (2026-10-21)
        // replaced the beam's stop and impact, the drone rebuild, the EMP, the overcharge and the dash.
        for (Sfx sfx : List.of(Sfx.BEAM_IMPACT_ROCK, Sfx.DEFLECTOR_RETURN, Sfx.LANCE_RUMBLE)) {
            assertTrue(sfx.path().endsWith("-r37-b.ogg"), sfx + " is round 37's b: " + sfx.path());
        }
        for (Sfx sfx : List.of(Sfx.ION_BEAM_START, Sfx.DEFLECTOR_TICK, Sfx.DRONE_BREAK, Sfx.COOLDOWN_READY)) {
            assertTrue(sfx.path().endsWith("-r37-a.ogg"), sfx + " is round 37's a: " + sfx.path());
        }
        for (Sfx sfx : List.of(Sfx.ION_BEAM_STOP, Sfx.BEAM_IMPACT, Sfx.DRONE_REBUILD, Sfx.EMP_POWERDOWN)) {
            assertTrue(sfx.path().endsWith("-r46-a.ogg"), sfx + " is round 46's a: " + sfx.path());
        }
        for (Sfx sfx : List.of(Sfx.EMP_RING, Sfx.OVERCHARGE_WHOOSH, Sfx.OVERCHARGE_END, Sfx.DASH)) {
            assertTrue(sfx.path().endsWith("-r46-b.ogg"), sfx + " is round 46's b: " + sfx.path());
        }
    }

    @Test
    void theBeamStartsWithItsLoopHoldsItAndStopsOnRelease() {
        WeaponSpec beam = WeaponSpec.beam("ion-beam", "beam", "beam", 8, 60, new WeaponSpec.Muzzle(0, 21, 0));
        WeaponSpec overdrive = WeaponSpec.beam("ion-beam", "beam", "beam", 10, 80, new WeaponSpec.Muzzle(0, 21, 0));
        Loadout base = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM);
        Loadout loadout = new Loadout(
                base.ship(),
                new Armament(List.of(new Armament.Mount(Armament.Slot.FRONT, beam, overdrive))),
                base.shield(),
                base.plating());
        Sortie sortie = sortie(loadout);
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertFalse(bank.looping(Sfx.ION_BEAM_LOOP));
        run(sortie, sounds, FIRE, 30);
        assertTrue(bank.looping(Sfx.ION_BEAM_LOOP), "the loop while fire is held");
        assertTrue(bank.playing(Sfx.ION_BEAM_START) > 0, "the start with it");
        run(sortie, sounds, 0, 2);
        assertFalse(bank.looping(Sfx.ION_BEAM_LOOP), "released");
        assertTrue(bank.playing(Sfx.ION_BEAM_STOP) > 0, "the stop");
        sounds.stopLoops();
        bank.dispose();
    }

    @Test
    void theEmpBurstsRingPlaysAsItGoesOff() {
        Sortie sortie = sortie(withSpecial(new SpecialSpec("EMP Burst", 1, 3, 0.1, new EmpSpec(0.4, 3, 1.5, 1.0))));
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertTrue(until(sortie, sounds, SPECIAL, 10, type -> type == SimEvents.Type.EMP_BURST), "it went off");
        assertTrue(bank.playing(Sfx.EMP_RING) > 0);
        bank.dispose();
    }

    @Test
    void theLanceBeepsAsItLocksAndHoldsItsLoopAndRumbleWhileItBurns() {
        LanceSpec lance = new LanceSpec(0.6, 40, 3, 0.3, 240, 400, 200, 400, 1.0);
        Sortie sortie = sortie(withSpecial(new SpecialSpec("Orbital Lance", 1, 2, 0.1, lance)));
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertTrue(until(sortie, sounds, SPECIAL, 10, type -> type == SimEvents.Type.LANCE_LOCK), "called");
        assertTrue(bank.playing(Sfx.LANCE_LOCK) > 0, "the lock beep");
        assertFalse(bank.looping(Sfx.LANCE_LOOP), "not before it fires");
        assertTrue(until(sortie, sounds, 0, SimStep.ticks(1), type -> type == SimEvents.Type.LANCE_FIRED));
        assertTrue(bank.looping(Sfx.LANCE_LOOP), "loop c while it burns");
        assertTrue(bank.looping(Sfx.LANCE_RUMBLE), "the rumble under it");
        assertTrue(until(sortie, sounds, 0, SimStep.ticks(4), type -> type == SimEvents.Type.LANCE_ENDED));
        run(sortie, sounds, 0, SimStep.ticks(0.4));
        assertFalse(bank.looping(Sfx.LANCE_LOOP), "faded out");
        assertFalse(bank.looping(Sfx.LANCE_RUMBLE));
        bank.dispose();
    }

    @Test
    void theOverchargeWhooshesAndItsRingChimesWhenReadyAgain() {
        SpecialSpec overcharge = SpecialSpec.cooldown("Shield Overcharge", 0.1, 45, 10, new OverchargeSpec(2, 8, 0.5));
        Sortie sortie = sortie(withSpecial(overcharge));
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertTrue(until(sortie, sounds, SPECIAL, 10, type -> type == SimEvents.Type.OVERCHARGE), "activated");
        assertTrue(bank.playing(Sfx.OVERCHARGE_WHOOSH) > 0);
        assertTrue(until(sortie, sounds, 0, SimStep.ticks(9), type -> type == SimEvents.Type.OVERCHARGE_ENDED));
        assertTrue(bank.playing(Sfx.OVERCHARGE_END) > 0, "the end shimmer");
        assertTrue(until(sortie, sounds, 0, SimStep.ticks(46), type -> type == SimEvents.Type.SPECIAL_READY));
        assertTrue(bank.playing(Sfx.COOLDOWN_READY) > 0, "the ready chime");
        bank.dispose();
    }

    @Test
    void aDashPlaysTheThrusterBurst() {
        Loadout loadout = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM)
                .withThrusters(new ThrusterSpec(72, 0.12, 0.25, 3, 0.25));
        Sortie sortie = sortie(loadout);
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertTrue(until(sortie, sounds, Command.DASH.bit(), 5, type -> type == SimEvents.Type.DASH), "dashed");
        assertTrue(bank.playing(Sfx.DASH) > 0);
        bank.dispose();
    }

    private static Loadout withSpecial(SpecialSpec special) {
        return SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM).withSpecial(special);
    }

    private static Sortie sortie(Loadout loadout) {
        return new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** Steps through the launch until the ship flies under the player's control. */
    private void launch(Sortie sortie, FlightSounds sounds) {
        for (int t = 0; t < SimStep.ticks(10) && sortie.launching(); t++) {
            step(sortie, sounds, 0);
        }
        assertFalse(sortie.launching(), "launched");
    }

    private void run(Sortie sortie, FlightSounds sounds, int commands, int steps) {
        for (int t = 0; t < steps; t++) {
            step(sortie, sounds, commands);
        }
    }

    /** Steps with {@code commands} (held the first step only) until an event of the kind shows; whether it did. */
    private boolean until(
            Sortie sortie, FlightSounds sounds, int commands, int steps, Predicate<SimEvents.Type> wanted) {
        for (int t = 0; t < steps; t++) {
            SimEvents events = step(sortie, sounds, t == 0 ? commands : 0);
            for (int i = 0; i < events.size(); i++) {
                if (wanted.test(events.type(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private SimEvents step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        SimEvents events = sortie.events();
        try {
            sounds.play(events);
        } catch (ArrayIndexOutOfBoundsException lookless) {
            // a kill without the test's looks
        }
        sounds.watch(sortie);
        return events;
    }
}
