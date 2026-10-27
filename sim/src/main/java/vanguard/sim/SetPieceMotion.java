package vanguard.sim;

import java.util.List;

/**
 * A set piece's motion (out of {@link SetPiece}): its passes' flight, and a boss's arrival, descent and sway, its
 * phases' moves (a glide, then a turn into a new pose), an anchored boss's ground and lift, and the arena bosses'
 * own motion (a walk, a breach cycle, a coil) read back into its centre; its pose and the lift's stump. The
 * coordinator hashes its fields.
 */
final class SetPieceMotion {
    private final SetPiece piece;
    private final SetPieceSpec spec;
    private final BossSpec boss;
    private final SlamArena arena;
    private final StrideArena stride;
    private final BreachArena breach;
    private final CoilArena coil;
    private final double extent;
    /** {@link SetPieceParts#partHp} and {@link SetPieceParts#armoured}: the lift reads which parts stay. */
    private final double[] partHp;

    private final boolean[] armoured;

    int pass;
    boolean present;
    boolean escaped;
    boolean onPlane;
    Layer layer;
    double x;
    double y;
    double prevX;
    double prevY;
    double altitude;
    double prevAltitude;
    double cos;
    double sin;

    private final double entryY;
    /** Steps since it arrived; -1 before. */
    int bossTicks;
    /** Steps since it settled; -1 before. */
    int swayTicks;
    /** The step of {@link #swayTicks} its sway (re)started from, at its station. */
    int swayStart;
    /** Steps into the phase's opening move; -1 when none runs. */
    int moveTicks;

    double moveFromX;
    double moveFromY;
    double moveFromAltitude;
    Layer moveFromLayer;
    /** Its station: where it sways. */
    double anchorX;

    double anchorY;
    /** Its pose (an index into its part layouts) and the one a move turns it from. */
    int pose;

    int fromPose;
    /** M5 part E: where an arena boss's centre lies on the ground layer, px above the screen's bottom at the level start. */
    private double anchorGround;
    /** M5 part E: the ground's scroll this step and whether the scroll has halted in the arena. */
    private double groundScroll;
    /**
     * Round 41: how far the ground scrolled in this step ({@link #ground}), which a destroyed arena boss
     * moves down with; derived from the sortie's scroll every step, so not hashed.
     */
    private double groundStep;

    private boolean halted;
    /** M5 part E: an arena boss scrolling in with the ground before it arrives (no bar, no damage). */
    boolean approaching;

    /**
     * M5 part H: whether an anchored boss has a lift (a phase with a move): its state is hashed only then, so Level
     * 11's Kraken hashes as before.
     */
    final boolean lifts;
    /**
     * M5 part H, the lift (design/enemies/bosses/siege-spire; decision H4 = a): whether it has left its
     * anchor (from its lift's start), per part whether it stays at the anchor (the stump: the parts
     * destroyed or armoured when it lifted), the pose it lifted from, and the step of {@link #bossTicks}
     * its lift ended at (-1 before).
     */
    boolean lifted;

    final boolean[] stays;
    int liftPose;
    int liftEndTicks;

    SetPieceMotion(SetPiece piece, SetPieceSpec spec, BossSpec boss) {
        this.piece = piece;
        this.spec = spec;
        this.boss = boss;
        arena = piece.arena;
        stride = piece.stride;
        breach = piece.breach;
        coil = piece.coil;
        partHp = piece.parts.partHp;
        armoured = piece.parts.armoured;
        extent = Math.max(spec.size().width(), spec.size().height()) / 2;
        int parts = spec.parts().size();
        // A boss arriving on high air is drawn (and its parts placed) at the high-air scale.
        double scale = boss != null && boss.layer() == Layer.HIGH_AIR ? BossSpec.HIGH_AIR_SCALE : 1;
        double reach = spec.size().height() / 2 * scale;
        for (int p = 0; p < parts; p++) {
            PartSpec part = spec.parts().get(p);
            reach = Math.max(
                    reach, (Math.abs(piece.parts.poseDy[0][p]) + part.box().height() / 2) * scale);
        }
        entryY = PlayField.HEIGHT + reach;
        int phases = boss == null ? 0 : boss.phases().size();
        boolean moves = false;
        for (int f = 0; f < phases; f++) {
            moves |= boss.phases().get(f).move().isPresent();
        }
        lifts = arena != null && moves;
        stays = new boolean[parts];
    }

