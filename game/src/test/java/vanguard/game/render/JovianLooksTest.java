package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.Regrowing;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3a: the looks of A1a's Act 6 mechanics (design/enemies: Reflect, Regeneration, The three-way war;
 * design/enemies/ground/graft-turret, shield-pylon; design/player/systems, the IFF spoofer; design/player/shields,
 * Reflex Shield) on the stand-in sprites: the mirror's plate, heat, cracks, shards and regrowth; the root's pulse and
 * ring and the knit; the seams and the Targeting computer's teal edge; the dome's frames, flicker, collapse and sheen;
 * the prey's teal flash and the spoofer's amber stutter; the bounced bullet and the Reflex bolt's art and mount. Every
 * glow is smooth (the 2026-10-30 dither audit): the generated plate in float steps, the art (ART-A's, the dome's held in
 * {@code build/art-held/} until part G) without a checker, drawn 1x.
 */
class JovianLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path HELD = ASSETS.resolveSibling("build/art-held/sprites");

    // The mirror.

    @Test
    void theHeatGlowsDullRedThenOrangeThenWhite() {
        Color c = new Color();
        MirrorLooks.heatColour(0, c);
        assertTrue(c.r > 0.5f && c.g < 0.15f && c.b < 0.1f, "dull red cold: " + c);
        MirrorLooks.heatColour(0.5, c);
        assertTrue(c.r == 1 && c.g > 0.4f && c.g < 0.6f && c.b < 0.2f, "orange half way: " + c);
        MirrorLooks.heatColour(1, c);
        assertTrue(c.r == 1 && c.g > 0.9f && c.b > 0.8f, "white at the break: " + c);
        assertEquals(0, MirrorLooks.heatOpacity(0));
        assertEquals(1, MirrorLooks.heatOpacity(1), 1e-6);
        float last = -1;
        for (int i = 0; i <= 100; i++) {
            float o = MirrorLooks.heatOpacity(i / 100.0);
            assertTrue(o >= last, "the glow only rises with the heat");
            last = o;
        }
    }

    @Test
    void cracksShowFromSeventyPercent() {
        assertEquals(0, MirrorLooks.crackOpacity(0.69));
        assertTrue(MirrorLooks.crackOpacity(0.7) > 0, "at 70 %");
        assertEquals(1, MirrorLooks.crackOpacity(1), 1e-6);
    }

    @Test
    void theRegrowthsShimmerSweepsFromOneTipToTheOther() {
        double half = JovianFixture.MIRROR.halfArcRadians();
        int total = SimStep.ticks(JovianFixture.MIRROR.regrowSeconds());
        assertEquals(0, MirrorLooks.regrowShare(total, total), 1e-9);
        assertEquals(1, MirrorLooks.regrowShare(0, total), 1e-9);
        assertEquals(-half, MirrorLooks.sweepAngle(0, half), 1e-9, "from one tip");
        assertEquals(half, MirrorLooks.sweepAngle(1, half), 1e-9, "to the other");
        assertEquals(0, MirrorLooks.specular(0), 1e-9);
        assertEquals(0.5, MirrorLooks.specular(MirrorLooks.SWEEP_SECONDS / 2), 1e-9, "the slow specular sweep");
    }

    @Test
    void theShardsFlyOutFromThePlateAndFadeInHalfASecond() {
        double[] at = new double[2];
        for (int k = 0; k < MirrorLooks.SHARDS; k++) {
            MirrorLooks.shardOffset(k, SimStep.ticks(0.3), 0, at);
            assertTrue(at[1] < 0, "facing down the screen, the shards fly down and out: shard " + k);
        }
        assertEquals(1, MirrorLooks.shardOpacity(0), 1e-6);
        assertEquals(0, MirrorLooks.shardOpacity(SimStep.ticks(MirrorLooks.SHARD_SECONDS)));
    }

    @Test
    void thePlateIsASmoothCrescentOverTheArc() {
        double half = JovianFixture.MIRROR.halfArcRadians();
        assertTrue(MirrorLooks.plateAlpha(MirrorLooks.PLATE_RADIUS, 0, half) > 0.99f, "solid in its middle");
        assertEquals(0, MirrorLooks.plateAlpha(MirrorLooks.PLATE_RADIUS, half * 1.01, half), "nothing past the arc");
        assertEquals(0, MirrorLooks.plateAlpha(MirrorLooks.PLATE_RADIUS + 5, 0, half), "nothing off the band");
        BufferedImage plate = image(half, false);
        assertTrue(levels(plate) >= 32, "smooth: " + levels(plate) + " alpha steps");
        for (int step = 1; step <= 2; step++) {
            assertTrue(SovereignLooksTest.checkerShare(plate, step) < 0.02, "no dither in the plate");
        }
    }

    @Test
    void aMirroredUnitsHullFollowsItsMirror() {
        Sortie sortie = JovianFixture.mirror();
        assertTrue(JovianFixture.runUntil(
                sortie, Command.NONE, 10, () -> JovianFixture.find(sortie, JovianFixture.MIRRORED) != null));
        Enemy unit = JovianFixture.find(sortie, JovianFixture.MIRRORED);
        JovianFixture.idle(sortie, 0.5);
        assertEquals(unit.mirror().facing(), MirrorLooks.heading(unit), 1e-12);
        assertTrue(MirrorLooks.any(sortie.enemyKinds()));
        assertEquals(MirrorLooks.REACH * 52, MirrorLooks.reach(52, 48), 1e-9, "out along its larger side");
    }

    // Regrowth.

    @Test
    void theRootPulsesFromOneToFourHertzAndItsRingShrinksToNothing() {
        assertEquals(1, RegrowthLooks.pulseHz(0), 1e-9);
        assertEquals(4, RegrowthLooks.pulseHz(1), 1e-9);
        assertEquals(1, RegrowthLooks.pulse(0, 5), 1e-9, "a beat as it opens");
        // Over the 5 s window: 1 Hz rising to 4 Hz is 12.5 beats.
        int beats = 0;
        double last = RegrowthLooks.pulse(0, 5);
        boolean rising = false;
        for (int t = 1; t <= SimStep.ticks(5); t++) {
            double now = RegrowthLooks.pulse(t / (double) SimStep.ticks(5), 5);
            if (rising && now < last) {
                beats++;
            }
            rising = now > last;
            last = now;
        }
        assertEquals(12, beats, "the beats quicken over the window");
        assertEquals(0, RegrowthLooks.ringRadius(1, 24), 1e-9);
        assertTrue(RegrowthLooks.ringRadius(0, 24) > 24, "round the root at first");
        assertEquals(0.5, RegrowthLooks.knitShare(SimStep.ticks(0.4), SimStep.ticks(0.8)), 1e-9);
    }

    @Test
    void aKilledTurretShowsItsRootThenKnitsBack() {
        Sortie sortie = JovianFixture.graft();
        boolean[] firstBurst = {false};
        assertTrue(JovianFixture.runUntil(sortie, Command.FIRE.bit(), 15, () -> {
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ROOT_EXPOSED) {
                    firstBurst[0] = killedBefore(events, i);
                    return true;
                }
            }
            return false;
        }));
        assertTrue(firstBurst[0], "its first death: the kill's burst is the shell's");
        Enemy turret = JovianFixture.find(sortie, JovianFixture.GRAFT);
        assertNotNull(turret);
        assertTrue(RegrowthLooks.regrowing(turret), "the root shows: the body is the shell");
        assertEquals(Regrowing.Phase.ROOT, turret.regrowth().phase());
        assertTrue(JovianFixture.runUntil(
                sortie, Command.NONE, 6, () -> turret.regrowth().phase() == Regrowing.Phase.KNIT));
        assertTrue(RegrowthLooks.regrowing(turret), "knitting");
        assertTrue(JovianFixture.runUntil(
                sortie, Command.NONE, 2, () -> turret.regrowth().phase() == Regrowing.Phase.TURRET));
        assertFalse(RegrowthLooks.regrowing(turret), "a turret again");
    }

    // Regeneration.

    @Test
    void theSeamsPulseTwiceASecondBrighterTowardFull() {
        double low = RegenerationLooks.seamGlow(true, 0.2, 0);
        double high = RegenerationLooks.seamGlow(true, 0.9, 0);
        assertTrue(high > low, "brighter the closer to full");
        assertEquals(high, RegenerationLooks.seamGlow(true, 0.9, 0.5), 1e-9, "two pulses a second");
        assertTrue(RegenerationLooks.seamGlow(true, 0.9, 0.25) < high, "between the pulses");
        assertEquals(RegenerationLooks.FULL, RegenerationLooks.seamGlow(false, 1, 3), 1e-9, "faint at full");
        assertEquals(RegenerationLooks.HIT, RegenerationLooks.seamGlow(false, 0.5, 3), 1e-9, "dark just hit");
        assertTrue(RegenerationLooks.HIT < RegenerationLooks.FULL);
    }

    @Test
    void aHurtChimeraHealsAndItsSeamsGlow() {
        Sortie sortie = JovianFixture.chimera();
        assertTrue(JovianFixture.runUntil(sortie, Command.FIRE.bit(), 10, () -> {
            Enemy c = JovianFixture.find(sortie, JovianFixture.CHIMERA);
            return c != null && c.hp() < c.spec().hp();
        }));
        Enemy chimera = JovianFixture.find(sortie, JovianFixture.CHIMERA);
        assertEquals(RegenerationLooks.HIT, RegenerationLooks.seamGlow(chimera, sortie.tick()), 1e-9, "dark when hit");
        assertTrue(JovianFixture.runUntil(
                sortie, Command.NONE, 2, () -> chimera.regeneration().healing()));
        assertTrue(RegenerationLooks.seamGlow(chimera, sortie.tick()) > 0, "the seams glow as it heals");
        assertTrue(RegenerationLooks.any(sortie.enemyKinds()));
        assertEquals(Color.valueOf("00FF9A"), TargetingOverlay.TEAL, "the bar's teal edge is the tissue's teal");
    }

    // The dome.

    @Test
    void theDomeLoopsFlickersBackAndCollapses() {
        assertEquals(0, PylonDomeLooks.domeFrame(0, 0, 12));
        assertEquals(1, PylonDomeLooks.domeFrame(PylonDomeLooks.DOME_FRAME_TICKS, 0, 12));
        assertEquals(0, PylonDomeLooks.domeFrame(12L * PylonDomeLooks.DOME_FRAME_TICKS, 0, 12), "a 4 s loop");
        assertEquals(SimStep.ticks(4), 12 * PylonDomeLooks.DOME_FRAME_TICKS);
        int total = SimStep.ticks(0.3);
        assertTrue(PylonDomeLooks.flickerOn(total, total), "its first flash");
        assertFalse(PylonDomeLooks.flickerOn(total - PylonDomeLooks.FLICKER_TICKS, total), "then dark");
        assertTrue(PylonDomeLooks.flickerOn(1, total), "steady in its last third");
        assertTrue(PylonDomeLooks.flickerOn(0, total), "standing");
        assertEquals(5, PylonDomeLooks.collapseFrame(SimStep.ticks(0.4) - 1, 6), "the 0.4 s collapse's last frame");
        assertEquals(-1, PylonDomeLooks.collapseFrame(SimStep.ticks(0.4), 6));
        assertEquals(1, PylonDomeLooks.sheenFrame(PylonDomeLooks.SHEEN_FRAME_TICKS, 0, 4));
    }

    @Test
    void aUnitUnderTheDomeIsCoveredAndTheDomeIsTracked() {
        Sortie sortie = JovianFixture.dome();
        assertNotNull(sortie.domes(), "a level with a pylon has its domes");
        assertTrue(
                JovianFixture.runUntil(sortie, Command.NONE, 10, () -> {
                    Enemy unit = JovianFixture.find(sortie, JovianFixture.COVERED);
                    return unit != null && unit.covered();
                }),
                "the unit in the dome's middle wears the sheen");
        int tracked = 0;
        for (int slot = 0; slot < sortie.domes().capacity(); slot++) {
            if (sortie.domes().standing(slot)) {
                tracked++;
                assertEquals(110, sortie.domes().radius(slot), 1e-9);
            }
        }
        assertEquals(1, tracked);
    }

    // The war's and the spoofer's flashes.

    @Test
    void thePreyFlashFadesAndTheStutterSearchesForHalfASecond() {
        assertEquals(1, JovianFlashes.preyOpacity(0), 1e-6);
        assertEquals(0, JovianFlashes.preyOpacity(JovianFlashes.PREY_TICKS));
        int on = 0;
        int changes = 0;
        boolean last = JovianFlashes.stutterOn(0);
        for (int t = 0; t < SimStep.ticks(JovianFlashes.STUTTER_SECONDS); t++) {
            boolean now = JovianFlashes.stutterOn(t);
            on += now ? 1 : 0;
            changes += now != last ? 1 : 0;
            last = now;
        }
        assertTrue(on > 0 && changes >= 4, "a flicker, not a steady glow: " + changes + " changes");
        assertFalse(JovianFlashes.stutterOn(SimStep.ticks(JovianFlashes.STUTTER_SECONDS)), "over with the delay");
        assertEquals(Color.valueOf("00FF9A"), JovianFlashes.TEAL);
    }

    // The bullets, the Reflex bolt and the art.

    @Test
    void theReflexBoltsMountHasALook() {
        Sortie sortie = JovianFixture.mirror();
        int mounts = sortie.armament().size();
        assertEquals(sortie.wingmanMount() + 1, WeaponLooks.boltMount(mounts), "PlayerFire.boltMount()");
        assertTrue(WeaponLooks.boltMount(mounts) < WeaponLooks.lookCount(mounts), "no index out of the looks");
        assertTrue(sortie.wingmanMount() < WeaponLooks.lookCount(mounts));
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertSet(SPRITES, BulletLooks.BOUNCED, BulletLooks.BOUNCED_HEADINGS, 27, 27);
        assertSet(SPRITES, WeaponLooks.REFLEX_BOLT, 4, 15, 15);
        BufferedImage trail =
                ImageIO.read(SPRITES.resolve(WeaponLooks.REFLEX_TRAIL + ".png").toFile());
        assertEquals(11, trail.getWidth());
        assertEquals(30, trail.getHeight());
        assertSet(HELD, PylonDomeLooks.DOME, 12, 232, 232);
        assertSet(HELD, PylonDomeLooks.HIT, 6, 32, 32);
        assertSet(HELD, PylonDomeLooks.SHEEN, 4, 48, 48);
        assertEquals(
                PylonDomeLooks.COLLAPSE_FRAMES,
                frames(HELD, PylonDomeLooks.COLLAPSE).size());
        BufferedImage feed =
                ImageIO.read(HELD.resolve(PylonDomeLooks.FEED + ".png").toFile());
        assertEquals(32, feed.getWidth());
        assertEquals(12, feed.getHeight());
    }

    @Test
    void theGlowsAreSmoothAndDrawnOnce() throws IOException {
        List<Path> glows = new java.util.ArrayList<>();
        glows.addAll(frames(HELD, PylonDomeLooks.DOME));
        glows.addAll(frames(HELD, PylonDomeLooks.HIT));
        glows.addAll(frames(HELD, PylonDomeLooks.SHEEN));
        glows.addAll(frames(HELD, PylonDomeLooks.COLLAPSE));
        glows.add(HELD.resolve(PylonDomeLooks.FEED + ".png"));
        glows.add(SPRITES.resolve(WeaponLooks.REFLEX_TRAIL + ".png"));
        glows.addAll(frames(SPRITES, BulletLooks.BOUNCED));
        for (Path file : glows) {
            BufferedImage image = ImageIO.read(file.toFile());
            for (int step = 1; step <= 2; step++) {
                double share = SovereignLooksTest.checkerShare(image, step);
                assertTrue(share < 0.02, file.getFileName() + ": a " + step + " px checker on " + share + " of it");
            }
        }
        assertEquals(110, PylonDomeLooks.ART_RADIUS, 1e-9, "a 110 px dome drawn 1x");
    }

    private static void assertSet(Path dir, String name, int count, int width, int height) throws IOException {
        List<Path> files = frames(dir, name);
        assertEquals(count, files.size(), name);
        for (Path file : files) {
            BufferedImage image = ImageIO.read(file.toFile());
            assertEquals(width, image.getWidth(), file.getFileName().toString());
            assertEquals(height, image.getHeight(), file.getFileName().toString());
        }
    }

    private static List<Path> frames(Path dir, String name) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(path ->
                            path.getFileName().toString().matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .sorted()
                    .toList();
        }
    }

    /** The generated plate (or its cracks) for a mirror over {@code half} each side, as an image. */
    private static BufferedImage image(double half, boolean cracked) {
        int size = MirrorLooks.PLATE_SIZE;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        double c = size / 2.0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x + 0.5 - c;
                double dy = y + 0.5 - c;
                float a = cracked
                        ? MirrorLooks.crackAlpha(dx, dy, half)
                        : MirrorLooks.plateAlpha(Math.hypot(dx, dy), Math.atan2(dx, dy), half);
                image.setRGB(x, y, Math.round(a * 255) << 24 | 0xFFFFFF);
            }
        }
        return image;
    }

    /** Whether an {@code ENEMY_DESTROYED} of event {@code i}'s kind at its place comes before it in the step. */
    private static boolean killedBefore(SimEvents events, int i) {
        for (int e = 0; e < i; e++) {
            if (events.type(e) == SimEvents.Type.ENEMY_DESTROYED
                    && events.value(e) == events.value(i)
                    && events.x(e) == events.x(i)) {
                return true;
            }
        }
        return false;
    }

    /** The distinct non-zero alpha values of an image. */
    private static int levels(BufferedImage image) {
        java.util.Set<Integer> values = new java.util.HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int a = image.getRGB(x, y) >>> 24;
                if (a != 0) {
                    values.add(a);
                }
            }
        }
        return values.size();
    }
}
