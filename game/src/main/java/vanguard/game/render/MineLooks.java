package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.EnemySpec;
import vanguard.sim.Mine;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part C: the Minelayer's line mines and its laying (design/enemies/space/minelayer, Mine lines; Level 30;
 * tools/art/belt_units_c.py, whose docstring gives the sprites), a look only:
 *
 * <ul>
 *   <li>a line mine turns slowly through its 8 spin steps ({@code minelayer-mine}, the lamp dim): laid and unarmed it
 *       is dim and faint, rising to full as it arms ({@link Mine#rising()}); armed its lamp blinks once a second
 *       ({@code minelayer-mine-lit} with the additive {@code -glow} swelling and fading over it, {@link #ARMED_LIT_TICKS}
 *       steps); in its proximity blink ({@link Mine#blinking()}) it flashes faster and faster ({@link #blinkLit}); set
 *       off by a neighbour ({@link Mine#setOff()}) it is lit in full for its hop;
 *   <li>its proximity burst ({@code MINE_BURST} value 1): the white-gold flash and the blast's ring ({@code
 *       minelayer-mine-blast}, additive, the 40 px ring in frame 3); a mine set off in a chain ({@code MINE_CHAINED}), shot
 *       or touched: the short pop ({@code -pop}, additive, no ring);
 *   <li>the Minelayer laying: its drum's hatch glowing ({@code minelayer-laying}, its 16 headings) while it strafes
 *       across its lay row ({@code Enemy#laying()}).
 * </ul>
 */
final class MineLooks {
    static final String LAYER = "minelayer";
    static final String MINE = LAYER + "-mine";
    /** The mine's spin steps (8 turns over 90 degrees: its 4-fold spikes). */
    static final int SPIN = 8;
    /** A spin step shows this many steps (90 degrees in 1.6 s). */
    static final int SPIN_TICKS = 12;
    /** The armed lamp's blink: lit this many steps of every second. */
    static final int ARMED_LIT_TICKS = 9;
    /** A laid mine before it arms is drawn at least this opaque, rising to full with its arming. */
    static final float UNARMED_ALPHA = 0.55f;
    /** The proximity blink's flashes over its length, faster toward the burst. */
    static final int BLINK_FLASHES = 6;
    /** The blast's frames show this many steps each, the pop's too. */
    static final int BLAST_FRAME_TICKS = 3;

    static final int POP_FRAME_TICKS = 3;
    private static final int BURSTS = 32;

    private final Array<AtlasRegion> dim;
    private final Array<AtlasRegion> lit;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> blast;
    private final Array<AtlasRegion> pop;
    private final Array<AtlasRegion> laying;
    /** Whether the level's units lay line mines (its mine events are theirs). */
    private final boolean lines;
    /** The bursts: frames (null free), start step, where. */
    private final List<Array<AtlasRegion>> burst = new java.util.ArrayList<>(BURSTS);

    private final long[] burstTick = new long[BURSTS];
    private final double[] burstX = new double[BURSTS];
    private final double[] burstY = new double[BURSTS];
    private int nextBurst;

    MineLooks(Sprites sprites, List<EnemySpec> kinds) {
        this(sprites, kinds, false);
    }

    /** Step BOSS70: also with line mines when {@code thrown} (a ringed boss's mine ring, {@link #thrownMines}). */
    MineLooks(Sprites sprites, List<EnemySpec> kinds, boolean thrown) {
        Array<AtlasRegion> none = new Array<>();
        dim = sprites.has(MINE) ? sprites.frames(MINE) : none;
        lit = sprites.has(MINE + "-lit") ? sprites.frames(MINE + "-lit") : dim;
        glow = sprites.has(MINE + "-glow") ? sprites.frames(MINE + "-glow") : none;
        blast = sprites.has(MINE + "-blast") ? sprites.frames(MINE + "-blast") : none;
        pop = sprites.has(MINE + "-pop") ? sprites.frames(MINE + "-pop") : none;
        laying = sprites.has(LAYER + "-laying") ? sprites.frames(LAYER + "-laying") : none;
        lines = thrown || lineMines(kinds);
        for (int b = 0; b < BURSTS; b++) {
            burst.add(null);
        }
    }

    /** Step BOSS70: whether a set piece of {@code sortie} throws line mines (the Iron Sovereign's mine ring). */
    static boolean thrownMines(vanguard.sim.Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            boolean ring = sortie.setPiece(k)
                    .boss()
                    .flatMap(boss -> boss.rings())
                    .flatMap(rings -> rings.core())
                    .flatMap(core -> core.pool())
                    .flatMap(pool -> pool.mines())
                    .isPresent();
            if (ring) {
                return true;
            }
        }
        return false;
    }

    /** Whether any of {@code kinds} lays line mines. */
    static boolean lineMines(List<EnemySpec> kinds) {
        for (EnemySpec kind : kinds) {
            if (kind.gun()
                    .flatMap(gun -> gun.mine())
                    .flatMap(mine -> mine.line())
                    .isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** Whether the level's mines are line mines. */
    boolean lines() {
        return lines;
    }

    /** The level restarts: no bursts. */
    void restart() {
        java.util.Collections.fill(burst, null);
        Arrays.fill(burstTick, Long.MIN_VALUE / 2);
        nextBurst = 0;
    }

    /** After each simulation step: the line mines' bursts start their flash. */
    void stepped(Sortie sortie) {
        if (!lines) {
            return;
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            Array<AtlasRegion> frames =
                    switch (events.type(i)) {
                        case MINE_BURST -> events.value(i) == 1 ? blast : pop;
                        case MINE_CHAINED, MINE_DESTROYED -> pop;
                        default -> null;
                    };
            if (frames != null && frames.size > 0) {
                burst.set(nextBurst, frames);
                burstTick[nextBurst] = sortie.tick();
                burstX[nextBurst] = events.x(i);
                burstY[nextBurst] = events.y(i);
                nextBurst = (nextBurst + 1) % BURSTS;
            }
        }
    }

    /** The Minelayer's frame while it lays (its drum glowing), else null. */
    AtlasRegion layerFrame(vanguard.sim.Enemy enemy, int headings) {
        if (!enemy.laying() || laying.size != headings) {
            return null;
        }
        return laying.get(EnemyLooks.heading(enemy.facing(), headings));
    }

    /** The spin step of mine {@code index} at step {@code tick}. */
    static int spinFrame(long tick, int index) {
        return (int) Math.floorMod(tick / SPIN_TICKS + index * 3L, (long) SPIN);
    }

    /** Whether an armed mine's lamp is lit at step {@code tick}: the first steps of every second. */
    static boolean armedLit(long tick) {
        return Math.floorMod(tick, (long) SimStep.PER_SECOND) < ARMED_LIT_TICKS;
    }

    /** Whether a blinking mine's lamp is lit at {@code share} (0 to 1) of its blink: faster and faster flashes. */
    static boolean blinkLit(double share) {
        double phase = Math.sqrt(Math.clamp(share, 0, 1)) * BLINK_FLASHES;
        return phase - Math.floor(phase) < 0.5;
    }

    /** The glow frame of the armed blink at step {@code tick} (swell, full, fade), -1 when the lamp is dark. */
    static int armedGlow(long tick, int frames) {
        long t = Math.floorMod(tick, (long) SimStep.PER_SECOND);
        if (t >= ARMED_LIT_TICKS || frames == 0) {
            return -1;
        }
        return (int) Math.min(frames - 1, t * frames / ARMED_LIT_TICKS);
    }

    /**
     * The line mines (solid, the lamp's glow additive over a lit one) and their bursts (additive); the batch is in
     * normal blending and stays so.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!lines || dim.size < SPIN) {
            return;
        }
        long tick = sortie.tick();
        for (int i = 0; i < sortie.mineCount(); i++) {
            Mine mine = sortie.mine(i);
            if (!mine.line()) {
                continue;
            }
            int spin = spinFrame(tick, i);
            double x = mine.renderX(alpha);
            double y = mine.renderY(alpha);
            if (!mine.armed()) {
                batch.setColor(1, 1, 1, UNARMED_ALPHA + (1 - UNARMED_ALPHA) * (float) mine.rising());
                drawCentred(batch, dim.get(spin), x, y);
                batch.setColor(1, 1, 1, 1);
                continue;
            }
            drawCentred(batch, litNow(mine, tick) ? lit.get(spin) : dim.get(spin), x, y);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.mineCount() && glow.size > 0; i++) {
            Mine mine = sortie.mine(i);
            if (!mine.line() || !mine.armed()) {
                continue;
            }
            int frame;
            if (mine.blinking() || mine.setOff()) {
                frame = litNow(mine, tick) ? Math.min(1, glow.size - 1) : -1;
            } else {
                frame = armedGlow(tick, glow.size);
            }
            if (frame >= 0) {
                drawCentred(batch, glow.get(frame), mine.renderX(alpha), mine.renderY(alpha));
            }
        }
        for (int b = 0; b < BURSTS; b++) {
            Array<AtlasRegion> frames = burst.get(b);
            if (frames == null) {
                continue;
            }
            int step = frames == blast ? BLAST_FRAME_TICKS : POP_FRAME_TICKS;
            long age = tick - burstTick[b];
            if (age < 0 || age >= (long) frames.size * step) {
                continue;
            }
            drawCentred(batch, frames.get((int) (age / step)), burstX[b], burstY[b]);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static boolean litNow(Mine mine, long tick) {
        if (mine.setOff()) {
            return true;
        }
        if (mine.blinking()) {
            return blinkLit(mine.blinkShare());
        }
        return armedLit(tick);
    }
}
