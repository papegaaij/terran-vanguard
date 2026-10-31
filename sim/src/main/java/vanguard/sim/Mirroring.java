package vanguard.sim;

/**
 * M10 part A (design/enemies, Reflect; design/enemies/air/mirror-interceptor): one unit's {@link EnemySpec.Mirror}
 * state. Its facing is its own (radians clockwise from straight down the screen, as {@link Enemy#facing()}): it turns at
 * the mirror's turn rate toward the ship while the unit holds its station and toward its flight heading otherwise
 * (holding still through a burst), and the arc and the bounce use it; the sprite follows it. A beam heats it (cooling
 * after a pause) until it breaks; a {@code shield-breaker} hit or the EMP Burst shatters it. A broken or shattered
 * mirror is down for its down time, then regrows (still down) over its regrow time. Its bounce gap spaces the bounced
 * bullets. The timers run on the real steps (an EMP stun's included), the turn on the unit's own clock. Allocates
 * nothing; hashed only for a mirrored unit.
 */
public final class Mirroring {
    private double facing;
    private boolean turned;
    private double heat;
    private int coolWait;
    private int down;
    private int regrow;
    private int gap;
    /** The velocity {@link #reflect} worked out last (scratch, not state). */
    private double reflectedVx;

    private double reflectedVy;

    /** A unit just (re)started: the mirror whole and cold, facing its first flight. */
    void clear() {
        facing = 0;
        turned = false;
        heat = 0;
        coolWait = 0;
        down = 0;
        regrow = 0;
        gap = 0;
    }

    /**
     * One real step of its timers: the bounce gap, the cooling (its share of the heat a second once the beam has been
     * off its cool delay), the down time ({@link SimEvents.Type#MIRROR_REGROWING} as it ends) and the regrowth.
     */
    void tick(EnemySpec.Mirror spec, Enemy unit) {
        if (gap > 0) {
            gap--;
        }
        if (heat > 0) {
            if (coolWait >= SimStep.ticks(spec.coolDelaySeconds())) {
                heat = Math.max(0, heat - spec.coolShare() * spec.heat() * SimStep.SECONDS);
            } else {
                coolWait++;
            }
        }
        if (down > 0 && --down == 0) {
            regrow = SimStep.ticks(spec.regrowSeconds());
            unit.events.add(SimEvents.Type.MIRROR_REGROWING, unit.x(), unit.y(), unit.kind());
        } else if (regrow > 0) {
            regrow--;
        }
    }

