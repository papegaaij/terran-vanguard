package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.content.WeatherBlock;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.Flock;
import vanguard.sim.PlayField;
import vanguard.sim.Pursuit;
import vanguard.sim.Sortie;

/**
 * M7 part G (design/campaign Level 20; step GB): the pursuit swarm's lunge tell (its members brightening through the
 * tell, cooling along the dive), the rear-guard drone's look on its trail (its gun straight back, frame 8 of 16,
 * swinging a frame as it lags), the rim guns' flak in the top band, and the travelling ridge: surfacing at its key's
 * t=12 (round 43 Q.4), crossing near the top edge ahead of the strike, its body the last 900 px of the head's track laid on
 * the ground, gone by the right edge. The flight needs Level 20 mapped (step G4).
 */
class Level20LooksTest {
    private static final Path SPRITES =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve("sprites");

    @Test
    void theTellBrightensTheBallAndTheDiveCoolsIt() {
        assertEquals(0, PursuitLooks.glow(Pursuit.Phase.BAND, 0.9), "nothing in the band");
        assertEquals(0, PursuitLooks.glow(Pursuit.Phase.TELL, 0), "the tell starts dark");
        float half = PursuitLooks.glow(Pursuit.Phase.TELL, 0.5);
        assertTrue(half > 0 && half < PursuitLooks.TELL_GLOW, "rising: " + half);
        assertEquals(PursuitLooks.TELL_GLOW, PursuitLooks.glow(Pursuit.Phase.TELL, 1), 1e-6, "brightest as it drops");
        assertEquals(PursuitLooks.TELL_GLOW, PursuitLooks.glow(Pursuit.Phase.DIVE, 0), 1e-6);
        assertTrue(PursuitLooks.glow(Pursuit.Phase.DIVE, 0.5) < PursuitLooks.TELL_GLOW, "cooling along the dive");
        assertEquals(0, PursuitLooks.glow(Pursuit.Phase.DIVE, 1), 1e-6);
        assertEquals(0, PursuitLooks.glow(Pursuit.Phase.CLIMB, 0.5));
        assertEquals(0, PursuitLooks.glow(Pursuit.Phase.BREAK, 0.5));
        assertEquals(0, PursuitLooks.glow(null), "a unit without a flock");
    }

    @Test
    void theRearGuardDroneFacesStraightBackAndSwingsAFrameInATurn() {
        assertEquals(8, ArsenalLooks.headingIndex(ArsenalLooks.trailHeading(0, 40), 16), "on its station: frame 8");
        assertEquals(9, ArsenalLooks.headingIndex(ArsenalLooks.trailHeading(-12, 40), 16), "lagging left");
        assertEquals(7, ArsenalLooks.headingIndex(ArsenalLooks.trailHeading(12, 40), 16), "lagging right");
        for (int k = 0; k < 16; k++) {
            assertTrue(
                    Files.isRegularFile(SPRITES.resolve(ArsenalLooks.REAR_DRONE + "_" + k + ".png")),
                    "the rear-guard drone's heading " + k);
        }
    }

    @Test
    void theRimGunsBurstInTheTopBand() {
        LevelData.Flak data = RetreatFixture.level().flak().orElseThrow();
        assertEquals(160, data.from(), 1e-9);
        assertEquals(1.5, data.every(), 1e-9);
        MilitiaFlak flak = new MilitiaFlak(data);
        assertEquals(LevelData.FlakBand.TOP, flak.band());
        for (int n = 0; n < 200; n++) {
            double y = MilitiaFlak.y(n, flak.band());
            assertTrue(y >= MilitiaFlak.TOP_LOW_Y && y <= MilitiaFlak.TOP_HIGH_Y, "y " + y);
            assertTrue(y > PlayField.HEIGHT - 200 && y < PlayField.HEIGHT, "among the pursuers, below the top edge");
        }
        assertEquals(LevelData.FlakBand.LOWER, new MilitiaFlak(160, 1.5).band(), "Level 17's over the lower field");
    }

    @Test
    void theRidgeSurfacesAtItsKeyAndOnlyInLevel20() {
        assertEquals(12, RetreatFixture.level().ridge().orElseThrow().t(), 1e-9, "the ridge key (round 43 Q.4)");
        assertTrue(FoundryFixture.level().ridge().isEmpty(), "Level 19 has no ridge key");
        for (String part : new String[] {"", "-head", "-tail", "-glow"}) {
            for (int k = 0; k < RidgeLooks.HEADINGS; k++) {
                assertTrue(
                        Files.isRegularFile(SPRITES.resolve(RidgeLooks.ROOT + part + "_" + k + ".png")),
                        RidgeLooks.ROOT + part + "_" + k);
            }
        }
        assertTrue(Files.isRegularFile(SPRITES.resolve(RidgeLooks.ROOT + "-dust_0.png")));
    }

