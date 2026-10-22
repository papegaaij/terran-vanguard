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
import vanguard.content.BackdropData;
import vanguard.content.LevelData;
import vanguard.game.render.AbyssalPlainFixture;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.RisePhase;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part D's Level 24 sounds (design/audio/sfx, Level 24's rows; round 53's a variants until the user's pick): the
 * Abyss Ray's horn at its shadow, its strokes as it starts its glide, its fans; the Barbfin's needles; the spawning
 * reefs' iris instead of the Hive Node's; a Ray's and a reef's death over their bursts; the relay beacons' pulse while
 * one is on the screen; the Meridian's hatch; track 10 "Thera Deep" from the reef field, its full mix along the beacon
 * line and the base stem again over the Ray crossing, over the bed under the ice. Round 52's picks are in the assets
 * (and the AI-made under-water file is not).
 */
class Level24SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    private static final List<Sfx> SOUNDS = List.of(
            Sfx.RAY_RISE,
            Sfx.RAY_GLIDE,
            Sfx.RAY_FAN,
            Sfx.RAY_DEATH,
            Sfx.BARBFIN_NEEDLE,
            Sfx.REEF_IRIS,
            Sfx.REEF_DEATH,
            Sfx.BEACON_PULSE,
            Sfx.WRECK_HATCH);

    @Test
    void everySoundHasItsFileRoundFiftyThreesA() {
        for (Sfx sfx : SOUNDS) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r53-a.ogg"), "round 53's a until the pick: " + sfx.path());
            assertTrue(sfx.priority() != Sfx.Priority.AMBIENCE, "a one-shot, not a loop: " + sfx);
        }
        assertFalse(
                Files.exists(ASSETS.resolve("sfx/explosion-underwater-r04-a.ogg")),
                "round 52's Q.1: the AI-made file left the assets");
    }

    @Test
    void theMusicIsTheraDeepFullAlongTheBeaconsAndTheBaseAgainOverTheCrossing() {
        LevelData.Music music = AbyssalPlainFixture.level().music();
        assertEquals(10, music.track());
        assertEquals(2, music.startSection(), "the bed alone at the plain's edge, the base stem from the reef field");
        assertFalse(music.full(2));
        assertTrue(music.full(3), "the full mix along the beacon line");
        assertFalse(music.full(4), "the base stem again over the Ray crossing");
        assertEquals(Sfx.AMBIENCE_EUROPA_UNDER, Sfx.ambience(music.ambience()));
    }

    /** A Ray's and a reef's kill sound over their bursts; another kill has no extra sound. */
    @Test
    void theRayAndTheReefDieWithTheirOwnSounds() {
        Sortie sortie = AbyssalPlainFixture.sortie();
        AbyssCues abyss = new AbyssCues(new SoundMix(bank));
        abyss.noteKinds(sortie.enemyKinds());
        List<EnemySpec> kinds = sortie.enemyKinds();
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            boolean own = slug.equals(AbyssCues.RAY) || slug.equals(AbyssCues.REEF);
            assertEquals(own, abyss.destroyed(k, 0), slug);
            assertEquals(slug.equals(AbyssCues.REEF), abyss.released(k), slug);
        }
        assertTrue(bank.playing(Sfx.RAY_DEATH) > 0);
        assertTrue(bank.playing(Sfx.REEF_DEATH) > 0);
    }

    /** The Meridian's hatch lets go as the wreck takes its last hit; another ground object's end is not it. */
    @Test
    void theMeridiansHatchLetsGo() {
        Sortie sortie = AbyssalPlainFixture.sortie();
        AbyssCues abyss = new AbyssCues(new SoundMix(bank));
        var objects = sortie.script().groundObjects();
        abyss.noteGround(objects);
        int wrecks = 0;
        for (int g = 0; g < objects.size(); g++) {
            boolean wreck = AbyssCues.WRECK.equals(objects.get(g).look());
            wrecks += wreck ? 1 : 0;
            assertEquals(wreck, abyss.spent(g, 0), objects.get(g).look());
        }
        assertEquals(1, wrecks, "the Meridian");
        assertTrue(bank.playing(Sfx.WRECK_HATCH) > 0);
    }

    /** The beacons are heard while one is on the screen: the three of the backdrop, none before the beacon line. */
    @Test
    void theBeaconsAreHeardWhileOnTheScreen() {
        LevelData level = AbyssalPlainFixture.level();
        AbyssCues abyss = new AbyssCues(new SoundMix(bank));
        abyss.noteBeacons(level);
        List<BackdropData.PlacedPiece> beacons = level.backdrop().placements().stream()
                .filter(placed -> placed.piece().equals(AbyssCues.BEACON))
                .toList();
        assertEquals(3, beacons.size());
        assertEquals(-1, abyss.beaconOnScreen(0), "the plain's edge");
        for (int b = 0; b < beacons.size(); b++) {
            double centre = level.pieceCentre(beacons.get(b));
            assertEquals(b, abyss.beaconOnScreen(centre - PlayField.HEIGHT / 2.0), "beacon " + (b + 1));
        }
    }

    /**
     * Flown over the plain to the beacon line, invulnerable, firing from the left of the middle: the Barbfins' needles,
     * the reefs' iris (never the Hive Node's), the first Ray's horn at its shadow, its strokes as it glides and its fans,
     * the beacons' pulse along the beacon line.
     */
    @Test
    void thePlainsUnitsBeaconsAndWreckSound() {
        Sortie sortie = AbyssalPlainFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        sounds.beacons(AbyssalPlainFixture.level());
        boolean needle = false;
        boolean iris = false;
        boolean horn = false;
        boolean strokes = false;
        boolean fan = false;
        boolean pulse = false;
        boolean wasGliding = false;
        while (sortie.levelSeconds() < 125 && !(needle && iris && horn && strokes && fan && pulse)) {
            step(sortie, sounds, IceShelfFixture.toward(sortie, 160, 100));
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.RISE_SHADOW) {
                    horn = true;
                    assertTrue(bank.playing(Sfx.RAY_RISE) > 0, "the horn at the shadow");
                }
            }
            var ray = AbyssalPlainFixture.first(sortie, AbyssCues.RAY);
            boolean gliding = ray != null && ray.risePhase() == RisePhase.GLIDE;
            if (gliding && !wasGliding) {
                strokes = true;
                assertTrue(bank.playing(Sfx.RAY_GLIDE) > 0, "the strokes as it glides");
            }
            wasGliding = gliding;
            needle |= bank.playing(Sfx.BARBFIN_NEEDLE) > 0;
            iris |= bank.playing(Sfx.REEF_IRIS) > 0;
            fan |= bank.playing(Sfx.RAY_FAN) > 0;
            pulse |= bank.playing(Sfx.BEACON_PULSE) > 0;
            assertEquals(0, bank.playing(Sfx.HIVE_IRIS), "no Hive Node's iris on the plain");
            if (sortie.levelSeconds() < 95) {
                assertEquals(0, bank.playing(Sfx.BEACON_PULSE), "no beacon before the beacon line");
            }
        }
        assertTrue(needle, "a Barbfin's needle");
        assertTrue(iris, "a reef's iris");
        assertTrue(horn, "the Ray's horn");
        assertTrue(strokes, "its strokes");
        assertTrue(fan, "its fans");
        assertTrue(pulse, "the beacons' pulse");
        sounds.stopLoops();
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
