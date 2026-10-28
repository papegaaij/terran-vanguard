package vanguard.sim;

import static vanguard.sim.TestSpecs.muzzle;

import java.util.List;
import java.util.Optional;

/**
 * M9 part A, step A1: the Act 5 fixtures the sim tests fly (the drafts' numbers, approved in round 60): a shielded
 * Talon, a Rail-Bunker-like {@code laser-line} turret, the Tesla Coil Pod and the Swarm Tail at their L1, the homing
 * diamond and the Act 5 hand plan's power budget. Content builds the same from the data (A2).
 */
final class Act5Specs {
    private Act5Specs() {}

    /** The global shield numbers (design/enemies, Enemy shields): 2 s delay, 50 %/s refill, a breaker's 3 s hold. */
    static EnemySpec.Shield shield(double hp) {
        return new EnemySpec.Shield(hp, 2.0, 0.5, 3.0);
    }

    /** A shielded flyer that scrolls down with the ground (for placing): the Talon's 2-point shield over {@code hp}. */
    static EnemySpec talon(double hp, double shieldHp) {
        return EnemySpec.of("talon", hp, new Hitbox(30, 30), Layer.AIR, 0, false, 10, 0)
                .withTerrain(true)
                .withMachine(true)
                .withShield(Optional.of(shield(shieldHp)));
    }

    /** A shielded Talon of 20 HP with its 2-point shield. */
    static final EnemySpec TALON = talon(20, 2);

    /** The Rail Bunker's rail (design/enemies/ground/rail-bunker): 1.0 s telegraph, 0.2 s live, 8 px, heavy 10. */
    static final EnemySpec.LaserLine RAIL = new EnemySpec.LaserLine(1.0, 0.2, 8, 10, 4.5, 0.5, 100);

    /** A hardened ground bunker carrying the rail, scrolling with the ground. */
    static final EnemySpec RAIL_BUNKER = EnemySpec.of(
                    "rail-bunker", 100, new Hitbox(56, 56), Layer.GROUND, 0, false, 45, 0)
            .withTerrain(true)
            .withHardened(true)
            .withMachine(true)
            .withLaserLine(Optional.of(RAIL));

    /** The Tesla Coil Pod's L1 on the left wing: 4 arcs/s of 3, reach 140 px, a 240° spread, sparks 24 px at 0.5. */
    static WeaponSpec tesla(double dx) {
        return WeaponSpec.arc(
                        "tesla-coil-pod", "arc", "tesla", 4, 3, 140, Math.toRadians(240), 24, 0.5, muzzle(dx, 4, 0))
                .withShieldBreaker(true);
    }

    static final WeaponSpec TESLA = tesla(-20);

    /**
     * The Swarm Tail's L1 (design/player/weapons/swarm-tail): 3/s of 5, launched backwards at 260 px/s, 480 px/s after
     * 0.3 s, turn 240°/s, 1.6 s, seeking within 320 px, rear first.
     */
    static final WeaponSpec SWARM_TAIL = new WeaponSpec(
                    "swarm-tail",
                    "micromissile",
                    "micromissile",
                    WeaponSpec.Delivery.HOMING,
                    false,
                    3,
                    5,
                    260,
                    new Hitbox(4, 10),
                    320,
                    1.6,
                    1,
                    0,
                    Math.toRadians(240),
                    Math.PI,
                    0,
                    0,
                    List.of(muzzle(0, -14, 180)),
                    480,
                    0.3,
                    WeaponSpec.Mines.NONE)
            .flanking(true)
            .withSeek(WeaponSpec.Seek.REAR_FIRST);

    /** The homing diamond's gun (the Hornet's salvo): 1 HP, seekable, at most 4 in flight. */
    static final EnemyGun DIAMOND = EnemyGun.aimed(2, 0, 1, 150, 6, false)
            .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.toRadians(90), 4, 1, 4, 0, true)));

    /** The drain's numbers (design/player/generator): 2 MW a drainer, at most 6, the rate never below 0.5. */
    static final Loadout.Drain DRAIN = new Loadout.Drain(2, 6, 0.5);

    /**
     * A power budget: {@code output} MW against {@code load} (the shield's {@code shieldDraw} in it), the shield's
     * regen {@code regen}/s before the bonus (+10 % a spare MW, at most +50 %).
     */
    static Loadout.Power power(double output, double load, double shieldDraw, double regen) {
        return new Loadout.Power(output, load, shieldDraw, regen, 0.1, 0.5, DRAIN);
    }

    /** {@code loadout} with {@code budget}, its shield's regen with the bonus of output − load baked in (as content's). */
    static Loadout powered(Loadout loadout, Loadout.Power budget) {
        ShieldModel model = loadout.shield();
        double bonus = Math.clamp((budget.output() - budget.load()) * 0.1, 0, 0.5);
        Loadout baked = new Loadout(
                loadout.ship(),
                loadout.armament(),
                new ShieldModel(
                        model.capacity(),
                        budget.regenPerSecond() * (1 + bonus),
                        model.regenDelaySeconds(),
                        model.breakSeconds()),
                loadout.plating());
        return baked.withPower(budget);
    }
}
