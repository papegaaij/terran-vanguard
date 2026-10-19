package vanguard.game.render;

import java.util.List;
import vanguard.content.BackdropData;
import vanguard.sim.PlayField;
import vanguard.sim.Terrain;

/**
 * M7 part C (round 40): the canyon's rock seen from above like the Grand Canyon (design/campaign Level 16,
 * Rendering): each wall descends from the plateau's rim in steps ({@value #SEGMENTS} segments: steep cliff
 * bands and talus benches, each its own stratum: the pale cream cap, the red-orange middle cliffs, the dark
 * inner gorge at the foot). Every step's edge (a node) is set back from the collision line (the foot, node 0,
 * never moved) toward the rock by a setback that grows with the room between the wall and the field's edge
 * and with the side canyons ({@link #notches}: three sizes, the big ones repeating every {@value #PERIOD} rows
 * so the plateau tile's gullies drain into them), so the rim is ragged and the strata run round the
 * amphitheatres as contour bands. The rock islands are buttes: the same steps as rings round a flat cap.
 * Lit from the upper left ({@link #shade}): the left wall's cliffs in deep shadow, its benches partly in the
 * cliffs' cast shadow, the right wall's cliffs lit.
 *
 * <p>The twin of tools/art/backdrop_l16.py's canyon model (same constants, hash and functions), so the
 * plateau's gullies, the buttes' caps and the review composites match what the game draws: change one,
 * change the other.
 *
 * <p>The texture convention ({@code canyon-wall}, {@value #TEX_W} × ({@value #TEX_PERIOD} + {@value
 * #TEX_PAD})): segment g's columns from {@link #NODE_U}[g] to [g + 1] (foot to rim), its rows the arc length
 * of the segment's middle contour along the wall ({@link Steps#v}), row 0 of a period at the image's bottom,
 * the image's top {@value #TEX_PAD} rows repeating the period's last ones (a quad's rows may run past the
 * period). The buttes' faces likewise with the perimeter for the rows and {@value #FACE_PAD} rows repeated.
 */
final class CanyonProfile {
    /** A full wall's rise in px (the normals and the cast shadows). */
    static final double WALL_PX = 120;
    /** Px of cast shadow per px of rise. */
    static final double SHADOW_RATIO = 0.45;
    /** The cast shadows' direction: x right, terrain rows up (down the screen, to the right). */
    static final double SHADOW_DX = 0.82;

    static final double SHADOW_DY = -0.57;
    /** Toward the key light: x right, rows up, z up (from the upper left). */
    static final double[] LIGHT = unit(-0.66, 0.46, 0.60);

    static final double AMBIENT = 0.45;
    static final double DIFFUSE = 0.55;
    /** A flat bench's shade; the lit cliffs clamp at 1. */
    static final double FLAT = 0.9;
    /** The shade of rock in shadow (ambient only). */
    static final float SHADOWED = (float) (AMBIENT / (AMBIENT + DIFFUSE * LIGHT[2]) * FLAT);
    /** A cast shadow's opacity on the floor (black over the lit floor down to {@link #SHADOWED}'s level). */
    static final float FLOOR_SHADOW = (float) (1 - SHADOWED / FLAT);
    /** Rows per plateau tile (960 ÷ 1.25): the big side canyons repeat with it. */
    static final int PERIOD = 768;

    static final int TEX_W = 128;
    static final int TEX_PERIOD = 1024;
    static final int TEX_PAD = 256;
    static final int FACE_PAD = 16;

    static final int SEGMENTS = 6;
    static final int NODES = SEGMENTS + 1;
    /** Per segment: a cliff (else a bench), its rise share, its setback px, its texture columns. */
    static final boolean[] CLIFF = {true, false, true, false, true, false};

    static final double[] RISE = {0.36, 0.05, 0.30, 0.07, 0.16, 0.06};
    static final double[] SETBACK = {3, 14, 4, 16, 3, 8};
    static final int[] COLUMNS = {24, 20, 32, 24, 16, 12};
    /** Per node: the side canyons' share of their depth there (the rim takes it all). */
    static final double[] SHARE = {0, 0.10, 0.30, 0.50, 0.80, 0.92, 1.0};
    /** Per node: its rise share and its texture column share. */
    static final double[] NODE_Z = new double[NODES];

    static final double[] NODE_U = new double[NODES];
    /** The segment a face patch (a burnt cliff habitat) covers: the red middle cliffs. */
    static final int HABITAT_SEGMENT = 2;
    /** The plateau layer's row of the rim of terrain row S is 1.25 S + this. */
    static final double PLATEAU_ROW0 = BackdropData.Tower.CENTRE_Y
            + WallProjection.PLATEAU_SCALE * (PlayField.HEIGHT - BackdropData.Tower.CENTRE_Y);

