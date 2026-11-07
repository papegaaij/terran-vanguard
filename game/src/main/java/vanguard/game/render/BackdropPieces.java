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
    /** M11 part B: per layer and piece whether its path parks it off the field ({@link #parks}). */
    private final boolean[][] parking;
    /** M11 part B: per layer and piece the layer's slide when it was last unparked ({@link #slide}); NONE parked. */
    private final int[][] anchors;
    /** M11 part B: no anchor yet (the piece is parked or not yet drawn). */
    private static final int NONE = Integer.MIN_VALUE;

    /**
     * M11 part B, step B4 (design/campaign Level 43, the backdrop; m11b-gaps.md §4: the ring at three scales): the
     * pieces held on the screen by their path and parked {@value #PARKED} px aside outside their section, which
     * cross-fade over {@value #CROSS_FADE_SECONDS} s at their swaps instead of jumping (the gate ring's far, third,
     * top and rim pieces and the mouth). Their {@code -glow} lights follow them.
     */
    static final java.util.Set<String> CROSS_FADED =
            java.util.Set.of("ring-far", "ring-third", "ring-top", "ring-rim", "mouth");

    static final double CROSS_FADE_SECONDS = 2;
    /** M11 part B: a path's sideways offset this far or more parks its piece off the field (the data's 3000). */
    static final double PARKED = 2000;
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
    /**
     * M10 part D (design/campaign Level 38, the backdrop): per deep piece whether its light flashes brighter on a told
     * strike: the storm's walls and cells ({@link #STRIKE_LIT}), whose light's frame 0 is its brightest flash
     * (tools/art/backdrop_l38.py).
     */
    private final boolean[] strikeLit;
    /** M10 part D: a told strike's pulse now, 0-1 ({@link Backdrop#strikePulse}). */
    private float strikePulse;

    /** M9 part B: steps since the hold opened (the escort swap's release); -1 while it is shut. */
    private int holdTicks = -1;

    /**
     * M11 part C, step C4 (design/campaign Level 45; tools/art/act7_reef.py: the placed {@code pit} pieces are the
     * level's pits in their order): per ground piece its pit's index, -1 for a piece that is not one; and the pits'
     * frames by their state ({@link PitFrames}), null in a level without placed pits.
     */
    static final String PIT = "pit";

    private final int[] pitOf;
    private final PitFrames pitFrames;
    /**
     * M11 part F, step F4 (design/campaign Level 48; tools/art/act7_citadel.py): the spire well's frames by the boss and
     * the reactor cores' veins ({@link CitadelLooks}); null in a level without them.
     */
    private final CitadelLooks citadel;
    /**
     * M11 part C, step C4 (Level 45: "every ground {@code -veins} image pulses"): per ground piece its veins' pulsing
     * share, an undeclared image {@code <id>-veins} the atlas holds (additive over the piece), or null; and the pulse
     * now, 0-1 ({@link VeinPulse}).
     */
    private final List<Array<AtlasRegion>> veins;

    private float veinPulse;
    /**
     * M11 part D, step D4 (design/campaign Level 46, the warm hull; tools/art/act7_deadfleet.py: "warm-hull-glow fades out
     * as the core rises"): the core's warm light on the hull's ribs ({@value #CORE_GLOW}, additive) at this share, 1
     * while the core sits in the hull ({@link Backdrop#storyStepped}).
     */
    static final String CORE_GLOW = "warm-hull-glow";

    private float coreGlow = 1;

    /**
     * M10 part E, step E4 (design/campaign Level 39, the pipes going dark): per ground piece the real time its light went
     * dark ({@link #darken}, its {@code dark_with} tower group cleared or pump station destroyed), {@link
     * Double#POSITIVE_INFINITY} once asked until its next draw starts the fade, NaN while lit.
     */
    private final double[] darkAt;

    /** M10 part E: a light going dark fades out over this long, s. */
    static final double DARK_SECONDS = 0.6;

    /**
     * M10 part E: the lights of the ground pieces whose {@code dark_with} names {@code name} (a tower's group, {@code
     * Loki A}, or a pump station, {@code P3}) go dark, fading from their next draw; presentation only.
     */
    void darken(String name) {
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        for (int p = 0; p < onGround.length; p++) {
            if (Double.isNaN(darkAt[p])
                    && onGround[p].placed().darkWith().filter(name::equals).isPresent()) {
                darkAt[p] = Double.POSITIVE_INFINITY;
            }
        }
    }

    /** M10 part E: every light is lit again (the level restarts). */
    void lightAll() {
        java.util.Arrays.fill(darkAt, Double.NaN);
    }

    /** M10 part E: whether ground piece {@code p}'s light is going or gone dark (for the tests). */
    boolean dark(int p) {
        return !Double.isNaN(darkAt[p]);
    }

    /** M10 part E: a light's share at real time {@code real} after going dark at {@code at} (NaN: lit), 0-1. */
    static float litShare(double at, double real) {
        if (Double.isNaN(at)) {
            return 1;
        }
        return (float) Math.clamp(1 - (real - at) / DARK_SECONDS, 0, 1);
    }

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
            double pathStart,
            boolean light) {}

    /**
     * M10 part C, step C4a: the level time from which the ground's placed pieces are not drawn (Level 37's falling city:
     * its halves are the ground then, {@link FallingCity}); infinite for none.
     */
    private double groundUntil = Double.POSITIVE_INFINITY;

    /** M10 part C: the ground's placed pieces are not drawn from level time {@code t} on ({@link #groundUntil}). */
    void groundUntil(double t) {
        groundUntil = t;
    }

    /** M10 part C: whether a layer's placed pieces are drawn at level time {@code script} ({@link #groundUntil}). */
    private boolean shown(BackdropLayer layer, double script) {
        return layer != BackdropLayer.GROUND || script < groundUntil;
    }

    /**
     * M10 part C, step C4a (tools/art/backdrop_l37.py: "the -glow pieces are additive at their piece's places"): a placed
     * piece {@code <id>-glow} at the same time and x as a placed {@code <id>} on its layer is its light, drawn additively
     * (Level 37's lift rings); a {@code -glow} piece placed alone keeps the normal blend (Level 01's {@code vrell-glow}).
     */
    static boolean light(BackdropData.PlacedPiece placed, List<BackdropData.PlacedPiece> all) {
        if (!placed.piece().endsWith(GLOW)) {
            return false;
        }
        String base = placed.piece().substring(0, placed.piece().length() - GLOW.length());
        for (BackdropData.PlacedPiece other : all) {
            if (other.piece().equals(base) && other.t() == placed.t() && other.x() == placed.x()) {
                return true;
            }
        }
        return false;
    }

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
                    onLayer.add(new Piece(placed, spec, images, bottom, pathStart, light(placed, data.placements())));
                }
            }
            pieces[l] = onLayer.toArray(Piece[]::new);
        }
        parking = new boolean[pieces.length][];
        anchors = new int[pieces.length][];
        for (int l = 0; l < pieces.length; l++) {
            parking[l] = new boolean[pieces[l].length];
            anchors[l] = new int[pieces[l].length];
            java.util.Arrays.fill(anchors[l], NONE);
            for (int p = 0; p < pieces[l].length; p++) {
                parking[l][p] = parks(pieces[l][p].placed());
            }
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
        strikeLit = new boolean[onDeep.length];
        for (int p = 0; p < onDeep.length; p++) {
            String light = onDeep[p].placed().piece() + GLOW;
            BackdropData.Piece spec = data.pieces().get(light);
            if (spec == null || spec.layer() != BackdropLayer.DEEP) {
                deepLights.add(null);
                continue;
            }
            deepLights.add(sprites.backdrop(folder + light, spec.imageCount()));
            deepGlowFps[p] = spec.animated() ? spec.fps().orElseThrow() : 0;
            strikeLit[p] = strikeLit(onDeep[p].placed().piece());
        }
        deepGlows = deepLights;
        darkAt = new double[onGround.length];
        lightAll();
        pitOf = new int[onGround.length];
        int pits = 0;
        List<Array<AtlasRegion>> veined = new ArrayList<>();
        for (int p = 0; p < onGround.length; p++) {
            boolean pit = onGround[p].placed().piece().equals(PIT) && onGround[p].images().size >= PitFrames.FRAMES;
            pitOf[p] = pit ? pits++ : -1;
            veined.add(undeclared(sprites, folder + onGround[p].placed().piece() + VeinPulse.VEINS, 1));
        }
        pitFrames = pits > 0 ? new PitFrames(pits) : null;
        veins = veined;
        String[] names = new String[onGround.length];
        for (int p = 0; p < onGround.length; p++) {
            names[p] = onGround[p].placed().piece();
        }
        citadel = CitadelLooks.of(names, glow -> {
            BackdropData.Piece spec = data.pieces().get(glow);
            return spec == null ? null : sprites.backdrop(folder + glow, spec.imageCount());
        });
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
            double scroll,
            BackdropClock clock,
            boolean overhead,
            BackdropTiles crawl) {
        drawPieces(batch, layer, scroll, clock, overhead, false, crawl);
    }

    /** As above, only the high-air overhead passes ({@code passes}) or all but them. */
    void drawPieces(
            SpriteBatch batch,
            BackdropLayer layer,
            double scroll,
            BackdropClock clock,
            boolean overhead,
            boolean passes,
            BackdropTiles crawl) {
        if (!shown(layer, clock.script())) {
            return;
        }
        Piece[] onLayer = pieces[layer.ordinal()];
        for (int p = 0; p < onLayer.length; p++) {
            Piece piece = onLayer[p];
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
            double run = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            // M11 part B: a cross-faded piece holds its place while it fades in before its section or out after it.
            double along = fadeTime(placed, run);
            float fade = fadeAlpha(placed, run);
            if (coreGlow < 1 && placed.piece().equals(CORE_GLOW)) {
                // M11 part D: the warm hull's light going out as its core rises (Level 46).
                fade *= coreGlow;
            }
            if (fade <= 0) {
                continue;
            }
            float y = pieceY(piece, shift, scroll, along);
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            // M11 part A: a drift level's layers slide sideways at their factor (BackdropTiles#lateral); M11 part B: a
            // parked piece from where the layer had slid when it came on (slide).
            float x = X0
                    + Math.round(placed.x() + placed.offsetX(along) - width / 2)
                    + slide(layer, p, placed.offsetX(along), crawl);
            int frame = frameOf(layer, p, piece, clock.real(), along);
            TextureRegion image = piece.images().get(frame);
            if (STROBES.contains(placed.piece()) && piece.spec().animated()) {
                // M9 part C: a lane buoy's strobe flashes in frame 0 (its tick, BUOY_TICK).
                strobeSeen(frame, placed.x());
            }
            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();
            if (piece.light()) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            }
            if (fade < 1) {
                batch.setColor(1, 1, 1, fade);
            }
            if (placed.mirrored()) {
                batch.draw(image, x + width, y, -width, height);
            } else {
                batch.draw(image, x, y);
            }
            if (fade < 1) {
                batch.setColor(1, 1, 1, 1);
            }
            if (piece.light()) {
                batch.setBlendFunction(src, dst);
            }
            if (layer == BackdropLayer.GROUND && holdTicks >= 0) {
                drawHold(batch, piece, x, y, width, height);
            }
            if (layer == BackdropLayer.GROUND && veinPulse > 0 && veins.get(p) != null) {
                // M11 part C: the piece's veins pulsing over it (Level 45's seam, edge and second-reef pieces).
                drawVeins(batch, veins.get(p).first(), placed.mirrored(), x, y, width, height, veinPulse * fade);
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
    void drawDeepGlows(SpriteBatch batch, double scroll, BackdropClock clock, BackdropTiles crawl) {
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
            double run = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            double along = fadeTime(piece.placed(), run);
            float fade = fadeAlpha(piece.placed(), run);
            float y = pieceY(piece, 0, scroll, along);
            if (fade <= 0 || y >= HEIGHT || y + height <= 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            float x = X0
                    + Math.round(piece.placed().x() + piece.placed().offsetX(along) - width / 2)
                    + slide(BackdropLayer.DEEP, p, piece.placed().offsetX(along), crawl);
            batch.setColor(1, 1, 1, fade);
            int frame = deepGlowFps[p] > 0
                    ? (int) Math.floorMod((long) Math.floor(clock.real() * deepGlowFps[p]), (long) light.size)
                    : Math.min(light.size - 1, imageAt(piece, clock.real(), along));
            drawLight(batch, light.get(frame), piece.placed().mirrored(), x, y, width, height);
            batch.setColor(1, 1, 1, 1);
            if (strikeLit[p] && strikePulse > 0 && frame != 0) {
                // M10 part D: a told strike lights the wall from inside: its brightest flash over its own frame.
                batch.setColor(1, 1, 1, strikePulse);
                drawLight(batch, light.get(0), piece.placed().mirrored(), x, y, width, height);
                batch.setColor(1, 1, 1, 1);
            }
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** M10 part D: the deep pieces whose light flashes on a told strike (Level 38's cloud walls and storm cells). */
    static final java.util.List<String> STRIKE_LIT = java.util.List.of("cloud-wall", "storm-cell");

    /** M10 part D: whether a deep piece's light flashes brighter on a told strike ({@link #STRIKE_LIT}). */
    static boolean strikeLit(String piece) {
        for (String lit : STRIKE_LIT) {
            if (piece.equals(lit) || piece.startsWith(lit + "-")) {
                return true;
            }
        }
        return false;
    }

    /** M10 part D: a told strike's pulse now ({@link Backdrop#strikePulse}). */
    void strikePulse(float pulse) {
        strikePulse = pulse;
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
    void drawPieceGlows(SpriteBatch batch, double scroll, BackdropClock clock, BackdropTiles crawl) {
        if (!shown(BackdropLayer.GROUND, clock.script())) {
            return;
        }
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        boolean additive = false;
        if (citadel != null) {
            additive = true;
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            drawVeins(batch, scroll, clock, crawl);
        }
        for (int p = 0; p < onGround.length; p++) {
            Piece piece = onGround[p];
            Array<AtlasRegion> light = glows.get(p);
            if (light == null || crawl.afterCrawl(piece.placed())) {
                continue;
            }
            float height = (float) piece.spec().size().height();
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            float y = pieceY(piece, 0, scroll, along);
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            if (darkAt[p] == Double.POSITIVE_INFINITY) {
                darkAt[p] = clock.real();
            }
            float lit = litShare(darkAt[p], clock.real());
            if (lit <= 0) {
                continue;
            }
            float width = (float) piece.spec().size().width();
            float x = X0
                    + Math.round(piece.placed().x() + piece.placed().offsetX(along) - width / 2)
                    + slide(BackdropLayer.GROUND, p, piece.placed().offsetX(along), crawl);
            int frame = frameOf(BackdropLayer.GROUND, p, piece, clock.real(), along);
            Array<AtlasRegion> red = turnedGlows.get(p);
            float turn = red == null ? 0 : turnShare(turnAt, clock.script());
            if (turn < 1) {
                if (turn > 0 || lit < 1) {
                    batch.setColor(1, 1, 1, (1 - turn) * lit);
                }
                drawLight(batch, light.get(frame), piece.placed().mirrored(), x, y, width, height);
            }
            if (turn > 0) {
                batch.setColor(1, 1, 1, turn * lit);
                drawLight(batch, red.get(frame), piece.placed().mirrored(), x, y, width, height);
            }
            batch.setColor(1, 1, 1, 1);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** M11 part F: the reactor cores' veins' glows (Level 48, {@link CitadelLooks}), additive (the caller's blend). */
    private void drawVeins(SpriteBatch batch, double scroll, BackdropClock clock, BackdropTiles crawl) {
        Piece[] onGround = pieces[BackdropLayer.GROUND.ordinal()];
        for (int p = 0; p < onGround.length; p++) {
            int v = citadel.vein(p);
            Piece piece = onGround[p];
            if (v < 0 || crawl.afterCrawl(piece.placed())) {
                continue;
            }
            float height = (float) piece.spec().size().height();
            double along = Double.isNaN(piece.pathStart()) ? 0 : clock.pathTime(piece.pathStart());
            float y = pieceY(piece, 0, scroll, along);
            if (y >= HEIGHT || y + height <= 0) {
                continue;
            }
            float width = (float) piece.spec().size().width();
            double centre = piece.placed().x() + piece.placed().offsetX(along);
            float x = X0
                    + Math.round(centre - width / 2)
                    + slide(BackdropLayer.GROUND, p, piece.placed().offsetX(along), crawl);
            citadel.drawVein(batch, v, x, y, centre, piece.placed().mirrored(), clock.real());
        }
    }

    /** M11 part F: Level 48's citadel hooks, null without them (for the backdrop's steps and the tests). */
    CitadelLooks citadel() {
        return citadel;
    }

    private static void drawLight(
            SpriteBatch batch, TextureRegion image, boolean mirrored, float x, float y, float width, float height) {
        if (mirrored) {
            batch.draw(image, x + width, y, -width, height);
        } else {
            batch.draw(image, x, y);
        }
    }

    /** M11 part C: a piece's veins, additive at {@code share} of their light. */
    private static void drawVeins(
            SpriteBatch batch,
            TextureRegion image,
            boolean mirrored,
            float x,
            float y,
            float width,
            float height,
            float share) {
        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1, 1, 1, share);
        drawLight(batch, image, mirrored, x, y, width, height);
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(src, dst);
    }

    /** M11 part D: the warm hull's light now, 0-1 ({@link #CORE_GLOW}). */
    void coreGlow(float share) {
        coreGlow = share;
    }

    /** M11 part D: the warm hull's light now (for the tests). */
    float coreGlow() {
        return coreGlow;
    }

    /** M11 part C: the veins' pulse now, 0-1 ({@link VeinPulse#share}). */
    void veinPulse(float share) {
        veinPulse = share;
    }

    /** M11 part C: whether any ground piece has its pulsing veins. */
    boolean hasVeins() {
        for (Array<AtlasRegion> vein : veins) {
            if (vein != null) {
                return true;
            }
        }
        return false;
    }

    /** M11 part C: the level's pits' frames by their state, null without placed pits ({@link PitFrames}). */
    PitFrames pitFrames() {
        return pitFrames;
    }

    /** M11 part C: ground piece {@code p}'s pit index, -1 for a piece that is not a pit (for the tests). */
    int pitOf(int p) {
        return pitOf[p];
    }

    /**
     * The frame of piece {@code p} of {@code layer}: a placed pit's by its state (M11 part C, {@link PitFrames}), any
     * other's by {@link #imageAt}.
     */
    private int frameOf(BackdropLayer layer, int p, Piece piece, double real, double along) {
        if (layer == BackdropLayer.GROUND && pitFrames != null && pitOf[p] >= 0) {
            return Math.min(piece.images().size - 1, pitFrames.frame(pitOf[p], real));
        }
        if (layer == BackdropLayer.GROUND && citadel != null && citadel.well(p)) {
            // M11 part F: the spire well shut until the boss arrives, its lid splitting, then open (Level 48).
            return citadel.wellFrame(piece.images().size);
        }
        return imageAt(piece, real, along);
    }

    /** M7 part B: whether a placed piece is a huge overhead pass on high-air ({@link #OVERHEAD_PASSES}). */
    static boolean overheadPass(BackdropLayer layer, BackdropData.PlacedPiece placed) {
        return layer == BackdropLayer.HIGH_AIR && OVERHEAD_PASSES.contains(placed.piece());
    }

    /** M11 part B: whether a path parks its piece off the field at some point (a waypoint {@value #PARKED} px aside). */
    static boolean parks(BackdropData.PlacedPiece placed) {
        return placed.path()
                .map(points -> points.stream().anyMatch(point -> parked(point.dx())))
                .orElse(false);
    }

    private static boolean parked(double dx) {
        return Math.abs(dx) >= PARKED;
    }

    /**
     * M11 part B (design/enemies, Distorted space; Level 44's curtain and beyond): the layer's sideways slide for piece
     * {@code p}: a piece its path parks takes the slide from where the layer had slid when it came on the field (so
     * Level 44's Vrell space behind the curtain, on from t=252, stays inside its 800 px whatever the drift before);
     * every other piece the layer's whole slide ({@link BackdropTiles#lateral}).
     */
    private int slide(BackdropLayer layer, int p, double dx, BackdropTiles crawl) {
        int slide = crawl.lateral(layer);
        int l = layer.ordinal();
        if (!parking[l][p]) {
            return slide;
        }
        if (parked(dx)) {
            anchors[l][p] = NONE;
            return slide;
        }
        if (anchors[l][p] == NONE) {
            anchors[l][p] = slide;
        }
        return slide - anchors[l][p];
    }

    /** M11 part B: the path time a cross-faded piece's first unparked waypoint begins; NaN for none. */
    static double unparkedAt(BackdropData.PlacedPiece placed) {
        if (placed.path().isEmpty()) {
            return Double.NaN;
        }
        List<BackdropData.Waypoint> points = placed.path().get();
        for (int i = 1; i < points.size(); i++) {
            if (parked(points.get(i - 1).dx()) && !parked(points.get(i).dx())) {
                return points.get(i).t();
            }
        }
        return Double.NaN;
    }

    /** M11 part B: the path time a cross-faded piece's last unparked waypoint before it parks again; NaN for none. */
    static double parkedFrom(BackdropData.PlacedPiece placed) {
        if (placed.path().isEmpty()) {
            return Double.NaN;
        }
        List<BackdropData.Waypoint> points = placed.path().get();
        for (int i = 1; i < points.size(); i++) {
            if (!parked(points.get(i - 1).dx()) && parked(points.get(i).dx())) {
                return points.get(i - 1).t();
            }
        }
        return Double.NaN;
    }

    /**
     * M11 part B: the path time a piece is placed at when its path has run {@code along}: a cross-faded piece holds its
     * first unparked place over the half fade before it comes on and its last after it parks ({@link #CROSS_FADED});
     * {@code along} for every other piece.
     */
    static double fadeTime(BackdropData.PlacedPiece placed, double along) {
        if (!CROSS_FADED.contains(placed.piece())) {
            return along;
        }
        double half = CROSS_FADE_SECONDS / 2;
        double in = unparkedAt(placed);
        if (!Double.isNaN(in) && along < in && along >= in - half) {
            return in;
        }
        double out = parkedFrom(placed);
        if (!Double.isNaN(out) && along > out && along <= out + half) {
            return out;
        }
        return along;
    }

    /**
     * M11 part B: a cross-faded piece's opacity when its path has run {@code along}: rising over the {@value
     * #CROSS_FADE_SECONDS} s centred on its coming on, falling over those centred on its parking (smoothstep), so two
     * swapped pieces cross; 1 for every other piece.
     */
    static float fadeAlpha(BackdropData.PlacedPiece placed, double along) {
        if (!CROSS_FADED.contains(placed.piece())) {
            return 1;
        }
        double half = CROSS_FADE_SECONDS / 2;
        double in = unparkedAt(placed);
        double out = parkedFrom(placed);
        double rise = Double.isNaN(in) ? 1 : smooth((along - (in - half)) / CROSS_FADE_SECONDS);
        double fall = Double.isNaN(out) ? 1 : 1 - smooth((along - (out - half)) / CROSS_FADE_SECONDS);
        return (float) (rise * fall);
    }

    private static double smooth(double t) {
        double c = Math.clamp(t, 0, 1);
        return c * c * (3 - 2 * c);
    }

    /**
     * The screen y of a piece's bottom edge, its layer scrolled by {@code scroll} px (not rounded) and the piece {@code
     * shift} px up, at {@code along} on its path. A piece without a path moves with its layer's tiles (the scroll rounded
     * as theirs, {@link BackdropTiles#scroll}). One on a path is rounded once, its offset and the scroll together: two
     * roundings stepping at different moments made a path that rides the scroll flicker a pixel up and down ten times a
     * second (round 80: Level 42's Callisto, held on the screen against the deep layer's 9.6 px/s).
     */
    static float pieceY(Piece piece, long shift, double scroll, double along) {
        long base = piece.bottom() + shift;
        if (Double.isNaN(piece.pathStart())) {
            return base - Math.round(scroll);
        }
        return base + Math.round(piece.placed().offsetY(along) - scroll);
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
