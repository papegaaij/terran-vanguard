package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Intel;

/**
 * M7 part C's data (step C2; design/tech/architecture, Data file schemas): Level 16's {@code terrain} with the one
 * coordinate helper, its gates as linked nests ({@code link: pairs}), the crawlers' {@code crawl} paths, the {@code
 * links} secondary and the {@code first-wall-contact} event; the Tendril Anchor's {@code link} and hard-only orb, the
 * Threadcrawler's {@code crawl}, {@code cut}, {@code width} and {@code pores}; the {@code plateau} backdrop layer; the
 * intel's gate and crawler marks; loaded strictly and checked by the static validator. The simulation's mapping of
 * the new keys is step C4's.
 */
class CanyonDataTest {
    private static final String KEY = "act-3-red-dust/level-16-valles-canyon-run";
    private static final String PATH = "campaign/" + KEY + "/data.yaml";
    private static final String LEVEL_15 = "campaign/act-3-red-dust/level-15-olympus-descent/data.yaml";
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final LevelData LEVEL = CONTENT.level(KEY);
    private static final TerrainBlock TERRAIN = LEVEL.terrain().orElseThrow();

    private static List<DataFile> files(String path, UnaryOperator<String> change) {
        return DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(path) ? new DataFile(f.path(), change.apply(f.text())) : f)
                .toList();
    }

    private static void assertProblem(String path, String what, String with, String message) {
        Exception e = assertThrows(
                Exception.class,
                () -> ContentLoader.load(files(path, text -> {
                    assertTrue(text.contains(what), path + " has " + what);
                    return text.replace(what, with);
                })));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    /** The one helper: a point y px from the bottom edge lies on row groundScroll + y − 540 (y up). */
    @Test
    void theRowOfAScreenPointIsTheScrollAtWhichItReachedTheTopEdge() {
        assertEquals(1000, TerrainBlock.row(1000, 540));
        assertEquals(730, TerrainBlock.row(1000, 270));
        assertEquals(460, TerrainBlock.row(1000, 0));
        // a ground placement enters with its bottom edge at the top edge: its centre half its height above that row
        assertEquals(LEVEL.scrollAt(34) + 15, LEVEL.placementRow(34, 30), 1e-9);
        assertEquals(6545, LEVEL.scrollAt(34), 1e-6);
    }

    @Test
    void theCanyonsWallsFollowTheWallTable() {
        assertTrue(Double.isNaN(TERRAIN.leftAt(2000)), "no wall on the plateau");
        assertEquals(0, TERRAIN.leftAt(2850));
        assertEquals(480, TERRAIN.rightAt(2850));
        assertEquals(40, TERRAIN.leftAt(4180));
        assertEquals(150, TERRAIN.leftAt(28000));
        assertEquals(330, TERRAIN.rightAt(28000));
        assertTrue(Double.isNaN(TERRAIN.rightAt(35000)), "none after the outflow");
        // the explicit pillar rows keep the left channel at 130 px (the gap check's risk 4)
        assertEquals(70, TERRAIN.leftAt(19200));
        assertEquals(70, TERRAIN.leftAt(19476));
        assertEquals(400, TERRAIN.rightAt(19300));
        List<double[]> pillar = TERRAIN.spans(19300);
        assertEquals(2, pillar.size());
        assertArrayEquals(new double[] {70, 200}, pillar.get(0));
        assertArrayEquals(new double[] {250, 400}, pillar.get(1));
        List<double[]> fork = TERRAIN.spans(14000);
        assertArrayEquals(new double[] {40, 190}, fork.get(0));
        assertArrayEquals(new double[] {290, 440}, fork.get(1));
        assertTrue(TERRAIN.rock(14000, 240));
        assertTrue(TERRAIN.rock(14000, 30));
        assertFalse(TERRAIN.rock(14000, 100));
        assertEquals(10, TERRAIN.toFoot(14000, 300), 1e-9);
        // the walls' height ramps up at the mouth and down over the outflow (presentation only)
        assertEquals(0, TERRAIN.leftHeightAt(2850));
        assertEquals(0.5, TERRAIN.leftHeightAt(3515), 1e-9);
        assertEquals(1, TERRAIN.rightHeightAt(20000));
        assertEquals(0.4, TERRAIN.rightHeightAt(33250), 1e-9);
        assertEquals(0, TERRAIN.rightHeightAt(34850));
        assertEquals(
                List.of("fork mesa", "rock pillar"),
                TERRAIN.islandList().stream().map(TerrainBlock.Island::name).toList());
        assertEquals(
                List.of("mesa", "pillar"),
                TERRAIN.islandList().stream().map(TerrainBlock.Island::look).toList());
        TerrainBlock.Contact contact = TERRAIN.contact();
        assertEquals(new PerDifficulty<>(6.0, 10.0, 10.0), contact.damage());
        assertEquals(List.of(28.0, 0.12, 0.5), List.of(contact.push(), contact.pushSeconds(), contact.cooldown()));
    }

    @Test
    void theGatesAreLinkedNestsOfNinePairsAtMedium() {
        List<LevelData.GroundTarget> gates = LEVEL.groundTargets().stream()
                .filter(target -> target.link().isPresent())
                .toList();
        assertEquals(6, gates.size());
        gates.forEach(gate -> assertEquals(Optional.of("tendril-anchor"), gate.enemy()));
        assertEquals(
                List.of(7, 9, 10),
                List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD).stream()
                        .map(difficulty -> gates.stream()
                                        .mapToInt(gate -> TerrainChecks.on(gate, difficulty)
                                                .size())
                                        .sum()
                                / 2)
                        .toList());
        LevelData.Secondary secondary = LEVEL.objectives().secondary().orElseThrow();
        assertTrue(secondary.breaksLinks());
        assertEquals(Optional.of("BARRIERS"), secondary.label());
        assertEquals(80, secondary.credits());
        assertTrue(LEVEL.radio().stream()
                .anyMatch(cue -> cue.event().orElse(null) == LevelData.CueEvent.FIRST_WALL_CONTACT));
        assertEquals("mars-canyon", LEVEL.music().ambience());
    }

    @Test
    void theCrawlersCrawlTheirGroundPaths() {
        List<LevelData.Wave> crawlers =
                LEVEL.waves().stream().filter(wave -> wave.crawl().isPresent()).toList();
        assertEquals(
                List.of(84.0, 100.0, 160.0),
                crawlers.stream().map(LevelData.Wave::t).toList());
        assertEquals(
                List.of(true, false, true),
                crawlers.stream().map(wave -> wave.fliesOn(Difficulty.MEDIUM)).toList());
        assertEquals(
                new TerrainBlock.GroundPoint(16530, 78),
                crawlers.getFirst().crawl().orElseThrow().getFirst());
    }

    @Test
    void theTendrilAnchorLinksAPairAndFiresOnlyOnHard() {
        EnemyData anchor = CONTENT.enemy("tendril-anchor");
        EnemyData.Attack tendril = anchor.attack("tendril").orElseThrow();
        assertEquals(new EnemyData.Link(40, 1.0, 6, 8, 0.5, 8), tendril.link().orElseThrow());
        assertEquals(Optional.of("medium"), tendril.bullet());
        EnemyData.Attack orb = anchor.attack("orb").orElseThrow();
        assertFalse(orb.firesOn(Difficulty.MEDIUM));
        assertTrue(orb.firesOn(Difficulty.HARD));
        assertTrue(tendril.firesOn(Difficulty.EASY));
        assertEquals(12, anchor.hp());
        assertEquals(12, anchor.bounty());
    }

    @Test
    void theThreadcrawlerCrawlsCutsAndFiresItsPoreWave() {
        EnemyData crawler = CONTENT.enemy("threadcrawler");
        assertEquals(new EnemyData.Crawl(70), crawler.movement().crawl().orElseThrow());
        EnemyData.SegmentChain chain = crawler.segmentChain().orElseThrow();
        assertEquals(12, chain.segments());
        assertEquals(Optional.of(48.0), chain.width());
        assertEquals(Optional.of(new EnemyData.Cut(1.0)), chain.cut());
        assertTrue(chain.regrow().isEmpty());
        EnemyData.Attack pores = crawler.attack("pores").orElseThrow();
        assertEquals(
                new EnemyData.Pores(2.0, 0.3, Optional.empty()), pores.pores().orElseThrow());
        assertEquals(Optional.of(0.25), pores.interval());
        EnemyData.Hook easy =
                crawler.difficulty().flatMap(EnemyData.Hooks::easy).orElseThrow();
        EnemyData.Hook hard =
                crawler.difficulty().flatMap(EnemyData.Hooks::hard).orElseThrow();
        assertEquals(Optional.of(10), easy.segments());
        assertEquals(Optional.of(new EnemyData.PoresChange(Optional.empty(), Optional.of(true))), easy.pores());
        assertEquals(Optional.of(16), hard.segments());
        assertEquals(Optional.of(new EnemyData.PoresChange(Optional.of(1.5), Optional.empty())), hard.pores());
        assertEquals(196, crawler.hp());
        assertEquals(
                List.of(Optional.of(18), Optional.of(4)),
                crawler.partList().orElseThrow().stream()
                        .map(EnemyData.PartData::firstBonus)
                        .toList());
    }

    @Test
    void theIntelMarksTheSixGatesAndTheTwoCrawlers() {
        Intel intel = Intel.of(CONTENT, KEY, 3);
        assertEquals(
                List.of(34.0, 46.0, 57.0, 132.0, 144.0, 152.0),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.GATE)
                        .map(Intel.Mark::from)
                        .toList());
        assertEquals(
                List.of(84.0, 160.0),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.CRAWLER)
                        .map(Intel.Mark::from)
                        .toList());
        assertTrue(intel.enemies().contains("Tendril Anchor"), intel.enemies().toString());
        assertTrue(
                intel.enemies().indexOf("Needler") < intel.enemies().indexOf("Tendril Anchor"),
                "in order of appearance: " + intel.enemies());
    }

    @Test
    void thePlateauLayerIsACanyonLevels() {
        assertTrue(LEVEL.backdrop().hasPlateau(), "step ART-C's backdrop draws the plateau beyond the walls");
        assertProblem(
                LEVEL_15,
                "scroll_factors: {deep: 0.15,",
                "scroll_factors: {plateau: 1.25, deep: 0.15,",
                "a plateau layer in a level without terrain");
        assertProblem(PATH, "plateau: 1.25, low-air", "plateau: 0.8, low-air", "plateau");
    }

    @Test
    void theValidatorKeepsTheCanyonFlyable() {
        assertProblem(PATH, "- [27650, 150]", "- [27650, 160]", "the flyable width at row 27650 is 170 px");
        assertProblem(PATH, "[200, 19452], [200, 19224]", "[190, 19452], [190, 19224]", "a channel at row");
        assertProblem(PATH, "- [4180, 40]", "- [2900, 40]", "sideways per px of scroll");
    }

    @Test
    void theValidatorKeepsEverythingOutOfTheRock() {
        assertProblem(PATH, "at: [[43, 101],", "at: [[43, 60],", "is in rock");
        assertProblem(PATH, "at: [[34, 98], [34, 382]]", "at: [[34, 140], [34, 382]]", "foot band");
        assertProblem(PATH, "crawl: [[16530, 78],", "crawl: [[16530, 40],", "crawl[0]: [16530, 40] is in rock");
        assertProblem(PATH, "x: 240, size: [360, 220]", "x: 240, size: [440, 220]", "the burrowing field");
    }

    @Test
    void theValidatorChecksTheGatesAndTheNewKeys() {
        assertProblem(PATH, "at: [[34, 98], [34, 382]]", "at: [[34, 98], [34.5, 382]]", "the same t");
        assertProblem(PATH, "[47.5, 102], [47.5, 362]", "[47, 102], [47, 362]", "two tendril pairs 1 s apart");
        assertProblem(PATH, "    crawl: [[19630, 380], [20300, 356]]", "", "a crawling unit's wave has its crawl path");
        assertProblem(
                LEVEL_15,
                "event: first-surfacing",
                "event: first-wall-contact",
                "first-wall-contact in a level without");
        assertProblem(
                "enemies/ground/threadcrawler/data.yaml",
                "  cut: {delay: 1.0}",
                "",
                "a chain's rear part regrows a head (regrow) or dies (cut)");
        assertProblem(
                PATH,
                "target: gate 1\n    section: 2\n    enemy: tendril-anchor",
                "target: gate 1\n    section: 2\n    enemy: spine-turret",
                "'spine-turret' has no link attack");
    }
}
