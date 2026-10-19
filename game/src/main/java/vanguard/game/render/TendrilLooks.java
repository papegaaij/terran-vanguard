package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Beam;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Links;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Tendril;

/**
 * M7 part C: the Tendril Anchors and their tendrils (design/enemies/ground/tendril-anchor; tools/art/
 * canyon_units.py). An anchor faces its partner across the canyon (the side's partner until it has one) and
 * pulses 0-1-2-1 at 6 fps. The tendril is stretched between the anchors' cone tips as a strip of quads swaying
 * slowly (at most {@value #SWAY} px at the middle, none at the anchors), its 64 px image tiling from the left
 * anchor: a flickering thread while dormant ({@code tendril-dormant}), its flare to full width over 0.15 s
 * ({@code tendril-flare}), then the standing rope ({@code tendril}, 12 fps), reddening as a beam burns it, the
 * white-hot spot where the beam's column crosses it ({@code tendril-burn}, additive). Broken it withers over
 * 0.4 s ({@code tendril-wither}): burnt through it parts in the middle and both halves recoil, an anchor's
 * death snaps it back into the survivor; its frayed ends ({@code tendril-end}) at the cut.
 */
final class TendrilLooks {
    /** An anchor's centre to its cone's tip, along its heading, px (the 40 px sprite's cone). */
    static final double TIP = 14;
    /** The sway at the middle of a standing tendril, px (the concept: at most 5). */
    static final double SWAY = 5;
    /** A sway's period, s (slow). */
    static final double SWAY_SECONDS = 2.6;
    /** The strip image's length, px: it tiles every 64 px. */
    static final int TILE = 64;
    /** The quads a whole tendril is cut into for its sway (and at every tile border). */
    static final int PIECES = 12;
    /** The anchors' pulse: frames 0-1-2-1 at 6 fps. */
    private static final int[] PULSE = {0, 1, 2, 1};

    private static final int PULSE_TICKS = SimStep.PER_SECOND / 6;
    /** The standing rope runs at 12 fps. */
    private static final int ROPE_TICKS = SimStep.PER_SECOND / 12;
    /** The dormant thread's flicker, steps a frame. */
    private static final int FLICKER_TICKS = 3;
    /** The burn spot's flicker, steps a frame. */
    private static final int BURN_TICKS = 2;
    /** How far the halves of a burnt tendril recoil over its wither (of each half). */
    private static final double RECOIL = 0.6;
    /** How much of a snapped tendril is left at the survivor once it has snapped back. */
    private static final double STUB = 0.15;
    /** Its snap back takes this share of the wither. */
    private static final double SNAP = 0.4;

    private final boolean[] anchors;
    private final Array<AtlasRegion> rope;
    private final Array<AtlasRegion> dormant;
    private final Array<AtlasRegion> flare;
    private final Array<AtlasRegion> wither;
    private final Array<AtlasRegion> end;
    private final Array<AtlasRegion> burn;
    private final float[] quad = new float[20];

    /** The level's enemy kinds ({@link Sortie#enemyKinds()}): those with a {@code link} are anchors. */
    TendrilLooks(Sprites sprites, List<EnemySpec> kinds) {
        anchors = new boolean[kinds.size()];
        boolean any = false;
        for (int k = 0; k < kinds.size(); k++) {
            anchors[k] = kinds.get(k).link().isPresent();
            any |= anchors[k];
        }
        boolean art = any && sprites.has("tendril");
        rope = art ? sprites.frames("tendril") : null;
        dormant = art ? sprites.frames("tendril-dormant") : null;
        flare = art ? sprites.frames("tendril-flare") : null;
        wither = art ? sprites.frames("tendril-wither") : null;
        end = art ? sprites.frames("tendril-end") : null;
        burn = art ? sprites.frames("tendril-burn") : null;
    }

    /** Whether {@code kind} is a Tendril Anchor's. */
    boolean anchor(int kind) {
        return kind >= 0 && kind < anchors.length && anchors[kind];
    }

    /** An anchor's frame: facing its partner, at its pulse; null for any other unit. */
    AtlasRegion anchorFrame(Sortie sortie, Enemy enemy, EnemyLooks look) {
        if (!anchor(enemy.kind())) {
            return null;
        }
        int pulse = PULSE[(int) Math.floorMod(sortie.tick() / PULSE_TICKS + enemy.serial(), (long) PULSE.length)];
        return look.frame(partnerFacing(sortie.links(), enemy.renderX(1), enemy.renderY(1)), pulse);
    }

