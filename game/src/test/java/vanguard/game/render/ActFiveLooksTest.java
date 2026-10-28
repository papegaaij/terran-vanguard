package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.ui.Fonts;
import vanguard.sim.Command;
import vanguard.sim.Loadout;
import vanguard.sim.PowerState;
import vanguard.sim.Sortie;

/**
 * M9 part A's looks (step A3; design/enemies Enemy shields, design/player/weapons tesla-coil-pod and swarm-tail,
 * design/art-direction the homing diamond and the vacuum set, design/ui/hud Act 5 additions; tools/art/act5_gear.py,
 * explosions.py {@code --vacuum}): the art's frames as the looks count them; the shield's size tiers, its opacity and
 * animations' frames; the diamond's and the exhaust's headings from the simulation's; the arc's turn; the vacuum
 * wreck's fade; the laser line's telegraph; the steady interference's tears; and the HUD's power row under a drain:
 * its label and readouts fit, its cells, and a real drain's readout, flash, muzzle flicker and dimmed ring.
 */
class ActFiveLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final BitmapFont.BitmapFontData SMALL = font(Fonts.LABEL_FILE);
    private static final BitmapFont.BitmapFontData BODY = font(Fonts.BODY_FILE);
    /** The label plate (assets/sprites/hud/plate.png) is 120 px wide, its text indented 4 px. */
    private static final int PLATE_WIDTH = 120;

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        assertEquals(3, frames("tesla-coil-pod-arc"), "three shapes in turn");
        assertEquals(6, frames(WeaponLooks.ARC_SPARK));
        assertEquals(2, frames("tesla-coil-pod-glow"));
        assertEquals(5, frames("pod-tesla-coil-left"), "the pod per banking frame");
        assertEquals(5, frames("pod-tesla-coil-right"));
        for (String tier : EnemyShieldLooks.TIERS) {
            assertEquals(4, frames("enemy-shield-" + tier), tier);
            assertEquals(6, frames("enemy-shield-" + tier + "-break"), tier);
            assertEquals(8, frames("enemy-shield-" + tier + "-refill"), tier);
        }
        assertEquals(6, frames("enemy-shield-hit"));
        assertEquals(DiamondLooks.HEADINGS, frames(DiamondLooks.ROOT));
        assertEquals(2 * DiamondLooks.HEADINGS, frames(DiamondLooks.ROOT + "-exhaust"), "heading x 2 + flicker");
        assertEquals(5, frames(DiamondLooks.ROOT + "-pop"));
        assertEquals(5, frames(DiamondLooks.ROOT + "-fizzle"));
        assertEquals(32, frames("swarm-tail-shot"), "32 headings");
        for (String rung : VacuumDeaths.RUNGS) {
            long bursts = frames("explosion-vacuum-" + rung);
            assertTrue(bursts > 0, rung);
            assertEquals(bursts, frames("explosion-vacuum-debris-" + rung), "the debris keeps the burst's timing");
        }
    }

    @Test
    void aShieldsTierIsItsUnitsSizeAndItsOpacityFollowsItsFill() {
        assertEquals(0, EnemyShieldLooks.tier(40), "a Talon-sized unit: the small bubble");
        assertEquals(0, EnemyShieldLooks.tier(48));
        assertEquals(1, EnemyShieldLooks.tier(49));
        assertEquals(1, EnemyShieldLooks.tier(84));
        assertEquals(2, EnemyShieldLooks.tier(104));
        assertEquals(EnemyShieldLooks.HOLD_LOW, EnemyShieldLooks.holdOpacity(0), 1e-6);
        assertEquals(1, EnemyShieldLooks.holdOpacity(1), 1e-6);
        assertTrue(EnemyShieldLooks.holdOpacity(0.5) > EnemyShieldLooks.holdOpacity(0.25));
        // The shimmer loops at 6 steps a frame, staggered by the unit.
        assertEquals(0, EnemyShieldLooks.holdFrame(5, 0, 4));
        assertEquals(1, EnemyShieldLooks.holdFrame(6, 0, 4));
        assertEquals(0, EnemyShieldLooks.holdFrame(24, 0, 4));
        assertEquals(1, EnemyShieldLooks.holdFrame(0, 1, 4));
        // The break plays 0.2 s (6 frames x 2 steps), the refill 0.4 s (8 x 3) then the holding look.
        assertEquals(5, EnemyShieldLooks.frameAt(11, EnemyShieldLooks.BREAK_FRAME_TICKS, 6));
        assertEquals(-1, EnemyShieldLooks.frameAt(12, EnemyShieldLooks.BREAK_FRAME_TICKS, 6));
        assertEquals(7, EnemyShieldLooks.frameAt(23, EnemyShieldLooks.REFILL_FRAME_TICKS, 8));
        assertEquals(-1, EnemyShieldLooks.frameAt(24, EnemyShieldLooks.REFILL_FRAME_TICKS, 8));
        assertEquals(-1, EnemyShieldLooks.frameAt(-1, 2, 6), "not yet");
    }

    /** The art's headings run clockwise from up; the simulation's missile heading clockwise from straight down. */
    @Test
    void theDiamondFacesWhereItFlies() {
        int n = DiamondLooks.HEADINGS;
        assertEquals(n / 2, DiamondLooks.heading(0, n), "flying down: nose down");
        assertEquals(0, DiamondLooks.heading(Math.PI, n), "flying up: nose up");
        assertEquals(3 * n / 4, DiamondLooks.heading(Math.PI / 2, n), "flying left (x falls): nose left");
        assertEquals(n / 4, DiamondLooks.heading(-Math.PI / 2, n), "flying right: nose right");
        assertEquals(2 * 5, DiamondLooks.exhaustFrame(5, 0, 0));
        assertEquals(2 * 5 + 1, DiamondLooks.exhaustFrame(5, DiamondLooks.FLICKER_TICKS, 0), "the flicker");
    }

    @Test
    void theArcTurnsFromItsPodToItsTarget() {
        assertEquals(0, ArcLooks.degrees(0, 0, 100, 0), 1e-6);
        assertEquals(90, ArcLooks.degrees(0, 0, 0, 100), 1e-6, "straight up the screen");
        assertEquals(180, Math.abs(ArcLooks.degrees(0, 0, -50, 0)), 1e-6);
        assertEquals(0, ArcLooks.shapeOf(3, 3), "the three shapes in turn");
        assertEquals(2, ArcLooks.shapeOf(2, 3));
    }

    @Test
    void aWreckInVacuumDriftsThenFadesOverItsLastSecond() {
        assertEquals(1, VacuumDeaths.wreckOpacity(0), 1e-6);
        assertEquals(1, VacuumDeaths.wreckOpacity(VacuumDeaths.WRECK_SECONDS - VacuumDeaths.WRECK_FADE_SECONDS), 1e-6);
        assertEquals(0.5, VacuumDeaths.wreckOpacity(VacuumDeaths.WRECK_SECONDS - 0.5), 1e-6);
        assertEquals(0, VacuumDeaths.wreckOpacity(VacuumDeaths.WRECK_SECONDS), 1e-6);
    }

    @Test
    void theLaserLinesTelegraphBrightensThenFlickersBeforeItFires() {
        assertEquals(LineLooks.TELEGRAPH_LOW, LineLooks.telegraphOpacity(0, 0), 1e-6);
        assertTrue(LineLooks.telegraphOpacity(0.7, 0) > LineLooks.telegraphOpacity(0.3, 0));
        float lit = LineLooks.telegraphOpacity(0.9, 0);
        float dark = LineLooks.telegraphOpacity(0.9, 2);
        assertTrue(dark < lit, "its last fifth flickers");
    }

    /** The steady interference's tear: 2.5 to 4 s apart, 0.1 s long, never in the transmission's first second. */
    @Test
    void theInterferenceTearsEveryFewSeconds() {
        for (long k = 1; k < 60; k++) {
            double gap = TransmissionStatic.tearStart(k + 1) - TransmissionStatic.tearStart(k);
            assertTrue(gap >= 2.5 - 1e-9 && gap <= 4.0 + 1e-9, "tear " + k + ": " + gap);
            int height = TransmissionStatic.tearHeight(k);
            assertTrue(height >= 2 && height <= 4, "2-4 px: " + height);
        }
        int tearing = 0;
        for (int step = 0; step < 60 * 60; step++) {
            tearing += TransmissionStatic.tearing(step / 60.0) ? 1 : 0;
        }
        assertTrue(tearing >= 6 * 15 && tearing <= 6 * 25, "about 6 steps a tear over a minute: " + tearing);
        for (int step = 0; step < 60; step++) {
            assertFalse(TransmissionStatic.tearing(step / 60.0), "not in the first second");
        }
    }

    @Test
    void thePowerRowsLabelAndReadoutsFitBesideThePlate() {
        int label = width(SMALL, PowerRow.LABEL);
        assertTrue(label <= PLATE_WIDTH - 2 * HudKit.PLATE_INDENT, "on the plate: " + label);
        int room = HudKit.INNER_WIDTH - (PLATE_WIDTH - HudKit.PLATE_INDENT);
        for (String readout : new String[] {PowerRow.REGEN_OFF, "FIRE 50%", "FIRE 88%", "14/20", "22.5/24"}) {
            assertTrue(width(BODY, readout) <= room, readout + " beside the plate: " + width(BODY, readout));
        }
        assertEquals("14/20", PowerRow.readout(1, false, 14, 20));
        assertEquals(PowerRow.REGEN_OFF, PowerRow.readout(1, true, 14, 20));
        assertEquals("FIRE 88%", PowerRow.readout(0.875, true, 14, 20), "as the HUD's draft writes it");
        assertEquals("FIRE 99%", PowerRow.readout(0.999, true, 14, 20), "never 100 while slowed");
        assertEquals("FIRE 50%", PowerRow.readout(0.5, true, 14, 20));
        assertEquals("14.5/20", PowerRow.readout(1, false, 14.5, 20));
        int cells = PowerRow.cells(HudKit.INNER_WIDTH);
        assertEquals(16, cells);
        assertEquals(cells, PowerRow.cellsOf(20, 20, cells), "the scale is the generator's output");
        assertEquals(11, PowerRow.effectiveCells(14, 20, cells), "E = 14 of 20: 6 MW drained, the top 5 cells hatched");
        assertEquals(cells, PowerRow.effectiveCells(20, 20, cells), "nothing drained: none hatched");
        assertEquals(12, PowerRow.cellsOf(14.5, 20, cells), "a load lights any cell it reaches");
    }

    /**
     * A real drain on the hand plan's 20/20 fit: three drainers cut E to 14 and the fire rate to 0.875; the row reads
     * the factor and flashes, the muzzle flashes flicker and the shield's ring is dimmed; shaken off, all clears.
     */
    @Test
    void aDrainShowsInTheHudAndOnTheShip() {
        String shield = CONTENT.shields().models().get(2).name();
        Loadout loadout = SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM)
                .withPower(SimSpecs.power(CONTENT, 20, 20, shield, Difficulty.MEDIUM));
        String key = CONTENT.levelKey(1).orElseThrow();
        Sortie sortie = new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, key, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, key, Difficulty.MEDIUM),
                60);
        sortie.step(Command.NONE);
        PowerState power = sortie.power().orElseThrow();
        assertFalse(PowerRow.draining(power), "no drainer: the spare-power row");
        assertFalse(PlayerPasses.slowed(sortie));
        for (int i = 0; i < 3; i++) {
            sortie.drain(+1);
        }
        sortie.step(Command.NONE);
        assertTrue(PowerRow.draining(power));
        assertEquals("FIRE 88%", PowerRow.readout(power));
        assertTrue(power.regenStopped(), "the ring dims");
        assertTrue(PowerRow.flashing(power, 0) && !PowerRow.flashing(power, PowerRow.FLASH_PHASE_TICKS));
        assertTrue(PlayerPasses.slowed(sortie), "the muzzle flashes flicker");
        assertTrue(PlayerPasses.flickerOff(2) && !PlayerPasses.flickerOff(0));
        for (int i = 0; i < 3; i++) {
            sortie.drain(-1);
        }
        sortie.step(Command.NONE);
        assertFalse(PowerRow.draining(power));
        assertFalse(PlayerPasses.slowed(sortie));
        assertFalse(power.regenStopped());
    }

    @Test
    void theInterceptTagFitsBesideThePortrait() {
        assertTrue(width(SMALL, MissionPanel.INTERCEPT) <= MissionLayout.NAME_WIDTH);
        assertTrue(width(BODY, "VORNE") <= MissionLayout.NAME_WIDTH);
    }

    private static BitmapFont.BitmapFontData font(String file) {
        return new BitmapFont.BitmapFontData(
                new FileHandle(new File(System.getProperty("vanguard.assetsDir", "../assets"), file)), false);
    }

    static int width(BitmapFont.BitmapFontData font, String text) {
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
}
