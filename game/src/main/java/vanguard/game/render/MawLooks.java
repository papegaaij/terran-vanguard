package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.BossSpec;
import vanguard.sim.CoilArena;
import vanguard.sim.CoilSpec;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H, step H5: the coiled arena boss's looks (the Abyssal Maw, design/enemies/bosses/abyssal-maw, Presentation and
 * Tells; tools/art/abyssal_maw.py's sprites, briefs/m8arth1-rows.md's names), following the simulation's {@link
 * CoilArena}. In the play field's stack over the ground units ({@link #draw}), back to front: the dive's darkness over
 * the caldera (the vent guttering: {@link DarkLight#ventDim}), the lunge's shadow running up its column with the silt
 * lifting in it, the coil's segments from the tail to the head (32 headings, the tail's taper; while the coil is under
 * the water, rising or diving, smaller, darker and fading), a sweep's silt streaming off its bight in the tell, the
 * lunge's body up the column, the gill sacs (idle, swelling, full, torn), the Helix implants (shut, sparking, open,
 * blown) and the head in its seven poses (rest, the throat's glow, open, reared in phase 3, the dive, the lunge, dead; a
 * hit flashing it at most once each 0.25 s, {@link HitFlash}); after the kill its death: the implants blowing out 0.4 s
 * apart, the segments sinking tail to head each in a burst of ichor, the head sinking last. After the grade, ungraded and
 * additive ({@link #drawGlows}): the coil's teal lines (lime over a sweep's bight in its tell, swing and hold and over a
 * shed point's flaring plate, with its flare), the sacs' and the implants' glow, the implants' arc, the head's eyes,
 * flank lines and throat, the jaw rim's teal orbs in a fan wall's tell, the blow-outs' flash and the vent's flare as the
 * head sinks. Presentation only; nothing is allocated per frame.
 */
final class MawLooks {
    /** The segment's headings and tapers ({@code -segment_<heading * 4 + taper>}). */
    static final int HEADINGS = 32;

    static final int TAPERS = 4;
    /** The tail's taper: its last segments in steps of this many (the last three 3, the three before 2, ...). */
    static final int TAPER_STEP = 3;
    /** The head's poses ({@code -head_0..6}). */
    static final int REST = 0;

    static final int GLOW = 1;
    static final int OPEN = 2;
    static final int REAR = 3;
    static final int DIVE = 4;
    static final int LUNGE = 5;
    static final int DEAD = 6;
    static final int HEAD_FRAMES = 7;
    /** The sacs' frames: idle, swelling, full, torn (the torn sac has no glow). */
    static final int SAC_IDLE = 0;

    static final int SAC_SWELLING = 1;
    static final int SAC_FULL = 2;
    static final int SAC_TORN = 3;
    /** The implants' frames: shut, spark a, spark b, open, blown (their glow: frames 1-3). */
    static final int IMPLANT_SHUT = 0;

    static final int IMPLANT_OPEN = 3;
    static final int IMPLANT_BLOWN = 4;
    /** The implant's tell's two spark frames alternate at about 12 fps. */
    static final int SPARK_TICKS = 5;
    /** The arc crawls at about 16 fps; the silt streams at 10 fps; the ichor and blow-outs at 10 and 12 fps. */
    static final int ARC_TICKS = 4;

    static final int SILT_TICKS = 6;
    static final int ICHOR_TICKS = 6;
    static final int BLOWOUT_TICKS = 5;
    /** A shed point's flare: frames 0-4 over its tell, frame 5 as the eels slip out (this many steps). */
    static final int SHED_FRAMES = 6;

    static final int SHED_OUT_TICKS = 10;
    /**
     * Under the water (the intro's rise, the dive): at depth 1 the coil is drawn this much smaller, this much darker and
     * this much fainter; its glows keep most of their light (the frame in the dark).
     */
    static final float SUB_SMALLER = 0.18f;

    static final float SUB_DARKER = 0.7f;
    static final float SUB_FAINTER = 0.45f;
    static final float GLOW_FAINTER = 0.35f;
    /** The dive's darkness over the caldera at its deepest, and how far the vent's light gutters then. */
    static final float DIVE_DARK = 0.72f;

    static final float VENT_DIM = 0.85f;

    private static final Color DARK = new Color(0.02f, 0.03f, 0.05f, 1);
    /** The lunge's body up the column: the segment's heading facing up the screen (toward the head). */
    static final int UP = HEADINGS / 2;
    /** The death: the blow-outs' gap, each segment's and the head's sink, s; the vent's flare (4 fps, x1.6). */
    static final double BLOWOUT_GAP = 0.4;

    static final double SEGMENT_SINK = 0.8;
    static final double HEAD_SINK = 1.0;
    static final int FLARE_TICKS = 15;
    static final float FLARE_SCALE = 1.6f;
    /** A sinking part shrinks to this share and darkens to this shade. */
    static final float SINK_SCALE = 0.7f;

    static final float SINK_SHADE = 0.2f;
    /**
     * The vent's throat on the halted arena (Level 28's caldera piece, briefs/m8arth2-rows.md: screen (240, 330), y down),
     * px, y up: the flare's centre.
     */
    static final double VENT_X = 240;

    static final double VENT_Y = PlayField.HEIGHT - 330;

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final FlashShader flash;
    private final TextureRegion pixel;
    private final Array<AtlasRegion> segment;
    private final Array<AtlasRegion> segmentGlow;
    private final Array<AtlasRegion> segmentTell;
    private final Array<AtlasRegion> head;
    private final Array<AtlasRegion> headGlow;
    private final Array<AtlasRegion> headRim;
    private final Array<AtlasRegion> sac;
    private final Array<AtlasRegion> sacGlow;
    private final Array<AtlasRegion> implant;
    private final Array<AtlasRegion> implantGlow;
    private final Array<AtlasRegion> arc;
    private final Array<AtlasRegion> shed;
    private final Array<AtlasRegion> silt;
    private final Array<AtlasRegion> shadowHead;
    private final Array<AtlasRegion> shadowColumn;
    private final Array<AtlasRegion> blowout;
    private final Array<AtlasRegion> blowoutGlow;
    private final Array<AtlasRegion> ichor;
    private final Array<AtlasRegion> flare;
    private final HitFlash hits = new HitFlash();

    private final int segments;
    private final double spacing;
    private final int[] sacs;
    private final int implantPhase;
    private final double deathSeconds;
    private final int swellTicks;
    private final int shedTellTicks;

    /** The sac swelling (a part, -1 none) and the step its swell started. */
    private int swelling = -1;

    private long swellTick = NEVER;
    /** The shed tell's start and end steps and its segment (the plate flaring); -1 for none yet. */
    private long shedTick = NEVER;

    private long shedEnd = NEVER;
    private int shedSegment = -1;
    private boolean wasTelling;
    /** The kill's step; NEVER while it lives. */
    private long deathTick = NEVER;
    /** At the kill: the segments' places and frames, the head's place, the implants' and sacs' places and states. */
    private final double[] deadX;

    private final double[] deadY;
    private final int[] deadFrame;
    private double deadHeadX;
    private double deadHeadY;
    private final double[] deadImplantX;
    private final double[] deadImplantY;
    private final boolean[] implantBlown;
    private final double[] deadSacX;
    private final double[] deadSacY;
    private final boolean[] sacTorn;

    private MawLooks(Sprites sprites, FlashShader flash, SetPieceSpec boss) {
        this.flash = flash;
        String slug = boss.slug();
        pixel = sprites == null ? null : sprites.pixel;
        segment = optional(sprites, slug + "-segment");
        segmentGlow = optional(sprites, slug + "-segment-glow");
        segmentTell = optional(sprites, slug + "-segment-tell");
        head = optional(sprites, slug + "-head");
        headGlow = optional(sprites, slug + "-head-glow");
        headRim = optional(sprites, slug + "-head-rim");
        sac = optional(sprites, slug + "-sac");
        sacGlow = optional(sprites, slug + "-sac-glow");
        implant = optional(sprites, slug + "-implant");
        implantGlow = optional(sprites, slug + "-implant-glow");
        arc = optional(sprites, slug + "-arc");
        shed = optional(sprites, slug + "-shed");
        silt = optional(sprites, slug + "-silt");
        shadowHead = optional(sprites, slug + "-shadow-head");
        shadowColumn = optional(sprites, slug + "-shadow-column");
        blowout = optional(sprites, slug + "-blowout");
        blowoutGlow = optional(sprites, slug + "-blowout-glow");
        ichor = optional(sprites, slug + "-ichor");
        flare = optional(sprites, slug + "-flare");
        BossSpec spec = boss.boss().orElseThrow();
        CoilSpec coil = spec.coil().orElseThrow();
        segments = coil.body().segments();
        spacing = coil.body().spacing();
        sacs = coil.sacs().stream().mapToInt(Integer::intValue).toArray();
        implantPhase = coil.implants().map(CoilSpec.Implants::phase).orElse(Integer.MAX_VALUE);
        int implants = coil.implants().map(i -> i.parts().size()).orElse(0);
        deathSeconds = Math.max(spec.deathSeconds(), BLOWOUT_GAP * implants + SEGMENT_SINK + HEAD_SINK);
        swellTicks = Math.max(1, SimStep.ticks(coil.swellSeconds()));
        shedTellTicks =
                Math.max(1, coil.shed().map(s -> SimStep.ticks(s.tellSeconds())).orElse(1));
        deadX = new double[segments];
        deadY = new double[segments];
        deadFrame = new int[segments];
        deadImplantX = new double[implants];
        deadImplantY = new double[implants];
        implantBlown = new boolean[implants];
        deadSacX = new double[sacs.length];
        deadSacY = new double[sacs.length];
        sacTorn = new boolean[sacs.length];
        reset();
    }

    /** The looks of the level's boss when it coils (its data's {@code coil}) and its head and segments are there; else null. */
    static MawLooks of(Sprites sprites, FlashShader flash, SetPieceSpec boss) {
        if (boss == null
                || boss.boss().flatMap(BossSpec::coil).isEmpty()
                || !sprites.has(boss.slug() + "-head")
                || !sprites.has(boss.slug() + "-segment")) {
            return null;
        }
        return new MawLooks(sprites, flash, boss);
    }

    /** For the tests: the looks following a sortie of {@code boss} without its sprites (they draw nothing). */
    static MawLooks following(SetPieceSpec boss) {
        return new MawLooks(null, null, boss);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The level restarts (or the fight is retried): no swell, no shed, no death. */
    void reset() {
        swelling = -1;
        swellTick = NEVER;
        shedTick = NEVER;
        shedEnd = NEVER;
        shedSegment = -1;
        wasTelling = false;
        deathTick = NEVER;
        hits.reset();
    }

    // --- the frames --------------------------------------------------------------------------------

    /**
     * The segment's heading for the simulation's {@code angle} (radians, 0 to the right, y up, toward the tail): the way
     * toward the head, k × 11.25° clockwise from straight down (tools/art/abyssal_maw.py).
     */
    static int heading(double angle) {
        return Math.floorMod((int) Math.round((Math.PI / 2 - angle) * HEADINGS / (2 * Math.PI)), HEADINGS);
    }

    /** Segment {@code i}'s taper of {@code count} (the head's end first): the tail's last three 3, then 2, then 1. */
    static int taper(int i, int count) {
        int fromTail = count - 1 - i;
        return Math.max(0, TAPERS - 1 - fromTail / TAPER_STEP);
    }

    /** The segment frame for heading {@code heading} and taper {@code taper}. */
    static int segmentFrame(int heading, int taper) {
        return heading * TAPERS + taper;
    }

    /**
     * The head's pose: the lunge's (facing up) while it races up the column and stays there, the dive's while the coil
     * slides into the dark, waits or slides back; otherwise the throat's glow before a fan or a wall, open in the
     * throat's window, reared from the implants' phase on, else at rest.
     */
    static int headFrame(CoilArena.DiveState dive, boolean glowing, boolean open, boolean reared) {
        return switch (dive) {
            case LUNGE, BODY -> LUNGE;
            case SLIDE, QUIET, SHADOW, RETURN -> DIVE;
            case READY, DONE -> glowing ? GLOW : open ? OPEN : reared ? REAR : REST;
        };
    }

    /** A sac's frame: torn when destroyed, else swelling over the first half of its swell, full over the second. */
    static int sacFrame(boolean torn, boolean swelling, double swellShare) {
        if (torn) {
            return SAC_TORN;
        }
        if (!swelling) {
            return SAC_IDLE;
        }
        return swellShare < 0.5 ? SAC_SWELLING : SAC_FULL;
    }

    /** An implant's frame: blown when destroyed, the tell's sparks alternating, open, else shut. */
    static int implantFrame(CoilArena.ImplantState state, boolean blown, long tick) {
        if (blown) {
            return IMPLANT_BLOWN;
        }
        return switch (state) {
            case SHUT -> IMPLANT_SHUT;
            case TELL -> 1 + (int) Math.floorMod(tick / SPARK_TICKS, 2L);
            case OPEN -> IMPLANT_OPEN;
        };
    }

    /** The coil's scale at {@code depth} (0 on the play plane, 1 deep in the dark). */
    static float subScale(double depth) {
        return (float) (1 - SUB_SMALLER * depth);
    }

    static float subShade(double depth) {
        return (float) (1 - SUB_DARKER * depth);
    }

    static float subAlpha(double depth) {
        return (float) (1 - SUB_FAINTER * depth);
    }

    static float glowAlpha(double depth) {
        return (float) (1 - GLOW_FAINTER * depth);
    }

    /** The dive's darkness over the caldera (0–{@value #DIVE_DARK}): its depth while it dives, none otherwise. */
    static float darkness(CoilArena coil) {
        return coil.diving() ? (float) (DIVE_DARK * coil.depth()) : 0;
    }

    /** The lunge's body under a head at height {@code headY}: segments {@code spacing} apart down past the bottom edge. */
    static int columnSegments(double headY, double spacing, int count) {
        return (int) Math.min(count, Math.max(0, Math.floor(headY / spacing) + 1));
    }

    /** When segment {@code i} (of {@code count}, head first) starts to sink after the kill, s: tail to head. */
    static double sinkStart(int i, int count, int implants, double deathSeconds) {
        double from = BLOWOUT_GAP * implants;
        double to = Math.max(from, deathSeconds - HEAD_SINK - SEGMENT_SINK);
        int fromTail = count - 1 - i;
        return count <= 1 ? from : from + (to - from) * fromTail / (count - 1);
    }

    /** When the head starts to sink after the kill, s: last, after the coil. */
    static double headSinkStart(double deathSeconds) {
        return deathSeconds - HEAD_SINK;
    }

    /** How far a part has sunk {@code age} s after the kill when it starts at {@code start} over {@code length}, 0–1. */
    static double sunk(double age, double start, double length) {
        return Math.clamp((age - start) / length, 0, 1);
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The coiled boss among the set pieces, or null. */
    static SetPiece maw(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.coil().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** After each simulation step: a sac's swell, a shed's tell and its plate, the kill. */
    void stepped(Sortie sortie) {
        SetPiece piece = maw(sortie);
        if (piece == null) {
            return;
        }
        CoilArena coil = piece.coil().orElseThrow();
        long tick = sortie.tick();
        if (piece.destroyed()) {
            if (deathTick == NEVER) {
                died(piece, coil, tick);
            }
            return;
        }
        if (deathTick != NEVER) {
            // A retry from the boss checkpoint brings it back.
            reset();
        }
        if (!piece.present()) {
            return;
        }
        int swell = coil.swellingSac();
        if (swell != swelling) {
            swelling = swell;
            swellTick = tick;
        }
        boolean telling = coil.shedTelling();
        if (telling && !wasTelling) {
            // A new tell: its plate, the segment nearest the point.
            shedTick = tick;
            shedEnd = NEVER;
            shedSegment = nearestSegment(coil, coil.shedX(coil.shedPoint()), coil.shedY(coil.shedPoint()));
        } else if (!telling && wasTelling) {
            shedEnd = tick;
        }
        wasTelling = telling;
    }

    private static int nearestSegment(CoilArena coil, double x, double y) {
        int best = 0;
        double nearest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < coil.segmentCount(); i++) {
            double d = Math.hypot(coil.segmentX(i) - x, coil.segmentY(i) - y);
            if (d < nearest) {
                nearest = d;
                best = i;
            }
        }
        return best;
    }

    /** The kill: every part's place and state as it died. */
    private void died(SetPiece piece, CoilArena coil, long tick) {
        deathTick = tick;
        int n = Math.min(segments, coil.segmentCount());
        for (int i = 0; i < n; i++) {
            deadX[i] = coil.segmentX(i);
            deadY[i] = coil.segmentY(i);
            deadFrame[i] = segmentFrame(heading(coil.segmentAngle(i)), taper(i, n));
        }
        deadHeadX = coil.headX();
        deadHeadY = coil.headY();
        for (int k = 0; k < deadImplantX.length && k < coil.implantCount(); k++) {
            deadImplantX[k] = coil.implantX(k);
            deadImplantY[k] = coil.implantY(k);
            implantBlown[k] = piece.partWrecked(coil.implantPart(k));
        }
        for (int s = 0; s < sacs.length; s++) {
            deadSacX[s] = deadHeadX + piece.partOffsetX(sacs[s]);
            deadSacY[s] = deadHeadY + piece.partOffsetY(sacs[s]);
            sacTorn[s] = piece.partWrecked(sacs[s]);
        }
    }

    /** Whether its death plays: from the kill for its length and the flare's. */
    boolean drawsDeath(long tick, float alpha) {
        double age = deathAge(tick, alpha);
        return age >= 0 && age < deathSeconds + flareSeconds();
    }

    /** How long ago it was killed, s, between the last step and the next; -1 while it lives. */
    double deathAge(long tick, float alpha) {
        return deathTick == NEVER ? -1 : (tick + alpha - deathTick) * SimStep.SECONDS;
    }

    /** Its death's length, s. */
    double deathSeconds() {
        return deathSeconds;
    }

    private double flareSeconds() {
        return Math.max(1, flare.size) * FLARE_TICKS * SimStep.SECONDS;
    }

    /** The segment flaring for a shed now (its tell and the slip-out), -1 for none. */
    int shedSegment(long tick) {
        if (shedTick == NEVER || shedSegment < 0) {
            return -1;
        }
        return shedEnd == NEVER || tick - shedEnd < SHED_OUT_TICKS ? shedSegment : -1;
    }

    /** The shed flare's frame at step {@code tick}: 0–4 over the tell, 5 as the eels slip out. */
    int shedFrame(long tick) {
        if (shedEnd != NEVER) {
            return SHED_FRAMES - 1;
        }
        return (int) Math.min(SHED_FRAMES - 2, (tick - shedTick) * (SHED_FRAMES - 1) / shedTellTicks);
    }

    /** The share of sac {@code part}'s swell run at step {@code tick} (0–1); 0 when it does not swell. */
    double swellShare(int part, long tick) {
        return part == swelling && swellTick != NEVER ? Math.min(1, (double) (tick - swellTick) / swellTicks) : 0;
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * Over the ground units: the dive's darkness, the lunge's shadow and silt, the coil, a sweep's silt, the lunge's
     * body, the sacs, the implants and the head; after the kill its death.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        SetPiece piece = maw(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (deathTick != NEVER && piece.destroyed()) {
            drawDeath(batch, tick, alpha);
            return;
        }
        if (!piece.present()) {
            return;
        }
        CoilArena coil = piece.coil().orElseThrow();
        float dark = darkness(coil);
        if (dark > 0 && pixel != null) {
            batch.setColor(DARK.r, DARK.g, DARK.b, dark);
            batch.draw(pixel, X0, 0, PlayField.WIDTH, PlayField.HEIGHT);
            batch.setColor(Color.WHITE);
        }
        drawShadow(batch, coil, tick);
        double depth = coil.depth();
        drawCoil(batch, coil, depth);
        drawSilt(batch, coil, tick, depth);
        drawColumn(batch, coil);
        float scale = subScale(depth);
        float shade = subShade(depth);
        float fade = subAlpha(depth);
        double x = piece.renderX(alpha);
        double y = piece.renderY(alpha);
        boolean lunging = coil.diveState() == CoilArena.DiveState.LUNGE || coil.diveState() == CoilArena.DiveState.BODY;
        for (int sacPart : sacs) {
            if (lunging) {
                // Facing up the column the sacs lie behind the head.
                break;
            }
            int frame = sacFrame(piece.partWrecked(sacPart), sacPart == swelling, swellShare(sacPart, tick));
            drawPart(
                    batch,
                    piece,
                    sacPart,
                    sac,
                    frame,
                    x + piece.partOffsetX(sacPart),
                    y + piece.partOffsetY(sacPart),
                    scale,
                    shade,
                    fade,
                    tick,
                    whiteFlash);
        }
        for (int i = 0; i < coil.implantCount(); i++) {
            int part = coil.implantPart(i);
            int frame = implantFrame(coil.implantState(i), piece.partWrecked(part), tick);
            drawPart(
                    batch,
                    piece,
                    part,
                    implant,
                    frame,
                    coil.implantX(i),
                    coil.implantY(i),
                    scale,
                    shade,
                    fade,
                    tick,
                    whiteFlash);
        }
        int pose = headFrame(coil.diveState(), coil.throatGlowing(), coil.jawOpen(), piece.phase() >= implantPhase);
        boolean onPlane = pose == LUNGE;
        drawPart(
                batch,
                piece,
                0,
                head,
                pose,
                x,
                y,
                onPlane ? 1 : scale,
                onPlane ? 1 : shade,
                onPlane ? 1 : fade,
                tick,
                whiteFlash);
        batch.setColor(Color.WHITE);
    }

    /** A part's frame at (x, y) in the sub look; at full scale a hit flashes it. */
    private void drawPart(
            SpriteBatch batch,
            SetPiece piece,
            int part,
            Array<AtlasRegion> frames,
            int frame,
            double x,
            double y,
            float scale,
            float shade,
            float fade,
            long tick,
            float whiteFlash) {
        if (frames.isEmpty()) {
            return;
        }
        AtlasRegion region = frames.get(Math.min(frames.size - 1, frame));
        if (scale >= 1 && hits.on(part, tick, piece.partTicksSinceHit(part)) && flash != null) {
            flash.draw(batch, region, Math.round(X0 + x), Math.round(y), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
            return;
        }
        batch.setColor(shade, shade, shade, fade);
        drawScaled(batch, region, x, y, scale);
        batch.setColor(Color.WHITE);
    }

    /** The coil's segments from the tail to the head, so the plates shingle toward the head. */
    private void drawCoil(SpriteBatch batch, CoilArena coil, double depth) {
        if (segment.isEmpty()) {
            return;
        }
        float scale = subScale(depth);
        float shade = subShade(depth);
        batch.setColor(shade, shade, shade, subAlpha(depth));
        int n = coil.segmentCount();
        for (int i = n - 1; i >= 0; i--) {
            int frame = segmentFrame(heading(coil.segmentAngle(i)), taper(i, n));
            drawScaled(
                    batch, segment.get(Math.min(segment.size - 1, frame)), coil.segmentX(i), coil.segmentY(i), scale);
        }
        batch.setColor(Color.WHITE);
    }

    /** A sweep's silt streaming off its bight in the tell; the silt falling off the coil as it rises. */
    private void drawSilt(SpriteBatch batch, CoilArena coil, long tick, double depth) {
        if (silt.isEmpty()) {
            return;
        }
        boolean tell = coil.sweepState() == CoilArena.SweepState.TELL;
        double rising = coil.up() ? 0 : 1 - coil.introShare();
        if (!tell && rising <= 0) {
            return;
        }
        for (int i = 0; i < coil.segmentCount(); i++) {
            boolean bight = tell && coil.inBight(i) && i % 2 == 0;
            boolean rise = rising > 0 && i % 3 == 0;
            if (!bight && !rise) {
                continue;
            }
            batch.setColor(1, 1, 1, bight ? 1 : (float) rising);
            int frame = (int) Math.floorMod(tick / SILT_TICKS + i, (long) silt.size);
            drawScaled(batch, silt.get(frame), coil.segmentX(i), coil.segmentY(i), subScale(depth));
        }
        batch.setColor(Color.WHITE);
    }

    /** The lunge's shadow running up its column from the bottom edge, the silt lifting in it; under the lunge fading. */
    private void drawShadow(SpriteBatch batch, CoilArena coil, long tick) {
        CoilArena.DiveState state = coil.diveState();
        if (state != CoilArena.DiveState.SHADOW && state != CoilArena.DiveState.LUNGE) {
            return;
        }
        double progress = coil.diveProgress();
        double tip = state == CoilArena.DiveState.SHADOW ? progress * coil.headY() : coil.headY();
        float opacity = state == CoilArena.DiveState.SHADOW ? 1 : (float) (1 - progress);
        double x = coil.columnX();
        batch.setColor(1, 1, 1, opacity);
        if (!shadowColumn.isEmpty()) {
            AtlasRegion tile = shadowColumn.first();
            int h = tile.getRegionHeight();
            double top = tip - (shadowHead.isEmpty() ? 0 : shadowHead.first().getRegionHeight() / 2.0);
            for (double y = h / 2.0; y - h / 2.0 < top; y += h) {
                drawScaled(batch, tile, x, y, 1);
            }
        }
        if (!shadowHead.isEmpty()) {
            drawScaled(batch, shadowHead.first(), x, tip, 1);
        }
        if (state == CoilArena.DiveState.SHADOW && !silt.isEmpty()) {
            for (int k = 0; k < 3; k++) {
                double y = tip - 40 - 70 * k;
                if (y > 0) {
                    int frame = (int) Math.floorMod(tick / SILT_TICKS + 3L * k, (long) silt.size);
                    drawScaled(batch, silt.get(frame), x + (k % 2 == 0 ? -14 : 14), y, 1);
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** The lunge's body up the column under the head (facing up), fading on the way back. */
    private void drawColumn(SpriteBatch batch, CoilArena coil) {
        CoilArena.DiveState state = coil.diveState();
        if (segment.isEmpty()
                || (state != CoilArena.DiveState.LUNGE
                        && state != CoilArena.DiveState.BODY
                        && state != CoilArena.DiveState.RETURN)) {
            return;
        }
        float opacity = state == CoilArena.DiveState.RETURN ? (float) (1 - coil.diveProgress()) : 1;
        double x = state == CoilArena.DiveState.RETURN ? coil.columnX() : coil.headX();
        batch.setColor(1, 1, 1, opacity);
        int n = segments;
        int k = columnSegments(coil.headY(), spacing, n);
        // The column's segments toward the bottom edge first, so the plates shingle up toward the head.
        for (int j = k - 1; j >= 0; j--) {
            double y = coil.headY() - spacing * (j + 1);
            int frame = segmentFrame(UP, taper(j, n));
            drawScaled(batch, segment.get(Math.min(segment.size - 1, frame)), x, y, 1);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * Its death: the implants blowing out 0.4 s apart (then their crater), the segments sinking tail to head each in a
     * burst of ichor, the sacs and the head (dead) sinking last.
     */
    private void drawDeath(SpriteBatch batch, long tick, float alpha) {
        double age = deathAge(tick, alpha);
        if (age < 0 || age >= deathSeconds) {
            return;
        }
        int n = segments;
        int implants = deadImplantX.length;
        for (int i = n - 1; i >= 0; i--) {
            double start = sinkStart(i, n, implants, deathSeconds);
            double share = sunk(age, start, SEGMENT_SINK);
            if (share >= 1 || segment.isEmpty()) {
                continue;
            }
            float shade = (float) (1 - (1 - SINK_SHADE) * share);
            batch.setColor(shade, shade, shade, (float) (1 - share));
            drawScaled(batch, segment.get(Math.min(segment.size - 1, deadFrame[i])), deadX[i], deadY[i], (float)
                    (1 - (1 - SINK_SCALE) * share));
        }
        batch.setColor(Color.WHITE);
        for (int i = n - 1; i >= 0; i--) {
            int frame = (int) ((age - sinkStart(i, n, implants, deathSeconds)) / (ICHOR_TICKS * SimStep.SECONDS));
            if (frame >= 0 && frame < ichor.size && i % 2 == 0) {
                drawScaled(batch, ichor.get(frame), deadX[i], deadY[i], 1);
            }
        }
        for (int k = 0; k < implants; k++) {
            double since = age - BLOWOUT_GAP * k;
            int frame = (int) (since / (BLOWOUT_TICKS * SimStep.SECONDS));
            double sink = sunk(age, headSinkStart(deathSeconds), HEAD_SINK);
            if (!implant.isEmpty() && (since >= 0 || implantBlown[k]) && sink < 1) {
                batch.setColor(1, 1, 1, (float) (1 - sink));
                drawScaled(
                        batch,
                        implant.get(Math.min(implant.size - 1, IMPLANT_BLOWN)),
                        deadImplantX[k],
                        deadImplantY[k],
                        1);
            } else if (!implant.isEmpty() && since < 0) {
                drawScaled(batch, implant.get(IMPLANT_SHUT), deadImplantX[k], deadImplantY[k], 1);
            }
            batch.setColor(Color.WHITE);
            if (since >= 0 && frame < blowout.size) {
                drawScaled(batch, blowout.get(frame), deadImplantX[k], deadImplantY[k], 1);
            }
        }
        double share = sunk(age, headSinkStart(deathSeconds), HEAD_SINK);
        float shade = (float) (1 - (1 - SINK_SHADE) * share);
        float scale = (float) (1 - (1 - SINK_SCALE) * share);
        batch.setColor(shade, shade, shade, (float) (1 - share));
        for (int s = 0; s < sacs.length; s++) {
            if (!sac.isEmpty()) {
                drawScaled(batch, sac.get(sacTorn[s] ? SAC_TORN : SAC_IDLE), deadSacX[s], deadSacY[s], scale);
            }
        }
        if (!head.isEmpty()) {
            drawScaled(batch, head.get(Math.min(head.size - 1, DEAD)), deadHeadX, deadHeadY, scale);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * After the grade, additive and ungraded: the coil's lines (lime in a sweep's bight and on a shed's plate, with its
     * flare), the sacs', implants' and head's glows, the implants' arc, the jaw rim's orbs in a fan wall's tell; after the
     * kill the blow-outs' flash and the vent's flare.
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = maw(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (deathTick != NEVER && piece.destroyed()) {
            drawDeathGlows(batch, tick, alpha);
        } else if (piece.present()) {
            drawLiveGlows(batch, piece, piece.coil().orElseThrow(), tick, alpha);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawLiveGlows(SpriteBatch batch, SetPiece piece, CoilArena coil, long tick, float alpha) {
        double depth = coil.depth();
        float scale = subScale(depth);
        float glow = glowAlpha(depth);
        boolean sweeping = coil.sweepState() == CoilArena.SweepState.TELL
                || coil.sweepState() == CoilArena.SweepState.SWING
                || coil.sweepState() == CoilArena.SweepState.HOLD;
        int shedAt = shedSegment(tick);
        int n = coil.segmentCount();
        for (int i = n - 1; i >= 0; i--) {
            boolean tell = (sweeping && coil.inBight(i)) || i == shedAt;
            Array<AtlasRegion> frames = tell && !segmentTell.isEmpty() ? segmentTell : segmentGlow;
            if (frames.isEmpty()) {
                continue;
            }
            int frame = segmentFrame(heading(coil.segmentAngle(i)), taper(i, n));
            batch.setColor(1, 1, 1, glow);
            drawScaled(batch, frames.get(Math.min(frames.size - 1, frame)), coil.segmentX(i), coil.segmentY(i), scale);
        }
        if (shedAt >= 0 && !shed.isEmpty()) {
            batch.setColor(Color.WHITE);
            drawScaled(
                    batch,
                    shed.get(Math.min(shed.size - 1, shedFrame(tick))),
                    coil.segmentX(shedAt),
                    coil.segmentY(shedAt),
                    1);
        }
        double x = piece.renderX(alpha);
        double y = piece.renderY(alpha);
        batch.setColor(1, 1, 1, glow);
        boolean lunging = coil.diveState() == CoilArena.DiveState.LUNGE || coil.diveState() == CoilArena.DiveState.BODY;
        for (int sacPart : sacs) {
            int frame = sacFrame(piece.partWrecked(sacPart), sacPart == swelling, swellShare(sacPart, tick));
            if (!lunging && frame != SAC_TORN && frame < sacGlow.size) {
                drawScaled(
                        batch,
                        sacGlow.get(frame),
                        x + piece.partOffsetX(sacPart),
                        y + piece.partOffsetY(sacPart),
                        scale);
            }
        }
        for (int i = 0; i < coil.implantCount(); i++) {
            CoilArena.ImplantState state = coil.implantState(i);
            int frame = implantFrame(state, piece.partWrecked(coil.implantPart(i)), tick);
            if (frame >= 1 && frame <= IMPLANT_OPEN && frame - 1 < implantGlow.size) {
                drawScaled(batch, implantGlow.get(frame - 1), coil.implantX(i), coil.implantY(i), scale);
            }
            if ((coil.implantArcing(i) || state == CoilArena.ImplantState.TELL) && !arc.isEmpty()) {
                int f = (int) Math.floorMod(tick / ARC_TICKS, (long) arc.size);
                drawScaled(batch, arc.get(f), coil.implantX(i), coil.implantY(i), scale);
            }
        }
        int pose = headFrame(coil.diveState(), coil.throatGlowing(), coil.jawOpen(), piece.phase() >= implantPhase);
        float headScale = pose == LUNGE ? 1 : scale;
        if (pose < headGlow.size) {
            batch.setColor(1, 1, 1, pose == LUNGE ? 1 : glow);
            drawScaled(batch, headGlow.get(pose), x, y, headScale);
        }
        if (coil.wallGlowing() && !headRim.isEmpty()) {
            float pulse = (float) (0.7 + 0.3 * Math.sin(tick * SimStep.SECONDS * 2 * Math.PI * 4));
            batch.setColor(1, 1, 1, pulse);
            drawScaled(batch, headRim.first(), x, y, headScale);
        }
    }

    private void drawDeathGlows(SpriteBatch batch, long tick, float alpha) {
        double age = deathAge(tick, alpha);
        if (age < 0) {
            return;
        }
        for (int k = 0; k < deadImplantX.length; k++) {
            double since = age - BLOWOUT_GAP * k;
            int frame = (int) (since / (BLOWOUT_TICKS * SimStep.SECONDS));
            if (since >= 0 && frame < blowoutGlow.size) {
                drawScaled(batch, blowoutGlow.get(frame), deadImplantX[k], deadImplantY[k], 1);
            }
        }
        double sinking = age - headSinkStart(deathSeconds);
        if (sinking < 0) {
            double share = sunk(age, 0, headSinkStart(deathSeconds));
            if (!headGlow.isEmpty()) {
                // The dead head's glows guttering until it sinks.
                batch.setColor(1, 1, 1, (float) (1 - 0.7 * share));
                drawScaled(batch, headGlow.get(Math.min(headGlow.size - 1, DEAD)), deadHeadX, deadHeadY, 1);
                batch.setColor(Color.WHITE);
            }
            return;
        }
        int frame = (int) (sinking / (FLARE_TICKS * SimStep.SECONDS));
        if (frame < flare.size) {
            drawScaled(batch, flare.get(frame), VENT_X, VENT_Y, FLARE_SCALE);
        }
    }

    /** How far the vent's light gutters now (0–{@value #VENT_DIM}): with the dive's depth. */
    float ventDim(Sortie sortie) {
        SetPiece piece = maw(sortie);
        if (piece == null || !piece.present()) {
            return 0;
        }
        CoilArena coil = piece.coil().orElseThrow();
        return coil.diving() ? (float) (VENT_DIM * coil.depth()) : 0;
    }
}
