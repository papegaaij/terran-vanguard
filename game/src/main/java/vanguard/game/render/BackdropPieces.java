package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * RF9d: the {@link Backdrop}'s set pieces at their places along the scroll, per layer: their animation frames or
 * headings, M8 part D's lights and M9 part B's turned lights and hold, M9 part C's strobes; and the ground's
 * perspective towers ({@link TowerProjection}). The places go by the level clock, the paths and frames by the real
 * clock ({@link BackdropClock}). Nothing is allocated per frame.
 */
final class BackdropPieces {
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    /**
     * M7 part B: the high-air pieces drawn as an overhead pass ({@link Backdrop#OVERHEAD_PASS_OPACITY}). M7 part C:
     * Level 16's rock arches (their alpha at most 53 %, so at most 40 % where they cross the field).
     */
    static final java.util.Set<String> OVERHEAD_PASSES = java.util.Set.of("transport", "arch-a", "arch-b");

    private final Piece[][] pieces;
    /**
     * M8 part D: per ground piece its light, or null: a piece {@code <id>-glow} declared next to it but not placed
     * (Level 24's {@code relay-beacon-glow}), drawn additively over the grade at the piece's place on its frame clock.
     */
    private final List<Array<AtlasRegion>> glows;
    /**
     * M9 part B (design/campaign Level 29, the turning station): per ground piece the light it turns to, or null: an
     * image {@code <id>-glow-red} beside its {@code -glow}, crossfaded in over {@value #TURN_SECONDS} s from the level
     * time {@link #turnAt} (Block C's and two Coalition blocks' white lights going red).
     */
    private final List<Array<AtlasRegion>> turnedGlows;
    /**
     * M9 part B (design/campaign Level 29, the rescue): per ground piece its hold opening, or null: a declared but
     * unplaced piece {@code <id>-hold} (the prison tender's), drawn over the piece from the escort swap's release
     * ({@link #holdOpened}), {@value #HOLD_FRAME_TICKS} steps a frame, its last frame held.
     */
    private final List<Array<AtlasRegion>> holds;
    /**
     * M10 part B, step B4 (design/campaign Level 36, the backdrop): per deep piece its light, or null: a declared but
     * unplaced piece {@code <id>-glow} (the carrier group's running lights, the storm wall's lightning), drawn additively
     * over the piece right after the deep layer's pieces on its own frame clock (its {@code frames} at its {@code fps}).
     */
    private final List<Array<AtlasRegion>> deepGlows;
    /** M10 part B: per deep piece its light's frames a second (0 for one following the piece's frames). */
    private final double[] deepGlowFps;

    /** M9 part B: steps since the hold opened (the escort swap's release); -1 while it is shut. */
    private int holdTicks = -1;

    /** M9 part B: the suffix of a piece's hold opening. */
    static final String HOLD = "-hold";

    /** M9 part B: the hold opens at 15 fps (Level 29's data: {@code prison-tender-hold}, 6 frames). */
    static final int HOLD_FRAME_TICKS = 4;

    /** M9 part B: the hold opened {@code ticks} steps ago (the escort swap's release), -1 while it is shut. */
    void holdOpened(int ticks) {
        holdTicks = ticks;
    }

    /** M9 part B: the level time the station turns: its dormant ground targets' wake ({@code dormant_until}); NaN. */
    private final double turnAt;

    private final TowerProjection towers;

    /**
     * M9 part C: the backdrop pieces with a strobe in their frame 0 (tools/art/backdrop_l30.py: Level 30's lane buoys and
     * its last beacon, one flash in 2 s, all in step), heard as a faint tick as each flash starts ({@link #strobed()}).
     */
    static final java.util.Set<String> STROBES = java.util.Set.of("lane-buoy", "lane-beacon");

    /** M9 part C: the strobe frame drawn at the last draw and at this one; the x of a flash started now, else NaN. */
    private int strobeFrame = -1;

