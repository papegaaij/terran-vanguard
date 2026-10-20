package vanguard.content;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Sortie;

/**
 * A level's numbers for tuning its waves, computed by the campaign-wide tests' own code ({@link LevelMetrics}) with
 * their rules' verdicts: {@code ./gradlew :content:levelReport -Plevel=19 [-Pdifficulty=easy,medium,hard] [-Pseeds=3]
 * [-Pfit=test|plan|user] [-Pparts=pacing,floor,haul,boss,secondary,hp,pursuit,colossus] [-Pjson]}. It reads the design data at run time,
 * so a data.yaml edit needs no recompile; the flights run in parallel.
 *
 * <p>Per difficulty: the enemies (waves + ground) and their rate ({@link DensityTest}), the mean on the screen and the
 * enemy shots a minute, the empty gaps over 2 s and the pacing rule ({@link PacingTest}), the Act 2+ floor at medium
 * ({@link ActFloorTest}), the typical haul against the budget, the {@code bounty_scale} that would hit it and the
 * perfect run ({@link TypicalHaulTest}), the autopilot over N seeds: wins, retries, the secondary met and the bosses'
 * fights against their par and window (the level tests' runs), and the beam era's small-unit time on the screen
 * against Level 10's ({@link BeamEraHpTest}), and (M7 part G) a pursuit swarm's motes on the screen and lunges over the
 * same seeds' runs with the plan's fit ({@link LevelMetrics.PursuitWatch}), and (M7 part H) a breaching boss's phases, the
 * shield and armour lost in each, the bites and the eruptions that caught the ship ({@link LevelMetrics.ColossusWatch}). The fits: {@code test} (default) flies each measure with the fit its test
 * uses, {@code plan} and {@code user} fly every measure with the balance plan's or the user's Act 2 fit.
 */
final class LevelReport {
    /** The autopilot's longest run, steps (the Act 3 level tests'). */
    static final int AUTOPILOT_STEPS = 60 * 60 * 30;

    enum Part {
        PACING,
        FLOOR,
        HAUL,
        BOSS,
        SECONDARY,
        HP,
        PURSUIT,
        COLOSSUS
    }

    enum Fit {
        TEST,
        PLAN,
        USER
    }

