package vanguard.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import vanguard.sim.BossSpec;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.Pursuit;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tendril;
import vanguard.sim.WaveSpec;

/**
 * The measurements of a level that the campaign-wide tests assert on ({@link PacingTest}, {@link ActFloorTest}, {@link
 * DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}, the bosses' windows) and the level tests' autopilot
 * runs, in one place, so the level report ({@link LevelReport}, {@code ./gradlew :content:levelReport}) prints the
 * numbers the tests compute with the same code.
 */
final class LevelMetrics {
    /** The content every measuring test and the report share: one load per JVM. */
    static final Content CONTENT = ContentLoader.fromClasspath();

    /** The seed the campaign-wide tests fly each level on. */
    static final long SEED = 2185;

    /** A mid-boss's fight at medium, s (design/enemies/bosses, target duration). */
    static final double MID_BOSS_LEAST = 45;

    static final double MID_BOSS_MOST = 75;
    /** An act boss's fight at medium, s. */
    static final double ACT_BOSS_LEAST = 90;

    static final double ACT_BOSS_MOST = 180;

    private LevelMetrics() {}

    /** The target duration of a boss's fight at medium, {least, most} s: a mid-boss's or an act boss's. */
    static double[] bossWindow(boolean midBoss) {
        return midBoss ? new double[] {MID_BOSS_LEAST, MID_BOSS_MOST} : new double[] {ACT_BOSS_LEAST, ACT_BOSS_MOST};
    }

    /** Whether a fight of {@code seconds} lies in the boss's window. */
    static boolean inBossWindow(boolean midBoss, double seconds) {
        double[] window = bossWindow(midBoss);
        return seconds >= window[0] && seconds <= window[1];
    }

