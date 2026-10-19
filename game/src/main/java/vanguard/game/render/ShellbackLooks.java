package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part D: the Shellback's own looks beyond its walk (design/enemies/ground/shellback; tools/art/siege_units.py,
 * whose docstring gives the sprites). Walking it is drawn by {@link EnemyLooks} (16 headings x 6 heavy-walk
 * frames, one cycle per stride) and dies into its husk ({@code -husk}, one per heading) there too; its mortar's
 * impact marker is the Polyp Mortar's (every lob's). Here: the curl ({@code -curl}, 16 headings x 4 frames over its
 * 0.6 s; the uncurl plays them backwards), the roll ({@code -roll}, 16 headings x 8 frames of 80 px, frame k = the
 * distance rolled / 3.7 px mod 8), the belly seam's lime glow after the uncurl ({@code -belly}, one per heading,
 * additive, fading over the window's last fifth), the vent's swell glow over the 0.5 s tell before a lob ({@code
 * -vent_0..2}, additive) and the destroyed vent ({@code -vent-dead}) for the rest of its life, both at the vent's
 * pivot per heading ({@code assets/pivots/shellback.json}); while it is curled a glancing hit flashes it grey (the
 * spark is the generic glance's, {@code SHOT_GLANCED}).
 */
final class ShellbackLooks {
    /** The art's headings: heading k turns k x 22.5 degrees clockwise from straight down. */
    static final int HEADINGS = 16;
    /** The curl's frames a heading, and the roll's. */
    static final int CURL_FRAMES = 4;

    static final int ROLL_FRAMES = 8;
    /** The roll turns a frame every this many px rolled (72 degrees over 8 frames on the ball's 34 px radius). */
    static final double ROLL_PX_PER_FRAME = 3.7;
    /** The walk frame's size, px: the pivots are px from its top left, y down. */
    static final double FRAME = 112;
    /** The mortar tell before a lob (the simulation's 0.5 s), in steps, over the vent's 3 glow frames. */
    static final int TELL_TICKS = SimStep.ticks(0.5);
    /** The belly glow fades out over the last share of its window. */
    static final double BELLY_FADE = 0.2;
    /** A glancing hit's grey flash on the curled shell, and how long it shows. */
    static final Color GLANCE_GREY = Color.valueOf("B8B8C0");

    static final int GLANCE_TICKS = 4;
    /** The tells and glances remembered at once (a level has a few Shellbacks on the screen at most). */
    private static final int SLOTS = 8;

    /** Per enemy kind its frames; null for a kind without a curl-roll (or without its art). */
    private final Frames[] frames;
    /** The units whose vent swells (their serials, -1 free) and the step their tell started. */
    private final int[] tellSerial = new int[SLOTS];

    private final long[] tellTick = new long[SLOTS];
    /** The units whose curled shell a hit glanced off and the step it did. */
    private final int[] glanceSerial = new int[SLOTS];

    private final long[] glanceTick = new long[SLOTS];
    private int nextTell;
    private int nextGlance;

    /**
     * A curl-roll unit's frames.
     *
     * @param vent per heading the vent's centre, px from the walk frame's centre, y up ({@code [heading][0]} x)
     */
    record Frames(
            Array<AtlasRegion> curl,
            Array<AtlasRegion> roll,
            Array<AtlasRegion> belly,
            Array<AtlasRegion> ventGlow,
            AtlasRegion ventDead,
            double[][] vent) {}

    ShellbackLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = new Frames[kinds.size()];
        for (int k = 0; k < frames.length; k++) {
            EnemySpec kind = kinds.get(k);
            String slug = kind.slug();
            if (kind.roll().isEmpty() || !sprites.has(slug + "-curl") || !sprites.has(slug + "-roll")) {
                continue;
            }
            frames[k] = new Frames(
                    sprites.frames(slug + "-curl"),
                    sprites.frames(slug + "-roll"),
                    sprites.has(slug + "-belly") ? sprites.frames(slug + "-belly") : new Array<>(),
                    sprites.has(slug + "-vent") ? sprites.frames(slug + "-vent") : new Array<>(),
                    sprites.has(slug + "-vent-dead") ? sprites.region(slug + "-vent-dead") : null,
                    ventOffsets(pivots.apply(slug)));
        }
        restart();
    }

    /**
     * The vent's centre per heading as px from the walk frame's centre, y up, from the pivot file's {@code vent}
     * (px from the 112 px frame's top left, y down); without the file the part's offset of the data is not known
     * here, so the frame's centre.
     */
    static double[][] ventOffsets(JsonValue pivots) {
        double[][] offsets = new double[HEADINGS][2];
        JsonValue vent = pivots == null ? null : pivots.get("vent");
        if (vent == null) {
            return offsets;
        }
        for (int h = 0; h < HEADINGS && h < vent.size; h++) {
            offsets[h][0] = vent.get(h).getDouble(0) - FRAME / 2;
            offsets[h][1] = FRAME / 2 - vent.get(h).getDouble(1);
        }
        return offsets;
    }

    /** The level restarts: no tell or glance shows. */
    void restart() {
        java.util.Arrays.fill(tellSerial, -1);
        java.util.Arrays.fill(glanceSerial, -1);
        nextTell = 0;
        nextGlance = 0;
    }

    /** Whether the kind has its curl-roll looks. */
    boolean has(int kind) {
        return kind >= 0 && kind < frames.length && frames[kind] != null;
    }

    /** After each simulation step: a vent's tell starts its swell, a glancing hit on a curled shell its grey flash. */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            if (type != SimEvents.Type.MORTAR_TELL && type != SimEvents.Type.SHOT_GLANCED) {
                continue;
            }
            Enemy unit = unitAt(sortie, events.x(i), events.y(i), type == SimEvents.Type.SHOT_GLANCED);
            if (unit == null) {
                continue;
            }
            if (type == SimEvents.Type.MORTAR_TELL) {
                tellSerial[nextTell] = unit.serial();
                tellTick[nextTell] = sortie.tick();
                nextTell = (nextTell + 1) % SLOTS;
            } else {
                glanceSerial[nextGlance] = unit.serial();
                glanceTick[nextGlance] = sortie.tick();
                nextGlance = (nextGlance + 1) % SLOTS;
            }
        }
    }

    /** The unit with these looks nearest (x, y), curled if {@code curled}; null when none is within its hit box. */
    private Enemy unitAt(Sortie sortie, double x, double y, boolean curled) {
        Enemy found = null;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind()) || (curled && !enemy.armoured())) {
                continue;
            }
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance < best && distance <= enemy.hitbox().width()) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    /** The curl frame's index {@code progress} into the curl (backwards for the uncurl) at {@code heading}. */
    static int curlFrame(int heading, double progress, boolean uncurl) {
        int frame = Math.clamp((int) Math.floor(progress * CURL_FRAMES), 0, CURL_FRAMES - 1);
        return heading * CURL_FRAMES + (uncurl ? CURL_FRAMES - 1 - frame : frame);
    }

    /** The roll frame's index after rolling {@code rolled} px along {@code heading}. */
    static int rollFrame(int heading, double rolled) {
        int frame = (int) Math.floorMod((long) Math.floor(rolled / ROLL_PX_PER_FRAME), (long) ROLL_FRAMES);
        return heading * ROLL_FRAMES + frame;
    }

    /** The vent's glow frame {@code age} steps into its tell; -1 outside it. */
    static int ventFrame(long age, int frames) {
        if (age < 0 || age >= TELL_TICKS || frames == 0) {
            return -1;
        }
        return Math.min(frames - 1, (int) (age * frames / TELL_TICKS));
    }

    /** The belly glow's opacity {@code progress} into the belly window: full, fading out over its last fifth. */
    static float bellyOpacity(double progress) {
        return (float) Math.clamp((1 - progress) / BELLY_FADE, 0, 1);
    }

    /** Whether the vent shows (the walk frames have it; the curl and the ball hide it). */
    static boolean ventShows(Enemy.CurlPhase phase) {
        return phase == Enemy.CurlPhase.WALK || phase == Enemy.CurlPhase.BELLY;
    }

    /** The unit's frame while it curls, rolls or uncurls; null while it walks or shows its belly (the walk frames). */
    AtlasRegion frame(Enemy enemy) {
        if (!has(enemy.kind())) {
            return null;
        }
        Frames own = frames[enemy.kind()];
        int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
        return switch (enemy.curlPhase()) {
            case CURLING -> own.curl().get(curlFrame(heading, enemy.curlProgress(), false));
            case UNCURLING -> own.curl().get(curlFrame(heading, enemy.curlProgress(), true));
            case ROLLING -> own.roll().get(rollFrame(heading, enemy.walked()));
            case WALK, BELLY -> null;
        };
    }

    /** Whether a hit glanced off the unit's curled shell in the last few steps (its grey flash). */
    boolean glancing(Enemy enemy, long tick) {
        if (!has(enemy.kind()) || !enemy.armoured()) {
            return false;
        }
        for (int s = 0; s < SLOTS; s++) {
            if (glanceSerial[s] == enemy.serial() && tick - glanceTick[s] < GLANCE_TICKS) {
                return true;
            }
        }
        return false;
    }

    /** The steps since the unit's latest tell started; -1 for none. */
    private long tellAge(Enemy enemy, long tick) {
        long age = -1;
        for (int s = 0; s < SLOTS; s++) {
            if (tellSerial[s] == enemy.serial() && (age < 0 || tick - tellTick[s] < age)) {
                age = tick - tellTick[s];
            }
        }
        return age;
    }

    /**
     * Over the ground units: the destroyed vents, then additively the vents' swell over their tell and the belly
     * seam's glow after an uncurl.
     */
    void drawOver(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind()) || !ventShows(enemy.curlPhase()) || enemy.partAlive()) {
                continue;
            }
            Frames own = frames[enemy.kind()];
            if (own.ventDead() != null) {
                drawAtVent(batch, own, own.ventDead(), enemy, alpha);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind()) || !ventShows(enemy.curlPhase())) {
                continue;
            }
            Frames own = frames[enemy.kind()];
            float glow = ArsenalLooks.glowShare(enemy);
            int vent = enemy.partAlive() ? ventFrame(tellAge(enemy, sortie.tick()), own.ventGlow().size) : -1;
            if (vent >= 0 && glow > 0) {
                batch.setColor(1, 1, 1, glow);
                drawAtVent(batch, own, own.ventGlow().get(vent), enemy, alpha);
            }
            if (enemy.curlPhase() == Enemy.CurlPhase.BELLY && !own.belly().isEmpty()) {
                batch.setColor(1, 1, 1, bellyOpacity(enemy.curlProgress()));
                AtlasRegion seam = own.belly().get(EnemyLooks.heading(enemy.facing(), own.belly().size));
                drawCentred(batch, seam, enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawAtVent(SpriteBatch batch, Frames own, AtlasRegion region, Enemy enemy, float alpha) {
        double[] at = own.vent()[EnemyLooks.heading(enemy.facing(), HEADINGS)];
        drawCentred(batch, region, enemy.renderX(alpha) + at[0], enemy.renderY(alpha) + at[1]);
    }

    private static void drawCentred(SpriteBatch batch, AtlasRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
