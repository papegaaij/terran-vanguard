package vanguard.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import vanguard.sim.BossSpec;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.HaltCycle;
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
        return sortie(content, key, difficulty, loadout, seed, RoutePlay.NONE);
    }

    /** M9 part E: a sortie of the level at full armour, flown on {@code route} ({@link RoutePlay#fly}). */
    static Sortie sortie(Content content, String key, Difficulty difficulty, Loadout loadout, long seed, String route) {
        return RoutePlay.fly(
                new Sortie(
                        seed,
                        loadout,
                        SimSpecs.level(content, key, difficulty),
                        SimSpecs.rules(content, key, difficulty),
                        loadout.plating().maxArmour()),
                route);
    }

    /** What a flight's watcher sees: each new attempt, then every step after it is flown. */
    interface Probe {
        /** A new attempt began this step (the level was retried): forget the last one. */
        default void newAttempt() {}

        /**
         * M9 part H (step H2): a new attempt restarted at the boss checkpoint (the approach before it unchanged): forget
         * the last one, as {@link #newAttempt()}, unless the probe measures only the approach.
         */
        default void bossRetry() {
            newAttempt();
        }

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
                // M9 part H: a retry from the boss checkpoint starts at the arena, past the launch.
                boolean fromBoss = sortie.levelSeconds() > BOSS_RETRY_SECONDS;
                for (Probe probe : probes) {
                    if (fromBoss) {
                        probe.bossRetry();
                    } else {
                        probe.newAttempt();
                    }
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

    /** M9 part H: a new attempt starting past this level time restarted at the boss checkpoint, s. */
    static final double BOSS_RETRY_SECONDS = 1;

    /**
     * {@link PacingTest}'s and {@link ActFloorTest}'s measure: see {@link Flight}. M9 part H (step H2): a retry from the
     * boss checkpoint keeps the approach it measured (the fight is outside it), so a lost fight still reads the approach.
     */
    static final class Pacing implements Probe {
        @Override
        public void bossRetry() {
            // The approach was flown on the attempt that took the checkpoint: keep it; the empty stretch open at the
            // loss ends there.
            if (emptySince >= 0) {
                stretches.add(new double[] {emptySince, realSeconds});
                emptySince = -1;
            }
        }

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
            if (sortie.flying() && !bossFight(sortie) && !wreck(sortie)) {
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
        private final Map<Integer, String> slugs = new HashMap<>(); // serial → its slug
        private final Map<String, long[]> bySlug = new TreeMap<>(); // slug → {killed, steps lived}
        private int killed;
        private long lived;

        SmallLife(Content content) {
            this.content = content;
        }

        @Override
        public void newAttempt() {
            entered.clear();
            slugs.clear();
            bySlug.clear();
            killed = 0;
            lived = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                EnemyData data = content.enemies().get(enemy.spec().slug()); // a chain's members have no stat block
                // M9 part B (Level 29): a neutral craft is no enemy; a dormant nest's life starts at its wake.
                if (data == null || data.tier() != Tier.SMALL || !enemy.spec().countsAsEnemy() || enemy.dormant()) {
                    continue;
                }
                boolean on = onScreen(
                        enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox());
                int[] track = entered.get(enemy.serial());
                if (track == null && on) {
                    entered.put(enemy.serial(), new int[] {step, step, 1});
                    slugs.put(enemy.serial(), enemy.spec().slug());
                } else if (track != null) {
                    track[1] = step;
                    track[2] = on ? 1 : 0;
                }
            }
            for (Iterator<Map.Entry<Integer, int[]>> it = entered.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Integer, int[]> entry = it.next();
                int[] track = entry.getValue();
                if (track[1] == step) {
                    continue;
                }
                // Gone this step: killed if it was on the screen, else it left.
                String slug = slugs.remove(entry.getKey());
                if (track[2] == 1) {
                    killed++;
                    lived += track[1] - track[0];
                    long[] unit = bySlug.computeIfAbsent(slug, k -> new long[2]);
                    unit[0]++;
                    unit[1] += track[1] - track[0];
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

        /** M9 part B (round 62): the same per unit (slug, by name): {killed, mean seconds on the screen}. */
        Map<String, double[]> bySlug() {
            Map<String, double[]> out = new TreeMap<>();
            bySlug.forEach((slug, unit) -> out.put(slug, new double[] {unit[0], unit[1] * SimStep.SECONDS / unit[0]}));
            return out;
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

    /**
     * M9 part D (step D2): a hull escort's flight on the attempt that completes the level (Level 31's mining tug): per
     * section its name, the HP share left and whether it is home (or lost), the damage it took; the hits by kind (the
     * missiles burst on a section and the links' touches counted, the dives what the rest of the damage makes at the
     * hull's dive damage); the rear Darts (a Dart born below the bottom edge) that crossed a section and the seconds
     * from their entry to the first crossing (the fewest and the mean; m9d §6's rule: at least 0.8 s).
     */
    record HullRun(
            String[] names,
            double[] share,
            boolean[] alive,
            double[] damage,
            int missiles,
            int links,
            double dives,
            int rearDarts,
            int rearCrossed,
            double rearFirst,
            double rearMean,
            int hornets,
            int hornetsKilled,
            double hornetLife) {
        /** The sections alive at the end (home). */
        int home() {
            int n = 0;
            for (boolean a : alive) {
                n += a ? 1 : 0;
            }
            return n;
        }

        /** Whether every section ends at half its HP or more (the CLEAN HULL secondary). */
        boolean clean() {
            for (double s : share) {
                if (s < 0.5) {
                    return false;
                }
            }
            return true;
        }
    }

    /** {@link HullRun}'s measure. */
    static final class HullWatch implements Probe {
        private final Map<Integer, Double> born = new HashMap<>();
        private final Map<Integer, Boolean> rear = new HashMap<>();
        private final Map<Integer, Double> crossed = new HashMap<>();
        /** A Hornet's serial → {its first level second on the screen, its last, 1 while on the screen}. */
        private final Map<Integer, double[]> hornets = new HashMap<>();

        private int hornetsSeen;
        private int hornetsKilled;
        private double hornetLived;
        private int missiles;
        private int links;
        private double[] damage = new double[0];
        private double[] last = new double[0];
        private String[] names = new String[0];
        private boolean[] alive = new boolean[0];

        @Override
        public void newAttempt() {
            born.clear();
            rear.clear();
            crossed.clear();
            hornets.clear();
            hornetsSeen = 0;
            hornetsKilled = 0;
            hornetLived = 0;
            missiles = 0;
            links = 0;
            damage = new double[0];
            last = new double[0];
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            if (!sortie.hullEscort()) {
                return;
            }
            int n = sortie.allyCount();
            if (damage.length != n) {
                damage = new double[n];
                last = new double[n];
                names = new String[n];
                alive = new boolean[n];
                for (int k = 0; k < n; k++) {
                    last[k] = 1;
                    names[k] = sortie.hullSection(k).name();
                }
            }
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.MISSILE_DESTROYED
                        && events.value(i) == vanguard.sim.Missiles.ON_ALLY) {
                    missiles++;
                } else if (events.type(i) == SimEvents.Type.AIR_LINK_TOUCH
                        && events.value(i) >= vanguard.sim.AirLinks.TOUCH_SECTION) {
                    links++;
                }
            }
            for (int k = 0; k < n; k++) {
                double share = sortie.ally(k).hpShare();
                if (share < last[k]) {
                    damage[k] += (last[k] - share) * sortie.hullSection(k).hp();
                }
                last[k] = share;
                alive[k] = sortie.ally(k).alive();
            }
            double now = sortie.levelSeconds();
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals("hornet")) {
                    continue;
                }
                boolean on = onScreen(
                        enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox());
                double[] track = hornets.get(enemy.serial());
                if (track == null && on) {
                    hornets.put(enemy.serial(), new double[] {now, now, 1});
                    hornetsSeen++;
                } else if (track != null) {
                    track[1] = now;
                    track[2] = on ? 1 : 0;
                }
            }
            for (var it = hornets.entrySet().iterator(); it.hasNext(); ) {
                double[] track = it.next().getValue();
                if (track[1] == now) {
                    continue;
                }
                if (track[2] == 1) {
                    hornetsKilled++;
                    hornetLived += track[1] - track[0];
                }
                it.remove();
            }
            double cx = sortie.hullX(1);
            double cy = sortie.hullY(1);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals("dart-drone")) {
                    continue;
                }
                double x = enemy.renderX(1);
                double y = enemy.renderY(1);
                // Its entry: its first step on the screen (a rear Dart enters below the bottom edge).
                if (!rear.containsKey(enemy.serial())) {
                    rear.put(enemy.serial(), y < 0);
                }
                if (!born.containsKey(enemy.serial())
                        && onScreen(x, y, enemy.spec().hitbox())) {
                    born.put(enemy.serial(), now);
                }
                if (!rear.get(enemy.serial())
                        || !born.containsKey(enemy.serial())
                        || crossed.containsKey(enemy.serial())) {
                    continue;
                }
                for (int k = 0; k < n; k++) {
                    var section = sortie.hullSection(k);
                    if (sortie.ally(k).alive()
                            && Math.abs(x - (cx + section.dx())) < section.box().width() / 2
                            && Math.abs(y - (cy + section.dy())) < section.box().height() / 2) {
                        crossed.put(enemy.serial(), now - born.get(enemy.serial()));
                        break;
                    }
                }
            }
        }

        HullRun run() {
            double total = 0;
            for (double d : damage) {
                total += d;
            }
            int rears = (int) rear.values().stream().filter(b -> b).count();
            return new HullRun(
                    names.clone(),
                    last.clone(),
                    alive.clone(),
                    damage.clone(),
                    missiles,
                    links,
                    Math.max(0, total - 5.0 * missiles - 4.0 * links) / 3,
                    rears,
                    crossed.size(),
                    crossed.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .min()
                            .orElse(Double.NaN),
                    crossed.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(Double.NaN),
                    hornetsSeen,
                    hornetsKilled,
                    hornetsKilled == 0 ? Double.NaN : hornetLived / hornetsKilled);
        }
    }

    /** Flies the autopilot as {@link #autopilot} and returns its hull escort's numbers. */
    static HullRun hull(Content content, String key, Difficulty difficulty, Loadout loadout, long seed, int steps) {
        HullWatch watch = new HullWatch();
        fly(sortie(content, key, difficulty, loadout, seed), steps, watch);
        return watch.run();
    }

    /** Whether the level has a hull escort (M9 part D, Level 31's mining tug). */
    static boolean hasHull(LevelScript level) {
        return level.objectives().escort().flatMap(escort -> escort.hull()).isPresent();
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
    /**
     * M9 part H (step H3): whether a ringed boss lies dead (Level 35's Iron Sovereign): the wreck after its kill is the
     * level's outro, outside the floor's means (Level 28's rule, there by its armour floor).
     */
    private static boolean wreck(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.destroyed() && piece.ring().isPresent()) {
                return true;
            }
        }
        return false;
    }

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
            // M9 part B (Level 29, Hold fire): a neutral craft is not an enemy.
            if (enemy.spec().countsAsEnemy()
                    && onScreen(enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                count++;
            }
        }
        return count;
    }

    private static boolean occupied(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            // M7 part B (round 36 Q.15): a Burrower counts as on the screen in every phase, buried too.
            // M9 part B: a neutral craft does not occupy the screen (Level 29's wavering Stormhawks).
            if (enemy.spec().countsAsEnemy()
                    && (enemy.burrowPhase() != Enemy.BurrowPhase.NONE
                            || onScreen(
                                    enemy.renderX(1),
                                    enemy.renderY(1),
                                    enemy.spec().hitbox()))) {
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
        return density(level, RoutePlay.NONE);
    }

    /**
     * M9 part E (Level 32): the density as {@code route} flies the level: only its branches' waves and units count
     * ({@link RoutePlay#NONE}: every branch).
     */
    static Density density(LevelScript level, String route) {
        double end = level.seconds();
        double start = 0;
        // M9 part B (Level 29, the rescue halt): a held arena does not end the count: the whole level and a typical
        // halt (one cycle of its halt waves, its length the cycle's last wave's time + 1 s) are counted.
        double halt = 0;
        for (Section section : level.sections()) {
            if (section.arena() && !level.heldArena()) {
                end = start;
                break;
            }
            start = section.end();
        }
        int waves = 0;
        List<WaveSpec> counted = new ArrayList<>(level.waves());
        if (level.heldArena() && level.haltCycle().isPresent()) {
            HaltCycle cycle = level.haltCycle().get();
            counted.addAll(cycle.waves());
            halt = typicalHalt(cycle);
        }
        for (WaveSpec wave : counted) {
            if (!RoutePlay.on(route, wave.branch())) {
                continue;
            }
            if (wave.t() < end && wave.enemy().countsAsEnemy()) {
                int released = wave.enemy().brood().map(EnemySpec.Brood::count).orElse(0)
                        + wave.enemy().spawner().map(EnemySpec.Spawner::count).orElse(0);
                waves += planned(wave) * (1 + released);
            }
        }
        int ground = 0;
        for (GroundUnit unit : level.groundUnits()) {
            if (!RoutePlay.on(route, unit.branch())) {
                continue;
            }
            if (unit.t() < end && unit.enemy().countsAsEnemy()) {
                // M5 part C: a periodic spawner (Level 09's Hive Nodes) counts one release
                ground +=
                        1 + unit.enemy().spawner().map(EnemySpec.Spawner::count).orElse(0);
            }
        }
        return new Density(waves, ground, (end - level.launchSeconds() + halt) / 60);
    }

    /**
     * M9 part B (design/campaign Level 29): a typical halt's length, s: one cycle of its waves, to a second past the
     * last one's start (the README's ≈ 14 s with its waves at H+1 … H+13).
     */
    static double typicalHalt(HaltCycle cycle) {
        return cycle.waves().stream().mapToDouble(WaveSpec::t).max().orElse(0) + 1;
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
        return autopilot(content, key, difficulty, loadout, seed, maxSteps, RoutePlay.NONE);
    }

    /** M9 part E: the same on {@code route} ({@link RoutePlay}). */
    static Run autopilot(
            Content content,
            String key,
            Difficulty difficulty,
            Loadout loadout,
            long seed,
            int maxSteps,
            String route) {
        Sortie sortie = sortie(content, key, difficulty, loadout, seed, route);
        fly(sortie, maxSteps);
        return Run.of(sortie);
    }
}
