package vanguard.sim;

/**
 * The parts of an {@link EnemySpec} stat block that describe its attacks other than its gun: sweeps, proximity rings, death bursts, latches and pulls. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.Sweep} ...).
 */
public interface AttackSpecs {
    /**
     * A laser sweep (design/enemies/air/mantis): while it hovers, every {@code intervalSeconds} (the
     * first {@code firstDelaySeconds} after it settles) a telegraph of {@code telegraphSeconds}
     * fixes the sweep's centre on the ship's bearing, clamped between straight inward and straight
     * down; then a beam {@code length} × {@code width} px from its eye sweeps {@code arcRadians}
     * over {@code sweepSeconds}, dealing {@code damage} at most once per sweep. A sweep starts only
     * if it ends before the hover does. The eye, where the beam and its telegraph start, is
     * {@code originIn} px from the unit's centre toward the field (mirrored on the right edge) and
     * {@code originDown} px down the screen.
     *
     * <p>Round 41 (the 2026-10-19 playtest, the Siege Spire's sweep; a boss's only): with a {@code
     * leadRadians}, the arc is not centred on the ship's bearing at the telegraph but starts that far from it
     * on the side the beam comes from, so the telegraph's line lies just beside the ship: a ship that crosses
     * it before the beam fires is behind the beam (safe), one that stands still or moves the other way is
     * swept. NaN (the default) centres the arc on the bearing.
     */
    public record Sweep(
            double arcRadians,
            double sweepSeconds,
            double telegraphSeconds,
            double length,
            double width,
            double intervalSeconds,
            double firstDelaySeconds,
            double damage,
            double originIn,
            double originDown,
            double leadRadians) {
        public Sweep {
            if (!Double.isNaN(leadRadians) && !(leadRadians >= 0 && leadRadians <= arcRadians)) {
                throw new IllegalArgumentException("a sweep's lead lies within its arc: " + leadRadians);
            }
        }

        /** A sweep centred on the ship's bearing (without round 41's lead). */
        public Sweep(
                double arcRadians,
                double sweepSeconds,
                double telegraphSeconds,
                double length,
                double width,
                double intervalSeconds,
                double firstDelaySeconds,
                double damage,
                double originIn,
                double originDown) {
            this(
                    arcRadians,
                    sweepSeconds,
                    telegraphSeconds,
                    length,
                    width,
                    intervalSeconds,
                    firstDelaySeconds,
                    damage,
                    originIn,
                    originDown,
                    Double.NaN);
        }

        /** Round 41: this sweep with its first edge {@code radians} from the ship's bearing (NaN: centred). */
        public Sweep withLead(double radians) {
            return new Sweep(
                    arcRadians,
                    sweepSeconds,
                    telegraphSeconds,
                    length,
                    width,
                    intervalSeconds,
                    firstDelaySeconds,
                    damage,
                    originIn,
                    originDown,
                    radians);
        }

        /** Round 41: whether its arc starts {@link #leadRadians()} from the ship's bearing (else centred on it). */
        public boolean leads() {
            return !Double.isNaN(leadRadians);
        }

        /** A sweep from the unit's centre. */
        public Sweep(
                double arcRadians,
                double sweepSeconds,
                double telegraphSeconds,
                double length,
                double width,
                double intervalSeconds,
                double firstDelaySeconds,
                double damage) {
            this(
                    arcRadians,
                    sweepSeconds,
                    telegraphSeconds,
                    length,
                    width,
                    intervalSeconds,
                    firstDelaySeconds,
                    damage,
                    0,
                    0);
        }
    }

    /**
     * M5 part E, a proximity ring (design/enemies/naval/driftjelly, user decision E3 = b): while the
     * ship's centre is within {@code within} px of its centre (on either of its layers) it fires a
     * ring of {@code count} bullets at {@code bulletSpeed} px/s, each dealing {@code damage}, at most
     * once every {@code cooldownSeconds} (counted from the last ring). A ring due while the ship is
     * closer than the bullets may spawn waits (design/enemies, bullet readability rules).
     */
    public record ProximityRing(int count, double bulletSpeed, double damage, double within, double cooldownSeconds) {
        public ProximityRing {
            if (count < 1 || !(bulletSpeed > 0) || !(within > 0) || !(cooldownSeconds > 0)) {
                throw new IllegalArgumentException("a proximity ring has bullets, a speed, a reach and a cooldown");
            }
        }
    }

