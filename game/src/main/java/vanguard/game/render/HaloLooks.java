package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import vanguard.sim.BossLines;
import vanguard.sim.BossSpec;
import vanguard.sim.RingArena;
import vanguard.sim.RingCore;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part G, step G4: the Halo Platform's looks (design/enemies/ground/halo-platform; Level 41's held centrepiece;
 * tools/art/halo_platform.py's sprites and assets/pivots/halo-platform.json), following the simulation's {@link
 * RingArena} as {@link SovereignLooks} does the Iron Sovereign's. Over the ground units ({@link #draw}), back to front:
 * the platform's long shadow (its segments, core and turrets tinted black at the pivots' {@code shadow} offset), the
 * ring's six segments placed at their exact angle each frame from the top-lit 0–45° set by the segment rule (QG-1,
 * pending round 79), the static shade and rim overlays, the live 120° arc's red band, the mounts' running lights (two
 * each: gold out of the arc, red inside it; dark while it sleeps and on a dead mount), the gun and rail turrets at 64
 * headings (a dead mount's burnt stump turning with the ring), the core (shut, the eye waking through the barrier's
 * 0.5 s fade, open, the white-hot flare of the open core's tell), a hit part flashing at most once each 0.25 s. After
 * the grade, additive ({@link #drawGlows}): the red hex barrier over the shut core (a shot spent on it sparks there;
 * fading over the opening), the open eye's glow and its flare, the gun turrets' muzzles glowing over their 0.3 s tell,
 * the rail turrets' coils charging over a line's telegraph, the IFF spoofer's amber stutter at a held volley's muzzle
 * ({@link SetPiece#spoofHolding}). With the bullets ({@link #drawLines}): the rail line in the Rail Bunker's look. Its
 * death: the eye's blowout, the standing turrets cooking off where they stood ({@code RING_COOKOFF}) with their shards,
 * then the shared vacuum blast and its debris on the centre ({@link #blastTicks} after the last cook-off); the ring
 * stops and the burnt mount stays (the segments tinted, the scorch, the core's and every mount's wreck), scrolling away
 * with the ground. A held piece (no bar, no warning): presentation only; nothing is allocated per frame.
 */
final class HaloLooks {
    static final String SLUG = "halo-platform";
    /** The turrets' headings ({@code -gun_0..63}, {@code -rail_0..63}: k × 5.625° clockwise from straight down). */
    static final int HEADINGS = 64;
    /** The core's frames: 0 shut, 1..4 waking through the barrier's fade, 5 open, 6..9 the flare. */
    static final int CORE_SHUT = 0;

    static final int CORE_OPEN = 5;
    static final int CORE_FLARE = 6;
    static final int FLARE_FRAMES = 4;
    /** The barrier's shimmer, steps a frame (the pivots' {@code steps_per_frame}); its hit spark's. */
    static final int BARRIER_TICKS = 20;

    static final int BARRIER_HIT_TICKS = 2;
    /** A shot glancing within this reach of the core's centre is spent on the barrier, px (its 46 px radius + a hit). */
    static final double BARRIER_REACH = 52;
    /** The eye's glow, steps a frame. */
    static final int EYE_TICKS = 8;
    /** The gun's muzzle tell (0.3 s, 4 frames rising) and the eye's flare (0.5 s), s. */
    static final double GUN_TELL_SECONDS = 0.3;

    static final double FLARE_SECONDS = 0.5;
    /** The death's frames: the blowout at 20 fps, the cook-offs at 30 fps, the vacuum blast 4 steps a frame. */
    static final int BLOWOUT_TICKS = SimStep.PER_SECOND / 20;

    static final int COOKOFF_TICKS = SimStep.PER_SECOND / 30;
    static final int BLAST_TICKS = 4;
    /** The rail's frames as the Rail Bunker's (3 steps each), its muzzle flash 2 steps a frame. */
    static final int FLASH_FRAME_TICKS = 2;
    /** The spoofer's amber at a held muzzle, its size, px. */
    static final double STUTTER_SIZE = 16;

    static final Color AMBER = Color.valueOf("FFB040");

    private static final long NEVER = Long.MIN_VALUE / 2;
    private static final double[] ORIGIN = {0, 0};
    private static final String BUNKER = "rail-bunker";
    private static final int COOKOFFS = 6;

    private final FlashShader flash;
    private final SoftGlow glow;
    private final BossSpec spec;
    private final int corePart;
    private final double cookoffSeconds;
    /** The ring's art: its segments' frames, sizes and offsets. */
    private final int segments;

    private final int n8;
    private final double mountRadius;
    private final Array<AtlasRegion> segment;
    private final int[] w;
    private final int[] h;
    private final int[] ox;
    private final int[] oy;
    private final double lampRadius;
    private final double[] lampDegrees;
    private final AtlasRegion arc;
    private final double arcFrom;
    private final double arcTo;
    /** Per mount (segment j): whether it carries a rail turret (else a gun). */
    private final boolean[] railMount;

    private final AtlasRegion rim;
    private final AtlasRegion shade;
    private final AtlasRegion scorch;
    private final AtlasRegion lampGold;
    private final AtlasRegion lampRed;
    private final Array<AtlasRegion> gun;
    private final Array<AtlasRegion> rail;
    private final AtlasRegion gunWreck;
    private final AtlasRegion railWreck;
    private final Array<AtlasRegion> gunMuzzle;
    private final Array<AtlasRegion> railCharge;
    private final double[][][] gunMuzzles;
    private final double[][] railMuzzles;
    private final double[][] coils;
    private final Array<AtlasRegion> core;
    private final AtlasRegion coreWreck;
    private final Array<AtlasRegion> barrier;
    private final Array<AtlasRegion> barrierHit;
    private final Array<AtlasRegion> eyeGlow;
    private final Array<AtlasRegion> eyeFlare;
    private final Array<AtlasRegion> cookoff;
    private final Array<AtlasRegion> cookoffDebris;
    private final Array<AtlasRegion> blowout;
    private final Array<AtlasRegion> blast;
    private final Array<AtlasRegion> blastDebris;
    private final Array<AtlasRegion> railStrip;
    private final Array<AtlasRegion> railFlash;
    private final float shadowDx;
    private final float shadowDy;
    private final float shadowAlpha;
    private final float wreckTint;

    private final HitFlash hits = new HitFlash();
    private final float[] quad = new float[20];

    /** Per part the step its tell started (a gun's muzzles, the core's flare), and its spoofed volley's. */
    private long[] tellTick = new long[0];

    private long[] spoofTick = new long[0];
    /** The barrier's last spark: its step and place. */
    private long barrierHitTick = NEVER;

    private double barrierHitX;
    private double barrierHitY;
    /** The last rail line fired: its step, part, origin and direction (kept for the rail's fade). */
    private long lineFiredTick = NEVER;

    private int linePart = -1;
    private double lineX;
    private double lineY;
    private double lineDirX;
    private double lineDirY;
    /** The kill: its step (NEVER while it lives), the centre, the scroll and the ring's angle then. */
    private long deathTick = NEVER;

    private double deadX;
    private double deadY;
    private double deadScroll;
    private double deadAngle;
    /** The cook-offs: their steps and places on the ground (y + scroll then), and the last one's step. */
    private final long[] cookTick = new long[COOKOFFS];

    private final double[] cookX = new double[COOKOFFS];
    private final double[] cookGround = new double[COOKOFFS];
    private int cookNext;
    private long lastCookTick = NEVER;

    private HaloLooks(Sprites sprites, FlashShader flash, SoftGlow glow, SetPieceSpec piece, JsonValue pivots) {
        this.flash = flash;
        this.glow = glow;
        spec = piece.boss().orElseThrow();
        RingSpec rings = spec.rings().orElseThrow();
        corePart = rings.core().map(RingSpec.Core::part).orElse(0);
        cookoffSeconds = rings.cookoffSeconds();
        JsonValue ring = pivots.get("rings").child;
        segments = ring.getInt("segments");
        n8 = ring.getInt("n8");
        mountRadius = ring.getDouble("mount_radius");
        segment = optional(sprites, ring.getString("sprite"));
        JsonValue list = ring.get("frames");
        w = new int[list.size];
        h = new int[list.size];
        ox = new int[list.size];
        oy = new int[list.size];
        int k = 0;
        for (JsonValue frame = list.child; frame != null; frame = frame.next, k++) {
            w[k] = frame.get("size").getInt(0);
            h[k] = frame.get("size").getInt(1);
            ox[k] = frame.get("offset").getInt(0);
            oy[k] = frame.get("offset").getInt(1);
        }
        JsonValue lamps = ring.get("lamps");
        lampRadius = lamps.getDouble("radius");
        lampDegrees = lamps.get("angles_deg").asDoubleArray();
        JsonValue arcs = ring.get("arc");
        arc = single(sprites, arcs.getString("sprite"));
        arcFrom = arcs.getDouble("from_radius");
        arcTo = arcs.getDouble("to_radius");
        String[] kinds = ring.get("battery_kinds").asStringArray();
        railMount = new boolean[kinds.length];
        for (int j = 0; j < kinds.length; j++) {
            railMount[j] = kinds[j].equals("rail");
        }
        rim = single(sprites, SLUG + "-rim");
        shade = single(sprites, SLUG + "-shade");
        scorch = single(sprites, SLUG + "-scorch");
        lampGold = single(sprites, SLUG + "-lamp-gold");
        lampRed = single(sprites, SLUG + "-lamp-red");
        gun = optional(sprites, SLUG + "-gun");
        rail = optional(sprites, SLUG + "-rail");
        gunWreck = single(sprites, SLUG + "-gun-wreck");
        railWreck = single(sprites, SLUG + "-rail-wreck");
        gunMuzzle = optional(sprites, SLUG + "-gun-muzzle");
        railCharge = optional(sprites, SLUG + "-rail-charge");
        JsonValue turrets = pivots.get("turrets");
        gunMuzzles = muzzles(turrets.get("gun").get("muzzle"));
        double[][][] railPoints = muzzles(turrets.get("rail").get("muzzle"));
        railMuzzles = new double[railPoints.length][];
        for (int i = 0; i < railPoints.length; i++) {
            railMuzzles[i] = railPoints[i].length > 0 ? railPoints[i][0] : ORIGIN;
        }
        coils = points(turrets.get("rail").get("coils"));
        core = optional(sprites, SLUG + "-core");
        coreWreck = single(sprites, SLUG + "-core-wreck");
        barrier = optional(sprites, SLUG + "-barrier");
        barrierHit = optional(sprites, SLUG + "-barrier-hit");
        eyeGlow = optional(sprites, SLUG + "-eye-glow");
        eyeFlare = optional(sprites, SLUG + "-eye-flare");
        cookoff = optional(sprites, SLUG + "-cookoff");
        cookoffDebris = optional(sprites, SLUG + "-cookoff-debris");
        blowout = optional(sprites, SLUG + "-blowout");
        blast = optional(sprites, "explosion-vacuum-large");
        blastDebris = optional(sprites, "explosion-vacuum-debris-large");
        railStrip = optional(sprites, BUNKER + "-rail");
        railFlash = optional(sprites, BUNKER + "-flash");
        JsonValue shadow = pivots.get("shadow");
        float[] offset = shadow.get("offset").asFloatArray();
        // The pivots' offset is y down; the play field's y is up.
        shadowDx = offset[0];
        shadowDy = -offset[1];
        shadowAlpha = shadow.getFloat("alpha");
        wreckTint = pivots.getFloat("wreck_tint", 0.5f);
        reset();
    }

    /**
     * The looks of the level's held ringed piece (the Halo Platform: a {@link BossSpec#held() held} boss with {@code
     * rings}) with its pivots and its segment frames; else null.
     */
    static HaloLooks of(Sprites sprites, FlashShader flash, SoftGlow glow, SetPieceSpec piece, JsonValue pivots) {
        if (piece == null
                || pivots == null
                || pivots.get("rings") == null
                || piece.boss().filter(BossSpec::held).flatMap(BossSpec::rings).isEmpty()
                || !sprites.has(SLUG + "-segment")) {
            return null;
        }
        return new HaloLooks(sprites, flash, glow, piece, pivots);
    }

    /** For the tests: the looks following a sortie of {@code piece} without its sprites (they draw nothing). */
    static HaloLooks following(SetPieceSpec piece, JsonValue pivots) {
        return new HaloLooks(null, null, null, piece, pivots);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion single(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.region(name) : null;
    }

    private static double[][] points(JsonValue list) {
        if (list == null || !list.isArray()) {
            return new double[0][];
        }
        double[][] points = new double[list.size][];
        int k = 0;
        for (JsonValue point = list.child; point != null; point = point.next, k++) {
            points[k] = point.asDoubleArray();
        }
        return points;
    }

    /** Per heading a list of points (the gun's two muzzles). */
    private static double[][][] muzzles(JsonValue list) {
        if (list == null || !list.isArray()) {
            return new double[0][][];
        }
        double[][][] muzzles = new double[list.size][][];
        int k = 0;
        for (JsonValue heading = list.child; heading != null; heading = heading.next, k++) {
            muzzles[k] = points(heading);
        }
        return muzzles;
    }

    /** The level restarts (or the halt is retried from its checkpoint): no tell, no line, no death. */
    void reset() {
        Arrays.fill(tellTick, NEVER);
        Arrays.fill(spoofTick, NEVER);
        barrierHitTick = NEVER;
        lineFiredTick = NEVER;
        linePart = -1;
        deathTick = NEVER;
        Arrays.fill(cookTick, NEVER);
        cookNext = 0;
        lastCookTick = NEVER;
        hits.reset();
    }

    // --- the rules ---------------------------------------------------------------------------------

    /** The core's frame: shut, waking over the barrier's fade ({@code openShare} 0–1), open, or its flare's. */
    static int coreFrame(boolean opening, boolean open, double openShare, long sinceTell, int tellTicks) {
        if (open) {
            if (sinceTell >= 0 && sinceTell < tellTicks) {
                return CORE_FLARE + (int) Math.min(FLARE_FRAMES - 1, sinceTell * FLARE_FRAMES / Math.max(1, tellTicks));
            }
            return CORE_OPEN;
        }
        if (!opening) {
            return CORE_SHUT;
        }
        return 1 + (int) Math.min(CORE_OPEN - 2, Math.floor(Math.clamp(openShare, 0, 1) * (CORE_OPEN - 1)));
    }

    /** The barrier's opacity: whole while the core is shut, fading out over the opening, none once open. */
    static float barrierOpacity(boolean opening, boolean open, double openShare) {
        if (open) {
            return 0;
        }
        return opening ? (float) Math.clamp(1 - openShare, 0, 1) : 1;
    }

    /** A tell's frame {@code since} steps into it, of {@code frames} rising over {@code ticks}; -1 outside it. */
    static int tellFrame(long since, int ticks, int frames) {
        if (since < 0 || since >= ticks || frames <= 0) {
            return -1;
        }
        return (int) Math.min(frames - 1, since * frames / Math.max(1, ticks));
    }

    /** A mount's lamps: none while it sleeps or once dead, red inside the arc, else gold. */
    static boolean lampsLit(boolean lit, boolean dead) {
        return lit && !dead;
    }

    /** The vacuum blast's step after the kill: {@code cookoffs} cook-offs {@code gap} s apart, the blast one gap after the last. */
    static int blastTicks(int cookoffs, double gap) {
        return SimStep.ticks(Math.max(1, cookoffs) * gap);
    }

    /**
     * The turrets that cook off at the kill: those standing, else (none standing) every one, as the simulation's {@link
     * RingArena}: the vacuum blast's step from the kill follows ({@link #blastTicks}).
     */
    static int deathBlastTicks(SetPiece piece) {
        RingArena ring = piece.ring().orElseThrow();
        int standing = 0;
        int batteries = 0;
        for (int p = 0; p < piece.partCount(); p++) {
            if (ring.battery(p) >= 0) {
                batteries++;
                standing += piece.partHp(p) > 0 ? 1 : 0;
            }
        }
        return blastTicks(standing > 0 ? standing : batteries, ring.spec().cookoffSeconds());
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The held ringed piece among the set pieces (the Halo), or null. */
    static SetPiece halo(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.held() && piece.ring().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** After each simulation step: the tells, the spoofed volleys, the barrier's sparks, the lines, the kill, the cook-offs. */
    void stepped(Sortie sortie) {
        SetPiece piece = halo(sortie);
        if (piece == null) {
            return;
        }
        if (tellTick.length != piece.partCount()) {
            tellTick = new long[piece.partCount()];
            spoofTick = new long[piece.partCount()];
            Arrays.fill(tellTick, NEVER);
            Arrays.fill(spoofTick, NEVER);
        }
        long tick = sortie.tick();
        if (piece.destroyed() && deathTick == NEVER) {
            deathTick = tick;
            deadX = Math.round(piece.renderX(1));
            deadY = Math.round(piece.renderY(1));
            deadScroll = sortie.groundScroll();
            deadAngle = piece.ring().orElseThrow().renderAngle(0, 1);
        } else if (!piece.destroyed() && deathTick != NEVER) {
            // A retry from the halt's checkpoint brings it back.
            reset();
        }
        RingCore heart = piece.ring().flatMap(RingArena::core).orElse(null);
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case TURRET_TELL -> mark(tellTick, events.value(i), tick);
                case BOSS_SPOOFED -> mark(spoofTick, events.value(i), tick);
                case SHOT_GLANCED -> {
                    boolean barrierUp = heart != null && !heart.open();
                    if (barrierUp
                            && piece.present()
                            && !piece.destroyed()
                            && Math.hypot(events.x(i) - piece.partX(corePart), events.y(i) - piece.partY(corePart))
                                    <= BARRIER_REACH) {
                        barrierHitTick = tick;
                        barrierHitX = events.x(i);
                        barrierHitY = events.y(i);
                    }
                }
                case BOSS_LINE_FIRED -> {
                    BossLines lines = piece.lines().orElse(null);
                    if (lines != null) {
                        lineFiredTick = tick;
                        linePart = lines.part();
                        lineX = lines.fromX();
                        lineY = lines.fromY();
                        lineDirX = lines.dirX();
                        lineDirY = lines.dirY();
                    }
                }
                case RING_COOKOFF -> {
                    if (deathTick != NEVER) {
                        cookTick[cookNext] = tick;
                        cookX[cookNext] = events.x(i);
                        cookGround[cookNext] = events.y(i) + sortie.groundScroll();
                        cookNext = (cookNext + 1) % COOKOFFS;
                        lastCookTick = tick;
                    }
                }
                default -> {}
            }
        }
    }

    private static void mark(long[] ticks, int part, long tick) {
        if (part >= 0 && part < ticks.length) {
            ticks[part] = tick;
        }
    }

    /** How long ago it was killed, steps; -1 while it lives. */
    long deathAge(long tick) {
        return deathTick == NEVER ? -1 : tick - deathTick;
    }

    /** The step of the vacuum blast: one cook-off gap after the last cook-off (NEVER before the first). */
    long blastTick() {
        return lastCookTick == NEVER ? NEVER : lastCookTick + SimStep.ticks(cookoffSeconds);
    }

    // --- drawing -----------------------------------------------------------------------------------

    private static void additive(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    }

    private static void normal(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private boolean drawn() {
        return segment.size == n8 + 1 && w.length == n8 + 1;
    }

    /** Over the ground units: the shadow, the ring, its overlays, arc band and lamps, the turrets and the core; dead, the wreck. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash, double scroll) {
        SetPiece piece = halo(sortie);
        if (piece == null || !drawn()) {
            return;
        }
        long tick = sortie.tick();
        if (deathTick != NEVER && piece.destroyed()) {
            drawWreck(batch, piece, deadX, deadY - (scroll - deadScroll), tick, scroll);
            return;
        }
        if (!piece.present() && !piece.approaching()) {
            return;
        }
        RingArena ring = piece.ring().orElseThrow();
        RingCore heart = ring.core().orElse(null);
        double cx = Math.round(piece.renderX(alpha));
        double cy = Math.round(piece.renderY(alpha));
        double angle = Math.toDegrees(ring.renderAngle(0, alpha) + startRadians());
        // The long shadow first: the segments, the core and the turrets tinted black at the Sun's offset.
        batch.setColor(0, 0, 0, shadowAlpha);
        for (int j = 0; j < segments; j++) {
            drawSegment(batch, angle + 360.0 * j / segments, cx, cy, shadowDx, shadowDy);
        }
        if (!core.isEmpty()) {
            drawCentred(batch, core.first(), cx + shadowDx, cy + shadowDy);
        }
        drawTurrets(batch, piece, ring, cx + shadowDx, cy + shadowDy, alpha, tick, 0, true);
        batch.setColor(Color.WHITE);
        for (int j = 0; j < segments; j++) {
            drawSegment(batch, angle + 360.0 * j / segments, cx, cy, 0, 0);
        }
        if (shade != null) {
            drawCentred(batch, shade, cx, cy);
        }
        additive(batch);
        if (rim != null) {
            drawCentred(batch, rim, cx, cy);
        }
        drawArc(batch, piece, ring, cx, cy, tick);
        drawLamps(batch, piece, ring, cx, cy, alpha);
        normal(batch);
        batch.setColor(Color.WHITE);
        drawTurrets(batch, piece, ring, cx, cy, alpha, tick, whiteFlash, false);
        drawCore(batch, piece, heart, cx, cy, tick, whiteFlash);
        batch.setColor(Color.WHITE);
    }

    private double startRadians() {
        return spec.rings().orElseThrow().rings().getFirst().startRadians();
    }

    /** The live arc's faint red band on the ring's hull round the arc's sector (additive, the batch is). */
    private void drawArc(SpriteBatch batch, SetPiece piece, RingArena ring, double cx, double cy, long tick) {
        if (arc == null || ring.spinningUp() || piece.destroyed() || !piece.engaged()) {
            return;
        }
        double half = ring.arcRadians() / 2;
        double bearing = ring.bearing();
        float pulse = 0.8f + 0.2f * (float) Math.sin(tick * SimStep.SECONDS * 2 * Math.PI * 0.8);
        batch.setColor(1, 1, 1, pulse);
        float color = batch.getPackedColor();
        double step = arc.getRegionHeight() / ((arcFrom + arcTo) / 2);
        int count = Math.max(1, (int) Math.ceil(2 * half / step));
        double da = 2 * half / count;
        for (int s = 0; s < count; s++) {
            double a0 = bearing - half + s * da;
            double a1 = a0 + da;
            arcCorner(0, cx, cy, arcFrom, a0, color, arc.getU(), arc.getV());
            arcCorner(5, cx, cy, arcTo, a0, color, arc.getU2(), arc.getV());
            arcCorner(10, cx, cy, arcTo, a1, color, arc.getU2(), arc.getV2());
            arcCorner(15, cx, cy, arcFrom, a1, color, arc.getU(), arc.getV2());
            batch.draw(arc.getTexture(), quad, 0, quad.length);
        }
        batch.setColor(Color.WHITE);
    }

    private void arcCorner(int at, double cx, double cy, double radius, double heading, float color, float u, float v) {
        quad[at] = (float) (X0 + cx - radius * Math.sin(heading));
        quad[at + 1] = (float) (cy - radius * Math.cos(heading));
        quad[at + 2] = color;
        quad[at + 3] = u;
        quad[at + 4] = v;
    }

    /** Each living mount's two running lights on the inner rail: red inside the arc, gold out of it; none asleep. */
    private void drawLamps(SpriteBatch batch, SetPiece piece, RingArena ring, double cx, double cy, float alpha) {
        if (lampGold == null || lampRed == null) {
            return;
        }
        for (int p = 0; p < piece.partCount(); p++) {
            if (ring.battery(p) < 0 || !lampsLit(ring.lit(0), piece.partWrecked(p))) {
                continue;
            }
            double heading = mountHeading(ring, p, alpha);
            AtlasRegion lamp = ring.inArc(p) && !ring.spinningUp() ? lampRed : lampGold;
            for (double degrees : lampDegrees) {
                double at = heading + Math.toRadians(degrees);
                drawCentred(batch, lamp, cx - lampRadius * Math.sin(at), cy - lampRadius * Math.cos(at));
            }
        }
    }

    private double mountHeading(RingArena ring, int p, float alpha) {
        RingSpec.Ring spec = this.spec.rings().orElseThrow().rings().get(ring.battery(p));
        return spec.mountRadians(ring.slot(p)) + ring.renderAngle(ring.battery(p), alpha);
    }

    /** Whether part {@code p}'s mount carries a rail turret (its slot's kind in the pivots). */
    private boolean railOf(RingArena ring, int p) {
        int slot = ring.slot(p);
        return slot >= 0 && slot < railMount.length && railMount[slot];
    }

    /** The turrets on their mounts (a dead one's stump); {@code shadow}: once more tinted at the shadow's offset. */
    private void drawTurrets(
            SpriteBatch batch,
            SetPiece piece,
            RingArena ring,
            double cx,
            double cy,
            float alpha,
            long tick,
            float whiteFlash,
            boolean shadow) {
        for (int p = 0; p < piece.partCount(); p++) {
            if (ring.battery(p) < 0) {
                continue;
            }
            boolean railTurret = railOf(ring, p);
            double heading = mountHeading(ring, p, alpha);
            double x = cx - mountRadius * Math.sin(heading);
            double y = cy - mountRadius * Math.cos(heading);
            if (piece.partWrecked(p)) {
                AtlasRegion stump = railTurret ? railWreck : gunWreck;
                if (stump != null) {
                    drawCentred(batch, stump, x, y);
                }
                continue;
            }
            Array<AtlasRegion> frames = railTurret ? rail : gun;
            if (frames.isEmpty()) {
                continue;
            }
            AtlasRegion region = frames.get(SovereignLooks.turretFrame(aim(piece, ring, p)) % frames.size);
            if (!shadow && flash != null && hits.on(p, tick, piece.partTicksSinceHit(p))) {
                flash.draw(
                        batch, region, Math.round(X0 + x), Math.round(y), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
            } else {
                drawCentred(batch, region, x, y);
            }
        }
    }

    /** A turret's heading: along its rail line while that telegraphs or fires, else toward the ship. */
    private static double aim(SetPiece piece, RingArena ring, int p) {
        BossLines lines = piece.lines().orElse(null);
        if (lines != null && lines.running() && lines.part() == p) {
            return Math.atan2(-lines.dirX(), -lines.dirY());
        }
        return ring.turretRadians(p);
    }

    private void drawCore(
            SpriteBatch batch, SetPiece piece, RingCore heart, double cx, double cy, long tick, float whiteFlash) {
        if (core.isEmpty()) {
            return;
        }
        int frame = heart == null
                ? CORE_SHUT
                : coreFrame(
                        heart.opening(),
                        heart.open(),
                        heart.openShare(),
                        corePart < tellTick.length ? tick - tellTick[corePart] : -1,
                        SimStep.ticks(FLARE_SECONDS));
        AtlasRegion region = core.get(Math.min(core.size - 1, frame));
        if (flash != null
                && hits.on(corePart, tick, piece.partTicksSinceHit(corePart))
                && heart != null
                && heart.open()) {
            flash.draw(batch, region, Math.round(X0 + cx), Math.round(cy), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
        } else {
            drawCentred(batch, region, cx, cy);
        }
    }

    /**
     * Segment at {@code degrees} by the segment rule round the centre (cx, cy) (play field, y up), moved by (tx, ty);
     * the batch's colour.
     */
    private void drawSegment(SpriteBatch batch, double degrees, double cx, double cy, double tx, double ty) {
        int k = SovereignLooks.segmentFrame(degrees, n8);
        AtlasRegion region = segment.get(k);
        float color = batch.getPackedColor();
        int x0 = ox[k];
        int y0 = oy[k];
        int fw = w[k];
        int fh = h[k];
        boolean mirrored = SovereignLooks.segmentMirrored(degrees);
        int turns = SovereignLooks.segmentTurns(degrees);
        double px = X0 + cx + tx;
        double py = cy + ty;
        corner(0, x0, y0 + fh, mirrored, turns, px, py, color, region.getU(), region.getV2());
        corner(5, x0, y0, mirrored, turns, px, py, color, region.getU(), region.getV());
        corner(10, x0 + fw, y0, mirrored, turns, px, py, color, region.getU2(), region.getV());
        corner(15, x0 + fw, y0 + fh, mirrored, turns, px, py, color, region.getU2(), region.getV2());
        batch.draw(region.getTexture(), quad, 0, quad.length);
    }

    private void corner(
            int at,
            double x,
            double y,
            boolean mirrored,
            int turns,
            double px,
            double py,
            float color,
            float u,
            float v) {
        quad[at] = (float) (px + SovereignLooks.turnX(x, y, mirrored, turns));
        quad[at + 1] = (float) (py - SovereignLooks.turnY(x, y, mirrored, turns));
        quad[at + 2] = color;
        quad[at + 3] = u;
        quad[at + 4] = v;
    }

    /** The burnt mount where it died: the segments tinted at the death's angle, the scorch, the stumps and the core's wreck. */
    private void drawWreck(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, double scroll) {
        double angle = Math.toDegrees(deadAngle + startRadians());
        batch.setColor(wreckTint, wreckTint, wreckTint, 1);
        for (int j = 0; j < segments; j++) {
            drawSegment(batch, angle + 360.0 * j / segments, cx, cy, 0, 0);
        }
        batch.setColor(Color.WHITE);
        if (shade != null) {
            drawCentred(batch, shade, cx, cy);
        }
        RingArena ring = piece.ring().orElseThrow();
        RingSpec.Ring spec = this.spec.rings().orElseThrow().rings().getFirst();
        for (int p = 0; p < piece.partCount(); p++) {
            if (ring.battery(p) < 0) {
                continue;
            }
            double heading = spec.mountRadians(ring.slot(p)) + deadAngle;
            AtlasRegion stump = railOf(ring, p) ? railWreck : gunWreck;
            if (stump != null) {
                drawCentred(batch, stump, cx - mountRadius * Math.sin(heading), cy - mountRadius * Math.cos(heading));
            }
        }
        boolean blown = blastTick() != NEVER && tick >= blastTick();
        AtlasRegion centre = blown || core.isEmpty() ? coreWreck : core.get(Math.min(core.size - 1, CORE_OPEN));
        if (centre != null) {
            drawCentred(batch, centre, cx, cy);
        }
        if (scorch != null && blown) {
            drawCentred(batch, scorch, cx, cy);
        }
        for (int c = 0; c < COOKOFFS; c++) {
            if (cookTick[c] == NEVER) {
                continue;
            }
            int f = (int) ((tick - cookTick[c]) / COOKOFF_TICKS);
            if (f < cookoffDebris.size) {
                drawCentred(batch, cookoffDebris.get(f), cookX[c], cookGround[c] - scroll);
            }
        }
        if (blown && !blastDebris.isEmpty()) {
            int b = (int) ((tick - blastTick()) / BLAST_TICKS);
            if (b < blastDebris.size) {
                drawCentred(batch, blastDebris.get(b), cx, cy);
            }
        }
    }

    /**
     * After the grade, additive: the barrier and its sparks, the eye's glow and flare, the guns' muzzles over their
     * tell, the rails' coils over a telegraph, the spoofer's amber stutter; dead: the blowout, the cook-offs, the blast.
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        SetPiece piece = halo(sortie);
        if (piece == null || !drawn()) {
            return;
        }
        long tick = sortie.tick();
        additive(batch);
        if (deathTick != NEVER && piece.destroyed()) {
            drawDeathGlows(batch, tick, deadX, deadY - (scroll - deadScroll), scroll);
        } else if (piece.present() || piece.approaching()) {
            drawLiveGlows(batch, piece, piece.ring().orElseThrow(), tick, alpha);
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    private void drawLiveGlows(SpriteBatch batch, SetPiece piece, RingArena ring, long tick, float alpha) {
        double cx = Math.round(piece.renderX(alpha));
        double cy = Math.round(piece.renderY(alpha));
        RingCore heart = ring.core().orElse(null);
        boolean open = heart != null && heart.open();
        float shield = heart == null ? 0 : barrierOpacity(heart.opening(), open, heart.openShare());
        if (shield > 0 && !barrier.isEmpty()) {
            batch.setColor(1, 1, 1, shield);
            drawCentred(batch, barrier.get((int) (tick / BARRIER_TICKS % barrier.size)), cx, cy);
            batch.setColor(Color.WHITE);
            int f = (int) ((tick - barrierHitTick) / BARRIER_HIT_TICKS);
            if (barrierHitTick != NEVER && f >= 0 && f < barrierHit.size) {
                drawCentred(batch, barrierHit.get(f), barrierHitX, barrierHitY);
            }
        }
        if (open && !eyeGlow.isEmpty()) {
            drawCentred(batch, eyeGlow.get((int) (tick / EYE_TICKS % eyeGlow.size)), cx, cy);
        }
        if (open && corePart < tellTick.length) {
            int f = tellFrame(tick - tellTick[corePart], SimStep.ticks(FLARE_SECONDS), eyeFlare.size);
            if (f >= 0) {
                drawCentred(batch, eyeFlare.get(f), cx, cy);
            }
        }
        BossLines lines = piece.lines().orElse(null);
        for (int p = 0; p < piece.partCount(); p++) {
            if (ring.battery(p) < 0 || piece.partWrecked(p)) {
                continue;
            }
            double heading = mountHeading(ring, p, alpha);
            double x = cx - mountRadius * Math.sin(heading);
            double y = cy - mountRadius * Math.cos(heading);
            int k = SovereignLooks.turretFrame(aim(piece, ring, p));
            boolean railTurret = railOf(ring, p);
            if (railTurret) {
                if (lines != null && lines.telegraphing() && lines.part() == p && !railCharge.isEmpty()) {
                    int frame = Math.min(railCharge.size - 1, (int) (lines.charge() * railCharge.size));
                    double[] at = k < coils.length ? coils[k] : ORIGIN;
                    drawCentred(batch, railCharge.get(frame), x + at[0], y - at[1]);
                }
            } else if (p < tellTick.length) {
                int frame = tellFrame(tick - tellTick[p], SimStep.ticks(GUN_TELL_SECONDS), gunMuzzle.size);
                if (frame >= 0 && k < gunMuzzles.length) {
                    for (double[] at : gunMuzzles[k]) {
                        drawCentred(batch, gunMuzzle.get(frame), x + at[0], y - at[1]);
                    }
                }
            }
            if (glow != null
                    && p < spoofTick.length
                    && piece.spoofHolding()
                    && JovianFlashes.stutterOn(tick - spoofTick[p])) {
                // The IFF spoofer holds its volley: the muzzle flickers amber, searching.
                double[] at = railTurret
                        ? (k < railMuzzles.length ? railMuzzles[k] : ORIGIN)
                        : (k < gunMuzzles.length && gunMuzzles[k].length > 0 ? gunMuzzles[k][0] : ORIGIN);
                batch.setColor(AMBER.r, AMBER.g, AMBER.b, 0.9f);
                glow.disc(batch, x + at[0], y - at[1], STUTTER_SIZE);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void drawDeathGlows(SpriteBatch batch, long tick, double cx, double cy, double scroll) {
        int b = (int) ((tick - deathTick) / BLOWOUT_TICKS);
        if (b >= 0 && b < blowout.size) {
            drawCentred(batch, blowout.get(b), cx, cy);
        }
        for (int c = 0; c < COOKOFFS; c++) {
            if (cookTick[c] == NEVER) {
                continue;
            }
            int f = (int) ((tick - cookTick[c]) / COOKOFF_TICKS);
            if (f >= 0 && f < cookoff.size) {
                drawCentred(batch, cookoff.get(f), cookX[c], cookGround[c] - scroll);
            }
        }
        long blown = blastTick();
        if (blown != NEVER && tick >= blown) {
            int f = (int) ((tick - blown) / BLAST_TICKS);
            if (f < blast.size) {
                drawCentred(batch, blast.get(f), cx, cy);
            }
        }
    }

    /**
     * With the bullets, additive: the rail line (the red line over its telegraph, locked, its origin riding with its
     * turret; then the Rail Bunker's rail live and fading with its muzzle flash).
     */
    void drawLines(SpriteBatch batch, Sortie sortie, float alpha, TextureRegion pixel) {
        SetPiece piece = halo(sortie);
        if (piece == null || !piece.present() || piece.destroyed()) {
            return;
        }
        long tick = sortie.tick();
        additive(batch);
        BossLines lines = piece.lines().orElse(null);
        if (lines != null && lines.telegraphing() && pixel != null) {
            Color red = LineLooks.TELEGRAPH;
            batch.setColor(red.r, red.g, red.b, LineLooks.telegraphOpacity(lines.charge(), tick));
            double in = muzzle(lines.part(), Math.atan2(-lines.dirX(), -lines.dirY()));
            double ox = lines.fromX() - lines.stepX() * (1 - alpha);
            double oy = lines.fromY() - lines.stepY() * (1 - alpha);
            float width = LineLooks.TELEGRAPH_WIDTH;
            float length = (float) (2 * (vanguard.sim.PlayField.WIDTH + vanguard.sim.PlayField.HEIGHT));
            batch.draw(
                    pixel,
                    (float) (X0 + ox + lines.dirX() * in),
                    (float) (oy + lines.dirY() * in - width / 2),
                    0,
                    width / 2,
                    length,
                    width,
                    1,
                    1,
                    (float) Math.toDegrees(Math.atan2(lines.dirY(), lines.dirX())));
            batch.setColor(Color.WHITE);
        }
        if (lineFiredTick != NEVER && linePart >= 0 && railStrip.size == 4) {
            long since = tick - lineFiredTick;
            var line = lines == null || lines.attack() < 0
                    ? null
                    : spec.attacks().get(lines.attack()).line().orElse(null);
            int liveTicks = SimStep.ticks(line == null ? 0.2 : line.liveSeconds());
            int frame = SovereignLooks.railFrame(since, liveTicks);
            double in = muzzle(linePart, Math.atan2(-lineDirX, -lineDirY));
            double ox = lineX;
            double oy = lineY;
            if (lines != null && lines.running() && lines.part() == linePart) {
                ox = lines.fromX() - lines.stepX() * (1 - alpha);
                oy = lines.fromY() - lines.stepY() * (1 - alpha);
            } else if (piece.partHp(linePart) > 0) {
                ox = piece.partX(linePart);
                oy = piece.partY(linePart);
            }
            batch.setColor(Color.WHITE);
            if (frame >= 0) {
                drawRail(batch, railStrip.get(frame), in, ox, oy, lineDirX, lineDirY);
            }
            int flashFrame = (int) (since / FLASH_FRAME_TICKS);
            if (flashFrame < railFlash.size) {
                drawCentred(batch, railFlash.get(flashFrame), ox + lineDirX * in, oy + lineDirY * in);
            }
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    /** The rail turret's muzzle distance from its mount's centre along {@code heading}. */
    private double muzzle(int p, double heading) {
        if (p == corePart || railMuzzles.length == 0) {
            return 0;
        }
        double[] at = railMuzzles[SovereignLooks.turretFrame(heading) % railMuzzles.length];
        return Math.hypot(at[0], at[1]);
    }

    private static void drawRail(
            SpriteBatch batch, AtlasRegion strip, double in, double ox, double oy, double dirX, double dirY) {
        float degrees = (float) Math.toDegrees(Math.atan2(dirY, dirX));
        float sw = strip.getRegionWidth();
        float sh = strip.getRegionHeight();
        double length = 2 * (vanguard.sim.PlayField.WIDTH + vanguard.sim.PlayField.HEIGHT);
        for (double at = in; at < length; at += sw) {
            double x = ox + dirX * at;
            double y = oy + dirY * at;
            if (x < -sw || x > vanguard.sim.PlayField.WIDTH + sw || y < -sw || y > vanguard.sim.PlayField.HEIGHT + sw) {
                break;
            }
            batch.draw(strip, (float) (X0 + x), (float) (y - sh / 2), 0, sh / 2, sw, sh, 1, 1, degrees);
        }
    }

    /** For the tests: the core's part. */
    int corePart() {
        return corePart;
    }

    /** For the tests: whether part {@code p} carries a rail turret. */
    boolean rail(Sortie sortie, int p) {
        SetPiece piece = halo(sortie);
        return piece != null && railOf(piece.ring().orElseThrow(), p);
    }
}
