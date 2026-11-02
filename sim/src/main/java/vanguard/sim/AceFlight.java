package vanguard.sim;

/**
 * M10 part F, step F1a (design/enemies/bosses/honour-guard, The aces and The playbook): where a {@link Squadron}'s aces
 * fly. Each ace flies a path of a few legs (lines and arcs at the leg's speed) built when a move starts: its entry from
 * beyond its edge, its station ({@link AceMove#HOLD}: the leader's racetrack orbit in the top band, the dancer's
 * figure-eights of two tangent circles in the middle band, the feinter's tight orbit in the bottom band's side lane away
 * from the ship) and its moves (the leader's slide, the dancer's loop, roll and ribbon pass, the feinter's feint dive with
 * its pull-out, rear rake and overhead feint, the pincer of two flankers). A move ends back on its station. A dead ace's
 * hull tumbles away down and out.
 *
 * <p>Step F6s (round 78, B.1: "the movement of the other crafts is not very natural"): they fly like aircraft. Every
 * path is smooth: a leg is joined to the one before by a turn of bounded rate ({@link #JOIN_TURN}; a pass arrives already
 * pointing along it), so no corner and no snap; the speed eases between legs ({@link #ACCEL}, braking ahead of a slower
 * leg); the nose follows the motion at a bounded rate ({@link #NOSE_RATE}) and the hull banks into its turns ({@link
 * #bank}); the holds are orbits, never a parked craft. An ace about to fire from its station (the leader's lines, pairs
 * and fans, a dive's tell) turns its nose onto the ship and slows to a creep along it ({@link #aim}), never out of its
 * band. The separation and the keep-offs are first a steering avoidance: it looks {@link #LOOK} s ahead along the paths
 * with a {@link #MARGIN}, and an eased offset (bounded acceleration and speed) turns the ace away early; only then does
 * the exact position solver hold the rules (rare once the avoidance works: {@link #pushed}). A holding ace's share of a
 * push never takes it out of its station's band (step F6s, B.2 b): the other ace yields instead.
 *
 * <p>Step F6s4 (F6r's full-duel recording): no stall at the switch into a hold (a path that ran out part way through a
 * step folded back to the last step's place, {@link #fold}); a banded ace's offset eases back ahead of its band's edges
 * along where its path will be ({@link #BAND_LOOKS}), never stopped dead by the clamp; an aiming ace aims by flying: its
 * way turns onto the ship from the start and levels into a slow drift along its band's edge ({@link #AIM_DRIFT}), its
 * nose leads its motion by no more than {@link #AIM_SLIP} lets it slip, and it gives way only along its way; the
 * pincer's lanes keep the told moves' margin over the lower flanker's keep-off.
 *
 * <p>Headings are continuous (the look quantises them to 64). Every array is allocated once; the squadron hashes them.
 */
public final class AceFlight {
    /** The dive's pull-out turn radius, px, and how far it turns. */
    static final double PULL_OUT_RADIUS = 50;

    static final double PULL_OUT_SWEEP = 2 * StrictMath.PI / 3;
    /** How far beyond its edge an entry starts, past the hull's half, px. */
    static final double OUTSIDE = 10;
    /** The overhead feint's run above the top edge, px past it. */
    static final double OVER_TOP = 60;
    /**
     * The rake's climb top, px up, and its loop's radius. Step F6s3 (round 78 follow-up): its loop's top (330) stays a
     * separation under the leader's band's top (Level 40's y-up 480; 400 and 60 before reached into his band).
     */
    static final double RAKE_TOP = 280;

    static final double RAKE_TURN = 50;
    /** The ribbon's pass: its height (the design's y 200–260 from the top), px up, and its ends' inset from the edges. */
    static final double RIBBON_Y_MIN = 280;

    static final double RIBBON_Y_MAX = 340;
    static final double RIBBON_INSET = 40;
    /** The roll passes this much farther than its keep-off off the ship's side, this high above it, px. */
    static final double ROLL_WIDER = 20;

    static final double ROLL_ABOVE = 40;
    /** The pincer's lanes' centre above the ship (the design's "the ship's height − 160 px"), and its ends' inset. */
    static final double PINCER_ABOVE = 160;

    static final double PINCER_INSET = 30;
    /** The feinter changes lanes only this far past the field's centre, px. */
    static final double LANE_HYSTERESIS = 40;
    /** Step F6s2: the room the feinter's orbit keeps outside its keep-off from a ship short of its lane change, px. */
    static final double LANE_ROOM = 10;
    /** The ship's no-spawn ring: a separation push never shoves an ace into it. */
    static final double SHIP_RING = 72;
    /** A dead ace's tumble: px/s sideways (out to the nearer edge) and down, and its spin, radians/s. */
    static final double TUMBLE_SIDE = 80;

    static final double TUMBLE_DOWN = 160;
    static final double TUMBLE_SPIN = 1.5 * StrictMath.PI;
    /** Step F6s: px/s² a wreck's drift eases from its flight to its fall, and radians/s² its spin builds up. */
    static final double TUMBLE_EASE = 300;

    static final double SPIN_EASE = 8;
    /**
     * Step F6s: radians/s a joining turn is flown at (its radius: the speed it starts at over this, at least {@link
     * #JOIN_RADIUS}); the authored arcs (the pull-out 6.4, the eights ≤ 5, the rake's turn 4.3, the loop 2.5) keep
     * theirs.
     */
    static final double JOIN_TURN = 4.0;

    static final double JOIN_RADIUS = 20;
    /** Step F6s: no turn is flown faster than this, radians/s (the pull-out's 6.4; a join's tightest). */
    static final double TURN_CAP = 6.5;
    /**
     * Step F6s2: the fastest a join turns to keep in its station's band, radians/s (twice the cap: a hard bank, slowing
     * into it).
     */
    static final double BAND_TURN = 2 * TURN_CAP;
    /** Step F6s3: a banded join's cost a px out of its band (4 before: a join after a fold swung 33 px out). */
    static final double BAND_COST = 20;
    /** Step F6s: a hold's turns at most this fast, radians/s (its least radius: the station's speed over this). */
    static final double ORBIT_TURN = 5.0;
    /** Step F6s: the leader's racetrack's ends at most this radius, px (less in a narrower band). */
    static final double ORBIT_RADIUS = 45;
    /** Step F6s: px/s² an ace speeds up or brakes by. */
    static final double ACCEL = 600;
    /** Step F6s: the creep along its nose while it aims (px/s), its nose's turn (radians/s), and its room to slow in. */
    static final double AIM_SPEED = 24;

    static final double AIM_TURN = 4.0;
    static final double AIM_ROOM = 40;
    /** Step F6s: the most the nose turns, radians/s (it follows the motion). */
    static final double NOSE_RATE = 8.0;
    /**
     * Step F6s: the avoidance looks this far ahead along the paths, s, with this margin over the rules, px. Step F6s2:
     * along the paths' own legs (their turns and speeds), at {@link #LOOKS} steps of this up to its last.
     */
    static final double LOOK = 0.4;

    static final int LOOKS = 4;

    static final double MARGIN = 30;
    /**
     * Step F6s3: a told move's margin over its keep-off in the avoidance, px (a dive has none: its pull-out keeps it): a
     * ship that came nearer turns it early, not the solver.
     */
    static final double TOLD_MARGIN = 15;
    /** Step F6s: an ace holding or flying home this far off its path (pushed) flies on from where it is, px. */
    static final double FOLD = 40;
    /** Step F6s: the avoidance's offset: its spring (radians/s), its most acceleration (px/s²) and speed (px/s). */
    static final double SPRING = 5;

    static final double PUSH_ACCEL = 600;
    static final double PUSH_SPEED = 160;
    /** Step F6s: the most bank (radians), the turn that gives it (radians/s) and how fast the bank changes. */
    static final double MAX_BANK = StrictMath.PI / 3;

    static final double BANK_FULL = 4.0;
    static final double BANK_RATE = 3.0;
    /**
     * Step F6s4: an aiming ace's most slip across its nose, px/s: its nose turns onto the ship only as far as its motion
     * lets it (it aims by turning onto the ship, never by sliding against its nose).
     */
    static final double AIM_SLIP = 20;
    /** Step F6s4: an aiming ace's least drift, px/s (under {@link #AIM_SLIP}: its nose may point anywhere). */
    static final double AIM_DRIFT = 18;
    /**
     * Step F6s4: the looks over which the avoidance keeps a banded ace's offset inside its band and the field ahead
     * (its offset shrinks before its orbit reaches the edge, never stopped there by the clamp).
     */
    static final int BAND_LOOKS = 1;
    /** The pincer's slower flanker paces its approach no slower than this, px/s. */
    private static final double PACE_MIN = 80;
    /** The solver's rounds a step (step F6s4: 256, 64 before, left a hull 0.07 px inside the separation at times). */
    private static final int ROUNDS = 256;
    /** The avoidance's rounds (twice a step: now and ahead). */
    private static final int SOFT_ROUNDS = 8;
    /** The most legs a path has. */
    static final int LEGS = 12;

    private static final int LINE = 0;
    private static final int ARC = 1;
    private static final double TURN = 2 * StrictMath.PI;
    private static final AceMove[] MOVES = AceMove.values();
    private static final AceLeg[] LEG_KINDS = AceLeg.values();

    private final int count;
    private final SquadronSpec.Station[] stations;
    private final SquadronSpec.Role[] roles;
    private final double hullW;
    private final double hullH;
    private final double separation;
    /** The pincer's lanes' half gap: the hulls one separation apart, with a margin. */
    private final double pincerLane;

    // Per ace: where it is, was, faces, its avoidance's offset and the offset's speed, its path's point, direction and
    // speed, its bank.
    final double[] x;
    final double[] y;
    final double[] prevX;
    final double[] prevY;
    final double[] facing;
    final double[] offX;
    final double[] offY;
    final double[] ovX;
    final double[] ovY;
    final double[] pathX;
    final double[] pathY;
    final double[] tanX;
    final double[] tanY;
    final double[] spd;
    /** Per ace: its bank, radians (positive: leaning into a turn to its right, the nose turning clockwise on screen). */
    final double[] bank;
    /** Per ace: whether it flies now (entering or on the field, alive): the solver moves it. */
    final boolean[] flying;
    /** Per ace: whether its hull tumbles (dead) until it left the field. */
    final boolean[] tumbling;

    final double[] spin;
    final double[] vx;
    final double[] vy;
    /** Per ace: its move ({@link AceMove} ordinal), the steps since it started and its stage (the overhead's dive). */
    final int[] move;

    final int[] moveTicks;
    final int[] stage;
    /** Per ace: its current leg, the legs of its path, the distance into the leg and the steps since the leg began. */
    final int[] leg;

    final int[] legCount;
    final double[] along;
    final int[] legTicks;
    // Per ace and leg (ace × LEGS + leg): its kind, its tag ({@link AceLeg}), its points or arc, length, speed and the
    // direction it ends in.
    final int[] legKind;
    final int[] legTag;
    final double[] legAx;
    final double[] legAy;
    final double[] legBx;
    final double[] legBy;
    final double[] legR;
    final double[] legLength;
    final double[] legSpeed;
    final double[] legEx;
    final double[] legEy;
    /**
     * Per ace, its station's state: the leader's racetrack centre (the dancer's eight's centre), its side (the feinter's
     * lane side, the dancer's side of the field; a wreck's way out), its hold height.
     */
    final double[] centreX;

    final int[] side;
    final double[] holdY;
    /** Per ace: its own pull-out for this dive (the feinter's keep-off when it began). */
    final double[] pullOut;
    /** Step F6s: per ace, whether the playbook wants its nose on the ship now (set before each step) and is aiming. */
    final boolean[] aim;

    final boolean[] aiming;
    /** Step F6s: per ace, its pincer partner while they approach (-1: none) and its approach's paced speed (0: none). */
    final int[] partner;

    final double[] pace;
    /** Step F6s, for the tests (not state): per ace, the px the exact solver shoved it after the avoidance, summed. */
    final double[] pushed;
    /** The aces that have died (the feinter commits its pull-out from the first). */
    int deaths;
    /** Step F1b: the least pull-out of the dive being started ({@link #startDive}); 0 otherwise (never kept). */
    private double borrowedKeepOff;
    /** Where the ship is this step. */
    private double shipX;

    private double shipY;
    /** Step F6s4: whether a path runs out part way through a step ({@link #fold} keeps that step's offset motion). */
    private boolean midStep;
    /** Step F6s4: the avoidance's scratch: a banded ace's offset's least and most ahead (its band, the field). */
    private final double[] wantLoX;

    private final double[] wantHiX;
    private final double[] wantLoY;
    private final double[] wantHiY;
    /** The solver's scratch: the candidates' distance to the ship before it pushed; the avoidance's points. */
    private final double[] before;

    private final double[] qx;
    private final double[] qy;
    private final double[] baseX;
    private final double[] baseY;
    /** Step F6s2: the avoidance's scratch: each ace's path's direction where it looks. */
    private final double[] baseTx;

    private final double[] baseTy;
    /** Step F6s2: a point ahead on a path and its direction ({@link #ahead}). */
    private double hX;

