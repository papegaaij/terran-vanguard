package vanguard.sim;

/**
 * M11 part A (the movement vocabulary's {@code teleport} and its fairness rules; design/enemies/space/rift-skater): one
 * unit's {@link EnemySpec.Teleport}. It flies its path as any unit until it has glided its first glide on the play
 * field; then each jump: a {@link Phase#FADE} (it holds still, hittable; {@link SimEvents.Type#JUMP_OUT}), the
 * {@link Phase#GONE} time while its arrival ring forms ({@link SimEvents.Type#JUMP_RING} at the ring: not hittable, no contact,
 * not on the screen for the EMP, the seekers re-pick: {@link ShotGeometry#onField}), the landing ({@link
 * SimEvents.Type#ARRIVE}), then a {@link Phase#GLIDE} straight on along its jump, its burst {@code fireAfterSeconds}
 * after the landing ({@link #trigger}). After its last jump's glide it glides out through the side edge downwind
 * ({@link Phase#OUT}; gone past the edge: it left).
 *
 * <p>The arrival, in the screen's frame: {@code jump} px away, {@code angleDegrees} off its frame's down toward the
 * drift's side (the jumps alternate sides with no drift: |θ| below {@link #CALM_DEGREES}); a candidate that breaks a
 * rule (within {@code minDistance} of the ship, in the {@link #BAND} px band at the ship's edge of the field: the
 * bottom, the top while the scroll runs reversed, within {@link #SIDE_MARGIN} px of a side or the far edge) is
 * shortened, then turned to the other side; failing all, the one farthest from the ship, kept inside the margins. While
 * it is gone the ring drifts with the current and a ring the ship comes near slides away from it at the unit's speed
 * (visibly); it lands only where the rules hold (checked again at the landing: it waits gone until they do). The jumps'
 * draws come from its serial, so a replay repeats them.
 *
 * <p>The rings on the screen are counted level-wide in its {@link Field} (a fade reserves its ring): a jump waits, still
 * gliding, while {@code mostRings} are taken. Each {@link Enemy} owns one, used and hashed only for a unit with a
 * teleport. Allocates nothing.
 *
 * <p>M11 part E, step E1 (design/enemies/air/choir-seraph, the cycle; {@link EnemySpec.Teleport.Cycle}): a cycling
 * unit may enter by its ring ({@link Phase#ENTER}: gone until a ring is free, then its first ring), folds before each
 * jump ({@link Phase#FOLD}, {@link SimEvents.Type#JUMP_FOLD}; the fold reserves its ring), may arrive at the ship's
 * mirrored point on its row ({@link PairRows}: a pair's rows and interleaved spiral arms from its wave's seed) and may
 * leave by a last jump after its leaving tell ({@link Phase#LEAVE_FOLD}, {@link SimEvents.Type#LEAVING_TELL}; gone at
 * that fade's end: it escaped). Its state beyond the built teleport's is hashed only for a unit with a cycle.
 */
public final class Teleporting {
    /** The band at the ship's edge of the field no ring lands in, px. */
    static final double BAND = 80;
    /** No ring lands within this of a side edge (nor of the far edge), px. */
    static final double SIDE_MARGIN = 24;
    /** Below this drift angle the jumps alternate sides, degrees. */
    static final double CALM_DEGREES = 5;

    /** Its jump's phases. */
    public enum Phase {
        /** Flying its path before its first jump (its first glide counting on the play field). */
        PATH,
        /** Fading out before a jump: holds still, hittable. */
        FADE,
        /** Gone while its ring forms: not hittable, no contact. */
        GONE,
        /** Gliding on along its last jump after a landing. */
        GLIDE,
        /** Its jumps done: gliding out through the side edge downwind. */
        OUT,
        /** M11 part E: a cycling unit that enters by its ring waits gone for a free ring (not on the screen). */
        ENTER,
        /** M11 part E: a cycling unit folds before a jump: holds still and silent, hittable (the jump's tell). */
        FOLD,
        /** M11 part E: its leaving tell before its last jump out: it rises, hittable, silent (an escape after). */
        LEAVE_FOLD
    }