    record Options(
            List<Integer> levels, List<Difficulty> difficulties, int seeds, Fit fit, Set<Part> parts, boolean json) {
        static Options parse(String... args) {
            List<Integer> levels = new ArrayList<>();
            List<Difficulty> difficulties = List.of(Difficulty.values());
            int seeds = 3;
            Fit fit = Fit.TEST;
            Set<Part> parts = EnumSet.allOf(Part.class);
            boolean json = false;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--json" -> json = true;
                    case "--level" -> {
                        for (String n : args[++i].split(",")) {
                            levels.add(Integer.parseInt(n.strip()));
                        }
                    }
                    case "--difficulty" -> {
                        List<Difficulty> list = new ArrayList<>();
                        for (String d : args[++i].split(",")) {
                            list.add(Difficulty.valueOf(d.strip().toUpperCase(Locale.ROOT)));
                        }
                        difficulties = list;
                    }
                    case "--seeds" -> seeds = Integer.parseInt(args[++i]);
                    case "--fit" -> fit = Fit.valueOf(args[++i].toUpperCase(Locale.ROOT));
                    case "--parts" -> {
                        parts = EnumSet.noneOf(Part.class);
                        for (String p : args[++i].split(",")) {
                            parts.add(Part.valueOf(p.strip().toUpperCase(Locale.ROOT)));
                        }
                    }
                    default -> throw new IllegalArgumentException("unknown argument: " + arg);
                }
            }
            if (levels.isEmpty()) {
                throw new IllegalArgumentException("no level: -Plevel=<n>[,<n>...]");
            }
            return new Options(List.copyOf(levels), List.copyOf(difficulties), seeds, fit, Set.copyOf(parts), json);
        }

        boolean has(Part part) {
            return parts.contains(part);
        }
    }

    /** One flight on {@link LevelMetrics#SEED}: what the player saw, and the small units' life if it was watched. */
    record Measured(String fit, boolean complete, LevelMetrics.Flight flight, double smallLife, int smallKilled) {}

    /** The pacing rule as the level's {@link PacingTest} applies it at a difficulty. */
    record PacingRule(boolean applies, boolean strict, double limit, int allowed) {}

    /** A level at one difficulty. */
    record ByDifficulty(
            Difficulty difficulty,
            LevelMetrics.Density density,
            int densityMinimum,
            Measured pacing,
            PacingRule rule,
            Measured floor,
            TypicalHaul haul,
            List<LevelMetrics.Run> runs,
            List<LevelMetrics.PursuitRun> pursuits,
            List<LevelMetrics.ColossusRun> colossi) {}

    /** A level's report. */
    record Level(
            String key,
            int number,
            int act,
            String fit,
            int seeds,
            Set<Part> parts,
            double bountyScale,
            double fittedBountyScale,
            boolean floorApplies,
            boolean hpApplies,
            double referenceSmallLife,
            List<ByDifficulty> difficulties) {}

    private LevelReport() {}

    public static void main(String[] args) throws Exception {
        Options options = Options.parse(args);
        long start = System.nanoTime();
        Content content = LevelMetrics.CONTENT;
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            List<Level> levels = new ArrayList<>();
            for (int number : options.levels()) {
                String key =
                        content.levelKey(number).orElseThrow(() -> new IllegalArgumentException("no level " + number));
                levels.add(measure(content, key, options, pool));
            }
            if (options.json()) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (Level level : levels) {
                    list.add(json(level));
                }
                System.out.println(JsonMapper.builder()
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .build()
                        .writeValueAsString(levels.size() == 1 ? list.getFirst() : list));
            } else {
                for (Level level : levels) {
                    System.out.print(text(level));
                }
                System.out.printf(Locale.ROOT, "(%.1f s)%n", (System.nanoTime() - start) / 1e9);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** Measures the level: its flights and runs in parallel on {@code pool}. */
    static Level measure(Content content, String key, Options options, ExecutorService pool)
            throws InterruptedException, ExecutionException {
        int number = Content.levelNumber(key);
        boolean wantFlights = options.has(Part.PACING) || options.has(Part.FLOOR) || options.has(Part.HP);
        boolean wantRuns = options.has(Part.BOSS) || options.has(Part.SECONDARY);
        String floorFit = options.fit() == Fit.USER ? "user" : "plan";

        // The flights on the campaign tests' seed, one per difficulty and fit: the pacing flight, and at medium the
        // floor's and the beam era's (the plan's fit) with it when the fits agree.
        Map<String, Future<Measured>> flights = new HashMap<>();
        Map<Difficulty, String> pacingFits = new HashMap<>();
        for (Difficulty difficulty : options.difficulties()) {
            if (!wantFlights) {
                break;
            }
            String fit = pacingFit(key, difficulty, options.fit());
            pacingFits.put(difficulty, fit);
            boolean medium = difficulty == Difficulty.MEDIUM;
            if (options.has(Part.PACING) || (medium && fit.equals(floorFit))) {
                flights.put(
                        difficulty + "/" + fit,
                        pool.submit(() -> flight(content, key, difficulty, fit, medium && fit.equals(floorFit))));
            }
            if (medium && !fit.equals(floorFit) && (options.has(Part.FLOOR) || options.has(Part.HP))) {
                flights.put(
                        difficulty + "/" + floorFit,
                        pool.submit(() -> flight(content, key, difficulty, floorFit, true)));
            }
        }
        String reference = content.levelKey(BeamEraHpTest.REFERENCE_LEVEL).orElseThrow();
        boolean hpRun = options.has(Part.HP) && options.difficulties().contains(Difficulty.MEDIUM);
        Future<Measured> referenceFlight = hpRun && !reference.equals(key)
                ? pool.submit(() -> flight(content, reference, Difficulty.MEDIUM, "plan", true))
                : null;

        // The autopilot runs, seeds 1..N per difficulty (the level tests').
        Map<Difficulty, List<Future<LevelMetrics.Run>>> runs = new HashMap<>();
        for (Difficulty difficulty : options.difficulties()) {
            List<Future<LevelMetrics.Run>> list = new ArrayList<>();
            for (long seed = 1; wantRuns && seed <= options.seeds(); seed++) {
                long s = seed;
                list.add(pool.submit(() -> LevelMetrics.autopilot(
                        content, key, difficulty, loadout(content, key, difficulty, floorFit), s, AUTOPILOT_STEPS)));
            }
            runs.put(difficulty, list);
        }
        // M7 part G: a pursuit swarm's motes and lunges, the same seeds with the plan's fit.
        Map<Difficulty, List<Future<LevelMetrics.PursuitRun>>> pursuits = new HashMap<>();
        for (Difficulty difficulty : options.difficulties()) {
            List<Future<LevelMetrics.PursuitRun>> list = new ArrayList<>();
            boolean chased =
                    options.has(Part.PURSUIT) && LevelMetrics.hasPursuit(SimSpecs.level(content, key, difficulty));
            for (long seed = 1; chased && seed <= options.seeds(); seed++) {
                long s = seed;
                list.add(pool.submit(() -> LevelMetrics.pursuit(
                        content, key, difficulty, loadout(content, key, difficulty, floorFit), s, AUTOPILOT_STEPS)));
            }
            pursuits.put(difficulty, list);
        }

        // M7 part H: a breaching boss's phases, losses, bites and eruptions, the same seeds with the plan's fit.
        Map<Difficulty, List<Future<LevelMetrics.ColossusRun>>> colossi = new HashMap<>();
        for (Difficulty difficulty : options.difficulties()) {
            List<Future<LevelMetrics.ColossusRun>> list = new ArrayList<>();
            boolean breaching =
                    options.has(Part.COLOSSUS) && LevelMetrics.hasBreach(SimSpecs.level(content, key, difficulty));
            for (long seed = 1; breaching && seed <= options.seeds(); seed++) {
                long s = seed;
                list.add(pool.submit(() -> LevelMetrics.colossus(
                        content, key, difficulty, loadout(content, key, difficulty, floorFit), s, AUTOPILOT_STEPS)));
            }
            colossi.put(difficulty, list);
        }

        double fitted = options.has(Part.HAUL) ? LevelMetrics.bountyScaleForBudget(content, key) : Double.NaN;
        List<ByDifficulty> rows = new ArrayList<>();
        for (Difficulty difficulty : options.difficulties()) {
            LevelScript level = SimSpecs.level(content, key, difficulty);
            String fit = pacingFits.get(difficulty);
            Future<Measured> pacing = fit == null ? null : flights.get(difficulty + "/" + fit);
            Future<Measured> floor = difficulty == Difficulty.MEDIUM ? flights.get(difficulty + "/" + floorFit) : null;
            boolean strict = PacingTest.strict(key, difficulty);
            List<LevelMetrics.Run> done = new ArrayList<>();
            for (Future<LevelMetrics.Run> run : runs.get(difficulty)) {
                done.add(run.get());
            }
            List<LevelMetrics.PursuitRun> chases = new ArrayList<>();
            for (Future<LevelMetrics.PursuitRun> chase : pursuits.get(difficulty)) {
                chases.add(chase.get());
            }
            List<LevelMetrics.ColossusRun> fights = new ArrayList<>();
            for (Future<LevelMetrics.ColossusRun> fight : colossi.get(difficulty)) {
                fights.add(fight.get());
            }
            rows.add(new ByDifficulty(
                    difficulty,
                    LevelMetrics.density(level),
                    LevelMetrics.densityMinimum(level),
                    pacing == null || !options.has(Part.PACING) ? null : pacing.get(),
                    new PacingRule(
                            number >= PacingTest.FIRST_LEVEL,
                            strict,
                            strict ? PacingTest.ACT_TWO_EMPTY_SECONDS : PacingTest.EMPTY_SECONDS,
                            strict ? PacingTest.ACT_TWO_LONG_PAUSES : PacingTest.LONG_PAUSES),
                    floor == null ? null : floor.get(),
                    options.has(Part.HAUL) ? TypicalHaul.of(content, key, difficulty) : null,
                    done,
                    chases,
                    fights));
        }
        double referenceLife = Double.NaN;
        if (hpRun) {
            referenceLife = referenceFlight != null
                    ? referenceFlight.get().smallLife()
                    : rows.stream()
                            .filter(row -> row.difficulty() == Difficulty.MEDIUM)
                            .findFirst()
                            .map(row -> row.floor().smallLife())
                            .orElse(Double.NaN);
        }
        return new Level(
                key,
                number,
                SimSpecs.level(content, key, Difficulty.MEDIUM).act(),
                options.fit().name().toLowerCase(Locale.ROOT),
                options.seeds(),
                options.parts(),
                content.level(key).bounties(),
                fitted,
                number >= ActFloorTest.FIRST_LEVEL && !ActFloorTest.PENDING.contains(key),
                number >= content.enemyBasis().beamEraHp().fromLevel()
                        && !PacingTest.PENDING_ACT_TWO_RULE.contains(key),
                referenceLife,
                rows);
    }

    /** The fit the pacing flight uses: {@link PacingTest}'s (the strong fit where the Act 2 rule applies), or one. */
    static String pacingFit(String key, Difficulty difficulty, Fit fit) {
        return switch (fit) {
            case TEST -> PacingTest.strict(key, difficulty) && PacingTest.userFitLevel(key) ? "user" : "plan";
            case PLAN -> "plan";
            case USER -> "user";
        };
    }

    static Loadout loadout(Content content, String key, Difficulty difficulty, String fit) {
        return fit.equals("user")
                ? PacingTest.userLoadout(content, difficulty)
                : PacingTest.planLoadout(key, difficulty);
    }

    /** One flight on the campaign tests' seed with the pacing probe and, if asked, the small units' life. */
    static Measured flight(Content content, String key, Difficulty difficulty, String fit, boolean smallLife) {
        Sortie sortie = LevelMetrics.sortie(
                content, key, difficulty, loadout(content, key, difficulty, fit), LevelMetrics.SEED);
        LevelMetrics.Pacing pacing = new LevelMetrics.Pacing();
        LevelMetrics.SmallLife life = new LevelMetrics.SmallLife(content);
        boolean complete = smallLife
                ? LevelMetrics.fly(sortie, PacingTest.MAX_STEPS, pacing, life)
                : LevelMetrics.fly(sortie, PacingTest.MAX_STEPS, pacing);
        return new Measured(
                fit, complete, pacing.flight(), smallLife ? life.seconds() : Double.NaN, smallLife ? life.killed() : 0);
    }

    // ---- the text report ----

    static String text(Level level) {
        StringBuilder out = new StringBuilder();
        out.append(String.format(
                Locale.ROOT,
                "Level %02d %s (Act %d), fit %s, %d autopilot seed%s%n",
                level.number(),
                Content.levelName(level.key()),
                level.act(),
                level.fit(),
                level.seeds(),
                level.seeds() == 1 ? "" : "s"));
        for (ByDifficulty row : level.difficulties()) {
            boolean medium = row.difficulty() == Difficulty.MEDIUM;
            out.append("== ")
                    .append(row.difficulty().name().toLowerCase(Locale.ROOT))
                    .append(" ==\n");
            LevelMetrics.Density density = row.density();
            if (level.parts().contains(Part.PACING)) {
                line(
                        out,
                        "enemies",
                        "%d = %d waves + %d ground, %.1f a minute%s",
                        density.enemies(),
                        density.waves(),
                        density.ground(),
                        density.perMinute(),
                        medium
                                ? String.format(
                                        Locale.ROOT,
                                        " (minimum %d: %s)",
                                        row.densityMinimum(),
                                        verdict(density.perMinute() >= row.densityMinimum()))
                                : "");
            }
            Measured pacing = row.pacing();
            if (pacing != null) {
                LevelMetrics.Flight flight = pacing.flight();
                line(
                        out,
                        "screen",
                        "%.2f on the screen, %.1f shots a minute [%s fit]%s",
                        flight.meanOnScreen(),
                        flight.shotsPerMinute(),
                        pacing.fit(),
                        pacing.complete() ? "" : " NOT COMPLETED");
                List<double[]> gaps = flight.pausesOver(PacingTest.ACT_TWO_EMPTY_SECONDS);
                PacingRule rule = row.rule();
                long over = gaps.stream()
                        .filter(gap -> gap[1] - gap[0] > rule.limit())
                        .count();
                StringBuilder times = new StringBuilder();
                for (double[] gap : gaps) {
                    times.append(String.format(Locale.ROOT, " %.1f-%.1f (%.1f)", gap[0], gap[1], gap[1] - gap[0]));
                }
                line(
                        out,
                        "gaps > 2 s",
                        "%d%s:%s",
                        gaps.size(),
                        rule.applies()
                                ? String.format(
                                        Locale.ROOT,
                                        " (%s rule: %d over %.0f s, at most %d: %s)",
                                        rule.strict() ? "Act 2+" : "Act 1",
                                        over,
                                        rule.limit(),
                                        rule.allowed(),
                                        verdict(over <= rule.allowed()))
                                : " (warm-up: no rule)",
                        times.isEmpty() ? " none" : times.toString());
            }
            if (medium && row.floor() != null && level.parts().contains(Part.FLOOR)) {
                LevelMetrics.Flight floor = row.floor().flight();
                line(
                        out,
                        "floor",
                        "%.2f on the screen (at least %.1f), %.1f shots a minute (at least %.0f) [%s fit]: %s",
                        floor.meanOnScreen(),
                        ActFloorTest.MEAN_ON_SCREEN,
                        floor.shotsPerMinute(),
                        ActFloorTest.SHOTS_PER_MINUTE,
                        row.floor().fit(),
                        level.floorApplies() ? verdict(floorMet(floor)) : "no rule");
            }
            TypicalHaul haul = row.haul();
            if (haul != null) {
                line(
                        out,
                        "haul",
                        "typical %.0f vs budget %.0f (%+.1f %%%s), perfect %d (%.2f× budget%s)%s",
                        haul.typical(),
                        haul.budget(),
                        100 * haul.offBudget(),
                        medium ? ": " + verdict(Math.abs(haul.offBudget()) <= TypicalHaulTest.TOLERANCE) : "",
                        haul.perfect(),
                        haul.perfect() / haul.budget(),
                        medium ? ": " + verdict(haul.perfect() >= TypicalHaulTest.PERFECT_BUDGETS * haul.budget()) : "",
                        medium
                                ? String.format(
                                        Locale.ROOT,
                                        "; bounty_scale %.2f, %.2f hits the budget",
                                        level.bountyScale(),
                                        level.fittedBountyScale())
                                : "");
            }
            if (!row.runs().isEmpty()) {
                List<LevelMetrics.Run> runs = row.runs();
                int n = runs.size();
                long wins = runs.stream().filter(LevelMetrics.Run::complete).count();
                int retries = runs.stream().mapToInt(LevelMetrics.Run::retries).sum();
                long met = runs.stream().filter(LevelMetrics.Run::secondaryMet).count();
                double credits = runs.stream()
                        .mapToInt(LevelMetrics.Run::credits)
                        .average()
                        .orElse(0);
                line(
                        out,
                        "autopilot",
                        "won %d/%d, retries %d, secondary met %d/%d, credits %.0f a run",
                        wins,
                        n,
                        retries,
                        met,
                        n,
                        credits);
                if (level.parts().contains(Part.BOSS)) {
                    for (BossSummary boss : bosses(runs)) {
                        line(
                                out,
                                "boss",
                                "%s %.1f s (%.1f-%.1f, killed %d/%d), par %.0f, %s window %.0f-%.0f: %d/%d in%s",
                                boss.slug(),
                                boss.mean(),
                                boss.least(),
                                boss.most(),
                                boss.killed(),
                                boss.fights(),
                                boss.par(),
                                boss.midBoss() ? "mid-boss" : "act-boss",
                                LevelMetrics.bossWindow(boss.midBoss())[0],
                                LevelMetrics.bossWindow(boss.midBoss())[1],
                                boss.inWindow(),
                                boss.fights(),
                                medium ? ": " + verdict(boss.inWindow() == boss.fights()) : "");
                    }
                }
            }
            if (!row.pursuits().isEmpty()) {
                List<LevelMetrics.PursuitRun> chases = row.pursuits();
                StringBuilder lunges = new StringBuilder();
                for (LevelMetrics.PursuitRun chase : chases) {
                    lunges.append(lunges.isEmpty() ? "" : " ")
                            .append(chase.lungesWithMotes())
                            .append('/')
                            .append(chase.lunges());
                }
                line(
                        out,
                        "pursuit",
                        "%.2f motes on the screen over %.0f s; lunges with motes/all a run: %s [plan fit]",
                        chases.stream()
                                .mapToDouble(LevelMetrics.PursuitRun::meanMotes)
                                .average()
                                .orElse(0),
                        chases.stream()
                                .mapToDouble(LevelMetrics.PursuitRun::seconds)
                                .average()
                                .orElse(0),
                        lunges);
            }
            if (!row.colossi().isEmpty()) {
                List<LevelMetrics.ColossusRun> fights = row.colossi();
                int n = fights.size();
                double[] seconds = new double[3];
                double[] armour = new double[3];
                double[] lost = new double[3];
                int bites = 0;
                int eruptions = 0;
                double breaches = 0;
                for (LevelMetrics.ColossusRun fight : fights) {
                    for (int p = 0; p < 3; p++) {
                        seconds[p] += fight.phaseSeconds()[p] / n;
                        armour[p] += fight.armourLost()[p] / n;
                        lost[p] += fight.damage()[p] / n;
                    }
                    bites += fight.bites();
                    eruptions += fight.eruptions();
                    breaches += fight.breaches() / (double) n;
                }
                line(
                        out,
                        "colossus",
                        "phases %.1f + %.1f + %.1f s, shield+armour lost %.0f / %.0f / %.0f (armour %.0f / %.0f / %.0f),"
                                + " %.1f breaches, bites %d, eruptions %d over %d runs [plan fit]",
                        seconds[0],
                        seconds[1],
                        seconds[2],
                        lost[0],
                        lost[1],
                        lost[2],
                        armour[0],
                        armour[1],
                        armour[2],
                        breaches,
                        bites,
                        eruptions,
                        n);
            }
            if (medium && level.parts().contains(Part.HP) && row.floor() != null) {
                double life = row.floor().smallLife();
                double ratio = life / level.referenceSmallLife();
                line(
                        out,
                        "small TTK",
                        "%.2f s on the screen (%d killed) vs Level %02d %.2f s = %.2f× (%.1f-%.1f: %s)",
                        life,
                        row.floor().smallKilled(),
                        BeamEraHpTest.REFERENCE_LEVEL,
                        level.referenceSmallLife(),
                        ratio,
                        BeamEraHpTest.LEAST,
                        BeamEraHpTest.most(level.key()),
                        level.hpApplies()
                                ? verdict(ratio >= BeamEraHpTest.LEAST && ratio <= BeamEraHpTest.most(level.key()))
                                : "before the beam era, no rule");
            }
        }
        return out.toString();
    }

    static boolean floorMet(LevelMetrics.Flight floor) {
        return floor.meanOnScreen() >= ActFloorTest.MEAN_ON_SCREEN
                && floor.shotsPerMinute() >= ActFloorTest.SHOTS_PER_MINUTE;
    }

    private static void line(StringBuilder out, String label, String format, Object... args) {
        out.append(String.format(Locale.ROOT, "  %-11s " + format + "%n", prepend(label, args)));
    }

    private static Object[] prepend(String label, Object[] args) {
        Object[] all = new Object[args.length + 1];
        all[0] = label;
        System.arraycopy(args, 0, all, 1, args.length);
        return all;
    }

    private static String verdict(boolean met) {
        return met ? "ok" : "FAIL";
    }

    /** A boss's fights over the runs. */
    record BossSummary(
            String slug,
            boolean midBoss,
            double par,
            int fights,
            int killed,
            int inWindow,
            double mean,
            double least,
            double most) {}

    static List<BossSummary> bosses(List<LevelMetrics.Run> runs) {
        Map<String, List<LevelMetrics.BossFight>> bySlug = new LinkedHashMap<>();
        for (LevelMetrics.Run run : runs) {
            for (LevelMetrics.BossFight fight : run.bosses()) {
                bySlug.computeIfAbsent(fight.slug(), slug -> new ArrayList<>()).add(fight);
            }
        }
        List<BossSummary> list = new ArrayList<>();
        bySlug.forEach((slug, fights) -> {
            var boss = fights.getFirst().boss();
            list.add(new BossSummary(
                    slug,
                    boss.midBoss(),
                    boss.parSeconds(),
                    fights.size(),
                    (int) fights.stream().filter(LevelMetrics.BossFight::killed).count(),
                    (int) fights.stream()
                            .filter(LevelMetrics.BossFight::inWindow)
                            .count(),
                    fights.stream()
                            .mapToDouble(LevelMetrics.BossFight::seconds)
                            .average()
                            .orElse(0),
                    fights.stream()
                            .mapToDouble(LevelMetrics.BossFight::seconds)
                            .min()
                            .orElse(0),
                    fights.stream()
                            .mapToDouble(LevelMetrics.BossFight::seconds)
                            .max()
                            .orElse(0)));
        });
        return list;
    }

    // ---- JSON for scripts ----

    static Map<String, Object> json(Level level) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("level", level.number());
        out.put("key", level.key());
        out.put("act", level.act());
        out.put("fit", level.fit());
        out.put("seeds", level.seeds());
        out.put("bounty_scale", level.bountyScale());
        out.put("fitted_bounty_scale", number(level.fittedBountyScale()));
        out.put("reference_small_life", number(level.referenceSmallLife()));
        Map<String, Object> byDifficulty = new LinkedHashMap<>();
        for (ByDifficulty row : level.difficulties()) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put(
                    "density",
                    ordered(
                            "waves", row.density().waves(),
                            "ground", row.density().ground(),
                            "per_minute", round(row.density().perMinute()),
                            "minimum", row.densityMinimum()));
            if (row.pacing() != null) {
                d.put("pacing", flightJson(row.pacing()));
                PacingRule rule = row.rule();
                d.put(
                        "pacing_rule",
                        ordered(
                                "applies", rule.applies(),
                                "strict", rule.strict(),
                                "limit", rule.limit(),
                                "allowed", rule.allowed(),
                                "over",
                                        row.pacing()
                                                .flight()
                                                .pausesOver(rule.limit())
                                                .size()));
            }
            if (row.floor() != null) {
                Map<String, Object> floor = flightJson(row.floor());
                floor.put("applies", level.floorApplies());
                floor.put("met", floorMet(row.floor().flight()));
                d.put("floor", floor);
                if (level.parts().contains(Part.HP)) {
                    d.put(
                            "small_life",
                            ordered(
                                    "seconds", number(row.floor().smallLife()),
                                    "killed", row.floor().smallKilled(),
                                    "ratio", number(row.floor().smallLife() / level.referenceSmallLife()),
                                    "applies", level.hpApplies()));
                }
            }
            if (row.haul() != null) {
                d.put(
                        "haul",
                        ordered(
                                "typical", round(row.haul().typical()),
                                "budget", round(row.haul().budget()),
                                "off_budget", round(row.haul().offBudget()),
                                "perfect", row.haul().perfect()));
            }
            if (!row.runs().isEmpty()) {
                List<Object> runs = new ArrayList<>();
                for (LevelMetrics.Run run : row.runs()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("complete", run.complete());
                    r.put("retries", run.retries());
                    r.put("secondary_met", run.secondaryMet());
                    r.put("credits", run.credits());
                    List<Object> bosses = new ArrayList<>();
                    for (LevelMetrics.BossFight fight : run.bosses()) {
                        bosses.add(ordered(
                                "slug", fight.slug(),
                                "killed", fight.killed(),
                                "seconds", round(fight.seconds()),
                                "par", fight.boss().parSeconds(),
                                "mid_boss", fight.boss().midBoss(),
                                "in_window", fight.inWindow()));
                    }
                    r.put("bosses", bosses);
                    runs.add(r);
                }
                d.put("autopilot", runs);
            }
            if (!row.pursuits().isEmpty()) {
                List<Object> chases = new ArrayList<>();
                for (LevelMetrics.PursuitRun chase : row.pursuits()) {
                    chases.add(ordered(
                            "seconds", round(chase.seconds()),
                            "mean_motes", round(chase.meanMotes()),
                            "lunges", chase.lunges(),
                            "lunges_with_motes", chase.lungesWithMotes()));
                }
                d.put("pursuit", chases);
            }
            if (!row.colossi().isEmpty()) {
                List<Object> fights = new ArrayList<>();
                for (LevelMetrics.ColossusRun fight : row.colossi()) {
                    fights.add(ordered(
                            "phase_seconds",
                                    List.of(
                                            round(fight.phaseSeconds()[0]),
                                            round(fight.phaseSeconds()[1]),
                                            round(fight.phaseSeconds()[2])),
                            "lost",
                                    List.of(
                                            round(fight.damage()[0]),
                                            round(fight.damage()[1]),
                                            round(fight.damage()[2])),
                            "armour_lost",
                                    List.of(
                                            round(fight.armourLost()[0]),
                                            round(fight.armourLost()[1]),
                                            round(fight.armourLost()[2])),
                            "breaches", fight.breaches(),
                            "bites", fight.bites(),
                            "eruptions", fight.eruptions()));
                }
                d.put("colossus", fights);
            }
            byDifficulty.put(row.difficulty().name().toLowerCase(Locale.ROOT), d);
        }
        out.put("difficulties", byDifficulty);
        return out;
    }

    private static Map<String, Object> flightJson(Measured measured) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fit", measured.fit());
        out.put("complete", measured.complete());
        out.put("mean_on_screen", round(measured.flight().meanOnScreen()));
        out.put("shots_per_minute", round(measured.flight().shotsPerMinute()));
        List<Object> gaps = new ArrayList<>();
        for (double[] gap : measured.flight().pausesOver(PacingTest.ACT_TWO_EMPTY_SECONDS)) {
            gaps.add(List.of(round(gap[0]), round(gap[1])));
        }
        out.put("gaps_over_2s", gaps);
        return out;
    }

    /** A map of {@code key, value, ...} in their order. */
    private static Map<String, Object> ordered(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    private static Object number(double value) {
        return Double.isFinite(value) ? round(value) : "n/a";
    }

    private static double round(double value) {
        return Math.rint(value * 1000) / 1000;
    }
}
