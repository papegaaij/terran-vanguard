package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part B, step B4: Level 36's units' looks beyond {@link EnemyLooks}' heading set (tools/art/jovian_units.py,
 * whose docstring gives the sprites; design/enemies/air/chimera, design/enemies/air/harvester and fleet-stormhawk), a
 * look only:
 *
 * <ul>
 *   <li>the Chimera: its tissue seams ({@code chimera-seams}, additive over the body per heading) at {@link
 *       RegenerationLooks#seamGlow}'s strength (faint at full health, dark just hit, brighter the fuller it heals), the
 *       healing pulse running out from the body twice a second while it heals ({@code chimera-heal}, heading × 6 +
 *       phase, at 12 fps), and at its death the spray of teal flecks and gold sparks over the small burst ({@code
 *       chimera-flecks}, 3 steps a frame); its muzzle glint is the Talon's ({@link CeresLooks}, on {@code
 *       pivots/chimera.json});
 *   <li>the helium-3 harvester: its hull by its drift's side ({@code harvester} flying right, {@code harvester-left}
 *       its own render flying left, never mirrored), its pale gas plume trailing from the scoop under the hull ({@code
 *       -plume}, 4 frames at 6 fps) and at its death the break in two sinking into the band ({@code -break}, 4 steps a
 *       frame) with the plume's flare and the fire at the cut ({@code -flare}, additive);
 *   <li>the fleet's drop craft (CDF Stormhawks diving up the screen): the engines' exhaust streak under the hull
 *       ({@code fleet-stormhawk-trail}, additive, its top centre on the tail).
 * </ul>
 *
 * The pathfinder pod is a debris chunk: its tumble and its break are the belt rocks' ({@link PropPasses}, {@link
 * CeresLooks}). Nothing is allocated per frame.
 */
final class CloudDiverLooks {
    static final String CHIMERA = "chimera";
    static final String HARVESTER = "harvester";
    static final String FLEET = "fleet-stormhawk";

    /** The art's headings: heading k turns k x 22.5 degrees clockwise from straight down. */
    static final int HEADINGS = 16;
    /** The healing pulse's phases per heading, at {@value #HEAL_FPS} fps (two pulses a second). */
    static final int HEAL_PHASES = 6;

    static final double HEAL_FPS = 12;
    /** The flecks: 3 steps a frame. */
    static final int FLECK_FRAME_TICKS = 3;
    /** The harvester's plume flows at 6 fps. */
    static final double PLUME_FPS = 6;
    /** The break and its flare: 4 steps a frame (0.8 s). */
    static final int BREAK_FRAME_TICKS = 4;
    /** The drop craft's trail: its top centre this far behind the hull's centre, px; its frames flicker at 12 fps. */
    static final double TRAIL_BEHIND = 12;

    static final double TRAIL_FPS = 12;
    /** The trail shows only while the craft flies this close to straight up, radians. */
    static final double TRAIL_HEADING_TOLERANCE = Math.PI / 6;

    private static final int NONE = 0;
    private static final int ROLE_CHIMERA = 1;
    private static final int ROLE_HARVESTER = 2;
    private static final int ROLE_FLEET = 3;

    private static final int SLOTS = 16;
    private static final int LEFT = 1;
    private static final int RIGHT = 0;

    private final int[] role;
    private final Array<AtlasRegion> seams;
    private final Array<AtlasRegion> heal;
    private final Array<AtlasRegion> flecks;
    private final Array<AtlasRegion> trail;
    /** Per side (0 right, 1 left) the hull, the plume, the break and its flare. */
    private final AtlasRegion[] hull = new AtlasRegion[2];

    private final List<Array<AtlasRegion>> plume;
    private final List<Array<AtlasRegion>> breakFrames;
    private final List<Array<AtlasRegion>> flare;

    /** The harvesters of the last step: serial (-1 free), place and side. */
    private final int[] harvesterSerial = new int[SLOTS];

    private final double[] harvesterX = new double[SLOTS];
    private final double[] harvesterY = new double[SLOTS];
    private final int[] harvesterSide = new int[SLOTS];
    /** The deaths' pieces: the flecks (start step, place) and the breaks (start step, place, side). */
    private final long[] fleckTick = new long[SLOTS];

    private final double[] fleckX = new double[SLOTS];
    private final double[] fleckY = new double[SLOTS];
    private int nextFleck;
    private final long[] breakTick = new long[SLOTS];
    private final double[] breakX = new double[SLOTS];
    private final double[] breakY = new double[SLOTS];
    private final int[] breakSide = new int[SLOTS];
    private int nextBreak;

