package vanguard.sim;

/**
 * The player's AF-12 Stormhawk: flies by commands with quick acceleration, stays inside the play
 * field and banks with its horizontal speed. Its weapons are the sortie's {@link PlayerFire}.
 */
public final class Ship {
    /** Where every sortie starts: centred in the lower third. */
    static final double START_X = PlayField.WIDTH / 2.0;

    static final double START_Y = 96;
    private static final double DIAGONAL = StrictMath.sqrt(0.5);

    private final ShipSpec spec;
    private final Defences defences;
    private final double accelerationPerStep;
    private final double stopPerStep;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double vx;
    private double vy;
    private int bank;
    private int bankTimer;
    /** M7 part A: the Evasive thrusters' dash; null without them (then nothing of it is hashed). */
    private final Thrusters thrusters;

    Ship(ShipSpec spec, Defences defences) {
        this(spec, defences, null);
    }

    /** @param dash M7 part A: the fitted Evasive thrusters' dash; null for none */
    Ship(ShipSpec spec, Defences defences, ThrusterSpec dash) {
        this.spec = spec;
        this.defences = defences;
        this.thrusters = dash == null ? null : new Thrusters(dash);
        this.accelerationPerStep = spec.speed() / spec.accelerationSeconds() * SimStep.SECONDS;
        this.stopPerStep = spec.speed() / spec.stopSeconds() * SimStep.SECONDS;
        reset(defences.maxArmour());
    }

    /**
     * The launch off the rail (design/campaign, Level 01: the non-playable launch): from below the
     * play field up to the start position, slowing down as it arrives. {@code progress} runs from 0
     * to 1; commands are ignored meanwhile.
     */
    void launch(double progress) {
        launch(progress, false);
    }

    /**
     * As {@link #launch(double)}; M7 part B (design/campaign Level 15, default pending the user's review):
     * with {@code drop} it drops from above the top edge (the transport's bay) to the start position
     * instead, slowing down as it arrives.
     */
    void launch(double progress, boolean drop) {
        double remaining = 1 - progress;
        double fromY = drop ? PlayField.HEIGHT + spec.size() : -spec.size();
        x = START_X;
        y = START_Y + (fromY - START_Y) * remaining * remaining;
        vx = vy = 0;
    }

    /** Back at the start position, at rest, with full defences. */
    void reset(double armour) {
        x = prevX = START_X;
        y = prevY = START_Y;
        vx = vy = 0;
        bank = bankTimer = 0;
        defences.restore(armour);
        if (thrusters != null) {
            thrusters.reset();
        }
    }

    void rememberPosition() {
        prevX = x;
        prevY = y;
    }

    /** Steers towards the commanded velocity, moves, banks and regenerates the shield. */
    void fly(int commands) {
        fly(commands, 0);
    }

    /**
     * As {@link #fly(int)}, pushed sideways by the level's {@code wind}, px/s to the right (M5 part F,
     * design/campaign Level 12, Weather): the push adds to the position, not the velocity, so the
     * ship's steering and banking are unchanged and the play field's edges clamp as ever; a wind of
     * exactly 0 changes nothing.
     */
    void fly(int commands, double wind) {
        fly(commands, wind, 0);
    }

    /**
     * As {@link #fly(int, double)}; M7 part E: also pushed {@code pushY} px/s up the screen (the Dust Devils'
     * pull, whose sideways part is in {@code wind}), added to the position like the wind; exactly 0 changes
     * nothing.
     */
    void fly(int commands, double wind, double pushY) {
        if (thrusters != null) {
            thrusters.command(commands);
            if (thrusters.dashing()) {
                dash(wind, pushY);
                return;
            }
        }
        double dx = (Command.RIGHT.in(commands) ? 1 : 0) - (Command.LEFT.in(commands) ? 1 : 0);
        double dy = (Command.UP.in(commands) ? 1 : 0) - (Command.DOWN.in(commands) ? 1 : 0);
        double speed = spec.speed() * (Command.PRECISION.in(commands) ? spec.precisionFactor() : 1);
        if (dx != 0 && dy != 0) {
            speed *= DIAGONAL;
        }
        vx = approach(vx, dx * speed);
        vy = approach(vy, dy * speed);
        x += vx * SimStep.SECONDS;
        y += vy * SimStep.SECONDS;
        if (wind != 0) {
            x += wind * SimStep.SECONDS;
        }
        if (pushY != 0) {
            y += pushY * SimStep.SECONDS;
        }
        double margin = spec.edgeLimit();
        if (x < margin || x > PlayField.WIDTH - margin) {
            x = Math.clamp(x, margin, PlayField.WIDTH - margin);
            vx = 0;
        }
        if (y < margin || y > PlayField.HEIGHT - margin) {
            y = Math.clamp(y, margin, PlayField.HEIGHT - margin);
            vy = 0;
        }
        steerBank();
        defences.step();
    }

