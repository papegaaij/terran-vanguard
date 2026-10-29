package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vanguard.sim.GroundObjectSpec;

/**
 * M9 part G's static checks and lookups (design/campaign Level 34 Salvage Run; design/enemies/ground/warden-tank, Act 5:
 * the return in livery; m9g-gaps.md §1 #2–4).
 *
 * <p>A unit's: a {@code livery} starts in a later act than the unit's first level's.
 *
 * <p>A level's: an {@code escapes} secondary naming no enemy but a ground object's look is an <em>object escapes</em>
 * objective ({@link #objectEscapes}): its look (or a variant, {@code <look>-<variant>}) on destructibles only, never on
 * a fork branch's object, and no wave {@code tag} (the allowed count against them is {@code FoundryChecks}'). A {@code
 * pickup: story} (the record checks its name and its group carrier): at most one in a level and none in a difficulty's
 * extra pickups, its group's targets all destructibles, at least one; no {@code story} as a target's {@code drop} or
 * {@code bonus_drop} or a point drop. The radio events {@code story-collected} and {@code story-missed} only with a
 * story pickup.
 */
final class SalvageChecks {
    private static final Pattern ACT_KEY = Pattern.compile("act-(\\d+)-.*");

    private SalvageChecks() {}

    /**
     * Whether {@code look} is {@code name} or a variant of it ({@code <name>-<variant>}): the sim's {@link
     * vanguard.sim.Secondary#countsObject(String)}.
     */
    static boolean ofLook(String look, String name) {
        return look.equals(name) || look.startsWith(name + "-");
    }

    /** A destructible ground target's look: its {@code sprite}, Level 01's cargo container if left out. */
    static String look(LevelData.GroundTarget target) {
        return target.sprite().orElse(GroundObjectSpec.CARGO_CONTAINER);
    }

    /** Whether it is a destructible (not an enemy nest, not a secret's trigger). */
    static boolean destructible(LevelData.GroundTarget target) {
        return target.enemy().isEmpty() && target.hits().isEmpty();
    }

    /** The destructibles of look {@code name} (or a variant) the level places. */
    static int objectsOf(LevelData level, String name) {
        return level.groundTargets().stream()
                .filter(target -> destructible(target) && ofLook(look(target), name))
                .mapToInt(target -> target.at().size())
                .sum();
    }

    /**
     * Whether {@code secondary} is an object escapes objective: its {@code escapes} names no enemy but the look of
     * destructibles the level places (Level 34's {@code escapes: cargo-container}).
     */
    static boolean objectEscapes(Content content, LevelData level, LevelData.Secondary secondary) {
        return secondary
                .escapes()
                .filter(name -> !content.enemies().containsKey(name) && objectsOf(level, name) > 0)
                .isPresent();
    }

    /** A unit's part-G keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        enemy.livery().ifPresent(livery -> actOf(content, enemy.firstLevel()).ifPresent(act -> {
            if (livery.fromAct() <= act) {
                problem.accept(
                        "livery.from_act",
                        "a livery changes the look from a later act than its first level's (act " + act + ")");
            }
        }));
    }

    /** The act number of the act that holds level {@code number}, if its data exists. */
    private static Optional<Integer> actOf(Content content, int number) {
        return content.actOf(number).map(entry -> {
            Matcher matcher = ACT_KEY.matcher(entry.getKey());
            return matcher.matches() ? Integer.parseInt(matcher.group(1)) : null;
        });
    }

    /** A level's part-G keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        level.objectives()
                .secondary()
                .filter(secondary -> objectEscapes(content, level, secondary))
                .ifPresent(secondary -> {
                    String name = secondary.escapes().orElseThrow();
                    if (secondary.tag().isPresent()) {
                        problem.accept(
                                "objectives.secondary.tag", "an object escapes objective counts objects, no wave");
                    }
                    for (LevelData.GroundTarget target : level.groundTargets()) {
                        if (!ofLook(look(target), name)
                                || (!destructible(target) && target.sprite().isEmpty())) {
                            // A trigger or nest names its sprite to share the look (a beacon has none of its own).
                            continue;
                        }
                        if (!destructible(target)) {
                            problem.accept(
                                    "ground_targets." + target.target(),
                                    "the objective's look '" + name + "' is on a trigger or nest: destructibles only");
                        } else if (target.branch().isPresent()) {
                            problem.accept(
                                    "ground_targets." + target.target(),
                                    "an object the secondary counts lies on every route (no branch)");
                        }
                    }
                });
        List<LevelData.PlacedPickup> stories =
                level.pickups().stream().filter(p -> p.pickup() == Pickup.STORY).toList();
        if (stories.size() > 1) {
            problem.accept("pickups", "at most one story pickup in a level, were " + stories.size());
        }
        for (Optional<LevelData.Variant> variant :
                List.of(level.difficulty().easy(), level.difficulty().hard())) {
            boolean story = variant.flatMap(LevelData.Variant::extraPickups)
                    .map(extra -> extra.stream().anyMatch(p -> p.pickup() == Pickup.STORY))
                    .orElse(false);
            if (story) {
                problem.accept("difficulty", "a story pickup is the same on every difficulty: not an extra");
            }
        }
        for (LevelData.PlacedPickup story : stories) {
            String group = story.carrier().group().orElseThrow();
            List<LevelData.GroundTarget> members = level.groundTargets().stream()
                    .filter(target -> target.group().filter(group::equals).isPresent())
                    .toList();
            if (members.isEmpty() || !members.stream().allMatch(SalvageChecks::destructible)) {
                problem.accept(
                        "pickups",
                        "the story pickup's group '" + group
                                + "' is of destructible ground targets (the hold's clamps)");
            }
        }
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.drop().filter(Pickup.STORY::equals).isPresent()
                    || target.bonusDrop().filter(Pickup.STORY::equals).isPresent()) {
                problem.accept(
                        "ground_targets." + target.target(),
                        "a story pickup is only a group's drop (pickups: dropped_by: {group, unit: last})");
            }
        }
        if (level.released().stream().anyMatch(drop -> drop.pickup() == Pickup.STORY)) {
            problem.accept("drops", "a story pickup is only a group's drop, not a point drop");
        }
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.CueEvent event = level.radio().get(i).event().orElse(null);
            if ((event == LevelData.CueEvent.STORY_COLLECTED || event == LevelData.CueEvent.STORY_MISSED)
                    && stories.isEmpty()) {
                problem.accept("radio[" + i + "].event", "a story cue in a level without a story pickup");
            }
        }
    }
}
