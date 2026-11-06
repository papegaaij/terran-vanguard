package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M11 part B, step B3: Level 43 The Tether Gate from its data (design/campaign/act-7-beyond-the-gate/level-43-the-tether-
 * gate: the mouth, the pickets, the ring's shadow, the glyph stones along the ring, the gate transit; the Gate Wardens'
 * seven groups, the secondary {@code WARDENS} failed by the first Warden that rises out of the top edge), flown by the
 * autopilot ({@link WardenPlay} among the Wardens: a window timed, the Tesla Coil Pod's arc held on a sealed Warden, off
 * an aimed line's lock, the leaving Warden first) with the balance plan's fit ({@link #planLoadout}), with Rook and
 * without him ({@code --escort none}): every run won on every difficulty, the secondary met on most runs. The pacing, the
 * floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link PacingTest}, {@link
 * ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level43Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-43-the-tether-gate";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 43, MW (the Tesla Coil Pod L4). */
    static final double LOAD = 23.5;

    /** The Gate Warden's slug. */
    static final String WARDEN = "gate-warden";

    /**
     * The runs of a fit (of {@link #SEEDS} on each difficulty) with the secondary met, at least ("most runs"): every
     * easy and medium one; hard's all 18 sometimes lose one (step B3: 7 of 9 with Rook and without).
     */
    static final int SECONDARY_RUNS = 6;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 43 ({@code "43"} in design/player/balance-plan.yaml: the visit raises the Tesla
     * Coil Pod to L4) with Rook in the slot (Level 42's Autocannon, at full armour): the Ion Beam L5, the Tesla Coil Pod
     * L4, the right Micro-missile Pod L5, the Swarm Tail L3, the Shield Mk III, Composite V, the Crucible's 25 MW with
     * 23.5 fitted, the EMP Burst.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return actSeven(content, difficulty, 3, LOAD);
    }

    /**
     * The plan's Act 7 fit without Rook (Levels 43 and 44): Level 42's guns with the Tesla Coil Pod L4 and the Swarm Tail
     * at {@code tail}, {@code load} MW fitted.
     */
    static Loadout actSeven(Content content, Difficulty difficulty, int tail, double load) {
        String shield = content.shields().models().get(2).name();
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", tail)),
                        shield,
                        content.armour().plating().get(5).name(),
                        Level40Test.OUTPUT - load,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, Level40Test.OUTPUT, load, shield, difficulty));
    }

    static Sortie sortie(Content content, Loadout loadout, long seed, Difficulty difficulty, boolean invulnerable) {
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    /**
     * Step B2's data: the units counted as enemies {@link #COUNTED} per difficulty (the waves'; the glyph stones are
     * ground targets, not enemies), the Gate Wardens {@link #WARDENS} (seven groups; hard's t=62 and t=94 lines of three),
     * the secondary {@code escapes: gate-warden} with none allowed (easy 2), the gate transit at t=262.
     */
    @Test
    void theLevelHoldsItsCount() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        Map<Difficulty, Integer> wardens = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            counted.put(
                    difficulty,
                    level.waves().stream()
                            .filter(wave -> wave.enemy().countsAsEnemy())
                            .mapToInt(WaveSpec::count)
                            .sum());
            wardens.put(
                    difficulty,
                    level.waves().stream()
                            .filter(wave -> wave.enemy().slug().equals(WARDEN))
                            .mapToInt(WaveSpec::count)
                            .sum());
            assertTrue(level.objectives().secondary().byEscapes(), difficulty + ": the Wardens' escapes");
        }
        System.out.println("Level 43 counted enemies: " + counted + ", Wardens " + wardens);
        assertEquals(COUNTED, counted);
        assertEquals(WARDENS, wardens);
    }

    /** Step B3b's waves' counts (B2's 424 / 556 / 690 with the denser mixed waves, the user's answer of 2026-11-05). */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 474, Difficulty.MEDIUM, 623, Difficulty.HARD, 774);

    /** Step B3b: the Warden lines' spacing, px between their centres (a line of three at x 80, 240 and 400). */
    static final double LINE_SPACING = 160;

    /** The Gate Warden's hull (its sprite's width), px: the lines keep at least {@link #HULL_GAP} between hulls. */
    static final double HULL = 76;

    /** The least gap between two Wardens' hulls in a line (the Act 5 lesson, the Warden's document), px. */
    static final double HULL_GAP = 80;

    /**
     * Step B3b (B3's item 5): every Warden line abreast (a pair, a line of three, hard's lines of three) stands {@link
     * #LINE_SPACING} apart, its hulls at least {@link #HULL_GAP} apart, and inside the play field.
     */
    @Test
    void theWardenLinesStandAbreastWithRoomBetweenTheirHulls() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> lines = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().slug().equals(WARDEN) && wave.count() > 1)
                    .toList();
            assertEquals(6, lines.size(), difficulty + ": six Warden lines");
            for (WaveSpec line : lines) {
                assertEquals(java.util.Optional.of(LINE_SPACING), line.lineSpacing(), difficulty + " t=" + line.t());
                assertTrue(LINE_SPACING - HULL >= HULL_GAP, "the hulls' gap");
                double half = (line.count() - 1) / 2.0 * LINE_SPACING + HULL / 2;
                assertTrue(half <= vanguard.sim.PlayField.WIDTH / 2, difficulty + " t=" + line.t() + ": on the field");
            }
        }
    }

    /** The Gate Wardens per difficulty: 16 (hard 18, the t=62 and t=94 lines of three). */
    static final Map<Difficulty, Integer> WARDENS =
            Map.of(Difficulty.EASY, 16, Difficulty.MEDIUM, 16, Difficulty.HARD, 18);

    /** What a run saw, over every attempt (the losses) and in its completing one (the Wardens). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new java.util.ArrayList<>();
        /** Laser lines that struck the ship in the completing attempt. */
        int lineHits;
        /** The most Wardens on the screen at once, and the most telegraphing at once, over every attempt. */
        int mostOnScreen;

        int mostTelegraphing;
        /** The Wardens killed in the completing attempt (the others rose out of the top edge). */
        int killed;

        @Override
        public void newAttempt() {
            lineHits = 0;
            killed = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), seen.x(i)));
                    case LINE_HIT -> lineHits++;
                    case ENEMY_DESTROYED -> {
                        if (sortie.enemyKinds().get(seen.value(i)).slug().equals(WARDEN)) {
                            killed++;
                        }
                    }
                    default -> {}
                }
            }
            int on = 0;
            int telegraphing = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals(WARDEN) || enemy.hp() <= 0) {
                    continue;
                }
                if (LevelMetrics.onScreen(
                        enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                    on++;
                }
                telegraphing += enemy.lineBeam().telegraphing() ? 1 : 0;
            }
            mostOnScreen = Math.max(mostOnScreen, on);
            mostTelegraphing = Math.max(mostTelegraphing, telegraphing);
        }
    }

    /** One whole flight's measures. */
    record Flight(
            String fit,
            Difficulty difficulty,
            long seed,
            boolean complete,
            LevelMetrics.Run run,
            Watch watch,
            int total) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch();
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(fit, difficulty, seed, complete, LevelMetrics.Run.of(sortie), watch, sortie.escapesTotal());
    }

    /** The flights of a fit over the seeds and difficulties, flown in parallel, in their order. */
    List<Flight> flights(String fit, BiFunction<Content, Difficulty, Loadout> loadout) {
        Difficulty[] difficulties = Difficulty.values();
        return IntStream.range(0, difficulties.length * SEEDS)
                .parallel()
                .mapToObj(i -> {
                    Difficulty difficulty = difficulties[i / SEEDS];
                    return fly(fit, loadout.apply(content, difficulty), difficulty, 1 + i % SEEDS);
                })
                .toList();
    }

    private static String summary(Flight flight) {
        return String.format(
                "%n    %s: %s, retries %d, losses %s; Wardens killed %d / %d %s, line hits %d, credits %d;"
                        + " at most %d on the screen, %d telegraphing",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                flight.watch().deaths,
                flight.watch().killed,
                flight.total(),
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.watch().lineHits,
                flight.run().credits(),
                flight.watch().mostOnScreen,
                flight.watch().mostTelegraphing);
    }

    /**
     * Each run's checks: completed; the Wardens' limits held (design/enemies/space/gate-warden: at most 4 on the screen,
     * 2 telegraphing).
     */
    private static void check(Flight flight) {
        String at = flight.at();
        assertTrue(flight.complete(), at + ": completes");
        assertTrue(flight.watch().mostOnScreen <= 4, at + ": Wardens on the screen " + flight.watch().mostOnScreen);
        assertTrue(
                flight.watch().mostTelegraphing <= 2, at + ": Wardens telegraphing " + flight.watch().mostTelegraphing);
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 43 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            check(flight);
        }
        long met =
                flights.stream().filter(flight -> flight.run().secondaryMet()).count();
        assertTrue(
                met >= SECONDARY_RUNS,
                fit + ": runs with the secondary met: " + met + " of " + flights.size() + ", at least "
                        + SECONDARY_RUNS);
    }

    /** The autopilot completes the level with the plan's fit and Rook on every difficulty and seed. */
    @Test
    void theAutopilotCrossesTheMouthWithRook() {
        report("Rook", flights("Rook", Level43Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotCrossesTheMouthWithoutRook() {
        report("--escort none", flights("--escort none", Level43Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level43Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        flyToEnd(first);
        flyToEnd(second);

        assertEquals(first.stateHash(), second.stateHash());
        assertTrue(first.complete());
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
