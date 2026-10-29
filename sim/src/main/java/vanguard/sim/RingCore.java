package vanguard.sim;

/**
 * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, Phases, Tells and Dart Drones; {@link RingSpec.Core}): a
 * ringed boss's core, kept by its {@link RingArena}. Shut (before its phase) its fans glow before each volley ({@link
 * #glowDue}). When its phase comes it opens over its opening time ({@link SimEvents.Type#CORE_OPENING}, {@link
 * SimEvents.Type#CORE_OPEN}): untouchable and silent until open (the phase's clocks wait). Open, its spiral runs in its
 * cycle (tell, run, rest; {@link #spiralling}), its rail line fires only below its HP share ({@link #fires}), and its
 * rim bays launch Dart streams, one bay a turn: the nearest the ship outside the skip distance, after the bay's tell
 * ({@link SimEvents.Type#BAY_TELL}), the stream's units its gap apart through {@link BossActions#bayLaunch}. Its state
 * is hashed with its arena's. Allocates nothing once made.
 */
public final class RingCore {
    private final SetPiece piece;
    private final RingSpec.Core spec;
    /** The phase that exposes the core (it opens then). */
    private final int phase;

    private final int openingTicks;
    private final int glowTicks;
    private final int tellTicks;
    private final int runTicks;
    private final int cycleTicks;
    private final RingSpec.Bays bays;
    private final int bayEveryTicks;
    private final int bayTellTicks;
    private final int bayGapTicks;

    /** Steps since its opening began; -1 before. */
    private int openTicks;
    /** Steps to the next bay's stream (from the opening's end); counts down while open. */
    private int bayTicks;
    /** The bay told for the next stream; -1 for none (its turn passes). */
    private int bay;
    /** The stream's units still to launch, and the steps to the next one. */
    private int streamLeft;

    private int streamGapTicks;
    /** The bay the running stream comes out of. */
    private int streamBay;

    RingCore(SetPiece piece, BossSpec boss, RingSpec.Core spec) {
        this.piece = piece;
        this.spec = spec;
        phase = boss.rings().orElseThrow().corePhase(boss.phases());
        openingTicks = SimStep.ticks(spec.openingSeconds());
        glowTicks = SimStep.ticks(spec.glowSeconds());
        tellTicks = SimStep.ticks(spec.spiralTellSeconds());
        runTicks = Math.max(1, SimStep.ticks(spec.spiralSeconds()));
        cycleTicks = Math.max(1, SimStep.ticks(spec.cycleSeconds()));
        bays = spec.bays().orElse(null);
        bayEveryTicks =
                bays == null ? 0 : Math.max(1, SimStep.ticks(bays.launcher().everySeconds()));
        bayTellTicks = bays == null ? 0 : SimStep.ticks(bays.tellSeconds());
        bayGapTicks =
                bays == null ? 0 : Math.max(1, SimStep.ticks(bays.launcher().gapSeconds()));
        reset();
    }

    /** Back to the level start (and the boss checkpoint): shut. */
    void reset() {
        openTicks = -1;
        bayTicks = 0;
        bay = -1;
        streamLeft = 0;
        streamGapTicks = 0;
        streamBay = -1;
    }

    /**
     * One engaged step with the ship at (shipX, shipY), the boss in {@code current}: in its phase the opening runs; open,
     * the spiral's tells and the bays' turns and streams (their launches only while {@code firing}).
     */
    void step(int current, double shipX, double shipY, BossActions actions, boolean firing) {
        if (current < phase || piece.partHp(spec.part()) <= 0) {
            return;
        }
        double cx = piece.partX(spec.part());
        double cy = piece.partY(spec.part());
        if (openTicks < 0) {
            openTicks = 0;
            actions.ring(SimEvents.Type.CORE_OPENING, cx, cy, 0);
        } else {
            openTicks++;
        }
        if (openTicks < openingTicks) {
            return;
        }
        int sinceOpen = openTicks - openingTicks;
        if (sinceOpen == 0) {
            actions.ring(SimEvents.Type.CORE_OPEN, cx, cy, 0);
            if (bays != null) {
                bayTicks = Math.max(1, SimStep.ticks(bays.launcher().firstSeconds()));
            }
        }
        if (spec.spiral() >= 0 && sinceOpen % cycleTicks == 0) {
            actions.ring(SimEvents.Type.CORE_SPIRAL_TELL, cx, cy, 0);
        }
        if (bays != null) {
            bays(shipX, shipY, actions, firing);
        }
    }

