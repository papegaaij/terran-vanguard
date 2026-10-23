package vanguard.content;

import java.util.Optional;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Range;

/**
 * M7 part E's rules for the simulation (step E4; design/campaign Level 18 Dust Storm, design/enemies/ground/dust-devil,
 * design/enemies/air/ghost-drone; round 36's M6 = a, Q.22e/h and the gap check's defaults pending the user's review in
 * round 41): the Dust Devil's spin cycle ({@code vortex}, its armoured {@code vanes} while closed are the simulation's
 * own rule for a vortex), its radial {@code pull} and its grit {@code spiral} fired {@code while: open}; the Ghost
 * Drone's {@code lane_strafe} with the hard {@code second_pass} and its burst fired by a pass {@code trigger} after its
 * {@code tell}, each shot re-aimed ({@code reaim}). The level's {@code visibility} and the music's {@code motif} are
 * presentation (the simulation never reads them), as is the intel's {@code unclassified_until_flown}.
 */
final class StormRules {
    /** The pattern of a non-boss spiral gun (the Dust Devil's grit). */
    static final String SPIRAL = "spiral";

    private StormRules() {}

    /**
     * {@code gun} as the stat block's M7 part E attack on {@code difficulty}: a {@code spiral} (the Dust Devil's grit)
     * as a spiral gun of the hook's {@code attacks.<name>.arms} (hard's 3) or the stat block's arms, turning clockwise
     * on the screen (with the vanes' spin; pending round 41) at its {@code turn_rate}, a bullet per arm every {@code
     * interval} s by the fire-rate lever; a {@code trigger}ed burst (the Ghost Drone's) of the hook's {@code
     * attacks.<name>.burst} (easy 2, hard 4) fired once per pass within {@code trigger.within_x} px of the ship's x,
     * {@code tell} s after its eye glint, re-aimed per shot with {@code reaim}. Another attack's gun as it is.
     */
    static EnemyGun gun(
            Content content, EnemyData enemy, EnemyData.Attack attack, Difficulty difficulty, EnemyGun gun) {
        Optional<EnemyData.AttackChange> change = attack.name().flatMap(name -> UnitRules.hook(enemy, difficulty)
                .flatMap(EnemyData.Hook::attacks)
                .map(changes -> changes.get(name)));
        if (attack.pattern().equals(SPIRAL)) {
            if (!attack.whileOpen()
                    || (enemy.vortex().isEmpty() && enemy.movement().coil().isEmpty())) {
                throw new IllegalArgumentException(
                        enemy.name() + ": a unit's spiral fires while its vortex (or coil) is open");
            }
            int arms = change.flatMap(EnemyData.AttackChange::arms)
                    .or(attack::arms)
                    .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a spiral has arms"));
            double turn = Math.toRadians(attack.turnRate()
                    .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a spiral turns")));
            double interval = attack.interval()
                            .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a spiral's interval"))
                    / content.difficulty().enemyFireRate().of(difficulty);
            // M8 part E (design/enemies/naval/spiral-nautilus): a coil's spiral turns the other way (counter-clockwise
            // on the screen, step E2's numbers); the Dust Devil's clockwise with its vanes.
            double sense = enemy.movement().coil().isPresent() ? 1 : -1;
            return EnemyGun.aimed(interval, 0, 1, gun.bulletSpeed(), gun.damage(), false)
                    .withSpiral(Optional.of(new EnemyGun.SpiralSpec(arms, sense * turn, interval)));
        }
        if (attack.trigger().isPresent()) {
            int burst = change.flatMap(EnemyData.AttackChange::burst)
                    .or(attack::burst)
                    .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a triggered attack is a burst"));
            return new EnemyGun(
                            Double.POSITIVE_INFINITY,
                            0,
                            burst,
                            gun.bulletSpeed(),
                            gun.damage(),
                            false,
                            1,
                            0,
                            Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY,
                            Optional.empty(),
                            Optional.empty(),
                            gun.burstGapSeconds(),
                            false)
                    .withPassTrigger(
                            attack.passWithinX(),
                            attack.tell().orElse(0.0),
                            attack.reaim().orElse(false));
        }
        return gun;
    }

    /**
     * {@code spec} with the stat block's spin cycle (the Dust Devil's, easy's 1.0 s window), its pull (easy's 25,
     * hard's 45 px/s on the ship) and its lane strafe (the Ghost Drone's, hard's second pass), plus its machine flag
     * kept. A unit without them is returned as it is.
     */
    static EnemySpec unit(EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        EnemySpec out = spec;
        Optional<EnemyData.Vortex> vortex = enemy.vortex(difficulty);
        if (vortex.isPresent()) {
            EnemyData.Vortex cycle = vortex.get();
            out = out.withVortex(Optional.of(new EnemySpec.Vortex(
                    cycle.closed(),
                    cycle.rise(),
                    cycle.open(),
                    cycle.offset(),
                    cycle.wanderSpeed(),
                    cycle.wanderAmplitude(),
                    cycle.keepAway())));
        }
        Optional<EnemyData.Pull> pull = enemy.pull(difficulty);
        if (pull.isPresent()) {
            EnemyData.Pull p = pull.get();
            out = out.withPull(
                    Optional.of(new EnemySpec.Pull(p.shipRadius(), p.shipSpeed(), p.pickupRadius(), p.pickupSpeed())));
        }
        Optional<EnemyData.LaneStrafe> strafe = enemy.movement().laneStrafe();
        if (strafe.isPresent()) {
            EnemyData.LaneStrafe lanes = strafe.get();
            boolean secondPass = UnitRules.hook(enemy, difficulty)
                    .flatMap(EnemyData.Hook::secondPass)
                    .orElse(false);
            out = out.withLaneStrafe(Optional.of(new EnemySpec.LaneStrafe(
                    lanes.descentSpeed(),
                    lanes.strafeSpeed(),
                    new Range(lanes.laneDepth().min(), lanes.laneDepth().max()),
                    lanes.laneSpacing(),
                    secondPass)));
        }
        return out;
    }
}
