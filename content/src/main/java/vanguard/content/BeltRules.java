package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.HaltCycle;
import vanguard.sim.HazardSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Range;
import vanguard.sim.WaveSpec;

/**
 * M9 part A, Act 5's unit rules for the simulation ({@link UnitRules}): the hex shield (design/enemies, Enemy shields)
 * and the {@code laser-line} (design/enemies/ground/rail-bunker).
 */
final class BeltRules {
    private BeltRules() {}

    /** {@code spec} with the unit's shield and its laser line at {@code difficulty}, as it is without them. */
    static EnemySpec unit(
            Content content, EnemyData enemy, Difficulty difficulty, UnitRules.InLevel inLevel, EnemySpec spec) {
        EnemySpec partB = partB(enemy, difficulty, spec);
        if (enemy.shield().isEmpty() && enemy.attacks().stream().noneMatch(BeltRules::laserLine)) {
            return partB;
        }
        return partB.withShield(shield(content, enemy, difficulty, inLevel))
                .withLaserLine(line(content, enemy, difficulty));
    }

    /**
     * M9 part B (design/campaign Level 29): the Talon's swoop path and its scatter (the hook {@code parting_pair}), the
     * Asteroid Mite's rock riding (the hook {@code ride}), a neutral craft and a hover's peel-off (the hook {@code
     * hover_y}); a unit without them as it is.
     */
    private static EnemySpec partB(EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        EnemyData.Movement movement = enemy.movement();
        boolean peels = movement.hover().flatMap(EnemyData.Hover::peel).isPresent();
        if (movement.swoopPath().isEmpty()
                && movement.ride().isEmpty()
                && movement.scatter().isEmpty()
                && !enemy.isNeutral()
                && !peels) {
            return spec;
        }
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        EnemySpec out = spec.withSwoopPath(movement.swoopPath()
                        .map(path -> new EnemySpec.SwoopPath(
                                UnitRules.range(path.lowest()), path.across(), path.fireBand())))
                .withScatter(movement.scatter()
                        .map(scatter -> new EnemySpec.SquadScatter(
                                scatter.speed(),
                                scatter.seconds(),
                                hook.flatMap(EnemyData.Hook::partingPair).orElse(false))))
                .withRide(movement.ride().map(ride -> {
                    Optional<EnemyData.RideChange> change = hook.flatMap(EnemyData.Hook::ride);
                    return new EnemySpec.Ride(
                            change.flatMap(EnemyData.RideChange::within).orElse(ride.within()),
                            change.flatMap(EnemyData.RideChange::tell).orElse(ride.tell()),
                            ride.gap(),
                            change.flatMap(EnemyData.RideChange::diveSpeed).orElse(ride.diveSpeed()),
                            ride.tumble(),
                            ride.crawl());
                }));
        if (peels) {
            EnemySpec.Hover hover = spec.hover().orElseThrow();
            Optional<Double> hoverY = hook.flatMap(EnemyData.Hook::hoverY);
            out = out.withHover(Optional.of(new EnemySpec.Hover(
                            hover.seconds(),
                            hoverY.map(y -> new Range(y, y)).orElse(hover.depth()),
                            hover.aboveShip(),
                            hover.drift(),
                            0)
                    .withPeel(movement.hover().flatMap(EnemyData.Hover::peel).orElseThrow())));
        }
        return enemy.isNeutral() ? out.withNeutral(true) : out;
    }

    /**
     * The unit's shield (design/enemies, Enemy shields): its {@code shield} HP with the difficulty's HP lever and, on a
     * returning {@code medium} or larger unit, the act HP factor (never the beam era's); the basis's shared delay,
     * refill and breaker hold.
     */
    static Optional<EnemySpec.Shield> shield(
            Content content, EnemyData enemy, Difficulty difficulty, UnitRules.InLevel inLevel) {
        EnemyBasisData.Shields shared = content.enemyBasis().shields();
        return enemy.shield()
                .map(hp -> new EnemySpec.Shield(
                        content.difficulty()
                                .enemyHp(hp * UnitRules.returningFactor(content, enemy, inLevel), difficulty),
                        shared.delay(),
                        shared.refill(),
                        shared.breakerHold()));
    }

