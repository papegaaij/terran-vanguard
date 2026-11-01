package vanguard.sim;

/**
 * M10 part F, step F1a (design/enemies/bosses/honour-guard, The aces and The playbook): where a {@link Squadron}'s aces
 * fly. Each ace flies a path of a few legs (lines, arcs and waits at the leg's speed) built when a move starts: its
 * entry from beyond its edge, its station ({@link AceMove#HOLD}: the leader strafing in the top band with its nose on
 * the ship, the dancer's figure-eights of two tangent circles in the middle band, the feinter in the bottom band's side
 * lane away from the ship) and its moves (the leader's slide, the dancer's loop, roll and ribbon pass, the feinter's
 * feint dive with its pull-out, rear rake and overhead feint, the pincer of two flankers). A move ends back on its
 * station. After the paths a solver keeps the living aces' hulls {@link SquadronSpec#separation()} apart edge to edge
 * (an ace in a told move holds its line, a holding one yields), keeps the dancer and the feinter their {@link
 * SquadronSpec.Station#keepOff()} from the ship (the feinter's pull-out, {@link SquadronSpec.Station#committedKeepOff()}
 * once an ace has died) and never shoves the leader into the ship's ring; its pushes ease back when not needed. A dead
 * ace's hull tumbles away down and out. Headings are continuous (the look quantises them to 64). Every array is
 * allocated once; the squadron hashes them.
 */
public final class AceFlight {
    /** The dive's pull-out turn radius, px, and how far it turns. */
    static final double PULL_OUT_RADIUS = 50;

    static final double PULL_OUT_SWEEP = 2 * StrictMath.PI / 3;
    /** How far beyond its edge an entry starts, past the hull's half, px. */
    static final double OUTSIDE = 10;
    /** The overhead feint's run above the top edge, px past it. */
    static final double OVER_TOP = 60;
    /** The rake's climb top, px up, and its loop's radius. */
    static final double RAKE_TOP = 400;

    static final double RAKE_TURN = 60;
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
    /** The feinter waits this long at each hold point; it changes lanes only this far past the field's centre. */
    static final double HOLD_WAIT_SECONDS = 1;

    static final double LANE_HYSTERESIS = 40;
    /** The ship's no-spawn ring: a separation push never shoves an ace into it. */
    static final double SHIP_RING = 72;
    /** px/s a push's offset eases back by once nothing pushes. */
    static final double EASE = 120;
    /** A dead ace's tumble: px/s sideways (out to the nearer edge) and down, and its spin, radians/s. */
    static final double TUMBLE_SIDE = 80;

    static final double TUMBLE_DOWN = 160;
    static final double TUMBLE_SPIN = 1.5 * StrictMath.PI;
    /** The solver's rounds a step. */
    private static final int ROUNDS = 32;
    /** The most legs a path has. */
    static final int LEGS = 6;

    private static final int LINE = 0;
    private static final int ARC = 1;
    private static final int WAIT = 2;
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

    // Per ace: where it is, was, faces, its push's offset and its path's point and direction.
    final double[] x;
    final double[] y;
    final double[] prevX;
    final double[] prevY;
    final double[] facing;
    final double[] offX;
    final double[] offY;
    final double[] pathX;
    final double[] pathY;
    private final double[] tanX;
    private final double[] tanY;
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
    // Per ace and leg (ace × LEGS + leg): its kind, its tag ({@link AceLeg}), its points or arc, length and speed.
    final int[] legKind;
    final int[] legTag;
    final double[] legAx;
    final double[] legAy;
    final double[] legBx;
    final double[] legBy;
    final double[] legR;
    final double[] legLength;
    final double[] legSpeed;
    /**
     * Per ace, its station's state: the leader's strafe centre (the dancer's eight's centre), its strafe side (the
     * feinter's lane side, the dancer's side of the field), its hold height and the dancer's eight's radius.
     */
    final double[] centreX;

    final int[] side;
    final double[] holdY;
    /** Per ace: its own pull-out for this dive (the feinter's keep-off when it began). */
    final double[] pullOut;
    /** The aces that have died (the feinter commits its pull-out from the first). */
    int deaths;
    /** Where the ship is this step. */
    private double shipX;

