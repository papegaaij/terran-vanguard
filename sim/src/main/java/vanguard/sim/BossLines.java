package vanguard.sim;

import java.util.Arrays;

/**
 * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, Fire rules; design/enemies/ground/rail-bunker): a boss's
 * rail lines, A1's {@link LineBeam} geometry fired from its parts ({@link BossSpec.Pattern#LINE}). A line attack's
 * volley does not fire at once: it makes its part's line <b>due</b> ({@link #request}); each step the boss starts the
 * oldest due line (the lower part first on a tie) when no line telegraphs, at least {@link #GAP_SECONDS} after the last
 * telegraph's start, and the ship is farther than the line's hold distance from the part (else that line waits). A due
 * line lapses when its part dies or may no longer fire (a battery out of its fire arc). The line <b>locks</b> its
 * direction (through the ship) at its telegraph's start, so a battery turns on under it; its origin (the part's centre)
 * rides with its part as the ring turns (round 68's row 9, on the recommendation of round 66's C.1 b, pending round 68:
 * a dead part's line keeps its last origin); round 69's B.1 (the user): the direction is led ({@link LineAim}), from
 * where the part will be when the line goes live (its turn on the ring over the telegraph) through the ship, so the
 * riding line passes through the locked ship point at its live moment; after
 * the telegraph it is live for its time, a band that deals its damage to the ship once. It hurts nothing else (enemy
 * fire never hurts enemies; Rook is never its target). A part killed in the telegraph cancels it; the EMP Burst does
 * nothing (a boss is never stunned). M10 part F, step F1b: a line with a {@link EnemySpec.LaserLine#lockBeforeSeconds}
 * shorter than its telegraph tracks the ship (led as at its start) until that long before it fires, then locks.
 *
 * <p>Kept by its {@link SetPiece}; made only for a boss with a line attack, so every other boss hashes as before. Its
 * events go through the {@link BossActions}. Allocates nothing once made.
 */
public final class BossLines {
    /** The fewest seconds between two of a boss's line telegraphs' starts (the station staggers them). */
    public static final double GAP_SECONDS = 1.5;

    private final SetPiece piece;
    private final BossSpec boss;
    private final int gapTicks;
    /** Per part, the step its line came due (on {@link #clock}); -1 for none due. */
    private final int[] dueTicks;
    /** Per part, the line attack that made it due. */
    private final int[] dueAttack;

    /** Steps since the level start (the due lines' order). */
    private int clock;
    /** Steps since the last telegraph started. */
    private int sinceStart;
    /** Steps into the running line's telegraph and live time; -1 while none runs. */
    private int ticks;

    private int part;
    private int attack;
    private double fromX;
    private double fromY;
    private final LineAim aim = new LineAim();
    /** The origin's move over the last step, px (the autopilot's look-ahead); 0 while none runs. */
    private double stepX;

    private double stepY;
    private boolean hit;

    /**
     * M10 round 80 fixes, R80a (design/enemies/bosses/ascendant, the phase-1 playbook): whether the boss has a playbook
     * with a lance sweep or twin lances; only then are the sweep's and the twin's state hashed (every other boss hashes
     * as before).
     */
    private final boolean extended;
    /** R80a: the running line's origin off its part's centre, px to the right (a twin's flank; 0 else). */
    private double offX;
    /** R80a: the running line's turn each live step (the lance sweep), as its cosine and sine; 0 radians for none. */
    private double sweepRadians;

    private double sweepCos = 1;
    private double sweepSin;
    /** R80a: the next started line's sweep, radians (counter-clockwise, y up), its first flank and its twin's delay. */
    private double nextSweep;

    private double nextFlank;
    private int nextApart;
    /** R80a: the twin lance: steps to its telegraph's start (-1: none waiting), then its steps run (-1: none runs). */
    private int twinDelay = -1;

    private int twinTicks = -1;
    private int twinAttack = -1;
    private double twinOffX;
    private double twinFromX;
    private double twinFromY;
    private final LineAim twinAim = new LineAim();
    private double twinStepX;
    private double twinStepY;
    private boolean twinHit;

    BossLines(SetPiece piece, BossSpec boss, int parts) {
        this.piece = piece;
        this.boss = boss;
        gapTicks = SimStep.ticks(GAP_SECONDS);
        dueTicks = new int[parts];
        dueAttack = new int[parts];
        extended = boss.playbooks().stream()
                .flatMap(book -> book.moves().stream())
                .anyMatch(move -> move.kind() == PhasePlaybook.Kind.SWEEP || move.kind() == PhasePlaybook.Kind.TWIN);
        reset();
    }

