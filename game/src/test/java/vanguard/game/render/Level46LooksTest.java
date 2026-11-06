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
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.content.Tier;
import vanguard.game.audio.FlightSounds;
import vanguard.game.audio.Sfx;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.GroundObject;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;

/**
 * M11 part D, step D4: Level 46 on the play field (design/campaign Level 46; design/enemies/space/husk and seed-casket;
 * design/enemies/space/leviathan, Act 7: the dying Leviathan; tools/art/act7_deadfleet.py): the art the looks draw, the
 * Husk's three hull shapes at its spin's headings, whole and cracked with its tell's flare, its and the casket's own
 * death sounds; the dying Leviathan drawn in its graded copy with its blowhole's slow pulse (the level's atlas holds no
 * Level 03 frame); the warm hull's light going out as its core rises, the sinews and the core drawn on the hull; the
 * Husks a step darker and harder than the scenery hulks at 1:1; every bullet read over the brightest hull; the tracker's
 * {@code CASKETS n / 5} and the radio's portrait static at step 4. A small review sheet (not a check) of the Husks over a
 * hulk, the starless patch over the deep and the casket, the core and the sinews is written to {@code
 * build/act7-deadfleet.png}.
 */
class Level46LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-46");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(46).orElseThrow();
    private static final String HUSK = GraveyardLooks.HUSK;
    private static final String DYING = "dying-leviathan";

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

    @Test
    void theArtIsWhatTheLooksDraw() throws IOException {
        for (String shape : GraveyardLooks.SHAPES) {
            assertEquals(GraveyardLooks.HEADINGS, frames(shape), shape);
            assertEquals(GraveyardLooks.HEADINGS, frames(shape + GraveyardLooks.CRACKED), shape + " cracked");
        }
        assertEquals(8, frames(GraveyardLooks.FLARE), "the tell's flare");
        assertEquals(16, frames(HUSK + "-slabs"), "the collapse");
        assertEquals(16, frames(HUSK + "-death"));
        assertEquals(16, frames(HUSK + "-shard"), "the shard at 16 headings");
        assertEquals(4, frames(HUSK + "-orb"), "the death ring's orb pulsing");
        assertTrue(Files.isRegularFile(SPRITES.resolve("seed-casket.png")));
        assertTrue(Files.isRegularFile(SPRITES.resolve("seed-casket-broken.png")));
        assertEquals(8, frames("seed-casket-tatters"));
        assertEquals(8, frames("seed-casket-death"));
        assertEquals(8, frames("vrell-core"), "the core breathing in its sinews");
        assertEquals(StoryLooks.RISE_FRAMES, frames("vrell-core-rise"));
        assertEquals(8, frames("vrell-core-drift"));
        assertEquals(StoryLooks.RISE_FRAMES + 6, frames("vrell-core-glow"));
        assertEquals(3, frames("warm-hull-sinew"), "whole, hit, cut");
        assertEquals(3, frames("helix-hatch"), "shut, dented, blown open");
        assertEquals(16, frames("debris-vorne-courier"));
        assertEquals(3, frames(DYING + "-cross"), "the dying Leviathan's crossing pass, graded");
        assertTrue(Files.isRegularFile(SPRITES.resolve(DYING + "-cross-glow.png")), "its blowhole's glow");
        assertTrue(Files.isRegularFile(ASSETS.resolve("pivots/" + DYING + ".json")), "its pivots, chunks renamed");
        String pivots = Files.readString(ASSETS.resolve("pivots/" + DYING + ".json"));
        assertFalse(pivots.contains("\"leviathan-"), "every chunk and ichor cloud in its graded copy");
        for (String piece : List.of("hulk-a", "hulk-b", "hulk-c", "hulk-d")) {
            assertEquals(16, frames(BACKDROP, piece), piece + " rocking");
        }
        assertEquals(8, frames(BACKDROP, BackdropPieces.CORE_GLOW), "the warm hull's light");
        assertTrue(Files.isRegularFile(BACKDROP.resolve("warm-hull.png")));
        assertTrue(Files.isRegularFile(BACKDROP.resolve("helix-wreck.png")));
    }

    /** A Husk's shape by its serial, its heading by its spin (frame k turned k × 11.25°), its flare over the tell. */
    @Test
    void aHuskSpinsThroughItsHeadingsAndFlaresInItsTell() {
        assertEquals(0, GraveyardLooks.shape(0));
        assertEquals(1, GraveyardLooks.shape(4));
        assertEquals(2, GraveyardLooks.shape(5));
        assertEquals(0, GraveyardLooks.heading(0));
        assertEquals(1, GraveyardLooks.heading(11.25));
        assertEquals(8, GraveyardLooks.heading(90));
        assertEquals(0, GraveyardLooks.heading(357), "the nearest frame wraps round");
        assertEquals(31, GraveyardLooks.heading(349));
        assertEquals(0, GraveyardLooks.flareFrame(0, 8));
        assertEquals(3, GraveyardLooks.flareFrame(0.45, 8), "its peak near the middle");
        assertEquals(7, GraveyardLooks.flareFrame(1, 8));
        assertEquals(Sfx.HUSK_DEATH, EnemyLooks.explosionSound(HUSK, Tier.MEDIUM, false));
        assertEquals(Sfx.HUSK_DEATH, EnemyLooks.explosionSound(HUSK, Tier.MEDIUM, true));
        assertEquals(Sfx.CASKET_CRACK, EnemyLooks.explosionSound("seed-casket", Tier.TINY, false));
        assertEquals("husk-shard", BulletLooks.OWN.get(HUSK));
        assertEquals("husk-orb", BulletLooks.DEATH_OWN.get(HUSK));
    }

    /**
     * Flown on medium with the ship invulnerable, holding fire: the Husks drift and turn slowly (their spin a smooth
     * presentation, never a jump of more than one heading a step), whole until the quiet's cue at t=144 cracks them.
     */
    @Test
    void theHusksTurnSlowlyAndCrackOnTheCue() {
        Sortie sortie = sortie();
        int last = SimStep.ticks(146);
        boolean seen = false;
        boolean cracked = false;
        double[] spins = new double[4096];
        for (int step = 0; step < last && !sortie.complete(); step++) {
            sortie.step(0);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                var enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals(HUSK)) {
                    continue;
                }
                seen = true;
                double spin = enemy.driftSpin(1);
                double before = spins[enemy.serial() % spins.length];
                if (before > 0) {
                    double turn = Math.abs(spin - before);
                    assertTrue(Math.min(turn, 360 - turn) <= 360.0 / GraveyardLooks.HEADINGS, "a slow spin");
                }
                spins[enemy.serial() % spins.length] = spin == 0 ? 1e-9 : spin;
                if (sortie.levelSeconds() < 143.9) {
                    assertTrue(
                            !GraveyardLooks.cracked(enemy)
                                    || enemy.hp() < enemy.spec().hp(),
                            "whole until hit");
                }
                cracked |= sortie.levelSeconds() >= 144 && GraveyardLooks.cracked(enemy);
            }
        }
        assertTrue(seen, "Husks flown");
        assertTrue(cracked, "the cue cracks the Husks on the screen");
    }

    /**
     * The dying Leviathan: the set piece keeps its slug and is scenery, so it is drawn in its graded copy and cries at
     * 0.75 pitch if it dies; its blowhole's glow pulses at most 0.25 Hz, smoothly between none and full.
     */
    @Test
    void theDyingLeviathanIsDrawnInItsGradedCopy() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        SetPieceSpec dying = script.setPieces().getFirst();
        assertEquals("leviathan", dying.slug());
        assertTrue(dying.isScenery());
        assertEquals(DYING, FlightSounds.setPieceSound(dying));
        assertEquals(SetPieceLooks.DYING + "leviathan", DYING);
        assertTrue(SetPieceLooks.CROSS_PULSE_HZ <= 0.25);
        assertEquals(0, SetPieceLooks.crossPulse(0), 1e-9);
        assertEquals(1, SetPieceLooks.crossPulse(0.5 / SetPieceLooks.CROSS_PULSE_HZ), 1e-9);
        double step = 1.0 / 60;
        double most = 0;
        for (double t = 0; t < 10; t += step) {
            most = Math.max(most, Math.abs(SetPieceLooks.crossPulse(t + step) - SetPieceLooks.crossPulse(t)));
        }
        assertTrue(most < 0.012, "smooth (a 0.2 Hz sine, under 0.011 a step): " + most);
    }

    /**
     * Flown on medium with the ship invulnerable, holding fire in the quiet, then under the warm hull's lowest sinew
     * firing and into the core once it rises: the hull's light holds while the core sits in it, fades over its 2 s rise
     * and is out once it rose; the sinews and the Helix hatch ride their hulls on low-air (drawn over them).
     */
    @Test
    void theWarmHullsLightGoesOutAsItsCoreRises() {
        Sortie sortie = sortie();
        int last = SimStep.ticks(215);
        float before = 1;
        boolean rising = false;
        boolean faded = false;
        boolean pinned = false;
        for (int step = 0; step < last && !sortie.complete(); step++) {
            sortie.step(steer(sortie));
            pinned |= GroundPasses.anyPinned(sortie);
            StoryPickups story = sortie.storyPickup().orElseThrow();
            float glow = Backdrop.coreGlow(story, rising);
            if (story.state() == StoryPickups.State.WAITING) {
                assertEquals(1, glow, 1e-6, "lit while the core sits in the hull");
            }
            if (story.rising()) {
                assertTrue(glow <= before + 1e-6, "fading over the rise");
                rising = true;
            }
            if (rising && !story.rising()) {
                assertEquals(0, glow, 1e-6, "out once it rose");
                faded = true;
            }
            before = glow;
        }
        assertTrue(pinned, "the sinews and the hatch are pinned on low-air");
        assertTrue(rising, "the four sinews cut, the core rose");
        assertTrue(faded);
        assertEquals(1, Backdrop.coreGlow(null, false), 1e-6, "a level without a story");
    }

    /** The tracker's {@code CASKETS 0 / 5} (5 of the 6), the friendly portraits' static at step 4, the Choir clean. */
    @Test
    void theTrackerCountsTheCasketsAndTheRadioCrackles() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        assertEquals("CASKETS", script.objectives().secondary().label());
        assertEquals("0 / 5", MissionPanel.escapesCount(0, 6, 1), "5 of the 6 caskets");
        LevelData level = CONTENT.level(KEY);
        assertEquals(4, level.radioNoiseStep());
        assertEquals(0.4f, MissionPanel.portraitNoise(0, 4, true), 1e-6, "the friendly portraits' steady static");
        assertEquals(0, MissionPanel.portraitNoise(0, 4, false), 1e-6, "the Choir's clean");
    }

    /**
     * At 1:1 (alpha-weighted pixels of the hulls): the Husks are a step darker than every scenery hulk and harder in
     * contrast, so the threat reads apart from the scenery (design/campaign Level 46, the dead fleet).
     */
    @Test
    void theHusksReadDarkerAndHarderThanTheSceneryHulks() throws IOException {
        double darkestHulk = 255;
        double hulkContrast = 0;
        for (String hulk : List.of("hulk-a", "hulk-b", "hulk-c", "hulk-d")) {
            for (int f = 0; f < 16; f += 5) {
                double[] stats = stats(
                        ImageIO.read(BACKDROP.resolve(hulk + "_" + f + ".png").toFile()));
                darkestHulk = Math.min(darkestHulk, stats[0]);
                hulkContrast = Math.max(hulkContrast, stats[1]);
            }
        }
        for (String shape : GraveyardLooks.SHAPES) {
            for (String state : List.of("", GraveyardLooks.CRACKED)) {
                for (int f = 0; f < GraveyardLooks.HEADINGS; f += 7) {
                    String name = shape + state + "_" + f;
                    double[] stats =
                            stats(ImageIO.read(SPRITES.resolve(name + ".png").toFile()));
                    assertTrue(stats[0] <= darkestHulk - 10, name + " darker: " + stats[0] + " vs " + darkestHulk);
                    assertTrue(stats[1] >= hulkContrast * 1.3, name + " harder: " + stats[1] + " vs " + hulkContrast);
                }
            }
        }
    }

    /**
     * At 1:1 over the brightest 15 px window of each hull piece (on the deep's base colour; the warm hull with its light
     * at full): an enemy orb's, a shard's and a death orb's white core stands far above the brightest hull, its dark rim
     * below the window's mean (the bullets are never lost on the hulls).
     */
    @Test
    void bulletsReadOverTheBrightestHull() throws IOException {
        for (String hull : List.of("hulk-a_0", "hulk-b_0", "hulk-c_0", "hulk-d_0", "warm-hull", "helix-wreck")) {
            float[][] field = hull(hull);
            BufferedImage tile = ImageIO.read(BACKDROP.resolve(hull + ".png").toFile());
            int w = tile.getWidth();
            int h = tile.getHeight();
            int size = 15;
            int bestX = 0;
            int bestY = 0;
            double best = -1;
            for (int y = 0; y + size <= h; y += 3) {
                for (int x = 0; x + size <= w; x += 3) {
                    double sum = 0;
                    for (int dy = 0; dy < size; dy++) {
                        for (int dx = 0; dx < size; dx++) {
                            sum += lum(field[(y + dy) * w + x + dx]);
                        }
                    }
                    if (sum > best) {
                        best = sum;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
            double mean = best / (size * size);
            double brightest = 0;
            for (int dy = 0; dy < size; dy++) {
                for (int dx = 0; dx < size; dx++) {
                    brightest = Math.max(brightest, lum(field[(bestY + dy) * w + bestX + dx]));
                }
            }
            for (String bullet :
                    List.of("orb_0", "orb-medium_0", HUSK + "-shard_0", HUSK + "-shard_5", HUSK + "-orb_0")) {
                BufferedImage orb =
                        ImageIO.read(SPRITES.resolve(bullet + ".png").toFile());
                double core = 0;
                double rim = 255;
                int ox = bestX + size / 2 - orb.getWidth() / 2;
                int oy = bestY + size / 2 - orb.getHeight() / 2;
                for (int y = 0; y < orb.getHeight(); y++) {
                    for (int x = 0; x < orb.getWidth(); x++) {
                        int argb = orb.getRGB(x, y);
                        float a = (argb >>> 24) / 255f;
                        if (a < 0.8f || ox + x < 0 || oy + y < 0 || ox + x >= w || oy + y >= h) {
                            continue;
                        }
                        float[] under = field[(oy + y) * w + ox + x];
                        float[] seen = new float[3];
                        for (int c = 0; c < 3; c++) {
                            seen[c] = under[c] * (1 - a) + (argb >> (16 - 8 * c) & 255) * a;
                        }
                        core = Math.max(core, lum(seen));
                        rim = Math.min(rim, lum(seen));
                    }
                }
                assertTrue(core - brightest >= 80, bullet + " over " + hull + ": core " + core + " vs " + brightest);
                assertTrue(mean - rim >= 30, bullet + " over " + hull + ": rim " + rim + " vs mean " + mean);
            }
        }
    }

    /** Mean and standard deviation of the luminance of a sprite's opaque pixels. */
    private static double[] stats(BufferedImage image) {
        double sum = 0;
        double squares = 0;
        int n = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) < 200) {
                    continue;
                }
                double l = lum(new float[] {argb >> 16 & 255, argb >> 8 & 255, argb & 255});
                sum += l;
                squares += l * l;
                n++;
            }
        }
        double mean = sum / n;
        return new double[] {mean, Math.sqrt(Math.max(0, squares / n - mean * mean))};
    }

    /** A hull piece over the deep's base colour (the warm hull with its light's first frame added), RGB 0-255. */
    private static float[][] hull(String name) throws IOException {
        BufferedImage tile = ImageIO.read(BACKDROP.resolve(name + ".png").toFile());
        BufferedImage light = name.equals("warm-hull")
                ? ImageIO.read(
                        BACKDROP.resolve(BackdropPieces.CORE_GLOW + "_0.png").toFile())
                : null;
        int w = tile.getWidth();
        int h = tile.getHeight();
        float[] base = {StarlessPatch.BASE.r * 255, StarlessPatch.BASE.g * 255, StarlessPatch.BASE.b * 255};
        float[][] out = new float[w * h][3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int t = tile.getRGB(x, y);
                float ta = (t >>> 24) / 255f;
                int g = light == null ? 0 : light.getRGB(x, y);
                float ga = (g >>> 24) / 255f;
                for (int c = 0; c < 3; c++) {
                    int shift = 16 - 8 * c;
                    out[y * w + x][c] =
                            Math.min(255, (t >> shift & 255) * ta + base[c] * (1 - ta) + (g >> shift & 255) * ga);
                }
            }
        }
        return out;
    }

    private static double lum(float[] rgb) {
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
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

    /**
     * The test's flight: guns held until t=160 (the quiet's Husks stay whole), then low under the warm hull's lowest
     * standing sinew firing, and into the core once it rises or drifts loose; else low in the middle.
     */
    private static int steer(Sortie sortie) {
        double tx = 240;
        double ty = 80;
        boolean fire = false;
        StoryPickups story = sortie.storyPickup().orElseThrow();
        if (story.rising() || story.state() == StoryPickups.State.LOOSE) {
            tx = story.renderX();
            ty = story.state() == StoryPickups.State.LOOSE ? loose(sortie, story) : 80;
        } else if (sortie.levelSeconds() >= 160) {
            GroundObject lowest = null;
            for (int i = 0; i < sortie.groundObjectCount(); i++) {
                GroundObject object = sortie.groundObject(i);
                if (object.spec().pinnedLowAir()
                        && !object.spent()
                        && object.spec().groupDrop().isPresent()
                        && object.renderY(1) < PlayField.HEIGHT - 10
                        && (lowest == null || object.renderY(1) < lowest.renderY(1))) {
                    lowest = object;
                }
            }
            if (lowest != null) {
                tx = lowest.renderX();
                fire = true;
            }
        }
        int commands = fire ? Command.FIRE.bit() : 0;
        double x = sortie.ship().x();
        double y = sortie.ship().y();
        if (x < tx - 3) {
            commands |= Command.RIGHT.bit();
        } else if (x > tx + 3) {
            commands |= Command.LEFT.bit();
        }
        if (y < ty - 3) {
            commands |= Command.UP.bit();
        } else if (y > ty + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    /** Where the loose core drifts now, y px (the story pickup in the pool). */
    private static double loose(Sortie sortie, StoryPickups story) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == vanguard.sim.PickupType.STORY) {
                return sortie.pickup(i).renderY(1);
            }
        }
        return story.renderY(1);
    }

    // A small review sheet (not a check).

    /**
     * {@code build/act7-deadfleet.png}: the deep with the starless patch at its place (the left half), a hulk with the
     * three Husk shapes whole, cracked and flaring over it, the casket whole and broken, the core in its sinews, the
     * dying Leviathan's cross frame with its glow.
     */
    @Test
    void writesASmallReviewSheet() throws IOException {
        BufferedImage deep = ImageIO.read(BACKDROP.resolve("deep-3.png").toFile());
        BufferedImage sheet = new BufferedImage(960, 540, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 540; y++) {
            for (int x = 0; x < 960; x++) {
                sheet.setRGB(x, y, deep.getRGB(x % deep.getWidth(), y % deep.getHeight()) & 0xFFFFFF);
            }
        }
        int[] mask = StarlessPatch.mask();
        int left = StarlessPatch.X - StarlessPatch.SIZE / 2;
        int top = StarlessPatch.BELOW_TOP - StarlessPatch.SIZE / 2;
        int base = (int) (StarlessPatch.BASE.r * 255) << 16
                | (int) (StarlessPatch.BASE.g * 255) << 8
                | (int) (StarlessPatch.BASE.b * 255);
        for (int y = 0; y < StarlessPatch.SIZE; y++) {
            for (int x = 0; x < StarlessPatch.SIZE; x++) {
                blend(sheet, left + x, top + y, base, mask[y * StarlessPatch.SIZE + x] / 255f, false);
            }
        }
        placeFrom(sheet, BACKDROP.resolve("hulk-a_0.png"), 720, 140, false);
        for (int s = 0; s < GraveyardLooks.SHAPES.length; s++) {
            String shape = GraveyardLooks.SHAPES[s];
            place(sheet, shape + "_" + (s * 5), 600 + s * 110, 330, false);
            place(sheet, shape + GraveyardLooks.CRACKED + "_" + (s * 5), 600 + s * 110, 440, false);
        }
        place(sheet, GraveyardLooks.FLARE + "_3", 820, 440, true);
        place(sheet, HUSK + "-shard_0", 660, 140, false);
        place(sheet, HUSK + "-orb_0", 690, 140, false);
        place(sheet, "seed-casket", 500, 300, false);
        place(sheet, "seed-casket-broken", 500, 340, false);
        place(sheet, "vrell-core_0", 520, 460, false);
        place(sheet, DYING + "-cross_0", 230, 430, false);
        place(sheet, DYING + "-cross-glow", 230, 430, true);
        Path out = Path.of("build", "act7-deadfleet.png");
        Files.createDirectories(out.getParent());
        ImageIO.write(sheet, "png", out.toFile());
        assertNotNull(ImageIO.read(out.toFile()));
    }

    private static void place(BufferedImage sheet, String sprite, int cx, int cy, boolean additive) throws IOException {
        placeFrom(sheet, SPRITES.resolve(sprite + ".png"), cx, cy, additive);
    }

    private static void placeFrom(BufferedImage sheet, Path file, int cx, int cy, boolean additive) throws IOException {
        BufferedImage sprite = ImageIO.read(file.toFile());
        int left = cx - sprite.getWidth() / 2;
        int top = cy - sprite.getHeight() / 2;
        for (int y = 0; y < sprite.getHeight(); y++) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                int argb = sprite.getRGB(x, y);
                blend(sheet, left + x, top + y, argb & 0xFFFFFF, (argb >>> 24) / 255f, additive);
            }
        }
    }

    private static void blend(BufferedImage sheet, int sx, int sy, int rgbIn, float a, boolean additive) {
        if (sx < 0 || sy < 0 || sx >= sheet.getWidth() || sy >= sheet.getHeight()) {
            return;
        }
        int under = sheet.getRGB(sx, sy);
        int rgb = 0;
        for (int shift = 16; shift >= 0; shift -= 8) {
            float b = under >> shift & 255;
            float s = rgbIn >> shift & 255;
            int v = Math.round(additive ? Math.min(255, b + s * a) : b * (1 - a) + s * a);
            rgb |= v << shift;
        }
        sheet.setRGB(sx, sy, rgb);
    }
}
