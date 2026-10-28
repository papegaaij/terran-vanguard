package vanguard.sim;

import java.util.List;

/**
 * M9 part B (design/campaign Level 29, the rescue halt's waves; m9b-gaps.md §1.2): the waves of a held arena's halt on
 * the halt's own clock. Each wave's {@link WaveSpec#t()} is its time from the halt's start H (the step the level clock
 * first stands still at the arena's end because a {@link GroundUnit#holder()} stands), between 0 and {@code
 * everySeconds}; the cycle repeats every {@code everySeconds} while the halt lasts and is cut the moment the last
 * holder falls: no wave starts after it (a wave already in flies on). Its units count among the level's enemies as
 * they come, as a boss stream's do. Plain waves from the front only: no chains, swarms' pursuits, carriers, mounts,
 * rocks or edge-warned entries.
 */
public record HaltCycle(double everySeconds, List<WaveSpec> waves) {
    public HaltCycle {
        waves = List.copyOf(waves);
        if (!(everySeconds > 0) || waves.isEmpty()) {
            throw new IllegalArgumentException("a halt cycle repeats its waves: every " + everySeconds + " s");
        }
        for (WaveSpec wave : waves) {
            EnemySpec enemy = wave.enemy();
            if (!(wave.t() >= 0 && wave.t() < everySeconds)
                    || wave.entry() != WaveSpec.Entry.FRONT
                    || wave.formation() == WaveSpec.Formation.PURSUIT
                    || wave.mount().isPresent()
                    || wave.onRocks().isPresent()
                    || wave.warningSeconds().isPresent()
                    || enemy.chain().isPresent()
                    || enemy.brood().isPresent()
                    || enemy.aura().isPresent()
                    || enemy.rise().isPresent()
                    || enemy.burrow().isPresent()
                    || (enemy.walker().isPresent() && enemy.walker().get().staggered())) {
                throw new IllegalArgumentException("a halt cycle's wave is a plain front wave inside the cycle: "
                        + enemy.slug() + " at " + wave.t() + " s of " + everySeconds + " s");
            }
        }
    }
}