    /**
     * The facing (radians clockwise from straight down) of an anchor at (x, y) toward its partner: the other
     * end of the tendril it holds, or across the canyon from its side while it holds none.
     */
    static double partnerFacing(Links links, double x, double y) {
        if (links != null) {
            for (int i = 0; i < links.size(); i++) {
                Tendril tendril = links.get(i);
                double dx;
                double dy;
                if (near(tendril.ax(), tendril.ay(), x, y)) {
                    dx = tendril.bx() - x;
                    dy = tendril.by() - y;
                } else if (near(tendril.bx(), tendril.by(), x, y)) {
                    dx = tendril.ax() - x;
                    dy = tendril.ay() - y;
                } else {
                    continue;
                }
                if (Math.abs(dx) + Math.abs(dy) > 1) {
                    return facing(dx, dy);
                }
            }
        }
        return facing(x < PlayField.WIDTH / 2.0 ? 1 : -1, 0);
    }

    /** The facing of the direction (dx, dy), y up: 0 straight down, a quarter turn facing left. */
    static double facing(double dx, double dy) {
        double angle = Math.atan2(-dx, -dy);
        return angle < 0 ? angle + 2 * Math.PI : angle;
    }

    private static boolean near(double ax, double ay, double x, double y) {
        return Math.abs(ax - x) < 1 && Math.abs(ay - y) < 1;
    }

    /** The tendrils, over the ground units and under the air units. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        Links links = sortie.links();
        if (links == null || rope == null) {
            return;
        }
        // Their ends scroll with the ground: between two steps they are drawn where the ground is.
        double lag = SimStep.SECONDS * (1 - alpha) * sortie.groundSpeed();
        long tick = sortie.tick();
        for (int i = 0; i < links.size(); i++) {
            Tendril tendril = links.get(i);
            if (tendril.standing() || tendril.broken()) {
                draw(batch, sortie, tendril, lag, tick, alpha);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void draw(SpriteBatch batch, Sortie sortie, Tendril tendril, double lag, long tick, float alpha) {
        double ax = tendril.ax();
        double ay = tendril.ay() + lag;
        double dx = tendril.bx() - ax;
        double dy = tendril.by() + lag - ay;
        double length = Math.hypot(dx, dy);
        if (length < 2 * TIP + 4) {
            return;
        }
        double ux = dx / length;
        double uy = dy / length;
        double ox = ax + ux * TIP;
        double oy = ay + uy * TIP;
        double span = length - 2 * TIP;
        double sway = SWAY * Math.sin(2 * Math.PI * tick * SimStep.SECONDS / SWAY_SECONDS + tendril.serial() * 1.7);
        Strip strip = new Strip(ox, oy, ux, uy, span, sway);
        if (!tendril.broken()) {
            TextureRegion frame;
            float opacity = 1;
            if (tendril.dormant()) {
                int f = (int) Math.floorMod(tick / FLICKER_TICKS + tendril.serial(), 2L);
                frame = dormant.get(f % dormant.size);
                opacity = f == 0 ? 0.7f : 1;
                strip = strip.calm();
            } else if (tendril.flare() < 1) {
                frame = flare.get(Math.min(flare.size - 1, (int) (tendril.flare() * flare.size)));
            } else {
                frame = rope.get((int) (tick / ROPE_TICKS % rope.size));
            }
            // A beam burning it reddens and darkens it as its HP goes.
            float burnt = (float) (1 - tendril.hpShare());
            float colour = Color.toFloatBits(1f, 1 - 0.35f * burnt, 1 - 0.45f * burnt, opacity);
            strip(batch, frame, strip, 0, span, colour);
            if (tendril.burning()) {
                drawBurn(batch, sortie, strip, tick, alpha);
            }
            return;
        }
        TextureRegion frame = wither.get(Math.min(wither.size - 1, (int) (tendril.wither() * wither.size)));
        float colour = Color.toFloatBits(1f, 1f, 1f, 1f);
        double w = tendril.wither();
        if (tendril.burnt()) {
            // Parted in the middle: both halves recoil toward their anchors.
            double keep = span / 2 * (1 - RECOIL * ease(w));
            strip(batch, frame, strip, 0, keep, colour);
            strip(batch, frame, strip, span - keep, span, colour);
            drawEnd(batch, strip, keep, 1, tick);
            drawEnd(batch, strip, span - keep, -1, tick);
        } else if (tendril.aAlive() != tendril.bAlive()) {
            // An anchor died: it snaps back into the survivor.
            double keep = span * (1 - (1 - STUB) * ease(Math.min(1, w / SNAP)));
            if (tendril.aAlive()) {
                strip(batch, frame, strip, 0, keep, colour);
                drawEnd(batch, strip, keep, 1, tick);
            } else {
                strip(batch, frame, strip, span - keep, span, colour);
                drawEnd(batch, strip, span - keep, -1, tick);
            }
        } else {
            strip(batch, frame, strip.calm(), 0, span, colour);
        }
    }

    private static double ease(double t) {
        double u = Math.clamp(t, 0, 1);
        return 1 - (1 - u) * (1 - u);
    }

    /**
     * A tendril's line: from its left tip (ox, oy) along the unit vector (ux, uy) over {@code span} px, bowed
     * sideways by {@code sway} px at its middle.
     */
    record Strip(double ox, double oy, double ux, double uy, double span, double sway) {
        /** The same line without the sway (the dormant thread, a rope withering where it hung). */
        Strip calm() {
            return new Strip(ox, oy, ux, uy, span, sway * 0.3);
        }

