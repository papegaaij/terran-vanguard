package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
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
import vanguard.sim.Terrain;

/**
 * A level's parallax backdrop as its data file lays it out (design/art-direction, Parallax layer
 * model): per layer the sections' tile sets, which change at a seam entering at the top edge when
 * a section starts, the set pieces at their places along the scroll (a ground piece with a tower
 * block drawn in perspective by {@link TowerProjection}), and the atmosphere of the section (low-air
 * cloud banks, high-air wisps, haze over the deep and far layers), which ramps across a section
 * boundary. Every layer scrolls by its factor of the ground's distance and is drawn at whole pixels
 * (a tower's walls lean between them); nothing is allocated per frame. The pieces' places along the
 * scroll and the atmosphere go by the level clock, their paths, animation frames and the tile sets'
 * drift by the real clock (M5 part C: moving scenery keeps its speed in a hold zone, see {@link
 * BackdropClock}). M7 part G: a strip backdrop (Level 20) keys the ground's tile sets by the strip's row instead
 * ({@link #stripTiles}); every layer scrolls with the ground's signed distance, so in a reversed section new ground
 * enters at the bottom edge and the set pieces leave by the top.
 */
public final class Backdrop {
    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** Weather on high-air may cover the play plane at no more than 40 % opacity. */
    private static final float HIGH_AIR_OPACITY = 0.4f;
    /**
     * M7 part B: a huge overhead pass on high-air (art direction, the layer table: 75 % so the ship shows
     * under it, normal blending): Level 15's Mars transport over the launch (tools/art/backdrop_l15.py
     * proposes the key {@code overhead_pass}; kept as this piece-name rule in the game, no data key, pending
     * the user's review in round 38).
     */
    static final float OVERHEAD_PASS_OPACITY = 0.75f;
    /**
     * M7 part B: the high-air pieces drawn as an overhead pass ({@link #OVERHEAD_PASS_OPACITY}). M7 part C:
     * Level 16's rock arches (their alpha at most 53 %, so at most 40 % where they cross the field).
     */
    static final java.util.Set<String> OVERHEAD_PASSES = java.util.Set.of("transport", "arch-a", "arch-b");
    /** M5 part H: the high-air layer's strength ({@link #frontFade}), 1 by default. */
    private float frontFade = 1;

    private final LevelData level;
    private final double ramp;
    /** The atmosphere along the level, each with its look in {@link #looks} (M5 part G: easy's, {@link #easy}). */
    private List<LevelData.Stretch> stretches;

    private final TextureRegion pixel;
    private final Color haze;
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

    private final Piece[][] pieces;
    /**
     * M8 part D: per ground piece its light, or null: a piece {@code <id>-glow} declared next to it but not placed
     * (Level 24's {@code relay-beacon-glow}), drawn additively over the grade at the piece's place on its frame clock.
     */
    private final List<Array<AtlasRegion>> glows;

    private final TowerProjection towers;
    private final Look[] looks;
    /** M5 part C: the collapse's dust peak's look (Level 09's {@code heavy}); null without a collapse. */
    private final Look dust;
    /** How strongly the dust peak shows now, 0 to 1 ({@link #dustPeak}). */
    private float dustWeight;
    /** M5 part C: drawn between the ground pieces and the towers; null for nothing ({@link #underTowers}). */
    private UnderTowers underTowers;

    private final TextureRegion strip = new TextureRegion();
    /** M7 part C: the backdrop atlas and the level's folder in it (the canyon's images, {@link #canyon}). */
    private final Sprites sprites;

    private final String folder;
    /** M7 part C: the canyon's rock drawn with the ground's pieces; null without terrain ({@link #canyon}). */
    private WallProjection canyon;

    private Look from;
    private Look to;
    private float weight;

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

    private record Tiles(TextureRegion image, double drift) {}

    private record Look(Tiles banks, Tiles wisps, float haze) {}

