package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.sim.PlayField;

/**
 * The boss bar at the top of the play field (design/ui/hud): the sum of the boss's remaining part HP
 * as a red fill, 400 px for an act boss and 240 px for a mid-boss, the boss's name above it. With
 * the production plate {@code hud/boss-bar-plate} (a nine-patch, {@code assets/sprites/hud/boss-bar-plate.9.png})
 * the plate is stretched round the bar and the fill drawn in its content box (the nine-patch's
 * padding, or the middle between its splits without one), at the plate's own height; without it
 * the bar is drawn plainly: a dark frame, a dark red trough and the fill, 6 px tall.
 *
 * <p>M10 part F (design/ui/hud, Act 6 additions: the Honour Guard's bar): a bar in {@link Segments} ({@link
 * #drawSegments}), one per body in join order, each as wide as its share of the bodies' HP, {@value #SEGMENT_GAP} px
 * apart, filled by its own HP left (so the bar's length is their sum and never jumps); each with its call-sign tag above
 * it (the name one line higher): an unjoined body's segment dim and hatched, its tag grey; a white flash as it joins
 * and on a hit (its own segment only); a dead one's segment dark, its tag struck through in red.
 *
 * <p>M10 part H, step H4a (design/ui/hud, the escape bar; Level 42's last phase): the escape bar ({@link #drawEscape}),
 * the act boss's bar turned into the lifeboat's: its plate's name the lifeboat's ({@code VORNE}), an amber time bar
 * draining from full to empty over the climb with the seconds left at the bar's right end ({@code 12s}), a gold fill over
 * its lower half growing from the left with the damage's share of the payout's cap, flashing once at the cap and reading
 * {@code MAX}; at the top edge the bar flashes and reads {@code ESCAPED} in amber, never a kill.
 */
final class BossBar {
    /** The plate's region in the sprite pages. */
    static final String PLATE = "hud/boss-bar-plate";
    /** The bar's width for an act boss and a mid-boss, the plain bar's height, the bar's top below the top edge. */
    static final float WIDTH = 400;

    static final float MID_WIDTH = 240;
    static final float PLAIN_HEIGHT = 6;
    static final float TOP = 22;

    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    private static final Color FRAME = Color.valueOf("0A0C1A");
    private static final Color EMPTY = Color.valueOf("3A1414");
    private static final Color FILL = Color.valueOf("E03C28");
    /** M10 part F: the gap between two segments, px; a waiting segment's hatch and colours; the tags' colours. */
    static final float SEGMENT_GAP = 2;

    static final int HATCH_STEP = 4;
    private static final Color WAITING = Color.valueOf("2A2030");
    private static final Color HATCH = Color.valueOf("5A4A60");
    private static final Color DEAD = Color.valueOf("16121A");
    private static final Color TAG = Color.valueOf("F0C860");
    private static final Color TAG_WAITING = Color.valueOf("8A8A90");
    /** A waiting or dead ace's call-sign sprite, greyed. */
    private static final Color TAG_DIM = new Color(0.5f, 0.5f, 0.52f, 1);

    private static final Color STRIKE = Color.valueOf("E03C28");
    /** M10 part H: the escape bar's time (amber), its payout fill (gold), its flashes' length and rate. */
    static final Color TIME = Color.valueOf("D8701C");

    static final Color PAYOUT = Color.valueOf("FFE27A");
    /** M10 part H: the escape bar's words in amber (the seconds, {@code ESCAPED}). */
    static final Color TIME_TEXT = Color.valueOf("FFB040");

    static final double CAP_FLASH_SECONDS = 0.25;
    static final double ESCAPED_FLASH_HZ = 4;
    /** M10 part H: the escape bar's words. */
    static final String MAX = "MAX";

    static final String ESCAPED = "ESCAPED";
    /** M10 part H: the seconds left as drawn, {@code 0s} to {@code 99s} (no text is allocated per frame). */
    private static final String[] SECONDS = new String[100];

    static {
        for (int i = 0; i < SECONDS.length; i++) {
            SECONDS[i] = i + "s";
        }
    }

    /** The space between the tags' line and the name's, px. */
    static final float TAG_GAP = 3;

    /** Where the plate and the fill go, in play-field pixels (y up); the plate's box is empty without one. */
    record Layout(
            float plateX,
            float plateY,
            float plateWidth,
            float plateHeight,
            float fillX,
            float fillY,
            float fillWidth,
            float fillHeight) {}

    private final TextureRegion pixel;
    /** The production plate, or null until it exists. */
    private final NinePatch plate;

