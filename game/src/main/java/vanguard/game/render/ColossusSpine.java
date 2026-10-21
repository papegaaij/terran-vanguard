package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import vanguard.sim.BreachArena;

/**
 * Round 44 K.b (step GK; tools/art/dust_colossus.py's {@code dust-colossus-spine}, briefs/l21-art-names.md "Round 44:
 * spine"; pending the user's review): the Dust Colossus's back breaking the sand behind a risen edge mouth (front, left,
 * right, rear; the under pose's body goes straight down), {@value #SEGMENTS} plated segments of one back along the
 * ridge's track that ends at the mouth, the first three carrying the spine vents, the fourth a tapering tail. Step SPR:
 * the frames come in {@value RidgeLooks#HEADINGS} headings of 22.5°, so each segment (and its shadow) turns by what is
 * left over ({@link #tilt}, at most ±11.25°) about its centre.
 *
 * <p>Step GSR (the user on the round 46 review: "it just appears one segment at a time, like it's warped in. The spine
 * must rise out of the sand from the silhouette that precedes it. It rises from the head to the back. When it goes
 * under again ... from the back to the head"): an emergence <b>front</b> ({@link #front}) runs along the back from the
 * collar to the tail's tip over the mouth's rise; ahead of it the buried ridge's silhouette ({@link RidgeBody}, drawn
 * under the back along the same track) still shows, at it the plates break through, each segment shown only up to the
 * front with a soft edge ({@link FlashShader#revealUpTo}), behind it in a band of {@value #BAND} px the plates rise
 * through their three states blended continuously ({@link #emergence}: a third out with the sand heaped on them, two
 * thirds with the sand sliding off, up), sand spilling off the flanks at the front ({@code colossus-ridge-dust}). The
 * dive runs it back from the tail's tip to the collar over the first {@value #DIVE_SHARE} of the dive, the head last
 * with the mouth. The back lies on the ridge's own track ({@link #onTrack}: the curve the body is drawn along, straight
 * on past its start), shown only as far as the ridge reaches ({@link #reach}). Presentation only; nothing is allocated
 * per frame.
 */
final class ColossusSpine {
    /** The segments: three vent segments and the tail. */
    static final int SEGMENTS = 4;
    /** A segment's states: a third out, two thirds, up. */
    static final int STATES = 3;
    /** The state a segment is fully up in (its vent shows). */
    static final int UP = STATES - 1;
    /** The segments' distances from the mouth's centre along the body, px (the pivots' {@code spine}). */
    static final double[] DISTANCES = {120, 200, 280, 350};
    /**
     * Where each segment's plates begin and end along the back, px from the mouth's centre (tools/art/dust_colossus.py:
     * SPINE_FRONT 60 by the collar, the cuts at 166, 246 and 326 with 12 px of overlap, the tail's tip at 400 + 8).
     */
    static final double[] SPAN_START = {60, 154, 234, 314};

    static final double[] SPAN_END = {166, 246, 326, 408};
    /** The back's collar end and the tail's tip, px from the mouth's centre. */
    static final double HEAD = SPAN_START[0];

    static final double TIP = SPAN_END[SEGMENTS - 1];
    /** The band behind the front in which the plates rise through their states, px. */
    static final double BAND = 60;
    /** The width of the front's soft edge, px. */
    static final double SOFT = 24;
    /**
     * Sand still on the plates just behind the front: they take the ridge's sand colour toward the edge over this
     * many px (the bulge's mean colour, {@code colossus-ridge}), so the back comes up out of the sand, not wiped on.
     */
    static final double SAND = 40;

    private static final Color SAND_COLOUR = new Color(0.67f, 0.57f, 0.42f, 1);
    /** The front's reach: nothing shows at {@link #FRONT_FROM}, everything is up at {@link #FRONT_TO}, px. */
    static final double FRONT_FROM = HEAD - SOFT / 2;