    /**
     * A placed set piece with its images and its bottom edge on its layer; {@code pathStart} is its
     * path's first waypoint's script time (NaN without a path).
     */
    private record Piece(
            BackdropData.PlacedPiece placed,
            BackdropData.Piece spec,
            Array<AtlasRegion> images,
            long bottom,
            double pathStart) {}

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param levelKey the level's key, {@code <act>/level-NN-<slug>}
     */
    public Backdrop(Sprites sprites, LevelData level, String levelKey) {
        this.level = level;
        this.sprites = sprites;
        BackdropData data = level.backdrop();
        ramp = data.ramp();
        pixel = sprites.pixel;
        haze = Color.valueOf(data.hazeColour());
        folder = folder(levelKey, level);
        int layerCount = BackdropLayer.values().length;
        int sectionCount = level.sections().size();
        tiles = new Tiles[layerCount][sectionCount];
        seams = new long[layerCount][sectionCount];
        pieces = new Piece[layerCount][];
        for (BackdropLayer layer : BackdropLayer.values()) {
            int l = layer.ordinal();
            factors[l] = data.factor(layer);
            for (int s = 0; s < sectionCount; s++) {
                seams[l][s] = s == 0 ? Long.MIN_VALUE : Math.round(level.seam(layer, s));
            }
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
        for (Piece piece : onGround) {
            String light = piece.placed().piece() + GLOW;
            BackdropData.Piece spec = data.pieces().get(light);
            lights.add(
                    spec != null && spec.imageCount() == piece.images().size
                            ? sprites.backdrop(folder + light, spec.imageCount())
                            : null);
        }
        glows = lights;
        towers = new TowerProjection(towers(sprites, folder, level), pixel);
        for (int s = 0; s < sectionCount; s++) {
            for (String id : level.sections().get(s).tiles()) {
                BackdropData.TileSet tileSet = data.tileSets().get(id);
                tiles[tileSet.layer().ordinal()][s] = tiles(sprites, folder, id, tileSet);
            }
        }
        data.strip().ifPresent(keyed -> stripTiles(keyed, data));
        stretches = level.atmosphereStretches();
        looks = new Look[stretches.size()];
        for (int s = 0; s < looks.length; s++) {
            BackdropData.Look look =
                    data.atmosphere().of(stretches.get(s).atmosphere()).orElseThrow();
            looks[s] = new Look(
                    look.banks()
                            .map(id ->
                                    tiles(sprites, folder, id, data.tileSets().get(id)))
                            .orElse(null),
                    look.wisps()
                            .map(id ->
                                    tiles(sprites, folder, id, data.tileSets().get(id)))
                            .orElse(null),
                    (float) look.haze());
        }
        dust = level.collapse()
                .flatMap(collapse -> data.atmosphere().of(collapse.dust().atmosphere()))
                .map(look -> new Look(
                        look.banks()
                                .map(id -> tiles(
                                        sprites, folder, id, data.tileSets().get(id)))
                                .orElse(null),
                        look.wisps()
                                .map(id -> tiles(
                                        sprites, folder, id, data.tileSets().get(id)))
                                .orElse(null),
                        (float) look.haze()))
                .orElse(null);
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
     * M5 part G: the atmosphere as the difficulty shows it: on {@code easy} a peak with an easy length
     * that long (Level 13's whiteout, 6 s); the same stretches, only their times differ.
     */
    public void easy(boolean easy) {
        stretches = level.atmosphereStretches(easy);
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
    private boolean afterCrawl(BackdropData.PlacedPiece placed) {
        return crawlNext >= 0 && placed.t() >= crawlStart;
    }

    /**
     * M5 part C: the collapse's event-triggered dust peak (Level 09) shows at {@code weight}, 0 (the
     * section's own atmosphere) to 1 (the peak's: its banks, wisps and haze), blended like a change
     * across a section boundary.
     */
    public void dustPeak(float weight) {
        dustWeight = dust == null ? 0 : Math.clamp(weight, 0, 1);
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

    /**
     * M7 part C: the level's canyon from the simulation's {@code terrain} (null: none), drawn with the
     * ground's pieces ({@link WallProjection}) in the {@code looks} the level data gives it: the walls'
     * {@code canyon-wall} and {@code wall-foot}, the islands' {@code <look>-face} and {@code <look>-top}
     * images, the face patches' images, all from the level's backdrop folder.
     */
    void canyon(Terrain terrain, WallProjection.Looks looks) {
        if (terrain == null) {
            canyon = null;
            return;
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
    public void drawCanyonGlow(SpriteBatch batch) {
        if (canyon != null) {
            canyon.drawGlow(batch);
        }
    }

    /** What is drawn over the ground layer's set pieces and under its towers (Level 09's collapse). */
    interface UnderTowers {
        void draw(SpriteBatch batch);
    }

    /** M5 part C: {@code layer} is drawn between the ground pieces and the towers; null for nothing. */
    void underTowers(UnderTowers layer) {
        underTowers = layer;
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}). */
    TowerProjection.Tower tower(int index) {
        return towers.tower(index);
    }

    /**
     * The atlas folder a level's backdrop images come from: another level's when the data names one
     * (e.g. {@code images: level-04}, for a level without art of its own yet), else its own.
     */
    public static String folder(String levelKey, LevelData level) {
        return level.backdrop().images().map(folder -> folder + "/").orElseGet(() -> folder(levelKey));
    }

    /** The atlas folder of a level's backdrop: {@code level-NN/}. */
    static String folder(String levelKey) {
        String name = levelKey.substring(levelKey.lastIndexOf('/') + 1);
        return name.substring(0, "level-NN".length()) + "/";
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

    private static Tiles tiles(Sprites sprites, String folder, String id, BackdropData.TileSet tileSet) {
        return new Tiles(
                sprites.backdrop(folder + id, 1).first(), tileSet.drift().orElse(0.0));
    }

    /**
     * The layers below the ground: deep (if the level has one), far and the haze; then
     * {@link #drawGroundTiles}.
     *
     * @param groundScroll the ground layer's distance in px
     * @param clock the level clock and the real clock at the render time
     */
    public void drawBehind(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        blend(clock.script());
        if (level.backdrop().hasDeep()) {
            // A surface without a deep layer (Luna) has ground tiles that cover the screen.
            drawLayer(batch, BackdropLayer.DEEP, groundScroll, clock);
        }
        drawLayer(batch, BackdropLayer.FAR, groundScroll, clock);
        float veil = from.haze() + (to.haze() - from.haze()) * weight;
        if (veil > 0) {
            batch.setColor(haze.r, haze.g, haze.b, veil);
            batch.draw(pixel, X0, 0, WIDTH, HEIGHT);
            batch.setColor(Color.WHITE);
        }
    }

    /** The ground layer's tiles, over the haze (they catch the flyers' shadows, see {@link Shadows}). */
    public void drawGroundTiles(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        drawSectionTiles(batch, BackdropLayer.GROUND, scroll(BackdropLayer.GROUND, groundScroll), clock);
    }

    /**
     * The ground layer's set pieces, over its tiles and the level's road (design/campaign, Level 04:
     * the convoy apron, the bridge and the gate lie over the road), then its perspective towers,
     * lowest first, each its walls and then its roof (design/art-direction, Perspective towers are
     * scenery), with Level 09's collapse's shadow and base dust between them ({@link #underTowers}):
     * all under the ground objects and units drawn next, and marking the shadow stencil with the rest
     * of the ground (see {@link Shadows}). M7 part C: before the towers, a canyon's rock ({@link
     * WallProjection}): the foot bands and the rock's shadows on the floor, the walls' steps, the {@code
     * plateau} layer beyond the walls' rims (over the rock below the rim's height), the buttes (the tower
     * rule: ground units, bullets and the flyers' shadows stay over them).
     */
    public void drawGroundPieces(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        if (crawlSet && settle != null) {
            // M7 part H: the settling dust's soft edge over the crawl's next ground seam, under the other pieces.
            long y = settleBottom - scroll(BackdropLayer.GROUND, groundScroll);
            if (y < HEIGHT && y + settle.getRegionHeight() > 0) {
                batch.draw(settle, X0, y);
            }
        }
        drawPieces(batch, BackdropLayer.GROUND, scroll(BackdropLayer.GROUND, groundScroll), clock, false);
        if (underTowers != null) {
            underTowers.draw(batch);
        }
        if (canyon != null) {
            canyon.prepare(Math.round(groundScroll));
            canyon.drawFloor(batch);
            canyon.drawWalls(batch);
            drawPlateau(batch, groundScroll, clock);
            canyon.drawIslands(batch);
        }
        towers.draw(batch, groundScroll);
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
        int l = BackdropLayer.PLATEAU.ordinal();
        long scroll = scroll(BackdropLayer.PLATEAU, groundScroll);
        for (int y = 0; y < HEIGHT; y++) {
            float left = canyon.leftClip(y);
            float right = canyon.rightClip(y);
            if (Float.isNaN(left) && Float.isNaN(right)) {
                continue;
            }
            long row = scroll + y;
            Tiles section = null;
            for (int s = 0; s < tiles[l].length; s++) {
                if (seams[l][s] > row) {
                    break;
                }
                section = tiles[l][s];
            }
            if (section == null) {
                continue;
            }
            TextureRegion image = section.image();
            int top = image.getRegionHeight() - 1 - (int) Math.floorMod(row + phase[l], (long) image.getRegionHeight());
            clipped(batch, image, 0, top, WIDTH, 0, y, left, right);
        }
        for (Piece piece : pieces[l]) {
            BackdropData.PlacedPiece placed = piece.placed();
            int width = (int) Math.round(piece.spec().size().width());
            int height = (int) Math.round(piece.spec().size().height());
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            long bottom = piece.bottom() - scroll + Math.round(placed.offsetY(along));
            if (bottom >= HEIGHT || bottom + height <= 0) {
                continue;
            }
            long x = Math.round(placed.x() + placed.offsetX(along) - width / 2.0);
            TextureRegion image = piece.images().get(imageAt(piece, clock.real(), along));
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

    /**
     * The ground layer's overhead pieces, over the convoy and the ground objects and under the
     * ground units (design/campaign, Level 04: the crawlers pass under the bridge's arches and drive
     * into the gate; the Spine Turrets stand on the arches).
     */
    public void drawOverhead(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        drawPieces(batch, BackdropLayer.GROUND, scroll(BackdropLayer.GROUND, groundScroll), clock, true);
    }

    /** The low-air layer above the ground objects and below the play plane: its tiles, the cloud banks, its pieces. */
    public void drawLowAir(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        blend(clock.script());
        long scroll = scroll(BackdropLayer.LOW_AIR, groundScroll);
        drawSectionTiles(batch, BackdropLayer.LOW_AIR, scroll, clock);
        drawAtmosphere(batch, from.banks(), to.banks(), scroll, clock.real());
        drawPieces(batch, BackdropLayer.LOW_AIR, scroll, clock, false);
    }

    /**
     * M5 part H: the high-air layer's strength, 1 (its own) to 0 (gone): Level 14's canopy veins fading
     * out after the Siege Spire's death.
     */
    public void frontFade(float fade) {
        frontFade = Math.clamp(fade, 0, 1);
    }

    /** M5 part H: the high-air layer's scroll at the ground's {@code groundScroll}, px (Level 14's canopy tear tiles on it). */
    public long highAirScroll(double groundScroll) {
        return scroll(BackdropLayer.HIGH_AIR, groundScroll);
    }

    /** The high-air layer above the play plane, additive and at most 40 % opaque. */
    public void drawFront(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        blend(clock.script());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long scroll = scroll(BackdropLayer.HIGH_AIR, groundScroll);
        batch.setColor(1, 1, 1, HIGH_AIR_OPACITY * frontFade);
        drawSectionTiles(batch, BackdropLayer.HIGH_AIR, scroll, clock);
        drawPieces(batch, BackdropLayer.HIGH_AIR, scroll, clock, false);
        drawAtmosphere(batch, from.wisps(), to.wisps(), scroll, clock.real());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1, 1, 1, OVERHEAD_PASS_OPACITY * frontFade);
        drawPieces(batch, BackdropLayer.HIGH_AIR, scroll, clock, false, true);
        batch.setColor(Color.WHITE);
    }

    /**
     * Sets the looks to draw: the atmosphere stretch's (a section, or its peak), or the two across a
     * stretch boundary while the change ramps.
     */
    private void blend(double seconds) {
        int current = looks.length - 1;
        for (int s = 0; s < looks.length; s++) {
            if (seconds < stretches.get(s).end()) {
                current = s;
                break;
            }
        }
        from = to = looks[current];
        weight = 1;
        for (int s = 1; s < looks.length; s++) {
            double progress = (seconds - stretches.get(s).start()) / ramp + 0.5;
            if (progress > 0 && progress < 1) {
                from = looks[s - 1];
                to = looks[s];
                weight = (float) (progress * progress * (3 - 2 * progress));
            }
        }
        if (dustWeight > 0) {
            // The collapse's dust over whatever the section shows (it falls in a section's middle).
            from = weight < 0.5f ? from : to;
            to = dust;
            weight = dustWeight;
        }
    }

    private void drawLayer(SpriteBatch batch, BackdropLayer layer, double groundScroll, BackdropClock clock) {
        long scroll = scroll(layer, groundScroll);
        drawSectionTiles(batch, layer, scroll, clock);
        drawPieces(batch, layer, scroll, clock, false);
    }

    private long scroll(BackdropLayer layer, double groundScroll) {
        return Math.round(groundScroll * factors[layer.ordinal()]);
    }

    private void drawSectionTiles(SpriteBatch batch, BackdropLayer layer, long scroll, BackdropClock clock) {
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

    /** One look's tile set, or two crossfading while the atmosphere changes. */
    private void drawAtmosphere(SpriteBatch batch, Tiles old, Tiles current, long scroll, double seconds) {
        float alpha = batch.getColor().a;
        if (old != current && old != null) {
            batch.setColor(1, 1, 1, alpha * (1 - weight));
            drawTiles(batch, old, scroll, scroll + HEIGHT, scroll, seconds, 0);
        }
        if (current != null) {
            batch.setColor(1, 1, 1, old == current ? alpha : alpha * weight);
            drawTiles(batch, current, scroll, scroll + HEIGHT, scroll, seconds, 0);
        }
        batch.setColor(1, 1, 1, alpha);
    }

    /**
     * The rows {@code [from, to)} of a layer filled with a tile set that repeats from position {@code -phase} (0; M7
     * part G: a strip's origin, so its pixel rows follow the strip row), drifting by real {@code seconds}.
     */
    private void drawTiles(
            SpriteBatch batch, Tiles tileSet, long from, long to, long scroll, double seconds, long phase) {
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

    private void drawPieces(
            SpriteBatch batch, BackdropLayer layer, long scroll, BackdropClock clock, boolean overhead) {
        drawPieces(batch, layer, scroll, clock, overhead, false);
    }

    /** As above, only the high-air overhead passes ({@code passes}) or all but them. */
    private void drawPieces(
            SpriteBatch batch,
            BackdropLayer layer,
            long scroll,
            BackdropClock clock,
            boolean overhead,
            boolean passes) {
        for (Piece piece : pieces[layer.ordinal()]) {
            BackdropData.PlacedPiece placed = piece.placed();
            if (placed.isOverhead() != overhead || overheadPass(layer, placed) != passes) {
                continue;
            }
            long shift = 0;
            if (afterCrawl(placed)) {
                if (!crawlSet) {
                    continue;
                }
                shift = Math.round(crawlShift * factors[layer.ordinal()]);
            }
            float width = (float) piece.spec().size().width();
            float height = (float) piece.spec().size().height();
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            float y = piece.bottom() + shift - scroll + Math.round(placed.offsetY(along));
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            float x = X0 + Math.round(placed.x() + placed.offsetX(along) - width / 2);
            TextureRegion image = piece.images().get(imageAt(piece, clock.real(), along));
            if (placed.mirrored()) {
                batch.draw(image, x + width, y, -width, height);
            } else {
                batch.draw(image, x, y);
            }
        }
    }

    /** M8 part D: the suffix of a ground piece's light ({@link #drawPieceGlows}). */
    static final String GLOW = "-glow";

    /** M8 part D: whether a ground piece has its light ({@link #drawPieceGlows}): Level 24's relay beacons. */
    public boolean hasPieceGlows() {
        for (Array<AtlasRegion> light : glows) {
            if (light != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * M8 part D: the ground pieces' lights (Level 24's relay beacons' pulse), additive, at their pieces' places and on
     * their frame clock, after the under-water grade so it does not dim them.
     */
    public void drawPieceGlows(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        long scroll = scroll(BackdropLayer.GROUND, groundScroll);
        boolean additive = false;
        for (int p = 0; p < onGround.length; p++) {
            Piece piece = onGround[p];
            Array<AtlasRegion> light = glows.get(p);
            if (light == null || afterCrawl(piece.placed())) {
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
            TextureRegion image = light.get(imageAt(piece, clock.real(), along));
            if (piece.placed().mirrored()) {
                batch.draw(image, x + width, y, -width, height);
            } else {
                batch.draw(image, x, y);
            }
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** M7 part B: whether a placed piece is a huge overhead pass on high-air ({@link #OVERHEAD_PASSES}). */
    static boolean overheadPass(BackdropLayer layer, BackdropData.PlacedPiece placed) {
        return layer == BackdropLayer.HIGH_AIR && OVERHEAD_PASSES.contains(placed.piece());
    }

    /** The animation frame at real {@code seconds}, the heading along the path at {@code along}, or the only image. */
    private static int imageAt(Piece piece, double seconds, double along) {
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
