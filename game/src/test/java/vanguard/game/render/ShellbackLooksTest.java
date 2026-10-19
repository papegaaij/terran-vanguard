package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part D: the Shellback's looks (design/enemies/ground/shellback; tools/art/siege_units.py's sprites and
 * assets/pivots/shellback.json), on a hand-built sortie ({@link SiegeUnitsFixture}): the curl over its four frames
 * and the uncurl backwards, the ball turning a frame every 3.7 px rolled, the vent's swell over the 0.5 s tell and
 * its pivot per heading toward the tail, the belly glow fading out at the end of its window; flown, the walk, the
 * tell, the curl, roll, uncurl and belly follow each other and a hit on the curled shell glances off.
 */
class ShellbackLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    @Test
    void theCurlRunsThroughItsFourFramesAndTheUncurlBackwards() {
        assertEquals(0, ShellbackLooks.curlFrame(0, 0, false));
        assertEquals(1, ShellbackLooks.curlFrame(0, 0.3, false));
        assertEquals(3, ShellbackLooks.curlFrame(0, 0.99, false));
        assertEquals(3, ShellbackLooks.curlFrame(0, 1, false), "half way into the ball at its end");
        assertEquals(5 * 4 + 3, ShellbackLooks.curlFrame(5, 0, true), "the uncurl starts tucked");
        assertEquals(5 * 4, ShellbackLooks.curlFrame(5, 1, true), "and ends a third drawn in");
    }

    @Test
    void theBallTurnsAFrameEvery3point7PxRolled() {
        assertEquals(0, ShellbackLooks.rollFrame(0, 0));
        assertEquals(1, ShellbackLooks.rollFrame(0, 3.8));
        assertEquals(0, ShellbackLooks.rollFrame(0, 8 * 3.7 + 0.1), "one band pair per 8 frames");
        assertEquals(15 * 8 + 2, ShellbackLooks.rollFrame(15, 2 * 3.7 + 0.1));
    }

    @Test
    void theVentSwellsOverItsTell() {
        assertEquals(-1, ShellbackLooks.ventFrame(-1, 3));
        assertEquals(0, ShellbackLooks.ventFrame(0, 3), "low");
        assertEquals(1, ShellbackLooks.ventFrame(ShellbackLooks.TELL_TICKS / 2, 3), "half");
        assertEquals(2, ShellbackLooks.ventFrame(ShellbackLooks.TELL_TICKS - 1, 3), "full as it lobs");
        assertEquals(-1, ShellbackLooks.ventFrame(ShellbackLooks.TELL_TICKS, 3), "gone after the lob");
        assertEquals(SimStep.ticks(0.5), ShellbackLooks.TELL_TICKS);
    }

    @Test
    void theBellyGlowsAndFadesOutAtTheEndOfItsWindow() {
        assertEquals(1, ShellbackLooks.bellyOpacity(0), 1e-6);
        assertEquals(1, ShellbackLooks.bellyOpacity(0.8), 1e-6);
        assertEquals(0.5, ShellbackLooks.bellyOpacity(0.9), 1e-6);
        assertEquals(0, ShellbackLooks.bellyOpacity(1), 1e-6);
        assertTrue(ShellbackLooks.ventShows(Enemy.CurlPhase.BELLY));
        assertTrue(ShellbackLooks.ventShows(Enemy.CurlPhase.WALK));
        assertFalse(ShellbackLooks.ventShows(Enemy.CurlPhase.ROLLING), "the ball hides it");
    }

    @Test
    void theVentSitsAtItsPivotTowardTheTail() throws IOException {
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/shellback.json")));
        double[][] vent = ShellbackLooks.ventOffsets(pivots);
        assertEquals(0, vent[0][0], 0.5, "heading 0 walks down");
        assertTrue(vent[0][1] > 10, "its tail up the screen: " + vent[0][1]);
        assertTrue(vent[4][0] > 10, "heading 4 faces left, its tail to the right: " + vent[4][0]);
        assertTrue(vent[8][1] < -10, "heading 8 walks up");
        assertTrue(vent[12][0] < -10, "heading 12 faces right");
        assertEquals(0, ShellbackLooks.ventOffsets(null)[3][1], "without the file: the frame's centre");
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(16 * 6, frames("shellback"), "16 headings x 6 heavy-walk frames");
        assertEquals(16 * ShellbackLooks.CURL_FRAMES, frames("shellback-curl"));
        assertEquals(16 * ShellbackLooks.ROLL_FRAMES, frames("shellback-roll"));
        assertEquals(16, frames("shellback-belly"));
        assertEquals(16, frames("shellback-husk"), "the husk at its last heading (EnemyLooks)");
        assertEquals(3, frames("shellback-vent"));
        assertTrue(Files.isRegularFile(SPRITES.resolve("shellback-vent-dead.png")));
    }

    @Test
    void flownItWalksTellsCurlsRollsUncurlsAndShowsItsBelly() {
        Sortie sortie = SiegeUnitsFixture.shellback();
        List<Enemy.CurlPhase> phases = new ArrayList<>();
        int tells = 0;
        int glanced = 0;
        int curled = 0;
        double walkedAtRoll = Double.NaN;
        double rolled = 0;
        Enemy shellback = null;
        // The ship's column is its path: its fire brings the body down to 40 % and it curls.
        for (int i = 0;
                i < SimStep.ticks(40)
                        && (phases.isEmpty()
                                || phases.getLast() != Enemy.CurlPhase.WALK
                                || !phases.contains(Enemy.CurlPhase.BELLY));
                i++) {
            sortie.step(Command.FIRE.bit());
            Enemy now = SiegeUnitsFixture.find(sortie, SiegeUnitsFixture.SHELLBACK);
            shellback = now == null ? shellback : now;
            if (now == null) {
                break;
            }
            tells += sortie.events().count(SimEvents.Type.MORTAR_TELL);
            curled += sortie.events().count(SimEvents.Type.CURLED);
            if (now.armoured()) {
                glanced += sortie.events().count(SimEvents.Type.SHOT_GLANCED);
            }
            Enemy.CurlPhase phase = now.curlPhase();
            if (phases.isEmpty() || phases.getLast() != phase) {
                phases.add(phase);
            }
            if (phase == Enemy.CurlPhase.ROLLING) {
                if (Double.isNaN(walkedAtRoll)) {
                    walkedAtRoll = now.walked();
                }
                rolled = now.walked() - walkedAtRoll;
                int frame = ShellbackLooks.rollFrame(EnemyLooks.heading(now.facing(), 16), now.walked());
                assertTrue(frame >= 0 && frame < 16 * ShellbackLooks.ROLL_FRAMES);
            }
        }
        assertTrue(tells > 0, "the vent swelled before a lob");
        assertEquals(1, curled, "it curls once");
        assertEquals(
                List.of(
                        Enemy.CurlPhase.WALK,
                        Enemy.CurlPhase.CURLING,
                        Enemy.CurlPhase.ROLLING,
                        Enemy.CurlPhase.UNCURLING,
                        Enemy.CurlPhase.BELLY,
                        Enemy.CurlPhase.WALK),
                phases);
        assertTrue(glanced > 0, "shots spark off the curled shell");
        assertEquals(140 * 2.5, rolled, 10, "the ball rolls 2.5 s at 140 px/s: its frames turn with it");
    }

    private static int frames(String name) throws IOException {
        try (var files = Files.list(SPRITES)) {
            return (int) files.map(path -> path.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }
}
