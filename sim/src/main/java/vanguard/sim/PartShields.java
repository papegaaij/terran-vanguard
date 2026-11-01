package vanguard.sim;

/**
 * M9 part H, step H1b (design/enemies, Enemy shields; design/enemies/bosses/iron-sovereign, the open core's hex
 * shield, round 60 Q1 a): a boss's part shields on A1's {@link EnemyShield} rule. Each shielded part carries a hex pool
 * over its HP: every hit spends it first at its full damage and only the overflow reaches the part (after its weak-spot
 * multiplier); after the shield's delay without a hit it refills; a {@code shield-breaker} hit empties it and holds the
 * refill ({@link #breaking}); the EMP Burst empties every touchable part's shield and holds the refill for its stun
 * ({@link #strip}), never stunning the boss. Kept by its {@link SetPiece}; made only for a boss with a part shield (the
 * Iron Sovereign's core, {@link RingSpec.Core#shield()}; M10 part F: a squadron's aces, {@link SquadronSpec#shield()};
 * M10 part H, step H1a: a part fed by others, {@link FedShield}: its pool shrinks with each feeder's death), so every
 * other boss hashes as before. Its events are {@code
 * PART_SHIELD_*} with the part as the value. Allocates nothing once made.
 */
public final class PartShields {
    /** The fewest steps between two {@link SimEvents.Type#PART_SHIELD_HIT} events (a beam's step-by-step damage). */
    private static final int EVENT_GAP = 6;

    private final SetPiece piece;
    /** Per part, its shield's rule; null for a part without one. */
    private final EnemySpec.Shield[] specs;
    /** Per part, its shield; null for a part without one. */
    private final EnemyShield[] shields;
    /** M10 part H, step H1a: per part, the parts feeding its shield ({@link FedShield}); null for an unfed one. */
    private final int[][] feeders;
    /** Where its events go (the sortie's); a sink that keeps none until the boss is wired into a sortie. */
    private SimEvents events = new SimEvents(0);

    PartShields(SetPiece piece, BossSpec boss, int parts) {
        this.piece = piece;
        specs = new EnemySpec.Shield[parts];
        shields = new EnemyShield[parts];
        feeders = new int[parts][];
        for (Source source : sources(boss)) {
            int p = source.part();
            if (p >= parts) {
                throw new IllegalArgumentException("a part shield is on one of its parts: " + p);
            }
            specs[p] = source.shield();
            shields[p] = new EnemyShield(true);
            if (!source.feeders().isEmpty()) {
                feeders[p] =
                        source.feeders().stream().mapToInt(Integer::intValue).toArray();
            }
            // Full: every feeder lives at the start (the parts are made after this; reset() reads them).
            shields[p].start(source.shield().hp());
        }
    }

    /**
     * M10 part H, step H1a: a part shield's source: its part, its rule, the parts feeding it (empty: a pool of its own).
     */
    private record Source(int part, EnemySpec.Shield shield, java.util.List<Integer> feeders) {}

    /**
     * M10 part H, step H1a: {@code boss}'s part shields, from each of its sources in turn: the ringed core's ({@link
     * RingSpec.Core#shield()}), the squadron's aces' ({@link SquadronSpec#shield()}) and the fed shields ({@link
     * BossSpec#fedShields()}).
     */
    private static java.util.List<Source> sources(BossSpec boss) {
        java.util.List<Source> sources = new java.util.ArrayList<>();
        boss.rings().flatMap(RingSpec::core).ifPresent(core -> core.shield()
                .ifPresent(shield -> sources.add(new Source(core.part(), shield, java.util.List.of()))));
        boss.squadron().ifPresent(squadron -> {
            // M10 part F: each ace carries its own hex shield.
            for (SquadronSpec.Ace ace : squadron.aces()) {
                sources.add(new Source(ace.part(), squadron.shield(), java.util.List.of()));
            }
        });
        for (FedShield fed : boss.fedShields()) {
            sources.add(new Source(fed.part(), fed.shield(), fed.feeders()));
        }
        return sources;
    }

