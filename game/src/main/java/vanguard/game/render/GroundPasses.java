package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_FLASH_TICKS;
import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * The ground objects for {@link LevelRenderer}'s passes: the loot targets and triggers (their frames,
 * hit flashes, beacons and trigger lights, foam collars and a sunken pod's rise), their glints, their
 * glows after the light pass, and the low-air triggers a lightning flash reveals under the banks.
 */
final class GroundPasses {
    /** The stuck sled's ore canister (Level 05): beacon dark, beacon lit, clamp shot. */
    private static final String ORE_CANISTER = "ore-canister";

    private static final int BEACON_BLINK_TICKS = 30;

    /** M5 part E: a foam collar's flicker loop and a wake's stream show a frame this many steps. */
    private static final int COLLAR_FRAME_TICKS = 8;

    /** M5 part E: a freed sunken pod rises a frame every 6 steps; its first four frames are under the water. */
    private static final int RISE_FRAME_TICKS = 6;

    private static final int RISE_UNDER_FRAMES = 4;

    /** The survey cache's markers twinkle at 10 fps while lit. */
    private static final int GLINT_TWINKLE_TICKS = 6;

    private static final int GLINT_PERIOD_TICKS = SimStep.ticks(2);

    private static final int GLINT_FRAME_TICKS = 3;

    /**
     * The destructible ground objects' frames (intact, damaged, wrecked) and the frames of the
     * triggers with a look of their own (Level 06's survey cache: closed, hit, opened; its terminal:
     * intact, released) by their look, looked up once.
     */
    private final Map<String, Array<AtlasRegion>> groundLooks = new HashMap<>();

    /** The looks of the triggers drawn with frames of their own instead of the beacon or trigger light. */
    private final Set<String> triggerLooks = new HashSet<>();

    /** M5 part E: a ground object's foam collar ({@code <look>-collar}, the floating containers), by its look. */
    private final Map<String, Array<AtlasRegion>> groundCollars = new HashMap<>();

    /** M5 part E: a sunken trigger's rise once freed ({@code <look>-rise}), by its look. */
    private final Map<String, Array<AtlasRegion>> groundRises = new HashMap<>();

    /**
     * A trigger look's {@code -glow} frames (the terminal's LEDs; the billboard's neon flicker, a loop
     * by {@link GroundGlow}), drawn after the light pass until it is spent.
     */
    private final Map<String, Array<AtlasRegion>> groundGlows = new HashMap<>();

    /**
     * A trigger look's {@code -glint} frames (the survey cache's markers), drawn after the light
     * pass only while the headlight or a flare lights it, until it is spent.
     */
    private final Map<String, Array<AtlasRegion>> groundGlints = new HashMap<>();

    /**
     * The light a level's triggers are drawn as when its backdrop has one (Level 03's lifeboat
     * rack: a lit lamp over the wreck's lens, off once shot); otherwise the crane beacon.
     */
    private final AtlasRegion triggerLight;

    private final Sprites sprites;
    private final FlashShader flash;
    private final PlayFrame play;
    private final WeatherLooks weather;

    GroundPasses(
            Sprites sprites,
            FlashShader flash,
            PlayFrame play,
            WeatherLooks weather,
            LevelScript script,
            String levelKey) {
        this.sprites = sprites;
        this.flash = flash;
        this.play = play;
        this.weather = weather;
        String light = Backdrop.folder(levelKey) + "lifeboat-light";
        triggerLight = sprites.hasBackdrop(light) ? sprites.backdrop(light, 1).first() : null;
        for (GroundObjectSpec spec : script.groundObjects()) {
            String look = spec.look();
            if (sprites.has(look + "-collar")) {
                groundCollars.computeIfAbsent(look, name -> sprites.frames(name + "-collar"));
            }
            if (spec.submerged() && sprites.has(look + "-rise")) {
                groundRises.computeIfAbsent(look, name -> sprites.frames(name + "-rise"));
            }
            if (!spec.trigger()) {
                groundLooks.computeIfAbsent(look, sprites::frames);
            } else if (!look.equals(GroundObjectSpec.CARGO_CONTAINER) && sprites.has(look)) {
                triggerLooks.add(look);
                groundLooks.computeIfAbsent(look, sprites::frames);
                String glow = sprites.has(look + "-glow")
                        ? look + "-glow"
                        : LevelRenderer.TRIGGER_CONTENTS.getOrDefault(look, look) + "-glow";
                if (sprites.has(glow)) {
                    groundGlows.computeIfAbsent(look, name -> sprites.frames(glow));
                }
                if (sprites.has(look + "-glint")) {
                    groundGlints.computeIfAbsent(look, name -> sprites.frames(name + "-glint"));
                }
            }
        }
    }

