package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Armament;
import vanguard.sim.ShipSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveFire;
import vanguard.sim.WeaponSpec;

/**
 * M11 part A, step A3b: the Choir Resonator as the player sees it (design/player/weapons/choir-resonator, round 85 A.1 a
 * "the bell"; tools/art/act7_gear.py {@code resonator}, whose docstring gives the sprites). Each wave front in flight
 * ({@link WaveFire}) is the frame of the {@code -wave} set whose width is nearest the front's ({@value #FRAMES} widths
 * from {@value #NARROWEST} to {@value #WIDEST} px, 15 % a step), scaled in x to it exactly, its apex (the front's middle,
 * {@value #APEX} px below the frame's top) on the front's point: first the thin dark {@code -wave-rim} in the normal blend
 * under the shots, then the light additive with the glowing shots, both at the brightness {@link #brightness} (up over
 * the first 12 px, out over the last fifth of the reach, ×falloff for each hull passed). In a level with rock a slice
 * stopped at rock is gone from the arc ({@link WaveFire#sliceLive}: the frame drawn in {@link WaveFire#SLICES} strips).
 * On the hull while the gun is fitted the Warden's eye in its collar ({@code -mount}, per banking frame, centred
 * {@value #MOUNT_BELOW} px below the ship's {@code front} pivot), and on the nose its crystal's light ({@code -glow}:
 * frame 0 the idle shimmer, frames 1–3 the bloom after each wave, {@value #GLOW_FRAME_TICKS} steps a frame). The ring on
 * each hull a wave passes is the weapon's impact ({@link WeaponLooks}, {@code -ring}).
 */
final class ResonatorLooks {
    static final int FRAMES = 16;
    static final double NARROWEST = 32;
    static final double WIDEST = 260;
    /** The arc's apex below the frame's top, px (the art's pad). */
    static final int APEX = 28;
    /** The ship's {@code front} pivot above its centre (pivots/ship.json (24, 3) on the 48 px hull), px. */
    static final int NOSE = 21;
    /** The mount's centre below the nose pivot, px. */
    static final int MOUNT_BELOW = 4;

    static final int GLOW_FRAME_TICKS = 2;
    /** The brightness's fade-in over the first px of the flight, and its start. */
    static final double FADE_IN = 12;

    static final double FADE_IN_START = 0.25;
    /** The share of the reach after which a front fades out. */
    static final double FADE_OUT_FROM = 0.8;

    private final Array<AtlasRegion> light;
    private final Array<AtlasRegion> rim;
    private final Array<AtlasRegion> mount;
    private final Array<AtlasRegion> glow;
    /** Per mount: whether it fires waves, and the step its last wave left at. */
    private final boolean[] waves;

    private final long[] fired;
    private final TextureRegion strip = new TextureRegion();

    ResonatorLooks(
            Array<AtlasRegion> light,
            Array<AtlasRegion> rim,
            Array<AtlasRegion> mount,
            Array<AtlasRegion> glow,
            boolean[] waves) {
        this.light = light;
        this.rim = rim;
        this.mount = mount;
        this.glow = glow;
        this.waves = waves;
        fired = new long[waves.length];
        restart();
    }

    /** The looks for an armament with a Choir Resonator fitted (a {@code WAVE} mount); null without one or its art. */
    static ResonatorLooks of(Sprites sprites, Armament armament) {
        String slug = null;
        boolean[] waves = new boolean[armament.size()];
        for (int m = 0; m < armament.size(); m++) {
            Armament.Mount fitted = armament.mount(m);
            if (fitted.fires() && fitted.weapon().delivery() == WeaponSpec.Delivery.WAVE) {
                waves[m] = true;
                slug = fitted.weapon().slug();
            }
        }
        if (slug == null || !sprites.has(slug + "-wave") || !sprites.has(slug + "-wave-rim")) {
            return null;
        }
        return new ResonatorLooks(
                sprites.frames(slug + "-wave"),
                sprites.frames(slug + "-wave-rim"),
                sprites.has(slug + "-mount") ? sprites.frames(slug + "-mount") : null,
                sprites.has(slug + "-glow") ? sprites.frames(slug + "-glow") : null,
                waves);
    }

    /** The frame of the wave set for a front {@code width} px wide: the nearest width (they grow 15 % a step). */
    static int frame(double width) {
        double steps = (FRAMES - 1) * Math.log(width / NARROWEST) / Math.log(WIDEST / NARROWEST);
        return Math.clamp(Math.round(steps), 0, FRAMES - 1);
    }

    /** Frame {@code i}'s arc width, px. */
    static double frameWidth(int i) {
        return NARROWEST * Math.pow(WIDEST / NARROWEST, i / (double) (FRAMES - 1));
    }

    /**
     * A front's brightness {@code distance} px into its {@code reach}, having passed {@code met} hulls at {@code
     * falloff} each: up over the first {@value #FADE_IN} px, out over the last fifth (the art's {@code k}).
     */
    static double brightness(double distance, double reach, int met, double falloff) {
        double in = smooth(distance / FADE_IN + FADE_IN_START);
        double out = 1 - smooth((distance / reach - FADE_OUT_FROM) / (1 - FADE_OUT_FROM));
        return in * out * Math.pow(falloff, met);
    }

