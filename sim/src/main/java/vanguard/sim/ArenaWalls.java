package vanguard.sim;

import java.util.List;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, Arena: the walls of phase 2; m11h-gaps.md §1 #3, §4): a boss's
 * arena walls in play ({@link ArenaWallsSpec}), kept by its {@link SetPiece} and stepped by the {@link Sortie} ({@link
 * Sortie#arenaWalls()}); a second source of rock beside the level's terrain ({@link Canyon} asks both), so the ship's
 * push, Rook's weave, the units, shots, beams, missiles, bullets and pickups meet it unchanged. Screen space, y up.
 *
 * <p>Each wall is two halves, upper and lower (0 the left wall's upper, 1 its lower, 2 the right wall's upper, 3 its
 * lower), each standing {@link #inset} px in from its edge; across the join ({@link #JOIN} px either side of the split)
 * a wall's face runs straight from one half's inset to the other's. As the walls' phase starts every half is told
 * ({@link SimEvents.Type#WALL_TOLD}) and grows to the corridor's edge over the grow (the closing). Then they breathe
 * about it: a sine of the breath's swing (outward first), followed at the walls' top speed; a half moves inward only in
 * the breath's rising half-periods, each told {@code tellSeconds} ahead, and never while a ship's hull is nearer its
 * face than {@code clear}. A node's death tells its half and sends it back to the edge at the top speed; after the phase
 * (or the boss's death) every half still standing goes the same way. Before its phase every half lies at the edge
 * (no rock). Allocates nothing in a step.
 */
public final class ArenaWalls {
    /** The halves: 0 the left wall's upper, 1 its lower, 2 the right wall's upper, 3 its lower. */
    static final int HALVES = 4;
    /** Half the height of the straight run joining a wall's halves, px (presentation: the walls' tissue bends there). */
    public static final double JOIN = 20;

    private final ArenaWallsSpec spec;
    private final SetPiece piece;
    /** The corridor's edge, px in from either side. */
    private final double edge;
    /** Where a wall's upper half meets its lower, y up. */
    private final double split;
    /** Per half its node's part (-1 for none); per part its half (-1: no node). */
    private final int[] nodeOf = new int[HALVES];

    private final int[] halfOf;
    private final double[] nodeY;
    private final int tellTicks;
    private final int growTicks;
    /** Steps of the grow and of the breath's period. */
    private final int periodTicks;

    private final double growStep;
    private final double mostStep;
    /** The ships' hulls' boxes this step (left, right, low, high per ship) and which are in (derived, not hashed). */
    private final double[] boxes = new double[8];

    private int ships;

    // The state (hashed).
    /** Per half, how far it stands in from its edge, px. */
    private final double[] inset = new double[HALVES];
    /** Per half, whether it goes back to its edge (its node died, or the phase is over). */
    private final boolean[] retreating = new boolean[HALVES];
    /** Per half, the steps its veins still glow (a told move to come). */
    private final int[] told = new int[HALVES];
    /** Per retreating half, the steps before its retreat moves (its tell). */
    private final int[] retreatIn = new int[HALVES];
    /** Whether the walls' phase has started (they stand, or retreat). */
    private boolean standing;
    /** Steps since the walls' phase started. */
    private int ticks;
    /** The breath's next tell, steps from the grow's end (negative: inside the grow). */
    private int nextTell;

    ArenaWalls(ArenaWallsSpec spec, SetPiece piece, int parts) {
        this.spec = spec;
        this.piece = piece;
        edge = (PlayField.WIDTH - spec.corridor()) / 2;
        split = ArenaWallsSpec.split(spec.nodes());
        java.util.Arrays.fill(nodeOf, -1);
        halfOf = new int[parts];
        nodeY = new double[parts];
        java.util.Arrays.fill(halfOf, -1);
        for (ArenaWallsSpec.Node node : spec.nodes()) {
            if (node.part() >= parts) {
                throw new IllegalArgumentException("a wall node is one of the boss's parts: " + node.part());
            }
            int half = node.half(split);
            nodeOf[half] = node.part();
            halfOf[node.part()] = half;
            nodeY[node.part()] = node.y();
        }
        tellTicks = SimStep.ticks(spec.tellSeconds());
        growTicks = Math.max(1, SimStep.ticks(spec.growSeconds()));
        periodTicks = Math.max(4, SimStep.ticks(1 / spec.breatheHz()));
        growStep = edge / growTicks;
        mostStep = spec.mostSpeed() * SimStep.SECONDS;
        reset();
    }

    /** Back to the level start: every half at its edge, nothing told. */
    void reset() {
        java.util.Arrays.fill(inset, 0);
        java.util.Arrays.fill(retreating, false);
        java.util.Arrays.fill(told, 0);
        java.util.Arrays.fill(retreatIn, 0);
        standing = false;
        ticks = 0;
        nextTell = periodTicks / 4 - tellTicks;
    }

    /** A boss checkpoint's copy (its record) or restore of {@code other}'s state. */
    void copyFrom(ArenaWalls other) {
        System.arraycopy(other.inset, 0, inset, 0, HALVES);
        System.arraycopy(other.retreating, 0, retreating, 0, HALVES);
        System.arraycopy(other.told, 0, told, 0, HALVES);
        System.arraycopy(other.retreatIn, 0, retreatIn, 0, HALVES);
        standing = other.standing;
        ticks = other.ticks;
        nextTell = other.nextTell;
    }

    /** A copy for a boss checkpoint's record (made once, with the checkpoint). */
    ArenaWalls mark() {
        return new ArenaWalls(spec, piece, halfOf.length);
    }

    /**
     * One step, on the boss's phase and its nodes as the last step left them (as the drift reads them; a phase
     * checkpoint's restore then lines up with the run it restores): the walls' phase starts, the halves move, the nodes'
     * deaths send their halves back. The ship's hull at (shipX, shipY) when {@code shipIn}, Rook's when {@code rookIn}
     * hold an inward move within {@code clear} of a face.
     */
    void step(
            double shipX,
            double shipY,
            Hull shipHull,
            boolean shipIn,
            double rookX,
            double rookY,
            Hull rookHull,
            boolean rookIn,
            SimEvents events) {
        for (int h = 0; h < HALVES; h++) {
            if (told[h] > 0) {
                told[h]--;
            }
        }
        boolean over = piece.destroyed() || (piece.engaged() && piece.phase() > spec.phase());
        if (!standing) {
            if (over || !piece.engaged() || piece.phase() != spec.phase()) {
                return;
            }
            standing = true;
            ticks = 0;
            for (int h = 0; h < HALVES; h++) {
                tell(h, events);
            }
        } else {
            ticks++;
        }
        for (int h = 0; h < HALVES; h++) {
            int node = nodeOf[h];
            if (!retreating[h] && (over || (node >= 0 && piece.partWrecked(node)))) {
                retreating[h] = true;
                retreatIn[h] = tellTicks;
                tell(h, events);
            }
        }
        int grown = tellTicks + growTicks;
        int breath = ticks - grown;
        boolean inward;
        double target;
        if (ticks < tellTicks) {
            return;
        } else if (ticks < grown) {
            inward = true;
            target = edge;
        } else {
            int u = Math.floorMod(breath, periodTicks);
            inward = u >= periodTicks / 4 && u < 3 * periodTicks / 4;
            target = edge - spec.breatheAmplitude() * Trig.sin(2 * StrictMath.PI * breath / periodTicks);
        }
        while (breath >= nextTell) {
            // The breath's rising half-periods, each told ahead.
            nextTell += periodTicks;
            for (int h = 0; h < HALVES; h++) {
                if (!retreating[h]) {
                    tell(h, events);
                }
            }
        }
        hulls(shipX, shipY, shipHull, shipIn, rookX, rookY, rookHull, rookIn);
        for (int h = 0; h < HALVES; h++) {
            if (retreating[h]) {
                if (retreatIn[h] > 0) {
                    retreatIn[h]--;
                } else {
                    inset[h] = Math.max(0, inset[h] - mostStep);
                }
                continue;
            }
            double step = ticks < grown ? growStep : mostStep;
            if (target < inset[h]) {
                inset[h] = Math.max(target, inset[h] - step);
            } else if (inward && target > inset[h]) {
                inset[h] = Math.min(target, Math.min(inset[h] + step, room(h)));
            }
        }
    }

    /** Half {@code h}'s veins glow for a told move ({@link SimEvents.Type#WALL_TOLD}, value the half). */
    private void tell(int h, SimEvents events) {
        told[h] = tellTicks;
        boolean left = h < 2;
        double y = h % 2 == 0 ? (split + PlayField.HEIGHT) / 2 : split / 2;
        events.add(SimEvents.Type.WALL_TOLD, left ? inset[h] : PlayField.WIDTH - inset[h], y, h);
    }

    /** The ships' hulls' extents this step: the player's ship's when {@code shipIn}, Rook's when {@code rookIn}. */
    private void hulls(
            double shipX,
            double shipY,
            Hull shipHull,
            boolean shipIn,
            double rookX,
            double rookY,
            Hull rookHull,
            boolean rookIn) {
        ships = 0;
        if (shipIn) {
            extent(0, shipHull, shipX, shipY);
        }
        if (rookIn) {
            extent(1, rookHull, rookX, rookY);
        }
    }

    /** Ship {@code ship}'s (0 the player's, 1 Rook's) hull's box: left, right, low, high. */
    private void extent(int ship, Hull hull, double x, double y) {
        double l = Double.POSITIVE_INFINITY;
        double r = Double.NEGATIVE_INFINITY;
        double lo = Double.POSITIVE_INFINITY;
        double hi = Double.NEGATIVE_INFINITY;
        List<Hull.Part> parts = hull.parts();
        for (int p = 0; p < parts.size(); p++) {
            Hull.Part part = parts.get(p);
            double hw = part.box().width() / 2;
            double hh = part.box().height() / 2;
            l = Math.min(l, x + part.dx() - hw);
            r = Math.max(r, x + part.dx() + hw);
            lo = Math.min(lo, y + part.dy() - hh);
            hi = Math.max(hi, y + part.dy() + hh);
        }
        boxes[4 * ship] = l;
        boxes[4 * ship + 1] = r;
        boxes[4 * ship + 2] = lo;
        boxes[4 * ship + 3] = hi;
        ships |= 1 << ship;
    }

    /**
     * How far in half {@code h} may stand this step: {@code clear} short of the nearest ship's hull in its band (its
     * rows and the join); never less than it stands now (a half only ever holds for a ship, it is never pushed out).
     */
    private double room(int h) {
        double most = PlayField.WIDTH / 2.0;
        boolean upper = h % 2 == 0;
        double low = upper ? split - JOIN : Double.NEGATIVE_INFINITY;
        double high = upper ? Double.POSITIVE_INFINITY : split + JOIN;
        for (int ship = 0; ship < 2; ship++) {
            if ((ships & (1 << ship)) == 0) {
                continue;
            }
            double lo = boxes[4 * ship + 2];
            double hi = boxes[4 * ship + 3];
            if (hi <= low || lo >= high) {
                continue;
            }
            double gap = h < 2 ? boxes[4 * ship] - spec.clear() : PlayField.WIDTH - boxes[4 * ship + 1] - spec.clear();
            most = Math.min(most, gap);
        }
        return Math.max(inset[h], most);
    }

    // The rock (screen space, y up).

    /** The left wall's face at height y, px from the left edge (0: no rock). */
    public double leftAt(double y) {
        return face(inset[1], inset[0], y);
    }

    /** The right wall's face at height y, px (480: no rock). */
    public double rightAt(double y) {
        return PlayField.WIDTH - face(inset[3], inset[2], y);
    }

    /** A wall's inset at y: its lower half's below the join, its upper's above it, straight across the join. */
    private double face(double lower, double upper, double y) {
        if (y <= split - JOIN) {
            return lower;
        }
        if (y >= split + JOIN) {
            return upper;
        }
        return lower + (upper - lower) * (y - (split - JOIN)) / (2 * JOIN);
    }

    /** As {@link Terrain#rock}: whether the box overlaps a wall (on the field only: the edges are no rock). */
    boolean rock(double x, double y, double halfW, double halfH) {
        double left = Math.max(leftAt(y - halfH), leftAt(y + halfH));
        if (Math.max(x - halfW, 0) < left) {
            return true;
        }
        double right = Math.min(rightAt(y - halfH), rightAt(y + halfH));
        return Math.min(x + halfW, PlayField.WIDTH) > right;
    }

    /** As {@link Terrain#ceiling}: the lowest height at or above {@code fromY} where the column meets a wall; +∞ none. */
    double ceiling(double x, double halfW, double fromY) {
        double xl = Math.max(x - halfW, 0);
        double xr = Math.min(x + halfW, PlayField.WIDTH);
        return Math.min(first(inset[1], inset[0], xl, fromY), first(inset[3], inset[2], PlayField.WIDTH - xr, fromY));
    }

    /** The lowest y ≥ fromY where a wall's inset (lower, upper) exceeds {@code reach}; +∞ for none. */
    private double first(double lower, double upper, double reach, double fromY) {
        if (face(lower, upper, fromY) > reach) {
            return fromY;
        }
        if (!(upper > reach) || !(upper > lower)) {
            return Double.POSITIVE_INFINITY;
        }
        // Rising across the join: where it passes the reach.
        double at = split - JOIN + (reach - lower) / (upper - lower) * 2 * JOIN;
        return Math.max(fromY, Math.nextUp(at));
    }

    /** As {@link Terrain#spanLeft}: the corridor's left edge at y when x lies in it; NaN when x is in a wall. */
    double spanLeft(double y, double x) {
        double left = leftAt(y);
        return x > left && x < rightAt(y) ? left : Double.NaN;
    }

    /** As {@link Terrain#spanRight}. */
    double spanRight(double y, double x) {
        double right = rightAt(y);
        return x > leftAt(y) && x < right ? right : Double.NaN;
    }

    /** As {@link Terrain#flyableX}: x moved clear of the walls by {@code margin} (a wall at its edge is no rock). */
    double flyableX(double y, double x, double margin) {
        double left = leftAt(y);
        double right = rightAt(y);
        double out = x;
        if (left > 0 && out < left + margin) {
            out = left + margin;
        }
        if (right < PlayField.WIDTH && out > right - margin) {
            out = right - margin;
        }
        return out;
    }

    /**
     * As {@link Terrain#contact}: whether the hull around (x, y) touches a wall; then {@code out} holds the deepest
     * touch's normal (straight out of the wall: (1, 0) off the left, (-1, 0) off the right) and its depth, px.
     */
    boolean contact(Hull hull, double x, double y, double[] out) {
        out[0] = 0;
        out[1] = 0;
        out[2] = 0;
        boolean any = false;
        List<Hull.Part> parts = hull.parts();
        for (int p = 0; p < parts.size(); p++) {
            Hull.Part part = parts.get(p);
            double cx = x + part.dx();
            double cy = y + part.dy();
            double hw = part.box().width() / 2;
            double hh = part.box().height() / 2;
            double left = Math.max(leftAt(cy - hh), leftAt(cy + hh)) - Math.max(cx - hw, 0);
            if (left > 0 && left > out[2]) {
                out[0] = 1;
                out[1] = 0;
                out[2] = left;
                any = true;
            }
            double right = Math.min(cx + hw, PlayField.WIDTH) - Math.min(rightAt(cy - hh), rightAt(cy + hh));
            if (right > 0 && right > out[2]) {
                out[0] = -1;
                out[1] = 0;
                out[2] = right;
                any = true;
            }
        }
        return any;
    }

    // The nodes and the looks.

    /** Whether part {@code p} rides a wall (a node). */
    public boolean rides(int p) {
        return p < halfOf.length && halfOf[p] >= 0;
    }

    /** Node part {@code p}'s centre now: on its half's inner face. */
    public double nodeX(int p) {
        int h = halfOf[p];
        return h < 2 ? inset[h] : PlayField.WIDTH - inset[h];
    }

    /** Node part {@code p}'s height, y up. */
    public double nodeY(int p) {
        return nodeY[p];
    }

    /** Half {@code h}'s inset now, px from its edge (0: at the edge). */
    public double inset(int h) {
        return inset[h];
    }

    /** Whether half {@code h}'s veins glow now (a told move to come). */
    public boolean told(int h) {
        return told[h] > 0;
    }

    /** Whether half {@code h} goes (or went) back to its edge. */
    public boolean retreating(int h) {
        return retreating[h];
    }

    /** Whether the walls stand (their phase has started; a half may already be back at its edge). */
    public boolean standing() {
        return standing;
    }

    /** Where a wall's halves meet, y up. */
    public double split() {
        return split;
    }

    /** Its spec (the corridor, the contact). */
    public ArenaWallsSpec spec() {
        return spec;
    }

    /** Whether any half stands off its edge (there is rock). */
    boolean any() {
        return inset[0] > 0 || inset[1] > 0 || inset[2] > 0 || inset[3] > 0;
    }

    void addTo(StateHash hash) {
        for (int h = 0; h < HALVES; h++) {
            hash.add(inset[h]).add(retreating[h] ? 1 : 0).add(told[h]).add(retreatIn[h]);
        }
        hash.add(standing ? 1 : 0).add(ticks).add(nextTell);
    }
}
