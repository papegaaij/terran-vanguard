package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Difficulty;
import vanguard.sim.ChainFire;
import vanguard.sim.Command;
import vanguard.sim.Eruptions;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * M10 part A, step A3b: the looks of Act 6's gear and hazards (design/player/weapons/plasma-arc; design/player/shields,
 * Reflex Shield (Act 6); design/world/jovian, Hazards &amp; set pieces; design/ui/hud, Act 6 additions): the Plasma
 * Arc's chain from the nose along its struck points, a strip per place in it; the lightning column's tell and strike
 * and the banks seen through after its flash; the eruptions' umbrella and rocks; the foreground banks' smooth cream veil
 * over the {@code air} bodies with their glows back at full; the Reflex tick and the pylon gates' line; and the art the
 * looks read, smooth (the 2026-10-30 dither audit).
 */
class ActSixHazardLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    // The Plasma Arc.

    @Test
    void aDischargeDrawsFromTheNoseAlongItsStrikesForATenthOfASecond() {
        Sortie sortie = JovianFixture.arcFlight();
        ChainArcLooks looks =
                new ChainArcLooks(strips(), new Array<>(0), sortie.armament().size());
        int front = frontMount(sortie);
        int most = 0;
        for (int t = 0; t < SimStep.ticks(20) && most < 3; t++) {
            sortie.step(Command.FIRE.bit());
            looks.stepped(sortie);
            int strikes = strikesNow(sortie, front);
            if (strikes > 0) {
                assertTrue(looks.showing(front, sortie.tick()));
                assertEquals(strikes, looks.strikes(front), "every strike of the step's chain");
                most = Math.max(most, strikes);
            }
        }
        assertTrue(most >= 3, "a chain of three or more over the packed Chimeras: " + most);
        long struck = sortie.tick();
        assertTrue(looks.showing(front, struck + ChainArcLooks.ARC_TICKS - 1));
        assertFalse(looks.showing(front, struck + ChainArcLooks.ARC_TICKS), "0.1 s");
        looks.restart();
        assertFalse(looks.showing(front, struck));
    }

    @Test
    void eachPlaceInTheChainHasItsStripAndTheSparkIsTheArcsOwn() {
        assertEquals("plasma-arc-arc-0", ChainArcLooks.strip("plasma-arc", 0));
        assertEquals(0, ChainArcLooks.place(0));
        assertEquals(4, ChainArcLooks.place(4));
        assertEquals(ChainArcLooks.PLACES - 1, ChainArcLooks.place(9), "a longer chain keeps the thinnest");
        assertEquals(6, ChainArcLooks.PLACES, "the L5 chain's six");
        assertEquals("plasma-arc" + WeaponLooks.SPARK, "plasma-arc-spark");
        assertEquals(ChainFire.event(2, 3), 3 * 16 + 2);
    }

    // The lightning.

    @Test
    void theColumnBrightensThenStrikesAndFades() {
        assertEquals(LightningLooks.TELL_FROM, LightningLooks.tellAlpha(0), 1e-6);
        assertEquals(1, LightningLooks.tellAlpha(1), 1e-6);
        assertEquals(0, LightningLooks.tellFrame(0.9));
        assertEquals(1, LightningLooks.tellFrame(0.4), "the brighter frame for the last 0.4 s");
        int live = SimStep.ticks(0.25);
        assertEquals(0, LightningLooks.strikeFrame(0, 3));
        assertEquals(1, LightningLooks.strikeFrame(2, 3), "two steps a shape");
        assertEquals(0, LightningLooks.strikeFrame(6, 3));
        assertEquals(1, LightningLooks.strikeAlpha(0, live), 1e-6);
        assertEquals(1, LightningLooks.strikeAlpha(live - SimStep.ticks(0.1), live), 1e-6);
        assertTrue(LightningLooks.strikeAlpha(live - 1, live) < 0.25f, "faded over the last 0.1 s");
        assertEquals(1, LightningLooks.skyAt(0), 1e-6);
        assertEquals(0, LightningLooks.skyAt(SimStep.ticks(WeatherLooks.SKY_SECONDS)), 1e-6, "at most 0.1 s");
    }

    @Test
    void aTellsFlashSeesThroughTheBanksForHalfASecond() {
        LightningLooks looks = lightning();
        assertEquals(0, looks.reveal(100), 1e-6);
        looks.told(0, 100);
        assertEquals(LightningLooks.SEE_THROUGH, looks.reveal(100), 1e-6);
        assertTrue(looks.reveal(100 + SimStep.ticks(0.4)) > 0);
        assertEquals(0, looks.reveal(100 + SimStep.ticks(LightningLooks.REVEAL_SECONDS)), 1e-6, "0.5 s");
        looks.restart();
        assertEquals(0, looks.reveal(100), 1e-6);
    }

    // The eruptions.

    @Test
    void theUmbrellaOpensSettlesAndThinsOut() {
        assertEquals(EruptionLooks.UMBRELLA_FROM, EruptionLooks.umbrellaSize(0), 1e-6);
        assertTrue(EruptionLooks.umbrellaSize(EruptionLooks.OPEN_SECONDS) >= EruptionLooks.UMBRELLA_TO - 1e-6);
        assertTrue(EruptionLooks.umbrellaSize(3) > EruptionLooks.umbrellaSize(1), "it settles wider");
        assertEquals(0, EruptionLooks.umbrellaAlpha(EruptionLooks.UMBRELLA_SECONDS), 1e-6);
        assertTrue(EruptionLooks.umbrellaAlpha(0.5) > EruptionLooks.umbrellaAlpha(3));
        assertEquals(EruptionLooks.SMALL, EruptionLooks.rockScale(Eruptions.Phase.RISE, 0), 1e-6);
        assertEquals(1, EruptionLooks.rockScale(Eruptions.Phase.RISE, 1), 1e-6, "grown as it flies");
        assertEquals(1, EruptionLooks.rockScale(Eruptions.Phase.FLY, 0.5), 1e-6);
        assertEquals(EruptionLooks.SMALL, EruptionLooks.rockScale(Eruptions.Phase.FALL, 1), 1e-6);
    }

    // The foreground banks.

    @Test
    void aBodyUnderABankReadsAtAThirdAndItsGlowsAtFull() {
        BackdropData.Bank bank = new BackdropData.Bank(10, 240, 200, 120, Optional.empty());
        BackdropData.Bank hard = new BackdropData.Bank(20, 100, 120, 80, Optional.of(List.of("easy")));
        ForegroundBanks banks = new ForegroundBanks(
                difficulty -> List.of(bank, hard).stream()
                        .filter(b -> b.on(difficulty))
                        .toList(),
                t -> 30 * t,
                1.5);
        banks.update(300, 0);
        assertTrue(banks.showing());
        double y = ForegroundBanks.centreY(300, 300, 1.5);
        assertEquals(PlayField.HEIGHT / 2.0, y, 1e-9, "the middle of the screen at its time");
        assertEquals(
                PlayField.HEIGHT / 2.0 - 1.5 * 30, ForegroundBanks.centreY(300, 330, 1.5), 1e-9, "high-air's drift");
        float cover = banks.cover(240, y);
        assertEquals(ForegroundBanks.PEAK, cover, 1e-6, "its middle");
        assertEquals(0.35, 1 - cover, 0.01, "a body under it reads at about 35 %");
        assertEquals(0, banks.cover(240, y + 61), 1e-6, "outside it");
        assertTrue(banks.cover(240 + 95, y) < cover / 2, "its edge fades");
        banks.update(300, LightningLooks.SEE_THROUGH);
        assertEquals(ForegroundBanks.PEAK * (1 - LightningLooks.SEE_THROUGH), banks.cover(240, y), 1e-6, "a flash");
        banks.difficulty(Difficulty.EASY);
        banks.update(600, 0);
        assertEquals(0, banks.cover(100, ForegroundBanks.centreY(600, 600, 1.5)), 1e-6, "easy leaves it out");
    }

    @Test
    void theVeilIsASmoothCreamDrawnOnce() {
        int size = ForegroundBanks.SIZE;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Color colour = new Color();
        java.util.Set<Integer> levels = new java.util.HashSet<>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double u = (x + 0.5) / size;
                double v = 1 - (y + 0.5) / size;
                ForegroundBanks.shade(u, v, colour);
                int a = Math.round(ForegroundBanks.veilAlpha(u, v) * 255);
                levels.add(a);
                image.setRGB(
                        x,
                        y,
                        a << 24
                                | Math.round(colour.r * 255) << 16
                                | Math.round(colour.g * 255) << 8
                                | Math.round(colour.b * 255));
            }
        }
        assertTrue(levels.size() >= 32, "at least 32 steps: " + levels.size());
        for (int step = 1; step <= 2; step++) {
            assertTrue(SovereignLooksTest.checkerShare(image, step) < 0.02, step + " px checker");
        }
        assertEquals(1, ForegroundBanks.veilAlpha(0.5, 0.5), 1e-6);
        assertEquals(0, ForegroundBanks.veilAlpha(0, 0.5), 1e-6);
        Color top = new Color();
        Color bottom = new Color();
        ForegroundBanks.shade(0.5, 1, top);
        ForegroundBanks.shade(0.5, 0, bottom);
        assertTrue(top.r + top.g + top.b > bottom.r + bottom.g + bottom.b, "lit on top, a warmer cream below");
        assertTrue(bottom.r > bottom.b && top.r >= top.b, "cream, never a cold grey");
    }

    // The HUD.

    @Test
    void theReflexTickSitsAtThreeQuartersAndTheTopIsGoldWhileTheWindowIsOpen() {
        int cells = HudKit.INNER_WIDTH / HudKit.SEGMENT_PITCH;
        assertEquals(Math.round(cells * 0.75), ReflexTick.tickCell(cells, 0.75));
        assertTrue(ReflexTick.open(30, 40, 0.75), "30 of 40");
        assertFalse(ReflexTick.open(29.9, 40, 0.75));
        assertTrue(ReflexTick.open(60, 80, 0.75), "an overcharged Reflex: 60 of 80");
        assertFalse(ReflexTick.open(40, 80, 0.75), "a full bar is not the window while overcharged");
        assertFalse(ReflexTick.open(40, 40, 0), "no Reflex fitted");
    }

    @Test
    void thePylonGatesCountToTheGatesTotalAndPipEachOpenGate() {
        assertEquals("0 / 2", GateTracker.count(0, 2));
        assertEquals("1 / 2", GateTracker.count(1, 2));
        assertEquals(GateTracker.OPEN, GateTracker.count(2, 2));
        assertTrue(GateTracker.open(0, 1, 3), "a gate before the current is open");
        assertFalse(GateTracker.open(1, 1, 3), "the current while a pylon stands");
        assertTrue(GateTracker.open(1, 1, 0), "the current once its last pylon fell");
        assertFalse(GateTracker.open(2, 1, 0), "a later gate");
        assertTrue(GateTracker.counting(true, 2, -1), "the halt runs");
        assertFalse(GateTracker.counting(false, 2, -1), "between gates only the pips");
        assertTrue(GateTracker.counting(false, 0, 10), "OPEN flashes");
        assertFalse(GateTracker.counting(false, 0, GateTracker.FLASH_FRAMES));
    }

    // The art.

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        for (int j = 0; j < ChainArcLooks.PLACES; j++) {
            assertSet(ChainArcLooks.strip("plasma-arc", j), 3, 160, 48);
        }
        assertSet("plasma-arc-spark", 6, 64, 64);
        assertSet("plasma-arc-glow", 2, 16, 16);
        assertSet(ShipLooks.REFLEX_RING, 4, 60, 60);
        assertSet(LightningLooks.TELL, 2, 48, 64);
        assertSet(LightningLooks.STRIKE, 3, 112, PlayField.HEIGHT);
    }

    @Test
    void theGlowsAreSmoothAndDrawnOnce() throws IOException {
        List<Path> glows = new ArrayList<>();
        for (int j = 0; j < ChainArcLooks.PLACES; j++) {
            glows.addAll(frames(ChainArcLooks.strip("plasma-arc", j)));
        }
        glows.addAll(frames("plasma-arc-spark"));
        glows.addAll(frames("plasma-arc-glow"));
        glows.addAll(frames(ShipLooks.REFLEX_RING));
        glows.addAll(frames(LightningLooks.TELL));
        glows.addAll(frames(LightningLooks.STRIKE));
        for (Path file : glows) {
            BufferedImage image = ImageIO.read(file.toFile());
            for (int step = 1; step <= 2; step++) {
                double share = SovereignLooksTest.checkerShare(image, step);
                assertTrue(share < 0.02, file.getFileName() + ": a " + step + " px checker on " + share + " of it");
            }
        }
    }

    // Helpers.

    private static int frontMount(Sortie sortie) {
        for (int m = 0; m < sortie.armament().size(); m++) {
            if (sortie.armament().mount(m).fires()
                    && sortie.armament().mount(m).weapon().delivery() == WeaponSpec.Delivery.CHAIN) {
                return m;
            }
        }
        throw new IllegalStateException("no Plasma Arc");
    }

    /** The strikes of mount {@code m}'s chain in this step's events, each in its place (0 first). */
    private static int strikesNow(Sortie sortie, int m) {
        SimEvents events = sortie.events();
        int n = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.CHAIN_STRIKE && ChainFire.eventMount(events.value(i)) == m) {
                assertEquals(n, ChainFire.eventIndex(events.value(i)), "in chain order");
                n++;
            }
        }
        return n;
    }

    /** Stand-in strips (three shapes a place) for the looks' bookkeeping; nothing is drawn. */
    private static List<Array<AtlasRegion>> strips() {
        List<Array<AtlasRegion>> strips = new ArrayList<>();
        for (int j = 0; j < ChainArcLooks.PLACES; j++) {
            strips.add(Array.with(new AtlasRegion[3]));
        }
        return strips;
    }

    private static LightningLooks lightning() {
        return new LightningLooks(new Array<>(0), new Array<>(0), null, 2);
    }

    private static void assertSet(String name, int count, int width, int height) throws IOException {
        List<Path> files = frames(name);
        assertEquals(count, files.size(), name);
        for (Path file : files) {
            BufferedImage image = ImageIO.read(file.toFile());
            assertEquals(width, image.getWidth(), file.getFileName().toString());
            assertEquals(height, image.getHeight(), file.getFileName().toString());
        }
    }

    private static List<Path> frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.filter(path ->
                            path.getFileName().toString().matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .sorted()
                    .toList();
        }
    }
}
