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
            int headFirstBonus) {
        public ChainSpec {
            segmentBoxes = java.util.List.copyOf(segmentBoxes);
            offsets = java.util.List.copyOf(offsets);
            if (offsets.size() != segmentBoxes.size() + 2) {
                throw new IllegalArgumentException("a chain has an offset per member: head, segments, tail");
            }
            if (!(headMultiplier > 0)) {
                throw new IllegalArgumentException("a chain's head multiplier is positive: " + headMultiplier);
            }
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
