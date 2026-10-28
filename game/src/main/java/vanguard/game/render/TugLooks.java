package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;
import vanguard.sim.Ally;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part D: a hull escort's tug (design/allies/mining-tug; Level 31's <i>Bellwether</i>; tools/art/belt_units_d.py),
 * drawn on the ground layer over the belt's outcrops from its sections' frames ({@code mining-tug-<section>}: intact,
 * below 50 %, the same with its lights out, lost), each centred at its offset from the hull's centre ({@code
 * pivots/mining-tug.json}, y down; the sway is the hull's): the engines first, then the cargo, the bow on top.
 *
 * <ul>
 *   <li>a section below 50 % shows its damaged frame with its lights flickering ({@value #FLICKER_HZ} Hz, one beat in
 *       three dark) and a venting plume trailing down from each of its smoke points ({@code -vent}, a loop);
 *   <li>a hit flashes its section white ({@value #HIT_FLASH_TICKS} steps);
 *   <li>a lost section flares ({@code -burn}, additive, {@value #BURN_FRAME_TICKS} steps a frame), then lies dark and
 *       vents; the tug flies on;
 *   <li>its living engines glow at the drive bells ({@code -glow}, additive, the small bells at their scale) and burn
 *       at full on the climb-out ({@code -flare});
 *   <li>all sections lost: the hull breaks at its joints and the dark pieces drift down and off the bottom edge over
 *       {@value #BREAK_SECONDS} s, darkening (presentation only).
 * </ul>
 *
 * Without the frames (or without a hull escort) nothing is drawn.
 */
final class TugLooks {
    /** The tug's sprites' root. */
    static final String SLUG = "mining-tug";
    /** A section's frames: intact, below 50 %, below 50 % with its lights out, lost. */
    static final int INTACT = 0;

    static final int DAMAGED = 1;
    static final int FLICKER = 2;
    static final int LOST = 3;
    /** The damaged lights' flicker: one beat in three dark at this rate. */
    static final double FLICKER_HZ = 9;
    /** A hit's white flash on its section. */
    static final int HIT_FLASH_TICKS = 2;
    /** The loss's fire flare, steps a frame (8 frames: 0.8 s). */
    static final int BURN_FRAME_TICKS = 6;
    /** The vents' plume, steps a frame (a 10 fps loop). */
    static final int VENT_FRAME_TICKS = 6;
    /** The drive glow's flicker, steps a frame (12 fps). */
    static final int GLOW_FRAME_TICKS = 5;
    /** The hull's break-up when every section is lost: its length, s. */
    static final double BREAK_SECONDS = 4;
    /** The bells' glow sits this far (y down, px at scale 1) inside the bell's mouth; the flare a little more. */
    private static final double GLOW_INSET = 10;

    private static final double FLARE_INSET = 12;

    private final FlashShader flash;
    /** Per section in hull order: its frames, its frame's centre from the hull's (y up), its smoke points (y up). */
    private final List<Array<AtlasRegion>> frames;

    private final double[][] offset;
    private final double[][][] smoke;
    /** The engines section's index (the bells' glow lives with it), -1 without one. */
    private final int engines;
    /** The drive bells: [x, y up, scale] from the hull's centre. */
    private final double[][] bells;

    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> flare;
    private final Array<AtlasRegion> vent;
    private final Array<AtlasRegion> burn;
    private final boolean drawn;

    TugLooks(Sprites sprites, FlashShader flash, Sortie sortie, JsonValue pivots) {
        this.flash = flash;
        int count = sortie.hullEscort() ? sortie.allyCount() : 0;
        frames = new ArrayList<>(count);
        for (int k = 0; k < count; k++) {
            frames.add(null);
        }
        offset = new double[count][2];
        smoke = new double[count][][];
        JsonValue sections = pivots == null ? null : pivots.get("sections");
        boolean all = count > 0 && sections != null;
        int found = -1;
        for (int k = 0; k < count; k++) {
            String name = sortie.allyName(k);
            if (name.equals("engines")) {
                found = k;
            }
            String root = SLUG + "-" + name;
            JsonValue section = sections == null ? null : sections.get(name);
            if (!sprites.has(root) || section == null) {
                all = false;
                smoke[k] = new double[0][];
                continue;
            }
            frames.set(k, sprites.frames(root));
            offset[k][0] = section.get("offset").getDouble(0);
            offset[k][1] = -section.get("offset").getDouble(1);
            JsonValue points = section.get("smoke");
            smoke[k] = new double[points == null ? 0 : points.size][];
            for (int j = 0; j < smoke[k].length; j++) {
                smoke[k][j] =
                        new double[] {points.get(j).getDouble(0), -points.get(j).getDouble(1)};
            }
        }
        engines = found;
        JsonValue at = pivots == null ? null : pivots.get("bells");
        bells = new double[at == null ? 0 : at.size][];
        for (int b = 0; b < bells.length; b++) {
            JsonValue bell = at.get(b);
            bells[b] =
                    new double[] {bell.get("at").getDouble(0), -bell.get("at").getDouble(1), bell.getDouble("scale", 1)
                    };
        }
        glow = optional(sprites, SLUG + "-glow");
        flare = optional(sprites, SLUG + "-flare");
        vent = optional(sprites, SLUG + "-vent");
        burn = optional(sprites, SLUG + "-burn");
        drawn = all;
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>();
    }

    /** Section frame of a living section: intact, or below 50 % with its lights flickering (one beat in three dark). */
    static int frame(double hpShare, long tick) {
        if (hpShare >= 0.5) {
            return INTACT;
        }
        return (long) Math.floor(tick * FLICKER_HZ / SimStep.PER_SECOND) % 3 == 0 ? FLICKER : DAMAGED;
    }

    /**
     * The break-up's drift of section {@code k} of {@code count} {@code seconds} after the last section was lost: px
     * (x, y up) from where it sat; down and apart, faster and faster.
     */
    static double[] drift(int k, int count, double seconds) {
        double spread = k - (count - 1) / 2.0;
        return new double[] {spread * 9 * seconds, -(20 + 6 * k) * seconds - 24 * seconds * seconds};
    }

    /** The tug on the ground layer, its sections, vents, glow and flares (the batch draws normally and is left so). */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        if (!drawn || !sortie.hullEscort()) {
            return;
        }
        double x = sortie.hullX(alpha);
        double y = sortie.hullY(alpha);
        long tick = sortie.tick();
        int count = frames.size();
        int lastLost = Integer.MAX_VALUE;
        boolean allLost = true;
        for (int k = 0; k < count; k++) {
            Ally section = sortie.ally(k);
            if (section.lost()) {
                lastLost = Math.min(lastLost, section.ticksSinceLost());
            } else {
                allLost = false;
            }
        }
        if (allLost) {
            drawBreakUp(batch, x, y, lastLost, alpha);
            return;
        }
        Ally.State state = sortie.ally(0).state();
        if (state == Ally.State.HOME) {
            return;
        }
        // The engines first, the bow on top (the hull order's reverse).
        for (int k = count - 1; k >= 0; k--) {
            Ally section = sortie.ally(k);
            AtlasRegion region = frames.get(k).get(section.lost() ? LOST : frame(section.hpShare(), tick));
            float cx = (float) Math.round(PlayFrame.X0 + x + offset[k][0]);
            float cy = (float) Math.round(y + offset[k][1]);
            if (!section.lost() && section.ticksSinceHit() < HIT_FLASH_TICKS) {
                flash.draw(batch, region, cx, cy, Color.WHITE, whiteFlash);
            } else {
                PlayFrame.drawCentred(batch, region, x + offset[k][0], y + offset[k][1]);
            }
        }
        drawVents(batch, sortie, x, y, tick);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (engines >= 0 && !sortie.ally(engines).lost()) {
            boolean climbing = state == Ally.State.CLIMBING;
            drawBells(
                    batch, climbing && flare.size > 0 ? flare : glow, climbing ? FLARE_INSET : GLOW_INSET, x, y, tick);
        }
        for (int k = 0; k < count && burn.size > 0; k++) {
            Ally section = sortie.ally(k);
            int step = section.lost() ? section.ticksSinceLost() / BURN_FRAME_TICKS : -1;
            if (step >= 0 && step < burn.size) {
                PlayFrame.drawCentred(batch, burn.get(step), x + offset[k][0], y + offset[k][1]);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The plumes at a damaged or lost section's smoke points, each trailing down from its point. */
    private void drawVents(SpriteBatch batch, Sortie sortie, double x, double y, long tick) {
        if (vent.size == 0) {
            return;
        }
        for (int k = 0; k < frames.size(); k++) {
            Ally section = sortie.ally(k);
            if (!section.lost() && section.hpShare() >= 0.5) {
                continue;
            }
            for (int j = 0; j < smoke[k].length; j++) {
                AtlasRegion plume =
                        vent.get((int) Math.floorMod(tick / VENT_FRAME_TICKS + 3L * j + k, (long) vent.size));
                double drop = plume.getRegionHeight() / 2.0 - 4;
                PlayFrame.drawCentred(batch, plume, x + smoke[k][j][0], y + smoke[k][j][1] - drop);
            }
        }
    }

    /** Additive: one bell's glow (or the flare) at each drive bell, the small ones scaled, pointing down. */
    private void drawBells(SpriteBatch batch, Array<AtlasRegion> frames, double inset, double x, double y, long tick) {
        if (frames.size == 0) {
            return;
        }
        AtlasRegion region = frames.get((int) Math.floorMod(tick / GLOW_FRAME_TICKS, (long) frames.size));
        for (double[] bell : bells) {
            double scale = bell[2];
            float w = (float) (region.getRegionWidth() * scale);
            float h = (float) (region.getRegionHeight() * scale);
            double cx = x + bell[0];
            double cy = y + bell[1] - h / 2.0 + inset * scale;
            batch.draw(region, Math.round(PlayFrame.X0 + cx - w / 2), Math.round(cy - h / 2), w, h);
        }
    }

    /** Every section lost: the dark pieces drifting down and apart, darkening, gone after {@link #BREAK_SECONDS}. */
    private void drawBreakUp(SpriteBatch batch, double x, double y, int ticksSince, float alpha) {
        double seconds = (ticksSince + alpha) * SimStep.SECONDS;
        if (seconds >= BREAK_SECONDS) {
            return;
        }
        double share = seconds / BREAK_SECONDS;
        float shade = (float) (1 - 0.6 * share);
        float fade = (float) Math.min(1, 4 * (1 - share));
        batch.setColor(shade, shade, shade, fade);
        for (int k = frames.size() - 1; k >= 0; k--) {
            double[] drift = drift(k, frames.size(), seconds);
            PlayFrame.drawCentred(
                    batch, frames.get(k).get(LOST), x + offset[k][0] + drift[0], y + offset[k][1] + drift[1]);
        }
        batch.setColor(Color.WHITE);
    }
}
