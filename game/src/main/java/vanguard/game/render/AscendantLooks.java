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
import vanguard.sim.BatteryBursts;
import vanguard.sim.BossLines;
import vanguard.sim.BossPlaybook;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.EscapeRun;
import vanguard.sim.EscapeSpec;
import vanguard.sim.PartMirrors;
import vanguard.sim.PartRegens;
import vanguard.sim.PartShields;
import vanguard.sim.PhasePlaybook;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part H, step H4a: the Ascendant's looks (design/enemies/bosses/ascendant, Presentation; Level 42's act boss;
 * tools/art/ascendant.py's sprites and assets/pivots/ascendant.json), following the simulation's set piece with its
 * {@link EscapeRun} as {@link SovereignLooks} and {@link HaloLooks} follow theirs. The hull is one fixed piece (bow down,
 * never turned) drawn with its frame's anchor on the piece's centre; every overlay sits in the hull's frame or at a
 * part's place, so the slide-in moves them together.
 *
 * <p>Over the air units ({@link #draw}), back to front. <b>Phase 1</b> (the gold hull): the hull, the generators (lit,
 * damaged below half, dead), the batteries (whole, damaged, dead) behind their mirror panels at the frame of each
 * panel's facing (down: the shattered stub; regrowing: fading in), the
 * cannon's base (whole, damaged below half, dead) and its turret at 64 headings toward the ship (holding a told line's
 * direction), the Dart bays' doors and the shoulder racks' hatches opening over their moves' tells, a hit part flashing
 * at most once each 0.25 s. <b>The eruption</b> (the phase-2 opening's 4 s): the 1 s shudder, then the keel frames
 * 0..5 over 3 s; phase 2 keeps frame 5 with the generators grafted, the sacs (at rest, swelling over the brood's tell,
 * split while it runs, burst husks once dead) and the heart (sealed, opening over 0.4 s when its phase starts, open).
 * <b>K</b> (the hull breaks, {@link EscapeRun#broken()}): the broken hull with the heart burst open and the sacs dead,
 * the chain of vacuum bursts down the keel (the pivots' {@code chain}) blowing gold plates out, then from the flash
 * (K+{@link EscapeSpec#flashSeconds()}) the wreck's two halves on the authored jagged fracture drifting apart out of the
 * sides by the escape's end, charred shards off the fracture; the lifeboat (its five weave tilts by its sideways speed).
 *
 * <p>After the grade, additive ({@link #drawGlows}): the arrival's running lights lit bow first over the slide-in, the
 * generators' gold beams to the living cannon and their halos, the cannon's gold hex shield (the units' shield set,
 * dimming with its share), each panel's heat glow ({@link MirrorLooks#heatColour}) with its cracks dark over it from 70 %
 * of its heat, its red rim over its battery's
 * burst tell and its slow specular sweep (the silver shimmer when it grows back), the tells' glows (the bow's muzzle
 * over the spinal lance's tell and line, the gun ports over the gold fan's, the bays' gold light, the racks' red lock
 * lights, the grafted mounts' flashes at the dead generators), the keel's teal cracks; the sacs' and heart's lime cores
 * pulsing (the heart dim while sealed), their healing pulse while they regenerate, the brooding sac's flaring seams; a
 * sac's burst at its death; at K the heart's burst and the surviving sacs' bursts; the halves' hot rims fading, their
 * orange and teal venting and the breaches' fires; the lifeboat's drive and its tear at the breakout. With the
 * bullets ({@link #drawLines}): the spinal lance's red line (thin and unsteady until its lock) and its white-gold beam.
 * Over the HUD ({@link #drawEscapeFlash}): the lifeboat's flash at the top edge at {@code ESCAPED}; from K there is no
 * boss bar ({@link #hidesBar}; round 80: the escape is a short scene). Presentation only; nothing is allocated per
 * frame.
 */
final class AscendantLooks {
    static final String SLUG = "ascendant";
    /** The turret's headings ({@code -cannon_0..63}: k x 5.625 degrees clockwise from straight down). */
    static final int HEADINGS = 64;
    /** The eruption's 1 s shudder, then the keel's six frames over 3 s (the pivots' {@code keel}). */
    static final double SHUDDER_SECONDS = 1;
    /** The heart opens over this long as its phase starts (two sacs dead), s. */
    static final double HEART_OPEN_SECONDS = 0.4;
    /** The bursts and the tear at 12 fps, the vacuum chain at 15 fps, the vents at 12 fps, the fires at 10 fps. */
    static final double BURST_FPS = 12;

    static final double CHAIN_FPS = 15;
    static final double FIRE_FPS = 10;
    /** The generator beams' pulse, frames a second; the strip's length along the line, px. */
    static final double BEAM_FPS = 8;
    /** The panels' specular sweep tip to tip, s; each battery's phase apart; its glint's strength. */
    static final double SWEEP_SECONDS = 2.5;

    static final double SWEEP_PHASE = 0.6;
    static final float SWEEP_GLINT = 0.45f;
    /** A gun port's flash: 2 steps a frame (the gold fan's tell). */
    static final int PORT_TICKS = 2;
    /** The lifeboat leans one tilt frame (10 degrees) per this much sideways speed, px/s. */
    static final double TILT_SPEED = 25;
    /** The lifeboat's flash toward the gate at {@code ESCAPED}: its centre this far below the top edge (the art's), px. */
    static final double ESCAPE_FLASH_Y = 30;
    /** The lance's beam colour (white-gold) and its core. */
    static final Color LANCE = Color.valueOf("FFE6A0");

    private static final Color HEAT = new Color();
    private static final Color CRACK_DARK = new Color(40 / 255f, 40 / 255f, 40 / 255f, 1);
    private static final long NEVER = Long.MIN_VALUE / 2;
    private static final int SAC_SPLIT = 4;
    private static final int HEART_OPEN = 3;

    private final FlashShader flash;
    private final TextureRegion pixel;
    private final BossSpec spec;
    private final EscapeSpec escapeSpec;
    /** A panel's regrowth (its silver shimmer), steps. */
    private final int regrowTicks;
    /** The parts by name (-1 when the spec lacks one). */
    final int cannon;

    final int[] gens;
    final int[] batteries;
    final int[] sacs;
    final int heart;
    final int lifeboat;
    /** The hull's frame anchor (its px on the piece's centre, y down) and the keel's and cracks' frame places. */
    private final float anchorX;

    private final float anchorY;
    private final float keelX;
    private final float keelY;
    private final float cracksX;
    private final float cracksY;
    private final int keelFrames;
    private final double keelSeconds;
    /** Pivot places as offsets from the centre, y up: the lamps, ports, bays, racks. */
    private final double[][] redLamps;

    private final double[][] goldLamps;
    private final double lampSeconds;
    private final double[][] ports;
    private final double[][] bays;
    private final double[][] racks;
    private final float rackLockX;
    private final float rackLockY;
    /** The cannon's muzzle per heading in its cell (px from the top left, y down) and the cell's size. */
    private final float[][] muzzles;

    private final float turretCell;
    /** The panels' frames: their headings, and per frame its arc's tips' angles (y up) and radius. */
    private final int[] panelHeadings;

    private final double[] panelFrom;
    private final double[] panelTurn;
    private final double[] panelRadius;
    /** The K chain: per burst its time after K, place (y up) and size (true: large). */
    private final double[] chainT;

    private final double[] chainX;
    private final double[] chainY;
    private final boolean[] chainLarge;
    /** The wreck's halves (0 left, 1 right): frame place, drift (y up), fracture points, vents and fires. */
    private final float[] halfX = new float[2];

    private final float[] halfY = new float[2];
    private final double[] driftX = new double[2];
    private final double[] driftY = new double[2];
    final double[][][] fracture = new double[2][][];
    private final double[][][] vents = new double[2][][];
    private final boolean[][] ventTeal = new boolean[2][];
    private final double[][][] fires = new double[2][][];
    /** The lifeboat's drive per tilt frame (px in its sprite, y down). */
    private final double[][] drive;

    private final AtlasRegion hull;
    private final Array<AtlasRegion> keel;
    private final Array<AtlasRegion> keelCracks;
    private final Array<AtlasRegion> bayL;
    private final Array<AtlasRegion> bayR;
    private final AtlasRegion bayLight;
    private final Array<AtlasRegion> rackL;
    private final Array<AtlasRegion> rackR;
    private final AtlasRegion rackLock;
    private final Array<AtlasRegion> portFlash;
    private final AtlasRegion lampRed;
    private final AtlasRegion lampGold;
    private final Array<AtlasRegion> cannonBase;
    private final Array<AtlasRegion> turret;
    private final Array<AtlasRegion> muzzle;
    private final Array<AtlasRegion> generator;
    private final AtlasRegion generatorGlow;
    private final Array<AtlasRegion> beam;
    private final Array<AtlasRegion> battery;
    private final Array<AtlasRegion> panel;
    private final Array<AtlasRegion> panelGlow;
    private final Array<AtlasRegion> panelCracks;
    private final Array<AtlasRegion> panelRim;
    private final Array<AtlasRegion> panelDown;
    private final AtlasRegion panelSweep;
    private final Array<AtlasRegion> sac;
    private final AtlasRegion sacDead;
    private final AtlasRegion sacCore;
    private final AtlasRegion sacHeal;
    private final AtlasRegion sacFlare;
    private final Array<AtlasRegion> heartFrames;
    private final AtlasRegion heartCore;
    private final AtlasRegion heartHeal;
    private final Array<AtlasRegion> heartBurst;
    private final Array<AtlasRegion> sacBurst;
    private final Array<AtlasRegion> plate;
    private final Array<AtlasRegion> shard;
    private final AtlasRegion[] wreck = new AtlasRegion[2];
    private final AtlasRegion[] wreckRim = new AtlasRegion[2];
    private final Array<AtlasRegion> ventOrange;
    private final Array<AtlasRegion> ventTealFrames;
    private final Array<AtlasRegion> fire;
    private final Array<AtlasRegion> boat;
    private final Array<AtlasRegion> boatDrive;
    private final Array<AtlasRegion> boatTear;
    private final Array<AtlasRegion> boatFlash;
    private final Array<AtlasRegion> shield;
    private final Array<AtlasRegion> vacuumMedium;
    private final Array<AtlasRegion> vacuumLarge;
    /** The vacuum bursts' debris (solid, their frames' timing). */
    private final Array<AtlasRegion> debrisMedium;

    private final Array<AtlasRegion> debrisLarge;

    private final HitFlash hits = new HitFlash();
    /** The turret's heading frame as last drawn (the muzzle's glow follows it). */
    private int lastHeading;

    /** Per part: the step it died (a sac's burst), NEVER while it lives; whether it lived last step. */
    private long[] deathTick = new long[0];

    private boolean[] alive = new boolean[0];
    /** K: its step and which sacs still lived then (they burst with the heart). */
    private long brokenTick = NEVER;

    private final boolean[] sacAtK = new boolean[3];
    /** The step phase 2b (the heart open) started. */
    private long heartOpenTick = NEVER;
    /** The breakout's step, the escape's step and the lifeboat's last place. */
    private long breakoutTick = NEVER;

    private long escapedTick = NEVER;
    private double escapedX;

    private AscendantLooks(Sprites sprites, FlashShader flash, SetPieceSpec piece, JsonValue pivots) {
        this.flash = flash;
        pixel = sprites == null ? null : sprites.pixel;
        spec = piece.boss().orElseThrow();
        escapeSpec = spec.escape().orElseThrow();
        regrowTicks = spec.mirrors().isEmpty()
                ? 1
                : Math.max(1, SimStep.ticks(spec.mirrors().getFirst().mirror().regrowSeconds()));
        cannon = part(piece, "cannon");
        gens = new int[] {part(piece, "gen-l"), part(piece, "gen-r")};
        batteries = new int[] {part(piece, "bat-1"), part(piece, "bat-2"), part(piece, "bat-3"), part(piece, "bat-4")};
        sacs = new int[] {part(piece, "sac-1"), part(piece, "sac-2"), part(piece, "sac-3")};
        heart = escapeSpec.breaksOn();
        lifeboat = escapeSpec.part();
        float[] anchor = pivots.get("hull").get("anchor").asFloatArray();
        anchorX = anchor[0];
        anchorY = anchor[1];
        JsonValue keelPivots = pivots.get("keel");
        keelX = keelPivots.get("offset").getFloat(0);
        keelY = keelPivots.get("offset").getFloat(1);
        cracksX = keelPivots.get("cracks_offset").getFloat(0);
        cracksY = keelPivots.get("cracks_offset").getFloat(1);
        keelFrames = keelPivots.getInt("frames");
        keelSeconds = keelPivots.getDouble("seconds");
        JsonValue lamps = pivots.get("lamps");
        redLamps = places(lamps.get("red"));
        goldLamps = places(lamps.get("gold"));
        lampSeconds = lamps.getDouble("slide_in");
        ports = places(pivots.get("ports"));
        bays = places(pivots.get("bays"));
        racks = places(pivots.get("racks"));
        rackLockX = pivots.get("rack_lock").getFloat(0);
        rackLockY = pivots.get("rack_lock").getFloat(1);
        JsonValue turretPivots = pivots.get("cannon");
        turretCell = turretPivots.getFloat("cell");
        JsonValue muzzleList = turretPivots.get("muzzle");
        muzzles = new float[muzzleList.size][];
        int k = 0;
        for (JsonValue m = muzzleList.child; m != null; m = m.next, k++) {
            muzzles[k] = m.asFloatArray();
        }
        JsonValue frames = pivots.get("panels").get("frames");
        panelHeadings = new int[frames.size];
        panelFrom = new double[frames.size];
        panelTurn = new double[frames.size];
        panelRadius = new double[frames.size];
        k = 0;
        for (JsonValue f = frames.child; f != null; f = f.next, k++) {
            panelHeadings[k] = f.getInt("heading");
            float[] t0 = f.get("tips").get(0).asFloatArray();
            float[] t1 = f.get("tips").get(1).asFloatArray();
            double half = MirrorLooks.PLATE_SIZE / 2.0;
            // The cell's px are y down; the angles here are y up.
            double a0 = Math.atan2(-(t0[1] - half), t0[0] - half);
            double a1 = Math.atan2(-(t1[1] - half), t1[0] - half);
            panelFrom[k] = a0;
            panelTurn[k] = Math.IEEEremainder(a1 - a0, 2 * Math.PI);
            panelRadius[k] = f.getDouble("radius");
        }
        JsonValue chain = pivots.get("chain");
        chainT = new double[chain.size];
        chainX = new double[chain.size];
        chainY = new double[chain.size];
        chainLarge = new boolean[chain.size];
        k = 0;
        for (JsonValue c = chain.child; c != null; c = c.next, k++) {
            chainT[k] = c.getDouble("t");
            chainX[k] = c.getDouble("dx");
            chainY[k] = -c.getDouble("dy");
            chainLarge[k] = c.getString("size").equals("large");
        }
        JsonValue wreckPivots = pivots.get("wreck");
        for (int side = 0; side < 2; side++) {
            JsonValue half = wreckPivots.get(side == 0 ? "left" : "right");
            halfX[side] = half.get("offset").getFloat(0);
            halfY[side] = half.get("offset").getFloat(1);
            driftX[side] = half.get("drift").getDouble(0);
            driftY[side] = -half.get("drift").getDouble(1);
            fracture[side] = points(half.get("fracture"));
            JsonValue ventList = half.get("vents");
            vents[side] = new double[ventList.size][];
            ventTeal[side] = new boolean[ventList.size];
            int v = 0;
            for (JsonValue vent = ventList.child; vent != null; vent = vent.next, v++) {
                vents[side][v] = new double[] {vent.getDouble(0), vent.getDouble(1), vent.getDouble(2)};
                ventTeal[side][v] = vent.getString(3).equals("teal");
            }
            fires[side] = points(half.get("fires"));
        }
        drive = points(pivots.get("lifeboat").get("drive"));
        String r = SLUG;
        hull = single(sprites, r + "-hull");
        keel = optional(sprites, r + "-keel");
        keelCracks = optional(sprites, r + "-keel-cracks");
        bayL = optional(sprites, r + "-bay-l");
        bayR = optional(sprites, r + "-bay-r");
        bayLight = single(sprites, r + "-bay-light");
        rackL = optional(sprites, r + "-rack-l");
        rackR = optional(sprites, r + "-rack-r");
        rackLock = single(sprites, r + "-rack-lock");
        portFlash = optional(sprites, r + "-port-flash");
        lampRed = single(sprites, r + "-lamp-red");
        lampGold = single(sprites, r + "-lamp-gold");
        cannonBase = optional(sprites, r + "-cannon-base");
        turret = optional(sprites, r + "-cannon");
        muzzle = optional(sprites, r + "-muzzle");
        generator = optional(sprites, r + "-generator");
        generatorGlow = single(sprites, r + "-generator-glow");
        beam = optional(sprites, r + "-beam");
        battery = optional(sprites, r + "-battery");
        panel = optional(sprites, r + "-panel");
        panelGlow = optional(sprites, r + "-panel-glow");
        panelCracks = optional(sprites, r + "-panel-cracks");
        panelRim = optional(sprites, r + "-panel-rim");
        panelDown = optional(sprites, r + "-panel-down");
        panelSweep = single(sprites, r + "-panel-sweep");
        sac = optional(sprites, r + "-sac");
        sacDead = single(sprites, r + "-sac-dead");
        sacCore = single(sprites, r + "-sac-core");
        sacHeal = single(sprites, r + "-sac-heal");
        sacFlare = single(sprites, r + "-sac-flare");
        heartFrames = optional(sprites, r + "-heart");
        heartCore = single(sprites, r + "-heart-core");
        heartHeal = single(sprites, r + "-heart-heal");
        heartBurst = optional(sprites, r + "-heart-burst");
        sacBurst = optional(sprites, r + "-sac-burst");
        plate = optional(sprites, r + "-plate");
        shard = optional(sprites, r + "-shard");
        wreck[0] = single(sprites, r + "-wreck-l");
        wreck[1] = single(sprites, r + "-wreck-r");
        wreckRim[0] = single(sprites, r + "-wreck-l-rim");
        wreckRim[1] = single(sprites, r + "-wreck-r-rim");
        ventOrange = optional(sprites, r + "-vent-orange");
        ventTealFrames = optional(sprites, r + "-vent-teal");
        fire = optional(sprites, r + "-fire");
        boat = optional(sprites, r + "-lifeboat");
        boatDrive = optional(sprites, r + "-lifeboat-drive");
        boatTear = optional(sprites, r + "-lifeboat-tear");
        boatFlash = optional(sprites, r + "-lifeboat-flash");
        shield = optional(sprites, EnemyShieldLooks.ROOT + "-medium");
        vacuumMedium = optional(sprites, "explosion-vacuum-medium");
        vacuumLarge = optional(sprites, "explosion-vacuum-large");
        debrisMedium = optional(sprites, "explosion-vacuum-debris-medium");
        debrisLarge = optional(sprites, "explosion-vacuum-debris-large");
        reset();
    }

    /** The looks of the level's escaping boss (the Ascendant: a boss with an {@code escape}) with its pivots; else null. */
    static AscendantLooks of(Sprites sprites, FlashShader flash, SetPieceSpec piece, JsonValue pivots) {
        if (piece == null
                || pivots == null
                || pivots.get("hull") == null
                || pivots.get("wreck") == null
                || piece.boss().flatMap(BossSpec::escape).isEmpty()
                || !sprites.has(SLUG + "-hull")) {
            return null;
        }
        return new AscendantLooks(sprites, flash, piece, pivots);
    }

    /** For the tests: the looks following a sortie of {@code piece} without its sprites (they draw nothing). */
    static AscendantLooks following(SetPieceSpec piece, JsonValue pivots) {
        return new AscendantLooks(null, null, piece, pivots);
    }

    private static int part(SetPieceSpec piece, String name) {
        for (int p = 0; p < piece.parts().size(); p++) {
            if (piece.parts().get(p).name().equals(name)) {
                return p;
            }
        }
        return -1;
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    private static AtlasRegion single(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.region(name) : null;
    }

    private static double[][] points(JsonValue list) {
        double[][] points = new double[list == null ? 0 : list.size][];
        int k = 0;
        for (JsonValue point = list == null ? null : list.child; point != null; point = point.next, k++) {
            points[k] = point.asDoubleArray();
        }
        return points;
    }

    /** Pivot offsets from the centre (y down) as offsets y up. */
    private static double[][] places(JsonValue list) {
        double[][] places = points(list);
        for (double[] place : places) {
            place[1] = -place[1];
        }
        return places;
    }

    /** The level restarts (or the fight is retried from its checkpoint): no deaths, no K, no escape. */
    void reset() {
        Arrays.fill(deathTick, NEVER);
        Arrays.fill(alive, true);
        brokenTick = NEVER;
        Arrays.fill(sacAtK, false);
        heartOpenTick = NEVER;
        breakoutTick = NEVER;
        escapedTick = NEVER;
        hits.reset();
    }

    // --- the rules ---------------------------------------------------------------------------------

    /** The 64-heading frame facing from (x, y) toward (tx, ty), y up (0 straight down, clockwise). */
    static int headingTo(double x, double y, double tx, double ty) {
        return EnemyLooks.heading(Math.atan2(-(tx - x), -(ty - y)), HEADINGS);
    }

    /** The panel frame of a facing (radians clockwise from straight down): the nearest of the panels' headings. */
    static int panelFrame(double facing, int[] headings) {
        int want = EnemyLooks.heading(facing, HEADINGS);
        int best = 0;
        int bestGap = Integer.MAX_VALUE;
        for (int j = 0; j < headings.length; j++) {
            int gap = Math.abs(headings[j] - want);
            gap = Math.min(gap, HEADINGS - gap);
            if (gap < bestGap) {
                bestGap = gap;
                best = j;
            }
        }
        return best;
    }

    /** A generator's, battery's or the cannon base's state: 0 whole, 1 damaged (below half), 2 dead. */
    static int damageState(double hp, double max) {
        if (hp <= 0) {
            return 2;
        }
        return hp < max / 2 ? 1 : 0;
    }

    /** The keel's frame {@code seconds} into the eruption: -1 through the shudder, then 0..frames-1 over the rest. */
    static int keelFrame(double seconds, int frames, double eruptSeconds) {
        if (seconds < SHUDDER_SECONDS) {
            return -1;
        }
        return (int) Math.min(frames - 1, Math.floor((seconds - SHUDDER_SECONDS) / (eruptSeconds / frames)));
    }

    /** The keel cracks' frame {@code seconds} into the eruption: rising over the shudder, then held flaring. */
    static int cracksFrame(double seconds, int frames) {
        return (int) Math.min(frames - 1, Math.max(0, Math.floor(seconds / SHUDDER_SECONDS * frames)));
    }

    /** The keel cracks' strength: full through the shudder, fading over the erupting frames. */
    static float cracksStrength(double seconds, double eruptSeconds) {
        if (seconds < SHUDDER_SECONDS) {
            return 1;
        }
        return (float) Math.clamp(1 - (seconds - SHUDDER_SECONDS) / eruptSeconds, 0, 1);
    }

    /** How far the wreck's halves have drifted, 0–1, {@code seconds} after the flash of a drift {@code total} s long. */
    static double driftShare(double seconds, double total) {
        double t = Math.clamp(seconds / total, 0, 1);
        return t * t * (3 - 2 * t) * 0.8 + 0.2 * t;
    }

    /** The lifeboat's tilt frame (of {@code frames}, centred on upright) at sideways speed {@code vx}, px/s. */
    static int tiltFrame(double vx, int frames) {
        int middle = frames / 2;
        return Math.clamp(Math.round(vx / TILT_SPEED), -middle, middle) + middle;
    }

    /** The frame {@code seconds} into a set of {@code frames} at {@code fps}; -1 once it has run. */
    static int runFrame(double seconds, double fps, int frames) {
        if (seconds < 0) {
            return -1;
        }
        int frame = (int) Math.floor(seconds * fps);
        return frame < frames ? frame : -1;
    }

    /**
     * How far a fracture strays from the straight line between its ends at most, px: the halves part along a jagged
     * cut (round 72 A.11), never a straight edge.
     */
    static double jaggedness(double[][] line) {
        double[] a = line[0];
        double[] b = line[line.length - 1];
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double length = Math.hypot(dx, dy);
        double most = 0;
        for (double[] p : line) {
            most = Math.max(most, Math.abs((p[0] - a[0]) * dy - (p[1] - a[1]) * dx) / length);
        }
        return most;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** The escaping boss among the set pieces (the Ascendant), or null. */
    static SetPiece ascendant(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.escape().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** After each simulation step: the parts' deaths, K, the heart's opening, the breakout, the escape. */
    void stepped(Sortie sortie) {
        SetPiece piece = ascendant(sortie);
        if (piece == null) {
            return;
        }
        int count = piece.partCount();
        if (deathTick.length != count) {
            deathTick = new long[count];
            alive = new boolean[count];
            Arrays.fill(deathTick, NEVER);
            Arrays.fill(alive, true);
        }
        long tick = sortie.tick();
        EscapeRun run = piece.escape().orElseThrow();
        if (!run.broken() && brokenTick != NEVER) {
            // A retry from the boss checkpoint brings it back whole.
            reset();
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case HULL_BROKEN -> {
                    brokenTick = tick;
                    for (int s = 0; s < sacs.length; s++) {
                        sacAtK[s] = sacs[s] >= 0 && alive[sacs[s]];
                    }
                }
                case LIFEBOAT_AWAY -> breakoutTick = tick;
                case BOSS_ESCAPED -> {
                    escapedTick = tick;
                    escapedX = events.x(i);
                }
                default -> {}
            }
        }
        for (int p = 0; p < count; p++) {
            boolean now = piece.partHp(p) > 0;
            if (alive[p] && !now && brokenTick == NEVER) {
                deathTick[p] = tick;
            }
            alive[p] = now;
        }
        if (piece.phase() >= 2 && heartOpenTick == NEVER) {
            heartOpenTick = tick;
        } else if (piece.phase() < 2) {
            heartOpenTick = NEVER;
        }
    }

    /** Whether the hull has broken (K) in what it follows. */
    boolean broken() {
        return brokenTick != NEVER;
    }

    // --- drawing -----------------------------------------------------------------------------------

    private static void additive(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    }

    private static void normal(SpriteBatch batch) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static double seconds(long ticks) {
        return ticks * SimStep.SECONDS;
    }

    /** Draws {@code region} with its top left at ({@code fx}, {@code fy}) of the hull's frame (y down) round (cx, cy). */
    private void drawInFrame(SpriteBatch batch, TextureRegion region, double cx, double cy, double fx, double fy) {
        batch.draw(
                region, Math.round(X0 + cx - anchorX + fx), Math.round(cy + anchorY - fy - region.getRegionHeight()));
    }

    private boolean drawn() {
        return hull != null;
    }

    /** Over the air units: the hull, its parts, panels, bays and racks; the eruption and phase 2; K, the wreck, the lifeboat. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, float whiteFlash) {
        SetPiece piece = ascendant(sortie);
        if (piece == null || !drawn() || !piece.present()) {
            return;
        }
        long tick = sortie.tick();
        double cx = Math.round(piece.renderX(alpha));
        double cy = Math.round(piece.renderY(alpha));
        EscapeRun run = piece.escape().orElseThrow();
        batch.setColor(Color.WHITE);
        if (run.broken()) {
            double k = run.brokenSeconds() + alpha * SimStep.SECONDS;
            if (k < escapeSpec.flashSeconds()) {
                drawPhase2(batch, piece, cx, cy, tick, whiteFlash, true);
            } else {
                drawHalves(batch, cx, cy, k - escapeSpec.flashSeconds());
            }
            drawChain(batch, cx, cy, k, false);
            drawLifeboat(batch, piece, run, alpha, tick, whiteFlash);
            return;
        }
        if (piece.phase() == 0) {
            drawPhase1(batch, piece, cx, cy, tick, whiteFlash, sortie, alpha);
        } else {
            drawPhase2(batch, piece, cx, cy, tick, whiteFlash, false);
        }
    }

    private void drawPhase1(
            SpriteBatch batch,
            SetPiece piece,
            double cx,
            double cy,
            long tick,
            float whiteFlash,
            Sortie sortie,
            float alpha) {
        var parts = piece.spec().parts();
        batch.draw(hull, Math.round(X0 + cx - anchorX), Math.round(cy + anchorY - hull.getRegionHeight()));
        for (int g : gens) {
            if (g >= 0 && !generator.isEmpty()) {
                int state = damageState(piece.partHp(g), parts.get(g).hp());
                drawPart(batch, piece, g, generator.get(state), cx, cy, tick, whiteFlash, state < 2);
            }
        }
        PartMirrors mirrors = piece.mirrors().orElse(null);
        for (int b : batteries) {
            if (b < 0 || battery.isEmpty()) {
                continue;
            }
            int state = damageState(piece.partHp(b), parts.get(b).hp());
            drawPart(batch, piece, b, battery.get(state), cx, cy, tick, whiteFlash, state < 2);
            if (state == 2 || mirrors == null || !mirrors.fitted(b) || panel.isEmpty()) {
                continue;
            }
            int j = panelFrame(mirrors.facing(b), panelHeadings);
            double px = cx + piece.partOffsetX(b);
            double py = cy + piece.partOffsetY(b);
            if (mirrors.down(b)) {
                if (j < panelDown.size) {
                    drawCentred(batch, panelDown.get(j), px, py);
                }
                continue;
            }
            int regrow = mirrors.regrowTicks(b);
            if (regrow > 0) {
                batch.setColor(1, 1, 1, (float) MirrorLooks.regrowShare(regrow, regrowTicks));
            }
            drawCentred(batch, panel.get(j), px, py);
            batch.setColor(Color.WHITE);
        }
        if (cannon >= 0 && !cannonBase.isEmpty()) {
            int state = damageState(piece.partHp(cannon), parts.get(cannon).hp());
            double px = cx + piece.partOffsetX(cannon);
            double py = cy + piece.partOffsetY(cannon);
            drawCentred(batch, cannonBase.get(Math.min(state, cannonBase.size - 1)), px, py);
            if (state < 2 && !turret.isEmpty()) {
                int frame = turretHeading(piece, sortie, alpha, px, py);
                lastHeading = frame;
                drawPart(batch, piece, cannon, turret.get(frame), cx, cy, tick, whiteFlash, true);
            }
        }
        BossPlaybook book = piece.playbook().orElse(null);
        PhasePlaybook.Kind kind = book == null ? null : book.kind();
        int doors = moveOpening(book, PhasePlaybook.Kind.STREAM, 3);
        if (doors > 0) {
            for (int i = 0; i < bays.length && i < 2; i++) {
                Array<AtlasRegion> set = i == 0 ? bayL : bayR;
                if (!set.isEmpty()) {
                    drawCentred(batch, set.get(Math.min(doors, set.size - 1)), cx + bays[i][0], cy + bays[i][1]);
                }
            }
        }
        int hatches = moveOpening(book, PhasePlaybook.Kind.SALVO, 3);
        if (hatches > 0 && kind == PhasePlaybook.Kind.SALVO) {
            for (int i = 0; i < racks.length && i < 2; i++) {
                Array<AtlasRegion> set = i == 0 ? rackL : rackR;
                if (!set.isEmpty() && gens[i] >= 0 && piece.partHp(gens[i]) > 0) {
                    drawCentred(batch, set.get(Math.min(hatches, set.size - 1)), cx + racks[i][0], cy + racks[i][1]);
                }
            }
        }
    }

    /** The turret's heading frame: toward the ship, holding a told line's direction while the cannon's line runs. */
    private int turretHeading(SetPiece piece, Sortie sortie, float alpha, double px, double py) {
        BossLines lines = piece.lines().orElse(null);
        if (lines != null && lines.running() && lines.part() == cannon) {
            return EnemyLooks.heading(Math.atan2(-lines.dirX(), -lines.dirY()), HEADINGS);
        }
        return headingTo(px, py, sortie.ship().renderX(alpha), sortie.ship().renderY(alpha));
    }

    /**
     * How far a move's doors or hatches stand open (0 shut .. {@code open}): opening over the move's tell while it is
     * of {@code kind}, open while it runs.
     */
    private static int moveOpening(BossPlaybook book, PhasePlaybook.Kind kind, int open) {
        if (book == null || book.kind() != kind) {
            return 0;
        }
        if (book.mode() == BossPlaybook.Mode.RUN) {
            return open;
        }
        return (int) Math.min(open, Math.floor(book.tellShare() * (open + 1)));
    }

    /** A part's sprite at its place, flashing white on a hit ({@code flashes}: a living part). */
    private void drawPart(
            SpriteBatch batch,
            SetPiece piece,
            int p,
            TextureRegion region,
            double cx,
            double cy,
            long tick,
            float whiteFlash,
            boolean flashes) {
        double px = cx + piece.partOffsetX(p);
        double py = cy + piece.partOffsetY(p);
        if (flashes && flash != null && hits.on(p, tick, piece.partTicksSinceHit(p))) {
            flash.draw(batch, region, Math.round(X0 + px), Math.round(py), HIT_WHITE, HitFlash.PART_FLASH * whiteFlash);
            return;
        }
        drawCentred(batch, region, px, py);
    }

    /** The eruption's place: its seconds while the phase-2 opening runs, else NaN (before it or after it). */
    private static double eruptionSeconds(SetPiece piece) {
        return piece.phase() == 1 && piece.opening() ? piece.phaseSeconds() : Double.NaN;
    }

    private void drawPhase2(
            SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, float whiteFlash, boolean broken) {
        batch.draw(hull, Math.round(X0 + cx - anchorX), Math.round(cy + anchorY - hull.getRegionHeight()));
        double erupting = eruptionSeconds(piece);
        int keelFrame = Double.isNaN(erupting) ? keelFrames - 1 : keelFrame(erupting, keelFrames, keelSeconds);
        for (int g : gens) {
            if (g >= 0 && generator.size > 3) {
                drawCentred(
                        batch,
                        generator.get(keelFrame >= keelFrames - 1 ? 3 : 2),
                        cx + piece.partOffsetX(g),
                        cy + piece.partOffsetY(g));
            }
        }
        for (int b : batteries) {
            if (b >= 0 && battery.size > 2) {
                drawCentred(batch, battery.get(2), cx + piece.partOffsetX(b), cy + piece.partOffsetY(b));
            }
        }
        if (cannon >= 0 && cannonBase.size > 2) {
            drawCentred(batch, cannonBase.get(2), cx + piece.partOffsetX(cannon), cy + piece.partOffsetY(cannon));
        }
        if (keelFrame >= 0 && keelFrame < keel.size) {
            drawInFrame(batch, keel.get(keelFrame), cx, cy, keelX, keelY);
        }
        if (keelFrame < 3 || sac.isEmpty()) {
            return;
        }
        int brooding = broodingSac(piece);
        BossPlaybook book = piece.playbook().orElse(null);
        for (int s = 0; s < sacs.length; s++) {
            int p = sacs[s];
            if (p < 0) {
                continue;
            }
            double px = cx + piece.partOffsetX(p);
            double py = cy + piece.partOffsetY(p);
            if (broken || piece.partHp(p) <= 0) {
                if (sacDead != null) {
                    drawCentred(batch, sacDead, px, py);
                }
                continue;
            }
            int frame = 0;
            if (s == brooding && book != null) {
                frame = book.mode() == BossPlaybook.Mode.RUN
                        ? SAC_SPLIT
                        : 1 + (int) Math.min(2, Math.floor(book.tellShare() * 3));
            }
            drawPart(batch, piece, p, sac.get(Math.min(frame, sac.size - 1)), cx, cy, tick, whiteFlash, true);
        }
        if (heart >= 0 && !heartFrames.isEmpty()) {
            int frame = broken ? HEART_OPEN : heartFrame(tick);
            drawPart(
                    batch,
                    piece,
                    heart,
                    heartFrames.get(Math.min(frame, heartFrames.size - 1)),
                    cx,
                    cy,
                    tick,
                    whiteFlash,
                    !broken);
        }
    }

    /** The heart's frame: sealed, then opening over {@value #HEART_OPEN_SECONDS} s from its phase's start, open. */
    private int heartFrame(long tick) {
        if (heartOpenTick == NEVER) {
            return 0;
        }
        double since = seconds(tick - heartOpenTick);
        return since >= HEART_OPEN_SECONDS ? HEART_OPEN : 1 + (int) Math.min(1, since / HEART_OPEN_SECONDS * 2);
    }

    /** The sac (0..2) swelling or splitting for the brood now (the first living one), -1 for none. */
    private int broodingSac(SetPiece piece) {
        BossPlaybook book = piece.playbook().orElse(null);
        if (book == null || book.kind() != PhasePlaybook.Kind.BROOD || book.mode() == BossPlaybook.Mode.REST) {
            return -1;
        }
        for (int s = 0; s < sacs.length; s++) {
            if (sacs[s] >= 0 && piece.partHp(sacs[s]) > 0) {
                return s;
            }
        }
        return -1;
    }

    /**
     * K's chain down the keel (the pivots' {@code chain}): each burst's debris (solid, {@code bursts} false) or its flash
     * and ring (additive, {@code bursts} true), the debris blowing two gold plates outward.
     */
    private void drawChain(SpriteBatch batch, double cx, double cy, double k, boolean bursts) {
        for (int i = 0; i < chainT.length; i++) {
            double dt = k - chainT[i];
            Array<AtlasRegion> set = bursts
                    ? (chainLarge[i] ? vacuumLarge : vacuumMedium)
                    : (chainLarge[i] ? debrisLarge : debrisMedium);
            int frame = runFrame(dt, CHAIN_FPS, set.size);
            if (frame < 0) {
                continue;
            }
            double bx = cx + chainX[i];
            double by = cy + chainY[i];
            drawCentred(batch, set.get(frame), bx, by);
            if (bursts || plate.isEmpty()) {
                continue;
            }
            double reach = 20 + 90 * dt;
            for (int j = 0; j < 2; j++) {
                // A fixed spread per burst: its plates fly out at angles set by its index.
                double angle = (i * 2.399 + j * Math.PI) % (2 * Math.PI);
                int pf = (int) (Math.floor(dt * BURST_FPS) + j) % plate.size;
                drawCentred(batch, plate.get(pf), bx + Math.cos(angle) * reach, by + Math.sin(angle) * reach);
            }
        }
    }

    /** The wreck's halves on the fracture, {@code t} s after the flash, drifting apart, charred shards off the cut. */
    private void drawHalves(SpriteBatch batch, double cx, double cy, double t) {
        double u = driftShare(t, driftSeconds());
        for (int side = 0; side < 2; side++) {
            if (wreck[side] == null) {
                continue;
            }
            double ox = halfX[side] + driftX[side] * u;
            // The drift is y up; the frame's y is down.
            double oy = halfY[side] - driftY[side] * u;
            drawInFrame(batch, wreck[side], cx, cy, ox, oy);
            if (shard.isEmpty()) {
                continue;
            }
            double[][] line = fracture[side];
            int sign = side == 0 ? 1 : -1;
            for (int j = 0; j < 5; j++) {
                double[] at = line[(line.length / 4 + j * 3) % line.length];
                double out = (8 + 30 * u) * (0.6 + 0.12 * j);
                double fx = ox + at[0] + sign * out;
                double fy = oy + at[1] + ((j * 7) % 20 - 10) + 20 * u;
                int frame = (int) (Math.floor(t * BURST_FPS) + j) % shard.size;
                drawCentred(batch, shard.get(frame), cx - anchorX + fx, cy + anchorY - fy);
            }
        }
    }

    /** The halves drift from the flash to the escape's end. */
    private double driftSeconds() {
        return escapeSpec.breakoutSeconds()
                - escapeSpec.flashSeconds()
                + escapeSpec.holdSeconds()
                + escapeSpec.climbSeconds();
    }

    /** The lifeboat while it is out: its tilt by its sideways speed, a hit flash. */
    private void drawLifeboat(SpriteBatch batch, SetPiece piece, EscapeRun run, float alpha, long tick, float white) {
        if (!run.out() || boat.isEmpty()) {
            return;
        }
        double x = run.x(alpha);
        double y = run.y(alpha);
        int frame = tiltFrame((run.x() - run.x(0)) / SimStep.SECONDS, boat.size);
        drawPart(
                batch,
                piece,
                lifeboat,
                boat.get(frame),
                x - piece.partOffsetX(lifeboat),
                y - piece.partOffsetY(lifeboat),
                tick,
                white,
                true);
    }

    // --- the glows -------------------------------------------------------------------------------------

    /** After the grade, additive: the lights, beams, shield, panels' glows, tells, cores, bursts, wreck and lifeboat. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = ascendant(sortie);
        if (piece == null || !drawn()) {
            return;
        }
        long tick = sortie.tick();
        double now = seconds(tick) + alpha * SimStep.SECONDS;
        additive(batch);
        batch.setColor(Color.WHITE);
        if (piece.present()) {
            double cx = Math.round(piece.renderX(alpha));
            double cy = Math.round(piece.renderY(alpha));
            EscapeRun run = piece.escape().orElseThrow();
            if (run.broken()) {
                drawBrokenGlows(batch, piece, run, cx, cy, alpha, tick, now);
            } else if (piece.phase() == 0) {
                drawPhase1Glows(batch, piece, cx, cy, tick, now);
            } else {
                drawPhase2Glows(batch, piece, cx, cy, tick, now);
            }
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    private void drawPhase1Glows(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, double now) {
        // The running lights, lit bow first over the slide-in.
        double lit = Math.clamp(piece.bossSeconds() / lampSeconds, 0, 1);
        if (lampRed != null) {
            batch.setColor(1, 1, 1, 0.8f);
            for (int j = 0; j < redLamps.length; j++) {
                if ((double) j / redLamps.length < lit) {
                    drawCentred(batch, lampRed, cx + redLamps[j][0], cy + redLamps[j][1]);
                }
            }
        }
        if (lampGold != null) {
            batch.setColor(1, 1, 1, 0.7f);
            for (int j = 0; j < goldLamps.length; j++) {
                if ((double) j / goldLamps.length < lit) {
                    drawCentred(batch, lampGold, cx + goldLamps[j][0], cy + goldLamps[j][1]);
                }
            }
        }
        batch.setColor(Color.WHITE);
        boolean cannonLives = cannon >= 0 && piece.partHp(cannon) > 0;
        double canX = cannon < 0 ? cx : cx + piece.partOffsetX(cannon);
        double canY = cannon < 0 ? cy : cy + piece.partOffsetY(cannon);
        for (int g : gens) {
            if (g < 0 || piece.partHp(g) <= 0) {
                continue;
            }
            double gx = cx + piece.partOffsetX(g);
            double gy = cy + piece.partOffsetY(g);
            if (cannonLives && !beam.isEmpty()) {
                batch.setColor(1, 1, 1, (float) (0.8 * lit));
                drawBeam(batch, beam.get((int) Math.floor(now * BEAM_FPS) % beam.size), gx, gy, canX, canY);
            }
            if (generatorGlow != null) {
                batch.setColor(1, 1, 1, (float) (0.8 + 0.2 * Math.sin(now * 5)));
                drawCentred(batch, generatorGlow, gx, gy);
            }
        }
        batch.setColor(Color.WHITE);
        drawShield(batch, piece, canX, canY, tick);
        drawPanelGlows(batch, piece, cx, cy, now);
        BossPlaybook book = piece.playbook().orElse(null);
        if (book != null && book.mode() == BossPlaybook.Mode.TELL) {
            float share = (float) book.tellShare();
            if (book.kind() == PhasePlaybook.Kind.VOLLEY && !portFlash.isEmpty()) {
                batch.setColor(1, 1, 1, share);
                int frame = (int) (tick / PORT_TICKS % portFlash.size);
                for (double[] port : ports) {
                    drawCentred(batch, portFlash.get(frame), cx + port[0], cy + port[1]);
                }
            }
            if (book.kind() == PhasePlaybook.Kind.SALVO && rackLock != null) {
                batch.setColor(1, 1, 1, share);
                for (int i = 0; i < racks.length && i < 2; i++) {
                    if (gens[i] >= 0 && piece.partHp(gens[i]) > 0 && !rackL.isEmpty()) {
                        // The lock light at the rack's lock point (px from the rack's top left, y down).
                        double rx = cx + racks[i][0] - rackL.first().getRegionWidth() / 2.0 + rackLockX;
                        double ry = cy + racks[i][1] + rackL.first().getRegionHeight() / 2.0 - rackLockY;
                        drawCentred(batch, rackLock, rx, ry);
                    }
                }
            }
        }
        int doors = moveOpening(book, PhasePlaybook.Kind.STREAM, 3);
        if (doors > 0 && bayLight != null) {
            batch.setColor(1, 1, 1, doors / 3f);
            for (double[] bay : bays) {
                drawCentred(batch, bayLight, cx + bay[0], cy + bay[1]);
            }
        }
        batch.setColor(Color.WHITE);
        // The bow's muzzle over the spinal lance's tell, held through its told line.
        if (cannonLives && !muzzle.isEmpty() && !turret.isEmpty()) {
            BossLines lines = piece.lines().orElse(null);
            boolean line = lines != null && lines.running() && lines.part() == cannon && lines.telegraphing();
            boolean telling = book != null
                    && book.mode() == BossPlaybook.Mode.TELL
                    && book.kind() != null
                    && PhasePlaybook.lance(book.kind());
            if (line || telling) {
                int frame = line
                        ? muzzle.size - 1
                        : (int) Math.min(muzzle.size - 1, Math.floor(book.tellShare() * muzzle.size));
                int heading = lines != null && lines.running() && lines.part() == cannon
                        ? EnemyLooks.heading(Math.atan2(-lines.dirX(), -lines.dirY()), HEADINGS)
                        : lastHeading;
                float[] m = muzzles[Math.min(heading, muzzles.length - 1)];
                drawCentred(batch, muzzle.get(frame), canX - turretCell / 2.0 + m[0], canY + turretCell / 2.0 - m[1]);
            }
        }
    }

    /** The cannon's gold hex shield (the units' medium shield set), dimming with its share. */
    private void drawShield(SpriteBatch batch, SetPiece piece, double x, double y, long tick) {
        PartShields shields = piece.shields().orElse(null);
        if (shields == null
                || cannon < 0
                || !shields.fitted(cannon)
                || shields.shield(cannon) <= 0
                || shield.isEmpty()) {
            return;
        }
        normal(batch);
        batch.setColor(1, 1, 1, EnemyShieldLooks.holdOpacity(shields.share(cannon)));
        drawCentred(batch, shield.get((int) (tick / EnemyShieldLooks.HOLD_FRAME_TICKS % shield.size)), x, y);
        batch.setColor(Color.WHITE);
        additive(batch);
    }

    /** Each standing panel's heat glow, its burst tell's red rim and its slow sweep (the shimmer when it grows back). */
    private void drawPanelGlows(SpriteBatch batch, SetPiece piece, double cx, double cy, double now) {
        PartMirrors mirrors = piece.mirrors().orElse(null);
        BatteryBursts bursts = piece.bursts().orElse(null);
        if (mirrors == null || panel.isEmpty()) {
            return;
        }
        // M10 round 80 fixes, R80a: the battery barrage's tell lights every living panel's rim together.
        BossPlaybook book = piece.playbook().orElse(null);
        float barrage =
                book != null && book.mode() == BossPlaybook.Mode.TELL && book.kind() == PhasePlaybook.Kind.BARRAGE
                        ? (float) book.tellShare()
                        : 0;
        for (int i = 0; i < batteries.length; i++) {
            int b = batteries[i];
            if (b < 0 || !mirrors.fitted(b) || piece.partHp(b) <= 0 || mirrors.down(b)) {
                continue;
            }
            int j = panelFrame(mirrors.facing(b), panelHeadings);
            double px = cx + piece.partOffsetX(b);
            double py = cy + piece.partOffsetY(b);
            double heat = mirrors.heatShare(b);
            if (heat > 0 && j < panelGlow.size) {
                MirrorLooks.heatColour(heat, HEAT);
                batch.setColor(HEAT.r, HEAT.g, HEAT.b, MirrorLooks.heatOpacity(heat));
                drawCentred(batch, panelGlow.get(j), px, py);
            }
            float cracks = MirrorLooks.crackOpacity(heat);
            if (cracks > 0 && j < panelCracks.size) {
                // The cracks dark over the hot plate (from 70 % of its heat), as the review composites them.
                normal(batch);
                batch.setColor(CRACK_DARK.r, CRACK_DARK.g, CRACK_DARK.b, cracks);
                drawCentred(batch, panelCracks.get(j), px, py);
                additive(batch);
            }
            if (bursts != null && (bursts.telling(b) || barrage > 0) && j < panelRim.size) {
                batch.setColor(1, 1, 1, Math.max(barrage, (float) bursts.tellShare(b)));
                drawCentred(batch, panelRim.get(j), px, py);
            }
            if (panelSweep != null) {
                boolean regrowing = mirrors.regrowTicks(b) > 0;
                double u = regrowing
                        ? MirrorLooks.regrowShare(mirrors.regrowTicks(b), regrowTicks)
                        : ((now + i * SWEEP_PHASE) / SWEEP_SECONDS) % 1.0;
                double angle = panelFrom[j] + panelTurn[j] * Math.clamp(u, 0, 1);
                batch.setColor(1, 1, 1, regrowing ? 1 : SWEEP_GLINT);
                drawCentred(
                        batch,
                        panelSweep,
                        px + Math.cos(angle) * panelRadius[j],
                        py + Math.sin(angle) * panelRadius[j]);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawPhase2Glows(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick, double now) {
        double erupting = eruptionSeconds(piece);
        if (!Double.isNaN(erupting) && !keelCracks.isEmpty()) {
            batch.setColor(1, 1, 1, cracksStrength(erupting, keelSeconds));
            int frame = cracksFrame(erupting, keelCracks.size);
            AtlasRegion cracks = keelCracks.get(frame);
            drawInFrame(batch, cracks, cx, cy, cracksX, cracksY);
            batch.setColor(Color.WHITE);
        }
        int keelFrame = Double.isNaN(erupting) ? keelFrames - 1 : keelFrame(erupting, keelFrames, keelSeconds);
        PartRegens regens = piece.regens().orElse(null);
        BossPlaybook book = piece.playbook().orElse(null);
        // The grafted mounts' muzzles at the dead generators over their tell.
        if (book != null
                && book.mode() == BossPlaybook.Mode.TELL
                && book.kind() == PhasePlaybook.Kind.MOUNTS
                && !portFlash.isEmpty()) {
            batch.setColor(1, 1, 1, (float) book.tellShare());
            int frame = (int) (tick / PORT_TICKS % portFlash.size);
            for (int g : gens) {
                if (g >= 0) {
                    drawCentred(batch, portFlash.get(frame), cx + piece.partOffsetX(g), cy + piece.partOffsetY(g));
                }
            }
            batch.setColor(Color.WHITE);
        }
        drawSacBursts(batch, piece, cx, cy, tick);
        if (keelFrame < 3) {
            return;
        }
        int brooding = broodingSac(piece);
        for (int s = 0; s < sacs.length; s++) {
            int p = sacs[s];
            if (p < 0 || piece.partHp(p) <= 0) {
                continue;
            }
            double px = cx + piece.partOffsetX(p);
            double py = cy + piece.partOffsetY(p);
            if (sacCore != null) {
                batch.setColor(1, 1, 1, (float) (0.7 + 0.3 * Math.sin(now * 6 + s)));
                drawCentred(batch, sacCore, px, py);
            }
            if (regens != null && regens.healing(p) && sacHeal != null) {
                batch.setColor(1, 1, 1, (float) (0.5 + 0.5 * Math.sin(now * 9)));
                drawCentred(batch, sacHeal, px, py);
            }
            if (s == brooding && book.mode() == BossPlaybook.Mode.TELL && sacFlare != null) {
                batch.setColor(1, 1, 1, (float) book.tellShare());
                drawCentred(batch, sacFlare, px, py);
            }
        }
        if (heart >= 0 && piece.partHp(heart) > 0) {
            double hx = cx + piece.partOffsetX(heart);
            double hy = cy + piece.partOffsetY(heart);
            if (heartCore != null) {
                double open = heartFrame(tick) == HEART_OPEN ? 1 : 0.4;
                batch.setColor(1, 1, 1, (float) (open * (0.75 + 0.25 * Math.sin(now * 4))));
                drawCentred(batch, heartCore, hx, hy);
            }
            if (regens != null && regens.healing(heart) && heartHeal != null) {
                batch.setColor(1, 1, 1, (float) (0.5 + 0.5 * Math.sin(now * 9)));
                drawCentred(batch, heartHeal, hx, hy);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** A sac's burst where it died (before K). */
    private void drawSacBursts(SpriteBatch batch, SetPiece piece, double cx, double cy, long tick) {
        for (int p : sacs) {
            if (p < 0 || p >= deathTick.length || deathTick[p] == NEVER) {
                continue;
            }
            int frame = runFrame(seconds(tick - deathTick[p]), BURST_FPS, sacBurst.size);
            if (frame >= 0) {
                drawCentred(batch, sacBurst.get(frame), cx + piece.partOffsetX(p), cy + piece.partOffsetY(p));
            }
        }
    }

    private void drawBrokenGlows(
            SpriteBatch batch,
            SetPiece piece,
            EscapeRun run,
            double cx,
            double cy,
            float alpha,
            long tick,
            double now) {
        double k = run.brokenSeconds() + alpha * SimStep.SECONDS;
        drawChain(batch, cx, cy, k, true);
        int heartFrame = runFrame(k, BURST_FPS, heartBurst.size);
        if (heartFrame >= 0 && heart >= 0) {
            drawCentred(
                    batch, heartBurst.get(heartFrame), cx + piece.partOffsetX(heart), cy + piece.partOffsetY(heart));
        }
        int sacFrame = runFrame(k, BURST_FPS, sacBurst.size);
        for (int s = 0; s < sacs.length && sacFrame >= 0; s++) {
            if (sacAtK[s] && sacs[s] >= 0) {
                drawCentred(
                        batch,
                        sacBurst.get(sacFrame),
                        cx + piece.partOffsetX(sacs[s]),
                        cy + piece.partOffsetY(sacs[s]));
            }
        }
        double t = k - escapeSpec.flashSeconds();
        if (t >= 0) {
            double total = driftSeconds();
            double u = driftShare(t, total);
            float fade = (float) Math.clamp(1 - t / total, 0, 1);
            for (int side = 0; side < 2; side++) {
                double ox = halfX[side] + driftX[side] * u;
                double oy = halfY[side] - driftY[side] * u;
                if (wreckRim[side] != null) {
                    batch.setColor(1, 1, 1, 0.5f + 0.5f * fade);
                    drawInFrame(batch, wreckRim[side], cx, cy, ox, oy);
                }
                for (int v = 0; v < vents[side].length; v++) {
                    Array<AtlasRegion> set = ventTeal[side][v] ? ventTealFrames : ventOrange;
                    if (set.isEmpty()) {
                        continue;
                    }
                    double[] vent = vents[side][v];
                    double a = Math.toRadians(vent[2]);
                    AtlasRegion frame = set.get((int) (Math.floor(now * BURST_FPS) + v) % set.size);
                    double vx = cx - anchorX + ox + vent[0] + Math.sin(a) * 22;
                    double vy = cy + anchorY - (oy + vent[1] - Math.cos(a) * 22);
                    batch.setColor(1, 1, 1, 0.8f * (1 - 0.6f * (1 - fade)));
                    float w = frame.getRegionWidth();
                    float h = frame.getRegionHeight();
                    batch.draw(
                            frame,
                            Math.round(X0 + vx - w / 2),
                            Math.round(vy - h / 2),
                            w / 2,
                            h / 2,
                            w,
                            h,
                            1,
                            1,
                            (float) -vent[2]);
                }
                if (!fire.isEmpty()) {
                    batch.setColor(1, 1, 1, 0.9f);
                    for (int f = 0; f < fires[side].length; f++) {
                        AtlasRegion frame = fire.get((int) (Math.floor(now * FIRE_FPS) + f * 2) % fire.size);
                        double fx = ox + fires[side][f][0];
                        double fy = oy + fires[side][f][1];
                        drawCentred(batch, frame, cx - anchorX + fx, cy + anchorY - fy);
                    }
                }
            }
            batch.setColor(Color.WHITE);
        }
        if (breakoutTick != NEVER) {
            int tear = runFrame(seconds(tick - breakoutTick) + alpha * SimStep.SECONDS, BURST_FPS, boatTear.size);
            if (tear >= 0) {
                drawCentred(batch, boatTear.get(tear), escapeSpec.fromX(), escapeSpec.fromY());
            }
        }
        if (!run.out()) {
            return;
        }
        double x = run.x(alpha);
        double y = run.y(alpha);
        if (!boatDrive.isEmpty() && !boat.isEmpty()) {
            int tilt = tiltFrame((run.x() - run.x(0)) / SimStep.SECONDS, boat.size);
            double[] d = drive[Math.min(tilt, drive.length - 1)];
            AtlasRegion b = boat.get(tilt);
            drawCentred(
                    batch,
                    boatDrive.get((int) Math.floor(now * BURST_FPS) % boatDrive.size),
                    x - b.getRegionWidth() / 2.0 + d[0],
                    y + b.getRegionHeight() / 2.0 - d[1]);
        }
    }

    /** The generator's gold beam from (x0, y0) to (x1, y1): the strip tiled along the line. */
    private void drawBeam(SpriteBatch batch, AtlasRegion strip, double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.hypot(dx, dy);
        float w = strip.getRegionWidth();
        float h = strip.getRegionHeight();
        float rotation = (float) (Math.toDegrees(Math.atan2(dy, dx)) - 90);
        int pieces = (int) Math.ceil(length / h);
        for (int i = 0; i < pieces; i++) {
            double t = (i + 0.5) * h / length;
            double mx = x0 + dx * Math.min(1, t);
            double my = y0 + dy * Math.min(1, t);
            batch.draw(strip, (float) (X0 + mx - w / 2), (float) (my - h / 2), w / 2, h / 2, w, h, 1, 1, rotation);
        }
    }

    // --- the eruption's shake ----------------------------------------------------------------------------

    /** The eruption's shake at its strongest, times the ridge pass's tremble (3 px at the full setting). */
    static final double ERUPTION_SHAKE = 1.5;

    /** The eruption's shake strength {@code seconds} into it: rising through the shudder, full, easing out at its end. */
    static double eruptionShake(double seconds, double total) {
        if (!(seconds >= 0) || seconds >= total) {
            return 0;
        }
        double in = Math.min(1, 0.5 + 0.5 * seconds / SHUDDER_SECONDS);
        double out = Math.min(1, (total - seconds) / 0.5);
        return in * out;
    }

    /** The play field's sideways shake now, px at the full setting: the eruption's (design: "the screen shake"). */
    float shakeX(Sortie sortie, double realSeconds) {
        SetPiece piece = ascendant(sortie);
        if (piece == null || !piece.present()) {
            return 0;
        }
        double strength = eruptionShake(eruptionSeconds(piece), SHUDDER_SECONDS + keelSeconds);
        return strength <= 0 ? 0 : (float) (ERUPTION_SHAKE * strength) * ScreenShake.ridge(1, realSeconds);
    }

    // --- the lance --------------------------------------------------------------------------------------

    /** With the bullets: the spinal lance's red line over its telegraph (unsteady until its lock), then its beam. */
    void drawLines(SpriteBatch batch, Sortie sortie, float alpha) {
        SetPiece piece = ascendant(sortie);
        if (piece == null || pixel == null || !piece.present()) {
            return;
        }
        BossLines lines = piece.lines().orElse(null);
        if (lines == null) {
            return;
        }
        long tick = sortie.tick();
        additive(batch);
        // M10 round 80 fixes, R80a: the lance sweep's way, a fading fan of thin lines over its arc, from its tell
        // (toward
        // the ship) through its line's telegraph (from the locked line).
        BossPlaybook book = piece.playbook().orElse(null);
        PhasePlaybook.Move told = book == null ? null : book.moveSpec();
        if (told != null && told.kind() == PhasePlaybook.Kind.SWEEP && !lines.live()) {
            double sweep = told.sweepRadians() * book.side();
            double ox;
            double oy;
            double heading;
            float share;
            if (lines.telegraphing() && lines.sweep() != 0) {
                ox = lines.fromX() - lines.stepX() * (1 - alpha);
                oy = lines.fromY() - lines.stepY() * (1 - alpha);
                heading = Math.atan2(lines.dirY(), lines.dirX());
                share = 1;
            } else if (book.mode() == BossPlaybook.Mode.TELL && cannon >= 0) {
                ox = piece.partX(cannon);
                oy = piece.partY(cannon);
                heading = Math.atan2(
                        sortie.ship().renderY(alpha) - oy, sortie.ship().renderX(alpha) - ox);
                share = (float) book.tellShare();
            } else {
                share = 0;
                ox = oy = heading = 0;
            }
            Color red = LineLooks.TELEGRAPH;
            for (int k = 1; k <= SWEEP_GHOSTS && share > 0; k++) {
                double a = heading + sweep * k / SWEEP_GHOSTS;
                batch.setColor(red.r, red.g, red.b, share * SWEEP_GHOST_OPACITY * (1 - 0.6f * (k - 1) / SWEEP_GHOSTS));
                stroke(batch, ox, oy, Math.cos(a), Math.sin(a), 1);
            }
        }
        if (lines.running() && lines.attack() >= 0) {
            drawLance(
                    batch,
                    lines.attack(),
                    lines.fromX() - lines.stepX() * (1 - alpha),
                    lines.fromY() - lines.stepY() * (1 - alpha),
                    lines.dirX(),
                    lines.dirY(),
                    lines.telegraphing(),
                    lines.live(),
                    lines.charge(),
                    tick);
        }
        if (lines.twinRunning() && lines.twinAttack() >= 0) {
            // R80a: the twin lance from the other flank.
            drawLance(
                    batch,
                    lines.twinAttack(),
                    lines.twinFromX() - lines.twinStepX() * (1 - alpha),
                    lines.twinFromY() - lines.twinStepY() * (1 - alpha),
                    lines.twinDirX(),
                    lines.twinDirY(),
                    lines.twinTelegraphing(),
                    lines.twinLive(),
                    lines.twinCharge(),
                    tick);
        }
        batch.setColor(Color.WHITE);
        normal(batch);
    }

    /** R80a: the sweep's way: its fading lines over the arc. */
    private static final int SWEEP_GHOSTS = 5;

    private static final float SWEEP_GHOST_OPACITY = 0.45f;

    /** One told line (the lance, the sweep, a twin): its red line over its telegraph, then its beam. */
    private void drawLance(
            SpriteBatch batch,
            int attack,
            double ox,
            double oy,
            double dx,
            double dy,
            boolean telegraphing,
            boolean live,
            double charge,
            long tick) {
        EnemySpec.LaserLine line = spec.attacks().get(attack).line().orElse(null);
        if (line == null) {
            return;
        }
        Color red = LineLooks.TELEGRAPH;
        if (telegraphing) {
            boolean locked = !line.tracks() || charge >= HonourGuardLooks.lockShare(line);
            float opacity = LineLooks.telegraphOpacity(charge, tick);
            if (!locked) {
                opacity *= tick / 3 % 2 == 0 ? 0.8f : 0.55f;
            }
            batch.setColor(red.r, red.g, red.b, opacity);
            stroke(batch, ox, oy, dx, dy, locked ? LineLooks.TELEGRAPH_WIDTH : 1);
        } else if (live) {
            batch.setColor(LANCE.r, LANCE.g, LANCE.b, LineLooks.RAIL_OPACITY);
            stroke(batch, ox, oy, dx, dy, (float) Math.max(LineLooks.CORE_WIDTH + 2, line.bandPx()));
            batch.setColor(LineLooks.CORE);
            stroke(batch, ox, oy, dx, dy, LineLooks.CORE_WIDTH + 2);
        }
    }

    /** A line {@code width} px wide from (x, y) along (dx, dy) past the screen's edge. */
    private void stroke(SpriteBatch batch, double x, double y, double dx, double dy, float width) {
        float length = (float) (2 * (PlayField.WIDTH + PlayField.HEIGHT));
        batch.draw(pixel, (float) (X0 + x), (float) (y - width / 2), 0, width / 2, length, width, 1, 1, (float)
                Math.toDegrees(Math.atan2(dy, dx)));
    }

    // --- over the HUD -------------------------------------------------------------------------------

    /** Whether the boss bar is hidden: from K on (round 80: the escape is a short scene, no bar). */
    boolean hidesBar(Sortie sortie) {
        SetPiece piece = ascendant(sortie);
        return piece != null && piece.present() && piece.escape().orElseThrow().broken();
    }

    /** The lifeboat's flash toward the gate at {@code ESCAPED} (additive), over the HUD at the top edge (step H4b). */
    void drawEscapeFlash(SpriteBatch batch, Sortie sortie) {
        if (escapedTick == NEVER || boatFlash.size == 0 || ascendant(sortie) == null) {
            return;
        }
        long tick = sortie.tick();
        int frame = runFrame(seconds(tick - escapedTick), BURST_FPS, boatFlash.size);
        if (frame >= 0) {
            additive(batch);
            batch.setColor(Color.WHITE);
            drawCentred(batch, boatFlash.get(frame), escapedX, PlayField.HEIGHT - ESCAPE_FLASH_Y);
            normal(batch);
        }
    }
}