    static final double FRONT_TO = TIP + BAND + SOFT / 2;
    /** The share of the dive the front takes to run back from the tail's tip to the collar. */
    static final double DIVE_SHARE = 0.7;
    /** The back is shown this much short of the ridge track's far end (the tail sprite's taper), px. */
    static final double TRACK_TAPER = 16;
    /** The span the back's direction at a segment is taken over, px either side of its centre. */
    static final double CHORD = 16;
    /** The sand puffs: one every {@value #PUFF_STEP} px along the back, each {@value #PUFF_SECONDS} s long. */
    static final double PUFF_STEP = 24;

    static final double PUFF_SECONDS = 0.5;
    /** The puffs' offset to the flanks (alternating), px, and their opacity. */
    static final double PUFF_SIDE = 20;

    static final float PUFF_ALPHA = 0.9f;
    static final int PUFFS = (int) ((TIP - HEAD) / PUFF_STEP) + 1;
    /**
     * The back's shadow on the sand (step GK: the segments read as a chain of pale bubbles at 1×; a shadow under them
     * lifts them off the sand as one back): each segment's silhouette this far to the lower right when fully up (less
     * while rising), px, at this opacity, under all of them.
     */
    static final double SHADOW_DX = 5;

    static final double SHADOW_DY = -6;
    static final float SHADOW_ALPHA = 0.38f;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> dust;
    private final FlashShader flash;
    private final double[] distances = DISTANCES.clone();
    /** The segments laid this frame: centre (x, y up), the unit way to the tail, heading index and leftover turn. */
    private final double[] segX = new double[SEGMENTS];

    private final double[] segY = new double[SEGMENTS];
    private final double[] segDirX = new double[SEGMENTS];
    private final double[] segDirY = new double[SEGMENTS];
    private final int[] segHeading = new int[SEGMENTS];
    private final float[] segTilt = new float[SEGMENTS];
    /** The track laid last (the ridge's curve ending at the mouth) and how far along it the back may show, px. */
    private double[] trackX;

    private double[] trackY;
    private int trackSamples;
    private double reach;
    /** The front this frame ({@link #front}), px from the mouth's centre, and the puffs' clock, s. */
    private double front = FRONT_FROM;

    private double puffClock = -1;
    private boolean diving;
    /** The breach's rise and dive set last, s. */
    private double rise = 1;

    private double dive = 1;
    private final double[] place = new double[2];
    private final double[] ahead = new double[2];

    ColossusSpine(Array<AtlasRegion> frames, Array<AtlasRegion> dust, FlashShader flash, JsonValue pivots) {
        this.frames = frames;
        this.dust = dust;
        this.flash = flash;
        JsonValue spine = pivots == null ? null : pivots.get("spine");
        JsonValue given = spine == null ? null : spine.get("distances");
        if (given != null && given.size == SEGMENTS) {
            for (int i = 0; i < SEGMENTS; i++) {
                distances[i] = given.getDouble(i);
            }
        }
    }

    /** Whether the spine's sprites are all there. */
    boolean drawable() {
        return frames.size >= SEGMENTS * STATES * RidgeLooks.HEADINGS;
    }

    /** The spine sprite's frame: segment {@code seg}, its state, its heading ((seg * 3 + state) * 16 + heading). */
    static int frame(int seg, int state, int heading) {
        return (seg * STATES + state) * RidgeLooks.HEADINGS + heading;
    }

    /**
     * The turn a heading frame needs to point exactly along a direction ({@code dx} right, {@code dy} up), degrees
     * counter-clockwise (the batch's rotation): the frames step 22.5° clockwise from down ({@link RidgeLooks#heading}),
     * so this is minus what the direction turns past its frame's heading, at most ±11.25°.
     */
    static float tilt(double dx, double dy) {
        double turn = Math.atan2(-dx, -dy);
        double step = 2 * Math.PI / RidgeLooks.HEADINGS;
        return (float) -Math.toDegrees(turn - Math.round(turn / step) * step);
    }

