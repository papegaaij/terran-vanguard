package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
import vanguard.content.TerrainBlock;
import vanguard.sim.PlayField;
import vanguard.sim.Terrain;

/**
 * M7 part C: a canyon's rock drawn in perspective (design/campaign Level 16, Rendering; camera model b,
 * MA.1 of round 36), seen from above like the Grand Canyon (round 40, {@link CanyonProfile}): each wall
 * descends from the plateau's rim to its foot (the collision line, scale 1.0) in steps, cliff bands and
 * talus benches, every step's edge set back toward the rock, so the rim is ragged (side canyons biting back
 * into the plateau) and the strata run round it as contour bands; each frame the steps are projected round
 * the play field's projection centre (240, 297 px below the top edge) with {@link TowerProjection}'s camera
 * model, the rim at the plateau's height ({@value #PLATEAU_HEIGHT} camera units, scale 1.25), so the walls
 * lean outward and turn as they scroll. Beyond the rim {@link #leftClip}/{@link #rightClip} bound the
 * plateau layer, which the {@link Backdrop} draws over the walls (it is higher than any rock below the rim).
 * The rock islands (Level 16's fork mesa and rock pillar) are buttes: the same steps as rings round a flat
 * cap at 1.25. The walls' 24 px foot bands (the crag line at the collision line, the scree) lie on the floor
 * at 1.0 without lean, and the left wall's and the buttes' cast shadows (key light from the upper left) fall
 * on the floor and the foot bands. All of it is drawn with the ground's pieces, so ground units, bullets and
 * the flyers' shadows stay over it (the tower rule). The steps are drawn in the order of their height (a
 * higher point is nearer the camera), every wall's segment g before any segment g + 1.
 *
 * <p>The geometry comes from the simulation's {@link Terrain} only (never a placed art piece, so art and
 * collision cannot drift): the foot is the collision line, the steps only ever lie on the rock's side. The
 * walls' drawn heights (the data's optional third value per wall point, 0 at the canyon mouth's ramp to 1),
 * the islands' looks, the cliff habitats' face patches and the plateau pieces the rim keeps clear are
 * presentation, given as {@link Looks}.
 *
 * <p>The art contract is {@link CanyonProfile}'s (tools/art/backdrop_l16.py): {@code canyon-wall} for both
 * walls, {@code <look>-face} for the buttes, shaded per step by its normal; {@code wall-foot} (24 px) has its
 * column 0 on the collision line, mirrored for the right wall; a butte's top is its cap × 1.25 on the canvas
 * of its foot's polygon × 1.25, far end first; a face patch covers the red middle cliff's segment.
 *
 * <p>Cost: per wall on screen about 270 cells of {@value CanyonProfile#SEGMENTS} to 9 quads, the buttes'
 * perimeter steps × 6, 540 one-pixel rows of foot band and shadow, the plateau's rows (in {@link
 * Backdrop}); nothing is allocated per frame.
 */
final class WallProjection {
    /** The plateau's height in camera units: its edge is drawn at 6 ÷ (6 − 1.2) = 1.25. */
    static final double PLATEAU_HEIGHT = 1.2;
    /** The plateau's scale. */
    static final double PLATEAU_SCALE = 1.25;
    /** Rows of the ground per cell along a wall. */
    static final int CELL = 2;
    /** The foot band's width, px. */
    static final int FOOT_WIDTH = 24;
    /** The most edges per wall in one frame (cells plus their bends, ends and patch boundaries). */
    static final int MAX_EDGES = 512;

    static final int LEFT = 0;
    static final int RIGHT = 1;

    private static final int SEGMENTS = CanyonProfile.SEGMENTS;
    private static final int NODES = CanyonProfile.NODES;
    private static final float CX = (float) BackdropData.Tower.CENTRE_X;
    private static final float CY = (float) BackdropData.Tower.CENTRE_Y;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    /** A row step for the slope tests at a cell's ends. */
    private static final double DELTA = 1e-3;
    /** Where the cast shadows' sweep samples a butte's rings. */
    private static final double[] SWEEP = {0, 0.5, 1};

    /**
     * A wall's drawn height along its rows, 0 (flat: the floor runs on, the canyon mouth) to 1 (its full
     * rise to the plateau), linear between the rows, the end values beyond them; {@link #FULL} without
     * points. Presentation only (the level data's third value per wall point).
     *
     * @param rows the rows (the wall points' scrolls), increasing
     * @param heights the height at each, 0 to 1
     */
    record Heights(double[] rows, double[] heights) {
        static final Heights FULL = new Heights(new double[0], new double[0]);

        Heights {
            rows = rows.clone();
            heights = heights.clone();
            if (rows.length != heights.length) {
                throw new IllegalArgumentException("a height per row");
            }
        }

