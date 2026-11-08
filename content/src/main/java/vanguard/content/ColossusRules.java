package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.BossSpec;
import vanguard.sim.BreachArena;
import vanguard.sim.BreachDraw;
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
        return enemy.breach(difficulty)
                .map(cycle -> withBurrow(withDraw(breach(content, enemy, cycle), enemy, cycle), enemy, cycle)
                        // M11 part G, step G5b: the echo's mouth dives once hurt enough in one breach.
                        // (scaled as its HP by the difficulty: the same number of wounds on every difficulty)
                        .withFlinch(cycle.flinch()
                                .map(hp -> UnitRules.enemyHp(content, enemy, hp, difficulty))
                                .orElse(0.0)));
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

    /** M11 part G, step G2 (the Dust Colossus echo): {@code built} with its breaches by the seeded draw, if it has one. */
    private static BossSpec.Breach withDraw(BossSpec.Breach built, EnemyData enemy, EnemyData.Breach cycle) {
        return built.withDraw(cycle.draw()
                .map(draw -> new BreachDraw.Spec(
                        draw.points().stream().map(ColossusRules::pose).toList(),
                        true,
                        draw.rearEvery().orElse(0),
                        draw.eruption().map(ColossusBlock.EruptionDraw::every).orElse(0),
                        draw.eruption()
                                .flatMap(ColossusBlock.EruptionDraw::notFirst)
                                .orElse(false),
                        // Step G5: the eruption only from its phase on (a later stage's new trick).
                        draw.eruption()
                                .flatMap(ColossusBlock.EruptionDraw::from)
                                .map(name -> phaseIndex(enemy, name))
                                .orElse(0),
                        draw.frontFirst().orElse(false),
                        // Step G5b: the front at least every n-th draw (a steadier fight).
                        draw.frontEvery().orElse(0))));
    }

    /** M11 part G, step G5 (the Dust Colossus echo): {@code built} firing its burrow volleys, if it has them. */
    private static BossSpec.Breach withBurrow(BossSpec.Breach built, EnemyData enemy, EnemyData.Breach cycle) {
        return built.withBurrow(cycle.burrow()
                .map(burrow -> new BossSpec.Burrow(
                        attackIndex(enemy, burrow.attack()),
                        burrow.every(),
                        burrow.clear().orElse(0.0))));
    }

    /** The index of {@code enemy}'s boss phase named {@code name}. */
    private static int phaseIndex(EnemyData enemy, String name) {
        List<EnemyData.PhaseData> phases = enemy.boss().orElseThrow().phases();
        for (int f = 0; f < phases.size(); f++) {
            if (phases.get(f).name().equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException(enemy.name() + ": no boss phase '" + name + "'");
    }

    /** The index of {@code enemy}'s attack named {@code name}. */
    private static int attackIndex(EnemyData enemy, String name) {
        for (int a = 0; a < enemy.attacks().size(); a++) {
            if (enemy.attacks().get(a).name().filter(name::equals).isPresent()) {
                return a;
            }
        }
        throw new IllegalArgumentException(enemy.name() + ": no attack '" + name + "'");
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
            BossSpec.Vents built = new BossSpec.Vents(parts, vents.distances(), burst);
            // M11 part G, step G2 (the Dust Colossus echo): the vents stay up through the dive and the buried stretch.
            if (vents.firstBreach().isPresent()) {
                // Step G5b: the echo's vents in turn, each from its breach on.
                return built.inTurn(vents.firstBreach().get());
            }
            return vents.stayUp().orElse(false) ? built.stayingUp() : built;
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