    BossBar(Sprites sprites) {
        this.pixel = sprites.pixel;
        this.plate = plate(sprites);
    }

    /** The plate if the sprite pages hold it as a nine-patch (a plain PNG is not used). */
    private static NinePatch plate(Sprites sprites) {
        if (!sprites.has(PLATE) || sprites.region(PLATE).findValue("split") == null) {
            return null;
        }
        return sprites.patch(PLATE);
    }

    /**
     * The layout of a bar {@code width} wide with its fill's top {@value #TOP} px below the play
     * field's top, round a plate with these paddings and total height (all 0 for the plain bar).
     */
    static Layout layout(float width, float padLeft, float padRight, float padTop, float padBottom, float plateHeight) {
        return layout(width, padLeft, padRight, padTop, padBottom, plateHeight, 0);
    }

    /** M10 part F: as {@link #layout(float, float, float, float, float, float)}, {@code drop} px lower (the tags' line). */
    static Layout layout(
            float width, float padLeft, float padRight, float padTop, float padBottom, float plateHeight, float drop) {
        float left = (PlayField.WIDTH - width) / 2;
        float top = PlayField.HEIGHT - TOP - drop;
        if (plateHeight <= 0) {
            return new Layout(left, top, 0, 0, left, top - PLAIN_HEIGHT, width, PLAIN_HEIGHT);
        }
        float fillHeight = plateHeight - padTop - padBottom;
        float bottom = top - fillHeight;
        return new Layout(
                left - padLeft,
                bottom - padBottom,
                width + padLeft + padRight,
                plateHeight,
                left,
                bottom,
                width,
                fillHeight);
    }

    /** The bar's layout at {@code width}, round the plate when there is one. */
    private Layout layoutOf(float width, float drop) {
        return plate == null
                ? layout(width, 0, 0, 0, 0, 0, drop)
                : layout(
                        width,
                        plate.getPadLeft(),
                        plate.getPadRight(),
                        plate.getPadTop(),
                        plate.getPadBottom(),
                        plate.getTotalHeight(),
                        drop);
    }

    /** Draws the bar of a boss with {@code share} (0..1) of its HP left and its name. */
    void draw(SpriteBatch batch, BitmapFont font, String name, boolean midBoss, double share) {
        float width = midBoss ? MID_WIDTH : WIDTH;
        Layout at = layoutOf(width, 0);
        float nameTop = drawTrough(batch, font, at);
        batch.setColor(FILL);
        batch.draw(
                pixel,
                X0 + at.fillX(),
                at.fillY(),
                (float) Math.floor(at.fillWidth() * Math.clamp(share, 0, 1)),
                at.fillHeight());
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
        font.draw(batch, name, X0 + at.fillX(), Math.round(nameTop));
    }

    /** M10 part H: the seconds-left text for {@code seconds} left: whole seconds rounded up, {@code 0s} at the end. */
    static String secondsText(double seconds) {
        int whole = (int) Math.ceil(Math.max(0, seconds) - 1e-9);
        return SECONDS[Math.min(SECONDS.length - 1, whole)];
    }

    /** M10 part H: the height of the payout's gold fill in a fill {@code height} px tall: its lower half, rounded up. */
    static float payoutHeight(float height) {
        return (float) Math.ceil(height / 2);
    }

    /** M10 part H: whether the {@code ESCAPED} bar is lit {@code seconds} after the escape (flashing at {@value #ESCAPED_FLASH_HZ} Hz). */
    static boolean escapedLit(double seconds) {
        return Math.floor(seconds * ESCAPED_FLASH_HZ * 2) % 2 == 0;
    }

