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
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.BreachArena;
import vanguard.sim.BreachArena.Phase;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WalkerHooks;

/**
 * M7 part H (design/campaign Level 21; step HA): the Dust Colossus's pose and frame choice (the rise's three steps, half
 * open then open, the bite's tell, the snap, the dive's two steps, the tells' and the funnel's frames, the death throe),
 * its body laid along a breach's curve a length every 16 px toward its head, the section after the crawling arena
 * starting at the next 512 px seam with the seam piece off the screen (the user's round 43 answer H.6), the rocks'
 * frames and the ridge pass's light shake within the setting.
 */
class Level21LooksTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_21 = "act-3-red-dust/level-21-dust-colossus";
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    @Test
    void theMouthRisesInThreeStepsOpensBitesSnapsAndDives() {
        double rise = 1.5;
        double dive = 1.5;
        assertEquals(0, ColossusLooks.shutStep(Phase.RISE, 0.1, rise, dive));
        assertEquals(1, ColossusLooks.shutStep(Phase.RISE, 0.6, rise, dive));
        assertEquals(2, ColossusLooks.shutStep(Phase.RISE, 1.2, rise, dive), "risen shut at the rise's end");
        assertEquals(2, ColossusLooks.shutStep(Phase.RISE, 1.5, rise, dive));
        assertEquals(3, ColossusLooks.shutStep(Phase.DIVE, 0.1, rise, dive), "the snap at the dive's start");
        assertEquals(4, ColossusLooks.shutStep(Phase.DIVE, 0.5, rise, dive));
        assertEquals(5, ColossusLooks.shutStep(Phase.DIVE, 1.2, rise, dive), "the tips last");
        assertEquals(-1, ColossusLooks.shutStep(Phase.OPEN, 1, rise, dive));
        assertEquals(0, ColossusLooks.openStep(Phase.OPEN, 0.1), "half open first");
        assertEquals(1, ColossusLooks.openStep(Phase.OPEN, 0.5), "then open, the gullet lit");
        assertEquals(2, ColossusLooks.openStep(Phase.BITE, 0.1), "the bite's tell, the teeth lit");
        for (Phase phase : List.of(Phase.BURIED, Phase.TELL, Phase.RISE, Phase.DIVE)) {
            assertEquals(-1, ColossusLooks.openStep(phase, 0.5), phase + "");
        }
    }

    @Test
    void everyPoseHasItsMouthFramesAndTheTellsTheirFour() {
        int poses = ColossusLooks.POSES;
        assertFrames("dust-colossus-shut", poses * ColossusLooks.SHUT_STEPS);
        assertFrames("dust-colossus-mouth", poses * ColossusLooks.OPEN_STEPS);
        assertFrames("dust-colossus-gullet", poses);
        assertFrames("dust-colossus-bulge", ColossusLooks.TELL_FRAMES);
        assertFrames("dust-colossus-marker", ColossusLooks.TELL_FRAMES);
        assertFrames("dust-colossus-funnel", ColossusLooks.FUNNEL_CRATER + 1);
        assertFrames("dust-colossus-death", 6);
        assertFrames("dust-colossus-burst", 8);
        assertFrames("dust-colossus-vent", 4);
        // Round 44 K.b (step GK): the back's segments and the death throe's sets.
        assertFrames("dust-colossus-spine", ColossusSpine.SEGMENTS * ColossusSpine.STATES * RidgeLooks.HEADINGS);
        assertFrames("dust-colossus-rear", 4);
        assertFrames("dust-colossus-collapse", 3);
        assertFrames("dust-colossus-geyser", ColossusThroe.GEYSER_FRAMES);
        assertFrames("dust-colossus-fissure", ColossusThroe.FISSURE_STARTS.length);
        assertFrames("dust-colossus-plume", ColossusThroe.PLUME_STARTS.length);
        assertFrames("dust-colossus-debris", ColossusThroe.ROCK_SHAPES * ColossusThroe.ROCK_TUMBLE);
        assertFrames("dust-colossus-rock", ColossusLooks.ROCK_SHAPES * ColossusLooks.ROCK_TUMBLE);
        Path level = ASSETS.resolve("backdrop/level-21");
        assertTrue(Files.isRegularFile(level.resolve(ColossusLooks.SINKHOLE + ".png")));
        assertTrue(Files.isRegularFile(level.resolve(BackdropTiles.SEAM_SETTLE + ".png")));
        for (int k = 0; k < RidgeLooks.HEADINGS; k++) {
            assertTrue(Files.isRegularFile(level.resolve(ColossusLooks.FURROW + "_" + k + ".png")), "furrow " + k);
        }
    }

    private static void assertFrames(String root, int count) {
        for (int i = 0; i < count; i++) {
            assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/" + root + "_" + i + ".png")), root + "_" + i);
        }
    }

    @Test
    void theTellsAndTheFunnelStepThroughTheirFrames() {
        assertEquals(0, ColossusLooks.tellFrame(0));
        assertEquals(1, ColossusLooks.tellFrame(0.3));
        assertEquals(3, ColossusLooks.tellFrame(0.99));
        assertEquals(3, ColossusLooks.tellFrame(1), "the last frame at its end");
        assertEquals(0, ColossusLooks.funnelFrame(Phase.RISE, 3), "collapsing at the rise");
        List<Integer> loop = new ArrayList<>();
        for (double t = 0; t < 1; t += 1 / ColossusLooks.FUNNEL_FPS) {
            loop.add(ColossusLooks.funnelFrame(Phase.OPEN, t + 0.01));
        }
        assertTrue(loop.stream().allMatch(f -> f >= 1 && f <= 3) && loop.containsAll(List.of(1, 2, 3)), "" + loop);
        assertEquals(4, ColossusLooks.funnelFrame(Phase.DIVE, 0), "refilling in the dive");
        assertEquals(-1, ColossusLooks.funnelFrame(Phase.BURIED, 0), "the crater is the ground's after the dive");
    }

    /** Step GK: the death throe runs past the boss's 3 s chain (the simulation's, unchanged) to the credit shower. */
    @Test
    void theThroeOutlastsTheBosssChain() {
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_21, Difficulty.MEDIUM);
        double chain = script.setPieces().stream()
                .filter(piece -> piece.isBoss())
                .findFirst()
                .orElseThrow()
                .boss()
                .orElseThrow()
                .deathSeconds();
        assertEquals(3, chain, 1e-9, "the simulation's chain stays 3 s");
        assertTrue(ColossusThroe.THROE_SECONDS > chain, "the throe runs on past it");
    }

    @Test
    void theBodyLiesAlongTheCurveALengthEvery16PxTowardItsHead() {
        int n = 17;
        double[] cx = new double[n];
        double[] cy = new double[n];
        for (int i = 0; i < n; i++) {
            cx[i] = 40 + 20 * i; // left to right, 320 px
            cy[i] = 300;
        }
        double[] xs = new double[RidgeLooks.MAX_POINTS];
        double[] ys = new double[RidgeLooks.MAX_POINTS];
        int[] headings = new int[RidgeLooks.MAX_POINTS];
        assertEquals(0, ColossusLooks.layOut(cx, cy, n, 0, 0, xs, ys, headings), "nothing before the head starts");
        int points = ColossusLooks.layOut(cx, cy, n, 0.5, 0, xs, ys, headings);
        assertEquals(11, points, "160 px of body: the head and a length every 16 px");
        assertEquals(200, xs[0], 1e-9, "the head halfway");
        for (int i = 1; i < points; i++) {
            assertEquals(RidgeLooks.SPACING, xs[i - 1] - xs[i], 1e-9);
            assertEquals(12, headings[i], "heading right (k × 22.5° clockwise from down)");
        }
        int trimmed = ColossusLooks.layOut(cx, cy, n, 1, 18.3, xs, ys, headings);
        assertEquals(360 - 18.3, xs[0], 1e-9, "behind a risen mouth it ends at the neck");
        assertEquals(19, trimmed);
        for (int i = 0; i < n; i++) {
            cy[i] = 540 - 30 * i; // straight down the field, 480 px
            cx[i] = 240;
        }
        ColossusLooks.layOut(cx, cy, n, 1, 0, xs, ys, headings);
        assertEquals(0, headings[0], "heading down");
    }

    @Test
    void theSectionAfterTheCrawlStartsAtTheNext512PxSeamOffTheScreen() {
        LevelData level = CONTENT.level(LEVEL_21);
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_21, Difficulty.MEDIUM);
        int arena = LevelRenderer.crawlingArena(script);
        assertEquals(4, arena, "section 5 crawls");
        int tile = level.backdrop().tileSets().get("basin-floor").height();
        assertEquals(512, tile);
        assertEquals(512, level.backdrop().tileSets().get("settled-floor").height(), "one pattern");
        int half = 320 / 2;
        for (long ground = 18_000; ground < 22_000; ground += 37) {
            long seam = BackdropTiles.crawlSeam(ground, tile, half);
            long top = ground + vanguard.sim.PlayField.HEIGHT;
            assertEquals(0, Math.floorMod(seam, (long) tile), "a multiple of 512: " + seam);
            assertTrue(seam - half >= top, ground + ": seam-settle's bottom edge at or above the top edge");
            assertTrue(seam - half - top < tile, ground + ": the next such seam (" + (seam - top) + " px)");
        }
        assertEquals(1024, BackdropTiles.crawlSeam(1024 - 540, tile, 0), "a seam at the top edge itself");
    }

    @Test
    void theRocksTumbleThroughTheirShapes() {
        assertEquals(0, ColossusLooks.rockFrame(0, 0));
        assertEquals(ColossusLooks.ROCK_TUMBLE + 1, ColossusLooks.rockFrame(1, 0), "shape by serial, its own phase");
        assertEquals(1, ColossusLooks.rockFrame(0, ColossusLooks.ROCK_TUMBLE_TICKS), "the next tumble step");
        for (int serial = 0; serial < 9; serial++) {
            for (long tick = 0; tick < 40; tick += 3) {
                int frame = ColossusLooks.rockFrame(serial, tick);
                assertTrue(frame >= 0 && frame < ColossusLooks.ROCK_SHAPES * ColossusLooks.ROCK_TUMBLE);
            }
        }
    }

    @Test
    void theSuctionStreaksRunInwardAndFadeAtBothEnds() {
        double span = 300;
        double before = ColossusLooks.streakDistance(3, 1.0, span);
        double after = ColossusLooks.streakDistance(3, 1.05, span);
        assertTrue(after < before || before - after < -span / 2, "inward (or wrapped to the outer end)");
        for (double t = 0; t < 3; t += 0.01) {
            float alpha = ColossusLooks.streakAlpha(5, t, span);
            assertTrue(alpha >= 0 && alpha <= 1);
            double d = ColossusLooks.streakDistance(5, t, span);
            if (d < 2 || d > span - 2) {
                assertTrue(alpha < 0.05f, "faded at the ends: " + d + " " + alpha);
            }
        }
    }

    @Test
    void theRidgePassShakesLightlyWithinTheSetting() {
        LevelData level = CONTENT.level(LEVEL_21);
        LevelData.Ridge pass = level.ridge().orElseThrow();
        RidgeLooks ridge = RidgeLooks.track(pass, level.scrollAt(pass.t()));
        assertEquals(0, ridge.shake(pass.t() - 1), "not before the pass");
        assertEquals(1, ridge.shake(pass.t() + pass.seconds() / 2), 1e-9, "full while the head crosses");
        assertEquals(0, ridge.shake(ridge.end() + 0.1), "not after it");
        double most = 0;
        for (double t = 0; t < 2; t += 0.003) {
            most = Math.max(most, Math.abs(ScreenShake.ridge(1, t)));
        }
        assertTrue(most <= ScreenShake.RIDGE_PX + 1e-9 && most > ScreenShake.RIDGE_PX / 2, "light: " + most);
        assertEquals(0, ScreenShake.scaled(ScreenShake.ridge(1, 0.02), 0), "none with the shake off");
        assertEquals(0, ScreenShake.ridge(0, 0.02), "none outside the pass");
    }

    /**
     * Step GFX3 (the H5 capture: the mouth vanished at the kill, no death chain, no sinkhole or furrow): the kill takes
     * the Colossus off the field at once (not present), and its looks keep its death through the chain, laid at the
     * mouth's place and along the body behind it, and keep the furrow and the collapsed crater after it, scrolling with
     * the ground.
     */
    @Test
    void theDeathPlaysThroughTheChainAndLeavesTheSinkholeAndTheFurrow() throws IOException {
        Sortie sortie = ColossusFixture.sortie();
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/dust-colossus.json")));
        ColossusLooks looks =
                ColossusLooks.following(ColossusFixture.level().setPieces().get(0), pivots);
        SetPiece piece = ColossusFixture.colossus(sortie);
        BreachArena breach = piece.breach().orElseThrow();
        for (int t = 0; t < SimStep.ticks(40) && breach.phase() != Phase.OPEN; t++) {
            sortie.step(Command.NONE);
            looks.stepped(sortie);
        }
        assertEquals(Phase.OPEN, breach.phase(), "a risen mouth");
        for (int t = 0; t < 30; t++) {
            sortie.step(Command.NONE);
            looks.stepped(sortie);
        }
        double mouthX = breach.mouthX();
        double mouthRow = breach.mouthY() + sortie.groundScroll();
        assertFalse(looks.drawsDeath(sortie), "alive");
        WalkerHooks.destroy(sortie, 0, ColossusFixture.MOUTH);
        sortie.step(Command.NONE);
        looks.stepped(sortie);
        assertTrue(piece.destroyed());
        assertFalse(piece.present(), "the kill takes it off the field: the looks must not stop at present()");
        assertTrue(looks.drawsDeath(sortie), "its death drawn from the kill on");
        assertEquals(mouthX, looks.deathX(), 2, "the crater at the mouth's place");
        assertEquals(mouthRow, looks.deathRow(), 4, "on the ground's row");
        assertTrue(looks.furrowPoints() > 4, "the furrow along the body behind the mouth: " + looks.furrowPoints());
        for (int i = 0; i < looks.furrowPoints(); i++) {
            double dy = looks.furrowRow(i) - mouthRow;
            assertTrue(Math.hypot(looks.furrowX(i) - mouthX, dy) <= RidgeLooks.LENGTH + 40, "near the mouth " + i);
        }
        List<Integer> frames = new ArrayList<>();
        boolean settledEarly = false;
        assertEquals(ColossusThroe.THROE_SECONDS, looks.deathSeconds(), 1e-9, "step GK: the 4.5 s throe");
        while (looks.deathAge(sortie.tick(), 0) < looks.deathSeconds() + 1) {
            double age = looks.deathAge(sortie.tick(), 0);
            int frame = ColossusThroe.bodyFrame(age);
            if (frame >= 0 && (frames.isEmpty() || frames.getLast() != frame)) {
                frames.add(frame);
            }
            settledEarly |= age < ColossusThroe.SETTLE_START && looks.settled(age) > 0;
            assertTrue(looks.drawsDeath(sortie), "still drawn " + age + " s after the kill");
            sortie.step(Command.NONE);
            looks.stepped(sortie);
        }
        assertEquals(ColossusThroe.BODY_FRAMES.length, frames.size(), "the whole throe: " + frames);
        assertFalse(settledEarly, "the crater and the furrow fade in only as the body has sunk");
        assertEquals(1, looks.settled(looks.deathAge(sortie.tick(), 0)), 1e-6, "settled after the chain");
        double row = looks.deathRow();
        double before = sortie.groundScroll();
        for (int t = 0; t < SimStep.ticks(5); t++) {
            sortie.step(Command.NONE);
            looks.stepped(sortie);
        }
        assertTrue(looks.drawsDeath(sortie), "the crater stays");
        assertEquals(row, looks.deathRow(), 1e-9, "fixed on the ground");
        assertTrue(sortie.groundScroll() > before, "the ground scrolls on, and the crater with it");
    }

    /** Step GFX3: a tell's crack glow grows over the tell and pulses, never out. */
    @Test
    void theTellsCrackGlowGrowsAndPulses() {
        float least = 1;
        float most = 0;
        for (double t = 0; t < 1; t += 0.004) {
            float early = ColossusLooks.tellGlow(0.05, t);
            float late = ColossusLooks.tellGlow(0.95, t);
            least = Math.min(least, early);
            most = Math.max(most, late);
            assertTrue(early > 0.2f && early <= 1 && late <= 1, t + ": " + early + " " + late);
        }
        assertTrue(most > 0.95f, "full near the end: " + most);
        assertTrue(least < ColossusLooks.GLOW_LEAST, "pulsing: " + least);
    }
}
