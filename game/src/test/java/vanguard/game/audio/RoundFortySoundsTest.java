package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.RoundFortyFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Command;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Round 40's sounds (pending the user's review), on the hand-built sorties of {@link RoundFortyFixture}: a
 * rooted Kraken root's wet hit (the Polyp Mortar's impact, reused), spaced out per root, and its cut's medium
 * blast; a Spire pod's aim tell (the Mantis's charge-up, reused, higher and quieter than the sweep's).
 */
class RoundFortySoundsTest {
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    /** A sortie flown with its sounds. */
    private static final class Flight implements AutoCloseable {
        final Sortie sortie;
        final long[] now = {0};
        final SfxBank bank;
        final FlightSounds sounds;

        Flight(Sortie sortie) {
            this.sortie = sortie;
            bank = new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);
            sounds = new FlightSounds(
                    bank,
                    new EnemyLooks[0],
                    sortie.armament(),
                    sortie.script().setPieces().stream()
                            .map(vanguard.sim.SetPieceSpec::slug)
                            .toList());
        }

        void step(int commands) {
            now[0] += NANOS_PER_STEP;
            sortie.step(commands);
            sounds.step();
            try {
                sounds.play(sortie.events());
            } catch (ArrayIndexOutOfBoundsException lookless) {
                // a kill of a lookless test fixture
            }
            sounds.watch(sortie);
        }

        /** Steps until an event of {@code type} passes {@code test}; its value. */
        int until(SimEvents.Type type, double seconds, int commands, Predicate<Integer> test) {
            for (int i = 0; i < SimStep.ticks(seconds); i++) {
                step(commands);
                SimEvents events = sortie.events();
                for (int e = 0; e < events.size(); e++) {
                    if (events.type(e) == type && test.test(events.value(e))) {
                        return events.value(e);
                    }
                }
            }
            throw new AssertionError(type + " within " + seconds + " s");
        }

        @Override
        public void close() {
            bank.dispose();
        }
    }

    @Test
    void aRootHitSplatsWetlySpacedOutAndItsCutBlasts() {
        assertTrue(FlightSounds.rootHitDue(Long.MIN_VALUE / 2, 0));
        assertFalse(FlightSounds.rootHitDue(100, 100 + FlightSounds.ROOT_HIT_GAP_STEPS - 1));
        assertTrue(FlightSounds.rootHitDue(100, 100 + FlightSounds.ROOT_HIT_GAP_STEPS));
        assertTrue(FlightSounds.ROOT_HIT_RAISED_LEVEL > FlightSounds.ROOT_HIT_LEVEL, "meatier while raised");
        try (Flight flight = new Flight(RoundFortyFixture.kraken(1))) {
            flight.until(SimEvents.Type.BOSS_SETTLED, 6, 0, v -> true);
            int root = 3;
            double x = flight.sortie.setPiece(0).partX(root);
            while (flight.sortie.ship().x() < x - 1) {
                flight.step(Command.RIGHT.bit());
            }
            flight.until(SimEvents.Type.ROOT_HIT, 3, Command.FIRE.bit(), v -> v == root);
            assertEquals(1, flight.bank.playing(Sfx.MORTAR_IMPACT), "the wet hit");
            // A stream of hits splats at most once each gap: no second splat within it, one after it.
            int hits = 0;
            for (int i = 1; i < FlightSounds.ROOT_HIT_GAP_STEPS; i++) {
                flight.step(Command.FIRE.bit());
                hits += flight.sortie.events().count(SimEvents.Type.ROOT_HIT);
                assertEquals(1, flight.bank.playing(Sfx.MORTAR_IMPACT), "within the gap");
            }
            assertTrue(hits >= 2, "the stream hit on: " + hits);
            int most = 1;
            for (int i = 0; i < FlightSounds.ROOT_HIT_GAP_STEPS; i++) {
                flight.step(Command.FIRE.bit());
                most = Math.max(most, flight.bank.playing(Sfx.MORTAR_IMPACT));
            }
            assertEquals(2, most, "the next splat after the gap");
            flight.until(SimEvents.Type.PART_DESTROYED, 30, Command.FIRE.bit(), v -> v == root);
            assertTrue(
                    flight.bank.playing(Sfx.EXPLOSION_MEDIUM_A) + flight.bank.playing(Sfx.EXPLOSION_MEDIUM_B) > 0,
                    "the cut's blast");
        }
    }

    @Test
    void aPodsAimTellPlaysTheChargeUp() {
        assertTrue(FlightSounds.AIM_TELL_PITCH > 1, "higher than the sweep's telegraph");
        assertTrue(FlightSounds.AIM_TELL_LEVEL < 1, "and quieter");
        try (Flight flight = new Flight(RoundFortyFixture.spire(1))) {
            flight.until(SimEvents.Type.AIM_TELL, 20, 0, v -> true);
            assertTrue(flight.bank.playing(Sfx.MANTIS_TELEGRAPH) > 0, "the charge-up at the tell");
        }
    }
}
