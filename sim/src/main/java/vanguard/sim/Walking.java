package vanguard.sim;

/**
 * One walker's walk over the ground (design/enemies/ground/scuttler; see {@link EnemySpec.Walker}): its authored
 * path, its walk cycle, its frontal armour and rear plate, its spit and its wave's staggered volleys (M5 part B,
 * design/enemies/ground/creeper). Each {@link Enemy} owns one, idle ({@link #active()} false) for any unit that is
 * no walker; it walks its unit through the unit's movement fields.
 */
final class Walking {
    /** A walker is at a waypoint this close to it and heads for the next. */
    private static final double ARRIVED = 6;

    private final Enemy unit;

    /** Its path; null for any other unit (the walker's fields are hashed only with one). */
    private WalkPath walkPath;
    /** The walk path's point it heads for; past the last it walks straight on. */
    private int waypoint;
    /** How far it walked over the ground, which drives its walk cycle. */
    private double walked;

    private int spitTicks;
    /**
     * M5 part B: its wave's volley clock (a staggered walker wave) and its place in the wave (from 0, in entry
     * order); -1 and 0 without one.
     */
    private int volleyGroup = -1;

    private int volleyUnit;

    Walking(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit that is no walker. */
    void clear() {
        walkPath = null;
        waypoint = 0;
        walked = 0;
        spitTicks = 0;
        volleyGroup = -1;
        volleyUnit = 0;
    }

    /** It starts on {@code path}, heading for its second point; its first spit comes half an interval in. */
    void start(WalkPath path, EnemySpec.Walker walker) {
        walkPath = path;
        waypoint = 1;
        spitTicks =
                walker.spit().isPresent() ? SimStep.ticks(walker.spit().get().intervalSeconds() / 2) : 0;
    }

    /** Whether it is a walker on its path. */
    boolean active() {
        return walkPath != null;
    }

    /** Its path; null for a unit that is no walker. */
    WalkPath path() {
        return walkPath;
    }

    /**
     * One step over the ground at {@code walkSpeed} px/s (M7 part D: a roll goes at the roll's speed): its facing
     * turns toward the path's next point by at most its turn rate, and it walks along its facing; a point it
     * reached, or one it can no longer turn into (behind it, inside its turning circle), is passed. Past the last
     * point it walks straight on.
     */
    void step(double walkSpeed) {
        Enemy u = unit;
        EnemySpec.Walker walker = u.spec.walker().orElseThrow();
        if (waypoint < walkPath.points()) {
            double dx = walkPath.x(waypoint) - u.x;
            double dy = walkPath.y(waypoint) - u.scrolled - u.y;
            double distanceLeft = Math.sqrt(dx * dx + dy * dy);
            double delta = Math.IEEEremainder(Enemy.heading(dx, dy) - u.facing, 2 * StrictMath.PI);
            double turning = 2 * walkSpeed / walker.turnRate();
            if (distanceLeft <= ARRIVED || (distanceLeft < turning && Math.abs(delta) > StrictMath.PI / 2)) {
                waypoint++;
            } else {
                double most = walker.turnRate() * SimStep.SECONDS;
                u.facing = Math.IEEEremainder(u.facing + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
            }
        }
        double step = walkSpeed * SimStep.SECONDS;
        u.x -= Trig.sin(u.facing) * step;
        u.y -= Trig.cos(u.facing) * step;
        walked += step;
    }

    /** Its distance walked over the ground in px (its walk cycle); 0 for other units. */
    double walked() {
        return walked;
    }

    /**
     * Whether a direct shot flying at (shotVx, shotVy) (in its frame) glances off its frontal armour: it arrives from
     * within the armour's arc of its facing.
     */
    boolean glances(double shotVx, double shotVy) {
        Enemy u = unit;
        if (walkPath == null || !(u.spec.walker().orElseThrow().frontArc() > 0)) {
            return false;
        }
        double length = Math.sqrt(shotVx * shotVx + shotVy * shotVy);
        if (length == 0) {
            return false;
        }
        // The facing as a direction (y up): a shot from the front flies against it.
        double against = (shotVx * Trig.sin(u.facing) + shotVy * Trig.cos(u.facing)) / length;
        return against >= Trig.cos(u.spec.walker().orElseThrow().frontArc());
    }

    /**
     * M7 part F (the rear plate): the factor a direct shot flying at (shotVx, shotVy) (in its frame) does to its
     * body: its rear plate's multiplier when the shot arrives within the plate's half arc of its rear (flies along
     * its facing), 1 otherwise and for a unit without a rear plate.
     */
    double rearFactor(double shotVx, double shotVy) {
        Enemy u = unit;
        if (u.spec.rearPlate().isEmpty() || walkPath == null) {
            return 1;
        }
        double length = Math.sqrt(shotVx * shotVx + shotVy * shotVy);
        if (length == 0) {
            return 1;
        }
        EnemySpec.RearPlate plate = u.spec.rearPlate().get();
        // The facing as a direction (y up): a shot from behind flies along it.
        double along = -(shotVx * Trig.sin(u.facing) + shotVy * Trig.cos(u.facing)) / length;
        return along >= Trig.cos(plate.halfArcRadians()) ? plate.multiplier() : 1;
    }

    /**
     * Counts down its spit while it is on the screen; returns whether it spits this step: when it is ready and the
     * ship at (shipX, shipY) (in its frame) is more than its away angle off its facing.
     */
    boolean spit(double shipX, double shipY) {
        Enemy u = unit;
        if (walkPath == null || !PlayField.overlaps(u.x, u.y, u.box)) {
            return false;
        }
        EnemySpec.Walker walker = u.spec.walker().orElseThrow();
        if (walker.spit().isEmpty()) {
            return false;
        }
        if (spitTicks > 0) {
            spitTicks--;
            return false;
        }
        double off =
                Math.abs(Math.IEEEremainder(Enemy.heading(shipX - u.x, shipY - u.y) - u.facing, 2 * StrictMath.PI));
        if (off <= walker.awayRadians()) {
            return false;
        }
        spitTicks = SimStep.ticks(walker.spit().get().intervalSeconds());
        return true;
    }

    /**
     * It joins its wave's volley clock {@code clock} as unit {@code index} (from 0, in entry order); -1: it keeps its
     * own clock (and so does a unit that is no walker).
     */
    void volley(int clock, int index) {
        volleyGroup = walkPath != null ? clock : -1;
        volleyUnit = volleyGroup >= 0 ? index : 0;
    }

    /** Its wave's volley clock; -1 for a unit on its own clock. */
    int volleyGroup() {
        return volleyGroup;
    }

    /**
     * Whether it fires its fan this step in its wave's staggered volleys: the wave's volley clock {@code
     * clocks[volleyGroup]} starts as the first of its units is on the screen; the first volley comes half an
     * interval later and then one every interval, unit i firing i × the stagger after the volley's start. A unit off
     * the screen skips its turn.
     */
    boolean staggeredVolley(int[] clocks) {
        Enemy u = unit;
        if (u.spec.gun().isEmpty() || u.gunQuiet() || !PlayField.overlaps(u.x, u.y, u.box)) {
            return false;
        }
        if (clocks[volleyGroup] == 0) {
            clocks[volleyGroup] = 1;
        }
        EnemyGun gun = u.spec.gun().get();
        int since = clocks[volleyGroup]
                - SimStep.ticks(gun.intervalSeconds() / 2)
                - SimStep.ticks(volleyUnit * u.spec.walker().orElseThrow().staggerSeconds());
        return since >= 0 && since % SimStep.ticks(gun.intervalSeconds()) == 0;
    }

    /** Its state, in the order the unit's hash has always added it; nothing for a unit that is no walker. */
    void addTo(StateHash hash) {
        Enemy u = unit;
        if (walkPath != null) {
            hash.add(u.facing)
                    .add(walked)
                    .add(waypoint)
                    .add(u.scrolled)
                    .add(spitTicks)
                    .add(u.entered ? 1 : 0);
        }
        if (volleyGroup >= 0) {
            hash.add(volleyGroup).add(volleyUnit);
        }
    }
}
