package vanguard.sim;

/**
 * M7 part G (design/campaign Level 20, The pursuit swarm; design/enemies/air/mote-swarm, {@code pursuit}): the leader
 * point of a {@code pursuit} wave's {@link Flock}, in screen space (y up from the bottom edge; the motes are never
 * flipped). It comes down from above the top edge into its band at the climb speed and holds the band ({@link
 * Phase#BAND}), bobbing slowly inside it and following the ship's x at the lag speed. Every {@code lungeEvery} s from
 * its entry (the next one from this tell's start; a tell due while it is out of the band waits for it) it tells
 * ({@link Phase#TELL}): it holds still while its flock contracts into a ball, and the ship's place at the tell's start
 * fixes the lunge: the lane at the ship's x and an end {@code overshoot} px below the ship. Then it dives straight to
 * that end at the dive speed ({@link Phase#DIVE}, no homing) and climbs straight back up into the band at the climb
 * speed ({@link Phase#CLIMB}), following the ship's x again. At the wave's break-off, from any phase, it climbs off the
 * top at the break speed ({@link Phase#BREAK}); its members follow it alive.
 *
 * <p>Deterministic and allocation-free; one per flock, reused. The presentation and the test autopilot read it through
 * {@link Sortie#pursuit()}.
 */
public final class Pursuit implements Hashed {
    /** What the leader point does. */
    public enum Phase {
        /** Holding the band below the top edge (and coming down into it on entry), following the ship's x. */
        BAND,
        /** Holding still while the flock contracts into a ball: the lunge's tell. */
        TELL,
        /** Diving straight down the ship's lane to below where the ship was at the tell's start. */
        DIVE,
        /** Climbing straight back up into the band, following the ship's x. */
        CLIMB,
        /** Climbing off the top at the break-off, the members following alive. */
        BREAK
    }

    /** One slow swing up and down the band per this many lunge periods. */
    static final double BOB_PERIODS = 2;
    /** The bob's reach: this share of the band's half height either side of its middle. */
    static final double BOB_REACH = 0.8;
    /** At the break-off it is gone once this far above the top edge, px. */
    static final double GONE_ABOVE = Formations.OUTSIDE + 60;

    private static final double TURN = 2 * StrictMath.PI;

    private WaveSpec.Pursuit spec;
    private Phase phase = Phase.BAND;
    /** Steps since its wave entered, and in this phase. */
    private int ticks;

    private int phaseTicks;
    /** The steps since entry of the next tell, of the break-off and of the wave's last refill (-1 for none). */
    private int nextTell;

    private int breakTicks;
    private int lastRefill;
    private int tellTicks;
    private int everyTicks;
    private int bobTicks;
    private double x;
    private double y;
    private double vx;
    private double vy;
    /** The lunge: its lane's x, its end's height, and where the leader point told from. */
    private double laneX;

    private double endY;
    private double fromX;
    private double fromY;
    /** Whether a tell, a lunge or the break-off started in this step (not hashed: set and read within the step). */
    private boolean toldNow;

    private boolean lungedNow;
    private boolean brokeNow;

    /**
     * Starts the leader point of a pursuit with {@code block} whose wave entered at level step {@code entryTick} (its
     * time {@code t}, s) at ({@code atX}, {@code atY}): its way in, above the top edge.
     */
    void start(WaveSpec.Pursuit block, double t, double atX, double atY) {
        spec = block;
        phase = Phase.BAND;
        ticks = 0;
        phaseTicks = 0;
        everyTicks = Math.max(1, SimStep.ticks(block.lungeEvery()));
        tellTicks = SimStep.ticks(block.tellSeconds());
        bobTicks = Math.max(1, SimStep.ticks(block.lungeEvery() * BOB_PERIODS));
        nextTell = everyTicks;
        int entry = SimStep.ticks(t);
        breakTicks = SimStep.ticks(block.breakAt()) - entry;
        int refills = block.refills(t);
        lastRefill = refills == 0 ? -1 : SimStep.ticks(t + refills * block.refillEvery()) - entry;
        x = atX;
        y = atY;
        vx = 0;
        vy = -block.climbSpeed();
        laneX = atX;
        endY = atY;
        fromX = atX;
        fromY = atY;
        toldNow = false;
        lungedNow = false;
        brokeNow = false;
    }

    /** One step toward the ship at ({@code shipX}, {@code shipY}) (screen, y up). */
    void step(double shipX, double shipY) {
        toldNow = false;
        lungedNow = false;
        brokeNow = false;
        ticks++;
        phaseTicks++;
        double startX = x;
        double startY = y;
        if (phase != Phase.BREAK && ticks >= breakTicks) {
            enter(Phase.BREAK);
            brokeNow = true;
            fromX = x;
            fromY = y;
        }
        switch (phase) {
            case BAND -> {
                follow(shipX);
                y = approach(y, bandY(), spec.climbSpeed() * SimStep.SECONDS);
                if (ticks >= nextTell && inBand()) {
                    enter(Phase.TELL);
                    toldNow = true;
                    laneX = Math.clamp(shipX, 0, PlayField.WIDTH);
                    endY = shipY - spec.overshoot();
                    fromX = x;
                    fromY = y;
                    nextTell = ticks + everyTicks;
                }
            }
            case TELL -> {
                if (phaseTicks >= tellTicks) {
                    enter(Phase.DIVE);
                    lungedNow = true;
                }
            }
            case DIVE -> {
                double dx = laneX - x;
                double dy = endY - y;
                double left = Math.sqrt(dx * dx + dy * dy);
                double step = spec.diveSpeed() * SimStep.SECONDS;
                if (left <= step) {
                    x = laneX;
                    y = endY;
                    enter(Phase.CLIMB);
                    fromY = y;
                } else {
                    x += dx / left * step;
                    y += dy / left * step;
                }
            }
            case CLIMB -> {
                follow(shipX);
                y += spec.climbSpeed() * SimStep.SECONDS;
                if (y >= bandLow()) {
                    enter(Phase.BAND);
                }
            }
            case BREAK -> y += spec.breakSpeed() * SimStep.SECONDS;
        }
        vx = (x - startX) * SimStep.PER_SECOND;
        vy = (y - startY) * SimStep.PER_SECOND;
    }