    /**
     * One step of its turn on the unit's own clock: toward the ship at (shipX, shipY) while the unit holds its station,
     * toward its flight heading otherwise, by at most its turn rate; it holds still while a burst is under way. Its
     * first step faces its target at once.
     */
    void turn(EnemySpec.Mirror spec, Enemy unit, double shipX, double shipY) {
        double target =
                unit.phase == Enemy.Phase.HOLD ? Enemy.heading(shipX - unit.x(), shipY - unit.y()) : unit.facing();
        if (!turned) {
            facing = target;
            turned = true;
            return;
        }
        if (unit.burstLeft > 0) {
            return;
        }
        double most = StrictMath.toRadians(spec.turnDegreesPerSecond()) * SimStep.SECONDS;
        double delta = Math.IEEEremainder(target - facing, 2 * StrictMath.PI);
        facing = Math.IEEEremainder(facing + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
    }

    /** Whether it stands (not broken, shattered or regrowing): only then does it meet shots. */
    public boolean standing() {
        return down == 0 && regrow == 0;
    }

    /**
     * Whether a standing mirror meets a shot flying at (vx, vy) px/s (y up): one arriving within its arc of its facing.
     * The flanks and the rear stay open; a still shot never meets it.
     */
    boolean meets(EnemySpec.Mirror spec, double vx, double vy) {
        if (!standing()) {
            return false;
        }
        double length = Math.sqrt(vx * vx + vy * vy);
        if (length == 0) {
            return false;
        }
        // The shot comes from -v; the plate faces (-sin, -cos) of its facing.
        double against = (vx * Trig.sin(facing) + vy * Trig.cos(facing)) / length;
        return against >= Trig.cos(spec.halfArcRadians());
    }

    /** Whether its gun may fire at the ship at (shipX, shipY): its facing within its fire angle of the bearing. */
    boolean aimed(EnemySpec.Mirror spec, Enemy unit, double shipX, double shipY) {
        double bearing = Enemy.heading(shipX - unit.x(), shipY - unit.y());
        return Math.abs(Math.IEEEremainder(bearing - facing, 2 * StrictMath.PI))
                <= StrictMath.toRadians(spec.fireWithinDegrees());
    }

    /** Whether a bounce is allowed now (its gap since the last one has passed). */
    boolean bounceReady() {
        return gap == 0;
    }

    /**
     * A shot flying at (vx, vy) bounces: its velocity mirrored about the facing (a shot straight into it comes straight
     * back down its own line) at {@code speed} px/s, read with {@link #reflectedVx()}; the gap starts.
     */
    void reflect(EnemySpec.Mirror spec, double vx, double vy) {
        double nx = -Trig.sin(facing);
        double ny = -Trig.cos(facing);
        double dot = vx * nx + vy * ny;
        double rx = vx - 2 * dot * nx;
        double ry = vy - 2 * dot * ny;
        double length = Math.sqrt(rx * rx + ry * ry);
        if (length == 0) {
            rx = nx;
            ry = ny;
            length = 1;
        }
        reflectedVx = rx / length * spec.bulletSpeed();
        reflectedVy = ry / length * spec.bulletSpeed();
        gap = SimStep.ticks(spec.gapSeconds());
    }

    double reflectedVx() {
        return reflectedVx;
    }

    double reflectedVy() {
        return reflectedVy;
    }

    /**
     * A beam's touch heats it by {@code amount} (no hull damage); at its heat it breaks ({@link
     * SimEvents.Type#MIRROR_BROKEN} value 0). Returns whether it broke.
     */
    boolean heat(EnemySpec.Mirror spec, Enemy unit, double amount) {
        if (!standing()) {
            return false;
        }
        heat += amount;
        coolWait = 0;
        if (heat < spec.heat()) {
            return false;
        }
        knockDown(spec, unit, 0);
        return true;
    }

    /**
     * A {@code shield-breaker} hit or the EMP Burst shatters a standing mirror at once ({@link
     * SimEvents.Type#MIRROR_BROKEN} value 1); one already down stays as it is.
     */
    void shatter(EnemySpec.Mirror spec, Enemy unit) {
        if (standing()) {
            knockDown(spec, unit, 1);
        }
    }

    private void knockDown(EnemySpec.Mirror spec, Enemy unit, int how) {
        heat = 0;
        coolWait = 0;
        down = Math.max(1, SimStep.ticks(spec.downSeconds()));
        regrow = 0;
        unit.events.add(SimEvents.Type.MIRROR_BROKEN, unit.x(), unit.y(), how);
    }

    /** Its facing, radians clockwise from straight down the screen (the sprite's heading). */
    public double facing() {
        return facing;
    }

    /** Its heat, in the unit's heat units (the look: dull red, orange, then white; cracks from 70 % of the heat). */
    public double heat() {
        return heat;
    }

    /** Whether it is broken or shattered (down, before its regrowth). */
    public boolean down() {
        return down > 0;
    }

    /** Steps left of its regrowth (the silver shimmer); 0 when it stands or is down. */
    public int regrowTicks() {
        return regrow;
    }

    void addTo(StateHash hash) {
        hash.add(facing)
                .add(turned ? 1 : 0)
                .add(heat)
                .add(coolWait)
                .add(down)
                .add(regrow)
                .add(gap);
    }
}
