package vanguard.sim;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * M8 part A (design/player/specials, Setting restrictions and the greyed state): a level's and its sections' lists of
 * the specials they forbid, built by {@code vanguard.content.SimSpecs} from the level data's {@code special_limits}.
 * Where no list applies, the fitted special's own {@link SpecialSpec.Setting} decides by the medium: under water the
 * Airstrike and the Orbital Lance read {@code UNDER ICE}. A list replaces that rule where it applies (a listed special
 * reads its setting's reason word, an unlisted one works), but the Sonar Pulse reads {@code NO WATER} wherever there
 * is no water, listed or not (L22's ice: nothing forbidden, the pulse still idle). A forbidden special is greyed with
 * its reason word; a press is refused (no charge spent).
 *
 * @param switches from each switch's time on the level clock, in time order: the list that applies until the next
 */
public record SpecialLimits(List<Switch> switches) {
    /** No lists: every special follows its own setting (every level before Act 4, and a level without a list). */
    public static final SpecialLimits NONE = new SpecialLimits(List.of());

    public SpecialLimits {
        switches = List.copyOf(switches);
        for (int i = 1; i < switches.size(); i++) {
            if (!(switches.get(i).atSeconds() > switches.get(i - 1).atSeconds())) {
                throw new IllegalArgumentException("the special limits' switches are in time order: " + switches);
            }
        }
    }

    /** A level's own list from its start to its end. */
    public static SpecialLimits level(Set<SpecialSpec.Kind> forbidden) {
        return new SpecialLimits(List.of(new Switch(0, Optional.of(forbidden))));
    }

    /**
     * From {@code atSeconds} on the level clock: {@code forbidden} lists the specials the level (or its section)
     * forbids, replacing the specials' own settings; empty: the settings decide again. M9 part A: {@code reason} is
     * the word a special it forbids reads (an underground section's {@link Reason#IN_ROCK}); {@link Reason#NONE}: the
     * special's own setting's word.
     */
    public record Switch(double atSeconds, Optional<Set<SpecialSpec.Kind>> forbidden, Reason reason) {
        public Switch {
            if (!(atSeconds >= 0)) {
                throw new IllegalArgumentException("a special limit switch is at 0 s or later: " + atSeconds);
            }
            forbidden = forbidden.map(Set::copyOf);
            java.util.Objects.requireNonNull(reason);
        }

        /** A switch whose forbidden specials read their own settings' words (every switch of Act 4). */
        public Switch(double atSeconds, Optional<Set<SpecialSpec.Kind>> forbidden) {
            this(atSeconds, forbidden, Reason.NONE);
        }
    }

    /** Why a special is greyed: the HUD's reason word where the charge count stands. */
    public enum Reason {
        /** Not limited: it works here. */
        NONE(""),
        /** Under the ice, in water: no bomber or satellite cover. */
        UNDER_ICE("UNDER ICE"),
        /** No water to ping. */
        NO_WATER("NO WATER"),
        /**
         * M9 part A (design/player/specials, Act 5 specials; Level 32): inside the asteroid, no bomber or satellite
         * reaches the ship. Appended last so the earlier reasons keep their ordinals (the limit event's value).
         */
        IN_ROCK("IN ROCK");

        private final String word;

        Reason(String word) {
            this.word = word;
        }

        /** The HUD's reason word; empty for {@link #NONE}. */
        public String word() {
            return word;
        }
    }
}
