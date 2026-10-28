package vanguard.content;

import java.util.List;

/**
 * design/player/generator/data.yaml.
 *
 * @param drain M9 part A: the drain's numbers (design/player/generator, Enemy drain effects)
 */
public record GeneratorData(SparePower sparePower, Drain drain, List<Model> models) {
    public GeneratorData {
        Check.notEmpty("models", models);
    }

    /** Spare MW boost the shield regen by {@code regenBonusPerMw} each, up to {@code maxRegenBonus}. */
    public record SparePower(double regenBonusPerMw, double maxRegenBonus) {
        public SparePower {
            Check.notNegative("regen_bonus_per_mw", regenBonusPerMw);
            Check.notNegative("max_regen_bonus", maxRegenBonus);
        }
    }

    /**
     * M9 part A (design/player/generator, Enemy drain effects): each latched drainer lowers the effective output by
     * {@code per} MW, all of them together by at most {@code max} MW; the weapons' fire-rate factor never falls below
     * {@code floor}.
     */
    public record Drain(double per, double max, double floor) {
        public Drain {
            Check.positive("drain.per", per);
            Check.that(max >= per, "drain.max is at least drain.per");
            Check.that(floor > 0 && floor <= 1, "drain.floor is in (0, 1]");
        }
    }

    /** One generator; {@code output} in MW. */
    public record Model(String name, double output, int price, String available) {
        public Model {
            Check.positive("output", output);
            Check.notNegative("price", price);
        }
    }
}
