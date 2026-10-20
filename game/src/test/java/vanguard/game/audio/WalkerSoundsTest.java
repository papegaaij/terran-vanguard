package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.render.WalkerFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;

/**
 * M7 part F: the Revenant Walker's sounds (design/audio/sfx, Revenant Walker): round 42's proposals (a each) until the
 * pick, in its arena ({@link WalkerFixture}): the stomp at each footfall, the hatch's latch and hiss before a salvo,
 * a missile's launch (one sound for the missiles of one step), the flamer's rising hiss with the cone's tell and the
 * flame's roar as a loop while it burns; its death is the boss's chained death on the large rung (a mid-boss).
 */
class WalkerSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void everyWalkerSoundHasItsFile() {
        for (Sfx sfx : List.of(
                Sfx.WALKER_STOMP,
                Sfx.WALKER_HATCH,
                Sfx.WALKER_MISSILE,
                Sfx.WALKER_FLAME_TELL,
                Sfx.WALKER_FLAME,
                Sfx.WALKER_VENT_HIT,
                Sfx.WALKER_DEATH,
                Sfx.WALKER_WRECK_SETTLE)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        }
        // Round 43's picks (the user, 2026-10-20); the stomp stays round 42's a, the missile is the chosen r08 launch.
        assertEquals("sfx/walker-stomp-r42-a.ogg", Sfx.WALKER_STOMP.path());
        assertEquals("sfx/walker-hatch-r43-b.ogg", Sfx.WALKER_HATCH.path());
        assertEquals("sfx/enemy-missile-r08-a.ogg", Sfx.WALKER_MISSILE.path());
        assertEquals("sfx/walker-flame-tell-r43-a.ogg", Sfx.WALKER_FLAME_TELL.path());
        assertEquals("sfx/walker-flame-r43-b.ogg", Sfx.WALKER_FLAME.path());
        assertEquals("sfx/walker-vent-hit-r43-a.ogg", Sfx.WALKER_VENT_HIT.path());
        assertEquals("sfx/walker-death-r43-a.ogg", Sfx.WALKER_DEATH.path());
        assertEquals("sfx/walker-wreck-settle-r43-a.ogg", Sfx.WALKER_WRECK_SETTLE.path());
        assertEquals(Sfx.Priority.EXPLOSION, Sfx.WALKER_DEATH.priority(), "its death on the explosion rung");
        assertEquals(1f, WalkerCues.WALKER_LEVEL, "levelled in their files");
    }

    @Test
    void aVentHitSoundsAtMostOnceEachFifteenHundredths() {
        assertTrue(WalkerCues.ventHitDue(Long.MIN_VALUE / 2, 0), "the first");
        assertFalse(WalkerCues.ventHitDue(100, 100 + WalkerCues.VENT_HIT_GAP_STEPS - 1), "a beam's next tick");
        assertTrue(WalkerCues.ventHitDue(100, 100 + SimStep.ticks(0.15)));
        assertTrue(WalkerCues.ventHitDue(100, 5), "after a restart the clock starts again");
    }

    /**
     * Round 44: the rail lance's charge and shot, the stomp's brace and shockwave (placeholders: the Mantis's charge-up
     * and sweep, the hatch and the footfall pitched down), once its pods are gone; its death its own, the wreck's groan
     * as the topple lands.
     */
    @Test
    void theLanceChargesAndFiresTheStompBracesAndItsDeathAndWreckSoundTheirOwn() {
        Sortie sortie = WalkerFixture.sortie();
        FlightSounds sounds = new FlightSounds(
                bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of("revenant-walker"));
        assertTrue(until(sortie, sounds, 20, type -> type == SimEvents.Type.BOSS_SETTLED), "it engaged");
        var walker = WalkerFixture.walker(sortie);
        vanguard.sim.WalkerHooks.damage(sortie, 0, WalkerFixture.LEFT_POD, 1000);
        vanguard.sim.WalkerHooks.damage(sortie, 0, WalkerFixture.RIGHT_POD, 1000);
        assertTrue(until(sortie, sounds, 6, type -> type == SimEvents.Type.LANCE_CHARGE), "the lance charges");
        assertTrue(bank.playing(Sfx.MANTIS_TELEGRAPH) > 0, "its charge-up");
        assertTrue(until(sortie, sounds, 2, type -> type == SimEvents.Type.LANCE_FIRE), "the lance fires");
        assertTrue(bank.playing(Sfx.MANTIS_SWEEP) > 0, "its shot");
        boolean braced = false;
        for (int t = 0; t < SimStep.ticks(30) && !braced; t++) {
            var stride = walker.stride().orElseThrow();
            int commands = stride.offScreen() ? Command.NONE : steer(sortie, walker.renderX(1), walker.renderY(1));
            run(sortie, sounds, commands, 1);
            braced = stride.strikes().stompPhase() != vanguard.sim.StrideStrikes.StompPhase.IDLE;
        }
        assertTrue(braced, "it braces for a stomp with the ship over it");
        assertTrue(bank.playing(Sfx.WALKER_HATCH) > 0, "the brace");
        vanguard.sim.WalkerHooks.destroy(sortie, 0, WalkerFixture.HULL);
        sounds.play(sortie.events());
        assertTrue(bank.playing(Sfx.WALKER_DEATH) > 0, "its own death");
        run(sortie, sounds, Command.NONE, SimStep.ticks(2.2));
        assertTrue(bank.playing(Sfx.WALKER_WRECK_SETTLE) > 0, "the wreck's groan as it lands");
    }

    @Test
    void aStepsMissilesShareOneLaunchSound() {
        assertTrue(WalkerCues.launchDue(Long.MIN_VALUE / 2, 0), "the first");
        assertFalse(WalkerCues.launchDue(100, 100), "the other pod's missile in the same step");
        assertTrue(WalkerCues.launchDue(100, 100 + SimStep.ticks(0.2)), "a salvo's next missile, 0.2 s on");
        assertTrue(WalkerCues.launchDue(100, 5), "after a restart the clock starts again");
    }

    @Test
    void itStompsItsHatchesHissAndItsMissilesLaunch() {
        Sortie sortie = WalkerFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        assertTrue(until(sortie, sounds, 20, type -> type == SimEvents.Type.WALKER_STEP), "a foot came down");
        assertTrue(bank.playing(Sfx.WALKER_STOMP) > 0, "the stomp");
        assertTrue(until(sortie, sounds, 20, type -> type == SimEvents.Type.POD_HATCH), "a hatch opened");
        assertTrue(bank.playing(Sfx.WALKER_HATCH) > 0, "the latch and hiss (the tell)");
        assertTrue(until(sortie, sounds, 2, type -> type == SimEvents.Type.MISSILE_LAUNCHED), "its salvo");
        assertTrue(bank.playing(Sfx.WALKER_MISSILE) > 0, "the launch");
    }

    /** The flame's loop runs while the cone burns (the ship flown behind it), and stops with the flight. */
    @Test
    void theFlameRoarsWhileTheConeBurns() {
        Sortie sortie = WalkerFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean told = false;
        boolean burnt = false;
        for (int t = 0; t < SimStep.ticks(90) && !burnt; t++) {
            var stride = WalkerFixture.walker(sortie).stride().orElseThrow();
            // Keep behind it: steer toward a point behind its flamer.
            double behindX = stride.coneX() - Math.sin(stride.coneHeading()) * 60;
            double behindY = stride.coneY() - Math.cos(stride.coneHeading()) * 60;
            int commands = stride.offScreen() ? Command.NONE : steer(sortie, behindX, behindY);
            run(sortie, sounds, commands, 1);
            told |= stride.conePhase() == StrideArena.ConePhase.TELL && bank.playing(Sfx.WALKER_FLAME_TELL) > 0;
            if (stride.conePhase() == StrideArena.ConePhase.BURN) {
                run(sortie, sounds, commands, 3);
                burnt = bank.looping(Sfx.WALKER_FLAME);
            }
        }
        assertTrue(told, "the rising hiss with the tell");
        assertTrue(burnt, "the roar while it burns");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.WALKER_FLAME));
    }

    private static int steer(Sortie sortie, double x, double y) {
        double sx = sortie.ship().x();
        double sy = sortie.ship().y();
        int bits = Command.NONE;
        if (x < sx - 4) {
            bits |= Command.LEFT.bit();
        } else if (x > sx + 4) {
            bits |= Command.RIGHT.bit();
        }
        if (y < sy - 4) {
            bits |= Command.DOWN.bit();
        } else if (y > sy + 4) {
            bits |= Command.UP.bit();
        }
        return bits;
    }

    private void run(Sortie sortie, FlightSounds sounds, int commands, int steps) {
        for (int t = 0; t < steps; t++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(commands);
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
        }
    }

    /** Steps for at most {@code seconds} until an event of the kind shows; whether it did. */
    private boolean until(Sortie sortie, FlightSounds sounds, double seconds, Predicate<SimEvents.Type> wanted) {
        for (int t = 0; t < SimStep.ticks(seconds); t++) {
            run(sortie, sounds, Command.NONE, 1);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (wanted.test(events.type(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
