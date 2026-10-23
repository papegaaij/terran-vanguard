package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.ShipSlow;
import vanguard.sim.SimStep;
import vanguard.sim.SonarRings;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
import vanguard.sim.Wingman;

/**
 * M8 part E: the Siren (design/enemies/naval/siren; tools/art/siren.py, round 48 b "frill bell"), a nest on a trench
 * wall's foot, and its rings. Its frame is its facing's (0 on the left wall facing right, 1 on the right wall) step:
 * the idle breath (0 and 1 at {@value #BREATH_FPS} fps), the tell's three steps over its sac's tell ({@link
 * Enemy#ringTell}), the song (5) while its ring is in its first {@value #SONG_SECONDS} s; drawn with its centre
 * {@value #BODY_OFFSET} px from the unit's toward its wall (the art's centre 6 px off the foot line, the unit's 12),
 * no shadow (it is part of the wall). After the grade, ungraded: its glow ({@code -glow}, per frame), the rings in
 * flight ({@link SonarRings}: the {@code -ring} strip wrapped round each circle, its row {@code front_row} at the
 * radius, tiling every 64 px along the arc at {@value #RING_FPS} fps, fading over the last quarter of its radius, the
 * arc rock stops left out: the rock on each line from the sac is looked up once a ring, as the ring and the rock scroll
 * together) and the slowed hulls' sonar shimmer ({@code -shimmer} on the ship and Rook while the slow lasts, fading
 * over its last quarter). Its death ({@link DeepDeaths}): its facing's collapse ({@code -death}, {@value
 * #DEATH_FRAME_TICKS} steps a frame), the pale plume at the sac rising {@value #PLUME_RISE} px ({@code -plume}), then
 * the dark husk ({@code -dead}) on the wall; all on the ground, scrolling with it. Presentation only.
 */
final class SirenLooks {
    /** The unit's slug. */
    static final String SLUG = "siren";
    /** The art's facings and steps a facing: the idle breath (0, 1), the tell (2-4), the song (5). */
    static final int FACINGS = 2;

    static final int STEPS = 6;
    static final int TELL = 2;
    static final int TELL_STEPS = 3;
    static final int SONG = 5;
    /** The idle breath's rate. */
    static final double BREATH_FPS = 2;
    /** The song frame shows while the ring is this young, s. */
    static final double SONG_SECONDS = 0.15;
    /** The art's centre lies this far from the unit's centre toward its wall, px (6 off the foot line, the unit 12). */
    static final double BODY_OFFSET = 6;
    /** The collapse's steps a facing and their steps (12 fps); the plume's steps (10 fps) and its rise, px. */
    static final int DEATH_STEPS = 4;

    static final int DEATH_FRAME_TICKS = 5;
    static final int PLUME_FRAME_TICKS = 6;
    static final double PLUME_RISE = 16;
    /** The ring strip's ripple, the share of the radius it fades over, its segment along the arc, px. */
    static final double RING_FPS = 12;

    static final double RING_FADE = 0.25;
    static final int RING_SEGMENT = 16;
    /** The lines from the sac whose rock is looked up, round the circle; their samples' spacing, px. */
    static final int ANGLES = 96;

    static final double LINE_STEP = 4;
    /** The shimmer's rate and the share of the slow it fades over at its end. */
    static final double SHIMMER_FPS = 12;

    static final double SHIMMER_FADE = 0.25;
    /** The rings' rock looked up at once (16 in flight at most). */
    static final int RING_SLOTS = 16;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> death;
    private final Array<AtlasRegion> dead;
    private final Array<AtlasRegion> plume;
    private final Array<AtlasRegion> ring;
    private final Array<AtlasRegion> shimmer;
    /** Per facing the collapse's steps and the husk as a one-frame animation (built once). */
    private final Array<Array<AtlasRegion>> deaths = new Array<>();

    private final Array<Array<AtlasRegion>> husks = new Array<>();
    private final Array<AtlasRegion> none = new Array<>(0);
    private final boolean[] siren;
    /** Per facing the sac's centre from the frame's centre (x right, y up), px. */
    private final float[][] sac = new float[FACINGS][2];
    /** The ring strip's row at the radius, from its top. */
    private final int frontRow;
    /** Per looked-up ring its singer, its centre's ground row, and per line the radius rock stops it at (+∞: none). */
    private final int[] ringSerial = new int[RING_SLOTS];

