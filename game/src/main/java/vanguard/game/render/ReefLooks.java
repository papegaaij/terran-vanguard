package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Pits;
import vanguard.sim.Sortie;

/**
 * M11 part C, step C4 (design/campaign Level 45; design/enemies/ground/spawning-organ; tools/art/act7_reef.py's
 * {@code organ}): Level 45's hive reef on the play field, built with the first sortie (null in a level without its
 * organs or pits, {@link #of}).
 *
 * <ul>
 *   <li>The spawning organ keeps the Hive Node's frame layout ({@link EnemyLooks#irisFrame}: 6 iris states of 4 breath
 *       frames, its glow, creep, tatters, ichor ring and torn husk are its unit look's); before a Whirl Seed cluster its
 *       seed pods swell instead of its iris ({@code -swell}, 6 frames over the 0.5 s telegraph by how far it has run,
 *       its {@code -swell-glow} lighting them; held, the last), then burst ({@code -release}, 6 frames of {@value
 *       #RELEASE_FRAME_TICKS} steps, with its {@code -release-glow}) and breathe shut again while the iris would close.
 *       Its Skitter pair keeps the iris.
 *   <li>A unit rising out of a spawning pit ({@link Pits#rising}) is drawn from the ground's scale ({@value
 *       #GROUND_SCALE}, a pouncer's ground size against its leap's) to the play plane's over its 0.5 s rise, from the
 *       pit's dark to its own colours, its shadow sliding out with it.
 * </ul>
 *
 * <p>The pits' frames by their state are the backdrop's ({@link PitFrames}); the call organ is {@link StoryLooks}', the
 * courier's cyst and the call organ's roots are ground objects in their own frames, the spore clouds {@link
 * SporeLooks}'. Fixed tables: nothing is allocated per frame.
 */
final class ReefLooks {
    static final String ORGAN = "spawning-organ";
    static final int SWELL_FRAMES = 6;
    static final int RELEASE_FRAMES = 6;
    /** The burst's frames, steps each (0.3 s in all: tools/art/act7_reef.py). */
    static final int RELEASE_FRAME_TICKS = 3;
    /** A rising unit's size at the pit's mouth: the ground's scale against the play plane's (1 / 1.43). */
    static final float GROUND_SCALE = (float) (1 / EnemyLooks.LEAP_SCALES[EnemyLooks.LEAP_SCALES.length - 1]);
    /** A rising unit's shade at the pit's mouth (the throat's dark), easing to its own colours. */
    static final float GROUND_SHADE = 0.55f;
    /** The organs followed at once (never two on the screen; a margin). */
    private static final int UNITS = 8;

    /** Per enemy kind whether it is a spawning organ with the production frames. */
    private final boolean[] organs;

    private final Array<AtlasRegion> swell;
    private final Array<AtlasRegion> swellGlow;
    private final Array<AtlasRegion> release;
    private final Array<AtlasRegion> releaseGlow;
    /** The organs swelling now, and those that burst a cluster since (while their iris would close). */
    private final SerialClock swells = new SerialClock(UNITS);

    private final SerialClock released = new SerialClock(UNITS);
    private long tick;

    private ReefLooks(
            boolean[] organs,
            Array<AtlasRegion> swell,
            Array<AtlasRegion> swellGlow,
            Array<AtlasRegion> release,
            Array<AtlasRegion> releaseGlow) {
        this.organs = organs;
        this.swell = swell;
        this.swellGlow = swellGlow;
        this.release = release;
        this.releaseGlow = releaseGlow;
    }

    /** The reef's looks for {@code sortie}'s level; null without spawning organs (with their frames) or pits. */
    static ReefLooks of(Sprites sprites, Sortie sortie) {
        List<EnemySpec> kinds = sortie.enemyKinds();
        boolean[] organs = new boolean[kinds.size()];
        boolean frames = sprites.has(ORGAN + "-swell")
                && sprites.has(ORGAN + "-swell-glow")
                && sprites.has(ORGAN + "-release")
                && sprites.has(ORGAN + "-release-glow");
        boolean any = false;
        for (int k = 0; k < organs.length; k++) {
            organs[k] = frames && kinds.get(k).slug().equals(ORGAN);
            any |= organs[k];
        }
        if (!any && sortie.pits() == null) {
            return null;
        }
        Array<AtlasRegion> none = new Array<>(0);
        return new ReefLooks(
                organs,
                frames ? sprites.frames(ORGAN + "-swell") : none,
                frames ? sprites.frames(ORGAN + "-swell-glow") : none,
                frames ? sprites.frames(ORGAN + "-release") : none,
                frames ? sprites.frames(ORGAN + "-release-glow") : none);
    }

