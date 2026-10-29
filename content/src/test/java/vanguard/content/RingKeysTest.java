package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.PartSpec;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPieceSpec;

/**
 * M9 part H, step H1c: the ringed boss's keys ({@link RingBlock}), their rules ({@link RingRules}) and checks
 * ({@link RingChecks}) on a test boss (src/test/resources/rings/test-sovereign.yaml) that mirrors the simulation's
 * fixture {@code Act5HSpecs.SOVEREIGN}: the parsed {@link RingSpec} equals the fixture's (the bearings' −90° conversion,
 * the hard speeds and step, the easy and hard arcs), the batteries appended as parts, their attacks taken from the
 * ground units, each check with a failing sample; and a level's mass-relay flash.
 */
class RingKeysTest {
    private static final String BOSS = "enemies/bosses/test-sovereign/data.yaml";
    private static final String YAML = resource("/rings/test-sovereign.yaml");
    private static final String LEVEL_KEY = "act-1-first-contact/level-01-break-at-dawn";
    private static final String LEVEL = "campaign/" + LEVEL_KEY + "/data.yaml";
    private static final Content CONTENT = load(YAML);

    private static final List<Integer> SAMS = List.of(1, 2, 3, 4, 5, 6);
    private static final List<Integer> RAILS = List.of(7, 8, 9, 10);
    private static final List<Integer> LASERS = List.of(11, 12, 13);
    private static final int SAM = 0;
    private static final int RAIL = 1;
    private static final int LASER = 2;
    private static final int SPIRAL = 6;
    private static final int CORE_RAIL = 7;