    /**
     * The level's shared state of the teleporting units: the rings taken now and the current's drift and angle and the
     * scroll's direction for the jumps (set by the force each step). Allocated once per force.
     */
    static final class Field {
        int rings;
        /** The current's sideways drift now, px/s to the right (0 without one). */
        double drift;
        /** The current's angle now, degrees (0 without one). */
        double degrees;
        /** Whether the scroll runs reversed now (the band lies at the top). */
        boolean reversed;

        /** Counts the units of {@code enemies} that fade or are gone (the rings taken at the step's start). */
        void count(Pool<Enemy> enemies) {
            rings = 0;
            for (int i = 0; i < enemies.size(); i++) {
                Enemy unit = enemies.get(i);
                if (unit.spec().teleport().isPresent() && unit.teleporting.ringTaken()) {
                    rings++;
                }
            }
        }
    }

    private Field field;
    private Phase phase = Phase.PATH;
    /** Steps left of its phase (a path's first glide: steps flown on the play field so far). */
    private int ticks;

    private int jumped;
    /** The arrival ring's point, on the screen. */
    private double ringX;

    private double ringY;
    /** Its glide's direction on the screen (a unit vector) after a landing. */
    private double dirX;

    private double dirY;
    /** Steps until its burst after a landing; -1 for none due. */
    private int fireTicks = -1;
    /** The side its last jump went to: 1 right, -1 left. */
    private int side;
    /** M11 part E: whether its fade is its leaving jump's (no ring; gone for good at its end). */
    private boolean leaving;
    /** M11 part E: the steps its entry and jumps waited for a free ring (the ring cap starving it). */
    private int waited;
    /** M11 part E: its wave's seed for its rows and spiral arms ({@link PairRows}), set at its spawn. */
    private long seed;
    /** M11 part E: its place in its wave (a pair's 0 upper, 1 lower). */
    private int index;
    /** M11 part E: its wave's fixed rows (null: drawn per landing). Not hashed: from its wave's plan. */
    private WaveSpec.Rows rows;
    /**
     * M11 part G, step G1 (E2's finding on hard: two Seraph hulls touched for 0.3 s): whether its wave holds its pair's
     * other unit (an upper unit then follows its lower one's lift). Not hashed: from its wave's plan.
     */
    private boolean partnered;

    /** Back to a unit before its first jump (a new life). */
    void clear() {
        phase = Phase.PATH;
        ticks = 0;
        jumped = 0;
        ringX = 0;
        ringY = 0;
        dirX = 0;
        dirY = 0;
        fireTicks = -1;
        side = 0;
        leaving = false;
        waited = 0;
        seed = 0;
        index = 0;
        rows = null;
        partnered = false;
    }

    /**
     * M11 part E, step E1: its wave's place for a cycling unit's rows and spiral arms (at its spawn, after {@link
     * #clear}): its wave's index {@code wave} seeds the draws, so both units of a pair draw the same; {@code place} its
     * place in the wave; {@code fixed} the wave's fixed rows (null: drawn per landing).
     */
    void wave(int wave, int place, WaveSpec.Rows fixed, int count) {
        seed = PairRows.seed(wave);
        index = place;
        rows = fixed;
        partnered = place % 2 == 1 || place + 1 < count;
    }

    /**
     * M11 part G, step G1 (E2's finding: on hard the pair's hulls touched): a pair's upper unit's rows' gap at its last
     * landing, px (it keeps that much more off the ship than the lower one, so a lift of the lower row lifts it too); 0
     * for any other unit.
     */
    double pairGap() {
        if (!partnered || index % 2 != 0 || jumped == 0) {
            return 0;
        }
        return PairRows.row(seed, jumped - 1, index | 1, rows) - PairRows.row(seed, jumped - 1, index, rows);
    }

    /** M11 part G, step G1: a pair's lower unit's rows' gap at its last landing, px; 0 for any other unit. */
    double pairBelow() {
        if (!partnered || index % 2 == 0 || jumped == 0) {
            return 0;
        }
        return PairRows.row(seed, jumped - 1, index, rows) - PairRows.row(seed, jumped - 1, index - 1, rows);
    }