    private static final long MASK = 0xFFFFFFFFL;
    private static final double CX = BackdropData.Tower.CENTRE_X;
    /** The notches' smaller octaves: cell, presence, width, extra width, depth, extra depth, salt. */
    private static final double[][] OCTAVES = {{112, 0.7, 8, 12, 8, 18, 5}, {40, 0.6, 4, 5, 3, 6, 9}};

    static {
        int columns = 0;
        for (int g = 0; g < SEGMENTS; g++) {
            NODE_Z[g + 1] = NODE_Z[g] + RISE[g];
            columns += COLUMNS[g];
            NODE_U[g + 1] = columns / (double) TEX_W;
        }
    }

    private CanyonProfile() {}

    private static double[] unit(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        return new double[] {x / l, y / l, z / l};
    }

    /** [0, 1) from integers. */
    static double hash01(long i, long seed) {
        long h = (i * 0x9E3779B1L + seed * 0x85EBCA77L) & MASK;
        h ^= h >>> 15;
        h = (h * 0x2C1B3C6DL) & MASK;
        h ^= h >>> 12;
        h = (h * 0x297A2D39L) & MASK;
        h ^= h >>> 15;
        return h / 4294967296.0;
    }

    /** Smooth value noise 0..1 along x. */
    static double vnoise(double x, double cell, long seed) {
        double f = x / cell;
        long i = (long) Math.floor(f);
        double t = f - i;
        t = t * t * (3 - 2 * t);
        return hash01(i, seed) * (1 - t) + hash01(i + 1, seed) * t;
    }

    /** 0..1, octaves halving the cell. */
    static double fbm(double x, double cell, long seed, int octaves) {
        double total = 0;
        double amp = 1;
        double norm = 0;
        for (int o = 0; o < octaves; o++) {
            total += amp * vnoise(x, cell / (1 << o), seed + 101L * o);
            norm += amp;
            amp *= 0.5;
        }
        return total / norm;
    }

    /** A side canyon's cross-section: 1 at its middle, 0 at |u| = 1. */
    static double bump(double u) {
        return Math.abs(u) < 1 ? Math.pow(Math.max(0, 1 - u * u), 0.7) : 0;
    }

    /** The side canyons biting back into the plateau at {@code row}: px of extra setback at the rim. */
    static double notches(int side, double row) {
        long base = 40 + 20L * side;
        double out = 0;
        for (int k = 0; k < 4; k++) {
            double c = (k + 0.2 + 0.6 * hash01(k, base + 1)) * PERIOD / 4.0;
            double w = 24 + 26 * hash01(k, base + 2);
            double d = 30 + 34 * hash01(k, base + 3);
            double x = row - c + PERIOD / 2.0;
            double du = x - PERIOD * Math.floor(x / PERIOD) - PERIOD / 2.0;
            out += d * bump(du / w);
        }
        for (double[] o : OCTAVES) {
            double cell = o[0];
            long salt = (long) o[6];
            long k0 = (long) Math.floor(row / cell);
            for (long k = k0 - 1; k <= k0 + 1; k++) {
                if (hash01(k, base + salt) < o[1]) {
                    double c = (k + 0.15 + 0.7 * hash01(k, base + salt + 1)) * cell;
                    double w = o[2] + o[3] * hash01(k, base + salt + 2);
                    double d = o[4] + o[5] * hash01(k, base + salt + 3);
                    out += d * bump((row - c) / w);
                }
            }
        }
        return out;
    }

    /** The benches' width against the room between the wall's foot and the field's edge. */
    static double roomFactor(int side, double foot) {
        double room = side == WallProjection.LEFT ? foot : PlayField.WIDTH - foot;
        return Math.clamp((room - 40) / 60, 0.3, 1.4);
    }

    /** The scale of a point at a share {@code z} of the plateau's height: 6 ÷ (6 − 1.2 z). */
    static double scale(double z) {
        return WallProjection.scale(1, z);
    }

    /** A surface's shade from its normal (x right, rows up, z up): the key light and the ambient, flat {@link #FLAT}. */
    static float shade(double nx, double ny, double nz) {
        double l = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (l == 0) {
            return (float) FLAT;
        }
        double dot = (nx * LIGHT[0] + ny * LIGHT[1] + nz * LIGHT[2]) / l;
        double lum = AMBIENT + DIFFUSE * Math.max(0, dot);
        return (float) Math.clamp(lum / (AMBIENT + DIFFUSE * LIGHT[2]) * FLAT, 0, 1);
    }

