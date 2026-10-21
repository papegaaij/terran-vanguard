package vanguard.sim;

import java.util.List;

/**
 * M7 part F: a walking arena boss's passes, front plate, vents and rear flame cone
 * (design/enemies/bosses/revenant-walker), stepped by its {@link SetPiece} on the simulation's real steps (the
 * level clock is halted in its arena), in the style of the {@link SlamArena}.
 *
 * <p><b>Passes.</b> It arrives off the top edge and walks its intro pass down; it takes no damage until its
 * vital part (the hull) is wholly on the screen, then it engages. Each pass runs from one edge to the opposite
 * one, along a lane of its {@link BossSpec.Stride} chosen when the pass starts from the ship's position (its
 * first phase: the lane beside the ship's), until it has walked off the screen; then it is off the screen for
 * the stride's off time, then enters from the next edge (the edge cycle in turn) with no edge warning (2026-10-21,
 * user: a boss never warns; its entry is the surprise). Its heading is one of 8 (45° steps, 0 straight down,
 * clockwise: 2 left, 4 up, 6 right); its parts, plate, vents and cone turn with it (offsets given as when it faces
 * straight down). In an overheating last phase ({@link BossSpec.Stride#turnInPlaceLastPhase()}) it no longer leaves:
 * where its centre comes to lie its body's reach inside the far edge it stops (its cone's tell, {@link
 * BossSpec.Cone#tellSeconds()}), turns round on the spot toward the ship over the stride's turn time (the cone
 * burning through the turn, sweeping 180°) and walks back toward the ship's lane at that moment (diagonally until it
 * is on it).
 *
 * <p><b>The cone.</b> While it walks on the screen, the ship or Rook behind it (the half plane behind its flamer)
 * within the cone's length plus its reach behind starts a tell (at most once per cooldown, tell start to tell
 * start); the burn follows; in phases after its first the cone's axis swivels toward the ship during the tell, up
 * to its swivel (round 43: and keeps turning toward it through the burn at its track rate, within the swivel). The
 * burn deals its damage once per burn to each of the ship and Rook it touches ({@link #coneTouches}); after it the
 * vents open for the cone's vent time; in the overheating phase they are always open.
 *
 * <p>Everything is allocated here; stepping does not allocate. Its state is hashed with its boss.
 */
public final class StrideArena implements Hashed {
    /** Where the flame cone is in its cycle. */
    public enum ConePhase {
        /** Cold. */
        IDLE,
        /** The tell: the pilot light flares, the cone's outline shimmers; it does not hurt yet. */
        TELL,
        /** Burning: it hurts the ship and Rook once per burn each. */
        BURN
    }

    /** What it does now. */
    public enum Gait {
        /** Not arrived yet (off the screen above the top edge). */
        WAIT,
        /** Walking a pass. */
        WALK,
        /** Off the screen between two passes. */
        OFF,
        /** Overheating: stopped at a pass's end, its cone's tell before the turn. */
        HOLD,
        /** Overheating: turning round on the spot, its cone burning. */
        TURN
    }

    /** One walk cycle (two footfalls, the 8-frame walk), px walked. */
    public static final double STRIDE_PX = 64;

    /** What a burn hit ({@link #coneHit}): the ship. */
    static final int CONE_SHIP = 1;
    /** The cone touched Rook. */
    static final int CONE_WINGMAN = 2;

    private static final double EIGHTH = StrictMath.PI / 4;
    private static final double HALF = Math.sqrt(0.5);
    /** cos and sin of heading k × 45° (clockwise from straight down), exact. */
    private static final double[] COS = {1, HALF, 0, -HALF, -1, -HALF, 0, HALF};

    private static final double[] SIN = {0, HALF, 1, HALF, 0, -HALF, -1, -HALF};

    private final SetPiece piece;
    private final BossSpec.Stride spec;
    /** Null without a plate or a cone. */
    private final BossSpec.Plate plate;