    private double hY;
    private double hTx;
    private double hTy;
    private final double[] wantX;
    private final double[] wantY;
    /**
     * Step F6s3: whether ace {@code a} has been inside its band since it last left it on a move (holding or sliding):
     * then its avoidance keeps it in the band itself, not only no farther out than its path.
     */
    private final boolean[] settled;
    // A path's end pose and a turn's solution (scratch while a path is built).
    private double eX;
    private double eY;
    private double eHx;
    private double eHy;
    private double eV;
    private double gC1x;
    private double gC1y;
    private double gR1;
    private double gSweep;
    private double gT2x;
    private double gT2y;
    private int gSide;
    private double gSweepBest;
    private double gRadius;

    AceFlight(SquadronSpec spec) {
        count = spec.aces().size();
        stations = new SquadronSpec.Station[count];
        roles = new SquadronSpec.Role[count];
        for (int a = 0; a < count; a++) {
            stations[a] = spec.aces().get(a).station();
            roles[a] = spec.aces().get(a).role();
        }
        hullW = spec.hull().width();
        hullH = spec.hull().height();
        separation = spec.separation();
        // Step F6s3: half a pixel over the rule (3 before), so its lanes fit under the leader's band over a ship at its
        // start height.
        pincerLane = (hullH + separation) / 2 + 0.5;
        x = new double[count];
        y = new double[count];
        prevX = new double[count];
        prevY = new double[count];
        facing = new double[count];
        offX = new double[count];
        offY = new double[count];
        ovX = new double[count];
        ovY = new double[count];
        pathX = new double[count];
        pathY = new double[count];
        tanX = new double[count];
        tanY = new double[count];
        spd = new double[count];
        bank = new double[count];
        flying = new boolean[count];
        tumbling = new boolean[count];
        spin = new double[count];
        vx = new double[count];
        vy = new double[count];
        move = new int[count];
        moveTicks = new int[count];
        stage = new int[count];
        leg = new int[count];
        legCount = new int[count];
        along = new double[count];
        legTicks = new int[count];
        legKind = new int[count * LEGS];
        legTag = new int[count * LEGS];
        legAx = new double[count * LEGS];
        legAy = new double[count * LEGS];
        legBx = new double[count * LEGS];
        legBy = new double[count * LEGS];
        legR = new double[count * LEGS];
        legLength = new double[count * LEGS];
        legSpeed = new double[count * LEGS];
        legEx = new double[count * LEGS];
        legEy = new double[count * LEGS];
        centreX = new double[count];
        side = new int[count];
        holdY = new double[count];
        pullOut = new double[count];
        aim = new boolean[count];
        aiming = new boolean[count];
        partner = new int[count];
        pace = new double[count];
        pushed = new double[count];
        before = new double[count];
        qx = new double[count];
        qy = new double[count];
        baseX = new double[count];
        baseY = new double[count];
        baseTx = new double[count];
        baseTy = new double[count];
        wantX = new double[count];
        wantY = new double[count];
        wantLoX = new double[count];
        wantHiX = new double[count];
        wantLoY = new double[count];
        wantHiY = new double[count];
        settled = new boolean[count];
        reset();
    }

    /** Back to the level start (and the boss checkpoint): every ace off the field above it, no path. */
    void reset() {
        deaths = 0;
        shipX = PlayField.WIDTH / 2.0;
        shipY = 0;
        for (int a = 0; a < count; a++) {
            SquadronSpec.Station station = stations[a];
            x[a] = prevX[a] = pathX[a] = PlayField.WIDTH / 2.0;
            y[a] = prevY[a] = pathY[a] = PlayField.HEIGHT + hullH + OUTSIDE;
            facing[a] = 0;
            offX[a] = offY[a] = ovX[a] = ovY[a] = 0;
            tanX[a] = 0;
            tanY[a] = -1;
            spd[a] = 0;
            bank[a] = 0;
            flying[a] = false;
            tumbling[a] = false;
            spin[a] = vx[a] = vy[a] = 0;
            move[a] = AceMove.HOLD.ordinal();
            moveTicks[a] = 0;
            stage[a] = 0;
            leg[a] = 0;
            legCount[a] = 0;
            along[a] = 0;
            legTicks[a] = 0;
            centreX[a] = roles[a] == SquadronSpec.Role.SNIPER ? station.x() : PlayField.WIDTH / 2.0;
            side[a] = 1;
            holdY[a] = (station.yMin() + station.yMax()) / 2;
            pullOut[a] = station.keepOff();
            aim[a] = false;
            aiming[a] = false;
            partner[a] = -1;
            pace[a] = 0;
            pushed[a] = 0;
            settled[a] = false;
        }
        java.util.Arrays.fill(legKind, 0);
        java.util.Arrays.fill(legTag, 0);
        java.util.Arrays.fill(legAx, 0);
        java.util.Arrays.fill(legAy, 0);
        java.util.Arrays.fill(legBx, 0);
        java.util.Arrays.fill(legBy, 0);
        java.util.Arrays.fill(legR, 0);
        java.util.Arrays.fill(legLength, 0);
        java.util.Arrays.fill(legSpeed, 0);
        java.util.Arrays.fill(legEx, 0);
        java.util.Arrays.fill(legEy, 0);
    }

    /** Every ace's place at the end of the last step (the render's interpolation). */
    void keepPrev() {
        for (int a = 0; a < count; a++) {
            prevX[a] = x[a];
            prevY[a] = y[a];
        }
    }

    /**
     * Ace {@code a} is about to enter by {@code edge} (its tell starts): it waits beyond that edge where it will cross,
     * its station's side set ({@code leaderX}: the leader's x, NaN without a flying leader).
     */
    void prepare(int a, WarningEdge edge, double leaderX) {
        SquadronSpec.Station station = stations[a];
        switch (roles[a]) {
            case SNIPER -> centreX[a] = station.x();
            case DANCER -> {
                side[a] = edge == WarningEdge.LEFT
                        ? -1
                        : edge == WarningEdge.RIGHT
                                ? 1
                                : (Double.isNaN(leaderX) || leaderX <= PlayField.WIDTH / 2.0 ? 1 : -1);
                centreX[a] = PlayField.WIDTH / 2.0 + side[a] * station.x();
            }
            case FEINTER -> {
                side[a] = shipX < PlayField.WIDTH / 2.0 ? 1 : -1;
                centreX[a] = laneX(a);
            }
        }
        double tx = entryX(a);
        double ty = holdY[a];
        double sx = tx;
        double sy = ty;
        switch (edge) {
            case TOP -> sy = PlayField.HEIGHT + hullH / 2 + OUTSIDE;
            case BOTTOM -> sy = -hullH / 2 - OUTSIDE;
            case LEFT -> sx = -hullW / 2 - OUTSIDE;
            case RIGHT -> sx = PlayField.WIDTH + hullW / 2 + OUTSIDE;
        }
        place(a, sx, sy);
        double d = Math.hypot(tx - sx, ty - sy);
        tanX[a] = d > 0 ? (tx - sx) / d : 0;
        tanY[a] = d > 0 ? (ty - sy) / d : -1;
        facing[a] = Enemy.heading(tx - sx, ty - sy);
    }

    /** Where ace {@code a}'s entry ends: its station's point. */
    private double entryX(int a) {
        return roles[a] == SquadronSpec.Role.FEINTER ? laneX(a) : centreX[a];
    }

    /** Places ace {@code a} at (px, py) without a path or a jump. */
    void place(int a, double px, double py) {
        x[a] = prevX[a] = pathX[a] = px;
        y[a] = prevY[a] = pathY[a] = py;
        offX[a] = offY[a] = ovX[a] = ovY[a] = 0;
        legCount[a] = 0;
    }

    /** Ace {@code a} crosses its edge: it flies in at its dash speed to its station's point, then holds. */
    void enter(int a) {
        flying[a] = true;
        spd[a] = stations[a].dashSpeed();
        begin(a, AceMove.ENTRY);
        line(a, AceLeg.ENTRY, entryX(a), holdY[a], stations[a].dashSpeed());
    }

    /** Ace {@code a} died: its hull tumbles away down and out to the nearer side edge, spinning up. */
    void tumble(int a) {
        flying[a] = false;
        tumbling[a] = true;
        aiming[a] = false;
        // It goes on as it flew (the blast kicks it down), its drift easing to the fall.
        vx[a] = (x[a] - prevX[a]) * SimStep.PER_SECOND;
        vy[a] = Math.min((y[a] - prevY[a]) * SimStep.PER_SECOND, -TUMBLE_DOWN / 4);
        spin[a] = 0;
        begin(a, AceMove.TUMBLE);
        side[a] = x[a] < PlayField.WIDTH / 2.0 ? -1 : 1;
    }

    /**
     * Ace {@code a} starts {@code m} (after its tell, the playbook's call) from where it is; the ship at (sx, sy) locks
     * a dive's line and places a roll and a pincer. {@link AceMove#PINCER} takes two aces ({@link #startPincer}).
     */
    void start(int a, AceMove m, double sx, double sy, BossActions actions) {
        shipX = sx;
        shipY = sy;
        if (!flying[a]) {
            return;
        }
        aiming[a] = false;
        SquadronSpec.Station station = stations[a];
        switch (m) {
            case HOLD -> {
                // Its station's orbit goes on from where its path is.
                move[a] = AceMove.HOLD.ordinal();
                moveTicks[a] = 0;
                stage[a] = 0;
                fold(a);
                holdLeg(a, actions);
            }
            case SLIDE -> {
                begin(a, m);
                centreX[a] = PlayField.WIDTH - centreX[a];
                go(a, AceLeg.PASS, AceLeg.APPROACH, centreX[a], holdY[a], station.dashSpeed(), true);
            }
            case LOOP -> {
                begin(a, m);
                // Step F6s3: no wider than its band (Level 40's 100 px: 50, at the same turn rate, slower; 80 before
                // reached into the leader's band), flown in its seconds.
                double r = Math.min(
                        station.speed() * station.loopSeconds() / (2 * StrictMath.PI),
                        (station.yMax() - station.yMin()) / 2);
                double loopSpeed =
                        station.loopSeconds() > 0 ? 2 * StrictMath.PI * r / station.loopSeconds() : station.speed();
                double band = (station.yMin() + station.yMax()) / 2;
                double nx = -tanY[a];
                double ny = tanX[a];
                // The loop turns toward its band's middle (the left normal's centre or the right's), away from the
                // ship.
                double keep = keepOff(a) + r;
                // Step F6s2: and away from the other aces' bands (the leader's above, the feinter's below).
                double left = Math.abs(pathY[a] + r * ny - band)
                        + 2 * Math.max(0, keep - Math.hypot(pathX[a] + r * nx - sx, pathY[a] + r * ny - sy))
                        + 2 * intrusion(a, pathY[a] + r * ny, r);
                double right = Math.abs(pathY[a] - r * ny - band)
                        + 2 * Math.max(0, keep - Math.hypot(pathX[a] - r * nx - sx, pathY[a] - r * ny - sy))
                        + 2 * intrusion(a, pathY[a] - r * ny, r);
                double turn = left <= right ? 1 : -1;
                arc(
                        a,
                        AceLeg.TURN,
                        pathX[a] + turn * r * nx,
                        pathY[a] + turn * r * ny,
                        r,
                        turn * 2 * StrictMath.PI,
                        loopSpeed);
            }
            case ROLL -> {
                begin(a, m);
                int by = sx < PlayField.WIDTH / 2.0 ? 1 : -1;
                double mx =
                        Math.clamp(sx + by * (station.keepOff() + ROLL_WIDER), hullW / 2, PlayField.WIDTH - hullW / 2);
                go(a, AceLeg.PASS, AceLeg.APPROACH, mx, sy + ROLL_ABOVE, station.dashSpeed(), false);
                home(a, centreX[a], holdY[a], station.dashSpeed());
            }
            case RIBBON -> {
                begin(a, m);
                double ry = RIBBON_Y_MIN + (RIBBON_Y_MAX - RIBBON_Y_MIN) * actions.draw();
                double from = pathX[a] < PlayField.WIDTH / 2.0 ? RIBBON_INSET : PlayField.WIDTH - RIBBON_INSET;
                double dir = from < PlayField.WIDTH / 2.0 ? 1 : -1;
                into(a, AceLeg.APPROACH, from, ry, dir, 0, station.dashSpeed());
                line(a, AceLeg.PASS, PlayField.WIDTH - from, ry, station.dashSpeed());
            }
            case DIVE -> {
                begin(a, m);
                dive(a);
            }
            case RAKE -> {
                begin(a, m);
                double lane = laneX(a);
                int inward = lane < PlayField.WIDTH / 2.0 ? 1 : -1;
                double climb = station.climbSpeed();
                double from =
                        Math.min(Math.max(pathY[a], station.yMin()) + climb / JOIN_TURN, RAKE_TOP - climb / JOIN_TURN);
                into(a, AceLeg.APPROACH, lane, from, 0, 1, climb);
                line(a, AceLeg.CLIMB, lane, RAKE_TOP, climb);
                arc(
                        a,
                        AceLeg.TURN,
                        lane + inward * RAKE_TURN,
                        RAKE_TOP,
                        RAKE_TURN,
                        inward > 0 ? -StrictMath.PI : StrictMath.PI,
                        climb);
                home(a, lane, holdY[a], climb);
            }
            case OVERHEAD -> {
                begin(a, m);
                double top = PlayField.HEIGHT + hullH / 2 + OVER_TOP;
                go(a, AceLeg.CLIMB, AceLeg.CLIMB, laneX(a), top, station.climbSpeed(), false);
                go(
                        a,
                        AceLeg.APPROACH,
                        AceLeg.APPROACH,
                        Math.clamp(sx, hullW, PlayField.WIDTH - hullW),
                        top,
                        station.dashSpeed(),
                        false);
            }
            case PINCER, ENTRY, TUMBLE -> throw new IllegalArgumentException(m + " is not an ace's own move");
        }
    }

