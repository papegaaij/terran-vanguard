package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.SetPieceSpec;

/**
 * M11 part F, step F2 (m11f §1 #1–#11, content side): Level 48's and Vorne's Chimera's keys loaded and checked: the
 * boss's {@code rear_sweep}, {@code last_words}, {@code rise}, {@code part_shields}, {@code rook} and {@code bar}, a move's
 * {@code rear-sweep}, {@code not_first} and {@code only}, a wave's {@code lane: away}, the {@code leaving} band, the
 * {@code rear-sweep} radio event; and {@link CitadelChecks}' and {@link BowChecks}' rows on Level 48's data with one
 * thing moved: two rear waves under 6 s apart, a rear wave just before a nest, a core under a nest, a front wave with
 * {@code lane: away}, a rear-sweep move without the boss's sweep, a rear-sweep cue without its turn.
 */
class CitadelKeysTest {
    private static final String LEVEL_48 = "campaign/act-7-beyond-the-gate/level-48-vornes-end/data.yaml";
    private static final String CHIMERA = "enemies/bosses/vornes-chimera/data.yaml";
    private static final Content CONTENT = LevelMetrics.CONTENT;

    /** The design tree with {@code edits} ([path, from, to]...) applied. */
    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (int i = 0; i < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        return files;
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    private static void assertProblem(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    @Test
    void theKeysLoad() {
        EnemyData.BossData script = CONTENT.enemy("vornes-chimera").boss().orElseThrow();
        assertEquals(new EnemyData.LastWordsData(4.5, 0.5), script.lastWords().orElseThrow());
        assertEquals(200, script.rearSweep().orElseThrow().speed());
        assertEquals("phases", script.bar().orElseThrow());
        SetPieceSpec spec = SimSpecs.level(CONTENT, Level48Test.LEVEL, Difficulty.EASY).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
        BossSpec easy = spec.boss().orElseThrow();
        // easy: no rear sweep in phase 3 (its move's only), phase 2's never drawn first
        assertTrue(easy.playbooks().get(2).moves().stream()
                .noneMatch(move -> move.kind() == PhasePlaybook.Kind.REAR_SWEEP));
        assertTrue(easy.playbooks().get(1).moves().stream()
                .filter(move -> move.kind() == PhasePlaybook.Kind.REAR_SWEEP)
                .allMatch(PhasePlaybook.Move::notFirst));
        BossSpec medium = SimSpecs.level(CONTENT, Level48Test.LEVEL, Difficulty.MEDIUM).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow()
                .boss()
                .orElseThrow();
        assertTrue(medium.playbooks().get(2).moves().stream()
                .anyMatch(move -> move.kind() == PhasePlaybook.Kind.REAR_SWEEP));
        assertEquals(1, medium.rookOrders().size());
        assertEquals(120, medium.partShields().getFirst().shield().hp(), 1e-9);
    }

    @Test
    void twoRearWavesUnderSixSecondsApart() {
        assertProblem(
                problems(
                        LEVEL_48,
                        "{t: 29, formation: stream, enemy: harrow",
                        "{t: 22, formation: stream, enemy: harrow"),
                "s after the one before");
    }

    @Test
    void aRearWaveJustBeforeANest() {
        assertProblem(
                problems(LEVEL_48, "{t: 43, formation: rear ambush", "{t: 54, formation: rear ambush"),
                "before a turret nest scrolls in");
    }

    @Test
    void aCoreUnderANest() {
        assertProblem(problems(LEVEL_48, "at: [[50, 118]]", "at: [[55, 200]]"), "lies under a nest unit");
    }

    @Test
    void aFrontWaveAwayFromTheShip() {
        assertProblem(
                problems(
                        LEVEL_48,
                        "{t: 9, formation: V-wing, enemy: needler, count: 5, from: front}",
                        "{t: 9, formation: V-wing, enemy: needler, count: 5, from: front, lane: away}"),
                "lane: away, only on a wave from: rear");
    }

    @Test
    void aRearSweepMoveWithoutTheBossSweep() {
        assertProblem(
                problems(
                        CHIMERA,
                        "  rear_sweep: {out: 1.0, speed: 200, off_ship: 160, clear: 100, fire: {attack: spore-pair,"
                                + " every: 0.4}}\n",
                        ""),
                "has its rear_sweep, and only it");
    }

    @Test
    void aRearSweepCueWithoutItsTurn() {
        assertProblem(
                problems(LEVEL_48, "{event: rear-sweep, subject: '1', ", "{event: rear-sweep, subject: '3', "),
                "its bark's turn, 1 or 2");
    }
}
