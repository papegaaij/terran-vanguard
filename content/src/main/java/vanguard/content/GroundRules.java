package vanguard.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.HazardSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelResult;
import vanguard.sim.PickupType;

/**
 * A level's ground for the simulation ({@link SimSpecs#level}): the ground units of its targets, its
 * destructibles and triggers with their groups and secrets, and its cranes.
 */
final class GroundRules {
    private GroundRules() {}

    /** The ground enemies of the level's ground targets at {@code difficulty}, each in its group of the secondary objective. */
    static List<GroundUnit> groundUnits(Content content, LevelData level, Difficulty difficulty, InLevel inLevel) {
        List<String> groups = level.objectives().groups();
        // M9 part B: the groups that hold a held arena (its section's `holds`).
        List<String> holders = level.sections().stream()
                .flatMap(section -> section.holds().stream())
                .toList();
        List<GroundUnit> units = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            // M7 part C: a linked nest's anchors are the gates' (TerrainRules.gates, LevelScript.withGates).
            if (target.enemy().isEmpty() || TerrainRules.linked(target)) {
                continue;
            }
            // M7 part F: a nest placed turned (arc: up, Level 19's gantry nests) has its guns' facing.
            // M9 part E, step E3b (Level 32, QE-3 a): a tower whose first sweep is a full turn.
            EnemySpec dry = DeepCoreRules.firstFullTurn(
                    target,
                    UnitRules.enemy(
                            content,
                            target.enemy().get(),
                            difficulty,
                            Optional.empty(),
                            inLevel,
                            FoundryRules.arcFacing(target)));
            int group = target.group().map(groups::indexOf).orElse(-1);
            List<LevelData.Placement> at =
                    switch (difficulty) {
                        case EASY ->
                            target.easy().flatMap(LevelData.Placements::at).orElse(target.at());
                        case MEDIUM -> target.at();
                        case HARD ->
                            target.hard().flatMap(LevelData.Placements::at).orElse(target.at());
                    };
            // M5 part E: a nest's current, along which its drifting units (rafts) drift.
            double current = Math.toRadians(target.current().orElse(0.0));
            for (int index = 0; index < at.size(); index++) {
                LevelData.Placement placement = at.get(index);
                // M8 part C (gap check D2): a nest entering under water has its unit's underwater changes.
                InLevel where = inLevel.at(level, placement.t());
                EnemySpec wet = where.water()
                        ? DeepCoreRules.firstFullTurn(
                                target,
                                UnitRules.enemy(
                                        content,
                                        target.enemy().get(),
                                        difficulty,
                                        Optional.empty(),
                                        where,
                                        FoundryRules.arcFacing(target)))
                        : dry;
                // M9 part F (Level 33): a bunker pair's or trio's first lines staggered over the interval.
                // M10 part A (design/enemies/ground/shield-pylon): a pylon's dome along its placement's crown.
                EnemySpec enemy = JovianRules.crowned(target, staggered(target, wet, index, at.size()));
                // M5 part G: a nest entering from a side edge, drifting across (Level 13's mortar floe).
                GroundUnit unit = target.side().isPresent()
                        ? PartGRules.fromSide(placement, target.side().get(), enemy, group)
                        : new GroundUnit(placement.t(), placement.x(), enemy, group, current);
                // M9 part B (design/campaign Level 29): Block C's nests sleep until the turn; the clamp guns hold the
                // rescue halt (the held arena's `holds` group).
                if (target.dormantUntil().isPresent()) {
                    unit = unit.dormantUntil(target.dormantUntil().get());
                }
                if (target.group().filter(holders::contains).isPresent()) {
                    unit = unit.asHolder();
                }
                // M9 part E (Level 32): a fork branch's unit, dormant unless chosen.
                units.add(DeepCoreRules.unit(target, unit));
            }
        }
        // M5 part H: the root bursts' growths, placed at their bursts' points (Level 14).
        units.addAll(PartHRules.rootBurstUnits(content, level, difficulty, inLevel.act(), inLevel.level()));
        units.sort(Comparator.comparingDouble(GroundUnit::t));
        return units;
    }

    /**
     * M9 part F (design/campaign Level 33): the {@code index}-th of {@code of} units of a {@code staggered} nest, its first
     * line {@code index} × its interval / {@code of} later; a unit of another nest, or without a line, as it is.
     */
    static EnemySpec staggered(LevelData.GroundTarget target, EnemySpec enemy, int index, int of) {
        if (!target.staggered().orElse(false) || enemy.laserLine().isEmpty() || index == 0) {
            return enemy;
        }
        EnemySpec.LaserLine line = enemy.laserLine().get();
        return enemy.withLaserLine(Optional.of(new EnemySpec.LaserLine(
                line.telegraphSeconds(),
                line.liveSeconds(),
                line.bandPx(),
                line.damage(),
                line.intervalSeconds(),
                line.firstDelaySeconds() + index * line.intervalSeconds() / of,
                line.holdWithinPx(),
                line.mostTelegraphing())));
    }

    /**
     * M5 part E: the groups of the level's destructibles (not the objectives' groups of enemies), in
     * the order they first appear among its ground targets.
     */
    static List<String> destructibleGroups(LevelData level) {
        List<String> groups = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isEmpty()
                    && target.group().isPresent()
                    && !groups.contains(target.group().get())) {
                groups.add(target.group().get());
            }
        }
        return groups;
    }

    /** The level's cranes at {@code difficulty}. */
    static List<HazardSpec.CraneSpec> cranes(LevelData level, Difficulty difficulty) {
        List<HazardSpec.CraneSpec> cranes = new ArrayList<>();
        for (LevelData.CraneData crane : level.cranes().orElse(List.of())) {
            List<Double> swings =
                    switch (difficulty) {
                        case EASY -> crane.easy().map(LevelData.Swings::swings).orElse(crane.swings());
                        case MEDIUM -> crane.swings();
                        case HARD -> crane.hard().map(LevelData.Swings::swings).orElse(crane.swings());
                    };
            Optional<LevelData.Secret> secret = crane.clamp().flatMap(clamp -> level.secrets().stream()
                    .filter(s -> s.name().equals(clamp.reveals()))
                    .findFirst());
            cranes.add(new HazardSpec.CraneSpec(
                    crane.x(),
                    crane.y(),
                    crane.length(),
                    crane.width(),
                    Math.toRadians(crane.from()),
                    Math.toRadians(crane.to()),
                    swings,
                    crane.swingSeconds(),
                    crane.telegraph(),
                    crane.damage(),
                    crane.clamp().map(LevelData.Clamp::hits).orElse(0),
                    secret.map(LevelData.Secret::crate).orElse(0),
                    secret.map(LevelData.Secret::name).orElse("")));
        }
        return cranes;
    }

    /**
     * The level's destructibles and triggers at {@code difficulty}, in time order; triggers that
     * reveal the same secret reveal it together (Level 03's lifeboat lights: the crate drops when the
     * last is shot). M5 part E: a trigger's hits on easy or hard (Level 11's sunken pod); a
     * destructible's group of its own, whose last one destroyed drops the pickup placed on the group
     * (Level 11's floating containers and their armour patch).
     */
    static List<GroundObjectSpec> groundObjects(
            LevelData level, Difficulty difficulty, List<LevelData.PlacedPickup> carried) {
        List<String> destructibleGroups = destructibleGroups(level);
        int[] groupSizes = new int[destructibleGroups.size()];
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isEmpty() && target.group().isPresent()) {
                groupSizes[destructibleGroups.indexOf(target.group().get())] +=
                        target.at().size();
            }
        }
        List<String> secretNames =
                level.secrets().stream().map(LevelData.Secret::name).toList();
        Map<String, Integer> triggersPerSecret = new TreeMap<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            target.reveals()
                    .ifPresent(name -> triggersPerSecret.merge(name, target.at().size(), Integer::sum));
        }
        List<GroundObjectSpec> objects = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isPresent()) {
                continue;
            }
            Optional<LevelData.Secret> secret = target.reveals().flatMap(name -> level.secrets().stream()
                    .filter(s -> s.name().equals(name))
                    .findFirst());
            Optional<LevelData.Placements> change =
                    switch (difficulty) {
                        case EASY -> target.easy();
                        case MEDIUM -> Optional.empty();
                        case HARD -> target.hard();
                    };
            int hits = change.flatMap(LevelData.Placements::hits)
                    .orElse(target.hits().orElse(0));
            int group = target.group().map(destructibleGroups::indexOf).orElse(-1);
            Optional<PickupType> groupDrop = target.group().flatMap(name -> carried.stream()
                    .filter(p -> p.carrier().group().filter(name::equals).isPresent())
                    .map(p -> SimSpecs.pickup(p.pickup()))
                    .findFirst());
            for (LevelData.Placement placement : target.at()) {
                GroundObjectSpec object = new GroundObjectSpec(
                        placement.t(),
                        placement.x(),
                        SimSpecs.hitbox(target.size().orElseThrow()),
                        change.flatMap(LevelData.Placements::hp)
                                .orElse(target.hp().orElse(0.0)),
                        target.bounty().orElse(0),
                        target.drop().map(SimSpecs::pickup),
                        hits,
                        // M9 part E (Level 32): a cache's lock frees its crate without a secret.
                        secret.map(LevelData.Secret::crate).orElse(DeepCoreRules.crate(target)),
                        secret.map(LevelData.Secret::name).orElse(""),
                        target.hardened().orElse(false),
                        secret.map(sec -> secretNames.indexOf(sec.name())).orElse(-1),
                        target.reveals().map(triggersPerSecret::get).orElse(1),
                        target.bonusDrop().map(SimSpecs::pickup),
                        target.sprite().orElse(GroundObjectSpec.CARGO_CONTAINER),
                        target.dark().orElse(false),
                        secret.flatMap(sec ->
                                sec.dataCore().map(core -> new LevelResult.DataCore(sec.name(), core.unlocks()))),
                        // M5 part E: a trigger on `sub` (Level 11's sunken pod).
                        target.layer().map(Layers::of).orElse(Layer.GROUND) == Layer.SUB,
                        group,
                        group < 0 ? 0 : groupSizes[group],
                        groupDrop);
                // M5 part F: a trigger on low-air drifting in screen space (Level 12's canister).
                // M9 part E (Level 32): a mining charge's burst, a fork branch's object.
                objects.add(DeepCoreRules.object(
                        target,
                        target.drift()
                                .map(drift -> object.drifting(drift.x(), drift.y()))
                                .orElse(object)));
            }
        }
        objects.sort(Comparator.comparingDouble(GroundObjectSpec::t));
        return objects;
    }
}
