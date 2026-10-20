package vanguard.content;

import java.util.List;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M7 part F's static checks (design/campaign Level 19, Foundry of Tharsis; design/enemies/ground/warden-tank and
 * bosses/revenant-walker).
 *
 * <p>A unit's: a {@code turret} only on a walker (movement {@code walk}) that is not a boss and fires one aimed burst,
 * a {@code rear_plate} only on a walker; a {@code stride} only on a boss, without a hover, an anchor or a straight
 * descent; a boss's {@code plate} and {@code cone} only with a stride, the plate on a part of it, the cone's bullet a
 * known class; an attack's {@code parts} only a boss's, naming its parts; {@code homing} only a boss's (the walker's
 * pods) and not on a fan; a fan's {@code burst} only a boss's (round 43); {@code aim: facing} on a boss attack only
 * for a walking boss; a phase's {@code until.share} naming its parts; the hooks {@code turret}, {@code homing} and
 * {@code cone} only with what they change.
 *
 * <p>A level's: its belt runs inside the level and not in or after its arena's halt (a run may reach past it), no
 * standing ground nest's placement on a belt's column while the belt passes (it would ride it); its vent rows inside
 * the level before the arena, their grates on the play field and apart, every difficulty's period holding the row's
 * whole ripple, the bullet class known; {@code arc: up} only on a nest whose unit fires within an arc; {@code
 * hold_after_boss} only in a level with a boss; {@code stems: motif} only with the music's {@code motif}; an escapes
 * objective's {@code allowed} fewer than the units it counts.
 */
final class FoundryChecks {
    private FoundryChecks() {}

