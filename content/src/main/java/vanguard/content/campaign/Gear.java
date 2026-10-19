package vanguard.content.campaign;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * What the hangar changes in the campaign (design/ui/hangar): the credits, the fitted item per
 * slot, the owned items that are not fitted (by kind), the charges per special, the armour and the
 * escort slot (Rook's fitted gun in the loadout, his other guns in the inventory, the rest in
 * {@link Escort}) and (M7 part A) the utility bays bought. Immutable, so a hangar visit can keep the
 * state before each transaction for its undo.
 *
 * @param armour the current armour points (repair is not automatic)
 * @param escort Rook: hired or not, his side and armour
 * @param bays M7 part A: the utility bays bought in the hangar (design/player/systems, Third utility
 *     bay): 0 or 1, the third bay's slot {@link LoadoutSlot#UTILITY_3} open from 1
 */
public record Gear(
        int credits,
        Map<LoadoutSlot, Fitted> loadout,
        Map<ItemKind, List<Fitted>> inventory,
        Map<String, Integer> specials,
        double armour,
        Escort escort,
        int bays) {
    /** The first utility slot a bought bay opens; the bays before it are fitted from the start. */
    static final LoadoutSlot FIRST_BOUGHT_BAY = LoadoutSlot.UTILITY_3;

    public Gear {
        if (bays < 0) {
            throw new IllegalArgumentException("bays bought: " + bays);
        }
        if (loadout.containsKey(LoadoutSlot.UTILITY_3) && bays < 1) {
            throw new IllegalArgumentException("a module in the third utility bay, which is not bought");
        }
        loadout = ordered(LoadoutSlot.class, loadout);
        inventory = inventoryCopy(inventory);
        specials = Collections.unmodifiableMap(new TreeMap<>(specials));
        java.util.Objects.requireNonNull(escort, "escort");
        if (!escort.hired() && (loadout.containsKey(LoadoutSlot.ESCORT) || inventory.containsKey(ItemKind.ESCORT))) {
            throw new IllegalArgumentException("guns in the escort slot of a Rook who is not hired");
        }
    }

    /** Gear without a bought utility bay (the gear of Acts 1–2). */
    public Gear(
            int credits,
            Map<LoadoutSlot, Fitted> loadout,
            Map<ItemKind, List<Fitted>> inventory,
            Map<String, Integer> specials,
            double armour,
            Escort escort) {
        this(credits, loadout, inventory, specials, armour, escort, 0);
    }

    /** A copy in slot order, so a save lists the slots as the ship has them. */
    static <K extends Enum<K>, V> Map<K, V> ordered(Class<K> type, Map<K, V> map) {
        Map<K, V> copy = new EnumMap<>(type);
        copy.putAll(map);
        return Collections.unmodifiableMap(copy);
    }

    /** A copy of an inventory in kind order, without empty kinds. */
    static Map<ItemKind, List<Fitted>> inventoryCopy(Map<ItemKind, List<Fitted>> inventory) {
        Map<ItemKind, List<Fitted>> copy = new EnumMap<>(ItemKind.class);
        inventory.forEach((kind, items) -> {
            if (!items.isEmpty()) {
                copy.put(kind, List.copyOf(items));
            }
        });
        return Collections.unmodifiableMap(copy);
    }

    public Gear withCredits(int changed) {
        return new Gear(changed, loadout, inventory, specials, armour, escort, bays);
    }

    public Gear withArmour(double changed) {
        return new Gear(credits, loadout, inventory, specials, changed, escort, bays);
    }

    public Gear withEscort(Escort changed) {
        return new Gear(credits, loadout, inventory, specials, armour, changed, bays);
    }

    /** M7 part A: this gear with {@code changed} utility bays bought. */
    public Gear withBays(int changed) {
        return new Gear(credits, loadout, inventory, specials, armour, escort, changed);
    }

    /** M7 part A: whether a utility slot is open: a starting bay, or a bought one. */
    public boolean open(LoadoutSlot slot) {
        if (slot.kind() != ItemKind.UTILITY) {
            return true;
        }
        return slot.ordinal() - FIRST_BOUGHT_BAY.ordinal() < bays;
    }

    /** The owned, unfitted items of a kind. */
    public List<Fitted> inventory(ItemKind kind) {
        return inventory.getOrDefault(kind, List.of());
    }

    /** The charges carried for a special. */
    public int charges(String special) {
        return specials.getOrDefault(special, 0);
    }
}
