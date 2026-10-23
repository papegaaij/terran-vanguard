package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import java.util.Set;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M8 part E: a drop released by a moored buoy (design/campaign Level 25: a Conamara survey buoy at the narrows' end
 * releases the armour patch; tools/art/l25_props.py): its {@code drops:} row's {@code sprite} names the buoy. Presentation
 * only, nobody can hit it. Moored on the play plane, it scrolls with the floor so that it stands at the drop's point at
 * the drop's time: shut with the patch inside before it ({@code _0}), its side hatch swinging open for {@value
 * #OPENING_SECONDS} s as the simulation places the pickup ({@code _1}), then open and empty ({@code _2}). Its strobe
 * ({@code -glow}, {@value #STROBE_FPS} fps) blinks after the grade, ungraded. All on the level clock.
 */
public final class BuoyLooks {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** The drops released by a moored buoy: the buoys' sprite roots. */
    static final Set<String> MOORED = Set.of("survey-buoy");
    /** The hatch's opening frame shows this long from the drop, s. */
    static final double OPENING_SECONDS = 0.2;
    /** The strobe's rate. */
    static final double STROBE_FPS = 4;

    static final int SHUT = 0;
    static final int OPENING = 1;
    static final int OPEN = 2;

    private final List<LevelData.Drop> drops;
    /** Per drop the ground scroll at its time (where the buoy stands on the floor). */
    private final double[] scrollAt;

    private final Array<AtlasRegion> body;
    private final Array<AtlasRegion> strobe;

    private BuoyLooks(
            List<LevelData.Drop> drops, double[] scrollAt, Array<AtlasRegion> body, Array<AtlasRegion> strobe) {
        this.drops = drops;
        this.scrollAt = scrollAt;
        this.body = body;
        this.strobe = strobe;
    }

    /** Whether a drop is released by a moored buoy (its {@code sprite} one of {@link #MOORED}). */
    public static boolean moored(LevelData.Drop drop) {
        return drop.sprite().filter(MOORED::contains).isPresent();
    }

    /** The level's buoys' looks, or null without such drops (or without the buoy's sprites). */
    static BuoyLooks of(Sprites sprites, LevelData level) {
        List<LevelData.Drop> drops = level.released().stream()
                .filter(BuoyLooks::moored)
                .filter(drop -> drop.sprite().filter(sprites::has).isPresent())
                .toList();
        if (drops.isEmpty()) {
            return null;
        }
        String slug = drops.getFirst().sprite().orElseThrow();
        Array<AtlasRegion> body = sprites.frames(slug);
        if (body.size <= OPEN) {
            return null;
        }
        double[] scrollAt = new double[drops.size()];
        for (int d = 0; d < scrollAt.length; d++) {
            scrollAt[d] = level.scrollAt(drops.get(d).t());
        }
        return new BuoyLooks(
                drops, scrollAt, body, sprites.has(slug + "-glow") ? sprites.frames(slug + "-glow") : new Array<>(0));
    }

    /** Its frame {@code since} s after its drop (negative before it): shut, opening, open. */
    static int frame(double since) {
        return since < 0 ? SHUT : since < OPENING_SECONDS ? OPENING : OPEN;
    }

    /** Where buoy {@code d} stands at the ground scroll {@code scroll}: its drop's height then, moved with the floor. */
    double y(int d, double scroll) {
        return y(drops.get(d).y(), scrollAt[d], scroll);
    }

    /** Where a buoy dropping at height {@code dropY} at the ground scroll {@code atDrop} stands at {@code scroll}. */
    static double y(double dropY, double atDrop, double scroll) {
        return dropY + atDrop - scroll;
    }

    /** The buoys on the screen at level time {@code seconds} and ground scroll {@code scroll} (before the grade). */
    void drawBodies(SpriteBatch batch, double seconds, double scroll) {
        for (int d = 0; d < drops.size(); d++) {
            AtlasRegion frame = body.get(frame(seconds - drops.get(d).t()));
            draw(batch, frame, drops.get(d).x(), y(d, scroll));
        }
    }

    /** Their strobes, additive (after the grade). */
    void drawLights(SpriteBatch batch, double seconds, double real, double scroll) {
        if (strobe.isEmpty()) {
            return;
        }
        AtlasRegion frame = strobe.get((int) Math.floorMod((long) Math.floor(real * STROBE_FPS), (long) strobe.size));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int d = 0; d < drops.size(); d++) {
            draw(batch, frame, drops.get(d).x(), y(d, scroll));
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void draw(SpriteBatch batch, AtlasRegion frame, double x, double y) {
        float half = frame.getRegionHeight() / 2f;
        if (y + half < 0 || y - half > PlayField.HEIGHT) {
            return;
        }
        batch.draw(frame, Math.round(X0 + x - frame.getRegionWidth() / 2f), Math.round(y - half));
    }

    /** The drops it releases (for the tests). */
    List<LevelData.Drop> drops() {
        return drops;
    }
}
