package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.RookLossSpec;
import vanguard.sim.WaveSpec;

/**
 * M8 part F's rules for the simulation (design/campaign Level 26 Blackwater, design/enemies/naval/glow-angler;
 * m8f-gaps.md §1.1–1.3, §5 F2, F10, pending the user's review in round 55): the Glow Angler's {@code lunge} with its
 * ring (the {@code ring} attack of a lunging unit: its count by the difficulty's hook, its bullets by the bullet-speed
 * lever, its re-arm the attack's interval by the fire-rate lever; the water's × 0.7 is the simulation's), the level's
 * {@code rook_loss} and a field's {@code unit: centre} carrier. The dark itself is presentation ({@code
 * underwater_look}): the simulation never reads it.
 */
final class DarkRules {
    private DarkRules() {}

    /** {@code spec} with the stat block's lunge and its ring on {@code difficulty}; a unit without one as it is. */
    static EnemySpec unit(Content content, EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        Optional<EnemyData.Lunge> lunge = enemy.lunge(difficulty);
        if (lunge.isEmpty()) {
            return spec;
        }
        EnemyData.Lunge data = lunge.get();
        EnemyData.Attack ring = enemy.lungeRing()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a lunge fires its ring attack"));
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        Optional<EnemyData.AttackChange> change = ring.name()
                .flatMap(name -> hook.flatMap(EnemyData.Hook::attacks).map(changes -> changes.get(name)));
        return spec.withLunge(Optional.of(new EnemySpec.Lunge(
                data.trigger(),
                data.speed(),
                data.seconds(),
                data.stopShort(),
                change.flatMap(EnemyData.AttackChange::count)
                        .orElse(ring.count().orElseThrow()),
                ring.speed().orElseThrow()
                        * content.difficulty().enemyBulletSpeed().of(difficulty),
                UnitRules.bulletDamage(content, ring.bullet().orElseThrow()),
                data.retreatSpeed(),
                data.retreatSeconds(),
                UnitRules.interval(content, ring, hook, difficulty))));
    }

    /** The level's scripted loss of Rook for the simulation; none without a {@code rook_loss}. */
    static Optional<RookLossSpec> rookLoss(LevelData level) {
        return level.rookLoss().map(loss -> new RookLossSpec(loss.t(), loss.trails(), loss.spin()));
    }

    /** The simulation's index of a wave's carrier {@code unit} ({@code centre}: a field's unit nearest its centre). */
    static int carrier(LevelData.CarrierUnit unit) {
        return switch (unit) {
            case FIRST -> 0;
            case SECOND -> 1;
            case LAST -> WaveSpec.Carried.LAST;
            case CENTRE -> WaveSpec.Carried.CENTRE;
        };
    }
}