        /** The height at {@code row}. */
        double at(double row) {
            int n = rows.length;
            if (n == 0) {
                return 1;
            }
            if (row <= rows[0]) {
                return heights[0];
            }
            if (row >= rows[n - 1]) {
                return heights[n - 1];
            }
            for (int i = 1; i < n; i++) {
                if (row <= rows[i]) {
                    double u = (row - rows[i - 1]) / (rows[i] - rows[i - 1]);
                    return heights[i - 1] + (heights[i] - heights[i - 1]) * u;
                }
            }
            return heights[n - 1];
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Heights h && Arrays.equals(rows, h.rows) && Arrays.equals(heights, h.heights);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(rows) + Arrays.hashCode(heights);
        }

        @Override
        public String toString() {
            return "Heights" + Arrays.toString(rows) + Arrays.toString(heights);
        }
    }

    /**
     * A rock island's look: its foot (the polygon the simulation's {@link Terrain} was built from) and
     * its art name, whose {@code <look>-face} and {@code <look>-top} images the backdrop holds.
     */
    record IslandLook(Terrain.Island foot, String look) {}

    /**
     * A face patch (a burnt cliff habitat): its image drawn on a wall's red middle cliff in place of the
     * wall texture's rows round {@code row}.
     *
     * @param side {@link #LEFT} or {@link #RIGHT}
     * @param row the terrain row of its middle
     * @param look its image's name in the backdrop ({@code cliff-habitat})
     */
    record FacePatch(int side, double row, String look) {}

