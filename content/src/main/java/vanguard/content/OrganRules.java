package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemySpec;

/**
 * M11 part C, step C2 (design/enemies/ground/spawning-organ; design/campaign Level 45, The spawning organ): a periodic
 * spawn's Act 7 keys mapped onto the simulation's spawner ({@link EnemySpec.Spawner}, step CD1): its first release
 * {@code first} s after its centre crosses the top edge, a {@code formation: whirl cluster} release (the Whirl Seed's
 * spiral out of its centre), {@code hold} (a due release waits while the ship is within {@code shut_within}, never
 * skipped), {@code emp: false} (its clock runs on under the EMP Burst) and an {@code alternate} release every second
 * turn (its count the hook's {@code alternate_count} on a difficulty). A spawn without them maps as before.
 */
final class OrganRules {
    private OrganRules() {}

    static EnemySpec.Spawner spawner(
            Content content,
            EnemyData.Spawn spawn,
            Optional<EnemyData.Hook> hook,
            Difficulty difficulty,
            UnitRules.InLevel inLevel,
            EnemySpec.Spawner spawner) {
        EnemySpec.Spawner out = spawner;
        if (spawn.first().isPresent()) {
            out = out.withFirst(spawn.first().get());
        }
        if (spawn.cluster()) {
            out = out.withCluster(true);
        }
        if (spawn.hold().orElse(false)) {
            out = out.withHold(true);
        }
        if (!spawn.emp().orElse(true)) {
            out = out.withEmpImmune(true);
        }
        if (spawn.alternate().isPresent()) {
            EnemyData.SpawnAlternate alternate = spawn.alternate().get();
            EnemySpec.Spawner other = new EnemySpec.Spawner(
                    UnitRules.enemy(content, alternate.enemy(), difficulty, Optional.empty(), inLevel),
                    hook.flatMap(EnemyData.Hook::alternateCount).orElse(alternate.count()),
                    spawner.everySeconds(),
                    alternate.telegraph(),
                    Math.toRadians(alternate.arc().orElse(360.0)),
                    alternate.speed().orElse(spawn.speed()),
                    spawner.shutWithin());
            out = out.withAlternate(alternate.formation().isPresent() ? other.withCluster(true) : other);
        }
        return out;
    }
}
