package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.WaveSpec;

/**
 * M9 part C, Level 30's unit rules for the simulation ({@link UnitRules}, design/campaign Level 30): the Minelayer's lay
 * path and line mines (design/enemies/space/minelayer), the Gilded Gunship's facing armour, ring lamps and Dart streams
 * (design/enemies/air/gilded-gunship), the Dart Drone's chase by difficulty (design/enemies/air/dart-drone) and the Rail
 * Serpent's turret cars and machine split (design/enemies/air/rail-serpent); a wave's mine lines.
 */
final class MinefieldRules {
    private MinefieldRules() {}

    /** {@code spec} with the unit's part C components at {@code difficulty}; a unit without them as it is. */
    static EnemySpec unit(
            Content content, EnemyData enemy, Difficulty difficulty, UnitRules.InLevel inLevel, EnemySpec spec) {
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        EnemySpec out = spec;
        if (enemy.movement().lay().isPresent()) {
            out = out.withMineLayer(Optional.of(mineLayer(enemy, hook)));
        }
        if (enemy.armour().facing()) {
            out = out.withFacingArmour(Optional.of(facingArmour(enemy)));
        }
        Optional<EnemyData.Spawn> stream = enemy.attacks().stream()
                .flatMap(attack -> attack.spawn().stream())
                .filter(spawn -> spawn.stream().isPresent())
                .findFirst();
        if (stream.isPresent()) {
            out = out.withLauncher(Optional.of(launcher(content, stream.get(), hook, difficulty, inLevel)))
                    // The ring's lamps (the Gunship's 0.4 s tell, read from the gun clock).
                    .withGun(out.gun().map(gun -> gun.withTell(ringTell(enemy))));
        }
        Optional<EnemyData.Chase> chase = enemy.movement().chase();
        Optional<Double> turnRate = hook.flatMap(EnemyData.Hook::chase).flatMap(EnemyData.ChaseChange::turnRate);
        if (chase.isPresent() && turnRate.isPresent()) {
            out = out.withChase(
                    new EnemySpec.Chase(
                            chase.get().speed(),
                            Math.toRadians(turnRate.get()),
                            chase.get().seconds()),
                    out.latch());
        }
        Optional<EnemyData.Cars> cars = enemy.segmentChain().flatMap(EnemyData.SegmentChain::cars);
        if (cars.isPresent() && out.chain().isPresent()) {
            out = out.withChain(Optional.of(train(content, enemy, difficulty, inLevel, cars.get(), out)));
        }
        return out;
    }

    /** Whether {@code attack} is the one a train's turret cars fire (not the engine's gun). */
    static boolean carAttack(EnemyData enemy, EnemyData.Attack attack) {
        return enemy.segmentChain()
                .flatMap(EnemyData.SegmentChain::cars)
                .map(cars -> attack.name().filter(cars.attack()::equals).isPresent())
                .orElse(false);
    }

    /** A line mine's rules from its stat block; empty for a spore mine. */
    static Optional<EnemyGun.LineMine> lineMine(Content content, EnemyData.Mine mine) {
        if (!mine.lineMine()) {
            return Optional.empty();
        }
        return Optional.of(new EnemyGun.LineMine(
                mine.proximity().orElseThrow(),
                mine.blink().orElseThrow(),
                mine.blast().map(EnemyData.Blast::radius).orElse(0.0),
                mine.blast()
                        .map(blast -> UnitRules.bulletDamage(content, blast.bullet()))
                        .orElse(0.0),
                mine.chain().orElse(0.0),
                mine.hop().orElseThrow()));
    }

