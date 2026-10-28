package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.Armament;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * M9 part A: the Tesla Coil Pod's arcs as the player sees them (design/player/weapons/tesla-coil-pod; tools/art/act5_gear.py
 * {@code arc}, whose docstring gives the sprites; round 61 a "blue-white" written until the pick), all additive: each
 * {@link SimEvents.Type#ARC} draws a jagged arc from the pod's terminal to the struck point for {@value #ARC_TICKS}
 * steps ({@code tesla-coil-pod-arc_0..2}, a 128 x 24 strip with the pod at its left end, stretched along x to the
 * distance and turned to the direction; the three shapes in turn, one an arc), and the terminal's light at the pod's tip
 * while it arcs ({@code tesla-coil-pod-glow_0..1}, alternate frames each step). The spark burst at the target is the
 * weapon's impact ({@link WeaponLooks}: the {@code arc} family's {@code tesla-coil-pod-spark}). The arc follows the
 * ship's pod as it moves; its end stays where it struck.
 */
final class ArcLooks {
    /** An arc shows this many steps (0.08 s, the art's 5 steps). */
    static final int ARC_TICKS = 5;
    /** The strip's length in the art, px; its pod end is at its left edge, centred on its height. */
    static final float ART_LENGTH = 128;

    private final Array<AtlasRegion> arc;
    private final Array<AtlasRegion> glow;
    /** Per mount: the step its last arc struck at (far in the past for none), where, and the shape it took. */
    private final long[] struck;

    private final double[] targetX;
    private final double[] targetY;
    private final int[] shape;
    /** Per mount: how many arcs it struck (the shapes in turn). */
    private final int[] arcs;

    private ArcLooks(Array<AtlasRegion> arc, Array<AtlasRegion> glow, int mounts) {
        this.arc = arc;
        this.glow = glow;
        struck = new long[mounts];
        targetX = new double[mounts];
        targetY = new double[mounts];
        shape = new int[mounts];
        arcs = new int[mounts];
        restart();
    }

    /** The looks for an armament with a Tesla Coil Pod fitted (an {@code ARC} mount); null without one or its art. */
    static ArcLooks of(Sprites sprites, Armament armament) {
        String slug = null;
        for (int m = 0; m < armament.size() && slug == null; m++) {
            Armament.Mount mount = armament.mount(m);
            if (mount.fires() && mount.weapon().delivery() == WeaponSpec.Delivery.ARC) {
                slug = mount.weapon().slug();
            }
        }
        if (slug == null || !sprites.has(slug + "-arc")) {
            return null;
        }
        return new ArcLooks(
                sprites.frames(slug + "-arc"),
                sprites.has(slug + "-glow") ? sprites.frames(slug + "-glow") : new Array<>(0),
                armament.size());
    }

    /** The level restarts: no arc shows. */
    void restart() {
        java.util.Arrays.fill(struck, Long.MIN_VALUE / 2);
        java.util.Arrays.fill(arcs, 0);
    }

    /** After each simulation step: an arc that struck ({@link SimEvents.Type#ARC}, value its mount) shows. */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) != SimEvents.Type.ARC) {
                continue;
            }
            int m = events.value(i);
            if (m < 0 || m >= struck.length) {
                continue;
            }
            struck[m] = sortie.tick();
            targetX[m] = events.x(i);
            targetY[m] = events.y(i);
            shape[m] = arcs[m]++ % Math.max(1, arc.size);
        }
    }

    /** Whether mount {@code m}'s arc shows at step {@code tick}. */
    boolean showing(int m, long tick) {
        long age = tick - struck[m];
        return age >= 0 && age < ARC_TICKS;
    }

    /** The shape (a frame of {@code -arc}) the {@code n}th arc of a pod takes: the three in turn. */
    static int shapeOf(int n, int shapes) {
        return Math.floorMod(n, Math.max(1, shapes));
    }

    /** The arc's turn from the pod at (x0, y0) to (x1, y1), degrees anticlockwise from the +x axis (y up). */
    static float degrees(double x0, double y0, double x1, double y1) {
        return (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));
    }

    /**
     * With the glowing shots, in their additive blend (the caller's): every showing arc from its pod's terminal (the ship at {@code alpha} and the
     * mount's muzzle) to the point it struck, and the terminal's light.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        double shipX = sortie.ship().renderX(alpha);
        double shipY = sortie.ship().renderY(alpha);
        for (int m = 0; m < struck.length; m++) {
            if (!showing(m, tick)) {
                continue;
            }
            WeaponSpec.Muzzle muzzle =
                    sortie.armament().mount(m).weapon().muzzles().getFirst();
            double x0 = shipX + muzzle.dx();
            double y0 = shipY + muzzle.dy();
            drawArc(batch, arc.get(shape[m]), x0, y0, targetX[m], targetY[m]);
            if (!glow.isEmpty()) {
                AtlasRegion light = glow.get((int) (tick % glow.size));
                batch.draw(light, (float) (PixelScreen.PLAY_FIELD_X + x0 - light.getRegionWidth() / 2f), (float)
                        (y0 - light.getRegionHeight() / 2f));
            }
        }
    }

    /** The strip stretched from (x0, y0) to (x1, y1) in the play field, its height unscaled. */
    static void drawArc(SpriteBatch batch, AtlasRegion strip, double x0, double y0, double x1, double y1) {
        double length = Math.hypot(x1 - x0, y1 - y0);
        if (length < 1) {
            return;
        }
        float height = strip.getRegionHeight();
        batch.draw(
                strip,
                (float) (PixelScreen.PLAY_FIELD_X + x0),
                (float) (y0 - height / 2),
                0,
                height / 2,
                (float) length,
                height,
                1,
                1,
                degrees(x0, y0, x1, y1));
    }
}