    /** Whether {@code boss} has a line attack (then its set piece keeps a {@link BossLines}). */
    static boolean any(BossSpec boss) {
        return boss != null && boss.attacks().stream().anyMatch(a -> a.pattern() == BossSpec.Pattern.LINE);
    }

    /** Back to the level start (the boss checkpoint, the kill): no line due or running. */
    void reset() {
        Arrays.fill(dueTicks, -1);
        Arrays.fill(dueAttack, -1);
        clock = 0;
        sinceStart = Integer.MAX_VALUE / 2;
        ticks = -1;
        part = -1;
        attack = -1;
        fromX = 0;
        fromY = 0;
        aim.clear();
        stepX = 0;
        stepY = 0;
        hit = false;
        offX = 0;
        sweepRadians = 0;
        sweepCos = 1;
        sweepSin = 0;
        nextSweep = 0;
        nextFlank = 0;
        nextApart = 0;
        twinDelay = -1;
        twinTicks = -1;
        twinAttack = -1;
        twinOffX = 0;
        twinFromX = 0;
        twinFromY = 0;
        twinAim.clear();
        twinStepX = 0;
        twinStepY = 0;
        twinHit = false;
    }

    /**
     * M10 part I, step I1: its held piece's EMP stun: a running line (telegraph or live) is cancelled without a shot and
     * no line stays due (the stun's end lets them come due again on their cycles).
     */
    void cancel() {
        Arrays.fill(dueTicks, -1);
        Arrays.fill(dueAttack, -1);
        ticks = -1;
        stepX = 0;
        stepY = 0;
        if (extended) {
            twinDelay = -1;
            twinTicks = -1;
            twinStepX = 0;
            twinStepY = 0;
            nextSweep = 0;
            nextFlank = 0;
        }
    }

    /** Line attack {@code a}'s volley from part {@code p}: its line is due (unless one is due there already). */
    void request(int a, int p) {
        if (dueTicks[p] < 0) {
            dueTicks[p] = clock;
            dueAttack[p] = a;
        }
    }

    /**
     * M10 round 80 fixes, R80a (the lance sweep): as {@link #request}, the line then turning {@code radians}
     * (counter-clockwise, y up) evenly over its live time once it fires.
     */
    void requestSweep(int a, int p, double radians) {
        request(a, p);
        nextSweep = radians;
        nextFlank = 0;
    }

    /**
     * M10 round 80 fixes, R80a (twin lances): as {@link #request}, but from {@code flank} px beside the part's centre (to
     * the right; negative: the left), and a second line of the same attack from the other flank {@code apartTicks}
     * steps after it, its own telegraph re-aimed at the ship then (it tracks to its lock as the first).
     */
    void requestTwin(int a, int p, double flank, int apartTicks) {
        request(a, p);
        nextSweep = 0;
        nextFlank = flank;
        nextApart = Math.max(1, apartTicks);
    }

    /**
     * One step with the ship at (shipX, shipY): the running line goes on (live after its telegraph, over after its live
     * time; cancelled in its telegraph when its part died); due lines lapse whose part may no longer fire ({@code
     * ring}'s arc, null for none); then the oldest due line starts if the gate allows it.
     */
    void step(double shipX, double shipY, RingArena ring, BossActions actions) {
        clock++;
        if (sinceStart < Integer.MAX_VALUE / 2) {
            sinceStart++;
        }
        if (ticks >= 0) {
            EnemySpec.LaserLine line = line(attack);
            int telegraph = SimStep.ticks(line.telegraphSeconds());
            ticks++;
            ride();
            if (ticks < telegraph && piece.partHp(part) <= 0) {
                ticks = -1;
                twinDelay = -1;
            } else if (line.tracks() && ticks < telegraph - SimStep.ticks(line.lockBeforeSeconds())) {
                // M10 part F, step F1b (the Honour Guard's sniper line): it tracks the ship, led for its part's move
                // over the telegraph's rest, until its lock.
                int left = telegraph - ticks;
                aim.lock(piece.partLeadX(part, left) + offX, piece.partLeadY(part, left), fromX, fromY, shipX, shipY);
            } else if (ticks == telegraph) {
                actions.ring(SimEvents.Type.BOSS_LINE_FIRED, fromX, fromY, part);
            } else if (ticks >= telegraph + Math.max(1, SimStep.ticks(line.liveSeconds()))) {
                ticks = -1;
            } else if (sweepRadians != 0 && ticks > telegraph) {
                // R80a: the lance sweep turns its beam on over its live time.
                aim.rotate(sweepCos, sweepSin);
            }
        }
        if (extended) {
            twin(shipX, shipY, actions);
        }
        int next = -1;
        for (int p = 0; p < dueTicks.length; p++) {
            if (dueTicks[p] < 0) {
                continue;
            }
            if (piece.partHp(p) <= 0 || (ring != null && !ring.fires(p))) {
                dueTicks[p] = -1;
                continue;
            }
            if (next < 0 || dueTicks[p] < dueTicks[next]) {
                double dx = shipX - piece.partX(p);
                double dy = shipY - piece.partY(p);
                double hold = line(dueAttack[p]).holdWithinPx();
                if (dx * dx + dy * dy > hold * hold && (dx != 0 || dy != 0)) {
                    next = p;
                }
            }
        }
        if (next < 0 || telegraphing() || sinceStart < gapTicks || twinBusy()) {
            return;
        }
        start(next, shipX, shipY, actions);
    }

