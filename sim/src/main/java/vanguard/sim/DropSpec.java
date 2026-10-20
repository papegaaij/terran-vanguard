package vanguard.sim;

import java.util.List;

/**
 * A level's scripted drops (refactoring step LS-3, {@link LevelScript#drops()}): the groups' and set-piece parts'
 * drops, the placed pickups and the drop factor.
 *
 * @param groupDrops pickups dropped where a group's last unit dies when it is cleared
 * @param partDrops part G: pickups dropped by a set piece's parts when they are shot off (Level 07's first destroyed
 *     bay sac)
 * @param placedPickups M5 part F: pickups entering at the top edge at their time (Level 12's armour patch in the eye);
 *     M5 part G: or released at a point in the field (Level 13's supply drones' patches), in time order
 * @param dropFactor M5 part F: each enemy's drop {@code every} is divided by it, rounded to the nearest, at least 1
 *     (Level 12's shield cells, 1.5); 1 for none
 */
public record DropSpec(
        List<GroupDrop> groupDrops, List<PartDrop> partDrops, List<PlacedPickup> placedPickups, double dropFactor) {
    /** No scripted drops and a drop factor of 1. */
    public static final DropSpec NONE = new DropSpec(List.of(), List.of(), List.of(), 1.0);

    public DropSpec {
        // M5 part G: the drones' point drops and the top edge's placed pickups in one time order.
        placedPickups = placedPickups.stream()
                .sorted(java.util.Comparator.comparingDouble(PlacedPickup::t))
                .toList();
        if (!(dropFactor > 0)) {
            throw new IllegalArgumentException("a drop factor is positive: " + dropFactor);
        }
        groupDrops = List.copyOf(groupDrops);
        partDrops = List.copyOf(partDrops);
    }

    /** These drops with another {@code groupDrops}. */
    public DropSpec withGroupDrops(List<GroupDrop> groupDrops) {
        return new DropSpec(groupDrops, partDrops, placedPickups, dropFactor);
    }

    /** These drops with another {@code partDrops}. */
    public DropSpec withPartDrops(List<PartDrop> partDrops) {
        return new DropSpec(groupDrops, partDrops, placedPickups, dropFactor);
    }

    /** These drops with another {@code placedPickups}. */
    public DropSpec withPlacedPickups(List<PlacedPickup> placedPickups) {
        return new DropSpec(groupDrops, partDrops, placedPickups, dropFactor);
    }

    /** These drops with another {@code dropFactor}. */
    public DropSpec withDropFactor(double dropFactor) {
        return new DropSpec(groupDrops, partDrops, placedPickups, dropFactor);
    }

    /** A pickup dropped where the last unit of group {@code group} died, when the group is cleared. */
    public record GroupDrop(int group, PickupType pickup) {}

    /**
     * Part G: a pickup dropped where the {@code nth} (from 1) of the {@code parts} of the set piece
     * {@code slug} to be shot off breaks (Level 07: the Brood Carrier's first destroyed bay sac).
     * Parts lost with the vital part, in the unit's death, drop nothing.
     *
     * @param nth from 1; {@link Integer#MAX_VALUE} for the last of them
     * @param parts indexes into the set piece's parts
     */
    public record PartDrop(String slug, List<Integer> parts, int nth, PickupType pickup) {
        public PartDrop {
            parts = List.copyOf(parts);
            if (parts.isEmpty() || nth < 1) {
                throw new IllegalArgumentException(slug + ": a part drop names its parts and which of them drops it");
            }
        }

        /** The {@code nth} for the last of the parts. */
        public static final int LAST = Integer.MAX_VALUE;

        /** Which shot-off part drops it: from 1, the last one for {@link #LAST}. */
        public int dropsAt() {
            return nth == LAST ? parts.size() : nth;
        }
    }

    /**
     * M5 part F: a pickup placed in the level instead of dropped (Level 12's armour patch): it enters at
     * the top edge at {@code t} at {@code x} and drifts and expires like any pickup. M5 part G (design/
     * allies, CDF supply drone; user decision G6 = a): released at the point ({@code x}, {@code y}) in
     * the field (y px from the bottom edge, y up; the drone's arc to it is presentation only), then an
     * ordinary pickup; {@code y} = {@link PlayField#HEIGHT} is the top edge.
     */
    public record PlacedPickup(double t, double x, double y, PickupType pickup) {
        public PlacedPickup {
            if (!(y >= 0 && y <= PlayField.HEIGHT) || !(x >= 0 && x <= PlayField.WIDTH)) {
                throw new IllegalArgumentException("a placed pickup lies on the play field: (" + x + ", " + y + ")");
            }
        }

        /** Entering at the top edge (M5 part F). */
        public PlacedPickup(double t, double x, PickupType pickup) {
            this(t, x, PlayField.HEIGHT, pickup);
        }

        /** M5 part G: whether it is released at a point in the field rather than entering at the top edge. */
        public boolean atPoint() {
            return y < PlayField.HEIGHT;
        }
    }
}
