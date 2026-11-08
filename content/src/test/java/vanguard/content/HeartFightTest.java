package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.Command;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H2 (design/enemies/bosses/choir-heart, Duration and HP; m11h-gaps.md §1 #15, §5): the Choir Heart's
 * fight measured on Level 50 with the plan's fit and Rook (the autopilot's {@link HeartPlay}), five seeds: at medium
 * every seed inside 150–180 s (the bar to K), the par the measured mean; never quiet over 1.0 s in any phase; the
 * autopilot cuts all four nodes; the reversal once; it enters firing (the descent volley) and every part's guns go
 * silent at its death (the user's boss recipe, round 93 L.3); its shots a minute in the reworked Chimera's range or
 * above; and a ship standing still loses armour in every phase. Easy and hard: won (hard: without a checkpoint).
 */
class HeartFightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String SLUG = "choir-heart";
    /** The seeds the fight is measured on (the spec's five). */
    static final long[] SEEDS = {1, 2, 3, 4, 5};

    /** The par: the autopilot's measured mean at medium (see {@link #theFightLastsItsParAtMedium}'s print). */
    static final double PAR = 165;

    /** The window at medium on every seed, s (Q7 a: 150–180). */
    static final double[] WINDOW = {150, 180};

    /** A shot within this of a part's centre (each axis) comes from that part, px. */
    static final double PLACE = 20;

    /** The least volleys as it comes into view (6 s, one every 0.35 s, a few held off a near ship). */
    static final int RISE_SHOTS = 8;

    /** The least enemy shots a minute in the fight at medium (the reworked Chimera's floor). */
    static final double SHOTS_FLOOR = 180;

    /** The fight's measures on the attempt that completes the level. */
    static final class Watch implements LevelMetrics.Probe {
        /** s after the bar each phase began (index 0 the engagement), and K; NaN before. */
        double[] starts = new double[4];
        /** The longest quiet stretch in each phase from its first threat, steps. */
        int[] quiet = new int[3];
        /** Shield and armour lost in each phase. */
        double[] lost = new double[3];
        /** The enemy shots in each phase (one per {@code ENEMY_FIRED}). */
        int[] shots = new int[3];
        /** Its volleys as it comes into view (the entry fires). */
        int riseShots;
        /** Shots from the place of a dead subtarget (a phase's part, no living part there). */
        int fromDead;
        /** The reversals told in phase 2. */
        int reversals;
        /** The heartbeats. */
        int beats;
        /** Steps in each phase with the ship's column on its target ({@link HeartPlay#target}), and with none. */
        int[] onTarget = new int[3];

        int[] noTarget = new int[3];
        /** Steps in each phase. */
        int[] steps = new int[3];
        /** Each part's death, s after the bar (NaN alive). */
        double[] deaths = new double[0];

        final StringBuilder notes = new StringBuilder();
        /** The shield and armour at each phase's start. */
        double[] pools = new double[3];

        /** Each lost attempt's phase and time in the fight (s after the bar; the approach if it never engaged). */
        final StringBuilder losses = new StringBuilder();

        private int attempts;

        private int run;
        private boolean opened;
        private double lastSeconds = Double.NaN;
        private int lastPhase = -1;
        private double before = Double.NaN;

        Watch() {
            newAttempt();
        }

        @Override
        public void newAttempt() {
            if (attempts++ > 0) {
                losses.append(
                        lastPhase < 0
                                ? " {lost in the approach}"
                                : String.format(
                                        Locale.ROOT, " {lost in phase %d at %.1f s}", lastPhase + 1, lastSeconds));
            }
            Arrays.fill(starts, Double.NaN);
            Arrays.fill(quiet, 0);
            Arrays.fill(lost, 0);
            Arrays.fill(shots, 0);
            Arrays.fill(onTarget, 0);
            Arrays.fill(noTarget, 0);
            Arrays.fill(steps, 0);
            riseShots = 0;
            fromDead = 0;
            reversals = 0;
            beats = 0;
            run = 0;
            opened = false;
            lastPhase = -1;
            before = Double.NaN;
            deaths = new double[0];
            notes.setLength(0);
            lastSeconds = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SetPiece piece = HeartPlay.heartBoss(sortie);
            if (piece == null) {
                if (lastPhase >= 0 && Double.isNaN(starts[3])) {
                    starts[3] = lastSeconds;
                }
                return;
            }
            boolean dead = piece.destroyed()
                    || piece.lastWords().filter(words -> words.spoken()).isPresent();
            if (!dead && !piece.engaged()) {
                if (piece.riseShare() > 0 && piece.riseShare() < 1) {
                    riseShots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
                }
                return;
            }
            if (dead) {
                if (Double.isNaN(starts[3])) {
                    starts[3] = lastSeconds;
                }
                return;
            }
            if (piece.bossSeconds() > 0) {
                lastSeconds = piece.bossSeconds();
            }
            int phase = piece.phase();
            if (phase != lastPhase) {
                pools[phase] = sortie.ship().defences().shield()
                        + sortie.ship().defences().armour();
                starts[phase] = piece.bossSeconds();
                lastPhase = phase;
                run = 0;
                opened = false;
            }
            shots[phase] += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            steps[phase]++;
            int target = HeartPlay.target(sortie, piece, sortie.ship().x());
            if (target < 0) {
                noTarget[phase]++;
            } else if (Math.abs(sortie.ship().x() - HeartPlay.aimX(sortie, piece, target))
                    <= HeartPlay.column(sortie, piece, target)) {
                onTarget[phase]++;
            }
            reversals += sortie.events().count(SimEvents.Type.REVERSAL_TOLD);
            beats += sortie.events().count(SimEvents.Type.HEARTBEAT);
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            if (!Double.isNaN(before)) {
                lost[phase] += Math.max(0, before - now);
            }
            before = now;
            if (deaths.length != piece.partCount()) {
                deaths = new double[piece.partCount()];
                Arrays.fill(deaths, Double.NaN);
            }
            for (int p = 0; p < piece.partCount(); p++) {
                if (Double.isNaN(deaths[p]) && piece.partHp(p) <= 0) {
                    deaths[p] = piece.bossSeconds();
                }
            }
            fromDead += fromDeadParts(sortie, piece);
            if (piece.opening()) {
                run = 0;
                return;
            }
            if (AscendantPressure.threat(sortie, piece)) {
                opened = true;
                run = 0;
                return;
            }
            if (opened) {
                run++;
                quiet[phase] = Math.max(quiet[phase], run);
                if (run == SimStep.ticks(AscendantPressure.MOST_QUIET) + 1 && notes.length() < 600) {
                    var book = piece.playbook().orElseThrow();
                    notes.append(
                            String.format(Locale.ROOT, " {%.1f %s %s}", piece.bossSeconds(), book.mode(), book.kind()));
                }
            }
        }

        /** Shots this step from the place of a dead subtarget with no living part of the boss there. */
        private int fromDeadParts(Sortie sortie, SetPiece piece) {
            int n = 0;
            for (int i = 0; i < sortie.events().size(); i++) {
                if (sortie.events().type(i) != SimEvents.Type.ENEMY_FIRED) {
                    continue;
                }
                double x = sortie.events().x(i);
                double y = sortie.events().y(i);
                if (near(piece, x, y, false)
                        && !near(piece, x, y, true)
                        && !nearUnit(sortie, x, y)
                        && !onWall(sortie, x, y)) {
                    n++;
                    if (notes.length() < 900) {
                        var book = piece.playbook().orElseThrow();
                        notes.append(String.format(
                                Locale.ROOT,
                                " {from a dead part at %.0f,%.0f, %.2f s phase %d, play %s %s}",
                                x,
                                y,
                                piece.bossSeconds(),
                                piece.phase(),
                                book.mode(),
                                book.kind()));
                    }
                }
            }
            return n;
        }

        /** Whether (x, y) lies on a standing wall's face (the wall spit: the walls' own gun, not a node's). */
        private static boolean onWall(Sortie sortie, double x, double y) {
            vanguard.sim.ArenaWalls walls = sortie.arenaWalls();
            return walls != null
                    && walls.standing()
                    && (Math.abs(x - walls.leftAt(y)) <= PLACE || Math.abs(x - walls.rightAt(y)) <= PLACE);
        }

        /** Whether (x, y) lies within {@link #PLACE} of a unit (a released one firing its own gun). */
        private static boolean nearUnit(Sortie sortie, double x, double y) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                if (Math.abs(sortie.enemy(i).renderX(1) - x) <= PLACE
                        && Math.abs(sortie.enemy(i).renderY(1) - y) <= PLACE) {
                    return true;
                }
            }
            return false;
        }

        /** Whether (x, y) lies within {@link #PLACE} of a phase's part that is dead, or else alive. */
        private boolean near(SetPiece piece, double x, double y, boolean living) {
            for (int p = 0; p < piece.partCount(); p++) {
                boolean gone = !Double.isNaN(deaths[p]) && deaths[p] < piece.bossSeconds();
                if (gone != living && Math.abs(piece.partX(p) - x) <= PLACE && Math.abs(piece.partY(p) - y) <= PLACE) {
                    return true;
                }
            }
            return false;
        }

        double seconds() {
            return starts[3];
        }

        double longestQuiet() {
            return Math.max(quiet[0], Math.max(quiet[1], quiet[2])) * SimStep.SECONDS;
        }

        double phaseSeconds(int phase) {
            return starts[phase + 1] - starts[phase];
        }

        double shotsPerMinute(int phase) {
            return shots[phase] * 60 / phaseSeconds(phase);
        }

        double shotsPerMinute() {
            return (shots[0] + shots[1] + shots[2]) * 60 / seconds();
        }

        double lostPerSecond(int phase) {
            return lost[phase] / phaseSeconds(phase);
        }

        /** The nodes cut (the parts of phase 2 dead). */
        int nodesCut(SetPieceSpec spec) {
            List<Integer> nodes = spec.boss().orElseThrow().phases().get(1).untilParts();
            int n = 0;
            for (int p : nodes) {
                n += p < deaths.length && !Double.isNaN(deaths[p]) ? 1 : 0;
            }
            return n;
        }

        @Override
        public String toString() {
            return String.format(
                            Locale.ROOT,
                            "K %.1f s (phases %.1f / %.1f / %.1f), quiet at most %.2f / %.2f / %.2f s, lost by phase %s",
                            seconds(),
                            phaseSeconds(0),
                            phaseSeconds(1),
                            phaseSeconds(2),
                            quiet[0] * SimStep.SECONDS,
                            quiet[1] * SimStep.SECONDS,
                            quiet[2] * SimStep.SECONDS,
                            Arrays.toString(Arrays.stream(lost).map(Math::round).toArray()))
                    + String.format(
                            Locale.ROOT,
                            ", shots a minute %.0f (%.0f / %.0f / %.0f), lost a second %.1f / %.1f / %.1f, descent volleys"
                                    + " %d, reversals %d, heartbeats %d, shots from dead parts %d",
                            shotsPerMinute(),
                            shotsPerMinute(0),
                            shotsPerMinute(1),
                            shotsPerMinute(2),
                            lostPerSecond(0),
                            lostPerSecond(1),
                            lostPerSecond(2),
                            riseShots,
                            reversals,
                            beats,
                            fromDead)
                    + String.format(
                            Locale.ROOT,
                            ", on target %.0f / %.0f / %.0f %%, no target %.0f / %.0f / %.0f %%",
                            100.0 * onTarget[0] / Math.max(1, steps[0]),
                            100.0 * onTarget[1] / Math.max(1, steps[1]),
                            100.0 * onTarget[2] / Math.max(1, steps[2]),
                            100.0 * noTarget[0] / Math.max(1, steps[0]),
                            100.0 * noTarget[1] / Math.max(1, steps[1]),
                            100.0 * noTarget[2] / Math.max(1, steps[2]))
                    + String.format(
                            Locale.ROOT, ", pools at the phases %.0f / %.0f / %.0f", pools[0], pools[1], pools[2])
                    + " deaths "
                    + Arrays.toString(Arrays.stream(deaths).map(Math::round).toArray())
                    + notes
                    + losses;
        }
    }

    /** A fight: the level's run and the watch. */
    record Fight(long seed, LevelMetrics.Run run, Watch watch) {
        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "seed %d: %s, retries %d; %s",
                    seed,
                    run.complete() ? "won" : "LOST",
                    run.retries(),
                    watch);
        }
    }

    /** The autopilot flies Level 50 with the plan's fit and Rook on {@code seed} (retried after a loss) and its fight. */
    static Fight fight(long seed, Difficulty difficulty) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT, Level50Test.LEVEL, difficulty, Level50Test.planLoadout(CONTENT, difficulty), seed);
        Watch watch = new Watch();
        LevelMetrics.fly(sortie, Level50Test.MAX_STEPS, watch);
        return new Fight(seed, LevelMetrics.Run.of(sortie), watch);
    }

    /** The fights over {@link #SEEDS}, flown in parallel, in their order. */
    static List<Fight> fights(Difficulty difficulty) {
        return LongStream.of(SEEDS)
                .parallel()
                .mapToObj(seed -> fight(seed, difficulty))
                .toList();
    }

    static SetPieceSpec spec(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, Level50Test.LEVEL, difficulty).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theStatBlockMapsToAnActBossWithItsPar() {
        BossSpec boss = spec(Difficulty.MEDIUM).boss().orElseThrow();
        assertTrue(!boss.midBoss(), "the act's boss");
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");
        assertTrue(boss.lastWords().orElseThrow().flashes(), "the act's flash at K + 3.5 s");
        EnemyData heart = CONTENT.enemy(SLUG);
        assertEquals(
                heart.partList().orElseThrow().stream()
                        .mapToDouble(EnemyData.PartData::hp)
                        .sum(),
                heart.hp(),
                "the parts' sum");
        assertEquals(
                heart.partList().orElseThrow().stream()
                        .mapToInt(EnemyData.PartData::bounty)
                        .sum(),
                heart.bounty(),
                "the bounty: the parts' sum");
    }

    /**
     * Measure, then tune the HP (the spec's Duration and HP): with the plan's fit and Rook the fight lasts its par at
     * medium (the bar to K), inside 150–180 s on every seed; never quiet; all four nodes cut; the reversal once; it enters
     * firing; dead parts' guns silent; the shots a minute at the floor or above.
     */
    @Test
    void theFightLastsItsParAtMedium() {
        List<Fight> fights = fights(Difficulty.MEDIUM);
        double sum = 0;
        for (Fight fight : fights) {
            System.out.printf("Heart medium %s%n", fight);
            sum += fight.watch().seconds();
        }
        double mean = sum / SEEDS.length;
        System.out.printf(Locale.ROOT, "Heart medium: mean %.1f s, par %.0f%n", mean, PAR);
        SetPieceSpec spec = spec(Difficulty.MEDIUM);
        for (Fight fight : fights) {
            String at = fight.toString();
            Watch watch = fight.watch();
            assertTrue(fight.run().complete(), at);
            assertTrue(watch.seconds() >= WINDOW[0] && watch.seconds() <= WINDOW[1], "every seed in 150-180 s: " + at);
            assertTrue(watch.longestQuiet() <= AscendantPressure.MOST_QUIET, "never quiet: " + at);
            assertEquals(4, watch.nodesCut(spec), "all four nodes cut: " + at);
            assertEquals(1, watch.reversals, "the reversal once: " + at);
            assertEquals(0, watch.fromDead, "a destroyed part's guns go silent: " + at);
            assertTrue(watch.riseShots >= RISE_SHOTS, "it comes into view firing: " + at);
            assertTrue(watch.shotsPerMinute() >= SHOTS_FLOOR, "the fight fires " + SHOTS_FLOOR + " a minute: " + at);
        }
        assertEquals(PAR, mean, 3, "the par is the measured mean");
    }

    /** Hard: won on every seed (no checkpoint), never quiet, its length reported. Easy: won. */
    @Test
    void theFightOnEasyAndHard() {
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            double sum = 0;
            for (Fight fight : fights(difficulty)) {
                System.out.printf("Heart %s %s%n", difficulty, fight);
                String at = difficulty + " " + fight;
                assertTrue(fight.run().complete() && !Double.isNaN(fight.watch().seconds()), at);
                if (difficulty == Difficulty.HARD) {
                    assertTrue(fight.watch().longestQuiet() <= AscendantPressure.MOST_QUIET, "never quiet: " + at);
                }
                assertEquals(0, fight.watch().fromDead, "a destroyed part's guns go silent: " + at);
                sum += fight.watch().seconds();
            }
            System.out.printf(Locale.ROOT, "Heart %s: mean %.1f s%n", difficulty, sum / SEEDS.length);
        }
    }

    /** It stands still in a phase for at most this long, s. */
    static final double STILL_SECONDS = 30;

    /**
     * Standing still costs armour (round 81) in every phase: the autopilot flies until the phase starts (after its
     * opening); then the ship flies to its start place and stands there, its front gun firing: it
     * loses armour at medium on every seed; the time a ship takes to lose its armour is printed (the recipe: ≈ 15 s).
     */
    @Test
    void standingStillCostsArmourInEveryPhase() {
        for (int phase = 0; phase < 3; phase++) {
            for (long seed : new long[] {1, 2, 3}) {
                Sortie sortie = Level50Test.sortie(
                        CONTENT, Level50Test.planLoadout(CONTENT, Difficulty.MEDIUM), seed, Difficulty.MEDIUM);
                SetPiece boss = null;
                int steps = 0;
                while (!sortie.complete() && steps++ < Level50Test.MAX_STEPS) {
                    Level04Test.step(sortie);
                    boss = HeartPlay.heartBoss(sortie);
                    if (boss != null && boss.engaged()) {
                        break;
                    }
                }
                if (boss == null || !boss.engaged()) {
                    throw new AssertionError("the Heart engages, seed " + seed);
                }
                while ((boss.phase() < phase || boss.opening())
                        && !sortie.complete()
                        && steps++ < Level50Test.MAX_STEPS) {
                    Level04Test.step(sortie);
                }
                double homeX = PlayField.WIDTH / 2.0;
                double homeY = 60;
                ToIntFunction<Sortie> home = s -> {
                    double dx = homeX - s.ship().x();
                    double dy = homeY - s.ship().y();
                    int c = Command.FIRE.bit();
                    c |= dx > 2 ? Command.RIGHT.bit() : dx < -2 ? Command.LEFT.bit() : 0;
                    c |= dy > 2 ? Command.UP.bit() : dy < -2 ? Command.DOWN.bit() : 0;
                    return c;
                };
                double armour = 0;
                double lost = 0;
                int stood = 0;
                double pool = sortie.ship().defences().armour()
                        + sortie.ship().defences().shield();
                double gone = Double.NaN;
                for (int i = 0; i < SimStep.ticks(STILL_SECONDS) && boss.phase() == phase && sortie.flying(); i++) {
                    double a = sortie.ship().defences().armour();
                    double before = a + sortie.ship().defences().shield();
                    sortie.step(home.applyAsInt(sortie));
                    armour += Math.max(0, a - sortie.ship().defences().armour());
                    lost += Math.max(
                            0,
                            before
                                    - sortie.ship().defences().armour()
                                    - sortie.ship().defences().shield());
                    stood++;
                    if (Double.isNaN(gone) && sortie.ship().defences().armour() <= 0) {
                        gone = stood * SimStep.SECONDS;
                    }
                }
                System.out.printf(
                        Locale.ROOT,
                        "Heart stand still phase %d seed %d: %.1f s, pool %.0f, armour lost %.0f, lost a second %.1f,"
                                + " armour gone at %.1f s%n",
                        phase + 1,
                        seed,
                        stood * SimStep.SECONDS,
                        pool,
                        armour,
                        lost / (stood * SimStep.SECONDS),
                        gone);
                assertTrue(
                        armour > 0 || !sortie.flying(),
                        "phase " + (phase + 1) + " seed " + seed + ": standing still costs armour");
            }
        }
    }
}
