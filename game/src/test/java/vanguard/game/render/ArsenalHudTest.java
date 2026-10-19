package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.SpecialsData;
import vanguard.game.ui.Fonts;
import vanguard.sim.Beam;
import vanguard.sim.LevelScript;

/**
 * M7 part A's HUD and looks (design/ui/hud; design/player/specials, Cooldown specials and the HUD ring;
 * design/player/systems, Evasive thrusters; the drone's hits strip): every special's name fits its row
 * beside its charges, its seconds left or {@code READY} (the Shield Overcharge reads {@code OVERCHARGE});
 * the ring's text; the overcharge's second layer as white cores; the drone strip draining and refilling;
 * the dash pip right of the shield's label plate; the beam's and the fields' sprite levels by width and
 * radius; a set piece's part as the beam's heat target.
 */
class ArsenalHudTest {
    private static final BitmapFont.BitmapFontData SMALL = font(Fonts.LABEL_FILE);
    private static final Content CONTENT = ContentLoader.fromClasspath();
    /** The label plate (assets/sprites/hud/plate.png) is 120 px wide, its text indented 4 px. */
    private static final int PLATE_WIDTH = 120;

    @Test
    void everySpecialsNameFitsItsRowBesideItsCount() {
        List<SpecialsData.Special> specials = CONTENT.specials().specials();
        assertTrue(specials.stream().anyMatch(SpecialsData.Special::cooldownSpecial), "the Shield Overcharge is in");
        for (SpecialsData.Special special : specials) {
            String label = ShipPanel.specialLabel(special.name());
            boolean compact = ShipPanel.compact(label, special.cooldownSpecial());
            int nameX = compact ? ShipPanel.COMPACT_NAME_X : ShipPanel.NAME_X;
            int name = width(SMALL, label);
            List<String> counts = special.cooldownSpecial()
                    ? List.of(ShipPanel.READY, ShipPanel.cooldownText(false, 44.2))
                    : List.of("x3", "x0");
            for (String count : counts) {
                int right = ShipPanel.COUNT_RIGHT - width(SMALL, count);
                assertTrue(
                        nameX + name + 4 <= right, label + " beside " + count + ": " + (nameX + name) + " / " + right);
            }
            if (!compact) {
                assertTrue(width(SMALL, "SPECIAL") + 6 + 2 <= ShipPanel.ICON_X, "the word before the icon");
            }
        }
        // The Act 1-2 specials keep their row; the cooldown special and the long name go compact.
        assertFalse(ShipPanel.compact("AIRSTRIKE", false));
        assertFalse(ShipPanel.compact("SMART BOMB", false));
        assertFalse(ShipPanel.compact("EMP BURST", false));
        assertTrue(ShipPanel.compact("ORBITAL LANCE", false));
        assertTrue(ShipPanel.compact("OVERCHARGE", true));
        assertEquals("OVERCHARGE", ShipPanel.specialLabel("Shield Overcharge"), "too wide in full (design/ui/hud)");
        assertEquals("EMP BURST", ShipPanel.specialLabel("EMP Burst"));
        assertEquals("ORBITAL LANCE", ShipPanel.specialLabel("Orbital Lance"));
    }

    @Test
    void aCoolingSpecialReadsItsWholeSecondsLeftAndThenReady() {
        assertEquals("45s", ShipPanel.cooldownText(false, 45));
        assertEquals("27s", ShipPanel.cooldownText(false, 26.01));
        assertEquals("1s", ShipPanel.cooldownText(false, 0.02));
        assertEquals("READY", ShipPanel.cooldownText(true, 0));
        assertEquals("READY", ShipPanel.cooldownText(false, 0), "the step it is over");
    }

    /**
     * Round 37, R.4 (d): the ring's READY flash and the dash pip's white state last 0.2 s of simulation
     * time, however many frames are drawn in it (at an uncapped 460 fps they were 12 and 10 frames).
     */
    @Test
    void theReadyFlashesLastTimeNotFrames() {
        assertEquals(12, ShipPanel.READY_FLASH_TICKS, "0.2 s at 60 steps a second");
        long start = 1000;
        assertTrue(ShipPanel.flashing(start, start));
        assertTrue(ShipPanel.flashing(start + 11, start));
        assertFalse(ShipPanel.flashing(start + 12, start), "over after 0.2 s");
        assertFalse(ShipPanel.flashing(start - 1, start));
        assertFalse(ShipPanel.flashing(start, Long.MIN_VALUE / 2), "no flash before the first one");
    }