    /** A butte's steps against a wall's (no side canyons): the fork mesa's, the pillar's. */
    static double islandScale(String look) {
        return switch (look) {
            case "mesa" -> 0.72;
            case "pillar" -> 0.36;
            default -> 0.5;
        };
    }

    /**
     * A plateau piece the rim keeps clear (the Helix station on the north plateau): on {@code side}, from
     * terrain row {@code from} to {@code to}, the rim stays beyond screen x {@code edge} (its side toward the
     * canyon).
     */
    record Cap(int side, double from, double to, double edge) {}

    /**
     * A wall's steps per whole terrain row (linear between): each node's setback (the wall's height share
     * applied) and each segment's v (the arc length of its middle contour from the wall's first row).
     */
    static final class Steps {
        final int side;
        /** The wall's first and last row. */
        final double start;

        final double end;
        private final Terrain terrain;
        private final WallProjection.Heights heights;
        private final int first;
        private final int rows;
        /** Per row, nodes 1..6. */
        private final float[] setback;
        /** Per row, segments 0..5. */
        private final float[] arc;

        /**
         * @param patchRows the face patches' middle rows on this side (no side canyons round them)
         * @param caps the plateau pieces the rim keeps clear (all sides; this side's apply)
         */
        Steps(
                Terrain terrain,
                int side,
                WallProjection.Heights heights,
                double start,
                double end,
                double[] patchRows,
                List<Cap> caps) {
            this.side = side;
            this.terrain = terrain;
            this.heights = heights;
            this.start = start;
            this.end = end;
            first = (int) Math.floor(start);
            rows = (int) Math.ceil(end) - first + 1;
            setback = new float[rows * SEGMENTS];
            arc = new float[rows * SEGMENTS];
            double sign = side == WallProjection.LEFT ? -1 : 1;
            double[] s = new double[NODES];
            double[] previous = new double[SEGMENTS];
            double[] length = new double[SEGMENTS];
            for (int i = 0; i < rows; i++) {
                double row = first + i;
                double at = Math.clamp(row, start, end);
                double foot = line(at);
                double h = Math.clamp(heights.at(at), 0, 1);
                double calm = 0;
                for (double p : patchRows) {
                    calm = Math.max(calm, Math.clamp((190 - Math.abs(row - p)) / 70, 0, 1));
                }
                double cap = Double.POSITIVE_INFINITY;
                for (Cap c : caps) {
                    if (c.side() == side) {
                        double limit = ((c.edge() - CX) / WallProjection.PLATEAU_SCALE + CX - foot) * sign;
                        double out = Math.max(0, Math.max(c.from() - row, row - c.to()));
                        cap = Math.min(cap, Math.max(0.5, limit - 4) + 1.5 * out);
                    }
                }
                setbacks(side, row, foot, h, cap, calm, s);
                for (int g = 0; g < SEGMENTS; g++) {
                    setback[i * SEGMENTS + g] = (float) s[g + 1];
                    double mid = foot + sign * (s[g] + s[g + 1]) / 2;
                    if (i > 0) {
                        double d = mid - previous[g];
                        length[g] += Math.sqrt(1 + d * d);
                    }
                    previous[g] = mid;
                    arc[i * SEGMENTS + g] = (float) length[g];
                }
            }
        }

        /** Each node's setback at {@code row} into {@code s} (NODES values, node 0 = 0), the height applied. */
        static void setbacks(int side, double row, double foot, double h, double cap, double calm, double[] s) {
            double r = roomFactor(side, foot);
            double n = notches(side, row) * r * (1 - calm);
            s[0] = 0;
            for (int g = 0; g < SEGMENTS; g++) {
                double wobble = fbm(row, 48, 300 + 20L * side + g, 3);
                double inc = CLIFF[g] ? SETBACK[g] * (0.7 + 0.6 * wobble) : SETBACK[g] * r * (0.6 + 0.8 * wobble);
                s[g + 1] = s[g] + inc + (SHARE[g + 1] - SHARE[g]) * n;
            }
            double k = Math.min(1, cap / Math.max(s[NODES - 1], 1e-9));
            for (int j = 0; j < NODES; j++) {
                s[j] *= k * h;
            }
        }

        /** The collision line at {@code row} (clamped to the wall's rows). */
        double line(double row) {
            double at = Math.clamp(row, start, end);
            return side == WallProjection.LEFT ? terrain.leftAt(at) : terrain.rightAt(at);
        }

        /** The wall's height share at {@code row}. */
        double height(double row) {
            return Math.clamp(heights.at(row), 0, 1);
        }

