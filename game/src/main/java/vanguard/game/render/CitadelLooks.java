package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.EnemySpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F4 (design/campaign Level 48, the backdrop; tools/art/act7_citadel.py's citadel, its proposal's F4
 * hooks): Level 48's citadel following the fight and the secondary.
 *
 * <ul>
 *   <li><b>The spire well</b> ({@value #WELL} and its {@code -glow}, 12 frames): shut (frame 0) until the boss arrives
 *       (the sting); its lid splitting over frames 1–8 at {@value #WELL_FPS} fps; then open on its gold-lit throat, frames
 *       9–11 looping at the same rate. A retry from the boss checkpoint shuts it again.
 *   <li><b>The core-linked veins</b> (each {@value #VEIN_L} / {@value #VEIN_R} piece belongs to the reactor core at its
 *       bottom centre): its {@code -glow} (18 frames, additive at the piece) loops 0–11 at {@value #VEIN_FPS} fps (one
 *       pulse up the vein in 1.5 s) while its core lives, plays 12–17 once at {@value #DARK_FPS} fps when the core dies
 *       (dark from the core outward over 1 s), then is not drawn; the vein piece itself stays (the dark channel). A
 *       core's death is matched to the vein whose bottom centre was last drawn nearest it.
 * </ul>
 *
 * Fixed arrays: nothing is allocated per frame.
 */
final class CitadelLooks {
    static final String WELL = "spire-well";
    static final String VEIN_L = "core-vein-l";
    static final String VEIN_R = "core-vein-r";
    static final String GLOW = "-glow";
    /** The reactor core's unit (design/enemies/ground/reactor-core). */
    static final String CORE = "reactor-core";
    /** The well's frames: shut, the lid splitting, the open throat's loop. */
    static final int WELL_FRAMES = 12;

    static final int SPLIT_FIRST = 1;
    static final int SPLIT_FRAMES = 8;
    static final int OPEN_FIRST = 9;
    static final int OPEN_FRAMES = 3;
    static final double WELL_FPS = 4;
    /** The veins' glow: the live loop's frames and rate, the dark run's first frame, its frames and rate. */
    static final int VEIN_FRAMES = 18;

    static final int LIVE_FRAMES = 12;
    static final double VEIN_FPS = 8;
    static final int DARK_FIRST = 12;
    static final int DARK_FRAMES = 6;
    static final double DARK_FPS = 6;
    /** A core's death is its vein's within this far of the vein's bottom centre, px (across, along). */
    static final double MATCH_X = 16;

    static final double MATCH_Y = 120;

    private static final long NEVER = Long.MIN_VALUE / 2;

    /** Per ground piece its vein's index, -1 for another piece; whether it is the well. */
    private final int[] veinOf;

    private final boolean[] well;
    /** Per vein its glow frames (18), the step its core died (NEVER while it lives), its bottom centre last drawn. */
    private final List<Array<AtlasRegion>> glow;

    private final long[] darkTick;
    private final double[] drawnX;
    private final double[] drawnY;
    /** The step the boss arrived (the well splits), NEVER before; the last step seen. */
    private long arrivedTick = NEVER;

    private long tick;
    /** Per enemy kind whether it is a reactor core; null until the first sortie is seen. */
    private boolean[] cores;

    private CitadelLooks(int[] veinOf, boolean[] well, List<Array<AtlasRegion>> glows) {
        this.veinOf = veinOf;
        this.well = well;
        glow = glows;
        darkTick = new long[glow.size()];
        drawnX = new double[glow.size()];
        drawnY = new double[glow.size()];
        restart();
    }

    /**
     * The citadel's hooks for a level's ground pieces (named {@code names}, in order): null without a spire well or a
     * core vein. {@code glows} gives a vein's {@code -glow} frames (null when the atlas lacks them).
     */
    static CitadelLooks of(String[] names, java.util.function.Function<String, Array<AtlasRegion>> glows) {
        int[] veinOf = new int[names.length];
        boolean[] well = new boolean[names.length];
        List<Array<AtlasRegion>> veins = new java.util.ArrayList<>();
        boolean any = false;
        for (int p = 0; p < names.length; p++) {
            veinOf[p] = -1;
            well[p] = names[p].equals(WELL);
            any |= well[p];
            if (names[p].equals(VEIN_L) || names[p].equals(VEIN_R)) {
                Array<AtlasRegion> frames = glows.apply(names[p] + GLOW);
                veinOf[p] = veins.size();
                veins.add(frames != null && frames.size >= VEIN_FRAMES ? frames : null);
                any = true;
            }
        }
        return any ? new CitadelLooks(veinOf, well, veins) : null;
    }

    /** A new attempt: the well shut, every vein lit. */
    void restart() {
        arrivedTick = NEVER;
        tick = 0;
        Arrays.fill(darkTick, NEVER);
        Arrays.fill(drawnX, Double.NaN);
        Arrays.fill(drawnY, Double.NaN);
    }

    // --- the rules ---------------------------------------------------------------------------------

    /** The well's frame {@code seconds} after the boss arrived (negative or NaN: before it): shut, splitting, open. */
    static int wellFrameAt(double seconds) {
        if (!(seconds >= 0)) {
            return 0;
        }
        int frame = (int) Math.floor(seconds * WELL_FPS);
        if (frame < SPLIT_FRAMES) {
            return SPLIT_FIRST + frame;
        }
        return OPEN_FIRST + (frame - SPLIT_FRAMES) % OPEN_FRAMES;
    }

    /**
     * A vein's glow frame at real time {@code real} while its core lives ({@code dark} NaN), else {@code dark} s after
     * its death: the dark run, -1 once it has run.
     */
    static int veinFrame(double real, double dark) {
        if (Double.isNaN(dark)) {
            return (int) Math.floorMod((long) Math.floor(real * VEIN_FPS), (long) LIVE_FRAMES);
        }
        int frame = (int) Math.floor(Math.max(0, dark) * DARK_FPS);
        return frame < DARK_FRAMES ? DARK_FIRST + frame : -1;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each step: the boss's arrival (the well), the reactor cores' deaths (their veins). */
    void stepped(Sortie sortie) {
        long now = sortie.tick();
        if (now < tick) {
            // A retry from the boss checkpoint: the fight starts over, the well shut again.
            arrivedTick = NEVER;
        }
        tick = now;
        if (cores == null) {
            List<EnemySpec> kinds = sortie.enemyKinds();
            cores = new boolean[kinds.size()];
            for (int k = 0; k < kinds.size(); k++) {
                cores[k] = kinds.get(k).slug().equals(CORE);
            }
        }
        if (arrivedTick == NEVER) {
            for (int k = 0; k < sortie.setPieceCount(); k++) {
                SetPiece piece = sortie.setPiece(k);
                if (piece.boss().isPresent() && piece.bossSeconds() >= 0) {
                    arrivedTick = now - Math.round(piece.bossSeconds() / SimStep.SECONDS);
                }
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED
                    && events.value(i) >= 0
                    && events.value(i) < cores.length
                    && cores[events.value(i)]) {
                coreDied(events.x(i), events.y(i), now);
            }
        }
    }

    /** A core died at (x, y): its vein (the nearest last drawn bottom centre within reach) goes dark from now. */
    void coreDied(double x, double y, long now) {
        int best = -1;
        double bestGap = Double.POSITIVE_INFINITY;
        for (int v = 0; v < glow.size(); v++) {
            if (darkTick[v] != NEVER || Double.isNaN(drawnX[v])) {
                continue;
            }
            double across = Math.abs(drawnX[v] - x);
            double along = Math.abs(drawnY[v] - y);
            if (across <= MATCH_X && along <= MATCH_Y && along + across < bestGap) {
                bestGap = along + across;
                best = v;
            }
        }
        if (best >= 0) {
            darkTick[best] = now;
        }
    }

    /** Whether ground piece {@code p} is the spire well. */
    boolean well(int p) {
        return well[p];
    }

    /** The well's frame now (of {@code count} images). */
    int wellFrame(int count) {
        double seconds = arrivedTick == NEVER ? Double.NaN : (tick - arrivedTick) * SimStep.SECONDS;
        return Math.min(count - 1, wellFrameAt(seconds));
    }

    /** Ground piece {@code p}'s vein, -1 for another piece. */
    int vein(int p) {
        return veinOf[p];
    }

    /** Whether vein {@code v}'s core has died, for the tests. */
    boolean dark(int v) {
        return darkTick[v] != NEVER;
    }

    /** The veins followed. */
    int veins() {
        return glow.size();
    }

    /**
     * Vein {@code v}'s glow over its piece, its image's bottom left at (x, y) in screen px (additive, the caller's
     * blend), its bottom centre ({@code centreX} in play-field px) remembered for its core's death; at real time {@code
     * real}.
     */
    void drawVein(SpriteBatch batch, int v, float x, float y, double centreX, boolean mirrored, double real) {
        drawnX[v] = centreX;
        drawnY[v] = y;
        Array<AtlasRegion> frames = glow.get(v);
        if (frames == null) {
            return;
        }
        double dark = darkTick[v] == NEVER ? Double.NaN : (tick - darkTick[v]) * SimStep.SECONDS;
        int frame = veinFrame(real, dark);
        if (frame < 0) {
            return;
        }
        AtlasRegion image = frames.get(frame);
        if (mirrored) {
            batch.draw(image, x + image.getRegionWidth(), y, -image.getRegionWidth(), image.getRegionHeight());
        } else {
            batch.draw(image, x, y);
        }
    }
}
