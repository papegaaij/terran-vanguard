package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropLayer;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part A, step A3a: the looks of Act 7's unit mechanics and Vrell space (design/enemies, Window shields, Distorted
 * space and the {@code teleport} movement; design/enemies/space/gate-warden; design/ui/hud, Act 7 additions;
 * design/world/vrell-space, Parallax layers): the window shell's petals, iris, regrowth and glyphs, a hit sparking off
 * it; the leaving tell's tilt and the tracker's amber blink; the jump's fade and shrinking ring; the reversal chevron's
 * timeline and shape; the backdrop's sideways slide per layer; the gate's crimson line; the art the looks read, smooth;
 * and the per-frame state allocating nothing. A small review sheet of the generated shell and chevron and ART-A's ring
 * over the Vrell light is written to the module's {@code build/act7-looks.png}.
 */
class ActSevenLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    // The window shell.

    @Test
    void aHitOnTheClosedShellSparksAndTheTelegraphOpensItLikeAnIris() {
        Sortie sortie = ActSevenFixture.shelled();
        int flakes = 0;
        double most = 0;
        boolean closedAtStart = false;
        for (int t = 0; t < SimStep.ticks(4); t++) {
            sortie.step(Command.FIRE.bit());
            Enemy shelled = ActSevenFixture.find(sortie, "gate-warden");
            if (shelled == null) {
                break;
            }
            if (t == 10) {
                closedAtStart = shelled.window().closed() && shelled.window().iris() == 0;
            }
            flakes += sortie.events().count(SimEvents.Type.SHELL_FLAKE);
            most = Math.max(most, shelled.window().iris());
        }
        assertTrue(closedAtStart, "closed before its first window");
        assertTrue(flakes > 0, "the pulse shots spark off the closed shell");
        assertEquals(1, most, 1e-9, "its line's window draws the iris all the way back");
    }

    @Test
    void thePetalsFoldBackAndFadeOverTheIris() {
        assertEquals(1, WindowShellLooks.petalOpacity(0), 1e-6);
        assertEquals(0, WindowShellLooks.petalOpacity(1), 1e-6);
        assertEquals(0, WindowShellLooks.petalOut(0), 1e-9);
        assertEquals(WindowShellLooks.PETAL_OUT, WindowShellLooks.petalOut(1), 1e-9);
        assertTrue(WindowShellLooks.petalOpacity(0.5) > 0 && WindowShellLooks.petalOpacity(0.5) < 1);
        assertEquals(Math.PI / 2, WindowShellLooks.petalMiddle(0), 1e-9, "petal 0 points up");
        assertEquals(0, WindowShellLooks.regrowOpacity(0), 1e-6);
        assertEquals(1, WindowShellLooks.regrowOpacity(1), 1e-6);
        assertEquals(0.5, WindowShellLooks.regrowShare(12, 24), 1e-9);
        assertEquals(-30, WindowShellLooks.shimmerY(0, 30), 1e-9, "the shimmer rises from the shell's foot");
        assertEquals(30, WindowShellLooks.shimmerY(1, 30), 1e-9);
    }

    @Test
    void theGlyphsBurnOpenAndDimAndFlickerInTheLeavingTell() {
        assertTrue(WindowShellLooks.glyphLevel(1, false) > 3 * WindowShellLooks.glyphLevel(0, false));
        assertEquals(
                WindowShellLooks.TELL_DIM * WindowShellLooks.glyphLevel(0, false),
                WindowShellLooks.glyphLevel(0, true),
                1e-9);
        assertEquals(1, WindowShellLooks.flicker(false, 7, 3), 1e-9);
        Set<Long> levels = new HashSet<>();
        for (long tick = 0; tick < 120; tick++) {
            double f = WindowShellLooks.flicker(true, tick, 3);
            assertTrue(f >= 0.25 && f <= 1, "flicker " + f);
            assertEquals(f, WindowShellLooks.flicker(true, tick - tick % WindowShellLooks.FLICKER_TICKS, 3), 1e-12);
            levels.add(Math.round(f * 100));
        }
        assertTrue(levels.size() > 10, "it flickers: " + levels.size());
    }

    @Test
    void theShellsPetalsAreSmoothAndFadeToNothingInsideTheirCanvas() {
        int width = WindowShellLooks.width(ActSevenFixture.SHELLED);
        int height = WindowShellLooks.height(ActSevenFixture.SHELLED);
        assertEquals(84, width, "the Warden's 76 px egg and its margin");
        assertEquals(92, height);
        float[] whole = new float[width * height];
        Set<Integer> steps = new HashSet<>();
        for (int p = 0; p < WindowShellLooks.PETALS; p++) {
            float[] petal = WindowShellLooks.petal(p, width, height);
            for (int x = 0; x < width; x++) {
                assertEquals(0, petal[x], 1e-6, "top row");
                assertEquals(0, petal[(height - 1) * width + x], 1e-6, "bottom row");
            }
            for (int y = 0; y < height; y++) {
                assertEquals(0, petal[y * width], 1e-6, "left column");
                assertEquals(0, petal[y * width + width - 1], 1e-6, "right column");
            }
            for (int i = 0; i < petal.length; i++) {
                whole[i] = whole[i] + petal[i] * (1 - whole[i]);
                steps.add(Math.round(petal[i] * 255));
            }
        }
        assertTrue(steps.size() >= 32, "smooth: " + steps.size() + " alpha steps");
        float rim = whole[(height / 2 + 8) * width + WindowShellLooks.MARGIN + 2];
        float middle = whole[(height / 2 + 10) * width + width / 2 + 3];
        assertTrue(rim > middle, "the rim brighter than the body: " + rim + " vs " + middle);
        assertTrue(middle > 0.15 && middle < 0.4, "a translucent shell: " + middle);
    }

    // The leaving tell.

    @Test
    void aWardensLeavingTellTiltsItAndBlinksTheTrackerAmberAtTwoHertz() {
        Sortie sortie = ActSevenFixture.warden(4);
        VrellLooks looks = new VrellLooks(null, null);
        long told = -1;
        for (int t = 0; t < SimStep.ticks(12) && told < 0; t++) {
            sortie.step(0);
            looks.stepped(sortie);
            Enemy warden = ActSevenFixture.find(sortie, "gate-warden");
            if (warden != null && warden.leavingTell()) {
                told = sortie.tick();
                assertTrue(VrellLooks.drawsBody(warden));
                assertEquals(0, looks.tilt(warden, sortie.tick()), 1e-9, "the tilt eases in from the tell's start");
            }
        }
        assertTrue(told > 0, "the Warden showed its leaving tell");
        int ease = SimStep.ticks(VrellLooks.TILT_SECONDS);
        assertEquals(1, VrellLooks.tilt(ease), 1e-9);
        assertTrue(VrellLooks.tilt(ease / 2) > 0.3 && VrellLooks.tilt(ease / 2) < 0.7);
        assertFalse(MissionPanel.leavingBlink(0, 0), "no blink without a leaving target");
        assertTrue(MissionPanel.leavingBlink(1, 0));
        assertTrue(MissionPanel.leavingBlink(2, 14));
        assertFalse(MissionPanel.leavingBlink(1, 15), "dark for the second quarter second");
        assertTrue(MissionPanel.leavingBlink(1, SimStep.ticks(0.5)), "2 Hz");
    }

    // The teleport.

    @Test
    void aJumpFadesTheBodyOutThenDrawsItsShrinkingRing() {
        Sortie sortie = ActSevenFixture.skater();
        boolean faded = false;
        boolean gone = false;
        double lastShare = -1;
        for (int t = 0; t < SimStep.ticks(10) && !(faded && gone); t++) {
            sortie.step(0);
            Enemy skater = ActSevenFixture.find(sortie, "rift-skater");
            if (skater == null) {
                continue;
            }
            Teleporting.Phase phase = skater.teleport().phase();
            if (phase == Teleporting.Phase.FADE) {
                float opacity = VrellLooks.opacity(skater);
                assertTrue(opacity <= 1 && opacity >= 0, "fading: " + opacity);
                faded |= opacity < 1;
            } else if (phase == Teleporting.Phase.GONE) {
                assertEquals(0, VrellLooks.opacity(skater), 1e-6, "not drawn while gone");
                double share = skater.teleport().share(ActSevenFixture.JUMPS);
                assertTrue(share >= lastShare, "the ring runs on");
                lastShare = share;
                gone = true;
            }
        }
        assertTrue(faded && gone, "the Skater jumped");
        assertEquals(0, TeleportLooks.ringFrame(0, 10));
        assertEquals(9, TeleportLooks.ringFrame(1, 10), "the last, smallest frame at the landing");
        assertEquals(4, TeleportLooks.ringFrame(0.45, 10));
        assertEquals(22, TeleportLooks.ringRadius(0, false), 1e-9);
        assertEquals(3, TeleportLooks.ringRadius(1, false), 1e-9, "it shrinks (round 84 A.2 a)");
        assertEquals(6, TeleportLooks.ringRadius(1, true), 1e-9);
    }

    // The reversal chevron.

    @Test
    void theChevronIsToldTwoSecondsAheadDimWhileReversedAndFlashesOnceAsItEasesBack() {
        var current = ActSevenFixture.CURRENT;
        assertEquals(ReversalChevron.Show.NONE, ReversalChevron.show(null, 5));
        assertEquals(ReversalChevron.Show.NONE, ReversalChevron.show(current, 3.9));
        assertEquals(ReversalChevron.Show.TELL, ReversalChevron.show(current, 4));
        assertEquals(1, ReversalChevron.opacity(current, 4), 1e-6, "brightest at the tell's start");
        assertEquals(ReversalChevron.PULSE_LOW, ReversalChevron.opacity(current, 4.25), 1e-5, "2 Hz");
        assertEquals(1, ReversalChevron.opacity(current, 4.5), 1e-5);
        assertEquals(ReversalChevron.Show.DIM, ReversalChevron.show(current, 6));
        assertEquals(ReversalChevron.DIM, ReversalChevron.opacity(current, 8), 1e-6);
        double back = 6 + ReversalChevron.EASE_SECONDS + 3;
        assertEquals(ReversalChevron.Show.FLASH, ReversalChevron.show(current, back + 0.05));
        assertTrue(ReversalChevron.opacity(current, back + ReversalChevron.FLASH_SECONDS / 2) > 0.8, "one flash");
        assertTrue(ReversalChevron.opacity(current, back + 0.01) < 0.5, "from the dim chevron");
        assertEquals(ReversalChevron.Show.NONE, ReversalChevron.show(current, back + ReversalChevron.FLASH_SECONDS));
    }

    @Test
    void theChevronIsAVioletWhiteArrowheadOnADarkOutline() {
        float[][] pixels = ReversalChevron.pixels();
        int width = ReversalChevron.WIDTH + 2 * ReversalChevron.PAD;
        int height = ReversalChevron.HEIGHT + 2 * ReversalChevron.PAD;
        assertEquals(width * height, pixels.length);
        for (int x = 0; x < width; x++) {
            assertEquals(0, pixels[x][3], 1e-6, "clear above");
            assertEquals(0, pixels[(height - 1) * width + x][3], 1e-6, "clear below");
        }
        float[] apex = pixels[(ReversalChevron.PAD + 1) * width + width / 2];
        assertTrue(apex[3] > 0.95 && apex[0] > 0.85 && apex[2] > 0.9, "violet-white at the apex");
        float[] outside = pixels[(ReversalChevron.PAD + 1) * width + width / 2 + 4];
        assertTrue(outside[3] > 0.3 && outside[0] < 0.4, "the dark outline beside the stroke");
        float[] hollow = pixels[(height - ReversalChevron.PAD - 1) * width + width / 2];
        assertEquals(0, hollow[3], 1e-6, "a chevron, not a filled triangle");
    }

    // The backdrop's slide.

    @Test
    void theLayersSlideSidewaysAtTheirFactorsDeepLeast() {
        assertEquals(40, BackdropTiles.lateral(40, 1));
        assertEquals(8, BackdropTiles.lateral(40, 0.2));
        assertEquals(-8, BackdropTiles.lateral(-40, 0.2));
        assertEquals(10, BackdropTiles.shift(0, 0, 10));
        assertEquals(470, BackdropTiles.shift(0, 0, -10), "the tiles wrap across the field");
        Sortie sortie = ActSevenFixture.drift();
        double slide = 0;
        for (int t = 0; t < SimStep.ticks(2); t++) {
            sortie.step(0);
            slide += sortie.current().driftStep();
        }
        double expected = 2 * 130 * Math.sin(Math.toRadians(20));
        assertEquals(expected, slide, 2, "20° to the right at 130 px/s");
        assertTrue(BackdropLayer.values().length > 1);
    }

    @Test
    void aGateUnitsLineIsCrimson() {
        assertTrue(LineLooks.gate(ActSevenFixture.SHELLED));
        assertFalse(LineLooks.gate(JovianFixture.CHIMERA));
        assertTrue(LineLooks.GATE_TELEGRAPH.r > 0.8 && LineLooks.GATE_TELEGRAPH.g < 0.2);
    }

    // The art.

    @Test
    void theShellAndTeleportArtIsInPlaceAndFadesInsideItsCanvas() throws IOException {
        record Frames(String name, int frames, int size) {}
        for (Frames set : List.of(
                new Frames(VrellLooks.SHELL_SPARK, 6, 28),
                new Frames(VrellLooks.SHELL_BREAK, 8, 72),
                new Frames(VrellLooks.TELEPORT_FADE, 6, 48),
                new Frames(VrellLooks.TELEPORT_RING, 10, 56),
                new Frames(VrellLooks.TELEPORT_RING_LARGE, 10, 84))) {
            for (int f = 0; f < set.frames(); f++) {
                Path file = SPRITES.resolve(set.name() + "_" + f + ".png");
                BufferedImage image = ImageIO.read(file.toFile());
                assertNotNull(image, file.toString());
                assertEquals(set.size(), image.getWidth(), file.toString());
                for (int i = 0; i < image.getWidth(); i++) {
                    assertEquals(0, lit(image, i, 0), file + ": a lit top border");
                    assertEquals(0, lit(image, i, image.getHeight() - 1), file + ": a lit bottom border");
                }
            }
        }
    }

    private static int lit(BufferedImage image, int x, int y) {
        int argb = image.getRGB(x, y);
        return Math.max(argb >>> 24 == 0 ? 0 : 1, (argb & 0xFFFFFF) == 0 ? 0 : 1);
    }

    // Per frame.

    @Test
    void theLooksStateAllocatesNothingPerStep() {
        Sortie sortie = ActSevenFixture.warden(3);
        VrellLooks looks = new VrellLooks(null, null);
        SerialClock clock = new SerialClock(16);
        var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long least = Long.MAX_VALUE;
        for (int run = 0; run < 5; run++) {
            long bytes = 0;
            for (int t = 0; t < 300; t++) {
                sortie.step(0);
                long before = threads.getCurrentThreadAllocatedBytes();
                looks.stepped(sortie);
                clock.begin();
                clock.touch(t % 7, t);
                clock.end();
                float sum = 0;
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    sum += VrellLooks.opacity(enemy) + (float) looks.tilt(enemy, sortie.tick());
                    sum += (float) WindowShellLooks.flicker(enemy.leavingTell(), sortie.tick(), enemy.serial());
                }
                sum += ReversalChevron.opacity(ActSevenFixture.CURRENT, t / 30.0);
                sum += MissionPanel.leavingBlink(1, t) ? 1 : 0;
                sum += BackdropTiles.shift(0.5, t / 60.0, BackdropTiles.lateral(t * 0.3, 0.2));
                bytes += threads.getCurrentThreadAllocatedBytes() - before;
                assertTrue(sum >= 0);
            }
            least = Math.min(least, bytes);
        }
        assertEquals(0, least, "the looks' state allocated " + least + " bytes");
    }

    // A small review sheet (not a check): the closed, half-open and regrowing shell, the chevron, ART-A's ring.

    @Test
    void writesASmallReviewSheet() throws IOException {
        BufferedImage mood = vrellLight();
        int width = WindowShellLooks.width(ActSevenFixture.SHELLED);
        int height = WindowShellLooks.height(ActSevenFixture.SHELLED);
        BufferedImage sheet = new BufferedImage(5 * 100, 120, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < sheet.getHeight(); y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                sheet.setRGB(x, y, mood.getRGB(250 + x, 300 + y));
            }
        }
        double[] irises = {0, 0.5, 1};
        for (int k = 0; k < irises.length; k++) {
            double out = WindowShellLooks.petalOut(irises[k]);
            float opacity = WindowShellLooks.petalOpacity(irises[k]);
            for (int p = 0; p < WindowShellLooks.PETALS; p++) {
                float[] petal = WindowShellLooks.petal(p, width, height);
                double middle = WindowShellLooks.petalMiddle(p);
                int dx = (int) Math.round(out * Math.cos(middle));
                int dy = (int) -Math.round(out * Math.sin(middle));
                blend(sheet, petal, width, height, 8 + k * 100 + dx, 14 + dy, WindowShellLooks.SHELL, opacity);
            }
        }
        float[][] chevron = ReversalChevron.pixels();
        int cw = ReversalChevron.WIDTH + 2 * ReversalChevron.PAD;
        int ch = ReversalChevron.HEIGHT + 2 * ReversalChevron.PAD;
        for (int copy = 0; copy < 2; copy++) {
            for (int y = 0; y < ch; y++) {
                for (int x = 0; x < cw; x++) {
                    float[] p = chevron[y * cw + x];
                    over(sheet, 330 + x, 30 + copy * (ReversalChevron.HEIGHT + ReversalChevron.GAP) + y, p, 1);
                }
            }
        }
        BufferedImage ring = ImageIO.read(
                SPRITES.resolve(VrellLooks.TELEPORT_RING + "_3.png").toFile());
        for (int y = 0; y < ring.getHeight(); y++) {
            for (int x = 0; x < ring.getWidth(); x++) {
                int argb = ring.getRGB(x, y);
                int base = sheet.getRGB(420 + x, 30 + y);
                int r = Math.min(255, (base >> 16 & 255) + (argb >> 16 & 255));
                int g = Math.min(255, (base >> 8 & 255) + (argb >> 8 & 255));
                int b = Math.min(255, (base & 255) + (argb & 255));
                sheet.setRGB(420 + x, 30 + y, r << 16 | g << 8 | b);
            }
        }
        Path out = Path.of("build", "act7-looks.png");
        Files.createDirectories(out.getParent());
        ImageIO.write(sheet, "png", out.toFile());
        assertTrue(Files.size(out) > 0);
    }

    // Step A3b: the Choir Resonator, the Swarm Call, the spore clouds.

    @Test
    void theWavePicksTheNearestWidthAndFadesInAndOut() {
        assertEquals(32, ResonatorLooks.frameWidth(0), 1e-9);
        assertEquals(260, ResonatorLooks.frameWidth(15), 1e-9);
        for (int i = 0; i < ResonatorLooks.FRAMES; i++) {
            assertEquals(i, ResonatorLooks.frame(ResonatorLooks.frameWidth(i)), "frame " + i);
            if (i > 0) {
                double step = ResonatorLooks.frameWidth(i) / ResonatorLooks.frameWidth(i - 1);
                assertEquals(1.15, step, 0.01, "15 % a step");
            }
        }
        assertEquals(0, ResonatorLooks.frame(10), "never below the narrowest");
        assertEquals(15, ResonatorLooks.frame(400), "never above the widest");
        double scale = 200 / ResonatorLooks.frameWidth(ResonatorLooks.frame(200));
        assertTrue(Math.abs(scale - 1) <= 0.075, "scaled at most 7.5 %: " + scale);
        assertTrue(ResonatorLooks.brightness(0, 340, 0, 0.75) < 0.2, "dim at the nose");
        assertEquals(1, ResonatorLooks.brightness(12, 340, 0, 0.75), 1e-9, "full after 12 px");
        assertEquals(1, ResonatorLooks.brightness(0.8 * 340, 340, 0, 0.75), 1e-9);
        assertEquals(0, ResonatorLooks.brightness(340, 340, 0, 0.75), 1e-9, "gone at its reach");
        assertEquals(0.75 * 0.75, ResonatorLooks.brightness(100, 340, 2, 0.75), 1e-9, "a step a hull");
    }

    @Test
    void theDronesTurnByHeadingAndSnapOnTheirThorn() {
        assertEquals(0, SwarmLooks.heading(Math.PI), "straight down: the art's first heading");
        assertEquals(8, SwarmLooks.heading(0), "straight up");
        assertEquals(4, SwarmLooks.heading(Math.PI * 1.5));
        assertEquals(SwarmLooks.CLAWS - 1, SwarmLooks.claw(0), "the snap as the thorn leaves");
        assertEquals(SwarmLooks.CLAWS - 1, SwarmLooks.claw(SwarmLooks.SNAP_TICKS - 1));
        Set<Integer> frames = new HashSet<>();
        for (long age = SwarmLooks.SNAP_TICKS; age < 200; age++) {
            int claw = SwarmLooks.claw(age);
            assertTrue(claw >= 0 && claw < SwarmLooks.CLAWS - 1, "opening: " + claw);
            frames.add(claw);
        }
        assertEquals(3, frames.size(), "the claws open over three frames");
        assertEquals(1, ShipPanel.swarmLeft(10, 18, 600), 1e-9, "whole through the bloom");
        assertEquals(0.5, ShipPanel.swarmLeft(318, 18, 600), 1e-9, "draining over the hunt");
        assertEquals(0, ShipPanel.swarmLeft(700, 18, 600), 1e-9);
        assertEquals(0, ShipPanel.swarmLeft(-1, 18, 600), 1e-9, "no call");
    }

    @Test
    void aBurningCloudKeepsItsWholeSizeWhileTheVeilShrinks() {
        assertEquals(100, SporeLooks.wholeRadius(100, 0), 1e-9);
        assertEquals(100, SporeLooks.wholeRadius(50, 0.5), 1e-9);
        assertEquals(0, SporeLooks.wholeRadius(0, 1), 1e-9);
        assertEquals(1, SetPiecePasses.rushFill(1, 60), 1e-9, "a rush bar full after a second");
        assertEquals(0.5, SetPiecePasses.rushFill(1, 30), 1e-9);
        assertEquals(0.4, SetPiecePasses.rushFill(0.4, 60), 1e-9, "never above its HP");
        assertEquals(60, ShipPanel.refilled(30, 60, 60), 1e-9);
        assertEquals(45, ShipPanel.refilled(30, 60, 30), 1e-9, "the armour fills over a second");
        assertEquals(3, ShipPanel.countedUp(1, 3, 60));
        assertTrue(ShipPanel.countedUp(1, 3, 30) == 2, "the charges count up");
        assertEquals("ECHO 1 / 3", EchoTracker.count(-1, 3));
        assertEquals("ECHO 2 / 3", EchoTracker.count(1, 3));
        assertTrue(EchoTracker.between(0, 3, true));
        assertFalse(EchoTracker.between(2, 3, true), "after the last echo nothing is next");
    }

    @Test
    void theGearAndCloudArtIsInPlaceAndFadesInsideItsCanvas() throws IOException {
        record Frames(String name, int frames, boolean additive) {}
        for (Frames set : List.of(
                new Frames("choir-resonator-wave", 16, true),
                new Frames("choir-resonator-wave-rim", 16, false),
                new Frames("choir-resonator-ring", 8, true),
                new Frames("choir-resonator-glow", 4, true),
                new Frames("choir-resonator-mount", 5, false),
                new Frames("vrell-swarm-call-drone", 64, false),
                new Frames("vrell-swarm-call-wither", 8, true),
                new Frames("vrell-swarm-call-bloom", 9, true),
                new Frames(SporeLooks.CLOUD, 8, true),
                new Frames(SporeLooks.BURN, 10, true))) {
            for (int f = 0; f < set.frames(); f++) {
                Path file = SPRITES.resolve(set.name() + "_" + f + ".png");
                BufferedImage image = ImageIO.read(file.toFile());
                assertNotNull(image, file.toString());
                if (!set.additive()) {
                    continue;
                }
                for (int i = 0; i < image.getWidth(); i++) {
                    assertEquals(0, lit(image, i, 0), file + ": a lit top border");
                    assertEquals(0, lit(image, i, image.getHeight() - 1), file + ": a lit bottom border");
                }
            }
        }
        assertTrue(Files.isRegularFile(SPRITES.resolve(SporeLooks.VEIL + ".png")));
        assertTrue(Files.isRegularFile(SPRITES.resolve("vrell-swarm-call-drone-glow.png")));
        assertTrue(Files.isRegularFile(SPRITES.resolve("vrell-swarm-call-trail.png")));
    }

    /**
     * A small review sheet (not a check), {@code build/act7-gear.png}: the Resonator's widest front over the brightest
     * band of the Vrell light (Level 44's event plane beside Level 43's deep), four drones across both, a spore cloud
     * whole and half burnt, and the end card's lines' room. Looked at small.
     */
    @Test
    void writesASmallGearReviewSheet() throws IOException {
        BufferedImage mood = vrellLight();
        int band = brightestBand(mood, 140);
        BufferedImage sheet = new BufferedImage(720, 140, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < sheet.getHeight(); y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                int sx = Math.min(mood.getWidth() - 1, x * mood.getWidth() / sheet.getWidth());
                sheet.setRGB(x, y, mood.getRGB(sx, Math.min(mood.getHeight() - 1, band + y)));
            }
        }
        BufferedImage rim =
                ImageIO.read(SPRITES.resolve("choir-resonator-wave-rim_15.png").toFile());
        BufferedImage wave =
                ImageIO.read(SPRITES.resolve("choir-resonator-wave_15.png").toFile());
        paint(sheet, rim, 10, 20, false);
        paint(sheet, wave, 10, 20, true);
        paint(sheet, ImageIO.read(SPRITES.resolve("choir-resonator-wave_6.png").toFile()), 100, 90, true);
        for (int k = 0; k < 4; k++) {
            int x = 300 + k * 34;
            paint(
                    sheet,
                    ImageIO.read(
                            SPRITES.resolve("vrell-swarm-call-drone-glow.png").toFile()),
                    x,
                    50,
                    true);
            paint(
                    sheet,
                    ImageIO.read(SPRITES.resolve("vrell-swarm-call-drone_" + (k * 4 * 4 + k % 4) + ".png")
                            .toFile()),
                    x,
                    50,
                    false);
        }
        BufferedImage veil =
                ImageIO.read(SPRITES.resolve(SporeLooks.VEIL + ".png").toFile());
        paint(sheet, veil, 440, -20, false);
        paint(sheet, ImageIO.read(SPRITES.resolve(SporeLooks.CLOUD + "_0.png").toFile()), 440, -20, true);
        paint(sheet, veil, 580, -20, false);
        paint(sheet, ImageIO.read(SPRITES.resolve(SporeLooks.BURN + "_5.png").toFile()), 580, -20, true);
        Path out = Path.of("build", "act7-gear.png");
        Files.createDirectories(out.getParent());
        ImageIO.write(sheet, "png", out.toFile());
        assertTrue(Files.size(out) > 0);
    }

    /**
     * The Vrell light the review sheets sit on, from the game's own backdrop art (round 84's mood sheet in the design
     * tree is an LFS pointer on CI): Level 44's bright event plane beside Level 43's dark deep, as one opaque image.
     */
    private static BufferedImage vrellLight() throws IOException {
        BufferedImage bright =
                ImageIO.read(ASSETS.resolve("backdrop/level-44/plane-1.png").toFile());
        BufferedImage dark =
                ImageIO.read(ASSETS.resolve("backdrop/level-43/deep-1.png").toFile());
        assertNotNull(bright, "Level 44's event plane");
        assertNotNull(dark, "Level 43's deep");
        int height = Math.min(bright.getHeight(), dark.getHeight());
        BufferedImage light =
                new BufferedImage(bright.getWidth() + dark.getWidth(), height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < light.getWidth(); x++) {
                light.setRGB(x, y, x < bright.getWidth() ? bright.getRGB(x, y) : dark.getRGB(x - bright.getWidth(), y));
            }
        }
        return light;
    }

    /** The top of the {@code height} px band of {@code image} with the most light. */
    private static int brightestBand(BufferedImage image, int height) {
        int best = 0;
        long most = -1;
        for (int top = 0; top + height <= image.getHeight(); top += 10) {
            long sum = 0;
            for (int y = top; y < top + height; y += 2) {
                for (int x = 0; x < image.getWidth(); x += 4) {
                    int rgb = image.getRGB(x, y);
                    sum += (rgb >> 16 & 255) + (rgb >> 8 & 255) + (rgb & 255);
                }
            }
            if (sum > most) {
                most = sum;
                best = top;
            }
        }
        return best;
    }

    /** Paints {@code sprite} at (x, y): added (premultiplied on black) or over (straight alpha). */
    private static void paint(BufferedImage sheet, BufferedImage sprite, int left, int top, boolean additive) {
        for (int y = 0; y < sprite.getHeight(); y++) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                int sx = left + x;
                int sy = top + y;
                if (sx < 0 || sy < 0 || sx >= sheet.getWidth() || sy >= sheet.getHeight()) {
                    continue;
                }
                int argb = sprite.getRGB(x, y);
                if (additive) {
                    int base = sheet.getRGB(sx, sy);
                    int r = Math.min(255, (base >> 16 & 255) + (argb >> 16 & 255));
                    int g = Math.min(255, (base >> 8 & 255) + (argb >> 8 & 255));
                    int b = Math.min(255, (base & 255) + (argb & 255));
                    sheet.setRGB(sx, sy, r << 16 | g << 8 | b);
                } else {
                    float a = (argb >>> 24) / 255f;
                    over(
                            sheet,
                            sx,
                            sy,
                            new float[] {(argb >> 16 & 255) / 255f, (argb >> 8 & 255) / 255f, (argb & 255) / 255f, a},
                            1);
                }
            }
        }
    }

    private static void blend(
            BufferedImage sheet,
            float[] alpha,
            int width,
            int height,
            int left,
            int top,
            com.badlogic.gdx.graphics.Color colour,
            float opacity) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                over(
                        sheet,
                        left + x,
                        top + y,
                        new float[] {colour.r, colour.g, colour.b, alpha[y * width + x]},
                        opacity);
            }
        }
    }

    private static void over(BufferedImage sheet, int x, int y, float[] rgba, float opacity) {
        if (x < 0 || y < 0 || x >= sheet.getWidth() || y >= sheet.getHeight()) {
            return;
        }
        float a = rgba[3] * opacity;
        int base = sheet.getRGB(x, y);
        int r = Math.round((base >> 16 & 255) * (1 - a) + rgba[0] * 255 * a);
        int g = Math.round((base >> 8 & 255) * (1 - a) + rgba[1] * 255 * a);
        int b = Math.round((base & 255) * (1 - a) + rgba[2] * 255 * a);
        sheet.setRGB(x, y, r << 16 | g << 8 | b);
    }
}
