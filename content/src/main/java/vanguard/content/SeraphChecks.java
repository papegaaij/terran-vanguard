package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M11 part E, step E2 (design/enemies/air/choir-seraph, Difficulty and level use; design/campaign Level 47 Waves; m11e §1
 * #6): a level's static rules for its mirror-row teleporters (a teleport whose cycle arrives on its mirror row: the Choir
 * Seraphs), on every difficulty. A {@code pair} wave sends two such units from the front (a {@code single} one; fixed
 * {@code rows} only on those). Their holds never overlap: a wave of them enters only after the one before it is gone
 * (its hold: the first ring, every cycle's mirror, fold, fade and ring, the last mirror, the leaving tell and its fade:
 * {@link #hold}), so at most one pair is on the screen. No edge warning (a side or rear wave's, from {@code t} minus its
 * warning, 3 s if not given; a rear ambush is unwarned) starts within {@value #WARNING_APART} s of one of their landings.
 * No other {@code medium} or larger hull enters while they hold, but a unit with an aura (the Herald over its pair). While
 * they hold the popcorn stays off the centre line: a front wave of {@code tiny} or {@code small} units names its {@code
 * edge} or flies {@code paths} (or lanes) whose points keep {@value #OFF_CENTRE} px from the centre, but a cross from every
 * edge.
 */
final class SeraphChecks {
    private SeraphChecks() {}

    /** The least time between an edge warning's start and a landing, s. */
    static final double WARNING_APART = 0.5;

    /** An edge warning's seconds when the wave gives none (the vocabulary's 3 s). */
    static final double WARNING = 3;

    /** How far a popcorn swarm's path keeps from the centre line while a pair holds, px. */
    static final double OFF_CENTRE = 60;

    /** The play field's centre line, px from the left edge. */
    static final double CENTRE = 240;

    /** A unit whose rings arrive on its mirror row (the Choir Seraph). */
    static boolean mirrorRow(EnemyData enemy) {
        return enemy.movement()
                .teleport()
                .flatMap(EnemyData.Teleport::cycle)
                .filter(cycle -> cycle.arrive() == EnemyData.Teleport.Cycle.Arrive.MIRROR)
                .isPresent();
    }

    /** The unit's cycle period at {@code difficulty}: its mirror, fold, fade and next ring, s. */
    static double period(EnemyData enemy, Difficulty difficulty) {
        EnemyData.Teleport teleport = enemy.movement().teleport().orElseThrow();
        EnemyData.Teleport.Cycle cycle = teleport.cycle().orElseThrow();
        return glide(enemy, difficulty) + cycle.fold() + teleport.fade() + ring(enemy, difficulty);
    }

    /** Its landings' times after its wave's {@code t} at {@code difficulty}, s (its first ring, then a period each). */
    static List<Double> landings(EnemyData enemy, Difficulty difficulty) {
        EnemyData.Teleport teleport = enemy.movement().teleport().orElseThrow();
        Optional<EnemyData.TeleportChange> change =
                UnitRules.hook(enemy, difficulty).flatMap(EnemyData.Hook::teleport);
        int landings = change.flatMap(EnemyData.TeleportChange::jumps).orElse(teleport.jumps()) + 1;
        List<Double> out = new ArrayList<>();
        double first = teleport.cycle().orElseThrow().firstRing();
        for (int k = 0; k < landings; k++) {
            out.add(first + k * period(enemy, difficulty));
        }
        return out;
    }

    /** Its hold at {@code difficulty}: from its wave's {@code t} until its leaving jump's fade has ended, s. */
    static double hold(EnemyData enemy, Difficulty difficulty) {
        EnemyData.Teleport teleport = enemy.movement().teleport().orElseThrow();
        List<Double> landings = landings(enemy, difficulty);
        return landings.getLast()
                + glide(enemy, difficulty)
                + teleport.cycle().orElseThrow().leaveFold()
                + teleport.fade();
    }

    private static double glide(EnemyData enemy, Difficulty difficulty) {
        return UnitRules.hook(enemy, difficulty)
                .flatMap(EnemyData.Hook::teleport)
                .flatMap(EnemyData.TeleportChange::glide)
                .orElse(enemy.movement().teleport().orElseThrow().glide());
    }

    private static double ring(EnemyData enemy, Difficulty difficulty) {
        return UnitRules.hook(enemy, difficulty)
                .flatMap(EnemyData.Hook::teleport)
                .flatMap(EnemyData.TeleportChange::ring)
                .orElse(enemy.movement().teleport().orElseThrow().ring());
    }

    /** The level's rules for its mirror-row teleporters. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.Wave> waves = level.waves();
        boolean any = false;
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            for (LevelData.Group group : wave.groupList()) {
                if (!content.enemies().containsKey(group.enemy())) {
                    continue; // an unknown enemy is the loader's problem
                }
                boolean mirrors = mirrorRow(content.enemy(group.enemy()));
                any |= mirrors;
                if (group.formation().equals(LevelData.Wave.PAIR)
                        && (!mirrors || group.count() != 2 || wave.from() != LevelData.Entry.FRONT)) {
                    problem.accept(
                            "waves[" + i + "].formation",
                            "a pair is two mirror-row teleporters from the front (count: 2, from: front)");
                }
                if (wave.rows().isPresent()
                        && (!mirrors
                                || !(group.formation().equals(LevelData.Wave.PAIR)
                                        || group.formation().equals("single")))) {
                    problem.accept("waves[" + i + "].rows", "rows: a mirror-row teleporter's pair or single");
                }
            }
            if (wave.rows().isPresent()
                    && wave.rows().get().get(1) - wave.rows().get().get(0) < 90) {
                problem.accept("waves[" + i + "].rows", "a pair's rows at least 90 px apart (more than a hull)");
            }
        }
        if (!any) {
            return;
        }
        for (Difficulty difficulty : Difficulty.values()) {
            check(content, level, difficulty, problem);
        }
    }

    private static void check(
            Content content, LevelData level, Difficulty difficulty, BiConsumer<String, String> problem) {
        List<LevelData.Wave> waves = level.waves();
        List<double[]> holds = new ArrayList<>();
        List<Double> landings = new ArrayList<>();
        double end = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            if (!wave.fliesOn(difficulty)) {
                continue;
            }
            for (LevelData.Group group : wave.groupList()) {
                if (!content.enemies().containsKey(group.enemy())) {
                    continue;
                }
                EnemyData enemy = content.enemy(group.enemy());
                if (!mirrorRow(enemy)) {
                    continue;
                }
                if (wave.t() < end) {
                    problem.accept(
                            "waves[" + i + "]",
                            difficulty + ": at most one pair on the screen: t=" + wave.t()
                                    + " enters before the one before it is gone (t=" + end + ")");
                }
                double hold = hold(enemy, difficulty);
                end = Math.max(end, wave.t() + hold);
                holds.add(new double[] {wave.t(), wave.t() + hold});
                for (double landing : landings(enemy, difficulty)) {
                    landings.add(wave.t() + landing);
                }
            }
        }
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            if (!wave.fliesOn(difficulty)) {
                continue;
            }
            Optional<LevelData.Change> change =
                    switch (difficulty) {
                        case EASY -> wave.easy();
                        case MEDIUM -> Optional.<LevelData.Change>empty();
                        case HARD -> wave.hard();
                    };
            LevelData.Entry from = change.flatMap(LevelData.Change::from).orElse(wave.from());
            boolean ambush =
                    wave.groupList().stream().allMatch(g -> g.formation().equals("rear ambush"));
            if (from != LevelData.Entry.FRONT && !ambush) {
                double warned = wave.t()
                        - change.flatMap(LevelData.Change::warning)
                                .or(wave::warning)
                                .orElse(WARNING);
                for (double landing : landings) {
                    if (Math.abs(warned - landing) < WARNING_APART - 1e-9) {
                        problem.accept(
                                "waves[" + i + "].t",
                                difficulty + ": its edge warning at t=" + warned + " within " + WARNING_APART
                                        + " s of a Seraph's landing at t=" + landing);
                    }
                }
            }
            for (double[] hold : holds) {
                if (!(wave.t() >= hold[0] && wave.t() < hold[1])) {
                    continue;
                }
                for (LevelData.Group group : wave.groupList()) {
                    if (!content.enemies().containsKey(group.enemy())) {
                        continue;
                    }
                    EnemyData enemy = content.enemy(group.enemy());
                    if (mirrorRow(enemy)) {
                        continue;
                    }
                    boolean hull = enemy.tier().compareTo(Tier.MEDIUM) >= 0;
                    if (hull && enemy.aura().isEmpty()) {
                        problem.accept(
                                "waves[" + i + "]",
                                difficulty + ": no " + enemy.tier().name().toLowerCase(java.util.Locale.ROOT)
                                        + " hull enters while a pair holds (t=" + hold[0] + "–" + hold[1] + ")");
                    }
                    boolean popcorn = enemy.tier() == Tier.TINY || enemy.tier() == Tier.SMALL;
                    if (popcorn && from == LevelData.Entry.FRONT && !offCentre(wave)) {
                        problem.accept(
                                "waves[" + i + "]",
                                difficulty + ": the popcorn off the centre line while a pair holds (t=" + hold[0]
                                        + "–" + hold[1] + "): an edge, or paths "
                                        + OFF_CENTRE + " px off the centre");
                    }
                }
            }
        }
    }

    /** Whether a front wave keeps off the centre line: by its edge, or its paths' or lanes' points. */
    private static boolean offCentre(LevelData.Wave wave) {
        if (wave.edge().isPresent()) {
            return true;
        }
        List<List<Point>> routes = wave.paths().or(wave::lanes).orElse(List.of());
        return !routes.isEmpty()
                && routes.stream().flatMap(List::stream).allMatch(point -> Math.abs(point.x() - CENTRE) >= OFF_CENTRE);
    }
}
