package vanguard.sim;

import java.util.Optional;

/**
 * The parts of an {@link EnemySpec} stat block that describe a walker's: its walk, pounce, curl-roll, turret and rear plate. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.Walker} ...).
 */
public interface WalkerSpecs {
    /**
     * A walker (design/enemies/ground/scuttler): it walks its wave's ground path at {@code speed}
     * px/s over the ground, its facing turning at most {@code turnRate} radians per second; one walk
     * cycle per {@code stride} px. Direct shots arriving within {@code frontArc} radians of its
     * facing glance off; its {@link EnemySpec#gun()} is a fan along its facing (the Scuttler) or, with
     * {@code fanAtShip}, centred on the ship (the Creeper, M5 part B), and {@code spit} an aimed
     * attack that fires only while the ship is more than {@code awayRadians} off its facing.
     *
     * @param staggerSeconds M5 part B (design/enemies/ground/creeper): with more than 0, the units of
     *     one wave share a volley clock that starts as the first of them comes onto the screen; unit
     *     i (in the order they enter) fires its fan i × this many seconds after the volley's start,
     *     the first volley half an interval after the clock starts and then one every interval; a
     *     unit off the screen skips its turn. 0: each unit keeps its own clock
     */
    public record Walker(
            double speed,
            double turnRate,
            double stride,
            double frontArc,
            Optional<EnemyGun> spit,
            double awayRadians,
            boolean fanAtShip,
            double staggerSeconds) {
        /** A walker whose fan goes along its facing, each unit on its own clock (Level 04's Scuttler). */
        public Walker(
                double speed,
                double turnRate,
                double stride,
                double frontArc,
                Optional<EnemyGun> spit,
                double awayRadians) {
            this(speed, turnRate, stride, frontArc, spit, awayRadians, false, 0);
        }

        /** Whether the units of a wave share a staggered volley clock. */
        public boolean staggered() {
            return staggerSeconds > 0;
        }
    }

    /**
     * A pounce (design/enemies/ground/ravager, M5 part C): a walker on the screen whose centre is
     * within {@code range} px of the ship's leaps at the ship's position at take-off over {@code
     * leapSeconds} (no homing); for the middle {@code airSeconds} of the leap its current layer is
     * {@link Layer#AIR} (contact with the ship and the wingman once per leap, air-reaching weapons,
     * mines), else {@link Layer#GROUND}; drawn up to {@code scale} at the apex. It lands, rejoins its
     * path and may leap again {@code intervalSeconds} after landing.
     */
    public record Pounce(double range, double leapSeconds, double airSeconds, double scale, double intervalSeconds) {
        public Pounce {
            if (!(range > 0)
                    || !(leapSeconds > 0)
                    || airSeconds < 0
                    || airSeconds > leapSeconds
                    || !(scale > 0)
                    || intervalSeconds < 0) {
                throw new IllegalArgumentException("a pounce has a range, a leap and its air window inside it");
            }
        }
    }

    /**
     * M7 part D, a walker's curl-roll (design/enemies/ground/shellback): once, when its HP falls to {@code
     * belowShare} of its stat block's (or below) without destroying it, it curls for {@code curlSeconds},
     * rolls {@code rollSeconds} along its walk path at {@code rollSpeed} px/s (on along its last heading past
     * the path's end), uncurls for {@code uncurlSeconds} and shows its belly for {@code bellySeconds}, taking
     * {@code bellyFactor} × the damage, then walks on. From the curl's start to the uncurl's end its hit box is
     * the {@code ball}, every damage glances off and its gun waits; it stands while it curls, uncurls and
     * shows its belly. The rolling ball crushes ground allies (one hit per roll per ally, round 40: where the
     * ball touches the ally's footprint) and destructible ground objects in its path, never enemies or the ship.
     * Round 40 R.e: while it drifts down the screen, a roll ends early once the scroll would carry it below {@code
     * bellyRow} px above the bottom edge before its uncurl ends, so its belly shows at that row or higher (0: never
     * early).
     */
    public record Roll(
            double belowShare,
            double curlSeconds,
            double rollSeconds,
            double rollSpeed,
            double uncurlSeconds,
            double bellySeconds,
            double bellyFactor,
            Hitbox ball,
            double bellyRow) {
        public Roll {
            if (!(belowShare > 0 && belowShare < 1) || !(rollSpeed > 0) || !(bellyFactor >= 1)) {
                throw new IllegalArgumentException("a roll starts below a share of the HP: " + belowShare);
            }
            if (!(bellyRow >= 0 && bellyRow < PlayField.HEIGHT)) {
                throw new IllegalArgumentException("a roll's belly row lies on the play field: " + bellyRow);
            }
        }

        /** A roll that always runs its full time (no belly row). */
        public Roll(
                double belowShare,
                double curlSeconds,
                double rollSeconds,
                double rollSpeed,
                double uncurlSeconds,
                double bellySeconds,
                double bellyFactor,
                Hitbox ball) {
            this(belowShare, curlSeconds, rollSeconds, rollSpeed, uncurlSeconds, bellySeconds, bellyFactor, ball, 0);
        }
    }

    /**
     * M7 part F, a turret turning independently of its unit's hull (design/enemies/ground/warden-tank): it turns
     * toward its target (the ship, or the objective the hook picks) at {@code turnRate} radians per second all
     * round, while the unit is on the screen; once its gun is ready and its aim is within {@code withinRadians} of
     * the target it stops turning for {@code lockSeconds} (the tell) and then fires its burst along its aim.
     */
    public record Turret(double turnRate, double lockSeconds, double withinRadians) {
        public Turret {
            if (!(turnRate > 0) || !(lockSeconds >= 0) || !(withinRadians >= 0)) {
                throw new IllegalArgumentException("a turret turns, locks 0 s or more and aims within an angle");
            }
        }
    }

    /**
     * M7 part F, a walker's thin rear plate (design/enemies/ground/warden-tank): a direct shot (a bolt, a missile,
     * a beam) arriving within {@code halfArcRadians} of its rear (flying along its facing) does {@code multiplier}
     * × its damage to the body; a unit part's damage is its own.
     */
    public record RearPlate(double halfArcRadians, double multiplier) {
        public RearPlate {
            if (!(halfArcRadians >= 0) || !(halfArcRadians <= StrictMath.PI) || !(multiplier > 0)) {
                throw new IllegalArgumentException("a rear plate has an arc and a multiplier");
            }
        }
    }
}