    /** Whether a mouth at {@code pose} shows its back (an edge breach; the under pose's body goes straight down). */
    static boolean shows(BreachArena.Pose pose) {
        return pose != BreachArena.Pose.UNDER;
    }

    // --- the front ---------------------------------------------------------------------------------

    /**
     * The emergence front {@code elapsed} s into {@code phase} (the rise and the dive {@code rise} and {@code dive} s
     * long), px from the mouth's centre along the back: the rise runs it from {@link #FRONT_FROM} (nothing out) to
     * {@link #FRONT_TO} (all up) at a steady pace, head to tail; open and the bite's tell hold it there; the dive runs it
     * back to {@link #FRONT_FROM} over its first {@value #DIVE_SHARE}, tail to head; buried and the tell: nothing out.
     */
    static double front(BreachArena.Phase phase, double elapsed, double rise, double dive) {
        return switch (phase) {
            case RISE -> FRONT_FROM + (FRONT_TO - FRONT_FROM) * Math.clamp(elapsed / rise, 0, 1);
            case OPEN, BITE -> FRONT_TO;
            case DIVE -> FRONT_TO - (FRONT_TO - FRONT_FROM) * Math.clamp(elapsed / (dive * DIVE_SHARE), 0, 1);
            default -> FRONT_FROM;
        };
    }

    /**
     * How far segment {@code seg} has risen with the front at {@code front} (0–1, 1 up): from when the front reaches its
     * first plates until it is {@value #BAND} px past its last; its state blends from a third out (0) through two
     * thirds to up (1).
     */
    static double emergence(int seg, double front) {
        return Math.clamp((front - SPAN_START[seg]) / (SPAN_END[seg] - SPAN_START[seg] + BAND), 0, 1);
    }

    /**
     * How much of the back shows {@code along} px from the mouth's centre with the front at {@code front} (0–1): the
     * reveal's soft edge ({@link FlashShader#revealUpTo}'s rule).
     */
    static double shown(double front, double along) {
        return Math.clamp((front - along) / SOFT + 0.5, 0, 1);
    }

    /** Whether any of segment {@code seg} shows with the front at {@code front}, the track reaching {@code reach} px. */
    static boolean out(int seg, double front, double reach) {
        return Math.min(front, reach) + SOFT / 2 > SPAN_START[seg];
    }

    // --- the track ---------------------------------------------------------------------------------

    /** The length of the track sampled in {@code cx}, {@code cy} ({@code samples} points), px. */
    static double length(double[] cx, double[] cy, int samples) {
        double total = 0;
        for (int i = 1; i < samples; i++) {
            total += Math.hypot(cx[i] - cx[i - 1], cy[i] - cy[i - 1]);
        }
        return total;
    }

    /**
     * The place {@code d} px back from the track's end (the mouth) along the track sampled in {@code cx}, {@code cy}
     * ({@code samples} points from the last breach point to the mouth): along its samples as {@link
     * ColossusLooks#layOut} lays the ridge's body, straight on past its start. {@code out}: x, y.
     */
    static void onTrack(double[] cx, double[] cy, int samples, double d, double[] out) {
        double left = Math.max(0, d);
        for (int i = samples - 1; i > 0; i--) {
            double dx = cx[i - 1] - cx[i];
            double dy = cy[i - 1] - cy[i];
            double length = Math.hypot(dx, dy);
            if (length >= left || i == 1) {
                double u = length > 1e-9 ? left / length : 0;
                out[0] = cx[i] + dx * u;
                out[1] = cy[i] + dy * u;
                return;
            }
            left -= length;
        }
        out[0] = cx[0];
        out[1] = cy[0];
    }

