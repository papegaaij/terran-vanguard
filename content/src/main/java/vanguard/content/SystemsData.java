package vanguard.content;

import java.util.List;
import java.util.Optional;

/** design/player/systems/data.yaml: engines, utility bays and utility modules. */
public record SystemsData(List<Engine> engines, Bays bays, List<Utility> utility) {
    public SystemsData {
        Check.notEmpty("engines", engines);
    }

    /**
     * The utility bays: {@code start} are fitted from the start, each of {@code extra} is bought once
     * (the third bay, from Act 3).
     */
    public record Bays(int start, List<Bay> extra) {
        public Bays {
            Check.positive("start", start);
        }
    }

    /** A utility bay bought in the hangar; {@code price} in credits. */
    public record Bay(int price, String available) {
        public Bay {
            Check.positive("price", price);
        }
    }

    /** One engine; {@code speed} in px/s, {@code draw} in MW. */
    public record Engine(String name, double speed, double draw, int price, String available) {
        public Engine {
            Check.positive("speed", speed);
            Check.notNegative("draw", draw);
            Check.notNegative("price", price);
        }
    }

    /**
     * A utility module with one price per upgrade level; {@code design} is its design status.
     *
     * @param magnet the Pickup magnet's numbers per level; empty for the other modules
     * @param forSale false keeps the module out of the shop although it is unlocked (an M5 module
     *     whose effects are not built yet); true if not given
     * @param targeting the Targeting computer's numbers; empty for the other modules
     * @param salvage the Salvage scanner's bonus per level; empty for the other modules
     * @param dash M7 part A: the Evasive thrusters' dash; empty for the other modules
     * @param repair M8 part A: the Auto-repair nanites' repair; empty for the other modules
     * @param hull M8 part A: the Pressure hull's effect under water; empty for the other modules
     */
    public record Utility(
            String name,
            double draw,
            List<Integer> prices,
            String available,
            String design,
            Optional<Magnet> magnet,
            Optional<Boolean> forSale,
            Optional<Targeting> targeting,
            Optional<Salvage> salvage,
            Optional<Dash> dash,
            Optional<Repair> repair,
            Optional<Hull> hull) {
        public Utility {
            Check.notNegative("draw", draw);
            Check.notEmpty("prices", prices);
            magnet.ifPresent(numbers -> Check.that(
                    numbers.radius().size() == prices.size() && numbers.pull().size() == prices.size(),
                    "magnet: one radius and one pull per level"));
            salvage.ifPresent(
                    numbers -> Check.that(numbers.bonus().size() == prices.size(), "salvage: one bonus per level"));
            repair.ifPresent(numbers ->
                    Check.that(numbers.interval().size() == prices.size(), "repair: one interval per level"));
        }

        /** Whether the shop sells it once it is unlocked. */
        public boolean sold() {
            return forSale.orElse(true);
        }
    }

    /**
     * The Targeting computer (design/player/systems): {@code turnBonus} is the share added to the
     * Stormhawk's homing turn rates and the Swivel Gun's slew; an enemy's HP bar stays {@code
     * barSeconds} after its last hit and then fades out over {@code barFade} s.
     */
    public record Targeting(double turnBonus, double barSeconds, double barFade) {
        public Targeting {
            Check.notNegative("turn_bonus", turnBonus);
            Check.positive("bar_seconds", barSeconds);
            Check.notNegative("bar_fade", barFade);
        }
    }

    /**
     * M7 part A, the Evasive thrusters' dash (design/player/systems, Evasive thrusters): {@code
     * distance} px in one of eight directions over {@code seconds}, the ship's own speed set aside;
     * {@code invulnerable} s from its start in which enemy bullets pass through the ship and contacts
     * and hazards do not hurt; the next dash {@code cooldown} s after its start; a double-tap is the
     * second press of a direction within {@code double_tap} s of the first press's release.
     */
    public record Dash(double distance, double seconds, double invulnerable, double cooldown, double doubleTap) {
        public Dash {
            Check.positive("distance", distance);
            Check.positive("seconds", seconds);
            Check.notNegative("invulnerable", invulnerable);
            Check.positive("cooldown", cooldown);
            Check.positive("double_tap", doubleTap);
        }
    }

    /**
     * M8 part A, the Auto-repair nanites (design/player/systems, Auto-repair nanites (L24)): in flight 1 armour point
     * every {@code interval} s (one per level) up to {@code cap} of the plating's maximum; a hit pauses it {@code
     * pause} s; nothing while the ship is wrecked. Rook's and the Bulwark's armour is not repaired.
     */
    public record Repair(List<Double> interval, double cap, double pause) {
        public Repair {
            interval = List.copyOf(interval);
            interval.forEach(seconds -> Check.positive("interval", seconds));
            Check.share("cap", cap);
            Check.positive("cap", cap);
            Check.notNegative("pause", pause);
        }

        /** Seconds per armour point at {@code level} (1 …). */
        public double interval(int level) {
            return interval.get(level - 1);
        }
    }

    /**
     * M8 part A, the Pressure hull (design/player/systems, Pressure hull (L22)): {@code drag} false, the ship keeps its
     * normal acceleration under water (the water's drag is removed); a second hull adds nothing.
     */
    public record Hull(boolean drag) {}

    /** The Salvage scanner: per level, the share added to salvage pickups' and hidden crates' credits. */
    public record Salvage(List<Double> bonus) {
        public Salvage {
            bonus.forEach(value -> Check.notNegative("bonus", value));
        }
    }

    /** A Pickup magnet's reach from the ship's centre (px) and pull speed (px/s), one of each per level. */
    public record Magnet(List<Double> radius, List<Double> pull) {
        public Magnet {
            radius.forEach(value -> Check.positive("radius", value));
            pull.forEach(value -> Check.positive("pull", value));
        }
    }
}
