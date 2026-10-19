package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vanguard.content.AlliesData;
import vanguard.content.LevelData;
import vanguard.sim.Ally;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part D, a level's ground allies (design/allies/ares-landing-dome; design/campaign Level 17, The Ares Landing
 * domes; the sprites of tools/art/siege_units.py, the chosen round-36 look a "geodesic glass on a concrete ring"):
 * each dome on the ground pass, under the ground units, scrolling with the ground in its own sprite set (Eden's
 * larger {@code ares-dome-eden}, the level's {@code sprite}). Intact, its inner light breathing ({@code <set>_0..3}
 * at {@value #BREATHE_FPS} fps); a white additive flash ({@code <set>-flash}) for {@value #HIT_FLASH_TICKS} steps on
 * each hit; a crack overlay per hit taken ({@code <set>-crack-1..3}, cumulative: the n-th holds the ones before);
 * at its last hit before breaking its inner light flickering ({@code <set>-flicker_0..3}, stepped irregularly);
 * broken, the cave-in ({@code <set>-broken_0..5} at {@value #BREAK_FPS} fps, its last frame the wreck that stays)
 * with the dust plume rising out of it once ({@code <set>-dust_0..9} at {@value #DUST_FPS} fps). Nothing is thrown
 * onto the player's plane. Presentation only: it reads each ally's hits, its steps since its last hit and its
 * loss; nothing is allocated per frame.
 */
final class DomeLooks {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** The intact dome's breathing loop. */
    static final double BREATHE_FPS = 4;
    /** The hit flash: 0.1 s. */
    static final int HIT_FLASH_TICKS = 6;
    /** The flicker steps at this rate through {@link #FLICKER}, an irregular sequence of its four frames. */
    static final double FLICKER_FPS = 10;
    /** The flicker's frames per step (0 dim, 1 half, 2 nearly out, 3 half): stuttering, never a plain loop. */
    static final int[] FLICKER = {0, 1, 1, 0, 2, 1, 3, 3, 0, 2, 2, 1, 0, 3, 1, 2, 2, 0, 1, 3};
    /** The cave-in and the dust plume. */
    static final double BREAK_FPS = 10;

    static final double DUST_FPS = 8;
    /** The base name of the ally's sprites without a set of its own. */
    static final String DEFAULT_SET = "ares-dome";

    /** One dome's sprite set: its body loop, flicker, flash, cracks, cave-in and dust; empty where it has none. */
    record Look(
            Array<AtlasRegion> body,
            Array<AtlasRegion> flicker,
            AtlasRegion flash,
            Array<AtlasRegion> cracks,
            Array<AtlasRegion> broken,
            Array<AtlasRegion> dust) {
        static Look of(Sprites sprites, String name) {
            Array<AtlasRegion> cracks = new Array<>(3);
            for (int n = 1; sprites.has(name + "-crack-" + n); n++) {
                cracks.add(sprites.region(name + "-crack-" + n));
            }
            return new Look(
                    sprites.frames(name),
                    optional(sprites, name + "-flicker"),
                    sprites.has(name + "-flash") ? sprites.region(name + "-flash") : null,
                    cracks,
                    optional(sprites, name + "-broken"),
                    optional(sprites, name + "-dust"));
        }
    }

    /** Per ally its set, in the sortie's order. */
    private final Look[] sets;

    private DomeLooks(Look[] sets) {
        this.sets = sets;
    }

    /**
     * The ground allies' looks, or null for a level without them or without their sprites.
     *
     * @param names per ally (in the sortie's order) its sprites' base name ({@link #names}); the default set where the
     *     list is short or a set is missing
     */
    static DomeLooks of(Sprites sprites, LevelScript script, List<String> names) {
        if (script.groundAllies().isEmpty()) {
            return null;
        }
        Map<String, Look> byName = new HashMap<>();
        Look[] sets = new Look[script.groundAllies().get().units().size()];
        for (int k = 0; k < sets.length; k++) {
            String name = k < names.size() && sprites.has(names.get(k)) ? names.get(k) : DEFAULT_SET;
            if (!sprites.has(name)) {
                return null;
            }
            sets[k] = byName.computeIfAbsent(name, base -> Look.of(sprites, base));
        }
        return new DomeLooks(sets);
    }

    /** Per ground ally of {@code level} its sprites' base name: its own {@code sprite}, else its ally's. */
    static List<String> names(LevelData level, AlliesData allies) {
        return level.groundAllies().stream()
                .map(unit -> {
                    AlliesData.Ally spec = allies.allies().get(unit.ally());
                    return spec == null ? unit.sprite().orElse(DEFAULT_SET) : unit.spriteName(spec);
                })
                .toList();
    }

    /** The crack overlays showing after {@code hitsTaken} hits: one per hit, at most {@code available}. */
    static int cracks(double hitsTaken, int available) {
        return Math.max(0, Math.min(available, (int) Math.floor(hitsTaken + 1e-9)));
    }

    /** Whether a standing dome flickers: hit, and its next hit breaks it (of {@code hits} it breaks on). */
    static boolean flickers(double hitsTaken, double hits) {
        return hitsTaken > 1e-9 && hits - hitsTaken <= 1 + 1e-9;
    }

    /** The breathing loop's frame at real {@code seconds}, each dome a little out of step ({@code k}). */
    static int breatheFrame(double seconds, int k, int frames) {
        return (int) Math.floorMod((long) Math.floor(seconds * BREATHE_FPS) + k, (long) frames);
    }

    /** The flicker's frame at real {@code seconds}: the irregular sequence, each dome at its own place in it. */
    static int flickerFrame(double seconds, int k, int frames) {
        int step = (int) Math.floorMod((long) Math.floor(seconds * FLICKER_FPS) + 7L * k, (long) FLICKER.length);
        return Math.min(frames - 1, FLICKER[step]);
    }

    /** The cave-in's frame {@code ticks} steps after the break: its frames, then the last (the wreck). */
    static int brokenFrame(int ticks, int frames) {
        return Math.min(frames - 1, (int) (ticks * BREAK_FPS / SimStep.PER_SECOND));
    }

    /** The dust plume's frame {@code ticks} steps after the break; -1 once it has risen and gone. */
    static int dustFrame(int ticks, int frames) {
        int frame = (int) (ticks * DUST_FPS / SimStep.PER_SECOND);
        return frame < frames ? frame : -1;
    }

    /**
     * Every dome on the screen in its state.
     *
     * @param whiteFlash the hit flash's strength (toned down by the Gameplay tab's flash reduction)
     * @param seconds the real clock (the light's loops run on it)
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash, double seconds) {
        int count = Math.min(sets.length, sortie.allyCount());
        for (int k = 0; k < count; k++) {
            Ally ally = sortie.ally(k);
            Look set = sets[k];
            double x = ally.renderX(alpha);
            double y = ally.renderY(alpha);
            if (ally.lost()) {
                drawBroken(batch, set, ally, x, y);
                continue;
            }
            AtlasRegion body = set.body().first();
            if (!onScreen(y, body.getRegionHeight())) {
                continue;
            }
            double hits = sortie.allySpec(k).hp();
            double taken = ally.hitsTaken();
            AtlasRegion frame = flickers(taken, hits) && set.flicker().size > 0
                    ? set.flicker().get(flickerFrame(seconds, k, set.flicker().size))
                    : set.body().get(breatheFrame(seconds, k, set.body().size));
            drawCentred(batch, frame, x, y);
            int cracks = cracks(taken, set.cracks().size);
            if (cracks > 0) {
                drawCentred(batch, set.cracks().get(cracks - 1), x, y);
            }
            if (set.flash() != null && ally.ticksSinceHit() < HIT_FLASH_TICKS) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                batch.setColor(1, 1, 1, whiteFlash);
                drawCentred(batch, set.flash(), x, y);
                batch.setColor(Color.WHITE);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    /** A broken dome: its cave-in to the wreck, the dust plume rising out of it once. */
    private static void drawBroken(SpriteBatch batch, Look set, Ally ally, double x, double y) {
        int ticks = ally.ticksSinceLost();
        Array<AtlasRegion> broken = set.broken().size > 0 ? set.broken() : set.body();
        AtlasRegion wreck = broken.get(set.broken().size > 0 ? brokenFrame(ticks, broken.size) : 0);
        int dust = set.dust().size > 0 ? dustFrame(ticks, set.dust().size) : -1;
        int tallest = dust >= 0
                ? Math.max(wreck.getRegionHeight(), set.dust().get(dust).getRegionHeight())
                : wreck.getRegionHeight();
        if (!onScreen(y, tallest)) {
            return;
        }
        drawCentred(batch, wreck, x, y);
        if (dust >= 0) {
            drawCentred(batch, set.dust().get(dust), x, y);
        }
    }

    /** Whether a sprite {@code height} px tall centred at height {@code y} (y up) shows on the play field. */
    static boolean onScreen(double y, double height) {
        return y - height / 2 < PlayField.HEIGHT && y + height / 2 > 0;
    }

    private static void drawCentred(SpriteBatch batch, AtlasRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(X0 + x - region.getRegionWidth() / 2f),
                Math.round(y - region.getRegionHeight() / 2f));
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }
}
