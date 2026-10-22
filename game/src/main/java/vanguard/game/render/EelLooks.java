package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import vanguard.sim.Chain;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part C: the Eel Swarm (design/enemies/naval/eel-swarm; tools/art/eel_swarm.py, round 48 a "ribbon eel"), a water
 * chain drawn from its tail to its head at 16 headings: the head ({@code eel-swarm}, heading × 3 + rest, tell, shot),
 * the body eels ({@code -segment}, heading × 2 + rest or lit) and the tail ({@code -tail}, likewise). The stripe tell:
 * over the {@value #TELL_SECONDS} s from a head attack's tell ({@code EYE_GLINT} at the head, its value the head's kind)
 * the violet stripe runs from the tail to the head (member i of n lit while |1 − i ⁄ n − u| &lt; {@value #WAVE}, u 0 at
 * the tell's start to 1 at the shot), the head shows its tell frame, then its shot frame for {@value #SHOT_TICKS} steps
 * after the volley ({@code ENEMY_FIRED} at the head); a lit part's stripe blooms ({@code -stripe}, additive, heading × 3
 * + part) and the jaws flash ({@code -muzzle}, additive, at the mouth of {@code pivots/eel-swarm.json}) after the
 * grade, so the tell reads under it ({@link #drawGlows}). A cut's rear part scatters as the simulation moves it; a dead
 * part sinks as its husk ({@code -husk} / {@code -head-husk}, heading × 4 + step, {@value #HUSK_FRAME_TICKS} steps a
 * frame, {@link #husk}). Presentation only.
 */
final class EelLooks {
    /** The unit's slug (its parts: {@code eel-swarm-segment}, {@code eel-swarm-tail}). */
    static final String SLUG = "eel-swarm";
    /** The art's headings. */
    static final int HEADINGS = 16;
    /** The head's frames per heading: rest, tell, shot. */
    static final int HEAD_STATES = 3;

    static final int TELL = 1;
    static final int SHOT = 2;
    /** The stripe tell's length (the attacks' {@code tell}), s, and the wave's half-width. */
    static final double TELL_SECONDS = 0.3;

    static final double WAVE = 0.2;
    /** The head's shot frame shows this many steps after a volley; the muzzle's 4 frames 2 steps each. */
    static final int SHOT_TICKS = 6;

    static final int MUZZLE_FRAME_TICKS = 2;
    /** A husk's 4 sinking steps, each this many steps. */
    static final int HUSK_FRAME_TICKS = 4;

    static final int HUSK_STEPS = 4;
    /** The stripe's parts in its frames. */
    static final int PART_HEAD = 0;

    static final int PART_SEGMENT = 1;
    static final int PART_TAIL = 2;
    /** An event at a head lies within this distance of it, px. */
    private static final double REACH = 8;

    private final Array<AtlasRegion> head;
    private final Array<AtlasRegion> segment;
    private final Array<AtlasRegion> tail;
    private final Array<AtlasRegion> stripe;
    private final Array<AtlasRegion> muzzle;
    /** Per heading the husk's 4 steps (body and tail, and the head's), built once. */
    private final Array<Array<AtlasRegion>> husks = new Array<>();

    private final Array<Array<AtlasRegion>> headHusks = new Array<>();
    /** Per heading the mouth from the head frame's centre, px, y up. */
    private final float[][] mouth = new float[HEADINGS][2];
    /** Per enemy kind: 0 not an eel, 1 its head, 2 a segment, 3 its tail. */
    private final int[] role;
    /** Per eel chain, the step its last tell started and its last volley left. */
    private final Map<Chain, long[]> clocks = new IdentityHashMap<>();

    EelLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        head = optional(sprites, SLUG);
        segment = optional(sprites, SLUG + "-segment");
        tail = optional(sprites, SLUG + "-tail");
        stripe = optional(sprites, SLUG + "-stripe");
        muzzle = optional(sprites, SLUG + "-muzzle");
        slices(optional(sprites, SLUG + "-husk"), husks);
        slices(optional(sprites, SLUG + "-head-husk"), headHusks);
        role = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            role[k] = slug.equals(SLUG) ? 1 : slug.equals(SLUG + "-segment") ? 2 : slug.equals(SLUG + "-tail") ? 3 : 0;
        }
        JsonValue pivot = pivots.apply(SLUG);
        JsonValue mouths = pivot == null ? null : pivot.get("mouth");
        double size =
                pivot == null || !pivot.has("size") ? 48 : pivot.get("size").getDouble(0);
        for (int h = 0; mouths != null && h < HEADINGS && h < mouths.size; h++) {
            mouth[h][0] = (float) (mouths.get(h).getDouble(0) - size / 2);
            mouth[h][1] = (float) (size / 2 - mouths.get(h).getDouble(1));
        }
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Splits a husk set (heading × 4 + step) into one 4-step animation per heading. */
    private static void slices(Array<AtlasRegion> frames, Array<Array<AtlasRegion>> into) {
        if (frames.size < HEADINGS * HUSK_STEPS) {
            return;
        }
        for (int h = 0; h < HEADINGS; h++) {
            Array<AtlasRegion> steps = new Array<>(HUSK_STEPS);
            for (int s = 0; s < HUSK_STEPS; s++) {
                steps.add(frames.get(h * HUSK_STEPS + s));
            }
            into.add(steps);
        }
    }

    /** Whether the level has an eel with its art. */
    boolean present() {
        return !head.isEmpty() && !segment.isEmpty() && !tail.isEmpty() && kindOf(1) >= 0;
    }

    private int kindOf(int wanted) {
        for (int k = 0; k < role.length; k++) {
            if (role[k] == wanted) {
                return k;
            }
        }
        return -1;
    }

    /** Whether enemy kind {@code kind} is an eel's part (head, segment or tail). */
    boolean part(int kind) {
        return kind >= 0 && kind < role.length && role[kind] != 0;
    }

    /** Whether enemy kind {@code kind} is an eel's head. */
    boolean head(int kind) {
        return kind >= 0 && kind < role.length && role[kind] == 1;
    }

    /** Whether it draws {@code chain} (an eel's, with its art). */
    boolean draws(Chain chain) {
        return present() && chain.spec().segment().slug().equals(SLUG + "-segment");
    }

    /** A new attempt: no tells or volleys remembered. */
    void restart() {
        clocks.clear();
    }

    /** After each step: the heads' tells and volleys from the step's events; the gone chains forgotten. */
    void stepped(Sortie sortie) {
        if (!present()) {
            return;
        }
        clocks.keySet().removeIf(chain -> !alive(sortie, chain));
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            boolean tell = type == SimEvents.Type.EYE_GLINT && head(events.value(i));
            if (!tell && type != SimEvents.Type.ENEMY_FIRED) {
                continue;
            }
            Chain chain = chainAt(sortie, events.x(i), events.y(i));
            if (chain != null) {
                clocks.computeIfAbsent(chain, c -> new long[] {Long.MIN_VALUE / 2, Long.MIN_VALUE / 2})[tell ? 0 : 1] =
                        sortie.tick();
            }
        }
    }

    private static boolean alive(Sortie sortie, Chain chain) {
        for (int c = 0; c < sortie.chainCount(); c++) {
            if (sortie.chain(c) == chain) {
                return true;
            }
        }
        return false;
    }

    /** The eel chain whose head is at (x, y), or null. */
    private Chain chainAt(Sortie sortie, double x, double y) {
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            Enemy first = chain.member(0);
            if (first != null
                    && head(first.kind())
                    && Math.abs(first.renderX(1) - x) <= REACH
                    && Math.abs(first.renderY(1) - y) <= REACH) {
                return chain;
            }
        }
        return null;
    }

    /**
     * The stripe wave's progress at step {@code tick} for a tell that started at {@code tellTick}: 0 at its start to 1
     * at its end, NaN outside it.
     */
    static double wave(long tick, long tellTick, float alpha) {
        double age = (tick - tellTick + (double) alpha) / SimStep.PER_SECOND;
        return age >= 0 && age <= TELL_SECONDS ? age / TELL_SECONDS : Double.NaN;
    }

    /** Whether member {@code i} of a chain of {@code n} + 1 members (0 the head) is lit at the wave's {@code u}. */
    static boolean lit(int i, int n, double u) {
        return !Double.isNaN(u) && n > 0 && Math.abs(1 - (double) i / n - u) < WAVE;
    }

    /** The head's state frame: its shot for {@value #SHOT_TICKS} steps after a volley, its tell in a tell, else rest. */
    static int headState(long tick, long tellTick, long shotTick, float alpha) {
        if (tick - shotTick >= 0 && tick - shotTick < SHOT_TICKS) {
            return SHOT;
        }
        return Double.isNaN(wave(tick, tellTick, alpha)) ? 0 : TELL;
    }

    /** An eel chain from its tail to its head, each part at its heading and state, flashing white after a hit. */
    void drawChain(SpriteBatch batch, Sortie sortie, Chain chain, float alpha, BurrowerLooks.Flasher flasher) {
        long[] clock = clocks.get(chain);
        long tellTick = clock == null ? Long.MIN_VALUE / 2 : clock[0];
        long shotTick = clock == null ? Long.MIN_VALUE / 2 : clock[1];
        double u = wave(sortie.tick(), tellTick, alpha);
        int n = chain.size() - 1;
        for (int k = chain.size() - 1; k >= 0; k--) {
            Enemy member = chain.member(k);
            if (member == null) {
                continue;
            }
            int heading = EnemyLooks.heading(member.facing(), HEADINGS);
            int kind = role[member.kind()];
            AtlasRegion frame;
            if (kind == 1) {
                frame = head.get(
                        (heading * HEAD_STATES + headState(sortie.tick(), tellTick, shotTick, alpha)) % head.size);
            } else {
                Array<AtlasRegion> set = kind == 3 ? tail : segment;
                frame = set.get((heading * 2 + (lit(k, n, u) ? 1 : 0)) % set.size);
            }
            flasher.draw(batch, member, frame, member.renderX(alpha), member.renderY(alpha));
        }
    }

    /** After the grade: the lit parts' stripe bloom and the jaws' flash of every eel on the screen. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!present()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            if (!draws(chain)) {
                continue;
            }
            long[] clock = clocks.get(chain);
            if (clock == null) {
                continue;
            }
            double u = wave(sortie.tick(), clock[0], alpha);
            int n = chain.size() - 1;
            for (int k = 0; k < chain.size() && !stripe.isEmpty(); k++) {
                Enemy member = chain.member(k);
                if (member == null || !lit(k, n, u)) {
                    continue;
                }
                int heading = EnemyLooks.heading(member.facing(), HEADINGS);
                int part = role[member.kind()] == 1 ? PART_HEAD : role[member.kind()] == 3 ? PART_TAIL : PART_SEGMENT;
                PlayFrame.drawCentred(
                        batch,
                        stripe.get((heading * 3 + part) % stripe.size),
                        member.renderX(alpha),
                        member.renderY(alpha));
            }
            Enemy first = chain.member(0);
            long since = sortie.tick() - clock[1];
            if (first != null && head(first.kind()) && !muzzle.isEmpty() && since >= 0) {
                int frame = (int) (since / MUZZLE_FRAME_TICKS);
                if (frame < muzzle.size) {
                    int heading = EnemyLooks.heading(first.facing(), HEADINGS);
                    PlayFrame.drawCentred(
                            batch,
                            muzzle.get(frame),
                            first.renderX(alpha) + mouth[heading][0],
                            first.renderY(alpha) + mouth[heading][1]);
                }
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * A dead part's husk sinking, at the heading of the nearest live eel part to (x, y) (its neighbours in the chained
     * death; down without one): the head's or the body's; empty without the art.
     */
    Array<AtlasRegion> husk(Sortie sortie, int kind, double x, double y) {
        Array<Array<AtlasRegion>> set = head(kind) ? headHusks : husks;
        if (set.isEmpty()) {
            return new Array<>(0);
        }
        double best = Double.POSITIVE_INFINITY;
        double facing = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!part(enemy.kind())) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (d < best) {
                best = d;
                facing = enemy.facing();
            }
        }
        return set.get(EnemyLooks.heading(facing, HEADINGS));
    }
}