    /**
     * The pincer: {@code upper} and {@code lower} fly to opposite side edges (each the side it is nearer, the lower
     * the other), arriving together pointing inward (the later sets the pace), then cross at their dash speeds in two
     * lanes one separation apart, centred {@link #PINCER_ABOVE} above the ship (higher when the lower one's keep-off
     * asks it), then go back to their stations.
     */
    void startPincer(int upper, int lower, double sx, double sy) {
        shipX = sx;
        shipY = sy;
        if (!flying[upper] || !flying[lower]) {
            return;
        }
        // Step F6s4: the lower lane the told moves' margin over its keep-off where the room allows (on the rule itself,
        // a ship rising under the pass shoved the lower flanker, the upper one and the leader together).
        double spare = pincerTop(upper, lower) - (sy + keepOff(lower) + 1 + 2 * pincerLane);
        double low = Math.max(
                sy + PINCER_ABOVE - pincerLane, sy + keepOff(lower) + 1 + Math.clamp(spare, 0, TOLD_MARGIN - 1));
        double high = low + 2 * pincerLane;
        // Step F6s3: the upper lane a separation under the other flying aces' bands' tops (Level 40: the leader's), so
        // it never reaches into his band (ship + 255 before).
        double top = pincerTop(upper, lower);
        if (high > top) {
            low -= high - top;
            high = top;
        }
        aiming[upper] = aiming[lower] = false;
        double upperFrom = pathX[upper] < PlayField.WIDTH / 2.0 ? PINCER_INSET : PlayField.WIDTH - PINCER_INSET;
        double lowerFrom = PlayField.WIDTH - upperFrom;
        begin(upper, AceMove.PINCER);
        begin(lower, AceMove.PINCER);
        into(upper, AceLeg.APPROACH, upperFrom, high, upperFrom < lowerFrom ? 1 : -1, 0, stations[upper].dashSpeed());
        into(lower, AceLeg.APPROACH, lowerFrom, low, lowerFrom < upperFrom ? 1 : -1, 0, stations[lower].dashSpeed());
        line(upper, AceLeg.PASS, lowerFrom, high, stations[upper].dashSpeed());
        line(lower, AceLeg.PASS, upperFrom, low, stations[lower].dashSpeed());
        partner[upper] = lower;
        partner[lower] = upper;
    }

    /**
     * Step F6s3: the pincer's upper lane's highest: under the field's top edge, and a separation (hull to hull) under the
     * band tops of the flying aces that do not fly it (Level 40: the leader's y-up 480, so 346).
     */
    private double pincerTop(int upper, int lower) {
        double top = PlayField.HEIGHT - hullH / 2;
        for (int b = 0; b < count; b++) {
            if (b != upper && b != lower && flying[b]) {
                top = Math.min(top, stations[b].yMax() - hullH - separation);
            }
        }
        return top;
    }

    /**
     * Step F6s3: whether the pincer's lanes fit with the ship at height {@code sy}: its lower lane outside the lower
     * flanker's keep-off over the ship, its upper one under {@link #pincerTop} (a ship flying too high, it is not
     * drawn).
     */
    boolean pincerFits(int upper, int lower, double sy) {
        return sy + keepOff(lower) + 1 + 2 * pincerLane <= pincerTop(upper, lower);
    }

    /**
     * Ace {@code a}'s path goes on from where it is, as it moves: its avoidance's offset folded into its path's point,
     * its motion into its path's direction and speed (no jump, no change of speed or heading). Step F6s4: from its
     * path's point now plus its offset, not its last step's place (a path that ended part way through a step folded
     * back to where it was: a one-step stall at each switch into a hold).
     */
    private void fold(int a) {
        double mx = tanX[a] * spd[a] + ovX[a];
        double my = tanY[a] * spd[a] + ovY[a];
        double v = Math.hypot(mx, my);
        pathX[a] += offX[a];
        pathY[a] += offY[a];
        if (v > 1) {
            tanX[a] = mx / v;
            tanY[a] = my / v;
        }
        spd[a] = v;
        // Step F6s4: folded part way through a step (its path ran out), its offset's motion this step stays in it: its
        // path's way this step was its old speed's (the folded motion flies from the next step on).
        offX[a] = midStep ? ovX[a] * SimStep.SECONDS : 0;
        offY[a] = midStep ? ovY[a] * SimStep.SECONDS : 0;
        ovX[a] = ovY[a] = 0;
    }

    /** A new move for ace {@code a}: a fresh path from where it is, as it moves. */
    private void begin(int a, AceMove m) {
        fold(a);
        move[a] = m.ordinal();
        moveTicks[a] = 0;
        stage[a] = 0;
        partner[a] = -1;
        pace[a] = 0;
        restart(a);
    }

    /** A fresh path from ace {@code a}'s path's point, direction and speed (its avoidance's offset flies on). */
    private void restart(int a) {
        leg[a] = 0;
        legCount[a] = 0;
        along[a] = 0;
        legTicks[a] = 0;
    }

    /**
     * Step F1b: ace {@code a} dives as {@link AceMove#DIVE} with the ship at (sx, sy), pulling out no closer than {@code
     * least} px either (the last ace's borrowed dive: the feinter's committed pull-out when its own keep-off is less).
     */
    void startDive(int a, double sx, double sy, double least, BossActions actions) {
        borrowedKeepOff = least;
        start(a, AceMove.DIVE, sx, sy, actions);
        borrowedKeepOff = 0;
    }

    /**
     * The dive from where ace {@code a}'s path is at the ship's spot now: the turn onto the line at the spot, its run
     * along it, its pull-out, its climb home.
     */
    private void dive(int a) {
        SquadronSpec.Station station = stations[a];
        pullOut[a] = Math.max(keepOff(a), borrowedKeepOff);
        double start = pullOutStart(pullOut[a]);
        endPose(a);
        double r1 = Math.max(JOIN_RADIUS, eV / JOIN_TURN);
        double best = Double.POSITIVE_INFINITY;
        int bestSide = 0;
        for (int s = 1; s >= -1; s -= 2) {
            double len = tangent(r1, s, shipX, shipY, 0);
            if (len < best) {
                best = len;
                bestSide = s;
            }
        }
        double fromX = eX;
        double fromY = eY;
        double ux = eHx;
        double uy = eHy;
        if (bestSide != 0) {
            tangent(r1, bestSide, shipX, shipY, 0);
            fromX = arcTo(a, AceLeg.APPROACH, station.dashSpeed());
            fromY = eY;
            double dx = shipX - fromX;
            double dy = shipY - fromY;
            double d = Math.hypot(dx, dy);
            if (d > 0) {
                ux = dx / d;
                uy = dy / d;
            }
            if (d > start + 1) {
                fromX = shipX - ux * start;
                fromY = shipY - uy * start;
                line(a, AceLeg.RUN, fromX, fromY, station.dashSpeed());
            }
        }
        pullOutFrom(a, fromX, fromY, ux, uy);
    }

    /**
     * The pull-out from (px, py) heading (ux, uy): an arc banking off toward its way home (step F6s: so the climb home
     * goes on from it; a side that would leave the field costs more), then home.
     */
    private void pullOutFrom(int a, double px, double py, double ux, double uy) {
        SquadronSpec.Station station = stations[a];
        double nx = -uy;
        double ny = ux;
        double left = pullOutCost(a, px, py, ux, uy, 1);
        double right = pullOutCost(a, px, py, ux, uy, -1);
        double turn = left <= right ? 1 : -1;
        arc(
                a,
                AceLeg.PULL_OUT,
                px + turn * PULL_OUT_RADIUS * nx,
                py + turn * PULL_OUT_RADIUS * ny,
                PULL_OUT_RADIUS,
                turn * PULL_OUT_SWEEP,
                station.dashSpeed());
        home(a, laneX(a), holdY[a], station.climbSpeed());
    }

    /** How far a pull-out turning {@code turn} ends from ace {@code a}'s lane, plus four times how far off the field. */
    private double pullOutCost(int a, double px, double py, double ux, double uy, int turn) {
        double cx = px - turn * PULL_OUT_RADIUS * uy;
        double cy = py + turn * PULL_OUT_RADIUS * ux;
        double end = StrictMath.atan2(py - cy, px - cx) + turn * PULL_OUT_SWEEP;
        double ex = cx + PULL_OUT_RADIUS * Trig.cos(end);
        double ey = cy + PULL_OUT_RADIUS * Trig.sin(end);
        double off =
                Math.max(0, Math.max(hullW / 2 - ex, ex - (PlayField.WIDTH - hullW / 2))) + Math.max(0, hullH / 2 - ey);
        return Math.hypot(ex - laneX(a), ey - holdY[a]) + 4 * off;
    }

    /**
     * Back to (tx, ty) ({@link AceLeg#RETURN}): straight there, or round the ship first (by points outside its keep-off
     * with the margin, over it) when the straight way passes inside that.
     */
    private void home(int a, double tx, double ty, double speed) {
        endPose(a);
        double keep = keepOff(a) + MARGIN;
        double dx = tx - eX;
        double dy = ty - eY;
        double length = Math.hypot(dx, dy);
        double along = length > 0 ? Math.clamp(((shipX - eX) * dx + (shipY - eY) * dy) / (length * length), 0, 1) : 0;
        if (keepOff(a) > 0 && Math.hypot(eX + dx * along - shipX, eY + dy * along - shipY) < keep) {
            // Round the ship (over it, never under the field's bottom) by one or two points outside the keep-off.
            double around = keep + hullH / 2;
            double from = StrictMath.atan2(eY - shipY, eX - shipX);
            double gap = Math.IEEEremainder(StrictMath.atan2(ty - shipY, tx - shipX) - from, 2 * StrictMath.PI);
            if (shipY + around * Trig.sin(from + gap / 2) < hullH / 2) {
                gap -= Math.signum(gap) * 2 * StrictMath.PI;
            }
            int points = Math.abs(gap) > 2 * StrictMath.PI / 3 ? 2 : 1;
            for (int i = 1; i <= points; i++) {
                double angle = from + gap * i / (points + 1);
                go(
                        a,
                        AceLeg.RETURN,
                        AceLeg.RETURN,
                        shipX + around * Trig.cos(angle),
                        shipY + around * Trig.sin(angle),
                        speed,
                        false);
            }
        }
        go(a, AceLeg.RETURN, AceLeg.RETURN, tx, ty, speed, false);
    }

    /**
     * How far from the ship's spot the dive's run ends so that its pull-out arc passes no closer than {@code pullOut}:
     * the turn's circle then clears the ship by that.
     */
    static double pullOutStart(double pullOut) {
        double outer = pullOut + PULL_OUT_RADIUS;
        return Math.sqrt(outer * outer - PULL_OUT_RADIUS * PULL_OUT_RADIUS);
    }

    /** Ace {@code a}'s keep-off from the ship now: the feinter's pull-out commits once an ace has died. */
    double keepOff(int a) {
        SquadronSpec.Station station = stations[a];
        return deaths > 0 ? station.committedKeepOff() : station.keepOff();
    }

    /** The feinter's side lane's centre now, px from the left: its climbs' line and its orbit's outer edge. */
    private double laneX(int a) {
        double inset = stations[a].x();
        return side[a] > 0 ? PlayField.WIDTH - inset : inset;
    }

    /**
     * Ace {@code a}'s next lap on its station (rebuilt as each ends), joined from where its path is: the leader's
     * racetrack (its strafe drawn each lap), the dancer's eight (its width drawn each lap), the feinter's tight orbit in
     * its lane (over the ship first when the lane changes sides).
     */
    private void holdLeg(int a, BossActions actions) {
        restart(a);
        SquadronSpec.Station station = stations[a];
        double v = station.speed();
        double band = station.yMax() - station.yMin();
        switch (roles[a]) {
            case SNIPER -> {
                double span = station.spanMin() + (station.spanMax() - station.spanMin()) * actions.draw();
                double r = Math.max(v / ORBIT_TURN, Math.min(ORBIT_RADIUS, band / 2));
                racetrack(a, centreX[a], holdY[a], span / 2, r, false, v);
            }
            case DANCER -> {
                double span = station.spanMin() + (station.spanMax() - station.spanMin()) * actions.draw();
                eight(a, centreX[a], holdY[a], Math.max(v / ORBIT_TURN, Math.min(span / 4, band / 2)), v);
            }
            case FEINTER -> {
                int was = side[a];
                double centre = PlayField.WIDTH / 2.0;
                double lane = laneHysteresis(a);
                if (shipX > centre + lane) {
                    side[a] = -1;
                } else if (shipX < centre - lane) {
                    side[a] = 1;
                }
                double r = v / ORBIT_TURN;
                double lo = Math.max(station.yMin(), hullH / 2);
                double hi = Math.max(lo, Math.min(station.yMax(), PlayField.HEIGHT - hullH / 2));
                double cx = side[a] > 0 ? laneX(a) - r : laneX(a) + r;
                holdY[a] = (lo + hi) / 2;
                if (side[a] != was || Math.abs(pathX[a] - cx) > hullW + r) {
                    // Over the ship to the other lane, outside its keep-off.
                    // Step F6s2: no higher than it must (from a rake's or a dive's way home it comes down to it).
                    // Step F6s3: its hull's half over the keep-off (its whole before), under the dancer's band.
                    double over = Math.max(station.yMax(), shipY + keepOff(a) + hullH / 2);
                    go(a, AceLeg.APPROACH, AceLeg.APPROACH, pathX[a], over, v, false);
                    go(a, AceLeg.APPROACH, AceLeg.APPROACH, cx, over, v, false);
                }
                racetrack(a, cx, holdY[a], Math.max(0, (hi - lo) / 2 - r), r, true, v);
            }
        }
    }

