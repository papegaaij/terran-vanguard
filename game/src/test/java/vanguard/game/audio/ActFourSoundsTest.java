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
import vanguard.sim.Loadout;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part A, step A6: the Act 4 systems' sounds in flight (design/audio/sfx, round 50's picks; the Sonar Pulse's
 * chosen r04 a): the Harpoon Torpedoes' launch with one run loop held while any harpoon is in flight, the
 * Depth Charges' roll-out and their burst on the fuse, the Sonar Pulse's ping. Flown on Level 01 under water from its
 * start (the debug option {@code --medium underwater}).
 */
class ActFourSoundsTest {
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
    void everyActFourSoundHasItsFile() {
        for (Sfx sfx : List.of(
                Sfx.SHOT_HARPOON,
                Sfx.HARPOON_RUN,
                Sfx.HARPOON_HIT,
                Sfx.SHOT_DEPTH_CHARGE,
                Sfx.DEPTH_CHARGE_BURST,
                Sfx.SPECIAL_LIMIT)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        }
        // round 50's picks (2026-10-22): launch b, run a, hit a, roll-out b, burst a, limit chime b
        assertTrue(Sfx.SHOT_HARPOON.path().endsWith("shot-harpoon-r50-b.ogg"), "the picked launch");
        assertTrue(Sfx.HARPOON_RUN.path().endsWith("harpoon-run-r50-a.ogg"), "the picked run");
        assertTrue(Sfx.HARPOON_HIT.path().endsWith("harpoon-hit-r50-a.ogg"), "the picked hit");
        assertTrue(Sfx.SHOT_DEPTH_CHARGE.path().endsWith("shot-depth-charge-r50-b.ogg"), "the picked roll-out");
        assertTrue(Sfx.DEPTH_CHARGE_BURST.path().endsWith("depth-charge-burst-r50-a.ogg"), "the picked burst");
        assertTrue(Sfx.SPECIAL_LIMIT.path().endsWith("special-limit-r50-b.ogg"), "the picked limit chime");
        assertTrue(Sfx.SPECIAL_SONAR.path().endsWith("special-sonar-r04-a.ogg"), "the chosen ping");
        assertTrue(Sfx.SPECIAL_FLARES.path().endsWith("special-flares-r08-a.ogg"), "the chosen flares");
        assertTrue(Sfx.SPECIAL_DENIED.path().endsWith("special-denied-r08-a.ogg"), "the chosen denied buzz");
        assertTrue(Sfx.SHOT_TORPEDO.path().endsWith("shot-torpedo-r03-a.ogg"), "the pod's launch (round 50, S.6)");
    }

    @Test
    void harpoonsLaunchAndRunOnOneLoopWhileAnyIsInFlight() {
        Sortie sortie = sortie(weapons(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 5)));
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertFalse(bank.looping(Sfx.HARPOON_RUN));
        assertTrue(until(sortie, sounds, FIRE, SimStep.ticks(3), type -> type == SimEvents.Type.SHOT_FIRED), "fired");
        assertTrue(bank.playing(Sfx.SHOT_HARPOON) > 0, "the launch");
        run(sortie, sounds, FIRE, 20);
        assertTrue(bank.looping(Sfx.HARPOON_RUN), "the run while they fly");
        run(sortie, sounds, 0, SimStep.ticks(6));
        assertFalse(bank.looping(Sfx.HARPOON_RUN), "faded out once none is left");
        sounds.stopLoops();
        bank.dispose();
    }

    @Test
    void aDepthChargeRollsOutAndBurstsOnItsFuse() {
        Sortie sortie = sortie(weapons(new SimSpecs.FittedWeapon(Armament.Slot.REAR, "depth-charges", 1)));
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        // Up the field first, so the barrel bursts on the screen rather than rolling off its bottom edge.
        run(sortie, sounds, Command.UP.bit(), SimStep.ticks(1.5));
        assertTrue(until(sortie, sounds, FIRE, SimStep.ticks(3), type -> type == SimEvents.Type.SHOT_FIRED), "fired");
        assertTrue(bank.playing(Sfx.SHOT_DEPTH_CHARGE) > 0, "the roll-out");
        assertTrue(until(sortie, sounds, 0, SimStep.ticks(2), type -> type == SimEvents.Type.BLAST), "burst");
        assertTrue(bank.playing(Sfx.DEPTH_CHARGE_BURST) > 0, "the under-water thud");
        bank.dispose();
    }

    @Test
    void theSonarPulsePings() {
        Loadout loadout = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM)
                .withSpecial(SimSpecs.special(CONTENT, SimSpecs.SONAR_PULSE, 2));
        Sortie sortie = sortie(loadout);
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        assertTrue(until(sortie, sounds, SPECIAL, 10, type -> type == SimEvents.Type.SONAR_PULSE), "it went off");
        assertTrue(bank.playing(Sfx.SPECIAL_SONAR) > 0);
        bank.dispose();
    }

    private static Loadout weapons(SimSpecs.FittedWeapon weapon) {
        Loadout base = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM);
        return new Loadout(
                base.ship(),
                new Armament(List.of(SimSpecs.mount(CONTENT, weapon.slot(), weapon.weapon(), weapon.level(), 0))),
                base.shield(),
                base.plating());
    }

    /** Level 01 under water from its start, nothing hitting the ship. */
    private static Sortie sortie(Loadout loadout) {
        return new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM).toBuilder()
                        .medium(SimSpecs.underwaterFrom(CONTENT, 0))
                        .build(),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

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

    private boolean until(
            Sortie sortie, FlightSounds sounds, int commands, int steps, Predicate<SimEvents.Type> wanted) {
        for (int t = 0; t < steps; t++) {
            SimEvents events = step(sortie, sounds, commands);
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
