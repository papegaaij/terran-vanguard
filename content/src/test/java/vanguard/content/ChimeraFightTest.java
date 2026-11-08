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
 * M11 part F, step F2: Vorne's Chimera's fight from its data (design/enemies/bosses/vornes-chimera, Duration and HP; the
 * panels, the playbooks, the regeneration, the rear sweep, the capsule's shield and the last words are the simulation's),
 * flown by the autopilot ({@link ChimeraPlay}) with the balance plan's Level 48 fit and Rook ({@link
 * Level48Test#planLoadout}) through the whole level, five seeds: "measure, then tune the HP, not the attacks". At medium
 * every seed's fight (the bar to K) lies in the mid-boss window 45–75 s, its mean is the par, no phase is quiet over
 * {@link AscendantPressure#MOST_QUIET} (from its first shot to its end, the rear sweep's drop and second out of sight
 * included: step F1c's cover), no sac is healed back to full more than {@link
 * #HEALS} times, the rear sweep comes at least twice in phase 2 and the climbing hull never touches the ship; on hard the
 * quiet rule (its length reported). A ship that stands still in phase 1 loses armour at medium on every seed.
 */
class ChimeraFightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String SLUG = "vornes-chimera";
    /** The seeds the fight is measured on (the spec's five). */
    static final long[] SEEDS = {1, 2, 3, 4, 5};

    /** The par: the autopilot's measured mean at medium (see {@link #theFightLastsItsParAtMedium}'s print). */
    static final double PAR = 59;

    /** The most times a sac may heal back to full in a fight (the spec: more means a slower rate, not more HP). */
    static final int HEALS = 2;

    /**
     * Step F1c: a sac counts as healed back to full only after it lost more than this share of its HP (a stray shot's
     * scratch of a few % healing in a second is the regeneration working, not stalling the fight: measured 2-10 %).
     */
    static final double SCRATCH = 0.2;

    /** Step F5: a shot within this of a part's centre (each axis) comes from that part, px. */
    static final double PLACE = 20;

    /** Step F5: the least volleys as it rises out of the well (4 s, one every 0.35 s, a few held off a near ship). */
    static final int RISE_SHOTS = 6;

    /** Step F5: the least enemy shots a minute in the fight at medium (before the rework 43-59: the floor is 3x that). */
    static final double SHOTS_FLOOR = 180;

    /** The least rear sweeps in phase 2 (the spec's Duration and HP). */
    static final int SWEEPS = 2;

    /** Whether the ship's hull touches the boss's body box now. */
    static boolean touching(Sortie sortie, SetPiece piece) {
        double halfW = piece.spec().body().width() / 2 + DarkPlay.HULL_X;
        double halfH = piece.spec().body().height() / 2 + DarkPlay.HULL_Y;
        return Math.abs(sortie.ship().x() - piece.renderX(1)) < halfW
                && Math.abs(sortie.ship().y() - piece.renderY(1)) < halfH;
    }

    /** The fight's measures on the attempt that completes the level. */
    static final class Watch implements LevelMetrics.Probe {
        /** s after the bar each phase began (index 0 the engagement), and K; NaN before. */
        double[] starts = new double[4];
        /** The sweeps told by each phase's end. */
        int[] sweeps = new int[3];
        /** The longest quiet stretch in each phase from its first threat, steps. */
        int[] quiet = new int[3];
        /** Shield and armour lost in each phase. */
        double[] lost = new double[3];
        /** Step F5: the enemy shots in each phase, as the level report counts them (one per {@code ENEMY_FIRED}). */
        int[] shots = new int[3];
        /** Step F5: its volleys as it rises out of the well (the entry fires). */
        int riseShots;
        /** Step F5: the climbs of the rear sweep, and the most steps from its source crossing the bottom edge to its fire. */
        int climbs;

        int climbLate;
        /**
         * Step F5: shots, missiles, lobs and launches from the place of a dead subtarget (a phase's part, no living part
         * there): a destroyed part's guns go silent.
         */
        int fromDead;

        private int crossed = -1;
        private double sourceY = Double.NaN;
        /** The sacs' heals back to full (after more than a {@link #SCRATCH}). */
        int heals;
        /** Steps the climbing hull touched the ship. */
        int touched;
        /** Each part's death, s after the bar (NaN alive). */
        double[] deaths = new double[0];

        /** What the playbook and the sweep did as each stretch passed the quiet rule (s after the bar). */
        final StringBuilder notes = new StringBuilder();

        private int run;
        private boolean opened;
        private double lastSeconds = Double.NaN;
        private int lastPhase = -1;
        private double before = Double.NaN;
        private boolean[] hurt = new boolean[0];

        Watch() {
            newAttempt();
        }

        @Override
        public void newAttempt() {
            Arrays.fill(starts, Double.NaN);
            Arrays.fill(sweeps, 0);
            Arrays.fill(quiet, 0);
            Arrays.fill(lost, 0);
            Arrays.fill(shots, 0);
            riseShots = 0;
            climbs = 0;
            climbLate = 0;
            fromDead = 0;
            crossed = -1;
            sourceY = Double.NaN;
            heals = 0;
            touched = 0;
            run = 0;
            opened = false;
            lastPhase = -1;
            before = Double.NaN;
            hurt = new boolean[0];
            notes.setLength(0);
            lastSeconds = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SetPiece piece = ChimeraPlay.chimeraBoss(sortie);
            if (piece == null) {
                return;
            }
            boolean spoken = piece.lastWords().filter(words -> words.spoken()).isPresent();
            if (!spoken && !piece.engaged()) {
                if (piece.riseShare() < 1) {
                    riseShots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
                }
                return;
            }
            if (spoken) {
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
                starts[phase] = piece.bossSeconds();
                lastPhase = phase;
                run = 0;
                opened = false;
            }
            sweeps[phase] = piece.rearSweep().orElseThrow().sweeps();
            shots[phase] += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            climbFire(sortie, piece);
            if (piece.rearSweep().orElseThrow().climbing() && touching(sortie, piece)) {
                touched++;
            }
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            if (!Double.isNaN(before)) {
                lost[phase] += Math.max(0, before - now);
            }
            before = now;
            if (hurt.length != piece.partCount()) {
                hurt = new boolean[piece.partCount()];
                deaths = new double[piece.partCount()];
                Arrays.fill(deaths, Double.NaN);
            }
            for (int p = 0; p < piece.partCount(); p++) {
                if (Double.isNaN(deaths[p]) && piece.partHp(p) <= 0) {
                    deaths[p] = piece.bossSeconds();
                }
            }
            fromDead += fromDeadParts(sortie, piece);
            BossSpec boss = piece.boss().orElseThrow();
            for (int i = 0; i < boss.regeneration().size(); i++) {
                int p = boss.regeneration().get(i).part();
                double full = piece.spec().parts().get(p).hp();
                if (piece.partHp(p) <= 0) {
                    continue;
                }
                if (piece.partHp(p) < full * (1 - SCRATCH)) {
                    hurt[p] = true;
                } else if (piece.partHp(p) >= full && hurt[p]) {
                    heals++;
                    hurt[p] = false;
                }
            }
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
                    notes.append(String.format(
                            Locale.ROOT,
                            " {%.1f %s %s %s}",
                            piece.bossSeconds(),
                            book.mode(),
                            book.kind(),
                            piece.rearSweep().orElseThrow().mode()));
                }
            }
        }

        double seconds() {
            return starts[3];
        }

        /**
         * Step F5: the rear sweep's climb fires as its source crosses the bottom edge: the steps from that crossing to
         * the first shot from the source.
         */
        private void climbFire(Sortie sortie, SetPiece piece) {
            int source = piece.playbook().orElseThrow().source();
            if (!piece.rearSweep().orElseThrow().climbing() || source < 0) {
                sourceY = Double.NaN;
                crossed = -1;
                return;
            }
            double y = piece.partY(source);
            if (!Double.isNaN(sourceY) && sourceY <= 0 && y > 0) {
                crossed = 0;
                climbs++;
            }
            sourceY = y;
            if (crossed < 0) {
                return;
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ENEMY_FIRED
                        && Math.abs(events.x(i) - piece.partX(source)) < 30
                        && Math.abs(events.y(i) - y) < 30) {
                    climbLate = Math.max(climbLate, crossed);
                    crossed = -1;
                    return;
                }
            }
            crossed++;
        }

        /** Step F5: this step's shots, missiles, lobs and launches from the place of a dead subtarget. */
        private int fromDeadParts(Sortie sortie, SetPiece piece) {
            SimEvents events = sortie.events();
            int found = 0;
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type == SimEvents.Type.ENEMY_FIRED
                        || type == SimEvents.Type.MISSILE_LAUNCHED
                        || type == SimEvents.Type.MORTAR_LOBBED
                        || type == SimEvents.Type.BOSS_LAUNCHED) {
                    found += near(piece, events.x(i), events.y(i), false)
                                    && !near(piece, events.x(i), events.y(i), true)
                                    && !nearUnit(sortie, events.x(i), events.y(i))
                            ? 1
                            : 0;
                    if (found == 1 && fromDead == 0 && notes.length() < 900) {
                        notes.append(String.format(
                                Locale.ROOT,
                                " {from a dead part: %s at %.0f,%.0f, %.2f s phase %d, play %s %s, sweep %s}",
                                type,
                                events.x(i),
                                events.y(i),
                                piece.bossSeconds(),
                                piece.phase(),
                                piece.playbook().orElseThrow().mode(),
                                piece.playbook().orElseThrow().kind(),
                                piece.rearSweep().orElseThrow().mode()));
                    }
                }
            }
            return found;
        }

        /** Whether (x, y) lies within {@link #PLACE} of a unit (a launched Dart or Chimera firing its own gun). */
        private static boolean nearUnit(Sortie sortie, double x, double y) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                if (Math.abs(sortie.enemy(i).renderX(1) - x) <= PLACE
                        && Math.abs(sortie.enemy(i).renderY(1) - y) <= PLACE) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Whether (x, y) lies within {@link #PLACE} of a subtarget (a phase's part) that is dead, or else alive (a part
         * that died this step fired before the hit: alive).
         */
        private boolean near(SetPiece piece, double x, double y, boolean living) {
            BossSpec boss = piece.boss().orElseThrow();
            for (int f = 0; f < boss.phases().size() - 1; f++) {
                java.util.List<Integer> parts = boss.phases().get(f).untilParts();
                for (int i = 0; i < parts.size(); i++) {
                    int p = parts.get(i);
                    boolean dead = !Double.isNaN(deaths[p]) && deaths[p] < piece.bossSeconds();
                    if (dead != living
                            && Math.abs(piece.partX(p) - x) <= PLACE
                            && Math.abs(piece.partY(p) - y) <= PLACE) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * The longest quiet stretch in any phase, s (step F1c: phase 3's and the rear sweep's drop included, the cover
         * as each tell starts and as the sweep drops and leaves the top edge).
         */
        double longestQuiet() {
            return Math.max(quiet[0], Math.max(quiet[1], quiet[2])) * SimStep.SECONDS;
        }

        /** Step F5: phase {@code phase}'s length, s. */
        double phaseSeconds(int phase) {
            return starts[phase + 1] - starts[phase];
        }

        /** Step F5: the enemy shots a minute in phase {@code phase}. */
        double shotsPerMinute(int phase) {
            return shots[phase] * 60 / phaseSeconds(phase);
        }

        /** Step F5: the enemy shots a minute over the fight (the bar to K). */
        double shotsPerMinute() {
            return (shots[0] + shots[1] + shots[2]) * 60 / seconds();
        }

        /** Step F5: the shield and armour the ship lost a second in phase {@code phase}. */
        double lostPerSecond(int phase) {
            return lost[phase] / phaseSeconds(phase);
        }

        int phaseTwoSweeps() {
            return sweeps[1] - sweeps[0];
        }

        @Override
        public String toString() {
            double[] lengths = new double[3];
            for (int f = 0; f < 3; f++) {
                lengths[f] = starts[f + 1] - starts[f];
            }
            return String.format(
                            Locale.ROOT,
                            "K %.1f s (phases %.1f / %.1f / %.1f), sweeps by phase %s, quiet at most %.2f / %.2f / %.2f s,"
                                    + " heals %d, hull touches %d, lost by phase %s",
                            seconds(),
                            lengths[0],
                            lengths[1],
                            lengths[2],
                            Arrays.toString(sweeps),
                            quiet[0] * SimStep.SECONDS,
                            quiet[1] * SimStep.SECONDS,
                            quiet[2] * SimStep.SECONDS,
                            heals,
                            touched,
                            Arrays.toString(Arrays.stream(lost).map(Math::round).toArray()))
                    + String.format(
                            Locale.ROOT,
                            ", shots a minute %.0f (%.0f / %.0f / %.0f), lost a second %.1f / %.1f / %.1f, rise volleys %d,"
                                    + " climbs %d (first shot within %d steps), shots from dead parts %d",
                            shotsPerMinute(),
                            shotsPerMinute(0),
                            shotsPerMinute(1),
                            shotsPerMinute(2),
                            lostPerSecond(0),
                            lostPerSecond(1),
                            lostPerSecond(2),
                            riseShots,
                            climbs,
                            climbLate,
                            fromDead)
                    + " deaths "
                    + Arrays.toString(Arrays.stream(deaths).map(Math::round).toArray())
                    + notes;
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

    /** The autopilot flies Level 48 with the plan's fit and Rook on {@code seed} (retried after a loss) and its fight. */
    static Fight fight(long seed, Difficulty difficulty) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT, Level48Test.LEVEL, difficulty, Level48Test.planLoadout(CONTENT, difficulty), seed);
        Watch watch = new Watch();
        LevelMetrics.fly(sortie, Level48Test.MAX_STEPS, watch);
        return new Fight(seed, LevelMetrics.Run.of(sortie), watch);
    }

    /** The fights over {@link #SEEDS}, flown in parallel, in their order. */
    static List<Fight> fights(Difficulty difficulty) {
        return LongStream.of(SEEDS)
                .parallel()
                .mapToObj(seed -> fight(seed, difficulty))
                .toList();
    }

    @Test
    void theStatBlockMapsToASweepingMidBossWithItsPar() {
        SetPieceSpec spec = SimSpecs.level(CONTENT, Level48Test.LEVEL, Difficulty.MEDIUM).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
        BossSpec boss = spec.boss().orElseThrow();
        assertTrue(boss.midBoss(), "a mid-boss");
        assertTrue(boss.escape().isEmpty(), "it dies on its last words");
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");
        EnemyData chimera = CONTENT.enemy(SLUG);
        double parts = chimera.partList().orElseThrow().stream()
                .mapToDouble(EnemyData.PartData::hp)
                .sum();
        assertEquals(parts, chimera.hp(), "the parts' sum");
        assertEquals(
                chimera.partList().orElseThrow().stream()
                        .mapToInt(EnemyData.PartData::bounty)
                        .sum(),
                chimera.bounty(),
                "the bounty: the parts' sum");
    }

    /**
     * Measure, then tune the HP (the spec's Duration and HP): with the plan's fit and Rook the fight lasts its par at
     * medium (the bar to K), inside the mid-boss window on every seed; never quiet; the regeneration never stalls; two
     * sweeps in phase 2 at least; the climbing hull never touches the ship.
     */
    @Test
    void theFightLastsItsParAtMedium() {
        List<Fight> fights = fights(Difficulty.MEDIUM);
        double sum = 0;
        for (Fight fight : fights) {
            System.out.printf("Chimera medium %s%n", fight);
            sum += fight.watch().seconds();
        }
        double mean = sum / SEEDS.length;
        System.out.printf(Locale.ROOT, "Chimera medium: mean %.1f s, par %.0f%n", mean, PAR);
        for (Fight fight : fights) {
            String at = fight.toString();
            Watch watch = fight.watch();
            assertTrue(fight.run().complete(), at);
            assertTrue(LevelMetrics.inBossWindow(true, watch.seconds()), "the mid-boss window: " + at);
            assertTrue(watch.longestQuiet() <= AscendantPressure.MOST_QUIET, "never quiet: " + at);
            assertTrue(watch.heals <= HEALS, "the regeneration never stalls it: " + at);
            assertTrue(watch.phaseTwoSweeps() >= SWEEPS, "two sweeps in phase 2: " + at);
            assertEquals(0, watch.touched, "the climbing hull never touches the ship: " + at);
            assertFires(watch, at);
            assertTrue(
                    watch.shotsPerMinute() >= SHOTS_FLOOR, "the fight fires " + SHOTS_FLOOR + " shots a minute: " + at);
        }
        assertEquals(PAR, mean, 3, "the par is the measured mean");
    }

    /**
     * Step F5 (round 93 L.3, the user's recipe): every part's guns go silent at its death (no shot, missile, lob or launch
     * from a dead subtarget's place), it enters firing as it rises out of the well, and every climb of the rear sweep
     * fires as its source crosses the bottom edge (within a step).
     */
    static void assertFires(Watch watch, String at) {
        assertEquals(0, watch.fromDead, "a destroyed part's guns go silent: " + at);
        assertTrue(watch.riseShots >= RISE_SHOTS, "it rises firing: " + at);
        assertTrue(watch.climbs >= SWEEPS, "the climbs measured: " + at);
        assertTrue(watch.climbLate <= 1, "the climb fires as it crosses the bottom edge: " + at);
    }

    /**
     * Hard: won on every seed and never quiet (as at medium), its length reported (the window binds medium: hard's HP lever
     * and short rests are measured, m11f-gaps.md §5). Easy: won.
     */
    @Test
    void theFightOnEasyAndHard() {
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            double sum = 0;
            for (Fight fight : fights(difficulty)) {
                System.out.printf("Chimera %s %s%n", difficulty, fight);
                String at = difficulty + " " + fight;
                assertTrue(fight.run().complete() && !Double.isNaN(fight.watch().seconds()), at);
                if (difficulty == Difficulty.HARD) {
                    // the window binds medium only (m11f §5: hard's ×1.05 HP and ×0.8 rests are measured, reported)
                    assertTrue(fight.watch().longestQuiet() <= AscendantPressure.MOST_QUIET, "never quiet: " + at);
                }
                assertEquals(0, fight.watch().fromDead, "a destroyed part's guns go silent: " + at);
                sum += fight.watch().seconds();
            }
            System.out.printf(Locale.ROOT, "Chimera %s: mean %.1f s%n", difficulty, sum / SEEDS.length);
        }
    }

    /** It stands still in phase 1 for at most this long, s. */
    static final double STILL_SECONDS = 40;

    /**
     * Standing still costs armour (round 81): the autopilot flies the approach (retried after a loss) until the Chimera
     * engages; then the ship flies back to its start place and stands there, its front gun firing, through phase 1: it
     * loses armour at medium on every seed.
     */
    @Test
    void standingStillCostsArmourInPhaseOne() {
        for (long seed : SEEDS) {
            Sortie sortie = Level48Test.sortie(
                    CONTENT, Level48Test.planLoadout(CONTENT, Difficulty.MEDIUM), seed, Difficulty.MEDIUM, false);
            double homeX = PlayField.WIDTH / 2.0;
            double homeY = sortie.ship().y();
            int steps = 0;
            SetPiece boss = null;
            while (!sortie.complete() && steps++ < Level48Test.MAX_STEPS) {
                Level04Test.step(sortie);
                boss = ChimeraPlay.chimeraBoss(sortie);
                if (boss != null && boss.engaged()) {
                    break;
                }
            }
            if (boss == null || !boss.engaged()) {
                throw new AssertionError("the Chimera engages, seed " + seed);
            }
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
            for (int i = 0; i < SimStep.ticks(STILL_SECONDS) && boss.phase() == 0 && sortie.flying(); i++) {
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
            }
            System.out.printf(
                    Locale.ROOT,
                    "Chimera stand still seed %d: %.1f s, armour lost %.0f, shield and armour lost a second %.1f%n",
                    seed,
                    stood * SimStep.SECONDS,
                    armour,
                    lost / (stood * SimStep.SECONDS));
            assertTrue(armour > 0 || !sortie.flying(), "seed " + seed + ": standing still costs armour");
        }
    }
}
