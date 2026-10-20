package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The level's {@code threat_profile} (the hangar intel panel) and its mission {@code briefing}.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.ThreatProfile}).
 */
public sealed interface BriefingBlock permits LevelData {
    /**
     * The level's threat profile (design/campaign, level template; design/player/systems, Sensor
     * levels and hangar intel); the attack directions, enemy types, waves and secrets are derived
     * from the script.
     *
     * @param layers the dominant gameplay layers
     * @param density 1–5
     * @param traits the recommended weapon traits
     * @param boss the boss or mid-boss, {@code none} without one
     * @param specials the special availability, where the level limits it ("No air support under the ice")
     * @param objective the OBJECTIVE field shown from sensor L1 ("ESCORT 5 CRAWLERS"); none without
     * @param varga Dr. Varga's intel line for each sensor level, all four: {@code none}, {@code l1},
     *     {@code l2}, {@code l3}
     * @param required M5 part C (user decision D7 = a): the traits among {@code traits} the primary
     *     objective cannot be met without (Level 09's {@code anti-ground}): a launch warning at every
     *     sensor level
     */
    public record ThreatProfile(
            String setting,
            List<String> layers,
            int density,
            List<String> traits,
            List<String> hazards,
            String boss,
            Optional<String> specials,
            Optional<String> objective,
            Map<String, String> varga,
            Optional<List<String>> required) {
        /** The keys of Varga's lines, from no sensor suite to L3. */
        public static final List<String> SENSOR_KEYS = List.of("none", "l1", "l2", "l3");

        public ThreatProfile {
            Check.that(density >= 1 && density <= 5, "density: 1–5, was " + density);
            Check.that(
                    varga.size() == SENSOR_KEYS.size() && SENSOR_KEYS.containsAll(varga.keySet()),
                    "varga: a line for each of " + SENSOR_KEYS + ", found " + varga.keySet());
            layers = List.copyOf(layers);
            // M7 part B: every name is a layer (Level 15's `buried`, the Burrowers under the sand).
            layers.forEach(Layers::of);
            traits = List.copyOf(traits);
            hazards = List.copyOf(hazards);
            varga = Map.copyOf(varga);
            required = required.map(List::copyOf);
            List<String> recommended = traits;
            required.ifPresent(names -> {
                Check.notEmpty("required", names);
                for (String name : names) {
                    Check.that(
                            recommended.contains(name),
                            "required: '" + name + "' is not one of the traits " + recommended);
                }
            });
        }

        /** Without M5 part C's required traits. */
        public ThreatProfile(
                String setting,
                List<String> layers,
                int density,
                List<String> traits,
                List<String> hazards,
                String boss,
                Optional<String> specials,
                Optional<String> objective,
                Map<String, String> varga) {
            this(setting, layers, density, traits, hazards, boss, specials, objective, varga, Optional.empty());
        }

        /** The traits the primary objective cannot be met without; empty for none. */
        public List<String> requiredTraits() {
            return required.orElse(List.of());
        }

        /** Varga's line for a sensor level, 0 (no sensor suite) to 3. */
        public String vargaLine(int sensor) {
            return varga.get(SENSOR_KEYS.get(sensor));
        }
    }

    /**
     * The mission briefing (design/ui/briefing): its pages, and the hangar teaser shown with the
     * level's intel on the hangar visit before it.
     */
    public record Briefing(List<BriefingPage> pages, BriefingPage teaser) {
        public Briefing {
            Check.notEmpty("pages", pages);
        }
    }
}
