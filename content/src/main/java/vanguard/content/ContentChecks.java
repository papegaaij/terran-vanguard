package vanguard.content;

import java.util.List;
import java.util.TreeSet;

/** What each concern's checks share: the content, the problem sink and the checks of a name, a time and a group. */
abstract class ContentChecks {
    /** M5 part E: the layer under the water's surface. */
    static final String SUB = "sub";

    final Content content;
    private final Problems problems;

    ContentChecks(Content content, Problems problems) {
        this.content = content;
        this.problems = problems;
    }

    final void checkPartName(EnemyData enemy, String field, String part) {
        if (enemy.partIndex(part) < 0) {
            problem(enemy, field, "no part named '" + part + "'");
        }
    }

    final void checkFormation(Object file, String field, String name) {
        if (!content.enemyBasis().formations().containsKey(name)) {
            problem(file, field, "unknown formation '" + name + "' (see design/enemies/data.yaml, formations)");
        }
    }

    /** M5 part E: the units of a naval convoy that can be damaged (slams hurt them); unknown allies count none. */
    final int damageable(LevelData.Convoy convoy) {
        int count = 0;
        for (LevelData.ConvoyUnit unit : convoy.units()) {
            AlliesData.Ally ally = content.allies().allies().get(unit.ally());
            count += ally != null && ally.damagedBy().hitsPerSlam() > 0 ? 1 : 0;
        }
        return count;
    }

    /** A group name must be one of the objectives' groups (the primary's targets or the secondary's groups). */
    final void checkGroup(LevelData level, String field, String group) {
        List<String> groups = level.objectives().groups();
        if (!groups.contains(group)) {
            problem(level, field, "no group '" + group + "' in the objectives' groups " + groups);
        }
    }

    final void checkTime(LevelData level, String field, double t) {
        if (t >= level.seconds()) {
            problem(level, field, "t=" + t + " is after the level end at " + level.seconds() + " s");
        }
    }

    final void checkEnemyName(Object file, String field, String slug) {
        if (!content.enemies().containsKey(slug)) {
            problem(
                    file,
                    field,
                    "unknown enemy '" + slug + "' (known: "
                            + String.join(", ", new TreeSet<>(content.enemies().keySet())) + ")");
        }
    }

    final void problem(Object file, String field, String message) {
        problems.add(file, field, message);
    }
}
