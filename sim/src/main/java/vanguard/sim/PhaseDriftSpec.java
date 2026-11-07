package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, phase 2: the drift; design/enemies, Distorted space;
 * m11h-gaps.md §1 #4): distorted space inside one boss phase while the scroll is halted ({@link PhaseDrift}). From the
 * phase's start the angle follows {@code profile} on the phase's own clock (smoothstep between the points, as a level's
 * current), and the sideways drift is the nominal arena drift {@code speed} (QH-2, round 95: a 60 px/s, pending the
 * user's review) times the angle's sine; at most one {@link Reversal} flips it; after the phase it eases out over
 * {@code easeOutSeconds}.
 *
 * @param phase the boss phase it drifts in (0 the first)
 * @param speed the halted arena's nominal drift speed, px/s (the drift is this times sin θ)
 * @param profile the angles on the phase's clock (s from the phase's start; degrees off straight down, positive: the
 *     drift to the right)
 * @param reversal the phase's one reversal; empty for none (easy)
 * @param easeOutSeconds the ease to no drift after the phase, s
 */
public record PhaseDriftSpec(
        int phase, double speed, List<HazardSpec.Bend> profile, Optional<Reversal> reversal, double easeOutSeconds) {
    public PhaseDriftSpec {
        profile = List.copyOf(profile);
        java.util.Objects.requireNonNull(reversal);
        if (phase < 0 || !(speed > 0) || profile.isEmpty() || !(easeOutSeconds > 0)) {
            throw new IllegalArgumentException("a phase drift drifts in a phase, at a speed, on a profile, easing out");
        }
        for (int i = 1; i < profile.size(); i++) {
            if (!(profile.get(i).t() > profile.get(i - 1).t())) {
                throw new IllegalArgumentException("a phase drift's profile runs forward in time: " + profile);
            }
        }
    }

    /** This drift without its reversal (easy), or with another. */
    public PhaseDriftSpec withReversal(Optional<Reversal> other) {
        return new PhaseDriftSpec(phase, speed, profile, other, easeOutSeconds);
    }

    /** This drift at another nominal {@code arenaSpeed} (QH-2's lever), px/s. */
    public PhaseDriftSpec withSpeed(double arenaSpeed) {
        return new PhaseDriftSpec(phase, arenaSpeed, profile, reversal, easeOutSeconds);
    }

    /**
     * The phase's reversal: its tell starts at the death of the {@code atKills}-th of the phase's parts (its {@code
     * until} parts: the Heart's second node) or {@code atSeconds − toldSeconds} into the phase, whichever comes first, and
     * runs {@code toldSeconds} later: the drift eases through zero over 1 s (as a level's reversal), runs reversed {@code
     * runSeconds} and eases back over 1 s.
     *
     * @param atKills the count of the phase's dead parts that tells it (0: by the time only)
     * @param atSeconds when it runs at the latest, s from the phase's start
     * @param runSeconds how long it runs reversed, s
     * @param toldSeconds how long it is told ahead, s
     */
    public record Reversal(int atKills, double atSeconds, double runSeconds, double toldSeconds) {
        public Reversal {
            if (atKills < 0 || !(atSeconds >= toldSeconds) || !(runSeconds > 0) || !(toldSeconds >= 0)) {
                throw new IllegalArgumentException("a phase reversal comes on its kills or its time, told, and runs");
            }
        }
    }
}
