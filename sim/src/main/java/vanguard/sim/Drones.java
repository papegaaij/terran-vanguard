package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * M7 part A: the light drones of the fitted drone bays (design/player/wingmen, Light drone), one per
 * bay, indexed by the bay's mount in the {@link Armament}. A drone orbits the ship's centre clockwise
 * at its orbit radius, one turn per period; a left bay's starts at 9 o'clock, a right bay's at 3
 * o'clock, and the orbit centre follows the ship at once. While it is whole it fires its bay's gun when
 * the player fires (aimed at the convergence point ahead of the ship's centre, by {@link PlayerFire}),
 * and the {@link Sortie} lets it absorb enemy bullets (one hit each), lasers (their laser cost, once
 * per touch) and {@code air} contacts (their contact cost). After its level's hits it breaks; the bay
 * rebuilds it in the rebuild time, then it fades in, moving from its bay to its orbit, and is whole
 * again. Every attempt (and a boss retry) starts with whole drones; when the ship is wrecked they break
 * with it. During an overdrive the drone's overdrive numbers count (its hits to break, its rebuild).
 *
 * <p>M7 part G (design/player/wingmen, Rear-guard drone): a bay whose drone has the {@link DroneSpec.Station#TRAIL}
 * station flies it behind the ship instead ({@code behind} px below the ship's centre, {@code side} px to its bay's
 * side, whatever the scroll's direction), following with its lag so it swings out in a turn, and fires straight back;
 * blocking, contacts, breaking and the rebuild are the light drone's.
 */
public final class Drones {
    /** How many air enemies a drone can touch at once; more contacts are counted as new each step. */
    private static final int CONTACTS = 8;

    private final Ship ship;
    private final Armament armament;
    private final boolean any;
    private final Hull[] hulls;
    /** Per mount: hits taken in this life, steps of rebuild left (0: not broken), steps of fade-in left. */
    private final int[] taken;

    private final int[] rebuilding;
    private final int[] fading;
    /** Per mount: whether a laser touched it in the last step (a lasting touch costs once). */
    private final boolean[] lasered;
    /** Per mount: the air enemies (serials) that touched it in the last step, and in this one. */
    private final int[][] contacts;

    private final int[] contactCounts;
    private final int[][] touching;
    private final int[] touchingCounts;
    /** Steps since the attempt started: the orbit's clock. */
    private int orbitTicks;
    /**
     * M7 part G: per mount, a trailing drone's place on the play field (it follows its station with its lag) and
     * whether it has one yet (placed at its station on the first step of an attempt).
     */
    private final double[] trailX;

    private final double[] trailY;
    private final boolean[] placed;
    /** M7 part G: whether any bay's drone trails (only then are the trail places hashed). */
    private final boolean trails;

    private boolean overdrive;

    Drones(Ship ship, Armament armament) {
        this.ship = ship;
        this.armament = armament;
        int size = armament.size();
        hulls = new Hull[size];
        boolean found = false;
        for (int m = 0; m < size; m++) {
            Armament.Mount mount = armament.mount(m);
            if (mount.bay()) {
                found = true;
                // A one-box hull for the beams' segment test (allocated once).
                hulls[m] = new Hull(List.of(new Hull.Part(0, 0, mount.drone().box())));
            }
        }
        any = found;
        boolean trailing = false;
        for (int m = 0; m < size; m++) {
            trailing |= armament.mount(m).bay() && armament.mount(m).drone().station() == DroneSpec.Station.TRAIL;
        }
        trails = trailing;
        trailX = new double[size];
        trailY = new double[size];
        placed = new boolean[size];
        taken = new int[size];
        rebuilding = new int[size];
        fading = new int[size];
        lasered = new boolean[size];
        contacts = new int[size][CONTACTS];
        contactCounts = new int[size];
        touching = new int[size][CONTACTS];
        touchingCounts = new int[size];
    }

    /** Every drone whole at its starting angle. */
    void reset() {
        Arrays.fill(taken, 0);
        Arrays.fill(rebuilding, 0);
        Arrays.fill(fading, 0);
        Arrays.fill(lasered, false);
        Arrays.fill(contactCounts, 0);
        Arrays.fill(touchingCounts, 0);
        Arrays.fill(placed, false);
        Arrays.fill(trailX, 0);
        Arrays.fill(trailY, 0);
        orbitTicks = 0;
        overdrive = false;
    }

    /**
     * One step: the orbit turns on, a broken drone's rebuild and a rebuilt one's fade-in run down
     * ({@link SimEvents.Type#DRONE_REBUILT} as the fade-in starts). {@code overdrive}: whether an
     * overdrive runs, which picks the drone's numbers.
     */
    void update(boolean overdrive, SimEvents events) {
        if (!any) {
            return;
        }
        this.overdrive = overdrive;
        orbitTicks++;
        for (int m = 0; m < armament.size(); m++) {
            if (!armament.mount(m).bay()) {
                continue;
            }
            if (station(m) == DroneSpec.Station.TRAIL) {
                follow(m);
            }
            if (rebuilding[m] > 0) {
                if (--rebuilding[m] == 0) {
                    fading[m] = SimStep.ticks(spec(m).fadeInSeconds());
                    events.add(SimEvents.Type.DRONE_REBUILT, x(m), y(m), m);
                }
            } else if (fading[m] > 0) {
                fading[m]--;
            }
        }
    }

    /**
     * M7 part G: a trailing drone's step toward its station behind the ship (its bay's side, {@code behind} px below
     * the ship's centre): a first-order lag of {@code lagSeconds} (0: on it), so it swings out as the ship turns.
     */
    private void follow(int m) {
        DroneSpec drone = spec(m);
        double toX = ship.x() + sideways(m);
        double toY = ship.y() - drone.behind();
        if (!placed[m] || drone.lagSeconds() <= 0) {
            trailX[m] = toX;
            trailY[m] = toY;
            placed[m] = true;
            return;
        }
        double share = Math.min(1, SimStep.SECONDS / drone.lagSeconds());
        trailX[m] += (toX - trailX[m]) * share;
        trailY[m] += (toY - trailY[m]) * share;
    }

    /** M7 part G: a trail station's offset right of the ship's centre: its bay's side. */
    private double sideways(int m) {
        double side = spec(m).side();
        return armament.mount(m).slot() == Armament.Slot.LEFT_WING ? -side : side;
    }

    /** M7 part G: where mount {@code m}'s drone flies: its orbit or its trail behind the ship. */
    public DroneSpec.Station station(int m) {
        return armament.mount(m).drone().station();
    }

    /** Whether any drone bay is fitted. */
    public boolean any() {
        return any;
    }

    /** Whether mount {@code m} is a drone bay. */
    public boolean bay(int m) {
        return armament.mount(m).bay();
    }

    /** The numbers of mount {@code m}'s drone now (its overdrive's during an overdrive). */
    public DroneSpec spec(int m) {
        Armament.Mount mount = armament.mount(m);
        return overdrive ? mount.droneOverdrive() : mount.drone();
    }

    /** Whether mount {@code m}'s drone flies whole: it fires, absorbs bullets and touches enemies. */
    public boolean whole(int m) {
        return armament.mount(m).bay() && rebuilding[m] == 0 && fading[m] == 0;
    }

    /** Whether mount {@code m}'s drone is broken and its bay rebuilds it (nothing is drawn). */
    public boolean broken(int m) {
        return rebuilding[m] > 0;
    }

    /** Whether mount {@code m}'s rebuilt drone fades in (drawn, but it neither fires nor blocks yet). */
    public boolean fadingIn(int m) {
        return rebuilding[m] == 0 && fading[m] > 0;
    }

    /** How far mount {@code m}'s drone has faded in, 0 to 1 (1 when whole, 0 while broken). */
    public double fadeIn(int m) {
        if (rebuilding[m] > 0) {
            return 0;
        }
        int total = SimStep.ticks(spec(m).fadeInSeconds());
        return fading[m] == 0 || total == 0 ? 1 : 1 - (double) fading[m] / total;
    }

    /** The hits mount {@code m}'s drone can still absorb (0 while broken), for the HUD's pips. */
    public int hitsLeft(int m) {
        return rebuilding[m] > 0 ? 0 : Math.max(0, spec(m).hits() - taken[m]);
    }

    /** How far the bay has rebuilt mount {@code m}'s drone, 0 to 1 (1 when it is not broken), for the HUD. */
    public double rebuilt(int m) {
        if (rebuilding[m] == 0) {
            return 1;
        }
        return 1 - (double) rebuilding[m] / Math.max(1, SimStep.ticks(spec(m).rebuildSeconds()));
    }

    /**
     * Mount {@code m}'s drone's orbit angle, radians clockwise from straight up; M7 part G: a trailing drone's is
     * straight back (π).
     */
    public double angle(int m) {
        if (station(m) == DroneSpec.Station.TRAIL) {
            return StrictMath.PI;
        }
        int period = Math.max(1, SimStep.ticks(spec(m).periodSeconds()));
        double start = armament.mount(m).slot() == Armament.Slot.LEFT_WING ? -StrictMath.PI / 2 : StrictMath.PI / 2;
        return start + 2 * StrictMath.PI * (orbitTicks % period) / period;
    }

    /**
     * Mount {@code m}'s drone right of the ship's centre, px: on its orbit, or while it fades in on its
     * way from the bay (the mount's first muzzle) to the orbit.
     */
    public double offsetX(int m) {
        // M7 part G: a trailing drone's station is its place behind the ship, lagging.
        double orbit = station(m) == DroneSpec.Station.TRAIL
                ? (placed[m] ? trailX[m] - ship.x() : sideways(m))
                : Trig.sin(angle(m)) * spec(m).orbit();
        return fadingIn(m) ? bayX(m) + (orbit - bayX(m)) * fadeIn(m) : orbit;
    }

    /** Mount {@code m}'s drone above the ship's centre, px (see {@link #offsetX}). */
    public double offsetY(int m) {
        double orbit = station(m) == DroneSpec.Station.TRAIL
                ? (placed[m] ? trailY[m] - ship.y() : -spec(m).behind())
                : Trig.cos(angle(m)) * spec(m).orbit();
        return fadingIn(m) ? bayY(m) + (orbit - bayY(m)) * fadeIn(m) : orbit;
    }

    private double bayX(int m) {
        return armament.mount(m).weapon().muzzles().getFirst().dx();
    }

    private double bayY(int m) {
        return armament.mount(m).weapon().muzzles().getFirst().dy();
    }

    /** Mount {@code m}'s drone's position on the play field. */
    public double x(int m) {
        return ship.x() + offsetX(m);
    }

    public double y(int m) {
        return ship.y() + offsetY(m);
    }

    /** Mount {@code m}'s drone's hull (its one hit box) for segment tests. */
    Hull hull(int m) {
        return hulls[m];
    }

    /**
     * Mount {@code m}'s drone absorbs {@code cost} hits ({@link SimEvents.Type#DRONE_HIT}); at its
     * level's hits it breaks. Returns whether it broke.
     */
    boolean hit(int m, int cost, SimEvents events) {
        events.add(SimEvents.Type.DRONE_HIT, x(m), y(m), m);
        taken[m] += cost;
        if (taken[m] < spec(m).hits()) {
            return false;
        }
        breakDrone(m, events);
        return true;
    }

    /** Mount {@code m}'s drone breaks ({@link SimEvents.Type#DRONE_BROKEN}, a small spark burst) and its bay starts the rebuild. */
    private void breakDrone(int m, SimEvents events) {
        events.add(SimEvents.Type.DRONE_BROKEN, x(m), y(m), m);
        rebuilding[m] = Math.max(1, SimStep.ticks(spec(m).rebuildSeconds()));
        fading[m] = 0;
        taken[m] = 0;
        lasered[m] = false;
        contactCounts[m] = 0;
        touchingCounts[m] = 0;
    }

    /** The ship is wrecked: every drone that is not broken already breaks with it. */
    void breakAll(SimEvents events) {
        for (int m = 0; m < armament.size(); m++) {
            if (armament.mount(m).bay() && rebuilding[m] == 0) {
                breakDrone(m, events);
            }
        }
    }

    /**
     * Whether a laser touching mount {@code m}'s drone now is a new touch (none touched it in the last
     * step); {@code touching}: whether one touches it.
     */
    boolean laser(int m, boolean touches) {
        boolean fresh = touches && !lasered[m];
        lasered[m] = touches;
        return fresh;
    }

    /** Starts the step's contact bookkeeping for mount {@code m}'s drone. */
    void beginContacts(int m) {
        touchingCounts[m] = 0;
    }

    /**
     * An air enemy ({@code serial}) touches mount {@code m}'s drone: whether it is a new contact (one
     * that did not touch it in the last step), which costs hits; a lasting contact costs once.
     */
    boolean touch(int m, int serial) {
        if (touchingCounts[m] < CONTACTS) {
            touching[m][touchingCounts[m]++] = serial;
        }
        for (int i = 0; i < contactCounts[m]; i++) {
            if (contacts[m][i] == serial) {
                return false;
            }
        }
        return true;
    }

    /** Ends the step's contact bookkeeping for mount {@code m}'s drone. */
    void endContacts(int m) {
        System.arraycopy(touching[m], 0, contacts[m], 0, touchingCounts[m]);
        contactCounts[m] = touchingCounts[m];
    }

    /** Hashed only when a drone bay is fitted, so the hashes of other sorties stay as they were. */
    void addTo(StateHash hash) {
        if (!any) {
            return;
        }
        hash.add(orbitTicks).add(overdrive ? 1 : 0);
        for (int m = 0; m < armament.size(); m++) {
            if (!armament.mount(m).bay()) {
                continue;
            }
            hash.add(taken[m])
                    .add(rebuilding[m])
                    .add(fading[m])
                    .add(lasered[m] ? 1 : 0)
                    .add(contactCounts[m]);
            for (int i = 0; i < contactCounts[m]; i++) {
                hash.add(contacts[m][i]);
            }
            if (trails && station(m) == DroneSpec.Station.TRAIL) {
                // M7 part G: only a trailing drone adds its place, so the light drones' sorties hash as before.
                hash.add(trailX[m]).add(trailY[m]).add(placed[m] ? 1 : 0);
            }
        }
    }
}
