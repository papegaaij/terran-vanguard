package vanguard.sim;

/**
 * The parts of an {@link EnemySpec} stat block that describe a segment chain's and a gate's: the chain, its head's crawl and pore wave, a gate anchor's link. {@link EnemySpec}
 * implements this interface only to inherit them as its member types, so they keep their names
 * ({@code EnemySpec.ChainSpec} ...).
 */
public interface ChainSpecs {
    /**
     * A segment chain (design/enemies/air/coilwyrm): the unit is its head, flying the wave's path;
     * behind it {@code segmentBoxes.size()} segments of {@code segment} (each with its own hit box)
     * and the {@code tail} follow the head's path history, member {@code i} at {@code offsets[i]} px
     * of history behind the head (the head's is 0). A segment's destruction cuts the chain: the
     * rear part grows a {@code regrown} head over {@code regrowSeconds} (once per chain, and not
     * when the rear part died meanwhile) and lunges at the ship at {@code regrowSpeed}; a second cut
     * kills the severed part from the cut backwards, one member every {@code popSeconds}, as the
     * head's death does with the whole chain. The tail pays {@code tailFirstBonus} more when it is
     * destroyed before any other part. The head is a weak point: every damage it takes is
     * multiplied by {@code headMultiplier} (its part's {@code multiplier}, like a set piece's part);
     * a regrown head's is 1. M7 part C: the head pays {@code headFirstBonus} more when it is destroyed
     * before any other part (design/enemies/ground/threadcrawler: a head-first kill pays the whole); a
     * ground chain ({@link Crawl}) never regrows, and its {@code regrown} may be null.
     *
     * <p>M8 part C (design/enemies/naval/eel-swarm): with a {@code scatter} an air chain never regrows: every cut
     * splits off its rear part, which swims on along the cut's heading as a headless trail ({@link Scatter}), and its
     * {@code regrown} is null. Its {@code headAttacks} are its head's attacks, each on its own clock while the head is
     * on the play field (the head's own gun is then empty): each fires its volley at its interval, the first its
     * {@link EnemyGun#firstShotDelay()} after the head entered the screen, and raises its tell ({@link
     * SimEvents.Type#EYE_GLINT}) its {@link EnemyGun#tellSeconds()} before (none for 0). An empty list for none.
     */
    public record ChainSpec(
            EnemySpec segment,
            java.util.List<Hitbox> segmentBoxes,
            EnemySpec tail,
            int tailFirstBonus,
            EnemySpec regrown,
            double regrowSeconds,
            double regrowSpeed,
            java.util.List<Double> offsets,
            double popSeconds,
            double headMultiplier,
            int headFirstBonus,
            java.util.Optional<Scatter> scatter,
            java.util.List<EnemyGun> headAttacks,
            java.util.Optional<Cars> cars) {
        /** M8 part C: the most attacks a chain's head has. */
        public static final int MOST_HEAD_ATTACKS = 4;

        public ChainSpec {
            segmentBoxes = java.util.List.copyOf(segmentBoxes);
            offsets = java.util.List.copyOf(offsets);
            headAttacks = java.util.List.copyOf(headAttacks);
            java.util.Objects.requireNonNull(scatter);
            java.util.Objects.requireNonNull(cars);
            if (cars.isPresent() && (segmentBoxes.size() < cars.get().every() || regrown != null)) {
                throw new IllegalArgumentException(
                        "a train has a turret car among its cars and never regrows (M9 part C): "
                                + segmentBoxes.size());
            }
            if (offsets.size() != segmentBoxes.size() + 2) {
                throw new IllegalArgumentException("a chain has an offset per member: head, segments, tail");
            }
            if (offsets.size() > Chain.MAX_MEMBERS) {
                throw new IllegalArgumentException(
                        "a chain has at most " + Chain.MAX_MEMBERS + " members: " + offsets.size());
            }
            if (!(headMultiplier > 0)) {
                throw new IllegalArgumentException("a chain's head multiplier is positive: " + headMultiplier);
            }
            if (scatter.isPresent() && regrown != null) {
                throw new IllegalArgumentException("a chain whose cut part scatters regrows no head");
            }
            if (headAttacks.size() > MOST_HEAD_ATTACKS) {
                throw new IllegalArgumentException(
                        "a chain's head has at most " + MOST_HEAD_ATTACKS + " attacks: " + headAttacks.size());
            }
            for (EnemyGun gun : headAttacks) {
                if (gun.up()
                        || gun.burst() != 1
                        || gun.mine().isPresent()
                        || gun.mortar().isPresent()
                        || gun.spiral().isPresent()
                        || gun.homing().isPresent()
                        || !Double.isNaN(gun.passWithinX())
                        || Double.isFinite(gun.turnRate())
                        || !Double.isFinite(gun.intervalSeconds())) {
                    throw new IllegalArgumentException(
                            "a chain head's attack is an aimed shot or a fan at an interval: " + gun);
                }
            }
        }

        /** Without M8 part C's scatter and head attacks. */
        public ChainSpec(
                EnemySpec segment,
                java.util.List<Hitbox> segmentBoxes,
                EnemySpec tail,
                int tailFirstBonus,
                EnemySpec regrown,
                double regrowSeconds,
                double regrowSpeed,
                java.util.List<Double> offsets,
                double popSeconds,
                double headMultiplier,
                int headFirstBonus) {
            this(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    headFirstBonus,
                    java.util.Optional.empty(),
                    java.util.List.of(),
                    java.util.Optional.empty());
        }

        /** Without M9 part C's turret cars. */
        public ChainSpec(
                EnemySpec segment,
                java.util.List<Hitbox> segmentBoxes,
                EnemySpec tail,
                int tailFirstBonus,
                EnemySpec regrown,
                double regrowSeconds,
                double regrowSpeed,
                java.util.List<Double> offsets,
                double popSeconds,
                double headMultiplier,
                int headFirstBonus,
                java.util.Optional<Scatter> scatter,
                java.util.List<EnemyGun> headAttacks) {
            this(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    headFirstBonus,
                    scatter,
                    headAttacks,
                    java.util.Optional.empty());
        }

        /** M8 part C: this chain whose cut parts scatter ({@code regrown} must be null); empty for none. */
        public ChainSpec withScatter(java.util.Optional<Scatter> withScatter) {
            return new ChainSpec(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    headFirstBonus,
                    withScatter,
                    headAttacks,
                    cars);
        }

        /** M8 part C: this chain with its head's attacks; an empty list for none. */
        public ChainSpec withHeadAttacks(java.util.List<EnemyGun> withAttacks) {
            return new ChainSpec(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    headFirstBonus,
                    scatter,
                    withAttacks,
                    cars);
        }

        /** M9 part C: this train with its turret cars (design/enemies/air/rail-serpent); empty for none. */
        public ChainSpec withCars(java.util.Optional<Cars> withCars) {
            return new ChainSpec(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    headFirstBonus,
                    scatter,
                    headAttacks,
                    withCars);
        }

        /**
         * M9 part C: the stat block of member {@code i} (0 the head's own is the unit's, not this): the tail, a turret
         * car or a plain segment.
         */
        public EnemySpec memberSpec(int i) {
            if (i == members() - 1) {
                return tail;
            }
            return cars.isPresent() && i % cars.get().every() == 0 ? cars.get().turret() : segment;
        }

        /** M9 part C: the turret cars before member {@code i} (its place among them, for the stagger). */
        public int turretsBefore(int i) {
            if (cars.isEmpty()) {
                return 0;
            }
            int n = 0;
            for (int k = 1; k < i && k < members() - 1; k++) {
                n += k % cars.get().every() == 0 ? 1 : 0;
            }
            return n;
        }

        /** Without M7 part C's head-first bonus. */
        public ChainSpec(
                EnemySpec segment,
                java.util.List<Hitbox> segmentBoxes,
                EnemySpec tail,
                int tailFirstBonus,
                EnemySpec regrown,
                double regrowSeconds,
                double regrowSpeed,
                java.util.List<Double> offsets,
                double popSeconds,
                double headMultiplier) {
            this(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    headMultiplier,
                    0);
        }

        /** A chain whose head takes plain damage (multiplier 1). */
        public ChainSpec(
                EnemySpec segment,
                java.util.List<Hitbox> segmentBoxes,
                EnemySpec tail,
                int tailFirstBonus,
                EnemySpec regrown,
                double regrowSeconds,
                double regrowSpeed,
                java.util.List<Double> offsets,
                double popSeconds) {
            this(
                    segment,
                    segmentBoxes,
                    tail,
                    tailFirstBonus,
                    regrown,
                    regrowSeconds,
                    regrowSpeed,
                    offsets,
                    popSeconds,
                    1);
        }

        /** The members: head, segments and tail. */
        public int members() {
            return offsets.size();
        }
    }

