package vanguard.sim;

/**
 * M9 part H, step H1b (design/enemies, Enemy shields; design/enemies/bosses/iron-sovereign, the open core's hex
 * shield, round 60 Q1 a): a boss's part shields on A1's {@link EnemyShield} rule. Each shielded part carries a hex pool
 * over its HP: every hit spends it first at its full damage and only the overflow reaches the part (after its weak-spot
 * multiplier); after the shield's delay without a hit it refills; a {@code shield-breaker} hit empties it and holds the
 * refill ({@link #breaking}); the EMP Burst empties every touchable part's shield and holds the refill for its stun
 * ({@link #strip}), never stunning the boss. Kept by its {@link SetPiece}; made only for a boss with a part shield (the
 * Iron Sovereign's core, {@link RingSpec.Core#shield()}), so every other boss hashes as before. Its events are {@code
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
    /** Where its events go (the sortie's); a sink that keeps none until the boss is wired into a sortie. */
    private SimEvents events = new SimEvents(0);

    PartShields(SetPiece piece, BossSpec boss, int parts) {
        this.piece = piece;
        specs = new EnemySpec.Shield[parts];
        shields = new EnemyShield[parts];
        boss.rings().flatMap(RingSpec::core).ifPresent(core -> core.shield().ifPresent(shield -> {
            if (core.part() >= parts) {
                throw new IllegalArgumentException("the core's shield is on one of its parts: " + core.part());
            }
            specs[core.part()] = shield;
            shields[core.part()] = new EnemyShield(true);
        }));
        reset();
    }

    /** Whether {@code boss} has a part shield (then its set piece keeps a {@link PartShields}). */
    static boolean any(BossSpec boss) {
        return boss != null
                && boss.rings()
                        .flatMap(RingSpec::core)
                        .flatMap(RingSpec.Core::shield)
                        .isPresent();
    }

    /** The sortie's events, where its shield events go. */
    void events(SimEvents sortieEvents) {
        events = sortieEvents;
    }

    /** Back to the level start (and the boss checkpoint): every shield full. */
    void reset() {
        for (int p = 0; p < shields.length; p++) {
            if (shields[p] != null) {
                shields[p].start(specs[p].hp());
            }
        }
    }

    /** One step of every shield's refill (its hold, its delay), on the real steps. */
    void step() {
        for (int p = 0; p < shields.length; p++) {
            if (shields[p] != null && piece.partHp(p) > 0) {
                shields[p].step(specs[p], events, piece.partX(p), piece.partY(p), p);
            }
        }
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
