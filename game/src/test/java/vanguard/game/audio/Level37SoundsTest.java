package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import com.badlogic.gdx.files.FileHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.screen.LevelScreen;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.Regrowing;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part C (step C4b), Level 37's sounds (design/campaign Level 37, Music &amp; ambience; design/audio/sfx, Act 6
 * sounds; step AUD-C's round 75 proposals, the user's picks since step C6b: S.1 b, S.5 b, S.8 b, the rest a): track 14 with Vorne's motif to the turn,
 * the theme out over 1 s at t=100, track 15 "Storm Wall" from t=103 without its motif, its {@code fall} stem from t=160;
 * the {@code aurelia} bed with the burning layer from the turn; flown, the PA chime as each shuttle pair is called, the
 * growth's burst at the turn, the deck's groan and the balloons tearing as the city falls, the Mirror Interceptor's hum
 * (the nearest) and pod bursts, the Graft Turret's own shot pair, its shell's crack, its root's heartbeat and
 * crumble.
 */
class Level37SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(37).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemesTheBedsAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(14, data.track(), "Act 6 A, Eye of the Storm");
        assertEquals("storm-wall", Tracks.name(15).orElseThrow(), "Act 6 B, Storm Wall");
        for (String file : new String[] {
            Tracks.path(15),
            Tracks.basePath("storm-wall"),
            Tracks.motifPath("storm-wall"),
            Tracks.stemPath("storm-wall", "fall")
        }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(15, data.trackAt(103), "track 15 from the turn's window");
        assertEquals(14, data.trackAt(102.9));
        assertTrue(data.motifOn(1, 99), "Vorne's motif up to the turn");
        assertFalse(data.motifOn(1, 100.5), "cut at t=100");
        assertEquals(Sfx.AMBIENCE_AURELIA, Sfx.ambience(data.ambience()));
        LevelData.Music.AmbienceLayer burning =
                data.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AURELIA_BURNING, Sfx.ambience(burning.ambience()));
        assertEquals(0, burning.level(99), 1e-9, "no fire before the turn");
        assertTrue(burning.level(102) > 0 && burning.level(102) < 1, "rising over 4 s from the turn");
        assertEquals(1, burning.level(150), 1e-9, "the city burning");
        List<LevelMusic.Theme> themes = LevelScreen.themes(data, new Lwjgl3Files());
        assertEquals(1, themes.size());
        assertEquals(103, themes.getFirst().t(), 1e-9);
        assertTrue(themes.getFirst().fullMix(), "its full mix");
        assertEquals(1, themes.getFirst().fade(), 1e-9);
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.AMBIENCE_AURELIA, "aurelia-r72-a.ogg"),
                Map.entry(Sfx.AURELIA_BURNING, "aurelia-burning-r75-b.ogg"),
                Map.entry(Sfx.PA_CHIME, "pa-chime-r75-b.ogg"),
                Map.entry(Sfx.GRAFT_SHOT, "graft-shot-r75-b.ogg"),
                Map.entry(Sfx.GROWTH_BURST, "growth-burst-r75-a.ogg"),
                Map.entry(Sfx.BALLOON_TEAR, "balloon-tear-r75-a.ogg"),
                Map.entry(Sfx.DECK_GROAN, "deck-groan-r75-a.ogg"),
                Map.entry(Sfx.MIRROR_HUM, "mirror-hum-r75-a.ogg"),
                Map.entry(Sfx.MIRROR_PODS, "mirror-pods-r75-a.ogg"),
                Map.entry(Sfx.GRAFT_SHELL_CRACK, "graft-shell-crack-r75-a.ogg"),
                Map.entry(Sfx.GRAFT_HEARTBEAT, "graft-heartbeat-r75-a.ogg"),
                Map.entry(Sfx.GRAFT_ROOT_CRUMBLE, "graft-root-crumble-r75-a.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /**
     * The theme change: track 14 with its motif to t=100, its stems out from t=100 (the {@code none} stem time), track
     * 15 from t=103 on its full mix fading in over 1 s with no motif, its {@code fall} stem alone from t=160; a retry
     * starts over on track 14.
     */
    @Test
    void theThemeChangesAtTheTurnAndFallsAtTheFall() {
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelData.Music data = CONTENT.level(KEY).music();
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        asset(files, Tracks.basePath("eye-of-the-storm")),
                        asset(files, Tracks.path(14)),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        1,
                        0,
                        section -> true,
                        Double.POSITIVE_INFINITY,
                        Optional.empty())
                .motif(asset(files, Tracks.motifPath("eye-of-the-storm")), data::motifOn)
                .stemTimes(data.stemTimeList(), stem -> asset(files, Tracks.stemPath("eye-of-the-storm", stem)))
                .themes(List.of(new LevelMusic.Theme(
                        103,
                        asset(files, Tracks.basePath("storm-wall")),
                        asset(files, Tracks.path(15)),
                        stem -> asset(files, Tracks.stemPath("storm-wall", stem)),
                        true,
                        1)));
        music.update(1, 50, 0.1f, false);
        assertEquals(-1, music.theme(), "track 14");
        assertTrue(music.motifOn(), "Vorne's motif over the rim and the decks");
        assertTrue(music.timedStemsOn());
        music.update(1, 100.05, 0.1f, false);
        assertFalse(music.timedStemsOn(), "the old theme out from t=100");
        assertFalse(music.motifOn(), "the motif cut at the turn");
        music.update(1, 103.05, 0.1f, false);
        assertEquals(0, music.theme(), "track 15 from t=103");
        assertTrue(music.timedStemsOn(), "its full mix");
        assertEquals(-1, music.timedExtra());
        assertEquals(0, music.themeIn(), 1e-6, "fading in from silence");
        double t = 103.15;
        while (t < 104.5) {
            music.update(1, t, 0.1f, false);
            t += 0.1;
        }
        assertEquals(1, music.themeIn(), 1e-6, "in over 1 s");
        assertFalse(music.motifOn());
        music.update(1, 160.05, 0.1f, false);
        assertEquals(0, music.timedExtra(), "track 15's fall stem from t=160");
        assertFalse(music.timedStemsOn(), "alone");
        music.restart();
        music.update(1, 5, 0.1f, false);
        assertEquals(-1, music.theme(), "a retry starts over on track 14");
        assertTrue(music.timedStemsOn());
        assertTrue(music.motifOn());
        music.dispose();
        sfx.dispose();
        bank.dispose();
    }

    private static FileHandle asset(Lwjgl3Files files, String path) {
        return files.absolute(ASSETS.resolve(path).toAbsolutePath().toString());
    }

    /**
     * Flown on medium with the ship invulnerable low on the field, the Ion Beam firing at the nearest Graft Turret or
     * root on the screen: the PA chime for each shuttle pair (three), the growth's burst once, the deck's groan once and
     * the balloons tearing as the city falls, the mirrors' hum and pod bursts, the Graft Turrets' own shot pair (not the
     * Talon's crack), their shells' crack, a root's heartbeat and crumble; the hum stopped with the flight.
     */
    @Test
    void aureliaSoundsItsOwn() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        var backdrop = CONTENT.level(KEY).backdrop();
        sounds.aurelia(
                backdrop.growth().orElseThrow().t(),
                backdrop.fall().orElseThrow().t(),
                backdrop.fall().orElseThrow().balloons().orElseThrow().stream()
                        .map(balloon -> balloon.x())
                        .toList());
        int chimes = 0;
        int bursts = 0;
        int groans = 0;
        int tears = 0;
        boolean hum = false;
        boolean pods = false;
        boolean graftShot = false;
        boolean shell = false;
        boolean beat = false;
        boolean crumble = false;
        int last = SimStep.ticks(175);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            int chimesBefore = bank.playing(Sfx.PA_CHIME);
            int burstsBefore = bank.playing(Sfx.GROWTH_BURST);
            int groansBefore = bank.playing(Sfx.DECK_GROAN);
            int tearsBefore = bank.playing(Sfx.BALLOON_TEAR);
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            chimes += Math.max(0, bank.playing(Sfx.PA_CHIME) - chimesBefore);
            bursts += Math.max(0, bank.playing(Sfx.GROWTH_BURST) - burstsBefore);
            groans += Math.max(0, bank.playing(Sfx.DECK_GROAN) - groansBefore);
            tears += Math.max(0, bank.playing(Sfx.BALLOON_TEAR) - tearsBefore);
            hum |= bank.looping(Sfx.MIRROR_HUM);
            pods |= bank.playing(Sfx.MIRROR_PODS) > 0;
            graftShot |= bank.playing(Sfx.GRAFT_SHOT) > 0;
            shell |= bank.playing(Sfx.GRAFT_SHELL_CRACK) > 0;
            beat |= bank.playing(Sfx.GRAFT_HEARTBEAT) > 0;
            crumble |= bank.playing(Sfx.GRAFT_ROOT_CRUMBLE) > 0;
            if (sortie.levelSeconds() < 99.9) {
                assertEquals(0, bank.playing(Sfx.GROWTH_BURST), "no burst before the turn");
            }
            if (sortie.levelSeconds() < 159.9) {
                assertEquals(0, bank.playing(Sfx.DECK_GROAN), "no groan before the fall");
                assertEquals(0, bank.playing(Sfx.BALLOON_TEAR), "no tear before the fall");
            }
        }
        sounds.stopLoops();
        assertEquals(3, chimes, "a chime for each shuttle pair");
        assertEquals(1, bursts, "the growth's burst once");
        assertEquals(1, groans, "the deck's groan once");
        assertEquals(2, tears, "each balloon tears");
        assertTrue(hum, "the nearest mirror's hum");
        assertTrue(pods, "a mirror's pod burst");
        assertTrue(graftShot, "the Graft Turrets' own shot pair");
        assertTrue(shell, "a Graft Turret's shell cracking");
        assertTrue(beat, "an exposed root's heartbeat");
        assertTrue(crumble, "a root's crumble");
        assertFalse(bank.looping(Sfx.MIRROR_HUM), "the hum stops with the flight");
        bank.dispose();
    }

    /** The pulses: one at the window's start, rising from 1 to 4 a second: 12.5 over a 5 s window. */
    @Test
    void theHeartbeatQuickensAsTheWindowCloses() {
        assertEquals(0, AureliaCues.pulses(0, 5), 1e-9);
        assertEquals(12.5, AureliaCues.pulses(1, 5), 1e-9);
        double early = AureliaCues.pulses(0.02, 5) - AureliaCues.pulses(0, 5);
        double late = AureliaCues.pulses(1.0, 5) - AureliaCues.pulses(0.98, 5);
        assertTrue(late > 3 * early, "four a second at the close, one at the start");
    }

    /** Low on the field, under the nearest Graft Turret or exposed root on the screen, else in the middle. */
    private static int steer(Sortie sortie) {
        double target = 240;
        double best = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            var enemy = sortie.enemy(e);
            boolean graft =
                    enemy.spec().regrowth().isPresent() && enemy.regrowth().phase() != Regrowing.Phase.KNIT;
            if (graft && enemy.renderY(1) < 600 && enemy.renderY(1) > 100) {
                double d = Math.abs(enemy.renderX(1) - sortie.ship().x());
                if (d < best) {
                    best = d;
                    target = enemy.renderX(1);
                }
            }
        }
        int commands = 0;
        if (sortie.ship().x() < target - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > target + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < 77) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > 83) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