    /**
     * The head comes in from the left edge near the top and crosses ahead of the strike; the body is the last 900 px of
     * its track, a point every 16 px with the track's heading (up the strip and right: 9 to 12), and lies on the
     * ground: the scroll moves every point with it, either way.
     */
    @Test
    void theRidgeCrossesNearTheTopEdgeOnItsTrack() {
        LevelData level = RetreatFixture.level();
        RidgeLooks ridge = RidgeLooks.track(level.ridge().orElseThrow(), level.scrollAt(12));
        assertFalse(ridge.shows(11.9), "before its t");
        assertEquals(0, ridge.layOut(12, level.scrollAt(12)));
        assertTrue(ridge.headX(0) < 0, "surfacing off the left edge");
        for (double t = 13; t <= 19; t += 0.5) {
            int points = ridge.layOut(t, level.scrollAt(t));
            assertTrue(points >= 1 && points <= RidgeLooks.MAX_POINTS, t + ": " + points);
            assertTrue(ridge.y(0) > PlayField.HEIGHT * 0.6 && ridge.y(0) < PlayField.HEIGHT, t + ": near the top edge");
            int heading = ridge.headingAt(0);
            assertTrue(heading >= 9 && heading <= 12, t + ": up the strip and right, " + heading);
            for (int i = 1; i < points; i++) {
                double gap = Math.hypot(ridge.x(i) - ridge.x(i - 1), ridge.y(i) - ridge.y(i - 1));
                assertEquals(RidgeLooks.SPACING, gap, 2, t + ": the lengths every 16 px");
            }
        }
        assertEquals(RidgeLooks.MAX_POINTS, ridge.layOut(19.5, level.scrollAt(19.5)), "its whole length by then");
        assertTrue(ridge.headX(8) > PlayField.WIDTH, "the head off the right edge by the turnabout");
        double scroll = level.scrollAt(16);
        ridge.layOut(16, scroll);
        int laid = ridge.layOut(16, scroll);
        double headY = ridge.y(0);
        double tailY = ridge.y(laid - 1);
        ridge.layOut(16, scroll + 100);
        assertEquals(headY - 100, ridge.y(0), 1e-9, "on the ground: down with the scroll");
        assertEquals(tailY - 100, ridge.y(laid - 1), 1e-9);
        ridge.layOut(16, scroll - 100);
        assertEquals(headY + 100, ridge.y(0), 1e-9, "and up with it reversed");
        double end = ridge.end();
        assertEquals(27, end, 1e-9, "the head's 8 s and the tail's 7 s");
        int points = ridge.layOut(end, level.scrollAt(end));
        for (int i = 0; i < points; i++) {
            assertTrue(ridge.x(i) > PlayField.WIDTH + 40, "the tail past the right edge at the end: " + ridge.x(i));
        }
        assertFalse(ridge.shows(end));
    }

    /**
     * Round 43 Q.4: Level 20's key keeps the approved track (the review GIF's path: {@code -40 + 70u + 30 sin(u / 1.3)}
     * across, {@code 470 + 124u - 60 sin(u / 1.6)} up the strip); the same pass from the right edge (Level 21's) is its
     * mirror, and a deeper one runs lower by the difference.
     */
    @Test
    void theKeyKeepsLevel20sTrackAndMirrorsFromTheRight() {
        RidgeLooks level20 = RidgeLooks.track(RetreatFixture.level().ridge().orElseThrow(), 0);
        RidgeLooks mirrored = RidgeLooks.track(
                new LevelData.Ridge(12, 8, WeatherBlock.RidgeEdge.RIGHT, WeatherBlock.RidgeEdge.LEFT, 170), 0);
        for (double u = 0; u <= 15; u += 0.25) {
            assertEquals(-40 + 70 * u + 30 * Math.sin(u / 1.3), level20.headX(u), 1e-9, "x at " + u);
            assertEquals(470 + 124 * u - 60 * Math.sin(u / 1.6), level20.headRow(u), 1e-9, "row at " + u);
            assertEquals(PlayField.WIDTH - level20.headX(u), mirrored.headX(u), 1e-9);
            assertEquals(level20.headRow(u) - 100, mirrored.headRow(u), 1e-9);
            int heading = level20.heading(u);
            assertEquals(
                    Math.floorMod(RidgeLooks.HEADINGS - heading, RidgeLooks.HEADINGS), mirrored.heading(u), u + "");
        }
    }

    @Test
    void theRidgesGlowPulses() {
        float low = 1;
        float high = 0;
        for (double t = 12; t < 14; t += 0.01) {
            low = Math.min(low, RidgeLooks.pulse(t));
            high = Math.max(high, RidgeLooks.pulse(t));
        }
        assertEquals(RidgeLooks.PULSE_LOW, low, 0.01);
        assertEquals(RidgeLooks.PULSE_HIGH, high, 0.01);
    }

    /** Flown at medium without firing: the first pursuit's members brighten in its first tell (needs step G4). */
    @Test
    void aPursuitsMembersBrightenInItsTell() {
        Sortie sortie = RetreatFixture.sortie();
        boolean told = false;
        while (!told && sortie.levelSeconds() < 40) {
            sortie.step(Command.NONE);
            Flock flock = RetreatFixture.pursuing(sortie);
            if (flock == null) {
                continue;
            }
            Pursuit pursuit = flock.pursuit().orElseThrow();
            if (pursuit.phase() == Pursuit.Phase.TELL && pursuit.progress() > 0.5) {
                told = true;
                Enemy member = null;
                for (int i = 0; i < flock.size() && member == null; i++) {
                    member = flock.member(i);
                }
                assertNotNull(member, "the flock has motes");
                assertTrue(PursuitLooks.glow(member.flock()) > 0, "brightening");
            }
        }
        assertTrue(told, "a tell by t=40");
    }
}
