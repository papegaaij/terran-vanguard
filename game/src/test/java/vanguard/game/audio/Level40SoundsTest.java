package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;
import vanguard.sim.Tow;

/**
 * M10 part F (step F4), Level 40's sounds (design/campaign Level 40, Music &amp; ambience; design/enemies/bosses/
 * honour-guard, Sounds brief; round 78's a variants until the picks): track 14 with its motif to t=96, the stems out
 * over t=96-100; the mid-boss's own music (the music plan's hook (2)): the sting, then track 32 "Three Aces" for the
 * duel (no klaxon, no banner), the level track's full mix back after the last ace; the {@code belt-open} bed with the
 * fleet's layer over section 1; flown, the aces' fly-bys, the sniper's charge and shot, Kestrel's dive and pull-out,
 * Vesper's cannon loop, the deaths and the last ace's chain, and Sentry Two's recorder's crack.
 */
class Level40SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(40).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theMusicTheBedAndTheDuelsSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(14, data.track(), "Act 6 A, Eye of the Storm");
        assertEquals(Optional.of("miniboss-sting"), data.bossSting());
        assertEquals(Optional.of("duel-theme"), data.bossTrack());
        assertEquals(Optional.of("full"), data.trackAfterBoss());
        assertEquals(Optional.of("duel-theme"), Tracks.name(32), "track 32, Three Aces");
        assertEquals(Sfx.AMBIENCE_BELT_OPEN, Sfx.ambience(data.ambience()));
        assertEquals(Sfx.AMBIENCE_FLEET_COMMS, Sfx.ambience("fleet-comms"));
        for (String file : new String[] {Tracks.path(32), Tracks.path(21), Tracks.path(14)}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.ACE_FLYBY, "ace-flyby-r78-a.ogg"),
                Map.entry(Sfx.KESTREL_FLYBY, "kestrel-flyby-r78-a.ogg"),
                Map.entry(Sfx.SNIPER_CHARGE, "sniper-charge-r78-a.ogg"),
                Map.entry(Sfx.SNIPER_SHOT, "sniper-shot-r78-a.ogg"),
                Map.entry(Sfx.KESTREL_DIVE, "kestrel-dive-r78-a.ogg"),
                Map.entry(Sfx.KESTREL_PULLOUT, "kestrel-pullout-r78-a.ogg"),
                Map.entry(Sfx.VESPER_CANNON, "vesper-cannon-r78-a.ogg"),
                Map.entry(Sfx.ACE_DEATH, "ace-death-r78-a.ogg"),
                Map.entry(Sfx.ACE_LAST_CHAIN, "ace-last-chain-r78-a.ogg"),
                Map.entry(Sfx.RECORDER_CRACK, "recorder-crack-r78-a.ogg"),
                Map.entry(Sfx.AMBIENCE_FLEET_COMMS, "fleet-comms-r78-a.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertEquals(Sfx.RECORDER_CRACK, AllyCues.towBreak("debris-sentry-recorder"));
        bank.dispose();
    }

    /**
     * The mid-boss's own track: at the arrival the boss music starts with the sting in the warning's place (no klaxon,
     * no banner: {@link LevelMusic#midBossTrack}); at the kill it fades and the level track comes back with its full
     * mix over the timed "none" before it, fading in over {@value LevelMusic#MID_BOSS_RETURN_SECONDS} s; a retry from
     * the boss holds it again.
     */
    @Test
    void theDuelThemeReplacesTheLevelTrackAndTheFullMixComesBack() {
        LevelData.Music data = CONTENT.level(KEY).music();
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("eye-of-the-storm")),
                        files.internal(Tracks.path("eye-of-the-storm")),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty(),
                        Optional.of(new LevelMusic.Boss(
                                Optional.of(files.internal(Tracks.path(21))),
                                Optional.of(files.internal(Tracks.path(32))),
                                Tracks.MINIBOSS_STING_SECONDS)))
                .stemTimes(data.stemTimeList(), stem -> files.internal(Tracks.stemPath("eye-of-the-storm", stem)))
                .midBoss(data.trackAfterBoss().orElseThrow());
        assertTrue(music.midBossTrack());
        music.update(3, 60, 0.1f, false);
        assertTrue(music.stemsOn(), "the theme before the duel");
        music.update(5, 101, 0.1f, false);
        assertFalse(music.stemsOn(), "the stems out from t=96");
        assertTrue(music.bossArrived(), "the sting and the duel theme");
        assertEquals(LevelMusic.BossState.FIGHT, music.bossState());
        music.update(6, 102.5, 0.1f, false);
        music.bossDown();
        assertTrue(music.midBossBack());
        music.update(6, 103, 0.1f, false);
        assertTrue(music.stemsOn(), "the level track back after the last ace");
        assertEquals(0.05f, music.themeIn(), 0.01f, "fading in");
        for (int i = 0; i < 25; i++) {
            music.update(7, 103 + 0.1 * i, 0.1f, false);
        }
        assertEquals(1, music.themeIn(), 1e-6f, "back in full");
        assertEquals(1, music.timedGain(), 1e-6f);
        music.bossRetry();
        assertFalse(music.midBossBack(), "a retry holds the theme for the duel again");
        music.dispose();
        sfx.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable under the nearest ace with the Ion Beam and two Autocannon Pods
     * (the recorder shot open as it drifts down the left edge): the duel's sounds and the recorder's crack.
     */
    @Test
    void theAcesTheirShotsAndTheRecorderSoundTheirOwn() {
        Sortie sortie = sortie();
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                sortie.script().setPieces().stream()
                        .map(vanguard.sim.SetPieceSpec::slug)
                        .toList());
        sounds.towLook("debris-sentry-recorder");
        Set<Sfx> heard = EnumSet.noneOf(Sfx.class);
        Set<Sfx> wanted = EnumSet.of(
                Sfx.ACE_FLYBY,
                Sfx.KESTREL_FLYBY,
                Sfx.SNIPER_CHARGE,
                Sfx.SNIPER_SHOT,
                Sfx.KESTREL_DIVE,
                Sfx.KESTREL_PULLOUT,
                Sfx.VESPER_CANNON,
                Sfx.ACE_DEATH,
                Sfx.ACE_LAST_CHAIN,
                Sfx.RECORDER_CRACK);
        int last = SimStep.ticks(320);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (Sfx sfx : wanted) {
                if (bank.playing(sfx) > 0 || bank.looping(sfx)) {
                    heard.add(sfx);
                }
            }
            if (heard.containsAll(wanted)) {
                break;
            }
        }
        sounds.stopLoops();
        Set<Sfx> missing = EnumSet.copyOf(wanted);
        missing.removeAll(heard);
        assertTrue(missing.isEmpty(), "not heard: " + missing);
        assertFalse(bank.looping(Sfx.VESPER_CANNON), "the cannon loop stopped with the flight");
        bank.dispose();
    }

    /** Medium, the ship invulnerable with the Ion Beam and two Autocannon Pods. */
    private static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "autocannon-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "autocannon-pod", 3)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** Under the recorder while it holds on the screen, else under the nearest ace on the field, else the middle; low. */
    private static int steer(Sortie sortie) {
        double target = 240;
        double y = 70;
        boolean recorder = false;
        for (int i = 0; i < sortie.towCount(); i++) {
            Tow tow = sortie.tow(i);
            if (tow.present() && tow.holding() && tow.renderY(1) < 600 && tow.renderY(1) > 150) {
                target = tow.renderX(1);
                recorder = true;
            }
        }
        for (int k = 0; k < sortie.setPieceCount() && !recorder; k++) {
            SetPiece piece = sortie.setPiece(k);
            Squadron squadron = piece.squadron().orElse(null);
            if (squadron == null || !piece.present()) {
                continue;
            }
            double best = Double.POSITIVE_INFINITY;
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (squadron.touchable(a) && squadron.y(a) > sortie.ship().y() + 40) {
                    double d = Math.abs(squadron.x(a) - sortie.ship().x());
                    if (d < best) {
                        best = d;
                        target = squadron.x(a);
                    }
                }
            }
        }
        int commands = 0;
        if (sortie.ship().x() < target - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > target + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < y - 3) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > y + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
