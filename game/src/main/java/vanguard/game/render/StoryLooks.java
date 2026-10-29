package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.LevelData;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;

/**
 * M9 part G: a level's story pickup in its own frames, named by its {@code story} (design/campaign Level 34, The
 * freighter: the Helix core; tools/art/belt_units_g.py, whose docstring gives the sprites). While the group that holds
 * it stands (the hold's four clamps) the shut crate lies at the group's centre on the ground, the seal's dim glow
 * pulsing at about 1 Hz ({@code <story>}, 2 frames; the hold in the backdrop is empty); freed, it rises out of the hold
 * over {@link StoryPickups#RISE_SECONDS} ({@code -rise}, 10 frames by {@link StoryPickups#riseShare}, the crate growing
 * to 1.2) with its glow ({@code -glow} 0–9, additive); loose, it drifts as a pickup ({@code -drift}, 8 fps) in the glow's
 * loop ({@code -glow} 10–15, 6 fps), blinking at the end of its life as every pickup does. Collected or missed, nothing
 * is left (the miss is radio only, QG-2 a).
 */
final class StoryLooks {
    /** The seal's glow in the hold: each of its 2 frames 0.5 s. */
    static final int SHUT_FRAME_TICKS = SimStep.ticks(0.5);
    /** The rise's frames (and the glow's first ten). */
    static final int RISE_FRAMES = 10;
    /** The drift loop at 8 fps, the glow's loop (frames 10–15) at 6 fps. */
    static final double DRIFT_FPS = 8;

    static final double GLOW_LOOP_FPS = 6;
    /** A loose pickup blinks over its last 1.5 s, as {@link PropPasses}' pickups. */
    private static final int BLINK_TICKS = SimStep.ticks(1.5);

    private final Array<AtlasRegion> shut;
    private final Array<AtlasRegion> rise;
    private final Array<AtlasRegion> drift;
    private final Array<AtlasRegion> glow;
    /** The group's members (the clamps), their places on the ground once seen (ground y: y + the ground scroll). */
    private final GroundObjectSpec[] members;

    private final double[] groundY;
    private final boolean[] seen;
    private final double centreX;

    private StoryLooks(
            Array<AtlasRegion> shut,
            Array<AtlasRegion> rise,
            Array<AtlasRegion> drift,
            Array<AtlasRegion> glow,
            List<GroundObjectSpec> members) {
        this.shut = shut;
        this.rise = rise;
        this.drift = drift;
        this.glow = glow;
        this.members = members.toArray(GroundObjectSpec[]::new);
        groundY = new double[this.members.length];
        seen = new boolean[this.members.length];
        centreX = members.stream().mapToDouble(GroundObjectSpec::x).average().orElse(0);
    }

    /**
     * The looks of the level's story pickup, empty without one (or without its frames): its name from the level's
     * {@code pickups} ({@code story}), its group the destructibles that drop a {@link PickupType#STORY}.
     */
    static Optional<StoryLooks> of(Sprites sprites, LevelData level, List<GroundObjectSpec> objects) {
        Optional<String> story = level.pickups().stream()
                .flatMap(pickup -> pickup.story().stream())
                .findFirst();
        if (story.isEmpty()) {
            return Optional.empty();
        }
        String name = story.get();
        if (!sprites.has(name) || !sprites.has(name + "-rise") || !sprites.has(name + "-drift")) {
            return Optional.empty();
        }
        List<GroundObjectSpec> members = new ArrayList<>();
        for (GroundObjectSpec spec : objects) {
            if (spec.groupDrop().filter(drop -> drop == PickupType.STORY).isPresent()) {
                members.add(spec);
            }
        }
        if (members.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new StoryLooks(
                sprites.frames(name),
                sprites.frames(name + "-rise"),
                sprites.frames(name + "-drift"),
                sprites.has(name + "-glow") ? sprites.frames(name + "-glow") : new Array<>(0),
                members));
    }

    /** A new attempt: no member seen yet. */
    void restart() {
        java.util.Arrays.fill(seen, false);
    }

    // --- the frames ------------------------------------------------------------------------------------------------

    /** The shut crate's frame {@code tick} steps into the level: the seal's glow, 2 frames at about 1 Hz. */
    static int shutFrame(long tick, int frames) {
        return (int) (tick / SHUT_FRAME_TICKS % Math.max(1, frames));
    }

