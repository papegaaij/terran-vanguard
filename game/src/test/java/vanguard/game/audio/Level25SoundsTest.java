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
import vanguard.content.LevelData;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.render.SirenTrenchFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part E's Level 25 sounds (design/audio/sfx, Level 25's rows; round 54's a variants until the user's pick): a
 * Siren's song from its sac's tell, the falling tone as a ring slows the ship, a Siren's and a Spiral Nautilus's death
 * over their bursts, a Nautilus's shell scraping open at each uncoil and the shells' spin loop while one is on the
 * screen; the survey sub lets go with the Meridian's hatch; track 11 "Pressure Line" over the trench's own bed.
 */
class Level25SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    private static final List<Sfx> SOUNDS = List.of(
            Sfx.SIREN_RING,
            Sfx.SIREN_SLOW,
            Sfx.SIREN_DEATH,
            Sfx.NAUTILUS_SPIN,
            Sfx.NAUTILUS_UNCOIL,
            Sfx.NAUTILUS_DEATH,
            Sfx.AMBIENCE_EUROPA_TRENCH);

    @Test
    void everySoundHasItsFileRoundFiftyFoursA() {
        for (Sfx sfx : SOUNDS) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r54-a.ogg"), "round 54's a until the pick: " + sfx.path());
        }
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_EUROPA_TRENCH.priority());
        assertTrue(Sfx.NAUTILUS_SPIN.priority() != Sfx.Priority.AMBIENCE, "a unit's loop is stolen like its peers");
    }

    @Test
    void theMusicIsPressureLineOverTheTrenchsBed() {
        LevelData.Music music = SirenTrenchFixture.level().music();
        assertEquals(11, music.track());
        assertEquals(2, music.startSection(), "the bed alone over the trench's mouth");
        assertFalse(music.full(4));
        assertTrue(music.full(5), "the full mix in the Song");
        assertEquals(Sfx.AMBIENCE_EUROPA_TRENCH, Sfx.ambience(music.ambience()));
        assertTrue(music.ambienceFrom().isPresent(), "the theme fades out over the floor");
    }

    /** A Siren's and a Nautilus's kill sound over their bursts; another kill has no extra sound of theirs. */
    @Test
    void theSirenAndTheNautilusDieWithTheirOwnSounds() {
        Sortie sortie = SirenTrenchFixture.sortie();
        TrenchCues trench = new TrenchCues(new SoundMix(bank));
        trench.noteKinds(sortie.enemyKinds());
        List<EnemySpec> kinds = sortie.enemyKinds();
        boolean siren = false;
        boolean nautilus = false;
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            boolean own = slug.equals(TrenchCues.SIREN) || slug.equals(TrenchCues.NAUTILUS);
            siren |= slug.equals(TrenchCues.SIREN);
            nautilus |= slug.equals(TrenchCues.NAUTILUS);
            assertEquals(own, trench.destroyed(k, 0), slug);
        }
        assertTrue(siren && nautilus, "both units fly in Level 25");
        assertTrue(bank.playing(Sfx.SIREN_DEATH) > 0);
        assertTrue(bank.playing(Sfx.NAUTILUS_DEATH) > 0);
    }

    /** The survey sub lets go with the Meridian's hatch (round 53's pick); no other ground object does. */
    @Test
    void theSurveySubLetsGoWithTheHatch() {
        Sortie sortie = SirenTrenchFixture.sortie();
        AbyssCues abyss = new AbyssCues(new SoundMix(bank));
        var objects = sortie.script().groundObjects();
        abyss.noteGround(objects);
        int subs = 0;
        for (int g = 0; g < objects.size(); g++) {
            boolean sub = AbyssCues.SURVEY_SUB.equals(objects.get(g).look());
            subs += sub ? 1 : 0;
            assertEquals(sub, abyss.spent(g, 0), objects.get(g).look());
        }
        assertEquals(1, subs, "the survey sub");
        assertTrue(bank.playing(Sfx.WRECK_HATCH) > 0);
    }

    /**
     * Flown into the trench, invulnerable, not firing, held low in its middle: a Siren's song at each sac's tell, the
     * falling tone as a ring slows the ship, the first Nautilus pair's shells scraping open and their spin while one is
     * on the screen (silent before the first pair).
     */
    @Test
    void theNestsSingSlowTheShipAndTheShellsSpinAndOpen() {
        Sortie sortie = SirenTrenchFixture.sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean song = false;
        boolean slow = false;
        boolean uncoil = false;
        boolean spin = false;
        while (sortie.levelSeconds() < 95 && !(song && slow && uncoil && spin)) {
            step(sortie, sounds, SirenTrenchFixture.hold(sortie, 240, 110));
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case RING_TELL -> {
                        song = true;
                        assertTrue(bank.playing(Sfx.SIREN_RING) > 0, "the song at the tell");
                    }
                    case SLOWED -> {
                        if (events.value(e) == 0) {
                            slow = true;
                            assertTrue(bank.playing(Sfx.SIREN_SLOW) > 0, "the falling tone at the slow");
                        }
                    }
                    case UNCOIL -> {
                        uncoil = true;
                        assertTrue(bank.playing(Sfx.NAUTILUS_UNCOIL) > 0, "the scrape at the uncoil");
                    }
                    default -> {}
                }
            }
            if (sortie.levelSeconds() < 62) {
                assertFalse(bank.looping(Sfx.NAUTILUS_SPIN), "no spin before the first pair");
            }
            spin |= bank.looping(Sfx.NAUTILUS_SPIN);
        }
        assertTrue(song, "a Siren's song");
        assertTrue(slow, "the slow's tone");
        assertTrue(uncoil, "a shell scraping open");
        assertTrue(spin, "the shells' spin");
        sounds.stopLoops();
        assertFalse(bank.looping(Sfx.NAUTILUS_SPIN), "the spin stops with the flight");
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