    private void enter(Phase next) {
        phase = next;
        phaseTicks = 0;
    }

    /** Sideways toward the ship's x at the lag speed. */
    private void follow(double shipX) {
        x = approach(x, Math.clamp(shipX, 0, PlayField.WIDTH), spec.lagSpeed() * SimStep.SECONDS);
    }

    private static double approach(double from, double to, double most) {
        return from + Math.clamp(to - from, -most, most);
    }

    /** The band's bottom (y up): {@code bandBottom} px below the top edge. */
    private double bandLow() {
        return PlayField.HEIGHT - spec.bandBottom();
    }

    private double bandHigh() {
        return PlayField.HEIGHT - spec.bandTop();
    }

    private boolean inBand() {
        return y >= bandLow() && y <= bandHigh();
    }

    /** Where in the band it is bound now: a slow swing about the band's middle. */
    private double bandY() {
        double middle = (bandLow() + bandHigh()) / 2;
        double reach = (bandHigh() - bandLow()) / 2 * BOB_REACH;
        return middle + reach * Trig.sin(TURN * (ticks % bobTicks) / bobTicks);
    }

    /** What it does now. */
    public Phase phase() {
        return phase;
    }

    /**
     * How far it is through its phase, 0 to 1: in the band, the time to the next tell; the tell's time; the dive's way;
     * the climb back to the band's bottom; the break-off's climb off the top.
     */
    public double progress() {
        return switch (phase) {
            case BAND -> Math.clamp(1 - (double) (nextTell - ticks) / everyTicks, 0, 1);
            case TELL -> tellTicks == 0 ? 1 : Math.clamp((double) phaseTicks / tellTicks, 0, 1);
            case DIVE -> {
                double whole = Math.hypot(laneX - fromX, endY - fromY);
                double left = Math.hypot(laneX - x, endY - y);
                yield whole == 0 ? 1 : Math.clamp(1 - left / whole, 0, 1);
            }
            case CLIMB -> {
                double whole = bandLow() - fromY;
                yield whole <= 0 ? 1 : Math.clamp((y - fromY) / whole, 0, 1);
            }
            case BREAK -> {
                double whole = PlayField.HEIGHT + GONE_ABOVE - fromY;
                yield whole <= 0 ? 1 : Math.clamp((y - fromY) / whole, 0, 1);
            }
        };
    }

    /** The leader point on the screen (not a unit): where its flock steers to. */
    public double leaderX() {
        return x;
    }

    /** Its height on the screen, px up from the bottom edge. */
    public double leaderY() {
        return y;
    }

    /** Its velocity, px/s (screen, y up). */
    double vx() {
        return vx;
    }

    double vy() {
        return vy;
    }

    /** The current or last lunge's lane: the ship's x at its tell's start. */
    public double laneX() {
        return laneX;
    }

    /** The current or last lunge's end: {@code overshoot} px below the ship's height at its tell's start. */
    public double laneEndY() {
        return endY;
    }

    /**
     * Whether a lunge threatens its lane now: during its tell and its dive (Rook steps out of it then, as for the
     * Kraken's telegraphed lanes).
     */
    public boolean lunging() {
        return phase == Phase.TELL || phase == Phase.DIVE;
    }

    /**
     * The left edge of the lunge's path, px: from where the leader point told to the lane, widened by half the ball
     * (Rook keeps his hull outside it during {@link #lunging()}).
     */
    public double laneLeft() {
        return Math.min(fromX, laneX) - spec.ballSize() / 2;
    }

    /** The right edge of the lunge's path, px (see {@link #laneLeft()}). */
    public double laneRight() {
        return Math.max(fromX, laneX) + spec.ballSize() / 2;
    }

    /** Its wave's block: the band, the cadence, the speeds, the refills and the break-off. */
    public WaveSpec.Pursuit spec() {
        return spec;
    }

    /** Whether a tell started in this step ({@link SimEvents.Type#PURSUIT_TELL}). */
    boolean toldNow() {
        return toldNow;
    }

    /** Whether a dive started in this step ({@link SimEvents.Type#PURSUIT_LUNGE}). */
    boolean lungedNow() {
        return lungedNow;
    }

    /** Whether the break-off started in this step ({@link SimEvents.Type#PURSUIT_BREAK}). */
    boolean brokeNow() {
        return brokeNow;
    }

    /** Whether it broke off and climbed off the top: its members still on the play field are left to follow. */
    boolean gone() {
        return phase == Phase.BREAK && y >= PlayField.HEIGHT + GONE_ABOVE;
    }

    /** Whether no member will join any more: it broke off, or its wave's last refill has come. */
    boolean refilled() {
        return phase == Phase.BREAK || ticks > lastRefill;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(ticks)
                .add(phaseTicks)
                .add(nextTell)
                .add(x)
                .add(y)
                .add(vx)
                .add(vy)
                .add(laneX)
                .add(endY)
                .add(fromX)
                .add(fromY);
    }
}