        private double table(float[] values, double row, int column) {
            double f = Math.clamp(row - first, 0, rows - 1.000001);
            int i = (int) Math.floor(f);
            double t = f - i;
            return values[i * SEGMENTS + column] * (1 - t) + values[(i + 1) * SEGMENTS + column] * t;
        }

        /** Node {@code j}'s setback at {@code row}, px toward the rock. */
        double setback(double row, int j) {
            return j == 0 ? 0 : table(setback, row, j - 1);
        }

        /** Node {@code j}'s x at {@code row}: the foot moved toward the rock by its setback. */
        double x(double row, int j) {
            double sign = side == WallProjection.LEFT ? -1 : 1;
            return line(row) + sign * setback(row, j);
        }

        /** Segment {@code g}'s texture row at {@code row}: the arc length of its middle contour. */
        double v(double row, int g) {
            return table(arc, row, g);
        }

        boolean valid(double row) {
            return row >= start && row <= end;
        }

        /** Segment {@code g}'s normal at {@code row} into {@code n} (x right, rows up, z up). */
        void normal(double row, int g, double[] n) {
            double h = height(row);
            double dz = (NODE_Z[g + 1] - NODE_Z[g]) * h * WALL_PX;
            double dx = x(row, g + 1) - x(row, g);
            double mid0 = (x(row - 1, g) + x(row - 1, g + 1)) / 2;
            double mid1 = (x(row + 1, g) + x(row + 1, g + 1)) / 2;
            double slope = (mid1 - mid0) / 2;
            if (Math.abs(dx) + dz < 1e-9) {
                n[0] = 0;
                n[1] = 0;
                n[2] = 1;
                return;
            }
            double sign = dx < 0 ? -1 : 1;
            n[0] = -dz * sign;
            n[1] = dz * slope * sign;
            n[2] = dx * sign;
        }

        /**
         * The share of bench {@code g} (from its back, under the cliff above it) in that cliff's and the steps'
         * above cast shadow at {@code row}; 0 for a cliff or the top bench.
         */
        double cast(double row, int g, double[] n) {
            if (CLIFF[g] || g + 1 >= SEGMENTS) {
                return 0;
            }
            normal(row, g + 1, n);
            double nh = Math.hypot(n[0], n[1]);
            double lh = Math.hypot(LIGHT[0], LIGHT[1]);
            double facing = nh < 1e-9 ? 0 : (n[0] * LIGHT[0] + n[1] * LIGHT[1]) / nh / lh;
            double h = height(row);
            double back = setback(row, g + 1);
            double width = Math.max(back - setback(row, g), 1e-6);
            double zBack = NODE_Z[g + 1] * h * WALL_PX;
            double reach = 0;
            for (int j = g + 2; j < NODES; j++) {
                double z = NODE_Z[j] * h * WALL_PX;
                reach = Math.max(
                        reach, (z - zBack) * SHADOW_RATIO * Math.clamp(-facing, 0, 1) - (setback(row, j) - back));
            }
            return Math.clamp(reach / width, 0, 1);
        }

        /** The x the left wall's cast shadow reaches on the floor at {@code row} (the foot: none). */
        double floorShadow(double row) {
            double best = line(row);
            if (side != WallProjection.LEFT) {
                return best;
            }
            double h = height(row);
            for (int j = 1; j < NODES; j++) {
                double reach = NODE_Z[j] * h * WALL_PX * SHADOW_RATIO;
                double source = row - reach * SHADOW_DY;
                if (valid(source)) {
                    best = Math.max(best, x(source, j) + reach * SHADOW_DX);
                }
            }
            return best;
        }
    }

    /**
     * A butte: its foot polygon resampled every ~3 px, each step edge's ring (the convex polygon offset
     * inward, the intermediate rings wobbling, the last one the cap's edge), the perimeter coordinate, each
     * step's shade per perimeter step, and the rings as offset polygons of the foot's corners (the shadows).
     */
    static final class Butte {
        final int points;
        /** Per node and point: x and row. */
        final double[][] x;

        final double[][] row;
        /** Per point: the perimeter coordinate (the faces' texture rows). */
        final double[] perimeterAt;

        final double perimeter;
        /** Per segment and perimeter step (point i to i + 1). */
        final float[][] shade;
        /** Per node: the ring as a polygon of the foot's corners offset by its mean setback. */
        final double[][] cornerX;

        final double[][] cornerRow;
        final double minRow;
        final double maxRow;