        double x(double d) {
            return ox + ux * d - uy * bow(d);
        }

        double y(double d) {
            return oy + uy * d + ux * bow(d);
        }

        private double bow(double d) {
            return sway * Math.sin(Math.PI * Math.clamp(d / span, 0, 1));
        }
    }

    /** The strip image along {@code strip} from {@code from} to {@code to} px, tiling every {@value #TILE} px. */
    private void strip(SpriteBatch batch, TextureRegion frame, Strip strip, double from, double to, float colour) {
        if (to - from < 0.5) {
            return;
        }
        double piece = strip.span() / PIECES;
        double half = frame.getRegionHeight() / 2.0;
        double nx = -strip.uy() * half;
        double ny = strip.ux() * half;
        float u = frame.getU();
        float du = frame.getU2() - u;
        double d0 = from;
        while (d0 < to - 1e-6) {
            double tileStart = Math.floor(d0 / TILE) * TILE;
            double d1 = Math.min(to, Math.min(tileStart + TILE, d0 + piece));
            float u0 = u + du * (float) ((d0 - tileStart) / TILE);
            float u1 = u + du * (float) ((d1 - tileStart) / TILE);
            double x0 = strip.x(d0);
            double y0 = strip.y(d0);
            double x1 = strip.x(d1);
            double y1 = strip.y(d1);
            vertex(0, x0 - nx, y0 - ny, colour, u0, frame.getV2());
            vertex(1, x0 + nx, y0 + ny, colour, u0, frame.getV());
            vertex(2, x1 + nx, y1 + ny, colour, u1, frame.getV());
            vertex(3, x1 - nx, y1 - ny, colour, u1, frame.getV2());
            batch.draw(frame.getTexture(), quad, 0, quad.length);
            d0 = d1;
        }
    }

    private void vertex(int i, double x, double y, float colour, float u, float v) {
        int o = i * 5;
        quad[o] = PixelScreen.PLAY_FIELD_X + (float) x;
        quad[o + 1] = (float) y;
        quad[o + 2] = colour;
        quad[o + 3] = u;
        quad[o + 4] = v;
    }

    /** A frayed end at {@code d} along the strip, pointing along it ({@code direction} 1) or back (-1). */
    private void drawEnd(SpriteBatch batch, Strip strip, double d, int direction, long tick) {
        AtlasRegion frame = end.get((int) (tick / ROPE_TICKS % end.size));
        float h = frame.getRegionHeight();
        float rotation = (float) Math.toDegrees(Math.atan2(strip.uy() * direction, strip.ux() * direction));
        batch.draw(
                frame,
                PixelScreen.PLAY_FIELD_X + (float) strip.x(d),
                (float) strip.y(d) - h / 2,
                0,
                h / 2,
                frame.getRegionWidth(),
                h,
                1,
                1,
                rotation);
    }

    /** The white-hot spot where a running beam's column crosses the tendril. */
    private void drawBurn(SpriteBatch batch, Sortie sortie, Strip strip, long tick, float alpha) {
        double minX = Math.min(strip.ox(), strip.ox() + strip.ux() * strip.span());
        double maxX = Math.max(strip.ox(), strip.ox() + strip.ux() * strip.span());
        double x = Double.NaN;
        for (int m = 0; m < sortie.armament().size(); m++) {
            Beam beam = sortie.beam(m);
            if (beam != null && beam.on() && beam.x() >= minX && beam.x() <= maxX) {
                x = beam.x();
                break;
            }
        }
        if (Double.isNaN(x)) {
            x = Math.clamp(sortie.ship().renderX(alpha), minX, maxX);
        }
        double d = Math.abs(strip.ux()) < 1e-6 ? strip.span() / 2 : (x - strip.ox()) / strip.ux();
        AtlasRegion frame = burn.get((int) (tick / BURN_TICKS % burn.size));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(Color.WHITE);
        batch.draw(
                frame,
                PixelScreen.PLAY_FIELD_X + (float) strip.x(d) - frame.getRegionWidth() / 2f,
                (float) strip.y(d) - frame.getRegionHeight() / 2f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
