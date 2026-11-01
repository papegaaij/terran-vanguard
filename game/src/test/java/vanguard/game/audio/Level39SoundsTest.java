package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part E (step E4), Level 39's sounds (design/campaign Level 39, Music &amp; ambience; design/audio/sfx, Act 6
 * sounds): track 14 "Eye of the Storm", its strings over section 1, the base from section 2 and the full mix from
 * section 4, Vorne's motif under his line (t=91 to 99.5); the {@code io-refinery} bed; flown, the eruption at a tell's
 * start, a rock's crack when it is shot (round 77 a), a cracking tower's collapse and a pump station's blast over their
 * rungs (round 77 a).
 */
class Level39SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(39).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheMotifTheBedAndTheRefinerysSoundsAreThere() {
        LevelData level = CONTENT.level(KEY);
        LevelData.Music data = level.music();
        assertEquals(14, data.track(), "Act 6 A, Eye of the Storm");
        assertEquals(2, data.startSection(), "its strings over the Tvashtar plains, the base from the Loki works");
        assertEquals(4, data.fullSection(), "the full mix in the Pele works and the plume field");
        assertFalse(data.full(3));
        assertTrue(data.full(4) && data.full(6));
        assertFalse(data.motifOn(3, 90), "not before Vorne's line");
        assertTrue(data.motifOn(3, 91.5), "under Vorne's line");
        assertFalse(data.motifOn(3, 100), "cut at its end");
        assertEquals(Sfx.AMBIENCE_IO_REFINERY, Sfx.ambience(data.ambience()));
        for (String file :
                new String[] {Tracks.path(14), Tracks.basePath("eye-of-the-storm"), Tracks.motifPath("eye-of-the-storm")
                }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_IO_REFINERY, "io-refinery-r72-a.ogg",
                Sfx.ROCK_CRACK, "rock-crack-r77-a.ogg",
                Sfx.ROCK_THUD, "rock-thud-r77-a.ogg",
                Sfx.TOWER_COLLAPSE, "tower-collapse-r77-a.ogg",
                Sfx.PUMP_BLAST, "pump-blast-r77-a.ogg",
                Sfx.ERUPTION, "eruption-r72-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        assertEquals(Sfx.TOWER_COLLAPSE, UnitCues.refineryDeath("cracking-tower"));
        assertEquals(Sfx.PUMP_BLAST, UnitCues.refineryDeath("pump-station"));
        assertNull(UnitCues.refineryDeath("strider"), "another kind keeps its rung alone");
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable low under the lowest tower or pump station, the Ion Beam and the pods firing: an
     * eruption's tell, a rock's crack, a tower's collapse and a pump station's blast sound.
     */
    @Test
    void theEruptionsTheTowersAndThePumpsSoundTheirOwn() {
        Sortie sortie = sortie();
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean eruption = false;
        boolean crack = false;
        boolean collapse = false;
        boolean blast = false;
        boolean towerDied = false;
        boolean pumpDied = false;
        int last = SimStep.ticks(120);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED) {
                    String slug = sortie.enemyKinds().get(events.value(i)).slug();
                    towerDied |= slug.equals("cracking-tower");
                    pumpDied |= slug.equals("pump-station");
                }
            }
            eruption |= bank.playing(Sfx.ERUPTION) > 0;
            crack |= bank.playing(Sfx.ROCK_CRACK) > 0;
            collapse |= bank.playing(Sfx.TOWER_COLLAPSE) > 0;
            blast |= bank.playing(Sfx.PUMP_BLAST) > 0;
        }
        sounds.stopLoops();
        assertTrue(eruption, "a vent's eruption");
        assertTrue(towerDied && collapse, "a tower's collapse");
        assertTrue(pumpDied && blast, "a pump station's blast");
        assertTrue(crack, "a rock shot to pieces");
        bank.dispose();
    }

    /** Medium, the ship invulnerable with the Ion Beam and two Autocannon Pods (a beam does not hit the rocks). */
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

    /** Low on the field under the lowest tower or pump station on the screen, else the middle. */
    private static int steer(Sortie sortie) {
        double target = 240;
        double lowest = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            String slug = sortie.enemyKinds().get(enemy.kind()).slug();
            double y = enemy.renderY(1);
            if ((slug.equals("cracking-tower") || slug.equals("pump-station")) && y > 120 && y < 520 && y < lowest) {
                lowest = y;
                target = enemy.renderX(1);
            }
        }
        int commands = 0;
        if (sortie.ship().x() < target - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > target + 3) {
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
