package vanguard.sim;

import java.util.Optional;

/**
 * What the ship flies with: its hull, weapons, shield, plating, special and utility modules, and the
 * wingman in its escort slot.
 *
 * @param ship the hull's flight numbers, including the fitted engine's speed
 * @param armament the fitted weapons at their upgrade levels
 * @param shield the shield, its regeneration including the spare-power bonus (design/player/generator)
 * @param special the fitted special with the charges carried into the level; empty for none
 * @param magnet the fitted Pickup magnet at its level; empty for none
 * @param wingman the wingman flying in the escort slot (Rook, from Level 08); empty for none
 * @param salvageBonus the fitted Salvage scanner's share added to salvage pickups' and hidden crates'
 *     credits (design/player/systems); 0 for none
 * @param thrusters M7 part A: the fitted Evasive thrusters' dash; empty for none
 * @param pressureHull M8 part A: the Pressure hull is fitted (no water drag on the ship)
 * @param nanites M8 part A: the fitted Auto-repair nanites at their level; empty for none
 * @param bulwark M8 part A: the Bulwark heavy drone in the escort slot; empty for none (never with a wingman)
 * @param power M9 part A: the power budget in flight (design/player/generator, Enemy drain effects; {@link Power}):
 *     empty for a loadout no drainer can reach (the sim then never sheds power)
 * @param rescue M9 part B (design/player/wingmen, Rook's rescue and return; Level 29): Rook waiting to be rescued, with
 *     the gun he was lost with and full armour: he joins the escort slot at the level's escort swap ({@link
 *     EscortSwapSpec}); empty for none (never with Rook already in the slot)
 * @param spoofDelay M10 part A (design/player/systems, the IFF spoofer): the fitted Ascendancy IFF spoofer's delay, s:
 *     every spoofable unit's aimed volley comes this late, at its stale aim ({@link EnemyForce#spoof}); 0 without it
 */
