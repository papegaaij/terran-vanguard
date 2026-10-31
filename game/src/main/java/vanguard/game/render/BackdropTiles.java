package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * RF9d: the {@link Backdrop}'s layer tiles: per layer and section the tile set, which changes at a seam entering at the
 * top edge when a section starts (M7 part G: a strip backdrop keys the ground's tile sets by the strip's row instead,
 * {@link #stripTiles}), each layer scrolled by its factor of the ground's distance and drawn at whole pixels; and M7
 * part H's crawl, whose next section's seams wait until it starts ({@link #crawls}). Nothing is allocated per frame.
 */
final class BackdropTiles {
    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    private final LevelData level;
    private final Sprites sprites;
    private final String folder;

    private final double[] factors = new double[BackdropLayer.values().length];
    /** Per layer and section: the tile set, or null where the layer is empty. */
    private final Tiles[][] tiles;
    /** Per layer and section: where the section's tile set begins on the layer. */
    private final long[][] seams;
    /**
     * Per layer: the rows its tile pixel rows count from, added to a layer row (0; M7 part G: a strip's origin on the
     * ground layer, so a tile's pixel row is the strip row's, see {@link #stripTiles}).
     */
    private final long[] phase = new long[BackdropLayer.values().length];

    private final TextureRegion strip = new TextureRegion();

    /**
     * M7 part H: the section after a crawling arena (Level 21's section 6), -1 without one ({@link #crawls}); its
     * start's script time, and the ground scroll the data places it at.
     */
    private int crawlNext = -1;

    private double crawlStart = Double.NaN;
    private double crawlPlanned;
    /** Whether that section has started ({@link #crawlEnded(double)}): its seams and its pieces' shift are set. */
    private boolean crawlSet;
    /** How far the ground at its start lies past where the data places it, ground px. */
    private double crawlShift;
    /** The seam piece laid over its ground seam ({@code seam-settle}), null without one, and its bottom row. */
    private TextureRegion settle;

    private long settleBottom;

    record Tiles(TextureRegion image, double drift) {}

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param folder the level's backdrop folder ({@link Backdrop#folder(String, LevelData)})
     */
    BackdropTiles(Sprites sprites, LevelData level, String folder) {
        this.level = level;
        this.sprites = sprites;
        this.folder = folder;
        BackdropData data = level.backdrop();
        int layerCount = BackdropLayer.values().length;
        int sectionCount = level.sections().size();
        tiles = new Tiles[layerCount][sectionCount];
        seams = new long[layerCount][sectionCount];
        for (BackdropLayer layer : BackdropLayer.values()) {
            int l = layer.ordinal();
            factors[l] = data.factor(layer);
            for (int s = 0; s < sectionCount; s++) {
                seams[l][s] = s == 0 ? Long.MIN_VALUE : Math.round(level.seam(layer, s));
            }
        }
        for (int s = 0; s < sectionCount; s++) {
            for (String id : level.sections().get(s).tiles()) {
                BackdropData.TileSet tileSet = data.tileSets().get(id);
                tiles[tileSet.layer().ordinal()][s] = tiles(sprites, folder, id, tileSet);
            }
        }
        data.strip().ifPresent(keyed -> stripTiles(keyed, data));
    }

    /**
     * M7 part G, the strip backdrop (design/campaign Level 20, a level that flies back over ground it has shown;
     * m7g-gaps §1.7): the layers its zones name tile sets on are keyed by the strip's row instead of by section. Strip
     * row = origin + the ground's (signed) scroll + y, so on the ground layer a zone from strip row {@code from} begins
     * at layer row {@code from - origin} (on another layer at that times its factor), whichever way the ground moves;
     * the tile's pixel row is the strip row's ({@link #phase}). The zones replace the sections' tile sets on those
     * layers (the sections name none with a strip).
     */
    private void stripTiles(BackdropData.Strip strip, BackdropData data) {
        List<BackdropData.Zone> zones = strip.zones();
        for (BackdropLayer layer : BackdropLayer.values()) {
            int l = layer.ordinal();
            Tiles[] keyed = new Tiles[zones.size()];
            long[] starts = new long[zones.size()];
            boolean any = false;
            for (int z = 0; z < zones.size(); z++) {
                BackdropData.Zone zone = zones.get(z);
                starts[z] = z == 0 ? Long.MIN_VALUE : stripSeam(strip, z, factors[l]);
                for (String id : zone.tiles()) {
                    BackdropData.TileSet tileSet = data.tileSets().get(id);
                    if (tileSet.layer() == layer) {
                        keyed[z] = tiles(sprites, folder, id, tileSet);
                        any = true;
                    }
                }
            }
            if (any) {
                tiles[l] = keyed;
                seams[l] = starts;
                phase[l] = Math.round(strip.origin() * factors[l]);
            }
        }
    }

    /**
     * M7 part G: where zone {@code zone} of a strip begins on a layer of scroll factor {@code factor}, in the layer's
     * rows (0 at the screen's bottom edge at the level start, as {@link LevelData#pieceCentre} places the pieces).
     */
    static long stripSeam(BackdropData.Strip strip, int zone, double factor) {
        return Math.round((strip.zones().get(zone).from() - strip.origin()) * factor);
    }

    /**
     * M7 part H (design/campaign Level 21; the user's round 43 answer H.6): section {@code arena} (0-based) is an arena
     * whose ground crawls on while the clock is held, so the ground at the next section's start is not where the data
     * places it. Until that section starts the crawl keeps drawing the arena's tile sets (its seams wait) and the next
     * section's pieces are not drawn; then {@link #crawlEnded(double)} lays them out from the actual ground.
     */
    void crawls(int arena) {
        if (arena < 0 || arena + 1 >= level.sections().size()) {
            return;
        }
        crawlNext = arena + 1;
        crawlStart = level.sections().get(arena).end();
        crawlPlanned = level.scrollAt(crawlStart);
        settle = sprites.hasBackdrop(folder + SEAM_SETTLE)
                ? sprites.backdrop(folder + SEAM_SETTLE, 1).first()
                : null;
        crawlPending();
    }

    /** M7 part H: the seam piece a crawl's next section lays over its ground seam (Level 21's settling dust). */
    static final String SEAM_SETTLE = "seam-settle";

    /** M7 part H: the crawl's next section has not started (a restart, a retry from the boss): its seams wait. */
    void crawlPending() {
        if (crawlNext < 0) {
            return;
        }
        for (int l = 0; l < tiles.length; l++) {
            if (crawlNext < tiles[l].length && tiles[l][crawlNext] != null) {
                seams[l][crawlNext] = Long.MAX_VALUE;
            }
        }
        crawlSet = false;
        crawlShift = 0;
    }

    /** M7 part H: whether the crawl's next section has started ({@link #crawlEnded(double)}); false without a crawl. */
    boolean crawlEnded() {
        return crawlSet;
    }

    /**
     * M7 part H: the crawl's next section starts with the ground at {@code ground}: on each layer its tile set starts at
     * the next seam of the arena's tile at or above the top edge ({@link #crawlSeam}; on the ground the next multiple of
     * 512 for Level 21's floor with the seam piece's lower half off the screen), {@code seam-settle} centred on the
     * ground's;
     * its pieces shift by the ground's distance past where the data places the section's start (times their layer's
     * factor), so Level 21's lift-offs keep their place in the section however long the fight ran.
     */
    void crawlEnded(double ground) {
        if (crawlNext < 0) {
            return;
        }
        for (int l = 0; l < tiles.length; l++) {
            if (crawlNext < tiles[l].length && tiles[l][crawlNext] != null) {
                Tiles arena = tiles[l][crawlNext - 1];
                int height = arena == null ? 1 : arena.image().getRegionHeight();
                int margin = l == BackdropLayer.GROUND.ordinal() && settle != null ? settle.getRegionHeight() / 2 : 0;
                seams[l][crawlNext] = crawlSeam(Math.round(ground * factors[l]), height, margin);
            }
        }
        long groundSeam = seams[BackdropLayer.GROUND.ordinal()][crawlNext];
        if (settle != null && groundSeam != Long.MAX_VALUE) {
            settleBottom = groundSeam - settle.getRegionHeight() / 2;
        }
        crawlShift = ground - crawlPlanned;
        crawlSet = true;
    }

    /**
     * M7 part H: where the section after a crawl begins on a layer whose scroll is {@code scroll} as it starts: the
     * next multiple of the arena's tile {@code height} at or above the top edge (both tile sets share the pattern
     * there, H.6), at least {@code margin} px above it (the seam piece's lower half: it never pops in on the screen; on
     * Level 21's ground 160 px, so the settled floor enters within 512 + 160 px, ≤ 27 s at 25 px/s).
     */
    static long crawlSeam(long scroll, int height, int margin) {
        long top = scroll + HEIGHT + margin;
        return Math.floorDiv(top + height - 1, (long) height) * height;
    }

    /** M7 part H: a piece of the crawl's next section ({@link #crawls}): held back until it starts, then shifted. */
    boolean afterCrawl(BackdropData.PlacedPiece placed) {
        return crawlNext >= 0 && placed.t() >= crawlStart;
    }

    /** M7 part H: how far the crawl's next section's pieces shift on {@code layer} once it started ({@link #crawlEnded}). */
    long crawlShift(BackdropLayer layer) {
        return Math.round(crawlShift * factors[layer.ordinal()]);
    }

    /** M7 part H: the settling dust's soft edge over the crawl's next ground seam, under the ground's pieces. */
    void drawSettle(SpriteBatch batch, double groundScroll) {
        if (crawlSet && settle != null) {
            long y = settleBottom - scroll(BackdropLayer.GROUND, groundScroll);
            if (y < HEIGHT && y + settle.getRegionHeight() > 0) {
                batch.draw(settle, X0, y);
            }
        }
    }

    /** M5 part H: the high-air layer's scroll at the ground's {@code groundScroll}, px (Level 14's canopy tear tiles on it). */
    long highAirScroll(double groundScroll) {
        return scroll(BackdropLayer.HIGH_AIR, groundScroll);
    }

    /** A layer's scroll at the ground's {@code groundScroll}, whole px. */
    long scroll(BackdropLayer layer, double groundScroll) {
        return Math.round(groundScroll * factors[layer.ordinal()]);
    }

    /** M7 part C: a layer's tile sets per section (or per strip zone), for the plateau drawn row by row. */
    Tiles[] tiles(BackdropLayer layer) {
        return tiles[layer.ordinal()];
    }

    /** M7 part C: where a layer's sections' tile sets begin ({@link #tiles(BackdropLayer)}). */
    long[] seams(BackdropLayer layer) {
        return seams[layer.ordinal()];
    }

    /** M7 part G: the rows a layer's tile pixel rows count from ({@link #phase}). */
    long phase(BackdropLayer layer) {
        return phase[layer.ordinal()];
    }

    static Tiles tiles(Sprites sprites, String folder, String id, BackdropData.TileSet tileSet) {
        return new Tiles(
                sprites.backdrop(folder + id, 1).first(), tileSet.drift().orElse(0.0));
    }

    void drawSectionTiles(SpriteBatch batch, BackdropLayer layer, long scroll, BackdropClock clock) {
        int l = layer.ordinal();
        for (int s = 0; s < tiles[l].length; s++) {
            Tiles section = tiles[l][s];
            if (section != null) {
                long end = s + 1 < tiles[l].length ? seams[l][s + 1] : Long.MAX_VALUE;
                drawTiles(
                        batch,
                        section,
                        Math.max(seams[l][s], scroll),
                        Math.min(end, scroll + HEIGHT),
                        scroll,
                        clock.real(),
                        phase[l]);
            }
        }
    }

    /**
     * The rows {@code [from, to)} of a layer filled with a tile set that repeats from position {@code -phase} (0; M7
     * part G: a strip's origin, so its pixel rows follow the strip row), drifting by real {@code seconds}.
     */
    void drawTiles(SpriteBatch batch, Tiles tileSet, long from, long to, long scroll, double seconds, long phase) {
        TextureRegion image = tileSet.image();
        int height = image.getRegionHeight();
        int shift = (int) Math.floorMod(Math.round(tileSet.drift() * seconds), (long) WIDTH);
        for (long row = from; row < to; ) {
            int inTile = (int) Math.floorMod(row + phase, (long) height);
            int rows = (int) Math.min(height - inTile, to - row);
            float y = row - scroll;
            // The tile image's top row is its highest; a strip [inTile, inTile + rows) counts from its bottom.
            int top = height - inTile - rows;
            drawStrip(batch, image, 0, top, WIDTH - shift, rows, X0 + shift, y);
            if (shift > 0) {
                drawStrip(batch, image, WIDTH - shift, top, shift, rows, X0, y);
            }
            row += rows;
        }
    }

    private void drawStrip(
            SpriteBatch batch, TextureRegion image, int x, int y, int width, int height, float screenX, float screenY) {
        strip.setRegion(image, x, y, width, height);
        batch.draw(strip, screenX, screenY);
    }
}
