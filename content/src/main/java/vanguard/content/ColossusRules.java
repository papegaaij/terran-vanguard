package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.BossSpec;
import vanguard.sim.BreachArena;
import vanguard.sim.PlayField;

/**
 * M7 part H (design/enemies/bosses/dust-colossus, design/campaign Level 21): a breaching boss's keys for the simulation
 * ({@link SimSpecs}, through {@link BossRules} and {@link MarsRules#sections}): its breach cycle ({@link
 * BossSpec.Breach}, the points turned once from the data's screen frame, y below the top edge, into the simulation's,
 * y up from the bottom edge), its suction ({@link BossSpec.Suction}) and the crawling arena of its level (a moving arena
 * of a level whose boss breaches crawls), its spine vents, its eruption under the ship and its debris rocks (sim steps
 * PH1 and PH3, mapped in step PH2).
 */
final class ColossusRules {
    private ColossusRules() {}

    /** Whether {@code level}'s boss breaches (its arena then crawls, if it moves). */
    static boolean breaching(Content content, LevelData level) {
        return level.boss()
                .map(LevelData.BossPlacement::enemy)
                .filter(content.enemies()::containsKey)
                .map(content::enemy)
                .map(EnemyData::breaches)
                .orElse(false);
    }

    /** {@code enemy}'s breach cycle on {@code difficulty} for the simulation; empty if it does not breach. */
    static Optional<BossSpec.Breach> breach(Content content, EnemyData enemy, Difficulty difficulty) {
        return enemy.breach(difficulty).map(cycle -> breach(content, enemy, cycle));
    }

    private static BossSpec.Breach breach(Content content, EnemyData enemy, EnemyData.Breach cycle) {
        List<BossSpec.Point> points = new ArrayList<>();
        List<BossSpec.Clamp> clamps = new ArrayList<>();
        for (String name : ColossusBlock.EDGE_POINTS) {
            Point at = cycle.points().get(name);
            points.add(new BossSpec.Point(at.x(), PlayField.HEIGHT - at.y()));
            Span range = cycle.follow().get(name);
            boolean side = name.equals("left") || name.equals("right");
            // The sides follow the ship's y: below the top edge in the data, up from the bottom edge in the simulation.
            clamps.add(
                    side
                            ? new BossSpec.Clamp(PlayField.HEIGHT - range.max(), PlayField.HEIGHT - range.min())
                            : new BossSpec.Clamp(range.min(), range.max()));
        }
        return new BossSpec.Breach(
                cycle.tell(),
                cycle.rise(),
                cycle.open(),
                cycle.biteTell(),
                cycle.dive(),
                cycle.buried(),
                cycle.warn(),
                points,
                clamps,
                orders(enemy, cycle),
                cycle.bite().radius(),
                UnitRules.bulletDamage(content, cycle.bite().bullet()),
                cycle.intro(),
                cycle.glowSeconds());
    }

    /** The orders in the boss's phase order (ColossusChecks: one per phase, by name, in that order). */
    private static List<BossSpec.Order> orders(EnemyData enemy, EnemyData.Breach cycle) {
        List<BossSpec.Order> orders = new ArrayList<>();
        for (EnemyData.PhaseData phase : enemy.boss().orElseThrow().phases()) {
            cycle.orders().stream()
                    .filter(order -> order.phase().equals(phase.name()))
                    .findFirst()
                    .ifPresent(order -> orders.add(new BossSpec.Order(
                            order.points().stream().map(ColossusRules::pose).toList(), order.follows())));
        }
        if (orders.isEmpty()) {
            throw new IllegalArgumentException(enemy.name() + ": a breach order names a boss phase");
        }
        return orders;
    }

    /** A breach point's pose by its data name. */
    static BreachArena.Pose pose(String point) {
        return switch (point) {
            case "front" -> BreachArena.Pose.FRONT;
            case "left" -> BreachArena.Pose.LEFT;
            case "right" -> BreachArena.Pose.RIGHT;
            case "rear" -> BreachArena.Pose.REAR;
            case ColossusBlock.UNDER -> BreachArena.Pose.UNDER;
            default -> throw new IllegalArgumentException("no breach point '" + point + "'");
        };
    }

    /** {@code enemy}'s suction on {@code difficulty} for the simulation; empty without one. */
    static Optional<BossSpec.Suction> suction(EnemyData enemy, Difficulty difficulty) {
        return enemy.suction(difficulty).map(s -> new BossSpec.Suction(s.speed(), s.inner(), s.outer()));
    }

    /** {@code enemy}'s spine vents for the simulation: their part indexes, distances and the burst's attack index. */
    static Optional<BossSpec.Vents> vents(EnemyData enemy) {
        return enemy.boss().flatMap(EnemyData.BossData::vents).map(vents -> {
            List<Integer> parts = vents.parts().stream().map(enemy::partIndex).toList();
            int burst = -1;
            for (int a = 0; a < enemy.attacks().size(); a++) {
                if (enemy.attacks().get(a).name().filter(vents.attack()::equals).isPresent()) {
                    burst = a;
                }
            }
            return new BossSpec.Vents(parts, vents.distances(), burst);
        });
    }

    /** {@code enemy}'s eruption under the ship on {@code difficulty} for the simulation; empty without one. */
    static Optional<BossSpec.Eruption> eruption(Content content, EnemyData enemy, Difficulty difficulty) {
        return enemy.eruption(difficulty)
                .map(e -> new BossSpec.Eruption(
                        e.marker(),
                        e.radius(),
                        UnitRules.bulletDamage(content, e.bullet()),
                        e.inset(),
                        e.open(),
                        e.buried()));
    }

    /** {@code enemy}'s debris rocks for the simulation: the time between two by boss phase (0: none). */
    static Optional<BossSpec.DebrisFlow> debris(Content content, EnemyData enemy) {
        return enemy.boss()
                .flatMap(EnemyData.BossData::debris)
                .map(debris -> new BossSpec.DebrisFlow(
                        enemy.boss().orElseThrow().phases().stream()
                                .map(phase -> debris.every().getOrDefault(phase.name(), 0.0))
                                .toList(),
                        debris.size().min(),
                        debris.size().max(),
                        debris.hp(),
                        debris.speed(),
                        debris.acceleration(),
                        content.enemyBasis().contactDamage().get(debris.contact())));
    }
}
