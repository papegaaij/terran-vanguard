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
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.render.EnemyLooks;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Loadout;
import vanguard.sim.Missiles;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part A's sounds (step A3; design/audio/sfx, Act 5 sounds; round 60's picks from step AUD-A): every key plays its
 * picked file; the Tesla Coil Pod's arcs zap (its {@code tesla} family) only with a target; the hex shield's hit, break
 * and refill; the homing diamonds' launch (a salvo shares one), fall and run loop in a level of diamonds, the walker's
 * sounds elsewhere; the brown-out hum while the load is over the drained output.
 */
class ActFiveSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(1).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everyActFiveSoundPlaysRoundSixtysPick() {
        Map<Sfx, String> picks = Map.ofEntries(
                Map.entry(Sfx.TESLA_SHOT, "shot-tesla-r03-a.ogg"),
                Map.entry(Sfx.ENEMY_SHIELD_HIT, "enemy-shield-hit-r60-a.ogg"),
                Map.entry(Sfx.ENEMY_SHIELD_BREAK, "enemy-shield-break-r60-b.ogg"),
                Map.entry(Sfx.ENEMY_SHIELD_REFILL, "enemy-shield-refill-r60-a.ogg"),
                Map.entry(Sfx.HOMING_LAUNCH, "homing-launch-r60-b.ogg"),
                Map.entry(Sfx.HOMING_RUN, "homing-run-r60-a.ogg"),
                Map.entry(Sfx.HOMING_DOWN, "homing-down-r60-b.ogg"),
                Map.entry(Sfx.BROWNOUT_HUM, "brownout-hum-r60-a.ogg"),
                Map.entry(Sfx.INTERCEPT_OPEN, "intercept-open-r60-a.ogg"),
                Map.entry(Sfx.INTERCEPT_CLOSE, "intercept-close-r60-a.ogg"),
                Map.entry(Sfx.INTERCEPT_CARRIER, "intercept-carrier-r60-a.ogg"));
        picks.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertEquals(Bus.RADIO, Sfx.INTERCEPT_OPEN.bus(), "the radio's bus, as the squelch");
        assertEquals(Bus.RADIO, Sfx.INTERCEPT_CARRIER.bus());
    }

    /**
     * The pod arcs with a target only, and its report (its {@code SHOT_FIRED}, which plays the {@code tesla} family's zap)
     * comes with each arc and never without one.
     */
    @Test
    void theTeslaCoilPodZapsWithEachArc() {
        assertEquals(
                Sfx.TESLA_SHOT, PlayerCues.shot(CONTENT.weapon("tesla-coil-pod").sfx()));
        Sortie sortie = sortie(new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 1));
        int arcs = 0;
        for (int t = 0; t < SimStep.ticks(60); t++) {
            sortie.step(Command.FIRE.bit());
            SimEvents events = sortie.events();
            assertEquals(
                    events.count(SimEvents.Type.ARC),
                    events.count(SimEvents.Type.SHOT_FIRED),
                    "a report with each arc only, step " + t);
            arcs += events.count(SimEvents.Type.ARC);
        }
        assertTrue(arcs > 0, "Level 01's drones came in reach");
    }

    @Test
    void theShieldsHitBreakAndRefillPlayTheirCues() {
        BeltCues belt = new BeltCues(new SoundMix(bank));
        for (SimEvents.Type type : List.of(
                SimEvents.Type.ENEMY_SHIELD_HIT,
                SimEvents.Type.ENEMY_SHIELD_BROKEN,
                SimEvents.Type.ENEMY_SHIELD_REFILLED)) {
            assertTrue(belt.types().contains(type), type.name());
        }
        assertEquals(Sfx.ENEMY_SHIELD_HIT, BeltCues.cue(SimEvents.Type.ENEMY_SHIELD_HIT));
        assertEquals(Sfx.ENEMY_SHIELD_BREAK, BeltCues.cue(SimEvents.Type.ENEMY_SHIELD_BROKEN));
        assertEquals(Sfx.ENEMY_SHIELD_REFILL, BeltCues.cue(SimEvents.Type.ENEMY_SHIELD_REFILLED));
        bank.dispose();
    }

    @Test
    void homingDiamondsLaunchFallAndRunOnlyInALevelOfDiamonds() {
        SoundMix mix = new SoundMix(bank);
        BeltCues belt = new BeltCues(mix);
        assertFalse(belt.launched(0), "a level without diamonds: the walker's launch");
        assertFalse(belt.gone(Missiles.SHOT_DOWN, 0));
        assertEquals(0, bank.playing(Sfx.HOMING_LAUNCH));
        belt.diamonds(true);
        belt.begin(100);
        assertTrue(belt.launched(0));
        assertTrue(belt.launched(0), "a salvo's second missile in the same step");
        assertEquals(1, bank.playing(Sfx.HOMING_LAUNCH), "shares the first's launch");
        assertTrue(BeltCues.launchDue(100, 100 + BeltCues.LAUNCH_GAP_STEPS));
        assertFalse(BeltCues.launchDue(100, 101));
        assertTrue(belt.gone(Missiles.SHOT_DOWN, 0));
        assertEquals(1, bank.playing(Sfx.HOMING_DOWN), "shot down: the crack");
        assertTrue(belt.gone(Missiles.FIZZLED, 0));
        assertTrue(belt.gone(Missiles.STRUCK, 0));
        assertEquals(1, bank.playing(Sfx.HOMING_DOWN), "one that ran out or struck a hull plays nothing of its own");
        bank.dispose();
    }

    /** The hand plan's 20/20 fit: two drainers stop the regen (the hum), shaken off it stops. */
    @Test
    void theShipBrownsOutWhileItsLoadIsOverTheDrainedOutput() {
        String shield = CONTENT.shields().models().get(2).name();
        Loadout loadout = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM)
                .withPower(SimSpecs.power(CONTENT, 20, 20, shield, Difficulty.MEDIUM));
        Sortie sortie = new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        launch(sortie, sounds);
        sounds.watch(sortie);
        assertFalse(bank.looping(Sfx.BROWNOUT_HUM));
        sortie.drain(+1);
        step(sortie, sounds, 0);
        sounds.watch(sortie);
        assertTrue(bank.looping(Sfx.BROWNOUT_HUM), "18 MW for a 20 MW load");
        sortie.drain(-1);
        step(sortie, sounds, 0);
        sounds.watch(sortie);
        assertFalse(bank.looping(Sfx.BROWNOUT_HUM), "shaken off: the load fits again");
        sounds.stopLoops();
        bank.dispose();
    }

    private static Sortie sortie(SimSpecs.FittedWeapon weapon) {
        Loadout base = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM);
        Loadout loadout = new Loadout(
                base.ship(),
                new Armament(List.of(SimSpecs.mount(CONTENT, weapon.slot(), weapon.weapon(), weapon.level(), 0))),
                base.shield(),
                base.plating());
        return new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    private void launch(Sortie sortie, FlightSounds sounds) {
        for (int t = 0; t < SimStep.ticks(10) && sortie.launching(); t++) {
            step(sortie, sounds, 0);
        }
        assertFalse(sortie.launching(), "launched");
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
        return events;
    }
}
