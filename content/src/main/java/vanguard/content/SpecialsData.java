package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * design/player/specials/data.yaml: the specials, the Airstrike's and the Smart Bomb's numbers and
 * (M7 part A) the EMP Burst's, the Orbital Lance's and the Shield Overcharge's.
 *
 * @param inputBuffer seconds a press of the special button waits while the special is busy
 * @param cooldownPickup M7 part A: seconds a special charge pickup takes off a cooldown special's
 *     running cooldown
 */
public record SpecialsData(
        double inputBuffer,
        double cooldownPickup,
        List<Special> specials,
        Airstrike airstrike,
        SmartBomb smartBomb,
        Emp emp,
        OrbitalLance orbitalLance,
        Overcharge overcharge) {
    public SpecialsData {
        Check.notNegative("input_buffer", inputBuffer);
        Check.notNegative("cooldown_pickup", cooldownPickup);
        specials = List.copyOf(specials);
    }

    /**
     * A special available from level {@code unlock}: bought by the charge ({@code charge_price},
     * {@code max_charges}), or (M7 part A) a cooldown special bought once ({@code price}) that is
     * ready again {@code cooldown} s after each use.
     *
     * @param freeCharges charges given once, at the hangar visit before the unlock level (a charge
     *     special only)
     */
    public record Special(
            String name,
            Optional<Integer> chargePrice,
            Optional<Integer> maxCharges,
            Optional<Integer> price,
            Optional<Double> cooldown,
            int unlock,
            Optional<Integer> freeCharges) {
        public Special {
            chargePrice.ifPresent(cost -> Check.notNegative("charge_price", cost));
            maxCharges.ifPresent(most -> Check.positive("max_charges", most));
            price.ifPresent(cost -> Check.positive("price", cost));
            cooldown.ifPresent(seconds -> Check.positive("cooldown", seconds));
            Check.positive("unlock", unlock);
            freeCharges.ifPresent(free -> Check.notNegative("free_charges", free));
            boolean charged = chargePrice.isPresent() && maxCharges.isPresent();
            boolean cooled = price.isPresent() && cooldown.isPresent();
            Check.that(
                    charged != cooled
                            && (charged || chargePrice.isEmpty() && maxCharges.isEmpty() && freeCharges.isEmpty())
                            && (cooled || price.isEmpty() && cooldown.isEmpty()),
                    name + ": a special has charge_price and max_charges (and optional free_charges), or a price"
                            + " and a cooldown");
        }

        /** M7 part A: whether it is a cooldown special (bought once, no charges). */
        public boolean cooldownSpecial() {
            return cooldown.isPresent();
        }

        /** The charges given once at the unlock; 0 for none. */
        public int free() {
            return freeCharges.orElse(0);
        }
    }

    /**
     * The Airstrike (design/player/specials, Airstrike (L04)).
     *
     * @param delay seconds from the call to the bombers entering at the bottom edge
     * @param offset px left and right of the ship's x at the call
     * @param speed the bombers' px/s up the screen
     * @param bomberSize the bomber sprite, px, facing up
     * @param bombSpacing px of travel between two bombs of a bomber
     * @param fall seconds from a bomb's release to its blast
     * @param blastRadius px
     */
    public record Airstrike(
            double delay,
            double offset,
            double speed,
            Size bomberSize,
            double bombSpacing,
            double fall,
            double blastRadius,
            Damage damage,
            Cap cap,
            Radio radio) {
        public Airstrike {
            Check.notNegative("delay", delay);
            Check.notNegative("offset", offset);
            Check.positive("speed", speed);
            Check.positive("bomb_spacing", bombSpacing);
            Check.positive("fall", fall);
            Check.positive("blast_radius", blastRadius);
        }
    }

    /** Damage per blast to ground and low-air targets, and to air targets. */
    public record Damage(double ground, double air) {
        public Damage {
            Check.notNegative("ground", ground);
            Check.notNegative("air", air);
        }
    }

    /** The most damage one strike deals to one ground or low-air target, air target and boss part. */
    public record Cap(double ground, double air, double bossPart) {
        public Cap {
            Check.notNegative("ground", ground);
            Check.notNegative("air", air);
            Check.notNegative("boss_part", bossPart);
        }
    }

    /** The call's radio line. */
    public record Radio(String speaker, String portrait, String line) {}

    /**
     * The Smart Bomb (design/player/specials, Smart Bomb (L06)).
     *
     * @param ring seconds for the ring to cover the whole play field
     * @param invulnerable seconds the ship takes no damage from the activation
     * @param repeat seconds between two bombs
     */
    public record SmartBomb(Flash flash, double ring, BombDamage damage, double invulnerable, double repeat) {
        public SmartBomb {
            Check.positive("ring", ring);
            Check.notNegative("invulnerable", invulnerable);
            Check.positive("repeat", repeat);
        }
    }

    /** A white flash: held {@code seconds} at {@code opacity}, then fading over {@code fade} seconds. */
    public record Flash(double seconds, double opacity, double fade) {
        public Flash {
            Check.notNegative("seconds", seconds);
            Check.notNegative("opacity", opacity);
            Check.notNegative("fade", fade);
        }
    }

    /** The Smart Bomb's damage to every enemy on screen and to each boss part. */
    public record BombDamage(double all, double bossPart) {
        public BombDamage {
            Check.notNegative("all", all);
            Check.notNegative("boss_part", bossPart);
        }
    }

    /**
     * M7 part A, the EMP Burst (design/player/specials, EMP Burst (L15)).
     *
     * @param ring seconds for the ring to cover the whole play field from the ship
     * @param stun seconds a machine and a Vrell (biomechanical) unit stay stunned
     * @param repeat seconds between two bursts
     */
    public record Emp(double ring, Stun stun, double repeat) {
        public Emp {
            Check.positive("ring", ring);
            Check.positive("repeat", repeat);
        }
    }

    /** The EMP's stun, seconds: a machine's ({@code machine: true} in its stat block) and a Vrell unit's. */
    public record Stun(double machine, double vrell) {
        public Stun {
            Check.positive("machine", machine);
            Check.positive("vrell", vrell);
        }
    }

    /**
     * M7 part A, the Orbital Lance (design/player/specials, Orbital Lance (L17)).
     *
     * @param lock seconds the red targeting line shows before the lance fires
     * @param width the column's width, px
     * @param seconds how long the column burns
     * @param fade seconds of its fade afterwards
     * @param follow px/s its centre follows the ship's x at most
     * @param dps damage per second to every enemy in the column
     * @param bossPart a boss part's damage per second and the most one lance deals it
     * @param repeat seconds after the fade before the next lance can be called
     */
    public record OrbitalLance(
            double lock,
            double width,
            double seconds,
            double fade,
            double follow,
            double dps,
            BossPart bossPart,
            double repeat,
            Radio radio) {
        public OrbitalLance {
            Check.notNegative("lock", lock);
            Check.positive("width", width);
            Check.positive("seconds", seconds);
            Check.notNegative("fade", fade);
            Check.positive("follow", follow);
            Check.positive("dps", dps);
            Check.notNegative("repeat", repeat);
        }
    }

    /** A boss part's damage per second under the lance and the most one lance deals it. */
    public record BossPart(double dps, double cap) {
        public BossPart {
            Check.positive("dps", dps);
            Check.positive("cap", cap);
        }
    }

    /**
     * M7 part A, the Shield Overcharge (design/player/specials, Shield Overcharge (L19)).
     *
     * @param factor the shield capacity's multiplier while overcharged
     * @param seconds how long the capacity stays raised
     * @param bleed seconds the excess over the normal capacity bleeds away over afterwards
     */
    public record Overcharge(double factor, double seconds, double bleed) {
        public Overcharge {
            Check.that(factor > 1, "factor must be > 1, was " + factor);
            Check.positive("seconds", seconds);
            Check.notNegative("bleed", bleed);
        }
    }
}
