package vanguard.sim;

import java.util.List;

/**
 * M8 part H, step H1: a coiled arena boss (design/enemies/bosses/abyssal-maw; {@link CoilSpec}), stepped by its {@link
 * SetPiece} on the simulation's real steps (the level clock is halted in its arena), in the style of the {@link
 * BreachArena}. Its centre is its head's.
 *
 * <p><b>The body.</b> Its segments lie along the coil's path round the arena's edges ({@link #segmentX(int)}); each
 * belongs to the left, bottom or right run (or the top: no sweep) by its inward normal. They are armoured ({@link
 * #touches}: shots glance, {@link #column}: the beam stops; a segment carrying an open implant lets shots through to
 * it) and a wall ({@link #push}): the ship and the escort keep the body's clearance inside its inner flanks, a sweep's
 * bight pushing them along; enemy bullets pass.
 *
 * <p><b>The script.</b> It arrives (the bar) under the water ({@code sub}) and rises over its intro; then it is up (the
 * head, the sacs and the implants on the play plane) and engages. Each boss phase runs its clocks from its start: the
 * sacs' breaths (its {@code RING} attack, one sac at a time after a swell; a dead sac's turn passes silently), the
 * throat's fan (its {@code FAN} attack after the jaw's glow, the jaw open its open time), the fan walls (the wall's
 * attack after its glow, the jaw open through it; the gaps placed beside the ship, chaining), the sweeps (the phase's
 * sides in turn), the implants (from their phase on, in turn: tell, open, bursts after each arc) and the shed Eel
 * Swarms (at most the phase's alive). Once, in the dive's phase, as the head falls below its share it dives: every
 * clock waits, the walls go, the head and sacs go {@code sub}; the shadow at the ship's x, the lunge up the column (on
 * the play plane again; the jaw open), its body a wall in the column, the slide back; then the clocks start over.
 *
 * <p>Its events are raised in {@link #act} through the {@link SetPiece.BossActions}. Everything is allocated here;
 * stepping does not allocate. Its state is hashed with its boss.
 */
public final class CoilArena implements Hashed {
    /** Where the dive is. */
    public enum DiveState {
        /** Not yet (or a boss without a dive). */
        READY,
        /** The coil slides off the edges into the dark ({@code sub} from its middle). */
        SLIDE,
        /** The arena open and quiet. */
        QUIET,
        /** The shadow runs up the column (the lunge's tell). */
        SHADOW,
        /** The head races up the column (on the play plane, the jaw open). */
        LUNGE,
        /** Its body stays in the column (a wall). */
        BODY,
        /** The coil slides back round the edges (the walls return at its end). */
        RETURN,
        /** Over (once in the fight). */
        DONE
    }

    /** Where a sweep is. */
    public enum SweepState {
        IDLE,
        TELL,
        SWING,
        HOLD,
        BACK
    }

    /** Where an implant is. */
    public enum ImplantState {
        SHUT,
        TELL,
        OPEN
    }

    /** A segment on no side run (the top's): no sweep there. */
    public static final int TOP = -1;
    /** The most a wall moves the ship or the escort in a step, px. */
    static final double PUSH_STEP = 8;
    /** What a sweep or the lunge caught: the ship. */
    public static final int STRUCK_SHIP = 1;
    /** ... the escort (Rook or the Bulwark). */
    public static final int STRUCK_ESCORT = 2;
    /** {@link SetPiece.BossActions#coilStrike}'s kinds: a swinging bight. */
    static final int SWEEP = 0;
    /** ... the lunge. */
    static final int LUNGE = 1;
    /** The head bursts up from here, px up (the bottom edge). */
    static final double LUNGE_FROM = 0;
    /** A fan wall's gap is placed at the ship's height, but never less than this below the throat, px. */
    static final double LEAST_DROP = 40;

    private static final int EVENTS = 16;
    private static final CoilSpec.Side[] SIDES = CoilSpec.Side.values();

    private final SetPiece piece;
    private final BossSpec boss;
    private final CoilSpec spec;
    private final CoilSpec.Body body;
    private final CoilSpec.Sweeps sweeps;
    /** Null without them. */
    private final CoilSpec.Wall wall;

    private final CoilSpec.Dive dive;
    private final CoilSpec.Implants implantSpec;
    private final CoilSpec.Shed shed;
    private final List<BossSpec.Spot> spots;
    /** The head: the first vital part (else the first). */
    private final int head;

    private final double headHp;
    private final int[] sacs;
    /** The implants' parts, and per part its index among them (-1 for none). */
    private final int[] implants;

    private final int[] partImplant;
    /** The throat: the head's {@code while: open} spot's offset (0, 0 without one). */
    private final double throatDx;

    private final double throatDy;
    /** The head at rest. */
    private final double restX;

    private final double restY;
    // The segments at rest: place, inward normal, side run, heading; and now.
    private final double[] segRestX;
    private final double[] segRestY;
    private final double[] normalX;
    private final double[] normalY;
    private final int[] side;
    private final double[] angle;
    private final double[] segX;
    private final double[] segY;
    /** Per side, its run's segments in path order. */
    private final int[][] runs;
    /** The bight's half length along its run, px. */
    private final double bightHalf;
    /** Per implant, the segment it rides and its offset from it. */
    private final int[] implantSeg;

    private final double[] implantDx;
    private final double[] implantDy;
    /** Per boss phase, its attacks by role (-1 for none), and whether it fires the walls and the implants' bursts. */
    private final int[] ringAttack;

    private final int[] fanAttack;
    private final boolean[] wallOn;
    private final boolean[] burstOn;

    private final int introTicks;
    private final int swellTicks;
    private final int jawGlowTicks;
    private final int jawOpenTicks;
    private final int wallGlowTicks;
    private final int tellTicks;
    private final int swingTicks;
    private final int holdTicks;
    private final int backTicks;
    private final int slideTicks;
    private final int quietTicks;
    private final int shadowTicks;
    private final int lungeTicks;
    private final int bodyTicks;
    private final int returnTicks;
    private final int implantTellTicks;
    private final int implantOpenTicks;
    private final int arcTicksMax;
    private final int shedTellTicks;

    // Where the ship is (set by the sortie before each step).
    private double shipX;
    private double shipY;

    private boolean arrived;
    /** Steps since it arrived. */
    private int ticks;
    /** Whether the intro is over: up on the play plane, engaged. */
    private boolean up;
    /** The boss phase its clocks run for; -1 before the first. */
    private int lastPhase;