    /** The running line's origin onto its living part's centre now (the ring turned it); the direction stays. */
    private void ride() {
        stepX = 0;
        stepY = 0;
        if (piece.partHp(part) > 0) {
            stepX = piece.partX(part) + offX - fromX;
            stepY = piece.partY(part) - fromY;
            fromX += stepX;
            fromY += stepY;
        }
    }

    private void start(int p, double shipX, double shipY, BossActions actions) {
        part = p;
        attack = dueAttack[p];
        offX = extended ? nextFlank : 0;
        fromX = piece.partX(p) + offX;
        fromY = piece.partY(p);
        // Round 69's B.1: the part rides on over the telegraph (a battery on its turning ring): aim from where it
        // fires.
        int telegraph = SimStep.ticks(line(attack).telegraphSeconds());
        aim.lock(piece.partLeadX(p, telegraph) + offX, piece.partLeadY(p, telegraph), fromX, fromY, shipX, shipY);
        stepX = 0;
        stepY = 0;
        hit = false;
        ticks = 0;
        sinceStart = 0;
        dueTicks[p] = -1;
        dueAttack[p] = -1;
        if (extended) {
            // R80a: the sweep's turn per live step; the twin's second line waits its delay.
            int live = Math.max(1, SimStep.ticks(line(attack).liveSeconds()));
            sweepRadians = nextSweep;
            sweepCos = Trig.cos(nextSweep / live);
            sweepSin = Trig.sin(nextSweep / live);
            if (nextFlank != 0) {
                twinDelay = nextApart;
                twinAttack = attack;
                twinOffX = -nextFlank;
            }
            nextSweep = 0;
            nextFlank = 0;
        }
        actions.ring(SimEvents.Type.BOSS_LINE_TELEGRAPH, fromX, fromY, p);
    }

    /** R80a: the twin lance waits its delay, then telegraphs (tracking to its lock), fires and runs out. */
    private void twin(double shipX, double shipY, BossActions actions) {
        if (twinDelay > 0 && --twinDelay == 0) {
            twinDelay = -1;
            if (piece.partHp(part) > 0) {
                twinTicks = 0;
                twinFromX = piece.partX(part) + twinOffX;
                twinFromY = piece.partY(part);
                int telegraph = SimStep.ticks(line(twinAttack).telegraphSeconds());
                twinAim.lock(
                        piece.partLeadX(part, telegraph) + twinOffX,
                        piece.partLeadY(part, telegraph),
                        twinFromX,
                        twinFromY,
                        shipX,
                        shipY);
                twinStepX = 0;
                twinStepY = 0;
                twinHit = false;
                actions.ring(SimEvents.Type.BOSS_LINE_TELEGRAPH, twinFromX, twinFromY, part);
            }
            return;
        }
        if (twinTicks < 0) {
            return;
        }
        EnemySpec.LaserLine line = line(twinAttack);
        int telegraph = SimStep.ticks(line.telegraphSeconds());
        twinTicks++;
        twinStepX = 0;
        twinStepY = 0;
        if (piece.partHp(part) > 0) {
            twinStepX = piece.partX(part) + twinOffX - twinFromX;
            twinStepY = piece.partY(part) - twinFromY;
            twinFromX += twinStepX;
            twinFromY += twinStepY;
        }
        if (twinTicks < telegraph && piece.partHp(part) <= 0) {
            twinTicks = -1;
        } else if (line.tracks() && twinTicks < telegraph - SimStep.ticks(line.lockBeforeSeconds())) {
            int left = telegraph - twinTicks;
            twinAim.lock(
                    piece.partLeadX(part, left) + twinOffX,
                    piece.partLeadY(part, left),
                    twinFromX,
                    twinFromY,
                    shipX,
                    shipY);
        } else if (twinTicks == telegraph) {
            actions.ring(SimEvents.Type.BOSS_LINE_FIRED, twinFromX, twinFromY, part);
        } else if (twinTicks >= telegraph + Math.max(1, SimStep.ticks(line.liveSeconds()))) {
            twinTicks = -1;
        }
    }

