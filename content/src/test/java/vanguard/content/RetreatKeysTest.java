package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Section;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part G (step G2): the loader's records and {@link RetreatChecks} for Level 20 Retreat from Hellas's keys
 * (design/campaign Level 20: a section's negative {@code speed}, entries by travel, the BOTTOM lessons' {@code
 * warning}, the {@code pursuit} formation and its wave block, the strip backdrop with pieces by {@code row}, the
 * music's {@code ambience_layers}, the flak's {@code band: top}), the Mote Swarm's {@code pursuit} block and hooks and
 * Rook's "Up top!" barks ({@code top_lines}). Step G4 mapped the level for the simulation ({@link RetreatRules}).
 */
class RetreatKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String LEVEL_KEY = "act-3-red-dust/level-20-retreat-from-hellas";
    private static final String LEVEL = "campaign/act-3-red-dust/level-20-retreat-from-hellas/data.yaml";
    private static final String MOTES = "enemies/air/mote-swarm/data.yaml";

    private static LevelData level() {
        return CONTENT.level(LEVEL_KEY);
    }

    @Test
    void theScrollRunsForwardThenBackThroughTheTurnabout() {
        LevelData level = level();
        assertTrue(level.hasReversal());
        assertFalse(level.sections().getFirst().reversed());
        assertEquals(
                6, level.sections().stream().filter(LevelData.Section::reversed).count());
        assertEquals(4.0, level.sections().get(1).easeSeconds());
        assertEquals(1, level.travelAt(10));
        assertEquals(1, level.travelAt(21.5), "still forward before the turnabout's zero");
        assertEquals(-1, level.travelAt(23), "reversed after it");
        assertEquals(-1, level.travelAt(170));
        // The README's scroll column: 2,800 at the turn, 2,840 at t=24; its top ≈ 2,983 at the zero (t ≈ 22.1).
        assertEquals(2800, level.scrollAt(20), 1e-6);
        assertEquals(2840, level.scrollAt(24), 1e-6);
        double top = 0;
        for (double t = 20; t < 26; t += 0.01) {
            top = Math.max(top, level.scrollAt(t));
        }
        assertEquals(2983, top, 1);
        assertTrue(level.scrollAt(level.seconds()) < -15_000, "past its start: " + level.scrollAt(level.seconds()));
    }

    @Test
    void thePursuitsComeFromBehindWithTheirRefillsAndBreakOffs() {
        List<LevelData.Wave> pursuits =
                level().waves().stream().filter(LevelData.Wave::pursues).toList();
        assertEquals(2, pursuits.size());
        assertEquals(
                List.of(24.0, 124.0), pursuits.stream().map(LevelData.Wave::t).toList());
        assertEquals(
                List.of(108.0, 163.0),
                pursuits.stream().map(w -> w.pursuit().orElseThrow().breakAt()).toList());
        for (LevelData.Wave wave : pursuits) {
            assertEquals(LevelData.Entry.REAR, wave.from());
            assertEquals(10, wave.groupList().getFirst().count());
            assertEquals(Optional.of(3), wave.pursuit().orElseThrow().refill());
            assertEquals(Optional.of(4.0), wave.easy().orElseThrow().warning(), "the TOP warning 4 s ahead on easy");
        }
        // 10 + 8 × 3 and 10 + 3 × 3: the README's 53 motes.
        assertEquals(
                34 + 19, pursuits.stream().mapToInt(RetreatKeysTest::medium).sum());

        EnemyData motes = CONTENT.enemy("mote-swarm");
        EnemyData.Pursuit defaults = motes.pursuit().orElseThrow();
        assertEquals(new Span(50, 150), defaults.band());
        assertEquals(30, defaults.lag(), "round 43 Q.1: 90 to 30");
        assertEquals(4, defaults.lungeEvery(), "round 43 Q.1: 6 to 4");
        assertEquals(0.8, defaults.tell());
        assertEquals(60, defaults.ball());
        assertEquals(240, defaults.dive());
        assertEquals(140, defaults.overshoot());
        assertEquals(200, defaults.climb());
        assertEquals(3, defaults.refill());
        assertEquals(10, defaults.refillEvery());
        assertEquals(200, defaults.breakSpeed());
        EnemyData.PursuitChange easy = motes.difficulty()
                .flatMap(EnemyData.Hooks::easy)
                .flatMap(EnemyData.Hook::pursuit)
                .orElseThrow();
        assertEquals(Optional.of(5.5), easy.lungeEvery());
        assertEquals(Optional.of(1.2), easy.tell());
        EnemyData.PursuitChange hard = motes.difficulty()
                .flatMap(EnemyData.Hooks::hard)
                .flatMap(EnemyData.Hook::pursuit)
                .orElseThrow();
        assertEquals(Optional.of(3.0), hard.lungeEvery());
        // The entry and the refills by the formation lever: 8 + 2 on easy, 12 + 4 on hard.
        assertEquals(8, CONTENT.difficulty().formationSize(10, Difficulty.EASY));
        assertEquals(2, CONTENT.difficulty().formationSize(3, Difficulty.EASY));
        assertEquals(12, CONTENT.difficulty().formationSize(10, Difficulty.HARD));
        assertEquals(4, CONTENT.difficulty().formationSize(3, Difficulty.HARD));
    }

    @Test
    void theFirstThreeBottomEntriesAfterTheTurnAreTheLessons() {
        LevelData level = level();
        List<Double> warned = level.waves().stream()
                .filter(w -> w.from() == LevelData.Entry.FRONT && w.warning().isPresent())
                .map(LevelData.Wave::t)
                .toList();
        assertEquals(List.of(27.5, 31.5, 35.5), warned);
        warned.forEach(t -> assertEquals(-1, level.travelAt(t)));
        // Every front wave after the turn enters from the bottom (by travel); the vanguard is all in before it.
        LevelData.Wave vanguard =
                level.waves().stream().filter(w -> w.t() == 20.5).findFirst().orElseThrow();
        double last = vanguard.t()
                + (vanguard.groupList().getFirst().count() - 1)
                        * vanguard.interval().orElseThrow();
        assertEquals(1, level.travelAt(last), "the last vanguard Skitter enters before the zero");
    }

    @Test
    void mostWavesComeFromAheadByTravel() {
        List<LevelData.Wave> medium = level().waves().stream()
                .filter(w -> w.fliesOn(Difficulty.MEDIUM))
                .toList();
        long ahead =
                medium.stream().filter(w -> w.from() == LevelData.Entry.FRONT).count();
        assertTrue(ahead >= 0.6 * medium.size(), ahead + " of " + medium.size());
        int enemies = medium.stream().mapToInt(RetreatKeysTest::medium).sum();
        int ground = level().groundTargets().stream()
                .filter(g -> g.enemy().isPresent())
                .mapToInt(g -> g.at().size())
                .sum();
        assertEquals(175, enemies, "step G4's tuned waves");
        assertEquals(22, ground);
    }

    @Test
    void theStripKeysTheGroundByRowAndItsPiecesLieByRow() {
        LevelData level = level();
        BackdropData backdrop = level.backdrop();
        BackdropData.Strip strip = backdrop.strip().orElseThrow();
        assertEquals(15_360, strip.origin());
        level.sections().forEach(section -> assertTrue(section.tiles().isEmpty(), section.name()));
        assertEquals("basin-dunes", strip.zoneAt(strip.row(0)).tiles().getFirst());
        assertEquals(
                "rim-plain",
                strip.zoneAt(strip.row(level.scrollAt(level.seconds()))).tiles().getFirst());
        BackdropData.PlacedPiece lip = backdrop.placed().stream()
                .filter(p -> p.piece().equals("rim-lip"))
                .findFirst()
                .orElseThrow();
        assertTrue(lip.byRow());
        assertTrue(Double.isNaN(lip.t()));
        assertEquals(400 - 15_360, level.pieceCentre(lip), 1e-9);
    }

    @Test
    void theRumbleTheRimFlakAndRooksTopBarks() {
        LevelData level = level();
        LevelData.Music.AmbienceLayer rumble =
                level.music().ambienceLayers().orElseThrow().getFirst();
        assertEquals("basin-rumble", rumble.ambience());
        assertEquals(0, rumble.level(11));
        assertEquals(0.5, rumble.level(13), 1e-12);
        assertEquals(1, rumble.level(20));
        assertEquals(0.5, rumble.level(34), 1e-12);
        assertEquals(0, rumble.level(40));
        assertEquals(WeatherBlock.FlakBand.TOP, level.flak().orElseThrow().bandOrDefault());
        assertEquals(160, level.flak().orElseThrow().from());
        assertEquals(
                WeatherBlock.FlakBand.LOWER,
                CONTENT.level("act-3-red-dust/level-17-dome-siege")
                        .flak()
                        .orElseThrow()
                        .bandOrDefault());
        WingmenData.Bark rear = CONTENT.wingmen().barks().bark("rear-wave").orElseThrow();
        assertEquals(3, rear.linesReversed().size());
        assertTrue(rear.linesReversed().stream().allMatch(line -> line.contains("up top") || line.contains("Up top")));
        // The quiet beat plays without requires: escort (round 36, Q.20d).
        LevelData.RadioCue beat = level.radio().stream()
                .filter(cue -> cue.t().filter(t -> t == 113).isPresent())
                .findFirst()
                .orElseThrow();
        assertEquals("Rook", beat.speaker());
        assertTrue(beat.requires().isEmpty());
    }

    @Test
    void theChecksRejectKeysOutsideTheRetreat() {
        assertProblem(
                replace(
                        LEVEL,
                        "  - {t: 31.5, formation: V-wing, enemy: dust-kite, count: 3, from: front, warning: 3,",
                        "  - {t: 9.5, formation: V-wing, enemy: dust-kite, count: 3, from: front, warning: 3,"),
                "a front wave is warned only in a reversed section");
        assertProblem(
                replace(
                        LEVEL,
                        "    pursuit: {break_at: 108, refill: 3, refill_every: 10}",
                        "    pursuit: {break_at: 130, refill: 3, refill_every: 10}"),
                "one pursuit at a time");
        assertProblem(
                replace(
                        LEVEL,
                        "    pursuit: {break_at: 163, refill: 3, refill_every: 10}",
                        "    pursuit: {break_at: 180, refill: 3, refill_every: 10}"),
                "before the level's end");
        assertProblem(
                replace(
                        LEVEL,
                        "  - t: 24\n    formation: pursuit\n    enemy: mote-swarm\n    count: 10\n    from: rear",
                        "  - t: 24\n    formation: pursuit\n    enemy: mote-swarm\n    count: 10\n    from: front"),
                "from: rear");
        assertProblem(
                replace(
                        LEVEL,
                        "    at: [[42, 200], [42.4, 240], [42.8, 280]]",
                        "    arc: up\n    at: [[42, 200], [42.4, 240], [42.8, 280]]"),
                "the travel frame turns a nest's arc");
        assertProblem(
                replace(
                        LEVEL,
                        "  - name: The Strike\n    end: 20\n",
                        "  - name: The Strike\n    end: 20\n    speed: -140\n"),
                "launches forward");
        assertProblem(
                replace(
                        LEVEL,
                        "    tiles: []\n    notes:\n      layers:\n        ground: as section 1",
                        "    tiles: [basin-flats]\n    notes:\n      layers:\n        ground: as section 1"),
                "strip keys the tiles");
        assertProblem(
                replace(
                        LEVEL,
                        "      - {from: -4800, to: 400, tiles: [rim-plain]}",
                        "      - {from: 0, to: 400, tiles: [rim-plain]}"),
                "the zones cover rows");
        assertProblem(replace(MOTES, "  - {name: pursuit, size: [8, 12]}", ""), "its formations do not list");
        assertProblem(
                replace(MOTES, "  hard: {pursuit: {lunge_every: 3}}", "  hard: {pursuit: {lunge_every: 3, dive: 3}}"),
                "dive");
        assertProblem(
                replace("player/weapons/rear-guard-drone-bay/data.yaml", "traits: [rear]", "traits: [forward]"),
                "rear trait");
        assertProblem(
                replace("player/weapons/rear-guard-drone-bay/data.yaml", "  behind: 40 ", "  orbit: 40 "),
                "trailing drone");
    }

    @Test
    void withoutItsPursuitsTheLevelMapsToTheReverseScroll() {
        // The level's reverse scroll without its pursuits (step G1's API); the pursuits themselves in Level20Test.
        List<DataFile> files = replace(
                LEVEL,
                "    formation: pursuit\n    enemy: mote-swarm\n    count: 10\n    from: rear\n"
                        + "    pursuit: {break_at: 108, refill: 3, refill_every: 10}",
                "    formation: rear ambush\n    enemy: wraith\n    count: 2\n    from: rear");
        files = files.stream()
                .map(f -> f.path().equals(LEVEL)
                        ? new DataFile(
                                LEVEL,
                                f.text()
                                        .replace(
                                                "    formation: pursuit\n    enemy: mote-swarm\n    count: 10\n"
                                                        + "    from: rear\n"
                                                        + "    pursuit: {break_at: 163, refill: 3, refill_every: 10}",
                                                "    formation: rear ambush\n    enemy: wraith\n    count: 2\n"
                                                        + "    from: rear"))
                        : f)
                .toList();
        Content content = ContentLoader.load(files);
        LevelScript script = SimSpecs.level(content, LEVEL_KEY, Difficulty.MEDIUM);
        assertTrue(script.reverses());
        List<Section> sections = script.sections();
        assertEquals(140, sections.getFirst().speed());
        assertEquals(-120, sections.get(1).speed());
        assertEquals(4, sections.get(1).easeSeconds(), "the turnabout eases through zero");
        assertEquals(2, sections.get(3).easeSeconds(), "a retreat speed eases from the one before it");
        assertEquals(8, sections.get(6).easeSeconds());
        assertEquals(1, Sortie.travelAt(script, 21.5));
        assertEquals(-1, Sortie.travelAt(script, 23));
        List<WaveSpec> lessons = script.waves().stream()
                .filter(w ->
                        w.entry() == WaveSpec.Entry.FRONT && w.warningSeconds().isPresent())
                .toList();
        assertEquals(
                List.of(27.5, 31.5, 35.5), lessons.stream().map(WaveSpec::t).toList());
    }

    /** A wave's units at medium: its count, a pursuit's with its refills (one every refill_every s before break_at). */
    static int medium(LevelData.Wave wave) {
        int count = wave.groupList().stream().mapToInt(LevelData.Group::count).sum();
        if (wave.pursuit().isEmpty()) {
            return count;
        }
        LevelData.Pursuit pursuit = wave.pursuit().get();
        EnemyData.Pursuit defaults =
                CONTENT.enemy(wave.groupList().getFirst().enemy()).pursuit().orElseThrow();
        double every = pursuit.refillEvery().orElse(defaults.refillEvery());
        int each = pursuit.refill().orElse(defaults.refill());
        int refills = 0;
        while (wave.t() + (refills + 1) * every < pursuit.breakAt()) {
            refills++;
        }
        return count + refills * each;
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

    @Test
    void theRidgePassIsALevelKey() {
        // Round 43 Q.4: the travelling ridge's pass is its own key, no longer tied to the basin rumble's layer start.
        WeatherBlock.Ridge ridge = level().ridge().orElseThrow();
        assertEquals(12, ridge.t());
        assertEquals(8, ridge.seconds());
        assertEquals(WeatherBlock.RidgeEdge.LEFT, ridge.from());
        assertEquals(WeatherBlock.RidgeEdge.RIGHT, ridge.to());
        assertFalse(ridge.leftward());
        assertEquals(70, ridge.depth());
        assertTrue(CONTENT.level("act-3-red-dust/level-19-foundry-of-tharsis")
                .ridge()
                .isEmpty());
        String key = "ridge: {t: 12, seconds: 8, from: left, to: right, depth: 70}";
        assertProblem(replace(LEVEL, key, "ridge: {t: 12, seconds: 8, from: left, to: left, depth: 70}"), "opposite");
        assertProblem(replace(LEVEL, key, "ridge: {t: 12, seconds: 0, from: left, to: right, depth: 70}"), "seconds");
        assertProblem(replace(LEVEL, key, "ridge: {t: 12, seconds: 8, from: left, to: right, depth: 540}"), "depth");
        assertProblem(replace(LEVEL, key, "ridge: {t: 12, seconds: 8, from: top, to: right, depth: 70}"), "ridge");
    }

    @Test
    void theBandLiesInsideThePlayField() {
        assertTrue(CONTENT.enemy("mote-swarm").pursuit().orElseThrow().band().max() < PlayField.HEIGHT);
    }
}