    /** Joins the level's shared {@code jumps} state (at its spawn). */
    void field(Field jumps) {
        field = jumps;
    }

    /**
     * Whether it is gone now: not hittable, no contact, not on the screen for anything ({@link ShotGeometry#onField});
     * M11 part E: also while it waits to enter by its ring.
     */
    public boolean gone() {
        return phase == Phase.GONE || phase == Phase.ENTER;
    }

    /** M11 part E: whether it folds before a jump now (hittable, holding still and silent: the jump's tell). */
    public boolean folding() {
        return phase == Phase.FOLD;
    }

    /** M11 part E: whether it shows its leaving tell now (its last fold before it jumps out for good). */
    public boolean leavingTell() {
        return phase == Phase.LEAVE_FOLD;
    }

    /** M11 part E: whether it fades out for good now (its leaving jump's fade, after its leaving tell). */
    public boolean leavingNow() {
        return phase == Phase.LEAVE_FOLD || (phase == Phase.FADE && leaving);
    }

    /**
     * M11 part E (m11e §5, the ring cap): the steps its entry and its jumps have waited for a free ring so far (0: the
     * cap never starved it; each such step stretches its cycle).
     */
    public int waited() {
        return waited;
    }

    /** M11 part E: its place in its wave (a pair's 0 upper, 1 lower). */
    public int place() {
        return index;
    }

    /** Whether it fades out now (hittable; the 0.2 s fade). */
    public boolean fading() {
        return phase == Phase.FADE;
    }

    /** Whether it holds a ring now (fading or gone; M11 part E: folding, not in its leaving jump's fade). */
    boolean ringTaken() {
        return (phase == Phase.FADE && !leaving) || phase == Phase.GONE || phase == Phase.FOLD;
    }

    /** Its phase. */
    public Phase phase() {
        return phase;
    }

    /** The arrival ring's point on the screen (while gone; its last landing's after). */
    public double ringX() {
        return ringX;
    }

    public double ringY() {
        return ringY;
    }

    /**
     * How far its fade or its ring has run, 0 to 1 (the fade's alpha, the shrinking ring); M11 part E: its fold or its
     * leaving tell too; 0 otherwise.
     */
    public double share(EnemySpec.Teleport spec) {
        if (phase == Phase.FADE) {
            int total = Math.max(1, SimStep.ticks(spec.fadeSeconds()));
            return 1 - (double) ticks / total;
        }
        if (phase == Phase.GONE) {
            int total = Math.max(
                    1, SimStep.ticks(firstRing(spec) ? spec.cycle().get().firstRingSeconds() : spec.ringSeconds()));
            return Math.min(1, 1 - (double) ticks / total);
        }
        if (phase == Phase.FOLD) {
            int total = Math.max(1, SimStep.ticks(spec.cycle().get().foldSeconds()));
            return Math.min(1, 1 - (double) ticks / total);
        }
        if (phase == Phase.LEAVE_FOLD) {
            int total = Math.max(1, SimStep.ticks(spec.cycle().get().leaveFoldSeconds()));
            return Math.min(1, 1 - (double) ticks / total);
        }
        return 0;
    }

    /** M11 part E: whether its ring now is its entry's first (a cycling unit entering by its ring, not landed yet). */
    private boolean firstRing(EnemySpec.Teleport spec) {
        return jumped == 0 && spec.cycle().isPresent() && spec.cycle().get().enterByRing();
    }

    /** The jumps it has made. */
    public int jumps() {
        return jumped;
    }

