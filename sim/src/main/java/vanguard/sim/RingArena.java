package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * M9 part H, step H1a: a ringed arena boss (design/enemies/bosses/iron-sovereign; {@link RingSpec}), stepped by its
 * {@link SetPiece} on the simulation's real steps while it is engaged (the scroll halted under it), in the style of the
 * {@link CoilArena}. Its centre is the boss's ({@link BossSpec#x()}, {@link BossSpec#hoverY()}); it moves with the
 * ground as any anchored arena boss.
 *
 * <p><b>The rings.</b> Each ring's angle turns its batteries' mounts ({@link #offsetX(int)}): an even ring at its speed
 * and direction, a stepping ring by its {@link RingSpec.Step}: its tell (the teeth, {@link
 * SimEvents.Type#RING_STEP_TELL}), the step at an even speed ({@link SimEvents.Type#RING_STEP}), the hold ({@link
 * SimEvents.Type#RING_HOLD}). The turning rings run at their speed × {@link RingSpec#speedUp} of the locked rings'
 * count (a step's time ÷ it). Over the spin-up from the engagement the rings light one after the other ({@link
 * SimEvents.Type#RING_LIT}) and speed up from rest; the boss is invulnerable and silent until {@link
 * SimEvents.Type#RINGS_UP}.
 *
 * <p><b>The fire arc.</b> Its centre is the bearing from the centre to the ship, clamped ({@link RingSpec#bearing()});
 * a battery is {@link #armed} while it lives, its ring is not locked, the spin-up is over and its mount lies inside the
 * arc. Only armed batteries fire (the coordinator's guns ask {@link #fires}); a battery outside the arc can still be
 * hit. A stepping ring's attack fires once at each hold's start ({@link #holdStarted}).
 *
 * <p><b>Locks.</b> A ring whose every battery is dead locks ({@link SimEvents.Type#RING_LOCKED}) and grinds to a halt
 * ({@link SimEvents.Type#RING_HALTED}); the boss's phases end on the count of locked rings ({@link #phaseOver}).
 *
 * <p><b>Death.</b> From the kill every battery cooks off in the rings' order, {@link RingSpec#cookoffSeconds()} apart
 * ({@link SimEvents.Type#RING_COOKOFF}, value {@link #cookoffValue}); the rings stop turning.
 *
 * <p><b>The core</b> (step H1b): its opening, shield, fan glow, spiral cycle, rail and bays are its {@link RingCore}
 * ({@link #core()}), stepped here.
 *
 * <p>Its events are raised through the {@link BossActions}. Everything is allocated here; stepping does not allocate.
 * Its state is hashed with its boss (only a ringed boss hashes it).
 */
public final class RingArena implements Hashed {
    /** Where a stepping ring is in its cycle. */
    public enum StepState {
        /** An even ring, a stepping ring before its first tell, or a locked ring. */
        NONE,
        /** The teeth flash before the step. */
        TELL,
        /** It turns its step. */
        STEP,
        /** It holds (its battery's sweep fires at the hold's start). */
        HOLD
    }

    /** A {@link SimEvents.Type#RING_COOKOFF} value: {@code ring × COOKOFF_RING + battery}. */
    public static final int COOKOFF_RING = 64;

    private static final double TURN = 2 * StrictMath.PI;
    private static final StepState[] STATES = StepState.values();

    private final SetPiece piece;
    private final RingSpec spec;
    private final int count;
    /** Per boss part, its ring (-1: not a battery) and its battery index on that ring. */
    private final int[] ringOf;

    private final int[] slotOf;
    /** Every battery in the cook-off order. */
    private final int[] order;

    private final double[] radius;
    private final int[] lightTicks;
    private final int spinUpTicks;
    private final int grindTicks;
    private final int[] tellTicks;
    private final int[] holdTicks;

    // Per ring.
    private final double[] angle;
    private final double[] prevAngle;
    private final double[] velocity;
    private final int[] stepState;
    private final int[] stepTicks;
    private final int[] stepLength;
    private final double[] stepFrom;
    private final boolean[] locked;
    private final int[] lockTicks;
    private final double[] lockVelocity;
    /** Whether the ring's hold began in this step (derived each step, not hashed). */
    private final boolean[] holdNow;

    private int lockedCount;
    /** Steps since the engagement; -1 before. */
    private int spinTicks;
    /** Steps since the kill; -1 while it lives. */
    private int deathTicks;
    /** The fire arc's centre, radians clockwise from straight down. */
    private double bearing;

    private double shipX;
    private double shipY;

    /** Step H1b: the core's opening, spiral, rail and bays; null without a core block. */
    private final RingCore core;
    /** {@link #core} for the presentation, made once. */
    private final java.util.Optional<RingCore> coreView;

    RingArena(SetPiece piece, BossSpec boss, SetPieceSpec setPiece) {
        this.piece = piece;
        spec = boss.rings().orElseThrow();
        count = spec.rings().size();
        int parts = setPiece.parts().size();
        ringOf = new int[parts];
        slotOf = new int[parts];
        Arrays.fill(ringOf, -1);
        radius = new double[count];
        lightTicks = new int[count];
        tellTicks = new int[count];
        holdTicks = new int[count];
        spinUpTicks = SimStep.ticks(spec.spinUpSeconds());
        grindTicks = SimStep.ticks(spec.grindSeconds());
        int batteries = 0;
        for (int i = 0; i < count; i++) {
            RingSpec.Ring ring = spec.rings().get(i);
            radius[i] = ring.radius();
            lightTicks[i] = SimStep.ticks(i * spec.spinUpSeconds() / count);
            tellTicks[i] = ring.step().map(s -> SimStep.ticks(s.tellSeconds())).orElse(0);
            holdTicks[i] = ring.step()
                    .map(s -> Math.max(1, SimStep.ticks(s.holdSeconds())))
                    .orElse(0);
            for (int k = 0; k < ring.parts().size(); k++) {
                int p = ring.parts().get(k);
                if (p >= parts) {
                    throw new IllegalArgumentException(setPiece.slug() + ": " + ring.name() + "'s battery " + p
                            + " is not one of its " + parts + " parts");
                }
                ringOf[p] = i;
                slotOf[p] = k;
                batteries++;
            }
        }
        order = new int[batteries];
        int n = 0;
        for (RingSpec.Ring ring : spec.rings()) {
            for (int k = 0; k < ring.parts().size(); k++) {
                order[n++] = ring.parts().get(k);
            }
        }
        angle = new double[count];
        prevAngle = new double[count];
        velocity = new double[count];
        stepState = new int[count];
        stepTicks = new int[count];
        stepLength = new int[count];
        stepFrom = new double[count];
        locked = new boolean[count];
        lockTicks = new int[count];
        lockVelocity = new double[count];
        holdNow = new boolean[count];
        core = spec.core().map(c -> new RingCore(piece, boss, c)).orElse(null);
        coreView = java.util.Optional.ofNullable(core);
        reset();
    }

    /** Back to the level start (and the boss checkpoint): every ring at rest at its start, none locked or lit. */
    void reset() {
        Arrays.fill(angle, 0);
        Arrays.fill(prevAngle, 0);
        Arrays.fill(velocity, 0);
        Arrays.fill(stepState, StepState.NONE.ordinal());
        Arrays.fill(stepTicks, 0);
        Arrays.fill(stepLength, 0);
        Arrays.fill(stepFrom, 0);
        Arrays.fill(locked, false);
        Arrays.fill(lockTicks, -1);
        Arrays.fill(lockVelocity, 0);
        Arrays.fill(holdNow, false);
        lockedCount = 0;
        spinTicks = -1;
        deathTicks = -1;
        bearing = 0;
        shipX = 0;
        shipY = 0;
        if (core != null) {
            core.reset();
        }
    }

    /**
     * One engaged step: the fire arc follows the ship at (shipX, shipY); a ring whose last battery died locks; the rings
     * light, spin up, turn, step and grind; their events go through {@code actions}. Step H1b: then the core in boss
     * phase {@code phase} (its launches only while {@code firing}).
     */
    void step(double shipX, double shipY, int phase, BossActions actions, boolean firing) {
        this.shipX = shipX;
        this.shipY = shipY;
        double cx = piece.x();
        double cy = piece.y();
        bearing = spec.bearing().apply(StrictMath.atan2(-(shipX - cx), -(shipY - cy)));
        spinTicks++;
        for (int i = 0; i < count; i++) {
            holdNow[i] = false;
            if (!locked[i] && allDead(i)) {
                locked[i] = true;
                lockTicks[i] = 0;
                lockVelocity[i] = velocity[i];
                stepState[i] = StepState.NONE.ordinal();
                lockedCount++;
                actions.ring(SimEvents.Type.RING_LOCKED, cx, cy, i);
            }
        }
        double factor = spec.speedUp(lockedCount);
        for (int i = 0; i < count; i++) {
            prevAngle[i] = angle[i];
            RingSpec.Ring ring = spec.rings().get(i);
            if (spinTicks == lightTicks[i]) {
                actions.ring(SimEvents.Type.RING_LIT, cx, cy, i);
            }
            if (locked[i]) {
                grind(i, cx, cy, actions);
            } else if (spinTicks < spinUpTicks) {
                // The spin-up: an even ring speeds up from rest after it lights; a stepping ring waits.
                double share = ring.step().isPresent() || spinTicks < lightTicks[i]
                        ? 0
                        : (double) (spinTicks - lightTicks[i] + 1) / Math.max(1, spinUpTicks - lightTicks[i]);
                velocity[i] = ring.direction() * ring.radiansPerSecond() * Math.min(1, share);
                angle[i] = wrap(angle[i] + velocity[i] * SimStep.SECONDS);
            } else if (ring.step().isPresent()) {
                cycle(i, ring, factor, cx, cy, actions);
            } else {
                velocity[i] = ring.direction() * ring.radiansPerSecond() * factor;
                angle[i] = wrap(angle[i] + velocity[i] * SimStep.SECONDS);
            }
        }
        if (spinTicks == spinUpTicks) {
            actions.ring(SimEvents.Type.RINGS_UP, cx, cy, 0);
        }
        if (core != null) {
            core.step(phase, shipX, shipY, actions, firing);
        }
    }

    /** A locked ring slows evenly to rest over the grind. */
    private void grind(int i, double cx, double cy, BossActions actions) {
        if (lockTicks[i] < 0) {
            return;
        }
        lockTicks[i]++;
        double share = grindTicks == 0 ? 0 : Math.max(0, 1 - (double) lockTicks[i] / grindTicks);
        velocity[i] = lockVelocity[i] * share;
        angle[i] = wrap(angle[i] + velocity[i] * SimStep.SECONDS);
        if (lockTicks[i] >= grindTicks) {
            lockTicks[i] = -1;
            velocity[i] = 0;
            actions.ring(SimEvents.Type.RING_HALTED, cx, cy, i);
        }
    }

    /** A stepping ring's tell, step and hold. */
    private void cycle(int i, RingSpec.Ring ring, double factor, double cx, double cy, BossActions actions) {
        RingSpec.Step step = ring.step().orElseThrow();
        velocity[i] = 0;
        if (stepState[i] == StepState.NONE.ordinal()) {
            stepState[i] = StepState.TELL.ordinal();
            stepTicks[i] = 0;
            actions.ring(SimEvents.Type.RING_STEP_TELL, cx, cy, i);
        } else {
            stepTicks[i]++;
        }
        if (stepState[i] == StepState.TELL.ordinal() && stepTicks[i] >= tellTicks[i]) {
            stepState[i] = StepState.STEP.ordinal();
            stepTicks[i] = 0;
            stepFrom[i] = angle[i];
            stepLength[i] = Math.max(1, SimStep.ticks(step.seconds() / factor));
            actions.ring(SimEvents.Type.RING_STEP, cx, cy, i);
            return;
        }
        if (stepState[i] == StepState.STEP.ordinal()) {
            double share = Math.min(1, (double) stepTicks[i] / stepLength[i]);
            angle[i] = wrap(stepFrom[i] + ring.direction() * step.radians() * share);
            velocity[i] = ring.direction() * step.radians() / (stepLength[i] * SimStep.SECONDS);
            if (stepTicks[i] >= stepLength[i]) {
                stepState[i] = StepState.HOLD.ordinal();
                stepTicks[i] = 0;
                holdNow[i] = true;
                actions.ring(SimEvents.Type.RING_HOLD, cx, cy, i);
            }
            return;
        }
        if (stepState[i] == StepState.HOLD.ordinal() && stepTicks[i] >= holdTicks[i] - tellTicks[i]) {
            stepState[i] = StepState.TELL.ordinal();
            stepTicks[i] = 0;
            actions.ring(SimEvents.Type.RING_STEP_TELL, cx, cy, i);
            if (tellTicks[i] == 0) {
                cycle(i, ring, factor, cx, cy, actions);
            }
        }
    }

    private boolean allDead(int i) {
        List<Integer> parts = spec.rings().get(i).parts();
        for (int k = 0; k < parts.size(); k++) {
            if (piece.partHp(parts.get(k)) > 0) {
                return false;
            }
        }
        return true;
    }

    private static double wrap(double radians) {
        return Math.IEEEremainder(radians, TURN);
    }

    /** The boss was killed: the rings stop and the cook-offs start. */
    void died() {
        if (deathTicks < 0) {
            deathTicks = 0;
            Arrays.fill(velocity, 0);
            System.arraycopy(angle, 0, prevAngle, 0, count);
        }
    }

    /** One step after the kill: the batteries cook off in order ({@link SimEvents.Type#RING_COOKOFF}). */
    void dying(BossActions actions) {
        if (deathTicks < 0 || deathTicks > lastCookoffTicks()) {
            return;
        }
        for (int n = 0; n < order.length; n++) {
            if (deathTicks == SimStep.ticks(n * spec.cookoffSeconds())) {
                int p = order[n];
                actions.ring(SimEvents.Type.RING_COOKOFF, piece.partX(p), piece.partY(p), cookoffValue(p));
            }
        }
        deathTicks++;
    }

    private int lastCookoffTicks() {
        return SimStep.ticks((order.length - 1) * spec.cookoffSeconds());
    }

    /** The {@link SimEvents.Type#RING_COOKOFF} value of battery {@code p}: its ring × {@link #COOKOFF_RING} + its index. */
    public int cookoffValue(int p) {
        return ringOf[p] * COOKOFF_RING + slotOf[p];
    }

    /** Whether the phase {@code phase} is over on locks: it ends on locks and that many rings are locked. */
    boolean phaseOver(int phase) {
        int locks = spec.locks(phase);
        return locks > 0 && lockedCount >= locks;
    }

    /** Step H1b: whether nothing fires now (the phases' clocks wait): the spin-up, or the core opening. */
    boolean silent() {
        return spinningUp() || (core != null && core.opening());
    }

    /** Step H1b: whether part {@code p} cannot be touched now beyond the spin-up: the core while it opens. */
    boolean untouchable(int p) {
        return core != null && core.untouchable(p);
    }

    /** Step H1b: whether attack {@code a} may fire now (the core's rail only below its HP share). */
    boolean attackFires(int a) {
        return core == null || core.fires(a);
    }

    /** Step H1b: whether attack {@code a} runs now (the core's spiral only in its cycle's run). */
    boolean spiralling(int a) {
        return core == null || core.spiralling(a);
    }

    /** Step H1b: whether {@code attack}'s glow (the shut core's fan tell) starts now, its volley {@code ticks} away. */
    boolean glowDue(BossSpec.Attack attack, int ticks) {
        return core != null && core.glowDue(attack, ticks);
    }

    /** Step H1b: the core's opening, spiral, rail and bays; empty without a core block. */
    public java.util.Optional<RingCore> core() {
        return coreView;
    }

    /** Whether the rings still spin up (or it has not engaged): every part invulnerable, nothing fires. */
    public boolean spinningUp() {
        return spinTicks < spinUpTicks;
    }

    /** Whether part {@code p} may fire now: a battery while {@link #armed}; any other part always. */
    boolean fires(int p) {
        return ringOf[p] < 0 || armed(p);
    }

    /**
     * Whether battery {@code p} is armed: it lives, its ring is not locked, the spin-up is over and its mount lies
     * inside the fire arc. False for a part that is no battery.
     */
    public boolean armed(int p) {
        int i = ringOf[p];
        return i >= 0 && !spinningUp() && !locked[i] && piece.partHp(p) > 0 && inArc(p);
    }

    /** Whether battery {@code p}'s mount lies inside the fire arc now (dead or alive). */
    public boolean inArc(int p) {
        return Math.abs(wrap(mountRadians(p) - bearing)) <= spec.arcRadians() / 2;
    }

    /** Whether ring {@code i}'s hold began in this step (its attack's sweep fires then). */
    boolean holdStarted(int i) {
        return holdNow[i];
    }

    /** Ring {@code i}'s attack, fired at its holds' starts: -1 for an even ring or a ring without one. */
    int holdAttack(int i) {
        RingSpec.Ring ring = spec.rings().get(i);
        return ring.step().isPresent() ? ring.attack() : -1;
    }

    /** Whether attack {@code a} fires only at a stepping ring's holds (not on its own interval). */
    boolean firesAtHolds(int a) {
        for (int i = 0; i < count; i++) {
            if (holdAttack(i) == a) {
                return true;
            }
        }
        return false;
    }

    /** The ring battery {@code p} rides; -1 for a part that is no battery. */
    public int battery(int p) {
        return ringOf[p];
    }

    /** Battery {@code p}'s index on its ring. */
    public int slot(int p) {
        return slotOf[p];
    }

    /** Battery {@code p}'s mount angle now, radians clockwise from straight down. */
    public double mountRadians(int p) {
        int i = ringOf[p];
        return wrap(spec.rings().get(i).mountRadians(slotOf[p]) + angle[i]);
    }

    /** Battery {@code p}'s offset from the centre, px to the right. */
    double offsetX(int p) {
        return -radius[ringOf[p]] * Trig.sin(mountRadians(p));
    }

    /** Battery {@code p}'s offset from the centre, px up. */
    double offsetY(int p) {
        return -radius[ringOf[p]] * Trig.cos(mountRadians(p));
    }

    /** Battery {@code p}'s turret heading toward the ship, radians clockwise from straight down (64 headings: the game's). */
    public double turretRadians(int p) {
        return StrictMath.atan2(-(shipX - piece.partX(p)), -(shipY - piece.partY(p)));
    }

    /** Its spec. */
    public RingSpec spec() {
        return spec;
    }

    /** Its rings. */
    public int ringCount() {
        return count;
    }

    /** Ring {@code i}'s angle now (its first battery's turn from its start), radians clockwise. */
    public double angle(int i) {
        return angle[i];
    }

    /** Ring {@code i}'s angle between the last step and this one; {@code alpha} in [0, 1]. */
    public double renderAngle(int i, double alpha) {
        return prevAngle[i] + wrap(angle[i] - prevAngle[i]) * alpha;
    }

    /** Ring {@code i}'s turning speed in this step, radians a second, clockwise positive. */
    public double velocity(int i) {
        return velocity[i];
    }

    /** Ring {@code i}'s step state (a stepping ring's). */
    public StepState stepState(int i) {
        return STATES[stepState[i]];
    }

    /** Whether ring {@code i} is locked (every battery dead). */
    public boolean locked(int i) {
        return locked[i];
    }

    /** Whether ring {@code i} still grinds to its halt after its lock. */
    public boolean grinding(int i) {
        return locked[i] && lockTicks[i] >= 0;
    }

    /** Whether ring {@code i} has lit (its lights on, from its turn in the spin-up). */
    public boolean lit(int i) {
        return spinTicks >= lightTicks[i];
    }

    /** The locked rings' count. */
    public int lockedCount() {
        return lockedCount;
    }

    /** The fire arc's centre now, radians clockwise from straight down. */
    public double bearing() {
        return bearing;
    }

    /** The fire arc's width, radians. */
    public double arcRadians() {
        return spec.arcRadians();
    }

    /** The spin-up's progress, 0 before the engagement to 1 when the rings are up. */
    public double spinUpShare() {
        return spinUpTicks == 0 ? (spinTicks >= 0 ? 1 : 0) : Math.clamp((double) spinTicks / spinUpTicks, 0.0, 1.0);
    }

    /** Seconds since the kill; negative while it lives. */
    public double deathSeconds() {
        return deathTicks < 0 ? -1 : deathTicks * SimStep.SECONDS;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(lockedCount).add(spinTicks).add(deathTicks).add(bearing);
        for (int i = 0; i < count; i++) {
            hash.add(angle[i])
                    .add(velocity[i])
                    .add(stepState[i])
                    .add(stepTicks[i])
                    .add(stepLength[i])
                    .add(stepFrom[i])
                    .add(locked[i] ? 1 : 0)
                    .add(lockTicks[i])
                    .add(lockVelocity[i]);
        }
        if (core != null) {
            core.addTo(hash);
        }
    }
}