    /**
     * The loot targets, intact or damaged, a hit flashing white; the beacon blinks until spent, a
     * trigger light blinks until it is shot and is off then.
     */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (object.spec().submerged()) {
                // M5 part E: a sunken trigger lies on sub (drawSub); freed, its rise breaks the surface.
                drawRising(batch, object, alpha, false);
                continue;
            }
            boolean ownLook = object.spec().trigger()
                    && triggerLooks.contains(object.spec().look());
            if (object.spec().trigger() && !ownLook && triggerLight != null && !sledClamp(sortie, object)) {
                drawTriggerLight(batch, sortie, object, alpha);
                continue;
            }
            int damaged = object.damaged() ? 1 : 0;
            TextureRegion frame;
            if (ownLook) {
                // Its own frames: hit after the first hit (with three frames), the last once spent.
                Array<AtlasRegion> frames = groundLooks.get(object.spec().look());
                int hits = object.spec().hits();
                int state = TriggerBreak.door(object.spec().look()) && frames.size == 4
                        // M7 part C: the relay hut's door, sealed, hit, buckled, burst.
                        ? TriggerBreak.doorFrame(hits, object.hitsLeft(), object.spent())
                        : TriggerBreak.hitByHit(frames.size, hits)
                                // M5 part H: a frame per hit (Level 14's armoured car's creep cleared blob by blob).
                                ? TriggerBreak.hitFrame(hits, object.hitsLeft(), object.ticksSinceHit())
                                : object.spent() ? frames.size - 1 : frames.size > 2 ? damaged : 0;
                frame = frames.get(state);
            } else if (object.spec().trigger() && sledClamp(sortie, object)) {
                // The stuck sled (Level 05): the ore canister, its clamp's beacon lit while it can be
                // hit (the rail dark), the clamp shot open once spent.
                int state = object.spent() ? 2 : !object.shut() ? 1 : 0;
                frame = groundLooks
                        .computeIfAbsent(ORE_CANISTER, sprites::frames)
                        .get(state);
            } else if (object.spec().trigger()) {
                int lit = !object.spent() && sortie.tick() / BEACON_BLINK_TICKS % 2 == 0 ? 1 : 0;
                frame = sprites.beacon.get(2 * damaged + lit);
            } else {
                frame = groundLooks.get(object.spec().look()).get(damaged);
            }
            boolean heat = object.ticksSinceHit() < HIT_FLASH_TICKS
                    && Math.abs(object.renderX(alpha) - play.groundHeatX)
                            <= object.spec().size().width() / 2.0 + 1;
            if (object.ticksSinceHit() < HIT_FLASH_TICKS) {
                // M7 part A: under the beam a steady heat tint instead of a white silhouette.
                flash.draw(
                        batch,
                        frame,
                        Math.round(X0 + object.renderX(alpha)),
                        Math.round(object.renderY(alpha)),
                        heat ? ArsenalLooks.HEAT : HIT_WHITE,
                        heat ? ArsenalLooks.HEAT_AMOUNT : play.whiteFlash);
            } else {
                // M5 part F: a drifting trigger (Level 12's canister) moves sideways between the steps.
                drawCentred(batch, frame, object.renderX(alpha), object.renderY(alpha));
            }
            Array<AtlasRegion> collar = groundCollars.get(object.spec().look());
            if (collar != null && !object.spent()) {
                // M5 part E: an object afloat (the floating containers) in its foam collar.
                int phase = (int) object.renderX() % collar.size;
                drawCentred(
                        batch,
                        collar.get((int) ((sortie.tick() / COLLAR_FRAME_TICKS + phase) % collar.size)),
                        object.renderX(),
                        object.renderY(alpha));
            }
        }
    }

    /** Whether the trigger is the clamp of the level's stuck sled. */
    private static boolean sledClamp(Sortie sortie, GroundObject object) {
        return sortie.sled()
                .map(sled -> sled.spec().clampSecret().equals(object.spec().secret()))
                .orElse(false);
    }

    /** A trigger light: lit at the beacon's 1 Hz blink until shot, a white flash, then dark (the wreck's lens shows). */
    private void drawTriggerLight(SpriteBatch batch, Sortie sortie, GroundObject object, float alpha) {
        float x = Math.round(X0 + object.renderX());
        float y = Math.round(object.renderY(alpha));
        if (object.ticksSinceHit() < HIT_FLASH_TICKS) {
            flash.draw(batch, triggerLight, x, y, HIT_WHITE, play.whiteFlash);
        } else if (!object.spent() && sortie.tick() / BEACON_BLINK_TICKS % 2 == 0) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            drawCentred(batch, triggerLight, object.renderX(), object.renderY(alpha));
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /**
     * Each loot target sparkles at its top-left quarter (the key light's side) every ~2 s; not a
     * dark one (Level 06's survey cache), which only its markers' glint shows, while lit.
     */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (object.spec().dark()) {
                continue;
            }
            int phase = (int) object.renderX() * 7;
            int frame = (int) ((sortie.tick() + phase) % GLINT_PERIOD_TICKS / GLINT_FRAME_TICKS);
            if (!object.spent() && frame < sprites.glint.size) {
                drawCentred(
                        batch,
                        sprites.glint.get(frame),
                        object.renderX() - object.spec().size().width() / 4,
                        object.renderY(alpha) + object.spec().size().height() / 4);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * After the light pass, at full brightness: the triggers' glow frames (the data core terminal's
     * LEDs, the billboard's flicker looping by {@link GroundGlow}) until they are spent, and their
     * markers' glint (the survey cache's) only while the headlight or a flare lights them, so the
     * secret stays dark until found.
     */
    void drawGroundGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (groundGlows.isEmpty() && groundGlints.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (object.spent() || (weather.occludes() && object.spec().lowAir())) {
                // M5 part F: a low-air trigger's glow under occluding banks is drawn over them (drawHiddenGlows).
                continue;
            }
            Array<AtlasRegion> glow = groundGlows.get(object.spec().look());
            if (glow != null) {
                int frame = GroundGlow.frame(
                        sortie.tick(), glow.size, groundLooks.get(object.spec().look()).size, object.damaged());
                drawCentred(batch, glow.get(frame), object.renderX(), object.renderY(alpha));
            }
            Array<AtlasRegion> glint = groundGlints.get(object.spec().look());
            if (glint != null && sortie.lit(object)) {
                int frame = (int) (sortie.tick() / GLINT_TWINKLE_TICKS % glint.size);
                drawCentred(batch, glint.get(frame), object.renderX(), object.renderY(alpha));
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * M5 part E: a sunken trigger (the CDF supply pod snagged on a reef root): under the water
     * ({@code under}) its snagged, hit or freed frame and, once freed, the first steps of its rise;
     * on the surface the rise's last steps breaking it (the crate is the pickup's then).
     */
    private void drawRising(SpriteBatch batch, GroundObject object, float alpha, boolean under) {
        Array<AtlasRegion> frames = groundLooks.get(object.spec().look());
        double x = object.renderX();
        double y = object.renderY(alpha);
        if (under && frames != null) {
            int state = object.spent() ? frames.size - 1 : frames.size > 2 && object.damaged() ? 1 : 0;
            drawCentred(batch, frames.get(state), x, y);
        }
        Array<AtlasRegion> rise = groundRises.get(object.spec().look());
        if (rise == null || !object.spent()) {
            return;
        }
        int step = object.ticksSinceHit() / RISE_FRAME_TICKS;
        if (step < rise.size && (step < RISE_UNDER_FRAMES) == under) {
            drawCentred(batch, rise.get(step), x, y);
        }
    }

    /** M5 part E: the sunken triggers in the {@code sub} pass, under the water, and the first steps of a freed pod's rise. */
    void drawSunken(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (object.spec().submerged()) {
                drawRising(batch, object, alpha, true);
            }
        }
    }

    /** M5 part F, a lightning flash's reveal: the low-air triggers with frames of their own, at the batch's colour. */
    void drawRevealed(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            Array<AtlasRegion> frames = groundLooks.get(object.spec().look());
            if (object.spent()
                    || !object.spec().lowAir()
                    || frames == null
                    || !triggerLooks.contains(object.spec().look())) {
                continue;
            }
            int state = frames.size > 2 && object.damaged() ? 1 : 0;
            drawCentred(batch, frames.get(state), object.renderX(alpha), object.renderY(alpha));
        }
    }

    /** M5 part F, over the occluding banks (additive): a low-air trigger's own glow. */
    void drawHiddenGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            Array<AtlasRegion> glow = groundGlows.get(object.spec().look());
            if (object.spent() || !object.spec().lowAir() || glow == null) {
                continue;
            }
            int frame = GroundGlow.frame(
                    sortie.tick(), glow.size, groundLooks.get(object.spec().look()).size, object.damaged());
            drawCentred(batch, glow.get(frame), object.renderX(alpha), object.renderY(alpha));
        }
    }
}