    CloudDiverLooks(Sprites sprites, List<EnemySpec> kinds) {
        role = new int[kinds.size()];
        for (int k = 0; k < role.length; k++) {
            String slug = kinds.get(k).slug();
            if (!sprites.has(slug)) {
                continue;
            }
            role[k] = switch (slug) {
                case CHIMERA -> ROLE_CHIMERA;
                case HARVESTER -> ROLE_HARVESTER;
                case FLEET -> ROLE_FLEET;
                default -> NONE;
            };
        }
        Array<AtlasRegion> none = new Array<>();
        seams = optional(sprites, CHIMERA + "-seams", none);
        heal = optional(sprites, CHIMERA + "-heal", none);
        flecks = optional(sprites, CHIMERA + "-flecks", none);
        trail = optional(sprites, FLEET + "-trail", none);
        hull[RIGHT] = sprites.has(HARVESTER) ? sprites.region(HARVESTER) : null;
        hull[LEFT] = sprites.has(HARVESTER + "-left") ? sprites.region(HARVESTER + "-left") : null;
        plume = List.of(
                optional(sprites, HARVESTER + "-plume", none), optional(sprites, HARVESTER + "-left-plume", none));
        breakFrames = List.of(
                optional(sprites, HARVESTER + "-break", none), optional(sprites, HARVESTER + "-left-break", none));
        flare = List.of(
                optional(sprites, HARVESTER + "-flare", none), optional(sprites, HARVESTER + "-left-flare", none));
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name, Array<AtlasRegion> none) {
        return sprites.has(name) ? sprites.frames(name) : none;
    }

    /** Whether any of the level's kinds has these looks. */
    boolean any() {
        for (int r : role) {
            if (r != NONE) {
                return true;
            }
        }
        return false;
    }

    private int roleOf(int kind) {
        return kind >= 0 && kind < role.length ? role[kind] : NONE;
    }

    /** The level restarts: no harvesters tracked, no pieces. */
    void restart() {
        Arrays.fill(harvesterSerial, -1);
        Arrays.fill(fleckTick, Long.MIN_VALUE / 2);
        Arrays.fill(breakTick, Long.MIN_VALUE / 2);
        nextFleck = 0;
        nextBreak = 0;
    }

