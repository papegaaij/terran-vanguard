package vanguard.sim;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, the capsule's hex shield; m11f-gaps.md §1 #4): a boss
 * part's own hex shield under the enemy shield rule ({@link PartShields}), fed by nothing: its pool is {@code shield}'s
 * HP, refilled after its delay without a hit; a {@code shield-breaker} hit empties it and the EMP Burst strips it, as
 * any part shield.
 *
 * @param part the shielded part
 * @param shield its rule (the HP, the refill's delay and share, the breaker's hold)
 */
public record PartShield(int part, EnemySpec.Shield shield) {
    public PartShield {
        java.util.Objects.requireNonNull(shield);
        if (part < 0) {
            throw new IllegalArgumentException("a part shield is on a part: " + part);
        }
    }
}
