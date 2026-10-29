package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M9 part E's static checks (design/campaign Level 32 Deep Core; design/enemies/ground/sentinel-tower and strider).
 *
 * <p>A unit's: a hardened collar ({@code armour.collar}) inside its hit box; a sweep's {@code full_turn} only on a
 * {@code terrain} unit's {@code laser-sweep} (the ground sweep), a {@code full_turn_every} hook only with one; a part
 * that {@code topples} only on a walker whose {@code walk} has its {@code topple}, and such a topple only with one.
 *
 * <p>A level's: a {@code branch} (a wave's, a ground target's, a radio cue's) names one of the terrain's {@code forks};
 * a branch's wave and ground target come at or after the fork's latest choice (its island's tip at the bottom edge,
 * {@link #latestChoice}) and while the island is on the screen, a ground target inside its side's channel; a {@code
 * topple-crush} cue only with a unit that topples; a ground target's {@code full_turn_first} only on a unit whose sweep
 * has a {@code full_turn} (step E3b, QE-3 a). Placement (QE-1 a, pending round 65): every hardened collar's emitter
 * and every blast-door lock (a ground target whose {@code sprite} starts with {@code blast-door}) at least {@value
 * #EDGE_INSET} px inside the field; a ground-sweeping unit (the Sentinel) never beside a passage narrower than its reach
 * plus {@value #PASSAGE_MARGIN} px over its reach's rows, and never beside a fork's island; a walker's walk path never
 * in rock.
 */
final class DeepCoreChecks {
    /** The least gap between an emitter's or a lock's box and the play field's side edges, px (QE-1 a). */
    static final double EDGE_INSET = 40;

    /** A ground sweep's passage: at least its reach plus this, px (design/enemies/ground/sentinel-tower). */
    static final double PASSAGE_MARGIN = 80;

    /** The step along a walk path's segments and a reach's rows, px. */
    private static final double SAMPLE = 8;

    private DeepCoreChecks() {}

    /** A unit's part-E keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        enemy.armour().collar().ifPresent(collar -> {
            if (collar.width() > enemy.hitbox().width()
                    || collar.height() > enemy.hitbox().height()) {
                problem.accept("armour.collar", "the collar's emitter lies inside the unit's hit box");
            }
        });
        boolean fullTurn = false;
        for (int i = 0; i < enemy.attacks().size(); i++) {
            Optional<EnemyData.Sweep> sweep = enemy.attacks().get(i).sweep();
            if (sweep.flatMap(EnemyData.Sweep::fullTurn).isPresent()) {
                fullTurn = true;
                if (enemy.movement().terrain().isEmpty()) {
                    problem.accept(
                            "attacks[" + i + "].sweep.full_turn",
                            "a full turn is a ground unit's sweep (movement: terrain)");
                }
            }
        }
        boolean hook = enemy.difficulty()
                .map(hooks -> java.util.stream.Stream.of(hooks.easy(), hooks.hard())
                        .flatMap(Optional::stream)
                        .anyMatch(change -> change.fullTurnEvery().isPresent()))
                .orElse(false);
        if (hook && !fullTurn) {
            problem.accept("difficulty", "full_turn_every changes a sweep's full_turn: the unit has none");
        }
        boolean topples = enemy.partList().orElse(List.of()).stream().anyMatch(EnemyData.PartData::toppling);
        boolean topple = enemy.movement().walk().flatMap(EnemyData.Walk::topple).isPresent();
        if (topples != topple) {
            problem.accept(
                    "movement.walk.topple",
                    "a walker's topple goes with a part that topples it (topples: true), and only so");
        }
    }

    /** A level's part-E keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        Optional<TerrainBlock> terrain = level.terrain();
        List<TerrainBlock.Fork> forks = terrain.map(TerrainBlock::forkList).orElse(List.of());
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            if (wave.branch().isPresent()) {
                branchTime(level, terrain, forks, wave.branch().get(), wave.t(), "waves[" + i + "].branch", problem);
            }
        }
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            String field = "ground_targets[" + i + "]";
            if (target.fullTurnFirst().isPresent() && !fullTurns(content, target)) {
                problem.accept(
                        field + ".full_turn_first",
                        "a first full turn is a ground-sweeping unit's (a terrain unit's laser-sweep with a full_turn)");
            }
            double height = height(content, target);
            double width = width(content, target);
            for (List<LevelData.Placement> at : TerrainChecks.lists(target)) {
                for (int p = 0; p < at.size(); p++) {
                    LevelData.Placement placement = at.get(p);
                    String where = field + ".at[" + p + "]";
                    if (target.branch().isPresent()
                            && branchTime(
                                    level, terrain, forks, target.branch().get(), placement.t(), where, problem)) {
                        channel(
                                level,
                                terrain.orElseThrow(),
                                forks,
                                target.branch().get(),
                                placement,
                                height,
                                where,
                                problem);
                    }
                    inset(content, target, placement, width, where, problem);
                    if (terrain.isPresent()) {
                        passage(content, level, terrain.get(), forks, target, placement, height, where, problem);
                    }
                }
            }
        }
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            if (cue.fork().isPresent() && cue.fork().get() > forks.size()) {
                problem.accept(
                        "radio[" + i + "].fork",
                        "fork " + cue.fork().get() + " of the terrain's " + forks.size() + " forks");
            }
            if (cue.event().orElse(null) == LevelData.CueEvent.TOPPLE_CRUSH && !topples(content, level)) {
                problem.accept("radio[" + i + "].event", "a topple-crush cue in a level where nothing topples");
            }
        }
        terrain.ifPresent(t -> walkPaths(content, level, t, problem));
    }

    /**
     * Whether a branch's item at {@code t} names one of the forks and comes at or after the fork's latest choice and
     * while its island is still on the screen; reports what it does not.
     */
    private static boolean branchTime(
            LevelData level,
            Optional<TerrainBlock> terrain,
            List<TerrainBlock.Fork> forks,
            TerrainBlock.Branch branch,
            double t,
            String field,
            BiConsumer<String, String> problem) {
        if (branch.fork() > forks.size()) {
            problem.accept(field, "fork " + branch.fork() + " of the terrain's " + forks.size() + " forks");
            return false;
        }
        TerrainBlock.Island island = terrain.orElseThrow()
                .islandList()
                .get(forks.get(branch.fork() - 1).island());
        double[] rows = island.rows();
        double latest = latestChoice(level, rows[0]);
        if (t < latest - 1e-6) {
            problem.accept(
                    field,
                    "a branch's item comes at or after its fork's latest choice (the island's tip at the bottom edge,"
                            + " t " + num(latest) + "), was " + num(t));
            return false;
        }
        if (level.scrollAt(t) > rows[1]) {
            problem.accept(field, "a branch's item comes while its island is on the screen (t " + num(t) + ")");
            return false;
        }
        return true;
    }

    /**
     * The latest level time a fork whose island's tip lies on {@code tipRow} can be chosen: the tip reaching the bottom
     * edge (the ship's row can lie no lower), when the ground has scrolled the tip's row plus the field's height.
     */
    static double latestChoice(LevelData level, double tipRow) {
        double scroll = tipRow + PlayField.HEIGHT;
        double from = 0;
        double to = 1;
        while (level.scrollAt(to) < scroll && to < 100_000) {
            to *= 2;
        }
        for (int k = 0; k < 60; k++) {
            double mid = (from + to) / 2;
            if (level.scrollAt(mid) < scroll) {
                from = mid;
            } else {
                to = mid;
            }
        }
        return to;
    }

    /** A branch's ground target stands in its side's channel: off rock, on its side of the island's middle. */
    private static void channel(
            LevelData level,
            TerrainBlock terrain,
            List<TerrainBlock.Fork> forks,
            TerrainBlock.Branch branch,
            LevelData.Placement placement,
            double height,
            String field,
            BiConsumer<String, String> problem) {
        TerrainBlock.Island island =
                terrain.islandList().get(forks.get(branch.fork() - 1).island());
        double row = level.placementRow(placement.t(), height);
        boolean left = placement.x() < middle(island);
        if (terrain.rock(row, placement.x()) || left != branch.side().equals("left")) {
            problem.accept(
                    field,
                    "a branch's ground target stands in its " + branch.side() + " channel, [" + num(placement.t())
                            + ", " + num(placement.x()) + "] does not");
        }
    }

    /** The middle of an island across (the simulation's: half way between its leftmost and rightmost points). */
    static double middle(TerrainBlock.Island island) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Point point : island.points()) {
            min = Math.min(min, point.x());
            max = Math.max(max, point.x());
        }
        return (min + max) / 2;
    }

    /** QE-1 a: a collar's emitter and a blast-door lock at least {@value #EDGE_INSET} px inside the field. */
    private static void inset(
            Content content,
            LevelData.GroundTarget target,
            LevelData.Placement placement,
            double width,
            String field,
            BiConsumer<String, String> problem) {
        double half;
        String what;
        Optional<Size> collar = target.enemy()
                .filter(content.enemies()::containsKey)
                .flatMap(slug -> content.enemy(slug).armour().collar());
        if (collar.isPresent()) {
            half = collar.get().width() / 2;
            what = "a collar's emitter";
        } else if (target.sprite()
                .filter(sprite -> sprite.startsWith("blast-door"))
                .isPresent()) {
            half = width / 2;
            what = "a blast-door lock";
        } else {
            return;
        }
        if (placement.x() - half < EDGE_INSET - 1e-6 || placement.x() + half > PlayField.WIDTH - EDGE_INSET + 1e-6) {
            problem.accept(
                    field,
                    what + " lies at least " + num(EDGE_INSET) + " px inside the field (QE-1), x " + num(placement.x())
                            + " ± " + num(half) + " does not");
        }
    }

    /**
     * A ground-sweeping unit (a {@code terrain} unit's {@code laser-sweep}) never beside a passage narrower than its
     * reach plus {@value #PASSAGE_MARGIN} px, nor beside a fork's island, over the rows its reach covers.
     */
    private static void passage(
            Content content,
            LevelData level,
            TerrainBlock terrain,
            List<TerrainBlock.Fork> forks,
            LevelData.GroundTarget target,
            LevelData.Placement placement,
            double height,
            String field,
            BiConsumer<String, String> problem) {
        Optional<EnemyData> enemy =
                target.enemy().filter(content.enemies()::containsKey).map(content::enemy);
        if (enemy.isEmpty() || enemy.get().movement().terrain().isEmpty()) {
            return;
        }
        Optional<EnemyData.Sweep> sweep = enemy.get().attacks().stream()
                .flatMap(attack -> attack.sweep().stream())
                .findFirst();
        if (sweep.isEmpty()) {
            return;
        }
        double reach = sweep.get().length();
        double row = level.placementRow(placement.t(), height);
        for (double r = row - reach; r <= row + reach; r += SAMPLE) {
            double l = terrain.leftAt(r);
            double right = terrain.rightAt(r);
            double width = (Double.isNaN(right) ? PlayField.WIDTH : right) - (Double.isNaN(l) ? 0 : l);
            boolean island = false;
            for (TerrainBlock.Fork fork : forks) {
                double[] rows = terrain.islandList().get(fork.island()).rows();
                island |= r >= rows[0] && r <= rows[1];
            }
            if (island || width < reach + PASSAGE_MARGIN - 1e-6) {
                problem.accept(
                        field,
                        "a sweeping tower stands beside a passage of at least its reach + " + num(PASSAGE_MARGIN) + " ("
                                + num(reach + PASSAGE_MARGIN) + " px) and no fork, row " + num(r) + " has "
                                + (island ? "a fork's island" : num(width) + " px"));
                return;
            }
        }
    }

    /** Whether the target's enemy is a {@code terrain} unit whose sweep has a {@code full_turn}. */
    private static boolean fullTurns(Content content, LevelData.GroundTarget target) {
        Optional<EnemyData> enemy =
                target.enemy().filter(content.enemies()::containsKey).map(content::enemy);
        return enemy.isPresent()
                && enemy.get().movement().terrain().isPresent()
                && enemy.get().attacks().stream()
                        .flatMap(attack -> attack.sweep().stream())
                        .anyMatch(sweep -> sweep.fullTurn().isPresent());
    }

    /** A walker's walk path (its wave's paths or lanes, x and depth below the top edge at its t) never in rock. */
    private static void walkPaths(
            Content content, LevelData level, TerrainBlock terrain, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            boolean walker = wave.groupList().stream()
                    .anyMatch(group -> content.enemies().containsKey(group.enemy())
                            && content.enemy(group.enemy()).movement().walk().isPresent());
            if (!walker) {
                continue;
            }
            double scroll = level.scrollAt(wave.t());
            List<List<Point>> paths = new java.util.ArrayList<>(wave.paths().orElse(List.of()));
            paths.addAll(wave.lanes().orElse(List.of()));
            for (int k = 0; k < paths.size(); k++) {
                List<Point> path = paths.get(k);
                for (int p = 1; p < path.size(); p++) {
                    Point a = path.get(p - 1);
                    Point b = path.get(p);
                    double length = Math.hypot(b.x() - a.x(), b.y() - a.y());
                    int steps = Math.max(1, (int) Math.ceil(length / SAMPLE));
                    for (int s = 0; s <= steps; s++) {
                        double x = a.x() + (b.x() - a.x()) * s / steps;
                        double depth = a.y() + (b.y() - a.y()) * s / steps;
                        if (terrain.rock(scroll - depth, x)) {
                            problem.accept(
                                    "waves[" + i + "]",
                                    "a walker's walk path runs into rock at [" + num(x) + ", " + num(depth) + "]");
                            s = steps;
                            p = path.size();
                        }
                    }
                }
            }
        }
    }

    /** Whether a unit of the level topples (a walker wave's unit whose walk has a topple). */
    private static boolean topples(Content content, LevelData level) {
        return level.waves().stream()
                .flatMap(wave -> wave.groupList().stream())
                .anyMatch(group -> content.enemies().containsKey(group.enemy())
                        && content.enemy(group.enemy())
                                .movement()
                                .walk()
                                .flatMap(EnemyData.Walk::topple)
                                .isPresent());
    }

    private static double height(Content content, LevelData.GroundTarget target) {
        return target.enemy()
                .filter(content.enemies()::containsKey)
                .map(slug -> content.enemy(slug).hitbox().height())
                .or(() -> target.size().map(Size::height))
                .orElse(0.0);
    }

    private static double width(Content content, LevelData.GroundTarget target) {
        return target.enemy()
                .filter(content.enemies()::containsKey)
                .map(slug -> content.enemy(slug).hitbox().width())
                .or(() -> target.size().map(Size::width))
                .orElse(0.0);
    }

    private static String num(double value) {
        return value == Math.rint(value)
                ? String.valueOf((long) value)
                : String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