    /**
     * One step of {@code u}'s teleport with the ship at (shipX, shipY) on the screen, before its own flight: returns
     * {@code -1} while it flies its path (its own flight goes on), else 1 while it is in play and 0 once it is gone
     * past the edge.
     */
    int step(Enemy u, EnemySpec.Teleport spec, double shipX, double shipY) {
        // No Optional.map here: it would allocate in the step loop.
        EnemySpec.Teleport.Cycle cycle = spec.cycle().isPresent() ? spec.cycle().get() : null;
        switch (phase) {
            case PATH -> {
                if (cycle != null && cycle.enterByRing()) {
                    // M11 part E: it flies no path; it enters by its ring (once one is free).
                    phase = Phase.ENTER;
                    return enter(u, spec, cycle, shipX, shipY);
                }
                if (PlayField.overlaps(u.x, u.y(), u.box)) {
                    ticks++;
                }
                if (ticks < SimStep.ticks(spec.firstGlideSeconds()) || !jump(u, spec, cycle)) {
                    return -1;
                }
                return 1;
            }
            case ENTER -> {
                return enter(u, spec, cycle, shipX, shipY);
            }
            case FOLD -> {
                // M11 part E: it holds still and silent (its ring reserved), then fades.
                u.prevX = u.x;
                u.prevY = u.y;
                if (--ticks <= 0) {
                    fadeOut(u, spec);
                }
                return 1;
            }
            case LEAVE_FOLD -> {
                // M11 part E: its leaving tell: it rises up the screen, hittable, then fades out for good.
                u.prevX = u.x;
                u.prevY = u.y;
                int total = Math.max(1, SimStep.ticks(cycle.leaveFoldSeconds()));
                u.y = u.frame.y(u.y() + cycle.leaveRise() / total);
                if (--ticks <= 0) {
                    leaving = true;
                    fadeOut(u, spec);
                }
                return 1;
            }
            case FADE -> {
                u.prevX = u.x;
                u.prevY = u.y;
                if (--ticks <= 0) {
                    if (leaving) {
                        // M11 part E: its leaving jump's fade is over: gone for good (it escaped).
                        return 0;
                    }
                    ring(u, spec, shipX, shipY);
                }
                return 1;
            }
            case GONE -> {
                u.prevX = u.x;
                u.prevY = u.y;
                ringX += field.drift * SimStep.SECONDS;
                slide(u.speed, spec, shipX, shipY);
                if (--ticks <= 0 && fits(spec, ringX, ringY, shipX, shipY)) {
                    land(u, spec, cycle);
                }
                return 1;
            }
            case GLIDE -> {
                if (u.spec().shadow().isPresent()) {
                    // A mirroring unit mirrors the ship between its jumps instead of gliding on.
                    u.shadowing.follow(u, u.spec().shadow().get(), shipX, shipY);
                } else {
                    glide(u);
                }
                if (--ticks <= 0 && (jumped >= spec.landings() || !jump(u, spec, cycle))) {
                    if (jumped >= spec.landings()) {
                        if (cycle != null && cycle.exit() == EnemySpec.Teleport.Exit.JUMP) {
                            leaveFold(u, cycle);
                        } else {
                            out(u);
                        }
                    } else {
                        // The ring cap is full: it glides (mirrors) on and tries again next step.
                        ticks = 1;
                        waited++;
                    }
                }
                return PlayField.overlaps(u.x, u.y(), u.box) ? 1 : 0;
            }
            case OUT -> {
                glide(u);
                return PlayField.overlaps(u.x, u.y(), u.box) ? 1 : 0;
            }
        }
        return 1;
    }

    /**
     * M11 part B, step B1 (design/campaign Level 44, radio: the landing kill): whether it landed at most {@code steps}
     * steps ago and glides on from that landing.
     */
    boolean landedWithin(EnemySpec.Teleport spec, int steps) {
        return phase == Phase.GLIDE && Math.max(1, SimStep.ticks(spec.glideSeconds())) - ticks <= steps;
    }

    /**
     * Starts a jump if a ring is free: its fade, or (M11 part E) a cycling unit's fold before it; returns whether it
     * did.
     */
    private boolean jump(Enemy u, EnemySpec.Teleport spec, EnemySpec.Teleport.Cycle cycle) {
        if (!reserve(spec)) {
            return false;
        }
        if (cycle == null) {
            fadeOut(u, spec);
            return true;
        }
        phase = Phase.FOLD;
        ticks = Math.max(1, SimStep.ticks(cycle.foldSeconds()));
        hush(u);
        u.events.add(SimEvents.Type.JUMP_FOLD, u.x, u.y(), u.kind);
        return true;
    }

