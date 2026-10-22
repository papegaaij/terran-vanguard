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
import vanguard.sim.Layer;
import vanguard.sim.RisePhase;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part D: the Abyss Ray (design/enemies/naval/abyss-ray; tools/art/abyss_ray.py, round 48 b "bone skate") through
 * its rise ({@link Enemy#risePhase}): the shadow growing in the ship's lane ({@code -shadow}, frame i at i / 8 of the
 * shadow's time, heading 0), the rise over the shadow's last frame fading out with it ({@code -rise}), the glide at its
 * 16 headings with its slow wing beat ({@code abyss-ray}, heading × 3 + beat, the beat 0, 1, 0, 2 over 2.2 s), the sink
 * on the glide's side ({@code -sink}, side 0 gliding left, 1 right). While it is on {@code sub} (its shadow, the rise's
 * first half, the sink's second) it is drawn under the flyers ({@link #drawDeep}), on {@code air} with them. After the
 * grade, ungraded, its gill slits' and eye spots' glow ({@code -glow}, per heading) over the glide, fading in with the
 * rise and out with the sink, and the fan's violet mouth flash ({@code -mouth}, {@value #MOUTH_FRAME_TICKS} steps a
 * frame from each fan, at the heading's {@code mouth} of {@code pivots/abyss-ray.json}). Its {@code large} break-up
 * ({@code -death}, at the nearest of headings 0, 4 and 12) and its bloom ({@code -bloom}) are its death's (DeepDeaths).
 * Presentation only.
 */
final class AbyssRayLooks {
    /** The unit's slug. */
    static final String SLUG = "abyss-ray";
    /** The glide's headings and wing-beat frames per heading. */
    static final int HEADINGS = 16;

    static final int BEATS = 3;
    /** The beat plays these frames in turn, the whole cycle over {@value #BEAT_SECONDS} s. */
    static final int[] BEAT = {0, 1, 0, 2};

    static final double BEAT_SECONDS = 2.2;
    /** The shadow's, the rise's and each sink side's steps. */
    static final int STEPS = 8;
    /** The sink's sides: gliding left (heading 4) and right (heading 12). */
    static final int SINK_LEFT_HEADING = 4;

    static final int SINK_RIGHT_HEADING = 12;
    /** The break-up's headings (the rise and the two glides), its steps a heading and their steps (8 fps). */
    static final int[] DEATH_HEADINGS = {0, SINK_LEFT_HEADING, SINK_RIGHT_HEADING};

    static final int DEATH_FRAME_TICKS = 8;
    /** The bloom over the break-up plays at 10 fps. */
    static final int BLOOM_FRAME_TICKS = 6;
    /** The mouth flash's frames show this many steps each. */
    static final int MOUTH_FRAME_TICKS = 2;
    /** A volley's event lies within this distance of the Ray that fired it, px. */
    private static final double FIRED_REACH = 8;
    /** A killed shadow's break-up and bloom are this faint (it dies in the deep water). */
    static final float SHADOW_DEATH_OPACITY = 0.5f;

    private final Array<AtlasRegion> beat;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> shadow;
    private final Array<AtlasRegion> rise;
    private final Array<AtlasRegion> sink;
    private final Array<AtlasRegion> mouth;
    private final Array<Array<AtlasRegion>> deaths = new Array<>();
    private final Array<AtlasRegion> bloom;
    private final boolean[] ray;
    /** Per heading the mouth's offset from the frame's centre (x right, y up), px. */
    private final float[][] mouthAt = new float[HEADINGS][2];
    /** The last Ray seen: its heading drawn, whether it was a shadow still, and the step its last fan left. */
    private int lastHeading;

    private boolean lastShadow;
    private int fanSerial = -1;
    private long fanTick = Long.MIN_VALUE / 2;
    private final Array<AtlasRegion> none = new Array<>(0);

    AbyssRayLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        beat = optional(sprites, SLUG);
        glow = optional(sprites, SLUG + "-glow");
        shadow = optional(sprites, SLUG + "-shadow");
        rise = optional(sprites, SLUG + "-rise");
        sink = optional(sprites, SLUG + "-sink");
        mouth = optional(sprites, SLUG + "-mouth");
        bloom = optional(sprites, SLUG + "-bloom");
        Array<AtlasRegion> death = optional(sprites, SLUG + "-death");
        if (death.size >= DEATH_HEADINGS.length * STEPS) {
            for (int h = 0; h < DEATH_HEADINGS.length; h++) {
                Array<AtlasRegion> steps = new Array<>(STEPS);
                for (int s = 0; s < STEPS; s++) {
                    steps.add(death.get(h * STEPS + s));
                }
                deaths.add(steps);
            }
        }
        ray = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            ray[k] = kinds.get(k).slug().equals(SLUG);
        }
        JsonValue pivot = pivots.apply(SLUG);
        JsonValue mouths = pivot == null ? null : pivot.get("mouth");
        double width =
                pivot == null || !pivot.has("frame") ? 208 : pivot.get("frame").getDouble(0);
        double height =
                pivot == null || !pivot.has("frame") ? 208 : pivot.get("frame").getDouble(1);
        for (int h = 0; mouths != null && h < HEADINGS && h < mouths.size; h++) {
            mouthAt[h][0] = (float) (mouths.get(h).getDouble(0) - width / 2);
            mouthAt[h][1] = (float) (height / 2 - mouths.get(h).getDouble(1));
        }
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** A new attempt: no fan remembered. */
    void restart() {
        fanSerial = -1;
        fanTick = Long.MIN_VALUE / 2;
    }

    /** Whether kind {@code kind} is the Abyss Ray. */
    boolean ray(int kind) {
        return kind >= 0 && kind < ray.length && ray[kind];
    }

    /** Whether {@code enemy} is an Abyss Ray drawn by these looks (its art present). */
    boolean draws(Enemy enemy) {
        return ray(enemy.kind())
                && beat.size >= HEADINGS * BEATS
                && shadow.size >= STEPS
                && rise.size >= STEPS
                && sink.size >= 2 * STEPS;
    }

    /** Whether it is drawn under the flyers now: on {@code sub} (its shadow, the rise's first half, the sink's second). */
    static boolean deep(Enemy enemy) {
        return enemy.layer() == Layer.SUB;
    }

    /** The step of a phase drawn at its progress (0 to 1): {@code round down(progress × 8)}, the last one held. */
    static int step(double progress) {
        return Math.clamp((int) Math.floor(progress * STEPS), 0, STEPS - 1);
    }

    /** The wing-beat frame (0 level, 1 up, 2 down) at simulation step {@code tick} for a Ray at {@code phase}. */
    static int beatFrame(long tick, int phase) {
        double share = BEAT_SECONDS / BEAT.length;
        int at = Math.floorMod((long) Math.floor(tick / (share * SimStep.PER_SECOND)) + phase, BEAT.length);
        return BEAT[at];
    }

    /** The sink's side: 0 gliding left (heading 4), 1 gliding right (heading 12). */
    static int sinkSide(int direction) {
        return direction < 0 ? 0 : 1;
    }

    /** The glide's frame: its heading's beat frame. */
    AtlasRegion glideFrame(Enemy enemy, long tick) {
        int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
        return beat.get(heading * BEATS + beatFrame(tick, enemy.serial()));
    }

    /**
     * Its body frame in the glide (for the drop shadow and a hit's flash); null in another phase or for another unit
     * (the shadow, the rise and the sink are drawn by {@link #draw}).
     */
    AtlasRegion frame(Enemy enemy, long tick) {
        if (!draws(enemy) || enemy.risePhase() != RisePhase.GLIDE) {
            return null;
        }
        return glideFrame(enemy, tick);
    }

    /** The heading it is drawn at: the shadow's and the rise's 0, the glide's own, the sink's side's. */
    static int drawnHeading(Enemy enemy) {
        return switch (enemy.risePhase()) {
            case GLIDE -> EnemyLooks.heading(enemy.facing(), HEADINGS);
            case SINK -> sinkSide(enemy.riseDirection()) == 0 ? SINK_LEFT_HEADING : SINK_RIGHT_HEADING;
            default -> 0;
        };
    }

    /**
     * Draws the Ray: its shadow, the rise over the shadow's last frame fading out, the glide (through {@code flasher},
     * flashing after a hit) or the sink.
     */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        double progress = enemy.riseProgress(alpha);
        switch (enemy.risePhase()) {
            case SHADOW -> drawCentred(batch, shadow.get(step(progress)), x, y);
            case RISE -> {
                batch.setColor(1, 1, 1, (float) (1 - progress));
                drawCentred(batch, shadow.get(STEPS - 1), x, y);
                batch.setColor(Color.WHITE);
                drawCentred(batch, rise.get(step(progress)), x, y);
            }
            case GLIDE -> flasher.draw(batch, enemy, glideFrame(enemy, sortie.tick()), x, y);
            case SINK -> drawCentred(batch, sink.get(sinkSide(enemy.riseDirection()) * STEPS + step(progress)), x, y);
            default -> {}
        }
    }

    /** The Rays on {@code sub} (the shadow, the rise's first half, the sink's second), under the flyers. */
    void drawDeep(SpriteBatch batch, Sortie sortie, float alpha, BurrowerLooks.Flasher flasher) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (draws(enemy) && deep(enemy)) {
                draw(batch, sortie, enemy, alpha, flasher);
            }
        }
    }

    /** The glow's opacity: none as a shadow, in over the rise, full in the glide, out over the sink. */
    static float glowOpacity(RisePhase phase, double progress) {
        return switch (phase) {
            case RISE -> (float) Math.clamp(progress, 0, 1);
            case GLIDE -> 1;
            case SINK -> (float) Math.clamp(1 - progress, 0, 1);
            default -> 0;
        };
    }

    /** After each step: the last Ray's heading and whether it is a shadow; its fans (the mouth flash's start). */
    void stepped(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (ray(enemy.kind()) && enemy.risePhase() != RisePhase.NONE) {
                lastHeading = drawnHeading(enemy);
                lastShadow = enemy.risePhase() == RisePhase.SHADOW;
            }
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) != SimEvents.Type.ENEMY_FIRED) {
                continue;
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (ray(enemy.kind())
                        && Math.abs(enemy.renderX(1) - events.x(e)) <= FIRED_REACH
                        && Math.abs(enemy.renderY(1) - events.y(e)) <= FIRED_REACH) {
                    fanSerial = enemy.serial();
                    fanTick = sortie.tick();
                }
            }
        }
    }

    /** The mouth flash's frame {@code ticks} steps after a fan: -1 once it has played. */
    static int mouthFrame(long ticks, int frames) {
        long frame = ticks / MOUTH_FRAME_TICKS;
        return ticks < 0 || frame >= frames ? -1 : (int) frame;
    }

    /** After the grade: the Rays' gill glow (in with the rise, out with the sink) and the mouth flash of a fan. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (glow.size < HEADINGS) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy)) {
                continue;
            }
            float opacity = glowOpacity(enemy.risePhase(), enemy.riseProgress(alpha)) * ArsenalLooks.glowShare(enemy);
            if (opacity <= 0) {
                continue;
            }
            int heading = drawnHeading(enemy);
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            batch.setColor(1, 1, 1, opacity);
            drawCentred(batch, glow.get(heading), x, y);
            int flash = enemy.serial() == fanSerial ? mouthFrame(sortie.tick() - fanTick, mouth.size) : -1;
            if (flash >= 0) {
                drawCentred(batch, mouth.get(flash), x + mouthAt[heading][0], y + mouthAt[heading][1]);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The index in {@link #DEATH_HEADINGS} nearest a 16-heading index. */
    static int deathHeading(int heading) {
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int d = 0; d < DEATH_HEADINGS.length; d++) {
            int distance = Math.floorMod(heading - DEATH_HEADINGS[d], HEADINGS);
            distance = Math.min(distance, HEADINGS - distance);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = d;
            }
        }
        return best;
    }

    /** Its break-up (solid) at the last Ray's heading; empty for another kind or without the art. */
    Array<AtlasRegion> breakUp(int kind) {
        return ray(kind) && !deaths.isEmpty() ? deaths.get(deathHeading(lastHeading)) : none;
    }

    /** Its bloom (additive); empty for another kind. */
    Array<AtlasRegion> bloom(int kind) {
        return ray(kind) ? bloom : none;
    }

    /** How opaque its death is drawn: faint for a shadow killed in the deep water. */
    float deathOpacity() {
        return lastShadow ? SHADOW_DEATH_OPACITY : 1;
    }
}
