package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import vanguard.sim.BreachArena;
import vanguard.sim.PlayField;

/**
 * Round 44 K.b (step GK; tools/art/dust_colossus.py's {@code dust-colossus-spine}, briefs/l21-art-names.md "Round 44:
 * spine"; pending the user's review in round 45): the Dust Colossus's back breaking the sand behind a risen edge mouth
 * (front, left, right, rear; the under pose's body goes straight down), {@value #SEGMENTS} plated segments along the
 * ridge's curve that ends at the mouth, the first three carrying the spine vents (the simulation's {@code
 * BreachArena.ventX}: the same distances, measured the same way), the fourth a tapering tail. Each segment rises a beat
 * after the one before it ({@value #STAGGER_SECONDS} s, head to tail) in three steps over the mouth's rise (a third out
 * with the sand heaped on it, two thirds with the sand sliding off, up) and sinks back the same way in the dive.
 * Presentation only; nothing is allocated per frame.
 */
final class ColossusSpine {
    /** The segments: three vent segments and the tail. */
    static final int SEGMENTS = 4;
    /** A segment's states: a third out, two thirds, up. */
    static final int STATES = 3;
    /** The state a segment is fully up in (its vent shows). */
    static final int UP = STATES - 1;
    /** Each segment starts its rise (and its dive) this long after the one before it, s. */
    static final double STAGGER_SECONDS = 0.12;
    /** The segments' distances from the mouth's centre along the body, px (the pivots' {@code spine}). */
    static final double[] DISTANCES = {120, 200, 280, 350};
    /** The simulation's sampling of the curve and its margin from the field's edges (BreachArena.onSpine). */
    static final int SAMPLES = 64;

    static final double MARGIN = 20;
    /**
     * The back's shadow on the sand (step GK: the segments read as a chain of pale bubbles at 1×, an art question for
     * round 45; a shadow under them lifts them off the sand as one back): each segment's silhouette this far to the
     * lower right when fully up (less while rising), px, at this opacity, under all of them.
     */
    static final double SHADOW_DX = 5;

    static final double SHADOW_DY = -6;
    static final float SHADOW_ALPHA = 0.38f;

    private final Array<AtlasRegion> frames;
    private final double[] distances = DISTANCES.clone();
    /** The segments laid this frame: centre (x, y up) and heading index. */
    private final double[] segX = new double[SEGMENTS];

    private final double[] segY = new double[SEGMENTS];
    private final int[] segHeading = new int[SEGMENTS];
    private final double[] place = new double[4];

