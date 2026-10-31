package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;
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
 * ({@link BackdropTiles}); every layer scrolls with the ground's signed distance, so in a reversed section new ground
 * enters at the bottom edge and the set pieces leave by the top.
 *
 * <p>RF9d: drawn by layer: the tiles and the crawl ({@link BackdropTiles}), the atmosphere ({@link
 * BackdropAtmosphere}), the set pieces and towers ({@link BackdropPieces}) and the canyon walls ({@link TrenchWalls});
 * this class keeps the draw passes' order and the API its callers use.
 */
public final class Backdrop {
    /** Weather on high-air may cover the play plane at no more than 40 % opacity. */
    private static final float HIGH_AIR_OPACITY = 0.4f;
    /**
     * M7 part B: a huge overhead pass on high-air (art direction, the layer table: 75 % so the ship shows
     * under it, normal blending): Level 15's Mars transport over the launch (tools/art/backdrop_l15.py
     * proposes the key {@code overhead_pass}; kept as this piece-name rule in the game, no data key, pending
     * the user's review in round 38).
     */
    static final float OVERHEAD_PASS_OPACITY = 0.75f;

    /** M8 part C: an ice shaft's wall texture ({@link TrenchWalls#ICE_WALL}). */
    static final String ICE_WALL = TrenchWalls.ICE_WALL;

    private final LevelData level;
    private final BackdropPieces pieces;
    private final BackdropTiles tiles;
    private final BackdropAtmosphere atmosphere;
    private final TrenchWalls trench;
    /** M5 part C: drawn between the ground pieces and the towers; null for nothing ({@link #underTowers}). */
    private UnderTowers underTowers;

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param levelKey the level's key, {@code <act>/level-NN-<slug>}
     */
    public Backdrop(Sprites sprites, LevelData level, String levelKey) {
        this.level = level;
        String folder = folder(levelKey, level);
        // The atlas lookups in their order: the pieces, their lights and holds, the towers; the tile sets; the looks.
        pieces = new BackdropPieces(sprites, level, folder);
        tiles = new BackdropTiles(sprites, level, folder);
        atmosphere = new BackdropAtmosphere(sprites, level, folder, tiles);
        trench = new TrenchWalls(sprites, level, folder, tiles, pieces);
    }

    /** M9 part B: the hold opened {@code ticks} steps ago (the escort swap's release), -1 while it is shut. */
    public void holdOpened(int ticks) {
        pieces.holdOpened(ticks);
    }

    /**
     * M9 part C: after a frame's draw, the play-field x of a strobe whose flash started in it (NaN for none); the next
     * frame starts afresh.
     */
    public double strobed() {
        return pieces.strobed();
    }

    /**
     * M5 part G: the atmosphere as the difficulty shows it: on {@code easy} a peak with an easy length
     * that long (Level 13's whiteout, 6 s); the same stretches, only their times differ.
     */
    public void easy(boolean easy) {
        atmosphere.easy(easy);
    }

    /** M10 part A: the foreground banks as {@code difficulty} draws them (easy leaves some out). */
    public void difficulty(vanguard.content.Difficulty difficulty) {
        atmosphere.easy(difficulty == vanguard.content.Difficulty.EASY);
        atmosphere.difficulty(difficulty);
    }

    /** M10 part A: the foreground cloud banks (Level 36); null in a level without them. */
    ForegroundBanks banks() {
        return atmosphere.banks();
    }

    /**
     * M10 part A (design/world/jovian, foreground cloud banks): the banks on the {@code high-air} scenery over the {@code
     * air} bodies, for the ground scrolled by {@code groundScroll}, seen through by {@code seeThrough} (a flash's reveal).
     */
    public void drawForegroundBanks(SpriteBatch batch, double groundScroll, float seeThrough) {
        atmosphere.drawForegroundBanks(batch, groundScroll, seeThrough);
    }

    /** M10 part A: frees what the backdrop made itself (the banks' veil); the atlases are the sprites'. */
    public void dispose() {
        atmosphere.dispose();
    }

    /** M7 part H: section {@code arena} (0-based) is an arena whose ground crawls on ({@link BackdropTiles#crawls}). */
    void crawls(int arena) {
        tiles.crawls(arena);
    }