    private double strobeX = Double.NaN;
    private boolean strobeDrawn;

    private void strobeSeen(int frame, double x) {
        if (frame == 0 && strobeFrame != 0 && Double.isNaN(strobeX)) {
            strobeX = x;
        }
        strobeDrawn = true;
        drawnStrobeFrame = frame;
    }

    private int drawnStrobeFrame = -1;

    /**
     * M9 part C: after a frame's draw, the play-field x of a strobe whose flash started in it (NaN for none); the next
     * frame starts afresh.
     */
    double strobed() {
        double x = strobeX;
        strobeFrame = strobeDrawn ? drawnStrobeFrame : -1;
        strobeDrawn = false;
        strobeX = Double.NaN;
        return x;
    }

    /**
     * A placed set piece with its images and its bottom edge on its layer; {@code pathStart} is its
     * path's first waypoint's script time (NaN without a path).
     */
    record Piece(
            BackdropData.PlacedPiece placed,
            BackdropData.Piece spec,
            Array<AtlasRegion> images,
            long bottom,
            double pathStart) {}

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param folder the level's backdrop folder ({@link Backdrop#folder(String, LevelData)})
     */
    BackdropPieces(Sprites sprites, LevelData level, String folder) {
        BackdropData data = level.backdrop();
        pieces = new Piece[BackdropLayer.values().length][];
        for (BackdropLayer layer : BackdropLayer.values()) {
            int l = layer.ordinal();
            List<Piece> onLayer = new ArrayList<>();
            for (BackdropData.PlacedPiece placed : data.placements()) {
                BackdropData.Piece spec = data.pieces().get(placed.piece());
                // M7 part C: a face patch is drawn on its wall's face (WallProjection), not flat.
                if (spec.layer() == layer && !spec.isTower() && !WallProjection.FACE_PATCHES.contains(placed.piece())) {
                    Array<AtlasRegion> images = sprites.backdrop(folder + placed.piece(), spec.imageCount());
                    long bottom =
                            Math.round(level.pieceCentre(placed) - spec.size().height() / 2);
                    double pathStart =
                            placed.path().map(points -> points.getFirst().t()).orElse(Double.NaN);
                    onLayer.add(new Piece(placed, spec, images, bottom, pathStart));
                }
            }
            pieces[l] = onLayer.toArray(Piece[]::new);
        }
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        List<Array<AtlasRegion>> lights = new ArrayList<>();
        List<Array<AtlasRegion>> turned = new ArrayList<>();
        for (Piece piece : onGround) {
            String light = piece.placed().piece() + GLOW;
            BackdropData.Piece spec = data.pieces().get(light);
            lights.add(
                    spec != null && spec.imageCount() == piece.images().size
                            ? sprites.backdrop(folder + light, spec.imageCount())
                            : spec == null ? undeclared(sprites, folder + light, piece.images().size) : null);
            // M9 part B: the light the piece turns to ({@code <id>-glow-red}, Level 29's Block C) at the turn.
            turned.add(undeclared(sprites, folder + light + TURNED, piece.images().size));
        }
        glows = lights;
        turnedGlows = turned;
        List<Array<AtlasRegion>> opened = new ArrayList<>();
        for (Piece piece : onGround) {
            String hold = piece.placed().piece() + HOLD;
            BackdropData.Piece spec = data.pieces().get(hold);
            opened.add(
                    spec != null && spec.frames().isPresent()
                            ? sprites.backdrop(folder + hold, spec.imageCount())
                            : null);
        }
        holds = opened;
        Piece[] onDeep = pieces[BackdropLayer.DEEP.ordinal()];
        List<Array<AtlasRegion>> deepLights = new ArrayList<>();
        deepGlowFps = new double[onDeep.length];
        for (int p = 0; p < onDeep.length; p++) {
            String light = onDeep[p].placed().piece() + GLOW;
            BackdropData.Piece spec = data.pieces().get(light);
            if (spec == null || spec.layer() != BackdropLayer.DEEP) {
                deepLights.add(null);
                continue;
            }
            deepLights.add(sprites.backdrop(folder + light, spec.imageCount()));
            deepGlowFps[p] = spec.animated() ? spec.fps().orElseThrow() : 0;
        }
        deepGlows = deepLights;
        turnAt = turnSeconds(level);
        towers = new TowerProjection(towers(sprites, folder, level), sprites.pixel);
    }

