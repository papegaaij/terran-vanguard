package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
import vanguard.sim.Enemy;
import vanguard.sim.GroundObject;
import vanguard.sim.LevelScript;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part F (step F4), Level 48's sounds (design/campaign Level 48, Music &amp; ambience; design/enemies/bosses/
 * vornes-chimera, Sounds; design/audio/sfx, Act 7 sounds; round 84's picks): track 16 and track 34 "Vorne's Last
 * Stand", the {@code vrell-hive} bed; the Chimera's rise as its arrival's sound, the fusion's pain and the capsule's
 * opening at its phases, the rear sweep's two passes, the burst as its own death, its lance on the Ascendant's files at
 * ×1.25; flown, a reactor core's death, Vorne's archive wrenched open, the rise at the sting.
 */
class Level48SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(48).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemesTheBedAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(16, data.track(), "Act 7 A, Far Shore");
        assertEquals("vorne-last-stand", data.bossTrack().orElseThrow());
        for (String file : new String[] {Tracks.path(16), Tracks.path(Tracks.VORNE_LAST_STAND)}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(Sfx.AMBIENCE_VRELL_HIVE, Sfx.ambience(data.ambience()));
        Map<Sfx, String> files = Map.of(
                Sfx.CHIMERA_RISE, "chimera-rise-r84-a.ogg",
                Sfx.CHIMERA_PAIN, "chimera-pain-r84-b.ogg",
                Sfx.CHIMERA_SWEEP, "chimera-sweep-r84-b.ogg",
                Sfx.CHIMERA_CAPSULE, "chimera-capsule-r84-b.ogg",
                Sfx.CHIMERA_BURST, "chimera-burst-r84-a.ogg",
                Sfx.REACTOR_CORE_DEATH, "reactor-core-death-r84-a.ogg",
                Sfx.POD_ARCHIVE, "pod-archive-r84-b.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    @Test
    void theChimerasMomentsHaveTheirSounds() {
        assertEquals(Sfx.CHIMERA_RISE, BossCues.arrival(ChimeraCues.SLUG), "the well splitting, the rise");
        assertEquals(Sfx.CHIMERA_BURST, BossCues.death(ChimeraCues.SLUG), "the burst as its own death");
        assertEquals(Sfx.CHIMERA_PAIN, ChimeraCues.phaseSound(1), "the fusion's pain");
        assertEquals(Sfx.CHIMERA_CAPSULE, ChimeraCues.phaseSound(2), "the capsule opening");
        assertNull(ChimeraCues.phaseSound(0));
        assertEquals(Sfx.CHIMERA_SWEEP, ChimeraCues.sound(SimEvents.Type.SWEEP_OUT), "the drop's pass");
        assertEquals(Sfx.CHIMERA_SWEEP, ChimeraCues.sound(SimEvents.Type.SWEEP_CLIMB), "the climb's pass");
        for (SimEvents.Type silent : List.of(
                SimEvents.Type.SWEEP_TELL,
                SimEvents.Type.SWEEP_MARK,
                SimEvents.Type.SWEEP_DONE,
                SimEvents.Type.LAST_WORDS,
                SimEvents.Type.BULLETS_FADE,
                SimEvents.Type.LAST_BURST)) {
            assertNull(ChimeraCues.sound(silent), silent.name());
        }
        assertEquals(1.25f, ChimeraCues.LANCE_PITCH, 1e-6, "the Ascendant's lance, higher and thinner");
        // The charge at ×1.25 lasts 1.6 s: from the 0.8 s tell and the 1.2 s line it starts 0.4 s in.
        assertEquals(SimStep.ticks(0.4), AscendantCues.lanceChargeDelay(0.8, 1.2, ChimeraCues.LANCE_PITCH));
        assertEquals(AscendantCues.lanceChargeDelay(0.8, 1.2), AscendantCues.lanceChargeDelay(0.8, 1.2, 1));
    }

    @Test
    void aCoreDiesTheArchiveOpensAndTheChimeraRises() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                script.setPieces().stream().map(FlightSounds::setPieceSound).toList());
        Set<Sfx> heard = EnumSet.noneOf(Sfx.class);
        Set<Sfx> wanted = EnumSet.of(Sfx.REACTOR_CORE_DEATH, Sfx.POD_ARCHIVE, Sfx.CHIMERA_RISE);
        int archives = 0;
        boolean archiveWas = false;
        boolean secret = false;
        int last = SimStep.ticks(192);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie));
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            for (int e = 0; e < sortie.events().size(); e++) {
                secret |= sortie.events().type(e) == SimEvents.Type.SECRET_FOUND;
            }
            for (Sfx sfx : wanted) {
                if (bank.playing(sfx) > 0) {
                    heard.add(sfx);
                }
            }
            boolean archive = bank.playing(Sfx.POD_ARCHIVE) > 0;
            if (archive && !archiveWas) {
                archives++;
            }
            archiveWas = archive;
            if (sortie.levelSeconds() < 186) {
                assertEquals(0, bank.playing(Sfx.CHIMERA_RISE), "no rise before the sting");
            }
        }
        sounds.stopLoops();
        assertTrue(secret, "the archive was broken");
        assertEquals(wanted, heard);
        assertEquals(1, archives, "the archive opens once");
        bank.dispose();
    }

    /**
     * Guns held but under a reactor core on the screen (the lowest) or, from t=135, under Vorne's archive, firing;
     * else low in the middle.
     */
    private static int steer(Sortie sortie) {
        double tx = 240;
        boolean fire = false;
        double lowest = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            double y = enemy.renderY(1);
            if (enemy.spec().slug().equals(ChimeraCues.CORE) && y > 120 && y < 560 && y < lowest) {
                lowest = y;
                tx = enemy.renderX(1);
                fire = true;
            }
        }
        if (sortie.levelSeconds() >= 135) {
            for (int g = 0; g < sortie.groundObjectCount(); g++) {
                GroundObject object = sortie.groundObject(g);
                double y = object.renderY(1);
                if (object.spec().look().equals(ChimeraCues.ARCHIVE) && !object.spent() && y > 100 && y < 600) {
                    tx = object.renderX();
                    fire = true;
                }
            }
        }
        int commands = fire ? Command.FIRE.bit() : 0;
        double x = sortie.ship().x();
        if (x < tx - 3) {
            commands |= Command.RIGHT.bit();
        } else if (x > tx + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() > 80) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