    /**
     * A lap of a racetrack centred at (cx, cy): two half circles of radius {@code r} {@code half} px either side of the
     * centre (across, or up and down when {@code vertical}) joined by straights, flown either way round (the shorter
     * join from where the path is); it ends where it began, on the circle it joined.
     */
    private void racetrack(int a, double cx, double cy, double half, double r, boolean vertical, double speed) {
        double best = Double.POSITIVE_INFINITY;
        int bestEnd = 0;
        int bestDir = 1;
        for (int e = 0; e < 2; e++) {
            double sign = e == 0 ? -1 : 1;
            double ex = vertical ? cx : cx + sign * half;
            double ey = vertical ? cy + sign * half : cy;
            double ux = vertical ? 0 : -sign;
            double uy = vertical ? -sign : 0;
            for (int dir = 1; dir >= -1; dir -= 2) {
                double join = onto(a, ex, ey, r, dir, true);
                if (join == Double.POSITIVE_INFINITY) {
                    continue;
                }
                double leave = leaving(ux, uy, dir);
                double len = join + r * Math.abs(sweep(StrictMath.atan2(gT2y - ey, gT2x - ex), leave, dir));
                if (len < best) {
                    best = len;
                    bestEnd = e;
                    bestDir = dir;
                }
            }
        }
        double sign = bestEnd == 0 ? -1 : 1;
        double ex = vertical ? cx : cx + sign * half;
        double ey = vertical ? cy + sign * half : cy;
        double ox = vertical ? cx : cx - sign * half;
        double oy = vertical ? cy - sign * half : cy;
        double ux = vertical ? 0 : -sign;
        double uy = vertical ? -sign : 0;
        double leave = leaving(ux, uy, bestDir);
        joinOnto(a, ex, ey, r, bestDir, speed);
        arcRound(a, AceLeg.HOLD, ex, ey, r, leave, bestDir, speed);
        double c = Trig.cos(leave);
        double s = Trig.sin(leave);
        line(a, AceLeg.HOLD, ox + r * c, oy + r * s, speed);
        arc(a, AceLeg.HOLD, ox, oy, r, bestDir * StrictMath.PI, speed);
        line(a, AceLeg.HOLD, ex - r * c, ey - r * s, speed);
    }

    /**
     * Step F6s2: how far a circle round height {@code cy} of radius {@code r} reaches into the bands of the other flying
     * aces, px (summed): a told move's turn goes the way that keeps out of them when it can.
     */
    private double intrusion(int a, double cy, double r) {
        double in = 0;
        for (int b = 0; b < count; b++) {
            if (b != a && flying[b]) {
                SquadronSpec.Station station = stations[b];
                in += Math.max(0, Math.min(cy + r, station.yMax()) - Math.max(cy - r, station.yMin()));
            }
        }
        return in;
    }

    /** The angle on a circle where its tangent, turning {@code dir}, points along (ux, uy). */
    private static double leaving(double ux, double uy, int dir) {
        return dir > 0 ? StrictMath.atan2(-ux, uy) : StrictMath.atan2(ux, -uy);
    }

    /**
     * A figure-eight centred at (cx, cy): the left circle anticlockwise, the right clockwise, both of radius {@code r},
     * tangent at the centre; joined onto the nearer, round to the centre, then once round each.
     */
    private void eight(int a, double cx, double cy, double r, double speed) {
        double best = Double.POSITIVE_INFINITY;
        int bestE = 0;
        for (int e = 0; e < 2; e++) {
            double ex = e == 0 ? cx - r : cx + r;
            int dir = e == 0 ? 1 : -1;
            double cross = e == 0 ? 0 : StrictMath.PI;
            double join = onto(a, ex, cy, r, dir, true);
            if (join == Double.POSITIVE_INFINITY) {
                continue;
            }
            double len = join + r * Math.abs(sweep(StrictMath.atan2(gT2y - cy, gT2x - ex), cross, dir));
            if (len < best) {
                best = len;
                bestE = e;
            }
        }
        double ex = bestE == 0 ? cx - r : cx + r;
        double ox = bestE == 0 ? cx + r : cx - r;
        int dir = bestE == 0 ? 1 : -1;
        joinOnto(a, ex, cy, r, dir, speed);
        arcRound(a, AceLeg.HOLD, ex, cy, r, bestE == 0 ? 0 : StrictMath.PI, dir, speed);
        arc(a, AceLeg.HOLD, ox, cy, r, -dir * TURN, speed);
        arc(a, AceLeg.HOLD, ex, cy, r, dir * TURN, speed);
    }

    /** The dancer's eight's centre goes to the side away from the leader at (lx, ...) (NaN: it stays). */
    void awayFrom(int a, double leaderX) {
        if (roles[a] != SquadronSpec.Role.DANCER || Double.isNaN(leaderX)) {
            return;
        }
        side[a] = leaderX <= PlayField.WIDTH / 2.0 ? 1 : -1;
        centreX[a] = PlayField.WIDTH / 2.0 + side[a] * stations[a].x();
    }

    // The path's legs.

    /** The end of ace {@code a}'s path so far: its point, direction and speed (into the scratch). */
    private void endPose(int a) {
        if (legCount[a] == 0) {
            eX = pathX[a];
            eY = pathY[a];
            eHx = tanX[a];
            eHy = tanY[a];
            eV = spd[a];
        } else {
            int k = a * LEGS + legCount[a] - 1;
            eX = endX(k);
            eY = endY(k);
            eHx = legEx[k];
            eHy = legEy[k];
            eV = legSpeed[k];
        }
    }

    /**
     * A turn from the end pose (radius {@code r1}, anticlockwise for {@code s1} 1) and a straight onto the circle
     * (cx, cy) of signed radius {@code rho2} (positive: flown anticlockwise; 0: the point itself): its length, or
     * infinity when there is no such straight. Leaves the turn's centre, sweep and the landing point in the scratch.
     */
    private double tangent(double r1, int s1, double cx, double cy, double rho2) {
        double c1x = eX - s1 * r1 * eHy;
        double c1y = eY + s1 * r1 * eHx;
        double dx = cx - c1x;
        double dy = cy - c1y;
        double dist = Math.hypot(dx, dy);
        double delta = rho2 - s1 * r1;
        if (!(dist > 1e-9) || Math.abs(delta) > dist) {
            return Double.POSITIVE_INFINITY;
        }
        double phi = StrictMath.atan2(dy, dx) - StrictMath.asin(delta / dist);
        double ux = Trig.cos(phi);
        double uy = Trig.sin(phi);
        double t1x = c1x + s1 * r1 * uy;
        double t1y = c1y - s1 * r1 * ux;
        gC1x = c1x;
        gC1y = c1y;
        gR1 = r1;
        gT2x = cx + rho2 * uy;
        gT2y = cy - rho2 * ux;
        gSweep = sweep(StrictMath.atan2(eY - c1y, eX - c1x), StrictMath.atan2(t1y - c1y, t1x - c1x), s1);
        return r1 * Math.abs(gSweep) + Math.max(0, dx * ux + dy * uy);
    }

    /** The turn from {@code from} to {@code to}, radians, the way {@code dir} turns (none for a hair's breadth). */
    static double sweep(double from, double to, int dir) {
        double s = dir > 0 ? to - from : from - to;
        s -= TURN * Math.floor(s / TURN);
        if (s < 1e-9 || s > TURN - 1e-6) {
            s = 0;
        }
        return dir > 0 ? s : -s;
    }

    /**
     * The shortest join from ace {@code a}'s path's end onto the circle (cx, cy) of radius {@code r} flown {@code dir}
     * ({@code banded}: a turn leaving its station's band costs more): its length (infinity: none), the landing point in
     * the scratch. Already on that circle and flying round it: no join.
     */
    private double onto(int a, double cx, double cy, double r, int dir, boolean banded) {
        endPose(a);
        double rx = eX - cx;
        double ry = eY - cy;
        double d = Math.hypot(rx, ry);
        if (Math.abs(d - r) < 0.5 && d > 0 && (dir * (-ry * eHx + rx * eHy)) / d > 0.995) {
            gSide = 0;
            gT2x = eX;
            gT2y = eY;
            return 0;
        }
        double best = Double.POSITIVE_INFINITY;
        double bestOut = 0;
        double least = Math.max(JOIN_RADIUS / 4, eV / BAND_TURN);
        for (double r1 = Math.max(JOIN_RADIUS, eV / JOIN_TURN); r1 >= JOIN_RADIUS / 4; r1 = next(r1, least, banded)) {
            for (int s = 1; s >= -1; s -= 2) {
                double len = tangent(r1, s, cx, cy, dir * r);
                double out = 0;
                if (len < Double.POSITIVE_INFINITY) {
                    out = outside(a, banded);
                    len += (banded ? BAND_COST : 4) * out + 4 * nearShip(a);
                }
                if (len < best) {
                    best = len;
                    bestOut = out;
                    gSide = s;
                    gRadius = r1;
                }
            }
            // Step F6s2: a join that would leave the station's band turns tighter (slowing into it) if that keeps in,
            // never tighter than its speed turns at its most.
            if (best < Double.POSITIVE_INFINITY && !(banded && bestOut > 1 && r1 > least)) {
                break;
            }
        }
        if (best < Double.POSITIVE_INFINITY) {
            tangent(gRadius, gSide, cx, cy, dir * r);
        }
        return best;
    }

    /** The next tighter join radius after {@code r1}: half, or ({@code banded}) no tighter than {@code least}. */
    private static double next(double r1, double least, boolean banded) {
        return banded && r1 > least ? Math.max(r1 / 2, least) : r1 / 2;
    }

    /**
     * How far ace {@code a}'s join (the turn in the scratch, then its straight's end) takes its hull off the field's
     * sides, or ({@code banded}) its centre out of its station's band, px: a join turns the way that stays in.
     */
    private double outside(int a, boolean banded) {
        SquadronSpec.Station station = stations[a];
        double from = StrictMath.atan2(eY - gC1y, eX - gC1x);
        double out = 0;
        for (int i = 0; i <= 8; i++) {
            double px = i == 0 ? gT2x : gC1x + gR1 * Trig.cos(from + gSweep * i / 8);
            double py = i == 0 ? gT2y : gC1y + gR1 * Trig.sin(from + gSweep * i / 8);
            out = Math.max(out, Math.max(hullW / 2 - px, px - (PlayField.WIDTH - hullW / 2)));
            if (banded) {
                out = Math.max(out, Math.max(py - station.yMax(), station.yMin() - py));
            }
        }
        return out;
    }

    /**
     * How far ace {@code a}'s join (the turn in the scratch, then its straight) comes inside its keep-off from the ship
     * with the margin, px (0 for an ace without a keep-off): a join turns the way away from the ship.
     */
    private double nearShip(int a) {
        double keep = keepOff(a);
        if (keep <= 0) {
            return 0;
        }
        keep += MARGIN;
        double from = StrictMath.atan2(eY - gC1y, eX - gC1x);
        double t1x = gC1x + gR1 * Trig.cos(from + gSweep);
        double t1y = gC1y + gR1 * Trig.sin(from + gSweep);
        double in = 0;
        for (int i = 1; i <= 8; i++) {
            double angle = from + gSweep * i / 8;
            double px = gC1x + gR1 * Trig.cos(angle);
            double py = gC1y + gR1 * Trig.sin(angle);
            in = Math.max(in, keep - Math.hypot(px - shipX, py - shipY));
            double lx = t1x + (gT2x - t1x) * i / 8;
            double ly = t1y + (gT2y - t1y) * i / 8;
            in = Math.max(in, keep - Math.hypot(lx - shipX, ly - shipY));
        }
        return in;
    }

