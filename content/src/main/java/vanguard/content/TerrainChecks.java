package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M7 part C's static checks (design/campaign Level 16, Terrain; design/enemies/ground/tendril-anchor and
 * threadcrawler). A level's {@code terrain}: the flyable width at least {@value TerrainBlock#MIN_WIDTH} px and a
 * channel beside an island at least {@value TerrainBlock#MIN_CHANNEL} px on every row (checked on the rows where a
 * line bends, where the least values lie), a wall moving at most {@value TerrainBlock#MAX_SLOPE} px sideways per px
 * of scroll, the islands between the walls; no ground target, secret trigger, released pickup or crawl path point in
 * rock (a gate's anchors in the {@value TerrainBlock#FOOT_BAND} px foot band along a collision line), a burrowing
 * field's area on the flyable floor; {@code first-wall-contact} and the {@code plateau} layer only with terrain. A
 * linked nest ({@code link: pairs}): of a unit with a {@code link} attack, its placements in pairs on every
 * difficulty (the same t, the left one first), two pairs at least {@value #GATE_GAP} s apart; a {@code links}
 * secondary only with linked nests. A crawling unit's wave has its {@code crawl} path, only such a wave has one.
 *
 * <p>A unit's: a {@code link} only on a ground unit that stands ({@code terrain} movement), at most one; a {@code
 * pores} attack and the {@code crawl} movement only on a ground segment chain, together; a pores hook only with
 * pores. Wave paths against rock are not re-simulated here: a test flies the real sortie (step C4,
 * TerrainClearanceTest; the default of the gap check, pending the user's review in round 39).
 */
final class TerrainChecks {
    /** Never two gates (tendril pairs) closer than this many s of scroll (the anchor spec). */
    static final double GATE_GAP = 1.5;

    private static final double EPSILON = 1e-6;

    private TerrainChecks() {}

    /** A unit's M7 part C keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        long links = enemy.attacks().stream()
                .filter(attack -> attack.link().isPresent())
                .count();
        if (links > 1) {
            problem.accept("attacks", "a unit has at most one link");
        }
        if (links > 0
                && (!enemy.layer().equals("ground")
                        || enemy.movement().terrain().isEmpty())) {
            problem.accept("attacks", "a link's anchors stand on the ground (layer ground, terrain movement)");
        }
        boolean pores =
                enemy.attacks().stream().anyMatch(attack -> attack.pores().isPresent());
        boolean crawls = enemy.movement().crawl().isPresent();
        if (pores != crawls) {
            problem.accept("movement.crawl", "a crawling chain fires a pore wave (pores) and only it");
        }
        if (crawls && (!enemy.layer().equals("ground") || enemy.segmentChain().isEmpty())) {
            problem.accept("movement.crawl", "a crawling unit is a segment chain on the ground");
        }
        enemy.segmentChain().flatMap(EnemyData.SegmentChain::cut).ifPresent(cut -> {
            if (!crawls) {
                problem.accept("segment_chain.cut", "only a crawling chain's rear part dies at a cut");
            }
        });
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::pores).isPresent() && !pores) {
                problem.accept("difficulty.pores", "a pores change without a pore wave");
            }
        }
    }

    /** A level's M7 part C keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        Optional<TerrainBlock> terrain = level.terrain();
        terrain.ifPresent(t -> geometry(t, problem));
        links(content, level, problem);
        crawls(content, level, problem);
        for (int i = 0; i < level.radio().size(); i++) {
            if (level.radio().get(i).event().orElse(null) == LevelData.CueEvent.FIRST_WALL_CONTACT
                    && terrain.isEmpty()) {
                problem.accept("radio[" + i + "].event", "first-wall-contact in a level without terrain");
            }
        }
        if (level.backdrop().hasPlateau() && terrain.isEmpty()) {
            problem.accept("backdrop.scroll_factors.plateau", "a plateau layer in a level without terrain");
        }
        terrain.ifPresent(t -> placements(content, level, t, problem));
    }

    private static void geometry(TerrainBlock terrain, BiConsumer<String, String> problem) {
        terrain.left().ifPresent(points -> slope("terrain.left", points, problem));
        terrain.right().ifPresent(points -> slope("terrain.right", points, problem));
        boolean walls = terrain.left().isPresent() || terrain.right().isPresent();
        for (double row : terrain.breaks()) {
            double l = terrain.leftAt(row);
            double r = terrain.rightAt(row);
            if (!Double.isNaN(l) && !Double.isNaN(r) && r - l < TerrainBlock.MIN_WIDTH - EPSILON) {
                problem.accept(
                        "terrain",
                        "the flyable width at row " + num(row) + " is " + num(r - l) + " px, under "
                                + num(TerrainBlock.MIN_WIDTH));
            }
            List<double[]> spans = terrain.spans(row);
            if (spans.size() > 1) {
                for (double[] span : spans) {
                    if (span[1] - span[0] < TerrainBlock.MIN_CHANNEL - EPSILON) {
                        problem.accept(
                                "terrain.islands",
                                "a channel at row " + num(row) + " is " + num(span[1] - span[0]) + " px ("
                                        + num(span[0]) + "–" + num(span[1]) + "), under "
                                        + num(TerrainBlock.MIN_CHANNEL));
                    }
                }
            }
        }
        List<TerrainBlock.Island> islands = terrain.islandList();
        for (int i = 0; i < islands.size(); i++) {
            TerrainBlock.Island island = islands.get(i);
            for (var point : island.points()) {
                double l = terrain.leftAt(point.y());
                double r = terrain.rightAt(point.y());
                if (walls && (Double.isNaN(l) && Double.isNaN(r))
                        || !Double.isNaN(l) && point.x() <= l
                        || !Double.isNaN(r) && point.x() >= r) {
                    problem.accept(
                            "terrain.islands[" + i + "]",
                            "'" + island.name() + "' reaches the walls at [" + num(point.x()) + ", " + num(point.y())
                                    + "]: an island stands between them");
                    break;
                }
            }
        }
    }

    private static void slope(String field, List<TerrainBlock.WallPoint> points, BiConsumer<String, String> problem) {
        for (int i = 1; i < points.size(); i++) {
            TerrainBlock.WallPoint a = points.get(i - 1);
            TerrainBlock.WallPoint b = points.get(i);
            double slope = Math.abs(b.x() - a.x()) / (b.scroll() - a.scroll());
            if (slope > TerrainBlock.MAX_SLOPE + EPSILON) {
                problem.accept(
                        field + "[" + i + "]",
                        "the wall moves " + num(slope) + " px sideways per px of scroll from row " + num(a.scroll())
                                + ", over " + num(TerrainBlock.MAX_SLOPE));
            }
        }
    }

    /** Every list of a ground target's placements: medium's and the easy and hard ones it has. */
    static List<List<LevelData.Placement>> lists(LevelData.GroundTarget target) {
        List<List<LevelData.Placement>> out = new ArrayList<>();
        out.add(target.at());
        target.easy().flatMap(LevelData.Placements::at).ifPresent(out::add);
        target.hard().flatMap(LevelData.Placements::at).ifPresent(out::add);
        return out;
    }

    /** The placements of a ground target on a difficulty. */
    static List<LevelData.Placement> on(LevelData.GroundTarget target, Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> target.easy().flatMap(LevelData.Placements::at).orElse(target.at());
            case MEDIUM -> target.at();
            case HARD -> target.hard().flatMap(LevelData.Placements::at).orElse(target.at());
        };
    }

    private static void links(Content content, LevelData level, BiConsumer<String, String> problem) {
        boolean linked = false;
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.link().isEmpty()) {
                continue;
            }
            linked = true;
            String field = "ground_targets[" + i + "]";
            String slug = target.enemy().orElseThrow();
            if (content.enemies().containsKey(slug)
                    && content.enemy(slug).attacks().stream()
                            .noneMatch(attack -> attack.link().isPresent())) {
                problem.accept(field + ".link", "'" + slug + "' has no link attack");
            }
            for (List<LevelData.Placement> at : lists(target)) {
                if (at.size() % 2 != 0) {
                    problem.accept(field + ".at", "a linked nest's placements pair up: " + at.size() + " is odd");
                    continue;
                }
                for (int p = 0; p < at.size(); p += 2) {
                    LevelData.Placement a = at.get(p);
                    LevelData.Placement b = at.get(p + 1);
                    if (Math.abs(a.t() - b.t()) > EPSILON || a.x() >= b.x()) {
                        problem.accept(
                                field + ".at[" + p + "]",
                                "a pair's anchors enter together (the same t), the left one first");
                    }
                }
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            List<Double> pairs = new ArrayList<>();
            for (LevelData.GroundTarget target : level.groundTargets()) {
                if (target.link().isPresent()) {
                    List<LevelData.Placement> at = on(target, difficulty);
                    for (int p = 0; p + 1 < at.size(); p += 2) {
                        pairs.add(at.get(p).t());
                    }
                }
            }
            pairs.sort(Double::compare);
            for (int p = 1; p < pairs.size(); p++) {
                if (pairs.get(p) - pairs.get(p - 1) < GATE_GAP - EPSILON) {
                    problem.accept(
                            "ground_targets",
                            "two tendril pairs " + num(pairs.get(p) - pairs.get(p - 1)) + " s apart at t="
                                    + num(pairs.get(p)) + " ("
                                    + difficulty.name().toLowerCase(java.util.Locale.ROOT)
                                    + "), under " + num(GATE_GAP) + " s");
                }
            }
        }
        if (level.objectives().secondary().map(LevelData.Secondary::breaksLinks).orElse(false) && !linked) {
            problem.accept("objectives.secondary.links", "a links objective in a level without linked nests");
        }
    }

    private static void crawls(Content content, LevelData level, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            boolean crawling = wave.groupList().stream()
                    .anyMatch(group -> content.enemies().containsKey(group.enemy())
                            && content.enemy(group.enemy()).movement().crawl().isPresent());
            if (crawling != wave.crawl().isPresent()) {
                problem.accept(
                        "waves[" + i + "].crawl", "a crawling unit's wave has its crawl path, and only such a wave");
            }
            if (wave.crawl().isPresent() && level.terrain().isEmpty()) {
                problem.accept("waves[" + i + "].crawl", "a crawl path in a level without terrain");
            }
        }
    }

    private static void placements(
            Content content, LevelData level, TerrainBlock terrain, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.side().isPresent()) {
                continue;
            }
            String field = "ground_targets[" + i + "]";
            double height = target.enemy()
                    .filter(content.enemies()::containsKey)
                    .map(slug -> content.enemy(slug).hitbox().height())
                    .or(() -> target.size().map(Size::height))
                    .orElse(0.0);
            for (List<LevelData.Placement> at : lists(target)) {
                for (int p = 0; p < at.size(); p++) {
                    LevelData.Placement placement = at.get(p);
                    double row = level.placementRow(placement.t(), height);
                    String where = field + ".at[" + p + "]";
                    if (terrain.rock(row, placement.x())) {
                        problem.accept(
                                where,
                                "[" + num(placement.t()) + ", " + num(placement.x()) + "] is in rock (row " + num(row)
                                        + ")");
                    } else if (target.link().isPresent()
                            && terrain.toFoot(row, placement.x()) > TerrainBlock.FOOT_BAND + EPSILON) {
                        problem.accept(
                                where,
                                "a gate's anchor stands in the " + num(TerrainBlock.FOOT_BAND)
                                        + " px foot band along a collision line, [" + num(placement.t()) + ", "
                                        + num(placement.x()) + "] is " + num(terrain.toFoot(row, placement.x()))
                                        + " px off it");
                    }
                }
            }
        }
        for (int i = 0; i < level.released().size(); i++) {
            LevelData.Drop drop = level.released().get(i);
            double row = TerrainBlock.row(level.scrollAt(drop.t()), drop.y());
            if (terrain.rock(row, drop.x())) {
                problem.accept("drops[" + i + "]", "released over rock (row " + num(row) + ")");
            }
        }
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            List<TerrainBlock.GroundPoint> path = wave.crawl().orElse(List.of());
            for (int p = 0; p < path.size(); p++) {
                TerrainBlock.GroundPoint point = path.get(p);
                if (terrain.rock(point.scroll(), point.x())) {
                    problem.accept(
                            field + ".crawl[" + p + "]",
                            "[" + num(point.scroll()) + ", " + num(point.x()) + "] is in rock");
                }
            }
            boolean burrows = wave.groupList().stream()
                    .anyMatch(group -> content.enemies().containsKey(group.enemy())
                            && content.enemy(group.enemy()).movement().burrow().isPresent());
            if (burrows && wave.x().isPresent() && wave.size().isPresent()) {
                field(level, terrain, wave, field, problem);
            }
        }
    }

    /** A burrowing field's area (its bottom edge at the top edge at its t) lies on the flyable floor, clear of rock. */
    private static void field(
            LevelData level,
            TerrainBlock terrain,
            LevelData.Wave wave,
            String field,
            BiConsumer<String, String> problem) {
        double bottom = TerrainBlock.row(level.scrollAt(wave.t()), PlayField.HEIGHT);
        double top = bottom + wave.size().get().height();
        double from = wave.x().get() - wave.size().get().width() / 2;
        double to = wave.x().get() + wave.size().get().width() / 2;
        List<Double> rows = new ArrayList<>(List.of(bottom, top));
        terrain.breaks().stream().filter(row -> row > bottom && row < top).forEach(rows::add);
        for (double row : rows) {
            List<double[]> spans = terrain.spans(row);
            boolean inside = spans.stream().anyMatch(span -> from >= span[0] - EPSILON && to <= span[1] + EPSILON);
            if (!inside) {
                problem.accept(
                        field + ".size",
                        "the burrowing field (x " + num(from) + "–" + num(to) + ") reaches rock at row " + num(row));
                return;
            }
        }
    }

    private static String num(double value) {
        return value == Math.rint(value)
                ? String.valueOf((long) value)
                : String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
