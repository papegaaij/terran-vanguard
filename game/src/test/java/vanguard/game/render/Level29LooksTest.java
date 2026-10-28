package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;

/**
 * M9 part B, Level 29's looks (step B4; tools/art/belt_units.py, backdrop_l29.py): the art's frames as the looks count
 * them; the Talon's twin muzzles and the clamp gun's muzzle and jaws from their pivots; the glints' and the hatch's
 * tell before a predicted shot; the Stormhawk's IFF blink; the clamps' sides; the belt rocks' spin; Block C's lights
 * turning at the nests' wake over 1 s; the tender's hold and the Ember's launch and flight away.
 */
class Level29LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static long frames(Path folder, String name) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static boolean single(Path folder, String name) {
        return Files.isRegularFile(folder.resolve(name + ".png"));
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        int headings = CeresLooks.HEADINGS;
        assertEquals(headings, frames(SPRITES, CeresLooks.TALON));
        assertEquals(headings, frames(SPRITES, CeresLooks.TALON + "-leader"));
        assertEquals(3, frames(SPRITES, CeresLooks.TALON + "-glint"));
        assertEquals(8, frames(SPRITES, CeresLooks.TALON + "-wing"));
        assertEquals(headings, frames(SPRITES, CeresLooks.NEST));
        assertEquals(headings, frames(SPRITES, CeresLooks.NEST + "-open"));
        assertEquals(4, frames(SPRITES, CeresLooks.NEST + "-wake"));
        for (String one : new String[] {"-base", "-base-dormant", "-dormant", "-wreck"}) {
            assertTrue(single(SPRITES, CeresLooks.NEST + one), one);
        }
        assertEquals(headings * CeresLooks.MITE_POSES, frames(SPRITES, CeresLooks.MITE));
        for (String set : new String[] {"", "-iff", "-hit"}) {
            assertEquals(headings * CeresLooks.BANKS, frames(SPRITES, CeresLooks.HAWK + set), set);
        }
        assertEquals(headings, frames(SPRITES, CeresLooks.CLAMP));
        assertEquals(3, frames(SPRITES, CeresLooks.CLAMP + "-glint"));
        assertEquals(6, frames(SPRITES, CeresLooks.CLAMP + "-gas"));
        for (String side : new String[] {"left", "right"}) {
            assertTrue(single(SPRITES, CeresLooks.CLAMP + "-clamp-" + side), side);
            assertEquals(6, frames(SPRITES, CeresLooks.CLAMP + "-clamp-" + side + "-open"), side);
        }
        assertEquals(8, frames(SPRITES, "rook-launch"));
        Path backdrop = ASSETS.resolve("backdrop/level-29");
        assertEquals(6, frames(backdrop, "prison-tender" + Backdrop.HOLD));
        for (String piece : new String[] {"block-c", "coalition-block-3", "coalition-block-4"}) {
            assertTrue(single(backdrop, piece + Backdrop.GLOW), piece);
            assertTrue(single(backdrop, piece + Backdrop.GLOW + Backdrop.TURNED), piece + " turns red");
        }
        LevelData level = CONTENT.level(CONTENT.levelKey(29).orElseThrow());
        for (String chunk : level.debris().orElseThrow().chunks().keySet()) {
            assertEquals(PropPasses.SPIN_FRAMES, frames(SPRITES, "debris-" + chunk), chunk);
            assertEquals(8, frames(SPRITES, "debris-" + chunk + "-break"), chunk);
        }
    }

    @Test
    void theMuzzlesAndJawsComeFromThePivots() {
        JsonValue talon = new JsonReader().parse(new FileHandle(new File(ASSETS.toFile(), "pivots/talon.json")));
        double[][][] muzzles = CeresLooks.muzzles(talon);
        assertEquals(CeresLooks.HEADINGS, muzzles.length);
        // Heading 0 flies down the screen: the twin muzzles side by side below the centre, mirrored.
        assertEquals(-muzzles[0][0][0], muzzles[0][1][0], 1e-9);
        assertTrue(muzzles[0][0][1] < 0, "below the centre (y up)");
        JsonValue clamp = new JsonReader().parse(new FileHandle(new File(ASSETS.toFile(), "pivots/clamp-gun.json")));
        double[][] muzzle = CeresLooks.clampMuzzle(clamp);
        assertEquals(0, muzzle[0][0], 0.5, "heading 0's barrel straight down");
        assertTrue(muzzle[0][1] < -15, "at the barrel's tip");
        double[][] jaw = CeresLooks.jaw(clamp);
        assertTrue(jaw[0][0] > 0, "a left clamp's jaw points right, at the tender");
        assertTrue(jaw[1][0] < 0, "a right clamp's jaw points left");
        assertEquals(0, CeresLooks.side(150));
        assertEquals(1, CeresLooks.side(330));
        assertEquals(0, CeresLooks.muzzles(null)[3][1][0], "no file: the centre");
    }

    @Test
    void aTellRunsOverItsLastStepsBeforeThePredictedShot() {
        int tell = SimStep.ticks(0.3);
        assertEquals(-1, CeresLooks.tellShare(tell + 1, 0.3), "before the tell");
        assertEquals(0, CeresLooks.tellShare(tell, 0.3), 1e-9, "its start");
        assertTrue(CeresLooks.tellShare(1, 0.3) > 0.9, "its end");
        assertEquals(-1, CeresLooks.tellShare(0, 0.3), "the shot");
        assertEquals(-1, CeresLooks.tellShare(-30, 0.3), "overdue: the gun held fire, no glint");
        assertEquals(-1, CeresLooks.tellShare(Long.MAX_VALUE, 0.3), "no clock");
    }

    @Test
    void theIffLightBlinksAtOneHertz() {
        int lit = 0;
        for (long t = 0; t < SimStep.PER_SECOND; t++) {
            lit += CeresLooks.iffLit(t) ? 1 : 0;
        }
        assertEquals(SimStep.PER_SECOND / 2, lit, "lit half of each second");
        assertTrue(CeresLooks.iffLit(0));
        assertFalse(CeresLooks.iffLit(CeresLooks.BLINK_TICKS));
    }

    @Test
    void aBeltRockTurnsThroughItsSpinFrames() {
        Set<Integer> seen = new HashSet<>();
        for (long t = 0; t < 16L * PropPasses.SPIN_FRAME_TICKS; t++) {
            int frame = PropPasses.spinFrame(t, 3);
            assertTrue(frame >= 0 && frame < PropPasses.SPIN_FRAMES);
            seen.add(frame);
        }
        assertEquals(PropPasses.SPIN_FRAMES, seen.size(), "a full turn in 16 frames");
        assertEquals(PropPasses.spinFrame(0, 4), PropPasses.spinFrame(PropPasses.SPIN_FRAME_TICKS - 1, 4));
        assertEquals(
                Math.floorMod(PropPasses.spinFrame(0, 4) + 1, PropPasses.SPIN_FRAMES),
                PropPasses.spinFrame(PropPasses.SPIN_FRAME_TICKS, 4),
                "even serials turn one way");
        assertEquals(
                Math.floorMod(PropPasses.spinFrame(0, 5) - 1, PropPasses.SPIN_FRAMES),
                PropPasses.spinFrame(PropPasses.SPIN_FRAME_TICKS, 5),
                "odd ones the other");
    }

    @Test
    void blockCsLightsTurnRedAtTheNestsWakeOverOneSecond() {
        LevelData level = CONTENT.level(CONTENT.levelKey(29).orElseThrow());
        double turn = Backdrop.turnSeconds(level);
        assertEquals(60, turn, 1e-9, "Block C's nests' dormant_until");
        assertEquals(0, Backdrop.turnShare(turn, 59.9), 1e-9);
        assertEquals(0.5, Backdrop.turnShare(turn, 60.5), 1e-6);
        assertEquals(1, Backdrop.turnShare(turn, 61.5), 1e-9);
        assertTrue(Double.isNaN(
                Backdrop.turnSeconds(CONTENT.level(CONTENT.levelKey(28).orElseThrow()))));
        assertEquals(0, Backdrop.turnShare(Double.NaN, 100), 1e-9, "a level without a turn");
    }

    @Test
    void theEmberRisesFromTheHoldAndAnUnclaimedOneLeavesUpward() {
        assertEquals(0, RescueLooks.launchFrame(0, 8));
        assertEquals(7, RescueLooks.launchFrame(8 * RescueLooks.LAUNCH_FRAME_TICKS - 1, 8));
        assertEquals(-1, RescueLooks.launchFrame(8 * RescueLooks.LAUNCH_FRAME_TICKS, 8), "then his craft");
        assertEquals(-1, RescueLooks.launchFrame(0, 0), "without its art his craft at once");
        double y = RescueLooks.leftY(290, SimStep.PER_SECOND);
        assertEquals(290 + RescueLooks.LEAVE_SPEED, y, 1e-6, "up the screen at its speed");
        assertTrue(RescueLooks.leftY(290, 2 * SimStep.PER_SECOND) > PlayField.HEIGHT, "off the top in under 2 s");
    }

    @Test
    void theHudCountsTheWaveringCraftAndTheClamps() {
        assertEquals("0 / 6", MissionPanel.sparedCount(0, 6, false, false));
        assertEquals("4 / 6", MissionPanel.sparedCount(4, 6, false, false));
        assertEquals("DONE", MissionPanel.sparedCount(6, 6, true, false));
        assertEquals("FAILED", MissionPanel.sparedCount(2, 6, false, true));
        assertEquals("CLAMPS", MissionPanel.CLAMPS);
    }
}
