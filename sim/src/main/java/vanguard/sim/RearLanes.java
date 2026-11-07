package vanguard.sim;

import java.util.List;

/**
 * M11 part F, step F1 (design/campaign Level 48, rear rules: "a rear Chimera V's tip ≥ 120 px off the ship's x at the
 * warning"; m11f-gaps.md §1 #9): the level's rear waves that come up in the lane the ship is not in ({@link
 * WaveSpec.Lane#AWAY}). As such a wave's edge warning starts, its lane is drawn from the ship's x: its first planned unit
 * (a V's tip) comes up at the middle of the field's half the ship is not in (so at least {@link #LEAST_OFF} px off the
 * ship's x), every unit of the wave moved sideways by as much as that unit; the warning's arrow shows there ({@link
 * #laneX()}). Made by the sortie's force only in a level with such a wave (its state hashed then); a retry draws afresh.
 * Allocates nothing once made.
 *
 * <p>M11 part F, step F1b (Level 48's rear Chimera V-wings, t=17 and t=58): a wave whose tip sweeps across (a swooping
 * V rising from the bottom edge, {@link WaveSchedule#rises}) sweeps toward the field's middle from its lane: when its
 * planned sweep points at the near side edge of the drawn lane, its units fly their paths mirrored left to right about
 * the tip's x (made with the force), so its curve stays on the field; a straight wave (a glide V, a line) only moves.
 */
public final class RearLanes {
    /** The least distance of a wave's tip from the ship's x as its warning starts, px. */
    public static final double LEAST_OFF = 120;

    /** Per lane wave: its index among the level's waves, its warning's start and its entry, steps, its planned tip x. */
    private final int[] waves;

    private final int[] warnTicks;
    private final int[] entryTicks;
    private final double[] tipX;
    /** Per lane wave: its drawn lane (the tip's x) and whether it is drawn in this attempt. */
    private final double[] laneX;

    private final boolean[] drawn;
    /** F1b: per lane wave, its tip's sweep (its path's end x less its start x) and whether it flies mirrored now. */
    private final double[] sweep;

    private final boolean[] mirrored;
    /** F1b: per unit of the plan, its spawn mirrored about its wave's tip; null but for a lane wave that sweeps. */
    private final Spawn[] mirroredSpawns;
    /** The level tick of the last {@link #step}. */
    private int levelTick;

    private RearLanes(int count, int units) {
        waves = new int[count];
        warnTicks = new int[count];
        entryTicks = new int[count];
        tipX = new double[count];
        laneX = new double[count];
        drawn = new boolean[count];
        sweep = new double[count];
        mirrored = new boolean[count];
        mirroredSpawns = new Spawn[units];
        reset();
    }

    /** The lanes of {@code specs}' {@code away} waves as {@code schedule} plans them; null without such a wave. */
    static RearLanes of(List<WaveSpec> specs, WaveSchedule schedule) {
        int count = (int)
                specs.stream().filter(wave -> wave.lane() == WaveSpec.Lane.AWAY).count();
        if (count == 0) {
            return null;
        }
        RearLanes lanes = new RearLanes(count, schedule.size());
        int i = 0;
        for (int w = 0; w < specs.size(); w++) {
            WaveSpec wave = specs.get(w);
            if (wave.lane() != WaveSpec.Lane.AWAY) {
                continue;
            }
            double lead = Math.max(
                    WaveSchedule.EDGE_WARNING_SECONDS, wave.warningSeconds().orElse(0.0));
            lanes.waves[i] = w;
            lanes.warnTicks[i] = SimStep.ticks(wave.t() - lead);
            lanes.entryTicks[i] = SimStep.ticks(wave.t());
            lanes.tipX[i] = schedule.firstStartX(w);
            lanes.sweep[i] = schedule.firstSweep(w);
            i++;
        }
        for (int u = 0; u < schedule.size(); u++) {
            int lane = lanes.indexOf(schedule.waveAt(u));
            if (lane >= 0 && lanes.sweep[lane] != 0) {
                Spawn spawn = schedule.spawnAt(u);
                lanes.mirroredSpawns[u] = spawn.withPath(spawn.path().mirrored());
            }
        }
        return lanes;
    }

    /** Back to an attempt's start: nothing drawn. */
    void reset() {
        java.util.Arrays.fill(laneX, 0);
        java.util.Arrays.fill(drawn, false);
        java.util.Arrays.fill(mirrored, false);
        levelTick = 0;
    }

    /** The level clock at {@code tick} with the ship at {@code shipX}: the lanes whose warnings start now are drawn. */
    void step(int tick, double shipX) {
        levelTick = tick;
        for (int i = 0; i < waves.length; i++) {
            if (!drawn[i] && tick >= warnTicks[i]) {
                drawn[i] = true;
                laneX[i] = lane(shipX);
                // F1b: a sweep toward the lane's near side edge flies mirrored, toward the field's middle.
                mirrored[i] = sweep[i] * (laneX[i] - PlayField.WIDTH / 2.0) > 0;
            }
        }
    }

    /** The lane's tip x for a ship at {@code shipX}: the middle of the field's half it is not in. */
    static double lane(double shipX) {
        return shipX < PlayField.WIDTH / 2.0 ? PlayField.WIDTH * 0.75 : PlayField.WIDTH * 0.25;
    }

    /**
     * How far wave {@code wave}'s units move sideways, px (0 for a wave of no lane, or one not drawn yet); F1b: for a
     * mirrored wave, from their mirrored paths ({@link #plan}).
     */
    double shift(int wave) {
        int i = indexOf(wave);
        if (i < 0 || !drawn[i] || Double.isNaN(tipX[i])) {
            return 0;
        }
        return mirrored[i] ? tipX[i] + laneX[i] - PlayField.WIDTH : laneX[i] - tipX[i];
    }

    /**
     * F1b: the spawn unit {@code unit} of the plan (of wave {@code wave}) flies: {@code planned}, or its mirror when its
     * lane wave flies mirrored in this attempt.
     */
    Spawn plan(int wave, int unit, Spawn planned) {
        int i = indexOf(wave);
        if (i < 0 || !drawn[i] || !mirrored[i] || mirroredSpawns[unit] == null) {
            return planned;
        }
        return mirroredSpawns[unit];
    }

    /** F1b: whether the {@code i}-th lane wave flies mirrored in this attempt (false while not drawn). */
    public boolean mirrored(int i) {
        return drawn[i] && mirrored[i];
    }

    /** The index among the lane waves of the level's wave {@code wave}; -1 for none. */
    private int indexOf(int wave) {
        for (int i = 0; i < waves.length; i++) {
            if (waves[i] == wave) {
                return i;
            }
        }
        return -1;
    }

    /** The lane (its tip's x) of the lane wave whose warning shows now; NaN for none (the warning's arrow). */
    public double laneX() {
        for (int i = 0; i < waves.length; i++) {
            if (drawn[i] && levelTick >= warnTicks[i] && levelTick < entryTicks[i]) {
                return laneX[i];
            }
        }
        return Double.NaN;
    }

    /** The lane drawn for the level's {@code i}-th lane wave (in the waves' order); NaN while not drawn. */
    public double laneX(int i) {
        return drawn[i] ? laneX[i] : Double.NaN;
    }

    /** The number of lane waves in the level. */
    public int count() {
        return waves.length;
    }

    void addTo(StateHash hash) {
        hash.add(levelTick);
        for (int i = 0; i < waves.length; i++) {
            hash.add(drawn[i] ? 1 : 0).add(laneX[i]);
        }
    }
}
