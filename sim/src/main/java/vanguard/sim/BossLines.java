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
 * nothing (a boss is never stunned).
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

    BossLines(SetPiece piece, BossSpec boss, int parts) {
        this.piece = piece;
        this.boss = boss;
        gapTicks = SimStep.ticks(GAP_SECONDS);
        dueTicks = new int[parts];
        dueAttack = new int[parts];
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
    }

    /** Line attack {@code a}'s volley from part {@code p}: its line is due (unless one is due there already). */
    void request(int a, int p) {
        if (dueTicks[p] < 0) {
            dueTicks[p] = clock;
            dueAttack[p] = a;
        }
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
            } else if (ticks == telegraph) {
                actions.ring(SimEvents.Type.BOSS_LINE_FIRED, fromX, fromY, part);
            } else if (ticks >= telegraph + Math.max(1, SimStep.ticks(line.liveSeconds()))) {
                ticks = -1;
            }
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
        if (next < 0 || telegraphing() || sinceStart < gapTicks) {
            return;
        }
        start(next, shipX, shipY, actions);
    }

    /** The running line's origin onto its living part's centre now (the ring turned it); the direction stays. */
    private void ride() {
        stepX = 0;
        stepY = 0;
        if (piece.partHp(part) > 0) {
            stepX = piece.partX(part) - fromX;
            stepY = piece.partY(part) - fromY;
            fromX += stepX;
            fromY += stepY;
        }
    }

    private void start(int p, double shipX, double shipY, BossActions actions) {
        part = p;
        attack = dueAttack[p];
        fromX = piece.partX(p);
        fromY = piece.partY(p);
        // Round 69's B.1: the part rides on over the telegraph (a battery on its turning ring): aim from where it
        // fires.
        int telegraph = SimStep.ticks(line(attack).telegraphSeconds());
        aim.lock(piece.partLeadX(p, telegraph), piece.partLeadY(p, telegraph), fromX, fromY, shipX, shipY);
        stepX = 0;
        stepY = 0;
        hit = false;
        ticks = 0;
        sinceStart = 0;
        dueTicks[p] = -1;
        dueAttack[p] = -1;
        actions.ring(SimEvents.Type.BOSS_LINE_TELEGRAPH, fromX, fromY, p);
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
    }
}