    /**
     * M10 part H: the escape bar of {@code name}: the climb's {@code timeShare} (1 → 0) in amber with {@code secondsLeft}
     * at its right end, the payout's {@code share} of the cap in gold over the lower half (flashing white for {@value
     * #CAP_FLASH_SECONDS} s {@code capped} s after the cap, -1 before, and reading {@code MAX} from then), and {@code
     * escaped} s after the escape (-1 before) the whole bar flashing amber under {@code ESCAPED}.
     */
    void drawEscape(
            SpriteBatch batch,
            BitmapFont font,
            String name,
            double timeShare,
            double secondsLeft,
            double share,
            double capped,
            double escaped) {
        Layout at = layoutOf(WIDTH, 0);
        float nameTop = drawTrough(batch, font, at);
        float x = X0 + at.fillX();
        float w = at.fillWidth();
        float h = at.fillHeight();
        if (escaped >= 0) {
            if (escapedLit(escaped)) {
                batch.setColor(TIME);
                batch.draw(pixel, x, at.fillY(), w, h);
            }
        } else {
            batch.setColor(TIME);
            batch.draw(pixel, x, at.fillY(), (float) Math.floor(w * Math.clamp(timeShare, 0, 1)), h);
        }
        float gold = (float) Math.floor(w * Math.clamp(share, 0, 1));
        if (gold > 0 && escaped < 0) {
            // The gold over the lower half, a dark rule above it so it reads apart from the amber.
            float payout = payoutHeight(h);
            batch.setColor(FRAME);
            batch.draw(pixel, x, at.fillY() + payout, gold, 1);
            batch.setColor(PAYOUT);
            batch.draw(pixel, x, at.fillY(), gold, payout);
        }
        if (capped >= 0 && capped < CAP_FLASH_SECONDS) {
            batch.setColor(1, 1, 1, (float) (1 - capped / CAP_FLASH_SECONDS));
            batch.draw(pixel, x, at.fillY(), w, h);
        }
        batch.setColor(Color.WHITE);
        float top = Math.round(nameTop);
        font.setColor(Color.WHITE);
        font.draw(batch, name, x, top);
        if (escaped >= 0) {
            font.setColor(TIME_TEXT);
            font.draw(batch, ESCAPED, x + w - vanguard.game.ui.Fonts.width(font, ESCAPED), top);
        } else {
            String left = secondsText(secondsLeft);
            font.setColor(TIME_TEXT);
            font.draw(batch, left, x + w - vanguard.game.ui.Fonts.width(font, left), top);
            if (capped >= 0) {
                font.setColor(PAYOUT);
                font.draw(batch, MAX, Math.round(x + (w - vanguard.game.ui.Fonts.width(font, MAX)) / 2), top);
            }
        }
        font.setColor(Color.WHITE);
    }

    /** The plate (or the plain frame and trough); returns the name's top above it. */
    private float drawTrough(SpriteBatch batch, BitmapFont font, Layout at) {
        if (plate == null) {
            batch.setColor(FRAME);
            batch.draw(pixel, X0 + at.fillX() - 2, at.fillY() - 2, at.fillWidth() + 4, at.fillHeight() + 4);
            batch.setColor(EMPTY);
            batch.draw(pixel, X0 + at.fillX(), at.fillY(), at.fillWidth(), at.fillHeight());
            return at.fillY() + at.fillHeight() + 12;
        }
        batch.setColor(Color.WHITE);
        plate.draw(batch, X0 + at.plateX(), at.plateY(), at.plateWidth(), at.plateHeight());
        // The name's capitals sit 3 px above the plate.
        return at.plateY() + at.plateHeight() + 3 + font.getCapHeight();
    }

    /**
     * M10 part F: where segment {@code s} of {@code count} lies in a fill {@code fillWidth} wide from {@code fillX}, the
     * segments as wide as their shares of {@code max} ({@code max[s]}), {@value #SEGMENT_GAP} px apart; {@code out} gets
     * its left x and width.
     */
    static void segment(float fillX, float fillWidth, double[] max, int count, int s, float[] out) {
        double total = 0;
        for (int i = 0; i < count; i++) {
            total += max[i];
        }
        float room = fillWidth - SEGMENT_GAP * (count - 1);
        double before = 0;
        for (int i = 0; i < s; i++) {
            before += max[i];
        }
        float left = (float) Math.floor(fillX + room * before / total) + SEGMENT_GAP * s;
        float right = (float) Math.floor(fillX + room * (before + max[s]) / total) + SEGMENT_GAP * s;
        out[0] = left;
        out[1] = right - left;
    }

    private final float[] span = new float[2];

