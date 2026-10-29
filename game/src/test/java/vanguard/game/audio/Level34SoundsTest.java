package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.PickupType;

/**
 * M9 part G (step G4), Level 34's sounds (design/campaign Level 34, Music &amp; ambience; design/audio/sfx, Act 5
 * sounds; step AUD-G's round 67 proposals, their a variants until the picks): track 12 "Hollow Rock"'s base stem alone,
 * 3 dB down throughout, never the full mix; Vorne's motif stem once, t=106 to t=124 over the freighter's hold
 * ({@code music.motif.to}); the belt's open bed with the wrecks' groans as a layer to t=174; a container's burst, a
 * clamp's unlock and the core's hum swell over the large salvage pickup's sound.
 */
class Level34SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(34).orElseThrow();

    @Test
    void theBaseStemPlaysThreeDbDownEverywhereAndTheMotifOnlyOverTheHold() {
        LevelData level = CONTENT.level(KEY);
        LevelData.Music data = level.music();
        String theme = Tracks.name(data.track()).orElseThrow();
        assertEquals("hollow-rock", theme, "track 12");
        for (int section = 1; section <= level.sections().size(); section++) {
            assertFalse(data.full(section), "never the full mix: section " + section);
            assertEquals(-3, data.sectionLevel(section), 1e-9, "3 dB down: section " + section);
        }
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath(theme)),
                        files.internal(Tracks.path(theme)),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(files.internal(Tracks.motifPath(theme)), data::motifOn);
        music.update(1, 10, 0.1f, false);
        assertFalse(music.motifOn(), "no motif before the hold");
        music.update(3, 105.9, 0.1f, false);
        assertFalse(music.motifOn(), "nor just before it");
        music.update(4, 106.05, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the base stem with the motif from t=106");
        music.update(4, 123.9, 0.1f, false);
        assertTrue(music.motifOn(), "to t=124");
        music.update(4, 124.05, 0.1f, false);
        assertFalse(music.motifOn(), "out at t=124, inside section 4");
        music.update(5, 150, 0.1f, false);
        assertFalse(music.motifOn(), "once only");
        music.dispose();
        sfx.dispose();
    }

    @Test
    void theOpenBedTheWrecksGroansAndTheSalvageSoundsAreThere() {
        LevelData.Music music = CONTENT.level(KEY).music();
        assertEquals(Sfx.AMBIENCE_BELT_OPEN, Sfx.ambience(music.ambience()), "the belt's open bed");
        LevelData.Music.AmbienceLayer groans =
                music.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AMBIENCE_WRECK_GROANS, Sfx.ambience(groans.ambience()));
        assertEquals(174, groans.to(), 1e-9, "out before clear space");
        Map<Sfx, String> files = Map.of(
                Sfx.CONTAINER_BURST, "sfx/container-burst-r67-a.ogg",
                Sfx.CLAMP_RELEASE, "sfx/clamp-release-r62-b.ogg",
                Sfx.HELIX_CORE, "sfx/helix-core-r67-a.ogg",
                Sfx.AMBIENCE_WRECK_GROANS, "sfx/wreck-groans-r67-a.ogg",
                Sfx.CHARGE_BLAST, "sfx/charge-blast-r65-b.ogg");
        files.forEach((sfx, file) -> {
            assertEquals(file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        });
        assertTrue(Files.isRegularFile(ASSETS.resolve(Sfx.AMBIENCE_BELT_OPEN.path())));
        assertEquals(Sfx.HELIX_CORE, FlightSounds.pickupSound(PickupType.STORY), "the core's hum swell");
    }

    /** A CDF or Helix container bursts, a hold clamp unlocks; Level 01's plain container keeps the ground break alone. */
    @Test
    void theContainersVariantsBurstAndTheClampsUnlock() {
        assertEquals(Sfx.CONTAINER_BURST, DeepCoreCues.breakSound("cargo-container-cdf"));
        assertEquals(Sfx.CONTAINER_BURST, DeepCoreCues.breakSound("cargo-container-helix"));
        assertNull(DeepCoreCues.breakSound("cargo-container"), "Level 01's container");
        assertEquals(Sfx.CLAMP_RELEASE, DeepCoreCues.breakSound("hold-clamp-left"), "round 67's S.2: Dock 9's release");
        assertEquals(Sfx.CLAMP_RELEASE, DeepCoreCues.breakSound("hold-clamp-right"));
        assertEquals(Sfx.DOOR_GRIND, DeepCoreCues.breakSound("blast-door-left"));
        assertNull(DeepCoreCues.breakSound("mining-charge"), "a charge bursts in its own blast");
    }
}