    /**
     * Back to the level start: before its first pass; a boss above the top edge, not arrived (the coordinator then
     * places an arena boss where its arena has it).
     */
    void reset() {
        pass = 0;
        present = false;
        escaped = false;
        onPlane = false;
        layer = Layer.HIGH_AIR;
        x = y = prevX = prevY = 0;
        altitude = prevAltitude = 1;
        cos = 1;
        sin = 0;
        if (boss != null) {
            layer = boss.layer();
            bossTicks = -1;
            swayTicks = -1;
            swayStart = 0;
            moveTicks = -1;
            moveFromX = moveFromY = moveFromAltitude = 0;
            moveFromLayer = layer;
            anchorX = boss.x();
            anchorY = boss.hoverY();
            pose = fromPose = 0;
            x = prevX = boss.x();
            y = prevY = entryY;
            altitude = prevAltitude = layer == Layer.HIGH_AIR ? 1 : 0;
            approaching = false;
            lifted = false;
            java.util.Arrays.fill(stays, false);
            liftPose = 0;
            liftEndTicks = -1;
        }
    }

    /** Places its centre at (x, y) without a jump from the last step. */
    void placeAt(double x, double y) {
        this.x = prevX = x;
        this.y = prevY = y;
    }

    /** As {@link SetPiece#anchorAt}. */
    void anchorAt(double ground) {
        anchorGround = ground;
    }

    /** As {@link SetPiece#ground}. */
    void ground(double scroll, boolean halt) {
        groundStep = scroll - groundScroll;
        groundScroll = scroll;
        halted = halt;
    }

    /** Where it was at the end of the last step (a boss keeps its altitude's until it moves). */
    void keepPrev(boolean withAltitude) {
        prevX = x;
        prevY = y;
        if (withAltitude) {
            prevAltitude = altitude;
        }
    }

    /** The unit is destroyed: it leaves the play field at once. */
    void vanish() {
        present = false;
        onPlane = false;
    }

    /**
     * Flies its passes to where they have it at {@code levelTick}; returns whether its last pass ended with it alive
     * this step (it escaped).
     */
    boolean fly(int levelTick) {
        double t = levelTick * SimStep.SECONDS;
        List<Pass> passes = spec.passes();
        while (pass < passes.size()) {
            Pass current = passes.get(pass);
            if (t < current.start()) {
                present = false;
                return false;
            }
            if (place(current, t)) {
                if (!present) {
                    // Entering a pass: it appears at its first point, without a jump from the last one.
                    present = true;
                    prevX = x;
                    prevY = y;
                    prevAltitude = altitude;
                    cos = Trig.cos(current.headingRadians());
                    sin = Trig.sin(current.headingRadians());
                }
                boolean plane = layer.collidesWithPlayer();
                if (plane && !onPlane) {
                    piece.guns.arm();
                }
                onPlane = plane;
                return false;
            }
            present = false;
            onPlane = false;
            pass++;
        }
        escaped = true;
        return true;
    }

    /** Places it on {@code pass} at {@code t}; returns false once the pass is over. */
    private boolean place(Pass current, double t) {
        List<Waypoint> path = current.path();
        if (!current.descends()) {
            if (t > path.getLast().t()) {
                return false;
            }
            along(path, t);
            layer = current.layer();
            altitude = layer == Layer.HIGH_AIR ? 1 : 0;
            return true;
        }
        if (t < current.leaveAt()) {
            along(path, t);
            if (t < current.descendAt() + current.descentSeconds()) {
                layer = Layer.HIGH_AIR;
                altitude = t < current.descendAt() ? 1 : 1 - (t - current.descendAt()) / current.descentSeconds();
            } else {
                layer = current.layer();
                altitude = 0;
            }
            return true;
        }
        along(path, current.leaveAt());
        y += (t - current.leaveAt()) * current.leaveSpeed();
        layer = Layer.HIGH_AIR;
        altitude = Math.min(1, (t - current.leaveAt()) / current.descentSeconds());
        return y - extent < PlayField.HEIGHT;
    }

