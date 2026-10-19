package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.render.EnemyLooks;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part B's Level 15 sounds as round 38 closed them (the user's picks of 2026-10-14, design/audio/sfx): the
 * bulge's crack layer b over the eruption, both from the bulge's start; the dive's grain slide b; the entry
 * roar a and the Mars dust wind a.
 */
class Level15SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(15).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    @Test
    void everySoundHasItsFileAndTheUsersPick() {
        for (Sfx sfx : List.of(
                Sfx.BURROW_ERUPTION,
                Sfx.BURROW_BULGE,
                Sfx.BURROW_DIVE,
                Sfx.AMBIENCE_MARS_ENTRY,
                Sfx.AMBIENCE_MARS_DUST)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        }
        assertTrue(Sfx.BURROW_BULGE.path().endsWith("burrow-bulge-r38-b.ogg"), "S.1 = b");
        assertTrue(Sfx.BURROW_DIVE.path().endsWith("burrow-dive-r38-b.ogg"), "S.2 = b");
        assertTrue(Sfx.AMBIENCE_MARS_ENTRY.path().endsWith("entry-roar-r38-a.ogg"), "S.3 = a");
        assertTrue(Sfx.AMBIENCE_MARS_DUST.path().endsWith("mars-dust-r38-a.ogg"), "S.4 = a");
        assertFalse(Files.exists(ASSETS.resolve("sfx/burrow-dive-r38-a.ogg")), "the dive's a left");
    }

    @Test
    void theBulgePlaysTheCrackLayerOverTheEruption() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        long[] now = {0};
        var bank = new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);
        var sounds = new FlightSounds(bank, new EnemyLooks[0], sortie.armament(), List.of());
        int bulges = 0;
        for (long t = 0; t < SimStep.ticks(80) && bulges == 0; t++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(0);
            sounds.step();
            SimEvents events = sortie.events();
            try {
                sounds.play(events);
            } catch (ArrayIndexOutOfBoundsException lookless) {
                // a kill without the test's looks
            }
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.BURROW_BULGE) {
                    assertTrue(bank.playing(Sfx.BURROW_ERUPTION) > 0, "the eruption from the bulge's start");
                    assertTrue(bank.playing(Sfx.BURROW_BULGE) > 0, "the crack layer over it");
                    bulges++;
                }
            }
        }
        assertEquals(1, bulges, "the first field's first bulge by t=80");
        bank.dispose();
    }
}
