package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.Armament;
import vanguard.sim.ChainFire;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * M10 part A, step A3b: the Plasma Arc's chains as the player sees them (design/player/weapons/plasma-arc, round 72 A.7
 * a "the wide Tesla"; tools/art/act6_gear.py {@code arc}, whose docstring gives the sprites), all additive: each
 * discharge's strikes ({@link SimEvents.Type#CHAIN_STRIKE}, value {@link ChainFire#event}) draw for {@value #ARC_TICKS}
 * steps, the first from the nose (the mount's muzzle, following the ship) to the first struck point and each jump from
 * the point before to its own, strip {@code plasma-arc-arc-<j>} for place {@code j} in the chain ({@value #PLACES}
 * widths, 1.7× the pod's then 0.72× each jump; a longer chain keeps the last), a 160 × 48 strip stretched along x to
 * the distance (y unscaled) and turned to the direction, the discharge's shape one of three in turn; and the nose's
 * emitter light ({@code plasma-arc-glow_0..1}, alternate frames each step) while it shows. The spark burst on each
 * struck unit is the weapon's impact ({@link WeaponLooks}: {@code plasma-arc-spark}). The struck points stay where they
 * were struck.
 */
final class ChainArcLooks {
    /** A discharge shows this many steps (0.1 s, the art's). */
    static final int ARC_TICKS = 6;
    /** The widths the art has, one per place in the chain (0 the first strike); later places keep the last. */
    static final int PLACES = 6;
    /** The most strikes a discharge's look keeps. */
    static final int MOST = 16;

    private final List<Array<AtlasRegion>> arcs;
    private final Array<AtlasRegion> glow;
    /** Per mount: the step its last discharge struck at (far in the past for none), how many strikes, where. */
    private final long[] struck;

    private final int[] count;
    private final double[][] xs;
    private final double[][] ys;
    /** Per mount: the shape its last discharge took, and how many it discharged (the shapes in turn). */
    private final int[] shape;

    private final int[] discharges;

    ChainArcLooks(List<Array<AtlasRegion>> arcs, Array<AtlasRegion> glow, int mounts) {
        this.arcs = arcs;
        this.glow = glow;
        struck = new long[mounts];
        count = new int[mounts];
        xs = new double[mounts][MOST];
        ys = new double[mounts][MOST];
        shape = new int[mounts];
        discharges = new int[mounts];
        restart();
    }

    /** The looks for an armament with a Plasma Arc fitted (a {@code CHAIN} mount); null without one or its art. */
    static ChainArcLooks of(Sprites sprites, Armament armament) {
        String slug = null;
        for (int m = 0; m < armament.size() && slug == null; m++) {
            Armament.Mount mount = armament.mount(m);
            if (mount.fires() && mount.weapon().delivery() == WeaponSpec.Delivery.CHAIN) {
                slug = mount.weapon().slug();
            }
        }
        if (slug == null) {
            return null;
        }
        List<Array<AtlasRegion>> arcs = new ArrayList<>();
        for (int j = 0; j < PLACES; j++) {
            if (!sprites.has(strip(slug, j))) {
                return null;
            }
            arcs.add(sprites.frames(strip(slug, j)));
        }
        return new ChainArcLooks(
                arcs, sprites.has(slug + "-glow") ? sprites.frames(slug + "-glow") : new Array<>(0), armament.size());
    }

    /** The strip of place {@code j} in the chain of weapon {@code slug}. */
    static String strip(String slug, int j) {
        return slug + "-arc-" + j;
    }

    /** The strip's place for strike {@code index} of a chain (0 the first): its own, a longer chain's the last. */
    static int place(int index) {
        return Math.min(index, PLACES - 1);
    }

    /** The level restarts: no chain shows. */
    void restart() {
        Arrays.fill(struck, Long.MIN_VALUE / 2);
        Arrays.fill(count, 0);
        Arrays.fill(discharges, 0);
    }

    /** After each simulation step: the step's strikes. */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) != SimEvents.Type.CHAIN_STRIKE) {
                continue;
            }
            int m = ChainFire.eventMount(events.value(e));
            int i = ChainFire.eventIndex(events.value(e));
            if (m >= struck.length || i >= MOST) {
                continue;
            }
            if (i == 0 || struck[m] != sortie.tick()) {
                // A new discharge: its shape the next of three.
                struck[m] = sortie.tick();
                count[m] = 0;
                shape[m] = ArcLooks.shapeOf(discharges[m]++, arcs.getFirst().size);
            }
            xs[m][i] = events.x(e);
            ys[m][i] = events.y(e);
            count[m] = Math.max(count[m], i + 1);
        }
    }

    /** Whether mount {@code m}'s last discharge shows at step {@code tick}. */
    boolean showing(int m, long tick) {
        long age = tick - struck[m];
        return age >= 0 && age < ARC_TICKS && count[m] > 0;
    }

    /** How many strikes mount {@code m}'s last discharge drew. */
    int strikes(int m) {
        return count[m];
    }

    /**
     * With the glowing shots, in their additive blend (the caller's): every showing chain, from the nose (the ship at
     * {@code alpha} and the mount's muzzle) along its struck points, and the nose's light.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        double shipX = sortie.ship().renderX(alpha);
        double shipY = sortie.ship().renderY(alpha);
        for (int m = 0; m < struck.length; m++) {
            if (!showing(m, tick)) {
                continue;
            }
            WeaponSpec.Muzzle nose =
                    sortie.armament().mount(m).weapon().muzzles().getFirst();
            double x0 = shipX + nose.dx();
            double y0 = shipY + nose.dy();
            double fromX = x0;
            double fromY = y0;
            for (int i = 0; i < count[m]; i++) {
                Array<AtlasRegion> strips = arcs.get(place(i));
                ArcLooks.drawArc(batch, strips.get(shape[m] % strips.size), fromX, fromY, xs[m][i], ys[m][i]);
                fromX = xs[m][i];
                fromY = ys[m][i];
            }
            if (!glow.isEmpty()) {
                AtlasRegion light = glow.get((int) (tick % glow.size));
                batch.draw(light, (float) (PixelScreen.PLAY_FIELD_X + x0 - light.getRegionWidth() / 2f), (float)
                        (y0 - light.getRegionHeight() / 2f));
            }
        }
    }
}
