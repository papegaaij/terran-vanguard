package vanguard.content.campaign;

import vanguard.sim.WingmanSpec;

/**
 * The escort slot in the campaign (design/player/wingmen): whether Rook is hired, his side and his
 * armour. His fitted gun is the {@link LoadoutSlot#ESCORT} slot of the {@link Gear}'s loadout and
 * his other guns are its {@link ItemKind#ESCORT} inventory, so the hangar's transactions treat them
 * like the player's items; the save keeps all of it in its {@code escort} field.
 *
 * <p>M8 part A (design/player/wingmen, Heavy drone "Bulwark"): the slot holds Rook or the Bulwark heavy drone, the
 * player's choice ({@code flies}), once a Bulwark is bought (from Level 22); while Rook is missing (Levels 27–29) it
 * holds the Bulwark, a free loaner when none is owned ({@link Campaign#escortFlight(int)}).
 *
 * @param hired Rook has joined (from the hangar visit before Level 08)
 * @param side the player's side he flies on (a hangar setting; the Bulwark's slot is on the same side)
 * @param armour his current armour (repair is not automatic); 0 after an ejection: grounded
 * @param flies M8 part A: who the player chose for the slot; Rook when no Bulwark is owned
 * @param bulwark M8 part A: the owned Bulwark, if bought, and its armour
 */
public record Escort(boolean hired, WingmanSpec.Side side, double armour, Escorter flies, Bulwark bulwark) {
    /** M8 part A: who flies in the escort slot. */
    public enum Escorter {
        ROOK,
        BULWARK
    }

    /**
     * M8 part A: the Bulwark heavy drone the player bought: {@code owned} and its {@code armour} (kept between levels,
     * repaired in the hangar; a destroyed one is rebuilt for the next level). Not owned, the armour is its full armour.
     */
    public record Bulwark(boolean owned, double armour) {
        public Bulwark {
            if (!(armour >= 0)) {
                throw new IllegalArgumentException("invalid Bulwark: armour " + armour);
            }
        }

        public Bulwark withArmour(double changed) {
            return new Bulwark(owned, changed);
        }
    }

    public Escort {
        if (side == null || !(armour >= 0) || flies == null || bulwark == null) {
            throw new IllegalArgumentException("invalid escort: side " + side + ", armour " + armour + ", flies "
                    + flies + ", Bulwark " + bulwark);
        }
    }

    /** An escort slot before M8 part A: Rook chosen, no Bulwark owned (its full armour {@code bulwarkArmour}). */
    public Escort(boolean hired, WingmanSpec.Side side, double armour, double bulwarkArmour) {
        this(hired, side, armour, Escorter.ROOK, new Bulwark(false, bulwarkArmour));
    }

    /** Rook's armour changed. */
    public Escort withArmour(double changed) {
        return new Escort(hired, side, changed, flies, bulwark);
    }

    public Escort withSide(WingmanSpec.Side changed) {
        return new Escort(hired, changed, armour, flies, bulwark);
    }

    /** M8 part A: the player's choice for the slot. */
    public Escort withFlies(Escorter changed) {
        return new Escort(hired, side, armour, changed, bulwark);
    }

    /** M8 part A: the Bulwark bought, sold or its armour changed. */
    public Escort withBulwark(Bulwark changed) {
        return new Escort(hired, side, armour, flies, changed);
    }
}
