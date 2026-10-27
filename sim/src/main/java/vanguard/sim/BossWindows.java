package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * A boss's windows (design/enemies/bosses, part G: {@link BossSpec.Windows}), stepped by its {@link SetPiece} in the
 * style of the {@link BreachArena}: per phase and part whether the phase's windows hold the part (it takes damage only
 * while open), per part the steps it stays open, and the windows' clock: when one is due, the next group in order
 * with a living part on the field opens (every such group when {@code all}), and each opened group releases the spawn
 * of this opening from its open parts.
 */
final class BossWindows {
    private final SetPiece piece;
    /** Per phase and part, whether the phase's windows hold the part. */
    private final boolean[][] windowed;
    /** Per part, the steps it stays open in its window; 0 closed. */
    private final int[] openTicks;

    private int windowTicks;
    private int windowNext;
    private int windowOpenings;

    BossWindows(SetPiece piece, BossSpec boss, int parts, int phases) {
        this.piece = piece;
        windowed = new boolean[phases][parts];
        openTicks = new int[parts];
        for (int f = 0; f < phases; f++) {
            BossSpec.Phase current = boss.phases().get(f);
            for (int p = 0; p < parts; p++) {
                windowed[f][p] =
                        current.windows().isPresent() && current.windows().get().holds(p);
            }
        }
    }

    /** Back to the level start: every window shut, the clock at its start. */
    void reset() {
        windowTicks = 0;
        windowNext = 0;
        windowOpenings = 0;
        Arrays.fill(openTicks, 0);
    }

    /** A phase starts: its windows close. */
    void closeAll() {
        Arrays.fill(openTicks, 0);
    }

    /** One step of the open windows' time. */
    void tick() {
        for (int p = 0; p < openTicks.length; p++) {
            if (openTicks[p] > 0) {
                openTicks[p]--;
            }
        }
    }

    /** Phase {@code current} engages: its windows first open its {@code offset} after the phase's {@code delay}. */
    void arm(BossSpec.Phase current, int delay) {
        int offset = current.windows().isPresent()
                ? SimStep.ticks(current.windows().get().offsetSeconds())
                : 0;
        windowTicks = Math.max(1, delay + offset);
        windowNext = 0;
        windowOpenings = 0;
    }

    /** One step of the phase's {@code windows}, the ship at (shipX, shipY). */
    void step(BossSpec.Windows windows, double shipX, double shipY, BossActions actions, boolean firing) {
        if (--windowTicks > 0) {
            return;
        }
        windowTicks = Math.max(1, SimStep.ticks(windows.everySeconds()));
        int open = Math.max(1, SimStep.ticks(windows.openSeconds()));
        BossSpec.Spawn spawn = windows.spawns().isEmpty()
                ? null
                : windows.spawns().get(windowOpenings % windows.spawns().size());
        int groups = windows.groups().size();
        boolean opened = false;
        for (int i = 0; i < groups; i++) {
            int g = (windowNext + i) % groups;
            List<Integer> group = windows.groups().get(g);
            int alive = 0;
            int ready = 0;
            for (int k = 0; k < group.size(); k++) {
                int p = group.get(k);
                if (piece.partHp(p) > 0) {
                    alive++;
                    ready += piece.partOnField(p) ? 1 : 0;
                }
            }
            if (ready == 0) {
                continue;
            }
            for (int k = 0; k < group.size(); k++) {
                int p = group.get(k);
                if (piece.partHp(p) > 0 && piece.partOnField(p)) {
                    openTicks[p] = open;
                }
            }
            if (spawn != null && firing) {
                release(spawn, group, alive, ready, shipX, shipY, actions);
            }
            opened = true;
            if (!windows.all()) {
                windowNext = (g + 1) % groups;
                break;
            }
        }
        if (opened) {
            windowOpenings++;
        }
    }

    /**
     * An opened group releases its share of {@code spawn}: the count times its living share, rounded up, dealt out
     * among its open parts, each part's units spread over the arc centred on the direction from it to the ship.
     */
    private void release(
            BossSpec.Spawn spawn,
            List<Integer> group,
            int alive,
            int ready,
            double shipX,
            double shipY,
            BossActions actions) {
        int units = (spawn.count() * alive + group.size() - 1) / group.size();
        if (spawn.ambush()) {
            // M5 part H: ambush launches fly their planned rear paths; the arc is not read.
            int j = 0;
            for (int k = 0; k < group.size(); k++) {
                int p = group.get(k);
                if (piece.partHp(p) <= 0 || !piece.partOnField(p)) {
                    continue;
                }
                int mine = units / ready + (j < units % ready ? 1 : 0);
                j++;
                for (int u = 0; u < mine; u++) {
                    actions.ambush(spawn, p, u, mine, piece.partX(p), piece.partY(p));
                }
            }
            return;
        }
        int j = 0;
        for (int k = 0; k < group.size(); k++) {
            int p = group.get(k);
            if (piece.partHp(p) <= 0 || !piece.partOnField(p)) {
                continue;
            }
            int mine = units / ready + (j < units % ready ? 1 : 0);
            j++;
            double px = piece.partX(p);
            double py = piece.partY(p);
            double toShip = StrictMath.atan2(shipY - py, shipX - px);
            for (int u = 0; u < mine; u++) {
                double angle =
                        mine == 1 ? toShip : toShip - spawn.arcRadians() / 2 + u * spawn.arcRadians() / (mine - 1);
                actions.launch(spawn, px, py, angle);
            }
        }
    }

    /** Whether phase {@code phase}'s windows hold part {@code p}. */
    boolean windowed(int phase, int p) {
        return windowed[phase][p];
    }

    /** Steps part {@code p}'s window stays open; 0 shut. */
    int openTicks(int p) {
        return openTicks[p];
    }

    /** The windows' clock: steps to the next opening (hashed with the boss's own state). */
    int ticks() {
        return windowTicks;
    }

    /** The group that opens next (hashed with the boss's own state). */
    int next() {
        return windowNext;
    }

    /** The openings so far (hashed with the boss's own state). */
    int openings() {
        return windowOpenings;
    }

    /** The parts' open times, as the boss's hash has always added them. */
    void addOpenTicks(StateHash hash) {
        for (int ticks : openTicks) {
            hash.add(ticks);
        }
    }
}
