package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M11 part C, step C2 (design/world/vrell-space, living terrain; design/campaign Level 45, Pit swarms, Hazards and The
 * spawning organ; m11c-gaps.md §1 #3/#4/#6): a reef level's checks ({@code pits}). A wave's {@code pit} names one of the
 * level's pits, from the front, each pit serving one wave; its pit is on the screen through its tell (its centre at or
 * below the top edge at {@code t − tell}) and opens in the field's top 60 % (the pit swarms count as front). No pit lies
 * under a ground target (no ground body over another: the pit's 72 px mouth clear of every placed body), and no
 * crawler's path runs over a pit or an organ (the Threadcrawler's Act 7 section). The organs (a destroy-targets level's
 * spawning units, {@code spawn.every}) are never two on the screen at once, and no spore cloud drifts over an organ or the
 * story pickup's group (the call organ's roots) while both are on the screen. All in ground rows: a ground placement
 * {@code [t, x]}'s centre row is the scroll at {@code t} plus half its height ({@link LevelData#placementRow}), a pit's
 * its {@code at}, a crawl point's its {@code [scroll, x]}.
 */
final class ReefChecks {
    /** A pit's mouth, px across (the backdrop's pit piece). */
    static final double PIT = 72;

    /** The share of the field from the top a pit opens in. */
    static final double OPEN_SHARE = 0.6;

    /** A Threadcrawler's half width, px (its 30 px segments' boxes, rounded up), kept clear of a pit or an organ. */
    static final double CRAWLER_HALF = 20;

    private static final double STEP = 0.05;

