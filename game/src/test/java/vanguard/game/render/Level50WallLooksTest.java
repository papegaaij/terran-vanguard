package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import vanguard.sim.ArenaWalls;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part G, step G6 (H4's leftovers; design/enemies/bosses/choir-heart, Phases 2 and Tells; ART-H's {@code -pit-l/-r},
 * {@code -spit} and {@code -debris}): Level 50's last looks. The wall pits sit where the playbook's brood releases
 * (one on each wall), shut, then swelling and parting over the brood's tell, open while it releases and closing after;
 * the wall spit flashes at each vein point as its pair leaves the face, the veins telling it; and the debris rings'
 * chunks are the art's.
 */
class Level50WallLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(50).orElseThrow();
    private static final String H = HeartLooks.SLUG;

    private static SetPieceSpec heart() {
        return SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM).setPieces().stream()
                .filter(HeartLooks::drawn)
                .findFirst()
                .orElseThrow();
    }

    private static JsonValue pivots() throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + H + ".json")));
    }

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.filter(f -> f.getFileName().toString().matches(name + "_\\d+\\.png"))
                    .count();
        }
    }

    @Test
    void thePitsSpitAndDebrisAreTheArtsAndThePitsAreTheBroodsPlaces() throws IOException {
        assertEquals(HeartLooks.PIT_OPEN + 1, frames(H + "-pit-l"), "shut, swelling, parting, open");
        assertEquals(HeartLooks.PIT_OPEN + 1, frames(H + "-pit-r"));
        assertEquals(6, frames(H + "-spit"), "the spit's building and shot frames");
        assertEquals(8, frames(H + "-debris"), "the tumbling chunk");
        double[][] pits = HeartLooks.wallPits(heart());
        assertEquals(2, pits.length, "a pit on each wall");
        assertArrayEquals(new double[] {-1, 240}, pits[0], "the left wall's at y 300 from the top (240 up)");
        assertArrayEquals(new double[] {1, 200}, pits[1], "the right wall's at y 340 from the top (200 up)");
        assertTrue(pivots().get("pits").getString("note").contains("8 px inside"), "the pivots' rule");
        assertEquals(8, HeartLooks.PIT_INSET, "centred 8 px inside the face (the pivots' note)");
        assertEquals(4, HeartLooks.spitFrame(0), "the shot's first frame");
        assertEquals(5, HeartLooks.spitFrame(HeartLooks.SPIT_TICKS_PER_FRAME));
        assertEquals(0, HeartLooks.spitFrame(5L * HeartLooks.SPIT_TICKS_PER_FRAME), "its glow back down");
        assertEquals(-1, HeartLooks.spitFrame(6L * HeartLooks.SPIT_TICKS_PER_FRAME), "then gone");
    }

    @Test
    void thePitsOpenForTheBroodAndTheSpitFlashesWhereItFires() throws IOException {
        Sortie sortie = BossPilot.sortie(CONTENT, KEY);
        HeartLooks looks = HeartLooks.following(heart(), pivots());
        boolean[] pitFrames = new boolean[HeartLooks.PIT_OPEN + 1];
        int spits = 0;
        boolean told = false;
        boolean closedAfter = false;
        int last = 0;
        int limit = SimStep.ticks(420);
        for (int step = 0; step < limit; step++) {
            sortie.step(BossPilot.commands(sortie, piece -> HeartLooks.drawn(piece.spec())));
            looks.stepped(sortie);
            SetPiece piece = HeartLooks.heart(sortie);
            ArenaWalls walls = sortie.arenaWalls();
            if (piece == null || !piece.present() || walls == null || !walls.standing()) {
                continue;
            }
            int f = looks.pitFrame(piece, sortie.tick());
            pitFrames[f] = true;
            closedAfter |= last == HeartLooks.PIT_OPEN && f < HeartLooks.PIT_OPEN && f > 0;
            last = f;
            spits = Math.max(spits, looks.spitsShowing(sortie.tick()));
            told |= HeartLooks.spitTelling(piece);
            if (pitFrames[HeartLooks.PIT_OPEN] && closedAfter && spits >= 2 && told) {
                break;
            }
        }
        for (int f = 0; f < pitFrames.length; f++) {
            assertTrue(pitFrames[f], "the pits show frame " + f);
        }
        assertTrue(closedAfter, "the pits close after the brood");
        assertTrue(spits >= 2, "the spit flashes on both walls' pairs: " + spits);
        assertTrue(told, "the spit is told");
    }
}
