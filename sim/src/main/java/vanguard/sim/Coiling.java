package vanguard.sim;

/**
 * M8 part E: one coiling unit's orbit round the ship (design/enemies/naval/spiral-nautilus; {@link EnemySpec.Coil};
 * m8e-gaps.md §1.3, §5 E7, E8), close to the Dust Devil's {@link VortexCycle}. Its cycle: closed, it spirals in round
 * the ship from the outer to the inner radius; it uncoils ({@link SimEvents.Type#UNCOIL}), opening then open (its
 * spiral's volleys, the first at the ship), and closes ({@link SimEvents.Type#COIL}), breaking outward to the outer
 * radius again. Its first cycle starts its place in its wave times the offset later, and its place picks its sense of
 * orbit (even counter-clockwise, odd clockwise: a pair orbits in opposite directions).
 *
 * <p>The orbit is a pursuit: per step the unit takes its angle round the orbit's centre from where it is, moves it on
 * by its speed (closed) or its open speed over the radius, and flies toward that point at up to twice that speed. The
 * centre is the ship, raised so the circle stays on the play field but never so far that the ship comes within the
 * inner radius of its bottom; in a level with rock (the trench clamp, round 53 A.1) it is clamped across so a circle
 * of the inner radius plus the unit's half size fits the flyable span holding the ship (at the centre's row and the
 * circle's top and bottom rows), its middle when even that does not fit, and the radius is cut to the room the span
 * leaves (never under the inner radius). After its move it is pushed out to the inner radius from the ship (as a
 * vortex keeps away: a clamped circle can run past a ship near a wall) and then clear of the rock. Once its wave's hold is over it leaves at its next closed moment: closed, it
 * breaks off sideways toward the nearer side of its orbit's centre at its speed and is gone off the play field (missed
 * for a kill ratio). Each {@link Enemy} owns one, used only while its stat block has a coil (hashed only then); its
 * {@link Canyon} is set at its spawn. Allocation-free. Not for a flipped scroll (no level reverses with one).
 */
final class Coiling {
    /** {@link #now()}: it began to uncoil in this step. */
    static final int NOW_UNCOILED = 1;
    /** {@link #now()}: it closed in this step. */
    static final int NOW_COILED = 2;
    /** {@link #now()}: its spiral fires a volley in this step (while open). */
    static final int NOW_SPIRAL = 4;

    private EnemySpec.Coil coil;
    private EnemyGun.SpiralSpec spiral;
    /** The rock and the scroll (not hashed: the level's, set at the spawn); null without rock. */
    private Canyon canyon;

    private int inTicks;
    private int openingTicks;
    private int openTicks;
    /** Steps into the cycle; negative while its place's offset still holds it closed at the outer radius. */
    private int cycle;
    /** Whether it opened at least once (only then does a cycle's start close it). */
    private boolean opened;
    /** Its sense of orbit: 1 counter-clockwise (y up), -1 clockwise. */
    private int dir = 1;
    /** Steps left of its wave's hold; it leaves at its next closed moment once this is 0. */
    private int stayTicks;
    /** Whether it left its orbit (sideways toward {@link #leaveDir}). */
    private boolean leaving;

    private int leaveDir;
    /** Its orbit's centre's x in the last step (the side it leaves by). */
    private double centreX;
    /** The spiral's arm angle (radians, 0 = right, y up) and its steps to the next volley. */
    private double spiralAngle;

    private int spiralWait;
    /** What happened in this step, as the {@code NOW_} bits (not hashed: set and read within the step). */
    private int now;

    /** Starts the coil of a unit with {@code holdSeconds} of its wave's hold; it orbits counter-clockwise. */
    void start(EnemySpec.Coil spec, EnemyGun.SpiralSpec spiralSpec, double holdSeconds, double atX) {
        coil = spec;
        spiral = spiralSpec;
        inTicks = Math.max(1, SimStep.ticks(spec.inSeconds()));
        openingTicks = SimStep.ticks(spec.openingSeconds());
        openTicks = Math.max(1, SimStep.ticks(spec.openSeconds()));
        cycle = 0;
        opened = false;
        dir = 1;
        stayTicks = Math.max(0, SimStep.ticks(holdSeconds));
        leaving = false;
        leaveDir = 0;
        centreX = atX;
        spiralAngle = 0;
        spiralWait = 0;
        now = 0;
    }