    /** {@code count} bullets in a ring at {@code speed} px/s, each dealing {@code damage}. */
    public record DeathBurst(int count, double speed, double damage) {}

    /**
     * M5 part F, the {@code latch} block with the {@code drain} attack (design/enemies/air/lamprey; user
     * decisions F3 and F4 = a). Touching the ship while it chases or flies on, with fewer than
     * {@code slots} units latched, it latches at a hull spot ({@link #spots}, indexed by {@link #LEFT},
     * {@link #RIGHT} and {@link #TAIL}: the tail for a touch within 45 degrees of straight down from the
     * ship's centre, else the side by its x, a tie to the left) and moves with the ship, draining
     * {@code drainRate} shield points a second (the shield only, its regeneration paused, nothing once
     * it broke) and taking {@code damageFactor} times the damage. {@code reversals} reversals of the
     * commanded horizontal direction within {@code withinSeconds} throw off every latched unit: thrown
     * outward from its spot at {@code throwSpeed} px/s, slowing to a stop over {@code stunSeconds}, it
     * then flees at its speed to the nearer side edge and never latches again.
     *
     * @param spots the offsets of the left, right and tail spots from the ship's centre, px, y up
     */
    public record Latch(
            int slots,
            java.util.List<Spot> spots,
            double damageFactor,
            int reversals,
            double withinSeconds,
            double stunSeconds,
            double throwSpeed,
            double drainRate) {
        /** The spot on the hull's left side. */
        public static final int LEFT = 0;
        /** The spot on the hull's right side. */
        public static final int RIGHT = 1;
        /** The spot at the hull's tail. */
        public static final int TAIL = 2;

        public Latch {
            spots = java.util.List.copyOf(spots);
            if (slots < 1 || spots.size() != 3 || reversals < 2 || !(withinSeconds > 0) || !(stunSeconds > 0)) {
                throw new IllegalArgumentException("a latch has slots, the left, right and tail spots, a shake of at"
                        + " least two reversals within a time, and a stun");
            }
            if (!(damageFactor > 0) || throwSpeed < 0 || drainRate < 0) {
                throw new IllegalArgumentException(
                        "a latch's damage factor is positive, its throw and drain not negative");
            }
        }

        /** The hull spot for a touch at ({@code dx}, {@code dy}) from the ship's centre, y up. */
        public static int spotFor(double dx, double dy) {
            if (dy < 0 && Math.abs(dx) <= -dy) {
                return TAIL;
            }
            return dx <= 0 ? LEFT : RIGHT;
        }
    }

    /** A hull spot's offset from the ship's centre, px, y up. */
    public record Spot(double dx, double dy) {}

    /**
     * M7 part E, a radial pull toward the unit's centre (design/enemies/ground/dust-devil): on the ship within
     * {@code shipRadius} px, {@code shipSpeed} px/s at the centre fading evenly to 0 at the edge, added to the
     * ship's step; on the loose pickups within {@code pickupRadius} px at {@code pickupSpeed} px/s (a fitted
     * Pickup magnet's pull wins where it is the stronger). A pickup that reaches the centre is held.
     */
    public record Pull(double shipRadius, double shipSpeed, double pickupRadius, double pickupSpeed) {
        public Pull {
            if (!(shipRadius >= 0) || !(shipSpeed >= 0) || !(pickupRadius >= 0) || !(pickupSpeed >= 0)) {
                throw new IllegalArgumentException("a pull's radii and speeds are 0 or more");
            }
        }

        /** Its speed on the ship at {@code distance} px from the centre, px/s; 0 at the edge and beyond. */
        public double shipSpeedAt(double distance) {
            return distance >= shipRadius ? 0 : shipSpeed * (1 - distance / shipRadius);
        }
    }
}