    @Test
    void theOverchargesSecondLayerFillsTheCellsAsWhiteCores() {
        assertEquals(0, ShipPanel.secondLayer(45, 45), 1e-9, "a full shield has no second layer");
        assertEquals(0, ShipPanel.secondLayer(30, 45), 1e-9);
        assertEquals(1, ShipPanel.secondLayer(90, 45), 1e-9, "twice the capacity: every cell");
        assertEquals(0.5, ShipPanel.secondLayer(67.5, 45), 1e-9);
        assertEquals(0, ShipPanel.secondLayer(10, 0), 1e-9, "no shield");
        assertEquals(java.awt.Color.WHITE.getRGB() & 0xFFFFFF, rgb(ShipPanel.OVERCHARGE_CORE), "white (G.4 = b)");
    }

    @Test
    void theDroneStripDrainsWithItsHitsAndRefillsDimWhileRebuilt() {
        assertEquals(5, ShipPanel.droneHitPips(8, 8, false, 1));
        assertEquals(4, ShipPanel.droneHitPips(6, 8, false, 1), "a pip lit while any of its share is left");
        assertEquals(1, ShipPanel.droneHitPips(1, 12, false, 1));
        assertEquals(0, ShipPanel.droneHitPips(0, 12, true, 0), "broken: empty");
        assertEquals(2, ShipPanel.droneHitPips(0, 12, true, 0.5));
        assertEquals(5, ShipPanel.droneHitPips(0, 12, true, 1));
    }

    @Test
    void theDashPipSitsRightOfTheShieldsLabelPlate() {
        int plateRight = PLATE_WIDTH - HudKit.PLATE_INDENT;
        assertTrue(ShipPanel.DASH_PIP_X >= plateRight + 4, "clear of the plate");
        // The shield number (at most "90" with the Shield Overcharge, body font 10 px a glyph) stays clear.
        assertTrue(ShipPanel.DASH_PIP_X + ShipPanel.DASH_PIP_WIDTH + 4 <= HudKit.INNER_WIDTH - 2 * 10);
    }

    @Test
    void theBeamsAndFieldsSpritesFollowTheirWidthAndRadius() {
        int[] widths = {6, 8, 10, 12, 14, 18};
        for (int level = 0; level < widths.length; level++) {
            assertEquals(level, ArsenalLooks.level(widths, widths[level]));
        }
        assertEquals(5, ArsenalLooks.level(widths, 17), "the overdrive's 18 px");
        int[] radii = {22, 24, 26, 28, 30, 32};
        assertEquals(0, ArsenalLooks.level(radii, 22));
        assertEquals(5, ArsenalLooks.level(radii, 32), "the overdrive's field");
    }

    @Test
    void aSetPiecesPartIsTheBeamsHeatTargetByPieceAndPart() {
        int target = Beam.PART_TARGET + 2 * LevelScript.SetPieceSpec.MAX_PARTS + 5;
        assertEquals(2, ArsenalLooks.heatPiece(target));
        assertEquals(5, ArsenalLooks.heatPart(target));
        assertEquals(-1, ArsenalLooks.heatPiece(17), "an enemy's serial");
        assertEquals(-1, ArsenalLooks.heatPart(17));
        assertEquals(-1, ArsenalLooks.heatPiece(ArsenalLooks.GROUND_TARGET), "a ground object");
    }

    @Test
    void theStunnedGlowDimsAndTheHeatTintIsSteady() {
        assertTrue(ArsenalLooks.STUNNED_GLOW > 0 && ArsenalLooks.STUNNED_GLOW < 1, "a Vrell glow dims, not out");
        assertTrue(ArsenalLooks.HEAT_AMOUNT > 0 && ArsenalLooks.HEAT_AMOUNT < 1, "a tint, not a white silhouette");
        assertTrue(ArsenalLooks.FLICKER_ALPHA > 0 && ArsenalLooks.FLICKER_ALPHA < 1, "the dash flicker is see-through");
    }

    private static int rgb(com.badlogic.gdx.graphics.Color colour) {
        return Math.round(colour.r * 255) << 16 | Math.round(colour.g * 255) << 8 | Math.round(colour.b * 255);
    }

    private static BitmapFont.BitmapFontData font(String file) {
        return new BitmapFont.BitmapFontData(
                new FileHandle(new File(System.getProperty("vanguard.assetsDir"), file)), false);
    }

