package vanguard.sim;

/**
 * M10 part C (design/enemies/air/mirror-interceptor, Movement; design/campaign Level 37): a unit holding its {@link
 * EnemySpec.Station station}. It starts as the unit stops at its path's end (its station point): a side station's
 * strafe runs up and down the screen, a rear one's (the unit came up from the bottom edge) across it. While it holds,
 * {@link #strafe} slides it toward the ship's height (or x) at the station's strafe speed, at most half the strafe from
 * the station point, and never to within the station's gap of another station unit on its line (same side, or both
 * rear; the simulation's hull is the hit box). Its hold over, a rear unit leaves up through the nearer side
 * edge ({@link #exit}), a side unit through its own edge (its plan's exit). Each {@link Enemy} owns one; its fields are
 * hashed only while it is active. Allocation-free.
 */
final class StationHold {
    /** M10 part C: up and out to the left (a rear station's way out; one exit, so choosing it allocates nothing). */
    static final Spawn.Exit UP_LEFT = new Spawn.Exit(false, -0.8, 0.6);
    /** M10 part C: up and out to the right. */
    static final Spawn.Exit UP_RIGHT = new Spawn.Exit(false, 0.8, 0.6);

    private boolean active;
    /** Whether it holds a rear station (strafing across), not a side one (strafing up and down). */
    private boolean rear;
    /** The station point's coordinate along its strafe: y for a side station, x for a rear one. */
    private double centre;
    /** Whether it still has to strafe in this step's {@link #strafe} (a pass's scratch, set at its start; not hashed). */
    private boolean pending;

    /** Back to a unit holding no station. */
    void clear() {
        active = false;
        rear = false;
        centre = 0;
        pending = false;
    }

    /** {@code u} stops at its station (its path's end): its strafe runs from where it is now. */
    void begin(Enemy u) {
        active = true;
        rear = u.entry() == WaveSpec.Entry.REAR;
        centre = rear ? u.x : u.y;
    }

    boolean active() {
        return active;
    }

    /** Whether it holds a rear station (strafing across the screen). */
    boolean rear() {
        return rear;
    }

    /**
     * Its way out once its hold is over: a rear station's up through the side edge nearer to {@code x} (a tie: the left);
     * a side station's {@code planned} (back through its edge).
     */
    Spawn.Exit exit(double x, Spawn.Exit planned) {
        if (!rear) {
            return planned;
        }
        return x <= PlayField.WIDTH / 2.0 ? UP_LEFT : UP_RIGHT;
    }

    /**
     * One strafing step of every unit holding a station (not stunned by the EMP Burst), after the moves, toward the ship
     * at (shipX, shipY): each slides along its line at its strafe speed toward the ship's coordinate on it, kept within
     * half its strafe of its station point, and stays put rather than close to within its gap of another station unit
     * on its line (holding or still flying in). The units move in rounds until none can move further, so a pair moving
     * the same way keeps its spacing whatever its order in the pool. Nothing changes in a level without stations.
     */
    static void strafe(Pool<Enemy> enemies, double shipX, double shipY) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy u = enemies.get(i);
            u.stationHold.pending = holding(u) && !u.empStunned();
        }
        boolean moved = true;
        while (moved) {
            moved = false;
            for (int i = 0; i < enemies.size(); i++) {
                Enemy u = enemies.get(i);
                StationHold hold = u.stationHold;
                if (!hold.pending) {
                    continue;
                }
                EnemySpec.Station station = u.spec.station().orElseThrow();
                double at = hold.rear ? u.x : u.y;
                double half = station.strafe() / 2;
                double target = hold.centre + Math.clamp((hold.rear ? shipX : shipY) - hold.centre, -half, half);
                double most = station.strafeSpeed() * SimStep.SECONDS;
                double to = at + Math.clamp(target - at, -most, most);
                if (to == at) {
                    hold.pending = false;
                } else if (!crowds(enemies, u, to)) {
                    if (hold.rear) {
                        u.x = to;
                    } else {
                        u.y = to;
                    }
                    hold.pending = false;
                    moved = true;
                }
            }
        }
    }

    /**
     * Whether {@code u} at {@code to} along its line closes to within its gap of another station unit on that line: the
     * same side edge for a side station, the rear for a rear one; holding or still flying in (a side unit flies in at
     * its station's height, a rear one up its lane).
     */
    private static boolean crowds(Pool<Enemy> enemies, Enemy u, double to) {
        StationHold hold = u.stationHold;
        double gap = u.spec.station().orElseThrow().gap();
        double at = hold.rear ? u.x : u.y;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy other = enemies.get(j);
            if (other == u
                    || other.spec.station().isEmpty()
                    || (other.phase != Enemy.Phase.HOLD && other.phase != Enemy.Phase.ENTER)
                    || (other.entry() == WaveSpec.Entry.REAR) != hold.rear) {
                continue;
            }
            if (!hold.rear && (other.x < PlayField.WIDTH / 2.0) != (u.x < PlayField.WIDTH / 2.0)) {
                // Side stations on the other edge are not on its line.
                continue;
            }
            double along = hold.rear ? other.x : other.y;
            double reach =
                    (hold.rear ? u.box.width() + other.box.width() : u.box.height() + other.box.height()) / 2 + gap;
            // Closing to within the gap (a pair already too close may still move apart).
            if (Math.abs(to - along) < reach && Math.abs(to - along) < Math.abs(at - along)) {
                return true;
            }
        }
        return false;
    }

    /** Its station point's coordinate along its strafe (y at a side, x at the rear). */
    double centre() {
        return centre;
    }

    /** Whether {@code u} holds a station now. */
    private static boolean holding(Enemy u) {
        return u.phase == Enemy.Phase.HOLD && u.stationHold.active;
    }

    /** Hashed only while active. */
    void addTo(StateHash hash) {
        hash.add(rear ? 1 : 0).add(centre);
    }
}
