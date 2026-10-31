package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Domes;
import vanguard.sim.Enemy;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies/ground/shield-pylon, The dome): the Shield Pylons' domes on the ground layer, over the
 * ground units, all additive (tools/art/act6_gear.py {@code dome}): each standing dome ({@code shield-pylon-dome_0..11},
 * the gold hex lattice at about 20 % with its brighter rim, a 4 s shimmer loop of {@value #DOME_FRAME_TICKS} steps a
 * frame, scaled by its radius / {@value #ART_RADIUS}) with the thin gold feed beam from its pylon to its rim ({@code
 * shield-pylon-feed}, stretched); a dropped dome flickers back over its back time ({@link #flickerOn}); every covered
 * unit wears the faint gold hex sheen ({@code shield-pylon-sheen_0..3}, {@value #SHEEN_FRAME_TICKS} steps a frame,
 * scaled to it); a dome collapsing for good shrinks to its pylon's crown ({@code shield-pylon-dome-collapse_0..5},
 * {@value #COLLAPSE_FRAME_TICKS} steps a frame, its centre moving from the dome's to the crown's). A hit spent on the
 * dome sparks there ({@link #hit}, {@code shield-pylon-dome-hit_0..5}, the screen's effects). The frames are held in
 * {@code build/art-held/} until the pylon's data claims them (part G); without them each look falls back to {@link
 * SoftGlow}'s disc and ring in the same gold, smooth and drawn once.
 */
final class PylonDomeLooks {
    static final String ROOT = "shield-pylon";
    static final String DOME = ROOT + "-dome";
    static final String HIT = ROOT + "-dome-hit";
    static final String COLLAPSE = ROOT + "-dome-collapse";
    static final String FEED = ROOT + "-feed";
    static final String SHEEN = ROOT + "-sheen";
    /** The dome art's radius, px: a dome of another radius is drawn scaled by radius / this. */
    static final double ART_RADIUS = 110;

    static final int DOME_FRAME_TICKS = 20;
    static final int HIT_FRAME_TICKS = 2;
    static final int COLLAPSE_FRAME_TICKS = 4;
    static final int SHEEN_FRAME_TICKS = 15;
    /** The collapse's length without its art: the art's 6 frames. */
    static final int COLLAPSE_FRAMES = 6;
    /** The sheen art's side, px: drawn scaled to the covered unit's larger hit-box side / this. */
    static final double SHEEN_SIZE = 48;
    /** A flicker's on and off spells while the dome comes back, steps. */
    static final int FLICKER_TICKS = 3;

    static final Color GOLD = Color.valueOf("FFCE5A");
    /** The fallback's dome fill and rim opacity (the art's ≈ 20 % lattice). */
    static final float FILL = 0.14f;

    static final float RIM = 0.55f;
    /** The collapses running at once. */
    private static final int COLLAPSES = 8;

    private final Array<AtlasRegion> dome;
    private final Array<AtlasRegion> hit;
    private final Array<AtlasRegion> collapse;
    private final AtlasRegion feed;
    private final Array<AtlasRegion> sheen;
    private final TextureRegion pixel;
    private final SoftGlow glow;
    /** Per dome slot: its pylon's crown and the dome's centre last seen (a collapse runs between them). */
    private final double[] crownX = new double[16];

    private final double[] crownY = new double[16];
    private final double[] centreX = new double[16];
    private final double[] centreY = new double[16];
    /** The collapses running: their start step (-1 free), the ground's scroll then, from where to where. */
    private final long[] collapseTick = new long[COLLAPSES];

    private final double[] collapseScroll = new double[COLLAPSES];
    private final double[] fromX = new double[COLLAPSES];
    private final double[] fromY = new double[COLLAPSES];
    private final double[] toX = new double[COLLAPSES];
    private final double[] toY = new double[COLLAPSES];
    private final double[] collapseRadius = new double[COLLAPSES];
    private int nextCollapse;

    PylonDomeLooks(Sprites sprites, SoftGlow glow) {
        this.glow = glow;
        pixel = sprites.pixel;
        dome = sprites.has(DOME) ? sprites.frames(DOME) : null;
        hit = sprites.has(HIT) ? sprites.frames(HIT) : null;
        collapse = sprites.has(COLLAPSE) ? sprites.frames(COLLAPSE) : null;
        feed = sprites.has(FEED) ? sprites.region(FEED) : null;
        sheen = sprites.has(SHEEN) ? sprites.frames(SHEEN) : null;
        restart();
    }

    /** The dome hit's spark frames for the screen's effects ({@link #HIT_FRAME_TICKS} steps a frame); null without art. */
    Array<AtlasRegion> hit() {
        return hit;
    }

    /** The level restarts: no collapse runs. */
    void restart() {
        Arrays.fill(collapseTick, -1);
        nextCollapse = 0;
    }

    /** The dome's shimmer frame at step {@code tick} for slot {@code slot} (the slots' loops staggered). */
    static int domeFrame(long tick, int slot, int frames) {
        return (int) Math.floorMod(tick / DOME_FRAME_TICKS + 3L * slot, (long) frames);
    }

    /** The sheen's frame at step {@code tick} for a unit (its serial staggers the pulses). */
    static int sheenFrame(long tick, int serial, int frames) {
        return (int) Math.floorMod(tick / SHEEN_FRAME_TICKS + serial, (long) frames);
    }

    /**
     * Whether a dome coming back with {@code left} of its {@code total} back steps shows in this step: it flickers,
     * the off spells shortening as it comes back (on, off, on, ..., on for good in its last third).
     */
    static boolean flickerOn(int left, int total) {
        if (left <= 0) {
            return true;
        }
        if (total <= 0 || left <= total / 3) {
            return true;
        }
        int done = total - left;
        return done / FLICKER_TICKS % 2 == 0;
    }

    /** The collapse's frame {@code age} steps after it started; -1 once it has played out. */
    static int collapseFrame(long age, int frames) {
        if (age < 0) {
            return -1;
        }
        long frame = age / COLLAPSE_FRAME_TICKS;
        return frame >= frames ? -1 : (int) frame;
    }

    /** After each step: where every tracked dome and its pylon's crown are (a collapse runs between them). */
    void stepped(Sortie sortie) {
        Domes domes = sortie.domes();
        if (domes == null) {
            return;
        }
        for (int slot = 0; slot < Math.min(domes.capacity(), crownX.length); slot++) {
            if (!domes.tracked(slot)) {
                continue;
            }
            Enemy pylon = bySerial(sortie, domes.pylon(slot));
            centreX[slot] = domes.x(slot);
            centreY[slot] = domes.y(slot);
            if (pylon != null) {
                crownX[slot] = pylon.renderX(1);
                crownY[slot] = pylon.renderY(1);
            }
        }
    }

    /**
     * A dome collapsed for good ({@link vanguard.sim.SimEvents.Type#DOME_COLLAPSED} at its centre (x, y)): its ring
     * shrinks to its pylon's crown, on the ground.
     */
    void collapse(Sortie sortie, double x, double y) {
        Domes domes = sortie.domes();
        int found = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int slot = 0; domes != null && slot < Math.min(domes.capacity(), crownX.length); slot++) {
            double d = Math.hypot(centreX[slot] - x, centreY[slot] - y);
            if (d < best) {
                best = d;
                found = slot;
            }
        }
        boolean known = found >= 0 && best <= 2;
        collapseTick[nextCollapse] = sortie.tick();
        collapseScroll[nextCollapse] = sortie.groundScroll();
        fromX[nextCollapse] = x;
        fromY[nextCollapse] = y;
        toX[nextCollapse] = known ? crownX[found] : x;
        toY[nextCollapse] = known ? crownY[found] : y;
        collapseRadius[nextCollapse] = known ? domes.radius(found) : ART_RADIUS;
        nextCollapse = (nextCollapse + 1) % COLLAPSES;
    }

    /** The domes, their feeds, the covered units' sheen and the collapses, additive, over the ground units. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        Domes domes = sortie.domes();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (domes != null) {
            for (int slot = 0; slot < domes.capacity(); slot++) {
                if (domes.tracked(slot)) {
                    drawDome(batch, sortie, domes, slot, alpha);
                }
            }
            drawSheen(batch, sortie, alpha);
        }
        drawCollapses(batch, sortie);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }

    private void drawDome(SpriteBatch batch, Sortie sortie, Domes domes, int slot, float alpha) {
        Enemy pylon = bySerial(sortie, domes.pylon(slot));
        if (pylon == null) {
            return;
        }
        if (!domes.standing(slot)) {
            int left = domes.backTicks(slot);
            int total = pylon.spec().dome().isPresent()
                    ? SimStep.ticks(pylon.spec().dome().get().backSeconds())
                    : 0;
            if (left <= 0 || !flickerOn(left, total)) {
                return;
            }
        }
        // The centre moves with its pylon between the steps.
        double px = pylon.renderX(alpha);
        double py = pylon.renderY(alpha);
        double cx = px + domes.x(slot) - pylon.renderX(1);
        double cy = py + domes.y(slot) - pylon.renderY(1);
        double radius = domes.radius(slot);
        batch.setColor(Color.WHITE);
        if (dome != null) {
            AtlasRegion frame = dome.get(domeFrame(sortie.tick(), slot, dome.size));
            PlayFrame.drawScaled(batch, frame, cx, cy, (float) (radius / ART_RADIUS));
        } else {
            batch.setColor(GOLD.r, GOLD.g, GOLD.b, FILL);
            glow.disc(batch, cx, cy, 2.2 * radius);
            batch.setColor(GOLD.r, GOLD.g, GOLD.b, RIM);
            glow.ring(batch, cx, cy, radius);
        }
        drawFeed(batch, px, py, cx, cy, radius);
    }

    /** The feed beam from the crown at (px, py) to the rim of the dome round (cx, cy). */
    private void drawFeed(SpriteBatch batch, double px, double py, double cx, double cy, double radius) {
        double dx = cx - px;
        double dy = cy - py;
        double distance = Math.hypot(dx, dy);
        float length = (float) (distance - radius);
        if (length <= 0) {
            return;
        }
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        float x = (float) (PixelScreen.PLAY_FIELD_X + px);
        if (feed != null) {
            float h = feed.getRegionHeight();
            batch.setColor(Color.WHITE);
            batch.draw(feed, x, (float) py - h / 2, 0, h / 2, length, h, 1, 1, angle);
        } else {
            batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.8f);
            batch.draw(pixel, x, (float) py - 1, 0, 1, length, 2, 1, 1, angle);
        }
    }

    private void drawSheen(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!enemy.covered()) {
                continue;
            }
            double size = Math.max(enemy.hitbox().width(), enemy.hitbox().height());
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (sheen != null) {
                batch.setColor(Color.WHITE);
                PlayFrame.drawScaled(
                        batch, sheen.get(sheenFrame(sortie.tick(), enemy.serial(), sheen.size)), x, y, (float)
                                (size / SHEEN_SIZE));
            } else {
                batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.3f);
                glow.disc(batch, x, y, size * 1.2);
            }
        }
    }

    private void drawCollapses(SpriteBatch batch, Sortie sortie) {
        int frames = collapse != null ? collapse.size : COLLAPSE_FRAMES;
        for (int c = 0; c < COLLAPSES; c++) {
            if (collapseTick[c] < 0) {
                continue;
            }
            long age = sortie.tick() - collapseTick[c];
            int frame = collapseFrame(age, frames);
            if (frame < 0) {
                collapseTick[c] = -1;
                continue;
            }
            double share = Math.min(1, (age + 1.0) / (frames * COLLAPSE_FRAME_TICKS));
            double scrolled = sortie.groundScroll() - collapseScroll[c];
            double x = fromX[c] + (toX[c] - fromX[c]) * share;
            double y = fromY[c] + (toY[c] - fromY[c]) * share - scrolled;
            batch.setColor(Color.WHITE);
            if (collapse != null) {
                PlayFrame.drawCentred(batch, collapse.get(frame), x, y);
            } else {
                batch.setColor(GOLD.r, GOLD.g, GOLD.b, RIM * (float) (1 - share * 0.5));
                glow.ring(batch, x, y, Math.max(1, collapseRadius[c] * (1 - share)));
            }
        }
    }

    private static Enemy bySerial(Sortie sortie, int serial) {
        if (serial < 0) {
            return null;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.serial() == serial) {
                return enemy;
            }
        }
        return null;
    }
}
