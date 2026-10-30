package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * M9 part G, Level 34's looks (step G4; tools/art/belt_units_g.py, tools/art/warden_tank.py's livery): the art's frames
 * as the looks count them (the Warden Tank's Ascendancy livery in the grey tank's layout without a flash of its own, the
 * CDF and Helix containers, the hold clamps, the Helix core's crate, rise, drift and glow, the secrets' shutter, hatch and
 * airlock); the livery from Act 5 only; the core's frames by its clocks; the containers' tag counts all six.
 */
class Level34LooksTest {
    private static final Path SPRITES =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(34).orElseThrow();

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        assertEquals(frames("warden-tank"), frames("warden-tank-asc"), "the livery in the grey tank's layout");
        assertEquals(TankLooks.AIMS * 2, frames("warden-tank-asc-turret"));
        assertEquals(TankLooks.HEADINGS, frames("warden-tank-asc-wreck"));
        assertTrue(Files.isRegularFile(SPRITES.resolve("warden-tank-asc-turret-stub.png")));
        assertEquals(0, frames("warden-tank-asc-flash"), "the muzzle flash stays the grey tank's");
        // Round 67 G.1 c: the livery's ground shadows, a hull's and a wreck's per heading; the grey tank casts none.
        assertEquals(TankLooks.HEADINGS, frames("warden-tank-asc-shadow"));
        assertEquals(TankLooks.HEADINGS, frames("warden-tank-asc-wreck-shadow"));
        assertEquals(0, frames("warden-tank-shadow"), "Level 19's grey tank casts no shadow");
        for (String container : new String[] {"cargo-container-cdf", "cargo-container-helix"}) {
            assertEquals(3, frames(container), container + ": intact, damaged, wrecked");
            assertEquals(8, frames(container + "-break"), container);
        }
        for (String clamp : new String[] {"hold-clamp-left", "hold-clamp-right"}) {
            assertEquals(3, frames(clamp), clamp + ": intact, hit, spent");
            assertEquals(8, frames(clamp + "-break"), clamp);
        }
        assertEquals(2, frames("helix-core"), "the seal's glow");
        assertEquals(StoryLooks.RISE_FRAMES, frames("helix-core-rise"));
        assertEquals(8, frames("helix-core-drift"));
        assertEquals(StoryLooks.RISE_FRAMES + 6, frames("helix-core-glow"), "the rise's ten, the loop's six");
        for (String secret : new String[] {"vigil-shutter", "sable-wren-hatch", "old-marta-airlock"}) {
            assertEquals(3, frames(secret), secret + ": shut, hit, open");
        }
    }

    @Test
    void theWardenTankWearsItsLiveryFromAct5Only() {
        assertEquals(5, EnemyLooks.act(KEY));
        assertEquals(3, EnemyLooks.act(CONTENT.levelKey(19).orElseThrow()));
        assertEquals("warden-tank-asc", EnemyLooks.root(CONTENT, "warden-tank", 5, name -> true));
        assertEquals("warden-tank", EnemyLooks.root(CONTENT, "warden-tank", 3, name -> true), "Level 19's grey");
        assertEquals("warden-tank", EnemyLooks.root(CONTENT, "warden-tank", 5, name -> false), "without its frames");
        assertEquals("talon", EnemyLooks.root(CONTENT, "talon", 5, name -> true), "no livery");
        assertEquals("Jovian Ascendancy", CONTENT.enemy("warden-tank").factionIn(5));
    }

    @Test
    void theCoresFramesFollowItsClocks() {
        int second = SimStep.PER_SECOND;
        assertEquals(0, StoryLooks.shutFrame(0, 2));
        assertEquals(1, StoryLooks.shutFrame(second / 2, 2), "the seal's glow at about 1 Hz");
        assertEquals(0, StoryLooks.shutFrame(second, 2));
        assertEquals(0, StoryLooks.riseFrame(0));
        assertEquals(5, StoryLooks.riseFrame(0.55));
        assertEquals(StoryLooks.RISE_FRAMES - 1, StoryLooks.riseFrame(1), "the last at the rise's end");
        assertEquals(0, StoryLooks.driftFrame(0, 8));
        assertEquals(7, StoryLooks.driftFrame(second * 7 / 8 + 1, 8), "8 fps");
        assertEquals(0, StoryLooks.driftFrame(second, 8));
        assertEquals(10, StoryLooks.glowLoopFrame(0, 16), "the glow's loop from frame 10");
        assertEquals(15, StoryLooks.glowLoopFrame(second * 5 / 6, 16), "at 6 fps");
        assertEquals(10, StoryLooks.glowLoopFrame(second, 16));
        assertEquals(-1, StoryLooks.glowLoopFrame(0, StoryLooks.RISE_FRAMES), "no loop without its frames");
    }

    @Test
    void theContainersTagCountsAllSixAndTheCoreIsTheLevelsStory() {
        LevelData level = CONTENT.level(KEY);
        assertEquals(
                "CONTAINERS",
                level.objectives().secondary().orElseThrow().label().orElseThrow());
        assertEquals("3 / 6", MissionPanel.escapesCount(3, 6, 0), "CONTAINERS n / 6 (the objects of all six)");
        assertFalse(level.pickups().stream().noneMatch(pickup -> pickup.story().isPresent()), "a story pickup");
        assertEquals(
                "helix-core",
                level.pickups().stream()
                        .flatMap(pickup -> pickup.story().stream())
                        .findFirst()
                        .orElseThrow());
    }
}
