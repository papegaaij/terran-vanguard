package vanguard.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M10 part A, step A2b's static checks (design/world/jovian, Hazards &amp; set pieces; design/enemies/ground/shield-pylon,
 * The pylon gates; design/enemies, The three-way war): a level's authoring of the Jovian hazards, the numbers being the
 * world's ({@link JovianData}).
 *
 * <p>The {@code weather.strikes}: inside the level; on every difficulty never two tells within the world's {@code
 * spacing} (1.5 s) and no tell within the world's {@code entry} (1 s) of a wave's entry (the simulation also moves a
 * column off the ship's x near one: the ship's x is not known here); a {@code track} strike is hard's (skipped on easy
 * and medium). The {@code eruptions}: inside the level; the level's first eruption plays alone (Level 27's teaching
 * rule): no wave enters from its vent's earliest tell to the end of its latest one. The {@code backdrop.banks}: at most
 * the world's share of the play field under banks at a time (a third, easy a quarter) and never a bank over the ship's
 * band (the bottom 140 px) longer than 2 s, each bank drifting with the {@code high-air} layer's factor. The held gates
 * (several {@code arena: true, holds: <group>} sections, Level 41): apart (never two in a row, never the first
 * section), each group its own, its holders entering after the gate before it ends (a holder holds the first gate
 * ending after its entry), no escort swap with more than one. The {@code turn}: inside the level, only with a hunting
 * wave ({@code hunts: <tag>}).
 */
final class JovianHazardChecks {
    /** s between the samples of the banks' cover and band checks. */
    static final double SAMPLE_SECONDS = 0.05;

