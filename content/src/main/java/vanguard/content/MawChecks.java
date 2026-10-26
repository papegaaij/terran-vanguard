package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M8 part H's static checks (design/enemies/bosses/abyssal-maw, design/campaign Level 28).
 *
 * <p>A unit's: a {@code boss.coil} only on an anchored boss without lanes, a stride or a breach, every part starting
 * {@code sub} (the coil rises from the dark), its head (the first vital part) carrying a {@code while: open} weak spot
 * (the throat); its sacs destroyable parts, its implants parts that are not vital; the fan wall one of its {@code fan}
 * attacks, the implants' attack one of its {@code aimed} ones; every phase it names one of the boss's; the bullet classes
 * known; the shed unit a known segment chain; the hook {@code coil_arena} only with a coil.
 *
 * <p>A level's: the {@code cruisers} and the {@code armour_floor} only in a level with a boss, the cruisers' run after
 * the boss's arrival and ending by the level's end.
 */
final class MawChecks {
    private MawChecks() {}

    /** A unit's M8 part H keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        Optional<EnemyData.CoilData> found = enemy.boss().flatMap(EnemyData.BossData::coil);
        if (found.isEmpty()) {
            for (EnemyData.Hook hook : hooks(enemy)) {
                if (hook.coilArena().isPresent()) {
                    problem.accept("difficulty", "the coil_arena hook changes a coiled boss (boss.coil)");
                }
            }
            return;
        }
        EnemyData.CoilData coil = found.get();
        EnemyData.BossData script = enemy.boss().orElseThrow();
        EnemyData.Movement movement = enemy.movement();
        if (movement.anchored().isEmpty()
                || script.lanes().isPresent()
                || movement.stride().isPresent()
                || movement.breach().isPresent()) {
            problem.accept("boss.coil", "a coiled boss is anchored, without lanes, a stride or a breach");
        }
        List<EnemyData.PartData> parts = enemy.partList().orElse(List.of());
        for (int i = 0; i < parts.size(); i++) {
            if (!parts.get(i).layer().orElse(enemy.layer()).equals("sub")) {
                problem.accept(
                        "part_list[" + i + "].layer", "a coiled boss's parts start sub (they rise from the dark)");
            }
        }
        EnemyData.PartData head = parts.stream()
                .filter(part -> part.kind().equals("vital"))
                .findFirst()
                .orElse(null);
        if (head == null
                || head.spots().orElse(List.of()).stream()
                        .noneMatch(spot -> spot.during().isPresent())) {
            problem.accept(
                    "part_list", "a coiled boss's head (its first vital part) has a while: open spot (the throat)");
        }
        for (String sac : coil.sacs().parts()) {
            int index = enemy.partIndex(sac);
            if (index < 0 || !parts.get(index).kind().equals("destroyable")) {
                problem.accept("boss.coil.sacs.parts", "'" + sac + "' is no destroyable part");
            }
        }
        coil.wall().ifPresent(wall -> attack(enemy, wall.attack(), "fan", "boss.coil.wall.attack", problem));
        bullet(content, coil.sweeps().bullet(), "boss.coil.sweeps.bullet", problem);
        for (EnemyData.SweepPhase phase : coil.sweeps().phases()) {
            phase(script, phase.phase(), "boss.coil.sweeps.phases", problem);
        }
        coil.dive().ifPresent(dive -> {
            phase(script, dive.phase(), "boss.coil.dive.phase", problem);
            bullet(content, dive.bullet(), "boss.coil.dive.bullet", problem);
        });
        coil.implants().ifPresent(implants -> {
            for (String implant : implants.parts()) {
                int index = enemy.partIndex(implant);
                if (index < 0 || parts.get(index).kind().equals("vital")) {
                    problem.accept("boss.coil.implants.parts", "'" + implant + "' is no part other than the head");
                }
            }
            phase(script, implants.phase(), "boss.coil.implants.phase", problem);
            attack(enemy, implants.attack(), "aimed", "boss.coil.implants.attack", problem);
        });
        coil.shed().ifPresent(shed -> {
            EnemyData unit = content.enemies().get(shed.enemy());
            if (unit == null || unit.segmentChain().isEmpty()) {
                problem.accept("boss.coil.shed.enemy", "'" + shed.enemy() + "' is no known segment chain");
            }
            for (EnemyData.ShedPhase phase : shed.phases()) {
                phase(script, phase.phase(), "boss.coil.shed.phases", problem);
            }
        });
    }

    private static void attack(
            EnemyData enemy, String name, String pattern, String field, BiConsumer<String, String> problem) {
        if (enemy.attack(name).filter(a -> a.pattern().equals(pattern)).isEmpty()) {
            problem.accept(field, "no " + pattern + " attack '" + name + "'");
        }
    }

    private static void phase(
            EnemyData.BossData script, String name, String field, BiConsumer<String, String> problem) {
        if (script.phases().stream().noneMatch(phase -> phase.name().equals(name))) {
            problem.accept(field, "no phase '" + name + "'");
        }
    }

    private static void bullet(Content content, String bullet, String field, BiConsumer<String, String> problem) {
        if (!content.enemyBasis().knowsBullet(bullet)) {
            problem.accept(field, "no bullet class '" + bullet + "'");
        }
    }

    private static List<EnemyData.Hook> hooks(EnemyData enemy) {
        return enemy.difficulty()
                .map(hooks -> java.util.stream.Stream.of(hooks.easy(), hooks.hard())
                        .flatMap(Optional::stream)
                        .toList())
                .orElse(List.of());
    }

    /** A level's M8 part H keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.armourFloor().isPresent() && level.boss().isEmpty()) {
            problem.accept("armour_floor", "the armour floor holds from a boss's kill: only in a level with a boss");
        }
        level.cruisers().ifPresent(cruisers -> {
            if (level.boss().isEmpty()) {
                problem.accept("cruisers", "the cruisers' run follows a boss: only in a level with a boss");
            } else if (cruisers.rise() < level.boss().get().t()) {
                problem.accept("cruisers.rise", "the cruisers rise after the boss's arrival");
            }
            double end = level.sections().getLast().end();
            if (cruisers.end() + cruisers.fall() > end + 1e-9) {
                problem.accept("cruisers.end", "the run and the fall end by the level's end (" + end + " s)");
            }
            for (Difficulty difficulty : Difficulty.values()) {
                LevelData.Cruisers on = cruisers.on(difficulty);
                for (String bullet : List.of(
                        on.broadside().bullet(),
                        on.flak().bullet(),
                        on.flak().ringBullet(),
                        on.pointDefence().bullet())) {
                    bullet(content, bullet, "cruisers", problem);
                }
                if (on.broadside().doubleAfter().isPresent()
                        && 2 * on.broadside().band() + on.broadside().free() > vanguard.sim.PlayField.HEIGHT) {
                    problem.accept("cruisers.broadside", "two bands and the free band between them fit the field");
                }
            }
        });
    }
}
