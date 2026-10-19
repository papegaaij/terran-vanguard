package vanguard.sim;

/**
 * M7 part A, the Shield Overcharge's numbers (design/player/specials, Shield Overcharge (L19)): the
 * shield fills to {@code factor} times its capacity at once and its capacity stays raised for
 * {@code seconds}; then the excess bleeds away over {@code bleedSeconds}.
 */
public record OverchargeSpec(double factor, double seconds, double bleedSeconds) implements SpecialSpec.Effect {
    public OverchargeSpec {
        if (!(factor > 1) || !(seconds > 0) || bleedSeconds < 0) {
            throw new IllegalArgumentException("an overcharge raises the capacity (factor above 1) for a time");
        }
    }
}