    private JovianHazardChecks() {}

    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        strikes(content, level, problem);
        eruptions(content, level, problem);
        banks(content, level, problem);
        gates(level, problem);
        turn(level, problem);
    }

    private static void inside(LevelData level, String field, double t, BiConsumer<String, String> problem) {
        if (t < 0 || t >= level.seconds()) {
            problem.accept(field, "t=" + t + " lies outside the level (0–" + level.seconds() + " s)");
        }
    }

    /** The entry times of the waves flown on {@code difficulty}. */
    private static List<Double> entries(LevelData level, Difficulty difficulty) {
        return level.waves().stream()
                .filter(wave -> wave.fliesOn(difficulty))
                .map(LevelData.Wave::t)
                .toList();
    }

    private static void strikes(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.StrikeAt> all =
                level.weather().flatMap(LevelData.Weather::strikes).orElse(List.of());
        if (all.isEmpty()) {
            return;
        }
        JovianData.Strike numbers = content.jovian().strike();
        for (int i = 0; i < all.size(); i++) {
            LevelData.StrikeAt strike = all.get(i);
            String field = "weather.strikes[" + i + "]";
            inside(level, field + ".t", strike.t(), problem);
            if (strike.tracks() && (strike.strikesOn(Difficulty.EASY) || strike.strikesOn(Difficulty.MEDIUM))) {
                problem.accept(
                        field + ".track",
                        "a tracking column is hard's: skip it on easy and medium (skip: [easy, medium])");
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            List<LevelData.StrikeAt> on = JovianHazardRules.strikesOn(level, difficulty);
            for (int i = 1; i < on.size(); i++) {
                double gap = on.get(i).t() - on.get(i - 1).t();
                if (gap < numbers.spacing() - 1e-9) {
                    problem.accept(
                            "weather.strikes",
                            "never two strikes within " + numbers.spacing() + " s ("
                                    + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ": t="
                                    + on.get(i - 1).t() + " and t=" + on.get(i).t() + ")");
                }
            }
            List<Double> entries = entries(level, difficulty);
            for (LevelData.StrikeAt strike : on) {
                entries.stream()
                        .filter(entry -> Math.abs(entry - strike.t()) < numbers.entry() - 1e-9)
                        .findFirst()
                        .ifPresent(entry -> problem.accept(
                                "weather.strikes",
                                "no tell within " + numbers.entry() + " s of a wave's entry ("
                                        + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ": the strike at t="
                                        + strike.t() + ", a wave at t=" + entry + ")"));
            }
        }
    }

    private static void eruptions(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.EruptionVent> vents = level.eruptions().orElse(List.of());
        if (vents.isEmpty()) {
            return;
        }
        for (int i = 0; i < vents.size(); i++) {
            inside(level, "eruptions[" + i + "].t", vents.get(i).t(), problem);
        }
        JovianData.Eruption numbers = content.jovian().eruption();
        LevelData.EruptionVent first = vents.getFirst();
        Span period = first.period().orElse(numbers.period());
        for (Difficulty difficulty : Difficulty.values()) {
            double from = first.t() + period.min();
            double to = first.t() + period.max() + numbers.tellOn(difficulty);
            entries(level, difficulty).stream()
                    .filter(entry -> entry >= from && entry <= to)
                    .findFirst()
                    .ifPresent(entry -> problem.accept(
                            "eruptions[0]",
                            "the level's first eruption plays alone: no wave enters from its earliest tell to the end"
                                    + " of its latest (" + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ": t="
                                    + from + "–" + to
                                    + ", a wave at t=" + entry + ")"));
        }
    }

    /**
     * Where a bank lies at {@code at} s: its rectangle {@code [left, bottom, right, top]} in px from the play field's
     * bottom left, drifting down with the {@code high-air} factor from the middle of the screen at its time.
     */
    static double[] bankAt(LevelData level, BackdropData.Bank bank, double at) {
        double factor = level.backdrop().factor(BackdropLayer.HIGH_AIR);
        double centre = PlayField.HEIGHT / 2.0 + factor * (level.scrollAt(bank.t()) - level.scrollAt(at));
        return new double[] {
            bank.x() - bank.w() / 2, centre - bank.h() / 2, bank.x() + bank.w() / 2, centre + bank.h() / 2
        };
    }

    /** The area of the union of {@code boxes} ({@code [left, bottom, right, top]}) inside the play field, px². */
    static double coveredArea(List<double[]> boxes) {
        List<double[]> clipped = new ArrayList<>();
        Set<Double> cuts = new HashSet<>();
        for (double[] box : boxes) {
            double[] c = {
                Math.max(0, box[0]),
                Math.max(0, box[1]),
                Math.min(PlayField.WIDTH, box[2]),
                Math.min(PlayField.HEIGHT, box[3])
            };
            if (c[2] > c[0] && c[3] > c[1]) {
                clipped.add(c);
                cuts.add(c[1]);
                cuts.add(c[3]);
            }
        }
        List<Double> ys = cuts.stream().sorted().toList();
        double area = 0;
        for (int i = 1; i < ys.size(); i++) {
            double y0 = ys.get(i - 1);
            double y1 = ys.get(i);
            List<double[]> spans = clipped.stream()
                    .filter(c -> c[1] <= y0 && c[3] >= y1)
                    .map(c -> new double[] {c[0], c[2]})
                    .sorted((a, b) -> Double.compare(a[0], b[0]))
                    .toList();
            double length = 0;
            double start = 0;
            double end = 0;
            for (double[] span : spans) {
                if (span[0] > end) {
                    length += end - start;
                    start = span[0];
                    end = span[1];
                } else {
                    end = Math.max(end, span[1]);
                }
            }
            length += end - start;
            area += length * (y1 - y0);
        }
        return area;
    }

    private static void banks(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.backdrop().banks().isEmpty()) {
            return;
        }
        JovianData.Banks caps = content.jovian().banks();
        double field = (double) PlayField.WIDTH * PlayField.HEIGHT;
        List<BackdropData.Bank> all = level.backdrop().banks().get();
        for (int i = 0; i < all.size(); i++) {
            inside(level, "backdrop.banks[" + i + "].t", all.get(i).t(), problem);
        }
        int samples = (int) Math.ceil(level.seconds() / SAMPLE_SECONDS);
        for (Difficulty difficulty : Difficulty.values()) {
            List<BackdropData.Bank> on = level.backdrop().banksOn(difficulty);
            double cap = caps.coverOn(difficulty);
            boolean coverReported = false;
            double[] overBand = new double[on.size()];
            boolean[] bandReported = new boolean[on.size()];
            for (int s = 0; s <= samples; s++) {
                double at = s * SAMPLE_SECONDS;
                List<double[]> boxes = new ArrayList<>();
                for (int b = 0; b < on.size(); b++) {
                    double[] box = bankAt(level, on.get(b), at);
                    boxes.add(box);
                    boolean over = box[1] < caps.band() && box[3] > 0 && box[2] > 0 && box[0] < PlayField.WIDTH;
                    overBand[b] = over ? overBand[b] + SAMPLE_SECONDS : 0;
                    if (overBand[b] > caps.bandSeconds() + SAMPLE_SECONDS / 2 && !bandReported[b]) {
                        bandReported[b] = true;
                        problem.accept(
                                "backdrop.banks[" + all.indexOf(on.get(b)) + "]",
                                "never a bank over the ship's band (the bottom " + caps.band() + " px) for more than "
                                        + caps.bandSeconds() + " s ("
                                        + difficulty.name().toLowerCase(java.util.Locale.ROOT) + ": from t="
                                        + Math.round((at - overBand[b]) * 100) / 100.0 + ")");
                    }
                }
                double share = coveredArea(boxes) / field;
                if (share > cap + 1e-6 && !coverReported) {
                    coverReported = true;
                    problem.accept(
                            "backdrop.banks",
                            "at most " + cap + " of the play field under banks at a time ("
                                    + difficulty.name().toLowerCase(java.util.Locale.ROOT)
                                    + ": " + Math.round(share * 1000) / 1000.0 + " at t="
                                    + Math.round(at * 100) / 100.0 + ")");
                }
            }
        }
    }

    private static void gates(LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.Section> sections = level.sections();
        long held = sections.stream().filter(LevelData.Section::held).count();
        if (held < 2) {
            return;
        }
        if (level.escortSwap().isPresent()) {
            problem.accept("escort_swap", "an escort swap releases at the one held arena, not at a gate of several");
        }
        Set<String> groups = new HashSet<>();
        double before = 0;
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            if (!section.isArena()) {
                continue;
            }
            String field = "sections[" + i + "]";
            if (i == 0 || sections.get(i - 1).isArena()) {
                problem.accept(field, "a held gate follows a moving section (never the first, never two in a row)");
            }
            String group = section.holds().orElse("");
            if (!groups.add(group)) {
                problem.accept(field + ".holds", "each held gate has its own holders' group: '" + group + "'");
            }
            double after = before;
            for (LevelData.GroundTarget target : level.groundTargets()) {
                if (target.enemy().isEmpty()
                        || target.group().filter(group::equals).isEmpty()) {
                    continue;
                }
                for (LevelData.Placement placement : target.at()) {
                    if (placement.t() < after) {
                        problem.accept(
                                "ground_targets",
                                "a gate's holder enters after the gate before it ends (" + after + " s), or it holds"
                                        + " that gate: '" + group + "' at t=" + placement.t());
                    }
                }
            }
            before = section.end();
        }
    }

    private static void turn(LevelData level, BiConsumer<String, String> problem) {
        if (level.turn().isEmpty()) {
            return;
        }
        inside(level, "turn", level.turn().get(), problem);
        boolean hunting = level.waves().stream()
                .anyMatch(wave -> wave.hunts().filter(JovianRules::preyTag).isPresent());
        if (!hunting) {
            problem.accept("turn", "a turn makes the hunting waves' links live: the level has no hunts: <tag> wave");
        }
    }
}