public record Loadout(
        ShipSpec ship,
        Armament armament,
        ShieldModel shield,
        Plating plating,
        Optional<SpecialSpec> special,
        Optional<Magnet> magnet,
        Optional<WingmanSpec> wingman,
        double salvageBonus,
        Optional<ThrusterSpec> thrusters,
        boolean pressureHull,
        Optional<NaniteSpec> nanites,
        Optional<BulwarkSpec> bulwark,
        Optional<Power> power,
        Optional<WingmanSpec> rescue,
        double spoofDelay) {
    public Loadout {
        java.util.Objects.requireNonNull(power);
        java.util.Objects.requireNonNull(rescue);
        if (rescue.isPresent() && wingman.isPresent()) {
            throw new IllegalArgumentException("a rescued Rook joins an escort slot Rook does not fly in");
        }
        if (!(salvageBonus >= 0)) {
            throw new IllegalArgumentException("the salvage bonus is a share of at least 0");
        }
        if (wingman.isPresent() && bulwark.isPresent()) {
            throw new IllegalArgumentException("the escort slot holds Rook or a Bulwark, not both");
        }
        if (!(spoofDelay >= 0)) {
            throw new IllegalArgumentException("the spoofer's delay is 0 s or more: " + spoofDelay);
        }
    }

    /** A loadout without a special, a magnet, a wingman, a salvage bonus or any later module. */
    public Loadout(ShipSpec ship, Armament armament, ShieldModel shield, Plating plating) {
        this(
                ship,
                armament,
                shield,
                plating,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0,
                Optional.empty(),
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0);
    }

    /**
     * M10 part A (step A1b, as RF9b's {@link WaveSpec#builder}): a builder for a loadout of this hull, armament, shield
     * and plating and nothing else fitted; its setters name the components.
     */
    public static Builder builder(ShipSpec ship, Armament armament, ShieldModel shield, Plating plating) {
        return new Builder(new Loadout(ship, armament, shield, plating));
    }

    /** A builder holding this loadout's every component, for a copy with some changed. */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * M9 part B: this loadout with Rook ({@code rook}) waiting to be rescued in the level's escort swap (the slot holds
     * the Bulwark, or nothing, until then).
     */
    public Loadout withRescue(WingmanSpec rook) {
        return toBuilder().rescue(Optional.of(rook)).build();
    }

    /** This loadout with {@code fitted} in the special slot. */
    public Loadout withSpecial(SpecialSpec fitted) {
        return toBuilder().special(Optional.of(fitted)).build();
    }

    /** This loadout with {@code fitted} in a utility bay. */
    public Loadout withMagnet(Magnet fitted) {
        return toBuilder().magnet(Optional.of(fitted)).build();
    }

    /** This loadout with {@code flying} in the escort slot. */
    public Loadout withWingman(WingmanSpec flying) {
        return toBuilder().wingman(Optional.of(flying)).build();
    }

    /** This loadout with a Salvage scanner adding {@code bonus} (a share) to salvage and hidden crates. */
    public Loadout withSalvage(double bonus) {
        return toBuilder().salvageBonus(bonus).build();
    }

    /** M7 part A: this loadout with the Evasive thrusters' {@code dash} in a utility bay. */
    public Loadout withThrusters(ThrusterSpec dash) {
        return toBuilder().thrusters(Optional.of(dash)).build();
    }

    /** M8 part A: this loadout with the Pressure hull in a utility bay (no water drag on the ship). */
    public Loadout withPressureHull() {
        return toBuilder().pressureHull(true).build();
    }

    /** M8 part A: this loadout with the Auto-repair nanites ({@code repair}) in a utility bay. */
    public Loadout withNanites(NaniteSpec repair) {
        return toBuilder().nanites(Optional.of(repair)).build();
    }

    /** M8 part A: this loadout with the Bulwark {@code flying} in the escort slot (instead of Rook). */
    public Loadout withBulwark(BulwarkSpec flying) {
        return toBuilder()
                .wingman(Optional.empty())
                .bulwark(Optional.of(flying))
                .build();
    }

    /** M9 part A: this loadout with its power budget in flight (design/player/generator, Enemy drain effects). */
    public Loadout withPower(Power budget) {
        return toBuilder().power(Optional.of(budget)).build();
    }

    /**
     * M10 part A (design/player/systems, the IFF spoofer): this loadout with the Ascendancy IFF spoofer in a utility bay:
     * every spoofable unit's aimed volleys come {@code delaySeconds} late.
     */
    public Loadout withSpoofer(double delaySeconds) {
        return toBuilder().spoofDelay(delaySeconds).build();
    }

    /**
     * M10 part A (step A1b): a loadout under construction ({@link Loadout#builder}, {@link Loadout#toBuilder}), one
     * setter per component; {@link #build()} runs the canonical constructor, so its checks see the whole loadout.
     */
    public static final class Builder {
        private ShipSpec ship;
        private Armament armament;
        private ShieldModel shield;
        private Plating plating;
        private Optional<SpecialSpec> special;
        private Optional<Magnet> magnet;
        private Optional<WingmanSpec> wingman;
        private double salvageBonus;
        private Optional<ThrusterSpec> thrusters;
        private boolean pressureHull;
        private Optional<NaniteSpec> nanites;
        private Optional<BulwarkSpec> bulwark;
        private Optional<Power> power;
        private Optional<WingmanSpec> rescue;
        private double spoofDelay;

        private Builder(Loadout from) {
            ship = from.ship;
            armament = from.armament;
            shield = from.shield;
            plating = from.plating;
            special = from.special;
            magnet = from.magnet;
            wingman = from.wingman;
            salvageBonus = from.salvageBonus;
            thrusters = from.thrusters;
            pressureHull = from.pressureHull;
            nanites = from.nanites;
            bulwark = from.bulwark;
            power = from.power;
            rescue = from.rescue;
            spoofDelay = from.spoofDelay;
        }

        public Builder ship(ShipSpec value) {
            ship = value;
            return this;
        }

        public Builder armament(Armament value) {
            armament = value;
            return this;
        }

        public Builder shield(ShieldModel value) {
            shield = value;
            return this;
        }

        public Builder plating(Plating value) {
            plating = value;
            return this;
        }

        public Builder special(Optional<SpecialSpec> value) {
            special = value;
            return this;
        }

        public Builder magnet(Optional<Magnet> value) {
            magnet = value;
            return this;
        }

        public Builder wingman(Optional<WingmanSpec> value) {
            wingman = value;
            return this;
        }

        public Builder salvageBonus(double value) {
            salvageBonus = value;
            return this;
        }

        public Builder thrusters(Optional<ThrusterSpec> value) {
            thrusters = value;
            return this;
        }

        public Builder pressureHull(boolean value) {
            pressureHull = value;
            return this;
        }

        public Builder nanites(Optional<NaniteSpec> value) {
            nanites = value;
            return this;
        }

        public Builder bulwark(Optional<BulwarkSpec> value) {
            bulwark = value;
            return this;
        }

        public Builder power(Optional<Power> value) {
            power = value;
            return this;
        }

        public Builder rescue(Optional<WingmanSpec> value) {
            rescue = value;
            return this;
        }

        public Builder spoofDelay(double seconds) {
            spoofDelay = seconds;
            return this;
        }

        public Loadout build() {
            return new Loadout(
                    ship,
                    armament,
                    shield,
                    plating,
                    special,
                    magnet,
                    wingman,
                    salvageBonus,
                    thrusters,
                    pressureHull,
                    nanites,
                    bulwark,
                    power,
                    rescue,
                    spoofDelay);
        }
    }

    /**
     * M9 part A, the power budget in flight (design/player/generator, Enemy drain effects; {@link PowerState}): the
     * generator's {@code output}, the fitted {@code load} (every fitted part's draw at its level, the shield's
     * included; an overdrive's extra draw never counts), the shield's own draw {@code shieldDraw}, the shield's regen
     * {@code regenPerSecond} without the spare-power bonus (the difficulty's lever in it), the bonus rule ({@code
     * regenBonusPerMw} a spare MW, at most {@code maxRegenBonus}) and the drain's numbers. The {@link ShieldModel}'s
     * regen is this regen with the bonus of output − load already in it; under a drain the bonus follows the
     * effective output E.
     */
    public record Power(
            double output,
            double load,
            double shieldDraw,
            double regenPerSecond,
            double regenBonusPerMw,
            double maxRegenBonus,
            Drain drain) {
        public Power {
            if (!(output > 0) || !(load >= 0) || !(shieldDraw >= 0) || !(shieldDraw <= load)) {
                throw new IllegalArgumentException("a power budget has an output and a load that holds the shield's "
                        + "draw: " + output + ", " + load + ", " + shieldDraw);
            }
            if (!(regenPerSecond >= 0) || !(regenBonusPerMw >= 0) || !(maxRegenBonus >= 0)) {
                throw new IllegalArgumentException("a power budget's regen and bonus are at least 0");
            }
            java.util.Objects.requireNonNull(drain);
        }
    }

    /**
     * M9 part A, the drain's numbers (design/player/generator, Enemy drain effects; {@code generator/data.yaml}'s
     * {@code drain}): each latched drainer lowers the effective output by {@code perDrainer} MW, all of them together
     * by at most {@code most} MW; the weapons' fire-rate factor never falls below {@code floor}.
     */
    public record Drain(double perDrainer, double most, double floor) {
        public Drain {
            if (!(perDrainer > 0) || !(most >= perDrainer) || !(floor > 0 && floor <= 1)) {
                throw new IllegalArgumentException("a drain takes MW, at most a cap of them, and a floor in (0, 1]: "
                        + perDrainer + ", " + most + ", " + floor);
            }
        }
    }
}