    /** Its centre on the path at {@code t}: between the waypoints around it, at the last one after it. */
    private void along(List<Waypoint> path, double t) {
        Waypoint last = path.getLast();
        if (t >= last.t()) {
            x = last.x();
            y = last.y();
            return;
        }
        for (int i = 1; i < path.size(); i++) {
            Waypoint to = path.get(i);
            if (t < to.t()) {
                Waypoint from = path.get(i - 1);
                double p = Math.max(0, (t - from.t()) / (to.t() - from.t()));
                x = from.x() + (to.x() - from.x()) * p;
                y = from.y() + (to.y() - from.y()) * p;
                return;
            }
        }
    }

    /**
     * A boss arrives at its time, descends to its height and then sways there; a phase's move
     * glides it to a new station and layer and turns it into a new pose first.
     */
    void moveBoss(int levelTick) {
        if (piece.destroyed()) {
            if (arena != null) {
                // Round 41 (the 2026-10-19 playtest): a dead arena boss, the free spire too, falls to the
                // ground and scrolls away with it, with its stump, so every part's place moves with the ground.
                y -= groundStep;
            }
            return;
        }
        prevAltitude = altitude;
        if (breach != null) {
            moveBreach(levelTick);
            return;
        }
        if (coil != null) {
            moveCoil(levelTick);
            return;
        }
        if (stride != null) {
            moveStride(levelTick);
            return;
        }
        if (arena != null) {
            moveAnchored(levelTick);
            return;
        }
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = layer == Layer.HIGH_AIR ? 1 : 0;
            x = prevX = boss.x();
            y = prevY = entryY;
            if (boss.engagesOnArrival()) {
                piece.guns.engage();
            }
        }
        bossTicks++;
        if (moveTicks >= 0) {
            stepMove();
        } else if (swayTicks < 0) {
            y = Math.max(anchorY, entryY - bossTicks * boss.descentSpeed() * SimStep.SECONDS);
            if (y <= anchorY) {
                swayTicks = 0;
                if (!piece.engaged()) {
                    piece.guns.engage();
                }
            }
        } else {
            swayTicks++;
            x = anchorX
                    + boss.sineAmplitude()
                            * Trig.sin(
                                    2 * StrictMath.PI * (swayTicks - swayStart) * SimStep.SECONDS / boss.sinePeriod());
        }
    }

    /**
     * M5 part E: an arena boss lies on the ground and scrolls with it: before it arrives it scrolls in
     * ({@link SetPiece#approaching()} once a part of it may be over the top edge); it arrives on its time (the
     * bar), settles and engages when the scroll has halted. M5 part H: a phase's move lifts it off its
     * anchor ({@link #stepLift()}); from then on it holds at its new station, swaying.
     */
    private void moveAnchored(int levelTick) {
        if (!lifted) {
            x = boss.x();
            y = anchorGround - groundScroll;
        }
        if (bossTicks < 0) {
            approaching = y - (entryY - PlayField.HEIGHT) < PlayField.HEIGHT;
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            approaching = false;
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = 0;
        }
        bossTicks++;
        if (moveTicks >= 0) {
            stepLift();
        } else if (swayTicks >= 0) {
            swayTicks++;
            if (lifted) {
                x = anchorX
                        + boss.sineAmplitude()
                                * Trig.sin(2
                                        * StrictMath.PI
                                        * (swayTicks - swayStart)
                                        * SimStep.SECONDS
                                        / boss.sinePeriod());
            }
        } else if (halted) {
            swayTicks = 0;
            piece.guns.engage();
        }
    }

    /**
     * M7 part F: a walking boss arrives on its time off the top edge and walks its passes ({@link StrideArena});
     * it settles and engages once its hull is wholly on the screen (invulnerable until then).
     */
    private void moveStride(int levelTick) {
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = 0;
            stride.arrive(piece.phase());
            x = prevX = stride.x();
            y = prevY = stride.y();
        } else {
            stride.step(piece.phase());
            x = stride.x();
            y = stride.y();
        }
        bossTicks++;
        if (swayTicks >= 0) {
            swayTicks++;
        } else if (piece.parts.wholeOnField(stride.hull())) {
            swayTicks = 0;
            piece.guns.engage();
        }
    }

    /**
     * M7 part H: a breaching boss arrives on its time (the bar), buried; its cycle runs on the real steps ({@link
     * BreachArena}) and it engages when its intro is over (its first tell); its centre is its mouth's.
     */
    private void moveBreach(int levelTick) {
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            onPlane = layer.collidesWithPlayer();
            altitude = prevAltitude = 0;
            breach.arrive();
        } else {
            breach.step(piece.phase());
        }
        x = breach.mouthX();
        y = breach.mouthY();
        if (piece.parts.poseDx.length == BreachArena.POSES) {
            // Its data's poses, one per breach point in the poses' order: the mouth's part layout at the point.
            pose = fromPose = breach.pose().ordinal();
        }
        bossTicks++;
        if (swayTicks >= 0) {
            swayTicks++;
        } else if (breach.engages()) {
            swayTicks = 0;
            piece.guns.engage();
        }
    }

    /**
     * M8 part H: a coiled boss arrives on its time (the bar) under the water and rises over its intro; its script runs
     * on the real steps ({@link CoilArena}) and it engages once it is up; its centre is its head's, on the play plane
     * (its body colliding) while the head is.
     */
    private void moveCoil(int levelTick) {
        if (bossTicks < 0) {
            if (levelTick < SimStep.ticks(boss.arriveSeconds())) {
                return;
            }
            present = true;
            layer = boss.layer();
            altitude = prevAltitude = 0;
            coil.arrive();
        } else {
            coil.step(piece.phase());
        }
        x = coil.headX();
        y = coil.headY();
        onPlane = coil.headOnPlane() && layer.collidesWithPlayer();
        bossTicks++;
        if (swayTicks >= 0) {
            swayTicks++;
        } else if (coil.engages()) {
            swayTicks = 0;
            piece.guns.engage();
        }
    }

    /** As {@link SetPiece#coilTargets}. */
    void coilTargets(double shipX, double shipY) {
        if (coil != null) {
            coil.targets(shipX, shipY);
        }
    }

    /** As {@link SetPiece#breachTargets}. */
    void breachTargets(double shipX, double shipY) {
        if (breach != null) {
            breach.targets(shipX, shipY);
        }
    }

    /** As {@link SetPiece#strideTargets}. */
    void strideTargets(double shipX, double shipY, double rookX, double rookY, boolean rookIn) {
        if (stride != null) {
            stride.targets(shipX, shipY, rookX, rookY, rookIn);
        }
    }

    /**
     * M5 part H, one step of an anchored boss's lift (design/enemies/bosses/siege-spire; decision H4 =
     * a): it glides from its anchor to the move's station, eased (smoothstep); at the glide's end it and
     * its lifting parts take the move's layer (on the player's layer only from then: no contact while it
     * lifts), then it turns into the move's pose, and at the end the phase engages there.
     */
    private void stepLift() {
        BossSpec.Move move = boss.phases().get(piece.phase()).move().orElseThrow();
        moveTicks++;
        int glide = SimStep.ticks(move.descendSeconds());
        int turnSteps = SimStep.ticks(move.turnSeconds());
        double share = glide == 0 ? 1 : Math.min(1, (double) moveTicks / glide);
        double eased = share * share * (3 - 2 * share);
        x = moveFromX + (move.x() - moveFromX) * eased;
        y = moveFromY + (move.y() - moveFromY) * eased;
        if (share >= 1 && layer != move.layer()) {
            layer = move.layer();
            for (int p = 0; p < stays.length; p++) {
                if (!stays[p]) {
                    arena.lift(p, move.layer());
                }
            }
        }
        onPlane = layer.collidesWithPlayer();
        if (moveTicks >= glide && 2 * (moveTicks - glide) >= turnSteps) {
            pose = move.pose();
        }
        if (moveTicks >= glide + turnSteps) {
            pose = fromPose = move.pose();
            anchorX = move.x();
            anchorY = move.y();
            moveTicks = -1;
            swayStart = swayTicks;
            liftEndTicks = bossTicks;
            piece.guns.armPhase();
        }
    }

    /**
     * One step of the phase's move: the glide to its station and layer, then the turn in place
     * (the pose switching half-way); at its end the phase engages.
     */
    private void stepMove() {
        BossSpec.Move move = boss.phases().get(piece.phase()).move().orElseThrow();
        moveTicks++;
        int descend = SimStep.ticks(move.descendSeconds());
        int turnSteps = SimStep.ticks(move.turnSeconds());
        double share = descend == 0 ? 1 : Math.min(1, (double) moveTicks / descend);
        double toAltitude = move.layer() == Layer.HIGH_AIR ? 1 : 0;
        x = moveFromX + (move.x() - moveFromX) * share;
        y = moveFromY + (move.y() - moveFromY) * share;
        altitude = moveFromAltitude + (toAltitude - moveFromAltitude) * share;
        if (share >= 1) {
            layer = move.layer();
        } else if (moveFromLayer == Layer.HIGH_AIR || move.layer() == Layer.HIGH_AIR) {
            layer = Layer.HIGH_AIR;
        } else {
            layer = move.layer();
        }
        onPlane = layer.collidesWithPlayer();
        if (moveTicks >= descend && 2 * (moveTicks - descend) >= turnSteps) {
            pose = move.pose();
        }
        if (moveTicks >= descend + turnSteps) {
            pose = fromPose = move.pose();
            anchorX = move.x();
            anchorY = move.y();
            moveTicks = -1;
            if (swayTicks < 0) {
                // A move cut its entrance short: it holds at the new station without settling again.
                swayTicks = 1;
            }
            swayStart = swayTicks;
            piece.guns.armPhase();
        }
    }

    /**
     * A phase starts ({@link SetPieceGuns#startPhase}): M5 part H, an anchored boss's first move lifts it (what is
     * destroyed or armoured now stays at the anchor, the stump); a phase with a move begins it here. Returns whether it
     * began a move (the phase engages at its end).
     */
    boolean beginMove(BossSpec.Phase current) {
        if (current.move().isPresent() && arena != null && !lifted) {
            // M5 part H: the lift: what is destroyed or armoured now stays at the anchor (the stump).
            lifted = true;
            liftPose = pose;
            for (int p = 0; p < stays.length; p++) {
                stays[p] = armoured[p] || partHp[p] <= 0;
            }
        }
        if (current.move().isPresent()) {
            moveTicks = 0;
            moveFromX = x;
            moveFromY = y;
            moveFromAltitude = altitude;
            moveFromLayer = layer;
            fromPose = pose;
            return true;
        }
        return false;
    }

    /** As {@link SetPiece#liftShare}. */
    double liftShare(double alpha) {
        if (!lifted) {
            return 0;
        }
        if (moveTicks < 0) {
            return 1;
        }
        int glide = SimStep.ticks(
                boss.phases().get(piece.phase()).move().orElseThrow().descendSeconds());
        return glide == 0 ? 1 : Math.clamp((moveTicks - 1 + alpha) / glide, 0.0, 1.0);
    }

    /** As {@link SetPiece#stumpX}. */
    double stumpX() {
        return boss == null ? x : boss.x();
    }

    /** As {@link SetPiece#stumpY}. */
    double stumpY() {
        return anchorGround - groundScroll;
    }

    /** As {@link SetPiece#scale}. */
    double scale() {
        return 1 + (BossSpec.HIGH_AIR_SCALE - 1) * altitude;
    }

    /** As {@link SetPiece#motion}. */
    SetPiece.Motion motion() {
        if (boss == null || !present || piece.destroyed() || bossTicks < 0) {
            return SetPiece.Motion.NONE;
        }
        if (moveTicks >= 0) {
            int descend = SimStep.ticks(
                    boss.phases().get(piece.phase()).move().orElseThrow().descendSeconds());
            return moveTicks < descend ? SetPiece.Motion.DESCEND : SetPiece.Motion.TURN;
        }
        return swayTicks < 0 ? SetPiece.Motion.PASS : SetPiece.Motion.HOLD;
    }

    /** As {@link SetPiece#turnToPose}. */
    int turnToPose() {
        return moveTicks >= 0
                ? boss.phases().get(piece.phase()).move().orElseThrow().pose()
                : pose;
    }

    /** As {@link SetPiece#turnShare}. */
    double turnShare(double alpha) {
        if (moveTicks < 0) {
            return 0;
        }
        BossSpec.Move move = boss.phases().get(piece.phase()).move().orElseThrow();
        int descend = SimStep.ticks(move.descendSeconds());
        int turnSteps = SimStep.ticks(move.turnSeconds());
        if (turnSteps == 0) {
            return moveTicks >= descend ? 1 : 0;
        }
        return Math.clamp((moveTicks - 1 + alpha - descend) / turnSteps, 0, 1);
    }
}
