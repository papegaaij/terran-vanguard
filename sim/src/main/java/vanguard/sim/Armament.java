package vanguard.sim;

import java.util.List;

/**
 * The fitted weapons: one {@link Mount} per weapon slot that holds one (design/player, the slot
 * layout), each with its pattern at its upgrade level and its overdrive pattern, one level higher
 * (design/player, in-level pickups). Each mount fires on its own clock.
 */
public record Armament(List<Mount> mounts) {
    public Armament {
        mounts = List.copyOf(mounts);
    }

    /** The weapon slots of the Stormhawk. */
    public enum Slot {
        FRONT,
        REAR,
        LEFT_WING,
        RIGHT_WING
    }

    /**
     * A fitted weapon. M7 part A: a mount is one of three kinds: a gun (only {@code weapon} and {@code
     * overdrive}), a drone bay on a wing ({@link #droneBay}: its drone's gun as {@code weapon} and the
     * drone itself) or a Deflector Pod on a wing ({@link #deflector}: no weapon at all, {@code weapon} and
     * {@code overdrive} are null). Use {@link #fires()}, {@link #bay()} and {@link #deflects()} to tell
     * them apart; code that reads {@code weapon()} must skip a mount that does not fire.
     *
     * @param weapon at its upgrade level; null for a Deflector Pod
     * @param overdrive what it fires during an overdrive: the next level's pattern, or the overdrive
     *     pattern at L5; null for a Deflector Pod
     * @param drone a drone bay's drone at its upgrade level; null for other mounts
     * @param droneOverdrive the drone during an overdrive (the next level's, or the overdrive row at
     *     L5); null for other mounts
     * @param deflector a Deflector Pod's field at its upgrade level; null for other mounts
     * @param deflectorOverdrive the field during an overdrive (the next level's, or the overdrive row
     *     at L5); null for other mounts
     */
    public record Mount(
            Slot slot,
            WeaponSpec weapon,
            WeaponSpec overdrive,
            DroneSpec drone,
            DroneSpec droneOverdrive,
            DeflectorSpec deflector,
            DeflectorSpec deflectorOverdrive) {
        public Mount {
            boolean wing = slot == Slot.LEFT_WING || slot == Slot.RIGHT_WING;
            if ((drone == null) != (droneOverdrive == null)
                    || (deflector == null) != (deflectorOverdrive == null)
                    || (drone != null && deflector != null)) {
                throw new IllegalArgumentException(
                        "a mount is a gun, a drone bay or a deflector, each with its overdrive");
            }
            if ((weapon == null) != (overdrive == null) || (weapon == null) != (deflector != null)) {
                throw new IllegalArgumentException("every mount but a deflector has a weapon and its overdrive");
            }
            if ((drone != null || deflector != null) && !wing) {
                throw new IllegalArgumentException("drone bays and deflectors sit on a wing mount");
            }
        }

        /** A gun: {@code weapon} at its upgrade level, {@code overdrive} during an overdrive. */
        public Mount(Slot slot, WeaponSpec weapon, WeaponSpec overdrive) {
            this(slot, weapon, overdrive, null, null, null, null);
        }

        /**
         * M7 part A: a drone bay (design/player/weapons/light-drone-bay) on wing {@code slot}: its drone
         * flies {@code drone} and fires {@code weapon} (the first muzzle is the bay, where a rebuilt
         * drone fades in; its angle is not used, the drone aims at its convergence point).
         */
        public static Mount droneBay(
                Slot slot, WeaponSpec weapon, WeaponSpec overdrive, DroneSpec drone, DroneSpec droneOverdrive) {
            return new Mount(slot, weapon, overdrive, drone, droneOverdrive, null, null);
        }

        /** M7 part A: a Deflector Pod (design/player/weapons/deflector-pod) on wing {@code slot}; it fires nothing. */
        public static Mount deflector(Slot slot, DeflectorSpec deflector, DeflectorSpec deflectorOverdrive) {
            return new Mount(slot, null, null, null, null, deflector, deflectorOverdrive);
        }

        /** Whether it fires shots (or a beam): every mount but a Deflector Pod. */
        public boolean fires() {
            return weapon != null;
        }

        /** M7 part A: whether it is a drone bay. */
        public boolean bay() {
            return drone != null;
        }

        /** M7 part A: whether it is a Deflector Pod. */
        public boolean deflects() {
            return deflector != null;
        }
    }

    public int size() {
        return mounts.size();
    }

    public Mount mount(int index) {
        return mounts.get(index);
    }
}