    /**
     * M8 part C, a water chain's cut (design/enemies/naval/eel-swarm, Cut and scatter; the stat block's {@code cut:
     * {scatter, seconds}}): the rear part a cut splits off keeps the heading the chain had at the cut and swims on
     * straight at {@code speed} px/s for {@code seconds}, its members hittable and firing nothing, then dies from the
     * cut backwards (one member every {@link ChainSpec#popSeconds()}), drifting to a halt as a dead chain does. A cut
     * of a scattering part only takes that member out of it.
     *
     * <p>M9 part C (design/enemies/air/rail-serpent, the machine split): a {@code wreck} instead loses power at once: it
     * drifts on the cut's heading from {@code speed} px/s slowing evenly to a stop over {@code seconds}, its cars
     * doomed (nothing hits them, they touch nothing, their turrets dark), then blows from the cut backwards one car
     * every {@link ChainSpec#popSeconds()}, each paying its bounty as it blows ({@link SimEvents.Type#CHAIN_WRECK} at
     * the cut).
     */
    public record Scatter(double speed, double seconds, boolean wreck) {
        public Scatter {
            if (!(speed >= 0) || !(seconds >= 0)) {
                throw new IllegalArgumentException("a scatter has a speed and a time: " + speed + ", " + seconds);
            }
        }