    private static String resource(String path) {
        try (InputStream in = RingKeysTest.class.getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<DataFile> files(String boss) {
        List<DataFile> files = new ArrayList<>(DesignTree.dataFiles());
        files.add(new DataFile(BOSS, boss));
        return files;
    }

    private static Content load(String boss) {
        return ContentLoader.load(files(boss));
    }

    /** The problems the loader reports for the test boss with {@code from} replaced by {@code to}. */
    private static String problems(String from, String to) {
        assertTrue(YAML.contains(from), from);
        return assertThrows(ContentException.class, () -> load(YAML.replace(from, to)))
                .getMessage();
    }

    private static void fails(String from, String to, String expected) {
        String message = problems(from, to);
        assertTrue(message.contains(expected), message);
    }

    private static SetPieceSpec boss(Difficulty difficulty) {
        return BossRules.boss(CONTENT, new LevelData.BossPlacement("test-sovereign", 3, 240, 1), difficulty);
    }

    private static BossSpec script(Difficulty difficulty) {
        return boss(difficulty).boss().orElseThrow();
    }

    /** The fixture's rings at {@code arcDegrees} and the speed factor (Act5HSpecs.rings), with the core block. */
    private static RingSpec expected(double arcDegrees, double speed, int rail, EnemySpec dart) {
        return new RingSpec(
                        List.of(
                                new RingSpec.Ring(
                                        "outer", 200, 1, Math.toRadians(6) * speed, 0, SAMS, SAM, Optional.empty()),
                                new RingSpec.Ring(
                                        "middle",
                                        140,
                                        -1,
                                        Math.toRadians(10) * speed,
                                        0,
                                        RAILS,
                                        RAIL,
                                        Optional.empty()),
                                new RingSpec.Ring(
                                        "inner",
                                        85,
                                        1,
                                        0,
                                        0,
                                        LASERS,
                                        LASER,
                                        Optional.of(new RingSpec.Step(Math.toRadians(120), 4 / speed, 4, 0.4)))),
                        Math.toRadians(arcDegrees),
                        new BossSpec.Clamp(Math.toRadians(-60), Math.toRadians(60)),
                        4,
                        2,
                        List.of(1.0, 1.25, 1.5),
                        List.of(1, 2, 3, 0),
                        0.15)
                .withCore(Optional.of(new RingSpec.Core(
                        0,
                        0.5,
                        3,
                        Optional.of(new EnemySpec.Shield(80, 2.0, 0.5, 3.0)),
                        SPIRAL,
                        4,
                        2,
                        0.6,
                        rail,
                        rail < 0 ? 1.0 : 0.5,
                        Optional.of(new RingSpec.Bays(
                                4,
                                40,
                                0,
                                new EnemySpec.Launcher(dart, 4, 5, 0.4, 1, 240, Math.toRadians(30), 0.4, 16),
                                0.5,
                                72)))));
    }

    private static EnemySpec dart(RingSpec rings) {
        return rings.core().orElseThrow().bays().orElseThrow().launcher().unit();
    }

    @Test
    void theRingsParseIntoTheFixturesSpec() {
        RingSpec medium = script(Difficulty.MEDIUM).rings().orElseThrow();
        assertEquals("dart-drone", dart(medium).slug());
        assertEquals(expected(120, 1, CORE_RAIL, dart(medium)), medium);
        RingSpec hard = script(Difficulty.HARD).rings().orElseThrow();
        // The core's shield 80 by hard's HP lever (×1.3).
        RingSpec hardExpected = expected(150, 1.2, CORE_RAIL, dart(hard));
        RingSpec.Core core = hardExpected.core().orElseThrow();
        assertEquals(
                hardExpected.withCore(Optional.of(new RingSpec.Core(
                        core.part(),
                        core.glowSeconds(),
                        core.openingSeconds(),
                        Optional.of(new EnemySpec.Shield(104, 2.0, 0.5, 3.0)),
                        core.spiral(),
                        core.spiralSeconds(),
                        core.restSeconds(),
                        core.spiralTellSeconds(),
                        core.rail(),
                        core.railBelow(),
                        core.bays()))),
                hard);
        RingSpec easy = script(Difficulty.EASY).rings().orElseThrow();
        // Easy: the arc 90°, no core rail (its attack's `only`), the shield 80 × 0.75.
        assertEquals(Math.toRadians(90), easy.arcRadians());
        assertEquals(Math.toRadians(6), easy.rings().getFirst().radiansPerSecond());
        assertEquals(-1, easy.core().orElseThrow().rail());
        assertEquals(60, easy.core().orElseThrow().shield().orElseThrow().hp());
    }

    @Test
    void theBatteriesArePartsOnTheirRings() {
        EnemyData sovereign = CONTENT.enemy("test-sovereign");
        assertTrue(sovereign.ringed());
        List<EnemyData.PartData> parts = sovereign.partList().orElseThrow();
        assertEquals(14, parts.size());
        assertEquals(
                List.of(
                        "core", "sam-1", "sam-2", "sam-3", "sam-4", "sam-5", "sam-6", "rail-1", "rail-2", "rail-3",
                        "rail-4", "laser-1", "laser-2", "laser-3"),
                parts.stream().map(EnemyData.PartData::name).toList());
        List<PartSpec> specs = boss(Difficulty.MEDIUM).parts();
        // As the fixture places them: a mount at a sits at (−r sin a, −r cos a) from the centre, y up.
        double[][] rings = {{200, 60, 220, 30}, {140, 90, 330, 36}, {85, 120, 440, 32}};
        int p = 1;
        for (double[] ring : rings) {
            for (int k = 0; k < 360 / ring[1]; k++, p++) {
                double a = Math.toRadians(ring[1] * k);
                PartSpec part = specs.get(p);
                assertEquals(-ring[0] * Math.sin(a), part.dx(), 1e-9, part.name());
                assertEquals(-ring[0] * Math.cos(a), part.dy(), 1e-9, part.name());
                assertEquals(ring[2], part.hp(), part.name());
                assertEquals(new Hitbox(ring[3], ring[3]), part.box(), part.name());
                assertEquals(6, part.bounty());
                assertFalse(part.vital());
            }
        }
        assertTrue(specs.getFirst().vital());
        assertEquals(5000, specs.getFirst().hp());
    }

    @Test
    void theBatteriesFireTheirUnitsAttacks() {
        BossSpec medium = script(Difficulty.MEDIUM);
        List<BossSpec.Attack> attacks = medium.attacks();
        BossSpec.Attack sam = attacks.get(SAM);
        assertEquals(BossSpec.Pattern.AIMED, sam.pattern());
        assertEquals(SAMS, sam.parts());
        assertTrue(sam.stagger());
        assertEquals(6, sam.gun().intervalSeconds(), 1e-9);
        assertEquals(2, sam.gun().burst(), "the SAM Nest's salvo");
        assertEquals(0.4, sam.gun().homing().orElseThrow().hatchSeconds(), "the SAM Nest's hatch");
        BossSpec.Attack rail = attacks.get(RAIL);
        assertEquals(BossSpec.Pattern.LINE, rail.pattern());
        assertEquals(RAILS, rail.parts());
        EnemySpec.LaserLine line = rail.line().orElseThrow();
        assertEquals(1.0, line.telegraphSeconds(), "the Rail Bunker's telegraph");
        assertEquals(0.2, line.liveSeconds());
        assertEquals(8, line.bandPx());
        assertEquals(10, line.damage(), "heavy");
        assertEquals(100, line.holdWithinPx());
        assertEquals(5, rail.gun().intervalSeconds(), 1e-9);
        BossSpec.Attack laser = attacks.get(LASER);
        assertEquals(BossSpec.Pattern.SWEEP, laser.pattern());
        EnemySpec.Sweep sweep = laser.sweep().orElseThrow();
        assertEquals(300, sweep.length(), "the boss's beam");
        assertEquals(0.8, sweep.telegraphSeconds(), "the Sentinel Tower's telegraph");
        assertEquals(1.6, sweep.sweepSeconds());
        assertEquals(Math.toRadians(120), sweep.arcRadians(), 1e-12);
        assertEquals(BossSpec.Pattern.LINE, attacks.get(CORE_RAIL).pattern());
        assertEquals(BossSpec.Pattern.SPIRAL, attacks.get(SPIRAL).pattern());
        assertEquals(List.of(SPIRAL, CORE_RAIL), medium.phases().getLast().attacks());
        assertEquals(List.of(0), medium.phases().getLast().exposes());
        // The battery intervals by the ring factor (hard ×0.8, easy ×1.3), the units' own hooks on their shape.
        BossSpec hard = script(Difficulty.HARD);
        assertEquals(4.8, hard.attacks().get(SAM).gun().intervalSeconds(), 1e-9);
        assertEquals(3, hard.attacks().get(SAM).gun().burst(), "the SAM Nest's hard salvo of 3");
        assertEquals(0.8, hard.attacks().get(RAIL).line().orElseThrow().telegraphSeconds(), "the bunker's hard 0.8");
        assertEquals(
                Math.toRadians(160),
                hard.attacks().get(LASER).sweep().orElseThrow().arcRadians(),
                1e-12);
        BossSpec easy = script(Difficulty.EASY);
        assertEquals(7.8, easy.attacks().get(SAM).gun().intervalSeconds(), 1e-9);
        assertEquals(6.5, easy.attacks().get(RAIL).gun().intervalSeconds(), 1e-9);
        assertEquals(List.of(SPIRAL), easy.phases().getLast().attacks(), "no core rail on easy");
    }

    @Test
    void theRingChecksFailTheirSamples() {
        fails("fire_arc: {degrees: 120", "fire_arc: {degrees: 200", "a fire arc of 60.0–180.0°");
        fails("{arc: 90, interval: 1.3}", "{arc: 40, interval: 1.3}", "easy.ring_arena.arc");
        fails("radius: 200", "radius: 240", "fit the field across");
        fails("until: {locks: 2}", "until: {locks: 1}", "more locked rings each");
        fails("until: {parts: [core]}", "until: {locks: 3}", "the last phase does not end on locked rings");
        fails("attack: sam}", "attack: nope}", "no attack named 'nope'");
        fails("hold: 4, tell", "hold: 2, tell", "ends inside the hold");
        fails("from: sam-nest, interval: 6", "from: sam-nest, interval: 2", "the hatch (0.4 s) opens inside");
        fails("exposes: [core]", "exposes: [sam-1]", "a phase exposes the core");
        fails("part: core\n", "part: sam-1\n", "the core is no battery");
        fails("rail: {attack: core-rail", "rail: {attack: core-spiral", "is a spiral, not a laser-line");
        fails("from: dart-drone", "from: sam-nest", "the bays launch a plain air unit");
        fails("from: sam-nest,", "from: nobody,", "no ground unit 'nobody'");
        fails("pattern: aimed, from: sam-nest", "pattern: laser-line, from: sam-nest", "fires a aimed");
        fails(
                "{name: sam, pattern: aimed, from: sam-nest,",
                "{name: sam, pattern: aimed, speed: 100, from: sam-nest,",
                "takes its bullet, speed");
        fails(
                "    - name: inner\n",
                "    - {name: fourth, radius: 50, speed: 5, direction: cw, batteries: {count: 2, part: {hp: 10, box:"
                        + " [20, 20], bounty: 1}, attack: sam}}\n    - name: inner\n",
                "at most 3 rings");
    }

    /** The problems the loader reports for the test boss with design file {@code path} edited from {@code from} to {@code to}. */
    private static String problemsIn(String path, String from, String to) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : files(YAML)) {
            if (file.path().equals(path)) {
                assertTrue(file.text().contains(from), from);
                files.add(new DataFile(path, file.text().replace(from, to)));
            } else {
                files.add(file);
            }
        }
        return assertThrows(ContentException.class, () -> ContentLoader.load(files))
                .getMessage();
    }