    private final BossSpec.Cone cone;
    private final List<BossSpec.Spot> spots;
    private final int phases;
    /** The part whose box decides the intro (the hull): the plate's part, else the first vital one. */
    private final int hull;
    /** How far beyond an edge its centre lies off the screen: its drawn size's larger half, px. */
    private final double margin;

    private final int offTicks;
    private final int turnTicks;
    private final int tellTicks;
    private final int burnTicks;
    private final int ventSpan;
    private final int cooldownTicks;
    private final double tanHalf;
    /** Round 43: how far a later phase's burn turns toward the ship in a step, radians (0: none). */
    private final double trackStep;
    /** Per part, its box as it faces straight down, turned a quarter, and at a diagonal (the box around it). */
    private final Hitbox[][] partBoxes;

    private final Hitbox[] plateBoxes;
    private final Hitbox[][] spotBoxes;

    // Where the ship and Rook are (set by the sortie before each step).
    private double shipX;
    private double shipY;
    private double rookX;
    private double rookY;
    private boolean rook;

    private Gait gait;
    /** Passes started in this attempt; the next pass enters from the edge cycle's entry at this index. */
    private int passes;
    /** The current pass's entry edge (while off the screen: the next one's). */
    private WaveSpec.Edge edge;

    private double x;
    private double y;
    /** Radians clockwise from straight down (a multiple of 45° except in a turn). */
    private double angle;

    private int heading;
    /** The pass's direction (a unit axis). */
    private double dirX;

    private double dirY;
    /** The cross coordinate of the lane it walks to (x on a vertical pass, y on a horizontal one). */
    private double lane;

    private int gaitTicks;
    private double walked;
    private boolean footfall;
    /** The beside-lane rule's turns from the centre lane (left first). */
    private int flips;

    private double turnFrom;
    private int turnSign;
    private ConePhase conePhase;
    private int coneTicks;
    /** Steps since the last tell started. */
    private int coneSince;
    /** The cone's axis, radians clockwise from straight down. */
    private double coneAngle;
    /** Whether the cone runs the overheating turn's tell and burn. */
    private boolean forced;
    /** What the current burn hit ({@link #CONE_SHIP}, {@link #CONE_WINGMAN}). */
    private int coneHits;

    private int ventTicks;
    /** Round 44: its rail lance and stomp (none without them in its stride). */
    private final StrideStrikes strikes;

    StrideArena(SetPiece piece, BossSpec boss, SetPieceSpec setPiece) {
        this.piece = piece;
        spec = boss.stride().orElseThrow();
        plate = boss.plate().orElse(null);
        cone = boss.cone().orElse(null);
        spots = boss.arena().orElseThrow().spots();
        phases = boss.phases().size();
        int vital = -1;
        for (int p = setPiece.parts().size() - 1; p >= 0; p--) {
            vital = setPiece.parts().get(p).vital() ? p : vital;
        }
        hull = plate != null ? plate.part() : Math.max(0, vital);
        if (hull >= setPiece.parts().size()) {
            throw new IllegalArgumentException(setPiece.slug() + ": its plate covers no part");
        }
        margin = Math.max(setPiece.size().width(), setPiece.size().height()) / 2;
        offTicks = SimStep.ticks(spec.offSeconds());
        turnTicks = Math.max(1, SimStep.ticks(spec.turnSeconds()));
        tellTicks = cone == null ? 0 : SimStep.ticks(cone.tellSeconds());
        burnTicks = cone == null ? 0 : Math.max(1, SimStep.ticks(cone.burnSeconds()));
        ventSpan = cone == null ? 0 : SimStep.ticks(cone.ventSeconds());
        cooldownTicks = cone == null ? 0 : SimStep.ticks(cone.cooldownSeconds());
        tanHalf = cone == null ? 0 : StrictMath.tan(cone.halfAngleRadians());
        trackStep = cone == null ? 0 : cone.trackRadiansPerSecond() * SimStep.SECONDS;
        partBoxes = new Hitbox[setPiece.parts().size()][];
        for (int p = 0; p < partBoxes.length; p++) {
            partBoxes[p] = turnings(setPiece.parts().get(p).box());
        }
        plateBoxes = plate == null ? null : turnings(plate.box());
        spotBoxes = new Hitbox[spots.size()][];
        for (int i = 0; i < spotBoxes.length; i++) {
            spotBoxes[i] = turnings(spots.get(i).box());
        }
        strikes = new StrideStrikes(this, piece, spec, phases);
        reset();
    }

