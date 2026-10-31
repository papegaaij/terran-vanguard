package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * M10 part A's static checks (design/enemies, Reflect, Regeneration, The three-way war; design/enemies/ground/
 * graft-turret and shield-pylon; design/enemies/air/harrow; design/player/systems, the IFF spoofer).
 *
 * <p>A unit's: a {@code mirror} only on a single {@code air} unit (no segment chain, flock or parts), its bullet a known
 * class; a {@code regrowth} or a {@code dome} only on a single fixed {@code ground} unit ({@code movement: terrain}, no
 * walk), never both, the root inside the unit's sprite; at most one {@code death-burst} attack, its bullet a known class,
 * never beside a hook's {@code death_burst}; a hook's {@code mirror}, {@code regeneration}, {@code regrowth}, {@code
 * dome} or {@code attacks.<name>.speed} only for what the unit has; {@code spoofable} only on a {@code ground} unit that
 * is neither a boss nor of the Vrell (never aircraft, bosses or the Vrell; a hybrid only by its document's flag).
 *
 * <p>A level's: a wave's {@code hunts} (other than Level 33's {@code prey}) names the {@code tag} of another of its
 * waves; the prey wave flies units of the Jovian Ascendancy that are not bosses or neutral craft (never the hybrids,
 * bosses or allies), the hunting wave units of the Vrell; a ground target's {@code crown} only on a nest of a unit with a
 * dome.
 */
final class JovianChecks {
    /** The faction whose units hunt in the three-way war (design/enemies, Factions). */
    static final String VRELL = "Vrell";

    /** The faction whose waves are prey in the three-way war. */
    static final String ASCENDANCY = "Jovian Ascendancy";

    private JovianChecks() {}

    /** A unit's Act 6 keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        boolean single = enemy.partList().isEmpty() && enemy.segmentChain().isEmpty();
        enemy.mirror().ifPresent(mirror -> {
            if (!enemy.layer().equals("air")
                    || !single
                    || enemy.movement().flock().isPresent()
                    || enemy.boss().isPresent()) {
                problem.accept("mirror", "a mirror is a single air unit's (no parts, chain, flock or boss)");
            }
            if (!content.enemyBasis().knowsBullet(mirror.bullet())) {
                problem.accept("mirror.bullet", "unknown bullet class '" + mirror.bullet() + "'");
            }
        });
        for (String key : List.of("regrowth", "dome")) {
            boolean has = key.equals("regrowth")
                    ? enemy.regrowth().isPresent()
                    : enemy.dome().isPresent();
            if (has
                    && (!enemy.layer().equals("ground")
                            || enemy.movement().terrain().isEmpty()
                            || enemy.movement().walk().isPresent()
                            || !single
                            || enemy.boss().isPresent())) {
                problem.accept(
                        key, "a " + key + " is a single fixed ground unit's (movement: terrain, no walk or parts)");
            }
        }
        if (enemy.regrowth().isPresent() && enemy.dome().isPresent()) {
            problem.accept("dome", "a pylon (dome) does not regrow");
        }
        enemy.regrowth().map(EnemyData.Regrowth::root).ifPresent(root -> {
            if (root.hitbox().width() > enemy.size().width()
                    || root.hitbox().height() > enemy.size().height()) {
                problem.accept("regrowth.root.hitbox", "the root lies under the turret, inside its sprite");
            }
        });
        List<EnemyData.Attack> rings =
                enemy.attacks().stream().filter(EnemyData.Attack::deathBurst).toList();
        if (rings.size() > 1) {
            problem.accept("attacks", "a unit has at most one death-burst");
        }
        for (EnemyData.Attack ring : rings) {
            ring.bullet()
                    .filter(bullet -> !content.enemyBasis().knowsBullet(bullet))
                    .ifPresent(bullet -> problem.accept("attacks", "unknown bullet class '" + bullet + "'"));
        }
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
            String field = "difficulty." + difficulty.name().toLowerCase(java.util.Locale.ROOT);
            if (!rings.isEmpty() && hook.flatMap(EnemyData.Hook::deathBurst).isPresent()) {
                problem.accept(field + ".death_burst", "a unit with a death-burst attack changes it by attacks.<name>");
            }
            hook(problem, field + ".mirror", hook.flatMap(EnemyData.Hook::mirror), enemy.mirror());
            hook(problem, field + ".regeneration", hook.flatMap(EnemyData.Hook::regeneration), enemy.regeneration());
            hook(problem, field + ".regrowth", hook.flatMap(EnemyData.Hook::regrowth), enemy.regrowth());
            hook(problem, field + ".dome", hook.flatMap(EnemyData.Hook::dome), enemy.dome());
            hook.flatMap(EnemyData.Hook::attacks)
                    .ifPresent(changes -> changes.forEach((name, change) -> {
                        boolean ring = rings.stream()
                                .anyMatch(attack ->
                                        attack.name().filter(name::equals).isPresent());
                        if (change.speed().isPresent() && !ring) {
                            problem.accept(
                                    field + ".attacks." + name + ".speed",
                                    "a speed changes a death-burst's ring, by its name");
                        }
                    }));
        }
        if (enemy.isSpoofable()
                && (!enemy.layer().equals("ground")
                        || enemy.boss().isPresent()
                        || enemy.faction().equals(VRELL))) {
            problem.accept(
                    "spoofable",
                    "the IFF spoofer fools ground turrets only: never aircraft, bosses or the Vrell (design/player/"
                            + "systems)");
        }
    }

    private static void hook(BiConsumer<String, String> problem, String field, Optional<?> change, Optional<?> stat) {
        if (change.isPresent() && stat.isEmpty()) {
            problem.accept(field, "changes what the unit does not have");
        }
    }

    /** A level's Act 6 keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.Wave> waves = level.waves();
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            Optional<String> hunts = wave.hunts().filter(JovianRules::preyTag);
            if (hunts.isEmpty()) {
                continue;
            }
            String field = "waves[" + i + "].hunts";
            String tag = hunts.get();
            List<LevelData.Wave> prey = waves.stream()
                    .filter(other ->
                            other != wave && other.tag().filter(tag::equals).isPresent())
                    .toList();
            if (prey.isEmpty()) {
                problem.accept(field, "hunts: prey or another wave's tag; it names no other wave's tag: '" + tag + "'");
            }
            prey.stream()
                    .flatMap(other -> units(content, other))
                    .filter(unit ->
                            !unit.faction().equals(ASCENDANCY) || unit.boss().isPresent() || unit.isNeutral())
                    .forEach(unit -> problem.accept(
                            field,
                            "prey is an Ascendancy wave (never a hybrid, a boss or an ally): '" + unit.name() + "'"));
            units(content, wave)
                    .filter(unit -> !unit.faction().equals(VRELL))
                    .forEach(
                            unit -> problem.accept(field, "the Vrell hunt (the three-way war): '" + unit.name() + "'"));
        }
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.crown().isPresent()
                    && target.enemy()
                            .filter(content.enemies()::containsKey)
                            .map(slug -> content.enemy(slug).dome().isEmpty())
                            .orElse(false)) {
                problem.accept("ground_targets[" + i + "].crown", "only a nest of a unit with a dome turns its crown");
            }
        }
    }

    /** The stat blocks a wave's groups fly (the known ones). */
    private static Stream<EnemyData> units(Content content, LevelData.Wave wave) {
        return wave.groupList().stream()
                .map(LevelData.Group::enemy)
                .filter(content.enemies()::containsKey)
                .map(content::enemy);
    }
}
