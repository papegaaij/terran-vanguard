package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part D, step D4: Level 38's units' and objects' looks beyond {@link EnemyLooks}' heading set and {@link
 * PropPasses}' debris (tools/art/storm_layer_units.py, whose docstring gives the sprites; design/enemies/air/harrow,
 * design/campaign Level 38, Secrets and pickups), a look only:
 *
 * <ul>
 *   <li>the Harrow: its sac's vein glow ({@code harrow-sac}, additive over the body per heading) at the state's strength
 *       ({@link #sacGlow}: dim 0.2 stunned, hunting 0.6 ± 0.15 in a slow pulse once a second, the lock tell 0.75 to 1.0
 *       over the tell, ramming 0.9), the lock tell's halo over the sac ({@code harrow-tell}, the tell's progress × 12,
 *       on {@code pivots/harrow.json}'s sac), at its death the teal flash and tissue flecks over the small burst ({@code
 *       harrow-burst}, 3 steps a frame, at the sac) and its shell tumbling on along its last movement, fading over
 *       {@value #SHELL_SECONDS} s ({@code harrow-shell}, 12 fps); the ring of 8 is the game's bullets;
 *   <li>Aurelia's cargo pod: its blue-white outline flashing over its tumble for {@value #OUTLINE_SECONDS} s on every
 *       lightning flash or told strike ({@code debris-cargo-pod-outline}, additive; any debris chunk with an {@code
 *       -outline} set of its tumble's frames); its tumble and its break are the belt rocks' ({@link PropPasses}, {@link
 *       CeresLooks});
 *   <li>the CDF supply canister: its parachute's sway is {@link PropPasses}' (round 76 L.1 b: it opens by itself at its
 *       height, so no latch is drawn on it); its opening ({@code DEBRIS_RELEASED}) plays its release ({@code
 *       debris-canister-release}, 10 fps) where it was, drifting on with it (the armour patch is the game's pickup); its
 *       friendly brackets are {@link FriendlyMarks}'.
 * </ul>
 *
 * Nothing is allocated per frame.
 */
final class StormLayerLooks {
    static final String HARROW = "harrow";

    /** The Harrow's headings: heading k turns k x 22.5 degrees clockwise from straight down. */
    static final int HEADINGS = 16;
    /** The sac's glow by state (tools/art/storm_layer_units.py SAC_STATES). */
    static final double SAC_DIM = 0.2;

    static final double SAC_HUNT = 0.6;
    static final double SAC_HUNT_SWING = 0.15;
    static final double SAC_TELL_FROM = 0.75;
    static final double SAC_TELL_TO = 1.0;
    static final double SAC_RAM = 0.9;
    /** The hunting sac pulses once a second. */
    static final double SAC_PULSE_HZ = 1;
    /** The burst: 3 steps a frame. */
    static final int BURST_FRAME_TICKS = 3;
    /** The shell tumbles at 12 fps and fades over this long, s. */
    static final double SHELL_FPS = 12;

    static final double SHELL_SECONDS = 1.0;
    /** The pod's flash outline: this long after a flash, fading, s (design/campaign Level 38: "a flash outlines it"). */
    static final double OUTLINE_SECONDS = 0.5;
    /** The canister's release: 10 fps. */
    static final double RELEASE_FPS = 10;

    static final String OUTLINE = "-outline";
    static final String RELEASE = "-release";

    private static final int SLOTS = 32;
    private static final int PIECES = 16;
    private static final int CHUNKS = 8;

    private final boolean[] harrow;
    private final Array<AtlasRegion> sac;
    private final Array<AtlasRegion> tell;
    private final Array<AtlasRegion> burst;
    private final Array<AtlasRegion> shell;
    /** Per heading the sac's place from the unit's centre, px, y up. */
    private final double[][] sacAt;

    private final Sprites sprites;
    /** The Harrows of the last step: serial (-1 free), place, step's movement and heading. */
    private final int[] serial = new int[SLOTS];

    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];
    private final double[] lastVx = new double[SLOTS];
    private final double[] lastVy = new double[SLOTS];
    private final int[] lastHeading = new int[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];
    /** The deaths' pieces: start step, place (the sac's for the burst), drift per step (the shell's). */
    private final long[] pieceTick = new long[PIECES];

    private final double[] burstX = new double[PIECES];
    private final double[] burstY = new double[PIECES];
    private final double[] shellX = new double[PIECES];
    private final double[] shellY = new double[PIECES];
    private final double[] shellVx = new double[PIECES];
    private final double[] shellVy = new double[PIECES];
    private int nextPiece;
    /** The last lightning flash's or told strike's step (the pod's outline). */
    private long flashTick;
    /** The chunks with a release of the last step: serial (-1 none), place, step's movement, sprite. */
    private final int[] chunkSerial = new int[CHUNKS];

    private final double[] chunkX = new double[CHUNKS];
    private final double[] chunkY = new double[CHUNKS];
    private final double[] chunkVx = new double[CHUNKS];
    private final double[] chunkVy = new double[CHUNKS];
    private final String[] chunkSprite = new String[CHUNKS];
    /** The releases playing: start step, place, drift per step, frames. */
    private final long[] releaseTick = new long[CHUNKS];

    private final double[] releaseX = new double[CHUNKS];
    private final double[] releaseY = new double[CHUNKS];
    private final double[] releaseVx = new double[CHUNKS];
    private final double[] releaseVy = new double[CHUNKS];
    private final List<Array<AtlasRegion>> releaseFrames = new java.util.ArrayList<>(CHUNKS);
    private int nextRelease;
    /** Per debris sprite its flash outline's and its release's frames (empty for none), looked up once. */
    private final java.util.Map<String, Array<AtlasRegion>> outlines = new java.util.HashMap<>();

    private final java.util.Map<String, Array<AtlasRegion>> releases = new java.util.HashMap<>();
    private final Array<AtlasRegion> none = new Array<>();

    StormLayerLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        this.sprites = sprites;
        harrow = new boolean[kinds.size()];
        boolean any = false;
        for (int k = 0; k < harrow.length; k++) {
            harrow[k] = kinds.get(k).slug().equals(HARROW) && sprites.has(HARROW + "-sac");
            any |= harrow[k];
        }
        for (int r = 0; r < CHUNKS; r++) {
            releaseFrames.add(null);
        }
        sac = any ? sprites.frames(HARROW + "-sac") : none;
        tell = any && sprites.has(HARROW + "-tell") ? sprites.frames(HARROW + "-tell") : none;
        burst = any && sprites.has(HARROW + "-burst") ? sprites.frames(HARROW + "-burst") : none;
        shell = any && sprites.has(HARROW + "-shell") ? sprites.frames(HARROW + "-shell") : none;
        sacAt = any ? sacs(pivots.apply(HARROW)) : new double[0][];
        restart();
    }

    /** The sac's place per heading from {@code pivots}' {@code sac} ([x, y] px from the top left), from the centre, y up. */
    static double[][] sacs(JsonValue pivots) {
        JsonValue sacs = pivots == null ? null : pivots.get("sac");
        if (sacs == null) {
            return new double[0][];
        }
        double halfX = pivots.get("size").getDouble(0) / 2;
        double halfY = pivots.get("size").getDouble(1) / 2;
        double[][] at = new double[sacs.size][2];
        for (int k = 0; k < sacs.size; k++) {
            at[k][0] = sacs.get(k).getDouble(0) - halfX;
            at[k][1] = halfY - sacs.get(k).getDouble(1);
        }
        return at;
    }

    /** The level restarts: nothing tracked, no pieces, no flash, no release. */
    void restart() {
        Arrays.fill(serial, -1);
        Arrays.fill(pieceTick, Long.MIN_VALUE / 2);
        Arrays.fill(chunkSerial, -1);
        Arrays.fill(releaseTick, Long.MIN_VALUE / 2);
        java.util.Collections.fill(releaseFrames, null);
        Arrays.fill(chunkSprite, null);
        flashTick = Long.MIN_VALUE / 2;
        nextPiece = 0;
        nextRelease = 0;
    }

    private boolean isHarrow(int kind) {
        return kind >= 0 && kind < harrow.length && harrow[kind];
    }

    /** After each simulation step: the deaths (from the last step's Harrows), the flashes, the releases, then this step. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case ENEMY_DESTROYED -> {
                    if (isHarrow(events.value(i))) {
                        died(events.x(i), events.y(i), tick);
                    }
                }
                case LIGHTNING, STRIKE_TOLD -> flashTick = tick;
                case DEBRIS_RELEASED -> released(events.x(i), events.y(i), tick);
                default -> {}
            }
        }
        trackHarrows(sortie);
        trackChunks(sortie);
    }

    private void trackHarrows(Sortie sortie) {
        if (sac.size == 0) {
            return;
        }
        Arrays.fill(seen, false);
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!isHarrow(enemy.kind())) {
                continue;
            }
            int slot = slotOf(enemy.serial());
            if (slot < 0) {
                slot = freeSlot();
                if (slot < 0) {
                    continue;
                }
                serial[slot] = enemy.serial();
            }
            seen[slot] = true;
            lastX[slot] = enemy.renderX(1);
            lastY[slot] = enemy.renderY(1);
            lastVx[slot] = enemy.renderX(1) - enemy.renderX(0);
            lastVy[slot] = enemy.renderY(1) - enemy.renderY(0);
            lastHeading[slot] = EnemyLooks.heading(enemy.facing(), HEADINGS);
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serial[s] = -1;
            }
        }
    }

    private void trackChunks(Sortie sortie) {
        Arrays.fill(chunkSerial, -1);
        int n = 0;
        for (int d = 0; d < sortie.debrisCount() && n < CHUNKS; d++) {
            Debris chunk = sortie.debris(d);
            if (chunk.thrown() || chunk.pulled() || chunk.large() || releaseOf(chunk.sprite()).size == 0) {
                continue;
            }
            chunkSerial[n] = chunk.serial();
            chunkX[n] = chunk.renderX(1);
            chunkY[n] = chunk.renderY(1);
            chunkVx[n] = chunk.renderX(1) - chunk.renderX(0);
            chunkVy[n] = chunk.renderY(1) - chunk.renderY(0);
            chunkSprite[n] = chunk.sprite();
            n++;
        }
    }

    private int slotOf(int unit) {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] == unit) {
                return s;
            }
        }
        return -1;
    }

    private int freeSlot() {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] < 0) {
                return s;
            }
        }
        return -1;
    }

    /** A Harrow died at (x, y): the last step's one nearest it bursts at its sac, its shell drifting on. */
    private void died(double x, double y, long tick) {
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] < 0) {
                continue;
            }
            double d = Math.hypot(lastX[s] - x, lastY[s] - y);
            if (d < best) {
                best = d;
                nearest = s;
            }
        }
        int p = nextPiece;
        nextPiece = (nextPiece + 1) % PIECES;
        pieceTick[p] = tick;
        shellX[p] = x;
        shellY[p] = y;
        shellVx[p] = nearest >= 0 ? lastVx[nearest] : 0;
        shellVy[p] = nearest >= 0 ? lastVy[nearest] : 0;
        int heading = nearest >= 0 ? lastHeading[nearest] : 0;
        double[] at = heading < sacAt.length ? sacAt[heading] : null;
        burstX[p] = x + (at == null ? 0 : at[0]);
        burstY[p] = y + (at == null ? 0 : at[1]);
        if (nearest >= 0) {
            serial[nearest] = -1;
        }
    }

    /** A chunk let its pickup fall at (x, y): the last step's chunk with a release nearest it plays it. */
    private void released(double x, double y, long tick) {
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int c = 0; c < CHUNKS; c++) {
            if (chunkSerial[c] < 0) {
                continue;
            }
            double d = Math.hypot(chunkX[c] - x, chunkY[c] - y);
            if (d < best) {
                best = d;
                nearest = c;
            }
        }
        if (nearest < 0) {
            return;
        }
        int r = nextRelease;
        nextRelease = (nextRelease + 1) % CHUNKS;
        releaseTick[r] = tick;
        releaseX[r] = chunkX[nearest];
        releaseY[r] = chunkY[nearest];
        releaseVx[r] = chunkVx[nearest];
        releaseVy[r] = chunkVy[nearest];
        releaseFrames.set(r, releaseOf(chunkSprite[nearest]));
    }

    /** A debris sprite's flash outline frames, empty without them (looked up once). */
    private Array<AtlasRegion> outlineOf(String sprite) {
        Array<AtlasRegion> frames = outlines.get(sprite);
        if (frames == null) {
            frames = sprites.has(sprite + OUTLINE) ? sprites.frames(sprite + OUTLINE) : none;
            outlines.put(sprite, frames);
        }
        return frames;
    }

    /** A debris sprite's release frames, empty without them (looked up once). */
    private Array<AtlasRegion> releaseOf(String sprite) {
        Array<AtlasRegion> frames = releases.get(sprite);
        if (frames == null) {
            frames = sprites.has(sprite + RELEASE) ? sprites.frames(sprite + RELEASE) : none;
            releases.put(sprite, frames);
        }
        return frames;
    }

    /**
     * The sac's glow for a Harrow: dim while stunned, from {@value #SAC_TELL_FROM} to {@value #SAC_TELL_TO} over its lock
     * tell ({@code tellShare}), {@value #SAC_RAM} ramming, else hunting: {@value #SAC_HUNT} ± {@value #SAC_HUNT_SWING}
     * once a second ({@code seconds} on its own phase).
     */
    static double sacGlow(boolean stunned, boolean telling, double tellShare, boolean ramming, double seconds) {
        if (stunned) {
            return SAC_DIM;
        }
        if (telling) {
            return SAC_TELL_FROM + (SAC_TELL_TO - SAC_TELL_FROM) * Math.clamp(tellShare, 0, 1);
        }
        if (ramming) {
            return SAC_RAM;
        }
        return SAC_HUNT + SAC_HUNT_SWING * Math.sin(2 * Math.PI * SAC_PULSE_HZ * seconds);
    }

    /** The tell's halo frame at its progress {@code share}: share x frames, the last held. */
    static int tellFrame(double share, int frames) {
        return Math.clamp((int) Math.floor(Math.clamp(share, 0, 1) * frames), 0, frames - 1);
    }

    /** Over the air hulls, additive (the caller's blend): the Harrows' sacs and their lock tells' halos. */
    void drawSacs(SpriteBatch batch, Sortie sortie, float alpha) {
        if (sac.size != HEADINGS) {
            return;
        }
        double seconds = (sortie.tick() + alpha) * SimStep.SECONDS;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!isHarrow(enemy.kind()) || enemy.cloaked()) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
            boolean telling = enemy.dashTelling() && !enemy.empStunned();
            float glow = (float) sacGlow(
                    enemy.empStunned(),
                    telling,
                    enemy.dashTellShare(),
                    enemy.dashing(),
                    seconds + enemy.serial() * 0.37);
            batch.setColor(1, 1, 1, Math.clamp(glow, 0, 1));
            drawCentred(batch, sac.get(heading), x, y);
            if (telling && tell.size > 0) {
                double[] at = heading < sacAt.length ? sacAt[heading] : null;
                batch.setColor(1, 1, 1, 1);
                drawCentred(
                        batch,
                        tell.get(tellFrame(enemy.dashTellShare(), tell.size)),
                        x + (at == null ? 0 : at[0]),
                        y + (at == null ? 0 : at[1]));
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    /** The shell's alpha {@code age} steps after the death: 1 fading to 0 over {@value #SHELL_SECONDS} s. */
    static float shellAlpha(long age) {
        double seconds = age * SimStep.SECONDS;
        return seconds < 0 || seconds >= SHELL_SECONDS ? 0 : (float) (1 - seconds / SHELL_SECONDS);
    }

    /** Over the death pieces: the Harrows' shells tumbling on (normal), then their bursts (additive). */
    void drawPieces(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        if (shell.size > 0) {
            for (int p = 0; p < PIECES; p++) {
                long age = tick - pieceTick[p];
                float fade = shellAlpha(age);
                if (fade <= 0) {
                    continue;
                }
                double steps = age + alpha;
                int frame =
                        (int) Math.floorMod((long) Math.floor(steps * SimStep.SECONDS * SHELL_FPS), (long) shell.size);
                batch.setColor(1, 1, 1, fade);
                drawCentred(batch, shell.get(frame), shellX[p] + shellVx[p] * steps, shellY[p] + shellVy[p] * steps);
            }
            batch.setColor(1, 1, 1, 1);
        }
        boolean additive = false;
        for (int p = 0; p < PIECES; p++) {
            int frame = CloudDiverLooks.frame(tick - pieceTick[p], burst.size, BURST_FRAME_TICKS);
            if (frame < 0) {
                continue;
            }
            if (!additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                additive = true;
            }
            drawCentred(batch, burst.get(frame), burstX[p], burstY[p]);
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** The flash outline's strength {@code age} steps after a flash: 1 fading to 0 over {@value #OUTLINE_SECONDS} s. */
    static float outlineAlpha(long age) {
        double seconds = age * SimStep.SECONDS;
        return seconds < 0 || seconds >= OUTLINE_SECONDS ? 0 : (float) (1 - seconds / OUTLINE_SECONDS);
    }

    /**
     * Over the debris: a chunk's flash outline over its tumble frame (additive), after a lightning flash or a told
     * strike; then the releases (normal), drifting on with their chunks.
     */
    void drawDebris(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        float outline = outlineAlpha(tick - flashTick);
        if (outline > 0) {
            boolean additive = false;
            for (int d = 0; d < sortie.debrisCount(); d++) {
                Debris chunk = sortie.debris(d);
                if (chunk.thrown() || chunk.pulled()) {
                    continue;
                }
                Array<AtlasRegion> frames = outlineOf(chunk.sprite());
                if (frames.size != PropPasses.SPIN_FRAMES) {
                    continue;
                }
                if (!additive) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    additive = true;
                }
                batch.setColor(1, 1, 1, outline);
                drawCentred(
                        batch,
                        frames.get(PropPasses.spinFrame(tick, chunk.serial())),
                        chunk.renderX(alpha),
                        chunk.renderY(alpha));
            }
            batch.setColor(1, 1, 1, 1);
            if (additive) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
        for (int r = 0; r < CHUNKS; r++) {
            Array<AtlasRegion> frames = releaseFrames.get(r);
            if (frames == null) {
                continue;
            }
            long age = tick - releaseTick[r];
            int ticksPerFrame = Math.max(1, (int) Math.round(1 / (SimStep.SECONDS * RELEASE_FPS)));
            int frame = CloudDiverLooks.frame(age, frames.size, ticksPerFrame);
            if (frame < 0) {
                continue;
            }
            double steps = age + alpha;
            drawCentred(
                    batch, frames.get(frame), releaseX[r] + releaseVx[r] * steps, releaseY[r] + releaseVy[r] * steps);
        }
    }
}