    private static int width(BitmapFont.BitmapFontData font, String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            BitmapFont.Glyph glyph = font.getGlyph(text.charAt(i));
            assertNotNull(glyph, "the font has '" + text.charAt(i) + "'");
            width += glyph.xadvance;
            if (i + 1 < text.length()) {
                width += glyph.getKerning(text.charAt(i + 1));
            }
        }
        return width;
    }

    /**
     * In flight the beam burns the first unit in its column (Level 03's Needlers diving through it, the
     * ship holding still with fire held): the looks' heat target is that unit, and the contact flare
     * follows its size.
     */
    @Test
    void inFlightTheBeamsTargetIsTheHeatTarget() {
        String key = CONTENT.levelKey(3).orElseThrow();
        vanguard.sim.WeaponSpec.Muzzle nose = new vanguard.sim.WeaponSpec.Muzzle(0, 21, 0);
        vanguard.sim.Loadout base =
                vanguard.content.SimSpecs.starterLoadout(CONTENT, vanguard.content.Difficulty.MEDIUM);
        vanguard.sim.Loadout loadout = new vanguard.sim.Loadout(
                base.ship(),
                new vanguard.sim.Armament(List.of(new vanguard.sim.Armament.Mount(
                        vanguard.sim.Armament.Slot.FRONT,
                        vanguard.sim.WeaponSpec.beam("ion-beam", "beam", "beam", 8, 60, nose),
                        vanguard.sim.WeaponSpec.beam("ion-beam", "beam", "beam", 10, 80, nose)))),
                base.shield(),
                base.plating());
        vanguard.sim.Sortie sortie = new vanguard.sim.Sortie(
                1,
                loadout,
                vanguard.content.SimSpecs.level(CONTENT, key, vanguard.content.Difficulty.MEDIUM),
                vanguard.content.SimSpecs.rules(CONTENT, key, vanguard.content.Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
        int burns = 0;
        int inField = 0;
        for (int t = 0; t < vanguard.sim.SimStep.ticks(60); t++) {
            sortie.step(vanguard.sim.Command.FIRE.bit());
            Beam beam = sortie.beam(0);
            if (beam.on() && beam.contact() == Beam.Contact.BURN) {
                burns++;
                int expected = beam.target() < 0 ? ArsenalLooks.GROUND_TARGET : beam.target();
                assertEquals(expected, ArsenalLooks.heatTarget(sortie));
                if (beam.endY() < vanguard.sim.PlayField.HEIGHT) {
                    // A unit on the field, not one entering over the top edge.
                    inField++;
                }
            } else {
                assertEquals(-1, ArsenalLooks.heatTarget(sortie));
            }
        }
        assertTrue(burns > 0, "it burnt something in a minute");
        assertTrue(inField > 0, "cut at a contact on the field");
    }

    /**
     * Round 37, R.4 (a) and (b): at Level 05's Gorgon Frigate the beam burns a head (a part of the boss,
     * which {@code BossLooks} then draws in the heat tint, not the hit flash), and its closed hull stops
     * it without damage: the column then ends at the hull's edge, in the glance spark, not bored in.
     */
    @Test
    void atTheGorgonFrigateTheBeamHeatsAHeadAndGlancesOffTheHull() {
        String key = CONTENT.levelKey(5).orElseThrow();
        vanguard.sim.WeaponSpec.Muzzle nose = new vanguard.sim.WeaponSpec.Muzzle(0, 21, 0);
        vanguard.sim.Loadout base =
                vanguard.content.SimSpecs.starterLoadout(CONTENT, vanguard.content.Difficulty.MEDIUM);
        vanguard.sim.Loadout loadout = new vanguard.sim.Loadout(
                base.ship(),
                new vanguard.sim.Armament(List.of(new vanguard.sim.Armament.Mount(
                        vanguard.sim.Armament.Slot.FRONT,
                        vanguard.sim.WeaponSpec.beam("ion-beam", "beam", "beam", 8, 60, nose),
                        vanguard.sim.WeaponSpec.beam("ion-beam", "beam", "beam", 10, 80, nose)))),
                base.shield(),
                base.plating());
        vanguard.sim.Sortie sortie = new vanguard.sim.Sortie(
                1,
                loadout,
                vanguard.content.SimSpecs.level(CONTENT, key, vanguard.content.Difficulty.MEDIUM),
                vanguard.content.SimSpecs.rules(CONTENT, key, vanguard.content.Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
        int heads = 0;
        int blocked = 0;
        for (int t = 0; t < vanguard.sim.SimStep.ticks(240) && !sortie.complete(); t++) {
            sortie.step(vanguard.sim.Command.FIRE.bit());
            Beam beam = sortie.beam(0);
            if (!beam.on()) {
                continue;
            }
            if (beam.contact() == Beam.Contact.BLOCKED) {
                blocked++;
                assertEquals(beam.endY() - Beam.BORE, ArsenalLooks.blockedEdge(beam), 1e-9, "at the edge");
                continue;
            }
            assertTrue(Double.isNaN(ArsenalLooks.blockedEdge(beam)), "only a blocked beam glances at its end");
            int target = ArsenalLooks.heatTarget(sortie);
            int piece = ArsenalLooks.heatPiece(target);
            if (piece >= 0 && sortie.setPiece(piece).boss().isPresent()) {
                int part = ArsenalLooks.heatPart(target);
                assertTrue(part >= 0 && part < sortie.setPiece(piece).partCount());
                heads++;
            }
        }
        assertTrue(heads > 0, "it burnt a part of the frigate");
        assertTrue(blocked > 0, "the frigate's hull stopped it");
    }
}