    /** A box as it faces straight down, turned a quarter (its sides swapped) and at a diagonal (the square round it). */
    private static Hitbox[] turnings(Hitbox box) {
        double diagonal = (box.width() + box.height()) * HALF;
        return new Hitbox[] {box, new Hitbox(box.height(), box.width()), new Hitbox(diagonal, diagonal)};
    }

    /** Back to the level start: not arrived, off the screen above the top edge, the cone cold. */
    void reset() {
        gait = Gait.WAIT;
        passes = 0;
        edge = spec.edgeCycle().getFirst();
        x = PlayField.WIDTH / 2.0;
        y = PlayField.HEIGHT + margin;
        angle = 0;
        heading = 0;
        dirX = 0;
        dirY = -1;
        lane = x;
        gaitTicks = 0;
        walked = 0;
        footfall = false;
        flips = 0;
        turnFrom = 0;
        turnSign = 1;
        conePhase = ConePhase.IDLE;
        coneTicks = 0;
        coneSince = Integer.MAX_VALUE / 2;
        coneAngle = StrictMath.PI;
        forced = false;
        coneHits = 0;
        ventTicks = 0;
        if (strikes != null) {
            strikes.reset();
        }
    }

    /** It was destroyed: its cone goes cold, its vents shut (its heading and place stay for the wreck). */
    void died() {
        conePhase = ConePhase.IDLE;
        coneTicks = 0;
        forced = false;
        ventTicks = 0;
        footfall = false;
        strikes.died();
    }

    /** Where the ship and Rook are now ({@code rookIn}: Rook flies); the passes' lanes and the cone read them. */
    void targets(double ship1X, double ship1Y, double wingmanX, double wingmanY, boolean rookIn) {
        shipX = ship1X;
        shipY = ship1Y;
        rookX = wingmanX;
        rookY = wingmanY;
        rook = rookIn;
    }

    /** It arrives: its intro pass starts off the top edge. */
    void arrive(int phase) {
        startPass(phase);
    }

    /** One step of its walk in {@code phase}: the pass, the time off the screen, an overheating turn. */
    void step(int phase) {
        footfall = false;
        switch (gait) {
            case WAIT -> {}
            case WALK -> walk(phase);
            case OFF -> {
                if (++gaitTicks >= offTicks) {
                    startPass(phase);
                }
            }
            case HOLD -> {
                if (++gaitTicks >= tellTicks) {
                    gait = Gait.TURN;
                    gaitTicks = 0;
                    turnFrom = angle;
                }
            }
            case TURN -> turn(phase);
        }
    }

    /** The next pass starts off its entry edge, on the lane its phase chooses. */
    private void startPass(int phase) {
        edge = spec.edgeCycle().get(passes % spec.edgeCycle().size());
        passes++;
        boolean vertical = edge == WaveSpec.Edge.TOP || edge == WaveSpec.Edge.BOTTOM;
        double[] lanes = vertical ? spec.laneXs() : spec.laneYs();
        lane = chooseLane(lanes, vertical ? shipX : shipY, spec.besideLaneFirstPhase() && phase == 0);
        switch (edge) {
            case BOTTOM -> {
                x = lane;
                y = -margin;
                setDirection(0, 1);
            }
            case LEFT -> {
                x = -margin;
                y = lane;
                setDirection(1, 0);
            }
            case RIGHT -> {
                x = PlayField.WIDTH + margin;
                y = lane;
                setDirection(-1, 0);
            }
            default -> {
                x = lane;
                y = PlayField.HEIGHT + margin;
                setDirection(0, -1);
            }
        }
        gait = Gait.WALK;
        gaitTicks = 0;
    }