    /**
     * Lays the segments on the track behind the mouth ({@code cx}, {@code cy}, {@code samples} points ending at the
     * mouth; kept, not copied): each centred at its distance, heading along the track there.
     */
    void lay(double[] cx, double[] cy, int samples) {
        trackX = cx;
        trackY = cy;
        trackSamples = samples;
        reach = length(cx, cy, samples) - TRACK_TAPER;
        for (int i = 0; i < SEGMENTS; i++) {
            double d = distances[i];
            onTrack(cx, cy, samples, d, place);
            segX[i] = place[0];
            segY[i] = place[1];
            onTrack(cx, cy, samples, d - CHORD, ahead);
            onTrack(cx, cy, samples, d + CHORD, place);
            double dx = place[0] - ahead[0];
            double dy = place[1] - ahead[1];
            double length = Math.hypot(dx, dy);
            if (length < 1e-9) {
                dx = 0;
                dy = 1;
            } else {
                dx /= length;
                dy /= length;
            }
            segDirX[i] = dx;
            segDirY[i] = dy;
            segHeading[i] = RidgeLooks.heading(dx, dy);
            segTilt[i] = tilt(dx, dy);
        }
    }

    /**
     * Sets the front for this frame: {@code elapsed} s into {@code phase} of a breach whose rise and dive are {@code
     * rise} and {@code dive} s long ({@link #front}).
     */
    void at(BreachArena.Phase phase, double elapsed, double rise, double dive) {
        front = front(phase, elapsed, rise, dive);
        diving = phase == BreachArena.Phase.DIVE;
        puffClock = switch (phase) {
            case RISE, DIVE -> elapsed;
            case OPEN -> rise + elapsed;
            default -> -1;
        };
        this.rise = rise;
        this.dive = dive;
    }

    /** The front set last, px from the mouth's centre. */
    double front() {
        return front;
    }

    /** How far the track laid last lets the back show, px from the mouth's centre. */
    double reach() {
        return reach;
    }

    /** Segment {@code i}'s centre as laid last: x, and y up. */
    double x(int i) {
        return segX[i];
    }

    double y(int i) {
        return segY[i];
    }

    /** Segment {@code i}'s emergence with the front set last (0–1, 1 up). */
    double emergence(int i) {
        return emergence(i, front);
    }

