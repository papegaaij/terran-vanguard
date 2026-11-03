package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import vanguard.game.screen.LevelScreen;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.GameTestEvents;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tow;

/**
 * M10 part H (step H4b), Level 42's sounds (design/campaign Level 42, Music &amp; ambience; design/enemies/bosses/
 * ascendant, Sounds brief; round 80's a variants until the picks): track 15 with its motif to the halt, the boss warning
 * (22) and track 19 "Iron Sovereign" through phase 1, track 18 "The Choir Descends" by a 2 s crossfade at the eruption,
 * the escape sting (track 33) cutting it at {@code ESCAPED}, the {@code belt-open} bed alone after it; flown, the
 * tender's strongbox, the Ascendant's arrival, the spinal lance's charge and shot and a panel's bounce; played by hand,
 * the eruption, a generator's death, a panel's mirror breaking, the hull's break with the {@code huge} rung at the flash
 * (the level's only one) and the lifeboat.
 */
class Level42SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(42).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theMusicTheBedAndTheAscendantsSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(15, data.track(), "Act 6 B, Storm Wall");
        assertTrue(data.motifOn(5, 80) && !data.motifOn(6, 92), "Vorne's motif out at the halt");
        assertEquals(Optional.of("boss-warning"), data.bossWarning());
        assertEquals(Optional.of("iron-sovereign"), data.bossTrack());
        assertEquals(Optional.of("escape-sting"), data.bossEndSting());
        assertEquals(Optional.of("escape-sting"), Tracks.name(Tracks.ESCAPE_STING), "track 33");
        assertEquals(Sfx.ESCAPE_STING, LevelScreen.endSting(data));
        assertEquals("music/escape-sting.ogg", Sfx.ESCAPE_STING.path());
        List<LevelMusic.BossChange> changes = LevelScreen.bossChanges(data, new Lwjgl3Files());
        assertEquals(1, changes.size());
        assertEquals("eruption", changes.getFirst().on());
        assertEquals(Tracks.path(Tracks.BOSS_VRELL), changes.getFirst().track().path(), "track 18");
        assertEquals(2.0, changes.getFirst().fade(), 1e-9);
        assertNull(
                LevelScreen.endSting(
                        CONTENT.level(CONTENT.levelKey(35).orElseThrow()).music()),
                "Level 35's kill");
        assertEquals(Sfx.AMBIENCE_BELT_OPEN, Sfx.ambience(data.ambience()));
        for (String file : new String[] {
            Tracks.path(15), Tracks.path(19), Tracks.path(18), Tracks.path(22), Tracks.path(33), Tracks.path(24)
        }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.ASCENDANT_ARRIVAL, "ascendant-arrival-r80-b.ogg"),
                Map.entry(Sfx.ASCENDANT_LANCE_CHARGE, "ascendant-lance-charge-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_LANCE_SHOT, "ascendant-lance-shot-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_PANEL_BOUNCE, "ascendant-panel-bounce-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_GENERATOR_DOWN, "ascendant-generator-down-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_ERUPTION, "ascendant-eruption-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_SAC_HEAL, "ascendant-sac-heal-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_BROOD, "ascendant-brood-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_HULL_BREAK, "ascendant-hull-break-r80-a.ogg"),
                Map.entry(Sfx.ASCENDANT_LIFEBOAT, "ascendant-lifeboat-r80-a.ogg"),
                Map.entry(Sfx.TENDER_STRONGBOX, "tender-strongbox-r80-a.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertEquals(Sfx.TENDER_STRONGBOX, AllyCues.towBreak("debris-home-tender"), "not the cable snap");
        assertEquals(Sfx.ASCENDANT_BROOD, AscendantCues.playSound(PhasePlaybook.Kind.BROOD));
        assertNull(AscendantCues.playSound(PhasePlaybook.Kind.VOLLEY), "the borrowed attacks keep their own sounds");
        // The lance charge's 2 s end at the shot: medium's 0.8 s glow and 1.2 s red line, easy's 1.5 s line later.
        assertEquals(0, AscendantCues.lanceChargeDelay(0.8, 1.2));
        assertEquals(SimStep.ticks(0.3), AscendantCues.lanceChargeDelay(0.8, 1.5));
        assertEquals(0, AscendantCues.lanceChargeDelay(0.8, 1.0), "hard's: cut by the shot");
        assertEquals(
                "eruption",
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM).setPieces().stream()
                        .flatMap(piece -> piece.boss().stream())
                        .findFirst()
                        .orElseThrow()
                        .phases()
                        .get(BossCues.ERUPTION_PHASE)
                        .name(),
                "the eruption is the Ascendant's second phase");
        bank.dispose();
    }

    /**
     * The boss music: the warning and track 19 at the arrival; at the eruption (not at another subject) track 18 comes in
     * over 2 s as track 19 goes; at {@code ESCAPED} the escape sting cuts it and plays once, the theme stays out; a
     * restart stops the sting and starts the fight's music over.
     */
    @Test
    void theBossTrackChangesAtTheEruptionAndTheStingCutsItAtTheEscape() {
        LevelData.Music data = CONTENT.level(KEY).music();
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer, () -> now[0]);
        LevelMusic music = new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("storm-wall")),
                        files.internal(Tracks.path("storm-wall")),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty(),
                        Optional.of(new LevelMusic.Boss(
                                Optional.of(files.internal(Tracks.path(22))),
                                Optional.of(files.internal(Tracks.path(19))),
                                Tracks.BOSS_WARNING_BARS_SECONDS)))
                .bossChanges(LevelScreen.bossChanges(data, files), LevelScreen.endSting(data));
        music.update(3, 60, 0.1f, false);
        music.bossPhase("eruption");
        assertEquals(-1, music.bossChange(), "no change before the boss music");
        assertTrue(music.bossArrived());
        assertEquals(LevelMusic.BossState.FIGHT, music.bossState());
        music.bossPhase("first-generator");
        assertEquals(-1, music.bossChange(), "track 19 through phase 1");
        music.bossPhase("eruption");
        assertEquals(0, music.bossChange(), "track 18 at the eruption");
        assertEquals(0, music.bossChangeIn(), 1e-6f);
        for (int i = 0; i < 10; i++) {
            music.update(6, 93, 0.1f, true);
        }
        assertEquals(0.5f, music.bossChangeIn(), 1e-4f, "halfway through the 2 s crossfade");
        for (int i = 0; i < 11; i++) {
            music.update(6, 93, 0.1f, true);
        }
        assertEquals(1, music.bossChangeIn(), 1e-6f, "track 18 alone");
        music.bossPhase("eruption");
        assertEquals(0, music.bossChange(), "once a fight");
        assertFalse(music.endStung());
        music.bossEscaped();
        assertEquals(LevelMusic.BossState.DOWN, music.bossState(), "the fight's music over");
        assertTrue(music.endStung(), "the escape sting");
        now[0] += NANOS_PER_STEP;
        assertTrue(sfx.playing(Sfx.ESCAPE_STING) > 0, "track 33 plays");
        music.update(7, 95, 0.1f, true);
        assertFalse(music.midBossBack(), "the theme stays out: the bed alone");
        assertEquals(LevelMusic.BossState.DOWN, music.bossState());
        music.restart();
        assertFalse(music.endStung(), "a restart stops the sting");
        assertEquals(-1, music.bossChange(), "and the fight's music starts over");
        assertEquals(LevelMusic.BossState.WAITING, music.bossState());
        music.dispose();
        sfx.dispose();
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable, the Ion Beam and two Autocannon Pods: under the tender's strongbox as
     * it drifts down the right half, then under a living battery: the strongbox's crack, the Ascendant's arrival, the
     * spinal lance's charge and shot, a panel's bounce. Then the fight's rarer moments by hand while the hull stands:
     * the eruption, a generator's death over its blast, a panel's mirror breaking, the hull's break with the {@code huge}
     * rung at its flash, the lifeboat.
     */
    @Test
    void theAscendantSoundsItsOwn() {
        Sortie sortie = sortie();
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                sortie.script().setPieces().stream()
                        .map(vanguard.sim.SetPieceSpec::slug)
                        .toList());
        sounds.towLook("debris-home-tender");
        Set<Sfx> heard = EnumSet.noneOf(Sfx.class);
        Set<Sfx> wanted = EnumSet.of(
                Sfx.TENDER_STRONGBOX,
                Sfx.ASCENDANT_ARRIVAL,
                Sfx.ASCENDANT_LANCE_CHARGE,
                Sfx.ASCENDANT_LANCE_SHOT,
                Sfx.ASCENDANT_PANEL_BOUNCE);
        int last = SimStep.ticks(170);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (Sfx sfx : wanted) {
                if (bank.playing(sfx) > 0) {
                    heard.add(sfx);
                }
            }
            if (heard.containsAll(wanted)) {
                break;
            }
        }
        Set<Sfx> missing = EnumSet.copyOf(wanted);
        missing.removeAll(heard);
        assertTrue(missing.isEmpty(), "not heard: " + missing);
        assertFalse(bank.playing(Sfx.CABLE_SNAP) > 0, "the strongbox, not the cable snap");

        SetPiece piece = AscendantCues.ascendant(sortie);
        assertNotNull(piece, "the Ascendant fights");
        int generator = -1;
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.spec().parts().get(p).name().equals("gen-l")) {
                generator = p;
            }
        }
        double x = piece.renderX(1);
        double y = piece.renderY(1);
        sounds.play(new GameTestEvents()
                .add(SimEvents.Type.BOSS_PHASE, x, y, BossCues.ERUPTION_PHASE)
                .add(SimEvents.Type.PART_DESTROYED, piece.partX(generator), piece.partY(generator), generator)
                .add(SimEvents.Type.PART_MIRROR_BROKEN, x, y, 0)
                .events());
        assertTrue(bank.playing(Sfx.ASCENDANT_ERUPTION) > 0, "the eruption");
        assertTrue(bank.playing(Sfx.ASCENDANT_GENERATOR_DOWN) > 0, "the generator failing");
        assertTrue(bank.playing(Sfx.MIRROR_BREAK) > 0, "a panel's mirror breaking");
        assertFalse(bank.playing(Sfx.EXPLOSION_HUGE_A) > 0, "no huge rung before the flash");
        sounds.play(
                new GameTestEvents().add(SimEvents.Type.HULL_BROKEN, x, y, 0).events());
        assertTrue(bank.playing(Sfx.ASCENDANT_HULL_BREAK) > 0, "the hull breaking from K");
        assertFalse(bank.playing(Sfx.EXPLOSION_HUGE_A) > 0, "the huge rung waits for the flash");
        sounds.play(new GameTestEvents()
                .add(SimEvents.Type.HULL_FLASH, x, y, 0)
                .add(SimEvents.Type.LIFEBOAT_AWAY, x, y, 0)
                .events());
        assertTrue(bank.playing(Sfx.EXPLOSION_HUGE_A) > 0, "the huge rung at the flash");
        assertTrue(bank.playing(Sfx.ASCENDANT_LIFEBOAT) > 0, "the lifeboat tearing free");
        sounds.stopLoops();
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

    /** Under the tender's strongbox while it holds; then under the Ascendant's first living battery; else the middle. */
    private static int steer(Sortie sortie) {
        double targetX = 240;
        double targetY = 60;
        boolean found = false;
        for (int i = 0; i < sortie.towCount() && !found; i++) {
            Tow tow = sortie.tow(i);
            if (tow.present() && tow.holding() && tow.podY() < 600) {
                targetX = tow.podX();
                found = true;
            }
        }
        SetPiece piece = AscendantCues.ascendant(sortie);
        if (!found && piece != null && piece.present()) {
            for (int p = 0; p < piece.partCount() && !found; p++) {
                if (piece.spec().parts().get(p).name().startsWith("bat-") && !piece.partWrecked(p)) {
                    targetX = piece.partX(p);
                    found = true;
                }
            }
        }
        int commands = 0;
        if (sortie.ship().x() < targetX - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > targetX + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < targetY - 3) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > targetY + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