    private void setDirection(double toX, double toY) {
        dirX = toX;
        dirY = toY;
        heading = headingOf(toX, toY);
        angle = heading * EIGHTH;
    }

    /** The heading (0–7) nearest the direction (dx, dy). */
    private static int headingOf(double dx, double dy) {
        double bearing = StrictMath.atan2(-dx, -dy);
        return Math.floorMod((int) Math.rint(bearing / EIGHTH), 8);
    }

    /**
     * Of {@code lanes}, the one nearest {@code ship} (ties to the lower); {@code beside}: moved one toward the
     * field's centre (from the centre lane to the lower and the higher one in turn).
     */
    private double chooseLane(double[] lanes, double ship, boolean beside) {
        int nearest = nearest(lanes, ship);
        if (!beside || lanes.length < 2) {
            return lanes[nearest];
        }
        double centre = (lanes.length - 1) / 2.0;
        if (nearest < centre) {
            nearest++;
        } else if (nearest > centre) {
            nearest--;
        } else {
            nearest += flips++ % 2 == 0 ? -1 : 1;
        }
        return lanes[nearest];
    }

    private static int nearest(double[] lanes, double at) {
        int best = 0;
        for (int i = 1; i < lanes.length; i++) {
            if (Math.abs(lanes[i] - at) < Math.abs(lanes[best] - at)) {
                best = i;
            }
        }
        return best;
    }

    /** Whether {@code phase} is an overheating last phase. */
    private boolean overheats(int phase) {
        return spec.turnInPlaceLastPhase() && phase == phases - 1;
    }

    /**
     * A walking step at its phase's speed: along the pass, diagonally while it is off its lane; at the pass's end
     * it steps off the screen (or, overheating, stops to turn round).
     */
    private void walk(int phase) {
        double speed = spec.speed(phase) * SimStep.SECONDS;
        double cross = dirX != 0 ? y : x;
        double off = lane - cross;
        double side;
        double along;
        if (Math.abs(off) * Math.sqrt(2) >= speed) {
            side = Math.copySign(speed * HALF, off);
            along = speed * HALF;
        } else {
            side = off;
            along = Math.sqrt(speed * speed - off * off);
        }
        double moveX = dirX * along + (dirX != 0 ? 0 : side);
        double moveY = dirY * along + (dirX != 0 ? side : 0);
        heading = headingOf(moveX, moveY);
        angle = heading * EIGHTH;
        double before = x * dirX + y * dirY;
        x += moveX;
        y += moveY;
        stepped(speed);
        double now = x * dirX + y * dirY;
        double exit = dirX + dirY > 0 ? (dirX != 0 ? PlayField.WIDTH : PlayField.HEIGHT) + margin : margin;
        double turnLine = exit - 2 * margin;
        if (overheats(phase) && before < turnLine && now >= turnLine) {
            // Overheating: it stops with its body just on the screen and turns round.
            x -= dirX * (now - turnLine);
            y -= dirY * (now - turnLine);
            gait = Gait.HOLD;
            gaitTicks = 0;
            double bearing = StrictMath.atan2(-(shipX - x), -(shipY - y));
            turnSign = Math.IEEEremainder(bearing - angle, 2 * StrictMath.PI) < 0 ? -1 : 1;
            return;
        }
        if (now >= exit) {
            gait = Gait.OFF;
            gaitTicks = 0;
            edge = spec.edgeCycle().get(passes % spec.edgeCycle().size());
            conePhase = ConePhase.IDLE;
            forced = false;
        }
    }

