package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.WaveSpec;

/**
 * M9 part E, Level 32's rules for the simulation ({@link UnitRules}, {@link WaveRules}, {@link GroundRules}, {@link
 * TerrainRules}; design/campaign Level 32 Deep Core): the Sentinel Tower's hardened collar and its ground sweep with the
 * full turns (design/enemies/ground/sentinel-tower), the Strider's legs that topple it and its pod's homing salvo
 * (design/enemies/ground/strider), a terrain's forks, a wave's, a ground unit's and a ground object's branch, a
 * cache's crate and a mining charge's blast.
 */
final class DeepCoreRules {
    private DeepCoreRules() {}

    /** {@code spec} with the unit's part E components at {@code difficulty}; a unit without them as it is. */
    static EnemySpec unit(Content content, EnemyData enemy, Difficulty difficulty, double actHp, EnemySpec spec) {
        EnemySpec out = spec;
        Optional<Size> collar = enemy.armour().collar();
        if (collar.isPresent()) {
            out = out.withCore(Optional.of(SimSpecs.hitbox(collar.get())));
        }
        Optional<EnemyData.FullTurn> fullTurn = enemy.attacks().stream()
                .flatMap(attack -> attack.sweep().stream())
                .flatMap(sweep -> sweep.fullTurn().stream())
                .findFirst();
        if (fullTurn.isPresent() && enemy.movement().terrain().isPresent()) {
            EnemyData.FullTurn turn = fullTurn.get();
            int every = UnitRules.hook(enemy, difficulty)
                    .flatMap(EnemyData.Hook::fullTurnEvery)
                    .orElse(turn.every());
            out = out.withGroundSweep(Optional.of(
                    every == 0
                            ? EnemySpec.GroundSweep.ARCS_ONLY
                            : new EnemySpec.GroundSweep(every, turn.duration(), turn.telegraph(), turn.slot())));
        }
        Optional<EnemyData.Topple> topple = enemy.movement().walk().flatMap(EnemyData.Walk::topple);
        if (topple.isPresent()) {
            out = toppler(content, enemy, difficulty, actHp, topple.get(), out);
        }
        return out;
    }

    /**
     * A walker that topples (the Strider): its destroyable parts (legs that topple it, the pod carrying its second
     * attack), each with its HP by the HP lever and the act factor, its bounty and its own {@code first_bonus} (paid
     * with a body kill or a topple while it stands: the whole); the pod's attack as the walker's spit (a homing salvo
     * on its own clock, always: no facing rule; its hatch as the tell).
     */
    private static EnemySpec toppler(
            Content content,
            EnemyData enemy,
            Difficulty difficulty,
            double actHp,
            EnemyData.Topple topple,
            EnemySpec spec) {
        List<EnemyData.PartData> parts = enemy.partList().orElseThrow();
        List<EnemySpec.UnitPart> units = parts.stream()
                .filter(part -> part.kind().equals("destroyable"))
                .map(part -> {
                    EnemySpec.UnitPart unit = new EnemySpec.UnitPart(
                            part.name(),
                            content.difficulty().enemyHp(part.hp() * actHp, difficulty),
                            SimSpecs.hitbox(part.hitbox()),
                            part.offset().x(),
                            part.offset().y(),
                            part.bounty(),
                            false,
                            part.firstBonus().orElse(0),
                            0);
                    if (part.toppling()) {
                        unit = unit.withTopples();
                    }
                    if (part.attack().isPresent()) {
                        unit = unit.withSilencesSpit();
                    }
                    return unit;
                })
                .toList();
        // The spit first: a part silencing it needs the walker to have one.
        EnemySpec withSpit = spec;
        Optional<EnemyData.Attack> pod = enemy.attacks().stream()
                .filter(attack -> podAttack(enemy, attack))
                .findFirst();
        if (pod.isPresent() && spec.walker().isPresent()) {
            EnemyGun gun = UnitRules.gun(content, enemy, pod.get(), difficulty, Optional.empty());
            EnemyGun told = gun.homing()
                    .map(homing -> gun.withTell(homing.hatchSeconds()))
                    .orElse(gun);
            EnemySpec.Walker walker = spec.walker().get();
            withSpit = spec.withWalker(Optional.of(new EnemySpec.Walker(
                    walker.speed(),
                    walker.turnRate(),
                    walker.stride(),
                    walker.frontArc(),
                    Optional.of(told),
                    0,
                    walker.fanAtShip(),
                    walker.staggerSeconds())));
        }
        EnemySpec out = withSpit.withToppleParts(
                units,
                Optional.of(new EnemySpec.Topple(
                        topple.stagger(),
                        topple.fall(),
                        topple.line().width(),
                        topple.line().height())));
        return out;
    }