    /** Whether any of segment {@code i} shows with the front set last. */
    boolean out(int i) {
        return out(i, front, reach);
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * Draws the segments laid last, tail first, each shown up to the front in its blended state, over their shadows;
     * then the sand spilling off the flanks at the front.
     */
    void draw(SpriteBatch batch, double realSeconds) {
        if (!drawable()) {
            return;
        }
        if (flash != null) {
            flash.beginReveal(batch);
        }
        for (int i = SEGMENTS - 1; i >= 0; i--) {
            if (!out(i)) {
                continue;
            }
            double state = UP * emergence(i);
            int low = Math.min(UP, (int) Math.floor(state));
            float mix = (float) (state - low);
            double lift = (state + 1.0) / STATES;
            double sx = segX[i] + SHADOW_DX * lift;
            double sy = segY[i] + SHADOW_DY * lift;
            cut(batch, i, sx, sy, false);
            batch.setColor(0, 0, 0, SHADOW_ALPHA * (1 - mix));
            drawTilted(batch, frames.get(frame(i, low, segHeading[i])), sx, sy, segTilt[i]);
            if (mix > 0 && low < UP) {
                batch.setColor(0, 0, 0, SHADOW_ALPHA * mix);
                drawTilted(batch, frames.get(frame(i, low + 1, segHeading[i])), sx, sy, segTilt[i]);
            }
        }
        for (int i = SEGMENTS - 1; i >= 0; i--) {
            if (!out(i)) {
                continue;
            }
            double state = UP * emergence(i);
            int low = Math.min(UP, (int) Math.floor(state));
            float mix = (float) (state - low);
            cut(batch, i, segX[i], segY[i], true);
            batch.setColor(Color.WHITE);
            drawTilted(batch, frames.get(frame(i, low, segHeading[i])), segX[i], segY[i], segTilt[i]);
            if (mix > 0 && low < UP) {
                batch.setColor(1, 1, 1, mix);
                drawTilted(batch, frames.get(frame(i, low + 1, segHeading[i])), segX[i], segY[i], segTilt[i]);
            }
        }
        if (flash != null) {
            flash.end(batch);
        }
        batch.setColor(Color.WHITE);
        drawPuffs(batch);
    }

    /**
     * Draws {@code region} (a vent organ, its scar) centred on ({@code x}, {@code y} up) as part of segment {@code i}:
     * shown up to the front as the segment is.
     */
    void drawOn(SpriteBatch batch, TextureRegion region, double x, double y, int i) {
        boolean cut = flash != null && emergence(i) < 1;
        if (cut) {
            flash.beginReveal(batch);
            this.cut(batch, i, x, y, true);
        }
        PlayFrame.drawCentred(batch, region, x, y);
        if (cut) {
            flash.end(batch);
        }
    }

    /**
     * The reveal's edge for segment {@code i} drawn centred on ({@code x}, {@code y} up), with the sand on the plates
     * behind it ({@code sand}; not for a shadow).
     */
    private void cut(SpriteBatch batch, int i, double x, double y, boolean sand) {
        if (flash == null) {
            return;
        }
        double edge = Math.min(front, reach) - distances[i];
        flash.revealUpTo(
                batch,
                (float) (X0 + x),
                (float) y,
                (float) segDirX[i],
                (float) segDirY[i],
                (float) edge,
                (float) SOFT,
                SAND_COLOUR,
                sand ? (float) SAND : 0);
    }

    /**
     * The sand spilling off the flanks where the front passes: a puff every {@value #PUFF_STEP} px along the back,
     * alternately left and right, playing once from when the front passes it ({@link #puffAge}).
     */
    private void drawPuffs(SpriteBatch batch) {
        if (dust.size == 0 || puffClock < 0 || trackX == null) {
            return;
        }
        batch.setColor(1, 1, 1, PUFF_ALPHA);
        for (int k = 0; k < PUFFS; k++) {
            double d = HEAD + PUFF_STEP * (k + 0.5);
            if (d > reach) {
                break;
            }
            double age = puffAge(d, puffClock, diving, rise, dive);
            if (age < 0 || age >= PUFF_SECONDS) {
                continue;
            }
            onTrack(trackX, trackY, trackSamples, d - CHORD, ahead);
            onTrack(trackX, trackY, trackSamples, d + CHORD, place);
            double dx = place[0] - ahead[0];
            double dy = place[1] - ahead[1];
            double length = Math.max(1e-9, Math.hypot(dx, dy));
            double side = (k % 2 == 0 ? PUFF_SIDE : -PUFF_SIDE) / length;
            onTrack(trackX, trackY, trackSamples, d, place);
            int f = Math.min(dust.size - 1, (int) (age / PUFF_SECONDS * dust.size));
            PlayFrame.drawCentred(batch, dust.get(f), place[0] - dy * side, place[1] + dx * side);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * How long ago the front passed {@code d} px along the back, s, at {@code clock} s into the rise (on through the
     * open) or the dive ({@code diving}); negative before it has.
     */
    static double puffAge(double d, double clock, boolean diving, double rise, double dive) {
        double share = (d - FRONT_FROM) / (FRONT_TO - FRONT_FROM);
        double passed = diving ? dive * DIVE_SHARE * (1 - share) : rise * share;
        return clock - passed;
    }

    /** Draws {@code region} centred on ({@code x}, {@code y} up), turned {@code degrees} counter-clockwise about it. */
    private static void drawTilted(SpriteBatch batch, AtlasRegion region, double x, double y, float degrees) {
        int width = region.getRegionWidth();
        int height = region.getRegionHeight();
        batch.draw(
                region,
                Math.round(X0 + x - width / 2.0),
                Math.round(y - height / 2.0),
                width / 2f,
                height / 2f,
                width,
                height,
                1,
                1,
                degrees);
    }
}