    /** A step of the overheating turn round: 180° toward the ship's side, then the walk back toward its lane. */
    private void turn(int phase) {
        gaitTicks++;
        double share = Math.min(1, (double) gaitTicks / turnTicks);
        angle = turnFrom + turnSign * StrictMath.PI * share;
        heading = Math.floorMod((int) Math.rint(angle / EIGHTH), 8);
        stepped(spec.speed(phase) * SimStep.SECONDS);
        if (share >= 1) {
            setDirection(-dirX, -dirY);
            double[] lanes = dirX == 0 ? spec.laneXs() : spec.laneYs();
            lane = lanes[nearest(lanes, dirX == 0 ? shipX : shipY)];
            gait = Gait.WALK;
            gaitTicks = 0;
        }
    }

    /** It stepped {@code distance} px (in a turn: on the spot): a footfall every half walk cycle. */
    private void stepped(double distance) {
        double half = STRIDE_PX / 2;
        footfall = Math.floor((walked + distance) / half) > Math.floor(walked / half);
        walked = (walked + distance) % (STRIDE_PX * 1024);
    }

    /**
     * One step of the flame cone and the vents after the walk (the boss engaged, {@code firing}: the level's
     * fire runs): the overheating turn's tell and burn, else a tell when the ship or Rook is behind it, the burn,
     * the vents after it.
     */
    void act(int phase, SetPiece.BossActions actions, boolean firing) {
        if (ventTicks > 0) {
            ventTicks--;
        }
        if (coneSince < Integer.MAX_VALUE / 2) {
            coneSince++;
        }
        boolean walking = gait == Gait.WALK || gait == Gait.HOLD || gait == Gait.TURN;
        // Round 44: the rail lance and the stomp, while it walks on the screen with its hull on it.
        strikes.act(phase, actions, firing && walking && PlayField.overlaps(x, y, partBoxes[hull][0]));
        if (cone == null) {
            return;
        }
        double base = angle + StrictMath.PI;
        if (gait == Gait.HOLD && !(forced && conePhase == ConePhase.TELL)) {
            forced = true;
            startTell(base, actions);
        } else if (gait == Gait.TURN && forced && conePhase == ConePhase.TELL) {
            startBurn(actions);
        }
        switch (conePhase) {
            case IDLE -> {
                if (firing
                        && gait == Gait.WALK
                        && coneSince >= cooldownTicks
                        && PlayField.overlaps(x, y, partBoxes[hull][0])
                        && (behind(shipX, shipY) || (rook && behind(rookX, rookY)))) {
                    forced = false;
                    startTell(base, actions);
                }
            }
            case TELL -> {
                coneTicks++;
                if (forced) {
                    coneAngle = base;
                } else {
                    double swivel = phase > 0 ? cone.swivelRadians() : 0;
                    double toShip = StrictMath.atan2(-(shipX - coneX()), -(shipY - coneY()));
                    coneAngle =
                            base + Math.clamp(Math.IEEEremainder(toShip - base, 2 * StrictMath.PI), -swivel, swivel);
                    if (coneTicks >= tellTicks) {
                        startBurn(actions);
                    }
                }
            }
            case BURN -> {
                coneTicks++;
                if (forced) {
                    coneAngle = base;
                    if (gait != Gait.TURN && gait != Gait.HOLD) {
                        endBurn();
                    }
                } else if (coneTicks >= burnTicks) {
                    endBurn();
                } else if (phase > 0 && trackStep > 0) {
                    // Round 43: a later phase's burn keeps turning toward the ship, within its swivel.
                    double swivel = cone.swivelRadians();
                    double toShip = StrictMath.atan2(-(shipX - coneX()), -(shipY - coneY()));
                    double goal =
                            base + Math.clamp(Math.IEEEremainder(toShip - base, 2 * StrictMath.PI), -swivel, swivel);
                    coneAngle +=
                            Math.clamp(Math.IEEEremainder(goal - coneAngle, 2 * StrictMath.PI), -trackStep, trackStep);
                }
            }
        }
    }

