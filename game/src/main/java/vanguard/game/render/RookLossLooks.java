package vanguard.game.render;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Optional;
import vanguard.sim.RookLoss;
import vanguard.sim.RookLossSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M8 part F: the look of Rook's loss (design/campaign Level 26, Rook's loss; design/player/wingmen, Rook's loss and
 * absence; m8f-gaps.md §1.5, §5 F6, F7; frames from tools/art/rook_loss.py), driven by the simulation's {@link
 * RookLoss} ({@link Wingman#loss}) and the level's {@link RookLossSpec}: drawn only, no hit boxes.
 *
 * <ul>
 *   <li><b>The trails</b> (from the trails' time, {@code ROOK_TRAILS}): three thin white-orange torpedo trails rise from
 *       the bottom edge in their lanes (his x and x ± {@value #LANE} px when the trails start; a lane within {@value
 *       #LANE_CLEAR} px of the ship's x moves to his far side, F6), converging on him: torpedo {@code i} reaches him
 *       {@code i} × {@value #FLASH_GAP} s after the loss's time. Each trail is the {@code rook-trail} strip repeated
 *       every 64 px from its torpedo's head ({@code rook-torpedo}) back to the bottom edge, faded toward its tail and
 *       out over {@value #TRAIL_FADE} s after its hit.
 *   <li><b>The flashes</b>: at each torpedo's hit a {@code rook-flash} on him (20 fps).
 *   <li><b>The spin</b> (from {@code ROOK_LOST}): his hull as {@code rook-spin} at his hull's turn ({@link
 *       RookLoss#hullTurn}), dimming toward the black, and over the grade his failing lamp's and torn engine's {@code
 *       rook-spin-glow}; the lamp ({@link #lamp}) flickers (on, off, a stutter) and dies over the spin, its centre the
 *       pivot file's {@code lamp} per spin frame. Gone below the bottom edge, nothing.
 * </ul>
 *
 * Read from the level clock and the loss's state, so a retry needs nothing reset but the lanes, which are planned again
 * when the trails start again. Allocation-free per frame.
 */
final class RookLossLooks {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** The side lanes' distance from his x, px; a lane this close to the ship's x moves to his far side. */
    static final double LANE = 48;

    static final double LANE_CLEAR = 40;
    /** The torpedoes reach him this far apart, s (the art's three flashes 0.1 s apart). */
    static final double FLASH_GAP = 0.1;

    static final int TORPEDOES = 3;
    /** They rise from this far below the bottom edge, px. */
    static final double BELOW = 12;
    /** A trail fades out over this long after its torpedo's hit, s. */
    static final double TRAIL_FADE = 0.6;
    /** The trail's tail end: its strength there, of its head's. */
    static final float TAIL = 0.25f;
    /** The trail's and the torpedo's churn, frames a second; the flash's. */
    static final double TRAIL_FPS = 12;

    static final double FLASH_FPS = 20;
    /** The trail strip's length, px (it repeats along the trail). */
    static final int STRIP = 64;
    /** The lamp's flicker: a slot's length, s; it stays dark this long after the hit (the flashes cover it). */
    static final double FLICKER_SLOT = 0.08;

    static final double DARK_AFTER_HIT = 0.15;
    /** His hull dims to this share of its colour by the spin's end (going dark). */
    static final float DIM = 0.8f;

    private final Optional<RookLossSpec> spec;
    private final Array<AtlasRegion> spin;
    private final Array<AtlasRegion> spinGlow;
    private final Array<AtlasRegion> trail;
    private final Array<AtlasRegion> torpedo;
    private final Array<AtlasRegion> flash;
    /** Per spin frame the lamp's offset from the hull's centre, px, y up. */
    private final float[] lampDx;

    private final float[] lampDy;
    /** The lanes' x offsets from his x at the trails' start; planned at the first frame of the trails. */
    private final double[] lanes = new double[TORPEDOES];

    private boolean planned;
    private final double[] lane = new double[2];

    RookLossLooks(Sprites sprites, Files files, Optional<RookLossSpec> spec) {
        this.spec = spec;
        spin = frames(sprites, "rook-spin");
        spinGlow = frames(sprites, "rook-spin-glow");
        trail = frames(sprites, "rook-trail");
        torpedo = frames(sprites, "rook-torpedo");
        flash = frames(sprites, "rook-flash");
        int count = Math.max(1, spin.size);
        lampDx = new float[count];
        lampDy = new float[count];
        FileHandle pivots = files.internal("pivots/rook-spin.json");
        if (pivots.exists()) {
            JsonValue json = new JsonReader().parse(pivots);
            float half = json.get("frame").asIntArray()[0] / 2f;
            JsonValue lamp = json.get("lamp");
            for (int k = 0; k < Math.min(count, lamp.size); k++) {
                float[] point = lamp.get(k).asFloatArray();
                lampDx[k] = point[0] - half;
                lampDy[k] = half - point[1];
            }
        }
    }

    private static Array<AtlasRegion> frames(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Whether the level has a loss of Rook. */
    boolean planned() {
        return spec.isPresent();
    }

    /**
     * The lanes' x offsets from his x {@code rookX} with the ship at {@code shipX} into {@code out} (3): 0 and ± {@value
     * #LANE}; a lane within {@value #LANE_CLEAR} px of the ship moves to his far side, beyond the lanes there (F6).
     */
    static void lanes(double rookX, double shipX, double[] out) {
        double far = rookX >= shipX ? 1 : -1;
        double next = 2 * LANE;
        for (int i = 0; i < TORPEDOES; i++) {
            double offset = (i - 1) * LANE;
            if (Math.abs(rookX + offset - shipX) < LANE_CLEAR) {
                offset = far * next;
                next += LANE;
            }
            out[i] = offset;
        }
    }

    /** Torpedo {@code i}'s hit, s on the level clock: the loss's time and {@value #FLASH_GAP} s apart after it. */
    static double hit(RookLossSpec loss, int i) {
        return loss.t() + i * FLASH_GAP;
    }

    /**
     * How far torpedo {@code i} has come at level time {@code now}, 0 at the bottom edge at the trails' time to 1 on him
     * at its hit.
     */
    static double progress(RookLossSpec loss, int i, double now) {
        double hit = hit(loss, i);
        return Math.clamp((now - loss.trails()) / (hit - loss.trails()), 0, 1);
    }

    /**
     * Its head at {@code p} from its lane at the bottom ({@code laneX}, {@value #BELOW} px below the edge) to him at
     * ({@code toX}, {@code toY}) into {@code out}: rising steadily, turning in on him late (it homes).
     */
    static void head(double laneX, double toX, double toY, double p, double[] out) {
        out[0] = laneX + (toX - laneX) * p * p;
        out[1] = -BELOW + (toY + BELOW) * p;
    }

    /**
     * The lamp's light, 0 to 1, for his loss's state: full while he flies, dark for {@value #DARK_AFTER_HIT} s after
     * the hit, then flickering (on, off, a stutter: per {@value #FLICKER_SLOT} s slot, more often off as the spin goes
     * on) and dying with the spin; 0 once he is gone.
     */
    static float lamp(boolean lost, boolean gone, double sinceLoss, double progress) {
        if (!lost) {
            return 1;
        }
        if (gone || sinceLoss < DARK_AFTER_HIT) {
            return 0;
        }
        long slot = (long) Math.floor(sinceLoss / FLICKER_SLOT);
        double u = ((slot * 0x9E3779B97F4A7C15L) >>> 40) / (double) (1L << 24);
        double dying = Math.pow(1 - progress, 1.2);
        if (u < 0.25 + 0.45 * progress) {
            return 0;
        }
        return (float) (dying * (u < 0.45 + 0.35 * progress ? 0.4 : 1));
    }

    /** The lamp's light now for {@code rook} (1 without a loss). */
    static float lamp(Wingman rook) {
        RookLoss loss = rook.loss();
        return lamp(loss.lost(), loss.gone(), loss.ticksSinceLoss() * SimStep.SECONDS, loss.spinProgress());
    }

    /** The spin frame for his hull's turn {@code turn} (radians, clockwise positive) of {@code frames}. */
    static int spinFrame(double turn, int frames) {
        return Math.floorMod((int) Math.round(turn / (2 * Math.PI / frames)), frames);
    }

    /** The lamp's place for {@code rook} at {@code alpha}: on his hull, the spin frame's lamp while he spins. */
    double lampX(Wingman rook, float alpha) {
        double x = rook.renderX(alpha);
        if (!rook.loss().lost() || spin.isEmpty()) {
            return x;
        }
        return x + lampDx[spinFrame(rook.loss().hullTurn(), spin.size)];
    }

    double lampY(Wingman rook, float alpha) {
        double y = rook.renderY(alpha);
        if (!rook.loss().lost() || spin.isEmpty()) {
            return y;
        }
        return y + lampDy[spinFrame(rook.loss().hullTurn(), spin.size)];
    }

    /** The level clock at the render's {@code alpha} between the last two steps. */
    private static double now(Sortie sortie, float alpha) {
        return sortie.levelSeconds() - (1 - alpha) * SimStep.SECONDS * sortie.scriptRate();
    }

    /**
     * His spinning hull in place of his craft (under the grade, as {@link WingmanLooks#draw} would draw him), dimming
     * toward the black; nothing before his loss or once he is gone.
     */
    void drawSpin(SpriteBatch batch, Wingman rook, float alpha) {
        RookLoss loss = rook.loss();
        if (!loss.lost() || loss.gone() || spin.isEmpty()) {
            return;
        }
        AtlasRegion frame = spin.get(spinFrame(loss.hullTurn(), spin.size));
        float shade = 1 - DIM * (float) loss.spinProgress();
        batch.setColor(shade, shade, shade, 1);
        batch.draw(
                frame,
                Math.round(X0 + rook.renderX(alpha) - frame.getRegionWidth() / 2f),
                Math.round(rook.renderY(alpha) - frame.getRegionHeight() / 2f));
        batch.setColor(1, 1, 1, 1);
    }

    /**
     * Over the grade, additive: the trails and their torpedoes, the hits' flashes on him, and his failing lamp's and
     * engine's glow while he spins down.
     */
    void drawOver(SpriteBatch batch, Sortie sortie, float alpha) {
        Wingman rook = sortie.wingman().orElse(null);
        if (spec.isEmpty() || rook == null) {
            return;
        }
        RookLoss loss = rook.loss();
        if (!loss.trailing()) {
            planned = false;
            return;
        }
        if (rook.ejected()) {
            return;
        }
        RookLossSpec spec = this.spec.get();
        double rookX = rook.renderX(alpha);
        double rookY = rook.renderY(alpha);
        if (!planned) {
            lanes(rookX, sortie.ship().renderX(alpha), lanes);
            planned = true;
        }
        double now = now(sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < TORPEDOES; i++) {
            drawTrail(batch, spec, i, now, rookX, rookY);
        }
        for (int i = 0; i < TORPEDOES; i++) {
            drawFlash(batch, spec, i, now, rookX, rookY);
        }
        if (loss.lost() && !loss.gone() && !spinGlow.isEmpty()) {
            float light = lamp(rook);
            if (light > 0) {
                AtlasRegion frame = spinGlow.get(spinFrame(loss.hullTurn(), spinGlow.size));
                batch.setColor(1, 1, 1, light);
                batch.draw(
                        frame,
                        Math.round(X0 + rookX - frame.getRegionWidth() / 2f),
                        Math.round(rookY - frame.getRegionHeight() / 2f));
            }
        }
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Trail {@code i}: its strip from its head back to its lane's start, and its torpedo until its hit. */
    private void drawTrail(SpriteBatch batch, RookLossSpec spec, int i, double now, double rookX, double rookY) {
        if (trail.isEmpty()) {
            return;
        }
        double hit = hit(spec, i);
        float fade = now <= hit ? 1 : (float) Math.max(0, 1 - (now - hit) / TRAIL_FADE);
        if (fade <= 0) {
            return;
        }
        double laneX = rookX + lanes[i];
        head(laneX, rookX, rookY, progress(spec, i, now), lane);
        double dx = lane[0] - laneX;
        double dy = lane[1] + BELOW;
        double length = Math.hypot(dx, dy);
        if (length < 1) {
            return;
        }
        float degrees = (float) Math.toDegrees(Math.atan2(-dx, dy));
        double ux = dx / length;
        double uy = dy / length;
        int phase = (int) Math.floor((now - spec.trails()) * TRAIL_FPS) + i * 3;
        AtlasRegion strip = trail.get(Math.floorMod(phase, trail.size));
        int tiles = (int) Math.ceil(length / STRIP);
        float w = strip.getRegionWidth();
        float h = strip.getRegionHeight();
        for (int k = 0; k < tiles; k++) {
            float along = (float) k / Math.max(1, tiles - 1);
            float strength = fade * (1 - (1 - TAIL) * along);
            double cx = lane[0] - ux * (STRIP / 2.0 + STRIP * k);
            double cy = lane[1] - uy * (STRIP / 2.0 + STRIP * k);
            batch.setColor(1, 1, 1, strength);
            batch.draw(strip, (float) (X0 + cx - w / 2), (float) (cy - h / 2), w / 2, h / 2, w, h, 1, 1, degrees);
        }
        if (now < hit && !torpedo.isEmpty()) {
            AtlasRegion motor = torpedo.get(Math.floorMod(phase, torpedo.size));
            batch.setColor(1, 1, 1, 1);
            batch.draw(
                    motor,
                    Math.round(X0 + lane[0] - motor.getRegionWidth() / 2f),
                    Math.round(lane[1] - motor.getRegionHeight() / 2f));
        }
    }

    /** Hit {@code i}'s flash on him, from its hit for its frames. */
    private void drawFlash(SpriteBatch batch, RookLossSpec spec, int i, double now, double rookX, double rookY) {
        double since = now - hit(spec, i);
        int frame = (int) Math.floor(since * FLASH_FPS);
        if (since < 0 || frame >= flash.size) {
            return;
        }
        AtlasRegion region = flash.get(frame);
        batch.setColor(1, 1, 1, 1);
        batch.draw(
                region,
                Math.round(X0 + rookX - region.getRegionWidth() / 2f),
                Math.round(rookY - region.getRegionHeight() / 2f));
    }
}
