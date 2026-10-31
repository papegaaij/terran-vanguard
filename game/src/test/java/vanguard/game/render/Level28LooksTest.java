package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.CoilArena;
import vanguard.sim.CoilArena.DiveState;
import vanguard.sim.CoilArena.ImplantState;
import vanguard.sim.CruiserRunSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H's Level 28 looks (design/campaign Level 28; design/enemies/bosses/abyssal-maw, Presentation and Tells;
 * tools/art/abyssal_maw.py, ascendancy_cruiser.py, l28_props.py): the art's frames as the looks count them; the coil's
 * headings and taper as the art's pivots give them; the head's poses, the sacs' and implants' frames by the
 * simulation's state; the sub look and the dive's darkness; the death's order (tail to head, the head last); the
 * cruisers' rise, tell glow and ice; the breakout at the first section in the air and the orbit's white; flown: the coil
 * rises out of the dark, a sac swells, the throat glows and opens, a plate flares for a shed, and with the sacs gone
 * the fan walls' rim and a sweep's bight show.
 */
class Level28LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = MawFixture.CONTENT;
    private static final String MAW = "abyssal-maw";

    private static long frames(String name) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }

    private static boolean single(String name) {
        return Files.isRegularFile(SPRITES.resolve(name + ".png"));
    }

    private static LevelData level(int number) {
        return CONTENT.levels().get(CONTENT.levelKey(number).orElseThrow());
    }

    private static JsonValue pivots(String name) throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/" + name + ".json")));
    }

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        int segments = MawLooks.HEADINGS * MawLooks.TAPERS;
        assertEquals(segments, frames(MAW + "-segment"), "heading × taper");
        assertEquals(segments, frames(MAW + "-segment-glow"));
        assertEquals(segments, frames(MAW + "-segment-tell"));
        assertEquals(MawLooks.HEAD_FRAMES, frames(MAW + "-head"));
        assertEquals(MawLooks.HEAD_FRAMES, frames(MAW + "-head-glow"));
        assertTrue(single(MAW + "-head-rim"), "the wall's tell");
        assertEquals(MawLooks.SWIM_HEADINGS, frames(MAW + "-head-swim"), "round 58 F.2: the head swimming");
        assertEquals(MawLooks.SAC_TORN + 1, frames(MAW + "-sac"));
        assertEquals(MawLooks.SAC_FULL + 1, frames(MAW + "-sac-glow"), "the torn sac has none");
        assertEquals(MawLooks.IMPLANT_BLOWN + 1, frames(MAW + "-implant"));
        assertEquals(MawLooks.IMPLANT_OPEN, frames(MAW + "-implant-glow"), "frames 1-3");
        assertEquals(4, frames(MAW + "-arc"));
        assertEquals(MawLooks.SHED_FRAMES, frames(MAW + "-shed"));
        assertEquals(8, frames(MAW + "-silt"));
        assertTrue(single(MAW + "-shadow-head"));
        assertTrue(single(MAW + "-shadow-column"));
        assertEquals(8, frames(MAW + "-blowout"));
        assertEquals(8, frames(MAW + "-blowout-glow"));
        assertEquals(8, frames(MAW + "-ichor"));
        assertEquals(4, frames(MAW + "-flare"));
        String cruiser = CruiserLooks.SLUG;
        assertTrue(single(cruiser), "one hull for both flanks");
        assertTrue(single(cruiser + "-shadow"));
        assertTrue(single(cruiser + "-tracer"));
        assertEquals(2, frames(cruiser + "-glow"), "running, the broadside's tell");
        assertEquals(6, frames(cruiser + "-ice"));
        assertEquals(4, frames(cruiser + "-muzzle"));
        assertEquals(3, frames(cruiser + "-pd"));
        assertEquals(4, frames(cruiser + "-flak-marker"));
        assertEquals(8, frames(cruiser + "-flak"));
        assertEquals(4, frames(cruiser + "-spark"));
        assertEquals(10, frames("ice-shatter"));
        assertEquals(BreakoutLooks.SLABS * BreakoutLooks.TUMBLE, frames("ice-slab"));
    }

    /**
     * Step DITH7 (the user, 2026-10-30: the glow round the vent dithered, "the same defect we fixed in level 35"): the
     * caldera's glow and the Maw's soft additive glows are smooth undithered steps. With a 4x4 Bayer dither (10 steps)
     * the caldera's glow, drawn 1x over the halted arena, had a 1 px checker on about half its pixels; the vent's flare
     * was a 192 px frame drawn x1.6, now full size and drawn 1x. The measure is {@link
     * SovereignLooksTest#checkerShare}'s (step PIX7).
     */
    @Test
    void theVentsGlowsAreUndithered() throws IOException {
        java.util.List<Path> glows = new java.util.ArrayList<>();
        glows.add(ASSETS.resolve("backdrop/level-28/caldera-glow.png"));
        glows.add(SPRITES.resolve(MAW + "-head-rim.png"));
        for (String set : new String[] {"arc", "shed", "blowout-glow", "flare"}) {
            try (Stream<Path> files = Files.list(SPRITES)) {
                files.filter(file -> file.getFileName()
                                .toString()
                                .matches(java.util.regex.Pattern.quote(MAW + "-" + set) + "_\\d+\\.png"))
                        .forEach(glows::add);
            }
        }
        assertEquals(2 + 4 + MawLooks.SHED_FRAMES + 8 + 4, glows.size(), "the glows' frames");
        for (Path file : glows) {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(file.toFile());
            for (int step = 1; step <= 2; step++) {
                double share = SovereignLooksTest.checkerShare(image, step);
                assertTrue(share < 0.02, file.getFileName() + ": a " + step + " px checker on " + share + " of it");
            }
        }
        assertEquals(1f, MawLooks.FLARE_SCALE, "the vent's flare drawn 1x");
        java.awt.image.BufferedImage flare =
                javax.imageio.ImageIO.read(SPRITES.resolve(MAW + "-flare_1.png").toFile());
        assertTrue(flare.getWidth() >= 300, "the flare at its drawn size: " + flare.getWidth());
    }

    /** The coil's segment frame from the simulation's angle and its index, as tools/art/abyssal_maw.py's pivots say. */
    @Test
    void theSegmentsHeadingAndTaperAreTheArts() throws IOException {
        JsonValue segment = pivots(MAW).get("segment");
        assertEquals(MawLooks.HEADINGS, segment.getInt("headings"));
        assertEquals(MawLooks.TAPERS, segment.get("tapers").size);
        JsonValue taper = segment.get("taper_of_31");
        for (int i = 0; i < taper.size; i++) {
            assertEquals(taper.getInt(i), MawLooks.taper(i, taper.size), "segment " + i);
        }
        // The heading is the way toward the head, k × 11.25° clockwise from straight down; the angle runs toward the
        // tail.
        assertEquals(16, MawLooks.heading(-Math.PI / 2), "the tail below: facing up");
        assertEquals(0, MawLooks.heading(Math.PI / 2), "the tail above: facing down");
        assertEquals(8, MawLooks.heading(0), "the tail to the right: facing left");
        assertEquals(24, MawLooks.heading(Math.PI), "the tail to the left: facing right");
        assertEquals(1, MawLooks.heading(Math.PI / 2 - 2 * Math.PI / 32));
        assertEquals(16 * 4 + 3, MawLooks.segmentFrame(16, 3));
        JsonValue head = pivots(MAW).get("head");
        assertEquals(MawLooks.HEAD_FRAMES, head.get("frames").size);
        assertEquals("lunge", head.get("frames").getString(MawLooks.LUNGE));
        assertEquals("rear", head.get("frames").getString(MawLooks.REAR));
        assertEquals("death", head.get("frames").getString(MawLooks.DEAD));
        // Round 58 F.2: the swimming head faces k × 22.5° clockwise from straight down.
        assertEquals(MawLooks.SWIM_HEADINGS, pivots(MAW).get("head_swim").getInt("headings"));
        assertEquals(0, MawLooks.swimHeading(-Math.PI / 2), "facing down: the rest frame's facing");
        assertEquals(4, MawLooks.swimHeading(Math.PI), "facing left");
        assertEquals(8, MawLooks.swimHeading(Math.PI / 2), "facing up");
        assertEquals(12, MawLooks.swimHeading(0), "facing right");
        assertEquals(1, MawLooks.swimHeading(-Math.PI / 2 - 2 * Math.PI / 16));
    }

    @Test
    void theHeadsPoseFollowsTheFight() {
        assertEquals(MawLooks.REST, MawLooks.headFrame(DiveState.READY, false, false, false));
        assertEquals(MawLooks.GLOW, MawLooks.headFrame(DiveState.READY, true, true, false), "a fan's or a wall's glow");
        assertEquals(MawLooks.OPEN, MawLooks.headFrame(DiveState.READY, false, true, false), "the throat's window");
        assertEquals(MawLooks.REAR, MawLooks.headFrame(DiveState.DONE, false, false, true), "phase 3: reared");
        assertEquals(MawLooks.OPEN, MawLooks.headFrame(DiveState.DONE, false, true, true), "open over reared");
        for (DiveState state : new DiveState[] {DiveState.SLIDE, DiveState.QUIET, DiveState.SHADOW, DiveState.RETURN}) {
            assertEquals(MawLooks.DIVE, MawLooks.headFrame(state, false, false, false), state.name());
        }
        assertEquals(MawLooks.LUNGE, MawLooks.headFrame(DiveState.LUNGE, false, true, false));
        assertEquals(MawLooks.LUNGE, MawLooks.headFrame(DiveState.BODY, false, false, false));
    }

    @Test
    void theSacsAndImplantsFollowTheirState() {
        assertEquals(MawLooks.SAC_IDLE, MawLooks.sacFrame(false, false, 0));
        assertEquals(MawLooks.SAC_SWELLING, MawLooks.sacFrame(false, true, 0.2));
        assertEquals(MawLooks.SAC_FULL, MawLooks.sacFrame(false, true, 0.8), "the tell's peak");
        assertEquals(MawLooks.SAC_TORN, MawLooks.sacFrame(true, true, 0.8));
        assertEquals(MawLooks.IMPLANT_SHUT, MawLooks.implantFrame(ImplantState.SHUT, false, 0));
        assertEquals(1, MawLooks.implantFrame(ImplantState.TELL, false, 0));
        assertEquals(2, MawLooks.implantFrame(ImplantState.TELL, false, MawLooks.SPARK_TICKS), "the sparks alternate");
        assertEquals(MawLooks.IMPLANT_OPEN, MawLooks.implantFrame(ImplantState.OPEN, false, 7));
        assertEquals(MawLooks.IMPLANT_BLOWN, MawLooks.implantFrame(ImplantState.OPEN, true, 7));
    }

    /** Under the water the coil is smaller, darker and fainter, its glows keep most of their light. */
    @Test
    void theSubLookDimsTheCoilAndKeepsItsGlow() {
        assertEquals(1, MawLooks.subScale(0));
        assertEquals(1, MawLooks.subShade(0));
        assertEquals(1, MawLooks.subAlpha(0));
        assertTrue(MawLooks.subScale(1) < 0.9f && MawLooks.subShade(1) < 0.4f && MawLooks.subAlpha(1) < 0.6f);
        assertTrue(MawLooks.glowAlpha(1) > MawLooks.subAlpha(1), "the frame in the dark");
        assertTrue(MawLooks.glowAlpha(1) >= 0.6f);
    }

    /**
     * Round 58 F.2, flown: the head swims in from off the screen turning through the coil's bends a heading at a time
     * (every heading on the way round), the segments' frames turn with the line, and once up it is at rest facing down.
     */
    @Test
    void theHeadSwimsInTurningThroughTheBends() {
        Sortie sortie = MawFixture.sortie();
        Set<Integer> headings = new HashSet<>();
        int last = -1;
        boolean wound = false;
        while (!MawFixture.maw(sortie).present() || !MawFixture.coil(sortie).up()) {
            sortie.step(0);
            if (!MawFixture.maw(sortie).present()) {
                continue;
            }
            CoilArena coil = MawFixture.coil(sortie);
            if (!coil.up()) {
                assertTrue(MawLooks.winding(coil), "winding in over the intro");
                wound = true;
                int k = MawLooks.swimHeading(coil.headFacing());
                if (last >= 0) {
                    int step = Math.floorMod(k - last, MawLooks.SWIM_HEADINGS);
                    assertTrue(
                            step <= 1 || step == MawLooks.SWIM_HEADINGS - 1, "a heading at a time: " + last + " " + k);
                }
                last = k;
                headings.add(k);
            }
        }
        assertTrue(wound);
        assertEquals(MawLooks.SWIM_HEADINGS, headings.size(), "round the arena: every heading " + headings);
        CoilArena coil = MawFixture.coil(sortie);
        assertFalse(MawLooks.winding(coil));
        assertEquals(0, MawLooks.swimHeading(coil.headFacing()), "at rest, facing down");
        assertEquals(0, MawLooks.darkness(coil), "no darkness on the arrival");
    }

    /** The death: the implants blow first, the segments sink from the tail to the head, the head last. */
    @Test
    void theDeathSinksTheCoilTailToHeadAndTheHeadLast() {
        int n = 31;
        double death = 4;
        double tail = MawLooks.sinkStart(n - 1, n, 3, death);
        double neck = MawLooks.sinkStart(0, n, 3, death);
        assertEquals(3 * MawLooks.BLOWOUT_GAP, tail, 1e-9, "after the three blow-outs");
        assertTrue(neck > tail, "tail to head");
        for (int i = 1; i < n; i++) {
            assertTrue(MawLooks.sinkStart(i - 1, n, 3, death) >= MawLooks.sinkStart(i, n, 3, death));
        }
        assertTrue(MawLooks.headSinkStart(death) >= neck + MawLooks.SEGMENT_SINK - 1e-9, "the head last");
        assertEquals(death, MawLooks.headSinkStart(death) + MawLooks.HEAD_SINK, 1e-9);
        assertEquals(0, MawLooks.sunk(0, 1, 1));
        assertEquals(0.5, MawLooks.sunk(1.5, 1, 1), 1e-9);
        assertEquals(1, MawLooks.sunk(9, 1, 1));
    }

    /** The cruisers rise from dim and small to full, shed their ice over the rise and cross-fade to the tell's glow. */
    @Test
    void theCruisersRiseAndTellTheirBroadsides() throws IOException {
        assertEquals(CruiserLooks.RISE_SCALE, CruiserLooks.scale(0));
        assertEquals(1, CruiserLooks.scale(1));
        assertEquals(1, CruiserLooks.shade(1));
        assertTrue(CruiserLooks.shade(0) < 0.5f);
        assertEquals(0, CruiserLooks.iceFrame(0, 6));
        assertEquals(5, CruiserLooks.iceFrame(0.99, 6));
        assertEquals(-1, CruiserLooks.iceFrame(1, 6), "risen: no crust");
        assertEquals(0, CruiserLooks.tellGlow(0));
        assertEquals(0.5f, CruiserLooks.tellGlow(CruiserLooks.TELL_FADE / 2), 1e-6f);
        assertEquals(1, CruiserLooks.tellGlow(1.2));
        JsonValue pivots = pivots(CruiserLooks.SLUG);
        LevelScript script = SimSpecs.level(CONTENT, MawFixture.LEVEL, Difficulty.MEDIUM);
        CruiserRunSpec run = script.hazards().cruisers().orElseThrow();
        assertEquals(110, run.leftInner(), "the left hull's inner edge");
        assertEquals(370, run.rightInner());
        assertEquals(116, pivots.getInt("half_width"), "the art's half width to the inner edge");
        assertEquals(14, pivots.get("ports").get("right").size, "the left hull's inner ports");
        assertEquals(14, pivots.get("ports").get("left").size);
        CruiserLooks looks = CruiserLooks.following(script, pivots);
        looks.reset();
        assertTrue(script.hazards().cruisers().isPresent());
        assertTrue(SimSpecs.level(CONTENT, CONTENT.levelKey(27).orElseThrow(), Difficulty.MEDIUM)
                .hazards()
                .cruisers()
                .isEmpty());
    }

    /**
     * Step I2 (round 58 F.3): the cruisers break through intact night ice where they are. Their crust is the night ice
     * (at the rise's scale its colours are the ground's); flown, each hull starts on its hole in the ice and moves with
     * the ground there (the hole under it, on the ground's scroll), then slows to the wing's pace and is in its running
     * place by the run, the hole left behind below it; nothing of the hole before the rise; the backdrop has no open
     * water there (no ice-split pieces).
     */
    @Test
    void theCruisersBreakThroughTheIceWhereTheyAre() throws IOException {
        java.awt.image.BufferedImage crust = javax.imageio.ImageIO.read(
                SPRITES.resolve(CruiserLooks.SLUG + "-ice_0.png").toFile());
        java.awt.image.BufferedImage ice = javax.imageio.ImageIO.read(
                ASSETS.resolve("backdrop/level-28/night-ice.png").toFile());
        double[] crustMean = mean(crust);
        double[] iceMean = mean(ice);
        for (int c = 0; c < 3; c++) {
            assertEquals(iceMean[c], crustMean[c], 24, "the crust in the night ice's colours, channel " + c);
        }
        java.awt.image.BufferedImage hole = javax.imageio.ImageIO.read(
                SPRITES.resolve(CruiserLooks.SLUG + "-hole.png").toFile());
        java.awt.image.BufferedImage hull = javax.imageio.ImageIO.read(
                SPRITES.resolve(CruiserLooks.SLUG + ".png").toFile());
        int covered = 0;
        int footprint = 0;
        for (int y = 0; y < hull.getHeight(); y += 4) {
            for (int x = 0; x < hull.getWidth(); x += 4) {
                if ((hull.getRGB(x, y) >>> 24) > 200) {
                    footprint++;
                    int hx = (int)
                            Math.round(hole.getWidth() / 2.0 + (x - hull.getWidth() / 2.0) * CruiserLooks.RISE_SCALE);
                    int hy = (int)
                            Math.round(hole.getHeight() / 2.0 + (y - hull.getHeight() / 2.0) * CruiserLooks.RISE_SCALE);
                    covered += (hole.getRGB(hx, hy) >>> 24) > 200 ? 1 : 0;
                }
            }
        }
        assertTrue(
                covered > 0.97 * footprint,
                "the hole under the hull's footprint at the rise: " + covered + " of " + footprint);
        assertTrue(
                level(28).backdrop().pieces().keySet().stream().noneMatch(name -> name.startsWith("ice-split")),
                "no open leads drawn in the ice before the rise");

        LevelScript script = SimSpecs.level(CONTENT, MawFixture.LEVEL, Difficulty.MEDIUM);
        CruiserRunSpec spec = script.hazards().cruisers().orElseThrow();
        assertEquals(
                150 * CruiserLooks.CATCH * (spec.runAt() - spec.riseAt()) / 2,
                CruiserLooks.riseDrop(script, spec),
                1e-9);
        assertEquals(420, CruiserLooks.riseY(270, 150, 0), 1e-9);
        assertEquals(270, CruiserLooks.riseY(270, 150, CruiserLooks.CATCH), 1e-9, "caught up half way");
        assertEquals(270, CruiserLooks.riseY(270, 150, 1), 1e-9);
        Sortie sortie = MawFixture.finale();
        CruiserLooks looks = CruiserLooks.following(sortie.script(), pivots(CruiserLooks.SLUG));
        vanguard.sim.CruiserRun run = sortie.cruisers().orElseThrow();
        int riseTick = SimStep.ticks(spec.riseAt());
        double drop = looks.riseDrop();
        int follow = 0;
        double lastHull = Double.NaN;
        while (sortie.levelSeconds() < spec.runAt() + 0.5) {
            sortie.step(IceShelfFixture.toward(sortie, 240, 120));
            looks.stepped(sortie);
            int at = CruiserLooks.levelTick(sortie);
            if (at < riseTick) {
                assertTrue(Double.isNaN(looks.riseScroll()), "no hole before the rise");
                continue;
            }
            double hullY = CruiserLooks.riseY(run.hullY(at), drop, run.riseShare(at));
            double holeY = looks.holeY(run, sortie.groundScroll());
            if (at == riseTick) {
                assertEquals(holeY, hullY, 1, "the hull starts on its hole");
            }
            if (at - riseTick < SimStep.ticks(0.25)) {
                assertEquals(holeY, hullY, 3, "moving with the ground at first");
                follow++;
            }
            assertTrue(Double.isNaN(lastHull) || hullY <= lastHull + 1e-9, "it never climbs back");
            lastHull = hullY;
        }
        assertTrue(follow >= SimStep.ticks(0.25) - 1, "followed the hole");
        int runTick = SimStep.ticks(spec.runAt());
        assertEquals(
                PlayField.HEIGHT / 2.0,
                CruiserLooks.riseY(run.hullY(runTick), drop, run.riseShare(runTick)),
                1e-9,
                "in its running place by the run (the lanes as before)");
        assertTrue(looks.holeY(run, sortie.groundScroll()) < PlayField.HEIGHT / 2.0 - 400, "the hole left behind");
    }

    private static double[] mean(java.awt.image.BufferedImage image) {
        double[] sum = new double[3];
        int n = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                if ((rgb >>> 24) > 200) {
                    sum[0] += (rgb >> 16) & 255;
                    sum[1] += (rgb >> 8) & 255;
                    sum[2] += rgb & 255;
                    n++;
                }
            }
        }
        return new double[] {sum[0] / n, sum[1] / n, sum[2] / n};
    }

    /** The breakout at the first section in the air after the water (Level 28's t=115), the orbit's white to the end. */
    @Test
    void theBreakoutAndTheOrbit() {
        LevelData l28 = level(28);
        assertEquals(115, BreakoutLooks.breakAt(l28));
        assertTrue(Double.isNaN(BreakoutLooks.breakAt(level(23))), "Level 23 dives in");
        assertTrue(Double.isNaN(BreakoutLooks.breakAt(level(27))));
        assertTrue(Double.isNaN(BreakoutLooks.breakAt(level(22))), "on the ice");
        BreakoutLooks looks = BreakoutLooks.following(l28);
        assertEquals(0, looks.orbitWhite(150));
        assertEquals(0, looks.orbitWhite(157));
        assertTrue(looks.orbitWhite(159.5) > 0 && looks.orbitWhite(159.5) < BreakoutLooks.ORBIT_WHITE);
        assertEquals(BreakoutLooks.ORBIT_WHITE, looks.orbitWhite(162), 1e-6f);
        assertEquals(0, BreakoutLooks.shatterFrame(0, 10));
        assertEquals(9, BreakoutLooks.shatterFrame(9.5 * BreakoutLooks.SHATTER_TICKS * SimStep.SECONDS, 10));
        assertEquals(-1, BreakoutLooks.shatterFrame(10.5 * BreakoutLooks.SHATTER_TICKS * SimStep.SECONDS, 10));
        Set<Integer> slabs = new HashSet<>();
        for (int s = 0; s < BreakoutLooks.SLABS; s++) {
            for (double age = 0; age < BreakoutLooks.SLAB_SECONDS; age += 0.05) {
                int frame = BreakoutLooks.slabFrame(s, age);
                assertEquals(s, frame / BreakoutLooks.TUMBLE, "slab " + s + "'s own tumble");
                slabs.add(frame);
            }
        }
        assertEquals(BreakoutLooks.SLABS * BreakoutLooks.TUMBLE, slabs.size(), "every tumble frame");
    }

    /**
     * Step I2 (round 58 F.4): the lip that times the burst follows the art (the shaft mouth's water begins under it,
     * within its foam and ragged edge, across the bore), and flown at the left, in the middle, at the right and moving
     * the burst starts within one step of the ship's nose crossing the lip, at the lip where the ship crossed, and stays
     * anchored there on the ice (scrolling with the ground) while the ship flies on; high or at a flank the ship
     * breaches earlier than low in the middle.
     */
    @Test
    void theBurstStartsAsTheShipBreachesTheIce() throws IOException {
        LevelData l28 = level(28);
        BreachLine lip = BreachLine.of(l28).orElseThrow();
        assertEquals(l28.scrollAt(115) + 96, lip.apexRow(), 1e-9, "the breakout line");
        assertTrue(BreachLine.of(level(27)).isEmpty());
        java.awt.image.BufferedImage mouth = javax.imageio.ImageIO.read(
                ASSETS.resolve("backdrop/level-28/shaft-mouth.png").toFile());
        var placed = l28.backdrop().placed().stream()
                .filter(piece -> piece.piece().equals("shaft-mouth"))
                .findFirst()
                .orElseThrow();
        double top = l28.pieceCentre(placed) + mouth.getHeight() / 2.0;
        for (int x = 100; x <= 380; x += 40) {
            int water = -1;
            for (int y = 0; y < mouth.getHeight() - 24 && water < 0; y++) {
                boolean run = true;
                for (int k = 0; k < 24 && run; k++) {
                    run = dark(mouth, x, y + k);
                }
                water = run ? y : -1;
            }
            assertTrue(water > 0, "water under the lip at x " + x);
            assertEquals(lip.edgeRow(x) - 16, top - water, 16, "the lip at x " + x + " (the water under its foam)");
        }
        long[] ticks = new long[4];
        double[][] at = {{110, 96}, {240, 96}, {370, 96}, {0, 0}};
        for (int path = 0; path < at.length; path++) {
            Sortie sortie = MawFixture.finale();
            BreakoutLooks looks = BreakoutLooks.following(l28);
            long crossing = -1;
            double shipX = 0;
            double edgeY = 0;
            while (sortie.levelSeconds() < 119 && looks.burstTick() < 0) {
                double t = sortie.levelSeconds();
                int commands = path < 3
                        ? IceShelfFixture.toward(sortie, at[path][0], at[path][1])
                        : IceShelfFixture.toward(sortie, t % 2 < 1 ? 120 : 360, t % 1.4 < 0.7 ? 60 : 300);
                sortie.step(commands);
                looks.stepped(sortie);
                double x = sortie.ship().x();
                double gap =
                        lip.edgeRow(x) - sortie.groundScroll() - (sortie.ship().y() + BreachLine.NOSE);
                if (crossing < 0 && gap <= 0) {
                    crossing = sortie.tick();
                    shipX = x;
                    edgeY = lip.edgeRow(x) - sortie.groundScroll();
                }
            }
            assertTrue(crossing > 0, "path " + path + " crosses the lip");
            assertEquals(crossing, looks.burstTick(), 1, "path " + path + ": the burst within a step of the crossing");
            assertEquals(shipX, looks.burstX(), 3, "at the ship's x");
            assertEquals(edgeY, looks.burstY(sortie.groundScroll()), 3, "on the lip");
            if (path == 3) {
                assertTrue(Math.abs(sortie.ship().x() - sortie.ship().renderX(0)) > 0.5, "moving as it breaches");
            }
            ticks[path] = looks.burstTick();
            double scroll = sortie.groundScroll();
            for (int i = 0; i < 30; i++) {
                sortie.step(IceShelfFixture.toward(sortie, 400, 400));
                looks.stepped(sortie);
            }
            assertEquals(
                    looks.burstY(scroll) - (sortie.groundScroll() - scroll),
                    looks.burstY(sortie.groundScroll()),
                    1e-6,
                    "anchored on the ice");
            assertEquals(ticks[path], looks.burstTick(), "one burst");
        }
        assertTrue(ticks[0] < ticks[1] && ticks[2] < ticks[1], "the flanks breach before the middle");
    }

    private static boolean dark(java.awt.image.BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int lum = (((rgb >> 16) & 255) + ((rgb >> 8) & 255) + (rgb & 255)) / 3;
        return lum < 45 && (rgb >>> 24) > 200;
    }

    /**
     * Flown: the coil rises out of the dark (its depth from 1 to 0), a sac swells (swelling, then full), the throat
     * glows and opens, a plate flares for a shed (its frames over the tell, then the slip-out); with the sacs shot away
     * phase 2's fan walls light the jaw's rim and a sweep's bight streams silt and turns lime.
     */
    @Test
    void theFightShowsItsTells() {
        Sortie sortie = MawFixture.sortie();
        MawLooks looks = MawLooks.following(MawFixture.maw(sortie).spec());
        Set<Integer> heads = new HashSet<>();
        Set<Integer> sacFrames = new HashSet<>();
        Set<Integer> shedFrames = new HashSet<>();
        boolean rose = false;
        boolean shed = false;
        boolean rim = false;
        boolean bight = false;
        double lastDepth = 1;
        for (int i = 0; i < SimStep.ticks(150) && !(rim && bight); i++) {
            sortie.step(MawFixture.attack(sortie));
            looks.stepped(sortie);
            SetPiece maw = MawFixture.maw(sortie);
            if (!maw.present()) {
                continue;
            }
            CoilArena coil = MawFixture.coil(sortie);
            if (!coil.up()) {
                assertTrue(coil.depth() <= lastDepth + 1e-9, "rising");
                lastDepth = coil.depth();
                rose = true;
            }
            heads.add(MawLooks.headFrame(coil.diveState(), coil.throatGlowing(), coil.jawOpen(), maw.phase() >= 2));
            int swelling = coil.swellingSac();
            if (swelling >= 0) {
                sacFrames.add(MawLooks.sacFrame(false, true, looks.swellShare(swelling, sortie.tick())));
            }
            int segment = looks.shedSegment(sortie.tick());
            if (segment >= 0) {
                shed = true;
                shedFrames.add(looks.shedFrame(sortie.tick()));
            }
            rim |= coil.wallGlowing();
            if (coil.sweepState() == CoilArena.SweepState.TELL) {
                for (int s = 0; s < coil.segmentCount(); s++) {
                    bight |= coil.inBight(s);
                }
            }
        }
        assertTrue(rose, "the coil rises");
        assertEquals(0, MawFixture.coil(sortie).depth(), 1e-9, "up on the play plane");
        assertTrue(heads.containsAll(Set.of(MawLooks.REST, MawLooks.GLOW, MawLooks.OPEN)), "head poses " + heads);
        assertEquals(Set.of(MawLooks.SAC_SWELLING, MawLooks.SAC_FULL), sacFrames);
        assertTrue(shed, "a plate flares for a shed");
        assertTrue(
                shedFrames.contains(0) && shedFrames.contains(MawLooks.SHED_FRAMES - 1), "shed frames " + shedFrames);
        SetPiece end = MawFixture.maw(sortie);
        assertTrue(
                rim,
                "phase 2's fan walls light the jaw's rim: phase " + end.phase() + " sacs " + end.partHp(1) + "/"
                        + end.partHp(2) + "/" + end.partHp(3) + "/" + end.partHp(4) + " head " + end.partHp(0) + " at "
                        + sortie.realSeconds() + " ship " + sortie.ship().x() + ","
                        + sortie.ship().y());
        assertTrue(bight, "a sweep's bight in its tell");
        assertFalse(looks.drawsDeath(sortie.tick(), 0), "alive");
        assertEquals(0, MawLooks.darkness(MawFixture.coil(sortie)), "no dive yet");
    }
}
