package vanguard.sim;

/**
 * The parts of an {@link EnemySpec} stat block that describe how it flies: its formation paths, hovers, dives, spirals, side entries, swoops, ambushes, lane strafes and chases. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.Snake} ...).
 */
public interface FlightSpecs {
    /** A snake's units follow one another {@code spacingSeconds} apart. */
    public record Snake(double spacingSeconds) {}

    /**
     * Hovers for {@code seconds}, {@code depth} px below the top edge of the play field.
     *
     * <p>M8 part D (design/enemies/naval/barbfin): with {@code aboveShip} each unit draws once, by its own seed, a
     * height in that range; coming down its path on the play field it stops as soon as its centre is that far above
     * the ship's centre (but never higher than the depth's min below the top edge); a path from the top edge then ends
     * at the depth's max (its formation's stop, which stops it at the latest; a side entry keeps its height). While it holds it drifts sideways at {@code drift} px/s (to a side drawn by
     * its seed, turning back at the side edges) and turns its facing toward the ship. Empty and 0 for the plain hover.
     *
     * <p>M9 part B (design/campaign Level 29, the wavering Stormhawks): with a {@code peelSpeed} above 0 its hover ends
     * in a peel-off: straight sideways toward the nearer side edge (a centre on the middle line goes right) at that
     * speed, gone past the edge, instead of its exit; 0 for none.
     */
    public record Hover(
            Range seconds, Range depth, java.util.Optional<Range> aboveShip, double drift, double peelSpeed) {
        public Hover {
            java.util.Objects.requireNonNull(aboveShip);
            if (!(drift >= 0) || (aboveShip.isPresent() && !(aboveShip.get().min() >= 0)) || !(peelSpeed >= 0)) {
                throw new IllegalArgumentException(
                        "a hover drifts 0 px/s or more, above the ship by 0 px or more, peels off at 0 px/s or more");
            }
        }

        /** The plain hover, without M8 part D's height above the ship and drift. */
        public Hover(Range seconds, Range depth) {
            this(seconds, depth, java.util.Optional.empty(), 0, 0);
        }

        /** M8 part D: a hover with a height above the ship and a drift, without a peel-off. */
        public Hover(Range seconds, Range depth, java.util.Optional<Range> aboveShip, double drift) {
            this(seconds, depth, aboveShip, drift, 0);
        }

        /** M8 part D: this hover held {@code above} px above the ship, drifting {@code driftSpeed} px/s. */
        public Hover aboveShip(Range above, double driftSpeed) {
            return new Hover(seconds, depth, java.util.Optional.of(above), driftSpeed, peelSpeed);
        }

        /** M9 part B: this hover ending in a peel-off to the nearer side edge at {@code speed} px/s (0: none). */
        public Hover withPeel(double speed) {
            return new Hover(seconds, depth, aboveShip, drift, speed);
        }
    }

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
     *
     * <p>M9 part F (design/enemies/space/void-leech): with a {@code drift} above 0 it first drifts from its entry at
     * {@code drift} px/s, steering toward its target's centre (the ship, or the prey it hunts) at its turn rate, and its
     * chase starts only once its centre is within {@code within} px of the target's; both 0 for the chase from the entry.
     */
    public record Chase(double speed, double turnRate, double seconds, double within, double drift) {
        public Chase {
            if (!(speed > 0) || !(turnRate > 0) || !(seconds > 0)) {
                throw new IllegalArgumentException("a chase has a speed, a turn rate and a time");
            }
            if (!(within >= 0) || !(drift >= 0) || (within > 0) != (drift > 0)) {
                throw new IllegalArgumentException(
                        "a chase drifts within a distance, or neither (M9 part F): " + within + ", " + drift);
            }
        }

        /** The chase from its entry (before M9 part F). */
        public Chase(double speed, double turnRate, double seconds) {
            this(speed, turnRate, seconds, 0, 0);
        }

        /** M9 part F: this chase after a drift at {@code driftSpeed} px/s until within {@code distance} px. */
        public Chase withDrift(double distance, double driftSpeed) {
            return new Chase(speed, turnRate, seconds, distance, driftSpeed);
        }

        /** M9 part F: whether it drifts before its chase. */
        public boolean drifts() {
            return drift > 0;
        }
    }

