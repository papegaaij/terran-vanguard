package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
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
 */
final class VeinPulse {
    /** The suffix of a ground image's pulsing veins. */
    static final String VEINS = "-veins";
    /** One breath of the veins, s. */
    static final double PERIOD_SECONDS = 4;

    private final BackdropTiles tiles;
    /** Per section the ground tile set's veins, null where it has none (or no ground tile set). */
    private final BackdropTiles.Tiles[] veins;

    private VeinPulse(BackdropTiles tiles, BackdropTiles.Tiles[] veins) {
        this.tiles = tiles;
        this.veins = veins;
    }

    /** The pulse of a level's ground tile sets' veins; null when none of them has a {@code -veins} image. */
    static VeinPulse of(Sprites sprites, LevelData level, String folder, BackdropTiles tiles) {
        BackdropData data = level.backdrop();
        BackdropTiles.Tiles[] veins = new BackdropTiles.Tiles[level.sections().size()];
        boolean any = false;
        for (int s = 0; s < veins.length; s++) {
            for (String id : level.sections().get(s).tiles()) {
                BackdropData.TileSet tileSet = data.tileSets().get(id);
                if (tileSet.layer() == BackdropLayer.GROUND && sprites.hasBackdrop(folder + id + VEINS)) {
                    veins[s] = BackdropTiles.tiles(sprites, folder, id + VEINS, tileSet);
                    any = true;
                }
            }
        }
        return any ? new VeinPulse(tiles, veins) : null;
    }

    /** The veins' share at real time {@code seconds}: 0 to 1 and back over {@value #PERIOD_SECONDS} s, smooth. */
    static float share(double seconds) {
        return (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * seconds / PERIOD_SECONDS));
    }

    /** The ground tile sets' veins over them, additive at the pulse's share, the ground scrolled by {@code scroll}. */
    void draw(SpriteBatch batch, long scroll, BackdropClock clock) {
        float share = share(clock.real());
        if (share <= 0) {
            return;
        }
        long[] seams = tiles.seams(BackdropLayer.GROUND);
        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1, 1, 1, share);
        for (int s = 0; s < veins.length; s++) {
            if (veins[s] == null) {
                continue;
            }
            long end = s + 1 < veins.length ? seams[s + 1] : Long.MAX_VALUE;
            tiles.drawTiles(
                    batch,
                    veins[s],
                    Math.max(seams[s], scroll),
                    Math.min(end, scroll + vanguard.sim.PlayField.HEIGHT),
                    scroll,
                    clock.real(),
                    tiles.phase(BackdropLayer.GROUND),
                    tiles.lateral(BackdropLayer.GROUND));
        }
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(src, dst);
    }
}