    /** M7 part H: the crawl's next section has not started (a restart, a retry from the boss): its seams wait. */
    void crawlPending() {
        tiles.crawlPending();
    }

    /** M7 part H: whether the crawl's next section has started ({@link #crawlEnded(double)}); false without a crawl. */
    boolean crawlEnded() {
        return tiles.crawlEnded();
    }

    /** M7 part H: the crawl's next section starts with the ground at {@code ground} ({@link BackdropTiles#crawlEnded}). */
    void crawlEnded(double ground) {
        tiles.crawlEnded(ground);
    }

    /** M5 part C: the collapse's dust peak (Level 09) shows at {@code weight}, 0 to 1. */
    public void dustPeak(float weight) {
        atmosphere.dustPeak(weight);
    }

    /**
     * M5 part C: the tower whose footprint's centre lies nearest {@code groundCentre} on the ground
     * layer, within {@code reach} px; -1 for none.
     */
    int towerNear(double groundCentre, double reach) {
        return pieces.towerNear(groundCentre, reach);
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}) is not drawn (the fallen arcology); -1 draws them all. */
    void hideTower(int index) {
        pieces.hideTower(index);
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}) is drawn bent (see {@link TowerProjection#bend}); -1 for none. */
    void bendTower(int index, double lean, double shakeX, double shakeY, double share, double crush, float dust) {
        pieces.bendTower(index, lean, shakeX, shakeY, share, crush, dust);
    }

    /** M7 part C: the level's canyon from the simulation's {@code terrain} (null: none), see {@link TrenchWalls#canyon}. */
    void canyon(Terrain terrain, WallProjection.Looks looks) {
        trench.canyon(terrain, looks);
    }

    /** M8 part C: an ice shaft's walls are wet from terrain row {@code row} up (the waterline). */
    void wetFrom(double row) {
        trench.wetFrom(row);
    }

    /** M8 part C: an ice shaft's collar strips glowing over its walls, after the grade (nothing for a canyon). */
    public void drawCanyonGlow(SpriteBatch batch) {
        trench.drawCanyonGlow(batch);
    }

    /** What is drawn over the ground layer's set pieces and under its towers (Level 09's collapse). */
    interface UnderTowers {
        void draw(SpriteBatch batch);
    }

    /** M8 part E: {@code layer} is drawn under the canyon's walls, over its floor; null for nothing. */
    void underWalls(UnderTowers layer) {
        trench.underWalls(layer);
    }

    /** M5 part C: {@code layer} is drawn between the ground pieces and the towers; null for nothing. */
    void underTowers(UnderTowers layer) {
        underTowers = layer;
    }

    /** M5 part C: tower {@code index} ({@link #towerNear}). */
    TowerProjection.Tower tower(int index) {
        return pieces.tower(index);
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

    /**
     * The layers below the ground: deep (if the level has one), far and the haze; then
     * {@link #drawGroundTiles}.
     *
     * @param groundScroll the ground layer's distance in px
     * @param clock the level clock and the real clock at the render time
     */
    public void drawBehind(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        atmosphere.blend(clock.script());
        if (level.backdrop().hasDeep()) {
            // A surface without a deep layer (Luna) has ground tiles that cover the screen.
            drawLayer(batch, BackdropLayer.DEEP, groundScroll, clock);
        }
        drawLayer(batch, BackdropLayer.FAR, groundScroll, clock);
        atmosphere.drawHaze(batch);
    }

    /** The ground layer's tiles, over the haze (they catch the flyers' shadows, see {@link Shadows}). */
    public void drawGroundTiles(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        tiles.drawSectionTiles(batch, BackdropLayer.GROUND, tiles.scroll(BackdropLayer.GROUND, groundScroll), clock);
    }

    /**
     * The ground layer's set pieces, over its tiles and the level's road (design/campaign, Level 04:
     * the convoy apron, the bridge and the gate lie over the road), then its perspective towers,
     * lowest first, each its walls and then its roof (design/art-direction, Perspective towers are
     * scenery), with Level 09's collapse's shadow and base dust between them ({@link #underTowers}):
     * all under the ground objects and units drawn next, and marking the shadow stencil with the rest
     * of the ground (see {@link Shadows}). M7 part C: before the towers, a canyon's rock ({@link
     * TrenchWalls#draw}): the foot bands and the rock's shadows on the floor, the walls' steps, the {@code
     * plateau} layer beyond the walls' rims (over the rock below the rim's height), the buttes (the tower
     * rule: ground units, bullets and the flyers' shadows stay over them).
     */
    public void drawGroundPieces(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        // M7 part H: the settling dust's soft edge over the crawl's next ground seam, under the other pieces.
        tiles.drawSettle(batch, groundScroll);
        pieces.drawPieces(
                batch, BackdropLayer.GROUND, tiles.scroll(BackdropLayer.GROUND, groundScroll), clock, false, tiles);
        if (underTowers != null) {
            underTowers.draw(batch);
        }
        trench.draw(batch, groundScroll, clock);
        pieces.drawTowers(batch, groundScroll);
    }

    /**
     * The ground layer's overhead pieces, over the convoy and the ground objects and under the
     * ground units (design/campaign, Level 04: the crawlers pass under the bridge's arches and drive
     * into the gate; the Spine Turrets stand on the arches).
     */
    public void drawOverhead(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        pieces.drawPieces(
                batch, BackdropLayer.GROUND, tiles.scroll(BackdropLayer.GROUND, groundScroll), clock, true, tiles);
    }

    /** The low-air layer above the ground objects and below the play plane: its tiles, the cloud banks, its pieces. */
    public void drawLowAir(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        atmosphere.blend(clock.script());
        long scroll = tiles.scroll(BackdropLayer.LOW_AIR, groundScroll);
        tiles.drawSectionTiles(batch, BackdropLayer.LOW_AIR, scroll, clock);
        atmosphere.drawBanks(batch, scroll, clock.real());
        pieces.drawPieces(batch, BackdropLayer.LOW_AIR, scroll, clock, false, tiles);
    }

    /**
     * M5 part H: the high-air layer's strength, 1 (its own) to 0 (gone): Level 14's canopy veins fading
     * out after the Siege Spire's death.
     */
    public void frontFade(float fade) {
        atmosphere.frontFade(fade);
    }

    /** M5 part H: the high-air layer's scroll at the ground's {@code groundScroll}, px (Level 14's canopy tear tiles on it). */
    public long highAirScroll(double groundScroll) {
        return tiles.highAirScroll(groundScroll);
    }

    /** The high-air layer above the play plane, additive and at most 40 % opaque. */
    public void drawFront(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        atmosphere.blend(clock.script());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long scroll = tiles.scroll(BackdropLayer.HIGH_AIR, groundScroll);
        batch.setColor(1, 1, 1, HIGH_AIR_OPACITY * atmosphere.frontFade());
        tiles.drawSectionTiles(batch, BackdropLayer.HIGH_AIR, scroll, clock);
        pieces.drawPieces(batch, BackdropLayer.HIGH_AIR, scroll, clock, false, tiles);
        atmosphere.drawWisps(batch, scroll, clock.real());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1, 1, 1, OVERHEAD_PASS_OPACITY * atmosphere.frontFade());
        pieces.drawPieces(batch, BackdropLayer.HIGH_AIR, scroll, clock, false, true, tiles);
        batch.setColor(Color.WHITE);
    }

    private void drawLayer(SpriteBatch batch, BackdropLayer layer, double groundScroll, BackdropClock clock) {
        long scroll = tiles.scroll(layer, groundScroll);
        tiles.drawSectionTiles(batch, layer, scroll, clock);
        pieces.drawPieces(batch, layer, scroll, clock, false, tiles);
    }

    /** M8 part D: whether a ground piece has its light ({@link #drawPieceGlows}): Level 24's relay beacons. */
    public boolean hasPieceGlows() {
        return pieces.hasPieceGlows();
    }

    /**
     * M8 part D: the ground pieces' lights (Level 24's relay beacons' pulse), additive, at their pieces' places and on
     * their frame clock, after the under-water grade so it does not dim them.
     */
    public void drawPieceGlows(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        pieces.drawPieceGlows(batch, tiles.scroll(BackdropLayer.GROUND, groundScroll), clock, tiles);
    }
}