    /** Appends the join {@link #onto} finds (its turn and straight, {@link AceLeg#RETURN}) onto the circle. */
    private void joinOnto(int a, double cx, double cy, double r, int dir, double speed) {
        double join = onto(a, cx, cy, r, dir, true);
        if (join == Double.POSITIVE_INFINITY) {
            // No turn reaches it (the path inside the circle): straight to its nearest point.
            double rx = eX - cx;
            double ry = eY - cy;
            double d = Math.hypot(rx, ry);
            line(a, AceLeg.RETURN, cx + (d > 0 ? rx / d : 1) * r, cy + (d > 0 ? ry / d : 0) * r, speed);
            return;
        }
        if (gSide == 0) {
            return;
        }
        double tx = gT2x;
        double ty = gT2y;
        arcTo(a, AceLeg.RETURN, speed);
        line(a, AceLeg.RETURN, tx, ty, speed);
    }

    /** Appends the turn in the scratch (if any) at no more than its radius's join speed; returns the path end's x. */
    private double arcTo(int a, AceLeg tag, double speed) {
        double sweep = gSweep;
        double r1 = gR1;
        if (Math.abs(sweep) * r1 > 0.5) {
            arc(a, tag, gC1x, gC1y, r1, sweep, Math.min(speed, Math.max(Math.min(eV, r1 * TURN_CAP), r1 * JOIN_TURN)));
        }
        endPose(a);
        return eX;
    }

    /** Appends an arc round (cx, cy) from the path's end to the angle {@code to}, turning {@code dir}. */
    private void arcRound(int a, AceLeg tag, double cx, double cy, double r, double to, int dir, double speed) {
        endPose(a);
        double sweep = sweep(StrictMath.atan2(eY - cy, eX - cx), to, dir);
        if (Math.abs(sweep) * r > 0.5) {
            arc(a, tag, cx, cy, r, sweep, speed);
        }
    }

    /**
     * Turns from the path's end toward (tx, ty) and flies straight to it at {@code speed}: the turn tagged {@code
     * joinTag} ({@code banded}: the way that stays in the station's band, if it is near as short), the straight {@code
     * tag}.
     */
    private void go(int a, AceLeg tag, AceLeg joinTag, double tx, double ty, double speed, boolean banded) {
        endPose(a);
        double best = Double.POSITIVE_INFINITY;
        int bestSide = 0;
        double bestR = 0;
        double bestOut = 0;
        // Its join turn, or (when that would turn it more than half round, or step F6s2: leave its band) its tightest
        // turn at its speed, or tighter still (slowing into it) to keep in its band.
        for (int tight = 0;
                tight < 4
                        && (bestSide == 0
                                || (tight < 2 && Math.abs(gSweepBest) > StrictMath.PI)
                                || (banded && bestOut > 1));
                tight++) {
            double r1 = tight < 2
                    ? Math.max(JOIN_RADIUS, eV / (tight == 0 ? JOIN_TURN : TURN_CAP))
                    : Math.max(eV / BAND_TURN, JOIN_RADIUS / (tight == 2 ? 1 : 2));
            for (int s = 1; s >= -1; s -= 2) {
                double len = tangent(r1, s, tx, ty, 0);
                double out = 0;
                if (len < Double.POSITIVE_INFINITY) {
                    out = outside(a, banded);
                    len += (banded ? BAND_COST : 4) * out + 4 * nearShip(a);
                }
                if (len < best) {
                    best = len;
                    bestOut = out;
                    bestSide = s;
                    bestR = r1;
                    gSweepBest = gSweep;
                }
            }
        }
        if (bestSide != 0) {
            tangent(bestR, bestSide, tx, ty, 0);
            arcTo(a, joinTag, speed);
        }
        line(a, tag, tx, ty, speed);
    }

    /**
     * Turns from the path's end so as to arrive at (tx, ty) pointing along (hx, hy) at {@code speed}: a turn, a
     * straight and a turn onto the arrival, all tagged {@code tag}.
     */
    private void into(int a, AceLeg tag, double tx, double ty, double hx, double hy, double speed) {
        double r2 = Math.max(JOIN_RADIUS, speed / JOIN_TURN);
        endPose(a);
        if (Math.hypot(eX - tx, eY - ty) < 1 && eHx * hx + eHy * hy > 0.999) {
            return;
        }
        double best = Double.POSITIVE_INFINITY;
        int bestDir = 0;
        for (int dir = 1; dir >= -1; dir -= 2) {
            double cx = tx - dir * r2 * hy;
            double cy = ty + dir * r2 * hx;
            double join = onto(a, cx, cy, r2, dir, false);
            if (join == Double.POSITIVE_INFINITY) {
                continue;
            }
            double arrive = sweep(StrictMath.atan2(gT2y - cy, gT2x - cx), StrictMath.atan2(ty - cy, tx - cx), dir);
            // Step F6s2: the arrival's turn away from the other aces' bands if it is near as short (step F6s3: by far:
            // the ribbon's turn onto its pass reached into the leader's band).
            double len = join + r2 * Math.abs(arrive) + 8 * intrusion(a, cy, r2);
            if (len < best) {
                best = len;
                bestDir = dir;
            }
        }
        if (bestDir == 0) {
            go(a, tag, tag, tx, ty, speed, false);
            return;
        }
        double cx = tx - bestDir * r2 * hy;
        double cy = ty + bestDir * r2 * hx;
        onto(a, cx, cy, r2, bestDir, false);
        if (gSide != 0) {
            double lx = gT2x;
            double ly = gT2y;
            arcTo(a, tag, speed);
            line(a, tag, lx, ly, speed);
        }
        arcRound(a, tag, cx, cy, r2, StrictMath.atan2(ty - cy, tx - cx), bestDir, speed);
    }

    private void line(int a, AceLeg tag, double toX, double toY, double speed) {
        if (legCount[a] >= LEGS) {
            return;
        }
        int k = a * LEGS + legCount[a];
        double fromX = legCount[a] == 0 ? pathX[a] : endX(k - 1);
        double fromY = legCount[a] == 0 ? pathY[a] : endY(k - 1);
        double length = Math.hypot(toX - fromX, toY - fromY);
        if (length < 1e-6) {
            return;
        }
        legKind[k] = LINE;
        legTag[k] = tag.ordinal();
        legAx[k] = fromX;
        legAy[k] = fromY;
        legBx[k] = toX;
        legBy[k] = toY;
        legR[k] = 0;
        legLength[k] = length;
        legSpeed[k] = speed;
        legEx[k] = (toX - fromX) / length;
        legEy[k] = (toY - fromY) / length;
        legCount[a]++;
    }

    /** An arc round (cx, cy) of {@code r} from the path's end, turning {@code sweep} radians (positive: anticlockwise). */
    private void arc(int a, AceLeg tag, double cx, double cy, double r, double sweep, double speed) {
        if (legCount[a] >= LEGS) {
            return;
        }
        int k = a * LEGS + legCount[a];
        double fromX = legCount[a] == 0 ? pathX[a] : endX(k - 1);
        double fromY = legCount[a] == 0 ? pathY[a] : endY(k - 1);
        legKind[k] = ARC;
        legTag[k] = tag.ordinal();
        legAx[k] = cx;
        legAy[k] = cy;
        legBx[k] = StrictMath.atan2(fromY - cy, fromX - cx);
        legBy[k] = sweep;
        legR[k] = r;
        legLength[k] = Math.abs(sweep) * r;
        legSpeed[k] = speed;
        double end = legBx[k] + sweep;
        double turn = sweep >= 0 ? 1 : -1;
        legEx[k] = -Trig.sin(end) * turn;
        legEy[k] = Trig.cos(end) * turn;
        legCount[a]++;
    }

    private double endX(int k) {
        return legKind[k] == ARC ? legAx[k] + legR[k] * Trig.cos(legBx[k] + legBy[k]) : legBx[k];
    }

    private double endY(int k) {
        return legKind[k] == ARC ? legAy[k] + legR[k] * Trig.sin(legBx[k] + legBy[k]) : legBy[k];
    }

    // The step.

    /**
     * One step: the flying aces move along their paths (a hold's next lap built as one ends, a move back to its
     * station as it ends; a dive pulls out early if the ship came within its pull-out start; an aiming ace creeps along
     * its nose), the tumbling hulls fall, then the avoidance and the solver keep the hulls apart and the keep-offs, and
     * the noses and banks follow the motion.
     */
    void step(double sx, double sy, BossActions actions) {
        shipX = sx;
        shipY = sy;
        paces();
        for (int a = 0; a < count; a++) {
            if (tumbling[a]) {
                fall(a);
            } else if (flying[a]) {
                moveTicks[a]++;
                fly(a, actions);
            }
        }
        avoid();
        solve();
        for (int a = 0; a < count; a++) {
            if (flying[a]) {
                face(a);
            }
        }
    }

    /** The hulls after the kill: the dead ones tumble on (the last ace's in its chain). */
    void fallOnly() {
        for (int a = 0; a < count; a++) {
            if (tumbling[a]) {
                fall(a);
            }
        }
    }

    private void fall(int a) {
        double ease = TUMBLE_EASE * SimStep.SECONDS;
        vx[a] += Math.clamp(side[a] * TUMBLE_SIDE - vx[a], -ease, ease);
        vy[a] += Math.clamp(-TUMBLE_DOWN - vy[a], -ease, ease);
        double spinEase = SPIN_EASE * SimStep.SECONDS;
        spin[a] += Math.clamp(side[a] * TUMBLE_SPIN - spin[a], -spinEase, spinEase);
        x[a] += vx[a] * SimStep.SECONDS;
        y[a] += vy[a] * SimStep.SECONDS;
        facing[a] = Math.IEEEremainder(facing[a] + spin[a] * SimStep.SECONDS, 2 * StrictMath.PI);
        if (x[a] + hullW / 2 < 0 || x[a] - hullW / 2 > PlayField.WIDTH || y[a] + hullH / 2 < 0) {
            tumbling[a] = false;
        }
    }

    /** The pincer's flankers pace their approaches so they turn into their passes together. */
    private void paces() {
        for (int a = 0; a < count; a++) {
            int b = partner[a];
            pace[a] = 0;
            if (b < 0 || !flying[a] || !flying[b] || move[a] != AceMove.PINCER.ordinal() || partner[b] != a) {
                partner[a] = -1;
                continue;
            }
            double ra = toPass(a);
            double rb = toPass(b);
            if (ra <= 0 || rb <= 0) {
                continue;
            }
            double eta = Math.max(ra / stations[a].dashSpeed(), rb / stations[b].dashSpeed());
            pace[a] = Math.max(PACE_MIN, ra / eta);
        }
    }

    /** How far ace {@code a}'s path runs before its pass starts, px (0 once passing or with none). */
    private double toPass(int a) {
        double rest = 0;
        for (int l = leg[a]; l < legCount[a]; l++) {
            int k = a * LEGS + l;
            if (legTag[k] == AceLeg.PASS.ordinal()) {
                return rest;
            }
            rest += l == leg[a] ? legLength[k] - along[a] : legLength[k];
        }
        return 0;
    }

    private void fly(int a, BossActions actions) {
        if (move[a] == AceMove.HOLD.ordinal() && aim[a] && !pinched(a)) {
            if (!aiming[a]) {
                aiming[a] = true;
                fold(a);
                restart(a);
            }
            creep(a);
            return;
        }
        if (aiming[a]) {
            aiming[a] = false;
            fold(a);
            restart(a);
        }
        if (legCount[a] > 0 && Math.hypot(offX[a], offY[a]) > FOLD && homing(a)) {
            // Pushed far off its way home: it flies on from where it is, as it moves.
            fold(a);
            restart(a);
        }
        if (legCount[a] == 0 || laneChange(a)) {
            nextPath(a, actions);
            if (legCount[a] == 0) {
                return;
            }
        }
        AceMove m = MOVES[move[a]];
        if ((m == AceMove.DIVE || (m == AceMove.OVERHEAD && stage[a] == 1))
                && legTag[a * LEGS + leg[a]] == AceLeg.RUN.ordinal()
                && Math.hypot(shipX - x[a], shipY - y[a]) <= pullOutStart(pullOut[a])) {
            // The ship came within the pull-out's start: it pulls out now.
            restart(a);
            pullOutFrom(a, pathX[a], pathY[a], tanX[a], tanY[a]);
        }
        int k = a * LEGS + leg[a];
        double target = speedOf(a, k);
        double rest = legLength[k] - along[a];
        double next = leg[a] + 1 < legCount[a] ? speedOf(a, k + 1) : endSpeed(a);
        target = Math.min(target, Math.sqrt(next * next + 2 * ACCEL * Math.max(0, rest)));
        double change = ACCEL * SimStep.SECONDS;
        spd[a] += Math.clamp(target - spd[a], -change, change);
        int was = leg[a];
        double reach = spd[a] * SimStep.SECONDS;
        for (int guard = 0; reach > 0 && guard < 2 * LEGS; guard++) {
            k = a * LEGS + leg[a];
            double left = legLength[k] - along[a];
            if (reach < left) {
                along[a] += reach;
                reach = 0;
            } else {
                reach -= left;
                along[a] = 0;
                leg[a]++;
                if (leg[a] == legCount[a]) {
                    leg[a]--;
                    along[a] = legLength[k];
                    eval(a);
                    midStep = true;
                    nextPath(a, actions);
                    midStep = false;
                    was = -1;
                    if (legCount[a] == 0) {
                        break;
                    }
                }
            }
        }
        eval(a);
        legTicks[a] = leg[a] == was ? legTicks[a] + 1 : 0;
    }

