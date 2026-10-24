package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.sim.RookLossSpec;

/**
 * M8 part F: Rook's loss's look (design/campaign Level 26, Rook's loss; tools/art/rook_loss.py; gap check F6): the art's
 * frames as the look counts them, the trails' lanes clear of the ship, the torpedoes rising from the bottom edge and
 * meeting him at their hits 0.1 s apart, the lamp's flicker dying with the spin, the spin frames turning with his hull;
 * and the headlights' haze (the cone's volume in empty water) strongest near the lamp.
 */
class RookLossLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final RookLossSpec LOSS =
            BlackwaterFixture.script(Difficulty.MEDIUM).rookLoss().orElseThrow();

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(ASSETS.resolve("sprites"))) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    @Test
    void theArtIsWhatTheLookDraws() throws IOException {
        assertEquals(16, frames("rook-spin"));
        assertEquals(16, frames("rook-spin-glow"));
        assertEquals(8, frames("rook-trail"));
        assertEquals(4, frames("rook-torpedo"));
        assertEquals(10, frames("rook-flash"));
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/rook-spin.json")));
        assertEquals(16, pivots.get("lamp").size, "the lamp per spin frame");
        assertEquals(new RookLossSpec(163.5, 162.5, 2.5), LOSS, "Level 26's loss");
    }

    /** The lanes are his x and x ± 48; one within 40 px of the ship moves to his far side (F6). */
    @Test
    void theLanesKeepClearOfTheShip() {
        double[] lanes = new double[3];
        RookLossLooks.lanes(100, 300, lanes);
        assertEquals(-48, lanes[0]);
        assertEquals(0, lanes[1]);
        assertEquals(48, lanes[2]);
        RookLossLooks.lanes(200, 250, lanes);
        assertEquals(-48, lanes[0]);
        assertEquals(0, lanes[1]);
        assertEquals(-96, lanes[2], "the lane over the ship goes to his far side");
        for (double offset : lanes) {
            assertTrue(Math.abs(200 + offset - 250) >= RookLossLooks.LANE_CLEAR);
        }
    }

    /** The torpedoes rise from below the bottom edge at the trails' time and meet him at their hits 0.1 s apart. */
    @Test
    void theTorpedoesRiseAndMeetHimAtTheirHits() {
        double[] head = new double[2];
        for (int i = 0; i < RookLossLooks.TORPEDOES; i++) {
            assertEquals(LOSS.t() + 0.1 * i, RookLossLooks.hit(LOSS, i), 1e-9);
            assertEquals(0, RookLossLooks.progress(LOSS, i, LOSS.trails()), 1e-9);
            assertEquals(1, RookLossLooks.progress(LOSS, i, RookLossLooks.hit(LOSS, i)), 1e-9);
        }
        RookLossLooks.head(150, 200, 120, 0, head);
        assertEquals(150, head[0], 1e-9);
        assertEquals(-RookLossLooks.BELOW, head[1], 1e-9);
        RookLossLooks.head(150, 200, 120, 1, head);
        assertEquals(200, head[0], 1e-9);
        assertEquals(120, head[1], 1e-9);
        RookLossLooks.head(150, 200, 120, 0.5, head);
        assertTrue(head[0] < 175, "it turns in on him late");
    }

    /** The lamp burns while he flies, is dark through the hits, flickers and dies over the spin, and is out when gone. */
    @Test
    void theLampFlickersAndDies() {
        assertEquals(1, RookLossLooks.lamp(false, false, 0, 0));
        assertEquals(0, RookLossLooks.lamp(true, false, 0.05, 0.02));
        assertEquals(0, RookLossLooks.lamp(true, true, 2.6, 1));
        int on = 0;
        int off = 0;
        double sum = 0;
        for (double t = 0.2; t < 1.2; t += 0.01) {
            float light = RookLossLooks.lamp(true, false, t, t / 2.5);
            assertTrue(light >= 0 && light <= 1);
            on += light > 0 ? 1 : 0;
            off += light == 0 ? 1 : 0;
            sum += light;
        }
        assertTrue(on > 10 && off > 10, "it flickers: on " + on + ", off " + off);
        double late = 0;
        for (double t = 1.8; t < 2.5; t += 0.01) {
            late += RookLossLooks.lamp(true, false, t, t / 2.5);
        }
        assertTrue(late < sum / 3, "it dies with the spin");
    }

    /** His spin frames turn with his hull: clockwise to the right, back the other way to the left. */
    @Test
    void theSpinFramesTurnWithHisHull() {
        double frame = 2 * Math.PI / 16;
        assertEquals(0, RookLossLooks.spinFrame(0, 16));
        assertEquals(1, RookLossLooks.spinFrame(frame, 16));
        assertEquals(15, RookLossLooks.spinFrame(-frame, 16));
        assertEquals(0, RookLossLooks.spinFrame(2 * Math.PI * 3, 16), "three whole turns");
    }

    /**
     * The cone in empty water (the user on Level 25: "I can't see the cones of the headlights at all"): a faint haze
     * along the beam, strongest near the lamp and gone before the beam's end, never more than a few % of white.
     */
    @Test
    void theHazeIsStrongestNearTheLampAndFaint() {
        assertEquals(0, UnderwaterGrade.hazeAlong(0));
        assertEquals(0, UnderwaterGrade.hazeAlong(1));
        assertTrue(UnderwaterGrade.hazeAlong(0.1) > UnderwaterGrade.hazeAlong(0.4));
        assertTrue(UnderwaterGrade.hazeAlong(0.4) > UnderwaterGrade.hazeAlong(0.8));
        assertTrue(UnderwaterGrade.HAZE * 2 <= 0.15f, "the two beams together stay faint");
        assertTrue(UnderwaterGrade.SNOW_CONE_BOOST > 0, "the snow in the cone is lit more");
    }
}