        /** A water chain's swim (M8 part C), no wreck. */
        public Scatter(double speed, double seconds) {
            this(speed, seconds, false);
        }
    }

    /**
     * M9 part C (design/enemies/air/rail-serpent): a train's turret cars: every {@code every}-th car behind the engine
     * (member every, 2 × every, ...; never the tail) flies {@code turret}'s stat block, its gun firing on its own clock
     * while the car is on the play field, the k-th turret car's first shot {@code staggerSeconds} × k later than its
     * gun's first-shot delay (the cars staggered); a turret's {@link EnemyGun#tellSeconds()} lights its lamp first
     * ({@link SimEvents.Type#CAR_TELL}); a burst fires its shots its burst gap apart. A car doomed in a split or the
     * engine's death is dark.
     */
    public record Cars(EnemySpec turret, int every, double staggerSeconds) {
        public Cars {
            java.util.Objects.requireNonNull(turret);
            if (every < 1 || !(staggerSeconds >= 0) || turret.gun().isEmpty()) {
                throw new IllegalArgumentException("a train's turret cars come every few cars, staggered, with a gun");
            }
            EnemyGun gun = turret.gun().get();
            if (gun.up()
                    || gun.mine().isPresent()
                    || gun.mortar().isPresent()
                    || gun.spiral().isPresent()
                    || gun.homing().isPresent()
                    || !Double.isNaN(gun.passWithinX())
                    || !Double.isFinite(gun.intervalSeconds())) {
                throw new IllegalArgumentException("a turret car's gun is an aimed shot, a burst or a fan: " + gun);
            }
        }
    }

    /**
     * M7 part C, the {@code crawl} movement of a ground chain's head (design/enemies/ground/threadcrawler):
     * the head crawls the ground path through the points ({@code xs[i]}, row {@code scrolls[i]}: the ground
     * scroll at which that row reaches the top edge, as a canyon wall's points; linear between them) at
     * {@code speed} px/s on top of the scroll, from the wave's time on, and straight on along its last leg
     * past the end; the members follow on its history in ground space and scroll with the ground. A cut
     * (a destroyed segment) stops the members behind it where they are; they die {@code cutSeconds} later,
     * from the cut backwards, one every {@link ChainSpec#popSeconds()}; it never regrows. Its segments fire
     * the {@code pores} wave.
     */
    public record Crawl(double speed, double[] scrolls, double[] xs, PoreWave pores, double cutSeconds) {
        public Crawl {
            scrolls = scrolls.clone();
            xs = xs.clone();
            java.util.Objects.requireNonNull(pores);
            if (scrolls.length < 2 || scrolls.length != xs.length) {
                throw new IllegalArgumentException("a crawl path has at least two points, each a row and an x");
            }
            for (int i = 1; i < scrolls.length; i++) {
                if (scrolls[i] == scrolls[i - 1] && xs[i] == xs[i - 1]) {
                    throw new IllegalArgumentException("a crawl path has no repeated point");
                }
            }
            if (!(speed > 0) || !(cutSeconds >= 0)) {
                throw new IllegalArgumentException("a crawl has a speed and a cut delay: " + speed + ", " + cutSeconds);
            }
        }

