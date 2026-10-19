package vanguard.sim;

/**
 * M7 part E: one lane strafer's flight and its gun's pass trigger (design/enemies/air/ghost-drone; see {@link
 * EnemySpec.LaneStrafe}). A unit entering at the top drops to its lane, banks a quarter turn and strafes across
 * toward the far side; a unit entering from a side edge strafes in at its lane. With the second pass (hard) a
 * top entry turns at the far edge and strafes back once, on the lane {@link EnemySpec.LaneStrafe#RETURN_RADIUS}
 * × 2 lower. On each straight pass, the first time its centre is within its gun's {@link EnemyGun#passWithinX()}
 * of the ship's x on the play field, its eye glints and its tell later its burst fires. Each {@link Enemy} owns
 * one, used only while its stat block has a lane strafe (hashed only then); the unit takes its place from {@link
 * #x()} and {@link #y()}.
 */
final class LaneStrafing {
    /** {@link #fire}: its eye glints in this step (the tell). */
    static final int NOW_GLINT = 1;
    /** {@link #fire}: a shot of its burst goes in this step. */
    static final int NOW_SHOT = 2;

    private static final double QUARTER = StrictMath.PI / 2;

    private EnemySpec.LaneStrafe spec;
    private Enemy.StrafePhase phase = Enemy.StrafePhase.DESCENT;
    /** Its strafe's direction: 1 to the right, -1 to the left. */
    private int dir = 1;
    /** Whether it entered at the top (only then may it fly a second pass). */
    private boolean fromTop;
    /** Whether it flies a straight pass now (its pass trigger may fire). */
    private boolean straight;

    private double x;
    private double y;
    /** Its lane's height (y up). */
    private double laneY;
    /** The centre of its bank or of its half turn, and how far it turned round it (radians). */
    private double centreX;

    private double centreY;
    private double turned;

    /** Whether its pass trigger is armed on this pass (once per pass). */
    private boolean armed;
    /** Steps left of its tell; 0 when it does not glint. */
    private int tellTicks;
    /** Shots of its burst still to come after the last, and the steps to the next. */
    private int burstLeft;

    private int burstTicks;
    /** Shots of the running burst fired so far. */
    private int shots;
    /** Its burst's line when its shots are not re-aimed: radians clockwise from straight down. */
    private double lockedAim;

    /** Back to a unit without a lane strafe. */
    void clear() {
        spec = null;
        phase = Enemy.StrafePhase.DESCENT;
        dir = 1;
        fromTop = false;
        straight = false;
        x = y = laneY = 0;
        centreX = centreY = turned = 0;
        armed = false;
        tellTicks = 0;
        burstLeft = 0;
        burstTicks = 0;
        shots = 0;
        lockedAim = 0;
    }

    /**
     * Starts the flight of a unit appearing at (startX, startY) whose planned path ends at (endX, endY): a start
     * beside the play field is a side entry strafing in at the end's height, any other a top entry dropping to
     * the lane at the end's height; it strafes toward the end's side of the start.
     */
    void start(EnemySpec.LaneStrafe strafe, double startX, double startY, double endX, double endY) {
        clear();
        spec = strafe;
        x = startX;
        y = startY;
        laneY = endY;
        dir = endX >= startX ? 1 : -1;
        fromTop = startX >= 0 && startX <= PlayField.WIDTH;
        if (fromTop) {
            phase = Enemy.StrafePhase.DESCENT;
        } else {
            phase = Enemy.StrafePhase.STRAFE;
            straight = true;
            armed = true;
        }
    }