    /**
     * M7 part A: one step of a dash (design/player/systems, Evasive thrusters): its share of the dash's
     * distance along its direction, the ship's own velocity set aside (it keeps it for afterwards), the
     * wind's push as ever; the play field's edges clamp it.
     */
    private void dash(double wind, double pushY) {
        x += thrusters.stepX();
        y += thrusters.stepY();
        thrusters.moved();
        if (wind != 0) {
            x += wind * SimStep.SECONDS;
        }
        if (pushY != 0) {
            y += pushY * SimStep.SECONDS;
        }
        double margin = spec.edgeLimit();
        x = Math.clamp(x, margin, PlayField.WIDTH - margin);
        y = Math.clamp(y, margin, PlayField.HEIGHT - margin);
        steerBank();
        defences.step();
    }

    /**
     * M7 part C (design/campaign Level 16, Wall contact): moved {@code distance} px along the unit vector
     * (nx, ny) out of rock, its velocity into the rock dropped (it slides along the wall); the play field's
     * edges clamp it as ever.
     */
    void pushOut(double nx, double ny, double distance) {
        x += nx * distance;
        y += ny * distance;
        double into = vx * nx + vy * ny;
        if (into < 0) {
            vx -= into * nx;
            vy -= into * ny;
        }
        double margin = spec.edgeLimit();
        x = Math.clamp(x, margin, PlayField.WIDTH - margin);
        y = Math.clamp(y, margin, PlayField.HEIGHT - margin);
    }

    /** Moves one velocity component towards its target: accelerating, or braking to a stop. */
    private double approach(double velocity, double target) {
        double rate = target == 0 ? stopPerStep : accelerationPerStep;
        if (velocity < target) {
            return Math.min(target, velocity + rate);
        }
        return Math.max(target, velocity - rate);
    }

    /** Banking follows the horizontal speed, one frame every few steps (design/art-direction). */
    private void steerBank() {
        int target =
                Math.clamp(Math.round(ShipSpec.HARD_BANK * vx / spec.speed()), -ShipSpec.HARD_BANK, ShipSpec.HARD_BANK);
        if (bank == target) {
            bankTimer = 0;
        } else if (++bankTimer >= spec.bankStepTicks()) {
            bank += Integer.signum(target - bank);
            bankTimer = 0;
        }
    }

    void addTo(StateHash hash) {
        hash.add(x).add(y).add(vx).add(vy).add(bank).add(bankTimer);
        defences.addTo(hash);
        if (thrusters != null) {
            // M7 part A: only with the Evasive thrusters fitted, so other runs hash as before.
            thrusters.addTo(hash);
        }
    }

    /** M7 part A: whether the Evasive thrusters are fitted (the HUD's dash pip). */
    public boolean thrustersFitted() {
        return thrusters != null;
    }

    /** M7 part A: whether a dash moves the ship now. */
    public boolean dashing() {
        return thrusters != null && thrusters.dashing();
    }

    /** M7 part A: whether a dash started in this step (the {@link SimEvents.Type#DASH} event). */
    boolean dashStarted() {
        return thrusters != null && thrusters.started();
    }

    /** M7 part A: the last dash's direction as an eighth: 0 straight up, clockwise (2 right, 4 down, 6 left). */
    public int dashEighth() {
        return thrusters == null ? 0 : thrusters.eighth();
    }

    /** M7 part A: the last dash's direction, a unit vector (y up); 0 before any. */
    public double dashDirX() {
        return thrusters == null ? 0 : thrusters.dirX();
    }

    public double dashDirY() {
        return thrusters == null ? 0 : thrusters.dirY();
    }

    /**
     * M7 part A: whether the dash's invulnerability holds: enemy bullets pass through the ship (they
     * are not absorbed), contacts and hazards do not hurt it.
     */
    public boolean evading() {
        return thrusters != null && thrusters.invulnerable();
    }

    /** M7 part A: how far the dash's cooldown has run, 0 just dashed to 1 ready (the dash pip); 1 without thrusters. */
    public double dashReadiness() {
        return thrusters == null ? 1 : thrusters.cooldownShare();
    }

    /** M7 part A: whether a dash can start now. */
    public boolean dashReady() {
        return thrusters != null && thrusters.coolingTicks() == 0 && !thrusters.dashing();
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }

    public double vx() {
        return vx;
    }

    public double vy() {
        return vy;
    }

    /** The banking frame: -2 hard left, 0 level, 2 hard right. */
    public int bank() {
        return bank;
    }

    public Defences defences() {
        return defences;
    }

    public ShipSpec spec() {
        return spec;
    }
}
