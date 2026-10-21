package vanguard.sim;

import java.util.Arrays;

/**
 * M5 part H: a boss's sweep attacks (design/enemies, Boss attacks and root bursts; decision H5 = a), stepped by its
 * {@link SetPiece} in the style of the {@link BreachArena}: per attack its sweep's telegraph and beam, the part it
 * sweeps from, its centre (radians clockwise from straight down), its direction (1 clockwise, -1 the other way; the
 * sweeps alternate it) and what its current one hit already ({@link SetPiece#SWEPT_SHIP}, {@link
 * SetPiece#SWEPT_WINGMAN}). Its state is hashed only for a boss with a sweep, so Level 11's Kraken hashes as before.
 */
final class BossSweeps {
    private final SetPiece piece;
    /** The boss; null for a set piece flying passes (it then has no attacks). */
    private final BossSpec boss;
    /** Whether any of its attacks sweeps. */
    private final boolean any;
    /** Per attack, steps into its telegraph and beam; -1 while none runs. */
    private final int[] sweepTicks;

    private final int[] sweepPart;
    private final double[] sweepCentre;
    private final int[] sweepDirection;
    private final int[] sweepsStarted;
    private final int[] sweepHits;
    /** The ship's position in the boss's current step, where a sweep's centre aims as it starts. */
    private double shipAtX;

    private double shipAtY;

    BossSweeps(SetPiece piece, BossSpec boss) {
        this.piece = piece;
        this.boss = boss;
        int attacks = boss == null ? 0 : boss.attacks().size();
        boolean sweeping = false;
        for (int a = 0; a < attacks; a++) {
            sweeping |= boss.attacks().get(a).pattern() == BossSpec.Pattern.SWEEP;
        }
        any = sweeping;
        sweepTicks = new int[attacks];
        sweepPart = new int[attacks];
        sweepCentre = new double[attacks];
        sweepDirection = new int[attacks];
        sweepsStarted = new int[attacks];
        sweepHits = new int[attacks];
    }

    /** Whether any of its attacks sweeps (the sweeps step and hash only then). */
    boolean any() {
        return any;
    }

    /** Back to the level start: no sweep run or started. */
    void reset() {
        Arrays.fill(sweepTicks, -1);
        Arrays.fill(sweepPart, -1);
        Arrays.fill(sweepCentre, 0);
        Arrays.fill(sweepDirection, 1);
        Arrays.fill(sweepsStarted, 0);
        Arrays.fill(sweepHits, 0);
    }

    /** A sweep under way ends with its phase. */
    void endAll() {
        Arrays.fill(sweepTicks, -1);
    }

    /** The ship is at (shipX, shipY) in this step. */
    void aimAt(double shipX, double shipY) {
        shipAtX = shipX;
        shipAtY = shipY;
    }

    /**
     * Attack {@code a}'s sweep starts from part {@code p} (unless one runs): its centre on the ship's bearing from the
     * beam's origin now, its direction the other way from its last one's; its telegraph shows first.
     */
    void start(int a, int p, BossSpec.Attack attack, SetPiece.BossActions actions) {
        if (sweepTicks[a] >= 0) {
            return;
        }
        EnemySpec.Sweep sweep = attack.sweep().orElseThrow();
        double ox = piece.partX(p);
        double oy = piece.partY(p) - sweep.originDown();
        sweepPart[a] = p;
        sweepCentre[a] = StrictMath.atan2(-(shipAtX - ox), -(shipAtY - oy));
        sweepDirection[a] = sweepsStarted[a] % 2 == 0 ? 1 : -1;
        if (sweep.leads()) {
            // Round 41: the first edge lies its lead from the ship's bearing, on the side the beam comes from.
            sweepCentre[a] += sweepDirection[a] * (sweep.arcRadians() / 2 - sweep.leadRadians());
        }
        sweepsStarted[a]++;
        sweepTicks[a] = 0;
        sweepHits[a] = 0;
        actions.sweep(a, true, ox, oy);
    }

