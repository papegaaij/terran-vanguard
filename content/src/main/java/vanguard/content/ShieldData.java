package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * design/player/shields/data.yaml.
 *
 * @param breakSeconds how long a broken shield stays down before the regen delay starts
 */
public record ShieldData(double breakSeconds, List<Model> models) {
    public ShieldData {
        Check.notNegative("break_seconds", breakSeconds);
        Check.notEmpty("models", models);
    }

    /**
     * One shield; {@code regen} in points per second, {@code delay} in seconds, {@code draw} in MW. M10 part A: a variant
     * shield's {@code reflect} (the Reflex Shield's, {@link Reflect}); none for a Mk shield.
     */
    public record Model(
            String name,
            double capacity,
            double regen,
            double delay,
            double draw,
            int price,
            String available,
            Optional<Reflect> reflect) {
        public Model {
            Check.positive("capacity", capacity);
            Check.positive("regen", regen);
            Check.notNegative("delay", delay);
            Check.notNegative("draw", draw);
            Check.notNegative("price", price);
        }

        /** A Mk shield (no reflect). */
        public Model(
                String name, double capacity, double regen, double delay, double draw, int price, String available) {
            this(name, capacity, regen, delay, draw, price, available, Optional.empty());
        }
    }

    /**
     * M10 part A (design/player/shields, Reflex Shield (Act 6)): while the shield holds at least {@code window} of its
     * capacity, an enemy bullet or missile hitting it costs it {@code share} of its damage and comes back as the ship's
     * bolt, straight up the screen at {@code speed} px/s with {@code factor} × its class's damage.
     */
    public record Reflect(double window, double share, double speed, double factor) {
        public Reflect {
            Check.that(window > 0 && window <= 1, "reflect.window: a share of the capacity, in (0, 1]");
            Check.share("reflect.share", share);
            Check.positive("reflect.speed", speed);
            Check.positive("reflect.factor", factor);
        }
    }
}
