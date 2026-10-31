package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
import vanguard.game.render.BackdropPieces.Piece;
import vanguard.game.render.BackdropTiles.Tiles;
import vanguard.sim.PlayField;
import vanguard.sim.Terrain;

/**
 * RF9d: the {@link Backdrop}'s canyon walls (M7 part C, {@link WallProjection}), drawn with the ground's pieces: the
 * floor, what swims under the walls (M8 part E), the walls, the {@code plateau} layer beyond their rims, the islands,
 * the side caves' mouths (M8 part E) and Level 32's galleries (M9 part E); an ice shaft's walls, wet below the
 * waterline, and its collars' glow (M8 part C). Nothing is allocated per frame.
 */
final class TrenchWalls {
    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    private final LevelData level;
    private final Sprites sprites;
    private final String folder;
    private final TextureRegion pixel;
    private final BackdropTiles tiles;
    private final BackdropPieces pieces;

    private final TextureRegion strip = new TextureRegion();

    /** M7 part C: the canyon's rock drawn with the ground's pieces; null without terrain ({@link #canyon}). */
    private WallProjection canyon;
    /**
     * M8 part E: the terrain whose notches (the side caves) have their mouths drawn over the walls ({@link
     * #drawCaveMouths}); null without notches or without the art. Per notch its mouth's image (null: none), its column
     * on the wall's foot line and its row (from the top) on the notch's middle row.
     */
    private Terrain caves;

    private AtlasRegion[] caveMouths = new AtlasRegion[0];
    private int[] caveFoot = new int[0];
    private int[] caveMid = new int[0];
    /** M8 part E: drawn under the walls (over the floor); null for nothing ({@link #underWalls}). */
    private Backdrop.UnderTowers underWalls;

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param folder the level's backdrop folder ({@link Backdrop#folder(String, LevelData)})
     * @param tiles the layer tiles, whose {@code plateau} tile sets are drawn beyond the walls' rims
     * @param pieces the set pieces, whose {@code plateau} pieces are drawn beyond the walls' rims
     */
    TrenchWalls(Sprites sprites, LevelData level, String folder, BackdropTiles tiles, BackdropPieces pieces) {
        this.level = level;
        this.sprites = sprites;
        this.folder = folder;
        this.pixel = sprites.pixel;
        this.tiles = tiles;
        this.pieces = pieces;
    }

    /**
     * M7 part C: the level's canyon from the simulation's {@code terrain} (null: none), drawn with the
     * ground's pieces ({@link WallProjection}) in the {@code looks} the level data gives it: the walls'
     * {@code canyon-wall} and {@code wall-foot}, the islands' {@code <look>-face} and {@code <look>-top}
     * images, the face patches' images, all from the level's backdrop folder.
     */
    void canyon(Terrain terrain, WallProjection.Looks looks) {
        caves = null;
        if (terrain == null) {
            canyon = null;
            return;
        }
        // M8 part E: the side caves' mouths (Level 25's cave-mouth-left and -right, declared, not placed); M9 part E:
        // Level 32's galleries, one image per side and depth (gallery-<side>-<depth>) in place of them.
        List<Terrain.Notch> notches = terrain.notches();
        caveMouths = new AtlasRegion[notches.size()];
        caveFoot = new int[notches.size()];
        caveMid = new int[notches.size()];
        boolean any = false;
        for (int n = 0; n < notches.size(); n++) {
            Terrain.Notch notch = notches.get(n);
            int side = notch.side() == Terrain.Side.LEFT ? 0 : 1;
            String sideName = side == 0 ? "left" : "right";
            int depth = (int) Math.round(notch.depth());
            String gallery = folder + GALLERY + sideName + "-" + depth;
            String mouth = folder + CAVE_MOUTH + sideName;
            if (sprites.hasBackdrop(gallery)) {
                caveMouths[n] = sprites.backdrop(gallery, 1).first();
                caveFoot[n] = side == 0 ? depth + GALLERY_RIM : GALLERY_SPILL;
                caveMid[n] = caveMouths[n].getRegionHeight() / 2;
            } else if (sprites.hasBackdrop(mouth)) {
                caveMouths[n] = sprites.backdrop(mouth, 1).first();
                caveFoot[n] = CAVE_FOOT[side];
                caveMid[n] = CAVE_MID;
            }
            any |= caveMouths[n] != null;
        }
        if (any) {
            caves = terrain;
        }
        List<WallProjection.Island> islands = new ArrayList<>();
        for (WallProjection.IslandLook look : looks.islands()) {
            List<AtlasRegion> top = new ArrayList<>();
            sprites.backdrop(folder + look.look() + "-top").forEach(top::add);
            islands.add(new WallProjection.Island(
                    look.foot(),
                    look.look(),
                    sprites.backdrop(folder + look.look() + "-face", 1).first(),
                    top));
        }
        List<WallProjection.Patch> patches = new ArrayList<>();
        for (WallProjection.FacePatch patch : looks.patches()) {
            AtlasRegion image = sprites.backdrop(folder + patch.look(), 1).first();
            double half = image.getRegionHeight() / 2.0;
            patches.add(new WallProjection.Patch(patch.side(), patch.row() - half, patch.row() + half, image));
        }
        // M8 part C: an ice shaft (Level 23) has its own walls, wet below the waterline, with the collars' glow.
        boolean ice = !sprites.hasBackdrop(folder + CANYON_WALL) && sprites.hasBackdrop(folder + ICE_WALL);
        String wall = ice ? ICE_WALL : CANYON_WALL;
        String foot = ice ? ICE_WALL + "-foot" : "wall-foot";
        canyon = new WallProjection(
                terrain,
                looks,
                sprites.backdrop(folder + wall, 1).first(),
                sprites.backdrop(folder + foot, 1).first(),
                pixel,
                islands,
                patches,
                ice
                        ? new WallProjection.Ice(
                                optionalImage(folder + wall + "-wet"),
                                optionalImage(folder + foot + "-wet"),
                                optionalImage(folder + wall + "-glow"),
                                wetRow)
                        : null);
    }