    /** The layer's lay path and its line at the difficulty (the hook {@code mine}: easy's gap and short middle). */
    private static EnemySpec.MineLayer mineLayer(EnemyData enemy, Optional<EnemyData.Hook> hook) {
        EnemyData.Lay lay = enemy.movement().lay().orElseThrow();
        EnemyData.MineLine line = enemy.attacks().stream()
                .flatMap(attack -> attack.mine().stream())
                .flatMap(mine -> mine.line().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a mine layer lays a line"));
        Optional<EnemyData.MineChange> change = hook.flatMap(EnemyData.Hook::mine);
        return new EnemySpec.MineLayer(
                lay.rise(),
                lay.strafe(),
                lay.lane(),
                line.clusters(),
                line.perCluster(),
                line.spacing(),
                line.clusterGap(),
                change.flatMap(EnemyData.MineChange::gap).orElse(line.gap()),
                change.flatMap(EnemyData.MineChange::middleShort)
                        .orElse(line.middleShort().orElse(0)),
                line.most());
    }

    /** Facing armour: the front arc and factor, the rear plate's arc and multiplier (none without one: × 1). */
    private static EnemySpec.FacingArmour facingArmour(EnemyData enemy) {
        EnemyData.Armour armour = enemy.armour();
        Optional<EnemyData.RearPlate> plate = enemy.rearPlate();
        return new EnemySpec.FacingArmour(
                Math.toRadians(armour.frontArc().orElseThrow()),
                armour.factor().orElseThrow(),
                plate.map(p -> Math.toRadians(p.arc())).orElse(0.0),
                plate.map(EnemyData.RearPlate::multiplier).orElse(1.0));
    }

    /** A stream launcher: its streams' size by the hook {@code spawn_count}, its launched unit's spec. */
    private static EnemySpec.Launcher launcher(
            Content content,
            EnemyData.Spawn spawn,
            Optional<EnemyData.Hook> hook,
            Difficulty difficulty,
            UnitRules.InLevel inLevel) {
        EnemyData.Stream stream = spawn.stream().orElseThrow();
        return new EnemySpec.Launcher(
                UnitRules.enemy(content, spawn.enemy(), difficulty, Optional.empty(), inLevel),
                hook.flatMap(EnemyData.Hook::spawnCount).orElse(spawn.count()),
                spawn.every().orElseThrow(),
                stream.gap(),
                stream.first(),
                spawn.speed(),
                Math.toRadians(spawn.arc()),
                stream.straight(),
                stream.most());
    }

    /** The tell of a launcher's ring (its lamps), 0 without one. */
    private static double ringTell(EnemyData enemy) {
        return enemy.attacks().stream()
                .filter(attack -> attack.pattern().equals(SiegeRules.RING))
                .findFirst()
                .flatMap(EnemyData.Attack::tell)
                .orElse(0.0);
    }

    /**
     * A train's chain with its turret cars and its machine split: the turret car a chain part of the cars' HP (by the
     * HP lever and the act's factor) and bounty firing the named attack with its tell; the cut's wreck.
     */
    private static EnemySpec.ChainSpec train(
            Content content,
            EnemyData enemy,
            Difficulty difficulty,
            UnitRules.InLevel inLevel,
            EnemyData.Cars cars,
            EnemySpec head) {
        EnemySpec.ChainSpec chain = head.chain().orElseThrow();
        EnemyData.Attack attack = enemy.attacks().stream()
                .filter(a -> a.name().filter(cars.attack()::equals).isPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": no attack '" + cars.attack() + "'"));
        EnemyGun gun = UnitRules.gun(content, enemy, attack, difficulty, Optional.empty())
                .withTell(attack.tell().orElse(0.0));
        EnemySpec segment = chain.segment();
        EnemySpec turret = EnemySpec.of(
                        head.slug() + "-turret",
                        content.difficulty()
                                .enemyHp(cars.hp() * UnitRules.actHpFactor(content, enemy, inLevel), difficulty),
                        segment.hitbox(),
                        segment.layer(),
                        segment.contactDamage(),
                        segment.destroyedByRamming(),
                        cars.bounty(),
                        segment.speed())
                .withGun(Optional.of(gun))
                .withMachine(enemy.isMachine());
        EnemySpec.ChainSpec out = chain;
        EnemyData.Cut cut = enemy.segmentChain().orElseThrow().cut().orElseThrow();
        if (cut.wrecks()) {
            // The machine split: the rear part loses power and slows to a stop, then blows paying its cars.
            out = out.withScatter(Optional.of(new EnemySpec.Scatter(
                    cut.scatter().orElseThrow(), cut.seconds().orElseThrow(), true)));
        }
        return out.withCars(Optional.of(new EnemySpec.Cars(turret, cars.every(), cars.stagger())));
    }

    /** M9 part C: {@code spec} with its wave's mine lines (call it last: older {@code withX} copies drop them). */
    static WaveSpec wave(LevelData.Wave wave, WaveSpec spec) {
        return wave.mineLines()
                .map(lines -> spec.withMineLines(new WaveSpec.MineLines(lines.rows(), lines.gaps())))
                .orElse(spec);
    }
}