    private EnemySpec.LaserLine line(int a) {
        return boss.attacks().get(a).line().orElseThrow();
    }

    /** Whether the line is live and has not struck yet, and {@code hull} at (x, y) touches its band. */
    boolean strikes(Hull hull, double x, double y) {
        return live()
                && !hit
                && hull.touchesSegment(
                        x,
                        y,
                        fromX,
                        fromY,
                        fromX + aim.dirX() * LineBeam.LENGTH,
                        fromY + aim.dirY() * LineBeam.LENGTH,
                        line(attack).bandPx() / 2);
    }

    /** The line struck the ship: once per line. */
    void markHit() {
        hit = true;
    }

    /** R80a: whether the twin lance is live and has not struck yet, and {@code hull} at (x, y) touches its band. */
    boolean twinStrikes(Hull hull, double x, double y) {
        return twinLive()
                && !twinHit
                && hull.touchesSegment(
                        x,
                        y,
                        twinFromX,
                        twinFromY,
                        twinFromX + twinAim.dirX() * LineBeam.LENGTH,
                        twinFromY + twinAim.dirY() * LineBeam.LENGTH,
                        line(twinAttack).bandPx() / 2);
    }

    /** R80a: the twin lance struck the ship: once. */
    void markTwinHit() {
        twinHit = true;
    }

    /** R80a: the twin lance's damage. */
    double twinDamage() {
        return line(twinAttack).damage();
    }

    /** The running line's damage. */
    double damage() {
        return line(attack).damage();
    }

    /** Whether a telegraph or a live line runs now. */
    public boolean running() {
        return ticks >= 0;
    }

    /** Whether the line is in its telegraph now (the thin red line, brightening). */
    public boolean telegraphing() {
        return ticks >= 0 && ticks < SimStep.ticks(line(attack).telegraphSeconds());
    }

    /** Whether the line is live now (the rail's flash). */
    public boolean live() {
        return ticks >= 0 && !telegraphing();
    }

    /** How far the telegraph has run, 0 to 1 (its brightening); 1 once live, 0 while none runs. */
    public double charge() {
        if (ticks < 0) {
            return 0;
        }
        int telegraph = SimStep.ticks(line(attack).telegraphSeconds());
        return live() ? 1 : Math.min(1, (double) ticks / Math.max(1, telegraph));
    }

    /** Whether part {@code p}'s line is due (waiting for the gate). */
    public boolean due(int p) {
        return dueTicks[p] >= 0;
    }

    /** The running line's part; -1 while none runs. */
    public int part() {
        return ticks >= 0 ? part : -1;
    }

    /** The running line's attack; -1 while none runs. */
    public int attack() {
        return ticks >= 0 ? attack : -1;
    }

    /** The running line's band width, px; 0 while none runs. */
    public double bandPx() {
        return ticks >= 0 ? line(attack).bandPx() : 0;
    }

    /** The running line's origin, px from the left: its part's centre (it rides with the part). */
    public double fromX() {
        return fromX;
    }

    /** The running line's origin, px up. */
    public double fromY() {
        return fromY;
    }

    /** How far the origin moved over the last step, px to the right (its part on the turning ring). */
    public double stepX() {
        return stepX;
    }

    /** How far the origin moved over the last step, px up. */
    public double stepY() {
        return stepY;
    }

    /**
     * The locked line's direction (a unit vector, y up the screen), led for its part's move over the telegraph (round
     * 69's B.1); it runs to the screen's edge.
     */
    public double dirX() {
        return aim.dirX();
    }