    /**
     * Whether {@code attack} is a walker's pod attack (a destroyable part names it with {@code attack}: the Strider's
     * homing salvo), fired as the walker's spit, not its gun.
     */
    static boolean podAttack(EnemyData enemy, EnemyData.Attack attack) {
        if (enemy.movement().walk().flatMap(EnemyData.Walk::topple).isEmpty()
                || attack.name().isEmpty()) {
            return false;
        }
        return enemy.partList().orElse(List.of()).stream()
                .anyMatch(part -> part.kind().equals("destroyable")
                        && part.attack().filter(attack.name().get()::equals).isPresent());
    }

    /** The terrain {@code spec} with the block's forks (their islands, fork 1 first); as it is without. */
    static GroundSpec.TerrainSpec forks(TerrainBlock block, GroundSpec.TerrainSpec spec) {
        List<TerrainBlock.Fork> forks = block.forkList();
        return forks.isEmpty()
                ? spec
                : spec.withForks(forks.stream().map(TerrainBlock.Fork::island).toList());
    }

    /**
     * {@code ground} with the level's terrain when it has forks (set again by {@link TerrainRules#script}): the script's
     * check of its branches needs the forks when it is first built; another level's ground as it is.
     */
    static GroundSpec forkedTerrain(LevelData level, Difficulty difficulty, GroundSpec ground) {
        boolean forked = level.terrain().map(t -> !t.forkList().isEmpty()).orElse(false);
        return forked ? ground.withTerrain(TerrainRules.terrain(level, difficulty)) : ground;
    }

    /** {@code spec} as one of its wave's fork branch's (call it last: older {@code withX} copies drop it). */
    static WaveSpec wave(LevelData.Wave wave, WaveSpec spec) {
        return wave.branch().map(branch -> spec.withBranch(branch.sim())).orElse(spec);
    }

    /** {@code unit} as one of its ground target's fork branch's; as it is without. */
    static GroundUnit unit(LevelData.GroundTarget target, GroundUnit unit) {
        return target.branch().map(branch -> unit.withBranch(branch.sim())).orElse(unit);
    }

    /**
     * {@code enemy}, a ground target's unit, with its first sweep a full turn when the target says so ({@code
     * full_turn_first}, Level 32's towers: QE-3 a); as it is without (or without a ground sweep).
     */
    static EnemySpec firstFullTurn(LevelData.GroundTarget target, EnemySpec enemy) {
        if (!target.fullTurnFirst().orElse(false) || enemy.groundSweep().isEmpty()) {
            return enemy;
        }
        return enemy.withGroundSweep(enemy.groundSweep().map(EnemySpec.GroundSweep::withFirstFullTurn));
    }

    /** The destructible {@code object} with its target's charge burst and fork branch; as it is without. */
    static GroundObjectSpec object(LevelData.GroundTarget target, GroundObjectSpec object) {
        GroundObjectSpec out = target.blast()
                .map(blast -> object.withBlast(new GroundObjectSpec.Blast(blast.radius(), blast.damage())))
                .orElse(object);
        return target.branch().map(branch -> out.withBranch(branch.sim())).orElse(out);
    }

    /** A cache's crate credits (Act 1 terms) for a destructible that is no secret's trigger; 0 without. */
    static int crate(LevelData.GroundTarget target) {
        return target.reveals().isPresent() ? 0 : target.crate().orElse(0);
    }
}