    /**
     * The presentation of a canyon beyond its collision lines: the walls' heights, the islands' looks, the
     * face patches and the plateau pieces the rims keep clear.
     */
    record Looks(
            Heights left,
            Heights right,
            List<IslandLook> islands,
            List<FacePatch> patches,
            List<CanyonProfile.Cap> caps) {
        /** Full walls, no islands drawn, no patches. */
        static final Looks PLAIN = new Looks(Heights.FULL, Heights.FULL, List.of(), List.of(), List.of());

        Looks {
            islands = List.copyOf(islands);
            patches = List.copyOf(patches);
            caps = List.copyOf(caps);
        }

        /**
         * M7 part C: a level's canyon looks from the same {@link TerrainBlock} the simulation's terrain is
         * built from: the walls' heights (the points' third value), the islands' feet (equal to the
         * simulation's {@link Terrain.Island}s) and looks, the {@link #FACE_PATCHES} placed in the backdrop
         * block (left of the field's middle: the left wall; the centre's terrain row from {@code t}) and the
         * plateau layer's pieces (the rim keeps clear of them: round 40); {@link #PLAIN} without terrain.
         */
        static Looks of(LevelData level) {
            if (level.terrain().isEmpty()) {
                return PLAIN;
            }
            TerrainBlock block = level.terrain().get();
            List<IslandLook> islands = new ArrayList<>();
            for (TerrainBlock.Island island : block.islandList()) {
                double[] xs = new double[island.points().size()];
                double[] scrolls = new double[xs.length];
                for (int i = 0; i < xs.length; i++) {
                    xs[i] = island.points().get(i).x();
                    scrolls[i] = island.points().get(i).y();
                }
                islands.add(new IslandLook(new Terrain.Island(xs, scrolls), island.look()));
            }
            List<FacePatch> patches = new ArrayList<>();
            List<CanyonProfile.Cap> caps = new ArrayList<>();
            for (BackdropData.PlacedPiece placed : level.backdrop().placements()) {
                int side = placed.x() < PlayField.WIDTH / 2.0 ? LEFT : RIGHT;
                if (FACE_PATCHES.contains(placed.piece())) {
                    double row = TerrainBlock.row(level.pieceCentre(placed), 0);
                    patches.add(new FacePatch(side, row, placed.piece()));
                }
                BackdropData.Piece spec = level.backdrop().pieces().get(placed.piece());
                if (spec.layer() == BackdropLayer.PLATEAU) {
                    double centre = level.pieceCentre(placed);
                    double half = spec.size().height() / 2;
                    double halfWidth = spec.size().width() / 2;
                    caps.add(new CanyonProfile.Cap(
                            side,
                            (centre - half - CanyonProfile.PLATEAU_ROW0) / PLATEAU_SCALE,
                            (centre + half - CanyonProfile.PLATEAU_ROW0) / PLATEAU_SCALE,
                            placed.x() + (side == LEFT ? halfWidth : -halfWidth)));
                }
            }
            return new Looks(
                    heights(block.left().orElse(List.of())),
                    heights(block.right().orElse(List.of())),
                    islands,
                    patches,
                    caps);
        }

        private static Heights heights(List<TerrainBlock.WallPoint> points) {
            double[] rows = new double[points.size()];
            double[] values = new double[rows.length];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = points.get(i).scroll();
                values[i] = points.get(i).wallHeight();
            }
            return new Heights(rows, values);
        }
    }

    /**
     * M7 part C: the backdrop pieces drawn on a wall's face ({@link FacePatch}) instead of flat on their
     * layer: Level 16's burnt cliff habitats.
     */
    static final Set<String> FACE_PATCHES = Set.of("cliff-habitat");

    /** An island with its look and images: the face texture and the top's slices, far end first. */
    record Island(Terrain.Island foot, String look, TextureRegion face, List<? extends TextureRegion> top) {}

    /** A face patch with its image. */
    record Patch(int side, double from, double to, TextureRegion image) {}

    /**
     * M8 part C: an ice shaft's walls (Level 23, tools/art/backdrop_l23.py; the {@code canyon-wall} convention): the
     * wet wall and foot below the waterline (from terrain row {@code wetRow} up), the collar strips' glow added
     * unshaded over the walls ({@link #drawGlow}, after the grade), smooth steps (no side canyons).
     *
     * @param wetWall {@code ice-wall-wet} (null: the dry wall throughout)
     * @param wetFoot {@code ice-wall-foot-wet} (null: the dry foot)
     * @param glow {@code ice-wall-glow} (null: none)
     * @param wetRow the terrain row from which the walls are wet ({@code +∞}: never)
     */
    record Ice(TextureRegion wetWall, TextureRegion wetFoot, TextureRegion glow, double wetRow) {}

    private final Terrain terrain;
    private final TextureRegion wall;
    private final TextureRegion foot;
    /** M8 part C: an ice shaft's wet walls and glow; null for a canyon. */
    private final Ice ice;

    private final TextureRegion pixel;
    private final Island[] islands;
    private final CanyonProfile.Butte[] buttes;
    private final Patch[] patches;
    /** Per wall its steps; null where the terrain has no such wall. */
    private final CanyonProfile.Steps[] steps = new CanyonProfile.Steps[2];
    /** Per butte its foot's bounds. */
    private final double[] islandMinX;

    /** Per wall and edge, this frame: the row, the foot (play-field px, y up), the height. */
    private final double[][] edgeRow = new double[2][MAX_EDGES];

    private final double[][] edgeX = new double[2][MAX_EDGES];
    private final double[][] edgeH = new double[2][MAX_EDGES];
    /** Per wall, edge and node: its x (play-field px, before the projection). */
    private final double[][] nodeX = new double[2][MAX_EDGES * NODES];
    /** Per wall, edge and segment: its shade, its texture row, a bench's share in cast shadow. */
    private final float[][] segShade = new float[2][MAX_EDGES * SEGMENTS];

    private final double[][] segV = new double[2][MAX_EDGES * SEGMENTS];
    private final float[][] segCast = new float[2][MAX_EDGES * SEGMENTS];
    private final int[] edges = new int[2];
    /** Per screen row, this frame: the left wall's rim (the plateau left of it), the right wall's; NaN: no wall. */
    private final float[] leftClip = new float[HEIGHT];

    private final float[] rightClip = new float[HEIGHT];
    private long scroll;
    private final float[] quad = new float[20];
    private final double[] normal = new double[3];
    private final TextureRegion strip = new TextureRegion();

    /**
     * @param terrain the simulation's rock
     * @param looks the walls' heights, the face patches' rows and the plateau's caps (the islands' and
     *     patches' looks are resolved into {@code islands} and {@code patches})
     * @param wall the {@code canyon-wall} texture (null: geometry only, for the tests)
     * @param foot the {@code wall-foot} texture
     * @param pixel a white pixel (the shadows)
     */
    WallProjection(
            Terrain terrain,
            Looks looks,
            TextureRegion wall,
            TextureRegion foot,
            TextureRegion pixel,
            List<Island> islands,
            List<Patch> patches) {
        this(terrain, looks, wall, foot, pixel, islands, patches, null);
    }

    /** As above; M8 part C: {@code ice} an ice shaft's wet walls and glow (null: a canyon). */
    WallProjection(
            Terrain terrain,
            Looks looks,
            TextureRegion wall,
            TextureRegion foot,
            TextureRegion pixel,
            List<Island> islands,
            List<Patch> patches,
            Ice ice) {
        this.ice = ice;
        this.terrain = terrain;
        this.wall = wall;
        this.foot = foot;
        this.pixel = pixel;
        this.islands = islands.toArray(Island[]::new);
        this.patches = patches.toArray(Patch[]::new);
        int n = this.islands.length;
        buttes = new CanyonProfile.Butte[n];
        islandMinX = new double[n];
        for (int i = 0; i < n; i++) {
            buttes[i] = new CanyonProfile.Butte(this.islands[i].foot(), this.islands[i].look());
            islandMinX[i] = Arrays.stream(this.islands[i].foot().xs()).min().orElseThrow();
        }
        Heights[] heights = {looks.left(), looks.right()};
        for (int side = LEFT; side <= RIGHT; side++) {
            double[] range = range(side);
            if (range != null) {
                final int s = side;
                double[] rows = looks.patches().stream()
                        .filter(p -> p.side() == s)
                        .mapToDouble(FacePatch::row)
                        .toArray();
                steps[side] = new CanyonProfile.Steps(
                        terrain, side, heights[side], range[0], range[1], rows, looks.caps(), ice != null);
            }
        }
    }

    /** A wall's first and last row (null: no wall), found on the terrain's line. */
    private double[] range(int side) {
        double any = Double.NaN;
        for (double row = -20_000; row <= 400_000; row += 8) {
            if (!Double.isNaN(line(side, row))) {
                any = row;
                break;
            }
        }
        if (Double.isNaN(any)) {
            return null;
        }
        double last = any;
        while (!Double.isNaN(line(side, last + 8)) && last < 400_000) {
            last += 8;
        }
        return new double[] {boundary(side, any - 8, any), boundary(side, last + 8, last)};
    }

    /** The wall's end between a row without it ({@code off}) and one with it ({@code on}), by bisection. */
    private double boundary(int side, double off, double on) {
        for (int i = 0; i < 60; i++) {
            double mid = (off + on) / 2;
            if (Double.isNaN(line(side, mid))) {
                off = mid;
            } else {
                on = mid;
            }
        }
        return on;
    }

    /** The scale of a point a share {@code u} up a face of height share {@code height}: 6 ÷ (6 − 1.2 h u). */
    static double scale(double u, double height) {
        return BackdropData.Tower.scaleAt(PLATEAU_HEIGHT * height * u);
    }

    /** A play-field x drawn at scale {@code k} round the projection centre. */
    static double projectX(double x, double k) {
        return BackdropData.Tower.project(x, CX, k);
    }

    /** A play-field y (up) drawn at scale {@code k} round the projection centre. */
    static double projectY(double y, double k) {
        return BackdropData.Tower.project(y, CY, k);
    }

    /** A wall's collision line at {@code row}, px; NaN where it has none. */
    private double line(int side, double row) {
        return side == LEFT ? terrain.leftAt(row) : terrain.rightAt(row);
    }

    /** A wall's steps (null: none). */
    CanyonProfile.Steps steps(int side) {
        return steps[side];
    }

    /**
     * Lays out the frame at the ground's whole-pixel scroll {@code groundScroll}: each wall's edges from
     * the bottom edge's row to the top edge's (every {@value #CELL} rows, at its bends, ends and patch
     * boundaries) with their steps, and the plateau's bounds per screen row.
     */
    void prepare(long groundScroll) {
        scroll = groundScroll;
        Arrays.fill(leftClip, Float.NaN);
        Arrays.fill(rightClip, Float.NaN);
        for (int side = LEFT; side <= RIGHT; side++) {
            edges[side] = 0;
            if (steps[side] == null) {
                continue;
            }
            double bottom = Terrain.row(groundScroll, 0);
            double top = Terrain.row(groundScroll, HEIGHT);
            double first = Math.floor(bottom / CELL) * CELL;
            for (double a = first; a < top; a += CELL) {
                double b = a + CELL;
                if (a == first) {
                    add(side, a);
                }
                split(side, a, b, 2);
                add(side, b);
            }
            clips(side);
        }
    }

    /** Adds the edges strictly inside the cell (a, b): the wall's end or start, its bends, the patches' bounds. */
    private void split(int side, double a, double b, int depth) {
        double xa = line(side, a);
        double xb = line(side, b);
        if (Double.isNaN(xa) != Double.isNaN(xb)) {
            // The wall starts or ends in the cell: its first or last row, by bisection.
            double lo = a;
            double hi = b;
            for (int i = 0; i < 40; i++) {
                double mid = (lo + hi) / 2;
                if (Double.isNaN(line(side, mid)) == Double.isNaN(xa)) {
                    lo = mid;
                } else {
                    hi = mid;
                }
            }
            double end = Double.isNaN(xa) ? hi : lo;
            if (end > a && end < b) {
                if (Double.isNaN(xa)) {
                    add(side, end);
                    split(side, end, b, depth);
                } else {
                    split(side, a, end, depth);
                    add(side, end);
                }
                return;
            }
        }
        for (Patch patch : patches) {
            if (patch.side() != side) {
                continue;
            }
            double bound = patch.from() > a && patch.from() < b ? patch.from() : patch.to();
            if (bound > a && bound < b) {
                split(side, a, bound, depth);
                add(side, bound);
                split(side, bound, b, depth);
                return;
            }
        }
        if (Double.isNaN(xa) || Double.isNaN(xb) || depth == 0) {
            return;
        }
        // A bend: the lines through the cell's two ends meet inside it.
        double sa = (line(side, a + DELTA) - xa) / DELTA;
        double sb = (xb - line(side, b - DELTA)) / DELTA;
        if (Double.isNaN(sa) || Double.isNaN(sb) || Math.abs(sa - sb) < 1e-6) {
            return;
        }
        double bend = (xb - xa + sa * a - sb * b) / (sa - sb);
        if (bend > a + 1e-6 && bend < b - 1e-6) {
            split(side, a, bend, depth - 1);
            add(side, bend);
            split(side, bend, b, depth - 1);
        }
    }

    private void add(int side, double row) {
        int e = edges[side];
        if (e >= MAX_EDGES) {
            return;
        }
        edgeRow[side][e] = row;
        double x = line(side, row);
        edgeX[side][e] = x;
        CanyonProfile.Steps wall = steps[side];
        edgeH[side][e] = wall.height(row);
        double sign = side == LEFT ? -1 : 1;
        for (int j = 0; j < NODES; j++) {
            nodeX[side][e * NODES + j] = x + sign * wall.setback(row, j);
        }
        if (!Double.isNaN(x)) {
            for (int g = 0; g < SEGMENTS; g++) {
                wall.normal(row, g, normal);
                segShade[side][e * SEGMENTS + g] = CanyonProfile.shade(normal[0], normal[1], normal[2]);
                segV[side][e * SEGMENTS + g] = wall.v(row, g);
                segCast[side][e * SEGMENTS + g] = (float) wall.cast(row, g, normal);
            }
        }
        edges[side] = e + 1;
    }

    /** The plateau's bound per screen row: the rim's x where it crosses the row's middle. */
    private void clips(int side) {
        float[] clip = side == LEFT ? leftClip : rightClip;
        for (int e = 1; e < edges[side]; e++) {
            if (!cell(side, e)) {
                continue;
            }
            double rx0 = rimX(side, e - 1);
            double ry0 = rimY(side, e - 1);
            double rx1 = rimX(side, e);
            double ry1 = rimY(side, e);
            double low = Math.min(ry0, ry1);
            double high = Math.max(ry0, ry1);
            if (high - low < 1e-9) {
                continue;
            }
            int from = Math.max(0, (int) Math.ceil(low - 0.5));
            int to = Math.min(HEIGHT - 1, (int) Math.floor(high - 0.5));
            for (int y = from; y <= to; y++) {
                double t = (y + 0.5 - ry0) / (ry1 - ry0);
                float x = (float) (rx0 + (rx1 - rx0) * t);
                float old = clip[y];
                clip[y] = Float.isNaN(old) ? x : side == LEFT ? Math.max(old, x) : Math.min(old, x);
            }
        }
    }

    /** Whether edges {@code e − 1} and {@code e} of a wall bound a drawn cell (the wall exists at both). */
    boolean cell(int side, int e) {
        return !Double.isNaN(edgeX[side][e - 1]) && !Double.isNaN(edgeX[side][e]);
    }

    int edgeCount(int side) {
        return edges[side];
    }

    double edgeRow(int side, int e) {
        return edgeRow[side][e];
    }

    /** The foot of edge {@code e}, play-field px. */
    double footX(int side, int e) {
        return edgeX[side][e];
    }

    /** The foot of edge {@code e}, px up from the play field's bottom edge. */
    double footY(int side, int e) {
        return Terrain.y(scroll, edgeRow[side][e]);
    }

    /** Node {@code j}'s x (the step edge, before the projection) at edge {@code e}, play-field px. */
    double nodeX(int side, int e, int j) {
        return nodeX[side][e * NODES + j];
    }

    /** Node {@code j} at edge {@code e} on screen, play-field px. */
    double faceX(int side, int e, int j) {
        return projectX(nodeX(side, e, j), scale(CanyonProfile.NODE_Z[j], edgeH[side][e]));
    }

    double faceY(int side, int e, int j) {
        return projectY(footY(side, e), scale(CanyonProfile.NODE_Z[j], edgeH[side][e]));
    }

    /** The plateau's rim at edge {@code e}. */
    double rimX(int side, int e) {
        return faceX(side, e, NODES - 1);
    }

    double rimY(int side, int e) {
        return faceY(side, e, NODES - 1);
    }

    /** Segment {@code g}'s shade at edge {@code e}. */
    float shade(int side, int e, int g) {
        return segShade[side][e * SEGMENTS + g];
    }

    /** Bench {@code g}'s share in cast shadow at edge {@code e}. */
    float cast(int side, int e, int g) {
        return segCast[side][e * SEGMENTS + g];
    }

    /** The plateau is drawn left of this x on screen row {@code y} (play-field px); NaN: no left wall there. */
    float leftClip(int y) {
        return leftClip[y];
    }

    /** The plateau is drawn right of this x on screen row {@code y}; NaN: no right wall there. */
    float rightClip(int y) {
        return rightClip[y];
    }

    /**
     * The floor's part, under the walls and the plateau: the foot bands at 1.0 along both walls, then the
     * left wall's and the buttes' cast shadows over them and the floor.
     */
    void drawFloor(SpriteBatch batch) {
        Color tint = batch.getColor();
        float lit = batch.getPackedColor();
        int tileRows = foot.getRegionHeight();
        for (int y = 0; y < HEIGHT; y++) {
            double row = Terrain.row(scroll, y + 0.5);
            int texRow = tileRows - 1 - (int) Math.floorMod((long) Math.floor(row), (long) tileRows);
            double left = terrain.leftAt(row);
            TextureRegion foot = footAt(row);
            if (!Double.isNaN(left)) {
                strip.setRegion(foot, 0, texRow, FOOT_WIDTH, 1);
                batch.draw(strip, X0 + Math.round(left), y);
            }
            double right = terrain.rightAt(row);
            if (!Double.isNaN(right)) {
                strip.setRegion(foot, 0, texRow, FOOT_WIDTH, 1);
                // Mirrored: its column 0 on the collision line, the band running left onto the floor.
                batch.draw(strip, X0 + Math.round(right), y, -FOOT_WIDTH, 1);
            }
        }
        batch.setColor(0, 0, 0, CanyonProfile.FLOOR_SHADOW * tint.a);
        for (int y = 0; y < HEIGHT; y++) {
            double row = Terrain.row(scroll, y + 0.5);
            CanyonProfile.Steps left = steps[LEFT];
            if (left != null && left.valid(row)) {
                double footX = left.line(row);
                double edge = left.floorShadow(row);
                if (edge > footX) {
                    span(batch, footX, edge, y);
                }
            }
            for (CanyonProfile.Butte butte : buttes) {
                double a = Double.POSITIVE_INFINITY;
                double b = Double.NEGATIVE_INFINITY;
                if (row < butte.minRow - CanyonProfile.WALL_PX || row > butte.maxRow + CanyonProfile.WALL_PX) {
                    continue;
                }
                for (int j = 0; j < NODES; j++) {
                    double reach = CanyonProfile.NODE_Z[j] * CanyonProfile.WALL_PX * CanyonProfile.SHADOW_RATIO;
                    for (double f : SWEEP) {
                        double at = row - reach * CanyonProfile.SHADOW_DY * f;
                        double dx = reach * CanyonProfile.SHADOW_DX * f;
                        a = Math.min(a, crossing(butte.cornerX[j], butte.cornerRow[j], at, true) + dx);
                        b = Math.max(b, crossing(butte.cornerX[j], butte.cornerRow[j], at, false) + dx);
                    }
                }
                if (a < b) {
                    span(batch, a, b, y);
                }
            }
        }
        batch.setPackedColor(lit);
    }

    private void span(SpriteBatch batch, double from, double to, int y) {
        long a = Math.max(0, Math.round(from));
        long b = Math.min(WIDTH, Math.round(to));
        if (b > a) {
            batch.draw(pixel, X0 + a, y, b - a, 1);
        }
    }

    /** A polygon's leftmost ({@code first}) or rightmost crossing of {@code row} (±∞ off it). */
    private static double crossing(double[] xs, double[] rows, double row, boolean first) {
        double best = first ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        int n = xs.length;
        for (int c = 0; c < n; c++) {
            int d = (c + 1) % n;
            double low = Math.min(rows[c], rows[d]);
            double high = Math.max(rows[c], rows[d]);
            if (row < low || row > high || high == low) {
                continue;
            }
            double x = xs[c] + (xs[d] - xs[c]) * (row - rows[c]) / (rows[d] - rows[c]);
            best = first ? Math.min(best, x) : Math.max(best, x);
        }
        return best;
    }

    /** Both walls' steps, segment by segment from the foot up (the plateau layer goes over them next). */
    void drawWalls(SpriteBatch batch) {
        Color tint = batch.getColor();
        float lit = batch.getPackedColor();
        for (int g = 0; g < SEGMENTS; g++) {
            for (int side = LEFT; side <= RIGHT; side++) {
                drawSegment(batch, side, g, tint);
            }
        }
        batch.setPackedColor(lit);
    }

    private void drawSegment(SpriteBatch batch, int side, int g, Color tint) {
        float u0 = (float) CanyonProfile.NODE_U[g];
        float u1 = (float) CanyonProfile.NODE_U[g + 1];
        float dark = colour(tint, CanyonProfile.SHADOWED);
        for (int e = 1; e < edges[side]; e++) {
            if (!cell(side, e)) {
                continue;
            }
            double r0 = edgeRow[side][e - 1];
            double r1 = edgeRow[side][e];
            double mid = (r0 + r1) / 2;
            TextureRegion image = wallAt(mid);
            float iu0 = image.getU() + (image.getU2() - image.getU()) * u0;
            float iu1 = image.getU() + (image.getU2() - image.getU()) * u1;
            float v0;
            float v1;
            Patch patch = g == CanyonProfile.HABITAT_SEGMENT ? patchAt(side, mid) : null;
            if (patch != null) {
                image = patch.image();
                iu0 = image.getU();
                iu1 = image.getU2();
                double period = patch.to() - patch.from();
                v0 = texV(image, 1 - (r0 - patch.from()) / period);
                v1 = texV(image, 1 - (r1 - patch.from()) / period);
            } else {
                double va = segV[side][(e - 1) * SEGMENTS + g];
                double vb = segV[side][e * SEGMENTS + g];
                double origin = Math.floor(Math.min(va, vb) / CanyonProfile.TEX_PERIOD) * CanyonProfile.TEX_PERIOD;
                double h = image.getRegionHeight();
                v0 = texV(image, 1 - (va - origin) / h);
                v1 = texV(image, 1 - (vb - origin) / h);
            }
            float c0 = colour(tint, shade(side, e - 1, g));
            float c1 = colour(tint, shade(side, e, g));
            if (CanyonProfile.CLIFF[g]) {
                quad(batch, image, side, e, g, 0, 0, 1, 1, c0, c1, c0, c1, iu0, iu1, v0, v1);
                continue;
            }
            double f0 = cast(side, e - 1, g);
            double f1 = cast(side, e, g);
            if (f0 < 1 - 1e-3 || f1 < 1 - 1e-3) {
                quad(batch, image, side, e, g, 0, 0, 1 - f0, 1 - f1, c0, c1, c0, c1, iu0, iu1, v0, v1);
            }
            if (f0 > 1e-3 || f1 > 1e-3) {
                quad(batch, image, side, e, g, 1 - f0, 1 - f1, 1, 1, dark, dark, dark, dark, iu0, iu1, v0, v1);
            }
        }
    }

    /** M8 part C: the wall texture at terrain row {@code row}: an ice shaft's wet one below the waterline. */
    private TextureRegion wallAt(double row) {
        return ice != null && ice.wetWall() != null && row >= ice.wetRow() ? ice.wetWall() : wall;
    }

    /** M8 part C: the foot band at terrain row {@code row}: an ice shaft's wet one below the waterline. */
    private TextureRegion footAt(double row) {
        return ice != null && ice.wetFoot() != null && row >= ice.wetRow() ? ice.wetFoot() : foot;
    }

    /**
     * M8 part C: an ice shaft's collar strips glowing over its walls (the {@code ice-wall-glow} texture on the same
     * quads, unshaded, additive), drawn after the under-water grade so they glow through it; nothing for a canyon. The
     * frame's layout is {@link #prepare}'s.
     */
    void drawGlow(SpriteBatch batch) {
        if (ice == null || ice.glow() == null) {
            return;
        }
        float lit = batch.getPackedColor();
        float white = Color.WHITE_FLOAT_BITS;
        TextureRegion image = ice.glow();
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
        for (int g = 0; g < SEGMENTS; g++) {
            float iu0 = image.getU() + (image.getU2() - image.getU()) * (float) CanyonProfile.NODE_U[g];
            float iu1 = image.getU() + (image.getU2() - image.getU()) * (float) CanyonProfile.NODE_U[g + 1];
            for (int side = LEFT; side <= RIGHT; side++) {
                for (int e = 1; e < edges[side]; e++) {
                    if (!cell(side, e)) {
                        continue;
                    }
                    double va = segV[side][(e - 1) * SEGMENTS + g];
                    double vb = segV[side][e * SEGMENTS + g];
                    double origin = Math.floor(Math.min(va, vb) / CanyonProfile.TEX_PERIOD) * CanyonProfile.TEX_PERIOD;
                    double h = image.getRegionHeight();
                    float v0 = texV(image, 1 - (va - origin) / h);
                    float v1 = texV(image, 1 - (vb - origin) / h);
                    quad(batch, image, side, e, g, 0, 0, 1, 1, white, white, white, white, iu0, iu1, v0, v1);
                }
            }
        }
        batch.setBlendFunction(
                com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setPackedColor(lit);
    }

    private Patch patchAt(int side, double row) {
        for (Patch patch : patches) {
            if (patch.side() == side && row > patch.from() && row < patch.to()) {
                return patch;
            }
        }
        return null;
    }

    private static float texV(TextureRegion image, double share) {
        return image.getV() + (image.getV2() - image.getV()) * (float) share;
    }

    /**
     * One quad of segment {@code g} between edges {@code e − 1} and {@code e}: from share {@code a0}/{@code a1}
     * up the segment (at the two edges) to {@code b0}/{@code b1}, its colours at the four corners.
     */
    private void quad(
            SpriteBatch batch,
            TextureRegion image,
            int side,
            int e,
            int g,
            double a0,
            double a1,
            double b0,
            double b1,
            float ca0,
            float ca1,
            float cb0,
            float cb1,
            float u0,
            float u1,
            float v0,
            float v1) {
        float ua0 = u0 + (u1 - u0) * (float) a0;
        float ua1 = u0 + (u1 - u0) * (float) a1;
        float ub0 = u0 + (u1 - u0) * (float) b0;
        float ub1 = u0 + (u1 - u0) * (float) b1;
        corner(0, side, e - 1, g, a0, ca0, ua0, v0);
        corner(1, side, e - 1, g, b0, cb0, ub0, v0);
        corner(2, side, e, g, b1, cb1, ub1, v1);
        corner(3, side, e, g, a1, ca1, ua1, v1);
        batch.draw(image.getTexture(), quad, 0, quad.length);
    }

    /** Quad corner {@code i}: the point a share {@code t} up segment {@code g} at edge {@code e}. */
    private void corner(int i, int side, int e, int g, double t, float colour, float u, float v) {
        double x = nodeX(side, e, g) + (nodeX(side, e, g + 1) - nodeX(side, e, g)) * t;
        double z = CanyonProfile.NODE_Z[g] + (CanyonProfile.NODE_Z[g + 1] - CanyonProfile.NODE_Z[g]) * t;
        double k = scale(z, edgeH[side][e]);
        vertex(i, projectX(x, k), projectY(footY(side, e), k), colour, u, v);
    }

    /** The buttes over the plateau: their steps segment by segment from the foot up, then their caps. */
    void drawIslands(SpriteBatch batch) {
        Color tint = batch.getColor();
        float lit = batch.getPackedColor();
        double kTop = scale(1, 1);
        for (int g = 0; g < SEGMENTS; g++) {
            for (int i = 0; i < islands.length; i++) {
                if (onScreen(i, kTop)) {
                    drawButteSegment(batch, i, g, tint);
                }
            }
        }
        batch.setPackedColor(lit);
        for (int i = 0; i < islands.length; i++) {
            if (!onScreen(i, kTop)) {
                continue;
            }
            // The cap: its slices over the foot polygon's box × 1.25 round the centre, from the far end down.
            long left = Math.round(projectX(islandMinX[i], kTop));
            long edge = Math.round(projectY(Terrain.y(scroll, buttes[i].maxRow), kTop));
            for (TextureRegion slice : islands[i].top()) {
                edge -= slice.getRegionHeight();
                batch.draw(slice, X0 + left, edge);
            }
        }
    }

    private boolean onScreen(int i, double k) {
        double top = Terrain.y(scroll, buttes[i].maxRow);
        double bottom = Terrain.y(scroll, buttes[i].minRow);
        return Math.min(bottom, projectY(bottom, k)) <= HEIGHT && Math.max(top, projectY(top, k)) >= 0;
    }

    private void drawButteSegment(SpriteBatch batch, int i, int g, Color tint) {
        CanyonProfile.Butte butte = buttes[i];
        TextureRegion face = islands[i].face();
        double h = face.getRegionHeight();
        double period = h - CanyonProfile.FACE_PAD;
        float u0 = face.getU() + (face.getU2() - face.getU()) * (float) CanyonProfile.NODE_U[g];
        float u1 = face.getU() + (face.getU2() - face.getU()) * (float) CanyonProfile.NODE_U[g + 1];
        double k0 = scale(CanyonProfile.NODE_Z[g], 1);
        double k1 = scale(CanyonProfile.NODE_Z[g + 1], 1);
        int n = butte.points;
        for (int p = 0; p < n; p++) {
            int q = (p + 1) % n;
            double pa = butte.perimeterAt[p];
            double pb = q == 0 ? butte.perimeter : butte.perimeterAt[q];
            double origin = Math.floor(pa / period) * period;
            float va = texV(face, 1 - (pa - origin) / h);
            float vb = texV(face, 1 - (pb - origin) / h);
            float colour = colour(tint, butte.shade[g][p]);
            double ya = Terrain.y(scroll, butte.row[g][p]);
            double yb = Terrain.y(scroll, butte.row[g][q]);
            double ya1 = Terrain.y(scroll, butte.row[g + 1][p]);
            double yb1 = Terrain.y(scroll, butte.row[g + 1][q]);
            vertex(0, projectX(butte.x[g][p], k0), projectY(ya, k0), colour, u0, va);
            vertex(1, projectX(butte.x[g + 1][p], k1), projectY(ya1, k1), colour, u1, va);
            vertex(2, projectX(butte.x[g + 1][q], k1), projectY(yb1, k1), colour, u1, vb);
            vertex(3, projectX(butte.x[g][q], k0), projectY(yb, k0), colour, u0, vb);
            batch.draw(face.getTexture(), quad, 0, quad.length);
        }
    }

    private static float colour(Color tint, float shade) {
        return Color.toFloatBits(tint.r * shade, tint.g * shade, tint.b * shade, tint.a);
    }

    private void vertex(int i, double x, double y, float colour, float u, float v) {
        int o = i * 5;
        quad[o] = X0 + (float) x;
        quad[o + 1] = (float) y;
        quad[o + 2] = colour;
        quad[o + 3] = u;
        quad[o + 4] = v;
    }
}