    /** A sortie of the level at full armour. */
    static Sortie sortie(Content content, String key, Difficulty difficulty, Loadout loadout, long seed) {
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, key, difficulty),
                SimSpecs.rules(content, key, difficulty),
                loadout.plating().maxArmour());
    }

    /** What a flight's watcher sees: each new attempt, then every step after it is flown. */
    interface Probe {
        /** A new attempt began this step (the level was retried): forget the last one. */
        default void newAttempt() {}

        /** The sortie after its {@code step}-th step (1 for the first). */
        void stepped(Sortie sortie, int step);
    }

    /**
     * Flies the sortie with the autopilot ({@link Level04Test#step}: a retry at full armour after a loss or a failed
     * primary) until it completes or {@code maxSteps} pass, showing each step to the probes.
     *
     * @return whether the level was completed
     */
    static boolean fly(Sortie sortie, int maxSteps, Probe... probes) {
        int attempt = sortie.attempt();
        int steps = 0;
        while (!sortie.complete() && steps++ < maxSteps) {
            Level04Test.step(sortie);
            if (sortie.attempt() != attempt) {
                attempt = sortie.attempt();
                for (Probe probe : probes) {
                    probe.newAttempt();
                }
            }
            for (Probe probe : probes) {
                probe.stepped(sortie, steps);
            }
        }
        return sortie.complete();
    }

    /**
     * What the player saw on the attempt that completes the level, from the end of its launch: the empty stretches
     * as [from, to] in real seconds since the attempt's start, the mean number of enemies on the screen, and the
     * enemy shots a minute (one per {@code ENEMY_FIRED} event: a fan or a burst counts once), both outside a boss
     * fight (step WB); M8 part H: nothing from the kill of a level with an armour floor on (Level 28's finale).
     */
    record Flight(List<double[]> empty, double meanOnScreen, double shotsPerMinute) {
        /** The empty stretches longer than {@code seconds}. */
        List<double[]> pausesOver(double seconds) {
            return empty.stream()
                    .filter(stretch -> stretch[1] - stretch[0] > seconds)
                    .toList();
        }
    }

    /** {@link PacingTest}'s and {@link ActFloorTest}'s measure: see {@link Flight}. */
    static final class Pacing implements Probe {
        private final List<double[]> stretches = new ArrayList<>();
        private double emptySince = -1;
        private double seconds;
        private long onScreen;
        private int flown;
        private int shots;
        private double realSeconds;

        @Override
        public void newAttempt() {
            stretches.clear();
            emptySince = -1;
            seconds = 0;
            onScreen = 0;
            flown = 0;
            shots = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            if (sortie.launching() || sortie.ship().defences().floor() > 0) {
                // M8 part H: from a boss's kill on a level with an armour floor (Level 28's climb, breakout and run,
                // which
                // cannot be lost) the finale is outside the pacing checks (the level's README, Implementation).
                return;
            }
            realSeconds = sortie.realSeconds();
            // A boss fight has its own tests of what it fires (KrakenTest, SiegeSpireTest): the floor's means are the
            // level's around it (step WB, pending the user's review).
            if (sortie.flying() && !bossFight(sortie)) {
                seconds += SimStep.SECONDS;
                flown++;
                onScreen += enemiesOnScreen(sortie);
                shots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            }
            boolean empty = !occupied(sortie);
            if (empty && emptySince < 0) {
                emptySince = sortie.realSeconds();
            } else if (!empty && emptySince >= 0) {
                stretches.add(new double[] {emptySince, sortie.realSeconds()});
                emptySince = -1;
            }
        }

        /** The flight so far; a stretch still empty ends at the last step. */
        Flight flight() {
            List<double[]> empty = new ArrayList<>(stretches);
            if (emptySince >= 0) {
                empty.add(new double[] {emptySince, realSeconds});
            }
            return new Flight(empty, (double) onScreen / flown, shots / (seconds / 60));
        }
    }

    /**
     * {@link BeamEraHpTest}'s measure: the mean time on the screen, from its entry to its death, of a {@code small}
     * unit killed while on the screen, on the attempt that completes the level.
     */
    static final class SmallLife implements Probe {
        private final Content content;
        private final Map<Integer, int[]> entered = new HashMap<>(); // serial → {entry step, last step, on screen}
        private int killed;
        private long lived;

        SmallLife(Content content) {
            this.content = content;
        }

        @Override
        public void newAttempt() {
            entered.clear();
            killed = 0;
            lived = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                EnemyData data = content.enemies().get(enemy.spec().slug()); // a chain's members have no stat block
                if (data == null || data.tier() != Tier.SMALL) {
                    continue;
                }
                boolean on = onScreen(
                        enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox());
                int[] track = entered.get(enemy.serial());
                if (track == null && on) {
                    entered.put(enemy.serial(), new int[] {step, step, 1});
                } else if (track != null) {
                    track[1] = step;
                    track[2] = on ? 1 : 0;
                }
            }
            for (Iterator<int[]> it = entered.values().iterator(); it.hasNext(); ) {
                int[] track = it.next();
                if (track[1] == step) {
                    continue;
                }
                // Gone this step: killed if it was on the screen, else it left.
                if (track[2] == 1) {
                    killed++;
                    lived += track[1] - track[0];
                }
                it.remove();
            }
        }

        /** The small units killed on the screen. */
        int killed() {
            return killed;
        }

        /** Their mean time on the screen, s (NaN for none). */
        double seconds() {
            return killed == 0 ? Double.NaN : lived * SimStep.SECONDS / killed;
        }
    }

    /**
     * M7 part G (step SW): a pursuit swarm's life on the attempt that completes the level (Level 20): the seconds a
     * pursuit flies, the mean number of its flock's members (units with a flock) on the screen over them, its lunges
     * (each tell counts once) and those of them with motes on the screen at the tell's start (the leader point lunges
     * on with an empty flock until a refill joins).
     */
    record PursuitRun(double seconds, double meanMotes, int lunges, int lungesWithMotes) {}

    /** {@link PursuitRun}'s measure. */
    static final class PursuitWatch implements Probe {
        private int steps;
        private long motes;
        private int lunges;
        private int lungesWithMotes;
        private boolean telling;

        @Override
        public void newAttempt() {
            steps = 0;
            motes = 0;
            lunges = 0;
            lungesWithMotes = 0;
            telling = false;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            var chase = sortie.pursuit();
            if (chase.isEmpty()) {
                telling = false;
                return;
            }
            steps++;
            int seen = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().flock().isPresent()
                        && onScreen(
                                enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                    seen++;
                }
            }
            motes += seen;
            boolean tell = chase.get().phase() == Pursuit.Phase.TELL;
            if (tell && !telling) {
                lunges++;
                lungesWithMotes += seen > 0 ? 1 : 0;
            }
            telling = tell;
        }

        PursuitRun run() {
            return new PursuitRun(
                    steps * SimStep.SECONDS, steps == 0 ? 0 : (double) motes / steps, lunges, lungesWithMotes);
        }
    }

    /** Flies the autopilot as {@link #autopilot} and returns its pursuit's numbers; none if it never completes. */
    static PursuitRun pursuit(
            Content content, String key, Difficulty difficulty, Loadout loadout, long seed, int steps) {
        PursuitWatch watch = new PursuitWatch();
        fly(sortie(content, key, difficulty, loadout, seed), steps, watch);
        return watch.run();
    }

    /** Whether the level flies a pursuit swarm (a wave with a {@code pursuit} block). */
    static boolean hasPursuit(LevelScript level) {
        return level.waves().stream().anyMatch(wave -> wave.pursuit().isPresent());
    }

    /**
     * M7 part H (step H4): a breaching boss's fight on the attempt that completes the level (Level 21's Dust Colossus):
     * per boss phase (the last counting every later one) the seconds it is engaged and alive and the shield and the
     * armour the ship lost in them, the bites and the eruptions that caught the ship, the breaches it made.
     */
    record ColossusRun(
            double[] phaseSeconds,
            double[] damage,
            double[] armourLost,
            int[] shots,
            int bites,
            int eruptions,
            int breaches,
            int[] breachesByPhase) {
        /** Its shots a minute in phase {@code phase}, as the level report counts them (one per {@code ENEMY_FIRED}). */
        double shotsPerMinute(int phase) {
            return phaseSeconds[phase] > 0 ? shots[phase] * 60 / phaseSeconds[phase] : 0;
        }

        /** The fight's seconds, its phases' sum. */
        double seconds() {
            return phaseSeconds[0] + phaseSeconds[1] + phaseSeconds[2];
        }

        /** The shield and armour lost per 10 s of phase {@code phase} (the calibration harness's measure in phase 1). */
        double lostPerTen(int phase) {
            return phaseSeconds[phase] > 0 ? damage[phase] * 10 / phaseSeconds[phase] : 0;
        }
    }

    /** {@link ColossusRun}'s measure; it allocates nothing per step. */
    static final class ColossusWatch implements Probe {
        private final double[] phaseSeconds = new double[3];
        private final double[] damage = new double[3];
        private final double[] armourLost = new double[3];
        private final int[] shots = new int[3];
        private int bites;
        private int eruptions;
        private int breaches;
        private final int[] breachesByPhase = new int[3];
        private double shield = Double.NaN;
        private double armour = Double.NaN;

        @Override
        public void newAttempt() {
            java.util.Arrays.fill(phaseSeconds, 0);
            java.util.Arrays.fill(damage, 0);
            java.util.Arrays.fill(armourLost, 0);
            java.util.Arrays.fill(shots, 0);
            bites = 0;
            eruptions = 0;
            breaches = 0;
            java.util.Arrays.fill(breachesByPhase, 0);
            shield = Double.NaN;
            armour = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double nowShield = sortie.ship().defences().shield();
            double nowArmour = sortie.ship().defences().armour();
            double lostShield = Double.isNaN(shield) ? 0 : Math.max(0, shield - nowShield);
            double lostArmour = Double.isNaN(armour) ? 0 : Math.max(0, armour - nowArmour);
            shield = nowShield;
            armour = nowArmour;
            SetPiece boss = ColossusPlay.breachingBoss(sortie);
            if (boss == null || !boss.engaged()) {
                return;
            }
            int phase = Math.min(2, boss.phase());
            phaseSeconds[phase] += SimStep.SECONDS;
            damage[phase] += lostShield + lostArmour;
            armourLost[phase] += lostArmour;
            SimEvents events = sortie.events();
            shots[phase] += events.count(SimEvents.Type.ENEMY_FIRED);
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BITE_SNAP && (events.value(e) & 1) != 0) {
                    bites++;
                }
                if (events.type(e) == SimEvents.Type.ERUPTION && (events.value(e) & 1) != 0) {
                    eruptions++;
                }
            }
            int now = boss.breach().orElseThrow().breaches();
            breachesByPhase[phase] += now - breaches;
            breaches = now;
        }

        ColossusRun run() {
            return new ColossusRun(
                    phaseSeconds.clone(),
                    damage.clone(),
                    armourLost.clone(),
                    shots.clone(),
                    bites,
                    eruptions,
                    breaches,
                    breachesByPhase.clone());
        }
    }

    /** Flies the autopilot as {@link #autopilot} and returns its breaching boss's fight. */
    static ColossusRun colossus(
            Content content, String key, Difficulty difficulty, Loadout loadout, long seed, int steps) {
        ColossusWatch watch = new ColossusWatch();
        fly(sortie(content, key, difficulty, loadout, seed), steps, watch);
        return watch.run();
    }

    /** Whether the level has a breaching boss (a set piece whose boss breaches). */
    static boolean hasBreach(LevelScript level) {
        return level.setPieces().stream()
                .anyMatch(piece -> piece.boss().flatMap(BossSpec::breach).isPresent());
    }

    /** Whether a boss (a set piece) is engaged and alive: its fight, left out of the floor's means. */
    private static boolean bossFight(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.engaged() && !piece.destroyed()) {
                return true;
            }
        }
        return false;
    }

    /** The live enemies whose hitbox overlaps the play field (set pieces, tendrils and chains not counted). */
    private static int enemiesOnScreen(Sortie sortie) {
        int count = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (onScreen(enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                count++;
            }
        }
        return count;
    }

    private static boolean occupied(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            // M7 part B (round 36 Q.15): a Burrower counts as on the screen in every phase, buried too.
            if (enemy.burrowPhase() != Enemy.BurrowPhase.NONE
                    || onScreen(enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                return true;
            }
        }
        // M7 part C (Level 16): a gate's standing tendril and a Threadcrawler's members count as on the screen.
        if (sortie.links() != null) {
            for (int i = 0; i < sortie.links().size(); i++) {
                Tendril tendril = sortie.links().get(i);
                if (tendril.standing()
                        && (onScreen(tendril.ax(), tendril.ay(), POINT)
                                || onScreen(tendril.bx(), tendril.by(), POINT))) {
                    return true;
                }
            }
        }
        for (int c = 0; c < sortie.chainCount(); c++) {
            var chain = sortie.chain(c);
            for (int k = 0; k < chain.size(); k++) {
                Enemy member = chain.member(k);
                if (member != null
                        && onScreen(
                                member.renderX(1),
                                member.renderY(1),
                                member.spec().hitbox())) {
                    return true;
                }
            }
        }
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && !piece.destroyed() && !piece.escaped()) {
                return true;
            }
        }
        return false;
    }

    private static final Hitbox POINT = new Hitbox(0, 0);

    /** Whether a hitbox at (x, y) overlaps the play field. */
    static boolean onScreen(double x, double y, Hitbox box) {
        return x + box.width() / 2 > 0
                && x - box.width() / 2 < PlayField.WIDTH
                && y + box.height() / 2 > 0
                && y - box.height() / 2 < PlayField.HEIGHT;
    }

    /**
     * {@link DensityTest}'s count: the units of the waves (with their broods' and spawners' releases) and the ground
     * units (a periodic spawner's one release) from the end of the launch to the level's first arena, and the minutes
     * they come in.
     */
    record Density(int waves, int ground, double minutes) {
        int enemies() {
            return waves + ground;
        }

        double perMinute() {
            return enemies() / minutes;
        }
    }

    static Density density(LevelScript level) {
        double end = level.seconds();
        double start = 0;
        for (Section section : level.sections()) {
            if (section.arena()) {
                end = start;
                break;
            }
            start = section.end();
        }
        int waves = 0;
        for (WaveSpec wave : level.waves()) {
            if (wave.t() < end) {
                int released = wave.enemy().brood().map(EnemySpec.Brood::count).orElse(0)
                        + wave.enemy().spawner().map(EnemySpec.Spawner::count).orElse(0);
                waves += planned(wave) * (1 + released);
            }
        }
        int ground = 0;
        for (GroundUnit unit : level.groundUnits()) {
            if (unit.t() < end) {
                // M5 part C: a periodic spawner (Level 09's Hive Nodes) counts one release
                ground +=
                        1 + unit.enemy().spawner().map(EnemySpec.Spawner::count).orElse(0);
            }
        }
        return new Density(waves, ground, (end - level.launchSeconds()) / 60);
    }

    /**
     * A wave's planned units: its count, and (M7 part G) a pursuit's refills, one group of {@code refill} every {@code
     * refill_every} s before its break-off (Level 20's 10 + 8 × 3 at medium); the refills a full flock leaves out at
     * run time still count, as in the README's tables.
     */
    static int planned(WaveSpec wave) {
        return wave.count()
                + wave.pursuit()
                        .map(pursuit -> pursuit.refill() * pursuit.refills(wave.t()))
                        .orElse(0);
    }

    /** {@link DensityTest}'s minimum for the level: the warm-up's or its act's. */
    static int densityMinimum(LevelScript level) {
        return level.number() <= DensityTest.LAST_WARM_UP
                ? DensityTest.WARM_UP_MINIMUM
                : DensityTest.ACT_MINIMUM[level.act() - 1];
    }

    /**
     * The level's {@code bounty_scale}, to two decimals, whose typical haul at medium ({@link TypicalHaul}) lands
     * nearest to its budget.
     */
    static double bountyScaleForBudget(Content content, String key) {
        double none = TypicalHaul.of(content, key, Difficulty.MEDIUM, 0).typical();
        TypicalHaul full = TypicalHaul.of(content, key, Difficulty.MEDIUM, 1);
        double guess = Math.max(0.01, Math.rint(100 * (full.budget() - none) / (full.typical() - none)) / 100);
        double best = guess;
        double bestOff = Double.MAX_VALUE;
        for (int d = -2; d <= 2; d++) {
            double scale = Math.rint(100 * guess + d) / 100;
            if (scale <= 0) {
                continue;
            }
            double off = Math.abs(
                    TypicalHaul.of(content, key, Difficulty.MEDIUM, scale).offBudget());
            if (off < bestOff) {
                bestOff = off;
                best = scale;
            }
        }
        return best;
    }

    /**
     * One autopilot run of a level test (e.g. {@link Level19Test#theAutopilotFliesTheLevel}): every attempt counted.
     *
     * @param complete whether the level was completed
     * @param retries the attempts after the first
     * @param secondaryMet whether the secondary objective was met
     * @param credits the credits earned (0 if not completed)
     * @param bosses the bosses' fights on the attempt that completed the level
     */
    record Run(boolean complete, int retries, boolean secondaryMet, int credits, List<BossFight> bosses) {
        static Run of(Sortie sortie) {
            List<BossFight> bosses = new ArrayList<>();
            for (int i = 0; i < sortie.setPieceCount(); i++) {
                SetPiece piece = sortie.setPiece(i);
                piece.boss()
                        .ifPresent(boss -> bosses.add(new BossFight(
                                piece.slug(),
                                boss,
                                piece.destroyed(),
                                piece.destroyed() ? piece.killSeconds() : piece.bossSeconds())));
            }
            boolean complete = sortie.complete();
            return new Run(
                    complete,
                    sortie.attempt() - 1,
                    complete && sortie.result().secondaryMet(),
                    complete ? sortie.result().credits().total() : 0,
                    bosses);
        }
    }

    /**
     * A boss's fight in a run: from its bar appearing to its kill (or to the run's end if it lives).
     *
     * @param slug the boss's slug
     * @param boss its spec: the par, mid-boss or act boss
     */
    record BossFight(String slug, BossSpec boss, boolean killed, double seconds) {
        boolean inWindow() {
            return killed && inBossWindow(boss.midBoss(), seconds);
        }
    }

    /** The autopilot's run of the level with {@code loadout} on {@code seed}, at most {@code maxSteps}. */
    static Run autopilot(Content content, String key, Difficulty difficulty, Loadout loadout, long seed, int maxSteps) {
        Sortie sortie = sortie(content, key, difficulty, loadout, seed);
        fly(sortie, maxSteps);
        return Run.of(sortie);
    }
}
