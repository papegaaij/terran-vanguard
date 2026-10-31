package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Lightning;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3b (design/world/jovian, Hazards &amp; set pieces: lightning strikes, J3; tools/art/act6_gear.py
 * {@code lightning}, whose docstring gives the sprites): the told strikes of a level with them ({@link
 * Sortie#lightning()}), all additive, under the bullets:
 *
 * <ul>
 *   <li>the <b>tell</b>: the 40 px column's faint edges and its thin blue-white line ({@code lightning-tell_0..1}, a 48 ×
 *       64 tile repeated down the whole field, centred on the column), brightening from {@value #TELL_FROM} to full
 *       over the tell, its brighter frame 1 for the last {@value #LATE_SECONDS} s;
 *   <li>the <b>strike</b>: the bolt down the whole field ({@code lightning-strike_0..2}, 112 × 540 centred on the
 *       column, its top at the field's top), three shapes {@value #STRIKE_FRAME_TICKS} steps each through its live
 *       time, the last {@value #FADE_SECONDS} s fading out;
 *   <li>the <b>sky flash</b> at the tell's start (Level 12's flat tint, at most {@value WeatherLooks#SKY_SECONDS} s,
 *       softened by the flash-reduction option), and the banks seen through for {@value #REVEAL_SECONDS} s after it
 *       ({@link #reveal}: the foreground banks, Level 12's rule).
 * </ul>
 *
 * The tell and the live time are the simulation's state; the strike's frames and the flash run from its events
 * ({@code STRIKE_TOLD}, {@code STRIKE}: {@code StormEffects}), on the real steps. Nothing is allocated per frame.
 */
final class LightningLooks {
    /** The tell's line starts at this strength and brightens to full over the tell. */
    static final float TELL_FROM = 0.35f;
    /** The tell's brighter frame shows for this long before the strike. */
    static final double LATE_SECONDS = 0.4;
    /** The strike's shapes change every this many steps. */
    static final int STRIKE_FRAME_TICKS = 2;
    /** The strike fades out over its last this long. */
    static final double FADE_SECONDS = 0.1;
    /** A flash makes the banks see-through for this long (design: "for its 0.5 s"). */
    static final double REVEAL_SECONDS = 0.5;
    /** The banks are this see-through at the reveal's peak (Level 12's {@link WeatherLooks#SEE_THROUGH}). */
    static final float SEE_THROUGH = WeatherLooks.SEE_THROUGH;

    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    private final Array<AtlasRegion> tell;
    private final Array<AtlasRegion> strike;
    private final TextureRegion pixel;
    /** Per strike: the step it struck at (far in the past before). */
    private final long[] struck;
    /** The step the last tell started at (its sky flash and reveal), far in the past for none. */
    private long flashed;

    LightningLooks(Array<AtlasRegion> tell, Array<AtlasRegion> strike, TextureRegion pixel, int strikes) {
        this.tell = tell;
        this.strike = strike;
        this.pixel = pixel;
        struck = new long[strikes];
        restart();
    }

    /** The looks for a sortie with strikes; null without them or without their art. */
    static LightningLooks of(Sprites sprites, Sortie sortie) {
        Lightning lightning = sortie.lightning().orElse(null);
        if (lightning == null || !sprites.has(TELL) || !sprites.has(STRIKE)) {
            return null;
        }
        return new LightningLooks(sprites.frames(TELL), sprites.frames(STRIKE), sprites.pixel, lightning.count());
    }

    /** The art's names (tools/art/act6_gear.py). */
    static final String TELL = "lightning-tell";

    static final String STRIKE = "lightning-strike";

    /** The level restarts: nothing told, nothing struck. */
    void restart() {
        Arrays.fill(struck, Long.MIN_VALUE / 2);
        flashed = Long.MIN_VALUE / 2;
    }

    /** Strike {@code i}'s tell started at step {@code tick}: the sky flashes. */
    void told(int i, long tick) {
        flashed = tick;
    }

    /** Strike {@code i} struck at step {@code tick}. */
    void struck(int i, long tick) {
        if (i >= 0 && i < struck.length) {
            struck[i] = tick;
        }
    }

    /** The tell's frame {@code remaining} s before the strike: the brighter one for the last {@value #LATE_SECONDS} s. */
    static int tellFrame(double remaining) {
        return remaining <= LATE_SECONDS ? 1 : 0;
    }

    /** The tell's strength at {@code charge} of it (0..1). */
    static float tellAlpha(double charge) {
        return (float) (TELL_FROM + (1 - TELL_FROM) * Math.clamp(charge, 0, 1));
    }

    /** The strike's frame {@code age} steps into it. */
    static int strikeFrame(long age, int frames) {
        return (int) (Math.max(0, age) / STRIKE_FRAME_TICKS % Math.max(1, frames));
    }

    /** The strike's strength {@code age} steps into a strike live for {@code liveTicks}: full, then fading out. */
    static float strikeAlpha(long age, int liveTicks) {
        int fade = SimStep.ticks(FADE_SECONDS);
        long left = liveTicks - age;
        return left >= fade ? 1 : (float) Math.clamp((double) left / fade, 0, 1);
    }

    /** The sky flash's strength {@code age} steps after a tell started: 1 at once, 0 after the flash's length. */
    static float skyAt(long age) {
        double seconds = age * SimStep.SECONDS;
        return seconds < 0 || seconds >= WeatherLooks.SKY_SECONDS
                ? 0
                : (float) (1 - seconds / WeatherLooks.SKY_SECONDS);
    }

    /** How see-through the banks are at step {@code tick}: {@value #SEE_THROUGH} at a flash, gone over its reveal. */
    float reveal(long tick) {
        long age = tick - flashed;
        if (age < 0) {
            return 0;
        }
        return SEE_THROUGH * WeatherLooks.revealAt(age * SimStep.SECONDS, REVEAL_SECONDS);
    }

    /**
     * Under the bullets: each told column, each live strike, then the sky flash at {@code whiteFlash} (the flash
     * reduction's strength).
     */
    void draw(SpriteBatch batch, Sortie sortie, float whiteFlash) {
        Lightning lightning = sortie.lightning().orElse(null);
        if (lightning == null) {
            return;
        }
        long tick = sortie.tick();
        double tellSeconds = lightning.spec().tellSeconds();
        int liveTicks = Math.max(1, SimStep.ticks(lightning.spec().liveSeconds()));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < lightning.count(); i++) {
            if (lightning.told(i)) {
                double charge = lightning.charge(i);
                AtlasRegion tile = tell.get(Math.min(tell.size - 1, tellFrame((1 - charge) * tellSeconds)));
                batch.setColor(1, 1, 1, tellAlpha(charge));
                float left = (float) (X0 + lightning.x(i) - tile.getRegionWidth() / 2.0);
                for (int y = PlayField.HEIGHT; y > 0; y -= tile.getRegionHeight()) {
                    batch.draw(tile, left, y - tile.getRegionHeight());
                }
            } else if (lightning.live(i)) {
                long age = tick - struck[i];
                if (age < 0 || age >= liveTicks) {
                    age = 0;
                }
                AtlasRegion bolt = strike.get(strikeFrame(age, strike.size));
                batch.setColor(1, 1, 1, strikeAlpha(age, liveTicks));
                batch.draw(
                        bolt,
                        (float) (X0 + lightning.x(i) - bolt.getRegionWidth() / 2.0),
                        PlayField.HEIGHT - bolt.getRegionHeight());
            }
        }
        float sky = skyAt(tick - flashed) * WeatherLooks.SKY_STRENGTH * whiteFlash;
        if (sky > 0) {
            Color c = WeatherLooks.SKY;
            batch.setColor(c.r, c.g, c.b, sky);
            batch.draw(pixel, X0, 0, PlayField.WIDTH, PlayField.HEIGHT);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
