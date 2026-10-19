package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M7 part D: the Choir Herald's {@code aura} as the player sees it (design/enemies/air/choir-herald; Q.22g;
 * tools/art/siege_units.py, whose docstring gives the sprites), all of it additive: on each ringed unit a thin
 * segmented violet ring ({@code choir-herald-ring_0..6}, frame k = k of its 6 segments lit, so it thins with the
 * ring's share; scaled to the unit's hit box), a white-violet flash widening over it as it absorbs a hit ({@code
 * -ring-flash}); at its Herald's death the ring shrinks and fades over the collapse (0.3 s). The Herald itself
 * shows the faint dashed circle of its radius ({@code -aura}, scaled from the art's 90 px to the difficulty's
 * radius, pulsing), a thread of light to every unit it feeds ({@code -thread}, a 16 px strip tiled along the line,
 * its motes running toward the unit at 12 fps) and the bloom of its halo crest over its body ({@code -crest}, a
 * 0-1-2-3-2-1 pulse at 6 fps). While the EMP Burst stuns it its crest is off and its circle and threads dim (its
 * aura waits).
 */
final class AuraLooks {
    /** The ring's segments: frame k of {@code -ring} lights k of them (0 the faint empty track). */
    static final int SEGMENTS = 6;
    /** The ring's radius in its 64 px frame (the art's), px. */
    static final double ART_RING_RADIUS = 26;
    /** A ring lies this far outside its unit's hit box, px, and is never smaller than {@link #MIN_RING_RADIUS}. */
    static final double RING_MARGIN = 4;

    static final double MIN_RING_RADIUS = 18;
    /** The radius circle's radius in its 184 px frame (the art's 90 px aura at medium). */
    static final double ART_AURA_RADIUS = 90;
    /** The circle pulses between these opacities over {@value #AURA_PULSE_SECONDS} s. */
    static final float AURA_LOW = 0.45f;

    static final float AURA_HIGH = 0.9f;
    static final double AURA_PULSE_SECONDS = 1.6;
    /** While its Herald is EMP-stunned its circle and threads show at this share. */
    static final float STUNNED = 0.3f;
    /** The thread's tile length, px, and its animation: 4 frames at 12 fps (5 steps a frame). */
    static final int THREAD_TILE = 16;

    static final int THREAD_FRAME_TICKS = 5;
    /** The crest's pulse at 6 fps (10 steps a frame) through its frames 0-1-2-3-2-1. */
    static final int CREST_FRAME_TICKS = 10;
    /** The absorb flash's frames, 3 steps each (0.15 s for the art's 3). */
    static final int FLASH_FRAME_TICKS = 3;
    /** A collapsing ring shrinks to this share of its radius as it fades out. */
    static final double COLLAPSE_SHRINK = 0.35;
    /** The absorb flashes playing at once (a unit absorbs at most once in 6 steps, the sim's spacing). */
    private static final int FLASHES = 16;

    private final boolean[] heralds;
    private final Array<AtlasRegion> ring;
    private final Array<AtlasRegion> flash;
    private final Array<AtlasRegion> thread;
    private final Array<AtlasRegion> crest;
    private final AtlasRegion aura;
    /** The flashes: the unit's serial and the step it absorbed at; serial -1 for a free slot. */
    private final int[] flashSerial = new int[FLASHES];

    private final long[] flashTick = new long[FLASHES];
    private int nextFlash;

    private AuraLooks(
            boolean[] heralds,
            Array<AtlasRegion> ring,
            Array<AtlasRegion> flash,
            Array<AtlasRegion> thread,
            Array<AtlasRegion> crest,
            AtlasRegion aura) {
        this.heralds = heralds;
        this.ring = ring;
        this.flash = flash;
        this.thread = thread;
        this.crest = crest;
        this.aura = aura;
        restart();
    }

    /**
     * The looks for the level's enemy kinds: the first kind with an {@code aura} names the sprites ({@code
     * <slug>-ring}, …); null for a level without one, or while its art is missing.
     */
    static AuraLooks of(Sprites sprites, List<EnemySpec> kinds) {
        boolean[] heralds = heralds(kinds);
        String slug = null;
        for (int k = 0; k < kinds.size() && slug == null; k++) {
            slug = heralds[k] ? kinds.get(k).slug() : null;
        }
        if (slug == null || !sprites.has(slug + "-ring") || !sprites.has(slug + "-aura")) {
            return null;
        }
        return new AuraLooks(
                heralds,
                sprites.frames(slug + "-ring"),
                sprites.has(slug + "-ring-flash") ? sprites.frames(slug + "-ring-flash") : new Array<>(),
                sprites.has(slug + "-thread") ? sprites.frames(slug + "-thread") : new Array<>(),
                sprites.has(slug + "-crest") ? sprites.frames(slug + "-crest") : new Array<>(),
                sprites.region(slug + "-aura"));
    }

