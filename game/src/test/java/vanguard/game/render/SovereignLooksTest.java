package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part H (step H4): the Iron Sovereign's looks ({@link SovereignLooks}) and Level 35's mass relay ({@link
 * MassRelayLooks}): the art has every set the looks read; the segment rule places a frame as the pivots' rule says (the
 * mirror and the quarter turns, a pixel cell to a pixel cell); the turrets', mounts', core's and bays' frames; the
 * death's timing; the relay's frames and flash.
 */
class SovereignLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(35).orElseThrow();

    private static JsonValue pivots() throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/iron-sovereign.json")));
    }

    private static SetPieceSpec boss() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        return script.setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        JsonValue pivots = pivots();
        for (JsonValue ring = pivots.get("rings").child; ring != null; ring = ring.next) {
            assertEquals(ring.getInt("n8") + 1, frames("iron-sovereign-segment-" + ring.name), ring.name);
            assertEquals(ring.getInt("n8") + 1, ring.get("frames").size, ring.name + "'s pivots");
        }
        for (String kind : new String[] {"sam", "rail", "laser"}) {
            assertEquals(SovereignLooks.HEADINGS, frames("iron-sovereign-" + kind), kind + " at 64 headings");
            assertEquals(3, frames("iron-sovereign-" + kind + "-mount"), kind + "'s mount: dark, gold, red");
        }
        assertEquals(SovereignLooks.HEADINGS, frames("iron-sovereign-sam-open"));
        assertEquals(SovereignLooks.CORE_FRAMES, frames("iron-sovereign-core"));
        for (int b = 0; b < 4; b++) {
            assertEquals(4, frames("iron-sovereign-bay-" + b), "bay " + b);
        }
        assertEquals(4, frames("iron-sovereign-core-glow"));
        assertEquals(10, frames("iron-sovereign-cookoff"));
        assertEquals(12, frames("iron-sovereign-implosion"));
        for (String set : new String[] {"line", "glow", "bolt", "trail"}) {
            assertTrue(frames("mass-relay-" + set) > 0, "the relay's " + set);
        }
        assertTrue(frames("sentinel-tower-telegraph-120") > 0, "the laser batteries' wedge (the tower's)");
        assertTrue(frames("rail-bunker-rail") > 0, "the rail batteries' rail (the bunker's)");
    }

    /** The pivots' rule: r ≤ 45 the frame round(r / 45 × n8), else round((90 − r) / 45 × n8) mirrored; q quarter turns. */
    @Test
    void theSegmentRulePicksTheFrameTheMirrorAndTheTurns() {
        assertEquals(0, SovereignLooks.segmentFrame(0, 87));
        assertFalse(SovereignLooks.segmentMirrored(0));
        assertEquals(0, SovereignLooks.segmentTurns(0));
        assertEquals(87, SovereignLooks.segmentFrame(45, 87));
        assertFalse(SovereignLooks.segmentMirrored(45));
        assertEquals(Math.round(40 / 45.0 * 87), SovereignLooks.segmentFrame(50, 87));
        assertTrue(SovereignLooks.segmentMirrored(50));
        assertEquals(1, SovereignLooks.segmentTurns(90));
        assertEquals(0, SovereignLooks.segmentFrame(90, 87));
        assertEquals(3, SovereignLooks.segmentTurns(-10), "350°");
        assertTrue(SovereignLooks.segmentMirrored(-10));
        assertEquals(Math.round(10 / 45.0 * 87), SovereignLooks.segmentFrame(-10, 87));
        assertEquals(SovereignLooks.segmentFrame(30, 62), SovereignLooks.segmentFrame(30 + 720, 62));
    }

    /**
     * A frame at offset (x0, y0), w × h: mirrored its pixels start at (−y0 − h, −x0 − w), h × w; each quarter turn makes
     * an offset (x, y) of a w × h frame (−y − h, x) (the pivots' rule, exact on the pixel grid round the centre's corner).
     */
    @Test
    void theRuleMapsAFramesPixelCellsToPixelCells() {
        int x0 = -54;
        int y0 = 175;
        int w = 108;
        int h = 45;
        assertEquals(-y0 - h, minX(x0, y0, w, h, true, 0), 1e-9);
        assertEquals(-x0 - w, minY(x0, y0, w, h, true, 0), 1e-9);
        assertEquals(-y0 - h, minX(x0, y0, w, h, false, 1), 1e-9);
        assertEquals(x0, minY(x0, y0, w, h, false, 1), 1e-9);
        // Two quarter turns: the frame upside down round the centre, (−x0 − w, −y0 − h).
        assertEquals(-x0 - w, minX(x0, y0, w, h, false, 2), 1e-9);
        assertEquals(-y0 - h, minY(x0, y0, w, h, false, 2), 1e-9);
        // A segment at heading 0 lies straight below the centre (screen y down), at 90° to its left, at 180° above.
        assertTrue(minY(x0, y0, w, h, false, 0) > 0);
        assertTrue(minX(x0, y0, w, h, false, 1) + h < 0, "heading 90°: left of the centre");
        assertTrue(minY(x0, y0, w, h, false, 2) + h < 0, "heading 180°: above it");
    }

    private static double minX(int x0, int y0, int w, int h, boolean mirrored, int turns) {
        return Stream.of(new int[] {x0, y0}, new int[] {x0 + w, y0}, new int[] {x0, y0 + h}, new int[] {x0 + w, y0 + h})
                .mapToDouble(p -> SovereignLooks.turnX(p[0], p[1], mirrored, turns))
                .min()
                .orElseThrow();
    }

    private static double minY(int x0, int y0, int w, int h, boolean mirrored, int turns) {
        return Stream.of(new int[] {x0, y0}, new int[] {x0 + w, y0}, new int[] {x0, y0 + h}, new int[] {x0 + w, y0 + h})
                .mapToDouble(p -> SovereignLooks.turnY(p[0], p[1], mirrored, turns))
                .min()
                .orElseThrow();
    }

    @Test
    void theTurretsMountsCoreAndBaysFrames() {
        assertEquals(0, SovereignLooks.turretFrame(0));
        assertEquals(32, SovereignLooks.turretFrame(Math.PI));
        assertEquals(63, SovereignLooks.turretFrame(-2 * Math.PI / 64));
        assertEquals(16, SovereignLooks.turretFrame(Math.PI / 2 + 0.01));
        assertEquals(SovereignLooks.MOUNT_DARK, SovereignLooks.mountFrame(false, false));
        assertEquals(SovereignLooks.MOUNT_GOLD, SovereignLooks.mountFrame(true, false), "lit, out of the arc");
        assertEquals(SovereignLooks.MOUNT_ARMED, SovereignLooks.mountFrame(true, true), "red in the arc");
        assertEquals(0, SovereignLooks.coreFrame(false, false, 0), "shut");
        assertEquals(6, SovereignLooks.coreFrame(true, false, 0.5), "parting");
        assertEquals(11, SovereignLooks.coreFrame(false, true, 1), "open");
        int tell = SimStep.ticks(0.5);
        assertEquals(1, SovereignLooks.bayFrame(0, tell));
        assertEquals(2, SovereignLooks.bayFrame(tell - 1, tell));
        assertEquals(SovereignLooks.BAY_OPEN, SovereignLooks.bayFrame(tell, tell));
    }

    /**
     * The death on Level 35's numbers: thirteen batteries cook off 0.15 s apart (1.95 s), the core blows 1.15 s later
     * (the flash and the shower, {@link LevelRenderer#ringBlastTicks}), the segments drift 14 → 22 px/s over 3.5 s.
     */
    @Test
    void theDeathCooksOffThenBlowsThenDrifts() throws IOException {
        SovereignLooks looks = SovereignLooks.following(boss(), pivots());
        assertEquals(14, looks.parts(), "the core and 13 batteries");
        assertEquals(1.95, looks.cookoffsEnd(), 1e-9);
        assertEquals(SimStep.ticks(1.95 + 1.15), looks.blastTicks());
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                60);
        assertNotNull(SovereignLooks.sovereign(sortie), "the ringed boss is among the set pieces");
        assertEquals(looks.blastTicks(), LevelRenderer.ringBlastTicks(SovereignLooks.sovereign(sortie)));
        assertEquals(0, SovereignLooks.drift(0), 1e-9);
        assertEquals(14 * 3.5 + 8 / 3.5 * 3.5 * 3.5 / 2, SovereignLooks.drift(3.5), 1e-9);
        assertTrue(SovereignLooks.driftTurn(0, 0, 1) * SovereignLooks.driftTurn(0, 1, 1) < 0, "alternate signs");
        assertEquals(1.95 + 3.5, looks.deathSeconds(), 1e-9);
    }

    @Test
    void theRelaysLineBoltTrailAndFlash() {
        assertEquals(0, MassRelayLooks.telegraphFrame(0, 4));
        assertEquals(2, MassRelayLooks.telegraphFrame(0.6, 4));
        assertEquals(3, MassRelayLooks.telegraphFrame(1, 4));
        assertEquals(0.55f, MassRelayLooks.flashAlpha(0), 1e-6);
        assertEquals(0.4f, MassRelayLooks.flashAlpha(0.05), 1e-6);
        assertEquals(0.2f, MassRelayLooks.flashAlpha(0.1), 1e-6);
        assertEquals(0f, MassRelayLooks.flashAlpha(0.15), 1e-6);
        assertEquals(0, MassRelayLooks.trailFrame(0, 6));
        assertEquals(5, MassRelayLooks.trailFrame(0.59, 6));
        assertEquals(-1, MassRelayLooks.trailFrame(0.6, 6), "faded after 0.6 s");
    }

    private static int frames(String name) throws IOException {
        if (Files.isRegularFile(SPRITES.resolve(name + ".png"))) {
            return 1;
        }
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.map(path -> path.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }
}