    /** After each simulation step: the deaths' pieces (from the last step's harvesters), then this step's harvesters. */
    void stepped(Sortie sortie) {
        if (!any()) {
            return;
        }
        long tick = sortie.tick();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) != SimEvents.Type.ENEMY_DESTROYED) {
                continue;
            }
            int r = roleOf(events.value(i));
            double x = events.x(i);
            double y = events.y(i);
            if (r == ROLE_CHIMERA && flecks.size > 0) {
                fleckTick[nextFleck] = tick;
                fleckX[nextFleck] = x;
                fleckY[nextFleck] = y;
                nextFleck = (nextFleck + 1) % SLOTS;
            } else if (r == ROLE_HARVESTER) {
                breakTick[nextBreak] = tick;
                breakX[nextBreak] = x;
                breakY[nextBreak] = y;
                breakSide[nextBreak] = sideNear(x, y);
                nextBreak = (nextBreak + 1) % SLOTS;
            }
        }
        int[] serials = harvesterSerial;
        // Keep the sides of the harvesters still flying (by serial), note the new ones.
        boolean[] seen = seenScratch;
        Arrays.fill(seen, false);
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (roleOf(enemy.kind()) != ROLE_HARVESTER) {
                continue;
            }
            int slot = slotOf(enemy.serial());
            double x = enemy.renderX(1);
            if (slot < 0) {
                slot = freeSlot();
                if (slot < 0) {
                    continue;
                }
                serials[slot] = enemy.serial();
                harvesterSide[slot] = x < PlayField.WIDTH / 2.0 ? LEFT : RIGHT;
            }
            seen[slot] = true;
            double dx = x - enemy.renderX(0);
            if (Math.abs(dx) > 1e-6) {
                harvesterSide[slot] = dx < 0 ? LEFT : RIGHT;
            }
            harvesterX[slot] = x;
            harvesterY[slot] = enemy.renderY(1);
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serials[s] = -1;
            }
        }
    }

    private final boolean[] seenScratch = new boolean[SLOTS];

    private int slotOf(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (harvesterSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    private int freeSlot() {
        for (int s = 0; s < SLOTS; s++) {
            if (harvesterSerial[s] < 0) {
                return s;
            }
        }
        return -1;
    }

    /** The side of the last step's harvester nearest (x, y); by the field's half without one. */
    private int sideNear(double x, double y) {
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int s = 0; s < SLOTS; s++) {
            if (harvesterSerial[s] < 0) {
                continue;
            }
            double d = Math.hypot(harvesterX[s] - x, harvesterY[s] - y);
            if (d < best) {
                best = d;
                nearest = s;
            }
        }
        return nearest >= 0 ? harvesterSide[nearest] : x < PlayField.WIDTH / 2.0 ? LEFT : RIGHT;
    }

    /** A harvester's side as last seen (its drift's sign), by the field's half before it was. */
    int side(Enemy enemy) {
        int slot = slotOf(enemy.serial());
        return slot >= 0 ? harvesterSide[slot] : enemy.renderX(1) < PlayField.WIDTH / 2.0 ? LEFT : RIGHT;
    }

    /** The unit's frame, or null when {@link EnemyLooks}' set draws it: a harvester's hull by its drift's side. */
    AtlasRegion frame(Enemy enemy) {
        if (roleOf(enemy.kind()) != ROLE_HARVESTER) {
            return null;
        }
        return hull[side(enemy)];
    }

    /** Under the low-air hulls: the harvesters' plumes trailing from their scoops (normal blend). */
    void drawPlumes(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (roleOf(enemy.kind()) != ROLE_HARVESTER) {
                continue;
            }
            Array<AtlasRegion> frames = plume.get(side(enemy));
            if (frames.size == 0) {
                continue;
            }
            int frame =
                    (int) Math.floorMod((long) Math.floor(seconds * PLUME_FPS) + enemy.serial(), (long) frames.size);
            drawCentred(batch, frames.get(frame), enemy.renderX(alpha), enemy.renderY(alpha));
        }
    }

    /** Under the air hulls: the drop craft's exhaust streaks, additive, while they dive up the screen. */
    void drawTrails(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        if (trail.size == 0) {
            return;
        }
        boolean additive = false;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (roleOf(enemy.kind()) != ROLE_FLEET
                    || Math.abs(Math.PI
                                    - Math.floorMod(Math.round(enemy.facing() * 1e6), Math.round(2 * Math.PI * 1e6))
                                            / 1e6)
                            > TRAIL_HEADING_TOLERANCE) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            AtlasRegion frame = trail.get(
                    (int) Math.floorMod((long) Math.floor(seconds * TRAIL_FPS) + enemy.serial(), (long) trail.size));
            double x = enemy.renderX(alpha);
            double top = enemy.renderY(alpha) - TRAIL_BEHIND;
            drawCentred(batch, frame, x, top - frame.getRegionHeight() / 2.0);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /**
     * The Chimeras' seams and healing pulse over their bodies, additive (the caller's blend); each at {@code banks}'
     * cover where it is when given (drawn again over the banks, so the seams read at full under them), else at full.
     */
    void drawSeams(SpriteBatch batch, Sortie sortie, float alpha, ForegroundBanks banks) {
        if (seams.size != HEADINGS) {
            return;
        }
        double ticks = sortie.tick() + alpha;
        double seconds = ticks * SimStep.SECONDS;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (roleOf(enemy.kind()) != ROLE_CHIMERA || enemy.cloaked()) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            float cover = banks == null ? 1 : banks.cover(x, y);
            if (cover <= 0) {
                continue;
            }
            int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
            float glow = (float) RegenerationLooks.seamGlow(enemy, ticks);
            batch.setColor(1, 1, 1, Math.clamp(glow * cover, 0, 1));
            drawCentred(batch, seams.get(heading), x, y);
            if (enemy.regeneration().healing() && heal.size == HEADINGS * HEAL_PHASES) {
                double share = enemy.hp() / enemy.spec().hp();
                float level = (float) (RegenerationLooks.HEAL_LOW + (1 - RegenerationLooks.HEAL_LOW) * share);
                int phase = (int) Math.floorMod((long) Math.floor(seconds * HEAL_FPS), (long) HEAL_PHASES);
                batch.setColor(1, 1, 1, Math.clamp(level * cover, 0, 1));
                drawCentred(batch, heal.get(heading * HEAL_PHASES + phase), x, y);
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    /** Whether a kind's regeneration is drawn by its own seams here (not {@link RegenerationLooks}' stand-in light). */
    boolean ownSeams(int kind) {
        return roleOf(kind) == ROLE_CHIMERA && seams.size == HEADINGS;
    }

    /**
     * Over the low-air units: the harvesters breaking in two (normal), then their plumes' flare and the fire at the cut
     * (additive).
     */
    void drawBreaks(SpriteBatch batch, Sortie sortie) {
        long tick = sortie.tick();
        for (int b = 0; b < SLOTS; b++) {
            Array<AtlasRegion> frames = breakFrames.get(breakSide[b]);
            int frame = frame(tick - breakTick[b], frames.size, BREAK_FRAME_TICKS);
            if (frame >= 0) {
                drawCentred(batch, frames.get(frame), breakX[b], breakY[b]);
            }
        }
        boolean additive = false;
        for (int b = 0; b < SLOTS; b++) {
            Array<AtlasRegion> frames = flare.get(breakSide[b]);
            int frame = frame(tick - breakTick[b], frames.size, BREAK_FRAME_TICKS);
            if (frame < 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            drawCentred(batch, frames.get(frame), breakX[b], breakY[b]);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** Over the bursts: the Chimeras' flecks and sparks, additive. */
    void drawFlecks(SpriteBatch batch, Sortie sortie) {
        long tick = sortie.tick();
        boolean additive = false;
        for (int f = 0; f < SLOTS; f++) {
            int frame = frame(tick - fleckTick[f], flecks.size, FLECK_FRAME_TICKS);
            if (frame < 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            drawCentred(batch, flecks.get(frame), fleckX[f], fleckY[f]);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** The frame of a piece {@code age} steps old, {@code ticks} steps a frame; -1 before it or after its last. */
    static int frame(long age, int frames, int ticks) {
        if (age < 0 || frames == 0) {
            return -1;
        }
        long frame = age / ticks;
        return frame < frames ? (int) frame : -1;
    }
}
