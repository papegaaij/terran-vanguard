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

    /**
     * {@code count} bullets in a ring at {@code speed} px/s, each dealing {@code damage}. M10 part A (design/enemies/
     * air/harrow, the {@code death-burst} on contact): with {@code onContact} the ring comes on every death however
     * close the ship is (a ram's included), and its bullets cannot hit the ship for their first {@code graceSeconds}
     * after a ram; with {@code between} the ring is turned so that the ship's bearing falls between two bullets.
     */
    public record DeathBurst(
            int count, double speed, double damage, boolean onContact, boolean between, double graceSeconds) {
        public DeathBurst {
            if (!(graceSeconds >= 0) || (!onContact && graceSeconds > 0)) {
                throw new IllegalArgumentException("a death burst's grace is a burst on contact's: " + graceSeconds);
            }
        }

        /** A ring that spawns only clear of the ship (before Act 6). */
        public DeathBurst(int count, double speed, double damage) {
            this(count, speed, damage, false, false, 0);
        }
    }

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
     * <p>M9 part F (design/enemies/space/void-leech): with {@code drainsPower} a latched unit lowers the generator's
     * effective output instead (one drainer of {@link PowerState}; no shield drain, {@code drainRate} 0); with {@code
     * empThrows} the EMP Burst throws every latched unit off as a shake-off (the Lamprey's: it stays latched, its drain
     * paused); with a {@code preyDrift} above 0 it latches on a touched free {@link EnemySpec.PreyRole#PREY} unit
     * instead of the ship (that unit's shield emptied for good, no fire, drifting down at {@code preyDrift} px/s until
     * it leaves; {@link PreyLatches}).
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
            double drainRate,
            boolean drainsPower,
            boolean empThrows,
            double preyDrift) {
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
            if (!(damageFactor > 0) || throwSpeed < 0 || drainRate < 0 || !(preyDrift >= 0)) {
                throw new IllegalArgumentException(
                        "a latch's damage factor is positive, its throw, drain and prey drift not negative");
            }
        }

        /** The Lamprey's latch (before M9 part F): it drains the shield, the EMP leaves it latched, no prey. */
        public Latch(
                int slots,
                java.util.List<Spot> spots,
                double damageFactor,
                int reversals,
                double withinSeconds,
                double stunSeconds,
                double throwSpeed,
                double drainRate) {
            this(
                    slots,
                    spots,
                    damageFactor,
                    reversals,
                    withinSeconds,
                    stunSeconds,
                    throwSpeed,
                    drainRate,
                    false,
                    false,
                    0);
        }

        /** M9 part F: this latch draining the generator's output ({@code power}) instead of the shield. */
        public Latch withDrainsPower(boolean power) {
            return new Latch(
                    slots,
                    spots,
                    damageFactor,
                    reversals,
                    withinSeconds,
                    stunSeconds,
                    throwSpeed,
                    drainRate,
                    power,
                    empThrows,
                    preyDrift);
        }

        /** M9 part F: this latch thrown off by the EMP Burst ({@code thrown}) instead of staying latched. */
        public Latch withEmpThrows(boolean thrown) {
            return new Latch(
                    slots,
                    spots,
                    damageFactor,
                    reversals,
                    withinSeconds,
                    stunSeconds,
                    throwSpeed,
                    drainRate,
                    drainsPower,
                    thrown,
                    preyDrift);
        }

        /** M9 part F: this latch latching on prey too, its prey drifting down at {@code speed} px/s (0: none). */
        public Latch withPreyDrift(double speed) {
            return new Latch(
                    slots,
                    spots,
                    damageFactor,
                    reversals,
                    withinSeconds,
                    stunSeconds,
                    throwSpeed,
                    drainRate,
                    drainsPower,
                    empThrows,
                    speed);
        }

        /** M9 part F: whether it latches on a touched prey unit. */
        public boolean hunts() {
            return preyDrift > 0;
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

    /**
     * M8 part E, a sung ring that slows (design/enemies/naval/siren, its {@code ring} attack): {@code firstSeconds}
     * after it enters, then every {@code intervalSeconds}, a ring of sound leaves its sac, each told by the sac's glow
     * for the {@code tellSeconds} before it. The ring expands from the sac at {@code speed} px/s (sound: not the water's
     * x 0.7) to a radius of {@code radius} px, its band {@code band} px wide behind its edge; it deals no damage. Each
     * hull it reaches (the ship, Rook) is tested once, when its band first touches the hull, with a clear line from the
     * sac (rock stops it; a dash passes): a hit slows the hull to {@code slow} of its speed and acceleration for {@code
     * slowSeconds}, then makes it immune to slows for {@code immunitySeconds}. Shooting the sac ends its song (a ring
     * told or in flight with it).
     */
    public record SlowRing(
            double firstSeconds,
            double intervalSeconds,
            double tellSeconds,
            double speed,
            double radius,
            double band,
            double slow,
            double slowSeconds,
            double immunitySeconds) {
        public SlowRing {
            if (!(firstSeconds >= 0)
                    || !(intervalSeconds > 0)
                    || !(tellSeconds >= 0)
                    || !(speed > 0)
                    || !(radius > 0)
                    || !(band > 0)
                    || !(slow > 0 && slow <= 1)
                    || !(slowSeconds > 0)
                    || !(immunitySeconds >= 0)) {
                throw new IllegalArgumentException("a slowing ring needs its times, its speed, its radius and band, and"
                        + " a slow between 0 and 1");
            }
            if (tellSeconds > intervalSeconds) {
                throw new IllegalArgumentException("a ring's tell fits between two rings: " + tellSeconds);
            }
        }
    }

    /**
     * M9 part A, a {@code laser-line} (design/enemies/ground/rail-bunker; the Sovereign's batteries): every {@code
     * intervalSeconds}, telegraph to telegraph (the first {@code firstDelaySeconds} after its start), while the unit is
     * on the play field and the ship is farther than {@code holdWithinPx} from it, a telegraph of {@code
     * telegraphSeconds} locks a line from its centre through the ship's position to the screen's edge (lock, not
     * track: the direction is locked, the origin rides with the unit, round 66's C.1 b); then the line is live {@code liveSeconds}: a band {@code bandPx} wide that deals {@code damage} to the
     * ship once. Enemy fire never hurts enemies. The EMP Burst's stun cancels a running telegraph (no shot).
     */
    public record LaserLine(
            double telegraphSeconds,
            double liveSeconds,
            double bandPx,
            double damage,
            double intervalSeconds,
            double firstDelaySeconds,
            double holdWithinPx,
            int mostTelegraphing) {
        /** A line without the gate (before M9 part F). */
        public LaserLine(
                double telegraphSeconds,
                double liveSeconds,
                double bandPx,
                double damage,
                double intervalSeconds,
                double firstDelaySeconds,
                double holdWithinPx) {
            this(telegraphSeconds, liveSeconds, bandPx, damage, intervalSeconds, firstDelaySeconds, holdWithinPx, 0);
        }

        /**
         * M9 part F (design/enemies/ground/rail-bunker, Level 33): this line waiting, when due, while {@code most}
         * lines of the level (any unit's) telegraph (0: no gate).
         */
        public LaserLine withMostTelegraphing(int most) {
            return new LaserLine(
                    telegraphSeconds,
                    liveSeconds,
                    bandPx,
                    damage,
                    intervalSeconds,
                    firstDelaySeconds,
                    holdWithinPx,
                    most);
        }

        public LaserLine {
            if (!(telegraphSeconds > 0)
                    || !(liveSeconds > 0)
                    || !(bandPx > 0)
                    || !(damage >= 0)
                    || !(intervalSeconds >= telegraphSeconds + liveSeconds)
                    || !(firstDelaySeconds >= 0)
                    || !(holdWithinPx >= 0)
                    || mostTelegraphing < 0) {
                throw new IllegalArgumentException("a laser line telegraphs, lives, has a band and an interval that "
                        + "holds both: " + telegraphSeconds + ", " + liveSeconds + ", " + intervalSeconds);
            }
        }
    }

    /**
     * M9 part C (design/enemies/space/minelayer; design/enemies, Mine lines; Level 30's C1 a): a mine layer's lay
     * path and line. It enters below the bottom edge in a side lane {@code laneInset} px in from that edge, rises at
     * {@code riseSpeed} px/s to its lay row (its wave's: {@link WaveSpec.MineLines}), then strafes across to the far edge
     * at {@code strafeSpeed} px/s and leaves past it, laying its gun's line mine at each mine's x as it passes (the mines
     * between its lane and its own edge as it turns), at its own height: the mines hold their place in the field, so
     * the line slopes. The line is {@code clusters} clusters of {@code perCluster} mines {@code spacing} px apart
     * ({@code middleShort} fewer in each middle cluster: easy's 3 beside a wider gap), {@code clusterGap} px between two
     * clusters and the one {@code gap} at the cluster boundary nearest its wave's gap point, centred on the play field.
     * It lays nothing while {@code most} line mines are alive (an unfinished line is not topped up); its unlaid mines
     * are lost with it.
     */
    public record MineLayer(
            double riseSpeed,
            double strafeSpeed,
            double laneInset,
            int clusters,
            int perCluster,
            double spacing,
            double clusterGap,
            double gap,
            int middleShort,
            int most) {
        /** The most mines a line has. */
        public static final int MOST_MINES = 32;

        public MineLayer {
            if (!(riseSpeed > 0) || !(strafeSpeed > 0) || !(laneInset > 0) || !(laneInset < PlayField.WIDTH / 2.0)) {
                throw new IllegalArgumentException("a mine layer has a rise, a strafe and a lane inside the field");
            }
            if (clusters < 2 || perCluster < 1 || middleShort < 0 || middleShort >= perCluster || most < 1) {
                throw new IllegalArgumentException(
                        "a mine line has at least two clusters (its gap is at a boundary), mines in each, a limit");
            }
            if (!(spacing > 0) || !(clusterGap > 0) || !(gap > 0)) {
                throw new IllegalArgumentException("a mine line's spacing, cluster gap and gap are positive");
            }
            if (mines() > MOST_MINES || !(width() <= PlayField.WIDTH)) {
                throw new IllegalArgumentException(
                        "a mine line has at most " + MOST_MINES + " mines and fits the play field: " + width());
            }
        }

        /** The mines of cluster {@code k}. */
        public int clusterMines(int k) {
            return k > 0 && k < clusters - 1 ? perCluster - middleShort : perCluster;
        }

        /** The mines of a whole line. */
        public int mines() {
            int n = 0;
            for (int k = 0; k < clusters; k++) {
                n += clusterMines(k);
            }
            return n;
        }

        /** A whole line's width, its outer mines' centres apart. */
        public double width() {
            double w = (clusters - 2) * clusterGap + gap;
            for (int k = 0; k < clusters; k++) {
                w += (clusterMines(k) - 1) * spacing;
            }
            return w;
        }

        /** The middle of the gap when it is at boundary {@code b} (between cluster b and b + 1), px from the left edge. */
        public double gapMiddle(int b) {
            double x = (PlayField.WIDTH - width()) / 2;
            for (int k = 0; k <= b; k++) {
                x += (clusterMines(k) - 1) * spacing + (k < b ? clusterGap : 0);
            }
            return x + gap / 2;
        }

        /** The cluster boundary whose gap middle is nearest {@code x} (the lower on a tie). */
        public int gapBoundary(double x) {
            int best = 0;
            for (int b = 1; b < clusters - 1; b++) {
                if (Math.abs(gapMiddle(b) - x) < Math.abs(gapMiddle(best) - x)) {
                    best = b;
                }
            }
            return best;
        }

        /** Fills {@code xs} with the line's mines' x, left to right, with its gap at boundary {@code b}; returns how many. */
        public int positions(int b, double[] xs) {
            double x = (PlayField.WIDTH - width()) / 2;
            int n = 0;
            for (int k = 0; k < clusters; k++) {
                for (int m = 0; m < clusterMines(k); m++) {
                    xs[n++] = x;
                    x += m < clusterMines(k) - 1 ? spacing : 0;
                }
                x += k == b ? gap : clusterGap;
            }
            return n;
        }
    }

    /**
     * M9 part C (design/enemies/air/gilded-gunship, the arcs): facing armour on a unit that always faces down the screen
     * (its fixed orientation). A direct hit arriving within {@code frontHalfArcRadians} of its facing (flying up into
     * its nose) does {@code frontFactor} of its damage; one arriving within {@code rearHalfArcRadians} of its rear (flying
     * down onto its engines) does {@code rearFactor}. Its hex shield spends itself first at full damage; only the
     * overflow takes the factor. Blasts, bombs, shells and the specials take neither (they come from above).
     */
    public record FacingArmour(
            double frontHalfArcRadians, double frontFactor, double rearHalfArcRadians, double rearFactor) {
        public FacingArmour {
            if (!(frontHalfArcRadians >= 0)
                    || !(rearHalfArcRadians >= 0)
                    || !(frontHalfArcRadians + rearHalfArcRadians <= StrictMath.PI)
                    || !(frontFactor > 0)
                    || !(rearFactor > 0)) {
                throw new IllegalArgumentException("facing armour has two arcs that do not overlap and two factors");
            }
        }
    }
    /**
     * M9 part D, the air links (design/enemies, Air links; design/enemies/space/shard-drone; {@link AirLinks}): a wave of
     * these units is one linked group (3–4), each unit linked to the next in its wave's order by an energy link (a
     * {@code circle} wave closes the ring). A link is harmless during its tell, {@code tellSeconds} after the group's
     * last unit entered the play field (and after an EMP stun of either end); lit, it touches a hull within {@code band}
     * ÷ 2 px of it for {@code damage}, at most once every {@code cooldownSeconds} (the ship's own cooldown, shared by
     * every link; the ship is not pushed). Every shot passes through it; killing either end breaks it. A {@code line
     * abreast} wave flies {@code spacing} px apart, centred; a {@code circle} wave's centre descends {@code descend} px/s
     * and drifts toward the ship's x at {@code follow} px/s, and its units never break off (gone once the circle is past
     * the bottom edge).
     */
    public record AirLink(
            double band,
            double damage,
            double cooldownSeconds,
            double tellSeconds,
            double spacing,
            double descend,
            double follow) {
        public AirLink {
            if (!(band > 0)
                    || !(damage >= 0)
                    || !(cooldownSeconds > 0)
                    || !(tellSeconds >= 0)
                    || !(spacing > 0)
                    || !(descend >= 0)
                    || !(follow >= 0)) {
                throw new IllegalArgumentException("an air link has a band, a touch with a cooldown, a tell, a spacing"
                        + " and a drift: " + band + ", " + damage + ", " + cooldownSeconds + ", " + tellSeconds + ", "
                        + spacing + ", " + descend + ", " + follow);
            }
        }
    }

    /**
     * M9 part E, a fixed ground unit's sweep (design/enemies/ground/sentinel-tower, {@code laser-sweep}): its {@link
     * Sweep} is due while the unit is on the play field (the first {@link Sweep#firstDelaySeconds()} after it comes
     * onto it, then one every {@link Sweep#intervalSeconds()}, telegraph to telegraph), centred on the ship's bearing
     * at the telegraph's start in any direction (no clamp: a tower the ship passed sweeps up the screen), sweeping its
     * arc one way and the next the other. Every {@code fullTurnEvery}-th sweep (0: never) is a <b>full turn</b>: a
     * telegraph of {@code fullTurnTelegraphSeconds} (the whole reach ring and the start ray, on the ship's bearing),
     * then the beam turns 360° over {@code fullTurnSeconds}, the full turns' direction alternating; the next telegraph
     * comes {@code fullTurnSlotSeconds} after the full turn's. With {@code firstFullTurn} (a level's placement, Level
     * 32's towers: QE-3 a) the first sweep is a full turn and every {@code fullTurnEvery}-th after it; without, the
     * first full turn is the {@code fullTurnEvery}-th sweep. Each sweep hits the ship once at most. The EMP Burst's stun
     * cuts a running sweep; the next telegraph starts after the stun.
     */
    record GroundSweep(
            int fullTurnEvery,
            double fullTurnSeconds,
            double fullTurnTelegraphSeconds,
            double fullTurnSlotSeconds,
            boolean firstFullTurn) {
        /** No full turns (the easy Sentinel's). */
        public static final GroundSweep ARCS_ONLY = new GroundSweep(0, 1, 1, 2);

        public GroundSweep {
            if (fullTurnEvery < 0
                    || !(fullTurnSeconds > 0)
                    || !(fullTurnTelegraphSeconds > 0)
                    || !(fullTurnSlotSeconds >= fullTurnSeconds + fullTurnTelegraphSeconds)) {
                throw new IllegalArgumentException("a full turn comes every few sweeps, telegraphed, in its slot: "
                        + fullTurnEvery + ", " + fullTurnSeconds + ", " + fullTurnTelegraphSeconds + ", "
                        + fullTurnSlotSeconds);
            }
        }

        /** The full turns counted from the {@code fullTurnEvery}-th sweep (the first an arc). */
        public GroundSweep(
                int fullTurnEvery,
                double fullTurnSeconds,
                double fullTurnTelegraphSeconds,
                double fullTurnSlotSeconds) {
            this(fullTurnEvery, fullTurnSeconds, fullTurnTelegraphSeconds, fullTurnSlotSeconds, false);
        }

        /** This sweep with its first sweep a full turn (no change without full turns). */
        public GroundSweep withFirstFullTurn() {
            return new GroundSweep(fullTurnEvery, fullTurnSeconds, fullTurnTelegraphSeconds, fullTurnSlotSeconds, true);
        }

        /** Whether the {@code n}-th sweep (from 1) is a full turn. */
        public boolean fullTurn(int n) {
            return fullTurnEvery > 0 && (firstFullTurn ? n - 1 : n) % fullTurnEvery == 0;
        }

        /** The {@code n}-th sweep's number among the full turns (from 1), if it is one: their direction alternates. */
        public int fullTurnNumber(int n) {
            return firstFullTurn ? (n - 1) / fullTurnEvery + 1 : n / fullTurnEvery;
        }
    }
}