    /**
     * Step F6s2: whether the feinter must change lanes with the ship already near its keep-off: the lane change goes
     * before an aim (easy's: the ship flew into its lane's keep-off while it aimed and pushed it out of its band).
     */
    private boolean pinched(int a) {
        return roles[a] == SquadronSpec.Role.FEINTER
                && ((laneWanted(a) && Math.hypot(shipX - x[a], shipY - y[a]) < keepOff(a) + MARGIN + hullW)
                        || offLane(a));
    }

    /**
     * Step F6s3: whether the feinter is away from its lane's orbit (its lane changed as its aim began: it crosses first,
     * never aims from the old lane with the ship pressing it up out of its band, Level 40 easy's 120 px).
     */
    private boolean offLane(int a) {
        double r = stations[a].speed() / ORBIT_TURN;
        double cx = side[a] > 0 ? laneX(a) - r : laneX(a) + r;
        return Math.abs(x[a] - cx) > hullW + r;
    }

    /** Step F6s2: whether the feinter must change lanes or is on its way over the ship to the other. */
    boolean crossing(int a) {
        return roles[a] == SquadronSpec.Role.FEINTER
                && move[a] == AceMove.HOLD.ordinal()
                && (laneWanted(a) || (legCount[a] > 0 && legTag[a * LEGS + leg[a]] == AceLeg.APPROACH.ordinal()));
    }

    /** Whether ace {@code a} holds its station or flies home from a move (its path may be built anew). */
    private boolean homing(int a) {
        return move[a] == AceMove.HOLD.ordinal() || legTag[a * LEGS + leg[a]] == AceLeg.RETURN.ordinal();
    }

    /**
     * Whether the feinter on its orbit must change lanes now: the ship crossed the field's centre (by {@link
     * #LANE_HYSTERESIS}) to its side (it re-plans at once rather than at its lap's end).
     */
    private boolean laneChange(int a) {
        if (roles[a] != SquadronSpec.Role.FEINTER || move[a] != AceMove.HOLD.ordinal() || legCount[a] == 0) {
            return false;
        }
        return laneWanted(a);
    }

    /** Step F6s2: whether the ship is past the field's centre on the feinter's lane's side (by its hysteresis). */
    private boolean laneWanted(int a) {
        double centre = PlayField.WIDTH / 2.0;
        double lane = laneHysteresis(a);
        return (shipX > centre + lane && side[a] > 0) || (shipX < centre - lane && side[a] < 0);
    }

    /**
     * Step F6s2: how far past the field's centre the ship comes before the feinter changes lanes: {@link
     * #LANE_HYSTERESIS}, less when its keep-off would reach its orbit first (easy's 130 px: the ship there flew into its
     * lane's keep-off and pushed it out of its band).
     */
    private double laneHysteresis(int a) {
        double inner = PlayField.WIDTH / 2.0 - stations[a].x() - 2 * stations[a].speed() / ORBIT_TURN;
        return Math.clamp(inner - keepOff(a) - LANE_ROOM, 0, LANE_HYSTERESIS);
    }

    /** Leg {@code k}'s speed for ace {@code a} (a pincer's approach at its pace). */
    private double speedOf(int a, int k) {
        double v = legSpeed[k];
        if (pace[a] > 0 && legTag[k] == AceLeg.APPROACH.ordinal()) {
            v = Math.min(v, pace[a]);
        }
        return v;
    }

    /** The speed ace {@code a}'s path ends at: its station's (a hold follows), the dash before the overhead's dive. */
    private double endSpeed(int a) {
        SquadronSpec.Station station = stations[a];
        if (move[a] == AceMove.OVERHEAD.ordinal() && stage[a] == 0) {
            return station.dashSpeed();
        }
        return station.speed();
    }

    /**
     * Ace {@code a} aims from its station: its nose turns onto the ship and it slows to a creep along it, slowing to a
     * stop short of its keep-off (or the ship's ring). Step F6s4: its nose leads only as far as its motion lets it slip
     * ({@link #face}: it braked on its way with its nose already on the ship, a slide across its nose); its way levels
     * out near its band's edge into a slow drift along it, never a stop there ({@link #AIM_DRIFT}: too slow to slide).
     */
    private void creep(int a) {
        SquadronSpec.Station station = stations[a];
        double bx = shipX - pathX[a];
        double by = shipY - pathY[a];
        double d = Math.hypot(bx, by);
        double wx = d > 0 ? bx / d : tanX[a];
        double wy = d > 0 ? by / d : tanY[a];
        double shipRoom = d - Math.max(keepOff(a), SHIP_RING) - hullH;
        double edge = wy < 0 ? pathY[a] - station.yMin() : station.yMax() - pathY[a];
        wy *= Math.clamp(edge / AIM_ROOM, 0, 1);
        if (Math.abs(wx) < 1e-6 && Math.abs(wy) < 1e-6) {
            // Right over (or under) the ship at its band's edge: it drifts on the way it flew, inward on the field.
            wx = tanX[a] >= 0 ? 1 : -1;
            if ((wx > 0 && pathX[a] > PlayField.WIDTH - hullW / 2 - AIM_ROOM)
                    || (wx < 0 && pathX[a] < hullW / 2 + AIM_ROOM)) {
                wx = -wx;
            }
        }
        if (spd[a] <= 2 * AIM_SPEED) {
            // Slowed to its creep, its way turns onto the ship (faster, it brakes on its way first, its nose leading
            // only as far as its slip lets it, {@link #face}).
            double turn = StrictMath.atan2(tanX[a] * wy - tanY[a] * wx, tanX[a] * wx + tanY[a] * wy);
            double step = Math.clamp(turn, -AIM_TURN * SimStep.SECONDS, AIM_TURN * SimStep.SECONDS);
            double c = Trig.cos(step);
            double s = Trig.sin(step);
            double hx = tanX[a] * c - tanY[a] * s;
            double hy = tanX[a] * s + tanY[a] * c;
            double h = Math.hypot(hx, hy);
            tanX[a] = hx / h;
            tanY[a] = hy / h;
        }
        double room = tanY[a] < 0 ? pathY[a] - station.yMin() : station.yMax() - pathY[a];
        double target = AIM_SPEED * Math.clamp(Math.min(room, shipRoom) / AIM_ROOM, 0, 1);
        if (shipRoom > AIM_ROOM) {
            // (near the ship it still slows to a stop short of its keep-off, its nose on it: a dive's tell)
            target = Math.max(target, AIM_DRIFT);
        }
        double change = ACCEL * SimStep.SECONDS;
        spd[a] += Math.clamp(target - spd[a], -change, change);
        pathX[a] += tanX[a] * spd[a] * SimStep.SECONDS;
        // Step F6s3: and never creeps out past its band's edge (an aim begun fast toward it braked beyond it).
        // Step F6s4: only from inside it (an aim begun out of its band, the feinter's way over the ship, brakes and
        // turns toward it: held where it was, it stopped dead).
        double ny = pathY[a] + tanY[a] * spd[a] * SimStep.SECONDS;
        if (ny > station.yMax() && tanY[a] > 0 && pathY[a] <= station.yMax()) {
            ny = station.yMax();
        } else if (ny < station.yMin() && tanY[a] < 0 && pathY[a] >= station.yMin()) {
            ny = station.yMin();
        }
        pathY[a] = ny;
        legTicks[a]++;
    }

    /** Ace {@code a}'s path ended: a hold's next lap, the overhead's dive, or back to its station. */
    private void nextPath(int a, BossActions actions) {
        AceMove m = MOVES[move[a]];
        restart(a);
        if (m == AceMove.OVERHEAD && stage[a] == 0) {
            stage[a] = 1;
            dive(a);
            return;
        }
        if (m != AceMove.HOLD) {
            if (m != AceMove.ENTRY && m != AceMove.LOOP) {
                // Step F6s2: home from where its move left it (its avoidance's offset folded into the way back, so it
                // never lingers out of its band once its way is in it); an entry and a loop end on its station.
                fold(a);
            }
            move[a] = AceMove.HOLD.ordinal();
            moveTicks[a] = 0;
            stage[a] = 0;
            partner[a] = -1;
        }
        holdLeg(a, actions);
    }

    /**
     * Step F6s2: where ace {@code a}'s path will be {@code seconds} ahead (its current leg at its speed now, the next at
     * theirs, straight on past its end; an aiming ace where it is) and its direction there, into {@link #hX}...
     */
    private void ahead(int a, double seconds) {
        if (aiming[a] || legCount[a] == 0) {
            double d = aiming[a] ? 0 : spd[a] * seconds;
            hX = pathX[a] + tanX[a] * d;
            hY = pathY[a] + tanY[a] * d;
            hTx = tanX[a];
            hTy = tanY[a];
            return;
        }
        double t = seconds;
        double at = along[a];
        for (int l = leg[a]; ; l++) {
            int k = a * LEGS + l;
            double v = l == leg[a] ? spd[a] : speedOf(a, k);
            double left = legLength[k] - at;
            if (l == legCount[a] - 1 || v * t < left) {
                pointOn(k, at + v * t);
                double over = at + v * t - legLength[k];
                if (over > 0 && move[a] == AceMove.OVERHEAD.ordinal() && stage[a] == 0) {
                    // Its run above the field ends in its dive at the ship's spot (built as it gets there).
                    double ex = hX - hTx * over;
                    double ey = hY - hTy * over;
                    double d = Math.hypot(shipX - ex, shipY - ey);
                    if (d > 0) {
                        hTx = (shipX - ex) / d;
                        hTy = (shipY - ey) / d;
                        hX = ex + hTx * over;
                        hY = ey + hTy * over;
                    }
                } else if (over > 0) {
                    // Step F6s3: any other path ends on (or by) its station, where it holds next: its look stops at
                    // the end (straight on, an entry's dash looked up into the bands above and steered it off the
                    // field).
                    hX -= hTx * over;
                    hY -= hTy * over;
                }
                return;
            }
            t -= v > 0 ? left / v : t;
            at = 0;
        }
    }

    /** The point {@code s} px along leg {@code k} (straight on past its end) and its direction, into {@link #hX}... */
    private void pointOn(int k, double s) {
        double over = Math.max(0, s - legLength[k]);
        double t = legLength[k] > 0 ? Math.min(s, legLength[k]) / legLength[k] : 1;
        if (legKind[k] == ARC) {
            double angle = legBx[k] + legBy[k] * t;
            double c = Trig.cos(angle);
            double sn = Trig.sin(angle);
            double turn = legBy[k] >= 0 ? 1 : -1;
            hTx = -sn * turn;
            hTy = c * turn;
            hX = legAx[k] + legR[k] * c + hTx * over;
            hY = legAy[k] + legR[k] * sn + hTy * over;
        } else {
            hTx = legEx[k];
            hTy = legEy[k];
            hX = legAx[k] + (legBx[k] - legAx[k]) * t + hTx * over;
            hY = legAy[k] + (legBy[k] - legAy[k]) * t + hTy * over;
        }
    }

    /** Ace {@code a}'s path point and direction at its leg and distance. */
    private void eval(int a) {
        if (legCount[a] == 0) {
            return;
        }
        int k = a * LEGS + leg[a];
        double t = legLength[k] > 0 ? along[a] / legLength[k] : 1;
        if (legKind[k] == ARC) {
            double angle = legBx[k] + legBy[k] * t;
            double c = Trig.cos(angle);
            double s = Trig.sin(angle);
            pathX[a] = legAx[k] + legR[k] * c;
            pathY[a] = legAy[k] + legR[k] * s;
            double turn = legBy[k] >= 0 ? 1 : -1;
            tanX[a] = -s * turn;
            tanY[a] = c * turn;
        } else {
            pathX[a] = legAx[k] + (legBx[k] - legAx[k]) * t;
            pathY[a] = legAy[k] + (legBy[k] - legAy[k]) * t;
            tanX[a] = legEx[k];
            tanY[a] = legEy[k];
        }
    }

    /**
     * The nose follows the motion (the path's and the avoidance's), turning at most {@link #NOSE_RATE}; an aiming ace's
     * is its aim; the bank leans into the turn.
     */
    private void face(int a) {
        double mx = tanX[a] * spd[a] + ovX[a];
        double my = tanY[a] * spd[a] + ovY[a];
        double m = Math.hypot(mx, my);
        double way = m < 5 ? Enemy.heading(tanX[a], tanY[a]) : Enemy.heading(mx, my);
        double target = way;
        if (aiming[a]) {
            // Step F6s4: on the ship only as far as its motion lets it slip across its nose (none of it backwards).
            double off = Math.IEEEremainder(Enemy.heading(shipX - x[a], shipY - y[a]) - way, 2 * StrictMath.PI);
            double slip = m > AIM_SLIP ? StrictMath.asin(AIM_SLIP / m) : StrictMath.PI;
            target = way + Math.clamp(off, -slip, slip);
        }
        double most = NOSE_RATE * SimStep.SECONDS;
        double turn = Math.clamp(Math.IEEEremainder(target - facing[a], 2 * StrictMath.PI), -most, most);
        facing[a] = Math.IEEEremainder(facing[a] + turn, 2 * StrictMath.PI);
        double lean = Math.clamp(turn * SimStep.PER_SECOND / BANK_FULL, -1, 1) * MAX_BANK;
        double rate = BANK_RATE * SimStep.SECONDS;
        bank[a] += Math.clamp(lean - bank[a], -rate, rate);
    }

