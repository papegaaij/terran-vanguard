package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, the bow cannon's gold hex shield; m10h-gaps.md §1 #3): a boss
 * part's hex shield under the enemy shield rule ({@link PartShields}) fed by other parts: its pool is {@code shield}'s
 * HP times the share of its {@code feeders} still alive (the Ascendant's cannon: 240 with both generators, 120 with
 * one, none with neither). A feeder's death caps what is left at once; the refill fills to the cap. A {@code
 * shield-breaker} hit empties it and the EMP Burst strips it, as any part shield.
 *
 * @param part the shielded part
 * @param shield its shield's rule at full feed (the HP, the refill's delay and share, the breaker's hold)
 * @param feeders the parts that feed it
 */
public record FedShield(int part, EnemySpec.Shield shield, List<Integer> feeders) {
    public FedShield {
        java.util.Objects.requireNonNull(shield);
        feeders = List.copyOf(feeders);
        if (part < 0
                || feeders.isEmpty()
                || feeders.contains(part)
                || feeders.stream().anyMatch(f -> f < 0)) {
            throw new IllegalArgumentException("a fed shield is on a part and fed by other parts: " + part);
        }
    }
}