        Butte(Terrain.Island foot, String look) {
            double[] px = foot.xs();
            double[] ps = foot.scrolls();
            int n = px.length;
            double area = 0;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                area += px[i] * ps[j] - px[j] * ps[i];
            }
            double orient = area > 0 ? 1 : -1;
            double[][] miter = new double[n][2];
            for (int i = 0; i < n; i++) {
                int p = (i - 1 + n) % n;
                int q = (i + 1) % n;
                double l0 = Math.hypot(px[i] - px[p], ps[i] - ps[p]);
                double l1 = Math.hypot(px[q] - px[i], ps[q] - ps[i]);
                double e0x = -(ps[i] - ps[p]) / l0 * orient;
                double e0y = (px[i] - px[p]) / l0 * orient;
                double e1x = -(ps[q] - ps[i]) / l1 * orient;
                double e1y = (px[q] - px[i]) / l1 * orient;
                double d = 1 + e0x * e1x + e0y * e1y;
                miter[i][0] = (e0x + e1x) / d;
                miter[i][1] = (e0y + e1y) / d;
            }
            int count = 0;
            for (int i = 0; i < n; i++) {
                int q = (i + 1) % n;
                count += Math.max(1, (int) Math.round(Math.hypot(px[q] - px[i], ps[q] - ps[i]) / 3));
            }
            points = count;
            double[] bx = new double[count];
            double[] bs = new double[count];
            double[] mx = new double[count];
            double[] my = new double[count];
            perimeterAt = new double[count];
            double p = 0;
            int k = 0;
            for (int i = 0; i < n; i++) {
                int q = (i + 1) % n;
                double seg = Math.hypot(px[q] - px[i], ps[q] - ps[i]);
                int steps = Math.max(1, (int) Math.round(seg / 3));
                for (int s = 0; s < steps; s++) {
                    double a = s / (double) steps;
                    bx[k] = px[i] + (px[q] - px[i]) * a;
                    bs[k] = ps[i] + (ps[q] - ps[i]) * a;
                    mx[k] = miter[i][0] * (1 - a) + miter[q][0] * a;
                    my[k] = miter[i][1] * (1 - a) + miter[q][1] * a;
                    perimeterAt[k] = p + seg * a;
                    k++;
                }
                p += seg;
            }
            perimeter = p;
            double scale = islandScale(look);
            double[] cum = new double[NODES];
            for (int g = 0; g < SEGMENTS; g++) {
                cum[g + 1] = cum[g] + SETBACK[g] * scale;
            }
            double total = cum[NODES - 1];
            long seed = "mesa".equals(look) ? 700 : 750;
            x = new double[NODES][count];
            row = new double[NODES][count];
            double[] previous = new double[count];
            for (int j = 0; j < NODES; j++) {
                for (int i = 0; i < count; i++) {
                    double s;
                    if (j == 0) {
                        s = 0;
                    } else if (j == NODES - 1) {
                        s = total;
                    } else {
                        double wobble = vnoise(perimeterAt[i], 20, seed + j) - 0.5;
                        s = Math.clamp(cum[j] * (1 + 0.5 * wobble), previous[i], total);
                    }
                    x[j][i] = bx[i] + mx[i] * s;
                    row[j][i] = bs[i] + my[i] * s;
                    previous[i] = s;
                }
            }
            shade = new float[SEGMENTS][count];
            for (int g = 0; g < SEGMENTS; g++) {
                double dz = (NODE_Z[g + 1] - NODE_Z[g]) * WALL_PX;
                for (int i = 0; i < count; i++) {
                    int q = (i + 1) % count;
                    double ax = x[g + 1][i] - x[g][i];
                    double ay = row[g + 1][i] - row[g][i];
                    double cx = x[g][q] - x[g][i];
                    double cy = row[g][q] - row[g][i];
                    // (ax, ay, dz) × (cx, cy, 0), turned up
                    double nx = -dz * cy;
                    double ny = dz * cx;
                    double nz = ax * cy - ay * cx;
                    if (nz < 0) {
                        nx = -nx;
                        ny = -ny;
                        nz = -nz;
                    }
                    shade[g][i] = shade(nx, ny, nz);
                }
            }
            cornerX = new double[NODES][n];
            cornerRow = new double[NODES][n];
            double low = Double.POSITIVE_INFINITY;
            double high = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < NODES; j++) {
                for (int i = 0; i < n; i++) {
                    cornerX[j][i] = px[i] + miter[i][0] * cum[j];
                    cornerRow[j][i] = ps[i] + miter[i][1] * cum[j];
                }
            }
            for (double s : ps) {
                low = Math.min(low, s);
                high = Math.max(high, s);
            }
            minRow = low;
            maxRow = high;
        }
    }
}