    // The avoidance and the solver.

    /**
     * The avoidance: each flying ace's path {@link #LOOK} s ahead, pushed apart by the rules with a {@link #MARGIN}
     * (the separation; the keep-offs of the aces that yield; a holding ace kept in its band and on the field), gives
     * each its wanted offset; the offset eases toward it (a damped spring of bounded acceleration and speed), so an ace
     * turns away early.
     */
    private void avoid() {
        // Step F6s2: from the farthest look in to now, each look starting from the offsets the one beyond it wanted, so
        // the offset now steers early out of a meeting ahead without walking into one nearer.
        java.util.Arrays.fill(wantX, 0);
        java.util.Arrays.fill(wantY, 0);
        java.util.Arrays.fill(wantLoX, Double.NEGATIVE_INFINITY);
        java.util.Arrays.fill(wantLoY, Double.NEGATIVE_INFINITY);
        java.util.Arrays.fill(wantHiX, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(wantHiY, Double.POSITIVE_INFINITY);
        for (int look = LOOKS; look >= 0; look--) {
            soft(look * LOOK);
            for (int a = 0; a < count; a++) {
                wantX[a] = qx[a] - baseX[a];
                wantY[a] = qy[a] - baseY[a];
                if (look > 0 && look <= BAND_LOOKS && flying[a]) {
                    // Step F6s4: the offset that keeps it in its band and on the field where its path will be (a look
                    // that contradicts the farther ones, its path crossing the band, is left out).
                    SquadronSpec.Station station = stations[a];
                    double bx = baseX[a];
                    double by = baseY[a];
                    double lx = Math.max(wantLoX[a], Math.min(bx, hullW / 2) - bx);
                    double hx = Math.min(wantHiX[a], Math.max(bx, PlayField.WIDTH - hullW / 2) - bx);
                    double ly = Math.max(wantLoY[a], (settled[a] ? station.yMin() : Math.min(by, station.yMin())) - by);
                    double hy = Math.min(wantHiY[a], (settled[a] ? station.yMax() : Math.max(by, station.yMax())) - by);
                    if (lx <= hx && ly <= hy) {
                        wantLoX[a] = lx;
                        wantHiX[a] = hx;
                        wantLoY[a] = ly;
                        wantHiY[a] = hy;
                    }
                }
            }
        }
        for (int a = 0; a < count; a++) {
            if (flying[a] && banded(a)) {
                // Step F6s2: an offset wanted ahead never takes an ace on its station out of its band (or the field)
                // now; step F6s3: once settled in its band, never out of it (its path's join may swing out); step
                // F6s4: nor where its path will be over the next looks (it eases back before its orbit nears the edge).
                double lx = loX(a) - pathX[a];
                double hx = hiX(a) - pathX[a];
                double ly = loY(a) - pathY[a];
                double hy = hiY(a) - pathY[a];
                if (Math.max(lx, wantLoX[a]) <= Math.min(hx, wantHiX[a])
                        && Math.max(ly, wantLoY[a]) <= Math.min(hy, wantHiY[a])) {
                    lx = Math.max(lx, wantLoX[a]);
                    hx = Math.min(hx, wantHiX[a]);
                    ly = Math.max(ly, wantLoY[a]);
                    hy = Math.min(hy, wantHiY[a]);
                }
                wantX[a] = Math.clamp(wantX[a], lx, hx);
                wantY[a] = Math.clamp(wantY[a], ly, hy);
            }
        }
        double dt = SimStep.SECONDS;
        for (int a = 0; a < count; a++) {
            if (!flying[a]) {
                continue;
            }
            double vx0 = ovX[a];
            double vy0 = ovY[a];
            double ax = SPRING * SPRING * (wantX[a] - offX[a]) - 2 * SPRING * ovX[a];
            double ay = SPRING * SPRING * (wantY[a] - offY[a]) - 2 * SPRING * ovY[a];
            double acc = Math.hypot(ax, ay);
            if (acc > PUSH_ACCEL) {
                ax *= PUSH_ACCEL / acc;
                ay *= PUSH_ACCEL / acc;
            }
            ovX[a] += ax * dt;
            ovY[a] += ay * dt;
            double v = Math.hypot(ovX[a], ovY[a]);
            if (v > PUSH_SPEED) {
                ovX[a] *= PUSH_SPEED / v;
                ovY[a] *= PUSH_SPEED / v;
            }
            boolean banded = banded(a);
            // Step F6s3: a banded ace's limits, never inside where it was last step (no snap in).
            double px = pathX[a] + offX[a];
            double py = pathY[a] + offY[a];
            double x0 = Math.min(loX(a), x[a]);
            double x1 = Math.max(hiX(a), x[a]);
            double y0 = Math.min(loY(a), y[a]);
            double y1 = Math.max(hiY(a), y[a]);
            if (banded) {
                // Step F6s3: the offset brakes ahead of its band's edges and the field's and stops at them (it never
                // overshoots them and leaves the solver a clamp: the leader's shoves at his band's top); its path's own
                // way goes on (its joins keep in its band).
                double mx = tanX[a] * spd[a];
                double my = tanY[a] * spd[a];
                // (no faster than its acceleration a step: a band that starts to hold it as its way home enters it
                // never stops it at once)
                double ease = PUSH_ACCEL * dt;
                ovX[a] = Math.clamp(
                        ovX[a],
                        Math.min(-outward(px - x0, -mx), vx0 + ease),
                        Math.max(outward(x1 - px, mx), vx0 - ease));
                ovY[a] = Math.clamp(
                        ovY[a],
                        Math.min(-outward(py - y0, -my), vy0 + ease),
                        Math.max(outward(y1 - py, my), vy0 - ease));
            }
            if (aiming[a]) {
                // Step F6s4: an aiming ace gives way along its way (its creep's line onto the ship), never more
                // sideways under its nose: any sideways motion it had only eases out; and never slower than its drift
                // (its nose stays ahead of its motion, never a hover).
                double along = Math.max(-Math.max(0, spd[a] - AIM_DRIFT), ovX[a] * tanX[a] + ovY[a] * tanY[a]);
                double latX = ovX[a] - (ovX[a] * tanX[a] + ovY[a] * tanY[a]) * tanX[a];
                double latY = ovY[a] - (ovX[a] * tanX[a] + ovY[a] * tanY[a]) * tanY[a];
                double was = Math.abs(vx0 * tanY[a] - vy0 * tanX[a]);
                double lat = Math.hypot(latX, latY);
                double keep = lat > 0 ? Math.min(lat, Math.max(0, was - PUSH_ACCEL * dt)) / lat : 0;
                ovX[a] = along * tanX[a] + latX * keep;
                ovY[a] = along * tanY[a] + latY * keep;
            }
            offX[a] += ovX[a] * dt;
            offY[a] += ovY[a] * dt;
            if (banded) {
                offX[a] = Math.clamp(offX[a], Math.min(0, x0 - pathX[a]), Math.max(0, x1 - pathX[a]));
                offY[a] = Math.clamp(offY[a], Math.min(0, y0 - pathY[a]), Math.max(0, y1 - pathY[a]));
            }
            SquadronSpec.Station station = stations[a];
            double y = pathY[a] + offY[a];
            settled[a] = held(a) && (settled[a] || (y >= station.yMin() - 0.5 && y <= station.yMax() + 0.5));
        }
    }

    /**
     * Step F6s3: the most offset speed toward an edge {@code room} px away that, with its path's own speed {@code path}
     * toward it, still stops there at the offset's acceleration. Step F6s4: less than none when its path runs at the
     * edge faster than that (its offset then eases back inward ahead of it: at none, the edge's clamp stopped the ace's
     * path dead there, Vesper's 150-230 px/s vertical stops at her band's edges).
     */
    private static double outward(double room, double path) {
        return Math.sqrt(2 * PUSH_ACCEL * Math.max(0, room)) - Math.max(0, path);
    }

    /** Step F6s3: a banded ace's least and most x (the field, or no farther off it than its path). */
    private double loX(int a) {
        return Math.min(pathX[a], hullW / 2);
    }

    private double hiX(int a) {
        return Math.max(pathX[a], PlayField.WIDTH - hullW / 2);
    }

    /** Step F6s3: a banded ace's least and most y (its band once settled in it, else no farther out than its path). */
    private double loY(int a) {
        double band = stations[a].yMin();
        return settled[a] ? band : Math.min(pathY[a], band);
    }

    private double hiY(int a) {
        double band = stations[a].yMax();
        return settled[a] ? band : Math.max(pathY[a], band);
    }

    /**
     * The avoidance's pushes on the flying aces' paths {@code look} s ahead (into {@link #qx}, from {@link #baseX}): the
     * keep-offs (with the margin for an ace that yields; none for a dive, its pull-out keeps it), a holding ace in its
     * band and on the field, the separation with the margin.
     */
    private void soft(double look) {
        for (int a = 0; a < count; a++) {
            if (!flying[a]) {
                continue;
            }
            ahead(a, look);
            baseX[a] = hX;
            baseY[a] = hY;
            baseTx[a] = hTx;
            baseTy[a] = hTy;
            qx[a] = baseX[a] + wantX[a];
            qy[a] = baseY[a] + wantY[a];
        }
        for (int round = 0; round < SOFT_ROUNDS; round++) {
            for (int a = 0; a < count; a++) {
                if (flying[a]) {
                    confine(qx, qy, a);
                    if (keepOff(a) > 0 && !diving(a)) {
                        keepOut(qx, qy, a, keepOff(a) + (yields(a) ? MARGIN : toldMargin(a)));
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                for (int j = i + 1; j < count; j++) {
                    if (flying[i] && flying[j]) {
                        // Step F6s3: the pincer's flankers pass on lanes placed a separation apart (no margin).
                        double gap = partner[i] == j ? separation : separation + MARGIN;
                        apart(qx, qy, i, j, gap, round >= SOFT_ROUNDS / 2, true);
                    }
                }
            }
        }
    }

    /**
     * Step F6s3: a told move's margin over its keep-off (step F6s4: the pincer's too, its lower lane placed that far out
     * where the room allows).
     */
    private double toldMargin(int a) {
        return TOLD_MARGIN;
    }

    /** Whether ace {@code a} flies a dive (the feinter's, the overhead's after its run above the field). */
    private boolean diving(int a) {
        return move[a] == AceMove.DIVE.ordinal() || (move[a] == AceMove.OVERHEAD.ordinal() && stage[a] == 1);
    }

    /**
     * The flying aces' places: each its path's point plus its offset; then rounds of exact pushes: an ace out of its
     * keep-off from the ship (the leader only out of the ship's ring a push put it in), a holding ace back inside the
     * field, then two hulls nearer than the separation apart (the one in a told move holds, a holding or entering one
     * yields, never out of its band; what the field's edge or the band takes back from one goes to the other), the
     * separation last. What it pushed stays in the offset.
     */
    private void solve() {
        for (int a = 0; a < count; a++) {
            if (!flying[a]) {
                continue;
            }
            x[a] = pathX[a] + offX[a];
            y[a] = pathY[a] + offY[a];
            qx[a] = x[a];
            qy[a] = y[a];
            before[a] = Math.hypot(x[a] - shipX, y[a] - shipY);
        }
        for (int round = 0; round < ROUNDS; round++) {
            for (int a = 0; a < count; a++) {
                if (flying[a]) {
                    confine(x, y, a);
                    clear(a);
                }
            }
            for (int i = 0; i < count; i++) {
                for (int j = i + 1; j < count; j++) {
                    if (flying[i] && flying[j]) {
                        // In the later rounds a told move yields too: a holding ace pinned by the ship's keep-off.
                        apart(x, y, i, j, separation, true, false);
                    }
                }
            }
        }
        for (int a = 0; a < count; a++) {
            if (flying[a]) {
                pushed[a] += Math.hypot(x[a] - qx[a], y[a] - qy[a]);
                offX[a] = x[a] - pathX[a];
                offY[a] = y[a] - pathY[a];
            }
        }
    }

    /**
     * Step F6s2: ace {@code a}'s right of way in the avoidance: 0 holding, sliding or entering; 1 a told move; 2 a run at
     * the ship (a dive's, the overhead's from the top). The lower steps out of the higher's way.
     */
    private int way(int a) {
        return yields(a) ? 0 : diving(a) ? 2 : 1;
    }

    /** Whether ace {@code a} yields to a push: holding, sliding or entering (a told move holds its line). */
    private boolean yields(int a) {
        AceMove m = MOVES[move[a]];
        return m == AceMove.HOLD || m == AceMove.SLIDE || m == AceMove.ENTRY;
    }

    /**
     * Step F6s2: whether ace {@code a} keeps to its band and the field (no push takes it farther out than it is): holding
     * (its orbit, aiming, or on its way back onto it) or sliding, its path inside its band (a way home from below or
     * above it, or the feinter's crossing over the ship, may give way either way).
     */
    private boolean banded(int a) {
        // Step F6s3: as that doc says (the code held a way home from below to its path too, so a way home up through a
        // dive's line carried the diver up with it): once settled in its band, or its path in it.
        SquadronSpec.Station station = stations[a];
        return held(a) && (settled[a] || (pathY[a] >= station.yMin() && pathY[a] <= station.yMax()));
    }

    /** Step F6s3: whether ace {@code a} holds (its orbit, aiming, its way back onto it) or slides, not crossing over. */
    private boolean held(int a) {
        return (move[a] == AceMove.HOLD.ordinal() || move[a] == AceMove.SLIDE.ordinal()) && !crossing(a);
    }

    /** Whether ace {@code a} flies its station's orbit now (or aims from it): it keeps to its band and the field. */
    private boolean onStation(int a) {
        return move[a] == AceMove.HOLD.ordinal()
                && (aiming[a] || (legCount[a] > 0 && legTag[a * LEGS + leg[a]] == AceLeg.HOLD.ordinal()));
    }

    /**
     * Pushes aces {@code i} and {@code j} (at px, py) apart until their hulls' gap is {@code gap}. Step F6s2: in the
     * avoidance ({@code soft}), an ace that yields to one in a told move steps out of that one's way: across its
     * motion (sideways off a climb or a dive, up or down off a pass), never ahead of it.
     */
    private void apart(double[] px, double[] py, int i, int j, double gap, boolean even, boolean soft) {
        double dx = px[j] - px[i];
        double dy = py[j] - py[i];
        double sxn = dx >= 0 ? 1 : -1;
        double syn = dy >= 0 ? 1 : -1;
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double ex = ax - hullW;
        double ey = ay - hullH;
        double pushX = 0;
        double pushY = 0;
        int across = soft && way(i) != way(j) ? (way(i) > way(j) ? i : j) : -1;
        int low = across == i ? j : i;
        if (across >= 0) {
            if (ex < gap && ey < gap && (ex <= 0 || ey <= 0 || Math.hypot(ex, ey) < gap)) {
                // Across the told ace's motion, to the side the yielding one's path is on, until their hulls are the
                // gap apart along that.
                double nx = -baseTy[across];
                double ny = baseTx[across];
                if ((baseX[low] - baseX[across]) * nx + (baseY[low] - baseY[across]) * ny < 0) {
                    nx = -nx;
                    ny = -ny;
                }
                double along = (px[low] - px[across]) * nx + (py[low] - py[across]) * ny;
                double need = hullW * Math.abs(nx) + hullH * Math.abs(ny) + gap - along;
                if (need > 0) {
                    double toJ = low == j ? need : -need;
                    pushX = nx * toJ;
                    pushY = ny * toJ;
                }
            }
        } else if (ex <= 0 && ey <= 0) {
            double needX = hullW + gap - ax;
            double needY = hullH + gap - ay;
            if (needX <= needY) {
                pushX = needX * sxn;
            } else {
                pushY = needY * syn;
            }
        } else if (ex <= 0) {
            pushY = Math.max(0, hullH + gap - ay) * syn;
        } else if (ey <= 0) {
            pushX = Math.max(0, hullW + gap - ax) * sxn;
        } else {
            double d = Math.hypot(ex, ey);
            if (d < gap) {
                pushX = ex / d * (gap - d) * sxn;
                pushY = ey / d * (gap - d) * syn;
            }
        }
        if (pushX == 0 && pushY == 0) {
            return;
        }
        double wi = across >= 0 ? (low == i ? 1 : 0) : even || yields(i) ? 1 : 0;
        double wj = across >= 0 ? (low == j ? 1 : 0) : even || yields(j) ? 1 : 0;
        double shareI = wi + wj == 0 ? 0.5 : wi / (wi + wj);
        double shareJ = 1 - shareI;
        px[i] -= pushX * shareI;
        py[i] -= pushY * shareI;
        px[j] += pushX * shareJ;
        py[j] += pushY * shareJ;
        // What the field's edge or the band takes back from one goes to the other (its relative place kept); step
        // F6s2: in the avoidance, from a yielding ace to the told one only past the margin (the told move gives way
        // only to the rule itself, never to the margin).
        double ix = px[i];
        double iy = py[i];
        confine(px, py, i);
        double f = across == j ? past(px[i] - ix, py[i] - iy) : 1;
        px[j] += (px[i] - ix) * f;
        py[j] += (py[i] - iy) * f;
        double jx = px[j];
        double jy = py[j];
        confine(px, py, j);
        f = across == i ? past(px[j] - jx, py[j] - jy) : 1;
        px[i] += (px[j] - jx) * f;
        py[i] += (py[j] - jy) * f;
    }

    /** Step F6s2: the share of a take-back (dx, dy) past the avoidance's margin. */
    private static double past(double dx, double dy) {
        double d = Math.hypot(dx, dy);
        return d > MARGIN ? (d - MARGIN) / d : 0;
    }

    /**
     * The avoidance never pushes a flying ace farther off the field than its path (its hull's), nor either solver an
     * ace on its station (its orbit, or aiming from it) farther off the field or out of its band than its path is: (px,
     * py) clamped against its unpushed point (its path's for the solver, the avoidance's ahead).
     */
    private void confine(double[] px, double[] py, int a) {
        if (move[a] == AceMove.ENTRY.ordinal() || (px == x && !banded(a))) {
            // The exact solver holds only an ace on its station to the field (a told move may swing out).
            return;
        }
        // Step F6s2: the exact solver's against where the avoidance steered it (never a snap back; step F6s3: on its
        // orbit too, the avoidance keeps it in its band once settled); the avoidance's against its path ahead.
        double rx = px == x ? qx[a] : baseX[a];
        double ry = px == x ? qy[a] : baseY[a];
        px[a] = within(px[a], rx, hullW / 2, PlayField.WIDTH - hullW / 2);
        py[a] = within(py[a], ry, hullH / 2, PlayField.HEIGHT - hullH / 2);
        if (banded(a)) {
            SquadronSpec.Station station = stations[a];
            py[a] = within(py[a], ry, station.yMin(), station.yMax());
        }
    }

    /** {@code v} within [lo, hi], or no farther outside than {@code ref} is. */
    private static double within(double v, double ref, double lo, double hi) {
        return Math.clamp(v, Math.min(ref, lo), Math.max(ref, hi));
    }

    /** An ace on its station keeps its hull wholly on the field (step F6s2: or no farther off than it was steered). */
    private void inField(double[] px, double[] py, int a) {
        if (!onStation(a)) {
            return;
        }
        px[a] = within(px[a], qx[a], hullW / 2, PlayField.WIDTH - hullW / 2);
        py[a] = within(py[a], qy[a], hullH / 2, PlayField.HEIGHT - hullH / 2);
    }

    /** Ace {@code a} (at px, py) out to {@code keep} px from the ship. */
    private void keepOut(double[] px, double[] py, int a, double keep) {
        double dx = px[a] - shipX;
        double dy = py[a] - shipY;
        double d = Math.hypot(dx, dy);
        if (d >= keep) {
            return;
        }
        if (d == 0) {
            py[a] = shipY + keep;
            return;
        }
        px[a] = shipX + dx / d * keep;
        py[a] = shipY + dy / d * keep;
    }

    /** Ace {@code a} out of its keep-off from the ship (the leader out of the ring only if a push put it in). */
    private void clear(int a) {
        double keep = keepOff(a);
        if (keep <= 0) {
            keep = before[a] >= SHIP_RING ? SHIP_RING : 0;
        }
        double dx = x[a] - shipX;
        double dy = y[a] - shipY;
        if (keep <= 0 || Math.hypot(dx, dy) >= keep) {
            return;
        }
        keepOut(x, y, a, keep);
        if (!onStation(a)) {
            return;
        }
        // A holding ace the field's edge holds back slides along that edge out of the keep-off instead.
        double px = x[a];
        double py = y[a];
        inField(x, y, a);
        // Step F6s: on the side of the ship it was on (never through it).
        if (px != x[a]) {
            double along = Math.sqrt(Math.max(0, keep * keep - (x[a] - shipX) * (x[a] - shipX)));
            y[a] = edgeSide(shipY, along, prevY[a] - shipY, hullH / 2, PlayField.HEIGHT - hullH / 2);
        } else if (py != y[a]) {
            double along = Math.sqrt(Math.max(0, keep * keep - (y[a] - shipY) * (y[a] - shipY)));
            x[a] = edgeSide(shipX, along, prevX[a] - shipX, hullW / 2, PlayField.WIDTH - hullW / 2);
        }
    }

    /** {@code centre} ± {@code along} on the side of {@code toward}, the other side if that one is off the field. */
    private static double edgeSide(double centre, double along, double toward, double min, double max) {
        double first = centre + (toward >= 0 ? along : -along);
        double second = centre - (toward >= 0 ? along : -along);
        return first >= min && first <= max ? first : Math.clamp(second, min, max);
    }

    // The reads.

    /** As {@link Squadron#leadX}: ace {@code a} moving on as over its last step. */
    double leadX(int a, int ticks) {
        return x[a] + (x[a] - prevX[a]) * ticks;
    }

    double leadY(int a, int ticks) {
        return y[a] + (y[a] - prevY[a]) * ticks;
    }

    /** Step F1b: whether ace {@code a} flies a dive's run now (the feinter's dive or the overhead's dive). */
    boolean running(int a) {
        return flying[a] && !aiming[a] && legCount[a] > 0 && legTag[a * LEGS + leg[a]] == AceLeg.RUN.ordinal();
    }

    /** Step F1b: where ace {@code a}'s current line leg starts, px from the left (a dive's run's start, a pass's). */
    double legStartX(int a) {
        return legAx[a * LEGS + leg[a]];
    }

    double legStartY(int a) {
        return legAy[a * LEGS + leg[a]];
    }

    /** Step F1b: where ace {@code a}'s current line leg ends, px from the left. */
    double legEndX(int a) {
        return legBx[a * LEGS + leg[a]];
    }

    /** Step F1b: the running dive's locked spot (the ship's at its start: its run's end plus its pull-out's start). */
    double runToX(int a) {
        int k = a * LEGS + leg[a];
        double d = legLength[k];
        return d > 0 ? legBx[k] + (legBx[k] - legAx[k]) / d * pullOutStart(pullOut[a]) : legBx[k];
    }

    double runToY(int a) {
        int k = a * LEGS + leg[a];
        double d = legLength[k];
        return d > 0 ? legBy[k] + (legBy[k] - legAy[k]) / d * pullOutStart(pullOut[a]) : legBy[k];
    }

    /** Ace {@code a}'s current leg's tag (an aiming ace holds). */
    AceLeg legOf(int a) {
        if (aiming[a]) {
            return AceLeg.HOLD;
        }
        return legCount[a] == 0 ? AceLeg.WAIT : LEG_KINDS[legTag[a * LEGS + leg[a]]];
    }

    /** Whether ace {@code a}'s hull lies wholly on the play field. */
    boolean wholeOnField(int a) {
        return x[a] - hullW / 2 >= 0
                && x[a] + hullW / 2 <= PlayField.WIDTH
                && y[a] - hullH / 2 >= 0
                && y[a] + hullH / 2 <= PlayField.HEIGHT;
    }

    /** The gap between aces {@code i}'s and {@code j}'s hulls, edge to edge, px (0 when they overlap). */
    double gap(int i, int j) {
        double ex = Math.max(0, Math.abs(x[j] - x[i]) - hullW);
        double ey = Math.max(0, Math.abs(y[j] - y[i]) - hullH);
        return Math.hypot(ex, ey);
    }

    void addTo(StateHash hash) {
        hash.add(deaths).add(shipX).add(shipY);
        for (int a = 0; a < count; a++) {
            hash.add(x[a])
                    .add(y[a])
                    .add(facing[a])
                    .add(offX[a])
                    .add(offY[a])
                    .add(ovX[a])
                    .add(ovY[a])
                    .add(pathX[a])
                    .add(pathY[a])
                    .add(tanX[a])
                    .add(tanY[a])
                    .add(spd[a])
                    .add(bank[a])
                    .add(flying[a] ? 1 : 0)
                    .add(tumbling[a] ? 1 : 0)
                    .add(vx[a])
                    .add(vy[a])
                    .add(spin[a])
                    .add(move[a])
                    .add(moveTicks[a])
                    .add(stage[a])
                    .add(leg[a])
                    .add(legCount[a])
                    .add(along[a])
                    .add(legTicks[a])
                    .add(centreX[a])
                    .add(side[a])
                    .add(holdY[a])
                    .add(pullOut[a])
                    .add(aim[a] ? 1 : 0)
                    .add(aiming[a] ? 1 : 0)
                    .add(partner[a])
                    .add(pace[a])
                    .add(settled[a] ? 1 : 0);
            for (int l = 0; l < legCount[a]; l++) {
                int k = a * LEGS + l;
                hash.add(legKind[k])
                        .add(legTag[k])
                        .add(legAx[k])
                        .add(legAy[k])
                        .add(legBx[k])
                        .add(legBy[k])
                        .add(legR[k])
                        .add(legLength[k])
                        .add(legSpeed[k])
                        .add(legEx[k])
                        .add(legEy[k]);
            }
        }
    }
}