    /** A unit's M7 part F keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        boolean walker = enemy.movement().walk().isPresent();
        boolean boss = enemy.boss().isPresent();
        if (enemy.turret().isPresent()) {
            if (!walker || boss) {
                problem.accept("turret", "a turret turns on a walker's hull (movement walk), not on a boss");
            }
            long aimed = enemy.attacks().stream()
                    .filter(attack -> attack.pattern().equals("aimed"))
                    .count();
            if (aimed != 1 || enemy.attacks().size() != 1) {
                problem.accept("turret", "a turret fires its unit's one aimed attack along its aim");
            }
        }
        if (enemy.rearPlate().isPresent() && !walker) {
            problem.accept("rear_plate", "a rear plate is a walker's (movement walk): its rear is its facing's back");
        }
        if (enemy.walks()) {
            EnemyData.Movement movement = enemy.movement();
            if (!boss
                    || movement.anchored().isPresent()
                    || movement.hover().isPresent()
                    || movement.straight().isPresent()) {
                problem.accept("movement.stride", "only a boss walks passes, without an anchor, a hover or a descent");
            }
        }
        enemy.boss().ifPresent(script -> {
            script.plate().ifPresent(plate -> {
                if (!enemy.walks()) {
                    problem.accept("boss.plate", "a front plate is a walking boss's (movement stride)");
                }
                if (enemy.partIndex(plate.part()) < 0) {
                    problem.accept("boss.plate.part", "no part '" + plate.part() + "'");
                }
            });
            script.cone().ifPresent(cone -> {
                if (!enemy.walks()) {
                    problem.accept("boss.cone", "a flame cone is a walking boss's (movement stride)");
                }
                if (!bullet(content, cone.bullet())) {
                    problem.accept("boss.cone.bullet", "no bullet class '" + cone.bullet() + "'");
                }
            });
            for (int i = 0; i < script.phases().size(); i++) {
                int f = i;
                script.phases().get(i).until().share().ifPresent(share -> share.parts()
                        .forEach(part -> {
                            if (enemy.partIndex(part) < 0) {
                                problem.accept("boss.phases[" + f + "].until.share.parts", "no part '" + part + "'");
                            }
                        }));
            }
        });
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            String field = "attacks[" + i + "]";
            if (attack.parts().isPresent()) {
                if (!boss) {
                    problem.accept(field + ".parts", "only a boss's attack lists the parts that fire it");
                }
                for (String part : attack.parts().get()) {
                    if (enemy.partIndex(part) < 0) {
                        problem.accept(field + ".parts", "no part '" + part + "'");
                    }
                }
            }
            if (attack.homing().isPresent() && !boss) {
                problem.accept(field + ".homing", "only a boss's parts fire homing missiles (the walker's pods)");
            }
            if (!boss
                    && attack.pattern().equals("fan")
                    && (attack.burst().isPresent() || attack.burstGap().isPresent())) {
                problem.accept(field + ".burst", "only a boss's fan comes in a burst (round 43: the walker's)");
            }
            if (boss && attack.aim().filter("facing"::equals).isPresent() && !enemy.walks()) {
                problem.accept(field + ".aim", "a boss fires along its facing only when it walks (movement stride)");
            }
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::turret).isPresent()
                    && enemy.turret().isEmpty()) {
                problem.accept("difficulty.turret", "a turret's turn rate without a turret");
            }
            if (hook.flatMap(EnemyData.Hook::homing).isPresent()
                    && enemy.attacks().stream()
                            .noneMatch(attack -> attack.homing().isPresent())) {
                problem.accept("difficulty.homing", "a homing turn rate without homing missiles");
            }
            if (hook.flatMap(EnemyData.Hook::cone).isPresent()
                    && enemy.boss().flatMap(EnemyData.BossData::cone).isEmpty()) {
                problem.accept("difficulty.cone", "a flame cone's change without a flame cone");
            }
        }
    }

    /** A level's M7 part F keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        double end = level.sections().getLast().end();
        double arena = arenaStart(level);
        level.belts().ifPresent(belts -> {
            for (int i = 0; i < belts.runs().size(); i++) {
                LevelData.BeltRun run = belts.runs().get(i);
                if (run.from() >= end) {
                    problem.accept("belts.runs[" + i + "].from", "a belt run enters inside the level");
                }
                if (run.from() >= arena && run.from() < arenaEnd(level)) {
                    problem.accept("belts.runs[" + i + "].from", "a belt run enters outside the arena's halt");
                }
                checkNestsOffBelt(content, level, belts, run, i, problem);
            }
        });
        level.vents().ifPresent(vents -> {
            if (!bullet(content, vents.bullet())) {
                problem.accept("vents.bullet", "no bullet class '" + vents.bullet() + "'");
            }
            for (int i = 0; i < vents.rows().size(); i++) {
                LevelData.VentRowData row = vents.rows().get(i);
                String field = "vents.rows[" + i + "]";
                if (row.t() >= Math.min(end, arena)) {
                    problem.accept(field + ".t", "a vent row enters before the arena (they are cold from it on)");
                }
                List<Double> xs = row.xs().stream().sorted().toList();
                for (int k = 0; k < xs.size(); k++) {
                    if (xs.get(k) - vents.width() / 2 < 0 || xs.get(k) + vents.width() / 2 > PlayField.WIDTH) {
                        problem.accept(field + ".xs", "a grate lies on the play field");
                    }
                    if (k > 0 && xs.get(k) - xs.get(k - 1) < vents.width()) {
                        problem.accept(field + ".xs", "the grates lie at least their width apart");
                    }
                }
                for (Difficulty difficulty : Difficulty.values()) {
                    double whole = vents.tellOn(difficulty)
                            + vents.burn()
                            + vents.rippleOn(difficulty) * (row.xs().size() - 1);
                    if (vents.periodOn(difficulty) < whole - 1e-9) {
                        problem.accept(
                                field + ".xs",
                                difficulty + ": the period " + vents.periodOn(difficulty)
                                        + " s holds the whole ripple (" + whole + " s)");
                    }
                }
            }
        });
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.arc().isPresent()
                    && target.enemy().filter(content.enemies()::containsKey).isPresent()
                    && content.enemy(target.enemy().get()).attacks().stream()
                            .noneMatch(attack -> attack.arc().isPresent())) {
                problem.accept("ground_targets[" + i + "].arc", "arc: up turns a nest whose unit fires within an arc");
            }
        }
        for (int i = 0; i < level.sections().size(); i++) {
            if (level.sections().get(i).holdAfterBoss().isPresent()
                    && level.boss().isEmpty()) {
                problem.accept("sections[" + i + "].hold_after_boss", "a hold after the boss needs the level's boss");
            }
        }
        LevelData.Music music = level.music();
        boolean motifAlone = music.stems()
                .map(map -> map.containsValue(LevelData.Music.Stems.MOTIF))
                .orElse(false);
        if (motifAlone && music.motif().isEmpty()) {
            problem.accept("music.stems", "stems: motif plays the motif layer alone: the music needs its motif");
        }
        level.objectives()
                .secondary()
                .filter(secondary -> secondary.allowed().isPresent())
                .ifPresent(secondary -> {
                    String slug = secondary.escapes().orElse("");
                    int units = level.waves().stream()
                            .filter(wave -> wave.fliesOn(Difficulty.MEDIUM))
                            .flatMap(wave -> wave.groupList().stream())
                            .filter(group -> group.enemy().equals(slug))
                            .mapToInt(LevelData.Group::count)
                            .sum();
                    if (secondary.escapesAllowed() >= units) {
                        problem.accept(
                                "objectives.secondary.allowed",
                                "fewer may get away than the " + units + " '" + slug + "' the level flies");
                    }
                });
    }

    /**
     * No standing enemy nest is placed on a belt's column while the run passes the top edge: it would ride the belt
     * (the gantry nests hang beside the belts).
     */
    private static void checkNestsOffBelt(
            Content content,
            LevelData level,
            LevelData.Belts belts,
            LevelData.BeltRun run,
            int index,
            BiConsumer<String, String> problem) {
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isEmpty() || target.side().isPresent()) {
                continue;
            }
            for (LevelData.Placement placement : target.at()) {
                boolean column = Math.abs(placement.x() - run.x()) < belts.width() / 2;
                if (column && placement.t() >= run.from() && placement.t() <= run.to()) {
                    problem.accept(
                            "ground_targets." + target.target(),
                            "a nest at t=" + placement.t() + ", x " + placement.x() + " stands on belt run " + index
                                    + " (it would ride it)");
                }
            }
        }
    }

    private static boolean bullet(Content content, String bullet) {
        return content.enemyBasis().bulletDamage().stream()
                .anyMatch(b -> b.bullet().equals(bullet));
    }

    /** The level time the arena section starts, or infinity without one. */
    private static double arenaStart(LevelData level) {
        for (int i = 0; i < level.sections().size(); i++) {
            if (level.sections().get(i).isArena()) {
                return level.sectionStart(i);
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    /** The level time the arena section ends, or infinity without one. */
    private static double arenaEnd(LevelData level) {
        return level.sections().stream()
                .filter(LevelData.Section::isArena)
                .map(LevelData.Section::end)
                .findFirst()
                .orElse(Double.POSITIVE_INFINITY);
    }
}