    private void bays(double shipX, double shipY, BossActions actions, boolean firing) {
        if (streamLeft > 0 && --streamGapTicks <= 0) {
            EnemySpec.Launcher launcher = bays.launcher();
            int place = launcher.stream() - streamLeft;
            streamLeft--;
            streamGapTicks = bayGapTicks;
            if (firing) {
                int n = launcher.stream();
                double spread = launcher.spreadRadians();
                double out = bays.radians(streamBay);
                // Outward through the bay (y up: the bay at (-r sin a, -r cos a) from the centre), spread round it.
                double angle = StrictMath.atan2(-Trig.cos(out), -Trig.sin(out))
                        + (n == 1 ? 0 : -spread / 2 + place * spread / (n - 1));
                actions.bayLaunch(launcher, bayX(streamBay), bayY(streamBay), angle);
            }
        }
        bayTicks--;
        if (bayTicks == bayTellTicks) {
            bay = nearestBay(shipX, shipY);
            if (bay >= 0 && firing) {
                actions.ring(SimEvents.Type.BAY_TELL, bayX(bay), bayY(bay), bay);
            }
        }
        if (bayTicks <= 0) {
            bayTicks = bayEveryTicks;
            if (bay >= 0 && firing) {
                streamBay = bay;
                streamLeft = bays.launcher().stream();
                streamGapTicks = 1;
            }
            bay = -1;
        }
    }

    /** The bay nearest (shipX, shipY) that lies farther than the skip distance from it; -1 for none. */
    private int nearestBay(double shipX, double shipY) {
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        double skip = bays.skipWithinPx();
        for (int k = 0; k < bays.count(); k++) {
            double dx = bayX(k) - shipX;
            double dy = bayY(k) - shipY;
            double d = dx * dx + dy * dy;
            if (d > skip * skip && d < bestDistance) {
                best = k;
                bestDistance = d;
            }
        }
        return best;
    }

    /** Whether the fan attack {@code a}'s glow starts now: its volley {@code ticks} steps away, the core shut. */
    boolean glowDue(BossSpec.Attack attack, int ticks) {
        return attack.pattern() == BossSpec.Pattern.FAN
                && ticks == glowTicks
                && glowTicks > 0
                && attack.parts().contains(spec.part())
                && openTicks < 0;
    }

    /** Whether the core is in its phase and not yet open (untouchable, silent). */
    public boolean opening() {
        return piece.phase() >= phase && (openTicks < 0 || openTicks < openingTicks) && !piece.destroyed();
    }

    /** Whether the core is open (its phase, past its opening). */
    public boolean open() {
        return openTicks >= openingTicks && openTicks >= 0;
    }

    /** The opening's progress, 0 shut to 1 open. */
    public double openShare() {
        if (openTicks < 0) {
            return 0;
        }
        return openingTicks == 0 ? 1 : Math.min(1, (double) openTicks / openingTicks);
    }

    /** Whether part {@code p} is the core and cannot be touched now (in its phase until open). */
    boolean untouchable(int p) {
        return p == spec.part() && opening();
    }

    /** Whether attack {@code a} may fire now: the core's rail only below its HP share; any other attack always. */
    boolean fires(int a) {
        return a != spec.rail()
                || piece.partHp(spec.part())
                        < spec.railBelow()
                                * piece.spec().parts().get(spec.part()).hp();
    }

    /** Whether attack {@code a} runs now: the core's spiral only in its cycle's run; any other attack always. */
    boolean spiralling(int a) {
        if (a != spec.spiral()) {
            return true;
        }
        return open() && spiralOn();
    }

    /** Whether the open core's spiral is in its run now (past its tell, before its rest). */
    public boolean spiralOn() {
        if (!open()) {
            return false;
        }
        int at = (openTicks - openingTicks) % cycleTicks;
        return at >= tellTicks && at < tellTicks + runTicks;
    }

    /** Whether the open core's spiral is in its tell now (the reactor brightening). */
    public boolean spiralTelling() {
        return open() && (openTicks - openingTicks) % cycleTicks < tellTicks;
    }

    /** The bay whose doors are open now (its tell, then its stream); -1 for none. */
    public int bayOpen() {
        if (streamLeft > 0) {
            return streamBay;
        }
        return bay >= 0 && bayTicks <= bayTellTicks ? bay : -1;
    }

    /** Its spec. */
    public RingSpec.Core spec() {
        return spec;
    }

    /** Its bays' count; 0 without bays. */
    public int bayCount() {
        return bays == null ? 0 : bays.count();
    }

    /** Bay {@code k}'s centre, px from the left. */
    public double bayX(int k) {
        return piece.partX(spec.part()) - bays.radius() * Trig.sin(bays.radians(k));
    }

    /** Bay {@code k}'s centre, px up. */
    public double bayY(int k) {
        return piece.partY(spec.part()) - bays.radius() * Trig.cos(bays.radians(k));
    }

    void addTo(StateHash hash) {
        hash.add(openTicks)
                .add(bayTicks)
                .add(bay)
                .add(streamLeft)
                .add(streamGapTicks)
                .add(streamBay);
    }
}