    /** The running sweeps advance: the beam starts after the telegraph, the sweep ends after its time or with its part. */
    void step(SetPiece.BossActions actions) {
        for (int a = 0; a < sweepTicks.length; a++) {
            if (sweepTicks[a] < 0) {
                continue;
            }
            if (piece.partHp(sweepPart[a]) <= 0) {
                sweepTicks[a] = -1;
                continue;
            }
            EnemySpec.Sweep sweep = boss.attacks().get(a).sweep().orElseThrow();
            int telegraph = SimStep.ticks(sweep.telegraphSeconds());
            if (++sweepTicks[a] >= telegraph + Math.max(1, SimStep.ticks(sweep.sweepSeconds()))) {
                sweepTicks[a] = -1;
            } else if (sweepTicks[a] == telegraph) {
                actions.sweep(a, false, originX(a), originY(a));
            }
        }
    }

    /** Whether attack {@code a}'s sweep is in its telegraph now. */
    boolean telegraphing(int a) {
        return sweepTicks[a] >= 0
                && sweepTicks[a]
                        < SimStep.ticks(
                                boss.attacks().get(a).sweep().orElseThrow().telegraphSeconds());
    }

    /** Whether attack {@code a}'s beam sweeps now. */
    boolean beaming(int a) {
        return sweepTicks[a] >= 0 && !telegraphing(a);
    }

    /** The boss's attacks (as {@link BossSpec#attacks()}). */
    int count() {
        return sweepTicks.length;
    }

    /** Attack {@code a}'s beam's origin now, px from the left: its part's centre. */
    double originX(int a) {
        return piece.partX(Math.max(0, sweepPart[a]));
    }

    /** Attack {@code a}'s beam's origin now, px up: its sweep's origin below its part's centre. */
    double originY(int a) {
        return piece.partY(Math.max(0, sweepPart[a]))
                - boss.attacks().get(a).sweep().orElseThrow().originDown();
    }

    /** Attack {@code a}'s sweep's first edge, radians clockwise from straight down (the telegraph's line). */
    double from(int a) {
        return sweepCentre[a]
                - sweepDirection[a]
                        * boss.attacks().get(a).sweep().orElseThrow().arcRadians()
                        / 2;
    }

    /** Attack {@code a}'s sweep's last edge, radians clockwise from straight down. */
    double to(int a) {
        return sweepCentre[a]
                + sweepDirection[a]
                        * boss.attacks().get(a).sweep().orElseThrow().arcRadians()
                        / 2;
    }

    /**
     * Attack {@code a}'s beam's heading, radians clockwise from straight down, moving evenly from {@link #from} to
     * {@link #to} over the sweep; {@code alpha} interpolates between steps.
     */
    double beam(int a, double alpha) {
        EnemySpec.Sweep sweep = boss.attacks().get(a).sweep().orElseThrow();
        int telegraph = SimStep.ticks(sweep.telegraphSeconds());
        double into = Math.max(0, sweepTicks[a] - telegraph + alpha) / Math.max(1, SimStep.ticks(sweep.sweepSeconds()));
        return from(a) + (to(a) - from(a)) * Math.min(1, into);
    }

    /** Whether attack {@code a}'s current sweep hit {@code what} already. */
    boolean hit(int a, int what) {
        return (sweepHits[a] & what) != 0;
    }

    /** Attack {@code a}'s current sweep hit {@code what}: it does not again in this sweep. */
    void markHit(int a, int what) {
        sweepHits[a] |= what;
    }

    /** Its state, in the order the boss's hash has always added it; nothing for a boss without a sweep. */
    void addTo(StateHash hash) {
        if (!any) {
            return;
        }
        for (int a = 0; a < sweepTicks.length; a++) {
            hash.add(sweepTicks[a])
                    .add(sweepPart[a])
                    .add(sweepCentre[a])
                    .add(sweepDirection[a])
                    .add(sweepsStarted[a])
                    .add(sweepHits[a]);
        }
    }
}
