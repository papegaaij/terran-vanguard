package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.game.render.DomesFixture;
import vanguard.game.render.EnemyLooks;
import vanguard.game.render.JovianFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.ChainFire;
import vanguard.sim.Command;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3b: the sounds of Act 6's gear and hazards (design/audio/sfx, Act 6 sounds): round 72's picks for
 * the Reflex ricochet, the lightning's tell and strike and the eruption play their files; the Plasma Arc replays the
 * {@code tesla} zap per strike a semitone a jump, all on the discharge's tick, and the family has the voices for a
 * whole chain; one family per event.
 */
class ActSixHazardSoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theHazardsAndTheReflexPlayRoundSeventyTwosPicks() {
        Map<Sfx, String> picks = Map.of(
                Sfx.REFLEX_RICOCHET, "reflex-ricochet-r72-a.ogg",
                Sfx.LIGHTNING_TELL, "lightning-tell-r72-a.ogg",
                Sfx.LIGHTNING_STRIKE, "lightning-strike-r72-b.ogg",
                Sfx.ERUPTION, "eruption-r72-a.ogg");
        picks.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        for (Sfx standIn : List.of(JovianHazardCues.KNOCK_STAND_IN, JovianHazardCues.CRACK_STAND_IN)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(standIn.path())), standIn.path());
        }
        bank.dispose();
    }

    @Test
    void everyHazardEventHasOneFamily() {
        // FlightSounds throws when two families claim one event.
        new FlightSounds(bank, new EnemyLooks[0], JovianFixture.arcFlight().armament(), List.of());
        JovianHazardCues cues = new JovianHazardCues(new SoundMix(bank));
        for (SimEvents.Type type : List.of(
                SimEvents.Type.STRIKE_TOLD,
                SimEvents.Type.STRIKE,
                SimEvents.Type.ERUPTION_TELL,
                SimEvents.Type.ROCK_HIT,
                SimEvents.Type.ROCK_DESTROYED,
                SimEvents.Type.ROCK_STRUCK)) {
            assertTrue(cues.types().contains(type), type.name());
        }
        bank.dispose();
    }

    @Test
    void eachJumpZapsASemitoneAboveTheOneBefore() {
        assertEquals(1, PlayerCues.jumpPitch(0), 1e-6);
        assertEquals(1.06, PlayerCues.jumpPitch(1), 1e-6);
        assertEquals(1.34, PlayerCues.jumpPitch(5), 0.005, "a six-strike chain ends ×1.34");
        int most = DomesFixture.CONTENT.weapon("plasma-arc").levels().stream()
                .mapToInt(level -> level.chain().orElse(0))
                .max()
                .orElseThrow();
        assertTrue(Sfx.TESLA_SHOT.instanceLimit() >= most, "a whole chain's zaps on one tick: " + most);
        bank.dispose();
    }

    @Test
    void aDischargeZapsOncePerStrike() {
        Sortie sortie = JovianFixture.arcFlight();
        FlightSounds sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        int chained = 0;
        for (int t = 0; t < SimStep.ticks(20) && chained < 3; t++) {
            now[0] += NANOS_PER_STEP * 20;
            sortie.step(Command.FIRE.bit());
            int strikes = 0;
            for (int i = 0; i < sortie.events().size(); i++) {
                if (sortie.events().type(i) == SimEvents.Type.CHAIN_STRIKE) {
                    strikes = Math.max(
                            strikes, ChainFire.eventIndex(sortie.events().value(i)) + 1);
                }
            }
            sounds.play(sortie.events());
            if (strikes > 0) {
                assertEquals(
                        Math.min(strikes, Sfx.TESLA_SHOT.instanceLimit()),
                        bank.playing(Sfx.TESLA_SHOT),
                        "one zap a strike, none for the discharge itself");
                chained = Math.max(chained, strikes);
            }
        }
        assertTrue(chained >= 3, "a chain over the pack: " + chained);
        bank.dispose();
    }

    @Test
    void theRicochetIsThrottled() {
        assertTrue(PlayerCues.RICOCHET_GAP_SECONDS <= 0.06 + 1e-9, "once per 0.06 s (design/audio/sfx)");
        bank.dispose();
    }
}
