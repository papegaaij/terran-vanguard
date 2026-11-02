package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.PartSpec;
import vanguard.sim.SetPieceSpec;

/**
 * M10 part H (step H4a): the Ascendant's looks ({@link AscendantLooks}; tools/art/ascendant.py, assets/pivots/
 * ascendant.json): the art has every set the looks read; the pivots' parts sit where the data puts them (step H2's
 * places, the pivots y down); the panels' frames by facing, the turret's headings, the parts' damage states, the
 * eruption's keel and cracks frames, the heart's places; the wreck's halves part along a jagged fracture (round 72
 * A.11), drifting out over the escape; the lifeboat's tilt by its sideways speed.
 */
class AscendantLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(42).orElseThrow();

    private static JsonValue pivots() throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/ascendant.json")));
    }

    private static SetPieceSpec ascendant() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        return script.setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
    }

    private static int frames(String set) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.map(path -> path.getFileName().toString())
                    .filter(name -> name.equals(set + ".png")
                            || name.matches(java.util.regex.Pattern.quote(set) + "_\\d+\\.png"))
                    .count();
        }
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(AscendantLooks.HEADINGS, frames("ascendant-cannon"), "the turret at 64 headings");
        assertEquals(AscendantLooks.HEADINGS, pivots().get("cannon").get("muzzle").size, "its muzzles");
        int panels = pivots().get("panels").get("frames").size;
        for (String set : new String[] {"panel", "panel-glow", "panel-cracks", "panel-rim", "panel-down"}) {
            assertEquals(panels, frames("ascendant-" + set), set);
        }
        assertEquals(pivots().get("keel").getInt("frames"), frames("ascendant-keel"));
        assertEquals(3, frames("ascendant-cannon-base"), "whole, damaged, dead");
        assertEquals(4, frames("ascendant-generator"), "lit, damaged, dead, grafted");
        assertEquals(3, frames("ascendant-battery"));
        assertEquals(5, frames("ascendant-sac"), "rest, three swelling, split");
        assertEquals(4, frames("ascendant-heart"), "sealed, two opening, open");
        assertEquals(5, frames("ascendant-lifeboat"), "five tilts");
        assertEquals(5, pivots().get("lifeboat").get("drive").size, "a drive per tilt");
        for (String set : new String[] {
            "hull",
            "wreck-l",
            "wreck-r",
            "wreck-l-rim",
            "wreck-r-rim",
            "sac-dead",
            "sac-core",
            "heart-core",
            "generator-glow",
            "panel-sweep",
            "bay-light",
            "rack-lock",
            "lamp-red",
            "lamp-gold"
        }) {
            assertEquals(1, frames("ascendant-" + set), set);
        }
        for (String set : new String[] {
            "beam",
            "keel-cracks",
            "bay-l",
            "bay-r",
            "rack-l",
            "rack-r",
            "port-flash",
            "muzzle",
            "heart-burst",
            "sac-burst",
            "plate",
            "shard",
            "vent-orange",
            "vent-teal",
            "fire",
            "lifeboat-drive",
            "lifeboat-wake",
            "lifeboat-tear",
            "lifeboat-flash"
        }) {
            assertTrue(frames("ascendant-" + set) > 1, set);
        }
        assertTrue(frames("explosion-vacuum-medium") > 0 && frames("explosion-vacuum-large") > 0, "K's chain");
        assertTrue(frames("enemy-shield-medium") > 0, "the cannon's fed hex shield");
    }

    @Test
    void thePivotsPartsSitWhereTheDataPutsThem() throws IOException {
        SetPieceSpec piece = ascendant();
        JsonValue parts = pivots().get("parts");
        for (PartSpec part : piece.parts()) {
            JsonValue at = parts.get(part.name());
            assertNotNull(at, part.name());
            assertEquals(part.dx(), at.getDouble(0), 0.5, part.name() + " x");
            // The pivots are y down, the data y up.
            assertEquals(part.dy(), -at.getDouble(1), 0.5, part.name() + " y");
        }
        AscendantLooks looks = AscendantLooks.following(piece, pivots());
        assertTrue(looks.cannon >= 0 && looks.heart >= 0 && looks.lifeboat >= 0, "every part found by name");
        for (int p : looks.gens) {
            assertTrue(p >= 0);
        }
        for (int p : looks.batteries) {
            assertTrue(p >= 0);
        }
        for (int p : looks.sacs) {
            assertTrue(p >= 0);
        }
        assertEquals("heart", piece.parts().get(looks.heart).name(), "the hull breaks on the heart");
        assertEquals("lifeboat", piece.parts().get(looks.lifeboat).name());
    }

    @Test
    void thePanelsTheTurretAndTheStates() throws IOException {
        int[] headings = pivots().get("panels").get("headings").asIntArray();
        // The left face rests 45 degrees to its side of straight down (heading 8), the right face -45 (heading 56).
        assertEquals(8, headings[AscendantLooks.panelFrame(Math.toRadians(45), headings)]);
        assertEquals(56, headings[AscendantLooks.panelFrame(Math.toRadians(-45), headings)]);
        assertEquals(12, headings[AscendantLooks.panelFrame(Math.toRadians(80), headings)], "clamped to its frames");
        assertEquals(0, AscendantLooks.headingTo(240, 300, 240, 100), "straight down");
        assertEquals(16, AscendantLooks.headingTo(240, 300, 100, 300), "to the left: a quarter turn clockwise");
        assertEquals(48, AscendantLooks.headingTo(240, 300, 380, 300), "to the right");
        assertEquals(0, AscendantLooks.damageState(800, 800));
        assertEquals(1, AscendantLooks.damageState(399, 800), "below half");
        assertEquals(2, AscendantLooks.damageState(0, 800));
    }

    @Test
    void theEruptionsKeelAndCracks() {
        assertEquals(-1, AscendantLooks.keelFrame(0.5, 6, 3), "the shudder: the hull still whole");
        assertEquals(0, AscendantLooks.keelFrame(1.0, 6, 3));
        assertEquals(1, AscendantLooks.keelFrame(1.5, 6, 3), "0.5 s a frame");
        assertEquals(5, AscendantLooks.keelFrame(3.99, 6, 3));
        assertEquals(5, AscendantLooks.keelFrame(9, 6, 3), "held");
        assertEquals(0, AscendantLooks.cracksFrame(0.1, 3));
        assertEquals(2, AscendantLooks.cracksFrame(0.9, 3), "flaring by the shudder's end");
        assertEquals(2, AscendantLooks.cracksFrame(2.5, 3), "held under the erupting frames");
        assertEquals(1, AscendantLooks.cracksStrength(0.5, 3));
        assertEquals(0, AscendantLooks.cracksStrength(4, 3), 1e-6, "faded by the eruption's end");
        assertEquals(0.5, AscendantLooks.eruptionShake(0, 4), 1e-9, "the shake starts with the shudder");
        assertEquals(1, AscendantLooks.eruptionShake(2, 4), 1e-9);
        assertEquals(0, AscendantLooks.eruptionShake(4, 4), 1e-9, "over with the eruption");
        assertEquals(0, AscendantLooks.eruptionShake(Double.NaN, 4), "none outside it");
    }

    /** Round 72 A.11 (the user): the hull breaks along a jagged fracture; the halves never part on a straight edge. */
    @Test
    void theHalvesPartAlongAJaggedFractureAndDriftOutOverTheEscape() throws IOException {
        AscendantLooks looks = AscendantLooks.following(ascendant(), pivots());
        for (int side = 0; side < 2; side++) {
            double[][] line = looks.fracture[side];
            assertTrue(line.length >= 12, "an authored line of many points");
            assertTrue(AscendantLooks.jaggedness(line) >= 8, "jagged: " + AscendantLooks.jaggedness(line));
        }
        assertEquals(0, AscendantLooks.driftShare(0, 14.5), 1e-9);
        assertEquals(1, AscendantLooks.driftShare(14.5, 14.5), 1e-9, "out of the sides by the escape's end");
        assertTrue(AscendantLooks.driftShare(3, 14.5) < AscendantLooks.driftShare(6, 14.5));
        // The halves drift outward: the left one left, the right one right (the pivots' drift, y down).
        JsonValue wreck = pivots().get("wreck");
        assertTrue(wreck.get("left").get("drift").getDouble(0) < 0);
        assertTrue(wreck.get("right").get("drift").getDouble(0) > 0);
    }

    @Test
    void theLifeboatLeansWithItsWeaveAndTheRunsFrames() {
        assertEquals(2, AscendantLooks.tiltFrame(0, 5), "upright");
        assertEquals(3, AscendantLooks.tiltFrame(30, 5), "moving right: leaning right");
        assertEquals(4, AscendantLooks.tiltFrame(500, 5), "at most 20 degrees");
        assertEquals(0, AscendantLooks.tiltFrame(-500, 5));
        assertEquals(0, AscendantLooks.runFrame(0, 12, 8));
        assertEquals(7, AscendantLooks.runFrame(0.62, 12, 8));
        assertEquals(-1, AscendantLooks.runFrame(0.67, 12, 8), "over");
        assertEquals(-1, AscendantLooks.runFrame(-0.1, 12, 8), "not yet");
    }
}