    @Test
    void onlyARingedBossHasRingKeysAndItsLevelHoldsItsRings() {
        String kraken = "enemies/bosses/harbour-kraken/data.yaml";
        String locks = problemsIn(kraken, "until: {parts: [head], left: 0}", "until: {locks: 1}");
        assertTrue(locks.contains("only a ringed boss's phases end on locked rings"), locks);
        String arena = problemsIn(kraken, "hard: {telegraph: 1.1,", "hard: {ring_arena: {arc: 120}, telegraph: 1.1,");
        assertTrue(arena.contains("only a ringed boss (boss.rings) has ring changes"), arena);
        String nest = "enemies/ground/sam-nest/data.yaml";
        String from = problemsIn(nest, "    name: salvo\n", "    name: salvo\n    from: rail-bunker\n");
        assertTrue(from.contains("only a boss's attack is taken from a unit"), from);
        String level = "campaign/act-1-first-contact/level-07-brood-carrier/data.yaml";
        String placed = problemsIn(
                level, "  enemy: brood-carrier\n  t: 100\n  x: 240", "  enemy: test-sovereign\n  t: 100\n  x: 200");
        assertTrue(placed.contains("the ring 'outer' (215.0 px out) lies inside the field across at x 200"), placed);
    }

    @Test
    void aBossWithoutRingsHasNone() {
        assertTrue(BossRules.boss(CONTENT, new LevelData.BossPlacement("harbour-kraken", 3, 240, 1), Difficulty.MEDIUM)
                .boss()
                .orElseThrow()
                .rings()
                .isEmpty());
        assertFalse(CONTENT.enemy("abyssal-maw").ringed());
    }

    @Test
    void aLevelsRelayFlashMapsToTheSimulation() {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            files.add(
                    file.path().equals(LEVEL)
                            ? new DataFile(
                                    LEVEL,
                                    file.text()
                                            + "\nrelay: {t: 59.8, telegraph: 1.2, live: 0.2, band: 16, damage: 20,"
                                            + " from: right, easy: {telegraph: 1.5}}\n")
                            : file);
        }
        Content content = ContentLoader.load(files);
        HazardSpec.Relay medium = SimSpecs.level(content, LEVEL_KEY, Difficulty.MEDIUM)
                .hazards()
                .relay()
                .orElseThrow();
        assertEquals(new HazardSpec.Relay(59.8, 1.2, 0.2, 16, 20, true), medium);
        assertEquals(
                1.5,
                SimSpecs.level(content, LEVEL_KEY, Difficulty.EASY)
                        .hazards()
                        .relay()
                        .orElseThrow()
                        .telegraphSeconds());
        assertTrue(SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.MEDIUM)
                .hazards()
                .relay()
                .isEmpty());
    }
}