    /** Back to a unit without a coil. */
    void clear() {
        coil = null;
        spiral = null;
        canyon = null;
        inTicks = openingTicks = openTicks = 0;
        cycle = 0;
        opened = false;
        dir = 1;
        stayTicks = 0;
        leaving = false;
        leaveDir = 0;
        centreX = 0;
        spiralAngle = 0;
        spiralWait = 0;
        now = 0;
    }

    /** Whether it has a coil. */
    boolean active() {
        return coil != null;
    }

    /** The level's rock and scroll it keeps clear of (null: none). */
    void canyon(Canyon rock) {
        canyon = rock;
    }

    /** Its place in its wave (from 0): its cycle starts that many offsets later; an odd place orbits clockwise. */
    void wavePlace(int place) {
        if (coil != null) {
            cycle = -SimStep.ticks(place * coil.offsetSeconds());
            dir = place % 2 == 0 ? 1 : -1;
        }
    }

    /**
     * One step of {@code u}'s coil, the ship at (shipX, shipY): its cycle runs on (its events through the unit's
     * events), then it orbits, or leaves. Returns false once it left the play field after its hold.
     */
    boolean step(Enemy u, double shipX, double shipY) {
        now = 0;
        if (leaving) {
            u.x += leaveDir * u.speed * SimStep.SECONDS;
            return PlayField.overlaps(u.x, u.y, u.box);
        }
        if (stayTicks > 0) {
            stayTicks--;
        }
        if (stayTicks == 0 && cycle < inTicks) {
            // Its hold is over and it is closed: it breaks off toward the nearer side of its orbit's centre.
            leaving = true;
            leaveDir = u.x < centreX ? -1 : 1;
            u.x += leaveDir * u.speed * SimStep.SECONDS;
            return PlayField.overlaps(u.x, u.y, u.box);
        }
        cycle++;
        int cycleTicks = inTicks + openingTicks + openTicks;
        if (cycle >= cycleTicks) {
            cycle = 0;
        }
        if (cycle == 0 && opened) {
            now |= NOW_COILED;
            u.events.add(SimEvents.Type.COIL, u.x, u.y, u.kind);
        } else if (cycle == inTicks) {
            now |= NOW_UNCOILED;
            u.events.add(SimEvents.Type.UNCOIL, u.x, u.y, u.kind);
        }
        orbit(u, shipX, shipY);
        if (cycle == inTicks + openingTicks) {
            opened = true;
            spiralAngle = StrictMath.atan2(shipY - u.y, shipX - u.x);
            spiralWait = 0;
        } else if (phase() == CoilPhase.OPEN && spiral != null) {
            spiralAngle = Math.IEEEremainder(
                    spiralAngle + spiral.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        }
        if (phase() == CoilPhase.OPEN && spiral != null && --spiralWait <= 0) {
            now |= NOW_SPIRAL;
            spiralWait = Math.max(1, SimStep.ticks(spiral.intervalSeconds()));
        }
        return true;
    }

    /** One orbit step toward its point on its circle round the (clamped) centre. */
    private void orbit(Enemy u, double shipX, double shipY) {
        double inner = coil.innerRadius();
        double half = Math.max(u.box.width(), u.box.height()) / 2;
        double radius = radius();
        // Raised so the circle stays on the play field, but never so far that the ship nears its bottom.
        double cy = Math.max(shipY, Math.min(radius + half, shipY + radius - inner));
        cy = Math.min(cy, PlayField.HEIGHT - radius - half);
        double left = 0;
        double right = PlayField.WIDTH;
        if (canyon != null) {
            // The trench clamp: the narrowest span holding the ship at the circle's top, centre and bottom rows.
            for (int k = -1; k <= 1; k++) {
                double y = cy + k * radius;
                double l = canyon.terrain.spanLeft(canyon.scroll(), y, shipX, false);
                double r = canyon.terrain.spanRight(canyon.scroll(), y, shipX, false);
                if (!Double.isNaN(l) && l > left) {
                    left = l;
                }
                if (!Double.isNaN(r) && r < right) {
                    right = r;
                }
            }
        }
        double lo = left + inner + half;
        double hi = right - inner - half;
        double cx = lo <= hi ? Math.clamp(shipX, lo, hi) : (left + right) / 2;
        if (canyon != null) {
            radius = Math.max(inner, Math.min(radius, Math.min(cx - left, right - cx) - half));
        }
        centreX = cx;
        double speed = phase() == CoilPhase.CLOSED ? u.speed : coil.openSpeed();
        double dx = u.x - cx;
        double dy = u.y - cy;
        double angle = dx == 0 && dy == 0 ? StrictMath.PI / 2 : StrictMath.atan2(dy, dx);
        angle += dir * speed * SimStep.SECONDS / radius;
        double toX = cx + radius * Trig.cos(angle);
        double toY = cy + radius * Trig.sin(angle);
        double mx = toX - u.x;
        double my = toY - u.y;
        double step = 2 * speed * SimStep.SECONDS;
        double distance = Math.sqrt(mx * mx + my * my);
        if (distance <= step) {
            u.x = toX;
            u.y = toY;
        } else {
            u.x += mx * step / distance;
            u.y += my * step / distance;
        }
        // Never closer than the inner radius to the ship (a clamped circle can run past it): pushed out from its
        // centre.
        double sx = u.x - shipX;
        double sy = u.y - shipY;
        double d2 = sx * sx + sy * sy;
        if (d2 < inner * inner) {
            double d = Math.sqrt(d2);
            if (d == 0) {
                u.y = shipY + inner;
            } else {
                u.x = shipX + sx * inner / d;
                u.y = shipY + sy * inner / d;
            }
        }
        if (canyon != null) {
            // Rock wins over the ship's distance: kept clear of the walls.
            u.x = canyon.flyableX(u.y, u.x, half);
        }
    }

    /** The radius of its spiral now (before the trench clamp): the outer closing to the inner while closed. */
    private double radius() {
        if (cycle < 0) {
            return coil.outerRadius();
        }
        if (cycle < inTicks) {
            return coil.outerRadius() + (coil.innerRadius() - coil.outerRadius()) * cycle / inTicks;
        }
        return coil.innerRadius();
    }

    /** Where it is in its cycle now. */
    CoilPhase phase() {
        if (coil == null) {
            return CoilPhase.NONE;
        }
        if (leaving) {
            return CoilPhase.LEAVING;
        }
        if (cycle < inTicks) {
            return CoilPhase.CLOSED;
        }
        return cycle < inTicks + openingTicks ? CoilPhase.OPENING : CoilPhase.OPEN;
    }

    /** How far into its current phase it is, 0 to 1 (0 while leaving or held by its offset). */
    double progress() {
        return switch (phase()) {
            case NONE, LEAVING -> 0;
            case CLOSED -> cycle < 0 ? 0 : (double) cycle / inTicks;
            case OPENING -> (double) (cycle - inTicks) / Math.max(1, openingTicks);
            case OPEN -> (double) (cycle - inTicks - openingTicks) / openTicks;
        };
    }

    /** Whether its crown glows now: the last {@link EnemySpec.Coil#tellSeconds()} of its opening. */
    boolean tell() {
        return phase() == CoilPhase.OPENING && inTicks + openingTicks - cycle <= SimStep.ticks(coil.tellSeconds());
    }

    /** Whether its shell is closed (it is not open): armoured but against blasts and {@code anti-sub} fire. */
    boolean closed() {
        return coil != null && phase() != CoilPhase.OPEN;
    }

    /** Whether it orbits clockwise. */
    boolean clockwise() {
        return dir < 0;
    }

    /** What happened in this step, as the {@code NOW_} bits. */
    int now() {
        return now;
    }

    /** The spiral's arm angle now (radians, 0 = right, y up). */
    double spiralAngle() {
        return spiralAngle;
    }

    void addTo(StateHash hash) {
        hash.add(cycle)
                .add(opened ? 1 : 0)
                .add(dir)
                .add(stayTicks)
                .add(leaving ? 1 : 0)
                .add(leaveDir)
                .add(centreX)
                .add(spiralAngle)
                .add(spiralWait);
    }
}
