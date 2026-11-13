package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M11 part H, step H2 (design/enemies/bosses/choir-heart; m11h-gaps.md §1 #13): the Choir Heart's keys ({@link
 * HeartBlock}) name its phases, parts and attacks; the walls stand in a phase after the first and carry that phase's
 * parts as their nodes, at most one a half, and leave a corridor of 300–360 px on every difficulty (the spec's hard and
 * easy) with the ship's room; the drift's phase is the walls' and its profile stays within |θ| ≤ 15° (the gentler
 * profile); the window parts are the first phase's; the heartbeat's attacks are rings from a part every phase keeps;
 * the checkpoints at each phase's start belong to an act boss; the difficulties' {@code heart} changes only to a boss
 * with walls or a drift.
 */
final class HeartChecks {
    /** The corridor's narrowest and widest, px (the spec's hard 300, easy 360). */
    static final double[] CORRIDOR = {300, 360};

    /** The drift's steepest angle, degrees (the spec's gentler profile). */
    static final double MOST_ANGLE = 15;

    private HeartChecks() {}

    /** The Heart's keys in a stat block. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        Optional<EnemyData.BossData> script = enemy.boss();
        boolean changed = false;
        for (Difficulty difficulty : Difficulty.values()) {
            changed |= enemy.heartChange(difficulty).isPresent();
        }
        if (script.isEmpty()) {
            if (changed) {
                problem.accept("difficulty.heart", "only a boss has heart changes");
            }
            return;
        }
        EnemyData.BossData boss = script.get();
        if (changed && boss.walls().isEmpty() && boss.drift().isEmpty()) {
            problem.accept("difficulty.heart", "only a boss with walls or a drift has heart changes");
        }
        List<String> phases =
                boss.phases().stream().map(EnemyData.PhaseData::name).toList();
        boss.walls().ifPresent(walls -> walls(enemy, boss, phases, walls, problem));
        boss.drift().ifPresent(drift -> {
            if (!phases.contains(drift.phase())) {
                problem.accept("boss.drift.phase", "no boss phase '" + drift.phase() + "'");
            }
            if (boss.walls().isPresent() && !boss.walls().get().phase().equals(drift.phase())) {
                problem.accept("boss.drift.phase", "the drift runs in the walls' phase");
            }
            for (List<Double> point : drift.profile()) {
                if (Math.abs(point.get(1)) > MOST_ANGLE) {
                    problem.accept("boss.drift.profile", "|θ| at most " + MOST_ANGLE + "°, was " + point.get(1));
                }
            }
        });
        boss.partWindows().ifPresent(windows -> {
            List<String> first = boss.phases().getFirst().until().parts().orElse(List.of());
            for (String name : windows.parts()) {
                if (enemy.partIndex(name) < 0) {
                    problem.accept("boss.part_windows.parts", "no part '" + name + "'");
                } else if (!first.contains(name)) {
                    problem.accept("boss.part_windows.parts", "a window part is the first phase's: " + name);
                }
            }
        });
        boss.lulls().flatMap(HeartBlock.LullsData::keep).ifPresent(keep -> {
            for (String name : keep) {
                if (enemy.attack(name).isEmpty()) {
                    problem.accept("boss.lulls.keep", "no attack named '" + name + "'");
                }
            }
        });
        List<String> beats = boss.heartbeat().orElse(List.of());
        for (String name : beats) {
            Optional<EnemyData.Attack> attack = enemy.attack(name);
            if (attack.isEmpty()) {
                problem.accept("boss.heartbeat", "no attack named '" + name + "'");
            } else if (!attack.get().pattern().equals("ring")) {
                problem.accept(
                        "boss.heartbeat",
                        "a heartbeat is a ring, not " + attack.get().pattern());
            }
        }
        if (boss.checkpoints().isPresent() && boss.midBoss()) {
            problem.accept("boss.checkpoints", "a checkpoint at each phase's start is an act boss's");
        }
        if (boss.checkpoints().isPresent() && boss.phases().size() < 2) {
            problem.accept("boss.checkpoints", "a checkpoint at each phase's start needs phases after the first");
        }
        boss.dark().ifPresent(dark -> {
            if (!phases.contains(dark.phase())) {
                problem.accept("boss.dark.phase", "no boss phase '" + dark.phase() + "'");
            }
        });
    }

    private static void walls(
            EnemyData enemy,
            EnemyData.BossData boss,
            List<String> phases,
            HeartBlock.WallsData walls,
            BiConsumer<String, String> problem) {
        int phase = phases.indexOf(walls.phase());
        if (phase < 1) {
            problem.accept("boss.walls.phase", "the walls stand in a phase after the first: '" + walls.phase() + "'");
            return;
        }
        List<String> parts = boss.phases().get(phase).until().parts().orElse(List.of());
        int[] halves = new int[4];
        for (HeartBlock.WallNodeData node : walls.nodes()) {
            if (enemy.partIndex(node.part()) < 0) {
                problem.accept("boss.walls.nodes", "no part '" + node.part() + "'");
            } else if (!parts.contains(node.part())) {
                problem.accept("boss.walls.nodes", "a node is a part of the walls' phase: " + node.part());
            }
            halves[(node.left() ? 0 : 2) + (node.y() < 270 ? 0 : 1)]++;
        }
        for (int h = 0; h < halves.length; h++) {
            if (halves[h] > 1) {
                problem.accept("boss.walls.nodes", "at most one node a half-wall");
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            double corridor = enemy.heartChange(difficulty)
                    .flatMap(HeartBlock.HeartChange::corridor)
                    .orElse(walls.corridor());
            if (corridor < CORRIDOR[0] || corridor > CORRIDOR[1]) {
                problem.accept(
                        "boss.walls.corridor",
                        difficulty + ": a corridor of " + CORRIDOR[0] + "–" + CORRIDOR[1] + " px, was " + corridor);
            }
        }
        if (walls.breathe().amp() * 2 * Math.PI * walls.breathe().hz() > 2 * walls.mostSpeed()) {
            problem.accept("boss.walls.breathe", "the breath stays near the walls' top speed (most_speed)");
        }
    }
}