    /**
     * M8 part E, the coiling orbit (design/enemies/naval/spiral-nautilus; {@link Coiling}): a unit that orbits the ship
     * in a closed, armoured shell, spiralling in from {@code outerRadius} to {@code innerRadius} px over {@code
     * inSeconds} at its speed, never closer than {@code innerRadius}; then it uncoils: {@code openingSeconds} opening
     * (still armoured, its crown's tell the last {@code tellSeconds} of it) and {@code openSeconds} open (hittable, its
     * spiral gun firing) at {@code openSpeed} px/s; then it closes, breaks outward and spirals in again. A wave's units
     * orbit in turn the other way round (its first counter-clockwise), each cycle {@code offsetSeconds} after the one
     * before; they leave after their wave's hold.
     */
    public record Coil(
            double outerRadius,
            double innerRadius,
            double inSeconds,
            double openingSeconds,
            double tellSeconds,
            double openSeconds,
            double openSpeed,
            double offsetSeconds) {
        public Coil {
            if (!(innerRadius > 0)
                    || !(outerRadius >= innerRadius)
                    || !(inSeconds > 0)
                    || !(openingSeconds >= 0)
                    || !(tellSeconds >= 0 && tellSeconds <= openingSeconds)
                    || !(openSeconds > 0)
                    || !(openSpeed > 0)
                    || !(offsetSeconds >= 0)) {
                throw new IllegalArgumentException("a coil spirals in, opens and closes: " + outerRadius + " → "
                        + innerRadius + " px, " + inSeconds + " s, " + openingSeconds + " s (tell " + tellSeconds
                        + " s), " + openSeconds + " s at " + openSpeed + " px/s, offset " + offsetSeconds + " s");
            }
        }
    }

    /**
     * M8 part F, the lunge from a lure (design/enemies/naval/glow-angler; {@link Lunging}; m8f-gaps.md §1.1, §5 F2): a
     * unit that hangs drifting with the water (a field's current, else its path's way, at its speed, scrolling with the
     * ground) and, armed and on the play field with the ship's centre within {@code trigger} px of its centre (its lure
     * lies inside that, so touching the lure needs no test of its own), dives at {@code speed} px/s at where the ship was
     * then, stopping {@code stopShort} px short of that point or after {@code seconds}; at the dive's end its {@code
     * ring} of bullets at {@code ringSpeed} px/s, each dealing {@code ringDamage}, is due, and it retreats away from the
     * ship at {@code retreatSpeed} px/s for {@code retreatSeconds}, then drifts again. A ring due while the ship is closer
     * than bullets may spawn waits for room within the retreat (none once it is over); it re-arms {@code rearmSeconds}
     * after its ring (or after its retreat when none fired).
     */
    public record Lunge(
            double trigger,
            double speed,
            double seconds,
            double stopShort,
            int ring,
            double ringSpeed,
            double ringDamage,
            double retreatSpeed,
            double retreatSeconds,
            double rearmSeconds) {
        public Lunge {
            if (!(stopShort >= 0)
                    || !(trigger > stopShort)
                    || !(speed > 0)
                    || !(seconds > 0)
                    || ring < 1
                    || !(ringSpeed > 0)
                    || !(ringDamage >= 0)
                    || !(retreatSpeed >= 0)
                    || !(retreatSeconds >= 0)
                    || !(rearmSeconds >= 0)) {
                throw new IllegalArgumentException("a lunge triggers beyond its stop and fires a ring: trigger "
                        + trigger + " px, " + speed + " px/s for " + seconds + " s, " + stopShort
                        + " px short, a ring of "
                        + ring + " at " + ringSpeed + " px/s, retreat " + retreatSpeed + " px/s for " + retreatSeconds
                        + " s, re-arm " + rearmSeconds + " s");
            }
        }
    }

    /**
     * M9 part B (design/enemies/air/talon, Squad leader; Level 29): how a wing's member breaks off when its wave's leader
     * is killed: sideways, away from the wing's centre (the nearer side edge for one on the centre), at {@code speed}
     * px/s, faster when it needs to be to leave by that edge within {@code seconds}; it holds its fire, and with {@code
     * partingPair} (hard) it fires one volley of its gun as it breaks off.
     */
    public record SquadScatter(double speed, double seconds, boolean partingPair) {
        public SquadScatter {
            if (!(speed > 0) || !(seconds > 0)) {
                throw new IllegalArgumentException(
                        "a scatter breaks off at a speed for a time: " + speed + " px/s, " + seconds + " s");
            }
        }
    }

