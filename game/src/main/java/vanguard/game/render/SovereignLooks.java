package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import vanguard.sim.BossLines;
import vanguard.sim.BossSpec;
import vanguard.sim.PartShields;
import vanguard.sim.RingArena;
import vanguard.sim.RingCore;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part H, step H4: the ringed arena boss's looks (the Iron Sovereign, design/enemies/bosses/iron-sovereign,
 * Presentation, Tells and Death; tools/art/iron_sovereign.py's sprites and assets/pivots/iron-sovereign.json), following
 * the simulation's {@link RingArena}. Over the ground units ({@link #draw}), back to front: the static base with its
 * catwalk lights (the inner catwalk lit with the inner ring, the outer with the middle ring, all out from the core's
 * opening), each ring's identical segments placed at their exact angle each frame from the top-lit 0–45° set by the
 * pivots' segment rule (quarter turns and a mirror, exact on the pixel grid; a locked ring tinted dark), the segments'
 * red running lights (a lit ring, not locked), the static shade and rim-light overlays (drawn 2x), the fire arc's red
 * band on each live ring's hull round the arc's sector, the batteries' mounts (dark before their ring lights, gold out of
 * the arc, red armed) under their turrets at 64 headings (the SAM rack's hatch open for its salvo; a dead battery's
 * burnt collar venting grey gas), the rim bays' doors (opening over a stream's tell) and the core (shut; its petals
 * parting over the 3 s opening, the station shuddering; open), a hit part flashing at most once each 0.25 s. After
 * the grade, additive ({@link #drawGlows}): the inner ring's teeth flashing gold in its step's tell, the shut core's slits
 * over a fan's glow, the open reactor's light (brightening in the spiral's tell) under the hex shield (holding, hit,
 * shattered, refilling), the bays' gold light, the rail batteries' coils charging over a line's telegraph and the laser
 * batteries' lens lit over a sweep. With the bullets ({@link #drawLines}): the rail lines in the Rail Bunker's look (the
 * locked red line, the rail and its muzzle flash) and the laser sweeps in the Sentinel Tower's (the crimson wedge, the
 * beam and its tip). Its death: the batteries cooking off ring by ring 0.15 s apart, then the segments drifting apart
 * and turning slowly, fading, while the core folds shut and implodes into the shared vacuum blast drawn 2x; the base
 * stays and scrolls away with the ground. Presentation only; nothing is allocated per frame.
 */
final class SovereignLooks {
    /** The turrets' headings ({@code -<kind>_0..63}: k × 5.625° clockwise from straight down). */
    static final int HEADINGS = 64;
    /** The mounts' frames: dark, gold (out of the arc), red (armed). */
    static final int MOUNT_DARK = 0;

    static final int MOUNT_GOLD = 1;
    static final int MOUNT_ARMED = 2;
    /** The core's frames: 0 shut .. 11 open. */
    static final int CORE_FRAMES = 12;
    /** The bays' frames: shut, opening (two), open. */
    static final int BAY_OPEN = 3;
    /** The red lamps' and the overlays' scale: the rim and shade are drawn 2x. */
    static final float OVERLAY_SCALE = 2;
    /** A locked ring's (and the dead station's) segments: RGB × this (the pivots' {@code locked_tint}). */
    static final float LOCKED_TINT = 0.62f;
    /** The inner ring's teeth flash in its step's tell: gold, at this opacity. */
    static final Color TELL_GOLD = new Color(1f, 0.78f, 0.3f, 1f);

    static final float TELL_ALPHA = 0.45f;
    /** The station shudders over the core's opening: 1 px, this many steps a side. */
    static final int SHUDDER_TICKS = 3;
    /** The fx: the vents at 8 fps, the cook-offs at 30 fps, the core and the implosion at 15 fps. */
    static final int VENT_TICKS = SimStep.PER_SECOND / 8;

    static final int COOKOFF_TICKS = SimStep.PER_SECOND / 30;
    static final int IMPLOSION_TICKS = SimStep.PER_SECOND / 15;
    /** The vacuum blast's steps a frame (the shared {@code explosion-vacuum-large}, drawn 2x). */
    static final int BLAST_TICKS = 4;
    /**
     * The death after the cook-offs, s from their end: the petals folding shut (the core's frames backwards at 15 fps,
     * 0.8 s), the implosion from 0.75 s (its blow, frame 6, at 1.15 s: the implosion sound's whoomph and the huge rung),
     * the vacuum blast and the flash at 1.15 s; the segments drift and fade over 3.5 s.
     */
    static final double IMPLOSION_AFTER = 0.75;

    static final double BLAST_AFTER = 1.15;
    static final double DRIFT_SECONDS = 3.5;
    /** The segments drift outward from 14 px/s, growing to 22 at the drift's end, turning 5-11 °/s. */
    static final double DRIFT_FROM = 14;

    static final double DRIFT_TO = 22;
    static final double TURN_FROM = 5;
    static final double TURN_SPREAD = 6;
    /** The open core's idle glow alternates its two frames at 3 fps. */
    static final int GLOW_TICKS = SimStep.PER_SECOND / 3;
    /** The hex shield's frame steps (as {@link EnemyShieldLooks}). */
    static final int SHIELD_HOLD_TICKS = 6;

    static final int SHIELD_HIT_TICKS = 2;
    static final int SHIELD_BREAK_TICKS = 2;
    static final int SHIELD_REFILL_TICKS = 3;
    /** The rail's frames: 0-1 live, 2-3 fading (the Rail Bunker's), 3 steps each; its muzzle flash 2 steps a frame. */
    static final int RAIL_FRAME_TICKS = 3;

    static final int FLASH_FRAME_TICKS = 2;
    /** The core's rail starts this far out from its centre (its petals' rim), px. */
    static final double CORE_MUZZLE = 40;

    private static final long NEVER = Long.MIN_VALUE / 2;
    private static final double[] ORIGIN = {0, 0};
    private static final String TOWER = "sentinel-tower";
    private static final String BUNKER = "rail-bunker";

    /** One ring's art: its segment set and the frames' sizes and offsets, its lamps, its arc strip and battery kind. */
    private static final class RingArt {
        final String name;
        final int segments;
        final int n8;
        final double mountRadius;
        final Array<AtlasRegion> frames;
        final int[] w;
        final int[] h;
        final int[] ox;
        final int[] oy;
        final double lampRadius;
        final double[] lampDegrees;
        final AtlasRegion arc;
        final double arcFrom;
        final double arcTo;
        final BatteryArt battery;

        RingArt(String name, JsonValue ring, Sprites sprites, String slug, BatteryArt battery) {
            this.name = name;
            segments = ring.getInt("segments");
            n8 = ring.getInt("n8");
            mountRadius = ring.getDouble("mount_radius");
            frames = optional(sprites, slug + "-segment-" + name);
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
            String arcName = arcs.getString("sprite");
            arc = sprites != null && sprites.has(arcName) ? sprites.region(arcName) : null;
            arcFrom = arcs.getDouble("from_radius");
            arcTo = arcs.getDouble("to_radius");
            this.battery = battery;
        }

        /** Whether its segment frames are all there (n8 + 1). */
        boolean drawn() {
            return frames.size == n8 + 1 && w.length == n8 + 1;
        }
    }

    /** A battery kind's art: its mount (3 frames), wreck, turret at 64 headings and its tell's extras. */
    private static final class BatteryArt {
        final Array<AtlasRegion> mount;
        final Array<AtlasRegion> wreck;
        final Array<AtlasRegion> turret;
        final Array<AtlasRegion> open;
        final Array<AtlasRegion> charge;
        final Array<AtlasRegion> lens;
        /** Per turret heading: the muzzle's, the coils' or the lens's place, px from the battery's centre, y down. */
        final double[][] muzzle;

        final double[][] coils;
        final double[][] lensAt;

        BatteryArt(String kind, JsonValue spec, Sprites sprites, String slug) {
            mount = optional(sprites, slug + "-" + kind + "-mount");
            wreck = optional(sprites, slug + "-" + kind + "-wreck");
            turret = optional(sprites, slug + "-" + kind);
            open = optional(sprites, slug + "-" + kind + "-open");
            charge = optional(sprites, slug + "-" + kind + "-charge");
            lens = optional(sprites, slug + "-" + kind + "-lens");
            muzzle = points(spec.get("muzzle"));
            coils = points(spec.get("coils"));
            lensAt = points(spec.get("lens"));
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
    }

    private final FlashShader flash;
    /** The sprites (null in the tests): the sweeps' telegraph wedges are looked up by their arc. */
    private final Sprites sprites;

    private final TextureRegion pixel;
    private final RingArt[] art;
    /** Per simulation ring: its art (by its name; the pivots' order without one). */
    private final RingArt[] ringArt;

    private final AtlasRegion base;
    private final AtlasRegion rim;
    private final AtlasRegion shade;
    private final AtlasRegion lampRed;
    private final AtlasRegion lampWhite;
    private final AtlasRegion lampGold;
    private final double[][] innerWhite;
    private final double[][] innerGold;
    private final double[][] outerWhite;
    private final double[][] outerGold;
    private final Array<Array<AtlasRegion>> bays = new Array<>();
    private final int[] bayOffsetX;
    private final int[] bayOffsetY;
    private final Array<AtlasRegion> bayGlow;
    private final Array<AtlasRegion> core;
    private final AtlasRegion coreSlits;
    private final Array<AtlasRegion> coreGlow;
    private final Array<AtlasRegion> vent;
    private final Array<AtlasRegion> cookoff;
    private final Array<AtlasRegion> cookoffDebris;
    private final Array<AtlasRegion> implosion;
    private final Array<AtlasRegion> blast;
    private final Array<AtlasRegion> shieldHold;
    private final Array<AtlasRegion> shieldBreak;
    private final Array<AtlasRegion> shieldRefill;
    private final Array<AtlasRegion> shieldHit;
    private final Array<AtlasRegion> rail;
    private final Array<AtlasRegion> railFlash;
    private final Array<AtlasRegion> beam;
    private final Array<AtlasRegion> beamTip;
    private final HitFlash hits = new HitFlash();
    private final float[] quad = new float[20];
    /** A battery's place at the kill ({@link #deadBattery}). */
    private final double[] place = new double[2];

    /** The boss's numbers: its parts, the core's part, the batteries in their cook-off order, the death's length. */
    private final int parts;

    private final int corePart;
    private final int[] order;
    private final double cookoffSeconds;
    private final double glowSeconds;
    private final double bayTellSeconds;
    private final BossSpec spec;

    /** The moments the looks time from: the core's fan glow, each bay's tell, the shield's hit, break and refill. */
    private long glowTick = NEVER;

    private final long[] bayTell = new long[4];
    private long shieldHitTick = NEVER;
    private double shieldHitAngle;
    private long shieldBreakTick = NEVER;
    private long shieldRefillTick = NEVER;
    /** The last rail line fired: its step, part, origin and direction (kept for the rail's fade). */
    private long lineFiredTick = NEVER;

    private int linePart = -1;
    private double lineX;
    private double lineY;
    private double lineDirX;
    private double lineDirY;
    /** The kill's step (NEVER while it lives) and the station as it died: its centre, the scroll, its rings' angles. */
    private long deathTick = NEVER;

    private double deadX;
    private double deadY;
    private double deadScroll;
    private final double[] deadAngle;

    private SovereignLooks(Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots) {
        this.flash = flash;
        this.sprites = sprites;
        String slug = boss.slug();
        pixel = sprites == null ? null : sprites.pixel;
        spec = boss.boss().orElseThrow();
        RingSpec rings = spec.rings().orElseThrow();
        JsonValue ringsJson = pivots.get("rings");
        JsonValue batteries = pivots.get("batteries");
        art = new RingArt[ringsJson.size];
        int k = 0;
        for (JsonValue ring = ringsJson.child; ring != null; ring = ring.next, k++) {
            String kind = ring.getString("battery");
            art[k] = new RingArt(
                    ring.name, ring, sprites, slug, new BatteryArt(kind, batteries.get(kind), sprites, slug));
        }
        ringArt = new RingArt[rings.rings().size()];
        for (int i = 0; i < ringArt.length; i++) {
            String name = rings.rings().get(i).name();
            RingArt match = i < art.length ? art[i] : null;
            for (RingArt candidate : art) {
                if (candidate.name.equals(name)) {
                    match = candidate;
                }
            }
            ringArt[i] = match;
        }
        base = single(sprites, slug + "-base");
        rim = single(sprites, slug + "-rim");
        shade = single(sprites, slug + "-shade");
        lampRed = single(sprites, slug + "-lamp-red");
        lampWhite = single(sprites, slug + "-lamp-white");
        lampGold = single(sprites, slug + "-lamp-gold");
        JsonValue lamps = pivots.get("base").get("lamps");
        innerWhite = BatteryArt.points(lamps.get("inner").get("white"));
        innerGold = BatteryArt.points(lamps.get("inner").get("gold"));
        outerWhite = BatteryArt.points(lamps.get("outer").get("white"));
        outerGold = BatteryArt.points(lamps.get("outer").get("gold"));
        JsonValue bayList = pivots.get("bays");
        bayOffsetX = new int[bayList.size];
        bayOffsetY = new int[bayList.size];
        int b = 0;
        for (JsonValue bay = bayList.child; bay != null; bay = bay.next, b++) {
            bays.add(optional(sprites, bay.getString("sprite")));
            bayOffsetX[b] = bay.get("offset").getInt(0);
            bayOffsetY[b] = bay.get("offset").getInt(1);
        }
        bayGlow = optional(sprites, slug + "-bay-glow");
        core = optional(sprites, slug + "-core");
        coreSlits = single(sprites, slug + "-core-slits");
        coreGlow = optional(sprites, slug + "-core-glow");
        vent = optional(sprites, slug + "-vent");
        cookoff = optional(sprites, slug + "-cookoff");
        cookoffDebris = optional(sprites, slug + "-cookoff-debris");
        implosion = optional(sprites, slug + "-implosion");
        blast = optional(sprites, "explosion-vacuum-large");
        shieldHold = optional(sprites, EnemyShieldLooks.ROOT + "-medium");
        shieldBreak = optional(sprites, EnemyShieldLooks.ROOT + "-medium-break");
        shieldRefill = optional(sprites, EnemyShieldLooks.ROOT + "-medium-refill");
        shieldHit = optional(sprites, EnemyShieldLooks.ROOT + "-hit");
        rail = optional(sprites, BUNKER + "-rail");
        railFlash = optional(sprites, BUNKER + "-flash");
        beam = optional(sprites, TOWER + "-beam");
        beamTip = optional(sprites, TOWER + "-beam-tip");
        // The core and the batteries the rings append to the boss's parts.
        parts = boss.parts().size();
        corePart = rings.core().map(RingSpec.Core::part).orElse(0);
        order = rings.rings().stream()
                .flatMap(r -> r.parts().stream())
                .mapToInt(Integer::intValue)
                .toArray();
        cookoffSeconds = rings.cookoffSeconds();
        glowSeconds = rings.core().map(RingSpec.Core::glowSeconds).orElse(0.5);
        bayTellSeconds = rings.core()
                .flatMap(RingSpec.Core::bays)
                .map(RingSpec.Bays::tellSeconds)
                .orElse(0.5);
        deadAngle = new double[ringArt.length];
        reset();
    }

    /**
     * The looks of the level's boss when it has rings (its data's {@code rings}), its pivots and its segments; else
     * null.
     */
    static SovereignLooks of(Sprites sprites, FlashShader flash, SetPieceSpec boss, JsonValue pivots) {
        if (boss == null
                || pivots == null
                || pivots.get("rings") == null
                || boss.boss().flatMap(BossSpec::rings).isEmpty()
                || !sprites.has(boss.slug() + "-base")) {
            return null;
        }
        return new SovereignLooks(sprites, flash, boss, pivots);
    }

    /** For the tests: the looks following a sortie of {@code boss} without its sprites (they draw nothing). */
    static SovereignLooks following(SetPieceSpec boss, JsonValue pivots) {
        return new SovereignLooks(null, null, boss, pivots);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion single(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.region(name) : null;
    }

    /** The level restarts (or the fight is retried): no glow, no tell, no line, no death. */
    void reset() {
        glowTick = NEVER;
        java.util.Arrays.fill(bayTell, NEVER);
        shieldHitTick = NEVER;
        shieldBreakTick = NEVER;
        shieldRefillTick = NEVER;
        lineFiredTick = NEVER;
        linePart = -1;
        deathTick = NEVER;
        hits.reset();
    }

    // --- the frames --------------------------------------------------------------------------------

    /** The segment rule (the pivots' {@code segment_rule}): a heading's place in the quarter, 0–90°. */
    private static double inQuarter(double degrees) {
        double d = ((degrees % 360) + 360) % 360;
        return d - 90.0 * segmentTurns(degrees);
    }

    /**
     * The segment rule for a heading of {@code degrees} (clockwise on the screen from straight down): its frame in a set
     * of {@code n8 + 1} over 0–45°, round(r / 45 × n8), or round((90 − r) / 45 × n8) where it is mirrored.
     */
    static int segmentFrame(double degrees, int n8) {
        double r = inQuarter(degrees);
        int frame = (int) Math.round((r > 45 ? 90 - r : r) / 45 * n8);
        return Math.clamp(frame, 0, n8);
    }

    /** The segment rule: whether the frame is mirrored across the 45° line (the heading's place in its quarter > 45°). */
    static boolean segmentMirrored(double degrees) {
        return inQuarter(degrees) > 45;
    }

    /** The segment rule: the quarter turns clockwise, floor(d / 90). */
    static int segmentTurns(double degrees) {
        double d = ((degrees % 360) + 360) % 360;
        return Math.min(3, (int) Math.floor(d / 90));
    }

    /** A turret's frame for its heading (radians clockwise from straight down): k = round(h × 64 / 2π) mod 64. */
    static int turretFrame(double radians) {
        return Math.floorMod((int) Math.round(radians * HEADINGS / (2 * Math.PI)), HEADINGS);
    }

    /** A mount's frame: dark before its ring lights, red while the battery is armed, else gold. */
    static int mountFrame(boolean lit, boolean armed) {
        return !lit ? MOUNT_DARK : armed ? MOUNT_ARMED : MOUNT_GOLD;
    }

    /** The core's frame: shut, its petals parting over the opening ({@code openShare} 0–1), open. */
    static int coreFrame(boolean opening, boolean open, double openShare) {
        if (open) {
            return CORE_FRAMES - 1;
        }
        return opening ? (int) Math.round(Math.clamp(openShare, 0, 1) * (CORE_FRAMES - 1)) : 0;
    }

    /** A bay's frame {@code since} steps into its tell of {@code tellTicks}: opening over the tell, then open. */
    static int bayFrame(long since, int tellTicks) {
        if (since < 0) {
            return 0;
        }
        return since >= tellTicks ? BAY_OPEN : 1 + (int) Math.min(1, 2 * since / Math.max(1, tellTicks));
    }

    /** When battery {@code n} of the cook-off order cooks off after the kill, s. */
    double cookoffAt(int n) {
        return n * cookoffSeconds;
    }

    /** When the last cook-off is over, s after the kill: the collapse and the drift start then. */
    double cookoffsEnd() {
        return order.length * cookoffSeconds;
    }

    /** When the core's blow, the vacuum blast and the white flash come, s after the kill. */
    double blastAt() {
        return cookoffsEnd() + BLAST_AFTER;
    }

    /** How far a segment has drifted {@code t} s into the drift, px: from 14 px/s growing evenly to 22. */
    static double drift(double t) {
        double a = (DRIFT_TO - DRIFT_FROM) / DRIFT_SECONDS;
        return DRIFT_FROM * t + a * t * t / 2;
    }

    /** Segment {@code j}'s turn {@code t} s into the drift, degrees: 5-11 °/s, alternating its sign. */
    static double driftTurn(int ring, int j, double t) {
        double rate = TURN_FROM + TURN_SPREAD * (((j * 7 + ring * 3) % 5) / 4.0);
        return ((j + ring) % 2 == 0 ? rate : -rate) * t;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The ringed boss among the set pieces, or null. */
    static SetPiece sovereign(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.ring().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** After each simulation step: the core's glow, the bays' tells, the shield's moments, the lines fired, the kill. */
    void stepped(Sortie sortie) {
        SetPiece piece = sovereign(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (piece.destroyed()) {
            if (deathTick == NEVER) {
                died(piece, sortie, tick);
            }
            return;
        }
        if (deathTick != NEVER) {
            // A retry from the boss checkpoint brings it back.
            reset();
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case CORE_GLOW -> glowTick = tick;
                case BAY_TELL -> {
                    int bay = events.value(i);
                    if (bay >= 0 && bay < bayTell.length) {
                        bayTell[bay] = tick;
                    }
                }
                case PART_SHIELD_HIT -> {
                    if (events.value(i) == corePart) {
                        shieldHitTick = tick;
                        shieldHitAngle = Math.atan2(
                                sortie.ship().y() - piece.partY(corePart),
                                sortie.ship().x() - piece.partX(corePart));
                    }
                }
                case PART_SHIELD_BROKEN -> {
                    if (events.value(i) == corePart) {
                        shieldBreakTick = tick;
                    }
                }
                case PART_SHIELD_REFILLED -> {
                    if (events.value(i) == corePart) {
                        shieldRefillTick = tick;
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
                default -> {}
            }
        }
    }

    /** The kill: the station's place, the scroll and the rings' angles as it died. */
    private void died(SetPiece piece, Sortie sortie, long tick) {
        deathTick = tick;
        RingArena ring = piece.ring().orElseThrow();
        deadX = Math.round(piece.renderX(1));
        deadY = Math.round(piece.renderY(1));
        deadScroll = sortie.groundScroll();
        for (int i = 0; i < deadAngle.length; i++) {
            deadAngle[i] = ring.renderAngle(i, 1);
        }
    }

    /** How long ago it was killed, s, between the last step and the next; -1 while it lives. */
    double deathAge(long tick, float alpha) {
        return deathTick == NEVER ? -1 : (tick + alpha - deathTick) * SimStep.SECONDS;
    }

    /** Its death's length as drawn, s: the cook-offs, then the drift (the blast inside it). */
    double deathSeconds() {
        return cookoffsEnd() + DRIFT_SECONDS;
    }

    /** Whether the core's fan glow runs at step {@code tick} (its slits lit). */
    boolean glowing(long tick) {
        return glowTick != NEVER && tick - glowTick < SimStep.ticks(glowSeconds);
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * Over the ground units: the base and its lights, the rings' segments and lamps, the overlays, the arc's band, the
     * batteries, the bays and the core; after the kill its death's solid parts.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash, double scroll) {
        SetPiece piece = sovereign(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        if (deathTick != NEVER && piece.destroyed()) {
            drawDeath(batch, tick, alpha, scroll);
            return;
        }
        if (!piece.present() && !piece.approaching()) {
            return;
        }
        RingArena ring = piece.ring().orElseThrow();
        RingCore heart = ring.core().orElse(null);
        double cx = Math.round(piece.renderX(alpha));
        double cy = Math.round(piece.renderY(alpha));
        if (heart != null && heart.opening() && (tick / SHUDDER_TICKS) % 2 == 1) {
            // The station shudders as the core opens.
            cx += 1;
        }
        boolean coreOut = heart != null && (heart.opening() || heart.open());
        drawBase(batch, ring, cx, cy, coreOut, 1);
        for (int i = 0; i < ringArt.length; i++) {
            RingArt look = ringArt[i];
            if (look == null || !look.drawn()) {
                continue;
            }
            float tint = ring.locked(i) ? LOCKED_TINT : 1;
            batch.setColor(tint, tint, tint, 1);
            double angle = Math.toDegrees(ring.renderAngle(i, alpha));
            double start =
                    Math.toDegrees(spec.rings().orElseThrow().rings().get(i).startRadians());
            for (int j = 0; j < look.segments; j++) {
                drawSegment(batch, look, angle + start + 360.0 * j / look.segments, cx, cy, 0, 0);
            }
        }
        batch.setColor(Color.WHITE);
        additive(batch);
        for (int i = 0; i < ringArt.length; i++) {
            RingArt look = ringArt[i];
            if (look == null || lampRed == null || !ring.lit(i) || ring.locked(i)) {
                continue;
            }
            double angle = Math.toDegrees(ring.renderAngle(i, alpha))
                    + Math.toDegrees(spec.rings().orElseThrow().rings().get(i).startRadians());
            for (int j = 0; j < look.segments; j++) {
                double heading = angle + 360.0 * j / look.segments;
                for (double lamp : look.lampDegrees) {
                    drawAt(batch, lampRed, cx, cy, look.lampRadius, Math.toRadians(heading + lamp));
                }
            }
        }
        normal(batch);
        if (shade != null) {
            drawScaled(batch, shade, cx, cy, OVERLAY_SCALE);
        }
        additive(batch);
        if (rim != null) {
            drawScaled(batch, rim, cx, cy, OVERLAY_SCALE);
        }
        drawArcs(batch, piece, ring, cx, cy, tick);
        normal(batch);
        drawBatteries(batch, piece, ring, cx, cy, alpha, tick, whiteFlash);
        if (heart != null) {
            drawBays(batch, heart, cx, cy, tick);
        }
        drawCore(batch, piece, heart, cx, cy, tick, whiteFlash);
        batch.setColor(Color.WHITE);
    }

    private static void additive(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    }

    private static void normal(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The base and its catwalk lights: the inner catwalk with the inner ring, the outer with the middle; out at {@code dark}. */
    private void drawBase(SpriteBatch batch, RingArena ring, double cx, double cy, boolean dark, float fade) {
        if (base == null) {
            return;
        }
        batch.setColor(1, 1, 1, fade);
        drawCentred(batch, base, cx, cy);
        batch.setColor(Color.WHITE);
        if (dark || ring == null) {
            return;
        }
        additive(batch);
        int n = ring.ringCount();
        if (n >= 1 && ring.lit(n - 1)) {
            lamps(batch, innerWhite, lampWhite, cx, cy);
            lamps(batch, innerGold, lampGold, cx, cy);
        }
        if (n >= 2 && ring.lit(n - 2)) {
            lamps(batch, outerWhite, lampWhite, cx, cy);
            lamps(batch, outerGold, lampGold, cx, cy);
        }
        normal(batch);
    }

    private static void lamps(SpriteBatch batch, double[][] points, AtlasRegion lamp, double cx, double cy) {
        if (lamp == null) {
            return;
        }
        for (double[] point : points) {
            drawCentred(batch, lamp, cx + point[0], cy - point[1]);
        }
    }

    /** A sprite centred {@code radius} px from the centre at {@code heading} (radians clockwise from straight down). */
    private static void drawAt(
            SpriteBatch batch, TextureRegion region, double cx, double cy, double radius, double heading) {
        drawCentred(batch, region, cx - radius * Math.sin(heading), cy - radius * Math.cos(heading));
    }

    /**
     * Segment {@code look} at {@code degrees} by the segment rule round the centre (cx, cy) (play field, y up), moved by
     * (tx, ty); the batch's colour.
     */
    private void drawSegment(
            SpriteBatch batch, RingArt look, double degrees, double cx, double cy, double tx, double ty) {
        int k = segmentFrame(degrees, look.n8);
        AtlasRegion region = look.frames.get(k);
        float color = batch.getPackedColor();
        int x0 = look.ox[k];
        int y0 = look.oy[k];
        int w = look.w[k];
        int h = look.h[k];
        boolean mirrored = segmentMirrored(degrees);
        int turns = segmentTurns(degrees);
        double ox = X0 + cx + tx;
        double oy = cy + ty;
        // The frame's corners on the screen (y down) round the centre: bottom-left, top-left, top-right, bottom-right.
        corner(0, x0, y0 + h, mirrored, turns, ox, oy, color, region.getU(), region.getV2());
        corner(5, x0, y0, mirrored, turns, ox, oy, color, region.getU(), region.getV());
        corner(10, x0 + w, y0, mirrored, turns, ox, oy, color, region.getU2(), region.getV());
        corner(15, x0 + w, y0 + h, mirrored, turns, ox, oy, color, region.getU2(), region.getV2());
        batch.draw(region.getTexture(), quad, 0, quad.length);
    }

    /** One corner: mirrored (x, y) → (−y, −x), then quarter turns clockwise (x, y) → (−y, x); to the play field. */
    private void corner(
            int at,
            double x,
            double y,
            boolean mirrored,
            int turns,
            double ox,
            double oy,
            float color,
            float u,
            float v) {
        quad[at] = (float) (ox + turnX(x, y, mirrored, turns));
        quad[at + 1] = (float) (oy - turnY(x, y, mirrored, turns));
        quad[at + 2] = color;
        quad[at + 3] = u;
        quad[at + 4] = v;
    }

    /** The segment rule's map of a point (x, y) round the centre (screen, y down): its new x. */
    static double turnX(double x, double y, boolean mirrored, int turns) {
        double px = mirrored ? -y : x;
        double py = mirrored ? -x : y;
        return switch (Math.floorMod(turns, 4)) {
            case 0 -> px;
            case 1 -> -py;
            case 2 -> -px;
            default -> py;
        };
    }

    /** The segment rule's map of a point (x, y) round the centre (screen, y down): its new y. */
    static double turnY(double x, double y, boolean mirrored, int turns) {
        double px = mirrored ? -y : x;
        double py = mirrored ? -x : y;
        return switch (Math.floorMod(turns, 4)) {
            case 0 -> py;
            case 1 -> px;
            case 2 -> -py;
            default -> -px;
        };
    }

    /** The fire arc's faint red band across each live ring's hull, round the arc's sector (additive, the batch is). */
    private void drawArcs(SpriteBatch batch, SetPiece piece, RingArena ring, double cx, double cy, long tick) {
        if (ring.spinningUp() || piece.destroyed()) {
            return;
        }
        double half = ring.arcRadians() / 2;
        double bearing = ring.bearing();
        float pulse = 0.8f + 0.2f * (float) Math.sin(tick * SimStep.SECONDS * 2 * Math.PI * 0.8);
        batch.setColor(1, 1, 1, pulse);
        float color = batch.getPackedColor();
        for (int i = 0; i < ringArt.length; i++) {
            RingArt look = ringArt[i];
            if (look == null || look.arc == null || ring.locked(i) || !ring.lit(i)) {
                continue;
            }
            AtlasRegion strip = look.arc;
            double inner = look.arcFrom;
            double outer = look.arcTo;
            double step = strip.getRegionHeight() / ((inner + outer) / 2);
            int count = Math.max(1, (int) Math.ceil(2 * half / step));
            double da = 2 * half / count;
            for (int s = 0; s < count; s++) {
                double a0 = bearing - half + s * da;
                double a1 = a0 + da;
                arcCorner(0, cx, cy, inner, a0, color, strip.getU(), strip.getV());
                arcCorner(5, cx, cy, outer, a0, color, strip.getU2(), strip.getV());
                arcCorner(10, cx, cy, outer, a1, color, strip.getU2(), strip.getV2());
                arcCorner(15, cx, cy, inner, a1, color, strip.getU(), strip.getV2());
                batch.draw(strip.getTexture(), quad, 0, quad.length);
            }
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

    /** Battery {@code p}'s place now: on its ring at its mount's angle, the ring's render angle. */
    private double mountHeading(RingArena ring, int p, float alpha) {
        int i = ring.battery(p);
        RingSpec.Ring spec = this.spec.rings().orElseThrow().rings().get(i);
        return spec.mountRadians(ring.slot(p)) + ring.renderAngle(i, alpha);
    }

    /** The batteries: each mount (dark, gold or red; a burnt collar venting once dead) under its turret. */
    private void drawBatteries(
            SpriteBatch batch,
            SetPiece piece,
            RingArena ring,
            double cx,
            double cy,
            float alpha,
            long tick,
            float whiteFlash) {
        for (int p = 0; p < piece.partCount(); p++) {
            int i = ring.battery(p);
            if (i < 0 || ringArt[i] == null) {
                continue;
            }
            BatteryArt look = ringArt[i].battery;
            double heading = mountHeading(ring, p, alpha);
            double radius = ringArt[i].mountRadius;
            double x = cx - radius * Math.sin(heading);
            double y = cy - radius * Math.cos(heading);
            if (piece.partWrecked(p)) {
                if (!look.wreck.isEmpty()) {
                    drawCentred(batch, look.wreck.first(), x, y);
                }
                if (!vent.isEmpty()) {
                    int frame = (int) Math.floorMod(tick / VENT_TICKS + 3L * p, (long) vent.size);
                    drawCentred(batch, vent.get(frame), x, y);
                }
                continue;
            }
            if (!look.mount.isEmpty()) {
                int frame = mountFrame(ring.lit(i), ring.armed(p));
                drawCentred(batch, look.mount.get(Math.min(look.mount.size - 1, frame)), x, y);
            }
            Array<AtlasRegion> turret = piece.partHatchOpen(p) && !look.open.isEmpty() ? look.open : look.turret;
            if (turret.isEmpty()) {
                continue;
            }
            AtlasRegion region = turret.get(Math.min(turret.size - 1, turretFrame(aim(piece, ring, p, alpha))));
            if (hits.on(p, tick, piece.partTicksSinceHit(p)) && flash != null) {
                flash.draw(
                        batch, region, Math.round(X0 + x), Math.round(y), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
            } else {
                drawCentred(batch, region, x, y);
            }
        }
    }

    /**
     * Battery {@code p}'s turret heading: along its rail line while that telegraphs or fires, along its sweep's beam
     * (or the sweep's first edge over its telegraph), else toward the ship.
     */
    private double aim(SetPiece piece, RingArena ring, int p, float alpha) {
        BossLines lines = piece.lines().orElse(null);
        if (lines != null && lines.running() && lines.part() == p) {
            return Math.atan2(-lines.dirX(), -lines.dirY());
        }
        int a = sweepOf(piece, p);
        if (a >= 0) {
            return piece.sweepBeaming(a) ? piece.sweepBeam(a, alpha) : piece.sweepFrom(a);
        }
        return ring.turretRadians(p);
    }

    /** The attack whose sweep runs (telegraph or beam) from part {@code p} now; -1 for none. */
    private int sweepOf(SetPiece piece, int p) {
        for (int a = 0; a < piece.attackCount() && a < spec.attacks().size(); a++) {
            if (spec.attacks().get(a).sweep().isEmpty() || !(piece.sweepTelegraphing(a) || piece.sweepBeaming(a))) {
                continue;
            }
            if (Math.hypot(piece.sweepOriginX(a) - piece.partX(p), piece.sweepOriginY(a) - piece.partY(p)) < 12) {
                return a;
            }
        }
        return -1;
    }

    /** The rim bays' doors: shut, opening over a stream's tell, open while its stream runs. */
    private void drawBays(SpriteBatch batch, RingCore heart, double cx, double cy, long tick) {
        int open = heart.open() ? heart.bayOpen() : -1;
        int tellTicks = SimStep.ticks(bayTellSeconds);
        for (int b = 0; b < bays.size; b++) {
            Array<AtlasRegion> doors = bays.get(b);
            if (doors.isEmpty()) {
                continue;
            }
            int frame = b == open && bayTell[b] != NEVER ? bayFrame(tick - bayTell[b], tellTicks) : 0;
            AtlasRegion region = doors.get(Math.min(doors.size - 1, frame));
            batch.draw(
                    region, (float) (X0 + cx + bayOffsetX[b]), (float) (cy - bayOffsetY[b] - region.getRegionHeight()));
        }
    }

    /** The core: shut, its petals parting over the opening, open (a hit flashing it). */
    private void drawCore(
            SpriteBatch batch, SetPiece piece, RingCore heart, double cx, double cy, long tick, float whiteFlash) {
        if (core.isEmpty()) {
            return;
        }
        int frame = heart == null ? 0 : coreFrame(heart.opening(), heart.open(), heart.openShare());
        AtlasRegion region = core.get(Math.min(core.size - 1, frame));
        if (hits.on(corePart, tick, piece.partTicksSinceHit(corePart)) && flash != null) {
            flash.draw(batch, region, Math.round(X0 + cx), Math.round(cy), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
        } else {
            drawCentred(batch, region, cx, cy);
        }
    }

    /**
     * After the grade, additive and ungraded: the inner ring's gold teeth in its step's tell, the core's slits over a
     * fan's glow, the open reactor's light and its hex shield, the bays' light, the rail coils' charge and the laser
     * lenses; after the kill the cook-offs, the implosion and the blast.
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        SetPiece piece = sovereign(sortie);
        if (piece == null) {
            return;
        }
        long tick = sortie.tick();
        additive(batch);
        if (deathTick != NEVER && piece.destroyed()) {
            drawDeathGlows(batch, tick, alpha, scroll);
        } else if (piece.present()) {
            drawLiveGlows(batch, piece, piece.ring().orElseThrow(), tick, alpha);
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    private void drawLiveGlows(SpriteBatch batch, SetPiece piece, RingArena ring, long tick, float alpha) {
        double cx = Math.round(piece.renderX(alpha));
        double cy = Math.round(piece.renderY(alpha));
        for (int i = 0; i < ringArt.length; i++) {
            RingArt look = ringArt[i];
            if (look == null || !look.drawn() || ring.stepState(i) != RingArena.StepState.TELL || ring.locked(i)) {
                continue;
            }
            // The ring's gold teeth flash as it unlocks for its step.
            batch.setColor(TELL_GOLD.r, TELL_GOLD.g, TELL_GOLD.b, TELL_ALPHA);
            double angle = Math.toDegrees(ring.renderAngle(i, alpha))
                    + Math.toDegrees(spec.rings().orElseThrow().rings().get(i).startRadians());
            for (int j = 0; j < look.segments; j++) {
                drawSegment(batch, look, angle + 360.0 * j / look.segments, cx, cy, 0, 0);
            }
        }
        batch.setColor(Color.WHITE);
        BossLines lines = piece.lines().orElse(null);
        for (int p = 0; p < piece.partCount(); p++) {
            int i = ring.battery(p);
            if (i < 0 || ringArt[i] == null || piece.partWrecked(p)) {
                continue;
            }
            BatteryArt look = ringArt[i].battery;
            double heading = mountHeading(ring, p, alpha);
            double radius = ringArt[i].mountRadius;
            double x = cx - radius * Math.sin(heading);
            double y = cy - radius * Math.cos(heading);
            int aim = turretFrame(aim(piece, ring, p, alpha));
            if (lines != null && lines.telegraphing() && lines.part() == p && !look.charge.isEmpty()) {
                int frame = Math.min(look.charge.size - 1, (int) (lines.charge() * look.charge.size));
                double[] at = aim < look.coils.length ? look.coils[aim] : ORIGIN;
                drawCentred(batch, look.charge.get(frame), x + at[0], y - at[1]);
            }
            int a = sweepOf(piece, p);
            if (a >= 0 && !look.lens.isEmpty()) {
                int frame = piece.sweepBeaming(a) ? look.lens.size - 1 : (int) (tick / 6 % (look.lens.size - 1));
                double[] at = aim < look.lensAt.length ? look.lensAt[aim] : ORIGIN;
                drawCentred(batch, look.lens.get(frame), x + at[0], y - at[1]);
            }
        }
        RingCore heart = ring.core().orElse(null);
        if (heart == null) {
            return;
        }
        if (!heart.open() && !heart.opening() && glowing(tick) && coreSlits != null) {
            double share = (tick - glowTick) / (double) Math.max(1, SimStep.ticks(glowSeconds));
            batch.setColor(1, 1, 1, (float) (0.5 + 0.5 * share));
            drawCentred(batch, coreSlits, cx, cy);
            batch.setColor(Color.WHITE);
        }
        if (heart.open() && coreGlow.size >= 4) {
            int frame = heart.spiralTelling() ? 2 + (int) (tick / 4 % 2) : (int) (tick / GLOW_TICKS % 2);
            drawCentred(batch, coreGlow.get(frame), cx, cy);
        }
        drawShield(batch, piece, cx, cy, tick);
        int open = heart.open() ? heart.bayOpen() : -1;
        if (open >= 0 && open < bays.size && bayTell[open] != NEVER && bayGlow.size >= 2) {
            long since = tick - bayTell[open];
            AtlasRegion glow = bayGlow.get(since < SimStep.ticks(bayTellSeconds) ? 0 : 1);
            AtlasRegion doors = bays.get(open).isEmpty() ? null : bays.get(open).first();
            int half = doors == null ? 12 : doors.getRegionWidth() / 2;
            drawCentred(batch, glow, cx + bayOffsetX[open] + half, cy - bayOffsetY[open] - half);
        }
    }

    /** The open core's hex shield: holding (its opacity by its fill), a hit's rim, its shatter and its refill. */
    private void drawShield(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick) {
        PartShields shields = piece.shields().orElse(null);
        RingCore heart = piece.ring().flatMap(RingArena::core).orElse(null);
        if (shields == null || heart == null || !heart.open() || !shields.fitted(corePart)) {
            return;
        }
        long broke = tick - shieldBreakTick;
        if (shieldBreakTick != NEVER && broke >= 0 && broke < (long) shieldBreak.size * SHIELD_BREAK_TICKS) {
            drawCentred(batch, shieldBreak.get((int) (broke / SHIELD_BREAK_TICKS)), cx, cy);
        }
        if (shields.shield(corePart) <= 0) {
            return;
        }
        long refill = tick - shieldRefillTick;
        if (shieldRefillTick != NEVER && refill >= 0 && refill < (long) shieldRefill.size * SHIELD_REFILL_TICKS) {
            drawCentred(batch, shieldRefill.get((int) (refill / SHIELD_REFILL_TICKS)), cx, cy);
        } else if (!shieldHold.isEmpty()) {
            batch.setColor(1, 1, 1, EnemyShieldLooks.holdOpacity(shields.share(corePart)));
            drawCentred(batch, shieldHold.get((int) (tick / SHIELD_HOLD_TICKS % shieldHold.size)), cx, cy);
            batch.setColor(Color.WHITE);
        }
        long hit = tick - shieldHitTick;
        if (shieldHitTick != NEVER
                && hit >= 0
                && hit < (long) shieldHit.size * SHIELD_HIT_TICKS
                && !shieldHold.isEmpty()) {
            double r = shieldHold.first().getRegionWidth() / 2.0 - 6;
            drawCentred(
                    batch,
                    shieldHit.get((int) (hit / SHIELD_HIT_TICKS)),
                    cx + r * Math.cos(shieldHitAngle),
                    cy + r * Math.sin(shieldHitAngle));
        }
    }

    /**
     * With the bullets, additive: the rail lines (the red line over the telegraph, its direction locked and its origin
     * riding with its battery, then the Rail Bunker's rail live and fading with its muzzle flash) and the laser sweeps (the Sentinel Tower's crimson wedge over the telegraph,
     * the beam and its tip).
     */
    void drawLines(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = sovereign(sortie);
        if (piece == null || !piece.present() || piece.destroyed()) {
            return;
        }
        long tick = sortie.tick();
        additive(batch);
        BossLines lines = piece.lines().orElse(null);
        if (lines != null && lines.telegraphing()) {
            Color red = LineLooks.TELEGRAPH;
            batch.setColor(red.r, red.g, red.b, LineLooks.telegraphOpacity(lines.charge(), tick));
            double in = muzzle(piece, lines.part(), Math.atan2(-lines.dirX(), -lines.dirY()));
            // Round 68's row 9 (pending): the origin rides with its battery on the turning ring (between the steps).
            double ox = lines.fromX() - lines.stepX() * (1 - alpha);
            double oy = lines.fromY() - lines.stepY() * (1 - alpha);
            stroke(batch, ox + lines.dirX() * in, oy + lines.dirY() * in, lines.dirX(), lines.dirY());
            batch.setColor(Color.WHITE);
        }
        if (lineFiredTick != NEVER && linePart >= 0 && rail.size == 4) {
            long since = tick - lineFiredTick;
            var line = lines == null || lines.attack() < 0
                    ? null
                    : spec.attacks().get(lines.attack()).line().orElse(null);
            int liveTicks = SimStep.ticks(line == null ? 0.2 : line.liveSeconds());
            int frame = railFrame(since, liveTicks);
            double in = muzzle(piece, linePart, Math.atan2(-lineDirX, -lineDirY));
            // The rail rides with its battery (live and fading); where it fired once the battery is gone.
            double ox = lineX;
            double oy = lineY;
            if (lines != null && lines.running() && lines.part() == linePart) {
                ox = lines.fromX() - lines.stepX() * (1 - alpha);
                oy = lines.fromY() - lines.stepY() * (1 - alpha);
            } else if (piece.partHp(linePart) > 0) {
                ox = piece.partX(linePart);
                oy = piece.partY(linePart);
            }
            if (frame >= 0) {
                drawRail(batch, rail.get(frame), in, ox, oy);
            }
            int flashFrame = (int) (since / FLASH_FRAME_TICKS);
            if (flashFrame < railFlash.size) {
                drawCentred(batch, railFlash.get(flashFrame), ox + lineDirX * in, oy + lineDirY * in);
            }
        }
        for (int a = 0; a < piece.attackCount() && a < spec.attacks().size(); a++) {
            var sweep = spec.attacks().get(a).sweep().orElse(null);
            if (sweep == null || !(piece.sweepTelegraphing(a) || piece.sweepBeaming(a))) {
                continue;
            }
            double x = piece.sweepOriginX(a);
            double y = piece.sweepOriginY(a);
            if (piece.sweepTelegraphing(a)) {
                float pulse = 0.45f + 0.35f * (float) Math.abs(Math.sin(tick * 0.4));
                String name = TOWER + "-telegraph-" + Math.round(Math.toDegrees(sweep.arcRadians()));
                double centre = (piece.sweepFrom(a) + piece.sweepTo(a)) / 2;
                if (pixel != null && sprites != null && sprites.has(name)) {
                    AtlasRegion wedge = sprites.region(name);
                    float h = wedge.getRegionHeight();
                    batch.setColor(1, 1, 1, Math.min(1, 0.6f + 0.5f * pulse));
                    batch.draw(
                            wedge,
                            (float) (X0 + x),
                            (float) (y - h / 2),
                            0,
                            h / 2,
                            wedge.getRegionWidth(),
                            h,
                            1,
                            1,
                            FarsideLooks.degrees(centre));
                }
            } else if (!beam.isEmpty()) {
                double heading = piece.sweepBeam(a, alpha);
                int frame = (int) (tick / 2 % beam.size);
                batch.setColor(Color.WHITE);
                FarsideLooks.drawBeam(batch, beam.get(frame), x, y, heading, sweep.length());
                if (!beamTip.isEmpty()) {
                    drawCentred(
                            batch,
                            beamTip.get(frame % beamTip.size),
                            x - Math.sin(heading) * sweep.length(),
                            y - Math.cos(heading) * sweep.length());
                }
            }
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    /** The muzzle's distance from part {@code p}'s centre along its line: the rail turret's, the core's rim. */
    private double muzzle(SetPiece piece, int p, double heading) {
        if (p == corePart) {
            return CORE_MUZZLE;
        }
        RingArena ring = piece.ring().orElseThrow();
        int i = p >= 0 ? ring.battery(p) : -1;
        if (i < 0 || ringArt[i] == null || ringArt[i].battery.muzzle.length == 0) {
            return 0;
        }
        double[] at = ringArt[i].battery.muzzle[turretFrame(heading) % ringArt[i].battery.muzzle.length];
        return Math.hypot(at[0], at[1]);
    }

    /** The rail's frame {@code since} steps after it fired, live for {@code liveTicks}: 0-1 live, 2-3 fading, -1 after. */
    static int railFrame(long since, int liveTicks) {
        if (since < 0) {
            return -1;
        }
        if (since < liveTicks) {
            return (int) (since / RAIL_FRAME_TICKS % 2);
        }
        long fading = (since - liveTicks) / RAIL_FRAME_TICKS;
        return fading < 2 ? 2 + (int) fading : -1;
    }

    /** The rail's strip tiled from {@code in} px along the last line fired from (ox, oy) to past the screen's edge. */
    private void drawRail(SpriteBatch batch, AtlasRegion strip, double in, double ox, double oy) {
        float degrees = (float) Math.toDegrees(Math.atan2(lineDirY, lineDirX));
        float w = strip.getRegionWidth();
        float h = strip.getRegionHeight();
        double length = 2 * (vanguard.sim.PlayField.WIDTH + vanguard.sim.PlayField.HEIGHT);
        for (double at = in; at < length; at += w) {
            double x = ox + lineDirX * at;
            double y = oy + lineDirY * at;
            if (x < -w || x > vanguard.sim.PlayField.WIDTH + w || y < -w || y > vanguard.sim.PlayField.HEIGHT + w) {
                break;
            }
            batch.draw(strip, (float) (X0 + x), (float) (y - h / 2), 0, h / 2, w, h, 1, 1, degrees);
        }
    }

    /** A 2 px line from (x, y) along (dx, dy) past the screen's edge. */
    private void stroke(SpriteBatch batch, double x, double y, double dx, double dy) {
        if (pixel == null) {
            return;
        }
        float width = LineLooks.TELEGRAPH_WIDTH;
        float length = (float) (2 * (vanguard.sim.PlayField.WIDTH + vanguard.sim.PlayField.HEIGHT));
        batch.draw(pixel, (float) (X0 + x), (float) (y - width / 2), 0, width / 2, length, width, 1, 1, (float)
                Math.toDegrees(Math.atan2(dy, dx)));
    }

    // --- the death ---------------------------------------------------------------------------------

    /** Its death's solid parts: the base scrolling away, the wrecked mounts, the segments drifting, the core folding. */
    private void drawDeath(SpriteBatch batch, long tick, float alpha, double scroll) {
        double age = deathAge(tick, alpha);
        if (age < 0) {
            return;
        }
        double cx = deadX;
        double cy = deadY - (scroll - deadScroll);
        drawBase(batch, null, cx, cy, true, 1);
        double drift = Math.max(0, age - cookoffsEnd());
        double share = Math.min(1, drift / DRIFT_SECONDS);
        float fade = (float) (1 - share);
        if (share < 1) {
            RingSpec rings = spec.rings().orElseThrow();
            batch.setColor(LOCKED_TINT, LOCKED_TINT, LOCKED_TINT, fade);
            for (int i = 0; i < ringArt.length; i++) {
                RingArt look = ringArt[i];
                if (look == null || !look.drawn()) {
                    continue;
                }
                double start =
                        Math.toDegrees(deadAngle[i] + rings.rings().get(i).startRadians());
                for (int j = 0; j < look.segments; j++) {
                    double heading = start + 360.0 * j / look.segments;
                    double turned = heading + driftTurn(i, j, drift);
                    double out = drift(drift);
                    // Where its centre should be (drifted outward), less where the turned frame puts it.
                    double h0 = Math.toRadians(heading);
                    double h1 = Math.toRadians(turned);
                    double r = look.mountRadius;
                    double tx = -(r + out) * Math.sin(h0) + r * Math.sin(h1);
                    double ty = -(r + out) * Math.cos(h0) + r * Math.cos(h1);
                    drawSegment(batch, look, turned, cx, cy, tx, ty);
                }
            }
            batch.setColor(1, 1, 1, fade);
            if (shade != null) {
                drawScaled(batch, shade, cx, cy, OVERLAY_SCALE);
            }
            drawDeadBatteries(batch, rings, cx, cy, drift, tick);
            batch.setColor(Color.WHITE);
        }
        double folding = age - cookoffsEnd();
        int frame = folding < 0
                ? CORE_FRAMES - 1
                : CORE_FRAMES - 1 - (int) (folding * SimStep.PER_SECOND / IMPLOSION_TICKS);
        if (frame >= 0 && !core.isEmpty() && age < blastAt()) {
            drawCentred(batch, core.get(Math.min(core.size - 1, frame)), cx, cy);
        }
        long since = tick - deathTick;
        for (int n = 0; n < order.length; n++) {
            long at = since - SimStep.ticks(cookoffAt(n));
            int f = (int) (at / COOKOFF_TICKS);
            if (at >= 0 && f < cookoffDebris.size) {
                double[] place = deadBattery(rings(), order[n], cx, cy);
                drawCentred(batch, cookoffDebris.get(f), place[0], place[1]);
            }
        }
    }

    private RingSpec rings() {
        return spec.rings().orElseThrow();
    }

    /** The wrecked mounts drifting out with their segments, fading. */
    private void drawDeadBatteries(SpriteBatch batch, RingSpec rings, double cx, double cy, double drift, long tick) {
        for (int i = 0; i < ringArt.length; i++) {
            RingArt look = ringArt[i];
            if (look == null || look.battery.wreck.isEmpty()) {
                continue;
            }
            RingSpec.Ring ring = rings.rings().get(i);
            for (int k = 0; k < ring.parts().size(); k++) {
                double heading = ring.mountRadians(k) + deadAngle[i];
                double r = look.mountRadius + drift(drift);
                drawCentred(batch, look.battery.wreck.first(), cx - r * Math.sin(heading), cy - r * Math.cos(heading));
                if (!vent.isEmpty()) {
                    int frame = (int) Math.floorMod(tick / VENT_TICKS + 3L * k + i, (long) vent.size);
                    drawCentred(batch, vent.get(frame), cx - r * Math.sin(heading), cy - r * Math.cos(heading));
                }
            }
        }
    }

    /** Battery part {@code p}'s place at the kill (on its ring at the death's angle), into {@link #place}. */
    private double[] deadBattery(RingSpec rings, int p, double cx, double cy) {
        place[0] = cx;
        place[1] = cy;
        for (int i = 0; i < rings.rings().size(); i++) {
            RingSpec.Ring ring = rings.rings().get(i);
            int k = ring.parts().indexOf(p);
            if (k >= 0 && ringArt[i] != null) {
                double heading = ring.mountRadians(k) + deadAngle[i];
                double r = ringArt[i].mountRadius;
                place[0] = cx - r * Math.sin(heading);
                place[1] = cy - r * Math.cos(heading);
            }
        }
        return place;
    }

    /** The death's lights: the cook-offs' flashes, the implosion and the vacuum blast drawn 2x. */
    private void drawDeathGlows(SpriteBatch batch, long tick, float alpha, double scroll) {
        double age = deathAge(tick, alpha);
        if (age < 0) {
            return;
        }
        double cx = deadX;
        double cy = deadY - (scroll - deadScroll);
        long since = tick - deathTick;
        for (int n = 0; n < order.length; n++) {
            long at = since - SimStep.ticks(cookoffAt(n));
            int f = (int) (at / COOKOFF_TICKS);
            if (at >= 0 && f < cookoff.size) {
                double[] place = deadBattery(rings(), order[n], cx, cy);
                drawCentred(batch, cookoff.get(f), place[0], place[1]);
            }
        }
        long implode = since - SimStep.ticks(cookoffsEnd() + IMPLOSION_AFTER);
        int f = (int) (implode / IMPLOSION_TICKS);
        if (implode >= 0 && f < implosion.size) {
            drawScaled(batch, implosion.get(f), cx, cy, 1.5f);
        }
        long blown = since - SimStep.ticks(blastAt());
        int b = (int) (blown / BLAST_TICKS);
        if (blown >= 0 && b < blast.size) {
            drawScaled(batch, blast.get(b), cx, cy, 2);
        }
    }

    // --- for the tests and the event effects -----------------------------------------------------

    /** The parts the boss has (the core's and the batteries'). */
    int parts() {
        return parts;
    }

    /** The steps from the kill to the core's blow (the white flash and the credit shower). */
    int blastTicks() {
        return SimStep.ticks(blastAt());
    }
}