    /** A layer's placed pieces, in the data's order. */
    Piece[] onLayer(BackdropLayer layer) {
        return pieces[layer.ordinal()];
    }

    /**
     * M5 part C: the tower whose footprint's centre lies nearest {@code groundCentre} on the ground
     * layer, within {@code reach} px; -1 for none.
     */
    int towerNear(double groundCentre, double reach) {
        return towers.nearest(groundCentre, reach);
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}) is not drawn (the fallen arcology); -1 draws them all. */
    void hideTower(int index) {
        towers.hide(index);
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}) is drawn bent (see {@link TowerProjection#bend}); -1 for none. */
    void bendTower(int index, double lean, double shakeX, double shakeY, double share, double crush, float dust) {
        towers.bend(index, lean, shakeX, shakeY, share, crush, dust);
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}). */
    TowerProjection.Tower tower(int index) {
        return towers.tower(index);
    }

    /** The ground's perspective towers, lowest first, each its walls and then its roof. */
    void drawTowers(SpriteBatch batch, double groundScroll) {
        towers.draw(batch, groundScroll);
    }

    /** The level's placed towers with their roofs and wall textures. */
    private static List<TowerProjection.Tower> towers(Sprites sprites, String folder, LevelData level) {
        BackdropData data = level.backdrop();
        List<TowerProjection.Tower> towers = new ArrayList<>();
        for (BackdropData.PlacedPiece placed : data.placements()) {
            BackdropData.Piece spec = data.pieces().get(placed.piece());
            if (spec.tower().isEmpty()) {
                continue;
            }
            BackdropData.Tower tower = spec.tower().get();
            AtlasRegion roof = sprites.backdrop(folder + placed.piece(), 1).first();
            if (roof.getRegionWidth() != spec.imageWidth() || roof.getRegionHeight() != spec.imageHeight()) {
                throw new IllegalStateException("'" + placed.piece() + "': a tower's roof is drawn at its scale, "
                        + spec.imageWidth() + "x" + spec.imageHeight() + ", the image is " + roof.getRegionWidth()
                        + "x" + roof.getRegionHeight());
            }
            int footWidth = (int) Math.round(spec.size().width());
            int footHeight = (int) Math.round(spec.size().height());
            double centre = level.pieceCentre(placed);
            towers.add(new TowerProjection.Tower(
                    placed.x(),
                    centre,
                    Math.round(centre - footHeight / 2.0),
                    footWidth,
                    footHeight,
                    tower.scale(),
                    tower.height(),
                    (float) tower.shadeOrDefault(),
                    roof,
                    sprites.backdrop(folder + tower.wall(), 1).first()));
        }
        return towers;
    }

    /** A layer's pieces but the high-air overhead passes; the crawl's next section's as {@code crawl} has it. */
    void drawPieces(
            SpriteBatch batch,
            BackdropLayer layer,
            long scroll,
            BackdropClock clock,
            boolean overhead,
            BackdropTiles crawl) {
        drawPieces(batch, layer, scroll, clock, overhead, false, crawl);
    }

    /** As above, only the high-air overhead passes ({@code passes}) or all but them. */
    void drawPieces(
            SpriteBatch batch,
            BackdropLayer layer,
            long scroll,
            BackdropClock clock,
            boolean overhead,
            boolean passes,
            BackdropTiles crawl) {
        for (Piece piece : pieces[layer.ordinal()]) {
            BackdropData.PlacedPiece placed = piece.placed();
            if (placed.isOverhead() != overhead || overheadPass(layer, placed) != passes) {
                continue;
            }
            long shift = 0;
            if (crawl.afterCrawl(placed)) {
                if (!crawl.crawlEnded()) {
                    continue;
                }
                shift = crawl.crawlShift(layer);
            }
            float width = (float) piece.spec().size().width();
            float height = (float) piece.spec().size().height();
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            float y = piece.bottom() + shift - scroll + Math.round(placed.offsetY(along));
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            float x = X0 + Math.round(placed.x() + placed.offsetX(along) - width / 2);
            int frame = imageAt(piece, clock.real(), along);
            TextureRegion image = piece.images().get(frame);
            if (STROBES.contains(placed.piece()) && piece.spec().animated()) {
                // M9 part C: a lane buoy's strobe flashes in frame 0 (its tick, BUOY_TICK).
                strobeSeen(frame, placed.x());
            }
            if (placed.mirrored()) {
                batch.draw(image, x + width, y, -width, height);
            } else {
                batch.draw(image, x, y);
            }
            if (layer == BackdropLayer.GROUND && holdTicks >= 0) {
                drawHold(batch, piece, x, y, width, height);
            }
        }
    }

    /** M9 part B: a piece's hold opening over it ({@link #holds}), its last frame held. */
    private void drawHold(SpriteBatch batch, Piece piece, float x, float y, float width, float height) {
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        for (int p = 0; p < onGround.length; p++) {
            Array<AtlasRegion> hold = holds.get(p);
            if (onGround[p] != piece || hold == null) {
                continue;
            }
            TextureRegion image = hold.get(Math.min(hold.size - 1, holdTicks / HOLD_FRAME_TICKS));
            if (piece.placed().mirrored()) {
                batch.draw(image, x + width, y, -width, height);
            } else {
                batch.draw(image, x, y);
            }
        }
    }

    /** M8 part D: the suffix of a ground piece's light ({@link #drawPieceGlows}). */
    static final String GLOW = "-glow";

    /** M9 part B: the suffix of the light a piece turns to after its glow ({@link #turnedGlows}). */
    static final String TURNED = "-red";

    /** M9 part B: the turned lights' crossfade, s (design/campaign Level 29: "white to red over 1 s"). */
    static final double TURN_SECONDS = 1;

    /**
     * M9 part B: a piece's light that the level's data does not declare (Level 29's backdrop: "glows are added by the
     * game at their pieces"), from the backdrop atlas when it holds one with the piece's frame count; null otherwise.
     */
    private static Array<AtlasRegion> undeclared(Sprites sprites, String name, int count) {
        if (!sprites.hasBackdrop(name)) {
            return null;
        }
        Array<AtlasRegion> images = sprites.backdrop(name);
        return images.size == count ? images : null;
    }

    /** M9 part B: the level time its dormant ground targets wake, the earliest; NaN without any. */
    static double turnSeconds(LevelData level) {
        double at = Double.NaN;
        for (var target : level.groundTargets()) {
            if (target.dormantUntil().isPresent() && !(target.dormantUntil().get() >= at)) {
                at = target.dormantUntil().get();
            }
        }
        return at;
    }

    /** M9 part B: how far the turned lights have come in at level time {@code script}: 0 before the turn, then to 1. */
    static float turnShare(double turnAt, double script) {
        if (Double.isNaN(turnAt)) {
            return 0;
        }
        return (float) Math.clamp((script - turnAt) / TURN_SECONDS, 0, 1);
    }

    /**
     * M10 part B, step B4: the deep pieces' lights (Level 36's carrier group and storm wall), additive, at their pieces'
     * places, the deep layer scrolled by {@code scroll}; an animated light on its own frame clock (the real clock).
     */
    void drawDeepGlows(SpriteBatch batch, long scroll, BackdropClock clock, BackdropTiles crawl) {
        Piece[] onDeep = pieces[BackdropLayer.DEEP.ordinal()];
        boolean additive = false;
        for (int p = 0; p < onDeep.length; p++) {
            Piece piece = onDeep[p];
            Array<AtlasRegion> light = deepGlows.get(p);
            if (light == null || crawl.afterCrawl(piece.placed())) {
                continue;
            }
            float width = (float) piece.spec().size().width();
            float height = (float) piece.spec().size().height();
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            float y = piece.bottom() - scroll + Math.round(piece.placed().offsetY(along));
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            float x = X0 + Math.round(piece.placed().x() + piece.placed().offsetX(along) - width / 2);
            int frame = deepGlowFps[p] > 0
                    ? (int) Math.floorMod((long) Math.floor(clock.real() * deepGlowFps[p]), (long) light.size)
                    : Math.min(light.size - 1, imageAt(piece, clock.real(), along));
            drawLight(batch, light.get(frame), piece.placed().mirrored(), x, y, width, height);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** M8 part D: whether a ground piece has its light ({@link #drawPieceGlows}): Level 24's relay beacons. */
    boolean hasPieceGlows() {
        for (Array<AtlasRegion> light : glows) {
            if (light != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * M8 part D: the ground pieces' lights (Level 24's relay beacons' pulse), additive, at their pieces' places and on
     * their frame clock, after the under-water grade so it does not dim them; the ground layer scrolled by {@code
     * scroll}, the crawl's next section's lights left out ({@code crawl}).
     */
    void drawPieceGlows(SpriteBatch batch, long scroll, BackdropClock clock, BackdropTiles crawl) {
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        boolean additive = false;
        for (int p = 0; p < onGround.length; p++) {
            Piece piece = onGround[p];
            Array<AtlasRegion> light = glows.get(p);
            if (light == null || crawl.afterCrawl(piece.placed())) {
                continue;
            }
            float height = (float) piece.spec().size().height();
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            float y = piece.bottom() - scroll + Math.round(piece.placed().offsetY(along));
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            float width = (float) piece.spec().size().width();
            float x = X0 + Math.round(piece.placed().x() + piece.placed().offsetX(along) - width / 2);
            int frame = imageAt(piece, clock.real(), along);
            Array<AtlasRegion> red = turnedGlows.get(p);
            float turn = red == null ? 0 : turnShare(turnAt, clock.script());
            if (turn < 1) {
                if (turn > 0) {
                    batch.setColor(1, 1, 1, 1 - turn);
                }
                drawLight(batch, light.get(frame), piece.placed().mirrored(), x, y, width, height);
            }
            if (turn > 0) {
                batch.setColor(1, 1, 1, turn);
                drawLight(batch, red.get(frame), piece.placed().mirrored(), x, y, width, height);
            }
            batch.setColor(1, 1, 1, 1);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private static void drawLight(
            SpriteBatch batch, TextureRegion image, boolean mirrored, float x, float y, float width, float height) {
        if (mirrored) {
            batch.draw(image, x + width, y, -width, height);
        } else {
            batch.draw(image, x, y);
        }
    }

    /** M7 part B: whether a placed piece is a huge overhead pass on high-air ({@link #OVERHEAD_PASSES}). */
    static boolean overheadPass(BackdropLayer layer, BackdropData.PlacedPiece placed) {
        return layer == BackdropLayer.HIGH_AIR && OVERHEAD_PASSES.contains(placed.piece());
    }

    /** The animation frame at real {@code seconds}, the heading along the path at {@code along}, or the only image. */
    static int imageAt(Piece piece, double seconds, double along) {
        int count = piece.images().size;
        if (piece.spec().animated()) {
            return (int)
                    Math.floorMod((long) Math.floor(seconds * piece.spec().fps().orElseThrow()), (long) count);
        }
        if (piece.spec().headings().isPresent()) {
            return (int) Math.floorMod(Math.round(piece.placed().heading(along) * count / 360), (long) count);
        }
        return 0;
    }
}