    /** The rise's frame at {@code share} (0 to 1) of the rise. */
    static int riseFrame(double share) {
        return Math.clamp((int) (share * RISE_FRAMES), 0, RISE_FRAMES - 1);
    }

    /** The drift loop's frame {@code tick} steps into the level. */
    static int driftFrame(long tick, int frames) {
        return (int) ((long) (tick * DRIFT_FPS / SimStep.PER_SECOND) % Math.max(1, frames));
    }

    /** The glow's frame in the drift: its loop (frames 10 to 15) at 6 fps; -1 without the loop's frames. */
    static int glowLoopFrame(long tick, int frames) {
        int loop = frames - RISE_FRAMES;
        if (loop <= 0) {
            return -1;
        }
        return RISE_FRAMES + (int) ((long) (tick * GLOW_LOOP_FPS / SimStep.PER_SECOND) % loop);
    }

    // --- following the simulation ----------------------------------------------------------------------------------

    /**
     * After each simulation step: each member on the screen fixes its place on the ground; one not come yet is
     * estimated from its entry time at the ground's speed (the crate is drawn from the first member's entry on).
     */
    void stepped(Sortie sortie) {
        double scroll = sortie.groundScroll();
        for (int m = 0; m < members.length; m++) {
            GroundObject object = live(sortie, members[m]);
            if (object != null) {
                groundY[m] = object.renderY(1) + scroll;
                seen[m] = true;
            } else if (!seen[m]) {
                double ahead = Math.max(0, members[m].t() - sortie.levelSeconds()) * sortie.groundSpeed();
                groundY[m] = PlayField.HEIGHT + members[m].size().height() / 2 + scroll + ahead;
            }
        }
    }

    private static GroundObject live(Sortie sortie, GroundObjectSpec spec) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (object.spec() == spec) {
                return object;
            }
        }
        return null;
    }

    // --- drawing ---------------------------------------------------------------------------------------------------

    /** On the ground, under its group: the shut crate at the group's centre while the group stands. */
    void drawHold(SpriteBatch batch, Sortie sortie, float alpha) {
        StoryPickups story = sortie.storyPickup().orElse(null);
        if (story == null || story.state() != StoryPickups.State.WAITING) {
            return;
        }
        int ref = -1;
        GroundObject anchor = null;
        double mean = 0;
        for (int m = 0; m < members.length; m++) {
            mean += groundY[m];
            if (anchor == null) {
                anchor = live(sortie, members[m]);
                ref = m;
            }
        }
        if (anchor == null) {
            return;
        }
        mean /= members.length;
        double y = anchor.renderY(alpha) + mean - groundY[ref];
        drawCentred(batch, shut.get(shutFrame(sortie.tick(), shut.size)), centreX, y);
    }

    /** With the pickups: the crate rising with its glow, or drifting loose in its glow's loop. */
    void drawAir(SpriteBatch batch, Sortie sortie, float alpha) {
        StoryPickups story = sortie.storyPickup().orElse(null);
        if (story == null) {
            return;
        }
        long tick = sortie.tick();
        if (story.rising()) {
            int frame = riseFrame(story.riseShare(alpha));
            double x = story.renderX();
            double y = story.renderY(alpha);
            drawGlow(batch, frame < glow.size ? frame : -1, x, y);
            drawCentred(batch, rise.get(Math.min(frame, rise.size - 1)), x, y);
            return;
        }
        if (story.state() != StoryPickups.State.LOOSE) {
            return;
        }
        for (int i = 0; i < sortie.pickupCount(); i++) {
            Pickup pickup = sortie.pickup(i);
            if (pickup.type() != PickupType.STORY) {
                continue;
            }
            if (pickup.ticksLeft() < BLINK_TICKS && pickup.ticksLeft() / 6 % 2 == 0) {
                return;
            }
            double x = pickup.renderX(alpha);
            double y = pickup.renderY(alpha);
            drawGlow(batch, glowLoopFrame(tick, glow.size), x, y);
            drawCentred(batch, drift.get(driftFrame(tick, drift.size)), x, y);
            return;
        }
    }

    private void drawGlow(SpriteBatch batch, int frame, double x, double y) {
        if (frame < 0 || frame >= glow.size) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawCentred(batch, glow.get(frame), x, y);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
