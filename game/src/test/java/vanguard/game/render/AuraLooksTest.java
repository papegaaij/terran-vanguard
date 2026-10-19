package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Intel;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part D: the Choir Herald's aura as drawn (design/enemies/air/choir-herald, Q.22g; tools/art/siege_units.py's
 * sprites), on a hand-built sortie ({@link SiegeUnitsFixture}): a ringed unit's ring thins segment by segment with
 * its share, scaled to the unit; the absorb flash widens over 0.15 s; the crest pulses 0-1-2-3-2-1; the radius
 * circle is the art's 90 px scaled to the difficulty's radius; at the Herald's death the rings collapse over
 * 0.3 s and are gone. The level's intel lists both new units with their portraits and marks the dome sections.
 */
class AuraLooksTest {
    private static final Path SPRITES = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "sprites");

    @Test
    void theRingThinsSegmentBySegmentWithItsShare() {
        assertEquals(6, AuraLooks.segments(1, 7), "full: all six lit");
        assertEquals(6, AuraLooks.segments(0.9, 7), "a scratch still shows six");
        assertEquals(5, AuraLooks.segments(5.0 / 6, 7));
        assertEquals(3, AuraLooks.segments(0.5, 7));
        assertEquals(1, AuraLooks.segments(0.01, 7), "any absorb left lights one");
        assertEquals(0, AuraLooks.segments(0, 7), "spent: the faint empty track");
        assertEquals(2, AuraLooks.segments(1, 3), "a shorter set stays inside it");
    }

    @Test
    void theRingIsScaledToItsUnit() {
        assertEquals(46, AuraLooks.ringRadius(84, 84), 1e-9, "a Shellback's 84 px body");
        assertEquals(30, AuraLooks.ringRadius(52, 48), 1e-9, "a Needler-sized unit");
        assertEquals(AuraLooks.MIN_RING_RADIUS, AuraLooks.ringRadius(10, 12), 1e-9, "never smaller");
    }

    @Test
    void theAbsorbFlashWidensOverItsThreeFrames() {
        assertEquals(0, AuraLooks.flashFrame(0, 3));
        assertEquals(1, AuraLooks.flashFrame(AuraLooks.FLASH_FRAME_TICKS, 3));
        assertEquals(2, AuraLooks.flashFrame(3L * AuraLooks.FLASH_FRAME_TICKS - 1, 3));
        assertEquals(-1, AuraLooks.flashFrame(3L * AuraLooks.FLASH_FRAME_TICKS, 3), "played out after 0.15 s");
        assertEquals(-1, AuraLooks.flashFrame(-1, 3));
    }

    @Test
    void theCrestPulsesBackAndForthAtSixFramesASecond() {
        int[] expected = {0, 1, 2, 3, 2, 1, 0, 1};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], AuraLooks.crestFrame((long) i * AuraLooks.CREST_FRAME_TICKS, 0, 4), "step " + i);
        }
        assertEquals(SimStep.PER_SECOND / 6, AuraLooks.CREST_FRAME_TICKS, "6 fps");
        assertEquals(0, AuraLooks.crestFrame(5, 0, 1));
    }

    @Test
    void theRadiusCirclePulsesFaintly() {
        float low = 1;
        float high = 0;
        for (int i = 0; i < 100; i++) {
            float opacity = AuraLooks.auraOpacity(i * AuraLooks.AURA_PULSE_SECONDS / 100);
            low = Math.min(low, opacity);
            high = Math.max(high, opacity);
        }
        assertEquals(AuraLooks.AURA_LOW, low, 0.01);
        assertEquals(AuraLooks.AURA_HIGH, high, 0.01);
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(7, frames("choir-herald-ring"), "0 to 6 segments");
        assertEquals(3, frames("choir-herald-ring-flash"));
        assertEquals(4, frames("choir-herald-thread"));
        assertEquals(4, frames("choir-herald-crest"));
        assertEquals(8, frames("choir-herald"), "the wing beat");
        BufferedImage ring =
                ImageIO.read(SPRITES.resolve("choir-herald-ring_6.png").toFile());
        assertEquals(64, ring.getWidth());
        BufferedImage aura =
                ImageIO.read(SPRITES.resolve("choir-herald-aura.png").toFile());
        assertEquals(184, aura.getWidth(), "the art's 90 px radius (2 x 90 + its stroke)");
        BufferedImage thread =
                ImageIO.read(SPRITES.resolve("choir-herald-thread_0.png").toFile());
        assertEquals(AuraLooks.THREAD_TILE, thread.getWidth(), "a strip tiling every 16 px");
    }

    @Test
    void theHeraldIsTheKindThatSingsAnAura() {
        Sortie sortie = SiegeUnitsFixture.herald();
        boolean[] heralds = AuraLooks.heralds(sortie.enemyKinds());
        for (int k = 0; k < heralds.length; k++) {
            assertEquals(sortie.enemyKinds().get(k) == SiegeUnitsFixture.HERALD, heralds[k]);
        }
    }

    @Test
    void aRingedUnitShowsItsRingFullThenTheRingsCollapseAtTheHeraldsDeath() {
        Sortie sortie = SiegeUnitsFixture.herald();
        Enemy target = null;
        while (sortie.levelSeconds() < 10 && (target == null || target.ringShare() < 1)) {
            sortie.step(Command.NONE);
            target = SiegeUnitsFixture.find(sortie, SiegeUnitsFixture.TARGET);
        }
        assertNotNull(target);
        Enemy herald = SiegeUnitsFixture.find(sortie, SiegeUnitsFixture.HERALD);
        assertNotNull(herald);
        assertEquals(herald.serial(), target.ringFeeder(), "a thread from the Herald to it");
        assertTrue(AuraLooks.ringed(target));
        assertEquals(6, AuraLooks.segments(target.ringShare(), 7), "the full ring");
        assertFalse(AuraLooks.ringed(herald), "no ring on the Herald itself");
        // The ship's column is the Herald's: its fire kills the Herald, never the target beside it.
        int collapsed = 0;
        for (int i = 0; i < SimStep.ticks(8) && collapsed == 0; i++) {
            sortie.step(Command.FIRE.bit());
            collapsed += sortie.events().count(SimEvents.Type.RINGS_COLLAPSED);
        }
        assertEquals(1, collapsed, "the Herald died");
        assertEquals(null, SiegeUnitsFixture.find(sortie, SiegeUnitsFixture.HERALD));
        sortie.step(Command.NONE);
        assertTrue(target.ringCollapse() > 0, "collapsing");
        assertTrue(AuraLooks.ringed(target), "the shrinking ring still shows");
        SiegeUnitsFixture.runTo(sortie, sortie.levelSeconds() + SiegeUnitsFixture.AURA.collapseSeconds() + 0.05);
        assertFalse(AuraLooks.ringed(target), "gone after 0.3 s");
        assertEquals(0, AuraLooks.segments(target.ringShare(), 7));
    }

    @Test
    void theLevelsIntelShowsBothNewUnitsAndMarksTheDomeSections() {
        var content = DomesFixture.CONTENT;
        String key = content.levelKey(17).orElseThrow();
        Intel intel = Intel.of(content, key, 2);
        List<String> enemies = intel.enemies();
        assertTrue(enemies.contains("Choir Herald"), enemies.toString());
        assertTrue(enemies.contains("Shellback"), enemies.toString());
        assertTrue(Files.isRegularFile(SPRITES.resolve("intel/" + Portraits.slug("Choir Herald") + ".png")));
        assertTrue(Files.isRegularFile(SPRITES.resolve("intel/" + Portraits.slug("Shellback") + ".png")));
        long phases = intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.PHASE)
                .count();
        assertEquals(2, phases, "the two dome sections: " + intel.marks());
    }

    private static int frames(String name) throws IOException {
        try (var files = Files.list(SPRITES)) {
            return (int) files.map(path -> path.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }
}