    /** M7 part C: a canyon's wall texture; M8 part C: an ice shaft's ({@code ice-wall}, {@code -wet}, {@code -glow}). */
    static final String CANYON_WALL = "canyon-wall";
    /**
     * M8 part E: a side cave's mouth seen from above ({@code cave-mouth-left}, {@code -right}; tools/art/backdrop_l25.py):
     * per side the image's column on the wall's foot line, and its row (from the top) on the notch's middle row.
     */
    static final String CAVE_MOUTH = "cave-mouth-";

    static final int[] CAVE_FOOT = {84, 27};
    static final int CAVE_MID = 62;

    /**
     * M9 part E: Level 32's notch mouths ({@code gallery-<side>-<depth>}, tools/art/backdrop_l32.py): the foot line at
     * column depth + {@link #GALLERY_RIM} (left) or {@link #GALLERY_SPILL} (right), the notch's middle row at the image's
     * middle row.
     */
    static final String GALLERY = "gallery-";

    static final int GALLERY_RIM = 16;
    static final int GALLERY_SPILL = 28;

    static final String ICE_WALL = "ice-wall";

    /**
     * M8 part C: the terrain row from which an ice shaft's walls are wet (the first waterline's ground row less the play
     * field's height: Level 23's 2,106); +∞ without a waterline.
     */
    private double wetRow = Double.POSITIVE_INFINITY;

    /** M8 part C: an ice shaft's walls are wet from terrain row {@code row} up (the waterline). */
    void wetFrom(double row) {
        wetRow = row;
    }

    private AtlasRegion optionalImage(String name) {
        return sprites.hasBackdrop(name) ? sprites.backdrop(name, 1).first() : null;
    }

    /** M8 part C: an ice shaft's collar strips glowing over its walls, after the grade (nothing for a canyon). */
    void drawCanyonGlow(SpriteBatch batch) {
        if (canyon != null) {
            canyon.drawGlow(batch);
        }
    }

    /** M8 part E: {@code layer} is drawn under the canyon's walls, over its floor; null for nothing. */
    void underWalls(Backdrop.UnderTowers layer) {
        underWalls = layer;
    }

    /**
     * M7 part C: a canyon's rock, after the ground's pieces and before its towers ({@link WallProjection}): the foot
     * bands and the rock's shadows on the floor, the walls' steps, the {@code plateau} layer beyond the walls' rims
     * (over the rock below the rim's height), the buttes, the caves' mouths; nothing without a canyon.
     */
    void draw(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        if (canyon != null) {
            canyon.prepare(Math.round(groundScroll));
            canyon.drawFloor(batch);
            if (underWalls != null) {
                // M8 part E: what swims into the rock goes behind the walls' face.
                underWalls.draw(batch);
            }
            canyon.drawWalls(batch);
            drawPlateau(batch, groundScroll, clock);
            canyon.drawIslands(batch);
            drawCaveMouths(batch, Math.round(groundScroll));
        }
    }

    /**
     * M8 part E: each notch's mouth on the screen ({@link #CAVE_MOUTH}), over the walls and under the ground objects
     * (Level 25's survey sub lies in one): its image's {@link #CAVE_FOOT} column on its wall's line at the notch's
     * middle row, its {@link #CAVE_MID} row on that row, at full size (no projection); M9 part E: Level 32's galleries
     * ({@link #GALLERY}) by the notch's side and depth, their foot column and middle row their own.
     */
    private void drawCaveMouths(SpriteBatch batch, long groundScroll) {
        if (caves == null) {
            return;
        }
        List<Terrain.Notch> notches = caves.notches();
        for (int n = 0; n < notches.size(); n++) {
            Terrain.Notch notch = notches.get(n);
            int side = notch.side() == Terrain.Side.LEFT ? 0 : 1;
            AtlasRegion mouth = caveMouths[n];
            double y = notch.scroll() - groundScroll + HEIGHT;
            if (mouth == null || y + mouth.getRegionHeight() < 0 || y - mouth.getRegionHeight() > HEIGHT) {
                continue;
            }
            double foot = side == 0 ? caves.leftAt(notch.scroll()) : caves.rightAt(notch.scroll());
            if (Double.isNaN(foot)) {
                continue;
            }
            batch.draw(
                    mouth, Math.round(X0 + foot - caveFoot[n]), Math.round(y - (mouth.getRegionHeight() - caveMid[n])));
        }
    }