    private final long[] ringRow = new long[RING_SLOTS];
    private final double[][] blocked = new double[RING_SLOTS][ANGLES];
    private final boolean[] ringSeen = new boolean[RING_SLOTS];
    /** A piece of the ring strip, set per segment (allocated once). */
    private final TextureRegion piece = new TextureRegion();

    SirenLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = optional(sprites, SLUG);
        glow = optional(sprites, SLUG + "-glow");
        death = optional(sprites, SLUG + "-death");
        dead = optional(sprites, SLUG + "-dead");
        plume = optional(sprites, SLUG + "-plume");
        ring = optional(sprites, SLUG + "-ring");
        shimmer = optional(sprites, SLUG + "-shimmer");
        for (int f = 0; f < FACINGS; f++) {
            Array<AtlasRegion> steps = new Array<>(DEATH_STEPS);
            for (int k = 0; k < DEATH_STEPS && death.size >= FACINGS * DEATH_STEPS; k++) {
                steps.add(death.get(f * DEATH_STEPS + k));
            }
            deaths.add(steps);
            Array<AtlasRegion> husk = new Array<>(1);
            if (dead.size >= FACINGS) {
                husk.add(dead.get(f));
            }
            husks.add(husk);
        }
        siren = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            siren[k] = kinds.get(k).slug().equals(SLUG);
        }
        JsonValue pivot = pivots.apply(SLUG);
        double width =
                pivot == null || !pivot.has("frame") ? 64 : pivot.get("frame").getDouble(0);
        double height =
                pivot == null || !pivot.has("frame") ? 56 : pivot.get("frame").getDouble(1);
        JsonValue sacs = pivot == null ? null : pivot.get("sac");
        for (int f = 0; f < FACINGS; f++) {
            if (sacs != null && f < sacs.size) {
                sac[f][0] = (float) (sacs.get(f).getDouble(0) - width / 2);
                sac[f][1] = (float) (height / 2 - sacs.get(f).getDouble(1));
            }
        }
        JsonValue strip = pivot == null ? null : pivot.get("ring");
        frontRow = strip == null || !strip.has("front_row") ? 7 : strip.getInt("front_row");
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** A new attempt: no ring looked up. */
    void restart() {
        Arrays.fill(ringSerial, -1);
    }

    /** Whether kind {@code kind} is the Siren. */
    boolean siren(int kind) {
        return kind >= 0 && kind < siren.length && siren[kind];
    }

    /** Whether {@code enemy} is a Siren with its art. */
    boolean draws(Enemy enemy) {
        return frames.size >= FACINGS * STEPS && siren(enemy.kind());
    }

    /** The facing of a nest at {@code x}: 0 on the left wall (facing right), 1 on the right one. */
    static int facing(double x) {
        return x < PlayField.WIDTH / 2.0 ? 0 : 1;
    }

    /** The art's centre for a unit at {@code x}: {@value #BODY_OFFSET} px toward its wall. */
    static double bodyX(double x) {
        return facing(x) == 0 ? x - BODY_OFFSET : x + BODY_OFFSET;
    }

    /**
     * The step drawn: the song while its ring is young, the tell's step over its sac's tell ({@code tell} 0 to 1, 0
     * outside it), else the idle breath at {@code tick} at its own {@code phase}.
     */
    static int step(double tell, boolean song, long tick, int phase) {
        if (song) {
            return SONG;
        }
        if (tell > 0) {
            return TELL + Math.min(TELL_STEPS - 1, (int) (tell * TELL_STEPS));
        }
        return (int) Math.floorMod((long) Math.floor(tick * BREATH_FPS / SimStep.PER_SECOND) + phase, 2L);
    }

    /** Whether a ring of {@code serial}'s is in its first {@value #SONG_SECONDS} s. */
    private static boolean singing(Sortie sortie, Enemy enemy, float alpha) {
        SonarRings rings = sortie.sonarRings();
        if (rings == null || enemy.spec().slowRing().isEmpty()) {
            return false;
        }
        double young = enemy.spec().slowRing().get().speed() * SONG_SECONDS;
        for (int i = 0; i < rings.count(); i++) {
            if (rings.source(i) == enemy.serial() && rings.radius(i, alpha) < young) {
                return true;
            }
        }
        return false;
    }

    /** Its frame index now. */
    int frameIndex(Sortie sortie, Enemy enemy, float alpha) {
        int step = step(enemy.ringTell(alpha), singing(sortie, enemy, alpha), sortie.tick(), enemy.serial());
        return facing(enemy.renderX(alpha)) * STEPS + step;
    }

    /** Its frame now; null for another unit. */
    AtlasRegion frame(Sortie sortie, Enemy enemy, float alpha) {
        return draws(enemy) ? frames.get(frameIndex(sortie, enemy, alpha)) : null;
    }

    /** A Siren on its wall's foot, flashing after a hit. */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        flasher.draw(
                batch,
                enemy,
                frames.get(frameIndex(sortie, enemy, alpha)),
                bodyX(enemy.renderX(alpha)),
                enemy.renderY(alpha));
    }

    /** After each step: the rock on the lines of each new ring looked up (once: the ring and the rock scroll together). */
    void stepped(Sortie sortie) {
        SonarRings rings = sortie.sonarRings();
        Terrain terrain = sortie.terrain();
        if (rings == null || terrain == null) {
            return;
        }
        Arrays.fill(ringSeen, false);
        double scroll = sortie.groundScroll();
        for (int i = 0; i < rings.count(); i++) {
            long row = Math.round(rings.y(i, 1) + scroll);
            int s = ringSlot(rings.source(i), row);
            if (s < 0) {
                s = ringSlot(-1, Long.MIN_VALUE);
                if (s < 0) {
                    continue;
                }
                ringSerial[s] = rings.source(i);
                ringRow[s] = row;
                lookUp(terrain, scroll, rings.x(i), rings.y(i, 1), rings.maxRadius(i), blocked[s]);
            }
            ringSeen[s] = true;
        }
        for (int s = 0; s < RING_SLOTS; s++) {
            if (!ringSeen[s]) {
                ringSerial[s] = -1;
                ringRow[s] = Long.MIN_VALUE;
            }
        }
    }

    private int ringSlot(int serial, long row) {
        for (int s = 0; s < RING_SLOTS; s++) {
            if (ringSerial[s] == serial && (serial < 0 || ringRow[s] == row)) {
                return s;
            }
        }
        return -1;
    }

    /** Per line from (x, y) the radius where rock stops it ({@link Terrain#rock}, as the simulation's line), +∞ none. */
    static void lookUp(Terrain terrain, double scroll, double x, double y, double radius, double[] out) {
        for (int a = 0; a < out.length; a++) {
            double angle = (a + 0.5) * 2 * Math.PI / out.length;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            out[a] = Double.POSITIVE_INFINITY;
            for (double r = LINE_STEP; r <= radius + LINE_STEP; r += LINE_STEP) {
                if (terrain.rock(scroll, x + r * cos, y + r * sin, 0, 0)) {
                    out[a] = r - LINE_STEP / 2;
                    break;
                }
            }
        }
    }

    /** A ring's opacity at {@code radius} of its {@code most}: full, fading to none over the last quarter. */
    static float ringOpacity(double radius, double most) {
        double from = most * (1 - RING_FADE);
        return radius <= from ? 1 : (float) Math.clamp((most - radius) / (most - from), 0, 1);
    }

    /** The segments a ring of {@code radius} is drawn in: about one each {@value #RING_SEGMENT} px of arc. */
    static int segments(double radius) {
        return Math.clamp((int) Math.ceil(2 * Math.PI * radius / RING_SEGMENT), 12, ANGLES);
    }

    /** After the grade, additive: the nests' glow, the rings, the slowed hulls' shimmer. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (glow.size >= FACINGS * STEPS) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (draws(enemy)) {
                    drawCentred(
                            batch,
                            glow.get(frameIndex(sortie, enemy, alpha)),
                            bodyX(enemy.renderX(alpha)),
                            enemy.renderY(alpha));
                }
            }
        }
        drawRings(batch, sortie, alpha, seconds);
        drawShimmer(batch, sortie, alpha, seconds);
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawRings(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        SonarRings rings = sortie.sonarRings();
        if (rings == null || ring.isEmpty()) {
            return;
        }
        AtlasRegion strip = ring.get((int) Math.floorMod((long) Math.floor(seconds * RING_FPS), (long) ring.size));
        int height = strip.getRegionHeight();
        int tiles = Math.max(1, strip.getRegionWidth() / RING_SEGMENT);
        float originY = height - frontRow - 0.5f;
        double scroll = sortie.groundScroll();
        for (int i = 0; i < rings.count(); i++) {
            double radius = rings.radius(i, alpha);
            if (radius < 1) {
                continue;
            }
            int s = ringSlot(rings.source(i), Math.round(rings.y(i, 1) + scroll));
            double[] rock = s < 0 ? null : blocked[s];
            batch.setColor(1, 1, 1, ringOpacity(radius, rings.maxRadius(i)));
            double cx = rings.x(i);
            double cy = rings.y(i, alpha);
            int n = segments(radius);
            float length = (float) (2 * Math.PI * radius / n) + 1;
            for (int k = 0; k < n; k++) {
                if (rock != null && rock[(int) ((k + 0.5) * ANGLES / n) % ANGLES] < radius) {
                    continue;
                }
                double angle = (k + 0.5) * 2 * Math.PI / n;
                piece.setRegion(strip, (k % tiles) * RING_SEGMENT, 0, RING_SEGMENT, height);
                float px = (float) (X0 + cx + radius * Math.cos(angle));
                float py = (float) (cy + radius * Math.sin(angle));
                batch.draw(
                        piece,
                        px - length / 2,
                        py - originY,
                        length / 2,
                        originY,
                        length,
                        height,
                        1,
                        1,
                        (float) Math.toDegrees(angle) - 90);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** The shimmer's opacity with {@code share} of the slow left: full, fading over its last quarter. */
    static float shimmerOpacity(double share) {
        return share <= 0 ? 0 : (float) Math.min(1, share / SHIMMER_FADE);
    }

    private void drawShimmer(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        if (shimmer.isEmpty()) {
            return;
        }
        AtlasRegion frame =
                shimmer.get((int) Math.floorMod((long) Math.floor(seconds * SHIMMER_FPS), (long) shimmer.size));
        if (sortie.flying()) {
            ShipSlow slow = sortie.ship().slow();
            if (slow.slowed()) {
                batch.setColor(1, 1, 1, shimmerOpacity(slow.share()));
                drawCentred(
                        batch,
                        frame,
                        sortie.ship().renderX(alpha),
                        sortie.ship().renderY(alpha));
            }
        }
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null && !rook.ejected() && rook.slow().slowed()) {
            batch.setColor(1, 1, 1, shimmerOpacity(rook.slow().share()));
            drawCentred(batch, frame, rook.renderX(alpha), rook.renderY(alpha));
        }
        batch.setColor(Color.WHITE);
    }

    /** A killed Siren's collapse at its facing (at {@code x}, its unit's centre); empty for another kind. */
    Array<AtlasRegion> collapse(int kind, double x) {
        return siren(kind) ? deaths.get(facing(x)) : none;
    }

    /** A killed Siren's husk left on its wall at its facing, as a one-frame animation; empty for another kind. */
    Array<AtlasRegion> husk(int kind, double x) {
        return siren(kind) ? husks.get(facing(x)) : none;
    }

    /** A killed Siren's plume; empty for another kind. */
    Array<AtlasRegion> plume(int kind) {
        return siren(kind) ? plume : none;
    }

    /** The sac's centre across from a unit's centre at {@code x}, px (the plume's origin). */
    double sacX(double x) {
        return bodyX(x) + sac[facing(x)][0];
    }

    /** The sac's centre's height from the unit's centre, px. */
    double sacDy(double x) {
        return sac[facing(x)][1];
    }
}
