package vanguard.sim;

/**
 * M5 part F: one chaser's chase, latch and shake-off (design/enemies/air/lamprey, user decision F4 = a; see
 * {@link EnemySpec.Chase} and {@link EnemySpec.Latch}). Each {@link Enemy} owns one, idle ({@link
 * Enemy.LatchPhase#NONE}) for any unit without a chase; it steers, places and throws its unit through the unit's
 * movement fields.
 */
final class Chasing {
    private final Enemy unit;

    /** Where it is in its life; {@link Enemy.LatchPhase#NONE} for other units. */
    private Enemy.LatchPhase latchPhase = Enemy.LatchPhase.NONE;
    /** Steps left after its entry before its chase starts. */
    private int chaseWait;
    /** Steps into its chase. */
    private int chaseTicks;
    /** Its heading while it chases, radians clockwise from straight down. */
    private double heading;
    /** The hull spot it latched onto ({@link EnemySpec.Latch#LEFT} ...); -1 before. */
    private int spot = -1;
    /** Steps into its stun. */
    private int stunTicks;

    Chasing(Enemy unit) {
        this.unit = unit;
    }

    /** Back to a unit without a chase. */
    void clear() {
        latchPhase = Enemy.LatchPhase.NONE;
        chaseWait = 0;
        chaseTicks = 0;
        heading = 0;
        spot = -1;
        stunTicks = 0;
    }

    /** A chaser flies in along its path; its chase starts {@code waitTicks} steps after its entry. */
    void enter(int waitTicks) {
        latchPhase = Enemy.LatchPhase.ENTER;
        chaseWait = waitTicks;
    }

    /**
     * A chaser flying in counts its delay down from its entry (its centre inside the play field), then starts its
     * chase along its path's heading.
     */
    void await() {
        Enemy u = unit;
        u.entered |= u.x >= 0 && u.x <= PlayField.WIDTH && u.y >= 0 && u.y <= PlayField.HEIGHT;
        if (!u.entered || chaseWait-- > 0) {
            return;
        }
        u.phase = Enemy.Phase.CHASE;
        latchPhase = Enemy.LatchPhase.CHASE;
        chaseTicks = 0;
        heading = Enemy.heading(u.path.dx(u.segment), u.path.dy(u.segment));
    }

    /**
     * One step of a chase (design/enemies/air/lamprey): its heading turns toward the ship's centre by at most its
     * turn rate and it flies along it at its chase speed; after its time it flies straight on.
     */
    void chase(double shipX, double shipY) {
        Enemy u = unit;
        EnemySpec.Chase chase = u.spec.chase().orElseThrow();
        double delta = Math.IEEEremainder(Enemy.heading(shipX - u.x, shipY - u.y) - heading, 2 * StrictMath.PI);
        double most = chase.turnRate() * SimStep.SECONDS;
        heading = Math.IEEEremainder(heading + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
        u.vx = -Trig.sin(heading) * chase.speed();
        u.vy = -Trig.cos(heading) * chase.speed();
        u.x += u.vx * SimStep.SECONDS;
        u.y += u.vy * SimStep.SECONDS;
        u.facing = heading;
        if (++chaseTicks >= SimStep.ticks(chase.seconds())) {
            u.phase = Enemy.Phase.LEAVE;
            latchPhase = Enemy.LatchPhase.ON;
        }
    }

    /** A latched unit stays on its spot on the hull of the ship at (shipX, shipY) (in its frame). */
    void hold(double shipX, double shipY) {
        Enemy u = unit;
        EnemySpec.Spot at = u.spec.latch().orElseThrow().spots().get(spot);
        u.x = shipX + at.dx();
        u.y = shipY + u.frame.vy(at.dy());
    }

    /**
     * It latches onto the ship at {@code ship}'s centre plus its {@code at} spot's offset; it moves with the ship
     * from now on.
     */
    void latch(int at, double shipX, double shipY) {
        Enemy u = unit;
        EnemySpec.Spot offset = u.spec.latch().orElseThrow().spots().get(at);
        spot = at;
        u.phase = Enemy.Phase.LATCHED;
        latchPhase = Enemy.LatchPhase.LATCHED;
        u.vx = u.vy = 0;
        u.x = shipX + offset.dx();
        // M7 part G: the spot on the hull, in its frame.
        u.y = u.frame.y(shipY + offset.dy());
    }

    /**
     * Thrown off the hull (the shake-off): outward from its spot at the latch's throw speed, slowing to a stop over
     * its stun.
     */
    void shakeOff() {
        Enemy u = unit;
        EnemySpec.Latch latch = u.spec.latch().orElseThrow();
        EnemySpec.Spot offset = latch.spots().get(spot);
        double length = Math.sqrt(offset.dx() * offset.dx() + offset.dy() * offset.dy());
        double ux = length > 0 ? offset.dx() / length : 0;
        double uy = length > 0 ? u.frame.vy(offset.dy()) / length : -1;
        u.vx = ux * latch.throwSpeed();
        u.vy = uy * latch.throwSpeed();
        u.facing = Enemy.heading(ux, uy);
        u.phase = Enemy.Phase.STUNNED;
        latchPhase = Enemy.LatchPhase.STUNNED;
        stunTicks = 0;
        // M7 part A: thrown off, it takes the shake-off's own stun instead of the EMP Burst's.
        u.empStun = 0;
    }

    /** One step of its stun; at its end it flees at its speed to the nearer side edge. */
    void stun() {
        Enemy u = unit;
        EnemySpec.Latch latch = u.spec.latch().orElseThrow();
        int total = Math.max(1, SimStep.ticks(latch.stunSeconds()));
        double left = 1 - (double) ++stunTicks / total;
        u.x += u.vx * left * SimStep.SECONDS;
        u.y += u.vy * left * SimStep.SECONDS;
        if (stunTicks >= total) {
            u.phase = Enemy.Phase.LEAVE;
            latchPhase = Enemy.LatchPhase.FLEE;
            u.vx = (u.x <= PlayField.WIDTH / 2.0 ? -1 : 1)
                    * u.spec.chase().orElseThrow().speed();
            u.vy = 0;
        }
    }

    /** Where it is in its life. */
    Enemy.LatchPhase phase() {
        return latchPhase;
    }

    /**
     * Whether it may latch onto the ship when it touches it: a chaser flying in, chasing or flying on that never
     * latched (stunned or fleeing it does not even collide with the ship).
     */
    boolean latchable() {
        return latchPhase == Enemy.LatchPhase.ENTER
                || latchPhase == Enemy.LatchPhase.CHASE
                || latchPhase == Enemy.LatchPhase.ON;
    }

    /** The hull spot it latched onto; -1 before it latched. */
    int spot() {
        return spot;
    }

    /** How far into its stun it is, 0 to 1 (1 outside a stun, as {@code stunned} says). */
    double stunProgress(boolean stunned) {
        if (!stunned) {
            return 1;
        }
        return Math.min(
                1,
                (double) stunTicks
                        / Math.max(
                                1, SimStep.ticks(unit.spec.latch().orElseThrow().stunSeconds())));
    }

    /** Its state, in the order the unit's hash has always added it. */
    void addTo(StateHash hash) {
        hash.add(latchPhase.ordinal())
                .add(chaseWait)
                .add(chaseTicks)
                .add(heading)
                .add(spot)
                .add(stunTicks);
    }
}