    /** One step of its flight. */
    void step() {
        double seconds = SimStep.SECONDS;
        switch (phase) {
            case DESCENT -> {
                y -= spec.descentSpeed() * seconds;
                double r = EnemySpec.LaneStrafe.BANK_RADIUS;
                if (y <= laneY + r) {
                    y = laneY + r;
                    phase = Enemy.StrafePhase.BANK;
                    centreX = x + dir * r;
                    centreY = laneY + r;
                    turned = 0;
                }
            }
            case BANK -> {
                double r = EnemySpec.LaneStrafe.BANK_RADIUS;
                double share = Math.min(1, turned / QUARTER);
                double speed = spec.descentSpeed() + (spec.strafeSpeed() - spec.descentSpeed()) * share;
                turned += speed * seconds / r;
                if (turned >= QUARTER) {
                    phase = Enemy.StrafePhase.STRAFE;
                    x = centreX + dir * (turned - QUARTER) * r;
                    y = laneY;
                    straight = true;
                    armed = true;
                } else {
                    double angle = (dir > 0 ? StrictMath.PI : 0) + dir * turned;
                    x = centreX + r * Trig.cos(angle);
                    y = centreY + r * Trig.sin(angle);
                }
            }
            case STRAFE -> {
                x += dir * spec.strafeSpeed() * seconds;
                double r = EnemySpec.LaneStrafe.RETURN_RADIUS;
                if (spec.secondPass() && fromTop && (dir > 0 ? x >= PlayField.WIDTH - r : x <= r)) {
                    // Hard: it turns at the far edge, a half turn toward the bottom, and strafes back once.
                    phase = Enemy.StrafePhase.RETURN;
                    straight = false;
                    centreX = x;
                    centreY = laneY - r;
                    turned = 0;
                }
            }
            case RETURN -> {
                if (straight) {
                    x += dir * spec.strafeSpeed() * seconds;
                    return;
                }
                double r = EnemySpec.LaneStrafe.RETURN_RADIUS;
                turned += spec.strafeSpeed() * seconds / r;
                if (turned >= StrictMath.PI) {
                    dir = -dir;
                    laneY -= 2 * r;
                    x = centreX + dir * (turned - StrictMath.PI) * r;
                    y = laneY;
                    straight = true;
                    armed = true;
                } else {
                    double angle = QUARTER - dir * turned;
                    x = centreX + r * Trig.cos(angle);
                    y = centreY + r * Trig.sin(angle);
                }
            }
        }
    }

    /** M7 part A: its flight drifts with a stunned unit by (dx, dy). */
    void drift(double dx, double dy) {
        x += dx;
        y += dy;
        laneY += dy;
        centreX += dx;
        centreY += dy;
    }

    /**
     * One step of its gun's pass trigger ({@code gun} has one) with the ship at {@code shipX}: its tell counts
     * down to the burst's first shot, then the shots go {@code gun}'s burst gap apart; on a straight pass, armed and
     * {@code onField}, within the trigger's distance of the ship's x it glints (or fires at once without a tell).
     * Returns what happens in this step as the {@code NOW_} bits.
     */
    int fire(EnemyGun gun, double shipX, boolean onField) {
        if (tellTicks > 0) {
            if (--tellTicks > 0) {
                return 0;
            }
            startBurst(gun);
            return NOW_SHOT;
        }
        if (burstLeft > 0) {
            if (--burstTicks > 0) {
                return 0;
            }
            burstLeft--;
            burstTicks = SimStep.ticks(gun.burstGapSeconds());
            shots++;
            return NOW_SHOT;
        }
        if (!armed || !straight || !onField || Math.abs(x - shipX) > gun.passWithinX()) {
            return 0;
        }
        armed = false;
        int tell = SimStep.ticks(gun.tellSeconds());
        if (tell > 0) {
            tellTicks = tell;
            return NOW_GLINT;
        }
        startBurst(gun);
        return NOW_SHOT;
    }

    private void startBurst(EnemyGun gun) {
        burstLeft = gun.burst() - 1;
        burstTicks = SimStep.ticks(gun.burstGapSeconds());
        shots = 1;
    }

    /** Whether the shot {@link #fire} just let go is its burst's first. */
    boolean firstShot() {
        return shots == 1;
    }

    /** The line its burst's shots follow when they are not re-aimed, radians clockwise from straight down. */
    double lockedAim() {
        return lockedAim;
    }

    void lockAim(double aim) {
        lockedAim = aim;
    }

    /** Hashed only for a unit with a lane strafe. */
    void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(dir)
                .add(fromTop ? 1 : 0)
                .add(straight ? 1 : 0)
                .add(laneY)
                .add(centreX)
                .add(centreY)
                .add(turned)
                .add(armed ? 1 : 0)
                .add(tellTicks)
                .add(burstLeft)
                .add(burstTicks)
                .add(shots)
                .add(lockedAim);
    }

    Enemy.StrafePhase phase() {
        return phase;
    }

    /** Its strafe's direction now: 1 to the right, -1 to the left. */
    int side() {
        return dir;
    }

    /** Whether its eye glints now (its tell runs). */
    boolean glinting() {
        return tellTicks > 0;
    }

    /** Whether it flies a straight pass now. */
    boolean straight() {
        return straight;
    }

    /** Its lane's height now (y up). */
    double laneY() {
        return laneY;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }
}