    private double shipY;
    /** The solver's scratch: the candidates' distance to the ship before it pushed. */
    private final double[] before;

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
        pincerLane = (hullH + separation) / 2 + 3;
        x = new double[count];
        y = new double[count];
        prevX = new double[count];
        prevY = new double[count];
        facing = new double[count];
        offX = new double[count];
        offY = new double[count];
        pathX = new double[count];
        pathY = new double[count];
        tanX = new double[count];
        tanY = new double[count];
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
        centreX = new double[count];
        side = new int[count];
        holdY = new double[count];
        pullOut = new double[count];
        before = new double[count];
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
            offX[a] = offY[a] = 0;
            tanX[a] = 0;
            tanY[a] = -1;
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
        offX[a] = offY[a] = 0;
        legCount[a] = 0;
    }

    /** Ace {@code a} crosses its edge: it flies in at its dash speed to its station's point, then holds. */
    void enter(int a) {
        flying[a] = true;
        begin(a, AceMove.ENTRY);
        line(a, AceLeg.ENTRY, entryX(a), holdY[a], stations[a].dashSpeed());
    }

    /** Ace {@code a} died: its hull tumbles away down and out to the nearer side edge, spinning. */
    void tumble(int a) {
        flying[a] = false;
        tumbling[a] = true;
        double out = x[a] < PlayField.WIDTH / 2.0 ? -1 : 1;
        vx[a] = out * TUMBLE_SIDE;
        vy[a] = -TUMBLE_DOWN;
        spin[a] = out * TUMBLE_SPIN;
        begin(a, AceMove.TUMBLE);
        legCount[a] = 0;
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
        SquadronSpec.Station station = stations[a];
        switch (m) {
            case HOLD -> {
                // Its station's path goes on from its point, the push's offset kept.
                move[a] = AceMove.HOLD.ordinal();
                moveTicks[a] = 0;
                stage[a] = 0;
                holdLeg(a, actions);
            }
            case SLIDE -> {
                begin(a, m);
                centreX[a] = PlayField.WIDTH - centreX[a];
                line(a, AceLeg.PASS, centreX[a], holdY[a], station.dashSpeed());
            }
            case LOOP -> {
                begin(a, m);
                double r = station.speed() * station.loopSeconds() / (2 * StrictMath.PI);
                double band = (station.yMin() + station.yMax()) / 2;
                double nx = -tanY[a];
                double ny = tanX[a];
                // The loop turns toward its band's middle (the left normal's centre or the right's).
                double turn = Math.abs(y[a] + r * ny - band) <= Math.abs(y[a] - r * ny - band) ? 1 : -1;
                arc(
                        a,
                        AceLeg.TURN,
                        x[a] + turn * r * nx,
                        y[a] + turn * r * ny,
                        r,
                        turn * 2 * StrictMath.PI,
                        station.speed());
            }
            case ROLL -> {
                begin(a, m);
                int by = sx < PlayField.WIDTH / 2.0 ? 1 : -1;
                double mx =
                        Math.clamp(sx + by * (station.keepOff() + ROLL_WIDER), hullW / 2, PlayField.WIDTH - hullW / 2);
                line(a, AceLeg.PASS, mx, sy + ROLL_ABOVE, station.dashSpeed());
                line(a, AceLeg.RETURN, centreX[a], holdY[a], station.dashSpeed());
            }
            case RIBBON -> {
                begin(a, m);
                double ry = RIBBON_Y_MIN + (RIBBON_Y_MAX - RIBBON_Y_MIN) * actions.draw();
                double from = x[a] < PlayField.WIDTH / 2.0 ? RIBBON_INSET : PlayField.WIDTH - RIBBON_INSET;
                line(a, AceLeg.APPROACH, from, ry, station.dashSpeed());
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
                if (Math.abs(x[a] - lane) > 1) {
                    line(a, AceLeg.APPROACH, lane, Math.max(y[a], station.yMin()), station.climbSpeed());
                }
                line(a, AceLeg.CLIMB, lane, RAKE_TOP, station.climbSpeed());
                arc(
                        a,
                        AceLeg.TURN,
                        lane + inward * RAKE_TURN,
                        RAKE_TOP,
                        RAKE_TURN,
                        inward > 0 ? -StrictMath.PI : StrictMath.PI,
                        station.climbSpeed());
                line(a, AceLeg.RETURN, lane, holdY[a], station.climbSpeed());
            }
            case OVERHEAD -> {
                begin(a, m);
                double top = PlayField.HEIGHT + hullH / 2 + OVER_TOP;
                line(a, AceLeg.CLIMB, laneX(a), top, station.climbSpeed());
                line(a, AceLeg.APPROACH, Math.clamp(sx, hullW, PlayField.WIDTH - hullW), top, station.dashSpeed());
            }
            case PINCER, ENTRY, TUMBLE -> throw new IllegalArgumentException(m + " is not an ace's own move");
        }
    }

    /**
     * The pincer: {@code upper} and {@code lower} fly to opposite side edges (each the side it is nearer, the lower
     * the other), arriving together, then cross at their dash speeds in two lanes one separation apart, centred {@link
     * #PINCER_ABOVE} above the ship (higher when the lower one's keep-off asks it), then go back to their stations.
     */
    void startPincer(int upper, int lower, double sx, double sy) {
        shipX = sx;
        shipY = sy;
        if (!flying[upper] || !flying[lower]) {
            return;
        }
        double low = Math.max(sy + PINCER_ABOVE - pincerLane, sy + keepOff(lower) + 5);
        double high = low + 2 * pincerLane;
        double top = PlayField.HEIGHT - hullH / 2;
        if (high > top) {
            low -= high - top;
            high = top;
        }
        double upperFrom = x[upper] < PlayField.WIDTH / 2.0 ? PINCER_INSET : PlayField.WIDTH - PINCER_INSET;
        double lowerFrom = PlayField.WIDTH - upperFrom;
        double upperRun = Math.hypot(upperFrom - x[upper], high - y[upper]);
        double lowerRun = Math.hypot(lowerFrom - x[lower], low - y[lower]);
        double seconds = Math.max(upperRun / stations[upper].dashSpeed(), lowerRun / stations[lower].dashSpeed());
        begin(upper, AceMove.PINCER);
        begin(lower, AceMove.PINCER);
        line(upper, AceLeg.APPROACH, upperFrom, high, Math.max(1, upperRun / Math.max(seconds, SimStep.SECONDS)));
        line(lower, AceLeg.APPROACH, lowerFrom, low, Math.max(1, lowerRun / Math.max(seconds, SimStep.SECONDS)));
        line(upper, AceLeg.PASS, lowerFrom, high, stations[upper].dashSpeed());
        line(lower, AceLeg.PASS, upperFrom, low, stations[lower].dashSpeed());
    }

    /** A new move for ace {@code a}: a fresh path from where it is (its push's offset folded in). */
    private void begin(int a, AceMove m) {
        move[a] = m.ordinal();
        moveTicks[a] = 0;
        stage[a] = 0;
        restart(a);
    }

    /** A fresh path from where ace {@code a} is. */
    private void restart(int a) {
        pathX[a] = x[a];
        pathY[a] = y[a];
        offX[a] = offY[a] = 0;
        leg[a] = 0;
        legCount[a] = 0;
        along[a] = 0;
        legTicks[a] = 0;
    }

    /** The dive from where ace {@code a} is at the ship's spot now: its run, its pull-out, its climb home. */
    private void dive(int a) {
        SquadronSpec.Station station = stations[a];
        pullOut[a] = keepOff(a);
        double dx = shipX - x[a];
        double dy = shipY - y[a];
        double d = Math.hypot(dx, dy);
        double ux = d > 0 ? dx / d : 0;
        double uy = d > 0 ? dy / d : -1;
        double start = pullOutStart(pullOut[a]);
        double px = x[a];
        double py = y[a];
        if (d > start + 1) {
            px = shipX - ux * start;
            py = shipY - uy * start;
            line(a, AceLeg.RUN, px, py, station.dashSpeed());
        }
        pullOutFrom(a, px, py, ux, uy);
    }

    /** The pull-out from (px, py) heading (ux, uy): an arc turning toward the field's middle, then home. */
    private void pullOutFrom(int a, double px, double py, double ux, double uy) {
        SquadronSpec.Station station = stations[a];
        double nx = -uy;
        double ny = ux;
        double midX = PlayField.WIDTH / 2.0;
        double midY = PlayField.HEIGHT / 2.0;
        double left = Math.hypot(px + PULL_OUT_RADIUS * nx - midX, py + PULL_OUT_RADIUS * ny - midY);
        double right = Math.hypot(px - PULL_OUT_RADIUS * nx - midX, py - PULL_OUT_RADIUS * ny - midY);
        double turn = left <= right ? 1 : -1;
        arc(
                a,
                AceLeg.PULL_OUT,
                px + turn * PULL_OUT_RADIUS * nx,
                py + turn * PULL_OUT_RADIUS * ny,
                PULL_OUT_RADIUS,
                turn * PULL_OUT_SWEEP,
                station.dashSpeed());
        line(a, AceLeg.RETURN, laneX(a), holdY[a], station.climbSpeed());
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

    /** The feinter's side lane's centre now, px from the left. */
    private double laneX(int a) {
        double inset = stations[a].x();
        return side[a] > 0 ? PlayField.WIDTH - inset : inset;
    }

    /**
     * Ace {@code a}'s next leg on its station (the path of a hold, rebuilt as each ends): from its path's point, its
     * push's offset kept (easing back), so the station's path stays in its band.
     */
    private void holdLeg(int a, BossActions actions) {
        leg[a] = 0;
        legCount[a] = 0;
        along[a] = 0;
        legTicks[a] = 0;
        SquadronSpec.Station station = stations[a];
        switch (roles[a]) {
            case SNIPER -> {
                double span = station.spanMin() + (station.spanMax() - station.spanMin()) * actions.draw();
                side[a] = -side[a];
                line(a, AceLeg.HOLD, centreX[a] + side[a] * span / 2, holdY[a], station.speed());
            }
            case DANCER -> {
                double span = station.spanMin() + (station.spanMax() - station.spanMin()) * actions.draw();
                double r = span / 4;
                if (Math.hypot(pathX[a] - centreX[a], pathY[a] - holdY[a]) > 1) {
                    line(a, AceLeg.RETURN, centreX[a], holdY[a], station.speed());
                }
                arc(a, AceLeg.HOLD, centreX[a] - r, holdY[a], r, 2 * StrictMath.PI, station.speed());
                arc(a, AceLeg.HOLD, centreX[a] + r, holdY[a], r, -2 * StrictMath.PI, station.speed());
            }
            case FEINTER -> {
                int was = side[a];
                double centre = PlayField.WIDTH / 2.0;
                if (shipX > centre + LANE_HYSTERESIS) {
                    side[a] = -1;
                } else if (shipX < centre - LANE_HYSTERESIS) {
                    side[a] = 1;
                }
                double lane = laneX(a);
                holdY[a] = station.yMin() + (station.yMax() - station.yMin()) * actions.draw();
                if (side[a] != was || Math.abs(pathX[a] - lane) > hullW) {
                    // Over the ship to the other lane, outside its keep-off.
                    double over = Math.max(Math.max(station.yMax(), shipY + keepOff(a) + hullH), pathY[a]);
                    line(a, AceLeg.APPROACH, pathX[a], over, station.speed());
                    line(a, AceLeg.APPROACH, lane, over, station.speed());
                }
                line(a, AceLeg.HOLD, lane, holdY[a], station.speed());
                waitLeg(a, HOLD_WAIT_SECONDS);
            }
        }
    }

    /** The dancer's eight's centre goes to the side away from the leader at (lx, ...) (NaN: it stays). */
    void awayFrom(int a, double leaderX) {
        if (roles[a] != SquadronSpec.Role.DANCER || Double.isNaN(leaderX)) {
            return;
        }
        side[a] = leaderX <= PlayField.WIDTH / 2.0 ? 1 : -1;
        centreX[a] = PlayField.WIDTH / 2.0 + side[a] * stations[a].x();
    }

    private void line(int a, AceLeg tag, double toX, double toY, double speed) {
        int k = a * LEGS + legCount[a];
        double fromX = legCount[a] == 0 ? pathX[a] : endX(k - 1);
        double fromY = legCount[a] == 0 ? pathY[a] : endY(k - 1);
        legKind[k] = LINE;
        legTag[k] = tag.ordinal();
        legAx[k] = fromX;
        legAy[k] = fromY;
        legBx[k] = toX;
        legBy[k] = toY;
        legR[k] = 0;
        legLength[k] = Math.hypot(toX - fromX, toY - fromY);
        legSpeed[k] = speed;
        legCount[a]++;
    }

    /** An arc round (cx, cy) of {@code r} from the path's end, turning {@code sweep} radians (positive: anticlockwise). */
    private void arc(int a, AceLeg tag, double cx, double cy, double r, double sweep, double speed) {
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
        legCount[a]++;
    }

    private void waitLeg(int a, double seconds) {
        int k = a * LEGS + legCount[a];
        double atX = legCount[a] == 0 ? pathX[a] : endX(k - 1);
        double atY = legCount[a] == 0 ? pathY[a] : endY(k - 1);
        legKind[k] = WAIT;
        legTag[k] = AceLeg.WAIT.ordinal();
        legAx[k] = legBx[k] = atX;
        legAy[k] = legBy[k] = atY;
        legR[k] = 0;
        legLength[k] = seconds;
        legSpeed[k] = 1;
        legCount[a]++;
    }

    private double endX(int k) {
        return legKind[k] == ARC ? legAx[k] + legR[k] * Trig.cos(legBx[k] + legBy[k]) : legBx[k];
    }

    private double endY(int k) {
        return legKind[k] == ARC ? legAy[k] + legR[k] * Trig.sin(legBx[k] + legBy[k]) : legBy[k];
    }

    /**
     * One step: the flying aces move along their paths (a hold's next leg built as one ends, a move back to its
     * station as it ends; a dive pulls out early if the ship came within its pull-out start), the tumbling hulls fall,
     * then the solver keeps the hulls apart and the keep-offs.
     */
    void step(double sx, double sy, BossActions actions) {
        shipX = sx;
        shipY = sy;
        for (int a = 0; a < count; a++) {
            if (tumbling[a]) {
                fall(a);
            } else if (flying[a]) {
                moveTicks[a]++;
                fly(a, actions);
            }
        }
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
        x[a] += vx[a] * SimStep.SECONDS;
        y[a] += vy[a] * SimStep.SECONDS;
        facing[a] = Math.IEEEremainder(facing[a] + spin[a] * SimStep.SECONDS, 2 * StrictMath.PI);
        if (x[a] + hullW / 2 < 0 || x[a] - hullW / 2 > PlayField.WIDTH || y[a] + hullH / 2 < 0) {
            tumbling[a] = false;
        }
    }

    private void fly(int a, BossActions actions) {
        if (legCount[a] == 0) {
            nextPath(a, actions);
        }
        AceMove m = MOVES[move[a]];
        if ((m == AceMove.DIVE || (m == AceMove.OVERHEAD && stage[a] == 1))
                && legTag[a * LEGS + leg[a]] == AceLeg.RUN.ordinal()
                && Math.hypot(shipX - x[a], shipY - y[a]) <= pullOutStart(pullOut[a])) {
            // The ship came within the pull-out's start: it pulls out now.
            restart(a);
            pullOutFrom(a, x[a], y[a], tanX[a], tanY[a]);
        }
        int was = leg[a];
        double seconds = SimStep.SECONDS;
        while (seconds > 0 && leg[a] < legCount[a]) {
            int k = a * LEGS + leg[a];
            double left = legLength[k] - along[a];
            double reach = legSpeed[k] * seconds;
            if (reach < left) {
                along[a] += reach;
                seconds = 0;
            } else {
                seconds -= left / legSpeed[k];
                along[a] = 0;
                leg[a]++;
                if (leg[a] == legCount[a]) {
                    along[a] = legLength[k];
                    leg[a]--;
                    eval(a);
                    nextPath(a, actions);
                    legTicks[a] = 0;
                    return;
                }
            }
        }
        eval(a);
        legTicks[a] = leg[a] == was ? legTicks[a] + 1 : 0;
    }

    /** Ace {@code a}'s path ended: a hold's next leg, the overhead's dive, or back to its station. */
    private void nextPath(int a, BossActions actions) {
        AceMove m = MOVES[move[a]];
        if (m == AceMove.OVERHEAD && stage[a] == 0) {
            x[a] = pathX[a] + offX[a];
            y[a] = pathY[a] + offY[a];
            int ticks = moveTicks[a];
            restart(a);
            stage[a] = 1;
            moveTicks[a] = ticks;
            dive(a);
            return;
        }
        if (m != AceMove.HOLD) {
            move[a] = AceMove.HOLD.ordinal();
            moveTicks[a] = 0;
            stage[a] = 0;
        }
        holdLeg(a, actions);
    }

    /** Ace {@code a}'s path point and direction at its leg and distance. */
    private void eval(int a) {
        int k = a * LEGS + leg[a];
        double t = legLength[k] > 0 ? along[a] / legLength[k] : 1;
        switch (legKind[k]) {
            case ARC -> {
                double angle = legBx[k] + legBy[k] * t;
                double c = Trig.cos(angle);
                double s = Trig.sin(angle);
                pathX[a] = legAx[k] + legR[k] * c;
                pathY[a] = legAy[k] + legR[k] * s;
                double turn = legBy[k] >= 0 ? 1 : -1;
                tanX[a] = -s * turn;
                tanY[a] = c * turn;
            }
            case LINE -> {
                pathX[a] = legAx[k] + (legBx[k] - legAx[k]) * t;
                pathY[a] = legAy[k] + (legBy[k] - legAy[k]) * t;
                if (legLength[k] > 0) {
                    tanX[a] = (legBx[k] - legAx[k]) / legLength[k];
                    tanY[a] = (legBy[k] - legAy[k]) / legLength[k];
                }
            }
            default -> {
                pathX[a] = legAx[k];
                pathY[a] = legAy[k];
            }
        }
    }

    /** The leader's nose and a waiting ace's are on the ship; any other's along its path. */
    private void face(int a) {
        int k = a * LEGS + leg[a];
        boolean onShip = (roles[a] == SquadronSpec.Role.SNIPER && move[a] != AceMove.ENTRY.ordinal())
                || (legCount[a] > 0 && legKind[k] == WAIT);
        facing[a] = onShip ? Enemy.heading(shipX - x[a], shipY - y[a]) : Enemy.heading(tanX[a], tanY[a]);
    }

    /**
     * The flying aces' places: each its path's point plus its eased offset; then rounds of pushes: an ace out of its
     * keep-off from the ship (the leader only out of the ship's ring a push put it in), a holding ace back inside the
     * field, then two hulls nearer than the separation apart (the one in a told move holds, a holding or entering one
     * yields; what the field's edge takes back from one goes to the other), the separation last.
     */
    private void solve() {
        double ease = EASE * SimStep.SECONDS;
        for (int a = 0; a < count; a++) {
            if (!flying[a]) {
                continue;
            }
            double off = Math.hypot(offX[a], offY[a]);
            if (off <= ease) {
                offX[a] = offY[a] = 0;
            } else {
                offX[a] -= offX[a] / off * ease;
                offY[a] -= offY[a] / off * ease;
            }
            x[a] = pathX[a] + offX[a];
            y[a] = pathY[a] + offY[a];
            before[a] = Math.hypot(x[a] - shipX, y[a] - shipY);
        }
        for (int round = 0; round < ROUNDS; round++) {
            for (int a = 0; a < count; a++) {
                if (flying[a]) {
                    clear(a);
                    inField(a);
                }
            }
            for (int i = 0; i < count; i++) {
                for (int j = i + 1; j < count; j++) {
                    if (flying[i] && flying[j]) {
                        // In the later rounds a told move yields too: a holding ace pinned by the ship's keep-off.
                        apart(i, j, round >= ROUNDS / 2);
                    }
                }
            }
        }
        for (int a = 0; a < count; a++) {
            if (flying[a]) {
                offX[a] = x[a] - pathX[a];
                offY[a] = y[a] - pathY[a];
            }
        }
    }

    /** Whether ace {@code a} yields to a push: holding, sliding or entering (a told move holds its line). */
    private boolean yields(int a) {
        AceMove m = MOVES[move[a]];
        return m == AceMove.HOLD || m == AceMove.SLIDE || m == AceMove.ENTRY;
    }

    /** Pushes aces {@code i} and {@code j} apart until their hulls' gap is the separation. */
    private void apart(int i, int j, boolean even) {
        double dx = x[j] - x[i];
        double dy = y[j] - y[i];
        double sxn = dx >= 0 ? 1 : -1;
        double syn = dy >= 0 ? 1 : -1;
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double ex = ax - hullW;
        double ey = ay - hullH;
        double px = 0;
        double py = 0;
        if (ex <= 0 && ey <= 0) {
            double needX = hullW + separation - ax;
            double needY = hullH + separation - ay;
            if (needX <= needY) {
                px = needX * sxn;
            } else {
                py = needY * syn;
            }
        } else if (ex <= 0) {
            py = Math.max(0, hullH + separation - ay) * syn;
        } else if (ey <= 0) {
            px = Math.max(0, hullW + separation - ax) * sxn;
        } else {
            double d = Math.hypot(ex, ey);
            if (d < separation) {
                px = ex / d * (separation - d) * sxn;
                py = ey / d * (separation - d) * syn;
            }
        }
        if (px == 0 && py == 0) {
            return;
        }
        double wi = even || yields(i) ? 1 : 0;
        double wj = even || yields(j) ? 1 : 0;
        double shareI = wi + wj == 0 ? 0.5 : wi / (wi + wj);
        double shareJ = 1 - shareI;
        x[i] -= px * shareI;
        y[i] -= py * shareI;
        x[j] += px * shareJ;
        y[j] += py * shareJ;
        // What the field's edge takes back from one goes to the other (its relative place kept).
        double ix = x[i];
        double iy = y[i];
        inField(i);
        x[j] += x[i] - ix;
        y[j] += y[i] - iy;
        double jx = x[j];
        double jy = y[j];
        inField(j);
        x[i] += x[j] - jx;
        y[i] += y[j] - jy;
    }

    /** A holding ace's hull stays wholly on the field. */
    private void inField(int a) {
        if (move[a] != AceMove.HOLD.ordinal()) {
            return;
        }
        x[a] = Math.clamp(x[a], hullW / 2, PlayField.WIDTH - hullW / 2);
        y[a] = Math.clamp(y[a], hullH / 2, PlayField.HEIGHT - hullH / 2);
    }

    /** Ace {@code a} out of its keep-off from the ship (the leader out of the ring only if a push put it in). */
    private void clear(int a) {
        double keep = keepOff(a);
        if (keep <= 0) {
            keep = before[a] >= SHIP_RING ? SHIP_RING : 0;
        }
        double dx = x[a] - shipX;
        double dy = y[a] - shipY;
        double d = Math.hypot(dx, dy);
        if (keep <= 0 || d >= keep) {
            return;
        }
        if (d == 0) {
            y[a] = shipY + keep;
            return;
        }
        x[a] = shipX + dx / d * keep;
        y[a] = shipY + dy / d * keep;
        if (move[a] != AceMove.HOLD.ordinal()) {
            return;
        }
        // A holding ace the field's edge holds back slides along that edge out of the keep-off instead.
        double px = x[a];
        double py = y[a];
        inField(a);
        if (px != x[a]) {
            double along = Math.sqrt(Math.max(0, keep * keep - (x[a] - shipX) * (x[a] - shipX)));
            y[a] = edgeSide(shipY, along, dy, hullH / 2, PlayField.HEIGHT - hullH / 2);
        } else if (py != y[a]) {
            double along = Math.sqrt(Math.max(0, keep * keep - (y[a] - shipY) * (y[a] - shipY)));
            x[a] = edgeSide(shipX, along, dx, hullW / 2, PlayField.WIDTH - hullW / 2);
        }
    }

    /** {@code centre} ± {@code along} on the side of {@code toward}, the other side if that one is off the field. */
    private static double edgeSide(double centre, double along, double toward, double min, double max) {
        double first = centre + (toward >= 0 ? along : -along);
        double second = centre - (toward >= 0 ? along : -along);
        return first >= min && first <= max ? first : Math.clamp(second, min, max);
    }

    /** As {@link Squadron#leadX}: ace {@code a} moving on as over its last step. */
    double leadX(int a, int ticks) {
        return x[a] + (x[a] - prevX[a]) * ticks;
    }

    double leadY(int a, int ticks) {
        return y[a] + (y[a] - prevY[a]) * ticks;
    }

    /** Ace {@code a}'s current leg's tag. */
    AceLeg legOf(int a) {
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
                    .add(pathX[a])
                    .add(pathY[a])
                    .add(tanX[a])
                    .add(tanY[a])
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
                    .add(pullOut[a]);
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
                        .add(legSpeed[k]);
            }
        }
    }
}