    /**
     * The unit's {@code laser-line} at {@code difficulty}: the line's telegraph (the hook's {@code telegraph} instead),
     * its live time, band and hold, the attack's bullet class as its damage, its interval (the hook's authored one, or
     * the fire-rate lever's), its first shot's delay and (M9 part F) its gate of lines telegraphed at once.
     */
    static Optional<EnemySpec.LaserLine> line(Content content, EnemyData enemy, Difficulty difficulty) {
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        return enemy.attacks().stream().filter(BeltRules::laserLine).findFirst().map(attack -> {
            EnemyData.Line line = attack.line().orElseThrow();
            return new EnemySpec.LaserLine(
                            hook.flatMap(EnemyData.Hook::telegraph).orElse(line.telegraph()),
                            line.live(),
                            line.band(),
                            UnitRules.bulletDamage(content, attack.bullet().orElseThrow()),
                            UnitRules.interval(content, attack, hook, difficulty),
                            attack.firstShotDelay().orElse(0.0),
                            line.holdWithin())
                    // M9 part F (Level 33): the gate of at most that many lines telegraphed at once.
                    .withMostTelegraphing(line.mostTelegraphing().orElse(0));
        });
    }

    /**
     * M9 part B (design/campaign Level 29): {@code spec} with its V-wing's leader at the tip ({@code leader: tip}: the
     * unit's {@code leader} airframe, {@link EnemyData#asLeader}) and its rocks with their riders ({@code on_rocks}: the
     * level's debris chunk); a wave without them as it is.
     */
    static WaveSpec wave(
            Content content,
            LevelData level,
            LevelData.Wave wave,
            String slug,
            Difficulty difficulty,
            UnitRules.InLevel inLevel,
            WaveSpec spec) {
        WaveSpec out = spec;
        if (wave.leader().isPresent()) {
            // M9 part F: a prey wing's leader holds and is prey as its wing (Level 33).
            out = out.withLeader(PartFRules.preyRole(
                    wave,
                    UnitRules.enemy(
                            content,
                            content.enemy(slug).asLeader(),
                            slug,
                            difficulty,
                            Optional.empty(),
                            inLevel,
                            0,
                            Optional.empty())));
        }
        if (wave.onRocks().isPresent()) {
            LevelData.OnRocks rocks = wave.onRocks().get();
            LevelData.DebrisField field = level.debris()
                    .orElseThrow(() -> new IllegalArgumentException("on_rocks: the level has no debris chunks"));
            LevelData.Chunk chunk = field.chunks().get(rocks.chunk());
            if (chunk == null) {
                throw new IllegalArgumentException("on_rocks: no debris chunk '" + rocks.chunk() + "'");
            }
            out = out.withRocks(new WaveSpec.OnRocks(
                    rocks.rocks().stream()
                            .map(rock -> new WaveSpec.Rock(
                                    rock.at().x(),
                                    PlayField.HEIGHT - rock.at().y(),
                                    rock.drift().x(),
                                    rock.drift().y()))
                            .toList(),
                    rocks.riders(),
                    new HazardSpec.DebrisSpec(
                            0,
                            0,
                            rocks.chunk(),
                            SimSpecs.hitbox(chunk.size()),
                            0,
                            -1,
                            chunk.hp().orElse(Double.POSITIVE_INFINITY),
                            chunk.damage().orElse(0.0),
                            0)));
        }
        return out;
    }

    /**
     * M9 part B (design/campaign Level 29, the rescue halt): the level's {@code halt_waves} at {@code difficulty} as the
     * held arena's halt cycle (its waves' times from the halt's start); none without them.
     */
    static Optional<HaltCycle> haltCycle(
            Content content,
            LevelData level,
            Difficulty difficulty,
            java.util.Map<String, LevelData.EnemyChange> enemyChanges,
            UnitRules.InLevel inLevel) {
        return level.haltWaves().map(halt -> {
            List<WaveSpec> waves = new ArrayList<>();
            for (LevelData.Wave wave : halt.waves()) {
                if (wave.fliesOn(difficulty)) {
                    WaveRules.addWave(content, level, wave, difficulty, enemyChanges, List.of(), waves, inLevel);
                }
            }
            return new HaltCycle(halt.every(), waves);
        });
    }

    static boolean laserLine(EnemyData.Attack attack) {
        return attack.pattern().equals(EnemyData.Attack.LASER_LINE);
    }
}