    private ReefChecks() {}

    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.Wave> waves = level.waves();
        boolean pits = !level.pitList().isEmpty();
        int[] served = new int[level.pitList().size()];
        VrellSpaceData.LivingTerrain terrain = content.vrellSpace().livingTerrain();
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            if (wave.pit().isEmpty()) {
                continue;
            }
            String field = "waves[" + i + "].pit";
            int pit = level.pitIndex(wave.pit().get());
            if (pit < 0) {
                problem.accept(
                        field,
                        "names one of the level's pits, was '" + wave.pit().get() + "'");
                continue;
            }
            served[pit]++;
            if (wave.from() != LevelData.Entry.FRONT
                    || wave.easy().flatMap(LevelData.Change::from).isPresent()
                    || wave.hard().flatMap(LevelData.Change::from).isPresent()) {
                problem.accept(field, "a pit's wave rises from the front's ground (from: front)");
            }
            VrellBlock.Pit placed = level.pitList().get(pit);
            double told = level.scrollAt(Math.max(0, wave.t() - terrain.tell())) - placed.at();
            double open = level.scrollAt(wave.t()) - placed.at();
            if (told < 0 || open > OPEN_SHARE * PlayField.HEIGHT) {
                problem.accept(
                        field,
                        "its pit is on the screen through its tell and opens in the field's top 60 %: its centre "
                                + Math.round(told) + " px down at the tell, " + Math.round(open) + " px at t");
            }
        }
        for (int p = 0; p < served.length; p++) {
            if (pits && served[p] != 1) {
                problem.accept(
                        "pits[" + p + "]",
                        "each pit serves one wave, " + level.pitList().get(p).id() + " serves " + served[p]);
            }
        }
        List<Body> bodies = bodies(content, level);
        for (int p = 0; p < level.pitList().size(); p++) {
            VrellBlock.Pit pit = level.pitList().get(p);
            for (Body body : bodies) {
                if (Math.abs(pit.x() - body.x) < PIT / 2 + body.halfWidth
                        && Math.abs(pit.at() - body.row) < PIT / 2 + body.halfHeight) {
                    problem.accept("pits[" + p + "]", "no pit under a ground target: it lies under " + body.name);
                }
            }
        }
        if (pits) {
            crawls(level, bodies, problem);
            organs(content, level, bodies, problem);
        }
    }

    /** A placed ground body: its name, centre (x, row) and half size. */
    private record Body(String name, double x, double row, double halfWidth, double halfHeight, boolean organ) {}

    private static List<Body> bodies(Content content, LevelData level) {
        List<Body> out = new ArrayList<>();
        for (GroundTargetsBlock.GroundTarget target : level.groundTargets()) {
            Size size = target.enemy()
                    .filter(content.enemies()::containsKey)
                    .map(slug -> content.enemy(slug).size())
                    .or(target::size)
                    .orElse(null);
            if (size == null) {
                continue;
            }
            boolean organ = target.enemy()
                    .filter(content.enemies()::containsKey)
                    .map(content::enemy)
                    .map(ReefChecks::spawns)
                    .orElse(false);
            for (GroundTargetsBlock.Placement at : target.at()) {
                out.add(new Body(
                        target.target(),
                        at.x(),
                        level.placementRow(at.t(), size.height()),
                        size.width() / 2,
                        size.height() / 2,
                        organ));
            }
        }
        return out;
    }

    /** Whether a unit is a spawner on the ground (a periodic spawn: the Hive Node, the Spawning Reef, the organ). */
    static boolean spawns(EnemyData enemy) {
        return enemy.layer().equals("ground")
                && enemy.attacks().stream()
                        .anyMatch(attack ->
                                attack.spawn().map(EnemyData.Spawn::periodic).orElse(false));
    }

    private static void crawls(LevelData level, List<Body> bodies, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.waves().size(); i++) {
            List<TerrainBlock.GroundPoint> path = level.waves().get(i).crawl().orElse(List.of());
            for (int k = 1; k < path.size(); k++) {
                TerrainBlock.GroundPoint a = path.get(k - 1);
                TerrainBlock.GroundPoint b = path.get(k);
                for (int p = 0; p < level.pitList().size(); p++) {
                    VrellBlock.Pit pit = level.pitList().get(p);
                    if (distance(a, b, pit.x(), pit.at()) < PIT / 2 + CRAWLER_HALF) {
                        problem.accept(
                                "waves[" + i + "].crawl",
                                "a crawler's path runs clear of the pits: it crosses " + pit.id());
                    }
                }
                for (Body body : bodies) {
                    if (body.organ
                            && distance(a, b, body.x, body.row)
                                    < Math.max(body.halfWidth, body.halfHeight) + CRAWLER_HALF) {
                        problem.accept(
                                "waves[" + i + "].crawl",
                                "a crawler's path runs clear of the organs: it crosses " + body.name);
                    }
                }
            }
        }
    }

    /** The distance from (x, row) to the segment a–b in ground space, px. */
    private static double distance(TerrainBlock.GroundPoint a, TerrainBlock.GroundPoint b, double x, double row) {
        double dx = b.x() - a.x();
        double dr = b.scroll() - a.scroll();
        double length = dx * dx + dr * dr;
        double u = length == 0 ? 0 : Math.clamp(((x - a.x()) * dx + (row - a.scroll()) * dr) / length, 0, 1);
        return Math.hypot(a.x() + u * dx - x, a.scroll() + u * dr - row);
    }

    private static void organs(
            Content content, LevelData level, List<Body> bodies, BiConsumer<String, String> problem) {
        if (!level.objectives().primary().equals("destroy-targets")) {
            return;
        }
        List<Body> organs = bodies.stream().filter(Body::organ).toList();
        for (int i = 0; i < organs.size(); i++) {
            for (int j = i + 1; j < organs.size(); j++) {
                Body a = organs.get(i);
                Body b = organs.get(j);
                if (Math.abs(a.row - b.row) < PlayField.HEIGHT + a.halfHeight + b.halfHeight) {
                    problem.accept("ground_targets", "never two organs on the screen: " + a.name + " and " + b.name);
                }
            }
        }
        List<String> story = level.pickups().stream()
                .filter(p -> p.story().isPresent())
                .flatMap(p -> p.droppedBy().flatMap(PickupsBlock.Carrier::group).stream())
                .toList();
        List<Body> watched = new ArrayList<>(organs);
        for (GroundTargetsBlock.GroundTarget target : level.groundTargets()) {
            if (target.group().filter(story::contains).isPresent()) {
                bodies.stream()
                        .filter(body -> body.name.equals(target.target()))
                        .forEach(watched::add);
            }
        }
        if (level.sporeClouds().isEmpty()) {
            return;
        }
        VrellSpaceData.Spores numbers = content.vrellSpace().spores();
        double factor = level.backdrop().scrollFactors().getOrDefault(BackdropLayer.HIGH_AIR, numbers.scrollFactor());
        double end = level.sections().getLast().end();
        for (int c = 0; c < level.sporeClouds().size(); c++) {
            LevelData.SporeCloud cloud = level.sporeClouds().get(c);
            for (Body body : watched) {
                for (double t = cloud.t(); t < end; t += STEP) {
                    double cloudY = VrellLevelChecks.y(level, cloud, factor, t);
                    if (cloudY - cloud.radius() > PlayField.HEIGHT) {
                        break;
                    }
                    double bodyY = level.scrollAt(t) - body.row;
                    boolean shown = bodyY + body.halfHeight > 0 && bodyY - body.halfHeight < PlayField.HEIGHT;
                    if (shown
                            && Math.abs(cloud.x() - body.x) < cloud.radius() + body.halfWidth
                            && Math.abs(cloudY - bodyY) < cloud.radius() + body.halfHeight) {
                        problem.accept(
                                "spores[" + c + "]",
                                "never over an organ or the call organ while it is on the screen: over " + body.name
                                        + " at t=" + Math.round(t * 10) / 10.0);
                        break;
                    }
                }
            }
        }
    }
}
