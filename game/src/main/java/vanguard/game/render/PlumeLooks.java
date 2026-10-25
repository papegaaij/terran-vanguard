package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.content.HazardsBlock;
import vanguard.content.LevelData;
import vanguard.sim.FurnaceVents;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part G: Level 27's heat plumes (design/campaign Level 27, Hazards; the vents' {@code look: heat-plume}; tools/art/
 * l27_props.py's {@code heat-plume*}), the furnace vents' cycle drawn as a vent on the sea floor. On the ground, under
 * the ground units ({@link #drawGround}): the mouth ({@code heat-plume_0..3}: cold, the tell's three steps by its share,
 * the last under the eruption). Over the ground units ({@link #drawOver}): the silt settling after a burst ({@code
 * -settle}, {@value #SETTLE_FPS} fps), over the tell the silt lifting off the mouth ({@code -silt}, {@value #SILT_FPS}
 * fps on the step clock) and its amber light ({@code -glow} by the tell's step, additive), over the burn the eruption
 * seen from above ({@code -column}, normal blending, its 16 frames spread over the burn) and its heat ({@code -heat},
 * additive, the same frame); then each bend's short water ripple where a bullet was turned ({@code -bend}, {@value
 * #BEND_FRAME_TICKS} steps a frame, from {@code PLUME_BEND}). The mouth's amber pool in the grade's light map is {@link
 * DarkLight}'s ({@link #poolLight}). Presentation only; nothing allocated per frame or step.
 */
final class PlumeLooks {
    /** The art's mouth states: 0 cold, 1-3 the tell's steps (the last also under the eruption). */
    static final int MOUTH_FRAMES = 4;
    /** The tell's glow steps, the eruption's frames. */
    static final int GLOW_FRAMES = 3;

    static final int COLUMN_FRAMES = 16;
    /** The silt's rate over the tell, the settling's after a burst (fps). */
    static final double SILT_FPS = 6;

    static final double SETTLE_FPS = 8;
    /** The bend's ripple: its frames show this many steps (15 fps). */
    static final int BEND_FRAME_TICKS = 4;
    /** The mouth's pool: its radius per disc radius, its colour (the vents' amber), its light cold to burning. */
    static final double POOL_REACH = 2.5;

    static final Color AMBER = new Color(1f, 0.63f, 0.25f, 1);
    static final float TELL_LIGHT_FROM = 0.25f;
    static final float TELL_LIGHT_TO = 0.7f;
    static final float BURN_LIGHT = 0.9f;
    /** The burn's light fades over its last this share of the burn, to this much of it. */
    static final double BURN_FADE_SHARE = 0.3;

    static final float BURN_FADE_TO = 0.35f;

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final Array<AtlasRegion> mouth;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> silt;
    private final Array<AtlasRegion> column;
    private final Array<AtlasRegion> heat;
    private final Array<AtlasRegion> settle;
    private final Array<AtlasRegion> bend;
    /** The bends' ripples on the play field. */
    private final Effects ripples = Effects.solid();
    /** Per grate the step its settling started (its burn ended) and the phase at the last step. */
    private long[] settleTick = new long[0];

    private FurnaceVents.Phase[] lastPhase = new FurnaceVents.Phase[0];

    private PlumeLooks(Sprites sprites) {
        String root = HazardsBlock.Vents.HEAT_PLUME;
        mouth = optional(sprites, root);
        glow = optional(sprites, root + "-glow");
        silt = optional(sprites, root + "-silt");
        column = optional(sprites, root + "-column");
        heat = optional(sprites, root + "-heat");
        settle = optional(sprites, root + "-settle");
        bend = optional(sprites, root + "-bend");
    }

    /** The level's plume looks, or null for a level whose vents are not heat plumes. */
    static PlumeLooks of(Sprites sprites, LevelData level) {
        return plumes(level) ? new PlumeLooks(sprites) : null;
    }

    /** Whether the level's vents are heat plumes ({@code look: heat-plume}). */
    static boolean plumes(LevelData level) {
        return level.vents()
                .map(vents -> vents.lookOrDefault().equals(HazardsBlock.Vents.HEAT_PLUME))
                .orElse(false);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    // --- the frames --------------------------------------------------------------------------------

    /** The mouth's frame: cold while idle, the tell's three steps by its share, the last under the eruption. */
    static int mouthFrame(FurnaceVents.Phase phase, double progress) {
        return switch (phase) {
            case IDLE -> 0;
            case TELL -> 1 + tellStep(progress);
            case BURN -> MOUTH_FRAMES - 1;
        };
    }

    /** The tell's step (0-2) {@code progress} into it. */
    static int tellStep(double progress) {
        return Math.clamp((int) Math.floor(progress * GLOW_FRAMES), 0, GLOW_FRAMES - 1);
    }

    /** The eruption's frame {@code progress} into its burn (16 frames over the burn: 10 fps over 1.6 s). */
    static int columnFrame(double progress) {
        return Math.clamp((int) Math.floor(progress * COLUMN_FRAMES), 0, COLUMN_FRAMES - 1);
    }

    /** The tell's silt frame at step {@code tick} for grate {@code g} (its own phase) of {@code count}. */
    static int siltFrame(long tick, int g, int count) {
        return (int) Math.floorMod((long) Math.floor(tick * SILT_FPS / SimStep.PER_SECOND) + 3L * g, (long) count);
    }

    /** The settling's frame {@code age} steps after its burn ended, of {@code count}; -1 once it has gone. */
    static int settleFrame(long age, int count) {
        if (age < 0) {
            return -1;
        }
        int frame = (int) Math.floor(age * SETTLE_FPS / SimStep.PER_SECOND);
        return frame < count ? frame : -1;
    }

    /**
     * The mouth's pool's light at its phase: none while cold, rising from {@value #TELL_LIGHT_FROM} to {@value
     * #TELL_LIGHT_TO} through the tell, {@value #BURN_LIGHT} through the eruption, fading over its last {@value
     * #BURN_FADE_SHARE} to {@value #BURN_FADE_TO} of it.
     */
    static float poolLight(FurnaceVents.Phase phase, double progress) {
        return switch (phase) {
            case IDLE -> 0;
            case TELL -> (float) (TELL_LIGHT_FROM + (TELL_LIGHT_TO - TELL_LIGHT_FROM) * Math.clamp(progress, 0, 1));
            case BURN -> {
                double left = Math.clamp((1 - progress) / BURN_FADE_SHARE, 0, 1);
                yield (float) (BURN_LIGHT * (BURN_FADE_TO + (1 - BURN_FADE_TO) * left));
            }
        };
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The level restarts: no silt settles, no ripple plays. */
    void restart() {
        Arrays.fill(settleTick, NEVER);
        Arrays.fill(lastPhase, FurnaceVents.Phase.IDLE);
        ripples.clear();
    }

    /** After each simulation step: a burn that ended starts its settling; each bend starts its ripple. */
    void stepped(Sortie sortie) {
        FurnaceVents vents = sortie.vents();
        if (settleTick.length != vents.grates()) {
            settleTick = new long[vents.grates()];
            lastPhase = new FurnaceVents.Phase[vents.grates()];
            restart();
        }
        for (int g = 0; g < vents.grates(); g++) {
            FurnaceVents.Phase phase = vents.phase(g);
            if (lastPhase[g] == FurnaceVents.Phase.BURN && phase != FurnaceVents.Phase.BURN) {
                settleTick[g] = sortie.tick();
            }
            lastPhase[g] = phase;
        }
        ripples.step();
        if (bend.isEmpty()) {
            return;
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.PLUME_BEND) {
                ripples.start(bend, BEND_FRAME_TICKS, events.x(e), events.y(e));
            }
        }
    }

    /** The bends' ripples playing (for the tests). */
    int ripples() {
        return ripples.size();
    }

    // --- drawing -----------------------------------------------------------------------------------

    /** On the ground, under the ground units: each mouth by its phase. */
    void drawGround(SpriteBatch batch, Sortie sortie, double scroll) {
        if (mouth.isEmpty()) {
            return;
        }
        FurnaceVents vents = sortie.vents();
        double lag = scroll - sortie.groundScroll();
        for (int g = 0; g < vents.grates(); g++) {
            double y = vents.y(g) - lag;
            if (vents.present(g) && onScreen(y)) {
                int frame = mouthFrame(vents.phase(g), vents.progress(g));
                drawCentred(batch, mouth.get(frame % mouth.size), vents.x(g), y);
            }
        }
    }

    private static boolean onScreen(double y) {
        return y > -64 && y < PlayField.HEIGHT + 64;
    }

    /**
     * Over the ground units: the settling silt, the tell's lifting silt, the eruption's column (normal blending), then
     * the tell's glow and the eruption's heat (additive), then the bends' ripples.
     */
    void drawOver(SpriteBatch batch, Sortie sortie, double scroll) {
        FurnaceVents vents = sortie.vents();
        if (vents.grates() == 0) {
            return;
        }
        double lag = scroll - sortie.groundScroll();
        long tick = sortie.tick();
        for (int g = 0; g < vents.grates(); g++) {
            double y = vents.y(g) - lag;
            if (!vents.present(g) || !onScreen(y)) {
                continue;
            }
            double x = vents.x(g);
            int settled = g < settleTick.length ? settleFrame(tick - settleTick[g], settle.size) : -1;
            if (settled >= 0 && vents.phase(g) == FurnaceVents.Phase.IDLE) {
                drawCentred(batch, settle.get(settled), x, y);
            }
            switch (vents.phase(g)) {
                case TELL -> {
                    if (!silt.isEmpty()) {
                        drawCentred(batch, silt.get(siltFrame(tick, g, silt.size)), x, y);
                    }
                }
                case BURN -> {
                    if (!column.isEmpty()) {
                        drawCentred(batch, column.get(columnFrame(vents.progress(g)) % column.size), x, y);
                    }
                }
                case IDLE -> {}
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int g = 0; g < vents.grates(); g++) {
            double y = vents.y(g) - lag;
            if (!vents.present(g) || !onScreen(y)) {
                continue;
            }
            double x = vents.x(g);
            switch (vents.phase(g)) {
                case TELL -> {
                    if (!glow.isEmpty()) {
                        drawCentred(batch, glow.get(tellStep(vents.progress(g)) % glow.size), x, y);
                    }
                }
                case BURN -> {
                    if (!heat.isEmpty()) {
                        drawCentred(batch, heat.get(columnFrame(vents.progress(g)) % heat.size), x, y);
                    }
                }
                case IDLE -> {}
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        ripples.draw(batch, 0);
    }
}
