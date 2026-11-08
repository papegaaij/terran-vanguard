package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.RadioBlock;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.PartSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F4: Level 48 on the play field (design/campaign Level 48; design/enemies/bosses/vornes-chimera;
 * tools/art/act7_citadel.py): the art the looks draw; the pivots' parts where the data puts them; the Chimera's frames
 * by its bank, rise, fusion, opening and last words; the bar in three segments by phase; the spire well's frames by the
 * boss's arrival and the cores' veins going dark; the reactor cores cracked and flickering in the bottom band; the rear
 * lane's chevrons; the tracker's {@code CORES n / 8}; Vorne's fused portrait on his lines; and gold on gold: the
 * citadel's wreckage paler and duller than the boss's gold.
 */
class Level48LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-48");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(48).orElseThrow();
    private static final String C = ChimeraLooks.SLUG;

    private static long frames(Path dir, String name) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static long frames(String name) throws IOException {
        return frames(SPRITES, name);
    }

    private static boolean single(String name) {
        return Files.isRegularFile(SPRITES.resolve(name + ".png"));
    }

    private static JsonValue pivots() throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + C + ".json")));
    }

    private static SetPieceSpec chimera() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        return script.setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        for (String body : List.of("", "-tissue", "-open")) {
            assertEquals(ChimeraLooks.BANKS, frames(C + body), "five banks of " + body);
        }
        assertEquals(6, frames(C + "-opening"), "the capsule's 2 s");
        assertEquals(3, frames(C + "-battery"), "whole, damaged, dead");
        for (String set : List.of("-panel", "-panel-down", "-panel-cracks", "-panel-glow", "-panel-rim")) {
            assertEquals(18, frames(C + set), set);
        }
        assertEquals(6, frames(C + "-rack"), "shut .. open, damaged, dead");
        assertEquals(6, frames(C + "-bay"));
        assertEquals(5, frames(C + "-sac"), "rest, three swelling, split");
        assertEquals(6, frames(C + "-sac-grow"));
        assertEquals(6, frames(C + "-sac-burst"));
        assertEquals(8, frames(C + "-fusion"));
        assertEquals(9, frames(C + "-closing"), "K's 4.5 s at 2 fps");
        assertEquals(8, frames(C + "-shield"));
        assertEquals(6, frames(C + "-muzzle"));
        assertEquals(4, frames(C + "-port-flash"));
        assertEquals(8, frames(C + "-pulse"));
        assertEquals(8, frames(C + "-flare-l"));
        assertEquals(8, frames(C + "-flare-r"));
        assertEquals(8, frames(C + "-sweep-mark"));
        assertEquals(10, frames(C + "-burst"));
        assertEquals(8, frames(C + "-plate"));
        for (String set : List.of(
                "-panel-sweep",
                "-rack-lock",
                "-bay-light",
                "-sac-dead",
                "-sac-core",
                "-sac-heal",
                "-sac-flare",
                "-capsule-cracks")) {
            assertTrue(single(C + set), set);
        }
        assertEquals(2, frames("reactor-core"), "whole, cracked");
        assertTrue(single("reactor-core-stump"));
        assertEquals(8, frames("reactor-core-glow"));
        assertEquals(8, frames("reactor-core-death"));
        assertEquals(8, frames("reactor-core-tatters"));
        assertEquals(3, frames("vorne-archive"), "sealed, cracked, broken open");
        assertEquals(8, frames("vorne-archive-break"));
        assertEquals(CitadelLooks.WELL_FRAMES, frames(BACKDROP, CitadelLooks.WELL));
        assertEquals(CitadelLooks.WELL_FRAMES, frames(BACKDROP, CitadelLooks.WELL + CitadelLooks.GLOW));
        for (String vein : List.of(CitadelLooks.VEIN_L, CitadelLooks.VEIN_R)) {
            assertTrue(Files.isRegularFile(BACKDROP.resolve(vein + ".png")), vein);
            assertEquals(CitadelLooks.VEIN_FRAMES, frames(BACKDROP, vein + CitadelLooks.GLOW), vein);
        }
        for (String expression : List.of("neutral", "smug", "rage")) {
            assertTrue(single("portraits/radio-vorne-fused-" + expression), expression);
            assertTrue(single("portraits/briefing-vorne-fused-" + expression), expression);
        }
    }

    @Test
    void thePivotsPartsSitWhereTheDataPutsThem() throws IOException {
        SetPieceSpec piece = chimera();
        JsonValue parts = pivots().get("parts");
        for (PartSpec part : piece.parts()) {
            JsonValue at = parts.get(part.name());
            assertNotNull(at, part.name());
            assertEquals(part.dx(), at.getDouble(0), 0.5, part.name() + " x");
            // The pivots are y down, the data y up; the sealed sacs' boxes sit up to 3 px above their sprites (F2's
            // rule).
            assertEquals(part.dy(), -at.getDouble(1), 3.5, part.name() + " y");
        }
        ChimeraLooks looks = ChimeraLooks.following(piece, pivots());
        assertTrue(looks.capsule >= 0 && looks.rack >= 0 && looks.bay >= 0, "every part found by name");
        for (int p : looks.batteries) {
            assertTrue(p >= 0);
        }
        for (int p : looks.sacs) {
            assertTrue(p >= 0);
        }
        assertEquals(4, looks.ports.length, "the four gun ports");
        assertTrue(looks.noseAt[1] < 0, "the nose below the centre (nose down)");
        assertTrue(ChimeraLooks.drawn(piece), "ChimeraLooks draws it, not BossLooks");
    }

    @Test
    void theChimerasFramesFollowItsFight() {
        assertEquals(2, ChimeraLooks.bankFrame(0), "upright at rest");
        assertEquals(4, ChimeraLooks.bankFrame(90), "the sway's fastest banks it fully, moving right");
        assertEquals(0, ChimeraLooks.bankFrame(-200), "the rear sweep's return");
        assertEquals(3, ChimeraLooks.bankFrame(45));
        assertEquals(ChimeraLooks.RISE_SCALE, ChimeraLooks.riseScale(0), 1e-6, "out of the well's depth");
        assertEquals(1, ChimeraLooks.riseScale(1), 1e-6);
        assertTrue(ChimeraLooks.riseShade(0.5) > ChimeraLooks.riseShade(0.2), "out of the well's dark");
        assertEquals(0, ChimeraLooks.fusionFrame(0.2, 8), "the fusion's tell");
        assertEquals(1, ChimeraLooks.fusionFrame(0.7, 8));
        assertEquals(2, ChimeraLooks.fusionFrame(1.0, 8), "the burst out of the sockets");
        assertEquals(7, ChimeraLooks.fusionFrame(2.4, 8));
        assertEquals(-1, ChimeraLooks.fusionFrame(2.6, 8), "faded by the opening's end");
        assertEquals(-1, ChimeraLooks.growFrame(0.5, 6), "the sacs grow after the tell");
        assertEquals(5, ChimeraLooks.growFrame(2.9, 6), "grown by the 3 s opening's end");
        assertEquals(0, ChimeraLooks.openingFrame(0, 6));
        assertEquals(5, ChimeraLooks.openingFrame(1.95, 6), "open by the 2 s opening's end");
        assertEquals(0, ChimeraLooks.closingFrame(0, 4.5, 9), "K");
        assertEquals(4, ChimeraLooks.closingFrame(2.25, 4.5, 9));
        assertEquals(8, ChimeraLooks.closingFrame(4.5, 4.5, 9), "closed at the burst");
        assertEquals(0, ChimeraLooks.markFrame(0, 8));
        assertEquals(7, ChimeraLooks.markFrame(1, 8), "the mark brightest as it rises");
    }

    @Test
    void theSpireWellAndTheCoresVeinsFollowTheLevel() {
        assertEquals(0, CitadelLooks.wellFrameAt(Double.NaN), "shut until the sting");
        assertEquals(1, CitadelLooks.wellFrameAt(0));
        assertEquals(8, CitadelLooks.wellFrameAt(1.99), "the lid split over 2 s");
        assertEquals(9, CitadelLooks.wellFrameAt(2.0), "open");
        assertEquals(11, CitadelLooks.wellFrameAt(2.5));
        assertEquals(9, CitadelLooks.wellFrameAt(2.75), "the throat's loop");
        assertEquals(0, CitadelLooks.veinFrame(0, Double.NaN));
        assertEquals(11, CitadelLooks.veinFrame(1.4, Double.NaN), "one pulse up the vein in 1.5 s");
        assertEquals(0, CitadelLooks.veinFrame(1.5, Double.NaN));
        assertEquals(CitadelLooks.DARK_FIRST, CitadelLooks.veinFrame(9, 0), "dark from the core outward");
        assertEquals(17, CitadelLooks.veinFrame(9, 0.99));
        assertEquals(-1, CitadelLooks.veinFrame(9, 1.01), "then not drawn: the dark channel");

        LevelData level = CONTENT.level(KEY);
        String[] names = level.backdrop().placements().stream()
                .filter(placed ->
                        level.backdrop().pieces().get(placed.piece()).layer() == vanguard.content.BackdropLayer.GROUND)
                .map(placed -> placed.piece())
                .toArray(String[]::new);
        CitadelLooks citadel = CitadelLooks.of(names, name -> null);
        assertNotNull(citadel);
        assertEquals(10, citadel.veins(), "one vein for each of the ten cores");
        // Two veins drawn side by side on the screen: a core's death darkens the one whose bottom centre it sits on.
        citadel.drawVein(null, 0, 78, 300, 118, false, 0);
        citadel.drawVein(null, 1, 316, 310, 356, false, 0);
        citadel.coreDied(356, 312, 100);
        assertFalse(citadel.dark(0));
        assertTrue(citadel.dark(1));
        citadel.coreDied(240, 300, 101);
        assertFalse(citadel.dark(0), "a death off every vein darkens none");
        citadel.restart();
        assertFalse(citadel.dark(1), "lit again for a new attempt");
    }

    @Test
    void theReactorCoresCrackAndFlickerNearTheBottomEdge() {
        assertFalse(ReactorCoreLooks.cracked(40, 40));
        assertTrue(ReactorCoreLooks.cracked(19, 40), "cracked below half");
        assertEquals(1, ReactorCoreLooks.glowShare(121, 120, 5, 3), 1e-6, "full above the band");
        double least = 1;
        double most = 0;
        for (long tick = 0; tick < 120; tick++) {
            double share = ReactorCoreLooks.glowShare(60, 120, tick, 3);
            least = Math.min(least, share);
            most = Math.max(most, share);
            assertTrue(share >= ReactorCoreLooks.FLICKER_LOW - 1e-6 && share <= 1 + 1e-6);
        }
        assertTrue(most - least > 0.4, "flickering in the bottom band: " + least + ".." + most);
        assertEquals(1, ReactorCoreLooks.glowShare(10, 0, 5, 3), 1e-6, "no band, no flicker");
        assertEquals(
                120, CONTENT.level(KEY).leaving().orElseThrow().bottomBand().orElseThrow(), 1e-9);
        assertEquals("0 / 8", MissionPanel.escapesCount(0, 10, 2), "CORES n / 8 on medium");
        assertEquals(
                "CORES",
                CONTENT.level(KEY)
                        .objectives()
                        .secondary()
                        .orElseThrow()
                        .label()
                        .orElseThrow());
    }

    @Test
    void theRearLanesChevronsSitOverItsLaneInsideTheField() {
        assertEquals(240, EdgeWarnings.rearMiddle(Double.NaN), 1e-6, "no lane: the middle");
        assertEquals(350, EdgeWarnings.rearMiddle(350), 1e-6, "over the lane");
        assertEquals(60, EdgeWarnings.rearMiddle(10), 1e-6, "kept inside the field");
        assertEquals(419, EdgeWarnings.rearMiddle(470), 1e-6);
    }

    @Test
    void vornesLinesShowHisFusedPortrait() {
        int vorne = 0;
        for (RadioBlock.RadioCue cue : CONTENT.level(KEY).radio()) {
            if (cue.speaker().equals("Vorne")) {
                vorne++;
                assertEquals("vorne-fused", cue.portrait().orElseThrow(), cue.line());
            }
        }
        assertEquals(8, vorne, "his lines (the fourth is the Choir's)");
    }

    @Test
    void theFightsStartFollowsTheArrival() {
        Sortie sortie = sortie();
        SetPieceSpec spec = chimera();
        LevelData level = CONTENT.level(KEY);
        String[] names = level.backdrop().placements().stream()
                .filter(placed ->
                        level.backdrop().pieces().get(placed.piece()).layer() == vanguard.content.BackdropLayer.GROUND)
                .map(placed -> placed.piece())
                .toArray(String[]::new);
        CitadelLooks citadel = CitadelLooks.of(names, name -> null);
        int arrivedFrames = 0;
        boolean shutBefore = true;
        int openFrame = -1;
        double lastRise = 0;
        // To the Chimera in place (the clock halts at the arena's end while it lives).
        int limit = vanguard.sim.SimStep.ticks(195);
        for (int step = 0; step < limit && !(chimeraOf(sortie).bossSeconds() >= 4.5); step++) {
            sortie.step(hold(sortie));
            citadel.stepped(sortie);
            SetPiece piece = chimeraOf(sortie);
            int frame = citadel.wellFrame(CitadelLooks.WELL_FRAMES);
            if (piece == null || piece.bossSeconds() < 0) {
                shutBefore &= frame == 0;
            } else {
                arrivedFrames++;
                assertTrue(piece.riseShare() >= lastRise, "rising");
                lastRise = piece.riseShare();
                openFrame = frame;
            }
        }
        assertTrue(shutBefore, "the well shut until the sting");
        assertTrue(arrivedFrames > 0, "the Chimera arrives at the sting");
        assertEquals(1, chimeraOf(sortie).riseShare(), 1e-9, "in place");
        assertTrue(openFrame >= CitadelLooks.OPEN_FIRST, "open once the Chimera is in place: " + openFrame);
        // The bar in three segments, as wide as the phases' HP: the gold, the tissue, the capsule.
        SetPiece piece = chimeraOf(sortie);
        BossBar.Segments segments = new BossBar.Segments(3);
        BossLooks.phases(piece, segments);
        double total = segments.max[0] + segments.max[1] + segments.max[2];
        assertEquals(1040 / 2250.0, segments.max[0] / total, 0.01, "the gold (step F5's HP)");
        assertEquals(690 / 2250.0, segments.max[1] / total, 0.01, "the tissue");
        assertEquals(520 / 2250.0, segments.max[2] / total, 0.01, "the capsule");
        for (int s = 0; s < 3; s++) {
            assertEquals(segments.max[s], segments.hp[s], 1e-6, "full at the start");
            assertEquals(0, segments.flash[s], 1e-6);
        }
        assertTrue(ChimeraLooks.drawn(spec));
        assertEquals(1, LastWordsFade.alpha(sortie), 1e-6, "no fade before K");
    }

    /**
     * Step F6: the parts' guns read on the field. The rise volley flashes the batteries and the bay while it rises (none
     * from the rack, the sacs or the capsule); in phase 1 every part flashes as its own gun fires (the rack's homing
     * diamond included), each flash at most {@link ChimeraFire#FLASH_TICKS} steps; and the told moves come in both lanes
     * (step F5's two at once), each found by {@link ChimeraLooks#lane}.
     */
    @Test
    void thePartsGunsFlashAsTheyFireAndTheTellsFollowBothLanes() throws IOException {
        Sortie sortie = sortie();
        ChimeraLooks looks = ChimeraLooks.following(chimera(), pivots());
        int parts = chimera().parts().size();
        int[] riseFlashes = new int[parts];
        int[] fightFlashes = new int[parts];
        boolean[] lanes = new boolean[vanguard.sim.BossPlaybook.LANES];
        int limit = vanguard.sim.SimStep.ticks(215);
        for (int step = 0; step < limit; step++) {
            sortie.step(hold(sortie));
            looks.stepped(sortie);
            SetPiece piece = chimeraOf(sortie);
            if (piece == null || piece.bossSeconds() < 0) {
                continue;
            }
            assertEquals(0, piece.phase(), "phase 1 all through: the guns are held");
            for (int p = 0; p < parts; p++) {
                int since = looks.flashTicks(p, sortie.tick());
                assertTrue(since < ChimeraFire.FLASH_TICKS, "a flash is short");
                if (since == 0) {
                    assertTrue(piece.partHp(p) > 0, "only a living part fires");
                    (piece.riseShare() < 1 ? riseFlashes : fightFlashes)[p]++;
                }
            }
            vanguard.sim.BossPlaybook book = piece.playbook().orElse(null);
            for (vanguard.sim.PhasePlaybook.Kind kind : vanguard.sim.PhasePlaybook.Kind.values()) {
                int l = ChimeraLooks.lane(book, kind);
                if (l >= 0) {
                    lanes[l] = true;
                    assertEquals(kind, book.kind(l));
                }
            }
        }
        for (int b : looks.batteries) {
            assertTrue(riseFlashes[b] >= 2, "the rise volley from each battery: " + riseFlashes[b]);
            assertTrue(fightFlashes[b] >= 3, "each battery's burst: " + fightFlashes[b]);
        }
        assertTrue(riseFlashes[looks.bay] >= 2, "the rise volley from the bay: " + riseFlashes[looks.bay]);
        assertEquals(0, riseFlashes[looks.rack] + riseFlashes[looks.capsule], "the rack and the capsule hold the rise");
        assertTrue(fightFlashes[looks.rack] >= 3, "the rack's diamonds: " + fightFlashes[looks.rack]);
        assertTrue(fightFlashes[looks.bay] >= 3, "the bay's fan: " + fightFlashes[looks.bay]);
        for (int sac : looks.sacs) {
            assertEquals(0, riseFlashes[sac] + fightFlashes[sac], "the sealed sacs hold their fire in phase 1");
        }
        assertTrue(lanes[0] && lanes[1], "two told moves at once");
    }

    private static SetPiece chimeraOf(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            if (ChimeraLooks.drawn(sortie.setPiece(k).spec())) {
                return sortie.setPiece(k);
            }
        }
        return null;
    }

    /** Low in the middle, guns held. */
    private static int hold(Sortie sortie) {
        int commands = 0;
        if (sortie.ship().y() > 80) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    private static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** Gold on gold: the citadel's open hulls paler and duller than the boss's gold (design/world/vrell-space). */
    @Test
    void theWreckageIsPalerAndDullerThanTheBossesGold() throws IOException {
        double[] boss = goldStats(ImageIO.read(SPRITES.resolve(C + "_2.png").toFile()));
        double[] hull =
                goldStats(ImageIO.read(BACKDROP.resolve("gold-hull-a.png").toFile()));
        assertTrue(boss[2] > 50 && hull[2] > 50, "gold in both: " + boss[2] + ", " + hull[2]);
        assertTrue(hull[1] < boss[1], "the wreckage's gold less saturated: " + hull[1] + " vs " + boss[1]);
    }

    /** The mean value and saturation of an image's opaque gold pixels (hue 30-60 degrees), and their count. */
    private static double[] goldStats(BufferedImage image) {
        double value = 0;
        double saturation = 0;
        int count = 0;
        float[] hsb = new float[3];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) < 200) {
                    continue;
                }
                java.awt.Color.RGBtoHSB((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, hsb);
                double hue = hsb[0] * 360;
                if (hue >= 30 && hue <= 60 && hsb[1] > 0.25 && hsb[2] > 0.3) {
                    value += hsb[2];
                    saturation += hsb[1];
                    count++;
                }
            }
        }
        return new double[] {count == 0 ? 0 : value / count, count == 0 ? 0 : saturation / count, count};
    }
}
