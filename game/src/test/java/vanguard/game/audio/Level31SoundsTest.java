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
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Loadout;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tow;

/**
 * M9 part D4b, Level 31's music cue and its claim rock (design/campaign Level 31, Music &amp; ambience and Secrets;
 * design/audio/music, Cues per level (Act 5): Vorne's motif "on"): track 13 "Gilded Cage" with its motif stem on top
 * from the base stem's start (section 2, t=24) to the end; Dacosta's claim, a rock tow, breaks with the small rubble
 * burst (round 08 b), not the lifeboat's cable snap.
 */
class Level31SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(31).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;

    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    /** The base stem from section 2 with the motif on top from its start, the full mix from the ambush, to the end. */
    @Test
    void theMotifPlaysOnTopFromTheBaseStemsStartToTheEnd() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(13, data.track(), "Act 5 B, Gilded Cage");
        String theme = Tracks.name(data.track()).orElseThrow();
        assertEquals("gilded-cage", theme);
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.motifPath(theme))), "track 13's motif stem");
        assertTrue(data.motif().isPresent(), "the level's motif cue");
        assertEquals(2, data.startSection());
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
        assertFalse(music.motifOn(), "no motif in the dock");
        music.update(2, 24.05, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the base stem with the motif from t=24");
        music.update(5, 120, 0.1f, false);
        assertTrue(data.full(5));
        assertTrue(music.motifOn(), "the motif over the full mix in the ambush");
        music.update(6, 183, 0.1f, false);
        assertTrue(music.motifOn(), "to the end");
        music.dispose();
        sfx.dispose();
    }

    /** The tows' break sound follows their look: the claim rock crumbles, the lifeboat and the buoy snap. */
    @Test
    void aRockTowCrumblesAndTheOthersSnap() {
        assertEquals(Optional.of("debris-claim-rock"), LevelRenderer.towLook(31));
        assertEquals(
                Sfx.CRUMBLE_SMALL, AllyCues.towBreak(LevelRenderer.towLook(31).orElseThrow()));
        assertEquals(Sfx.CABLE_SNAP, AllyCues.towBreak(LevelRenderer.towLook(30).orElseThrow()), "the dead buoy");
        assertEquals(Optional.empty(), LevelRenderer.towLook(7), "the lifeboat");
        assertTrue(Files.isRegularFile(ASSETS.resolve(Sfx.CRUMBLE_SMALL.path())));
    }

    /**
     * Flown to the claim rock (t=100) and its housing shot through with the Pulse Cannon (a tow takes the player's
     * shots, not a beam; Level31Test's flight), invulnerable: the housing's break plays the crumble, never the snap.
     */
    @Test
    void theClaimRocksHousingCrumblesAsItBreaks() {
        Loadout bolts = SimSpecs.loadout(
                CONTENT,
                CONTENT.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 3)),
                CONTENT.shields().models().get(2).name(),
                CONTENT.armour().plating().get(4).name(),
                0,
                Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                bolts,
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                bolts.plating().maxArmour());
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        sounds.towLook(LevelRenderer.towLook(31).orElseThrow());
        Tow tow = sortie.tow(0);
        boolean snap = false;
        double broke = Double.NaN;
        while (!sortie.complete() && sortie.levelSeconds() < 140 && Double.isNaN(broke)) {
            now[0] += NANOS_PER_STEP;
            boolean holding = tow.holding();
            int crumbles = bank.playing(Sfx.CRUMBLE_SMALL);
            double x = sortie.levelSeconds() < tow.spec().t() - 1
                    ? 240
                    : tow.present() ? tow.cableX() : tow.spec().x();
            sortie.step(steer(sortie, x, Command.FIRE.bit()));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            snap |= bank.playing(Sfx.CABLE_SNAP) > 0;
            if (holding && !tow.holding()) {
                broke = sortie.levelSeconds();
                assertTrue(bank.playing(Sfx.CRUMBLE_SMALL) > crumbles, "the crumble as the housing breaks");
            }
        }
        assertFalse(Double.isNaN(broke), "the housing shot through by t=140");
        assertFalse(snap, "no cable snap");
        sounds.stopLoops();
        bank.dispose();
    }

    /** Steers the ship to {@code targetX} low on the screen, with {@code fire} (Level31Test's). */
    private static int steer(Sortie sortie, double targetX, int fire) {
        int commands = fire;
        if (sortie.ship().x() < targetX - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > targetX + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < 57) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > 63) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
