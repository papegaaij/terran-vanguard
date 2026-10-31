package vanguard.content;

import java.util.Optional;
import vanguard.sim.CreditSource;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;

/**
 * M10 part B (design/campaign Level 36 "Cloud Divers"; gap check m10b-gaps.md §1 #2–#6): the level's unit and level keys
 * for the simulation (flown from step B1). A stat block's {@code counted: false} (a hostile craft not counted as an
 * enemy), its {@code haul} (the credit source its bounty is paid from) and its drift's sideways {@code vx} with {@code
 * mirrored} (a drifting carrier: the harvesters); a swarm wave's unit that also swoops flies its flock with the flock's
 * {@code fire_cap} and {@code stagger} in its swoop path's fire band (the Chimera); a small debris chunk that {@code
 * reveals} a secret frees its hidden crate when broken (the pathfinder pod).
 */
final class CloudDiverRules {
    private CloudDiverRules() {}

    /** {@code spec} with the stat block's Level 36 keys; a unit without them as it is. */
    static EnemySpec unit(EnemyData enemy, EnemySpec spec) {
        Optional<EnemyData.Drift> drift = enemy.movement().drift().filter(d -> d.vx().isPresent());
        if (enemy.isCounted() && enemy.haul().isEmpty() && drift.isEmpty()) {
            return spec;
        }
        return spec.withUncounted(!enemy.isCounted())
                .withHaul(enemy.haul().map(CloudDiverRules::source))
                .withCarrierDrift(drift.map(d -> new EnemySpec.CarrierDrift(
                        d.vx().orElseThrow(), d.mirrored().orElse(false))));
    }

    /** The credit source of a stat block's {@code haul}. */
    static CreditSource source(String haul) {
        return switch (haul) {
            case "ground-targets" -> CreditSource.GROUND_TARGETS;
            case "kills" -> CreditSource.KILLS;
            default -> throw new IllegalArgumentException("haul: ground-targets or kills, was '" + haul + "'");
        };
    }

    /**
     * A wave's unit as its formation casts it (design/enemies/air/chimera): in a {@code swarm} a unit whose stat block
     * both swoops and flocks leaves its swoop path and flies its flock, under the flock's fire cap in the swoop path's
     * fire band; any other unit or formation as it is.
     */
    static EnemySpec swarmRole(Content content, String formation, String slug, Difficulty difficulty, EnemySpec unit) {
        EnemyData enemy = content.enemy(slug);
        EnemyData.Movement movement = enemy.movement();
        if (!formation.equals(SWARM)
                || movement.flock().isEmpty()
                || movement.swoopPath().isEmpty()) {
            return unit;
        }
        EnemyData.Flock flock = movement.flock().get();
        Optional<EnemySpec.SwarmFire> fire = flock.fireCap()
                .map(cap -> new EnemySpec.SwarmFire(
                        cap,
                        flock.stagger().orElseThrow(),
                        movement.swoopPath().get().fireBand()));
        return unit.withSwoopPath(Optional.empty())
                .withFlock(Optional.of(UnitRules.flock(flock, UnitRules.hook(enemy, difficulty), Optional.empty())
                        .withFire(fire)));
    }

    /** The {@code swarm} formation's name in the data. */
    static final String SWARM = "swarm";

    /**
     * A placed debris chunk for the simulation: one that {@code reveals} a secret of the level holds it, its hidden
     * crate the secret's (Level 36's pathfinder pod); any other as it is.
     */
    static HazardSpec.DebrisSpec secret(LevelData level, LevelData.PlacedChunk placed, HazardSpec.DebrisSpec chunk) {
        if (placed.reveals().isEmpty()) {
            return chunk;
        }
        String name = placed.reveals().get();
        LevelData.Secret secret = level.secrets().stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("debris: no secret '" + name + "'"));
        return chunk.withSecret(secret.name(), secret.crate());
    }
}