    public double dirY() {
        return aim.dirY();
    }

    /** R80a: whether a twin lance waits for its telegraph or runs. */
    public boolean twinBusy() {
        return twinDelay > 0 || twinTicks >= 0;
    }

    /** R80a: whether the twin lance runs (its telegraph or its beam). */
    public boolean twinRunning() {
        return twinTicks >= 0;
    }

    /** R80a: whether the twin lance telegraphs now. */
    public boolean twinTelegraphing() {
        return twinTicks >= 0 && twinTicks < SimStep.ticks(line(twinAttack).telegraphSeconds());
    }

    /** R80a: whether the twin lance is live now. */
    public boolean twinLive() {
        return twinTicks >= 0 && !twinTelegraphing();
    }

    /** R80a: how far the twin's telegraph has run, 0 to 1 (1 once live, 0 while none runs). */
    public double twinCharge() {
        if (twinTicks < 0) {
            return 0;
        }
        int telegraph = SimStep.ticks(line(twinAttack).telegraphSeconds());
        return twinLive() ? 1 : Math.min(1, (double) twinTicks / Math.max(1, telegraph));
    }

    /** R80a: the twin lance's band width, px; 0 while none runs. */
    public double twinBandPx() {
        return twinTicks >= 0 ? line(twinAttack).bandPx() : 0;
    }

    /** R80a: the twin lance's attack; -1 while none runs. */
    public int twinAttack() {
        return twinTicks >= 0 ? twinAttack : -1;
    }

    /** R80a: the twin lance's origin, px from the left, and its move over the last step. */
    public double twinFromX() {
        return twinFromX;
    }

    public double twinFromY() {
        return twinFromY;
    }

    public double twinStepX() {
        return twinStepX;
    }

    public double twinStepY() {
        return twinStepY;
    }

    /** R80a: the twin lance's direction, a unit vector (y up). */
    public double twinDirX() {
        return twinAim.dirX();
    }

    public double twinDirY() {
        return twinAim.dirY();
    }

    /** R80a: the running line's whole sweep, radians (counter-clockwise, y up; 0: it does not sweep). */
    public double sweep() {
        return ticks >= 0 ? sweepRadians : 0;
    }

    /**
     * R80a: the running line's direction {@code seconds} ahead (the autopilot's look-ahead): turned on by its sweep
     * over the part of its live time by then.
     */
    public double dirXAhead(double seconds) {
        return aheadTurn(seconds, true);
    }

    public double dirYAhead(double seconds) {
        return aheadTurn(seconds, false);
    }

    private double aheadTurn(double seconds, boolean wantX) {
        if (ticks < 0 || sweepRadians == 0) {
            return wantX ? aim.dirX() : aim.dirY();
        }
        EnemySpec.LaserLine line = line(attack);
        int telegraph = SimStep.ticks(line.telegraphSeconds());
        int live = Math.max(1, SimStep.ticks(line.liveSeconds()));
        int at = ticks + (int) Math.round(seconds / SimStep.SECONDS);
        int turned = Math.clamp(at - telegraph, 0, live) - Math.clamp(ticks - telegraph, 0, live);
        double angle = sweepRadians / live * turned;
        double c = Trig.cos(angle);
        double s = Trig.sin(angle);
        return wantX ? aim.dirX() * c - aim.dirY() * s : aim.dirX() * s + aim.dirY() * c;
    }

    void addTo(StateHash hash) {
        hash.add(clock)
                .add(sinceStart)
                .add(ticks)
                .add(part)
                .add(attack)
                .add(fromX)
                .add(fromY)
                .add(aim.dirX())
                .add(aim.dirY())
                .add(stepX)
                .add(stepY)
                .add(hit ? 1 : 0);
        for (int p = 0; p < dueTicks.length; p++) {
            hash.add(dueTicks[p]).add(dueAttack[p]);
        }
        if (extended) {
            hash.add(offX)
                    .add(sweepRadians)
                    .add(nextSweep)
                    .add(nextFlank)
                    .add(nextApart)
                    .add(twinDelay)
                    .add(twinTicks)
                    .add(twinAttack)
                    .add(twinOffX)
                    .add(twinFromX)
                    .add(twinFromY)
                    .add(twinAim.dirX())
                    .add(twinAim.dirY())
                    .add(twinHit ? 1 : 0);
        }
    }
}
