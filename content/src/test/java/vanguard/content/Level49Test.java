package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.BossSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * M11 part G, step G2: Level 49 Echoes from its data (design/campaign/act-7-beyond-the-gate/level-49-echoes: the Choir's
 * memory of Luna, Mars and Ceres, then the boss rush of the three echoes, each in its own gate with the refill and a
 * checkpoint between them, then the unmaking), flown by the autopilot (the echoes are the built plays': the Carrier's
 * open pair and core, {@link ColossusPlay}, {@link RingPlay}) with the balance plan's fit ({@link #planLoadout}), with
 * Rook and without him ({@code --escort none}): every run won on every difficulty, retries at the echoes' checkpoints
 * allowed and counted. The fights' measures are {@link EchoFightTest}'s; the pacing, the floor, the density and the haul
 * the campaign-wide tests'.
 */
class Level49Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-49-echoes";

    static final int MAX_STEPS = 60 * 60 * 30;

    /** The rush's par, s: the autopilot's measured mean at medium (EchoFightTest, five seeds). */
    static final double PAR = 162;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 49, MW (Level 48's with the Bastion Shield for the Mk III). */
    static final double LOAD = 26;

    /** The Mk VI "Choir Core"'s output, MW. */
    static final double OUTPUT = 30;

    /** The Bastion Shield among the shields (design/player/shields: index 6). */
    static final int BASTION = 6;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 49 ({@code "49"} in design/player/balance-plan.yaml: the visit fits the Bastion
     * Shield in place of the Mk III and Rook's Autocannon): Level 48's fit with the Bastion, 26 of 30 MW, Rook flying the
     * Autocannon (L1).
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "autocannon",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        String shield = content.shields().models().get(BASTION).name();
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 5)),
                        shield,
                        content.armour().plating().get(5).name(),
                        OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, SimSpecs.SWARM_CALL, 0))
                .withPower(SimSpecs.power(content, OUTPUT, LOAD, shield, difficulty));
    }

    /**
     * The data: the rush of the three echoes (each its original's echo stat block under the original's slug, in its own
     * gate, warned 5 s ahead), the refill and the checkpoint on every difficulty, the kill ratio judged by the first
     * warning.
     */
    @Test
    void theLevelSendsTheThreeEchoesInTheirGates() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<SetPieceSpec> bosses =
                    level.setPieces().stream().filter(SetPieceSpec::isBoss).toList();
            assertEquals(
                    List.of("brood-carrier", "dust-colossus", "iron-sovereign"),
                    bosses.stream().map(SetPieceSpec::slug).toList());
            for (SetPieceSpec piece : bosses) {
                BossSpec boss = piece.boss().orElseThrow();
                assertTrue(boss.echo().isPresent(), piece.slug() + " flies its echo");
                assertTrue(boss.incomingSeconds() > 4, piece.slug() + ": warned before it shows");
            }
            assertTrue(level.vrell().rush().orElseThrow().everyDifficulty());
            assertEquals(PAR, level.vrell().rush().orElseThrow().parSeconds(), 1e-9, "the measured par");
            assertTrue(level.objectives().secondary().untilWarning(), "KILLS n / 104 by the first warning");
            assertEquals(0, level.waves().stream().filter(wave -> wave.t() > 89).count(), "no wave in the rush");
        }
    }

    /** The autopilot wins every run on {@code difficulty} with the loadout {@code fit} gives; the runs' retries. */
    private List<LevelMetrics.Run> runs(Difficulty difficulty, BiFunction<Content, Difficulty, Loadout> fit) {
        return IntStream.range(1, SEEDS + 1)
                .parallel()
                .mapToObj(seed -> LevelMetrics.autopilot(
                        content, LEVEL, difficulty, fit.apply(content, difficulty), seed, MAX_STEPS))
                .toList();
    }

    @Test
    void theAutopilotWinsEveryDifficultyWithRookAndWithout() {
        List<String> out = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            for (boolean rook : new boolean[] {true, false}) {
                BiFunction<Content, Difficulty, Loadout> fit =
                        rook ? Level49Test::planLoadout : Level49Test::withoutRook;
                for (LevelMetrics.Run run : runs(difficulty, fit)) {
                    String at = difficulty + (rook ? " with Rook" : " without Rook") + ": " + run;
                    System.out.println(at);
                    if (!run.complete()) {
                        out.add(at);
                    }
                }
            }
        }
        assertTrue(out.isEmpty(), "every run won: " + out);
    }

    /** The sortie of the plan's fit (for the probes). */
    static Sortie sortie(Content content, Loadout loadout, long seed, Difficulty difficulty) {
        return LevelMetrics.sortie(content, LEVEL, difficulty, loadout, seed);
    }
}
