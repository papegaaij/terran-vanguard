package vanguard.sim;

/**
 * M7 part A, the Evasive thrusters in flight (design/player/systems, Evasive thrusters (Act 3)): the
 * Dash action and the dash. A dash starts on a press of {@link Command#DASH} (in the direction held, or
 * straight ahead with none) or on a double-tap of a direction, which is detected here from the
 * direction commands of every step, so a replay dashes as the run did: the second press of a direction
 * within the double-tap time of the first press's release, that direction alone pressed and released,
 * nothing else pressed in between. A dash moves the ship its distance over its time in one of 8
 * directions (a diagonal its distance along it), its own speed set aside; the ship cannot be hurt for
 * the invulnerable time from the start, and the next dash waits for the cooldown from the start; a dash
 * asked for while it cools does nothing. The ship owns it; it is hashed only with the module fitted.
 */
final class Thrusters {
    private static final int DIRECTIONS =
            Command.UP.bit() | Command.DOWN.bit() | Command.LEFT.bit() | Command.RIGHT.bit();
    private static final double DIAGONAL = StrictMath.sqrt(0.5);

    private final ThrusterSpec spec;
    private final int dashTicks;
    private final int invulnerableTicks;
    private final int cooldownTicks;
    private final int tapTicks;
    private final double stepLength;

    /** The direction commands of the last step. */
    private int lastDirections;
    /** Whether the Dash button was down in the last step. */
    private boolean held;
    /** The direction released alone that a second press would make a double-tap of; 0 for none. */
    private int tapped;
    /** Steps since that release. */
    private int sinceRelease;
    /** Steps of the dash's movement left; 0 when none runs. */
    private int dashLeft;
    /** The dash's direction, a unit vector (y up). */
    private double dirX;

    private double dirY;
    /** The dash's direction as an eighth, 0 straight up, clockwise (for the event). */
    private int eighth;
    /** Steps the ship still cannot be hurt. */
    private int invulnerable;
    /** Steps until the next dash may start. */
    private int cooling;
    /** Whether a dash started in this step (not hashed: set and read within the step). */
    private boolean started;

    Thrusters(ThrusterSpec spec) {
        this.spec = spec;
        dashTicks = Math.max(1, SimStep.ticks(spec.seconds()));
        invulnerableTicks = SimStep.ticks(spec.invulnerableSeconds());
        cooldownTicks = SimStep.ticks(spec.cooldownSeconds());
        tapTicks = SimStep.ticks(spec.doubleTapSeconds());
        stepLength = spec.distance() / dashTicks;
        reset();
    }

    /** At an attempt's start or a boss checkpoint: ready, nothing remembered. */
    void reset() {
        lastDirections = 0;
        held = false;
        tapped = 0;
        sinceRelease = 0;
        dashLeft = 0;
        dirX = 0;
        dirY = 0;
        eighth = 0;
        invulnerable = 0;
        cooling = 0;
        started = false;
    }

    /**
     * Reads one step's commands: the timers run down, then a Dash press or a double-tap starts a dash
     * when it is ready. Returns whether a dash started now.
     */
    boolean command(int commands) {
        started = false;
        if (invulnerable > 0) {
            invulnerable--;
        }
        if (cooling > 0) {
            cooling--;
        }
        if (tapped != 0) {
            sinceRelease++;
        }
        int directions = commands & DIRECTIONS;
        boolean down = Command.DASH.in(commands);
        boolean button = down && !held;
        held = down;
        int pressed = directions & ~lastDirections;
        int wanted = -1;
        if (button) {
            wanted = directions;
        } else if (pressed != 0) {
            if (tapped != 0 && pressed == tapped && directions == tapped && sinceRelease <= tapTicks) {
                wanted = tapped;
            }
            // A new press uses or breaks the remembered tap.
            tapped = 0;
        }
        if (directions == 0 && lastDirections != 0) {
            // A direction released alone can be tapped again.
            tapped = Integer.bitCount(lastDirections) == 1 ? lastDirections : 0;
            sinceRelease = 0;
        } else if (tapped != 0 && sinceRelease > tapTicks) {
            tapped = 0;
        }
        lastDirections = directions;
        if (wanted < 0 || cooling > 0 || dashLeft > 0) {
            return false;
        }
        start(wanted);
        return true;
    }

    private void start(int directions) {
        int dx = (Command.RIGHT.in(directions) ? 1 : 0) - (Command.LEFT.in(directions) ? 1 : 0);
        int dy = (Command.UP.in(directions) ? 1 : 0) - (Command.DOWN.in(directions) ? 1 : 0);
        if (dx == 0 && dy == 0) {
            // Nothing held (or opposites): straight ahead.
            dy = 1;
        }
        double scale = dx != 0 && dy != 0 ? DIAGONAL : 1;
        dirX = dx * scale;
        dirY = dy * scale;
        eighth = eighth(dx, dy);
        dashLeft = dashTicks;
        invulnerable = invulnerableTicks;
        cooling = cooldownTicks;
        started = true;
    }

    private static int eighth(int dx, int dy) {
        if (dx == 0) {
            return dy > 0 ? 0 : 4;
        }
        if (dx > 0) {
            return 2 - dy;
        }
        return 6 + dy;
    }

    /** Whether the dash moves the ship this step. */
    boolean dashing() {
        return dashLeft > 0;
    }

    /** The dash's movement this step, px along x; counts the step. */
    double stepX() {
        return dirX * stepLength;
    }

    double stepY() {
        return dirY * stepLength;
    }

    /** One step of the dash's movement is done. */
    void moved() {
        dashLeft--;
    }

    boolean started() {
        return started;
    }

    int eighth() {
        return eighth;
    }

    boolean invulnerable() {
        return invulnerable > 0;
    }

    double cooldownShare() {
        return cooldownTicks == 0 ? 1 : 1 - (double) cooling / cooldownTicks;
    }

    int coolingTicks() {
        return cooling;
    }

    double dirX() {
        return dirX;
    }

    double dirY() {
        return dirY;
    }

    ThrusterSpec spec() {
        return spec;
    }

    void addTo(StateHash hash) {
        hash.add(lastDirections)
                .add(held ? 1 : 0)
                .add(tapped)
                .add(sinceRelease)
                .add(dashLeft)
                .add(dirX)
                .add(dirY)
                .add(eighth)
                .add(invulnerable)
                .add(cooling);
    }
}