    private void startTell(double base, SetPiece.BossActions actions) {
        conePhase = ConePhase.TELL;
        coneTicks = 0;
        coneSince = 0;
        coneAngle = base;
        actions.coneTell(coneX(), coneY());
    }

    private void startBurn(SetPiece.BossActions actions) {
        conePhase = ConePhase.BURN;
        coneTicks = 0;
        coneHits = 0;
        actions.coneBurn(coneX(), coneY());
    }

    private void endBurn() {
        conePhase = ConePhase.IDLE;
        coneTicks = 0;
        forced = false;
        ventTicks = ventSpan;
    }

    /** Whether (px, py) lies behind the flamer within the cone's length and its reach behind. */
    private boolean behind(double px, double py) {
        double dx = px - coneX();
        double dy = py - coneY();
        double backX = -Trig.sin(angle + StrictMath.PI);
        double backY = -Trig.cos(angle + StrictMath.PI);
        double reach = cone.length() + cone.reachBehind();
        return dx * backX + dy * backY > 0 && dx * dx + dy * dy <= reach * reach;
    }

    /** Whether (px, py) lies in the cone now (whatever its phase). */
    boolean inCone(double px, double py) {
        double dx = px - coneX();
        double dy = py - coneY();
        double ux = -Trig.sin(coneAngle);
        double uy = -Trig.cos(coneAngle);
        double along = dx * ux + dy * uy;
        if (along < 0 || dx * dx + dy * dy > cone.length() * cone.length()) {
            return false;
        }
        return Math.abs(dx * uy - dy * ux) <= along * tanHalf;
    }