    private double headX;
    private double headY;
    // The jaw: steps to the throat's next glow, steps since it opened (-1 shut), whether it opened for a wall.
    private int jawTimer;
    private int jawTicks;
    private boolean jawForWall;
    // The breaths: steps to the next ring, the turn, the sac swelling now (-1), whether this turn's swell came.
    private int breathTimer;
    private int breathTurn;
    private int swellPart;
    private boolean swelled;
    // The walls: steps to the next glow, steps into the glow (-1 none), the last wall's gaps at the ship's height.
    private int wallTimer;
    private int wallGlow;
    private double lastGap;
    private double secondGap;
    // The sweeps: steps to the next tell, the turn, the state, steps into it, the side, the bight's centre, whom it
    // hit.
    private int sweepTimer;
    private int sweepTurn;
    private SweepState sweepState;
    private int sweepTicks;
    private int sweepSide;
    private int sweepCentre;
    private double sweepU;
    private int sweepCaught;
    // The dive: its state, steps into it, the column's x, whom the lunge hit.
    private DiveState diveState;
    private int diveTicks;
    private double columnX;
    private int lungeCaught;
    // The implants: whose turn, the state, steps into it, steps to the next arc, steps into the arc (-1), the burst.
    private int implantTurn;
    private ImplantState implantState;
    private int implantTicks;
    private int arcTimer;
    private int arcTicks;
    private int burstLeft;
    private int burstGap;
    // The sheds: steps to the next, the turn, steps into the tell (-1 none), its point, the shed units' chains.
    private int shedTimer;
    private int shedTurn;
    private int shedTell;
    private int shedPoint;
    private final int[] shedSerials;
    // In this step (set by step, used by act): what fires, the events to raise.
    private int ringPart;
    private boolean fanFire;
    private boolean wallFire;
    private boolean burstShot;
    private int shedRelease;
    private boolean diveCue;
    private int eventCount;
    private final SimEvents.Type[] eventTypes = new SimEvents.Type[EVENTS];
    private final double[] eventX = new double[EVENTS];
    private final double[] eventY = new double[EVENTS];
    private final int[] eventValues = new int[EVENTS];

    CoilArena(SetPiece piece, BossSpec boss, SetPieceSpec setPiece) {
        this.piece = piece;
        this.boss = boss;
        spec = boss.coil().orElseThrow();
        body = spec.body();
        sweeps = spec.sweeps();
        wall = spec.wall().orElse(null);
        dive = spec.dive().orElse(null);
        implantSpec = spec.implants().orElse(null);
        shed = spec.shed().orElse(null);
        spots = boss.arena().orElseThrow().spots();
        int parts = setPiece.parts().size();
        int vital = -1;
        for (int p = parts - 1; p >= 0; p--) {
            vital = setPiece.parts().get(p).vital() ? p : vital;
        }
        head = Math.max(0, vital);
        headHp = setPiece.parts().get(head).hp();
        sacs = toArray(spec.sacs(), parts, setPiece.slug() + ": a gill sac is one of its parts");
        implants = implantSpec == null
                ? new int[0]
                : toArray(implantSpec.parts(), parts, setPiece.slug() + ": an implant is one of its parts");
        partImplant = new int[parts];
        java.util.Arrays.fill(partImplant, -1);
        for (int i = 0; i < implants.length; i++) {
            if (implants[i] == head) {
                throw new IllegalArgumentException(setPiece.slug() + ": the head is no implant");
            }
            partImplant[implants[i]] = i;
        }
        double tdx = 0;
        double tdy = 0;
        for (int i = spots.size() - 1; i >= 0; i--) {
            if (spots.get(i).part() == head && spots.get(i).whileOpen()) {
                tdx = spots.get(i).dx();
                tdy = spots.get(i).dy();
            }
        }
        throatDx = tdx;
        throatDy = tdy;
        restX = boss.x();
        restY = boss.hoverY();
        int n = body.segments();
        segRestX = new double[n];
        segRestY = new double[n];
        normalX = new double[n];
        normalY = new double[n];
        side = new int[n];
        angle = new double[n];
        segX = new double[n];
        segY = new double[n];
        layOut();
        runs = new int[SIDES.length][];
        for (int s = 0; s < SIDES.length; s++) {
            int count = 0;
            for (int i = 0; i < n; i++) {
                count += side[i] == s ? 1 : 0;
            }
            runs[s] = new int[count];
            for (int i = 0, k = 0; i < n; i++) {
                if (side[i] == s) {
                    runs[s][k++] = i;
                }
            }
        }
        bightHalf = (sweeps.segments() / 2.0 + 0.5) * body.spacing();
        implantSeg = new int[implants.length];
        implantDx = new double[implants.length];
        implantDy = new double[implants.length];
        for (int i = 0; i < implants.length; i++) {
            int p = implants[i];
            double x = restX + restOffsetX(setPiece, p);
            double y = restY + restOffsetY(setPiece, p);
            int nearest = 0;
            for (int k = 1; k < n; k++) {
                if (distance2(x, y, segRestX[k], segRestY[k]) < distance2(x, y, segRestX[nearest], segRestY[nearest])) {
                    nearest = k;
                }
            }
            implantSeg[i] = nearest;
            implantDx[i] = x - segRestX[nearest];
            implantDy[i] = y - segRestY[nearest];
        }
        int phases = boss.phases().size();
        ringAttack = new int[phases];
        fanAttack = new int[phases];
        wallOn = new boolean[phases];
        burstOn = new boolean[phases];
        for (int f = 0; f < phases; f++) {
            ringAttack[f] = -1;
            fanAttack[f] = -1;
            for (int a : boss.phases().get(f).attacks()) {
                BossSpec.Attack attack = boss.attacks().get(a);
                if (wall != null && a == wall.attack()) {
                    wallOn[f] = true;
                } else if (implantSpec != null && a == implantSpec.attack()) {
                    burstOn[f] = true;
                } else if (attack.pattern() == BossSpec.Pattern.RING && ringAttack[f] < 0) {
                    ringAttack[f] = a;
                } else if (attack.pattern() == BossSpec.Pattern.FAN && fanAttack[f] < 0) {
                    fanAttack[f] = a;
                }
            }
        }
        introTicks = Math.max(1, SimStep.ticks(spec.introSeconds()));
        swellTicks = SimStep.ticks(spec.swellSeconds());
        jawGlowTicks = SimStep.ticks(spec.jaw().glowSeconds());
        jawOpenTicks = Math.max(1, SimStep.ticks(spec.jaw().openSeconds()));
        wallGlowTicks = wall == null ? 0 : SimStep.ticks(wall.glowSeconds());
        tellTicks = Math.max(1, SimStep.ticks(sweeps.tellSeconds()));
        swingTicks = Math.max(1, SimStep.ticks(sweeps.swingSeconds()));
        holdTicks = SimStep.ticks(sweeps.holdSeconds());
        backTicks = Math.max(1, SimStep.ticks(sweeps.backSeconds()));
        slideTicks = dive == null ? 1 : Math.max(1, SimStep.ticks(dive.slideSeconds()));
        quietTicks = dive == null ? 0 : SimStep.ticks(dive.quietSeconds());
        shadowTicks = dive == null ? 1 : Math.max(1, SimStep.ticks(dive.shadowSeconds()));
        lungeTicks = dive == null ? 1 : Math.max(1, SimStep.ticks(dive.lungeSeconds()));
        bodyTicks = dive == null ? 0 : SimStep.ticks(dive.bodySeconds());
        returnTicks = dive == null ? 1 : Math.max(1, SimStep.ticks(dive.returnSeconds()));
        implantTellTicks = implantSpec == null ? 0 : SimStep.ticks(implantSpec.tellSeconds());
        implantOpenTicks = implantSpec == null ? 1 : Math.max(1, SimStep.ticks(implantSpec.openSeconds()));
        arcTicksMax = implantSpec == null ? 0 : SimStep.ticks(implantSpec.arcSeconds());
        shedTellTicks = shed == null ? 0 : SimStep.ticks(shed.tellSeconds());
        shedSerials = new int[CoilSpec.Shed.MOST];
        reset();
    }

