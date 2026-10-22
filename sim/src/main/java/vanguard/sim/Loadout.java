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
        Optional<BulwarkSpec> bulwark) {
    public Loadout {
        if (!(salvageBonus >= 0)) {
            throw new IllegalArgumentException("the salvage bonus is a share of at least 0");
        }
        if (wingman.isPresent() && bulwark.isPresent()) {
            throw new IllegalArgumentException("the escort slot holds Rook or a Bulwark, not both");
        }
    }

    /** A loadout without M8 part A's Pressure hull, nanites or Bulwark. */
    public Loadout(
            ShipSpec ship,
            Armament armament,
            ShieldModel shield,
            Plating plating,
            Optional<SpecialSpec> special,
            Optional<Magnet> magnet,
            Optional<WingmanSpec> wingman,
            double salvageBonus,
            Optional<ThrusterSpec> thrusters) {
        this(
                ship,
                armament,
                shield,
                plating,
                special,
                magnet,
                wingman,
                salvageBonus,
                thrusters,
                false,
                Optional.empty(),
                Optional.empty());
    }

    /** A loadout without the Evasive thrusters. */
    public Loadout(
            ShipSpec ship,
            Armament armament,
            ShieldModel shield,
            Plating plating,
            Optional<SpecialSpec> special,
            Optional<Magnet> magnet,
            Optional<WingmanSpec> wingman,
            double salvageBonus) {
        this(ship, armament, shield, plating, special, magnet, wingman, salvageBonus, Optional.empty());
    }

    /** A loadout without a special, a magnet, a wingman or a salvage bonus. */
    public Loadout(ShipSpec ship, Armament armament, ShieldModel shield, Plating plating) {
        this(ship, armament, shield, plating, Optional.empty(), Optional.empty(), Optional.empty(), 0);
    }

    /** This loadout with {@code fitted} in the special slot. */
    public Loadout withSpecial(SpecialSpec fitted) {
        return new Loadout(
                ship,
                armament,
                shield,
                plating,
                Optional.of(fitted),
                magnet,
                wingman,
                salvageBonus,
                thrusters,
                pressureHull,
                nanites,
                bulwark);
    }

    /** This loadout with {@code fitted} in a utility bay. */
    public Loadout withMagnet(Magnet fitted) {
        return new Loadout(
                ship,
                armament,
                shield,
                plating,
                special,
                Optional.of(fitted),
                wingman,
                salvageBonus,
                thrusters,
                pressureHull,
                nanites,
                bulwark);
    }

    /** This loadout with {@code flying} in the escort slot. */
    public Loadout withWingman(WingmanSpec flying) {
        return new Loadout(
                ship,
                armament,
                shield,
                plating,
                special,
                magnet,
                Optional.of(flying),
                salvageBonus,
                thrusters,
                pressureHull,
                nanites,
                bulwark);
    }

    /** This loadout with a Salvage scanner adding {@code bonus} (a share) to salvage and hidden crates. */
    public Loadout withSalvage(double bonus) {
        return new Loadout(
                ship,
                armament,
                shield,
                plating,
                special,
                magnet,
                wingman,
                bonus,
                thrusters,
                pressureHull,
                nanites,
                bulwark);
    }

    /** M7 part A: this loadout with the Evasive thrusters' {@code dash} in a utility bay. */
    public Loadout withThrusters(ThrusterSpec dash) {
        return new Loadout(
                ship,
                armament,
                shield,
                plating,
                special,
                magnet,
                wingman,
                salvageBonus,
                Optional.of(dash),
                pressureHull,
                nanites,
                bulwark);
    }

    /** M8 part A: this loadout with the Pressure hull in a utility bay (no water drag on the ship). */
    public Loadout withPressureHull() {
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
                true,
                nanites,
                bulwark);
    }

    /** M8 part A: this loadout with the Auto-repair nanites ({@code repair}) in a utility bay. */
    public Loadout withNanites(NaniteSpec repair) {
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
                Optional.of(repair),
                bulwark);
    }

    /** M8 part A: this loadout with the Bulwark {@code flying} in the escort slot (instead of Rook). */
    public Loadout withBulwark(BulwarkSpec flying) {
        return new Loadout(
                ship,
                armament,
                shield,
                plating,
                special,
                magnet,
                Optional.empty(),
                salvageBonus,
                thrusters,
                pressureHull,
                nanites,
                Optional.of(flying));
    }
}