    ColossusSpine(Array<AtlasRegion> frames, JsonValue pivots) {
        this.frames = frames;
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

    /** Whether a mouth at {@code pose} shows its back (an edge breach; the under pose's body goes straight down). */
    static boolean shows(BreachArena.Pose pose) {
        return pose != BreachArena.Pose.UNDER;
    }

    /**
     * Segment {@code i}'s state (0 a third out, 1 two thirds, 2 up) {@code elapsed} s into {@code phase}, the rise and
     * the dive {@code rise} and {@code dive} s long; -1 while it is under the sand. The rise: a step each third of it
     * from {@value #STAGGER_SECONDS} s × i on; open and the bite's tell: up; the dive: up until its turn, then two
     * thirds and a third, a third of the dive each, then gone.
     */
    static int state(BreachArena.Phase phase, double elapsed, int i, double rise, double dive) {
        double u = elapsed - STAGGER_SECONDS * i;
        return switch (phase) {
            case RISE -> u < 0 ? -1 : Math.min(UP, (int) Math.floor(u / (rise / STATES)));
            case OPEN, BITE -> UP;
            case DIVE -> {
                if (u < 0) {
                    yield UP;
                }
                int down = (int) Math.floor(u / (dive / STATES));
                yield down >= UP ? -1 : UP - 1 - down;
            }
            default -> -1;
        };
    }

    /**
     * The place {@code d} px back from the mouth along the curve sampled in {@code cx}, {@code cy} ({@code samples}
     * points from the last breach point to the mouth, a quadratic curve), as {@code BreachArena.onSpine} measures it:
     * along the curve from the mouth, straight on where it turns back on itself (more than a right angle from its last
     * stretch) or ends, kept {@value #MARGIN} px inside the field. {@code out}: x, y, and the unit way back (away from
     * the mouth) there.
     */
    static void place(double[] cx, double[] cy, int samples, double d, double[] out) {
        double fromX = cx[0];
        double fromY = cy[0];
        double toX = cx[samples - 1];
        double toY = cy[samples - 1];
        int middle = (samples - 1) / 2;
        double um = (double) middle / (samples - 1);
        // The curve's control point from its middle sample: c(u) = (1-u)^2 a + 2u(1-u) b + u^2 c.
        double share = 2 * um * (1 - um);
        double viaX = (cx[middle] - (1 - um) * (1 - um) * fromX - um * um * toX) / share;
        double viaY = (cy[middle] - (1 - um) * (1 - um) * fromY - um * um * toY) / share;
        double left = d;
        double x = toX;
        double y = toY;
        double backX = fromX - toX;
        double backY = fromY - toY;
        double backLength = Math.sqrt(backX * backX + backY * backY);
        if (backLength < 1e-9) {
            backX = 0;
            backY = 1;
        } else {
            backX /= backLength;
            backY /= backLength;
        }
        boolean first = true;
        double firstX = backX;
        double firstY = backY;
        for (int k = SAMPLES - 1; k >= 0; k--) {
            double u = (double) k / SAMPLES;
            double px = (1 - u) * (1 - u) * fromX + 2 * u * (1 - u) * viaX + u * u * toX;
            double py = (1 - u) * (1 - u) * fromY + 2 * u * (1 - u) * viaY + u * u * toY;
            double dx = px - x;
            double dy = py - y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length < 1e-9) {
                continue;
            }
            dx /= length;
            dy /= length;
            if (first) {
                first = false;
                firstX = dx;
                firstY = dy;
            } else if (dx * firstX + dy * firstY <= 0) {
                break;
            }
            backX = dx;
            backY = dy;
            if (length >= left) {
                break;
            }
            left -= length;
            x = px;
            y = py;
        }
        out[0] = Math.clamp(x + backX * left, MARGIN, PlayField.WIDTH - MARGIN);
        out[1] = Math.clamp(y + backY * left, MARGIN, PlayField.HEIGHT - MARGIN);
        out[2] = backX;
        out[3] = backY;
    }

    /**
     * Lays the segments along the curve behind the mouth ({@code cx}, {@code cy}, {@code samples} points ending at the
     * mouth); a vent segment whose vent rose with this breach sits on the vent's own place ({@code breach}'s, the
     * simulation's), heading along the body there.
     */
    void lay(double[] cx, double[] cy, int samples, BreachArena breach) {
        for (int i = 0; i < SEGMENTS; i++) {
            if (i < breach.ventCount() && breach.ventRaised(i)) {
                segX[i] = breach.ventX(i);
                segY[i] = breach.ventY(i);
                segHeading[i] = RidgeLooks.heading(-breach.ventDirX(i), -breach.ventDirY(i));
            } else {
                place(cx, cy, samples, distances[i], place);
                segX[i] = place[0];
                segY[i] = place[1];
                segHeading[i] = RidgeLooks.heading(place[2], place[3]);
            }
        }
    }

    /** Segment {@code i}'s centre as laid last: x, and y up. */
    double x(int i) {
        return segX[i];
    }

    double y(int i) {
        return segY[i];
    }

    /**
     * Draws the segments laid last, tail first, each in its state for {@code phase} ({@link #state}), over their
     * shadows.
     */
    void draw(SpriteBatch batch, BreachArena.Phase phase, double elapsed, double rise, double dive) {
        if (!drawable()) {
            return;
        }
        batch.setColor(0, 0, 0, SHADOW_ALPHA);
        for (int i = SEGMENTS - 1; i >= 0; i--) {
            int state = state(phase, elapsed, i, rise, dive);
            if (state >= 0) {
                double lift = (state + 1.0) / STATES;
                drawCentred(
                        batch,
                        frames.get(frame(i, state, segHeading[i])),
                        segX[i] + SHADOW_DX * lift,
                        segY[i] + SHADOW_DY * lift);
            }
        }
        batch.setColor(Color.WHITE);
        for (int i = SEGMENTS - 1; i >= 0; i--) {
            int state = state(phase, elapsed, i, rise, dive);
            if (state >= 0) {
                drawCentred(batch, frames.get(frame(i, state, segHeading[i])), segX[i], segY[i]);
            }
        }
    }
}