    private static int[] toArray(List<Integer> list, int parts, String problem) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
            if (array[i] < 0 || array[i] >= parts) {
                throw new IllegalArgumentException(problem);
            }
        }
        return array;
    }

    private double restOffsetX(SetPieceSpec setPiece, int p) {
        return boss.poses().isEmpty()
                ? setPiece.parts().get(p).dx()
                : boss.poses().getFirst().offsets().get(p).dx();
    }

    private double restOffsetY(SetPieceSpec setPiece, int p) {
        return boss.poses().isEmpty()
                ? setPiece.parts().get(p).dy()
                : boss.poses().getFirst().offsets().get(p).dy();
    }

    private static double distance2(double ax, double ay, double bx, double by) {
        return (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
    }

    /**
     * The segments along the path from its first point, {@code spacing} apart (on straight past its last point): each
     * turned along it, its normal toward the field's centre, its run by that normal.
     */
    private void layOut() {
        List<BossSpec.Point> path = body.path();
        double centreX = PlayField.WIDTH / 2.0;
        double centreY = PlayField.HEIGHT / 2.0;
        int leg = 0;
        double legStart = 0;
        for (int i = 0; i < segRestX.length; i++) {
            double d = i * body.spacing();
            while (leg < path.size() - 2 && d > legStart + legLength(path, leg)) {
                legStart += legLength(path, leg);
                leg++;
            }
            BossSpec.Point from = path.get(leg);
            BossSpec.Point to = path.get(leg + 1);
            double length = legLength(path, leg);
            double ux = length > 0 ? (to.x() - from.x()) / length : 1;
            double uy = length > 0 ? (to.y() - from.y()) / length : 0;
            double x = from.x() + ux * (d - legStart);
            double y = from.y() + uy * (d - legStart);
            segRestX[i] = x;
            segRestY[i] = y;
            angle[i] = StrictMath.atan2(uy, ux);
            double nx = -uy;
            double ny = ux;
            if (nx * (centreX - x) + ny * (centreY - y) < 0) {
                nx = -nx;
                ny = -ny;
            }
            normalX[i] = nx;
            normalY[i] = ny;
            if (Math.abs(nx) >= Math.abs(ny)) {
                side[i] = nx > 0 ? CoilSpec.Side.LEFT.ordinal() : CoilSpec.Side.RIGHT.ordinal();
            } else {
                side[i] = ny > 0 ? CoilSpec.Side.BOTTOM.ordinal() : TOP;
            }
        }
    }

    private static double legLength(List<BossSpec.Point> path, int leg) {
        BossSpec.Point from = path.get(leg);
        BossSpec.Point to = path.get(leg + 1);
        return Math.hypot(to.x() - from.x(), to.y() - from.y());
    }

    /** Back to the level start: not arrived, under the water, every clock at rest. */
    void reset() {
        arrived = false;
        ticks = 0;
        up = false;
        lastPhase = -1;
        headX = restX;
        headY = restY;
        jawTimer = 0;
        jawTicks = -1;
        jawForWall = false;
        breathTimer = 0;
        breathTurn = 0;
        swellPart = -1;
        swelled = false;
        wallTimer = 0;
        wallGlow = -1;
        lastGap = Double.NaN;
        secondGap = Double.NaN;
        sweepTimer = 0;
        sweepTurn = 0;
        sweepState = SweepState.IDLE;
        sweepTicks = 0;
        sweepSide = 0;
        sweepCentre = -1;
        sweepU = 0;
        sweepCaught = 0;
        diveState = DiveState.READY;
        diveTicks = 0;
        columnX = restX;
        lungeCaught = 0;
        implantTurn = 0;
        implantState = ImplantState.SHUT;
        implantTicks = 0;
        arcTimer = 0;
        arcTicks = -1;
        burstLeft = 0;
        burstGap = 0;
        shedTimer = 0;
        shedTurn = 0;
        shedTell = -1;
        shedPoint = -1;
        java.util.Arrays.fill(shedSerials, -1);
        shipX = restX;
        shipY = 0;
        clearStep();
        place();
    }

    private void clearStep() {
        ringPart = -1;
        fanFire = false;
        wallFire = false;
        burstShot = false;
        shedRelease = -1;
        diveCue = false;
        eventCount = 0;
    }

    /** It was destroyed: nothing fires, no sweep, no wall (its place stays for the death). */
    void died() {
        clearStep();
        sweepState = SweepState.IDLE;
        jawTicks = -1;
        wallGlow = -1;
        swellPart = -1;
        shedTell = -1;
        burstLeft = 0;
        arcTicks = -1;
        place();
    }

    /** Where the ship is now: a bight, a gap, the column and a shed point read it. */
    void targets(double ship1X, double ship1Y) {
        shipX = ship1X;
        shipY = ship1Y;
    }

    /** It arrives (the bar): under the water, rising over its intro. */
    void arrive() {
        arrived = true;
        ticks = 0;
    }

    /** Whether its intro is over (it is up): it engages then. */
    boolean engages() {
        return arrived && up;
    }

    /** One step in boss phase {@code bossPhase}: the intro, the clocks, the dive, the body's place. */
    void step(int bossPhase) {
        clearStep();
        if (!arrived) {
            return;
        }
        ticks++;
        if (!up) {
            if (ticks < introTicks) {
                return;
            }
            up = true;
            layers(Layer.AIR, true);
        }
        if (bossPhase != lastPhase) {
            // A phase's clocks start in this step and run from the next (each first comes its interval later).
            phaseStarted(bossPhase);
            place();
            return;
        }
        if (diving()) {
            stepDive();
        } else if (dive != null
                && diveState == DiveState.READY
                && bossPhase == dive.phase()
                && piece.partHp(head) < dive.below() * headHp) {
            startDive();
        } else {
            stepJaw(bossPhase);
            stepWall(bossPhase);
            stepBreath(bossPhase);
            stepSweep(bossPhase);
            stepImplants(bossPhase);
            stepShed();
        }
        place();
    }

    /** A boss phase starts: its clocks from now; the implants start their turns in their first phase. */
    private void phaseStarted(int bossPhase) {
        boolean implantsBefore = implantSpec != null && lastPhase >= implantSpec.phase();
        lastPhase = bossPhase;
        clocks(bossPhase);
        sweepTurn = 0;
        breathTurn = 0;
        if (implantSpec != null && bossPhase >= implantSpec.phase() && !implantsBefore) {
            implantTurn = 0;
            startImplantTell();
        }
    }

    /** The phase's clocks from now (a phase's start, the dive's end): each first comes one interval on. */
    private void clocks(int bossPhase) {
        jawTicks = -1;
        jawForWall = false;
        int fan = fanAttack[bossPhase];
        jawTimer = fan < 0 ? 0 : Math.max(1, interval(fan) - jawGlowTicks);
        int ring = ringAttack[bossPhase];
        breathTimer = ring < 0 ? 0 : interval(ring);
        swellPart = -1;
        swelled = false;
        wallGlow = -1;
        wallTimer = wallOn[bossPhase] ? Math.max(1, interval(wall.attack()) - wallGlowTicks) : 0;
        double every = sweeps.every(bossPhase);
        sweepTimer = every > 0 ? Math.max(1, SimStep.ticks(every)) : 0;
        double shedEvery = shed == null ? 0 : shed.every(bossPhase);
        shedTimer = shedEvery > 0 ? Math.max(1, SimStep.ticks(shedEvery)) : 0;
    }

    /** Attack {@code a}'s interval, steps (at least one). */
    private int interval(int a) {
        return Math.max(1, SimStep.ticks(boss.attacks().get(a).gun().intervalSeconds()));
    }

    /** The throat's fan: the glow (the jaw opens), the fan at its end, the jaw shut after its open time. */
    private void stepJaw(int bossPhase) {
        int fan = fanAttack[bossPhase];
        if (fan >= 0 && --jawTimer <= 0) {
            jawTimer = interval(fan);
            jawTicks = 0;
            jawForWall = false;
            event(SimEvents.Type.THROAT_GLOW, throatX(), throatY(), 0);
        } else if (jawTicks >= 0 && !jawForWall) {
            jawTicks++;
        }
        if (jawTicks >= 0 && !jawForWall) {
            if (jawTicks == jawGlowTicks) {
                fanFire = true;
            }
            if (jawTicks >= jawOpenTicks) {
                jawTicks = -1;
            }
        }
    }

    /** The fan walls: the glow (the jaw open), the wall at its end (the jaw shuts). */
    private void stepWall(int bossPhase) {
        if (!wallOn[bossPhase]) {
            wallGlow = -1;
            return;
        }
        if (--wallTimer <= 0) {
            wallTimer = interval(wall.attack());
            wallGlow = 0;
            jawTicks = 0;
            jawForWall = true;
            event(SimEvents.Type.THROAT_GLOW, throatX(), throatY(), 1);
        } else if (wallGlow >= 0) {
            wallGlow++;
            jawTicks = wallGlow;
        }
        if (wallGlow >= 0 && wallGlow >= wallGlowTicks) {
            wallFire = true;
            wallGlow = -1;
            jawTicks = -1;
            jawForWall = false;
        }
    }

    /** The sacs breathe in turn: the swell, then the ring; a dead sac's turn passes silently. */
    private void stepBreath(int bossPhase) {
        int ring = ringAttack[bossPhase];
        if (ring < 0) {
            swellPart = -1;
            return;
        }
        List<Integer> parts = boss.attacks().get(ring).parts();
        int count = parts.isEmpty() ? sacs.length : parts.size();
        if (count == 0) {
            return;
        }
        int sac = parts.isEmpty() ? sacs[breathTurn % count] : parts.get(breathTurn % count);
        breathTimer--;
        if (!swelled && breathTimer <= swellTicks) {
            swelled = true;
            if (!piece.partWrecked(sac)) {
                swellPart = sac;
                event(SimEvents.Type.SAC_SWELL, piece.partX(sac), piece.partY(sac), sac);
            }
        }
        if (breathTimer <= 0) {
            if (!piece.partWrecked(sac)) {
                ringPart = sac;
            }
            breathTurn = (breathTurn + 1) % count;
            breathTimer = interval(ring);
            swellPart = -1;
            swelled = false;
        }
    }

    /** The sweeps: a tell when its clock comes round (and none runs), then the swing, the hold and the way back. */
    private void stepSweep(int bossPhase) {
        if (sweepState != SweepState.IDLE) {
            sweepTicks++;
            switch (sweepState) {
                case TELL -> {
                    if (sweepTicks >= tellTicks) {
                        sweepState = SweepState.SWING;
                        sweepTicks = 0;
                        event(SimEvents.Type.COIL_SWEEP, segRestX[sweepCentre], segRestY[sweepCentre], sweepSide);
                    }
                }
                case SWING -> {
                    if (sweepTicks >= swingTicks) {
                        sweepState = holdTicks > 0 ? SweepState.HOLD : SweepState.BACK;
                        sweepTicks = 0;
                    }
                }
                case HOLD -> {
                    if (sweepTicks >= holdTicks) {
                        sweepState = SweepState.BACK;
                        sweepTicks = 0;
                    }
                }
                case BACK -> {
                    if (sweepTicks >= backTicks) {
                        sweepState = SweepState.IDLE;
                        sweepTicks = 0;
                    }
                }
                case IDLE -> {}
            }
        }
        double every = sweeps.every(bossPhase);
        if (!(every > 0)) {
            return;
        }
        if (sweepTimer > 0) {
            sweepTimer--;
        }
        if (sweepTimer <= 0 && sweepState == SweepState.IDLE) {
            startSweep(bossPhase, every);
        }
    }

    /** A sweep's tell: the phase's next side, the bight centred on its run's segment nearest the ship. */
    private void startSweep(int bossPhase, double every) {
        List<CoilSpec.Side> order = sweeps.orders().get(bossPhase);
        CoilSpec.Side next = order.get(sweepTurn % order.size());
        sweepTurn++;
        sweepTimer = Math.max(1, SimStep.ticks(every));
        int[] run = runs[next.ordinal()];
        if (run.length == 0) {
            return;
        }
        boolean bottom = next == CoilSpec.Side.BOTTOM;
        double target = bottom ? sweeps.across().apply(shipX) : sweeps.along().apply(shipY);
        int best = run[0];
        for (int k = 1; k < run.length; k++) {
            if (Math.abs(along(run[k], bottom) - target) < Math.abs(along(best, bottom) - target)) {
                best = run[k];
            }
        }
        sweepSide = next.ordinal();
        sweepCentre = best;
        sweepU = along(best, bottom);
        sweepState = SweepState.TELL;
        sweepTicks = 0;
        sweepCaught = 0;
        event(SimEvents.Type.COIL_SWEEP_TELL, segRestX[best], segRestY[best], sweepSide);
    }

    /** Segment {@code i}'s place along its run at rest: its x on the bottom, else its y. */
    private double along(int i, boolean bottom) {
        return bottom ? segRestX[i] : segRestY[i];
    }

    /** The implants in turn: the tell, open (the arcs and the bursts), then the next one's tell. */
    private void stepImplants(int bossPhase) {
        if (implantSpec == null || bossPhase < implantSpec.phase()) {
            return;
        }
        implantTicks++;
        switch (implantState) {
            case SHUT -> startImplantTell();
            case TELL -> {
                if (implantTicks >= implantTellTicks) {
                    implantState = ImplantState.OPEN;
                    implantTicks = 0;
                    event(SimEvents.Type.IMPLANT_OPEN, implantX(implantTurn), implantY(implantTurn), implantTurn);
                    startArc();
                    burstLeft = 0;
                }
            }
            case OPEN -> {
                if (implantTicks >= implantOpenTicks) {
                    event(SimEvents.Type.IMPLANT_SHUT, implantX(implantTurn), implantY(implantTurn), implantTurn);
                    implantTurn = (implantTurn + 1) % implants.length;
                    burstLeft = 0;
                    arcTicks = -1;
                    startImplantTell();
                    return;
                }
                if (--arcTimer <= 0) {
                    startArc();
                } else if (arcTicks >= 0) {
                    arcTicks++;
                }
            }
        }
        if (implantState != ImplantState.OPEN) {
            return;
        }
        if (arcTicks >= 0 && arcTicks >= arcTicksMax) {
            arcTicks = -1;
            if (burstOn[bossPhase]) {
                burstLeft = boss.attacks().get(implantSpec.attack()).gun().burst();
                burstGap = 0;
            }
        }
        if (burstLeft > 0 && --burstGap <= 0) {
            burstShot = true;
            burstLeft--;
            double gap = boss.attacks().get(implantSpec.attack()).burstGapSeconds();
            burstGap = Math.max(1, SimStep.ticks(gap > 0 ? gap : EnemyGun.BURST_GAP_SECONDS));
        }
    }

    private void startImplantTell() {
        implantState = ImplantState.TELL;
        implantTicks = 0;
        arcTicks = -1;
        burstLeft = 0;
        event(SimEvents.Type.IMPLANT_SPARK, implantX(implantTurn), implantY(implantTurn), implantTurn);
    }

    private void startArc() {
        arcTimer = interval(implantSpec.attack());
        arcTicks = 0;
        event(SimEvents.Type.IMPLANT_ARC, implantX(implantTurn), implantY(implantTurn), implantTurn);
    }

    /** The shed's clock and its tell (the tell starts in {@link #act}, which knows how many live). */
    private void stepShed() {
        if (shed == null) {
            return;
        }
        if (shedTimer > 0) {
            shedTimer--;
        }
        if (shedTell >= 0 && ++shedTell >= shedTellTicks) {
            shedRelease = shedPoint;
            shedTell = -1;
            double every = shed.every(lastPhase);
            shedTimer = every > 0 ? Math.max(1, SimStep.ticks(every)) : 0;
        }
    }

    /** The dive starts: the roar, every clock waits (what was under way ends), the walls go. */
    private void startDive() {
        diveState = DiveState.SLIDE;
        diveTicks = 0;
        diveCue = true;
        sweepState = SweepState.IDLE;
        sweepTicks = 0;
        jawTicks = -1;
        jawForWall = false;
        wallGlow = -1;
        swellPart = -1;
        swelled = false;
        shedTell = -1;
        event(SimEvents.Type.MAW_DIVE, headX, headY, 0);
    }

    private void stepDive() {
        diveTicks++;
        switch (diveState) {
            case SLIDE -> {
                if (2 * diveTicks >= slideTicks && piece.partLayer(head) != Layer.SUB) {
                    layers(Layer.SUB, false);
                }
                if (diveTicks >= slideTicks) {
                    enterDive(DiveState.QUIET);
                }
            }
            case QUIET -> {
                if (diveTicks >= quietTicks) {
                    enterDive(DiveState.SHADOW);
                    columnX = dive.column().apply(shipX);
                    event(SimEvents.Type.LUNGE_TELL, columnX, LUNGE_FROM, (int) Math.round(columnX));
                }
            }
            case SHADOW -> {
                if (diveTicks >= shadowTicks) {
                    enterDive(DiveState.LUNGE);
                    layers(Layer.AIR, false);
                    lungeCaught = 0;
                    event(SimEvents.Type.LUNGE, columnX, LUNGE_FROM, 0);
                }
            }
            case LUNGE -> {
                if (diveTicks >= lungeTicks) {
                    enterDive(DiveState.BODY);
                }
            }
            case BODY -> {
                if (diveTicks >= bodyTicks) {
                    enterDive(DiveState.RETURN);
                    event(SimEvents.Type.COIL_RETURN, headX, headY, 0);
                }
            }
            case RETURN -> {
                if (diveTicks >= returnTicks) {
                    enterDive(DiveState.DONE);
                    clocks(lastPhase);
                }
            }
            case READY, DONE -> {}
        }
    }

    private void enterDive(DiveState next) {
        diveState = next;
        diveTicks = 0;
    }

    /** Whether boss phase {@code bossPhase} must not end yet: it is the dive's and its dive is not over. */
    boolean holdsPhase(int bossPhase) {
        return dive != null && bossPhase == dive.phase() && diveState != DiveState.DONE && !piece.destroyed();
    }

    /** Whether the dive runs (from the slide to the end of the way back). */
    public boolean diving() {
        return diveState != DiveState.READY && diveState != DiveState.DONE;
    }

    /** The head and the sacs (and, {@code all}, the implants) onto {@code layer}. */
    private void layers(Layer layer, boolean all) {
        SlamArena arena = piece.slamArena();
        arena.lift(head, layer);
        for (int sac : sacs) {
            arena.lift(sac, layer);
        }
        if (all) {
            for (int implant : implants) {
                arena.lift(implant, layer);
            }
        }
    }

    /** The head's and the segments' places now. */
    private void place() {
        switch (diveState) {
            case LUNGE -> {
                headX = columnX;
                headY = LUNGE_FROM + (restY - LUNGE_FROM) * Math.min(1, (double) diveTicks / lungeTicks);
            }
            case BODY -> {
                headX = columnX;
                headY = restY;
            }
            case RETURN -> {
                double share = Math.min(1, (double) diveTicks / returnTicks);
                headX = columnX + (restX - columnX) * share;
                headY = restY;
            }
            default -> {
                headX = restX;
                headY = restY;
            }
        }
        for (int i = 0; i < segX.length; i++) {
            double offset = side[i] == TOP ? 0 : offset(side[i], along(i, side[i] == CoilSpec.Side.BOTTOM.ordinal()));
            segX[i] = segRestX[i] + normalX[i] * offset;
            segY[i] = segRestY[i] + normalY[i] * offset;
        }
    }

    /** How far the bight on {@code sideIndex}'s run reaches in at {@code u} along it now, px (0 off the bight). */
    private double offset(int sideIndex, double u) {
        if (sweepState == SweepState.IDLE || sweepState == SweepState.TELL || sideIndex != sweepSide) {
            return 0;
        }
        double t = (u - sweepU) / bightHalf;
        if (Math.abs(t) >= 1) {
            return 0;
        }
        double reach = sideIndex == CoilSpec.Side.BOTTOM.ordinal() ? sweeps.bottomReach() : sweeps.reach();
        return reach * sweepShare() * (0.5 + 0.5 * Trig.cos(StrictMath.PI * t));
    }

    /** How far the bight has swung in, 0–1. */
    public double sweepShare() {
        return switch (sweepState) {
            case SWING -> Math.min(1, (double) sweepTicks / swingTicks);
            case HOLD -> 1;
            case BACK -> Math.max(0, 1 - (double) sweepTicks / backTicks);
            case IDLE, TELL -> 0;
        };
    }

    /**
     * One step of its actions after its step (engaged): the step's events, the dive's line, the strikes of a swinging
     * bight and of the lunge, the step's fire (while {@code firing}), the shed's tell and release.
     */
    void act(SetPiece.BossActions actions, boolean firing) {
        for (int e = 0; e < eventCount; e++) {
            actions.coil(eventTypes[e], eventX[e], eventY[e], eventValues[e]);
        }
        eventCount = 0;
        if (diveCue) {
            actions.dive();
        }
        if (sweepState == SweepState.SWING) {
            int hit = actions.coilStrike(this, SWEEP, sweeps.damage(), sweepCaught);
            if (hit != 0) {
                sweepCaught |= hit;
                actions.coil(SimEvents.Type.COIL_STRIKE, segX[sweepCentre], segY[sweepCentre], hit);
            }
        }
        if (diveState == DiveState.LUNGE) {
            int hit = actions.coilStrike(this, LUNGE, dive.damage(), lungeCaught);
            if (hit != 0) {
                lungeCaught |= hit;
                actions.coil(SimEvents.Type.LUNGE_HIT, columnX, headY, hit);
            }
        }
        if (firing) {
            fire(actions);
        }
        if (shed != null) {
            sheds(actions, firing);
        }
    }

    private void fire(SetPiece.BossActions actions) {
        if (ringPart >= 0 && lastPhase >= 0 && ringAttack[lastPhase] >= 0) {
            BossSpec.Attack ring = boss.attacks().get(ringAttack[lastPhase]);
            actions.ring(
                    piece.partX(ringPart),
                    piece.partY(ringPart),
                    ring.count(),
                    ring.gun().bulletSpeed(),
                    ring.gun().damage());
        }
        if (fanFire && lastPhase >= 0 && fanAttack[lastPhase] >= 0) {
            actions.aimed(
                    throatX(),
                    throatY(),
                    boss.attacks().get(fanAttack[lastPhase]).gun());
        }
        if (wallFire) {
            fireWall(actions);
        }
        if (burstShot && implantState == ImplantState.OPEN) {
            actions.aimed(
                    implantX(implantTurn),
                    implantY(implantTurn),
                    boss.attacks().get(implantSpec.attack()).gun());
        }
    }

    /**
     * A fan wall: its orbs straight down from the throat over its spread, its gaps left out (the first beside the ship
     * at its height, chaining with the last wall's; a second on the ship's other side).
     */
    private void fireWall(SetPiece.BossActions actions) {
        EnemyGun gun = boss.attacks().get(wall.attack()).gun();
        int count = Math.max(1, gun.fan());
        double tx = throatX();
        double ty = throatY();
        double drop = Math.max(LEAST_DROP, ty - shipY);
        int first = gap(gun, count, drop, tx, Double.NaN);
        double firstX = orbX(gun, count, first, drop, tx);
        int second = wall.gaps() > 1 ? gap(gun, count, drop, tx, firstX) : -1;
        secondGap = second < 0 ? Double.NaN : orbX(gun, count, second, drop, tx);
        lastGap = firstX;
        actions.coil(SimEvents.Type.FAN_WALL, tx, ty, first);
        for (int k = 0; k < count; k++) {
            if (k != first && k != second) {
                actions.bullet(tx, ty, -StrictMath.PI / 2 + orbAngle(gun, count, k), gun.bulletSpeed(), gun.damage());
            }
        }
    }

    /** Orb {@code k}'s angle from straight down, radians (positive: to the right). */
    private static double orbAngle(EnemyGun gun, int count, int k) {
        return count == 1 ? 0 : -gun.spreadRadians() / 2 + k * gun.spreadRadians() / (count - 1);
    }

    /** Where orb {@code k} crosses the ship's height ({@code drop} below the throat at {@code tx}), px. */
    private static double orbX(EnemyGun gun, int count, int k, double drop, double tx) {
        double angle = orbAngle(gun, count, k);
        return tx + drop * Trig.sin(angle) / Trig.cos(angle);
    }

    /**
     * The orb to leave out: the one whose place at the ship's height is nearest to {@code near}–{@code far} px beside
     * the ship, within {@code chain} px of the last wall's gap, inside the walls; with {@code other} (a second gap's) on
     * the ship's other side from it, without the chain.
     */
    private int gap(EnemyGun gun, int count, double drop, double tx, double other) {
        int best = 0;
        double bestPenalty = Double.POSITIVE_INFINITY;
        double bestTie = Double.POSITIVE_INFINITY;
        double inLeft = body.left() + body.clearance();
        double inRight = body.right() - body.clearance();
        for (int k = 0; k < count; k++) {
            double x = orbX(gun, count, k, drop, tx);
            double off = Math.abs(x - shipX);
            double penalty = off < wall.near() ? 4 * (wall.near() - off) : off > wall.far() ? off - wall.far() : 0;
            if (x < inLeft || x > inRight) {
                penalty += 1000 + Math.max(inLeft - x, x - inRight);
            }
            double tie;
            if (!Double.isNaN(other)) {
                if (Math.signum(x - shipX) == Math.signum(other - shipX)
                        || k == indexNear(other, gun, count, drop, tx)) {
                    penalty += 2000;
                }
                tie = Math.abs(x - shipX);
            } else if (!Double.isNaN(lastGap)) {
                double chained = Math.abs(x - lastGap);
                penalty += Math.max(0, chained - wall.chain());
                tie = chained;
            } else {
                tie = Math.abs(x - PlayField.WIDTH / 2.0);
            }
            if (penalty < bestPenalty || (penalty == bestPenalty && tie < bestTie)) {
                best = k;
                bestPenalty = penalty;
                bestTie = tie;
            }
        }
        return best;
    }

    /** The orb whose place at the ship's height is {@code x}. */
    private static int indexNear(double x, EnemyGun gun, int count, double drop, double tx) {
        int best = 0;
        for (int k = 1; k < count; k++) {
            if (Math.abs(orbX(gun, count, k, drop, tx) - x) < Math.abs(orbX(gun, count, best, drop, tx) - x)) {
                best = k;
            }
        }
        return best;
    }

    /** The shed's tell when its clock has come round and fewer than the phase's most live; the release after it. */
    private void sheds(SetPiece.BossActions actions, boolean firing) {
        if (shedRelease >= 0) {
            if (firing) {
                int serial = actions.shed(shedRelease);
                for (int i = 0; i < shedSerials.length && serial >= 0; i++) {
                    if (shedSerials[i] < 0) {
                        shedSerials[i] = serial;
                        serial = -1;
                    }
                }
            }
            shedRelease = -1;
        }
        if (lastPhase < 0 || diving() || shedTell >= 0 || shedTimer > 0 || !(shed.every(lastPhase) > 0)) {
            return;
        }
        int alive = 0;
        for (int i = 0; i < shedSerials.length; i++) {
            if (shedSerials[i] >= 0 && actions.shedAlive(shedSerials[i])) {
                alive++;
            } else {
                shedSerials[i] = -1;
            }
        }
        if (alive >= shed.most(lastPhase)) {
            return;
        }
        List<CoilSpec.ShedPoint> points = shed.points();
        int nearest = 0;
        for (int k = 1; k < points.size(); k++) {
            if (distance2(shedX(k), shedY(k), shipX, shipY) < distance2(shedX(nearest), shedY(nearest), shipX, shipY)) {
                nearest = k;
            }
        }
        int pick = shedTurn % points.size();
        for (int j = 0; j < points.size(); j++) {
            int k = (shedTurn + j) % points.size();
            if (points.size() == 1 || k != nearest) {
                pick = k;
                break;
            }
        }
        shedTurn = (pick + 1) % points.size();
        shedPoint = pick;
        shedTell = 0;
        actions.coil(SimEvents.Type.SHED_TELL, shedX(pick), shedY(pick), pick);
    }

    private void event(SimEvents.Type type, double x, double y, int value) {
        if (eventCount < EVENTS) {
            eventTypes[eventCount] = type;
            eventX[eventCount] = x;
            eventY[eventCount] = y;
            eventValues[eventCount] = value;
            eventCount++;
        }
    }

    // --- the level's view ----------------------------------------------------------------------

    /**
     * Whether a sweep's swinging bight ({@link #SWEEP}) or the lunge ({@link #LUNGE}) reaches a hull centred at (x, y)
     * now: inside the bight's displaced wall during the swing; in the column (with the wall's clearance) once the head
     * has passed its height.
     */
    boolean reaches(int what, double x, double y) {
        if (what == SWEEP) {
            if (sweepState != SweepState.SWING) {
                return false;
            }
            double clearance = body.clearance();
            return switch (SIDES[sweepSide]) {
                case LEFT -> x < body.left() + clearance + offset(sweepSide, y);
                case RIGHT -> x > body.right() - clearance - offset(sweepSide, y);
                case BOTTOM -> y < body.bottom() + clearance + offset(sweepSide, x);
            };
        }
        return diveState == DiveState.LUNGE
                && Math.abs(x - columnX) < dive.width() / 2 + body.clearance()
                && y <= headY;
    }

    /** Whether the walls round the edges stand now: it is up, not diving and not destroyed. */
    public boolean walled() {
        return up && !diving() && !piece.destroyed();
    }

    /** Whether its body is a wall in the column now (below the lunging head, then the whole column). */
    private boolean columnWalled(double y) {
        return !piece.destroyed() && (diveState == DiveState.BODY || (diveState == DiveState.LUNGE && y <= headY));
    }

    /**
     * Whether a hull centred at (x, y) is inside the coil's wall (or the lunge's column): then {@code out} holds the
     * unit vector to push it along and how far in this step (at most {@link #PUSH_STEP} px), as {@code nx, ny, px}.
     */
    boolean push(double x, double y, double[] out) {
        double dx = 0;
        double dy = 0;
        if (walled()) {
            double clearance = body.clearance();
            double minX = body.left() + clearance + offset(CoilSpec.Side.LEFT.ordinal(), y);
            double maxX = body.right() - clearance - offset(CoilSpec.Side.RIGHT.ordinal(), y);
            double minY = body.bottom() + clearance + offset(CoilSpec.Side.BOTTOM.ordinal(), x);
            double maxY = body.top() - clearance;
            if (x < minX) {
                dx = minX - x;
            } else if (x > maxX) {
                dx = maxX - x;
            }
            if (y < minY) {
                dy = minY - y;
            } else if (y > maxY) {
                dy = maxY - y;
            }
        }
        if (dive != null && columnWalled(y)) {
            double half = dive.width() / 2 + body.clearance();
            if (Math.abs(x - columnX) < half) {
                dx = x < columnX ? columnX - half - x : columnX + half - x;
            }
        }
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (!(distance > 0)) {
            return false;
        }
        out[0] = dx / distance;
        out[1] = dy / distance;
        out[2] = Math.min(distance, PUSH_STEP);
        return true;
    }

    /** Whether a shot of {@code size} at (sx, sy) meets the armoured coil (it glances). */
    boolean touches(Hitbox size, double sx, double sy) {
        if (piece.destroyed() || !up) {
            return false;
        }
        if (diving()) {
            if (dive != null && columnWalled(sy)) {
                return Math.abs(sx - columnX) * 2 < dive.width() + size.width();
            }
            return false;
        }
        Hitbox box = body.box();
        for (int i = 0; i < segX.length; i++) {
            if (!carriesOpenImplant(i) && size.overlaps(sx, sy, box, segX[i], segY[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Where a beam's column (centred on {@code cx}, {@code halfWidth} either side, running up from {@code fromY}) first
     * meets the armoured coil; {@link Double#POSITIVE_INFINITY} for none.
     */
    double column(double cx, double halfWidth, double fromY) {
        if (piece.destroyed() || !up) {
            return Double.POSITIVE_INFINITY;
        }
        if (diving()) {
            if (dive != null
                    && (diveState == DiveState.BODY || diveState == DiveState.LUNGE)
                    && Math.abs(cx - columnX) < dive.width() / 2 + halfWidth
                    && fromY < headY) {
                return fromY;
            }
            return Double.POSITIVE_INFINITY;
        }
        double nearest = Double.POSITIVE_INFINITY;
        Hitbox box = body.box();
        for (int i = 0; i < segX.length; i++) {
            if (!carriesOpenImplant(i)) {
                nearest = Math.min(nearest, PlayerFire.columnContact(cx, halfWidth, fromY, segX[i], segY[i], box));
            }
        }
        return nearest;
    }

    private boolean carriesOpenImplant(int segment) {
        return implantState == ImplantState.OPEN && implants.length > 0 && implantSeg[implantTurn] == segment;
    }

    /**
     * The damage multiplier of a hit on part {@code p} at (hx, hy) by a shot flying along (dirX, dirY) (0, 0 for a
     * blast): a weak spot's on the shot's line ahead ({@code while: open} ones only while the jaw is open), else the
     * part's own (an implant's counts on the head).
     */
    double multiplier(int p, double hx, double hy, double dirX, double dirY, double partMultiplier) {
        for (int i = 0; i < spots.size(); i++) {
            BossSpec.Spot spot = spots.get(i);
            if (spot.part() == p
                    && (!spot.whileOpen() || jawOpen())
                    && SlamArena.ahead(
                            hx - piece.partX(p) - spot.dx(),
                            hy - piece.partY(p) - spot.dy(),
                            dirX,
                            dirY,
                            spot.box().width() / 2,
                            spot.box().height() / 2)) {
                return spot.multiplier();
            }
        }
        return partMultiplier;
    }

    /** The part whose HP a hit on part {@code p} takes: the head's for an implant, else its own. */
    int hpPart(int p) {
        return partImplant[p] >= 0 ? head : p;
    }

    /** Part {@code p}'s index among the implants; -1 for none. */
    public int implant(int p) {
        return partImplant[p];
    }

    /** Whether implant {@code i} is open (its core hittable, firing). */
    public boolean implantOpen(int i) {
        return implantState == ImplantState.OPEN && implantTurn == i && !piece.destroyed();
    }

    /** Whether the head is on the play plane (its body collides): up, and not under the water in the dive. */
    boolean headOnPlane() {
        return up && piece.partLayer(head) == Layer.AIR;
    }

    // --- the escort's view (step H2, {@link MawHazards}) -----------------------------------------------

    /** Whether a sweep's bight is told or swinging now (the escort keeps out of its full reach until it has passed). */
    boolean sweepThreat() {
        return !piece.destroyed() && (sweepState == SweepState.TELL || sweepState == SweepState.SWING);
    }

    /**
     * How far the told or swinging bight reaches in, once fully swung, over a hull of half size {@code half} centred
     * at {@code u} along its run (its x on the bottom, else its y), px; 0 off the bight or with none threatening.
     */
    double bightReach(double u, double half) {
        if (!sweepThreat()) {
            return 0;
        }
        double nearest = Math.clamp(sweepU, u - half, u + half);
        double t = (nearest - sweepU) / bightHalf;
        if (Math.abs(t) >= 1) {
            return 0;
        }
        double reach = sweepSide == CoilSpec.Side.BOTTOM.ordinal() ? sweeps.bottomReach() : sweeps.reach();
        return reach * (0.5 + 0.5 * Trig.cos(StrictMath.PI * t));
    }

    /** Whether the lunge's column is dangerous now: its shadow, the lunge, its body standing in it. */
    boolean columnThreat() {
        return !piece.destroyed()
                && dive != null
                && (diveState == DiveState.SHADOW || diveState == DiveState.LUNGE || diveState == DiveState.BODY);
    }

    /** The gap a hull's centre keeps to the coil's inner flanks (and the column's sides), px. */
    double clearance() {
        return body.clearance();
    }

    /** The left wall's inner limit for a hull's centre at rest (the bight's swing aside), px. */
    double innerLeft() {
        return body.left() + body.clearance();
    }

    double innerRight() {
        return body.right() - body.clearance();
    }

    double innerBottom() {
        return body.bottom() + body.clearance();
    }

    double innerTop() {
        return body.top() - body.clearance();
    }

    /** The gill sacs (their parts, the head's breathing order). */
    int sacCount() {
        return sacs.length;
    }

    int sacPart(int k) {
        return sacs[k];
    }

    double dropX() {
        return PlayField.WIDTH / 2.0;
    }

    double dropY() {
        return PlayField.HEIGHT / 2.0;
    }

    // --- the presentation's view ---------------------------------------------------------------

    /** The head's centre now, px (y up). */
    public double headX() {
        return headX;
    }

    public double headY() {
        return headY;
    }

    /** The throat's place now (the fans' and the walls' origin). */
    public double throatX() {
        return headX + throatDx;
    }

    public double throatY() {
        return headY + throatDy;
    }

    /** Whether it is up (the intro over). */
    public boolean up() {
        return up;
    }

    /** How far its intro has run, 0–1 (1 once up; 0 before it arrives). */
    public double introShare() {
        return up ? 1 : arrived ? Math.min(1, (double) ticks / introTicks) : 0;
    }

    /** Whether the jaw is open (the throat's window: a fan's glow and open time, a wall's glow, the lunge). */
    public boolean jawOpen() {
        return (jawTicks >= 0 || diveState == DiveState.LUNGE) && !piece.destroyed();
    }

    /** Whether the throat glows now (before a fan or a wall). */
    public boolean throatGlowing() {
        return jawTicks >= 0 && (jawForWall || jawTicks < jawGlowTicks);
    }

    /** Whether the jaw opened for a fan wall (its glow), not the throat's fan. */
    public boolean wallGlowing() {
        return jawTicks >= 0 && jawForWall;
    }

    /** The sac swelling now (its ring next); -1 for none. */
    public int swellingSac() {
        return swellPart;
    }

    /** The last fan wall's gap at the ship's height then, px; NaN before the first. */
    public double lastGap() {
        return lastGap;
    }

    /** The last fan wall's second gap (easy), px; NaN without one. */
    public double secondGap() {
        return secondGap;
    }

    public int segmentCount() {
        return segX.length;
    }

    /** Segment {@code i}'s centre now (a bight's swung in), px. */
    public double segmentX(int i) {
        return segX[i];
    }

    public double segmentY(int i) {
        return segY[i];
    }

    /** Segment {@code i}'s heading along the coil, radians (0 to the right, y up; toward the tail). */
    public double segmentAngle(int i) {
        return angle[i];
    }

    /** Segment {@code i}'s run: a {@link CoilSpec.Side} ordinal, or {@link #TOP}. */
    public int segmentSide(int i) {
        return side[i];
    }

    /** Whether segment {@code i} is in the bight of the sweep under way (its silt and glow line). */
    public boolean inBight(int i) {
        return sweepState != SweepState.IDLE
                && side[i] == sweepSide
                && Math.abs(along(i, side[i] == CoilSpec.Side.BOTTOM.ordinal()) - sweepU) < bightHalf;
    }

    public SweepState sweepState() {
        return sweepState;
    }

    /** The side of the sweep under way (or the last). */
    public CoilSpec.Side sweepSide() {
        return SIDES[sweepSide];
    }

    /** The sweep's state's progress, 0–1. */
    public double sweepProgress() {
        int length =
                switch (sweepState) {
                    case TELL -> tellTicks;
                    case SWING -> swingTicks;
                    case HOLD -> Math.max(1, holdTicks);
                    case BACK -> backTicks;
                    case IDLE -> 1;
                };
        return sweepState == SweepState.IDLE ? 0 : Math.min(1, (double) sweepTicks / length);
    }

    public DiveState diveState() {
        return diveState;
    }

    /** The dive's state's progress, 0–1. */
    public double diveProgress() {
        int length =
                switch (diveState) {
                    case SLIDE -> slideTicks;
                    case QUIET -> Math.max(1, quietTicks);
                    case SHADOW -> shadowTicks;
                    case LUNGE -> lungeTicks;
                    case BODY -> Math.max(1, bodyTicks);
                    case RETURN -> returnTicks;
                    case READY, DONE -> 1;
                };
        return diving() ? Math.min(1, (double) diveTicks / length) : 0;
    }

    /** How far the coil is down in the dark, 0 (on the play plane) – 1 (the dive's quiet to its way back). */
    public double depth() {
        return switch (diveState) {
            case SLIDE -> diveProgress();
            case QUIET, SHADOW, LUNGE, BODY -> 1;
            case RETURN -> 1 - diveProgress();
            case READY, DONE -> up ? 0 : 1 - introShare();
        };
    }

    /** The lunge's column's centre, px (fixed as its shadow starts). */
    public double columnX() {
        return columnX;
    }

    /** The column's width, px; 0 without a dive. */
    public double columnWidth() {
        return dive == null ? 0 : dive.width();
    }

    public int implantCount() {
        return implants.length;
    }

    /** Implant {@code i}'s part. */
    public int implantPart(int i) {
        return implants[i];
    }

    /** Implant {@code i}'s state now. */
    public ImplantState implantState(int i) {
        return implantTurn == i && !piece.destroyed() ? implantState : ImplantState.SHUT;
    }

    /** The current implant's state's progress, 0–1. */
    public double implantProgress() {
        int length = implantState == ImplantState.TELL ? Math.max(1, implantTellTicks) : implantOpenTicks;
        return implantState == ImplantState.SHUT ? 0 : Math.min(1, (double) implantTicks / length);
    }

    /** Whether implant {@code i} arcs now (its burst's tell). */
    public boolean implantArcing(int i) {
        return implantOpen(i) && arcTicks >= 0;
    }

    /** Implant {@code i}'s centre now (on its segment), px. */
    public double implantX(int i) {
        return segX[implantSeg[i]] + implantDx[i];
    }

    public double implantY(int i) {
        return segY[implantSeg[i]] + implantDy[i];
    }

    /** Whether a coil plate flares now (a shed's tell). */
    public boolean shedTelling() {
        return shedTell >= 0;
    }

    /** The point of the shed telling now (or the last). */
    public int shedPoint() {
        return shedPoint;
    }

    public int shedPointCount() {
        return shed == null ? 0 : shed.points().size();
    }

    /** Shed point {@code k}'s place: on its side's inner flank, px. */
    public double shedX(int k) {
        CoilSpec.ShedPoint point = shed.points().get(k);
        return switch (point.side()) {
            case LEFT -> body.left();
            case RIGHT -> body.right();
            case BOTTOM -> point.at();
        };
    }

    public double shedY(int k) {
        CoilSpec.ShedPoint point = shed.points().get(k);
        return point.side() == CoilSpec.Side.BOTTOM ? body.bottom() : point.at();
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(arrived ? 1 : 0)
                .add(ticks)
                .add(up ? 1 : 0)
                .add(lastPhase)
                .add(headX)
                .add(headY)
                .add(shipX)
                .add(shipY)
                .add(jawTimer)
                .add(jawTicks)
                .add(jawForWall ? 1 : 0)
                .add(breathTimer)
                .add(breathTurn)
                .add(swellPart)
                .add(swelled ? 1 : 0)
                .add(wallTimer)
                .add(wallGlow)
                .add(lastGap)
                .add(secondGap)
                .add(sweepTimer)
                .add(sweepTurn)
                .add(sweepState.ordinal())
                .add(sweepTicks)
                .add(sweepSide)
                .add(sweepCentre)
                .add(sweepU)
                .add(sweepCaught)
                .add(diveState.ordinal())
                .add(diveTicks)
                .add(columnX)
                .add(lungeCaught)
                .add(implantTurn)
                .add(implantState.ordinal())
                .add(implantTicks)
                .add(arcTimer)
                .add(arcTicks)
                .add(burstLeft)
                .add(burstGap)
                .add(shedTimer)
                .add(shedTurn)
                .add(shedTell)
                .add(shedPoint);
        for (int serial : shedSerials) {
            hash.add(serial);
        }
    }

    @Override
    public String toString() {
        return "coil dive " + diveState + " sweep " + sweepState + " implant " + implantState;
    }
}
