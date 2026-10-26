package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import vanguard.sim.CruiserRun;
import vanguard.sim.CruiserRunSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Lob;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H, step H5: the finale's two Ascendancy cruisers (design/enemies/bosses/abyssal-maw, The cruisers and
 * Presentation; design/campaign Level 28, sections 8–10; tools/art/ascendancy_cruiser.py's sprites and
 * assets/pivots/ascendancy-cruiser.json), following the simulation's {@link CruiserRun} on the level clock, on {@code
 * low-air} over the low flyers ({@link #draw}): one hull for both flanks, bow up, its inner edge on the run's {@code
 * inner}; rising through the split ice it grows from {@value #RISE_SCALE} of its size and from dim to full over the
 * rise, its ice crust sheeting off ({@code -ice}, over the rise), its shadow on the ice once risen; its running glow
 * always (additive), cross-fading to the broadside's tell (every port hot amber) over a told band's first {@value
 * #TELL_FADE} s on its flank; the tell's amber tracer dashes in rows across the band; the port blasts along the inner
 * flank's ports in the band as the shells leave; a point-defence flash at a turret's burst; each flak marker over its fuse,
 * then its airburst at the ring (the flak's lobs are not drawn as the Vrell's: {@link #flakLob}); sparks where shots
 * glance off a hull. Presentation only; nothing is allocated per frame.
 */
final class CruiserLooks {
    /** The sprites' root (pipeline SpriteUse claims it with the level's {@code cruisers}). */
    static final String SLUG = "ascendancy-cruiser";
    /** The rise: from this share of its size, and this shade, to full. */
    static final float RISE_SCALE = 0.6f;

    static final float RISE_SHADE = 0.3f;
    /** The shadow's offset under the hull once risen (x right, y up), px. */
    static final int SHADOW_DX = 12;

    static final int SHADOW_DY = -16;
    /** The broadside's tell glow cross-fades in over the tell's first this many s. */
    static final double TELL_FADE = 0.5;
    /** The port blast, the point defence and the sparks at 20 fps; the airburst at 16 fps (the marker over its fuse). */
    static final int BLAST_TICKS = 3;

    static final int BURST_TICKS = 4;
    /** The tracer: rows across the band, dashes this far apart marching across at this speed (px/s). */
    static final int TRACER_ROWS = 3;

    static final double TRACER_GAP = 40;
    static final double TRACER_SPEED = 240;
    /** The flashes remembered at once. */
    static final int FLASHES = 16;

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final CruiserRunSpec spec;
    private final AtlasRegion hull;
    private final AtlasRegion shadow;
    private final AtlasRegion tracer;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> ice;
    private final Array<AtlasRegion> muzzle;
    private final Array<AtlasRegion> pd;
    private final Array<AtlasRegion> marker;
    private final Array<AtlasRegion> burst;
    private final Array<AtlasRegion> spark;
    /** The frame's size and per flank the inner ports' places in it (x right, y down from its top), px. */
    private final float frameWidth;

    private final float frameHeight;
    /** Ports on the left hull's inner (right) flank and the right hull's inner (left) flank. */
    private final float[][] portX = new float[2][];

    private final float[][] portY = new float[2][];
    private final int fuseTicks;
    /** Per flank its last broadside's step and band. */
    private final long[] blastTick = new long[2];

    private final double[] blastY = new double[2];
    /** Ring buffers: point-defence flashes, flak markers (their bursts after the fuse), sparks. */
    private final double[] pdX = new double[FLASHES];

    private final double[] pdY = new double[FLASHES];
    private final long[] pdTick = new long[FLASHES];
    private int nextPd;
    private final double[] flakX = new double[FLASHES];
    private final double[] flakY = new double[FLASHES];
    private final long[] flakTick = new long[FLASHES];
    private int nextFlak;
    private final double[] sparkX = new double[FLASHES];
    private final double[] sparkY = new double[FLASHES];
    private final long[] sparkTick = new long[FLASHES];
    private int nextSpark;
    /** The last step it followed. */
    private long now = NEVER;

    private CruiserLooks(Sprites sprites, CruiserRunSpec spec, JsonValue pivots) {
        this.spec = spec;
        hull = sprites == null ? null : sprites.frames(SLUG).first();
        shadow = single(sprites, SLUG + "-shadow");
        tracer = single(sprites, SLUG + "-tracer");
        glow = optional(sprites, SLUG + "-glow");
        ice = optional(sprites, SLUG + "-ice");
        muzzle = optional(sprites, SLUG + "-muzzle");
        pd = optional(sprites, SLUG + "-pd");
        marker = optional(sprites, SLUG + "-flak-marker");
        burst = optional(sprites, SLUG + "-flak");
        spark = optional(sprites, SLUG + "-spark");
        JsonValue frame = pivots == null ? null : pivots.get("frame");
        frameWidth = frame == null ? (float) spec.hull().width() : frame.getFloat(0);
        frameHeight = frame == null ? (float) spec.hull().height() : frame.getFloat(1);
        JsonValue ports = pivots == null ? null : pivots.get("ports");
        for (int side = 0; side < 2; side++) {
            // The left hull fires from its right flank's ports, the right hull from its left flank's.
            JsonValue flank = ports == null ? null : ports.get(side == CruiserRun.LEFT ? "right" : "left");
            int n = flank == null ? 0 : flank.size;
            portX[side] = new float[n];
            portY[side] = new float[n];
            for (int i = 0; i < n; i++) {
                portX[side][i] = flank.get(i).getFloat(0);
                portY[side][i] = flank.get(i).getFloat(1);
            }
        }
        fuseTicks = SimStep.ticks(spec.flak().fuseSeconds());
        reset();
    }

    /** The level's cruisers' looks, or null for a level without a run or without their sprites. */
    static CruiserLooks of(Sprites sprites, LevelScript script, JsonValue pivots) {
        if (script.hazards().cruisers().isEmpty() || !sprites.has(SLUG)) {
            return null;
        }
        return new CruiserLooks(sprites, script.hazards().cruisers().get(), pivots);
    }

    /** For the tests: the looks of a run without its sprites (they draw nothing). */
    static CruiserLooks following(CruiserRunSpec spec, JsonValue pivots) {
        return new CruiserLooks(null, spec, pivots);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion single(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name).first() : null;
    }

    /** The level restarts: no flash remembered. */
    void reset() {
        Arrays.fill(blastTick, NEVER);
        Arrays.fill(pdTick, NEVER);
        Arrays.fill(flakTick, NEVER);
        Arrays.fill(sparkTick, NEVER);
    }

    // --- the looks' numbers ------------------------------------------------------------------------

    /** The hull's scale at rise share {@code rise} (0–1). */
    static float scale(double rise) {
        return (float) (RISE_SCALE + (1 - RISE_SCALE) * rise);
    }

    /** The hull's shade at rise share {@code rise}: dim in the ice, full once risen. */
    static float shade(double rise) {
        return (float) (RISE_SHADE + (1 - RISE_SHADE) * rise);
    }

    /** The ice crust's frame at rise share {@code rise}: over the rise, -1 once risen. */
    static int iceFrame(double rise, int frames) {
        return rise >= 1 || frames == 0 ? -1 : Math.min(frames - 1, (int) (rise * frames));
    }

    /** The broadside tell glow's share {@code seconds} into a tell. */
    static float tellGlow(double seconds) {
        return (float) Math.clamp(seconds / TELL_FADE, 0, 1);
    }

    /** The level clock's step now (the one the run last stepped at). */
    static int levelTick(Sortie sortie) {
        return (int) Math.floor(sortie.levelSeconds() * SimStep.PER_SECOND + 1e-6);
    }

    /** Whether a lob is one of the cruisers' flak shells (drawn by its marker, not as a Vrell blob). */
    boolean flakLob(Lob lob) {
        for (int i = 0; i < FLASHES; i++) {
            if (flakTick[i] != NEVER
                    && now - flakTick[i] <= fuseTicks + 1
                    && flakX[i] == lob.targetX()
                    && flakY[i] == lob.targetY()) {
                return true;
            }
        }
        return false;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each simulation step: the broadsides, point-defence bursts, flak markers and sparks off the hulls. */
    void stepped(Sortie sortie) {
        CruiserRun run = sortie.cruisers().orElse(null);
        if (run == null) {
            return;
        }
        long tick = sortie.tick();
        now = tick;
        int at = levelTick(sortie);
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            switch (events.type(e)) {
                case BROADSIDE -> {
                    int side = Math.clamp(events.value(e), 0, 1);
                    blastTick[side] = tick;
                    blastY[side] = events.y(e);
                }
                case POINT_DEFENCE -> {
                    pdX[nextPd] = events.x(e);
                    pdY[nextPd] = events.y(e);
                    pdTick[nextPd] = tick;
                    nextPd = (nextPd + 1) % FLASHES;
                }
                case FLAK -> {
                    flakX[nextFlak] = events.x(e);
                    flakY[nextFlak] = events.y(e);
                    flakTick[nextFlak] = tick;
                    nextFlak = (nextFlak + 1) % FLASHES;
                }
                case SHOT_GLANCED -> {
                    if (run.present(at) && onHull(run, at, events.x(e), events.y(e))) {
                        sparkX[nextSpark] = events.x(e);
                        sparkY[nextSpark] = events.y(e);
                        sparkTick[nextSpark] = tick;
                        nextSpark = (nextSpark + 1) % FLASHES;
                    }
                }
                default -> {}
            }
        }
    }

    /** Whether (x, y) lies on a hull (its box, a few px to spare) at level clock step {@code at}. */
    private boolean onHull(CruiserRun run, int at, double x, double y) {
        double halfW = spec.hull().width() / 2 + 6;
        double halfH = spec.hull().height() / 2 + 6;
        double hy = run.hullY(at);
        return Math.abs(y - hy) <= halfH
                && (Math.abs(x - run.hullX(CruiserRun.LEFT)) <= halfW
                        || Math.abs(x - run.hullX(CruiserRun.RIGHT)) <= halfW);
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * On {@code low-air}: the shadows, the hulls in their rise with the ice crust, their glow and tells, the tracer
     * dashes, the port blasts, the point defence, the flak markers and airbursts, the sparks.
     */
    void draw(SpriteBatch batch, Sortie sortie) {
        CruiserRun run = sortie.cruisers().orElse(null);
        int at = levelTick(sortie);
        if (run == null || hull == null || !run.present(at)) {
            drawFlak(batch, sortie.tick());
            return;
        }
        long tick = sortie.tick();
        double rise = run.riseShare(at);
        double y = run.hullY(at);
        float scale = scale(rise);
        if (shadow != null && rise > 0) {
            batch.setColor(1, 1, 1, (float) (rise * rise));
            for (int side = 0; side < 2; side++) {
                drawScaled(batch, shadow, run.hullX(side) + SHADOW_DX, y + SHADOW_DY, scale);
            }
        }
        float shade = shade(rise);
        batch.setColor(shade, shade, shade, 1);
        int iceFrame = iceFrame(rise, ice.size);
        for (int side = 0; side < 2; side++) {
            drawScaled(batch, hull, run.hullX(side), y, scale);
            if (iceFrame >= 0) {
                drawScaled(batch, ice.get(iceFrame), run.hullX(side), y, scale);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        double tellSeconds = spec.broadside().tellSeconds();
        for (int side = 0; side < 2; side++) {
            float tell = 0;
            for (int k = 0; k < CruiserRun.BANDS; k++) {
                if (run.bandTelling(k) && run.bandSide(k) == side) {
                    tell = Math.max(tell, tellGlow(run.bandProgress(k) * tellSeconds));
                }
            }
            if (!glow.isEmpty()) {
                batch.setColor(1, 1, 1, (float) rise * (1 - tell));
                drawScaled(batch, glow.first(), run.hullX(side), y, scale);
            }
            if (glow.size > 1 && tell > 0) {
                batch.setColor(1, 1, 1, tell);
                drawScaled(batch, glow.get(1), run.hullX(side), y, scale);
            }
        }
        drawTracers(batch, run, tick);
        drawBlasts(batch, run, y, tick);
        drawFlashes(batch, tick);
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawFlak(batch, tick);
    }

    /** A told band's tracer dashes: rows across it from its flank's inner edge, marching across, brightening. */
    private void drawTracers(SpriteBatch batch, CruiserRun run, long tick) {
        if (tracer == null) {
            return;
        }
        for (int k = 0; k < CruiserRun.BANDS; k++) {
            if (!run.bandTelling(k)) {
                continue;
            }
            boolean left = run.bandSide(k) == CruiserRun.LEFT;
            double from = left ? spec.leftInner() : spec.rightInner();
            double band = run.bandHeight();
            double march = (tick * SimStep.SECONDS * TRACER_SPEED) % TRACER_GAP;
            batch.setColor(1, 1, 1, (float) (0.35 + 0.65 * run.bandProgress(k)));
            for (int row = 0; row < TRACER_ROWS; row++) {
                double y = run.bandY(k) + band * ((row + 0.5) / TRACER_ROWS - 0.5);
                double offset = row % 2 == 0 ? 0 : TRACER_GAP / 2;
                for (double d = march + offset; d < PlayField.WIDTH; d += TRACER_GAP) {
                    double x = left ? from + d : from - d;
                    if (x < 0 || x > PlayField.WIDTH) {
                        break;
                    }
                    drawScaled(batch, tracer, x, y, 1);
                }
            }
        }
    }

    /** The port blasts along each flank's inner ports in its last broadside's band. */
    private void drawBlasts(SpriteBatch batch, CruiserRun run, double y, long tick) {
        if (muzzle.isEmpty()) {
            return;
        }
        batch.setColor(Color.WHITE);
        double half = run.bandHeight() / 2 + 10;
        for (int side = 0; side < 2; side++) {
            int frame = (int) ((tick - blastTick[side]) / BLAST_TICKS);
            if (blastTick[side] == NEVER || frame < 0 || frame >= muzzle.size) {
                continue;
            }
            double left = run.hullX(side) - frameWidth / 2;
            double top = y + frameHeight / 2;
            for (int i = 0; i < portX[side].length; i++) {
                double py = top - portY[side][i];
                if (Math.abs(py - blastY[side]) <= half) {
                    drawScaled(batch, muzzle.get(frame), left + portX[side][i], py, 1);
                }
            }
        }
    }

    /** The point-defence flashes and the sparks off the hulls. */
    private void drawFlashes(SpriteBatch batch, long tick) {
        batch.setColor(Color.WHITE);
        for (int i = 0; i < FLASHES; i++) {
            int f = (int) ((tick - pdTick[i]) / BLAST_TICKS);
            if (pdTick[i] != NEVER && f >= 0 && f < pd.size) {
                drawScaled(batch, pd.get(f), pdX[i], pdY[i], 1);
            }
            int s = (int) ((tick - sparkTick[i]) / BLAST_TICKS);
            if (sparkTick[i] != NEVER && s >= 0 && s < spark.size) {
                drawScaled(batch, spark.get(s), sparkX[i], sparkY[i], 1);
            }
        }
    }

    /** Each flak marker over its fuse (additive), then its airburst (normal, its hot core baked in). */
    private void drawFlak(SpriteBatch batch, long tick) {
        for (int i = 0; i < FLASHES; i++) {
            if (flakTick[i] == NEVER) {
                continue;
            }
            long age = tick - flakTick[i];
            if (age < fuseTicks && !marker.isEmpty()) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                int f = (int) Math.min(marker.size - 1, age * marker.size / Math.max(1, fuseTicks));
                drawScaled(batch, marker.get(f), flakX[i], flakY[i], 1);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                int f = (int) ((age - fuseTicks) / BURST_TICKS);
                if (f >= 0 && f < burst.size) {
                    drawScaled(batch, burst.get(f), flakX[i], flakY[i], 1);
                }
            }
        }
    }
}
