package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M9 part F's static checks (design/campaign Level 33 Leech Field; design/enemies/space/void-leech and
 * design/enemies/ground/rail-bunker).
 *
 * <p>A unit's: a latch's {@code drains: power} with a {@code drain} attack without a rate, a shield drain's attack with
 * its rate (the latch's {@code drains}, {@code emp} and {@code prey_drift} values are the record's own checks); a
 * chase's {@code drift} only on a unit that latches; the hook {@code chase.speed} only on a chaser.
 *
 * <p>A level's: a {@code prey} wave flies a V-wing or a line abreast of a plain air unit without a latch; a {@code
 * hunts: prey} wave's unit latches with a {@code prey_drift}, and a prey wave comes at most {@value #HUNT_WITHIN} s
 * before it (at or before its {@code t}). Not a check: at most 6 free leeches on the screen (Level 33's test).
 */
final class LeechChecks {
    /** A hunting wave comes at most this long after its prey wave, s (the hold's 5 s plus the wing's way in). */
    static final double HUNT_WITHIN = 10;

    private LeechChecks() {}

    /** A unit's part-F keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        Optional<EnemyData.Latch> latch = enemy.latch();
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            if (!attack.pattern().equals(PartFRules.DRAIN)) {
                continue;
            }
            boolean power = latch.map(EnemyData.Latch::drainsPower).orElse(false);
            if (power && attack.rate().isPresent()) {
                problem.accept(
                        "attacks[" + i + "].rate",
                        "a latch that drains the generator's output (drains: power) drains no shield points: no rate");
            }
            if (!power && attack.rate().isEmpty()) {
                problem.accept("attacks[" + i + "].rate", "a shield drain has its rate (shield points a second)");
            }
        }
        boolean drifts =
                enemy.movement().chase().flatMap(EnemyData.Chase::drift).isPresent();
        if (drifts && latch.isEmpty()) {
            problem.accept("movement.chase.drift", "a drifting chaser latches on (a latch block): the Void Leech");
        }
        boolean speedHook = enemy.difficulty()
                .map(hooks -> java.util.stream.Stream.of(hooks.easy(), hooks.hard())
                        .flatMap(Optional::stream)
                        .anyMatch(change -> change.chase()
                                .flatMap(EnemyData.ChaseChange::speed)
                                .isPresent()))
                .orElse(false);
        if (speedHook && enemy.movement().chase().isEmpty()) {
            problem.accept("difficulty", "chase.speed changes a chase the unit does not have");
        }
    }

    /** A level's part-F keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.Wave> waves = level.waves();
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            // M10 part A: a wave hunting a prey wave's tag is the three-way war's (JovianChecks), not a leech wave.
            boolean leeches = wave.hunts().filter(PartFRules.HUNTS_PREY::equals).isPresent();
            if (wave.prey().isEmpty() && !leeches) {
                continue;
            }
            String field = "waves[" + i + "]";
            for (LevelData.Group group : wave.groupList()) {
                if (!content.enemies().containsKey(group.enemy())) {
                    continue;
                }
                EnemyData enemy = content.enemy(group.enemy());
                if (wave.prey().isPresent()) {
                    if (!group.formation().equals("V-wing")
                            && !group.formation().equals("line abreast")) {
                        problem.accept(
                                field + ".prey",
                                "a prey wing holds as a V-wing or a line abreast: " + group.formation());
                    }
                    if (!enemy.layer().equals("air")
                            || enemy.latch().isPresent()
                            || enemy.movement().chase().isPresent()
                            || enemy.segmentChain().isPresent()) {
                        problem.accept(
                                field + ".prey", "prey is a plain air unit without a latch: '" + group.enemy() + "'");
                    }
                }
                if (leeches && enemy.latch().flatMap(EnemyData.Latch::preyDrift).isEmpty()) {
                    problem.accept(
                            field + ".hunts",
                            "a hunting unit latches on prey (latch.prey_drift): '" + group.enemy() + "'");
                }
            }
            if (leeches) {
                double t = wave.t();
                boolean prey = waves.stream()
                        .anyMatch(other -> other.prey().isPresent() && other.t() <= t && t - other.t() <= HUNT_WITHIN);
                if (!prey) {
                    problem.accept(
                            field + ".hunts",
                            "a hunting wave follows a prey wave within " + HUNT_WITHIN + " s (prey: ...)");
                }
            }
        }
        // The Rail Bunkers' staggered first lines: a nest of a unit with a laser-line.
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.staggered().isPresent()
                    && target.enemy()
                            .filter(content.enemies()::containsKey)
                            .map(slug -> content.enemy(slug).attacks().stream().noneMatch(BeltRules::laserLine))
                            .orElse(false)) {
                problem.accept(
                        "ground_targets[" + i + "].staggered",
                        "only a nest of a unit with a laser-line staggers its first lines");
            }
        }
    }
}