        /** A crawl whose cut part dies 1.0 s after the cut (the stat block's {@code cut.delay}). */
        public Crawl(double speed, double[] scrolls, double[] xs, PoreWave pores) {
            this(speed, scrolls, xs, pores, 1.0);
        }

        @Override
        public double[] scrolls() {
            return scrolls.clone();
        }

        @Override
        public double[] xs() {
            return xs.clone();
        }

        /** The path's points, without a copy (the chain reads them every step). */
        int points() {
            return xs.length;
        }

        double row(int i) {
            return scrolls[i];
        }

        double x(int i) {
            return xs[i];
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Crawl crawl
                    && speed == crawl.speed
                    && cutSeconds == crawl.cutSeconds
                    && pores.equals(crawl.pores)
                    && java.util.Arrays.equals(scrolls, crawl.scrolls)
                    && java.util.Arrays.equals(xs, crawl.xs);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    speed, cutSeconds, pores, java.util.Arrays.hashCode(scrolls), java.util.Arrays.hashCode(xs));
        }

        @Override
        public String toString() {
            return "Crawl[" + speed + ", " + java.util.Arrays.toString(scrolls) + ", " + java.util.Arrays.toString(xs)
                    + ", " + pores + ", " + cutSeconds + "]";
        }
    }

    /**
     * M7 part C, a ground chain's pore wave (design/enemies/ground/threadcrawler, Travelling wave): the
     * segments' pores fire one slow aimed spore each in turn, head to tail, {@code interval} s apart (every
     * other segment's only when {@code everyOther}: easy), each aimed at the ship as it fires, at {@code
     * bulletSpeed} px/s dealing {@code damage}; a pore glows for {@code glow} s before it fires (the tell),
     * and the next wave's first glow starts {@code rest} s after a wave's last pore fired. The first wave's
     * first glow starts as the chain enters the play field (default pending the user's review); a pore off
     * the play field, of a doomed or destroyed segment, or of a stunned chain does not fire (its turn
     * passes).
     */
    public record PoreWave(
            double interval, double rest, double glow, double bulletSpeed, double damage, boolean everyOther) {
        public PoreWave {
            if (!(interval > 0) || !(rest >= 0) || !(glow >= 0) || !(bulletSpeed > 0) || !(damage >= 0)) {
                throw new IllegalArgumentException("a pore wave has an interval, a rest, a glow, a speed and a damage");
            }
        }
    }

    /**
     * M7 part C, a gate's tendril (design/enemies/ground/tendril-anchor, the {@code link} attack; M9 = a): the
     * line between the centres of the two anchors of a {@link GroundSpec.Gate}. It is dormant (harmless, a
     * flickering thread) for {@code dormantSeconds} after the pair enters at the top edge, then its touch
     * deals {@code contactDamage} to the ship (shield first) or Rook, with {@code cooldownSeconds} of its
     * own cooldown per craft (the ship is not pushed), for a hull within {@code band}/2 px of the line.
     * Either anchor's death breaks it; a {@code beam} damages it ({@code tendrilHp}, the beam going on past
     * it), and burning it through pays {@code burntBounty} (Act 1 terms). Every other shot, bomb, enemy and
     * bullet passes through it.
     */
    public record Link(
            double tendrilHp,
            double dormantSeconds,
            double contactDamage,
            double cooldownSeconds,
            double band,
            int burntBounty) {
        public Link {
            if (!(tendrilHp > 0)
                    || !(dormantSeconds >= 0)
                    || !(contactDamage >= 0)
                    || !(cooldownSeconds >= 0)
                    || !(band > 0)
                    || burntBounty < 0) {
                throw new IllegalArgumentException(
                        "a tendril has HP, a dormancy, a touch, a cooldown, a band and a bounty");
            }
        }
    }
}
