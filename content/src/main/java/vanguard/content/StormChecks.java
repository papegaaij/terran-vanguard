package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M7 part E's static checks (design/campaign Level 18, Dust Storm; design/enemies/ground/dust-devil and
 * air/ghost-drone).
 *
 * <p>A unit's: a {@code vortex} (the spin cycle) only on a {@code ground} unit, with the armour {@code vanes} and
 * only with it; a {@code pull} only with a vortex; an attack fired {@code while: open} only with a vortex, and a
 * spiral on a unit that is not a boss only {@code while: open} (it fires in the window); a {@code lane_strafe} only
 * on an {@code air} unit; an attack's pass {@code trigger} only with a lane strafe, a {@code tell} only with a
 * trigger; the hooks {@code vortex}, {@code pull} and {@code second_pass} only with what they change; a unit kept
 * {@code unclassified_until_flown} first flies in its {@code first_level} and in no level before it.
 *
 * <p>A level's: its {@code visibility} radii on every difficulty reach at least the pull on the ship of every unit
 * it flies (what pulls the ship is always seen; the hard back wall's 130 equals the Dust Devil's 130), its ramps
 * inside the level; the music's {@code motif} starts inside a section the theme plays and turns off only sections
 * the level has.
 */
final class StormChecks {
    private StormChecks() {}

    /** A unit's M7 part E keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        boolean vortex = enemy.vortex().isPresent();
        if (vortex && !enemy.layer().equals("ground")) {
            problem.accept("vortex", "a spin cycle rises from the ground: only on a ground unit");
        }
        if (vortex != enemy.armour().vanes()) {
            problem.accept("armour", "the armour 'vanes' and a vortex go together");
        }
        if (enemy.pull().isPresent() && !vortex) {
            problem.accept("pull", "only a vortex pulls (a pull without a vortex)");
        }
        boolean laneStrafe = enemy.movement().laneStrafe().isPresent();
        if (laneStrafe && !enemy.layer().equals("air")) {
            problem.accept("movement.lane_strafe", "only an air unit strafes in a lane");
        }
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            String field = "attacks[" + i + "]";
            if (attack.whileOpen() && !vortex) {
                problem.accept(field + ".while", "'while: open' needs a vortex (a spin cycle's open window)");
            }
            if (attack.pattern().equals("spiral") && enemy.boss().isEmpty() && !attack.whileOpen()) {
                problem.accept(field, "a spiral on a unit that is not a boss fires in its window ('while: open')");
            }
            if (attack.trigger().isPresent() && !laneStrafe) {
                problem.accept(field + ".trigger", "a pass trigger needs a lane strafe (once per pass)");
            }
            if (attack.tell().isPresent() && attack.trigger().isEmpty()) {
                problem.accept(field + ".tell", "a tell comes before a triggered burst (a tell without a trigger)");
            }
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::vortex).isPresent() && !vortex) {
                problem.accept("difficulty.vortex", "a vortex hook without a vortex");
            }
            if (hook.flatMap(EnemyData.Hook::pull).isPresent() && enemy.pull().isEmpty()) {
                problem.accept("difficulty.pull", "a pull hook without a pull");
            }
            if (hook.flatMap(EnemyData.Hook::secondPass).isPresent() && !laneStrafe) {
                problem.accept("difficulty.second_pass", "a second pass without a lane strafe");
            }
        }
        if (enemy.unclassifiedUntilFlown()) {
            unclassified(content, enemy, problem);
        }
    }

    /** An unclassified unit first flies in its first level, and in no level before it. */
    private static void unclassified(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        String slug = content.enemies().entrySet().stream()
                .filter(entry -> entry.getValue() == enemy)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(enemy.name());
        Optional<String> first = content.levelKey(enemy.firstLevel());
        if (first.isPresent() && !flies(content.levels().get(first.get()), slug)) {
            problem.accept(
                    "intel.unclassified_until_flown",
                    "kept unclassified until its first level is flown, but Level " + enemy.firstLevel() + " flies no '"
                            + slug + "'");
        }
        content.levels().forEach((key, level) -> {
            if (Content.levelNumber(key) < enemy.firstLevel() && flies(level, slug)) {
                problem.accept(
                        "intel.unclassified_until_flown",
                        key + " flies it before its first level " + enemy.firstLevel());
            }
        });
    }

    /** Whether a wave of {@code level} flies {@code slug}, or (M7 part F: the Revenant Walker) its boss is it. */
    private static boolean flies(LevelData level, String slug) {
        return level.waves().stream().flatMap(wave -> wave.groupList().stream()).anyMatch(group -> group.enemy()
                        .equals(slug))
                || level.boss().filter(boss -> boss.enemy().equals(slug)).isPresent();
    }

    /** A level's M7 part E keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        level.visibility().ifPresent(visibility -> visibility(content, level, visibility, problem));
        level.music().motif().ifPresent(motif -> {
            int sections = level.sections().size();
            double end = level.sections().get(sections - 1).end();
            int section = level.sectionAt(motif.from());
            if (motif.from() >= end || section < level.music().startSection()) {
                problem.accept(
                        "music.motif.from",
                        "the motif starts inside a section the theme plays (from section "
                                + level.music().startSection() + "), was t=" + motif.from());
            }
            motif.sections().keySet().forEach(n -> {
                if (n > sections) {
                    problem.accept("music.motif.sections", "the level has " + sections + " sections, not " + n);
                }
            });
        });
    }

    private static void visibility(
            Content content, LevelData level, LevelData.Visibility visibility, BiConsumer<String, String> problem) {
        double end = level.sections().get(level.sections().size() - 1).end();
        for (int i = 0; i < visibility.ramps().size(); i++) {
            if (visibility.ramps().get(i).to() > end) {
                problem.accept("visibility.ramps[" + i + "]", "the ramp ends inside the level (by t=" + end + ")");
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            double pull = level.waves().stream()
                    .filter(wave -> wave.fliesOn(difficulty))
                    .flatMap(wave -> wave.groupList().stream())
                    .map(group -> content.enemies().get(group.enemy()))
                    .filter(enemy -> enemy != null)
                    .flatMap(enemy -> enemy.pull(difficulty).stream())
                    .mapToDouble(EnemyData.Pull::shipRadius)
                    .max()
                    .orElse(0);
            double smallest = visibility.smallest(difficulty);
            if (smallest < pull) {
                problem.accept(
                        "visibility.radii",
                        "the radius " + smallest + " px on " + difficulty.name().toLowerCase(java.util.Locale.ROOT)
                                + " lies inside a pull on the ship (" + pull + " px): what pulls the ship is seen");
            }
        }
    }
}
