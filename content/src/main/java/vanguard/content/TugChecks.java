package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M9 part D's static checks (design/campaign Level 31; design/allies/mining-tug; design/enemies/air/hornet,
 * space/shard-drone, air/buzzsaw-drone).
 *
 * <p>A unit's: a {@code dash} on a plain {@code air} unit without attacks (contact only) and no other movement that
 * steers it; an {@code air-link} attack on an {@code air} unit, its bullet class known, the unit's only link; the
 * difficulty hooks {@code dash} and {@code air_link} only with what they change.
 *
 * <p>A level's: a hull escort ({@code objectives.escort.hull}) only with an ally that {@code follows: hull}, its HP one
 * per section, its hull inside the play field across at its station with the sway and below the top edge, its climb-out
 * inside the level; its hooked kinds known, flying in the level (or launched by a unit that does) and hurting the hull
 * (an aimed homing launcher, a chaser or an air link); its unhooked waves the times of waves of a hooked kind; no ground
 * target passing under the hull's swayed footprint (hidden: m9d-gaps.md §6's SAM Nest rule); an air-linked wave a
 * {@code line abreast} or a {@code circle} of 2 to 4 (a circle's unit orbits).
 */
final class TugChecks {
    private TugChecks() {}

    /** A unit's part-D keys. */
    static void enemy(EnemyData enemy, Content content, BiConsumer<String, String> problem) {
        EnemyData.Movement movement = enemy.movement();
        if (movement.dash().isPresent()) {
            if (!enemy.layer().equals("air")) {
                problem.accept("movement.dash", "a dashing unit flies on the air layer");
            }
            if (!enemy.attacks().isEmpty()) {
                problem.accept("movement.dash", "a dashing unit has no attack: its contact is its threat");
            }
            if (movement.chase().isPresent()
                    || movement.hover().isPresent()
                    || movement.dive().isPresent()
                    || movement.swoop().isPresent()
                    || movement.swoopPath().isPresent()
                    || movement.lay().isPresent()
                    || movement.ride().isPresent()
                    || movement.walk().isPresent()
                    || movement.terrain().isPresent()
                    || movement.flock().isPresent()
                    || movement.laneStrafe().isPresent()) {
                problem.accept(
                        "movement.dash", "a dash is its only steering after its entry (no chase, hover, dive, …)");
            }
        }
        long links = enemy.attacks().stream()
                .filter(attack -> attack.pattern().equals(EnemyData.Attack.AIR_LINK))
                .count();
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            if (!attack.pattern().equals(EnemyData.Attack.AIR_LINK)) {
                continue;
            }
            String field = "attacks[" + i + "]";
            if (!enemy.layer().equals("air")) {
                problem.accept(field, "air links join units on the air layer");
            }
            attack.bullet()
                    .filter(bullet -> !content.enemyBasis().knowsBullet(bullet))
                    .ifPresent(bullet -> problem.accept(field + ".bullet", "unknown bullet class '" + bullet + "'"));
        }
        if (links > 1) {
            problem.accept("attacks", "a unit has at most one air-link attack");
        }
        boolean dashChange = false;
        boolean linkChange = false;
        Optional<EnemyData.Hooks> hooks = enemy.difficulty();
        for (Optional<EnemyData.Hook> hook :
                hooks.map(h -> List.of(h.easy(), h.hard())).orElse(List.of())) {
            dashChange |= hook.flatMap(EnemyData.Hook::dash).isPresent();
            linkChange |= hook.flatMap(EnemyData.Hook::airLink).isPresent();
        }
        if (dashChange && movement.dash().isEmpty()) {
            problem.accept("difficulty.dash", "a dash change without a dash");
        }
        if (linkChange && links == 0) {
            problem.accept("difficulty.air_link", "an air link change without an air-link attack");
        }
    }

    /** A level's part-D keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        airLinkedWaves(content, level, problem);
        Optional<LevelData.Escort> escort = level.objectives().escort();
        if (escort.isEmpty()) {
            return;
        }
        AlliesData.Ally ally = content.allies().allies().get(escort.get().ally());
        if (ally == null) {
            return;
        }
        String field = "objectives.escort";
        if (ally.hull() != escort.get().hull().isPresent()) {
            problem.accept(
                    field,
                    ally.hull()
                            ? "a " + escort.get().ally() + " follows hull: give its hull block"
                            : "a hull block needs an ally that follows hull");
            return;
        }
        if (escort.get().hull().isEmpty()) {
            return;
        }
        hull(content, level, ally, escort.get().hull().get(), problem);
    }

    private static void hull(
            Content content,
            LevelData level,
            AlliesData.Ally ally,
            LevelData.Hull hull,
            BiConsumer<String, String> problem) {
        String field = "objectives.escort.hull";
        int sections = ally.sections().orElseThrow().size();
        for (Difficulty difficulty : Difficulty.values()) {
            if (hull.hp(difficulty).size() != sections) {
                problem.accept(
                        field + ".hp",
                        "one per section of the " + ally.name() + ", " + sections + " (" + difficulty + ")");
            }
        }
        double halfWidth = ally.size().width() / 2;
        double halfHeight = ally.size().height() / 2;
        LevelData.Station station = hull.station();
        double left = station.at().x() - station.sway().x() - halfWidth;
        double right = station.at().x() + station.sway().x() + halfWidth;
        if (left < 0 || right > PlayField.WIDTH) {
            problem.accept(field + ".station", "the hull swings off the play field's sides: x " + left + "–" + right);
        }
        if (station.at().y() - station.sway().y() - halfHeight < 0
                || station.at().y() + station.sway().y() + halfHeight > PlayField.HEIGHT) {
            problem.accept(field + ".station", "the hull at its station leaves the play field's top or bottom edge");
        }
        if (hull.climb().t() + hull.climb().seconds() > level.seconds()) {
            problem.accept(field + ".climb", "the climb-out ends inside the level (" + level.seconds() + " s)");
        }
        Set<String> flying = flying(content, level);
        for (Difficulty difficulty : Difficulty.values()) {
            for (Map.Entry<String, AlliesBlock.HookMode> entry :
                    hull.modes(difficulty).entrySet()) {
                String slug = entry.getKey();
                EnemyData enemy = content.enemies().get(slug);
                if (enemy == null) {
                    problem.accept(field + ".targeted_by", "unknown enemy '" + slug + "'");
                    continue;
                }
                if (!flying.contains(slug)) {
                    problem.accept(field + ".targeted_by", "no '" + slug + "' in this level (nor launched in it)");
                }
                if (entry.getValue() == AlliesBlock.HookMode.ALWAYS && !hurtsHull(enemy)) {
                    problem.accept(
                            field + ".targeted_by",
                            "a hooked " + slug + " cannot hurt the hull: only an aimed homing launcher (homing.aimed),"
                                    + " a chaser or an air link does");
                }
            }
            for (double t : hull.unhooked(difficulty)) {
                boolean found = false;
                for (LevelData.Wave wave : level.waves()) {
                    found |= wave.t() == t
                            && wave.enemy()
                                    .filter(hull.modes(difficulty)::containsKey)
                                    .isPresent();
                }
                if (!found) {
                    problem.accept(
                            field + ".unhooked", "no wave of a targeted_by kind at t=" + t + " (" + difficulty + ")");
                }
            }
        }
        double near = station.at().x() - station.sway().x() - halfWidth;
        double far = station.at().x() + station.sway().x() + halfWidth;
        for (int i = 0; i < level.groundTargets().size(); i++) {
            GroundTargetsBlock.GroundTarget target = level.groundTargets().get(i);
            double half = target.size()
                            .or(() -> target.enemy().map(content.enemies()::get).map(EnemyData::size))
                            .map(Size::width)
                            .orElse(0.0)
                    / 2;
            for (GroundTargetsBlock.Placement at : target.at()) {
                if (at.x() + half > near && at.x() - half < far) {
                    problem.accept(
                            "ground_targets[" + i + "]",
                            "x=" + at.x() + " passes under the hull's swayed footprint (x " + near + "–" + far
                                    + "): hidden; place it beside the hull");
                }
            }
        }
    }

    /** Whether a hooked unit of {@code enemy} can hurt a hull: an aimed homing launcher, a chaser, an air link. */
    private static boolean hurtsHull(EnemyData enemy) {
        return enemy.movement().chase().isPresent()
                || enemy.attacks().stream()
                        .anyMatch(attack -> attack.pattern().equals(EnemyData.Attack.AIR_LINK)
                                || attack.homing()
                                        .flatMap(EnemyData.Homing::aimed)
                                        .orElse(false));
    }

    /** The slugs of the level's wave units, ground units and the units they launch or spawn. */
    private static Set<String> flying(Content content, LevelData level) {
        Set<String> slugs = new java.util.TreeSet<>();
        for (LevelData.Wave wave : level.waves()) {
            wave.enemy().ifPresent(slugs::add);
            wave.groups().ifPresent(groups -> groups.forEach(group -> slugs.add(group.enemy())));
        }
        level.groundTargets().forEach(target -> target.enemy().ifPresent(slugs::add));
        for (String slug : List.copyOf(slugs)) {
            EnemyData enemy = content.enemies().get(slug);
            if (enemy != null) {
                enemy.attacks().forEach(attack -> attack.spawn().ifPresent(spawn -> slugs.add(spawn.enemy())));
            }
        }
        return slugs;
    }

    /** A wave of an air-linked unit: a line abreast or a circle of 2 to 4 (a circle's unit orbits). */
    private static void airLinkedWaves(Content content, LevelData level, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            EnemyData enemy = wave.enemy().map(content.enemies()::get).orElse(null);
            if (enemy == null
                    || enemy.attacks().stream()
                            .noneMatch(attack -> attack.pattern().equals(EnemyData.Attack.AIR_LINK))) {
                continue;
            }
            String field = "waves[" + i + "]";
            String formation = wave.formation().orElse("");
            if (!formation.equals("line abreast") && !formation.equals("circle")) {
                problem.accept(
                        field + ".formation", "a linked group flies a line abreast or a circle, not " + formation);
            }
            int count = wave.count().orElse(1);
            if (count < 2 || count > vanguard.sim.AirLinks.MEMBERS) {
                problem.accept(field + ".count", "a linked group has 2 to " + vanguard.sim.AirLinks.MEMBERS + " units");
            }
            if (formation.equals("circle") && enemy.movement().orbit().isEmpty()) {
                problem.accept(field, "a linked circle's unit orbits (movement.orbit)");
            }
        }
    }
}
