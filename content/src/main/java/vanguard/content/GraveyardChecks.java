package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M11 part D, step D2 (design/enemies/space/husk, {@code line_drift} and the crack; design/campaign Level 46, Waves and
 * the quiet; m11d-gaps.md §1 #1–#6): a dead-fleet level's checks. A drifting unit's wave names its {@code lines}, one for
 * each of its units on every difficulty (no two units on one line), and only such a wave has lines or {@code warn:
 * false}; each line enters from outside the field, and the lines of one wave keep their hulls at least {@link #APART}
 * px apart wherever they are on the field (parallel or diverging, never meeting). A unit's {@code crack} names a {@code
 * fan} among its attacks; a level's {@code crack_cue} needs a cracking unit in its waves. A scenery set piece flies on
 * {@code high-air} without a descent (it never touches the play plane).
 */
final class GraveyardChecks {
    /** Two drifting hulls' centres at least this far apart on the field, px (the Husk's rule). */
    static final double APART = 80;

    /** The sampling step along a line, px. */
    private static final double STEP = 4;

    private GraveyardChecks() {}

    /** A unit's Act 7 part D keys. */
    static void unit(EnemyData enemy, BiConsumer<String, String> problem) {
        enemy.crack().ifPresent(crack -> {
            Optional<EnemyData.Attack> fan = enemy.attack(crack.attack());
            if (fan.isEmpty()
                    || !fan.get().pattern().equals("fan")
                    || fan.get().count().isEmpty()) {
                problem.accept("crack.attack", "a crack fires one of the unit's fan attacks: '" + crack.attack() + "'");
            }
        });
    }

    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        boolean cracks = false;
        List<LevelData.Wave> waves = level.waves();
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            String field = "waves[" + i + "]";
            EnemyData enemy = wave.enemy().map(content.enemies()::get).orElse(null);
            boolean drifting = enemy != null && enemy.movement().lineDrift().isPresent();
            cracks |= enemy != null && enemy.crack().isPresent();
            if (wave.lines().isPresent() != drifting) {
                problem.accept(
                        field + ".lines",
                        drifting
                                ? "a drifting unit's wave names its lines"
                                : "only a drifting unit's wave (movement.line_drift) has lines");
            }
            if (wave.warn().isPresent() && !drifting) {
                problem.accept(field + ".warn", "only a drifting unit's wave enters unwarned (warn: false)");
            }
            if (!drifting || wave.lines().isEmpty()) {
                continue;
            }
            List<List<Double>> lines = wave.lines().get();
            int most = wave.count().orElse(1);
            for (Optional<LevelData.Change> change : List.of(wave.easy(), wave.hard())) {
                most = Math.max(most, change.flatMap(LevelData.Change::count).orElse(most));
            }
            if (lines.size() < most) {
                problem.accept(
                        field + ".lines",
                        "a line for each drifting unit: " + lines.size() + " lines for " + most
                                + " (pin the counts with easy/hard)");
            }
            for (int k = 0; k < lines.size(); k++) {
                List<Double> line = lines.get(k);
                if (onField(line.get(0), line.get(1))) {
                    problem.accept(field + ".lines[" + k + "]", "a drift line enters from outside the field: " + line);
                }
                for (int m = k + 1; m < lines.size(); m++) {
                    double apart = closest(line, lines.get(m));
                    if (apart < APART) {
                        problem.accept(
                                field + ".lines[" + m + "]",
                                String.format(
                                        java.util.Locale.ROOT,
                                        "drift lines %d and %d come %.0f px close on the field (at least %.0f)",
                                        k,
                                        m,
                                        apart,
                                        APART));
                    }
                }
            }
        }
        if (level.crackCue().isPresent() && !cracks) {
            problem.accept("crack_cue", "a crack cue needs a cracking unit (crack) in the level's waves");
        }
        List<LevelData.SetPieceData> pieces = level.setPieces().orElse(List.of());
        for (int i = 0; i < pieces.size(); i++) {
            LevelData.SetPieceData piece = pieces.get(i);
            if (piece.isScenery()
                    && piece.passes().stream()
                            .anyMatch(pass -> !pass.layer().equals("high-air")
                                    || pass.descend().isPresent())) {
                problem.accept(
                        "set_pieces[" + i + "].passes", "a scenery set piece flies on high-air and never descends");
            }
        }
    }

    private static boolean onField(double x, double depth) {
        return x >= 0 && x <= PlayField.WIDTH && depth >= 0 && depth <= PlayField.HEIGHT;
    }

    /**
     * The least distance between two lines' points on the field: each followed from its first point through its second
     * and on until it leaves the field.
     */
    private static double closest(List<Double> a, List<Double> b) {
        double[] pa = onFieldPoints(a);
        double[] pb = onFieldPoints(b);
        double least = Double.MAX_VALUE;
        for (int i = 0; i < pa.length; i += 2) {
            for (int j = 0; j < pb.length; j += 2) {
                least = Math.min(least, Math.hypot(pa[i] - pb[j], pa[i + 1] - pb[j + 1]));
            }
        }
        return least;
    }

    private static double[] onFieldPoints(List<Double> line) {
        double x0 = line.get(0);
        double y0 = line.get(1);
        double length = Math.hypot(line.get(2) - x0, line.get(3) - y0);
        double dx = (line.get(2) - x0) / length;
        double dy = (line.get(3) - y0) / length;
        java.util.List<Double> points = new java.util.ArrayList<>();
        boolean entered = false;
        for (double s = 0; s < 4 * (PlayField.WIDTH + PlayField.HEIGHT); s += STEP) {
            double x = x0 + dx * s;
            double y = y0 + dy * s;
            boolean on = onField(x, y);
            if (on) {
                entered = true;
                points.add(x);
                points.add(y);
            } else if (entered) {
                break;
            }
        }
        return points.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
