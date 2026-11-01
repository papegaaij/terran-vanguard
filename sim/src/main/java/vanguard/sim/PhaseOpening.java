package vanguard.sim;

import java.util.Optional;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The eruption; m10h-gaps.md §1 #7): how a boss's phase opens.
 * For its first {@code untouchableSeconds} (the Ascendant's eruption: the 1 s shudder and 3 s erupting) every part of
 * the boss takes no damage (a shot sparks) and its phase playbook waits; as it starts it drops {@code drop} (an armour
 * patch) at ({@code dropDx}, {@code dropDy}) from the boss's centre. The phase's own name is its {@code boss-phase}
 * radio subject ({@code eruption}), as for every phase.
 *
 * @param phase the phase it opens (after the first)
 * @param untouchableSeconds how long nothing hurts it from the phase's start, s (0: hurt at once)
 * @param drop the pickup dropped at the phase's start; empty for none
 * @param dropDx the drop's place right of the centre, px
 * @param dropDy the drop's place above the centre, px (negative: below)
 */
public record PhaseOpening(
        int phase, double untouchableSeconds, Optional<PickupType> drop, double dropDx, double dropDy) {
    public PhaseOpening {
        java.util.Objects.requireNonNull(drop);
        if (phase < 1 || !(untouchableSeconds >= 0) || !Double.isFinite(dropDx) || !Double.isFinite(dropDy)) {
            throw new IllegalArgumentException("a phase opening opens a later phase, untouchable a while: " + phase);
        }
    }
}
