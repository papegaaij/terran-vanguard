package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.VrellLevel;

/**
 * M11 part B (step B4), Level 43's sounds (design/campaign Level 43, Music &amp; ambience; design/audio/sfx, Act 7
 * sounds; round 84's picks): track 16 "Far Shore" by section (its intro over the mouth, the base from the pickets, the
 * full mix from the ring's shadow, its {@code choir} stem from the Choir's line to the transit), the {@code vrell-gate}
 * bed (round 89's proposal a until the pick), the theme out at the transit's start; flown, the Gate Warden's telegraph
 * and beam (never the rail's), its rockfall over its rung, a glyph stone's break, and the gate transit's roar timed to
 * end with the level inside the warp.
 */
class Level43SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(43).orElseThrow();
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    @Test
    void theThemeTheBedAndTheNewSoundsAreThere() {
        LevelData.Music data = CONTENT.level(KEY).music();
        assertEquals(16, data.track(), "Act 7 A, Far Shore");
        assertEquals("far-shore", Tracks.name(16).orElseThrow());
        for (String file :
                new String[] {Tracks.path(16), Tracks.basePath("far-shore"), Tracks.motifPath("far-shore")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(1, data.startSection(), "the theme's intro over the mouth");
        assertEquals(3, data.fullSection(), "the full mix from the ring's shadow");
        assertFalse(data.motifOn(4, 207), "no choir before the Choir's line");
        assertTrue(data.motifOn(4, 209), "the choir stem from the Choir's line");
        assertFalse(data.motifOn(5, 263), "out at the transit");
        assertEquals(262, data.ambienceFrom().orElseThrow(), 1e-9, "the theme out at the transit's start");
        assertEquals(Sfx.AMBIENCE_VRELL_GATE, Sfx.ambience(data.ambience()));
        Map<Sfx, String> files = Map.of(
                Sfx.AMBIENCE_VRELL_GATE, "vrell-gate-r89-a.ogg",
                Sfx.WARDEN_TELEGRAPH, "warden-telegraph-r84-b.ogg",
                Sfx.WARDEN_BEAM, "warden-beam-r84-a.ogg",
                Sfx.WARDEN_DEATH, "warden-death-r84-a.ogg",
                Sfx.GLYPH_STONE_BREAK, "glyph-stone-break-r84-a.ogg",
                Sfx.GATE_TRANSIT, "gate-transit-r84-a.ogg");
        files.forEach((sfx, file) -> {
            assertEquals("sfx/" + file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        bank.dispose();
    }

    /** The roar starts 3.5 s before the warp's end: t=264.5 on Level 43 (the formation at 262, the warp 266.5-268). */
    @Test
    void theTransitsRoarEndsWithTheLevel() {
        VrellLevel.Transit transit = new VrellLevel.Transit(262, 266.5, 1.5);
        assertEquals(SimStep.ticks(2.5), GateCues.transitDelaySteps(transit));
        bank.dispose();
    }

    /**
     * Flown on medium with the ship invulnerable low under the nearest Warden, the Tesla pod and the pulse cannon firing:
     * the Wardens' telegraphs and beams (never the rail's charge or shot), a Warden's rockfall, a glyph stone's break
     * and the transit's roar after the formation.
     */
    @Test
    void theGateSoundsItsOwn() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 4)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds =
                new FlightSounds(bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of());
        boolean telegraph = false;
        boolean beam = false;
        boolean rail = false;
        boolean death = false;
        boolean stone = false;
        boolean roar = false;
        double roarAt = Double.NaN;
        int last = SimStep.ticks(270);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            now[0] += NANOS_PER_STEP;
            sortie.step(steer(sortie) | Command.FIRE.bit());
            sounds.step();
            sounds.play(sortie.events());
            sounds.watch(sortie);
            telegraph |= bank.playing(Sfx.WARDEN_TELEGRAPH) > 0;
            beam |= bank.playing(Sfx.WARDEN_BEAM) > 0;
            rail |= bank.playing(Sfx.RAIL_CHARGE) > 0 || bank.playing(Sfx.RAIL_SHOT) > 0;
            death |= bank.playing(Sfx.WARDEN_DEATH) > 0;
            stone |= bank.playing(Sfx.GLYPH_STONE_BREAK) > 0;
            if (!roar && bank.playing(Sfx.GATE_TRANSIT) > 0) {
                roar = true;
                roarAt = sortie.levelSeconds();
            }
        }
        sounds.stopLoops();
        assertTrue(telegraph, "a Warden's telegraph");
        assertTrue(beam, "a Warden's beam");
        assertFalse(rail, "never the rail's charge or shot");
        assertTrue(death, "a Warden's rockfall");
        assertTrue(stone, "a glyph stone's break");
        assertTrue(roar, "the transit's roar");
        assertEquals(264.5, roarAt, 0.1, "the roar 3.5 s before the level's end");
        bank.dispose();
    }

    /** Low on the field, under the nearest Warden on the screen, else under the glyph stones' strip, else the middle. */
    private static int steer(Sortie sortie) {
        double target = 240;
        double best = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            var enemy = sortie.enemy(e);
            if (enemy.spec().slug().equals(GateCues.WARDEN)
                    && enemy.renderY(1) < 540
                    && Math.abs(enemy.renderX(1) - sortie.ship().x()) < best) {
                best = Math.abs(enemy.renderX(1) - sortie.ship().x());
                target = enemy.renderX(1);
            }
        }
        if (best == Double.POSITIVE_INFINITY && sortie.levelSeconds() > 205 && sortie.levelSeconds() < 240) {
            target = 100 + 280 * ((sortie.tick() / 120) % 2);
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