    /** Takes a ring if one is free (the level's cap); returns whether it did. */
    private boolean reserve(EnemySpec.Teleport spec) {
        if (field != null && field.rings >= spec.mostRings()) {
            return false;
        }
        if (field != null) {
            field.rings++;
        }
        return true;
    }

    /** It stops: no speed, no burst due (a fade, a fold, a leaving tell). */
    private void hush(Enemy u) {
        u.phase = Enemy.Phase.LEAVE;
        u.vx = 0;
        u.vy = 0;
        u.burstLeft = 0;
        fireTicks = -1;
    }

    /**
     * M11 part E: a cycling unit entering by its ring: gone until a ring is free, then its first ring forms where its
     * arrival rule puts it.
     */
    private int enter(Enemy u, EnemySpec.Teleport spec, EnemySpec.Teleport.Cycle cycle, double shipX, double shipY) {
        u.prevX = u.x;
        u.prevY = u.y;
        if (!reserve(spec)) {
            waited++;
            return 1;
        }
        hush(u);
        ring(u, spec, shipX, shipY);
        ticks = Math.max(1, SimStep.ticks(cycle.firstRingSeconds()));
        return 1;
    }

    /** M11 part E: its last landing's glide is over: its leaving tell, then its last jump out for good. */
    private void leaveFold(Enemy u, EnemySpec.Teleport.Cycle cycle) {
        phase = Phase.LEAVE_FOLD;
        ticks = Math.max(1, SimStep.ticks(cycle.leaveFoldSeconds()));
        hush(u);
        u.events.add(SimEvents.Type.LEAVING_TELL, u.x, u.y(), u.kind);
    }

    /** Its fade starts (its ring already reserved, or its leaving jump's: none). */
    private void fadeOut(Enemy u, EnemySpec.Teleport spec) {
        phase = Phase.FADE;
        ticks = Math.max(1, SimStep.ticks(spec.fadeSeconds()));
        hush(u);
        u.events.add(SimEvents.Type.JUMP_OUT, u.x, u.y(), u.kind);
    }

    /** The fade is over: it is gone and its ring forms at its arrival point. */
    private void ring(Enemy u, EnemySpec.Teleport spec, double shipX, double shipY) {
        if (spec.cycle().isPresent() && spec.cycle().get().arrive() == EnemySpec.Teleport.Arrival.MIRROR_ROW) {
            mirrorRing(u, shipX, shipY);
        } else {
            jumpRing(u, spec, shipX, shipY);
        }
        phase = Phase.GONE;
        ticks = Math.max(1, SimStep.ticks(spec.ringSeconds()));
        u.events.add(SimEvents.Type.JUMP_RING, ringX, ringY, u.kind);
    }

    /**
     * M11 part E (design/enemies/air/choir-seraph; m11e §4): its ring at the ship's mirrored point now, on its row for
     * the landing to come ({@link PairRows#row}), the row kept its mirror's row distance above the ship; inside the
     * margins and out of the band. The ring's slide and the landing's check hold the rest (as every ring's).
     */
    private void mirrorRing(Enemy u, double shipX, double shipY) {
        double margin = Math.max(SIDE_MARGIN, u.box.width() / 2);
        double minDy = u.spec().shadow().isPresent() ? u.spec().shadow().get().minDy() : 0;
        // M11 part G, step G1: when the lower row is lifted above the ship, the upper one is lifted as far (the rows
        // keep their gap, the hulls never touch).
        double lowerY = PlayField.HEIGHT - PairRows.row(seed, jumped, index | 1, rows);
        double lift = partnered && index % 2 == 0 ? Math.max(0, shipY + minDy - lowerY) : 0;
        double y = Math.max(PlayField.HEIGHT - PairRows.row(seed, jumped, index, rows) + lift, shipY + minDy);
        ringX = Math.clamp(PlayField.WIDTH - shipX, margin, PlayField.WIDTH - margin);
        double high = Math.min(bandHigh(), PlayField.HEIGHT - u.box.height() / 2);
        if (partnered && index % 2 == 1) {
            // M11 part G, step G1: the lower one stays its rows' gap under the upper one's highest.
            high -= PairRows.row(seed, jumped, index, rows) - PairRows.row(seed, jumped, index - 1, rows);
        }
        ringY = Math.clamp(y, Math.min(bandLow(), high), high);
    }

