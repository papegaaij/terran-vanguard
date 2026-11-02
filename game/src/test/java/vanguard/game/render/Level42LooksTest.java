package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CruiserRun;
import vanguard.sim.CruiserRunSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part H (step H4a), Level 42's scenery: the home fleet's cruisers one flank at a time (cruiser A on the left, B on
 * the right), each overtaking from below the bottom edge in open space (no ice, no hole); the CDF frigate pair on the
 * flank across from cruiser A for its run only, marked friendly, their marks apart and on the field; the dead tender's
 * hull under its strongbox at the pivots' offset.
 */
class Level42LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(42).orElseThrow();

    private static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                60);
    }

    private static int at(double seconds) {
        return SimStep.ticks(seconds);
    }

    @Test
    void theCruisersRunOneFlankAtATimeOvertakingInOpenSpace() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        CruiserRunSpec spec = script.hazards().cruisers().orElseThrow();
        assertTrue(CruiserLooks.perFlank(spec), "Level 42's runs fly one flank each");
        LevelScript level28 = SimSpecs.level(CONTENT, CONTENT.levelKey(28).orElseThrow(), Difficulty.MEDIUM);
        assertFalse(CruiserLooks.perFlank(level28.hazards().cruisers().orElseThrow()), "Level 28's one run of both");
        CruiserRun run = sortie().cruisers().orElseThrow();
        assertTrue(run.present(CruiserRun.LEFT, at(30)) && !run.present(CruiserRun.RIGHT, at(30)), "A alone");
        assertTrue(run.present(CruiserRun.RIGHT, at(48)) && !run.present(CruiserRun.LEFT, at(48)), "B alone");
        double height = spec.hull().height();
        assertEquals(-height / 2, CruiserLooks.overtakeY(PlayField.HEIGHT / 2.0, 0, height), 1e-9, "below the edge");
        assertEquals(PlayField.HEIGHT / 2.0, CruiserLooks.overtakeY(PlayField.HEIGHT / 2.0, 1, height), 1e-9);
        double last = Double.NEGATIVE_INFINITY;
        for (double r = 0; r <= 1; r += 0.1) {
            double y = CruiserLooks.overtakeY(PlayField.HEIGHT / 2.0, r, height);
            assertTrue(y >= last, "it only climbs into its place");
            last = y;
        }
    }

    @Test
    void theFrigatesFlyAcrossFromCruiserAForItsRunMarkedApart() {
        Sortie sortie = sortie();
        CruiserRun run = sortie.cruisers().orElseThrow();
        float[] a = new float[2];
        float[] b = new float[2];
        assertTrue(FrigateLooks.place(run, at(32), 0, a) && FrigateLooks.place(run, at(32), 1, b), "beside A's run");
        assertTrue(a[0] > PlayField.WIDTH / 2.0 && b[0] > PlayField.WIDTH / 2.0, "on the right flank, across from A");
        for (float[] f : new float[][] {a, b}) {
            assertTrue(
                    f[0] + FrigateLooks.HALF_WIDTH + FriendlyMarks.MARGIN <= PlayField.WIDTH, "its mark on the field");
        }
        // The marks (the brackets round each hull) never overlap.
        boolean apartX = Math.abs(a[0] - b[0]) >= 2 * (FrigateLooks.HALF_WIDTH + FriendlyMarks.MARGIN);
        boolean apartY = Math.abs(a[1] - b[1]) >= 2 * (FrigateLooks.HALF_HEIGHT + FriendlyMarks.MARGIN);
        assertTrue(apartX || apartY, "marks apart: " + a[0] + "," + a[1] + " / " + b[0] + "," + b[1]);
        assertFalse(FrigateLooks.place(run, at(20), 0, a), "not before A rises");
        assertFalse(FrigateLooks.place(run, at(48), 0, a), "not beside B");
        assertEquals(
                FrigateLooks.burstAge(10, 0, 0) + 0.3,
                FrigateLooks.burstAge(10.3, 0, 0),
                1e-9,
                "bursts on the level clock");
    }

    @Test
    void theTendersHullSitsUnderItsStrongboxAtThePivotsOffset() throws IOException {
        var pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/debris-home-tender.json")));
        assertEquals(TowLooks.HULL_DX, pivots.get("hull_offset").getInt(0));
        assertEquals(TowLooks.HULL_DY, pivots.get("hull_offset").getInt(1));
        assertEquals("debris-home-tender", TowLooks.LOOKS.get(42));
        assertEquals(0, TowLooks.breathFrame(0, 8));
        assertEquals(1, TowLooks.breathFrame(0.25, 8), "4 frames a second");
        assertEquals(0, TowLooks.breathFrame(2, 8), "round again");
    }
}
