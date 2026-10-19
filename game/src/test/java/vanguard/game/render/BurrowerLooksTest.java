package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.EnemySpec;
import vanguard.sim.Range;

/** M7 part B: the Burrower's looks follow tools/art/burrower.py's timeline, and the Dust Kite's entry fade. */
class BurrowerLooksTest {
    private static final Path SPRITES = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "sprites");

    private static EnemySpec.Burrow burrow(double surfaced, Optional<EnemySpec.BurrowRing> second) {
        return new EnemySpec.Burrow(
                90,
                new EnemySpec.SurfaceSpot(140, 240, 110, 40, 110),
                new Range(1.0, 1.8),
                1.0,
                surfaced,
                0.4,
                3,
                new EnemySpec.BurrowRing(10, 150, 10, 0.4),
                second,
                0.8,
                2,
                30);
    }

    /** The docstring: 0.30 s half, 0.35 s open, 0.40–0.50 s firing, open, 1.20 s half, 1.30 s shut. */
    @Test
    void theMawOpensForTheRingAndShutsAgain() {
        EnemySpec.Burrow medium = burrow(2.0, Optional.empty());
        assertEquals(BurrowerLooks.SHUT, BurrowerLooks.maw(0.1, medium));
        assertEquals(BurrowerLooks.HALF, BurrowerLooks.maw(0.32, medium));
        assertEquals(BurrowerLooks.OPEN, BurrowerLooks.maw(0.37, medium));
        assertEquals(BurrowerLooks.FIRING, BurrowerLooks.maw(0.45, medium));
        assertEquals(BurrowerLooks.OPEN, BurrowerLooks.maw(1.0, medium));
        assertEquals(BurrowerLooks.HALF, BurrowerLooks.maw(1.25, medium));
        assertEquals(BurrowerLooks.SHUT, BurrowerLooks.maw(1.9, medium));
    }

    /** Hard: open again 1.30–1.40 s, firing 1.40–1.50 s, half from 1.70 s. */
    @Test
    void theHardSecondRingOpensItAgain() {
        EnemySpec.Burrow hard = burrow(1.8, Optional.of(new EnemySpec.BurrowRing(8, 150, 10, 1.4)));
        assertEquals(BurrowerLooks.FIRING, BurrowerLooks.maw(0.45, hard));
        assertEquals(BurrowerLooks.HALF, BurrowerLooks.maw(1.25, hard));
        assertEquals(BurrowerLooks.OPEN, BurrowerLooks.maw(1.35, hard));
        assertEquals(BurrowerLooks.FIRING, BurrowerLooks.maw(1.45, hard));
        assertEquals(BurrowerLooks.OPEN, BurrowerLooks.maw(1.6, hard));
        assertEquals(BurrowerLooks.HALF, BurrowerLooks.maw(1.75, hard));
    }

    /** Heading k travels k x 22.5 degrees clockwise from straight down: 4 left, 8 up, 12 right (y up). */
    @Test
    void theRippleFacesItsGroundRelativeTravel() {
        assertEquals(0, BurrowerLooks.heading(0, -2, 1));
        assertEquals(4, BurrowerLooks.heading(-2, 0, 0));
        assertEquals(8, BurrowerLooks.heading(0, 2, 0));
        assertEquals(12, BurrowerLooks.heading(2, 0, 0));
        assertEquals(2, BurrowerLooks.heading(-1, -1, 0));
        assertEquals(8, BurrowerLooks.heading(0, 0, Math.PI), "still on the ground: its facing");
    }

    @Test
    void theBulgeRunsThroughItsFramesOverItsTell() {
        assertEquals(0, BurrowerLooks.frame(0, 10));
        assertEquals(5, BurrowerLooks.frame(0.55, 10));
        assertEquals(9, BurrowerLooks.frame(1, 10));
    }

    @Test
    void theKiteFadesInOverAThirdOfASecond() {
        assertEquals(0, KiteFade.opacity(0), 1e-6);
        assertEquals(0.5, KiteFade.opacity(0.15), 1e-6);
        assertEquals(1, KiteFade.opacity(0.3), 1e-6);
        assertEquals(1, KiteFade.opacity(5), 1e-6);
    }

    /** tools/art/burrower.py renders every set the looks need. */
    @Test
    void everyBurrowerSetIsRendered() {
        for (String part : new String[] {"", "-glow", "-bow", "-ripple", "-bulge", "-breach", "-dive", "-hole"}) {
            assertTrue(Files.isRegularFile(SPRITES.resolve("burrower" + part + "_0.png")), "burrower" + part);
        }
        assertTrue(Files.isRegularFile(SPRITES.resolve("burrower-ripple_63.png")), "16 headings x 4 frames");
        assertTrue(Files.isRegularFile(SPRITES.resolve("burrower-crater.png")));
    }
}