    /** Its ring {@code jump} px away along its jump's angle, by the fairness rules (the built teleport's). */
    private void jumpRing(Enemy u, EnemySpec.Teleport spec, double shipX, double shipY) {
        double x = u.x;
        double y = u.y();
        double drift = field == null ? 0 : field.drift;
        double degrees = field == null ? 0 : field.degrees;
        int towards;
        if (Math.abs(degrees) >= CALM_DEGREES && drift != 0) {
            towards = drift > 0 ? 1 : -1;
        } else if (side == 0) {
            towards = Ramming.draw(0x3C6EF372FE94F82BL ^ u.serial) < 0.5 ? -1 : 1;
        } else {
            towards = -side;
        }
        long seed = 0xA54FF53A5F1D36F1L ^ ((long) u.serial << 8) ^ jumped;
        double distance = spec.jump().at(Ramming.draw(seed));
        double angle = StrictMath.toRadians(spec.angleDegrees().at(Ramming.draw(seed ^ 0x510E527FADE682D1L)));
        double down = u.frame.vy(-1);
        double bestX = Double.NaN;
        double bestY = 0;
        double bestFar = -1;
        boolean found = false;
        for (int turn = 0; turn < 2 && !found; turn++) {
            int s = turn == 0 ? towards : -towards;
            for (int k = 0; k < 3 && !found; k++) {
                double d = distance * (1 - 0.25 * k);
                double cx = x + s * d * Trig.sin(angle);
                double cy = y + down * d * Trig.cos(angle);
                if (fits(spec, cx, cy, shipX, shipY)) {
                    ringX = cx;
                    ringY = cy;
                    side = s;
                    found = true;
                } else {
                    double kx = Math.clamp(cx, SIDE_MARGIN, PlayField.WIDTH - SIDE_MARGIN);
                    double ky = Math.clamp(cy, bandLow(), bandHigh());
                    double far = (kx - shipX) * (kx - shipX) + (ky - shipY) * (ky - shipY);
                    if (far > bestFar) {
                        bestFar = far;
                        bestX = kx;
                        bestY = ky;
                        side = s;
                    }
                }
            }
        }
        if (!found) {
            ringX = bestX;
            ringY = bestY;
        }
    }

    /** The lowest arrival height on the screen: above the band at the bottom (none there while reversed). */
    private double bandLow() {
        return field != null && field.reversed ? SIDE_MARGIN : BAND;
    }

    /** The highest arrival height on the screen: below the band at the top while reversed, else the far margin. */
    private double bandHigh() {
        return PlayField.HEIGHT - (field != null && field.reversed ? BAND : SIDE_MARGIN);
    }

    /** Whether an arrival at (x, y) on the screen keeps every rule with the ship at (shipX, shipY). */
    boolean fits(EnemySpec.Teleport spec, double x, double y, double shipX, double shipY) {
        double dx = x - shipX;
        double dy = y - shipY;
        return x >= SIDE_MARGIN
                && x <= PlayField.WIDTH - SIDE_MARGIN
                && y >= bandLow()
                && y <= bandHigh()
                && dx * dx + dy * dy >= spec.minDistance() * spec.minDistance();
    }