    /**
     * M9 part B (design/enemies/air/talon, Movement; the enemies' {@code swoop} vocabulary, not the Dust Kite's triggered
     * {@link Swoop}): its wave ({@code V-wing}, {@code line abreast}, {@code column} or {@code pincer}; any other
     * formation flies as before) flies one smooth curve across the upper half instead of its formation's path, the
     * lowest point {@code lowest} px below the top edge drawn once per wave. From the front each unit's curve enters at
     * the top edge heading down {@code across} / 2 px before its formation place, bottoms out at its place and leaves
     * heading up through the top edge (or out through a side edge) {@code across} / 2 px beyond it; a wave in the left
     * edge's column sweeps right, in the right edge's left, a centred one toward a side drawn per wave. A {@code
     * pincer}'s wings enter at their side edges 60 px below the top edge, bottom out at that depth and leave up through
     * the top edge {@code across} px in from their own edge. The units keep their formation's shape round the curve:
     * each flies it shifted to its place, the ranks behind one after another. It fires while on the play field within
     * {@code fireBand} px of the top edge, its first shot after its gun's first-shot delay counted in that band.
     */
    public record SwoopPath(Range lowest, double across, double fireBand) {
        public SwoopPath {
            if (!(lowest.min() > 0) || !(across >= 0) || !(fireBand > 0)) {
                throw new IllegalArgumentException("a swoop path bottoms out below the top edge and fires in a band: "
                        + lowest + ", " + across + " px across, band " + fireBand + " px");
            }
        }
    }

    /**
     * M9 part B (design/enemies, Rock riding; design/enemies/space/asteroid-mite): a unit riding a small drifting rock
     * of its wave ({@link WaveSpec#onRocks()}). It clings to the rock's rim, crawling round it at {@code
     * crawlDegreesPerSecond}; when the ship's centre comes within {@code within} px of the rock's, the rock's riders leap
     * one after another, {@code gapSeconds} apart in a seeded order, each after a {@code tellSeconds} tell: a dive
     * straight at the ship's position at the tell's end at {@code diveSpeed} px/s, no steering. A broken rock frees its
     * riders: each tumbles off for {@code tumbleSeconds}, then leaps after its tell in the same order. Riders carried
     * off the screen on their rock leave with it (no credits).
     */
    public record Ride(
            double within,
            double tellSeconds,
            double gapSeconds,
            double diveSpeed,
            double tumbleSeconds,
            double crawlDegreesPerSecond) {
        public Ride {
            if (!(within > 0)
                    || !(tellSeconds > 0)
                    || !(gapSeconds >= 0)
                    || !(diveSpeed > 0)
                    || !(tumbleSeconds >= 0)
                    || !Double.isFinite(crawlDegreesPerSecond)) {
                throw new IllegalArgumentException("a ride leaps within a distance after a tell: " + within + " px, "
                        + tellSeconds + " s, " + gapSeconds + " s apart, " + diveSpeed + " px/s, tumble "
                        + tumbleSeconds + " s, crawl " + crawlDegreesPerSecond + " deg/s");
            }
        }
    }
    /**
     * M9 part D, the dash cycle (design/enemies/air/buzzsaw-drone; {@link Dashing}): from its entry (its centre inside the
     * play field) it chases the ship at its speed, turning at most {@code turnRate} radians a second, for {@code
     * chaseSeconds}; then its spin-up tell of {@code tellSeconds} (never under {@link #LEAST_TELL_SECONDS}) at {@code
     * tellSpeed} px/s along its heading; then a straight dash at {@code dashSpeed} px/s along the bearing to the ship at
     * the tell's start (locked), bouncing off the play field's edges and the large debris chunks (equal angles; a small
     * chunk it touches breaks and does not deflect it) until it has bounced {@code bounces} times; then it slows to its
     * speed over {@code slowSeconds} and chases again. After its {@code dashes}-th dash it leaves through the nearest
     * edge at its speed.
     */
    public record Dash(
            double turnRate,
            double chaseSeconds,
            double tellSeconds,
            double tellSpeed,
            double dashSpeed,
            int bounces,
            double slowSeconds,
            int dashes) {
        /** The tell is the rule: never shorter than this at any difficulty (design: ≥ 0.5 s). */
        public static final double LEAST_TELL_SECONDS = 0.5;

        public Dash {
            if (!(turnRate > 0)
                    || !(chaseSeconds > 0)
                    || !(tellSeconds >= LEAST_TELL_SECONDS)
                    || !(tellSpeed >= 0)
                    || !(dashSpeed > 0)
                    || bounces < 1
                    || !(slowSeconds > 0)
                    || dashes < 1) {
                throw new IllegalArgumentException("a dash chases, tells at least " + LEAST_TELL_SECONDS
                        + " s, dashes, bounces and slows: " + turnRate + ", " + chaseSeconds + ", " + tellSeconds
                        + ", " + tellSpeed + ", " + dashSpeed + ", " + bounces + ", " + slowSeconds + ", " + dashes);
            }
        }
    }
}
