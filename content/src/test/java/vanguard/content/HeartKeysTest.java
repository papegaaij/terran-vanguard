package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.VrellLevel;

/**
 * M11 part H, step H2 (m11h §1 #1–#13, content side): the Choir Heart's and Level 50's keys loaded and mapped: the
 * boss's {@code walls}, {@code drift}, {@code part_windows}, {@code dark}, {@code heartbeat}, {@code checkpoints}, a
 * move's {@code spores}/{@code wall-spit} with their {@code clear}, a playbook's {@code cover_every} and a move's {@code
 * last}, a brood's {@code toward_ship}/{@code wall_pits}, Rook's {@code pick}, the last words' {@code flash_at}, a boss
 * attack's {@code mine}, the difficulties' {@code heart} changes; the level's transit from K ({@code to}, {@code ease},
 * {@code reverse}, {@code after_kill}, {@code beats}), {@code radio_noise_changes} and the backdrop's {@code pulse}; and
 * {@link HeartChecks}' rows with one thing moved.
 */
class HeartKeysTest {
    private static final String HEART = "enemies/bosses/choir-heart/data.yaml";
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

    private static BossSpec boss(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, Level50Test.LEVEL, difficulty).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow()
                .boss()
                .orElseThrow();
    }

    @Test
    void theKeysLoad() {
        EnemyData.BossData script = CONTENT.enemy("choir-heart").boss().orElseThrow();
        assertEquals(320, script.walls().orElseThrow().corridor());
        assertEquals(4, script.walls().orElseThrow().nodes().size());
        assertEquals(60, script.drift().orElseThrow().speed());
        assertEquals(6, script.partWindows().orElseThrow().parts().size());
        assertEquals(0.08, script.dark().orElseThrow().ambient(), 1e-9);
        assertEquals(List.of("heartbeat", "heartbeat-storm"), script.heartbeat().orElseThrow());
        assertEquals("phases", script.checkpoints().orElseThrow());
        assertEquals(3.5, script.lastWords().orElseThrow().flashAt().orElseThrow(), 1e-9);
        LevelData level = CONTENT.level(Level50Test.LEVEL);
        assertEquals(List.of(new LevelData.NoiseChange("near-side", 0)), level.noiseChanges());
        assertEquals(6, level.backdrop().pulse().orElseThrow().size(), "the pulse light per section to the Heart");
    }

    @Test
    void theHeartMapsToTheSimulationOnEveryDifficulty() {
        BossSpec medium = boss(Difficulty.MEDIUM);
        assertEquals(1, medium.walls().orElseThrow().phase(), "the walls stand in the arena phase");
        assertEquals(10, medium.walls().orElseThrow().damage(), 1e-9);
        assertEquals(1, medium.drift().orElseThrow().phase());
        assertTrue(medium.drift().orElseThrow().reversal().isPresent());
        assertTrue(medium.lastWords().orElseThrow().flashes());
        assertEquals(0, medium.lastWords().orElseThrow().seconds(), 1e-9, "the fade without a hang");
        PhasePlaybook membranes = medium.playbooks().get(0);
        assertEquals(8, membranes.ensemble().coverEvery(), "each membrane in any 8 draws");
        assertTrue(membranes.ensemble().parallel().orElseThrow().fromHalf(), "two at once from the half");
        assertEquals(List.of(0), membranes.ensemble().lastMoves(), "the last membrane sings the Herald's song too");
        assertTrue(membranes.moves().stream().allMatch(move -> move.source() >= 0), "every song from its membrane");
        assertTrue(membranes.moves().stream()
                .anyMatch(move -> move.kind() == PhasePlaybook.Kind.SPORES && move.clearPx() == 80));
        PhasePlaybook arena = medium.playbooks().get(1);
        assertTrue(arena.moves().stream()
                .anyMatch(move -> move.kind() == PhasePlaybook.Kind.WALL_SPIT && move.clearPx() == 120));
        assertTrue(arena.moves().stream()
                .anyMatch(
                        move -> move.bays().filter(PhasePlaybook.Bays::onWalls).isPresent()));
        assertTrue(medium.playbooks().get(2).ensemble().parallel().isPresent(), "the storm: two at once");
        assertEquals(
                BossSpec.RookOrder.Pick.OPEN_WINDOW, medium.rookOrders().get(0).pick());
        assertEquals(
                BossSpec.RookOrder.Pick.OWN_SIDE, medium.rookOrders().get(1).pick());
        BossSpec.Attack spore = medium.attacks().stream()
                .filter(attack -> attack.name().equals("spore-drop"))
                .findFirst()
                .orElseThrow();
        assertTrue(spore.gun().mine().isPresent(), "the spore drop throws the Spore Bomber's spores");

        BossSpec easy = boss(Difficulty.EASY);
        assertEquals(360, easy.walls().orElseThrow().corridor(), 1e-9);
        assertEquals(6, easy.walls().orElseThrow().damage(), 1e-9);
        assertFalse(easy.drift().orElseThrow().reversal().isPresent(), "no reversal on easy");
        assertTrue(
                easy.playbooks().stream()
                        .allMatch(book -> book.ensemble().parallel().isEmpty()),
                "one at a time");

        BossSpec hard = boss(Difficulty.HARD);
        assertEquals(300, hard.walls().orElseThrow().corridor(), 1e-9);
        assertEquals(16, hard.attacks().get(hard.heartbeat()).count(), "the heartbeat ring of 16");
        assertEquals(16, hard.attacks().get(hard.heartbeatFast()).count());
    }

    @Test
    void theClosingGateCountsFromK() {
        LevelScript level = SimSpecs.level(CONTENT, Level50Test.LEVEL, Difficulty.MEDIUM);
        VrellLevel.Transit gate = level.vrell().transit().orElseThrow();
        assertTrue(gate.fromKill());
        assertTrue(gate.reverse());
        assertEquals(240, gate.toX(), 1e-9);
        assertEquals(1.5, gate.easeSeconds(), 1e-9);
        assertEquals(11, gate.beats().size(), "the gate, the near side, the plane, eight glyphs");
        assertEquals(
                VrellLevel.Transit.Beat.Kind.GATE_CLOSING,
                gate.beats().getFirst().kind());
    }

    @Test
    void aCorridorOutsideTheSpecsRange() {
        assertProblem(problems(HEART, "heart: {corridor: 300}", "heart: {corridor: 260}"), "a corridor of");
    }

    @Test
    void aWindowPartOutsideTheFirstPhase() {
        assertProblem(
                problems(HEART, "part_windows: {parts: [m1,", "part_windows: {parts: [n-lu, m1,"),
                "a window part is the first phase's");
    }

    @Test
    void aSteepDrift() {
        assertProblem(problems(HEART, "[6, 12], [14, -10]", "[6, 22], [14, -10]"), "|θ| at most");
    }

    @Test
    void aNodeOutsideTheWallsPhase() {
        assertProblem(problems(HEART, "{part: n-lu, side: left, y: 220}", "{part: m1, side: left, y: 220}"), "a node");
    }
}