    /** A new attempt: no organ swelling or bursting. */
    void restart() {
        swells.clear();
        released.clear();
        tick = 0;
    }

    /** After each step: which organs swell, and which burst a cluster (from the step their swell ended). */
    void stepped(Sortie sortie) {
        tick = sortie.tick();
        swells.begin();
        released.begin();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!organ(enemy)) {
                continue;
            }
            int serial = enemy.serial();
            boolean closing = !enemy.irisOpening() && enemy.iris() > 0;
            // A cluster's release: the swell (seen last step) ended and the iris closes; kept while it closes.
            if (closing && (released.since(serial, -1) >= 0 || swells.since(serial, -1) >= 0)) {
                released.touch(serial, tick);
            }
            if (enemy.swelling()) {
                swells.touch(serial, tick);
            }
        }
        swells.end();
        released.end();
    }

    private boolean organ(Enemy enemy) {
        int kind = enemy.kind();
        return kind >= 0 && kind < organs.length && organs[kind];
    }

    // --- the frames ------------------------------------------------------------------------------------------------

    /** The swell's frame when its telegraph has run {@code share} (0 to 1; held, 1). */
    static int swellFrame(double share) {
        return Math.clamp((int) Math.floor(share * SWELL_FRAMES), 0, SWELL_FRAMES - 1);
    }

    /** The burst's frame {@code steps} steps after the release; -1 once it has played. */
    static int releaseFrame(long steps) {
        long frame = steps / RELEASE_FRAME_TICKS;
        return steps < 0 || frame >= RELEASE_FRAMES ? -1 : (int) frame;
    }

    /**
     * The organ's body frame in place of its iris's: its pods swelling, bursting, or breathing shut after the burst;
     * null for another unit, or an organ at its iris (idle, or its Skitters' iris).
     */
    AtlasRegion organFrame(Enemy enemy, EnemyLooks look) {
        return organ(enemy) ? frame(enemy, look.frames(), swell, release) : null;
    }

    /** The organ's glow frame in place of its iris's ({@link #organFrame}'s); null for the iris's. */
    AtlasRegion organGlow(Enemy enemy, EnemyLooks look) {
        return organ(enemy) && !look.glow().isEmpty() ? frame(enemy, look.glow(), swellGlow, releaseGlow) : null;
    }

    private AtlasRegion frame(
            Enemy enemy, Array<AtlasRegion> iris, Array<AtlasRegion> swelling, Array<AtlasRegion> bursting) {
        if (enemy.swelling()) {
            return swelling.get(Math.min(swelling.size - 1, swellFrame(enemy.iris())));
        }
        long since = released.since(enemy.serial(), Long.MIN_VALUE);
        if (since == Long.MIN_VALUE) {
            return null;
        }
        int burst = releaseFrame(tick - since);
        if (burst >= 0) {
            return bursting.get(Math.min(bursting.size - 1, burst));
        }
        return iris.get(EnemyLooks.irisFrame(0, tick, enemy.serial()));
    }

    // --- the rise out of a pit -------------------------------------------------------------------------------------

    /** Whether {@code enemy} is rising out of a spawning pit now. */
    static boolean rising(Sortie sortie, Enemy enemy) {
        Pits pits = sortie.pits();
        return pits != null && pits.rising(enemy);
    }

    /** A rising unit's scale at {@code share} (0 at the mouth, 1 risen): the ground's to the play plane's, eased. */
    static float riseScale(double share) {
        return GROUND_SCALE + (1 - GROUND_SCALE) * ease(share);
    }

    /** A rising unit's shade at {@code share}: the throat's dark to its own colours, eased. */
    static float riseShade(double share) {
        return GROUND_SHADE + (1 - GROUND_SHADE) * ease(share);
    }

    private static float ease(double share) {
        float s = (float) Math.clamp(share, 0, 1);
        return s * s * (3 - 2 * s);
    }

    /** Draws a unit rising out of its pit: {@code frame} at its rise's scale and shade. */
    static void drawRising(SpriteBatch batch, Sortie sortie, Enemy enemy, TextureRegion frame, float alpha) {
        double share = sortie.pits().rise(enemy, alpha);
        float shade = riseShade(share);
        batch.setColor(shade, shade, shade, 1);
        PlayFrame.drawScaled(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha), riseScale(share));
        batch.setColor(Color.WHITE);
    }

    /** A rising unit's shadow offset, a share of the air offset at {@code share} of its rise: it slides out, eased. */
    static float shadowSlide(double share) {
        return ease(share);
    }
}