    /**
     * Whether the cone, burning, touches {@code hull} at (hx, hy): a box's centre or corner in it, or one of its
     * two edges through a box.
     */
    boolean coneTouches(Hull hull1, double hx, double hy) {
        if (cone == null || conePhase != ConePhase.BURN) {
            return false;
        }
        for (int i = 0; i < hull1.parts().size(); i++) {
            Hull.Part part = hull1.parts().get(i);
            double cx = hx + part.dx();
            double cy = hy + part.dy();
            double w = part.box().width() / 2;
            double h = part.box().height() / 2;
            if (inCone(cx, cy)
                    || inCone(cx - w, cy - h)
                    || inCone(cx + w, cy - h)
                    || inCone(cx - w, cy + h)
                    || inCone(cx + w, cy + h)) {
                return true;
            }
        }
        double ox = coneX();
        double oy = coneY();
        double length = cone.length();
        for (int side = -1; side <= 1; side += 2) {
            double edgeAngle = coneAngle + side * cone.halfAngleRadians();
            if (hull1.touchesSegment(
                    hx, hy, ox, oy, ox - Trig.sin(edgeAngle) * length, oy - Trig.cos(edgeAngle) * length, 0)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the current burn hit {@code what} ({@link #CONE_SHIP}, {@link #CONE_WINGMAN}) already. */
    boolean coneHit(int what) {
        return (coneHits & what) != 0;
    }

    /** The current burn hit {@code what}: not again in this burn. */
    void markConeHit(int what) {
        coneHits |= what;
    }

    /**
     * The damage multiplier of a hit on part {@code p} at (hx, hy) by a shot flying along (dirX, dirY) (0, 0 for a
     * blast): an open vent's on the shot's line ahead (as {@link SlamArena#multiplier}), else the part's own.
     */
    double multiplier(int p, double hx, double hy, double dirX1, double dirY1, double partMultiplier) {
        int turning = turning();
        for (int i = 0; i < spots.size(); i++) {
            BossSpec.Spot spot = spots.get(i);
            if (spot.part() != p || (spot.whileOpen() && !ventsOpen())) {
                continue;
            }
            Hitbox box = spotBoxes[i][turning];
            double sx = piece.partX(p) + turnX(spot.dx(), spot.dy());
            double sy = piece.partY(p) + turnY(spot.dx(), spot.dy());
            if (SlamArena.ahead(hx - sx, hy - sy, dirX1, dirY1, box.width() / 2, box.height() / 2)) {
                return spot.multiplier();
            }
        }
        return partMultiplier;
    }

    /** Which of a box's turnings its heading takes: 0 as it faces up or down, 1 sideways, 2 at a diagonal. */
    private int turning() {
        return heading % 2 == 1 ? 2 : heading % 4 == 2 ? 1 : 0;
    }

    /** Part {@code p}'s box at its heading. */
    Hitbox partBox(int p) {
        return partBoxes[p][turning()];
    }

    /** An offset (dx right, dy up, as it faces straight down) turned with its heading: across. */
    double turnX(double dx, double dy) {
        return dx * COS[heading] + dy * SIN[heading];
    }

    /** ... and up. */
    double turnY(double dx, double dy) {
        return -dx * SIN[heading] + dy * COS[heading];
    }

    /** Whether a shot of {@code size} at (sx, sy) touches the plate (while the part it covers lives). */
    boolean plateTouches(Hitbox size, double sx, double sy) {
        return plate != null
                && !piece.partWrecked(plate.part())
                && size.overlaps(sx, sy, plateBoxes[turning()], plateX(), plateY());
    }

    /**
     * Where a beam's column (centred on {@code cx}, {@code halfWidth} either side, running up from {@code fromY})
     * first touches the plate; {@link Double#POSITIVE_INFINITY} for none (or no plate).
     */
    double plateColumn(double cx, double halfWidth, double fromY) {
        if (plate == null || piece.partWrecked(plate.part())) {
            return Double.POSITIVE_INFINITY;
        }
        return PlayerFire.columnContact(cx, halfWidth, fromY, plateX(), plateY(), plateBoxes[turning()]);
    }

    // --- the presentation's view ---------------------------------------------------------------

    /** Its heading, 0–7: 45° steps clockwise from straight down (2 left, 4 up, 6 right); its sprite's. */
    public int heading() {
        return heading;
    }

    /** Its heading in radians clockwise from straight down (between the 8 headings in a turn). */
    public double headingRadians() {
        return angle;
    }

    /** Where it is in its 8-frame walk cycle, 0–1 (it steps on the spot in a turn). */
    public double walkPhase() {
        return walked % STRIDE_PX / STRIDE_PX;
    }

    /** Whether a foot came down in the last step (its {@link SimEvents.Type#WALKER_STEP}). */
    boolean footfall() {
        return footfall;
    }

    /** What it does now. */
    public Gait gait() {
        return gait;
    }

    /** Whether it is off the screen (before its arrival or between two passes). */
    public boolean offScreen() {
        return gait == Gait.WAIT || gait == Gait.OFF;
    }

    /** The edge its current pass entered from; while off the screen the next pass's. */
    public WaveSpec.Edge nextEdge() {
        return edge;
    }

    /** How many passes it has started in this attempt (the intro is the first). */
    public int passes() {
        return passes;
    }

    /** The bottom edge's {@link WarningEdge} bit while it walks up the screen (a rear wave for Rook), else 0. */
    public int rear() {
        boolean onScreen = gait == Gait.WALK || gait == Gait.HOLD || gait == Gait.TURN;
        return onScreen && heading >= 3 && heading <= 5 ? WarningEdge.BOTTOM.bit() : 0;
    }

    /** Where its flame cone is in its cycle. */
    public ConePhase conePhase() {
        return conePhase;
    }

    /** The cone's axis, radians clockwise from straight down (against its heading, swivelled in a tell). */
    public double coneHeading() {
        return coneAngle;
    }

    /** How far the cone's tell or burn has run, 0–1 (0 when cold; an overheating burn by its turn). */
    public double coneShare() {
        return switch (conePhase) {
            case IDLE -> 0;
            case TELL -> Math.min(1, (double) coneTicks / Math.max(1, tellTicks));
            case BURN -> Math.min(1, forced ? (double) gaitTicks / turnTicks : (double) coneTicks / burnTicks);
        };
    }

    /** The flamer's position (the cone's apex), px from the left. */
    public double coneX() {
        return x + (cone == null ? 0 : turnX(0, cone.originBehind()));
    }

    /** The flamer's position, px up from the bottom edge. */
    public double coneY() {
        return y + (cone == null ? 0 : turnY(0, cone.originBehind()));
    }

    /** Whether its vents are open (×2): for its cone's vent time after each burn, always while overheating. */
    public boolean ventsOpen() {
        return !piece.destroyed() && (ventTicks > 0 || overheats(piece.phase()));
    }

    /**
     * Round 43 (the vent-hit sound): whether (px, py) lies on its open vents (a {@code while: open} spot's box turned with
     * its heading, grown by {@code margin} px each side) while they are open.
     */
    public boolean onOpenVents(double px, double py, double margin) {
        if (!ventsOpen()) {
            return false;
        }
        int turning = turning();
        for (int i = 0; i < spots.size(); i++) {
            BossSpec.Spot spot = spots.get(i);
            if (!spot.whileOpen()) {
                continue;
            }
            Hitbox box = spotBoxes[i][turning];
            double sx = piece.partX(spot.part()) + turnX(spot.dx(), spot.dy());
            double sy = piece.partY(spot.part()) + turnY(spot.dx(), spot.dy());
            if (Math.abs(px - sx) <= box.width() / 2 + margin && Math.abs(py - sy) <= box.height() / 2 + margin) {
                return true;
            }
        }
        return false;
    }

    /** The plate's centre (its box turned with the heading: {@link #plateBox()}), px from the left. */
    public double plateX() {
        return plate == null ? x : piece.partX(plate.part()) + turnX(0, -plate.ahead());
    }

    /** The plate's centre, px up. */
    public double plateY() {
        return plate == null ? y : piece.partY(plate.part()) + turnY(0, -plate.ahead());
    }

    /** The plate's box at its heading; null without one. */
    public Hitbox plateBox() {
        return plate == null ? null : plateBoxes[turning()];
    }

    /**
     * The 16 headings a pod snaps to (22.5° steps clockwise from straight down) toward (toX, toY) from (fromX,
     * fromY), as its missiles leave: the pods' look.
     */
    public static int bearing16(double fromX, double fromY, double toX, double toY) {
        double bearing = StrictMath.atan2(-(toX - fromX), -(toY - fromY));
        return Math.floorMod((int) Math.rint(bearing / (StrictMath.PI / 8)), 16);
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    /** The part whose box decides the intro: the hull. */
    int hull() {
        return hull;
    }

    double shipX() {
        return shipX;
    }

    double shipY() {
        return shipY;
    }

    double rookX() {
        return rookX;
    }

    double rookY() {
        return rookY;
    }

    /** Whether Rook flies. */
    boolean rookIn() {
        return rook;
    }

    /** Round 44: its rail lance and stomp (their phases idle without them in its stride). */
    public StrideStrikes strikes() {
        return strikes;
    }

    /** Round 44: whether it overheats now (its last phase with {@link BossSpec.Stride#turnInPlaceLastPhase()}). */
    boolean overheating() {
        return overheats(piece.phase());
    }

    /** Round 44: whether a part that fires a homing attack takes damage only while its hatch is open. */
    boolean hatchArmour() {
        return spec.hatchArmour();
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(gait.ordinal())
                .add(passes)
                .add(edge.ordinal())
                .add(x)
                .add(y)
                .add(angle)
                .add(heading)
                .add(dirX)
                .add(dirY)
                .add(lane)
                .add(gaitTicks)
                .add(walked)
                .add(flips)
                .add(turnFrom)
                .add(turnSign)
                .add(conePhase.ordinal())
                .add(coneTicks)
                .add(coneSince)
                .add(coneAngle)
                .add(forced ? 1 : 0)
                .add(coneHits)
                .add(ventTicks);
        strikes.addTo(hash);
    }
}
