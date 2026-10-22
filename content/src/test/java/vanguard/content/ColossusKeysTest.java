package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.BreachArena;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Section;

/**
 * M7 part H (step PH2): the loader's records, {@link ColossusChecks} and {@link ColossusRules} for Level 21 Dust Colossus
 * and its boss (design/campaign Level 21; design/enemies/bosses/dust-colossus): the breach cycle, the suction, the
 * vents, the eruption and the debris in the stat block, the crawling arena (a moving arena of a breaching boss) with
 * its ease, the level's waves re-planned to the Act 2+ rules (round 43, H.1), the Act 3 outro and {@code next_act}.
 * The fight itself is flown from step H4 (Unmapped).
 */
class ColossusKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String LEVEL_KEY = "act-3-red-dust/level-21-dust-colossus";
    private static final String LEVEL = "campaign/act-3-red-dust/level-21-dust-colossus/data.yaml";
    private static final String BOSS = "enemies/bosses/dust-colossus/data.yaml";

    private static LevelData level() {
        return CONTENT.level(LEVEL_KEY);
    }

    @Test
    void theColossusBreachesInItsCycleAndPullsTowardItsMouth() {
        EnemyData colossus = CONTENT.enemy("dust-colossus");
        assertTrue(colossus.breaches());
        assertTrue(colossus.arenaBoss());
        assertEquals("buried", colossus.layer());
        EnemyData.Breach breach = colossus.movement().breach().orElseThrow();
        assertEquals(12.8, breach.cycleSeconds(), 1e-9, "rise 1.5 + open 7 + bite 0.8 + dive 1.5 + buried 2");
        assertEquals(new Point(450, 260), breach.points().get("right"));
        assertEquals(new Span(160, 420), breach.follow().get("left"));
        assertEquals(
                List.of("Circling", "Hunting", "Breaching anywhere"),
                breach.orders().stream().map(EnemyData.BreachOrder::phase).toList());
        assertFalse(breach.orders().getFirst().follows());
        assertTrue(breach.orders().get(1).follows());
        assertEquals(0.8, breach.biteTell());
        assertEquals(1.0, colossus.breach(Difficulty.EASY).orElseThrow().biteTell());
        assertEquals(0.6, colossus.breach(Difficulty.HARD).orElseThrow().biteTell());
        assertEquals(45, colossus.suction(Difficulty.MEDIUM).orElseThrow().speed());
        assertEquals(30, colossus.suction(Difficulty.EASY).orElseThrow().speed());
        assertEquals(55, colossus.suction(Difficulty.HARD).orElseThrow().speed());
        assertEquals(1.8, colossus.eruption(Difficulty.EASY).orElseThrow().marker());
        assertEquals(1.2, colossus.eruption(Difficulty.HARD).orElseThrow().marker());
        EnemyData.BossData script = colossus.boss().orElseThrow();
        assertEquals(List.of(120.0, 200.0, 280.0), script.vents().orElseThrow().distances());
        assertEquals(1.2, script.debris().orElseThrow().every().get("Hunting"));
        assertEquals(0.8, script.debris().orElseThrow().every().get("Breaching anywhere"));
        // H.5: the boss keeps its 22.7 % of budget(21) at the level's measured scale (step L21R after round 44 Q.1's
        // rework): 480 × 2.56 × 0.50 = 614.
        assertEquals(480, colossus.bounty());
        assertEquals(6440, colossus.hp());
    }

    @Test
    void theBreachMapsIntoTheSimulationsFrame() {
        EnemyData colossus = CONTENT.enemy("dust-colossus");
        BossSpec.Breach breach =
                ColossusRules.breach(CONTENT, colossus, Difficulty.MEDIUM).orElseThrow();
        // y up from the bottom edge: front (240, 445; round 44 Q.11: 95 px below the top edge, clear of the boss bar),
        // left (30, 280), right (450, 280), rear (240, 25).
        assertEquals(
                List.of(
                        new BossSpec.Point(240, 445),
                        new BossSpec.Point(30, 280),
                        new BossSpec.Point(450, 280),
                        new BossSpec.Point(240, 25)),
                breach.points());
        assertEquals(new BossSpec.Clamp(100, 380), breach.clamps().getFirst());
        assertEquals(new BossSpec.Clamp(120, 380), breach.clamps().get(1), "the sides' 160-420 from the top");
        assertEquals(
                List.of(
                        BreachArena.Pose.REAR,
                        BreachArena.Pose.LEFT,
                        BreachArena.Pose.REAR,
                        BreachArena.Pose.RIGHT,
                        BreachArena.Pose.FRONT),
                breach.order(1).poses());
        assertEquals(List.of(BreachArena.Pose.UNDER), breach.order(2).poses());
        assertEquals(10, breach.biteDamage(), "heavy");
        assertEquals(70, breach.biteRadius());
        assertEquals(6, breach.introSeconds());
        assertEquals(0.5, breach.glowSeconds());
        assertEquals(
                55,
                ColossusRules.suction(colossus, Difficulty.HARD).orElseThrow().speedAt(100));
    }

    @Test
    void theLevelsArenaCrawlsAndItsBossBreaches() {
        LevelData level = level();
        LevelData.Section arena = level.sections().get(4);
        assertTrue(arena.isArena());
        assertEquals(2, arena.easeSeconds(), "140 -> 20 over the arena's first 2 s (H.7a)");
        // ART-H1's seams (briefs/l21-art-names.md): the arena's tile from 16,810 (its top edge), 19,190 at t=245.
        assertEquals(16810, level.scrollAt(105) + 540, 1e-6);
        assertEquals(19190, level.scrollAt(245), 1e-6);
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_KEY, Difficulty.MEDIUM);
        Section crawl = script.sections().get(4);
        assertTrue(crawl.crawls());
        assertEquals(20, crawl.speed());
        assertEquals(2, crawl.easeSeconds());
        assertFalse(script.sections().get(5).crawls());
        BossSpec boss = script.setPieces().getLast().boss().orElseThrow();
        assertTrue(boss.breach().isPresent());
        assertEquals(45, boss.suction().orElseThrow().speed());
        assertEquals(Layer.BURIED, boss.layer());
        assertEquals(Layer.BURIED, boss.arena().orElseThrow().partLayers().getFirst(), "the mouth starts buried");
        assertEquals(105, boss.arriveSeconds());
        // Sim step PH3's parts: the vents (parts 1-3, the burst the vent-burst attack), the eruption, the debris.
        BossSpec.Vents vents = boss.vents().orElseThrow();
        assertEquals(List.of(1, 2, 3), vents.parts());
        assertEquals("vent-burst", boss.attacks().get(vents.burst()).name());
        assertEquals(1.5, boss.eruption().orElseThrow().markerSeconds());
        assertEquals(10, boss.eruption().orElseThrow().damage());
        assertEquals(List.of(0.0, 1.2, 0.8), boss.debris().orElseThrow().everyByPhase());
        assertEquals(6, boss.debris().orElseThrow().contact(), "tiny");
        // Levels 05 and 07's moving arenas stop their ground while the clock is held, as before.
        for (String key :
                List.of("act-1-first-contact/level-05-crater-nest", "act-1-first-contact/level-07-brood-carrier")) {
            assertTrue(SimSpecs.level(CONTENT, key, Difficulty.MEDIUM).sections().stream()
                    .noneMatch(Section::crawls));
        }
    }

    @Test
    void theApproachMeetsTheActTwoRulesAndClearsBeforeTheBoss() {
        LevelData level = level();
        int waves = level.waves().stream()
                .filter(wave -> wave.fliesOn(Difficulty.MEDIUM))
                .mapToInt(wave -> wave.groupList().stream()
                        .mapToInt(LevelData.Group::count)
                        .sum())
                .sum();
        int ground = level.groundTargets().stream()
                .filter(target -> target.enemy().isPresent())
                .mapToInt(target -> target.at().size())
                .sum();
        assertEquals(158, waves, "round 44 Q.1: more firing groups as Level 20 (H.1's 96 failed the floor)");
        assertEquals(10, ground);
        double perMinute = (waves + ground) / (105.0 - 8.0) * 60;
        assertTrue(perMinute >= 55 && perMinute <= 105, "Q.1's 150-170 units: " + perMinute);
        double last =
                level.waves().stream().mapToDouble(LevelData.Wave::t).max().orElseThrow();
        assertEquals(97.5, last, "H.3: nothing enters after the Mote stream");
        assertEquals(0.50, level.bountyScale().orElseThrow(), "step L21R's measured fit (step H4's 0.74)");
        WeatherBlock.Ridge ridge = level.ridge().orElseThrow();
        assertTrue(ridge.leftward());
        assertEquals(64, ridge.t());
    }

    /**
     * Round 44 Q.5: the survey crawler (the secret's destructible) has the design's 28 HP on hard, 20 on easy and
     * medium (a destructible's {@code hard: {hp}}); only a destructible's change gives {@code hp}.
     */
    @Test
    void theSurveyCrawlerIsTougherOnHard() {
        for (Difficulty difficulty : Difficulty.values()) {
            double hp = SimSpecs.level(CONTENT, Level21Test.LEVEL, difficulty).groundObjects().stream()
                    .filter(object -> object.secret().equals("survey crawler"))
                    .findFirst()
                    .orElseThrow()
                    .hp();
            assertEquals(difficulty == Difficulty.HARD ? 28 : 20, hp, difficulty.name());
        }
        assertProblem(
                replace(LEVEL, "    hard: {hp: 28}   #", "    hard: {hp: 28, hits: 2}   #"), "a destructible's its hp");
        assertProblem(replace(LEVEL, "    hard: {hp: 28}   #", "    hard: {hp: -1}   #"), "hp");
        assertProblem(
                replace(
                        LEVEL,
                        "    count: 2\n    at: [[92, 120]",
                        "    count: 2\n    hard: {hp: 9}\n    at: [[92, 120]"),
                "an enemy nest's easy/hard gives its at");
    }

    @Test
    void actThreeEndsWithItsOutroAndActFourFollows() {
        ActData act = CONTENT.actOf(21).orElseThrow().getValue();
        assertEquals(21, act.levels().last());
        assertEquals(4, act.outro().orElseThrow().pages().size());
        assertEquals(Optional.of("act-complete"), act.outro().orElseThrow().music());
        // M8 part B: Act 4 is built from Level 22 on, so Act 3 names no next act (part H gives Act 4 its own).
        assertEquals(Optional.empty(), act.nextAct());
        assertEquals(22, CONTENT.actOf(22).orElseThrow().getValue().levels().first());
    }

    @Test
    void theValidatorKeepsTheBreachToItsBossAndItsArena() {
        assertProblem(
                replace(BOSS, "      - {phase: Hunting, points: [rear, left, rear, right, front], follow: true}\n", ""),
                "one order per boss phase");
        assertProblem(replace(BOSS, "{phase: Hunting, points:", "{phase: Hunted, points:"), "follow the boss's phases");
        assertProblem(
                replace(BOSS, "points: [under], follow: true", "points: [under, front], follow: true"), "only point");
        assertProblem(replace(BOSS, "      rear: [240, 515]\n", ""), "points: front, left, right and rear");
        assertProblem(replace(BOSS, "bite: {radius: 70, bullet: heavy}", "bite: {radius: 70, bullet: huge}"), "bullet");
        assertProblem(replace(BOSS, "    attack: vent-burst\n", "    attack: vent-blast\n"), "no attack 'vent-blast'");
        assertProblem(replace(BOSS, "    phase: Hunting\n", "    phase: Hunt\n"), "no phase 'Hunt'");
        assertProblem(replace(BOSS, "    layer: buried           #", "    layer: ground           #"), "starts buried");
        assertProblem(replace(BOSS, "  buried: 2       #", "  buried: 1       #"), "at least the tell");
        // The level: the boss arrives at the arena's start; only a breaching boss's arena eases in.
        assertProblem(replace(LEVEL, "  t: 105\n  x: 240", "  t: 104\n  x: 240"), "arrives at its arena's start");
        assertProblem(
                replace(
                        "campaign/act-1-first-contact/level-07-brood-carrier/data.yaml",
                        "    arena: true\n",
                        "    arena: true\n    ramp: 2\n"),
                "only a breaching boss's arena eases in");
    }

    private static void assertProblem(List<DataFile> files, String message) {
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(files));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    private static List<DataFile> replace(String path, String from, String to) {
        assertTrue(DesignTree.dataFiles().stream().anyMatch(f -> f.path().equals(path)), path);
        return DesignTree.dataFiles().stream()
                .map(f -> {
                    if (!f.path().equals(path)) {
                        return f;
                    }
                    assertTrue(f.text().contains(from), path + " has " + from);
                    return new DataFile(path, f.text().replace(from, to));
                })
                .toList();
    }
}