    /**
     * M7 part C: the {@code plateau} layer (its tiles, then its pieces) only beyond the canyon walls'
     * rims, row by row (drawn over the walls: the rock below the rim is behind the plateau) ({@link WallProjection#leftClip}, {@link WallProjection#rightClip}); nothing where
     * a row has no wall. Its tile sets do not drift.
     */
    private void drawPlateau(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        if (!level.backdrop().hasPlateau()) {
            return;
        }
        Tiles[] sections = tiles.tiles(BackdropLayer.PLATEAU);
        long[] seams = tiles.seams(BackdropLayer.PLATEAU);
        long phase = tiles.phase(BackdropLayer.PLATEAU);
        long scroll = tiles.scroll(BackdropLayer.PLATEAU, groundScroll);
        for (int y = 0; y < HEIGHT; y++) {
            float left = canyon.leftClip(y);
            float right = canyon.rightClip(y);
            if (Float.isNaN(left) && Float.isNaN(right)) {
                continue;
            }
            long row = scroll + y;
            Tiles section = null;
            for (int s = 0; s < sections.length; s++) {
                if (seams[s] > row) {
                    break;
                }
                section = sections[s];
            }
            if (section == null) {
                continue;
            }
            TextureRegion image = section.image();
            int top = image.getRegionHeight() - 1 - (int) Math.floorMod(row + phase, (long) image.getRegionHeight());
            clipped(batch, image, 0, top, WIDTH, 0, y, left, right);
        }
        for (Piece piece : pieces.onLayer(BackdropLayer.PLATEAU)) {
            BackdropData.PlacedPiece placed = piece.placed();
            int width = (int) Math.round(piece.spec().size().width());
            int height = (int) Math.round(piece.spec().size().height());
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            long bottom = piece.bottom() - scroll + Math.round(placed.offsetY(along));
            if (bottom >= HEIGHT || bottom + height <= 0) {
                continue;
            }
            long x = Math.round(placed.x() + placed.offsetX(along) - width / 2.0);
            TextureRegion image = piece.images().get(BackdropPieces.imageAt(piece, clock.real(), along));
            for (int r = 0; r < height; r++) {
                long y = bottom + r;
                if (y < 0 || y >= HEIGHT) {
                    continue;
                }
                float left = canyon.leftClip((int) y);
                float right = canyon.rightClip((int) y);
                if (Float.isNaN(left) && Float.isNaN(right)) {
                    continue;
                }
                int column = placed.mirrored() ? -1 : 0;
                clipped(batch, image, column, height - 1 - r, width, x, (int) y, left, right);
            }
        }
    }

    /**
     * One row of an image ({@code imageRow} from its top, {@code width} px from column 0, or mirrored when
     * {@code mirrored} is -1) drawn at play-field x {@code x} on screen row {@code y}, only left of {@code
     * left} and right of {@code right} (NaN: nothing on that side).
     */
    private void clipped(
            SpriteBatch batch,
            TextureRegion image,
            int mirrored,
            int imageRow,
            int width,
            long x,
            int y,
            float left,
            float right) {
        if (!Float.isNaN(left)) {
            part(
                    batch,
                    image,
                    mirrored,
                    imageRow,
                    width,
                    x,
                    y,
                    Math.max(0, x),
                    Math.min(Math.min(x + width, WIDTH), (long) Math.ceil(left)));
        }
        if (!Float.isNaN(right)) {
            part(
                    batch,
                    image,
                    mirrored,
                    imageRow,
                    width,
                    x,
                    y,
                    Math.max(Math.max(0, x), (long) Math.floor(right)),
                    Math.min(x + width, WIDTH));
        }
    }

    /** The image row's columns that land on play-field x {@code [from, to)}. */
    private void part(
            SpriteBatch batch,
            TextureRegion image,
            int mirrored,
            int imageRow,
            int width,
            long x,
            int y,
            long from,
            long to) {
        if (to <= from) {
            return;
        }
        int columns = (int) (to - from);
        if (mirrored < 0) {
            // A mirrored piece: play-field x maps to the image's column width − 1 − (x − its left).
            int column = (int) (x + width - to);
            strip.setRegion(image, column, imageRow, columns, 1);
            batch.draw(strip, X0 + to, y, -columns, 1);
        } else {
            strip.setRegion(image, (int) (from - x), imageRow, columns, 1);
            batch.draw(strip, X0 + from, y);
        }
    }
}
