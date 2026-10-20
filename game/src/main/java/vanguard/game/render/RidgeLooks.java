package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M7 part G, the Dust Colossus's travelling ridge (design/campaign Level 20, Layout, section 1: "from t≈12 a travelling
 * ridge (≈ 900 px, the Dust Colossus's body, scenery) crosses the floor near the top edge, dunes heaving"; Level 21's
 * ridge pass; the sprites of tools/art/l20_props.py): presentation only, nothing hits it. The level's {@code ridge} key
 * places the pass (round 43 Q.4: its own key, no longer the start of the {@code basin-rumble} ambience layer): the head
 * surfaces off its {@code from} edge at its {@code t}, {@code depth} px below the top edge, and runs on its own track
 * under the sand, a slow S up the strip and across the field (the review GIF's path, made the game's), to off its
 * {@code to} edge {@code seconds} s later; the body is the last {@value #LENGTH} px of the head's track, so it slides
 * along the track, which lies on the ground and scrolls with it, both ways. Drawn on the ground layer, back to front:
 * the tail where the track ends, a length every {@value #SPACING} px with the track's heading there, the head at the
 * front; the crest cracks' lime glow over each length, its alpha pulsing; a sand slide puffing off a flank at the
 * middle. It leaves by the {@code to} edge, the tail last, {@value #TAIL_SECONDS} s after the head. On the level clock
 * and the ground scroll, so a restart or {@code --debug-speed} keeps in step; nothing is allocated per frame.
 */
public final class RidgeLooks {
    /** The sprite root: lengths, {@code -head}, {@code -tail}, {@code -glow}, {@code -dust}. */
    public static final String ROOT = "colossus-ridge";
    /** The body's length along its track, px. */
    static final double LENGTH = 900;
    /** The lengths' spacing along the track, px (tools/art/l20_props.py's SPACING). */
    static final double SPACING = 16;
    /** The most points a body has: its head and a point every {@link #SPACING} px of its length. */
    static final int MAX_POINTS = (int) (LENGTH / SPACING) + 1;
    /** The headings of the ridge's sprites: k × 22.5° clockwise from straight down. */
    static final int HEADINGS = 16;
    /** This long after the head is off the far edge its tail is too (≈ the body's length at the track's speed). */
    static final double TAIL_SECONDS = 7;
    /** How far off the side edge the head surfaces and leaves, px. */
    static final double MARGIN = 40;
    /** The head's run up the strip, px/s of ground rows (a little slower than Level 20's strike at 140). */
    static final double RUN = 124;
    /** The track's S: across, its amplitude (px) and period (s per radian); up the strip, the same. */
    static final double SWAY = 30;

    static final double SWAY_PERIOD = 1.3;
    static final double SURGE = 60;
    static final double SURGE_PERIOD = 1.6;
    /** The track's sampling step while walking it back from the head, s of the head's run. */
    static final double SAMPLE_SECONDS = 0.01;
    /** The glow's pulse (radians a second) and its range. */
    static final double PULSE_RATE = 5;

    static final float PULSE_LOW = 0.1f;
    static final float PULSE_HIGH = 1f;

    private final double start;
    /** The head's crossing time, s: from off the {@code from} edge to off the {@code to} edge. */
    private final double crossing;
    /** The head's mean speed across, px/s. */
    private final double across;
    /** Whether the head runs from the right edge to the left. */
    private final boolean leftward;
    /** Where the head surfaces, px above the bottom edge. */
    private final double surface;
    /** The ground scroll at {@link #start}: the track's rows count from the bottom edge then. */
    private final double startScroll;

    /** The body's sprites and drawing (M7 part H: shared with the Dust Colossus's ridge, {@link ColossusLooks}). */
    private final RidgeBody body;
    /** The body's points this frame, head first: x, height above the bottom edge (y up) and heading index. */
    private final double[] xs = new double[MAX_POINTS];

    private final double[] ys = new double[MAX_POINTS];
    private final int[] headings = new int[MAX_POINTS];
    private int points;

    private RidgeLooks(LevelData.Ridge ridge, double startScroll, RidgeBody body) {
        this.start = ridge.t();
        this.crossing = ridge.seconds();
        this.across = (PlayField.WIDTH + 2 * MARGIN) / ridge.seconds();
        this.leftward = ridge.leftward();
        this.surface = PlayField.HEIGHT - ridge.depth();
        this.startScroll = startScroll;
        this.body = body;
    }

    /** The ridge's track alone (no sprites), its pass {@code ridge} with the ground at {@code startScroll}: for the tests. */
    static RidgeLooks track(LevelData.Ridge ridge, double startScroll) {
        return new RidgeLooks(ridge, startScroll, RidgeBody.none());
    }

    /** The level's ridge, or null for a level without a {@code ridge} key (or without the sprites). */
    static RidgeLooks of(Sprites sprites, LevelData level) {
        if (level.ridge().isEmpty() || !RidgeBody.drawable(sprites)) {
            return null;
        }
        LevelData.Ridge ridge = level.ridge().get();
        return new RidgeLooks(ridge, level.scrollAt(ridge.t()), RidgeBody.of(sprites));
    }

    /**
     * The head's x on the play field {@code u} s after it surfaced: from off the {@code from} edge across to off the
     * {@code to} edge in the crossing time, a slow S (Level 20's: {@code -40 + 70u + 30 sin(u / 1.3)}).
     */
    double headX(double u) {
        double along = -MARGIN + across * u + SWAY * Math.sin(u / SWAY_PERIOD);
        return leftward ? PlayField.WIDTH - along : along;
    }

    /**
     * The head's place on the ground {@code u} s after it surfaced, px above the bottom edge as it was then: from its
     * depth below the top edge, running up the strip at {@value #RUN} px/s, a slow S (Level 20's: {@code 470 + 124u -
     * 60 sin(u / 1.6)}).
     */
    double headRow(double u) {
        return surface + RUN * u - SURGE * Math.sin(u / SURGE_PERIOD);
    }

    /** The head's heading index {@code u} s after it surfaced: its track's direction (k × 22.5° clockwise from down). */
    int heading(double u) {
        double dx = across + SWAY / SWAY_PERIOD * Math.cos(u / SWAY_PERIOD);
        double dy = RUN - SURGE / SURGE_PERIOD * Math.cos(u / SURGE_PERIOD);
        return heading(leftward ? -dx : dx, dy);
    }

    /** The heading index of a direction ({@code dx} right, {@code dy} up): 0 down, 4 left, 8 up, 12 right. */
    static int heading(double dx, double dy) {
        double turn = Math.atan2(-dx, -dy);
        return Math.floorMod((int) Math.round(turn / (2 * Math.PI) * HEADINGS), HEADINGS);
    }

    /** Whether the ridge shows at level time {@code seconds}: from its start until the tail is past the far edge. */
    boolean shows(double seconds) {
        return seconds > start && seconds < end();
    }

    /** The level time the tail is past the far edge: nothing more to draw. */
    double end() {
        return start + crossing + TAIL_SECONDS;
    }

    /**
     * Lays the body out at level time {@code seconds} with the ground at {@code scroll}: a point every {@link
     * #SPACING} px back along the head's track from the head, at most {@link #LENGTH} px and never before it surfaced;
     * returns how many.
     */
    int layOut(double seconds, double scroll) {
        double u = seconds - start;
        double shift = scroll - startScroll;
        points = 0;
        if (!(u > 0)) {
            return 0;
        }
        double x = headX(u);
        double row = headRow(u);
        add(x, row - shift, heading(u));
        double run = 0;
        double walked = 0;
        for (double back = u - SAMPLE_SECONDS; back > 0 && points < MAX_POINTS; back -= SAMPLE_SECONDS) {
            double nx = headX(back);
            double nrow = headRow(back);
            double step = Math.hypot(nx - x, nrow - row);
            run += step;
            walked += step;
            x = nx;
            row = nrow;
            if (walked > LENGTH) {
                break;
            }
            if (run >= SPACING) {
                run -= SPACING;
                add(x, row - shift, heading(back));
            }
        }
        return points;
    }

    private void add(double x, double y, int heading) {
        xs[points] = x;
        ys[points] = y;
        headings[points] = heading;
        points++;
    }

    /** Point {@code i}'s x (0 the head), for the tests. */
    double x(int i) {
        return xs[i];
    }

    /** Point {@code i}'s height above the bottom edge, for the tests. */
    double y(int i) {
        return ys[i];
    }

    /** Point {@code i}'s heading index, for the tests. */
    int headingAt(int i) {
        return headings[i];
    }

    /** The glow's alpha at level time {@code seconds}: a slow pulse between {@link #PULSE_LOW} and {@link #PULSE_HIGH}. */
    static float pulse(double seconds) {
        double wave = 0.5 + 0.5 * Math.sin(seconds * PULSE_RATE);
        return (float) (PULSE_LOW + (PULSE_HIGH - PULSE_LOW) * wave);
    }

    /** The body on the ground at level time {@code seconds}, the ground at {@code scroll}. */
    void draw(SpriteBatch batch, double seconds, double scroll) {
        if (!shows(seconds) || layOut(seconds, scroll) == 0) {
            return;
        }
        body.draw(batch, xs, ys, headings, points, seconds, true, pulse(seconds), 1);
    }

    /**
     * M7 part H: the light shake of the ridge pass (design/campaign Level 21, the ridge pass; round 43 Q.4), 0–1 at level
     * time {@code seconds}: rising over its first second, full while the head crosses, fading out over the tail's
     * {@link #TAIL_SECONDS} s; the game scales it by the screen-shake setting ({@code ScreenShake}).
     */
    double shake(double seconds) {
        double u = seconds - start;
        if (!(u > 0) || seconds >= end()) {
            return 0;
        }
        double in = Math.min(1, u);
        double out = Math.min(1, (end() - seconds) / TAIL_SECONDS);
        return in * out;
    }
}
