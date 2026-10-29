package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.sim.SimStep;

/**
 * M9 part F, Level 33's looks (step F4; tools/art/belt_units_f.py): the art's frames as the looks count them (the Void
 * Leech, the Rail Bunker, Claim 114, the nav buoy), the pivots' mouths and muzzle, the sac's pulse once a second, the
 * prey's slow tumble, the rail's live and fading frames; the nav buoy is a moored buoy and Claim 114 the shack's look.
 */
class Level33LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static JsonValue pivots(String name) {
        return new JsonReader().parse(new FileHandle(new File(ASSETS.toFile(), "pivots/" + name + ".json")));
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        String leech = LeechFieldLooks.LEECH;
        assertEquals(16 * 4, frames(leech), "16 headings x 4 swim frames");
        assertEquals(3 * LampreyLooks.LATCHED_FRAMES, frames(leech + "-latched"));
        assertEquals(LampreyLooks.LATCHED_FRAMES, frames(leech + "-drain"));
        assertEquals(LeechFieldLooks.PREY_HEADINGS * LeechFieldLooks.PULSE_FRAMES, frames(leech + "-prey"));
        for (String set : new String[] {"-stunned", "-death", "-tatters"}) {
            assertEquals(8, frames(leech + set), set);
        }
        String bunker = LeechFieldLooks.BUNKER;
        assertTrue(Files.isRegularFile(SPRITES.resolve(bunker + ".png")), "the base");
        assertTrue(Files.isRegularFile(SPRITES.resolve(bunker + "-wreck.png")), "the wreck");
        assertEquals(LeechFieldLooks.TURRET_HEADINGS, frames(bunker + "-turret"));
        assertEquals(LeechFieldLooks.TURRET_HEADINGS, frames(bunker + "-coils"));
        assertEquals(LeechFieldLooks.RAIL_FRAMES, frames(bunker + "-rail"));
        assertEquals(4, frames(bunker + "-flash"));
        assertEquals(4, frames(LeechFieldLooks.SHACK));
        assertEquals(6, frames(LeechFieldLooks.SHACK + "-open"));
        assertEquals(3, frames("nav-buoy"), "BuoyLooks' shut, opening and open");
    }

    @Test
    void theLeechsMouthsAndTheBunkersMuzzleComeFromThePivots() {
        int[][] mouth = LampreyLooks.mouth(pivots(LeechFieldLooks.LEECH));
        assertArrayEquals(new int[] {-8, -13}, mouth[0], "left, y up");
        assertArrayEquals(new int[] {7, -13}, mouth[1], "right");
        assertArrayEquals(new int[] {0, -21}, mouth[2], "tail");
        assertEquals(null, LampreyLooks.mouth(pivots("lamprey")), "the Lamprey keeps its DRAIN_AT");
        double muzzle = LeechFieldLooks.muzzle(pivots(LeechFieldLooks.BUNKER));
        assertTrue(muzzle > 45 && muzzle < 56, "the rails' tip from the turret's centre: " + muzzle);
    }

    @Test
    void theSacPulsesOnceASecondThePreyTumblesAndTheRailFades() {
        int second = SimStep.PER_SECOND;
        assertEquals(0, LeechFieldLooks.pulseFrame(0, 0));
        assertEquals(1, LeechFieldLooks.pulseFrame(second / 4, 0));
        assertEquals(0, LeechFieldLooks.pulseFrame(second, 0), "one pulse a second");
        assertEquals(3, LampreyLooks.gulpFrame(3 * second / 4, 0, LeechFieldLooks.PULSE_FPS), "on the hull too");
        assertEquals(5, LeechFieldLooks.tumbleHeading(5, 0));
        assertEquals(7, LeechFieldLooks.tumbleHeading(5, second), "two headings a second");
        assertEquals(0, LeechFieldLooks.tumbleHeading(15, second / 2));
        assertEquals(4 * 4 + 2, LeechFieldLooks.preyIndex(4, 2));
        int live = SimStep.ticks(0.2);
        assertEquals(-1, LeechFieldLooks.railFrame(-1, live));
        assertEquals(0, LeechFieldLooks.railFrame(0, live));
        assertEquals(1, LeechFieldLooks.railFrame(LeechFieldLooks.RAIL_FRAME_TICKS, live));
        assertEquals(2, LeechFieldLooks.railFrame(live, live), "fading");
        assertEquals(3, LeechFieldLooks.railFrame(live + LeechFieldLooks.RAIL_FRAME_TICKS, live));
        assertEquals(-1, LeechFieldLooks.railFrame(live + 2 * LeechFieldLooks.RAIL_FRAME_TICKS, live), "gone");
        assertEquals(1, LeechFieldLooks.beaconFrame(second / 4, 4), "the beacon at 4 fps");
    }

    /**
     * FIX3 (H5's finding, Level 35): the lifeboat drawn as Claim 114 (its beacon frames, shot open: its 6 open frames,
     * the last one left in the clamps), its opening over a second; Claim 114's keeps its 0.4 s.
     */
    @Test
    void theLifeboatOpensOverASecondAndStaysOpen() throws IOException {
        String boat = LeechFieldLooks.LIFEBOAT;
        assertEquals(4, frames(boat), "the beacon's blink");
        assertEquals(6, frames(boat + "-open"), "shot open");
        int second = SimStep.PER_SECOND;
        assertEquals(0, LeechFieldLooks.openFrame(0, 6, boat));
        assertEquals(2, LeechFieldLooks.openFrame(second / 3, 6, boat));
        assertEquals(5, LeechFieldLooks.openFrame(second - 1, 6, boat), "the last frame by the second's end");
        assertEquals(5, LeechFieldLooks.openFrame(60 * second, 6, boat), "and it stays");
        assertEquals(5, LeechFieldLooks.openFrame(6 * LeechFieldLooks.OPEN_FRAME_TICKS, 6, LeechFieldLooks.SHACK));
        LevelData level = CONTENT.level(CONTENT.levelKey(35).orElseThrow());
        assertTrue(
                level.groundTargets().stream()
                        .anyMatch(target -> target.sprite().filter(boat::equals).isPresent()),
                "Level 35's secret is the lifeboat");
    }

    @Test
    void theNavBuoyIsMooredAndClaim114IsTheShack() {
        LevelData level = CONTENT.level(CONTENT.levelKey(33).orElseThrow());
        assertTrue(level.released().stream().anyMatch(BuoyLooks::moored), "the armour patch's nav buoy");
        assertTrue(
                level.groundTargets().stream().anyMatch(target -> target.sprite()
                        .filter(LeechFieldLooks.SHACK::equals)
                        .isPresent()),
                "Claim 114");
    }
}
