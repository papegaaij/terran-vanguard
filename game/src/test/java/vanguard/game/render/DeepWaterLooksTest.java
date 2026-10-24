package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.game.ui.Fonts;
import vanguard.sim.SimStep;
import vanguard.sim.SpecialLimits;

/**
 * M8 part A, step A6: the Act 4 systems' looks in flight (tools/art/act4_gear.py's layouts and timings): the depth
 * charge's drum stages and spin, its fuse lamp blinking faster towards the burst, the burst sized by the level's
 * radius, the harpoon's cable fading and waving, the sonar contact brackets snapping in, the Bulwark's frames, and the
 * special row's reason word beside the cut name; every sprite the looks draw is in the assets.
 */
class DeepWaterLooksTest {
    private static final Path SPRITES = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "sprites");
    private static final BitmapFont.BitmapFontData SMALL = new BitmapFont.BitmapFontData(
            new FileHandle(new File(System.getProperty("vanguard.assetsDir", "../assets"), Fonts.LABEL_FILE)), false);

    /**
     * M8 AW: the depth charge's toss over the hull: 0.15 s, swelling to its most at the arc's top and back to its size
     * as it drops in ahead.
     */
    @Test
    void theTossSwellsOverTheHullAndDropsIn() {
        assertEquals(Math.round(0.15f * SimStep.PER_SECOND), DeepWaterLooks.TOSS_TICKS);
        assertEquals(1, DeepWaterLooks.tossScale(0), 1e-6);
        assertEquals(1 + DeepWaterLooks.TOSS_SWELL, DeepWaterLooks.tossScale(DeepWaterLooks.TOSS_TICKS / 2.0), 1e-6);
        assertEquals(1, DeepWaterLooks.tossScale(DeepWaterLooks.TOSS_TICKS), 1e-6);
        assertEquals(1, DeepWaterLooks.tossScale(-1), 1e-6, "before its first step");
    }

    /** M8 AW: the beam under water runs faint, eased with the water's blend. */
    @Test
    void theBeamRunsFaintUnderWater() {
        assertEquals(1, ArsenalLooks.waterStrength(0), 1e-6);
        assertEquals(ArsenalLooks.WATER_STRENGTH, ArsenalLooks.waterStrength(1), 1e-6);
        float half = ArsenalLooks.waterStrength(0.5);
        assertTrue(half < 1 && half > ArsenalLooks.WATER_STRENGTH);
    }

    @Test
    void everyActFourSpriteIsInTheAssets() {
        List<String> files = List.of(
                "harpoon-torpedoes-shot_31",
                "harpoon-torpedoes-glow",
                "harpoon-torpedoes-cable",
                "harpoon-torpedoes-muzzle",
                "harpoon-torpedoes-cavity_2",
                "harpoon-torpedoes-hit_7",
                "depth-charges-shot_47",
                "depth-charges-lamp",
                "sonar-pulse-dash",
                "sonar-pulse-corner_1",
                "decoy-flares-flare_3",
                "decoy-flares-glow",
                "bulwark_2",
                "bulwark-jets_2",
                "bulwark-lure_9",
                "bulwark-shot",
                "bulwark-muzzle",
                "bulwark-spark_3",
                "bulwark-loss_9",
                "bulwark-debris_9",
                "ship-hydro_4",
                "rook-hydro_4");
        for (String file : files) {
            assertTrue(Files.isRegularFile(SPRITES.resolve(file + ".png")), file);
        }
        for (int radius : DeepWaterLooks.BURST_RADII) {
            assertTrue(Files.isRegularFile(SPRITES.resolve("depth-charges-burst-" + radius + "_9.png")), "" + radius);
            assertTrue(Files.isRegularFile(SPRITES.resolve("depth-charges-cavity-" + radius + "_3.png")), "" + radius);
        }
    }

    @Test
    void theDrumSinksInThreeStagesAndSpins() {
        int fuse = SimStep.ticks(0.9);
        assertEquals(0, WeaponLooks.chargeFrame(0, fuse));
        assertEquals(1, WeaponLooks.chargeFrame(WeaponLooks.CHARGE_SPIN_TICKS, fuse), "one spin frame on");
        assertEquals(1, WeaponLooks.chargeFrame(fuse / 3, fuse) / WeaponLooks.CHARGE_SPINS, "a third: stage 1");
        assertEquals(2, WeaponLooks.chargeFrame(2 * fuse / 3, fuse) / WeaponLooks.CHARGE_SPINS, "two thirds: stage 2");
        assertEquals(2, WeaponLooks.chargeFrame(5 * fuse, fuse) / WeaponLooks.CHARGE_SPINS, "never past the last");
        for (int age = 0; age < fuse; age++) {
            int frame = WeaponLooks.chargeFrame(age, fuse);
            assertTrue(frame >= 0 && frame < WeaponLooks.CHARGE_STAGES * WeaponLooks.CHARGE_SPINS, "" + frame);
        }
    }

    @Test
    void theFuseLampBlinksFasterTowardsTheBurst() {
        double fuse = 0.9;
        assertTrue(DeepWaterLooks.lampLit(0, fuse), "lit as it rolls out");
        int early = changes(0, fuse / 3, fuse);
        int late = changes(2 * fuse / 3, fuse, fuse);
        assertTrue(late > early, "blinks " + early + " then " + late + " times");
    }

    private static int changes(double from, double to, double fuse) {
        int changes = 0;
        boolean was = DeepWaterLooks.lampLit(from, fuse);
        for (double t = from; t < to; t += SimStep.SECONDS) {
            boolean lit = DeepWaterLooks.lampLit(t, fuse);
            if (lit != was) {
                changes++;
                was = lit;
            }
        }
        return changes;
    }

    @Test
    void theBurstMatchesTheLevelsRadius() {
        assertEquals(0, DeepWaterLooks.burstIndex(40));
        assertEquals(1, DeepWaterLooks.burstIndex(46));
        assertEquals(2, DeepWaterLooks.burstIndex(50));
        assertEquals(4, DeepWaterLooks.burstIndex(64));
        assertEquals(4, DeepWaterLooks.burstIndex(90));
        assertEquals(0, DeepWaterLooks.burstIndex(10));
    }

    @Test
    void theCableFadesAfterAHitAndItsSlackGrowsAlongIt() {
        assertEquals(1f, DeepWaterLooks.cableStrength(-1));
        assertEquals(1f, DeepWaterLooks.cableStrength(0));
        assertEquals(0f, DeepWaterLooks.cableStrength(DeepWaterLooks.CABLE_FADE_TICKS));
        assertTrue(DeepWaterLooks.cableStrength(DeepWaterLooks.CABLE_FADE_TICKS / 2) < 1);
        assertEquals(0, DeepWaterLooks.cableWave(0, 1.3), 1e-9, "no slack at the harpoon");
        for (double t = 0; t < 1; t += 0.05) {
            assertTrue(Math.abs(DeepWaterLooks.cableWave(DeepWaterLooks.CABLE_LENGTH, t)) <= DeepWaterLooks.CABLE_WAVE);
        }
    }

    @Test
    void aContactBracketSnapsInOverAThirdOfASecond() {
        assertEquals(DeepWaterLooks.BRACKET_SNAP, DeepWaterLooks.bracketSnap(0), 1e-9);
        assertEquals(0, DeepWaterLooks.bracketSnap(DeepWaterLooks.BRACKET_SNAP_TICKS), 1e-9);
        assertEquals(0, DeepWaterLooks.bracketSnap(600), 1e-9);
        assertEquals(18, DeepWaterLooks.BRACKET_SNAP_TICKS, "0.3 s");
        assertEquals(24, DeepWaterLooks.BRACKET_PING_TICKS, "0.4 s");
    }

    @Test
    void theBulwarkShowsItsHitThenScorchesUnderHalf() {
        assertEquals(0, DeepWaterLooks.bulwarkFrame(120, 120, 1000));
        assertEquals(1, DeepWaterLooks.bulwarkFrame(120, 120, 0));
        assertEquals(1, DeepWaterLooks.bulwarkFrame(30, 120, DeepWaterLooks.BULWARK_HIT_TICKS - 1));
        assertEquals(2, DeepWaterLooks.bulwarkFrame(59, 120, DeepWaterLooks.BULWARK_HIT_TICKS));
        assertEquals(0, DeepWaterLooks.bulwarkFrame(60, 120, DeepWaterLooks.BULWARK_HIT_TICKS));
    }

    @Test
    void theReasonWordFitsTheSpecialRowWithTheNameCut() {
        for (SpecialLimits.Reason reason : SpecialLimits.Reason.values()) {
            if (reason == SpecialLimits.Reason.NONE) {
                assertTrue(reason.word().isEmpty());
                continue;
            }
            int width = width(reason.word());
            assertTrue(width <= ShipPanel.reasonWidth(reason.word()), reason + ": " + width);
            // The limited row is compact: the forbidden specials' names fit beside it, the Orbital Lance's first word.
            int room = ShipPanel.COUNT_RIGHT
                    - ShipPanel.COMPACT_NAME_X
                    - ShipPanel.reasonWidth(reason.word())
                    - ShipPanel.REASON_GAP;
            for (String name : List.of("AIRSTRIKE", "SONAR PULSE")) {
                assertEquals(name, ShipPanel.cutLabel(name, room), reason + ": " + room);
            }
            assertEquals("ORBITAL", ShipPanel.cutLabel("ORBITAL LANCE", room), reason + ": " + room);
        }
        assertEquals("UNDER ICE", SpecialLimits.Reason.UNDER_ICE.word());
        assertEquals("NO WATER", SpecialLimits.Reason.NO_WATER.word());
        assertEquals("AIRS", ShipPanel.cutLabel("AIRSTRIKE", 35));
        assertFalse(ShipPanel.BULWARK_NAME.isEmpty());
        // The weapons that can read NO WATER keep their name beside it.
        assertEquals("HARPOONS", ShipPanel.hudName("Harpoon Torpedoes"));
        for (String weapon : List.of("Harpoon Torpedoes", "Depth Charges", "Torpedo Pod")) {
            String name = ShipPanel.hudName(weapon);
            assertTrue(width(name) <= ShipPanel.NO_WATER_NAME_WIDTH, name + ": " + width(name));
        }
        assertTrue(width(ShipPanel.BULWARK_NAME) <= 8 * ShipPanel.GLYPH_WIDTH);
    }

    private static int width(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            BitmapFont.Glyph glyph = SMALL.getGlyph(text.charAt(i));
            assertNotNull(glyph, "the font has '" + text.charAt(i) + "'");
            width += glyph.xadvance;
        }
        return width;
    }
}