    /** The ring slides away from a ship that came within its minimum distance, at {@code speed}, inside the margins. */
    private void slide(double speed, EnemySpec.Teleport spec, double shipX, double shipY) {
        double dx = ringX - shipX;
        double dy = ringY - shipY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double step = speed * SimStep.SECONDS;
        if (distance < spec.minDistance()) {
            if (distance == 0) {
                dx = 0;
                dy = 1;
                distance = 1;
            }
            ringX += dx / distance * step;
            ringY += dy / distance * step;
        }
        ringX = Math.clamp(ringX, SIDE_MARGIN, PlayField.WIDTH - SIDE_MARGIN);
        ringY = Math.clamp(ringY, bandLow(), bandHigh());
    }

    /**
     * It lands at its ring and glides on along its jump; its burst comes its fire delay later. M11 part E: a cycling
     * unit with a mirror starts its spiral's arms afresh at each landing ({@link PairRows#arm}: a pair's interleaved).
     */
    private void land(Enemy u, EnemySpec.Teleport spec, EnemySpec.Teleport.Cycle cycle) {
        if (cycle != null && u.spec().shadow().isPresent()) {
            u.shadowing.arm(PairRows.arm(seed, jumped, index));
        }
        double fromX = u.x;
        double fromY = u.y();
        double dx = ringX - fromX;
        double dy = ringY - fromY;
        double length = Math.sqrt(dx * dx + dy * dy);
        dirX = length > 0 ? dx / length : 0;
        dirY = length > 0 ? dy / length : u.frame.vy(-1);
        u.x = ringX;
        u.y = u.frame.y(ringY);
        u.prevX = u.x;
        u.prevY = u.y;
        u.offX = 0;
        u.offY = 0;
        jumped++;
        phase = Phase.GLIDE;
        ticks = Math.max(1, SimStep.ticks(spec.glideSeconds()));
        fireTicks = SimStep.ticks(spec.fireAfterSeconds());
        u.facing = Enemy.heading(dirX, u.frame.vy(dirY));
        u.events.add(SimEvents.Type.ARRIVE, ringX, ringY, u.kind);
    }

    /** One step straight on along its glide's direction at its speed. */
    private void glide(Enemy u) {
        u.prevX = u.x;
        u.prevY = u.y;
        u.x += dirX * u.speed * SimStep.SECONDS;
        u.y += u.frame.vy(dirY) * u.speed * SimStep.SECONDS;
    }

    /** Its jumps done: it glides out sideways through the edge downwind (the nearer side edge with no drift). */
    private void out(Enemy u) {
        double drift = field == null ? 0 : field.drift;
        double degrees = field == null ? 0 : field.degrees;
        int to;
        if (Math.abs(degrees) >= CALM_DEGREES && drift != 0) {
            to = drift > 0 ? 1 : -1;
        } else {
            to = u.x < PlayField.WIDTH / 2.0 ? -1 : 1;
        }
        phase = Phase.OUT;
        dirX = to;
        dirY = 0;
        u.facing = Enemy.heading(dirX, 0);
    }

    /**
     * Its gun's trigger ({@link EnemyGunnery#trigger}): a burst only once its fire delay after a landing has run (its
     * shots its burst gap apart), never else; nothing while it fades or is gone.
     */
    boolean trigger(Enemy u, EnemyGun gun) {
        if (phase != Phase.GLIDE && phase != Phase.OUT) {
            return false;
        }
        if (u.burstLeft > 0) {
            if (--u.burstTicks > 0) {
                return false;
            }
            u.burstLeft--;
            u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
            return true;
        }
        if (fireTicks < 0 || fireTicks-- > 0) {
            return false;
        }
        fireTicks = -1;
        u.burstLeft = gun.burst() - 1;
        u.burstTicks = SimStep.ticks(gun.burstGapSeconds());
        return true;
    }

    void addTo(StateHash hash) {
        hash.add(phase.ordinal())
                .add(ticks)
                .add(jumped)
                .add(ringX)
                .add(ringY)
                .add(dirX)
                .add(dirY)
                .add(fireTicks)
                .add(side);
    }

    /** M11 part E: its cycle's state (hashed only for a unit with a cycle, so every earlier teleport hashes as before). */
    void addCycleTo(StateHash hash) {
        hash.add(leaving ? 1 : 0).add(waited).add(seed).add(index);
    }
}