    /** Whether {@code boss} has a part shield (then its set piece keeps a {@link PartShields}). */
    static boolean any(BossSpec boss) {
        return boss != null && !sources(boss).isEmpty();
    }

    /** The sortie's events, where its shield events go. */
    void events(SimEvents sortieEvents) {
        events = sortieEvents;
    }

    /** Back to the level start (and the boss checkpoint): every shield full. */
    void reset() {
        for (int p = 0; p < shields.length; p++) {
            if (shields[p] != null) {
                shields[p].start(fed(p));
            }
        }
    }

    /** One step of every shield's refill (its hold, its delay), on the real steps; a fed one capped by its feeders. */
    void step() {
        for (int p = 0; p < shields.length; p++) {
            if (shields[p] != null && piece.partHp(p) > 0) {
                if (feeders[p] != null && fed(p) != shields[p].max()) {
                    // M10 part H, step H1a: a feeder died: the pool shrinks at once.
                    shields[p].cap(fed(p), events, piece.partX(p), piece.partY(p), p);
                }
                shields[p].step(specs[p], events, piece.partX(p), piece.partY(p), p);
            }
        }
    }

    /** Part {@code p}'s pool now: its rule's HP, times the share of its feeders alive for a fed shield. */
    private double fed(int p) {
        if (feeders[p] == null) {
            return specs[p].hp();
        }
        int alive = 0;
        for (int f : feeders[p]) {
            alive += piece.partHp(f) > 0 ? 1 : 0;
        }
        return specs[p].hp() * alive / feeders[p].length;
    }

    /** Part {@code p}'s shield spends itself on {@code amount}; returns what is left for the part. */
    double spend(int p, double amount) {
        if (shields[p] == null || !(piece.partHp(p) > 0)) {
            return amount;
        }
        return shields[p].spend(amount, events, piece.partX(p), piece.partY(p), p, EVENT_GAP);
    }

    /** A {@code shield-breaker} hit on part {@code p}: its shield emptied, its refill held for the breaker's hold. */
    void breaking(int p) {
        if (shields[p] != null) {
            shields[p].empty(
                    specs[p], SimStep.ticks(specs[p].breakerHoldSeconds()), events, piece.partX(p), piece.partY(p), p);
        }
    }

    /**
     * The EMP Burst: every living, touchable part's shield emptied and its refill held {@code ticks} steps (a boss is
     * never stunned). Returns how many held anything.
     */
    int strip(int ticks) {
        int stripped = 0;
        for (int p = 0; p < shields.length; p++) {
            if (shields[p] != null
                    && piece.partHp(p) > 0
                    && !piece.partShielded(p)
                    && shields[p].empty(specs[p], ticks, events, piece.partX(p), piece.partY(p), p)) {
                stripped++;
            }
        }
        return stripped;
    }

    /** Whether part {@code p} carries a shield. */
    public boolean fitted(int p) {
        return shields[p] != null;
    }

    /** Part {@code p}'s shield left; 0 without one or while it is down. */
    public double shield(int p) {
        return shields[p] == null ? 0 : shields[p].shield();
    }

    /** Part {@code p}'s shield's maximum; 0 without one. */
    public double max(int p) {
        return shields[p] == null ? 0 : shields[p].max();
    }

    /** Part {@code p}'s shield's share left, 0 to 1 (the look: the hexes at 40 % while it holds). */
    public double share(int p) {
        return shields[p] == null ? 0 : shields[p].share();
    }

    /** Whether part {@code p}'s refill is held back now (a breaker's hit, the EMP Burst). */
    public boolean held(int p) {
        return shields[p] != null && shields[p].held();
    }

    void addTo(StateHash hash) {
        for (EnemyShield shield : shields) {
            if (shield != null) {
                shield.addTo(hash);
            }
        }
    }
}
