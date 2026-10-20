package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.LevelScript;

/**
 * Step CSF: a destructible ground object (an HP target, no {@code hits}) is drawn with its look's {@code -glow} frames
 * like a trigger, so Level 20's emergency cache shows its strobe and the strobe's halo ({@link StrobeHalo}); and only
 * the HP targets whose art has a glow gain one.
 */
class DestructibleGlowTest {
    private static final Path SPRITES =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve("sprites");

    /** Whether the sprite files hold frames of that name (a single image or {@code _0}, {@code _1}, ...). */
    private static boolean onDisk(String name) {
        return Files.isRegularFile(SPRITES.resolve(name + ".png"))
                || Files.isRegularFile(SPRITES.resolve(name + "_0.png"));
    }

    private static int frameCount(String name) {
        int n = 0;
        while (Files.isRegularFile(SPRITES.resolve(name + "_" + n + ".png"))) {
            n++;
        }
        return n == 0 && onDisk(name) ? 1 : n;
    }

    @Test
    void theEmergencyCacheIsADestructibleThatDrawsItsStrobeHalo() {
        LevelScript script = RetreatFixture.script(Difficulty.MEDIUM);
        GroundObjectSpec cache = script.groundObjects().stream()
                .filter(spec -> spec.look().equals("emergency-cache"))
                .findFirst()
                .orElseThrow();
        assertFalse(cache.trigger(), "an HP target, not a trigger");
        String glow = GroundPasses.glowName(cache, DestructibleGlowTest::onDisk);
        assertEquals("emergency-cache-glow", glow, "its glow frames are loaded");
        StrobeHalo.Strobe strobe = StrobeHalo.of(cache.look());
        assertNotNull(strobe);
        int glowFrames = frameCount(glow);
        int lookFrames = frameCount(cache.look());
        int litIntact = 0;
        int litHit = 0;
        for (long tick = 0; tick < 120; tick++) {
            if (strobe.strength(GroundGlow.frame(tick, glowFrames, lookFrames, false)) > 0) {
                litIntact++;
            }
            if (strobe.strength(GroundGlow.frame(tick, glowFrames, lookFrames, true)) > 0) {
                litHit++;
            }
        }
        assertTrue(litIntact > 0, "the halo strobes while intact");
        assertTrue(litHit > 0, "and after the first hit");
    }

    @Test
    void onlyTheHpTargetsWithGlowArtGainAGlow() {
        Set<String> glowing = new TreeSet<>();
        for (String key : RetreatFixture.CONTENT.levels().keySet()) {
            for (Difficulty difficulty : Difficulty.values()) {
                for (GroundObjectSpec spec :
                        SimSpecs.level(RetreatFixture.CONTENT, key, difficulty).groundObjects()) {
                    if (!spec.trigger() && GroundPasses.glowName(spec, DestructibleGlowTest::onDisk) != null) {
                        glowing.add(spec.look());
                    }
                }
            }
        }
        // Level 17's seed-vault capsule (its contents' glow), Level 19's payroll safe, Level 20's emergency cache.
        assertEquals(Set.of("emergency-cache", "greenhouse-frame", "payroll-safe"), glowing);
        assertEquals(null, GroundPasses.glowName(cargo(), DestructibleGlowTest::onDisk), "a cargo container has none");
    }

    /** A plain cargo container from Level 01's script. */
    private static GroundObjectSpec cargo() {
        String key = RetreatFixture.CONTENT.levelKey(1).orElseThrow();
        return SimSpecs.level(RetreatFixture.CONTENT, key, Difficulty.MEDIUM).groundObjects().stream()
                .filter(spec -> spec.look().equals(GroundObjectSpec.CARGO_CONTAINER))
                .findFirst()
                .orElseThrow();
    }
}
