package vanguard.sim;

import java.util.Arrays;

/**
 * M7 part A: the fitted Deflector Pods (design/player/weapons/deflector-pod), indexed by their mount in
 * the {@link Armament}. Each pod's field is the half-disc of its radius round the ship's centre on its
 * side (left of the centre line for the left pod); an enemy bullet whose centre enters it while the
 * pod holds a charge is caught: the {@link Sortie} frees it, one charge is used and {@link
 * SimEvents.Type#DEFLECTOR_CAUGHT} marks the spot. A bullet on the centre line goes to the pod with
 * more charges left (the left one on a tie). Charges return one at a time at the pod's interval while
 * it is below its cap, also while it catches ({@link SimEvents.Type#DEFLECTOR_CHARGED}). Every attempt
 * starts full. A caught bullet scores nothing, never reaches the shield and does not break the chain.
 *
 * <p>During an overdrive the pod uses its overdrive numbers (default, pending the user's review: the
 * next level's radius and interval, and its cap rises to the next level's charges with the new
 * charges empty); when the overdrive ends the charges above the normal cap are lost.
 */
public final class Deflectors {
    private final Ship ship;
    private final Armament armament;
    private final boolean any;
    /** Per mount: the charges it holds, and the steps since a charge last came back (or since it fell below its cap). */
    private final int[] charges;

    private final int[] recharge;
    private boolean overdrive;

    Deflectors(Ship ship, Armament armament) {
        this.ship = ship;
        this.armament = armament;
        boolean found = false;
        for (int m = 0; m < armament.size(); m++) {
            found |= armament.mount(m).deflects();
        }
        any = found;
        charges = new int[armament.size()];
        recharge = new int[armament.size()];
        reset();
    }

    /** Every pod full. */
    void reset() {
        overdrive = false;
        Arrays.fill(recharge, 0);
        for (int m = 0; m < armament.size(); m++) {
            charges[m] =
                    armament.mount(m).deflects() ? armament.mount(m).deflector().charges() : 0;
        }
    }

    /**
     * One step: a pod below its cap gets a charge back once its interval has passed; above its cap (an
     * overdrive ended) it drops to it. {@code overdrive}: whether an overdrive runs.
     */
    void update(boolean overdrive, SimEvents events) {
        if (!any) {
            return;
        }
        this.overdrive = overdrive;
        for (int m = 0; m < armament.size(); m++) {
            if (!armament.mount(m).deflects()) {
                continue;
            }
            DeflectorSpec spec = spec(m);
            if (charges[m] >= spec.charges()) {
                charges[m] = spec.charges();
                recharge[m] = 0;
            } else if (++recharge[m] >= spec.rechargeTicks()) {
                recharge[m] = 0;
                charges[m]++;
                events.add(SimEvents.Type.DEFLECTOR_CHARGED, ship.x() + side(m) * spec.radius() / 2, ship.y(), m);
            }
        }
    }

    /**
     * Whether a pod catches an enemy bullet at (x, y): it uses a charge and marks the spot. The caller
     * frees the bullet.
     */
    boolean catches(double x, double y, SimEvents events) {
        if (!any) {
            return false;
        }
        double dx = x - ship.x();
        double dy = y - ship.y();
        double distance = dx * dx + dy * dy;
        int best = -1;
        for (int m = 0; m < armament.size(); m++) {
            if (!armament.mount(m).deflects() || charges[m] == 0) {
                continue;
            }
            double radius = spec(m).radius();
            if (distance > radius * radius || dx * side(m) < 0) {
                continue;
            }
            if (best < 0 || charges[m] > charges[best] || (charges[m] == charges[best] && side(m) < 0)) {
                best = m;
            }
        }
        if (best < 0) {
            return false;
        }
        charges[best]--;
        events.add(SimEvents.Type.DEFLECTOR_CAUGHT, x, y, best);
        return true;
    }

    /** Whether any Deflector Pod is fitted. */
    public boolean any() {
        return any;
    }

    /** Whether mount {@code m} is a Deflector Pod. */
    public boolean pod(int m) {
        return armament.mount(m).deflects();
    }

    /** Mount {@code m}'s numbers now (its overdrive's during an overdrive). */
    public DeflectorSpec spec(int m) {
        Armament.Mount mount = armament.mount(m);
        return overdrive ? mount.deflectorOverdrive() : mount.deflector();
    }

    /** The side mount {@code m}'s field covers: -1 left of the ship's centre line, 1 right. */
    public int side(int m) {
        return armament.mount(m).slot() == Armament.Slot.LEFT_WING ? -1 : 1;
    }

    /** The charges mount {@code m} holds; its field is drawn only while it holds one. */
    public int charges(int m) {
        return charges[m];
    }

    /** How far mount {@code m}'s next charge has come back, 0 to 1 (0 when it is full). */
    public double recharged(int m) {
        return (double) recharge[m] / spec(m).rechargeTicks();
    }

    /** Hashed only when a pod is fitted, so the hashes of other sorties stay as they were. */
    void addTo(StateHash hash) {
        if (!any) {
            return;
        }
        hash.add(overdrive ? 1 : 0);
        for (int m = 0; m < armament.size(); m++) {
            if (armament.mount(m).deflects()) {
                hash.add(charges[m]).add(recharge[m]);
            }
        }
    }
}
