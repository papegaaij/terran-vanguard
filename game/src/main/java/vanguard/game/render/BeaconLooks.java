package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part G: Level 27's relay beacons (design/enemies/ground/relay-beacon, the secondary's targets; tools/art/
 * relay_beacon.py), Levels 24–26's scenery mast as a ground unit on the beacon ridge. Its frame is its lamp's 4 s pulse
 * ({@code relay-beacon_0..15} at {@value #PULSE_FPS} fps on the step clock, every beacon in step); after the grade its
 * light ({@code -glow}, additive, the same frame). Its death (EventEffects' deep death, the {@code small} under-water
 * burst): the lamp's break at the pivot file's {@code lamp} ({@code -break}, additive, {@value #BREAK_FRAME_TICKS} steps
 * a frame) and the dark mast left on the ground ({@code -dead}, scrolling with it).
 */
final class BeaconLooks {
    /** The unit's slug. */
    static final String SLUG = "relay-beacon";
    /** The pulse's rate: its 16 frames over the 4 s pulse. */
    static final double PULSE_FPS = 4;
    /** The break's frames show this many steps (12 fps). */
    static final int BREAK_FRAME_TICKS = 5;
    /** The art's frame, px. */
    static final int FRAME_WIDTH = 72;

    static final int FRAME_HEIGHT = 104;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> breakFrames;
    private final Array<Array<AtlasRegion>> remains = new Array<>(1);
    private final boolean[] beacon;
    /** The lamp from the frame's centre (x right, y up), px. */
    private final float lampDx;

    private final float lampDy;

    BeaconLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = optional(sprites, SLUG);
        glow = optional(sprites, SLUG + "-glow");
        breakFrames = optional(sprites, SLUG + "-break");
        if (sprites.has(SLUG + "-dead")) {
            Array<AtlasRegion> one = new Array<>(1);
            one.add(sprites.frames(SLUG + "-dead").first());
            remains.add(one);
        }
        beacon = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            beacon[k] = kinds.get(k).slug().equals(SLUG);
        }
        JsonValue pivot = pivots.apply(SLUG);
        JsonValue lamp = pivot == null ? null : pivot.get("lamp");
        lampDx = lamp == null ? 0 : (float) (lamp.getDouble(0) - FRAME_WIDTH / 2.0);
        lampDy = lamp == null ? FRAME_HEIGHT / 2f - 14 : (float) (FRAME_HEIGHT / 2.0 - lamp.getDouble(1));
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Whether kind {@code kind} is a relay beacon. */
    boolean beacon(int kind) {
        return kind >= 0 && kind < beacon.length && beacon[kind];
    }

    /** The pulse's frame at step {@code tick} of {@code count} (every beacon in step, as the scenery's). */
    static int pulseFrame(long tick, int count) {
        return (int) Math.floorMod((long) Math.floor(tick * PULSE_FPS / SimStep.PER_SECOND), (long) count);
    }

    /** Its frame now; null for another unit or without its art. */
    AtlasRegion frame(Enemy enemy, long tick) {
        return frames.isEmpty() || !beacon(enemy.kind()) ? null : frames.get(pulseFrame(tick, frames.size));
    }

    /** After the grade, additive: each live beacon's lamp light at its pulse's frame. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (glow.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        int frame = pulseFrame(sortie.tick(), glow.size);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (beacon(enemy.kind())) {
                drawCentred(batch, glow.get(frame), enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The lamp's break (additive), empty for another kind. */
    Array<AtlasRegion> breakUp(int kind) {
        return beacon(kind) ? breakFrames : EMPTY;
    }

    /** The dark mast left on the ground, a one-frame animation; none for another kind. */
    Array<Array<AtlasRegion>> remains(int kind) {
        return beacon(kind) ? remains : NO_REMAINS;
    }

    /** The lamp from the unit's centre, x and y (up), px. */
    float lampDx() {
        return lampDx;
    }

    float lampDy() {
        return lampDy;
    }

    private static final Array<AtlasRegion> EMPTY = new Array<>(0);
    private static final Array<Array<AtlasRegion>> NO_REMAINS = new Array<>(0);
}
