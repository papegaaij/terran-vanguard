package vanguard.content;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * M7 part H's static checks (design/enemies/bosses/dust-colossus, design/campaign Level 21).
 *
 * <p>A unit's: a {@code breach} only on a boss, without an anchor, a stride, a hover or a straight descent, its body
 * {@code buried} and its vital part starting {@code buried} too; its orders one per boss phase, by name, in the phases'
 * order, the last naming {@code under} only if it holds nothing else; the bite's bullet class known; the boss's {@code
 * suction}, {@code vents}, {@code eruption} and {@code debris} only with a breach, the vents naming its destroyable
 * parts and an attack of its own, the vents', the eruption's and the debris' phases its phases, the eruption's and the
 * debris' bullet class and contact tier known; the hooks {@code breach}, {@code suction} and {@code eruption} only with
 * what they change.
 *
 * <p>A level's: a breaching boss only in a level whose arena moves (it crawls) or stands, arriving at the arena's start
 * in it; a moving arena's {@code ramp} (its ease) only with a breaching boss; at most one breaching boss.
 */
final class ColossusChecks {
    private ColossusChecks() {}

    /** A unit's M7 part H keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        enemy.movement().breach().ifPresentOrElse(breach -> breach(content, enemy, breach, problem), () -> {
            enemy.boss().ifPresent(script -> {
                if (script.suction().isPresent()
                        || script.vents().isPresent()
                        || script.eruption().isPresent()
                        || script.debris().isPresent()) {
                    problem.accept("boss", "suction, vents, an eruption and debris are a breaching boss's");
                }
            });
            for (EnemyData.Hook hook : hooks(enemy)) {
                if (hook.breach().isPresent()
                        || hook.suction().isPresent()
                        || hook.eruption().isPresent()) {
                    problem.accept("difficulty", "the breach, suction and eruption hooks change a breaching boss");
                }
            }
        });
    }

    private static void breach(
            Content content, EnemyData enemy, EnemyData.Breach breach, BiConsumer<String, String> problem) {
        EnemyData.Movement movement = enemy.movement();
        if (enemy.boss().isEmpty()
                || movement.anchored().isPresent()
                || movement.stride().isPresent()
                || movement.hover().isPresent()
                || movement.straight().isPresent()) {
            problem.accept(
                    "movement.breach", "only a boss breaches, without an anchor, a stride, a hover or a descent");
            return;
        }
        if (!enemy.layer().equals("buried")) {
            problem.accept("layer", "a breaching boss's body is buried");
        }
        List<EnemyData.PartData> parts = enemy.partList().orElse(List.of());
        EnemyData.PartData mouth = parts.stream()
                .filter(part -> part.kind().equals("vital"))
                .findFirst()
                .orElse(null);
        if (mouth == null || !mouth.layer().orElse(enemy.layer()).equals("buried")) {
            problem.accept("part_list", "a breaching boss's mouth (its first vital part) starts buried");
        }
        EnemyData.BossData script = enemy.boss().orElseThrow();
        List<EnemyData.PhaseData> phases = script.phases();
        if (breach.orders().size() != phases.size()) {
            problem.accept("movement.breach.orders", "one order per boss phase (" + phases.size() + ")");
        }
        for (int i = 0; i < breach.orders().size(); i++) {
            EnemyData.BreachOrder order = breach.orders().get(i);
            if (i < phases.size() && !order.phase().equals(phases.get(i).name())) {
                problem.accept(
                        "movement.breach.orders[" + i + "].phase",
                        "the orders follow the boss's phases: '" + phases.get(i).name() + "', was '" + order.phase()
                                + "'");
            }
            if (order.points().contains(ColossusBlock.UNDER) && order.points().size() > 1) {
                problem.accept("movement.breach.orders[" + i + "].points", "under the ship is a phase's only point");
            }
        }
        if (!bullet(content, breach.bite().bullet())) {
            problem.accept(
                    "movement.breach.bite.bullet",
                    "no bullet class '" + breach.bite().bullet() + "'");
        }
        script.vents().ifPresent(vents -> {
            for (String part : vents.parts()) {
                int index = enemy.partIndex(part);
                if (index < 0 || !parts.get(index).kind().equals("destroyable")) {
                    problem.accept("boss.vents.parts", "'" + part + "' is no destroyable part");
                }
            }
            if (enemy.attacks().stream()
                    .noneMatch(attack ->
                            attack.name().filter(vents.attack()::equals).isPresent())) {
                problem.accept("boss.vents.attack", "no attack '" + vents.attack() + "'");
            }
            phase(script, vents.phase(), "boss.vents.phase", problem);
        });
        script.eruption().ifPresent(eruption -> {
            if (!bullet(content, eruption.bullet())) {
                problem.accept("boss.eruption.bullet", "no bullet class '" + eruption.bullet() + "'");
            }
            phase(script, eruption.phase(), "boss.eruption.phase", problem);
        });
        script.debris()
                .ifPresent(debris ->
                        debris.every().keySet().forEach(name -> phase(script, name, "boss.debris.every", problem)));
    }

    private static void phase(
            EnemyData.BossData script, String name, String field, BiConsumer<String, String> problem) {
        if (script.phases().stream().noneMatch(phase -> phase.name().equals(name))) {
            problem.accept(field, "no phase '" + name + "'");
        }
    }

    private static List<EnemyData.Hook> hooks(EnemyData enemy) {
        return enemy.difficulty()
                .map(hooks -> java.util.stream.Stream.of(hooks.easy(), hooks.hard())
                        .flatMap(java.util.Optional::stream)
                        .toList())
                .orElse(List.of());
    }

    private static boolean bullet(Content content, String bullet) {
        return content.enemyBasis().bulletDamage().stream()
                .anyMatch(b -> b.bullet().equals(bullet));
    }

    /** A level's M7 part H keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        boolean breaching = ColossusRules.breaching(content, level);
        List<LevelData.Section> sections = level.sections();
        int arena = -1;
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            arena = section.isArena() ? i : arena;
            boolean moving = section.speed().filter(speed -> speed > 0).isPresent();
            if (section.isArena() && moving && section.ramp().isPresent() && !breaching) {
                problem.accept(
                        "sections[" + i + "].ramp",
                        "only a breaching boss's arena eases in (it crawls); another moving arena changes at once");
            }
        }
        if (!breaching) {
            return;
        }
        LevelData.BossPlacement placement = level.boss().orElseThrow();
        if (arena < 1) {
            problem.accept("sections", "a breaching boss is fought in an arena after a section that scrolls");
            return;
        }
        if (sections.get(arena).speed().filter(speed -> speed >= 0).isEmpty()) {
            problem.accept("sections[" + arena + "].speed", "a breaching boss's arena gives its speed (it crawls)");
        }
        double start = level.sectionStart(arena);
        if (Math.abs(placement.t() - start) > 1e-9 || placement.section() != arena + 1) {
            problem.accept(
                    "boss.t",
                    "a breaching boss arrives at its arena's start (" + start + ", section " + (arena + 1) + ")");
        }
    }
}
