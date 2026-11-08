package vanguard.sim;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, The rear sweep; m11f-gaps.md §1 #2, §4, §5): a boss's rear
 * sweep ({@link RearSweepSpec}), kept by its {@link SetPiece} behind its one accessor ({@link SetPiece#rearSweep()}),
 * made only for a boss with one (every other boss hashes as before). Its playbook's {@link
 * PhasePlaybook.Kind#REAR_SWEEP} move tells it ({@link #tell}: {@link SimEvents.Type#SWEEP_TELL} at the far flank, value
 * that flank's side, +1 right; Rook's {@link CueTrigger#REAR_SWEEP} bark, its subject {@code 1} and {@code 2} in turn) and
 * starts it ({@link #start}); it then runs on the boss's steps, moving the piece's centre (its parts follow their
 * offsets):
 *
 * <ol>
 *   <li>the drop: straight up at the spec's speed until its sprite is wholly above the top edge ({@link
 *       SimEvents.Type#SWEEP_OUT} as it starts), firing the climb's attack as it starts and as its source leaves the top
 *       edge (step F1c: the cover through its time out of sight);
 *   <li>out of sight for the spec's seconds; as it goes the point it will rise from is marked on the bottom edge ({@link
 *       SimEvents.Type#SWEEP_MARK}, value the side): on the half of the field the ship is not on, along that side (its
 *       body box at the field's edge), its centre at least {@code offShip} px off the ship's x;
 *   <li>the climb ({@link SimEvents.Type#SWEEP_CLIMB} as it rises at the mark): from just below the bottom edge up at the
 *       spec's speed to its station's height, firing the spec's attack from its playbook's source (else its centre) at
 *       the ship every {@code fireEverySeconds} while that point is on the field (step F5: the first as it crosses the bottom
 *       edge). The ship's centre is kept {@code
 *       clear} px off its body box (the box grown by it): the climb never brings it nearer (it holds its height and
 *       slides out sideways at its speed until the way up is clear); a ship coming at it from the side pushes it away
 *       sideways as far as the ship came on (beyond the field's edge if the ship chases it there: it never crosses the
 *       ship's path); one coming down onto it from above it slides out from under at its speed;
 *   <li>the return: sideways at its speed into its sway's reach at its station; then its sway resumes from there
 *       ({@link SimEvents.Type#SWEEP_DONE}).
 * </ol>
 *
 * <p>Its parts take hits wherever they are on the screen (the rear and side mounts' moment); its contact is the body's,
 * as ever. A phase ending in the middle lets it finish (its playbook draws again once it is back). Its state is hashed
 * with its piece's; the boss checkpoint and a retry start it afresh. Allocates nothing once made.
 */
public final class RearSweep {
    /** Rook's barks' subjects, in turn ({@link CueTrigger#REAR_SWEEP}). */
    public static final String FIRST_BARK = "1";

    public static final String SECOND_BARK = "2";

    /** What it does now. */
    public enum Mode {
        /** Not sweeping: at its station. */
        IDLE,
        /** Told (the playbook's tell): still at its station. */
        TELL,
        /** Dropping out of the top edge. */
        DROP,
        /** Out of sight above the top edge, its rising point marked. */
        AWAY,
        /** Climbing from the bottom edge. */
        CLIMB,
        /** Gliding sideways back into its sway's reach. */
        RETURN
    }

    private static final Mode[] MODES = Mode.values();

    private final SetPiece piece;
    private final BossSpec boss;
    private final RearSweepSpec spec;
    /** Its body box's half width and height (the clear rule). */
    private final double halfWidth;

    private final double halfHeight;
    /** Its sprite's half height (out of sight above the top edge, rising from below the bottom edge). */
    private final double reach;

    private final int outTicks;
    private final int fireTicks;
    private final double step;

    private int mode;
    private int modeTicks;
    /** The tell's flaring flank (+1 right, -1 left: away from the ship). */
    private int flank;
    /** The side it rises on (+1 right, -1 left), set at the mark. */
    private int side;
    /** The rising point's x, set at the mark. */
    private double markX;
    /** Steps to the climb's next shot. */
    private int nextShot;
    /** The sweeps told in this attempt (Rook's barks in turn). */
    private int sweeps;

    RearSweep(SetPiece piece, BossSpec boss, RearSweepSpec spec, SetPieceSpec pieceSpec) {
        this.piece = piece;
        this.boss = boss;
        this.spec = spec;
        halfWidth = pieceSpec.body().width() / 2;
        halfHeight = pieceSpec.body().height() / 2;
        reach = Math.max(pieceSpec.size().height(), pieceSpec.body().height()) / 2;
        outTicks = SimStep.ticks(spec.outSeconds());
        fireTicks = spec.fireAttack() < 0 ? 0 : Math.max(1, SimStep.ticks(spec.fireEverySeconds()));
        step = spec.speed() * SimStep.SECONDS;
        reset();
    }

    /** Back to the level start (and the boss checkpoint): at its station, nothing told. */
    void reset() {
        mode = Mode.IDLE.ordinal();
        modeTicks = 0;
        flank = 0;
        side = 0;
        markX = 0;
        nextShot = 0;
        sweeps = 0;
    }

    /** The playbook drew it: the far flank flares (away from the ship at {@code shipX}), Rook calls it. */
    void tell(double shipX, BossActions actions) {
        mode = Mode.TELL.ordinal();
        modeTicks = 0;
        flank = shipX < piece.x() ? 1 : -1;
        sweeps++;
        actions.ring(SimEvents.Type.SWEEP_TELL, piece.x() + flank * halfWidth, piece.y(), flank);
        actions.sweepCue(sweeps % 2 == 1 ? FIRST_BARK : SECOND_BARK);
    }

    /** Its phase ended during its tell: it is called off (at its station still). */
    void untell() {
        if (mode == Mode.TELL.ordinal()) {
            mode = Mode.IDLE.ordinal();
            modeTicks = 0;
        }
    }

    /** The tell is over: it drops out of the top edge. */
    void start(BossActions actions) {
        mode = Mode.DROP.ordinal();
        modeTicks = 0;
        actions.ring(SimEvents.Type.SWEEP_OUT, piece.x(), piece.y(), flank);
    }

    /**
     * One of the boss's steps with the ship at (shipX, shipY): the drop, the time away, the climb (firing from part
     * {@code source}, -1 for its centre, only while {@code firing}) and the return.
     */
    void step(double shipX, double shipY, int source, BossActions actions, boolean firing) {
        if (!flying()) {
            return;
        }
        SetPieceMotion motion = piece.motion;
        modeTicks++;
        switch (MODES[mode]) {
            case DROP -> {
                motion.y += step;
                // M11 part F, step F1c: the drop fires the climb's pair as it starts (the told sweep's, as a told
                // line's
                // as it is drawn) and as its source leaves the top edge: that pair still closes on the ship through the
                // second out of sight, so the fight never goes quiet as the hull leaves (the spec's no-quiet rule).
                double fy = source < 0 ? motion.y : piece.partY(source);
                if (modeTicks == 1 || (fy < PlayField.HEIGHT && fy + step >= PlayField.HEIGHT)) {
                    nextShot = 1;
                    fire(motion, source, actions, firing);
                }
                if (motion.y - reach >= PlayField.HEIGHT) {
                    mark(shipX, actions);
                }
            }
            case AWAY -> {
                if (modeTicks >= outTicks) {
                    mode = Mode.CLIMB.ordinal();
                    modeTicks = 0;
                    nextShot = fireTicks;
                    motion.placeAt(markX, -reach);
                    actions.ring(SimEvents.Type.SWEEP_CLIMB, markX, 0, side);
                }
            }
            case CLIMB -> climb(motion, shipX, shipY, source, actions, firing);
            case RETURN -> {
                double to = Math.clamp(motion.x, lowSway(motion), highSway(motion));
                double dx = to - motion.x;
                if (Math.abs(dx) <= step) {
                    motion.x = to;
                    mode = Mode.IDLE.ordinal();
                    modeTicks = 0;
                    motion.resumeSway();
                    actions.ring(SimEvents.Type.SWEEP_DONE, motion.x, motion.y, side);
                } else {
                    motion.x += Math.signum(dx) * step;
                }
            }
            case IDLE, TELL -> {}
        }
    }

    /** Out of sight: the rising point on the bottom edge, on the half of the field the ship is not on. */
    private void mark(double shipX, BossActions actions) {
        mode = Mode.AWAY.ordinal();
        modeTicks = 0;
        side = shipX < PlayField.WIDTH / 2.0 ? 1 : -1;
        markX = side > 0
                ? Math.max(PlayField.WIDTH - halfWidth, shipX + spec.offShip())
                : Math.min(halfWidth, shipX - spec.offShip());
        actions.ring(SimEvents.Type.SWEEP_MARK, markX, 0, side);
    }

    /** One step of the climb (the clear rule, the fire). */
    private void climb(
            SetPieceMotion motion, double shipX, double shipY, int source, BossActions actions, boolean firing) {
        double below = source < 0 ? motion.y : piece.partY(source);
        double up = Math.min(motion.anchorY, motion.y + step);
        double away = motion.x == shipX ? side : Math.signum(motion.x - shipX);
        double keep = halfWidth + spec.clear();
        if (!clearAt(motion.x, motion.y, shipX, shipY)) {
            // The ship came at it: from the side it is pushed away as far as the ship came on; from over it, it slides
            // out at its speed.
            double dx = Math.abs(shipX - motion.x);
            motion.x = dx >= halfWidth ? shipX + away * keep : motion.x + away * Math.min(step, keep - dx);
        }
        if (clearAt(motion.x, up, shipX, shipY)) {
            motion.y = up;
        } else if (clearAt(motion.x, motion.y, shipX, shipY)) {
            // The climb would bring it onto the ship: it holds its height and slides out sideways at its speed.
            motion.x += away * Math.min(step, keep - Math.abs(shipX - motion.x));
        }
        double fy = source < 0 ? motion.y : piece.partY(source);
        if (below <= 0 && fy > 0) {
            // M11 part F, step F5 (round 93 L.3: "make sure it enters the screen firing real rounds at you"): its first
            // volley as its source crosses the bottom edge, then every fireEverySeconds.
            nextShot = 1;
        }
        fire(motion, source, actions, firing);
        if (motion.y >= motion.anchorY) {
            mode = Mode.RETURN.ordinal();
            modeTicks = 0;
        }
    }

    /**
     * The drop's and the climb's fire: the spec's attack from part {@code source} (-1: its centre) at the ship every
     * {@code fireEverySeconds}, only while {@code firing} and that point is on the field.
     */
    private void fire(SetPieceMotion motion, int source, BossActions actions, boolean firing) {
        if (fireTicks > 0 && --nextShot <= 0) {
            nextShot = fireTicks;
            double fx = source < 0 ? motion.x : piece.partX(source);
            double fy = source < 0 ? motion.y : piece.partY(source);
            if (firing && fy > 0 && fy < PlayField.HEIGHT && fx > 0 && fx < PlayField.WIDTH) {
                actions.aimed(fx, fy, boss.attacks().get(spec.fireAttack()).gun());
            }
        }
    }

    /** Whether the ship at (shipX, shipY) is clear of its body box at (x, y) grown by the spec's clearance. */
    boolean clearAt(double x, double y, double shipX, double shipY) {
        return Math.abs(shipX - x) - halfWidth >= spec.clear() - 1e-9
                || Math.abs(shipY - y) - halfHeight >= spec.clear() - 1e-9;
    }

    private double lowSway(SetPieceMotion motion) {
        return motion.anchorX - Math.abs(boss.sineAmplitude());
    }

    private double highSway(SetPieceMotion motion) {
        return motion.anchorX + Math.abs(boss.sineAmplitude());
    }

    // --- the views -----------------------------------------------------------------------------------

    public RearSweepSpec spec() {
        return spec;
    }

    /** What it does now. */
    public Mode mode() {
        return MODES[mode];
    }

    /** Whether it moves the piece now (from its drop to its return's end). */
    public boolean flying() {
        return mode >= Mode.DROP.ordinal();
    }

    /** Whether it is told or flying (Rook flies Trail). */
    public boolean busy() {
        return mode != Mode.IDLE.ordinal();
    }

    /** Whether it is out of sight now (its rising point marked on the bottom edge). */
    public boolean away() {
        return mode == Mode.AWAY.ordinal();
    }

    /** Whether it is climbing now. */
    public boolean climbing() {
        return mode == Mode.CLIMB.ordinal();
    }

    /** The tell's flaring flank (+1 right, -1 left); 0 before a tell. */
    public int flank() {
        return flank;
    }

    /** The side it rises on (+1 right, -1 left); 0 before its first mark. */
    public int side() {
        return side;
    }

    /** The marked rising point's x on the bottom edge (meaningful from its mark on). */
    public double markX() {
        return markX;
    }

    /** How far its time out of sight has run, 0–1 (the bottom-edge glow brightening); 0 when not away. */
    public double awayShare() {
        return mode != Mode.AWAY.ordinal() ? 0 : Math.min(1, (double) modeTicks / Math.max(1, outTicks));
    }

    /** The sweeps told in this attempt. */
    public int sweeps() {
        return sweeps;
    }

    void addTo(StateHash hash) {
        hash.add(mode)
                .add(modeTicks)
                .add(flank)
                .add(side)
                .add(markX)
                .add(nextShot)
                .add(sweeps);
    }
}
