package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;
import vanguard.sim.Ally;
import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * Step MARK70 (design/campaign Level 29, Friendly marker; round 70 B.1 d, the rendering friendly-marker-r70-d): HUD-green
 * IFF corner brackets round each neutral craft (Level 29's wavering Stormhawks; any unit flagged {@code neutral}) while
 * it is on the screen, with a small {@code FRIENDLY} tag under them, so the craft read as friendly once the radio and
 * the banner are gone. They follow the craft as shown and go when it leaves the screen or is destroyed. Pixel-crisp:
 * the craft's spot rounded to whole pixels, 2 px arms on a 1 px dark outline. Drawn in code (a HUD mark, no art);
 * geometry in play-field pixels, y up. M10 part C (design/campaign Level 37, The shuttles): a level's air allies
 * outside the objectives (Level 37's Halcyon shuttles: not {@code neutral}, enemy fire hits them) are marked too, from
 * their pads to their climb out ({@link #markedAlly}). M10 part D (design/campaign Level 38): a debris chunk the level
 * marks ({@code marked}: the CDF supply canister on its parachute) is bracketed too ({@link #markedChunk}). M10 part F
 * (design/campaign Level 40): a supply drone carrying a {@code marked} drop is bracketed through its flight ({@link
 * #drones}, at {@link DroneLooks}' place for it).
 */
public final class FriendlyMarks {
    /** The tag under the brackets. */
    static final String TAG = "FRIENDLY";
    /** The brackets keep this far outside the craft's hit box (a 28 px box: corners 26 px from its middle). */
    static final int MARGIN = 12;
    /** Each corner's arms, px. */
    static final int ARM = 9;
    /** The arms' thickness, px. */
    static final int THICK = 2;
    /** The sprite is larger than its hit box by about this much a side: the craft shows while this near the edges. */
    static final int SPRITE = 6;
    /** The tag's box: its top this far under the brackets, its height. */
    static final int TAG_GAP = 4;

    static final int TAG_HEIGHT = 12;
    /** The most craft marked at once. */
    static final int MAX = 16;

    private static final Color GREEN = Glass.GREEN;
    private static final Color OUTLINE = new Color(0, 20 / 255f, 8 / 255f, 1);
    private static final Color TAG_EDGE = new Color(30 / 255f, 110 / 255f, 50 / 255f, 1);

    private final TextureRegion pixel;
    private final BitmapFont font;
    private final int[] xs = new int[MAX];
    private final int[] ys = new int[MAX];
    private final int[] halves = new int[MAX];
    private int count;
    /** M10 part F: the marked drops whose drones are bracketed (their drones fly in from below the bottom edge). */
    private java.util.List<vanguard.content.LevelData.Drop> drones = java.util.List.of();

    private final float[] at = new float[2];
    /** M10 part F: a supply drone's half size for the brackets (its 40 px sprite, a 28 px body). */
    static final int DRONE_HALF = 14;

    /**
     * @param pixel a white pixel
     * @param font the tag's font (the 8x12 label font)
     */
    public FriendlyMarks(TextureRegion pixel, BitmapFont font) {
        this.pixel = pixel;
        this.font = font;
    }

    /**
     * M10 part F: the level's drops ({@code drops}): those {@code marked} and carried by a drone ({@link DroneLooks}, no
     * defended ally's helipad) get the brackets while their drone flies on the screen.
     */
    public FriendlyMarks drones(java.util.List<vanguard.content.LevelData.Drop> drops) {
        drones = drops.stream()
                .filter(vanguard.content.LevelData.Drop::isMarked)
                .filter(drop -> !IceCrawlerLooks.carried(drop) && !BuoyLooks.moored(drop))
                // M10 part G: a beacon on the ice (Level 41's) is not marked.
                .filter(drop -> !CdfBeaconLooks.dropped(drop))
                .toList();
        return this;
    }

    /** Whether {@code enemy}, shown at ({@code x}, {@code y}), gets the brackets: a neutral craft on the screen. */
    static boolean marked(Enemy enemy, double x, double y) {
        if (!enemy.spec().neutral()) {
            return false;
        }
        double halfWidth = enemy.hitbox().width() / 2 + SPRITE;
        double halfHeight = enemy.hitbox().height() / 2 + SPRITE;
        return x + halfWidth > 0
                && x - halfWidth < PlayField.WIDTH
                && y + halfHeight > 0
                && y - halfHeight < PlayField.HEIGHT;
    }

    /**
     * M10 part C: whether air ally {@code ally}, shown at ({@code x}, {@code y}) with a hit box {@code width} x {@code
     * height}, gets the brackets: on its pad, lifting, on its station or climbing out, on the screen (not lost, not yet
     * there, not home).
     */
    static boolean markedAlly(Ally ally, double x, double y, double width, double height) {
        switch (ally.state()) {
            case PAD, LIFTING, FLYING, CLIMBING -> {}
            default -> {
                return false;
            }
        }
        if (!ally.alive()) {
            return false;
        }
        double halfWidth = width / 2 + SPRITE;
        double halfHeight = height / 2 + SPRITE;
        return x + halfWidth > 0
                && x - halfWidth < PlayField.WIDTH
                && y + halfHeight > 0
                && y - halfHeight < PlayField.HEIGHT;
    }

    /**
     * M10 part D: whether debris chunk {@code chunk}, shown at ({@code x}, {@code y}), gets the brackets: a chunk the
     * level marks friendly ({@code marked}: Level 38's CDF canister), drifting, on the screen.
     */
    static boolean markedChunk(Debris chunk, double x, double y) {
        if (!chunk.spec().marked() || chunk.thrown() || chunk.pulled()) {
            return false;
        }
        double halfWidth = chunk.spec().size().width() / 2 + SPRITE;
        double halfHeight = chunk.spec().size().height() / 2 + SPRITE;
        return x + halfWidth > 0
                && x - halfWidth < PlayField.WIDTH
                && y + halfHeight > 0
                && y - halfHeight < PlayField.HEIGHT;
    }

    /** Finds the marked craft at the tick being shown ({@code alpha} of a step after the last). */
    void plan(Sortie sortie, float alpha) {
        count = 0;
        if (sortie.script().objectives().airAllies().isPresent()) {
            for (int k = 0; k < sortie.allyCount() && count < MAX; k++) {
                Ally ally = sortie.ally(k);
                double x = ally.renderX(alpha);
                double y = ally.renderY(alpha);
                var box = sortie.allySpec(k).hitbox();
                if (markedAlly(ally, x, y, box.width(), box.height())) {
                    xs[count] = (int) Math.round(x);
                    ys[count] = (int) Math.round(y);
                    halves[count] = (int) Math.round(Math.max(box.width(), box.height()) / 2) + MARGIN;
                    count++;
                }
            }
        }
        for (int d = 0; d < sortie.debrisCount() && count < MAX; d++) {
            // M10 part D (design/campaign Level 38, the CDF supply canister): a marked debris chunk on the screen.
            Debris chunk = sortie.debris(d);
            double x = chunk.renderX(alpha);
            double y = chunk.renderY(alpha);
            var size = chunk.spec().size();
            if (markedChunk(chunk, x, y)) {
                xs[count] = (int) Math.round(x);
                ys[count] = (int) Math.round(y);
                halves[count] = (int) Math.round(Math.max(size.width(), size.height()) / 2) + MARGIN;
                count++;
            }
        }
        if (!drones.isEmpty()) {
            // M10 part F (design/campaign Level 40): the marked supply drones in flight, where DroneLooks draws them.
            double seconds = sortie.levelSeconds();
            for (int i = 0; i < drones.size() && count < MAX; i++) {
                var drop = drones.get(i);
                if (DroneLooks.position(
                                seconds - drop.t(), i, (float) drop.x(), -DroneLooks.BELOW_EDGE, drop.x(), drop.y(), at)
                        && at[1] + DRONE_HALF > 0
                        && at[1] - DRONE_HALF < PlayField.HEIGHT) {
                    xs[count] = Math.round(at[0]);
                    ys[count] = Math.round(at[1]);
                    halves[count] = DRONE_HALF + MARGIN;
                    count++;
                }
            }
        }
        for (int i = 0; i < sortie.enemyCount() && count < MAX; i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (marked(enemy, x, y)) {
                xs[count] = (int) Math.round(x);
                ys[count] = (int) Math.round(y);
                halves[count] = (int) Math.round(
                                Math.max(enemy.hitbox().width(), enemy.hitbox().height()) / 2)
                        + MARGIN;
                count++;
            }
        }
    }

    /** How many craft are marked (after {@link #plan}). */
    int count() {
        return count;
    }

    /** The middle of mark {@code i}, play-field pixels (after {@link #plan}). */
    int x(int i) {
        return xs[i];
    }

    int y(int i) {
        return ys[i];
    }

    /** Draws the brackets and tags of the craft on the screen at the tick being shown. */
    public void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        plan(sortie, alpha);
        if (count == 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            int cx = PixelScreen.PLAY_FIELD_X + xs[i];
            int half = halves[i];
            int left = cx - half;
            int right = cx + half;
            int bottom = ys[i] - half;
            int top = ys[i] + half;
            batch.setColor(OUTLINE);
            corners(batch, left, right, bottom, top, 1);
            batch.setColor(GREEN);
            corners(batch, left, right, bottom, top, 0);
            tag(batch, cx, bottom - TAG_GAP);
        }
        batch.setColor(Color.WHITE);
    }

    /** The four corners' arms inside the box, grown by {@code grow} px all round (the outline). */
    private void corners(SpriteBatch batch, int left, int right, int bottom, int top, int grow) {
        int g2 = 2 * grow;
        // Top left, top right, bottom left, bottom right: a horizontal arm and a vertical one each.
        batch.draw(pixel, left - grow, top - THICK - grow, ARM + g2, THICK + g2);
        batch.draw(pixel, left - grow, top - ARM - grow, THICK + g2, ARM + g2);
        batch.draw(pixel, right - ARM - grow, top - THICK - grow, ARM + g2, THICK + g2);
        batch.draw(pixel, right - THICK - grow, top - ARM - grow, THICK + g2, ARM + g2);
        batch.draw(pixel, left - grow, bottom - grow, ARM + g2, THICK + g2);
        batch.draw(pixel, left - grow, bottom - grow, THICK + g2, ARM + g2);
        batch.draw(pixel, right - ARM - grow, bottom - grow, ARM + g2, THICK + g2);
        batch.draw(pixel, right - THICK - grow, bottom - grow, THICK + g2, ARM + g2);
    }

    /** The {@code FRIENDLY} tag in its dark box, centred on {@code cx}, its box's top at {@code boxTop}. */
    private void tag(SpriteBatch batch, int cx, int boxTop) {
        int width = Math.round(Fonts.width(font, TAG));
        int textX = cx - width / 2;
        int boxLeft = textX - 3;
        int boxWidth = width + 5;
        int boxBottom = boxTop - TAG_HEIGHT;
        batch.setColor(TAG_EDGE);
        batch.draw(pixel, boxLeft, boxBottom, boxWidth, TAG_HEIGHT);
        batch.setColor(OUTLINE);
        batch.draw(pixel, boxLeft + 1, boxBottom + 1, boxWidth - 2, TAG_HEIGHT - 2);
        batch.setColor(Color.WHITE);
        font.setColor(GREEN);
        font.draw(batch, TAG, textX, Math.round(boxBottom + TAG_HEIGHT / 2f + font.getCapHeight() / 2));
        font.setColor(Color.WHITE);
    }
}
