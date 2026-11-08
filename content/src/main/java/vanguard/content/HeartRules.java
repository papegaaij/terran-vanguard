package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.ArenaWallsSpec;
import vanguard.sim.BossSpecBuilder;
import vanguard.sim.EnemyGun;
import vanguard.sim.HazardSpec;
import vanguard.sim.PartWindowsSpec;
import vanguard.sim.PhaseDriftSpec;
import vanguard.sim.PlayField;

/**
 * M11 part H, step H2 (design/enemies/bosses/choir-heart; m11h-gaps.md §1 #1, #3–#5, #7): the Choir Heart's keys ({@link
 * HeartBlock}) for the simulation at a difficulty, on the boss's builder: the arena walls (the difficulty's {@code
 * heart.corridor} and {@code contact}), the phase's drift (no reversal where {@code heart.reversal} is false), the
 * membranes' windows, the heartbeat's attack and the checkpoint at each phase's start. A boss without them is left as
 * it is.
 */
final class HeartRules {
    private HeartRules() {}

    /** Sets {@code enemy}'s Heart keys on {@code builder} at {@code difficulty}; its phases' and attacks' names in order. */
    static void apply(
            BossSpecBuilder builder,
            EnemyData enemy,
            Difficulty difficulty,
            List<String> phases,
            List<String> attackNames) {
        EnemyData.BossData script = enemy.boss().orElseThrow();
        Optional<HeartBlock.HeartChange> change = enemy.heartChange(difficulty);
        builder.walls(script.walls().map(walls -> walls(enemy, walls, phases, change)));
        builder.drift(script.drift().map(drift -> drift(drift, phases, change)));
        builder.partWindows(script.partWindows()
                .map(windows -> new PartWindowsSpec(
                        windows.parts().stream().map(name -> part(enemy, name)).toList(),
                        windows.breaker(),
                        windows.emp())));
        List<String> beats = script.heartbeat().orElse(List.of());
        builder.heartbeat(beats.isEmpty() ? -1 : index(attackNames, beats.get(0), "attack"));
        builder.heartbeatFast(beats.size() < 2 ? -1 : index(attackNames, beats.get(1), "attack"));
        builder.phaseCheckpoints(script.checkpoints().isPresent());
    }

    /** The walls at the difficulty: its corridor and contact, the nodes' heights turned y up. */
    static ArenaWallsSpec walls(
            EnemyData enemy, HeartBlock.WallsData walls, List<String> phases, Optional<HeartBlock.HeartChange> change) {
        return new ArenaWallsSpec(
                index(phases, walls.phase(), "boss phase"),
                change.flatMap(HeartBlock.HeartChange::corridor).orElse(walls.corridor()),
                walls.tell(),
                walls.grow(),
                walls.breathe().amp(),
                walls.breathe().hz(),
                walls.mostSpeed(),
                walls.clear(),
                walls.nodes().stream()
                        .map(node -> new ArenaWallsSpec.Node(
                                part(enemy, node.part()), node.left(), PlayField.HEIGHT - node.y()))
                        .toList(),
                change.flatMap(HeartBlock.HeartChange::contact).orElse(walls.contact()),
                walls.push().orElse(HeartBlock.WallsData.PUSH),
                walls.pushSeconds().orElse(HeartBlock.WallsData.PUSH_SECONDS),
                walls.cooldown().orElse(HeartBlock.WallsData.COOLDOWN));
    }

    /** The drift at the difficulty: without its reversal where the difficulty says so. */
    static PhaseDriftSpec drift(
            HeartBlock.DriftData drift, List<String> phases, Optional<HeartBlock.HeartChange> change) {
        boolean reverses = change.flatMap(HeartBlock.HeartChange::reversal).orElse(true);
        return new PhaseDriftSpec(
                index(phases, drift.phase(), "boss phase"),
                drift.speed(),
                drift.profile().stream()
                        .map(point -> new HazardSpec.Bend(point.get(0), point.get(1)))
                        .toList(),
                reverses
                        ? drift.reversal()
                                .map(turn -> new PhaseDriftSpec.Reversal(
                                        turn.atKills(), turn.atSeconds(), turn.seconds(), turn.tell()))
                        : Optional.empty(),
                drift.easeOut());
    }

    /**
     * A boss's spore ({@code pattern: mine}, the Choir Heart's spore drop): one spore of {@code attack}'s mine a shot,
     * bursting on contact ({@code damage}) or at its life's end into its ring ({@code speed} px/s).
     */
    static EnemyGun spore(Content content, EnemyData.Attack attack, double interval, double speed, double damage) {
        EnemyData.Mine mine = attack.mine().orElseThrow();
        return new EnemyGun(
                interval,
                0,
                1,
                speed,
                damage,
                false,
                1,
                0,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Optional.of(new EnemyGun.MineSpec(
                        mine.arm(),
                        mine.life(),
                        mine.drift(),
                        mine.hp(),
                        mine.ring(),
                        UnitRules.bulletDamage(content, mine.ringBullet()),
                        true,
                        mine.credits())));
    }

    private static int part(EnemyData enemy, String name) {
        int index = enemy.partIndex(name);
        if (index < 0) {
            throw new IllegalArgumentException(enemy.name() + ": no part '" + name + "'");
        }
        return index;
    }

    private static int index(List<String> names, String name, String what) {
        int index = names.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("no " + what + " '" + name + "'");
        }
        return index;
    }
}
