package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.LevelData;

/**
 * M11 part C, step C4 (design/campaign Level 45, the backdrop: "veins pulse slowly and are never bullet-sized dots";
 * tools/art/act7_reef.py: every ground {@code -veins} image, deliberately not in the data): the reef's veins breathing.
 * The ground tile sets hold their veins at about 45 %; their {@code <tile>-veins} images (and the ground pieces'
 * {@code <piece>-veins}, drawn over each piece by {@link BackdropPieces}) add the rest additively at one slow, smooth
 * share {@link #share}: a sine breath of {@value #PERIOD_SECONDS} s (0.25 Hz, under the 0.5 Hz the brief allows), from
 * none to full and back, by the real clock. A level without such images has no pulse ({@link #of}). Nothing is
 * allocated per frame.
 *
 * <p>M11 part H, step H4 (design/campaign Level 50; tools/art/act7_core.py, its backdrop's H4 hook 1): in a level whose
 * backdrop has a {@code pulse} block (the hive core) every layer's tile sets pulse, each {@code <set>-veins} image (and
 * {@code singing-ribs-glow}, the Choir Chambers' markings: a low-air set's {@code <set>-glow}) over its own layer's
 * tiles, and the share is the Choir Heart's pulse once it has been seen ({@link #heart}: smooth, in time with {@code
 * HEARTBEAT}, out from K), its own breath before. Those images are stored at half size (the atlas budget) and drawn at
 * 2x with linear filtering: a light, they lose nothing at the screen's scale.
 */
final class VeinPulse {
    /** The suffix of a ground image's pulsing veins. */
    static final String VEINS = "-veins";
    /** M11 part H: the suffix of a low-air set's pulsing light (Level 50's singing ribs' markings). */
    static final String GLOW = "-glow";
    /** One breath of the veins, s. */
    static final double PERIOD_SECONDS = 4;

    private final BackdropTiles tiles;
    /** Per layer and section the tile set's veins, null where it has none; per layer whether any section has. */
    private final BackdropTiles.Tiles[][] veins;

    private final boolean[] layered;
    /** M11 part H: the Choir Heart's pulse now (NaN: none seen yet, the veins' own breath). */
    private double heart = Double.NaN;

    private VeinPulse(BackdropTiles tiles, BackdropTiles.Tiles[][] veins, boolean[] layered) {
        this.tiles = tiles;
        this.veins = veins;
        this.layered = layered;
    }

    /**
     * The pulse of a level's tile sets' veins: its ground sets' (Level 45), every layer's in a level whose backdrop has
     * a {@code pulse} block (Level 50); null when none of them has a {@code -veins} image.
     */
    static VeinPulse of(Sprites sprites, LevelData level, String folder, BackdropTiles tiles) {
        BackdropData data = level.backdrop();
        boolean everyLayer = data.pulse().isPresent();
        BackdropLayer[] layers = BackdropLayer.values();
        BackdropTiles.Tiles[][] veins =
                new BackdropTiles.Tiles[layers.length][level.sections().size()];
        boolean[] layered = new boolean[layers.length];
        boolean any = false;
        for (int s = 0; s < level.sections().size(); s++) {
            for (String id : level.sections().get(s).tiles()) {
                BackdropData.TileSet tileSet = data.tileSets().get(id);
                BackdropLayer layer = tileSet.layer();
                if (!everyLayer && layer != BackdropLayer.GROUND) {
                    continue;
                }
                String name = sprites.hasBackdrop(folder + id + VEINS)
                        ? id + VEINS
                        : everyLayer && layer == BackdropLayer.LOW_AIR && sprites.hasBackdrop(folder + id + GLOW)
                                ? id + GLOW
                                : null;
                if (name != null) {
                    veins[layer.ordinal()][s] = BackdropTiles.tiles(sprites, folder, name, tileSet);
                    layered[layer.ordinal()] = true;
                    any = true;
                }
            }
        }
        return any ? new VeinPulse(tiles, veins, layered) : null;
    }

    /** The veins' share at real time {@code seconds}: 0 to 1 and back over {@value #PERIOD_SECONDS} s, smooth. */
    static float share(double seconds) {
        return (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * seconds / PERIOD_SECONDS));
    }

    /** M11 part H: the Choir Heart's pulse now, 0-1 (NaN: not seen, the veins breathe on their own). */
    void heart(double pulse) {
        heart = pulse;
    }

    /** The share now: the Heart's pulse once seen, else the breath at real time {@code real}. */
    float shareAt(double real) {
        return Double.isNaN(heart) ? share(real) : (float) Math.clamp(heart, 0, 1);
    }

    /** Whether {@code layer} has pulsing veins. */
    boolean has(BackdropLayer layer) {
        return layered[layer.ordinal()];
    }

    /** The ground tile sets' veins over them, additive at the pulse's share, the ground scrolled by {@code scroll}. */
    void draw(SpriteBatch batch, long scroll, BackdropClock clock) {
        draw(batch, BackdropLayer.GROUND, scroll, clock);
    }

    /** {@code layer}'s tile sets' veins over its tiles, additive at the pulse's share, the layer scrolled by {@code scroll}. */
    void draw(SpriteBatch batch, BackdropLayer layer, long scroll, BackdropClock clock) {
        if (!layered[layer.ordinal()]) {
            return;
        }
        float share = shareAt(clock.real());
        if (share <= 0) {
            return;
        }
        BackdropTiles.Tiles[] sets = veins[layer.ordinal()];
        long[] seams = tiles.seams(layer);
        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1, 1, 1, share);
        for (int s = 0; s < sets.length; s++) {
            if (sets[s] == null) {
                continue;
            }
            long end = s + 1 < sets.length ? seams[s + 1] : Long.MAX_VALUE;
            long from = Math.max(seams[s], scroll);
            long to = Math.min(end, scroll + vanguard.sim.PlayField.HEIGHT);
            if (from >= to) {
                continue;
            }
            int scale = BackdropTiles.scaleOf(sets[s]);
            if (scale == 1) {
                tiles.drawTiles(
                        batch, sets[s], from, to, scroll, clock.real(), tiles.phase(layer), tiles.lateral(layer));
                continue;
            }
            // A half-size light: drawn at 2x, linearly filtered (the page's own filter back after it).
            Texture texture = sets[s].image().getTexture();
            Texture.TextureFilter min = texture.getMinFilter();
            Texture.TextureFilter mag = texture.getMagFilter();
            batch.flush();
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            tiles.drawTilesScaled(
                    batch, sets[s], from, to, scroll, clock.real(), tiles.phase(layer), tiles.lateral(layer), scale);
            batch.flush();
            texture.setFilter(min, mag);
        }
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(src, dst);
    }
}
