package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.PlayField;
import vanguard.sim.RelayFlash;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part H, step H4: Level 35's mass-relay flash as the player sees it (design/campaign Level 35, The mass relay;
 * tools/art/l35_props.py's sprites, variant a until round 68's QH-2, with assets/pivots/mass-relay.json), following
 * the simulation's {@link RelayFlash}. Named apart from {@link RelayLooks} (Level 13's Nansen Relay, a defend
 * primary's ally). Over the telegraph the red line tiled across the field on the locked row ({@code -line}, its frame
 * by the telegraph's charge, additive) and the relay's point of light at the field's edge it fires from ({@code
 * -glow}, the same frame); live, the white-hot bolt tiled across ({@code -bolt} at 20 fps), the glow at full and a
 * full-field flash ({@link #FLASH}, its alpha falling 0.55 → 0.4 → 0 over 0.15 s; the Gameplay tab's flash
 * reduction tones it down); then the ion trail fading over 0.6 s ({@code -trail}). Nothing without a relay; nothing
 * is allocated per frame.
 */
final class MassRelayLooks {
    /** The sprites' root. */
    static final String ROOT = "mass-relay";
    /** The strips' tile width, px (32 px strips, 15 across the field). */
    static final int TILE = 32;
    /** The glow's place, px in from the edge the relay fires from (x 476 from the right edge). */
    static final double GLOW_IN = 4;
    /** The bolt alternates its two frames at 20 fps; the trail's six run over 0.6 s. */
    static final double BOLT_FPS = 20;

    static final double TRAIL_SECONDS = 0.6;
    /** The flash's colour and its alpha at 0, 0.05 and 0.15 s after the fire (the pivots' curve). */
    static final Color FLASH = new Color(255 / 255f, 236 / 255f, 222 / 255f, 1);

    static final double[][] FLASH_ALPHA = {{0.0, 0.55}, {0.05, 0.4}, {0.15, 0.0}};

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final TextureRegion pixel;
    private final Array<AtlasRegion> line;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> bolt;
    private final Array<AtlasRegion> trail;
    /** The step the bolt went live and its row (kept for the trail after it). */
    private long firedTick = NEVER;

    private double firedY;
    private boolean wasLive;

    private MassRelayLooks(Sprites sprites) {
        pixel = sprites.pixel;
        line = frames(sprites, ROOT + "-line");
        glow = frames(sprites, ROOT + "-glow");
        bolt = frames(sprites, ROOT + "-bolt");
        trail = frames(sprites, ROOT + "-trail");
    }

    /** The looks for a level whose script has a relay and whose atlas has its line; else null. */
    static MassRelayLooks of(Sprites sprites, vanguard.sim.LevelScript script) {
        if (script.hazards().relay().isEmpty() || !sprites.has(ROOT + "-line")) {
            return null;
        }
        return new MassRelayLooks(sprites);
    }

    private static Array<AtlasRegion> frames(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The level restarts: no trail. */
    void reset() {
        firedTick = NEVER;
        wasLive = false;
    }

    /** The telegraph line's (and the glow's) frame for the telegraph's {@code charge} (0..1), of {@code frames}. */
    static int telegraphFrame(double charge, int frames) {
        return Math.clamp((int) (charge * frames), 0, frames - 1);
    }

    /** The flash's alpha {@code seconds} after the fire: the pivots' curve, 0 outside it. */
    static float flashAlpha(double seconds) {
        for (int k = 0; k + 1 < FLASH_ALPHA.length; k++) {
            double s0 = FLASH_ALPHA[k][0];
            double s1 = FLASH_ALPHA[k + 1][0];
            if (seconds >= s0 && seconds < s1) {
                double a0 = FLASH_ALPHA[k][1];
                double a1 = FLASH_ALPHA[k + 1][1];
                return (float) (a0 + (a1 - a0) * (seconds - s0) / (s1 - s0));
            }
        }
        return 0;
    }

    /** The trail's frame {@code seconds} after the bolt ended, of {@code frames}; -1 once it has faded. */
    static int trailFrame(double seconds, int frames) {
        if (seconds < 0 || seconds >= TRAIL_SECONDS) {
            return -1;
        }
        return Math.min(frames - 1, (int) (seconds / TRAIL_SECONDS * frames));
    }

    /** After each simulation step: the bolt's start, for the flash and the trail. */
    void stepped(Sortie sortie) {
        RelayFlash relay = sortie.relay().orElse(null);
        if (relay == null) {
            return;
        }
        boolean live = relay.live();
        if (live && !wasLive) {
            firedTick = sortie.tick();
            firedY = relay.lineY();
        } else if (!relay.spent() && !relay.telegraphing() && !live) {
            // A restart before the flash.
            firedTick = NEVER;
        }
        wasLive = live;
    }

    /** With the bullets, additive: the telegraph's line and glow, the bolt and its glow, the trail. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        RelayFlash relay = sortie.relay().orElse(null);
        if (relay == null) {
            return;
        }
        long tick = sortie.tick();
        double glowX = relay.originX() > PlayField.WIDTH / 2.0 ? PlayField.WIDTH - GLOW_IN : GLOW_IN;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (relay.telegraphing() && !line.isEmpty()) {
            int frame = telegraphFrame(relay.charge(), line.size);
            tile(batch, line.get(frame), relay.lineY());
            if (!glow.isEmpty()) {
                PlayFrame.drawCentred(batch, glow.get(Math.min(glow.size - 1, frame)), glowX, relay.lineY());
            }
        } else if (relay.live() && !bolt.isEmpty()) {
            double since = Math.max(0, (tick - firedTick + alpha) * SimStep.SECONDS);
            tile(batch, bolt.get((int) (since * BOLT_FPS) % bolt.size), relay.lineY());
            if (!glow.isEmpty()) {
                PlayFrame.drawCentred(batch, glow.get(glow.size - 1), glowX, relay.lineY());
            }
        } else if (firedTick != NEVER && relay.spent() && !trail.isEmpty()) {
            double after =
                    (tick - firedTick + alpha) * SimStep.SECONDS - relay.spec().liveSeconds();
            int frame = trailFrame(after, trail.size);
            if (frame >= 0) {
                tile(batch, trail.get(frame), firedY);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Over the play field, after the units and shots: the flash as the bolt goes live, toned by {@code whiteFlash}. */
    void drawFlash(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        if (firedTick == NEVER || pixel == null || sortie.relay().isEmpty()) {
            return;
        }
        float a = flashAlpha((sortie.tick() - firedTick + alpha) * SimStep.SECONDS) * whiteFlash;
        if (a <= 0) {
            return;
        }
        batch.setColor(FLASH.r, FLASH.g, FLASH.b, a);
        batch.draw(pixel, X0, 0, PlayField.WIDTH, PlayField.HEIGHT);
        batch.setColor(Color.WHITE);
    }

    /** A strip tiled across the field, centred on row {@code y}. */
    private static void tile(SpriteBatch batch, AtlasRegion strip, double y) {
        float h = strip.getRegionHeight();
        float bottom = (float) Math.round(y - h / 2);
        for (int x = 0; x < PlayField.WIDTH; x += TILE) {
            batch.draw(strip, X0 + x, bottom);
        }
    }
}