    /** M10 part F: the mid-boss bar in segments (the Honour Guard's), its name above the call-sign tags. */
    void drawSegments(SpriteBatch batch, BitmapFont font, String name, Segments segments) {
        // The bar a tag line lower than a plain mid-boss bar: its name where that bar's is, the tags between.
        float drop = tagLine(font);
        Layout at = layoutOf(MID_WIDTH, drop);
        float tagTop = drawTrough(batch, font, at);
        // The trough's own colour shows in the gaps: the segments are drawn over it.
        for (int s = 0; s < segments.count; s++) {
            segment(at.fillX(), at.fillWidth(), segments.max, segments.count, s, span);
            float x = X0 + span[0];
            float w = span[1];
            int state = segments.state[s];
            batch.setColor(FRAME);
            if (s > 0) {
                batch.draw(pixel, x - SEGMENT_GAP, at.fillY(), SEGMENT_GAP, at.fillHeight());
            }
            if (state == Segments.WAITING) {
                batch.setColor(WAITING);
                batch.draw(pixel, x, at.fillY(), w, at.fillHeight());
                batch.setColor(HATCH);
                // Diagonal hatching: one pixel a row, stepping right as it rises.
                for (float hx = 0; hx < w + at.fillHeight(); hx += HATCH_STEP) {
                    for (int row = 0; row < (int) at.fillHeight(); row++) {
                        float px = hx - row;
                        if (px >= 0 && px < w) {
                            batch.draw(pixel, x + px, at.fillY() + row, 1, 1);
                        }
                    }
                }
            } else if (state == Segments.DEAD) {
                batch.setColor(DEAD);
                batch.draw(pixel, x, at.fillY(), w, at.fillHeight());
            } else {
                batch.setColor(EMPTY);
                batch.draw(pixel, x, at.fillY(), w, at.fillHeight());
                batch.setColor(FILL);
                double share = segments.max[s] <= 0 ? 0 : segments.hp[s] / segments.max[s];
                batch.draw(pixel, x, at.fillY(), (float) Math.floor(w * Math.clamp(share, 0, 1)), at.fillHeight());
            }
            if (segments.flash[s] > 0) {
                batch.setColor(1, 1, 1, Math.min(1, segments.flash[s]));
                batch.draw(pixel, x, at.fillY(), w, at.fillHeight());
            }
            // The call sign centred over its segment: its sprite (the label font, gold), else the bar's font.
            TextureRegion sign = segments.sign[s];
            float tagWidth;
            float tx;
            float middle;
            if (sign != null) {
                tagWidth = sign.getRegionWidth();
                tx = Math.round(x + (w - tagWidth) / 2);
                float ty =
                        Math.round(tagTop - font.getCapHeight() + (font.getCapHeight() - sign.getRegionHeight()) / 2);
                Color tint = state == Segments.LIVE ? Color.WHITE : TAG_DIM;
                batch.setColor(tint);
                batch.draw(sign, tx, ty);
                middle = ty + sign.getRegionHeight() / 2f;
            } else {
                tagWidth = Math.min(w, segments.tagWidth(s, font));
                tx = Math.round(x + (w - tagWidth) / 2);
                font.setColor(state == Segments.LIVE ? TAG : TAG_WAITING);
                font.draw(batch, segments.tag[s], tx, Math.round(tagTop));
                middle = tagTop - font.getCapHeight() / 2;
            }
            if (state == Segments.DEAD) {
                batch.setColor(STRIKE);
                batch.draw(pixel, tx - 1, Math.round(middle), tagWidth + 2, 1);
            }
        }
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
        font.draw(batch, name, X0 + at.fillX(), Math.round(tagTop + drop));
    }

    /** M10 part F: the tags' line, px: the font's line height and {@value #TAG_GAP} px. */
    static float tagLine(BitmapFont font) {
        return Math.round(font.getLineHeight() + TAG_GAP);
    }

    /**
     * M10 part F: a segmented bar's state, set each frame by its boss's looks (nothing allocated): per segment its HP
     * left and most, its state ({@link #WAITING}, {@link #LIVE}, {@link #DEAD}), its flash (0-1) and its tag.
     */
    static final class Segments {
        static final int WAITING = 0;
        static final int LIVE = 1;
        static final int DEAD = 2;

        final int count;
        final double[] hp;
        final double[] max;
        final int[] state;
        final float[] flash;
        final String[] tag;
        /** Per segment its call sign's sprite, or null for the bar's font. */
        final TextureRegion[] sign;

        private final float[] tagWidth;
        private BitmapFont measured;

        Segments(int count) {
            this.count = count;
            hp = new double[count];
            max = new double[count];
            state = new int[count];
            flash = new float[count];
            tag = new String[count];
            sign = new TextureRegion[count];
            tagWidth = new float[count];
            java.util.Arrays.fill(tag, "");
        }

        void tag(int s, String callSign) {
            tag[s] = callSign;
            measured = null;
        }

        void sign(int s, TextureRegion region) {
            sign[s] = region;
        }

        void hp(int s, double left, double most) {
            hp[s] = left;
            max[s] = most;
        }

        void state(int s, int value) {
            state[s] = value;
        }

        void flash(int s, float value) {
            flash[s] = value;
        }

        /** The tag's width in {@code font}, measured once. */
        float tagWidth(int s, BitmapFont font) {
            if (measured != font) {
                measured = font;
                var layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
                for (int i = 0; i < count; i++) {
                    layout.setText(font, tag[i]);
                    tagWidth[i] = layout.width;
                }
            }
            return tagWidth[s];
        }
    }
}
