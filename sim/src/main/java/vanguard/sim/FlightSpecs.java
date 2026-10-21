package vanguard.sim;

/**
 * The parts of an {@link EnemySpec} stat block that describe how it flies: its formation paths, hovers, dives, spirals, side entries, swoops, ambushes, lane strafes and chases. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.Snake} ...).
 */
public interface FlightSpecs {
    /** A snake's units follow one another {@code spacingSeconds} apart. */
    public record Snake(double spacingSeconds) {}

    /** Hovers for {@code seconds}, {@code depth} px below the top edge of the play field. */
    public record Hover(Range seconds, Range depth) {}

    /** Circles a point at {@code radius} px and {@code degreesPerSecond}. */
    public record Orbit(double radius, double degreesPerSecond) {}

    /**
     * Pauses {@code depth} px below the top edge for {@code pauseSeconds}, then dives at
     * {@code speed} px/s at where the ship was, firing once when it passes the ship's height or
     * {@code fireAfterSeconds} into the dive.
     */
    public record Dive(Range depth, double pauseSeconds, double speed, double fireAfterSeconds) {}

    /**
     * Circles its release point for {@code seconds} at {@code turnsPerSecond} while its radius grows
     * {@code growth} px/s and the release point drifts down {@code drift} px/s; then flies on at its
     * speed along its outward angle turned downward, bouncing off the side edges up to
     * {@code ricochets} times.
     */
    public record Spiral(double seconds, double turnsPerSecond, double growth, double drift, int ricochets) {}

    /** Swings {@code amplitude} px to each side of its path, {@code periodSeconds} per full swing. */
    public record Sine(double amplitude, double periodSeconds) {}

    /**
     * A unit that enters from a side edge and hovers {@code edgeX} px from it (design/enemies/air/mantis);
     * with {@code exitBack} it leaves through that edge when its hover ends.
     */
    public record SideHover(double edgeX, boolean exitBack) {}

    /**
     * M7 part B, the {@code swoop} (design/enemies/air/dust-kite): flying its path, once its centre is
     * above the ship's centre by {@code triggerAbove} px or less, it leaves the path on one shallow curve at
     * {@code speed} px/s through the point where the ship was as the swoop started (aimed, no homing): a
     * quadratic curve whose control point lies {@code bend} px to its side of its start, halfway down; the
     * units of a wave bend to alternate sides (the first to the left). Past that point it flies straight
     * on along the curve's end and is gone once off the play field.
     */
    public record Swoop(double triggerAbove, double speed, double bend) {
        public Swoop {
            if (!(triggerAbove > 0) || !(speed > 0) || !(bend >= 0)) {
                throw new IllegalArgumentException("a swoop has a trigger distance, a speed and a bend");
            }
        }
    }

    /**
     * M5 part D, the path of a {@code rear ambush} wave's unit (design/enemies/air/wraith): it enters
     * at the top edge in its lane and flies straight down it at its speed, past the ship and off the
     * bottom edge; {@code gapSeconds} below it, it comes back up its lane to its {@link Hover} depth
     * (the hold point), decloaks there (its {@link Cloak}'s flash) and holds its hover time, its gun
     * counting its first-shot delay from the stop; then it leaves up the nearer side lane, its centre
     * {@code lane} px from that edge (a tie goes left), at {@code exitSpeed} px/s.
     */
    public record Ambush(double gapSeconds, double lane, double exitSpeed) {
        public Ambush {
            if (!(gapSeconds >= 0) || !(lane >= 0) || !(exitSpeed > 0)) {
                throw new IllegalArgumentException("an ambush has a gap, a lane and an exit speed");
            }
        }
    }

    /**
     * M7 part E, a lane strafe (design/enemies/air/ghost-drone): a unit entering at the top drops straight down at
     * {@code descentSpeed} px/s to its lane, banks 90° (a quarter turn of {@link #BANK_RADIUS} px, its speed
     * easing to the strafe's) and strafes across at {@code strafeSpeed} px/s at a fixed height toward the far side
     * from where it entered, leaving through that side edge; a unit entering from a side edge strafes in at its
     * lane and leaves through the opposite edge. A wave's lanes lie {@code laneSpacing} px apart from the top of
     * {@code laneDepth} (px below the top edge), closer when the wave has more units than the band holds at that
     * spacing, unit i on lane i. With {@code secondPass} (hard) a unit that entered at the top turns at the far
     * edge (a half turn of {@link #RETURN_RADIUS} px toward the bottom) and strafes back once, its gun's pass
     * trigger armed again, leaving through the edge it came from.
     */
    public record LaneStrafe(
            double descentSpeed, double strafeSpeed, Range laneDepth, double laneSpacing, boolean secondPass) {
        /** The radius of the bank from the descent into the strafe, px (pending round 41). */
        public static final double BANK_RADIUS = 24;
        /**
         * The radius of the second pass's half turn at the edge, px: the strafe back runs twice this lower (one
         * lane of the approved 40 px spacing; pending round 41).
         */
        public static final double RETURN_RADIUS = 20;

        public LaneStrafe {
            if (!(descentSpeed > 0) || !(strafeSpeed > 0)) {
                throw new IllegalArgumentException("a lane strafe flies: " + descentSpeed + ", " + strafeSpeed);
            }
            if (!(laneDepth.min() > 0) || !(laneDepth.max() < PlayField.HEIGHT) || !(laneSpacing >= 0)) {
                throw new IllegalArgumentException("a lane strafe's lanes lie on the play field: " + laneDepth);
            }
        }

        /** The depth of lane {@code lane} of a wave of {@code lanes} units, px below the top edge. */
        public double depth(int lane, int lanes) {
            double step = lanes > 1 ? Math.min(laneSpacing, (laneDepth.max() - laneDepth.min()) / (lanes - 1)) : 0;
            return laneDepth.min() + lane * step;
        }
    }

    /**
     * M5 part F, the {@code chase} movement (design/enemies/air/lamprey): from its entry (its centre
     * inside the play field, then a swarm member's start delay) it steers toward the ship's centre at
     * {@code speed} px/s, turning at most {@code turnRate} radians a second, for {@code seconds}; then
     * it flies straight on along its heading at its speed and leaves the screen.
     */
    public record Chase(double speed, double turnRate, double seconds) {
        public Chase {
            if (!(speed > 0) || !(turnRate > 0) || !(seconds > 0)) {
                throw new IllegalArgumentException("a chase has a speed, a turn rate and a time");
            }
        }
    }
}