    private static double smooth(double x) {
        double t = Math.clamp(x, 0, 1);
        return t * t * (3 - 2 * t);
    }

    /** The level restarts: no wave just fired. */
    void restart() {
        Arrays.fill(fired, Long.MIN_VALUE / 2);
    }

    /** After each simulation step: the waves that left the nose (the crystal's bloom). */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.WAVE_FIRED && events.value(e) < fired.length) {
                fired[events.value(e)] = sortie.tick();
            }
        }
    }

    /** With the shots in the normal blend (the caller's): every front's dark rim. */
    void drawRims(SpriteBatch batch, Sortie sortie, float alpha) {
        drawFronts(batch, sortie, alpha, rim, false);
    }

    /** With the glowing shots, in their additive blend (the caller's): every front's light and the crystals' light. */
    void drawLight(SpriteBatch batch, Sortie sortie, float alpha) {
        drawFronts(batch, sortie, alpha, light, true);
        if (glow == null || !sortie.flying()) {
            return;
        }
        long tick = sortie.tick();
        for (int m = 0; m < waves.length; m++) {
            if (!waves[m]) {
                continue;
            }
            long age = tick - fired[m];
            int bloom = age >= 0 ? (int) (age / GLOW_FRAME_TICKS) + 1 : glow.size;
            AtlasRegion frame = glow.get(bloom < glow.size ? bloom : 0);
            drawCentred(
                    batch, frame, sortie.ship().renderX(alpha), sortie.ship().renderY(alpha) + NOSE);
        }
    }

    /** Over the hull while it flies (normal blend): the Warden's eye in its collar, rolled with the hull. */
    void drawMount(SpriteBatch batch, Sortie sortie, float alpha) {
        if (mount == null) {
            return;
        }
        int bank = Math.clamp(sortie.ship().bank() + ShipSpec.HARD_BANK, 0, mount.size - 1);
        drawCentred(
                batch,
                mount.get(bank),
                sortie.ship().renderX(alpha),
                sortie.ship().renderY(alpha) + NOSE - MOUNT_BELOW);
    }

    private void drawFronts(SpriteBatch batch, Sortie sortie, float alpha, Array<AtlasRegion> set, boolean additive) {
        WaveFire fronts = sortie.waves();
        for (int i = 0; i < fronts.count(); i++) {
            WeaponSpec.Wave wave =
                    sortie.armament().mount(fronts.mount(i)).weapon().wave().orElse(null);
            if (wave == null) {
                continue;
            }
            double travelled = fronts.progress(i) * wave.reach();
            // Between two steps: back along its path by the part of the step not yet run.
            double distance = Math.max(0, travelled - (1 - alpha) * wave.speed() * SimStep.SECONDS);
            double y = fronts.y(i) - (travelled - distance);
            double width = wave.widthAt(distance);
            float k = (float) brightness(distance, wave.reach(), fronts.met(i), wave.falloff());
            if (k <= 0) {
                continue;
            }
            if (additive) {
                batch.setColor(k, k, k, 1);
            } else {
                batch.setColor(1, 1, 1, k);
            }
            drawFront(batch, set.get(frame(width)), fronts, i, fronts.x(i), y, width);
        }
        batch.setColor(Color.WHITE);
    }

    /** One front's frame scaled in x to {@code width}, its apex at (x, y); a slice rock stopped left out. */
    private void drawFront(
            SpriteBatch batch, AtlasRegion region, WaveFire fronts, int i, double x, double y, double width) {
        int frameWidth = region.getRegionWidth();
        int height = region.getRegionHeight();
        float scale = (float) (width / frameWidth(frame(width)));
        float left = (float) (PixelScreen.PLAY_FIELD_X + x - frameWidth * scale / 2);
        float bottom = (float) (y - (height - APEX));
        boolean whole = true;
        for (int k = 0; k < WaveFire.SLICES && whole; k++) {
            whole = fronts.sliceLive(i, k);
        }
        if (whole) {
            batch.draw(region, left, bottom, frameWidth * scale, height);
            return;
        }
        double slice = width / WaveFire.SLICES;
        for (int k = 0; k < WaveFire.SLICES; k++) {
            if (!fronts.sliceLive(i, k)) {
                continue;
            }
            // The slice's span from the frame's left edge, px of the frame (the end slices take the glow's pad too).
            double from = k == 0 ? 0 : (frameWidth * scale / 2 - width / 2 + k * slice) / scale;
            double to = k == WaveFire.SLICES - 1
                    ? frameWidth
                    : (frameWidth * scale / 2 - width / 2 + (k + 1) * slice) / scale;
            int u0 = (int) Math.round(Math.clamp(from, 0, frameWidth));
            int u1 = (int) Math.round(Math.clamp(to, 0, frameWidth));
            if (u1 <= u0) {
                continue;
            }
            strip.setRegion(region, u0, 0, u1 - u0, height);
            batch.draw(strip, left + u0 * scale, bottom, (u1 - u0) * scale, height);
        }
    }

    private static void drawCentred(SpriteBatch batch, TextureRegion region, double x, double y) {
        batch.draw(region, (float) Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2.0), (float)
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
