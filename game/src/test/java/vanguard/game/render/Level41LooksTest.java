package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Domes;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Uplinks;

/**
 * M10 part G, step G4 (design/campaign Level 41; tools/art/shield_pylon.py, l41_props.py, backdrop_l41.py): Level 41's
 * looks and HUD by their rules: the pylon's crown facing (16 facings, toward its dome), the emitter dark while it sleeps,
 * the topple's frames; the uplink's ring by the upload's share; the blast doors' frames over their seconds and the
 * backdrop's three door rows (one per pylon gate); the CDF beacon's fall scale and that no drone carries it; the gate
 * tracker's three pips (the pylon gates, not the Halo's halt) and {@code FILES n / 3}; flown on medium to Gate One's
 * halt: every awake pylon's crown faces its dome's centre and the archive core's hatch is under it.
 */
class Level41LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(41).orElseThrow();

    @Test
    void thePylonsTheUplinkAndTheDoorsByTheirRules() {
        assertEquals(0, PylonLooks.facing(0), "facing down");
        assertEquals(4, PylonLooks.facing(Math.PI / 2), "clockwise from down: left");
        assertEquals(8, PylonLooks.facing(Math.PI), "up");
        assertEquals(0, PylonLooks.facing(2 * Math.PI - 0.01));
        assertEquals(0, PylonLooks.facing(PylonLooks.heading(100, 300, 100, 200)), "a dome below faces down");
        assertEquals(8, PylonLooks.facing(PylonLooks.heading(100, 300, 100, 400)), "a dome above faces up");
        assertEquals(-1, PylonLooks.emitterFrame(10, 3, true, 4), "dark while it sleeps");
        assertTrue(PylonLooks.emitterFrame(10, 3, false, 4) >= 0, "lit once awake");
        assertEquals(0, PylonLooks.toppleFrame(0, 6));
        assertEquals(5, PylonLooks.toppleFrame(SimStep.ticks(0.4), 6), "the topple over in 0.4 s");
        assertEquals(5, PylonLooks.toppleFrame(SimStep.ticks(10), 6), "its last frame stays");
        assertEquals(0, UplinkLooks.ringFrame(0, 32), "the bare track");
        assertEquals(16, UplinkLooks.ringFrame(0.52, 32));
        assertEquals(31, UplinkLooks.ringFrame(1, 32), "full");
        int seconds = SimStep.ticks(2);
        assertEquals(0, BackdropDoors.frame(-1, seconds, 12), "shut until the gate opens");
        assertEquals(0, BackdropDoors.frame(0, seconds, 12));
        assertEquals(11, BackdropDoors.frame(seconds, seconds, 12), "open after its 2 s");
        assertEquals(11, BackdropDoors.frame(10 * seconds, seconds, 12), "and held");
        assertEquals(2f, CdfBeaconLooks.fallScale(-CdfBeaconLooks.FALL_SECONDS), 1e-6f, "from 2x");
        assertEquals(1f, CdfBeaconLooks.fallScale(0), 1e-6f, "to 1x as it lands");
    }

    @Test
    void theDataGivesTheDoorsTheBeaconAndTheTracker() throws IOException {
        LevelData level = CONTENT.level(KEY);
        List<BackdropData.Door> doors = level.backdrop().doors().orElseThrow();
        assertEquals(
                List.of(1, 2, 3), doors.stream().map(BackdropData.Door::gate).toList(), "one per pylon gate");
        for (BackdropData.Door door : doors) {
            BackdropData.Piece piece = level.backdrop().pieces().get(door.piece());
            assertEquals(12, piece.imageCount(), door.piece());
            assertTrue(Files.isRegularFile(ASSETS.resolve("backdrop/level-41/" + door.piece() + "_11.png")));
        }
        List<LevelData.Drop> beacons =
                level.released().stream().filter(CdfBeaconLooks::dropped).toList();
        assertEquals(1, beacons.size(), "the CDF beacon at t=20");
        assertFalse(beacons.getFirst().isMarked(), "not marked");
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                60);
        assertEquals(4, sortie.gates(), "three pylon gates and the Halo's halt");
        assertEquals(3, GateTracker.pylonGates(script, sortie.gates()), "three pips");
        assertTrue(script.gatePiece(3) >= 0, "the Halo holds the fourth");
        assertTrue(script.objectives().secondary().byFiles());
        assertEquals(
                "FILES", MissionPanel.filesLabel(script.objectives().secondary().label()));
        assertEquals("0 / 3", MissionPanel.filesCount(0, 3, false));
        assertEquals("2 / 3", MissionPanel.filesCount(2, 3, false));
        assertEquals("DONE", MissionPanel.filesCount(3, 3, true));
        assertTrue(UplinkLooks.drawn(script.groundObjects().stream()
                .filter(object -> object.look().equals(UplinkLooks.CORE))
                .findFirst()
                .orElseThrow()));
        assertEquals(
                null,
                GroundPasses.glowName(
                        script.groundObjects().stream()
                                .filter(object -> object.look().equals(UplinkLooks.CORE))
                                .findFirst()
                                .orElseThrow(),
                        name -> true),
                "the core's glow is the uplink's");
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/shield-pylon.json")));
        assertEquals(PylonLooks.FACINGS, pivots.get("lens").size, "a lens per facing");
    }

    /** Flown to Gate One's halt: each awake pylon's crown faces its dome; the courtyard core's hatch waits shut. */
    @Test
    void gateOnesPylonsFaceTheirDomes() throws IOException {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 1)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/shield-pylon.json")));
        PylonLooks looks = new PylonLooks(null, sortie.enemyKinds(), pivots);
        int last = SimStep.ticks(50);
        for (int step = 0; step < last && !(sortie.halting() && sortie.levelSeconds() > 43); step++) {
            sortie.step(0);
        }
        assertTrue(sortie.halting(), "Gate One's halt");
        Domes domes = sortie.domes();
        int faced = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!looks.isPylon(enemy.kind())) {
                continue;
            }
            for (int slot = 0; slot < domes.capacity(); slot++) {
                if (domes.tracked(slot) && domes.pylon(slot) == enemy.serial()) {
                    double dx = domes.x(slot) - enemy.renderX(1);
                    double dy = domes.y(slot) - enemy.renderY(1);
                    int k = looks.crownFacing(sortie, enemy);
                    double heading = k * 2 * Math.PI / PylonLooks.FACINGS;
                    // The facing's direction (clockwise from down, y up) points at the dome within half a facing.
                    double fx = -Math.sin(heading);
                    double fy = -Math.cos(heading);
                    double cos = (fx * dx + fy * dy) / Math.hypot(dx, dy);
                    assertTrue(cos > Math.cos(Math.PI / PylonLooks.FACINGS + 1e-6), "pylon " + enemy.serial());
                    faced++;
                }
            }
        }
        assertTrue(faced >= 2, "Gate One's pylons: " + faced);
        Uplinks uplinks = sortie.uplinks();
        assertEquals(Uplinks.SHUT, uplinks.state(0), "the courtyard's core under its hatch");
    }
}
