package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;
import vanguard.sim.Uplinks;

/**
 * M10 part G, step G4: Level 41's archive cores and Varga's uplink (design/campaign Level 41, the secondary {@code
 * FILES}; tools/art/l41_props.py's {@code archive-core*} sprites), following the simulation's {@link Uplinks}, a look
 * only. On the ground ({@link #drawGround}): a core's armoured hatch, sealed, then hit after its first hit (a hit
 * flashing it); shot open, the hatch's shards flung out ({@code -break}, {@value #BREAK_TICKS} steps a frame) over the
 * open core's well with its teal glass stack, which scrolls on with the ground. After the light pass, additive ({@link
 * #drawGlows}): an open core's teal light pulsing ({@code -glow}, {@value #GLOW_TICKS} steps a frame) and the uplink's
 * ring at the 40 px zone's edge, its faint track while the core is live, filling clockwise with the upload's share
 * ({@code -ring_0..31}); the file in, the full ring flaring and running out ({@code -done}, {@value #DONE_TICKS} steps a
 * frame). A lost core goes dark. Nothing is allocated per frame.
 */
final class UplinkLooks {
    /** The archive core's look: the hatch is a ground object of it, drawn here rather than by {@link GroundPasses}. */
    static final String CORE = "archive-core";
    /** The hatch's frames: sealed, hit, open. */
    static final int SEALED = 0;

    static final int HIT = 1;
    static final int OPEN = 2;
    static final int BREAK_TICKS = 2;
    static final int GLOW_TICKS = 8;
    static final int DONE_TICKS = 2;
    static final int HIT_FLASH_TICKS = 2;

    private static final long NEVER = Long.MIN_VALUE / 2;
    private static final int CORES = 8;

    private final FlashShader flash;
    private final Array<AtlasRegion> core;
    private final Array<AtlasRegion> breaking;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> ring;
    private final Array<AtlasRegion> done;
    /** Per core: its state seen last, the step it opened and the step its file came in. */
    private final int[] seen = new int[CORES];

    private final long[] openTick = new long[CORES];
    private final long[] doneTick = new long[CORES];

    private UplinkLooks(Sprites sprites, FlashShader flash) {
        this.flash = flash;
        core = optional(sprites, CORE);
        breaking = optional(sprites, CORE + "-break");
        glow = optional(sprites, CORE + "-glow");
        ring = optional(sprites, CORE + "-ring");
        done = optional(sprites, CORE + "-done");
        restart();
    }

    /** The level's uplink looks, or null for a level without archive cores (or without their sprites). */
    static UplinkLooks of(Sprites sprites, FlashShader flash, LevelScript script) {
        if (script.uplinks().isEmpty() || !sprites.has(CORE)) {
            return null;
        }
        return new UplinkLooks(sprites, flash);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Whether ground object {@code spec} is an archive core's hatch (drawn here). */
    static boolean drawn(GroundObjectSpec spec) {
        return CORE.equals(spec.look());
    }

    /** The level restarts (or is retried): every core shut again. */
    void restart() {
        Arrays.fill(seen, Uplinks.SHUT);
        Arrays.fill(openTick, NEVER);
        Arrays.fill(doneTick, NEVER);
    }

    /** The uplink's ring frame for an upload's {@code share} of {@code frames}: 0 the bare track, the last full. */
    static int ringFrame(double share, int frames) {
        if (frames <= 1) {
            return 0;
        }
        return Math.clamp(Math.round(share * (frames - 1)), 0, frames - 1);
    }

    /** After each step: when each core opened and when its file came in. */
    void stepped(Sortie sortie) {
        Uplinks uplinks = sortie.uplinks();
        if (uplinks == null) {
            return;
        }
        for (int c = 0; c < Math.min(CORES, uplinks.count()); c++) {
            int state = uplinks.state(c);
            if (state != seen[c]) {
                if (state == Uplinks.SHUT) {
                    // A retry or the halt's checkpoint: shut again.
                    openTick[c] = NEVER;
                    doneTick[c] = NEVER;
                } else if (seen[c] == Uplinks.SHUT) {
                    openTick[c] = sortie.tick();
                }
                if (state == Uplinks.IN) {
                    doneTick[c] = sortie.tick();
                }
                seen[c] = state;
            }
        }
    }

    /** On the ground: each shut core's hatch (a hit flashing it), each open one's well and the hatch's break. */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        Uplinks uplinks = sortie.uplinks();
        if (uplinks == null || core.size < 3) {
            return;
        }
        for (int c = 0; c < uplinks.count(); c++) {
            if (uplinks.state(c) == Uplinks.SHUT) {
                GroundObject hatch = hatch(sortie, uplinks, c);
                if (hatch == null) {
                    continue;
                }
                AtlasRegion frame = core.get(hatch.damaged() ? HIT : SEALED);
                if (hatch.ticksSinceHit() < HIT_FLASH_TICKS && flash != null) {
                    flash.draw(
                            batch,
                            frame,
                            Math.round(X0 + hatch.renderX(alpha)),
                            Math.round(hatch.renderY(alpha)),
                            HIT_WHITE,
                            whiteFlash);
                } else {
                    drawCentred(batch, frame, hatch.renderX(alpha), hatch.renderY(alpha));
                }
                continue;
            }
            double x = uplinks.x(c);
            double y = uplinks.renderY(c, alpha);
            drawCentred(batch, core.get(OPEN), x, y);
            if (c < CORES && openTick[c] != NEVER) {
                long f = (sortie.tick() - openTick[c]) / BREAK_TICKS;
                if (f >= 0 && f < breaking.size) {
                    drawCentred(batch, breaking.get((int) f), x, y);
                }
            }
        }
    }

    /** After the light pass, additive: the open cores' teal light and the uplink's ring; the file in, its flare. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        Uplinks uplinks = sortie.uplinks();
        if (uplinks == null) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long tick = sortie.tick();
        for (int c = 0; c < uplinks.count(); c++) {
            double x = uplinks.x(c);
            double y = uplinks.renderY(c, alpha);
            int state = uplinks.state(c);
            if (state == Uplinks.OPEN) {
                if (!glow.isEmpty()) {
                    drawCentred(batch, glow.get((int) Math.floorMod(tick / GLOW_TICKS + c, (long) glow.size)), x, y);
                }
                if (!ring.isEmpty()) {
                    drawCentred(batch, ring.get(ringFrame(uplinks.share(c), ring.size)), x, y);
                }
            } else if (state == Uplinks.IN && c < CORES && doneTick[c] != NEVER) {
                long f = (tick - doneTick[c]) / DONE_TICKS;
                if (f >= 0 && f < done.size) {
                    drawCentred(batch, done.get((int) f), x, y);
                } else if (!glow.isEmpty()) {
                    // Its file in, the core glows on, steady.
                    drawCentred(batch, glow.first(), x, y);
                }
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Core {@code c}'s hatch among the ground objects in play (its spec the script's), or null once gone. */
    private static GroundObject hatch(Sortie sortie, Uplinks uplinks, int c) {
        int index = uplinks.hatch(c);
        var objects = sortie.script().groundObjects();
        if (index < 0 || index >= objects.size()) {
            return null;
        }
        GroundObjectSpec spec = objects.get(index);
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (object.spec() == spec) {
                return object;
            }
        }
        return null;
    }
}