    /** Which of the level's enemy kinds sing an aura (the Choir Herald). */
    static boolean[] heralds(List<EnemySpec> kinds) {
        boolean[] found = new boolean[kinds.size()];
        for (int k = 0; k < found.length; k++) {
            found[k] = kinds.get(k).aura().isPresent();
        }
        return found;
    }

    /** The level restarts: no flash plays. */
    void restart() {
        java.util.Arrays.fill(flashSerial, -1);
        nextFlash = 0;
    }

    /** After each simulation step: a ring that absorbed a hit flashes ({@link SimEvents.Type#RING_ABSORBED}). */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) != SimEvents.Type.RING_ABSORBED) {
                continue;
            }
            Enemy unit = ringedAt(sortie, events.x(i), events.y(i));
            if (unit != null) {
                flashSerial[nextFlash] = unit.serial();
                flashTick[nextFlash] = sortie.tick();
                nextFlash = (nextFlash + 1) % FLASHES;
            }
        }
    }

    /** The ringed unit nearest (x, y), where a ring absorbed; null when none is within its hit box. */
    private static Enemy ringedAt(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.ringMax() <= 0) {
                continue;
            }
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance < best
                    && distance <= enemy.hitbox().width() + enemy.hitbox().height()) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    /** Whether a unit shows a ring now: fed by a Herald (full, partly spent or empty), or collapsing. */
    static boolean ringed(Enemy enemy) {
        return enemy.ringMax() > 0 && (enemy.ringFeeder() >= 0 || enemy.ringCollapse() > 0);
    }

    /** The ring's frame at its share: k of the 6 segments lit, any absorb left lighting at least one. */
    static int segments(double share, int frames) {
        int lit = (int) Math.ceil(Math.clamp(share, 0, 1) * SEGMENTS - 1e-9);
        return Math.clamp(lit, 0, frames - 1);
    }

    /** The ring's radius round a unit of hit box {@code width} x {@code height}, px. */
    static double ringRadius(double width, double height) {
        return Math.max(MIN_RING_RADIUS, Math.max(width, height) / 2 + RING_MARGIN);
    }

    /** The radius circle's opacity at {@code seconds} (its pulse). */
    static float auraOpacity(double seconds) {
        double wave = 0.5 + 0.5 * Math.sin(2 * Math.PI * seconds / AURA_PULSE_SECONDS);
        return (float) (AURA_LOW + (AURA_HIGH - AURA_LOW) * wave);
    }

    /** The crest's frame at {@code tick} through its ping-pong pulse over {@code frames} (0-1-2-3-2-1 for 4). */
    static int crestFrame(long tick, int phase, int frames) {
        if (frames <= 1) {
            return 0;
        }
        int cycle = 2 * (frames - 1);
        int step = (int) Math.floorMod(tick / CREST_FRAME_TICKS + phase, (long) cycle);
        return step < frames ? step : cycle - step;
    }

    /** The absorb flash's frame {@code age} steps after the absorb; -1 once it has played out. */
    static int flashFrame(long age, int frames) {
        int frame = (int) (age / FLASH_FRAME_TICKS);
        return age < 0 || frame >= frames ? -1 : frame;
    }

    /**
     * The rings of the units on one side of the layers ({@code ground}: the ground units, drawn with them; else
     * the flyers), additive, each with its absorb flash.
     */
    void drawRings(SpriteBatch batch, Sortie sortie, float alpha, boolean ground) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!ringed(enemy)
                    || enemy.cloaked()
                    || (LevelRenderer.Depth.of(enemy) == LevelRenderer.Depth.GROUND) != ground) {
                continue;
            }
            double collapse = enemy.ringCollapse();
            float scale =
                    (float) (ringRadius(enemy.hitbox().width(), enemy.hitbox().height())
                            / ART_RING_RADIUS
                            * (1 - COLLAPSE_SHRINK * collapse));
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            batch.setColor(1, 1, 1, (float) (1 - collapse));
            drawScaled(batch, ring.get(segments(enemy.ringShare(), ring.size)), x, y, scale, 0);
            int frame = flash.isEmpty() ? -1 : flashFrame(sortie.tick() - flashTick(enemy.serial()), flash.size);
            if (frame >= 0) {
                batch.setColor(Color.WHITE);
                drawScaled(batch, flash.get(frame), x, y, scale, 0);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The step of the unit's latest absorb flash; far in the past for none. */
    private long flashTick(int serial) {
        long latest = Long.MIN_VALUE / 2;
        for (int f = 0; f < FLASHES; f++) {
            if (flashSerial[f] == serial) {
                latest = Math.max(latest, flashTick[f]);
            }
        }
        return latest;
    }

    /**
     * Under the flyers: each Herald's pulsing radius circle and its threads to the units it feeds, additive (dim
     * while it is stunned).
     */
    void drawAuras(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int h = 0; h < sortie.enemyCount(); h++) {
            Enemy herald = sortie.enemy(h);
            if (!herald(herald)) {
                continue;
            }
            float share = herald.empStunned() ? STUNNED : 1;
            double hx = herald.renderX(alpha);
            double hy = herald.renderY(alpha);
            double radius = herald.spec().aura().orElseThrow().radius();
            batch.setColor(1, 1, 1, auraOpacity(seconds + herald.serial() * 0.37) * share);
            drawScaled(batch, aura, hx, hy, (float) (radius / ART_AURA_RADIUS), 0);
            if (thread.isEmpty()) {
                continue;
            }
            batch.setColor(1, 1, 1, share);
            AtlasRegion tile = thread.get((int) (sortie.tick() / THREAD_FRAME_TICKS % thread.size));
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy unit = sortie.enemy(i);
                if (unit.ringFeeder() == herald.serial() && unit.ringMax() > 0 && !unit.cloaked()) {
                    drawThread(batch, tile, hx, hy, unit.renderX(alpha), unit.renderY(alpha));
                }
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Over the Herald's body: its halo crest's bloom, off while it is stunned. */
    void drawCrests(SpriteBatch batch, Sortie sortie, float alpha) {
        if (crest.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int h = 0; h < sortie.enemyCount(); h++) {
            Enemy herald = sortie.enemy(h);
            if (!herald(herald) || herald.empStunned() || herald.cloaked()) {
                continue;
            }
            AtlasRegion frame = crest.get(crestFrame(sortie.tick(), herald.serial(), crest.size));
            drawScaled(batch, frame, herald.renderX(alpha), herald.renderY(alpha), 1, 0);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Whether the unit sings an aura (a Choir Herald of the level). */
    private boolean herald(Enemy enemy) {
        return enemy.kind() >= 0 && enemy.kind() < heralds.length && heralds[enemy.kind()];
    }

    /** The thread from (x0, y0) to (x1, y1): its tile repeated along the line, the last one cut to fit. */
    private static void drawThread(SpriteBatch batch, AtlasRegion tile, double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.hypot(dx, dy);
        if (length < 1) {
            return;
        }
        float degrees = (float) Math.toDegrees(Math.atan2(dy, dx));
        double ux = dx / length;
        double uy = dy / length;
        int tiles = (int) Math.ceil(length / THREAD_TILE);
        float height = tile.getRegionHeight();
        for (int t = 0; t < tiles; t++) {
            double from = t * THREAD_TILE;
            float width = (float) Math.min(THREAD_TILE, length - from);
            double sx = x0 + ux * from;
            double sy = y0 + uy * from;
            TextureRegion part = width < THREAD_TILE ? cut(tile, width) : tile;
            batch.draw(
                    part,
                    (float) (PixelScreen.PLAY_FIELD_X + sx),
                    (float) (sy - height / 2),
                    0,
                    height / 2,
                    width,
                    height,
                    1,
                    1,
                    degrees);
        }
    }

    /** The scratch region for the last tile of a thread (allocated once). */
    private static final TextureRegion CUT = new TextureRegion();

    /** The first {@code width} px of {@code tile} (the thread's last, shorter tile). */
    private static TextureRegion cut(AtlasRegion tile, float width) {
        CUT.setRegion(tile, 0, 0, Math.max(1, Math.round(width)), tile.getRegionHeight());
        return CUT;
    }

    /** {@code region} centred at (x, y) in the play field, scaled about its centre and turned by {@code degrees}. */
    private static void drawScaled(
            SpriteBatch batch, TextureRegion region, double x, double y, float scale, float degrees) {
        float width = region.getRegionWidth();
        float height = region.getRegionHeight();
        batch.draw(
                region,
                (float) Math.round(PixelScreen.PLAY_FIELD_X + x - width / 2),
                (float) Math.round(y - height / 2),
                width / 2,
                height / 2,
                width,
                height,
                scale,
                scale,
                degrees);
    }
}
