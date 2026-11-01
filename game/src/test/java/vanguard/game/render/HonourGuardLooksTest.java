package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part F (step F4): the Honour Guard's looks ({@link HonourGuardLooks}) and Level 40's other looks: the aces' sprite
 * sets and pivots in the assets, the heading and wreck frames, the sniper line's lock, the segmented bar's layout
 * ({@link BossBar#segment}: three segments by HP share, 2 px apart, each wide enough for its call sign), Sentry Two's
 * recorder as the level's tow look and the marked supply drones' brackets.
 */
class HonourGuardLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(40).orElseThrow();
    private static final List<String> ACES = List.of("harker", "vesper", "kestrel");

    @Test
    void theAcesSetsAndPivotsAreInTheAssets() throws Exception {
        for (String ace : ACES) {
            for (int k = 0; k < HonourGuardLooks.HEADINGS; k++) {
                assertTrue(sprite("honour-guard-" + ace + "_" + k), ace + " heading " + k);
            }
            for (int f = 0; f < HonourGuardLooks.WRECK_FRAMES; f++) {
                assertTrue(sprite("honour-guard-" + ace + "-wreck_" + f), ace + " wreck " + f);
            }
            assertTrue(sprite("honour-guard-callsign-" + ace), ace + "'s call sign");
        }
        for (String mark : List.of("harker-crest", "vesper-ribbons", "kestrel-band")) {
            assertTrue(sprite("honour-guard-" + mark + "_0"), mark);
        }
        for (String edge : List.of("top", "bottom", "left", "right")) {
            assertTrue(sprite("honour-guard-chevron-" + edge), edge + " chevron");
            assertTrue(sprite("honour-guard-chevron-glow-" + edge), edge + " chevron glow");
        }
        for (String fx : List.of("barrel-glint", "engine-flare", "wingtip", "cockpit-fire", "edge-glint")) {
            assertTrue(sprite("honour-guard-" + fx + "_0"), fx);
        }
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/honour-guard.json")));
        assertEquals(HonourGuardLooks.HEADINGS, pivots.getInt("headings"));
        for (String ace : ACES) {
            JsonValue headings = pivots.get("aces").get(ace);
            assertEquals(HonourGuardLooks.HEADINGS, headings.size, ace);
            assertNotNull(headings.get(0).get("engines"), ace + "'s engines");
            assertNotNull(headings.get(0).get("wingtips"), ace + "'s wingtips");
        }
        assertNotNull(pivots.get("aces").get("harker").get(0).get("barrel"), "Harker's barrel: the line's origin");
        assertEquals(HonourGuardLooks.WRECK_FRAMES, pivots.get("wreck").size);
        JsonValue chain = pivots.get("chain");
        JsonValue last = chain.get(chain.size - 1);
        assertEquals(2.0, last.getDouble("t"), 1e-9, "the last ace's chain lasts 2 s");
        assertEquals("large", last.getString("size"), "it ends large");
    }

    @Test
    void theHeadingsAndTheWreckTurnAsTheArtIsRendered() {
        assertEquals(0, HonourGuardLooks.headingFrame(0));
        assertEquals(32, HonourGuardLooks.headingFrame(Math.PI), "facing up");
        assertEquals(16, HonourGuardLooks.headingFrame(Math.PI / 2), "a quarter turn clockwise");
        assertEquals(0, HonourGuardLooks.headingFrame(-1e-3));
        assertEquals(63, HonourGuardLooks.headingFrame(-2 * Math.PI / 64));
        assertEquals(5, HonourGuardLooks.wreckFrame(5, 1, 0));
        assertEquals(6, HonourGuardLooks.wreckFrame(5, 1, 1 / HonourGuardLooks.WRECK_FPS));
        assertEquals(4, HonourGuardLooks.wreckFrame(5, -1, 1 / HonourGuardLooks.WRECK_FPS));
        assertEquals(5, HonourGuardLooks.wreckFrame(5, 1, HonourGuardLooks.WRECK_FRAMES / HonourGuardLooks.WRECK_FPS));
    }

    /** The sniper line tracks for its telegraph's first 0.8 s and is locked for the last 0.2 s (the data's lock_before). */
    @Test
    void theSniperLineLocksBeforeItFires() {
        SetPieceSpec boss = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
        assertTrue(boss.boss().flatMap(BossSpec::squadron).isPresent(), "the squadron");
        EnemySpec.LaserLine line = boss.boss().orElseThrow().attacks().stream()
                .filter(attack -> attack.name().equals("sniper-line"))
                .findFirst()
                .orElseThrow()
                .line()
                .orElseThrow();
        assertTrue(line.tracks());
        assertEquals(0.8, HonourGuardLooks.lockShare(line), 1e-9);
    }

    /**
     * The segmented bar (design/ui/hud, Act 6 additions): three segments in the mid-boss bar, each as wide as its ace's
     * share of the HP, 2 px apart, filling the bar; each wide enough for its call sign (the widest, KESTREL, 56 px).
     */
    @Test
    void theSegmentsShareTheMidBarAndTheirTagsFit() {
        float[] out = new float[2];
        double[] equal = {1900, 1900, 1900};
        float fillX = (vanguard.sim.PlayField.WIDTH - BossBar.MID_WIDTH) / 2;
        float end = fillX;
        float total = 0;
        for (int s = 0; s < 3; s++) {
            BossBar.segment(fillX, BossBar.MID_WIDTH, equal, 3, s, out);
            if (s > 0) {
                assertEquals(end + BossBar.SEGMENT_GAP, out[0], 1e-6, "2 px after the one before");
            } else {
                assertEquals(fillX, out[0], 1e-6);
            }
            assertTrue(out[1] >= 56, "segment " + s + " holds the call sign: " + out[1]);
            end = out[0] + out[1];
            total += out[1];
        }
        assertEquals(fillX + BossBar.MID_WIDTH, end, 1, "the segments fill the bar");
        assertEquals(BossBar.MID_WIDTH - 2 * BossBar.SEGMENT_GAP, total, 1);
        double[] uneven = {2000, 1000, 1000};
        BossBar.segment(fillX, BossBar.MID_WIDTH, uneven, 3, 0, out);
        float first = out[1];
        BossBar.segment(fillX, BossBar.MID_WIDTH, uneven, 3, 1, out);
        assertEquals(2 * out[1], first, 1.5, "by HP share");
    }

    /** Level 40's tow is Sentry Two's recorder on its dead scout, its beacon lit 0.16 s once a second. */
    @Test
    void theRecorderIsTheLevelsTowLook() {
        assertEquals("debris-sentry-recorder", TowLooks.LOOKS.get(40));
        assertEquals(java.util.Optional.of("debris-sentry-recorder"), LevelRenderer.towLook(40));
        for (String set :
                List.of("debris-sentry-recorder", "debris-sentry-recorder-glow", "debris-sentry-recorder-scout")) {
            for (int f = 0; f < 16; f++) {
                assertTrue(sprite(set + "_" + f), set + " " + f);
            }
        }
        assertTrue(TowLooks.beaconLit(3.1));
        assertFalse(TowLooks.beaconLit(3.5));
    }

    /**
     * The CDF supply drones carrying the two armour patches are marked friendly (design/campaign Level 40): bracketed
     * where {@link DroneLooks} draws them, through their flight on the screen.
     */
    @Test
    void theSupplyDronesAreMarkedFriendly() {
        LevelData level = CONTENT.level(KEY);
        assertEquals(
                2, level.released().stream().filter(LevelData.Drop::isMarked).count(), "two marked drones");
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FriendlyMarks marks = new FriendlyMarks(null, null).drones(level.released());
        var drop = level.released().stream()
                .filter(LevelData.Drop::isMarked)
                .findFirst()
                .orElseThrow();
        float[] at = new float[2];
        int marked = 0;
        long limit = SimStep.ticks(drop.t() + 3);
        while (!sortie.complete() && sortie.tick() < limit) {
            sortie.step(0);
            marks.plan(sortie, 1);
            if (DroneLooks.position(
                            sortie.levelSeconds() - drop.t(),
                            0,
                            (float) drop.x(),
                            -DroneLooks.BELOW_EDGE,
                            drop.x(),
                            drop.y(),
                            at)
                    && at[1] > 0) {
                boolean found = false;
                for (int m = 0; m < marks.count(); m++) {
                    found |= Math.abs(marks.x(m) - at[0]) <= 1 && Math.abs(marks.y(m) - at[1]) <= 1;
                }
                assertTrue(found, "the drone marked at t=" + sortie.levelSeconds());
                marked++;
            }
        }
        assertTrue(marked > SimStep.ticks(3), "marked through its flight: " + marked);
    }

    private static boolean sprite(String name) {
        return Files.isRegularFile(ASSETS.resolve("sprites/" + name + ".png"));
    }
}
