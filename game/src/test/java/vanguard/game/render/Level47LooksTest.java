package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part E, step E4: Level 47 on the play field (design/campaign Level 47; design/enemies/air/choir-seraph;
 * tools/art/act7_sphere.py): the art the looks draw; the Seraph's frames by its cycle (banked with its sideways speed,
 * its fold and leaving tell by how far they have run, the spiral's tell after a landing), gone while it waits to enter
 * and between its rings, never tilted in its leaving tell; its spiral's own pulsing orb; the core light's curve (nothing
 * before t=118, smooth, the top of the screen by the end); the memory pearl's light and release; the tracker's {@code
 * SERAPHS n / 14} (11 easy, 16 hard) and the radio's portrait static at step 5; and at 1:1 over the core light at its
 * brightest (t=262, the deep's brightest stretch, no rib over it): the Seraph standing off the stone in its rows and
 * every bullet's dark rim below the light it crosses.
 */
class Level47LooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Path BACKDROP = ASSETS.resolve("backdrop/level-47");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(47).orElseThrow();
    private static final String SERAPH = SeraphLooks.SLUG;
    private static final String PEARL = "debris-memory-pearl";

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
        assertEquals(4, frames(SERAPH), "gliding");
        assertEquals(7, frames(SERAPH + SeraphLooks.BANK), "banked: 30, 20, 10 left, level, 10, 20, 30 right");
        assertEquals(6, frames(SERAPH + SeraphLooks.FOLD), "the 0.3 s fold");
        assertEquals(12, frames(SERAPH + SeraphLooks.LEAVE), "the 0.8 s leaving tell");
        assertEquals(12, frames(SERAPH + SeraphLooks.LEAVE_GLOW));
        assertEquals(6, frames(SERAPH + SeraphLooks.FLARE), "the spiral's tell");
        assertTrue(Files.isRegularFile(SPRITES.resolve(SERAPH + SeraphLooks.GLOW + ".png")));
        assertEquals(16, frames(SERAPH + "-tatters"));
        assertEquals(16, frames(SERAPH + "-death"));
        assertEquals(4, frames(SERAPH + "-shot"), "the spiral's orb pulsing");
        assertTrue(Files.isRegularFile(SPRITES.resolve("intel/" + SERAPH + ".png")));
        assertEquals(PropPasses.SPIN_FRAMES, frames(PEARL), "the pearl's shard tumbling");
        assertEquals(8, frames(PEARL + "-break"));
        assertEquals(8, frames(PEARL + "-glow"));
        assertEquals(8, frames(PEARL + "-release"));
        BufferedImage light =
                ImageIO.read(BACKDROP.resolve(SphereLooks.CORE_LIGHT + ".png").toFile());
        assertEquals(720, light.getWidth());
        assertEquals(720, light.getHeight());
        assertTrue(
                CONTENT.level(KEY).backdrop().pieces().containsKey(SphereLooks.CORE_LIGHT), "in the atlas, unplaced");
        assertTrue(CONTENT.level(KEY).backdrop().placed().stream()
                .noneMatch(placed -> placed.piece().equals(SphereLooks.CORE_LIGHT)));
    }

    /** Its bank by its sideways speed; its tells' frames by how far they have run; the spiral's tell; its orb. */
    @Test
    void aSeraphBanksFoldsAndFlares() {
        assertEquals(SeraphLooks.LEVEL, SeraphLooks.bankFrame(0));
        assertEquals(SeraphLooks.LEVEL, SeraphLooks.bankFrame(30), "level at a slow drift");
        assertEquals(2, SeraphLooks.bankFrame(-60), "10 degrees moving left");
        assertEquals(4, SeraphLooks.bankFrame(60), "10 degrees moving right");
        assertEquals(1, SeraphLooks.bankFrame(-140));
        assertEquals(0, SeraphLooks.bankFrame(-260), "30 degrees at the mirror's cap");
        assertEquals(6, SeraphLooks.bankFrame(260));
        assertEquals(6, SeraphLooks.bankFrame(900));
        assertEquals(0, SeraphLooks.shareFrame(0, 6));
        assertEquals(5, SeraphLooks.shareFrame(1, 6), "folded at the fold's end");
        assertEquals(11, SeraphLooks.shareFrame(0.999, 12));
        assertEquals(0, SeraphLooks.flareFrame(0, 6));
        assertEquals(5, SeraphLooks.flareFrame(6L * SeraphLooks.FLARE_FRAME_TICKS - 1, 6));
        assertEquals(-1, SeraphLooks.flareFrame(6L * SeraphLooks.FLARE_FRAME_TICKS, 6), "0.4 s, then gone");
        assertEquals(24, 6 * SeraphLooks.FLARE_FRAME_TICKS, "0.4 s");
        Set<Integer> glide = new java.util.HashSet<>();
        for (long tick = 0; tick < 60; tick++) {
            glide.add(SeraphLooks.glideFrame(tick, 3, 4));
        }
        assertEquals(4, glide.size(), "10 fps through its four frames");
        assertEquals(SeraphLooks.SLUG + "-shot", BulletLooks.OWN.get(SERAPH));
        assertTrue(BulletLooks.OWN_PULSING.contains(SERAPH), "an orb pulsing, not headings");
    }

    /**
     * Flown on medium with the ship invulnerable, holding fire and sweeping side to side: the first Seraph waits gone,
     * lands, mirrors banked both ways, folds (drawn whole) before each jump, is gone between its rings, and shows its
     * leaving tell untilted before it leaves.
     */
    @Test
    void aSeraphGoesThroughItsCycle() {
        Sortie sortie = sortie(Difficulty.MEDIUM);
        Set<Teleporting.Phase> phases = EnumSet.noneOf(Teleporting.Phase.class);
        Set<Integer> banks = new java.util.HashSet<>();
        int last = SimStep.ticks(62);
        for (int step = 0; step < last && !sortie.complete(); step++) {
            sortie.step(sweep(sortie));
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals(SERAPH)) {
                    continue;
                }
                Teleporting.Phase phase = enemy.teleport().phase();
                phases.add(phase);
                assertTrue(VrellLooks.jumpsOut(enemy), "it leaves by a jump");
                switch (phase) {
                    case ENTER, GONE -> assertEquals(0, VrellLooks.opacity(enemy), 1e-6, "not drawn while gone");
                    case FOLD, LEAVE_FOLD, GLIDE -> assertEquals(1, VrellLooks.opacity(enemy), 1e-6);
                    default -> {}
                }
                if (phase == Teleporting.Phase.GLIDE && enemy.shadow().mirroring()) {
                    banks.add(SeraphLooks.bankFrame(SeraphLooks.sideways(enemy)));
                }
            }
        }
        assertTrue(
                phases.containsAll(EnumSet.of(
                        Teleporting.Phase.GONE,
                        Teleporting.Phase.GLIDE,
                        Teleporting.Phase.FOLD,
                        Teleporting.Phase.FADE,
                        Teleporting.Phase.LEAVE_FOLD)),
                "the whole cycle: " + phases);
        assertTrue(banks.stream().anyMatch(frame -> frame < SeraphLooks.LEVEL), "banked moving left: " + banks);
        assertTrue(banks.stream().anyMatch(frame -> frame > SeraphLooks.LEVEL), "banked moving right: " + banks);
        assertTrue(banks.contains(SeraphLooks.LEVEL), "level when still");
    }

    /**
     * The core light: nothing before t=118, then growing smoothly (well under 32 levels' worth a second), its centre
     * coming down toward the screen and its scale up, at full by the level's end (t=266); held after.
     */
    @Test
    void theCoreLightGrowsSmoothlyToTheEnd() {
        assertEquals(0, SphereLooks.weight(0), 1e-9);
        assertEquals(0, SphereLooks.weight(117.9), 1e-9);
        assertEquals(0, SphereLooks.weight(118), 1e-9);
        assertEquals(0.15, SphereLooks.weight(124), 1e-6);
        assertEquals(1, SphereLooks.weight(266), 1e-9);
        assertEquals(1, SphereLooks.weight(300), 1e-9);
        assertEquals(PlayField.HEIGHT + 150, SphereLooks.centreY(266), 1e-9, "150 px above the top edge");
        double step = 1.0 / SimStep.PER_SECOND;
        double before = 0;
        double y = SphereLooks.centreY(118);
        for (double t = 118; t <= 266; t += step) {
            double weight = SphereLooks.weight(t);
            assertTrue(weight >= before - 1e-9, "never dimming: " + t);
            assertTrue(weight - before < 1.0 / 32 / SimStep.PER_SECOND, "smooth: " + t);
            assertTrue(SphereLooks.centreY(t) <= y + 1e-9, "coming down: " + t);
            before = weight;
            y = SphereLooks.centreY(t);
        }
        double scale = SphereLooks.at(266, 2);
        double reach = PlayField.HEIGHT + 720 * scale / 2 - SphereLooks.centreY(266);
        assertTrue(reach >= PlayField.HEIGHT / 2.0, "its disc reaches the top half by the end: " + reach);
        assertEquals(SphereLooks.LEVEL, 47);
    }

    /** The pearl's remembered image loops at 6 fps over its tumble; its release plays over its break, slower. */
    @Test
    void thePearlFlickersAndLetsGo() {
        Set<Integer> seen = new java.util.HashSet<>();
        for (int tick = 0; tick < SimStep.PER_SECOND * 2; tick++) {
            int frame = PropPasses.glowFrame(tick, 7, 8);
            assertTrue(frame >= 0 && frame < 8);
            seen.add(frame);
        }
        assertEquals(8, seen.size(), "every frame in 2 s at 6 fps");
        assertEquals(PropPasses.glowFrame(0, 7, 8), PropPasses.glowFrame(SimStep.PER_SECOND / 6.0 - 0.5, 7, 8));
        assertTrue(CeresLooks.RELEASE_FRAME_TICKS > CeresLooks.BREAK_FRAME_TICKS, "the ring widens slower");
        LevelData level = CONTENT.level(KEY);
        assertTrue(level.debris().orElseThrow().chunks().containsKey("memory-pearl"));
    }

    /** The tracker counts to the target (14 of 17, easy 11 of 15, hard 16 of 18); the portraits' static at step 5. */
    @Test
    void theTrackerCountsTheSeraphsAndTheRadioCrackles() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        assertEquals("SERAPHS", script.objectives().secondary().label());
        for (var expected : List.of(
                List.of(Difficulty.EASY, "0 / 11"),
                List.of(Difficulty.MEDIUM, "0 / 14"),
                List.of(Difficulty.HARD, "0 / 16"))) {
            Difficulty difficulty = (Difficulty) expected.get(0);
            Sortie sortie = sortie(difficulty);
            assertEquals(
                    expected.get(1),
                    MissionPanel.escapesCount(
                            0,
                            sortie.escapesTotal(),
                            sortie.script().objectives().secondary().escapesAllowed()),
                    difficulty.name());
        }
        LevelData level = CONTENT.level(KEY);
        assertEquals(5, level.radioNoiseStep());
        assertEquals(0.5f, MissionPanel.portraitNoise(0, 5, true), 1e-6, "the friendly portraits' static, step 5");
        assertEquals(0, MissionPanel.portraitNoise(0, 5, false), 1e-6, "the Choir's clean");
        assertTrue(MissionPanel.cleanChannel("The Choir", 5));
    }

    /**
     * At 1:1 at t=262 (the core light at 97 %, the deep's brightest stretch, no rib over it): over the brightest 88 px
     * window where a Seraph flies (its centre 110–290 px under the top), its body (with its light) stands well off the
     * stone in every state; over the brightest 15 px window anywhere, every bullet's dark rim lies far below the light
     * and its core above the window's mean (the outline carries it: the core cannot stand far above a light this bright).
     */
    @Test
    void theSeraphAndTheBulletsReadOverTheCoreLight() throws IOException {
        float[][] screen = screen(262);
        int w = PlayField.WIDTH;
        int[] seraphWindow = brightest(screen, 88, 110 - 44, 290 - 44);
        float[][] glow = read(SPRITES.resolve(SERAPH + SeraphLooks.GLOW + ".png"));
        for (String state : List.of(SERAPH + "_0", SERAPH + "-bank_0", SERAPH + "-bank_6", SERAPH + "-fold_5")) {
            BufferedImage body = ImageIO.read(SPRITES.resolve(state + ".png").toFile());
            double sum = 0;
            int n = 0;
            for (int y = 0; y < 88; y++) {
                for (int x = 0; x < 88; x++) {
                    int argb = body.getRGB(x, y);
                    float a = (argb >>> 24) / 255f;
                    if (a < 0.8f) {
                        continue;
                    }
                    float[] under = screen[(seraphWindow[1] + y) * w + seraphWindow[0] + x];
                    float[] light = glow[(y + 8) * 104 + x + 8];
                    float[] seen = new float[3];
                    for (int c = 0; c < 3; c++) {
                        seen[c] = Math.min(
                                255, under[c] * (1 - a) + (argb >> (16 - 8 * c) & 255) * a + light[c] * light[3]);
                    }
                    sum += Math.abs(lum(seen) - lum(under));
                    n++;
                }
            }
            double apart = sum / n;
            assertTrue(apart >= 60, state + " stands off the stone over the core light: " + apart);
        }
        int[] bulletWindow = brightest(screen, 15, 0, PlayField.HEIGHT - 15);
        double mean = 0;
        double brightest = 0;
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 15; x++) {
                double l = lum(screen[(bulletWindow[1] + y) * w + bulletWindow[0] + x]);
                mean += l / 225;
                brightest = Math.max(brightest, l);
            }
        }
        assertTrue(brightest > 200, "the window is in the light: " + brightest);
        for (String bullet : List.of("orb_0", "orb-medium_0", SERAPH + "-shot_0", SERAPH + "-shot_2")) {
            BufferedImage orb = ImageIO.read(SPRITES.resolve(bullet + ".png").toFile());
            double core = 0;
            double rim = 255;
            int ox = bulletWindow[0] + 7 - orb.getWidth() / 2;
            int oy = bulletWindow[1] + 7 - orb.getHeight() / 2;
            for (int y = 0; y < orb.getHeight(); y++) {
                for (int x = 0; x < orb.getWidth(); x++) {
                    int argb = orb.getRGB(x, y);
                    float a = (argb >>> 24) / 255f;
                    if (a < 0.8f || ox + x < 0 || oy + y < 0 || ox + x >= w || oy + y >= PlayField.HEIGHT) {
                        continue;
                    }
                    float[] under = screen[(oy + y) * w + ox + x];
                    float[] seen = new float[3];
                    for (int c = 0; c < 3; c++) {
                        seen[c] = under[c] * (1 - a) + (argb >> (16 - 8 * c) & 255) * a;
                    }
                    core = Math.max(core, lum(seen));
                    rim = Math.min(rim, lum(seen));
                }
            }
            assertTrue(mean - rim >= 150, bullet + ": rim " + rim + " vs the light's " + mean);
            assertTrue(core - mean >= 15, bullet + ": core " + core + " vs the light's " + mean);
        }
    }

    /**
     * The play field at level time {@code t} without units (y down, RGB 0-255): the 540 rows of {@code deep-4} whose top
     * 200 are brightest, the core light added over them as the game draws it (its curve, nearest sampling).
     */
    private static float[][] screen(double t) throws IOException {
        int w = PlayField.WIDTH;
        int h = PlayField.HEIGHT;
        float[][] deep = read(BACKDROP.resolve("deep-4.png"));
        int deepHeight = deep.length / w;
        int top = 0;
        double best = -1;
        for (int y0 = 0; y0 + h <= deepHeight; y0 += 24) {
            double sum = 0;
            for (int y = y0; y < y0 + 200; y++) {
                for (int x = 0; x < w; x++) {
                    sum += lum(deep[y * w + x]);
                }
            }
            if (sum > best) {
                best = sum;
                top = y0;
            }
        }
        float[][] light = read(BACKDROP.resolve(SphereLooks.CORE_LIGHT + ".png"));
        double scale = SphereLooks.at(t, 2);
        double weight = SphereLooks.weight(t);
        double centreBelowTop = SphereLooks.at(t, 1);
        float[][] screen = new float[w * h][];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float[] px = deep[(top + y) * w + x].clone();
                int lx = (int) Math.floor((x + 0.5 - SphereLooks.X) / scale + 360);
                int ly = (int) Math.floor((y + 0.5 - centreBelowTop) / scale + 360);
                if (lx >= 0 && ly >= 0 && lx < 720 && ly < 720) {
                    float[] add = light[ly * 720 + lx];
                    for (int c = 0; c < 3; c++) {
                        px[c] = (float) Math.min(255, px[c] + add[c] * add[3] * weight);
                    }
                }
                screen[y * w + x] = px;
            }
        }
        return screen;
    }

    /** The left and top of the brightest {@code size} px window whose top lies in [{@code from}, {@code to}]. */
    private static int[] brightest(float[][] screen, int size, int from, int to) {
        int w = PlayField.WIDTH;
        int[] at = {0, from};
        double best = -1;
        for (int y = from; y <= to; y += 2) {
            for (int x = 0; x + size <= w; x += 2) {
                double sum = 0;
                for (int dy = 0; dy < size; dy += 2) {
                    for (int dx = 0; dx < size; dx += 2) {
                        sum += lum(screen[(y + dy) * w + x + dx]);
                    }
                }
                if (sum > best) {
                    best = sum;
                    at = new int[] {x, y};
                }
            }
        }
        return at;
    }

    /** An image's pixels, row by row from its top: RGB 0-255 and alpha 0-1. */
    private static float[][] read(Path file) throws IOException {
        BufferedImage image = ImageIO.read(file.toFile());
        float[][] pixels = new float[image.getWidth() * image.getHeight()][];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                pixels[y * image.getWidth() + x] =
                        new float[] {argb >> 16 & 255, argb >> 8 & 255, argb & 255, (argb >>> 24) / 255f};
            }
        }
        return pixels;
    }

    private static double lum(float[] rgb) {
        return 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2];
    }

    private static Sortie sortie(Difficulty difficulty) {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        30,
                        difficulty),
                SimSpecs.level(CONTENT, KEY, difficulty),
                SimSpecs.rules(CONTENT, KEY, difficulty).withInvulnerableShip(),
                60);
    }

    /** Guns held; low, sweeping across the field and back every 4 s, resting a second at each side. */
    private static int sweep(Sortie sortie) {
        double phase = sortie.levelSeconds() % 8;
        double tx = phase < 4 ? 400 : 80;
        int commands = 0;
        double x = sortie.ship().x();
        if (x < tx - 3) {
            commands |= Command.RIGHT.bit();
        } else if (x > tx + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() > 80) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
